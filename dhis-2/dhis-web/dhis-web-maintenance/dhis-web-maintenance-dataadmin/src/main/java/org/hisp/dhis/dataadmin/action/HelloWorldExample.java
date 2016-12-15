/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The simplest possible servlet.
 * 
 * @author James Duncan Davidson
 */

public class HelloWorldExample
    extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws IOException, ServletException
    {
        ResourceBundle rb = ResourceBundle.getBundle( "LocalStrings", request.getLocale() );
        response.setContentType( "text/html" );

        Map<String, String> gujratiTranslationMap = new HashMap<String, String>();

        gujratiTranslationMap.put( "PERFECT_MESSAGE", "આભાર! તમે" );
        gujratiTranslationMap.put( "male", "પુરુષ" );
        gujratiTranslationMap.put( "female", "સ્ત્રી" );
        gujratiTranslationMap.put( "sideEffect", "સાઇડ ઇફેક્ટ્સ" );
        gujratiTranslationMap.put( "sent", "મોકલવામાં" );

        PrintWriter out = response.getWriter();

        String responseMessage = "";

        String mobileNo = "";
        String smsMessage = "";
        String smsDate = "";

        Map<String, String[]> params = new HashMap<String, String[]>( request.getParameterMap() );

        if ( params != null && params.size() > 0 )
        {
            for ( String key : params.keySet() )
            {
                if ( key.equalsIgnoreCase( "mobileno" ) )
                {
                    mobileNo = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "message" ) )
                {
                    smsMessage = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase( "date" ) )
                {
                    smsDate = ((String[]) params.get( key ))[0];
                }
            }

            /*
             * Iterator<String> it = params.keySet().iterator();
             * 
             * while ( it.hasNext() ) { String key = (String) it.next(); String
             * messageValue = ((String[]) params.get( key ))[ 0 ];
             * 
             * if( key == "mobileno") { mobileNo = messageValue; } if( key ==
             * "message") { smsMessage = messageValue; }
             * 
             * System.out.println( key + "---" + messageValue );
             * 
             * 
             * }
             */
            responseMessage = sendSMS( mobileNo, smsMessage, smsDate, gujratiTranslationMap );

        }

        out.println( responseMessage );

    }

    public String sendSMS( String mobileNo, String message, String smsDate, Map<String, String> gujratiTranslationMap )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        System.out.println( mobileNo + " -- 1 -- " + message );
        // System.out.println(encodeMessage(new String(message.getBytes())));
        try
        {
            // Construct data

            String[] tempMessage = message.split( "," );

            message = gujratiTranslationMap.get( "PERFECT_MESSAGE" ) + " " + gujratiTranslationMap.get( "male" ) + "("
                + tempMessage[0] + "," + tempMessage[1] + "," + tempMessage[2] + ")"
                + gujratiTranslationMap.get( "female" ) + "(" + tempMessage[3] + "," + tempMessage[4] + ","
                + tempMessage[5] + ")" + gujratiTranslationMap.get( "sideEffect" ) + "(" + tempMessage[6] + ") "
                + smsDate;

            String user = "username=" + "harsh.atal@gmail.com";
            String hash = "&hash=" + "04fa1b5546432e99162704a7025403879d589271";
            message = "&message=" + message;
            String sender = "&sender=" + "TXTLCL";
            // String numbers = "&numbers=" + "919654232779&unicode=1&test=1";
            String numbers = "&numbers=" + mobileNo + "&unicode=1";

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL( "http://api.textlocal.in/send/?" ).openConnection();
            String data = user + hash + message + sender + numbers;
            conn.setDoOutput( true );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Length", Integer.toString( data.length() ) );
            conn.getOutputStream().write( data.getBytes( "UTF-8" ) );
            final BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ( (line = rd.readLine()) != null )
            {
                stringBuffer.append( line );
            }

            rd.close();

            resopnseString = stringBuffer.toString();

            System.out.println( stringBuffer.toString() );
        }

        catch ( Exception e )
        {
            System.out.println( "Error SMS " + e );
        }

        return resopnseString;

    }

}
