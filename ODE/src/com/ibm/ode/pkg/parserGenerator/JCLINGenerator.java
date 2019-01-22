/*******************************************************************************
 *
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator ;

import java.io.FileOutputStream;
import java.util.*;
import com.ibm.ode.lib.string.PlatformConstants;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;

/**
 *
 */
public class JCLINGenerator extends Generator
{
  /**
   * This holds a list of part names that need an entry in the JCLIN file. This
   * is populated by reading the B390_JCLIN_FILES property when generateKeyLists
   * method is called for the first time.
   */
  private static Vector jclinParts = new Vector();
  
  private EntityTreeObject eto_;
  private InstallEntity curIE_;
  private FileOutputStream jclinFile_;
  private Vector pdsKeys_;
  private Vector hfsKeys_;

  /**
   * Creates a new instance of type JCLINGenerator.
   */
  public JCLINGenerator( EntityTreeObject curEto )
  {
    eto_ = curEto;
    curIE_ = eto_.getInstallEntityReference();
    pdsKeys_ = new Vector();
    hfsKeys_ = new Vector();

    context_ = PlatformConstants.CURRENT_MACHINE;
  }

  /**
   * Main method to drive calling of other methods to generate JCLIN
   */
  void generateJCLIN() throws GeneratorException
  {
     generateKeyLists();
     writeJCLIN();
  }
  
  /**
   * Method to populate lists of jobstep keys form both HFS and PDS
   * link-edit jobsteps
   *
   * NOTE: Using arrays instead of hashtables/lists for the PDS/HFS
   *  keys to avoid potential problems with the hash codes of long but
   *  similar strings hashing to the same value.
   */
  void generateKeyLists() throws GeneratorException
  {
    ArrayList fileEntityArray = eto_.getChildReferenceArray();
    ArrayList pkgDataArray;
    ListIterator pkgDataIterator;
    EntityTreeObject childEto;
    FileEntity curFE;
    String fileType;
    String keyStr;
    String lkedTo;
    String pkgClass = MvsProperties.pkgClass;
    
    // If in service packaging, parse the B390_JCLIN_FILES property and populate
    // the vector with the parts.
    if (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._servicePkg_) &&
        jclinParts.size() == 0)
    {
       if (MvsProperties.jclinFiles != null)
       {
          StringTokenizer tokenizer = 
             new StringTokenizer(MvsProperties.jclinFiles, " ,");
          while (tokenizer.hasMoreTokens())
          {
             jclinParts.addElement(tokenizer.nextToken());
          }
          tokenizer = null;
       }
    }
    
