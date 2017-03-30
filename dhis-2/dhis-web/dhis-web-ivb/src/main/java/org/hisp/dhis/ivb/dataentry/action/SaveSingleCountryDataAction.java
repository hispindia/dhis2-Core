package org.hisp.dhis.ivb.dataentry.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.ivb.util.ReportScheduler;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */
public class SaveSingleCountryDataAction
    implements Action
{

    private static final Log log = LogFactory.getLog( SaveSingleCountryDataAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private OptionService optionservice;

    public OptionService getOptionservice()
    {
        return optionservice;
    }

    public void setOptionservice( OptionService optionservice )
    {
        this.optionservice = optionservice;
    }

    private Option Option;

    public Option getOption()
    {
        return Option;
    }

    public void setOption( Option option )
    {
        Option = option;
    }

    
    private PeriodService periodService;
    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private DeMapping demapping;

    public DeMapping getDemapping()
    {
        return demapping;
    }

    public void setDemapping( DeMapping demapping )
    {
        this.demapping = demapping;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
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

    private DataValueAuditService dataValueAuditService;

    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
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

    private ReportScheduler reportScheduler;

    public void setReportScheduler( ReportScheduler reportScheduler )
    {
        this.reportScheduler = reportScheduler;
    }

    @Autowired
    private DeMappingService demappingService;

    public DeMappingService getDemappingService()
    {
        return demappingService;
    }

    public void setDemappingService( DeMappingService demappingService )
    {
        this.demappingService = demappingService;
    }

    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }

    private String currentDataSet;

    public String getCurrentDataSet()
    {
        return currentDataSet;
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

    private String selectedPeriodId;

    public void setSelectedPeriodId( String selectedPeriodId )
    {
        this.selectedPeriodId = selectedPeriodId;
    }

    public String getSelectedPeriodId()
    {
        return selectedPeriodId;
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

    private String dataElements;

    public void setDataElements( String dataElements )
    {
        this.dataElements = dataElements;
    }

    private String conflict;

    public void setConflict( String conflict )
    {
        this.conflict = conflict;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see com.opensymphony.xwork2.Action#execute()
     */
    public String execute()
    {
        // System.out.println( " Inside Save Single CountryData Action" ) ;

        Period period = PeriodType.getPeriodFromIsoString( selectedPeriodId );
        if ( period == null )
        {
            return logError( "Illegal period identifier: " + selectedPeriodId );
        }
        List<String> orgUnitUids = new ArrayList<String>();
        orgUnitUids.add( orgUnitUid );

        List<OrganisationUnit> organisationUnits = organisationUnitService.getOrganisationUnitsByUid( orgUnitUids );

        if ( organisationUnits == null )
        {
            return logError( "Invalid organisation unit identifier: " + orgUnitUid );
        }

        OrganisationUnit selOrgUnit = organisationUnits.get( 0 );
        List<String> dataSetUids = new ArrayList<String>();
        dataSetUids.add( dataSetUId );

        DataSet selDataSet = null;
        Section selDataSetSection = null;
        List<DataElement> dataElementList = new ArrayList<DataElement>();
        List<DataElement> changeddataElementList = new ArrayList<DataElement>();
        String[] dataEntryIds = dataElements.split( "," );

        for ( String deID : dataEntryIds )
        {
            if ( deID != "" )
            {
                String[] dataElementId = deID.split( "-" );
                if ( !dataElementId[0].equals( "" ) && !dataElementId[0].equalsIgnoreCase( "conflict" ) )
                {
                    DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( dataElementId[0] ) );
                    if ( !changeddataElementList.contains( dataElement ) )
                    {
                        changeddataElementList.add( dataElement );
                    }
                }
            }
        }
        try
        {
            if ( dataSetSectionId != null && !dataSetSectionId.trim().equals( "" ) )
            {
                selDataSetSection = sectionService.getSection( Integer.parseInt( dataSetSectionId ) );

                if ( dataSetSectionId == null || selDataSetSection == null )
                {
                    return logError( "Illegal dataset identifier: " + dataSetSectionId );
                }

                selDataSet = selDataSetSection.getDataSet();
                currentDataSet = selDataSetSection.getId() + "";

                dataElementList = new ArrayList<DataElement>( selDataSetSection.getDataElements() );
            }
            else if ( dataSetUId != null && !dataSetUId.trim().equals( "" ) )
            {
                List<String> datasetList = new ArrayList<String>();

                datasetList.add( dataSetUId );

                selDataSet = dataSetService.getDataSetsByUid( datasetList ).get( 0 );
                currentDataSet = selDataSet.getUid();
                List<DataElement> tempDEList = new ArrayList<DataElement>();
                for ( Section section : selDataSet.getSections() )
                {
                    tempDEList.addAll( section.getDataElements() );
                }

                dataElementList.addAll( tempDEList );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return logError( "Illegal dataset identifier: " + dataSetUId );
        }

        User curUser = currentUserService.getCurrentUser();
        Set<DataElement> userDataElements = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>(
            curUser.getUserCredentials().getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        dataElementList.retainAll( changeddataElementList );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        HttpServletRequest request = ServletActionContext.getRequest();

        for ( DataElement dataElement : dataElementList )
        {

            String value = request
                .getParameter( dataElement.getId() + "-" + selOrgUnit.getId() + "-" + optionCombo.getId() + "-val" );

            value = value.replace( ",", "" );
            value = value.replace( "%", "" );

            // System.out.println(dataElement.getOptionSet().getOptions());
            // String l1= dataElement.getOptionSet().getName();

      

    

            // System.out.println(l1);

            /*
             * System.err.println("value after applying regEx "+ value);
             * if(value.equalsIgnoreCase("na")){ value = ""; }
             */

            String comment = request.getParameter(
                dataElement.getId() + "-" + selOrgUnit.getId() + "-" + optionCombo.getId() + "-comment" );

            if ( dataElement.getOptionSet() != null && dataElement.getOptionSet().getOptions().size() > 0 )
            {
                if ( value != null && value.trim().equals( "-1" ) )
                {
                    value = null;
                }
            }
            DataValue dataValue = dataValueService.getDataValue( dataElement, period, selOrgUnit, optionCombo );

          /*  demapping = demappingService.getDeMapping( dataElement.getUid() );
            Period period1=  periodService.getPeriod( 1008 );
            DataElement dataElement1 = dataElementService.getDataElement( demapping.getMappeddeid() );
            DataValue dataValue2 = dataValueService.getDataValue( dataElement1, period, selOrgUnit, optionCombo );
            System.out.println( "dataValue2" + dataValue2 );
            if ( dataValue2 == null )
            {
                dataValue2 = new DataValue( dataElement1, period1, selOrgUnit, optionCombo, optionCombo, value1,
                    storedBy, now, comment );
                dataValue2.setStatus( 1 );
           
                dataValueService.addDataValue( dataValue2 );
            }
            else
            {
                dataValue2.setDataElement( dataElement1 );
                dataValue2.setPeriod( period1 );
                dataValue2.setSource( selOrgUnit );
                dataValue2.setCategoryOptionCombo( optionCombo );
                dataValue2.setAttributeOptionCombo( optionCombo );
                dataValue2.setValue( value1 );
                dataValue2.setStoredBy( storedBy );
                dataValue2.setCreated( now );
                dataValue2.setComment( comment );
                dataValue2.setStatus( 1 );

                dataValueService.addDataValue( dataValue2 );
            }
*/
            if ( dataValue == null )
            {
                if ( (value != null && !value.trim().equals( "" ))
                    || (comment != null && !comment.trim().equals( "" )) )
                {
                    dataValue = new DataValue( dataElement, period, selOrgUnit, optionCombo, optionCombo, value,
                        storedBy, now, comment );
                    dataValue.setStatus( 1 );
                    dataValueService.addDataValue( dataValue );

                    DataValue dataValue1 = dataValueService.getDataValue( dataElement, period, selOrgUnit,
                        optionCombo );
                    DataValueAudit dataValueAudit = new DataValueAudit( dataValue1, dataValue1.getValue(),
                        dataValue1.getStoredBy(), dataValue1.getLastUpdated(), dataValue1.getComment(),
                        DataValueAudit.DVA_CT_HISOTRY, DataValueAudit.DVA_STATUS_ACTIVE );
                    dataValueAuditService.addDataValueAudit( dataValueAudit );
                }
            }
            else
            {
                if ( conflict == null && (dataValue.getComment() != null && dataValue.getValue() != null)
                    && !(dataValue.getStoredBy().equalsIgnoreCase( storedBy ))
                    && (!(dataValue.getValue().trim().equalsIgnoreCase( value.trim() ))
                        || !(dataValue.getComment().equalsIgnoreCase( comment.trim() ))) )
                {
                    dataValue.setFollowup( true );
                }
                else
                {
                    dataValue.setFollowup( false );
                }

                dataValue.setValue( value );
                dataValue.setComment( comment );
                dataValue.setLastUpdated( now );
                dataValue.setStoredBy( storedBy );
                dataValue.setStatus( 1 );
                dataValueService.updateDataValue( dataValue );

                DataValueAudit dataValueAudit = dataValueAuditService.getDataValueAuditByLastUpdated_StoredBy(
                    dataElement, selOrgUnit, now, storedBy, 1, DataValueAudit.DVA_CT_HISOTRY );

                if ( dataValueAudit == null )
                {
                    dataValueAudit = new DataValueAudit( dataValue, dataValue.getValue(), dataValue.getStoredBy(),
                        dataValue.getLastUpdated(), dataValue.getComment(), DataValueAudit.DVA_CT_HISOTRY,
                        DataValueAudit.DVA_STATUS_ACTIVE );
                    dataValueAuditService.addDataValueAudit( dataValueAudit );
                }
                else
                {
                    dataValueAudit.setOrganisationUnit( dataValue.getSource() );
                    dataValueAudit.setCategoryOptionCombo( dataValue.getCategoryOptionCombo() );
                    dataValueAudit.setPeriod( dataValue.getPeriod() );
                    dataValueAudit.setDataElement( dataValue.getDataElement() );

                    // dataValueAudit.setDataValue( dataValue );
                    dataValueAudit.setValue( value );
                    dataValueAudit.setComment( comment );
                    dataValueAudit.setCommentType( DataValueAudit.DVA_CT_HISOTRY );
                    dataValueAudit.setModifiedBy( storedBy );
                    dataValueAudit.setTimestamp( now );
                    dataValueAuditService.updateDataValueAudit( dataValueAudit );
                }
            }
        }

        // System.out.println( " Data Set Id " + selDataSet.getId() + "--
        // OrgUnit : " + selOrgUnit.getId() );

        Lookup keyFlagIndicatorAttributeLookup = lookupService.getLookupByName( Lookup.KEYFLAG_INDICATOR_ATTRIBUTE_ID );

        List<Indicator> indicators = new ArrayList<Indicator>();
        indicators = new ArrayList<Indicator>(
            reportScheduler.getKeyFlagIndicatorList( Integer.parseInt( keyFlagIndicatorAttributeLookup.getValue() ) ) );

        // Saving in Key Flag Analytic
        if ( selDataSet != null )
        {
            Set<Indicator> indicatorList = new HashSet<Indicator>();
            indicatorList = new HashSet<Indicator>( selDataSet.getIndicators() );

            if ( indicatorList != null && indicatorList.size() > 0 )
            {
                for ( Indicator indicator : indicatorList )
                {
                    if ( indicators.contains( indicator ) )
                    {
                        // System.out.println( " OrgUnit Id " +
                        // selOrgUnit.getId() + "-- Indicator Id : " +
                        // indicator.getId() );
                        reportScheduler.updateSingleKeyFlagAnalytic( selOrgUnit, indicator );
                    }
                }
            }
        }

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String logError( String message )
    {
        return logError( message, 1 );
    }

    private String logError( String message, int statusCode )
    {
        log.info( message );

        this.statusCode = statusCode;

        return SUCCESS;
    }
}
