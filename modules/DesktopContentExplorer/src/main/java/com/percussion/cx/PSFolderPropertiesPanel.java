/******************************************************************************
 *
 * [ PSFolderPropertiesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.cx;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.cx.guitools.UTPropertiesTablePanel;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Properties Panel on the Cx Folder Properties Dialog.
 * Allows users to change custom folder properties.
 */
@SuppressWarnings("serial")
public class PSFolderPropertiesPanel extends UTPropertiesTablePanel
{
   /**
    * The only constructor.
    * @param folder shared instance of the PSFolder, never <code>null</code>.
    * @param editable <code>true</code> if any data can be entered,
    * <code>false</code> otherwise.
    */
   public PSFolderPropertiesPanel(PSFolder folder, boolean editable, PSContentExplorerApplet applet)
   {
      if (folder==null)
         throw new IllegalArgumentException("folder may not be null");
      
      if (applet==null)
         throw new IllegalArgumentException("applet may not be null");

      m_folder = folder;
      m_editable = editable;
      m_applet = applet;

      String columnNames[] =
         new String[]{m_applet.getResourceString(
         getClass(), "Name"), m_applet.getResourceString(
         getClass(), "Value"), m_applet.getResourceString(
         getClass(), "Description")};

      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);   
      setTableCellFocusColor(dispOptions.getFocusColor());
      setTableUseFocusHighlight(true);   

      //init properties table layout
      init(columnNames, 3, m_editable);
      
      setCellEditor();

      setScrollPaneSize(new Dimension(0, 250));

      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      loadTableData();
   }
   
   /**
    * Cell editor for the custom properties table.
    */
   private void setCellEditor()
   {
      JTable table = getTable();
      
      // create the combo box editor with the known property name, 
      // sys_pubFilename
      JComboBox comboBox = new JComboBox(
         new String[]{PSFolder.PROPERTY_PUB_FILE_NAME});
      comboBox.setEditable(true);
      DefaultCellEditor editor = new DefaultCellEditor(comboBox);
      
      // assign the editor to the 1st column
      TableColumnModel tcm = table.getColumnModel();
      tcm.getColumn(0).setCellEditor(editor);
   }

   /**
    * loads folder properties into the table.
    */
   private void loadTableData()
   {
      Iterator iter = m_folder.getProperties();

      DefaultTableModel model = (DefaultTableModel)getTableModel();

      clearAllRows();

      while(iter.hasNext())
      {
         PSFolderProperty property = (PSFolderProperty)iter.next();

         // skip properties which are handled in other tab's
         if (PSFolder.isDisplayFormatProperty(property) || 
            PSFolder.isFolderPublishProperty(property) ||
            PSFolder.isFolderGlobalTemplateProperty(property))
            continue;

         Vector<String> vRow = new Vector<String>();

         vRow.add(property.getName());
         vRow.add(property.getValue());
         vRow.add(property.getDescription());

         model.addRow(vRow);

         //remember loaded properties
         m_mapLoadedProperties.put(property.getName(), property);
      }

      for (int i=0; i < 3; i++)
         addRow(); //always add several empty rows
   }

   /**
    * Folder Dialog calls this method when user pushes OK button to save changes.
    * Depending on the folder ACL and user permissions the data may or may not
    * be saved.
    * @return <code>true</code> if success, <code>false</code> otherwise.
    */
   public boolean onOk()
   {
      if (!m_editable)
         return false;

      if (!validateData())
         return false;

      DefaultTableModel model = (DefaultTableModel)getTableModel();

      Vector vRows = model.getDataVector();

      for (int i = 0; i < vRows.size(); i++)
      {
         Vector vColumns = (Vector)vRows.elementAt(i);

         String name = (String)vColumns.elementAt(0);
         String value = (String)vColumns.elementAt(1);
         String desc = (String)vColumns.elementAt(2);

         if (name==null || name.trim().length()<=0)
            continue;

         if (value==null)
            continue;

         PSFolderProperty prop = new PSFolderProperty(name, value, desc);

         // skip properties which are handled in other tab's
         if (PSFolder.isDisplayFormatProperty(prop) || 
            PSFolder.isFolderPublishProperty(prop) ||
            PSFolder.isFolderGlobalTemplateProperty(prop))
            continue;

         m_folder.setProperty(prop);

         //once set we don't need to remember about it anymore
         m_mapLoadedProperties.remove(name);
      }

      //if any properties were removed, remove them from the PSFolder
      if (m_mapLoadedProperties.size() > 0 )
      {
         Iterator entries = m_mapLoadedProperties.entrySet().iterator();

         while(entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();

            String deletedName = (String)entry.getKey();

            //delete this property
            m_folder.deleteProperty(deletedName);
         }
      }

      return true;
   }

   /**
    *  <code>true</code> indicates that user can make and save modifications
    *  to any control on this panel, <code>false</code> otherwise.
    */
   private boolean m_editable;

   /**
    * The folder to create or edit, initialized in the ctor and modified in
    * <code>onOk()</code> as per user selections. Never <code>null</code> after
    * that.
    */
   private PSFolder m_folder;

   /**
    * Remembers which properties where loaded into the table.
    * On save allows to detect and delete properties that were removed.
    */
   private Map<String, PSFolderProperty> m_mapLoadedProperties = new HashMap<String, PSFolderProperty>();
   
   /**
    * A reference back to the applet.
    */
   private PSContentExplorerApplet m_applet;
}
