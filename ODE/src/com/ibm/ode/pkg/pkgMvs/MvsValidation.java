package com.ibm.ode.pkg.pkgMvs;

import java.util.*;

/**
 * Class to provide validation of product metadata tags, part metadata tags,
 * SMP/E part types (from <PARTTYPE> metadata tag) and SMP/E keywords per
 * part type.
**/

public class MvsValidation
{
  //-----------------------------------------------------------------------
  // These HashSets will contain the values to be validated by the
  // validation routines.  The data in these sets is loaded the first
  // time the validation routine that uses it is called.
  //-----------------------------------------------------------------------
  private static HashSet prodMetadata_  = null;   // product metadata tags

  private static HashSet partMetadata_  = null;   // part metadata tags

  private static HashSet smpMcsTypes_   = null;   // SMP/E mcs types
  private static HashSet hfsMcsTypes_   = null;   // mcs types for HFS elements
  private static HashSet smpLangCodes_  = null;   // SMP/E language codes

  private static HashSet allMcsKwds_    = null;   // keywords for all MCS types
  private static HashSet macMcsKwds_    = null;   // keywords for ++MAC
  private static HashSet modMcsKwds_    = null;   // keywords for ++MOD
  private static HashSet dataMcsKwds_   = null;   // keywords for data elements
  private static HashSet hfsMcsKwds_    = null;   // keywords for ++HFS
  private static HashSet srcMcsKwds_    = null;   // keywords for ++SRC
  private static HashSet jclMcsKwds_    = null;   // keywords for ++JCL
  private static HashSet pgmMcsKwds_    = null;   // keywords for ++PROGRAM
  //-----------------------------------------------------------------------

  //***************************************************************************
  // All methods are static, but null constructor is provided to create a
  // reference to the class while it is in use so that it is not unloaded
  // during garbage collection and then staticly constructed againg on the
  // on next the next call to one of the validation routines.
  //***************************************************************************
  public void MvsValidation() {}

  //***************************************************************************
  // Validate the given product metadata tag - validates the tag name only
  //***************************************************************************
  public static boolean validateProductDataTag(String tag)
  {
    if (prodMetadata_ == null) createProdMetadataSet();

    if ( prodMetadata_.contains(tag) )
      return true;
    else
      return false;
  }

  //***************************************************************************
  // Validate the given part metadata tag - validates the tag name only
  //***************************************************************************
  public static boolean validatePartDataTag(String tag)
  {
    if (partMetadata_ == null) createPartMetadataSet();

    if ( partMetadata_.contains(tag) )
      return true;
    else
      return false;
  }

  //***************************************************************************
  // Validate the SMP/E MCS element given on the <PARTTYPE> metadata tag
  //***************************************************************************
  public static boolean validatePartType(String parttype)
  {
    createMcsSets();

    if ( smpMcsTypes_.contains(parttype) )
    {
      return true;
    }

    // check for data element with language code
    // (i.e., "BOOK" is invalid, but "BOOKENU" or "BOOKRUS" are valid)
    int len = parttype.length();
    if ( smpMcsTypes_.contains(parttype.substring(0, len-3) + "%") &&
         smpLangCodes_.contains(parttype.substring(len-3)) )
    {
      return true;
    }

    if (isHfsPartType( parttype ))
      return true;

    return false;
  }

  //***************************************************************************
  // Return true if the specified part is a valid HFS parttype
  //***************************************************************************
  public static boolean isHfsPartType(String parttype)
  {
    parttype = parttype.toUpperCase();
    if (hfsMcsTypes_ == null)
      createMcsSets();

    int len = parttype.length();
    if (hfsMcsTypes_.contains(parttype))
      return true;
    if (hfsMcsTypes_.contains(parttype.substring(0, len-3) + "%") &&
              smpLangCodes_.contains(parttype.substring(len-3)) )
      return true;

    return false;
  }

