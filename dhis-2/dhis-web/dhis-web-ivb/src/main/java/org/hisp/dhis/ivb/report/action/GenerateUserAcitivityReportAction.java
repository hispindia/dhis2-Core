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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class GenerateUserAcitivityReportAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

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
    
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private UserService userService;
    
    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }
    
    private PeriodService periodService;
    
    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }  
    
    @Autowired
    private LookupService lookupService;
    
    // -------------------------------------------------------------------------
    // Getter / Setters
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
    
    private List<Integer> orgUnitIds = new ArrayList<Integer>();
    
    public void setOrgUnitIds(List<Integer> orgUnitIds) 
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
    
    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
    
    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }
    
    private Map<String, List<DataValueAudit>> dataValueAuditMap;
    
    public Map<String, List<DataValueAudit>> getDataValueAuditMap()
    {
        return dataValueAuditMap;
    }

    private List<Integer> selectedListDataset = new ArrayList<Integer>();
    
    public void setSelectedListDataset( List<Integer> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    private String userByName;
    
    public void setUserByName( String userByName )
    {
        this.userByName = userByName;
    }
    
    private String reportQuarter;
    
    public void setReportQuarter( String reportQuarter )
    {
        this.reportQuarter = reportQuarter;
    }
    
    private List<DataElement> dataElements;
    
    public List<DataElement> getDataElements()
    {
        return dataElements;
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
    private String selectedUser;
    
    public void setSelectedUser( String selectedUser )
    {
        this.selectedUser = selectedUser;
    }
    
    private String reportType;
    
    public String getReportType()
    {
        return reportType;
    }
    
    private String dataSetNameByComma;
    
    public String getDataSetNameByComma()
    {
        return dataSetNameByComma;
    }
    
    private String displayUser;
    
    public String getDisplayUser()
    {
        return displayUser;
    }

    private List<Period> periods = new ArrayList<Period>();
    
    public List<Period> getPeriods()
    {
        return periods;
    }
    
    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------    
    public String execute()
    {
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        simpleDateFormat1 = new SimpleDateFormat( "MMM" );
        simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );
        userName = currentUserService.getCurrentUser().getUsername();

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
     
        // ORGUNIT
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
        String deIdsByComma = "-1";
        String orgUnitIdsByComma = "-1";
        if(orgUnitList.size() > 0) 
        {
           Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );        
           orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
        }       
        
        Date sDate = ivbUtil.getStartDateByString( startDate );
        System.out.println("startDate"+startDate);
        System.out.println("sDate"+sDate);
        Date eDate = ivbUtil.getEndDateByString( endDate );
        System.out.println("endDate"+endDate);
        
        String startD = null;
        String endD = null;
        
        try
        {
            sDate = simpleDateFormat.parse( startDate );
            eDate = simpleDateFormat.parse( endDate );
        	
            startD = startDate;
            endD = endDate;
            System.out.println("startD"+startD);
            System.out.println("endD"+endD);
        }
        catch( Exception e )
        {
            startD = simpleDateFormat.format( sDate );
            endD = simpleDateFormat.format( eDate );
        }
        
        periods.addAll( periodService.getIntersectingPeriods( sDate, eDate ) );
        System.out.println("periods"+periods);
        //Date reportDate = getDateByString( reportQuarter );
        //Period period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), reportDate );
        //period = periodService.reloadPeriod( period );
        
        Lookup lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        Set<DataElement> restrictedDes = new HashSet<DataElement>( ivbUtil.getRestrictedDataElements( restrictedDeAttributeId ) );
        User curUser = currentUserService.getCurrentUser();
        Set<DataElement> userDes = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups1 = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups1 )
        {
        	userDes.addAll( userAuthorityGroup.getDataElements() );
        }
        
        if(selectedListDataset.size() > 0)
        {
            dataElements = new ArrayList<DataElement>();
            for(Integer dsId : selectedListDataset)
            {
                DataSet dataSet = dataSetService.getDataSet( dsId );
                dataElements.addAll( dataSet.getDataElements() );
                if(dataSetNameByComma != null)
                {   
                    dataSetNameByComma = dataSetNameByComma +" , "+dataSet.getName();
                }
                else
                {
                    dataSetNameByComma = dataSet.getName();
                }
            }
            
            restrictedDes.removeAll( userDes );
            dataElements.removeAll( restrictedDes );
            
            Collection<Integer> deIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );  
            if(deIds.size() > 0)
            {
                deIdsByComma = getCommaDelimitedString( deIds );
            } 
            //deIdsByComma = getCommaDelimitedString( deIds );
            reportType = "BY_AREA_OF_WORK";
            dataValueAuditMap = ivbUtil.getDataValueAuditMapByUser_UserActivity( deIdsByComma, orgUnitIdsByComma, startD, endD, DataValueAudit.DVA_CT_HISOTRY, null );
        }
        else if(!selectedUser.isEmpty() || selectedUser != null)
        {
            dataElements = new ArrayList<DataElement>();
            User user = userService.getUser( Integer.parseInt( selectedUser ) );
            
            List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( user.getUserCredentials().getUserAuthorityGroups() );
            for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
            {
                if(userAuthorityGroup.getDataElements().size() > 0)
                {
                    dataElements.addAll( userAuthorityGroup.getDataElements() );
                }
            }

            restrictedDes.removeAll( userDes );
            dataElements.removeAll( restrictedDes );

            Collection<Integer> deIds = new ArrayList<Integer>( getIdentifiers( dataElements ) ); 
            if(deIds.size() > 0)
            {
                deIdsByComma = getCommaDelimitedString( deIds );
            }
            
            reportType = "BY_USER";
            displayUser = user.getName()+" ( "+user.getUsername()+" ) ";
            
            dataValueAuditMap = ivbUtil.getDataValueAuditMapByUser_UserActivity( deIdsByComma, orgUnitIdsByComma, startD, endD, DataValueAudit.DVA_CT_HISOTRY ,user );
        }
        else
        {
            return SUCCESS;
        } 
        
        //System.out.println( dataValueAuditMap.size() );
        //System.out.println( dataElements.size() );
        
        return SUCCESS;        
    }

    
    private Date getDateByString( String dateStr )
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
}