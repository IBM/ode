/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2000.  All Rights Reserved.
 *
 *****************************************************************************/
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.*;
import java.util.*;
import PkgFile;

/**
 * Main driver for FileEntity stanza creation.
 * @author Chad Holliday
**/

class MakeCmf
{

  public static void main(String[] args)
  {
    // validate program arguments
    if (args.length != 3)
    {
      System.out.println("Usage: MakeCmf pkgmetadatalist productstanzafile outputfile");
      System.out.println("where pkgmetadatalist  = the file w/ pkging metadata ");
      System.out.println("      productstanzafile  = the product data stanza file to be used for the product data in the CMF");
      System.out.println("      outputfile  = the CMF file be written");
      System.exit(1);
    }

    int    returnCode = 0;
    String pkgmetadatalist = args[0];
    String productStanzaFile = args[1];
    String outputfile = args[2];
    String productStanzas = "";
    String fileStanza = "";
    String srcData = "";
    Vector fmidList  = new Vector();
    Hashtable pkgFileHash = new Hashtable();
    Hashtable fmidFileList = new Hashtable();

    try
    {
      BufferedWriter bw  = new BufferedWriter(new FileWriter(outputfile));

      System.out.println("Reading in Part Metadata File.");
      Hashtable pkgHt = readInPkgMetadataFile( pkgmetadatalist, fmidFileList );

      System.out.println("Reading in Product Metadata and Creating File List for Each Product.");
      productStanzas = readInProductStanzaFile( productStanzaFile, fmidFileList );
      System.out.println("Writing Product Data to CMF File.");
      writeProductData( productStanzas, bw );

      System.out.println("Iterating Through Part Metadata and Creating File Stanzas.");
      Enumeration pkgHtIterator = pkgHt.keys();
      while (pkgHtIterator.hasMoreElements())
      {
        String pkgKey = (String)pkgHtIterator.nextElement();
        Vector pkgData = (Vector)pkgHt.get( pkgKey );

        if (pkgData.size() == 1) // Don't need SouceData/PackageData stanzas
        {
            fileStanza = createFileStanza( (PkgFile)pkgData.elementAt( 0 ), 
                                           true );
            writeFileData( fileStanza, bw );
        }
        else // need SouceData/PackageData stanzas
        {
          srcData = createSrcData( pkgKey );
          writeFileData( srcData, bw );
          for (int i = 0; i < pkgData.size(); i++)
          {
            fileStanza = createFileStanza( (PkgFile)pkgData.elementAt( i ), 
                                           false );
            writeFileData( fileStanza, bw );
          }
          writeFileData( "}\n", bw );
        }
      }
    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing product data to ");
      System.out.println(outputfile + "\n");
    }
    return;
  } //main()


  //***************************************************************************
  // Read in Package Metadata from input file
  //***************************************************************************
  private static Hashtable readInPkgMetadataFile ( String inputfn, 
                                                   Hashtable fmidFileList )
  {
    BufferedReader inputFile_ = null;
    Vector pkgDatas;
    String fileList;
    Hashtable pkgFileHash = new Hashtable();

    try
    {
      inputFile_ = new BufferedReader( new FileReader(inputfn) );
    }
    catch( FileNotFoundException e )
    {
      System.out.println("File not found ");
      System.out.println(inputfn + "\n");
    }
    catch( IOException e )
    {
      System.out.println("IO Exception reading ");
      System.out.println(inputfn + "\n");
    }

    // Read the input file line by line and store the data into the
    // output Hashtable
    try
    {
      String lineFromFile = inputFile_.readLine();
      while ( lineFromFile != null )
      {
        lineFromFile = lineFromFile.trim();
        if ( lineFromFile.length() > 0 )
        {
          // Build part hash table 
          PkgFile pf = new PkgFile( lineFromFile );
          String pkgDataKey = pf.getSourceDir().trim();
          if (!pkgDataKey.endsWith("/"))
            pkgDataKey += "/";
          pkgDataKey += pf.getLongFilename();

          if ( pkgFileHash.containsKey( pkgDataKey ) )
            pkgDatas = (Vector)pkgFileHash.get( pkgDataKey );
          else
            pkgDatas = new Vector();
          pkgDatas.addElement( pf );
          pkgFileHash.put( pkgDataKey, pkgDatas );

          // Build fmid file list
          String fmids = pf.getFmidList().toUpperCase();
          StringTokenizer st = new StringTokenizer(fmids, ",");
          String currentFile =  "  < " + pf.getSourceDir() + "/" +
                              pf.getLongFilename() + " > \n";
          while (st.hasMoreTokens())
          {
            String currentFmid = st.nextToken().toUpperCase();
            if ( fmidFileList.containsKey( currentFmid ) )
              fileList = (String)fmidFileList.get( currentFmid ) + currentFile;
            else
              fileList = currentFile;
            fmidFileList.put( currentFmid, fileList );
          }
        }
        lineFromFile = inputFile_.readLine();
      }
    }
    catch( IOException e )
    {
      System.out.println("IO Exception reading ");
      System.out.println(inputfn + "\n");
    }
    return pkgFileHash;

  } //

