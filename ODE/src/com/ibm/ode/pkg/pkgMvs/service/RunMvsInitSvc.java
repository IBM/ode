/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:45:24
 * Date and Time File was extracted/checked out: 04/10/04 09:13:29
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import com.ibm.ode.pkg.pkgMvs.MvsPackageData;
import com.ibm.ode.pkg.pkgMvs.MvsPkgError;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;
import com.ibm.ode.pkg.pkgMvs.MvsControlFile;

/**
 * Main class to drive the service transfer process for products
 * using Build/390 and ODE for service packaging.
 */
class RunMvsInitSvc
{
  public static void main( String[] args )
  {
    // make sure all required properties are set
    if (!MvsProperties.checkValues())
    {
      System.exit(1);
    }

    String pdtName = MvsProperties.pdtName;
    String initPdt = MvsProperties.initPdt;
    try
    {
      B390CommandInterface.setB390Path( MvsProperties.B390Path );
    }
    catch (B390CommandException e)
    {
      System.err.println(e.getMessage());
    }

    if (pdtName != null && pdtName.length() != 0)
    {
      try
      {
        if ((initPdt != null) && (initPdt.equalsIgnoreCase( "yes" )))
          B390CommandInterface.initB390DB( pdtName );
        Vector releaseVec = RunMvsInitSvc.getReleases();
        Enumeration releases = releaseVec.elements();
        while (releases.hasMoreElements())
        {
          B390Release release = (B390Release)releases.nextElement();

          // Determine the feature FMIDs of the base FMID if the featureFmids
          // attribute is not specified in the CMF for it.
          if (release.getBase().equals(release.getName()) &&
              release.getFeatureFMIDs() == null)
          {
            String features =
              getFeatures(releaseVec, release.getName());
            release.setFeatureFMIDs(features);
          }
          release.create();
          release.createCopySentDriver( pdtName );
          release.defineVersion( pdtName );
        }
      }
      catch (B390CommandException e)
      {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      catch (IOException e)
      {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      catch (InterruptedException e)
      {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }
  } //main()

  private static Vector getReleases() throws B390CommandException
  {
    Vector releases = new Vector();
    try
    {
      // open the control file
      String packageControlFile = MvsProperties.pkgControlDir + "pcd.mvs";
      MvsControlFile controlFile = new MvsControlFile( packageControlFile );
      MvsPackageData pkgData;
      while ( (pkgData = controlFile.nextPackage()) != null )
      {
        Hashtable pd = pkgData.getProductData();
        String mvsHLQ    = (String) pd.get("APPLID");     // MVS hlq
        String mvsRelease= (String) pd.get("FUNCTION");   // function FMID
        String baseRelease= (String) pd.get("FMID");      // base release
        String component = (String) pd.get("RETAINCOMPONENT");// retain component
        String retainRelease = (String)pd.get("RETAINRELEASE"); // retain release
        String systemRelease = (String) pd.get("SREL");   // system release
        String changeTeam    = (String) pd.get("CHANGETEAM");// system release
        String features  = (String) pd.get("FEATUREFMIDS"); // features
        int noShippedFiles = pkgData.getNoShippedFiles();
        B390Release release = new B390Release( mvsRelease, mvsHLQ, component,
                                               systemRelease, baseRelease,
                                               retainRelease, changeTeam,
                                               features, noShippedFiles );
        releases.addElement( release );
      }
      if (releases.isEmpty())
      {
        throw new B390CommandException(
          "Required field entity name not defined in the CMF.");
      }
    }
    catch (MvsPkgError e)
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    catch (B390CommandException e)
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return releases;
  }

  /**
   * This method gets the features for the specified base FMID
   *
   * @param releases a Vector of B390Release objects
   * @param name a valid base FMID
   * @return a space separated string of feature FMIDs for the specified base FMID.
   */
  private static String getFeatures( Vector releases, String name )
  {
    StringBuffer featureFmids = new StringBuffer("");
    Enumeration fmids = releases.elements();
    for (int idx = 0; fmids.hasMoreElements(); idx++)
    {
      B390Release releaseObj = (B390Release)fmids.nextElement();
      if (!releaseObj.getBase().equals(releaseObj.getName()) &&
          releaseObj.getBase().equals(name))
      {
        featureFmids.append(releaseObj.getName());
        featureFmids.append(" ");
      }
    }
    return featureFmids.toString();
  }

}
