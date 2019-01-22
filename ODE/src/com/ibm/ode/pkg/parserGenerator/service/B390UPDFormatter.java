/********************************************************************
 *
 *******************************************************************/
package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.PackageData;
import com.ibm.ode.pkg.parserGenerator.ReqType;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 *
 */
public class B390UPDFormatter extends Formatter
{
  private String attribSeparator = ",";
  private String assignValue = "=";
  private Vector mcsFiles = new Vector();
  public static final boolean required = true;
  public static final boolean optional = false;
  public static final String  nextLine = ",\n   ";
  public static final String  nextParm = ",";

  public B390UPDFormatter( B390NodeHandler nodeHandler )
     throws GeneratorException
  {
     super(nodeHandler);
  }

  public String formatProductInfo() 
    throws GeneratorException
  {
    String fileInfo = getUPDHeader();
    return fileInfo;
  }
  
  private static String getUPDHeader() 
  {  
    //  The old UPD Header format used before defect 3189 changes.
    //  String header = "OP=SET,@PLANID=3 \nOP=DO,BLDLVL=0 ";
    String header = "OP=SET,@PLANID=3 ";
    return header;
  }
  
  public Vector getMcsFiles() 
  {
    return mcsFiles;
  }
  
  public String formatFileInfo() 
    throws GeneratorException
  {
    String fileInfo = "";
    mcsFiles = new Vector();

    // Ensure source file exists, ignore if not
    File toStageFile = 
        new File( nodeHandler.getToStageDir() + nodeHandler.getSourceDir(), 
                  nodeHandler.getSourceFile());
    if (!toStageFile.exists()) return fileInfo;

    //Iterate through multiple PackageData stanzas              
    ArrayList packageDataArray = nodeHandler.getPackageData();
    ListIterator packageDataIterator;
    if (packageDataArray != null)
    {
      if (!packageDataArray.isEmpty())
      {
        packageDataIterator = packageDataArray.listIterator();
        while (packageDataIterator.hasNext())
        {
          PackageData packageData=(PackageData)packageDataIterator.next();
          if (filtered(packageData)) continue;
          fileInfo += formatPackageData(packageData);
        } //end for
      } //end if
    }
    return fileInfo;
  }
  
