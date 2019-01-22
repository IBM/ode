package com.ibm.ode.pkg.test;

import java.rmi.*;          
import java.rmi.server.UnicastRemoteObject;

/**
* A test server used to test the ODE packaging API
*
* The implementation of the remote packaging interface can be 
*   found in the object com.ibm.ode.pkg.test.TestPkgApi
*
* A test client program that can be used in conjuction with
*   this test server can be found at com.ibm.ode.pkg.test.TestPkgClient                                
*                                                                           
* Execute TestPkgServer as follows:                                      
* - add odei<rel_num>_b<build_num>_tools.jar to the CLASSPATH environment
*   variable, where <rel_num> is the ode release number, and <build_num> 
*   is the build number, i.e. odei2.3_b13_tools.jar      
* - execute the command: java com.ibm.ode.pkg.test.TestPkgServer            
* - test server program must be manually stopped (via Ctrl-C)  
*
* To use TestPkgServer as a simulated remote packaging server with ODE, 
* start the TestPkgServer program as stated above, and then specify
* the following as a command line variable to the ODE build/mk command:
* - PKG_API_URL="<hostname>/PkgApiRemoteObject"
* where <hostname> is the hostname of the machine the server is running on,
* for example:
* - PKG_API_URL="ode3.raleigh.ibm.com/PkgApiRemoteObject"
* The other variables that must be defined from the build/mk command line 
* to fully simulate Official and/or Service packaging are:
* - PKG_CLASS ("SP" or "IPP")
* - PKG_TYPE ("OFFICIAL" or "USER")
* - PKG_FIX_STRATEGY (depends on the platform, could be "Cumulative" or "Refresh")
*                                                                           
**/

public class TestPkgServer
{

  public TestPkgServer()
  { } 

  public static void main(String[] args)
  {
    try 
    {
			// Instantiate the server and the API implementation
      TestPkgServer server = new TestPkgServer();
      TestPkgApi testPkgApi  = new TestPkgApi();

      // Export and register the PkgApi object to be accessed remotely
      System.setSecurityManager(new RMISecurityManager());
      UnicastRemoteObject.exportObject( testPkgApi );
      Naming.rebind( "PkgApiRemoteObject", testPkgApi );
      System.out.println("PkiApiRemoteObject is registered for RMI.");

      // Display the version of the API
      System.out.println("PkgApiVersion is " + testPkgApi.getPackageApiVersion());
    }
    catch(Exception e)
    {
      System.out.println(e);
      System.exit(0);
    }

    // loop here until Ctrl-C by operator.
    while(true) 
    {  }  
  }

}
