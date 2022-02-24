package org.hisp.dhis.inspectionmanagement.updatelicensestatus;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.hisp.dhis.system.notification.NotificationLevel.INFO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.scheduling.AbstractJob;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.hisp.dhis.system.util.CodecUtils;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * @author Mithilesh Kumar Thakur
 */

@Component( "updateLicenseStatusJob" )
public class UpdateLicenseStatusJob extends AbstractJob
{

    //private final static int  LICENSE_STATUS_DATAELEMENT_ID = 3430;
    private final static String  LICENSE_STATUS_DATAELEMENT_UID = "AWprRTJ8phx";
    private final static String  REASON_FOR_LICENSE_DATAELEMENT_UID = "vKOlyc0RkLk";
    
    static final String HEADER_AUTHORIZATION = "Authorization";
    
    //private final static int  LICENSE_VALIDITY_DATE_DATAELEMENT_ID = 2609;
    
    //private final static int  LICENSE_STATUS_ATTRIBUTE_ID = 2736;
    //private final static int  LICENSE_VALIDITY_DATE_ATTRIBUTE_ID = 1085;
    
    //private I18nFormat format;
    
    private JdbcTemplate jdbcTemplate;
    private TrackedEntityInstanceService trackedEntityInstanceService;
    private ProgramStageInstanceService programStageInstanceService;
    private ProgramInstanceService programInstanceService;
    private ProgramStageService programStageService;
    private DataElementService dataElementService;
    private CategoryService categoryService;
    private OrganisationUnitService organisationUnitService;
    private final Notifier notifier;
    
    private Date executionDate;
    
    private SimpleDateFormat simpleDateFormat;

    String currentDate = "";

    String currentMonth = "";

    String currentYear = "";

    String todayDate = "";
    
    String firstDateOfYear = "";
    String lastDateOfYear = "";
    
    
    public UpdateLicenseStatusJob( TrackedEntityInstanceService trackedEntityInstanceService,
            ProgramInstanceService programInstanceService,ProgramStageInstanceService programStageInstanceService,
            ProgramStageService programStageService, DataElementService dataElementService, CategoryService categoryService, 
            OrganisationUnitService organisationUnitService,JdbcTemplate jdbcTemplate,Notifier notifier )
    {
        checkNotNull( trackedEntityInstanceService );
        checkNotNull( programInstanceService );
        checkNotNull( programStageInstanceService );
        checkNotNull( programStageService );
        checkNotNull( dataElementService );
        checkNotNull( categoryService );
        checkNotNull( organisationUnitService );
        checkNotNull( jdbcTemplate );
        
        checkNotNull( notifier );

        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.programInstanceService = programInstanceService;
        this.programStageInstanceService = programStageInstanceService;
        this.programStageService = programStageService;
        this.dataElementService = dataElementService;
        this.categoryService = categoryService;
        this.organisationUnitService = organisationUnitService;
        this.jdbcTemplate = jdbcTemplate;
        
        this.notifier = notifier;
    }
    
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public JobType getJobType()
    {
        return JobType.UPDATE_LICENSE_STATUS;
    }
    
