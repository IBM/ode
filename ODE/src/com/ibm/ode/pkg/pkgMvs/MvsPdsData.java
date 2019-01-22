//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgMvs/MvsPdsData.java, pkgMvs, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.2
//*
//* Date and Time File was last checked in:       97/09/30 10:31:58
//* Date and Time File was extracted/checked out: 99/04/25 09:14:39
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* MAD      F 1463  Initial creation
//*
//*****************************************************************************

package com.ibm.ode.pkg.pkgMvs;


/**
 * Container class used by MvsJclGenerator class to store the source file name
 * and the distlib name for each shippable part whose source is in a PDS.
 * MvsPdsData objects are stored in an OrderedSet (order by source file name) to
 * facilitate the generation of IEBCOPY gather steps for each distlib.
 * @version 1.2 97/09/30
 * @author  Mark DeBiase 
**/

class MvsPdsData
{
  //***************************************************************************
  // Construct a MvsPdsdata object.
  //
  // Parameters: thSrcFile   - partitioned data set & member name
  //             theDistName - the distname for the part
  //***************************************************************************
  public MvsPdsData(String theSrcFile,
                    String theDistName)
  {
    srcFile_   = theSrcFile;
    distName_  = theDistName;
  }

  //***************************************************************************
  // Returns the PDS data set name of the source file     
  //***************************************************************************
  public String getSrcLib()
  {
    int lparen = srcFile_.indexOf("("); 

    return srcFile_.substring(0,lparen);
  }

  //***************************************************************************
  // Returns the member name of the source file
  //***************************************************************************
  public String getSrcName()
  {
    int lparen = srcFile_.indexOf("("), 
        rparen = srcFile_.indexOf(")");

    return srcFile_.substring(lparen+1,rparen);
  }
   
  //***************************************************************************
  // Test it this object is equal to another based on source file name.
  //***************************************************************************
  public boolean equals(Object o)
  { return ((MvsPdsData) o).getSrcFile().equals(srcFile_); }

  //***************************************************************************
  // Return objects hash code value based on source file name.
  //***************************************************************************
  public int hashCode() { return srcFile_.hashCode(); }

  //***************************************************************************
  // Return string representation of this object as source file name.
  // This exact return value is required because PdsData objects will be 
  // stored in an OrderedSet with a LessString() binary predicate used to
  // order the set.  The LessString predicate orders objects based on 
  // String1.compareTo(String2) & the PdsData objects are to be ordered
  // based on the source file name.
  //***************************************************************************
  public String toString() { return srcFile_; }

  //***************************************************************************
  // "Getter" methods.
  //***************************************************************************
  public String getSrcFile()   { return srcFile_; }
  public String getDistName()  { return distName_; } 

  //***************************************************************************
  // Private data.
  //***************************************************************************
  private String srcFile_;
  private String distName_;
}
