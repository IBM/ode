//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgCommon/SystemVariableNotFoundError.java, pkgCommon, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.4
//* 
//* Date and Time File was last checked in:       98/01/22 18:15:05
//* Date and Time File was extracted/checked out: 99/04/25 09:13:27
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* CLC     F 764
//*
//*****************************************************************************
package com.ibm.ode.pkg.pkgCommon;

import java.lang.Error;

/**
 * <pre>
 * Thrown when the common interface(to packaging tool)
 * finds the required environment variable null
 * becuase either the required environment variable not
 * defined or defined and set to null.
 * </pre>
 *
 * @author  Chary Lingachary
 * @version     1.4 98/01/22
 * @since   SDWB 1.3.1
 */
public class SystemVariableNotFoundError extends Error 
{
   /**
    * Constructs a <code>SystemVariableNotFoundError</code>
    *
    * @since   SDWB 1.3.1
    */
   public SystemVariableNotFoundError()
   {
      super();
   }

   /**
    * Constructs a <code>SystemVariableNotFoundError</code> with the
    * system variable name
    *
    * @param   envStr Environment variable name
    * @since   SDWB 1.3.1
    */
   public SystemVariableNotFoundError( String envStr )
   {
      super();
      detailMessage_ = detailMessage_ + " Variable " + envStr + " not found...";
   }

   /**
    * detailed custom message
    *
    * @since   SDWB 1.3.1
    */
   public String getMessage()
   {
      return detailMessage_;
   }

   /**
    * String representation of the Error object
    */
   public String toString()
   {
      return (detailMessage_ != null) ? (getClass().getName() + ": " + detailMessage_) : getClass().getName();
   }

   /**
    * detailed message
    *
    * @since   SDWB 1.3.1
    */
   private String detailMessage_ = "Reading environment variables...";
}

