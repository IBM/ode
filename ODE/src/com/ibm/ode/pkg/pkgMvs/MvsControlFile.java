/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:30:04
 * Date and Time File was extracted/checked out: 06/04/13 16:46:39
 *******************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Class to read product and ship metadata from package control file.
 * @version 1.2
 * @author  Mark DeBiase
 * @see     MvsPackageData
 */
public class MvsControlFile
{
  /**
   * Default Contructor. Calls Contructor that takes a String as
   * parameter. The value passed to that contructor is
   * MvsProperties.pkgControlDir + 'pcd.mvs'.
   */
  public MvsControlFile()
    throws MvsPkgError
  {
    this(MvsProperties.pkgControlDir + "pcd.mvs");
  }

  /**
   * Construct a MvsControlFile object for the given control file name.
   */
  public MvsControlFile( String mvsControlFileName ) throws MvsPkgError
  {
    cntlFileName_ = mvsControlFileName;

    try
    {
      controlFile_ = new BufferedReader(new FileReader(cntlFileName_));
      pkgNum_      = 0;
    }
    catch (FileNotFoundException e)
    {
      throw new MvsPkgError(MvsPkgError.fileNotFound2,
                new Object[] {"package control", cntlFileName_});
    }
    catch (IOException e)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"opening", cntlFileName_, e.getMessage()});
    }
  }

  /**
   * Read the next package from the control file.
   * Each package starts with a PACKAGE: tag.
   * The product metadata immediately follows the PACKAGE: tag.
   * The part metadata starts after the SHIP_LIST: tag.
   * The package ends with the END_PACKAGE: tag.
   *
   * @return MvsPackageData object representing the next package in the
   *         control file or null for no more packages.
   */
  public MvsPackageData nextPackage() throws MvsPkgError
  {
    String     lineFromFile;
    boolean    found;
    Hashtable  productData = null;
    boolean    duplicates = true;
    ArrayList  shipDataSet = new ArrayList();
    int noShippedFiles = 0;

    try
    {
      // read to next PACKAGE: tag
      found = false;
      lineFromFile = controlFile_.readLine();
      while ( lineFromFile != null && !found )
      {
        lineFromFile = lineFromFile.trim().toUpperCase();
        if ( lineFromFile.equals("PACKAGE:") )
        {
          found = true;
        }
        else
        {
          lineFromFile = controlFile_.readLine();
        }
      }
      if ( lineFromFile == null )
      {
        controlFile_.close();
        return null;   // no more packages...
      }

      // increment package number
      pkgNum_++;
      System.out.println("Reading package " + pkgNum_ + " from control file");

      // read product data & save into a string buffer
      StringBuffer  prodDataSB = new StringBuffer(1000);
      found = false;
      lineFromFile = controlFile_.readLine();
      while ( lineFromFile != null && !found )
      {
        lineFromFile = lineFromFile.trim();
        if ( lineFromFile.toUpperCase().equals("SHIP_LIST:") )
        {
          found = true;
        }
        else
        {
          prodDataSB.append(lineFromFile).append(" ");
          lineFromFile = controlFile_.readLine();
        }
      }
      productData= MvsProductData.parseProductDataString(prodDataSB.toString());
      prodDataSB = null;   // for garbage collection

      // read part data and save in HashSet
      found = false;
      lineFromFile = controlFile_.readLine();
      while ( lineFromFile != null && !found )
      {
        if ( lineFromFile.trim().toUpperCase().equals("END_PACKAGE:") )
        {
          found = true;
        }
        else
        {
          // Number of shipped files is needed for B390 service packaging
          noShippedFiles++;
          MvsShipData shipData = MvsShipData.parseShipDataString(lineFromFile);
          shipDataSet.add(shipData);
          lineFromFile = controlFile_.readLine();
        }
      }
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"reading", cntlFileName_, ex.getMessage()});
    }

    // return MvsPackageData object
    return new MvsPackageData(productData, shipDataSet, noShippedFiles);
  }

  /**
   * Read all the packages from the control file and return them as a
   * vector of MvsPackageData objects.
   */
  public Vector readAllPackages() throws MvsPkgError
  {
    MvsPackageData pkgData;
    Vector v = new Vector();

    while ( (pkgData = nextPackage()) != null)
    {
      v.addElement(pkgData);
    }

    return v;
  }

  /**
   * Returns the package number of the last package read from the control file.
   */
  public int getPkgNum()
  {
    // returns number of package just read from control file
    return pkgNum_;
  }

  /**
   * Close the package control file.  The package control file is only
   * closed on object finalization.
   */
  protected void finalize() throws Throwable
  {
    if ( controlFile_ != null ) controlFile_.close();
  }

  private BufferedReader  controlFile_ = null;
  private int             pkgNum_;
  private String          cntlFileName_;


  /**
   * Main program for testing purposes.  Reads each package in the specified
   * control file and prints each package (MvsPackageData object) read.
   *
   * @param the name of the control file
   */
  public static void main( String[] args )
  {
    if ( args.length == 0 )
    {
      System.err.println("usage: MvsControlFile fileName [1|A]");
      System.err.println("       1 - call nextPackage() for each package" +
                         " (default)" );
      System.err.println("       A = call readAllPackages()");
      System.exit(1);
    }

    try
    {
      MvsControlFile cf = new MvsControlFile(args[0]);

      if (args.length == 2 && args[1].equals("A"))
      {
        System.out.println("calling readAllPackages()");
        Vector v = cf.readAllPackages();
        for (int i=0; i<v.size(); i++)
        {
          System.out.println( (MvsPackageData) v.elementAt(i));
        }
      }
      else
      {
        System.out.println("calling nextPackage() for each package");
        MvsPackageData pd;
        pd = cf.nextPackage();
        while ( pd != null )
        {
          System.out.println(pd);
          pd = cf.nextPackage();
        }
      }
    }
    catch( MvsPkgError e )
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}

