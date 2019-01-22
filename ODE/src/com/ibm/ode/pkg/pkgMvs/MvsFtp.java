//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgMvs/MvsFtp.java, pkgMvs, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.2
//*
//* Date and Time File was last checked in:       98/06/04 16:46:05
//* Date and Time File was extracted/checked out: 99/04/25 09:17:22
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* MAD      D 4953 Initial creation
//*
//*****************************************************************************

package com.ibm.ode.pkg.pkgMvs;

/**
 * Minimal FTP client to perform specific functions for MVS packaging:
 * submit jobs to JES, check job status, retrieve job output.
 * See RFC 959 for FTP documentation.
 * @version 1.2 98/06/04
 * @author  Mark DeBiase
**/

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

class MvsFtp
{

  //****************************************************************************
  // Job status codes returned by checkJob() and table to print text equivalent.
  //****************************************************************************
  public static final int JOB_STATUS_INPUT   = 0;
  public static final int JOB_STATUS_HELD    = 1;
  public static final int JOB_STATUS_ACTIVE  = 2;
  public static final int JOB_STATUS_OUTPUT  = 3;
  public static final int JOB_STATUS_UNKNOWN = 4;
  public static final String[] JOB_STATUS_TEXT = { "INPUT", "HELD", "ACTIVE",
                                                   "OUTPUT", "UNKNOWN"        };

  //****************************************************************************
  // FTP status codes returned in first character of FTP response.
  //****************************************************************************
  private static final int POSITIVE_PRELIMINARY_REPLY          = 1;
  private static final int POSITIVE_COMPLETION_REPLY           = 2;
  private static final int POSITIVE_INTERMEDIATE_REPLY         = 3;
  private static final int TRANSIENT_NEGATIVE_COMPLETION_REPLY = 4;
  private static final int PERMANENT_NEGATIVE_COMPLETION_REPLY = 5;

  //****************************************************************************
  // FTP control socket connection.
  //****************************************************************************
  private Socket         controlSocket_ = null;
  private BufferedReader controlInput_  = null;
  private BufferedWriter controlOutput_ = null;

  //****************************************************************************
  // Host name and port number of FTP server to connect to.
  //****************************************************************************
  private String  ftpHost_;
  private int     ftpPort_;

  //****************************************************************************
  // Holds all FTP output.
  //****************************************************************************
  private StringBuffer ftpOutput_;

  //****************************************************************************
  // Holds full text of current (last) FTP response message.
  //****************************************************************************
  private String currentResponse_ = null;

  //****************************************************************************
  // Misc. constants.
  //****************************************************************************
  private static final String NL    = System.getProperty("line.separator");
  private static final String ASCII = "8859_1";
  private static final String CRLF  = "\r\n";
  private static final int OUTPUT_BUFFER_SIZE = 1024;

  //****************************************************************************
  // Create MvsFtp object for given host with default FTP port (21).
  //****************************************************************************
  public MvsFtp(String hostName)
  {
    this(hostName, 21);
  }

  //****************************************************************************
  // Create MvsFtp object for given host with specified FTP port.
  //****************************************************************************
  public MvsFtp(String hostName, int port)
  {
    ftpHost_ = hostName;
    ftpPort_ = port;
  }

  //****************************************************************************
  // Submit job from specified file.  Return job number.
  //****************************************************************************
  public String submitJob(String userid, String password, String jclFileName)
                throws MvsFtpException
  {
    String submitResponse;

    ftpOutput_ = new StringBuffer(OUTPUT_BUFFER_SIZE);
    try
    {
      connect();
      login(userid, password);
      submitResponse = submitJob(jclFileName);
      quit();
    }
    finally
    {
      disconnect();
    }

    // First line of FTP response should be:
    //   250-It is known to JES as JOBxxxxx
    // If the JCL jobcard is really messed up, the JOBxxxxx will be missing
    // from the message because it was never assigned a job number.
    try
    {
      BufferedReader br = new BufferedReader(new StringReader(submitResponse));
      String line = br.readLine().toUpperCase();

      if (line.startsWith("250-IT IS KNOWN TO JES AS"))
      {
        StringTokenizer st = new StringTokenizer(line, " ");
        String currentToken = "";
        // skip to last word on the line
        while (st.hasMoreTokens())
        {
          currentToken = st.nextToken();
        }
        // make sure it is JOBxxxxx
        if (currentToken.startsWith("JOB"))
        {
          return currentToken;
        }
      }
      throw new MvsFtpException("Unable to determine job number.");
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException parsing FTP output.", e);
    }
  }

