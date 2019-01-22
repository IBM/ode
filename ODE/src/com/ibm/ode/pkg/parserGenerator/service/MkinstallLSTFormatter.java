package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.PackageData;
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
public class MkinstallLSTFormatter extends LSTFormatter
{
  /**********************************************************************
   * This method sets the filter to the type passed as a parameter. 
   * @param int filter The type of filtering to be used when generating output. 
   **/
  public MkinstallLSTFormatter (MkinstallNodeHandler nodeHandler,int filter)
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
   String sourceDir=nodeHandler.getSourceDir();
    String sourceFile=nodeHandler.getSourceFile();
   return sourceDir+sourceFile+"\n";
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
    if (isDirectory(packageData)||filtered()) filtered=true;
    return filtered;
  }

}  //end class definition
