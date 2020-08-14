package org.hisp.dhis.predictor;

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.ERROR;
import static org.hisp.dhis.system.util.ValidationUtils.dataValueIsZeroAndInsignificant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.*;
import org.hisp.dhis.commons.collection.CachingMap;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.datavalue.DataExportParams;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.expression.Expression;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.expression.MissingValueStrategy;
import org.hisp.dhis.jdbc.batchhandler.DataValueBatchHandler;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.AnalyticsType;
import org.hisp.dhis.program.ProgramIndicator;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.parameters.PredictorJobParameters;
import org.hisp.dhis.system.notification.NotificationLevel;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.util.DateUtils;
import org.hisp.quick.BatchHandler;
import org.hisp.quick.BatchHandlerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Jim Grace
 */
@Service( "org.hisp.dhis.predictor.PredictionService" )
@Transactional
public class DefaultPredictionService
    implements PredictionService
{
    private static final Log log = LogFactory.getLog( DefaultPredictionService.class );

    private final PredictorService predictorService;

    private final ConstantService constantService;

    private final ExpressionService expressionService;

    private final DataValueService dataValueService;

    private final CategoryService categoryService;

    private final OrganisationUnitService organisationUnitService;

    private final PeriodService periodService;

    private final IdentifiableObjectManager idObjectManager;

    private AnalyticsService analyticsService;

    private final Notifier notifier;

    private final BatchHandlerFactory batchHandlerFactory;

    private CurrentUserService currentUserService;

    public DefaultPredictionService( PredictorService predictorService, ConstantService constantService,
        ExpressionService expressionService, DataValueService dataValueService, CategoryService categoryService,
        OrganisationUnitService organisationUnitService, PeriodService periodService,
        IdentifiableObjectManager idObjectManager, AnalyticsService analyticsService, Notifier notifier,
        BatchHandlerFactory batchHandlerFactory, CurrentUserService currentUserService )
    {
        checkNotNull( predictorService );
        checkNotNull( constantService );
        checkNotNull( expressionService );
        checkNotNull( dataValueService );
        checkNotNull( categoryService );
        checkNotNull( periodService );
        checkNotNull( idObjectManager );
        checkNotNull( analyticsService );
        checkNotNull( notifier );
        checkNotNull( batchHandlerFactory );
        checkNotNull( currentUserService );

        this.predictorService = predictorService;
        this.constantService = constantService;
        this.expressionService = expressionService;
        this.dataValueService = dataValueService;
        this.categoryService = categoryService;
        this.organisationUnitService = organisationUnitService;
        this.periodService = periodService;
        this.idObjectManager = idObjectManager;
        this.analyticsService = analyticsService;
        this.notifier = notifier;
        this.batchHandlerFactory = batchHandlerFactory;
        this.currentUserService = currentUserService;
    }

    /**
     * Used only for testing, remove when test is refactored
     */
    @Deprecated
    public void setAnalyticsService( AnalyticsService analyticsService )
    {
        this.analyticsService = analyticsService;
    }

    /**
     * Used only for testing, remove when test is refactored
     */
    @Deprecated
    public void setCurrentUserService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    // -------------------------------------------------------------------------
    // Prediction business logic
    // -------------------------------------------------------------------------

    private final static String NON_AOC = ""; // String that is not an Attribute Option Combo

    @Override
    public PredictionSummary predictJob( PredictorJobParameters params, JobConfiguration jobId )
    {
        Date startDate = DateUtils.getDateAfterAddition( new Date(), params.getRelativeStart() );
        Date endDate = DateUtils.getDateAfterAddition( new Date(), params.getRelativeEnd() );

        return predictTask( startDate, endDate, params.getPredictors(), params.getPredictorGroups(), jobId );
    }

    @Override
    public PredictionSummary predictTask( Date startDate, Date endDate,
        List<String> predictors, List<String> predictorGroups, JobConfiguration jobId )
    {
        PredictionSummary predictionSummary;

        try
        {
            notifier.notify( jobId, NotificationLevel.INFO, "Making predictions", false );

            predictionSummary = predictInternal( startDate, endDate, predictors, predictorGroups );

            notifier.update( jobId, NotificationLevel.INFO, "Prediction done", true )
                .addJobSummary( jobId, predictionSummary, PredictionSummary.class );
        }
        catch ( RuntimeException ex )
        {
            log.error( DebugUtils.getStackTrace( ex ) );

            predictionSummary = new PredictionSummary( PredictionStatus.ERROR, "Predictions failed: " + ex.getMessage() );

            notifier.update( jobId, ERROR, predictionSummary.getDescription(), true );
        }

        return predictionSummary;
    }

    private PredictionSummary predictInternal( Date startDate, Date endDate, List<String> predictors, List<String> predictorGroups )
    {
        List<Predictor> predictorList = new ArrayList<>();

        if ( CollectionUtils.isEmpty( predictors ) && CollectionUtils.isEmpty( predictorGroups ) )
        {
            predictorList = predictorService.getAllPredictors();
        }
        else
        {
            if ( !CollectionUtils.isEmpty( predictors ) )
            {
                predictorList = idObjectManager.get( Predictor.class, predictors );
            }

            if ( !CollectionUtils.isEmpty( predictorGroups ) )
            {
                List<PredictorGroup> predictorGroupList = idObjectManager.get( PredictorGroup.class, predictorGroups );

                for ( PredictorGroup predictorGroup : predictorGroupList )
                {
                    predictorList.addAll( predictorGroup.getMembers() );
                }
            }
        }

        PredictionSummary predictionSummary = new PredictionSummary();

        log.info( "Running " + predictorList.size() + " predictors from " + startDate.toString() + " to " + endDate.toString() );

        for ( Predictor predictor : predictorList )
        {
            predict( predictor, startDate, endDate, predictionSummary );
        }

        log.info( "Finished predictors from " + startDate.toString() + " to " + endDate.toString() + ": " + predictionSummary.toString() );

        return predictionSummary;
    }

    @Override
    public void predict( Predictor predictor, Date startDate, Date endDate, PredictionSummary predictionSummary )
    {
        Expression generator = predictor.getGenerator();
        Expression skipTest = predictor.getSampleSkipTest();
        DataElement outputDataElement = predictor.getOutput();

        Set<String> aggregates = new HashSet<>();
        Set<String> nonAggregates = new HashSet<>();
        expressionService.getAggregatesAndNonAggregatesInExpression( generator.getExpression(), aggregates, nonAggregates );
        Map<String, Double> constantMap = constantService.getConstantMap();
        List<Period> outputPeriods = getPeriodsBetweenDates( predictor.getPeriodType(), startDate, endDate );
        Set<Period> existingOutputPeriods = getExistingPeriods( outputPeriods );
        outputPeriods = periodService.reloadPeriods( outputPeriods );
        Set<Period> outputPeriodSet = new HashSet<>( outputPeriods );
        ListMap<Period, Period> samplePeriodsMap = getSamplePeriodsMap( outputPeriodSet, predictor );
        Set<Period> allSamplePeriods = samplePeriodsMap.uniqueValues();
        Set<Period> existingSamplePeriods = getExistingPeriods(  new ArrayList<>( allSamplePeriods ) );
        Set<DimensionalItemObject> aggregateDimensionItems = getDimensionItems( aggregates, skipTest );
        Set<DimensionalItemObject> nonAggregateDimensionItems = getDimensionItems( nonAggregates, null );
        User currentUser = currentUserService.getCurrentUser();
        Set<String> defaultOptionComboAsSet = Sets.newHashSet( categoryService.getDefaultCategoryOptionCombo().getUid() );
        Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> emptyMap4 = new Map4<>();
        MapMapMap<Period, String, DimensionalItemObject, Double> emptyMapMapMap = new MapMapMap<>();
        boolean usingAttributeOptions = hasAttributeOptions( aggregateDimensionItems ) || hasAttributeOptions( nonAggregateDimensionItems );
        CachingMap<String, CategoryOptionCombo> cocMap = new CachingMap<>();

        CategoryOptionCombo outputOptionCombo = predictor.getOutputCombo() == null ?
            categoryService.getDefaultCategoryOptionCombo() : predictor.getOutputCombo();

        DimensionalItemObject predictionReference = getPredictionReference( outputDataElement, outputOptionCombo, aggregateDimensionItems );
        Date now = new Date();
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<>();
        String storedBy = "system-process";

        if ( currentUser != null )
        {
            currentUserOrgUnits = currentUser.getOrganisationUnits();
            storedBy = currentUser.getUsername();
        }

        predictionSummary.incrementPredictors();

        for ( OrganisationUnitLevel orgUnitLevel : predictor.getOrganisationUnitLevels() )
        {
            List<OrganisationUnit> orgUnitsAtLevel = organisationUnitService.getOrganisationUnitsAtOrgUnitLevels(
                Lists.newArrayList( orgUnitLevel ), currentUserOrgUnits );

            if ( orgUnitsAtLevel.size() == 0 )
            {
                continue;
            }

            List<List<OrganisationUnit>> orgUnitLists = Lists.partition(orgUnitsAtLevel, 500);

            for ( List<OrganisationUnit> orgUnits : orgUnitLists )
            {
                Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> aggregateDataMap4 =
                    aggregateDimensionItems.isEmpty() ? emptyMap4 :
                        getDataValues( aggregateDimensionItems, allSamplePeriods, existingSamplePeriods, orgUnits );

                Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> nonAggregateDataMap4 =
                    nonAggregateDimensionItems.isEmpty() ? emptyMap4 :
                        getDataValues( nonAggregateDimensionItems, outputPeriodSet, existingOutputPeriods, orgUnits );

                List<DataValue> predictions = new ArrayList<>();

                for ( OrganisationUnit orgUnit : orgUnits )
                {
                    MapMapMap<Period, String, DimensionalItemObject, Double> aggregateDataMap =
                        firstNonNull( aggregateDataMap4.get( orgUnit ), new MapMapMap<>() ); // New Map because carryPredictionForward may add to it.

                    MapMapMap<Period, String, DimensionalItemObject, Double> nonAggregateDataMap =
                        firstNonNull( nonAggregateDataMap4.get( orgUnit ), emptyMapMapMap );

                    applySkipTest( aggregateDataMap, skipTest, constantMap );

                    for ( Period period : outputPeriods )
                    {
                        ListMapMap<String, String, Double> aggregateSampleMap = getAggregateSamples( aggregateDataMap,
                            aggregates, samplePeriodsMap.get( period ), constantMap, generator.getMissingValueStrategy() );

                        MapMap<String, DimensionalItemObject, Double> nonAggregateSampleMap = firstNonNull(
                            nonAggregateDataMap.get( period ), new MapMap<>() );

                        Set<String> attributeOptionCombos = usingAttributeOptions ?
                            Sets.union( aggregateSampleMap.keySet(), nonAggregateSampleMap.keySet() ) : defaultOptionComboAsSet;

                        if ( attributeOptionCombos.isEmpty() && generator.getMissingValueStrategy() == MissingValueStrategy.NEVER_SKIP )
                        {
                            attributeOptionCombos = defaultOptionComboAsSet;
                        }

                        ListMap<String, Double> aggregateSampleMapNonAoc = aggregateSampleMap.get( NON_AOC );

                        Map<DimensionalItemObject, Double> nonAggregateSampleMapNonAoc = nonAggregateSampleMap.get( NON_AOC );

                        for ( String aoc : attributeOptionCombos )
                        {
                            if ( NON_AOC.compareTo( aoc ) == 0 )
                            {
                                continue;
                            }

                            ListMap<String, Double> aggregateValueMap = ListMap.union( aggregateSampleMap.get( aoc ), aggregateSampleMapNonAoc );

                            Map<DimensionalItemObject, Double> nonAggregateValueMap = combine( nonAggregateSampleMap.get( aoc ), nonAggregateSampleMapNonAoc );

                            Double value = expressionService.getExpressionValueRegEx( generator, nonAggregateValueMap,
                                constantMap, null, period.getDaysInPeriod(), aggregateValueMap );

                            carryPredictionForward( value, period, predictionReference, aoc, aggregateDataMap );

                            if ( value != null && !value.isNaN() && !value.isInfinite() &&
                                !dataValueIsZeroAndInsignificant( Double.toString( value ), outputDataElement ) )
                            {
                                String valueString = outputDataElement.getValueType().isInteger() ?
                                    Long.toString( Math.round( value ) ) :
                                    Double.toString( MathUtils.roundFraction( value, 4 ) );

                                predictions.add( new DataValue( outputDataElement,
                                    period, orgUnit, outputOptionCombo,
                                    cocMap.get( aoc, () -> categoryService.getCategoryOptionCombo( aoc ) ),
                                    valueString, storedBy, now, null ) );
                            }
                        }
                    }
                }

                writePredictions( predictions, outputDataElement, outputOptionCombo,
                    outputPeriodSet, existingOutputPeriods, orgUnits, storedBy, predictionSummary );
            }
        }
    }

    private Map<DimensionalItemObject, Double> combine ( Map<DimensionalItemObject, Double> a, Map<DimensionalItemObject, Double> b )
    {
        if ( a == null || a.isEmpty() )
        {
            if ( b == null || b.isEmpty() )
            {
                return new HashMap<>();
            }
            else
            {
                return b;
            }
        }
        else if ( b == null || b.isEmpty() )
        {
            return a;
        }

        Map<DimensionalItemObject, Double> c = new HashMap<>( a );

        for (Map.Entry<DimensionalItemObject, Double> entry : b.entrySet() )
        {
            c.put(entry.getKey(), entry.getValue() );
        }

        return c;
    }

    /**
     * Gets all DimensionalItemObjects from the expressions and skip test.
     *
     * @param expressions set of expressions.
     * @param skipTest the skip test expression (if any).
     * @return set of all dimensional item objects found in all expressions.
     */
    private Set<DimensionalItemObject> getDimensionItems( Set<String> expressions, Expression skipTest )
    {
        Set<DimensionalItemObject> operands = new HashSet<>();

        for ( String expression : expressions )
        {
            operands.addAll( expressionService.getDimensionalItemObjectsInExpression( expression ) );
        }

        if ( skipTest != null )
        {
            operands.addAll( expressionService.getDimensionalItemObjectsInExpression( skipTest.getExpression() ) );
        }

        return operands;
    }

    /**
     * Checks to see if any dimensional item objects in a set have values
     * stored in the database by attribute option combo.
     *
     * @param oSet set of dimensional item objects
     * @return true if any are stored by attribuete option combo.
     */
    private boolean hasAttributeOptions( Set<DimensionalItemObject> oSet )
    {
        for ( DimensionalItemObject o : oSet )
        {
            if ( hasAttributeOptions( o ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if a dimensional item object has values
     * stored in the database by attribute option combo.
     *
     * @param o dimensional item object
     * @return true if values are stored by attribuete option combo.
     */
    private boolean hasAttributeOptions( DimensionalItemObject o )
    {
        return o.getDimensionItemType() != DimensionItemType.PROGRAM_INDICATOR
            || ( (ProgramIndicator)o ).getAnalyticsType() != AnalyticsType.ENROLLMENT;
    }

    /**
     * For a given predictor, orgUnit, and outputPeriod, returns for each
     * attribute option combo and aggregate expression a list of values for
     * the various sample periods.
     *
     * @param dataMap data to be used in evaluating expressions.
     * @param aggregates the aggregate expressions.
     * @param samplePeriods the periods to sample from.
     * @param constantMap any constants used in evaluating expressions.
     * @param missingValueStrategy strategy for sampled period missing values.
     * @return lists of sample values by attributeOptionCombo and expression
     */
    private ListMapMap<String, String, Double> getAggregateSamples (
        MapMapMap<Period, String, DimensionalItemObject, Double> dataMap,
        Collection<String> aggregates, List<Period> samplePeriods,
        Map<String, Double> constantMap, MissingValueStrategy missingValueStrategy )
    {
        ListMapMap<String, String, Double> result = new ListMapMap<>();

        if ( dataMap != null )
        {
            for ( String aggregate : aggregates )
            {
                Expression expression = new Expression( aggregate, "Aggregated", missingValueStrategy );

                for ( Period period : samplePeriods )
                {
                    MapMap<String, DimensionalItemObject, Double> periodValues = dataMap.get( period );

                    if ( periodValues != null )
                    {
                        for ( String aoc : periodValues.keySet() )
                        {
                            Double value = expressionService.getExpressionValueRegEx( expression,
                                periodValues.get( aoc ), constantMap, null, period.getDaysInPeriod() );

                            result.putValue( aoc, aggregate, value );
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Evaluates the skip test expression for any sample periods in which
     * skip test data occurs. For any combination of period and attribute
     * option combo where the skip test is true, removes all sample data with
     * that combination of period and attribute option combo.
     *
     * @param dataMap all data values (both skip and aggregate).
     * @param skipTest the skip test expression.
     * @param constantMap constants to use in skip expression if needed.
     */
    private void applySkipTest( MapMapMap<Period, String, DimensionalItemObject, Double> dataMap,
        Expression skipTest, Map<String, Double> constantMap )
    {
        if ( skipTest != null && dataMap != null )
        {
            for ( Period period : dataMap.keySet() )
            {
                MapMap<String, DimensionalItemObject, Double> periodData = dataMap.get( period );

                for ( String aoc : new ArrayList<String>( periodData.keySet() ) )
                {
                    Double testValue = expressionService.getExpressionValueRegEx( skipTest, periodData.get( aoc ),
                        constantMap, null, period.getDaysInPeriod() );

                    if ( testValue != null && !MathUtils.isZero( testValue ) )
                    {
                        periodData.remove( aoc );
                    }
                }
            }
        }
    }

    /**
     * Returns all Periods of the specified PeriodType with start date after or
     * equal the specified start date and end date before or equal the specified
     * end date. Periods are returned in ascending date order.
     *
     * The periods returned do not need to be in the database.
     *
     * @param periodType the PeriodType.
     * @param startDate the ultimate start date.
     * @param endDate the ultimate end date.
     * @return a list of all Periods with start date after or equal the
     *         specified start date and end date before or equal the specified
     *         end date, or an empty list if no Periods match.
     */
    private List<Period> getPeriodsBetweenDates( PeriodType periodType, Date startDate, Date endDate )
    {
        List<Period> periods = new ArrayList<>();

        Period period = periodType.createPeriod( startDate );

        if ( !period.getStartDate().before( startDate ) && !period.getEndDate().after( endDate ) )
        {
            periods.add( period );
        }

        period = periodType.getNextPeriod( period );

        while ( !period.getEndDate().after( endDate ) )
        {
            periods.add( period );
            period = periodType.getNextPeriod( period );
        }

        return periods;
    }

    /**
     * Creates a map relating each output period to a list of sample periods
     * from which the sample data is to be drawn.
     *
     * @param outputPeriods the output periods
     * @param predictor the predictor
     * @return map from output periods to sample periods
     */
    private ListMap<Period, Period> getSamplePeriodsMap( Set<Period> outputPeriods, Predictor predictor)
    {
        int sequentialCount = predictor.getSequentialSampleCount();
        int annualCount = predictor.getAnnualSampleCount();
        int skipCount = firstNonNull( predictor.getSequentialSkipCount(),  0 );
        PeriodType periodType = predictor.getPeriodType();

        ListMap<Period, Period> samplePeriodsMap = new ListMap<>();

        for ( Period outputPeriod : outputPeriods )
        {
            samplePeriodsMap.put( outputPeriod, new ArrayList<>() );

            Period p = periodType.getPreviousPeriod( outputPeriod, skipCount );

            for ( int i = skipCount; i < sequentialCount; i++ )
            {
                p = periodType.getPreviousPeriod( p );

                samplePeriodsMap.putValue( outputPeriod, p );
            }

            for ( int year = 1; year <= annualCount; year++ )
            {
                Period pPrev = periodType.getPreviousYearsPeriod( outputPeriod, year );
                Period pNext = pPrev;

                samplePeriodsMap.putValue( outputPeriod, pPrev );

                for ( int i = 0; i < sequentialCount; i++ )
                {
                    pPrev = periodType.getPreviousPeriod( pPrev );
                    pNext = periodType.getNextPeriod( pNext );

                    samplePeriodsMap.putValue( outputPeriod, pPrev );
                    samplePeriodsMap.putValue( outputPeriod, pNext );
                }
            }
        }
        return samplePeriodsMap;
    }

    /**
     * Finds the set of periods that exist, from a list of periods.
     *
     * Only adds the period if it is found in the database, because:
     * (a) We will need the period id, and
     * (b) If the period does not exist in the database, then
     *     there is no data in the database to look for.
     *
     * @param periods the periods to look for
     * @return the set of periods that exist, with ids.
     */
    private Set<Period> getExistingPeriods( List<Period> periods )
    {
        Set<Period> existingPeriods = new HashSet<>();

        for ( Period period : periods )
        {
            Period existingPeriod = period.getId() != 0 ? period :
                periodService.getPeriod( period.getStartDate(), period.getEndDate(), period.getPeriodType() );

            if ( existingPeriod != null )
            {
                existingPeriods.add( existingPeriod );
            }
        }
        return existingPeriods;
    }

    /**
     * Checks to see if the output predicted value should be used as input
     * to subsequent (later period) predictions. If so, returns the
     * DimensionalItemObject that should be updated with the predicted value.
     *
     * Note that we make the simplifying assumption that if the output data
     * element is sampled in an expression without a catOptionCombo, the
     * predicted data value will be used. This is usually what the user
     * wants, but would break if the expression assumes a sum of
     * catOptionCombos including the predicted value and other catOptionCombos.
     *
     * @param outputDataElement the data element to output predicted value to.
     * @param outputOptionCombo the option combo to output predicted value to.
     * @param aggregateDimensionItems the aggregate items used in future predictions.
     * @return the DimensionalItemObject, if any, for the predicted value.
     */
    private DimensionalItemObject getPredictionReference( DataElement outputDataElement,
        CategoryOptionCombo outputOptionCombo, Set<DimensionalItemObject> aggregateDimensionItems )
    {
        for ( DimensionalItemObject item : aggregateDimensionItems )
        {
            if ( item == outputDataElement )
            {
                return item;
            }

            if ( item.getDimensionItemType() == DimensionItemType.DATA_ELEMENT_OPERAND
                && ( (DataElementOperand) item ).getDataElement() == outputDataElement
                && ( (DataElementOperand) item ).getCategoryOptionCombo() == outputOptionCombo )
            {
                return item;
            }
        }

        return null;
    }

    /**
     * If the predicted value might be used in a future period prediction,
     * insert it into the period value map.
     *
     * @param value the predicted value.
     * @param period the period the value is predicted for.
     * @param predictionReference the item for the prediction, if any.
     * @param aoc attribute option combo for the predicted value.
     * @param aggregateDataMap the aggregate values data map.
     */
    private void carryPredictionForward( Double value, Period period,
        DimensionalItemObject predictionReference, String aoc,
        MapMapMap<Period, String, DimensionalItemObject, Double> aggregateDataMap )
    {
        if ( value == null || predictionReference == null )
        {
            return;
        }

        MapMap<String, DimensionalItemObject, Double> aocValueMap = aggregateDataMap.get( period );

        if ( aocValueMap == null )
        {
            aocValueMap = new MapMap<>();
            aggregateDataMap.put( period, aocValueMap );
        }

        aocValueMap.putEntry( aoc, predictionReference, value );
    }

    /**
     * Gets data values for a set of DimensionalItemObjects over a set of
     * Periods for an organisation unit and/or any of the organisation unit's
     * descendants.
     *
     * DimensionalItemObjects may reference aggregate and/or event data.
     *
     * Returns the values mapped by Period, then attribute option combo UID,
     * then DimensionalItemObject.
     *
     * @param dimensionItems the dimensionItems.
     * @param allPeriods all data Periods (to fetch event data).
     * @param existingPeriods existing data Periods (to fetch aggregate data).
     * @param orgUnits the roots of the OrganisationUnit trees to include.
     * @return the map of values
     */
    private Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> getDataValues(
        Set<DimensionalItemObject> dimensionItems, Set<Period> allPeriods, Set<Period> existingPeriods,
        List<OrganisationUnit> orgUnits)
    {
        Set<DataElement> dataElements = new HashSet<>();
        Set<DataElementOperand> dataElementOperands = new HashSet<>();
        Set<DimensionalItemObject> eventAttributeOptionObjects = new HashSet<>();
        Set<DimensionalItemObject> eventNonAttributeOptionObjects = new HashSet<>();
        Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> dataValues = new Map4<>();

        for ( DimensionalItemObject o : dimensionItems )
        {
            if ( o instanceof DataElement )
            {
                dataElements.add( (DataElement) o );
            }
            else if ( o instanceof DataElementOperand )
            {
                dataElementOperands.add( (DataElementOperand) o );
            }
            else if ( hasAttributeOptions( o ) )
            {
                eventAttributeOptionObjects.add( o );
            }
            else
            {
                eventNonAttributeOptionObjects.add( o );
            }
        }

        if ( !dataElements.isEmpty() || !dataElementOperands.isEmpty() )
        {
            dataValues = getAggregateDataValues( dataElements, dataElementOperands, existingPeriods, orgUnits );
        }

        if ( !eventAttributeOptionObjects.isEmpty() && !allPeriods.isEmpty() )
        {
            dataValues.putMap( getEventDataValues( eventAttributeOptionObjects, true, allPeriods, orgUnits ) );
        }

        if ( !eventNonAttributeOptionObjects.isEmpty() && !allPeriods.isEmpty() )
        {
            dataValues.putMap( getEventDataValues( eventNonAttributeOptionObjects, false, allPeriods, orgUnits ) );
        }

        return dataValues;
    }

    private Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> getAggregateDataValues(
        Set<DataElement> dataElements, Set<DataElementOperand> dataElementOperands, Set<Period> periods, List<OrganisationUnit> orgUnits )
    {
        DataExportParams params = new DataExportParams();
        params.setDataElements( dataElements );
        params.setDataElementOperands( dataElementOperands );
        params.setPeriods( periods );
        params.setOrganisationUnits( new HashSet<>( orgUnits ) );
        params.setReturnParentOrgUnit( true );

        List<DeflatedDataValue> deflatedDataValues = dataValueService.getDeflatedDataValues( params );

        Map<Long, DataElement> dataElementLookup = dataElements.stream().collect( Collectors.toMap( DataElement::getId, de -> de ) );
        Map<String, DataElementOperand> dataElementOperandLookup = dataElementOperands.stream().collect(
            Collectors.toMap( deo -> deo.getDataElement().getId() + "." + deo.getCategoryOptionCombo().getId(), deo -> deo ) );
        Map<Long, Period> periodLookup = periods.stream().collect( Collectors.toMap( Period::getId, p -> p ) );
        Map<Long, OrganisationUnit> orgUnitLookup = orgUnits.stream().collect( Collectors.toMap( OrganisationUnit::getId, ou -> ou ) );
        Map<Long, CategoryOptionCombo> aocLookup = new HashMap<>();

        Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> dataValues = new Map4<>();

        for ( DeflatedDataValue dv : deflatedDataValues )
        {
            DataElement dataElement = dataElementLookup.get( dv.getDataElementId() );
            DataElementOperand dataElementOperand = dataElementOperandLookup.get( dv.getDataElementId() + "." + dv.getCategoryOptionComboId() );
            Period p = periodLookup.get( dv.getPeriodId() );
            OrganisationUnit orgUnit = orgUnitLookup.get( dv.getSourceId() );
            CategoryOptionCombo attributeOptionCombo = aocLookup.get( dv.getAttributeOptionComboId() );
            String stringValue = dv.getValue();

            if ( stringValue == null )
            {
                continue;
            }

            if ( attributeOptionCombo == null )
            {
                attributeOptionCombo = categoryService.getCategoryOptionCombo( dv.getAttributeOptionComboId() );

                aocLookup.put( dv.getAttributeOptionComboId(), attributeOptionCombo );
            }

            if ( dataElement != null )
            {
                addAggregateDataValue( dataValues, orgUnit, p, attributeOptionCombo, dataElement, stringValue );
            }

            if ( dataElementOperand != null )
            {
                addAggregateDataValue( dataValues, orgUnit, p, attributeOptionCombo, dataElementOperand, stringValue );
            }
        }

        return dataValues;
    }

    private void addAggregateDataValue( Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> dataValues,
        OrganisationUnit orgUnit, Period p, CategoryOptionCombo attributeOptionCombo, DimensionalItemObject dimensionItem,
        String stringValue )
    {
        Double value;

        try
        {
            value = Double.parseDouble( stringValue );
        }
        catch ( NumberFormatException e )
        {
            return; // Ignore any non-numeric values.
        }

        Double valueSoFar = dataValues.getValue( orgUnit, p, attributeOptionCombo.getUid(), dimensionItem );

        if ( valueSoFar != null )
        {
            value += valueSoFar;
        }

        dataValues.putEntry( orgUnit, p, attributeOptionCombo.getUid(), dimensionItem, value );
    }

    /**
     * Gets data values for a set of Event dimensionItems over a set of
     * Periods for a list of organisation units and/or any of the organisation
     * units' descendants.
     *
     * Returns the values mapped by OrganisationUnit, Period, attribute option
     * combo UID, and DimensionalItemObject.
     *
     * @param dimensionItems the dimensionItems.
     * @param periods the Periods of the DataValues.
     * @param orgUnits the roots of the OrganisationUnit trees to include.
     * @return the map of values
     */
    private Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> getEventDataValues(
        Set<DimensionalItemObject> dimensionItems, boolean hasAttributeOptions, Set<Period> periods, List<OrganisationUnit> orgUnits )
    {
        Map4<OrganisationUnit, Period, String, DimensionalItemObject, Double> eventDataValues = new Map4<>();

        DataQueryParams.Builder paramsBuilder = DataQueryParams.newBuilder()
            .withPeriods(new ArrayList<>(periods) )
            .withDataDimensionItems( Lists.newArrayList( dimensionItems ) )
            .withOrganisationUnits( orgUnits );

        if ( hasAttributeOptions )
        {
            paramsBuilder.withAttributeOptionCombos( Lists.newArrayList() );
        }

        Grid grid = analyticsService.getAggregatedDataValues( paramsBuilder.build() );

        int peInx = grid.getIndexOfHeader( DimensionalObject.PERIOD_DIM_ID );
        int dxInx = grid.getIndexOfHeader( DimensionalObject.DATA_X_DIM_ID );
        int ouInx = grid.getIndexOfHeader( DimensionalObject.ORGUNIT_DIM_ID );
        int aoInx = hasAttributeOptions ? grid.getIndexOfHeader( DimensionalObject.ATTRIBUTEOPTIONCOMBO_DIM_ID ) : 0;
        int vlInx = grid.getWidth() - 1;

        Map<String, Period> periodLookup = periods.stream().collect( Collectors.toMap(Period::getIsoDate, p -> p ) );
        Map<String, DimensionalItemObject> dimensionItemLookup = dimensionItems.stream().collect( Collectors.toMap(DimensionalItemObject::getDimensionItem, d -> d ) );
        Map<String, OrganisationUnit> orgUnitLookup = orgUnits.stream().collect( Collectors.toMap(BaseIdentifiableObject::getUid, o -> o ) );

        for ( List<Object> row : grid.getRows() )
        {
            String pe = (String) row.get( peInx );
            String dx = (String) row.get( dxInx );
            String ou = (String) row.get( ouInx );
            String ao = hasAttributeOptions ? (String) row.get( aoInx ) : NON_AOC;
            Double vl = ( (Number)row.get( vlInx ) ).doubleValue();

            Period period = periodLookup.get( pe );
            DimensionalItemObject dimensionItem = dimensionItemLookup.get( dx );
            OrganisationUnit orgUnit = orgUnitLookup.get( ou );

            eventDataValues.putEntry( orgUnit, period, ao, dimensionItem, vl );
        }

        return eventDataValues;
    }

    /**
     * Writes the predicted values to the database. Also updates the
     * prediction summmary per-record counts.
     *
     * @param predictions Predictions to write to the database.
     * @param outputDataElement Predictor output data elmeent.
     * @param outputOptionCombo Predictor output category option commbo.
     * @param periods Periods to predict for.
     * @param existingPeriods Those periods to predict for already in DB.
     * @param orgUnits Organisation units to predict for.
     * @param summary Prediction summary to update.
     */
    private void writePredictions( List<DataValue> predictions, DataElement outputDataElement,
        CategoryOptionCombo outputOptionCombo, Set<Period> periods, Set<Period> existingPeriods,
        List<OrganisationUnit> orgUnits, String storedBy, PredictionSummary summary )
    {
        DataExportParams params = new DataExportParams();
        params.setDataElementOperands( Sets.newHashSet( new DataElementOperand( outputDataElement, outputOptionCombo ) ) );
        params.setPeriods( periods );
        params.setOrganisationUnits( new HashSet<>( orgUnits ) );
        params.setIncludeDeleted( true );

        List<DeflatedDataValue> oldValueList = dataValueService.getDeflatedDataValues( params );

        Map<String, DeflatedDataValue> oldValues = oldValueList.stream().collect( Collectors.toMap(
            d -> d.getPeriodId() + "-" + d.getSourceId() + "-" + d.getAttributeOptionComboId(), d -> d ) );

        BatchHandler<DataValue> dataValueBatchHandler = batchHandlerFactory.createBatchHandler( DataValueBatchHandler.class ).init();

        for ( DataValue newValue : predictions )
        {
            boolean zeroInsignificant = dataValueIsZeroAndInsignificant( newValue.getValue(), newValue.getDataElement() );

            String key = newValue.getPeriod().getId() + "-" + newValue.getSource().getId() + "-" + newValue.getAttributeOptionCombo().getId();

            DeflatedDataValue oldValue = oldValues.get( key );

            if ( oldValue == null )
            {
                if ( zeroInsignificant )
                {
                    continue;
                }

                summary.incrementInserted();

                /*
                 * Note: BatchHandler can be used for inserts only when the
                 * period previously existed. To insert values into new periods
                 * (just added to the database within this transaction), the
                 * dataValueService must be used.
                 */
                if ( existingPeriods.contains( newValue.getPeriod() ) )
                {
                    dataValueBatchHandler.addObject( newValue );
                }
                else
                {
                    dataValueService.addDataValue( newValue );
                }
            }
            else
            {
                if ( newValue.getValue().equals( oldValue.getValue() ) && !oldValue.isDeleted() )
                {
                    summary.incrementUnchanged();
                }
                else
                {
                    if ( zeroInsignificant )
                    {
                        continue; // Leave the old value to be deleted because the new value, insigificant, won't be stored.
                    }

                    summary.incrementUpdated();

                    dataValueBatchHandler.updateObject( newValue );
                }

                oldValues.remove( key );
            }
        }

        Map<Long, OrganisationUnit> orgUnitLookup = orgUnits.stream().collect( Collectors.toMap( OrganisationUnit::getId, o -> o ) );

        for ( DeflatedDataValue oldValue : oldValues.values() )
        {
            summary.incrementDeleted();

            DataValue toDelete = new DataValue( outputDataElement, oldValue.getPeriod(),
                orgUnitLookup.get( oldValue.getSourceId() ), outputOptionCombo,
                categoryService.getCategoryOptionCombo( oldValue.getAttributeOptionComboId() ),
                oldValue.getValue(), storedBy, null, null );

            dataValueBatchHandler.deleteObject( toDelete );
        }

        dataValueBatchHandler.flush();
    }
}
