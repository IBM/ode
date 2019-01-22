package com.ibm.ode.pkg.test;

import java.rmi.*;
import com.ibm.sdwb.bps.api.servicepkg.*;
import com.ibm.ode.pkg.api.PkgApiVrmf;
import com.ibm.ode.pkg.api.PkgApiVersion;
import java.util.*;

/**
* A test implementation of the ServicePackagingInterface that can be used with the
* TestPkgServer to test Service and/or Official packaging in ODE.
*
* The methods in this class have virtually no use, other that to provide
* simple implementations of all the required remote methods, as dictated
* by the remote interface ServicePackagingInterface. 
**/

public class TestPkgApi implements ServicePackagingInterface
{ 

  String name_;
  Hashtable ptfFmidPairs;

  public TestPkgApi () 
  {
    name_ = "TestPkgApi";
    ptfFmidPairs = new Hashtable();
  }

 public PkgApiVrmf getVrmf (String entityName, 
                            String familyName, 
                            String releaseName)
   throws java.rmi.RemoteException, ServicePackagingException
  {
    return (new PkgApiVrmf());
  }
 

 
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
   throws java.rmi.RemoteException, ServicePackagingException
  {
    // do something here
  }
 

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
   throws java.rmi.RemoteException, ServicePackagingException
  {
    System.out.println("in getLevelMembersAsApars");
    System.out.println("Getting APARS for: ");
    for (int i=0; i<levels.length; i++)
    {
      System.out.println(" level: " + levels[i] + " in release: " + release[i]);
    }
    String [] apars={"APARone","APARtwo","APARthree"};
    return apars;
  }

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
   throws java.rmi.RemoteException, ServicePackagingException
  {
    System.out.println("In makeDuplicateLevels");
    if (new_levels.length < 1)
      throw( new ServicePackagingException( "no levels to rename" ));
    System.out.println("would rename origigal level: " + source_level);
    System.out.println("               to new level: " + new_levels[0]);
    for (int i = 1; i < new_levels.length; i++)
    {
      System.out.println(" creating new level: " + new_levels[i]);
    }
  }

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
   throws java.rmi.RemoteException, ServicePackagingException
  {
    System.out.println("In makeDuplicateLevels");
    if (new_levels.length < 1)
      throw( new ServicePackagingException( "no levels to rename" ));
    System.out.println("would rename origigal level: " + source_level);
    System.out.println("               to new level: " + new_levels[0]);
    for (int i = 1; i < new_levels.length; i++)
    {
      System.out.println(" creating new level: " + new_levels[i]);
    }
  }
   
  
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
                             String[] fmids, 
                             String[] levels,
                             String[] releases,
                             String family) 
   throws java.rmi.RemoteException, ServicePackagingException
  {
    System.out.println("in addFMID_PTFPair");
    for (int i = 0; i < levels.length; i++)
      for (int j = 0; j < fmids.length; j++)
      {
        String key = family + ":" + releases[i] + ":" + levels[i] + ":" + fmids[j];
        String value = ptfs[j];
        System.out.println("storing key: " + key + " and value " + value);
        ptfFmidPairs.put( key, value );
      }
  }


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

 public String[] getPTFsFromFMIDs(String fmids[], 
                                  String levels[], 
                                  String releases[], 
                                  String family) 
   throws java.rmi.RemoteException, ServicePackagingException
  {
    String[] ptfs = new String[fmids.length];
    System.out.println("in getPTFsFromFMIDs");
    for (int i = 0; i < fmids.length; i++)
    {
      String key = family + ":" + releases[0] + ":" + levels[0] + ":" + fmids[i];
      String ptf = (String)ptfFmidPairs.get( key );
      if (ptf == null)
        throw (new ServicePackagingException( "PTF not found for key: " + key));
      ptfs[i] = ptf;
    }   
    return ptfs;
  }

	/**
	 *  Method to return to the packaging client the version of the PkgApi
   *  that is being used by the serer.
   **/
  public String getPackageApiVersion()  throws RemoteException
  {
    return( PkgApiVersion.getPkgApiVersion() ); 
  }

}
