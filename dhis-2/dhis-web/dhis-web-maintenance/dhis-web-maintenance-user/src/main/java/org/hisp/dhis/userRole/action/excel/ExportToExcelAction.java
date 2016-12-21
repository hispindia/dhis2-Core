package org.hisp.dhis.userRole.action.excel;

/*
 * Copyright (c) 2004-2011, University of Oslo
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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.engine.User;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;

/**
 * @author Dang Duy Hieu
 * @version $Id$
 */
public class ExportToExcelAction
    extends ActionPagingSupport<UserAuthorityGroup>
{
    private static final Log log = LogFactory.getLog( ExportToExcelAction.class );

    private static final String EXPORT_FORMAT_EXCEL = "xls";

    private static final String TYPE_USER = "getUserRole";

  

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }
    
    // -------------------------------------------------------------------------
    // Output
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

    private String htmlContent;
    
    public String getHtmlContent()
    {
        return htmlContent;
    }

    public void setHtmlContent( String htmlContent )
    {
        this.htmlContent = htmlContent;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    
    private String key;

    public void setKey( String key )
    {
        this.key = key;
    }
    
    private String type;

    public void setType( String type )
    {
        this.type = type;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

	public String execute()
        throws Exception
    {
		List<UserAuthorityGroup> userAuthorityGroups;
		if ( isNotBlank( key ) ) // Filter on key only if set
        {
            this.paging = createPaging( userService.getUserRoleCountByName( key ) );
            
            userAuthorityGroups = new ArrayList<UserAuthorityGroup>( userService.getUserRolesBetweenByName( key, paging.getStartPos(), paging.getPageSize() ) );
        }
        else
        {
            this.paging = createPaging( userService.getUserRoleCount() );
            
            userAuthorityGroups = new ArrayList<UserAuthorityGroup>( userService.getUserRolesBetween( paging.getStartPos(), paging.getPageSize() ) );
        }		
    	String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator +  "temp";
    	outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

        WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new File( outputReportPath ) );
        WritableSheet sheet0 = outputReportWorkbook.createSheet( "Data Elements", 0 );
        WritableSheet sheet1 = outputReportWorkbook.createSheet( "Authorities", 1 );
        WritableSheet sheet2 = outputReportWorkbook.createSheet( "Users", 2 );
        
        int rowStart = 0;
        int colStart = 1;
        int colCount = 0;
        int rowCount = 0;
        
        int sheet1rowStart = 0;
        int sheet1colStart = 1;
        int sheet1colCount = 0;
        int sheet1rowCount = 0;
        
        int sheet2rowStart = 0;
        int sheet2colStart = 1;
        int sheet2colCount = 0;
        int sheet2rowCount = 0;      
        
        sheet0.addCell( new Label( colStart + colCount - 1, rowStart + rowCount , "User Role " , getCellFormat1() ) );
        sheet0.setColumnView( colStart + colCount - 1, 30 );
        
        sheet0.addCell( new Label( colStart + 1 + colCount - 1, rowStart + rowCount, "Data Element Name", getCellFormat1() ) );
        sheet0.setColumnView( colStart + 1 + colCount - 1, 50 );
        
        sheet0.addCell( new Label( colStart + 2 + colCount - 1, rowStart + rowCount, "Data Set / Area of Work", getCellFormat1() ) );
        sheet0.setColumnView( colStart + 2 + colCount - 1, 50 );
        
        sheet1.addCell( new Label( sheet1colStart + sheet1colCount - 1, sheet1rowStart + sheet1rowCount , "User Role " , getCellFormat1() ) );
        sheet1.setColumnView( sheet1colStart + sheet1colCount - 1, 30 );
        
        sheet1.addCell( new Label( sheet1colStart + 1 + sheet1colCount - 1, sheet1rowStart + sheet1rowCount, "Authority Name", getCellFormat1() ) );
        sheet1.setColumnView( sheet1colStart + 1 + sheet1colCount - 1, 50 );
        
        sheet2.addCell( new Label( sheet2colStart + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, "User Role", getCellFormat1() ) );
        sheet2.setColumnView( sheet2colStart + sheet2colCount - 1, 30 );
        sheet2.addCell( new Label( sheet2colStart + 1 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, "User Id", getCellFormat1() ) );
        sheet2.setColumnView( sheet2colStart + 1 + sheet2colCount - 1, 30 );
        sheet2.addCell( new Label( sheet2colStart + 2 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, "First Name", getCellFormat1() ) );
        sheet2.setColumnView( sheet2colStart + 2 + sheet2colCount - 1, 30 );
        sheet2.addCell( new Label( sheet2colStart + 3 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, "Sur Name", getCellFormat1() ) );
        sheet2.setColumnView( sheet2colStart + 3 + sheet2colCount - 1, 30 );
        
        for( UserAuthorityGroup userAuGrp : userAuthorityGroups )
        {      	
            for(DataElement de : userAuGrp.getDataElements())
            {
            	for(DataSet ds : de.getDataSets())
            	{
	            	rowCount++;            	
	            	sheet0.addCell( new Label( colStart + colCount - 1, rowStart + rowCount, userAuGrp.getName(), getCellFormat2() ) );
	            	sheet0.addCell( new Label( colStart+1 + colCount - 1, rowStart + rowCount, de.getName(), getCellFormat2() ) );
	            	sheet0.addCell( new Label( colStart+2 + colCount - 1, rowStart + rowCount, ds.getName(), getCellFormat2() ) );
            	}
            }            
            for(String authority : userAuGrp.getAuthorities())
            {
            	sheet1rowCount++; 
            	sheet1.addCell( new Label( sheet1colStart + sheet1colCount - 1, sheet1rowStart + sheet1rowCount, userAuGrp.getName(), getCellFormat2() ) );
            	sheet1.addCell( new Label( sheet1colStart+1 + sheet1colCount - 1, sheet1rowStart + sheet1rowCount, authority, getCellFormat2() ) );
            }
            for(UserCredentials userCredentials : userAuGrp.getMembers())
            {
            	sheet2rowCount++;
            	
            	sheet2.addCell( new Label( sheet2colStart + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userAuGrp.getName(), getCellFormat2() ) );
            	sheet2.addCell( new Label( sheet2colStart+1 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userCredentials.getUsername(), getCellFormat2() ) );
            	/*
            	sheet2.addCell( new Label( sheet2colStart+2 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userCredentials.getUser().getFirstName(), getCellFormat2() ) );
            	sheet2.addCell( new Label( sheet2colStart+3 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userCredentials.getUser().getSurname(), getCellFormat2() ) );
            	*/
                sheet2.addCell( new Label( sheet2colStart+2 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userCredentials.getUserInfo().getFirstName(), getCellFormat2() ) );
                sheet2.addCell( new Label( sheet2colStart+3 + sheet2colCount - 1, sheet2rowStart + sheet2rowCount, userCredentials.getUserInfo().getSurname(), getCellFormat2() ) );
            }
            
        }
        outputReportWorkbook.write();
        outputReportWorkbook.close();

        fileName = "User Role Report.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
    
        outputReportFile.deleteOnExit();
        return SUCCESS;
    }
    public WritableCellFormat getCellFormat1() throws Exception
    {
    	WritableFont boldFont = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD);
    	
        WritableCellFormat wCellformat = new WritableCellFormat( boldFont );
        
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
    	WritableFont boldFont = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
    	
        WritableCellFormat wCellformat = new WritableCellFormat( boldFont );
        
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setBackground( Colour.AQUA);
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );
        return wCellformat;
    }

}
