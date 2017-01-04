package org.hisp.dhis.dataimport.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * Created by ganesh on 11/3/15.
 */
public class AggregateDataImportAction
    implements Action
{
    
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private OrganisationUnitService organisationUnitService;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private List<String> keyCodeList = new ArrayList<String>();
    
    public List<String> getKeyCodeList()
    {
        return keyCodeList;
    }
    
    private Map<String, String> uidMap = new HashMap<String, String>();
    
    public Map<String, String> getUidMap()
    {
        return uidMap;
    }

    private String XMLcontent = "";

    public String getXMLcontent()
    {
        return XMLcontent;
    }

    private List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>();
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    
    @Override
    public String execute()
        throws Exception
    {

        System.out.println( "inside java class" );
        
        String path = System.getenv( "DHIS2_HOME" ) + File.separator + "excelimport" + File.separator;
        
        organisationUnits.addAll( organisationUnitService.getAllOrganisationUnits() );

        for ( OrganisationUnit organisationUnit : organisationUnits )
        {

            // System.out.println(organisationUnit.getCode() + " " +
            // organisationUnit.getUid());

            if ( organisationUnit.getCode() != null )
            {
                keyCodeList.add( organisationUnit.getCode() );
                uidMap.put( organisationUnit.getCode(), organisationUnit.getUid() );
            }
        }

        BufferedReader in = null;
        try
        {
            String filename = path + "aggregateCcmp.xml";

            in = new BufferedReader( new FileReader( filename ) );
            while ( in.ready() )
            {
                XMLcontent += in.readLine();
            }
            // System.out.println(XMLcontent);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            in.close();
        }

        return SUCCESS;
    }

}