  //***************************************************************************
  // Read in Package Metadata from input file
  //***************************************************************************
  private static String readInProductStanzaFile (String inputfn, 
                                                 Hashtable fmidFileList )
  {
    String productStanzas = "";
    BufferedReader inputFile_ = null;

    try
    {
    inputFile_ = new BufferedReader( new FileReader(inputfn) );
    }
    catch( FileNotFoundException e )
    {
      System.out.println("File not found ");
      System.out.println(inputfn + "\n");
    }
    catch( IOException e )
    {
      System.out.println("IO Exception reading ");
      System.out.println(inputfn + "\n");
    }

    // Read the input file line by line and store the data into the
    // output string.
    try
    {
      String lineFromFile = inputFile_.readLine();
      String tmpline;
      while ( lineFromFile != null )
      {
        tmpline = lineFromFile.trim();
        if ( tmpline.indexOf("immChildFiles" ) != -1 )
        {
         // Replace %FMID% with fmid file list
          StringTokenizer st = new StringTokenizer(tmpline);
          String newLineFromFile = "";
          while ( st.hasMoreTokens() )
          {
            String token = st.nextToken();
            if ( token.startsWith("%") && token.endsWith( "%" ) )
            {
              String fmid = token.substring( 1, token.length() - 1);
              if (fmidFileList.containsKey( fmid ))
                token = "  " + (String)fmidFileList.get( fmid );
            }
            newLineFromFile = newLineFromFile + " " + token;
          }
          lineFromFile = newLineFromFile;
        }
        productStanzas = productStanzas + lineFromFile + "\n";
        lineFromFile = inputFile_.readLine();
      }
    }
    catch( IOException e )
    {
      System.out.println("IO Exception reading ");
      System.out.println(inputfn + "\n");
    }
  return productStanzas;

  } //
  //***************************************************************************
  // Create Strings of Package Metadata in File Stanza and List of Files Formats
  //***************************************************************************
  private static String createFileStanza( PkgFile pkgFile,
                                          boolean singlePkgData )
  {
    String tmpStr;
    String fileStanza = "";

    // Create the File Stanzas for this file to the output string.
    if (singlePkgData)
    {
      fileStanza = "file { \n";

      tmpStr = pkgFile.getSourceDir();
      if (tmpStr != null)
        fileStanza += "   sourceDir = \"" + tmpStr + "\";\n";
      tmpStr = pkgFile.getLongFilename();
      if (tmpStr != null)
        fileStanza += "   sourceFile = \"" + tmpStr + "\";\n";
    }
    else
    {
      fileStanza = "  PackageData { \n";
    }

    tmpStr = pkgFile.getPartType();
    if (tmpStr != null)
      fileStanza += "   fileType = '" + tmpStr + "';\n";

    tmpStr = pkgFile.getShortFilename();
    if (tmpStr != null)
      fileStanza += "   targetFile = \"" + tmpStr + "\";\n";

    tmpStr = pkgFile.getDistlib();
    if (tmpStr != null)
      fileStanza += "   targetDir = \"" + tmpStr + "\";\n";

    tmpStr = pkgFile.getPermissions();
    
    // Remove the commas from the permissions string.
    if (tmpStr != null)
    {
      char permissions[] = tmpStr.toCharArray();
      int idx = 0;
      StringBuffer sb = new StringBuffer();
      while (idx < permissions.length)
      {
        if (permissions[idx] != ',')
          sb.append(permissions[idx]);
        idx++;
      }
      tmpStr = sb.toString();
      fileStanza += "   permissions = \"" + tmpStr + "\";\n";
    }
    
    // partInfo
    fileStanza += "   partInfo = [\n";
    if ( (pkgFile.getPartType() != null)  && 
          pkgFile.getPartType().toUpperCase().startsWith("HFS") )
    {
      tmpStr = pkgFile.getLink();
      if (tmpStr != null)
        fileStanza += "       (\"link\" \"\'" + tmpStr + "\'\")\n";

      tmpStr = pkgFile.getSympath();
      if (tmpStr != null)
        fileStanza += "       (\"sympath\" \"\'" + tmpStr + "\'\")\n";

      tmpStr = pkgFile.getSymlink();
      if (tmpStr != null)
        fileStanza += "       (\"symlink\" \"\'" + tmpStr + "\'\")\n";

      tmpStr = pkgFile.getCopyType();
      if (tmpStr != null)
        fileStanza += "       (\"" + tmpStr + "\")\n";
    }      

    // Have two syslibs for MOD parts
    if ( (pkgFile.getPartType() != null)  && 
          pkgFile.getPartType().toUpperCase().equals("MOD") )
    {
      tmpStr = pkgFile.getHfsSyslib();
      if (tmpStr != null)
        fileStanza += "       (\"hfssyslib\" \"" + tmpStr + "\")\n";

      tmpStr = pkgFile.getPdsSyslib();
      if (tmpStr != null)
        fileStanza += "       (\"pdssyslib\" \"" + tmpStr + "\")\n";
    }
    else  // Use first (hfssyslib) for non-Mod parts
    {
      tmpStr = pkgFile.getHfsSyslib();
      if (tmpStr != null)
        fileStanza += "       (\"syslib\" \"" + tmpStr + "\")\n";
    }
    
    fileStanza += "   ]; \n";

    tmpStr = pkgFile.getUserid();
    if (tmpStr != null)
      fileStanza += "   userId = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getGroupid();
    if (tmpStr != null)
      fileStanza += "   groupId = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getComp();
    if (tmpStr != null)
      fileStanza += "   comp = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getShipType();
    if (tmpStr != null)
    {
      fileStanza += "   shipType = [";
      StringTokenizer strtok = new StringTokenizer( tmpStr, "," );
      while (strtok.hasMoreTokens())
        fileStanza += " \'" + strtok.nextToken() + "\'" ;
      fileStanza += " ];\n";
    }

    tmpStr = pkgFile.getCopyType();
    if (tmpStr != null)
      fileStanza += "   hfsCopyType = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getLkedto();
    if (tmpStr != null)
      fileStanza += "   lkedTo = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getHfslkedname();
    if (tmpStr != null)
      fileStanza += "   hfsLkedName = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getPdslkedname();
    if (tmpStr != null)
      fileStanza += "   pdsLkedName = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getLkedrc();
    if (tmpStr != null)
      fileStanza += "   lkedRc = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getHfsalias();
    if (tmpStr != null)
    {
      fileStanza += "   hfsAlias = [";
      StringTokenizer strtok = new StringTokenizer( tmpStr, "," );
      while (strtok.hasMoreTokens())
        fileStanza += " \"" + strtok.nextToken() + "\"" ;
      fileStanza += " ];\n";
    }

    tmpStr = pkgFile.getPdsalias();
    if (tmpStr != null)
    {
      fileStanza += "   pdsAlias = [";
      StringTokenizer strtok = new StringTokenizer( tmpStr, "," );
      while (strtok.hasMoreTokens())
        fileStanza += " \"" + strtok.nextToken() + "\"" ;
      fileStanza += " ];\n";
    }

    tmpStr = pkgFile.getSetcode();
    if (tmpStr != null)
      fileStanza += "   setCode = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getSyslibs();
    if (tmpStr != null)
    {
      fileStanza += "   sysLibs = [";
      StringTokenizer strtok = new StringTokenizer( tmpStr, "," );
      while (strtok.hasMoreTokens())
        fileStanza += " \"" + strtok.nextToken() + "\"" ;
      fileStanza += " ];\n";
    }

    tmpStr = pkgFile.getInclude();
    if (tmpStr != null)
    {
      fileStanza += "   include = [";
      StringTokenizer strtok = new StringTokenizer( tmpStr, "," );
      while (strtok.hasMoreTokens())
        fileStanza += " \"" + strtok.nextToken() + "\"" ;
      fileStanza += " ];\n";
    }

    tmpStr = pkgFile.getOrder();
    if (tmpStr != null)
      fileStanza += "   order = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getEntry();
    if (tmpStr != null)
      fileStanza += "   entry = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getExtattr();
    if (tmpStr != null)
      fileStanza += "   extAttr = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getJclinmode();
    if (tmpStr != null)
      fileStanza += "   jclinMode = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getLkedparms();
    if (tmpStr != null)
      fileStanza += "   lkedParms = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getJclinlkedparms();
    if (tmpStr != null)
      fileStanza += "   jclinLkedParms = \"" + tmpStr + "\";\n" ;

    tmpStr = pkgFile.getVplsecurity();
    if (tmpStr != null)
      fileStanza += "   vplSecurity = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getVplpartqual();
    if (tmpStr != null)
      fileStanza += "   vplPartqual = \'" + tmpStr + "\';\n" ;

    tmpStr = pkgFile.getLibraryDD();
    if (tmpStr != null)
      fileStanza += "   libraryDD = \"" + tmpStr + "\";\n" ;
    
    tmpStr = pkgFile.getSysLibsLibraryDD();
    if (tmpStr != null)
    {
      fileStanza += "   sysLibsLibraryDD = [";
      StringTokenizer strtok = new StringTokenizer(tmpStr, ",");
      while (strtok.hasMoreTokens())
        fileStanza += " \"" + strtok.nextToken() + "\"";
      fileStanza += " ];\n";
    }
    
    tmpStr = pkgFile.getSideDeckAppendDD();
    if (tmpStr != null)
    {
      fileStanza += "   sideDeckAppendDD = \"" + tmpStr + "\";\n";
    }

    fileStanza += " }";

  return fileStanza;

  } //


