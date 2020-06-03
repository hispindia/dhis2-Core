package org.hisp.dhis.config.action;

import org.hisp.dhis.config.ConfigurationService_IN;
import org.hisp.dhis.config.Configuration_IN;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ConfigurationFormAction implements Action
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
 
    private Configuration_IN mySqlPathConfig;
    
    public Configuration_IN getMySqlPathConfig()
    {
        return mySqlPathConfig;
    }
    
    private Configuration_IN backupPathConfig;
    
    public Configuration_IN getBackupPathConfig()
    {
        return backupPathConfig;
    }
    
    private Configuration_IN reportFolderConfig;
    
    public Configuration_IN getReportFolderConfig()
    {
        return reportFolderConfig;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        /* MYSQL PATH CONFIG */
        mySqlPathConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_MYSQLPATH );        
                        
        if( mySqlPathConfig == null )
        {
            mySqlPathConfig = new Configuration_IN( Configuration_IN.KEY_MYSQLPATH, Configuration_IN.DEFAULT_MYSQLPATH );
            
            configurationService_IN.addConfiguration( mySqlPathConfig );                        
        }
        
        /* MYSQL BACKUP PATH CONFIG */
        backupPathConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_BACKUPDATAPATH );
        
        if( backupPathConfig == null )
        {
            backupPathConfig = new Configuration_IN( Configuration_IN.KEY_BACKUPDATAPATH, Configuration_IN.DEFAULT_BACKUPDATAPATH );
            
            configurationService_IN.addConfiguration( backupPathConfig );                        
        }
        
        /* REPORT FOLDER PATH */
        reportFolderConfig = configurationService_IN.getConfigurationByKey( Configuration_IN.KEY_REPORTFOLDER );
        
        if( reportFolderConfig == null )
        {
            reportFolderConfig = new Configuration_IN( Configuration_IN.KEY_REPORTFOLDER, Configuration_IN.DEFAULT_REPORTFOLDER );
            
            configurationService_IN.addConfiguration( reportFolderConfig );                        
        }        

        return SUCCESS;
    }
}
