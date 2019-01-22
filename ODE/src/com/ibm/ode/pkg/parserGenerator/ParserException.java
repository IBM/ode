//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/ParserException.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//* 
//* Date and Time File was last checked in:       97/09/30 10:18:06
//* Date and Time File was extracted/checked out: 99/04/25 09:13:26
//* 
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* KS       F 784	Initial creation of file
//*
//*************************************************************************


package com.ibm.ode.pkg.parserGenerator;

/*******************************************************************
     Thrown when a parse Error occurs in the input. 
 * @author Kurt Shah
 * @version 1.3
 * @see Parser
**/

public class  ParserException extends Exception
{
/*******************************************************************
 * value of any specific message associated with this exception as a string
**/
  private String theMessage_;

/*******************************************************************
 * Constructs ParserException with a message saying 'No Message Provided'
**/

  public  ParserException()
  {
    super();
    theMessage_ = "No message Provided.";
  }


/*******************************************************************
 * Constructs ParserException with the specified detailed message.
**/

  public  ParserException( String s )
  {
    super( s );
    theMessage_ = s;
  }

/*******************************************************************
 * returns a brief description of this ParserException object.
 *
 * Overrides the toString method in Class Throwable
 * @see Throwable#toString
**/

  public String toString()
  {
    return theMessage_;
  }
}
