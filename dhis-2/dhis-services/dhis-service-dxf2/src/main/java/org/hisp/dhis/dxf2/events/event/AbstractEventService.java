package org.hisp.dhis.dxf2.events.event;

/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.IdSchemes;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.IdentifiableProperty;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.commons.collection.CachingMap;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.events.TrackerAccessManager;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.report.EventRow;
import org.hisp.dhis.dxf2.events.report.EventRows;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.dxf2.metadata.feedback.ImportReportMode;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.fileresource.FileResourceService;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.EventSyncService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.program.ProgramType;
import org.hisp.dhis.program.notification.ProgramNotificationEventType;
import org.hisp.dhis.program.notification.ProgramNotificationPublisher;
import org.hisp.dhis.programrule.engine.ProgramRuleEngineService;
import org.hisp.dhis.query.Order;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.query.Restrictions;
import org.hisp.dhis.scheduling.JobConfiguration;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.system.notification.NotificationLevel;
import org.hisp.dhis.system.notification.Notifier;
import org.hisp.dhis.system.util.Clock;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.ValidationUtils;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.trackedentitycomment.TrackedEntityCommentService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hisp.dhis.dxf2.events.event.EventSearchParams.*;
import static org.hisp.dhis.system.notification.NotificationLevel.ERROR;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Transactional
public abstract class AbstractEventService
    implements EventService
{
    private static final Log log = LogFactory.getLog( AbstractEventService.class );

    public static final List<String> STATIC_EVENT_COLUMNS = Arrays.asList( EVENT_ID, EVENT_CREATED_ID,
        EVENT_LAST_UPDATED_ID, EVENT_STORED_BY_ID, EVENT_COMPLETED_BY_ID, EVENT_COMPLETED_DATE_ID,
        EVENT_EXECUTION_DATE_ID, EVENT_DUE_DATE_ID, EVENT_ORG_UNIT_ID, EVENT_ORG_UNIT_NAME, EVENT_STATUS_ID,
        EVENT_LONGITUDE_ID, EVENT_LATITUDE_ID, EVENT_PROGRAM_STAGE_ID, EVENT_PROGRAM_ID,
        EVENT_ATTRIBUTE_OPTION_COMBO_ID, EVENT_DELETED );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    protected ProgramService programService;

    @Autowired
    protected ProgramStageService programStageService;

    @Autowired
    protected ProgramInstanceService programInstanceService;

    @Autowired
    protected ProgramStageInstanceService programStageInstanceService;

    @Autowired
    protected OrganisationUnitService organisationUnitService;

    @Autowired
    protected DataElementService dataElementService;

    @Autowired
    protected CurrentUserService currentUserService;

    @Autowired
    protected TrackedEntityDataValueService dataValueService;

    @Autowired
    protected TrackedEntityInstanceService entityInstanceService;

    @Autowired
    protected TrackedEntityCommentService commentService;

    @Autowired
    protected EventStore eventStore;

    @Autowired
    protected I18nManager i18nManager;

    @Autowired
    protected Notifier notifier;

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected DbmsManager dbmsManager;

    @Autowired
    protected IdentifiableObjectManager manager;

    @Autowired
    protected DataElementCategoryService categoryService;

    @Autowired
    protected FileResourceService fileResourceService;

    @Autowired
    protected SchemaService schemaService;

    @Autowired
    protected QueryService queryService;

    @Autowired
    protected TrackerAccessManager trackerAccessManager;

    @Autowired
    protected ProgramRuleEngineService programRuleEngineService;

    @Autowired
    protected ProgramNotificationPublisher programNotificationPublisher;

    @Autowired
    protected AclService aclService;

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected EventSyncService eventSyncService;

    protected static final int FLUSH_FREQUENCY = 100;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // -------------------------------------------------------------------------
    // Caches
    // -------------------------------------------------------------------------

    private CachingMap<String, OrganisationUnit> organisationUnitCache = new CachingMap<>();

    private CachingMap<String, Program> programCache = new CachingMap<>();

    private CachingMap<String, ProgramStage> programStageCache = new CachingMap<>();

    private CachingMap<String, DataElement> dataElementCache = new CachingMap<>();

    private CachingMap<String, DataElementCategoryOption> categoryOptionCache = new CachingMap<>();

    private CachingMap<String, DataElementCategoryOptionCombo> categoryOptionComboCache = new CachingMap<>();

    private CachingMap<String, DataElementCategoryOptionCombo> attributeOptionComboCache = new CachingMap<>();

    private CachingMap<String, List<ProgramInstance>> activeProgramInstanceCache = new CachingMap<>();
    
    private CachingMap<String, ProgramInstance> programInstanceCache = new CachingMap<>();
    
    private CachingMap<String, ProgramStageInstance> programStageInstanceCache = new CachingMap<>();
    
    private CachingMap<String, TrackedEntityInstance> trackedEntityInstanceCache = new CachingMap<>();

    private CachingMap<Class<? extends IdentifiableObject>, IdentifiableObject> defaultObjectsCache = new CachingMap<>();
    
    private Set<ProgramInstance> programInstancesToUpdate = new HashSet<>();
    
    private Set<TrackedEntityInstance> trackedEntityInstancesToUpdate = new HashSet<>();

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------
    
    public ImportSummaries processEventImport( List<Event> events, ImportOptions importOptions, JobConfiguration jobId )
    {
        User user = currentUserService.getCurrentUser();
        
        if ( importOptions == null )
        {
            importOptions = new ImportOptions();
        }
        
        ImportSummaries importSummaries = new ImportSummaries();        

        notifier.clear( jobId ).notify( jobId, "Importing events" );
        Clock clock = new Clock( log ).startClock();
        
        
        List<List<Event>> partitions = Lists.partition( events, FLUSH_FREQUENCY );

        for ( List<Event> _events : partitions )
        {            
            prepareCaches( user, _events );
            
            List<Event> create = new ArrayList<>();
            List<Event> update = new ArrayList<>();
            List<String> delete = new ArrayList<>();

            if ( importOptions.getImportStrategy().isCreate() )
            {
                create.addAll( events );
            }
            else if ( importOptions.getImportStrategy().isCreateAndUpdate() )
            {
                for ( Event event : events )
                {
                    if ( StringUtils.isEmpty( event.getEvent() ) )
                    {
                        create.add( event );
                    }
                    else
                    {
                        if ( event.isDeleted() )
                        {
                            delete.add( event.getEvent() );
                        }
                        else
                        {
                            ProgramStageInstance programStageInstance = getProgramStageInstance( event.getEvent() );

                            if ( programStageInstance == null )
                            {
                                create.add( event );
                            }
                            else
                            {
                                update.add( event );
                            }
                        }
                    }
                }
            }
            else if ( importOptions.getImportStrategy().isUpdate() )
            {
                update.addAll( events );
            }
            else if ( importOptions.getImportStrategy().isDelete() )
            {
                delete.addAll( events.stream().map( Event::getEvent ).collect( Collectors.toList() ) );
            }

            importSummaries.addImportSummaries( addEvents( create, importOptions, true ) );
            importSummaries.addImportSummaries( updateEvents( update, false, true ) );
            importSummaries.addImportSummaries( deleteEvents( delete ) );

            if ( events.size() >= FLUSH_FREQUENCY )
            {
                clearSession( user );
            }
        }
        
        if ( jobId != null )
        {
            notifier.notify( jobId, NotificationLevel.INFO, "Import done. Completed in " + clock.time() + ".", true ).
                addJobSummary( jobId, importSummaries );
        }
        else
        {
            clock.logTime( "Import done" );
        }

        if ( ImportReportMode.ERRORS == importOptions.getReportMode() )
        {
            importSummaries.getImportSummaries().removeIf( is -> is.getConflicts().isEmpty() );
        }

        return importSummaries;
    }

    @Override
    public ImportSummaries addEvents( List<Event> events, ImportOptions importOptions, boolean clearSession )
    {
        ImportSummaries importSummaries = new ImportSummaries();
        User user = currentUserService.getCurrentUser();

        List<List<Event>> partitions = Lists.partition( events, FLUSH_FREQUENCY );

        for ( List<Event> _events : partitions )
        {
            prepareCaches( user, events );

            for ( Event event : _events )
            {
                importSummaries.addImportSummary( addEvent( event, importOptions, true ) );
            }

            if ( clearSession && events.size() >= FLUSH_FREQUENCY )
            {
                clearSession( user );
            }
        }
        
        updateEntities( user );

        return importSummaries;
    }

    @Override
    public ImportSummaries addEvents( List<Event> events, ImportOptions importOptions, JobConfiguration jobId )
    {
        notifier.clear( jobId ).notify( jobId, "Importing events" );

        try
        {
            ImportSummaries importSummaries = addEvents( events, importOptions, true );

            if ( jobId != null )
            {
                notifier.notify( jobId, NotificationLevel.INFO, "Import done", true ).addJobSummary( jobId,
                    importSummaries );
            }

            return importSummaries;
        }
        catch ( RuntimeException ex )
        {
            log.error( DebugUtils.getStackTrace( ex ) );
            notifier.notify( jobId, ERROR, "Process failed: " + ex.getMessage(), true );
            return new ImportSummaries().addImportSummary(
                new ImportSummary( ImportStatus.ERROR, "The import process failed: " + ex.getMessage() ) );
        }
    }

    @Override
    public ImportSummary addEvent( Event event, ImportOptions importOptions, boolean bulkImport )
    {
        return addEvent( event, currentUserService.getCurrentUser(), importOptions, bulkImport );
    }

    protected ImportSummary addEvent( Event event, User user, ImportOptions importOptions, boolean bulkImport )
    {
        if ( importOptions == null )
        {
            importOptions = new ImportOptions();
        }
        
        ProgramStageInstance programStageInstance = getProgramStageInstance( event.getEvent() );
        
        if ( programStageInstance == null && !StringUtils.isEmpty( event.getEvent() ) && !CodeGenerator.isValidUid( event.getEvent() ) )
        {
            return new ImportSummary( ImportStatus.ERROR, "Event.event did not point to a valid event: " + event.getEvent() ).setReference( event.getEvent() ).incrementIgnored();
        }

        Program program = getProgram( importOptions.getIdSchemes().getProgramIdScheme(), event.getProgram() );
        ProgramStage programStage = getProgramStage( importOptions.getIdSchemes().getProgramStageIdScheme(), event.getProgramStage() );
        OrganisationUnit organisationUnit = getOrganisationUnit( importOptions.getIdSchemes(), event.getOrgUnit() );
        TrackedEntityInstance entityInstance = getTrackedEntityInstance( event.getTrackedEntityInstance() );
        ProgramInstance programInstance = getProgramInstance( event.getEnrollment() );

        if ( organisationUnit == null )
        {
            return new ImportSummary( ImportStatus.ERROR, "Event.orgUnit does not point to a valid organisation unit: " + event.getOrgUnit() )
                .setReference( event.getEvent() ).incrementIgnored();
        }        

        if ( program == null )
        {
            return new ImportSummary( ImportStatus.ERROR,
                "Event.program does not point to a valid program: " + event.getProgram() ).setReference( event.getEvent() ).incrementIgnored();
        }
        
        programStage = program.isWithoutRegistration() && programStage == null ? program.getProgramStageByStage( 1 ) : programStage;

        if ( programStage == null )
        {
            return new ImportSummary( ImportStatus.ERROR, "Event.programStage does not point to a valid programStage: " + event.getProgramStage() );
        }

        if ( program.isRegistration() )
        {
            if ( entityInstance == null )
            {
                return new ImportSummary( ImportStatus.ERROR,
                    "Event.trackedEntityInstance does not point to a valid tracked entity instance: "
                        + event.getTrackedEntityInstance() ).setReference( event.getEvent() ).incrementIgnored();
            }
            
            if ( programInstance == null )
            {
                List<ProgramInstance> programInstances = new ArrayList<>( programInstanceService.getProgramInstances( entityInstance, program, ProgramStatus.ACTIVE ) );
                
                if ( programInstances.isEmpty() )
                {
                    return new ImportSummary( ImportStatus.ERROR, "Tracked entity instance: " + entityInstance.getUid() + " is not enrolled in program: " + program.getUid() ).setReference( event.getEvent() ).incrementIgnored();
                }
                else if ( programInstances.size() > 1 )
                {
                    return new ImportSummary( ImportStatus.ERROR, "Tracked entity instance: " + entityInstance.getUid() + " has multiple active enrollments in program: " + program.getUid() ).setReference( event.getEvent() ).incrementIgnored();
                }
                
                programInstance = programInstances.get( 0 );
            }            
            
            if ( !programStage.getRepeatable() && programInstance.hasActiveProgramStageInstance( programStage ) )
            {
                return new ImportSummary( ImportStatus.ERROR, "Program stage is not repeatable and an event already exists" )
                    .setReference( event.getEvent() ).incrementIgnored();
            }
        }
        else
        {
            String cacheKey = program.getUid() + "-" + ProgramStatus.ACTIVE;
            List<ProgramInstance> programInstances = getActiveProgramInstances( cacheKey, program );

            if ( programInstances.isEmpty() )
            {
                // Create PI if it doesn't exist (should only be one)

                String storedBy = getValidUsername( event.getStoredBy(), null, user );

                ProgramInstance pi = new ProgramInstance();
                pi.setEnrollmentDate( new Date() );
                pi.setIncidentDate( new Date() );
                pi.setProgram( program );
                pi.setStatus( ProgramStatus.ACTIVE );
                pi.setStoredBy( storedBy );

                programInstanceService.addProgramInstance( pi );

                programInstances.add( pi );
            }
            else if ( programInstances.size() > 1 )
            {
                return new ImportSummary( ImportStatus.ERROR,
                    "Multiple active program instances exists for program: " + program.getUid() ).setReference( event.getEvent() ).incrementIgnored();
            }

            programInstance = programInstances.get( 0 );

        }

        program = programInstance.getProgram();

        if ( programStageInstance != null )
        {
            programStage = programStageInstance.getProgramStage();
        }

        if ( !programInstance.getProgram().hasOrganisationUnit( organisationUnit ) )
        {
            return new ImportSummary( ImportStatus.ERROR,
                "Program is not assigned to this organisation unit: " + event.getOrgUnit() ).setReference( event.getEvent() ).incrementIgnored();
        }

        validateExpiryDays( event, program, null );

        List<String> errors = trackerAccessManager.canWrite( user, new ProgramStageInstance( programInstance, programStage )
            .setOrganisationUnit( organisationUnit ).setStatus( event.getStatus() ) );

        if ( !errors.isEmpty() )
        {
            return new ImportSummary( ImportStatus.ERROR, errors.toString() );
        }

        return saveEvent( program, programInstance, programStage, programStageInstance, organisationUnit, event, user,
            importOptions, bulkImport );
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    public Events getEvents( EventSearchParams params )
    {
        validate( params );

        List<OrganisationUnit> organisationUnits = getOrganisationUnits( params );

        if ( !params.isPaging() && !params.isSkipPaging() )
        {
            params.setDefaultPaging();
        }

        Events events = new Events();

        if ( params.isPaging() )
        {
            int count = 0;

            if ( params.isTotalPages() )
            {
                count = eventStore.getEventCount( params, organisationUnits );
            }

            Pager pager = new Pager( params.getPageWithDefault(), count, params.getPageSizeWithDefault() );
            events.setPager( pager );
        }

        List<Event> eventList = eventStore.getEvents( params, organisationUnits );
        events.setEvents( eventList );

        return events;
    }

    @Override
    public Grid getEventsGrid( EventSearchParams params )
    {

        if ( params.getProgramStage() == null )
        {
            throw new IllegalQueryException( "Program stage can not be null." );
        }

        List<OrganisationUnit> organisationUnits = getOrganisationUnits( params );

        // ---------------------------------------------------------------------
        // If no data element is specified, use those configured for
        // display in report
        // ---------------------------------------------------------------------
        if ( params.getDataElements().isEmpty() && params.getProgramStage() != null
            && params.getProgramStage().getProgramStageDataElements() != null )
        {
            for ( ProgramStageDataElement pde : params.getProgramStage().getProgramStageDataElements() )
            {
                if ( pde.getDisplayInReports() )
                {
                    QueryItem qi = new QueryItem( pde.getDataElement(), pde.getDataElement().getLegendSet(),
                        pde.getDataElement().getValueType(), pde.getDataElement().getAggregationType(),
                        pde.getDataElement().hasOptionSet() ? pde.getDataElement().getOptionSet() : null );
                    params.getDataElements().add( qi );
                }
            }
        }

        // ---------------------------------------------------------------------
        // Grid headers
        // ---------------------------------------------------------------------

        Grid grid = new ListGrid();

        for ( String col : STATIC_EVENT_COLUMNS )
        {
            grid.addHeader( new GridHeader( col, col ) );
        }

        for ( QueryItem item : params.getDataElements() )
        {
            grid.addHeader( new GridHeader( item.getItem().getUid(), item.getItem().getName() ) );
        }

        List<Map<String, String>> events = eventStore.getEventsGrid( params, organisationUnits );

        // ---------------------------------------------------------------------
        // Grid rows
        // ---------------------------------------------------------------------

        for ( Map<String, String> event : events )
        {
            grid.addRow();

            for ( String col : STATIC_EVENT_COLUMNS )
            {
                grid.addValue( event.get( col ) );
            }

            for ( QueryItem item : params.getDataElements() )
            {
                grid.addValue( event.get( item.getItemId() ) );
            }
        }

        Map<String, Object> metaData = new HashMap<>();

        if ( params.isPaging() )
        {
            int count = 0;

            if ( params.isTotalPages() )
            {
                count = eventStore.getEventCount( params, organisationUnits );
            }

            Pager pager = new Pager( params.getPageWithDefault(), count, params.getPageSizeWithDefault() );
            metaData.put( PAGER_META_KEY, pager );
        }

        grid.setMetaData( metaData );

        return grid;
    }

    @Override
    public int getAnonymousEventValuesCountLastUpdatedAfter( Date lastSuccessTime )
    {
        EventSearchParams params = buildAnonymousEventsSearchParams( lastSuccessTime );
        return eventStore.getEventCount( params, null );
    }

    @Override
    public Events getAnonymousEventValuesLastUpdatedAfter( Date lastSuccessTime )
    {
        EventSearchParams params = buildAnonymousEventsSearchParams( lastSuccessTime );
        Events anonymousEvents = new Events();
        List<Event> events = eventStore.getEvents( params, null );
        anonymousEvents.setEvents( events );
        return anonymousEvents;
    }

    private EventSearchParams buildAnonymousEventsSearchParams( Date lastSuccessTime )
    {
        EventSearchParams params = new EventSearchParams();
        params.setProgramType( ProgramType.WITHOUT_REGISTRATION );
        params.setLastUpdatedStartDate( lastSuccessTime );
        params.setIncludeDeleted( true );
        return params;
    }

    @Override
    public EventRows getEventRows( EventSearchParams params )
    {
        List<OrganisationUnit> organisationUnits = getOrganisationUnits( params );

        EventRows eventRows = new EventRows();

        List<EventRow> eventRowList = eventStore.getEventRows( params, organisationUnits );

        eventRows.setEventRows( eventRowList );

        return eventRows;
    }

    @Override
    public Event getEvent( ProgramStageInstance programStageInstance )
    {
        if ( programStageInstance == null )
        {
            return null;
        }

        programStageInstance = programStageInstanceService.getProgramStageInstance( programStageInstance.getUid() );

        Event event = new Event();

        event.setEvent( programStageInstance.getUid() );

        if ( programStageInstance.getProgramInstance().getEntityInstance() != null )
        {
            event.setTrackedEntityInstance( programStageInstance.getProgramInstance().getEntityInstance().getUid() );
        }

        event.setFollowup( programStageInstance.getProgramInstance().getFollowup() );
        event.setEnrollmentStatus(
            EnrollmentStatus.fromProgramStatus( programStageInstance.getProgramInstance().getStatus() ) );
        event.setStatus( programStageInstance.getStatus() );
        event.setEventDate( DateUtils.getIso8601NoTz( programStageInstance.getExecutionDate() ) );
        event.setDueDate( DateUtils.getIso8601NoTz( programStageInstance.getDueDate() ) );
        event.setStoredBy( programStageInstance.getStoredBy() );
        event.setCompletedBy( programStageInstance.getCompletedBy() );
        event.setCompletedDate( DateUtils.getIso8601NoTz( programStageInstance.getCompletedDate() ) );
        event.setCreated( DateUtils.getIso8601NoTz( programStageInstance.getCreated() ) );
        event.setCreatedAtClient( DateUtils.getIso8601NoTz( programStageInstance.getCreatedAtClient() ) );
        event.setLastUpdated( DateUtils.getIso8601NoTz( programStageInstance.getLastUpdated() ) );
        event.setLastUpdatedAtClient( DateUtils.getIso8601NoTz( programStageInstance.getLastUpdatedAtClient() ) );

        User user = currentUserService.getCurrentUser();
        OrganisationUnit ou = programStageInstance.getOrganisationUnit();

        List<String> errors = trackerAccessManager.canRead( user, programStageInstance );

        if ( !errors.isEmpty() )
        {
            throw new IllegalQueryException( errors.toString() );
        }

        if ( ou != null )
        {
            event.setOrgUnit( ou.getUid() );
            event.setOrgUnitName( ou.getName() );
        }

        Program program = programStageInstance.getProgramInstance().getProgram();

        event.setProgram( program.getUid() );
        event.setEnrollment( programStageInstance.getProgramInstance().getUid() );
        event.setProgramStage( programStageInstance.getProgramStage().getUid() );
        event.setAttributeOptionCombo( programStageInstance.getAttributeOptionCombo().getUid() );
        event.setAttributeCategoryOptions(
            String.join( ";", programStageInstance.getAttributeOptionCombo().getCategoryOptions().stream()
                .map( DataElementCategoryOption::getUid ).collect( Collectors.toList() ) ) );

        if ( programStageInstance.getProgramInstance().getEntityInstance() != null )
        {
            event.setTrackedEntityInstance( programStageInstance.getProgramInstance().getEntityInstance().getUid() );
        }

        if ( programStageInstance.getProgramStage().getCaptureCoordinates() )
        {
            Coordinate coordinate = null;

            if ( programStageInstance.getLongitude() != null && programStageInstance.getLatitude() != null )
            {
                coordinate = new Coordinate( programStageInstance.getLongitude(), programStageInstance.getLatitude() );

                try
                {
                    List<Double> list = OBJECT_MAPPER.readValue( coordinate.getCoordinateString(),
                        new TypeReference<List<Double>>()
                        {
                        } );

                    coordinate.setLongitude( list.get( 0 ) );
                    coordinate.setLatitude( list.get( 1 ) );
                }
                catch ( IOException ignored )
                {
                }
            }

            if ( coordinate != null && coordinate.isValid() )
            {
                event.setCoordinate( coordinate );
            }
        }

        Collection<TrackedEntityDataValue> dataValues = dataValueService
            .getTrackedEntityDataValues( programStageInstance );

        for ( TrackedEntityDataValue dataValue : dataValues )
        {
            errors = trackerAccessManager.canRead( user, dataValue );

            if ( !errors.isEmpty() )
            {
                continue;
            }

            DataValue value = new DataValue();
            value.setCreated( DateUtils.getIso8601NoTz( dataValue.getCreated() ) );
            value.setLastUpdated( DateUtils.getIso8601NoTz( dataValue.getLastUpdated() ) );
            value.setDataElement( dataValue.getDataElement().getUid() );
            value.setValue( dataValue.getValue() );
            value.setProvidedElsewhere( dataValue.getProvidedElsewhere() );
            value.setStoredBy( dataValue.getStoredBy() );

            event.getDataValues().add( value );
        }

        List<TrackedEntityComment> comments = programStageInstance.getComments();

        for ( TrackedEntityComment comment : comments )
        {
            Note note = new Note();

            note.setValue( comment.getCommentText() );
            note.setStoredBy( comment.getCreator() );

            if ( comment.getCreatedDate() != null )
            {
                note.setStoredDate( DateUtils.getIso8601NoTz( comment.getCreatedDate() ) );
            }

            event.getNotes().add( note );
        }

        return event;
    }

    @Override
    public EventSearchParams getFromUrl( String program, String programStage, ProgramStatus programStatus,
        Boolean followUp, String orgUnit, OrganisationUnitSelectionMode orgUnitSelectionMode,
        String trackedEntityInstance, Date startDate, Date endDate, Date dueDateStart, Date dueDateEnd,
        Date lastUpdatedStartDate, Date lastUpdatedEndDate, EventStatus status,
        DataElementCategoryOptionCombo attributeOptionCombo, IdSchemes idSchemes, Integer page, Integer pageSize,
        boolean totalPages, boolean skipPaging, List<Order> orders, List<String> gridOrders, boolean includeAttributes,
        Set<String> events, Set<String> filters, Set<String> dataElements, boolean includeDeleted )
    {
        User user = currentUserService.getCurrentUser();
        UserCredentials userCredentials = user.getUserCredentials();

        EventSearchParams params = new EventSearchParams();

        Program pr = programService.getProgram( program );

        if ( StringUtils.isNotEmpty( program ) && pr == null )
        {
            throw new IllegalQueryException( "Program is specified but does not exist: " + program );
        }

        ProgramStage ps = programStageService.getProgramStage( programStage );

        if ( StringUtils.isNotEmpty( programStage ) && ps == null )
        {
            throw new IllegalQueryException( "Program stage is specified but does not exist: " + programStage );
        }

        OrganisationUnit ou = organisationUnitService.getOrganisationUnit( orgUnit );

        if ( StringUtils.isNotEmpty( orgUnit ) && ou == null )
        {
            throw new IllegalQueryException( "Org unit is specified but does not exist: " + orgUnit );
        }

        if ( ou != null && !organisationUnitService.isInUserHierarchy( ou ) )
        {
            if ( !userCredentials.isSuper()
                && !userCredentials.isAuthorized( "F_TRACKED_ENTITY_INSTANCE_SEARCH_IN_ALL_ORGUNITS" ) )
            {
                throw new IllegalQueryException( "User has no access to organisation unit: " + ou.getUid() );
            }
        }

        if ( pr != null && !userCredentials.isSuper() && !aclService.canDataRead( user, pr ) )
        {
            throw new IllegalQueryException( "User has no access to program: " + pr.getUid() );
        }

        if ( ps != null && !userCredentials.isSuper() && !aclService.canDataRead( user, ps ) )
        {
            throw new IllegalQueryException( "User has no access to program stage: " + ps.getUid() );
        }

        if ( attributeOptionCombo != null && !userCredentials.isSuper() )
        {
            List<String> errors = trackerAccessManager.canRead( user, attributeOptionCombo );

            if ( !errors.isEmpty() )
            {
                throw new IllegalQueryException( errors.toString() );
            }
        }

        TrackedEntityInstance tei = entityInstanceService.getTrackedEntityInstance( trackedEntityInstance );

        if ( StringUtils.isNotEmpty( trackedEntityInstance ) && tei == null )
        {
            throw new IllegalQueryException( "Tracked entity instance is specified but does not exist: " + trackedEntityInstance );
        }

        if ( events != null && filters != null )
        {
            throw new IllegalQueryException( "Event UIDs and filters can not be specified at the same time" );
        }

        if ( events == null )
        {
            events = new HashSet<>();
        }

        if ( filters != null )
        {
            if ( StringUtils.isNotEmpty( programStage ) && ps == null )
            {
                throw new IllegalQueryException( "ProgramStage needs to be specified for event filtering to work" );
            }

            for ( String filter : filters )
            {
                QueryItem item = getQueryItem( filter );
                params.getFilters().add( item );
            }
        }

        if ( dataElements != null )
        {
            for ( String de : dataElements )
            {
                QueryItem dataElement = getQueryItem( de );

                params.getDataElements().add( dataElement );
            }
        }

        params.setProgram( pr );
        params.setProgramStage( ps );
        params.setOrgUnit( ou );
        params.setTrackedEntityInstance( tei );
        params.setProgramStatus( programStatus );
        params.setFollowUp( followUp );
        params.setOrgUnitSelectionMode( orgUnitSelectionMode );
        params.setStartDate( startDate );
        params.setEndDate( endDate );
        params.setDueDateStart( dueDateStart );
        params.setDueDateEnd( dueDateEnd );
        params.setLastUpdatedStartDate( lastUpdatedStartDate );
        params.setLastUpdatedEndDate( lastUpdatedEndDate );
        params.setEventStatus( status );
        params.setCategoryOptionCombo( attributeOptionCombo );
        params.setIdSchemes( idSchemes );
        params.setPage( page );
        params.setPageSize( pageSize );
        params.setTotalPages( totalPages );
        params.setSkipPaging( skipPaging );
        params.setIncludeAttributes( includeAttributes );
        params.setOrders( orders );
        params.setGridOrders( gridOrders );
        params.setEvents( events );
        params.setIncludeDeleted( includeDeleted );

        return params;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummaries updateEvents( List<Event> events, boolean singleValue, boolean clearSession )
    {
        ImportSummaries importSummaries = new ImportSummaries();

        User user = currentUserService.getCurrentUser();
        List<List<Event>> partitions = Lists.partition( events, FLUSH_FREQUENCY );

        for ( List<Event> _events : partitions )
        {
            prepareCaches( user, events );

            for ( Event event : _events )
            {
                importSummaries.addImportSummary( updateEvent( event, singleValue, null, true ) );
            }

            if ( clearSession && events.size() >= FLUSH_FREQUENCY )
            {
                clearSession( user );
            }
        }
        
        updateEntities( user );

        return importSummaries;
    }

    @Override
    public ImportSummary updateEvent( Event event, boolean singleValue, boolean bulkUpdate )
    {
        return updateEvent( event, singleValue, null, bulkUpdate );
    }

    @Override
    public ImportSummary updateEvent( Event event, boolean singleValue, ImportOptions importOptions, boolean bulkUpdate )
    {
        return updateEvent( event, currentUserService.getCurrentUser(), singleValue, importOptions, bulkUpdate );
    }

    private ImportSummary updateEvent( Event event, User user, boolean singleValue, ImportOptions importOptions, boolean bulkUpdate )
    {
        if ( importOptions == null )
        {
            importOptions = new ImportOptions();
        }

        ImportSummary importSummary = new ImportSummary( event.getEvent() );

        ProgramStageInstance programStageInstance = getProgramStageInstance( event.getEvent() );

        if ( programStageInstance == null )
        {
            importSummary.getConflicts().add( new ImportConflict( "Invalid Event ID.", event.getEvent() ) );
            return importSummary.incrementIgnored();
        }

        OrganisationUnit organisationUnit = getOrganisationUnit( importOptions.getIdSchemes(), event.getOrgUnit() );

        if ( organisationUnit == null )
        {
            organisationUnit = programStageInstance.getOrganisationUnit();
        }
        
        Program program = getProgram( importOptions.getIdSchemes().getProgramIdScheme(), event.getProgram() );

        if ( program == null )
        {
            return new ImportSummary( ImportStatus.ERROR, "Program '" + event.getProgram() + "' for event '" + event.getEvent() + "' was not found." );
        }

        List<String> errors = trackerAccessManager.canWrite( user, programStageInstance );

        if ( !errors.isEmpty() )
        {
            return new ImportSummary( ImportStatus.ERROR, errors.toString() );
        }

        Date executionDate = new Date();

        if ( event.getEventDate() != null )
        {
            executionDate = DateUtils.parseDate( event.getEventDate() );
            programStageInstance.setExecutionDate( executionDate );
        }

        Date dueDate = new Date();

        if ( event.getDueDate() != null )
        {
            dueDate = DateUtils.parseDate( event.getDueDate() );
        }

        String storedBy = getValidUsername( event.getStoredBy(), null, user );
        programStageInstance.setStoredBy( storedBy );

        String completedBy = getValidUsername( event.getCompletedBy(), null, user );

        if ( event.getStatus() != programStageInstance.getStatus() && programStageInstance.getStatus() == EventStatus.COMPLETED )
        {
            UserCredentials userCredentials = user != null ? user.getUserCredentials() : currentUserService.getCurrentUser().getUserCredentials();

            if ( !userCredentials.isSuper() && !userCredentials.isAuthorized( "F_UNCOMPLETE_EVENT" ) )
            {
                throw new IllegalQueryException( "User is not authorized to uncomplete events." );
            }
        }

        if ( event.getStatus() == EventStatus.ACTIVE )
        {
            programStageInstance.setStatus( EventStatus.ACTIVE );
            programStageInstance.setCompletedBy( null );
            programStageInstance.setCompletedDate( null );
        }
        else if ( programStageInstance.getStatus() != event.getStatus() && event.getStatus() == EventStatus.COMPLETED )
        {
            programStageInstance.setStatus( EventStatus.COMPLETED );
            programStageInstance.setCompletedBy( completedBy );
            programStageInstance.setCompletedDate( executionDate );

            Date completedDate = new Date();
            
            if ( event.getCompletedDate() != null )
            {
                completedDate = DateUtils.parseDate( event.getCompletedDate() );
            }
            programStageInstance.setCompletedDate( completedDate );
            programStageInstance.setStatus( EventStatus.COMPLETED );            
        }
        else if ( event.getStatus() == EventStatus.SKIPPED )
        {
            programStageInstance.setStatus( EventStatus.SKIPPED );
        }

        else if ( event.getStatus() == EventStatus.SCHEDULE )
        {
            programStageInstance.setStatus( EventStatus.SCHEDULE );
        }

        programStageInstance.setDueDate( dueDate );
        programStageInstance.setOrganisationUnit( organisationUnit );

        if ( !singleValue )
        {
            if ( programStageInstance.getProgramStage().getCaptureCoordinates() )
            {
                if ( event.getCoordinate() != null && event.getCoordinate().isValid() )
                {
                    programStageInstance.setLatitude( event.getCoordinate().getLatitude() );
                    programStageInstance.setLongitude( event.getCoordinate().getLongitude() );
                }
                else
                {
                    programStageInstance.setLatitude( null );
                    programStageInstance.setLongitude( null );
                }
            }
        }        

        validateExpiryDays( event, program, programStageInstance );

        DataElementCategoryOptionCombo aoc = null;

        if ( (event.getAttributeCategoryOptions() != null && program.getCategoryCombo() != null)
            || event.getAttributeOptionCombo() != null )
        {
            IdScheme idScheme = importOptions.getIdSchemes().getCategoryOptionIdScheme();

            try
            {
                aoc = getAttributeOptionCombo( program.getCategoryCombo(),
                    event.getAttributeCategoryOptions(), event.getAttributeOptionCombo(), idScheme );
            }
            catch ( IllegalQueryException ex )
            {
                importSummary.setStatus( ImportStatus.ERROR );
                importSummary.getConflicts().add( new ImportConflict( ex.getMessage(), event.getAttributeCategoryOptions() ) );
                return importSummary.incrementIgnored();
            }
        }

        if ( aoc != null && aoc.isDefault() && program.getCategoryCombo() != null && !program.getCategoryCombo().isDefault() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getConflicts().add( new ImportConflict( "attributeOptionCombo", "Default attribute option combo is not allowed since program has non-default category combo" ) );
            return importSummary.incrementIgnored();
        }
        
        if ( aoc != null )
        {
            programStageInstance.setAttributeOptionCombo( aoc );
        }

        programStageInstance.setDeleted( event.isDeleted() );

        saveTrackedEntityComment( programStageInstance, event, storedBy );        
        programStageInstanceService.updateProgramStageInstance( programStageInstance );        
        updateTrackedEntityInstance( programStageInstance, user, bulkUpdate );

        Set<TrackedEntityDataValue> dataValues = new HashSet<>(
            dataValueService.getTrackedEntityDataValues( programStageInstance ) );
        Map<String, TrackedEntityDataValue> existingDataValues = getDataElementDataValueMap( dataValues );

        for ( DataValue value : event.getDataValues() )
        {
            DataElement dataElement = getDataElement( importOptions.getIdSchemes().getDataElementIdScheme(),
                value.getDataElement() );
            TrackedEntityDataValue dataValue = dataValueService.getTrackedEntityDataValue( programStageInstance,
                dataElement );

            if ( !validateDataValue( programStageInstance, user, dataElement, value.getValue(), importSummary ) )
            {
                continue;
            }

            if ( dataValue != null )
            {
                if ( StringUtils.isEmpty( value.getValue() ) && dataElement.isFileType()
                    && !StringUtils.isEmpty( dataValue.getValue() ) )
                {
                    fileResourceService.deleteFileResource( dataValue.getValue() );
                }

                dataValue.setValue( value.getValue() );
                dataValue.setProvidedElsewhere( value.getProvidedElsewhere() );
                dataValueService.updateTrackedEntityDataValue( dataValue );

                dataValues.remove( dataValue );
            }
            else
            {
                TrackedEntityDataValue existingDataValue = existingDataValues.get( value.getDataElement() );

                saveDataValue( programStageInstance, event.getStoredBy(), dataElement, value.getValue(),
                    value.getProvidedElsewhere(), existingDataValue, null );
            }

            if ( !importOptions.isSkipNotifications() )
            {
                programRuleEngineService.evaluate( programStageInstance );
            }
        }

        if ( !singleValue )
        {
            dataValues.forEach( dataValueService::deleteTrackedEntityDataValue );
        }

        importSummary.setStatus( importSummary.getConflicts().isEmpty() ? ImportStatus.SUCCESS : ImportStatus.WARNING );

        return importSummary;
    }

    @Override
    public void updateEventForNote( Event event )
    {
        User user = currentUserService.getCurrentUser();
        
        ProgramStageInstance programStageInstance = programStageInstanceService
            .getProgramStageInstance( event.getEvent() );

        if ( programStageInstance == null )
        {
            return;
        }

        saveTrackedEntityComment( programStageInstance, event, getValidUsername( event.getStoredBy(), null, user ) );
        updateTrackedEntityInstance( programStageInstance, user, false );
    }

    @Override
    public void updateEventForEventDate( Event event )
    {
        ProgramStageInstance programStageInstance = programStageInstanceService
            .getProgramStageInstance( event.getEvent() );

        if ( programStageInstance == null )
        {
            return;
        }

        List<String> errors = trackerAccessManager.canWrite( currentUserService.getCurrentUser(), programStageInstance );
        if ( !errors.isEmpty() )
        {
            return;
        }
        
        Date executionDate = new Date();

        if ( event.getEventDate() != null )
        {
            executionDate = DateUtils.parseDate( event.getEventDate() );
        }

        if ( event.getStatus() == EventStatus.COMPLETED )
        {
            programStageInstance.setStatus( EventStatus.COMPLETED );
        }
        else
        {
            programStageInstance.setStatus( EventStatus.VISITED );
        }

        ImportOptions importOptions = new ImportOptions();

        OrganisationUnit organisationUnit = getOrganisationUnit( importOptions.getIdSchemes(), event.getOrgUnit() );

        if ( organisationUnit == null )
        {
            organisationUnit = programStageInstance.getOrganisationUnit();
        }

        programStageInstance.setOrganisationUnit( organisationUnit );
        programStageInstance.setExecutionDate( executionDate );
        programStageInstanceService.updateProgramStageInstance( programStageInstance );
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public ImportSummary deleteEvent( String uid )
    {
        ProgramStageInstance programStageInstance = programStageInstanceService.getProgramStageInstance( uid );

        if ( programStageInstance != null )
        {
            List<String> errors = trackerAccessManager.canWrite( currentUserService.getCurrentUser(), programStageInstance );

            if ( !errors.isEmpty() )
            {
                return new ImportSummary( ImportStatus.ERROR, errors.toString() );
            }
            
            programStageInstanceService.deleteProgramStageInstance( programStageInstance );
            return new ImportSummary( ImportStatus.SUCCESS, "Deletion of event " + uid + " was successful" )
                .incrementDeleted();
        }

        return new ImportSummary( ImportStatus.SUCCESS, "Event " + uid + " cannot be deleted as it is not present in the system" )
            .incrementIgnored();
    }

    @Override
    public ImportSummaries deleteEvents( List<String> uids )
    {
        User user = currentUserService.getCurrentUser();
        ImportSummaries importSummaries = new ImportSummaries();
        int counter = 0;

        for ( String uid : uids )
        {
            importSummaries.addImportSummary( deleteEvent( uid ) );

            if ( counter % FLUSH_FREQUENCY == 0 )
            {
                clearSession( user );
            }

            counter++;
        }

        return importSummaries;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private void prepareCaches( User user, List<Event> events )
    {
        // prepare caches
        Collection<String> orgUnits = events.stream().map( Event::getOrgUnit ).collect( Collectors.toSet() );
        Collection<String> programIds = events.stream().map( Event::getProgram ).collect( Collectors.toSet() );
        Collection<String> eventIds = events.stream().map( Event::getEvent ).collect( Collectors.toList() );

        if ( !orgUnits.isEmpty() )
        {
            Query query = Query.from( schemaService.getDynamicSchema( OrganisationUnit.class ) );
            query.setUser( user );
            query.add( Restrictions.in( "id", orgUnits ) );
            queryService.query( query ).forEach( ou -> organisationUnitCache.put( ou.getUid(), (OrganisationUnit) ou ) );
        }
        
        if ( !programIds.isEmpty() )
        {
            Query query = Query.from( schemaService.getDynamicSchema( Program.class ) );
            query.setUser( user );
            query.add( Restrictions.in( "id", programIds ) );
            
            List<Program> programs = (List<Program>) queryService.query( query );
            
            if ( !programs.isEmpty() )
            {
                for ( Program program : programs )
                {
                    programCache.put( program.getUid(), program );
                    programStageCache.putAll( program.getProgramStages().stream().collect( Collectors.toMap( ProgramStage::getUid, ps -> ps ) ) );
                    
                    for ( ProgramStage programStage : program.getProgramStages() )
                    {
                        dataElementCache.putAll( programStage.getAllDataElements().stream().collect( Collectors.toMap( DataElement::getUid, de -> de ) ) );
                    }
                }
            }
        }
        
        if ( !eventIds.isEmpty() )
        {
            eventSyncService.getEvents( (List<String>) eventIds ).forEach( psi -> programStageInstanceCache.put( psi.getUid(), ( ProgramStageInstance ) psi ) );
            
            manager.getObjects( TrackedEntityInstance.class, IdentifiableProperty.UID, 
                events.stream()
                .filter( event -> event.getTrackedEntityInstance() != null )
                .map( Event::getTrackedEntityInstance ).collect( Collectors.toSet() ) )
            .forEach( tei -> trackedEntityInstanceCache.put( tei.getUid(), (TrackedEntityInstance) tei ) );
            
            manager.getObjects( ProgramInstance.class, IdentifiableProperty.UID, 
                events.stream()
                .filter( event -> event.getEnrollment() != null )
                .map( Event::getEnrollment ).collect( Collectors.toSet() ) )
            .forEach( tei -> programInstanceCache.put( tei.getUid(), (ProgramInstance) tei ) );
        }
    }

    private List<OrganisationUnit> getOrganisationUnits( EventSearchParams params )
    {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();

        OrganisationUnit orgUnit = params.getOrgUnit();
        OrganisationUnitSelectionMode orgUnitSelectionMode = params.getOrgUnitSelectionMode();

        if ( params.getOrgUnit() != null )
        {
            if ( OrganisationUnitSelectionMode.DESCENDANTS.equals( orgUnitSelectionMode ) )
            {
                organisationUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( orgUnit.getUid() ) );
            }
            else if ( OrganisationUnitSelectionMode.CHILDREN.equals( orgUnitSelectionMode ) )
            {
                organisationUnits.add( orgUnit );
                organisationUnits.addAll( orgUnit.getChildren() );
            }
            else // SELECTED
            {
                organisationUnits.add( orgUnit );
            }
        }

        return organisationUnits;
    }

    private boolean validateDataValue( ProgramStageInstance programStageInstance, User user, DataElement dataElement, String value, ImportSummary importSummary )
    {
        String status = ValidationUtils.dataValueIsValid( value, dataElement );

        if ( status != null )
        {
            importSummary.getConflicts().add( new ImportConflict( dataElement.getUid(), status ) );
            importSummary.getImportCount().incrementIgnored();

            return false;
        }

        List<String> errors = trackerAccessManager.canWrite( user, new TrackedEntityDataValue( programStageInstance, dataElement, value ) );

        if ( !errors.isEmpty() )
        {
            errors.forEach( error -> importSummary.getConflicts().add( new ImportConflict( dataElement.getUid(), error ) ) );
            importSummary.getImportCount().incrementIgnored();
        }

        return true;
    }

    private ImportSummary saveEvent( Program program, ProgramInstance programInstance, ProgramStage programStage,
        ProgramStageInstance programStageInstance, OrganisationUnit organisationUnit, Event event, User user,
        ImportOptions importOptions, boolean bulkSave )
    {
        Assert.notNull( program, "Program cannot be null" );
        Assert.notNull( programInstance, "Program instance cannot be null" );
        Assert.notNull( programStage, "Program stage cannot be null" );

        ImportSummary importSummary = new ImportSummary( event.getEvent() );

        if ( importOptions == null )
        {
            importOptions = new ImportOptions();
        }

        boolean existingEvent = programStageInstance != null;
        boolean dryRun = importOptions.isDryRun();

        Date executionDate = null; // = new Date();

        if ( event.getEventDate() != null )
        {
            executionDate = DateUtils.parseDate( event.getEventDate() );
        }

        Date dueDate = new Date();

        if ( event.getDueDate() != null )
        {
            dueDate = DateUtils.parseDate( event.getDueDate() );
        }

        String storedBy = getValidUsername( event.getStoredBy(), importSummary, user );
        String completedBy = getValidUsername( event.getCompletedBy(), importSummary, user );

        DataElementCategoryOptionCombo aoc = null;

        if ( (event.getAttributeCategoryOptions() != null && program.getCategoryCombo() != null) || event.getAttributeOptionCombo() != null )
        {
            IdScheme idScheme = importOptions.getIdSchemes().getCategoryOptionIdScheme();

            try
            {
                aoc = getAttributeOptionCombo( program.getCategoryCombo(),
                    event.getAttributeCategoryOptions(), event.getAttributeOptionCombo(), idScheme );
            }
            catch ( IllegalQueryException ex )
            {
                importSummary.getConflicts().add( new ImportConflict( ex.getMessage(), event.getAttributeCategoryOptions() ) );
                importSummary.setStatus( ImportStatus.ERROR );
                return importSummary.incrementIgnored();
            }
        }
        else
        {
            aoc = ( DataElementCategoryOptionCombo ) getDefaultObject( DataElementCategoryOptionCombo.class );
        }

        if ( aoc != null && aoc.isDefault() && program.getCategoryCombo() != null && !program.getCategoryCombo().isDefault() )
        {
            importSummary.getConflicts().add( new ImportConflict( "attributeOptionCombo", "Default attribute option combo is not allowed since program has non-default category combo" ) );
            importSummary.setStatus( ImportStatus.ERROR );
            return importSummary.incrementIgnored();
        }

        List<String> errors = trackerAccessManager.canWrite( user, aoc );

        if ( !errors.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getConflicts().addAll( errors.stream().map( s -> new ImportConflict( "CategoryOptionCombo", s ) ).collect( Collectors.toList() ) );
            importSummary.incrementIgnored();

            return importSummary;
        }

        if ( !dryRun )
        {
            if ( programStageInstance == null )
            {
                programStageInstance = createProgramStageInstance( event, programStage, programInstance, organisationUnit,
                    dueDate, executionDate, event.getStatus().getValue(), event.getCoordinate(), completedBy, storedBy,
                    event.getEvent(), aoc, importOptions );
            }
            else
            {
                updateProgramStageInstance( event, programStage, programInstance, organisationUnit, dueDate, executionDate,
                    event.getStatus().getValue(), event.getCoordinate(), completedBy, programStageInstance, aoc,
                    importOptions );
            }
            
            updateTrackedEntityInstance( programStageInstance, user, bulkSave );
            importSummary.setReference( programStageInstance.getUid() );
        }

        Map<String, TrackedEntityDataValue> dataElementValueMap = Maps.newHashMap();

        if ( existingEvent )
        {
            dataElementValueMap = getDataElementDataValueMap(
                dataValueService.getTrackedEntityDataValues( programStageInstance ) );
        }

        for ( DataValue dataValue : event.getDataValues() )
        {
            DataElement dataElement;

            if ( dataElementValueMap.containsKey( dataValue.getDataElement() ) )
            {
                dataElement = dataElementValueMap.get( dataValue.getDataElement() ).getDataElement();
            }
            else
            {
                dataElement = getDataElement( importOptions.getIdSchemes().getDataElementIdScheme(),
                    dataValue.getDataElement() );
            }

            if ( dataElement != null )
            {
                if ( validateDataValue( programStageInstance, user, dataElement, dataValue.getValue(), importSummary ) )
                {
                    String dataValueStoredBy = dataValue.getStoredBy() != null ? dataValue.getStoredBy() : storedBy;

                    if ( !dryRun )
                    {
                        TrackedEntityDataValue existingDataValue = dataElementValueMap
                            .get( dataValue.getDataElement() );

                        saveDataValue( programStageInstance, dataValueStoredBy, dataElement, dataValue.getValue(),
                            dataValue.getProvidedElsewhere(), existingDataValue, importSummary );
                    }
                }
            }
            else
            {
                importSummary.getConflicts().add(
                    new ImportConflict( "dataElement", dataValue.getDataElement() + " is not a valid data element" ) );
                importSummary.getImportCount().incrementIgnored();
            }
        }

        sendProgramNotification( programStageInstance, importOptions );

        importSummary.setStatus( importSummary.getConflicts().isEmpty() ? ImportStatus.SUCCESS : ImportStatus.WARNING );

        return importSummary;
    }

    private void sendProgramNotification( ProgramStageInstance programStageInstance, ImportOptions importOptions )
    {
        if ( programStageInstance.isCompleted() )
        {
            if ( !importOptions.isSkipNotifications() )
            {
                programNotificationPublisher.publishEvent( programStageInstance, ProgramNotificationEventType.PROGRAM_STAGE_COMPLETION );
            }
        }
    }

    private void saveDataValue( ProgramStageInstance programStageInstance, String storedBy, DataElement dataElement,
        String value, Boolean providedElsewhere, TrackedEntityDataValue dataValue, ImportSummary importSummary )
    {
        if ( value != null && value.trim().length() == 0 )
        {
            value = null;
        }

        if ( value != null )
        {
            if ( dataValue == null )
            {
                dataValue = new TrackedEntityDataValue( programStageInstance, dataElement, value );
                dataValue.setStoredBy( storedBy );
                dataValue.setProvidedElsewhere( providedElsewhere );

                dataValueService.saveTrackedEntityDataValue( dataValue );

                programStageInstance.getDataValues().add( dataValue );

                if ( importSummary != null )
                {
                    importSummary.getImportCount().incrementImported();
                }
            }
            else
            {
                dataValue.setValue( value );
                dataValue.setStoredBy( storedBy );
                dataValue.setProvidedElsewhere( providedElsewhere );

                dataValueService.updateTrackedEntityDataValue( dataValue );

                if ( importSummary != null )
                {
                    importSummary.getImportCount().incrementUpdated();
                }
            }
        }
        else if ( dataValue != null )
        {
            dataValueService.deleteTrackedEntityDataValue( dataValue );

            if ( importSummary != null )
            {
                importSummary.getImportCount().incrementDeleted();
            }
        }
    }

    private ProgramStageInstance createProgramStageInstance( Event event, ProgramStage programStage, ProgramInstance programInstance,
        OrganisationUnit organisationUnit, Date dueDate, Date executionDate, int status, Coordinate coordinate,
        String completedBy, String storeBy, String programStageInstanceIdentifier, DataElementCategoryOptionCombo aoc,
        ImportOptions importOptions )
    {
        ProgramStageInstance programStageInstance = new ProgramStageInstance();
        if ( importOptions.getIdSchemes().getProgramStageInstanceIdScheme().equals( IdScheme.UID ) )
        {
            programStageInstance.setUid( CodeGenerator.isValidUid( programStageInstanceIdentifier ) ? programStageInstanceIdentifier
                : CodeGenerator.generateUid() );
        }
        else if ( importOptions.getIdSchemes().getProgramStageInstanceIdScheme().equals( IdScheme.CODE ) )
        {
            programStageInstance.setUid( CodeGenerator.generateUid() );
            programStageInstance.setCode( programStageInstanceIdentifier );
        }

        programStageInstance.setStoredBy( storeBy );

        updateProgramStageInstance( event, programStage, programInstance, organisationUnit, dueDate, executionDate, status,
            coordinate, completedBy, programStageInstance, aoc, importOptions );

        return programStageInstance;
    }

    private void updateProgramStageInstance( Event event, ProgramStage programStage, ProgramInstance programInstance,
        OrganisationUnit organisationUnit, Date dueDate, Date executionDate, int status, Coordinate coordinate,
        String completedBy, ProgramStageInstance programStageInstance, DataElementCategoryOptionCombo aoc,
        ImportOptions importOptions )
    {
        programStageInstance.setProgramInstance( programInstance );
        programStageInstance.setProgramStage( programStage );
        programStageInstance.setDueDate( dueDate );
        programStageInstance.setExecutionDate( executionDate );
        programStageInstance.setOrganisationUnit( organisationUnit );
        programStageInstance.setAttributeOptionCombo( aoc );
        programStageInstance.setDeleted( event.isDeleted() );

        if ( programStage.getCaptureCoordinates() )
        {
            if ( coordinate != null && coordinate.isValid() )
            {
                programStageInstance.setLongitude( coordinate.getLongitude() );
                programStageInstance.setLatitude( coordinate.getLatitude() );
            }
        }

        updateDateFields( event, programStageInstance );

        programStageInstance.setStatus( EventStatus.fromInt( status ) );
        
        saveTrackedEntityComment( programStageInstance, event, event.getStoredBy() );
        
        if ( programStageInstance.isCompleted() )
        {
            Date completedDate = new Date();
            if ( event.getCompletedDate() != null )
            {
                completedDate = DateUtils.parseDate( event.getCompletedDate() );
            }
            programStageInstance.setCompletedBy( completedBy );
            programStageInstance.setCompletedDate( completedDate );
        }

        if ( programStageInstance.getId() == 0 )
        {
            programStageInstance.setAutoFields();
            programStageInstanceService.addProgramStageInstance( programStageInstance );
        }
        else
        {
            programStageInstanceService.updateProgramStageInstance( programStageInstance );
        }        
    }

    private void saveTrackedEntityComment( ProgramStageInstance programStageInstance, Event event, String storedBy )
    {
        for ( Note note : event.getNotes() )
        {
            TrackedEntityComment comment = new TrackedEntityComment();
            comment.setCreator( storedBy );
            comment.setCreatedDate( new Date() );
            comment.setCommentText( note.getValue() );

            commentService.addTrackedEntityComment( comment );
            programStageInstance.getComments().add( comment );
        }
    }

    private String getValidUsername( String userName, ImportSummary importSummary, User fallbackUser )    

    {
        String validUsername = userName;

        if ( StringUtils.isEmpty( validUsername ) )
        {
            validUsername = User.getSafeUsername( fallbackUser );
        }
        else if ( validUsername.length() >= 31 )
        {
            if ( importSummary != null )
            {
                importSummary.getConflicts().add( new ImportConflict( "Username",
                    validUsername + " is more than 31 characters, using current username instead" ) );
            }

            validUsername = User.getSafeUsername( fallbackUser );
        }

        return validUsername;
    }

    private Map<String, TrackedEntityDataValue> getDataElementDataValueMap(
        Collection<TrackedEntityDataValue> dataValues )
    {
        return dataValues.stream().collect( Collectors.toMap( dv -> dv.getDataElement().getUid(), dv -> dv ) );
    }

    private OrganisationUnit getOrganisationUnit( IdSchemes idSchemes, String id )
    {
        return organisationUnitCache.get( id,
            () -> manager.getObject( OrganisationUnit.class, idSchemes.getOrgUnitIdScheme(), id ) );
    }

    private ProgramStageInstance getProgramStageInstance( String uid )
    {
        if ( uid == null )
        {
           return null; 
        }
        
        ProgramStageInstance programStageInstance = programStageInstanceCache.get( uid );
        
        if ( programStageInstance == null )
        {
            programStageInstance = eventSyncService.getEvent( uid );
            
            programStageInstanceCache.put( uid, programStageInstance );
        }
        
        return programStageInstance;
    }
    
    private ProgramInstance getProgramInstance( String uid )
    {
        if ( uid == null )
        {
            return null;
        }
        
        ProgramInstance programInstance = programInstanceCache.get( uid );
        
        if ( programInstance == null )
        {
            eventSyncService.getEnrollment( uid );
        }
        
        return programInstance;
    }
    
    private TrackedEntityInstance getTrackedEntityInstance( String uid )
    {        
        if ( uid == null )
        {
            return null;            
        }
        
        TrackedEntityInstance tei =  trackedEntityInstanceCache.get( uid );
        
        if ( tei == null )
        {
            tei = entityInstanceService.getTrackedEntityInstance( uid );
            
            trackedEntityInstanceCache.put( uid, tei );
        }
        
        return tei;
    }

    private Program getProgram( IdScheme idScheme, String id )
    {
        if ( id == null )
        {
            return null;
        }
        
        Program program = programCache.get( id );
        
        if ( program == null )
        {
            program = manager.getObject( Program.class, idScheme, id );
        
            if( program != null )
            {
                programCache.put( id, program );
                
                programStageCache.putAll( program.getProgramStages().stream().collect( Collectors.toMap( ProgramStage::getUid, ps -> ps ) ) );
                
                for ( ProgramStage programStage : program.getProgramStages() )
                {
                    dataElementCache.putAll( programStage.getAllDataElements().stream().collect( Collectors.toMap( DataElement::getUid, de -> de ) ) );
                }
            }
        }
        
        return program;
    }    

    private ProgramStage getProgramStage( IdScheme idScheme, String id )
    {
        if ( id == null )
        {
            return null;
        }
        
        ProgramStage programStage = programStageCache.get( id );
        
        if ( programStage == null )
        {
            programStage = manager.getObject( ProgramStage.class, idScheme, id );
            
            if ( programStage != null )
            {
                programStageCache.put( id, programStage );
                
                dataElementCache.putAll( programStage.getAllDataElements().stream().collect( Collectors.toMap( DataElement::getUid, de -> de ) ) );
            }
        }
        
        return programStage;
    }

    private DataElement getDataElement( IdScheme idScheme, String id )
    {
        return dataElementCache.get( id, () -> manager.getObject( DataElement.class, idScheme, id ) );
    }

    private DataElementCategoryOption getCategoryOption( IdScheme idScheme, String id )
    {
        return categoryOptionCache.get( id, () -> manager.getObject( DataElementCategoryOption.class, idScheme, id ) );
    }

    private DataElementCategoryOptionCombo getCategoryOptionCombo( IdScheme idScheme, String id )
    {
        return categoryOptionComboCache.get( id, () -> manager.getObject( DataElementCategoryOptionCombo.class, idScheme, id ) );
    }

    private DataElementCategoryOptionCombo getAttributeOptionCombo( String key, DataElementCategoryCombo categoryCombo,
        Set<DataElementCategoryOption> categoryOptions )
    {
        return attributeOptionComboCache.get( key, () -> categoryService.getDataElementCategoryOptionCombo( categoryCombo, categoryOptions ) );
    }

    private List<ProgramInstance> getActiveProgramInstances( String key, Program program )
    {
        return activeProgramInstanceCache.get( key, () -> {
            return programInstanceService.getProgramInstances( program, ProgramStatus.ACTIVE );
        } );
    }

    private IdentifiableObject getDefaultObject( Class<? extends IdentifiableObject> key )
    {
        return defaultObjectsCache.get( key, () -> manager.getByName( DataElementCategoryOptionCombo.class , "default" ) );
    }

    @Override
    public void validate( EventSearchParams params )
        throws IllegalQueryException
    {
        String violation = null;

        if ( params == null )
        {
            throw new IllegalQueryException( "Query parameters can not be empty." );
        }

        if ( params.getProgram() == null && params.getOrgUnit() == null && params.getTrackedEntityInstance() == null
            && params.getEvents().isEmpty() )
        {
            violation = "At least one of the following query parameters are required: orgUnit, program, trackedEntityInstance or event.";
        }

        if ( violation != null )
        {
            log.warn( "Validation failed: " + violation );

            throw new IllegalQueryException( violation );
        }
    }

    private void validateExpiryDays( Event event, Program program, ProgramStageInstance programStageInstance )
    {
        if ( program != null )
        {

            if ( program.getCompleteEventsExpiryDays() > 0 )
            {
                if ( event.getStatus() == EventStatus.COMPLETED
                    || programStageInstance != null && programStageInstance.getStatus() == EventStatus.COMPLETED )
                {
                    Date referenceDate = null;

                    if ( programStageInstance != null )
                    {
                        referenceDate = programStageInstance.getCompletedDate();
                    }

                    else
                    {
                        if ( event.getCompletedDate() != null )
                        {
                            referenceDate = DateUtils.parseDate( event.getCompletedDate() );
                        }
                    }

                    if ( referenceDate == null )
                    {
                        throw new IllegalQueryException( "Event needs to have completed date." );
                    }

                    if ( (new Date()).after(
                        DateUtils.getDateAfterAddition( referenceDate, program.getCompleteEventsExpiryDays() ) ) )
                    {
                        throw new IllegalQueryException(
                            "The event's completness date has expired. Not possible to make changes to this event" );
                    }
                }
            }

            PeriodType periodType = program.getExpiryPeriodType();

            if ( periodType != null && program.getExpiryDays() > 0 )
            {
                if ( programStageInstance != null )
                {
                    Date today = new Date();

                    if ( programStageInstance.getExecutionDate() == null )
                    {
                        throw new IllegalQueryException( "Event needs to have event date." );
                    }

                    Period period = periodType.createPeriod( programStageInstance.getExecutionDate() );

                    if ( today.after( DateUtils.getDateAfterAddition( period.getEndDate(), program.getExpiryDays() ) ) )
                    {
                        throw new IllegalQueryException(
                            "The program's expiry date has passed. It is not possible to make changes to this event." );
                    }
                }
                else
                {
                    String referenceDate = event.getEventDate() != null ? event.getEventDate()
                        : event.getDueDate() != null ? event.getDueDate() : null;

                    if ( referenceDate == null )
                    {
                        throw new IllegalQueryException(
                            "Event needs to have at least one (event or schedule) date. " );
                    }

                    Period period = periodType.createPeriod( new Date() );

                    if ( DateUtils.parseDate( referenceDate ).before( period.getStartDate() ) )
                    {
                        throw new IllegalQueryException(
                            "The event's date belongs to an expired period. It is not possble to create such event." );
                    }
                }
            }
        }
    }

    private QueryItem getQueryItem( String item )
    {
        String[] split = item.split( DimensionalObject.DIMENSION_NAME_SEP );

        if ( split == null || split.length % 2 != 1 )
        {
            throw new IllegalQueryException( "Query item or filter is invalid: " + item );
        }

        QueryItem queryItem = getItem( split[0] );

        if ( split.length > 1 )
        {
            for ( int i = 1; i < split.length; i += 2 )
            {
                QueryOperator operator = QueryOperator.fromString( split[i] );
                queryItem.getFilters().add( new QueryFilter( operator, split[i + 1] ) );
            }
        }

        return queryItem;
    }

    private QueryItem getItem( String item )
    {
        DataElement de = dataElementService.getDataElement( item );

        if ( de == null )
        {
            throw new IllegalQueryException( "Dataelement does not exist: " + item );
        }

        return new QueryItem( de, null, de.getValueType(), de.getAggregationType(), de.getOptionSet() );
    }
    
    private void updateEntities( User user )
    {
        programInstancesToUpdate.forEach( pi -> manager.update( pi, user ) );
        trackedEntityInstancesToUpdate.forEach( tei -> manager.update( tei, user ) );
        
        programInstancesToUpdate.clear();
        trackedEntityInstancesToUpdate.clear();
    }
    
    private void clearSession( User user )
    {        
        organisationUnitCache.clear();
        programCache.clear();
        programStageCache.clear();
        programStageInstanceCache.clear();
        programInstanceCache.clear();
        activeProgramInstanceCache.clear();
        trackedEntityInstanceCache.clear();
        dataElementCache.clear();
        categoryOptionCache.clear();
        categoryOptionComboCache.clear();
        attributeOptionComboCache.clear();
        defaultObjectsCache.clear();
        
        updateEntities( user );

        dbmsManager.clearSession();
    }
    
    private void updateDateFields( Event event, ProgramStageInstance programStageInstance )
    {
        programStageInstance.setAutoFields();

        Date createdAtClient = DateUtils.parseDate( event.getCreatedAtClient() );

        if ( createdAtClient != null )
        {
            programStageInstance.setCreatedAtClient( createdAtClient );
        }

        String lastUpdatedAtClient = event.getLastUpdatedAtClient();

        if ( lastUpdatedAtClient != null )
        {
            programStageInstance.setLastUpdatedAtClient( DateUtils.parseDate( lastUpdatedAtClient ) );
        }
    }

    private void updateTrackedEntityInstance( ProgramStageInstance programStageInstance, User user, boolean bulkUpdate )
    {
        updateTrackedEntityInstance( Lists.newArrayList( programStageInstance ), user, bulkUpdate );
    }

    private void updateTrackedEntityInstance( List<ProgramStageInstance> programStageInstances, User user, boolean bulkUpdate )
    {
        for ( ProgramStageInstance programStageInstance : programStageInstances )
        {
            if ( programStageInstance.getProgramInstance() != null )
            {
                if ( !bulkUpdate )
                {
                    manager.update( programStageInstance.getProgramInstance(), user );
                    
                    if ( programStageInstance.getProgramInstance().getEntityInstance() != null )
                    {                        
                        manager.update( programStageInstance.getProgramInstance().getEntityInstance(), user );
                    }                    
                }
                else
                {
                    programInstancesToUpdate.add( programStageInstance.getProgramInstance() );
                    
                    if ( programStageInstance.getProgramInstance().getEntityInstance() != null )
                    {                        
                        trackedEntityInstancesToUpdate.add( programStageInstance.getProgramInstance().getEntityInstance() );
                    }
                }
            }
        }
    }

    private DataElementCategoryOptionCombo getAttributeOptionCombo( DataElementCategoryCombo categoryCombo, String cp,
        String attributeOptionCombo, IdScheme idScheme )
    {
        Set<String> opts = TextUtils.splitToArray( cp, TextUtils.SEMICOLON );

        return getAttributeOptionCombo( categoryCombo, opts, attributeOptionCombo, idScheme );
    }

    private DataElementCategoryOptionCombo getAttributeOptionCombo( DataElementCategoryCombo categoryCombo, Set<String> opts,
        String attributeOptionCombo, IdScheme idScheme )
    {
        if ( categoryCombo == null )
        {
            throw new IllegalQueryException( "Illegal category combo" );
        }

        // ---------------------------------------------------------------------
        // Attribute category options validation
        // ---------------------------------------------------------------------

        DataElementCategoryOptionCombo attrOptCombo = null;

        if ( opts != null )
        {
            Set<DataElementCategoryOption> categoryOptions = new HashSet<>();

            for ( String uid : opts )
            {
                DataElementCategoryOption categoryOption = getCategoryOption( idScheme, uid );

                if ( categoryOption == null )
                {
                    throw new IllegalQueryException( "Illegal category option identifier: " + uid );
                }

                categoryOptions.add( categoryOption );
            }

            List<String> options = Lists.newArrayList( opts );
            Collections.sort( options );

            String cacheKey = categoryCombo.getUid() + "-" + Joiner.on( "-" ).join( options );
            attrOptCombo = getAttributeOptionCombo( cacheKey, categoryCombo, categoryOptions );

            if ( attrOptCombo == null )
            {
                throw new IllegalQueryException( "Attribute option combo does not exist for given category combo and category options" );
            }
        }
        else if ( attributeOptionCombo != null )
        {
            attrOptCombo = getCategoryOptionCombo( idScheme, attributeOptionCombo );
        }

        // ---------------------------------------------------------------------
        // Fall back to default category option combination
        // ---------------------------------------------------------------------

        if ( attrOptCombo == null )
        {
            attrOptCombo = ( DataElementCategoryOptionCombo ) getDefaultObject( DataElementCategoryOptionCombo.class );
        }

        if ( attrOptCombo == null )
        {
            throw new IllegalQueryException( "Default attribute option combo does not exist" );
        }

        return attrOptCombo;
    }
}
