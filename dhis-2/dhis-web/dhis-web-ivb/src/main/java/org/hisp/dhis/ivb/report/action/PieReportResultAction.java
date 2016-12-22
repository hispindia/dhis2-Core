package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
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

public class PieReportResultAction implements Action 
{

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

    private String introStartDate;

    private String introEndDate;

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
    
    private List<String> selectedList;
    
    public List<String> getSelectedList()
    {
        return selectedList;
    }

    public void setSelectedList( List<String> selectedList )
    {
        this.selectedList = selectedList;
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

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
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

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();
    
    private Map<String, List<String>> reviewResultMap = new HashMap<String, List<String>>();
    
    public Map<String, List<String>> getReviewResultMap()
    {
        return reviewResultMap;
    }

    private List<String> selReviews = new ArrayList<String>();
    
    public List<String> getSelReviews()
    {
        return selReviews;
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

        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );
                
        // Getting start and end period from introStartDate && introEndDate
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        /*
        Date sDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( sDate );
        cal.add( Calendar.YEAR, 1 );        
        Date eDate = cal.getTime();
        */

        //introStartDate = dateFormat.format( sDate );
        //introEndDate = dateFormat.format( eDate );
        
        try
        {
            if( introStartDate.length() == 4 )
            {
                Integer temp = Integer.parseInt( introStartDate );
                introStartDate = introStartDate +"-01";
            }

            if( introEndDate.length() == 4 )
            {
                Integer temp = Integer.parseInt( introEndDate );
                introEndDate = introEndDate +"-12";
            }
        }
        catch( Exception e )
        {
            
        }
        
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
        
        if ( orgUnitIds.size() > 1 )
        {
            for ( Integer id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );

                if( orgUnit.getHierarchyLevel() == 3 )
                {
                    orgUnitList.add( orgUnit );
                }
            }
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
            for ( OrganisationUnit orgUnit : userOrgUnits )
            {
                if( orgUnit.getHierarchyLevel()== 3 )
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


        
        Date currentDate = new Date();
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );

        List<Integer> gaviApplicationOrgunitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        String orgUnitIdsByComma = "-1";
        if ( gaviApplicationOrgunitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( gaviApplicationOrgunitIds );
        }

        String dataElementIdsByComma = "-1";
        for( String selReview : selectedList )
        {
            dataElementIdsByComma += "," + selReview.split( ":" )[1];
            dataElementIdsByComma += "," + selReview.split( ":" )[2];
            dataElementIdsByComma += "," + selReview.split( ":" )[3];
            selReviews.add( selReview.split( ":" )[0] );
        }
        
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );

        // Filter2: Getting orgunit list whose intro year is between the
        // selected start year and end year
        Iterator<OrganisationUnit> orgUnitIterator = orgUnitList.iterator();
        while ( orgUnitIterator.hasNext() )
        {
            OrganisationUnit orgUnit = orgUnitIterator.next();

            for( String selReview : selectedList )
            {
                List<String> reviewList = new ArrayList<String>();
                reviewList.add( selReview.split( ":" )[0] );

                DataValue dv = dataValueMap.get( orgUnit.getId()+":"+selReview.split( ":" )[2] );
                if( dv != null && dv.getValue() != null )
                {
                    reviewList.add( dv.getValue() );
                    //System.out.println("1. " + dv.getValue() );
                }
                else
                {
                    reviewList.add( " " );
                    //System.out.println("2. " + dv.getValue() );
                }

                DataValue dv1 = dataValueMap.get( orgUnit.getId()+":"+selReview.split( ":" )[1] );
                if( dv1 != null && ( ( dv1.getValue() != null && !dv1.getValue().trim().equals( "" ) ) || ( dv1.getComment() != null && !dv1.getComment().trim().equals( "" ) ) ) )
                {
                    if( dv1.getValue() != null )
                    {
                        reviewList.add( dv1.getValue() );
                        //System.out.println("3. " + dv1.getValue()  );
                    }
                    else
                    {
                        reviewList.add( " " );
                    }
                    
                    if( dv1.getComment() != null && !dv1.getComment().trim().equals( "" ) )
                    {
                        reviewList.add( dv1.getComment() );
                        //System.out.println("4. " + dv1.getComment() );
                    }
                    else
                    {
                        reviewList.add( " " );
                    }                    
                }
                else if( (selReview.split( ":" )[0].trim().equalsIgnoreCase( "EPI" ) || selReview.split( ":" )[0].trim().equalsIgnoreCase( "Surveillance" )) && dv != null )
                {
                    if( dv.getValue() != null )
                    {
                        reviewList.add( dv.getValue() );
                        //System.out.println("5. " + dv.getValue()  );
                    }
                    else
                    {
                        reviewList.add( " " );
                    }
                    
                    if( dv.getComment() != null )
                    {
                        reviewList.add( dv.getComment() );
                        //System.out.println("6. " + dv.getComment()  );
                    }
                    else
                    {
                        reviewList.add( " " );
                    }                    
                }
                else
                {
                    reviewList.add( " " );
                    reviewList.add( " " );
                }

                DataValue dv2 = dataValueMap.get( orgUnit.getId()+":"+selReview.split( ":" )[3] );
                if( dv2 != null && dv2.getValue() != null )
                {
                    reviewList.add( dv2.getValue() );
                }
                else
                {
                    reviewList.add( " " );
                }

                String value = reviewList.get( 2 );
                Date valueDate = getStartDateByString( value );
                //System.out.println( orgUnit.getId()+":"+selReview.split( ":" )[0] +" ----- " + value );
                if( valueDate != null && (sDate == null || eDate == null) )
                {
                    reviewResultMap.put( orgUnit.getId()+":"+selReview.split( ":" )[0], reviewList );
                }
                else
                {
                    if ( valueDate != null && sDate.getTime() <= valueDate.getTime() && valueDate.getTime() <= eDate.getTime() )
                    {
                        reviewResultMap.put( orgUnit.getId()+":"+selReview.split( ":" )[0], reviewList );
                    }
                }
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
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( currentDate );
        
        String curMonth = "";
        if( cal.get( Calendar.MONTH )+1 <= 9 )
        {
            curMonth = "-0"+cal.get( Calendar.MONTH )+1;
        }
        else
        {
            curMonth = "-"+cal.get( Calendar.MONTH )+1;
        }
        
        String curDay = "";
        if( cal.get( Calendar.DATE ) <= 9 )
        {
            curDay = "-0"+cal.get( Calendar.DATE );
        }
        else
        {
            curDay = "-"+cal.get( Calendar.DATE );
        }
        
        String startDate = "";
        String[] startDateParts = dateStr.split( "-" );
        if ( startDateParts.length <= 1 )
        {
            startDate = startDateParts[0] + curMonth + curDay;
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
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-" + (monthDays[Integer.parseInt( endDateParts[1] )] + 1);
            }
            else
            {
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-" + (monthDays[Integer.parseInt( endDateParts[1] )]);
            }
        }

        Date eDate = format.parseDate( endDate );

        return eDate;
    }

}
