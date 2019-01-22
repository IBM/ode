package com.ibm.ode.lib.util;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class represents a thread that collects 
 * standard error or standard output and notifies 
 * Observers each time data is read
 *
 * @see          java.lang.Thread
 * @see          java.util.Observable
 * @author       Chary Lingachary
 **/
public abstract class Monitor extends Observable
implements Runnable
{
  /**
   * The thread that collects the results stream
   */
  protected Thread resultsThread_ = null;

  /**
   * monitor type - stdout or stderr
   * 0 - stdout monitor
   * 1 - stderr monitor
   */
  private int monitorType_ = 0; 

  /**
   * Constructor - set the attributes
   *
   * @param inProcess       Reference to the external process
   * @param threadName      Name to give to thread
   */
  public void setMonitorType( int type )
  {
    monitorType_ = type;
  }

  /**
   * Returns monitor type
   * 0 - stdout monitor
   * 1 - stderr monitor
   */
  public int getMonitorType()
  {
    return monitorType_;
  }

  /**
   * constructor that takes
   * thread name for the monitor thread
   */
  public Monitor( String threadName )
  {
    resultsThread_ = new Thread( this, threadName );
  }

  /**
   * Start the thread
   */
  protected void start()
  {
    resultsThread_.start();
  }

  /**
   * Add observers from a Vector
   *
   * @param vectorO Vector of Observers              
   */
  public void addObservers( Vector vectorO )
  {
    Enumeration enumer;
    for ( enumer = vectorO.elements(); enumer.hasMoreElements(); )
    {
      this.addObserver( (Observer)( enumer.nextElement() ) );
    }
  }

  /**
   * Add observers from an array
   *
   * @param arrayO Array of Observers               
   */
  public void addObservers( Observer arrayO[] )
  {
    for ( int i = 0; i < arrayO.length; i++ )
    {
      this.addObserver( arrayO[i] );
    }
  }

  /**
   * Waits for the executing thread to complete
   *
   * @exception             InterruptedException is thrown if the executing
   *                        thread is interrupted while waiting for it to
   *                        complete
   */
  public void join() throws InterruptedException
  {
    resultsThread_.join();
  }

  /**
   * interrupt monitor thread
   */
  public void interrupt() throws InterruptedException
  {
    resultsThread_.interrupt();
  }

  /**
   * put the monitor thread to sleep
   */
  public void sleep( long time ) throws InterruptedException
  {
    resultsThread_.sleep(time);
  }

  /**
   * Return the monitor thread name
   */
  public String getName()
  {
    return ( resultsThread_ != null ? resultsThread_.getName() : null );
  }

  /**
   * Method to be overridden by subclasses
   */
  public abstract void run();
}
