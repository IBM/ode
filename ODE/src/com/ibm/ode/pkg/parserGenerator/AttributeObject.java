//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/AttributeObject.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//* 
//* Date and Time File was last checked in:       97/09/30 10:16:57
//* Date and Time File was extracted/checked out: 99/04/25 09:13:24
//* 
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BP       F 784        Initial creation of file
//* BP       F 1004       Add documentation
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

/*****************************************************************************
 * This is an abstract base class for all the *AttribObject's. Any new *AttribObject
 * ( which represents info about a stanza containing attrib-value pairs ) must
 * extend from this class.
 * @version     1.3  97/09/30
 * @author 	Prem Bala
 ****************************************************************************/
abstract public class AttributeObject
{
  
  /******************************************************************************
   * abstract method which should be implemented by all the subclasses
   * given a token of the attribute, validates and returns the type of the attribute
   * @param int :- token representing the attribute in the stanza represented 
   *                by the *AttribObject
   **/  
  abstract public int validateAndGetType( int token );
}