  private String formatPackageData( PackageData packageData ) 
    throws GeneratorException
  {
    String    fileStart = "\nOP=UPD";
    boolean   mcsFileNeeded = false; 
    String    partType = this.getPartType(packageData, required).trim().toUpperCase();
    String    module   = this.getModule(packageData, required).trim().toUpperCase();
    String    distLib  = formatPath(this.getDistLib(packageData, required).trim().toUpperCase());
    ArrayList sysLib   = this.getSysLib(packageData);
    ArrayList link     = this.getLink(packageData);
    ArrayList symlink  = this.getSymLink(packageData);
    ArrayList sympath  = this.getSymPath(packageData);
    String    pathMode = this.getPathMode(packageData, optional).trim();
    String    mcsData  = this.getHFSCopyType(packageData, optional).trim().toUpperCase();
    String    leChar   = "";
    String    distSrc;

    if (partType.equalsIgnoreCase("mod"))
      distSrc = "OBJ";
    else
      distSrc = "C1";
    
    String fileInfo=fileStart;
    fileInfo += nextParm + "CLASS=" + partType;
    fileInfo += nextParm + "MOD=" + module;
    fileInfo += nextLine + "DISTNAME(0)=" + module;
    fileInfo += nextLine + "DISTLIB(0)=" + distLib;
    fileInfo += nextLine + "DISTSRC(0)=" + distSrc;
    fileInfo += nextLine + "BLDLVL=0";
    
    // Check if this is a new part
    String tempfile = distLib + "." + module;  // Separate with '.' for now
    if (((B390NodeHandler)nodeHandler).getNewFiles().contains(tempfile))
    {
      fileInfo += nextLine + "NEW=ON";
      // hard-code DISTTYPE=C for now
      fileInfo += nextLine + "DISTTYPE=C";
    }

    // Separate pathmode entries with commas
    if (pathMode != null && pathMode.length() != 0)
    {
      String formatedPathMode = "";
      for (int i = 0; i < pathMode.length(); i++) 
      {
        formatedPathMode += pathMode.substring(i, i + 1);
        if (i < (pathMode.length() - 1)) formatedPathMode += ",";
      }
      pathMode = formatedPathMode;
    }

    // LINK info
    if (link != null && !link.isEmpty())
    {
      String linkText = "";
      ListIterator packageDataIterator = link.listIterator();
      while (packageDataIterator.hasNext())           
      {
        String linkElement = (String)packageDataIterator.next();
        linkText += linkElement;
        if (packageDataIterator.hasNext()) 
          linkText += " ";
      }

      // Use 39 for length checking, 50 char limit - 11 for LECHAR text

      if (linkText != null && linkText.length() != 0)
      {
        if (linkText.length() > 39) 
        {
          mcsFileNeeded = true;  
          leChar = "LINK(" + linkText + ")";
        }
        else
            //leChar="'LINK(\""+linkText+"\")'";
          leChar = "LINK(" + linkText + ")";
      }
    }

    // SYMLINK info
    if (symlink != null && !symlink.isEmpty())
    {
      String symlinkText = "";
      ListIterator packageDataIterator = symlink.listIterator();
      while (packageDataIterator.hasNext())           
      {
        String symlinkElement = (String)packageDataIterator.next();
        symlinkText += symlinkElement;
        if (packageDataIterator.hasNext()) 
          symlinkText += " ";
      }

      if (symlinkText != null && symlinkText.length() != 0)
      {
        if ((symlinkText.length() > 39) || mcsFileNeeded)
        {
          mcsFileNeeded=true;  
          if (leChar != "")
            leChar += "\n";
          leChar += "SYMLINK(" + symlinkText + ")";
        }
        else
        {
          if (leChar != "")
            leChar += nextLine;
          //leChar += "'SYMLINK(\""+symlinkText+"\")'";
          leChar += "SYMLINK(" + symlinkText + ")";
        }
      }
    }

    // SYMPATH info
    if (sympath != null && !sympath.isEmpty())
    {
      String sympathText = "";
      ListIterator packageDataIterator=sympath.listIterator();
      while (packageDataIterator.hasNext())           
      {
        String sympathElement = (String)packageDataIterator.next();
        sympathText += sympathElement;
        if (packageDataIterator.hasNext()) 
          sympathText += " ";
      }

      if (sympathText != null && sympathText.length() != 0)
      {
        if ((sympathText.length() > 39) || mcsFileNeeded)
        {
          mcsFileNeeded = true;  
          if (leChar != "")
            leChar += "\n";
          leChar += "SYMPATH(" + sympathText + ")";
        }
        else
        {
          if (leChar != "")
            leChar += nextLine;
          //leChar += "'SYMPATH(\""+sympathText+"\")'";
          leChar += "SYMPATH(" + sympathText + ")";
        }
      }
    }

    // PARM(PATHMODE info)
    if (partType.equalsIgnoreCase("hfs"))
    {
      if (pathMode != null && pathMode.length() != 0)
      {
        if (mcsFileNeeded)
        { 
          if (leChar.length() != 0)
            leChar += "\n";
          leChar += "PARM(PATHMODE("+pathMode+"))";
        }
        else
        {
          if (leChar.length() != 0)
            leChar += nextLine;
          //leChar += "'PARM(PATHMODE("+pathMode+"))'";
          leChar += "PARM(PATHMODE("+pathMode+"))";
        }
      }
    }

    // McsFile not needed, format for inclusion in UPD file
    if (leChar != null && leChar.length() != 0 && !mcsFileNeeded)
    {
      leChar = formatLechar( leChar );
      leChar="LECHAR(0)=("+leChar+")";
    }

    if (partType != null && partType.length() != 0)
    {
      fileInfo+=nextLine+"PARTTYPE(0)="+partType;
    }
    
    if (sysLib != null && !sysLib.isEmpty())
    {
      fileInfo += nextLine + "SYSLIB(0)=";
      ListIterator packageDataIterator= sysLib.listIterator();
      while (packageDataIterator.hasNext())
      {
        String sysLibElement = (String)packageDataIterator.next();
        fileInfo += sysLibElement;
        if (packageDataIterator.hasNext()) fileInfo += ", ";
      }
    }
    
    if (mcsFileNeeded) 
    {
      String newleChar = "";
      StringTokenizer st1 = new StringTokenizer( leChar, "\n");
      while (st1.hasMoreTokens())
      {
        String leline = st1.nextToken();
        newleChar += formatLine72( leline ) + "\n";
      }

      File mcsFile = createMcsFile( mcsData, module, partType, newleChar );
      mcsFiles.addElement(mcsFile);
      fileInfo += nextLine + "MCSDATA=" + mcsFile.getName();
      fileInfo += fileStart + attribSeparator + "CLASS=MCS" + partType
                  + attribSeparator + "MOD=" + module + attribSeparator +
						"BLDLVL=0";
    } 
    else 
    {
      if (partType.equals("HFS") && mcsData != null && mcsData.length() != 0) 
      {
        fileInfo+= nextLine + "MCSDATA=" + mcsData;
      }
      if (leChar != null && leChar.length() != 0)
      {
        fileInfo += nextLine + leChar;
      }
    }
    return fileInfo;
  }

