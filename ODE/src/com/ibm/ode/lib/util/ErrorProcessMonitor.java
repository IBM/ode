package com.ibm.ode.lib.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.IOException;

/**
 *
 * This class represents a thread that collects standard error and
 * writes it to a buffer.
 *
 * @version      1.1 98/12/22
 * @author       Ann Griffiths
**/

public class ErrorProcessMonitor extends ProcessMonitor
{

   /************************************************************************
    *
    * Constructor - set the attributes
    *
    * @param inProcess       Reference to the external process
    *
   **/

   public ErrorProcessMonitor( String threadname, Process inProcess )
   {

      super( threadname, inProcess );
      setMonitorType( 1 );
   }


   /************************************************************************
    *
    * Collect the error and store it in the stdErr_ buffer
    *
   **/

   public void run()
   {

      String line;
      BufferedReader errBuffer;
      //String sep = System.getProperty( "line.separator" );

      try
      {

         // Create a buffered reader from the error stream of the process
         errBuffer = 
            new BufferedReader
               ( new InputStreamReader( process_.getErrorStream() ) );

         // Collect the output and store it in the buffer
         while ( ( line = errBuffer.readLine() ) != null )
         {
            this.setChanged();
            this.notifyObservers( new String( line + '\n' ) );
         }
   
      }
      catch( IOException x )
      {
         this.setChanged();
         this.notifyObservers
            ( "An IOException occurred in ErrorMonitor.run()." +
              '\n' +
              "Details of the exception follow : " +
              '\n' +
              x + " " + x.getMessage() +
              '\n' );
      }

   }

}
