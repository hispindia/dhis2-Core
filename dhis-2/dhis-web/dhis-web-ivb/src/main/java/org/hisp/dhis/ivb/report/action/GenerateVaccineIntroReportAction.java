package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */
public class GenerateVaccineIntroReportAction
    implements Action
{

    private static final String INTRO_YEAR_DE_GROUP = "INTRO_YEAR_DE_GROUP";

    private static final String VACCINE_ATTRIBUTE = "VACCINE_ATTRIBUTE"; 

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
    
    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
    {
	this.orgUnitIds = orgUnitIds;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getDataElementType()
    {
        return dataElementType;
    }

    private Map<OrganisationUnit, Map<String, Map<Integer,String>>> orgUnitResultMap;
    
    private Map<OrganisationUnit, Map<String, Map<Integer,String>>> orgUnitCommentMap;    

    public Map<OrganisationUnit, Map<String, Map<Integer, String>>> getOrgUnitCommentMap() 
    {
		return orgUnitCommentMap;
	}

	private List<Section> dataSetSections;

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    private List<DataElementGroup> dataElementGroups;

    private String language;

    private String userName;

    private DataElementGroup introYearDEGroup;
    
    public DataElementGroup getIntroYearDEGroup()
    {
        return introYearDEGroup;
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

    private Map<String, String> countryGeneralInfoMap = new HashMap<String, String>();
    
    public Map<String, String> getCountryGeneralInfoMap()
    {
        return countryGeneralInfoMap;
    }

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
    public String execute()
    {
        userName = currentUserService.getCurrentUser().getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        dataElementGroups = new ArrayList<DataElementGroup>( dataElementService.getDataElementGroupsByUid( dataElementGroupUIDs ) );

        Constant introYearGroupConstant = constantService.getConstantByName( INTRO_YEAR_DE_GROUP );
        introYearDEGroup = dataElementService.getDataElementGroup( (int) introYearGroupConstant.getValue() );
        List<DataElement> introYearDEs = new ArrayList<DataElement>( introYearDEGroup.getMembers() );

        Constant vaccineAttributeConstant = constantService.getConstantByName( VACCINE_ATTRIBUTE );

        dataSetSections = new ArrayList<Section>();
        List<String> sectionNames = new ArrayList<String>();
        for ( Integer sectionId : selectedListVaccine )
        {
            Section section = sectionService.getSection( sectionId );
            dataSetSections.add( section );
            
            sectionNames.add( section.getName().trim() );
        }

        // Getting start and end period from introStartDate && introEndDate
        Date sDate = null;
        Date eDate = null;

        if( introStartDate != null && !introStartDate.trim().equals( "" ) )
        {
            sDate = getStartDateByString( introStartDate );
        }
        
        if( introEndDate != null && !introEndDate.trim().equals( "" ) )
        {
            eDate = getEndDateByString( introEndDate );
        }
        
        //Date sDate = getStartDateByString( introStartDate );
        //Date eDate = getEndDateByString( introEndDate );
        if( orgUnitIds.size() > 1 )
        {
            for(Integer id : orgUnitIds )
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
        orgUnitResultMap = new HashMap<OrganisationUnit, Map<String, Map<Integer, String>>>();
        orgUnitCommentMap = new HashMap<OrganisationUnit, Map<String, Map<Integer, String>>>();
        Iterator<OrganisationUnit> orgUnitIterator = orgUnitList.iterator();
        
        while ( orgUnitIterator.hasNext() )
        {
            OrganisationUnit orgUnit = orgUnitIterator.next();

            Map<String, Map<Integer, String>> sectionResultMap = orgUnitResultMap.get( orgUnit );
            Map<String, Map<Integer, String>> sectionCommentMap = orgUnitCommentMap.get( orgUnit );
            
            if ( sectionResultMap == null || sectionResultMap.size() <= 0 )
            {
                sectionResultMap = new HashMap<String, Map<Integer,String>>();
            }
            if ( sectionCommentMap == null || sectionCommentMap.size() <= 0 )
            {
                sectionCommentMap = new HashMap<String, Map<Integer,String>>();
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
                            && deAttributeValue.getValue() != null && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                        {
                            DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                            if ( dv != null && dv.getValue() != null )
                            {
                                String value = dv.getValue();
                                String comment = dv.getComment();
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }

                                Map<Integer, String> valueCommentMap = sectionCommentMap.get( deAttributeValue.getValue().trim() );
                                if ( valueCommentMap == null || valueCommentMap.size() <= 0 )
                                {
                                	valueCommentMap = new HashMap<Integer, String>();
                                }
                                
                                Date valueDate = getStartDateByString( value );
                                if( valueDate != null && (sDate == null || eDate == null) )
                                {
                                    valueResultMap.put( introYearDEGroup.getId(), value );
                                    sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                    orgUnitResultMap.put( orgUnit, sectionResultMap );
                                    
                                    valueCommentMap.put( introYearDEGroup.getId(), comment );
                                    sectionCommentMap.put(deAttributeValue.getValue().trim(), valueCommentMap );
                                    orgUnitCommentMap.put( orgUnit, sectionCommentMap );
                                    flag = 1;
                                }
                                else
                                {
                                    if( valueDate!= null && sDate.getTime() <= valueDate.getTime() && valueDate.getTime() <= eDate.getTime())
                                    {
                                        valueResultMap.put( introYearDEGroup.getId(), value );
                                        sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                        orgUnitResultMap.put( orgUnit, sectionResultMap );
                                        
                                        valueCommentMap.put( introYearDEGroup.getId(), comment );
                                        sectionCommentMap.put(deAttributeValue.getValue().trim(), valueCommentMap );
                                        orgUnitCommentMap.put( orgUnit, sectionCommentMap );

                                        if ( valueDate.equals( sDate ) || valueDate.equals( eDate ) || (valueDate.after( sDate ) && valueDate.before( eDate )) )
                                        {
                                            flag = 1;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                valueResultMap.put( introYearDEGroup.getId(), " " );
                                sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                orgUnitResultMap.put( orgUnit, sectionResultMap );                                

                                Map<Integer, String> valueCommnetMap = sectionCommentMap.get( deAttributeValue.getValue().trim() );
                                if ( valueCommnetMap == null || valueCommnetMap.size() <= 0 )
                                {
                                	valueCommnetMap = new HashMap<Integer, String>();
                                }
                                valueCommnetMap.put( introYearDEGroup.getId(), " " );
                                sectionCommentMap.put( deAttributeValue.getValue().trim(), valueCommnetMap );
                                orgUnitCommentMap.put( orgUnit, sectionCommentMap );                                

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
                                    && deAttributeValue.getValue() != null && sectionNames.contains( deAttributeValue.getValue().trim() ) )
                                {
                                    DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
                                   
                                        if ( dv != null && dv.getValue() != null )
                                        {
                                            String value = dv.getValue();
                                            String comment = dv.getComment();
                                            Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                            if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                            {
                                               valueResultMap = new HashMap<Integer, String>();
                                            }                                        
                                            valueResultMap.put( dataElementGroup.getId(), value );
                                            sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                            orgUnitResultMap.put( orgUnit, sectionResultMap );
                                            
                                            Map<Integer, String> valueCommentMap = sectionCommentMap.get( deAttributeValue.getValue().trim() );
                                            if ( valueCommentMap == null || valueCommentMap.size() <= 0 )
                                            {
                                            	valueCommentMap = new HashMap<Integer, String>();
                                            }                                        
                                            valueCommentMap.put( dataElementGroup.getId(), comment );
                                            sectionCommentMap.put( deAttributeValue.getValue().trim(), valueCommentMap );
                                            orgUnitCommentMap.put( orgUnit, sectionCommentMap );                                        

                                        }
                                       else
                                        {
                                            Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                            if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                            {
                                                valueResultMap = new HashMap<Integer, String>();
                                            }
                                            valueResultMap.put( dataElementGroup.getId()," " );
                                            sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                            orgUnitResultMap.put( orgUnit, sectionResultMap );                                        

                                            Map<Integer, String> valueCommentMap = sectionCommentMap.get( deAttributeValue.getValue().trim() );
                                            if ( valueCommentMap == null || valueCommentMap.size() <= 0 )
                                            {
                                            	valueCommentMap = new HashMap<Integer, String>();
                                            }
                                            valueCommentMap.put( dataElementGroup.getId()," " );
                                            sectionCommentMap.put( deAttributeValue.getValue().trim(), valueCommentMap );
                                            orgUnitCommentMap.put( orgUnit, sectionCommentMap );                                        

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
            }
        }
        
        
        for( OrganisationUnit orgUnit : orgUnitList )
        {
            /**
             * Hardcoded dataelementids need to change this later
             */
            DataElement de1 = dataElementService.getDataElement( 4 );            
            DataValue dv = dataValueService.getLatestDataValue( de1, optionCombo, orgUnit );            
            if( dv == null || dv.getValue() == null )
            {
                countryGeneralInfoMap.put( orgUnit.getId()+":4", "" );
            }
            else
            {
                countryGeneralInfoMap.put( orgUnit.getId()+":4", dv.getValue() );
            }

            de1 = dataElementService.getDataElement( 3 );
            dv = dataValueService.getLatestDataValue( de1, optionCombo, orgUnit );            
            if( dv == null || dv.getValue() == null )
            {
                countryGeneralInfoMap.put( orgUnit.getId()+":3", "" );
            }
            else
            {
                countryGeneralInfoMap.put( orgUnit.getId()+":3", dv.getValue() );
            }
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
