package com.ibm.ode.lib.util;

import java.io.*;
import java.util.*;

/**
 *
 * Execute the command in an OS/2 shell defaulted to "cmd.exe /C". 
 * Note that from unofficial Java doc (FAQ for Programmers), a command 
 * like 'cmd.exe /C "mycommand options"' has to be broken up into three 
 * tokens "cmd.exe", "/C", and "mycommand options". Otherwise the shell 
 * will just treat mycommand as the only command for cmd.exe to run. 
 *
 * Also note that the command passed in as a whole (whether itself has options
 * or arguments) is treated as an argument to the shell command.
 *
 * If .bat files are executed, the standard output and standard error can
 * not be retrieved.  Thus, it is highly recommended that .cmd files are used
 * in place of .bat files.
 *
 * @see          ShellSystemCall
 * @version      1.3.1.1 99/01/28
 * @author       Ann Griffiths
 *
**/

public class OS2ShellSystemCall extends ShellSystemCall
{

   /************************************************************************
    *
    * Constructor sets 'cmd.exe' as SHELL and '/C' as the SHELL option in
    * the base class
    *
   **/

   public OS2ShellSystemCall()
   {
      super();
      this.setShell( "cmd.exe" );
      this.setShellOption( "/C" );
   }


   /************************************************************************
    *
    * Override the dceExec method in the base class
    *
    * @param command              Command to execute
    * @param id                   DCE login id
    * @param password             DCE password
    * @param envp                 Environment variables
    * @param dcelogin             Path and name of dce login executable
    * @return                     Return code            
    * @exception IOException thrown from Runtime.exec() for IO problem 
    * @exception InterruptedException thrown when the process is interrupted 
    *
   **/

   public int dceExec( String command, String id, String password,
                       String envp[], String dcelogin )
      throws IOException, InterruptedException
   {

      String[] commandArray;

      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = getShell();
      commandArray[5] = getShellOption();
      commandArray[6] = command;

      return exec( commandArray, envp );

   }


   /************************************************************************
    *
    * Override the dceExec method in the base class
    *
    * @param command              Command to execute
    * @param id                   DCE login id
    * @param password             DCE password
    * @param envp                 Environment variables
    * @param dcelogin             Path and name of dce login executable
    * @param interleave           Indicates if stdout/stderr should interleave
    * @return                     Return code            
    * @exception IOException thrown from Runtime.exec() for IO problem 
    * @exception InterruptedException thrown when the process is interrupted 
    *
   **/

   public int dceExec( String command, String id, String password,
                       String envp[], String dcelogin, boolean interleave )
      throws IOException, InterruptedException
   {

      String[] commandArray;

      // If interleave is set to true, then redirect the error stream
      // before calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
         updatedCmd = getCommandWithInterleaving( command );
      }

      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = getShell();
      commandArray[5] = getShellOption();
      commandArray[6] = updatedCmd;

      return exec( commandArray, envp );

   }


   /************************************************************************
    *
    * Override the dceExec method in the base class
    *
    * @param command              Command to execute
    * @param id                   DCE login id
    * @param password             DCE password
    * @param envp                 Environment variables
    * @return                     Return code            
    * @exception IOException thrown from Runtime.exec() for IO problem 
    * @exception InterruptedException thrown when the process is interrupted 
    *
   **/

   public int dceExec( String command, String id, 
                       String password, String envp[] )
      throws IOException, InterruptedException
   {
      return dceExec( command, id, password, envp, "dcelogin" );
   }


   /************************************************************************
    *
    * Override the dceExec method in the base class
    *
    * @param command              Command to execute
    * @param id                   DCE login id
    * @param password             DCE password
    * @param envp                 Environment variables
    * @param interleave           Indicates if stdout/stderr should be
    *                             interleaved
    * @return                     Return code            
    * @exception IOException thrown from Runtime.exec() for IO problem 
    * @exception InterruptedException thrown when the process is interrupted 
    *
   **/

   public int dceExec( String command, String id, 
                       String password, String envp[], boolean interleave )
      throws IOException, InterruptedException
   {
      return dceExec( command, id, password, envp, "dcelogin", interleave );
   }

}
