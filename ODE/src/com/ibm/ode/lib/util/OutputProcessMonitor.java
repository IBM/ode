package com.ibm.ode.lib.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.IOException;

/**
 *
 * This class represents a thread that collects standard output and
 * writes it to a buffer.
 *
 * @version      1.1 98/12/22
 * @author       Ann Griffiths
 **/
public class OutputProcessMonitor extends ProcessMonitor
{

  /************************************************************************
   *
   * Constructor - set the attributes
   *
   * @param inProcess       Reference to the external process
   *
   **/

  public OutputProcessMonitor( String threadname, Process inProcess )
  {
    super( threadname, inProcess );
  }


  /************************************************************************
   *
   * Collect the output and store it in the stdOut_ buffer
   *
   **/

  public void run()
  {

    String line;
    BufferedReader outBuffer;
    //String sep = System.getProperty( "line.separator" );

    try
    {

      // Create a buffered reader from the input stream of the process
      outBuffer = 
        new BufferedReader
        ( new InputStreamReader( process_.getInputStream() ) );

      // Collect the output and store it in the buffer
      while ( ( line = outBuffer.readLine() ) != null )
      {
        this.setChanged();
        this.notifyObservers( new String( line + '\n' ) );
      }

    }
    catch( IOException x )
    {
      this.setChanged();
      this.notifyObservers
        ( "An IOException occurred in OutputMonitor.run()." +
          '\n' +
          "Details of the exception follow : " +
          '\n' +
          x + " " + x.getMessage() +
          '\n' );
    }

  }

}