   /**********************************************************************
   * This method creates the mcs file containing the leChar text string. 
   * @param module String the path to be formatted. 
   * @returns String The formatted path. 
   **/
  public File createMcsFile( String mcsdata, String module,
                             String partType, String leChar )
    throws GeneratorException
  {
    String path = ((B390NodeHandler)nodeHandler).getOutputDirectoryName();
    String fileName = "MCS" + partType + "." + module;
    String copymode;
    File file = new File(path, fileName);
    if (mcsdata.equalsIgnoreCase( "binary" ))
      copymode = "BINARY\n";
    else
      copymode = "TEXT\n";
    FileOutputStream fileStream = nodeHandler.treeHandler.openFile(path, fileName);
    nodeHandler.treeHandler.writeString(fileStream, copymode);
    nodeHandler.treeHandler.writeString(fileStream, leChar);
    nodeHandler.treeHandler.closeFile(fileStream);
    return file;
  }
  
  /**********************************************************************
   * This method creates the 2nd dummy UPD entries for files to be deleted
   * @returns String The upd text
   **/
  public String formatDeletedFiles() 
    throws GeneratorException
  {
    String fileStart = "\nOP=UPD";
    String fileInfo = "";
    String module = "";
    String distLib = "";
    String fmid = "";
    String parttype = "";
    String distSrc = "";    
    
    Enumeration e = ((B390NodeHandler)nodeHandler).getDeletedFiles().elements();
    while( e != null && e.hasMoreElements() )
    {
      String delfile = (String)e.nextElement();
      StringTokenizer st = new StringTokenizer(delfile, ".");
      if (st.countTokens() == 4)
      {
        fmid = st.nextToken();
        distLib = st.nextToken();
        module = st.nextToken();
        parttype = st.nextToken();
        fileInfo += fileStart;
        fileInfo += nextParm + "CLASS="+parttype;
        fileInfo += nextParm + "MOD="+module;
        fileInfo += nextLine + "DISTNAME(0)="+module;
        fileInfo += nextLine + "DISTLIB(0)="+distLib;
        if (parttype.equalsIgnoreCase("mod"))
          distSrc = "OBJ";
        else
          distSrc = "C1";
        fileInfo += nextLine + "DISTSRC(0)="+distSrc;        
        fileInfo += nextLine + "PARTTYPE(0)="+parttype;
        fileInfo += nextLine + "FID="+fmid;
        fileInfo += nextLine + "DELETE=ON";
        fileInfo += nextLine + "INACTIVE=ON";
        fileInfo += nextLine + "BLDLVL=1";
      }
      else
      {
        throw new GeneratorException("Invalid format for deleted parts\n" +
               "Should have format: <fmid>.<distlib>.<targetFile>.<parttype>");
      }
    }
    return fileInfo;
  }

  /**********************************************************************
   * This method creates the 1st dummy UPD entries for files to be deleted
   * @returns String The upd text for the deleted file
   **/
  public String formatDeletedFileSectionOne() 
    throws GeneratorException
  {
    String fileStart = "\nOP=UPD";
    String fileInfo = "";
    String module = "";
    String distLib = "";
    String fmid = "";
    String parttype = "";

    Enumeration e = ((B390NodeHandler)nodeHandler).getDeletedFiles().elements();
    while( e != null && e.hasMoreElements() )
    {
      String delfile = (String)e.nextElement();
      StringTokenizer st = new StringTokenizer(delfile, ".");
      if (st.countTokens() == 4)
      {
        fmid = st.nextToken();
        distLib = st.nextToken();
        module = st.nextToken();
        parttype = st.nextToken();
        fileInfo += fileStart;
        fileInfo += nextParm + "CLASS="+parttype;
        fileInfo += nextParm + "MOD="+module;
        fileInfo += nextLine + "BLDLVL=1";
        fileInfo += nextLine + "LOCAL=ON";
        fileInfo += nextLine + "VHJN=00000000";
        fileInfo += nextLine + "INACTIVE=ON";
      }
      else
      {
        throw new GeneratorException("Invalid format for deleted parts\n" +
               "Should have format: <fmid>.<distlib>.<targetFile>.<parttype>");
      }
    }
    return fileInfo;
  }

