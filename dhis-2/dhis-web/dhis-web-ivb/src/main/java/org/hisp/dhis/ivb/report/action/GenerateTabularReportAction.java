package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;
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
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta Bajpai
 */
public class GenerateTabularReportAction
    implements Action
{
    private static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
   
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private FavoriteService favoriteService;

    public void setFavoriteService( FavoriteService favoriteService )
    {
        this.favoriteService = favoriteService;
    }

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    @Autowired 
    private LookupService lookupService;

    // -------------------------------------------------------------------------
    // Getters / Setters
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
    

    private String userSource;

    public String getUserSource()
    {
        return userSource;
    }

    public void setUserSource( String userSource )
    {
        this.userSource = userSource;
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

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private List<Integer> selectedDataElementsValidator = new ArrayList<Integer>();

    public void setSelectedDataElementsValidator( List<Integer> selectedDataElementsValidator )
    {
        this.selectedDataElementsValidator = selectedDataElementsValidator;
    }

    public List<Integer> getSelectedDataElementsValidator()
    {
        return selectedDataElementsValidator;
    }

    private List<Boolean> values = new ArrayList<Boolean>();

    public void setValues( List<Boolean> values )
    {
        this.values = values;
    }

    public List<Boolean> getValues()
    {
        return values;
    }

    private List<Boolean> comments = new ArrayList<Boolean>();

    public void setComments( List<Boolean> comments )
    {
        this.comments = comments;
    }

    public List<Boolean> getComments()
    {
        return comments;
    }

    private List<Integer> headerDataElements = new ArrayList<Integer>();

    public List<Integer> getHeaderDataElements()
    {
        return headerDataElements;
    }

    private List<String> orgUnitIds = new ArrayList<String>();

    public void setOrgUnitIds( List<String> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
    }

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    private Map<String, DataValue> headerDataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getHeaderDataValueMap()
    {
        return headerDataValueMap;
    }

    private List<String> dataElementName = new ArrayList<String>();

    public List<String> getDataElementName()
    {
        return dataElementName;
    }

    private List<String> headerDataElementName = new ArrayList<String>();

    public List<String> getHeaderDataElementName()
    {
        return headerDataElementName;
    }

    private List<Integer> valueDeList = new ArrayList<Integer>();

    public List<Integer> getValueDeList()
    {
        return valueDeList;
    }

    private List<Integer> commentDeList = new ArrayList<Integer>();

    public List<Integer> getCommentDeList()
    {
        return commentDeList;
    }

    private Map<Integer, String> valueDeMap = new HashMap<Integer, String>();

    public Map<Integer, String> getValueDeMap()
    {
        return valueDeMap;
    }

    private Map<Integer, String> commentDeMap = new HashMap<Integer, String>();

    public Map<Integer, String> getCommentDeMap()
    {
        return commentDeMap;
    }

    private String favoriteType;

    public String getFavoriteType()
    {
        return favoriteType;
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

    private OrganisationUnitGroupSet unicefRegionsGroupSet;
    
    public OrganisationUnitGroupSet getUnicefRegionsGroupSet()
    {
        return unicefRegionsGroupSet;
    }
    
    private Map<Integer, List<String>> dataElement_DatasetMap = new HashMap<Integer, List<String>>();
    
    public Map<Integer, List<String>> getDataElement_DatasetMap() 
    {
	return dataElement_DatasetMap;
    }
    
    private Map<Integer, String> typeMap = new HashMap<Integer, String>();
    
    public Map<Integer, String> getTypeMap() {
		return typeMap;
	}

	Set<Integer> percentageRequiredDe = new HashSet<Integer>();
	
	public Set<Integer> getPercentageRequiredDe() 
	{
		return percentageRequiredDe;
	}

	private Map<Integer, Set<Integer>> hiddenDes;
	
	public Map<Integer, Set<Integer>> getHiddenDes() 
	{
		return hiddenDes;
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
    	
    	//System.out.println("1: " + new Date() );
    	Lookup lookup1 = lookupService.getLookupByName(Lookup.IS_PERCENTAGE);
    	int percentage_attribute_id = Integer.parseInt(lookup1.getValue());
		
        List<Favorite> favorites = favoriteService.getAllFavoriteByFavoriteType( IVBUtil.TABULAR_REPORT );
        for ( Favorite f : favorites )
        {
            favoriteList.add( "\"" + f.getName() + "\"" );
        }
        favoriteType = IVBUtil.TABULAR_REPORT;
        // System.out.println( favoriteId );
        if ( favoriteId != null && !favoriteId.equalsIgnoreCase( "-1" ) )
        {
            String[] favoriteAccess = favoriteId.split( ":" );
            if ( favoriteAccess[1].equalsIgnoreCase( "Can View and Edit" ) )
            {
                favorite = favoriteService.getFavorite( Integer.parseInt( favoriteAccess[0] ) );
            }
        }

        ActionContext.getContext().getSession().put("favoriteId", favoriteId );
        Constant tabularDataElementGroupId = constantService.getConstantByName( TABULAR_REPORT_DATAELEMENTGROUP_ID );
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

        if ( orgUnitIds.size() > 1 )
        {
            for ( String id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( id ) );
                
                if ( orgUnit.getHierarchyLevel() == 3 )
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
                if ( orgUnit.getHierarchyLevel() == 3 )
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
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );

        String orgUnitIdsByComma = "-1";
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }
        
        //System.out.println("2: " + new Date() );

        for ( int i = 0; i < this.selectedDataElementsValidator.size(); i++ )
        {
            DataElement dataElement = dataElementService.getDataElement( selectedDataElementsValidator.get( i ) );
            Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( dataElement.getAttributeValues() );
        	
            //System.out.println("Dataelement name :" + dataElement.getName());
            
            for ( AttributeValue attValue : attrValueSet )
			{
				if ( attValue.getAttribute().getId() == percentage_attribute_id && attValue.getValue().equalsIgnoreCase( "true" ))
						percentageRequiredDe.add(dataElement.getId());
			}
            
            typeMap.put(dataElement.getId(), dataElement.getValueType().name() );
            
            if ( values.get( i ) == true )
            {
                dataElementName.add( dataElement.getName() );
                valueDeList.add( dataElement.getId() );
                valueDeMap.put( dataElement.getId(), "true" );
            }
            if ( comments.get( i ) == true )
            {
                dataElementName.add( "Comment of " + dataElement.getName() );
                commentDeList.add( dataElement.getId() );
                commentDeMap.put( dataElement.getId(), "true" );
            }
            
            DataSet ds = dataElement.getDataSet();
            
            if( ds != null )
            {
                //System.out.println( ds.getName() );
                List<String> tempL = new ArrayList<String>();
                if( ds.getSections().size() <= 1 )
                {
                    tempL.add( "DS" );
                    tempL.add( ds.getUid() );
                    dataElement_DatasetMap.put( dataElement.getId(), tempL );
                }
                else
                {
                    for( Section dss : ds.getSections() )
                    {
                        if( dss.getDataElements().contains( dataElement ) )
                        {
                            tempL.add( "DSS" );
                            tempL.add( ""+dss.getId() );
                            dataElement_DatasetMap.put( dataElement.getId(), tempL );
                            break;
                        }
                    }
                }
            }
            else
            {
                System.out.println( "dataset is null for de "+ dataElement.getName() );
            }
        }

        List<DataElement> dataElements = new ArrayList<DataElement>( dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );
    	
        for ( DataElement de : dataElements )
        {
            headerDataElementName.add( de.getName() );
        }

        headerDataElements = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";

        if ( headerDataElements.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElements );
        }

        //System.out.println("3: " + new Date() );
        
        headerDataValueMap = ivbUtil.getLatestDataValuesForTabularReport( headerDataElementIdsByComma, orgUnitIdsByComma );
        // dataValueMap.putAll( ivbUtil.getLatestDataValuesForTabularReport(
        // headerDataElementIdsByComma, orgUnitIdsByComma ) );

        //System.out.println("4: " + new Date() );
        
        String dataElementIdsByComma = "-1";

        if ( selectedDataElementsValidator.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( selectedDataElementsValidator );
        }
        
        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        
        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        //System.out.println("5: " + new Date() );
        
        ActionContext.getContext().getSession().put("tabulardataElementList", selectedDataElementsValidator );
        ActionContext.getContext().getSession().put("values", values );
        ActionContext.getContext().getSession().put("comments", comments );
        
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );
        
        hiddenDes = new HashMap<Integer, Set<Integer>>( ivbUtil.getHiddenDataElementByCountry() );
        
        //System.out.println("6: " + new Date() );

        return SUCCESS;

    }

	

}
