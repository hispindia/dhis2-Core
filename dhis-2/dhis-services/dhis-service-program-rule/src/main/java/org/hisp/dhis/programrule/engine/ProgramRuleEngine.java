/*
 * Copyright (c) 2004-2021, University of Oslo
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
package org.hisp.dhis.programrule.engine;

import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.math3.util.Pair;
import org.hisp.dhis.commons.collection.ListUtils;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.programrule.ProgramRule;
import org.hisp.dhis.programrule.ProgramRuleVariable;
import org.hisp.dhis.programrule.ProgramRuleVariableService;
import org.hisp.dhis.rules.DataItem;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleEngineIntent;
import org.hisp.dhis.rules.models.*;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;

import com.google.api.client.util.Lists;

/**
 * @author Zubair Asghar
 */
@Slf4j
@RequiredArgsConstructor
public class ProgramRuleEngine
{
    @NonNull
    private final ProgramRuleEntityMapperService programRuleEntityMapperService;

    @NonNull
    private final ProgramRuleVariableService programRuleVariableService;

    @NonNull
    private final ConstantService constantService;

    @NonNull
    private final ImplementableRuleService implementableRuleService;

    @NonNull
    private final SupplementaryDataProvider supplementaryDataProvider;

    public List<RuleEffect> evaluateOldOne( ProgramInstance enrollment, Set<ProgramStageInstance> events )
    {
        return evaluateProgramRules( enrollment, null, events, enrollment.getProgram(), Lists.newArrayList() );
    }

    public List<RuleEffectByObject> evaluateNTI( ProgramInstance enrollment, Set<ProgramStageInstance> events,
        List<TrackedEntityAttributeValue> trackedEntityAttributeValues )
    {
        return evaluateProgramRulesNTI( enrollment, events, enrollment.getProgram(), trackedEntityAttributeValues );
    }

    public List<RuleEffectByObject> evaluateProgramEventNTI( Set<ProgramStageInstance> events, Program program )
    {
        return evaluateProgramRulesNTI( null, events, program, Lists.newArrayList() );
    }

    public List<RuleEffect> evaluateOldOne( ProgramInstance enrollment, ProgramStageInstance programStageInstance,
        Set<ProgramStageInstance> events )
    {
        return evaluateProgramRules( enrollment, programStageInstance, events, enrollment.getProgram(),
            Lists.newArrayList() );
    }

    private List<RuleEffect> evaluateProgramRules( ProgramInstance enrollment,
        ProgramStageInstance programStageInstance, Set<ProgramStageInstance> events, Program program,
        List<TrackedEntityAttributeValue> trackedEntityAttributeValues )
    {
        String programStageUid = Optional.ofNullable( programStageInstance ).map( p -> p.getProgramStage().getUid() )
            .orElse( null );

        List<ProgramRule> programRules = implementableRuleService.getProgramRules( program, programStageUid );

        if ( programRules.isEmpty() )
        {
            return Collections.emptyList();
        }

        List<RuleEvent> ruleEvents = getRuleEvents( events, programStageInstance );

        RuleEnrollment ruleEnrollment = getRuleEnrollment( enrollment, trackedEntityAttributeValues );

        try
        {
            RuleEngine.Builder builder = getRuleEngineContext( program,
                programRules )
                    .toEngineBuilder()
                    .triggerEnvironment( TriggerEnvironment.SERVER )
                    .events( ruleEvents );

            if ( ruleEnrollment != null )
            {
                builder.enrollment( ruleEnrollment );
            }

            RuleEngine ruleEngine = builder.build();

            return getRuleEngineEvaluation( ruleEngine, ruleEnrollment,
                programStageInstance );
        }
        catch ( Exception e )
        {
            log.error( DebugUtils.getStackTrace( e ) );
            return Collections.emptyList();
        }
    }

    private List<RuleEffectByObject> evaluateProgramRulesNTI( ProgramInstance enrollment,
        Set<ProgramStageInstance> events, Program program,
        List<TrackedEntityAttributeValue> trackedEntityAttributeValues )
    {

        List<RuleEffectByObject> ruleEffects = Lists.newArrayList();

        if ( enrollment != null )
        {
            List<RuleEffect> effects = evaluateProgramRules( enrollment, null, events, program,
                trackedEntityAttributeValues );

            RuleEffectByObject ruleEffectsForEnrollment = RuleEffectByObject
                .ruleEffectForEnrollment( enrollment.getUid(), effects );

            ruleEffects.add( ruleEffectsForEnrollment );
        }

        List<RuleEffectByObject> ruleEffectsForEvents = events.stream()
            .map( event -> Pair
                .create( event.getUid(), evaluateProgramRules( enrollment, event, events, program,
                    trackedEntityAttributeValues ) ) )
            .map( pair -> RuleEffectByObject.ruleEffectForEvent( pair.getFirst(), pair.getSecond() ) )
            .collect( Collectors.toList() );

        ruleEffects.addAll( ruleEffectsForEvents );

        return ruleEffects;
    }

