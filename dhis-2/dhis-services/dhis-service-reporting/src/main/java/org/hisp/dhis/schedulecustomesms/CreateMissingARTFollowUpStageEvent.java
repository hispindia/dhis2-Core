package org.hisp.dhis.schedulecustomesms;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.scheduling.Job;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobProgress;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * @author Mithilesh Kumar Thakur
 */
@Slf4j
@Component( "createMissingARTFollowUpStageEventJob" )
public class CreateMissingARTFollowUpStageEvent implements Job
{
    private static final Log log = LogFactory.getLog( CreateMissingARTFollowUpStageEvent.class );
    
    private static final String KEY_TASK = "createMissingARTFollowUpStageEventJob";
    
    private final static String  ART_STATUS_DATAELEMENT_UID = "WpBa1L6xxPC";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private final Notifier notifier;
    
    private TrackedEntityInstanceService trackedEntityInstanceService;
    
    private JdbcTemplate jdbcTemplate;

    private ProgramInstanceService programInstanceService;
            
    private ProgramStageInstanceService programStageInstanceService;
    
    private DataElementService dataElementService;
    
    private ProgramStageService programStageService;
    
    private CategoryService categoryService;
    
    private OrganisationUnitService organisationUnitService;
    
    public CreateMissingARTFollowUpStageEvent( Notifier notifier, TrackedEntityInstanceService trackedEntityInstanceService,
            JdbcTemplate jdbcTemplate, DataElementService dataElementService, ProgramInstanceService programInstanceService,
            ProgramStageInstanceService programStageInstanceService, ProgramStageService programStageService, 
            CategoryService categoryService, OrganisationUnitService organisationUnitService )
    {

        checkNotNull( notifier );
        checkNotNull( trackedEntityInstanceService );
        checkNotNull( jdbcTemplate );
        checkNotNull( dataElementService ); 
        checkNotNull( programInstanceService );
        checkNotNull( programStageInstanceService );
        checkNotNull( categoryService );
        checkNotNull( organisationUnitService );
        
        this.notifier = notifier;
        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.jdbcTemplate = jdbcTemplate;
        this.dataElementService = dataElementService;
        this.programInstanceService = programInstanceService;
        this.programStageInstanceService = programStageInstanceService;
        this.programStageService = programStageService;
        this.categoryService = categoryService;
        this.organisationUnitService = organisationUnitService;
        
        
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    
    private Set<String> trackedEntityInstanceUIds = new HashSet<>();
    private Date nextExecutionDate;
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.CREATE_MISSING_EVENT;
    }

    @Override
    public void execute( JobConfiguration jobConfiguration, JobProgress progress  )
    {
        System.out.println("INFO: Create Missing ART Follow-up stage Event job has started at : " + new Date() +" -- " + JobType.CREATE_MISSING_EVENT );
        
        Clock clock = new Clock().startClock();

        //clock.logTime( "Starting Create Missing ART Follow-up stage Event job " );
        //notifier.notify( jobConfiguration, INFO, "Create Missing ART Follow-up stage Event job ", true );

        //sendMessages();
        
        //notifier.notify( jobConfiguration, INFO, String.format( "%s has started", KEY_TASK ) );
        log.info( String.format( "%s has started", KEY_TASK ) );
        
        trackedEntityInstanceUIds = new HashSet<>( getTrackedEntityInstanceUIds() );
        
        System.out.println( " 2 tei list size : " + trackedEntityInstanceUIds.size() );
        if( trackedEntityInstanceUIds != null && trackedEntityInstanceUIds.size() > 0 )
        {
            createMissingARTFollowUpStageEvent( trackedEntityInstanceUIds );
        }
        
        //clock.logTime( "Completed Create Missing ART Follow-up stage Event job" );
        notifier.notify( jobConfiguration, INFO, "Create Missing ART Follow-up stage Event job completed", true );
        
    }
    
    // --------------------------------------------------------------------------------
    // Create ART Follow Up Stage Event with art_status = missing 
    // --------------------------------------------------------------------------------
    
