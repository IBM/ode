package com.ibm.ode.bin.gui;

/** 
 * GuiTableMap can be subclassed by filters that only need to override a
 * few methods while leaving the rest to AbstractTableModel code.
 * GuiTableMap implements TableModel by passing all requests to its model, and
 * TableModelListener by passing all events to its listeners. Inserting 
 * a GuiTableMap which has not been subclassed into a chain of table filters 
 * should have no effect. The intent is that code in SandboxFiles and
 * SandboxList should subclass GuiTableMap.
**/

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

public class GuiTableMap extends AbstractTableModel
                         implements TableModelListener
{
  protected TableModel model; 

  public TableModel getModel() 
  {
    return model;
  }

  public void setModel( TableModel model ) 
  {
    this.model = model; 
    model.addTableModelListener( this ); 
  }

  public Object getValueAt( int row, int col )
  {
    return model.getValueAt( row, col ); 
  }
      
  public int getRowCount()
  {
    return (model == null) ? 0 : model.getRowCount(); 
  }

  public int getColumnCount()
  {
    return (model == null) ? 0 : model.getColumnCount(); 
  }
      
  public String getColumnName( int col )
  {
    return model.getColumnName( col ); 
  }

  public Class getColumnClass( int col )
  {
    return model.getColumnClass( col ); 
  }
      
  // Implement TableModelListener interface
  // By default forward all events to all the listeners. 
  public void tableChanged( TableModelEvent e )
  {
    fireTableChanged( e );
  }
}
