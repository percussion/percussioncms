/******************************************************************************
 *
 * [ PSSearchFieldEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.search.ui;

import com.percussion.UTComponents.IUTConstants;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.IPSPropertyPanel;
import com.percussion.guitools.IPSValueChangedListener;
import com.percussion.guitools.PSAccessibleActionListener;
import com.percussion.guitools.PSCalendarButton;
import com.percussion.guitools.PSCalendarField;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.PSStackedPropertyPanel;
import com.percussion.guitools.PSValueChangedEvent;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.search.PSCommonSearchUtils;
import com.percussion.search.PSSearchFieldFilter;
import com.percussion.search.PSSearchFieldOperators;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.IPSRemoteRequester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Search field editor allows customization of a collection of
 * {@link com.percussion.cms.objectstore.PSSearchField} objects.
 */
public class PSSearchFieldEditor extends JPanel
{
   /**
    * Convenience ctor that calls {@link #PSSearchFieldEditor(Iterator, Map,
    * IPSRemoteRequester, Properties, PSContentEditorFieldCataloger) 
    * PSSearchFieldEditor(fields, filterMap, remoteRequester, null, 
    * fieldCatalog)}
    */
   public PSSearchFieldEditor(Iterator fields, Map filterMap,
      IPSRemoteRequester remoteRequester, PSContentEditorFieldCataloger 
      fieldCatalog)
   {
      this(fields, filterMap, remoteRequester, null, fieldCatalog);
   }
   
   /**
    * Convenience ctor that calls {@link #PSSearchFieldEditor(Iterator, Map,
    * IPSRemoteRequester, Properties, PSContentEditorFieldCataloger) 
    * PSSearchFieldEditor(fields, filterMap, remoteRequester, null, 
    * fieldCatalog, boolean)}
    */
   public PSSearchFieldEditor(Iterator fields, Map filterMap,
      IPSRemoteRequester remoteRequester, PSContentEditorFieldCataloger 
      fieldCatalog, boolean inWorkbench)
   {
      this(fields, filterMap, remoteRequester, null, fieldCatalog, inWorkbench);
   }
   
   /**
    * Convenience ctor that calls {@link #PSSearchFieldEditor(Iterator, Map,
    * IPSRemoteRequester, PSContentEditorFieldCataloger) 
    * PSSearchFieldEditor(fields, null, remoteRequester, fieldCatalog)}
    */
   public PSSearchFieldEditor(Iterator fields,
      IPSRemoteRequester remoteRequester, 
      PSContentEditorFieldCataloger fieldCatalog)
   {
      this(fields, null, remoteRequester, fieldCatalog);
   }
   
   /**
    * Convenience ctor that calls {@link #PSSearchFieldEditor(Iterator, Map,
    * IPSRemoteRequester, PSContentEditorFieldCataloger) 
    * PSSearchFieldEditor(fields, null, remoteRequester, fieldCatalog, boolean)}
    */
   public PSSearchFieldEditor(Iterator fields,
      IPSRemoteRequester remoteRequester, 
      PSContentEditorFieldCataloger fieldCatalog, boolean inWorkbench)
   {
      this(fields, null, remoteRequester, fieldCatalog, inWorkbench);
   }
   
   /**
    * Convenience ctor that calls {@link #PSSearchFieldEditor(Iterator, Map,
    * IPSRemoteRequester, Properties, PSContentEditorFieldCataloger) 
    * PSSearchFieldEditor(fields, filterMap, remoteRequester, props, 
    * fieldCatalog, false)}
    */
   public PSSearchFieldEditor(Iterator fields, Map filterMap,
      IPSRemoteRequester remoteRequester, Properties props, 
      PSContentEditorFieldCataloger fieldCatalog)
   {
      this(fields, filterMap, remoteRequester, props, fieldCatalog, false);
   }

   /**
    * Constructs this editor with all parameters.
    * 
    * @param fields Iterator over zero or more <code>PSSearchField</code>
    * objects, never <code>null</code>, may be empty although not useful.
    * @param filterMap can be <code>null</code>.  Key is the field name as a 
    * <code>String</code>, and the value is a <code>PSSearchFieldFilter</code> 
    * object.
    * @param remoteRequester reference to the remote requester,
    * never <code>null</code>.
    * @param props Unused, may be <code>null</code>.
    * 
    * @param fieldCatalog The catalog of content editor fields from the server, 
    * may not be <code>null</code>.
    * @param Flag indicating that this dialog was launched from within
    * the Eclipse based workbench
    */
   public PSSearchFieldEditor(Iterator fields, Map filterMap,
      IPSRemoteRequester remoteRequester, Properties props, 
      PSContentEditorFieldCataloger fieldCatalog, boolean inWorkbench)
   {
      if (fields == null)
         throw new IllegalArgumentException("fields must not be null");


      if (fieldCatalog == null)
         throw new IllegalArgumentException("fieldCatalog may not be null");
         
      m_remoteRequester = remoteRequester;
      m_props = props;
      m_inWorkbench = inWorkbench;
     
      // Loads the fields
      init(fields, filterMap, fieldCatalog);
   }

   /**
    * Retreive the modified collection of objects.
    * 
    * @return A valid iterator over 0 or more <code>PSSearchField</code> 
    * objects.
    */
   public Iterator<PSSearchField> getFields()
   {
      Collection<PSSearchField> c = new ArrayList<PSSearchField>();

      Iterator i = m_viewsCollection.iterator();

      while (i.hasNext())
      {
         CustomFieldPanel p = (CustomFieldPanel) i.next();
         p.update();
         c.add(p.getField());
      }

      return c.iterator();
   }

   /**
    * Perform a save on our components
    *
    * @return <code>true</code> if all fields passed validation, otherwise
    *    an error msg is displayed to the user, the offending field is
    *    given focus and <code>false</code> is returned.
    */
   public boolean save()
   {
      Iterator i = m_viewsCollection.iterator();

      while (i.hasNext())
      {
         CustomFieldPanel p = (CustomFieldPanel) i.next();
         if (!p.update())
            return false;
         
         String msg = PSSearchFieldOperators.validateSearchFieldValue(
            p.getField(), PSI18NTranslationKeyValues.getInstance(), null);
         if (msg != null)
         {
            ErrorDialogs.showErrorDialog(this, msg, null, 
               JOptionPane.ERROR_MESSAGE);            
            return false;
         }
      }
      return true;
   }
   
   /**
    * Determine if this editor will display at least one field for editing.
    * 
    * @return <code>true</code> if this editor will display at least one
    * field for editing, <code>false</code> otherwise.
    */
   public boolean hasFields()
   {
      return m_viewsCollection.size() > 0;
   }

