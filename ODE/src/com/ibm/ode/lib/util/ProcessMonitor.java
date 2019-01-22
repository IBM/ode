package com.ibm.ode.lib.util;

/**
 * This class represents a Process Monitor.
 * Captures stdout or stderr from Process's 
 * InputStream or OutputStream
 *
 * @author       Chary Lingachary
 */
public abstract class ProcessMonitor extends Monitor
{
  protected Process process_;

  /**
   * construct with process monitor name
   */
  public ProcessMonitor( String threadname )
  {
    super( threadname );
  }   

  /**
   * construct with process monitor name and Process
   * to read stream data from
   */
  public ProcessMonitor( String threadname, Process inProcess )
  {
    super( threadname );
    process_ = inProcess;
  }
}
