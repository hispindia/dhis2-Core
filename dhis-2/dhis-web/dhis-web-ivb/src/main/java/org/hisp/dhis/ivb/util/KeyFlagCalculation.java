package org.hisp.dhis.ivb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author Samta bajpai
 */
@Transactional
@SessionAttributes
public class KeyFlagCalculation
{
    public static final String KEYFLAG_RED = "RED";
    public static final String KEYFLAG_GREEN = "GREEN";
    public static final String KEYFLAG_GREY = "GREY";
    
    public final static String KEY_FLAG = "Key Flag";
    public final static String KEY_THRESHOLD = "Flag Threshold";
    public final static String IS_BOOLEAN = "Is Boolean?";
    public final static String IS_THRESHOLDREV = "Reverse Threshold";
    public final static String USER_SOURCE = "Source";
    
    public static final String OPERATOR_AND = "@AND@";

    public static final String OPERATOR_OR = "@OR@";

    public static final String NESTED_OPERATOR_AND = "@NESTED_AND@";

    public static final String NESTED_OPERATOR_OR = "@NESTED_OR@";

    public static final String DE_VALUE = "#VALUE#";

    private final String OPERAND_EXPRESSION = "(#\\{(\\w+)\\.?(\\w*)\\}" + "|DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)"
        + "|YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|CURYEAR" + "|MAX\\("
        + "(#\\{(\\w+)\\.?(\\w*)\\},)*" + "#\\{(\\w+)\\.?(\\w*)\\}\\)" + "|MAX\\("
        + "(DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "DATEOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + "|MAX\\("
        + "(YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "YEAROF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + "|MAX\\("
        + "(MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\),)*" + "MONTHOF\\(#\\{(\\w+)\\.?(\\w*)\\}\\)\\)" + ")";

    private final String ARITHMETIC_OPERATOR = "(\\+" + "|" + "\\-" + "|" + "\\*" + "|" + "\\/" + ")";

    private final String numberExp = "\\d+";

    private String betweenExp = "(CURYEAR" + "|" + "NOW\\(\\)" + "|" + "NEXTYEAR" + "|" + "NEXT_" + numberExp + "_MONTHS" + "|" + "LAST_" + numberExp + "_MONTHS )";

    private final String CONSTANT_EXPRESSION = "(\\'YES\\'|\\'NO\\'|(\\d+\\.\\d+)|(\\d+)+|\\'[a-zA-Z0-9]+\\s[a-zA-Z0-9]*\\'"
        + "|NOW\\(\\)" + "|BETWEEN\\(" + betweenExp + "," + betweenExp + "\\)" + ")";

    private final Pattern OPERAND_PATTERN = Pattern.compile( OPERAND_EXPRESSION );

    private final Pattern CONSTANT_PATTERN = Pattern.compile( CONSTANT_EXPRESSION );

    private final String OPERATOR_EXPRESSION = "(<=|>=|=|!=|<|>)";

    private final String regExp = "^IF\\((" + OPERAND_PATTERN + "|" + OPERAND_PATTERN + ARITHMETIC_OPERATOR
        + OPERAND_PATTERN + "|" + "\\(" + OPERAND_PATTERN + ARITHMETIC_OPERATOR + OPERAND_PATTERN + "\\)"
        + ARITHMETIC_OPERATOR + "\\(" + OPERAND_PATTERN + ARITHMETIC_OPERATOR + OPERAND_PATTERN + "\\)" + ")"
        + OPERATOR_EXPRESSION + CONSTANT_PATTERN + "\\)$";

