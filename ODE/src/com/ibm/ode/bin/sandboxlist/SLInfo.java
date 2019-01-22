package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.string.StringTools;


/**
 * This encapsulates the per-sandbox information from the sandboxrc file,
 * at least what we can get by running mksb -list and currentsb -chain.
 *
 * status == OK means that the currentsb return code was 0, there were
 *  no WARNING: or ERROR: messages, and there was at least one other message
 *  which would be some part of the backing chain.
 * status == WARNING means that all of the above was true except that one or
 *  more WARNING: messages was read.
 * status == ERROR: means that there was a return code that was non-zero from
 *  currentsb or there was an ERROR: message or there was no part of the 
 *  backing chain in the currentsb output.
**/
class SLInfo
{
  static String STATUS_OK = "OK";
  static String STATUS_WARNING = "WARNING";
  static String STATUS_ERROR = "ERROR";

  private String sb;
  private String vb;
  private String bs;
  private boolean isBB;
  private boolean isDef;
  private String st;

  SLInfo( String sandbox, String variableBase, String base,
          boolean isBackingBuild, boolean isDefault,
          String status )
  {
    sb = sandbox;
    vb = variableBase;
    bs = base;
    isBB = isBackingBuild;
    isDef = isDefault;
    this.setStatus( status );
  }

  SLInfo( String sandbox )
  {
    sb = sandbox;
    vb = "";
    bs = "";
    isBB = false;
    isDef = false;
    st = STATUS_OK;
  }

  /**
   * returns the sandbox name
  **/
  String getSandboxName()
  {
    return sb;
  }

  /**
   * If there are environment variables specified for the base of the
   * sandbox, then this method returns the base before environment
   * variable evaluation; otherwise "" is returned.
  **/
  String getVariableBase()
  {
    return vb;
  }

  /**
  **/
  void setVariableBase( String s )
  {
    vb = s;
  }

  /**
   * returns the sandbox base after evaluation of environment variables
  **/
  String getBase()
  {
    return bs;
  }

  /**
  **/
  void setBase( String s )
  {
    bs = s;
  }

  /**
   * returns true if the sandbox is valid and is a backing build.
  **/
  boolean isBackingBuild()
  {
    return isBB;
  }
                        
  /**
  **/
  void setIsBackingBuild( boolean b )
  {
    isBB = b;
  }

  /**
   * This indicates whether the sandbox is the default sandbox in the 
   * .sandboxrc file.
  **/
  boolean isDefault()
  {
    return isDef;
  }
                        
  /**
  **/
  void setIsDefault( boolean b )
  {
    isDef = b;
  }

  /**
  **/
  String getStatus()
  {
    return st;
  }

  /**
  **/
  void setStatus( String status  )
  {
    if (status.equals( STATUS_OK ) || status.equals( STATUS_WARNING ))
      st = status;
    else
      st = STATUS_ERROR;
  }

  public String toString()
  {
    return ( "SLInfo: sandbox=" + sb + " variableBase=" + vb +
             " base=" + bs + " isBackingBuild=" + isBB +
             " isDefault=" + isDef + " status=" + st );
  }
                        
}
