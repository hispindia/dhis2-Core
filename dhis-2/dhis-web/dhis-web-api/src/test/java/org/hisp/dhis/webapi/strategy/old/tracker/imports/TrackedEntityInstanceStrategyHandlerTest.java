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
package org.hisp.dhis.webapi.strategy.old.tracker.imports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.webapi.controller.exception.BadRequestException;
import org.hisp.dhis.webapi.strategy.old.tracker.imports.impl.TrackedEntityInstanceAsyncStrategyImpl;
import org.hisp.dhis.webapi.strategy.old.tracker.imports.impl.TrackedEntityInstanceStrategyImpl;
import org.hisp.dhis.webapi.strategy.old.tracker.imports.impl.TrackedEntityInstanceSyncStrategyImpl;
import org.hisp.dhis.webapi.strategy.old.tracker.imports.request.TrackerEntityInstanceRequest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Luca Cambi <luca@dhis2.org>
 */
public class TrackedEntityInstanceStrategyHandlerTest
{

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @InjectMocks
    private TrackedEntityInstanceStrategyImpl trackedEntityInstanceStrategyHandler;

    @Mock
    private TrackedEntityInstanceAsyncStrategyImpl trackedEntityInstanceAsyncStrategy;

    @Mock
    private TrackedEntityInstanceSyncStrategyImpl trackedEntityInstanceSyncStrategy;

    @Mock
    private ImportOptions importOptions;

    @Test
    public void shouldCallSyncTrackedEntitySyncStrategy()
        throws IOException,
        BadRequestException
    {
        when( importOptions.isAsync() ).thenReturn( false );

        TrackerEntityInstanceRequest trackerEntityInstanceRequest = TrackerEntityInstanceRequest.builder()
            .mediaType( MediaType.APPLICATION_JSON ).importOptions( importOptions ).build();

        trackedEntityInstanceStrategyHandler.mergeOrDeleteTrackedEntityInstances( trackerEntityInstanceRequest );

        verify( trackedEntityInstanceAsyncStrategy, times( 0 ) ).mergeOrDeleteTrackedEntityInstances( any() );
        verify( trackedEntityInstanceSyncStrategy, times( 1 ) ).mergeOrDeleteTrackedEntityInstances( any() );
    }

    @Test
    public void shouldCallAsyncTrackedEntitySyncStrategy()
        throws IOException,
        BadRequestException
    {
        when( importOptions.isAsync() ).thenReturn( true );

        TrackerEntityInstanceRequest trackerEntityInstanceRequest = TrackerEntityInstanceRequest.builder()
            .mediaType( MediaType.APPLICATION_JSON ).importOptions( importOptions ).build();

        trackedEntityInstanceStrategyHandler.mergeOrDeleteTrackedEntityInstances( trackerEntityInstanceRequest );

        verify( trackedEntityInstanceAsyncStrategy, times( 1 ) ).mergeOrDeleteTrackedEntityInstances( any() );
        verify( trackedEntityInstanceSyncStrategy, times( 0 ) ).mergeOrDeleteTrackedEntityInstances( any() );
    }
}