   /**
    * Loads the ui. Sets up accessible info on components.
    * 
    * @param c The collection of search fields, assumed not <code>null</code>.
    * @param m The filter map, may be <code>null</code>.
    * @param cat The field catalog, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void init(Iterator c, Map m, PSContentEditorFieldCataloger cat)
   {
      // Save the collection
      m_fields = c;
      m_searchFilterMap = m;

      IPSPropertyPanel fieldPropertyPanel = m_inWorkbench 
      ? new PSStackedPropertyPanel()
         : new PSPropertyPanel();
            
      setLayout(new BorderLayout());

      Iterator i = m_fields;

      //copy iterator items into collection so that we can go over
      //the items multiple times which is needed to setup dependencies.
      Collection fields = new ArrayList();
      Set<String> fieldNames = new HashSet<String>();
      
      while(i.hasNext())
      {
         PSSearchField field = (PSSearchField) i.next();
         fields.add(field);
         fieldNames.add(field.getFieldName());
      }
      
      try
      {
         // load only new fields
         cat.loadFields(fieldNames, cat.getControlFlags(), false);
      }
      catch (PSCmsException e)
      {
         // Since the catalog has already been created, this is unlikely
         String msg = "Error: " + e.getLocalizedMessage();
         ErrorDialogs.showErrorDialog(this, msg, "Error", 
            JOptionPane.ERROR_MESSAGE);
         e.printStackTrace();
      }
      
      i = fields.iterator();

      while (i.hasNext())
      {
         PSSearchField field = (PSSearchField) i.next();
         String strType = field.getFieldType();
         
         // dynamically set display choices now if we don't already have them
         PSDisplayChoices choices = field.getDisplayChoices();
         if (choices == null || !choices.areChoicesLoaded())
            field.setDisplayChoices(cat.getDisplayChoices(field.getFieldName(), 
               strType));
            
         CustomFieldPanel fieldP = null;

         if (field.hasDisplayChoices())
         {
            fieldP = new KeywordSearchFieldPanel(field, m_searchFilterMap);
         }
         else if (field.usesExternalOperator())
         {
            fieldP = new ExternalSearchFieldPanel(field);
         }
         else if (strType.equalsIgnoreCase(PSSearchField.TYPE_DATE))
         {
            fieldP = new DateSearchFieldPanel(field);
         }
         else if (strType.equalsIgnoreCase(PSSearchField.TYPE_NUMBER))
         {
            fieldP = new NumberSearchFieldPanel(field);
         }
         else if (strType.equalsIgnoreCase(PSSearchField.TYPE_TEXT))
         {
            fieldP = new TextSearchFieldPanel(field);
         }

         if (fieldP != null)
         {
            String dispName = field.getDisplayName();
            if (!dispName.endsWith(":"))
               dispName += ":";
            String mnemonic = cat.getMnemonicKey(field.getFieldName());
            char mn = 0;
            if (StringUtils.isNotEmpty(mnemonic))
            {
               mn = mnemonic.charAt(0);
            }
            fieldP.addValueChangedListener(new IPSValueChangedListener()
               {

                  public void valueChanged(PSValueChangedEvent event)
                  {
                     PSSearchFieldEditor.this.fireValueChangedEvent();                     
                  }
               
               });
            fieldPropertyPanel.addPropertyRow(dispName, fieldP, mn);
            setAccessibilityInfoOnComponents(fieldPropertyPanel, dispName);
            m_viewsCollection.add(fieldP);
         }
      }

      //connect dependent fields
      setupDependentFieldListeners(m_viewsCollection);

      Border b1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
      ((JPanel)fieldPropertyPanel).setBorder(b1);
      JScrollPane fieldsPane = new JScrollPane((JPanel)fieldPropertyPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      if(!m_inWorkbench)
      {
         Border b3 = BorderFactory.createEtchedBorder();
         Border b = BorderFactory.createTitledBorder(b3,
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@Search Criteria"));
         
         fieldsPane.setBorder(b);
      }
      add(fieldsPane, BorderLayout.CENTER);
      //Re-adjust the selection based on the selected values
      Iterator it = m_viewsCollection.iterator();
      while(it.hasNext())
      {
         CustomFieldPanel p = (CustomFieldPanel)it.next();
         PSSearchField f = p.getField();
         if(f==null)
            continue;
         if(f.hasDisplayChoices())
            p.fireFieldSelectionChangeEvent();
      }
   }

   /**
    * @param fieldPropertyPanel The panel to which this search component is
    *           added
    * @param dispName The label which is used to identify the components in the
    *           panel.
    */
   private void setAccessibilityInfoOnComponents(
         IPSPropertyPanel fieldPropertyPanel, String dispName)
   {
      if ( fieldPropertyPanel == null || dispName == null || dispName.length()==0)
         return;
      
      List rowComps = fieldPropertyPanel.getMatchingRowByLabel(dispName);
      if ( rowComps != null )
      {
         Iterator comps = rowComps.iterator();
         // first find the label
         JLabel lbl = null;
         Component[] cmp = null;
         boolean lblFound = false;
         boolean cmpFound = false;
         
         while (comps.hasNext())
         {
            Component comp = (Component)comps.next();
            if (comp instanceof JLabel && ((JLabel)comp).getText().equals(dispName))
            {
               lbl = (JLabel)comp;
               lblFound = true;
            }
            else if ( comp instanceof CustomFieldPanel )
            {
               cmp = ((CustomFieldPanel)comp).getItsComponents();
               cmpFound = true;
            }
         }
         if ( lblFound==true && cmpFound == true && 
              cmp != null && cmp.length > 0)
         {
            lbl.setLabelFor(cmp[0]);
            String str = lbl.getText();
            ((JComponent)cmp[0]).setToolTipText(str);
            ((JComponent)cmp[0]).getAccessibleContext().
               setAccessibleDescription(str);
            ((JComponent)cmp[0]).getAccessibleContext().
               setAccessibleName(str);  
         }
      }
      return;
   }

   /**
    * Connects all dependent fields through custom field listeners.
    * @param fieldPanels collection of fields panels, never <code>null</code>,
    * may be <code>empty</code>.
    */
   private void setupDependentFieldListeners(Collection fieldPanels)
   {
      Iterator itFields = fieldPanels.iterator();
      while (itFields.hasNext())
      {
         CustomFieldPanel cfp1 = (CustomFieldPanel)itFields.next();

         PSSearchField sf = cfp1.getField();

         PSDisplayChoices dispChoices = sf.getDisplayChoices();

         if (dispChoices==null)
            continue; //no dependencies, keep going

         PSChoiceFilter choiceFilter = dispChoices.getChoiceFilter();

         if (choiceFilter==null)
            continue; //no dependencies, keep going

         Iterator itDepFields = choiceFilter.getDependentFields().iterator();

         while (itDepFields.hasNext())
         {
            PSChoiceFilter.DependentField depF =
               (PSChoiceFilter.DependentField)itDepFields.next();

            //dep field name
            String depName = depF.getFieldRef();

            //iterate through all fields and setup listeners
            Iterator itAllFields = fieldPanels.iterator();
            while (itAllFields.hasNext())
            {
               CustomFieldPanel cfp2 = (CustomFieldPanel)itAllFields.next();
               if (cfp2.getField().getFieldName().equalsIgnoreCase(depName))
               {
                  //setup a field selection listener
                  cfp2.addFieldSelectionListener(cfp1);
               }
            }
         }
      }

   }
   
