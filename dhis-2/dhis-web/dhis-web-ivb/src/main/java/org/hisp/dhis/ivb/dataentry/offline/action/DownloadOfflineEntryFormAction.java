package org.hisp.dhis.ivb.dataentry.offline.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;

import com.opensymphony.xwork2.Action;

import jxl.CellView;
import jxl.Workbook;
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
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class DownloadOfflineEntryFormAction implements Action
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

    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    private IVBUtil ivbUtil;
    
    public void setIvbUtil( IVBUtil ivbUtil )
    {
        this.ivbUtil = ivbUtil;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

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
        String periodNames = "";
        
        Set<DataElement>  userDataElements = new HashSet<DataElement>();
        
        User curUser = currentUserService.getCurrentUser();
        
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        
        for( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        
        
        List<DataSet> dataSetList = new ArrayList<DataSet>();
        
        if( selectedListDataset == null )
        {           
        }
        
        List<DataElement> dataElementList = new ArrayList<DataElement>();
        for( String selDataSet : selectedListDataset )
        {
            DataSet ds = dataSetService.getDataSet( Integer.parseInt( selDataSet ) );
            dataSetList.add( ds );
            dataElementList.addAll( ds.getDataElements() );
        }
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
        
        OrganisationUnit rootOrgUnit = organisationUnitService.getOrganisationUnit(  orgUnitId  );        
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitWithChildren( rootOrgUnit.getId() ) );

        OrganisationUnitGroup orgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroupId );
        if ( orgUnitGroup != null )
        {
            orgUnitList.retainAll( orgUnitGroup.getMembers() );
        }

        User currentUser = currentUserService.getCurrentUser();
        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUser.getOrganisationUnits() );
        Set<OrganisationUnit> currentUserOrgUnitTree = new HashSet<OrganisationUnit>();        
        for( OrganisationUnit orgUnit : currentUserOrgUnits )
        {
            currentUserOrgUnitTree.addAll( organisationUnitService.getOrganisationUnitWithChildren( orgUnit.getId() ) );
        }
        
        orgUnitList.retainAll( currentUserOrgUnitTree );        
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );
        
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
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

        WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new File( outputReportPath ) );
        WritableSheet sheet0 = outputReportWorkbook.createSheet( "OfflineEntry", 0 );
        WritableSheet sheet1 = outputReportWorkbook.createSheet( "OfflineEntryInput", 1 );

        int rowStart = 2;
        int colStart = 2;
        
        int cellCount = 0;
        int colCount = 0;
        int rowCount = 0;
        
        String cellStartRange = "";
        String cellEndRange = "";
        int cellRangeFlag = 0;
        
        sheet0.mergeCells( colStart + colCount - 1, rowStart, colStart + colCount - 1, rowStart + 1 );
        sheet0.addCell( new Label( colStart + colCount - 1, rowStart + rowCount, "Key Indicator", getCellFormat1() ) );
        sheet0.setColumnView( colStart + colCount - 1, 50 );
        for( OrganisationUnit orgUnit : orgUnitList )
        {
            sheet0.mergeCells( colStart + colCount, rowStart + rowCount, colStart + colCount + 2, rowStart + rowCount );
            sheet0.addCell( new Label( colStart + colCount, rowStart + rowCount, orgUnit.getName(), getCellFormat1() ) );
            sheet0.addCell( new Label( colStart + colCount, rowStart + rowCount + 1, "Value", getCellFormat1() ) );
            sheet0.setColumnView( colStart + colCount, 30 );
            sheet0.addCell( new Label( colStart + colCount + 1, rowStart + rowCount + 1, "Comment", getCellFormat1() ) );
            sheet0.setColumnView( colStart + colCount + 1, 30 );
            sheet0.addCell( new Label( colStart + colCount + 2, rowStart + rowCount + 1, "TA", getCellFormat1() ) );
            sheet0.setColumnView( colStart + colCount + 2, 30 );
            if( includeTA == null )
            {
                CellView cellView = new CellView();                
                cellView.setHidden( true );
                cellView.setSize( 10 );                
                sheet0.setColumnView( colStart + colCount + 2, cellView );
            }
            
            colCount += 3;
            cellCount++;
        }
        
        List<String> tempPeriodNames = new ArrayList<String>();
        
        rowCount += 2;
        colCount = -1;
        for( DataSet dataSet : dataSetList )
        {
            colCount = -1;
            sheet0.mergeCells( colStart + colCount, rowStart + rowCount, colStart + colCount + cellCount * 3, rowStart + rowCount );
            sheet0.addCell( new Label( colStart + colCount, rowStart + rowCount, dataSet.getName(), getCellFormat1() ) );
            rowCount++;
            
            PeriodType periodType = dataSet.getPeriodType();
            
            Period period = ivbUtil.getCurrentPeriod( periodType, new Date() );
            
            if( tempPeriodNames.contains( period.getName() ) )
            {
                
            }
            else
            {
                periodNames += period.getName() + " ";
                tempPeriodNames.add( period.getName() );
            }
            
            List<Section> dataSetSections = new ArrayList<Section>( dataSet.getSections() );
            Collections.sort( dataSetSections, new IdentifiableObjectNameComparator() );
            
            List<DataElement> dataSetMembers = new ArrayList<DataElement>();
            for( Section dataSetSection : dataSetSections )
            {
                dataSetMembers.addAll( dataSetSection.getDataElements() );
            }
            
            dataSetMembers.retainAll( userDataElements );
           
            for( DataElement dataElement : dataSetMembers )
            {
               
                colCount = -1;
                sheet0.addCell( new Label( colStart + colCount, rowStart + rowCount, dataElement.getName(), getCellFormat3() ) );
                
                OptionSet optionSet = dataElement.getOptionSet();
                
                colCount++;
                for( OrganisationUnit orgUnit : orgUnitList )
                {
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
                    
                    if( optionSet != null && optionSet.getOptions() != null && optionSet.getOptions().size() > 0 )
                    {                        
                        List<Option> optionObjs = new ArrayList<Option>( optionSet.getOptions() );
                        List<String> optionCodes = new ArrayList<String>();
                        for( Option option : optionObjs )
                        {
                        	optionCodes.add( option.getCode() );
                        }
                        WritableCellFeatures wcf = new WritableCellFeatures();
                        wcf.setDataValidationRange( cellEndRange );
                        wcf.setDataValidationList( optionCodes );                       
                        if( includeData == null )
                        {
                            Blank blankCell = new Blank( colStart + colCount, rowStart + rowCount );
                            blankCell.setCellFeatures( wcf );
                            blankCell.setCellFormat( getCellFormat2() );
                            sheet0.addCell( blankCell );
                        }
                        else
                        {
                            WritableCell writableCell = new Label( colStart + colCount, rowStart + rowCount, value );
                            writableCell.setCellFeatures( wcf );
                            writableCell.setCellFormat( getCellFormat2() );                        
                            sheet0.addCell( writableCell );
                        }
                    }
                    else if( dataElement.getValueType().isInteger() )
                    {
                        WritableCellFeatures wcf = new WritableCellFeatures();
                        wcf.setNumberValidation( 0, WritableCellFeatures.GREATER_EQUAL );

                        if( includeData == null )
                        {
                            Blank blankCell = new Blank( colStart + colCount, rowStart + rowCount );
                            blankCell.setCellFeatures( wcf );
                            blankCell.setCellFormat( getCellFormat2() );
                            sheet0.addCell( blankCell );
                        }
                        else
                        {
                            WritableCell writableCell = null;
                            try
                            {
                                writableCell  = new Number( colStart + colCount, rowStart + rowCount, Double.parseDouble( value ) );
                            }
                            catch( Exception e )
                            {
                                writableCell = new Label( colStart + colCount, rowStart + rowCount, value );
                            }
                            writableCell.setCellFeatures( wcf );
                            writableCell.setCellFormat( getCellFormat2() );
                            sheet0.addCell( writableCell );
                        }
                    }
                    else if( dataElement.getValueType().isBoolean() )
                    {
                        List<String> options = new ArrayList<String>( );
                        options.add( "Yes" );
                        options.add( "No" );
                        WritableCellFeatures wcf = new WritableCellFeatures();
                        wcf.setDataValidationRange( cellEndRange );
                        wcf.setDataValidationList( options );
                        
                        if( includeData == null )
                        {
                            Blank blankCell = new Blank( colStart + colCount, rowStart + rowCount );
                            blankCell.setCellFeatures( wcf );
                            blankCell.setCellFormat( getCellFormat2() );
                            sheet0.addCell( blankCell );
                        }
                        else
                        {
                            WritableCell writableCell = new Label( colStart + colCount, rowStart + rowCount, value );
                            writableCell.setCellFeatures( wcf );
                            writableCell.setCellFormat( getCellFormat2() );                        
                            sheet0.addCell( writableCell );
                        }
                    }
                    else if( dataElement.getValueType().isDate() )
                    {
                        WritableCellFeatures wcf = new WritableCellFeatures();
                        wcf.setComment( "Date Format: YYYY / YYYY-Qn / YYYY-MM e.g. 2012 / 2012-Q1 / 2012-01" );

                        if( includeData == null )
                        {
                            Blank blankCell = new Blank( colStart + colCount, rowStart + rowCount );
                            blankCell.setCellFeatures( wcf );
                            blankCell.setCellFormat( getCellFormat2() );
                            sheet0.addCell( blankCell );
                        }
                        else
                        {
                            WritableCell writableCell = new Label( colStart + colCount, rowStart + rowCount, value );
                            writableCell.setCellFeatures( wcf );
                            writableCell.setCellFormat( getCellFormat2() );                        
                            sheet0.addCell( writableCell );
                        }
                    }
                    else
                    {
                        sheet0.addCell( new Label( colStart + colCount, rowStart + rowCount, "", getCellFormat2() ) );    
                    }
                    
                    sheet0.addCell( new Label( colStart + colCount + 1, rowStart + rowCount, comment, getCellFormat2() ) );
                    sheet0.addCell( new Label( colStart + colCount + 2, rowStart + rowCount, "", getCellFormat2() ) );
                    
                    sheet1.addCell( new Label( colStart + colCount, rowStart + rowCount, "DV#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription(), getCellFormat2() ) );
                    sheet1.addCell( new Label( colStart + colCount + 1, rowStart + rowCount, "C#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription(), getCellFormat2() ) );
                    sheet1.addCell( new Label( colStart + colCount + 2, rowStart + rowCount, "TA#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription(), getCellFormat2() ) );
                    
                    if( cellRangeFlag == 0 )
                    {
                        cellStartRange = (colStart + colCount) + ":" + (rowStart + rowCount);
                        cellRangeFlag = 1;
                    }
                    colCount += 3;
                }
                rowCount++;
            }
        }
        
        //Adding cell start range and cell end range
        cellEndRange = (colStart + colCount - 1) + ":" + (rowStart + rowCount - 1);
        sheet1.addCell( new Label( 0, 0, cellStartRange, getCellFormat1() ) );
        sheet1.addCell( new Label( 1, 0, cellEndRange, getCellFormat1() ) );
        
        sheet0.mergeCells( colStart -1, rowStart -2, colStart - 1 + cellCount * 3, rowStart - 2 );
        sheet0.addCell( new Label( colStart -1, rowStart -2, "Offline Entry Form for the Period: "+periodNames, getCellFormat1() ) );
        sheet1.setHidden( true );
        
        outputReportWorkbook.write();
        outputReportWorkbook.close();

        fileName = "OfflineEntry.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
    
        outputReportFile.deleteOnExit();

        return SUCCESS;
    }
    
    public WritableCellFormat getCellFormat1() throws Exception
    {
        WritableCellFormat wCellformat = new WritableCellFormat();
        
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.LEFT );
        wCellformat.setBackground( Colour.GRAY_25 );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );

        return wCellformat;
    }
    
    public WritableCellFormat getCellFormat2() throws Exception
    {
        WritableCellFormat wCellformat = new WritableCellFormat();

        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.LEFT );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );

        return wCellformat;
    }

    public WritableCellFormat getCellFormat3() throws Exception
    {
        WritableCellFormat wCellformat = new WritableCellFormat();
        
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setBackground( Colour.GRAY_25 );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );
        return wCellformat;
    }

}
