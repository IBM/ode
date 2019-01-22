package com.ibm.ode.pkg.pkgMvs.service;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.Enumeration;
import java.util.Vector;

/* see com/ibm/ode/pkg/pkgCommon/PkgApiUtil.java*/
public class BPS_pkgapi 
{
  /******************************************************************
   * Constructor
   **/
  public BPS_pkgapi() 
  {
    super();
  }

  public static String[] getLevelMembersAsAPARs(String [] levels, String [] releases, String family) 
  {
    String [] apars={"APARone","APARtwo","APARthree"};
    return apars;
  }

  public static void addFMID_PTFPair( String ptfs[], String fmids[], String levels[], 
                                          String releases[], String family ) 
  {
    for (int i=0;i<ptfs.length;i++) 
    {
      System.out.println("--Adding pair - PTF: " + ptfs[i] + " FMID: " + fmids[i]);
    }
  }

  public static String[] getPTFsFromFMIDs( String fmids[], String level[], String release[], 
                                             String family ) 
  {
    String [] ptfs={"ptfHBPS112,P1152B1B","ptfJBPS112"};
    return ptfs;
  }

  public static void makeDuplicateLevels( String family, String release, String source_level, 
                                          String new_levels[] /*ptfs*/ )
  {
    System.out.println("--makeDuplicateLevels: family: " + family + " release: " + release);
    System.out.println("--Renaming level: " + source_level + " to: " + new_levels[0]);
    for (int i=1;i<new_levels.length;i++)
    {
      System.out.print("--Creating new level: " + new_levels[i]);
    }
    System.out.println();
  }

}
