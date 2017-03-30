package org.hisp.dhis.ivb.report.action;

//import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
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
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class GaviReportResultAction implements Action
{

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
    
    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------
    
    private String isoCode;
    
    private String whoRegion;
    
    private String unicefRegion;
    
    private String incomeLevel;
    
    private String gaviEligibleStatus;

    public String getIsoCode() 
    {
		return isoCode;
	}

	public void setIsoCode(String isoCode) 
	{
		this.isoCode = isoCode;
	}

	public String getWhoRegion() 
	{
		return whoRegion;
	}

	public void setWhoRegion(String whoRegion) 
	{
		this.whoRegion = whoRegion;
	}

	public String getUnicefRegion() 
	{
		return unicefRegion;
	}

	public void setUnicefRegion(String unicefRegion) 
	{
		this.unicefRegion = unicefRegion;
	}

	public String getIncomeLevel() 
	{
		return incomeLevel;
	}

	public void setIncomeLevel(String incomeLevel) 
	{
		this.incomeLevel = incomeLevel;
	}

	public String getGaviEligibleStatus() 
	{
		return gaviEligibleStatus;
	}

	public void setGaviEligibleStatus(String gaviEligibleStatus) 
	{
		this.gaviEligibleStatus = gaviEligibleStatus;
	}    

    private List<Integer> selectedListVaccine;

    private String introStartDate;

    private String introEndDate;

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

    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
    {
        this.orgUnitIds = orgUnitIds;
    }
    
    private String language;

    private String userName;

    public String getLanguage()
    {
        return language;
    }

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

    private Map<OrganisationUnit, Map<String, Map<Integer, String>>> gaviOrgUnitResultMap;

    public Map<OrganisationUnit, Map<String, Map<Integer, String>>> getGaviOrgUnitResultMap()
    {
        return gaviOrgUnitResultMap;
    }
    
    private List<OrganisationUnit> gaviApplicationsOrgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getGaviApplicationsOrgUnitList()
    {
        return gaviApplicationsOrgUnitList;
    }

    private DataElementGroup gaviAppYearMonthDEGroup;

    private DataElementGroup gaviAppStatus;

    private DataElementGroup gaviIntroSupportStatus;
    
    private DataElementGroup vaccinePresentation;
    
    private DataElementGroup introStatus;
    
    private DataElementGroup introYear;
    
    public DataElementGroup getIntroYear()
    {
        return introYear;
    }

    public DataElementGroup getIntroStatus()
    {
        return introStatus;
    }

    public DataElementGroup getVaccinePresentation()
    {
        return vaccinePresentation;
    }

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

    private List<Section> dataSetSections = new ArrayList<Section>();
    
    public List<Section> getDataSetSections()
    {
        return dataSetSections;
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
    	if( isoCode != null )
        {
    	    isoCode = "ON";
        }
        if( whoRegion != null )
        {
            whoRegion = "ON";
        }
        if( unicefRegion != null )
        {
            unicefRegion = "ON";
        }
        if( incomeLevel != null )
        {
            incomeLevel = "ON";
        }
        if( gaviEligibleStatus != null )
        {
            gaviEligibleStatus = "ON";
        }
        
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

        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        /*
         * Need to change group ids to constants
         */
        gaviAppYearMonthDEGroup = dataElementService.getDataElementGroup( 38 );
        gaviAppStatus = dataElementService.getDataElementGroup( 37 );
        gaviIntroSupportStatus = dataElementService.getDataElementGroup( 39 );
        vaccinePresentation = dataElementService.getDataElementGroup( 41 );
        introStatus = dataElementService.getDataElementGroup( 40 );
        introYear = dataElementService.getDataElementGroup( 42 );

        Set<DataElementGroup> gaviDataElementGroups = new HashSet<DataElementGroup>();
        gaviDataElementGroups.add( gaviAppStatus );
        gaviDataElementGroups.add( gaviIntroSupportStatus );
        gaviDataElementGroups.add( vaccinePresentation );
        gaviDataElementGroups.add( introStatus );
        gaviDataElementGroups.add( introYear );
        
        List<DataElement> gaviAppYearDEs = new ArrayList<DataElement>( gaviAppYearMonthDEGroup.getMembers() );
        
        Constant vaccineAttributeConstant = constantService.getConstantByName( VACCINE_ATTRIBUTE );
        
        List<String> sectionNames = new ArrayList<String>();
        for( Integer sectionId : selectedListVaccine )
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

        if ( orgUnitIds.size() > 1 )
        {
            for ( Integer id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );

                if ( orgUnit.getHierarchyLevel() == 3 )
                {
                    gaviApplicationsOrgUnitList.add( orgUnit );
                }
            }
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
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
            gaviApplicationsOrgUnitList.retainAll( lastLevelOrgUnit );
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date currentDate = new Date();
        Collections.sort( gaviApplicationsOrgUnitList, new IdentifiableObjectNameComparator() );

        // Filter2: Getting orgunit list whose intro year is between the
        // selected start year and end year
        gaviOrgUnitResultMap = new HashMap<OrganisationUnit, Map<String, Map<Integer, String>>>();
        Iterator<OrganisationUnit> orgUnitIterator = gaviApplicationsOrgUnitList.iterator();
        while ( orgUnitIterator.hasNext() )
        {
            OrganisationUnit orgUnit = orgUnitIterator.next();

            Map<String, Map<Integer, String>> sectionResultMap = gaviOrgUnitResultMap.get( orgUnit );
            if ( sectionResultMap == null || sectionResultMap.size() <= 0 )
            {
                sectionResultMap = new HashMap<String, Map<Integer, String>>();
            }

            int flag = 0;
            for ( DataElement dataElement : gaviAppYearDEs )
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
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
                                if ( valueResultMap == null || valueResultMap.size() <= 0 )
                                {
                                    valueResultMap = new HashMap<Integer, String>();
                                }
                                Date valueDate = getStartDateByString( value );

                                if( valueDate != null && (sDate == null || eDate == null) )
                                {
                                    valueResultMap.put( gaviAppYearMonthDEGroup.getId(), value );
                                    sectionResultMap.put( deAttributeValue.getValue().trim(), valueResultMap );
                                    gaviOrgUnitResultMap.put( orgUnit, sectionResultMap );
                                    flag = 1;
                                }
                                else
                                {
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
                                }
                                //System.out.println( orgUnit.getName() + " : " + dataElement.getName() + " : " + valueDate + " : " + sDate + " : " + eDate );
                            }
                            else
                            {
                                Map<Integer, String> valueResultMap = sectionResultMap.get( deAttributeValue.getValue().trim() );
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
                                    DataValue dv = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );

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
                                        //System.out.println( orgUnit.getName() + " : " + dataElement.getName() + " : " + value );
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

        Constant tabularDataElementGroupId = constantService.getConstantByName( TABULAR_REPORT_DATAELEMENTGROUP_ID );
        
        List<DataElement> dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );

        List<Integer >headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";

        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
        }

        List<Integer> gaviApplicationOrgunitIds = new ArrayList<Integer>( getIdentifiers( gaviApplicationsOrgUnitList ) );
        String orgUnitIdsByComma = "-1";
        if ( gaviApplicationOrgunitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( gaviApplicationOrgunitIds );
        }
        
        headerDataValueMap = ivbUtil.getLatestDataValuesForTabularReport( headerDataElementIdsByComma, orgUnitIdsByComma );
        
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


