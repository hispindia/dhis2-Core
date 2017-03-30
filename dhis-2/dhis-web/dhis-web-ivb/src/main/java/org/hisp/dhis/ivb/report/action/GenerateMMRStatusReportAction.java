package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Priyanka Bawa
 */

public class GenerateMMRStatusReportAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private SectionService sectionService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ConstantService constantService;

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private LookupService lookupService;

    @Autowired
    private IVBUtil ivbUtil;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private DataValueService dataValueService;

    // -------------------------------------------------------------------------
    // Setters & Getters
    // -------------------------------------------------------------------------

    private List<Integer> selectedListMMR;

    public void setSelectedListMMR( List<Integer> selectedListMMR )
    {
        this.selectedListMMR = selectedListMMR;
    }

    public List<Integer> getSelectedListMMR()
    {
        return selectedListMMR;
    }

    private String includeComments;

    public String getIncludeComments()
    {
        return includeComments;
    }

    public void setIncludeComments( String includeComments )
    {
        this.includeComments = includeComments;
    }

    private Integer ipvSectionId;

    public Integer getIpvSectionId()
    {
        return ipvSectionId;
    }

    public void setIpvSectionId( Integer ipvSectionId )
    {
        this.ipvSectionId = ipvSectionId;
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

    private List<OrganisationUnitGroup> whoregions = new ArrayList<OrganisationUnitGroup>();

    public List<OrganisationUnitGroup> getWhoregions()
    {
        return whoregions;
    }

    private List<OrganisationUnitGroup> unicef = new ArrayList<OrganisationUnitGroup>();

    public List<OrganisationUnitGroup> getUnicef()
    {
        return unicef;
    }

    private Map<String, DataValue> headerDataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getHeaderDataValueMap()
    {
        return headerDataValueMap;
    }

    private Map<String, DataValue> PopulationDataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getPopulationDataValueMap()
    {
        return PopulationDataValueMap;
    }

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private OrganisationUnitGroupSet unicefRegionsGroupSet;

    public OrganisationUnitGroupSet getUnicefRegionsGroupSet()
    {
        return unicefRegionsGroupSet;
    }

    private List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitList()
    {
        return orgUnitList;
    }

    private List<OrganisationUnit> orgUnitListParent = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getOrgUnitListParent()
    {
        return orgUnitListParent;
    }

    private List<OrganisationUnit> selectedOuParent = new ArrayList<OrganisationUnit>();

    public List<OrganisationUnit> getSelectedOuParent()
    {
        return selectedOuParent;
    }

    private List<String> deName = new ArrayList<String>();

    public List<String> getDeName()
    {
        return deName;
    }

    private OrganisationUnitGroupSet MMRGroupSet;

    public OrganisationUnitGroupSet getMMRGroupSet()
    {
        return MMRGroupSet;
    }

    private OrganisationUnitGroupSet whoRegionsGroupSet;

    public OrganisationUnitGroupSet getWhoRegionsGroupSet()
    {
        return whoRegionsGroupSet;
    }

    public String dataSetUId = "pnBInaJB4W0";

    public String getDataSetUId()
    {
        return dataSetUId;
    }

    @Autowired
    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    private List<Integer> orgUnitIds = new ArrayList<Integer>();

    public void setOrgUnitIds( List<Integer> orgUnitIds )
    {
        this.orgUnitIds = orgUnitIds;
    }

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
    public String execute()
    {

        if ( selectedListMMR != null )
        {
            for ( Integer orgUnitGroupId : selectedListMMR )
            {
                orgUnitList.addAll( organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroupId )
                    .getMembers() );

            }
        }

        for ( OrganisationUnit ouGroupParent : orgUnitList )
        {
            orgUnitListParent.add( ouGroupParent.getParent() );
        }

        List<OrganisationUnit> orgUnitListFromTree = new ArrayList<OrganisationUnit>();
        List<OrganisationUnit> orgUnitListParentFromTree = new ArrayList<OrganisationUnit>();
        if ( orgUnitIds.size() > 1 )
        {

            for ( Integer id : orgUnitIds )
            {
                OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( id );

                if ( orgUnit.getHierarchyLevel() == 3 )
                {

                    orgUnitListFromTree.add( orgUnit );
                    orgUnitListParentFromTree.add( orgUnit.getParent() );

                }
            }
            // System.out.println("orgUnitListFromTree***********" +
            // orgUnitListFromTree);
        }

        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {

            List<OrganisationUnit> lastLevelOrgUnit = new ArrayList<OrganisationUnit>();
            List<OrganisationUnit> lastLevelOrgUnitParent = new ArrayList<OrganisationUnit>();

            List<OrganisationUnit> userOrgUnits = new ArrayList<OrganisationUnit>(
                selectionTreeManager.getSelectedOrganisationUnits() );

            for ( OrganisationUnit orgUnit : userOrgUnits )
            {

                if ( orgUnit.getHierarchyLevel() == 3 )
                {

                    lastLevelOrgUnit.add( orgUnit );
                    lastLevelOrgUnitParent.add( orgUnit.getParent() );
                }
                /*
                 * else { lastLevelOrgUnit.addAll(
                 * organisationUnitService.getOrganisationUnitsAtLevel( 3,
                 * orgUnit ) ); }
                 */
            }

            orgUnitListFromTree.addAll( lastLevelOrgUnit );
            orgUnitListParentFromTree.addAll( lastLevelOrgUnitParent );

        }

        // orgUnitListFromTree.removeAll( lastLevelOrgUnit );

        orgUnitListParentFromTree.removeAll( orgUnitList );

        for ( OrganisationUnit ouParent : orgUnitListParentFromTree )
        {
            selectedOuParent.add( ouParent );

        }

        System.out.println( "orgUnitListFromTree size----" + orgUnitListFromTree.size() + "---orgUnitList size----"
            + orgUnitList.size() );
        // code for union of orgUnit Group and Tree selection

        if ( (orgUnitListFromTree.size() == 0) || (orgUnitList.size() == 0) )
        {
            orgUnitListFromTree.removeAll( orgUnitList );
            orgUnitList.addAll( orgUnitListFromTree );
        }

        // code for intersection of orgUnit Group and Tree selection

        else if ( (orgUnitListFromTree.size() > 0) && (orgUnitList.size() > 0) )
        {
            orgUnitList.retainAll( orgUnitListFromTree );
        }

        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        String orgUnitIdsByComma = "-1";
        List<Integer> orgunitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );

        // System.out.println( "orgUnitList--" + orgUnitList );

        if ( orgUnitList.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgunitIds );
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

        Lookup lookup = lookupService.getLookupByName( Lookup.UNICEF_REGIONS_GROUPSET );

        unicefRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup
            .getValue() ) );

        lookup = lookupService.getLookupByName( "MMR_GROUPSET" );

        MMRGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        lookup = lookupService.getLookupByName( Lookup.WHO_REGIONS_GROUPSET );

        whoRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup
            .getValue() ) );

        if ( includeComments == null )
        {
            includeComments = "NO";
        }
        else
        {
            includeComments = "YES";
        }

        for ( OrganisationUnitGroup org : unicefRegionsGroupSet.getOrganisationUnitGroups() )
        {

            unicef.add( org );

        }

        for ( OrganisationUnitGroup org : whoRegionsGroupSet.getOrganisationUnitGroups() )
        {

            whoregions.add( org );

        }
        // System.out.println( "who regions ---"+whoregions );

        DataSet dataset = dataSetService.getDataSet( 21 ); // data set id to get
                                                           // data elements
        Collection<DataElement> dataElements = dataset.getDataElements();

        for ( DataElement de : dataElements )
        {

            deName.add( de.getName() );

        }

        System.out.println( "DE 1******" + deName );

        List<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );
        String dataElementIdsByComma = "-1";
        if ( dataElementIds.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( dataElementIds );

        }
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );

        Constant tabularDataElementGroupId = constantService
            .getConstantByName( Lookup.TABULAR_REPORT_DATAELEMENTGROUP_ID );
        // System.out.println( "tabularDataElementGroupId" +
        // tabularDataElementGroupId );
        dataElements = new ArrayList<DataElement>(
            dataElementService.getDataElementsByGroupId( (int) tabularDataElementGroupId.getValue() ) );

        List<Integer> headerDataElementIds = new ArrayList<Integer>( getIdentifiers( dataElements ) );

        String headerDataElementIdsByComma = "-1";
        if ( headerDataElementIds.size() > 0 )
        {
            headerDataElementIdsByComma = getCommaDelimitedString( headerDataElementIds );
        }

        headerDataValueMap = ivbUtil.getLatestDataValuesForTabularReport( headerDataElementIdsByComma,
            orgUnitIdsByComma );
        dataValueMap.putAll( headerDataValueMap );
        /*
         * Constant PopulationMMRid = constantService.getConstantByName(
         * Lookup.PopulationMMR );
         * System.out.println("PopulationMMRid"+PopulationMMRid); dataElements =
         * new ArrayList<DataElement>(
         * dataElementService.getDataElementsByGroupId( (int)
         * PopulationMMRid.getValue() ) ); List<Integer>
         * populationDataElementIds = new ArrayList<Integer>( getIdentifiers(
         * dataElements ) ); String populationDataElementIdsByComma = "-1"; if (
         * populationDataElementIds.size() > 0 ) {
         * populationDataElementIdsByComma = getCommaDelimitedString(
         * populationDataElementIds ); } PopulationDataValueMap =
         * ivbUtil.getLatestDataValuesForTabularReport(
         * populationDataElementIdsByComma, orgUnitIdsByComma );
         * System.out.println("PopulationDataValueMap"+PopulationDataValueMap);
         */
        return SUCCESS;
    }
}
