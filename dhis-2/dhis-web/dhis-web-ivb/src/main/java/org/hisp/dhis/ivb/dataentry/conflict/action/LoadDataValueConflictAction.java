package org.hisp.dhis.ivb.dataentry.conflict.action;

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
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
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

import com.opensymphony.xwork2.Action;

public class LoadDataValueConflictAction implements Action  
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
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
    
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }
    
    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }
    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }
    
    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    private DataValueAuditService dataValueAuditService;
    
    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
    }
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
    
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private List<Integer> dataSetSelectedList;
    
    public void setDataSetSelectedList( List<Integer> dataSetSelectedList )
    {
        this.dataSetSelectedList = dataSetSelectedList;
    }

    private String orgUnitUid;

    public void setOrgUnitUid( String orgUnitUid )
    {
        this.orgUnitUid = orgUnitUid;
    }

    public String getOrgUnitUid()
    {
        return orgUnitUid;
    }
    
    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String oranisationUnitId;

    public String getOranisationUnitId()
    {
        return oranisationUnitId;
    }

    public void setOranisationUnitId( String oranisationUnitId )
    {
        this.oranisationUnitId = oranisationUnitId;
    }
    private String statusMessage;

    public String getStatusMessage()
    {
        return statusMessage;
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
    private String orgName;

    public String getOrgName()
    {
        return orgName;
    }
    public void setOrgName( String orgName )
    {
        this.orgName = orgName;
    }

    private Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> conflictAuditMap;
    
    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getConflictAuditMap()
    {
        return conflictAuditMap;
    }
    
    public void setConflictAuditMap( Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> conflictAuditMap )
    {
        this.conflictAuditMap = conflictAuditMap;
    }

    private Set<DataElement> dataElementList = new HashSet<DataElement>();
    
    public Set<DataElement> getDataElementList()
    {
        return dataElementList;
    }    
    public void setDataElementList( Set<DataElement> dataElementList )
    {
        this.dataElementList = dataElementList;
    }

    private List<String> countryList = new ArrayList<String>();
    
    public List<String> getCountryList()
    {
        return countryList;
    }
    public void setCountryList( List<String> countryList )
    {
        this.countryList = countryList;
    }  
    private Map<String,String> commentMap = new HashMap<String, String>();
    
    public Map<String, String> getCommentMap()
    {
        return commentMap;
    }

    public void setCommentMap( Map<String, String> commentMap )
    {
        this.commentMap = commentMap;
    }

    private Map<String,String> storedMap = new HashMap<String, String>();
    
    public Map<String, String> getStoredMap()
    {
        return storedMap;
    }

    public void setStoredMap( Map<String, String> storedMap )
    {
        this.storedMap = storedMap;
    }

    private Map<String,String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    public void setValueMap( Map<String, String> valueMap )
    {
        this.valueMap = valueMap;
    }
    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    private Map<String, String> dataSetMap = new HashMap<String, String>();
    
    public Map<String, String> getDataSetMap()
    {
        return dataSetMap;
    } 
    
    private Map<String,List<DataValueAudit>> historyMap = new HashMap<String, List<DataValueAudit>>();
    
    public Map<String, List<DataValueAudit>> getHistoryMap()
    {
        return historyMap;
    }
    private String selectedPeriod;

    public String getSelectedPeriod()
    {
        return selectedPeriod;
    }
    
    private String conflictPeriod;
    
    public void setConflictPeriod( String conflictPeriod )
    {
        this.conflictPeriod = conflictPeriod;
    }    
    
    private SimpleDateFormat standardDateFormat;
    
    public SimpleDateFormat getStandardDateFormat()
    {
        return standardDateFormat;
    }
    
    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    public String execute()
    {   
        standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        User curUser = currentUserService.getCurrentUser();
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
        userName = curUser.getUsername();

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }
        
        
        orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
        List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
        List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getOrganisationUnits() );
        for ( OrganisationUnit orgUnit : userOrgUnits )
        {
            if( orgUnit.getHierarchyLevel() == 3  )
            {
                lastLevelOrgUnit.add( orgUnit );
            }
            else
            {
                lastLevelOrgUnit.addAll( organisationUnitService.getOrganisationUnitsAtLevel( 3, orgUnit ) );
            }
        }
        orgUnitList.retainAll( lastLevelOrgUnit );        
        Collections.sort(orgUnitList, new IdentifiableObjectNameComparator() );
       
        DataElementCategoryOptionCombo  optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
          
        for( Integer dataSetId : dataSetSelectedList )
        {
            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            dataElementList.addAll( dataSet.getDataElements() );                
        } 
            
        Period period = new Period();
            
        Set<DataElement> userDataElements = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }

        if ( userDataElements == null || userDataElements.isEmpty() )
        {
            period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );
            selectedPeriod = period.getDescription();
            return SUCCESS;
        }

        dataElementList.retainAll( userDataElements );
        Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
        for(DataElement de : dataElementList)
        {
            List<AttributeValue> deAttributeValues = new ArrayList<AttributeValue>(de.getAttributeValues());
            for(AttributeValue da : deAttributeValues)
            if ( da.getAttribute().getId() == keyDEConst.getValue() &&  da.getValue().equalsIgnoreCase( "true" ))
            {
                dataElementList.remove( de );
            }
        }

        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElementList ) );        
        String dataElementIdsByComma = getCommaDelimitedString( dataElementIds );

        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        String orgUnitIdsByComma ;
        
        if(orgUnitIds.size() == 0)
        {
            orgUnitIdsByComma = "-1"; 
        }
        else
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
        }
        
        
        Date conflictDate = getDateByString( conflictPeriod );
        
        period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType() , conflictDate );
        
        //System.out.println("Start Date: "+ period.getStartDate());
        
        //System.out.println("End Date: "+ period.getEndDate());
        
        selectedPeriod = period.getDescription();
        
        period = periodService.reloadPeriod( period );
        
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        //System.out.println("Period: " + period.getId() );
        
        List<DataValue> conflictDataValueList = new ArrayList<DataValue>( ivbUtil.getConflictDataValueList( dataElementIdsByComma, orgUnitIdsByComma, period ) );

        List<DataElement> deList = new ArrayList<DataElement>();
        List<OrganisationUnit> ouList = new ArrayList<OrganisationUnit>();
        for(  DataValue dv : conflictDataValueList )
        {
            
            //DataValueAudit currentHistoryDVA = dataValueAuditService.getLatestDataValueAudit( dv.getDataElement(), dv.getSource(), DataValueAudit.DVA_CT_HISOTRY, dv.getValue(), dv.getComment() );

            List<DataValueAudit> historyList = new ArrayList<DataValueAudit>(ivbUtil.getActiveDataValueAuditByOrgUnit_DataElement_Period_type( dv.getDataElement(), dv.getSource(), period, DataValueAudit.DVA_CT_HISOTRY ) );

            
            Iterator<DataValueAudit> iterator = historyList.iterator();
            while( iterator.hasNext() )
            {
                DataValueAudit dva = iterator.next();
                
                if( dva.getValue() == null )
                    dva.setValue("");
                if( dva.getComment() ==  null )
                   dva.setComment( "" );
                
                String dvaTimeStamp = standardDateFormat.format( dva.getTimestamp() );
                String dvTimeStamp = standardDateFormat.format( dv.getLastUpdated() );
                if( dvaTimeStamp == null )  dvaTimeStamp = "";
                if( dvTimeStamp == null ) dvTimeStamp = "";
                
                //System.out.print( historyList.size() + " - " + dva.getValue() + "  " + dv.getValue() + "  " + dva.getComment() + "  " + dv.getComment() + "  " + dva.getModifiedBy() + "  " + dv.getStoredBy() + "  " + dvaTimeStamp + "  " + dvTimeStamp );
                if( dva.getValue().trim().equals( dv.getValue().trim() ) && dva.getComment().trim().equals( dv.getComment().trim() ) && dva.getModifiedBy().trim().equals( dv.getStoredBy().trim() ) && dvaTimeStamp.trim().equals( dvTimeStamp.trim() ) )
                {
                	//System.out.println( " DV and DVA are same, removing duplicate one" );
                    iterator.remove();
                    //break;
                }
                else{
                	 deList.add( dv.getDataElement() );
                     ouList.add( dv.getSource() );
                }
            }
            
            historyMap.put( dv.getSource().getUid() + "-" + dv.getDataElement().getUid(), historyList );
          //  deList.add( dv.getDataElement() );
           // ouList.add( dv.getSource() );
            List<DataSet> dsList = new ArrayList<DataSet>( dv.getDataElement().getDataSets());
            
            if( dsList != null )
            {
                for( DataSet dataSet : dsList )
                {
                    if( dataSet.getSources() != null && dataSet.getSources().size() > 0 )
                    {
                        String dataSetName =  dataSetMap.get( dv.getDataElement().getUid() );
                      
                        if( dataSetName == null )
                        {
                        	dataSetName = dataSet.getDisplayName() + "<br>";
                        	
                        }
                        else
                        {
                        	dataSetName = dataSet.getDisplayName() + "<br>";
                        }
                        dataSetMap.put( dv.getDataElement().getUid(), dataSetName );                        
                        //break;
                    }
                }
            }           
            // dataSetMap.put( dv.getDataElement().getUid(), dsList.get( 0 ) );
            valueMap.put( dv.getSource().getUid() + "-" + dv.getDataElement().getUid(), dv.getValue() );
            commentMap.put( dv.getSource().getUid() + "-" + dv.getDataElement().getUid(), dv.getComment());
            storedMap.put( dv.getSource().getUid() + "-" + dv.getDataElement().getUid(), dv.getStoredBy()+" ("+ standardDateFormat.format( dv.getLastUpdated() ) +") ");
            
        }        
        dataElementList.retainAll( deList );
        orgUnitList.retainAll( ouList );
        
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
