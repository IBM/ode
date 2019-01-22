package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
**/
class SFDeleteCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    SFInfo[] sel = frame.getFileSelections();
    if (sel.length == 0)
    {
      if (!SandboxFiles.command_line.isState( "-quiet" ))
      {
        JOptionPane.showMessageDialog( frame, 
                                       "No files selected to delete!",
                                       "Delete files",
                                       JOptionPane.WARNING_MESSAGE );
      }
    }
    else
    {
      StringBuffer sb = new StringBuffer( "" );
      for (int i = 0; i < sel.length; ++i)
        sb.append( "\n" ).append( sel[i].getFullPath() );
      int option = JOptionPane.YES_OPTION;
      if (!SandboxFiles.command_line.isState( "-quiet" ))
      {
        String[] msg = {"Delete these files?:", sb.toString() };
        option = JOptionPane.showConfirmDialog( frame, msg,
                                                "Delete files",
                                                JOptionPane.YES_NO_OPTION );
      }
      Interface.printDebug( "@@@ would delete file(s)" );
//      if (option == JOptionPane.YES_OPTION)
//      {
//        // WARNING! We must use mksb for sandboxes and mkbb for
//        // backing builds!
//        String cmd_part = " -auto -undo " +
//                          SandboxFiles.list_state.getRcFileFlag() + " ";
//        for (int i = 0; i < sel.length; ++i)
//        {
//          String cmd;
//          cmd = (sel[i].isBackingBuild() ? "mkbb" : "mksb") +
//                                cmd_part + sel[i].getSandboxName();
//          Interface.printDebug( "About to run: " + cmd );
//          GuiCommand guiCmd = new GuiCommand( cmd, frame );
//          guiCmd.runCommand( true, // show output if error or warning
//                             true  // modal
//                             );
//        }
//        frame.refresh(); // show the new table
//      }
    }
  }
}


