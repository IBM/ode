package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.PackageData;
import com.ibm.ode.pkg.parserGenerator.ReqType;
import java.io.File;
import java.util.*;
/**********************************************************************
 * This class is responsible handling the MVS plaform specific differences 
 * in the generation of the list of file to be packaged. 
 * 
 * File stanzas may represent files, links, or directories. 
 * System    file   link   directory
 * Solaris    f       s       d
 * hp         F       S       D 
 * nt         -       -       -
 * AIX        F       S       D 
 * MVS        mod/hfs mod/hfs -
 * 
 * The output file format is 
 * CLASS.MOD    HFSFILENAME
 * 
 * Where: 
 *    CLASS = Build/390 UNIMODC part class - which for us is the CMF 
 *            fileType, 
 *    MOD = the short name of the part in the UNIMODC - which for us 
 *            is the CMF targetFile, 
 *    HFSFILENAME = the full path and filename of the built part so 
 *            that Build/390 can copy it to the VSAM bulk log
 *
 * @see FileListGenerator
 * @see FileListCustomizer
 *
 * @version 0.1 9/19/2000
 * @author Bruce Gibbons
**/
public class B390LSTFormatter extends LSTFormatter
{
  /**********************************************************************
   * This method sets the filter to the type passed as a parameter. 
   * @param int filter The type of filtering to be used when generating output. 
   **/
  public B390LSTFormatter (B390NodeHandler nodeHandler,int filter)
    throws GeneratorException
  {
     super(nodeHandler, filter);
  }

  public String formatProductInfo() 
    throws GeneratorException
  {
    String fileInfo="";
    return fileInfo;
  }

  /**********************************************************************
   * This method formats the data for output. It isolates the platform 
   * specific formatting from the program logic. 
   * @return String The formatted string.
   * @throws GeneratorException
   **/
  public String formatFileInfo()
    throws GeneratorException
  {
   String fileInfo="";
    if (filtered()) return fileInfo;
    //Iterate through multiple PackageData stanzas 		
    ArrayList packageDataArray = nodeHandler.getPackageData();
    ListIterator packageDataIterator;
    if (packageDataArray!= null)
    {
      if (!packageDataArray.isEmpty())
      {
        packageDataIterator = packageDataArray.listIterator();
        while (packageDataIterator.hasNext())
        {
          PackageData packageData  = (PackageData)packageDataIterator.next();
          if (filtered(packageData)) continue;
          fileInfo+=formatPackageData(packageData);
        } //end for
      } //end if
    }
    return fileInfo;
  }

  /**********************************************************************
   * This method formats the data for output. It isolates the platform 
   * specific formatting from the program logic. 
   * @return String The formatted string.
   * @throws GeneratorException
   **/
  private String formatPackageData(PackageData packageData) 
    throws GeneratorException
  {
    String fileType = getFileType(packageData);
    String sourceFilePath=nodeHandler.getToStageDir()+nodeHandler.getSourceDir()+nodeHandler.getSourceFile();
    try 
    {
      sourceFilePath=(new File(sourceFilePath)).getCanonicalPath();
    } catch (java.io.IOException e) 
    {
      System.out.println("*********"+e);
    }
    
    return fileType+"."+getTargetFile(packageData)+" "+
       sourceFilePath+" "+getMVSFileAttribs(packageData)+"\n";
  }

  /**********************************************************************
   * This method gets the MVS file attributes for the USS file. 
   * @param PackageData curPD
   * @return String The formatted string.
   * @throws GeneratorException
   **/
  public String getMVSFileAttribs(PackageData packageData)
    throws GeneratorException
  {
    String collector="C1";
    if (getFileType(packageData).equalsIgnoreCase("mod")) 
    {
      collector="OBJ";
    }
    ArrayList distLib=getDistLibs(packageData);
    String separator="-";
    int recf=4;
    int recl=5;
    String recordFormat=((String)distLib.get(recf)).toUpperCase();
    if (recordFormat.equalsIgnoreCase("FB")) 
    {
      recordFormat="F";
    } else if (recordFormat.equalsIgnoreCase("VB")) 
    {
      recordFormat="V";
    } 
    String recordLength=(String)distLib.get(recl);

    // If MOD part for link-edit, must use FB80 and not RECFM U
    if (getFileType(packageData).equalsIgnoreCase("mod")) 
    {
      String lkedParms = packageData.getLkedParms();
      if (lkedParms!=null && !lkedParms.equals("") && 
          recordFormat.equalsIgnoreCase("U"))
      {
        recordFormat = "F";
        recordLength = "80"; 
      }
    }

    String mode=packageData.getHfsCopyType();
    if (mode==null) 
    {
      //throw new GeneratorException("B390LSTFormatter: HfsCopyType + " +
      //              "must be defined ");
      mode="B";
    }
    mode=mode.trim().substring(0,1).toUpperCase();
    if (!mode.equals("B")&&!mode.equals("T"))
    {
      throw new GeneratorException("B390LSTFormatter: HfsCopyType + " +
                    "Unknown HFS copy type "+ packageData.getHfsCopyType());
    }
    return collector+separator+recordFormat+separator+recordLength+separator+mode;
  }

  /**********************************************************************
   * This method formats a text string for the Vector of java.io.Files that need to
   * be added to the LST file. 
   * @param  Vector The Vector of Files. 
   * @return String The formatted text string. 
   * @throws GeneratorException
   **/
  public String addFiles(Vector fileList) 
    throws GeneratorException
  {
    String result="";
    Enumeration files=fileList.elements();
    while (files.hasMoreElements()) 
    {
      File file=(File)files.nextElement();
      String fileName=file.getName();
      String filePath=file.getPath();
      System.out.println("Added File: "+fileName);
      result+=fileName+" "+filePath+" SRC-F-80-T\n";
    } 
    return result;
  } 

  /**********************************************************************
   * This method determines if the file entity represents a file or a 
   * directory. 
   * @return boolean true if the file entity represents a directory. 
   * @throws GeneratorException
   **/
  public boolean isDirectory(PackageData packageData) throws GeneratorException
  {
    String fileType = getFileType(packageData);
    if (fileType == null)
    {
      throw new GeneratorException("FileListGenerator: Null file Type ");
    }
    return fileType.equalsIgnoreCase("d");
  } 

  /**********************************************************************
   * This method gives the platform specific code the chance to filter file
   * stanzas based on the data in the PackageData. 
   * @param packageData PackageData the current packageData. 
   * @returns boolean true if the file entity should be filtered false otherwise. 
   * @throws GeneratorException
   **/
  public boolean filtered(PackageData packageData) throws GeneratorException
  {
    boolean filtered=false;
    ArrayList shipTypes=packageData.getShipType();
    if (shipTypes.size()==1&&((String)shipTypes.get(0)).equalsIgnoreCase("VPL")) filtered=true;
    else if (isDirectory(packageData)) filtered=true;
    return filtered;
  }

}  //end class definition
