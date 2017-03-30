package org.hisp.dhis.ivb.aggregation.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import org.hisp.dhis.ivb.util.AggregationManager;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.opensymphony.xwork2.Action;

public class RunAggregationQueryAction
    implements Action
{

    private final static int VACCINE_INTRO_DE_GROUPSET = 1;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private DataValueService dataValueService;

    public DataValueService getDataValueService()
    {
        return dataValueService;
    }

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    @Autowired
    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    @Autowired
    private DataElementService dataElementService;

    public DataElementService getDataElementService()
    {
        return dataElementService;
    }

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    private AggregationManager aggregationManager;

    public void setAggregationManager( AggregationManager aggregationManager )
    {
        this.aggregationManager = aggregationManager;
    }

    private List<DataElement> allDataElementList = new ArrayList<DataElement>();

    public List<DataElement> getAllDataElementList()
    {
        return allDataElementList;
    }

    public void setAllDataElementList( List<DataElement> allDataElementList )
    {
        this.allDataElementList = allDataElementList;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private DeMapping demapping;

    public DeMapping getDemapping()
    {
        return demapping;
    }

    public void setDemapping( DeMapping demapping )
    {
        this.demapping = demapping;
    }

    @Autowired
    private DeMappingService demappingService;

    public DeMappingService getDemappingService()
    {
        return demappingService;
    }

    public void setDemappingService( DeMappingService demappingService )
    {
        this.demappingService = demappingService;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    private List<DataElement> dataElements = new ArrayList<DataElement>();

    public List<DataElement> getDataElements()
    {
        return dataElements;
    }

    private DataElement dataElements1;

    public DataElement getDataElements1()
    {
        return dataElements1;
    }

    public void setDataElements1( DataElement dataElements1 )
    {
        this.dataElements1 = dataElements1;
    }

    private String importStatus = "";

    public String getImportStatus()
    {
        return importStatus;
    }

    private List<DataElementGroup> deGroupList = new ArrayList<DataElementGroup>();

    private String language;

    public String getLanguage()
    {
        return language;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        List<OrganisationUnit> OrganisationUnit = organisationUnitService.getOrganisationUnitByName( "Xtra" );

        Integer greenland_ID = 0;
        Integer Svalbard_and_Jan_Mayen_ID = 0;
        Integer Denmark_ID = 0;
        Integer Norway_ID = 0;
        // Set<org.hisp.dhis.organisationunit.OrganisationUnit>orgunit=
        // (Set<org.hisp.dhis.organisationunit.OrganisationUnit>) new
        // ArrayList<OrganisationUnit>();

        // orgunit.add(arg0);
        Set<OrganisationUnit> orgin = null;
        for ( OrganisationUnit org : OrganisationUnit )
        {
            if ( org.getName().equalsIgnoreCase( "Xtra" ) )
            {
                orgin = org.getChildren();

            }

        }
        for ( OrganisationUnit orgn : orgin )
        {
            if ( orgn.getCode().startsWith( "disputed" ) )
            {
                // System.out.println("orgn" + orgn.getName());
            }
        }
        userName = currentUserService.getCurrentUser().getUsername();
        // System.out.println("orgn");
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        messageCount = (int) messageService.getUnreadMessageConversationCount();
        List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
        if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
        {
            adminStatus = "Yes";
        }
        else
        {
            adminStatus = "No";
        }
        Map<String, Integer> aggregationResultMap = new HashMap<String, Integer>();

        Set<OrganisationUnit> orgUnitList = new HashSet<OrganisationUnit>(
            selectionTreeManager.getReloadedSelectedOrganisationUnits() );

        /*
         * SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyyMM" );
         * String curMonth = simpleDateFormat.format( new Date() ); Period
         * period = PeriodType.getPeriodFromIsoString( curMonth ); period =
         * periodService.reloadPeriod( period );
         */

        // Period period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(),
        // new Date() );
        // period = periodService.reloadPeriod( period );
        // Period period=periodService.getPeriod( 1008 );

        Period period = periodService.getPeriod( 1008 );

        // System.out.println("period"+period);
        Map<Indicator, Map<OrganisationUnit, Integer>> individualKeyFlagResultMap = new HashMap<Indicator, Map<OrganisationUnit, Integer>>();

        // --------------------------------------------------------------------------------
        // KEY FLAG SCORE
        // --------------------------------------------------------------------------------

        /*
         * Set<CaseAggregationCondition> conditions = new
         * HashSet<CaseAggregationCondition>(
         * aggregationConditionService.getCaseAggregationConditionByOperator(
         * Lookup.AGG_TYPE_BY_KEYFLAG_SCORE ) );
         * 
         * CaseAggregationCondition condition1 = conditions.iterator().next();
         * 
         * DataElement dataElement = condition1.getAggregationDataElement();
         * 
         * individualKeyFlagResultMap =
         * aggregationManager.calculateKeyFlagScore( period, dataElement,
         * orgUnitList );
         * 
         * for( Indicator keyFlagIndicator : individualKeyFlagResultMap.keySet()
         * ) { Integer keyFlagScore = 0; Set<AttributeValue> attributeValues =
         * new HashSet<AttributeValue>( keyFlagIndicator.getAttributeValues() );
         * 
         * for ( AttributeValue attributeValue : attributeValues ) { if (
         * attributeValue.getAttribute().getName().equalsIgnoreCase(
         * "Key Flag Score" ) ) { try { keyFlagScore = Integer.parseInt(
         * attributeValue.getValue() ); } catch( Exception e ) { keyFlagScore =
         * 0; } } }
         * 
         * Map<OrganisationUnit, Integer> orgUnitResultMap = new
         * HashMap<OrganisationUnit, Integer>( individualKeyFlagResultMap.get(
         * keyFlagIndicator ) );
         * 
         * for( OrganisationUnit orgUnit : orgUnitList ) { Integer keyFlagValue
         * = orgUnitResultMap.get( orgUnit ); if( keyFlagValue != null &&
         * keyFlagValue == 1 ) { Integer tempSumScore =
         * aggregationResultMap.get( orgUnit.getId()+":"+dataElement.getId() );
         * if( tempSumScore == null ) { tempSumScore = 0; } tempSumScore +=
         * keyFlagScore; aggregationResultMap.put(
         * orgUnit.getId()+":"+dataElement.getId(), tempSumScore ); } } }
         */

        // --------------------------------------------------------------------------------
        // KEY FLAG
        // --------------------------------------------------------------------------------

        /*
         * conditions = new HashSet<CaseAggregationCondition>(
         * aggregationConditionService.getCaseAggregationConditionByOperator(
         * Lookup.AGG_TYPE_BY_KEYFLAG ) );
         * 
         * for ( CaseAggregationCondition condition : conditions ) { dataElement
         * = condition.getAggregationDataElement();
         * 
         * Integer keyFlagIndicatorId = Integer.parseInt(
         * condition.getAggregationExpression() );
         * 
         * Indicator keyFlagIndicator = indicatorService.getIndicator(
         * keyFlagIndicatorId );
         * 
         * //aggregationResultMap.putAll(
         * aggregationManager.calculateKeyFlagCount( period, dataElement,
         * orgUnitList, keyFlagIndicator ) );
         * 
         * Map<OrganisationUnit, Integer> orgUnitResultMap = new
         * HashMap<OrganisationUnit, Integer>( individualKeyFlagResultMap.get(
         * keyFlagIndicator ) ); for( OrganisationUnit orgUnit : orgUnitList ) {
         * Integer keyFlagValue = orgUnitResultMap.get( orgUnit ); if(
         * keyFlagValue != null ) { aggregationResultMap.put(
         * orgUnit.getId()+":"+dataElement.getId(), keyFlagValue ); } } }
         * 
         * 
         * //-------------------------------------------------------------------
         * ------------- // BY OPTION
         * //-------------------------------------------------------------------
         * -------------
         * 
         * conditions = new HashSet<CaseAggregationCondition>(
         * aggregationConditionService.getCaseAggregationConditionByOperator(
         * Lookup.AGG_TYPE_BY_OPTION ) );
         * 
         * for ( CaseAggregationCondition condition : conditions ) { dataElement
         * = condition.getAggregationDataElement();
         * 
         * aggregationResultMap.putAll(
         * aggregationManager.calculateByOptionCount( period, dataElement,
         * orgUnitList, condition.getAggregationExpression() ) ); }
         * 
         * //-------------------------------------------------------------------
         * ------------- // BY OPTION SCORE
         * //-------------------------------------------------------------------
         * -------------
         * 
         * conditions = new HashSet<CaseAggregationCondition>(
         * aggregationConditionService.getCaseAggregationConditionByOperator(
         * Lookup.AGG_TYPE_BY_OPTION_SCORE ) );
         * 
         * for ( CaseAggregationCondition condition : conditions ) { dataElement
         * = condition.getAggregationDataElement();
         * 
         * aggregationResultMap.putAll(
         * aggregationManager.calculateByOptionScore( period, dataElement,
         * orgUnitList, condition.getAggregationExpression() ) ); }
         * 
         * /* if( condition.getOperator().equals( Lookup.AGG_TYPE_BY_OPTION ) )
         * { aggregationResultMap.putAll(
         * aggregationManager.calculateByOptionCount( period, dataElement,
         * orgUnitList, condition.getAggregationExpression() ) ); } else if(
         * condition.getOperator().equals( Lookup.AGG_TYPE_BY_OPTION_SCORE ) ) {
         * aggregationResultMap.putAll(
         * aggregationManager.calculateByOptionScore( period, dataElement,
         * orgUnitList, condition.getAggregationExpression() ) ); } else if(
         * condition.getOperator().equals( Lookup.AGG_TYPE_BY_KEYFLAG ) ) {
         * Integer keyFlagIndicatorId = Integer.parseInt(
         * condition.getAggregationExpression() );
         * 
         * Indicator keyFlagIndicator = indicatorService.getIndicator(
         * keyFlagIndicatorId );
         * 
         * aggregationResultMap.putAll(
         * aggregationManager.calculateKeyFlagCount( period, dataElement,
         * orgUnitList, keyFlagIndicator ) ); }
         */

        /*
         * for( String key : aggregationResultMap.keySet() ) {
         * System.out.println( key + " -- " + aggregationResultMap.get( key ) );
         * }
         */

        importStatus = aggregationManager.importData( aggregationResultMap, period );

        DataElementGroupSet deGroupSet = dataElementService.getDataElementGroupSet( VACCINE_INTRO_DE_GROUPSET );
        deGroupList.addAll( deGroupSet.getMembers() );
        Collections.sort( deGroupList, new IdentifiableObjectNameComparator() );

        for ( DataElementGroup ds : deGroupList )
        {

            if ( ds.getName().equalsIgnoreCase( "Intro Status" ) )
            {
                allDataElementList.addAll( ds.getMembers() );

            }
        }
        // for disputed areas//

        for ( DataElement ds : allDataElementList )
        {
            for ( OrganisationUnit org : orgin )
            {

                demapping = demappingService.getDeMapping( ds.getUid() );

                if ( demapping != null )
                {
                    // System.out.println("dataelement"+demapping);
                    String deelementId = demapping.getMappeddeid();
                    DataElement dataelement = dataElementService.getDataElement( deelementId );
                    // System.out.println("deelementId"+deelementId);

                    DataValue dataValue = dataValueService.getLatestDataValue( dataelement.getId(), 1, org.getId() );
                    // System.out.println("dataValue"+dataValue);
                    if ( dataValue != null )
                    {

                        // System.out.println("code1"+dataValue);

                        dataValueService.deleteDataValue( dataValue );

                    }
                }
            }
        }

        for ( DataElement ds : allDataElementList )
        {
            for ( OrganisationUnit org : orgin )
            {

                demapping = demappingService.getDeMapping( ds.getUid() );

                if ( demapping != null )
                {

                    String deelementId = demapping.getMappeddeid();
                    DataElement dataelement = dataElementService.getDataElement( deelementId );

                    DataElementCategoryOptionCombo optionCombo = categoryService
                        .getDefaultDataElementCategoryOptionCombo();
                    DataValue dataValue = dataValueService.getLatestDataValue( dataelement.getId(), 1, org.getId() );

                    if ( dataValue == null )
                    {

                        dataValue = new DataValue();
                        dataValue.setDataElement( dataelement );
                        dataValue.setPeriod( period );
                        dataValue.setSource( org );
                        dataValue.setCategoryOptionCombo( optionCombo );
                        dataValue.setAttributeOptionCombo( optionCombo );
                        dataValue.setValue( "10" );

                        dataValue.setStatus( 1 );

                        dataValueService.addDataValue( dataValue );

                    }
                }
            }
        }

        /*
         * for ( DataElement ds : allDataElementList ) {
         * 
         * System.out.println( "hjello"+ ds.getName()); System.out.println(
         * "heeeeeello"+ ds.getId());
         * 
         * }
         */

        List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();

        lastLevelOrgUnit.addAll( organisationUnitService.getAllOrganisationUnits() );

        for ( OrganisationUnit org : lastLevelOrgUnit )
        {

            if ( org.getName().equalsIgnoreCase( "Svalbard and Jan Mayen" ) )
            {
                Svalbard_and_Jan_Mayen_ID = org.getId();

            }
            if ( org.getName().equalsIgnoreCase( "Greenland" ) )
            {
                greenland_ID = org.getId();

            }
            if ( org.getName().equalsIgnoreCase( "Denmark" ) )
            {
                Denmark_ID = org.getId();

            }
            if ( org.getName().equalsIgnoreCase( "Norway" ) )
            {
                Norway_ID = org.getId();

            }
        }

        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        Integer count = 0;
        lastLevelOrgUnit.removeAll( orgin );
        // delete all datavalue first//
        for ( DataElement ds : allDataElementList )
        {
            for ( OrganisationUnit org : lastLevelOrgUnit )
            {

                demapping = demappingService.getDeMapping( ds.getUid() );

                if ( demapping != null )
                {
                    // System.out.println("dataelement"+demapping);
                    String deelementId = demapping.getMappeddeid();
                    DataElement dataelement = dataElementService.getDataElement( deelementId );
                    // System.out.println("deelementId"+deelementId);

                    DataValue dataValue = dataValueService.getLatestDataValue( dataelement.getId(), 1, org.getId() );
                    // System.out.println("dataValue"+dataValue);
                    if ( dataValue != null )
                    {
                        count++;
                        // System.out.println("code1"+dataValue);

                        dataValueService.deleteDataValue( dataValue );

                    }
                }
            }
        }

        for ( DataElement ds : allDataElementList )
        {
            for ( OrganisationUnit org : lastLevelOrgUnit )
            {

                demapping = demappingService.getDeMapping( ds.getUid() );

                if ( demapping != null )
                {
                    String code = "";
                    // System.out.println("dataelement"+demapping);
                    String deelementId = demapping.getMappeddeid();
                    DataElement dataelement = dataElementService.getDataElement( deelementId );
                    // System.out.println("deelementId"+deelementId);

                    DataValue dataValue = dataValueService.getLatestDataValue( ds.getId(), 1, org.getId() );
                    // System.out.println("dataValue"+dataValue);
                    if ( dataValue != null )
                    {

                        // System.out.println( "code1" + dataValue.getValue() );

                        if ( dataValue.getValue() == null )
                        {
                            code = "4";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Introduced into Routine" ) )
                        {
                            code = "1";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Planned" ) )
                        {
                            code = "3";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Partially Introduced" ) )
                        {
                            code = "2";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Introduced partially" ) )
                        {
                            code = "2";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Introduced sequentially" ) )
                        {
                            code = "2";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Partial Intro planned" ) )
                        {
                            code = "3";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Intent to introduce" ) )
                        {
                            code = "3";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Formal commitment" ) )
                        {
                            code = "3";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Risk Groups" ) )
                        {
                            code = "4";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Suspended" ) )
                        {
                            code = "4";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Donation" ) )
                        {
                            code = "4";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Not Introduced" ) )
                        {
                            code = "4";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Not Available" ) )
                        {
                            code = "4";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Not Applicable" ) )
                        {
                            code = "4";

                        }
                        else
                        {
                            code = "4";
                        }
                    }
                    else
                    {
                        DataValue dataValue2 = dataValueService.getDataValue( dataelement.getId(), 1, org.getId(), 1 );
                        // System.out.println( "dataValue2" + dataValue2 );

                        if ( dataValue2 == null )
                        {

                            dataValue2 = new DataValue();
                            dataValue2.setDataElement( dataelement );
                            dataValue2.setPeriod( period );
                            dataValue2.setSource( org );
                            dataValue2.setCategoryOptionCombo( optionCombo );
                            dataValue2.setAttributeOptionCombo( optionCombo );
                            dataValue2.setValue( "4" );

                            dataValue2.setStatus( 1 );

                            dataValueService.addDataValue( dataValue2 );

                        }
                    }

                    DataValue dataValue2 = dataValueService.getDataValue( dataelement.getId(), 1, org.getId(), 1 );
                    // System.out.println( "dataValue2" + dataValue2 );

                    if ( dataValue2 == null )
                    {

                        dataValue2 = new DataValue();
                        dataValue2.setDataElement( dataelement );
                        dataValue2.setPeriod( period );
                        dataValue2.setSource( org );
                        dataValue2.setCategoryOptionCombo( optionCombo );
                        dataValue2.setAttributeOptionCombo( optionCombo );
                        dataValue2.setValue( code );

                        dataValue2.setStatus( 1 );

                        dataValueService.addDataValue( dataValue2 );

                    }

                }

            }
            // System.out.println("count"+dataelementcount);

        }

        for ( DataElement ds : allDataElementList )
        {

            demapping = demappingService.getDeMapping( ds.getUid() );

            if ( demapping != null )
            {
                // System.out.println("dataelement"+demapping);
                String deelementId = demapping.getMappeddeid();
                DataElement dataelement = dataElementService.getDataElement( deelementId );
                System.out.println( "deelementId" + dataelement.getId() );

                DataValue dataValue = dataValueService.getLatestDataValue( dataelement.getId(), 1, greenland_ID );
                DataValue dataValue1 = dataValueService.getLatestDataValue( dataelement.getId(), 1,
                    Svalbard_and_Jan_Mayen_ID );

                // System.out.println("dataValue"+dataValue);
                if ( dataValue != null )
                {
                    dataValueService.deleteDataValue( dataValue );

                }
                if ( dataValue1 != null )
                {
                    dataValueService.deleteDataValue( dataValue1 );

                }
            }

        }

        // / code for denamark ////////////////////////////
        for ( DataElement ds : allDataElementList )
        {
            demapping = demappingService.getDeMapping( ds.getUid() );

            if ( demapping != null )
            {

                String deelementId = demapping.getMappeddeid();
                DataElement dataelement1 = dataElementService.getDataElement( deelementId );

                DataValue dataValue = dataValueService.getLatestDataValue( dataelement1.getId(), 1, Denmark_ID ); // denmark

                System.out.println( "dataValue2" + dataValue );
                DataValue dataValue2 = dataValueService.getLatestDataValue( dataelement1.getId(), 1, greenland_ID ); // greenland

                DataValue dataValue3 = dataValueService.getLatestDataValue( dataelement1.getId(), 1, Norway_ID ); // norway

                DataValue dataValue4 = dataValueService.getLatestDataValue( dataelement1.getId(), 1,
                    Svalbard_and_Jan_Mayen_ID ); // svalbard
                // and
                // Jan
                // Mayen
                if ( dataValue != null )
                {
                    String code1 = dataValue.getValue();

                    if ( dataValue2 == null )
                    {
                        System.out.println( "dataValue2" );
                        OrganisationUnit org = organisationUnitService.getOrganisationUnit( greenland_ID );
                        dataValue2 = new DataValue();
                        dataValue2.setDataElement( dataelement1 );
                        dataValue2.setPeriod( period );
                        dataValue2.setSource( org );
                        dataValue2.setCategoryOptionCombo( optionCombo );
                        dataValue2.setAttributeOptionCombo( optionCombo );
                        dataValue2.setValue( code1 );

                        dataValue2.setStatus( 1 );

                        dataValueService.addDataValue( dataValue2 );

                    }

                }
                if ( dataValue3 != null )
                {
                    String code1 = dataValue3.getValue();

                    if ( dataValue4 == null )
                    {
                        System.out.println( "dataValue2" );

                        OrganisationUnit org = organisationUnitService.getOrganisationUnit( Svalbard_and_Jan_Mayen_ID );
                        dataValue4 = new DataValue();
                        dataValue4.setDataElement( dataelement1 );
                        dataValue4.setPeriod( period );
                        dataValue4.setSource( org );
                        dataValue4.setCategoryOptionCombo( optionCombo );
                        dataValue4.setAttributeOptionCombo( optionCombo );
                        dataValue4.setValue( code1 );

                        dataValue4.setStatus( 1 );

                        dataValueService.addDataValue( dataValue4 );

                    }

                }

            }
        }

        // System.out.println( "importstatus" + allDataElementList );
        /*
         * String query = "SELECT * FROM datavalue " + "WHERE " +
         * "dataelementid= " + "75 " + "AND " + "SOURCEID= " + "170";
         * 
         * System.out.println( "sqlResultSet1" + jdbcTemplate ); SqlRowSet
         * sqlResultSet1 = jdbcTemplate.queryForRowSet( query );
         * 
         * 
         * while ( sqlResultSet1.next() ) { String insertQuery=null; insertQuery
         * =
         * "INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, value, storedby, followup, status, attributeoptioncomboid) VALUES "
         * ;
         * 
         * Integer dataelementid = sqlResultSet1.getInt( 1 ); dataelementid=751;
         * Integer periodid = sqlResultSet1.getInt( 2);
         * System.out.println("periodid"+periodid); Integer sourceid =
         * sqlResultSet1.getInt( 3 ); System.out.println("sourceid"+sourceid);
         * Integer categoryoptioncomboid = sqlResultSet1.getInt( 4 );
         * System.out.println("categoryoptioncomboid"+categoryoptioncomboid);
         * String value = sqlResultSet1.getString( 5 );
         * System.out.println("value"+value); String storedby =
         * sqlResultSet1.getString(6); System.out.println("storedby"+storedby);
         * Date lastupdated = sqlResultSet1.getDate(7);
         * System.out.println("lastupdated"+lastupdated); String comment =
         * sqlResultSet1.getString(8); System.out.println("comment"+comment);
         * 
         * Integer followup = sqlResultSet1.getInt( 9 ); Integer status=
         * sqlResultSet1.getInt( 10 ); Integer attributeoptioncomboid =
         * sqlResultSet1.getInt( 11 );
         * System.out.println("attributeoptioncomboid"+attributeoptioncomboid);
         * String created = sqlResultSet1.getString( 12 ); System.out.println(
         * "value" + value ); int code = 0; if ( value.equals(
         * "Introduced into Routine" ) ) { code = 1; } else if ( value.equals(
         * "Planned" ) ) { code = 2; } else if ( value.equals(
         * "Partially Introduced" ) ) { code = 3; } else if ( value.equals(
         * "Partial Intro planned" ) ) { code = 3; }
         * 
         * // aggregationResultMap.put( orgUnitId + ":" + dataElement.getId(),
         * // scoreValue ); DataValue dataValue=
         * dataValueService.getLatestDataValue(75, 1, 170 );
         * System.out.println("datavalue"+dataValue);
         * 
         * DataElement dataElement = dataElementService.getDataElement(
         * dataelementid );
         * 
         * //demapping = demappingService.getDeMapping( dataElement.getUid() );
         * 
         * // DataElement dataElement1 = dataElementService.getDataElement(
         * demapping.getMappeddeid() );
         * 
         * insertQuery += "("+ dataelementid + ", " + periodid + ", " +
         * sourceid+ ", " + categoryoptioncomboid + ", '" + value + "', '" +
         * storedby + "', " + followup + ", " + status +", " +
         * attributeoptioncomboid + ") "; jdbcTemplate.update( insertQuery );
         * 
         * }
         */
        return SUCCESS;
    }

}
