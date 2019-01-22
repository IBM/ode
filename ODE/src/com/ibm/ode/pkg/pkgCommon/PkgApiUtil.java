package com.ibm.ode.pkg.pkgCommon;

import com.ibm.sdwb.bps.api.servicepkg.*;
import com.ibm.ode.pkg.api.*;
import java.rmi.*;
import java.net.MalformedURLException;

/**
 * This class implements utility methods used in conjunction
 *  with the remote packaging interface, ServicePackagingInterface
 */
  public class PkgApiUtil
  {
   /**
    * default constructor
    */
   public PkgApiUtil()
   {
   }

  /*******************************************************************************
   * Return the remote reference to the ServicePackagingInterface,
   *   as specified by the URL
   **/
   public static ServicePackagingInterface getServicePackagingInterface( String url_ ) throws                  
                  java.rmi.NotBoundException, 
                  java.net.MalformedURLException, 
                  java.rmi.UnknownHostException, 
                  java.rmi.RemoteException,
                  Exception
   {
     try
     {
       System.setSecurityManager( new RMISecurityManager() );
       return (ServicePackagingInterface) Naming.lookup( "//" + url_ );
     } 
     catch( NotBoundException e )
     {
       throw new NotBoundException( e.getMessage() );
     }
     catch( MalformedURLException e )
     {
       throw new MalformedURLException( e.getMessage() );
     }
     catch( UnknownHostException e )
     {
       throw new UnknownHostException( e.getMessage() );
     }
     catch( RemoteException e )
     {
       throw new RemoteException( e.getMessage() );
     }
     catch( Exception e )
     {
       throw new Exception( e.getMessage() );
     }
   } 
  /*******************************************************************************
   * Verifies the version of the API is compatible with the server version
   **/
   public static void verifyPkgApiVersion( ServicePackagingInterface api )
         {
     boolean versionFound = false;
                 try {
       String serverApiVersion = api.getPackageApiVersion();
       String[] supportedApiVersions = PkgApiVersion.getPkgApiSupportedVersions();
       for (int i=0; i < supportedApiVersions.length; i++)
       {
         if (serverApiVersion.equals( supportedApiVersions[i] ))
           versionFound = true;
       }
       if (!versionFound)
                   {
         String msg;
         msg = "Service Packaging API version not supported";
         msg += "\nServer is version: " + serverApiVersion;
         msg += "\nApi supports the following versions:  ";
         for( int i=0; i<supportedApiVersions.length; i++ )
                           {
           msg += supportedApiVersions[i] + "  ";
                           }
         throw new SecurityException( msg );
       }
     }
     catch (Exception e)
                 {
       System.out.println( e );
                 }
         }

}
   

