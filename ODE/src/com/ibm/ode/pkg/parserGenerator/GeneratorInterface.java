
//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/GeneratorInterface.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//*
//* Date and Time File was last checked in:       97/09/30 10:20:08
//* Date and Time File was extracted/checked out: 99/04/25 09:13:29
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BP       F 784
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

/*****************************************************************************
 * This interface has to be implemented by all the specific Generator classes 
 * This will basically hold the routine to create the platform specific
 * metadata files
 * @version     1.3  97/09/30
 * @author 	Prem Bala
 ****************************************************************************/
public interface GeneratorInterface
{
   public void generateTargetMetadataFiles( EntityTreeRoot entityTreeRoot,
					    Package packageObject )
                  throws GeneratorException ;
}
