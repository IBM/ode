/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.3
 *
 * Date and Time File was last checked in: 8/28/03 17:29:23
 * Date and Time File was extracted/checked out: 06/04/13 16:46:43
 *****************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.io.Path;
import java.net.*;

/**
 * Class to generate JCL for MVS ipp gather and packaging.
 * @version 1.3
 * @author  Mark DeBiase
 */
public class MvsJclGenerator
{
  /**
   * Construct a MvsJclGenerator for the given MvsPackageData object.
   */
  public MvsJclGenerator( MvsPackageData packageData, int pkgNumber )
  {
    pkgData_ = packageData;
    jobName_ = MvsProperties.userid + jobChars_[pkgNumber - 1];
    jobInfo_ = null;
  }

  /**
   * Generate IPP gather job stream.
   */
  public MvsJobInfo generateIppGather() throws MvsPkgError
  {
    // generate JCL filename as "pkgControlDir/<function>.gather.jcl"
    String function = (String) pkgData_.getProductData().get("FUNCTION");
    if (function == null)
    {
       throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                             new Object[] {"FUNCTION"});
    }

    String jclFileName = MvsProperties.pkgControlDir + function + ".gather.jcl";

    System.out.println("Generating file gather jobstream for sysmod " +
                       function + " to file");
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter( new FileWriter(jclFileName) );
      bw.write(readJobcard());

      long start, delta;

      start = System.currentTimeMillis();
      Hashtable distlibInfo = genAlloclibStep(bw);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated allocation step in " + delta + "ms");

      start = System.currentTimeMillis();
      genIebcopyStep(bw, distlibInfo);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated iebcopy steps in " + delta + "ms");

      start = System.currentTimeMillis();
      genGatherStep(bw, distlibInfo);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated gather step in " + delta + "ms");

      start = System.currentTimeMillis();
      genAliasStep(bw, distlibInfo);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated alias step in " + delta + "ms");