  //***************************************************************************
  // Create file stanza with SourceData inforamtion
  //***************************************************************************
  private static String createSrcData( String pkgDataKey )
  {

    // Create the File Stanzas for this file to the output string.
    String fileStanza = "file { \n";
    fileStanza += "  SourceData { \n";

    int val = pkgDataKey.lastIndexOf( "/" );
    String srcdir = pkgDataKey.substring( 0, val );
    String srcfile = pkgDataKey.substring( val+1 );

    if (srcdir != null)
      fileStanza += "   sourceDir = \"" + srcdir + "\";\n";

    if (srcfile != null)
      fileStanza += "   sourceFile = \"" + srcfile + "\";\n";
    
    fileStanza += "  }";

    return fileStanza;

  }

  //***************************************************************************
  // Write Strings of Package Metadata to Output Files
  //***************************************************************************
  private static void writeFileData( String FileStanza,
                                     BufferedWriter bw)
  {
    try
    {
    // Write the File Stanza for this file to the buffered writer.
    bw.write(FileStanza);
    bw.newLine();
    bw.flush();

    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing file stanza" + "\n");
    }
    return;

  } //

  //***************************************************************************
  // Write Product Stanzas to CMF File
  //***************************************************************************
  private static void writeProductData( String ProductData,
                                        BufferedWriter bw )
  {
    try
    {

    // Write the product data to the output file - new file
    bw.write(ProductData);
    bw.newLine();
    bw.flush();

    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing product data" + "\n");
    }
    return;

  } //
}