    @Override
    public void execute( JobConfiguration jobConfiguration )
    {
        Clock clock = new Clock().startClock();

        clock.logTime( "Start to Update License Status" );
        notifier.notify( jobConfiguration, INFO, "Start to Update License Status ", true );

        
        LocalDate now = LocalDate.now(); // 2015-11-23
        LocalDate firstDayYear = now.with(firstDayOfYear()); // 2015-01-01
        LocalDate lastDayYear = now.with(lastDayOfYear()); // 2015-12-31
        //Set<Program> userPrograms = new LinkedHashSet<>();
        System.out.println( "firstDayOfYear -- " + firstDayYear + " lastDayOfYear -- " + lastDayYear );
        
        firstDateOfYear = firstDayYear.toString();
        lastDateOfYear = lastDayYear.toString();
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date date = new Date();
        //System.out.println( timeFormat.format( date ) );

        todayDate = simpleDateFormat.format( date );
        currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        
        try
        {
            executionDate = simpleDateFormat.parse( currentYear+"-12-31" );
        }
        catch ( ParseException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
        //executionDate = simpleDateFormat.format( currentYear+"12-31" );
        
        //todayDate = "2022-12-31";
        System.out.println( "executionDate -- " + executionDate + " -- " + lastDayYear.toString() );
        
        if( todayDate.equalsIgnoreCase( lastDateOfYear ))
        {
            System.out.println( " 1 today date -- " + todayDate + " -- lastDateOfYear " + lastDateOfYear );
            //firstDateOfYear = "2021-01-01";
            //lastDateOfYear = "2021-12-31";
            
            updateLicenseStatus();
        }
        else
        {
            System.out.println( " 2 today date -- " + todayDate + " -- lastDateOfYear " + lastDateOfYear ); 
        }
        
        // update tracked entity attribute value data 
        
        /*
        String url = "http://127.0.0.1:8091/sl_ehis";
        String username = "";
        String password = "";
        HttpEntity<String> request = getBasicAuthRequestEntity( username, password );
        
        ResponseEntity<String> response = null;
        HttpStatus sc = null;
        String st = null;
        AvailabilityStatus status = null;
        RestTemplate restTemplate =  new RestTemplate();
        try
        {
            response = restTemplate.exchange( url, HttpMethod.GET, request, String.class );
            sc = response.getStatusCode();
            
            updateLicenseStatus();
        }
        catch ( HttpClientErrorException | HttpServerErrorException ex )
        {
            sc = ex.getStatusCode();
            st = ex.getStatusText();
        }
        catch ( ResourceAccessException ex )
        {
            //return new AvailabilityStatus( false, "Network is unreachable", HttpStatus.BAD_GATEWAY );
        }
        */
        
        
        
        //List<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>( getProgramStageInstanceFromTedvByDataElementId( LICENSE_VALIDITY_DATE_DATAELEMENT_ID ) );
        //System.out.println("programStageInstances list Size = " + programStageInstances.size() );
        
        /*
        List<TrackedEntityInstance> trackedEntityInstances = new ArrayList<TrackedEntityInstance>( getTrackedEntityInstanceFromTeAValue( LICENSE_VALIDITY_DATE_ATTRIBUTE_ID ) );
        System.out.println("trackedEntityInstances list Size = " + trackedEntityInstances.size() );
        */
        
        clock.logTime( "Update License Status completed" );
        notifier.notify( jobConfiguration, INFO, "Update License Status completed", true );
        
       
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // Get ProgramStageInstance List from trackedEntity dataValue table  
    //--------------------------------------------------------------------------------
    /*
    public List<ProgramStageInstance> getProgramStageInstanceFromTedvByDataElementId( Integer dataElementId )
    {
        List<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>();


        //SELECT programstageinstanceid FROM trackedentitydatavalue WHERE CURRENT_DATE > value::date and dataelementid = 2609 order by programstageinstanceid;
        try
        {
            
            String query = "SELECT tei.uid AS teiUID, pi.uid AS enrollment, org.uid AS orgUnitUID, " +
            "prg.uid as prgUID,prgs.uid as prgSUID, psi.uid eventUID, data.key as de_uid, cast(data.value::json ->> 'value' AS VARCHAR) " +
            "AS de_value FROM programstageinstance psi JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE " +
            "INNER JOIN programstage prgs ON prgs.programstageid = psi.programstageid " +
            "INNER JOIN organisationunit org ON org.organisationunitid = psi.organisationunitid " +
            "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
            "INNER JOIN program prg ON prg.programid = pi.programid " +
            "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " +
            "INNER JOIN dataelement de ON de.uid = data.key " +
            "where psi.eventdatavalues -> 'AWprRTJ8phx' ->> 'value' != '2' " +
            "and de.uid = 'AWprRTJ8phx' order by psi.programstageinstanceid ";
            
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String teiUID = rs.getString( 1 );
                String piUID = rs.getString( 2 );
                String orgUID = rs.getString( 3 );
                String prgUID = rs.getString( 4 );
                String prgSUID = rs.getString( 5 );
                
                if ( teiUID != null && piUID != null && orgUID != null && prgUID != null && prgSUID != null )
                {
                    //ProgramStageInstance psi = programStageInstanceService.getProgramStageInstance( psiId );
                    //programStageInstances.add( psi );
                    
                    System.out.println( "teiUID - " + teiUID + " piUID - " + piUID + " orgUID - " + orgUID + " prgUID - " + prgUID + " prgSUID - " + prgSUID );
                    Integer programStageInstanceId = null;
                    EventStatus eventStatus = EventStatus.COMPLETED;
                    ProgramStageInstance programStageInstance = new ProgramStageInstance();
                    ProgramInstance programInstance = programInstanceService.getProgramInstance( piUID );
                    
                    Set<EventDataValue> updatedEventDataValues = new HashSet<>();
                    
                    boolean providedElsewhere = false;
                    EventDataValue eventDataValueLicenseStatus = new EventDataValue();
                    
                    eventDataValueLicenseStatus.setDataElement( LICENSE_STATUS_DATAELEMENT_UID );
                    eventDataValueLicenseStatus.setValue( "2" ); // for Expired
                    eventDataValueLicenseStatus.setProvidedElsewhere( providedElsewhere );
                    updatedEventDataValues.add( eventDataValueLicenseStatus );
                    
                    EventDataValue eventDataValueReasonForLicense = new EventDataValue();
                    eventDataValueReasonForLicense.setDataElement( REASON_FOR_LICENSE_DATAELEMENT_UID );
                    eventDataValueReasonForLicense.setValue( "Compliance with the regulation" ); // for Expired
                    eventDataValueReasonForLicense.setProvidedElsewhere( providedElsewhere );
                    updatedEventDataValues.add( eventDataValueReasonForLicense );
                    
                    if ( programInstance != null )
                    {
                        programStageInstance = new ProgramStageInstance();

                        programStageInstance.setProgramInstance( programInstance );
                        programStageInstance.setProgramStage( programStageService.getProgramStage( prgSUID ) );
                        //programStageInstance.setProgramStageInstanceMembers( programStageInstanceMembers );
                        //programStageInstance.setAttributeOptionCombo( coc );  
                        programStageInstance.setOrganisationUnit( programInstance.getEntityInstance().getOrganisationUnit() );
                        programStageInstance.setExecutionDate( executionDate );
                        programStageInstance.setStatus( eventStatus );
                        
                        programStageInstance.setEventDataValues( updatedEventDataValues );
                        //programStageInstance.setCreated( now );
                        //programStageInstance.setStoredBy( storedBy );
                        //programStageInstance.setLastUpdated( now );

                        programStageInstanceId = (int)programStageInstanceService.addProgramStageInstance( programStageInstance );
                    }
                    
                   
                    ProgramStageInstance tempProgramStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
                    
                    
                    Set<DataValue> updatedEventDataValues = new HashSet<>();
                    Event updatedEvent = eventService.getEvent( tempProgramStageInstance );
                    
                    if ( tempProgramStageInstance != null )
                    {
                        boolean providedElsewhere = false;
                        DataValue eventDataValueLicenseStatus = new DataValue();
                        
                        eventDataValueLicenseStatus.setDataElement( LICENSE_STATUS_DATAELEMENT_UID );
                        eventDataValueLicenseStatus.setValue( "2" ); // for Expired
                        eventDataValueLicenseStatus.setProvidedElsewhere( providedElsewhere );
                        updatedEventDataValues.add( eventDataValueLicenseStatus );
                        
                        DataValue eventDataValueReasonForLicense = new DataValue();
                        eventDataValueReasonForLicense.setDataElement( REASON_FOR_LICENSE_DATAELEMENT_UID );
                        eventDataValueReasonForLicense.setValue( "Compliance with the regulation" ); // for Expired
                        eventDataValueReasonForLicense.setProvidedElsewhere( providedElsewhere );
                        updatedEventDataValues.add( eventDataValueReasonForLicense );
                    }
                    
                    updatedEvent.setDataValues( updatedEventDataValues );
                    
                    
                    ImportSummary importSummary = eventService.updateEvent( updatedEvent, true, null, false );
                    System.out.println(  " importSummary -- " + importSummary.toString());
                    
                }
            }

            return programStageInstances;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal DataElement id", e );
        }
    }
    */
    //--------------------------------------------------------------------------------
    // Get TrackedEntityInstance List from tracked entity attribute value
    //--------------------------------------------------------------------------------
    /*
    public List<TrackedEntityInstance> getTrackedEntityInstanceFromTeAValue( Integer attributeId )
    {
        List<TrackedEntityInstance> trackedEntityInstances = new ArrayList<TrackedEntityInstance>();


        //SELECT trackedentityinstanceid, value FROM trackedentityattributevalue WHERE CURRENT_DATE > value::date and trackedentityattributeid = 1085;
        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue  " +
                            "WHERE CURRENT_DATE > value::date and trackedentityattributeid = "+ attributeId +" ";
          
            //System.out.println( "query = " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                
                if ( teiId != null )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiId );
                    trackedEntityInstances.add( tei );
                }
            }

            return trackedEntityInstances;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    */
    private void updateLicenseStatus()
    {
        String storedBy = "admin";
            
        CategoryOptionCombo coc = null;
        coc = categoryService.getDefaultCategoryOptionCombo();
        
        String query = "SELECT tei.uid AS teiUID, pi.uid AS enrollment, org.uid AS orgUnitUID, " +
        "prg.uid as prgUID,prgs.uid as prgSUID, psi.uid eventUID, data.key as de_uid, cast(data.value::json ->> 'value' AS VARCHAR) " +
        "AS de_value FROM programstageinstance psi JOIN json_each_text(psi.eventdatavalues::json) data ON TRUE " +
        "INNER JOIN programstage prgs ON prgs.programstageid = psi.programstageid " +
        "INNER JOIN organisationunit org ON org.organisationunitid = psi.organisationunitid " +
        "INNER JOIN programinstance pi ON pi.programinstanceid = psi.programinstanceid " +
        "INNER JOIN program prg ON prg.programid = pi.programid " +
        "INNER JOIN trackedentityinstance tei ON tei.trackedentityinstanceid = pi.trackedentityinstanceid " +
        "INNER JOIN dataelement de ON de.uid = data.key " +
        "where psi.eventdatavalues -> 'AWprRTJ8phx' ->> 'value' != '2' " +
        "and de.uid = 'AWprRTJ8phx' AND psi.executiondate::date between '" + firstDateOfYear + 
        "' AND '" + lastDateOfYear +"' order by psi.programstageinstanceid ";

        System.out.println(  " query -- " + query  );
        
        SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
        int eventCount = 0;
        while ( rs.next() )
        {
            String teiUID = rs.getString( 1 );
            String piUID = rs.getString( 2 );
            String orgUID = rs.getString( 3 );
            String prgUID = rs.getString( 4 );
            String prgSUID = rs.getString( 5 );
            
            eventCount++;
            if ( teiUID != null && piUID != null && orgUID != null && prgUID != null && prgSUID != null )
            {
                //ProgramStageInstance psi = programStageInstanceService.getProgramStageInstance( psiId );
                //programStageInstances.add( psi );
                
                System.out.println( "teiUID - " + teiUID + " piUID - " + piUID + " orgUID - " + orgUID + " prgUID - " + prgUID + " prgSUID - " + prgSUID );
                Integer programStageInstanceId = null;
                EventStatus eventStatus = EventStatus.COMPLETED;
                ProgramStageInstance programStageInstance = new ProgramStageInstance();
                ProgramInstance programInstance = programInstanceService.getProgramInstance( piUID );
                
                Set<EventDataValue> updatedEventDataValues = new HashSet<>();
                
                boolean providedElsewhere = false;
                EventDataValue eventDataValueLicenseStatus = new EventDataValue();
                
                DataElement licenseStatus = dataElementService.getDataElement( LICENSE_STATUS_DATAELEMENT_UID );
                DataElement reasonForLicensede = dataElementService.getDataElement( REASON_FOR_LICENSE_DATAELEMENT_UID );
                
                eventDataValueLicenseStatus.setDataElement( LICENSE_STATUS_DATAELEMENT_UID );
                eventDataValueLicenseStatus.setValue( "2" ); // for Expired
                eventDataValueLicenseStatus.setProvidedElsewhere( providedElsewhere );
                eventDataValueLicenseStatus.setStoredBy( storedBy );
                updatedEventDataValues.add( eventDataValueLicenseStatus );
                
                EventDataValue eventDataValueReasonForLicense = new EventDataValue();
                eventDataValueReasonForLicense.setDataElement( REASON_FOR_LICENSE_DATAELEMENT_UID );
                eventDataValueReasonForLicense.setValue( "Compliance with the regulation" ); // for Expired
                eventDataValueReasonForLicense.setProvidedElsewhere( providedElsewhere );
                eventDataValueReasonForLicense.setStoredBy( storedBy );
                updatedEventDataValues.add( eventDataValueReasonForLicense );
                
                Map<DataElement, EventDataValue> dataElementEventDataValueMap = new HashMap<DataElement, EventDataValue>();
                
                dataElementEventDataValueMap.put( licenseStatus, eventDataValueLicenseStatus );
                dataElementEventDataValueMap.put( reasonForLicensede, eventDataValueReasonForLicense );
                
                if ( programInstance != null )
                {
                    programStageInstance = new ProgramStageInstance();

                    programStageInstance.setProgramInstance( programInstance );
                    programStageInstance.setProgramStage( programStageService.getProgramStage( prgSUID ) );
                    //programStageInstance.setProgramStageInstanceMembers( programStageInstanceMembers );
                    programStageInstance.setAttributeOptionCombo( coc );  // important to set coc
                    //programStageInstance.setOrganisationUnit( programInstance.getEntityInstance().getOrganisationUnit() );
                    programStageInstance.setOrganisationUnit( organisationUnitService.getOrganisationUnit( orgUID ));
                    programStageInstance.setExecutionDate( executionDate );
                    programStageInstance.setStatus( eventStatus );
                    programStageInstance.setCompletedBy( storedBy );
                    programStageInstance.setCompletedDate( executionDate );
                    //programStageInstance.setEventDataValues( updatedEventDataValues );
                    //programStageInstance.setCreated( now );
                    programStageInstance.setStoredBy( storedBy );
                    //programStageInstance.setLastUpdated( now );

                    //programStageInstanceId = (int)programStageInstanceService.addProgramStageInstance( programStageInstance );
                    
                    programStageInstanceService.saveEventDataValuesAndSaveProgramStageInstance( programStageInstance, dataElementEventDataValueMap );
                    
                }
                
                //ProgramStageInstance tempProgramStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstanceId );
                
                /*
                Set<DataValue> updatedEventDataValues = new HashSet<>();
                Event updatedEvent = eventService.getEvent( tempProgramStageInstance );
                
                if ( tempProgramStageInstance != null )
                {
                    boolean providedElsewhere = false;
                    DataValue eventDataValueLicenseStatus = new DataValue();
                    
                    eventDataValueLicenseStatus.setDataElement( LICENSE_STATUS_DATAELEMENT_UID );
                    eventDataValueLicenseStatus.setValue( "2" ); // for Expired
                    eventDataValueLicenseStatus.setProvidedElsewhere( providedElsewhere );
                    updatedEventDataValues.add( eventDataValueLicenseStatus );
                    
                    DataValue eventDataValueReasonForLicense = new DataValue();
                    eventDataValueReasonForLicense.setDataElement( REASON_FOR_LICENSE_DATAELEMENT_UID );
                    eventDataValueReasonForLicense.setValue( "Compliance with the regulation" ); // for Expired
                    eventDataValueReasonForLicense.setProvidedElsewhere( providedElsewhere );
                    updatedEventDataValues.add( eventDataValueReasonForLicense );
                }
                
                updatedEvent.setDataValues( updatedEventDataValues );
                ImportSummary importSummary = eventService.updateEvent( updatedEvent, true, null, false );
                */
                
                System.out.println(  " Event " + eventCount +  " created -- " );
            }
        }
        
        System.out.println(  " Total Event Created " + eventCount );
    }
    private static <T> HttpEntity<T> getBasicAuthRequestEntity( String username, String password )
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set( HEADER_AUTHORIZATION, CodecUtils.getBasicAuthString( username, password ) );
        return new HttpEntity<>( headers );
    }    
}
