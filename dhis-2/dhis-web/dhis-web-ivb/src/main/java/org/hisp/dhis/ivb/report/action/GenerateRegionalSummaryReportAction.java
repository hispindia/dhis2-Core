package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
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
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */

public class GenerateRegionalSummaryReportAction
    implements Action
{

    private final static String KEY_FLAG = "Key Flag";
    
    private static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    public void setIndicatorService( IndicatorService indicatorService )
    {
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

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }
    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    private KeyFlagCalculation keyFlagCalculation;
    
    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }
    
    @Autowired 
    private LookupService lookupService;

    @Autowired
    private ConstantService constantService;
    
    @Autowired
    private DataElementService dataElementService;
    
    private KeyFlagAnalyticsService keyFlagAnalyticsService;
    
    public void setKeyFlagAnalyticsService( KeyFlagAnalyticsService keyFlagAnalyticsService )
    {
        this.keyFlagAnalyticsService = keyFlagAnalyticsService;
    }
    
    
    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    private String startDate;

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    private String endDate;

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    private Integer regionId;

    public void setRegionId( Integer regionId )
    {
        this.regionId = regionId;
    }

    private List<Integer> selectedListDataset;

    public void setSelectedListDataset( List<Integer> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    private Integer orgUnitGroupId;

    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    private String userSource;

    public void setUserSource( String userSource )
    {
        this.userSource = userSource;
    }

    private String regionalDashboard;

    public void setRegionalDashboard( String regionalDashboard )
    {
        this.regionalDashboard = regionalDashboard;
    }

    private String dataElementType;

    public void setDataElementType( String dataElementType )
    {
        this.dataElementType = dataElementType;
    }
    
    private String currentQuarter;
    
    public void setCurrentQuarter( String currentQuarter )
    {
        this.currentQuarter = currentQuarter;
    }
    
    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getDataElementType()
    {
        return dataElementType;
    }

    public String getRegionalDashboard()
    {
        return regionalDashboard;
    }

    public String getUserSource()
    {
        return userSource;
    }

    private String language;

    public String getLanguage()
    {
        return language;
    }

    public String getStartDate()
    {
        return startDate;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    public String getEndDate()
    {
        return endDate;
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

    private Map<String, String> lastUpdatedMap = new HashMap<String, String>();

    public Map<String, String> getLastUpdatedMap()
    {
        return lastUpdatedMap;
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

    private List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnits()
    {
        return orgUnits;
    }

    private Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    private Map<String, List<DataSet>> datasetMap = new HashMap<String, List<DataSet>>();

    public Map<String, List<DataSet>> getDatasetMap()
    {
        return datasetMap;
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

    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }

    private String vaccinesByComma = "";

    public String getVaccinesByComma()
    {
        return vaccinesByComma;
    }

    private String introductionDates;

    public String getIntroductionDates()
    {
        return introductionDates;
    }

    private String flagFolderPath;

    public String getFlagFolderPath()
    {
        return flagFolderPath;
    }

    private Set<OrganisationUnit> keyFlagCountries = new HashSet<OrganisationUnit>();

    public Set<OrganisationUnit> getKeyFlagCountries()
    {
        return keyFlagCountries;
    }

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMap()
    {
        return dataValueAuditMap;
    }

    private List<DataElement> dataElementList = new ArrayList<DataElement>();

    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    private SimpleDateFormat simpleDateFormat;
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }

    private SimpleDateFormat simpleDateFormat1;
    
    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }
    private SimpleDateFormat simpleDateFormat2;
    
    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }
    
    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }
    
    private List<DataSet> datasetList = new ArrayList<DataSet>();
    
    public List<DataSet> getDatasetList()
    {
        return datasetList;
    }

    private Map<DataSet, Indicator> dataSet_Indicator_Map = new HashMap<DataSet, Indicator>();
    
    public Map<DataSet, Indicator> getDataSet_Indicator_Map()
    {
        return dataSet_Indicator_Map;
    }
    
    private Map<String, List<Period>> blankDataElementValueMap = new HashMap<String, List<Period>>();
    
    public Map<String, List<Period>> getBlankDataElementValueMap()
    {
        return blankDataElementValueMap;
    }
    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
    {
        this.orgUnitIds = orgUnitIds;
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
        Period curPeriod = null;
        
        if( currentQuarter != null )
        {
            curPeriod = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );
            curPeriod = periodService.reloadPeriod( curPeriod );
        }

        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        simpleDateFormat1 = new SimpleDateFormat( "MMM" );
        simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );       
        
        User curUser = currentUserService.getCurrentUser();

        userName = curUser.getUsername();
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

        flagFolderPath = System.getenv( "DHIS2_HOME" ) + File.separator + "flags" + File.separator;

       /* OrganisationUnit regionOrgUnit = organisationUnitService.getOrganisationUnit( regionId );

        organisationUnit = regionOrgUnit;

        orgUnits.addAll( regionOrgUnit.getChildren() );

        OrganisationUnitGroup orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroupId );
        if ( orgUnitGroup != null )
        {
            orgUnits.retainAll( orgUnitGroup.getMembers() );
        }
        */
        if(orgUnitIds.size() > 0)
        {
            for(Integer id : orgUnitIds)
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );
                orgUnits.add( orgUnit );
            }
        }
        else if(selectionTreeManager.getReloadedSelectedOrganisationUnits() != null)
        { 
                orgUnits =  new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() ) ;            
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if ( orgUnit.getHierarchyLevel() == 3  )
                {
                    lastLevelOrgUnit.add( orgUnit );
                }
                else
                {
                    lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
                }
            }
            orgUnits.retainAll( lastLevelOrgUnit );
        }        
        Collections.sort(orgUnits, new IdentifiableObjectNameComparator() );

        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        Set<Indicator> indicaList = new HashSet<Indicator>();
        
        dataElementList = new ArrayList<DataElement>();
        for ( Integer dataSetId : selectedListDataset )
        {
            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            datasetList.add( dataSet );
            indicaList.addAll( dataSet.getIndicators() );
            dataElementList.addAll( dataSet.getDataElements() );
            
            for( Indicator indicator : dataSet.getIndicators() )
            {
                List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
                for ( AttributeValue attributeValue : attributeValueList )
                {
                    if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_FLAG ) )
                    {
                        dataSet_Indicator_Map.put( dataSet, indicator );
                    }
                }
            }
        }
        
        
        Map<String, KeyFlagAnalytics> keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>();
        keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>( keyFlagAnalyticsService.getKeyFlagAnalyticsMap() );
        
        for ( Indicator indicator : indicaList )
        {
            for ( OrganisationUnit orgUnit : orgUnits )
            {               
                KeyFlagAnalytics kfa = keyFlagAnalyticMap.get( orgUnit.getId() + ":" + indicator.getId() );
                
                String mapKey =  indicator.getUid() + "-" + orgUnit.getUid();
                
                /*
                String keyIndicatorValue = "";
                String exString = indicator.getNumerator(); 
                
                if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ))
                { 
                    keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                        .getUid(), orgUnit ); 
                }
                else
                {
                    keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator
                        .getUid(), orgUnit); 
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
                
                
                valueMap = keyFlagCalculation.getValueMap();
                commentMap = keyFlagCalculation.getCommentMap();
                sourceMap = keyFlagCalculation.getSourceMap();
                periodMap = keyFlagCalculation.getPeriodMap();
                keyIndicatorValueMap.put( indicator.getUid() + "-" + orgUnit.getUid(), keyIndicatorValue );
                colorMap = keyFlagCalculation.getColorMap();
               
                lastUpdatedMap.put( indicator.getUid() + "-" + orgUnit.getUid(), keyFlagCalculation.getLastUpdated() );
                */
                
                if( kfa != null )
                {
                    
                    if( kfa.getKeyFlagValue() != null )
                    {
                        valueMap.put( mapKey, kfa.getKeyFlagValue() );
                    }
                    
                    if( kfa.getComment() != null )
                    {
                        commentMap.put( mapKey, kfa.getComment() );
                    }
                    
                    if( kfa.getSource() != null )
                    {
                        sourceMap.put( mapKey, kfa.getSource() );
                    }
                    
                    if( kfa.getPeriod() != null )
                    {
                        periodMap.put( mapKey, kfa.getPeriod() );
                    }
                    
                    if( kfa.getDeValue() != null )
                    {
                        keyIndicatorValueMap.put( mapKey, kfa.getDeValue() );
                    }
                    
                    if( kfa.getColor() != null )
                    {
                        colorMap.put( mapKey, kfa.getColor() );
                    }
                    
                    if( kfa.getLastUpdated() != null )
                    {
                        lastUpdatedMap.put( mapKey, kfa.getLastUpdated() );
                    } 
                    
                }
                
            }
        }
                
        keyFlagCountries = keyFlagCalculation.getKeyFlagCountries();
        indicatorList = keyFlagCalculation.getIndicatorList();
        datasetMap = keyFlagCalculation.getDatasetMap();
        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElementList ) );
        String dataElementIdsByComma = getCommaDelimitedString( dataElementIds );
        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnits ) );

        String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
        if ( orgUnitIdsByComma == null || orgUnitIdsByComma == "" || orgUnitIdsByComma.isEmpty() )
        {
            orgUnitIdsByComma = "-1";
        }
        
        if(startDate != null && endDate != null)
        {
            if ( dataElementType.equalsIgnoreCase( "SHOW_ALL_DES" ) )
            {
                dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>( ivbUtil.getDataValueAuditMap( dataElementIdsByComma, orgUnitIdsByComma, startDate, endDate, DataValueAudit.DVA_CT_HISOTRY, null ) );
            }
            else if ( dataElementType.equalsIgnoreCase( "SHOW_DATA_DES" ) )
            {
                dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>( ivbUtil.getDataValueAuditWithValueMap( dataElementIdsByComma, orgUnitIdsByComma, startDate, endDate, DataValueAudit.DVA_CT_HISOTRY, null ) );
            }
            else if ( dataElementType.equalsIgnoreCase( "SHOW_BLANK_DES" ) )
            {
                blankDataElementValueMap = new HashMap<String, List<Period>>( ivbUtil.getOrgUnit_DataElement_WithoutValueMap( dataElementIdsByComma, orgUnitIdsByComma, startDate, endDate, DataValueAudit.DVA_CT_HISOTRY, null ) );
            }
            else
            {
                
            }
        }
    
        Constant tabularDataElementGroupId = constantService.getConstantByName( TABULAR_REPORT_DATAELEMENTGROUP_ID );
        
        List<DataElement> dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );

        List<Integer >headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";

        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
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
        Collections.sort( orgUnits );

        return SUCCESS;
    }    
   
}
