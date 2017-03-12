package org.hisp.dhis.dxf2.metadata;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.dxf2.common.JacksonUtils;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.junit.Test;

import java.io.ByteArrayInputStream;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class JacksonUtilsTest
{
    @Test
    public void serializableTest() throws Exception
    {
        JacksonUtils.toJsonAsString( new Metadata() );
        JacksonUtils.toXmlAsString( new Metadata() );
        JacksonUtils.toJsonWithViewAsString( new Metadata(), DetailedView.class );
        JacksonUtils.toXmlWithViewAsString( new Metadata(), DetailedView.class );
        JacksonUtils.toJsonWithViewAsString( new Metadata(), ExportView.class );
        JacksonUtils.toXmlWithViewAsString( new Metadata(), ExportView.class );
    }

    @Test
    public void deserializableTest() throws Exception
    {
        JacksonUtils.fromJson( "{}", Metadata.class );
        JacksonUtils.fromXml( "<?xml version='1.0' encoding='UTF-8'?><metaData xmlns=\"http://dhis2.org/schema/dxf/2.0\"/>", Metadata.class );
    }

    @Test
    public void testDeserialization() throws Exception {
        String str = "{\"httpStatus\":\"OK\",\"httpStatusCode\":200,\"status\":\"SUCCESS\",\"message\":\"Import was successful.\",\"response\":{\"responseType\":\"ImportSummaries\",\"imported\":0,\"updated\":2,\"deleted\":0,\"ignored\":0," +
            "\"importSummaries\":[{\"responseType\":\"ImportSummary\",\"status\":\"SUCCESS\",\"importCount\":{\"imported\":0,\"updated\":2,\"ignored\":0,\"deleted\":0},\"reference\":\"sgsVBNSGPih\",\"href\":\"http://msfocamdsynchq.twhosted" +
            ".com/api/events/sgsVBNSGPih\"}]}}";

        ObjectMapper objectMapper = new ObjectMapper( );
        objectMapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
        objectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
        objectMapper.configure( SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false );
        objectMapper.configure( SerializationFeature.FAIL_ON_EMPTY_BEANS, false );
        objectMapper.configure( SerializationFeature.WRAP_EXCEPTIONS, true );

        objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        objectMapper.configure( DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true );
        objectMapper.configure( DeserializationFeature.WRAP_EXCEPTIONS, true );

        objectMapper.disable( MapperFeature.AUTO_DETECT_FIELDS );
        objectMapper.disable( MapperFeature.AUTO_DETECT_CREATORS );
        objectMapper.disable( MapperFeature.AUTO_DETECT_GETTERS );
        objectMapper.disable( MapperFeature.AUTO_DETECT_SETTERS );
        objectMapper.disable( MapperFeature.AUTO_DETECT_IS_GETTERS );

        objectMapper.readValue(new ByteArrayInputStream( str.getBytes("UTF-8") ), new TypeReference<ImportSummaries>(){} );

        JsonNode objectNode = objectMapper.readTree( str );
        JsonNode responseNode = objectNode.get( "response" );
        
        objectMapper.readValue(responseNode.toString(), ImportSummaries.class);
    }
}
