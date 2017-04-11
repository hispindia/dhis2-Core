package org.hisp.dhis.ivb.util;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.commons.filter.Filter;
import org.hisp.dhis.commons.filter.FilterUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.system.database.DatabaseInfo;
import org.hisp.dhis.system.database.DatabaseInfoProvider;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author BHARATH
 */

@Transactional
public class IVBUtil
{
    public static final int REGION_LEVEL = 2;
	
    public static final String CSV_FORMAT = "CSV";
    
    public static final String XLS_FORMAT = "XLS";
    
    public static final String XLS_FORMAT_COUNTRY_AS_ROWS = "XLS_COUNTRY_AS_ROWS";

    public static final String OPERATOR_AND = "AND";

    public static final String OPERATOR_OR = "OR";

    public static final String DE_VALUE = "#VALUE#";
    
    public static final String CUSTOM_DATAENTRY = "CUSTOM_DATAENTRY";
    
    public static final String TABULAR_REPORT = "TABULAR_REPORT";
    
    public static final String PROWG_INTRO_YEAR_DE_GROUP = "PROWG_INTRO_YEAR_DE_GROUP";

    public static final String OPERAND_EXPRESSION = "(#\\{(\\w+)\\.?(\\w*)\\}" + "|DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)"
        + "|YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|CURYEAR" + "|MAX\\("
        + "(#\\{(\\w+)\\.?(\\w*)\\},)*" + "#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|MAX\\("
        + "(DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + "|MAX\\("
        + "(YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + "|MAX\\("
        + "(MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + ")";

    public static final String ARITHMETIC_OPERATOR = "(\\+" + "|" + "\\-" + "|" + "\\*" + "|" + "\\/)";

    public static final String CONSTANT_EXPRESSION = "(\\'YES\\'|\\'NO\\'|[0-9]+|\\'[a-zA-Z0-9]+\\s[a-zA-Z0-9]*\\'"
        + "|NOW\\(\\)" + "|BETWEEN\\(NOW\\(\\),NEXTYEAR\\)" + ")";

    public static final Pattern OPERAND_PATTERN = Pattern.compile( OPERAND_EXPRESSION );

    public static final Pattern CONSTANT_PATTERN = Pattern.compile( CONSTANT_EXPRESSION );

    public static final String OPERATOR_EXPRESSION = "(<|=|>|!=|<=|>=)";

    public static final String regExp = "^IF\\((" + OPERAND_PATTERN + "|" + OPERAND_PATTERN + ARITHMETIC_OPERATOR
        + OPERAND_PATTERN + ")" + OPERATOR_EXPRESSION + CONSTANT_PATTERN + "\\)$";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    
    private static DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }
    
    private static ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
    
    private SectionService sectionService;
    
    public void setSectionService(SectionService sectionService) 
    {
	this.sectionService = sectionService;
    }

    private DataElementCategoryService dataElementCategoryService;
    
    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    enum csvModes { VCT, VC, V, C, T };
    
    //----------------------------------------------------------------
    // Input/Output
    //-----------------------------------------------------------------
    public static OrganisationUnit organisationUnit;    

    public static void setOrganisationUnit( OrganisationUnit organisationUnit )
    {
        IVBUtil.organisationUnit = organisationUnit;
    }
    public static DataElementCategoryOptionCombo optionCombo;
    
    public static void setOptionCombo( DataElementCategoryOptionCombo optionCombo )
    {
        IVBUtil.optionCombo = optionCombo;
    }
    
