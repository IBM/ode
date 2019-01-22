package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.FileEntity;
import com.ibm.ode.pkg.parserGenerator.InstallEntity;
import com.ibm.ode.pkg.parserGenerator.PackageData;
import java.io.FileOutputStream;

/**********************************************************************
 * This class is responsible handling the mkinstall specific differences 
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
 * 
 * The output file format is 
 * SRCDIR/SRCFILE 
 * 
 * Where: 
 *    SRCDIR = the full path of the built part.
 *    SRCFILE = the filename of the built part.
 *
 * @see FileListGenerator
 * @see FileListCustomizer
 *
 * @version 0.1 9/19/2000
 * @author Bruce Gibbons
**/
public class MkinstallNodeHandler extends NodeHandler
{
  /**********************************************************************
   * Used to maintain a handle to the
   * formatters.
   **/
  private Formatter lstFormatter;
  /**********************************************************************
   * Used to maintain a handle to the output streams.
   **/
  private FileOutputStream lstFile_;
  /**********************************************************************
   * This method sets the filter to the type passed as a parameter. 
   * @param int filter The type of filtering to be used when generating output. 
   **/
  public MkinstallNodeHandler(int filter) throws GeneratorException
  {
    super();
    lstFormatter = new MkinstallLSTFormatter(this,filter);
  }

/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param curFE FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void init()
    throws GeneratorException
  {
    this.lstFile_ = this.treeHandler.openFile(getOutputDirectoryName(),
                                            getLSTFileName());
  } //end method handleFile

/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param curFE FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void finish()
    throws GeneratorException
  {
    this.treeHandler.closeFile(this.lstFile_);
  } //end method handleFile

/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param curFE FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void handleFile(FileEntity fileEntity)
    throws GeneratorException
  {
    super.handleFile(fileEntity);

    String lstText=lstFormatter.formatFileInfo();
   this.treeHandler.writeString(lstFile_,lstText);
  } //end method handleFile

  /**********************************************************************
   * This method returns the name of the directory containing the 
   * file list and upd information. 
   * The file will be generated at ${PKG_CONTROL_DIR}/<fmid>/. 
   * @return String The name of the directory containing the file list 
   * and upd information. 
   **/
  private String getOutputDirectoryName()
  {
    //The file will be generated at ${PKG_CONTROL_DIR}. 
    String separator="/";
    String pkgControlDirectory=getPkgControlDir();
    return pkgControlDirectory; 
  }

  /**********************************************************************
   * This method returns the name of the file containing the file list information. 
   * The name is always B390.LST
   * @return String The name of the file containing the file list information. 
   **/
  private String getLSTFileName()
  {
    String staticName="file.lst";
    return staticName;
  }
 /********************************************************************
  * This method returns the tree customizer that will be used with
  * this NodeHandler.
  * @return TreeCustomizer The tree customizer that will be used with
  * this NodeHandler
  * @see TreeHandler
  * @see TreeCustomizer
  **/
  TreeCustomizer getTreeCustomizer()
  {
    return new MkinstallTreeCustomizer();
  }

}  //end class definition
