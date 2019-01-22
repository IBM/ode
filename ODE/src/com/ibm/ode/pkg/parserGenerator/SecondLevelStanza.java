//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/SecondLevelStanza.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.2
//* 
//* Date and Time File was last checked in:       97/09/30 10:18:45
//* Date and Time File was extracted/checked out: 99/04/25 09:13:26
//* 
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BP       F 784	Initial Creation of file
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

/*****************************************************************************
 * This interface has to be implemented by all classes which represents
 * stanza's in CMF which contain one or more level of sub-stanzas
 * @version	1.1 97/05/12
 * @author 	Prem Bala
 ****************************************************************************/
public interface SecondLevelStanza
{
  public void constructChildStanza( int stanzaToken )
              throws PackageException;
}
