package com.ibm.ode.pkg.parserGenerator;

import java.util.*;
import com.ibm.ode.lib.io.Interface;

//LVW new class to represent the multiply occurring stanza PackageData

public class PackageData
{
  //All the variables of FileEntity stanza except for sourceDir and sourceFile.
  private String partNum_;
  private String fileType_;
  private ArrayList fileDirectives_;
  private String targetFile_;
  private String targetDir_;
  private String permissions_;
  private String userId_;
  private String groupId_;
  private String majorDevNum_;
  private String minorDevNum_;
  private String flags_;
  private String comp_;
  private ArrayList  shipType_;
  private ArrayList  partInfo_;
  private String vplSecurity_;
  private String vplPartqual_;
  private String hfsCopyType_;
  private String extAttr_;
  private ArrayList  hfsAlias_;
  private ArrayList  pdsAlias_;
  private String setCode_;
  private String entry_;
  private String jclinMode_;
  private ArrayList  include_;
  private ArrayList  sysLibs_;
  private ArrayList  sysLibsLibraryDD_;
  private String lkedTo_;
  private String lkedRc_;
  private String pdsLkedName_;
  private String hfsLkedName_;
  private String jclinLkedParms_;
  private String order_;
  private String lkedParms_;
  private String libraryDD_;
  private String lkedCond_;
  private String sideDeckAppendDD_;

  public String toString()
  {
    if (targetDir_ != null)
		{
      if (targetFile_ != null)
        return (Package.checkForFileSeparator( targetDir_ + targetFile_ ));
      else
        return (targetDir_);
		}
    else
		{
      if (targetFile_ != null)
        return (Package.checkForFileSeparator( targetFile_ ));
    }
    return new String();
  }

  public int hashCode()
  {
    return (toString().hashCode());
  }

  public boolean equals( Object obj )
  {
    return (hashCode() == obj.hashCode());
  }

   //get and set functions for partNum_ attribute
  public void setPartNum(String partNum)
  {
    partNum_ = partNum;
  }
  
  public String getPartNum()
  {
    return partNum_;
  }
  
  //get and set functions for fileType_ attribute
  public void setFileType(String fileType)
  {
    fileType_ = fileType;
  }
  
  public String getFileType()
  {
    return fileType_;
  }
  
  //get and set functions for fileDirectives_ attribute
  public void setFileDirectives(ArrayList fileDirectives)
  {
    fileDirectives_ = new ArrayList(fileDirectives);
  }
  
  public ArrayList getFileDirectives()
  {
    return fileDirectives_;
  }

  //get and set functions for targetFile_ attribute
  public void setTargetFile(String targetFile)
  {
    targetFile_ = targetFile;
  }
  
  public String getTargetFile()
  {
    return targetFile_;
  }
  
  //get and set functions for targetDir_ attribute
  public void setTargetDir(String targetDir)
  {
    if (targetDir_ !=null) 
    {
      Interface.printWarning("Changing targetDir/distLib from "+targetDir_+" to "+targetDir+"\n");
    }
    targetDir_ = targetDir;
  }
  
  public String getTargetDir()
  {
    return targetDir_;
  }
  
  //get and set functions for permissions_ attribute
  public void setPermissions(String perm)
  {
    permissions_ = perm;
  }
  
  public String getPermissions()
  {
    return permissions_;
  }
  
  //get and set functions for userId_ attribute
  public void setUserId(String userId)
  {
    userId_ = userId;
  }
  
  public String getUserId()
  {
    return userId_;
  }
  
  //get and set functions for groupId_ attribute
  public void setGroupId(String groupId)
  {
    groupId_ = groupId;
  }
  
  public String getGroupId()
  {
    return groupId_;
  }
  
  //get and set functions for majorDevNum_ attribute
  public void setMajorDevNum(String majorDevNum)
  {
    majorDevNum_ = majorDevNum;
  }
  
  public String getMajorDevNum()
  {
    return majorDevNum_;
  }
  
  //get and set functions for minorDevNum_ attribute
  public void setMinorDevNum(String minorDevNum)
  {
    minorDevNum_ = minorDevNum;
  }
  
  public String getMinorDevNum()
  {
    return minorDevNum_;
  }

  //get and set functions for flags_ attribute
  public void setFlags( String flags )
  {
    flags_ = flags;
  }
  
  public String getFlags()
  {
    return flags_;
  }


  //get and set functions for comp attribute
  public void setComp(String comp)
  {
    comp_ = comp;
  }
  
  public String getComp()
  {
    return comp_;
  }

  //get and set functions for shipType attribute
  public void setShipType(ArrayList shipType)
  {
    shipType_ = new ArrayList(shipType);
  }
  
  public ArrayList getShipType()
  {
    return shipType_;
  }

  //get and set functions for partInfo attribute
  public void setPartInfo(ArrayList partInfo)
  {
    partInfo_ = new ArrayList(partInfo);
  }
  
  public ArrayList getPartInfo()
  {
   return partInfo_;
  }

