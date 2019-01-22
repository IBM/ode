
//******************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1994, 1996.  All Rights Reserved.
//*
//*  
//* File, Component, Release: com/ibm/sdwb/bps/api/servicepkg/ServicePackagingException.java, servicepkg, ode5.0, 20041004.0550
//*  
//* Version: 1.1
//*  
//* Date and Time File was last checked in:       03/05/10 01:18:22
//* Date and Time File was extracted/checked out: 06/04/13 16:47:13
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BAN      F10996 Initial creation 
//* BAN      D11807 remove dependencies on bps.util.RuntimeStack 
//******************************************************************************

package com.ibm.sdwb.bps.api.servicepkg;


import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * This class has the ServicePackagingException which is thrown by ServicePackaging class and the other 
 *
 * @version 1.1 03/05/10
 * @author Arun Balaraman
**/


public class ServicePackagingException 
       extends Exception implements java.io.Serializable 
{
  private String stackTrace_;

  public  ServicePackagingException() 
  {
    super ();
  }

  public ServicePackagingException( String s ) 
  {
    super ( s );
  }

  
  public ServicePackagingException( String mesg, Exception e )
  {
    super (mesg+"\n"+getStackFrames(e ));
    setLocalStackTrace(getStackFrames(e ));
  }


  public ServicePackagingException( Exception e )
  {
    super ( e.getMessage());
    setLocalStackTrace(getStackFrames(e ));
  }

  
  private String getLocalStackTrace()
  {
    return stackTrace_;
  }
  
  private void setLocalStackTrace(String str)
  {
    this.stackTrace_ = str;
  }
  
  public static String getStackFrames(Throwable throwable)
  {
    StringWriter out = new StringWriter();
    throwable.printStackTrace(new PrintWriter(out));
    return out.toString();
  }

}	