  /**
   * This method creates an entry for the specified FMID
   *
   * @param fmid a valid FMID (Function Modififer)
   * @returns String The entry text for the specified FMID in the UPD file
   */
  public String formatJCLINFile( String fmid ) 
  {
     StringBuffer fileInfo = new StringBuffer("\nOP=UPD,CLASS=JCLIN,MOD=");
     fileInfo.append(fmid);
     fileInfo.append(nextLine).append("DISTNAME(0)=").append(fmid);
     fileInfo.append(nextLine).append("DISTLIB(0)=JCLIN");
     fileInfo.append(nextLine).append("DISTSRC(0)=SRC");
     fileInfo.append(nextLine).append("DISPLN(0)=GENER");
     fileInfo.append(nextLine).append("BLDPROC=XNULLBLD");
     fileInfo.append(nextLine).append("CALLLIBS(0)=YES");
     fileInfo.append(nextLine).append("BLDLVL=0");
     return fileInfo.toString();
  }
  
  /**********************************************************************
   * This method formats path information for B390.  
   * Remove the trailing "/" and "\" in directories. 
   * @param path String the path to be formatted. 
   * @returns String The formatted path. 
   **/
  public String formatPath( String path )
  {
    if (path.endsWith("/") || path.endsWith("\\")) 
      path = path.substring(0, path.length() - 1);
    return path;
  }

  /**********************************************************************
   * This method formats lechar information for B390.  
   * Remove the leading and trailing "'". 
   * @param link String the link to be formatted. 
   * @returns String The formatted link. 
   **/
  public String formatLechar( String lechar )
  {
    String formattedLechar = "";
    String formattedLine;
    String line = "";
    String keyword;
    String newLine = "\n   ";

    StringTokenizer st1 = new StringTokenizer(lechar, "\n");
    while (st1.hasMoreTokens())
    {
      line = st1.nextToken().trim();
      formattedLine = "";
      if (line.endsWith( "," ))  // Strip trailing commas
        line = line.substring(0, line.length() - 1);
      keyword = line.substring(1, line.indexOf('('));
      if (!keyword.equals("PARM"))
      {
        StringTokenizer st = new StringTokenizer( line, "," );
        while (st.hasMoreTokens()) 
        {
          formattedLine += st.nextToken();
          if (st.hasMoreTokens())
            formattedLine += ",";
        }
      }
      else
        formattedLine = line; 
      formattedLine = formattedLine.replace( '\'', '"');
      formattedLine = "'" + formattedLine + "'";
      if (st1.hasMoreTokens())
        formattedLine += "," + newLine;
      formattedLechar += formattedLine;
    }
    return formattedLechar;
  }

  /**********************************************************************
   * This method gives the platform specific code the chance to filter file
   * stanzas based on the data in the PackageData curPD. 
   * @param curFE FileEntity the current file entity. 
   * @returns boolean true if the file entity should be filtered false otherwise. 
   * @throws GeneratorException
   **/
  public boolean filtered( PackageData packageData ) throws GeneratorException
  {
    boolean filtered = false;
    ArrayList shipTypes = packageData.getShipType();
    if (shipTypes.size() == 1 && 
        ((String)shipTypes.get(0)).equalsIgnoreCase("VPL")) 
      filtered = true;
    else if (isDirectory(packageData)) 
      filtered = true;
    return filtered;
  }

