//******************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1994, 1996.  All Rights Reserved.
//*
//*  
//* File, Component, Release: com/ibm/sdwb/bps/api/servicepkg/ServicePackagingInterface.java, servicepkg, ode5.0, 20041004.0550
//*  
//* Version: 1.2
//*  
//* Date and Time File was last checked in:       03/11/18 12:15:36
//* Date and Time File was extracted/checked out: 06/04/13 16:47:14
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BAN      F10996 Initial creation 
//******************************************************************************

package com.ibm.sdwb.bps.api.servicepkg;

import com.ibm.ode.pkg.api.PkgApiVrmf;

/**
 * This is the java interface that the servicepkg client will use to access the
 * BPS
 * @see com.ibm.sdwb.bps.api.servicepkg.servicepkg
 * @version 1.2 03/11/18
 * @author Arun  Balaraman
**/
public interface ServicePackagingInterface extends java.rmi.Remote
{
  /**
   * returns package api version
   * @param entityName  Entity Name
   * @param fmilyName   CMVC Family Name
   * @param releaseName CMVC Release Name
   * @return  PkgApiVrmf object
   * @exception  RemoteException
   */
 public String getPackageApiVersion() 
   throws java.rmi.RemoteException, ServicePackagingException;
 

  /**
   * This method will return a PkgApiVrmf object for a given entityName, 
   * familyName and releaseName or null if no record found
   * @param entityName  Entity Name
   * @param fmilyName   CMVC Family Name
   * @param releaseName CMVC Release Name
   * @return  PkgApiVrmf object
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public PkgApiVrmf getVrmf (String entityName, 
                            String familyName, 
                            String releaseName)
   throws java.rmi.RemoteException, ServicePackagingException;
 

 
 /**
   * Stores Vrmf in BPS using entityName, familyName and releaseName as key
   * @param entityName  Entity Name
   * @param fmilyName   CMVC Family Name
   * @param releaseName CMVC Release Name
   * @param vrmf        PkgApiVrmf object
   * @return  void
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public void setVrmf(String entityName,
                     String familyName,
                     String releaseName,
                     PkgApiVrmf vrmf) 
   throws java.rmi.RemoteException, ServicePackagingException;
 

  /**
   * This method will return the level members as the corresponding APAR names.
   * Each Level entry corresponds to the release array entry that it 
   * represents in BPS using entityName, familyName and releaseName as key
   * 
   * @param levels   String[] of level names
   * @param releases String[] of release names
   * @param family   CMVC family name
   * @return  String[] level member names
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public String[] getLevelMembersAsApars(String[] levels, 
                                        String[] release, 
                                        String family)
   throws java.rmi.RemoteException, ServicePackagingException;

 /**
   * This method will return the level members as the corresponding APAR names.
   *  Each Level entry corresponds to the release array entry that it 
   *  represents in BPS using entityName, familyName and releaseName as key
   * @param family       CMVC family name
   * @param releases     CMVC Release name
   * @param sourcelevels source level name
   * @param levelType    level type
   * @return   void
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public void makeDuplicateLevels(String family,
                                 String release,
                                 String source_level,
                                 String[] new_levels,
                                 String levelType) 
   throws java.rmi.RemoteException, ServicePackagingException;

  /**
   * This method will return the level members as the corresponding APAR names.
   *  Each Level entry corresponds to the release array entry that it 
   *  represents in BPS using entityName, familyName and releaseName as key
   * @param family       CMVC family name
   * @param releases     CMVC Release name
   * @param sourcelevels source level name
   * @return   void
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public void makeDuplicateLevels(String family,
                                 String release,
                                 String source_level,
                                 String[] new_levels) 
   throws java.rmi.RemoteException, ServicePackagingException;
   
  
   /**This method will store the PTF numbers the corresponds to each FMID for 
   * the family/release/level combination (key). This will allow overwriting 
   * data if there is already an entry in the DB for this key. 
   * @param family       CMVC family name
   * @param releases     CMVC Release name
   * @param sourcelevels source level name
   * @return   void
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */
 public void addFMID_PTFPair(String[] ptfs, 
                             String[] fimds, 
                             String[] levels,
                             String[] releases,
                             String family) 
   throws java.rmi.RemoteException, ServicePackagingException;


  /**
   * Retrieve the associated PTF number for the FMID/level/release/family 
   * combination (key). The ptfs[] returned will be in the same order
   * as the fmids[] array. Basically, a given entry in the fmids array will 
   * map to the cooresponding entry in the ptfs array.
   * @param ptfs        String[] of ptf names
   * @param fmids       String[] of fmid names
   * @param levels      String[] of level names
   * @param releases    String[] or release names
   * @param family      CMVC family name
   * @return  void
   * @exception  RemoteException
   * @exception  ServicePackagingException
   */

 public String[] getPTFsFromFMIDs(String fimds[], 
                                  String levels[], 
                                  String release[], 
                                  String family) 
   throws java.rmi.RemoteException, ServicePackagingException;
}
