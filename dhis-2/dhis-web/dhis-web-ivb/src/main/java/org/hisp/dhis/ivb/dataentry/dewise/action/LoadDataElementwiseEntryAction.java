package org.hisp.dhis.ivb.dataentry.dewise.action;


import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
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
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

public class LoadDataElementwiseEntryAction implements Action  
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    private static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";
    private final static int REGION = 2;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
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

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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
    
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    private UserGroupService userGroupService;
    
    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }
    private FavoriteService favoriteService;
    
    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }
    
    @Autowired
    private IVBUtil ivbUtil;
    
    @Autowired 
    private LookupService lookupService;
    
    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private Set<DataElement> userDataElements = new HashSet<DataElement>();
    
    public Set<DataElement> getUserDataElements()
    {
        return userDataElements;
    }

    private String conflictAuthority = "";

    public String getConflictAuthority()
    {
        return conflictAuthority;
    }
    private List<UserGroup> userGrpList = new ArrayList<UserGroup>();
    
    public List<UserGroup> getUserGrpList()
    {
        return userGrpList;
    }

    private List<Integer> dataElementsSelectedList;
    
    public void setDataElementsSelectedList(List<Integer> dataElementsSelectedList) 
    {
        this.dataElementsSelectedList = dataElementsSelectedList;
    }    

    private String dataSetId = "0";

    public void setDataSetId( String dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    public String getDataSetId()
    {
        return dataSetId;
    }

    private String selectedPeriod;

    public String getSelectedPeriod()
    {
        return selectedPeriod;
    }

    private String currentPeriod;

    public String getCurrentPeriod()
    {
        return currentPeriod;
    }

    private List<DataElement> dataElementList = new ArrayList<DataElement>();

    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    private List<DataSet> dataSetList = new ArrayList<DataSet>();

    public List<DataSet> getDataSetList()
    {
        return dataSetList;
    }

    private Map<DataElement, String> off_pri_de_map;

    public Map<DataElement, String> getOff_pri_de_map()
    {
        return off_pri_de_map;
    }

    private Map<String, List<String>> optionSetMap = new HashMap<String, List<String>>();

    public Map<String, List<String>> getOptionSetMap()
    {
        return optionSetMap;
    }

    private List<DataValue> dataValueList = new ArrayList<DataValue>();

    public List<DataValue> getDataValueList()
    {
        return dataValueList;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
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

    Map<String, String> dataValue = new HashMap<String, String>();

    Map<String, String> dataComments = new HashMap<String, String>();

    Map<String, String> dataStoredBy = new HashMap<String, String>();
    
    Set<Integer> commentRequiredDe = new HashSet<Integer>();
    
    Set<Integer> percentageRequiredDe = new HashSet<Integer>();

	public Set<Integer> getPercentageRequiredDe() {
		return percentageRequiredDe;
	}

	public Set<Integer> getCommentRequiredDe() {
		return commentRequiredDe;
	}

	public Map<String, String> getDataValue()
    {
        return dataValue;
    }

    public Map<String, String> getDataComments()
    {
        return dataComments;
    }

    public Map<String, String> getDataStoredBy()
    {
        return dataStoredBy;
    }
    Map<String, String> conflictMap = new HashMap<String, String>();

    public Map<String, String> getConflictMap()
    {
        return conflictMap;
    }
    
    private PeriodType periodT;

    public PeriodType getPeriod()
    {
        return periodT;
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

    private DataElementCategoryOptionCombo dataElementCategoryOptionCombo;

    public DataElementCategoryOptionCombo getDataElementCategoryOptionCombo()
    {
        return dataElementCategoryOptionCombo;
    }

    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }

    private List<OrganisationUnit> userOrgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getUserOrgUnitList()
    {
        return userOrgUnitList;
    }

    private String statusMessage;

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public String dataSetName;

    public String getDataSetName()
    {
        return dataSetName;
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
    
    private Map<String, String> dataSetMap = new HashMap<String, String>();

    public Map<String, String> getDataSetMap()
    {
        return dataSetMap;
    }

    Map<String,String> copyRightMap = new HashMap<String, String>();    

    public Map<String, String> getCopyRightMap()
    {
        return copyRightMap;
    }
    
    private String favoriteId;
    
    public void setFavoriteId( String favoriteId )
    {
        this.favoriteId = favoriteId;
    }
    
    private Favorite favorite;
    
    public Favorite getFavorite()
    {
        return favorite;
    }
    
    private List<String> favoriteList = new ArrayList<String>();
    
    public List<String> getFavoriteList()
    {
        return favoriteList;
    }
    
    private String favoriteType;
    
    public String getFavoriteType() 
    {
        return favoriteType;
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

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    
    public String execute()
    {
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
    	Lookup lookup1 = lookupService.getLookupByName(Lookup.COMMENT_REQUIRED_DATAELEMENT_ATTRIBUTE_ID);
		int comment_attribute_id = Integer.parseInt(lookup1.getValue());
		
		
		Lookup lookup2 = lookupService.getLookupByName(Lookup.IS_PERCENTAGE);
		int percentage_attribute_id = Integer.parseInt(lookup2.getValue());
        
        favoriteType = IVBUtil.CUSTOM_DATAENTRY;
        List<Favorite> favorites = favoriteService.getAllFavoriteByFavoriteType(IVBUtil.CUSTOM_DATAENTRY);

        //List<Favorite> favorites = favoriteService.getAllFavorite();
        for(Favorite f :favorites)
        {
            favoriteList.add( "\""+ f.getName()+"\"" );
        }
        if(favoriteId != null && !favoriteId.equalsIgnoreCase( "-1" ))
        {
            String[] favoriteAccess = favoriteId.split( ":" );
            if(favoriteAccess[1].equalsIgnoreCase( "Can View and Edit" ))
            {
                favorite = favoriteService.getFavorite( Integer.parseInt( favoriteAccess[0] ) );
            }
        }
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
        
        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        Period period = new Period();
        dataElementCategoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
        Constant off_pri_de_attribute = constantService.getConstantByName( "OFFICIAL/PRIVATE_DE_ATTRIBUTE_ID" );
        int off_pri_de_attribute_id = 1;
        if ( off_pri_de_attribute != null )
        {
            off_pri_de_attribute_id = (int) off_pri_de_attribute.getValue();
        }

        off_pri_de_map = new HashMap<DataElement, String>();

        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );

        
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_CONFLICT" ) )
            {
                conflictAuthority = "Yes";
            }
            else
            {
                conflictAuthority = "No";
            }  
        }

        if ( userDataElements == null || userDataElements.isEmpty() )
        {
            period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );

            selectedPeriod = period.getName();

            return SUCCESS;
        }
        
        List<DataElement> tempSelectedDEList = new ArrayList<DataElement>();
        for( int deId : dataElementsSelectedList )
        {
                DataElement de = dataElementService.getDataElement( deId );
                
                dataElementList.add( de );
                tempSelectedDEList.add( de );
        }        
        ActionContext.getContext().getSession().put("dataElementList", tempSelectedDEList );
        
        //dataElementList.retainAll( userDataElements );
        for ( DataElement de : dataElementList )
        {
            if ( de.getDataSets().size() != 0 )
            {
                Set<DataSet> tempDataSets = de.getDataSets();
                if( tempDataSets != null )
                {
                    for( DataSet dataSet : tempDataSets )
                    {
                        if( dataSet.getSources() != null && dataSet.getSources().size() > 0 )
                        {
                            dataSetMap.put( de.getUid(), dataSet.getDisplayName() );  
                            break;
                        }
                    }
                }
                else
                {
                    dataSetMap.put( de.getUid(), " ");
                }
                
            }
        }        
        
        PeriodType periodType = null;
        
        if( periodType == null )
        {
            periodType = new QuarterlyPeriodType();;
        }

        period = ivbUtil.getCurrentPeriod( periodType, new Date() );

        selectedPeriod = period.getName();

        currentPeriod = period.getDescription();

        
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
        
        for ( DataElement de : dataElementList )
        {
            for ( OrganisationUnit orgUnit : orgUnitList )
            {
                DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
            	Set<AttributeValue> attrValueSetforComment = new HashSet<AttributeValue>( de.getAttributeValues() );
            	
                if ( dv == null )
                {
                    dataValue.put( (orgUnit.getUid() + "-" + de.getUid()).trim(), "" );
                    dataComments.put( orgUnit.getUid() + "-" + de.getUid(), "" );
                    dataStoredBy.put( orgUnit.getUid() + "-" + de.getUid(), "" );
                    copyRightMap.put( orgUnit.getUid() + "-" + de.getUid(), "No" );
                    conflictMap.put( orgUnit.getUid() + "-" + de.getUid(), "No" );
                }
                else
                {

    				for ( AttributeValue attValue : attrValueSetforComment )
    				{
    				
    					if ( attValue.getAttribute().getId() == comment_attribute_id && attValue.getValue().equalsIgnoreCase( "true" ))
    						{
    							commentRequiredDe.add(de.getId());
    							System.out.println("Comment required custom data entry :"+ de.getId());
    						}
    					
    					if ( attValue.getAttribute().getId() == percentage_attribute_id && attValue.getValue().equalsIgnoreCase( "true" ))
						{
    						percentageRequiredDe.add(de.getId());
							System.out.println("Numeric Value required custom data entry :"+ de.getId());
						}
    				}
                	
                    dataValue.put( orgUnit.getUid() + "-" + de.getUid(), dv.getValue() );
                    dataComments.put( orgUnit.getUid() + "-" + de.getUid(), dv.getComment() );
                    dataStoredBy.put( orgUnit.getUid() + "-" + de.getUid(), dv.getStoredBy() + "(" + standardDateFormat.format(dv.getLastUpdated() ) + ")" );
                    
                    if( dv.getPeriod().getStartDate().getTime() <= period.getStartDate().getTime() )
                    {
                        copyRightMap.put( orgUnit.getUid() + "-" + de.getUid(), "Yes" );
                    }
                    else
                    {
                        copyRightMap.put( orgUnit.getUid() + "-" + de.getUid(), "No" );
                    }
                    
                    if ( dv.getFollowup() != null && dv.getFollowup() == true )
                    {
                        conflictMap.put( orgUnit.getUid() + "-" + de.getUid(), "Yes" );
                    }
                    else
                    {
                        conflictMap.put( orgUnit.getUid() + "-" + de.getUid(), "No" );                    
                    }
                }
            }

            /*
            if ( de.getOptionSet() != null )
            {
                List<String> optionsetList = de.getOptionSet().getOptions();
                optionSetMap.put( de.getOptionSet().getId() + "", optionsetList );
            }
            */
            Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
            Set<AttributeValue> dataElementAttributeValues = de.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if ( deAttributeValue.getAttribute().getId() == keyDEConst.getValue() &&  deAttributeValue.getValue().equalsIgnoreCase( "true" ))
                    {
                        dataElementList.remove( de );
                    }
                    if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Official" ) )
                    {
                        off_pri_de_map.put( de, "official" );
                    }
                    else if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id
                        && deAttributeValue.getValue() != null
                        && deAttributeValue.getValue().equalsIgnoreCase( "Private" ) )
                    {
                        off_pri_de_map.put( de, "private" );
                    }
                    else
                    {
                        off_pri_de_map.put( de, "other" );
                    }
                }
            }
            else
            {
                off_pri_de_map.put( de, "other" );
            }
        }
        userGrpList.addAll( userGroupService.getAllUserGroups() );
        
        //Collections.sort( dataElementList, new IdentifiableObjectNameComparator() );
        
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
        
        return SUCCESS;
    }
        
}
