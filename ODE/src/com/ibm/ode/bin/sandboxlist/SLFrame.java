package com.ibm.ode.bin.sandboxlist;


import java.awt.*;
import java.awt.event.*;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.event.TableModelEvent; 
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.bin.gui.*;


public class SLFrame extends JFrame implements ActionListener
{
  private int selections;
  private boolean[] selected;
  SLState list; // this is a copy, for convenience, but maybe only
            // SandboxList.list_state should be used, for maintenance reasons.
  int sbcnt = 0;
  GuiTableSorter sorter;
  WindowAdapter windowListener;
  JMenuBar menuBar;
  JMenu fileMenu, sboxMenu, helpMenu;
  SLMenuItem workonMenuItem, buildMenuItem, showFilesMenuItem,
//             modifyMenuItem,
//             retargetMenuItem,
             deleteMenuItem;
  JTable table;
  boolean disable = true; // disable certain menu items

  public void initSLFrame( SLState list )
  {
    setTitle( "ODE GUI" );
    setLocation( 100, 100 );

    sorter = new GuiTableSorter( new SLTableModel( list ) );
    table = new JTable( sorter );
    sorter.addMouseListenerToHeaderInTable( table );
    table.setPreferredScrollableViewportSize( new Dimension( 600, 80 ) );
    this.list = list;

    sbcnt = list.size();
    selected = new boolean[sbcnt];
    ListSelectionModel rowSM = table.getSelectionModel();
    rowSM.addListSelectionListener(
          new ListSelectionListener()
          {
            public void valueChanged( ListSelectionEvent e )
            {
              // Ignore ongoing adjustment messages.
              if (e.getValueIsAdjusting())
                return;
              ListSelectionModel lsm = (ListSelectionModel)e.getSource();
              if (lsm.isSelectionEmpty())
              {
                selections = 0;
                for (int i = 0; i < sbcnt; ++i)
                  selected[i] = false;
              }
              else
              {
                selections = 0;
                for (int i = 0; i < sbcnt; ++i)
                {
                  if (selected[i] = lsm.isSelectedIndex( i ))
                  {
                    selections++;
                  }
                }
              }
            }
          }
        );

    disable = (list.size() == 0);

    // Add menu bar first
    menuBar = new JMenuBar();
    setJMenuBar( menuBar );

    // File menu
    fileMenu = new JMenu( "File" );
    fileMenu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( fileMenu );

//    fileMenu.add( new SLMenuItem( this, new SLOpenCmd(), false,
//                                  "Open Sandbox List Here...", 
//                                  KeyEvent.VK_O ) );

    fileMenu.add( new SLMenuItem( this, new SLRefreshCmd(), false,
                                  "Refresh", KeyEvent.VK_R,
                                  ActionEvent.ALT_MASK ) );

    fileMenu.add( new SLMenuItem( this, new SLExitCmd(), false,
                                  "Exit", KeyEvent.VK_X,
                                  ActionEvent.ALT_MASK ) );

    // Sandbox Menu

    sboxMenu = new JMenu( "Sandbox" );
    sboxMenu.setMnemonic( KeyEvent.VK_S );
    menuBar.add( sboxMenu );

    sboxMenu.add( new SLMenuItem( this, new SLCreateCmd( true ), false,
                                  "Create Backing Build...",
                                  KeyEvent.VK_U ) );

    sboxMenu.add( new SLMenuItem( this, new SLCreateCmd( false ), false,
                                  "Create Sandbox...",
                                  KeyEvent.VK_C ) );

    workonMenuItem = new SLMenuItem( this, new SLWorkonCmd(), disable,
                                  "Workon...", KeyEvent.VK_W,
                                  ActionEvent.ALT_MASK );
    sboxMenu.add( workonMenuItem );

    buildMenuItem = new SLMenuItem( this, new SLBuildCmd(), disable,
                                    "Build...", KeyEvent.VK_B,
                                    ActionEvent.ALT_MASK );
    sboxMenu.add( buildMenuItem );

    showFilesMenuItem = new SLMenuItem( this, new SLShowFilesCmd(), disable,
                                        "Show files...", KeyEvent.VK_F );
    sboxMenu.add( showFilesMenuItem );

//    modifyMenuItem = new SLMenuItem( this, new SLModifyCmd(), disable,
//                                     "Modify...", KeyEvent.VK_M );
//    sboxMenu.add( modifyMenuItem );

//    retargetMenuItem = new SLMenuItem( this, new SLRetargetCmd(), disable,
//                                       "Retarget...", KeyEvent.VK_T );
//    sboxMenu.add( retargetMenuItem );

    deleteMenuItem = new SLMenuItem( this, new SLDeleteCmd(), disable,
                                     "Delete...", KeyEvent.VK_D );
    sboxMenu.add( deleteMenuItem );

    // Help Menu

    helpMenu = new JMenu( "Help" );
    helpMenu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( helpMenu );

    helpMenu.add( new SLMenuItem( this, new SLHelpCmd(), false,
                                  "Help Contents", KeyEvent.VK_C ) );

    helpMenu.add( new SLMenuItem( this, new SLDocumentationCmd(), false,
                                  "Documentation", KeyEvent.VK_D ) );

    helpMenu.add( new SLMenuItem( this, new SLAboutCmd(), false,
                                  "About", KeyEvent.VK_A ) );

    // adjust column sizes
    initColumnSizes( table, sorter );

    //Create the scroll pane and add the table to it. 
    JScrollPane scrollPane = new JScrollPane( table );

    //Add the scroll pane
    getContentPane().add( scrollPane, BorderLayout.CENTER );

    windowListener = new WindowAdapter()
                         { 
                           public void windowClosing( WindowEvent e )
                           {
                             SandboxList.exit( 0 );
                           }
                         };
    addWindowListener( windowListener );
  }


