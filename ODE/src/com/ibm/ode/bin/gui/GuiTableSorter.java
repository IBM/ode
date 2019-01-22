package com.ibm.ode.bin.gui;

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel) 
 * and itself implements TableModel. GuiTableSorter does not store or copy 
 * the data in the TableModel, instead it maintains an array of 
 * integers which it keeps the same size as the number of rows in its 
 * model. When the model changes it notifies the sorter that something 
 * has changed e.g. "rowsAdded" so that its internal array of integers 
 * can be reallocated. As requests are made of the sorter (like 
 * getValueAt(row, col) it redirects them to its model via the mapping 
 * array. That way the GuiTableSorter appears to hold another copy of the table 
 * with the rows in a different order. The sorting algorthm should be stable 
 * so that it does not move around rows when comparison shows two values to
 * be equal.
 */

import com.ibm.ode.lib.io.Interface;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class GuiTableSorter extends GuiTableMap implements Comparator
{
  int indexes[];
  boolean ascending = true;
  int sortColumn;

  public GuiTableSorter()
  {
    indexes = new int[0];
  }

  public GuiTableSorter( TableModel model )
  {
    setModel(model);
  }

  public void setModel( TableModel model )
  {
    super.setModel(model); 
    reallocateIndexes(); 
  }

  public int compareRowsByColumn( int row1, int row2, int column )
  {
    Class type = model.getColumnClass( column );
    TableModel data = model;

    if (type.getSuperclass() == java.lang.Number.class)
    {
      long ln1 = ((Number)data.getValueAt( row1, column )).longValue();
      long ln2 = ((Number)data.getValueAt( row2, column )).longValue();

      if (ln1 < ln2)
        return -1;
      else if (ln1 > ln2)
        return 1;
      else
        return 0;
    }
    else if (type == String.class)
    {
      String s1 = (String)data.getValueAt( row1, column );
      String s2    = (String)data.getValueAt( row2, column );
      int result = s1.compareTo( s2 );

      if (result < 0)
        return -1;
      else if (result > 0)
        return 1;
      else
        return 0;
    }
    else if (type == Boolean.class)
    {
      Boolean bool1 = (Boolean)data.getValueAt( row1, column );
      boolean b1 = bool1.booleanValue();
      Boolean bool2 = (Boolean)data.getValueAt( row2, column );
      boolean b2 = bool2.booleanValue();

      if (b1 == b2)
        return 0;
      else if (b1) // false < true
        return 1;
      else
        return -1;
    }
    else
    {
      Object v1 = data.getValueAt( row1, column );
      String s1 = v1.toString();
      Object v2 = data.getValueAt( row2, column );
      String s2 = v2.toString();
      int result = s1.compareTo( s2 );

      if (result < 0)
        return -1;
      else if (result > 0)
        return 1;
      else
        return 0;
    }
  }

  public int compare( int row1, int row2, int column )
  {
    int result = compareRowsByColumn( row1, row2, column );
    if (result != 0)
      return ascending ? result : -result;
    return 0;
  }

  public void reallocateIndexes()
  {
    int rowCount = model.getRowCount();

    // Set up a new array of indexes with the right number of elements
    // for the new data model.
    indexes = new int[rowCount];

    // Initialise with the identity mapping.
    for (int row = 0; row < rowCount; row++)
      indexes[row] = row;
  }

  public void tableChanged( TableModelEvent e )
  {
    reallocateIndexes();
    super.tableChanged( e );
  }

  public void sort( int column )
  {
    if (indexes.length > 1)
    {
      int i;
      Integer ixarray[];
      sortColumn = column;
      ixarray = new Integer[indexes.length];
      for (i = 0; i < indexes.length; ++i)
        ixarray[i] = new Integer( indexes[i] );
      Arrays.sort( ixarray, (Comparator)this );
      for (i = 0; i < indexes.length; ++i)
        indexes[i] = ((Integer)(ixarray[i])).intValue();
    }
  }

  // Return true if the first operand should be positioned before the second.
  // Use global sortColumn.
  public int compare( Object ixa, Object ixb )
  {
    return (compare( ((Integer)ixa).intValue(),
                     ((Integer)ixb).intValue(), sortColumn ));
  }

  public Object getValueAt(int aRow, int aColumn)
  {
      return model.getValueAt(indexes[aRow], aColumn);
  }

  // Get index mapped through the indexes array
  public int getMappedIndex ( int i )
  {
    return indexes[i];
  }

  public void sortByColumn( int column, boolean ascending )
  {
    this.ascending = ascending;
    sort( column );
    super.tableChanged( new TableModelEvent( this ) ); 
  }

  // Add a mouse listener to the JTable to cause a table sort 
  // when a column heading is clicked in the JTable. 
  public void addMouseListenerToHeaderInTable( JTable table )
  { 
    final GuiTableSorter sorter = this; 
    final JTable tableView = table; 
    tableView.setColumnSelectionAllowed( false ); 
    MouseAdapter listMouseListener = new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        TableColumnModel columnModel = tableView.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX( e.getX() ); 
        int column = tableView.convertColumnIndexToModel( viewColumn ); 
        if (e.getClickCount() == 1 && column != -1)
        {
          // If sorting takes a long time, this is where to
          // pop up a modal message window saying that we are sorting.
          int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
          boolean ascending = (shiftPressed == 0); 
          sorter.sortByColumn( column, ascending ); 
          // and we would take away the message window here
        }
      }
    };
    JTableHeader th = tableView.getTableHeader(); 
    th.addMouseListener( listMouseListener ); 
  }
}
