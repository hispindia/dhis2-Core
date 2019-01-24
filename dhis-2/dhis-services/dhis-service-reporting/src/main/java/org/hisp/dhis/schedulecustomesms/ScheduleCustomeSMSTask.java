package org.hisp.dhis.schedulecustomesms;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleCustomeSMSTask
    extends AbstractJob
{
    private static final Log log = LogFactory.getLog( ScheduleCustomeSMSTask.class );

    private final static int NAME_ATTRIBUTE_ID = 136;

    private final static int MOBILE_NUMBER_ATTRIBUTE_ID = 142;

    private final static int NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID = 133470;

    private final static int ANC_FIRST_VISIT_PROGRAM_STAGE_ID = 1339;

    private final static int ANC_VISITS_2_4_PROGRAM_STAGE_ID = 1364;

    private final static int CHILD_HEALTH_IMMUNIZATION_PROGRAM_STAGE_ID = 2125;

    private final static int POST_NATAL_CARE_PROGRAM_STAGE_ID = 1477;

    public static final String KEY_TASK = "scheduleCustomeSMSTask";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;

    @Autowired
    private TrackedEntityAttributeService trackedEntityAttributeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private SimpleDateFormat simpleDateFormat;

    private String complateDate = "";

    private Period currentperiod;

    private String trackedEntityInstanceIds = "";

    String currentDate = "";

    String currentMonth = "";

    String currentYear = "";

    String todayDate = "";

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.SCHEDULE_CUSTOM_SMS;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration )
    {
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
        // get current date time with Date()
        Date date = new Date();
        System.out.println( timeFormat.format( date ) );

        todayDate = simpleDateFormat.format( date );
        currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        //String currentHour = timeFormat.format( date ).split( ":" )[0];
        
        System.out.println( "INFO: scheduler Custome SMS job has started at : " + new Date() + " -- "
            + JobType.SCHEDULE_CUSTOM_SMS );
        boolean isScheduleCustomeSMSJobEnabled = (Boolean) systemSettingManager
            .getSystemSetting( SettingKey.SCHEDULE_CUSTOM_SMS );
        System.out.println( "isScheduleCustomeSMSJobEnabled -- " + isScheduleCustomeSMSJobEnabled );

        if ( !isScheduleCustomeSMSJobEnabled )
        {
            log.info( String.format( "%s aborted. Schedule Custome SMS Job are disabled", KEY_TASK ) );

            return;
        }

        log.info( String.format( "%s has started", KEY_TASK ) );

        try
        {
            scheduledNPCDCSProgramCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID, NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID );
            scheduledANCProgrammeCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID, ANC_FIRST_VISIT_PROGRAM_STAGE_ID );
            scheduledANCVISITS24CustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID, ANC_VISITS_2_4_PROGRAM_STAGE_ID );
            scheduledPNCProgrammeCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID, POST_NATAL_CARE_PROGRAM_STAGE_ID );
            scheduledChildHealthProgrammeCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID,
                CHILD_HEALTH_IMMUNIZATION_PROGRAM_STAGE_ID );
        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.out.println( "Error SMS " + e1.getMessage() );
        }

    }

    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------

    // NPCDCS Program (On Scheduling)
    public void scheduledNPCDCSProgramCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " NPCDCS_FOLLOW_UP SMS Scheduler Started at : " + new Date() + " -- current date  -  " +  currentDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );

                    // System.out.println( " 11-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 12-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * old String customMessage = teiName + " " +
                         * " डिस्पेंसरी में उच्च रक्त चाप, मधुमेह, स्ट्रोक वा केंसर जाँच कार्यक्रम में आप में पाई गयी बीमारी कि नियमित जाँच/ चेक-अप के लिए आपको डिस्पेंसरी में "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे का समय दिया गया हैं |  स्वस्थ रहने के लिए ज़रूरी है की आप समय समय पर अपनी जाँच करते रहें "
                         * ;
                         */
                        String customMessage = teiName
                            + " "
                            + ", 25 सेक्टर की डिस्पेंसरी में हाइ बीपी, शुगर, स्ट्रोक और कैंसर प्रोग्राम में जाँच के दौरान आप  में पाई गयी बीमारी की नियमित जाँच के लिए आप को दिनांक "
                            + dueDate
                            + " "
                            + "को डिस्पेंसरी आने का अनुरोध  किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| “स्वस्थ रहने के लिए ज़रूरी  है कि आप समय समय पर अपनी जाँच कराते रहें ";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " NPCDCS_FOLLOW_UP SMS Scheduler End at : " + new Date() );
    }

    // ANC Programme (On Scheduling)
    public void scheduledANCProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " ANC Programme SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 21-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 22-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसवपूर्व देखभाल  कि नियमित जाँच के लिए आपको डिस्पेंसरी में  "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच का समय दिया गया हैं | गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य  के लिए समय समय पर जाँच करना ज़रूरी है"
                         * ;
                         */

                        String customMessage = teiName
                            + " "
                            + ", सूचित किया जाता है कि प्रेग्नेन्सी दौरान देखभाल हेतु नियमित जाँच के लिए आपको   डिस्पेंसरी में दिनांक  "
                            + dueDate
                            + " "
                            + " को आने का अनुरोध किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य के लिए समय समय पर  जाँच करवाना ज़रूरी है ' ";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );

                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( "ANC Programme  SMS Scheduler End at : " + new Date() );
    }

    // ANC Programme 2 and 4 (On Scheduling)
    public void scheduledANCVISITS24CustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " ANC Programme 2 and 4 SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 31-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 32-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसवपूर्व देखभाल  कि नियमित जाँच के लिए आपको डिस्पेंसरी में  "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच का समय दिया गया हैं | गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य  के लिए समय समय पर जाँच करना ज़रूरी है"
                         * ;
                         */

                        String customMessage = teiName
                            + " "
                            + ", सूचित किया जाता है कि प्रेग्नेन्सी दौरान देखभाल हेतु नियमित जाँच के लिए आपको   डिस्पेंसरी में दिनांक  "
                            + dueDate
                            + " "
                            + " को आने का अनुरोध किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य के लिए समय समय पर  जाँच करवाना ज़रूरी है ' ";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " ANC Programme 2 and 4 SMS Scheduler End at : " + new Date() );
    }

    // PNC Programme(On Scheduling)
    public void scheduledPNCProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " PNC Programme SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 41-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 42-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसव के बाद कि देखभाल कि नियमित जाँच के लिए आपसे    "
                         * + dueDate + " " +
                         * " को डिस्पेंसरी  कि एएनएम या आशा दीदी आकर मिलेंगी |";
                         */

                        String customMessage = teiName
                            + " "
                            + " ,आपकी डेलिवरी के बाद की देखभाल हेतु दिनांक   "
                            + dueDate
                            + " को डिस्पेंसरी की आशा या ए एन एम "
                            + " "
                            + " दीदी आपको मिलने आएँगी| उस दौरान माँ और नवजात से संबंधित कोई भी दिक्कत हो तो दीदी को बताएँ| वो आप  की मदद करेंगी |";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " PNC Programme SMS Scheduler End at : " + new Date() );
    }

    // Child Health Programme(On Scheduling)
    public void scheduledChildHealthProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " Child Health Programme(On Scheduling) SMS Scheduler Started at : " + new Date()
            + " -- current date  -  " + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 51-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 52-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }
                        /*
                         * String customMessage = teiName + " " +
                         * " आपके बच्चे के टीकाकरण के लिए आपको  " + dueDate +
                         * " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच डिस्पेंसरी में आकर बच्चे को टीका लगवाना हैं  | बच्चे को जानलेवा बीमारियों से बचाने के लिए टीकाकरण करना ज़रूरी होता है |"
                         * ;
                         */
                        String customMessage = teiName
                            + " "
                            + " , कृपा अपने बच्चे को दिनांक   "
                            + dueDate
                            + " वाले दिन डिस्पेंसरी में  ला कर उस का टीकाकरण करवाएँ| "
                            + " "
                            + " डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'बच्चे को जानलेवा बीमारियों से बचाने के लिए टीकाकरण करवाना ज़रूरी होता है '";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " Child Health Programme(On Scheduling) SMS Scheduler End at : " + new Date() );
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public String getTrackedEntityInstanceIdsByAttributeId( Integer attributeId )
    {
        String trackedEntityInstanceIds = "-1";

        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                + "WHERE value = 'true' AND trackedentityattributeid =  " + attributeId
                + " order by trackedentityinstanceid ASC ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                if ( teiId != null )
                {
                    trackedEntityInstanceIds += "," + teiId;
                }
            }

            return trackedEntityInstanceIds;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<String> getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds(
        Integer attributeId, String trackedEntityInstanceIdsByComma )
    {
        List<String> mobileNumbers = new ArrayList<>();

        try
        {
            String query = "SELECT value FROM trackedentityattributevalue " + "WHERE trackedentityattributeid =  "
                + attributeId + " AND trackedentityinstanceid in ( " + trackedEntityInstanceIdsByComma + ")";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String mobileNo = rs.getString( 1 );
                if ( mobileNo != null )
                {
                    mobileNumbers.add( mobileNo );
                }
            }

            return mobileNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<TrackedEntityInstance> getTrackedEntityInstancesByAttributeId( Integer attributeId )
    {
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>();

        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                + "WHERE value = 'true' AND trackedentityattributeid =  " + attributeId
                + " order by trackedentityinstanceid ASC ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                if ( teiId != null )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiId );
                    teiList.add( tei );
                }
            }

            return teiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public String getLatestEventOrgAndDataValue( Integer psId, Integer dataElementId, Integer teiId )
    {
        String orgUnitIdAndValue = "";
        List<String> tempResult = new ArrayList<String>();
        try
        {
            String query = "SELECT psi.organisationunitid, tedv.dataelementid,tedv.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentitydatavalue tedv  ON  tedv.programstageinstanceid = psi.programstageinstanceid "
                + "WHERE psi.programstageid = "
                + psId
                + " AND tedv.dataelementid = "
                + dataElementId
                + "  AND pi.trackedentityinstanceid =  " + teiId + " order by psi.lastupdated desc ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                String value = rs.getString( 3 );

                if ( orgUnitId != null && value != null )
                {
                    tempResult.add( orgUnitId + ":" + value );
                }
            }

            if ( tempResult != null && tempResult.size() > 0 )
            {
                orgUnitIdAndValue = tempResult.get( 0 );
            }

            return orgUnitIdAndValue;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    //
    private String convertHtmlEntities( String s )
    {
        Pattern pattern = Pattern.compile( "\\&#(\\d{1,7});" );
        Matcher m = pattern.matcher( s );
        StringBuffer sb = new StringBuffer();
        while ( m.find() )
        {
            int cp = Integer.parseInt( m.group( 1 ) );
            String ch = new String( new int[] { cp }, 0, 1 );
            m.appendReplacement( sb, ch );
        }
        m.appendTail( sb );
        return sb.toString();
    }

}
