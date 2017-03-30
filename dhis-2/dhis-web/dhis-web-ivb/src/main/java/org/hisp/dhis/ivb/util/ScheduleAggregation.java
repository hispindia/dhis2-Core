package org.hisp.dhis.ivb.util;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleAggregation implements Action
{
    private final static int VACCINE_INTRO_DE_GROUPSET = 1;
    private final static int  VACCINE_INTRO_DE_GROUP = 40;
    
    public static final String XTRA_ORGUNIT_ID = "XTRA_ORGUNIT_ID";//208
    
    //public static final String VACCINE_INTRO_DE_GROUPSET_ID = "VACCINE_INTRO_DE_GROUPSET_ID";//1
    public static final String VACCINE_INTRO_DE_GROUP_ID = "VACCINE_INTRO_DE_GROUP_ID";//40
    public static final String ORGUNIT_SVALBARD_AND_JAN_MAYEN_ID = "ORGUNIT_SVALBARD_AND_JAN_MAYEN_ID";//217
    public static final String ORGUNIT_GREENLAND_ID = "ORGUNIT_GREENLAND_ID";//211
    public static final String ORGUNIT_DENMARK_ID = "ORGUNIT_DENMARK_ID";//90
    public static final String ORGUNIT_NORWAY_ID = "ORGUNIT_NORWAY_ID";//111
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DeMappingService demappingService;
    
    @Autowired
    private DataValueService dataValueService;
    
    @Autowired
    private DataElementCategoryService categoryService;
    
    @Autowired
    private PeriodService periodService;
    
    @Autowired
    private ConstantService constantService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private CurrentUserService currentUserService;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
 
    // -------------------------------------------------------------------------
    // Action IMplementation
    // -------------------------------------------------------------------------
    
    
    public String execute() throws Exception
    {
        System.out.println("INFO: scheduler job has started at : " + new Date() );
        //String userName = currentUserService.getCurrentUser().getUsername();
        
        Constant orgUnitDenmarkIdConstant = constantService.getConstantByName( ORGUNIT_DENMARK_ID );
        Integer denmark_ID = (int) orgUnitDenmarkIdConstant.getValue();
        
        Constant orgUnitNorwayIdConstant = constantService.getConstantByName( ORGUNIT_NORWAY_ID );
        Integer norway_ID = (int) orgUnitNorwayIdConstant.getValue();
        
        Constant orgUnitGreenLandIdConstant = constantService.getConstantByName( ORGUNIT_GREENLAND_ID );
        Integer greenland_ID = (int) orgUnitGreenLandIdConstant.getValue();
        
        Constant orgUnitSavIdConstant = constantService.getConstantByName( ORGUNIT_SVALBARD_AND_JAN_MAYEN_ID );
        Integer svalbard_and_Jan_Mayen_ID = (int) orgUnitSavIdConstant.getValue();
        
        Period period = periodService.getPeriod( 1008 );
        
        // OrgUnit Details
                
        Set<OrganisationUnit> orgin = new HashSet<OrganisationUnit>( getOrganisationUnits()  );
        
        System.out.println("Size of OrgUnit List : " + orgin.size() );
        
        // DataElements Details

        List<String> dataElementUidsList = new ArrayList<String>( getDataElementUids() );
        Map<String, String> getDataElementUidsMap = new HashMap<String,String>( getDataElementUidsMap() );
        
        System.out.println("Size of dataElement List : " + dataElementUidsList.size() );
        
        // for disputed areas//
        
        Integer updateCount = 0;
        Integer insertCount = 0;
        
        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );
        
        for ( String deUid : dataElementUidsList )
        {
            for ( OrganisationUnit org : orgin )
            {
                String mappedDeUid = getDataElementUidsMap.get( deUid );
                
                //DeMapping demapping = demappingService.getDeMapping( de.getUid() );

                if ( mappedDeUid != null )
                {
                    String query = "";
                    int defaultValue = 10;
                    String defaultUser = "admin";
                    String defaultComment = "by schedule";
                    
                    //String deelementId = demapping.getMappeddeid();
                    DataElement dataelement = dataElementService.getDataElement( mappedDeUid );
                    DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
                    
                    DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
                    DataElementCategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

//                    System.out.println("dataelement -- "+ dataelement.getId() );
//                    System.out.println("category -- "+ categoryOptionCombo.getId() );
//                    System.out.println("attribute combo -- "+ defaultAttributeOptionCombo.getId() );
//                    System.out.println("orgUnit -- "+ org.getId() );
//                    System.out.println("period -- "+ period.getId() );
                    
                    try
                    {
                        //int count = 1;
                        
                        query = " SELECT dataelementid,periodid,sourceid,categoryoptioncomboid,VALUE FROM datavalue WHERE dataelementid = " + dataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + org.getId() + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();
                        
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                        
                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            //System.out.println( " Indide Update "   );
                            
                            String updateQuery = " UPDATE datavalue SET VALUE = " + defaultValue + ", lastupdated = '" + lastUpdatedDate + "', storedby = '" + defaultUser 
                                                   + "' WHERE dataelementid = " + dataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + org.getId() + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                                   + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();

                            jdbcTemplate.update( updateQuery );
                            
                            updateCount++;
                        }
                        else
                        {
                            //System.out.println( " Indide Insert "   );
                            
                            String insertQuery = " INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, status, storedby, lastupdated, comment ) VALUES ";
                            
                            insertQuery += "( " + dataelement.getId() + ", " + period.getId() + ", " + org.getId() + ", " + categoryOptionCombo.getId() + ", " + defaultAttributeOptionCombo.getId() + ", '" + defaultValue +  "', 1 , '" + defaultUser + "', '" 
                                                 + lastUpdatedDate + "' ,'"  + defaultComment + "' ) ";
                            
                            //System.out.println( " insertQuery -- " + insertQuery   );
                            
                            jdbcTemplate.update( insertQuery );
                            
                            insertCount++;
                        }
                        
                    }
                    catch ( Exception e )
                    {
                        System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                    }
                }
            }
        }
        
        //System.out.println("Insert Count = " + insertCount + " Update Count = " + updateCount );
        
        //
         
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitsAtLevel( 3 ) );
        
        System.out.println("Size of OrgUnit List 1 : " + orgUnitList.size() );
        
        orgUnitList.removeAll( orgin );
        
