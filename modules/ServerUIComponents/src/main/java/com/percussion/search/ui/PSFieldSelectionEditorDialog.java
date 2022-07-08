/******************************************************************************
 *
 * [ PSFieldSelectionEditorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.search.ui;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.IPSSequencedComponent;
import com.percussion.cms.objectstore.PSDbComponentList;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.IPSValueChangedListener;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.PSTableSorter;
import com.percussion.guitools.PSValueChangedEvent;
import com.percussion.guitools.UTStandardCommandPanel;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.search.PSCommonSearchUtils;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * This dialog is used to let the user select fields used in Display Formats and
 * Searches. Fields already selected in the editor from where this is launched
 * will not be show in the Available List group.
 */
public class PSFieldSelectionEditorDialog extends PSDialog
{

   private static final int ADDITIONAL_BUTTON_BORDER_HEIGHT = 8;
   private static final int ADDITIONAL_BUTTON_BORDER_WIDTH = 10;
   
   
   /**
    * Convienence ctor that calls 
    * <code>PSFieldSelectionEditorDialog(
    * Frame, IPSDbComponent, PSContentEditorFieldCataloger, false)</code>.
    */
   public PSFieldSelectionEditorDialog(Frame frame,
      IPSDbComponent dbComponent, PSContentEditorFieldCataloger ceCatlg)
   {
      this(frame, dbComponent, ceCatlg, false);
   }
   
   /**
    * Constructs the dialog.
    *
    * @param frame - the parent frame.
    * @param dbComponent
    * @param ceCatlg
    * @param inWorkbench flag indicating this dialog was launched from
    * the Eclipse based workbench
    */
   public PSFieldSelectionEditorDialog(Frame frame,
      IPSDbComponent dbComponent, PSContentEditorFieldCataloger ceCatlg,
      boolean inWorkbench)
   {
      super(frame);
      m_inWorkbench = inWorkbench;
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         SwingUtilities.updateComponentTreeUI(this);
      }
      catch (Exception e)
      {         
         e.printStackTrace();
      }
      
