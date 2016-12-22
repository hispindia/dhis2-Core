package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.i18n.I18nUtils.i18n;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.comparator.SectionOrderComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.ivb.util.KeyFlagCalculation;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Samta bajpai
 */
public class GenerateHTMLReportAction
    implements Action
{
    private final static String KEY_DATAELEMENT = "KEYFLAG_DE_ATTRIBUTE_ID";
    private final static String SHOW_ALL_DES = "SHOW_ALL_DES";

    private final static String SHOW_DATA_DES = "SHOW_DATA_DES";

    private final static String SHOW_BLANK_DES = "SHOW_BLANK_DES";

    private final static String SHOW_KEY_FLAG_ONLY = "SHOW_KEY_FLAG_ONLY";

    private final static String KEY_FLAG = "Key Flag";

    private final static String KEY_THRESHOLD = "Flag Threshold";

    private final static String DE_GROUP = "Dashboard Header";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private LookupService lookupService;
        
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
    private List<DataElement> dataElementList = new ArrayList<DataElement>();
    public List<DataElement> getDataElementList()
    {
        return dataElementList;
    }
    private List<DataElement> dataElementList1 = new ArrayList<DataElement>();
 
    public List<DataElement> getDataElementList1()
    {
        return dataElementList1;
    }

    private String conflictAuthority = "";
    public String getConflictAuthority()
    {
        return conflictAuthority;
    }

    private OrganisationUnit organisationUnit1;
    
    public OrganisationUnit getOrganisationUnit1()
    {
        return organisationUnit1;
    }

    private String viewCommentAuthority = "";
    public String getViewCommentAuthority()
    {
        return viewCommentAuthority;
    }   
    private String dataSetSectionId;

    public void setDataSetSectionId( String dataSetSectionId )
    {
        this.dataSetSectionId = dataSetSectionId;
    }

    Set<DataElement> userDataElements = new HashSet<DataElement>();
    public Set<DataElement> getUserDataElements()
    {
        return userDataElements;
    }

    public void setUserDataElements( Set<DataElement> userDataElements )
    {
        this.userDataElements = userDataElements;
    }
    
    private Set<OrganisationUnit> userOrgUnit = new HashSet<OrganisationUnit>();

    public Set<OrganisationUnit> getUserOrgUnit()
    {
        return userOrgUnit;
    }

    public void setUserOrgUnit( Set<OrganisationUnit> userOrgUnit )
    {
        this.userOrgUnit = userOrgUnit;
    }

    @Autowired
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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

    private Map<String, List<Section>> dataSetSectionMap = new HashMap<String, List<Section>>();
    public Map<String, List<Section>> getDataSetSectionMap()
    {
        return dataSetSectionMap;
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

    @Autowired
    private SelectionTreeManager selectionTreeManager;
    
    @Autowired
    private IVBUtil ivbUtil;

    private KeyFlagCalculation keyFlagCalculation;

    public void setKeyFlagCalculation( KeyFlagCalculation keyFlagCalculation )
    {
        this.keyFlagCalculation = keyFlagCalculation;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String dataSetId;

    public void setDataSetId( String dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    public String getDataSetId()
    {
        return dataSetId;
    }

    

    private Map<DataElement, String> off_pri_de_map;

    public Map<DataElement, String> getOff_pri_de_map()
    {
        return off_pri_de_map;
    }

    private String orgUnitId;

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    public String getOrgUnitId()
    {
        return orgUnitId;
    }

    public OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String flagFolderPath;

    public String getFlagFolderPath()
    {
        return flagFolderPath;
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

    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }

    Map<String, String> dataValue = new HashMap<String, String>();

    public Map<String, String> getDataValue()
    {
        return dataValue;
    }

    
    private String currentDataSet;

    public String getCurrentDataSet()
    {
        return currentDataSet;
    }
    Map<String, String> dataValueTimeStampMap = new HashMap<String, String>();

    public Map<String, String> getDataValueTimeStampMap()
    {
        return dataValueTimeStampMap;
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

    private Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    private Map<String, String> keyIndicatorValueMap = new HashMap<String, String>();

    public Map<String, String> getKeyIndicatorValueMap()
    {
        return keyIndicatorValueMap;
    }

    private Map<String, String> thresholdMap = new HashMap<String, String>();

    public Map<String, String> getThresholdMap()
    {
        return thresholdMap;
    }

    private String lastUpdated;

    public String getLastUpdated()
    {
        return lastUpdated;
    }
    @Autowired
    private SectionService sectionService;
    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
    }

    private Map<String, List<DataSet>> datasetMap = new HashMap<String, List<DataSet>>();

    public Map<String, List<DataSet>> getDatasetMap()
    {
        return datasetMap;
    }

    private String keyFlagOnly;

    public String getKeyFlagOnly()
    {
        return keyFlagOnly;
    }

    private String showSource;

    public void setShowSource( String showSource )
    {
        this.showSource = showSource;
    }

    public String getShowSource()
    {
        return showSource;
    }

    public String dataSetSectionName;

    public String getDataSetSectionName()
    {
        return dataSetSectionName;
    }
    
    private String statusAndType;

    public void setStatusAndType( String statusAndType )
    {
        this.statusAndType = statusAndType;
    }

    private String dataElementType;

    public void setDataElementType( String dataElementType )
    {
        this.dataElementType = dataElementType;
    }

    private List<String> selectedListDataset = new ArrayList<String>();

   

    public void setSelectedListDataset( List<String> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    private List<DataSet> dataSetList = new ArrayList<DataSet>();

    public List<DataSet> getDataSetList()
    {
        return dataSetList;
    }

    private Map<DataSet, List<Indicator>> keyFlagMap = new HashMap<DataSet, List<Indicator>>();

    public Map<DataSet, List<Indicator>> getKeyFlagMap()
    {
        return keyFlagMap;
    }

    private Map<DataSet, List<DataElement>> dataElementMap = new HashMap<DataSet, List<DataElement>>();

    public Map<DataSet, List<DataElement>> getDataElementMap()
    {
        return dataElementMap;
    }

    private Map<DataElement, DataValue> dataValueMap = new HashMap<DataElement, DataValue>();

    public Map<DataElement, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private String periodTypeName;

    public String getPeriodTypeName()
    {
        return periodTypeName;
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

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    private List<Integer> treeSelectedId = new ArrayList<Integer>();

    public void setTreeSelectedId( List<Integer> treeSelectedId )
    {
        this.treeSelectedId = treeSelectedId;
    }
    public Map<String, String> colorMap = new HashMap<String, String>();
    
    public Map<String, String> getColorMap()
    {
        return colorMap;
    }
    
        private Set<Integer> percentageRequiredDe = new HashSet<Integer>();
        
        public Set<Integer> getPercentageRequiredDe() 
        {
                return percentageRequiredDe;
        }
        
        private Set<DataElement> keyFlagDes = new HashSet<DataElement>();
        
    public Set<DataElement> getKeyFlagDes() 
    {
                return keyFlagDes;
        }

    // ---------------------------------------------------------------------------------------------
    // Action implementation
    // ----------------------------------------------------------------------------------------------


        public String execute()
    {
            
          
        simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd" );
        
        Lookup lookup1 = lookupService.getLookupByName(Lookup.IS_PERCENTAGE);
        int percentage_attribute_id = Integer.parseInt(lookup1.getValue());

        Lookup lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        
  
        Set<DataElement> restrictedDeList = ivbUtil.getRestrictedDataElements( restrictedDeAttributeId );
        
        
        
        organisationUnit1 = organisationUnitService.getOrganisationUnit( orgUnitId );
        
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
        
        
        
        
        
        Set<DataElement> userDes = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
                userDes.addAll( userAuthorityGroup.getDataElements() );
        }
        
        

        List<UserAuthorityGroup> userAuthorityGroups1 = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials()
            .getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups1 )
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
 
        Map<String, List<Section>> sectionMap = ivbUtil.getSectionsByOrganisationUnit( (organisationUnitService.getOrganisationUnit( orgUnitId )).getId());

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
        DataSet selDataSet = null;
        Section selDataSetSection = null;

        if ( dataSetSectionId != null )
        {
            selDataSetSection = sectionService.getSection( Integer.parseInt( dataSetSectionId ) );

         
            selDataSet = selDataSetSection.getDataSet();

            dataElementList = new ArrayList<DataElement>( selDataSetSection.getDataElements() );
            
            List<DataElement> hiddenDeList = ivbUtil.getHiddenDataElementList( orgUnitId );
            
            dataElementList.removeAll( hiddenDeList );
            
            restrictedDeList.removeAll( userDataElements );
            dataElementList.removeAll( restrictedDeList );
            

            currentDataSet = selDataSetSection.getId() + "";

            dataSetSectionName = selDataSetSection.getName();
        }
        
        
        
      else if ( selectedListDataset != null )
        {
            List<String> datasetList = new ArrayList<String>();

            datasetList.addAll( selectedListDataset );

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
            
            List<DataElement> hiddenDeList = ivbUtil.getHiddenDataElementList( orgUnitId );
            
            dataElementList.removeAll( hiddenDeList );
            restrictedDeList.removeAll( userDataElements );
            dataElementList.removeAll( restrictedDeList );
        
            
        }
        
      
        
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
        
        
            
            
            Constant off_pri_de_attribute = constantService.getConstantByName( "OFFICIAL/PRIVATE_DE_ATTRIBUTE_ID" );
            int off_pri_de_attribute_id = 1;
            if ( off_pri_de_attribute != null )
            {
                off_pri_de_attribute_id = (int) off_pri_de_attribute.getValue();
            }

            off_pri_de_map = new HashMap<DataElement, String>();
          
            
            List<DataSet> datasetList1 = new ArrayList<DataSet>();
            for (String datasset: selectedListDataset)
            {
            	
            	DataSet ds=dataSetService.getDataSet(datasset);
            	datasetList1.add(ds);
            }
           
            for (DataSet dataSet: datasetList1)
            {   List<DataElement> datasetelement1 = new ArrayList<DataElement>();
            datasetelement1.addAll( dataSet.getDataElements() );
          
            	
            	
   			 for( DataElement dataElement : datasetelement1 )
            	
   			 {
                    /*
                if ( dataelemnt.getOptionSet() != null )
                {
                    List<String> optionsetList = dataelemnt.getOptionSet().getOptions();
                    optionSetMap.put( dataelemnt.getOptionSet().getId() + "", optionsetList );
                }
                */
                Constant keyDEConst = constantService.getConstantByName( KEY_DATAELEMENT );
                Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();

                if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
                {
                    for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                    {
                        if ( deAttributeValue.getAttribute().getId() == off_pri_de_attribute_id && deAttributeValue.getValue() != null )
                        {
                            if( deAttributeValue.getValue().trim().equalsIgnoreCase( "Official" ) )
                            {
                                off_pri_de_map.put( dataElement, "official" );
                            }
                            else if( deAttributeValue.getValue().trim().equalsIgnoreCase( "Private" ) )
                            {
                                off_pri_de_map.put( dataElement, "private" );
                            }
                            
                            else
                            {
                                off_pri_de_map.put( dataElement, "other" );
                            }
                        }
                    }
                }
                else
                {
                    off_pri_de_map.put( dataElement, "other" );
                }
            }
            
            
    }
            
            //System.out.println("dataelement1111111"+off_pri_de_map.size());
            
      
         
           
            
      
      
            
            
      
        userName = curUser.getUsername();
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        if ( dataElementType.equals( SHOW_KEY_FLAG_ONLY ) )
        {
            keyFlagOnly = "YES";
        }
        else
        {
            keyFlagOnly = "NO";
        }

        if ( showSource == null )
        {
            showSource = "NO";
        }
        else
        {
            showSource = "YES";
        }
        if ( treeSelectedId.size() > 0 )
        {
            organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            organisationUnit = selectionTreeManager.getSelectedOrganisationUnit();
        }
        else
        {
            return SUCCESS;
        }

        flagFolderPath = System.getenv( "DHIS2_HOME" ) + File.separator + "flags" + File.separator;
        DataElementGroup dataElementGroup = dataElementService.getDataElementGroupByName( DE_GROUP );
        List<DataElement> dataelementList = new ArrayList<DataElement>( dataElementGroup.getMembers() );

        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );

        IVBUtil.setOrganisationUnit( organisationUnit );
        IVBUtil.setOptionCombo( optionCombo );
        Date tempDate = null;
        
        
        
        List<DataElement> dataelementList1 = new ArrayList<DataElement>( dataElementGroup.getMembers() );
        
        
       // System.out.println("dataelementList"+dataelementList.size());
        for ( DataElement de : dataelementList )
        {
            Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( de.getAttributeValues() );
                
            DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
            if ( dv == null )
            {
                dataValue.put( de.getName(), "" );
                dataValueTimeStampMap.put( de.getName(), "" );
            }
            else
            {                   
                for ( AttributeValue attValue : attrValueSet )
                {
                    if ( attValue.getAttribute().getId() == percentage_attribute_id && attValue.getValue().equalsIgnoreCase( "true" ))
                        percentageRequiredDe.add(de.getId());
                }
                dataValue.put( de.getUid(), dv.getValue() );
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( dv.getPeriod().getStartDate() );
                if ( calendar.get( Calendar.MONTH ) >= 0 && calendar.get( Calendar.MONTH ) <= 2 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q1" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 3 && calendar.get( Calendar.MONTH ) <= 5 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q2" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 6 && calendar.get( Calendar.MONTH ) <= 8 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q3" );
                }
                else if ( calendar.get( Calendar.MONTH ) >= 9 && calendar.get( Calendar.MONTH ) <= 11 )
                {
                    dataValueTimeStampMap.put( de.getUid(), "" + calendar.get( Calendar.YEAR ) + " Q4" );
                }
                else
                {
                    dataValueTimeStampMap.put( de.getUid(), "" );
                }

                if ( tempDate == null || tempDate.before( dv.getLastUpdated() ) )
                {
                    tempDate = dv.getLastUpdated();
                }
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        if ( tempDate != null )
        {
            lastUpdated = simpleDateFormat.format( tempDate );
        }
        if ( selectedListDataset == null )
        {
        }

        
       
        Set<DataElement> hiddenDes = new HashSet<DataElement>( ivbUtil.getHiddenDataElementList( organisationUnit.getUid() ) );
        
        PeriodType periodType = null;
        for ( String dataSetId : selectedListDataset )
        {
            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            dataSetList.add( dataSet );

            List<DataElement> tempDEList = new ArrayList<DataElement>();

            if ( dataSet.getSections() == null || dataSet.getSections().size() <= 0 )
            {
                tempDEList.addAll( dataSet.getDataElements() );
            }
            else
            {
                for( Section section : dataSet.getSections() )
                {
                    if( section.getSources().contains( organisationUnit ) )
                    {
                        tempDEList.addAll( section.getDataElements() );
                    }
                }
            }

            if ( keyFlagOnly.equalsIgnoreCase( "NO" ) )
            {
                Iterator<DataElement> deIterator = tempDEList.iterator();
                while ( deIterator.hasNext() )
                {
                    DataElement dataElement = deIterator.next();

                    if( dataElement.getPublicAccess() != null && dataElement.getPublicAccess().equals( "--------" ) )
                    {
                        deIterator.remove();
                        continue;
                    }
                    
                    DataValue dataValue = dataValueService.getLatestDataValue( dataElement, optionCombo, organisationUnit );
                    if ( dataValue != null )
                    {
                        dataValueMap.put( dataElement, dataValue );
                    }
                    else
                    {
                        dataValueMap.put( dataElement, new DataValue() );
                    }

                    if ( dataElementType.equals( SHOW_DATA_DES ) )
                    {
                        if ( dataValue == null )
                        {
                            deIterator.remove();
                        }
                    }
                    else if ( dataElementType.equals( SHOW_BLANK_DES ) )
                    {
                        if ( dataValue != null )
                        {
                            deIterator.remove();
                        }
                    }
                }
            }

            List<Indicator> keyFlags = new ArrayList<Indicator>();
            List<Indicator> indicaList = new ArrayList<Indicator>( dataSet.getIndicators() );
            Set<DataElement> indicatorDataElements = new HashSet<DataElement>();
            for ( Indicator indicator : indicaList )
            {              
                List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
                for ( AttributeValue attributeValue : attributeValueList )
                {
                    if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_FLAG ) )
                    {
                        keyFlags.add( indicator );
                    }
                }

                String keyIndicatorValue = "";
                String exString = indicator.getNumerator();
                
                keyFlagDes.addAll( expressionService.getDataElementsInExpression( exString ) );
                
                if( exString.contains( "#VALUE#" ) )
                {
                    indicatorDataElements.addAll( expressionService.getDataElementsInExpression( exString ) );
                }
                
                if ( exString.contains( KeyFlagCalculation.NESTED_OPERATOR_AND ) || exString.contains( KeyFlagCalculation.NESTED_OPERATOR_OR ) )
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
                } 
                catch (Exception e) 
                {
                    
                }
                
                keyIndicatorValueMap.put( indicator.getUid() + "-" + organisationUnit.getUid(), keyIndicatorValue );
                commentMap = keyFlagCalculation.getCommentMap();
                sourceMap = keyFlagCalculation.getSourceMap();
                periodMap = keyFlagCalculation.getPeriodMap();
                colorMap = keyFlagCalculation.getColorMap();
            }
            
            if ( keyFlags != null && keyFlags.size() > 0 )
            {
                keyFlagMap.put( dataSet, keyFlags );
            }

            tempDEList.removeAll( indicatorDataElements );
            
            tempDEList.removeAll( hiddenDes );
            
            Set<DataElement> restrictedDes = new HashSet<DataElement>( ivbUtil.getRestrictedDataElements( restrictedDeAttributeId ) );
            restrictedDes.removeAll( userDes );
            tempDEList.removeAll( restrictedDes );
            
            dataElementMap.put( dataSet, tempDEList );
        }

        messageCount = (int) messageService.getUnreadMessageConversationCount();
     /*   List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
        if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
        {
            adminStatus = "Yes";
        }
        else
        {
            adminStatus = "No";
        }
*/
        return SUCCESS;
    }

}
