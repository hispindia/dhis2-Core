package org.hisp.dhis.ivb.dataentry.dewise.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.option.Option;
import org.hisp.dhis.option.OptionSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;

import com.opensymphony.xwork2.Action;

import jxl.write.WritableCellFeatures;

public class DownloadOfflineXLSheetAction implements Action
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

    private String countryAsRows;
    
    public void setCountryAsRows(String countryAsRows) 
    {
		this.countryAsRows = countryAsRows;
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
        
        
        Set<DataSet> dataSetList = new HashSet<DataSet>();
        
        if( selectedListDataset == null )
        { }
        
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
        
        List<String> tempPeriodNames = new ArrayList<String>();
        
        Period period = ivbUtil.getCurrentPeriod( new QuarterlyPeriodType(), new Date() );
        
        if( tempPeriodNames.contains( period.getName() ) )
        {
            
        }
        else
        {
            periodNames += period.getName() + " ";
            tempPeriodNames.add( period.getName() );
        }
        String curPeriod = period.getDescription();
        /*
        if( curPeriod.contains( "Q" ) )
        {
        	curPeriod = curPeriod.replace( "Q", "-Q" );
        }
        */
        
        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  "temp";
        File newdir = new File( outputReportPath );
        if( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet0 = workbook.createSheet("OfflineEntry");
        HSSFSheet sheet1 = workbook.createSheet("OfflineEntryInput");

        if( countryAsRows != null )
        {
	        CreationHelper factory = workbook.getCreationHelper();
	        Drawing drawing = sheet0.createDrawingPatriarch();
	        ClientAnchor anchor = factory.createClientAnchor();
	
	        int rowStart = 2;
	        int colStart = 1;
	        
	        int cellCount = 0;
	        int colCount = 0;
	        int rowCount = 0;
	        
	        String cellStartRange = "";
	        String cellEndRange = "";
	        int cellRangeFlag = 0;
	        
	        //Row row = sheet0.createRow( rowStart );
	        //sheet0.addMergedRegion( new CellRangeAddress( rowStart, rowStart + 1, colStart + colCount - 1, colStart + colCount - 1 ) );        
	        //Cell cell = row.createCell( colStart + colCount - 1 );
	        //cell.setCellValue( "Key Indicator" );
	        HSSFFont font = workbook.createFont();
	        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        
	        HSSFCellStyle cellStyle = workbook.createCellStyle();
	        cellStyle.setFont(font);
	        cellStyle.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );
	        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        //cell.setCellStyle( cellStyle );
	        
	        HSSFCellStyle cellStyle1 = workbook.createCellStyle();
	        cellStyle1.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyle1.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyle1.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyle1.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyle1.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        
	        //sheet0.setColumnWidth( colStart + colCount - 1, 50*256 );
	
	        Row sheet1Row = sheet0.createRow( rowStart -2 );
	        sheet0.addMergedRegion( new CellRangeAddress( rowStart -2, rowStart - 2,  colStart + colCount, colStart + colCount + 3 ) );
	        Cell cell = sheet1Row.createCell( colStart + colCount );
	        cell.setCellValue( "Offline Entry Form for the Period: "+periodNames );

	        Row row1 = sheet0.createRow( rowStart + rowCount + 1 );
	        cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Region" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 30*256 );
            colCount++;

            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "ISOCode" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 15*256 );
            colCount++;

	        cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Country" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 30*256 );
            colCount++;


            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "CountryCode" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 5*256 );
            colCount++;
            
            

            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Data Element" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 50*256 );
            colCount++;

            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "IndicatorCode" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount , 5*256 );
            colCount++;

            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Period" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount , 10*256 );
            colCount++;

            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Value" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 30*256 );
            colCount++;
            
            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "Comment" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 30*256 );
            colCount++;
            
            cell = row1.createCell( colStart + colCount );
            cell.setCellValue( "TA" );
            cell.setCellStyle( cellStyle );
            sheet0.setColumnWidth( colStart + colCount, 30*256 );

            if( includeTA == null )
            {
                sheet0.setColumnHidden( colStart + colCount, true );
            }
	
            rowCount += 2;         
            colCount = 0;
            cellStartRange = (colStart + colCount) + ":" + (rowStart + rowCount);
            
	        for( OrganisationUnit orgUnit : orgUnitList )
	        {
	        	for( DataElement dataElement : dataElementList )
	        	{
	        		colCount = 0;
	        		Row row2 = sheet0.createRow( rowStart + rowCount );
	        	
	                cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( orgUnit.getParent().getName() );
	                cell.setCellStyle( cellStyle1 );
	                colCount++;

	                cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( orgUnit.getCode() );
	                cell.setCellStyle( cellStyle1 );
	                colCount++;

	        		cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( orgUnit.getName() );
	                cell.setCellStyle( cellStyle1 );
	                colCount++;

	                cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( orgUnit.getId() );
	                cell.setCellStyle( cellStyle1 );
	                sheet0.setColumnHidden( colStart + colCount, true );
	                colCount++;

	        		cell = row2.createCell( colStart + colCount );
	        		cell.setCellValue( dataElement.getName() );
	        		cell.setCellStyle( cellStyle1 );
	        		colCount++;

	        		cell = row2.createCell( colStart + colCount );
	        		cell.setCellValue( dataElement.getId() );
	        		cell.setCellStyle( cellStyle1 );
	        		sheet0.setColumnHidden( colStart + colCount, true );
	        		colCount++;

	        		cell = row2.createCell( colStart + colCount );
	        		cell.setCellValue( curPeriod );
	        		cell.setCellStyle( cellStyle1 );
	        		sheet0.setColumnHidden( colStart + colCount, true );
	        		colCount++;

	        		OptionSet optionSet = dataElement.getOptionSet();

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
	                    List<String> optionCodes = new ArrayList<String>( );
	                    for( Option option : optionObjs )
	                    {
	                    	optionCodes.add( option.getCode() );
	                    }
	                    
	                    if( includeData == null )
	                    {                         
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );	                        
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( optionCodes.toArray( new String[0] ) );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                        colCount++;
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellValue( value );
	                        cell.setCellStyle( cellStyle1 );	                        
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( optionCodes.toArray( new String[0] ) );
	                        DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                        colCount++;
	                    }
	                }
	                else if( dataElement.getValueType().isInteger() )
	                {
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );	                        
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount );
	                        DVConstraint dvConstraint = DVConstraint.createNumericConstraint( DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.BETWEEN, "-9999999", "9999999");
	                        DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                        sheet0.addValidationData(dataValidation);
	                        colCount++;
	                    }
	                    else
	                    {
	                        try
	                        {
	                            cell = row2.createCell( colStart + colCount );
	                            cell.setCellValue( Double.parseDouble( value ) );
	                            cell.setCellStyle( cellStyle1 );	                            
	                            CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount );
	                            DVConstraint dvConstraint = DVConstraint.createNumericConstraint( DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.BETWEEN, "-9999999", "9999999");
	                            DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                            sheet0.addValidationData(dataValidation);
	                            colCount++;
	                        }
	                        catch( Exception e )
	                        {
	                            cell.setCellValue( value );
	                            cell.setCellStyle( cellStyle1 );
	                            colCount++;
	                        }
	                    }
	                }
	                else if( dataElement.getValueType().isBoolean() )
	                {
	                    List<String> options = new ArrayList<String>( );
	                    options.add( "Yes" );
	                    options.add( "No" );
	                    WritableCellFeatures wcf = new WritableCellFeatures();
	                    wcf.setDataValidationList( options );
	                    
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );	                        
	                        CellRangeAddressList addressList = new CellRangeAddressList(rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( new String[]{"Yes","No"} );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                        colCount++;
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        cell.setCellValue( value );	                        
	                        CellRangeAddressList addressList = new CellRangeAddressList(rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( new String[]{"Yes","No"} );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                        colCount++;
	                    }
	                }
	                else if( dataElement.getValueType().isDate() )
	                {
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );	                        
	                        
	                        anchor.setCol1( cell.getColumnIndex() );
	                        anchor.setCol2( cell.getColumnIndex() + 3 );
	                        anchor.setRow1( row2.getRowNum() );
	                        anchor.setRow2( row2.getRowNum() + 3 );
	                        
	                        Comment cellComment = drawing.createCellComment( anchor );
	                        RichTextString str = factory.createRichTextString( "Date Format: YYYY / YYYY-Qn / YYYY-MM e.g. 2012 / 2012-Q1 / 2012-01" );
	                        cellComment.setString(str);
	
	                        cell.setCellComment( cellComment );
	                        colCount++;
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );	                        
	
	                        anchor.setCol1( cell.getColumnIndex() );
	                        anchor.setCol2( cell.getColumnIndex() + 3 );
	                        anchor.setRow1( row2.getRowNum() );
	                        anchor.setRow2( row2.getRowNum() + 3 );
	                     
	
	                        Comment cellComment = drawing.createCellComment( anchor );
	                        RichTextString str = factory.createRichTextString( "Date Format: YYYY / YYYY-Qn / YYYY-MM e.g. 2012 / 2012-Q1 / 2012-01" );
	                        cellComment.setString(str);
	
	                        cell.setCellComment( cellComment );
	                        cell.setCellValue( value );
	                        colCount++;
	                    }
	                }
	                else
	                {
	                    cell = row2.createCell( colStart + colCount );    
	                    cell.setCellStyle( cellStyle1 );
	                    colCount++;
	                }
	                
	                //cell = row2.createCell( colStart + colCount );
	                //cell.setCellValue( value );
	                
	                cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( comment );
	                cell.setCellStyle( cellStyle1 );
	                colCount++;
	                
	                cell = row2.createCell( colStart + colCount );
	                cell.setCellValue( "" );
	                cell.setCellStyle( cellStyle1 );
	                colCount++;
	
	                /*
	                cell = sheet2Row.createCell( colStart + colCount );
	                cell.setCellValue( "DV#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	
	                cell = sheet2Row.createCell( colStart + colCount + 1 );
	                cell.setCellValue( "C#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	
	                cell = sheet2Row.createCell( colStart + colCount + 2 );
	                cell.setCellValue( "TA#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	                */
	                
	                //colCount += 3;
	            
	                rowCount++;
	        	}
	        }
	        
	        //Adding cell start range and cell end range
	        
	        cellEndRange = (colStart + colCount - 1) + ":" + (rowStart + rowCount - 1);
	        Row sheet2Row = sheet1.createRow( 0 );
	        cell = sheet2Row.createCell( 0 );
	        cell.setCellValue( cellStartRange );	        
	        cell = sheet2Row.createCell( 1 );
	        cell.setCellValue( cellEndRange );
	        //sheet1.setHidden( true );
	        
	        /*
	        sheet0.addMergedRegion( new CellRangeAddress( rowStart -2, rowStart - 2,  colStart -1, colStart - 1 + cellCount * 3 ) );
	        Row sheet1Row = sheet0.createRow( rowStart -2 );
	        cell = sheet1Row.createCell( colStart -1 );
	        cell.setCellValue( "Offline Entry Form for the Period: "+periodNames );
	        */
	        
        }
        else
        {
	        CreationHelper factory = workbook.getCreationHelper();
	        Drawing drawing = sheet0.createDrawingPatriarch();
	        ClientAnchor anchor = factory.createClientAnchor();
	
	        int rowStart = 2;
	        int colStart = 2;
	        
	        int cellCount = 0;
	        int colCount = 0;
	        int rowCount = 0;
	        
	        String cellStartRange = "";
	        String cellEndRange = "";
	        int cellRangeFlag = 0;
	        
	        Row row = sheet0.createRow( rowStart );
	        sheet0.addMergedRegion( new CellRangeAddress( rowStart, rowStart + 1, colStart + colCount - 1, colStart + colCount - 1 ) );        
	        Cell cell = row.createCell( colStart + colCount - 1 );
	        cell.setCellValue( "Key Indicator" );
	        HSSFFont font = workbook.createFont();
	        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        
	        HSSFCellStyle cellStyle = workbook.createCellStyle();
	        cellStyle.setFont(font);
	        cellStyle.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );
	        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        cell.setCellStyle( cellStyle );
	        
	        HSSFCellStyle cellStyle1 = workbook.createCellStyle();
	        cellStyle1.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyle1.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyle1.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyle1.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyle1.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyle1.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        
	        sheet0.setColumnWidth( colStart + colCount - 1, 50*256 );
	
	        Row row1 = sheet0.createRow( rowStart + rowCount+1 );
	        for( OrganisationUnit orgUnit : orgUnitList )
	        {
	            sheet0.addMergedRegion( new CellRangeAddress( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount + 2 ) );  
	            cell = row.createCell( colStart + colCount );
	            cell.setCellValue( orgUnit.getName() );
	            cell.setCellStyle( cellStyle );
	
	            cell = row1.createCell( colStart + colCount );
	            cell.setCellValue( "Value" );
	            cell.setCellStyle( cellStyle );
	
	            sheet0.setColumnWidth( colStart + colCount, 30*256 );
	            
	            cell = row1.createCell( colStart + colCount + 1 );
	            cell.setCellValue( "Comment" );
	            cell.setCellStyle( cellStyle );
	
	            
	            sheet0.setColumnWidth( colStart + colCount + 1, 30*256 );
	            
	            cell = row1.createCell( colStart + colCount + 2 );
	            cell.setCellValue( "TA" );
	            cell.setCellStyle( cellStyle );
	
	            sheet0.setColumnWidth( colStart + colCount + 2, 30*256 );
	            if( includeTA == null )
	            {
	                sheet0.setColumnHidden( colStart + colCount + 2, true );
	            }
	
	            colCount += 3;
	            cellCount++;
	        }
	        
	        rowCount += 2;
	        colCount = -1;        
	        
	        for( DataElement dataElement : dataElementList )
	        {
	           
	            colCount = -1;
	            Row row2 = sheet0.createRow( rowStart + rowCount );
	            cell = row2.createCell( colStart + colCount );
	            cell.setCellValue( dataElement.getName() );
	            cell.setCellStyle( cellStyle );
	            
	            Row sheet2Row = sheet1.createRow( rowStart + rowCount );
	            
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
	                    
	                    if( includeData == null )
	                    {                         
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( optionCodes.toArray( new String[0] ) );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellValue( value );
	                        cell.setCellStyle( cellStyle1 );
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( optionCodes.toArray( new String[0] ) );
	                        DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                    }
	                }
	                else if( dataElement.getValueType().isInteger() )
	                {
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount );
	                        DVConstraint dvConstraint = DVConstraint.createNumericConstraint( DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.BETWEEN, "-9999999", "9999999");
	                        DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                        sheet0.addValidationData(dataValidation);
	                    }
	                    else
	                    {
	                        try
	                        {
	                            cell = row2.createCell( colStart + colCount );
	                            cell.setCellValue( Double.parseDouble( value ) );
	                            cell.setCellStyle( cellStyle1 );
	                            CellRangeAddressList addressList = new CellRangeAddressList( rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount );
	                            DVConstraint dvConstraint = DVConstraint.createNumericConstraint( DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.BETWEEN, "-9999999", "9999999");
	                            DataValidation dataValidation = new HSSFDataValidation( addressList, dvConstraint );
	                            sheet0.addValidationData(dataValidation);
	                        }
	                        catch( Exception e )
	                        {
	                            cell.setCellValue( value );
	                            cell.setCellStyle( cellStyle1 );
	                        }
	                    }
	                }
	                else if( dataElement.getValueType().isBoolean() )
	                {
	                    List<String> options = new ArrayList<String>( );
	                    options.add( "Yes" );
	                    options.add( "No" );
	                    WritableCellFeatures wcf = new WritableCellFeatures();
	                    wcf.setDataValidationList( options );
	                    
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        CellRangeAddressList addressList = new CellRangeAddressList(rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( new String[]{"Yes","No"} );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        cell.setCellValue( value );
	                        CellRangeAddressList addressList = new CellRangeAddressList(rowStart + rowCount, rowStart + rowCount, colStart + colCount, colStart + colCount);
	                        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint( new String[]{"Yes","No"} );
	                        DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
	                        dataValidation.setSuppressDropDownArrow( false );
	                        sheet0.addValidationData( dataValidation );
	                    }
	                }
	                else if( dataElement.getValueType().isDate() )
	                {
	                    if( includeData == null )
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	                        
	                        anchor.setCol1( cell.getColumnIndex() );
	                        anchor.setCol2( cell.getColumnIndex() + 3 );
	                        anchor.setRow1( row2.getRowNum() );
	                        anchor.setRow2( row2.getRowNum() + 3 );
	                        
	                        Comment cellComment = drawing.createCellComment( anchor );
	                        RichTextString str = factory.createRichTextString( "Date Format: YYYY / YYYY-Qn / YYYY-MM e.g. 2012 / 2012-Q1 / 2012-01" );
	                        cellComment.setString(str);
	
	                        cell.setCellComment( cellComment );
	                    }
	                    else
	                    {
	                        cell = row2.createCell( colStart + colCount );
	                        cell.setCellStyle( cellStyle1 );
	
	                        anchor.setCol1( cell.getColumnIndex() );
	                        anchor.setCol2( cell.getColumnIndex() + 3 );
	                        anchor.setRow1( row2.getRowNum() );
	                        anchor.setRow2( row2.getRowNum() + 3 );
	                     
	
	                        Comment cellComment = drawing.createCellComment( anchor );
	                        RichTextString str = factory.createRichTextString( "Date Format: YYYY / YYYY-Qn / YYYY-MM e.g. 2012 / 2012-Q1 / 2012-01" );
	                        cellComment.setString(str);
	
	                        cell.setCellComment( cellComment );
	                        cell.setCellValue( value );
	                    }
	                }
	                else
	                {
	                    cell = row2.createCell( colStart + colCount );    
	                    cell.setCellStyle( cellStyle1 );
	                }
	                
	                
	                //cell = row2.createCell( colStart + colCount );
	                //cell.setCellValue( value );
	                
	                cell = row2.createCell( colStart + colCount + 1 );
	                cell.setCellValue( comment );
	                cell.setCellStyle( cellStyle1 );
	                
	                cell = row2.createCell( colStart + colCount + 2 );
	                cell.setCellValue( "" );
	                cell.setCellStyle( cellStyle1 );
	
	                cell = sheet2Row.createCell( colStart + colCount );
	                cell.setCellValue( "DV#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	
	                cell = sheet2Row.createCell( colStart + colCount + 1 );
	                cell.setCellValue( "C#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	
	                cell = sheet2Row.createCell( colStart + colCount + 2 );
	                cell.setCellValue( "TA#"+orgUnit.getId()+"#"+dataElement.getId()+"#"+period.getDescription() );
	                cell.setCellStyle( cellStyle1 );
	
	                if( cellRangeFlag == 0 )
	                {
	                    cellStartRange = (colStart + colCount) + ":" + (rowStart + rowCount);
	                    cellRangeFlag = 1;
	                }
	                colCount += 3;
	            }
	            rowCount++;
	        }
	        
	        //Adding cell start range and cell end range
	        cellEndRange = (colStart + colCount - 1) + ":" + (rowStart + rowCount - 1);
	        Row sheet2Row = sheet1.createRow( 0 );
	        cell = sheet2Row.createCell( 0 );
	        cell.setCellValue( cellStartRange );
	        
	        cell = sheet2Row.createCell( 1 );
	        cell.setCellValue( cellEndRange );
	
	        sheet0.addMergedRegion( new CellRangeAddress( rowStart -2, rowStart - 2,  colStart -1, colStart - 1 + cellCount * 3 ) );
	        Row sheet1Row = sheet0.createRow( rowStart -2 );
	        cell = sheet1Row.createCell( colStart -1 );
	        cell.setCellValue( "Offline Entry Form for the Period: "+periodNames );
	        //sheet1.setHidden( true );                	
        }
        
        try 
        {
            FileOutputStream out = new FileOutputStream( new File( outputReportPath ) );
            
            sheet1.protectSheet( "IVB" );
            
            workbook.setSheetHidden( 1, true );
            
            workbook.write( out );
            out.close();
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        fileName = "OfflineEntry.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
    
        outputReportFile.deleteOnExit();

        return SUCCESS;
    }      

}
