//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgMvs/MvsFtpException.java, pkgMvs, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.1
//*
//* Date and Time File was last checked in:       98/06/04 11:14:58
//* Date and Time File was extracted/checked out: 99/04/25 09:17:22
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* MAD      D 4953 Initial creation
//*
//*****************************************************************************

package com.ibm.ode.pkg.pkgMvs;

/**
 * Exception class for errors in MvsFtp class.
 * @version 1.1 98/06/04
 * @author  Mark DeBiase
**/

class MvsFtpException extends Exception
{
  private Exception nestedException_;

  private MvsFtpException()  { }    // don't allow use of default constructor

  /**
   * Create new MvsFtpException object with specified text message.
  **/
  public MvsFtpException(String text)
  {
    this(text, null);
  }

  /**
   * Create new MvsFtpException object with specified text message
   * and nested exception.
  **/
  public MvsFtpException(String text, Exception e) 
  {
    super(text);
    nestedException_ = e;
  }

  /**
   * Return true if this object has a nested exception.
  **/
  public boolean hasNestedException()
  {
    return (nestedException_ != null);
  }

  /**
   * Return the message text from the nested exception.
  **/
  public String getNestedExceptionMsg()
  {
    if (nestedException_ != null)
      return nestedException_.getMessage();
    else
      return "";
  }

  /**
   * Print nested exception stack trace to stdout.
  **/
  public void printNestedStackTrace()
  {
    if (nestedException_ != null)
      nestedException_.printStackTrace();
  }
}
