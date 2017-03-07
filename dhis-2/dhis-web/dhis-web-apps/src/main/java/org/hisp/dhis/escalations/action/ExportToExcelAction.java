package org.hisp.dhis.escalations.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.opensymphony.xwork2.Action;
/**
 * @author Mithilesh Kumar Thakur
 */
public class ExportToExcelAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Input & output
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

    private String htmlCode;

    public void setHtmlCode( String htmlCode )
    {
        this.htmlCode = htmlCode;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute() throws Exception
    {
        fileName = "EscalationReport.xls";

        inputStream = new BufferedInputStream( new ByteArrayInputStream( htmlCode.getBytes("UTF-8") ) );

        return SUCCESS;
    }
}
