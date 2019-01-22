//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgCommon/SolarisCompatibles.java, pkgCommon, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//* 
//* Date and Time File was last checked in:       98/01/22 18:15:03
//* Date and Time File was extracted/checked out: 99/04/25 09:14:53
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* 
//*
//*****************************************************************************

package com.ibm.ode.pkg.pkgCommon;

import java.io.*;
/**
 * Class representing Solaris Compatibles
 * @version     1.3 98/01/22
 * @author	Amit Ralkar(amitr@raleigh.ibm.com)
 **/

public class SolarisCompatibles	
{ 
 
/**
  * This class is used to represent the structure of the Solaris Compatibles data
  **/

/** 
  * Attributes include: 
  *   Version 
  *   Release
  **/

   private String Version_;
   private String Release_;
 
   // Default Constructor
   public SolarisCompatibles () 
   {
      Version_ = "";
      Release_ = "";
   }

   public SolarisCompatibles (String Version, String Release) 
   {
      Version_ = Version;
      Release_ = Release;
   }

   public String toString () 
   {
   return "Solaris Compatibles is : " + Version_ + " " + Release_ + "/n";
   }

   public void setVersion ( String Version )
   {
      Version_ = Version;
   }
   
   public String getVersion ()
   {
      return (Version_); 
   }

   public void setRelease ( String Release )
   {
      Release_ = Release;
   }
   
   public String getRelease ()
   {
      return (Release_); 
   }

} // End class Solaris Compatibles
