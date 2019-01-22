
//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/GeneratorException.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//*
//* Date and Time File was last checked in:       98/01/22 18:14:28
//* Date and Time File was extracted/checked out: 99/04/25 09:13:29
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BP       F 784
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

/**
 * A class representing an exception
 *
 * @version     1.3 98/01/22
 * @author      Prem Bala
**/

public class GeneratorException extends Exception
{
  private String theMessage_;

  public GeneratorException()
  {
    super();
    theMessage_ = "No message provided.";
    
  }

  public GeneratorException( String s )
  {
    super( s );
    theMessage_ = s;
  }

  public String toString()
  {
    return theMessage_;
  }
}
