package org.hisp.dhis.lookup;

import java.io.Serializable;

import org.hisp.dhis.common.BaseNameableObject;

@SuppressWarnings( "serial" )
public class Lookup
    extends BaseNameableObject
    implements Serializable
{

    public static final String AGGREGATION_TYPE = "AGGREGATION_TYPE";

    public static final String AGG_TYPE_BY_OPTION_SCORE = "BY OPTION SCORE";

    public static final String AGG_TYPE_BY_OPTION = "BY OPTION";

    public static final String AGG_TYPE_BY_KEYFLAG_SCORE = "BY KEYFLAG SCORE";

    public static final String AGG_TYPE_BY_KEYFLAG = "BY KEYFLAG";

    public static final String ORGUNITID_BY_COMMA = "ORGUNITID_BY_COMMA";

    public static final String CURRENT_PERIOD_ENDDATE = "CURRENT_PERIOD_ENDDATE";

    public static final String PERIODID = "PERIODID";

    public static final String IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID = "IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID";

    public static final String TABULAR_REPORT_DATAELEMENTGROUP_ID = "TABULAR_REPORT_DATAELEMENTGROUP_ID";

    public static final String WHO_REGIONS_GROUPSET = "WHO_REGIONS_GROUPSET";

    public static final String UNICEF_REGIONS_GROUPSET = "UNICEF_REGIONS_GROUPSET";

    public static final String TIER_GROUPSET = "TIER_GROUPSET";

    public final static String COMMENT_REQUIRED_DATAELEMENT_ATTRIBUTE_ID = "COMMENT_REQUIRED_DATAELEMENT_ATTRIBUTE_ID";

    public static final String IS_PERCENTAGE = "IS_PERCENTAGE";

    public static final String KEYFLAG_INDICATOR_ATTRIBUTE_ID = "KEYFLAG_INDICATOR_ATTRIBUTE_ID";// 4
    
    public static final String BWA_ACTIVITY_REPORT_PARAMS = "BWA_ACTIVITY_REPORT_PARAMS";
    
    public static final String HPVDEMO_REPORT_PARAMS_ORGUNITGROUP = "HPVDEMO_REPORT_PARAMS_ORGUNITGROUP";
    
    public static final String HPVDEMO_REPORT_PARAMS = "HPVDEMO_REPORT_PARAMS";

    public static final String RESTRICTED_DE_ATTRIBUTE_ID = "RESTRICTED_DE_ATTRIBUTE_ID";
    
    public static final String CMYP_DEV_TRACKING_REPORT_PARAMS = "CMYP_DEV_TRACKING_REPORT_PARAMS";
    
    public static final String EXPRESSION_FOR_ALERT_ATTR_ID = "EXPRESSION_FOR_ALERT_ATTR_ID";
    
    private String type;

    private String value;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Lookup()
    {

    }

    // -------------------------------------------------------------------------
    // hashCode, equals
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof Lookup) )
        {
            return false;
        }

        final Lookup other = (Lookup) o;

        return name.equals( other.getName() );
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

}
