package org.hisp.dhis.schedulinginspections.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduledInspectionService
{
    // ---------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    // -------------------------------------------------------------------------
    // Support Methods Defination
    // -------------------------------------------------------------------------    
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by Patient Ids
    //--------------------------------------------------------------------------------
    public Map<String, String> getTrackedEntityInstanceAttributeValuesByAttributeIds( String attributeIdsByComma )
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

//        SELECT trackedentityinstanceid, trackedentityattributeid, created, lastupdated, 
//        value, encryptedvalue
//        FROM trackedentityattributevalue where trackedentityattributeid IN( 89,698,699) order by trackedentityattributeid;
        
        try
        {
            String query = "SELECT trackedentityinstanceid, trackedentityattributeid, value FROM trackedentityattributevalue " +
                            "WHERE trackedentityattributeid IN ( "+ attributeIdsByComma +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                Integer teiAttributeId = rs.getInt( 2 );                
                String teiAttributeValue = rs.getString( 3 );
                
                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId+ ":" +teiAttributeId, teiAttributeValue );
                }
            }

            return teiValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }    

    
    public Map<String, String> getTrackedEntityInstanceAttributeValues()
    {
        Map<String, String> teiValueMap = new HashMap<String, String>();

//        SELECT trackedentityinstanceid, trackedentityattributeid, created, lastupdated, 
//        value, encryptedvalue
//        FROM trackedentityattributevalue where trackedentityattributeid IN( 89,698,699) order by trackedentityattributeid;
        
        try
        {
            String query = "SELECT trackedentityinstanceid, trackedentityattributeid, value FROM trackedentityattributevalue;";
          
            System.out.println( " query -- " + query );
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                Integer teiAttributeId = rs.getInt( 2 );                
                String teiAttributeValue = rs.getString( 3 );
                
                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId+ ":" +teiAttributeId, teiAttributeValue );
                }
            }

            return teiValueMap;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }    
    
    
    
    
    
    
    
    
    
    
    
    
}




