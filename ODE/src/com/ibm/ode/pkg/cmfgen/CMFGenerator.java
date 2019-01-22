/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:34:55
 * Date and Time File was extracted/checked out: 06/04/13 16:45:13
 ******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.io.*;
import java.util.Vector;
import java.util.Stack;

/**
 * This class drives the CMF Generation process.
 *
 * @author Kiran Lingutla
 * @version 1.1
 */
public class CMFGenerator
{
  public static void main(String[] args)
  {
    try
    {
      VariablesAndMessageHandler vmh = new VariablesAndMessageHandler();
      FileHandlingUtilities fhu = new FileHandlingUtilities();
      MetaDataParser mp = new MetaDataParser();
      Vector listOfObjSubDirs = fhu.getAllSubDirs();
      StringBuffer fileStanzasBuffer = mp.parseFiles( listOfObjSubDirs );
      fhu.createCMF( fileStanzasBuffer );
    }
    //this is placed to catch any exceptions uncaught else where, hence
    //it is made generic
    catch( Exception e )
    {
      VariablesAndMessageHandler.printException( e, "CMFGenerator", 
                                             "An exception occured" );
    }
  }
}
    
