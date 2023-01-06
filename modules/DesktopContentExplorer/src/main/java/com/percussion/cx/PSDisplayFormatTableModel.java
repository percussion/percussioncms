/******************************************************************************
 *
 * [ PSDisplayFormatTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.guitools.PSTableModel;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.util.PSDataTypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

/**
 * The table model that displays a children of a node according to its display
 * format. Does not allow editing of the data.
 */
public class PSDisplayFormatTableModel extends PSTableModel
{
   /**
    * Default constructor with no columns and rows.
    */
   public PSDisplayFormatTableModel(PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");   
     m_applet = applet;

     ms_defColumnName = m_applet.getResourceString(
     PSDisplayFormatTableModel.class.getName() + "@Name");
   }

   /**
    * Sets table model with the children of the supplied node. The
    * table structure is taken from this node's display format. Always will have
    * a default first column 'Name' that gets child node object in addition to
    * the display format columns.
    *
    * @param node the node, may not be <code>null</code>
    */
   public void setRoot(PSNode node)
   {
      if(node == null)
         throw new IllegalArgumentException("node may not be null.");

      m_root = node;

      Iterator children = node.getChildren();
      if(children != null && children.hasNext())
      {
         Vector columnNames = new Vector();
         Vector columnTypes = new Vector();

         //Get the column names
         Iterator tableFormat = node.getChildrenDisplayFormat();

         int titleIndex = -1;
         if(tableFormat!=null)
         {
            // Not a category. Get the title column index
            titleIndex = getSysTitleIndex(node,m_applet.getActionManager());
         }

         // if < 0, use default:
         if(titleIndex < 0)
         {
            columnNames.add(ms_defColumnName);
            columnTypes.add(PSNode.DATA_TYPE_TEXT);
         }
         else
            m_titleCol = titleIndex;

         if(tableFormat != null)
         {
            while(tableFormat.hasNext())
            {
               Map.Entry columnDef = (Map.Entry)tableFormat.next();
               columnNames.add(columnDef.getKey());
               columnTypes.add(columnDef.getValue());
            }
         }

         m_colTypes = columnTypes;

         Vector tableData = new Vector();
         while(children.hasNext())
         {
            PSNode childNode = (PSNode)children.next();
            Vector rowVector = new Vector();

            if(tableFormat != null)
            {
               Map rowData = childNode.getRowData();
               if(rowData != null)
               {
                  //Get all dynamic column names
                  Iterator colNames = null;
                  Iterator colTypes = null;
                  if(titleIndex < 0)
                  {
                     colNames = columnNames.subList(
                        1, columnNames.size()).iterator();
                     colTypes = columnTypes.subList(
                        1, columnTypes.size()).iterator();
                  }
                  else
                  {
                     colNames = columnNames.iterator();
                     colTypes = columnTypes.iterator();
                  }
                  for(int cnt = 0; colNames.hasNext() && colTypes.hasNext(); cnt++)
                  {
                     if (titleIndex >= 0 && cnt == titleIndex)
                     {
                        colNames.next();
                        colTypes.next();
                        continue;
                     }
                     
                     Object obj = rowData.get(colNames.next());
                     if (obj != null)
                     {
                        String colType = (String)colTypes.next();
                        if (PSNode.DATA_TYPE_DATE.equals(colType))
                        {
                           if (obj.toString().trim().length() > 0)
                           {
                              // extract a date from the raw string
                              obj = PSDataTypeConverter.parseStringToDate(
                                    (String) obj);
                           }
                        }
                        else if (PSNode.DATA_TYPE_NUMBER.equals(colType)) 
                        {
                           if (((String)obj).length() > 0)
                           {
                              try
                              {
                                 obj = new Integer((String)obj);
                              }
                              catch (NumberFormatException e)
                              {
                                 /*
                                  * Let it be string. Some times the column type
                                  * is number but the display value could be
                                  * string.
                                  */ 
                              }
                           }
                        }
                     }
                     rowVector.add(obj);
                  }
               }
               else //add null values for all columns
               {
                  for (int i = 1; i < columnNames.size(); i++)
                     rowVector.add( null );
               }
            }

            rowVector.add(m_titleCol,childNode);
            tableData.add(rowVector);
         }

         setDataVector(tableData, columnNames);
      }
   }
   
   /**
    * Get the last root node set on this model by {@link #setRoot(PSNode)}
    * 
    * @return The root, or <code>null</code> if one has not been set.
    */
   public PSNode getRoot()
   {
      return m_root;
   }
   
