package org.hisp.dhis.schedulecustomesms;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * @author Mithilesh Kumar Thakur
 */

@Component( "customSMSJob" )
public class ScheduleCustomeSMSTask implements Job
{
    private final static int SMS_CONSENT_ATTRIBUTE_ID = 2618;
    
    private final static int IMP_AGENCY_ATTRIBUTE_ID = 4728214;
    
    private final static String IMP_AGENCY_ATTRIBUTE_VALUE = "NCASC";

    private final static int SEX_ATTRIBUTE_ID = 2613;

    private final static int MOBILE_NUMBER_ATTRIBUTE_ID = 2617;
    
    private final static int MARITAL_STATUS_ATTRIBUTE_ID = 2614;
    
    private final static int AGE_ATTRIBUTE_ID = 2612;
    
    private final static int ART_FOLLOW_UP_PROGRAM_STAGE_ID = 2537;

    private final static int MEDICAL_HISTORY_PROGRAM_STAGE_ID = 2485;

    private final static int ART_HIV_PROGRAM_STAGE_ID = 2454;

    private final static int PREGNANCY_DELIVERY_PROGRAM_STAGE_ID = 9682;

    private final static int EID_AND_TREATMENT_PROGRAM_STAGE_ID = 2577;

    private final static int CD4_TEST_DATAELEMENT_ID = 2378;

    private final static int CLIENT_TEST_ART_RESULT_DATAELEMENT_ID = 2341;

    private final static int AGE_AT_VISIT_DATAELEMENT_ID = 5001;

    private final static int CD4_TEST_FOLLOW_UP_DATAELEMENT_ID = 2397;

    private final static int VIRAL_LOAD_FOLLOW_UP_DATAELEMENT_ID = 2399;

    private final static int EDD_DATAELEMENT_ID = 3896;

    private final static int DATE_OF_BIRTH_INFANT_DATAELEMENT_ID = 10398;
    
    private final static int DNA_PCR_TEST_RESULT_DATAELEMENT_ID = 10328;
    
    private final static int PREGNANCY_STATUS_DATAELEMENT_ID = 10328;
    
    private final static int STATUS_OF_INFANT_DATAELEMENT_ID = 2355;
    
    private final static int AGE_OF_INFANT_DATAELEMENT_ID = 10329;
    
    
    private static final String KEY_TASK = "scheduleCustomeSMSTask";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private final Notifier notifier;
    
    private OrganisationUnitService organisationUnitService;

    private TrackedEntityInstanceService trackedEntityInstanceService;
    
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;
    
    private TrackedEntityAttributeService trackedEntityAttributeService;
    
    private JdbcTemplate jdbcTemplate;
        
