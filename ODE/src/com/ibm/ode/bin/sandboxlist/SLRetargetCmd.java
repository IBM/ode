package com.ibm.ode.bin.sandboxlist;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.bin.gui.*;


/**
 * This menu action code presents a dialog that allows the user to retarget
 * a sandbox (or backing build?).
**/
class SLRetargetCmd implements SLAction
{
  public void doAction( SLFrame frame )
  {
//    SLInfo[] sel = frame.getSandboxSelections();
//    if (sel.length == 0)
//    {
//      if (!SandboxList.command_line.isState( "-quiet" ))
//      {
        JOptionPane.showMessageDialog( frame, 
                            "The 'Retarget' menu item is not implemented yet!",
                                       "Retarget sandbox",
                                       JOptionPane.WARNING_MESSAGE );
//      }
//    }
//    else
//    {
//      StringBuffer sb = new StringBuffer( "" );
//      for (int i = 0; i < sel.length; ++i)
//        sb.append( " " ).append( sel[i].getSandboxName() );
//      int option = JOptionPane.YES_OPTION;
//      if (!SandboxList.command_line.isState( "-quiet" ))
//      {
//        String[] msg = {"Delete these sandboxes?:", sb.toString() };
//        option = JOptionPane.showConfirmDialog( frame, msg,
//                                                "Delete sandboxes",
//                                                JOptionPane.YES_NO_OPTION );
//      }
//      if (option == JOptionPane.YES_OPTION)
//      {
//        // WARNING! We must use mksb for sandboxes and mkbb for
//        // backing builds!
//        String cmd_part = " -auto -undo " +
//                          SandboxList.list_state.getRcFileFlag() + " ";
//        for (int i = 0; i < sel.length; ++i)
//        {
//          String cmd;
//          cmd = (sel[i].isBackingBuild() ? "mkbb" : "mksb") +
//                                cmd_part + sel[i].getSandboxName();
//          Interface.printDebug( "About to run: " + cmd );
//          GuiCommand guiCmd = new GuiCommand( cmd );
//          guiCmd.runCommand( true, // show output if error or warning
//                             true  // modal
//                             );
//        }
//        frame.refresh(); // show the new table
//      }
//    }
  }
}


