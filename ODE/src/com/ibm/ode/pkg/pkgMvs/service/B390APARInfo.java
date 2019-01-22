package com.ibm.ode.pkg.pkgMvs.service;

import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
import java.lang.InterruptedException;

public class B390APARInfo
{
  private String name;            //The apar name assigned by retain.
  private boolean retainHasAPAR = true;//USERMODs are not defined in retain like apars. 
  private Vector mvsReleases;     //FMIDs
  /******************************************************************
   * Constructor
   * @param name String The name of the apar which is assigned by retain.
   * @param mvsRelease B390Release The MVS FMID/Version/product.
   * @see B390Release, B390CommandInterface
   **/
  public B390APARInfo(String name, Vector mvsReleases) 
  {
    super();
    this.setName(name);
    this.setMVSReleases(mvsReleases);
  }
  public B390APARInfo(String name, Vector mvsReleases, boolean retainHasAPAR) 
  {
    super();
    this.setName(name);
    this.setMVSReleases(mvsReleases);
    this.retainHasAPAR=retainHasAPAR;
  }
  private void setName(String name)
  {
    this.name=name.trim();
  }
  private String getName()
  {
    return name;
  }
  private void setMVSReleases(Vector mvsReleases) 
  {
    this.mvsReleases=mvsReleases;
  }
  private Vector getMVSReleases() 
  {
    return mvsReleases;
  }
  public void define() 
    throws B390CommandException,IOException,InterruptedException
  {
    if (!mvsReleases.isEmpty())
    {
      Enumeration releases=mvsReleases.elements();
      while (releases.hasMoreElements()) 
      {
        B390Release release=(B390Release)releases.nextElement();
        release.build(this.getName());
      }
    }
  }
  public void createTestPackages(boolean retainHasAPAR) 
    throws B390CommandException,IOException,InterruptedException
  {
    Enumeration releases=mvsReleases.elements();
    while (releases.hasMoreElements()) 
    {
      B390Release release=(B390Release)releases.nextElement();
      release.createTestPackage(this.getName(), retainHasAPAR);
    }
  }
  public static void main(String args[]) 
    throws B390CommandException,IOException,InterruptedException
  {
    String aparName="testApar";
    String releaseName="ODETST1";
    String highLevelQualifier="B390TEST";
    Vector releases=new Vector();
    boolean retainHasAPAR=false;
    if (args.length>0) 
    {
      aparName=args[0].trim();
      if (aparName.length()<1||aparName.length()>8) 
      {
        usage();
        throw new B390CommandException("APAR name must be less than eight characters");
      }
      if (args.length>1) 
      {
        releaseName=args[1].trim();
        if (releaseName.length()<1||releaseName.length()>8) 
        {
          usage();
          throw new B390CommandException("Release name must be less than eight characters");
        }
        if (args.length>2) 
        {
          highLevelQualifier=args[2].trim();
          if (highLevelQualifier.length()<1||highLevelQualifier.length()>8) 
          {
            usage();
            throw new B390CommandException("highLevelQualifier must be less than eight characters");
          }
          if (args.length>3) 
          {
            usage();
            throw new B390CommandException("More than 3 arguments.");
          }
        }  
      }
    }
    releases.addElement(new B390Release(releaseName,highLevelQualifier));
    B390APARInfo b390APAR=new B390APARInfo(aparName,releases);
    b390APAR.define();
    b390APAR.createTestPackages(retainHasAPAR);
  }
  public static void usage()
  {
    String usage="B390APARInfo(String aparName, String releaseName, String highLevelQualifier)";
    usage+="aparName is the name of the apar which must be "+
           "less than 8 characters long.";
    usage+="releaseName is the name of the release on MVS which must be "+
           "less than 8 characters long.";
    usage+="highLevelQualifier is the MVS high level qualifier containing"+
           "the release which must be less than 8 characters long.";
    System.out.println(usage);
  }
}
