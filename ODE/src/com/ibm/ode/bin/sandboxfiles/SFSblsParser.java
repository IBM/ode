package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;
import com.ibm.ode.lib.string.*;

public class SFSblsParser
{
  private String[] tempChain;
  private String[] filespecs;
  private String quotedFilespecs;
  private boolean checkDuplicates;

  private Object[][] data;
  private int dataSize;
  private SandboxFiles main_class;

  /**
   * This constructor looks at the program's arguments to set up for the
   * subsequent parse() operation.
  **/
  SFSblsParser( SandboxFiles main_class )
  {
    this.main_class = main_class;
    filespecs = main_class.command_line.getUnqualifiedVariables();
    quotedFilespecs = maybeQuote( filespecs );
    checkDuplicates = filespecs != null && filespecs.length > 1;
    parse();
  }


  /**
   * This is like a join with a blank as separator, except that if sar[i]
   * has a blank, then sar[i] will be double quoted.
   * This is to handle the possibility of files with blanks in them on Windows.
   * No analysis of the string is done other than looking for blanks.
   * In particular, no check is done for already existing quote characters.
   * It is assumed that none exist in the filespec arguments.
  **/
  public String maybeQuote( String[] sar )
  {
    if (sar == null)
      return "";
    StringBuffer sb = new StringBuffer( "" );
    for (int i = 0; i < sar.length; ++i)
    {
      if (i > 0)
        sb.append( ' ' );
      if (sar[i].indexOf( ' ' ) != -1)
      {
        sb.append( '"' ).append( sar[i] ).append( '"' );
      }
      else
        sb.append( sar[i] );
    }
    return sb.toString();
  }


  /**
   * Returns the data array produced by calling parse(). Do not call this
   * until parse() is called!
  **/
  Object[][] getDataArray()
  {
    return data;
  }


  /**
   * Do not call this until parse() is called. It returns the number of rows
   * that are filled in the data arrray.
  **/
  int getDataSize()
  {
    return dataSize;
  }


  /**
   * Runs sbls and produces an Object[][] array with the first n
   * rows filled. There may be rows at the end that are empty. This method
   * returns the number of rows filled. Use getDataArray() after running
   * this method, to retrieve the array itself. Use getDataSize() for the
   * number of rows.
  **/
  private void parse()
  {
    String[] chain = main_class.chain;
    if (chain == null)
      tempChain = null;
    else if (main_class.command_line.isState( "-sandboxonly" ))
    {
      tempChain = new String[1];
      tempChain[0] = chain[0];
    }
    else
      tempChain = chain;

    boolean ignoreCase = !PlatformConstants.onCaseSensitiveOS();

    // Run the sbls cmd, capturing the output in StringBuffer out.
    Interface.printDebug( "@@@ SFRunSbls with tempChain '" +
                          StringTools.join( chain,
                                   System.getProperty( "path.separator" ) ) +
                          "'" );
    StringBuffer out = SFRunSbls.runSbls( tempChain,
                         !main_class.command_line.isState( "-norecurse" ),
                                quotedFilespecs, main_class );
    String line;
    Hashtable hash = new Hashtable(); // keep the compiler happy
    Integer uselessValue = new Integer( 0 );
    // For now, we only need to check duplicates if there is more than
    // one filespec because sbls does each filespec independently.
    // If we implement more flags in the future, we may find that even a
    // single filespec could cause duplicates in the list of files,
    // if unusual things are being done with the value of BACKED_SANDBOXDIR
    // and multiple calls are made to sbls, one per item in the purported
    // backing chain. An example is allowing multiple sandboxes with their
    // possibly shared backing builds, for a 'comparison of sandboxes'
    // sort of option. In other words we might implement two different
    // calls of sbls, one for the BACKED_SANDBOXDIR of sandbox1 and another
    // call for the BACKED_SANDBOXDIR of sandbox2.
    if (checkDuplicates)
      hash = new Hashtable( 500 );
    
    try
    {
      LineNumberReader lineCounter =
              new LineNumberReader( new StringReader( out.toString() ) );
      int approx = 0;
      while ( ( line = lineCounter.readLine() ) != null )
      {
        line = line.trim();
        if (!line.equals( "" ) && line.charAt( 0 ) != '>')
          ++approx;
      }
      if (approx == 0)
      {
        data = new Object[0][0];
        dataSize = 0;
        return;
      }
      data = new Object[approx][9];
      dataSize = 0;
      LineNumberReader lineReader =
              new LineNumberReader( new StringReader( out.toString() ) );
      SFInfo sfi = new SFInfo( tempChain );
      while ( ( line = lineReader.readLine() ) != null )
      {
        sfi.parse( line );
        if (sfi.getStatus() == SFInfo.STATUS_OK)
        {
          // If there is only one filespec, then there cannot be duplicates
          // and we bypass the code that checks for and eliminates duplicates.
          if (checkDuplicates)
          {
            // is the fullPath in the hash already?
            if (ignoreCase)
            {
              String lowKey = sfi.getFullPath().toLowerCase();
              if (hash.containsKey( lowKey ))
                continue;
              hash.put( lowKey, uselessValue );
            }
            else
            {
              String key = sfi.getFullPath();
              if (hash.containsKey( key ))
                continue;
              hash.put( key, uselessValue );
            }
          }
          // copy info to data array
          data[dataSize][0] = sfi.getBase();
          data[dataSize][1] = sfi.getPath();
          data[dataSize][2] = sfi.getDate();
          data[dataSize][3] = new Long( sfi.getSize() );
          data[dataSize][4] = sfi.getFileName();
          data[dataSize][5] = sfi.getSuffix();
          data[dataSize][6] = sfi.getType();
          data[dataSize][7] = new Integer( sfi.getPosition() );
          data[dataSize][8] = sfi.getFullPath();
          ++dataSize;
        }
        else if (sfi.getStatus() == SFInfo.STATUS_BLANK )
          continue; // blank line - skip the little feller
        else if (main_class.command_line.isState( "-debug" ))
        {
          // we can end up here due to dfs error messages or sbls /dev
          Interface.printDebug( "Error when using sbls to read files: " +
                                sfi.getStatusMsg() );
//          String[] msg = { "Error when using sbls to read files:",
//                           sfi.getStatusMsg(),
//                           "Continue running SandboxFiles?"
//                         };
//          int rc = JOptionPane.showOptionDialog( main_class.frame,
//                                                 msg, "Error",
//                                                 JOptionPane.YES_NO_OPTION,
//                                                 JOptionPane.ERROR_MESSAGE,
//                                                 null, null, null
//                                                 );
//          if (rc != JOptionPane.YES_OPTION)
//            SandboxFiles.exit( 1 );
        }
      }
    }
    catch (IOException e)
    {
      GuiTextMsg.showErrorMsg( main_class.frame,
                               "SandboxFiles sbls parser: " + e.getMessage(),
                               "ERROR: IOException" );
    }
  }

}