  /**
   * adjust initial column sizes - an example of a programmer's "black art"
  **/
  private void initColumnSizes( JTable table, GuiTableSorter sorter )
  {
    TableColumnModel tcm = table.getColumnModel();

    tcm.getColumn( 0 ).setPreferredWidth( 50 ); // sandbox

    tcm.getColumn( 1 ).setPreferredWidth( 150 ); // base

    tcm.getColumn( 2 ).setPreferredWidth( 100 ); // variable base

    tcm.getColumn( 3 ).setMinWidth( 50 ); // backing build
    tcm.getColumn( 3 ).setPreferredWidth( 80 ); // backing build
    tcm.getColumn( 3 ).setMaxWidth( 80 ); // backing build

    tcm.getColumn( 4 ).setMinWidth( 30 ); // default
    tcm.getColumn( 4 ).setPreferredWidth( 40 ); // default
    tcm.getColumn( 4 ).setMaxWidth( 45 ); // default

    tcm.getColumn( 5 ).setMinWidth( 30 ); // status
    tcm.getColumn( 5 ).setPreferredWidth(  40 ); // status
    tcm.getColumn( 5 ).setMaxWidth( 70 ); // status
  }


  /**
   * recompute the window contents and display them
  **/
  public void refresh()
  { 
    ListSelectionModel rowSM = table.getSelectionModel();
    rowSM.clearSelection();
    SandboxList.list_state = new SLState(
                                    SandboxList.command_line.getRcfile() );
    list = SandboxList.list_state;
    sbcnt = list.size();
    selected = new boolean[sbcnt];
    selections = 0;
    disable = (list.size() == 0);
    // we need to rerefresh certain menu items
    workonMenuItem.setEnabled( !disable );
    buildMenuItem.setEnabled( !disable );
    showFilesMenuItem.setEnabled( !disable );
//    modifyMenuItem.setEnabled( !disable );
//    retargetMenuItem.setEnabled( !disable );
    deleteMenuItem.setEnabled( !disable );

    sorter.setModel( new SLTableModel( list ) );
    sorter.tableChanged( new TableModelEvent( sorter ) );
    Interface.printDebug( "refresh() ending" );
  }


  public void actionPerformed( ActionEvent e )
  {
    SLMenuItem source = (SLMenuItem)( e.getSource() );
    source.sblMenuAction();
  }


  public SLInfo[] getSandboxSelections()
  {
    SLInfo[] ret = new SLInfo[selections];
    Interface.printDebug( "selections=" + selections );
    int j = 0;
    for (int i = 0; i < list.size(); ++i)
    {
      if (selected[i])
      {
        Interface.printDebug( "selected screen index=" + i +
                              " original index=" + sorter.getMappedIndex( i ) );
        ret[j] = list.getSLInfo( sorter.getMappedIndex( i ) );
        ++j;
      }
    }
    return ret;
  }


  class SLTableModel extends AbstractTableModel
  {
    final String[] columnNames = {"Sandbox", 
                                  "Base",
                                  "Variable Base",
                                  "Backing Build",
                                  "Default",
                                  "Status"};
    Object[][] data;

    public SLTableModel( SLState list )
    {
      data = new Object[list.size()][columnNames.length];
      for (int i = 0; i < list.size(); ++i)
      {
        data[i][0] = list.getSandboxName( i );
        data[i][1] = list.getBase( i );
        data[i][2] = list.getVariableBase( i );
        data[i][3] = new Boolean( list.isBackingBuild( i ) );
        data[i][4] = new Boolean( list.isDefault( i ) );
        data[i][5] = list.getStatus( i );
      }
    }

    public int getColumnCount()
    {
      return columnNames.length;
    }
    
    public int getRowCount()
    {
      return data.length;
    }

    public String getColumnName( int col )
    {
      return columnNames[col];
    }

    public Object getValueAt( int row, int col )
    {
      return data[row][col];
    }

    /*
     * Implement this to show a checkbox instead of "true" or "false"
     * for the boolean columns.
     */
    public Class getColumnClass( int c )
    {
      return getValueAt( 0, c ).getClass();
    }

  }
}