   /**
    * Get the class of the object the column value contains.
    * 
    * @return the class, or {@link Object#getClass()} if there is no column type
    * defined for the supplied index. (base class behavior)
    */
   public Class getColumnClass(int columnIndex)
   {
      Class cls = Object.class;
      
      // title column is the PSNode itself, so let that return Object
      // image type should also return object
      if (m_colTypes != null && columnIndex < m_colTypes.size() 
         && columnIndex != m_titleCol)
      {
         String type = (String)m_colTypes.get(columnIndex);
         if (PSNode.DATA_TYPE_DATE.equals(type))
            cls = Date.class;
         else if (PSNode.DATA_TYPE_NUMBER.equals(type))
            cls = Integer.class;
         else if (PSNode.DATA_TYPE_TEXT.equals(type))
            cls = String.class;         
      }
      
      return cls;
   }

   /**
    * Set the locale of this table model, while displaying dates, use this 
    * locale to format them properly. The default language is used if 
    * <code>null</code> or empty is supplied.
    * 
    * @param str the current locale as a string, may be <code>null</code> or
    *    empty.
    */
   public void setLocale(String str)
   {
      if (str == null || str.trim().length() == 0)
         str = PSI18nUtils.DEFAULT_LANG;

      m_locale = PSI18nUtils.getLocaleFromString(str);
   }   
   
   /**
    * Get the locale used to render this table.
    * 
    * @return Returns the locale, may be <code>null</code>.
    */
   public Locale getLocale() 
   {
      return m_locale;
   }
   
   /**
    * Checks supplied <code>PSNode</code>to see if the sys_title field is
    * present in its associated <code>PSDisplayFormat</code>
    *
    * @param node the node in which the check will be made, is <code>null</code>
    * <code>-1</code> will be returned.
    *
    * @return the index of sys_title, <code>-1</code> if not found.
    */
   public static int getSysTitleIndex(PSNode node, PSActionManager actionManager)
   {
      int hasTitle = -1;

      if(node == null)
         return hasTitle;

      // get displayformat id:
      String dispId = node.getProp(IPSConstants.PROPERTY_DISPLAYFORMATID);
      if(dispId == null || dispId.trim().length() == 0)
         return hasTitle;

      try
      {
         // get format:
         PSDisplayFormat format = actionManager.getDisplayFormatCatalog().
            getDisplayFormatById(dispId);

         if(format == null)
            return hasTitle;

         Iterator it = format.getColumns();
         int index = 0;
         while(it.hasNext() && hasTitle < 0)
         {
            if(((PSDisplayColumn)it.next()).
               getSource().equalsIgnoreCase("sys_title"))
               hasTitle = index;

            index++;
         }
      }
      catch (PSContentExplorerException ex)
      {
         // do nothing
      }

      return hasTitle;
   }

   /**
    * Gets the children nodes that are represented by the model. See <code>
    * com.percussion.guitools.IPSTableModel</code> interface for more info.
    *
    * @return the data, an iterator over zero or more <code>PSNode</code>
    * objects, never <code>null</code>.
    */
   public Iterator getData()
   {
      List data = new ArrayList();

      Iterator rows = getDataVector().iterator();
      while(rows.hasNext())
      {
         Vector rowData = (Vector)rows.next();
         if(!rowData.isEmpty())
            data.add(rowData.get(0));
      }

      return data.iterator();
   }

   /**
    * Gets the node represented by this row.
    *
    * @param row the row index, must be >= 0 && < rowcount of the model.
    *
    * @return the data  node (<code>PSNode</code>), never <code>null</code>
    *
    * @throws IllegalArgumentException if row index is invalid.
    */
   public Object getData(int row)
   {
      checkRow(row);

      return getValueAt(row, m_titleCol);
   }

   //overridden to return <code>false</code> always.
   public boolean isCellEditable(int row, int col)
   {
      return false;
   }

   /**
    * The default column name to represent node as such.
    */
   public final String ms_defColumnName;

   /**
    * This is where the title column will be located.  The title column
    * is the PSNode.
    */
   private int m_titleCol = 0;

   /**
    * The current locale to use for this table model when displaying dates. Set
    * with a setter <code>setLocale</code>, may be <code>null</code> if it has
    * not been initialized.
    */
   private Locale m_locale = null;
   
   /**
    * List of columns types for each column in the model, each entry is one
    * the <code>PSNode.DATA_TYPE_xxx</code> values.  Modified by 
    * {@link #setRoot(PSNode)}, may be <code>null</code> if that method has not
    * been called.
    */
   private Vector m_colTypes;
   
   /**
    * Root node used to supply the data for this model.  Modified by 
    * {@link #setRoot(PSNode)}, may be <code>null</code> if never set.
    */
   private PSNode m_root;
   

   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
}
