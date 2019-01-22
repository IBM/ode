package com.ibm.ode.lib.io;

/**
 * An interface to be used by the "main" programs of each tool - to
 * facilitate the function of each tool defining it's own usage but having
 * the common CommandLine class being able to call this method.
**/
public interface UsagePrintable
{
  void printUsage();
}
