package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetTEIAttributeSetailsAction implements Action
{
    
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private Integer selTeiId;

    public void setSelTeiId( Integer selTeiId )
    {
        this.selTeiId = selTeiId;
    }
    
    public Integer getSelTeiId()
    {
        return selTeiId;
    }

    private Map<Integer, String> teiMap = new HashMap<Integer, String>();
    
    public Map<Integer, String> getTeiMap()
    {
        return teiMap;
    }

    private List<Integer> teiIds = new ArrayList<Integer>();
    
    public List<Integer> getTeiIds()
    {
        return teiIds;
    }
    
    private Map<String, String> teiValueMap = new HashMap<String, String>();
    
    public Map<String, String> getTeiValueMap()
    {
        return teiValueMap;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        if ( selTeiId != null )
        {
            String attributeIdsByComma = "89,698,699";
            Integer teNameAttributeId = 89;
            
            teiValueMap = new HashMap<String, String>( getTrackedEntityInstanceAttributeValuesByAttributeIds( attributeIdsByComma ));
            teiMap = new HashMap<Integer, String>( getTrackedEntityInstanceAttributeValuesByAttributeId( teNameAttributeId ) );
            teiIds = new ArrayList<Integer>( teiMap.keySet());
            
        }
        
        return SUCCESS;
    }
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
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
 
    
    //--------------------------------------------------------------------------------
    // Get Tracked Entity Instance Attribute Values by attributeIds  
    //--------------------------------------------------------------------------------
    public Map<Integer, String> getTrackedEntityInstanceAttributeValuesByAttributeId( Integer attributeId )
    {
        Map<Integer, String> teiValueMap = new HashMap<Integer, String>();


        //SELECT trackedentityinstanceid, value FROM trackedentityattributevalue where trackedentityattributeid IN( 89 ) order by value;
        
        try
        {
            String query = "SELECT trackedentityinstanceid, value FROM trackedentityattributevalue " +
                            "WHERE trackedentityattributeid IN ( "+ attributeId +")";
          
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                String teiAttributeValue = rs.getString( 2 );
                
                if ( teiAttributeValue != null )
                {
                    teiValueMap.put( teiId, teiAttributeValue );
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
