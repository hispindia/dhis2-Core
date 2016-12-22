package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectCodeComparator;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
//import org.hisp.dhis.dataset.comparator.DataSetSortOrderComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.KeyFlagCalculation;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class GenerateAgendaReportAction
    implements Action
{
    private static final String INTRO_YEAR_DE_GROUP = "INTRO_YEAR_DE_GROUP";

    private static final String VACCINE_ATTRIBUTE = "VACCINE_ATTRIBUTE";

    private static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private SectionService sectionService;

    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
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

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
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

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    
    @Autowired 
    private LookupService lookupService;

    @Autowired
    private IVBUtil ivbUtil;

    private KeyFlagCalculation keyFlagCalculation;
    
    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }
    
    private KeyFlagAnalyticsService keyFlagAnalyticsService;
    
    public void setKeyFlagAnalyticsService( KeyFlagAnalyticsService keyFlagAnalyticsService )
    {
        this.keyFlagAnalyticsService = keyFlagAnalyticsService;
    }
    
    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    private List<Integer> selectedListVaccine;

    private String introStartDate;

    private String introEndDate;

    private Integer orgUnitGroupId;

    private List<String> dataElementGroupUIDs;

    private String orgUnitId;

    public void setSelectedListVaccine( List<Integer> selectedListVaccine )
    {
        this.selectedListVaccine = selectedListVaccine;
    }

    public void setIntroStartDate( String introStartDate )
    {
        this.introStartDate = introStartDate;
    }

    public void setIntroEndDate( String introEndDate )
    {
        this.introEndDate = introEndDate;
    }

    public String getIntroStartDate()
    {
        return introStartDate;
    }

    public String getIntroEndDate()
    {
        return introEndDate;
    }

    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    public void setDataElementGroupUIDs( List<String> dataElementGroupUIDs )
    {
        this.dataElementGroupUIDs = dataElementGroupUIDs;
    }

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private String dataElementType;

    public void setDataElementType( String dataElementType )
    {
        this.dataElementType = dataElementType;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getDataElementType()
    {
        return dataElementType;
    }

    private Map<OrganisationUnit, Map<String, Map<Integer, String>>> orgUnitResultMap;

    private Map<OrganisationUnit, Map<String, Map<Integer, String>>> gaviOrgUnitResultMap;

    public Map<OrganisationUnit, Map<String, Map<Integer, String>>> getGaviOrgUnitResultMap()
    {
        return gaviOrgUnitResultMap;
    }

    private List<Section> dataSetSections;

    private List<OrganisationUnit> selectedOrgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getSelectedOrgUnitList()
    {
        return selectedOrgUnitList;
    }

    private List<OrganisationUnit> gaviApplicationsOrgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getGaviApplicationsOrgUnitList()
    {
        return gaviApplicationsOrgUnitList;
    }

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    private List<DataElementGroup> dataElementGroups;

    private String language;

    private String userName;

    private DataElementGroup introYearDEGroup;

    public DataElementGroup getIntroYearDEGroup()
    {
        return introYearDEGroup;
    }

    private DataElementGroup vacPresentation;

    private DataElementGroup introStatus;

    private DataElementGroup supplyStatus;

    private DataElementGroup coldStatus;

    private DataElementGroup trainingOverview;

    public DataElementGroup getVacPresentation()
    {
        return vacPresentation;
    }

    public DataElementGroup getIntroStatus()
    {
        return introStatus;
    }

    public DataElementGroup getSupplyStatus()
    {
        return supplyStatus;
    }

    public DataElementGroup getColdStatus()
    {
        return coldStatus;
    }

    public DataElementGroup getTrainingOverview()
    {
        return trainingOverview;
    }

    private DataElementGroup gaviAppYearMonthDEGroup;

    private DataElementGroup gaviAppStatus;

    private DataElementGroup gaviIntroSupportStatus;

    public DataElementGroup getGaviIntroSupportStatus()
    {
        return gaviIntroSupportStatus;
    }

    public DataElementGroup getGaviAppYearMonthDEGroup()
    {
        return gaviAppYearMonthDEGroup;
    }

    public DataElementGroup getGaviAppStatus()
    {
        return gaviAppStatus;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getUserName()
    {
        return userName;
    }

    public List<DataElementGroup> getDataElementGroups()
    {
        return dataElementGroups;
    }

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    public Map<OrganisationUnit, Map<String, Map<Integer, String>>> getOrgUnitResultMap()
    {
        return orgUnitResultMap;
    }

    public List<Section> getDataSetSections()
    {
        return dataSetSections;
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

    // Regional Report
    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }

    private Map<String, List<DataSet>> datasetMap = new HashMap<String, List<DataSet>>();

    public Map<String, List<DataSet>> getDatasetMap()
    {
        return datasetMap;
    }

    private Map<String, String> keyIndicatorValueMap = new HashMap<String, String>();

    public Map<String, String> getKeyIndicatorValueMap()
    {
        return keyIndicatorValueMap;
    }

    private Map<String, String> thresholdMap = new HashMap<String, String>();

    public Map<String, String> getThresholdMap()
    {
        return thresholdMap;
    }

    private Map<String, String> commentMap = new HashMap<String, String>();

    public Map<String, String> getCommentMap()
    {
        return commentMap;
    }

    private Map<String, String> sourceMap = new HashMap<String, String>();

    public Map<String, String> getSourceMap()
    {
        return sourceMap;
    }

    private Map<String, String> userInfoMap = new HashMap<String, String>();

    public Map<String, String> getUserInfoMap()
    {
        return userInfoMap;
    }
    
    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }
    
    private String lastUpdated = "";

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    private Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    private List<Integer> orgUnitIds = new ArrayList<Integer>();

    public void setOrgUnitIds( List<Integer> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
    }

    private String regionNames = "";

    public String getRegionNames()
    {
        return regionNames;
    }

    private String generatedDate;

    public String getGeneratedDate()
    {
        return generatedDate;
    }

    public Map<String, String> colorMap = new HashMap<String, String>();
    
    public Map<String, String> getColorMap()
    {
        return colorMap;
    }
    
    private OrganisationUnitGroupSet unicefRegionsGroupSet;
    
    public OrganisationUnitGroupSet getUnicefRegionsGroupSet()
    {
        return unicefRegionsGroupSet;
    }

    private Map<String, DataValue> headerDataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getHeaderDataValueMap()
    {
        return headerDataValueMap;
    }

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------

    public String execute()
    {
        
        System.out.println(" Agenda Report Started at : " + new Date() );
        
        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );
        
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        dataElementGroups = new ArrayList<DataElementGroup>( dataElementService.getDataElementGroupSet( 1 ).getMembers() );

        Constant introYearGroupConstant = constantService.getConstantByName( INTRO_YEAR_DE_GROUP );
        introYearDEGroup = dataElementService.getDataElementGroup( (int) introYearGroupConstant.getValue() );
        List<DataElement> introYearDEs = new ArrayList<DataElement>( introYearDEGroup.getMembers() );

        /*
         * Need to change group ids to constants
         */
        gaviAppYearMonthDEGroup = dataElementService.getDataElementGroup( 38 );
        gaviAppStatus = dataElementService.getDataElementGroup( 37 );
        gaviIntroSupportStatus = dataElementService.getDataElementGroup( 39 );

        Set<DataElementGroup> gaviDataElementGroups = new HashSet<DataElementGroup>();
        gaviDataElementGroups.add( gaviAppStatus );
        gaviDataElementGroups.add( gaviIntroSupportStatus );

        dataElementGroups.removeAll( gaviDataElementGroups );

        List<DataElement> gaviAppYearDEs = new ArrayList<DataElement>( gaviAppYearMonthDEGroup.getMembers() );        

        vacPresentation = dataElementService.getDataElementGroup( 41 );
        introStatus = dataElementService.getDataElementGroup( 40 );
        supplyStatus = dataElementService.getDataElementGroup( 44 );
        coldStatus = dataElementService.getDataElementGroup( 22 );
        trainingOverview = dataElementService.getDataElementGroup( 46 );

        Constant vaccineAttributeConstant = constantService.getConstantByName( VACCINE_ATTRIBUTE );

        dataSetSections = new ArrayList<Section>();

        List<String> sectionNames = new ArrayList<String>();

        DataSet dataSet = dataSetService.getDataSet( 4 );
        dataSetSections.addAll( dataSet.getSections() );
        
        dataSetSections.addAll(  dataSetService.getDataSet( 10 ).getSections() );
        
        for ( Section section : dataSetSections )
        {
            sectionNames.add( section.getName().trim() );
        }

        Set<DataElement> allDes = new HashSet<DataElement>();
        allDes.addAll( gaviAppYearMonthDEGroup.getMembers() );
        allDes.addAll( gaviAppStatus.getMembers() );
        allDes.addAll( gaviIntroSupportStatus.getMembers() );
        allDes.addAll( vacPresentation.getMembers() );
        allDes.addAll( introStatus.getMembers() );
        allDes.addAll( supplyStatus.getMembers() );
        allDes.addAll( coldStatus.getMembers() );
        allDes.addAll( trainingOverview.getMembers() );
        allDes.addAll( introYearDEGroup.getMembers() );
        for( DataElementGroup deg : dataElementGroups )
        {
            allDes.addAll( deg.getMembers() );
        }
        
        // Getting start and end period from introStartDate && introEndDate

        Date sDate = getStartDateByString( introStartDate );
        Date eDate = getEndDateByString( introEndDate );

        if ( orgUnitIds.size() > 1 )
        {
            for ( Integer id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );

                if ( orgUnit.getHierarchyLevel()  == 3 )
                {
                    orgUnitList.add( orgUnit );
                    gaviApplicationsOrgUnitList.add( orgUnit );
                    selectedOrgUnitList.add( orgUnit );
                }
            }
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            selectedOrgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );

            orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
            gaviApplicationsOrgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getOrganisationUnits() );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if ( orgUnit.getHierarchyLevel() == 3 )
                {
                    lastLevelOrgUnit.add( orgUnit );
                }
                else
                {
                    lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
                }
            }

            selectedOrgUnitList.retainAll( lastLevelOrgUnit );

            orgUnitList.retainAll( lastLevelOrgUnit );
            gaviApplicationsOrgUnitList.retainAll( lastLevelOrgUnit );
        }
        List<OrganisationUnit> regionList = new ArrayList<OrganisationUnit>();
        for ( OrganisationUnit orgUnit : selectedOrgUnitList )
        {
            if ( !regionList.contains( orgUnit.getParent() ) )
            {
                regionList.add( orgUnit.getParent() );
            }
        }
        if ( regionList.size() > 0 )
        {
            for ( OrganisationUnit region : regionList )
            {
                if ( regionNames == "" )
                {
                    regionNames = region.getName();
                }
                else
                {
                    regionNames = regionNames + "," + region.getName();
                }
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date currentDate = new Date();
        generatedDate = dateFormat.format( currentDate );
        Collections.sort( gaviApplicationsOrgUnitList, new IdentifiableObjectNameComparator() );
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        Collections.sort( selectedOrgUnitList, new IdentifiableObjectNameComparator() );
        
        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        String orgUnitIdsByComma1 = "-1";
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma1 = getCommaDelimitedString( organisationUnitIds );
        }
        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( allDes ) );
        String dataElementIdsByComma = "-1";
        if ( dataElementIds.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( dataElementIds );
        }
        
        Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma1 );

        // Filter2: Getting orgunit list whose intro year is between the
        // selected start year and end year
        orgUnitResultMap = new HashMap<OrganisationUnit, Map<String, Map<Integer, String>>>();
        Iterator<OrganisationUnit> orgUnitIterator = orgUnitList.iterator();
        while ( orgUnitIterator.hasNext() )
        {
            OrganisationUnit orgUnit = orgUnitIterator.next();
            

            Map<String, Map<Integer, String>> sectionResultMap = orgUnitResultMap.get( orgUnit );
            if ( sectionResultMap == null || sectionResultMap.size() <= 0 )
            {
                sectionResultMap = new HashMap<String, Map<Integer, String>>();
            }

            int flag = 0;
            for ( DataElement dataElement : introYearDEs )
            {
                Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
                if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
                {
                    for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                    {
                        if ( deAttributeValue.getAttribute().getId() == vaccineAttributeConstant.getValue()
                            && deAttributeValue.getValue() != null
                            && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                        {
                            //DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                            DataValue dv = dataValueMap.get( orgUnit.getId()+":"+dataElement.getId() );
                            if ( dv != null && dv.getValue() != null )
                            {
                                String value = dv.getValue();
                                String comment = dv.getComment();
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue()
                                    .trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                Date valueDate = getStartDateByString( value );
                                if ( valueDate != null && sDate.getTime() <= valueDate.getTime()
                                    && valueDate.getTime() <= eDate.getTime() )
                                {
                                    valueResultMap.put( introYearDEGroup.getId(), value + ":" + comment );
                                    sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                    orgUnitResultMap.put( orgUnit, sectionResultMap );
                                    if ( valueDate.equals( sDate ) || valueDate.equals( eDate )
                                        || (valueDate.after( sDate ) && valueDate.before( eDate )) )
                                    {
                                        flag = 1;
                                        // break;
                                    }
                                }
                            }
                            else
                            {
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue()
                                    .trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                valueResultMap.put( introYearDEGroup.getId(), " " );
                                sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                orgUnitResultMap.put( orgUnit, sectionResultMap );
                            }

                        }
                    }
                }
            }

            
            if ( flag != 0 )
            {
                for ( DataElementGroup dataElementGroup : dataElementGroups )
                {
                    List<DataElement> dataElements = new ArrayList<DataElement>( dataElementGroup.getMembers() );
                    for ( DataElement dataElement : dataElements )
                    {
                        Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
                        if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
                        {
                            for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                            {
                                if ( deAttributeValue.getAttribute().getId() == vaccineAttributeConstant.getValue()
                                    && deAttributeValue.getValue() != null
                                    && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                                {
                                    //DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                                    DataValue dv = dataValueMap.get( orgUnit.getId()+":"+dataElement.getId() );

                                    if ( dv != null && dv.getValue() != null )
                                    {
                                        String value = dv.getValue();
                                        String comment = dv.getComment();
                                        Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue
                                            .getValue().trim() );
                                        if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                        {
                                            valueResultMap = new HashMap<Integer, String>();
                                        }
                                        valueResultMap.put( dataElementGroup.getId(), value + ":" + comment );
                                        sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                        orgUnitResultMap.put( orgUnit, sectionResultMap );
                                    }
                                    else
                                    {
                                        Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue
                                            .getValue().trim() );
                                        if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                        {
                                            valueResultMap = new HashMap<Integer, String>();
                                        }
                                        valueResultMap.put( dataElementGroup.getId(), " " );
                                        sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                        orgUnitResultMap.put( orgUnit, sectionResultMap );
                                    }

                                }
                            }
                        }
                    }
                }
            }
            else
            {
                orgUnitIterator.remove();
                //System.out.print(  orgUnit.getName() + " is removed as intro year not fall in" );
            }
        }

        // Filter3: Getting orgunit list whose intro year is between the
        // selected start year and end year
        Set<OrganisationUnit> orgUnitsOutOfgaviAppYear = new HashSet<OrganisationUnit>();
        gaviOrgUnitResultMap = new HashMap<OrganisationUnit, Map<String, Map<Integer, String>>>();
        orgUnitIterator = gaviApplicationsOrgUnitList.iterator();
        while ( orgUnitIterator.hasNext() )
        {
            OrganisationUnit orgUnit = orgUnitIterator.next();
            //System.out.println( "**********  " + orgUnit.getName() );
            Map<String, Map<Integer, String>> sectionResultMap = gaviOrgUnitResultMap.get( orgUnit );
            if ( sectionResultMap == null || sectionResultMap.size() <= 0 )
            {
                sectionResultMap = new HashMap<String, Map<Integer, String>>();
            }

            int flag = 0;
            for ( DataElement dataElement : gaviAppYearDEs )
            {
                //System.out.print( dataElement.getName() + " ");
                Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
                if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
                {
                    for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                    {
                        //System.out.println( deAttributeValue.getValue() + " " );
                        if ( deAttributeValue.getAttribute().getId() == vaccineAttributeConstant.getValue()
                            && deAttributeValue.getValue() != null
                            && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                        {
                            //System.out.println( "Inside if " );
                            //DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                            DataValue dv = dataValueMap.get( orgUnit.getId()+":"+dataElement.getId() );
                            if ( dv != null && dv.getValue() != null )
                            {
                                String value = dv.getValue();
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                Date valueDate = getStartDateByString( value );

                                if ( valueDate != null && sDate.getTime() <= valueDate.getTime()
                                    && valueDate.getTime() <= eDate.getTime() )
                                {
                                    valueResultMap.put( gaviAppYearMonthDEGroup.getId(), value );
                                    sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                    gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );
                                    if ( valueDate.equals( sDate ) || valueDate.equals( eDate )
                                        || (valueDate.after( sDate ) && valueDate.before( eDate )) )
                                    {
                                        flag = 1;
                                        //System.out.print("Flag is 1 for : ");
                                    }                                    
                                }
                                //System.out.println( orgUnit.getName() + " : " + dataElement.getName() + " : " + valueDate + " : " + sDate + " : " + eDate );
                            }
                            else
                            {
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue()
                                    .trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                valueResultMap.put( gaviAppYearMonthDEGroup.getId(), " " );
                                sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );
                            }

                        }
                    }
                }
            }
            if ( flag != 0 )
            {
                for ( DataElementGroup dataElementGroup : gaviDataElementGroups )
                {
                    List<DataElement> dataElements = new ArrayList<DataElement>( dataElementGroup.getMembers() );
                    for ( DataElement dataElement : dataElements )
                    {
                        Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
                        if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
                        {
                            for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                            {
                                if ( deAttributeValue.getAttribute().getId() == vaccineAttributeConstant.getValue()
                                    && deAttributeValue.getValue() != null
                                    && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                                {
                                    //DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                                    DataValue dv = dataValueMap.get( orgUnit.getId()+":"+dataElement.getId() );

                                    if ( dv != null && dv.getValue() != null )
                                    {
                                        String value = dv.getValue();
                                        String comment = dv.getComment();
                                        Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue
                                            .getValue().trim() );
                                        if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                        {
                                            valueResultMap = new HashMap<Integer, String>();
                                        }
                                        valueResultMap.put( dataElementGroup.getId(), value + ":" + comment );
                                        sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                        gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );
                                    }
                                    else
                                    {
                                        Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue
                                            .getValue().trim() );
                                        if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                        {
                                            valueResultMap = new HashMap<Integer, String>();
                                        }
                                        valueResultMap.put( dataElementGroup.getId(), " " );
                                        sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                        gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );
                                    }

                                }
                            }
                        }
                    }
                }
            }
            else
            {
                orgUnitIterator.remove();
                //System.out.println(  orgUnit.getName() + " is removed as intro year not fall in" );
            }
        }

        for ( OrganisationUnit orgUnit : gaviApplicationsOrgUnitList )
        {
            Map<String, Map<Integer, String>> sectionResultMap = gaviOrgUnitResultMap.get( orgUnit );
            for ( Section dataSetSection : dataSetSections )
            {
                Map<Integer, String> valueResultMap = sectionResultMap.get( dataSetSection.getName().trim() );
                if ( valueResultMap != null )
                {
                }
                else
                {
                    valueResultMap = new HashMap<Integer, String>();
                    valueResultMap.put( gaviAppYearMonthDEGroup.getId(), " " );
                }
                sectionResultMap.put( dataSetSection.getName().trim(), valueResultMap );
            }

            gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );

        }

        //System.out.println("KeyFlag calculation starting...." + new Date() );
        
        List<DataSet> datasetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );
        
        Iterator<DataSet> dataSetIterator = datasetList.iterator();
        List<DataSet> datasets = new ArrayList<DataSet>();
        while( dataSetIterator.hasNext() )
        {
            DataSet ds = dataSetIterator.next();
            if( ds.getSources() != null && ds.getSources().size() > 0 )
            {
                datasets.add( ds );
            }
        }
        datasetList.retainAll( datasets );
     //   Collections.sort( datasetList, new IdentifiableObjectNameComparator() );
        
        //Collections.sort( datasetList, new DataSetSortOrderComparator() );
        Date tempDate = null;
        
        Map<String, KeyFlagAnalytics> keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>();
        keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>( keyFlagAnalyticsService.getKeyFlagAnalyticsMap() );
        
        for ( DataSet dataSet1 : datasetList )
        {
            for ( Indicator indicator : dataSet1.getIndicators() )
            { 
                if( !indicatorList.contains( indicator ) )
                {
                    indicatorList.add( indicator );
                    datasetMap.put( indicator.getUid(),new ArrayList<DataSet>(indicator.getDataSets()));
                }
                
                for ( OrganisationUnit orgUnit : selectedOrgUnitList )
                {
                    KeyFlagAnalytics kfa = keyFlagAnalyticMap.get( orgUnit.getId() + ":" + indicator.getId() );
                    
                    String mapKey =  indicator.getUid() + "-" + orgUnit.getUid();
                    
                    /*
                    lastUpdated = keyFlagCalculation.getLastUpdated();
                    String keyIndicatorValue = "";
                    String exString = indicator.getNumerator();
                    if ( exString.contains( KeyFlagCalculation.NESTED_OPERATOR_AND ) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
                    {
                        keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(),
                            indicator.getUid(), orgUnit );
                    }
                    else
                    {
                        keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(),
                            indicator.getUid(), orgUnit );
                    }
                    if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("Yes") )
                    {
                                            keyIndicatorValue = "No";
                    }
                    else if( keyFlagCalculation.getIsthresoldrev() == true && keyIndicatorValue != null && keyIndicatorValue.equalsIgnoreCase("No") )
                    {       
                            keyIndicatorValue = "Yes";
                    }

                    try 
                    {
                        Integer factor = indicator.getIndicatorType().getFactor();
                        double keyIndicatorVal = Double.parseDouble( keyIndicatorValue );
                        keyIndicatorVal = keyIndicatorVal * factor;
                        keyIndicatorVal = Math.round(keyIndicatorVal * 100);
                        keyIndicatorVal = keyIndicatorVal/100;
                        keyIndicatorValue = ""+keyIndicatorVal;
                    } 
                    catch (Exception e) 
                    {}
                    //valueMap = keyFlagCalculation.getValueMap();
                    commentMap = keyFlagCalculation.getCommentMap();
                    sourceMap = keyFlagCalculation.getSourceMap();
                    colorMap = keyFlagCalculation.getColorMap();
                    keyIndicatorValueMap.put( indicator.getUid() + "-" + orgUnit.getUid(), keyIndicatorValue );
                   // thresholdMap.put( indicator.getUid() + "-" + orgUnit.getUid(), thresoldValue );
                    
                   */
                    
                    
                    /*
                    if( kfa.getKeyFlagValue() != null )
                    {
                        valueMap.put( mapKey, kfa.getKeyFlagValue() );
                    }
                    */
                    if( kfa != null )
                    {
                        if( kfa.getComment() != null )
                        {
                            commentMap.put( mapKey, kfa.getComment() );
                        }
                        
                        if( kfa.getSource() != null )
                        {
                            sourceMap.put( mapKey, kfa.getSource() );
                        }
                        
                        /*
                        if( kfa.getUser() != null )
                        {
                            userInfoMap.put( mapKey, kfa.getUser() );
                        }
                       
                        
                        if( kfa.getPeriod() != null )
                        {
                            periodMap.put( mapKey, kfa.getPeriod() );
                        }
                         */
                        
                        if( kfa.getColor() != null )
                        {
                            colorMap.put( mapKey, kfa.getColor() );
                        }
                        
                        if( kfa.getDeValue() != null )
                        {
                            keyIndicatorValueMap.put( mapKey, kfa.getDeValue() );
                        }
                        
                        if( kfa.getLastUpdated() != null )
                        {
                            lastUpdated = kfa.getLastUpdated();
                        } 
                        
                    }
                    
                    
                }
            }
        }
        
        //indicatorList = new ArrayList<Indicator>( keyFlagCalculation.getIndicatorList() )  ;
        //datasetMap = keyFlagCalculation.getDatasetMap();  
        
        if ( regionList.size() == 1 )
        {
            Map<String, Indicator> keyFlagIndicatorCodeMap = new HashMap<String, Indicator>();
            for( Indicator indicator : indicatorList )
            {
                keyFlagIndicatorCodeMap.put(  indicator.getCode(), indicator );
            }
            Set<String> indicatorCodes = new HashSet<String>();
            indicatorCodes.addAll( keyFlagIndicatorCodeMap.keySet() );
            
            lookup = lookupService.getLookupByName( "KEYFLAG_ORDER_ATTRIBUTE" );
            Integer keyFlagCodeAttributeId = Integer.parseInt(  lookup.getValue() );
            String keyFlagCodes = "";
            
            Set<AttributeValue> orgUnitAttributeValues = regionList.get( 0 ).getAttributeValues();
            if ( orgUnitAttributeValues != null && orgUnitAttributeValues.size() > 0 )
            {
                for ( AttributeValue orgUnitAttributeValue : orgUnitAttributeValues )
                {
                    if ( orgUnitAttributeValue.getAttribute().getId() == keyFlagCodeAttributeId && orgUnitAttributeValue.getValue() != null )
                    {
                        keyFlagCodes = orgUnitAttributeValue.getValue();
                        break;                            
                    }
                }
            }
            
            List<Indicator> tempIndicatorList1 = new ArrayList<Indicator>();            
            for( String keyFlag : keyFlagCodes.split( ";" ) )
            {                
                Indicator ind = keyFlagIndicatorCodeMap.get( keyFlag );
                if( ind != null )
                {
                    tempIndicatorList1.add( ind );
                }
            }
            
            List<Indicator> tempIndicatorList2 = new ArrayList<Indicator>();
            tempIndicatorList2.addAll( indicatorList );
            
            tempIndicatorList2.removeAll( tempIndicatorList1 );
            
            indicatorList.clear();            
            indicatorList.addAll( tempIndicatorList1 );            
            indicatorList.addAll( tempIndicatorList2 );
            
            if( keyFlagCodes.equals( "" ) )
            {
                Collections.sort( indicatorList, new IdentifiableObjectCodeComparator() );
            }
        }
        else
        {
            Collections.sort( indicatorList, new IdentifiableObjectCodeComparator() );
        }
        
        Constant tabularDataElementGroupId = constantService.getConstantByName( TABULAR_REPORT_DATAELEMENTGROUP_ID );
        
        List<DataElement> dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );

        List<Integer >headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";

        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
        }

        List<Integer> orgunitIds = new ArrayList<Integer>( getIdentifiers( selectedOrgUnitList ) );
        String orgUnitIdsByComma = "-1";
        if ( orgunitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgunitIds );
        }

        headerDataValueMap = ivbUtil.getLatestDataValuesForTabularReport( headerDataElementIdsByComma, orgUnitIdsByComma );

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
        
        
        System.out.println(" Agenda Report End at : " + new Date() );
        
        
        return SUCCESS;

    }

    /**
     * Get Start Date from String date foramt (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    private Date getStartDateByString( String dateStr )
    {
        String startDate = "";
        String[] startDateParts = dateStr.split( "-" );
        if ( startDateParts.length <= 1 )
        {
            startDate = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            startDate = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            startDate = startDateParts[0] + "-04-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            startDate = startDateParts[0] + "-07-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            startDate = startDateParts[0] + "-10-01";
        }
        else
        {
            startDate = startDateParts[0] + "-" + startDateParts[1] + "-01";
        }

        Date sDate = format.parseDate( startDate );

        return sDate;
    }

    /**
     * Get End Date from String date foramt (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    private Date getEndDateByString( String dateStr )
    {
        String endDate = "";
        int monthDays[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        String[] endDateParts = dateStr.split( "-" );
        if ( endDateParts.length <= 1 )
        {
            endDate = endDateParts[0] + "-12-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            endDate = endDateParts[0] + "-03-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            endDate = endDateParts[0] + "-06-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            endDate = endDateParts[0] + "-09-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            endDate = endDateParts[0] + "-12-31";
        }
        else
        {
            if ( Integer.parseInt( endDateParts[0] ) % 400 == 0 )
            {
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )] + 1);
            }
            else
            {
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )]);
            }
        }

        Date eDate = format.parseDate( endDate );

        return eDate;
    }
    
}
