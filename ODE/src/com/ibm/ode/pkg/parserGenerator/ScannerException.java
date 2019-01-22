//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/ScannerException.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//* 
//* Date and Time File was last checked in:       97/09/30 10:18:40
//* Date and Time File was extracted/checked out: 99/04/25 09:13:26
//* 
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* KS       F 784	Initial Creation of file
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

/*******************************************************************
     Thrown when a character or word in the input file is not recognized
	by the Scanner.
 * @author Kurt Shah
 * @version 1.3
 * @see Scanner
 * @see Symbol
 * @see ExtendedSymbol
**/
public class ScannerException extends Exception
{
/*******************************************************************
 * value of any specific message associated with this exception as a string
**/
  private String theMessage_;

/*******************************************************************
 * Constructs ScannerException with a message saying 'No Message Provided'
**/
  public ScannerException()
  {
    super();
    theMessage_ = "No message Provided.";
  }

/*******************************************************************
 * Constructs ScannerException with the specified detailed message.
**/
  public ScannerException( String s )
  {
    super( s );
    theMessage_ = s;
  }

/*******************************************************************
 * returns a brief description of this ScannerException object.
 * 
 * Overrides the toString method in Class Throwable
 * @see Throwable#toString
**/
  public String toString()
  {
    return theMessage_;
  }
}