    private void createMissingARTFollowUpStageEvent( Set<String> teiUids )
    {
        
        if( teiUids != null && teiUids.size() > 0 )
        {
            for( String teiUID : teiUids )
            {
                // find the latest ART Follow Up Stage Event with art_status vlaue of TEI
                String query = " SELECT tei.uid as tei_uid, psi.uid as event, psi.executiondate::date, " +
                    " psi.eventdatavalues -> 'WpBa1L6xxPC' ->> 'value' as art_status,org.uid as orgUnitUID,pi.uid as enrollment, " +
                    " extract (epoch from (CURRENT_DATE - cast(psi.executiondate AS timestamp)))::integer/86400 as dayDiffrence FROM programstageinstance psi " +
                    " INNER JOIN programinstance pi ON psi.programinstanceid = pi.programinstanceid " +
                    " INNER JOIN program prg ON pi.programid = prg.programid " +
                    " INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " +
                    " INNER JOIN organisationunit org ON psi.organisationunitid = org .organisationunitid " +
                    " WHERE prg.uid = 'L78QzNqadTV' AND psi.deleted is false AND psi.programstageid in ( " +
                    " SELECT programstageid from programstage where uid = 'YRSdePjzzfs') AND psi.eventdatavalues -> 'WpBa1L6xxPC' is not null " + 
                    " AND tei.uid = '" + teiUID + "' ORDER BY psi.executiondate desc LIMIT 1; ";
                
                
                //System.out.println(  " query -- " + query  );
                
                SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
                int eventCount = 0;
                while ( rs.next() )
                {
                    String tempTeiUid = rs.getString( 1 );
                    String eventUID = rs.getString( 2 );
                    String executionDate = rs.getString( 3 );
                    String art_status = rs.getString( 4 );
                    String orgUID = rs.getString( 5 );
                    String enrillmentUID = rs.getString( 6 );
                    Integer dayDiffrence = rs.getInt( 7 );

                    
                    if ( art_status != null && art_status.equalsIgnoreCase( "lost_to_follow_up" ) && dayDiffrence != null && dayDiffrence > 90 )
                    {
                        eventCount++;
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
                        
                        //create instance of the Calendar class and set the date to the given date  
                        Calendar cal = Calendar.getInstance();  
                        try
                        {
                            cal.setTime(sdf.parse(executionDate));
                        }
                        catch ( ParseException e )
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                        
                             
                        // use add() method to add the days to the given date  
                        cal.add(Calendar.DAY_OF_MONTH, 90);  
                        String tempExecutionDate = sdf.format(cal.getTime());  
                          
                        try
                        {
                            nextExecutionDate = sdf.parse( tempExecutionDate );
                        }
                        catch ( ParseException e )
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //date after adding three days to the given date  
                        //System.out.println(dateAfter+" is the date after adding 3 days.");  
                        
                        
                        System.out.println(  "Sl No - " + eventCount + " tempTeiUid -- " + tempTeiUid + " art_status -- " + art_status + " dayDiffrence -- " + dayDiffrence + " executionDate -- " + executionDate + " nextExecutionDate -- " + nextExecutionDate );
                        DataElement artStatus = dataElementService.getDataElement( ART_STATUS_DATAELEMENT_UID );
                        
                        CategoryOptionCombo coc = null;
                        coc = categoryService.getDefaultCategoryOptionCombo();
                        
                        EventStatus eventStatus = EventStatus.COMPLETED;
                        ProgramStageInstance programStageInstance = new ProgramStageInstance();
                        ProgramInstance programInstance = programInstanceService.getProgramInstance( enrillmentUID );
                        
                        boolean providedElsewhere = false;
                        EventDataValue eventDataValue = new EventDataValue();
                        
                        eventDataValue.setDataElement( ART_STATUS_DATAELEMENT_UID );
                        eventDataValue.setValue( "missing" ); // for Expired
                        eventDataValue.setProvidedElsewhere( providedElsewhere );
                        eventDataValue.setStoredBy( "hispdev" );

                        
                        Map<DataElement, EventDataValue> dataElementEventDataValueMap = new HashMap<DataElement, EventDataValue>();
                        
                        dataElementEventDataValueMap.put( artStatus, eventDataValue );
                        
                        
                        if ( programInstance != null )
                        {
                            programStageInstance = new ProgramStageInstance();

                            programStageInstance.setProgramInstance( programInstance );
                            programStageInstance.setProgramStage( programStageService.getProgramStage( "YRSdePjzzfs" ) );
                            programStageInstance.setAttributeOptionCombo( coc );  // important to set coc
                            programStageInstance.setOrganisationUnit( organisationUnitService.getOrganisationUnit( orgUID ));
                            programStageInstance.setExecutionDate( nextExecutionDate );
                            programStageInstance.setStatus( eventStatus );
                            programStageInstance.setCompletedBy( "hispdev" );
                            programStageInstance.setCompletedDate( nextExecutionDate );
                            
                            programStageInstance.setStoredBy( "hispdev" );
                            programStageInstanceService.saveEventDataValuesAndSaveProgramStageInstance( programStageInstance, dataElementEventDataValueMap );
                            
                        }
                        

                        eventCount++;
                    }
                    
                    
                }
     
            }
        }
              
    }
    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids for ART Follow-up stage and art_status value entered
    // --------------------------------------------------------------------------------
    public Set<String> getTrackedEntityInstanceUIds()
    {
        Set<String> teiUids = new HashSet<String>();
        try
        {
            
            String query = "SELECT distinct(tei.uid ) as tei_uid FROM programstageinstance psi "
                            + "INNER JOIN programinstance pi ON psi.programinstanceid = pi.programinstanceid "
                            + "INNER JOIN program prg ON pi.programid = prg.programid "
                            + "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid "
                            + "INNER JOIN organisationunit org ON psi.organisationunitid = org .organisationunitid "
                            + "WHERE prg.uid = 'L78QzNqadTV' AND psi.deleted is false AND psi.programstageid in "
                            + " (select programstageid from programstage where uid = 'YRSdePjzzfs') AND " 
                            + " psi.eventdatavalues -> 'WpBa1L6xxPC' is not null; ";
                     
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String tei_uid = rs.getString( 1 );

                if ( tei_uid != null )
                {
                    teiUids.add( tei_uid );
                }
            }

            //System.out.println( " 1 tei list size : " + teiUids.size() );
            return teiUids;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    
    @Override
    public ErrorReport validate()
    {
        // TODO Auto-generated method stub
        System.out.println("INFO: Error in validate at : " + new Date() );
        return null;
    }
    
}
