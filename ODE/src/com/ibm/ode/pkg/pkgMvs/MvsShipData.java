package com.ibm.ode.pkg.pkgMvs;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Container class for parsing and storing MVS part metadata read from 
 * the package control file.  Each MvsShipData object represents a single
 * ship list entry.
 * @version 1.5 99/10/27
 * @author  Mark DeBiase
**/

class MvsShipData
{
  //***************************************************************************
  // Construct a new MvsShipData object.
  // 
  // Parameters: theSourceFile  source file name
  //             theTrackList   list of tracks associated with the file
  //                            or null for no tracks (i.e. ipp package)
  //             theFileType    type of data set:
  //                            MvsShipData.PO  = partitioned
  //                            MvsShipData.PS  = sequential 
  //                            MvsShipData.HFS = hfs file
  //             theTypeString  value associated with <TYPE> tag
  //             theShipData    a Hashtable containing the tag value metadata
  //                            for the ship list entry
  //***************************************************************************
  public MvsShipData(String     theSourceFile,
                     String     theTrackList,
                     byte       theFileType,
                     String     theTypeString,
                     Hashtable  theShipData)
  {
    sourceFile_ = theSourceFile;
    trackList_  = theTrackList;
    fileType_   = theFileType;

    // set ship data type based on typeString values:
    if ( theTypeString.indexOf("IPP") != -1 )  typeIpp_ = true;
    else                                       typeIpp_ = false;
    if ( theTypeString.indexOf("VPL") != -1 )  typeVpl_ = true;
    else                                       typeVpl_ = false;

    shipData_   = theShipData;
  }

  //***************************************************************************
  // Construct a new MvsShipData object.
  // 
  // Parameters: theSourceFile  source file name
  //             theTrackList   list of tracks associated with the file
  //                            or null for no tracks (i.e. ipp package)
  //             theFileType    type of data set:
  //                            MvsShipData.PO  = partitioned
  //                            MvsShipData.PS  = sequential 
  //                            MvsShipData.HFS = hfs file
  //             theTypeString  value associated with <TYPE> tag
  //             theShipData    a Hashtable containing the tag value metadata
  //                            for the ship list entry
  //             theLongData    a string containing the SYMLINK, SYMPATH, and 
  //                            LINK values that should go into a dataset
  //***************************************************************************
  public MvsShipData(String     theSourceFile,
                     String     theTrackList,
                     byte       theFileType,
                     String     theTypeString,
                     Hashtable  theShipData,
                     String     theLongData)
  {
    sourceFile_ = theSourceFile;
    trackList_  = theTrackList;
    fileType_   = theFileType;

    // set ship data type based on typeString values:
    if ( theTypeString.indexOf("IPP") != -1 )  typeIpp_ = true;
    else                                       typeIpp_ = false;
    if ( theTypeString.indexOf("VPL") != -1 )  typeVpl_ = true;
    else                                       typeVpl_ = false;

    shipData_   = theShipData;
    longData_   = theLongData;
  }

