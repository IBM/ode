/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1
 *
 * Date and Time File was last checked in: 5/10/03 15:30:24
 * Date and Time File was extracted/checked out: 06/04/13 16:46:48
 *****************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

import java.util.*;

/**
 * Container class for product and part metadata read from package control file.
 *
 * @version 1
 * @author  Mark DeBiase
 * @see     MvsShipData
 */
public class MvsPackageData
{
  /**
   * Construct a new MvsPackageData object.
   *
   * @param theProductData Hashtable containing the product data
   * @param theShipdataSet ArrayList containing MvsShipData objects for
   *                       each ship list entry
   */
  public MvsPackageData( Hashtable theProductData,
                         ArrayList theShipDataSet )
  {
    productData_ = theProductData;
    shipDataSet_ = theShipDataSet;
  }

  /**
   * Construct a new MvsPackageData object.
   *
   * @param theProductData Hashtable containing the product data
   * @param theShipdataSet ArrayList containing MvsShipData objects for
   *                       each ship list entry
   * @param noShippedFiles The number of shipped files in the package
   * Added to support NUMPARTS for B390 service packaging
   */
  public MvsPackageData( Hashtable theProductData,
                         ArrayList theShipDataSet,
                         int noShippedFiles )
  {
    productData_ = theProductData;
    shipDataSet_ = theShipDataSet;
    noShippedFiles_ = noShippedFiles;
  }

  /**
   * Return string representation of this object.
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("productData=").append(productData_.toString());
    sb.append(System.getProperty("line.separator"));
    sb.append("shipData=").append(shipDataSet_.toString());

    // noShippedFiles is not added here because it is a new attribute added
    // to support NUMPARTS for Service Packaging on MVS using Bld390
    // and the programs using this method does not need this attribute.
    // This attribute is only used by B390 service packaging for creating
    // a copysent driver.
    return sb.toString();
  }

  /**
   * Returns true if any file that is shipped as part of the FMID represented by
   * this object is of type VPL.
   */
  public boolean isAnyFileOfTypeVpl()
  {
    ArrayList hs = this.getShipDataSet();
    ListIterator hsi = hs.listIterator();
    while (hsi.hasNext())
    {
      MvsShipData shipData = (MvsShipData)hsi.next();
      if (shipData.isTypeVpl())
        return true;
    }
    return false;
  }

  /**
   * Returns a HashSet object that contains the filenames of all the files that
   * are of type VPL.
   */
  public HashSet getAllVplFiles()
  {
    // find all shippable parts with type VPL
    ArrayList hs = this.getShipDataSet();
    ListIterator hsi = hs.listIterator();
    HashSet vplFileSet = new HashSet();
    while (hsi.hasNext())
    {
      MvsShipData shipData = (MvsShipData)hsi.next();
      if (shipData.isTypeVpl())
      {
        vplFileSet.add(shipData.getSourceFile());
      }
    }
    return vplFileSet;
  }

  //***************************************************************************
  // "Getter" methods.
  //***************************************************************************
  public Hashtable getProductData()  { return productData_; }
  public ArrayList getShipDataSet()  { return shipDataSet_; }
  public int getNoShippedFiles() { return noShippedFiles_; }

  //***************************************************************************
  // Package data.
  //***************************************************************************
  private Hashtable productData_;  // product data tag value pairs
  private ArrayList shipDataSet_;  // set of MvsShipData objects
  private int noShippedFiles_ = 0;   // number of shipped files in the package
}
