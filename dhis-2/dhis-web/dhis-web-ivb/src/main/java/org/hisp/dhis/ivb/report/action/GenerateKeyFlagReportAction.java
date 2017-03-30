package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
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
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class GenerateKeyFlagReportAction
    implements Action
{

    private static final String INTRO_YEAR_DE_GROUP = "INTRO_YEAR_DE_GROUP";

    private static final String VACCINE_ATTRIBUTE = "VACCINE_ATTRIBUTE";    
   
    private static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

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

    private List<Integer> sectionIDs;

    public void setSectionIDs( List<Integer> sectionIDs )
    {
        this.sectionIDs = sectionIDs;
    }

    private String introStartDate;

    public void setIntroStartDate( String introStartDate )
    {
        this.introStartDate = introStartDate;
    }

    private String introEndDate;

    public void setIntroEndDate( String introEndDate )
    {
        this.introEndDate = introEndDate;
    }

    private Integer orgUnitGroupId;

    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    private String orgUnitId;

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private List<Integer> selectedListDataset;

    public void setSelectedListDataset( List<Integer> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }
    
   /* private String vaccines;
    
    public String getVaccines()
    {
        return vaccines;
    }

    public void setVaccines( String vaccines )
    {
        this.vaccines = vaccines;
    }

    */
    
    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

   
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
    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }

    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }

   /* private String vaccinesByComma = "";

    public String getVaccinesByComma()
    {
        return vaccinesByComma;
    }
	*/
    private String introductionDates;

    public String getIntroductionDates()
    {
        return introductionDates;
    }
    
    private Map<String,String> dataSetMap = new HashMap<String,String>();
    
    public Map<String, String> getDataSetMap()
    {
        return dataSetMap;
    }
    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
    {
        this.orgUnitIds = orgUnitIds;
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
        
        lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );

        //DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        //Constant introYearGroupConstant = constantService.getConstantByName( INTRO_YEAR_DE_GROUP );
        //DataElementGroup introYearDEGroup = dataElementService.getDataElementGroup( (int) introYearGroupConstant.getValue() );
        //List<DataElement> introYearDEs = new ArrayList<DataElement>( introYearDEGroup.getMembers() );

        //Constant vaccineAttributeConstant = constantService.getConstantByName( VACCINE_ATTRIBUTE );

        List<OrganisationUnit> orgUnitList =  new ArrayList<OrganisationUnit>();
        if(orgUnitIds.size() > 0)
        {
            for(Integer id : orgUnitIds)
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );
                orgUnitList.add( orgUnit );
            }
        }
        else if(selectionTreeManager.getReloadedSelectedOrganisationUnits() != null)
        { 
            orgUnitList =  new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() ) ;            
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
            orgUnitList.retainAll( lastLevelOrgUnit );
        }        
        Collections.sort(orgUnitList, new IdentifiableObjectNameComparator() );
        
        // Filter2: Getting orgunit list whose intro year is between the
        // selected start year and end year
        /* if(vaccines == null)
        {
            List<Section> dataSetSections = new ArrayList<Section>();
            List<String> sectionNames = new ArrayList<String>();
            
            for ( Integer sectionId : sectionIDs )
            {
                Section section = sectionService.getSection( sectionId );
                dataSetSections.add( section );
                sectionNames.add( section.getName().trim() );
                //vaccinesByComma += section.getName() + ", ";
            }

            // Getting start and end period from introStartDate && introEndDate
            Date sDate = null;
            Date eDate = null;
           
             sDate = getStartDateByString( introStartDate );
             eDate = getEndDateByString( introEndDate );

            introductionDates = introStartDate + " to " + introEndDate;
            Iterator<OrganisationUnit> orgUnitIterator = orgUnitList.iterator();
            while ( orgUnitIterator.hasNext() )
            {
                OrganisationUnit orgUnit = orgUnitIterator.next();

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
                                DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                                if ( dv != null && dv.getValue() != null )
                                {
                                    String value = dv.getValue();
                                    Date valueDate = getStartDateByString( value );
                                    if ( valueDate.equals( sDate ) || valueDate.equals( eDate )
                                    || (valueDate.after( sDate ) && valueDate.before( eDate )) )
                                    {
                                        flag = 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
            }

            if ( flag == 0 )
            {
                orgUnitIterator.remove();
            }
        }
        } */
        
        Set<DataElement> userDes = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
        	userDes.addAll( userAuthorityGroup.getDataElements() );
        }
        
        orgUnits = orgUnitList;
        
        for ( Integer dataSetId : selectedListDataset )
        {
            DataSet dataSet = dataSetService.getDataSet( dataSetId );           
            List<Indicator> indicaList = new ArrayList<Indicator>( dataSet.getIndicators() );
            for ( Indicator indicator : indicaList )
            {
                for ( OrganisationUnit orgUnit : orgUnits )
                {                   
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
                    valueMap.put( indicator.getUid() + "-" + orgUnit.getUid(), keyIndicatorValue );
                    commentMap = keyFlagCalculation.getCommentMap() ;
                    sourceMap = keyFlagCalculation.getSourceMap();                    
                    periodMap = keyFlagCalculation.getPeriodMap();
                }                
                //dataSetMap.put( indicator.getUid() , dataSet.getDisplayName() );
                dataSetMap.put( indicator.getUid() , dataSet.getShortName() );
            }
            indicatorList = new ArrayList<Indicator>( keyFlagCalculation.getIndicatorList() )  ;            
        }
        
        Constant tabularDataElementGroupId = constantService.getConstantByName( TABULAR_REPORT_DATAELEMENTGROUP_ID );
        
        List<DataElement> dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );

        List<Integer >headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";

        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
        }

        List<Integer> orgunitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
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
