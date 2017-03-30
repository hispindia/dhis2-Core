package org.hisp.dhis.ivb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test
{

    public static void main( String[] args )
    {
        /*
        String str1 = "";
        
        byte[] bytes = str1.getBytes( );
        

        String str = new String(bytes, Charset.forName("UTF-8"));

        System.out.println(str);
        */
        /*
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime( new Date() );
        
        System.out.println( calendar2.get( Calendar.MONTH ) + " : " + calendar2.get( Calendar.YEAR ) );
        calendar2.add( Calendar.MONTH, -34 );
        System.out.println( calendar2.get( Calendar.MONTH ) + " : " + calendar2.get( Calendar.YEAR ) );
        
        // TODO Auto-generated method stub
        System.out.println("Hello");
         */
        
        String expression = "#DATEVALUE#aFYD7nGJSlg.Y9WcH4KPalT >= NOW AND #VALUE#GcWlqbethsG.Y9WcH4KPalT == 'Planned'";
        String expressionParts[] = expression.split( "AND" );
        for( int i = 0; i < expressionParts.length; i++ )
        {
            String subExp = expressionParts[i];
            Integer subExpResult;
            
            if( subExp.contains( "<=" ) )
            {
                
            }
            else if( subExp.contains( ">=" ) )
            {
                
            }
            else if( subExp.contains( "==" ) )
            {
                String leftSideExp = subExp.split( "==" )[0];                
                String rightSideExp = subExp.split( "==" )[1];
                
                if( leftSideExp.contains( "#DATEVALUE#" ) )
                {
                    leftSideExp = leftSideExp.replace( "#DATEVALUE#", "" );
                    leftSideExp = leftSideExp.trim();
                    String deUID = leftSideExp.split( "\\." )[0];
                    String cocUID = leftSideExp.split( "\\." )[1];                    
                }
            }
        }
    }

}
