package org.hisp.dhis.db.migration.v32;

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.hisp.dhis.scheduling.JobParameters;
import org.hisp.dhis.scheduling.JobType;
import org.hisp.dhis.scheduling.parameters.PushAnalysisJobParameters;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @Author Zubair Asghar.
 */
public class V2_32_33__Convert_push_analysis_job_parameters_into_list_of_string extends BaseJavaMigration
{
    private static final Log log = LogFactory.getLog( V2_32_33__Convert_push_analysis_job_parameters_into_list_of_string.class );

    @Override
    public void migrate( Context context ) throws Exception
    {
        String pushAnalysisUid = null;

        try ( Statement statement = context.getConnection().createStatement() )
        {
            try ( ResultSet resultSet = statement.executeQuery( "select jsonb_typeof(jsonbjobparameters) from jobconfiguration where jobtype is not null AND encode(jobtype,'hex') ='aced00057e7200206f72672e686973702e646869732e7363686564756c696e672e4a6f625479706500000000000000001200007872000e6a6176612e6c616e672e456e756d0000000000000000120000787074000d505553485f414e414c59534953';" ) )
            {
                if ( resultSet.next() )
                {
                    String dataType = StringUtils.trimToEmpty( resultSet.getString( 1 ) );

                    if ( "array".equals( dataType ) )
                    {
                        // Skip this migration because parameter is already converted from String to List
                        return;
                    }
                }
            }

            try ( ResultSet resultSet = statement.executeQuery( "select jsonbjobparameters->1->'pushAnalysis' from jobconfiguration where jobtype is not null AND encode(jobtype,'hex') ='aced00057e7200206f72672e686973702e646869732e7363686564756c696e672e4a6f625479706500000000000000001200007872000e6a6176612e6c616e672e456e756d0000000000000000120000787074000d505553485f414e414c59534953';" ) )
            {
                if ( resultSet.next() )
                {
                    pushAnalysisUid = resultSet.getString( 1 );
                    pushAnalysisUid = StringUtils.strip( pushAnalysisUid, "\"" );
                }
            }
        }

        if ( pushAnalysisUid != null )
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping();
            mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );

            JavaType resultingJavaType = mapper.getTypeFactory().constructType( JobParameters.class );
            ObjectWriter writer = mapper.writerFor( resultingJavaType );

            try ( PreparedStatement ps = context.getConnection().prepareStatement( "UPDATE jobconfiguration SET jsonbjobparameters = ? where  jobtype = ?;" ) )
            {
                PushAnalysisJobParameters jobParameters = new PushAnalysisJobParameters( pushAnalysisUid );

                PGobject pg = new PGobject();
                pg.setType( "jsonb" );
                pg.setValue( writer.writeValueAsString( jobParameters ) );

                ps.setObject( 1, pg );
                ps.setString( 2, "aced00057e7200206f72672e686973702e646869732e7363686564756c696e672e4a6f625479706500000000000000001200007872000e6a6176612e6c616e672e456e756d0000000000000000120000787074000d505553485f414e414c59534953" );

                ps.execute();

                log.info( "JobType " + JobType.PUSH_ANALYSIS.name() + " has been updated." );
            }
        }
    }
}