  //****************************************************************************
  // Check status of specified job.  The job number should be of the form
  // JOBxxxxx as returned by submitJob().  The status will be returned as
  // an integer defined by one of the MvsFtp.JOB_STATUS_xxxx constants.
  //****************************************************************************
  public int checkJob(String userid, String password, String jobNumber)
             throws MvsFtpException
  {
    String jobList;

    ftpOutput_ = new StringBuffer(OUTPUT_BUFFER_SIZE);
    try
    {
      connect();
      login(userid, password);
      jobList = listJobs();
      quit();
    }
    finally
    {
      disconnect();
    }

    // A list of all the jobs on the JES queue is returned from the LIST 
    // command.  Scan through the list looking for the specified job and
    // then determine its status.  Each line in the list should be like:
    //    IBMVS9X   JOB25710  OUTPUT    3 Spool Files
    // with the status being the third word.
	 //
	 // NOTE: newer versions of this list output may have the status
	 //       in the fourth column!
    try
    {
      BufferedReader br = new BufferedReader(new StringReader(jobList));
      String line;
      while ( (line = br.readLine()) != null)
      {
        // locate the correct job number
        if (line.indexOf(jobNumber) != -1)
        {
          StringTokenizer st = new StringTokenizer(line);
          String status = null, status_new = null;

          for (int i=0; i<3; i++)
          {
			   if (st.hasMoreElements())
              status = st.nextToken();
				else
				  status = null;
          }
          if (st.hasMoreElements())
			   status_new = st.nextToken(); // fourth word

          if (status == null)
			   continue;
          if (status_new == null)
			   status_new = "";

          status = status.toUpperCase();
          status_new = status_new.toUpperCase();

          if (status.equals("INPUT") || status_new.equals("INPUT"))
			   return JOB_STATUS_INPUT;
          else if (status.equals("HELD") || status_new.equals("HELD"))
			   return JOB_STATUS_HELD;
          else if (status.equals("ACTIVE") || status_new.equals("ACTIVE"))
			   return JOB_STATUS_ACTIVE;
          else if (status.equals("OUTPUT") || status_new.equals("OUTPUT"))
			   return JOB_STATUS_OUTPUT;
        }
      }
      return JOB_STATUS_UNKNOWN;
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException parsing FTP output.", e);
    }
  }

  //****************************************************************************
  // Get the output for the specified job and write it to the specified file.
  // The job number should be of the form JOBxxxxx as returned by submitJob().
  //****************************************************************************
  public void getJob(String userid, String password,
                     String jobNumber, String outputFileName) 
              throws MvsFtpException
  {
    ftpOutput_ = new StringBuffer(OUTPUT_BUFFER_SIZE);
    try
    {
      connect();
      login(userid, password);
      getJob(jobNumber, outputFileName);
      quit();
    }
    finally
    {
      disconnect();
    }
  }

  //****************************************************************************
  // Delete the output for the specified job.
  // The job number should be of the form JOBxxxxx as returned by submitJob().
  //****************************************************************************
  public void deleteJobOutput(String userid, String password, String jobNumber) 
              throws MvsFtpException
  {
    ftpOutput_ = new StringBuffer(OUTPUT_BUFFER_SIZE);
    try
    {
      connect();
      login(userid, password);
      deleteJobOutput(jobNumber);
      quit();
    }
    finally
    {
      disconnect();
    }
  }

  //****************************************************************************
  // Return a string containing all the FTP output.
  //****************************************************************************
  public String getOutput()
  {
    return ftpOutput_.toString();
  }

  //****************************************************************************
  // Establish a connection to the FTP server.
  //****************************************************************************
  private void connect() throws MvsFtpException
  {
    // the control input/output streams are defined to use ASCII encoding
    // to avoid problems when client is running on MVS (otherwise the data
    // would be sent in EBCDIC).
    try
    {
      controlSocket_ = new Socket(ftpHost_, ftpPort_);
      controlInput_  = new BufferedReader( new InputStreamReader(
                             controlSocket_.getInputStream(), ASCII) );
      controlOutput_ = new BufferedWriter( new OutputStreamWriter(
                             controlSocket_.getOutputStream(), ASCII) );
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException during connect.", e);
    }

    if (readResponse() != POSITIVE_COMPLETION_REPLY)
    {
      throw new MvsFtpException("Connect failed.");
    }

    return;
  }