    public ScheduleCustomeSMSTask( Notifier notifier, OrganisationUnitService organisationUnitService,
        TrackedEntityInstanceService trackedEntityInstanceService,TrackedEntityAttributeValueService trackedEntityAttributeValueService,
        TrackedEntityAttributeService trackedEntityAttributeService, JdbcTemplate jdbcTemplate)
    {

        checkNotNull( notifier );
        checkNotNull( organisationUnitService );
        checkNotNull( trackedEntityInstanceService );
        checkNotNull( trackedEntityAttributeValueService );
        checkNotNull( trackedEntityAttributeService );
        checkNotNull( jdbcTemplate );
        
        this.notifier = notifier;
        this.organisationUnitService = organisationUnitService;
        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.trackedEntityAttributeValueService = trackedEntityAttributeValueService;
        this.trackedEntityAttributeService = trackedEntityAttributeService;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private SimpleDateFormat simpleDateFormat;

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
        return JobType.CUSTOM_SMS_TASK;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration, JobProgress progress )
    {
        Clock clock = new Clock().startClock();

        clock.logTime( "Starting scheduler custom SMS job " );
        notifier.notify( jobConfiguration, INFO, "Start scheduler custom SMS job ", true );

        //sendMessages();
        
        notifier.notify( jobConfiguration, INFO, String.format( "%s has started", KEY_TASK ) );
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
        // get current date time with Date()
        Date date = new Date();
        //System.out.println( timeFormat.format( date ) );

        todayDate = simpleDateFormat.format( date );
        currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        
        LocalDate today = LocalDate.now();
        
        //Getting the day of week for a given date
        java.time.DayOfWeek dayOfWeek = today.getDayOfWeek();
        System.out.println(today + " was a " + dayOfWeek.toString().equalsIgnoreCase( "FRIDAY") );
        
        //String currentHour = timeFormat.format( date ).split( ":" )[0];

        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>( getTrackedEntityInstancesByAttributeId() );
        notifier.notify( jobConfiguration, INFO, " TEI list size " + teiList.size() );
        
        List<String> mobileNumbers = new ArrayList<String>();
        
        if ( teiList != null && teiList.size() > 0 )
        {
            mobileNumbers = new ArrayList<String>( getTrackedEntityInstanceAttributeValue() );
        }

        notifier.notify( jobConfiguration, INFO, " mobile No list size " + mobileNumbers.size() );
        /*
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>( getTrackedEntityInstancesByAttributeId( SMS_CONSENT_ATTRIBUTE_ID ) );
        String trackedEntityInstanceIds = getTrackedEntityInstanceIdsByAttributeId( SMS_CONSENT_ATTRIBUTE_ID );
        //List<String> tempMobileNumbers = new ArrayList<String>();
        
        if ( trackedEntityInstanceIds != null && trackedEntityInstanceIds.length() > 0 )
        {
            mobileNumbers = new ArrayList<String>(
                getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds(
                    MOBILE_NUMBER_ATTRIBUTE_ID, trackedEntityInstanceIds ) );
        }
                */
        
        try
        {
            scheduledCustomePillPickUPANDTbScreeingSMS( ART_FOLLOW_UP_PROGRAM_STAGE_ID, SMS_CONSENT_ATTRIBUTE_ID, MOBILE_NUMBER_ATTRIBUTE_ID );
            scheduledCustomeCD4CountSMS( MEDICAL_HISTORY_PROGRAM_STAGE_ID, SMS_CONSENT_ATTRIBUTE_ID, MOBILE_NUMBER_ATTRIBUTE_ID, CD4_TEST_DATAELEMENT_ID );
            scheduledCustomeCD4CountAndViralLoadARTSMS( teiList );
            scheduledEIDAfter4WeekOfDevliverySMS( teiList );
            scheduledPMTCTEIDSMS( teiList );
            
        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.out.println( "Error SMS " + e1.getMessage() );
        }

        // trimester Messages
        if ( currentMonth.equalsIgnoreCase( "03" ) || currentMonth.equalsIgnoreCase( "06" )
            || currentMonth.equalsIgnoreCase( "09" ) || currentMonth.equalsIgnoreCase( "12" ) )
        {
            if ( currentDate.equalsIgnoreCase( "03" ) )
            {
                List<String> trimesterMessages = new ArrayList<String>();
                trimesterMessages.add( "औषधी शुरु गरेको र CD4 जाँच गरेको ६ महिना भए , पुनः CD4 जाँच गर्ने होइन त ?" );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, trimesterMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
            
            //Pregnant female clients
            if ( currentDate.equalsIgnoreCase( "13" ) )
            {
                String trimesterMessage = "स्वास्थ्य संस्थामा सुत्केरी गराऔं, आमा र बच्चा दुवैको स्वास्थ्य पक्का गराऔं |";
                try
                {
                    tempScheduledAwarenessEIDSMS( teiList, trimesterMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
            
            //Female client who delivered live birth
            if ( currentDate.equalsIgnoreCase( "14" ) )
            {
                String trimesterMessage = "तपाईको बच्चा १८ महिना पुगेपछि ,३ महिना स्तनपान गराउन छुटाई ,बच्चाको जाँचको लागि स्वास्थ्य संस्थामा ल्याउनु  होला.";
                try
                {
                    tempScheduledAwarenessChildComplete18MonthSMS( teiList, trimesterMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
        

        // Quarterly Messages
        if ( currentMonth.equalsIgnoreCase( "04" ) || currentMonth.equalsIgnoreCase( "08" ) || currentMonth.equalsIgnoreCase( "12" ) )
        {
            //Married Male and Female clients
            if ( currentDate.equalsIgnoreCase( "09" ) )
            {
                String quarterlyMessage = "स्वास्थ्य संस्थामा सुत्केरी गराऔ, आमा र बच्चा दुवैको स्वास्थ्य पक्का गराऔं |";
                try
                {
                    scheduledAwarenessCustomeSMS( teiList, quarterlyMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
            
            if ( currentDate.equalsIgnoreCase( "10" ) )
            {
                String quarterlyMessage = "आफ्नो  साथीको पनि जांच गराऔ, सम्बन्ध अझै बलियो बनाऔं ।";
                try
                {
                    scheduledAwarenessCustomeSMS( teiList, quarterlyMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
            
            //Female clients (20 years and above)
            if ( currentDate.equalsIgnoreCase( "11" ) )
            {
                String quarterlyMessage = "नियमित पाठेघरको जांच गराऔ ,पछि हुन सक्ने समस्यालाई अहिले नै पत्ता लगाऔं |";
                try
                {
                    tempScheduledAwarenessCustomeSMS( teiList, quarterlyMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
            
            //Married Male and Female clients
            if ( currentDate.equalsIgnoreCase( "12" ) )
            {
                String quarterlyMessage = "स्वास्थ्यकर्मीसंग सल्लाह लिई स्वस्थ बच्चा जन्माऔं .";
                try
                {
                    scheduledAwarenessCustomeSMS( teiList, quarterlyMessage );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
            
        // bi annual Messages
        if ( currentMonth.equalsIgnoreCase( "04" ) || currentMonth.equalsIgnoreCase( "10" ) )
        {
            if ( currentDate.equalsIgnoreCase( "04" ) )
            {
                List<String> biannualMessages = new ArrayList<String>();
                biannualMessages.add( "CD4 जाँच गराऔ रोगसँग लडने क्षमता कति छ भनेर बुझौ." );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, biannualMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
        
        // trimester Messages
        if ( currentMonth.equalsIgnoreCase( "02" ) || currentMonth.equalsIgnoreCase( "05" ) || currentMonth.equalsIgnoreCase( "08" )
            || currentMonth.equalsIgnoreCase( "11" ) )
        {
            if ( currentDate.equalsIgnoreCase( "02" ) )
            {
                List<String> trimesterMessages = new ArrayList<String>();
                trimesterMessages.add( "भाइरल लोड जाँच गराई शरिरमा भाइरसको अवस्था थाहा पाउनुहोस् |" );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, trimesterMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
        
        // bi annual Messages
        if ( currentMonth.equalsIgnoreCase( "05" ) || currentMonth.equalsIgnoreCase( "11" ) )
        {
            if ( currentDate.equalsIgnoreCase( "05" ) )
            {
                List<String> biannualMessages = new ArrayList<String>();
                biannualMessages.add( "भाइरल लोड जाँचले तपाईको अवस्था मात्र होइन तपाईको नजिकको साथीलाई पनि सुरक्षित राख्छ |" );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, biannualMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
 
        // bi annual Messages
        if ( currentMonth.equalsIgnoreCase( "06" ) || currentMonth.equalsIgnoreCase( "12" ) )
        {
            if ( currentDate.equalsIgnoreCase( "06" ) )
            {
                List<String> biannualMessages = new ArrayList<String>();
                biannualMessages.add( "औषधि शुरु गरेको पहिलो वर्ष २ पटक (६/६ महिनामा) र त्यसपछिको हरेक बर्षमा एक पटक मात्र भाइरल लोडको जाँच गराउनुहोला ।" );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, biannualMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }
        
        
        // Monthly Messages
        if ( currentDate.equalsIgnoreCase( "01" ) )
        {
            List<String> monthlyMessages = new ArrayList<String>();
            monthlyMessages.add( "स्वस्थ जीवनको लागि नियमित स्वास्थ्य  जांच गराउनुहोला ।" );
            try
            {
                scheduledCustomeSMS( mobileNumbers, monthlyMessages );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.out.println( "Error SMS " + e.getMessage() );
            }
        }           
        
        // Monthly Messages
        if ( currentDate.equalsIgnoreCase( "07" ) )
        {
            List<String> monthlyMessages = new ArrayList<String>();
            monthlyMessages.add( "समयको ख्याल राख्नुहोस नियमित औषधी लिन आउनुहोस् ।" );
            try
            {
                scheduledCustomeSMS( mobileNumbers, monthlyMessages );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.out.println( "Error SMS " + e.getMessage() );
            }
        }
        
        // Monthly Messages
        if ( currentDate.equalsIgnoreCase( "15" ) )
        {
            List<String> monthlyMessages = new ArrayList<String>();
            monthlyMessages.add( "हरेक दिन औषधी सेवन ,राख्छ स्वस्थ जीवन ।" );
            try
            {
                scheduledCustomeSMS( mobileNumbers, monthlyMessages );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.out.println( "Error SMS " + e.getMessage() );
            }
        }     
            
        // bimonthly Messages
        if ( currentMonth.equalsIgnoreCase( "02" ) || currentMonth.equalsIgnoreCase( "04" ) || currentMonth.equalsIgnoreCase( "06" ) )
        {
            if ( currentDate.equalsIgnoreCase( "08" ) )
            {
                List<String> bimonthlyMessages = new ArrayList<String>();
                bimonthlyMessages.add( "नियमित औषधिको सेवन, अवसरबादी समस्या रहित जीवन |" );
                try
                {
                    scheduledCustomeSMS( mobileNumbers, bimonthlyMessages );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    System.out.println( "Error SMS " + e.getMessage() );
                }
            }
        }            
        
        // for every FRIDAY
        if( dayOfWeek.toString().equalsIgnoreCase( "FRIDAY") )
        {
            List<String> fridayMessages = new ArrayList<String>();
            fridayMessages.add( "राखी राख्नुहोस  सम्झना, हरेक दिन औषधी सेवन र  गणना ।" );
            try
            {
                scheduledCustomeSMS( mobileNumbers, fridayMessages );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.out.println( "Error SMS " + e.getMessage() );
            }
        }

        System.out.println("INFO: Scheduler job has ended at : " + new Date() );

        clock.logTime( "scheduler custom SMS job completed" );
        notifier.notify( jobConfiguration, INFO, "scheduler custom SMS job completed", true );
    }

    @Override
    public ErrorReport validate()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    public void scheduledCustomeSMS( List<String> mobileNumbers, List<String> messages )
        throws IOException
    {
        System.out.println( " Monthly SMS Scheduler Started at : " + new Date() );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        if ( mobileNumbers != null && mobileNumbers.size() > 0 )
        {
            for ( String mobileNumber : mobileNumbers )
            {
                for ( String message : messages )
                {
                    bulkSMSHTTPInterface.sendSMS( message, mobileNumber );
                    System.out.println( mobileNumber + " -------- > " + message );
                }
            }
        }

        System.out.println( " Monthly SMS Scheduler End at : " + new Date() );
    }

    // Pill Pick Up and TB Screening

    public void scheduledCustomePillPickUPANDTbScreeingSMS( Integer art_foll_up_stage_id, Integer sms_consent_attri_id,
        Integer mobile_attribute_id )
        throws IOException
    {
        System.out.println( " Pill Pick Up SMS Scheduler Started at : " + new Date() );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( mobile_attribute_id );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + art_foll_up_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + sms_consent_attri_id + " and teav.value = 'true' ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );

                if ( teiID != null && orgUnitID != null && dueDate != null )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // three day before
                    Calendar threeDayBefore = Calendar.getInstance();
                    threeDayBefore.setTime( dueDateObject );
                    threeDayBefore.add( Calendar.DATE, -3 );
                    Date threeDayBeforeDate = threeDayBefore.getTime();

                    // 2 day after
                    Calendar twoDayAfter = Calendar.getInstance();
                    twoDayAfter.setTime( dueDateObject );
                    twoDayAfter.add( Calendar.DATE, 2 );
                    Date twoDayAfterDate = twoDayAfter.getTime();

                    String threeDayBeforeDateString = simpleDateFormat.format( threeDayBeforeDate );
                    String twoDayAfterDateString = simpleDateFormat.format( twoDayAfterDate );

                    if ( todayDate.equalsIgnoreCase( threeDayBeforeDateString )
                        || todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                    {
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        if ( teaValue != null )
                        {
                            System.out.println( tei.getId() + " -- " + teaValue.getValue() + " -- " + orgUnit.getName() );
                            if ( teaValue.getValue() != null && teaValue.getValue().length() == 10 )
                            {
                                String customMessagePillPick = "";
                                String customMessageTBScreening = "";
                                if( todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                                {
                                    customMessagePillPick = "तपाइको औषधि लिने बेला भयो,तपाई  " + orgUnit.getName() + "  को केन्द्रमा आउनुहोला |";
                                    
                                    customMessageTBScreening = "क्षयरोग (टि. बी) को जोखिमबाट बच्न   " + orgUnit.getName() + " मा क्षयरोग (टि. बी) को जांच गर्न आउनुहोला | ";
                                }
                                else
                                {
                                    customMessagePillPick = "तपाइको औषधि लिने बेला भयो,तपाई  मिति  " + dueDate  + " गते  " + orgUnit.getName() + " को केन्द्रमा आउनुहोला  |";
                                    
                                    customMessageTBScreening = "क्षयरोग (टि. बी) को जोखिमबाट बच्न मिति  " + dueDate  + " गते " + orgUnit.getName() + " मा क्षयरोग (टि. बी) को जांच गर्न आउनुहोला | ";
                                }
                                bulkSMSHTTPInterface.sendSMS( customMessagePillPick, teaValue.getValue() );
                                bulkSMSHTTPInterface.sendSMS( customMessageTBScreening, teaValue.getValue() );
                                
                                System.out.println( teaValue.getValue() + " -------- > " + customMessagePillPick
                                    + " -------- >" + customMessageTBScreening );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " Pill Pick Up SMS Scheduler End at : " + new Date() );
    }

    // CD4 Count

    public void scheduledCustomeCD4CountSMS( Integer medical_history_stage_id, Integer sms_consent_attri_id,
        Integer mobile_attribute_id, Integer cd4_test_de_id )
        throws IOException
    {
        System.out.println( " CD4 Count SMS Scheduler Started at : " + new Date() );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( mobile_attribute_id );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            /*
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid,tedv.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentitydatavalue tedv  ON  tedv.programstageinstanceid = psi.programstageinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + medical_history_stage_id
                + " and tedv.dataelementid = "
                + cd4_test_de_id
                + " and  "
                + "teav.trackedentityattributeid =  "
                + sms_consent_attri_id
                + " and teav.value = 'true' ";
            */
            
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, "
                + "cast(data.value::json ->> 'value' AS VARCHAR) AS de_value FROM programstageinstance psi "
                + "JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE  "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "INNER JOIN dataelement de ON de.uid = data.key "
                + "WHERE psi.programstageid = " + medical_history_stage_id + " AND de.dataelementid = " + cd4_test_de_id
                + " AND teav.trackedentityattributeid =  " + sms_consent_attri_id + " AND teav.value = 'true' ";           
            
            System.out.println( "query = " + query );

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String cd4TestDate = rs.getString( 3 );

                if ( teiID != null && orgUnitID != null && cd4TestDate != null )
                {
                    Date cd4TestDateObject = simpleDateFormat.parse( cd4TestDate );
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime( cd4TestDateObject );
                    calendar.set( Calendar.MONTH, (calendar.get( Calendar.MONTH ) + 6) );
                    Date sixMonthAfterDate = calendar.getTime();

                    String scheduleSMSDateString = simpleDateFormat.format( sixMonthAfterDate );

                    if ( todayDate.equalsIgnoreCase( scheduleSMSDateString ) )
                    {
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        if ( teaValue != null )
                        {
                            System.out.println( tei.getId() + " -- " + teaValue.getValue() + " -- " + orgUnit.getName() );
                            if ( teaValue.getValue() != null && teaValue.getValue().length() == 10 )
                            {
                                String customMessage = "६ महिना भयो, CD4 गर्न मिति    "
                                    + simpleDateFormat.format( sixMonthAfterDate ) + " मा  " + orgUnit.getName() + " को केन्द्रमा आउनुहोला |";
                                bulkSMSHTTPInterface.sendSMS( customMessage, teaValue.getValue() );
                                System.out.println( teaValue.getValue() + " -------- > " + customMessage );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute/dataelement id", e );
        }

        System.out.println( " CD4 Count SMS Scheduler End at : " + new Date() );
    }

    // CD4Count ART follow up

    public void scheduledCustomeCD4CountAndViralLoadARTSMS( List<TrackedEntityInstance> teiList )
        throws IOException
    {
        System.out.println( " CD4Count ART follow and Viral Load SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                        .getTrackedEntityAttributeValue( tei, teAttribute );
                    if ( teaValue != null && teaValue.getValue() != null && teaValue.getValue().length() == 10 )
                    {
                        String orgUnitAndCD4CountValue = getLatestEventOrgAndDataValue( ART_FOLLOW_UP_PROGRAM_STAGE_ID,
                            CD4_TEST_FOLLOW_UP_DATAELEMENT_ID, tei.getId() );
                        String orgUnitAndViralLoadValue = getLatestEventOrgAndDataValue(
                            ART_FOLLOW_UP_PROGRAM_STAGE_ID, VIRAL_LOAD_FOLLOW_UP_DATAELEMENT_ID, tei.getId() );

                        if ( orgUnitAndCD4CountValue != null && !orgUnitAndCD4CountValue.equalsIgnoreCase( "" ) )
                        {
                            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer
                                .parseInt( orgUnitAndCD4CountValue.split( ":" )[0] ) );

                            String cd4TestDate = orgUnitAndCD4CountValue.split( ":" )[1];

                            Date cd4TestDateObject = simpleDateFormat.parse( cd4TestDate );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( cd4TestDateObject );
                            calendar.set( Calendar.MONTH, (calendar.get( Calendar.MONTH ) + 6) );
                            Date sixMonthAfterDate = calendar.getTime();

                            // three day before sixMonthAfterDate
                            Calendar threeDayBefore = Calendar.getInstance();
                            threeDayBefore.setTime( sixMonthAfterDate );
                            threeDayBefore.add( Calendar.DATE, -3 );
                            Date threeDayBeforeDate = threeDayBefore.getTime();

                            // 2 day after sixMonthAfterDate
                            Calendar twoDayAfter = Calendar.getInstance();
                            twoDayAfter.setTime( sixMonthAfterDate );
                            twoDayAfter.add( Calendar.DATE, -2 );
                            Date twoDayAfterDate = twoDayAfter.getTime();

                            String threeDayBeforeDateString = simpleDateFormat.format( threeDayBeforeDate );
                            String twoDayAfterDateString = simpleDateFormat.format( twoDayAfterDate );

                            if ( todayDate.equalsIgnoreCase( threeDayBeforeDateString )
                                || todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                            {
                                String customMessage = "";
                                
                                if( todayDate.equalsIgnoreCase( twoDayAfterDateString ))
                                {
                                    customMessage = "भाइरल लोड जाचँ को लागि  " + orgUnit.getName()  + " को  केन्द्रमा आउनुहोला |";
                                }
                                else
                                {
                                    customMessage = "भाइरल लोड जाचँ को लागि मिति   " + simpleDateFormat.format( sixMonthAfterDate ) + " गते " + orgUnit.getName()
                                        + " को  केन्द्रमा आउनुहोला |";
                                }
                                
                                bulkSMSHTTPInterface.sendSMS( customMessage, teaValue.getValue() );
                                System.out.println( teaValue.getValue() + " -------- > " + customMessage );
                            }
                        }

                        if ( orgUnitAndViralLoadValue != null && !orgUnitAndViralLoadValue.equalsIgnoreCase( "" ) )
                        {
                            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer
                                .parseInt( orgUnitAndViralLoadValue.split( ":" )[0] ) );

                            String viralLoadDate = orgUnitAndViralLoadValue.split( ":" )[1];

                            Date viralLoadDateObject = simpleDateFormat.parse( viralLoadDate );
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( viralLoadDateObject );
                            calendar.set( Calendar.MONTH, (calendar.get( Calendar.MONTH ) + 6) );
                            Date sixMonthAfterDate = calendar.getTime();

                            // three day before sixMonthAfterDate
                            Calendar threeDayBefore = Calendar.getInstance();
                            threeDayBefore.setTime( sixMonthAfterDate );
                            threeDayBefore.add( Calendar.DATE, -1 );
                            Date threeDayBeforeDate = threeDayBefore.getTime();

                            // 2 day after sixMonthAfterDate
                            Calendar twoDayAfter = Calendar.getInstance();
                            twoDayAfter.setTime( sixMonthAfterDate );
                            twoDayAfter.add( Calendar.DATE, -2 );
                            Date twoDayAfterDate = twoDayAfter.getTime();

                            String threeDayBeforeDateString = simpleDateFormat.format( threeDayBeforeDate );
                            String twoDayAfterDateString = simpleDateFormat.format( twoDayAfterDate );

                            if ( todayDate.equalsIgnoreCase( threeDayBeforeDateString )
                                || todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                            {
                                String customMessage = "";
                                
                                if( todayDate.equalsIgnoreCase( twoDayAfterDateString ))
                                {
                                    customMessage = "भाइरल लोड जाचँ को लागि   " + orgUnit.getName() + " को  केन्द्रमा आउनुहोला |";
                                }
                                else
                                {
                                    customMessage = "भाइरल लोड जाचँ को लागि मिति "   + simpleDateFormat.format( sixMonthAfterDate ) + " गते  " + orgUnit.getName()
                                        + " को  केन्द्रमा आउनुहोला |";
                                }       
                                
                                bulkSMSHTTPInterface.sendSMS( customMessage, teaValue.getValue() );
                                System.out.println( teaValue.getValue() + " -------- > " + customMessage );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "CD4Count ART follow and Viral Load SMS Exception -- ", e );
        }

        System.out.println( " CD4Count ART follow and Viral Load SMS Scheduler End at : " + new Date() );
    }

    // Awareness For Child Complete 18 Month

    public void scheduledAwarenessChildComplete18MonthSMS( List<TrackedEntityInstance> teiList )
        throws IOException
    {
        System.out.println( " Awareness For Child Complete 18 Month SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                        .getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    if ( teaValue != null && teaValue.getValue() != null && teaValue.getValue().length() == 10 )
                    {
                        String orgUnitAndEdDateValue = getLatestEventOrgAndDataValue(
                            EID_AND_TREATMENT_PROGRAM_STAGE_ID, DATE_OF_BIRTH_INFANT_DATAELEMENT_ID, tei.getId() );

                        if ( orgUnitAndEdDateValue != null && !orgUnitAndEdDateValue.equalsIgnoreCase( "" ) )
                        {
                            //OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitAndEdDateValue.split( ":" )[0] ) );

                            String infantDateOfBirth = orgUnitAndEdDateValue.split( ":" )[1];
                            Date infantDateOfBirthObject = simpleDateFormat.parse( infantDateOfBirth );

                            // 18 month after from infant Date Of Birth
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( infantDateOfBirthObject );
                            calendar.set( Calendar.MONTH, (calendar.get( Calendar.MONTH ) + 18) );
                            Date eighteenMonthAfterDate = calendar.getTime();

                            // three day before eighteenMonthBeforeDate
                            Calendar threeDayBefore = Calendar.getInstance();
                            threeDayBefore.setTime( eighteenMonthAfterDate );
                            threeDayBefore.add( Calendar.DATE, -3 );
                            Date threeDayBeforeDate = threeDayBefore.getTime();

                            // 2 day after eighteenMonthBeforeDate
                            Calendar twoDayAfter = Calendar.getInstance();
                            twoDayAfter.setTime( eighteenMonthAfterDate );
                            twoDayAfter.add( Calendar.DATE, -2 );
                            Date twoDayAfterDate = twoDayAfter.getTime();

                            String threeDayBeforeDateString = simpleDateFormat.format( threeDayBeforeDate );
                            String twoDayAfterDateString = simpleDateFormat.format( twoDayAfterDate );

                            // one day before
                            
                            if ( todayDate.equalsIgnoreCase( threeDayBeforeDateString )
                                || todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                            {
                                String customMessage = "तपाईको बच्चा १८  महिना पुगेपछि ,३ महिना स्तनपान गराउन छुटाई ,बच्चाको जाँचको लागि स्वास्थ्य संस्थामा ल्याउनु  होला |";
                                bulkSMSHTTPInterface.sendSMS( customMessage, teaValue.getValue() );
                                System.out.println( " inside schedule SMS " + teaValue.getValue() + " -------- > " + customMessage );
                            }
                        }

                    }
                }
            }

        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Awareness For Child Complete 18 Month SMS Exception -- ", e );
        }

        System.out.println( " Awareness For Child Complete 18 Month SMS Scheduler End at : " + new Date() );
    }

    // EID appointment reminders after 4week of delivery/dob of child
    public void scheduledEIDAfter4WeekOfDevliverySMS( List<TrackedEntityInstance> teiList )
        throws IOException
    {
        System.out.println( " EID After 4Week Of Devlivery SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                        .getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    if ( teaValue != null && teaValue.getValue() != null && teaValue.getValue().length() == 10 )
                    {
                        String orgUnitAndEdDateValue = getLatestEventOrgAndDataValue(
                            EID_AND_TREATMENT_PROGRAM_STAGE_ID, DATE_OF_BIRTH_INFANT_DATAELEMENT_ID, tei.getId() );

                        if ( orgUnitAndEdDateValue != null && !orgUnitAndEdDateValue.equalsIgnoreCase( "" ) )
                        {
                            OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer
                                .parseInt( orgUnitAndEdDateValue.split( ":" )[0] ) );

                            String infantDateOfBirth = orgUnitAndEdDateValue.split( ":" )[1];
                            Date infantDateOfBirthObject = simpleDateFormat.parse( infantDateOfBirth );

                            // 4week after from infant Date Of Birth
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime( infantDateOfBirthObject );
                            calendar.add( Calendar.WEEK_OF_YEAR, 4 );
                            Date fourWeekAfterDate = calendar.getTime();

                            // three day before fourWeekAfterDate
                            Calendar threeDayBefore = Calendar.getInstance();
                            threeDayBefore.setTime( fourWeekAfterDate );
                            threeDayBefore.add( Calendar.DATE, -1 );
                            Date threeDayBeforeDate = threeDayBefore.getTime();

                            // 2 day after fourWeekAfterDate
                            Calendar twoDayAfter = Calendar.getInstance();
                            twoDayAfter.setTime( fourWeekAfterDate );
                            twoDayAfter.add( Calendar.DATE, -2 );
                            Date twoDayAfterDate = twoDayAfter.getTime();

                            String threeDayBeforeDateString = simpleDateFormat.format( threeDayBeforeDate );
                            String twoDayAfterDateString = simpleDateFormat.format( twoDayAfterDate );

                            // one day before
                            if ( todayDate.equalsIgnoreCase( threeDayBeforeDateString )
                                || todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                            {
                                String customMessage = "";
                                if( todayDate.equalsIgnoreCase( twoDayAfterDateString ) )
                                {
                                    customMessage = "तपाईको बच्चा जन्मेको ६ हफ्ता भित्रमा जाच  गराउन  "  + orgUnit.getName() + " मा   ल्याउनु होला |";
                                }
                                else
                                {
                                    customMessage = "तपाईको बच्चा जन्मेको ६ हफ्ता भित्रमा जाच  गराउन  " + orgUnit.getName() + " मा " + simpleDateFormat.format( fourWeekAfterDate )
                                        + " गते  ल्याउनु होला |";
                                }
                                
                                bulkSMSHTTPInterface.sendSMS( customMessage, teaValue.getValue() );
                                System.out.println( " inside schedule SMS " + teaValue.getValue() + " -------- > "
                                    + customMessage );
                            }
                        }

                    }
                }
            }

        }
        catch ( Exception e )
        {
            throw new RuntimeException( " EID After 4Week Of Devlivery SMS Exception -- ", e );
        }

        System.out.println( " EID After 4Week Of Devlivery SMS Scheduler End at : " + new Date() );
    }

    // PMTCT And EID Female PLHIV (15- 49 years) Positive pregnant women

    public void scheduledPMTCTEIDSMS( List<TrackedEntityInstance> teiList ) throws IOException
    {
        System.out.println( "PMTCT And EID Female PLHIV (15- 49 years) Positive pregnant women SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        TrackedEntityAttribute teSexAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( SEX_ATTRIBUTE_ID );
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( currentDate.equalsIgnoreCase( "01" ) )
            {
                if ( teiList != null && teiList.size() > 0 )
                {
                    for ( TrackedEntityInstance tei : teiList )
                    {
                        String orgUnitAndTestResultValue = getLatestEventOrgAndDataValue( EID_AND_TREATMENT_PROGRAM_STAGE_ID, DNA_PCR_TEST_RESULT_DATAELEMENT_ID, tei.getId() );
                        
                        if ( orgUnitAndTestResultValue != null && !orgUnitAndTestResultValue.equalsIgnoreCase( "" ))
                        {
                            System.out.println( " inside schedule SMS Not Send " + tei.getId() + " -------- > "   );
                        }
                        else
                        {
                            TrackedEntityAttributeValue teaValueMobileNo = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                            TrackedEntityAttributeValue teaSex = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teSexAttribute );
                            if ( teaValueMobileNo != null && teaValueMobileNo.getValue() != null  && teaValueMobileNo.getValue().length() == 10 )
                            {
                                if ( teaSex != null && teaSex.getValue() != null && teaSex.getValue().equalsIgnoreCase( "Female" ) )
                                {
                                    String orgUnitAndAgeAtVisit = getLatestEventOrgAndDataValue( ART_FOLLOW_UP_PROGRAM_STAGE_ID, AGE_AT_VISIT_DATAELEMENT_ID, tei.getId() );
                                    
                                    if ( orgUnitAndAgeAtVisit != null && !orgUnitAndAgeAtVisit.equalsIgnoreCase( "" ) )
                                    {
                                        String ageAtVisit = orgUnitAndAgeAtVisit.split( ":" )[1];
                                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( Integer.parseInt( orgUnitAndAgeAtVisit.split( ":" )[0] ) );
                                        if ( Integer.parseInt( ageAtVisit ) >= 15 && Integer.parseInt( ageAtVisit ) <= 49 )
                                        {
                                            String mesage = "जन्मने बित्तिकै " +  orgUnit.getName() + " मा बच्चाको रगत जांच गराऔं  ,बच्चालाई स्वस्थ बनाऔं |";
                                            bulkSMSHTTPInterface.sendSMS( mesage, teaValueMobileNo.getValue() );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "PMTCT And EID Female PLHIV (15- 49 years) Positive pregnant women SMS Exception -- ", e );
        }

        System.out.println( " PMTCT And EID Female PLHIV (15- 49 years) Positive pregnant women SMS Scheduler End at : " + new Date() );
    }    
    
    
    // scheduledAwarenessCustomeSMS april/aug/dec 9th/10th/12th
    public void scheduledAwarenessCustomeSMS( List<TrackedEntityInstance> teiList, String message )
        throws IOException
    {
        System.out.println( " 1  Awareness Custome SMS Scheduler Started at : " + new Date() );
        
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        TrackedEntityAttribute teSexAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( SEX_ATTRIBUTE_ID );
        TrackedEntityAttribute maritalStatusAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MARITAL_STATUS_ATTRIBUTE_ID );
        
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue teaValueMobileNo = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    TrackedEntityAttributeValue teaSex = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teSexAttribute );
                    TrackedEntityAttributeValue maritalStatus = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, maritalStatusAttribute );
                    
                    if ( teaValueMobileNo != null && teaValueMobileNo.getValue() != null  && teaValueMobileNo.getValue().length() == 10 )
                    {
                        if ( teaSex != null && teaSex.getValue() != null && ( teaSex.getValue().equalsIgnoreCase( "Female" ) || teaSex.getValue().equalsIgnoreCase( "Male" ) ) )
                        {
                            if ( maritalStatus != null && maritalStatus.getValue() != null && maritalStatus.getValue().equalsIgnoreCase( "married" ) )
                            {
                                bulkSMSHTTPInterface.sendSMS( message, teaValueMobileNo.getValue() );
                                System.out.println( teaValueMobileNo.getValue() + " -------- > " + message );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( " 1 Awareness Custome SMS Scheduler Exception -- ", e );
        }

        System.out.println( " 1 Awareness Custome SMS Scheduler End at : " + new Date() );
    }    
    
    // scheduledAwarenessCustomeSMS april/aug/dec 11th
    public void tempScheduledAwarenessCustomeSMS( List<TrackedEntityInstance> teiList, String message )
        throws IOException
    {
        System.out.println( " 2 Awareness Custome SMS Scheduler Started at : " + new Date() );
        
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        TrackedEntityAttribute teSexAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( SEX_ATTRIBUTE_ID );
        TrackedEntityAttribute ageAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( AGE_ATTRIBUTE_ID );
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue teaValueMobileNo = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    TrackedEntityAttributeValue teaSex = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, teSexAttribute );
                    TrackedEntityAttributeValue age = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, ageAttribute );
                    
                    if ( teaValueMobileNo != null && teaValueMobileNo.getValue() != null  && teaValueMobileNo.getValue().length() == 10 )
                    {
                        if ( teaSex != null && teaSex.getValue() != null && teaSex.getValue().equalsIgnoreCase( "Female" ) )
                        {
                            if ( age != null && age.getValue() != null && Integer.parseInt( age.getValue() ) >= 20 )
                            {
                                
                                bulkSMSHTTPInterface.sendSMS( message, teaValueMobileNo.getValue() );
                                System.out.println( teaValueMobileNo.getValue() + " -------- > " + message );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( " 2 Awareness Custome SMS Scheduler Exception -- ", e );
        }

        System.out.println( " 2 Awareness Custome SMS Scheduler End at : " + new Date() );
    }    
        
    // scheduledAwarenessChildComplete18MonthSMS 03/06/09/12 14
    public void tempScheduledAwarenessChildComplete18MonthSMS( List<TrackedEntityInstance> teiList, String message )
        throws IOException
    {
        System.out.println( " Awareness message for rapid test which is to be sent before child completes 18 months SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue mobileNumber = trackedEntityAttributeValueService
                        .getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    if ( mobileNumber != null && mobileNumber.getValue() != null && mobileNumber.getValue().length() == 10 )
                    {
                        String statusOfInfantValue = getLatestEventOrgAndDataValue( PREGNANCY_DELIVERY_PROGRAM_STAGE_ID, STATUS_OF_INFANT_DATAELEMENT_ID, tei.getId() );
                        
                        String statusOfInfant = statusOfInfantValue.split( ":" )[1];
                        
                        if( statusOfInfant.equalsIgnoreCase( "alive" ))
                        {
                            String orgUnitAndEdDateValue = getLatestEventOrgAndDataValue( EID_AND_TREATMENT_PROGRAM_STAGE_ID, AGE_OF_INFANT_DATAELEMENT_ID, tei.getId() );

                            if ( orgUnitAndEdDateValue != null && !orgUnitAndEdDateValue.equalsIgnoreCase( "" ) )
                            {
                                String infantage = orgUnitAndEdDateValue.split( ":" )[1];
                                
                                if ( infantage != null  && Integer.parseInt( infantage ) < 18 )
                                {
                                    bulkSMSHTTPInterface.sendSMS( message, mobileNumber.getValue() );
                                    System.out.println( mobileNumber.getValue() + " -------- > " + message );
                                }
                            }
                        }
                        
                    }
                }
            }

        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Awareness message for rapid test which is to be sent before child completes 18 months. SMS Exception -- ", e );
        }

        System.out.println( " Awareness message for rapid test which is to be sent before child completes 18 months. SMS Scheduler End at : " + new Date() );
    }    
    
    // scheduledAwarenessChildComplete18MonthSMS 03/06/09/12 13
    public void tempScheduledAwarenessEIDSMS( List<TrackedEntityInstance> teiList, String message )
        throws IOException
    {
        System.out.println( " Awareness message for EID which is to be sent before Expected Delivery Date mentioned in Pregnancy and Delivery Detail stage. SMS Scheduler Started at : " + new Date() );
        TrackedEntityAttribute teMobileNoAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( MOBILE_NUMBER_ATTRIBUTE_ID );
        
        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            if ( teiList != null && teiList.size() > 0 )
            {
                for ( TrackedEntityInstance tei : teiList )
                {
                    TrackedEntityAttributeValue mobileNumber = trackedEntityAttributeValueService
                        .getTrackedEntityAttributeValue( tei, teMobileNoAttribute );
                    if ( mobileNumber != null && mobileNumber.getValue() != null && mobileNumber.getValue().length() == 10 )
                    {
                        String eddValue = getLatestEventOrgAndDataValue( PREGNANCY_DELIVERY_PROGRAM_STAGE_ID, EDD_DATAELEMENT_ID, tei.getId() );
                        
                        String estimatedDeliveryDate  = eddValue.split( ":" )[1];
                        
                        Date estimatedDeliveryDateObject = simpleDateFormat.parse( estimatedDeliveryDate );
                        
                        if( estimatedDeliveryDateObject.compareTo( new Date() ) >= 0 )
                        {
                            bulkSMSHTTPInterface.sendSMS( message, mobileNumber.getValue() );
                            System.out.println( mobileNumber.getValue() + " -------- > " + message );
                        }
                    }
                }
            }

        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Awareness message for EID which is to be sent before Expected Delivery Date mentioned in Pregnancy and Delivery Detail stage. SMS Exception -- ", e );
        }

        System.out.println( "Awareness message for EID which is to be sent before Expected Delivery Date mentioned in Pregnancy and Delivery Detail stage. SMS Scheduler End at : " + new Date() );
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
        List<String> mobileNumbers = new ArrayList<String>();

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
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<String> getTempTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds(
        Integer attributeId, String trackedEntityInstanceIdsByComma )
    {
        List<String> mobileNumbers = new ArrayList<String>();

        try
        {
            String query = "SELECT teav.value FROM trackedentityattributevalue teav "
                + " INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = teav.trackedentityinstanceid "
                + " WHERE teav.trackedentityattributeid =  " + attributeId + " AND teav.trackedentityinstanceid in ( " + trackedEntityInstanceIdsByComma + ") "
                    + "AND tei.created::date >= '" + todayDate + "' ";

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
    //public List<TrackedEntityInstance> getTrackedEntityInstancesByAttributeId( Integer attributeId )
    public List<TrackedEntityInstance> getTrackedEntityInstancesByAttributeId()
    {
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>();

        try
        {
                /*
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                + "WHERE value = 'true' AND trackedentityattributeid =  " + attributeId
                + " order by trackedentityinstanceid ASC ";
                        */      
            
            String query = "SELECT teav1.trackedentityinstanceid, teav1.value FROM trackedentityattributevalue teav1 "
                           + " INNER JOIN ( SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                           + " WHERE trackedentityattributeid = " + IMP_AGENCY_ATTRIBUTE_ID + " AND value ILIKE '" + IMP_AGENCY_ATTRIBUTE_VALUE + "' ) teav2 "
                           + " ON teav1.trackedentityinstanceid = teav2.trackedentityinstanceid " 
                           + " INNER JOIN ( SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                           + " WHERE trackedentityattributeid =  " + SMS_CONSENT_ATTRIBUTE_ID + " AND value = 'true' ) teav3 "
                           + " ON teav2.trackedentityinstanceid = teav3.trackedentityinstanceid "
                           + " WHERE teav1.trackedentityattributeid =  " + MOBILE_NUMBER_ATTRIBUTE_ID;
            
            
            
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
    // Get Tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<String> getTrackedEntityInstanceAttributeValue( )
    {
        List<String> mobileNumbers = new ArrayList<String>();

        try
        {
                String query = "SELECT teav1.trackedentityinstanceid, teav1.value FROM trackedentityattributevalue teav1 "
                    + " INNER JOIN ( SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                    + " WHERE trackedentityattributeid = " + IMP_AGENCY_ATTRIBUTE_ID + " AND value ILIKE '" + IMP_AGENCY_ATTRIBUTE_VALUE + "' ) teav2 "
                    + " ON teav1.trackedentityinstanceid = teav2.trackedentityinstanceid " 
                    + " INNER JOIN ( SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                    + " WHERE trackedentityattributeid =  " + SMS_CONSENT_ATTRIBUTE_ID + " AND value = 'true' ) teav3 "
                    + " ON teav2.trackedentityinstanceid = teav3.trackedentityinstanceid "
                    + " WHERE teav1.trackedentityattributeid =  " + MOBILE_NUMBER_ATTRIBUTE_ID;
     
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String mobileNo = rs.getString( 2 );
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
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public String getLatestEventOrgAndDataValue( Integer psId, Integer dataElementId, long teiId )
    {
        String orgUnitIdAndValue = "";
        List<String> tempResult = new ArrayList<String>();
        try
        {
            /*
            String query = "SELECT psi.organisationunitid, tedv.dataelementid,tedv.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentitydatavalue tedv  ON  tedv.programstageinstanceid = psi.programstageinstanceid "
                + "WHERE psi.programstageid = "
                + psId
                + " AND tedv.dataelementid = "
                + dataElementId
                + "  AND pi.trackedentityinstanceid =  " + teiId + " order by psi.lastupdated desc ";
            */
            
            
            String query = "SELECT psi.organisationunitid, de.dataelementid, "
                + "cast(data.value::json ->> 'value' AS VARCHAR) AS de_value FROM programstageinstance psi "
                + "JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN dataelement de ON de.uid = data.key "
                + "WHERE psi.programstageid = " + psId + " AND de.dataelementid = " + dataElementId + " AND "
                + "pi.trackedentityinstanceid =  " + teiId + " order by psi.lastupdated desc ";
            
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

            //System.out.println( " orgUnitIdAndValue : " + orgUnitIdAndValue );
            return orgUnitIdAndValue;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    //
    @SuppressWarnings( "unused" )
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