      bw.write( "//" ); bw.newLine();
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName,
                              ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                            ex.getMessage()});
      }
    }
    return jobInfo_;
  }  //generateIppGather()

  /**
   * Generate IPP link-edit job stream.
   */
  public MvsJobInfo generateIppLinkEdit() throws MvsPkgError
  {
    if (genLinkEdit_)
    {
      // generate JCL filename as "pkgControlDir/<function>.linkEdit.jcl"
      String function = (String) pkgData_.getProductData().get("FUNCTION");
      if (function == null)
      {
         throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                               new Object[] {"FUNCTION"});
      }

      String jclFileName = MvsProperties.pkgControlDir + function + ".linkEdit.jcl";

      System.out.println("Generating file linkEdit jobstream for sysmod " +
                          function + " to file");
      System.out.println(jclFileName);
      System.out.println("  Job name is " + jobName_);

      jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
      BufferedWriter bw = null;
      try
      {
        bw = new BufferedWriter( new FileWriter(jclFileName) );
        bw.write(readJobcard());

        long start, delta;
        start = System.currentTimeMillis();
        genLinkEditStep(bw);
        delta = System.currentTimeMillis() - start;
        System.out.println("  Generated link-edit step in " + delta + "ms");

        bw.write( "//" ); bw.newLine();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                  new Object[] {"writing", jclFileName, ex.getMessage()});
      }
      finally
      {
        try
        {
          bw.close();
        }
        catch (IOException ex)
        {
          throw new MvsPkgError(MvsPkgError.ioException3,
                                new Object[] {"closing", jclFileName,
                                              ex.getMessage()});
        }
      }
      return jobInfo_;
    }
    return null;
  }

  /**
   * Generate IPP packaging job stream.
   */
  public MvsJobInfo generateIppPackage() throws MvsPkgError
  {
    // generate JCL filename as "pkgControlDir/<function>.package.jcl"
    String function = (String) pkgData_.getProductData().get("FUNCTION");
    if (function == null)
    {
       throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                             new Object[] {"FUNCTION"});
    }

    String jclFileName = MvsProperties.pkgControlDir + function +
                         ".package.jcl";

    System.out.println("Generating packaging jobstream for sysmod " +
                       function + " to file");
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(jclFileName));
      bw.write(readJobcard());

      long start, delta;

      start = System.currentTimeMillis();
      genSubuildSteps(bw);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated RTG steps in " + delta + "ms");

      if (pkgData_.isAnyFileOfTypeVpl())
      {
        start = System.currentTimeMillis();
        genVplStep(bw, null);
        delta = System.currentTimeMillis() - start;
        System.out.println("  Generated VPL step in " + delta + "ms");
      }

      bw.write( "//" ); bw.newLine();
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName, ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                            ex.getMessage()});
      }
    }
    return jobInfo_;
  }  //generateIppPackage()

  /**
   * Generate PTF VPL job stream.
   *
   * @param ptfNumber the ptf number to be used in the vpl header
   * @exception MvsPkgError an error occurs when writing or closing
   *                        the jcl file
   */
  public MvsJobInfo generatePtfVplStep( String ptfNumber ) throws MvsPkgError
  {
    String function = (String)pkgData_.getProductData().get("FUNCTION");
    if (function == null)
    {
       throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                             new Object[] {"FUNCTION"});
    }

    String jclFileName = MvsProperties.pkgControlDir + function + ".ptfvpl.jcl";

    System.out.println("Generating jobstream for sysmod " +
                        function + " to file");
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(jclFileName));
      bw.write(readJobcard());

      long start, delta;
      start = System.currentTimeMillis();
      this.genVplStep(bw, ptfNumber);
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated VPL step in " + delta + "ms");

      bw.write( "//" ); bw.newLine();
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                 new Object[] {"writing", jclFileName, ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch(IOException e)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"closing", jclFileName, e.getMessage()});
      }
    }
    return jobInfo_;
  } // generatePtfVplStep()

  /**
   * Generates JCL for creating a PDS with specified name and returns MvsJobInfo that
   * encapsulates this JCL
   *
   * @param function a valid MVS release / function
   * @param dsnName a valid DSN name
   */
  public MvsJobInfo generateAllocLibForLogRetrieve( String function, String dsnName )
    throws MvsPkgError
  {
    String jclFileName = MvsProperties.pkgControlDir + function + ".createPDS.jcl";
    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(jclFileName));
      bw.write(readJobcard());
      bw.write(commentLine_); bw.newLine();
      bw.write("//* Allocate a PDS for the output of LOGRETRIEVE command");
      bw.newLine();
      bw.write(commentLine_); bw.newLine();
      bw.write(genRexxTsoJcl("ALLOCLIB", null, null));
      bw.write(dsnName);
      bw.write(" TRACKS 5 5 5 FB 80 6160"); bw.newLine();
      bw.write("/*"); bw.newLine();
      bw.write("//");
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName,
                              ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                            ex.getMessage()});
      }
    }
    return jobInfo_;
  }

  /**
   * Generate JCL for a step to run the specified Rexx exec via IKJEFT01.
   *
   * @param rexxExecName the Rexx exec to be run
   * @param stepName the step name or null, in which case the step name will
   *                 be the rexx exec name
   * @param condRc if not null, a JCL IF statement will be generated with the
   *               specified condition
   * @param additionalJcl if not null, the specified JCL statement will be
   *                    added to the generated JCL after the SYSEXEC DD statement
   *
   * It is the caller's responsibility to add a '/*' for the end of instream
   * data (an //INDD DD * statement is generated to specify input to the
   * Rexx exec) and an ENDIF statment (if condRc was specified).
   */
  private String genRexxTsoJcl(String rexxExecName,
                               String stepName,
                               String condRc,
                               String additionalJcl)
  {
    String ifStmt1    = "//         IF (";
    String ifStmt2    = ") THEN";
    String execStmt   = "EXEC PGM=IKJEFT01,DYNAMNBR=25,PARM='";
    String sysexecDD1  = "//SYSEXEC  DD DISP=SHR,DSN=";
    String sysexecDD2  = "//         DD DISP=SHR,DSN=SYS1.SBPXEXEC";
    String systsprtDD = "//SYSTSPRT DD SYSOUT=*";
    String sysprintDD = "//SYSPRINT DD SYSOUT=*";
    String systsinDD  = "//SYSTSIN  DD DUMMY";
    String inputDD    = "//INDD     DD *";

    // pad stepname to 9 characters
    StringBuffer stepNameSB = new StringBuffer(9);
    if (stepName == null)
    {
      stepNameSB.append(rexxExecName);
    }
    else
    {
      stepNameSB.append(stepName);
    }
    for (int i=rexxExecName.length()+1; i <= 9; i++)
    {
      stepNameSB.append(' ');
    }

    StringBuffer sb = new StringBuffer(350);

    // if condRc was specified, generate the IF statement
    if (condRc != null)
    {
      sb.append(ifStmt1).append(condRc).append(ifStmt2).append(NL_);
    }
    sb.append("//").append(stepNameSB).append(execStmt);
    sb.append(rexxExecName).append("'").append(NL_);
    sb.append(sysexecDD1).append(MvsProperties.mvsExecDsn).append(NL_);
    if (rexxExecName.equals("GATHER"))
      sb.append(sysexecDD2).append(NL_);

    // if additional jcl was specified, add it after the sysexec dd stmt
    if (additionalJcl != null)
    {
      sb.append(additionalJcl).append(NL_);
    }
    sb.append(systsprtDD).append(NL_);
    sb.append(sysprintDD).append(NL_);
    sb.append(systsinDD).append(NL_);
    sb.append(inputDD).append(NL_);

    return sb.toString();

  }  // genRexxTsoJcl()

  /**
   * Overloaded version of genRexxTsoJcl in which stepName is not specified.
   * (The need to specify a unique stepname came when service packaging was
   * added - this method was added to avoid changes to calls made from existing
   * ipp jcl generation methods)
   */
  private String genRexxTsoJcl(String rexxExecName,
                               String condRc,
                               String additionalJcl)
  {
    return genRexxTsoJcl(rexxExecName, null, condRc, additionalJcl);
  }

  /**
   * Generate JCL to execute the ALLOCLIB Rexx exec to allocate the distribution
   * libraries.
   *
   * @param bw BufferedWriter object
   * @throws MvsPkgError
   * @throws IOException
   * @return a hashtable whose keys are the distlib and whose value is the
   *         full data set name of the distlib (applid.function.distlib).
   */
  private Hashtable genAlloclibStep( BufferedWriter bw )
                    throws MvsPkgError, IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Allocate distribution libraries"); bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write( genRexxTsoJcl("ALLOCLIB", null, null) );

    Hashtable distlibInfo = new Hashtable();

    // get required tags from product data
    Hashtable pd = pkgData_.getProductData();
    String applid    = (String) pd.get("APPLID");            // required
    String function  = (String) pd.get("FUNCTION");          // required
    String distlibs  = (String) pd.get("DISTLIBS");          // required
    String copyright = (String) pd.get("COPYRIGHT");         // required
    String jclinLib  = (String) pd.get("JCLINLIB");          // optional
    String createJclinLib  = (String) pd.get("CREATEJCLINLIB");
    boolean allocJclinLib = true;
    String primTracks;
    String secTracks;
    String dirBlocks;
    String recFormat;
    int tmpPrimTracks = 0;
    int tmpSecTracks = 0;
    int tmpDirBlocks = 0;
    boolean isPDSE = false;
    boolean allocatePrelinkDataset = false;

    if (applid == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"APPLID"});
    }
    else if (function == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"FUNCTION"});
    }
    else if (distlibs == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"DISTLIBS"});
    }
    else if (copyright == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"COPYRIGHT"});
    }

    // construct first two qualifiers of data set names
    String applidFunction = applid + "." + function + ".";

    // StringTokenizers:
    // st1 used to parse blank delimited set of distlib entries
    // st2 used to parse comma delimited individual distlib entry
    try   // catch any tokenization errors
    {
      StringTokenizer st1 = new StringTokenizer(distlibs, " ");
      while (st1.hasMoreTokens())
      {
        StringBuffer distlibName = new StringBuffer(30);  // full data set name
        String distlibAsStr;   // distlib as String
        String distlibLLQ;     // distlib low level qualifier
        String distlibEntry;

        distlibName.append(applidFunction);
        distlibEntry = st1.nextToken();
        StringTokenizer st2 = new StringTokenizer( distlibEntry, ",");
        distlibLLQ = st2.nextToken();
        distlibName.append(distlibLLQ);
        distlibAsStr = distlibName.toString();

        // If jclinLib was found as a distlib or createJclinLib equal no,
        // don't need to allocate it
        if (distlibLLQ.equalsIgnoreCase( jclinLib ) ||
            createJclinLib.equalsIgnoreCase( "no" ))
        {
           allocJclinLib = false;
        }

        // Save full distlib name in hastable, indexed by distlib llq
        String s = (String) distlibInfo.put(distlibLLQ, distlibAsStr);
        if (s != null)
        {
          throw new MvsPkgError(MvsPkgError.duplicateDistlib1,
                                new Object[] {distlibLLQ});
        }

        // There should be 6-8 more tokens for allocation info for the distlib
        // (last 2 tokens, VOLSER and UNIT are optional)
        if (st2.countTokens() < 6 || st2.countTokens() > 8)
        {
          throw new MvsPkgError(MvsPkgError.incorrectAllocationInfo1,
                                new Object[] {distlibLLQ});
        }
        else
        {
          // Write out the full distlib dataset name & its allocation info
          StringBuffer line = new StringBuffer(80);
          line.append("  ").append(distlibAsStr);
          line.append(" TRACKS");

          // Next token is primary tracks
          primTracks = st2.nextToken();
          line.append(" "); line.append( primTracks );

          // Next token is secondary tracks
          secTracks = st2.nextToken();
          line.append(" "); line.append( secTracks );

          // Next token is directory blocks
          dirBlocks = st2.nextToken();
          line.append(" "); line.append( dirBlocks );

          // Next token is record format
          recFormat = st2.nextToken();
          line.append(" "); line.append( recFormat );

          // If RECFM=U, must allocate temporary FB80 PDS or PDS/E for prelink-edit
          if (recFormat.equalsIgnoreCase("u"))
          {
            try
            {
              // Indicate that a temporary dataset for pre-link edit process
              // need to be created.
              allocatePrelinkDataset = true;

              tmpPrimTracks += new Integer(primTracks).intValue();
              tmpSecTracks += new Integer(secTracks).intValue();

              // If the value is 'u', it means the dataset is of type PDS/E.
              // If the dataset is of type PDS/E then the isPDSE flag is set to
              // true to indicate that the temporary dataset the will be used in
              // the pre-link edit process also need to be of type PDS/E.
              if (!dirBlocks.equalsIgnoreCase("u"))
              {
                tmpDirBlocks += new Integer(dirBlocks).intValue();
              }
              else
              {
                isPDSE = true;
              }
            }
            catch (NumberFormatException e)
            {
              throw new MvsPkgError(MvsPkgError.invalidDistlibFormat,
                                    new Object[] {distlibEntry});
            }
          }

          // Write out remaining tokens
          while (st2.hasMoreTokens())
          {
            line.append(" "); line.append(st2.nextToken());
          }
          bw.write( formatLine80(line) );
          bw.newLine();
          line = null;
        }
      } //while
    }
    catch (NoSuchElementException e)
    {
      throw new MvsPkgError(MvsPkgError.errorParsingDistlib0, new Object[0]);
    }

    // Allocate necessary JCLIN distlib
    jclinDistLib_ = applidFunction + jclinLib;
    if (allocJclinLib)
    {
      if (jclinLib == null)
      {
        // Get first distlib name
        int tmpidx = distlibs.indexOf( "," );
        String ident;

        // Get first four chars of distlib
        if (tmpidx >= 0)
          ident = distlibs.substring( 0, tmpidx );
        else
          ident = "";
        if (ident.length() >= 4)
          ident = ident.substring( 0, 4 );

        // Allocate as USERID.FMID.xxxxJCL
        jclinDistLib_ = applidFunction + ident + "JCL";
      }
      bw.write("  "); bw.write(jclinDistLib_);
                    bw.write(" TRACKS 5 5 5 FB 80 6160"); bw.newLine();
    }

    // Allocate PRELINK distlib for link-edit process
    if (allocatePrelinkDataset)
    {
      prelinkDistLib_ = applidFunction + "PRELINK";
      bw.write("  "); bw.write(prelinkDistLib_);
      bw.write(" TRACKS "); bw.write(tmpPrimTracks + " ");
      bw.write(tmpSecTracks + " ");
      if (isPDSE)
      {
        bw.write("U ");
      }
      else
      {
        bw.write(tmpDirBlocks + " ");
      }
      bw.write( "FB 80 8800" ); bw.newLine();
    }

    // Generate allocations for CPYRIGHT, TMP:
    // all data sets are PS, FB 80, BLKSIZE 0 (system determined)
    bw.write("  "); bw.write(applidFunction); bw.write("CPYRIGHT ");
                    bw.write("TRACKS 1 1 0 FB 80 0"); bw.newLine();
    bw.write("  "); bw.write(applidFunction); bw.write("TMP ");
                    bw.write("TRACKS 20 1 25 FB 80 0"); bw.newLine();

    bw.write("/*"); bw.newLine();

    jobInfo_.addStep("ALLOCLIB", 0, "Allocate distribution libraries");

    return distlibInfo;
  }  //genAlloclibStep()

  /**
   * Generate JCL for IEBCOPY steps to gather source that resides in
   * partitioned data sets.  There will be one IEBCOPY step generated
   * for each distribution library that has source files in a PDS.
   * The distlibInfo argument contains the distlib as the key and
   * the full distlib name (applid.function.distlib) as the value.
   */
  private void genIebcopyStep( BufferedWriter bw, Hashtable distlibInfo )
    throws MvsPkgError, IOException
  {
    boolean wroteComments = false;

    // Create a hash table with distlib as key and a list of MvsPdsData
    // objects as the value.  This will allow
    // access to all PDS source files for a given distlib.
    Hashtable pdsHT = new Hashtable();

    ArrayList hs = pkgData_.getShipDataSet();
    ListIterator hsi = hs.listIterator();

    // find all IPP shippable sources in a PO data set
    while (hsi.hasNext())
    {
      MvsShipData  shipData = (MvsShipData) hsi.next();
      if (shipData.isTypeIpp() && shipData.getFileType() == MvsShipData.PO)
      {
        Hashtable ht = shipData.getShipData();

        String distlib = (String) ht.get("DISTLIB");
        if (distlib == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTLIB", shipData.getSourceFile()});
        }

        String distname = (String) ht.get("DISTNAME");
        if (distname == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTNAME", shipData.getSourceFile()});
        }

        // create a MvsPdsData object for this source file and save it in
        // an ArrayList in pdsHT by distlib.
        MvsPdsData pdsData = new MvsPdsData(shipData.getSourceFile(), distname);
        ArrayList pdsdatalist;
        if (pdsHT.containsKey(distlib))
        {
          pdsdatalist = (ArrayList) pdsHT.get( distlib );
        }
        else
        {
          pdsdatalist = new ArrayList();
        }
        pdsdatalist.add( pdsData );
        pdsHT.put( distlib, pdsdatalist );
      }  //if typeIpp && fileType=PO
    }  //while

    // now that we have a listing of all input files for each distlib,
    // build the iebcopy steps

    String ifStmt     = "//         IF (RC=0) THEN";
    String execStmt   = " EXEC PGM=IEBCOPY";
    String sysprintDD = "//SYSPRINT DD SYSOUT=*";
    String outDD      = "//OUTDD    DD DISP=SHR,DSN=";
    String inDD1      = "//INDD";
    String inDD2      = " DD DISP=SHR,DSN=";
    String sysinDD    = "//SYSIN    DD *";
    String endifStmt  = "//         ENDIF";
    String ctlStmt1   = "  COPY INDD=INDD";
    String ctlStmt2   = ",OUTDD=OUTDD";
    String ctlStmt3   = "    SELECT MEMBER=";

    DecimalFormat df = new DecimalFormat("0000");  // for formatting stepnames

    Enumeration e = pdsHT.keys();
    while (e.hasMoreElements())
    {
      String          distlib = (String) e.nextElement();
      ArrayList       pdsdatalist = (ArrayList) pdsHT.get(distlib);
      ListIterator    lsi = pdsdatalist.listIterator();

      if (!wroteComments)
      {
        bw.write(commentLine_); bw.newLine();
        bw.write("//* PDS gather steps - one for each distlib"); bw.newLine();
        wroteComments = true;
      }

      // generate step name as distlib padded to 8 characters
      StringBuffer stepName = new StringBuffer(8);
      stepName.append(distlib);
      for (int i=distlib.length(); i<8; i++)
      {
        stepName.append(" ");
      }

      // get full distlib data set name from distlibInfo
      String distlibDSN = (String) distlibInfo.get(distlib);
      if (distlibDSN == null)
      {
        throw new MvsPkgError(MvsPkgError.distlibNotFound1,
                              new Object[] {distlib});
      }

      jobInfo_.addStep(stepName.toString(), 0,
                        "PDS gather for distlib " + distlib);

      // start generating jcl for this step
      bw.write(commentLine_); bw.newLine();
      bw.write(ifStmt); bw.newLine();
      bw.write("//"); bw.write(stepName.toString()); bw.write(execStmt);
      bw.newLine();
      bw.write(sysprintDD); bw.newLine();
      bw.write(outDD); bw.write(distlibDSN); bw.newLine();

      StringBuffer ddNames      = new StringBuffer();
      StringBuffer controlStmts = new StringBuffer();

      // generate DD & control statements for each input source file
      String lastSrcLib = "";
      int inddNum = 1;
      while (lsi.hasNext())
      {
        MvsPdsData pdsData = (MvsPdsData) lsi.next();

        if ( !lastSrcLib.equals(pdsData.getSrcLib()) )
        {
          // INDDxxxx for this source data set
          ddNames.append(inDD1).append(df.format(inddNum));
          ddNames.append(inDD2).append(pdsData.getSrcLib());
          ddNames.append(NL_);

          // COPY control statment for this source data set
          controlStmts.append(ctlStmt1).append(df.format(inddNum));
          controlStmts.append(ctlStmt2).append(NL_);

          lastSrcLib = pdsData.getSrcLib();
          inddNum++;
        }

        controlStmts.append(ctlStmt3);
        if (pdsData.getDistName().equals(pdsData.getSrcName()))
        {
          // SELECT MEMBER=member
          controlStmts.append(pdsData.getSrcName()).append(NL_);
        }
        else
        {
          // SELECT MEMBER=((oldname,newname))
          controlStmts.append("((").append(pdsData.getSrcName()).append(",");
          controlStmts.append(pdsData.getDistName()).append("))").append(NL_);
        }
      }

      bw.write(ddNames.toString());
      bw.write(sysinDD); bw.newLine();
      bw.write(controlStmts.toString());
      bw.write("/*"); bw.newLine();
      bw.write(endifStmt); bw.newLine();
    }  // while e.hasMoreElements()
  }  // genIebcopyStep()

  /**
   * Generate JCL to execute the GATHER Rexx exec to gather sequential
   * data sets and HFS files.
   *
   * @param bw BufferedWriter object
   * @param distlibInfo contains the distlib as the key and the full distlib
   *                    name (applid.function.distlib) as the value.
   * @exception MvsPkgError
   * @exception IOException
   */
  private void genGatherStep( BufferedWriter bw, Hashtable distlibInfo )
    throws MvsPkgError, IOException
  {
    // this step will always be generated since COPYRIGHT is required....
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Gather step for PO data sets and HFS files");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write( genRexxTsoJcl("GATHER", "RC=0", null) );

    // Generate gather for COPYRIGHT tag
    // If these tags look like HFS files, append TOSTAGE to the front:
    String copyright = (String) pkgData_.getProductData().get("COPYRIGHT");
    String applid    = (String) pkgData_.getProductData().get("APPLID");
    String function  = (String) pkgData_.getProductData().get("FUNCTION");
    boolean generateJCLIN = false;

    String hlq = applid + "." + function + ".";

    if (copyright == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"COPYRIGHT"});
    }
    else
    {
      StringBuffer copyrightSB = new StringBuffer(80);
      copyrightSB.append("  ");
      if (copyright.indexOf('/') != -1)     // HFS file
      {
        if (copyright.indexOf('/') != 0)     // relative path
        {
          copyrightSB.append(MvsProperties.toStage);
        }
      }
      else                                  // MVS data set
      {
        copyright = copyright.toUpperCase();
      }
      copyrightSB.append(copyright).append("  ").append(hlq).append("CPYRIGHT");
      bw.write( formatLine80(copyrightSB) ); bw.newLine();
      copyrightSB = null;
    }

    // find all HFS files and PS data sets with type = IPP
    ArrayList             hs  = pkgData_.getShipDataSet();
    ListIterator     hsi = hs.listIterator();

    while (hsi.hasNext())
    {
      MvsShipData shipData = (MvsShipData) hsi.next();
      if (shipData.isTypeIpp() && shipData.getFileType() != MvsShipData.PO)
      {
        Hashtable    ht = shipData.getShipData();

        String distlib  = (String) ht.get("DISTLIB");
        String parttype  = (String) ht.get("PARTTYPE");
        String hfsCopyType = (String) ht.get("HFSCOPYTYPE");
        String lkedCond = (String) ht.get("LKEDCOND");
        if (distlib == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTLIB", shipData.getSourceFile()});

        }

        String distname = (String) ht.get("DISTNAME");
        if (distname == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTNAME", shipData.getSourceFile()});
        }

        String distlibDSN = (String) distlibInfo.get(distlib);

        if (distlibDSN == null)
        {
          if ( (hfsCopyType != null) &&
              !(hfsCopyType.equalsIgnoreCase( "jclin_only" )) &&
              !(hfsCopyType.equalsIgnoreCase( "none" )) )
          {
            throw new MvsPkgError(MvsPkgError.distlibNotFound1,
                                  new Object[] {distlib});
          }
        }

        // Cannot have a link-edit part without a RECFM U dataset
        String lkedParms = (String) ht.get("LKEDPARMS");
        if ((lkedParms != null) && (prelinkDistLib_ == null))
        {
          throw new MvsPkgError(MvsPkgError.linkEditPartNotAllowed,
                                new Object[] {distname});
        }

        StringBuffer line = new StringBuffer(80);
        if (shipData.getFileType() == MvsShipData.HFS)
           line.append("  ").append(MvsProperties.toStage)
                            .append( stripLeadingFS(shipData.getSourceFile()) );
        else
           line.append("  ").append(shipData.getSourceFile());
        line.append("  ");
        // gather part to prelink distlib if intended for link-edit
        // link-edit this part if it is "mod" or "program" and has
        // lkedParms specified
        if ((parttype.equalsIgnoreCase("MOD") ||
              parttype.equalsIgnoreCase("PROGRAM")) &&
            (lkedParms != null) )
        {
          line.append(prelinkDistLib_);
          genLinkEdit_ = true;
          // Store link-edit key for later use in link-edit job-step creation
          // jobstep key is combination of targetDir, lkedParms and lkedCond
          if (lkedCond == null)
            lkedCond = "4";
          String lkedKey = distlib + ":" + lkedParms + ":" + lkedCond;
          // get the alias value for this part
          String aliasVal = (String) ht.get("ALIAS");
          if (lkedKeys.contains( lkedKey ))
          {
            int keynum = lkedKeys.indexOf( lkedKey );
            String distval = (String)lkedDistNames.elementAt( keynum );
            // if alias is specified, append it to that particular distname
            // before appending it to the list of distnames
            // eg: distval = logger:logx;client:clientx,clienty;server
            // where logger, client and server are distnames and logx and
            // clientx,clienty are aliases of logger and client respectively
            if (aliasVal != null)
            {
              distval += ";" + distname + ":"  + aliasVal;
            }
            else
            {
              distval += ";" + distname ;
            }
            lkedDistNames.setElementAt( distval, keynum );
          }
          else
          {
            lkedKeys.addElement( lkedKey );
            if (aliasVal != null)
            {
              lkedDistNames.addElement( distname + ":" + aliasVal  );
            }
            else
            {
              lkedDistNames.addElement( distname );
            }
          }
        }
        else
          line.append(distlibDSN);
        line.append("(").append(distname).append(")");

        // For HFS parts, determine BINARY/TEXT tags for type of copy:
        if (shipData.getFileType() == MvsShipData.HFS)
        {
          if ((parttype != null) && !(parttype.equals("PROGRAM")))
          {
            String binary      = (String) ht.get("BINARY");
            String text        = (String) ht.get("TEXT");

            if (hfsCopyType != null)
            {
              line.append(" ").append(hfsCopyType);
            }
            else if (binary != null && text != null)
            {
              throw new MvsPkgError(MvsPkgError.mutuallyExclusive3,
                      new Object[] {shipData.getSourceFile(), "BINARY","TEXT"});
            }
            else if (binary != null)
            {
              line.append(" BINARY");
            }
            else if (text != null)
            {
              line.append(" TEXT");
            }
            else if (binary == null && text == null)
            {
              // if binary or text is not specified && parttype is MOD
              // force BINARY mode
              if (parttype.equals("MOD"))
                line.append(" BINARY");
            }
          }
          else if (parttype.equals("PROGRAM"))
          {
            line.append(" PROGRAM");
          }
        }

        // Check if this FMID should generate a JCLIN gather statement
        String lkedTo = (String) ht.get("LKEDTO");
        if (!generateJCLIN && (parttype.equals("MOD") && (lkedTo != null)))
          generateJCLIN = true;

        // Do not write the entry if HFSCOPYTYPE is tso or jclin_only
        if ( (hfsCopyType == null) || (!(hfsCopyType.equalsIgnoreCase( "tso" )) 
              && !(hfsCopyType.equalsIgnoreCase( "none" ))
              && !(hfsCopyType.equalsIgnoreCase( "jclin_only" ))) )
        {
          bw.write( formatLine80(line) );
          bw.newLine();
        }
       }  //if typeIpp && fileType=PO
    }  //while

    // Generate JCLIN gather step
    if (generateJCLIN)
    {
      String jclinFile = function.toUpperCase() + ".jclin";
      StringBuffer jclinSB = new StringBuffer(80);
      jclinSB.append("  ");
      jclinSB.append(MvsProperties.pkgControlDir);
      jclinFile = stripLeadingFS( jclinFile );
      jclinSB.append(jclinFile).append("  ").append( jclinDistLib_ );
      jclinSB.append("(" + function +")").append(" TEXT");
      bw.write( formatLine80(jclinSB) ); bw.newLine();
      jclinSB = null;
    }

    bw.write("/*"); bw.newLine();
    bw.write("//         ENDIF"); bw.newLine();

    jobInfo_.addStep("GATHER", 0, "Gather HFS files and PS data sets");

    return;
  }  //genGatherStep()

  /**
   * Generate JCL to perform the link-edit of the pre-linked object modules
   */
  private void genLinkEditStep( BufferedWriter bw )
    throws MvsPkgError, IOException
  {
    int stepnum = 0;
    String applid    = (String) pkgData_.getProductData().get("APPLID");
    String function  = (String) pkgData_.getProductData().get("FUNCTION");
    String unit  = (String) pkgData_.getProductData().get("LKEDUNIT");
    String hlq = applid + "." + function + ".";
    String unitstr;

    bw.write(commentLine_); bw.newLine();
    bw.write("//* Link-edit steps for pre-linked object modules");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();

    // For each link-edit key, we need a job step
    for (int i = 0; i < lkedKeys.size(); i++)
    {
      String curKey = (String)lkedKeys.elementAt( i );
      String distlib = curKey.substring( 0, curKey.indexOf( ":" ));
      String leparms = curKey.substring( curKey.indexOf( ":" ) + 1, curKey.lastIndexOf(":") );
      String cond = curKey.substring( curKey.lastIndexOf(":") + 1);
      
      stepnum++;
      String lecmd = "//LESTEP" + stepnum + "    EXEC PGM=IEWL,REGION=0M,";
      lecmd += "COND=(" + cond + ",LT),PARM=" + "'" + leparms + "'";

      while (lecmd.length() > 71)
      {
        // Continuation line must start at column 16
        String padstr = "//             ";
        bw.write( lecmd.substring( 0, 71 )); bw.newLine();
        lecmd = padstr + lecmd.substring( 71 );
      }
      bw.write( lecmd ); bw.newLine();

      bw.write( "//SYSPRINT DD  SYSOUT=*" ); bw.newLine();
      if ( (unit != null) && (!unit.equals("")))
        unitstr = "UNIT=" + unit;
      else
        unitstr = "UNIT=VIO";
      bw.write( "//SYSUT1   DD  " + unitstr + ",SPACE=(CYL,(50,50))" );
      bw.newLine();
      bw.write( "//PRELINK  DD  DSN=" + prelinkDistLib_ + ",DISP=SHR");
      bw.newLine();
      bw.write( "//SYSLMOD  DD  DSN=" + hlq + distlib + ",DISP=SHR");
      bw.newLine();
      bw.write( "//SYSLIN   DD  *" ); bw.newLine();

      // distnames will contain the ; seperated list of parts that need
      // to be link-edited into this particular distribution library
      String distnames = (String)lkedDistNames.elementAt( i );

      // seperate each partname from this list
      StringTokenizer strTok = new StringTokenizer( distnames, ";" );
      while (strTok.hasMoreTokens())
      {
        String distentry = strTok.nextToken();

        // each partname will contain the partname followed by ":" and the
        // list of its aliases if any
        StringTokenizer st = new StringTokenizer( distentry, ":" );
        String distname = st.nextToken();
        String aliasStr = null;
        if (st.hasMoreTokens())
          aliasStr = st.nextToken();
        bw.write( "  INCLUDE PRELINK(" + distname + ")" ); bw.newLine();

        // get all the aliases for this distname
        if (aliasStr != null)
        {
          StringTokenizer aliasTok = new StringTokenizer( aliasStr, ",");
          while (aliasTok.hasMoreTokens())
          {
            bw.write( "  ALIAS " + aliasTok.nextToken() ); bw.newLine();
          }
        }
        bw.write( "  NAME " + distname + "(R)" ); bw.newLine();
      }

      bw.write("/*"); bw.newLine();
      jobInfo_.addStep("LESTEP" + stepnum, 4, "Link-edit pre-linked object modules");
    }

    // generate an IDCAMS step to delete the PRELINK data set
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Delete pre-link dataset");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write( "//DELPRL   EXEC PGM=IDCAMS" ); bw.newLine();
    bw.write( "//SYSPRINT DD SYSOUT=*" ); bw.newLine();
    bw.write( "//SYSIN    DD *" ); bw.newLine();
    bw.write( "  DELETE " + prelinkDistLib_ ); bw.newLine();
    bw.write( "//" ); bw.newLine();
    jobInfo_.addStep("DELPRL", 0, "Delete Link-edit pre-link dataset");
  }  // genLinkEditStep

  /**
   * Generate JCL to execute the GENALIAS Rexx exec to generate aliases
   * in the distribution libraries.
   * The distlibInfo argument contains the distlib as the key and
   * the full distlib name (applid.function.distlib) as the value.
   */
  private void genAliasStep( BufferedWriter bw, Hashtable distlibInfo )
    throws MvsPkgError, IOException
  {
    // find all type=IPP shippables with alias (ALIAS,DALIAS,TALIAS,MALIAS)
    ArrayList        hs  = pkgData_.getShipDataSet();
    ListIterator     hsi = hs.listIterator();

    boolean stepStarted = false;

    while ( hsi.hasNext() )
    {
      // ALIAS rules for each parttype:
      // MOD - dalias, talias (mutually exclusive)
      // MAC - malias
      // DATA, PROGRAM - alias
      // Each alias definition can have multiple comma-seperated aliases
      MvsShipData shipData = (MvsShipData) hsi.next();
      boolean skipThisDistLib = false;

      if (!shipData.isTypeIpp())
      {
        continue;
      }

      Hashtable    ht = shipData.getShipData();

      String partType = (String) ht.get("PARTTYPE");
      if (partType == null)
      {
        throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                  new Object[] {"PARTTYPE", shipData.getSourceFile()});
      }

      String    alias = (String) ht.get("ALIAS");
      String   dalias = (String) ht.get("DALIAS");
      String   talias = (String) ht.get("TALIAS");
      String   malias = (String) ht.get("MALIAS");

      String theAlias = null;
      if (alias != null) theAlias = alias;
      else if (dalias != null) theAlias = dalias;
      else if (talias != null) theAlias = talias;
      else if (malias != null) theAlias = malias;

      if (theAlias == null)
      {
        continue;
      }
      else if (malias != null && !partType.equals("MAC"))
      {
        throw new MvsPkgError(MvsPkgError.invalidForParttype3,
                  new Object[] {shipData.getSourceFile(), "MALIAS", partType});
      }
      else if ( (dalias != null | talias != null) &&
                !partType.equals("MOD") )
      {
        if (dalias != null)
        {
          throw new MvsPkgError(MvsPkgError.invalidForParttype3,
                    new Object[] {shipData.getSourceFile(),"DALIAS", partType});
        }
        else
        {
          throw new MvsPkgError(MvsPkgError.invalidForParttype3,
                    new Object[] {shipData.getSourceFile(),"TALIAS", partType});
        }
      }
      else if ( dalias != null && talias != null )
      {
        throw new MvsPkgError(MvsPkgError.mutuallyExclusive3,
                  new Object[] {shipData.getSourceFile(), "DALIAS", "TALIAS"});
      }
      else if ( alias != null && ( partType.equals("MOD") ||
                                   partType.equals("MAC") ||
                                   partType.equals("SRC") ||
                                   partType.equals("HFS") ||
                                   partType.equals("JCLIN") ) )
      {
        throw new MvsPkgError(MvsPkgError.invalidForParttype3,
                  new Object[] {shipData.getSourceFile(), "ALIAS", partType});
      }

      String distlib = (String) ht.get("DISTLIB");
      if (distlib == null)
      {
        throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                  new Object[] {"DISTLIB", shipData.getSourceFile()});
      }

      String distname = (String) ht.get("DISTNAME");
      if (distname == null)
      {
        throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                  new Object[] {"DISTNAME", shipData.getSourceFile()});
      }

      String distlibDSN = null;

      // It should be checked if this distname is also prelinked
      // If it is, then the alias should not be generated here as it'll be
      // generated in the link-edit step
      for (int index = 0; index < lkedDistNames.size(); index++)
      {
        // each entry in lkedDistNames is a list of ; seperated parttypes
        // along with their aliases
        String distnameList = (String) lkedDistNames.elementAt(index);
        StringTokenizer st = new StringTokenizer( distnameList, ";" );

        // processing each distname along with its aliases
        while (st.hasMoreTokens())
        {
          String distnamePlusAlias = st.nextToken();
          String distnameEntry;

          // seperating distname from its aliases
          if (distnamePlusAlias.indexOf( ":" ) != -1)
          {
            distnameEntry = distnamePlusAlias.substring(0,
                                            distnamePlusAlias.indexOf( ":" ) );
          }
          else
          {
            // there is no alias for this distname
            distnameEntry = distnamePlusAlias;
          }
          if (distnameEntry.equals( distname ))
          {
              // if this distname is prelinked, then don't generate an alias
              // for it
              skipThisDistLib = true;
              break;
          }
        }
      }

      if (skipThisDistLib)
        continue;

      if (distlibDSN == null)
      {
        distlibDSN = (String) distlibInfo.get(distlib);
      }
      
      String hfsCopyType = (String) ht.get("HFSCOPYTYPE");

      // do not throw exception if jclin_only  
      // - target distlib doesn't have to exist in distlibs in that case
      if (distlibDSN == null)
      {
        if ( (hfsCopyType != null) &&
            !(hfsCopyType.equalsIgnoreCase( "jclin_only" )) &&
            !(hfsCopyType.equalsIgnoreCase( "none" )) )
        {
          throw new MvsPkgError(MvsPkgError.distlibNotFound1,
                                new Object[] {distlib});
        }
      }

      if (!stepStarted)
      {
        stepStarted = true;
        bw.write(commentLine_); bw.newLine();
        bw.write("//* Generate aliases"); bw.newLine();
        bw.write(commentLine_); bw.newLine();
        bw.write( genRexxTsoJcl("GENALIAS", "RC=0", null) );
      }

      // create distlib data set/member name for quick ref. in loop
      String srcDsn = new StringBuffer(52)
                          .append(distlibDSN).append("(")
                          .append(distname).append(")").toString();

      StringTokenizer st = new StringTokenizer(theAlias,",");
      while ( st.hasMoreTokens() )
      {
        bw.write("  "); bw.write(srcDsn); bw.write(" ");
        bw.write(st.nextToken()); bw.newLine();
      }
    }  //while


    if (stepStarted)
    {
      bw.write("/*"); bw.newLine();
      bw.write("//         ENDIF"); bw.newLine();

      jobInfo_.addStep("GENALIAS", 0, "Generate aliases");
    }

    return;
  }  //genAliasStep()

  /**
   * Generate JCL to create DEFPKGS file and run the SUBUILD RTG program.
   */
  private void genSubuildSteps( BufferedWriter bw )
    throws MvsPkgError, IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* DEFPKGS and RTG package generation"); bw.newLine();
    bw.write(commentLine_); bw.newLine();

    // instream data sets to be generated for SUBUILD
    StringBuffer systsin     = new StringBuffer();     // RTG
    StringBuffer bldctl      = new StringBuffer();     // RTG

    Hashtable pd = pkgData_.getProductData();

    // product data tags
    String applid      = (String) pd.get("APPLID");         // required
    String function    = (String) pd.get("FUNCTION");       // required
    String fmid        = (String) pd.get("FMID");           // required
    String srel        = (String) pd.get("SREL");           // required
    String desc        = (String) pd.get("DESCRIPTION");    // required
    String sep         = (String) pd.get("SEP");            // optional

    if (applid == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"APPLID"});
    }
    else if (function == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"FUNCTION"});
    }
    else if (fmid == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"FMID"});
    }
    else if (srel == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"SREL"});
    }
    else if (desc == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"DESCRIPTION"});
    }

    // short version of applid.function high level qualifiers:
    String dsnHlq = applid + "." + function;

    // create RTG SYSTSIN
    systsin.append("  CALL '").append(MvsProperties.subuildLoadlib)
           .append("(RTG)' +");
    systsin.append(NL_);
    systsin.append("  'RTGMODE=STOP,RTGOP=DISK");
    if (MvsProperties.rtgAllocInfo != null)
    {
      systsin.append(",+").append(NL_).append("  ")
             .append(MvsProperties.rtgAllocInfo).append("'").append(NL_);
    }
    else
    {
      systsin.append("'").append(NL_);
    }

    // create RTG BLDCTL input
    bldctl.append("$ BUILD ID=").append(function);
    bldctl.append(",QUALIFY=").append(dsnHlq);
    bldctl.append(NL_);

    // $ SEP keywords for RTG BLDCTL
    if (sep != null)
    {
      // sepTok1 used to parse blank-delimited $SEP statements
      // sepTok2 used to parse comma-delimited dlib entries for a $SEP stmt
      StringTokenizer sepTok1 = new StringTokenizer(sep, " ");
      while (sepTok1.hasMoreTokens())
      {
        StringTokenizer sepTok2;
        sepTok2 = new StringTokenizer(sepTok1.nextToken(), ",");

        bldctl.append("$ SEP DLIB=(").append(sepTok2.nextToken());
        while (sepTok2.hasMoreTokens())
        {
          bldctl.append(",").append(NL_).append("$           ")
                .append(sepTok2.nextToken());
        }
        bldctl.append(")").append(NL_);
      }
    }

    // generate an IDCAMS step to delete the DEFPKGS data set
    // and to delete/define the temporary work KSDS for RTG and
    String idcamsExec = "//DELDEF   EXEC PGM=IDCAMS";
    String sysprintDD = "//SYSPRINT DD SYSOUT=*";
    String idcSysinDD = "//SYSIN    DD *";

    String vsamClusterName = new StringBuffer(44).append(dsnHlq)
                                                 .append(".RTG.WORK")
                                                 .toString();

    bw.write(idcamsExec); bw.newLine();
    bw.write(sysprintDD); bw.newLine();
    bw.write(idcSysinDD); bw.newLine();
    bw.write("  DELETE "); bw.write(dsnHlq); bw.write(".DEFPKGS");
    bw.newLine();
    bw.write("  DELETE "); bw.write(vsamClusterName);
    bw.newLine();
    bw.write("  SET MAXCC=0"); bw.newLine();
    bw.write("  /* DEFINE TEMPORARY VSAM WORK CLUSTER FOR RTG */");
    bw.newLine();
    bw.write("  DEFINE CLUSTER ( -"); bw.newLine();
    bw.write("    NAME("); bw.write(vsamClusterName); bw.write(") -");
    bw.newLine();
    bw.write("    KEYS(24 0) -"); bw.newLine();
    bw.write("    RECORDSIZE(558 558) -"); bw.newLine();
    if (MvsProperties.vsamVolumeInfo != null)
    {
      bw.write("    VOLUMES("); bw.write(MvsProperties.vsamVolumeInfo);
      bw.write(") -"); bw.newLine();
    }
    bw.write("    CYLINDERS(5 2) )"); bw.newLine();
    bw.write("/*"); bw.newLine();

    jobInfo_.addStep("DELDEF", 0, "IDCAMS deletes & define cluster");

    // simulate CTLDEF by generating SMP statements in DEFPKGS
    genDefPkgs(bw);

    // generate subuild rtg step
    // NOTE: REGION=4M is specified for IKJEFT01 because default region size
    // (on STL machines) is 1M.  RTG invokes IEBCOPY and 1M is not enough
    // storage to run IEBCOPY.  Default region at other sites may be higher,
    // but just to be safe make sure we get at least 4M because error msgs
    // that are received if region is too small are very cryptic and are not
    // really indicitave of the problem.  Region specified on job card can
    // always be used to override this value if it is too small for a given
    // package.
    String ifStmt2    = "//         IF (RC=0) THEN";
    String rtgExec    = "//RTG      EXEC PGM=IKJEFT01,REGION=4M";
    String bldctlDD   = "//BLDCTL   DD *";
    String systsinDD  = "//SYSTSIN  DD *";
    String funccntlDD = "//FUNCCNTL DD DISP=OLD,DSN=";
    String defpkgsDDs = "//DEFPKGS  DD DISP=SHR,DSN=";
    String cpyrightDD = "//CPYRIGHT DD DISP=SHR,DSN=";
    String sysinDD    = "//SYSIN    DD SPACE=(TRK,(1,1)),UNIT=SYSDA";
    String sysut3DD   = "//SYSUT3   DD SPACE=(CYL,(1,1)),UNIT=SYSDA";
    String sysut4DD   = "//SYSUT4   DD SPACE=(CYL,(1,1)),UNIT=SYSDA";
    String rtgut1DD   = "//RTGUT1   DD DISP=(OLD,DELETE),DSN=";
    String systsprtDD = "//SYSTSPRT DD SYSOUT=*";
    String sysoutDD   = "//SYSOUT   DD SYSOUT=*";
    String rtgprintDD = "//RTGPRINT DD SYSOUT=*";
    String rtgoutDD   = "//RTGOUT   DD SYSOUT=*";
    String funcoutDD  = "//FUNCOUT  DD SYSOUT=*";
    String tapeprtDD  = "//TAPEPRT  DD SYSOUT=*";
    String sysprtDmy  = "//SYSPRINT DD DUMMY";
    String reltapeDD  = "//RELTAPE  DD DUMMY";
    String endifStmt  = "//         ENDIF";

    bw.write(commentLine_); bw.newLine();
    bw.write(ifStmt2); bw.newLine();
    bw.write(rtgExec); bw.newLine();
    bw.write(bldctlDD); bw.newLine();
    bw.write(bldctl.toString());
    bw.write("/*"); bw.newLine();
    bw.write(systsinDD); bw.newLine();
    bw.write(systsin.toString());
    bw.write("/*"); bw.newLine();
    bw.write(funccntlDD); bw.write(MvsProperties.funccntlDsn); bw.newLine();
    bw.write(defpkgsDDs); bw.write(dsnHlq); bw.write(".DEFPKGS");
    bw.newLine();
    bw.write(cpyrightDD); bw.write(dsnHlq); bw.write(".CPYRIGHT");
    bw.newLine();
    bw.write(sysinDD); bw.newLine();
    bw.write(sysut3DD); bw.newLine();
    bw.write(sysut4DD); bw.newLine();
    bw.write(rtgut1DD); bw.write(vsamClusterName); bw.newLine();
    bw.write(systsprtDD); bw.newLine();
    bw.write(sysoutDD); bw.newLine();
    bw.write(rtgprintDD); bw.newLine();
    bw.write(rtgoutDD); bw.newLine();
    bw.write(funcoutDD); bw.newLine();
    bw.write(tapeprtDD); bw.newLine();
    bw.write(sysprtDmy); bw.newLine();
    bw.write(reltapeDD); bw.newLine();
    bw.write(endifStmt); bw.newLine();

    jobInfo_.addStep("RTG", 0, "RTG");

    return;
  }  //genSubuildSteps()

  /**
   * Generates SMP statements in a DEFPKGS file through JCL. These statements
   * will be used by RTG and are formatted as if they were generated by ctldef
   */
  private void genDefPkgs( BufferedWriter bw )
    throws MvsPkgError, IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* generate DEFPKG"); bw.newLine();
    bw.write(commentLine_); bw.newLine();

    // instream data sets to be generated for SUBUILD
    StringBuffer items       = new StringBuffer();
    StringBuffer smpcntl     = new StringBuffer();
    StringBuffer otherSmp    = new StringBuffer();
    StringBuffer deleteMcsSB = new StringBuffer();

    Hashtable pd = pkgData_.getProductData();

    String defpkgsDDn1 = "//STEP02 EXEC PGM=IEFBR14,COND=(4,LT)";
    String defpkgsDDn2 = "//DEFPKGS  DD DISP=(NEW,CATLG),DSN=";
    String defpkgsDDn3 = "//         LRECL=80,DSORG=PS,RECFM=FB,BLKSIZE=0,";
    String defpkgsDDn4 = "//         SPACE=(CYL,(1,1)),UNIT=SYSDA";
    String defpkgsDDn5 = "//STEP00 EXEC PGM=IEBGENER";
    String defpkgsDDn6 = "//SYSUT1 DD *";
    String defpkgsDDn7 = "//SYSUT2 DD DISP=MOD,DSN=";
    String defpkgsDDn8 = "//SYSIN DD DUMMY";
    String defpkgsDDn9 = "//SYSPRINT DD SYSOUT=*";

    // product data tags
    String applid      = (String) pd.get("APPLID");         // required
    String function    = (String) pd.get("FUNCTION");       // required
    String fmid        = (String) pd.get("FMID");           // required
    String srel        = (String) pd.get("SREL");           // required
    String desc        = (String) pd.get("DESCRIPTION");    // required
    String distlibs    = (String) pd.get("DISTLIBS");       // required
    String sup         = (String) pd.get("SUP");            // optional
    ArrayList  ifreq   = (ArrayList)  pd.get("IF");         // optional
    String npre        = (String) pd.get("NPRE");           // optional
    String pre         = (String) pd.get("PRE");            // optional
    String req         = (String) pd.get("REQ");            // optional
    String delete      = (String) pd.get("DELETE");         // optional
    String ctldefin    = (String) pd.get("CTLDEFINFILE");   // optional
    String extraSmpe   = (String) pd.get("EXTRASMPEFILE");  // optional
    String fesn        = (String) pd.get("FESN");           // optional
    String versionReq  = (String) pd.get("VERSIONREQ");     // optional
    String rework      = (String) pd.get("REWORK");         // optional
    String jclinLib    = (String) pd.get("JCLINLIB");       // optional
    ArrayList  delReq  = (ArrayList)  pd.get("DEL");        // optional

    boolean generateJCLIN = false;
    boolean useCalllibs = false;

    // short version of applid.function high level qualifiers:
    String dsnHlq = applid + "." + function;

    // generate ++FUNCTION command in the JCL for DEFPKGS which contains
    // product level information
    items.append("++FUNCTION(").append(function).append(")").append(NL_);
    if (fesn != null)
      items.append("  FESN(").append(fesn).append(")").append(NL_);
    items.append("  REWORK(").append(rework).append(")").append(NL_);
    items.append("  DESC(").append(desc).append(")").append(NL_);
    items.append("  RFDSNPFX(IBM)").append(NL_);
    items.append("  .").append(NL_);

    // generate ++VER command in the JCL for DEFPKGS
    if (srel.length() != 4)
      throw new MvsPkgError(MvsPkgError.invalidProductTag2,
                            new Object[] {"SREL", srel});
    items.append("++VER(").append(srel).append(")");
    if (!fmid.equals(function))
      items.append(NL_).append("  FMID(").append(fmid).append(")");

    if (sup != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "SUP", sup, "," ));
    }
    if (pre != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "PRE", pre, "," ));
    }
    if (npre != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "NPRE", npre, "," ));
    }
    if (req != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "REQ", req, "," ));
    }
    if (delete != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "DELETE", delete, "," ));
    }
    if (versionReq != null)
    {
      items.append(NL_);
      items.append( checkAndFormatSmp( "VERSION", versionReq, "," ));
    }
    items.append(NL_).append("  .");

    // generate ++DELETE statements for DEFPKGS
    // reading each entry in the delReq array, formatting it into ++DELETE style
    // and appending it to a stringbuffer
    if (delReq != null)
    {
      ListIterator i = delReq.listIterator();
      while (i.hasNext())
        deleteMcsSB.append( genDeleteMcs( (String) i.next() )).append(NL_)
               .append("  .").append(NL_);
    }
    // generate ++MOD, ++MAC, ++HFS, ++JCLIN, ++PROGRAM, ++SRC
    // statements for DEFPKGS
    ArrayList       hs = pkgData_.getShipDataSet();
    ListIterator    hsi = hs.listIterator();

    int     partCount = 0;
    boolean gotJcllib = false;
    StringBuffer jclinMcs = new StringBuffer(80);

    // process part data
    while (hsi.hasNext())
    {
      MvsShipData shipData = (MvsShipData) hsi.next();
      if (shipData.isTypeIpp())
      {
        Hashtable ht = shipData.getShipData();
        String ldata = shipData.getLongData();

        String distlib  = (String) ht.get("DISTLIB");    // required
        String distname = (String) ht.get("DISTNAME");   // required
        String parttype = (String) ht.get("PARTTYPE");   // required
        String assem    = (String) ht.get("ASSEM");      // ++MAC
        String distmod  = (String) ht.get("DISTMOD");    // ++MAC
        String distsrc  = (String) ht.get("DISTSRC");    // ++MAC
        String malias   = (String) ht.get("MALIAS");     // ++MAC
        String prefix   = (String) ht.get("PREFIX");     // ++MAC
        String alias    = (String) ht.get("ALIAS");      // data, ++PROGRAM
        String csect    = (String) ht.get("CSECT");      // ++MOD
        String dalias   = (String) ht.get("DALIAS");     // ++MOD
        String leparm   = (String) ht.get("LEPARM");     // ++MOD
        String lmod     = (String) ht.get("LMOD");       // ++MOD
        String talias   = (String) ht.get("TALIAS");     // ++MOD
        String lkedTo   = (String) ht.get("LKEDTO");     // ++MOD
        String syslibs  = (String) ht.get("SYSLIBS");     // ++MOD
        String syslib   = (String) ht.get("SYSLIB");     // all except ++MOD
        String binary   = (String) ht.get("BINARY");     // ++HFS
        String text     = (String) ht.get("TEXT");       // ++HFS
        String link     = (String) ht.get("LINK");       // ++HFS
        String symlink  = (String) ht.get("SYMLINK");    // ++HFS
        String sympath  = (String) ht.get("SYMPATH");    // ++HFS
        String parm     = (String) ht.get("PARM");       // ++HFS
        String perms    = (String) ht.get("PERMISSIONS");// ++HFS
        String shscript = (String) ht.get("SHSCRIPT");   // ++HFS
        String version  = (String) ht.get("VERSION");    // all
        String calllibs = (String) ht.get("CALLLIBS");   // ++JCLIN
        String linkData = (String) shipData.getLongData();   // Dataset Ref
        String hfsCopyType = (String) ht.get("HFSCOPYTYPE");

        StringBuffer tempMcs = new StringBuffer(80);

        if (distlib == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTLIB", shipData.getSourceFile()});
        }
        else if (distname == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTNAME", shipData.getSourceFile()});
        }
        else if (parttype == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"PARTTYPE", shipData.getSourceFile()});
        }

        // validate parttype (SMP MCS)
        if (!MvsValidation.validatePartType(parttype))
        {
          throw new MvsPkgError(MvsPkgError.invalidPartType2,
                    new Object[] {shipData.getSourceFile(), parttype});
        }

        // validate MCS keywords
        String invalidKwds = MvsValidation.validateMcsKeywords(parttype,
                                                                ht, ldata);
        if ( invalidKwds != null )
        {
          throw new MvsPkgError(MvsPkgError.invalidMcsKeywords3,
                new Object[] {shipData.getSourceFile(), parttype, invalidKwds});
        }

        if (parttype.equals("JCLIN"))
        {
          // distname must match function, or else...
          if (!distname.equals(function))
          {
            throw new MvsPkgError(MvsPkgError.jclinDistname3,
                new Object[] {shipData.getSourceFile(), distname, function});
          }
          if (gotJcllib)
          {
            throw new MvsPkgError(MvsPkgError.onlyOneJclinAllowed1,
                            new Object[] {shipData.getSourceFile()});
          }
          gotJcllib = true;

          // SMP statements for JCLIN should appear before other SMP statements
          // hence the JCLIN statement is put into a different stringbuffer
          // than the other SMP statements
          jclinMcs.append("++").append(parttype);
          jclinMcs.append("    RELFILE(").append(distlib).append(")");
          if (calllibs != null)
            jclinMcs.append("  CALLLIBS");
          jclinMcs.append(NL_).append("  .").append(NL_);

          // bypass the rest of the part processing for JCLIN...
          continue;
        }

        // generate MCS information to temporary buffer
        tempMcs.append("++").append(parttype).append("    (")
               .append(distname).append(")  DISTLIB(").append(distlib)
               .append(")");

        // MCS for ++MOD
        if (parttype.equals("MOD"))
        {
          if (csect != null)
            tempMcs.append(NL_).append( "  CSECT(" )
                               .append( csect ).append( ")" );
          if (dalias != null)
            tempMcs.append(NL_).append( "  DALIAS(" )
                               .append( dalias ).append( ")" );
          if (leparm != null)
            tempMcs.append(NL_).append( "  LEPARM(" )
                               .append( leparm ).append( ")" );
          if (lmod != null)
            tempMcs.append(NL_).append( "  LMOD(" )
                               .append( lmod ).append( ")" );
          if (talias != null)
            tempMcs.append(NL_).append( "  TALIAS(" )
                               .append( talias ).append( ")" );
          // If we have a ++MOD with lkedTo attribute, we must generate JCLIN
          if ( !generateJCLIN && (lkedTo != null))
            generateJCLIN = true;
          // If we have a ++MOD with sysLibs attribute, we must use CALLLIBS
          if ( !useCalllibs && (syslibs != null))
            useCalllibs = true;
        }
        // MCS for ++HFS
        else if (MvsValidation.isHfsPartType( parttype ))
        {
          if (binary != null)
            tempMcs.append(NL_).append("  BINARY");
          else if (text != null)
            tempMcs.append(NL_).append("  TEXT");

          // linkData has LINK, SYMLINK, SYMPATH with proper formatting
          if (linkData != "")
            tempMcs.append(NL_).append(linkData);
          if (parm != null)
            tempMcs.append(NL_).append( "  PARM(" )
                               .append( parm ).append( ")" );
          // Can optionally use permissions for PARM(PATHMODE) statements
          if ((perms != null) &&
             ((perms.length() == 3) || (perms.length() == 4)))
          {
            // Prepend "0" if only three numbers
            if (perms.length() == 3)
              perms = "0" + perms;
            // convert to "0,7,5,5"
            String setOptStr = "  PARM(PATHMODE(" + perms.charAt(0) + "," +
                       perms.charAt(1) + "," + perms.charAt(2) + ","
                       + perms.charAt(3) + "))";
            tempMcs.append(NL_).append( setOptStr );

          }
          if (shscript != null)
            tempMcs.append(NL_).append( "  SHSCRIPT(" )
                               .append( shscript ).append( ")" );
        }
        // MCS for ++MAC
        else if (parttype.equals("MAC"))
        {
          if (assem != null)
            tempMcs.append(NL_).append( "  ASSEM(" )
                               .append( assem ).append( ")" );
          if (distmod != null)
            tempMcs.append(NL_).append( "  DISTMOD(" )
                               .append( distmod ).append( ")" );
          if (distsrc != null)
            tempMcs.append(NL_).append( "  DISTSRC(" )
                               .append( distsrc ).append( ")" );
          if (malias != null)
            tempMcs.append(NL_).append( "  MALIAS(" )
                               .append( malias ).append( ")" );
          if (prefix != null)
            tempMcs.append(NL_).append( "  PREFIX(" )
                               .append( prefix ).append( ")" );
        }
        // MCS for ++SRC
        else if (parttype.equals("SRC"))
        {
          // nothing to do here - there are no keywords specific to ++SRC:
          // DISTLIB/DISTNAME keywords handled above for all parts
          // VERSION/SYSLIB keywords handled below for all parts
          // DISTMOD is not allowed per packaging rules (see R3.0 code)
        }
        // MCS for data elements and ++PROGRAM
        else
        {
          if (alias != null)
            tempMcs.append(NL_).append( "  ALIAS(" )
                               .append( alias ).append( ")" );
        }

        // MCS for all parttypes
        if (version != null)
        {
          tempMcs.append(NL_).append( "  VERSION(" )
                             .append( version ).append( ")" );
        }

        // MCS for all parttypes except ++MOD
        if (!parttype.equals("MOD"))
        {
          if (syslib != null)
            tempMcs.append(NL_).append( "  SYSLIB(" )
                               .append( syslib ).append( ")" );
        }

        tempMcs.append(NL_).append("  .").append(NL_);

        // append formatted mcs to smpcntl input
        // Do not write SMP statement if hfsCopyType is jclin_only
        if (( hfsCopyType == null) ||
            (!hfsCopyType.equalsIgnoreCase( "jclin_only" ) &&
             !hfsCopyType.equalsIgnoreCase( "none" )))
          smpcntl.append(tempMcs);
        tempMcs = null;

      } //if
    } //while

    // Handle additional ++JCLIN processing outside of file stanzas
    // This is only for automaticallay generated JCLIN parts based on
    // ++MOD part parameters
    if (generateJCLIN)
    {
      // If already processed an ++JCLIN, throw an exception
      if (gotJcllib)
      {
        throw new MvsPkgError("Error: There can be only one JCLIN part." + "\n"+
           " A ++JCLIN part was already specified - cannot generate JCLIN part");
      }
      // Get jclinLibName if it doesn't exist
      if (jclinLib == null)
      {
        // Get first distlib name
        int tmpidx = distlibs.indexOf( "," );
        String ident;
        // Get first four chars of distlib
        if (tmpidx >= 0)
          ident = distlibs.substring( 0, tmpidx );
        else
          ident = "";
        if (ident.length() >= 4)
          ident = ident.substring( 0, 4 );
        // Allocate as USERID.FMID.xxxxJCL
        jclinLib = ident + "JCL";
        jclinDistLib_ = dsnHlq + "." + jclinLib;
      }

      // SMP statements for JCLIN should appear before other SMP statements
      // hence the JCLIN statement is put into a different stringbuffer
      // than the other SMP statements
      jclinMcs.append("++JCLIN");
      jclinMcs.append(" RELFILE(").append(jclinLib).append(")");
      if (useCalllibs)
        jclinMcs.append(" CALLLIBS");
      jclinMcs.append(" .").append(NL_);
    }

    StringBuffer extraSmpeSB = new StringBuffer();
    StringBuffer ifreqSB = new StringBuffer();
    String  extraSmpe_tempStr = null;

    if ( (ctldefin != null) && (extraSmpe != null) )
      extraSmpe_tempStr = extraSmpe;
    else if (ctldefin != null)
      extraSmpe_tempStr = ctldefin;
    else if (extraSmpe != null)
      extraSmpe_tempStr = extraSmpe;

    if (extraSmpe_tempStr != null)
    {
      int nr_read = 0;
      StringBuffer  extraSmpePath = new StringBuffer(80);
      String relativeExtraSmpe;
      byte  b[] = new byte[10000];
      if (extraSmpe_tempStr.indexOf('/') != -1)  // HFS file
      {
        extraSmpePath.append(MvsProperties.toStage);
        relativeExtraSmpe = stripLeadingFS(extraSmpe_tempStr);
        extraSmpePath.append(relativeExtraSmpe);
      }

      try
      {
        FileInputStream fin = new FileInputStream( extraSmpePath.toString() );
        nr_read = fin.read( b );
        while (nr_read != -1)
        {
          String val = new String( b,0,nr_read );
          extraSmpeSB.append( val );
          nr_read = fin.read( b );
        }
        fin.close();
      }
      catch (IOException e)
      {
         throw new MvsPkgError(MvsPkgError.ioException3,
                          new Object[] {"reading", extraSmpe, e.getMessage()});
      }
    }

    // reading each entry in the ifreq array, formatting it into ++IF style
    // and appending it to a stringbuffer
    if (ifreq != null)
    {
      ListIterator ai = ifreq.listIterator();
      while (ai.hasNext())
        ifreqSB.append( formatIfReq( (String) ai.next() )).append(NL_)
               .append("  .").append(NL_);
    }

    bw.write(defpkgsDDn1);
    bw.newLine();
    bw.write(defpkgsDDn2); bw.write(dsnHlq); bw.write(".DEFPKGS,");
    bw.newLine();
    bw.write(defpkgsDDn3); bw.newLine();
    bw.write(defpkgsDDn4); bw.newLine();
    bw.write(defpkgsDDn5); bw.newLine();
    bw.write(defpkgsDDn6); bw.newLine();
    bw.write(items.toString()); bw.newLine();
    if (extraSmpe_tempStr != null)
    {
      bw.write(extraSmpeSB.toString());
    }
    if (ifreq != null)
    {
      bw.write(ifreqSB.toString());
    }
    if (delReq != null)
    {
      bw.write(deleteMcsSB.toString());
    }
    bw.write(jclinMcs.toString());
    bw.write(smpcntl.toString());
    bw.write("/*"); bw.newLine();
    bw.write(defpkgsDDn7); bw.write(dsnHlq); bw.write(".DEFPKGS");
    bw.newLine();
    bw.write(defpkgsDDn8); bw.newLine();
    bw.write(defpkgsDDn9); bw.newLine();
  }

  /**
   * This method generates the information for MCS ++DELETE statement
   *
   * @param inputString String with filename and syslib/alias info
   *                    example:  filename lib1,lib2 a1
   *
   * @return delMcs     Output String with ++DELETE MCS statement
   *                    example:  ++DELETE(filename)
   *                                SYSLIB(lib1,lib2)
   *                                ALIAS(a1)
   *
   * @exception MvsPkgError if an error occurs
   **/
  private String genDeleteMcs(String inputString) throws MvsPkgError
  {
    StringTokenizer wordTokens = new StringTokenizer( inputString );
    int numOfWordsInString = wordTokens.countTokens();

    String modName = (String) wordTokens.nextToken();
    String delMcs = "++DELETE(" + modName + ")";

    while ( wordTokens.hasMoreTokens() )
    {
      if (numOfWordsInString == 3)  // format: filename syslib alias
      {
        String syslib = "SYSLIB";
        String syslibList = (String) wordTokens.nextToken();

        delMcs += "\n";
        delMcs += StringTools.checkAndFormatString(syslib, syslibList);
        delMcs += "\n";

        String alias = "ALIAS";
        String aliasList = (String) wordTokens.nextToken();

        delMcs += StringTools.checkAndFormatString(alias, aliasList);
      }
      else if (numOfWordsInString == 2) // format: filename syslib
      {
        String syslib = "SYSLIB";
        String syslibList = (String) wordTokens.nextToken();

        delMcs += "\n";
        delMcs += StringTools.checkAndFormatString(syslib, syslibList);
      }
    }

    return delMcs;
  }

  /**
   * Generate JCL to execute the VPLFILES Rexx exec to create the VPL listings
   *
   * @param bw BufferedWriter object that represents the jcl file
   * @param ptfNumber a valid PTF number
   */
  private void genVplStep( BufferedWriter bw, String ptfNumber )
    throws MvsPkgError, IOException
  {
    // NOTE: it is expected that the 'TERSE' clist is made available in
    //       the sysproc/sysexec concatenation via the logon proc

    // get the product level VPL information:
    Hashtable prodData = pkgData_.getProductData();

    String applid       = (String) prodData.get("APPLID");         // required
    String function     = (String) prodData.get("FUNCTION");       // required
    String type         = (String) prodData.get("TYPE");           // required
    String vplAuthCode  = (String) prodData.get("VPLAUTHCODE");    // required
    String vplFromSys   = (String) prodData.get("VPLFROMSYS");     // required
    String vplVer       = (String) prodData.get("VPLVER");         // required
    String vplRel       = (String) prodData.get("VPLREL");         // required
    String vplMod       = (String) prodData.get("VPLMOD");         // required
    String vplAckn      = (String) prodData.get("VPLACKN");        // optional
    String vplAvailDate = (String) prodData.get("VPLAVAILDATE");   // optional
    String vpl          = (String) prodData.get("VPL");            // optional

    // flag to determine if vplfiles.rexx should call TERSE or TRSMAIN
    String terseFlag;

    // depending on terseFlag, this variable contains sysproc or steplib dd statements
    String additionalJcl;

    // get PKG_CLASS and change it to upperCase for ptfvpl file
    String pkgClass = MvsProperties.pkgClass.toUpperCase();

    if (applid == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"APPLID"});
    }
    else if (function == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"FUNCTION"});
    }
    else if (vplAuthCode == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"VPLAUTHCODE"});
    }
    else if (vplFromSys == null)
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"VPLFROMSYS"});
    }
    else if (vplVer == null  && MvsProperties.pkgClass.equalsIgnoreCase("IPP"))
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"VPLVER"});
    }
    else if (vplRel == null  && MvsProperties.pkgClass.equalsIgnoreCase("IPP"))
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"VPLREL"});
    }
    else if (vplMod == null  && MvsProperties.pkgClass.equalsIgnoreCase("IPP"))
    {
      throw new MvsPkgError(MvsPkgError.requiredProductTag1,
                            new Object[] {"VPLMOD"});
    }

    // change null values...
    if (vplAckn == null)
    {
      vplAckn = "ERROR";
    }
    if (vplAvailDate == null)
    {
      vplAvailDate = "NONE";
    }

    // if terseLoadDsn is specified in the command line, it is used ignoring the
    // specification for terseClistDsn
    if (MvsProperties.terseLoadDsn != null)
    {
      // create steplib dd stmt for trsmain
      additionalJcl = "//STEPLIB DD DISP=SHR,DSN=" + MvsProperties.terseLoadDsn;
      // making vplfiles.rexx use TRSMAIN
      terseFlag = "TRSMAIN";
    }
    else if (MvsProperties.terseClistDsn != null)
    {
      // create sysproc dd stmt for terse clist
      additionalJcl = "//SYSPROC DD DISP=SHR,DSN=" + MvsProperties.terseClistDsn;
      // making vplfiles.rexx use TERSE
      terseFlag = "TERSE";
    }
    else
    {
      throw new MvsPkgError(MvsPkgError.requiredVariable,
        new Object[] {"PKG_MVS_TERSE_CLIST_DATASET or PKG_MVS_TERSE_LOAD_DATASET"});
    }

    // start jcl stream...
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Process VPL files"); bw.newLine();
    bw.write(commentLine_); bw.newLine();
    if (pkgClass.equalsIgnoreCase("IPP"))
    {
      bw.write(genRexxTsoJcl("VPLFILES",
                             "RC=0 | RTG.RC=0",
                             additionalJcl));
    }
    else if (pkgClass.equalsIgnoreCase("SP"))
    {
      bw.write(genRexxTsoJcl("VPLFILES",
                             "RC=0",
                             additionalJcl));
    }

    // first line of input to VPLFILES Rexx exec is the product level data:
    bw.write("  "); bw.write(pkgClass);
    bw.write(" "); bw.write(applid);
    bw.write(" "); bw.write(function);
    bw.write(" "); bw.write(vplAuthCode);
    bw.write(" "); bw.write(vplFromSys);
    if (pkgClass.equalsIgnoreCase("IPP"))
    {
      bw.write(" "); bw.write(vplVer);
      bw.write(" "); bw.write(vplRel);
      bw.write(" "); bw.write(vplMod);
    }
    else if (pkgClass.equalsIgnoreCase("SP"))
    {
      bw.write(" "); bw.write("0");
      bw.write(" "); bw.write("0");
      bw.write(" "); bw.write("0");
    }
    bw.write(" "); bw.write(vplAckn);
    bw.write(" "); bw.write(vplAvailDate);
    bw.write(" "); bw.write(terseFlag);
    if (pkgClass.equalsIgnoreCase("SP"))
    {
      if (MvsProperties.pkgType.equalsIgnoreCase("ptfvpl"))
      {
        bw.write(" "); bw.write(ptfNumber);
      }
    }
    bw.newLine();

    // find all shippable parts with type VPL
    ArrayList hs = pkgData_.getShipDataSet();
    ListIterator hsi = hs.listIterator();
    while (hsi.hasNext())
    {
      MvsShipData shipData = (MvsShipData)hsi.next();
      Hashtable ht = (Hashtable)shipData.getShipData();

      if (shipData.isTypeVpl())
      {
        if (pkgClass.equalsIgnoreCase("SP") &&
            !Path.exists(MvsProperties.toStage + shipData.getSourceFile()))
        {
          continue;
        }

        String distName     = (String) ht.get("DISTNAME");   // required
        String vplSecurity = (String) ht.get("VPLSECURITY");  // required
        String vplPartQual = (String) ht.get("VPLPARTQUAL");  // optional

        if (distName == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"DISTNAME", shipData.getSourceFile()});
        }
        else if (vplSecurity == null)
        {
          throw new MvsPkgError(MvsPkgError.requiredShipTag2,
                    new Object[] {"VPLSECURITY", shipData.getSourceFile()});
        }

        if (vplPartQual == null)
        {
          vplPartQual = "NONE";
        }

        StringBuffer line = new StringBuffer(80);
        if (shipData.getFileType() == MvsShipData.HFS)
        {
          line.append("  ").append(MvsProperties.toStage)
                           .append( stripLeadingFS(shipData.getSourceFile()) );
        }
        else
        {
          line.append("  ").append(shipData.getSourceFile());
        }
        line.append(" ").append(distName);
        line.append(" ").append(vplSecurity);
        line.append(" ").append(vplPartQual);
        bw.write( formatLine80(line) );
        bw.newLine();
      }
    } // while

    bw.write("/*"); bw.newLine();
    bw.write("//         ENDIF"); bw.newLine();
    jobInfo_.addStep("VPLFILES", 0, "Process VPL");
  }  // genVplStep

  /**
   * Generate JCL to run the ALLOCLIB Rexx exec to allocate the partlist
   * dataset. This dataset is allocated in a separate step so the service
   * packaging gather steps that follow (one gather step per package) can
   * write to it with DISP=MOD.
   */
  private void genPartlistAllocStep( BufferedWriter bw, String dataSetPrefix )
    throws MvsPkgError, IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Allocate PARTLIST data set"); bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write( genRexxTsoJcl("ALLOCLIB", "PARTLIST", null, null) );
    bw.write("  "); bw.write(dataSetPrefix); bw.write("PARTLIST ");
    bw.write("TRACKS 3 5 0 VB 256 0"); bw.newLine();
    bw.write("/*");
    bw.newLine();
    jobInfo_.addStep("PARTLIST", 0, "Allocate PARTLIST data set");
  }  // genPartlistAllocStep()

  /**
   * Calculate the number of blocks that the given HFS file would require in
   * a RECFM=VB MVS data set when copied in TEXT mode.
   */
  private int getVplBlks( String fileName )
  {
    // Space calculation for HFS file -> VB data set in TEXT mode:
    //   ( (# lines in file * 4) + # bytes in file ) / blksize
    // Since we don't know the number of lines in the file & we don't
    // want the overhead of reading the entire file just to count lines,
    // we use the approximation that the average line in a VPL listing is
    // 60 bytes. Therefore the number of lines in the file is approximately
    // the number of bytes in the file/60.

    int  bytes = getFileSize(fileName);
    int  lines = bytes/60;

    return ( ((lines * 4) + bytes) / vplBlksize ) + 1;
  }

  /**
   * Calculate the number of blocks that the given HFS file would require in
   * a RECFM=FB MVS data set when copied in BINARY mode.
   */
  private int getObjBlks( String fileName )
  {
    // Space calculation for HFS file -> FB data set in BINARY mode:
    //   (# bytes in file) / blksize

    return (getFileSize(fileName) / objBlksize) + 1;
  }

  /**
   * Returns the number of bytes in a file.
   */
  private int getFileSize(String fileName)
  {
    File f = new File(fileName);
    return (int) f.length();
  }

  /**
   * Strip leading file separator (i.e., '/') character from given path name.
   */
  private String stripLeadingFS( String pathName )
  {
    if (pathName.charAt(0) == FS_)
      return pathName.substring(1);
    else
      return pathName;
  }

  /**
   * Read jobcard & substitute jobname created in constructor.
   */
  private String readJobcard() throws MvsPkgError
  {
    // Jobname must be same as userid.  To ensure this, jobname = userid +
    // 1 character based on pkg number is generated in constructor and will
    // be inserted in the jobcard, regardless of present jobname.
    String         jobcardFile = MvsProperties.jobcardFile;
    StringBuffer   sb = new StringBuffer(160);
    BufferedReader br = null;

    try
    {
      String line;
      boolean firstLine = true;

      br = new BufferedReader( new FileReader(jobcardFile) );

      while ( (line=br.readLine()) != null)
      {
        if (firstLine)
        {
          // MAD-D2855 remove sequence numbers if they are present
          // sequence numbers are in col 73-80
          if (line.length() == 80)
          {
            // verify data in col 73-80 is numeric
            String seqno = line.substring(72);
            try
            {
              Integer i = new Integer(seqno);
              line = line.substring(0, 72).trim();
            }
            catch(NumberFormatException nfe)
            {
              // data in col 73-80 is not sequence numbers....
            }
          }

          // put in jobname of userid + 1 character
          int jobIndex = line.indexOf(" JOB");
          if (jobIndex < 0)
          {
            throw new MvsPkgError(MvsPkgError.invalidJobcard1,
                                  new Object[] {jobcardFile});
          }
          String newLine = "//" + jobName_ + line.substring(jobIndex) + NL_;

          // make sure we didn't make the line too long
          if (newLine.length() > 71)
          {
            int lastComma = newLine.lastIndexOf(',');
            while (lastComma > 70)
            {
              lastComma = newLine.lastIndexOf(',', lastComma - 1);
            }
            newLine = newLine.substring(0, lastComma + 1) + NL_ +
                      "// " + newLine.substring(lastComma + 1);
          }

          sb.append(newLine);
          firstLine = false;
        }
        else
        {
          sb.append(line).append(NL_);
        }
      }
    }
    catch (FileNotFoundException e)
    {
      throw new MvsPkgError(MvsPkgError.fileNotFound2,
                            new Object[] {"job card", jobcardFile});
    }
    catch (IOException e)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"reading", jobcardFile, e.getMessage()});
    }
    finally
    {
      // if we get an exception closing the file, log a message & ignore
      try
      {
        br.close();
      }
      catch(IOException e)
      {
        System.err.println("Warning: Caught IOException closing " + jobcardFile);
        System.err.println(e.getMessage());
      }
    }
    return sb.toString();
  } // readJobcard()


  /**
   * Format input to Rexx execs so that the line does not exceed column 80.
   * If a line is longer than 80 characters, it will be continued with a "+"
   * in column 80.
   */
  private String formatLine80( StringBuffer line )
  {
    int lineLength = line.length();

    if (lineLength <= 80) return line.toString();   // no continuation needed

    // number of continuations needed
    int continues = lineLength / 80;

    // reserve space for each "+\n" to be added
    line.ensureCapacity(lineLength + (continues * 2));

    for (int i=1; i<=continues; i++)
    {
      // x marks the spot to insert continuation
      int x = (80 * i) - 1;
      x += (i - 1);
      line.insert(x, "+\n");
    }

    return line.toString();

  } // formatLine80()

  /**
   * Format MCS ++IF statement as input to the DEFPKGS
   * Takes "<IF> fmid1,fmid2,fmid3" entry in pcd.mvs and returns the following:
   *  ++IF FMID(fmid1) THEN REQ(fmid2,fmid3)
   *
   * @exception MvsPkgError if less than two tokens are included in ifreq
   * @param ifreq unformatted ifreq value, i.e. "fmid1,fmid2"
   * @return formatted ++IF entry
   */
  private String formatIfReq( String ifreq ) throws MvsPkgError
  {
    int count = 0;
    StringTokenizer st1 = new StringTokenizer( ifreq, "," );
    if (st1.countTokens() < 2)
    {
      // Must have two and only two tokens in an ifreq
      throw new MvsPkgError(MvsPkgError.invalidIfReq,
                             new Object[] { ifreq });
    }
    String fmid1 = st1.nextToken();
    String fmid2 = st1.nextToken();

    String newIfReq = "++IF FMID(" + fmid1.trim();
    newIfReq += ") THEN REQ(" + fmid2;
    count = newIfReq.length();
    while (st1.hasMoreTokens())
    {
      newIfReq += ",";
      count++;
      fmid2 = st1.nextToken();
      // Add new line if going past column 72
      if (count + fmid2.length() >= 72)
      {
        count = 2;    // 2 space indent
        newIfReq += "\n  ";
      }
      count += fmid2.length();
      newIfReq += fmid2;
    }
    newIfReq += ")";

    return newIfReq;

  }

  /**
   * Format keyword entries to the DEFPKGS so they don't exceed column 72.
   *
   * @param keyword a SMP statement keyword for this entry
   * @param value value of the keyword
   * @param delim character to split the entry on if > 72 chars
   * @return properly formatted SMP entry
   */
  private String checkAndFormatSmp(String keyword, String value, String delim)
                                   throws MvsPkgError
  {
    String checkedValue = "";
    String prefix = "  ";
    String smpStatement = prefix + keyword + "(";
    int startColumn = prefix.length() + keyword.length() + 1;
    int lastColumnNum = 70;
    int virtualLineLength = lastColumnNum - prefix.length();
    int totalSpace;

    // stuffing spaces after prefix so as to align multiple lines below "("
    for (int tmpVar=0; tmpVar < keyword.length(); tmpVar++)
      prefix += " ";

    StringTokenizer st1 = new StringTokenizer(value, delim);

    // Checking the format of each token and retaining tokens of length of
    // only 7
    while (st1.hasMoreTokens())
    {
      String token = st1.nextToken();
      token = token.trim();
      if (token.length() == 7)
      {
        checkedValue += token;
        if (st1.countTokens() != 0)
          checkedValue += delim;
      }
    }

    totalSpace = checkedValue.length() + startColumn + 1;

    if (totalSpace >= lastColumnNum)
    {
      StringTokenizer st2 = new StringTokenizer(checkedValue, delim);
      int lineLength = prefix.length();
      while(st2.hasMoreTokens())
      {
        String token = st2.nextToken();
        if (token.length() >= virtualLineLength)
        {
          // The token is too long and can't be processed
          throw new MvsPkgError(MvsPkgError.invalidVerStatement,
                             new Object[] { keyword, value });
        }
        else if ((token.length() + lineLength) >= virtualLineLength)
        {
          smpStatement += "\n";
          smpStatement += prefix;
          smpStatement += token;
          lineLength = prefix.length() + token.length();
        }
        else
        {
          smpStatement += token;
          lineLength += token.length();
        }
        if (st2.countTokens() != 0)
        {
          smpStatement += delim;
          lineLength++;
        }
      }
    }
    else
    {
      smpStatement += checkedValue;
    }
    smpStatement += ")";
    return smpStatement;

  }
  
  /**
   * Generates JCL for creating a PDS with specified name and copying files
   * via FTP.  It returns MvsJobInfo that encapsulates this JCL   
   *
   * @param pdsLangName: PDS for source files (USERID.SANDBOX.SRC)
   * @param pdsLMacName: PDS for local macros (USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacName: PDS for export macros(USERID.SANDBOX.MACEXPT)
   * @param pdsObject: PDS for object         (USERID.SANDBOX.O)
   * @param pdsListing: PDS for listing files (USERID.SANDBOX.LISTING)
   * @param pdsClrPrnt: PDS for PLS/PLS dependency (USERID.SANDBOX.CLRPRINT) 
   * @param pdsUdata: PDS for for ASM dependency   (USERID.SANDBOX.UNIDATA)
   * @param srcFileName: Source Files (test) 
   * @param srcFilePath: Full source names (/u/userid/sandbox/src/plx/test.plx)
   * @param lmacFiles: Local macros (/u/userid/sandbox/src/plx/a.mac)
   * @param emacFiles: Export macros(/u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @param numOfParts: Total number of source files to compile 
   * @param bldLstDir Directory to store generated JCL files (/u/userid/sandbox/obj/mvs390_oe_2/plx)
   *
   */
  public MvsJobInfo generateTsoBldGather(String pdsLangName, String pdsLMacName,
                                         String pdsEMacName, String pdsObject, String pdsListing,
                                         String pdsClrPrnt, String pdsUdata, String srcFileName[],
                                         String srcFilePaths[], String lmacFiles[], String emacFiles[],
                                         int numOfParts, String bldLstDir,
                                         boolean isAnyFileTooLong) throws MvsPkgError, IOException 
  {     
    // generate JCL filename as "objectDir/copy2tso.jcl"
    String jclFileName = bldLstDir + "copy2tso.jcl";

    System.out.println("-----------------------------------------");
    System.out.println("Generating Allocation and FTP File jobstream");
    
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter( new FileWriter(jclFileName) );
      
      bw.write(readJobcard());     
      
      long start, delta;

      start = System.currentTimeMillis();      
      writeAllocStep(pdsLangName, pdsLMacName, pdsEMacName,
                     pdsObject, pdsListing, pdsClrPrnt, pdsUdata, bw); 
 
      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated allocation step in " + delta + " ms");

      start = System.currentTimeMillis();
      writeGatherStep(pdsLangName, pdsLMacName, pdsEMacName,
                      bw, srcFilePaths, lmacFiles, emacFiles,
                      isAnyFileTooLong);               

      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated ftp file step in " + delta + " ms");

    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName,
                              ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                            ex.getMessage()});
      }
    }
    return jobInfo_;
  }  //generateTsoBldGather()

  /**
   * Generates JCL for compiling on TSO and returns MvsJobInfo that
   * encapsulates this JCL      
   *
   * @param compileCnt: The number assigned to the compile jcl. 
   * @param pdsLangName: PDS for source files (i.e. USERID.SANDBOX.SRC) 
   * @param pdsLMacName: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacName: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObject:   PDS for object files (i.e. USERID.SANDBOX.O) 
   * @param pdsListing:  PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrnt:  PDS for PLS/PLX dependencies (i.e. USERID.SANDBOX.CLRPRINT)
   * @param pdsUdata:    PDS for ASM dependencies (i.e. USERID.SANDBOX.UNIDATA) 
   * @param parts: Source Files (test) 
   * @param srcFilePaths: Full source names (/u/userid/sandbox/src/plx/test.plx)
   * @param startIndex:  Find the first src file in the array
   * @param lastIndex:   Find the last src file in the array
   * @param oneCompileStepNeeded:  (true or false)
   * @param asmFound: Found ASM parts (true or false)
   * @param cblFound: Found COBOL parts (true or false)
   * @param plsFound: Found PLS parts: (true or false)
   * @param plxFound: Found PLX parts: (true or false)   
   * @param lmacFiles: Local macros (i.e. /u/userid/sandbox/src/plx/a.mac)
   * @param emacFiles: Export macros(i.e. /u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @param numOfParts: Total number of source files to compile 
   * @param bldLstDir: Directory to store generated JCL files (i.e. /u/userid/sandbox/obj/mvs390_oe_2/plx)
   *
   */
  public MvsJobInfo generateTsoBldCompile( int compileCnt, String pdsLang, String pdsLMac, String pdsEMac,
                                           String pdsO, String pdsList, String pdsClrPrt, String pdsUdata,
                                           String parts[], String srcFilePaths[], int startIndex, int lastIndex,
                                           boolean oneCompileStepNeeded, boolean asmFound, boolean cblFound,
                                           boolean plsFound, boolean plxFound,
                                           String bldLstDir) throws MvsPkgError, IOException
  {
    // generate compile JCL filename  
    String jclFileName = bldLstDir +"compile" + compileCnt +".jcl";
 
    System.out.println("Generating Compile jobstream");
    
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {

      bw = new BufferedWriter(new FileWriter(jclFileName));
      writeJobCardForCompile(bw, asmFound, cblFound, 
                             plsFound, plxFound); 
      
      long start, delta;

      start = System.currentTimeMillis();

      writeCompileStep(pdsLang, pdsLMac, pdsEMac, pdsO, pdsList, pdsClrPrt,
                       pdsUdata, parts, srcFilePaths, bw, startIndex, lastIndex,
                       oneCompileStepNeeded); 

      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated compile step(s) in " + delta + " ms");

    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName, ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                            ex.getMessage()});
      }
    }
    return jobInfo_;
  }  //generateTSOBldCompile()

  /**
   * Generates JCL to copy the PDS members to HFS and deletes the temp PDS on TSO.
   * It returns MvsJobInfo that encapsulates this JCL      
   *
   * @param compileCnt: The number assigned to the compile jcl. 
   * @param pdsLang: PDS for source files (i.e. USERID.SANDBOX.SRC) 
   * @param pdsLMac: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMac: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObj:   PDS for object files (i.e. USERID.SANDBOX.O) 
   * @param pdsLst:  PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrnt:  PDS for PLS/PLX dependencies (i.e. USERID.SANDBOX.CLRPRINT)
   * @param pdsUdata:    PDS for ASM dependencies (i.e. USERID.SANDBOX.UNIDATA) 
   * @param srcFileName: Source Files (i.e. test) 
   * @param srcFilePaths: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)
   * @param startIndex:  Points to the first src file in the array
   * @param lastIndex:   Points to the last src file in the array
   * @param oneCompileStepNeeded:  (true or false)
   * @param asmFound: Found ASM parts (true or false)
   * @param cblFound: Found COBOL parts (true or false)
   * @param plsFound: Found PLS parts: (true or false)
   * @parma plxFound: Found PLX parts: (true or false)   
   * @param lmacFiles: Local macros (i.e. /u/userid/sandbox/src/plx/a.mac)
   * @param emacFiles: Export macros(i.e. /u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @param numOfParts: Total number of source files to compile 
   * @param bldLstDir: Directory to store generated JCL files (i.e. /u/userid/sandbox/obj/mvs390_oe_2/plx)   
   *
   */
  public MvsJobInfo generateTsoBldCopyToHFS( String pdsLang, String pdsLMac, String pdsEMac,
                                             String pdsObj, String pdsLst, String pdsClrPrnt,
                                             String pdsUdata, String srcFileName[], 
                                             String srcFilePaths[], String bldLstDir) throws MvsPkgError, IOException 
  {
    // generate copy JCL filename 
    String jclFileName = bldLstDir + "copy2hfs.jcl";

    System.out.println("Generating FTP File jobstream ");
    
    System.out.println(jclFileName);
    System.out.println("  Job name is " + jobName_);

    jobInfo_ = new MvsJobInfo(jclFileName, jobName_);
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(jclFileName));
      
      bw.write(readJobcard());     
      
      long start, delta;

      start = System.currentTimeMillis();

      writeMvsCopyStep(pdsObj, pdsLst, pdsClrPrnt, pdsUdata, srcFileName, 
                       srcFilePaths, bw);   

      delta = System.currentTimeMillis() - start;
      System.out.println("  Generated ftp file step in " + delta + " ms");

      writeDeleteStep(pdsLang, pdsLMac, pdsEMac, pdsObj, pdsLst,
                      pdsClrPrnt, pdsUdata, bw);   

    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"writing", jclFileName, ex.getMessage()});
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException ex)
      {
        throw new MvsPkgError(MvsPkgError.ioException3,
                              new Object[] {"closing", jclFileName,
                                               ex.getMessage()});
      }
    }
    return jobInfo_;
  }  //generateTSOBldCopyToHFS()

  /**
   * Write JobCard for allocation and copy JCL.
   *
   * @param bw BufferedWriter object that represents the jcl file
   *   
   */
  private void writeJobCardData(BufferedWriter bw) throws MvsPkgError, IOException
  {
     bw.write(readJobcard());     
  }

  /**
   * Write JobCard information for the compile JCL.
   *
   * @param bw BufferedWriter object that represents the jcl file
   *
   * @param asmFilesExist: Found ASM parts: (true or false)
   * @param cblFilesExist: Found COBOL parts: (true or false)   
   * @param plsFilesExist: Found PLS parts: (true or false)
   * @param plxFilesExist: Found PLX parts: (true or false)      
   */
  private void writeJobCardForCompile(BufferedWriter bw, boolean asmExist,
                                      boolean cblExist, boolean plsExist,
                                      boolean plxExist) 
                                      throws MvsPkgError, IOException 
  {
    bw.write(readJobcard());

    if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
    {
      setCompileValues(bw, asmExist, cblExist, plsExist, plxExist);
    }

  }
  
  /**
   * Write the ALLOCLIB step information to the JCL.
   *
   * @param pdsLang: PDS for source files (i.e. USERID.SANDBOX.SRC) 
   * @param pdsLMac: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMac: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObject:   PDS for object files (i.e. USERID.SANDBOX.O) 
   * @param pdsListing:  PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrnt:  PDS for PLS/PLX dependencies (i.e. USERID.SANDBOX.CLRPRINT)
   * @param pdsUdata:    PDS for ASM dependencies (i.e. USERID.SANDBOX.UNIDATA) 
   * @param bw BufferedWriter object that represents the jcl file   
   *   
   */
  private void writeAllocStep(String pdsLang, String pdsLMac, String pdsEMac,
                              String pdsObject, String pdsListing, String pdsClrPrt,
                              String pdsUdata, BufferedWriter bw) throws MvsPkgError, IOException 
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Allocate distribution libraries"); bw.newLine();
    bw.write(commentLine_); bw.newLine();
    
    if (MvsProperties.useRexxScript.equalsIgnoreCase("NO")) 
    {
      bw.write( genAllocStepInJcl("ALLOCLIB", null, pdsLang, pdsLMac, pdsEMac,
                                  pdsObject, pdsListing, pdsClrPrt, pdsUdata) );
    }
    else
    {
      bw.write( genRexxAllocStepInJcl("ALLOCLIB", null, null) );
   
      String pdsName  = null;        
      String spaceVal = null;
      String primQty;
      String secondQty;
      String dirBlocks;
      String recFormat;
      String recLength;
      String blkSize;     
      boolean isListing = false;
      boolean isClrPrnt = false;
      int allocCount = 7;
   
      for (int i=1; i<=allocCount; i++)    
      {      
        if ( i == 1)
          pdsName = pdsLang;           
        else if (i == 2) 
          pdsName = pdsLMac;           
        else if (i == 3) 
          pdsName = pdsEMac;           
        else if (i == 4) 
          pdsName = pdsObject;           
        else if (i == 5) 
        {
          pdsName = pdsListing;
          isListing = true;
        }
        else if (i == 6) 
        {
          pdsName = pdsClrPrt;
          isClrPrnt = true;
        }
        else if (i == 7) 
          pdsName = pdsUdata;
        
        if (isClrPrnt)
        {        
          // record format
          recFormat = "FBA";
          // record length
          recLength = "133";
  
          // space type
          if (MvsProperties.spaceType != null)
          {
            if (MvsProperties.spaceType.equalsIgnoreCase("TRACK") ||
                MvsProperties.spaceType.equalsIgnoreCase("TRK"))
            {
              spaceVal = "TRACK";
            }
            else
              spaceVal = "CYLINDER";
          }
  
          // primary tracks or cylinder
          if (MvsProperties.primaryQty != null)
            primQty = MvsProperties.primaryQty;
          else
            primQty = "34";
  
          // secondary tracks or cylinder
          if (MvsProperties.secondaryQty != null) 
            secondQty = MvsProperties.secondaryQty;
          else
            secondQty = "4";
  
          // directory blocks 
          if (MvsProperties.directoryBlks != null) 
            dirBlocks = MvsProperties.directoryBlks;
          else
            dirBlocks = "30";
  
          // block size
          blkSize = "32718";
  
          isClrPrnt = false;
        }
        else if (isListing)
        {
          // record format
          recFormat = "VBA";
          // record length
          recLength = "137";
  
          // space type
          if (MvsProperties.lstSpaceType != null)
          {
            if (MvsProperties.lstSpaceType.equalsIgnoreCase("TRACK") ||
                MvsProperties.lstSpaceType.equalsIgnoreCase("TRK"))
            {
              spaceVal = "TRACK";
            }
            else
              spaceVal = "CYLINDER";
          }
  
          // primary tracks or cylinder
          if (MvsProperties.lstPrimaryQty != null)
            primQty = MvsProperties.lstPrimaryQty;
          else
            primQty = "81";
  
          // secondary tracks or cylinder
          if (MvsProperties.lstSecondQty != null) 
            secondQty = MvsProperties.lstSecondQty;
          else
            secondQty = "9";
  
          // directory blocks 
          if (MvsProperties.lstDirBlks != null) 
            dirBlocks = MvsProperties.lstDirBlks;
          else
            dirBlocks = "10";
  
          // block size
          if (MvsProperties.lstBlockSize != null) 
            blkSize = MvsProperties.lstBlockSize;
          else
            blkSize = "13030";
  
          isListing = false;
        }
        else
        {
          // record format
          recFormat = "FB";
          // record length
          recLength = "80";
          
          // space type
          if (MvsProperties.spaceType != null)        
          {
            if (MvsProperties.spaceType.equalsIgnoreCase("CYLINDER") ||
                MvsProperties.spaceType.equalsIgnoreCase("CYL"))
            {
              spaceVal = "CYLINDER";
            }
            else
              spaceVal = "TRACK";
          }
  
          // primary tracks or cylinder
          if (MvsProperties.primaryQty != null)
            primQty = MvsProperties.primaryQty;
          else
            primQty = "34";
  
          // secondary tracks or cylinder
          if (MvsProperties.secondaryQty != null) 
            secondQty = MvsProperties.secondaryQty;
          else
            secondQty = "4";
  
          // directory blocks 
          if (MvsProperties.directoryBlks != null) 
            dirBlocks = MvsProperties.directoryBlks;
          else
            dirBlocks = "30";
  
          // block size
          if (MvsProperties.blockSize != null) 
            blkSize = MvsProperties.blockSize;
          else
            blkSize = "32720";
        }
    
        bw.write("  " + pdsName);      
        bw.write(" " + spaceVal + " " + primQty +
                 " " + secondQty + " "+ dirBlocks);      
        bw.write(" " + recFormat + " "+ recLength + " " + blkSize);
        bw.newLine();      
      }   // end loop

    }

    bw.write("/*"); bw.newLine();

    jobInfo_.addStep("ALLOCLIB", 0, "Allocate distribution libraries");

  }

  /**
   * Write the FTPFILE step information to the JCL.
   *
   * @param srcPds: PDS for source files (i.e. USERID.SANDBOX.SRC) 
   * @param lmacPds: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param emacPds: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)   
   * @param bw BufferedWriter object that represents the jcl file
   * @param srcPathArray: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)
   * @param lmacArray: Local macros (i.e. /u/userid/sandbox/src/plx/a.mac)
   * @param emacArray: Export macros(i.e. /u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)      
   *      
   */
  private void writeGatherStep(String srcPds, String lmacPds, String emacPds,
                               BufferedWriter bw, String srcPathArray[],
                               String lmacArray[], String emacArray[],
                               boolean anyFileTooLong) throws IOException
  { 
    int srcTaskNum    = 1;
    int localTaskNum  = 2;
    int exportTaskNum = 3;

    bw.write(commentLine_); bw.newLine();
    bw.write("//* Ftp File step to copy HFS files to PO data sets");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write(genFtpTransfer("FTPFILE", null, "RC=0") );        
    
    bw.write("ASCII"); bw.newLine();    
    writeFileData(srcPathArray, srcPds, bw, srcTaskNum, anyFileTooLong);
    writeFileData(lmacArray, lmacPds, bw, localTaskNum, anyFileTooLong);
    writeFileData(emacArray, emacPds, bw, exportTaskNum, anyFileTooLong);

    bw.write("QUIT"); bw.newLine();           
    bw.write("/*"); bw.newLine();
    bw.write("//         ENDIF"); bw.newLine();

    jobInfo_.addStep("FTPFILE", 0, "Copy HFS files to PDS ");

  }

  /**
   * converts a vector into a string array
   */
  private String[] vector2StringArray( Vector vector )
  {
    String result[] = new String[vector.size()];
    Enumeration enumeration = vector.elements();
    for (int idx = 0; enumeration.hasMoreElements(); idx++)
    {
      result[idx] = (String)enumeration.nextElement();
    }
    return result;
  }

  /**
   * Write the information to copy the HFS files to PDS.
   *
   * @param parts: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)      
   * @param pds: PDS for source, local macro or export macro (i.e. USERID.SANDBOX.SRC)      
   * @param bw BufferedWriter object that represents the jcl file
   * @param taskNum: Tells which type files are being written
   *          1 - Source Files 2 - Local Macros 3 - Export Macros      
   *
   */
  private void writeFileData(String parts[], String pds, BufferedWriter bw,
                             int taskNum, boolean aFileIsTooLong)  throws IOException
  {
    try
    {
      String dirsep = System.getProperty("file.separator");

      if (parts.length > 0) 
      {
        if (aFileIsTooLong)        
        {
          if (taskNum == 1 || taskNum == 3)
          {
            // create the local change directory line
            int endSlash = parts[0].lastIndexOf(dirsep);  
            String checkLine = parts[0].substring(0, endSlash + 1);
    
            bw.write(formatChngDirLine70(checkLine));
            bw.newLine();    
          }
        }
      }

      for (int i=0;i<parts.length;i++) 
      {
        StringBuffer line = new StringBuffer(80);     

        if (aFileIsTooLong)
        {
          int lastSlash = parts[i].lastIndexOf(dirsep);  
          String fileName = parts[i].substring(lastSlash + 1);          

          if (differentMachines_)    
             line.append("get ").append(fileName.toLowerCase());                             
          else                        
             line.append("put ").append(fileName.toLowerCase());

          line.append(" "); 
          line.append("'" + pds);
          line.append("(").append(createSrcData(parts[i])).append(")'");
          
          bw.write(line.toString());        
          bw.newLine();
        }
        else
        {
          StringBuffer line2 = new StringBuffer(80);     

          if (differentMachines_)    
            line.append("get ").append(parts[i]);                   
          else
            line.append("put ").append(parts[i]);
         
          line.append(" +");
          bw.write(line.toString());        
          bw.newLine();
  
          line2.append("'" + pds);
          line2.append("(").append(createSrcData(parts[i])).append(")'");
          bw.write(line2.toString());                
          bw.newLine();            
        }
        
      }
      bw.flush();
    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing ");
    }
    return;
  }

  /**
   * Set compile values for the compile JCL.
   *
   * @param bw BufferedWriter object that represents the jcl file
   *
   * @param asmFilesExist: Found ASM parts: (true or false)
   * @param cblFilesExist: Found COBOL parts: (true or false)   
   * @param plsFilesExist: Found PLS parts: (true or false)
   * @param plxFilesExist: Found PLX parts: (true or false)      
   */
  private void setCompileValues(BufferedWriter bw, boolean asmFilesExist, 
                                boolean cblFilesExist, boolean plsFilesExist,
                                boolean plxFilesExist) throws MvsPkgError, IOException
  {

    boolean asmLangSetByUser;
    boolean cblLangSetByUser;
    boolean plsLangSetByUser;
    boolean plxLangSetByUser;

    if (MvsProperties.useMyTemplates.equalsIgnoreCase("NO")) 
    {
      asmLangSetByUser = false;
      cblLangSetByUser = false;
      plsLangSetByUser = false;
      plxLangSetByUser = false;
    }
    else
    {
      String asmType = "ASM";
      String cblType = "COBOL";
      String plsType = "PLS";
      String plxType = "PLX";

      asmLangSetByUser = isLanguageFound(asmType);
      cblLangSetByUser = isLanguageFound(cblType);
      plsLangSetByUser = isLanguageFound(plsType);
      plxLangSetByUser = isLanguageFound(plxType);
    }

    if (asmFilesExist)
    {
      if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
           (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
            !asmLangSetByUser) )            
      {
        String asmFlagStr = ("// ASMFLAGS=('" + MvsProperties.asmFlags + ",'),");
           
        bw.write("//SP1 SET ASMPGM=" + MvsProperties.asmProgram + ",");  bw.newLine();       
        bw.write("//    REGION=" + MvsProperties.region + ",");  bw.newLine();        
        
        bw.write(formatCFlagsLine(asmFlagStr)); bw.newLine();
        
        if ( (cblFilesExist && !cblLangSetByUser) || 
             (plsFilesExist && !plsLangSetByUser) || 
             (plxFilesExist && !plxLangSetByUser) ) 
        {
           bw.write("//    STEPALIB=DSN=" + MvsProperties.asmStepLib + ",");  
           bw.newLine();
        }
        else
        {
           bw.write("//    STEPALIB=DSN=" + MvsProperties.asmStepLib);
           bw.newLine();
        }
      }
    }
    
    if (cblFilesExist)
    {
      if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
           (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
            !cblLangSetByUser) )                        
      {
        String cblFlagStr = ("// CBLFLAGS=('" + MvsProperties.cblFlags + ",'),");

        if (asmFilesExist && !asmLangSetByUser)
        {  
           bw.write("//    CBLPGM=" + MvsProperties.cblProgram + ",");
           bw.newLine();
        }
        else
        {
           bw.write("//SP1 SET CBLPGM=" + MvsProperties.cblProgram + ","); 
           bw.newLine();       
           bw.write("//    REGION=" + MvsProperties.region + ",");  
           bw.newLine();
        }

        bw.write(formatCFlagsLine(cblFlagStr)); bw.newLine();

        if ( (plsFilesExist && !plsLangSetByUser) || 
             (plxFilesExist && !plxLangSetByUser) )
        {
           bw.write("//    STEPCLIB=DSN=" + MvsProperties.cblStepLib + ",");
           bw.newLine();
        }
        else
        {       
           bw.write("//    STEPCLIB=DSN=" + MvsProperties.cblStepLib);
           bw.newLine();
        }
      }
    }
        
    if (plsFilesExist)
    {
      if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
           (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
            !plsLangSetByUser) )                        
      {
        String plsFlagStr = ("// PLSFLAGS=('" + MvsProperties.plsFlags + ",'),");
        
        if ( (asmFilesExist && !asmLangSetByUser) ||
             (cblFilesExist && !cblLangSetByUser))
        {
        
           bw.write("//    PLSPGM=" + MvsProperties.plsProgram + ",");
           bw.newLine();
        }
        else
        {
           bw.write("//SP1 SET PLSPGM=" + MvsProperties.plsProgram + ",");
           bw.newLine();           
           bw.write("//    REGION=" + MvsProperties.region + ",");
           bw.newLine();
        }
        
        bw.write(formatCFlagsLine(plsFlagStr)); bw.newLine();            
        
        if (plxFilesExist && !plxLangSetByUser)
        {        
           bw.write("//    STEPSLIB=DSN=" + MvsProperties.plsStepLib + ",");
           bw.newLine();
        }
        else
        {        
           bw.write("//    STEPSLIB=DSN=" + MvsProperties.plsStepLib);
           bw.newLine();
        }
      }
    }
    
    if (plxFilesExist)
    {
      if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
           (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
            !plxLangSetByUser) )                        
      {
        String plxFlagStr = ("// PLXFLAGS=('" + MvsProperties.plxFlags + ",'),");

        if ( (asmFilesExist && !asmLangSetByUser) || 
             (cblFilesExist && !cblLangSetByUser) || 
             (plsFilesExist && !plsLangSetByUser) ) 
        {
           bw.write("//    PLXPGM=" + MvsProperties.plxProgram + ",");
           bw.newLine();
        }
        else
        {
           bw.write("//SP1 SET PLXPGM=" + MvsProperties.plxProgram + ",");
           bw.newLine();
           bw.write("//    REGION=" + MvsProperties.region + ",");
           bw.newLine();
        }

        bw.write(formatCFlagsLine(plxFlagStr)); bw.newLine();
        bw.write("//    STEPXLIB=DSN=" + MvsProperties.plxStepLib);  bw.newLine();
      }
    }
  }


  /**
   * Write the compile step information for the JCL.
   *
   * @param pdsLang: PDS for source files (i.e. USERID.SANDBOX.SRC) 
   * @param pdsLMac: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMac: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObj:   PDS for object files (i.e. USERID.SANDBOX.O) 
   * @param pdsLst:  PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrnt:  PDS for PLS/PLX dependencies (i.e. USERID.SANDBOX.CLRPRINT)
   * @param pdsUni:    PDS for ASM dependencies (i.e. USERID.SANDBOX.UNIDATA) 
   * @param parts: Source Files (i.e. test) 
   * @param srcFileArray: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)
   * @param bw BufferedWriter object that represents the jcl file   
   * @param startIndex:  Points to the first src file in the array
   * @param lastIndex:   Points to the last src file in the array
   * @param oneCompileJob:  (true or false)
   *
   */
  private void writeCompileStep( String pdsLang, String pdsLMac,  String pdsEMac,
                                 String pdsObj, String pdsLst,  String pdsClrPrnt,
                                 String pdsUni, String parts[], String srcPathArray[],
                                 BufferedWriter bw, int startIndex, int lastIndex,
                                 boolean oneCompileJob)
  {
    try
    {
      String fileType = null;
      String comment0    = "//*---------------------------------------------";

      bw.write(comment0); bw.newLine();
      bw.write("//* Execute the compile process ");  bw.newLine();
      bw.write(comment0); bw.newLine();       

      if (oneCompileJob) 
      {
        for (int i=0;i<srcPathArray.length;i++) 
        {
          if (srcPathArray[i].endsWith(".asm"))
             fileType = "asm";
          else if (srcPathArray[i].endsWith(".cbl") ||
                   srcPathArray[i].endsWith(".cob") ||
                   srcPathArray[i].endsWith(".cobol"))
          {
             fileType = "cobol";
          }
          else if (srcPathArray[i].endsWith(".pls"))
             fileType = "pls";
          else if (srcPathArray[i].endsWith(".plx"))
             fileType = "plx";
                        

          readJCLTemplateFile(pdsLang, pdsLMac, pdsEMac,
                              pdsObj, pdsClrPrnt, pdsLst,
                              pdsUni, parts[i], i, fileType,
                              bw);

          jobInfo_.addStep(parts[i].toUpperCase(), 4, "Compile process");
        }
      }
      else
      {
        for (int i=startIndex;i<=lastIndex;i++) 
        {
           if (srcPathArray[i].endsWith(".asm"))
              fileType = "asm";
           else if (srcPathArray[i].endsWith(".cbl") ||
                    srcPathArray[i].endsWith(".cob") ||
                    srcPathArray[i].endsWith(".cobol"))
           {
              fileType = "cobol";
           }
           else if (srcPathArray[i].endsWith(".pls"))
              fileType = "pls";           
           else if (srcPathArray[i].endsWith(".plx"))
              fileType = "plx";
           

           readJCLTemplateFile(pdsLang, pdsLMac, pdsEMac,
                               pdsObj, pdsClrPrnt, pdsLst,
                               pdsUni, parts[i], i, fileType,
                               bw);

           jobInfo_.addStep(parts[i].toUpperCase(), 4, "Compile process");
        }
      }
    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing to file" + "\n");
    }
  }

  /**
   * Write the FTPFILE step information to the JCL.
   *
   * @param objPds: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param lstPds: PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param clrPrtPds: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param unidataPds: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param srcParts: Source Files (i.e. test) 
   * @param srcPathArray: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)
   * @param bw BufferedWriter object that represents the jcl file      
   *   
   */
  private void writeMvsCopyStep(String objPds, String lstPds, String clrPrtPds, String unidataPds, 
                               String srcParts[], String srcPathArray[], BufferedWriter bw) throws IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Ftp File step to copy PO data sets to HFS files");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write(genFtpTransfer("FTPFILE", null, null) );        
    writeCopyFileData(objPds, lstPds, clrPrtPds, unidataPds, srcParts, srcPathArray, bw);

    bw.write("QUIT"); bw.newLine();           
    bw.write("/*"); bw.newLine();

    jobInfo_.addStep("FTPFILE", 0, "Copy PDS members to HFS files");
  }

  /**
   * Write the information to copy the PDS members to HFS.
   *
   * @param pdsLangName: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMacName: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacName: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsO: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsLst: PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrt: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsUdata: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param parts: Source Files (i.e. test) 
   * @param partsPath: Full source names (i.e. /u/userid/sandbox/src/plx/test.plx)
   * @param bw BufferedWriter object that represents the jcl file      
   *      
   */
  private void writeCopyFileData(String pdsO, String pdsLst, String pdsClrPrt,
                                 String pdsUdata, String parts[], String partsPath[],
                                 BufferedWriter bw)  throws IOException
  {
    try
    {
      boolean isASMFile = false;
      boolean isCOBOLFile = false;
      boolean isFRTRNFile = false;
      boolean lineTooLong = false;

      String oExt    = ".o";
      String lstExt  = ".lst";
      String tmpExt  = ".tmp";

      String hfsObjName = null;
      String hfsLstName = null;
      String hfsTmpName = null;

      for (int i=0;i<parts.length;i++) 
      {
        
        if (partsPath[i].endsWith(".asm"))
        {
          isASMFile = true;
        }
        else if (partsPath[i].endsWith(".cbl") ||
                 partsPath[i].endsWith(".cob") ||
                 partsPath[i].endsWith(".cobol")) 
        {
          isCOBOLFile = true;
        }
        else if (partsPath[i].endsWith(".cbl") ||
                 partsPath[i].endsWith(".cob") ||
                 partsPath[i].endsWith(".cobol")) 
        {
          isFRTRNFile = true;
        }

        StringBuffer line = new StringBuffer(80);     
        StringBuffer line2 = new StringBuffer(80);     
        StringBuffer line3 = new StringBuffer(80);     

        String copyFile =  parts[i];

        hfsObjName = MvsProperties.objectDir + copyFile + oExt;
        hfsLstName = MvsProperties.objectDir + copyFile + lstExt;
        hfsTmpName = MvsProperties.objectDir + copyFile + tmpExt;
        
        String objDirLine = MvsProperties.objectDir;        

        bw.write("BINARY");
        bw.newLine();

        if (!lineTooLong) 
        {
          String tempStr = copyFile + lstExt;

          int strLength = objDirLine.length() + tempStr.length();

          if (strLength > 71) 
          {
            bw.write(formatChngDirLine70(objDirLine));
            bw.newLine();
              
            lineTooLong = true;
          }
        }
        
        if (lineTooLong)
        {
          if (differentMachines_)    
            line.append("put '");                
          else
            line.append("GET '");

          line.append(pdsO);                                  
          line.append("(").append(copyFile.toUpperCase()).append(")'");
          line.append(" ");                  
          line.append(copyFile.toLowerCase()).append(oExt);          
          if (!differentMachines_)                
             line.append(" (REPLACE");                                     
          bw.write(line.toString());
          bw.newLine(); 
  
          bw.write("ASCII");
          bw.newLine();

          if (differentMachines_)    
             line2.append("put '");        
          else
             line2.append("GET '");

          line2.append(pdsLst);                                  
          line2.append("(").append(copyFile.toUpperCase()).append(")'");        
          line2.append(" ");                  
          line2.append(copyFile.toLowerCase()).append(lstExt);
          if (!differentMachines_)    
             line2.append(" (REPLACE");                                     
          bw.write(line2.toString());
          bw.newLine();                
  
          if (differentMachines_)
             line3.append("put '");
          else
             line3.append("GET '");

          if (isASMFile)
          {
            line3.append(pdsUdata);
            isASMFile = false;
          }
          else
            line3.append(pdsClrPrt);
    
          // Don't generate .tmp files for COBOL 
          // Include listing is not available for dependency
          // generation
          if (!isCOBOLFile) 
          {
            line3.append("(").append(copyFile.toUpperCase()).append(")'");        
            line3.append(" ");                  
            line3.append(copyFile.toLowerCase()).append(tmpExt);
            if (!differentMachines_)    
               line3.append(" (REPLACE");                                     
            bw.write(line3.toString());
            bw.newLine(); 
          }

        }
        else
        {
          // Continuation on Multiple Lines
          StringBuffer line1a = new StringBuffer(80);             
          StringBuffer line1b = new StringBuffer(80);             
          StringBuffer line2a = new StringBuffer(80);     
          StringBuffer line2b = new StringBuffer(80);     
          StringBuffer line3a = new StringBuffer(80);     
          StringBuffer line3b = new StringBuffer(80);     

          if (differentMachines_) 
            line.append("put '");                
          else
            line.append("GET '");
                            
          line.append(pdsO);                                  
          line.append("(").append(copyFile.toUpperCase()).append(")'");
          line.append(" +");        
          bw.write(line.toString());          
          bw.newLine();

          line1a.append(hfsObjName).append(" +");                           
          bw.write(line1a.toString());
          bw.newLine();
          if (!differentMachines_)           
             line1b.append(" (REPLACE");                           
          bw.write(line1b.toString());
          bw.newLine();
          
          bw.write("ASCII");
          bw.newLine();
          
          if (differentMachines_) 
            line2.append("put '");                
          else
             line2.append("GET '");
                     
          line2.append(pdsLst);                                  
          line2.append("(").append(copyFile.toUpperCase()).append(")'");        
          line2.append(" +");                                   
          bw.write(line2.toString());
          bw.newLine();

          line2a.append(hfsLstName).append(" +");        
          bw.write(line2a.toString());
          bw.newLine();
          if (!differentMachines_)                     
             line2b.append(" (REPLACE");                           
          bw.write(line2b.toString());
          bw.newLine();

          // Don't generate .tmp files for COBOL
          // Include listing is not available for dependency
          // generation
          if (!isCOBOLFile) 
          {
            if (differentMachines_) 
              line3.append("put '");                
            else
              line3.append("GET '");
              
            if (isASMFile)
            {
              line3.append(pdsUdata);
              isASMFile = false;
            }
            else
              line3.append(pdsClrPrt);
    
            line3.append("(").append(copyFile.toUpperCase()).append(")'");        
            line3.append(" +");
            bw.write(line3.toString());
            bw.newLine();
  
            line3a.append(hfsTmpName).append(" +");                   
            bw.write(line3a.toString());
            bw.newLine();
            if (!differentMachines_)                     
               line3b.append(" (REPLACE");                           
            bw.write(line3b.toString());
            bw.newLine(); 
          }

        }  // isLineTooLong 

        isCOBOLFile = false;
        isFRTRNFile = false;
      } // for loop
      bw.flush();
    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing ");
    }
    return;
  }

  /**
   * Write the step to delete the temporary PDS.
   *
   * @param pdsSource: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMac: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMac: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObject: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsListing: PDS for listing files (i.e. USERID.SANDBOX.LISTING)
   * @param pdsClrPrt: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsUnidata: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param bw BufferedWriter object that represents the jcl file      
   *         
   */
  private void writeDeleteStep(String pdsSource, String pdsLMac, String pdsEMac, 
                               String pdsObject, String pdsListing, String pdsClrPrt,
                               String pdsUnidata, BufferedWriter bw) throws IOException
  {
    bw.write(commentLine_); bw.newLine();
    bw.write("//* Delete Temporary PDS ");
    bw.newLine();
    bw.write(commentLine_); bw.newLine();
    bw.write(deletePds(pdsSource, pdsLMac, pdsEMac, pdsObject,
                       pdsListing, pdsClrPrt, pdsUnidata) );

    bw.write("/*"); bw.newLine();

    jobInfo_.addStep("DELPDS", 0, "Delete Temporary PDS ");
  }

  /**
   * Get the source name from input line
   *
   * @param line: Input line (i.e. /u/userid/sandbox/src/plx/test.plx)  
   *
   * @return  fileName  (i.e. test)       
   */
  private static String createSrcData( String line )
  {
    // Create the file the string.
    String fileName = "";

    String dirsep = System.getProperty("file.separator");

    int val1 = line.lastIndexOf( dirsep );
    int val2 = line.lastIndexOf( "." );
    
    String srcfile = line.substring( (val1+1), val2 );

    if (srcfile != null)
      fileName = srcfile.toUpperCase();

    return fileName;
  }
  
  /**
   * Generate JCL for a step to run the specified Rexx exec via IKJEFT01.
   *
   * @param rexxExecName the Rexx exec to be run
   * @param stepName the step name or null, in which case the step name will
   *                 be the rexx exec name
   * @param condRc if not null, a JCL IF statement will be generated with the
   *               specified condition
   *
   */
  private String genRexxAllocStepInJcl(String rexxExecName,
                                       String stepName,
                                       String condRc)
  {
    String ifStmt1    = "//         IF (";
    String ifStmt2    = ") THEN";
    String execStmt   = "EXEC PGM=IKJEFT01,DYNAMNBR=25,PARM='";              
    String execStmt2  = "EXEC PGM=IKJEFT01";       
    String sysexecDD1 = "//SYSEXEC  DD DISP=SHR,DSN=";
    String sysexecDD2 = "//         DD DISP=SHR,DSN=SYS1.SBPXEXEC";
    String systsprtDD = "//SYSTSPRT DD SYSOUT=*";
    String sysprintDD = "//SYSPRINT DD SYSOUT=*";
    String systsinDD  = "//SYSTSIN  DD DUMMY";
    String inputDD    = "//INDD     DD *";

    // pad stepName to 9 characters
    StringBuffer stepNameSB = new StringBuffer(9);
    if (stepName == null)
      stepNameSB.append(rexxExecName);
    else
      stepNameSB.append(stepName);
    
    for (int i=rexxExecName.length()+1; i <= 9; i++)
    {
      stepNameSB.append(' ');
    }

    StringBuffer sb = new StringBuffer(350);

    // if condRc was specified, generate the IF statement       
    if (condRc != null)
    {
      sb.append(ifStmt1).append(condRc).append(ifStmt2).append(NL_);
    }
    
    sb.append("//").append(stepNameSB).append(execStmt);
    sb.append(rexxExecName).append("'").append(NL_);       
    sb.append(sysexecDD1).append(MvsProperties.mvsExecDsn).append(NL_);       
    sb.append(systsprtDD).append(NL_);
    sb.append(sysprintDD).append(NL_);
    sb.append(systsinDD).append(NL_);
    sb.append(inputDD).append(NL_);

    return sb.toString();

  }  // genRexxAllocStepInJcl()

  /**
   * Generate JCL for the FTPFILE step.
   *
   * @param defaultName: FTPFILE
   * @param stepName: null
   * @param condRc if not null, a JCL IF statement will be generated with the
   *               specified condition
   *
   */
  private String genFtpTransfer(String defaultName, String stepName,
                                String condRc) throws UnknownHostException
  {
    String ifStmt1       = "//         IF (";
    String ifStmt2       = ") THEN";       
    String execStmt = null;
    
    try 
    {
       String localHostName     = java.net.InetAddress.getLocalHost().getHostName();           
       String remoteHostName    = MvsProperties.remoteHost;

       String localHostStr  = null;
       String remoteHostStr = null;

       if (localHostName.indexOf(".") >= 0)
         localHostStr = localHostName.substring(0, localHostName.indexOf("."));
       else
         localHostStr = localHostName;

       if (remoteHostName.indexOf(".") >= 0)
         remoteHostStr = remoteHostName.substring(0, remoteHostName.indexOf("."));
       else
         remoteHostStr = remoteHostName;
   
       // Is the local host machine and the remote host machine different
       if (!localHostStr.equalsIgnoreCase(remoteHostStr))
       {
          differentMachines_ = true; 

          execStmt      = "EXEC PGM=FTP,PARM='" + localHostName + " (EXIT=8'";        
       }
       else
          execStmt      = "EXEC PGM=FTP,PARM='" + MvsProperties.remoteHost + " (EXIT=16'";            

    }    
    catch(UnknownHostException e)
    {
      System.out.println
        ("  ERROR: Unable to get a valid hostname for FTP Transfer");
      throw new UnknownHostException();
    }

    String netrcDD       = "//NETRC DD DISP=SHR,DSN=" + MvsProperties.netrcFile;       
    String sysprintDD    = "//SYSPRINT  DD SYSOUT=*";            
    String sysinDD       = "//SYSIN     DD *";

    // pad stepname to 9 characters
    StringBuffer stepNameSB = new StringBuffer(9);
    if (stepName == null)
      stepNameSB.append(defaultName);
    else
      stepNameSB.append(stepName);
    
    for (int i=defaultName.length()+1; i <= 9; i++)
    {
      stepNameSB.append(' ');
    }

    StringBuffer sb = new StringBuffer(350);

    // if condRc was specified, generate the IF statement
    if (condRc != null)
    {
      sb.append(ifStmt1).append(condRc).append(ifStmt2).append(NL_);
    }
    
    sb.append("//").append(stepNameSB).append(execStmt).append(NL_);

    if (MvsProperties.netrcFile != null) 
    {
       sb.append(netrcDD).append(NL_);
    }

    sb.append(sysprintDD).append(NL_);
    sb.append(sysinDD).append(NL_);
    
    if (MvsProperties.netrcFile == null) 
    {      
      sb.append(MvsProperties.userid).append(NL_);
      sb.append(MvsProperties.password).append(NL_);
    }

    return sb.toString();
  }  // genFtpTransfer()

  /**
   * Read the JCL Template File and replace the appropriate JCL information.
   * It returns the string command.    
   *
   * @param pdsLangStr: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMacStr: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacStr: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObjStr: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsClrPrtStr: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsLstStr: PDS for listing files (i.e. USERID.SANDBOX.LISTING)   
   * @param pdsUniStr: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param bw BufferedWriter object that represents the jcl file      
   * @param srcTarget: Source Files (i.e. test) 
   * @param fileNo: Index number of file  
   * @param langType: ASM, COBOL, PLS, or PLX  
   *            
   */
  private void readJCLTemplateFile(String pdsLangStr, String pdsLMacStr,
                                   String pdsEMacStr, String pdsObjStr,
                                   String pdsClrPrtStr, String pdsLstStr,
                                   String pdsUniStr, String srcTarget,
                                   int fileNo, String langType,
                                   BufferedWriter bw) throws
                                   IOException, FileNotFoundException
  {        
    StringBuffer srcTargetSB = new StringBuffer(9);
    StringBuffer listSB      = new StringBuffer(9);
    StringBuffer sb          = new StringBuffer(350);
     
    String listStr      = null;  
    String tempStr      = null;
    String jclTemplateFile = null;
    
    boolean jclTemplateDataNotNeeded = false;

    if (fileNo == 0)
    {
      listStr = "LIST";  
      tempStr = "SRC";   //default
    }
    else
    {
      listStr = "LIST" + fileNo;  
      tempStr = "SRC" + fileNo;   //default
    }

    if (srcTarget == null)
    {
      srcTarget = langType.toUpperCase() + tempStr; 
      srcTargetSB.append(srcTarget.toUpperCase());
    }
    else
      srcTargetSB.append(srcTarget.toUpperCase());

    listSB.append(listStr);

    // check srcTarget Name for longer than 9 characters here
    // pad stepname to 9 characters
    for (int i=srcTarget.length()+1; i <= 9; i++)
    {
      srcTargetSB.append(' ');
    }
    
    for (int i=listStr.length()+1; i <= 9; i++)
    {
      listSB.append(' ');
    }

    String execStepName = srcTargetSB.toString(); 
    String listStep     = listSB.toString();

    String dirsep = System.getProperty("file.separator");
    String baseDir = MvsProperties.srcBaseDir;
    String rulesDir = baseDir + "rules_mk" + dirsep;
    boolean odeTemplate = false;

    if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
    {
      if ( (MvsProperties.useMyTemplates.equalsIgnoreCase("YES")) && 
           (isLanguageFound(langType)) )
      {
        if (langType == "asm")
           jclTemplateFile = rulesDir + "usrasm.jcl";
        else if (langType == "cobol")
           jclTemplateFile = rulesDir + "usrcobol.jcl";    
        else if (langType == "pls")
           jclTemplateFile = rulesDir + "usrpls.jcl";    
        else if (langType == "plx")
           jclTemplateFile = rulesDir + "usrplx.jcl";
        
        bw.write( readUSERTemplate(jclTemplateFile,
                                   pdsLangStr, pdsLMacStr, pdsEMacStr,
                                   pdsObjStr, pdsClrPrtStr, pdsLstStr,
                                   pdsUniStr, srcTarget, langType,
                                   execStepName, listStep) );
      }
      else
      {
        if (langType == "asm")
           jclTemplateFile = rulesDir + "asmtemplate.jcl";
        else if (langType == "cobol")
           jclTemplateFile = rulesDir + "cbltemplate.jcl";
        else if (langType == "pls")
           jclTemplateFile = rulesDir + "plstemplate.jcl";       
        else if (langType == "plx")
           jclTemplateFile = rulesDir + "plxtemplate.jcl";

        bw.write( readODETemplate(jclTemplateFile,
                                  pdsLangStr, pdsLMacStr, pdsEMacStr,
                                  pdsObjStr, pdsClrPrtStr, pdsLstStr,
                                  pdsUniStr, srcTarget, langType,
                                  execStepName, listStep) );
      }
    }
    else
    {
      if (langType == "asm")
         jclTemplateFile = rulesDir + "usrasm.jcl";
      else if (langType == "cobol")
         jclTemplateFile = rulesDir + "usrcobol.jcl";    
      else if (langType == "pls")
         jclTemplateFile = rulesDir + "usrpls.jcl";    
      else if (langType == "plx")
         jclTemplateFile = rulesDir + "usrplx.jcl";

      bw.write( readUSERTemplate(jclTemplateFile,
                                 pdsLangStr, pdsLMacStr, pdsEMacStr,
                                 pdsObjStr, pdsClrPrtStr, pdsLstStr,
                                 pdsUniStr, srcTarget, langType,
                                 execStepName, listStep) );
    }

  }

  /**
   * Returns true if the specified language is found in the variable 
   * language2use
   *
   * @param String langStr
   */
  private boolean isLanguageFound( String langStr )
  {
    StringTokenizer wordTokens =
           new StringTokenizer(MvsProperties.languages2use);
    

    String lang2Use;
    int numOfWordsInString = wordTokens.countTokens();

    while (wordTokens.hasMoreTokens())
    {
      lang2Use = (String) wordTokens.nextToken();
      
      if (lang2Use.equalsIgnoreCase(langStr))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Read ODE's JCL Template File and replace the appropriate JCL information.
   * It returns the string command.    
   *
   * @param pdsLangStr: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMacStr: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacStr: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObjStr: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsClrPrtStr: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsLstStr: PDS for listing files (i.e. USERID.SANDBOX.LISTING)   
   * @param pdsUniStr: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param bw BufferedWriter object that represents the jcl file      
   * @param srcExecNamet: Source Files (i.e. test) 
   * @param langType: ASM, COBOL, PLS, or PLX  
   *            
   */
  private static String readODETemplate(String jclODETemplate,
                                        String pdsLangStr, String pdsLMacStr,
                                        String pdsEMacStr, String pdsObjStr,
                                        String pdsClrPrtStr, String pdsLstStr,
                                        String pdsUniStr, String srcTarget,
                                        String langType, String srcExecName,
                                        String lstStep) throws 
                                        IOException, FileNotFoundException
  {
    StringBuffer sb          = new StringBuffer(350);

    try
    {
      BufferedReader jclTemplateReader = new BufferedReader(new FileReader(jclODETemplate));
       
      String listStr      = null;  
      String tempStr      = null;
      String execStmt     = null;
      String stepLib      = null;
      String sysinStmtDD1 = null;
      String sysinStmtDD2 = null;
      String syslibDD1    = null;
      String syslibDD2    = null;
      String syslibDD3    = null;
      String syslibDD4    = null;
      String syslibDD5    = null;
      String syslibDD6    = null;       
      String asmlibDD1    = null;
      String asmlibDD2    = null;
      String asmlibDD3    = null;
      String asmlibDD4    = null;
      String asmObjDD1    = null;
      String cblObjDD1    = null;
      String plsObjDD1    = null;
      String plxObjDD1    = null;
      String asmClrDD1    = null;
      String asmUniDD1    = null;
      String plsClrDD1    = null;
      String plxClrDD1    = null;
      String parmStmt     = null;
      String comment2     = null;
        
      boolean jclTemplateDataNotNeeded = false;

      String execStmt2    = "EXEC PGM=IEBGENER,COND=(20,LE," + srcTarget.toUpperCase() + ")";
      String comment0     = "//*";        
      String comment1     = "//* These are standard system libraries";
      String comment5     = "//*------------------------------------------------------------";       
      String plsxLst      = "//* Combine the ASM and PLX Listing into one";
      String asmLst       = "//* ASM Listing ";
      String cblLst       = "//* COBOL Listing ";       
      String objDD        = "//           DSN=" +pdsObjStr+"("+srcTarget.toUpperCase()+")";               
      String clrPrntDD    = "//           DSN=" +pdsClrPrtStr+"("+srcTarget.toUpperCase()+")";     
      String lstSysut2DD1 = "//LIST.SYSUT2       DD DISP=SHR,"; 
      String lstSysut2DD2 = "//           DSN=" +pdsLstStr+"("+srcTarget.toUpperCase()+")";     
      String uniDataDD    = "//           DSN=" +pdsUniStr+"("+srcTarget.toUpperCase()+")";     

      if (langType == "asm")
      {
        execStmt     = "EXEC PGM=&ASMPGM,REGION=&REGION,";
        parmStmt     = "//  PARM=&ASMFLAGS";       
        stepLib      = "//STEPLIB  DD &STEPALIB,DISP=SHR";
        comment2     = "//* for the ASM Compile";
        sysinStmtDD1 = "//ASM.SYSIN         DD DISP=SHR,";
        sysinStmtDD2 = "//           DSN=" + pdsLangStr;                
        syslibDD1    = "//ASM.SYSLIB        DD DISP=SHR,";       
        syslibDD2    = "//           DSN=" + pdsLMacStr;                      
        syslibDD3    = "//                  DD DISP=SHR,";       
        syslibDD4    = "//           DSN=" + pdsEMacStr;                              
        asmObjDD1    = "//ASM.SYSLIN        DD DISP=SHR,";             
        asmClrDD1    = "//ASM.CLRPRINT      DD DISP=SHR,";
        asmUniDD1    = "//ASM.UNIDATA       DD DISP=SHR,";       
      }
      else if (langType == "cobol") 
      {
        execStmt     = "EXEC PGM=&CBLPGM,REGION=&REGION,";
        parmStmt     = "//  PARM=&CBLFLAGS";       
        stepLib      = "//STEPLIB  DD &STEPCLIB,DISP=SHR";
        comment2     = "//* for the COBOL Compile";
        sysinStmtDD1 = "//COB.SYSIN           DD DISP=SHR,";
        sysinStmtDD2 = "//             DSN=" + pdsLangStr;                
        syslibDD1    = "//COB.SYSLIB          DD DISP=SHR,";       
        syslibDD2    = "//             DSN=" + pdsLMacStr;                      
        syslibDD3    = "//                    DD DISP=SHR,";       
        syslibDD4    = "//             DSN=" + pdsEMacStr; 
        syslibDD5    = "//                    DD DISP=SHR,";       
        syslibDD6    = "//             DSN=" + pdsLangStr;
        cblObjDD1    = "//COB.SYSLIN          DD DISP=SHR,";             
      }
      else if (langType == "pls")
      {
        execStmt     = "EXEC PGM=&PLSPGM,REGION=&REGION,";
        parmStmt     = "//  PARM=&PLSFLAGS";       
        stepLib      = "//STEPLIB  DD &STEPSLIB,DISP=SHR";
        comment2     = "//* for the PLS Compile";
        sysinStmtDD1 = "//PLS.SYSIN         DD DISP=SHR,";           
        sysinStmtDD2 = "//           DSN=" + pdsLangStr;                           
        syslibDD1    = "//PLS.SYSLIB        DD DISP=SHR,";       
        syslibDD2    = "//           DSN=" + pdsLMacStr;
        syslibDD3    = "//                  DD DISP=SHR,";       
        syslibDD4    = "//           DSN=" + pdsEMacStr;                              
        asmlibDD1    = "//PLS.ASMLIB        DD DISP=SHR,";       
        asmlibDD2    = "//           DSN=" + pdsLMacStr;              
        asmlibDD3    = "//                  DD DISP=SHR,";       
        asmlibDD4    = "//           DSN=" + pdsEMacStr; 
        plsObjDD1    = "//PLS.ASMLIN        DD DISP=SHR,";                
        plsClrDD1    = "//PLS.CLRPRINT      DD DISP=SHR,";       
      }
      else if (langType == "plx")
      {
        execStmt     = "EXEC PGM=&PLXPGM,REGION=&REGION,";
        parmStmt     = "//  PARM=&PLXFLAGS";       
        stepLib      = "//STEPLIB  DD &STEPXLIB,DISP=SHR";
        comment2     = "//* for the PLX Compile";
        sysinStmtDD1 = "//PLX.SYSIN         DD DISP=SHR,";
        sysinStmtDD2 = "//           DSN=" + pdsLangStr;                
        syslibDD1    = "//PLX.SYSLIB        DD DISP=SHR,";       
        syslibDD2    = "//           DSN=" + pdsLMacStr;  
        syslibDD3    = "//                  DD DISP=SHR,";       
        syslibDD4    = "//           DSN=" + pdsEMacStr;                                         
        asmlibDD1    = "//PLX.ASMLIB        DD DISP=SHR,";       
        asmlibDD2    = "//           DSN=" + pdsLMacStr;              
        asmlibDD3    = "//                  DD DISP=SHR,";       
        asmlibDD4    = "//           DSN=" + pdsEMacStr;                      
        plxObjDD1    = "//PLX.ASMLIN        DD DISP=SHR,";                    
        plxClrDD1    = "//PLX.CLRPRINT      DD DISP=SHR,";         
      }

      String nextLine;

      while ((nextLine = jclTemplateReader.readLine()) != null) 
      {
        if (nextLine.endsWith("&REGION,")) 
        {                
          jclTemplateDataNotNeeded = true;
          
          sb.append("//").append(srcExecName).append(execStmt).append(NL_);
          sb.append(parmStmt).append(NL_);      
          sb.append(comment0).append(NL_);
          sb.append(stepLib).append(NL_);      
          sb.append(comment0).append(NL_);               
        }
        else if (nextLine.startsWith("//SYSPRINT"))
        {  
          sb.append(nextLine).append(NL_);

          jclTemplateDataNotNeeded = false;
        }
        else if (nextLine.startsWith("//ASM.SYSIN") ||
                 nextLine.startsWith("//COB.SYSIN") ||
                 nextLine.startsWith("//PLS.SYSIN") ||
                 nextLine.startsWith("//PLX.SYSIN"))
        {                
          jclTemplateDataNotNeeded = true;

          sb.append(sysinStmtDD1).append(NL_);
          sb.append(sysinStmtDD2).append("(").append(srcTarget.toUpperCase()).append(")");
          sb.append(NL_);
          sb.append(comment1).append(NL_);
          sb.append(comment2).append(NL_);
          sb.append(syslibDD1).append(NL_);
          sb.append(syslibDD2).append(NL_);
          sb.append(syslibDD3).append(NL_);
          sb.append(syslibDD4).append(NL_);
          if (langType == "cobol") 
          {
            sb.append(syslibDD5).append(NL_);
            sb.append(syslibDD6).append(NL_);
          }

          if (langType == "asm")
          {
            if (MvsProperties.prodAsmSysLib != null)
            {
              sb.append(genSyslibFormat(MvsProperties.prodAsmSysLib));
              sb.append(NL_);
            }
          }

          if (langType == "cobol")
          {
            if (MvsProperties.prodCblSysLib != null)
            {
              sb.append(genSyslibFormat(MvsProperties.prodCblSysLib));
              sb.append(NL_);
            }
          }

          if (langType == "pls")
          {
            if (MvsProperties.prodPlsSysLib != null)
            {
              sb.append(genSyslibFormat(MvsProperties.prodPlsSysLib));
              sb.append(NL_);
            }
          }

          if (langType == "plx")
          {
            if (MvsProperties.prodPlxSysLib != null)
            {
              sb.append(genSyslibFormat(MvsProperties.prodPlxSysLib));
              sb.append(NL_);
            }
          }
         
          sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);

          if ((langType == "pls") || (langType == "plx"))
          {
            sb.append(asmlibDD1).append(NL_);
            sb.append(asmlibDD2).append(NL_);
            sb.append(asmlibDD3).append(NL_);
            sb.append(asmlibDD4).append(NL_);
            sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);
          }  
        }
        else if (nextLine.startsWith("//LIST"))
        {
          if (langType == "asm")
            sb.append(asmObjDD1).append(NL_);
          else if (langType == "cobol")
            sb.append(cblObjDD1).append(NL_);
          else if (langType == "pls")
            sb.append(plsObjDD1).append(NL_);
          else if (langType == "plx")
            sb.append(plxObjDD1).append(NL_);                                

            sb.append(objDD).append(NL_);             

          if (langType == "asm")
            sb.append(asmClrDD1).append(NL_);
          else if (langType == "pls")
            sb.append(plsClrDD1).append(NL_);
          else if (langType == "plx")
            sb.append(plxClrDD1).append(NL_);                                

          if (langType != "cobol") 
          {
            sb.append(clrPrntDD).append(NL_);
          }

          if (langType == "asm")
          {
            sb.append(asmUniDD1).append(NL_);
            sb.append(uniDataDD).append(NL_);
          }

          sb.append(comment5).append(NL_);
          sb.append(comment0).append(NL_);
          if (langType == "asm")
            sb.append(asmLst).append(NL_);
          else if (langType == "cobol") 
             sb.append(cblLst).append(NL_);
          else
            sb.append(plsxLst).append(NL_);
          sb.append(comment0).append(NL_);
          sb.append(comment5).append(NL_);

          sb.append("//").append(lstStep).append(execStmt2).append(NL_);
       
          jclTemplateDataNotNeeded = false;
        }
        else if (nextLine.startsWith("//LST"))
        {
          jclTemplateDataNotNeeded = true;
        }  
        else if (nextLine.startsWith("//SYSIN"))
        {
          jclTemplateDataNotNeeded = false;

          sb.append(lstSysut2DD1).append(NL_);
          sb.append(lstSysut2DD2).append(NL_);

          sb.append(nextLine).append(NL_);
        }  
        else
        {
          if (!jclTemplateDataNotNeeded)
            sb.append(nextLine).append(NL_);
        }
      }
    }
    catch (FileNotFoundException ex)
    {
      System.err.println("JCL file " + jclODETemplate + " not found.");
      System.err.println(ex);
      System.exit(-1);
    }
    catch (IOException e)
    {
      System.err.println("Error reading " + jclODETemplate);
      System.err.println(e);
      System.exit(-1);
    }

    return sb.toString();
  }

  /**
   * Read the User's JCL Template File and replace the appropriate JCL information.
   * It returns the string command.    
   *
   * @param pdsLangStr: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMacStr: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacStr: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObjStr: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsClrPrtStr: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsLstStr: PDS for listing files (i.e. USERID.SANDBOX.LISTING)   
   * @param pdsUniStr: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   * @param bw BufferedWriter object that represents the jcl file      
   * @param srcExecNamet: Source Files (i.e. test) 
   * @param langType: ASM, COBOL, PLS, or PLX  
   *            
   */
  private static String readUSERTemplate(String jclUserTemplate,
                                         String pdsLangStr, String pdsLMacStr,
                                         String pdsEMacStr, String pdsObjStr,
                                         String pdsClrPrtStr, String pdsLstStr,
                                         String pdsUniStr, String srcTarget,
                                         String langType, String srcExecName, 
                                         String lstStep) throws 
                                         IOException, FileNotFoundException
  {
     StringBuffer sb          = new StringBuffer(350);

     try
     {
       BufferedReader jclTemplateReader = new BufferedReader(new FileReader(jclUserTemplate));   

       // Variables for Non-ode Templates
       String datasetStr1 = "FILELIST";
       String datasetStr2 = "ASMLIST";

       String syslibDD1    = null;
       String syslibDD2    = null;
       String syslibDD3    = null;
       String syslibDD4    = null;
       String sysprtDD1Str = null;
       String sysprtDD2Str = null;
       String syslinDD1Str = null;
       String syslinDD2Str = null;     
       String modObjStr    = null;
       String sysutStr     = null;
       String objDDStr     = null;
       String sysPrtStr    = null;
       String sysDumStr    = null;
       String lstStr       = null;
       String lstExecStr   = null;
       String lstSysut1DD1 = null;
       String lstSysut1DD2 = null;
       String lstSysut2DD1 = null; 
       String lstSysut2DD2 = null;            
       String sysinDumStr  = null;
       String sysinPrntStr = null;
       String asmObjDD1    = null;
       String cblObjDD1    = null;
       String plsObjDD1    = null;
       String plxObjDD1    = null;       
       String asmClrDD1    = null;       
       String plsClrDD1    = null;
       String plxClrDD1    = null;
       String unidata      = null;
       String asmUniDD1    = null;
       String asmPrtDD1    = null;
       String asmPrtDD2    = null;
       String asmMut1DD1   = null;
       String asmMut1DD2   = null;
       String asmlibDD1    = null;
       String sysut1DDStr  = null;
       String sysut2DDStr  = null;
       String sysut3DDStr  = null;
       String sysut4DDStr  = null;
       String sysut5DDStr  = null;
       String sysut1DDAsm  = null;
       String sysut5DD1Str = null;
       String sysut6DD1Str = null;
       String sysut7DD1Str = null;

       String comment0     = "//*";        
       String comment1     = "//* These are standard system libraries";       
       String comment5     = "//*------------------------------------------------------------";       

       sysprtDD1Str = "//SYSPRINT DD DSN=&&"+datasetStr1+",DISP=(NEW,PASS),SPACE=(CYL,(1,15)),";
       sysprtDD2Str = "//            DCB=(RECFM=FBM,LRECL=121),UNIT=SYSDA";
       unidata      = "//UNIDATA  DD SYSOUT=H";
       sysut1DDStr  = "//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(4,4))";
       sysut2DDStr  = "//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(5,5))";
       sysut3DDStr  = "//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(5,5))";
       sysut4DDStr  = "//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(5,5))";
       sysut5DDStr  = "//SYSUT5   DD DISP=(,DELETE),UNIT=SYSDA,";
       sysut5DD1Str = "//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(4,4))";
       sysut6DD1Str = "//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(5,5))";
       sysut7DD1Str = "//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(5,5))";

       sysut1DDAsm  = "//SYSUT1   DD UNIT=VIO,SPACE=(CYL,(5,1))";
       syslibDD1    = "//                  DD DISP=SHR,";       
       syslibDD2    = "//           DSN=" + pdsLMacStr;  
       syslibDD3    = "//                  DD DISP=SHR,";       
       syslibDD4    = "//           DSN=" + pdsEMacStr;

       objDDStr     = "//SYSUT2    DD DSN=" +pdsObjStr+"("+srcTarget.toUpperCase()+"),DISP=SHR";
       // ASM, COBOL, PLS, PLX information
       String objDD        = "//           DSN=" +pdsObjStr+"("+srcTarget.toUpperCase()+")";               
       String clrPrntDD    = "//           DSN=" +pdsClrPrtStr+"("+srcTarget.toUpperCase()+")";     
       String uniDataDD    = "//           DSN=" +pdsUniStr+"("+srcTarget.toUpperCase()+")";     

       // ASM information
       asmObjDD1    = "//ASM.SYSLIN        DD DISP=SHR,";             
       asmClrDD1    = "//ASM.CLRPRINT      DD DISP=SHR,";
       asmUniDD1    = "//ASM.UNIDATA       DD DISP=SHR,"; 
       // COBOL information
       cblObjDD1    = "//COB.SYSLIN        DD DISP=SHR,";             
       // PLS information
       plsObjDD1    = "//PLS.ASMLIN        DD DISP=SHR,";                
       plsClrDD1    = "//PLS.CLRPRINT      DD DISP=SHR,";       
       // PLX information
       plxObjDD1    = "//PLX.ASMLIN        DD DISP=SHR,";                    
       plxClrDD1    = "//PLX.CLRPRINT      DD DISP=SHR,";         
       // PLS and PLX information
       asmPrtDD1    = "//ASMPRINT DD DSN=&&"+datasetStr2+",DISP=(NEW,PASS),SPACE=(CYL,(1,15)),";
       asmPrtDD2    = "//            DCB=(RECFM=FBM,LRECL=121),UNIT=SYSDA";
       asmMut1DD1   = "//ASMUT1   DD UNIT=SYSDA,";
       asmMut1DD2   = "//            SPACE=(CYL,(5,10))";

       asmlibDD1    = "//ASMLIB            DD DISP=SHR,";       

       lstStr       = "//* Listing";
       lstExecStr   = "//LIST     EXEC PGM=IEBGENER,COND=(20,LE,"+srcTarget.toUpperCase()+")";
       
       lstSysut1DD1 = "//SYSUT1   DD DISP=(OLD,DELETE),DSN=&&"+datasetStr1+",UNIT=SYSDA";
       lstSysut1DD2 = "//         DD DISP=(OLD,DELETE),DSN=&&"+datasetStr2+",UNIT=SYSDA";       
       lstSysut2DD1 = "//LIST.SYSUT2       DD DISP=SHR,"; 
       lstSysut2DD2 = "//           DSN=" +pdsLstStr+"("+srcTarget.toUpperCase()+")";     
       sysinDumStr  = "//SYSIN    DD DUMMY";
       sysinPrntStr = "//SYSPRINT DD DUMMY";
       
       String nextLine;
       String parsedLinesFrom = null;

       boolean steplibFound = false;
       boolean sysutFound = false;
       boolean sysinFound = false;
       boolean syslibFound = false;
       boolean sysprtFound = false;
       boolean unidataFound = false;       
       boolean listFound = false;
       boolean printLineFound = false;

       boolean syslibInfoNeeded = false;
       boolean skipNextLine = false;
       boolean continuationLine = false;
       boolean needObjectInfo = false;
       boolean needListing = false;

       int execCount  = 0;
       int sysprtCount = 0;
       int sysutCount = 0;
       int libCount = 0;
       int syslibParsedCnt = 0;
       int asmPrtCount = 0;
       int continuationCount = 0;
       
       while ((nextLine = jclTemplateReader.readLine()) != null) 
       {
         if (nextLine.startsWith("//*"))
         {
           if (parsedLinesFrom == "SYSLIB")               
           {
             needObjectInfo = true;
           }
           else
           {
             // add this comment line to JCL
             sb.append(nextLine).append(NL_);
             skipNextLine = false;
           }
         }
         else if (nextLine.indexOf("// ") >= 0)
         { // continuation line
           continuationCount++;

           if (!skipNextLine) 
           {
             sb.append(nextLine).append(NL_);
           }
         }
         else
         {
           skipNextLine = false;

           // Add object data to the SYSLIB section of the user template
           if (parsedLinesFrom == "SYSLIB") 
           { 
             syslibParsedCnt++;

             if (langType == "cobol") 
             {
                String dsnStr   = "//         DD DSN=" + pdsLangStr + ",DISP=SHR"; 
                sb.append(dsnStr).append(NL_);             
             }

             // add DSNs (.MACLOCAL and .MACEXPT)                 
             sb.append(syslibDD1).append(NL_);
             sb.append(syslibDD2).append(NL_);
             sb.append(syslibDD3).append(NL_);
             sb.append(syslibDD4).append(NL_);

             needObjectInfo = true;

             if ((!nextLine.startsWith("//SYSLIB")) &&
                 (!nextLine.startsWith("//ASMLIB")) &&
                 (nextLine.indexOf(".SYSLIB") == -1) &&
                 (nextLine.indexOf(".ASMLIB") == -1))
             {  
               if (langType == "asm")
               {
                 sb.append(asmObjDD1).append(NL_);
                 sb.append(objDD).append(NL_);
               
                 sb.append(asmClrDD1).append(NL_);       
                 sb.append(clrPrntDD).append(NL_);
                 sb.append(asmUniDD1).append(NL_);
                 sb.append(uniDataDD).append(NL_);
               }
               else if (langType == "cobol") 
               {
                 sb.append(cblObjDD1).append(NL_);
                 sb.append(objDD).append(NL_);
               } 
               else if (langType == "pls")
               {
                 sb.append(plsObjDD1).append(NL_);
                 sb.append(objDD).append(NL_);
                
                 sb.append(plsClrDD1).append(NL_);                                
                 sb.append(clrPrntDD).append(NL_);
               }
               else if (langType == "plx")
               {
                 sb.append(plxObjDD1).append(NL_);        
                 sb.append(objDD).append(NL_);
  
                 sb.append(plxClrDD1).append(NL_);                                
                 sb.append(clrPrntDD).append(NL_);
               }
               needObjectInfo = false;
             }
             needListing = true;  
           }   
           else if ((parsedLinesFrom == "SYSUT") && 
                    (listFound) && (needListing))
           {
             //Add list data to the SYSUT section of user template
             sb.append(lstSysut2DD1).append(NL_); 
             sb.append(lstSysut2DD2).append(NL_);
             needListing = false;  
           }

           // Parsing user template........
           if ((nextLine.indexOf("EXEC") >= 0))
           {
             parsedLinesFrom = "EXEC";
             execCount++;

             // Does the EXEC statment contains the string PGM
             if ((nextLine.indexOf("PGM=") >= 0))
             { // Find first EXEC statemnet in the custom JCL template
               // Change the stepname to the source file name
               if (execCount == 1)
               { 
                  String execStr = nextLine.substring(nextLine.indexOf("EXEC"));
                  sb.append("//").append(srcExecName).append(execStr).append(NL_);

                  needListing = true;
               }
               else
               {
                 // No SYSUT statement exist in the custom JCL template
                 if (!sysutFound)
                 { 
                   if ((langType == "pls") || (langType == "plx"))
                   {
                     sb.append(sysut1DDStr).append(NL_);
                     sb.append(sysut2DDStr).append(NL_);
                     sb.append(sysut3DDStr).append(NL_);
                     sb.append(sysut4DDStr).append(NL_);
                     sb.append(sysut5DDStr).append(NL_);
                   }
                   else if (langType == "asm") 
                   {
                     sb.append(sysut1DDAsm).append(NL_);
                   }
                   else if (langType == "cobol") 
                   {
                     sb.append(sysut1DDStr).append(NL_);
                     sb.append(sysut2DDStr).append(NL_);
                     sb.append(sysut3DDStr).append(NL_);
                     sb.append(sysut4DDStr).append(NL_);
                     sb.append(sysut5DD1Str).append(NL_);
                     sb.append(sysut6DD1Str).append(NL_);
                     sb.append(sysut7DD1Str).append(NL_);
                   }
                   sysutFound = true;
                 }

                 // No SYSIN statement in the custom JCL template
                 // Need to add SYSIN information
                 if (!sysinFound)
                 {
                   if ((!printLineFound) &&
                       ((langType == "pls") || (langType == "plx"))) 
                   {
                      sb.append(asmPrtDD1).append(NL_);
                      sb.append(asmPrtDD2).append(NL_);
                      sb.append(asmMut1DD1).append(NL_);
                      sb.append(asmMut1DD2).append(NL_);
                   }

                   String sysinDD1 = "//SYSIN             DD DISP=SHR,";
                   String sysinDD2 = "//           DSN=" + pdsLangStr;
                  
                   sb.append(sysinDD1).append(NL_);
                   sb.append(sysinDD2+"("+srcTarget.toUpperCase()+")");
                   sb.append(NL_);

                   sysinFound = true;
                 }

                 // No SYSLIB statement in the custom JCL template
                 // Need to add SYSLIB information
                 if (!syslibFound) 
                 {
                   String slibDD1    = "//SYSLIB            DD DISP=SHR,";       
                   String slibDD2    = "//           DSN=" + pdsLMacStr;  
                   String slibDD3    = "//                  DD DISP=SHR,";       
                   String slibDD4    = "//           DSN=" + pdsEMacStr;
                   String slibDD5    = "//                  DD DISP=SHR,";       
                   String slibDD6    = "//           DSN=" + pdsLangStr;
                   
                   sb.append(slibDD1).append(NL_);
                   sb.append(slibDD2).append(NL_);
                   sb.append(slibDD3).append(NL_);
                   sb.append(slibDD4).append(NL_);
                   
                   if (langType == "cobol")
                   {
                     sb.append(slibDD5).append(NL_);
                     sb.append(slibDD6).append(NL_);

                     if (MvsProperties.prodCblSysLib != null)
                     {
                       sb.append(genSyslibFormat(MvsProperties.prodCblSysLib));
                       sb.append(NL_);
                     }
                   }

                   if (langType == "asm")
                   {
                     if (MvsProperties.prodAsmSysLib != null)
                     {
                       sb.append(genSyslibFormat(MvsProperties.prodAsmSysLib));
                       sb.append(NL_);
                     }
                   }
                 
                   if (langType == "pls")
                   {
                     if (MvsProperties.prodPlsSysLib != null)
                     {
                       sb.append(genSyslibFormat(MvsProperties.prodPlsSysLib));
                       sb.append(NL_);
                     }
                   }
         
                   if (langType == "plx")
                   {
                     if (MvsProperties.prodPlxSysLib != null)
                     {
                       sb.append(genSyslibFormat(MvsProperties.prodPlxSysLib));
                       sb.append(NL_);
                     }
                   }
        
                   if (MvsProperties.stndSysLib != null)
                     sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);
        
                   // ASMLIB            DD DISP=SHR,
                   // add DSNs (.MACLOCAL and .MACEXPT)
                   if ((langType == "pls") || (langType == "plx")) 
                   {
                     sb.append(asmlibDD1).append(NL_);
                     sb.append(syslibDD2).append(NL_);
                     sb.append(syslibDD3).append(NL_);
                     sb.append(syslibDD4).append(NL_);
        
                     if (langType == "pls")
                     {
                       if (MvsProperties.prodPlsSysLib != null)
                       {
                         sb.append(genSyslibFormat(MvsProperties.prodPlsSysLib));
                         sb.append(NL_);
                       }
                     }
           
                     if (langType == "plx")
                     {
                       if (MvsProperties.prodPlxSysLib != null)
                       {
                         sb.append(genSyslibFormat(MvsProperties.prodPlxSysLib));
                         sb.append(NL_);
                       }
                     }
                     
                     if (MvsProperties.stndSysLib != null)
                       sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);
                   }

                   if (langType == "asm")
                   {
                     sb.append(asmObjDD1).append(NL_);
                     sb.append(objDD).append(NL_);
                   
                     sb.append(asmClrDD1).append(NL_);       
                     sb.append(clrPrntDD).append(NL_);
                     sb.append(asmUniDD1).append(NL_);
                     sb.append(uniDataDD).append(NL_);
                   }
                   else if (langType == "cobol")
                   {
                     sb.append(cblObjDD1).append(NL_);
                     sb.append(objDD).append(NL_);
                   }
                   else if (langType == "pls")
                   {
                     sb.append(plsObjDD1).append(NL_);
                     sb.append(objDD).append(NL_);
                    
                     sb.append(plsClrDD1).append(NL_);                                
                     sb.append(clrPrntDD).append(NL_);
                   }
                   else if (langType == "plx")
                   {
                     sb.append(plxObjDD1).append(NL_);        
                     sb.append(objDD).append(NL_);
      
                     sb.append(plxClrDD1).append(NL_);                                
                     sb.append(clrPrntDD).append(NL_);
                   }
                   syslibFound = true;
                   needObjectInfo = false;
                 }   

                 // add the LIST EXEC line
                 if (nextLine.startsWith("//LIST"))
                 {
                   sb.append(comment5).append(NL_);
                   sb.append(comment0).append(NL_);                     
                   sb.append(lstStr).append(NL_);
                   sb.append(comment0).append(NL_);
                   sb.append(comment5).append(NL_);
                   sb.append(lstExecStr).append(NL_);
                   
                   listFound = true;
                   needListing = true;
                   sysutCount = 0;
                 }
                 else // add EXEC line
                 {
                   if (needListing)
                   {
                     // listing information
                     sb.append(comment0).append(NL_);
                     sb.append(comment5).append(NL_);
                     sb.append(comment0).append(NL_);                     
                     sb.append(lstStr).append(NL_);
                     sb.append(comment0).append(NL_);
                     sb.append(comment5).append(NL_);
                     sb.append(lstExecStr).append(NL_);
                     sb.append(comment0).append(NL_);
                     sb.append(lstSysut1DD1).append(NL_); 
                     if ((langType == "pls") ||
                         (langType == "plx")) 
                     {
                        sb.append(lstSysut1DD2).append(NL_); 
                     }                     
                     sb.append(lstSysut2DD1).append(NL_); 
                     sb.append(lstSysut2DD2).append(NL_);     
                     sb.append(sysinDumStr).append(NL_);
                     sb.append(sysinPrntStr).append(NL_);
                     sb.append(comment0).append(NL_);

                     needListing = false;
                     sysutCount = 0;
                   }

                   sb.append(nextLine).append(NL_);
                 }

               } // end if execCount == 1
             }  // end PGM
             else
             {
               // add this line to JCL 
               sb.append(nextLine).append(NL_);
             }
             continuationCount = 0;
           } // end if EXEC
           else if (nextLine.startsWith("//STEPLIB"))
           {
             parsedLinesFrom = "STEPLIB";
             steplibFound = true;
             // add this line to JCL 
             sb.append(nextLine).append(NL_);
           }
           else if (nextLine.startsWith("//SYSPRINT"))
           {
             parsedLinesFrom = "SYSPRINT";

             sysprtCount++;

             if (execCount == 1)
             {
               // add sysprint line to JCL
               sb.append(sysprtDD1Str).append(NL_);
               sb.append(sysprtDD2Str).append(NL_);

               skipNextLine = true;
             }
             else
               sb.append(nextLine).append(NL_);

             sysprtFound = true;
             continuationCount = 0;
           } // end if SYSPRINT
           else if (nextLine.indexOf("//UNIDATA") >= 0)
           {
             parsedLinesFrom = "UNIDATA";

             if (!sysprtFound) 
             {
               // add sysprint line to JCL
               sb.append(sysprtDD1Str).append(NL_);
               sb.append(sysprtDD2Str).append(NL_);
              
               sysprtFound = true;
             }
             sb.append(nextLine).append(NL_);

             unidataFound = true;
             continuationCount = 0;
           } // end if UNIDATA
           else if (nextLine.indexOf("//SYSUT") >= 0)
           {
             parsedLinesFrom = "SYSUT";
             sysutCount++;

             if (sysutCount == 1)  
             {
               if (!listFound) 
               {
                 if (sysprtCount == 1)
                 {
                   if ((!unidataFound) &&
                       ((langType == "pls") || (langType == "plx")))
                   { // Add the unidata statement to the jcl
                     sb.append(unidata).append(NL_);
                     unidataFound = true;
                   }
                 }
                 else
                 { // sysprtCount equals 0
                   // add sysprint line to JCL
                   sb.append(sysprtDD1Str).append(NL_);

                   if ((langType == "pls") ||
                       (langType == "plx")) 
                   {
                     sb.append(sysprtDD2Str).append(NL_);
                   }
                   
                   sysprtFound = true;

                   if ((!unidataFound) &&
                       ((langType == "pls") || (langType == "plx"))) 
                   {
                     sb.append(unidata).append(NL_);
                     unidataFound = true;
                   }                 
                 }                 
                 sb.append(nextLine).append(NL_);
               }
               else
               {
                 sb.append(lstSysut1DD1).append(NL_); 
                 if ((langType == "pls") ||
                     (langType == "plx")) 
                 {
                    sb.append(lstSysut1DD2).append(NL_); 
                 }              

                 skipNextLine = true;
               }
             }
             else
             {
               if ((execCount > 1) && (listFound))
               {
                 skipNextLine = true;
                 needListing = false;  
               }
               else
               {
                 // add sysut line to JCL 
                 sb.append(nextLine).append(NL_);
               }
             }

             sysutFound = true;
             continuationCount = 0;
           } // end if SYSUT
           else if ((nextLine.indexOf("SYSLIN") >= 0) ||
                    (nextLine.indexOf("ASMLIN") >= 0) ||
                    (nextLine.indexOf(".ASMLIN") >= 0) ||
                    (nextLine.indexOf(".SYSLIN") >= 0))
           {
             parsedLinesFrom = "SYSLIN";
  
             skipNextLine = true;
             continuationCount = 0;
           } // end if SYSLIN
           else if (nextLine.startsWith("//SYSIN") ||
                    (nextLine.indexOf(".SYSIN") >= 0))
           { 
             parsedLinesFrom = "SYSIN";
            
             if (execCount == 1) 
             {
               if ((!printLineFound) && 
                   ((langType == "pls") || (langType == "plx"))) 
               {
                 sb.append(asmPrtDD1).append(NL_);
                 sb.append(asmPrtDD2).append(NL_);
                 sb.append(asmMut1DD1).append(NL_);
                 sb.append(asmMut1DD2).append(NL_);
               }

               int indexDISP = nextLine.indexOf("DISP=");
               int indexDSN  = nextLine.indexOf("DSN=");

               //SYSIN   DD DISP=SHR,DSN=HLQ.DATA.SRC or
               //SYSIN   DD DSN=HLQ.DATA.SRC,DISP=SHR or
               //SYSIN   DD DISP=SHR,                 or
               //SYSIN   DD DSN=HLQ.DATA.SRC,         or
               if ( (indexDISP < 0) || (indexDSN < 0) )
               {
                 skipNextLine = true;
               }

               String sysinDD1 = "//SYSIN             DD DISP=SHR,";
               String sysinDD2 = "//           DSN=" + pdsLangStr;
              
               sb.append(sysinDD1).append(NL_);
               sb.append(sysinDD2+"("+srcTarget.toUpperCase()+")");
               sb.append(NL_);                 
             }
             else
             {
               if ((listFound) && (needListing))
               { 
                 //LIST.SYSUT2       DD DISP=SHR,
                 //           DSN=WMATHIS.D13839.LISTING(TEST)
                 sb.append(lstSysut2DD1).append(NL_); 
                 sb.append(lstSysut2DD2).append(NL_);     
                 needListing = false;  
               }
               sb.append(nextLine).append(NL_);
             }
             sysinFound = true;
             continuationCount = 0;
           } // end if SYSIN
           else if (nextLine.startsWith("//SYSLIB") ||
                    nextLine.startsWith("//ASMLIB") ||
                    (nextLine.indexOf(".SYSLIB") >= 0) ||
                    (nextLine.indexOf(".ASMLIB") >= 0))
           { 
             parsedLinesFrom = "SYSLIB";
             libCount++;

             int indexDISP = nextLine.indexOf("DISP=");
             int indexDSN  = nextLine.indexOf("DSN=");
      
             //SYSLIB   DD DISP=SHR,DSN=HLQ.DATA.SRC or
             //SYSLIB   DD DSN=HLQ.DATA.SRC,DISP=SHR or
             //SYSLIB   DD DISP=SHR,                 or
             //SYSLIB   DD DSN=HLQ.DATA.SRC,         or
             if ( (indexDISP >= 0) || (indexDSN >= 0) )
             {
               // No SYSIN statement in the custom JCL template
               // Need to add SYSIN information
               if (!sysinFound)
               { 
                 if (execCount == 1)
                 {
                   if ((!printLineFound) && 
                       ((langType == "pls") || (langType == "plx"))) 
                   {
                     sb.append(asmPrtDD1).append(NL_);
                     sb.append(asmPrtDD2).append(NL_);
                     sb.append(asmMut1DD1).append(NL_);
                     sb.append(asmMut1DD2).append(NL_);
                   }

                   String sysinDD1 = "//SYSIN             DD DISP=SHR,";
                   String sysinDD2 = "//           DSN=" + pdsLangStr;
                  
                   sb.append(sysinDD1).append(NL_);
                   sb.append(sysinDD2+"("+srcTarget.toUpperCase()+")").append(NL_);
                   
                   sysinFound = true;               
                 }
               }
               sb.append(nextLine).append(NL_);
             }
             syslibFound = true;
             continuationCount = 0;
           } // end if SYSLIB
           else if (nextLine.indexOf("ASMIN") >= 0) 
           {
             parsedLinesFrom = "ASMIN";

             skipNextLine = true;
             continuationCount = 0;
           }
           else if (nextLine.indexOf("PRINT DD") >= 0)
           {
             parsedLinesFrom = "PRINT";
             asmPrtCount++;

             if (asmPrtCount == 1) 
             {
               if ((langType == "pls") || (langType == "plx")) 
               {
                 sb.append(asmPrtDD1).append(NL_);
                 sb.append(asmPrtDD2).append(NL_);
                 sb.append(asmMut1DD1).append(NL_);
                 sb.append(asmMut1DD2).append(NL_);
               }
             }

             printLineFound = true;
             skipNextLine = true;
           }
           else if (nextLine.indexOf("//ASM") >= 0) 
           {
             parsedLinesFrom = "ASM";

             skipNextLine = true;
             continuationCount = 0;
           }
           else
           {
             if ((parsedLinesFrom == "SYSUT") && (sysutFound))
                skipNextLine = true;
             else
             {
                sb.append(nextLine).append(NL_);
                skipNextLine = false;
             }
           }  // end else
         }      
       }  // end while

       if ((execCount == 1) && (needListing))
       {
         // No SYSIN statement in the custom JCL template
         // Need to add SYSIN information
         if (!sysinFound)
         {
           if ((!printLineFound) &&
              ((langType == "pls") || (langType == "plx"))) 
           {
             sb.append(asmPrtDD1).append(NL_);
             sb.append(asmPrtDD2).append(NL_);
             sb.append(asmMut1DD1).append(NL_);
             sb.append(asmMut1DD2).append(NL_);
           }

           String sysinDD1 = "//SYSIN             DD DISP=SHR,";
           String sysinDD2 = "//           DSN=" + pdsLangStr;
          
           sb.append(sysinDD1).append(NL_);
           sb.append(sysinDD2+"("+srcTarget.toUpperCase()+")");
           sb.append(NL_);
         }

         String slibDD1     = "//SYSLIB            DD DISP=SHR,";       
         String cblDSNStr   = "//         DD DSN=" + pdsLangStr + ",DISP=SHR"; 

         if ((parsedLinesFrom == "SYSLIB") && (libCount == 1))
         { 
           // add DSNs (.MACLOCAL and .MACEXPT)                 
           sb.append(syslibDD1).append(NL_);
           sb.append(syslibDD2).append(NL_);
           sb.append(syslibDD3).append(NL_);
           sb.append(syslibDD4).append(NL_);

           if (langType == "cobol") 
             sb.append(cblDSNStr).append(NL_);             

           // ASMLIB            DD DISP=SHR,
           // add DSNs (.MACLOCAL and .MACEXPT) 
           if ((langType == "pls") || (langType == "plx")) 
           {
             sb.append(asmlibDD1).append(NL_);
             sb.append(syslibDD2).append(NL_);
             sb.append(syslibDD3).append(NL_);
             sb.append(syslibDD4).append(NL_);
           }

         }
         else if ((parsedLinesFrom == "SYSLIB") && 
                  (libCount > syslibParsedCnt)) 
         {
           // add DSNs (.MACLOCAL and .MACEXPT)                 
           sb.append(syslibDD1).append(NL_);
           sb.append(syslibDD2).append(NL_);
           sb.append(syslibDD3).append(NL_);
           sb.append(syslibDD4).append(NL_);

           if (langType == "cobol") 
              sb.append(cblDSNStr).append(NL_);             
         }
         else if (!syslibFound) 
         {
           // No SYSLIB statement in the custom JCL template
           // Need to add SYSLIB information
           
           sb.append(slibDD1).append(NL_);
           sb.append(syslibDD2).append(NL_);
           sb.append(syslibDD3).append(NL_);
           sb.append(syslibDD4).append(NL_);
           
           if (langType == "cobol")
             sb.append(cblDSNStr).append(NL_);

           if (langType == "asm")
           {
             if (MvsProperties.prodAsmSysLib != null)
             {
               sb.append(genSyslibFormat(MvsProperties.prodAsmSysLib));
               sb.append(NL_);
             }
           }
 
           if (langType == "cobol")
           {
             if (MvsProperties.prodCblSysLib != null)
             {
               sb.append(genSyslibFormat(MvsProperties.prodCblSysLib));
               sb.append(NL_);
             }
           }

           if (langType == "pls")
           {
             if (MvsProperties.prodPlsSysLib != null)
             {
               sb.append(genSyslibFormat(MvsProperties.prodPlsSysLib));
               sb.append(NL_);
             }
           }
 
           if (langType == "plx")
           {
             if (MvsProperties.prodPlxSysLib != null)
             {
               sb.append(genSyslibFormat(MvsProperties.prodPlxSysLib));
               sb.append(NL_);
             }
           }

           if (MvsProperties.stndSysLib != null)
             sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);

           // ASMLIB            DD DISP=SHR,
           // add DSNs (.MACLOCAL and .MACEXPT)
           if ((langType == "pls") || (langType == "plx")) 
           {
             sb.append(asmlibDD1).append(NL_);
             sb.append(syslibDD2).append(NL_);
             sb.append(syslibDD3).append(NL_);
             sb.append(syslibDD4).append(NL_);

             if (langType == "pls")
             {
               if (MvsProperties.prodPlsSysLib != null)
               {
                 sb.append(genSyslibFormat(MvsProperties.prodPlsSysLib));
                 sb.append(NL_);
               }
             }
   
             if (langType == "plx")
             {
               if (MvsProperties.prodPlxSysLib != null)
               {
                 sb.append(genSyslibFormat(MvsProperties.prodPlxSysLib));
                 sb.append(NL_);
               }
             }
             
             if (MvsProperties.stndSysLib != null)
               sb.append(genSyslibFormat(MvsProperties.stndSysLib)).append(NL_);
           }

           needObjectInfo = true;
         }

         if (needObjectInfo) 
         {  
           if (langType == "asm")
           {
             sb.append(asmObjDD1).append(NL_);
             sb.append(objDD).append(NL_);
           
             sb.append(asmClrDD1).append(NL_);       
             sb.append(clrPrntDD).append(NL_);
             sb.append(asmUniDD1).append(NL_);
             sb.append(uniDataDD).append(NL_);
           }
           else if (langType == "cobol") 
           {
             sb.append(cblObjDD1).append(NL_);
             sb.append(objDD).append(NL_);
           } 
          else if (langType == "pls")
           {
             sb.append(plsObjDD1).append(NL_);
             sb.append(objDD).append(NL_);
            
             sb.append(plsClrDD1).append(NL_);                                
             sb.append(clrPrntDD).append(NL_);
           }
           else if (langType == "plx")
           {
             sb.append(plxObjDD1).append(NL_);        
             sb.append(objDD).append(NL_);
  
             sb.append(plxClrDD1).append(NL_);                                
             sb.append(clrPrntDD).append(NL_);
           }
         }

         // listing information
         sb.append(comment5).append(NL_);
         sb.append(comment0).append(NL_);                     
         sb.append(lstStr).append(NL_);
         sb.append(comment0).append(NL_);
         sb.append(comment5).append(NL_);
         sb.append(lstExecStr).append(NL_);
         sb.append(comment0).append(NL_);
         sb.append(lstSysut1DD1).append(NL_); 
         if ((langType == "pls") ||
             (langType == "plx")) 
         {
            sb.append(lstSysut1DD2).append(NL_); 
         }                     
         sb.append(lstSysut2DD1).append(NL_); 
         sb.append(lstSysut2DD2).append(NL_);     
         sb.append(sysinDumStr).append(NL_);
         sb.append(sysinPrntStr).append(NL_);
         sb.append(comment0).append(NL_);
       }
     }
     catch (FileNotFoundException ex)
     {
       System.err.println("JCL file " + jclUserTemplate + " not found.");
       System.err.println(ex);
       System.exit(-1);
     }
     catch (IOException e)
     {
       System.err.println("Error reading " + jclUserTemplate);
       System.err.println(e);
       System.exit(-1);
     }

     return sb.toString();

  }

  /**
   * Generate the proper format for syslib
   *
   * @param syslibString: String with the syslib info
   *                    example:  SYS1.MACLIB SYS1.MODGEN
   *
   * @return syslibDD:  Output String for syslib DD
   *               (i.e //             DD DSN=SYS1.MACLIB,DISP=SHR 
   *                    //             DD DSN=SYS1.MODGEN,DISP=SHR )
   *                              
   **/
  private static String genSyslibFormat(String syslibString) 
  {
    String sysLine = null;
    String syslibDD = "";

    StringTokenizer wordTokens = new StringTokenizer( syslibString );
    int numOfWordsInString = wordTokens.countTokens();

    String syslib = (String) wordTokens.nextToken();      
    syslibDD   = "//             DD DSN=" + syslib.toUpperCase() + ",DISP=SHR";

    while ( wordTokens.hasMoreTokens() )
    {
      String syslib2 = (String) wordTokens.nextToken();  
      sysLine =  "//             DD DSN=" + syslib2.toUpperCase() + ",DISP=SHR";
      
      syslibDD    += "\n";
      syslibDD    += sysLine;
    }

    return syslibDD;
  } // end genSyslibFormat

  /**    
   * Generate the JCL to delete the temporary PDS created by ALLOCLIB step
   * and return the string command. 
   *
   * @param pdsLangStr: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param pdsLMacStr: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param pdsEMacStr: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param pdsObjStr: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param pdsClrPrtStr: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT) 
   * @param pdsLstStr: PDS for listing files (i.e. USERID.SANDBOX.LISTING)   
   * @param pdsUniStr: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)
   *            
   */
  private String deletePds(String pdsLangStr, String pdsLMacStr, String pdsEMacStr, 
                           String pdsObjStr, String pdsClrPrtStr, String pdsLstStr, 
                           String pdsUniStr)
  {
    String delPdsStmt = "//DELPDS   EXEC PGM=IDCAMS,COND=(20,LE,FTPFILE)";
    String sysprintDD = "//SYSPRINT DD SYSOUT=*";
    String sysinDD    = "//SYSIN    DD *";
    String delcmd     = "  DELETE ";

    StringBuffer sb = new StringBuffer(350);

    sb.append(delPdsStmt).append(NL_);
    sb.append(sysprintDD).append(NL_);
    sb.append(sysinDD).append(NL_);
    sb.append(delcmd).append("'").append(pdsLangStr).append("'").append(NL_);
    sb.append(delcmd).append("'").append(pdsLMacStr).append("'").append(NL_);
    sb.append(delcmd).append("'").append(pdsEMacStr).append("'").append(NL_);
    sb.append(delcmd).append("'").append(pdsObjStr).append("'").append(NL_);       
    sb.append(delcmd).append("'").append(pdsLstStr).append("'").append(NL_);
    sb.append(delcmd).append("'").append(pdsClrPrtStr).append("'").append(NL_);
    sb.append(delcmd).append("'").append(pdsUniStr).append("'").append(NL_);

    return sb.toString();

  }  // deletePDS()

  /**
   * Format CFLAGS line so it does not exceed column 72.
   */
  private static String formatCFlagsLine(String line)
  {
       
    StringBuffer sb = new StringBuffer(160);

    int lineLength = line.length();

    if (lineLength <= 72)
    {
      sb.append(line);
    }    
    else
    {
      int lastComma = line.lastIndexOf(',');

      while (lastComma > 70)
      {
        lastComma = line.lastIndexOf(',', lastComma - 1);
      }
      line = line.substring(0, lastComma) + "'," + NL_ +
                "//  '" + line.substring(lastComma + 1);

      sb.append(line);
    }

    return sb.toString();
  } // formatCFlagsLine()

  /**
   * Generate JCL for the allocation step via IEFBR14.
   * This is the default allocation task, if the user decides not to use
   * the ODE REXX Script.
   *
   * @param rexxExecName the Rexx exec to be run
   * @param stepName the step name or null, in which case the step name will
   *                 be the ALLOCLIB name
   * @param srdPds: PDS for source files (i.e. USERID.SANDBOX.SRC)
   * @param lmacPds: PDS for local macros (i.e. USERID.SANDBOX.MACLOCAL)
   * @param emacPds: PDS for export macros (i.e. USERID.SANDBOX.MACEXPT)
   * @param objPds: PDS for object         (i.e. USERID.SANDBOX.O)
   * @param lstPds: PDS for listing files (i.e. USERID.SANDBOX.LISTING)   
   * @param clrPrtPds: PDS for PLS/PLS dependency (i.e. USERID.SANDBOX.CLRPRINT)    
   * @param unidataPds: PDS for for ASM dependency   (i.e. USERID.SANDBOX.UNIDATA)   
   *
   */
  private String genAllocStepInJcl(String execName, String stepName, String srcPds,
                                   String lmacPds, String emacPds, String objPds,
                                   String lstPds, String clrPrtPds, String unidataPds)
  {
    String spaceVal;
    String primQty;
    String secondQty;
    String dirBlocks;
    String recFormat;
    String recLength;
    String blkSize;     
    // listing info
    String lstSpaceVal;
    String lstPrimQty;
    String lstSecondQty;
    String lstDirBlocks;
    String lstBlkSize;     

    // space type         
    if (MvsProperties.spaceType != null)
    {
      // space type     
      if (MvsProperties.spaceType.equalsIgnoreCase("TRACK") ||
          MvsProperties.spaceType.equalsIgnoreCase("TRK"))
      {
        spaceVal = "TRK";
      }
      else
        spaceVal = "CYL";
    }
    else
    {
      spaceVal = "CYL";
    }

    // primary tracks or cylinder
    if (MvsProperties.primaryQty != null)
      primQty = MvsProperties.primaryQty;
    else
      primQty = "34";

    // secondary tracks or cylinder
    if (MvsProperties.secondaryQty != null) 
      secondQty = MvsProperties.secondaryQty;
    else
      secondQty = "4";

    // directory blocks 
    if (MvsProperties.directoryBlks != null) 
      dirBlocks = MvsProperties.directoryBlks;
    else
      dirBlocks = "30";

    // block size
    if (MvsProperties.blockSize != null) 
      blkSize = MvsProperties.blockSize;
    else
      blkSize = "32720";


    // Setting Listing PDS information
    if (MvsProperties.lstSpaceType != null)        
    {
      // space type
      if (MvsProperties.lstSpaceType.equalsIgnoreCase("TRACK") ||
          MvsProperties.lstSpaceType.equalsIgnoreCase("TRK"))        
        lstSpaceVal = "TRK";
      else
        lstSpaceVal = "CYL";
    }
    else
    {
      lstSpaceVal = "CYL";
    }

    // primary tracks or cylinder for listing
    if (MvsProperties.lstPrimaryQty != null)
      lstPrimQty = MvsProperties.lstPrimaryQty;
    else
      lstPrimQty = "81";

    // secondary tracks or cylinder for listing
    if (MvsProperties.lstSecondQty != null) 
      lstSecondQty = MvsProperties.lstSecondQty;
    else
      lstSecondQty = "9";

    // directory blocks for listing 
    if (MvsProperties.lstDirBlks != null) 
      lstDirBlocks = MvsProperties.lstDirBlks;
    else
      lstDirBlocks = "10";

    // block size for listing
    if (MvsProperties.lstBlockSize != null) 
      lstBlkSize = MvsProperties.lstBlockSize;
    else
      lstBlkSize = "13030";


    String execStmt    = "EXEC PGM=IEFBR14";
    String outsrcDD1   = "//OUTSRC  DD DISP=(NEW,CATLG),";
    String outsrcDD2   = "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE="+blkSize+"),";
    String outsrcDD3   = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";    
    String outsrcDD4   = "//            DSN="+srcPds;
    String maclocalDD1 = "//LOCMAC  DD DISP=(NEW,CATLG),";
    String maclocalDD2 = "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE="+blkSize+"),";
    String maclocalDD3 = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";
    String maclocalDD4 = "//            DSN="+lmacPds;
    String macexptDD1  = "//EXPTMAC DD DISP=(NEW,CATLG),";
    String macexptDD2  = "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE="+blkSize+"),";
    String macexptDD3  = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";
    String macexptDD4  = "//            DSN="+emacPds;
    String objectDD1   = "//OBJECT  DD DISP=(NEW,CATLG),";
    String objectDD2   = "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE="+blkSize+"),";
    String objectDD3   = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";
    String objectDD4   = "//            DSN="+objPds;
    String listingDD1  = "//LISTING DD DISP=(NEW,CATLG),";
    String listingDD2  = "//            DCB=(RECFM=VBA,LRECL=137,BLKSIZE="+lstBlkSize+"),";
    String listingDD3  = "//            SPACE=("+lstSpaceVal+",("+lstPrimQty+","+lstSecondQty+","+lstDirBlocks+")),";
    String listingDD4  = "//            DSN="+lstPds;
    String clrprintDD1 = "//CLRPRNT DD DISP=(NEW,CATLG),";
    String clrprintDD2 = "//            DCB=(RECFM=FBA,LRECL=133,BLKSIZE=32718),";
    String clrprintDD3 = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";
    String clrprintDD4 = "//            DSN="+clrPrtPds;
    String unidataDD1  = "//UNIDATA DD DISP=(NEW,CATLG),";
    String unidataDD2  = "//            DCB=(RECFM=FB,LRECL=80,BLKSIZE="+blkSize+"),";
    String unidataDD3  = "//            SPACE=("+spaceVal+",("+primQty+","+secondQty+","+dirBlocks+")),";
    String unidataDD4  = "//            DSN="+unidataPds;
    String sysprintDD  = "//SYSPRINT DD SYSOUT=*";

    // pad stepname to 9 characters
    StringBuffer stepNameSB = new StringBuffer(9);
    if (stepName == null)
    {
      stepNameSB.append(execName);
    }
    else
    {
      stepNameSB.append(stepName);
    }
    for (int i=execName.length()+1; i <= 9; i++)
    {
      stepNameSB.append(' ');
    }

    StringBuffer sb = new StringBuffer(350);

    sb.append("//").append(stepNameSB).append(execStmt).append(NL_);   
    sb.append(outsrcDD1).append(NL_);
    sb.append(outsrcDD2).append(NL_);
    sb.append(outsrcDD3).append(NL_);
    sb.append(outsrcDD4).append(NL_);
    sb.append(maclocalDD1).append(NL_);
    sb.append(maclocalDD2).append(NL_);
    sb.append(maclocalDD3).append(NL_);
    sb.append(maclocalDD4).append(NL_);
    sb.append(macexptDD1).append(NL_);
    sb.append(macexptDD2).append(NL_);
    sb.append(macexptDD3).append(NL_);
    sb.append(macexptDD4).append(NL_);
    sb.append(objectDD1).append(NL_);
    sb.append(objectDD2).append(NL_);
    sb.append(objectDD3).append(NL_);
    sb.append(objectDD4).append(NL_);
    sb.append(listingDD1).append(NL_);
    sb.append(listingDD2).append(NL_);
    sb.append(listingDD3).append(NL_);
    sb.append(listingDD4).append(NL_);
    sb.append(clrprintDD1).append(NL_);
    sb.append(clrprintDD2).append(NL_);
    sb.append(clrprintDD3).append(NL_);
    sb.append(clrprintDD4).append(NL_);
    sb.append(unidataDD1).append(NL_);
    sb.append(unidataDD2).append(NL_);
    sb.append(unidataDD3).append(NL_);
    sb.append(unidataDD4).append(NL_);    
    sb.append(sysprintDD).append(NL_);

    return sb.toString();

  }  // genAllocStepInJcl()

  /**
   * Format line so it does not exceed column 70.
   * If line execeed column 70, create local change
   * directory string for FTP
   */
  private String formatChngDirLine70(String line)
  {
    String dirsep = System.getProperty("file.separator");

    StringBuffer sb = new StringBuffer(160);
    
    String cdLine = "cd " + line;
    String chngDirLine = "LCD " + line;
    int lastSlash;

    if (differentMachines_)
    {
       lastSlash = cdLine.lastIndexOf(dirsep);  
    }
    else
    {    
       lastSlash = chngDirLine.lastIndexOf(dirsep);  
    }

    if (lastSlash <= 65) 
    {
      if (differentMachines_) 
        cdLine = cdLine.substring(0, lastSlash + 1);       
      else
        chngDirLine = chngDirLine.substring(0, lastSlash + 1);       
    }
    else 
    {
      if (differentMachines_) 
      {
        while (lastSlash > 65)
        {      
          lastSlash = cdLine.lastIndexOf(dirsep, lastSlash - 1);
        }
   
        cdLine = cdLine.substring(0, lastSlash) + NL_ +
                      "cd " + cdLine.substring(lastSlash + 1);
      }
      else
      {
        while (lastSlash > 65)
        {      
          lastSlash = chngDirLine.lastIndexOf(dirsep, lastSlash - 1);
        }
   
        chngDirLine = chngDirLine.substring(0, lastSlash) + NL_ +
                       "LCD " + chngDirLine.substring(lastSlash + 1);
      }
    }

    if (differentMachines_) 
      sb.append(cdLine);
    else
      sb.append(chngDirLine);

    return sb.toString();

  } //formatChngDirLine70()

  //--------------------------------------------------------------------------

  // contructor args
  private MvsPackageData pkgData_       = null;
  private Vector pkgDataVector_ = null;

  // global values
  private String jobName_ = null;
  private MvsJobInfo jobInfo_ = null;
  private MvsJobInfo linkEditJobInfo_ = null;
  private String jclinDistLib_ = null;
  private String prelinkDistLib_ = null;
  private boolean genLinkEdit_ = false;
  private Vector lkedKeys = new Vector();
  private Vector lkedDistNames = new Vector();
  private boolean differentMachines_ = false;
  // constant values
  private final int vplBlksize = 8800;  // blocksize for VPL pds for service
  private final int objBlksize = 8800;  // blocksize for OBJ pds for service

  private static final String NL_ =
    System.getProperty("line.separator");
  private static final char FS_ =
    System.getProperty("file.separator").charAt(0);
  private static String commentLine_ = "//*---------------------------------" +
                                       "------------------------------------";
  private static char[] jobChars_    = {'1','2','3','4','5','6','7','8','9',
                                        'A','B','C','D','E','F','G','H','I',
                                        'J','K','L','M','N','O','P','Q','R',
                                        'S','T','U','V','W','X','Y','Z'};
}

