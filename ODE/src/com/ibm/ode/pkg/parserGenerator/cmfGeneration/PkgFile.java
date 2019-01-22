//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* Author: Chad Holliday
//*
//*****************************************************************************
import java.lang.*;
import java.util.StringTokenizer;

public class PkgFile
{

  //All the variables of PkgFile.
  private String longfilename_;
  private String shortfilename_;
  private String sourceDir_;
  private String distlib_;
  private String fmidlist_;
  private String comp_;
  private String copytype_;
  private String parttype_;
  private String hfsSyslib_;
  private String pdsSyslib_;
  private String link_;
  private String sympath_;
  private String symlink_;
  private String shiptype_;
  private String userid_;
  private String groupid_;
  private String permissions_;
  private String lkedto_;
  private String hfslkedname_;
  private String pdslkedname_;
  private String lkedrc_;
  private String hfsalias_;
  private String pdsalias_;
  private String setcode_;
  private String syslibs_;
  private String include_;
  private String order_;
  private String entry_;
  private String extattr_;
  private String jclinmode_;
  private String lkedparms_;
  private String jclinlkedparms_;
  private String vplsecurity_;
  private String vplpartqual_;
  private String libraryDD_;
  private String sysLibsLibraryDD_;
  private String sideDeckAppendDD_;
  
 //constructor w/ no parms
  public PkgFile()
  {
  }

 //constructor w/ parms
  public PkgFile( String dbEntry )
  {

    StringTokenizer st = new StringTokenizer( dbEntry, "|" );
    int counter = 0;
    String value;

    while (st.hasMoreTokens())
    {
      value = st.nextToken().trim();
      counter++;
      if (!value.equals("#") && (value != null))
      {
        switch( counter ) // switch on the token number in the database entry
        {
          case 1:
            longfilename_ = value; break;
          case 2:
            sourceDir_ = value; break;
          case 3:
            shortfilename_ = value; break;
          case 4:
            distlib_ = value; break;
          case 5:
            parttype_ = value; break;
          case 6:
            fmidlist_ = value; break;
          case 7:
            shiptype_ = value; break;
          case 8:
            comp_ = value; break;
          case 9:
            hfsSyslib_ = value; break;
          case 10:
            pdsSyslib_ = value; break;
          case 11:
            link_ = value; break;
          case 12:
            sympath_ = value; break;
          case 13:
            symlink_ = value; break;
          case 14:
            if ( value.equals("t") )
              copytype_ = "text";
            else if ( value.equals("b") )
              copytype_ = "binary";
            else
              copytype_ = value;
            break;
          case 15:
            userid_ = value; break;
          case 16:
            groupid_ = value; break;
          case 17:
            permissions_ = value; break;
          case 18:
            lkedto_ = value; break;
          case 19:
            hfslkedname_ = value; break;
          case 20:
            pdslkedname_ = value; break;
          case 21:
            lkedrc_ = value; break;
          case 22:
            hfsalias_ = value; break;
          case 23:
            pdsalias_ = value; break;
          case 24:
            setcode_ = value; break;
          case 25:
            syslibs_ = value; break;
          case 26:
            include_ = value; break;
          case 27:
            order_ = value; break;
          case 28:
            entry_ = value; break;
          case 29:
            extattr_ = value; break;
          case 30:
            jclinmode_ = value; break;
          case 31:
            lkedparms_ = value; break;
          case 32:
            jclinlkedparms_ = value; break;
          case 33:
            vplsecurity_ = value; break;
          case 34:
            vplpartqual_ = value; break;
          case 35:
            libraryDD_ = value; break;
          case 36:
            sysLibsLibraryDD_ = value; break;
          case 37:
            sideDeckAppendDD_ = value; break;
          default:
            break;
        }
      }
    }
  }

   //get and set functions for partNum_ attribute
  public void setLongFilename(String fn)
  {
     longfilename_ = fn;
  }

  public String getLongFilename()
  {
    return longfilename_;
  }

  public void setShortFilename(String fn)
  {
    shortfilename_ = fn;
  }

  public String getShortFilename()
  {
    return shortfilename_;
  }
  public void setDistlib(String distlib)
  {
    distlib_ = distlib;
  }

  public String getDistlib()
  {
    return distlib_;
  }
  public void setSourceDir(String sourceDir)
  {
    sourceDir_ = sourceDir;
  }

  public String getSourceDir()
  {
    return sourceDir_;
  }

  public void setFmidList(String fmids)
  {
    fmidlist_ = fmids;
  }

  public String getFmidList()
  {
    return fmidlist_;
  }

  public void setComp(String comp)
  {
    comp_ = comp;
  }

  public String getComp()
  {
    return comp_;
  }

  public void setCopyType(String ct)
  {
    if ( ct.length() == 1 )
    {
       if ( ct.equals("t") )
          copytype_ = "text";
       else if ( ct.equals("b") )
          copytype_ = "binary";
       else
          copytype_ = "";
    }
    else
       copytype_ = ct;
  }

