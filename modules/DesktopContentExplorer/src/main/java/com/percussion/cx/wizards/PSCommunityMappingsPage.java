/* *****************************************************************************
 *
 * [ PSCommunityMappingsPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.cx.wizards;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.cx.catalogers.PSCommunityCataloger;
import com.percussion.cx.catalogers.PSCommunityContentTypeMapperCataloger;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.wizard.PSWizardPanel;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This wizard panel allows a user to map source to target communities for the
 * copy site / copy site folder wizards.
 */
public class PSCommunityMappingsPage extends PSWizardPanel
{

   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSCommunityMappingsPage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());
   }

   /**
    * Construct a new community mappings page.
    */
   public PSCommunityMappingsPage()
   {
      initPanel(createMainPanel());
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#getSummary()
    */
   public String getSummary()
   {
      StringBuffer summary = new StringBuffer(PSI18NTranslationKeyValues
         .getInstance().getTranslationValue(
            getClass().getName() + "@Community Mappings:"));
      summary.append("\n");
      
      TableModel model = m_table.getModel();
      for (int i=0; i<model.getRowCount(); i++)
      {
         Object test = model.getValueAt(i, SOURCE_COLUMN_INDEX) == null ? ""
            : model.getValueAt(i, SOURCE_COLUMN_INDEX);
         String source = test.toString().trim();
         
         test = model.getValueAt(i, TARGET_COLUMN_INDEX) == null ? ""
            : model.getValueAt(i, TARGET_COLUMN_INDEX);
         String target = test.toString().trim();
         
         if (source.length() > 0 && target.length() > 0 && 
            !source.equals(target))
            summary.append("\t" + source + " --> " + target + "\n");
      }
      
      return summary.toString();
   }
   
   /**
    * Get the panel data.
    * 
    * @return the panel data as {@link OutputData}
    *    object, never <code>null</code>.
    */
   public Object getData()
   {
      return new OutputData(m_table.getModel());
   }
   
   /**
    * Set and initialize the the panel data.
    * 
    * @param data the data as {@link InputData}
    *    object, not <code>null</code>.
    */
   public void setData(Object data)
   {
      initData(data);
      super.setData(data);
   }
   
   /**
    * Create the main wizard panel.
    * 
    * @return the new main panel, never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      
      m_table = new MappingTable();
      JScrollPane scroll = new JScrollPane(m_table);
      panel.add(scroll);
      
      return panel;
   }
   
   /**
    * Initialize the panel data.
    * 
    * @param data the data to initialize the wizard with, must be an instance
    *    of {@link InputData}.
    */
   private void initData(Object data)
   {
      if (!(data instanceof InputData))
         throw new IllegalArgumentException(
            "the supplied data must be an array of Collection objects");
      
      Collection allCommunities = ((InputData) data).getCommunityDefinitions();
      List sourceCommunities = ((InputData) data).getSourceCommunities();
      PSCommunityContentTypeMapperCataloger mapper = ((InputData) data).getCommCtMapper();

      // initialize the table
      Object[] columnNames =
      {
         PSI18NTranslationKeyValues.getInstance().getTranslationValue(
            getClass().getName() + "@Source Community"),
         PSI18NTranslationKeyValues.getInstance().getTranslationValue(
            getClass().getName() + "@Target Community")
      };
      
      TableModel model = new DefaultTableModel(columnNames, 
         sourceCommunities.size());
      m_table.setModel(model);

      m_table.getTableHeader().setReorderingAllowed(false);
      
      m_table.setCellEditors(sourceCommunities);
      
      // initialize source and target community columns
      for (int row = 0; row < sourceCommunities.size(); row++) 
      {
         Integer srcCommunity = (Integer) sourceCommunities.get(row);
         m_table.initTableCells(row, srcCommunity, allCommunities, mapper,
               model);
      }
   }
  
   /**
    * The data object to initialize this wizard page with.
    */
   public static class InputData
   {
      /**
       * Construct the input data for this wizard page.
       * 
       * @param commCtMapper The community and content type mapper, never
       *    <code>null</code>.
       * @param communityDefinitions a collection of community definitions as
       *    <code>PSCommunityCataloger.Community</code> obejcts, not
       *    <code>null</code>, may be empty.
       * @param sourceCommunities a collection of source node community ids
       *    as <code>Integer</code> objects, not <code>null</code>, may be 
       *    empty.
       */
      public InputData(PSCommunityContentTypeMapperCataloger commCtMapper, 
            Collection communityDefinitions, 
         Collection sourceCommunities)
      {
         if (commCtMapper == null)
            throw new IllegalArgumentException("commCtMapper may not be null");
            
         if (!(communityDefinitions instanceof Collection))
            throw new IllegalArgumentException(
               "communityDefinitions must be an instance of Collection");
         
         if (!(sourceCommunities instanceof Collection))
            throw new IllegalArgumentException(
               "sourceCommunities must be an instance of Collection");
         
         m_commCtMapper = commCtMapper;
         m_communityDefinitions = communityDefinitions;
         m_sourceCommunities = new ArrayList(sourceCommunities);
         Collections.sort(m_sourceCommunities);
         Collections.reverse(m_sourceCommunities); // move smallest to the end
      }
      
      /**
       * Get all community definitions.
       * 
       * @return a collection of community definitions as
       *    <code>PSCommunityCataloger.Community</code> obejcts, never
       *    <code>null</code>, may be empty.
       */
      public Collection getCommunityDefinitions()
      {
         return m_communityDefinitions;
      }
      
      /**
       * get all source node community ids.
       * 
       * @return a collection of source node community ids
       *    as <code>Integer</code> objects, never <code>null</code>, may be 
       *    empty.
       */
      public List getSourceCommunities()
      {
         return m_sourceCommunities;
      }
      
      /**
       * @return the community and content type mapper.
       */
      public PSCommunityContentTypeMapperCataloger getCommCtMapper()
      {
         return m_commCtMapper;
      }
      
      /**
       * A collection with all community definitions in the current system
       * as <code>PSCommunityCataloger.Community</code> objects, initialized
       * during construction, never <code>null</code> or changed after that.
       */
      private Collection m_communityDefinitions = null;
      
      /**
       * A collection with all community ids found in the source object
       * (typically a site or site folder) as <code>integer</code> objects,
       * initialized during constuction, never <code>null</code> or changed 
       * after that.
       */
      private List m_sourceCommunities = null;
      
      /**
       * The community and content type mapper. It is initialized by ctor,
       * never <code>null</code> after that.
       */
      private PSCommunityContentTypeMapperCataloger m_commCtMapper;
      

   }
   
   /**
    * The data object returned by this wizard page.
    */
   public class OutputData extends HashMap
   {
      /**
       * Constructs a new source - target community id map as specified by the
       * wizard user. The map key is the source community id and the map value 
       * the target community id both as <code>Integer</code>. Only mappings 
       * where the target community id differes from the source community id 
       * are returned.
       * 
       * @param model the table model from which to construct the data object,
       *    assumed not <code>null</code>.
       */
      private OutputData(TableModel model)
      {
         for (int i=0; i<model.getRowCount(); i++)
         {
            PSCommunityCataloger.Community source = 
               (PSCommunityCataloger.Community) model.getValueAt(i, 
                  SOURCE_COLUMN_INDEX);
            PSCommunityCataloger.Community target = 
               (PSCommunityCataloger.Community) model.getValueAt(i, 
                  TARGET_COLUMN_INDEX);
            
            if (source == null || target == null)
               break;
            
            if (source.getId() != target.getId())
               put(new Integer(source.getId()), new Integer(target.getId()));
         }
      }
   }
   
   /**
    * The community mapping table, it maps the source community (on the first
    * column) to its corresponding target communities (on the second column). 
    * The each target community contains the same set or more content types 
    * that are defined in the source community.
    * 
    * It creates different cell editor for each target cell and overrides the 
    * {@link #getCellEditor(int, int)} to set the cell editor based on the
    * location of the cell.  
    */
   private class MappingTable extends JTable
   {
      /**
       * The default ctor.
       */
      public MappingTable()
      {
         super();
      }

      /**
       * Set the cell editors from the supplied parameters.
       * 
       * @param sourceCommunities a list of source community ids, each id 
       *    is an <code>Integer</code> object. Assume not <code>null</code>
       */
      private void setCellEditors(Collection sourceCommunities)
      {
         // set the cell editor for source column
         m_sourceEditor = new DefaultCellEditor(new JTextField())
         {
            public boolean isCellEditable(EventObject event)
            {
               return false;
            }
         };

         // init the cell editors for the target column. 
         // note: each target cell editor will be set later (in initTableCells)
         // according to its source community.
         m_targetEditors = new DefaultCellEditor[sourceCommunities.size()];
      }

      /**
       * Set the target cell editor for the specified row; initialize both 
       * source and target cells to be the supplied source community.
       * 
       * @param row the current row of the table.
       * @param srcCommunity the source community id. Assumed not 
       *    <code>null</code>.
       * @param allCommunities a complete list of communities. This is a list
       *    of {@link PSCommunityCataloger.Community} objects. Assumed not 
       *    <code>null</code>
       * @param mapper the community and content type mapper, assumed not
       *    <code>null</code>.
       * @param model the table model, assumed not <code>null</code>. 
       */
      private void initTableCells(int row, Integer srcCommunity,
            Collection allCommunities,
            PSCommunityContentTypeMapperCataloger mapper, TableModel model)
      {
         Collection communities = mapper.getCompatibleCommunities(srcCommunity);

         // set the cell editor for the target column
         if (communities == null)
         {
            // take all communities if source community not exist in the mapper 
            m_targetEditors[row] = createCellEditor(allCommunities);
            return;
         }
         else
         {
            m_targetEditors[row] = createCellEditor(communities);
         }
         
         // find the source community & init the cell values for source & target
         Iterator walker = communities.iterator();
         while (walker.hasNext())
         {
            PSCommunityCataloger.Community community = 
               (PSCommunityCataloger.Community) walker.next();
            
            if (community.getId() == srcCommunity.intValue())
            {
               model.setValueAt(community, row, SOURCE_COLUMN_INDEX);
               model.setValueAt(community, row, TARGET_COLUMN_INDEX);
               
               DefaultCellEditor defaultEditor = (DefaultCellEditor) m_table
                  .getCellEditor(row, TARGET_COLUMN_INDEX);
               JComboBox editor = (JComboBox) defaultEditor.getComponent();
               editor.setSelectedItem(community);
               
               break;
            }
         }
      }
      
      /**
       * Creates a cell editor from the supplied communities.
       * 
       * @param communities the selectable communities for the cell editor. 
       *    Each community is a {@link PSCommunityCataloger.Community}
       *    object. Assume not <code>null</code>, but may be empty.
       * 
       * @return the created cell editor, never <code>null</code>.
       */
      private DefaultCellEditor createCellEditor(Collection communities)
      {
         JComboBox cbox = new JComboBox();
         Iterator walker = communities.iterator();
         while (walker.hasNext())
         {
            PSCommunityCataloger.Community community = 
               (PSCommunityCataloger.Community) walker.next();
            cbox.addItem(community);
         }
         
         return new DefaultCellEditor(cbox);
      }

      // override the default implementation
      public TableCellEditor getCellEditor(int row, int col)
      {
         if (col == SOURCE_COLUMN_INDEX)
            return m_sourceEditor;
         else
            return m_targetEditors[row];
      }

      /**
       * The cell editors for the target column, init by setCellEditors.
       */
      private DefaultCellEditor m_targetEditors[];
      
      /**
       * The cell editors for the source column, init by setCellEditors.
       */
      private DefaultCellEditor m_sourceEditor;
   }
   
   /**
    * The table for the source - target community mappings. Initialized in
    * {@link #createMainPanel()}, never <code>null</code> after that.
    */
   private MappingTable m_table = null;
   
   /**
    * The column index for the source community.
    */
   private final static int SOURCE_COLUMN_INDEX = 0;
   
   /**
    * The column index for the target ccommunity.
    */
   private final static int TARGET_COLUMN_INDEX = 1;
}