    // Loop through each FileEntity in the current InstallEntity
    for (int i = 0; i < fileEntityArray.size(); i++)
    {
      childEto = (EntityTreeObject)fileEntityArray.get(i);
      curFE = childEto.getFileEntityReference();
      pkgDataArray = curFE.getPackageData();
      
      // Loop through each PackageData in the current FileEntity
      pkgDataIterator = pkgDataArray.listIterator();
      while ( pkgDataIterator.hasNext() )
      {
        PackageData curPD = (PackageData)pkgDataIterator.next();
        fileType = curPD.getFileType();
        lkedTo = curPD.getLkedTo();
        
        // Only considering MOD parts with lkedTo specified parts for JCLIN 
        // generation. In service packaging, JCLIN entries are generated only 
        // for the parts listed in the B390_JCLIN_FILES property and that are 
        // of type MOD and that have lkedTo specified.
        if (fileType != null && fileType.equalsIgnoreCase("mod") && 
            lkedTo != null &&
            (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._ippPkg_) ||
             (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._servicePkg_) &&
              this.isJCLINPart(jclinParts, this.getPartName(curPD))))
           )
        {          
          if (!lkedTo.equalsIgnoreCase("pds") &&
              !lkedTo.equalsIgnoreCase("hfs") &&
              !lkedTo.equalsIgnoreCase("both"))
          {
            throw new GeneratorException(
                "lkedTo attribute must be \"HFS\", \"PDS\", or \"BOTH\"" 
                + " for JCLIN processing.");
          }
          
          // Must at least have pdsLkedName and jclinLkedParms for PDS JCLINs
          if ((lkedTo.equalsIgnoreCase("pds") || 
               lkedTo.equalsIgnoreCase("both")) &&
             ((curPD.getPdsLkedName() == null) || 
              (curPD.getJclinLkedParms() == null)))
          {
            throw new GeneratorException(
              "Must specify pdsLkedName and jclinLkedParms " +
              "attributes for JCLIN processing of PDS ++MOD parts.");
          }
          
          // Must at least have hfsLkedName and jclinLkedParms for HFS JCLINs
          if ((lkedTo.equalsIgnoreCase("hfs") || 
               lkedTo.equalsIgnoreCase("both")) &&
             ((curPD.getHfsLkedName() == null) || 
              (curPD.getJclinLkedParms() == null)))
          {
            throw new GeneratorException(
               "Must specify hfsLkedName and jclinLkedParms " + 
               "attributes for JCLIN processing of HFS ++MOD parts.");
          }
          
          // If part is to be link-ediited to PDS
          if (lkedTo.equalsIgnoreCase("pds") || 
              lkedTo.equalsIgnoreCase("both"))
          {
            keyStr = getJCLINKey(curPD, "pds");
            if (!pdsKeys_.contains(keyStr)) 
              pdsKeys_.addElement(keyStr);
          }
          
          // If part is to be link-ediited to HFS
          if (lkedTo.equalsIgnoreCase("hfs") || 
              lkedTo.equalsIgnoreCase("both"))
          {
            keyStr = getJCLINKey(curPD, "hfs");
            if (!hfsKeys_.contains(keyStr))
              hfsKeys_.addElement(keyStr);            
          }
        }
      }
    } 
  }

  /**
   * Method to write the JCLIN to the appropriate output file
   */
  void writeJCLIN() throws GeneratorException
  {
    ArrayList fileEntityArray = eto_.getChildReferenceArray();
    ArrayList pkgDataArray;
    ListIterator pkgDataIterator;
    EntityTreeObject childEto;
    FileEntity curFE;
    boolean isFirstInStep;
    int stepCount = 1;
    
    // If there are no entries in the pdsKeys_ and hfsKeys_ ArrayList objects, it 
    // means nothing to process. Just return.
    if (pdsKeys_.size() == 0 && hfsKeys_.size() == 0)
    {
       return;
    }

    jclinFile_ = openFile(MvsProperties.pkgControlDir, 
                          curIE_.getEntityName().toUpperCase() + ".jclin" );
    String dsnHlq = curIE_.getDsnHlq();
    if (dsnHlq == null)
    {
      dsnHlq = "SYS1";
    }
    String pkgClass = MvsProperties.pkgClass;

    // Loop through each PDS key
    for (int idx = 0; idx < pdsKeys_.size(); idx++)
    {
      isFirstInStep = true;
      String curPdsKey = (String)pdsKeys_.elementAt( idx );

      // Loop through each FileEntity in the current InstallEntity
      for (int i = 0; i < fileEntityArray.size(); i++)
      { 
        childEto = (EntityTreeObject)fileEntityArray.get(i);
        curFE = childEto.getFileEntityReference();
        pkgDataArray = curFE.getPackageData();

        // Loop through each PackageData in the current FileEntity
        pkgDataIterator = pkgDataArray.listIterator();
        while ( pkgDataIterator.hasNext() )
        {
          PackageData curPD = (PackageData)pkgDataIterator.next();
          if (curPdsKey.equals(getJCLINKey(curPD, "pds")) && 
              (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._ippPkg_) ||
               (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._servicePkg_) &&
                this.isJCLINPart(jclinParts, this.getPartName(curPD))
               )
              )
             )
          {
            if ((curPD.getPdsLkedName() != null) && 
                (curPD.getJclinLkedParms() != null) &&
                (curPD.getLkedTo() != null))
            {
              if (isFirstInStep)
              {
                writeString(jclinFile_, 
                            createJCLINHeader(curPD, "pds", stepCount, dsnHlq));
                isFirstInStep = false;
                stepCount++;
              }   
              writeString(jclinFile_, createJCLINEntry(curPD, "pds"));
            }
          }
        }
      }
    }

    // Loop through each HFS key
    for (int idx = 0; idx < hfsKeys_.size(); idx++)
    {
      isFirstInStep = true;
      String curHfsKey = (String)hfsKeys_.elementAt(idx);  

      // Loop through each FileEntity in the current InstallEntity
      for (int i = 0; i < fileEntityArray.size(); i++)
      {
        childEto = (EntityTreeObject)fileEntityArray.get(i);
        curFE = childEto.getFileEntityReference();
        pkgDataArray = curFE.getPackageData();

        // Loop through each PackageData in the current FileEntity
        pkgDataIterator = pkgDataArray.listIterator();
        while ( pkgDataIterator.hasNext() )
        {
          PackageData curPD = (PackageData)pkgDataIterator.next();
          if (curHfsKey.equals(getJCLINKey(curPD, "hfs")) &&
              (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._ippPkg_) ||
              (pkgClass.equalsIgnoreCase(ParserGeneratorInitiator._servicePkg_) &&
               this.isJCLINPart(jclinParts, this.getPartName(curPD))))
             )
          {
            if ((curPD.getHfsLkedName() != null) && 
                (curPD.getJclinLkedParms() != null) &&
                (curPD.getLkedTo() != null))
            {
              if (isFirstInStep)
              {
                writeString(jclinFile_, 
                            createJCLINHeader(curPD, "hfs", stepCount, dsnHlq));
                isFirstInStep = false;
                stepCount++;
              }
              writeString(jclinFile_, createJCLINEntry(curPD, "hfs"));
            } 
          }
        }
      }
    }
    closeFile(jclinFile_);
  }

  /**
   * Returns the JCLIN jobstep key for a given PackageData object
   *
   * @param curPD The current PackageData object
   * @param type The type of object being processed (pds or hfs)
   *
   * @return The key for the specified PackageData object
   */
  String getJCLINKey( PackageData curPD, String type ) 
  {
    String     key;
    String     distLib;
    ArrayList  tmpSysLibs;
    String     sysLibs = "";
    ArrayList  tmpExtIncs;
    String     extIncs = "";
    String     partInfoSysLibs;
    String     lkedParms;
    String     tempStr, sep;

    // Cannot have any null values in key
    distLib = curPD.getTargetDir();
    if (distLib == null) distLib = "";
    lkedParms = curPD.getJclinLkedParms();
    if (lkedParms == null) lkedParms = "";

    partInfoSysLibs = getPartInfoSysLib(curPD, type);

    tmpExtIncs = curPD.getInclude();
    if (tmpExtIncs != null && !tmpExtIncs.isEmpty())
    {
      sep = "";
      for (int i = 0; i < tmpExtIncs.size(); i++)
      {
        tempStr = (String)tmpExtIncs.get(i);
          
        // Strip off extraneous part of external includes
        // AED1001(EDCA001),AED1002(EDCB002,EDCB003) to AED1001,AED1002
        if (tempStr.indexOf("(") >= 0)
          tempStr = tempStr.substring(0, tempStr.indexOf("("));
        extIncs += sep + tempStr;
        sep = ",";
      }
    }

    tmpSysLibs = curPD.getSysLibs();
    if (tmpSysLibs != null && !tmpSysLibs.isEmpty())
    {
      sep = "";
      for (int i = 0; i < tmpSysLibs.size(); i++)
      {
        tempStr = (String)tmpSysLibs.get(i);
        sysLibs += sep + tempStr;
        sep = ",";
      }
    }

    String sideDeckDD = curPD.getSideDeckAppendDD();
    if ((sideDeckDD == null) || (sideDeckDD.length() == 0))
      sideDeckDD = "";

    // Formulate ":" seperated key string
    key = type + ":" + partInfoSysLibs + ":" + distLib + ":" + sysLibs + ":" 
              + extIncs + ":" + lkedParms + ":" + sideDeckDD;
    return key;
  }

  /**
   * Returns the JCLIN job header info for a given PackageData object
   *
   * @param curPD The current PackageData object
   * @param type The type of object being processed (pds or hfs)
   * @param jobstep The number of the current jobstep
   *
   * @return The JCLIN header for the current jobstep
   */
  String createJCLINHeader( PackageData curPD, String type, int jobstep, 
                            String dsnHlq )
    throws GeneratorException
  {
    String jclinVal = "";
    String tempStr;
    String sepStr;
    String lecmd;
    dsnHlq = dsnHlq.toUpperCase();
    
    // Add a jobstep separator
    if (jobstep > 1)
      jclinVal += "/*\n";

    // LINK statement
    tempStr = curPD.getJclinLkedParms();
    String inlineParms="";
    if (tempStr.length() > 100)
    {
      String parmStr = tempStr.substring(0, 84);
      int i = parmStr.lastIndexOf(",");
      String parmStr1 = parmStr.substring(0, i);
      String parmStr2 = tempStr.substring(i+1);
      tempStr = parmStr1 + ",OPTIONS=PARMDD";
      inlineParms = "//PARMDD DD *\n";
      StringTokenizer st = new StringTokenizer(parmStr2, ",");
      while (st.hasMoreTokens())
      {
         inlineParms += "  " + st.nextToken() + "\n";
      }
    }
    lecmd = "//LINK" + jobstep + "    EXEC PGM=IEWL,PARM='";
    if (type.equals("hfs"))
    {
      lecmd += "CASE(MIXED)";
      if (tempStr != null)
        lecmd += ","; 
    }
    if (tempStr != null)
      lecmd += tempStr; 
    lecmd += "'";
    
    // Ensure command does not exceed column 72
    while (lecmd.length() > 71)
    {
      // Continuation line must start at column 16
      String tstr;
      String padstr = "//             ";
      tstr = lecmd.substring(0, 71);
      tstr = tstr.substring(0, tstr.lastIndexOf(",")+1);
      jclinVal += tstr.toUpperCase() + "\n";
      lecmd = padstr + lecmd.substring( tstr.lastIndexOf(",")+1 );
    }
    
    jclinVal += lecmd.toUpperCase() + "\n";
    
    if (inlineParms.length() > 0)
      jclinVal += inlineParms.toUpperCase();

    // SYSLMOD statement
    tempStr = getPartInfoSysLib( curPD, type );
    if (tempStr.length() != 0)
    {
      sepStr = "SYSLMOD";
      
      // Comments
      if (tempStr.indexOf('/') == -1)
      {
        jclinVal += "//" + sepStr + "  DD   DISP=SHR,DSN=" + dsnHlq + "." 
                    + tempStr.toUpperCase() + "\n";  
      }
      else
      {
        jclinVal += "//" + sepStr + "  DD   PATH='" + tempStr + "'\n";
      }
    }

    // LIBRARYDD statement
    String libDDStr = curPD.getLibraryDD();
    if (tempStr.indexOf('/') != -1 && libDDStr == null)
    {
      throw new GeneratorException(
        "Must specify libraryDD attribute for file entity '" +
        curPD.getTargetFile() + "' because the SYSLMOD statement has a PATH " +
        "operand, SMP/E expects the next statement to be a LIBRARYDD comment " +
        "statement.");  
    }
    if (libDDStr != null)
      jclinVal += "//*LIBRARYDD=" + libDDStr.toUpperCase() + "\n";

    //SIDEDECKAPPEND statement
    String sideDeckDDStr = curPD.getSideDeckAppendDD();
    if (sideDeckDDStr != null)
    {
	   sideDeckDDStr = sideDeckDDStr.toUpperCase();
	   if (sideDeckDDStr.equals( "DUMMY" ) ||
	       sideDeckDDStr.equals( "NULLFILE" ) ||
	       sideDeckDDStr.equals( "SMPDUMMY" ))
        jclinVal += "//SYSDEFSD DD DUMMY\n";
		else
        jclinVal += "//SYSDEFSD DD DISP=SHR,DSN=SYS1." + sideDeckDDStr + "\n";
    }
      
    // SYSLIB statement
    ArrayList sysLibs = curPD.getSysLibs();
    ArrayList sysLibsLibDD = curPD.getSysLibsLibraryDD();
    if (sysLibs != null && !sysLibs.isEmpty())
    {
      sepStr = "SYSLIB";
      for (int i = 0, idx = 0; i < sysLibs.size(); i++)
      {
        tempStr = (String)sysLibs.get(i);       
        if (tempStr.indexOf('/') == -1)
        {
          jclinVal += "//" + sepStr + "   DD   DISP=SHR,DSN=" + dsnHlq + "." 
                      + tempStr.toUpperCase() + "\n";
        }
        else
        {
          jclinVal += "//" + sepStr + "   DD   PATH='" + tempStr + "'\n";
          if (sysLibsLibDD != null && 
              (libDDStr = (String)sysLibsLibDD.get(idx)) != null)
          {
            jclinVal += "//*LIBRARYDD=" + libDDStr.toUpperCase() + "\n";
            idx++;
          }
          else
          {
            throw new GeneratorException(
            "A LIBRARYDD comment statement is needed for every " +
            "SYSLIB statement that has a PATH operand. Please make sure the " +
            "array size of the sysLibsLibraryDD attribute value is same as " +
            "the number of sysLibs attribute values that have '/'.");
          }
        }
        sepStr = "      ";
      }
    }

    // DISTLIB statement
    // strip trailing "/" from targetDir
    String distlibStr = curPD.getTargetDir();
    if (distlibStr.charAt(distlibStr.length() - 1) == '/')
      distlibStr = distlibStr.substring(0, distlibStr.length() - 1);
    distlibStr = distlibStr.toUpperCase();
    jclinVal += "//" + distlibStr + 
      "  DD   DISP=SHR,DSN=" + dsnHlq + "." + distlibStr + "\n";

    // External Includes
    ArrayList extIncs = curPD.getInclude();
    Vector    usedExtIncs = new Vector();

    if (extIncs != null && !extIncs.isEmpty())
    {
      for (int i = 0; i < extIncs.size(); i++)
      {
        tempStr = (String)(extIncs.get(i));
        if (tempStr.indexOf("(") >= 0)
          tempStr = tempStr.substring(0, tempStr.indexOf("("));
         
        // Don't repeat an already generated DD statement
        if (usedExtIncs.contains(tempStr))
          continue;
        else
          usedExtIncs.addElement(tempStr);

          
        // Don't repeat the distlib include
        if (!tempStr.equalsIgnoreCase(distlibStr))
        {
          tempStr = tempStr.toUpperCase();
          jclinVal += "//" + tempStr + "  DD   DISP=SHR,DSN=" + dsnHlq + "." 
                      + tempStr + "\n";
        }
      }
    }
    jclinVal += "//SYSLIN   DD   *\n";
    return jclinVal;
  }

  /**
   * Returns the JCLIN job entry info for a given PackageData object
   *
   * @param curPD The current PackageData object
   * @param type The type of object being processed (pds or hfs)
   *
   * @return The JCLIN entry for the specified PackageData object
   */
  String createJCLINEntry( PackageData curPD, String type ) 
    throws GeneratorException
  {
    String    jclinVal = "";
    String    tempStr;
    ArrayList tempArray = curPD.getPartInfo();
    String    symLinks = "";
    String    symPaths = "";
    ReqType   tempReq;
    String    distlibInc = "";
    String    newStr;    
    String    sepStr;    
    String    padSpace;
    String    cntrlStr;

    // ORDER Statement
    String order = curPD.getOrder();
    if (order != null)
    {
      // order statements may be comma-separated
      StringTokenizer orderTok = new StringTokenizer(order, ",");
      while (orderTok.hasMoreTokens())
      {
        tempStr = orderTok.nextToken();
        jclinVal += " ORDER " + tempStr + "\n";
      }
    }

    // INCLUDE statement
    tempStr = curPD.getTargetDir();
    if ( tempStr != null )
    {
      // strip trailing "/"
      if (tempStr.charAt(tempStr.length() - 1) == '/')
        tempStr = tempStr.substring( 0, tempStr.length() - 1);
      distlibInc += tempStr + "(" + curPD.getTargetFile() + ")";
      jclinVal += " INCLUDE " + distlibInc + "\n";
    }
    
    // External INCLUDEs
    ArrayList extIncs = curPD.getInclude();
    if ((extIncs != null) && (!extIncs.isEmpty()))
    {
      for (int i = 0; i < extIncs.size(); i++)
      {
        sepStr = " INCLUDE ";
        tempStr = (String)extIncs.get(i);          
        cntrlStr = sepStr + tempStr;

        // Don't repeat the distlib include
        if (!tempStr.equalsIgnoreCase(distlibInc))
        {
          if (cntrlStr.length() > 71) 
          {
            // Ensure command does not exceed column 72
            while (cntrlStr.length() > 71)
            { 
              // If length exceeds 71, place an "X" in column 72
              // Continuation line must start at column 16
              String padstr = "               ";
              newStr = cntrlStr.substring( 0, 71); 
              int lastComma = newStr.lastIndexOf(',');  
              if (lastComma < 0)
              {
                throw new GeneratorException(
                  "INCLUDE entry incorrectly formatted - commas missing");
              }
              int padCount  = 72 - (lastComma + 1);
              padSpace = "";
 
              for (int tmpVar=1; tmpVar < padCount; tmpVar++) {
                 padSpace += " ";
              } 
 
              jclinVal += cntrlStr.substring( 0, lastComma + 1 ) + padSpace + "X\n";    
                             
              cntrlStr = padstr + cntrlStr.substring( lastComma + 1 );
            } // end while
 
            jclinVal += cntrlStr + "\n";
 
          } // end if
          else
            jclinVal += cntrlStr + "\n";

        }
      
      } // end for loop
    } // end if
    
    // ENTRY statement
    tempStr = curPD.getEntry();
    if ( tempStr != null )
      jclinVal += " ENTRY   " + tempStr + "\n";

    // SETCODE statement
    tempStr = curPD.getSetCode();
    if ( tempStr != null )
      jclinVal += " SETCODE " + tempStr + "\n";

    // JCLIN MODE statement
    tempStr = curPD.getJclinMode();
    if ( tempStr != null )
      jclinVal += " MODE    " + tempStr + "\n";

    jclinVal = jclinVal.toUpperCase();

    if (type.equalsIgnoreCase("hfs"))
    {
      // SETOPT statement
      String setOptStr = "";
      String permStr = curPD.getPermissions();
      
      // expecting permissions in format '0755' or '755'
      if (permStr != null && 
          ((permStr.length() == 3) || (permStr.length() == 4)))
      {
        // Prepend "0" if only three numbers
        if (permStr.length() == 3)
          permStr = "0" + permStr;
        
        // convert to "0,7,5,5"
        setOptStr += " SETOPT  PARM(PATHMODE(" + permStr.charAt(0) + "," +
                       permStr.charAt(1) + "," + permStr.charAt(2) + "," 
                       + permStr.charAt(3) + ")";
        tempStr = curPD.getUserId();
        if (tempStr != null)
        {
          if (tempStr.equals("0"))
            setOptStr += ",UID(0)";
          else
            setOptStr += ",UID('" + tempStr + "')";
        }
        tempStr = curPD.getGroupId();
        if (tempStr != null)
          setOptStr += ",GID('" + tempStr + "')";
        tempStr = curPD.getExtAttr();
        if (tempStr != null)
          setOptStr += ",EXTATTR(" + tempStr + ")";
       
        setOptStr += ")";
      }
      
      // Split up SETOPT string if over 71 chars
      while (setOptStr.length() > 71)
      {
        String partone = setOptStr.substring(0, 71);
        String rest = setOptStr.substring(71);
        setOptStr = " " + rest;
        jclinVal += partone + "X\n";
      }
      if (setOptStr.length() != 0)
        jclinVal += setOptStr + "\n";
    }

    // ALIAS statements
    ArrayList aliases;
    if (type.equals("pds"))
      aliases = curPD.getPdsAlias();
    else
      aliases = curPD.getHfsAlias();
    if ((aliases != null) && (!aliases.isEmpty()))
    {
      String aliasStr = "";
      for (int i = 0; i < aliases.size(); i++)
      {
        tempStr = (String)(aliases.get(i));
        aliasStr += " ALIAS   ";
        if (type.equals("pds"))
        {
          aliasStr += tempStr + "(" + curPD.getEntry() + ")";
          aliasStr = aliasStr.toUpperCase();
        }
        else
          aliasStr += "'" + tempStr + "'";
        aliasStr += "\n";
      }
      jclinVal += aliasStr;
    }

    if (type.equalsIgnoreCase("hfs"))
    {
      // ALIAS SYMLINK/SYMPATH statements
      if (tempArray != null && !tempArray.isEmpty())
      {
        for (int i = 0; i < tempArray.size(); i++)
        {
          tempReq = (ReqType)(tempArray.get(i) );
          tempStr = tempReq.getType();
          if (tempStr != null && tempStr.equalsIgnoreCase("symlink"))
          {
            symLinks = tempReq.getValue();
          }
          else if (tempStr != null && tempStr.equalsIgnoreCase("sympath"))
          {
            symPaths = tempReq.getValue();
          }
        }
      }  
      
      // symlinks may be comma-separated
      StringTokenizer symLinksTok = new StringTokenizer(symLinks, ",");
      StringTokenizer symPathTok = new StringTokenizer(symPaths, ",");
      String symPathVal = "";
      String symLinkVal = "";
      while (symLinksTok.hasMoreTokens())
      {
        // Write each symlink entry
        symLinkVal = symLinksTok.nextToken();
        jclinVal += " ALIAS (SYMLINK," + symLinkVal + ")\n";
        
        // Get sympaths
        if (symPathTok.hasMoreElements())
        {
          symPathVal = symPathTok.nextToken();
        }
        jclinVal += " ALIAS (SYMPATH," + symPathVal + ")\n";
      }
    }
    
    // NAME and RC Statements
    String lkedName;
    String lkedRc = curPD.getLkedRc();
    
    // Process PDS or HFS names
    if (type.equalsIgnoreCase("pds"))
      lkedName = curPD.getPdsLkedName();
    else
      lkedName = curPD.getHfsLkedName();

    // Process RC - default to 0
    if (lkedRc == null) 
      lkedRc = "0";

    jclinVal += " NAME    " + lkedName + "(R)     RC=" + lkedRc +"\n";

    return jclinVal;
  }

  /**
   * Returns the partInfo SYSLIB element from a partInfo entry
   *
   * @param curPD The current PackageData object
   * @param type The type of object being processed (pds or hfs)
   *
   * @return The partInfo SYSLIB entry for the specified Packagedata object
   */
  String getPartInfoSysLib( PackageData curPD, String type )
  {
    String    partInfoSysLibs = "";
    ReqType   tempReq;
    String    tempStr;
    ArrayList tempArray = curPD.getPartInfo();

    if (tempArray != null && !tempArray.isEmpty())
    {
      for (int i = 0; i < tempArray.size(); i++)
      {
        tempReq = (ReqType)tempArray.get(i);
        tempStr = tempReq.getType();
        if (tempStr != null)
        {
          if (type.equalsIgnoreCase("hfs") && 
              tempStr.equalsIgnoreCase("hfssyslib"))
          {
            partInfoSysLibs = tempReq.getValue();
          }
          else if (type.equalsIgnoreCase("pds") && 
                   tempStr.equalsIgnoreCase("pdssyslib"))
          {
            partInfoSysLibs = tempReq.getValue();
          }
          else if (tempStr.equalsIgnoreCase("syslib"))
          {
            partInfoSysLibs = tempReq.getValue();
          }
        }
      }
    }
    return partInfoSysLibs;
  }
  
  /**
   * Returns true if the specified part needs an entry in the JCLIN file. The part 
   * name is of form 'targetDir.targetFile'. 
   *
   * @param jclinParts list of parts specified in the B390_JCLIN_FILES makefile
   *                   variable
   * @param part a valid part name
   */
  private boolean isJCLINPart( Vector jclinParts, String part )
  {  
     Enumeration enumer = jclinParts.elements();
     while (enumer.hasMoreElements())
     {
        if (((String)enumer.nextElement()).equalsIgnoreCase(part))
        {
           return true;
        }
     }
     return false;
  }
  
  /**
   * Returns part name for the specified Package Data object. The format for
   * the part name is 'targetDir.targetFile'.
   *
   * @param odObj a valid PackageData object
   * @return part name
   */
  private String getPartName( PackageData pdObj )
  {
     String targetDir = pdObj.getTargetDir();
     
     if (targetDir.endsWith("/") || targetDir.endsWith("\\"))
     {
        targetDir = targetDir.substring(0, (targetDir.length() - 1));
     }
     return targetDir + "." + pdObj.getTargetFile();
  }
}
