package org.hisp.dhis.config.action;

import org.hisp.dhis.config.ConfigurationService_IN;
import org.hisp.dhis.config.Configuration_IN;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ConfigurationResultAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ConfigurationService_IN configurationService_IN;

    /*
    public void setConfigurationService_IN( ConfigurationService_IN configurationService_IN )
    {
        this.configurationService_IN = configurationService_IN;
    }
    */
    
    
    // -------------------------------------------------------------------------
    // Input and Output Parameters
    // -------------------------------------------------------------------------
    
    private String mysqlPath;

    public void setMysqlPath( String mysqlPath )
    {
        this.mysqlPath = mysqlPath;
    }

    private String backupDataPath;

    public void setBackupDataPath( String backupDataPath )
    {
        this.backupDataPath = backupDataPath;
    }

    private String reportFolder;
    
    public void setReportFolder( String reportFolder )
    {
        this.reportFolder = reportFolder;
    }

    private Configuration_IN mySqlPathConfig;
       
    private Configuration_IN backupPathConfig;
    
    private Configuration_IN reportFolderConfig;
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {

        /* MYSQL PATH CONFIG */
        mySqlPathConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_MYSQLPATH );        

        if(mysqlPath == null || mysqlPath.trim().equals( "" ))
        {
            mysqlPath = Configuration_IN.DEFAULT_MYSQLPATH;
        }
        
        if( mySqlPathConfig == null )
        {
            mySqlPathConfig = new Configuration_IN( Configuration_IN.KEY_MYSQLPATH, mysqlPath );
            
            configurationService_IN.addConfiguration( mySqlPathConfig );                        
        }
        else
        {
            mySqlPathConfig.setValue( mysqlPath );
            
            configurationService_IN.updateConfiguration( mySqlPathConfig );
        }
        
        /* MYSQL BACKUP PATH CONFIG */
        backupPathConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_BACKUPDATAPATH );
        
        if(backupDataPath == null || backupDataPath.trim().equals( "" ))
        {
            backupDataPath = Configuration_IN.DEFAULT_BACKUPDATAPATH;
        }
                
        if( backupPathConfig == null )
        {
            backupPathConfig = new Configuration_IN( Configuration_IN.KEY_BACKUPDATAPATH, backupDataPath );
            
            configurationService_IN.addConfiguration( backupPathConfig );                        
        }
        else
        {
            backupPathConfig.setValue( backupDataPath );
            
            configurationService_IN.updateConfiguration( backupPathConfig );
        }
        
        /* REPORT FOLDER PATH */
        reportFolderConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER );
        
        if(reportFolder == null || reportFolder.trim().equals( "" ))
        {
            reportFolder = Configuration_IN.DEFAULT_REPORTFOLDER;
        }
        
        if( reportFolderConfig == null )
        {
            reportFolderConfig = new Configuration_IN( Configuration_IN.KEY_REPORTFOLDER, reportFolder );
            
            configurationService_IN.addConfiguration( reportFolderConfig );                        
        }        
        else
        {
            reportFolderConfig.setValue( reportFolder );
            
            configurationService_IN.updateConfiguration( reportFolderConfig );
        }
        
        return SUCCESS;
    }

}