  //***************************************************************************
  // Parse a String containing a single ship list entry and return a
  // MvsShipData object representing the ship list entry.
  //***************************************************************************
  public static MvsShipData parseShipDataString(String shipDataString)
                            throws MvsPkgError
  {
    StringTokenizer st = new StringTokenizer(shipDataString, " ");

    String sourceFile = st.nextToken();
    String trackList  = st.nextToken();
    String longData  = "";
  
    if (trackList.equals("NONE")) trackList = null;

    byte fileType;
    if (sourceFile.indexOf("(") != -1)          // partitioned data set
      fileType = PO;
    else if (sourceFile.indexOf("/") != -1)     // hfs file
      fileType = HFS;
    else                                        // sequential data set
      fileType = PS;

    if (fileType != HFS)   // don't uppercase HFS file names
      sourceFile = sourceFile.toUpperCase(); 
    
    String     typeString = null;
    Hashtable  ht = new Hashtable();
    String     tag, val;

    // parse each ship data tag
    while ( st.hasMoreTokens() )
    {
      tag = st.nextToken("<>").trim().toUpperCase();
      if (tag.length()==0) continue;//Bug in JDK1.3 Classic VM (build 1.3.0, J2RE 1.3.0 IBM build hm130-20001128 (JIT enabled: jitc)

      if (!MvsValidation.validatePartDataTag(tag))
      {
        throw new MvsPkgError(MvsPkgError.invalidPartTag2,
                              new Object[] {tag, sourceFile});
      }

      if ( tag.equals("TYPE") )
      {
        typeString = st.nextToken().trim().toUpperCase();
      }
      else
      {

         if ( tag.equals("SYMLINK") || tag.equals("LINK") 
                                      || tag.equals("SYMPATH") )
         {
            longData += "  " + tag;
            longData += "(\n"; 
            StringBuffer sb = new StringBuffer(st.nextToken().trim());
            longData += formatLine72(sb);
            longData += "\n  )"; 
         }
         else
         { 
            val = upperCase(tag, st.nextToken().trim());
            String s = (String) ht.put(tag, val);
            if ( s != null )
            {
               System.out.println("Warning: Duplicate " + tag +
                             " tag found in ship data for " + sourceFile);
               System.out.println("         Keeping current value    : " + val);
               System.out.println("         Discarding previous value: " + s);
            }
          }
       }
    }

    if ( typeString == null )
    {
      // <TYPE> tag is required....
      throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                            new Object[] {"TYPE", sourceFile});
    }
    else
    {
      return new MvsShipData(sourceFile, trackList, fileType, 
                             typeString, ht, longData);
    }
  }

  //***************************************************************************
  // Return objects hash code value based on source file name.
  //***************************************************************************
  public int hashCode()
  {
    return sourceFile_.hashCode();
  }

  //***************************************************************************
  // Test for equality with another MvsShipData object based on source file.
  //***************************************************************************
  public boolean equals(Object otherMvsShipData)
  {
   return sourceFile_.equals( ((MvsShipData)otherMvsShipData).getSourceFile() );
  }

  //***************************************************************************
  // Return string representation of this object.
  //***************************************************************************
  public String toString()
  {
    StringBuffer sb = new StringBuffer(160);
    
    sb.append("\n-->");                   // make it easier to see each entry
    sb.append(sourceFile_).append(",");
    sb.append(trackList_).append(",");
    switch (fileType_)
    {
      case PO:  sb.append("PO,"); break;
      case PS:  sb.append("PS,"); break;
      case HFS: sb.append("HFS,"); break;
    }
    sb.append("[");
    if (typeIpp_) sb.append("IPP ");
    if (typeVpl_) sb.append("VPL ");
    sb.append("],"); 
    sb.append(shipData_.toString());
    sb.append(longData_);

    return sb.toString();
  }

  //****************************************************************************
  // Format input to SMP/E so that the line does not exceed column 72.
  //****************************************************************************
  private static String formatLine72(StringBuffer line)
  {
    int lineLength = line.length();

    if (lineLength <= 72) return line.toString();   // no continuation needed

    // number of continuations needed
    int continues = lineLength / 72;

    // reserve space for each "\n" to be added
    line.ensureCapacity(lineLength + (continues));

    for (int i=1; i<=continues; i++)
    {
      // x marks the spot to insert continuation
      int x = (72 * i);
      x += (i - 1);
      line.insert(x, "\n");
    }

    return line.toString();

  } //formatLine72()

  //***************************************************************************
  // Conditionally convert values to uppercase.
  //***************************************************************************
  private static String upperCase(String tag, String val)
  {
    // any tags whose values should not be uppercased added here...
    // LINK is ++HFS link name
    // PARM is ++HFS copy utility parm string
    if (tag.equals("LINK") || 
        tag.equals("PARM") ||
        tag.equals("SYMLINK") ||
        tag.equals("SYMPATH") ||
        tag.equals("SHSCRIPT"))
      return val;
    else
      return val.toUpperCase(); 
  }

  //***************************************************************************
  // "Getter" methods.
  //***************************************************************************
  public String    getSourceFile() { return sourceFile_; }
  public String    getTrackList()  { return trackList_; }
  public byte      getFileType()   { return fileType_; }
  public boolean   isTypeIpp()     { return typeIpp_; }
  public boolean   isTypeVpl()     { return typeVpl_; }
  public Hashtable getShipData()   { return shipData_; }
  public String    getLongData()   { return longData_; }

  //***************************************************************************
  // Type of MVS data sets
  //***************************************************************************
  public static final byte PO  = 0;      // partitioned data set
  public static final byte PS  = 1;      // sequential data set
  public static final byte HFS = 2;      // HFS file

  //***************************************************************************
  // Ship data 
  //***************************************************************************
  private String    sourceFile_;
  private String    trackList_;
  private byte      fileType_;
  private boolean   typeIpp_;
  private boolean   typeVpl_;
  private Hashtable shipData_;
  private String    longData_;
}