  //***************************************************************************
  // Validate the SMP/E MCS keywords in the part ship data by parttype.
  // This routine only validates that the mcs keywords represented by
  // the metadata tags are valid for the given parttype - not that the
  // values specified on the keywords are valid.
  // Returns a string containing the metadata tags representing MCS keywords
  // which are invalid for the parttype if any are found; otherwise returns
  // null.
  //***************************************************************************
  public static String validateMcsKeywords(String parttype, Hashtable shipData,
                                           String longData)
  {
    if (allMcsKwds_ == null) createMcsKeywordSets();

    // Create a HashSet which is the intersection between the ship data tags
    // from the keys of the Hashtable and the set of all MCS keywords.
    // This will give us a set that contains all the tags in the ship data
    // that represent MCS keywords.
    HashSet shipDataMcsKwds = new HashSet();
    Enumeration e = shipData.keys();
    while (e.hasMoreElements())
    {
      String tag = (String) e.nextElement();
      if ( allMcsKwds_.contains(tag) )
        shipDataMcsKwds.add(tag);
    }
    if (longData.length() != 0)
    {
      int beginIndex = 0;
      int endIndex = 0;
      // parsing longData with ")\n" as the delimiter and checking if each
      // token starts with either LINK, SYMLINK or SYMPATH
      while (endIndex != -1)
      {
        endIndex = longData.indexOf( ")\n", beginIndex);
        if (endIndex == -1)
          break;
        String tag = longData.substring(beginIndex, endIndex );
        beginIndex = endIndex + 2;

        if (tag.startsWith( "LINK" ))
        {
          shipDataMcsKwds.add( "LINK" );
        }
        else if (tag.startsWith( "SYMLINK" ))
        {
          shipDataMcsKwds.add( "SYMLINK" );
        }
        else if (tag.startsWith( "SYMPATH" ))
        {
          shipDataMcsKwds.add( "SYMPATH" );
        }
      }
    }

    // Do a set difference between the results of the intersection above
    // (which contains all the MCS keywords from the part ship data) and
    // the HashSet that contains all the valid keywords for the parttype.
    // If the resultant set is non-empty, it contains the keywords that
    // are invalid for the parttype.
    HashSet diff;
    if (parttype.equals("MAC"))            // ++MAC
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( macMcsKwds_ );
    }
    else if (parttype.equals("MOD"))       // ++MOD
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( modMcsKwds_ );
    }
    else if ((parttype.substring(0,3)).equals("HFS"))  // ++HFS
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( hfsMcsKwds_ );
    }
    else if (hfsMcsTypes_.contains( parttype ) )      // other HFS types
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( hfsMcsKwds_ );
    }
    else if (parttype.equals("SRC"))       // ++SRC
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( srcMcsKwds_ );
    }
    else if (parttype.equals("JCLIN"))     // ++JCLIN
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( jclMcsKwds_ );
    }
    else if (parttype.equals("PROGRAM"))     // ++PROGRAM
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( pgmMcsKwds_ );
    }
    else                                   // MCS data element
    {
      diff = new HashSet( shipDataMcsKwds );
      diff.removeAll( dataMcsKwds_ );
    }


    if (diff.isEmpty())
    {
      // no invalid keywords found
      return null;
    }
    else
    {
      // return a string listing the invalid keywords
      Iterator hsi = diff.iterator();
      StringBuffer invalidKwds = new StringBuffer();
      while (hsi.hasNext())
      {
        invalidKwds.append( (String) hsi.next() ).append(" ");
      }
      return invalidKwds.toString();
    }
  }

  //***************************************************************************
  // Create the HashSet that contins the list of valid product metadata tags.
  //***************************************************************************
  private static void createProdMetadataSet()
  {
    if (prodMetadata_ != null) return;   // already done

    // product metadata tags
    String[] productTags = { "APARS",        "APPLID",       
                             "COPYRIGHT",    "CPYDATE",      "EXTRASMPEFILE",
                             "DELETE",       "DESCRIPTION",  "DISTLIBS",
                             "FESN",         "FMID",         "FUNCTION",
                             "FUTURE",       "IF",           "NPRE",
                             "PRE",          "PREVIOUS",     "REQ",   "REWORK",
                             "SEP",          "RETAINRELEASE",  "SUP", 
                             "SREL",         "TYPE",         "RETAINCOMPONENT",
                             "VERSIONREQ",   "VPL",          "VPLACKN",
                             "VPLAUTHCODE",  "VPLAVAILDATE", "VPLFROMSYS",
                             "VPLMOD",       "VPLREL",       "VPLVER",
                             "JCLINLIB",     "CTLDEFINFILE", "DSNHLQ",
                             "CHANGETEAM",   "LKEDUNIT",     "DEL",
                             "FEATUREFMIDS",  "CREATEJCLINLIB" 
                           };

    prodMetadata_  = new HashSet();
    for (int i=0; i<productTags.length; i++)
    {
      prodMetadata_.add(productTags[i]);
    }
  }

  //***************************************************************************
  // Create the HashSet that contins the list of valid part metadata tags.
  //***************************************************************************
  private static void createPartMetadataSet()
  {
    if (partMetadata_ != null) return;   // already done

    // part metadata tags
    String[] partTags = { "ALIAS",      "ASSEM",    "BINARY",
                          "CALLLIBS",   "CSECT",
                          "DALIAS",     "DISTLIB",  "DISTMOD",
                          "DISTNAME",   "DISTSRC",  "HFSCOPYTYPE",
                          "LEPARM",     "LINK",     "LMOD",
                          "MALIAS",     "PARM",     "PARTTYPE",
                          "PREFIX",     "LIBRARYDD",
                          "SYSLIB",     "TALIAS",   "TEXT",
                          "TYPE",       "VERSION",  "VPLPARTQUAL",
                          "VPLSECURITY", "SYMLINK", "SYMPATH",
                          "SHSCRIPT",   "EXTATTR",  "SETCODE",
                          "HFSALIAS",   "PDSALIAS", "ENTRY",
                          "INCLUDE",    "JCLINMODE","LKEDTO",
                          "LKEDRC",     "ORDER",    "JCLINLKEDPARMS",
                          "SYSLIBS",    "USERID",   "GROUPID",
                          "PERMISSIONS","PDSLKEDNAME", "HFSLKEDNAME",
                          "LKEDPARMS",  "HFSSYSLIB", "PDSSYSLIB", "LKEDCOND",
                          "SIDEDECKAPPENDDD"
                        };

    partMetadata_  = new HashSet();
    for (int i=0; i<partTags.length; i++)
    {
      partMetadata_.add(partTags[i]);
    }
  }

  //***************************************************************************
  // Create the HashSets that contain the list of valid SMP/E MCS elements
  // and the language codes for data elements.
  //***************************************************************************
  private static void createMcsSets()
  {
    int i;

    if (smpMcsTypes_ == null)
                {

      // SMP/E mcs parttypes
      // Use % as placeholder in data elements that may require language code
      // so that we don't match to a tag specified without the lang code.
      // (i.e., "BOOKENU" or "BOOKRUS" are valid; "BOOK" is not)
      String[]  mcsArray = { "BOOK%",   "BSIND%", "CGM%",   "CLIST",
                             "DATA",    "DATA1",  "DATA2",  "DATA3",
                             "DATA4",   "DATA5",  "DATA6%", "EXEC",
                             "FONT%",   "GDF%",   "HELP%",  "IMG%",
                             "JCLIN",   "MAC",    "MOD",    "MSG%",
                             "PARM",    "PNL%",   "PROBJ%", "PROC", "PRODXML",
                             "PROGRAM", "PRSRC%", "PSEG%",  "PUBLB%",
                             "SAMP%",   "SKL%",   "SRC",    "TBL%",
                             "TEXT%",   "USER1",  "USER2",  "USER3",
                             "USER4",   "USER5",  "UTIN%",  "UTOUT%",
                             "BOOK",    "BSIND",  "CGM",    "DATA6",
                             "FONT",    "GDF",    "HELP",   "IMG",
                             "MSG",     "PNL",    "PROBJ",  "PRSRC",
                             "PSEG",    "PUBLB",  "SAMP",   "SKL",
                             "TBL",     "TEXT",   "UTIN",   "UTOUT"
                           };
      smpMcsTypes_ = new HashSet();
      for ( i=0; i<mcsArray.length; i++)
      {
        smpMcsTypes_.add(mcsArray[i]);
      }
    }

    if (hfsMcsTypes_ == null)
                {
      // SMP/E HFS part types
      String[] hfsArray = {
                          "AIX1",    "AIX2",    "AIX3",    "AIX4",    "AIX5",
                          "CLIENT1", "CLIENT2", "CLIENT3", "CLIENT4", "CLIENT5",
                          "OS21",    "OS22",    "OS23",    "OS24",    "OS25",
                          "UNIX1",   "UNIX2",   "UNIX3",   "UNIX4",   "UNIX5",
                          "WIN1",    "WIN2",    "WIN3",    "WIN4",    "WIN5",
                          "SHELLSCR","HFS",     "HFS%"
                           };

      hfsMcsTypes_ = new HashSet();
      for (i=0; i<hfsArray.length; i++)
      {
        hfsMcsTypes_.add(hfsArray[i]);
      }
    }

    if (smpLangCodes_ == null)
                {
      // SMP/E language codes
      String[] langArray = { "ARA", "CHS", "CHT", "DAN", "DES", "DEU",
                             "ELL", "ENG", "ENP", "ENU", "ESP", "FIN",
                             "FRA", "FRB", "FRC", "FRS", "HEB", "ISL",
                             "ITA", "ITS", "JPN", "KOR", "NLB", "NLD",
                             "NOR", "PTB", "PTG", "RMS", "RUS", "SVE",
                             "THA", "TRK"
                           };

      smpLangCodes_ = new HashSet();
      for (i=0; i<langArray.length; i++)
      {
        smpLangCodes_.add(langArray[i]);
      }
    }


  }

  //***************************************************************************
  // Create the HashSets that contain all the valid SMP/E MCS kewords and
  // the keywords valid for each MCS part type.
  //***************************************************************************
  private static void createMcsKeywordSets()
  {
    if (allMcsKwds_ != null) return;    // already done

    String[] allMcsKeywords  = { "ALIAS",    "ASSEM",   "BINARY",  "CALLLIBS",
                                 "CSECT",    "DALIAS",  "DISTLIB", "DISTMOD",
                                 "DISTNAME", "DISTSRC", "LEPARM",  "LINK",
                                 "LMOD",     "MALIAS",  "PARM",    "PREFIX",
                                 "SYSLIB",   "TALIAS",  "TEXT",    "VERSION",
                                 "HFSSYSLIB","PDSSYSLIB"
                               };
    String[] macMcsKeywords  = { "DISTLIB", "DISTNAME", "ASSEM",  "DISTMOD",
                                 "DISTSRC", "MALIAS",   "PREFIX", "SYSLIB",
                                 "VERSION"
                               };
    String[] modMcsKeywords  = { "DISTLIB", "DISTNAME", "CSECT",  "DALIAS",
                                 "LEPARM",  "LMOD",     "TALIAS", "VERSION",
                                 "SYSLIB",  "SYMPATH",  "SYMLINK","PARM",
                                 "HFSSYSLIB", "PDSSYSLIB"
                               };
    String[] dataMcsKeywords = { "DISTLIB", "DISTNAME", "ALIAS", "SYSLIB",
                                 "VERSION"
                               };
    String[] hfsMcsKeywords  = { "DISTLIB", "DISTNAME", "BINARY", "TEXT",
                                 "LINK",    "PARM",     "SYSLIB", "VERSION",
                                 "SYMLINK", "SYMPATH",  "SHSCRIPT"
                               };
    String[] srcMcsKeywords  = { "DISTLIB", "DISTNAME", "SYSLIB", "VERSION"
                               };
    String[] jclMcsKeywords  = { "DISTLIB", "DISTNAME", "CALLLIBS"
                               };
    String[] pgmMcsKeywords  = { "ALIAS", "DISTNAME", "DISTLIB", "SYSLIB",
                                 "VERSION"
                               };

    allMcsKwds_  = new HashSet();
    for (int i=0; i<allMcsKeywords.length; i++)
    {
      allMcsKwds_.add(allMcsKeywords[i]);
    }

    macMcsKwds_  = new HashSet();
    for (int i=0; i<macMcsKeywords.length; i++)
    {
      macMcsKwds_.add(macMcsKeywords[i]);
    }

    modMcsKwds_  = new HashSet();
    for (int i=0; i<modMcsKeywords.length; i++)
    {
      modMcsKwds_.add(modMcsKeywords[i]);
    }

    dataMcsKwds_  = new HashSet();
    for (int i=0; i<dataMcsKeywords.length; i++)
    {
      dataMcsKwds_.add(dataMcsKeywords[i]);
    }

    hfsMcsKwds_  = new HashSet();
    for (int i=0; i<hfsMcsKeywords.length; i++)
    {
      hfsMcsKwds_.add(hfsMcsKeywords[i]);
    }

    jclMcsKwds_  = new HashSet();
    for (int i=0; i<jclMcsKeywords.length; i++)
    {
      jclMcsKwds_.add(jclMcsKeywords[i]);
    }

    srcMcsKwds_  = new HashSet();
    for (int i=0; i<srcMcsKeywords.length; i++)
    {
      srcMcsKwds_.add(srcMcsKeywords[i]);
    }

    pgmMcsKwds_  = new HashSet();
    for (int i=0; i<pgmMcsKeywords.length; i++)
    {
      pgmMcsKwds_.add(pgmMcsKeywords[i]);
    }
  }
}
