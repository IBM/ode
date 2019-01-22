package com.ibm.ode.pkg.test;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import com.ibm.sdwb.bps.api.servicepkg.*;
import com.ibm.ode.pkg.api.PkgApiVersion;


/** A test client program to the PkgApiServer program.  This program can
 *  be used to test the implementation of the methods in the TestPkgApi
 *  class, which implements the remote interface ServicePackagingInterface.
 *
 *  Execute TestPkgClient as follows:
 *  - add odei<rel_num>_b<build_num>_tools.jar to the CLASSPATH environment
 *    variable, where <rel_num> is the ode release number, and <build_num> 
 *    is the build number, i.e. odei2.3_b13_tools.jar
 *  - start the test server TestPkgServer by executing the following from
 *    the command line:
 *      java com.ibm.ode.pkg.test.TestPkgServer
 *  - similarly, run the test client as follows:
 *      java com.ibm.ode.pkg.test.TestPkgClient <hostname>
 *    where <hostname> is the hostname of the machine the test server was
 *    started on, i.e.
 *      java com.ibm.ode.pkg.test.TestPkgClient ode3.raleigh.ibm.com
 **/

public class TestPkgClient
{ 
  public TestPkgClient ()
  {
  }

  public static void main( String args[] )
  { 

    boolean error = false;
    boolean versionFound = false;
    if (args.length == 0)
		{
      System.out.println("Usage: ");
      System.out.println("  java COM.ibm.ode.pkg.test.TestPkgClient <hostname>");
      System.exit( 1 );
    }
    String hostname = args[0];
    System.out.println( " hostname is " + hostname );


		try 
    {
      // Obtain a remote reference to the ServicePackagingInterface
			System.out.println("Looking up API ...");
      ServicePackagingInterface api = (ServicePackagingInterface) Naming.lookup
           ( "rmi://" + hostname + "/ServicePackagingServer" );
      //ServicePackagingInterface api = (ServicePackagingInterface) Naming.lookup
      //     ( "rmi://" + hostname + "/PkgApiRemoteObject" );
 
			// Perform Version Verification
      String serverApiVersion = api.getPackageApiVersion();
      String[] supportedApiVersions = PkgApiVersion.getPkgApiSupportedVersions();
      for (int i=0; i < supportedApiVersions.length; i++)
      {
        if (serverApiVersion.equals( supportedApiVersions[i] ))
          versionFound = true;
      }
      if (!versionFound)
			{
        System.out.println("VERSION NOT SUPPORTED: Server is version: " + serverApiVersion); 
        System.out.print("Api supports the following versions: ");
        for( int i=0; i<supportedApiVersions.length; i++ )
			  {
          System.out.println( supportedApiVersions[i] + "  " );
			  }
        error = true;
      }

      // Test out all other API methods

      // getLevelMembersAsApars
      String[] levels = {"level1", "level2"};
      String[] releases = {"release1", "release2"};
      String cmvcfam = "myfamily";
      System.out.println("calling getLevelMembersAsApars...");
      String[] apars = api.getLevelMembersAsApars( levels, releases, cmvcfam);
      for (int i = 0; i < apars.length; i++)
        System.out.println("apar returned from server: " + apars[i]);

      // addFMID_PTFPair
      String[] ptfs = {"ptf1", "ptf2", "ptf3", "ptf4"};
      String[] fmids = {"fmid1", "fmid2", "fmid3", "fmid4"};
      System.out.println("Calling addFMID_PTFPair...");
      api.addFMID_PTFPair( ptfs, fmids, levels, releases, cmvcfam );

      // getPTFsFromFMIDs
      System.out.println("Calling getPTFsFromFMIDs...");
      String[] newptfs = api.getPTFsFromFMIDs( fmids, levels, releases, cmvcfam );
      for (int i = 0; i < fmids.length; i++)
          System.out.println("PTF from FMID: " + fmids[i] + " is: " + newptfs[i]);

      // makeDuplicateLevels
      System.out.println("Calling makeDuplicateLevels...");
      api.makeDuplicateLevels( cmvcfam, releases[0], levels[0], newptfs );

    }
    catch(Exception e)
    {
      System.out.println(e);
		} 
   
    if (error) 
      System.exit( 1 );
 
  }
}

