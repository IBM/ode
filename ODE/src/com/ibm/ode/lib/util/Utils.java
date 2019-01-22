package com.ibm.ode.lib.util;

import java.util.Vector;
import java.util.Enumeration;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.IOException;


/**
 *
 * This class represents utility functions used throughout the RMS.
 *
 * @version      1.3 98/12/18
 * @author       Ann Griffiths
 *
**/

public class Utils  
{

   /************************************************************************
    *
    * This method parses the specified string based on the second string
    * that is passed in and puts the resulting data into a vector
    *
    * @param dataString           String of data                          
    * @param parseString          String to use when parsing
    * @return                     Vector of parsed data
    *
   **/

   public static Vector parseData( String dataString, String parseString )
   {

      Vector data = new Vector();
      int sizeOfParser = parseString.length();

      // Initialize the start of data and the start of the parser
      int startOfData = 0;
      int startOfParser = dataString.indexOf( parseString );

      // If the parse string exists in the data string, then parse the
      // data
      if ( startOfParser != -1 )
      {

         while ( startOfData < dataString.length() )
         {

            String datapiece = 
               dataString.substring( startOfData, startOfParser );   
            data.addElement( datapiece );
            startOfData += datapiece.length() + sizeOfParser;
            startOfParser = dataString.indexOf( parseString, startOfData );
            if ( startOfParser == -1 )
            {
               data.addElement( dataString.substring( startOfData ) );
               startOfData = dataString.length();
            }

         }

      }
      else
      {
         data.addElement( dataString );
      }

      // Return the vector of data strings
      return data;

   }


   /************************************************************************
    *
    * This method parses the specified string based on a line feed ('\n'),
    * a carriage return ('\r'), or a carriage return followed immediately
    * by a line feed ('\r\n').
    *
    * @param dataString           String of data                          
    * @return                     Vector of parsed data
    * @exception                  IOException
    *
   **/

   public static Vector parseData( String dataString )
      throws IOException
   {

      Vector data = new Vector();

      // Create the line number reader
      LineNumberReader lineReader =
         new LineNumberReader( new StringReader( dataString ) );
      String line;
      
      // Loop until null
      while ( ( line = lineReader.readLine() ) != null )
      {
      
         // Add the line to the vector
         data.addElement( line );

      }

      // Return the vector
      return data;
      
   }


   /************************************************************************
    *
    * Creates a StringBuffer from a Vector of Strings
    *
    * @return                StringBuffer representing Vector values
    *
   **/

   public static StringBuffer createStringBuffer( Vector vec )
   {

      String linesep = System.getProperty( "line.separator" );
      StringBuffer allValues = new StringBuffer( "" );

      // Loop through the vector of elements and add the String 
      // representation of the vector element to the StringBuffer
      Enumeration enumer;
      for ( enumer = vec.elements(); enumer.hasMoreElements(); )
      {

          String text = (String)( enumer.nextElement() );
          allValues.append( text + linesep );

      }

      return allValues;

   }
     
}