    private boolean isDataDateType = false;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }
    
    @Autowired
    private IndicatorService indicatorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private IVBUtil ivbUtil;
    
    @Autowired
    private DataElementService dataElementService;
    
    // -------------------------------------------------------------------------
    // Getter and Setter
    // -------------------------------------------------------------------------
    
    public Map<String, String> valueMap = new HashMap<String, String>();

    public Map<String, String> getValueMap()
    {
        return valueMap;
    }

    public void setValueMap( Map<String, String> valueMap )
    {
        this.valueMap = valueMap;
    }
    public Map<String, String> colorMap = new HashMap<String, String>();
    
    public Map<String, String> getColorMap()
    {
        return colorMap;
    }

    public void setColorMap( Map<String, String> colorMap )
    {
        this.colorMap = colorMap;
    }
    private Map<String, String> commentMap = new HashMap<String, String>();

    public Map<String, String> getCommentMap()
    {
        return commentMap;
    }
    public void setCommentMap( Map<String, String> commentMap )
    {
        this.commentMap = commentMap;
    }

    private Map<String, String> sourceMap = new HashMap<String, String>();

    public Map<String, String> getSourceMap()
    {
        return sourceMap;
    }
    public void setSourceMap( Map<String, String> sourceMap )
    {
        this.sourceMap = sourceMap;
    }
    
    private Map<String, String> userInfoMap = new HashMap<String, String>();

    public Map<String, String> getUserInfoMap()
    {
        return userInfoMap;
    }
    public void setUserInfoMap( Map<String, String> userInfoMap )
    {
        this.userInfoMap = userInfoMap;
    }
    private Map<String, List<DataSet>> datasetMap = new HashMap<String, List<DataSet>>();

    public Map<String, List<DataSet>> getDatasetMap()
    {
        return datasetMap;
    }
    public void setDatasetMap( Map<String, List<DataSet>> datasetMap )
    {
        this.datasetMap = datasetMap;
    }
    private List<Indicator> indicatorList = new ArrayList<Indicator>();

    public List<Indicator> getIndicatorList()
    {
        return indicatorList;
    }
    public void setIndicatorList( List<Indicator> indicatorList )
    {
        this.indicatorList = indicatorList;
    }
    private String lastUpdated;

    public String getLastUpdated()
    {
        return lastUpdated;
    }
    public void setLastUpdated( String lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }
    private Map<String, String> periodMap = new HashMap<String, String>();

    public Map<String, String> getPeriodMap()
    {
        return periodMap;
    }
    public void setPeriodMap( Map<String, String> periodMap )
    {
        this.periodMap = periodMap;
    }
    private Set<OrganisationUnit> keyFlagCountries = new HashSet<OrganisationUnit>();

    public Set<OrganisationUnit> getKeyFlagCountries()
    {
        return keyFlagCountries;
    }
    public void setKeyFlagCountries( Set<OrganisationUnit> keyFlagCountries )
    {
        this.keyFlagCountries = keyFlagCountries;
    }
	
    private Boolean isthresoldrev = false;
	
    public Boolean getIsthresoldrev()
    {
        return isthresoldrev;
    }

    public void setIsthresoldrev( Boolean isthresoldrev )
    {
        this.isthresoldrev = isthresoldrev;
    }

    // -------------------------------------------------------------------------
    // Method Implementation
    // -------------------------------------------------------------------------

    public void getFlagColor(String indicatorUid, OrganisationUnit orgUnit) 
    {
        String getter = valueMapGetter( indicatorUid, orgUnit );
        Indicator indicator = indicatorService.getIndicator( indicatorUid );
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        String userInfo = "";
        String source = null;
        String comment = null;
        String thresoldValue = "";
        Boolean isBoolean = false;        
        Date tempDate = null;
        String period = null;
        isthresoldrev = false;
        
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        
        try
        {
	        List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>( indicator.getAttributeValues() );
	        for ( AttributeValue attributeValue : attributeValueList )
	        {
	            if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_FLAG ) && attributeValue.getValue().equals( "true" ) )
	            { 
	                if( !indicatorList.contains( indicator ) )
	                {
	                    indicatorList.add( indicator );
	                    datasetMap.put( indicator.getUid(),new ArrayList<DataSet>(indicator.getDataSets()));
	                }
	                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService.getDataElementsInExpression( indicator.getNumerator() ) );
	                for ( DataElement de : deNuList )
	                {                    
	                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
	                    if ( dv != null )
	                    {
	                        String constantValue = ""; 
	                        
	                        //System.out.println( dv.getStoredBy() + " --- " +  userService.getUserCredentialsByUsername( dv.getStoredBy() ) );
	                        
	                        UserCredentials userCredentials = userService.getUserCredentialsByUsername( dv.getStoredBy() );
	                        if( userCredentials != null )
	                        {
	                            User user = userCredentials.getUser();
	                            if( user != null)
	                            {
	                                {                                   
	                                    Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( user.getAttributeValues() );
	                                    for ( AttributeValue attValue : attrValueSet )
	                                    {
	                                        if ( attValue.getAttribute().getName().equalsIgnoreCase( USER_SOURCE ) )
	                                        {
	                                            constantValue = attValue.getValue();
	                                        }
	                                    }
	                                    
	                                    userInfo = "User: "+dv.getStoredBy()+"<br/>Full Name: " + user.getName() + "<br/>Organisation: "+constantValue;
	                                }
	                            }
	                            else
	                            {
	                                userInfo = "User: "+dv.getStoredBy();
	                            }
	                        }
	                        else
	                        {
	                            userInfo = "User: "+dv.getStoredBy();
	                        }
	                        
	                        if ( tempDate == null || tempDate.before( dv.getLastUpdated() ) )
	                        {
	                            tempDate = dv.getLastUpdated();
	                        	source = dv.getStoredBy() + "<br/>(";
		                        
		                        try
		                        {
		                            source +=  standardDateFormat.format( dv.getLastUpdated() ) + ")";
		                        }
		                        catch( Exception e )
		                        {
		                            source +=  dv.getLastUpdated() + ")";
		                        }
	                        }
	                        
	                        if ( comment == null && dv.getComment()!= null && dv.getComment()!= "")
	                        {
	                            comment = new String( dv.getComment().getBytes("UTF-8"), "UTF-8" );
	                            comment = new String( comment.getBytes("UTF-8"), "UTF-8" );
	                        }
	                        else
	                        {
	                            if ( dv.getComment()!= "" && dv.getComment()!= null )
	                            {
	                                comment = comment + "; " + new String( dv.getComment().getBytes("UTF-8"), "UTF-8" );
	                                comment = new String( comment.getBytes("UTF-8"), "UTF-8" );
	                            }                                
	                        }
	                        
	                        Period periodById = dv.getPeriod();
	                        if ( periodById.getPeriodType().getName().equalsIgnoreCase( "Quarterly" ) )
	                        {
	                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat( "MMM" );
	                            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );                                
	                            period = simpleDateFormat1.format( dv.getPeriod().getStartDate() ) +" - "+simpleDateFormat2.format( dv.getPeriod().getEndDate() );                                
	                        }
	                        else
	                        {
	                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd" );
	                            period = simpleDateFormat1.format( dv.getPeriod().getStartDate());                                
	                        }
	                    }
	                }
	                SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	                if ( tempDate != null )
	                {
	                    lastUpdated = simpleDateFormat.format( tempDate );
	                }
	            }
	            else if ( attributeValue.getAttribute().getName().equalsIgnoreCase( KEY_THRESHOLD ) )
	            {
	                thresoldValue = attributeValue.getValue();
	            }
	            else if ( attributeValue.getAttribute().getName().equalsIgnoreCase( IS_BOOLEAN ) && attributeValue.getValue().equals( "true" ) )
	            {
	                isBoolean = true;
	            }
	            else if ( attributeValue.getAttribute().getName().equalsIgnoreCase( IS_THRESHOLDREV ) && attributeValue.getValue().equals( "true" ) )
	            {
	                isthresoldrev = true;
	            }
	            
	            //System.out.println( indicator.getName() + "----" + attributeValue.getAttribute().getName() + " ---- " + attributeValue.getValue() );
	            
	            if ( thresoldValue.equalsIgnoreCase( valueMap.get( getter ) ) )
	            {
	                keyFlagCountries.add( orgUnit );
	            }
	        }
	        
	        //System.out.println( indicator.getName() + " :: " + comment );
	        
	        commentMap.put( getter, comment );
	        sourceMap.put( getter, source );
	        userInfoMap.put( getter, userInfo );
	        periodMap.put( getter , period );
	        
	        //keyFlagCountries.add( orgUnit );
	        
	        String keyFlagValue = valueMap.get( getter );
	        
	        //System.out.println( indicator.getName() + " ::: " + keyFlagValue + " ::: " + thresoldValue + " ::: " + orgUnit.getName() );
	        
	        if( source == null || ( isBoolean && !keyFlagValue.equals( "Yes" ) && !keyFlagValue.equals( "No" ) ) )
	        {
	            keyFlagValue = "";
	            //System.out.println( indicator.getName() + " : GREY because " + source + " : " + isBoolean );
	        }
	        
	        if( keyFlagValue != null && !keyFlagValue.trim().equals( "" ) && keyFlagValue.trim().equals( thresoldValue ) )
	        {
	        	//System.out.println( indicator.getName() + orgUnit.getName() + "  color is RED ");
	        	colorMap.put( getter , KEYFLAG_RED );
	        }
	        else if( keyFlagValue != null && !keyFlagValue.equals( "" ) && !keyFlagValue.equals( thresoldValue ) )
	        {
	        	//System.out.println( indicator.getName() + orgUnit.getName() + "  color is GREEN ");
	        	colorMap.put( getter , KEYFLAG_GREEN );
	        }
	        else
	        {
	        	//System.out.println( indicator.getName() + orgUnit.getName() + "  color is GREY ");
	            colorMap.put( getter , KEYFLAG_GREY );
	        }
        }
        catch( Exception e )
        {
        	System.out.println("Exception at KeyFlagCalculation: " + e.getMessage() );
        }
    }
    
    public String valueMapGetter( String indicatorUid, OrganisationUnit orgUnit )
    {
        String getter;
        if ( orgUnit == null )
        {
            getter = indicatorUid;
        }
        else
        {
            getter = indicatorUid + "-" + orgUnit.getUid();
        }
        return getter;
    }

    public String getNestedKeyIndicatorValueWithThresoldValue( String expression, String indicatorUid,
        OrganisationUnit orgUnit )
    {
        boolean isValue = false;

        String getter = valueMapGetter( indicatorUid, orgUnit );

        String indicatorValue = "";
        Pattern valueOperator = Pattern.compile( DE_VALUE );
        Matcher matcherOperator = valueOperator.matcher( expression );
        if ( matcherOperator.find() )
        {
            isValue = true;
        }
        expression = expression.replaceAll( DE_VALUE, "" );
        String[] patternexpression = expression.trim().split( NESTED_OPERATOR_AND + "|" + NESTED_OPERATOR_OR );
        String[] valStrings = new String[patternexpression.length];
        Map<String, String> values = new HashMap<String, String>();
        for ( int i = 0; i < patternexpression.length; i++ )
        {
            boolean isAndCondion = false;
            boolean isOrCondion = false;

            String subExp = patternexpression[i].trim();
            valStrings[i] = getKeyIndicatorValueWithThresoldValue( subExp, indicatorUid, orgUnit );

            if ( expression.contains( NESTED_OPERATOR_AND + subExp ) )
            {
                isAndCondion = true;
            }
            if ( expression.contains( NESTED_OPERATOR_OR + subExp ) )
            {
                isOrCondion = true;
            }
            // System.out.println(subExp+"*******"+valStrings[i]);
            if ( isAndCondion )
            {
                if ( values.containsKey( getter ) )
                {
                    if ( valStrings[i].isEmpty() )
                    {
                        valueMap.put( getter, "" );
                    }
                    if ( values.get( getter ).equalsIgnoreCase( "Yes" ) && valStrings[i].equalsIgnoreCase( "Yes" ) )
                    {
                        valueMap.put( getter, "Yes" );
                    }
                    else
                    {
                        valueMap.put( getter, "No" );
                    }
                }
                else
                {
                    valueMap.put( getter, valStrings[i] );
                }                
            }
            else if ( isOrCondion )
            {
                if ( valStrings[i].isEmpty() && values.get( getter ).isEmpty() )
                {
                    valueMap.put( getter, "" );
                }
                else
                {
                    if ( values.containsKey( getter ) )
                    {
                        if ( values.get( getter ).equalsIgnoreCase( "Yes" ) || valStrings[i].equalsIgnoreCase( "Yes" ) )
                        {
                            valueMap.put( getter, "Yes" );
                        }
                        else
                        {
                            valueMap.put( getter, "No" );
                        }
                    }
                    else
                    {
                        valueMap.put( getter, valStrings[i] );
                    }
                }
            }
            else
            {
                valueMap.put( getter, valStrings[i] );
            }
            values.put( getter, valueMap.get( getter ) );
            getFlagColor(indicatorUid, orgUnit);
        }

        if ( isValue )
        {
            if ( indicatorValue.endsWith( ".0" ) )
            {
                indicatorValue = indicatorValue.replace( ".0", "" );
            }
            return indicatorValue;
        }
        else
        {
            return valueMap.get( getter );
        }

    }

    public String getKyeIndicatorValueForProgrammatic( String expression, String indicatorUid, OrganisationUnit orgUnit )
    {
        boolean isValue = false;

        String getter = valueMapGetter( indicatorUid, orgUnit );

        String indicatorValue = "";
        Pattern valueOperator = Pattern.compile( DE_VALUE );
        Matcher matcherOperator = valueOperator.matcher( expression );
        if ( matcherOperator.find() )
        {
            isValue = true;
        }        
        expression = expression.replaceAll( DE_VALUE, "" );
        expression = expression.replaceAll( "#VALUE_OF#", "" );
        
        String[] patternexpression = expression.trim().split( OPERATOR_AND + "|" + OPERATOR_OR );
        Pattern patternCondition = Pattern.compile( regExp );

        //System.out.println( indicatorUid + " : " + patternexpression );
        
        for ( int i = 0; i < patternexpression.length; i++ )
        {
            boolean isAndCondion = false;
            boolean isOrCondion = false;
            String value = "";
            String DEValue = "";
            String subExp = patternexpression[i].trim();
            
            //System.out.print("2."+i + " " + subExp );
            
            if ( expression.contains( OPERATOR_AND + subExp ) )
            {
                isAndCondion = true;
            }
            if ( expression.contains( OPERATOR_OR + subExp ) )
            {
                isOrCondion = true;
            }
            
            Boolean isData = false;
            Set<DataElement> dataElements = expressionService.getDataElementsInExpression( subExp );
            DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
            for( DataElement de : dataElements )
            {
                DataValue dataValue = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                if( dataValue != null && dataValue.getValue() != null && dataValue.getValue().trim() != "" )
                {
                    indicatorValue += " " + dataValue.getValue();
                    
                    if( de.getValueType().isDate() &&  dataValue.getValue().trim().equalsIgnoreCase( "NA" ) )
                    {
                        isData = false; break;
                    }
                    else
                    {
                        isData = true; break;
                    }
                }
            }
            
            if( isData )
            {
                value = "Yes";
            }
            
            //System.out.println( getter + " " + value );
            
            if ( isAndCondion )
            {
                if ( valueMap.containsKey( getter ) )
                {
                    if ( valueMap.get( getter ).toLowerCase().equalsIgnoreCase( "Yes".toLowerCase() ) && value.toLowerCase().equalsIgnoreCase( "Yes".toLowerCase() ) )
                    {
                        valueMap.put( getter, "Yes" );
                    }
                    else if ( (value.isEmpty() || valueMap.get( getter ).equals( "" )) )
                    {
                        valueMap.put( getter, "" );
                    }
                    else
                    {
                        valueMap.put( getter, "No" );
                    }
                }
                else
                {
                    valueMap.put( getter, value );
                }
            }
            else if ( isOrCondion )
            {
                if ( valueMap.containsKey( getter ) )
                {
                    if ( valueMap.get( getter ).equalsIgnoreCase( "Yes" ) || value.equalsIgnoreCase( "Yes" ) )
                    {
                        valueMap.put( getter, "Yes" );
                    }  
                    else if ( (value.isEmpty() && valueMap.get( getter ).equals( "" )) )
                    {
                        valueMap.put( getter, "" );
                    }
                    else
                    {
                        valueMap.put( getter, "No" );
                    }
                }
                else
                {
                    valueMap.put( getter, value );
                }
            }
            else
            {
                valueMap.put( getter, value );
            }
            getFlagColor(indicatorUid, orgUnit);
            //System.out.println("OR value "+ valueMap.get( getter ) );
        }

        if ( isValue )
        {
            if ( indicatorValue.endsWith( ".0" ) )
            {
                indicatorValue = indicatorValue.replace( ".0", "" );
            }
            return indicatorValue;
        }
        else
        {
            return valueMap.get( getter );
        }
    }
    
    
    public String getKeyIndicatorValueForDateDiff( String expression, String indicatorUid, OrganisationUnit orgUnit )
    {
        //System.out.println( "Inside getKeyIndicatorValueForDateDiff ");
        
        boolean isValue = false;

        String getter = valueMapGetter( indicatorUid, orgUnit );

        String indicatorValue = "";
        Pattern valueOperator = Pattern.compile( DE_VALUE );
        Matcher matcherOperator = valueOperator.matcher( expression );
        if ( matcherOperator.find() )
        {
            isValue = true;
        }        
        
        expression = expression.replaceAll( DE_VALUE, "" );
        expression = expression.replaceAll( "#DATE_DIFF#", "" );
                
        String[] expressionParts = expression.split( "," );
                
        String value = "";                
        
        //System.out.println( expression );
        //System.out.println( expressionParts[0] );
        //System.out.println( expressionParts[1] );
        
        String dataElementUID =  expressionParts[0].replaceAll("\\{","");
        dataElementUID =  dataElementUID.replaceAll("\\}","");
        dataElementUID =  dataElementUID.split( "\\." )[0];
        
        DataElement dataElement = dataElementService.getDataElement( dataElementUID );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        //System.out.println( "1 : " + value );
        DataValue dataValue = dataValueService.getLatestDataValue( dataElement, optionCombo, orgUnit );
        if( dataValue != null && dataValue.getValue() != null && !dataValue.getValue().trim().equals( "" ) )
        {
            indicatorValue += " " + dataValue.getValue();
            if( dataValue.getValue().trim().trim().equals( "NA" ) )
            {
                value = "";
            }
            else
            {
                value = formatDate( dataValue.getValue() );
            }
        }
        
        //System.out.println( "2 : " + value );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try
        {
            Date dateVal = ivbUtil.getStartDateByString( value );
            String dateValStr = simpleDateFormat.format( dateVal );
            
            String lastXmonths = expressionParts[1].split( "_" )[1];
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime( new Date() );
            int monthCount = (Integer.parseInt( lastXmonths ))*-1;
            calendar2.add( Calendar.MONTH, monthCount );
            Date lastXMonthDateVal = calendar2.getTime();
            
            //System.out.println( dateValStr + " : " + lastXMonthDateVal );
            if( dateVal.before( lastXMonthDateVal ) )
            {
                value = "Yes";
            }
        }
        catch( Exception e )
        {
            value = "";
        }

        valueMap.put( getter, value );
        
        getFlagColor(indicatorUid, orgUnit);
                

        if ( isValue )
        {
            if ( indicatorValue.endsWith( ".0" ) )
            {
                indicatorValue = indicatorValue.replace( ".0", "" );
            }
            return indicatorValue;
        }
        else
        {
            return valueMap.get( getter );
        }
    }
    
    public String getKeyIndicatorValueWithThresoldValue( String expression, String indicatorUid, OrganisationUnit orgUnit )
    {
        boolean isValue = false;

        if( expression.contains( "#VALUE_OF#" ) )
        {
            return getKyeIndicatorValueForProgrammatic( expression, indicatorUid, orgUnit );
        }
        else if( expression.contains( "#DATE_DIFF#" ) )
        {
            return getKeyIndicatorValueForDateDiff( expression, indicatorUid, orgUnit );
        }
        
        String getter = valueMapGetter( indicatorUid, orgUnit );

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
            String subExp = patternexpression[i].trim();

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
                    String calValue = getCalculatedValue( match, orgUnit );
                    value = calValue.split( "," )[0];
                    indicatorValue = calValue.split( "," )[1];
                }
                else
                {
                    DEValue = getAbsoluteValue( match, orgUnit );
                    indicatorValue += DEValue;
                    String[] contionExp = match.split( OPERAND_EXPRESSION );
                    Pattern conOperator = Pattern.compile( OPERATOR_EXPRESSION );
                    Matcher matchConOperator = conOperator.matcher( contionExp[1] );

                    if ( matchConOperator.find() )
                    {
                        if ( DEValue != null && DEValue != "" )
                        {
                            String[] expressionValue = subExp.split( matchConOperator.group() );

                            expressionValue[1] = expressionValue[1].replaceAll( "\\)", "" ).replaceAll( "[\\[\\]]", "" )
                                .replaceAll( "\\(", "" );
                            expressionValue[1] = expressionValue[1].replaceAll( "'", "" );
                            // System.out.println( matchConOperator.group() );
                            value = getIndicatorValue( matchConOperator.group(), DEValue, expressionValue[1], orgUnit );
                        }
                    }
                }

            }
            
            if ( isAndCondion )
            {
                if ( valueMap.containsKey( getter ) )
                {
                    if ( valueMap.get( getter ).toLowerCase().equalsIgnoreCase( "Yes".toLowerCase() ) && value.toLowerCase().equalsIgnoreCase( "Yes".toLowerCase() ) )
                    {
                        valueMap.put( getter, "Yes" );
                    }
                    else if ( (value.isEmpty() || valueMap.get( getter ).equals( "" )) )
                    {
                        valueMap.put( getter, "" );
                    }
                    else
                    {
                        valueMap.put( getter, "No" );
                    }
                }
                else
                {
                    valueMap.put( getter, value );
                }
            }
            else if ( isOrCondion )
            {
                if ( valueMap.containsKey( getter ) )
                {
                    if ( valueMap.get( getter ).equalsIgnoreCase( "Yes" ) || value.equalsIgnoreCase( "Yes" ) )
                    {
                        valueMap.put( getter, "Yes" );
                    }  
                    else if ( (value.isEmpty() && valueMap.get( getter ).equals( "" )) )
                    {
                        valueMap.put( getter, "" );
                    }
                    else
                    {
                        valueMap.put( getter, "No" );
                    }
                }
                else
                {
                    valueMap.put( getter, value );
                }
            }
            else
            {
                valueMap.put( getter, value );
            }
            getFlagColor(indicatorUid, orgUnit);
            //System.out.println("OR value "+ valueMap.get( getter ) );
        }

        if ( isValue )
        {
            if ( indicatorValue.endsWith( ".0" ) )
            {
                indicatorValue = indicatorValue.replace( ".0", "" );
            }
            return indicatorValue;
        }
        else
        {
            return valueMap.get( getter );
        }
    }

    public String getIndicatorValue( String condition, String dataElementValue, String expressionValue,
        OrganisationUnit orgUnit )
    {
        boolean isDateType = false;
        boolean isBetweenDates = false;
        String value = "";
        Date date1 = null;
        Date date2 = null;
        Date min = null;
        Date max = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        dataElementValue = formatDate( dataElementValue );
        
        String isbetweenExp = "(CURYEAR" + "|" + "NOW" + "|" + "NEXTYEAR" + "|" + "NEXT_" + numberExp + "_MONTHS" + "|" + "LAST_" + numberExp + "_MONTHS)";
        try
        {
            if ( expressionValue.equalsIgnoreCase( "NOW" ) )
            {
                isDateType = true;
                date1 = simpleDateFormat.parse( dataElementValue );
                date2 = new Date();
            }
            else if ( expressionValue.matches( "BETWEEN" + isbetweenExp + "," + isbetweenExp ) )
            {
                isBetweenDates = true;
                String[] afterBetween = expressionValue.split( "BETWEEN" );

                String[] inBetweenStr = afterBetween[1].split( "," );
                if ( dataElementValue.contains( "\\|" ) )
                {
                    String[] quarterDates = dataElementValue.split( "\\|" );

                    date1 = simpleDateFormat.parse( quarterDates[0] );
                    date2 = simpleDateFormat.parse( quarterDates[1] );
                }
                else
                {
                    date1 = simpleDateFormat.parse( dataElementValue );
                }
                min = calculateDate( inBetweenStr[0], null );
                max = calculateDate( inBetweenStr[1], min );
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
                    if ( dataElementValue.contains( "\\|" ) )
                    {
                        if ( (date1.getTime() >= min.getTime())
                            && (date2.getTime() <= max.getTime() || date1.getTime() == max.getTime()) )
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
                        if ( min.getTime() <= date1.getTime() && date1.getTime() <= max.getTime() )
                        {
                            value = "Yes";
                        }
                        else
                        {
                            value = "No";
                        }
                    }
                }
                else
                {
                    dataElementValue = dataElementValue.toLowerCase();
                    expressionValue = expressionValue.toLowerCase();
                    if ( dataElementValue.equalsIgnoreCase( expressionValue ) )
                    {
                        value = "Yes";
                    }
                    else
                    {
                        value = "No";
                    }
                     //System.out.println("dataElementValue \t"+dataElementValue);
                     //System.out.println("expressionValue \t"+expressionValue);
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
                    dataElementValue = dataElementValue.toLowerCase();
                    expressionValue = expressionValue.toLowerCase();
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
                else if ( isBetweenDates )
                {
                    if ( dataElementValue.contains( "\\|" ) )
                    {
                        if ( date1.getTime() <= date2.getTime() && date2.before( max ) )
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
                        if ( date1.before( max ) )
                        {
                            value = "Yes";
                        }
                        else
                        {
                            value = "No";
                        }
                    }
                    //System.out.println("value \t"+value);
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

        }
        catch ( Exception e )
        {

        }
        return value;
    }

    public String getCalculatedValue( String expression, OrganisationUnit orgUnit )
    {
        String DataElementValue = "";

        String[] expStrings = expression.split( "(\\*" + "|" + "\\/" + ")" );

        int count2 = 0;
        Double[] deValue = new Double[expStrings.length];
        for ( int i = 0; i < expStrings.length; i++ )
        {
            Double value = 0.0;
            Double value1 = 0.0;
            int count = 0;
            String[] expStrings2 = expStrings[i].split( "(\\+" + "|" + "\\-" + ")" );
            if ( expStrings2.length > 1 )
            {
                for ( int j = 1; j < expStrings2.length; j++ )
                {

                    String DEValue1 = getAbsoluteValue( expStrings2[count], orgUnit );
                    count++;
                    String DEValue2 = getAbsoluteValue( expStrings2[j], orgUnit );
                    // System.out.println( "DEValue2: \n"+DEValue2 );
                    String[] calExpression = expression.split( OPERAND_EXPRESSION );
                    if ( calExpression[1].equalsIgnoreCase( "-" ) )
                    {
                        if ( isDataDateType == true
                            && (DEValue2 == "" || DEValue2.equalsIgnoreCase( "0" ) || DEValue1 == "" || DEValue1
                                .equalsIgnoreCase( "0" )) )
                        {
                            value1 = 0.0;
                        }
                        else
                        {
                            if ( DEValue1 == "" )
                            {
                                DEValue1 = "0";
                            }
                            if ( DEValue2 == "" )
                            {
                                DEValue2 = "0";
                            }
                            value1 = Double.parseDouble( DEValue1 ) - Double.parseDouble( DEValue2 );
                        }

                    }
                    else if ( calExpression[1].equalsIgnoreCase( "+" ) )
                    {
                        if ( DEValue1 == "" )
                        {
                            DEValue1 = "0";
                        }
                        if ( DEValue2 == "" )
                        {
                            DEValue2 = "0";
                        }
                        value1 = Double.parseDouble( DEValue1 ) + Double.parseDouble( DEValue2 );

                    }
                    // System.out.println("Data Element Value:\t"+value1);
                }

                if ( expStrings.length == 1 )
                {
                    value = value1;
                }
                else
                {
                    deValue[i] = value1;

                    String[] calExpression = expression.split( OPERAND_EXPRESSION );
                    calExpression[2] = calExpression[2].replaceAll( "\\)", "" ).replaceAll( "[\\[\\]]", "" )
                        .replaceAll( "\\(", "" );

                    if ( calExpression[2].equalsIgnoreCase( "*" ) )
                    {
                        if ( (i - 1) != -1 && deValue[i - 1] != 0 && deValue[i] != 0 )
                        {

                            value = deValue[i - 1] * deValue[i];
                        }

                    }
                    else if ( calExpression[2].equalsIgnoreCase( "/" ) )
                    {

                        if ( (i - 1) != -1 && deValue[i - 1] != 0.0 && deValue[i] != 0.0 )
                        {

                            value = deValue[i - 1] / deValue[i];
                        }
                    }
                    // System.out.println("Calculated Value:\t"+value);
                }
            }
            else
            {
                String DEValue1 = getAbsoluteValue( expStrings[count2], orgUnit );
                count2++;
                String DEValue2 = getAbsoluteValue( expStrings[i + 1], orgUnit );
                String[] calExpression = expression.split( OPERAND_EXPRESSION );
                if ( calExpression[1].equalsIgnoreCase( "*" ) )
                {
                    if ( DEValue1 == "" )
                    {
                        DEValue1 = "0";
                    }
                    if ( DEValue2 == "" )
                    {
                        DEValue2 = "0";
                    }
                    value = Double.parseDouble( DEValue1 ) * Double.parseDouble( DEValue2 );

                }
                else if ( calExpression[1].equalsIgnoreCase( "/" ) )
                {
                    if ( DEValue1 == "" && DEValue2 != "" )
                    {
                        DEValue1 = "0";
                        value = Double.parseDouble( DEValue1 ) / Double.parseDouble( DEValue2 );
                    }
                    else if ( DEValue2 == "" )
                    {
                        value = 0.0;
                    }
                }
            }

            String artmetic = "";
            String[] arthmeticExp = expression.split( ARITHMETIC_OPERATOR );
            for ( int k = 0; k <= arthmeticExp.length - 1; k++ )
            {
                artmetic = arthmeticExp[k];
            }

            String[] contionExp = artmetic.split( OPERAND_EXPRESSION );
            contionExp[contionExp.length - 1] = contionExp[contionExp.length - 1].replaceAll( "\\)", "" )
                .replaceAll( "[\\[\\]]", "" ).replaceAll( "\\(", "" );

            Pattern conOperator = Pattern.compile( OPERATOR_EXPRESSION );
            Matcher matchConOperator = conOperator.matcher( contionExp[contionExp.length - 1] );

            if ( matchConOperator.find() )
            {

                String[] expressionValue = expression.split( matchConOperator.group() );

                expressionValue[1] = expressionValue[1].replaceAll( "\\)", "" ).replaceAll( "[\\[\\]]", "" )
                    .replaceAll( "\\(", "" );
                expressionValue[1] = expressionValue[1].replaceAll( "'", "" );

                String calValue = getIndicatorValue( matchConOperator.group(), value + "", expressionValue[1], orgUnit );

                DataElementValue = calValue + "," + value;

            }
            else
            {
                DataElementValue = "," + value;
            }
        }
        return DataElementValue;
    }

    public String getAbsoluteValue( String expression, OrganisationUnit orgUnit )
    {
        String DataElementValue = "";
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( 1 );
        SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        try
        {
            if ( expression.contains( "YEAROF" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>(
                    expressionService.getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                    if ( dv != null )
                    {
                        if ( dv.getValue().isEmpty() || dv.getValue() == " " || dv.getValue() == null
                            || dv.getValue() == "" )
                        {
                        }
                        else if( de.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                        {
                            
                        }
                        else
                        {
                            String value = formatDate( dv.getValue() );
                            Date date = standardDateFormat.parse( value );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( date );
                            DataElementValue = calendar.get( Calendar.YEAR ) + "";
                        }
                    }
                }
            }
            else if ( expression.contains( "MONTHOF" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>(
                    expressionService.getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                    if ( dv != null )
                    {
                        if ( dv.getValue().isEmpty() || dv.getValue() == " " || dv.getValue() == null
                            || dv.getValue() == "" )
                        {
                        }
                        else if( de.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                        {
                            
                        }
                        else
                        {
                            String value = formatDate( dv.getValue() );
                            Date date = standardDateFormat.parse( value );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( date );
                            DataElementValue = calendar.get( Calendar.MONTH ) + "";
                        }
                    }
                }
            }
            else if ( expression.contains( "DATEOF" ) )
            {
                //System.out.println( "1.");
                
                List<DataElement> deNuList = new ArrayList<DataElement>(
                    expressionService.getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                    if ( dv != null )
                    {
                        if ( dv.getValue().isEmpty() || dv.getValue() == " " || dv.getValue() == null
                            || dv.getValue() == "" )
                        {
                        }
                        else if( de.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                        {
                            
                        }
                        else
                        {
                            String value = formatDate( dv.getValue() );
                            DataElementValue = value;
                        }
                    }
                }
            }
            if ( expression.contains( "CURYEAR" ) )
            {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( date );
                DataElementValue = calendar.get( Calendar.YEAR ) + "";
            }
            if ( expression.startsWith( "MAX" ) )
            {
                List<DataElement> deNuList = new ArrayList<DataElement>(
                    expressionService.getDataElementsInExpression( expression ) );
                if ( expression.contains( "DATEOF" ) || expression.contains( "MONTHOF" )
                    || expression.contains( "YEAROF" ) )
                {
                    isDataDateType = true;
                }
                long curValue = 0;
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                    if ( dv != null )
                    {
                        if ( dv.getValue().isEmpty() || dv.getValue() == " " || dv.getValue() == null
                            || dv.getValue() == "" )
                        {
                        }
                        else if( de.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                        {
                            
                        }
                        else
                        {
                            String value = formatDate( dv.getValue() );
                            if ( expression.contains( "DATEOF" ) )
                            {
                                //System.out.println( "2.");
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
                            DataElementValue = curValue + "";
                        }
                    }

                }

                DataElementValue = curValue + "";

            }
            else
            {
                List<DataElement> deNuList = new ArrayList<DataElement>( expressionService.getDataElementsInExpression( expression ) );
                for ( DataElement de : deNuList )
                {
                    DataValue dv = dataValueService.getLatestDataValue( de, optionCombo, orgUnit );
                    if ( dv != null && dv.getValue() != null )
                    { 
                        if( de.getValueType().isDate() &&  dv.getValue().trim().equalsIgnoreCase( "NA" ) )
                        {
                            DataElementValue = "";
                        }
                        else
                        {
                            DataElementValue = dv.getValue();
                        }
                    }
                }
            }

        }
        catch ( Exception e )
        {
        }
        return DataElementValue;
    }

    public String formatDate( String dataValue )
    {
        String value = dataValue.trim();
        if ( value.matches( "([0-9]{4})" ) )
        {
            value = value + "-01-01";
        }
        else if ( value.matches( "\\d{4}-\\d{2}" ) )
        {
            value = value + "-01";
        }
        else if ( value.split( "-" ).length > 1 )
        {
            if ( value.split( "-" )[1].equalsIgnoreCase( "Q1" ) || value.matches( "\\d{4}-Q1" ) )
            {
                value = value.split( "-" )[0] + "-01-01" + "\\|" + value.split( "-" )[0] + "-03-31";
            }
            else if ( value.split( "-" )[1].equalsIgnoreCase( "Q2" ) || value.matches( "\\d{4}-Q2" ) )
            {
                value = value.split( "-" )[0] + "-04-01" + "\\|" + value.split( "-" )[0] + "-06-30";
            }
            else if ( value.split( "-" )[1].equalsIgnoreCase( "Q3" ) || value.matches( "\\d{4}-Q3" ) )
            {
                value = value.split( "-" )[0] + "-07-01" + "\\|" + value.split( "-" )[0] + "-09-30";
            }
            else if ( value.split( "-" )[1].equalsIgnoreCase( "Q4" ) || value.matches( "\\d{4}-Q4" ) )
            {
                value = value.split( "-" )[0] + "-10-01" + "\\|" + value.split( "-" )[0] + "-12-31";
            }
        }
        else
        {
        }
        return value;
    }

    public Date calculateDate( String dateStr, Date perviousDate )
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date calculatedDate = null;
        try
        {
            if ( dateStr.matches( "NOW" ) )
            {
                Date curdate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( curdate );
                int currentMonth = calendar.get( Calendar.MONTH );
                String date;
                if ( currentMonth >= 0 && currentMonth <= 2 )
                {
                    date = calendar.get( Calendar.YEAR ) + "-01-01";
                }
                else if ( currentMonth >= 3 && currentMonth <= 5 )
                {
                    date = calendar.get( Calendar.YEAR ) + "-04-01";
                }
                else if ( currentMonth >= 6 && currentMonth <= 8 )
                {
                    date = calendar.get( Calendar.YEAR ) + "-07-01";
                }
                else
                {
                    date = calendar.get( Calendar.YEAR ) + "-10-01";
                }
                calculatedDate = simpleDateFormat.parse( date );
            }
            else if ( dateStr.matches( "CURYEAR" ) )
            {
                Date curdate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( curdate );
                String date = calendar.get( Calendar.YEAR ) + "-01-01";
                calculatedDate = simpleDateFormat.parse( date );
            }
            else if ( dateStr.matches( "NEXTYEAR" ) )
            {
                Date curdate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime( curdate );
                String date = calendar.get( Calendar.YEAR ) + 1 + "-01-01";
                calculatedDate = simpleDateFormat.parse( date );
            }
            else if ( dateStr.matches( "NEXT_" + numberExp + "_MONTHS" ) )
            {
                Calendar calendar = Calendar.getInstance();
                String[] next_month = dateStr.split( "_" );
                String month = "";
                int yearCount = 0;
                String date = "";
                int previousMonth = 0;
                int previousYear = 0;
                int previousDate = 0;
                String dateValue = "";
                if ( perviousDate != null )
                {
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime( perviousDate );
                    previousMonth = calendar2.get( Calendar.MONTH );
                    previousYear = calendar2.get( Calendar.YEAR );
                    previousDate = calendar2.get( Calendar.DATE );
                }
                if ( previousDate != 0 )
                {
                    dateValue = previousDate + "";
                }
                else
                {
                    dateValue = "01";
                }
                //System.out.println( previousMonth );
                //System.out.println( Integer.parseInt( next_month[1] ) );
                if ( (Integer.parseInt( next_month[1] ) + previousMonth) + 1 > 12 )
                {
                    int monthCount = 1;
                    for ( int i = 12; i <= Integer.parseInt( next_month[1] ) + previousMonth; i = i + 12 )
                    {
                        yearCount++;
                        monthCount = Integer.parseInt( next_month[1] ) + previousMonth - i + 1;
                    }
                    month = monthCount + "";
                    if ( month.matches( "[1-9]" ) )
                    {
                        month = "0" + monthCount;
                    }

                    if ( previousYear > calendar.get( Calendar.YEAR ) )
                    {
                        date = (previousYear + yearCount) + "-" + month + "-" + dateValue;
                    }
                    else
                    {
                        date = (calendar.get( Calendar.YEAR ) + yearCount) + "-" + month + "-" + dateValue;
                    }

                }
                else
                {
                    month = next_month[1];
                    if ( previousYear > calendar.get( Calendar.YEAR ) )
                    {
                        date = (previousYear) + "-" + month + "-01";
                    }
                    else
                    {
                        date = (calendar.get( Calendar.YEAR )) + "-" + month + "-" + dateValue;
                    }
                    date = (calendar.get( Calendar.YEAR )) + "-" + month + "-" + dateValue;
                }
                calculatedDate = simpleDateFormat.parse( date );
            }
            else if ( dateStr.matches( "LAST_" + numberExp + "_MONTHS" ) )
            {
                //System.out.println( "3.");
                Calendar calendar = Calendar.getInstance();
                String[] next_month = dateStr.split( "_" );
                String month = "";
                int yearCount = 0;
                String date = "";
                int previousMonth = 0;
                int previousYear = 0;
                int previousDate = 0;
                String dateValue = "";
                
                Calendar calendar2 = Calendar.getInstance();
                if ( perviousDate != null )
                {                    
                    calendar2.setTime( perviousDate );
                    previousMonth = calendar2.get( Calendar.MONTH );
                    previousYear = calendar2.get( Calendar.YEAR );
                    previousDate = calendar2.get( Calendar.DATE );
                    
                    //System.out.println( previousMonth + "/" + previousYear );
                }
                else
                {
                    calendar2.setTime( new Date() );
                }
                
                if ( previousDate != 0 )
                {
                    dateValue = previousDate + "";
                }
                else
                {
                    dateValue = "01";
                }
                
                int monthCount = (Integer.parseInt( next_month[1] ))*-1;
                
                calendar2.add( Calendar.MONTH, monthCount );
                date = calendar2.get( Calendar.YEAR ) + "-" + calendar2.get( Calendar.MONTH ) + "-" + dateValue;
                
                /*
                int monthCount = Integer.parseInt( next_month[1] );
                
                
                for( int i=1; i <= monthCount; i++ )
                {
                    
                    previousMonth--;
                    if( previousMonth == 0 )
                    {
                        previousYear--;
                        previousMonth = 12;
                    }
                }
                
                date = previousYear + "-" + previousMonth + "-" + dateValue;
                */
                
                /*
                //System.out.println( previousMonth );
                //System.out.println( Integer.parseInt( next_month[1] ) );
                if ( (Integer.parseInt( next_month[1] ) + previousMonth) + 1 > 12 )
                {
                    int monthCount = 1;
                    for ( int i = 12; i <= Integer.parseInt( next_month[1] ) + previousMonth; i = i + 12 )
                    {
                        yearCount++;
                        monthCount = Integer.parseInt( next_month[1] ) + previousMonth - i + 1;
                    }
                    month = monthCount + "";
                    if ( month.matches( "[1-9]" ) )
                    {
                        month = "0" + monthCount;
                    }

                    if ( previousYear > calendar.get( Calendar.YEAR ) )
                    {
                        date = (previousYear + yearCount) + "-" + month + "-" + dateValue;
                    }
                    else
                    {
                        date = (calendar.get( Calendar.YEAR ) + yearCount) + "-" + month + "-" + dateValue;
                    }

                }
                else
                {
                    month = next_month[1];
                    if ( previousYear > calendar.get( Calendar.YEAR ) )
                    {
                        date = (previousYear) + "-" + month + "-01";
                    }
                    else
                    {
                        date = (calendar.get( Calendar.YEAR )) + "-" + month + "-" + dateValue;
                    }
                    date = (calendar.get( Calendar.YEAR )) + "-" + month + "-" + dateValue;
                }
                */
                calculatedDate = simpleDateFormat.parse( date );
            }

        }
        catch ( ParseException e )
        {
            e.printStackTrace();
        }
        return calculatedDate;
    }

}