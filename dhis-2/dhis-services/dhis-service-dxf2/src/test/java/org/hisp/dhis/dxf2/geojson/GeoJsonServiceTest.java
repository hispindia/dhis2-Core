/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.dxf2.geojson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * For now just make sure that a {@link GeoJsonImportReport} can be stored in
 * redis.
 *
 * @author Jan Bernitt
 */
class GeoJsonServiceTest extends DhisSpringTest
{
    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    void testReportSerialisation()
        throws JsonProcessingException
    {
        GeoJsonImportReport report = new GeoJsonImportReport();
        report.getImportCount().incrementIgnored();
        report.addConflict( new ImportConflict( "a", "b" ) );

        String json = jsonMapper.writeValueAsString( report );
        GeoJsonImportReport reportFromJson = jsonMapper.readValue( json, GeoJsonImportReport.class );

        assertEquals( report.getConflictCount(), reportFromJson.getConflictCount() );
        assertEquals( report.getImportCount().getIgnored(), reportFromJson.getImportCount().getIgnored() );
        assertEquals( report.getTotalConflictOccurrenceCount(), reportFromJson.getTotalConflictOccurrenceCount() );
        assertIterableEquals( report.getConflicts(), reportFromJson.getConflicts() );
    }
}
