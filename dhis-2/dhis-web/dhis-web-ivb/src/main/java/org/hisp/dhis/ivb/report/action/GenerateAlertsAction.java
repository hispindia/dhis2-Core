package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
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

/**
 * @author BHARATH
 */
public class GenerateAlertsAction
    implements Action
{

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

    @Autowired
    private DataSetService dataSetService;
    
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

    public void setIsoCode( String isoCode )
    {
        this.isoCode = isoCode;
    }

    public String getWhoRegion()
    {
        return whoRegion;
    }

    public void setWhoRegion( String whoRegion )
    {
        this.whoRegion = whoRegion;
    }

    public String getUnicefRegion()
    {
        return unicefRegion;
    }

    public void setUnicefRegion( String unicefRegion )
    {
        this.unicefRegion = unicefRegion;
    }

    public String getIncomeLevel()
    {
        return incomeLevel;
    }

    public void setIncomeLevel( String incomeLevel )
    {
        this.incomeLevel = incomeLevel;
    }

    public String getGaviEligibleStatus()
    {
        return gaviEligibleStatus;
    }

    public void setGaviEligibleStatus( String gaviEligibleStatus )
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

    public Map<Integer, String> getTypeMap()
    {
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

    private List<DataElement> dataElementList;
    
    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }

    private List<Integer> selectedListDataset = new ArrayList<Integer>();

    public void setSelectedListDataset( List<Integer> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    private Map<String, String> alertResultMap = new HashMap<String, String>();
    
    public Map<String, String> getAlertResultMap()
    {
        return alertResultMap;
    }

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
    public String execute()
    {
        if ( isoCode != null )
        {
            isoCode = "ON";
        }
        if ( whoRegion != null )
        {
            whoRegion = "ON";
        }
        if ( unicefRegion != null )
        {
            unicefRegion = "ON";
        }
        if ( incomeLevel != null )
        {
            incomeLevel = "ON";
        }
        if ( gaviEligibleStatus != null )
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

        orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );
        List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
        List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser().getOrganisationUnits() );
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
        Collections.sort(orgUnitList, new IdentifiableObjectNameComparator() );
        
        Collection<Integer> organisationUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
        String orgUnitIdsByComma = "-1";
        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( organisationUnitIds );
        }

        Lookup lookup1 = lookupService.getLookupByName( Lookup.EXPRESSION_FOR_ALERT_ATTR_ID );
        int alert_exp_attribute_id = Integer.parseInt(lookup1.getValue());
        
        //dataElementList = new ArrayList<DataElement>( dataElementService.getAllDataElements() );
        dataElementList = new ArrayList<DataElement>();
        for ( Integer dataSetId : selectedListDataset )
        {
        	DataSet dataSet = dataSetService.getDataSet( dataSetId );
            dataElementList.addAll( dataSet.getDataElements() );
        }
        
        Map<DataElement, String> de_alertExp_map = new HashMap<DataElement, String>();
        Iterator<DataElement> iterator = dataElementList.iterator();
        while( iterator.hasNext() )
        {
            DataElement de = iterator.next();
            String alertExpression = "";
            Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( de.getAttributeValues() );
            for ( AttributeValue attValue : attrValueSet )
            {
                if ( attValue.getAttribute().getId() == alert_exp_attribute_id )
                {
                    alertExpression = attValue.getValue();
                    break;
                }
            }

            if( alertExpression == null || alertExpression.trim().equals( "" ) )
            {
                iterator.remove();
            }
            else
            {
                de_alertExp_map.put( de, alertExpression );
            }
        }

       //System.out.println( orgUnitList.size() + " ------ " + dataElementList.size() );
       
        for( OrganisationUnit orgUnit : orgUnitList )
        {
            for( DataElement de : dataElementList )
            {
                String alertExpResult = ivbUtil.findAlertValueFromExpression( de_alertExp_map.get( de ), de, orgUnit );
                if( alertExpResult != null && !alertExpResult.equals( "" ) )
                {
                    alertResultMap.put( orgUnit.getId()+":"+de.getId(), alertExpResult );
                }
            }
        }

        return SUCCESS;

    }

}
