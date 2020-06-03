package org.hisp.dhis.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultConfigurationService_IN
    implements ConfigurationService_IN
{
    private static final Log log = LogFactory.getLog( DefaultConfigurationService_IN.class );

    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ConfigurationStore_IN configurationStore_IN;

    public void setConfigurationStore_IN( ConfigurationStore_IN configurationStore_IN )
    {
        this.configurationStore_IN = configurationStore_IN;
    }
    
    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------


    @Override
    
    public int addConfiguration( Configuration_IN con )
    {        
        
        return configurationStore_IN.addConfiguration( con );    
    }
    @Override

    public void updateConfiguration( Configuration_IN con )
    {
        configurationStore_IN.updateConfiguration( con );
    }
    @Override

    public void deleteConfiguration( Configuration_IN con )
    {
        configurationStore_IN.deleteConfiguration( con );
    }
    @Override

    public Configuration_IN getConfiguration( int id )
    {
        return configurationStore_IN.getConfiguration( id );       
    }
    @Override

    public Configuration_IN getConfigurationByKey( String ckey )
    {        
        return configurationStore_IN.getConfigurationByKey( ckey );
    }
    
    // -------------------------------------------------------------------------
    // 
    // -------------------------------------------------------------------------

    @Override

    public boolean clearXfolder( String folderPath )
    {
        try
        {
            File dir = new File( folderPath );
            String[] files = dir.list();        
            for ( String file : files )
            {
                file = folderPath + File.separator + file;
                File tempFile = new File(file);
                tempFile.delete();
            }
            
            return true;
        }
        catch(Exception e)
        {
            log.error(e);
            return false;
        }        
    }
    
    @Override

    public String backupFolder( String folderPath )
    {
               
        Calendar curDateTime = Calendar.getInstance();
        Date curDate = new Date();                
        curDateTime.setTime( curDate );
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "ddMMMyyyy-HHmmssSSS" );
                
        String tempFolderName = simpleDateFormat.format( curDate );
        
        String zipFilePath = getConfigurationByKey( Configuration_IN.KEY_BACKUPDATAPATH ).getValue();
        zipFilePath += tempFolderName;

        File newdir = new File( zipFilePath );
        if( !newdir.exists() )
            newdir.mkdirs();

        //zipFilePath = zipFilePath.substring( 0, zipFilePath.lastIndexOf( "/" ) );

        zipFilePath += "/mi.zip";
        
        log.debug( "zipFilePath: "+ zipFilePath );

        ZipOutputStream out = null;

        try
        {
            File inFolder = new File( folderPath );
            File outFolder = new File( zipFilePath );
            out = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream(outFolder) ) );   
           
            addDirectoryForCompressing( inFolder, out );
            /*
            BufferedInputStream in = null;
            byte[] data    = new byte[1024];
            String files[] = inFolder.list();
            
            for (int i=0; i<files.length; i++)
            {
                in = new BufferedInputStream( new FileInputStream( inFolder.getPath() + "/" + files[i] ), 1024 );                  
                out.putNextEntry( new ZipEntry(files[i]) ); 
                int count;
                while( (count = in.read(data,0,1024)) != -1 )
                {
                    out.write(data, 0, count);
                }
                out.closeEntry();
            }
            */
            
            return zipFilePath;
         }
         catch(Exception e)
         {
           log.error( e );
           
           return "INPUT";
         }
         finally
         {
             try
             {
                 out.flush();
                 out.close();
             }
             catch( Exception e )
             {
                 log.error( "Exception trying to close output stream", e );
             }
         }
    }
    
    

    public void addDirectoryForCompressing( File dirObj, ZipOutputStream out )
    {
        try
        {
            File[] files = dirObj.listFiles();
            byte[] tmpBuf = new byte[1024];
    
            for( int i = 0; i < files.length; i++ ) 
            {
              if (files[i].isDirectory()) 
              {
                  addDirectoryForCompressing(files[i], out);
                  continue;
              }
              
              FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
              out.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
              int len;
              while( (len = in.read(tmpBuf)) > 0) 
              {
                out.write(tmpBuf, 0, len);
              }
              out.closeEntry();
              in.close();
            }
        }
        catch(Exception e)
        {
          log.error( "Should handle these file stream better", e );
        } 
    }

}