    /**
     * To getDescription rule condition in order to fetch its description
     *
     * @param condition of program rule
     * @param program {@link Program} which the programRule is associated with.
     * @return RuleValidationResult contains description of program rule
     *         condition or errorMessage
     */
    public RuleValidationResult getDescription( String condition, Program program )
    {
        if ( program == null )
        {
            log.error( "Program cannot be null" );
            return RuleValidationResult.builder().isValid( false ).errorMessage( "Program cannot be null" ).build();
        }

        List<ProgramRuleVariable> programRuleVariables = programRuleVariableService.getProgramRuleVariable( program );

        RuleEngine ruleEngine = ruleEngineBuilder( ListUtils.newList(), programRuleVariables,
            RuleEngineIntent.DESCRIPTION ).build();

        return ruleEngine.evaluate( condition );
    }

    private RuleEngineContext getRuleEngineContext( Program program, List<ProgramRule> programRules )
    {
        List<ProgramRuleVariable> programRuleVariables = programRuleVariableService
            .getProgramRuleVariable( program );

        Map<String, String> constantMap = constantService.getConstantMap().entrySet()
            .stream()
            .collect( Collectors.toMap( Map.Entry::getKey, v -> v.getValue().toString() ) );

        Map<String, List<String>> supplementaryData = supplementaryDataProvider.getSupplementaryData( programRules );

        return RuleEngineContext.builder()
            .supplementaryData( supplementaryData )
            .rules( programRuleEntityMapperService.toMappedProgramRules( programRules ) )
            .ruleVariables( programRuleEntityMapperService.toMappedProgramRuleVariables( programRuleVariables ) )
            .constantsValue( constantMap )
            .build();
    }

    private RuleEngine.Builder ruleEngineBuilder( List<ProgramRule> programRules,
        List<ProgramRuleVariable> programRuleVariables, RuleEngineIntent intent )
    {
        Map<String, String> constantMap = constantService.getConstantMap().entrySet()
            .stream()
            .collect( Collectors.toMap( Map.Entry::getKey, v -> v.getValue().toString() ) );

        Map<String, List<String>> supplementaryData = supplementaryDataProvider.getSupplementaryData( programRules );

        if ( RuleEngineIntent.DESCRIPTION == intent )
        {
            Map<String, DataItem> itemStore = programRuleEntityMapperService.getItemStore( programRuleVariables );

            return RuleEngineContext.builder()
                .supplementaryData( supplementaryData )
                .rules( programRuleEntityMapperService.toMappedProgramRules( programRules ) )
                .ruleVariables( programRuleEntityMapperService.toMappedProgramRuleVariables( programRuleVariables ) )
                .constantsValue( constantMap ).ruleEngineItent( intent ).itemStore( itemStore )
                .build()
                .toEngineBuilder()
                .triggerEnvironment( TriggerEnvironment.SERVER );
        }
        else
        {
            return RuleEngineContext.builder()
                .supplementaryData( supplementaryData )
                .rules( programRuleEntityMapperService.toMappedProgramRules( programRules ) )
                .ruleVariables( programRuleEntityMapperService.toMappedProgramRuleVariables( programRuleVariables ) )
                .constantsValue( constantMap ).ruleEngineItent( intent )
                .build()
                .toEngineBuilder()
                .triggerEnvironment( TriggerEnvironment.SERVER );
        }
    }

    private RuleEvent getRuleEvent( ProgramStageInstance programStageInstance )
    {
        return programRuleEntityMapperService.toMappedRuleEvent( programStageInstance );
    }

    private List<RuleEvent> getRuleEvents( Set<ProgramStageInstance> events,
        ProgramStageInstance programStageInstance )
    {
        return programRuleEntityMapperService.toMappedRuleEvents( events, programStageInstance );
    }

    private RuleEnrollment getRuleEnrollment( ProgramInstance enrollment,
        List<TrackedEntityAttributeValue> trackedEntityAttributeValues )
    {
        return programRuleEntityMapperService.toMappedRuleEnrollment( enrollment, trackedEntityAttributeValues );
    }

    private List<RuleEffect> getRuleEngineEvaluation( RuleEngine ruleEngine, RuleEnrollment enrollment,
        ProgramStageInstance event )
        throws Exception
    {
        if ( event == null )
        {
            return ruleEngine.evaluate( enrollment ).call();
        }
        else
        {
            return ruleEngine.evaluate( getRuleEvent( event ) ).call();
        }
    }
}
