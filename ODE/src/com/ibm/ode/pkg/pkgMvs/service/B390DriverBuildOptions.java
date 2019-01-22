package com.ibm.ode.pkg.pkgMvs.service;

import com.ibm.ode.pkg.pkgMvs.MvsProperties;

public class B390DriverBuildOptions 
{
  private boolean runScan=false;   //
  private String  shipTo="";       //node.userID
  private String  shipType="";     //object type (obj,)

  /******************************************************************
   * Constructor
   * @see B390APARInfo, B390CommandInterface, B390APARDriver
   **/
  public B390DriverBuildOptions() throws B390CommandException
  {
    super();
    if (MvsProperties.runScan != null)
    {
      if (MvsProperties.runScan.equalsIgnoreCase( "yes" ))
        this.setRunScan( true );
    }
    this.setShipInfo( MvsProperties.shipTo, MvsProperties.shipType );
  }
  public B390DriverBuildOptions(boolean runScan,String shipTo, String shipType) 
    throws B390CommandException
  {
    super();
    this.setRunScan(runScan);
    this.setShipInfo(shipTo,shipType);
  }

  public boolean getRunScan() 
  {
    return runScan;
  }
  public void setRunScan(boolean runScan) 
  {
    this.runScan=runScan;
  }
  public void setShipInfo(String shipTo, String shipType) throws B390CommandException
  {
    if (shipTo!=null && shipType!=null)
    {
      this.shipTo=shipTo;
      this.shipType=shipType;
    }
  }
  public String getShipTo() throws B390CommandException
  {
    if (shipTo!="") 
    {
      if (shipType=="") throw new B390CommandException("ShipTo "+shipTo+" requires a ShipType value");
    }else 
    {
      if (shipType!="") throw new B390CommandException("ShipType "+shipType+" requires a ShipTo value");
    } 
    return shipTo;
  }
  public String getShipType() 
    throws B390CommandException
  {
    if (shipType!="") 
    {
      if (shipTo=="") throw new B390CommandException("ShipType "+shipType+" requires a ShipTo value");
    }else 
    {
      if (shipTo!="") throw new B390CommandException("ShipTo "+shipTo+" requires a ShipType value");
    } 
    return shipType;
  }
}
