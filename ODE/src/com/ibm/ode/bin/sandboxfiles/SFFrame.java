package com.ibm.ode.bin.sandboxfiles;


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
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.bin.gui.*;


public class SFFrame extends JFrame implements ActionListener
{
  private int selections;
  private boolean[] selected;
  SFState list; // this is a copy, for convenience
  int filecnt = 0;
  GuiTableSorter sorter;
  WindowAdapter windowListener;
  JMenuBar menuBar;
  JMenu fileMenu, sboxMenu, helpMenu;
  SandboxFiles main_class;

  // the following menu items can be enabled or disabled
//  SFMenuItem // browseMenuItem,
//             // buildMenuItem,
//             // diffMenuItem,
//             // mergeMenuItem,
//             copyPathMenuItem,
//             deleteMenuItem;

  JTable table;
  boolean disable = true; // disable certain menu items

  public SFFrame( SandboxFiles main_class, SFState list )
  {
    this.main_class = main_class;
    this.list = list;
  }

  public void initSFFrame()
  {
    String[] filespecs = main_class.command_line.getUnqualifiedVariables();
    setTitle( "Sandbox Files " + StringTools.join( filespecs, " " ) );
    setLocation( 80, 120 );

    sorter = new GuiTableSorter( new SFTableModel( list ) );
    table = new JTable( sorter );
    sorter.addMouseListenerToHeaderInTable( table );
    table.setPreferredScrollableViewportSize( new Dimension( 900, 150 ) );

    filecnt = list.size();
    selected = new boolean[filecnt];
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
                for (int i = 0; i < filecnt; ++i)
                  selected[i] = false;
              }
              else
              {
                selections = 0;
                for (int i = 0; i < filecnt; ++i)
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

    // Sandbox menu
    sboxMenu = new JMenu( "Sandbox" );
    sboxMenu.setMnemonic( KeyEvent.VK_S );
    menuBar.add( sboxMenu );

//    sboxMenu.add( new SFMenuItem( this, new SFNewCmd(), false,
//                                  "Start New Sandbox Files Window...", 
//                                  KeyEvent.VK_N ) );

//    sboxMenu.add( new SFMenuItem( this, new SFOpenCmd(), false,
//                                  "Open Sandbox Files Here...", 
//                                  KeyEvent.VK_O ) );

    sboxMenu.add( new SFMenuItem( this, new SFRefreshCmd(), false,
                                  "Refresh", KeyEvent.VK_R,
                                  ActionEvent.ALT_MASK ) );

    sboxMenu.add( new SFMenuItem( this, new SFExitCmd(), false,
                                  "Exit", KeyEvent.VK_X,
                                  ActionEvent.ALT_MASK ) );

    // File Menu

    fileMenu = new JMenu( "Files" );
    fileMenu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( fileMenu );

    fileMenu.add( new SFMenuItem( this, new SFEditCmd( main_class ), false,
                                  "Edit...", KeyEvent.VK_E,
                                  ActionEvent.ALT_MASK ) );

//    browseMenuItem = new SFMenuItem( this, new SFBrowseCmd(), disable,
//                                  "Browse...", KeyEvent.VK_W,
//                                  ActionEvent.ALT_MASK );
//    fileMenu.add( browseMenuItem );

//    fileMenuadd( new SFMenuItem( this, new SFUserCmd(), false,
//                                   "User Command...", KeyEvent.VK_U,
//                                   ActionEvent.ALT_MASK ) );

//    buildMenuItem = new SLMenuItem( this, new SLBuildCmd(), disable,
//                                    "Build...", KeyEvent.VK_B,
//                                    ActionEvent.ALT_MASK );
//    sboxMenu.add( buildMenuItem );

//    diffMenuItem = new SLMenuItem( this, new SLDiffCmd(), disable,
//                                    "Difference...", KeyEvent.VK_I );
//    sboxMenu.add( buildMenuItem );

//    mergeMenuItem = new SLMenuItem( this, new SLMergeCmd(), disable,
//                                    "Merge...", KeyEvent.VK_M );
//    sboxMenu.add( buildMergeItem );

//    fileMenu.add( new SFMenuItem( this, new SFCreateDirectoryCmd(), false,
//                                  "Create directory...",
//                                  KeyEvent.VK_C ) );

//    fileMenu.add( new SFMenuItem( this, new SFLinkCmd(), false,
//                                  "Link/Copy...",
//                                  KeyEvent.VK_L ) );

//    copyPathMenuItem = new SFMenuItem( this, new SFCopyPathCmd(), disable,
//                                        "Copy path to clipboard",
//                                        KeyEvent.VK_P );
//    fileMenu.add( copyPathMenuItem );

//    deleteMenuItem = new SFMenuItem( this, new SFDeleteCmd(), disable,
//                                     "Delete...", KeyEvent.VK_D );
//    fileMenu.add( deleteMenuItem );

    // Help Menu

    helpMenu = new JMenu( "Help" );
    helpMenu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( helpMenu );

    helpMenu.add( new SFMenuItem( this, new SFHelpCmd(), false,
                                  "Help Contents", KeyEvent.VK_C ) );

    helpMenu.add( new SFMenuItem( this, new SFDocumentationCmd(), false,
                                  "Documentation", KeyEvent.VK_D ) );

    helpMenu.add( new SFMenuItem( this, new SFAboutCmd(), false,
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
                             main_class.frame.dispose();
                           }
                         };
    addWindowListener( windowListener );
  }