   /**
    * Creates calendar field.
    */
   protected PSCalendarField createCalendarField()
   {
      return new PSCalendarField();
   }

   /**
    * Creates a button for the provided date.
    */
   protected PSCalendarButton createCalendarButton(Date date)
   {
      return new PSCalendarButton(null, date);
   }

   /**
    * Field change selection inteface.
    */
   private interface CustomFieldSelectionEvent
   {
      /**
       * Returns selection as a map of params. A set of name/value pairs.
       * Each key is a String, while each value is either a String or a List of
       * Strings. If a list is supplied, then an htlm param with the name of the
       * key will be created for each entry.
       *
       * @return map of param key-values, never <code>null</code>, may be empty
       * if nothing is selected.
       */
      public Map getSelectionAsParams();
      
      /**
       * Return the field ref of the field that triggered the event
       * 
       * @return the ref, never <code>null</code> or empty. 
       */
      public String getFieldRef();
   }

   /**
    * Every custom field panel implements this interface, which allows
    * to connect interdependent search field panels, so that the data
    * can be dynamically updated based on the user selection.
    */
   private interface CustomFieldSelectionListener extends EventListener
   {
      /**
       * Notifies that field selection has changed
       * @param e a source of the change event, never <code>null</code>.
       */
      public void valueChanged(CustomFieldSelectionEvent e);
   }

   /**
    * Inner panel classes follow these are identified
    * by a particular search fields 'type': keyword, text, number, date
    */

