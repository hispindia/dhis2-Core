package org.hisp.dhis.ivb.dataentry.dewise.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DecimalFormat;
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
import java.util.UUID;

import jxl.CellView;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Blank;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;

import au.com.bytecode.opencsv.CSVWriter;

import com.opensymphony.xwork2.Action;

public class DownloadOfflineCSVFormAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<Integer> dataElementsSelectedList;
    
    public void setDataElementsSelectedList(List<Integer> dataElementsSelectedList) 
    {
         this.dataElementsSelectedList = dataElementsSelectedList;
    }
    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    private List<String> selectedListDataset;

    public void setSelectedListDataset( List<String> selectedListDataset )
    {
        this.selectedListDataset = selectedListDataset;
    }

    private List<String> selectedListOrgunit;

    public void setSelectedListOrgunit( List<String> selectedListOrgunit )
    {
        this.selectedListOrgunit = selectedListOrgunit;
    }

    private Integer orgUnitGroupId;
    
    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }
    
    private String orgUnitId;
    
    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }
    
    private String includeData;
    
    public void setIncludeData( String includeData )
    {
        this.includeData = includeData;
    }

    private String includeTA;
    
    public void setIncludeTA( String includeTA )
    {
        this.includeTA = includeTA;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        SimpleDateFormat yearFormat = new SimpleDateFormat( "yyyy" );
        
        String periodNames = "";
        
        Set<DataElement>  userDataElements = new HashSet<DataElement>();
        
        User curUser = currentUserService.getCurrentUser();
        
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        
        for( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        
        
        Set<DataSet> dataSetList = new HashSet<DataSet>();
        
        if( selectedListDataset == null )
        { 
            
        }
        
        List<DataElement> dataElementList = new ArrayList<DataElement>();
        for( Integer dataElementId : dataElementsSelectedList )
        {
            DataElement de = dataElementService.getDataElement( dataElementId ); 
            
            dataSetList.addAll( de.getDataSets() );
            
            dataElementList.add( de );
        }
        
        dataElementList.retainAll( userDataElements );
        Collection<Integer> dataElementIds = new ArrayList<Integer>( getIdentifiers( dataElementList ) );        
        String dataElementIdsByComma = "";
        if(dataElementIds.size() > 0)
        {
            dataElementIdsByComma = getCommaDelimitedString( dataElementIds ); 
        }
        else
        {
            dataElementIdsByComma = "-1";
        }
        
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>( selectionTreeManager.getReloadedSelectedOrganisationUnits() );

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
        
        
        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );        
        String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );

        Map<String, DataValue> latestDataValues = new HashMap<String, DataValue>();
        
        if( includeData != null )
        {
            latestDataValues.putAll( ivbUtil.getLatestDataValues( dataElementIdsByComma, orgUnitIdsByComma ) );
        }
        
        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  "temp";
        File newdir = new File( outputReportPath );
        if( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".csv";

        CSVWriter csvWriter = new CSVWriter(new FileWriter( outputReportPath ), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
        List<String[]> csvData = new ArrayList<String[]>();
        
        if( includeTA == null )
        {
            csvData.add( new String[] {"CountryCode","IndicatorCode","Period","Value","Comment"} );
        }
        else
        {
            csvData.add( new String[] {"CountryCode","IndicatorCode","Period","Value","Comment","TA"} );
        }

        Period period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );
                 
        for( DataElement dataElement : dataElementList )
        {
            String description = period.getDescription();
            if(description.contains( "Q" ))
            {
                description = description.replace( "Q", "-Q" );
            }
            String curPeriod = description;
            Constant yearlyDEConst = constantService.getConstantByName( "YEARLY_DATAELMENT_ATTRIBUTE_ID" );
            Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
            if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
            {
                for ( AttributeValue deAttributeValue : dataElementAttributeValues )
                {
                    if ( deAttributeValue.getAttribute().getId() == yearlyDEConst.getValue() &&  deAttributeValue.getValue().equalsIgnoreCase( "true" ) )
                    {
                        curPeriod = yearFormat.format( period.getStartDate() );
                        break;
                    }
                }
            }

            for( OrganisationUnit orgUnit : orgUnitList )
            {
                String dataElementCode = "";
                String pattern="000";
                DecimalFormat myFormatter = new DecimalFormat(pattern);
                
                if( includeData != null )
                {
                    dataElementCode = myFormatter.format( Integer.parseInt( dataElement.getCode().substring( 2 )));
                    DataValue dv = latestDataValues.get( orgUnit.getId()+":"+dataElement.getId() );
                    String value = "";
                    if( dv != null && dv.getValue() != null )
                    {
                        value = dv.getValue();
                    }
                
                    String comment = "";
                    if( dv != null && dv.getComment() != null )
                    {
                        comment = dv.getComment();
                    }
                    
                    if( includeTA == null )
                    {
                        csvData.add( new String[] {orgUnit.getCode(),dataElementCode,curPeriod,value,comment} );
                    }
                    else
                    {
                        csvData.add( new String[] {orgUnit.getCode(),dataElementCode,curPeriod,value,comment,""} );
                    }
                }
                else
                {
                    dataElementCode = myFormatter.format( Integer.parseInt(dataElement.getCode().substring( 2 )))+"";
                    System.out.println( dataElementCode );
                    if( includeTA == null )
                    {
                        csvData.add( new String[] {orgUnit.getCode(),dataElementCode,curPeriod,"",""} );
                    }
                    else
                    {
                        csvData.add( new String[] {orgUnit.getCode(),dataElementCode,curPeriod,"","",""} );
                    }
                }
            }
        }
        csvWriter.writeAll( csvData );
        
        csvWriter.close();
        
        fileName = "OfflineEntry.csv";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
    
        outputReportFile.deleteOnExit();

        return SUCCESS;
    }
}
