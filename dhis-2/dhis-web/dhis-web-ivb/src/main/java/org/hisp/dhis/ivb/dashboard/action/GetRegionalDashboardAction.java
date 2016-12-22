package org.hisp.dhis.ivb.dashboard.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.comparator.IdentifiableObjectCodeComparator;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
//import org.hisp.dhis.dataset.comparator.DataSetSortOrderComparator;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalyticsService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.KeyFlagCalculation;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta Bajpai
 */
public class GetRegionalDashboardAction
    implements Action
{
    private static final String SHOW_ALL_COUNTRIES_ORGUNIT_GROUP = "SHOW_ALL_COUNTRIES_ORGUNIT_GROUP";

    private final static int REGION_LEVEL = 2;

    private final static int ORGUNIT_GROUP_SET = 3;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
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

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
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

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
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
    
    private KeyFlagAnalyticsService keyFlagAnalyticsService;
    
    public void setKeyFlagAnalyticsService( KeyFlagAnalyticsService keyFlagAnalyticsService )
    {
        this.keyFlagAnalyticsService = keyFlagAnalyticsService;
    }
    
    @Autowired
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    private String orgUnitGrpId = "";

    public String getOrgUnitGrpId()
    {
        return orgUnitGrpId;
    }

    public void setOrgUnitGrpId( String orgUnitGrpId )
    {
        this.orgUnitGrpId = orgUnitGrpId;
    }

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
    // Setters
    // -------------------------------------------------------------------------

    private String orgUnitId;

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    public String getOrgUnitId()
    {
        return orgUnitId;
    }

    private String regionName;

    public void setRegionName( String regionName )
    {
        this.regionName = regionName;
    }

    private String lastUpdated;

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    private String orgUnitStatus;

    public void setOrgUnitStatus( String orgUnitStatus )
    {
        this.orgUnitStatus = orgUnitStatus;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

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

    private List<OrganisationUnitGroup> orgUnitGrpList = new ArrayList<OrganisationUnitGroup>();

    public List<OrganisationUnitGroup> getOrgUnitGrpList()
    {
        return orgUnitGrpList;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private List<String> countryList = new ArrayList<String>();

    public List<String> getCountryList()
    {
        return countryList;
    }

    private List<String> regionList = new ArrayList<String>();

    public List<String> getRegionList()
    {
        return regionList;
    }
    public Map<String, String> colorMap = new HashMap<String, String>();
    
    public Map<String, String> getColorMap()
    {
        return colorMap;
    }
    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------

    public String execute()
    {
        Constant show_all = constantService.getConstantByName( SHOW_ALL_COUNTRIES_ORGUNIT_GROUP );
        lastUpdated = "";
        User curUser = currentUserService.getCurrentUser();

        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        userName = curUser.getUsername();
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        if ( orgUnitStatus != null && orgUnitStatus.equalsIgnoreCase( "true" ) )
        {
            orgUnits = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser()
                .getOrganisationUnits() );
            for( OrganisationUnit orgUnit : userOrgUnits )
            {
                if( orgUnit.getHierarchyLevel() == 3 )
                {
                    lastLevelOrgUnit.add( orgUnit );
                }
                else
                {
                    lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
                }
            }
            orgUnits.retainAll( lastLevelOrgUnit );
            Collections.sort( orgUnits, new IdentifiableObjectNameComparator() );
        }
        else
        {
            OrganisationUnitGroupSet organisationUnitGroupSet = organisationUnitGroupService
                .getOrganisationUnitGroupSet( ORGUNIT_GROUP_SET );

            orgUnitGrpList.addAll( organisationUnitGroupService.getAllOrganisationUnitGroups() );

            orgUnitGrpList.retainAll( organisationUnitGroupSet.getOrganisationUnitGroups() );
            Collections.sort( orgUnitGrpList, new IdentifiableObjectCodeComparator() );
            OrganisationUnit rootOrgUnit = new OrganisationUnit();
            if ( regionName != null )
            {
                List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>(
                    organisationUnitService.getOrganisationUnitByShortName( regionName ) );
                rootOrgUnit = orgUnits.get( 0 );
                int id = rootOrgUnit.getId();
                orgUnitId = id + "";
                ActionContext.getContext().getSession().put( "orgUnitId", orgUnitId );
            }
            else if ( orgUnitId != null )
            {
                rootOrgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );
                ActionContext.getContext().getSession().put( "orgUnitId", orgUnitId );
            }
            else
            {
                orgUnitId = (String) ActionContext.getContext().getSession().get( "orgUnitId" );
                rootOrgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );
            }
            orgUnits = new ArrayList<OrganisationUnit>( ivbUtil.getLeafOrganisationUnits( rootOrgUnit.getId() ) );

            organisationUnit = rootOrgUnit;
            if ( orgUnitGrpId != "" )
            {
            }
            else if ( ActionContext.getContext().getSession().get( "orgUnitGrpId" ) != null )
            {
                orgUnitGrpId = (String) ActionContext.getContext().getSession().get( "orgUnitGrpId" );
            }
            else
            {
                orgUnitGrpId = (int) show_all.getValue() + "";
            }

            if ( orgUnitGrpId != "" )
            {
                OrganisationUnitGroup orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( Integer
                    .parseInt( orgUnitGrpId ) );
                orgUnits.retainAll( orgUnitGroup.getMembers() );
            }
        }

        List<DataSet> datasetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );
        //Collections.sort( datasetList, new DataSetSortOrderComparator() );
        
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
        //Collections.sort( datasetList, new IdentifiableObjectNameComparator() );
        
        
        Map<String, KeyFlagAnalytics> keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>();
        keyFlagAnalyticMap = new HashMap<String, KeyFlagAnalytics>( keyFlagAnalyticsService.getKeyFlagAnalyticsMap() );
        
        for ( DataSet dataSet : datasetList )
        {
            for ( Indicator indicator : dataSet.getIndicators() )
            {
                if( !indicatorList.contains( indicator ) )
                {
                    indicatorList.add( indicator );
                    datasetMap.put( indicator.getUid(),new ArrayList<DataSet>(indicator.getDataSets()));
                }
                
                for ( OrganisationUnit orgUnit : orgUnits )
                {
                    KeyFlagAnalytics kfa = keyFlagAnalyticMap.get( orgUnit.getId() + ":" + indicator.getId() );
                    
                    String mapKey =  indicator.getUid() + "-" + orgUnit.getUid();
                    
                    /*
                    lastUpdated = keyFlagCalculation.getLastUpdated();

                    String keyIndicatorValue = "";
                    String exString = indicator.getNumerator();
                    if ( exString.contains( KeyFlagCalculation.NESTED_OPERATOR_AND )
                        || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
                    {
                        keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), orgUnit );
                    }
                    else
                    {
                        keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), orgUnit );
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
                        keyIndicatorVal = Math.round( keyIndicatorVal * 100 );
                        keyIndicatorVal = keyIndicatorVal / 100;
                        keyIndicatorValue = "" + keyIndicatorVal;
                    }
                    catch ( Exception e )
                    {
                    }
                    commentMap = keyFlagCalculation.getCommentMap();
                    sourceMap = keyFlagCalculation.getSourceMap();
                    keyIndicatorValueMap.put( indicator.getUid() + "-" + orgUnit.getUid(), keyIndicatorValue );
                    colorMap = keyFlagCalculation.getColorMap();
                    
                    // thresholdMap.put( indicator.getUid() + "-" +
                    // orgUnit.getUid(), thresoldValue );
                    
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
                            lastUpdated = kfa.getLastUpdated();
                        } 
                    }
                    
                }
            }
        }
        
        
        //indicatorList = new ArrayList<Indicator>( keyFlagCalculation.getIndicatorList() )  ;
        //datasetMap = keyFlagCalculation.getDatasetMap();       
        
        // valueMap = keyFlagCalculation.getValueMap();
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
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>(
            organisationUnitService.getAllOrganisationUnits() );
        for ( OrganisationUnit org : orgUnitList )
        {
            for ( OrganisationUnit o : ivbUtil.getLeafOrganisationUnits( org.getId() ) )
            {
                if ( !(countryList.contains( "\"" + o.getShortName() + "\"" )) )
                {
                    countryList.add( "\"" + o.getShortName() + "\"" );
                }
            }
        }
        for ( OrganisationUnit region : new ArrayList<OrganisationUnit>(
            organisationUnitService.getOrganisationUnitsAtLevel( REGION_LEVEL ) ) )
        {
            regionList.add( "\"" + region.getShortName() + "\"" );
        }
        Collections.sort( countryList );
        Collections.sort( regionList );
        return SUCCESS;
    }
}