  /**
   * Adjust initial column sizes - an example of a programmer's "black art".
   * The results are different for UNIX and Window/2000 too. sigh....
  **/
  private void initColumnSizes( JTable table, GuiTableSorter sorter )
  {
    TableColumnModel tcm = table.getColumnModel();

    tcm.getColumn( 0 ).setPreferredWidth( 100 ); // base

    tcm.getColumn( 1 ).setPreferredWidth( 250 ); // path

    tcm.getColumn( 2 ).setMinWidth( 110 ); // date-time
    tcm.getColumn( 2 ).setPreferredWidth( 140 ); // date-time
//    tcm.getColumn( 2 ).setMaxWidth( 150 ); // date-time

    tcm.getColumn( 3 ).setPreferredWidth( 30 ); // size

    tcm.getColumn( 4 ).setPreferredWidth( 90 ); // filename

    tcm.getColumn( 5 ).setPreferredWidth( 25 ); // suffix

    tcm.getColumn( 6 ).setMinWidth( 20 ); // type
    tcm.getColumn( 6 ).setPreferredWidth( 25 ); // type
//    tcm.getColumn( 6 ).setMaxWidth( 30 ); // type

    tcm.getColumn( 7 ).setMinWidth( 20 ); // position in chain
    tcm.getColumn( 7 ).setPreferredWidth(  25 ); // position in chain
    tcm.getColumn( 7 ).setMaxWidth( 25 ); // position in chain
  }


  /**
   * recompute the window contents and display them
  **/
  public void refresh()
  { 
    ListSelectionModel rowSM = table.getSelectionModel();
    rowSM.clearSelection();
    main_class.list_state = new SFState( main_class );
    list = main_class.list_state;
    filecnt = list.size();
    selected = new boolean[filecnt];
    selections = 0;
    disable = (list.size() == 0);
    // we need to refresh certain menu items
//    browseMenuItem.setEnabled( !disable );
//    buildMenuItem.setEnabled( !disable );
//    diffMenuItem.setEnabled( !disable );
//    mergeMenuItem.setEnabled( !disable );
//    copyPathMenuItem.setEnabled( !disable );
//    deleteMenuItem.setEnabled( !disable );

    sorter.setModel( new SFTableModel( list ) );
    sorter.tableChanged( new TableModelEvent( sorter ) );
  }


  public void actionPerformed( ActionEvent e )
  {
    SFMenuItem source = (SFMenuItem)( e.getSource() );
    source.sblMenuAction();
  }


  public SFInfo[] getFileSelections()
  {
    SFInfo[] ret = new SFInfo[selections];
    Interface.printDebug( "selections=" + selections );
    int j = 0;
    for (int i = 0; i < list.size(); ++i)
    {
      if (selected[i])
      {
        Interface.printDebug( "selected screen index=" + i +
                             " original index=" + sorter.getMappedIndex( i ) );
        ret[j] = list.getSFInfo( sorter.getMappedIndex( i ) );
        ++j;
      }
    }
    return ret;
  }


  class SFTableModel extends AbstractTableModel
  {
    final String[] columnNames = {
                                  "Base",
                                  "Path",
                                  "Date",
                                  "Size",
                                  "Filename",
                                  "Suffix",
                                  "Type", 
                                  "Pos"
                                  };
    Object[][] data;

    public SFTableModel( SFState state )
    {
      data = state.getDataArray();
    }

    public int getColumnCount()
    {
      return columnNames.length;
    }
    
    public int getRowCount()
    {
        return list.size();
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
     * Implement this to sort numbers numerically instead of lexically?
     */
    public Class getColumnClass( int c )
    {
      return getValueAt( 0, c ).getClass();
    }

  }
}
