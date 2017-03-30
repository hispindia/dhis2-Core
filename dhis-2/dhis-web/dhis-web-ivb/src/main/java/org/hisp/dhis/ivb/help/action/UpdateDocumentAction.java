package org.hisp.dhis.ivb.help.action;

/*
 * Copyright (c) 2004-2013, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.document.Document;
import org.hisp.dhis.document.DocumentService;
import org.hisp.dhis.external.location.LocationManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class UpdateDocumentAction
    implements Action
{
    private static final Log log = LogFactory.getLog( UpdateDocumentAction.class );
    
    private static final String HTTP_PREFIX = "http://";

    private static final String HTTPS_PREFIX = "https://";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private LocationManager locationManager;

    public void setLocationManager( LocationManager locationManager )
    {
        this.locationManager = locationManager;
    }

    private DocumentService documentService;

    public void setDocumentService( DocumentService documentService )
    {
        this.documentService = documentService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer documentId;

    public void setDocumentId( Integer documentId )
    {
        this.documentId = documentId;
    }

    private String documentFileName;

    public void setDocumentFileName( String documentFileName )
    {
        this.documentFileName = documentFileName;
    }

    private String fileUrl;

    public void setFileUrl( String fileUrl )
    {
        this.fileUrl = fileUrl;
    }

    private Boolean attachment1 = true;

    public void setAttachment1( Boolean attachment1 )
    {
        this.attachment1 = attachment1;
    }

    private File file1;

    public void setFileupload( File file1 )
    {
        this.file1 = file1;
    }

    private String fileName;

    public void setFileuploadFileName( String fileName )
    {
        this.fileName = fileName;
    }

    private String contentType;

    public void setFileuploadContentType( String contentType )
    {
        this.contentType = contentType;
    }
    private Boolean external;

    public void setExternal( Boolean external )
    {
        this.external = external;
    }

    private Boolean attachment = false;

    public void setAttachment( Boolean attachment )
    {
        this.attachment = attachment;
    }
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        Document document = documentService.getDocument( documentId );
        if ( file1 != null )
        {
            log.info( "Uploading file: '" + fileName + "', content-type: '" + contentType + "'" );
    
            File destination = locationManager.getFileForWriting( fileName, DocumentService.DIR );
    
            log.info( "Destination: '" + destination.getAbsolutePath() + "'" );
    
            //boolean fileMoved = file1.renameTo( destination );
    
           /* if ( !fileMoved )
            {
                throw new RuntimeException( "File could not be moved to: '" + destination.getAbsolutePath() + "'" );
            }*/
            
            fileUrl = fileName;
            document.setUrl( fileUrl );
            document.setContentType( contentType );
        }
        document.setAttachment( attachment );
            
        document.setName( documentFileName );
            
            
        documentService.saveDocument( document );

        return SUCCESS;
    }

}
