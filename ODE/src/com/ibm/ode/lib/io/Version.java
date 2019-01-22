//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1999.  All Rights Reserved.
//*
//*****************************************************************************

package com.ibm.ode.lib.io;

/** 
 * This class provides information about the Ode version.
 */

public class Version
{
  /**
   * The ODE version number. For example, "ODE 2.3". Note that there is a tool to fill
   * this value in during building. So make sure they are in sync if there is any 
   * change.
   */
  private static final String OdeVersionNumber_="%RELEASE_NAME%";

  /**
   * The build level name, e.g. 1, 2, 2a.... Note that there is a tool to fill this
   * value in during building. So make sure they are in sync if there is any change.
   */
  private static final String OdeLevelName_="%LEVEL_NAME%";

  /**
   * The timestamp for this build. For example, 06/21/99 21:31. Note that
   * there is a tool to fill this value in during building. So make sure
   * they are in sync if there is any change.
   */
  private static final String OdeBuildTimestamp_="%BUILD_DATE%";

  /**
   * The complete ODE version that will be added by the build process
   */
  private static final String OdeVersion_ = "@(#)" + OdeVersionNumber_ + " " + OdeLevelName_ + " (" + OdeBuildTimestamp_ + ")";

  /**********************************************************************
   * Retrieve the ODE version. Remove the @(#) prefix.
   * 
   * @return the ODE version
   */
  public static String getOdeVersion()
  {
     return OdeVersion_.substring(new String("@(#)").length());
  }

  /**********************************************************************
   * Retrieve the ODE version number. For example, "ODE 2.3". 
   *
   * @return the ODE version number
   */
  public static String getOdeVersionNumber()
  {
    return OdeVersionNumber_;
  }

  /**********************************************************************
   * Retrieve the build level name for this version. For daily builds, this
   * might be the same, so it needs to check the BuildAreaName.
   *
   * @return the Build level name used for this build
   */
  public static String getOdeLevelName()
  {
    return OdeLevelName_;
  }

  /**********************************************************************
   * Retrieve the timestamp for this ODE build. For example, 06/21/99 21:31
   *
   * @return the timestamp for this ODE build
   */
  public static String getOdeBuildTimestamp()
  {
    return OdeBuildTimestamp_;
  }

  /**********************************************************************
   * Print ODE version number. Remove the @(#) prefix.
   */
  public static void main( String[] args )
  {
    System.out.println(getOdeVersion()); // Print out Ode version.
  }
}