  public String getCopyType()
  {
    return copytype_;
  }

  public void setPartType(String pt)
  {
    parttype_ = pt;
  }

  public String getPartType()
  {
    return parttype_;
  }

  public void setShipType(String st)
  {
    shiptype_ = st;
  }

  public String getShipType()
  {
    return shiptype_;
  }

  public void setPdsSyslib(String val)
  {
    pdsSyslib_ = val;
  }

  public String getPdsSyslib()
  {
    return pdsSyslib_;
  }

  public void setHfsSyslib(String val)
  {
    hfsSyslib_ = val;
  }

  public String getHfsSyslib()
  {
    return hfsSyslib_;
  }

  public void setLink(String val)
  {
    link_ = val;
  }

  public String getLink()
  {
    return link_;
  }

  public void setSympath(String val)
  {
    sympath_ = val;
  }

  public String getSympath()
  {
    return sympath_;
  }

  public void setSymlink(String val)
  {
    symlink_ = val;
  }

  public String getSymlink()
  {
    return symlink_;
  }

  public void setUserid(String val)
  {
    userid_ = val;
  }

  public String getUserid()
  {
    return userid_;
  }

  public void setGroupid(String val)
  {
    groupid_ = val;
  }

  public String getGroupid()
  {
    return groupid_;
  }

  public void setPermissions(String val)
  {
    permissions_ = val;
  }

  public String getPermissions()
  {
    return permissions_;
  }

  public void setLkedto(String val)
  {
    lkedto_ = val;
  }

  public String getLkedto()
  {
    return lkedto_;
  }

  public void setHfslkedname(String val)
  {
    hfslkedname_ = val;
  }

  public String getHfslkedname()
  {
    return hfslkedname_;
  }

  public void setPdslkedname(String val)
  {
    pdslkedname_ = val;
  }

  public String getPdslkedname()
  {
    return pdslkedname_;
  }

  public void setLkedrc(String val)
  {
    lkedrc_ = val;
  }

  public String getLkedrc()
  {
    return lkedrc_;
  }

  public void setHfsalias(String val)
  {
    hfsalias_ = val;
  }

  public String getHfsalias()
  {
    return hfsalias_;
  }

  public void setPdsalias(String val)
  {
    pdsalias_ = val;
  }

  public String getPdsalias()
  {
    return pdsalias_;
  }

  public void setSetcode(String val)
  {
    setcode_ = val;
  }

  public String getSetcode()
  {
    return setcode_;
  }

  public void setSyslibs(String val)
  {
    syslibs_ = val;
  }

  public String getSyslibs()
  {
    return syslibs_;
  }

  public void setInclude(String val)
  {
    include_ = val;
  }

  public String getInclude()
  {
    return include_;
  }

  public void setOrder(String val)
  {
    order_ = val;
  }

  public String getOrder()
  {
    return order_;
  }

  public void setEntry(String val)
  {
    entry_ = val;
  }

  public String getEntry()
  {
    return entry_;
  }

  public void setExtattr(String val)
  {
    extattr_ = val;
  }

  public String getExtattr()
  {
    return extattr_;
  }

  public void setJclinmode(String val)
  {
    jclinmode_ = val;
  }

  public String getJclinmode()
  {
    return jclinmode_;
  }

  public void setLkedparms(String val)
  {
    lkedparms_ = val;
  }

  public String getLkedparms()
  {
    return lkedparms_;
  }

  public void setJclinlkedparms(String val)
  {
    jclinlkedparms_ = val;
  }

  public String getJclinlkedparms()
  {
    return jclinlkedparms_;
  }

  public void setVplsecurity(String val)
  {
    vplsecurity_ = val;
  }

  public String getVplsecurity()
  {
    return vplsecurity_;
  }

  public void setVplpartqual(String val)
  {
    vplpartqual_ = val;
  }

  public String getVplpartqual()
  {
    return vplpartqual_;
  }

  public void setLibraryDD(String val)
  {
    libraryDD_ = val;
  }

  public String getLibraryDD()
  {
    return libraryDD_;
  }

  public void setSysLibsLibraryDD(String val)
  {
    sysLibsLibraryDD_ = val;
  }
  
  public String getSysLibsLibraryDD()
  {
    return sysLibsLibraryDD_;
  }
  
  public void setSideDeckAppendDD(String val)
  {
    sideDeckAppendDD_ = val;
  }

  public String getSideDeckAppendDD()
  {
    return sideDeckAppendDD_;
  }

  public String toString()
  {
    return longfilename_ + " " + shortfilename_ + " " + distlib_ + " " + 
           fmidlist_ + " " + comp_ + " " + copytype_ + " " + parttype_;
  }
} //end of PkgFile class