    private static Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }
    @Autowired
    private DatabaseInfoProvider databaseInfoProvider;   

    public void setDatabaseInfoProvider( DatabaseInfoProvider databaseInfoProvider )
    {
        this.databaseInfoProvider = databaseInfoProvider;
    }

    
    //----------------------------------------------------------------
    // Methods
    //-----------------------------------------------------------------
    
    public CSVImportSatus importCSVData( List csvRows , String conflict )
        throws Exception
    {
        CSVImportSatus csvImportSatus = new CSVImportSatus();

        List<String> statusMsgs = new ArrayList<String>();

        Integer facilityCount = 0;
        Integer importFacilityCount = 0;
        Integer updateCount = 0;
        Integer insertCount = 0;
        Integer totalCount = 0;
        String missingFacilities = "";

        String facilityCode = "0";

        String storedBy = currentUserService.getCurrentUsername();
        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        User user = currentUserService.getCurrentUser();
        if ( user == null || user.getOrganisationUnits() == null )
        {
            statusMsgs.add( "<br><font color=red><strong>Current Logged in user may not have any organisationunits, Please check.</font></strong>" );
            csvImportSatus.setStatusMsg( statusMsgs );
            return csvImportSatus;
        }

        List<OrganisationUnit> userRootOrgunits = new ArrayList<OrganisationUnit>( user.getOrganisationUnits() );
        Set<OrganisationUnit> userOrgUnits = new HashSet<OrganisationUnit>();
        for ( OrganisationUnit orgUnit : userRootOrgunits )
        {
            userOrgUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( orgUnit.getId() ) );
        }
        Collection<Integer> userOrgUnitIds = new ArrayList<Integer>( getIdentifiers( userOrgUnits ) );

        Set<DataElement> userDataElements = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( user.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        Collection<Integer> userDataElementIds = new ArrayList<Integer>( getIdentifiers( userDataElements ) );

        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );

        String query = "";
        int insertFlag = 1;       
        String insertQuery = "INSERT IGNORE INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, status ) VALUES ";
        //String insertQueryDVA = "INSERT INTO datavalue_audit ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, commenttype, status ) VALUES ";
        String insertQueryDVA = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES ";

        
        
        int count = 1;
        Integer rowCount = 0;
        int flag = 0;
        
        String[] headerRow = (String[]) csvRows.get( 0 );
        Integer numberOfCols = headerRow.length;
        csvModes csvMode;
        if( numberOfCols == 6 && headerRow[0].trim().equalsIgnoreCase( "CountryCode" ) && headerRow[1].trim().equalsIgnoreCase( "IndicatorCode" ) && headerRow[2].trim().equalsIgnoreCase( "Period" ) && headerRow[3].trim().equalsIgnoreCase( "Value" ) && headerRow[4].trim().equalsIgnoreCase( "Comment" ) && headerRow[5].trim().equalsIgnoreCase( "TA" ) )
        {
            csvMode = csvModes.VCT;
        }
        else if( numberOfCols == 5 && headerRow[0].trim().equalsIgnoreCase( "CountryCode" ) && headerRow[1].trim().equalsIgnoreCase( "IndicatorCode" ) && headerRow[2].trim().equalsIgnoreCase( "Period" ) && headerRow[3].trim().equalsIgnoreCase( "Value" ) && headerRow[4].trim().equalsIgnoreCase( "Comment" ) )
        {
            csvMode = csvModes.VC;
        }
        else if( numberOfCols == 4 && headerRow[0].trim().equalsIgnoreCase( "CountryCode" ) && headerRow[1].trim().equalsIgnoreCase( "IndicatorCode" ) && headerRow[2].trim().equalsIgnoreCase( "Period" ) && headerRow[3].trim().equalsIgnoreCase( "Value" ) )
        {
            csvMode = csvModes.V;
        }        
        else if( numberOfCols == 4 && headerRow[0].trim().equalsIgnoreCase( "CountryCode" ) && headerRow[1].trim().equalsIgnoreCase( "IndicatorCode" ) && headerRow[2].trim().equalsIgnoreCase( "Period" ) && headerRow[3].trim().equalsIgnoreCase( "Comment" ) )
        {
            csvMode = csvModes.C;
        }
        else if( numberOfCols == 4 && headerRow[0].trim().equalsIgnoreCase( "CountryCode" ) && headerRow[1].trim().equalsIgnoreCase( "IndicatorCode" ) && headerRow[2].trim().equalsIgnoreCase( "Period" ) && headerRow[3].trim().equalsIgnoreCase( "TA" ) )
        {
            csvMode = csvModes.T;
        }        
        else
        {
            statusMsgs.add( "<br><font color=red><strong>Uploaded CSV doesn't have all fileds.</font></strong>" );
            csvImportSatus.setStatusMsg( statusMsgs );
            return csvImportSatus;
        }

        for ( Object obj : csvRows )
        {
            if ( flag == 0 )
            {
                flag = 1;
                //continue;
            }

            rowCount++;
            
            String[] oneRow = (String[]) obj;
            int noOfCols = oneRow.length;

            //System.out.println(rowCount + " : " + oneRow[0] + " : " + oneRow[1] + " : " + oneRow[2]);
            
            if( oneRow[0].equalsIgnoreCase( "CountryCode" ) )
            {
                continue;
            }
            
            //System.out.println( oneRow.length + " : " + noOfCols );
            
            /*
            if ( oneRow == null || ( noOfCols != 6 && noOfCols != 5  ) )
            {
            	statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". doesn't have all fileds.</font></strong>" );                
                continue;
            }
            */
            
            String orgUnitCode = oneRow[0];
            Integer orgUnitId = getOrgUnitIdByCode( orgUnitCode );

            if ( orgUnitId == null )
            {
                if ( !facilityCode.equals( oneRow[0] ) )
                {
                    facilityCode = orgUnitCode;
                    facilityCount++;
                    missingFacilities += orgUnitCode + ", ";
                }
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". No Orgunit found with the code : " + orgUnitCode + "</font></strong>" );               
                continue;
            }

            if ( !facilityCode.equals( orgUnitCode ) )
            {
                facilityCode = orgUnitCode;
                facilityCount++;
                importFacilityCount++;
            }

            if ( !userOrgUnitIds.contains( orgUnitId ) )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Current User doesn't have authority to upload/import data for Orgunit with the code : " + orgUnitCode + "</font></strong>" );                
                continue;
            }

            String deCode = oneRow[1];
            String dataElement = getDataElementByCode( "DE" + deCode );
            Integer deId = null;
            String deValueType = null;
            if( dataElement != null )
            {
                try
                {
                    deId = Integer.parseInt( dataElement.split( ":" )[0] );
                    deValueType = dataElement.split( ":" )[1];
                }
                catch( Exception e )
                {
                    deId = null;
                }
            }
            
            /**
             * TODO - DataEelement OptionCombo Id is hardcoded to 1, need to fix
             * this
             */
            Integer deCOCId = 1;
            if ( deId == null )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". No DataElement found with the code : " + deCode + "</font></strong>" );                
                continue;
            }

            if ( !userDataElementIds.contains( deId ) )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Current User doesn't have authority to upload/import data for DataElement with the code : " + deCode + "</font></strong>" );                
                continue;
            }

            String period = oneRow[2];

            String selQuarter = "";

            Period selectedPeriod = null;
            
            try
            {
                if( period.split( "-") == null )
                {
                    selQuarter = period.split( "-" )[0] + "Q1";
                }
                else
                {
                    if ( period.split( "-" )[1].equalsIgnoreCase( "Q1" ) )
                    {
                        //selQuarter = "Quarterly_" + period.split( "-" )[0] + "-01-01";
                        selQuarter = period.split( "-" )[0] + "Q1";
                    }
                    else if ( period.split( "-" )[1].equalsIgnoreCase( "Q2" ) )
                    {
                        //selQuarter = "Quarterly_" + period.split( "-" )[0] + "-04-01";
                        selQuarter = period.split( "-" )[0] + "Q2";
                    }
                    else if ( period.split( "-" )[1].equalsIgnoreCase( "Q3" ) )
                    {
                        //selQuarter = "Quarterly_" + period.split( "-" )[0] + "-07-01";
                        selQuarter = period.split( "-" )[0] + "Q3";
                    }
                    else
                    {
                        //selQuarter = "Quarterly_" + period.split( "-" )[0] + "-10-01";
                        selQuarter = period.split( "-" )[0] + "Q4";
                    }
                }
                
                selectedPeriod = PeriodType.getPeriodFromIsoString( selQuarter );
                selectedPeriod = periodService.reloadPeriod( selectedPeriod );
            }
            catch ( Exception e )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Period format is not Valid : " + period + "</font></strong>" );
                continue;
            }

            String value = "";
            if( csvMode == csvModes.VCT || csvMode == csvModes.V || csvMode == csvModes.VC )
            {            
                value = oneRow[3];
                if ( value == null )
                {
                    value = "";
                }
                value = value.trim();
                value = value.replaceAll( "'", "\\\\\'" );
            
                //System.out.println( "VALUE IS : " + value );
                
                if( deValueType.equalsIgnoreCase( ValueType.INTEGER.toString() ) )
                {
                    try
                    {
                        double tempD = Double.parseDouble( value );
                    }
                    catch( Exception e )
                    {
                        value = "";                   
                    }
                }
            }
            
            String comment = "";
            if( csvMode == csvModes.VCT || csvMode == csvModes.VC )
            {
                comment = oneRow[4];
            }
            else if( csvMode == csvModes.C )
            {
                comment = oneRow[3];
            }
            
            if ( comment == null )
            {
                comment = "";
            }
            comment = comment.trim();
            comment = comment.replaceAll( "'", " " );
            comment = new String( comment.getBytes("UTF-8"), "UTF-8" );
            
            String techAssistance = null;
            if( csvMode == csvModes.VCT )
            {
                techAssistance = oneRow[5];
            }
            else if( csvMode == csvMode.T )
            {
                techAssistance = oneRow[3];
            }
            
            if ( techAssistance == null )
            {
                techAssistance = "";
            }
            techAssistance = techAssistance.trim();
            techAssistance = techAssistance.replaceAll( "'", " " );
            techAssistance = new String( techAssistance.getBytes("UTF-8"), "UTF-8" );

            DataValue latestDataValue = dataValueService.getLatestDataValue( deId, deCOCId, orgUnitId );
            
            //query = "SELECT value, storedby, comment FROM datavalue WHERE dataelementid = " + deId + " AND categoryoptioncomboid = " + deCOCId + " AND periodid = " + selectedPeriod.getId() + " AND sourceid = " + orgUnitId;
            
            //System.out.println( query );
            
            //SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );

            //if ( sqlResultSet1 != null && sqlResultSet1.next() )
            if( latestDataValue != null && latestDataValue.getPeriod().getId() == selectedPeriod.getId() )
            {
            	//String prevValue = sqlResultSet1.getString(1);
            	//String previousStoredBy = sqlResultSet1.getString(2);
            	//String prevComment = sqlResultSet1.getString( 3 );

                String prevValue = latestDataValue.getValue();
                String previousStoredBy = latestDataValue.getStoredBy();
                String prevComment = latestDataValue.getComment();

            	if( csvMode == csvModes.VCT && value.equals( prevValue) && comment.equals(prevComment) && storedBy.equals(previousStoredBy) )
            	{
            	    continue;
            	}
            	else if( csvMode == csvModes.VC && value.equals( prevValue) && comment.equals(prevComment) && storedBy.equals(previousStoredBy) )
            	{
            	    continue;
            	}
                else if( csvMode == csvModes.V && value.equals( prevValue) && storedBy.equals(previousStoredBy) )
                {
                    continue;
                }
                else if( csvMode == csvModes.C && comment.equals(prevComment) && storedBy.equals(previousStoredBy) )
                {
                    continue;
                }
            	
            	if( csvMode == csvModes.V )
            	{
            	    comment = prevComment;
            	}
            	else if( csvMode == csvModes.C )
            	{
            	    value = prevValue;
            	    value = value.replaceAll( "'", "\\\\\'" );
            	}
            	else if( csvMode == csvModes.T )
            	{
            	    value = prevValue;
            	    value = value.replaceAll( "'", "\\\\\'" );
            	    comment = prevComment;
            	}
            	    
            	int datavalueConflict = 0;
            	if(conflict == null && !previousStoredBy.equalsIgnoreCase(storedBy))
            	{
            	    datavalueConflict = 1;
            	}

            	String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy
                    + "',lastupdated='" + lastUpdatedDate + "',comment='" + comment + "' ,followup='" + datavalueConflict + "' WHERE dataelementid = " + deId
                    + " AND periodid = " + selectedPeriod.getId() + " AND sourceid = " + orgUnitId
                    + " AND categoryoptioncomboid = " + deCOCId;
                
                jdbcTemplate.update( updateQuery );
               
                updateCount++;
                
                if( ( csvMode == csvModes.VCT ) )
                {
                	String insertHistoryQuery = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES " + 
                			"( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 )";
                    jdbcTemplate.update( insertHistoryQuery );
                    
                    String insertTAQuery = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES " +
                        "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + techAssistance + "', 'T', 1 )";
                    jdbcTemplate.update( insertTAQuery );
                }
                else if( csvMode == csvModes.V || csvMode == csvModes.VC || csvMode == csvModes.C )
                {
                	String insertHistoryQuery = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES " + 
                			"( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 )";
                    jdbcTemplate.update( insertHistoryQuery );
                }
                else if( csvMode == csvModes.T )
                {
                    String insertTAQuery = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES " +
                            "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + techAssistance + "', 'T', 1 )";
                    jdbcTemplate.update( insertTAQuery );
                }
            }
            else
            {
                if( latestDataValue != null && latestDataValue.getPeriod().getId() != selectedPeriod.getId() )
                {
                    if( csvMode == csvModes.V && latestDataValue.getComment() != null && !latestDataValue.getComment().trim().equals( "" ) )
                    {
                        comment = latestDataValue.getComment();
                    }
                    else if( csvMode == csvModes.C && latestDataValue.getValue() != null && !latestDataValue.getValue().trim().equals( "" ) )
                    {
                        value = latestDataValue.getValue();
                        value = value.replaceAll( "'", "\\\\\'" );
                    }
                }
                
                insertQuery += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", "+ deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "', '" + comment + "', 1 ), ";
                
                if( csvMode == csvModes.V || csvMode == csvModes.VC )
                {
                					  
                	insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 ), ";                    
                }
                else if( csvMode == csvModes.VCT )
                {
                    insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 ), ";
                    insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + techAssistance + "', 'T', 1 ), ";
                }
                else if( csvMode == csvModes.C && comment != null && !comment.trim().equals( "" ) )
                {
                	insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 ), ";
                }                
                else if( csvMode == csvModes.T && techAssistance != null && !techAssistance.trim().equals( "" ) )
                {
                	insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + techAssistance + "', 'T', 1 ), ";                    
                }

                insertFlag = 2;
                insertCount++;
            }

            if ( count == 1000 )
            {
                count = 1;

                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    insertQueryDVA = insertQueryDVA.substring( 0, insertQueryDVA.length() - 2 );
                    jdbcTemplate.update( insertQuery );
                    jdbcTemplate.update( insertQueryDVA );
                }

                insertFlag = 1;
                insertQuery = "INSERT IGNORE INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, status ) VALUES ";
                insertQueryDVA = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES ";
            }

            count++;
        }

        if ( insertFlag != 1 )
        {
            insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
            insertQueryDVA = insertQueryDVA.substring( 0, insertQueryDVA.length() - 2 );
            jdbcTemplate.update( insertQuery );
            jdbcTemplate.update( insertQueryDVA );
        }

        csvImportSatus.setFacilityCount( facilityCount );
        csvImportSatus.setImportFacilityCount( importFacilityCount );
        csvImportSatus.setUpdateCount( updateCount );
        csvImportSatus.setInsertCount( insertCount );
        csvImportSatus.setTotalCount( rowCount );
        csvImportSatus.setMissingFacilities( missingFacilities );
        csvImportSatus.setStatusMsg( statusMsgs );

        return csvImportSatus;
    }

    
   public CSVImportSatus importXLSData( Map<String, List<String>> excelCellDataMap , String conflict ) throws Exception
    {
        CSVImportSatus csvImportSatus = new CSVImportSatus();

        List<String> statusMsgs = new ArrayList<String>();

        Integer facilityCount = 0;
        Integer importFacilityCount = 0;
        Integer updateCount = 0;
        Integer insertCount = 0;
        Integer totalCount = 0;
        String missingFacilities = "";

        String facilityCode = "0";

        String storedBy = currentUserService.getCurrentUsername();
        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        User user = currentUserService.getCurrentUser();
        if ( user == null || user.getOrganisationUnits() == null )
        {
            statusMsgs.add( "<br><font color=red><strong>Current Logged in user may not have any organisationunits, Please check.</font></strong>" );
            csvImportSatus.setStatusMsg( statusMsgs );
            return csvImportSatus;
        }

        List<OrganisationUnit> userRootOrgunits = new ArrayList<OrganisationUnit>( user.getOrganisationUnits() );
        Set<OrganisationUnit> userOrgUnits = new HashSet<OrganisationUnit>();
        for ( OrganisationUnit orgUnit : userRootOrgunits )
        {
            userOrgUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( orgUnit.getId() ) );
        }
        Collection<Integer> userOrgUnitIds = new ArrayList<Integer>( getIdentifiers( userOrgUnits ) );

        Set<DataElement> userDataElements = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( user.getUserCredentials().getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        Collection<Integer> userDataElementIds = new ArrayList<Integer>( getIdentifiers( userDataElements ) );

        long t;
        Date d = new Date();
        t = d.getTime();
        java.sql.Date lastUpdatedDate = new java.sql.Date( t );

        String query = "";
        int insertFlag = 1;
        String insertQuery = "INSERT IGNORE INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, status ) VALUES ";
        //String insertQueryDVA = "INSERT INTO datavalue_audit ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, commenttype, status ) VALUES ";
        String insertQueryDVA = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES ";
        
        int count = 1;
        Integer rowCount = 0;
        int flag = 0;
        //System.out.println( excelCellDataMap.size() );
        for ( String cellKey : excelCellDataMap.keySet() )
        {
            String[] oneRow = cellKey.split( "#" );
            
            //System.out.println("oneRow[1] "+oneRow[1]+"oneRow[2]"+oneRow[2]);
            
            if ( flag == 0 )
            {
                flag = 1;   
              //continue;
            }

            rowCount++;

            // Orgunit
            
            
            Integer orgUnitId = Integer.parseInt( oneRow[1] );
            String orgUnitCode = getOrgUnitCodeById( orgUnitId );
           
            if ( orgUnitId == null )
            {
                if ( !facilityCode.equals( oneRow[1] ) )
                {
                    facilityCode = orgUnitCode;
                    facilityCount++;
                    missingFacilities += orgUnitCode + ", ";
                }
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". No Orgunit found with the code : " + orgUnitCode + "</font></strong>" );                
                continue;
            }

            if ( !facilityCode.equals( orgUnitCode ) )
            {
                facilityCode = orgUnitCode;
                facilityCount++;
                importFacilityCount++;
            }

            if ( !userOrgUnitIds.contains( orgUnitId ) )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Current User doesn't have authority to upload/import data for Orgunit with the code : " + orgUnitCode + "</font></strong>" );
                continue;
            }

            // Dataelement            
            Integer deId = Integer.parseInt( oneRow[2] );
            //System.out.println("deId "+ deId );
           
            String deCode = getDataElementCodeById( deId );
            //System.out.println("deCode "+ deCode );
            /*
             * DataEelement OptionCombo Id is hardcoded to 1, need to fix             
             */
            Integer deCOCId = 1;
            if ( deId == null )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". No DataElement found with the code : " + deCode + "</font></strong>" );
               continue;
            }

            if ( !userDataElementIds.contains( deId ) )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Current User doesn't have authority to upload/import data for DataElement with the code : " + deCode + "</font></strong>" );
                continue;
            }

            // Period
            String period = oneRow[3];

            Period selectedPeriod = null;

            try
            {
                selectedPeriod = PeriodType.getPeriodFromIsoString( period );
                selectedPeriod = periodService.reloadPeriod( selectedPeriod );
            }
            catch ( Exception e )
            {
                statusMsgs.add( "<br><font color=red><strong>Row No : " + rowCount + ". Period format is not Valid : " + period + "</font></strong>" );
                continue;
            }

            if( oneRow[0].equals( "DV" ) )
            {
            	List<String> cellValues = new ArrayList<String>( excelCellDataMap.get( cellKey ) );
                String value = cellValues.get( 0 );
                String comment = cellValues.get( 1 );
                String techAssistance = cellValues.get( 2 );
                
                if( ( value == null || value.trim().equals( "" ) ) && ( comment == null || comment.trim().equals( "" ) ) && ( techAssistance == null || techAssistance.trim().equals( "" ) ) ) 
                {
                    continue;
                }
                
                if ( value == null )
                {
                    value = "";
                }
                value = value.trim();
                value = value.replaceAll( "'", "\\\\\'" );
                
                //System.out.println( "VALUE IS : " + value );
		
                if ( comment == null )
                {
                	comment = "";
                }
                comment = comment.trim();
                comment = comment.replaceAll( "'", " " );
                comment = new String(comment.getBytes("UTF-8"), "UTF-8");
                
                //byte comment1[] = comment.getBytes("ISO-8859-1");
                //comment = new String(comment1, "UTF-8");
		
                if ( techAssistance == null )
                {
                	techAssistance = "";
                }
                techAssistance = techAssistance.trim();

                query = "SELECT value, storedby, comment FROM datavalue WHERE dataelementid = " + deId + " AND categoryoptioncomboid = " + deCOCId + " AND periodid = " + selectedPeriod.getId() + " AND sourceid = " + orgUnitId;
                //System.out.println( query );
                
                SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( query );

                if ( sqlResultSet1 != null && sqlResultSet1.next() )
                {
                	String prevValue = sqlResultSet1.getString(1);
                	String previousStoredBy = sqlResultSet1.getString(2);
                	String prevComment = sqlResultSet1.getString( 3 );
                	
                	if( value.equals( prevValue) && comment.equals(prevComment) && storedBy.equals(previousStoredBy) )
                	{
                		continue;
                	}
                	
                	//comment = comment.replaceAll( "'", " " );
                	int datavalueConflict = 0;
                	if( conflict == null && !previousStoredBy.equalsIgnoreCase(storedBy) )
                	{
                		datavalueConflict = 1;
                	}
                	
                	//comment = new String(comment.getBytes("UTF-8"), "UTF-8");
                	//System.out.println("PSB "+previousStoredBy);
                	//System.out.println(datavalueConflict);
		   
                	String updateQuery = "UPDATE datavalue SET value = '" + value + "', storedby = '" + storedBy
						+ "',lastupdated='" + lastUpdatedDate + "',comment='" + comment + "',followup='" + datavalueConflict + "' WHERE dataelementid = " + deId
						+ " AND periodid = " + selectedPeriod.getId() + " AND sourceid = " + orgUnitId
						+ " AND categoryoptioncomboid = " + deCOCId;		    
		  
                	jdbcTemplate.update( updateQuery );		    
                	updateCount++;
                	
                	//System.out.println("InsideUpdate:" + updateQuery );
                	String insertHistoryQuery = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES " + 
                			"( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 )";

                	//String insertHistoryQuery = "INSERT INTO datavalue_audit ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, commenttype, status ) VALUES " +
                	//								"( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", " + deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "', '" + comment + "', 'H', 1 )";
                	jdbcTemplate.update( insertHistoryQuery );		    
                }
                else
                {
                	//comment = comment.replaceAll( "'", " " );
                	//comment = new String(comment.getBytes("UTF-8"), "UTF-8");
                	insertQuery += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", " + deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "', '" + comment + "', 1 ), ";
                	//insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", " + deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "', '" + comment + "', 'H', 1 ), ";

                    insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + comment + "', 'H', 1 ), ";

                	if( techAssistance != null && !techAssistance.trim().equals("") )
                	{
                		//insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", " + deCOCId + ", '" + value + "', '" + storedBy + "', '" + lastUpdatedDate + "', '" + techAssistance + "', 'T', 1 ), ";
                        insertQueryDVA += "( " + deId + ", " + selectedPeriod.getId() + ", " + orgUnitId + ", " + deCOCId + ", '" + value + "', '" + lastUpdatedDate + "', '" + storedBy + "', 'UPDATE', '" + techAssistance + "', 'T', 1 ), ";

                	}
                	insertFlag = 2;
                	insertCount++;
                	
                	//System.out.println("InsideInsert:" + insertQuery );
                }
            }
            
            if ( count == 1000 )
            {
                count = 1;

                if ( insertFlag != 1 )
                {
                    insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
                    insertQueryDVA = insertQueryDVA.substring( 0, insertQueryDVA.length() - 2 );
                    jdbcTemplate.update( insertQuery );
                    jdbcTemplate.update( insertQueryDVA );
                }

                insertFlag = 1;

                insertQuery = "INSERT IGNORE INTO datavalue ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, status ) VALUES ";
                //insertQueryDVA = "INSERT INTO datavalue_audit ( dataelementid, periodid, sourceid, categoryoptioncomboid, attributeoptioncomboid, value, storedby, lastupdated, comment, commenttype, status ) VALUES ";
                insertQueryDVA = "INSERT INTO datavalueaudit ( dataelementid, periodid, organisationunitid, categoryoptioncomboid, value, timestamp, modifiedby, audittype, comment, commenttype, status ) VALUES ";
            }

            count++;
        }

        if ( insertFlag != 1 )
        {
            insertQuery = insertQuery.substring( 0, insertQuery.length() - 2 );
            insertQueryDVA = insertQueryDVA.substring( 0, insertQueryDVA.length() - 2 );
            
            byte dvtext[] = insertQuery.getBytes("UTF-8"); 
            String insertQueryDV = new String(dvtext, "UTF-8");
            jdbcTemplate.update( insertQueryDV );
            
            byte dvatext[] = insertQueryDVA.getBytes("UTF-8"); 
            String insertQueryForDVA = new String(dvatext, "UTF-8");
            jdbcTemplate.update( insertQueryForDVA );
        }

        csvImportSatus.setFacilityCount( facilityCount );
        csvImportSatus.setImportFacilityCount( importFacilityCount );
        csvImportSatus.setUpdateCount( updateCount );
        csvImportSatus.setInsertCount( insertCount );
        csvImportSatus.setTotalCount( rowCount );
        csvImportSatus.setMissingFacilities( missingFacilities );
        csvImportSatus.setStatusMsg( statusMsgs );

        return csvImportSatus;
    }

    public String getOrgUnitCodeById( Integer orgUnitId )
    {
        String query = "SELECT code FROM organisationunit WHERE organisationunitid = "+orgUnitId;
        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
        if ( sqlResultSet != null && sqlResultSet.next() )
        {
            return sqlResultSet.getString( 1 );
        }
        return null;
    }

    public Integer getOrgUnitIdByCode( String orgUnitCode )
    {
        String query = "SELECT organisationunitid FROM organisationunit WHERE code LIKE '" + orgUnitCode + "'";
       
        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
        if ( sqlResultSet != null && sqlResultSet.next() )
        {
            return sqlResultSet.getInt( 1 );
        }
        return null;
    }

    public String getDataElementCodeById( Integer dataElementId )
    {
        String query = "SELECT dataelementid FROM dataelement WHERE dataelementid = " + dataElementId;
        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
        if ( sqlResultSet != null && sqlResultSet.next() )
        {
            //System.out.println( "sqlResultSet.getString( 1 ) "+sqlResultSet.getString( 1 ) );
            return sqlResultSet.getString( 1 );
        }
        return null;
    }

    public String getDataElementByCode( String dataElementCode )
    {
        String query = "SELECT dataelementid, valuetype FROM dataelement WHERE code LIKE '" + dataElementCode + "'";
        SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );
        if ( sqlResultSet != null && sqlResultSet.next() )
        {
            
            return sqlResultSet.getInt( 1 )+":"+sqlResultSet.getString(2);
        }
        return null;
    }

    /**
     * This method is to get current period by providing periodtype and date
     * Currently it supports only quarterly and yearly
     * 
     * @param periodType
     * @param currentDate
     * @return period
     */
    public Period getCurrentPeriod( PeriodType periodType, Date currentDate )
    {
        Period period = new Period();

        String currentPeriodName = "";
        String periodName = "";
        String currentQ = "";

        Calendar calendar = Calendar.getInstance();

        calendar.setTime( currentDate );

        
        int currentMonth = calendar.get( Calendar.MONTH );

        if ( periodType.getName().equalsIgnoreCase( "quarterly" ) )
        {
            if ( currentMonth >= 0 && currentMonth <= 2 )
            {
                periodName = "Jan-Mar " + calendar.get( Calendar.YEAR );
                //currentPeriodName = "Quarterly_" + calendar.get( Calendar.YEAR ) + "-01-01";
                currentPeriodName = calendar.get( Calendar.YEAR ) + "Q1";
                currentQ = calendar.get( Calendar.YEAR ) + "-Q1";
            }
            else if ( currentMonth >= 3 && currentMonth <= 5 )
            {
                periodName = "Apr-Jun " + calendar.get( Calendar.YEAR );
                //currentPeriodName = "Quarterly_" + calendar.get( Calendar.YEAR ) + "-04-01";
                currentPeriodName = calendar.get( Calendar.YEAR ) + "Q2";
                currentQ = calendar.get( Calendar.YEAR ) + "-Q2";
            }
            else if ( currentMonth >= 6 && currentMonth <= 8 )
            {
                periodName = "Jul-Sep " + calendar.get( Calendar.YEAR );
                //currentPeriodName = "Quarterly_" + calendar.get( Calendar.YEAR ) + "-07-01";
                currentPeriodName = calendar.get( Calendar.YEAR ) + "Q3";
                currentQ = calendar.get( Calendar.YEAR ) + "-Q3";
            }
            else
            {
                periodName = "Oct-Dec " + calendar.get( Calendar.YEAR );
                //currentPeriodName = "Quarterly_" + calendar.get( Calendar.YEAR ) + "-10-01";
                currentPeriodName = calendar.get( Calendar.YEAR ) + "Q4";
                currentQ = calendar.get( Calendar.YEAR ) + "-Q4";
            }
        }
        else if ( periodType.getName().equalsIgnoreCase( "yearly" ) )
        {
            periodName = calendar.get( Calendar.YEAR ) + "";
            //currentPeriodName = "Yearly_" + calendar.get( Calendar.YEAR );
            currentPeriodName = "" + calendar.get( Calendar.YEAR );
        }
        
        //System.out.println( currentPeriodName );
        
        period = PeriodType.getPeriodFromIsoString( currentPeriodName );
        
        if( period == null )
        {
            System.out.println( "Period is null" );
        }
        else
        {
            System.out.println( period.getStartDateString() + " - " + period.getEndDateString() + " - " );
        }
        
        Date sDate = getStartDateByString( currentQ );
        Date eDate = getEndDateByString( currentQ );
        Period curPeriod = periodService.getPeriod( sDate, eDate, periodType );
        if( curPeriod == null )
        {
            curPeriod = periodService.reloadPeriod( period );
        }
        period.setId( curPeriod.getId() );
        period.setName( periodName );
        period.setDescription( currentPeriodName );

        return period;
    }

    public static String getKeyIndicatorValueWithThresoldValue( String expression, String indicatorUid )
    {
        boolean isValue = false;

        String indicatorValue = "";
        Pattern valueOperator = Pattern.compile( DE_VALUE );
        Matcher matcherOperator = valueOperator.matcher( expression );
        if ( matcherOperator.find() )
        {
            isValue = true;
        }
        
        expression = expression.replaceAll( DE_VALUE, "" );
        
        String[] patternexpression = expression.trim().split( OPERATOR_AND + "|" + OPERATOR_OR );
        Pattern patternCondition = Pattern.compile( regExp );

        for ( int i = 0; i < patternexpression.length; i++ )
        {
            boolean isAndCondion = false;
            boolean isOrCondion = false;
            String value = "";
            String DEValue = "";
            String subExp = patternexpression[i];
            if ( expression.contains( OPERATOR_AND + subExp ) )
            {
                isAndCondion = true;
            }
            if ( expression.contains( OPERATOR_OR + subExp ) )
            {
                isOrCondion = true;
            }
            
            Matcher matcherCondition = patternCondition.matcher( subExp );
            if ( matcherCondition.find() )
            {

                String match = matcherCondition.group();
                Pattern valueArithMetic = Pattern.compile( ARITHMETIC_OPERATOR );
                Matcher matcherArithMetic = valueArithMetic.matcher( expression );
                if ( matcherArithMetic.find() )
                {
                    String calValue = getCalculatedValue( match );
                    value = calValue.split( "," )[0];
                    indicatorValue = calValue.split( "," )[1];
                }
                else
                {
                    DEValue = getAbsoluteValue( match );
                    indicatorValue += DEValue;
                    String[] contionExp = match.split( OPERAND_EXPRESSION );
                    Pattern conOperator = Pattern.compile( OPERATOR_EXPRESSION );
                    Matcher matchConOperator = conOperator.matcher( contionExp[1] );

                    if ( matchConOperator.find() )
                    {
                        if ( DEValue != "" )
                        {
                            String[] expressionValue = subExp.split( matchConOperator.group() );
                            expressionValue[1] = expressionValue[1].replaceAll( "\\)", "" ).replaceAll( "[\\[\\]]", "" )
                                .replaceAll( "\\(", "" );
                            expressionValue[1] = expressionValue[1].replaceAll( "'", "" );

                            value = getIndicatorValue( matchConOperator.group(), DEValue, expressionValue[1] );

                        }
                    }
                }

            }
            if ( isAndCondion )
            {
                if ( valueMap.containsKey( indicatorUid ) )
                {
                    if ( valueMap.get( indicatorUid ).equalsIgnoreCase( "Yes" ) && value.equalsIgnoreCase( "Yes" ) )
                    {
                        valueMap.put( indicatorUid, "Yes" );
                    }
                    else
                    {
                        valueMap.put( indicatorUid, "No" );
                    }
                }
                else
                {
                    valueMap.put( indicatorUid, value );
                }

            }
            else if ( isOrCondion )
            {
                if ( valueMap.containsKey( indicatorUid ) )
                {
                    if ( valueMap.get( indicatorUid ).equalsIgnoreCase( "Yes" ) || value.equalsIgnoreCase( "Yes" ) )
                    {
                        valueMap.put( indicatorUid, "Yes" );
                    }
                    else
                    {
                        valueMap.put( indicatorUid, "No" );
                    }
                }
                else
                {
                    valueMap.put( indicatorUid, value );
                }
            }
            else
            {
                valueMap.put( indicatorUid, value );
            }
        }

        if ( isValue )
        {
            return indicatorValue;
        }
        else
        {
            return valueMap.get( indicatorUid );
        }

    }

    public static String getIndicatorValue( String condition, String dataElementValue, String expressionValue )
    {
        boolean isDateType = false;
        boolean isBetweenDates = false;
        String value = "";
        Date date1 = null;
        Date date2 = null;
        Date min = null;
        Date max = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        dataElementValue = formatDate(dataElementValue);
        try
        {
            if ( expressionValue.equalsIgnoreCase( "NOW" ) )
            {
                isDateType = true;
                date1 = simpleDateFormat.parse( dataElementValue );
                date2 = new Date();
            }
            else if ( expressionValue.equalsIgnoreCase( "BETWEENNOW,NEXTYEAR" ) )
            {
                isBetweenDates = true;
                Calendar calendar = Calendar.getInstance();
                date1 = simpleDateFormat.parse( dataElementValue );
                min = new Date();
                calendar.setTime( min );
                String maxDate = (calendar.get( Calendar.YEAR ) + 1) + "-" + calendar.get( Calendar.MONTH ) + "-"
                    + calendar.get( Calendar.DATE );
                max = simpleDateFormat.parse( maxDate );
            }
            if ( condition.equals( "=" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() == date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else if ( isBetweenDates )
                {
                    if ( min.getTime() <= date1.getTime() && date1.getTime() <= max.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( dataElementValue.equalsIgnoreCase( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
            else if ( condition.equals( "!=" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() != date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( !(dataElementValue.equalsIgnoreCase( expressionValue )) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
            else if ( condition.equals( "<" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() < date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( Double.parseDouble( dataElementValue ) < Double.parseDouble( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
            else if ( condition.equals( "<=" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() <= date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( Double.parseDouble( dataElementValue ) <= Double.parseDouble( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
            else if ( condition.equals( ">" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() > date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( Double.parseDouble( dataElementValue ) > Double.parseDouble( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
            else if ( condition.equals( ">=" ) )
            {
                if ( isDateType )
                {
                    if ( date1.getTime() >= date2.getTime() )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
                else
                {
                    if ( Double.parseDouble( dataElementValue ) >= Double.parseDouble( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                }
            }
        }
        catch ( Exception e )
        { }
        return value;
    }

    public static String getCalculatedValue( String expression )
    {
        String DataElementValue = "";
        String[] expStrings = expression.split( ARITHMETIC_OPERATOR );
        int count = 0;
        for ( int i = 1; i < expStrings.length; i++ )
        {
            String DEValue1 = getAbsoluteValue( expStrings[count] );
            count++;
            String DEValue2 = getAbsoluteValue( expStrings[i] );
            String[] calExpression = expression.split( OPERAND_EXPRESSION );
            double value = 0;
            if ( calExpression[1].equalsIgnoreCase( "-" ) )
            {
                if ( DEValue1 != "" && DEValue2 != "" )
                {
                    value = Double.parseDouble( DEValue1 ) - Double.parseDouble( DEValue2 );
                }
            }
            else if ( calExpression[1].equalsIgnoreCase( "+" ) )
            {
                if ( DEValue1 != "" && DEValue2 != "" )
                {
                    value = Double.parseDouble( DEValue1 ) + Double.parseDouble( DEValue2 );
                }
            }
            else if ( calExpression[1].equalsIgnoreCase( "*" ) )
            {
                if ( DEValue1 != "" && DEValue2 != "" )
                {
                    value = Double.parseDouble( DEValue1 ) * Double.parseDouble( DEValue2 );
                }
            }
            else if ( calExpression[1].equalsIgnoreCase( "/" ) )
            {
                if ( DEValue1 != "" && DEValue2 != "" )
                {
                    value = Double.parseDouble( DEValue1 ) / Double.parseDouble( DEValue2 );
                }
            }
            String[] contionExp = expStrings[i].split( OPERAND_EXPRESSION );
            Pattern conOperator = Pattern.compile( OPERATOR_EXPRESSION );
            Matcher matchConOperator = conOperator.matcher( contionExp[1] );
            if ( matchConOperator.find() )
            {
                String[] expressionValue = expression.split( matchConOperator.group() );

                expressionValue[1] = expressionValue[1].replaceAll( "\\)", "" ).replaceAll( "[\\[\\]]", "" )
                    .replaceAll( "\\(", "" );
                expressionValue[1] = expressionValue[1].replaceAll( "'", "" );
                String calValue = getIndicatorValue( matchConOperator.group(), value + "", expressionValue[1] );

                DataElementValue = calValue + "," + value;
            }
        }
        return DataElementValue;
    }

    public static String getAbsoluteValue( String expression )
    {
        String DataElementValue = "";        
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        try
        {
            if ( expression.contains( "CURYEAR" ) )
            {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( date );
                DataElementValue = calendar.get( Calendar.YEAR ) + "";
            }
            else if ( expression.contains( "MAX" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService.getDataElementsInExpression( expression ) );
                long curValue = 0;
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                    if ( dv != null )
                    {
                        String value = formatDate( dv.getValue() );
                        if ( expression.contains( "DATEOF" ) )
                        {
                            Date date = standardDateFormat.parse( value );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( date );
                            if ( curValue < date.getTime() )
                            {
                                curValue = date.getTime();
                            }
                        }
                        else if ( expression.contains( "MONTHOF" ) )
                        {
                            Date date = standardDateFormat.parse( value );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( date );
                            if ( curValue < calendar.get( Calendar.MONTH ) )
                            {
                                curValue = calendar.get( Calendar.MONTH );
                            }
                        }
                        else if ( expression.contains( "YEAROF" ) )
                        {
                            Date date = standardDateFormat.parse( value );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( date );
                            if ( curValue < calendar.get( Calendar.YEAR ) )
                            {
                                curValue = calendar.get( Calendar.YEAR );
                            }
                        }
                        else
                        {
                            if ( curValue < Long.parseLong( dv.getValue() ) )
                            {
                                curValue = Long.parseLong( dv.getValue() );
                            }
                        }
                    }
                }
                DataElementValue = curValue + "";
            }
            else if ( expression.contains( "YEAROF" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService
                    .getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                    if ( dv != null )
                    {
                        String value = formatDate( dv.getValue() );
                        Date date = standardDateFormat.parse( value );
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime( date );
                        DataElementValue = calendar.get( Calendar.YEAR ) + "";
                    }
                }
            }
            else if ( expression.contains( "MONTHOF" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService
                    .getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                    if ( dv != null )
                    {
                        String value = formatDate( dv.getValue() );
                        Date date = standardDateFormat.parse( value );
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime( date );
                        DataElementValue = calendar.get( Calendar.MONTH ) + "";
                    }
                }
            }
            else if ( expression.contains( "DATEOF" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService
                    .getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                    if ( dv != null )
                    {
                        DataElementValue = dv.getValue();
                    }
                }
            }
            else
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService.getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit );
                    if ( dv != null )
                    {
                        DataElementValue = dv.getValue();
                    }
                }
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            e.printStackTrace();
        }
        return DataElementValue;
    }
    
    /**
     * Get Start Date from String date foramt (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    public Date getStartDateByString( String dateStr )
    {
        String startDate = "";
        String[] startDateParts = dateStr.split( "-" );
        if ( startDateParts.length <= 1 )
        {
            startDate = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            startDate = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            startDate = startDateParts[0] + "-04-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            startDate = startDateParts[0] + "-07-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            startDate = startDateParts[0] + "-10-01";
        }
        else
        {
            startDate = startDateParts[0] + "-" + startDateParts[1] + "-01";
        }
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            Date sDate = simpleDateFormat.parse( startDate );
            return sDate;
        }
        catch( Exception e )
        {
            System.out.println( "Exception in getStartDateByString : "+ e.getMessage() );
            return null;
        }
        
    }

    /**
     * Get End Date from String date foramt (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    public Date getEndDateByString( String dateStr )
    {
        String endDate = "";
        int monthDays[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        String[] endDateParts = dateStr.split( "-" );
        if ( endDateParts.length <= 1 )
        {
            endDate = endDateParts[0] + "-12-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            endDate = endDateParts[0] + "-03-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            endDate = endDateParts[0] + "-06-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            endDate = endDateParts[0] + "-09-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            endDate = endDateParts[0] + "-12-31";
        }
        else
        {
            if ( Integer.parseInt( endDateParts[0] ) % 400 == 0 )
            {
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )] + 1);
            }
            else
            {
                endDate = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )]);
            }
        }
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            Date eDate = simpleDateFormat.parse( endDate );
            return eDate;
        }
        catch( Exception e )
        {
            System.out.println( "Exception in getStartDateByString : "+ e.getMessage() );
            return null;
        }

    }
 
   

    public String getQuarterFormatByString( String dateStr )
    {
        String startDate = "";
        String[] startDateParts = dateStr.split( "-" );
        
        if ( startDateParts.length <= 1 )
        {
            startDate = startDateParts[0] + "Q1";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            startDate = startDateParts[0] + "Q1";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            startDate = startDateParts[0] + "Q2";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            startDate = startDateParts[0] + "Q3";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            startDate = startDateParts[0] + "Q4";
        }
        else if( startDateParts[1].equalsIgnoreCase( "01" ) || startDateParts[1].equalsIgnoreCase( "02" ) || startDateParts[1].equalsIgnoreCase( "03" ) )
        {
            startDate = startDateParts[0] + "Q1";
        }
        else if( startDateParts[1].equalsIgnoreCase( "04" ) || startDateParts[1].equalsIgnoreCase( "05" ) || startDateParts[1].equalsIgnoreCase( "06" ) )
        {
            startDate = startDateParts[0] + "Q2";
        }
        else if( startDateParts[1].equalsIgnoreCase( "07" ) || startDateParts[1].equalsIgnoreCase( "08" ) || startDateParts[1].equalsIgnoreCase( "09" ) )
        {
            startDate = startDateParts[0] + "Q3";
        }
        else if( startDateParts[1].equalsIgnoreCase( "10" ) || startDateParts[1].equalsIgnoreCase( "11" ) || startDateParts[1].equalsIgnoreCase( "12" ) )
        {
            startDate = startDateParts[0] + "Q4";
        }
        
        return startDate;
        
    }    
    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMap( String dataElementIdsByComma, String orgUnitIdsByComma, String startDate, String endDate, String commentType, Period curPeriod )
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT * FROM ( "+
                            "SELECT dva.dataelementid, dva.organisationunitid, dva.periodid, dva.value, dva.modifiedby, dva.timestamp, dva.comment, dva.status, period.startDate FROM datavalueaudit AS dva " +
                            " INNER JOIN period ON dva.periodid = period.periodid  " +
                            " WHERE " +                                
                                " dva.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                " dva.organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                                " dva.commenttype = '"+ commentType +"' AND " +
                                " DATE(dva.timestamp) BETWEEN '"+ startDate +"' AND '"+ endDate +"' "; 
                                //" dva.timestamp >= '"+ startDate +"' AND " +
                                //" dva.timestamp <= '"+ endDate +"' ";
                                if( curPeriod != null )
                                {
                                    query += " AND dva.periodid = " + curPeriod.getId() + " ";
                                }                                
                              query += " UNION "+
                              " SELECT dv.dataelementid, dv.sourceid, dv.periodid, dv.VALUE, dv.storedby, dv.lastupdated, dv.COMMENT, dv.STATUS, period.startDate FROM datavalue AS dv " +
                            " INNER JOIN period ON dv.periodid = period.periodid  " +
                            " WHERE " +                                
                                " dv.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                " dv.sourceid IN ("+ orgUnitIdsByComma +") AND " +                                
                                " dv.lastupdated >= '"+ startDate +"' AND " +
                                " dv.lastupdated <= '"+ endDate +"' ";
                                if( curPeriod != null )
                                {
                                    query += " AND dv.periodid = " + curPeriod.getId() + " ";
                                }  
                                query += " ) a ORDER BY a.timestamp, a.startDate DESC";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );

                Integer periodId = rs.getInt( 3 );
                Period period = periodService.getPeriod( periodId );
                
                String value = rs.getString( 4 );
                String storedBy = rs.getString( 5 );
                String lastUpdated = rs.getString( 6 );
                String comment = rs.getString( 7 );
                Integer status = rs.getInt( 8 );
                
                DataValueAudit dva = new DataValueAudit();
                
                DataValue dv = new DataValue();
                dv.setSource( ou );
                dv.setDataElement( de );
                dv.setPeriod( period );

                dva.setOrganisationUnit( ou );
                dva.setDataElement( de );
                dva.setPeriod( period );
                
                //dva.setDataValue( dv );
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditWithValueMap( String dataElementIdsByComma, String orgUnitIdsByComma, String startDate, String endDate, String commentType, Period curPeriod )
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT * FROM ( "+
                            "SELECT dva.dataelementid, dva.organisationunitid, dva.periodid, dva.value, dva.modifiedby, dva.timestamp, dva.comment, dva.status, period.startDate FROM datavalueaudit AS dva " +
                            " INNER JOIN period ON dva.periodid = period.periodid " +
                            " WHERE " +                                
                                " dva.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                " dva.organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                                " dva.commenttype = '"+ commentType +"' AND " +
                                " dva.value IS NOT NULL AND " +
                                " DATE(dva.timestamp) BETWEEN '"+ startDate +"' AND '"+ endDate +"' ";
                                //" dva.timestamp >= '"+ startDate +"' AND " +
                                //" dva.timestamp <= '"+ endDate +"' ";
                                if( curPeriod != null )
                                {
                                    query += " AND dva.periodid = " + curPeriod.getId() + " ";
                                } 
                                query += " UNION "+
                                    " SELECT dataelementid, sourceid, dva.periodid, VALUE, storedby, lastupdated, COMMENT, STATUS, period.startDate FROM datavalue AS dva " +
                                  " INNER JOIN period ON dva.periodid = period.periodid  " +
                                  " WHERE " +                                
                                      " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                      " VALUE IS NOT NULL AND " +
                                      " sourceid IN ("+ orgUnitIdsByComma +") AND " +                                
                                      " lastupdated >= '"+ startDate +"' AND " +
                                      " lastupdated <= '"+ endDate +"' ";
                                      if( curPeriod != null )
                                      {
                                          query += " AND dva.periodid = " + curPeriod.getId() + " ";
                                      }  
                                query += " ) a ORDER BY a.timestamp, a.startDate DESC";
                                
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );

                Integer periodId = rs.getInt( 3 );
                Period period = periodService.getPeriod( periodId );
                
                String value = rs.getString( 4 );
                String storedBy = rs.getString( 5 );
                String lastUpdated = rs.getString( 6 );
                String comment = rs.getString( 7 );
                Integer status = rs.getInt( 8 );
                
                DataValueAudit dva = new DataValueAudit();
                
                DataValue dv = new DataValue();
                dv.setSource( ou );
                dv.setDataElement( de );
                dv.setPeriod( period );

                dva.setOrganisationUnit( ou );
                dva.setDataElement( de );
                dva.setPeriod( period );
                
                //dva.setDataValue( dv );
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }

    public Map<String, List<Period>> getOrgUnit_DataElement_WithoutValueMap( String dataElementIdsByComma, String orgUnitIdsByComma, String startDate, String endDate, String commentType, Period curPeriod )
    {
        Map<String, List<Period>> blankDataElementValueMap = new HashMap<String, List<Period>>();
        
        Map<OrganisationUnit, List<DataValue>> dataValueAuditMap = new HashMap<OrganisationUnit, List<DataValue>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT dataelementid, sourceid, p.periodid FROM datasetmembers dsm " +
                                " INNER JOIN datasetsource dss ON dss.datasetid=dsm.datasetid " +
                                " INNER JOIN dataset ds ON ds.datasetid = dss.datasetid " +
                                " INNER JOIN period p ON p.periodtypeid = ds.periodtypeid " +
                                " WHERE " +
                                    " dataelementid IN ( " + dataElementIdsByComma + " ) AND " +
                                    " sourceid IN ( " + orgUnitIdsByComma + " ) AND " + 
                                    " p.startDate >= '" + startDate +"' AND p.startDate <= '" + endDate +"' AND " + 
                                    " CONCAT( dataelementid,sourceid,p.periodid ) NOT IN " + 
                                        "(" +
                                            "SELECT CONCAT(dataelementid,sourceid,periodid) FROM datavalue " +
                                                " WHERE " +
                                                    " dataelementid IN ( "+ dataElementIdsByComma +") AND " +
                                                    " sourceid IN ( " + orgUnitIdsByComma + ") AND " + 
                                                    " lastupdated >= '"+ startDate +"' AND " +                                
                                                    " lastupdated <= '"+ endDate +"' ";

                                                    if( curPeriod != null )
                                                    {
                                                        query += " AND periodid = " + curPeriod.getId() + " ";
                                                    }                                
                                                    
                                                query += ") ORDER BY dataelementid, periodid";
                                                
            //System.out.println( "Query: " + query );

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                //DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                //OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );

                Integer periodId = rs.getInt( 3 );
                Period period = periodService.getPeriod( periodId );

                List<Period> periods = blankDataElementValueMap.get( ouId+":"+deId );
                if( periods == null )
                {
                    periods = new ArrayList<Period>();
                }
                periods.add( period );
                blankDataElementValueMap.put( ouId+":"+deId, periods );
                
                /*
                DataValue dataValue = new DataValue();
                dataValue.setSource( ou );
                dataValue.setDataElement( de );
                dataValue.setPeriod( period );
                
                List<DataValue> dataValues = dataValueAuditMap.get( ou );
                if( dataValues == null )
                {
                    dataValues = new ArrayList<DataValue>();
                }
                dataValues.add( dataValue );
                dataValueAuditMap.put( ou, dataValues );
                */
                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return blankDataElementValueMap;
    }
    
    
    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditWithoutValueMap( String dataElementIdsByComma, String orgUnitIdsByComma, String startDate, String endDate, String commentType, Period curPeriod )
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT * FROM ( "+
                            "SELECT dva.dataelementid, dva.organisationunitid, dva.periodid, dva.value, dva.modifiedby, dva.timestamp, dva.COMMENT, dva.STATUS, period.startDate FROM datavalueaudit AS dva " +
                            " INNER JOIN period ON dva.periodid = period.periodid " +
                            " WHERE " +                                
                                " dva.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                " dva.organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                                " dva.commenttype = '"+ commentType +"' AND " +
                                " dva.VALUE IS NULL AND " +
                                " DATE(dva.timestamp) BETWEEN '"+ startDate +"' AND '"+ endDate +"' ";
                                //" dva.timestamp >= '"+ startDate +"' AND " +                                
                                //" dva.timestamp <= '"+ endDate +"' ";
            
                                if( curPeriod != null )
                                {
                                    query += " AND dva.periodid = " + curPeriod.getId() + " ";
                                }                                
                                query += " UNION "+
                                    " SELECT dataelementid, sourceid, dva.periodid, VALUE, storedby, lastupdated, COMMENT, STATUS, period.startDate FROM datavalue AS dva " +
                                  " INNER JOIN period ON dva.periodid = period.periodid  " +
                                  " WHERE " +                                
                                      " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                      " VALUE IS NULL AND " +
                                      " sourceid IN ("+ orgUnitIdsByComma +") AND " +                                
                                      " lastupdated >= '"+ startDate +"' AND " +
                                      " lastupdated <= '"+ endDate +"' ";
                                      if( curPeriod != null )
                                      {
                                          query += " AND dva.periodid = " + curPeriod.getId() + " ";
                                      }  
                                query += " ) a ORDER BY a.timestamp, a.startDate DESC";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );

                Integer periodId = rs.getInt( 3 );
                Period period = periodService.getPeriod( periodId );
                
                String value = rs.getString( 4 );
                String storedBy = rs.getString( 5 );
                String lastUpdated = rs.getString( 6 );
                String comment = rs.getString( 7 );
                Integer status = rs.getInt( 8 );
                
                DataValueAudit dva = new DataValueAudit();
                
                DataValue dv = new DataValue();
                dv.setSource( ou );
                dv.setDataElement( de );
                dv.setPeriod( period );

                dva.setOrganisationUnit( ou );
                dva.setDataElement( de );
                dva.setPeriod( period );
                
                //dva.setDataValue( dv );
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }
    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMap( String dataElementIdsByComma, String orgUnitIdsByComma, Integer periodId, String commentType )
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT dataelementid, organisationunitid, VALUE, modifiedby, dva.timestamp, COMMENT, STATUS FROM datavalueaudit dva " +
                " WHERE " +
                    " periodid = "+ periodId +" AND " +
                    " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                    " organisationunitid IN ("+ orgUnitIdsByComma +") AND " +                    
                    " commenttype = '"+ commentType +"' ORDER BY periodid, dva.timestamp DESC";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                
                String value = rs.getString( 3 );
                String storedBy = rs.getString( 4 );
                String lastUpdated = rs.getString( 5 );
                String comment = rs.getString( 6 );
                Integer status = rs.getInt( 7 );
                
                DataValueAudit dva = new DataValueAudit();
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }

    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMapForConflicts( String dataElementIdsByComma, String orgUnitIdsByComma, Integer periodId )
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT dva.dataelementid, dva.organisationunitid, dva.VALUE, dva.modifiedby, dva.timestamp, dva.COMMENT, dva.STATUS FROM datavalueaudit AS dva, datavalue AS dv " +  
                               " WHERE " + 
                                   " dv.periodid = dva.periodid AND " + 
                                   " dv.sourceid = dva.organisationunitid AND " + 
                                   " dv.dataelementid = dva.dataelementid AND " + 
                                   " dv.followup = TRUE AND " + 
                                   " dva.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                   " dva.organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                                   " dva.periodid = "+ periodId +" ORDER BY dva.timestamp ASC"; 
                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                
                String value = rs.getString( 3 );
                String storedBy = rs.getString( 4 );
                String lastUpdated = rs.getString( 5 );
                String comment = rs.getString( 6 );
                Integer status = rs.getInt( 7 );
                
                DataValueAudit dva = new DataValueAudit();
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }

    
    public Map<String, String> getClosedDiscussions( String dataElementIdsByComma, String orgUnitIdsByComma, Integer periodId )
    {
        Map<String, String> colsedDiscussionMap = new HashMap<String, String>();
        DatabaseInfo dataBaseInfo = databaseInfoProvider.getDatabaseInfo();

        try
        {
            String query = "";
            if ( dataBaseInfo.getType().equalsIgnoreCase( "mysql" ) )
           
            {
            query = "SELECT dv.dataelementid, dv.sourceid FROM datavalue AS dv " +  
                               " WHERE " + 
                                   " dv.status = FALSE AND " + 
                                   " dv.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                   " dv.sourceid IN ("+ orgUnitIdsByComma +") AND " +
                                   " dv.periodid = "+ periodId;
                                   		
            }
            else if ( dataBaseInfo.getType().equalsIgnoreCase( "postgresql" ) )
            {
                query = "SELECT dv.dataelementid, dv.sourceid FROM datavalue AS dv " +  
                    " WHERE " + 
                        " dv.status = 0 AND " + 
                        " dv.dataelementid IN ("+ dataElementIdsByComma +") AND " +
                        " dv.sourceid IN ("+ orgUnitIdsByComma +") AND " +
                        " dv.periodid = "+ periodId;
                
            }

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                Integer ouId = rs.getInt( 2 );

                colsedDiscussionMap.put( ouId+":"+deId, "Discussion is closed" );
                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return colsedDiscussionMap;
    }
    
    public List<DataValue> getConflictDataValueList( String dataElementIdsByComma, String orgUnitIdsByComma, Period curPeriod )
    {
        List<DataValue> conflictDataValueList = new ArrayList<DataValue>();
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT dataelementid, sourceid, periodid, VALUE, storedby, lastupdated, comment, status FROM datavalue AS dv " +
                            " WHERE " +                                
                                " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                " sourceid IN ("+ orgUnitIdsByComma +") AND " +
                                " periodid = " + curPeriod.getId() + " AND " +
                                " followup = true";
                                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );            

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );

                Integer periodId = rs.getInt( 3 );
                Period period = periodService.getPeriod( periodId );
                
                String value = rs.getString( 4 );
                String storedBy = rs.getString( 5 );
                String lastUpdated = rs.getString( 6 );
                String comment = rs.getString( 7 );
                Integer status = rs.getInt( 8 );
                
                DataValue dv = new DataValue();
                dv.setSource( ou );
                dv.setDataElement( de );
                dv.setPeriod( period );
                dv.setValue( value );
                dv.setComment( comment );
                dv.setStatus( status );
                dv.setStoredBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dv.setLastUpdated( timeStamp );
                
                conflictDataValueList.add( dv );               
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return conflictDataValueList;
    }

    
    public Map<String, DataValue> getLatestDataValues( String dataElementIdsByComma, String orgUnitIdsByComma )
    {
        Map<String, DataValue> latestDataValues = new HashMap<String, DataValue>();
        
        try
        {
            String query = "SELECT dv.sourceid, dv.dataelementid, dv.periodid, dv.value, dv.comment, dv.storedby, dv.lastupdated FROM datavalue dv " +
                            " INNER JOIN period p ON dv.periodid = p.periodid " + 
                            " WHERE " +
                                " CONCAT(dv.sourceid,\",\",dv.dataelementid,\",\",p.startdate) " +
                                    " IN ( "+ 
                                            " SELECT CONCAT( sourceid,\",\",dataelementid,\",\",MAX(period.startdate) ) FROM datavalue " +
                                                " INNER JOIN period ON datavalue.periodid = period.periodid " + 
                                                    " WHERE sourceid IN (" + orgUnitIdsByComma +") AND dataelementid IN ("+ dataElementIdsByComma +") " + 
                                                    " GROUP BY sourceid,dataelementid" +
                                          " ) ORDER BY sourceid, dataelementid";
                                                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer ouId = rs.getInt( 1 );
                Integer deId = rs.getInt( 2 );
                Integer pId = rs.getInt( 3 );
                String value = rs.getString( 4 );
                String comment = rs.getString( 5 );
                String storedBy = rs.getString( 6 );
                Date lastUpdated = rs.getDate( 7 );
                
                DataElement de = dataElementService.getDataElement( deId );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                Period p = periodService.getPeriod( pId );
                
                DataValue dv = new DataValue();
                dv.setDataElement( de );
                dv.setSource( ou );
                dv.setPeriod( p );
                dv.setValue( value );
                dv.setComment( comment );
                dv.setStoredBy( storedBy );
                dv.setLastUpdated( lastUpdated );
                
                latestDataValues.put( ouId+":"+deId, dv );
                
                //System.out.println( dv.getValue() );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getLatestDataValues", e );
        }
        
        return latestDataValues;
    }

    
    public Map<Integer, List<String>> getHPVDemoYearsByCountry( String orgUnitIdsByComma, String hpvDemoYear )
    {
        Map<Integer, List<String>> resultMap = new HashMap<Integer, List<String>>();
        
        try
        {
            String query = "SELECT dva.organisationunitid,dva.value "+
            " FROM datavalueaudit dva "+
            " WHERE dva.dataelementid = "+ hpvDemoYear + 
            " AND dva.organisationunitid IN (" + orgUnitIdsByComma +" ) "+ 
            " AND dva.commenttype LIKE 'H' "+
            " AND dva.value in ('Year 1', 'Year 2','Year 3') " +
            " GROUP BY dva.organisationunitid, dva.value " +
            " ORDER BY dva.organisationunitid,dva.value";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer ouId = rs.getInt( 1 );
                String demoY = rs.getString( 2 );

                List<String> demoYears = resultMap.get( ouId );
                if( demoYears == null || demoYears.size() <= 0 )
                {
                    demoYears = new ArrayList<String>();
                }
                
                demoYears.add( demoY );
                resultMap.put( ouId, demoYears );
            }
        }        
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getLatestDataValues", e );
        }
    
        return resultMap;
    }
    
    public Map<String, DataValue> getLatestDataValuesForHPVDemoReport( String hpvDemoYearDes, String orgUnitIdsByComma, String hpvDemoYear )
    {
        Map<String, DataValue> latestDataValues = new HashMap<String, DataValue>();
        
        try
        {
            /*
            String query = "SELECT ou, case when yr like 'yr1' then 'Year 1' when yr like 'yr2' then 'Year 2' when yr like 'yr3' then 'Year 3' end 'yr'";
            
            for( String hpvDemoYearDe : hpvDemoYearDes.split( "," ) )
            {
                query += ",  GROUP_CONCAT("+hpvDemoYearDe + "d)";
            }
            
            query += " FROM " + 
            "(" +
                " SELECT ou,yr ";
                for( String hpvDemoYearDe : hpvDemoYearDes.split( "," ) )
                {
                    query += ", CASE WHEN dataelementid = "+ hpvDemoYearDe + " THEN val END AS '"+hpvDemoYearDe+"d'";
                }
            
            query += " FROM " +
                "(" +
                " SELECT C1.dataelementid, C1.ou, C2.val, C1.yr FROM " +
                   " ( "+
                   " SELECT dataelementid,ou,MAX(ts) 'ts' ,yr "+
                   " FROM "+
                   " ( " +
                   " SELECT dataelementid, ou, val, ts, yr1, yr2, CASE WHEN yr2 IS NULL THEN 'yr1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'yr1' " + 
                   " WHEN yr3 IS NULL THEN 'yr2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'yr2' ELSE 'yr3' END 'yr' "+
                   " FROM "+
                   " ( " +
                           " SELECT * FROM " + 
                           " ( " +
                                  " SELECT dva.dataelementid,dva.organisationunitid 'ou',concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' " +
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid IN (" + hpvDemoYearDes + ") " +
                                   " AND dva.organisationunitid IN (" + orgUnitIdsByComma +")"+ 
                            ")A1" +
                            " INNER JOIN " + 
                            "( "+
                                   " SELECT GROUP_CONCAT(yr1) 'yr1',GROUP_CONCAT(yr2) 'yr2',GROUP_CONCAT(yr3) 'yr3' "+
                                   " FROM "+
                                   " ( "+
                                   " SELECT CASE WHEN val LIKE 'Year 1' THEN ts END AS 'yr1', "+
                                   " CASE WHEN val LIKE 'Year 2' THEN ts END AS 'yr2', "+
                                   " CASE WHEN val LIKE 'Year 3' THEN ts END AS 'yr3' "+ 
                                   " FROM "+ 
                                   " ( "+
                                   " SELECT dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',MAX(dva.timestamp) 'ts',dva.value 'val' "+
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid = " + hpvDemoYear +
                                   " AND dva.organisationunitid IN (" + orgUnitIdsByComma + ")" + 
                                   " AND dva.commenttype LIKE 'H' "+    
                                   " GROUP BY dva.organisationunitid, dva.value "+
                                   " )asd " +
                                   " )sag " +
                            ")A2 " +
                            " ON 1=1 " +
                    ")B " +
                    " )fin "+
                    " GROUP BY dataelementid,ou,yr " +
            ")C1 "+
            " INNER JOIN "+ 
            "( "+
                  " SELECT dataelementid,ou,val,ts,yr1,yr2,CASE WHEN yr2 IS NULL THEN 'yr1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'yr1' "+ 
                  " WHEN yr3 IS NULL THEN 'yr2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'yr2' ELSE 'yr3' END 'yr' "+
                  " FROM "+
                   " ( " +
                           " SELECT * FROM "+ 
                           " ( "+
                                 " SELECT dva.dataelementid,dva.organisationunitid 'ou',concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' "+
                                   " FROM datavalueaudit dva " +
                                   " WHERE dva.dataelementid IN ("+ hpvDemoYearDes +") "+ 
                                   " AND dva.organisationunitid IN (" + orgUnitIdsByComma + ") "+ 
                                    
                            ")A1 "+
                            " INNER JOIN "+ 
                            "( "+
                                   " SELECT GROUP_CONCAT(yr1) 'yr1',GROUP_CONCAT(yr2) 'yr2',GROUP_CONCAT(yr3) 'yr3' "+
                                   " FROM "+
                                   " ( "+
                                    " SELECT CASE WHEN val LIKE 'Year 1' THEN ts END AS 'yr1', " +
                                    " CASE WHEN val LIKE 'Year 2' THEN ts END AS 'yr2', "+
                                    " CASE WHEN val LIKE 'Year 3' THEN ts END AS 'yr3' "+ 
                                    " FROM " + 
                                    "( "+
                                    " SELECT dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',MAX(dva.timestamp) 'ts',dva.value 'val' "+
                                    " FROM datavalueaudit dva "+
                                    " WHERE dva.dataelementid=" + hpvDemoYear + 
                                    " AND dva.organisationunitid IN ("+ orgUnitIdsByComma + ")" + 
                                    " AND dva.commenttype LIKE 'H' "+   
                                    " GROUP BY dva.organisationunitid,dva.value "+
                                    " )asd "+
                                    " )sag "+
                            ")A2 "+
                            " ON 1=1 "+
                    ")B "+
            ")C2 "+
            " ON C1.ts=C2.ts "+
            " AND C1.dataelementid=C2.dataelementid "+
            " AND C1.ou=C2.ou "+
            " AND C1.yr=C2.yr "+
            " GROUP BY C1.yr,C1.ou,C1.dataelementid "+

            " union "+

            " SELECT C1.dataelementid,C1.ou,C2.val,'yr2' FROM " +
                    " ( "+
                    " SELECT dataelementid,ou,MAX(ts) 'ts' ,yr "+
                    " FROM "+
                    " ( "+
                    " SELECT dataelementid,ou,val,ts,yr1,yr2,CASE WHEN yr2 IS NULL THEN 'yr1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'yr1' "+ 
                    " WHEN yr3 IS NULL THEN 'yr2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'yr2' ELSE 'yr3' END 'yr' "+
                    " FROM "+
                    " ( "+
                          "  SELECT * FROM "+ 
                            " ( "+
                                   " SELECT dva.dataelementid,dva.organisationunitid 'ou',concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' "+
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid IN ("+ hpvDemoYearDes +") "+ 
                                   " AND dva.organisationunitid IN ("+ orgUnitIdsByComma +") "+ 
                            ")A1 "+
                            " INNER JOIN "+ 
                            " ( "+
                                   " SELECT GROUP_CONCAT(yr1) 'yr1',GROUP_CONCAT(yr2) 'yr2',GROUP_CONCAT(yr3) 'yr3' "+
                                   " FROM "+
                                   " ( "+
                                   " SELECT CASE WHEN val LIKE 'Year 1' THEN ts END AS 'yr1', "+
                                   " CASE WHEN val LIKE 'Year 2' THEN ts END AS 'yr2', "+
                                   " CASE WHEN val LIKE 'Year 3' THEN ts END AS 'yr3' "+ 
                                   " FROM "+ 
                                   " ( "+
                                   " SELECT dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',MAX(dva.timestamp) 'ts',dva.value 'val' "+
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid= " + hpvDemoYear +
                                   " AND dva.organisationunitid IN ("+ orgUnitIdsByComma +") "+ 
                                   " AND dva.commenttype LIKE 'H' "+    
                                   " GROUP BY dva.organisationunitid,dva.value "+
                                   " )asd "+
                                   " )sag "+
                           " )A2 "+
                           " ON 1=1 "+
                   " )B "+
                   " )fin "+
                   " GROUP BY dataelementid,ou,yr "+
            ")C1 "+
            " INNER JOIN "+ 
            "( "+
                   " SELECT dataelementid,ou,val,ts,yr1,yr2,CASE WHEN yr2 IS NULL THEN 'yr1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'yr1' "+ 
                   " WHEN yr3 IS NULL THEN 'yr2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'yr2' ELSE 'yr3' END 'yr' "+
                   " FROM " +
                   " ( "+
                          "  SELECT * FROM "+ 
                           " ( " +
                                  "  SELECT dva.dataelementid,dva.organisationunitid 'ou',concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' "+
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid IN ("+ hpvDemoYearDes +") "+ 
                                   " AND dva.organisationunitid IN ("+ orgUnitIdsByComma + ") "+                                     
                            ")A1 "+
                            " INNER JOIN "+ 
                            "( "+
                                   " SELECT GROUP_CONCAT(yr1) 'yr1',GROUP_CONCAT(yr2) 'yr2',GROUP_CONCAT(yr3) 'yr3' "+
                                   " FROM "+
                                   " ( "+
                                   " SELECT CASE WHEN val LIKE 'Year 1' THEN ts END AS 'yr1', "+
                                   " CASE WHEN val LIKE 'Year 2' THEN ts END AS 'yr2', "+
                                   " CASE WHEN val LIKE 'Year 3' THEN ts END AS 'yr3' "+ 
                                   " FROM "+ 
                                   " ( "+
                                   " SELECT dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',MAX(dva.timestamp) 'ts',dva.value 'val' "+
                                   " FROM datavalueaudit dva "+
                                   " WHERE dva.dataelementid= "+ hpvDemoYear + 
                                   " AND dva.organisationunitid IN ("+ orgUnitIdsByComma +") "+ 
                                   " AND dva.commenttype LIKE 'H' "+    
                                   " GROUP BY dva.organisationunitid,dva.value "+
                                   " )asd "+
                                   " )sag "+
                            ")A2 "+
                           " ON 1=1 "+
                    ")B "+
            ")C2 "+
            " ON C1.ts=C2.ts "+
            " AND C1.dataelementid=C2.dataelementid "+
            " AND C1.ou=C2.ou "+
            " AND C1.yr=C2.yr "+
            " GROUP BY C1.yr,C1.ou,C1.dataelementid "+
            " )sag "+
            " )sag1 "+
            " GROUP BY ou,yr "+
            " ORDER BY ou,yr ";
            
            */
            
            
            String query = "SELECT ou, yr";
            
            for( String hpvDemoYearDe : hpvDemoYearDes.split( "," ) )
            {
                query += ",  GROUP_CONCAT("+hpvDemoYearDe + "d)";
            }

            query += " from ( select ou, yr";

            for( String hpvDemoYearDe : hpvDemoYearDes.split( "," ) )
            {
                query += ", case when dataelementid = "+ hpvDemoYearDe + " then val end as '"+hpvDemoYearDe+"d'";
            }
             
            query += " from "+
            "( " +
            " select C1.dataelementid,C1.ou,C2.val,C1.yr from " +
                    "("+
                    "select dataelementid,ou,max(ts) 'ts' ,yr "+
                    "from "+
                    "( "+
                    " select dataelementid,ou,val,ts,yr1,yr2, CASE WHEN yr2 IS NULL THEN 'Year 1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'Year 1' "+
                    " WHEN yr3 IS NULL THEN 'Year 2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'Year 2' ELSE 'Year 3' END 'yr' " +
                    " from " +
                    "( "+
                            "select A1.*,A2.yr1,A2.yr2,A2.yr3 from "+ 
                            "("+
                                   " select dva.dataelementid,dva.organisationunitid 'ou', concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' "+
                                   " from datavalueaudit dva "+
                                   " where dva.dataelementid in ("+hpvDemoYearDes+")"+ 
                                   " and dva.organisationunitid in ("+orgUnitIdsByComma+")"+ 
                                   " and dva.commenttype like 'H' "+
                            ")A1"+
                            " inner join "+ 
                            "( "+
                                   " select ou,group_concat(yr1) 'yr1',group_concat(yr2) 'yr2',group_concat(yr3) 'yr3' "+
                                   " from "+
                                   "("+
                                   " select ou,case when val like 'Year 1' then ts end as 'yr1' ," +
                                   " case when val like 'Year 2' then ts end as 'yr2' ," +
                                   " case when val like 'Year 3' then ts end as 'yr3' "+ 
                                   " from "+ 
                                   " ( "+
                                   " select dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',max(dva.timestamp) 'ts',dva.value 'val' "+
                                   " from datavalueaudit dva "+
                                   " where dva.dataelementid= "+ hpvDemoYear + 
                                   " and dva.organisationunitid in ("+ orgUnitIdsByComma +") "+  
                                   " and dva.commenttype like 'H' "+    
                                   " group by dva.organisationunitid,dva.value "+
                                   " )asd "+
                                   " )sag GROUP BY ou"+
                           " )A2 "+
                           " on A1.ou=A2.ou "+
                    " )B "+
                    " )fin "+
                    " group by dataelementid,ou,yr "+
            ")C1 "+
            " inner join "+ 
            "( "+
                   " select dataelementid,ou,val,ts,yr1,yr2, CASE WHEN yr2 IS NULL THEN 'Year 1' WHEN yr2 IS NOT NULL AND ts < yr2  THEN 'Year 1' " +
                   " WHEN yr3 IS NULL THEN 'Year 2' WHEN yr3 IS NOT NULL AND ts BETWEEN yr2 AND yr3 THEN 'Year 2' ELSE 'Year 3' END 'yr' "+
                   " from "+
                   " ( "+
                          "  select A1.*,A2.yr1,A2.yr2,A2.yr3 from "+ 
                           " ( "+
                                  "  select dva.dataelementid,dva.organisationunitid 'ou',concat(dva.value,'#@#',ifnull(dva.comment,'')) 'val',dva.timestamp 'ts' "+
                                  "  from datavalueaudit dva "+
                                  "  where dva.dataelementid in (" + hpvDemoYearDes +") "+ 
                                  "  and dva.organisationunitid in (" + orgUnitIdsByComma + ") " +  
                                  " and dva.commenttype like 'H' "+
                            " )A1 "+
                            " inner join "+ 
                            "(" +
                               "     select ou, group_concat(yr1) 'yr1',group_concat(yr2) 'yr2',group_concat(yr3) 'yr3' "+
                                   " from "+
                                   " ( "+
                                   " select ou, case when val like 'Year 1' then ts end as 'yr1' , "+
                                   " case when val like 'Year 2' then ts end as 'yr2' , "+
                                   " case when val like 'Year 3' then ts end as 'yr3' "+ 
                                   " from "+ 
                                   " ( "+
                                   " select dva.dataelementid,dva.periodid,dva.organisationunitid 'ou',max(dva.timestamp) 'ts',dva.value 'val' "+
                                   " from datavalueaudit dva "+
                                   " where dva.dataelementid= "+hpvDemoYear + 
                                   " and dva.organisationunitid in ("+ orgUnitIdsByComma +")" +
                                   " and dva.commenttype like 'H' "+    
                                   " group by dva.organisationunitid,dva.value "+
                                   " )asd "+
                                   " )sag GROUP BY ou"+
                           " )A2 "+
                           " on A2.ou=A1.ou "+
                   " )B "+
            " )C2 "+
            " on C1.ts=C2.ts "+
            " and C1.dataelementid=C2.dataelementid "+
            " and C1.ou=C2.ou "+
            " and C1.yr=C2.yr "+
            " group by C1.yr,C1.ou,C1.dataelementid "+
            " )sag "+
            " )sag1"+
            " group by ou,yr "+
            " order by ou,yr ";
            
            
            //System.out.println( query );
            
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer ouId = rs.getInt( 1 );
                String demoY = rs.getString( 2 );

                int i = 2;
                for( String hpvDemoYearDe : hpvDemoYearDes.split( "," ) )
                {
                    i++;
                    String value = rs.getString( i );
                    
                    DataValue dv = new DataValue();
                    try
                    {
                        if( value != null )
                        {                        
                            try
                            {
                                dv.setValue( value.split("#@#")[0] );
                            }
                            catch( Exception e )
                            {
                                dv.setValue( "" );
                            }
                            
                            try
                            {
                                dv.setComment( value.split("#@#")[1] );
                            }
                            catch( Exception e )
                            {
                                dv.setComment( "" );
                            }
                        }
                    }
                    catch( Exception e )
                    {
                        dv.setValue( "" );
                        dv.setComment( "" );
                    }
                    
                    latestDataValues.put( ouId+":"+demoY+":"+hpvDemoYearDe, dv );
                }
                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getLatestDataValues", e );
        }
        
        return latestDataValues;
    }
    
    public Map<String, DataValue> getLatestDataValuesForTabularReport( String dataElementIdsByComma, String orgUnitIdsByComma )
    {
        Map<String, DataValue> latestDataValues = new HashMap<String, DataValue>();
        
        try
        {
            String query = "SELECT dv.sourceid, dv.dataelementid, dv.periodid, dv.value, dv.comment, dv.storedby, dv.lastupdated " +
                            " FROM " +
                                "( " +
                                    " SELECT periodid,dataelementid,sourceid FROM " + 
                                        "(SELECT MAX(p.startdate) AS startdate,dv.dataelementid,dv.sourceid FROM datavalue dv " +
                                            " INNER JOIN period p ON p.periodid=dv.periodid " +
                                                " WHERE dv.dataelementid IN ( "+ dataElementIdsByComma +") AND dv.sourceid IN ( " + orgUnitIdsByComma + " ) " + 
                                                " GROUP BY dv.dataelementid,dv.sourceid " +
                                         ")asd " +
                                     " INNER JOIN period p ON p.startdate=asd.startdate " +
                                 ")asd1 " +
                             " INNER JOIN datavalue dv ON dv.sourceid=asd1.sourceid " +
                             " AND dv.dataelementid=asd1.dataelementid " +
                             " AND dv.periodid=asd1.periodid";
            
            //System.out.println( query );
            
            /*
            String query = "SELECT dv.sourceid, dv.dataelementid, dv.periodid, dv.value, dv.comment, dv.storedby, dv.lastupdated FROM datavalue dv " +
                            " INNER JOIN period p ON dv.periodid = p.periodid " + 
                            " WHERE " +
                                " CONCAT(dv.sourceid,\",\",dv.dataelementid,\",\",p.startdate) " +
                                    " IN ( "+ 
                                            " SELECT CONCAT( sourceid,\",\",dataelementid,\",\",MAX(period.startdate) ) FROM datavalue " +
                                                " INNER JOIN period ON datavalue.periodid = period.periodid " + 
                                                    " WHERE sourceid IN (" + orgUnitIdsByComma +") AND dataelementid IN ("+ dataElementIdsByComma +") " + 
                                                    " GROUP BY sourceid,dataelementid" +
                                          " ) ORDER BY sourceid, dataelementid";
                                                
            */
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                Integer ouId = rs.getInt( 1 );
                Integer deId = rs.getInt( 2 );
                String value = rs.getString( 4 );
				if(deId==730)
				{  value = rs.getString( 4 );
					int foo = Integer.parseInt(value);
					 value = NumberFormat.getNumberInstance(Locale.US).format(foo);
				}
			
                Integer pId = rs.getInt( 3 );
              // String value = rs.getString( 4 );
                String comment = rs.getString( 5 );
                String storedBy = rs.getString( 6 );
                Date lastUpdated = rs.getDate( 7 );
                
                //DataElement de = dataElementService.getDataElement( deId );
                //OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                //Period p = periodService.getPeriod( pId );
                
                DataValue dv = new DataValue();
                //dv.setDataElement( de );
                //dv.setSource( ou );
                //dv.setPeriod( p );
                dv.setValue( value );
                dv.setComment( comment );
                dv.setStoredBy( storedBy );
                dv.setLastUpdated( lastUpdated );
                
                latestDataValues.put( ouId+":"+deId, dv );
                
               
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getLatestDataValues", e );
        }
        
        return latestDataValues;
    }
    
    
    public List<DataValueAudit> getActiveDataValueAuditByOrgUnit_DataElement_Period_type( DataElement dataElement, OrganisationUnit organisationUnit, Period period ,String commentType )
    {
        List<DataValueAudit> dataValueAudits = new ArrayList<DataValueAudit>();
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT datavalueauditid, VALUE, modifiedby, dva.timestamp, COMMENT, STATUS FROM datavalueaudit dva " +
                " WHERE " +
                    " periodid = "+ period.getId() +" AND " +
                    " dataelementid = "+ dataElement.getId() +" AND " +
                    " organisationunitid = "+ organisationUnit.getId() +" AND " +
                    " commenttype = '"+ commentType +"' AND " +
                    " status = 1 AND " +
                    " ( value IS NOT NULL OR comment IS NOT NULL ) " +
                    " ORDER BY dva.timestamp DESC";
            
            //System.out.println( query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer dvaId = rs.getInt( 1 );
                String value = rs.getString( 2 );
                String storedBy = rs.getString( 3 );
                String lastUpdated = rs.getString( 4 );
                String comment = rs.getString( 5 );
                Integer status = rs.getInt( 6 );
                
                DataValueAudit dva = new DataValueAudit();
                
                DataValue dataValue = new DataValue();
                dataValue.setDataElement( dataElement );
                dataValue.setSource( organisationUnit );
                dataValue.setPeriod( period );
                dataValue.setCategoryOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
                
                dva.setId( dvaId );
                
                dva.setDataElement( dataElement );
                dva.setOrganisationUnit( organisationUnit );
                dva.setPeriod( period );
                dva.setCategoryOptionCombo( dataElementCategoryService.getDefaultDataElementCategoryOptionCombo() );
				
				//dva.setDataValue( dataValue );
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                dataValueAudits.add( dva );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAudits;
    }
    

    public List<Period> getDistinctPeriodsFromHisotry( String dataElementIdsByComma, String orgUnitIdsByComma, String commentType )
    {
        List<Period> periods = new ArrayList<Period>();
        DatabaseInfo dataBaseInfo = databaseInfoProvider.getDatabaseInfo();
        System.out.println("dataBaseInfo--"+dataBaseInfo);

        try
        {
            String query = "";
            if ( dataBaseInfo.getType().equalsIgnoreCase( "mysql" ) )
            {
             query = "SELECT distinct(dva.periodid) FROM datavalueaudit dva INNER JOIN period p " +
                                " ON dva.periodid = p.periodid " +
                                " WHERE " +                                
                                    " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                                    " organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                                    " commenttype = '"+ commentType +"' ORDER BY p.startdate";
            }
            
            else if ( dataBaseInfo.getType().equalsIgnoreCase( "postgresql" ) )
            {
                 query = "SELECT distinct(dva.periodid) , p.startdate FROM datavalueaudit dva INNER JOIN period p " +
                    " ON dva.periodid = p.periodid " +
                    " WHERE " +                                
                        " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                        " organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                        " commenttype = '"+ commentType +"' ORDER BY p.startdate";
            }
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            //System.out.println( query );
            
            while ( rs.next() )
            {
                Integer periodId = rs.getInt( 1 );
                Period p = periodService.getPeriod( periodId );
                periods.add( p );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return periods;
         
    }
    
    public Map<String, List<DataValueAudit>> getDataValueAuditMapByUser_UserActivity( String dataElementIdsByComma, String orgUnitIdsByComma, String startDate, String endDate, String commentType ,User user)
    {
        Map<String, List<DataValueAudit>> dataValueAuditMap = new HashMap<String, List<DataValueAudit>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

        
        try
        {
           
              String  query = "SELECT dataelementid, periodid, organisationunitid, VALUE, modifiedby, dva.timestamp, COMMENT, STATUS FROM datavalueaudit dva " +
                    " WHERE " +                                
                        " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                        " organisationunitid IN ("+ orgUnitIdsByComma +") AND ";
                        
                        if( user != null )
                        {
                            query += " modifiedby = '"+ user.getUsername() +"' AND ";
                        }
                        
                        if( startDate != null && endDate != null )
                        {
                            query += " DATE(dva.timestamp) BETWEEN '"+ startDate +"' AND '"+ endDate +"' AND ";
                        }
                        //query += " dva.timestamp >= '" + startDate + "' AND " +
                        //" dva.timestamp <= '" + endDate + "' AND " +
                        query += " commenttype = '"+ commentType +"' ORDER BY periodid, dva.timestamp DESC";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            System.out.println( "query----"+query );
            
            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                //DataElement de = dataElementService.getDataElement( deId );
                
                Integer periodId = rs.getInt( 2 );
                
                Integer ouId = rs.getInt( 3 );
                //OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                
                String value = rs.getString( 4 );
                String storedBy = rs.getString( 5 );
                String lastUpdated = rs.getString( 6 );
                System.out.println("lastUpdated---"+lastUpdated);
                
                String comment = rs.getString( 7 );
                Integer status = rs.getInt( 8 );
                
                DataValueAudit dva = new DataValueAudit();
                				
		dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                List<DataValueAudit> dataValueAuditList = dataValueAuditMap.get( ouId+":"+deId+":"+periodId );
                if( dataValueAuditList == null )
                {
                    dataValueAuditList = new ArrayList<DataValueAudit>();
                }

                dataValueAuditList.add( dva );
                dataValueAuditMap.put( ouId+":"+deId+":"+periodId, dataValueAuditList );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }
    
    
    public Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> getDataValueAuditMapByUser( String dataElementIdsByComma, String orgUnitIdsByComma, Integer periodId, String commentType ,User user)
    {
        Map<OrganisationUnit, Map<DataElement, List<DataValueAudit>>> dataValueAuditMap = new HashMap<OrganisationUnit, Map<DataElement, List<DataValueAudit>>>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        try
        {
            String query = "SELECT dataelementid, organisationunitid, VALUE, modifiedby, dva.timestamp, COMMENT, STATUS FROM datavalueaudit dva " +
                " WHERE " +
                    " periodid = "+ periodId +" AND " +
                    " dataelementid IN ("+ dataElementIdsByComma +") AND " +
                    " organisationunitid IN ("+ orgUnitIdsByComma +") AND " +
                    " modifiedby = '"+ user.getUsername() +"' AND " +
                    " commenttype = '"+ commentType +"' ORDER BY periodid, dva.timestamp DESC";
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                
                Integer ouId = rs.getInt( 2 );
                OrganisationUnit ou = organisationUnitService.getOrganisationUnit( ouId );
                
                String value = rs.getString( 3 );
                String storedBy = rs.getString( 4 );
                String lastUpdated = rs.getString( 5 );
                String comment = rs.getString( 6 );
                Integer status = rs.getInt( 7 );
                
                DataValueAudit dva = new DataValueAudit();
                dva.setValue( value );
                dva.setComment( comment );
                dva.setStatus( status );
                dva.setModifiedBy( storedBy );
                Date timeStamp = simpleDateFormat.parse( lastUpdated );
                dva.setTimestamp( timeStamp );
                
                Map<DataElement, List<DataValueAudit>> dataElementAuditMap = dataValueAuditMap.get( ou );
                if( dataElementAuditMap == null )
                {
                    dataElementAuditMap = new HashMap<DataElement, List<DataValueAudit>>();
                    List<DataValueAudit> dataValueAuditList = new ArrayList<DataValueAudit>();
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }
                else
                {
                    List<DataValueAudit> dataValueAuditList = dataElementAuditMap.get( de );
                    if( dataValueAuditList == null )
                    {
                        dataValueAuditList = new ArrayList<DataValueAudit>();
                    }
                    dataValueAuditList.add( dva );
                    dataElementAuditMap.put( de, dataValueAuditList );
                    dataValueAuditMap.put( ou, dataElementAuditMap );
                }                
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getDataValueAuditMap", e );
        }
        
        return dataValueAuditMap;
    }
    
    public Map<String, List<Section>> getSectionsByOrganisationUnit(Integer orgUnitId)
    {
      Map<String, List<Section>> dataSetSectionMap = new HashMap<String, List<Section>>();
      try
      {
        String query = "SELECT ss.sectionid FROM sectionsource ss  WHERE ss.sourceid = " + orgUnitId + " ORDER BY ss.sectionid";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

        while (rs.next())
        {
          Integer sectionId = Integer.valueOf(rs.getInt(1));
          Section section = sectionService.getSection(sectionId.intValue());
          if (dataSetSectionMap.containsKey(section.getDataSet().getUid()))
          {
            List<Section> sectionList = dataSetSectionMap.get(section.getDataSet().getUid());
            sectionList.add(section);
          }
          else
          {
            List<Section> sectionList = new ArrayList<Section>();
            sectionList.add(section);
            dataSetSectionMap.put(section.getDataSet().getUid(), sectionList);
          }

        }

      }
      catch (Exception e)
      {
        throw new RuntimeException("Exception in getLatestDataValues", e);
      }
      return dataSetSectionMap;
    }
    public void saveFavoriteDataValueType(int favoriteId,List<DataElement> dataElementList,Map<Integer,String> typeMap)
    {
        String selectQuery = "SELECT *FROM favorite_datavaluetypes WHERE favoriteid = " + favoriteId ;
        
        SqlRowSet sqlResultSet1 = jdbcTemplate.queryForRowSet( selectQuery );
       
        if ( sqlResultSet1 != null && sqlResultSet1.next() )
        {
            for(DataElement de : dataElementList)
            {
                String updateQuery = "UPDATE favorite_datavaluetypes SET dataValueType = '" + typeMap.get( de.getId() ) + "'"
                + " WHERE favoriteid = " + favoriteId + " AND idx = "+de.getId();
            
                jdbcTemplate.update( updateQuery );
            }
        }
        else
        {
            String query = "INSERT INTO favorite_datavaluetypes ( favoriteid, dataValueType, idx ) VALUES ";
            int count = 1;
            for(DataElement de : dataElementList)
            {
                query = query + "( "+favoriteId+", '"+typeMap.get( de.getId() )+"', "+de.getId()+" )";
                if(dataElementList.size() > count)
                {
                    query+= ",";
                }
                count++;
            }
            jdbcTemplate.update(query);
        }        
        
    }
    public Map<Integer,String> getFavoriteDataValueType(Integer favoriteId )
    {
        Map<Integer,String> favoriteDataValueTypeMap = new HashMap<Integer, String>();
        try
        {
          String query = "SELECT idx, dataValueType FROM favorite_datavaluetypes WHERE  favoriteid = " + favoriteId;

          SqlRowSet rs = jdbcTemplate.queryForRowSet(query);

          while (rs.next())
          {
            Integer dataElementId = rs.getInt(1);
            String valueType = rs.getString( 2 );
            favoriteDataValueTypeMap.put( dataElementId, valueType );            
          }

        }
        catch (Exception e)
        {
          throw new RuntimeException("Exception in getLatestDataValues", e);
        }
        return favoriteDataValueTypeMap;
    }
   
    public static String formatDate(String dataValue)
    {
       String value = dataValue.trim();
       if(value.matches( "\\d{4}-Q1" ))
       {
           value.replaceAll( "Q1", "01-01" );
       }
       else if(value.matches( "\\d{4}-Q2" ))
       {
           value.replaceAll( "Q2", "04-01" ); 
       }
       else if(value.matches( "\\d{4}-Q3" ))
       {
           value.replaceAll( "Q3", "07-01" );
       }
       else if(value.matches( "\\d{4}-Q4" ))
       {           
           value.replaceAll( "Q4", "10-01" );
       }
       else if(value.matches("([0-9]{4})"))
       {
           value = value + "-01-01";
       }
       else if(value.matches("\\d{4}-\\d{2}"))
       {
           value = value + "-01";
       }
       else
       {}
      return value;  
    }
   

    // to get the list of data element which were hidden for a given
    // organisation unit

    public List<DataElement> getHiddenDataElementList( String orgUnitUid )
    {
    	OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitUid );
    	
        List<DataElement> hiddenDeList = new ArrayList<DataElement>();
        Set<DataElement> allDes = new HashSet<DataElement>( dataElementService.getAllDataElements() );
        for( DataElement de : allDes )
        {
        	if( de.getOrgUnits() != null && de.getOrgUnits().contains( orgUnit ) )
        	{
        		hiddenDeList.add( de );
        	}
        	else if( de.getPublicAccess() != null && de.getPublicAccess().equals( "--------" ) )
            {
        		hiddenDeList.add( de );
            }
        }

        /*
        Integer orgUnitId = organisationUnitService.getOrganisationUnit( orgUnitUid ).getId();
        try
        {
            String query = "SELECT dataelementid FROM hiddende_orgunit WHERE orgunitid = " + orgUnitId;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                try
                {
                	Integer deId = rs.getInt( 1 );
                	DataElement de = dataElementService.getDataElement( deId );
                	hiddenDeList.add( de );
                }
                catch( Exception e )
                {
                	
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getHiddenDEListForGivenOrgnuit", e );
        }
        */

        return hiddenDeList;
    }

    public Map<Integer, Set<Integer>> getHiddenDataElementByCountry( )
    {
    	Map<Integer, Set<Integer>> hiddenDes = new HashMap<Integer, Set<Integer>>();

        try
        {
            String query = "SELECT orgunitid, dataelementid FROM hiddende_orgunit";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
            	Integer ouId = rs.getInt( 1 );
                Integer deId = rs.getInt( 2 );
                
                Set<Integer> des = hiddenDes.get( ouId );
                if( des == null )
                {
                	des = new HashSet<Integer>();
                }
                des.add( deId );
                hiddenDes.put( ouId, des );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getHiddenDEListForGivenOrgnuit", e );
        }

        return hiddenDes;
    }

    
    public Set<DataElement> getRestrictedDataElements( Integer restrictedDeAttributeId )
    {
    	Set<DataElement> restrictedDes = new HashSet<DataElement>();

        try
        {
            String query = "SELECT dataelementid FROM dataelementattributevalues deav " + 
            					" INNER JOIN attributevalue av ON deav.attributevalueid = av.attributevalueid " + 
            					" WHERE av.attributeid = "+ restrictedDeAttributeId + " AND " +
            					" value = 'true'";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer deId = rs.getInt( 1 );
                DataElement de = dataElementService.getDataElement( deId );
                restrictedDes.add( de );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getHiddenDEListForGivenOrgnuit", e );
        }

        return restrictedDes;
    }

    
    public String findAlertValueFromExpression( String deAlertExpression, DataElement de, OrganisationUnit orgUnit )
    {
        String expressionParts[] = deAlertExpression.split( "AND" );
        List<Integer> subExpResults = new ArrayList<Integer>();
        Boolean expResult = true;
        String expValue = "";
        
        for( int i = 0; i < expressionParts.length; i++ )
        {
            String subExp = expressionParts[i];
                        
            if( subExp.contains( "<=" ) )
            {
                String leftSideExp = subExp.split( "<=" )[0];                
                String rightSideExp = subExp.split( "<=" )[1];
                ExpressionValue leftSideExpValObj = findSubExpressionValue( leftSideExp, orgUnit );
                ExpressionValue rightSideExpValObj = findSubExpressionValue( rightSideExp, orgUnit );

                String leftSideExpVal = leftSideExpValObj.getExpValue();
                String rightSideExpVal = rightSideExpValObj.getExpValue();

                if( leftSideExpVal.compareTo( rightSideExpVal ) <= 0 )
                {
                    subExpResults.add( 1 );
                    
                    if( leftSideExpValObj.getDe() != null )
                    {
                        expValue += leftSideExpValObj.getDe().getName() + " : " + leftSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += leftSideExpValObj.getExpValue();
                    }
                    expValue += " <= ";
                    if( rightSideExpValObj.getDe() != null )
                    {
                        expValue += rightSideExpValObj.getDe().getName() + " : " + rightSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += rightSideExpValObj.getExpValue();
                    }
                    expValue += " <br/>AND<br/> ";
                }
                else
                {
                    subExpResults.add( 0 );
                    expResult = false;
                }
                //System.out.println("Inside <= " + leftSideExpValObj.getExpValue() + " : " + rightSideExpValObj.getExpValue() + " : " + expValue );
                
            }
            else if( subExp.contains( ">=" ) )
            {
                String leftSideExp = subExp.split( ">=" )[0];                
                String rightSideExp = subExp.split( ">=" )[1];
                ExpressionValue leftSideExpValObj = findSubExpressionValue( leftSideExp, orgUnit );
                ExpressionValue rightSideExpValObj = findSubExpressionValue( rightSideExp, orgUnit );

                String leftSideExpVal = leftSideExpValObj.getExpValue();
                String rightSideExpVal = rightSideExpValObj.getExpValue();

                if( leftSideExpVal.compareTo( rightSideExpVal ) >= 0 )
                {
                    subExpResults.add( 1 );
                    if( leftSideExpValObj.getDe() != null )
                    {
                        expValue += leftSideExpValObj.getDe().getName() + " : " + leftSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += leftSideExpValObj.getExpValue();
                    }
                    expValue += " >= ";
                    if( rightSideExpValObj.getDe() != null )
                    {
                        expValue += rightSideExpValObj.getDe().getName() + " : " + rightSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += rightSideExpValObj.getExpValue();
                    }
                    expValue += " <br/>AND<br/> ";
                }
                else
                {
                    subExpResults.add( 0 );
                    expResult = false;
                }
                //System.out.println("Inside >= " + leftSideExpValObj.getExpValue() + " : " + rightSideExpValObj.getExpValue() + " : " + expValue );

            }
            else if( subExp.contains( "==" ) )
            {
                String leftSideExp = subExp.split( "==" )[0];                
                String rightSideExp = subExp.split( "==" )[1];

                ExpressionValue leftSideExpValObj = findSubExpressionValue( leftSideExp, orgUnit );
                ExpressionValue rightSideExpValObj = findSubExpressionValue( rightSideExp, orgUnit );

                String leftSideExpVal = leftSideExpValObj.getExpValue();
                String rightSideExpVal = rightSideExpValObj.getExpValue();
                
                if( leftSideExpVal.trim().equals( rightSideExpVal.trim() ) )
                {
                    subExpResults.add( 1 );
                    if( leftSideExpValObj.getDe() != null )
                    {
                        expValue += leftSideExpValObj.getDe().getName() + " : " + leftSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += leftSideExpValObj.getExpValue();
                    }
                    expValue += " == ";
                    if( rightSideExpValObj.getDe() != null )
                    {
                        expValue += rightSideExpValObj.getDe().getName() + " : " + rightSideExpValObj.getExpValue();
                                            
                    }
                    else
                    {
                        expValue += rightSideExpValObj.getExpValue();
                    }
                    expValue += " <br/>AND<br/> ";                    
                }
                else
                {
                    subExpResults.add( 0 );
                    expResult = false;
                }
                //System.out.println("Inside == " + leftSideExpValObj.getExpValue() + " : " + rightSideExpValObj.getExpValue() + " : " + expValue );
            }
        }
        
        if( expResult == false )
        {
            expValue = "";
        }
       
        if( expValue.lastIndexOf( "AND" ) != -1 )
            expValue = expValue.substring( 0, expValue.lastIndexOf( "AND" ) );
        
        return expValue;
    }
    
    public ExpressionValue findSubExpressionValue( String exp, OrganisationUnit orgUnit )
    {
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        ExpressionValue expVal = new ExpressionValue();
        
        String expValue = "";

        if( exp.contains( "#DATEVALUE#" ) )
        {
            exp = exp.replace( "#DATEVALUE#", "" );
            exp = exp.trim();
            String deUID = exp.split( "\\." )[0];
            String cocUID = exp.split( "\\." )[1];
            
            DataElement dataElement = dataElementService.getDataElement( deUID );
            DataElementCategoryOptionCombo deCoc = dataElementCategoryService.getDataElementCategoryOptionCombo( cocUID );
            
            DataValue dv = dataValueService.getLatestDataValue( dataElement, deCoc, orgUnit );
            
            if( dv != null )
            {
                if ( dv.getValue() == null || dv.getValue().isEmpty() || dv.getValue().trim().equals("") )
                {
                }
                else if( dataElement.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                {                    
                }
                else
                {
                    expValue = formatDateByEndDate( dv.getValue() );                    
                }
            }
            expVal.setDe( dataElement );
            expVal.setOu( orgUnit );
            expVal.setExpValue( expValue );
            //System.out.println( "#DATEVALUE#" + dataElement.getName() + " : " + orgUnit.getName() + " : " + expValue );
        }
        else if( exp.trim().equals( "NOW" ) )
        {
            Date curDate = new Date();
            expValue = standardDateFormat.format( curDate );
            expVal.setDe( null );
            expVal.setOu( orgUnit );
            expVal.setExpValue( expValue );
            //System.out.println( "NOW" + " : " + orgUnit.getName() + " : " + expValue );
        }
        else if( exp.contains( "#VALUE#" ) )
        {
            exp = exp.replace( "#VALUE#", "" );
            exp = exp.trim();
            String deUID = exp.split( "\\." )[0];
            String cocUID = exp.split( "\\." )[1];
            
            DataElement dataElement = dataElementService.getDataElement( deUID );
            DataElementCategoryOptionCombo deCoc = dataElementCategoryService.getDataElementCategoryOptionCombo( cocUID );
            
            DataValue dv = dataValueService.getLatestDataValue( dataElement, deCoc, orgUnit );
            
            if( dv != null )
            {
                if ( dv.getValue() == null || dv.getValue().isEmpty() || dv.getValue().trim().equals("") )
                {
                }
                else if( dataElement.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                {
                }
                else
                {
                    expValue = dv.getValue();                    
                }
            }
            expVal.setDe( dataElement );
            expVal.setOu( orgUnit );
            expVal.setExpValue( expValue );
            //System.out.println( "#VALUE# " + dataElement.getName() + " : " + orgUnit.getName() + " : " + expValue );
        }
        else
        {
            expValue = exp;
            expVal.setDe( null );
            expVal.setOu( orgUnit );
            expVal.setExpValue( expValue );
            //System.out.println( "ELSE" + " : " + orgUnit.getName() + " : " + expValue );
        }
        
        return expVal;
    }
    
    public static String formatDateByEndDate( String dateValue )
    {
       String value = dateValue.trim();
       int monthDays[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
       
       if(value.matches( "\\d{4}-Q1" ))
       {
           value = value.replaceAll( "Q1", "03-31" );
       }
       else if(value.matches( "\\d{4}-Q2" ))
       {
           value = value.replaceAll( "Q2", "06-30" ); 
       }
       else if(value.matches( "\\d{4}-Q3" ))
       {
           value = value.replaceAll( "Q3", "09-30" );
       }
       else if(value.matches( "\\d{4}-Q4" ))
       {           
           value = value.replaceAll( "Q4", "12-31" );
       }
       else if(value.matches("([0-9]{4})"))
       {
           value = value + "-12-31";
       }
       else if(value.matches("\\d{4}-\\d{2}"))
       {
           int year = Integer.parseInt( value.split( "-" )[0] );
           int month = Integer.parseInt( value.split( "-" )[1] );
           
           if( year%4 == 0 && month == 2 )
           {
               value = value + "-" + (monthDays[ month-1 ]+1);
           }
           else
           {
               value = value + "-" + monthDays[ month-1 ];
           }
       }
      
       return value;      
    }
    
    
    public Collection<OrganisationUnit> getLeafOrganisationUnits( int id )
    {
    	Collection<OrganisationUnit> units = organisationUnitService.getOrganisationUnitWithChildren( id );

    	return FilterUtils.filter( units, new Filter<OrganisationUnit>()
    	{
    		public boolean retain( OrganisationUnit object )
    		{
    			return object != null && object.getChildren().isEmpty();
    		}
    	} );
    }
    
    
    public List<UserDetails> getUserDetails( )
    {
    	List<UserDetails> userDetails = new ArrayList<UserDetails>();
    	SimpleDateFormat sd = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        try
        {
            String query = "SELECT t1.username, t2.surname, t2.firstName, t2.email, t1.lastLogin FROM users t1 INNER JOIN userinfo t2 ON t1.userid = t2.userinfoid ORDER BY t1.lastLogin";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
            	UserDetails userDetail = new UserDetails();
            	
            	userDetail.setUserName( rs.getString(1) );
            	userDetail.setSurName( rs.getString(2) );
            	userDetail.setFirstName( rs.getString(3) );
            	userDetail.setEmail( rs.getString(4) );
            	
            	try
            	{
            		Date d = rs.getDate(5);
            		userDetail.setLastLogin( sd.format(d) );
            	}
            	catch( Exception e )
            	{
            		//System.out.println( "inside catch" );
            		userDetail.setLastLogin( rs.getString(5) );
            	}
            		
            	userDetails.add( userDetail );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Exception in getHiddenDEListForGivenOrgnuit", e );
        }

        return userDetails;
    }
}