   /**
    * Inner class to represent a search field panel
    */
   abstract class CustomFieldPanel extends JPanel
      implements CustomFieldSelectionListener, CustomFieldSelectionEvent
   {
      public CustomFieldPanel(PSSearchField field)
      {
         if (field == null)
            throw new IllegalArgumentException(
               "field must not be null");

         m_field = field;
         m_choiceFilterUrl = getChoiceFilterUrl();
      }

      public CustomFieldPanel(PSSearchField field, Map filterMap)
      {
         if (field == null)
            throw new IllegalArgumentException(
               "field must not be null");

         m_field = field;
         m_filterMap = filterMap;
         m_choiceFilterUrl = getChoiceFilterUrl();
      }

      /**
       * See {@link CustomFieldSelectionEvent} interface. Dummy implementation
       * that returns map with one key-value pair as fieldName-'empty string'.
       */
      public Map getSelectionAsParams()
      {
         Map<String, String> params = new HashMap<String, String>();

         params.put(m_field.getFieldName(), "");

         return params;
      }
      
      // see CustomFieldSelectionEvent interface
      public String getFieldRef()
      {
         return m_field.getFieldName();
      }


      /**
       * Adds a listener to the list of field panels interested in the selection
       * change notification occurred in this custom field.
       *
       * @param listener an instace of the field change listener,
       * never <code>null</code>.
       */
      public void addFieldSelectionListener(CustomFieldSelectionListener listener)
      {
         //create list on demand
         if (m_selectionListeners==null)
            m_selectionListeners = new ArrayList<CustomFieldSelectionListener>();

         //add new listener
         m_selectionListeners.add(listener);
      }
      
      /**
       * Adds a value changed listener to this dialog
       * @param listener cannot be <code>null</code>.
       */
      public void addValueChangedListener(IPSValueChangedListener listener)   
      {
         if(listener == null)
            throw new IllegalArgumentException("listener cannot be null.");
         if(!mi_valueChangedListeners.contains(listener))
         {
            mi_valueChangedListeners.add(listener);
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
         if(mi_valueChangedListeners.contains(listener))
         {
            mi_valueChangedListeners.remove(listener);
         }
      }
      
      /**
       * Handles notification for all registered listeners of a value
       * changed event.
       */
      protected void fireValueChangedEvent()
      {
         PSValueChangedEvent event = 
            new PSValueChangedEvent(this, Event.ACTION_EVENT);
         Iterator it = mi_valueChangedListeners.iterator();
         while(it.hasNext())
         {
            IPSValueChangedListener listener = (IPSValueChangedListener)it.next();
            listener.valueChanged(event);
         }
      }

      /**
       * Returns search field from which this panel was constructed.
       * @return search field, never <code>null</code>.
       */
      public PSSearchField getField()
      {
         return m_field;
      }

      /**
       * Field selection change notification fired by a custom field panel in
       * order to provide a dependent child field with the current selection
       * made by the user, so that the dependent field panel can refresh its
       * content.
       * @param e event source interface, never <code>null</code>.
       */
      public void valueChanged(CustomFieldSelectionEvent e)
      {

      }

      /**
       * Notifies all interested custom field selection change listeners.
       */
      protected void fireFieldSelectionChangeEvent()
      {
         if (m_selectionListeners==null)
            return; //not set yet

         Iterator itListeners = m_selectionListeners.iterator();

         while(itListeners.hasNext())
         {
            CustomFieldSelectionListener fsl =
               (CustomFieldSelectionListener)itListeners.next();

            //notify an interested custom field of the selection change
            fsl.valueChanged(this);
         }
      }

      protected void wrapWithBorder(JComponent c)
      {
         Border b1 = BorderFactory.createEmptyBorder(7, 7, 7, 5 );
         Border b3 = BorderFactory.createEtchedBorder();
         Border b2 = BorderFactory.createCompoundBorder(b3, b1);
         c.setBorder(b2);
      }
      
      /**
       * @return An array of JComponents that are added for this
       *         CustomFieldPanel This is needed for accessiblity of the
       *         JComponents
       */
      protected abstract void init();
      
      /**
       * An Abstract method to return its components. Accessibility must be
       * turned on the component and not on the enclosing panel or any container
       * 
       * @return array of components in this panel. Implemented by all
       *         subclasses.
       */
      protected abstract JComponent[] getItsComponents();

      /**
       * Returns choice filter lookup Url if any.
       * @return choice filter lookup Url, may be <code>null</code>.
       */
      protected PSUrlRequest getChoiceFilterUrl()
      {
         if (m_choiceFilterUrl!=null)
            return m_choiceFilterUrl;

         PSDisplayChoices dispChoices = m_field.getDisplayChoices();

         if (dispChoices==null)
            return null;

         PSChoiceFilter choiceFilter = dispChoices.getChoiceFilter();

         if (choiceFilter==null)
            return null;

         PSUrlRequest lookup = choiceFilter.getLookup();

         if (lookup==null)
           return null;

         m_choiceFilterUrl = lookup;

         return lookup;
      }

      /**
       * Fetches a document from the server provided resource name and params.
       * For more inforrmatio see {@link IPSRemoteRequester}.
       *
       * @param lookup An instance of the PSUrlRequest that contains a Url and
       * may also contain static params, which are merged with the supplied
       * params map.
       *
       * @param params A set of name/value pairs. Each key is a String, while
       *    each value is either a String or a List of Strings. If a list
       *    is supplied, then an htlm param with the name of the key will
       *    be created for each entry.
       * @return The document representing the returned data, or null if no
       *    data was returned.
       *
       * @throws IOException If any problems occur while communicating with the
       *    server.
       *
       * @throws SAXException If the returned data is not parsable as an xml
       *    document.
       */
      protected Document getRemoteDocument(PSUrlRequest lookup, Map params)
         throws IOException, SAXException
      {
         if (lookup==null)
            throw new IllegalArgumentException("lookup may not be null");

         if (params==null)
            throw new IllegalArgumentException("params may not be null");

         String resource = lookup.getHref();

         if (resource==null)
            throw new IllegalArgumentException("resource may not be null");


         /*if the lookup url XML defines static params
           we also need to merge them into the params map,
           that is expected by the remote requester.
         */
         Iterator itQueryParams = lookup.getQueryParameters();
         while(itQueryParams.hasNext())
         {
            PSParam param = (PSParam)itQueryParams.next();

            String name = param.getName();
            String value = param.getValue().getValueText();

            Object objVal = params.get(name);

            if (objVal!=null)
            {
               if (objVal instanceof List)
               {
                  //multy-value param and the list is already there - add value
                  List listVal = (List)objVal;
                  listVal.add(value);
               }
               else
               {
                  // multy-value param, but the list is NOT yet there -
                  // put a list and add both found and a new params to it.
                  List values = new ArrayList();
                  values.add(objVal);
                  values.add(value);
                  params.put(name, values);
               }
            }
            else
            {
               // for now setup single-value param
               params.put(name, value);
            }
         }

         //use remote requestor to fetch the doc
         if( m_remoteRequester == null)
            return null;
         return m_remoteRequester.getDocument(resource, params);
      }
      

      /**
       * Updates search field data from the UI fields.
       * @return <code>true</code> if the field passed validation and the
       *    update step was successful. <code>false</code> otherwise. If
       *    <code>false</code> is returned, the error msg should have been
       *    displayed to the user and the cursor should be left in the
       *    offending field.
       */
      protected abstract boolean update();

      /**
       * Data model search field, initialized in ctor,
       * never <code>null</code>
       */
      protected PSSearchField m_field;

      /**
       * filter map
       */
      protected Map m_filterMap;

      /**
       * a list of registered field selection listeners.
       * Initilized when {@link #addFieldSelectionListener(listener)}
       * is invoked for the first time, never <code>null</code> after that.
       */
      private Collection<CustomFieldSelectionListener>  m_selectionListeners;

      /**
       * Choice filter Url. Initilized by the Ctor., may be <code>null</code>.
       */
      private PSUrlRequest m_choiceFilterUrl;
      
      /**
       * List of all registered value changed listeners
       */
      private List<IPSValueChangedListener> mi_valueChangedListeners =
            new ArrayList<IPSValueChangedListener>();
      
   }

   class TextSearchFieldPanel extends CustomFieldPanel
   {
      public TextSearchFieldPanel(PSSearchField f)
      {
         super(f);
         init();
      }

      protected void init()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         // Create Components
         m_operatorCombo = new JComboBox(new String []
         {
                 PSCommonSearchUtils.OP_STARTS_WITH,
                 PSCommonSearchUtils.OP_CONTAINS,
                 PSCommonSearchUtils.OP_ENDS_WITH,
                 PSCommonSearchUtils.OP_EXACT
         });

         m_operatorCombo.setMaximumSize(m_operatorCombo.getPreferredSize());
         m_operatorCombo.setMinimumSize(m_operatorCombo.getPreferredSize());

         m_systemText = new UTFixedHeightTextField();
         m_systemText.setMaximumSize(new Dimension(Integer.MAX_VALUE,
               (int) m_systemText.getSize().getHeight()));

         // Layout components
         add(m_operatorCombo);
         add(Box.createRigidArea(new Dimension(5, 0)));
         add(m_systemText);

         
         String op = m_field.getOperator();
         String value = m_field.getFieldValue();
         String opLabel = PSCommonSearchUtils.OP_CONTAINS;
         if (op.length() > 0 && !op.equals(PSSearchField.OP_EQUALS))
         {
            if (value.startsWith("%") && value.endsWith("%"))
            {
               opLabel = PSCommonSearchUtils.OP_CONTAINS;
               if (value.length() > 1)
                  value = value.substring(1, value.length()-1);
               else
                  //just have a %
                  value = "";
            }
            else if (value.startsWith("%"))
            {
               opLabel = PSCommonSearchUtils.OP_ENDS_WITH;
               value = value.substring(1);
            }
            else if (value.endsWith("%"))
            {
               opLabel = PSCommonSearchUtils.OP_STARTS_WITH;
               value = value.substring(0, value.length()-1);
            }
            else
               //default
               opLabel = PSCommonSearchUtils.OP_CONTAINS;
         }
         else if (op.equals(PSSearchField.OP_EQUALS))
            //default
            opLabel = PSCommonSearchUtils.OP_EXACT;

         m_systemText.setText(value);
         m_systemText.getDocument().addDocumentListener(
            new DocumentChangedListener(this));
         m_operatorCombo.addActionListener(new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  PSSearchFieldEditor.TextSearchFieldPanel.
                     this.fireValueChangedEvent();                  
               }
            
            });
         // Add accessible listener for the combo
         m_operatorCombo.addActionListener(new PSAccessibleActionListener());
         m_operatorCombo.setSelectedItem(opLabel);
      }

      protected boolean update()
      {
         String op = (String) m_operatorCombo.getSelectedItem();
         //default operator
         String newOp = PSSearchField.OP_LIKE;
         String value = m_systemText.getText().trim();
         if (op.equals(PSCommonSearchUtils.OP_EXACT))
            newOp = PSSearchField.OP_EQUALS;
         else if (value.length() > 0)
         {
            if (op.equals(PSCommonSearchUtils.OP_STARTS_WITH))
            {
               if (!value.endsWith("%"))
                  value += "%";
            }
            else if (op.equals(PSCommonSearchUtils.OP_CONTAINS))
            {
               if (!value.startsWith("%"))
                  value = "%" + value;
               if (!value.endsWith("%"))
                  value += "%";
            }
            else if (op.equals(PSCommonSearchUtils.OP_ENDS_WITH))
            {
               if (!value.startsWith("%"))
                  value = "%" + value;
            }
         }
         m_field.setFieldValue(newOp, value);
         return true;
      }

      protected JComponent[] getItsComponents()
      {
         JComponent[] jcArr = {m_systemText, m_operatorCombo};
         return jcArr; 
      }

      private UTFixedHeightTextField m_systemText;
      private JComboBox m_operatorCombo;
   }

   /**
    * Inner class to represent a search field panel that displays a set of
    * choices using a combobox.
    */
   class KeywordSearchFieldPanel extends CustomFieldPanel
      implements ListSelectionListener
   {
      /**
       * Construct a keyword panel.
       *
       * @param f The field, assumed not <code>null</code>, must contain a list
       * of keywords.
       */
      public KeywordSearchFieldPanel(PSSearchField f, Map fm)
      {
         super(f, fm);

         if (f.getDisplayChoices() == null)
            throw new IllegalStateException("field must contain keywords");

         init();
      }

      protected void init()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         // create array from the field's keywords, adding blank entry first
         Iterator choices = m_field.getDisplayChoices().getChoices();

         List keywords = new ArrayList();

         while (choices.hasNext())
            keywords.add(choices.next());

         //Apply filter
         if(m_filterMap != null && m_filterMap.containsKey(
            m_field.getFieldName()))
         {
            keywords = ((PSSearchFieldFilter)m_filterMap.get(
               m_field.getFieldName())).getFilteredList(keywords);
         }

         m_keywordList = new JList();
         m_keywordList.addListSelectionListener(this);
         // make it at least 3 and at most 5 rows
         int numRows = keywords.size() > 3 ? 5 : 3;

         if (getChoiceFilterUrl()!=null)
         {
            //has a choice filter, make it 5
            numRows = 5;
         }
         Dimension size = new Dimension(IUTConstants.PREF_WIDTH,
            IUTConstants.FIXED_HEIGHT * numRows);
         JScrollPane scrollPane = new JScrollPane(m_keywordList);
         scrollPane.setPreferredSize(size);
         scrollPane.setMaximumSize(size);

         add(scrollPane);

         loadListData(keywords);
         if(m_field.getFieldName().equalsIgnoreCase(
               IPSHtmlParameters.SYS_COMMUNITYID) &&
               m_props != null &&
               m_props.getProperty(
               IPSHtmlParameters.SYS_RESTRICTFIELDSTOUSERCOMMUNITY, "").
               equalsIgnoreCase("yes"))
            {
               m_keywordList.setEnabled(false);
               String commid = m_props.getProperty(IPSHtmlParameters.SYS_COMMUNITYID);
               int i;
               for (i=0; i < keywords.size(); i++)
               {
                  PSEntry entry = (PSEntry)keywords.get(i);
                  if (commid.equals(entry.getValue()))
                     break;
               }
               if(i<keywords.size())
                  m_keywordList.setSelectedIndex(i);
            }
         m_keywordList.addListSelectionListener(new ListSelectionListener()
            {

               public void valueChanged(ListSelectionEvent e)
               {
                  PSSearchFieldEditor.KeywordSearchFieldPanel.
                     this.fireValueChangedEvent();                  
               }
            
            });
      }
      

      /**
       * Loads/Reloads given keywords into the choice list.
       * @param keywords diplay choices to load, never <code>null</code>,
       * may be <code>empty</code>.
       */
      private void loadListData(List keywords)
      {
         if (keywords==null)
            throw new IllegalArgumentException("keywords may not be null");

         //Apply filter
         if(m_filterMap != null && m_filterMap.containsKey(
            m_field.getFieldName()))
         {
            keywords = ((PSSearchFieldFilter)m_filterMap.get(
               m_field.getFieldName())).getFilteredList(keywords);
         }

         m_keywordList.removeAll();

         DefaultListModel listModel = new DefaultListModel();
         Iterator it = keywords.iterator();
         while(it.hasNext())
            listModel.addElement(it.next());

         m_keywordList.setModel(listModel);

         // try to set a default value based on the field value
         boolean[] selected = new boolean[keywords.size()];
         List vals = m_field.getFieldValues();

         // walk the keywords and see if in the field's values.  If so, mark
         // its index to true
         int selectedCount = 0;
         for (int i = 0; i < keywords.size(); i++)
         {
            PSEntry entry = (PSEntry)keywords.get(i);
            if (vals.contains(entry.getValue()))
            {
               selectedCount++;
               selected[i] = true;
            }
         }

         // build array of selected indexes
         int[] indexes = new int[selectedCount];
         int selIndex = 0;
         for (int i = 0; i < selected.length; i++)
         {
            if (selected[i])
               indexes[selIndex++] = i;
         }

         // now select those indexes
         m_keywordList.setSelectedIndices(indexes);

         // notify all dependents of the new selection
         super.fireFieldSelectionChangeEvent();
      }

      protected boolean update()
      {
         List values = new ArrayList();
         Object[] entries = m_keywordList.getSelectedValues();
         for (int i = 0; i < entries.length; i++)
         {
            PSEntry entry = (PSEntry)entries[i];
            values.add(entry.getValue());
         }
         
         if (m_field.usesExternalOperator())
            m_field.setExternalFieldValues(PSCommonSearchUtils.EXT_OP, values);
         else
            m_field.setFieldValues(PSSearchField.OP_IN, values);

         return true;
      }

      /**
       * Receives JList selection notifications and fires custom field selection
       * change event, so that the dependent fields can refresh their content.
       * @param e event, assumed never <code>null</code>.
       */
      public void valueChanged(ListSelectionEvent e)
      {
         if (e.getValueIsAdjusting()) {
             // The mouse button has not yet been released
             return;
         }

         JList list = (JList)e.getSource();
         int selIndex = list.getSelectedIndex();
         int selCount = list.getSelectedIndices().length;
         int size = list.getModel().getSize();
         if(size>0 && selIndex!=-1 && selCount>0)
         {
            Object obj2 = list.getSelectedValue();
            list.getAccessibleContext().setAccessibleName(
               obj2.toString()
                  + " "
                  + selCount
                  + " selected out of"
                  + size
                  + " total");
         }
         super.fireFieldSelectionChangeEvent();
      }

      /**
       * See {@link CustomFieldSelectionListener} interface.
       */
      public void valueChanged(CustomFieldSelectionEvent e)
      {
         //requery values using supplied query and a choice filter Url

         //get choice filter Url of this search field
         PSUrlRequest lookup = getChoiceFilterUrl();

         if (lookup==null)
            return; //no choice filter

         //get current selection as param map
         Map params = e.getSelectionAsParams();

         try
         {
            List keywords = new ArrayList();

            //query the server to get filtered choices
            Document result = super.getRemoteDocument(lookup, params);

            if (result!=null)
            {
               PSDisplayChoices dispChoices2 =
                  new PSDisplayChoices(result.getDocumentElement());

               Iterator itChoices = dispChoices2.getChoices();

               while (itChoices.hasNext())
                  keywords.add(itChoices.next());
            }
            else
            {
               keywords.add(new PSEntry("No server available to get choices",
                  "No server available to get choices"));
            }

            loadListData(keywords);
         }
         catch (IOException io)
         {
            String msg =
               PSI18NTranslationKeyValues.getInstance().
                  getTranslationValue(getClass().getName() +
                  "@ChoiceFilterLookupException");

            Object args[] = { io.getClass().getName(),
               lookup.getHref(), io.getLocalizedMessage()};

            msg = MessageFormat.format(msg, args);

            ErrorDialogs.showErrorDialog(this, msg,
               null, JOptionPane.ERROR_MESSAGE);

            io.printStackTrace();
         }
         catch (SAXException sax)
         {
            String msg =
               PSI18NTranslationKeyValues.getInstance().
                  getTranslationValue(getClass().getName() +
                  "@ChoiceFilterLookupException");

            Object args[] = { sax.getClass().getName(),
               lookup.getHref(), sax.getLocalizedMessage()};

            msg = MessageFormat.format(msg, args);

            ErrorDialogs.showErrorDialog(this, msg,
               null, JOptionPane.ERROR_MESSAGE);

            sax.printStackTrace();
         }
         catch(PSUnknownNodeTypeException ux)
         {
             String msg =
               PSI18NTranslationKeyValues.getInstance().
                  getTranslationValue(getClass().getName() +
                  "@ChoiceFilterLookupException");

            Object args[] = { ux.getClass().getName(),
               lookup.getHref(), ux.getLocalizedMessage()};

            msg = MessageFormat.format(msg, args);

            ErrorDialogs.showErrorDialog(this, msg,
               null, JOptionPane.ERROR_MESSAGE);

            ux.printStackTrace();
         }
      }

      /**
       * See {@link CustomFieldSelectionEvent} interface.
       */
      public Map getSelectionAsParams()
      {
         DefaultListModel listModel =
               (DefaultListModel)m_keywordList.getModel();

         final Map<String, Object> mapParams = new HashMap<String, Object>();
         final List<Object> listValues = new ArrayList<Object>();

         Object[] entries = m_keywordList.getSelectedValues();

         if (listModel.size() > 0 && entries.length < 1)
         {
            //nothing was selected, which by convention is equivalent to all
            entries = listModel.toArray();
         }

         for (int i = 0; i < entries.length; i++)
         {
            PSEntry entry = (PSEntry)entries[i];

            listValues.add(entry.getValue());
         }

         if (listValues.size() == 1)
            mapParams.put(m_field.getFieldName(), listValues.get(0));
         else if (listValues.size() > 1)
            mapParams.put(m_field.getFieldName(), listValues);
         else
            mapParams.put(m_field.getFieldName(), "");//nothing selected

         return mapParams;
      }
      public JComponent[] getItsComponents()
      {
         JComponent[] jcArr = {m_keywordList};
         return jcArr; 
      }
      private JList m_keywordList;
   }

   /**
    * Inner class to represent a search field panel that allows the entry of
    * 1 or 2 criteria for the field. For example, Greater than xxxx AND Less
    * than yyyy. Where 'Greater than' would be an entry in the strings supplied
    * for the first operand and 'Less than' would be an entry in the strings
    * supplied for the 2nd operand.
    */
   abstract class DualSearchFieldPanel extends CustomFieldPanel
   {
      /**
       *
       * @param f The field being edited. Never <code>null</code>.
       *
       * @param op1 The display strings for the allowed operations on the first
       *    field of the 2 fields available for this editor. Must have at least
       *    1 value. Each value must be one of the OP_xxx values.
       *
       * @param op2 The display strings for the allowed operations on the 2nd
       *    field of the 2 fields available for this editor. Must have at least
       *    1 value. Each value must be one of the OP_xxx values.
       */
      protected DualSearchFieldPanel(PSSearchField f, String[] op1) //, String[] op2)
      {
         super(f);
         if (null == op1)
            throw new IllegalArgumentException("Operand arrays cannot be null.");
         for (int i=0; i < op1.length; i++)
         {
            if (op1[i] == null || op1[i].trim().length() == 0)
            {
               throw new IllegalArgumentException(
                     "No operands in op1 can be null or empty.");
            }
         }
         m_operands1 = op1;
      }

      protected void init()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         JPanel firstCol = new JPanel();
         firstCol.setLayout(new BoxLayout(firstCol, BoxLayout.Y_AXIS));

         JPanel secondCol = new JPanel();
         secondCol.setLayout(new BoxLayout(secondCol, BoxLayout.Y_AXIS));

         // Create Components
         JLabel andLabel = new JLabel(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@AND"));

         m_operatorCombo = new JComboBox(m_operands1);
         
         m_operatorCombo.setMaximumSize(m_operatorCombo.getPreferredSize());
         m_operatorCombo.setMinimumSize(m_operatorCombo.getPreferredSize());

         m_contentIdvalueText = new UTFixedHeightTextField();
         
         m_contentIdvalueText2 = new UTFixedHeightTextField();
         

         Dimension rowDim = new Dimension(0,0);
         rowDim.height = m_operatorCombo.getPreferredSize().height;

         //pixels between components in 2 rows
         int rowSpacing = 2;
         // Layout components
         firstCol.add(m_operatorCombo);
         m_operatorCombo.setAlignmentX(1.0f);
         firstCol.add(Box.createRigidArea(new Dimension(0, rowSpacing)));
         firstCol.add(andLabel);
         andLabel.setAlignmentX(1.0f);

         secondCol.add(m_contentIdvalueText);
         secondCol.add(Box.createRigidArea(new Dimension(0, rowSpacing)));
         secondCol.add(m_contentIdvalueText2);


         add(firstCol);
         add(Box.createRigidArea(new Dimension(5, 0)));
         add(secondCol);
         add(Box.createRigidArea(new Dimension(5, 0)));
         wrapWithBorder(this);

         // Load the values
         initFieldData();
         m_operatorCombo.addActionListener(new ActionListener()
            {        
               
               public void actionPerformed(ActionEvent e)
               {
                  PSSearchFieldEditor.DualSearchFieldPanel.
                     this.fireValueChangedEvent();
               
               }
            });
         m_contentIdvalueText.getDocument().addDocumentListener(
            new DocumentChangedListener(this));
         m_contentIdvalueText2.getDocument().addDocumentListener(
            new DocumentChangedListener(this));
      }

      private void initFieldData()
      {
         String strFirstValue = "";
         String strSecondValue = "";

         List values = m_field.getFieldValues();

         if (values.size() > 0)
         {
            strFirstValue = (String) values.get(0);
         }
         if (values.size() > 1)
         {
            strSecondValue = (String) values.get(1);
         }

         m_contentIdvalueText.setText(strFirstValue);
         m_contentIdvalueText2.setText(strSecondValue);

         m_operatorCombo.setSelectedItem(getLabelForOp(m_field.getOperator()));
      }

      abstract protected String getLabelForOp(String operator);
      abstract protected String getOpForLabel(String label);

      protected boolean update()
      {
         String op = getOpForLabel((String) m_operatorCombo.getSelectedItem());
         List<String> values = new ArrayList<String>();
         //all operators have at least 1 value
         values.add(m_contentIdvalueText.getText().trim());
         if (op.equalsIgnoreCase(PSSearchField.OP_BETWEEN))
         {
            values.add(m_contentIdvalueText2.getText().trim());
         }
         m_field.setFieldValues(op, values);
         return true;
      }

      public JComponent[] getItsComponents()
      {
         JComponent[] jcArr = {m_operatorCombo, m_contentIdvalueText, m_contentIdvalueText2};
         return jcArr; 
      }
      private String[] m_operands1;
      private JComboBox m_operatorCombo;
      private UTFixedHeightTextField m_contentIdvalueText;
      private UTFixedHeightTextField m_contentIdvalueText2;
   }

   /**
    * Inner class to represent a search field panel that allows the entry of
    * 1 or 2 Dates for the field. For example, Greater than xxxx AND Less
    * than yyyy. Where 'Greater than' would be an entry in the strings supplied
    * for the first operand and 'Less than' would be an entry in the strings
    * supplied for the 2nd operand.
    */
   class DateSearchFieldPanel extends CustomFieldPanel
   {
      /**
       * Constructs a new <code>DateSearchFieldPanel</code>.
       * @param f The field being edited. Never <code>null</code>.
       */
      protected DateSearchFieldPanel(PSSearchField f)
      {
         super(f);
         init();
      }

      /**
       * Initializes the gui
       */
      protected void init()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         JPanel firstCol = new JPanel();
         firstCol.setLayout(new BoxLayout(firstCol, BoxLayout.Y_AXIS));

         JPanel secondCol = new JPanel();
         secondCol.setLayout(new BoxLayout(secondCol, BoxLayout.Y_AXIS));

         // Create Components
         JLabel andLabel = new JLabel(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@AND"));

         m_operatorCombo = new JComboBox(m_operands1);
         m_operatorCombo.setMaximumSize(m_operatorCombo.getPreferredSize());
         m_operatorCombo.setMinimumSize(m_operatorCombo.getPreferredSize());

         m_contentIdvalueText = createCalendarField();
         
         m_contentIdvalueText.setHeightFixed(true);
         m_contentIdvalueText2 = createCalendarField();
         
         m_contentIdvalueText2.setHeightFixed(true);

         Dimension rowDim = new Dimension(0,0);
         rowDim.height = m_operatorCombo.getPreferredSize().height;

         //pixels between components in 2 rows
         int rowSpacing = 2;
         // Layout components
         firstCol.add(m_operatorCombo);
         m_operatorCombo.setAlignmentX(1.0f);
         firstCol.add(Box.createRigidArea(new Dimension(0, rowSpacing)));
         firstCol.add(andLabel);
         
         // add accessible listener
         m_operatorCombo.addActionListener(new PSAccessibleActionListener(andLabel.getText()));
         andLabel.setAlignmentX(1.0f);

         secondCol.add(m_contentIdvalueText);
         secondCol.add(Box.createRigidArea(new Dimension(0, rowSpacing)));
         secondCol.add(m_contentIdvalueText2);


         add(firstCol);
         add(Box.createRigidArea(new Dimension(5, 0)));
         add(secondCol);
         add(Box.createRigidArea(new Dimension(5, 0)));
         wrapWithBorder(this);

         // Load the values
         initFieldData();
         m_operatorCombo.addActionListener(new ActionListener()
            {               
               public void actionPerformed(ActionEvent e)
               {
                  PSSearchFieldEditor.DateSearchFieldPanel.
                     this.fireValueChangedEvent();               
               }
            });
         m_contentIdvalueText.addValueChangedListener(new ValueChangedListener(this));
         m_contentIdvalueText2.addValueChangedListener(new ValueChangedListener(this));
      }

      /**
       * Loads the data into the calendar fields
       */
      private void initFieldData()
      {
         String strFirstValue = "";
         String strSecondValue = "";

         List values = m_field.getFieldValues();

         if (values.size() > 0)
         {
            strFirstValue = (String) values.get(0);
         }
         if (values.size() > 1)
         {
            strSecondValue = (String) values.get(1);
         }

         m_contentIdvalueText.setDate(strFirstValue, null);
         m_contentIdvalueText2.setDate(strSecondValue, null);

         m_operatorCombo.setSelectedItem(getLabelForOp(m_field.getOperator()));
      }

      /**
       * Returns the label for the specified operator
       * @param operator
       * @return the label
       */
      protected String getLabelForOp(String operator)
      {
         return match(m_ops, operator, 0, 1);
      }

      /**
       * Returns the operator for the specified label
       * @param label
       * @return the operator string
       */
      protected String getOpForLabel(String label)
      {
         return match(m_ops, label, 1, 0);
      }

      private String match(String[][] values, String key, int checkIndex,
            int valueIndex)
      {
         for (int i=0; i < values.length; i++)
         {
            if (values[i][checkIndex].equals(key))
               return values[i][valueIndex];
         }
         return "";
      }

      /**
       * Update the data from the calendar fields
       * @return <code>true</code> if update occurred.
       */
      protected boolean update()
      {
         String op = getOpForLabel((String) m_operatorCombo.getSelectedItem());
         List<String> values = new ArrayList<String>();
         //all operators have at least 1 value
         values.add(m_contentIdvalueText.getDateString().trim());
         if (op.equalsIgnoreCase(PSSearchField.OP_BETWEEN))
         {
            values.add(m_contentIdvalueText2.getDateString().trim());
         }
         m_field.setFieldValues(op, values);
         return true;
      }

      /**
       * Array of available operands. Never <code>null</code>.
       */
      private String[] m_operands1 =
      {
              PSCommonSearchUtils.OP_ON,
              PSCommonSearchUtils.OP_BEFORE,
              PSCommonSearchUtils.OP_AFTER,
              PSCommonSearchUtils.OP_BETWEEN
      };

      public JComponent[] getItsComponents()
      {
         JComponent[] jcArr = {m_operatorCombo, m_contentIdvalueText};
         return jcArr; 
      }
      /**
       * The operator combo box. Initialized in {@link #init()}.
       * Never <code>null</code> after that.
       */
      private JComboBox m_operatorCombo;

      /**
       * The first calendar field. Initialized in {@link #init()}.
       * Never <code>null</code> after that.
       */
      private PSCalendarField m_contentIdvalueText;

      /**
       * The second calendar field. Initialized in {@link #init()}.
       * Never <code>null</code> after that.
       */
      private PSCalendarField m_contentIdvalueText2;

      /**
       * Array of operations. Never <code>null</code>.
       */
      private String[][] m_ops =
      {
         {PSSearchField.OP_EQUALS, PSCommonSearchUtils.OP_ON},
         {PSSearchField.OP_GREATERTHAN, PSCommonSearchUtils.OP_AFTER},
         {PSSearchField.OP_LESSTHAN, PSCommonSearchUtils.OP_BEFORE},
         {PSSearchField.OP_BETWEEN, PSCommonSearchUtils.OP_BETWEEN}
      };

   }


   /**
    * Very thin wrapper class that supplies the correct operands for dealing
    * with numbers.
    */
   class NumberSearchFieldPanel extends DualSearchFieldPanel
   {
      public NumberSearchFieldPanel(PSSearchField f)
      {
         super(f, new String [] {
                 PSCommonSearchUtils.OP_EQUALS,
                 PSCommonSearchUtils.OP_GREATER_THAN,
                 PSCommonSearchUtils.OP_LESS_THAN,
                 PSCommonSearchUtils.OP_BETWEEN});
         //this must be used or the compiler can't determine which init to call
         this.init();
      }
      protected String getLabelForOp(String operator)
      {
         return match(m_ops, operator, 0, 1);
      }

      protected String getOpForLabel(String label)
      {
         return match(m_ops, label, 1, 0);
      }

      private String match(String[][] values, String key, int checkIndex,
            int valueIndex)
      {
         for (int i=0; i < values.length; i++)
         {
            if (values[i][checkIndex].equalsIgnoreCase(key))
               return values[i][valueIndex];
         }
         return "";
      }
      public JComponent[] getItsComponents()
      {
         return super.getItsComponents();
      }
      /**
       * Must match ctor.
       */
      private String[][] m_ops =
      {
         {PSSearchField.OP_EQUALS, PSCommonSearchUtils.OP_EQUALS},
         {PSSearchField.OP_GREATERTHAN, PSCommonSearchUtils.OP_GREATER_THAN},
         {PSSearchField.OP_LESSTHAN, PSCommonSearchUtils.OP_LESS_THAN},
         {PSSearchField.OP_BETWEEN, PSCommonSearchUtils.OP_BETWEEN}
      };

   }

   /**
    * Panel to display and edit an external field value.
    */
   class ExternalSearchFieldPanel extends CustomFieldPanel
   {
      // see base class
      public ExternalSearchFieldPanel(PSSearchField f)
      {
         super(f);
         if (!f.usesExternalOperator())
            throw new IllegalArgumentException(
               "field must use external operator");
         init();
      }

      /**
       * Initializes the panel with a text field, and adds a calendar pop-up
       * button if a date field is supplied.
       */
      protected void init()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         // Create Components
         m_systemText = new UTFixedHeightTextField();
         m_systemText.setMaximumSize(new Dimension(Integer.MAX_VALUE,
               (int) m_systemText.getSize().getHeight()));

         // Layout components
         add(m_systemText);
         // add date button if a date field
         if (m_field.getFieldType().equalsIgnoreCase(PSSearchField.TYPE_DATE))
         {            
            final PSCalendarButton button = createCalendarButton(
                  PSCalendarField.convertStringToDate(m_field.getFieldValue()));
            button.addDateChangedListener(new ActionListener()
            {  
               public void actionPerformed(ActionEvent e)
               {
                  // if a date change event, then update the result
                  if (e.getActionCommand().equals(
                     PSCalendarButton.DATE_UPDATED_CMD))
                  {  
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                     String strDate = sdf.format(button.getDate()); 
                     m_systemText.setText(strDate);
                  }
               }               
            });
               
            add(button);
         }

         // concat all values space delim 
         String txt = "";
         Iterator values = m_field.getFieldValues().iterator();
         while (values.hasNext())
         {
            txt += (values.next().toString() + " ");
         }
         m_systemText.setText(txt.trim());
         m_systemText.getDocument().addDocumentListener(
            new DocumentChangedListener(this));
      }

      // see base class
      protected boolean update()
      {
         m_field.setExternalFieldValue(PSCommonSearchUtils.EXT_OP, m_systemText.getText().trim());
         return true;
      }

      /**
       * Override to set focus always on the operand combo box.
       * @see javax.swing.JComponent#requestFocus()
       */
      public void requestFocus()
      {
         m_systemText.requestFocus();
      }

      public JComponent[] getItsComponents()
      {
         JComponent[] jcArr = {m_systemText};
         return jcArr; 
      }
      
      /**
       * Adds an action listener to this dialog
       * @param listener cannot be <code>null</code>.
       */
      public void addActionListener(ActionListener listener)   
      {
         if(listener == null)
            throw new IllegalArgumentException("listener cannot be null.");
         if(!m_actionListeners.contains(listener))
         {
            m_actionListeners.add(listener);
         }
      }
      
      /**
       * Adds an action listener to this dialog
       * @param listener cannot be <code>null</code>.
       */
      public void removeActionListener(ActionListener listener)   
      {
         if(listener == null)
            throw new IllegalArgumentException("listener cannot be null.");
         if(m_actionListeners.contains(listener))
         {
            m_actionListeners.remove(listener);
         }
      }
      
      /**
       * Field to hold the text entered for the parameter value.  Never
       * <code>null</code> or modified after construction.
       */
      private UTFixedHeightTextField m_systemText;      
   }
   
   /**
    * Adds a value changed listener to this dialog
    * @param listener cannot be <code>null</code>.
    */
   public void addValueChangedListener(IPSValueChangedListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if (!m_valueChangedListeners.contains(listener))
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
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if (m_valueChangedListeners.contains(listener))
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
   
   class ValueChangedListener implements IPSValueChangedListener
   {
      
      ValueChangedListener(CustomFieldPanel panel)
      {
         mi_panel = panel;
      }
       
      public void valueChanged(PSValueChangedEvent event)
      {
         mi_panel.fireValueChangedEvent();
         
      }
      
      private CustomFieldPanel mi_panel;
      
   }
   
   class DocumentChangedListener implements DocumentListener
   {
      
      DocumentChangedListener(CustomFieldPanel panel)
      {
         mi_panel = panel;
      }      

      public void insertUpdate(DocumentEvent e)
      {
         mi_panel.fireValueChangedEvent();        
      }

      public void removeUpdate(DocumentEvent e)
      {
         mi_panel.fireValueChangedEvent();         
      }

      public void changedUpdate(DocumentEvent e)
      {
         mi_panel.fireValueChangedEvent();         
      }
     
      private CustomFieldPanel mi_panel;
   }

   /**
    * Collection of objects to edit via this ui.
    * Initialized in ctor, never <code>null</code>.
    */
   private Iterator m_fields;
   
   private List<ActionListener> m_actionListeners =
         new ArrayList<ActionListener>();

   /**
    * Collection of objects to edit via this ui.
    * Initialized in ctor, never <code>null</code>.
    */
   private Map m_searchFilterMap;

   /**
    * Properties that can be passed to the constructor and can be used
    * apporpiately
    */
   private Properties m_props; 

   /**
    * Flag indicating that this dialog was launched from within
    * the Eclipse based workbench
    */
   private boolean m_inWorkbench;
   
   /**
    * List of all registered value changed listeners
    */
   private List<IPSValueChangedListener> m_valueChangedListeners =
         new ArrayList<IPSValueChangedListener>();

   /**
    * Collection of search field panels. Never <code>null</code>
    */
   private Collection m_viewsCollection = new ArrayList();

   /**
    * Reference to the remote requester that is used by fields with choice
    * filters to dynamically fetch filtered field choices from the server.
    * Initialized in ctor, never <code>null</code> after that.
    */
   private IPSRemoteRequester m_remoteRequester = null;

   

}