//        Collection<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( orgUnitList ) );
//        String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
//        System.out.println(" orgUnitIdsByComma : " + orgUnitIdsByComma );
        
        
        System.out.println("Size of OrgUnit List 2 : " + orgUnitList.size() );
        
        for ( String deUid : dataElementUidsList )
        {
            for ( OrganisationUnit org : orgUnitList )
            {

                String mappedDeUid = getDataElementUidsMap.get( deUid );

                if ( mappedDeUid != null )
                {
                    String code = "";
                    // System.out.println("dataelement"+demapping);
                    //String deelementId = demapping.getMappeddeid();
                    
                    DataElement dataelement = dataElementService.getDataElement( deUid );
                    //DataElement aggDataelement = dataElementService.getDataElement( mappedDeUid );
                    // System.out.println("deelementId"+deelementId);

                    DataValue dataValue = dataValueService.getLatestDataValue( dataelement.getId(), 1, org.getId() );
                    // System.out.println("dataValue"+dataValue);
                    if ( dataValue != null )
                    {
                        // System.out.println( "code1" + dataValue.getValue() );

                        if ( dataValue.getValue() == null )
                        {
                            code = "4";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Introduced into Routine" ) )
                        {
                            code = "1";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Planned" ) )
                        {
                            code = "3";

                        }
                        else if ( dataValue.getValue().equalsIgnoreCase( "Partially Introduced" ) || dataValue.getValue().equalsIgnoreCase( "Introduced partially" )
                            || dataValue.getValue().equalsIgnoreCase( "Introduced sequentially" ) )
                        {
                            code = "2";

                        }
                        
                        else if ( dataValue.getValue().equalsIgnoreCase( "Partial Intro planned" ) || dataValue.getValue().equalsIgnoreCase( "Intent to introduce" )
                            || dataValue.getValue().equalsIgnoreCase( "Formal commitment" ) )
                        {
                            code = "3";

                        }

                        else if ( dataValue.getValue().equalsIgnoreCase( "Risk Groups" ) || dataValue.getValue().equalsIgnoreCase( "Suspended" )
                            || dataValue.getValue().equalsIgnoreCase( "Donation" ) || dataValue.getValue().equalsIgnoreCase( "Not Introduced" )
                            || dataValue.getValue().equalsIgnoreCase( "Not Available" ) || dataValue.getValue().equalsIgnoreCase( "Not Applicable" ))
                        {
                            code = "4";

                        }
                        else
                        {
                            code = "4";
                        }
                        
                        //System.out.println( dataValue.getValue() + " --- " + code );
                    }
                    else
                    {
                        code = "4";
                    }
                    
                    String query = "";
                    
                    String defaultUser = "admin";
                    String defaultComment = "by schedule";
                    
                    //String deelementId = demapping.getMappeddeid();
                    DataElement aggDataelement = dataElementService.getDataElement( mappedDeUid );
                    DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
                    
                    DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
                    DataElementCategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
                    
                    try
                    {
                        //int count = 1;
                        
                        query = " SELECT dataelementid,periodid,sourceid,categoryoptioncomboid,VALUE FROM datavalue WHERE dataelementid = " + aggDataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + org.getId() + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();
                        
                        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
                        
                        if ( sqlResultSet != null && sqlResultSet.next() )
                        {
                            //System.out.println( " Indide Update "   );
                            
                            String updateQuery = " UPDATE datavalue SET VALUE = " + code + ", lastupdated = '" + lastUpdatedDate + "', storedby = '" + defaultUser 
                                                   + "' WHERE dataelementid = " + aggDataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + org.getId() + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                                   + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();

                            jdbcTemplate.update( updateQuery );
                            
                            updateCount++;
                        }
                        else
                        {
                            //System.out.println( " Indide Insert "   );
                            
                            String insertQuery = " INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, status, storedby, lastupdated, comment ) VALUES ";
                            
                            insertQuery += "( " + aggDataelement.getId() + ", " + period.getId() + ", " + org.getId() + ", " + categoryOptionCombo.getId() + ", " + defaultAttributeOptionCombo.getId() + ", '" + code +  "', 1 , '" + defaultUser + "', '" 
                                                 + lastUpdatedDate + "' ,'"  + defaultComment + "' ) ";
                            
                            //System.out.println( " insertQuery -- " + insertQuery   );
                            
                            jdbcTemplate.update( insertQuery );
                            
                            insertCount++;
                        }
                        
                    }
                    catch ( Exception e )
                    {
                        System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                    }
                    
                    // for transfer data form
                    
                    if( denmark_ID.equals( org.getId() ) )
                    {
                        DataValue denMarkDataValue = dataValueService.getLatestDataValue( aggDataelement.getId(), 1, denmark_ID );
                        DataValue greenLandDataValue = dataValueService.getLatestDataValue( aggDataelement.getId(), 1, greenland_ID );
                        
                        if ( denMarkDataValue != null )
                        {
                            //System.out.println(" For transfer data from  = " + denmark_ID + " to = " + greenland_ID + " for dataElement Id = " + aggDataelement.getId() );
                            String denMarkCode = denMarkDataValue.getValue();
                            if ( greenLandDataValue != null )
                            {
                                try
                                {
                                    String updateQuery = " UPDATE datavalue SET VALUE = '" + denMarkCode + "', lastupdated = '" + lastUpdatedDate + "', storedby = '" + defaultUser 
                                        + "' WHERE dataelementid = " + aggDataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + greenland_ID + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                        + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();

                                    jdbcTemplate.update( updateQuery );
                 
                                    updateCount++;
                                    
                                }
                                catch ( Exception e )
                                {
                                    System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                                }
                                
                            }
                            else
                            {
                                try
                                {
                                    String insertQuery = " INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, status, storedby, lastupdated, comment ) VALUES ";
                                    
                                    insertQuery += "( " + aggDataelement.getId() + ", " + period.getId() + ", " + greenland_ID + ", " + categoryOptionCombo.getId() + ", " + defaultAttributeOptionCombo.getId() + ", '" + denMarkCode +  "', 1 , '" + defaultUser + "', '" 
                                                         + lastUpdatedDate + "' ,'"  + defaultComment + "' ) ";
                                    
                                    //System.out.println( " insertQuery -- " + insertQuery   );
                                    
                                    jdbcTemplate.update( insertQuery );
                                    
                                    insertCount++;
                                   
                                }
                                catch ( Exception e )
                                {
                                    System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                                }
                            }
                        }
                    }
                    if( norway_ID.equals( org.getId() ) )
                    {
                        DataValue norwayDataValue = dataValueService.getLatestDataValue( aggDataelement.getId(), 1, norway_ID );
                        DataValue svalbardDataValue = dataValueService.getLatestDataValue( aggDataelement.getId(), 1, svalbard_and_Jan_Mayen_ID );
                    
                        if ( norwayDataValue != null )
                        {
                            //System.out.println(" For transfer data from  = " + norway_ID + " to = " + svalbard_and_Jan_Mayen_ID + " for dataElement Id = " + aggDataelement.getId() );
                            
                            String norwayCode = norwayDataValue.getValue();
                            if ( svalbardDataValue != null )
                            {
                                try
                                {
                                    String updateQuery = " UPDATE datavalue SET VALUE = '" + norwayCode + "', lastupdated = '" + lastUpdatedDate + "', storedby = '" + defaultUser 
                                        + "' WHERE dataelementid = " + aggDataelement.getId() + " AND periodid = " + period.getId() + " AND sourceid = " + svalbard_and_Jan_Mayen_ID + "  AND categoryoptioncomboid = " + categoryOptionCombo.getId() 
                                        + " AND attributeoptioncomboid = " + defaultAttributeOptionCombo.getId();
    
                                    jdbcTemplate.update( updateQuery );
                 
                                    updateCount++;
                                    
                                }
                                catch ( Exception e )
                                {
                                    System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                                }
                                
                            }
                            else
                            {
                                try
                                {
                                    String insertQuery = " INSERT INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, status, storedby, lastupdated, comment ) VALUES ";
                                    
                                    insertQuery += "( " + aggDataelement.getId() + ", " + period.getId() + ", " + svalbard_and_Jan_Mayen_ID + ", " + categoryOptionCombo.getId() + ", " + defaultAttributeOptionCombo.getId() + ", '" + norwayCode +  "', 1 , '" + defaultUser + "', '" 
                                                         + lastUpdatedDate + "' ,'"  + defaultComment + "' ) ";
                                    
                                    //System.out.println( " insertQuery -- " + insertQuery   );
                                    
                                    jdbcTemplate.update( insertQuery );
                                    
                                    insertCount++;
                                   
                                }
                                catch ( Exception e )
                                {
                                    System.out.println( "Exception occured while inserting/updating, please check log for more details" + e.getMessage() ) ;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        
        System.out.println("Insert Count = " + insertCount + " Update Count = " + updateCount );
        
        System.out.println("INFO: Scheduler job has ended at : " + new Date() );
        
        return SUCCESS;
    }
    
    
    
    
    
    // Supportive Methods
    // get Source List for KeyPerformance Indicators
    public List<OrganisationUnit> getOrganisationUnits()
    {
        List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>();
       
        Constant orgUnitIdConstant = constantService.getConstantByName( XTRA_ORGUNIT_ID );
        int orgUnitId = (int) orgUnitIdConstant.getValue();
        
        try
        {
            String query = "SELECT organisationunitid FROM organisationunit " +
                      " WHERE  parentid = " + orgUnitId;
            
            //SELECT organisationunitid FROM organisationunit WHERE parentid = 208;
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer orgId = rs.getInt( 1 );
                
                if ( orgId != null )
                {
                    OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgId );
                    organisationUnits.add( orgUnit );
                }
            }

            return organisationUnits;
        }
        
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal De OrgUnit Id ", e );
        }
        
    }    
    
    // get dataElements List
    public List<String> getDataElementUids()
    {
        List<String> dataElementUids = new ArrayList<String>();
        
        Constant vaccineIntroDataElementGroupConstant = constantService.getConstantByName( VACCINE_INTRO_DE_GROUP_ID );
        int deGroupId = (int) vaccineIntroDataElementGroupConstant.getValue();
        
        try
        {
            String query = "SELECT uid FROM dataelement WHERE dataelementid IN " +
                 "( SELECT dataelementid FROM dataelementgroupmembers WHERE dataelementgroupid = " + deGroupId + ")";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            //SELECT uid FROM dataelement WHERE dataelementid IN ( SELECT dataelementid FROM dataelementgroupmembers WHERE dataelementgroupid = 40);
            
            while ( rs.next() )
            {
                String deUid = rs.getString( 1 );
                
                if ( deUid != null )
                {
                    dataElementUids.add( deUid );
                }
            }

            return dataElementUids;
        }
        
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal De Group Id ", e );
        }
        
    }
    
    public Map<String, String> getDataElementUidsMap()
    {
        Map<String, String> getDataElementUidsMap = new HashMap<String,String>();
        
        Constant vaccineIntroDataElementGroupConstant = constantService.getConstantByName( VACCINE_INTRO_DE_GROUP_ID );
        int deGroupId = (int) vaccineIntroDataElementGroupConstant.getValue();
        
        try
        {
            String query = "SELECT deid,mappeddeid FROM demapping WHERE deid IN (SELECT uid FROM dataelement WHERE dataelementid IN ( " +
                " SELECT dataelementid FROM dataelementgroupmembers WHERE dataelementgroupid  = " + deGroupId  + " ) )";
            
            
//            SELECT deid,mappeddeid FROM demapping WHERE deid IN (SELECT uid FROM dataelement WHERE dataelementid IN ( SELECT dataelementid FROM dataelementgroupmembers WHERE dataelementgroupid = 40)
//                );

            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                String keyUid = rs.getString( 1 );
                String valueUid = rs.getString( 2 );
                
                //String lastUpdated = simpleDateFormat.format( rs.getDate( 6 ) );
                
                if ( keyUid != null )
                {
                    getDataElementUidsMap.put( keyUid, valueUid );
                }
                
            }
            return getDataElementUidsMap;
        }
        
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal De Group Id ", e );
        }
        
    }
     
    
    
}
