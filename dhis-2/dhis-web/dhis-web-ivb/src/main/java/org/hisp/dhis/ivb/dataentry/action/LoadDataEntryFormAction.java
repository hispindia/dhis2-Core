package org.hisp.dhis.ivb.dataentry.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

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
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.dataset.comparator.SectionOrderComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.ivb.kfa.KeyFlagAnalytics;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.KeyFlagCalculation;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

public class LoadDataEntryFormAction
    implements Action
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";

    private final static String KEY_THRESHOLD = "Flag Threshold";

    private static String SOURCES = "SOURCES";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private LookupService lookupService;
    
    @Autowired
    private DataElementService dataElementService;
    
    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private SectionService sectionService;

    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
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

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
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

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private IVBUtil ivbUtil;

    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    private KeyFlagCalculation keyFlagCalculation;

    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private Boolean crumbsTrue = true;

    public Boolean getCrumbsTrue()
    {
        return crumbsTrue;
    }

    private String conflictAuthority = "";

    public String getConflictAuthority()
    {
        return conflictAuthority;
    }

    private String viewCommentAuthority = "";

    public String getViewCommentAuthority()
    {
        return viewCommentAuthority;
    }

    private String valueConflict = "";

    public String getValueConflict()
    {
        return valueConflict;
    }

    private String dataSetId;

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

    private List<DataSet> dataSetList;

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

    private Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    Map<String, Integer> dataValueStatusMap = new HashMap<String, Integer>();

    public Map<String, Integer> getDataValueStatusMap()
    {
        return dataValueStatusMap;
    }

    Map<String, String> dataValue = new HashMap<String, String>();

    Map<String, String> dataComments = new HashMap<String, String>();

    Map<String, String> dataStoredBy = new HashMap<String, String>();

    Set<Integer> commentRequiredDe = new HashSet<Integer>();

    public Set<Integer> getCommentRequiredDe()
    {
        return commentRequiredDe;
    }

    Set<Integer> percentageRequiredDe = new HashSet<Integer>();

    public Set<Integer> getPercentageRequiredDe()
    {
        return percentageRequiredDe;
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

    Map<String, String> copyRightMap = new HashMap<String, String>();

    public Map<String, String> getCopyRightMap()
    {
        return copyRightMap;
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

    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
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

    public String dataSetSectionName;

    public String getDataSetSectionName()
    {
        return dataSetSectionName;
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

    Set<DataElement> userDataElements = new HashSet<DataElement>();

    public Set<DataElement> getUserDataElements()
    {
        return userDataElements;
    }

    private Set<OrganisationUnit> userOrgUnit = new HashSet<OrganisationUnit>();

    public Set<OrganisationUnit> getUserOrgUnit()
    {
        return userOrgUnit;
    }

    private String dataSetSectionId;

    public void setDataSetSectionId( String dataSetSectionId )
    {
        this.dataSetSectionId = dataSetSectionId;
    }

    public String getDataSetSectionId()
    {
        return dataSetSectionId;
    }

    private String dataSetUId;

    public void setDataSetUId( String dataSetUId )
    {
        this.dataSetUId = dataSetUId;
    }

    public String getDataSetUId()
    {
        return dataSetUId;
    }

    private Map<String, List<Section>> dataSetSectionMap = new HashMap<String, List<Section>>();

    public Map<String, List<Section>> getDataSetSectionMap()
    {
        return dataSetSectionMap;
    }

    private String currentDataSet;

    public String getCurrentDataSet()
    {
        return currentDataSet;
    }

    private Set<DataElement> keyFlagDataElements = new HashSet<DataElement>();

    public Set<DataElement> getKeyFlagDataElements()
    {
        return keyFlagDataElements;
    }

    private DataElementCategoryOptionCombo optionCombo;

    public DataElementCategoryOptionCombo getOptionCombo()
    {
        return optionCombo;
    }

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private Map<String, String> userInfoMap = new HashMap<String, String>();

    public Map<String, String> getUserInfoMap()
    {
        return userInfoMap;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    private Map<String, String> colorMap = new HashMap<String, String>();

    public Map<String, String> getColorMap()
    {
        return colorMap;
    }

    private String countryName;

    public String getCountryName()
    {
        return countryName;
    }

    public void setCountryName( String countryName )
    {
        this.countryName = countryName;
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

    private String indicatorName;

    private String indValue;

    private String indComment = null;

    private String indSource;

    public String getIndicatorName()
    {
        return indicatorName;
    }

    public String getIndValue()
    {
        return indValue;
    }

    public void setIndValue( String indValue )
    {
        this.indValue = indValue;
    }

    public String getIndComment()
    {
        return indComment;
    }

    public String getIndSource()
    {
        return indSource;
    }

    private Indicator indicator;

    public Indicator getIndicator()
    {
        return indicator;
    }

    private Map<String, String> dataValuePeriodMap = new HashMap<String, String>();

    public Map<String, String> getDataValuePeriodMap()
    {
        return dataValuePeriodMap;
    }

    Map<String, String> discussionMap = new HashMap<String, String>();

    public Map<String, String> getDiscussionMap()
    {
        return discussionMap;
    }

    Map<String, String> technicalAMap = new HashMap<String, String>();

    public Map<String, String> getTechnicalAMap()
    {
        return technicalAMap;
    }
    
    private List<KeyFlagAnalytics> keyFlagIndicators = new ArrayList<KeyFlagAnalytics>();
    
    public List<KeyFlagAnalytics> getKeyFlagIndicators()
    {
        return keyFlagIndicators;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------
    public String execute()
    {

        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

        Lookup lookup = lookupService.getLookupByName( Lookup.COMMENT_REQUIRED_DATAELEMENT_ATTRIBUTE_ID );
        int attribute_id = Integer.parseInt( lookup.getValue() );

        Lookup lookup1 = lookupService.getLookupByName( Lookup.IS_PERCENTAGE );
        int percentage_attribute_id = Integer.parseInt( lookup1.getValue() );
        
        lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        Set<DataElement> restrictedDeList = ivbUtil.getRestrictedDataElements( restrictedDeAttributeId );

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
        Collections.sort( countryList );

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

        List<OrganisationUnit> organUnit = new ArrayList<OrganisationUnit>( currentUserService.getCurrentUser()
            .getOrganisationUnits() );

        for ( OrganisationUnit orgU : organUnit )
        {
            userOrgUnit.addAll( ivbUtil.getLeafOrganisationUnits( orgU.getId() ) );
        }

        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials()
            .getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_CONFLICT" ) )
            {
                conflictAuthority = "Yes";
            }
            else if( !conflictAuthority.equalsIgnoreCase( "Yes" ) )
            {
                conflictAuthority = "No";
            }
            
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_VIEW_COMMENT" ) )
            {
                viewCommentAuthority = "Yes";
            }
            else if( !viewCommentAuthority.equalsIgnoreCase( "Yes" ) )
            {
                viewCommentAuthority = "No";
            }
        }

        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        statusMessage = "NONE";

        Set<DataSet> dataSets = new HashSet<DataSet>( dataSetService.getAllDataSets() );
        List<Section> allSectionList = new ArrayList<Section>( sectionService.getAllSections() );
        for ( DataSet dataSet : dataSets )
        {
            if ( dataSet.getSections() != null && dataSet.getSections().size() > 0 )
            {
                List<Section> sectionList = new ArrayList<Section>();
                sectionList.add( (new ArrayList<Section>( dataSet.getSections() )).get( 0 ) );
                Collections.sort( sectionList, new SectionOrderComparator() );
                sectionList.retainAll( allSectionList );
                dataSetSectionMap.put( dataSet.getUid(), sectionList );
            }
        }
        if ( countryName != null )
        {
            List<OrganisationUnit> orgUnits = new ArrayList<OrganisationUnit>(
                organisationUnitService.getOrganisationUnitByShortName( countryName ) );
            organisationUnit = orgUnits.get( 0 );
            orgUnitUid = organisationUnit.getUid();
            ActionContext.getContext().getSession().put( "CountryId", organisationUnit.getId() );
        }
        else if ( ActionContext.getContext().getSession().get( "CountryId" ) == null
            && (orgUnitUid == null || orgUnitUid.trim().equals( "" )) )
        {
            statusMessage = "Please select organisationunit";

            return SUCCESS;
        }
        else if ( ActionContext.getContext().getSession().get( "CountryId" ) != null
            && (orgUnitUid == null || orgUnitUid.trim().equals( "" )) )
        {
            String orgUnitId = ActionContext.getContext().getSession().get( "CountryId" ).toString();

            organisationUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitId ) );

            orgUnitUid = organisationUnit.getUid();
        }

        List<String> orgUnitUids = new ArrayList<String>();
        orgUnitUids.add( orgUnitUid );

        List<OrganisationUnit> orgUnit = organisationUnitService.getOrganisationUnitsByUid( orgUnitUids );

        organisationUnit = organisationUnitService.getOrganisationUnit( orgUnit.get( 0 ).getId() );

        Map<String, List<Section>> sectionMap = ivbUtil.getSectionsByOrganisationUnit( organisationUnit.getId() );

        for ( String key : sectionMap.keySet() )
        {
            // System.out.println("sList.size()1 " +
            // (sectionMap.get(key)).size());
            if ( (sectionMap.get( key )).size() > 1 )
            {
                List<Section> sList = new ArrayList<Section>( sectionMap.get( key ) );

                Collections.sort( sList, new SectionOrderComparator() );
                // System.out.println("sList.size()2 " +
                // (sectionMap.get(key)).size());
                dataSetSectionMap.put( key, sList );
            }
        }

        if ( dataSetUId == null && dataSetSectionId == null
            && ActionContext.getContext().getSession().get( "CURRENT_DATASET_SECTION_ID" ) != null )
        {
            dataSetSectionId = ActionContext.getContext().getSession().get( "CURRENT_DATASET_SECTION_ID" ).toString();
        }
        else if ( dataSetUId == null && dataSetSectionId == null
            && ActionContext.getContext().getSession().get( "CURRENT_DATASET_UID" ) != null )
        {
            dataSetUId = ActionContext.getContext().getSession().get( "CURRENT_DATASET_UID" ).toString();
        }
        else if ( dataSetUId == null && dataSetSectionId == null )
        {
            statusMessage = "Please select key indicator";

            return SUCCESS;
        }

        optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        Period period = new Period();

        DataSet selDataSet = null;
        Section selDataSetSection = null;

        if ( dataSetSectionId != null )
        {
            selDataSetSection = sectionService.getSection( Integer.parseInt( dataSetSectionId ) );

            if ( dataSetSectionId == null || selDataSetSection == null )
            {
                statusMessage = "Please select key indicator";

                return SUCCESS;
            }
            
            if( !selDataSetSection.getSources().contains( organisationUnit ) )
            {
            	statusMessage = "Please select key indicator";

                return SUCCESS;
            }

            selDataSet = selDataSetSection.getDataSet();

            dataElementList = new ArrayList<DataElement>( selDataSetSection.getDataElements() );
            
            List<DataElement> hiddenDeList = ivbUtil.getHiddenDataElementList( orgUnitUid );
            
            dataElementList.removeAll( hiddenDeList );
            
            restrictedDeList.removeAll( userDataElements );
            dataElementList.removeAll( restrictedDeList );
            

            currentDataSet = selDataSetSection.getId() + "";

            dataSetSectionName = selDataSetSection.getName();
        }
        else if ( dataSetUId != null )
        {
            List<String> datasetList = new ArrayList<String>();

            datasetList.add( dataSetUId );

            selDataSet = dataSetService.getDataSetsByUid( datasetList ).get( 0 );
            currentDataSet = selDataSet.getUid();
            List<DataElement> tempDEList = new ArrayList<DataElement>();
            List<Section> sectionList = new ArrayList<Section>( selDataSet.getSections() );
            Collections.sort( sectionList, new SectionOrderComparator() );
            for ( Section section : sectionList )
            {
                if ( sectionMap.get( currentDataSet ).contains( section ) )
                {
                    tempDEList.addAll( section.getDataElements() );
                }
            }

            dataElementList.addAll( tempDEList );
            
            List<DataElement> hiddenDeList = ivbUtil.getHiddenDataElementList( orgUnitUid );
            
            dataElementList.removeAll( hiddenDeList );
            restrictedDeList.removeAll( userDataElements );
            dataElementList.removeAll( restrictedDeList );
        }
        else
        {
            statusMessage = "Please select key indicator";

            return SUCCESS;
        }

        if ( indicator != null )
        {
            indicatorName = indicator.getName();
        }
        else
        {
            indicatorName = "";
        }
        
        periodT = selDataSet.getPeriodType();

        PeriodType periodType = selDataSet.getPeriodType();

        dataSetName = selDataSet.getName();

        period = ivbUtil.getCurrentPeriod( periodType, new Date() );

        selectedPeriod = period.getName();

        currentPeriod = period.getIsoDate();
        
        Set<DataElement> allDes = new HashSet<DataElement>( dataElementService.getAllDataElements() );
        dataElementList.retainAll( allDes );

        i18n( i18nService, dataElementList );

        for ( DataElement de : dataElementList )
        {

            DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit.get( 0 ) );

            Set<AttributeValue> attrValueSetforComment = new HashSet<AttributeValue>( de.getAttributeValues() );

            // System.out.println(" period id: "+period+"");

            if ( dv == null )
            {
                dataValue.put( (orgUnit.get( 0 ).getUid() + "-" + de.getUid()).trim(), "" );
                dataComments.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "" );
                dataStoredBy.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "" );
                conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                dataValueStatusMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), 1 );
                technicalAMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                discussionMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
            }
            else
            {

                for ( AttributeValue attValue : attrValueSetforComment )
                {

                    if ( attValue.getAttribute().getId() == attribute_id
                        && attValue.getValue().equalsIgnoreCase( "true" ) )
                    {
                        commentRequiredDe.add( de.getId() );
                        //System.out.println( "Comment required De :" + de.getId() );
                    }

                    if ( attValue.getAttribute().getId() == percentage_attribute_id
                        && attValue.getValue().equalsIgnoreCase( "true" ) )
                    {
                        percentageRequiredDe.add( de.getId() );
                        //System.out.println( "Percentage required De :" + de.getId() );
                    }
                }

                Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> technicalAssistanceAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                    ivbUtil.getDataValueAuditMap( de.getId() + "", orgUnit.get( 0 ).getId() + "", period.getId(),
                        DataValueAudit.DVA_CT_TA ) );
                Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> discussionAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>(
                    ivbUtil.getDataValueAuditMap( de.getId() + "", orgUnit.get( 0 ).getId() + "", period.getId(),
                        DataValueAudit.DVA_CT_DISCUSSION ) );

                //System.out.println(technicalAssistanceAuditMap.size()+"");

                if ( technicalAssistanceAuditMap.size() > 0 )
                {
                    String techComment = "No";
                    for ( DataValueAudit dva : technicalAssistanceAuditMap.get( orgUnit.get( 0 ) ).get( de ) )
                    {
                    	//System.out.println( " D dva -- 1 " + dva);
                    	
                    	if( dva.getComment() != null )
                    	{
                    		//System.out.println( " D dva -- 2 " + dva);
                    		if ( !dva.getComment().isEmpty() && dva.getComment() != "" )
                            {
                                techComment = "Yes";
                            }
                    	}
                    	
                    }
                    technicalAMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), techComment );
                }
                else
                {
                    technicalAMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                }
                if ( discussionAuditMap.size() > 0 )
                {
                    discussionMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "Yes" );
                }
                else
                {
                    discussionMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                }

                String userInfo = "";
                Constant sourecs = constantService.getConstantByName( SOURCES );
                dataValue.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getValue() );

                // System.out.println("-------" + dv.getValue());
                dataComments.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getComment() );

                /*
                 * if(dataElementsCommentRequired.get(de) && dv.getComment() !=
                 * null){ dataComments.put( orgUnit.get( 0 ).getUid() + "-" +
                 * de.getUid(), dv.getComment() ); } else{ dataComments.put(
                 * orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "" ); }
                 */

                dataStoredBy.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getStoredBy() + " ("
                    + standardDateFormat.format( dv.getLastUpdated() ) + ")" );
                dataValueStatusMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dv.getStatus() );

                String dvPeriod = "";
                // SimpleDateFormat standardDateFormat = new
                // SimpleDateFormat("yyyy-MM-dd");
                String dvPeriodStartDate = standardDateFormat.format( dv.getPeriod().getStartDate() );
                int tempMonth = Integer.parseInt( dvPeriodStartDate.split( "-" )[1] );
                if ( tempMonth >= 1 && tempMonth <= 3 )
                {
                    dvPeriod = dvPeriodStartDate.split( "-" )[0] + " Q1";
                }
                else if ( tempMonth >= 4 && tempMonth <= 6 )
                {
                    dvPeriod = dvPeriodStartDate.split( "-" )[0] + " Q2";
                }
                else if ( tempMonth >= 7 && tempMonth <= 9 )
                {
                    dvPeriod = dvPeriodStartDate.split( "-" )[0] + " Q3";
                }
                else if ( tempMonth >= 10 && tempMonth <= 12 )
                {
                    dvPeriod = dvPeriodStartDate.split( "-" )[0] + " Q4";
                }

                if ( dv.getPeriod().getStartDate().getTime() <= period.getStartDate().getTime() )
                {
                    copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "Yes" );
                }
                else
                {
                    copyRightMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                }
                if ( dv.getFollowup() != null && dv.getFollowup() == true )
                {
                    conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "Yes" );
                }
                else
                {
                    conflictMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), "No" );
                }
                String constantValue = "";
                UserCredentials userCredentials = userService.getUserCredentialsByUsername( dv.getStoredBy() );
                if ( userCredentials != null )
                {
                    User user = userService.getUserCredentialsByUsername( dv.getStoredBy() ).getUser();
                    if ( user != null )
                    {
                        Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( user.getAttributeValues() );
                        for ( AttributeValue attValue : attrValueSet )
                        {
                            if ( attValue.getAttribute().getId() == sourecs.getValue() )
                            {
                                constantValue = attValue.getValue();
                            }
                        }
                        userInfo = "Full Name: " + user.getName() + "<br />Organisation: " + constantValue
                            + "<br />Period: " + dvPeriod;
                    }
                }
                else
                {
                    userInfo = "Full Name: " + dv.getStoredBy() + " (REMOVED)<br />Period: " + dvPeriod;
                }

                userInfoMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), userInfo );
                dataValuePeriodMap.put( orgUnit.get( 0 ).getUid() + "-" + de.getUid(), dvPeriod );
            }
        }

        Constant off_pri_de_attribute = constantService.getConstantByName( "OFFICIAL/PRIVATE_DE_ATTRIBUTE_ID" );
        int off_pri_de_attribute_id = 1;
        if ( off_pri_de_attribute != null )
        {
            off_pri_de_attribute_id = (int) off_pri_de_attribute.getValue();
        }

        off_pri_de_map = new HashMap<DataElement, String>();
        for ( DataElement dataelemnt : dataElementList )
        {
        	/*
            if ( dataelemnt.getOptionSet() != null )
            {
                List<String> optionsetList = dataelemnt.getOptionSet().getOptions();
                optionSetMap.put( dataelemnt.getOptionSet().getId() + "", optionsetList );
            }
            */

            Set<AttributeValue> dataElementAttributeValues = dataelemnt.getAttributeValues();

            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id && deAttributeValue.getValue() != null )
                    {
                        if( deAttributeValue.getValue().trim().equalsIgnoreCase( "Official" ) )
                        {
                            off_pri_de_map.put( dataelemnt, "official" );
                        }
                        else if( deAttributeValue.getValue().trim().equalsIgnoreCase( "Private" ) )
                        {
                            off_pri_de_map.put( dataelemnt, "private" );
                        }
                        else
                        {
                            off_pri_de_map.put( dataelemnt, "other" );
                        }
                    }
                }
            }
            else
            {
                off_pri_de_map.put( dataelemnt, "other" );
            }
        }

        ActionContext.getContext().getSession().put( "OrgUnitId", orgUnit.get( 0 ).getParent().getId() );

        if ( dataSetSectionId != null )
        {
            ActionContext.getContext().getSession().put( "CURRENT_DATASET_SECTION_ID", selDataSetSection.getId() );

            ActionContext.getContext().getSession().put( "CURRENT_DATASET_UID", null );
        }
        else if ( dataSetUId != null )
        {
            ActionContext.getContext().getSession().put( "CURRENT_DATASET_SECTION_ID", null );

            ActionContext.getContext().getSession().put( "CURRENT_DATASET_UID", selDataSet.getUid() );
        }

        keyFlagDataElements.addAll( getKeyFlagDataElements( selDataSet ) );

        ActionContext.getContext().getSession().put( "CountryId", organisationUnit.getId() );
        
        return SUCCESS;
    }

    Set<DataElement> getKeyFlagDataElements( DataSet dataSet )
    {
        Set<DataElement> keyFlagDataElements = new HashSet<DataElement>();

        List<Indicator> indicaList = new ArrayList<Indicator>( dataSet.getIndicators() );

        int y=0;
        
        
        for ( Indicator indicator : indicaList )
        {
            
        	String thresoldValue = null;
            
            KeyFlagAnalytics keyFlagInd = new KeyFlagAnalytics();
            keyFlagInd.setIndicator( indicator );
            
            List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
            for ( AttributeValue attributeValue : attributeValueList )
            {
                if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_THRESHOLD ) )
                {
                    thresoldValue = attributeValue.getValue();
                }
            }

            String keyIndicatorValue = "";
            String exString = indicator.getNumerator();
            
            if(exString.contains(KeyFlagCalculation.NESTED_OPERATOR_AND) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
            { 
                keyIndicatorValue = keyFlagCalculation.getNestedKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), organisationUnit ); 
            }
            else
            {
                keyIndicatorValue = keyFlagCalculation.getKeyIndicatorValueWithThresoldValue( indicator.getNumerator(), indicator.getUid(), organisationUnit ); 
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
                
                if ( keyIndicatorValue.endsWith( ".0" ) )
                {
                	keyIndicatorValue = keyIndicatorValue.replace( ".0", "" );
                }
            } 
            catch (Exception e) 
            {}

            String mapKey =  indicator.getUid() + "-" + organisationUnit.getUid();
            String keyFlagColor = keyFlagCalculation.getColorMap().get( mapKey );
            
            if( KeyFlagCalculation.KEYFLAG_GREEN.equalsIgnoreCase( keyFlagColor ) )
            {
            	//System.out.println( indicator.getName() + " : " + keyFlagColor + " : #BCF5A9" );
            	keyFlagColor = "#BCF5A9";
            }
            else if( KeyFlagCalculation.KEYFLAG_RED.equalsIgnoreCase( keyFlagColor ) )
            {
            	//System.out.println( indicator.getName() + " : " + keyFlagColor + " : #FF6969" );
            	keyFlagColor = "#FF6969";
            }
            else if( KeyFlagCalculation.KEYFLAG_GREY.equalsIgnoreCase( keyFlagColor ) )
            {
            	//System.out.println( indicator.getName() + " : " + keyFlagColor + " : #F3F781" );
            	keyFlagColor = "#F3F781";
            }
            	
            indValue = keyIndicatorValue;
            keyFlagInd.setKeyFlagValue( indValue );
            
            //keyFlagDataElements.addAll( expressionService.getDataElementsInExpression( indicator.getNumerator() ) );

            Set<DataElement> indicatorDEs = new HashSet<DataElement>( expressionService.getDataElementsInExpression( indicator.getNumerator() ) );
            
            if( indicatorDEs != null && indicatorDEs.size() > 0 )
            {
            	keyFlagDataElements.addAll( indicatorDEs );
            }
            
            String comment = null;
            String source = null;
            Date tempDate = null;
            SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
            
            for ( DataElement de : indicatorDEs )
            {
                DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                if ( dv != null )
                {

                	if ( tempDate == null || tempDate.before( dv.getLastUpdated() ) )
                    {
                        tempDate = dv.getLastUpdated();
                    	source = dv.getStoredBy() + "<br/>(";
                        
                        try
                        {
                            source +=  standardDateFormat.format( dv.getLastUpdated() ) + ")";
                        }
                        catch( Exception e )
                        {
                            source +=  dv.getLastUpdated() + ")";
                        }
                    }
                    //source = dv.getStoredBy() + " (" + dv.getLastUpdated() + ")";
                    if ( comment == null && dv.getComment() != null && dv.getComment() != "" )
                    {
                        comment = dv.getComment();
                    }
                    else
                    {
                        if ( dv.getComment() != "" && dv.getComment() != null )
                        {
                            comment = comment + "; " + dv.getComment();
                        }
                    }

                }
                valueMap = keyFlagCalculation.getValueMap();
                
                /*
                if ( thresoldValue.toLowerCase().equalsIgnoreCase(
                    valueMap.get( indicator.getUid() + "-" + organisationUnit.getUid() ).toLowerCase() ) )
                {
                    colorMap.put( de.getUid(), "#FF6969" );
                }
                else if ( dataValue.get( organisationUnit.getUid() + "-" + de.getUid() ) == null
                    || (dataValue.get( organisationUnit.getUid() + "-" + de.getUid() ).trim() == "" && dataComments
                        .get( organisationUnit.getUid() + "-" + de.getUid() ).trim() == "") )
                {
                    colorMap.put( de.getUid(), "#F3F781" );
                }
                else
                {
                    colorMap.put( de.getUid(), "#BCF5A9" );
                }
                */
                colorMap.put( de.getUid(), keyFlagColor );
            }
            
            indComment = comment;
            indSource = source;
            keyFlagInd.setComment( indComment );
            keyFlagInd.setSource( indSource );
            
            //System.out.println( "1."+ indicator.getName() + " --  " + indComment + " ---- " + indSource );
            
            Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
            List<DataSet> dataSets = new ArrayList<DataSet>( indicator.getDataSets() );
            List<DataElement> dataElements = new ArrayList<DataElement>( dataSets.get( 0 ).getDataElements() );
            for ( DataElement de : dataElements )
            {
                List<AttributeValue> deAttributeValues = new ArrayList<AttributeValue>( de.getAttributeValues() );
                for ( AttributeValue da : deAttributeValues )
                    if ( da.getAttribute().getId() == keyDEConst.getValue() && da.getValue().equalsIgnoreCase( "true" ) )
                    {
                        dataElementList.remove( de );
                        DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                        if ( dv == null )
                        {
                            indComment = comment;
                            indSource = source;
                            keyFlagInd.setComment( indComment );
                            keyFlagInd.setSource( indSource );
                            //System.out.println( "2."+ indicator.getName() + " --  " +indComment + " ---- " + indSource );
                        }
                        else
                        {
                            indComment = dv.getValue();
                            indSource = dv.getStoredBy() + " (" + dv.getLastUpdated() + ")";
                            keyFlagInd.setComment( indComment );
                            keyFlagInd.setSource( indSource );
                            //System.out.println( "3."+ indicator.getName() + " --  " + indComment + " ---- " + indSource );
                        }
                    }

            }
            
            keyFlagIndicators.add( keyFlagInd );
        }
        
        return keyFlagDataElements;
    }

}