  //get and set functions for vplSecurity attribute
  public void setVplSecurity(String vplSecurity)
  {
    vplSecurity_ = vplSecurity;
  }
  
  public String getVplSecurity()
  {
    return vplSecurity_;
  }

  //get and set functions for vplPartqual attribute
  public void setVplPartqual(String vplPartqual)
  {
    vplPartqual_ = vplPartqual;
  }
  
  public String getVplPartqual()
  {
    return vplPartqual_;
  }

 
 //get and set functions for hfsCopyType attribute
  public void setHfsCopyType(String hfsCopyType)
  {
    hfsCopyType_ = hfsCopyType;
  }

  public String getHfsCopyType()
  {
    return hfsCopyType_;
  }

 //get and set functions for extAttr attribute
  public void setExtAttr(String extAttr)
  {
    extAttr_ = extAttr;
  }

  public String getExtAttr()
  {
    return extAttr_;
  }
 
 //get and set functions for hfsAlias attribute
  public void setHfsAlias(ArrayList hfsAlias)
  {
    hfsAlias_ = new ArrayList(hfsAlias);
  }

  public ArrayList getHfsAlias()
  {
    return hfsAlias_;
  }
 
 //get and set functions for pdsAlias attribute
  public void setPdsAlias(ArrayList pdsAlias)
  {
    pdsAlias_ = new ArrayList(pdsAlias);
  }

  public ArrayList getPdsAlias()
  {
    return pdsAlias_;
  }

 //get and set functions for setCode attribute
  public void setSetCode(String setCode)
  {
    setCode_ = setCode;
  }

  public String getSetCode()
  {
    return setCode_;
  }

 //get and set functions for entry attribute
  public void setEntry(String entry)
  {
    entry_ = entry;
  }

  public String getEntry()
  {
    return entry_;
  }

 //get and set functions for jclinMode attribute
  public void setJclinMode(String jclinMode)
  {
    jclinMode_ = jclinMode;
  }

  public String getJclinMode()
  {
    return jclinMode_;
  }

 //get and set functions for include attribute
  public void setInclude(ArrayList include)
  {
    include_ = new ArrayList(include);
  }

  public ArrayList getInclude()
  {
    return include_;
  }

 //get and set functions for lkedTo attribute
  public void setLkedTo(String lkedTo)
  {
    lkedTo_ = lkedTo;
  }

  public String getLkedTo()
  {
    return lkedTo_;
  }

 //get and set functions for lkedRc attribute
  public void setLkedRc(String lkedRc)
  {
    lkedRc_ = lkedRc;
  }

  public String getLkedRc()
  {
    return lkedRc_;
  }

 //get and set functions for hfsLlkedName attribute
  public void setHfsLkedName(String hfsLkedName)
  {
    hfsLkedName_ = hfsLkedName;
  }

  public String getHfsLkedName()
  {
    return hfsLkedName_;
  }

 //get and set functions for pdsLkedName attribute
  public void setPdsLkedName(String pdsLkedName)
  {
    pdsLkedName_ = pdsLkedName;
  }

  public String getPdsLkedName()
  {
    return pdsLkedName_;
  }

 //get and set functions for jclinLkedParms attribute
  public void setJclinLkedParms(String jclinLkedParms)
  {
    jclinLkedParms_ = jclinLkedParms;
  }

  public String getJclinLkedParms()
  {
    return jclinLkedParms_;
  }

  // get and set functions for sysLibs attribute
  public void setSysLibs(ArrayList sysLibs)
  {
    sysLibs_ = new ArrayList(sysLibs);
  }

  public ArrayList getSysLibs()
  {
    return sysLibs_;
  }
  
  // get and set functions for sysLibsLibraryDD attribute
  public void setSysLibsLibraryDD(ArrayList sysLibsLibraryDD)
  {
    sysLibsLibraryDD_ = new ArrayList(sysLibsLibraryDD);
  }

  public ArrayList getSysLibsLibraryDD()
  {
    return sysLibsLibraryDD_;
  }

 //get and set functions for order attribute
  public void setOrder(String order)
  {
    order_ = order;
  }

  public String getOrder()
  {
    return order_;
  }

 //get and set functions for lkedParms attribute
  public void setLkedParms(String lkedParms)
  {
    lkedParms_ = lkedParms;
  }

  public String getLkedParms()
  {
    return lkedParms_;
  }

 //get and set functions for libraryDD attribute
  public void setLibraryDD(String libraryDD)
  {
    libraryDD_ = libraryDD;
  }

  public String getLibraryDD()
  {
    return libraryDD_;
  }
  
  //get and set functions for lkedCond attribute
  public void setLkedCond(String lkedCond)
  {
    lkedCond_ = lkedCond;
  }

  public String getLkedCond()
  {
    return lkedCond_;
  }

  //get and set functions for sideDeckAppendDD attribute
  public void setSideDeckAppendDD(String sideDeckAppendDD)
  {
    sideDeckAppendDD_ = sideDeckAppendDD;
  }

  public String getSideDeckAppendDD()
  {
    return sideDeckAppendDD_;
  }
 }  //end of PackageData class