  //****************************************************************************
  // Send USER/PASS FTP commands.
  //****************************************************************************
  private void login(String userid, String password) throws MvsFtpException
  {
    try
    {
      ftpOutput_.append("USER ").append(userid).append(NL);
      controlOutput_.write("USER ");
      controlOutput_.write(userid);
      this.flushControlOutput();
      if (readResponse() != POSITIVE_INTERMEDIATE_REPLY)
      {
        throw new MvsFtpException("USER command failed.");
      }

      ftpOutput_.append("PASS ********").append(NL);
      controlOutput_.write("PASS ");
      controlOutput_.write(password);
      this.flushControlOutput();
      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("PASS command failed.");
      }
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException during login.", e);
    }
  }

  //****************************************************************************
  // Send QUIT FTP command.
  //****************************************************************************
  private void quit()
  {
    // we really dont care if this command fails, so we can skip checking 
    // the response code and ignore any exceptions that may have been thrown
    // while sending the command or reading the response.
    try
    {
      ftpOutput_.append("QUIT").append(NL);
      controlOutput_.write("QUIT");
      flushControlOutput();
    //  readResponse();  // dont need to check response code....
    }
    // catch(MvsFtpException e)
    // {
      // may be thrown by readResponse()
    // }
    catch(IOException e) 
    { 
      // may be thrown writing to the control output stream
    }
  }

  //****************************************************************************
  // Get list of jobs on the JES queue & thier status.
  //****************************************************************************
  private String listJobs() throws MvsFtpException
  {
    ServerSocket   dataSocket     = null;
    Socket         dataConnection = null;
    StringBuffer   jobList        = new StringBuffer(512);

    try
    {
      sendJesSiteCmd();

      dataSocket = new ServerSocket(0); 
      sendPortCmd(dataSocket);

      ftpOutput_.append("LIST").append(NL);
      controlOutput_.write("LIST");
      this.flushControlOutput();
      if (readResponse() != POSITIVE_PRELIMINARY_REPLY)
      {
        throw new MvsFtpException("LIST command failed.");
      }

      dataConnection = dataSocket.accept();

      BufferedReader dataRdr = new BufferedReader( new InputStreamReader(
                                   dataConnection.getInputStream(), ASCII) );
      String line;
      while ( (line = dataRdr.readLine()) != null)
      {
        jobList.append(line).append(NL);
        ftpOutput_.append(line).append(NL);
      }
      dataRdr.close();

      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("LIST command failed.");
      }

      return jobList.toString();
    }    
    catch(IOException e)
    {
      throw new MvsFtpException("IOException checking job status.", e);
    }
    finally
    {
      // make sure data connection sockets get closed
      try
      {
        if (dataConnection != null)
          dataConnection.close();
      }
      catch(IOException e)  { }

      try
      {
        if (dataSocket != null)
          dataSocket.close();
      }
      catch(IOException e)  { }
    }
  }

  //****************************************************************************
  // Delete a specified job from the JES queue.
  //****************************************************************************
  private void deleteJobOutput(String jobNumber) throws MvsFtpException
  { 
    try
    {
      sendJesSiteCmd();
      ftpOutput_.append("DELE ").append(jobNumber).append(NL);
      controlOutput_.write("DELE ");
      controlOutput_.write(jobNumber);
      this.flushControlOutput();
      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("DELE command failed.");
      }
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException sending DELE command.", e);
    }

  }

  //****************************************************************************
  // Submit JCL from specified file.  Return FTP reponse to STOR command.
  //****************************************************************************
  private String submitJob(String jclFileName) throws MvsFtpException
  {
    ServerSocket dataSocket     = null;
    Socket       dataConnection = null;

    try
    {
      sendJesSiteCmd();

      dataSocket = new ServerSocket(0); 
      sendPortCmd(dataSocket);

      ftpOutput_.append("STOR ").append(jclFileName).append(NL);
      controlOutput_.write("STOR ");
      controlOutput_.write(jclFileName);
      this.flushControlOutput();
      if (readResponse() != POSITIVE_PRELIMINARY_REPLY)
      {
        throw new MvsFtpException("STOR command failed.");
      }

      dataConnection = dataSocket.accept();

      BufferedReader fileRdr = new BufferedReader(new FileReader(jclFileName));
      BufferedWriter dataWtr = new BufferedWriter( new OutputStreamWriter(
                                    dataConnection.getOutputStream(), ASCII) );

      String line;
      while ( (line = fileRdr.readLine()) != null)
      {
        dataWtr.write(line);
        dataWtr.write(CRLF);
      }
      dataWtr.close();
      fileRdr.close();

      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("STOR command failed.");
      }

      return currentResponse_;
    }    
    catch(IOException e)
    {
      throw new MvsFtpException("IOException submitting job.", e);
    }
    finally
    {
      // make sure data connection sockets get closed
      try
      {
        if (dataConnection != null)
          dataConnection.close();
      }
      catch(IOException e)  { }

      try
      {
        if (dataSocket != null)
          dataSocket.close();
      }
      catch(IOException e)  { }
    }
  }

  //****************************************************************************
  // Retrieve output for specified job & save it to specified file.
  //****************************************************************************
  private void getJob(String jobNumber, String outputFile) 
               throws MvsFtpException
  {
    ServerSocket dataSocket     = null;
    Socket       dataConnection = null;

    try
    {
      sendJesSiteCmd();

      dataSocket = new ServerSocket(0); 
      sendPortCmd(dataSocket);

      ftpOutput_.append("RETR ").append(jobNumber + ".X").append(NL);
      controlOutput_.write("RETR ");
      controlOutput_.write(jobNumber + ".X");
      this.flushControlOutput();
      if (readResponse() != POSITIVE_PRELIMINARY_REPLY)
      {
        throw new MvsFtpException("RETR command failed.");
      }

      dataConnection = dataSocket.accept();

      BufferedWriter fileWtr = new BufferedWriter(new FileWriter(outputFile));
      BufferedReader dataRdr = new BufferedReader( new InputStreamReader(
                                   dataConnection.getInputStream(), ASCII) );

      String line;
      while ( (line = dataRdr.readLine()) != null)
      {
        fileWtr.write(line);
        fileWtr.newLine();
      }
      fileWtr.close();

      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("RETR command failed.");
      }
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException retrieving job output.", e);
    }
    finally
    {
      // make sure data connection sockets get closed
      try
      {
        if (dataConnection != null)
          dataConnection.close();
      }
      catch(IOException e)  { }

      try
      {
        if (dataSocket != null)
          dataSocket.close();
      }
      catch(IOException e)  { }
    }
  }

  //****************************************************************************
  // Read the server's response to the command. Return status code taken from
  // first digit of FTP response code.
  //****************************************************************************
  private int readResponse() throws MvsFtpException
  {
    String       response = null;
    StringBuffer tempStr  = new StringBuffer(80);

    try
    {
      do
      {
        response = controlInput_.readLine(); 
        if (response == null)
        {
          throw new MvsFtpException("Premature EOF while reading response.");
        }
        tempStr.append(response).append(NL);
      } while( !responseDone(response) );
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException reading response.", e);
    }

    currentResponse_ = tempStr.toString();
    ftpOutput_.append(tempStr.toString());

    return Integer.parseInt(response.substring(0,1));
  }

  //****************************************************************************
  // Helper method to check for end of FTP response.  The end of the response
  // is indicated by three numeric digits at the beginning of a line followed
  // by a blank space.
  //****************************************************************************
  private final boolean responseDone(String responseLine)
  {
    return ( Character.isDigit(responseLine.charAt(0)) &&
             Character.isDigit(responseLine.charAt(1)) &&
             Character.isDigit(responseLine.charAt(2)) &&
             responseLine.charAt(3) == ' ' );  
  }

  //****************************************************************************
  // Send SITE FTP command to allow interaction with JES.
  //****************************************************************************
  private void sendJesSiteCmd() throws MvsFtpException
  {
    try
    {
      ftpOutput_.append("SITE FILETYPE=JES").append(NL);
      controlOutput_.write("SITE FILETYPE=JES");
      this.flushControlOutput();
      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("SITE command failed.");
      }
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException sending SITE command.", e);
    }
  }

  //****************************************************************************
  // Send PORT FTP command to specify the location of our data connection.
  // See RFC 959 for details on PORT command argument.
  //****************************************************************************
  private void sendPortCmd(ServerSocket s) throws MvsFtpException
  {
    // get the data port specification
    int         portNo    = s.getLocalPort();
    InetAddress localHost = null;
    try
    {
      localHost = InetAddress.getLocalHost();
    }
    catch(UnknownHostException e) 
    {
      throw new MvsFtpException("Unable to determine local host.", e);
    }

    StringBuffer portSpec = new StringBuffer(53);
    portSpec.append(localHost.getHostAddress().replace('.', ','));
    portSpec.append(",")
            .append( ((portNo & 0xff00) >> 8) )
            .append(",")
            .append( (portNo & 0x00ff) );

    try
    {
      String dataPortSpec = portSpec.toString();
      ftpOutput_.append("PORT ").append(dataPortSpec).append(NL);
      controlOutput_.write("PORT ");
      controlOutput_.write(dataPortSpec);
      this.flushControlOutput();
      if (readResponse() != POSITIVE_COMPLETION_REPLY)
      {
        throw new MvsFtpException("PORT command failed.");
      }
    }
    catch(IOException e)
    {
      throw new MvsFtpException("IOException sending PORT command.", e);
    }
  }

  //****************************************************************************
  // Terminate the current command & flush the stream to send it.
  //****************************************************************************
  private final void flushControlOutput() throws IOException
  {
    controlOutput_.write(CRLF);
    controlOutput_.flush();
  }

  //****************************************************************************
  // Close the control I/O streams and socket.
  //****************************************************************************
  private void disconnect()
  {
    try
    {
      if (controlInput_ != null)
        controlInput_.close();
    }
    catch(IOException e) { }

    try
    {
      if (controlOutput_ != null)
        controlOutput_.close();
    }
    catch(IOException e) { }

    try
    {
      if (controlSocket_ != null)
        controlSocket_.close();
    }
    catch(IOException e) { }

    controlInput_  = null;
    controlOutput_ = null;
    controlSocket_ = null;
  }
}