      //get all the data to be displayed and make system row in combo box
      //as selected by default.
      if (dbComponent instanceof PSSearch)
         m_search = (PSSearch)dbComponent;
      if (dbComponent instanceof PSDisplayFormat)
         m_dformat = (PSDisplayFormat)dbComponent;
      m_ceCatlg = ceCatlg;
      init();
      
   }
   
   /**
    * Convienence ctor that calls 
    * <code>PSFieldSelectionEditorDialog(
    * Dialog, IPSDbComponent, PSContentEditorFieldCataloger, false)</code>
    */
   public PSFieldSelectionEditorDialog(Dialog dialog,
      IPSDbComponent dbComponent, PSContentEditorFieldCataloger ceCatlg)
   {
      this(dialog, dbComponent, ceCatlg, false);
   }
   
   /**
    * Constructs the dialog.
    *
    * @param dialog - the parent dialog.
    * @param dbComponent
    * @param ceCatlg
    * @param inWorkbench flag indicating this dialog was launched from
    * the Eclipse based workbench
    */
   public PSFieldSelectionEditorDialog(Dialog dialog,
      IPSDbComponent dbComponent, PSContentEditorFieldCataloger ceCatlg, 
      boolean inWorkbench)
   {
      super(dialog);
      m_inWorkbench = inWorkbench;
      //get all the data to be displayed and make system row in combo box
      //as selected by default.
      if (dbComponent instanceof PSSearch)
         m_search = (PSSearch)dbComponent;
      if (dbComponent instanceof PSDisplayFormat)
         m_dformat = (PSDisplayFormat)dbComponent;
      m_ceCatlg = ceCatlg;
      
      init();
   }
   
   /**
    * Set flag to indicate if external search engine is to be used.  Affects the
    * operators used when columns are added. 
    * 
    * @param useExternal <code>true</code> if an external engine is to be used,
    * <code>false</code> if not.
    */
   public void setUseExternalSearchEngine(boolean useExternal)
   {
      m_useExternalSearch = useExternal;
   }

   /**
    * Override base class to limit dialog size to 100 pixel less than the screen
    * width and height.
    */
   @Override
   public Dimension getPreferredSize()
   {
      Dimension d = super.getPreferredSize();
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); 
   
      int w = screen.width - 100;
      int h = screen.height - 100;
      
      d = new Dimension(d.width > w ? w : d.width, d.height > h ? h : d.height);
      
      return d;
   }

   /**
    * Retrieves the translated string from the i18n object.
    *
    * @param key Assumed not <code>null</code> or empty.
    *
    * @param addColon If <code>true</code>, a ':' is appended to the value
    *    before it is returned (if there isn't one there already).
    *
    * @return The text associated with the supplied key, or if the key is not
    *    found, the key is returned. Never <code>null</code> or empty.
    */
   private String getResource(String key, boolean addColon)
   {
      String value = PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@" + key);
      if (value == null || value.trim().length() == 0)
         value = key;
      if (addColon && !value.endsWith(":"))
         value += ":";

      return value;
   }


   /**
    * Retrieves the translated mnemonic from the i18n <TUV>.
    * @param key cannot not be <code>null</code> or empty.
    *@return The mnemonic key's int value, else 0.
    */
   private char getResourceMnemonic(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException(
         "Key for mnemonic may not be null or empty");
      
      int value = PSI18NTranslationKeyValues.getInstance().
            getMnemonic(getClass().getName() + "@" + key);    
      return (char)value;
   }
   
   /**
    * Retrieves the translated tooltip from the i18n <TUV>.
    * @param key cannot not be <code>null</code> or empty.
    *@return the tool tip
    */
   private String getResourceTooltip(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException(
         "Key for tooltip may not be null or empty");
      
      String value = PSI18NTranslationKeyValues.getInstance().
            getTooltip(getClass().getName() + "@" + key);    
      return value;
   }
   
   
   /**
    * Convenience method that calls {@link #getResource(String,boolean)
    * getResource(key, false)}.
    */
   private String getResource(String key)
   {
      return getResource(key, false);
   }

   /**
    * Initializes the dialog.
    */
   private void init()
   {
      
      // load all fields w/out choices      
      try
      {
         m_ceCatlg.loadFields(null, m_ceCatlg.getControlFlags() | 
            IPSFieldCataloger.FLAG_EXCLUDE_CHOICES, false);
      }
      catch (PSCmsException e)
      {
         // unlikely since we've already created the catalog
         String msg = "Error: " + e.getLocalizedMessage();
         ErrorDialogs.showErrorDialog(this, msg, "Error", 
            JOptionPane.ERROR_MESSAGE);         
         e.printStackTrace();
      }
      
      setTitle(getResource("Field Selection Editor"));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());

      //category and field filter
      JPanel topPane = new JPanel();
      topPane.setLayout(new FlowLayout(FlowLayout.LEFT));
      PSPropertyPanel propPane = new PSPropertyPanel();
      PSPropertyPanel applyPane = new PSPropertyPanel();
      propPane.setBorder(emptyBorder);

      m_availSelectorComboBox = new JComboBox(prepareComboData());
      m_availSelectorComboBox.addActionListener(new AvailableComboBoxListener());
      JComponent[] comp = new JComponent[]{m_availSelectorComboBox};
      char mnemonic = getResourceMnemonic("Category");
      propPane.addPropertyRow(getResource("Category", true), comp, 
              m_availSelectorComboBox, mnemonic, null);

      // filter text field:
      m_filterText = new JTextField();

      String label = getResource("Field Filter", true);
      mnemonic = getResourceMnemonic("Field Filter");
      propPane.addPropertyRow(label, m_filterText, mnemonic);

      // get the check mark icon if it not initialized yet
      if(null == ms_checkMarkIcon)
      {
         ms_checkMarkIcon =
            new ImageIcon(getClass().getResource(
               getResourceString("icon.check.mark")));
      }

      //load up icon for table header sort.
      if(null == ms_upIcon)
      {
         ms_upIcon = new ImageIcon(getClass().getResource(
               getResourceString("icon.sort.up")));
      }

      //load down icon for table header sort.
      if(null == ms_downIcon)
      {
         ms_downIcon = new ImageIcon(getClass().getResource(
               getResourceString("icon.sort.down")));
      }

      // apply filter button:
      m_applyFilterBtn = new JButton(ms_checkMarkIcon);
      m_applyFilterBtn.setToolTipText(getResourceString("tooltip.apply.filter"));
      Dimension buttonSize =
         new Dimension(ms_checkMarkIcon.getIconWidth() + 
             ADDITIONAL_BUTTON_BORDER_WIDTH,
             ms_checkMarkIcon.getIconHeight() + 
             ADDITIONAL_BUTTON_BORDER_HEIGHT);
      m_applyFilterBtn.setPreferredSize(buttonSize);
      m_applyFilterBtn.setMinimumSize(buttonSize);
      m_applyFilterBtn.setMaximumSize(buttonSize);

      m_applyFilterBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
           onApplyAvailableFilter();
         }
      });

      applyPane.add(Box.createVerticalStrut(
         PSPropertyPanel.FIXED_FIELD_HEIGHT));
      applyPane.add(Box.createVerticalStrut(
         PSPropertyPanel.FIXED_FIELD_HEIGHT));
      applyPane.add(m_applyFilterBtn);

      topPane.add(propPane);
      topPane.add(applyPane);

      //set available and selected fields table
      JPanel centerPane = new JPanel();
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
      centerPane.setPreferredSize(new Dimension(600, 550));
      centerPane.setBorder(emptyBorder);

      //available fields
      JPanel availablePane = new JPanel();
      availablePane.setLayout(new BorderLayout());
      Border b = createGroupBorder(getResource("Available Fields"));

      availablePane.setBorder(b);

      DefaultTableModel model = new DefaultTableModel(
         new Object[]
         {
            getResource("Display Label"),
            getResource("Field"),
            getResource("Data Type"),
         }, 0);

      m_availableTableSorter = new PSTableSorter(model, true);

      m_availableTable = new JTable(m_availableTableSorter)
      {
         @Override
         @SuppressWarnings("unused") 
         public boolean isCellEditable(int row, int col)
         {
            return false;
         }
      };

      m_availableTable.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      m_availableTable.setColumnSelectionAllowed(false);
      m_availableTable.setRowSelectionAllowed(true);

      JTableHeader availHeader = m_availableTable.getTableHeader();
      availHeader.setReorderingAllowed(false);
      availHeader.setDefaultRenderer(
         new SortableTableHeaderRenderer(m_availableTableSorter));

      m_availableTableSorter.addMouseListenerToHeaderInTable(m_availableTable);
      m_availableTableSorter.sortByColumn(0, true);

      JScrollPane jsp = new JScrollPane(m_availableTable);
      jsp.getViewport().setBackground(m_availableTable.getBackground());
      ListSelectionModel rowSM = m_availableTable.getSelectionModel();
      rowSM.addListSelectionListener(new RowSelectionListener());

      availablePane.add(jsp, BorderLayout.CENTER);
      centerPane.add(availablePane);

      //inter table row movement button
      JPanel btnPane = new JPanel();
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));

      String str = getResource("Select");
      char mnem = getResourceMnemonic("Select");
      
      m_selectBtn = new JButton(str,
         new ImageIcon(getClass().getResource(getResourceString("icon.down"))));      
      m_selectBtn.setMnemonic(mnem);
      
      m_selectBtn.addActionListener(new MoveAvailableToSelectedRowListener());
      btnPane.add(Box.createHorizontalStrut(25));
      btnPane.add(m_selectBtn);
      centerPane.add(Box.createVerticalStrut(10));
      centerPane.add(btnPane);
      centerPane.add(Box.createVerticalStrut(5));
      //selected table panel
      JPanel selectedPane = new JPanel();
      selectedPane.setLayout(new BoxLayout(selectedPane, BoxLayout.X_AXIS));
      b = super.createGroupBorder(getResource("Selected Fields"));
      selectedPane.setBorder(b);
      JToolBar tb = new JToolBar();
      
      ImageIcon upIcon = new ImageIcon(getClass().
         getResource(getResourceString("icon.up")));
      m_upBtn = new JButton(upIcon);
      m_upBtn.setMinimumSize(new Dimension(
         upIcon.getIconWidth() + ADDITIONAL_BUTTON_BORDER_WIDTH,
         upIcon.getIconHeight() + ADDITIONAL_BUTTON_BORDER_HEIGHT
      ));
      m_upBtn.setToolTipText(getResourceTooltip("Up"));
      m_upBtn.setMnemonic(getResourceMnemonic("Up"));
      
      tb.add(m_upBtn);
      m_upBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            moveSelectedUpDown(true);
            setButtonState();
         }
      });

      m_downBtn = new JButton(
         new ImageIcon(getClass().getResource(getResourceString("icon.down"))));
      m_downBtn.setMinimumSize(m_upBtn.getPreferredSize());
      m_downBtn.setToolTipText(getResourceTooltip("Down"));
      m_downBtn.setMnemonic(getResourceMnemonic("Down"));

      m_downBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            moveSelectedUpDown(false);
            setButtonState();
         }
      });
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_downBtn);
      tb.setOrientation(JToolBar.VERTICAL);

      model = new DefaultTableModel(new Object[]
         {
            getResource("Display Label"),
            getResource("Field"),
            getResource("Data Type"),
         }, 0);

      m_selectedTable = new JTable(model)
      {
         @Override
         @SuppressWarnings("unused")
         public boolean isCellEditable(int row, int col)
         {
            return false;
         }
      };

      // remove button:
      m_removeBtn = new JButton(
         new ImageIcon(getClass().getResource(getResourceString("icon.delete"))));

      m_removeBtn.setToolTipText(getResourceTooltip("Delete"));
      m_removeBtn.setMnemonic(getResourceMnemonic("Delete"));
      m_removeBtn.setMinimumSize(m_upBtn.getPreferredSize());
      
      m_removeBtn.addActionListener(new RemoveSelectedRowListener(m_selectedTable));

      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_removeBtn);

      m_selectedTable.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      m_selectedTable.setColumnSelectionAllowed(false);
      m_selectedTable.setRowSelectionAllowed(true);

      rowSM = m_selectedTable.getSelectionModel();
      rowSM.addListSelectionListener(new RowSelectionListener());

      //first load selected table
      initSelectedFieldsTable();

      //then load available table
      m_availSelectorComboBox.setSelectedIndex(0);

      jsp = new JScrollPane(m_selectedTable);
      jsp.getViewport().setBackground(m_selectedTable.getBackground());
      selectedPane.add(jsp);
      selectedPane.add(Box.createHorizontalStrut(15));
      selectedPane.add(tb);
      centerPane.add(selectedPane);

      //set command panel
      JPanel bottomPane = new JPanel();
      bottomPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      UTStandardCommandPanel cmdPane = new UTStandardCommandPanel(this,
         SwingConstants.HORIZONTAL, !m_inWorkbench);
      Box box = new Box(BoxLayout.X_AXIS);
      box.add(cmdPane);
      bottomPane.add(box);
      mainPane.add(topPane, BorderLayout.NORTH);
      mainPane.add(centerPane, BorderLayout.CENTER);
      mainPane.add(bottomPane, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      setButtonState();
      pack();
      center();
      setResizable(true);

   }

   /**
    * Initializes the selected fields table from <code>PSSearch</code> and
    * <code>PSDisplayFormat</code> objects.
    */
   private void initSelectedFieldsTable()
   {
      Iterator itr = null;
      DefaultTableModel mdl = (DefaultTableModel)m_selectedTable.getModel();

      if (m_search != null)
      {
         itr = m_search.getFields();

         while(itr.hasNext())
         {
            PSSearchField srchFld = (PSSearchField)itr.next();

            //convert PSSearchField into PSLightWeightField
            PSLightWeightField lwFld = new PSLightWeightField(
               srchFld.getFieldName(), srchFld.getFieldType(),
               srchFld.getDisplayName(), srchFld.getMnemonic());
            lwFld.setDisplayChoices(srchFld.getDisplayChoices());

            //add originally saved field to the selected table, preserve order.
            addLightWeightField2TableModel(mdl, lwFld, true, true);
         }
      }
      else if (m_dformat != null)
      {
         itr = m_dformat.getColumns();

         while(itr.hasNext())
         {
            PSDisplayColumn col = (PSDisplayColumn)itr.next();

            //convert PSDisplayColumn into PSLightWeightField
            PSLightWeightField lwFld = new PSLightWeightField(
               col.getSource(), col.getRenderType(),
               col.getDisplayName(), "");

            addLightWeightField2TableModel(mdl, lwFld, false, true);
         }
      }
   }

   /**
    * Recursively adds light weight field and its dependent fields to a given
    * table model. If addDependentFields is set to true then it builds dependency
    * and adds all discovered dependent fields in the "depth-first" fasion.
    * If ignoreOptionalDependencies is set to false then for each optional
    * dependency it also pops up an option dialog that allows the user to tell
    * if he wants to include an optional dependency of not.
    * @param dtm table model to which to add the light weight field,
    * never <code>null</code>.
    * @param lwf LightWeightField to add, never <code>null</code>.
    * @param addDependentFields <code>true</code> makes it to recurse the
    * dependency tree, otherwise simply adds a given field and returns.
    * @param ignoreOptionalDependencies <code>true</code> makes it to skip
    * optional dependecies with no questions asked, <code>false</code> results
    * in the JOptionPane option dialog popup for each optional dependency of
    * a given field, which allows user to decide whether to include it or not.
    *
    * @return JOptionPane result, which could be either YES_OPTION or
    * CANCEL_OPTION. Unless there was an optional dependency the result returned
    * will always be YES_OPTION. If there was an optional dependency and user
    * decided to cancel or close the option dialog, then the result will NOT be
    * YES_OPTION, which signals that any field addition/removal must be aborted.
    */
   private int addLightWeightField2TableModel(DefaultTableModel dtm,
      PSLightWeightField lwf, boolean addDependentFields,
      boolean ignoreOptionalDependencies)
   {
      if (dtm==null)
         throw new IllegalArgumentException("dtm may not be null");
      if (lwf==null)
         throw new IllegalArgumentException("lwf may not be null");

      if (addDependentFields)
      {
         // first build a dependency tree.
         buildForwardDependencyTree(lwf);

         //walk the dependency tree using 'depth-first' logic and on the way
         //back from the recusive call add all not yet added fields to the model
         //this makes dependent fields to show up first.
         PSDisplayChoices dispChoices = lwf.getDisplayChoices();

         if (dispChoices!=null)
         {
            PSChoiceFilter choiceFilter = dispChoices.getChoiceFilter();

            if (choiceFilter!=null)
            {
               Iterator itDepFields =
                  choiceFilter.getDependentFields().iterator();

               while(itDepFields.hasNext())
               {
                  PSChoiceFilter.DependentField depField =
                     (PSChoiceFilter.DependentField)itDepFields.next();

                  //see if dependency has already been set

                  String fRef = depField.getFieldRef();
                  String depType = depField.getDependencyType();

                  PSLightWeightField depLwf = depField.getField();

                  if (depType.equalsIgnoreCase(
                       PSChoiceFilter.DependentField.TYPE_OPTIONAL))
                  {
                     if (ignoreOptionalDependencies)
                        continue; //ignore as told

                     //see if the field has already been added to the selected table
                     if (findLightWeightFieldInTableModel(dtm, fRef)!=null)
                        continue; //found it, do not ask and skip it

                     //ask if user would like to add it
                     String msg = getResource("opt.msg.includeOptionalDependency");
                     Object args[] = {lwf.getInternalName(), fRef};
                     msg = MessageFormat.format(msg, args);

                     JTextArea textBox = new JTextArea(msg, 8, 20);
                     textBox.setWrapStyleWord( true );
                     textBox.setLineWrap( true );
                     textBox.setEditable( false );
                     JScrollPane pane = new JScrollPane( textBox );
                     pane.setPreferredSize(new Dimension( 400, 125));

                     //ask if user would like to add an optional dependency ?
                     int result = JOptionPane.showConfirmDialog(null,
                           pane,
                           getResource("opt.title.includeOptionalDependency"),
                           JOptionPane.YES_NO_CANCEL_OPTION,
                           JOptionPane.QUESTION_MESSAGE);

                     switch (result)
                     {
                        case JOptionPane.YES_OPTION:
                           //we got OK, include it
                           break;

                        case JOptionPane.NO_OPTION:
                           continue; //skip this one optional dependency

                        default:
                           //this could be canceled or closed window, which implies abort
                           return JOptionPane.CANCEL_OPTION;
                     }
                  }

                  //keep descending until we reach the last dependency
                  //in this dependency tree branch.
                  int result = addLightWeightField2TableModel(dtm, depLwf,
                     addDependentFields, ignoreOptionalDependencies);

                  if (result != JOptionPane.YES_OPTION)
                     return JOptionPane.CANCEL_OPTION;
               }
            }
         }
      }

      if (findLightWeightFieldInTableModel(dtm, lwf.getInternalName())==null)
      {
         //make sure we don't add duplicates
         Vector<Object> row = new Vector<Object>();
         row.add(lwf.getDisplayName());
         //put LightWeightField in the second col, so that we can get choices.
         row.add(lwf);
         row.add(lwf.getDataType());
         dtm.addRow(row);
      }

      return JOptionPane.YES_OPTION;
   }

   /**
    * Updates the <code>PSSearch</code> and <code>PSDisplayFormat</code>
    * objects.
    */
   @Override
   public void onOk()
   {
      PSDbComponentList items = null;
      if (m_search != null)
      {
         items = m_search.getFieldContainer();
         updateSearch(items);
         persistModelSequence(items);
      }
      else
      {
         items = m_dformat.getColumnContainer();
         updateDispColumns(items);
         persistModelSequence(items);

      }
      if(m_changeOccured)
         fireValueChangedEvent();
      super.onOk();
   }

   /**
    * Applies the regular expressions filter
    *
    */
   private void onApplyAvailableFilter()
   {
      String type = (String)m_availSelectorComboBox.getSelectedItem();

      DefaultTableModel modelAvail =
            (DefaultTableModel)m_availableTableSorter.getModel();

      //remove all rows from the available table
      modelAvail.setRowCount(0);
      m_availableTableSorter.setModel(modelAvail);

      String filter =
         m_filterText.getText().length() > 0 ? m_filterText.getText().trim() : null;

      if (type.equals(I18N_SYSTEM))
      {
         loadAvailableTableData(m_ceCatlg.getSystemMap(), false, filter);
      }
      else if (type.equals(I18N_SHARED))
      {
         loadAvailableTableData(m_ceCatlg.getSharedMap(), false, filter);
      }
      else if (type.equals(I18N_ALL))
      {
         loadAvailableTableData(m_ceCatlg.getAll(), true, filter);
      }
      else
      {
         Collection fields =
            (Collection)m_ceCatlg.getLocalContentTypeMap().get(type);

         loadAvailableTableData(fields, filter);
      }
   }

   /**
    * Updates the <code>PSSearch</code> object.
    *
    * @param dbColl collection of {@link
    * com.percussion.cms.objectstore.PSSearchField}. Assumed to be not <code>
    * null</code>.
    */
   private void updateDispColumns(PSDbComponentList dbColl)
   {
      DefaultTableModel mdl = (DefaultTableModel)m_selectedTable.getModel();
      int sz = mdl.getRowCount();
      Iterator itr = dbColl.iterator();
      PSDisplayColumn col = null;
      String srcName = null;
      String mdlSrcName = null;

      //delete entries which have been removed
      if (sz != 0)
      {
         while (itr.hasNext())
         {
            col = (PSDisplayColumn)itr.next();
            srcName = col.getSource();
            for (int k = 0; k < sz; k++)
            {

               PSLightWeightField lwFld =
                  (PSLightWeightField) mdl.getValueAt(k, FIELD_COLUMN);

               mdlSrcName = lwFld.getInternalName();

               if (mdlSrcName.equalsIgnoreCase(srcName))
                  break;
               if (k == sz - 1)
               {
                  dbColl.remove(col);
                  itr = dbColl.iterator();
               }
            }
         }
      }
      else
      {
         dbColl.clear();
      }

      //add new entries
      for (int k = 0; k < sz; k++)
      {
         PSLightWeightField lwFld =
            (PSLightWeightField) mdl.getValueAt(k, FIELD_COLUMN);

         //create new column out of a light weight field
         PSDisplayColumn newCol = new PSDisplayColumn(
            lwFld.getInternalName(),
            lwFld.getDisplayName(),
            PSDisplayColumn.GROUPING_FLAT,
            lwFld.getDataType(), "", true);

         mdlSrcName = lwFld.getInternalName();

         itr = dbColl.iterator();

         if (itr.hasNext())
         {
            while (itr.hasNext())
            {
               col = (PSDisplayColumn)itr.next();
               srcName = col.getSource();
               if (mdlSrcName.equalsIgnoreCase(srcName))
                  break;

               if (!itr.hasNext())
               {
                  dbColl.add(newCol);
                  break;
               }
            }
         }
         else
         {
            dbColl.add(newCol);
         }
      }      
   }

   /**
    * Updates the position of each dbCol based on it's location in the
    * model.
    *
    * @param dispCols collection of {@link IPSSequencedComponent
    * IPSSequencedComponent}. Assumed to be not <code>null</code>.
    */
   private void persistModelSequence(PSDbComponentList dispCols)
   {
      DefaultTableModel model = (DefaultTableModel)m_selectedTable.getModel();

     // get all columns:
      Iterator itr = dispCols.iterator();
      Map<String, IPSSequencedComponent> colMap =
            new HashMap<String, IPSSequencedComponent>();
      IPSSequencedComponent dsCol = null;
      String source = null;

      // iterate through the columns
      // create a map useing the column: getSource() as the key
      // the col as the value.
      while(itr.hasNext())
      {
         // for each column,
         dsCol = (IPSSequencedComponent)itr.next();
         colMap.put(getFieldColumnValue(dsCol), dsCol);
      }

      for(int k = 0; k < model.getRowCount(); k++)
      {
         PSLightWeightField lwFld =
                  (PSLightWeightField) model.getValueAt(k, FIELD_COLUMN);

         source = lwFld.getInternalName();

         dsCol = colMap.get(source);
         dispCols.move(dsCol.getPosition(), k);
      }
   }

   /**
    * This is a convience method that returns the appropriate string to be
    * used as the <code>FIELD_COLUMN</code>.  There were 2 different types
    * of <code>IPSSequencedComponent</code> objects used when originally
    * creating this class and these 2 objects store the value used in the
    * <code>FIELD_COLUMN</code> in different places. This method does the
    * work of getting the value from those objects.
    *
    * @todo there's really no need for this type of method, there should be a
    * way (through polymorphism, data object or adapter) so that the
    * appropriate data can be retrieved from the objects in a more efficient
    * manner. This class has many redundancies that can and should be
    * eliminated.
    *
    * @param dbComp assumed not <code>null</code>
    * @return never <code>null</code> or empty.
    */
   private String getFieldColumnValue(IPSSequencedComponent dbComp)
   {
      String fieldName = "";
      if(dbComp instanceof PSDisplayColumn)
         fieldName = ((PSDisplayColumn)dbComp).getSource();
      else
         fieldName = ((PSSearchField)dbComp).getFieldName();

      return fieldName;
   }

   /**
    * Updates the <code>PSSearch</code> object.
    *
    * @param fields collection of {@link
    * com.percussion.cms.objectstore.PSSearchField}. Assumed to be not <code>
    * null</code>.
    */
   private void updateSearch(PSDbComponentList fields)
   {
      DefaultTableModel mdl = (DefaultTableModel)m_selectedTable.getModel();
      int sz = mdl.getRowCount();
      Iterator itr = fields.iterator();
      PSSearchField fld = null;
      String srchName = null;
      String mdlName = null;
      //delete entries which have been removed
      if (sz != 0)
      {
         while (itr.hasNext())
         {
            fld = (PSSearchField)itr.next();
            srchName = fld.getFieldName();
            for (int k = 0; k < sz; k++)
            {
               PSLightWeightField lwFld =
                  (PSLightWeightField)mdl.getValueAt(k, FIELD_COLUMN);

               mdlName = lwFld.getInternalName();

               if (mdlName.equalsIgnoreCase(srchName))
                  break;

               if (k == sz - 1)
               {
                  fields.remove(fld);
                  itr = fields.iterator();
               }
            }
         }
      }
      else
      {
         fields.clear();
      }

      //add new entries

      for (int k = 0; k < sz; k++)
      {
         PSLightWeightField lwFld =
                  (PSLightWeightField)mdl.getValueAt(k, FIELD_COLUMN);

         mdlName = lwFld.getInternalName();

         itr = fields.iterator();

         if (itr.hasNext())
         {

            while (itr.hasNext())
            {
               fld = (PSSearchField)itr.next();
               srchName = fld.getFieldName();
               if (mdlName.equalsIgnoreCase(srchName))
                  break;
               if (!itr.hasNext())
               {
                  PSSearchField srchFld = new PSSearchField(
                     lwFld.getInternalName(), lwFld.getDisplayName(),
                     lwFld.getMnemonic(), lwFld.getDataType(), "");
                     
                  if (m_useExternalSearch)
                     srchFld.setExternalOperator(PSCommonSearchUtils.EXT_OP);

                  // load any keywords (disp. choices) found in the catalog map
                  loadKeywords(srchFld);

                  fields.add(srchFld);
                  break;
               }
            }
         }
         else
         {
            PSSearchField srchFld = new PSSearchField(
               lwFld.getInternalName(), lwFld.getDisplayName(),
               lwFld.getMnemonic(), lwFld.getDataType(), "");
               
            if (m_useExternalSearch)
               srchFld.setExternalOperator(PSCommonSearchUtils.EXT_OP);

            // load any keywords (disp. choices) found in the catalog map
            loadKeywords(srchFld);

            fields.add(srchFld);
         }
      }
      
   }

   /**
    * Locates the correct lightweight field in the catalog and sets keywords
    * found into the supplied field.  First checks the local fields, then
    * shared, finally system, looking for a match that has keywords defined.
    *
    * @param field The field to set keywords on, assumed not <code>null</code>.
    */
   private void loadKeywords(PSSearchField field)
   {
      PSDisplayChoices choices = m_ceCatlg.getDisplayChoices(
         field.getFieldName(), field.getFieldType());
      field.setDisplayChoices(choices);
   }

   /**
    * Intialize the 'Available Fields' table with system, shared and local
    * fields on selection in the combo box.
    *
    * @param map contains system, shared or local fields. Assumed to be not
    * <code>null</code>.
    *
    * @param isAll specifies if all the fields have to be displayed.
    */
   private void loadAvailableTableData(Map map, boolean isAll)
   {
      loadAvailableTableData(map, isAll, null);
   }

   /**
    * Intialize the 'Available Fields' table with system, shared and local
    * fields on selection in the combo box.
    *
    * @param map contains system, shared or local fields. Assumed to be not
    * <code>null</code>.
    *
    * @param isAll specifies if all the fields have to be displayed.
    *
    * @param filter the regex string to filter the table on. If <code>null</code>
    * , filtering will not be performed.
    *
    */
   private void loadAvailableTableData(Map map, boolean isAll, String filter)
   {
      if (!isAll)
      {
         loadAvailableTableData(map.values(), filter);
      }
      else
      {
         Iterator itr = map.keySet().iterator();
         String key = "";
         Map dataMap = null;

         key = "";
         itr = map.keySet().iterator();
         while (itr.hasNext())
         {
            key = (String)itr.next();
            dataMap = (Map)map.get(key);
            loadAvailableTableData(dataMap.values(), filter);
         }
      }
   }

   /**
    * Puts the data in the 'Available Fields' table based on the selection in
    * <code>m_availSelectorComboBox</code>.
    * 
    * @param fields collection of system, shared or local fields, assumed to be
    *           not <code>null</code>. Each entry in the collection is
    *           {@link PSLightWeightField}.
    * 
    * @param filter the regex string to filter the table on. If
    *           <code>null</code>, filtering will not be performed.
    *  
    */
   private void loadAvailableTableData(Collection fields, String filter)
   {
      DefaultTableModel modelAvail = (DefaultTableModel) m_availableTableSorter
            .getModel();
      DefaultTableModel modelSel = (DefaultTableModel) m_selectedTable
            .getModel();

      boolean isMatch = false;
      boolean isFilterOn = false;

      if (filter!=null && filter.trim().length()>0)
         isFilterOn = true;

      List sortList = new ArrayList(fields);
      Collections.sort(sortList);

      PSLightWeightField availLwField = null;
      Iterator iter = sortList.iterator();
      while(iter.hasNext())
      {
         // now that all are in, it should be sorted for us:

         availLwField = (PSLightWeightField)iter.next();
         
         // for display formats, skip sys_title as we always add it to the 
         // results
         if (m_dformat != null && 
            availLwField.getInternalName().trim().equalsIgnoreCase(
               CONTENT_TITLE_FIELD))
         {
            continue;
         }
         
         String dispName = availLwField.getDisplayName();
         if (dispName.length() == 0)
            dispName = availLwField.getInternalName();

         if(isFilterOn)
         {
            Perl5Util regex = new Perl5Util();
            // Regex expressions need to be in the form of /exp/
            // so we check for the "/" chars existance and if none
            // we add them to try to make a valid regex expression
            if(filter.indexOf("/") == -1)
               filter = "/" + filter + "/";
            try
            {
               isMatch = regex.match(filter,dispName);
            }
            catch(MalformedPerl5PatternException mpe)
            {
               ErrorDialogs.showErrorDialog(this,
               getResourceString("error.msg.malformed.regex"),
               getResourceString("error.title.malformed.regex"),
               JOptionPane.ERROR_MESSAGE);
               return;
            }
         }

         //skip those that are already in the selected table

         int sz = modelSel.getRowCount();
         boolean foundInSelectedTable = false;

         for (int k = 0; k < sz; k++)
         {
            PSLightWeightField selLwf =
               (PSLightWeightField)modelSel.getValueAt(k, FIELD_COLUMN);

            String selectedName = selLwf.getInternalName();

            if (selectedName.equalsIgnoreCase(availLwField.getInternalName()))
            {
               foundInSelectedTable = true;
               break;
            }
         }

         if (foundInSelectedTable)
         {
            continue;
         }

         if(!isFilterOn || isMatch)
            addLightWeightField2TableModel(modelAvail, availLwField, false, true);
      }

      m_availableTableSorter.fireTableDataChanged();
   }

   /**
    * Finds a given fieldName in the given table model.
    * @param dtm table model, never <code>null</code>.
    * @param fieldName internal field name, never <code>null</code>.
    * @return LightWeightField if match is found, <code>null</code> otherwise.
    */
   private PSLightWeightField findLightWeightFieldInTableModel(
      DefaultTableModel dtm, String fieldName)
   {
      if (dtm==null)
         throw new IllegalArgumentException("dtm may not be null");
      if (fieldName==null)
         throw new IllegalArgumentException("fieldName may not be null");

      if (dtm.getRowCount()<=0)
         return null; //table is empty

      Vector vecData = dtm.getDataVector();

      Iterator itRows = vecData.iterator();

      while (itRows.hasNext())
      {
         Vector row = (Vector)itRows.next();

         PSLightWeightField lwf = (PSLightWeightField)row.elementAt(FIELD_COLUMN);

         if (lwf.getInternalName().equalsIgnoreCase(fieldName))
            return lwf;
      }

      //not found
      return null;
   }

   /**
    * Gets the data for the combo box.
    *
    * @return, vector containing the combo box data, never <code>null</code> or
    * empty.
    */
   private Vector prepareComboData()
   {
      Vector<String> vec = new Vector<String>();
      vec.add(I18N_SYSTEM);
      vec.add(I18N_SHARED);

      //get all local content type names
      Iterator itContentTypeNames =
         m_ceCatlg.getLocalContentTypeMap().keySet().iterator();

      while(itContentTypeNames.hasNext())
         vec.add((String)itContentTypeNames.next());

      vec.add(I18N_ALL);
      return vec;
   }

   /**
    * Based on the row selection in 'Available Fields' and 'Selected Fields'
    * table 'up' and 'down' buttons for moving rows across tables and within
    * 'Selected Fields' are enabld or disabled.
    */
   private class RowSelectionListener implements ListSelectionListener
   {
      /**
       * Implementing the interface. Based on the row selection buttons states
       * {@link #setButtonState()} are changed.
       *
       * @param e provided by the event handling mechanism of swing, never
       * <code>null</code>.
       */
      public void valueChanged(@SuppressWarnings("unused") ListSelectionEvent e)
      {
         setButtonState();
      }
   }

   /**
    * Listens to selection of items in the combo box. Loads available table
    * with appropriate fields based on the selected field type: System, Shared,
    * Local or All.
    */
   public class AvailableComboBoxListener implements ActionListener
   {
      /**
       * Implements the interface.
       *
       * @param e never <code>null</code> provided by the swing event handler
       * mechanism.
       */
      public void actionPerformed(ActionEvent e)
      {
         DefaultTableModel modelAvail =
            (DefaultTableModel)m_availableTableSorter.getModel();

         //remove all rows from the available table
         modelAvail.setRowCount(0);
         m_availableTableSorter.setModel(modelAvail);

         JComboBox cb = (JComboBox)e.getSource();
         String type = (String)cb.getSelectedItem();

         if (type.equalsIgnoreCase(I18N_SYSTEM))
         {
            loadAvailableTableData(m_ceCatlg.getSystemMap(), false);
         }
         else if (type.equalsIgnoreCase(I18N_SHARED))
         {
            loadAvailableTableData(m_ceCatlg.getSharedMap(), false);
         }
         else if (type.equalsIgnoreCase(I18N_ALL))
         {
            loadAvailableTableData(m_ceCatlg.getAll(), true);
         }
         else
         {
            //must be local fields of one of the specific content types
            Collection fields =
               (Collection)m_ceCatlg.getLocalContentTypeMap().get(type);

            loadAvailableTableData(fields, null);
         }
      }
   }

   /**
    * Moves the rows up and down in <code>m_selectedTable</code> table.
    *
    * @param up if <code>true</code> the rows move up else down.
    */
   private void moveSelectedUpDown(boolean up)
   {
      DefaultTableModel dtm = ((DefaultTableModel)m_selectedTable.getModel());
      ListSelectionModel lsm = m_selectedTable.getSelectionModel();
      Vector data = dtm.getDataVector();
      int minIndex = lsm.getMinSelectionIndex();
      int maxIndex = lsm.getMaxSelectionIndex();
      if (up)
      {
         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (lsm.isSelectedIndex(i))
            {
               Object aRow = data.elementAt(i);
               data.removeElementAt(i);
               data.insertElementAt(aRow, i - 1);
               m_changeOccured = true;
            }
         }
      }
      else
      {
         for (int i = maxIndex; i >= minIndex; i--)
         {
            if (lsm.isSelectedIndex(i))
            {
               Object aRow = data.elementAt(i);
               data.removeElementAt(i);
               data.insertElementAt(aRow, i + 1);
               m_changeOccured = true;
            }
         }
      }

       dtm.fireTableDataChanged();
       minIndex = up ? minIndex - 1: minIndex + 1;
       maxIndex = up ? maxIndex - 1: maxIndex + 1;
       m_selectedTable.setRowSelectionInterval(minIndex, maxIndex);
   }

    /**
    * The sortable table header renderer.
    */
   private class SortableTableHeaderRenderer extends DefaultTableCellRenderer
   {
      /**
       * Table sorter for a given sortable table. Initialized in the Ctor,
       * never <code>null</code> after that.
       */
      private PSTableSorter m_tableSorter;

      /**
       * Ctor.
       * @param tableSorter table sorter for a given sortable table,
       * never <code>null</code>.
       */
      public SortableTableHeaderRenderer(PSTableSorter tableSorter)
      {
         if (tableSorter==null)
            throw new IllegalArgumentException("tableSorter may not be null");

         m_tableSorter = tableSorter;
      }

      /**
       * overridden method that displays up and down arrow that indicate sorting
       */
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         super.getTableCellRendererComponent(table, value,
               isSelected, hasFocus, row, column);

         JTableHeader header = table.getTableHeader();
         if (header != null)
         {
             setForeground(header.getForeground());
             setBackground(header.getBackground());
             setFont(header.getFont());
         }

         setBorder(UIManager.getBorder("TableHeader.cellBorder"));

         // is this the column that is being sorted?
         if(table.convertColumnIndexToModel(column) ==
          m_tableSorter.getLatestSortingColumn())
         {
            if(m_tableSorter.isAscending())
               setIcon(ms_upIcon);
            else
               setIcon(ms_downIcon);
         }
         else
         {
            setIcon(null);
         }

         setText((value == null) ? "" : value.toString());
         setHorizontalAlignment(LEFT);

         return this;

      } //method ends

   } //inner class ends

   /**
    * Removes the selected rows in the <code>m_selectedTable</code> table.
    */
   private class RemoveSelectedRowListener implements ActionListener
   {
      private JTable  m_table;

      /**
       * Ctor.
       * @param table selected table, never <code>null</code>.
       */
      RemoveSelectedRowListener(JTable table)
      {
         if (table==null)
            throw new IllegalArgumentException("table may not be null");

         this.m_table = table;
      }

      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
      {
         removeSelectedRows();
         setButtonState();
      }

      /**
       * Removes all selected rows in the <code>m_selectedTable</code> table.
       */
      @SuppressWarnings("unchecked")
      private void removeSelectedRows()
      {
         DefaultTableModel dtm = ((DefaultTableModel) m_table.getModel());
         ListSelectionModel lsm = m_table.getSelectionModel();

         if (lsm.isSelectionEmpty())
            return; //nothing to remove

         Vector data = dtm.getDataVector();
         int minIndex = lsm.getMinSelectionIndex();
         int maxIndex = lsm.getMaxSelectionIndex();

         /*
          * Since those selected fields might be required as a dependency for
          * other fields we need to not simply remove selected ones,
          * but also enforce the dependencies among them.
          */
         Set notSelectedIndexes = new HashSet();
         for (int i = 0; i < data.size(); i++)
            notSelectedIndexes.add(new Integer(i));

         Set selectedIndexes = new TreeSet(new Comparator()
         {
            /**
             * This comparator sorts in descending order.
             * @param o1 - Assumed to be an <code>Integer</code>.
             * @param o2 - Assumed to be an <code>Integer</code>.
             */
            public int compare(Object o1, Object o2)
            {
               int result = ((Integer) o1).compareTo((Integer) o2);
               if (result < 0)
                  result = 1;
               else if (result > 0)
                  result = -1;
               return result;
            }
         });
         
         for (int j = maxIndex; j >= minIndex; j--)
         {
            if (!lsm.isSelectedIndex(j))
               continue;

            Integer selInd = new Integer(j);
            selectedIndexes.add(selInd);
            notSelectedIndexes.remove(selInd);
         }

         // discover all interdependent items' indexes that must be selected
         int result = findAllIndexesToRemove(dtm, selectedIndexes,
            notSelectedIndexes);

         if (result != JOptionPane.YES_OPTION)
            return; //abort;

         // now delete all selected rows
         /* The iterator must return the values from the bottom up (highest
            index to lowest) or the wrong values will be removed (possibly
            non-existent ones). */
         Iterator indexes = selectedIndexes.iterator();
         while (indexes.hasNext())
         {
            int index = ((Integer) indexes.next()).intValue();
            Object test = dtm.getValueAt(index, FIELD_COLUMN);
            if (test != null)
            {
               String fieldName = test.toString();
               if (fieldName.equalsIgnoreCase(CONTENT_TITLE_FIELD))
               {
                  ErrorDialogs.showErrorDialog(
                     PSFieldSelectionEditorDialog.this,
                     getResourceString("systitle.del.msg"),
                     getResourceString("systitle.del.title"),
                     JOptionPane.WARNING_MESSAGE);
                  
                  continue;
               }
            }
            
            data.removeElementAt(index);
            m_changeOccured = true;
         }

         // reload available table
         onApplyAvailableFilter();

         dtm.fireTableDataChanged();
      }

      /**
       * Discovers backward dependencies for all selected fields. The result
       * selectedIndexes contains all the indexes that must be demoved together.
       *
       * @param dtm DefaultTableModel of the SelectedTable,
       * never <code>null</code>.
       * @param selectedIndexes user selected indexes set,
       * never <code>null</code>, may be <code>empty</code>.
       * @param notSelectedIndexes not selected rows' indexes,
       * never <code>null</code>, may be <code>empty</code>.
       */
      private int findAllIndexesToRemove(DefaultTableModel dtm,
                                            Set selectedIndexes,
                                            Set notSelectedIndexes)
      {
         if (dtm==null)
            throw new IllegalArgumentException("dtm may not be null");

         if (selectedIndexes==null)
            throw new IllegalArgumentException("selectedIndexes may not be null");

         if (notSelectedIndexes==null)
            throw new IllegalArgumentException("notSelectedIndexes may not be null");

         Set<Integer> addToSelection = new HashSet<Integer>();

         do
         {
            Iterator itAdded = addToSelection.iterator();
            while (itAdded.hasNext())
            {
               Integer addedInd = (Integer)itAdded.next();
               selectedIndexes.add(addedInd);
               notSelectedIndexes.remove(addedInd);
            }

            addToSelection.clear();

            //for each selected one discover if there is an unselected one that
            //depends on it, if so, then make it select, etc.
            Iterator itSel = selectedIndexes.iterator();
            while (itSel.hasNext())
            {
               Integer iSel = (Integer)itSel.next();
               int i = iSel.intValue();

               PSLightWeightField selectedLwf =
                  (PSLightWeightField)dtm.getValueAt(i, FIELD_COLUMN);

               //check if any other not yet selected field(s) in the selected table
               //depends on the one that is selected for removal?

               Iterator itNotSel = notSelectedIndexes.iterator();

               while (itNotSel.hasNext())
               {
                  Integer iNotSel = (Integer)itNotSel.next();
                  int j = iNotSel.intValue();

                  if (j == i)
                     continue; //this one is already selected for removal

                  PSLightWeightField notSelectedLwf =
                     (PSLightWeightField)dtm.getValueAt(j, FIELD_COLUMN);

                  PSDisplayChoices dispChoices = notSelectedLwf.getDisplayChoices();

                  if (dispChoices==null)
                     continue; //this one has no dependencies

                  PSChoiceFilter choiceFilter = dispChoices.getChoiceFilter();

                  if (choiceFilter==null)
                     continue; //this one has no dependencies

                  Iterator itDep = choiceFilter.getDependentFields().iterator();

                  while(itDep.hasNext())
                  {
                     PSChoiceFilter.DependentField df =
                        (PSChoiceFilter.DependentField)itDep.next();

                     // does this unselected field depend on the selected one?
                     if (!df.getFieldRef().
                         equalsIgnoreCase(selectedLwf.getInternalName()))
                         continue; //nope, skip it

                     if (df.getDependencyType().equalsIgnoreCase(df.TYPE_OPTIONAL))
                     {
                        String msg = getResource("opt.msg.removeOptionalDependent");
                        Object args[] = {df.getFieldRef(),
                           notSelectedLwf.getInternalName()};

                        msg = MessageFormat.format(msg, args);

                        JTextArea textBox = new JTextArea(msg, 8, 20);
                        textBox.setWrapStyleWord( true );
                        textBox.setLineWrap( true );
                        textBox.setEditable( false );
                        JScrollPane pane = new JScrollPane( textBox );
                        pane.setPreferredSize(new Dimension( 400, 125));

                        //ask if user would like to add an optional dependency ?
                        int result = JOptionPane.showConfirmDialog(null,
                                       pane,
                                       getResource("opt.title.removeOptionalDependent"),
                                       JOptionPane.YES_NO_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);

                        switch (result)
                        {
                           case JOptionPane.YES_OPTION:
                              //we got OK, include it
                              break;
                           case JOptionPane.NO_OPTION:
                              continue; //user didn't want to remove dependent
                           default:
                              return JOptionPane.CANCEL_OPTION;
                        }
                     }

                     //this field though unselected depends on the selected
                     //one, so we must remove it along with the selected one.
                     addToSelection.add(iNotSel);
                  }
               }
            }
         }
         while(addToSelection.size() > 0);

         return JOptionPane.YES_OPTION;

      } //method ends

   } //inner class ends


   /**
    * Moves the rows from <code>m_availableTable</code> to <code>m_selectedTable
    * </code> when <code>m_selectBtn</code> is pressed.
    */
   private class MoveAvailableToSelectedRowListener implements ActionListener
   {
      /**
       * Implements the interface.
       *
       * @param e provided by the event handling mechanism of swing, never
       * <code>null</code>.
       */
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
      {

         DefaultTableModel dtmAvail =
            (DefaultTableModel)m_availableTableSorter.getModel();
         ListSelectionModel lsmAvail = m_availableTable.getSelectionModel();

         if (lsmAvail.isSelectionEmpty())
            return; //nothing to move
         m_changeOccured = true;
         DefaultTableModel dtmSel =
            (DefaultTableModel)m_selectedTable.getModel();

         int minIndex = lsmAvail.getMinSelectionIndex();
         int maxIndex = lsmAvail.getMaxSelectionIndex();

         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (!lsmAvail.isSelectedIndex(i))
               continue;

            int modelRow = m_availableTableSorter.getModelRow(i);

            PSLightWeightField lwf =
               (PSLightWeightField)dtmAvail.getValueAt(modelRow, FIELD_COLUMN);

            int result = addLightWeightField2TableModel(dtmSel, lwf, true, false);

            if (result != JOptionPane.YES_OPTION)
               return; //user wants to abort
         }

         //make available table to reload itself
         int cmbInd = m_availSelectorComboBox.getSelectedIndex();
         m_availSelectorComboBox.setSelectedIndex(cmbInd);
         m_availableTableSorter.fireTableDataChanged();

         ((DefaultTableModel)
            m_selectedTable.getModel()).fireTableDataChanged();

         setButtonState();
      }
   }

   /**
    * Recursively builds field dependency tree by discovering
    * and attaching PSLightWeightField instances to the corresponding
    * PSChoiceFilter.DependentField objects.
    * @param lwf a LightWeight field for which dependency tree needs
    * to be built, never <code>null</code>.
    */
   private void buildForwardDependencyTree(PSLightWeightField lwf)

   {
      if (lwf==null)
         throw new IllegalArgumentException("lwf may not be null");

      PSDisplayChoices dispChoices = m_ceCatlg.getDisplayChoices(lwf);
      lwf.setDisplayChoices(dispChoices);

      if (dispChoices==null)
         return;

      PSChoiceFilter choiceFilter = dispChoices.getChoiceFilter();

      if (choiceFilter==null)
         return;

      Iterator itDepFields = choiceFilter.getDependentFields().iterator();

      while(itDepFields.hasNext())
      {
         PSChoiceFilter.DependentField depField =
            (PSChoiceFilter.DependentField)itDepFields.next();

         //see if dependency has already been set

         String fRef = depField.getFieldRef();
         if (depField.getField() != null)
            continue; //already discovered all dependencies

         PSLightWeightField depLwf = lookupField(fRef);

         if (depLwf==null)
         {
            Object args[] = {lwf.getInternalName(), fRef};
            String msg = getResourceString("error.msg.dependentFieldNotFound");

            msg = MessageFormat.format(msg, args);

            ErrorDialogs.showErrorDialog(this,
            msg,
            getResourceString("error.title.dependentFieldNotFound"),
            JOptionPane.ERROR_MESSAGE);
            continue;
         }

         depField.attachField(depLwf);
         buildForwardDependencyTree(depLwf); //recurse
      }
   }

   /**
    * Looks up cataloged LightWeightField by name.
    * First checks local then shared then system field maps.
    * @param fieldName internal field name, never <code>null</code>.
    * @return cataloged LightWeightField if any, may be <code>null</code>.
    */
   private PSLightWeightField lookupField(String fieldName)
   {
      if (fieldName==null)
         throw new IllegalArgumentException("fieldName may not be null");

      //first check local then shared then system fields
      PSLightWeightField lwf =
         (PSLightWeightField) m_ceCatlg.getLocalMap().get(fieldName);

      if (lwf==null)
      {
         lwf = (PSLightWeightField)m_ceCatlg.getSharedMap().get(fieldName);

         if (lwf==null)
            lwf = (PSLightWeightField)m_ceCatlg.getSystemMap().get(fieldName);
      }

      return lwf;
   }

   /**
    * Up and down buttons are enabled or disabled based on row selection in the
    * tables.
    */
   private void setButtonState()
   {
      ListSelectionModel lsm = m_selectedTable.getSelectionModel();
      int rc = m_selectedTable.getRowCount();
      if (m_availableTable.getRowCount() == 0 ||
          m_availableTable.getSelectionModel().isSelectionEmpty())
         m_selectBtn.setEnabled(false);
      else
         m_selectBtn.setEnabled(true);

      if (m_selectedTable.getRowCount() == 0 || lsm.isSelectionEmpty())
      {
         m_upBtn.setEnabled(false);
         m_downBtn.setEnabled(false);
         m_removeBtn.setEnabled(false);
      }
      else
      {
         m_removeBtn.setEnabled(true);

         if (lsm.isSelectedIndex(0))
            m_upBtn.setEnabled(false);
         else
            m_upBtn.setEnabled(true);
         if (lsm.isSelectedIndex(rc - 1))
            m_downBtn.setEnabled(false);
         else
            m_downBtn.setEnabled(true);
      }
   }
   
   /**
    * Sets if this dialog should show the focus highlight. There is no
    * option to reset this value.
    * @param color the new value, must never be <code>null</code>
    */
   public void setUseFocusHighlight(Color color)
   {
      if (color == null)
      {
         throw new IllegalArgumentException("color must never be null");
      }
      m_focusHighlightColor = color;      
      PSFocusBorder border = new PSFocusBorder(1, m_focusHighlightColor);
      border.addToAllNavigable((JComponent) getContentPane());
      border.addToComponent(m_upBtn, true);
      border.addToComponent(m_downBtn, true);
      border.addToComponent(m_removeBtn, true);      
   }
   
   /**
    * Adds a value changed listener to this dialog
    * @param listener cannot be <code>null</code>.
    */
   public void addValueChangedListener(IPSValueChangedListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_valueChangedListeners.contains(listener))
      {
         m_valueChangedListeners.add(listener);
      }
   }
   
   /**
    * Removes the specified value changed listener to this dialog
    * @param listener cannot be <code>null</code>.
    */
   public void removeValueChangedListener(IPSValueChangedListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_valueChangedListeners.contains(listener))
      {
         m_valueChangedListeners.remove(listener);
      }
   }
   
   /**
    * Handles notification for all registered listeners of a value
    * changed event.
    */
   private void fireValueChangedEvent()
   {
      PSValueChangedEvent event = 
         new PSValueChangedEvent(this, Event.ACTION_EVENT);
      Iterator it = m_valueChangedListeners.iterator();
      while(it.hasNext())
      {
         IPSValueChangedListener listener = (IPSValueChangedListener)it.next();
         listener.valueChanged(event);
      }
   }
   
  private PSContentEditorFieldCataloger m_ceCatlg;

  /**
   * Represents all search fields and properties that apply to this search/view.
   * Initialized in the ctor, may be <code>null</code>.
   */
  private PSSearch m_search;

  /**
   * Represents a search/view output. Initialized in the ctor, may be <code>null
   * </code>.
   */
  private PSDisplayFormat m_dformat;

   /**
    * The table shows all the available fields based on the filter defintions.
    * The user can select one or more rows and move them to the 'Selected
    * Fields' table through <code>m_selectBtn</code>.
    */
   private JTable m_availableTable;

   /**
    * The table sorter model that is used to sort the columns data represented
    * by this view, initialized in <code>init()</code> method and never <code>
    * null</code> after that. The data model of this is changed as the view
    * changes.
    */
   private PSTableSorter m_availableTableSorter;

   /**
    * The table shows all the selected fields during this edit session.
    */
   private JTable m_selectedTable;

   /**
    * Moves the selected rows in 'Available Fields' table to 'Selected Fields'.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_selectBtn;

   /**
    * Shifts the selected rows in 'Selected Fields' table up.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_upBtn;

   /**
    * Shifts the selected rows in 'Selected Fields' table down.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_downBtn;

   /**
    * Removes the selected rows in 'Selected Fields' table.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_removeBtn;

   /**
    * the secondary field filter filters the list that made it through the
    * primary field filter. This is regular expression filter. Initialized in {@
    * link #init()}, never <code>null</code> or modified after that.
    */
   private JTextField m_filterText;

   /**
    * The button that is used to apply the filter.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JButton m_applyFilterBtn;

   /**
    * Represents the primary field filter is based on the field type. The user
    * can choose from 'System', 'Shared', 'Local' or 'All'. Initialized in {@
    * link #init()}, never <code>null</code> or modified after that.
    */
   private JComboBox m_availSelectorComboBox;
   
   /**
    * Determines if an external search engine will be used.  Initially 
    * <code>false</code>, modified by calls to 
    * {@link #setUseExternalSearchEngine(boolean)}
    */
   private boolean m_useExternalSearch = false;
   
   /**
    * The color to use when drawing focus borders. Defaulted here, but
    * set through the {@link #setUseFocusHighlight(Color)} method. 
    */
   private Color m_focusHighlightColor = null;
   
   /**
    * Flag indicating that this dialog was launched from within
    * the Eclipse based workbench
    */
   private boolean m_inWorkbench;
   
   /**
    * Flag indicating that a change has occurred since this
    * dialog was initialized.
    */
   private boolean m_changeOccured;
   
   /**
    * List of all registered value changed listeners
    */
   private List<IPSValueChangedListener> m_valueChangedListeners =
         new ArrayList<IPSValueChangedListener>();
   
   /**
    * The green check mark icon used for the apply filter button.
    * Initialized in {@link #init()}. Never null after that.
    */
   private static ImageIcon ms_checkMarkIcon;

   /**
    * Up arrow icon.
    * Initialized in {@link #init()}. Never null after that.
    */
   private static ImageIcon ms_upIcon;

   /**
    * Down arrow icon.
    * Initialized in {@link #init()}. Never null after that.
    */
   private static ImageIcon ms_downIcon;

   /** i18ned keyword for display. */
   private static String I18N_SYSTEM = PSI18NTranslationKeyValues.getInstance().
    getTranslationValue(PSFieldSelectionEditorDialog.class.getName() + "@System");

   /** i18ned keyword for display. */
   private static String I18N_SHARED = PSI18NTranslationKeyValues.getInstance().
    getTranslationValue(PSFieldSelectionEditorDialog.class.getName() + "@Shared");

   /** i18ned keyword for display. */
   private static String I18N_ALL = PSI18NTranslationKeyValues.getInstance().
    getTranslationValue(PSFieldSelectionEditorDialog.class.getName() + "@All");

   /**
    * The index for the column showing the field name.
    */
   private static final int FIELD_COLUMN = 1;

   /**
    * The content title field name.
    */
   public static final String CONTENT_TITLE_FIELD = "sys_title";
}
