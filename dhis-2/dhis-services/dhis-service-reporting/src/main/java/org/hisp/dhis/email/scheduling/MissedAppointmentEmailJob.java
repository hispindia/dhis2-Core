package org.hisp.dhis.email.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.message.EmailMessageSender;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstanceService;
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

@Component( "missedAppointmentEmailJob" )
public class MissedAppointmentEmailJob implements Job
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private final Notifier notifier;
    
    private final ProgramService programService;
    
    private OrganisationUnitService organisationUnitService;

    private TrackedEntityInstanceService trackedEntityInstanceService;
    
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;
    
    private TrackedEntityAttributeService trackedEntityAttributeService;
    
    private ProgramStageInstanceService programStageInstanceService;
    
    private JdbcTemplate jdbcTemplate;
    
    private EmailMessageSender emailMessageSender;
        
    public MissedAppointmentEmailJob( Notifier notifier, ProgramService programService, OrganisationUnitService organisationUnitService,
        TrackedEntityInstanceService trackedEntityInstanceService,TrackedEntityAttributeValueService trackedEntityAttributeValueService,
        TrackedEntityAttributeService trackedEntityAttributeService, ProgramStageInstanceService programStageInstanceService, JdbcTemplate 
        jdbcTemplate, EmailMessageSender emailMessageSender)
    {

        checkNotNull( notifier );
        checkNotNull( programService );
        checkNotNull( organisationUnitService );
        checkNotNull( trackedEntityInstanceService );
        checkNotNull( trackedEntityAttributeValueService );
        checkNotNull( trackedEntityAttributeService );
        checkNotNull( programStageInstanceService );
        checkNotNull( jdbcTemplate );
        checkNotNull( emailMessageSender );
        
        this.notifier = notifier;
        this.programService = programService;
        this.organisationUnitService = organisationUnitService;
        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.trackedEntityAttributeValueService = trackedEntityAttributeValueService;
        this.trackedEntityAttributeService = trackedEntityAttributeService;
        this.programStageInstanceService = programStageInstanceService;
        this.jdbcTemplate = jdbcTemplate;
        this.emailMessageSender = emailMessageSender;
    }
    
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.SEND_SCHEDULED_EMAIL_MISS_APPOINTMENT;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration, JobProgress progress )
    {
        Clock clock = new Clock().startClock();

        clock.logTime( "Starting to send Email to miss appointment" );
        notifier.notify( jobConfiguration, INFO, "send Email to miss appointment", true );

        sendEmail();

        clock.logTime( "send Email to miss appointment completed" );
        notifier.notify( jobConfiguration, INFO, "send Email to miss appointment completed", true );
    }

    @Override
    public ErrorReport validate()
    {
        if ( !emailMessageSender.isConfigured() )
        {
            return new ErrorReport( MissedAppointmentEmailJob.class, ErrorCode.E7010,
                "Email configuration does not exist" );
        }

        return Job.super.validate();
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void sendEmail()
    {
        Program program = programService.getAllPrograms().get( 0 );
        Set<OrganisationUnit> programOrgList = new HashSet<OrganisationUnit>();
        
        if( program != null )
        {
            programOrgList = new HashSet<OrganisationUnit>( getProgramOrganisationUnitList() );
        }
        
        if( programOrgList != null && programOrgList.size() > 0 )
        {
            for( OrganisationUnit organisationUnit : programOrgList)
            {
                Set<String> recipients = new HashSet<>();
                Set<TrackedEntityInstance> trackedEntityInstanceList = new HashSet<TrackedEntityInstance>();
                if( organisationUnit.getEmail() != null )
                {
                    recipients.add( organisationUnit.getEmail() );
                    /*
                    String teiDashboard = "https://swasthyakawach.in/pccds/dhis-web-tracker-capture/index.html#/dashboard?org=" + organisationUnit.getName() + "&program=" + program.getName() + "&ou=" + organisationUnit.getName();
                    
                    String emailText = "Hello, " + '\n' + "A new case has been reported from " + organisationUnit.getName()+ " on the " + organisationUnit.getName() + " ."  + '\n' + '\n' +" Please click on the below link to review the case and take appropriate action."
                                        + '\n' + '\n' + teiDashboard ;
                    
                    
                    */
                    
                    String emailSubject = "PrEP_Miss-Appointment Client list of " + organisationUnit.getName();
                    
                    
                    String emailText= "Dear PrEP provider, " + '\n' + '\n' + 
                        "    DHIS2 Tracker system is sending you a list of clients who missed the appointment>=7 days after their last scheduled date for your necessary action. " + '\n' + '\n' + 
                        
                        "<html><body><table width='100%' border='1' align='left'>"
                               + "<tr align='left' >"
                               /*+ "<td style='background-color:blue;color:white' ><b>TEI-UID-- count<b></td>"*/
                               + "<td style='background-color:blue;color:white' ><b>Client's ID<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Client's PrEP ID<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Client's Sex<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>State/Region<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Township<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Facility <b></td>"
                               + "<td style='background-color:blue;color:white' ><b>PrEP Initiation Date<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Last Visit Date at PrEP clinic<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Last PrEP ARV taken<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Last Next schedule Date<b></td>"
                               + "<td style='background-color:blue;color:white' ><b>Days of missed appointment since last schedule date<b></td>"
                               + "</tr>";
                    
                    trackedEntityInstanceList = new HashSet<TrackedEntityInstance>( getTrackedEntityInstanceList( organisationUnit.getUid() ));
                    
                    if( trackedEntityInstanceList != null && trackedEntityInstanceList.size() > 0 )
                    {
                        int i = 1;
                        int count = 1;
                        Set<String> missAppointmentClientList = new HashSet<String>();
                        for( TrackedEntityInstance trackedEntityInstance : trackedEntityInstanceList )
                        {
                            //System.out.println( i + " -- " + trackedEntityInstance.getUid() + " -- " + getTraceOutcome( trackedEntityInstance.getUid() ));
                            
                            if( !getTraceOutcome( trackedEntityInstance.getUid() ) )
                            {
                                missAppointmentClientList = new HashSet<String>( getMissAppointmentTEIList( trackedEntityInstance.getUid(), organisationUnit.getUid(), "wmKHppc1gL7", "QB79pRV2LqV" ) );
                                
                                if( missAppointmentClientList != null && missAppointmentClientList.size() > 0 )
                                {
                                    
                                    for( String missAppointmentClientDetails : missAppointmentClientList )
                                    {
                                        //System.out.println( " -- missAppointmentClientDetails " + missAppointmentClientDetails );
                                        //String teiUID = missAppointmentClientDetails.split( ":" )[0];
                                        String eventUID = missAppointmentClientDetails.split( ":" )[1];
                                        String orgUID = missAppointmentClientDetails.split( ":" )[2];
                                        String orgName = missAppointmentClientDetails.split( ":" )[3];
                                        String event_date = missAppointmentClientDetails.split( ":" )[4];
                                        String last_next_schedule_date = missAppointmentClientDetails.split( ":" )[5];
                                        String dayDiffrence = missAppointmentClientDetails.split( ":" )[6];
                                        
                                        String clientPrEPID = "";
                                        TrackedEntityAttribute teAttribute = trackedEntityAttributeService.getTrackedEntityAttribute( "n2gG7cdigPc" );
                                        //TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiUID );
                                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( trackedEntityInstance, teAttribute );
                                        if( teaValue != null && teaValue.getValue() != null )
                                        {
                                            clientPrEPID = teaValue.getValue();
                                        }
                                        
                                        String client_ID = "";
                                        TrackedEntityAttribute teAttribute_client_ID = trackedEntityAttributeService.getTrackedEntityAttribute( "P3Spi0kT92n" );
                                        
                                        TrackedEntityAttributeValue teaValue_client_ID = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( trackedEntityInstance, teAttribute_client_ID );
                                        if( teaValue_client_ID != null && teaValue_client_ID.getValue() != null )
                                        {
                                            client_ID = teaValue_client_ID.getValue();
                                        }
                                        
                                        String  prEPScreeningStageEventUid = getLatestEvent( trackedEntityInstance.getUid(), "BrZ8MF97cDH"  );
                                        
                                        
                                        //ProgramStageInstance tempProgramStageInstance = programStageInstanceService.getProgramStageInstance( eventUID );
                                        
                                        
                                        //Set<EventDataValue> updatedEventDataValues = new HashSet<>( tempProgramStageInstance.getEventDataValues());
                                        
                                        emailText = emailText 
                                           +"<tr align='left'>"
                                           /*+ "<td align='left' ><b>" + i + " -- "+ trackedEntityInstance.getUid() + "<b></td>"*/
                                           + "<td align='left' ><b>" + client_ID + "<b></td>"
                                           + "<td align='left' ><b>" + clientPrEPID + "<b></td>"
                                           + "<td align='left' ><b>" + getEventDataValue( prEPScreeningStageEventUid, "kQzpqh4JL7l" ) + "<b></td>"
                                           + "<td align='left' ><b>" + getEventDataValue( prEPScreeningStageEventUid, "nVU4BU2jHc3" ) + "<b></td>"
                                           + "<td align='left' ><b>" + getEventDataValue( prEPScreeningStageEventUid, "x3s6CXPdNjd" ) + "<b></td>"
                                           + "<td align='left' ><b>" + orgName + "<b></td>"
                                           + "<td align='left' ><b>" + getEventDataValue( prEPScreeningStageEventUid, "ts9LEoIEJWC" ) + "<b></td>"
                                           + "<td align='left' ><b>" + event_date + "<b></td>"
                                           + "<td align='left' ><b>" + getEventDataValue(eventUID, "icJeQiH7vf3" ) + "<b></td>"
                                           + "<td align='left' ><b>" + last_next_schedule_date + "<b></td>"
                                           + "<td align='left' ><b>" + dayDiffrence + "<b></td>"
                                           +"</tr>";
                                        
                                        count++;
                                    }
                                    
                                }
                            }
                            else
                            {
                                System.out.println( i + " not send email to -- " + trackedEntityInstance.getUid() + " -- " + getTraceOutcome( trackedEntityInstance.getUid() ));
                            }
                            i++;
                        }
                    }

                    emailText = emailText  + "</table></body></html>";

                    //System.out.println( " -- emailText " + emailText );
                    
                    emailMessageSender.sendMessage( emailSubject, emailText, recipients );
                }
            }
        }
        
        /*
        Set<String> recipients = new HashSet<>();
        String teiDashboard = "https://swasthyakawach.in/pccds/dhis-web-tracker-capture/index.html#/dashboard?tei=" + tempEvent.getTrackedEntityInstance() + "&program=" + tempEvent.getProgram() + "&ou=" + tempEvent.getOrgUnit();
        
        String emailText = "Hello, " + '\n' + "A new case has been reported from " + orgUnit.getName()+ " on the " + tempEvent.getEventDate() + " ."  + '\n' + '\n' +" Please click on the below link to review the case and take appropriate action."
                            + '\n' + '\n' + teiDashboard ;

        String emailSubject = "PrEP_Miss-Appointment Client list of " + ;
        
        emailMessageSender.sendMessage( emailSubject, emailText, recipients );
        */
    
       
    }
    public Set<OrganisationUnit> getProgramOrganisationUnitList()
    {
        
        Set<OrganisationUnit> programOrgList = new HashSet<OrganisationUnit>();
        try
        {
            
            String query = "SELECT organisationunitid,uid,name,email from organisationunit where email is not null "
                + "and organisationunitid in ( select organisationunitid from program_organisationunits) ";
                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Long orgUnitId = rs.getLong( 1 );
                String email = rs.getString( 4 );

                if ( orgUnitId != null && email != null )
                {
                    OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
                    programOrgList.add( organisationUnit );
                }
            }

            return programOrgList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal programOrgList ", e );
        }
    }

    public Set<TrackedEntityInstance> getTrackedEntityInstanceList( String orgUnitUid )
    {
        
        Set<TrackedEntityInstance> trackedEntityInstanceList = new HashSet<TrackedEntityInstance>();
        try
        {
            
            String query = "SELECT tei.uid AS teiUID FROM programinstance pi "
                           + "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " 
                           + "INNER JOIN organisationunit org ON pi.organisationunitid = org.organisationunitid " 
                           + "WHERE org.uid = '" + orgUnitUid + "' ";
                           
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                String teiUId = rs.getString( 1 );

                if ( teiUId != null  )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiUId );
                    trackedEntityInstanceList.add( tei );
                }
            }

            return trackedEntityInstanceList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal orgUnitUid ", e );
        }
    }    
    
    public Boolean getTraceOutcome( String teiUid )
    {
        
        Boolean teiTraceOutCome = false; 
        try
        {
            
            String query = "SELECT tei.uid AS teiUID, cast(data.value::json ->> 'value' AS VARCHAR) AS tei_trace_outCome  "
                           + "FROM programstageinstance psi JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE " 
                           + "INNER JOIN dataelement de ON data.key = de.uid " 
                           + "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " 
                           + "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " 
                           + "WHERE psi.programstageid in ( select programstageid from programstage where uid = 'uMYfp6QX2d0') " 
                           + "AND de.uid = 'zkFIdGgOThz' AND tei.uid  = '" + teiUid + "' ";
                           
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while ( rs.next() )
            {
                String teiUId = rs.getString( 1 );

                if ( teiUId != null  )
                {
                    teiTraceOutCome = true; 
                }
            }

            return teiTraceOutCome;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal orgUnitUid ", e );
        }
    }
    
    
    public List<String> getMissAppointmentTEIList( String teiUID, String orgUnitUID, String stageUID, String deUID )
    {
        
        List<String> missAppointmentClientList = new ArrayList<String>();
        try
        {

            String query = "SELECT tei.uid AS teiUID, psi.uid AS eventUID, org.uid AS orgUID,org.name AS orgName, "
                + "psi.executiondate::date as Event_date, cast(data.value::json ->> 'value' AS VARCHAR) AS last_next_schedule_date, "
                + "extract (epoch from (CURRENT_DATE - cast(data.value::json ->> 'value' AS timestamp)))::integer/86400 as dayDiffrence FROM programstageinstance psi "
                + "JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE "
                + "INNER JOIN dataelement de ON data.key = de.uid "
                + "INNER JOIN organisationunit org ON psi.organisationunitid = org.organisationunitid "
                + "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE de.uid = '" + deUID + "' AND org.uid = '" + orgUnitUID + "' AND tei.uid =  '" + teiUID + "' "
                + "AND psi.programstageid in ( select programstageid from programstage where uid = '" + stageUID + "' ) " 
                + "AND cast(data.value::json ->> 'value' AS DATE) <= CURRENT_DATE - interval '7 day' order by psi.executiondate DESC LIMIT 1 ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String trackedEntityInstanceUID = rs.getString( 1 );
                String eventUID = rs.getString( 2 );
                String orgUID = rs.getString( 3 );
                String orgName = rs.getString( 4 );
                String event_date = rs.getString( 5 );
                String last_next_schedule_date = rs.getString( 6 );
                String dayDiffrence = rs.getString( 7 );
                
                if ( trackedEntityInstanceUID != null && eventUID != null )
                {
                    String missAppointmentClientDetails = trackedEntityInstanceUID + ":" + eventUID + ":" + orgUID + ":" + orgName + ":" + event_date + ":" + last_next_schedule_date + ":" + dayDiffrence;
                    
                    missAppointmentClientList.add( missAppointmentClientDetails );
                }
            }

            return missAppointmentClientList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Query ", e );
        }
    }
    
    public String getEventDataValue( String eventUID, String dataElementUID  )
    {
        String eventDataValue = "";
        
        try
        {            
            String query = "SELECT psi.uid eventID, data.key as de_uid, cast(data.value::json ->> 'value' AS VARCHAR) AS de_value "
                + "FROM programstageinstance psi "
                + "JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE  "
                + "INNER JOIN dataelement de ON de.uid = data.key "
                + "WHERE psi.uid = '" + eventUID + "' AND de.uid = '" + dataElementUID + "' ";
                
                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                
                String evUID = rs.getString( 1 );
                String deUID= rs.getString( 2 );
                String dataValue = rs.getString( 3 );
                
                if ( evUID != null && deUID != null && dataValue != null  )
                {
                    eventDataValue = dataValue;
                }
            }

            return eventDataValue;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Query ", e );
        }
    }
    
    public String getLatestEvent( String trackedEntityInstanceUID, String stageUID  )
    {
        String eventUID = "";
        
        try
        {            
            String query = "SELECT tei.uid AS teiUID, psi.uid AS eventUID,psi.executiondate::date as Event_date "
                + "FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid  "
                + "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid in ( select programstageid from programstage where uid = '" + stageUID + "' ) "
                + "AND tei.uid  = '" + trackedEntityInstanceUID + "' ORDER BY psi.executiondate desc LIMIT 1 ";
                
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String teiUID = rs.getString( 1 );
                String evUID= rs.getString( 2 );
                String event_date = rs.getString( 3 );
                
                if ( teiUID != null && evUID != null && event_date != null  )
                {
                    eventUID = evUID;
                }
            }

            return eventUID;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Query ", e );
        }
    }

}