  /**********************************************************************
   * This method determines if the file entity represents a file or a 
   * directory. 
   * @return boolean true if the file entity represents a directory. 
   * @throws GeneratorException
   **/
  public boolean isDirectory( PackageData packageData ) throws GeneratorException
  {
    String fileType = packageData.getFileType();
    if (fileType == null)
    {
      throw new GeneratorException("FileListGenerator: Null file Type ");
    }
    return fileType.equalsIgnoreCase("d");
  } 
  public String getPartType( PackageData packageData, boolean required ) 
    throws GeneratorException
  { 
    String result = packageData.getFileType();
    if (required) validate(result);
    return result;
  }
  public String getModule( PackageData packageData, boolean required ) 
    throws GeneratorException
  { 
    String result = packageData.getTargetFile();
    if (required) validate(result);
    return result;
  }
  public String getDistLib( PackageData packageData, boolean required ) 
    throws GeneratorException
  { 
    String result = packageData.getTargetDir();
    if (required) validate(result);
    return result;
  }
  public ArrayList getSysLib(PackageData packageData) 
  { 
    ArrayList result = new ArrayList();
    if (packageData == null) return result;
    ArrayList partInfo = packageData.getPartInfo();
    ListIterator packageDataIterator;
    if (partInfo != null && !partInfo.isEmpty())
    {
      packageDataIterator = partInfo.listIterator();
      while (packageDataIterator.hasNext())
      {
        ReqType partInfoElement=(ReqType)packageDataIterator.next();
        if (partInfoElement.getType().equalsIgnoreCase("syslib")) 
        {
          if (partInfoElement.getValue() != null &&
              partInfoElement.getValue().length() != 0)
            result.add(partInfoElement.getValue());
        }
      }
    }
    return result;
  }
  public ArrayList getLink( PackageData packageData ) 
  { 
    ArrayList result = new ArrayList();
    if (packageData == null) return result;
    ArrayList partInfo = packageData.getPartInfo();
    ListIterator packageDataIterator;
    if (partInfo != null && !partInfo.isEmpty())
    {
      packageDataIterator = partInfo.listIterator();
      while (packageDataIterator.hasNext())
      {
        ReqType partInfoElement=(ReqType)packageDataIterator.next();
        if (partInfoElement.getType().equalsIgnoreCase("link")) 
        {
          if (partInfoElement.getValue() != null &&
              partInfoElement.getValue().length() != 0)
            result.add( partInfoElement.getValue() );
        }
      }
    }
    return result;
  }

  public ArrayList getSymLink(PackageData packageData) 
  { 
    ArrayList result = new ArrayList();
    if (packageData == null) return result;
    ArrayList partInfo = packageData.getPartInfo();
    ListIterator packageDataIterator;
    if (partInfo != null && !partInfo.isEmpty())
    {
      packageDataIterator = partInfo.listIterator();
      while (packageDataIterator.hasNext())
      {
        ReqType partInfoElement = (ReqType)packageDataIterator.next();
        if (partInfoElement.getType().equalsIgnoreCase("symlink")) 
        {
          if (partInfoElement.getValue() != null && 
              partInfoElement.getValue().length() != 0)
            result.add( partInfoElement.getValue() );
        }
      }
    }
    return result;
  }

  public ArrayList getSymPath( PackageData packageData ) 
  { 
    ArrayList result = new ArrayList();
    if (packageData == null) return result;
    ArrayList partInfo = packageData.getPartInfo();
    ListIterator packageDataIterator;
    if (partInfo != null && !partInfo.isEmpty())
    {
      packageDataIterator = partInfo.listIterator();
      while (packageDataIterator.hasNext())
      {
        ReqType partInfoElement = (ReqType)packageDataIterator.next();
        if (partInfoElement.getType().equalsIgnoreCase("sympath")) 
        {
          if (partInfoElement.getValue() != null && 
              partInfoElement.getValue().length() != 0)
            result.add(partInfoElement.getValue());
        }
      }
    }
    return result;
  }

  public String getPathMode( PackageData packageData, boolean required ) 
    throws GeneratorException
  { 
    String result = packageData.getPermissions();
    if (required) validate(result);
    if (result == null) result = "";
    return result;
  }
  
  public void validate( String value ) throws GeneratorException
  { 
    if (value == null || value.length() == 0) 
      throw new GeneratorException("Required value not found.");
  }
  
  public String getHFSCopyType( PackageData packageData, boolean required ) 
    throws GeneratorException
  { 
    String result = packageData.getHfsCopyType();
    if (required) validate(result);
    if (result == null) result = "";
    if (result.length() == 0) 
    {
      result = "binary"; //default to binary
      ArrayList partInfo = packageData.getPartInfo();
      ListIterator packageDataIterator;
      if (partInfo != null && !partInfo.isEmpty())
      {
        packageDataIterator = partInfo.listIterator();
        while (packageDataIterator.hasNext())
        {
          ReqType partInfoElement=(ReqType)packageDataIterator.next();
          if (partInfoElement.getType().equalsIgnoreCase("text")) 
          {
            result = partInfoElement.getType();
          }
        }
      }
    }
    return result.trim();
  }

  //****************************************************************************
  // Format mcsdata file so that the line does not exceed column 72.
  //****************************************************************************
  private static String formatLine72(String line)
  {
    int lineLength = line.length();

    if (lineLength <= 72) return line;   // no continuation needed
    
    // number of continuations needed
    int continues = lineLength / 72;

    for (int i = 1; i <= continues; i++)
    {
      // x marks the spot to insert continuation
      int x = (72 * i);
      x += (i - 1);
      line = line.substring(0, x) + "\n" + line.substring(x);
    }
    return line;
  }
}
