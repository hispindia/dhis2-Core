package org.hisp.dhis.dataadmin.action;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class NoAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    
    private Map<String, String> gujratiTranslationMap;
    
    private String message;

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    private SimpleDateFormat simpleDateFormat;
    
    @Override
    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        
        initializeGujratiTranslationMap();
        // String thisUrlTemp = request.getRequestURL().toString();
        //
        // System.out.println( "This URL  " + thisUrlTemp );
        //
        // String [] urlFrgmant = thisUrlTemp.split("/");
        //
        // for( int i = 0 ; i< urlFrgmant.length; i++ )
        // {
        // System.out.println( i + "--" + urlFrgmant[i] );
        // }
        //
        
        simpleDateFormat = new SimpleDateFormat( "yyyy-mm-dd" ); 
        String responseMessage = "";

        String mobileNo = "";
        String smsMessage = "";
        String msgDate = "";

        Map<String, String[]> params = new HashMap<String, String[]>( request.getParameterMap() );

        if ( params != null && params.size() > 0 )
        {
            for( String key : params.keySet() )
            {
                if ( key.equalsIgnoreCase( "mobileno" ) )
                {
                    mobileNo = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase("message" ) )
                {
                    smsMessage = ((String[]) params.get( key ))[0];
                }
                if ( key.equalsIgnoreCase("date" ) )
                {
                    msgDate = ((String[]) params.get( key ))[0];
                }
            }
            
//            
//            Iterator<String> it = params.keySet().iterator();
//
//            while ( it.hasNext() )
//            {
//                String key = (String) it.next();
//                String value = ((String[]) params.get( key ))[0];
//
//                if ( key == "mobileno" )
//                {
//                    mobileNo = value;
//                }
//                if ( key == "message" )
//                {
//                    smsMessage = value;
//                }
//
//                // System.out.println( key + "---" + value );
//                System.out.println( key + "---" + value );
//            }

            //String message = "";
            try
            {
                responseMessage = sendSMS( mobileNo, smsMessage, msgDate );
            }
            catch ( UnsupportedEncodingException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // System.out.println( "This URL is after setting the url " + urlFrgmant
        // );

        // System.out.println( "This URL is after setting the url " +
        // urlFrgmant[0]+"/"+urlFrgmant[1]+"/"+urlFrgmant[2]+"/"+urlFrgmant[3]+"/api"
        // );

        return SUCCESS;
    }

    public static String encodeMessage( String message )
    {
        StringBuilder newMessage = new StringBuilder();
        newMessage.append( "@U" );

        for ( char c : message.toCharArray() )
        {
            String charHex = String.format( "%1$4s", Integer.toHexString( c ) );
            newMessage.append( charHex );
        }

        return newMessage.toString();
    }

    public String sendSMS( String mobileNo, String message, String msgDate )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        // String message = new
        // String("Тест на български език за СМС изпращане");
        System.out.println( encodeMessage( new String( message.getBytes() ) ) );
        try
        {
            // Construct data
            
            String [] tempMessage = message.split( "," );
            
            message = gujratiTranslationMap.get( "PERFECT_MESSAGE" ) + " " + gujratiTranslationMap.get( "male" ) +"("+
                        tempMessage[0]+","+tempMessage[1]+","+tempMessage[2]+")"+gujratiTranslationMap.get( "female" )+"("+
                        tempMessage[3]+","+tempMessage[4]+","+tempMessage[5]+")"+gujratiTranslationMap.get( "sideEffect" )+"("+
                        tempMessage[6]+") "+gujratiTranslationMap.get("sent")+new Date();
                
            
            
            String user = "username=" + "harsh.atal@gmail.com";
            String hash = "&hash=" + "04fa1b5546432e99162704a7025403879d589271";
            message = "&message=" + message;
            String sender = "&sender=" + "TXTLCL";
            // String numbers = "&numbers=" + "919654232779&unicode=1&test=1";
            String numbers = "&numbers=" + mobileNo + "&unicode=1&test=1";

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

    
    public void initializeGujratiTranslationMap()
    {
        gujratiTranslationMap = new HashMap<String, String>();

        gujratiTranslationMap.put( "PERFECT_MESSAGE", "આભાર! તમે");
        gujratiTranslationMap.put( "male", "પુરુષ" );
        gujratiTranslationMap.put( "female", "સ્ત્રી" );
        gujratiTranslationMap.put( "sideEffect", "સાઇડ ઇફેક્ટ્સ" );
        gujratiTranslationMap.put( "sent", "મોકલવામાં" );
    }
    
    
}
