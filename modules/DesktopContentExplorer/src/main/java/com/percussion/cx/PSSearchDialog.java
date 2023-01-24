/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.cx;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSCataloger;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.UTStandardCommandPanel;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.search.ui.PSSearchAdvancedPanel;
import com.percussion.search.ui.PSSearchFieldEditor;
import com.percussion.search.ui.PSSearchSimplePanel;
import com.percussion.util.IPSHtmlParameters;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Dialog to build/edit Search Query.
 */
public class PSSearchDialog extends PSDialog
{
   static Logger log = Logger.getLogger(PSSearchDialog.class);
   
   /**
    * Constructs the dialog with supplied parameters.
    * <p>Either retrieves a <code>PSSearch</code> or creates a new one 
    * depending on the supplied flags. If this is a new, non-RC search, 
    * a new one will be created, otherwise, it is obtained from the supplied
    * <code>mgr</code>.
    * <p>If a new one is created, and the property 
    * {@link IPSConstants#PROPERTY_FOLDER_PATH} is found on the
    * supplied <code>searchNode</code>, the 
    * {@link PSSearch#PROP_FOLDER_PATH} will be set on the created 
    * <code>PSSearch</code> object, along with the 
    * {@link PSSearch#PROP_FOLDER_PATH_RECURSE} property set to 
    * <code>true</code>.
    * <p>If the <code>PSSearch</code> object that is edited by this dialog
    * contains the folder path property, it will affect the behavior of this 
    * class. Firstly, the title will change to indicate that the search is a
    * folder search. Secondly, a checkbox will appear to allow the user to 
    * change the recurse property. 
    * 
    * @param parent the parent frame of the dialog, may be <code>null</code>
    * @param mgr the action manager to use to get different catalogers to
    * get the search object, display formats and content editor fields, may not
    * be <code>null</code>
    * @param cataloger The PSRemoteCataloger object.
    * @param searchNode the search node whose search criteria is to be edited,
    * may not be <code>null</code>
    * @param filterMap It is the map that need to be applied on the contents of 
    * search fields.
    * @param isNewSearch if <code>true</code> an empty search object will be used
    * otherwise the search object corresponding to initiating search will be used.
    * Ignored if isRcSearch param is set to <code>true</code>.
    * @param isRcSearch <code>true</code> if the search is Related Content Search.
    * otherwise <code>false</code>. This is used to select appropriate search 
    * object from the cataloger.
    * @param searchConfig the search configuration, never <code>null</code>.
    * @param searchableFieldsCache it is used to cache (or hold) the catalogged 
    * searchable fields. It maps cataloger flag of the applet (as 
    * <code>String</code> object) to its corresponding searchable fields 
    * (as <code>PSContentEditorFieldCataloger</code> object). It may be
    * <code>null</code> if not to cache the searchable fields.
    * @param codeBase the code base of the current applet, which is used as
    * part of the key for the searchable fields cache.
    * 
    * @throws PSCmsException if the catalog request to get the content editor
    * fields fails.
    */
   public PSSearchDialog(Frame parent, PSSearchViewActionManager mgr,
         PSRemoteCataloger cataloger, PSNode searchNode, Map filterMap,
         boolean isNewSearch, boolean isRcSearch, PSSearchConfig searchConfig,
         Map searchableFieldsCache, URL codeBase)
         throws PSCmsException
   {
      super(parent, mgr.getApplet().getResourceString(
         PSSearchDialog.class, "Content Search"));

      if(mgr == null)
         throw new IllegalArgumentException("mgr may not be null.");
      if(mgr.getApplet() == null)
         throw new IllegalArgumentException("applet may not be null.");

      if(cataloger == null)
         throw new IllegalArgumentException("cataloger may not be null.");

      if(searchNode == null)
         throw new IllegalArgumentException("searchNode may not be null.");
      if(searchConfig == null)
         throw new IllegalArgumentException("searchConfig may not be null.");
      
      m_mgr = mgr;
      m_applet = mgr.getApplet();
      m_remoteCataloger = cataloger;
      m_searchNode = searchNode;
      m_searchFieldFilterMap = filterMap;
      m_searchConfig = searchConfig;
      if(isRcSearch)
      {
         m_search = m_mgr.getRcSearch();
      }
      else
      {
         m_search = m_mgr.getSearchById(m_searchNode.getSearchId());
      }
      
      if(m_search == null)
         throw new IllegalStateException(
            "Search Criteria is not found to edit/create new search");

      if(isNewSearch && !isRcSearch)
      {
         m_newSearch = true;
         try
         {
            m_search = new PSSearch(
            m_mgr.getEmptySearchDoc().getDocumentElement());
            String folderPath = 
                  searchNode.getProp(IPSConstants.PROPERTY_FOLDER_PATH);
            if (null != folderPath && folderPath.trim().length() > 0)
            {
               m_search.setProperty(PSSearch.PROP_FOLDER_PATH, folderPath);
               m_search.setProperty(PSSearch.PROP_FOLDER_PATH_RECURSE, "true");
            }
         }
         catch(Exception e)
         {
            /*This should not happen as we have already created a search from
            document*/
            e.printStackTrace();
         }
      }

      m_folderPath = m_search.getProperty(PSSearch.PROP_FOLDER_PATH);
      if (null != m_folderPath && m_folderPath.trim().length() == 0)
         m_folderPath = null;
      if (null != m_folderPath)
      {
         String titleTemplate = m_applet.getResourceString(
               getClass(), "@Folder Search: {0}");
         setTitle(MessageFormat.format(titleTemplate, 
               new Object[] {m_folderPath}));
      }

      /*If isRestrictToUserCommunity is true then add communityid and the
      flag to the m_feprops. PSSearchFieldEditor in turn looks into these
      properties and avoids the user from selecting the community.*/

      if(isRestrictToUserCommunity())
      {
         m_feprops.put(IPSHtmlParameters.SYS_COMMUNITYID, String.valueOf(
               m_applet.getUserInfo().getCommunityId()));
         m_feprops.put(
               IPSHtmlParameters.SYS_RESTRICTFIELDSTOUSERCOMMUNITY,"yes");
      }

      int controlflags = 0x0;

      /*If isRestrictToUserCommunity is true then field catalogging needs to be
      restricted to the user community.*/

      if(m_applet.isContentRestrict())
         controlflags = controlflags | IPSCataloger.FLAG_RESTRICT_TOUSERCOMMUNITY;
      if(m_search.isRCSearch() || m_search.isUserSearch())
         controlflags = controlflags | IPSCataloger.FLAG_USER_SEARCH;

      /*
       * Add the control falg to exclude the content types that have
       * value hidden from menu as 1 
       */
      controlflags = controlflags | IPSCataloger.FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU;

      initFieldCatalog(controlflags, cataloger, searchableFieldsCache, codeBase);

      // default to advanced if not defined for backward compatibility
      String strMode = m_search.getProperty(PSSearch.PROP_SEARCH_MODE);
      m_advSearch = !PSSearch.SEARCH_MODE_SIMPLE.equals(strMode);
      
      initDialog();
   }

   /**
    * Initialize the searchable fields.
    * 
    * @param controlflags the control flag used to catalog the fields.
    * @param cataloger the cataloger, assume not <code>null</code>.
    * @param searchableFieldsCache it is used to cache (or hold) the catalogged 
    *    searchable fields. It maps cataloger flag of the applet (as 
    *    <code>String</code> object) to its corresponding searchable fields 
    *    (as <code>PSContentEditorFieldCataloger</code> object). It may be
    *    <code>null</code> if not to cache the searchable fields.
    * @param codeBase the code base of the current applet, which is used as
    *    part of the key for the searchable fields cache.
    * 
    * @throws PSCmsException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void initFieldCatalog(int controlflags,
         PSRemoteCataloger cataloger, Map searchableFieldsCache, URL codeBase)
         throws PSCmsException
   {
      Set<String> fieldNames = new HashSet<String>();
      Iterator fields = m_search.getFields();
      while (fields.hasNext())
         fieldNames.add(((PSSearchField)(fields.next())).getFieldName());
         
      if (searchableFieldsCache == null)
      {
         if (m_applet.isDebug())
            log.debug("Load searchableFields no cache");
         
         m_fieldCatalog = new PSContentEditorFieldCataloger(cataloger,
            fieldNames, controlflags);
      }
      else
      {
         // key is the combination of "code base" and the "control flags"
         String key = codeBase.toString() + "@" + controlflags;
         
         m_fieldCatalog = (PSContentEditorFieldCataloger) searchableFieldsCache
               .get(key);
         if (m_fieldCatalog == null)
         {
            if (m_applet.isDebug())
               log.debug("Load searchableFields for key = " + key);
            
            m_fieldCatalog = new PSContentEditorFieldCataloger(cataloger,
               fieldNames, controlflags);
            searchableFieldsCache.put(key, m_fieldCatalog);
         }
      }
   }
   
   /* 
    * @see com.percussion.guitools.PSDialog#subclassHelpId(java.lang.String)
    */
   protected String subclassHelpId(String helpId)
   {
      String temp = helpId;
      if(!m_isEngineAvailable)
         temp += "_noengine";  
      return temp + (m_advSearch ? "_advanced" : "_simple");
   }
   
   /**
    * Initializes the dialog box with initial values. The dropdown list for the
    * display format options are obtained from the display format catalog. The
    * search object associated with content explorer node is initialized by
    * looking from the search catalog.
    */
   @SuppressWarnings("unchecked")
   private void initDialog()
   {
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      final Color focusColor = dispOptions.getFocusColor();
      PSFocusBorder border = new PSFocusBorder(1, focusColor);
      
      
      // get setting for external engine
      m_isEngineAvailable = m_searchConfig.isFtsEnabled() 
            && m_search.useExternalSearch();
      m_mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 3, 7));
      m_mainPanel.setLayout(new BoxLayout(m_mainPanel, BoxLayout.Y_AXIS));

      m_fieldEditorCtrl = new PSSearchFieldEditor(m_search.getFields(),
         m_searchFieldFilterMap, m_remoteCataloger.getRemoteRequester(),
         m_feprops, m_fieldCatalog);
      setFieldEditorSize();

      Map map = new HashMap(); 
      Iterator iter = m_mgr.getDisplayFormats();
      while(iter.hasNext())
      {
         PSDisplayFormat format = (PSDisplayFormat)iter.next();
         map.put(Integer.toString(format.getDisplayId()),
                 format.getDisplayName());
      }
      m_searchSimplePanel = new PSSearchSimplePanel(m_isEngineAvailable, map, 
         m_searchConfig.getMaxSearchResult());
      m_ftQuery = m_searchSimplePanel.getQueryText();
      
      if (null != m_folderPath)
      {
         String labelKey = "@Include Subfolders:";
         m_searchSimplePanel.addPropertyRow(
               m_applet.getResourceString(getClass(), labelKey), 
               m_includeSubfoldersCtrl, 
               PSContentExplorerApplet.getResourceMnemonic(getClass(), labelKey,
               'I'));
      }
      
      m_searchPanelPanel = new JPanel(new BorderLayout());

      // Search simple panel will have full text query components and display 
      // format components. if search engine is vailable otherwise only display 
      // format components.
      // For backward compatibility add this panel before search criteria panel 
      // if engine is available otherwise after it.  
      if(m_isEngineAvailable)
      {      
         m_mainPanel.add(m_searchSimplePanel);
         m_mainPanel.add(m_searchPanelPanel);
         m_mainPanel.add(Box.createVerticalStrut(10));
      }
      else
      {
         m_mainPanel.add(m_searchPanelPanel);
         m_mainPanel.add(Box.createVerticalStrut(5));
         m_mainPanel.add(m_searchSimplePanel);
         m_mainPanel.add(Box.createVerticalStrut(10));
      }

      m_advancedPropsPanel = new PSSearchAdvancedPanel(m_isEngineAvailable, 
            m_mgr.isDBCaseSensitive(), 
            m_searchConfig.isSynonymExpansionRequired());

      // add search panel if not using search engine regardless of search mode
      if(!m_isEngineAvailable || m_advSearch)
      {
         populateAdvancedPanel();
      }
      
      //add listener on advanced panel synonym expansion control
      m_advancedPropsPanel.getSynonymExpansionControl().addActionListener(
            new ActionListener()
            {
               public void actionPerformed(
                     @SuppressWarnings("unused") ActionEvent e)
               {
                  boolean isSelected = m_advancedPropsPanel.
                     getSynonymExpansionControl().isSelected();
                  if (isSelected)
                  {
                     //validate fts query for use with synonym expansion
                     String ftsQuery = m_ftQuery.getText();
                     if (ftsQuery != null)
                     {
                        ftsQuery = ftsQuery.trim();
                        if (!validateForSynonymExp(ftsQuery))
                        {
                           m_advancedPropsPanel.getSynonymExpansionControl().
                              setSelected(false);
                        }
                     }
                  }
               }
            });
      
      //add command panel
      UTStandardCommandPanel defCommandPanel = new UTStandardCommandPanel(
            this, SwingConstants.HORIZONTAL, true);
      defCommandPanel.setAlignmentX(1.0f);    //right align      
      m_okButton = defCommandPanel.getOkButton();
     
      m_customButton = new JButton(m_applet.getResourceString(
         getClass(), "@Customize..."));
      m_customButton.setMnemonic(PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "Customize...", 'u'));
      Dimension stdButton = (new UTFixedButton("foo")).getPreferredSize();
      m_customButton.setMaximumSize(
            new Dimension(200, (int) stdButton.getHeight()));

      m_customButton.addActionListener(new ActionListener()
      {
         //Applies default options to the dialog.
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            PSFieldSelectionEditorDialog dlg = new PSFieldSelectionEditorDialog(
               PSSearchDialog.this, m_search, m_fieldCatalog);
            dlg.setUseExternalSearchEngine(m_isEngineAvailable);
            dlg.setUseFocusHighlight(focusColor);
            dlg.setVisible(true);
            if (dlg.isOk())
            {
               PSSearchFieldEditor curFieldEditorCtrl = m_fieldEditorCtrl;
               m_fieldEditorCtrl = new PSSearchFieldEditor(m_search.getFields(),
                  m_searchFieldFilterMap, 
                  m_remoteCataloger.getRemoteRequester(), m_feprops, 
                  m_fieldCatalog);

               setFieldEditorSize();

               m_searchPanelPanel.remove(curFieldEditorCtrl);
               m_searchPanelPanel.add(m_fieldEditorCtrl, BorderLayout.CENTER);
               pack();
               center();
            }
         }
      });
      // Set initial visibility
      m_customButton.setVisible(false);

      m_advBtnText = m_applet.getResourceString(
         getClass().getName() + "@Advanced >>");
      m_simpBtnText = m_applet.getResourceString(
         getClass().getName() + "@<< Simple");

      m_advBtnMnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "Advanced >>", 'A');
      m_simpBtnMnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "<< Simple", 'S');
      
      m_advButton = new JButton(m_advSearch ? m_simpBtnText : m_advBtnText);
      m_advButton.setMnemonic(
         m_advSearch ? m_advBtnMnemonic : m_simpBtnMnemonic);
      m_advButton.setMaximumSize(
              new Dimension(200, (int) stdButton.getHeight()));

      
      m_advButton.setEnabled(true);
      m_advButton.addActionListener(new ActionListener()
      {
         // handle click of the adv button
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            // if in simple mode, expand dialog, switch label to simple, add
            // customize button
            if (!m_advSearch)
            {
               m_advSearch = true;
               // switch label
               m_advButton.setText(m_simpBtnText);
               // switch mnemonic
               m_advButton.setMnemonic(m_simpBtnMnemonic);
               // show cust btn
               if (m_search.isUserCustomizable())
                   m_customButton.setVisible(true);
               // check on ok button
               checkOKState();
               // show search panel
               populateAdvancedPanel();
            }
            
            // else if in advance mode, shrink dialog, switch label to adv,
            // remove customize button
            else
            {
               m_advSearch = false;
               // switch label
               m_advButton.setText(m_advBtnText);
               // switch mnemonic
               m_advButton.setMnemonic(m_advBtnMnemonic);
               // show cust btn
               m_customButton.setVisible(false);
               // check on ok button
               checkOKState();
               // hide search panel
               clearAdvancedPanel();
            }
            pack();
            ensureVisible();
            m_searchSimplePanel.focusQueryBox();
         }
      });

      // add listener on query field to check OK button state
      m_ftQuery.addKeyListener(new KeyListener()
      {
         public void keyTyped(@SuppressWarnings("unused") KeyEvent e){}

         public void keyPressed(@SuppressWarnings("unused") KeyEvent e){}

         public void keyReleased(@SuppressWarnings("unused") KeyEvent e)
         {
            if (m_advancedPropsPanel.getSynonymExpansionControl().isSelected())
            {
               String key = String.valueOf(e.getKeyChar());
               if (!validateForSynonymExp(key))
               {
                  String ftQuery = m_ftQuery.getText();
                  if (ftQuery != null)
                     ftQuery = ftQuery.substring(0, ftQuery.length() - 1);
                  m_ftQuery.setText(ftQuery);
                  return;
               }
            }
            
            checkOKState();
         }         
      });
      
      JPanel cmdButtonsPanel = new JPanel();
      cmdButtonsPanel.setLayout(new BorderLayout());
      JPanel advButtonPanel = new JPanel();
      advButtonPanel.setLayout(new BoxLayout(advButtonPanel, BoxLayout.X_AXIS));
      advButtonPanel.add(m_advButton);
      advButtonPanel.add(Box.createHorizontalStrut(5));
      advButtonPanel.add(m_customButton);
      advButtonPanel.add(Box.createHorizontalStrut(5));
      
      //add button only if required
      if(m_search.isUserCustomizable())
      {
         if (!m_isEngineAvailable || m_advSearch)
            m_customButton.setVisible(true);
         else
            m_customButton.setVisible(false);
      }
      
      cmdButtonsPanel.add(defCommandPanel, BorderLayout.EAST);
      
      // if using engine, checkOKState must be called after the next line
      m_searchSimplePanel.updateData(false, m_search);
      m_advancedPropsPanel.updateData(false, m_search);
      if (m_folderPath != null)
      {
         boolean includeSubFolders = true;
         String tmp = m_search.getProperty(PSSearch.PROP_FOLDER_PATH_RECURSE);
         if (tmp != null && tmp.trim().equalsIgnoreCase("false"))
            includeSubFolders = false;
         m_includeSubfoldersCtrl.setSelected(includeSubFolders);
      }

      cmdButtonsPanel.add(advButtonPanel, BorderLayout.WEST);
      
      if(m_isEngineAvailable)
      {
         m_advButton.setVisible(true);
         checkOKState();
      }
      else
         m_advButton.setVisible(false);
      
      // if using engine, checkOKState must be called after the next line
      m_searchSimplePanel.updateData(false, m_search);
      
      // wrap command button panel to force it to bottom
      JPanel bottomPanel = new JPanel(new BorderLayout());
      JPanel filler = new JPanel();
      filler.setMinimumSize(new Dimension(1,1)); // keep center from expanding
      bottomPanel.add(filler, BorderLayout.CENTER);
      bottomPanel.add(cmdButtonsPanel, BorderLayout.SOUTH);
      m_mainPanel.add(bottomPanel);
      setContentPane(m_mainPanel);
      border.addToAllNavigable(m_mainPanel);
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * Used to to add the all controls to the advanced panel. The dialog works
    * by leaving the panel on the dialog and adding/removing all controls.
    * <p>The equivalent method {@link #clearAdvancedPanel()} must match what
    * this method does in reverse.
    * <p>The panel being modified is {@link #m_searchPanelPanel}.
    *
    */
   private void populateAdvancedPanel()
   {
      m_searchPanelPanel.add(m_fieldEditorCtrl, BorderLayout.CENTER);
      
      /* only add if the panel contains any controls. If FTS is disabled and the
       * db is not case-sensitive, it will be empty. If that is the case, for
       * some reason the whole dialog comes up empty (a Java bug)
       */
      if (m_advancedPropsPanel.getRowCount() > 0)
         m_searchPanelPanel.add(m_advancedPropsPanel, BorderLayout.SOUTH);
   }
   
   /**
    * Removes all controls on the advanced panel. See 
    * {@link #populateAdvancedPanel()} for a detailed description.
    *
    */
   private void clearAdvancedPanel()
   {
      m_searchPanelPanel.remove(m_advancedPropsPanel);
      m_searchPanelPanel.remove(m_fieldEditorCtrl);
   }
   
   /**
    * Ensures that the entire dialog is visible on the screen, and if not, moves
    * it only to the extent that is required.  Assumes that the size of the
    * dialog will fit on the screen.
    */
   private void ensureVisible()
   {
      if (!isVisible())
         return;
      
      Point loc = getLocationOnScreen();
      Dimension d = getPreferredSize();
      
      // get screen size adjusted to exclude the taskbar
      Dimension screen = getScreenSize(); 
      
      // check the top left and bottom right corners and ensure the dialog is on
      // the screen.
      Point topLeft = new Point(loc);
      Point bottomRight = new Point(loc.x + d.width, loc.y + d.height);
      
      int newX = loc.x;
      int newY = loc.y;
      if (topLeft.x < 0)
         newX = newX + (0 - topLeft.x);
      if (topLeft.y < 0)
         newY = newY + (0 - topLeft.y);
      if (bottomRight.x > screen.width)
         newX = newX - (bottomRight.x - screen.width);
      if (bottomRight.y > screen.height)
         newY = newY - (bottomRight.y - screen.height);
      
      setLocation(newX, newY);
   }

   /**
    * Get the screen size, adjusted to exclude the taskbar or any other 
    * "insets".
    * 
    * @return The size, never <code>null</code>.
    */
   private Dimension getScreenSize()
   {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
         getGraphicsConfiguration());
      screen.setSize(screen.width - (insets.left + insets.right), screen.height 
         - (insets.top + insets.bottom));
      return screen;
   }

   /**
    * Override base class to limit dialog size to 100 pixel less than the screen
    * width and height.
    */
   public Dimension getPreferredSize()
   {
      Dimension d = super.getPreferredSize();
      Dimension screen = getScreenSize();
   
      int w = screen.width - 100;
      int h = screen.height - 100;
      
      Dimension newSize = new Dimension(d.width > w ? w : d.width, 
         d.height > h ? h : d.height);
         
      return newSize;
   }
  

   /**
    * Maintains the enabled state of the OK button based on the contents of the 
    * fts query field and the search mode.  If in simple mode, the OK button
    * is only enabled if the fts query field is not empty.
    */
   private void checkOKState()
   {
      boolean state = true;
      if (!m_advSearch && m_ftQuery.getText().trim().length() == 0)
         state = false;
      
      m_okButton.setEnabled(state);
   }

   /**
    * Calculates the preferred size of the search panel and adds the scroll bar
    * width or height appropriately and sets the preferred size to the panel.
    * Limits the height of the panel to <code>MAX_DEFAULT_HEIGHT</code>
    */
   private void setFieldEditorSize()
   {
      Dimension size = m_fieldEditorCtrl.getPreferredSize();
      int width = size.width +
         (new JScrollBar(JScrollBar.VERTICAL)).getPreferredSize().width;
      int height = size.height +
         (new JScrollBar(JScrollBar.HORIZONTAL)).getPreferredSize().height;

      if(height > MAX_DEFAULT_HEIGHT)
         height = MAX_DEFAULT_HEIGHT; //Limit to this height by default

      m_fieldEditorCtrl.setPreferredSize(new Dimension(width, height));
   }

   /**
    * Method called when user clicks the OK button of the dialog box. All the
    * field values are read from the UI components and attached to the search
    * object after validating.
    */
   @Override
   public void onOk()
   {
      //Validate the max results value
      if(!m_searchSimplePanel.onValidateData(false) 
            || !m_advancedPropsPanel.onValidateData(false))
      {
         return;
      }
      
      //Validate the fts query field value
      String ftQueryText = m_ftQuery.getText();
      if (ftQueryText != null)
      {
         ftQueryText = ftQueryText.trim();
         if (ftQueryText.startsWith("*") || ftQueryText.startsWith("?"))
         {
            String message = m_applet.getResourceString(
                  getClass().getName() +
                  "@The following characters are not allowed as the first " +
                  "character of a full text search query: *, ?");
            int selection = showErrorDialog(message);
            return;
         }
         
         if (m_advancedPropsPanel.getSynonymExpansionControl().isSelected())
         {
            if (!validateForSynonymExp(ftQueryText))
               return;
         }
      }
            
      //save the fields in search panel, that validates the field data and
      //updates the data objects.
      if(!m_fieldEditorCtrl.save())
         return;

      Iterator iter = m_fieldEditorCtrl.getFields();
      String missingFieldNames = "";
      /*If isRestrictToUserCommunity is true then check whether all fields
      belongs to user community or not. If not warn the user about it.
      */
      if(isRestrictToUserCommunity())
      {
         List<PSSearchField> missingFields = new ArrayList<PSSearchField>();
         while (iter.hasNext())
         {
            PSSearchField field = (PSSearchField) iter.next();
            /**
             * @todo The field need to be checked in m_fieldCatalog.getAll(),
             * but some reason it is not working that need to be fixed.
             */
            if(!(m_fieldCatalog.getSystemMap().containsKey(field.getFieldName()) ||
               m_fieldCatalog.getSharedMap().containsKey(field.getFieldName()) ||
               m_fieldCatalog.getLocalMap().containsKey(field.getFieldName())))
            {
               missingFieldNames += field.getDisplayName() + "\n";
               missingFields.add(field);
            }
         }
         if(missingFields.size() > 0)
         {
            /*Seems there are some fields for which user has no access through his
            current community*/
            String message1 = m_applet.getResourceString(
               getClass().getName() +
               "@The following fields do not belong to your current community.");

            String message2 = m_applet.getResourceString(
               getClass().getName() +
               "@Do you want to remove the fields and save the search " +
               "to your community?");

            String message = message1 + "\n" + missingFieldNames + "\n" + 
                message2;
            int selection = showError(message);
            //If OK then remove the missing fields and save the search
            //to user community
            if (selection == JOptionPane.YES_OPTION)
            {
               m_search.removeFields(missingFields.iterator());
               m_search.removeProperty(PSSearch.PROP_COMMUNITY,null);
               m_search.setShowTo(PSSearch.SHOW_TO_COMMUNITY, String.valueOf(
                     m_applet.getUserInfo().getCommunityId()));
            }
            else
            {
               return;
            }
         }
         else
         {
            //If this search has been saved previously to all communities
            //If yes then warn the user and save the search to current community
            if(m_searchNode.getType().equalsIgnoreCase(PSNode.TYPE_SAVE_SRCH) &&
               m_search.doesPropertyHaveValue(PSSearch.PROP_COMMUNITY,
               PSSearch.PROP_COMMUNITY_ALL))
            {
               String message1 = m_applet.getResourceString(
                  getClass(),
                  "@This search has been saved previously to all communities.");
      
               String message2 = m_applet.getResourceString(
                  getClass(), "@With the modified server settings searches "
                  + "can not be shared accross the communities.");

               String message3 = m_applet.getResourceString(
                  getClass(), 
                  "@Do you want to save the search to your community?");

               String message = message1 + "\n" + message2 + "\n" + message3;
               int selection = showError(message);
               //If Yes then save the search to user community
               if (selection == JOptionPane.YES_OPTION)
               {
                  m_search.removeProperty(PSSearch.PROP_COMMUNITY,null);
                  m_search.setShowTo(PSSearch.SHOW_TO_COMMUNITY, String.valueOf(
                        m_applet.getUserInfo().getCommunityId()));
               }
               else
               {
                  return;
               }
            }
            m_search.setFields(m_fieldEditorCtrl.getFields());
         }
      }
      else
      {
         m_search.setFields(m_fieldEditorCtrl.getFields());
      }
      // Call search simple panel to update the data
      m_searchSimplePanel.updateData(true, m_search);
      m_advancedPropsPanel.updateData(true, m_search);
      if (m_folderPath != null && m_folderPath.trim().length() > 0)
      {
         m_search.setProperty(PSSearch.PROP_FOLDER_PATH, m_folderPath);
         m_search.setProperty(PSSearch.PROP_FOLDER_PATH_RECURSE,
               m_includeSubfoldersCtrl.isSelected() ? "true" : "false");
      }
      else
      {
         m_search.removeProperty(PSSearch.PROP_FOLDER_PATH, null);
         m_search.removeProperty(PSSearch.PROP_FOLDER_PATH_RECURSE, null);
      }
      m_searchNode.setDisplayFormatId(m_search.getDisplayFormatId());
      
      PSProperties props = m_searchNode.getProperties();
      props.setProperty(IPSConstants.PROPERTY_SEARCHID,
         PSSearchViewCatalog.getSearchId(m_search));

      // remember if simple or advanced
      m_search.setProperty(PSSearch.PROP_SEARCH_MODE, m_advSearch ? 
         PSSearch.SEARCH_MODE_ADVANCED : PSSearch.SEARCH_MODE_SIMPLE);
      
      if(m_newSearch)
      {
         m_mgr.setEmptySearch(m_search);
      }
      
      super.onOk();
   }

   /**
    * This method validates a given fts query string for use with synonym
    * expansion.  An fts query is valid for synonym expansion if it does not
    * contain any characters which are special as part of a lucene query, see
    * {@link #ms_specialChars}.  If any such characters are found, they are
    * listed in an error dialog along with an appropriate message.
    * 
    * @param ftQueryText the fts query string to be validated, assumed not
    * <code>null</code>
    * 
    * @return <code>true</code> if the query string is valid for use with
    * synonym expansion, <code>false</code> otherwise.
    */
   private boolean validateForSynonymExp(String ftQueryText)
   {
      String spChars = null;
      for (String specialChar : ms_specialChars)
      {
         if (ftQueryText.indexOf(specialChar) != -1)
         {
            if (spChars == null)
               spChars = specialChar;
            else
               spChars += ", " + specialChar;
         }
      }
      
      if (spChars != null)
      {
         String message = m_applet.getResourceString(
               getClass().getName() +
               "@The following characters are not allowed as part of a " +
               "full text search query when synonym expansion is " +
               "enabled:") + " " + spChars;
         
         int selection = showErrorDialog(message);
         return false;
      }
      
      return true;
   }
   
   /**
    * Gets the search modified by this dialog. Should be called after the dialog
    * returns control to the caller when user clicks OK.
    *
    * @return the search, never <code>null</code>
    */
   public PSSearch getSearch()
   {
      return m_search;
   }

   /**
    * This is a convenient method which returns <code>true</code> if
    * RestrictSearchFieldsToUserCommunity property exists in the server.property
    * and its value is yes and if the search type is either user search or
    * related content search or a new search. Otherwise returns 
    * <code>false</code>.
    *
    * @return <code>true</code> or <code>false> based on the search type and
    * RestrictSearchFieldsToUserCommunity flag.
    */
   public boolean isRestrictToUserCommunity()
   {
      String part = m_search.getLocator().getPart();
      if(part == null)
         new IllegalStateException("Invalid locator for the search object");

      if (m_applet.isContentRestrict()
            && (part.equals(PSSearchViewCatalog.EMPTY_SEARCHID) || m_search
                  .isUserSearch() || m_search.isRCSearch()))
      {
         return true;
      }
      return false;
   }

   /**
    * Displays the supplied message in the error dialog box and returns the
    * user selection.
    * @param message The message to be displayed in the error dialog box.
    * @return The user selection on the error dialog box
    */
   private int showError(String message)
   {
      JOptionPane option = new JOptionPane(message,
            JOptionPane.ERROR_MESSAGE,  JOptionPane.YES_NO_OPTION);
      JDialog dlg = option.createDialog(null,
            m_applet.getResourceString(
                  PSContentExplorerStatusDialog.class, "Error"));
      dlg.setVisible(true);
      return Integer.parseInt(option.getValue().toString());
   }

   /**
    * Displays the supplied message in the error dialog box and returns the
    * user selection.  The dialog box will contain one 'OK' button.
    * 
    * @param message The message to be displayed in the error dialog box.
    * @return The user selection on the error dialog box
    */
   private int showErrorDialog(String message)
   {
      JOptionPane option = new JOptionPane(message,
            JOptionPane.ERROR_MESSAGE);
      JDialog dlg = option.createDialog(null,
            m_applet.getResourceString(
                  PSContentExplorerStatusDialog.class, "Error"));
      dlg.setVisible(true);
      return Integer.parseInt(option.getValue().toString());
   }

   /**
    * We keep this around so we can replace the search fields sub-panel when
    * it is customized. Never <code>null</code>.
    */
   private JPanel m_mainPanel = new JPanel();

   /**
    * Reference to the action manager that handles the loading and saving the
    * searches and views, initialized in the ctor and never <code>null</code> or
    * modified after that.
    */
   private PSSearchViewActionManager m_mgr = null;

   /**
    * The search object associated with this dialog box. This is always going to
    * be the one attached to the content explorer node this dialog initialized 
    * with .
    * Never <code>null</code> after initailization of the dialog box.
    */
   private PSSearch m_search = null;

   /**
    * The Content Explorer search node, Never <code>null</code> after
    * initailization of the dialog box.
    */
   private PSNode m_searchNode = null;

   /**
    * The Content Explorer search node, Never <code>null</code> after
    * initailization of the dialog box.
    */
   private Map m_searchFieldFilterMap = null;

   /**
    * This dynamic fields panel
    */
   private PSSearchFieldEditor m_fieldEditorCtrl = null;

   /**
    * The content editor field cataloger to be supplied to field selection
    * dialog to get the available fields, initialized in the ctor and never
    * <code>null</code> or modified after that.
    */
   private PSContentEditorFieldCataloger m_fieldCatalog = null;

   /**
    * When creating or adding/removing search fields, if the entire panel is
    * taller than this height, we limit it to this height. In pixels.
    */
   private static int MAX_DEFAULT_HEIGHT = 400;

   /**
    * The text area to enter the full text search query. 
    * Never <code>null</code> after initialization.
    */
   private JTextArea m_ftQuery = null;

   /**
    * Stores the optional folder path search property. Either <code>null</code>,
    * or a non-empty path. Set in ctor, then never modified. <code>null</code>
    * means the search is not limited to folder scope. See ctor description
    * for more details.
    */
   private String m_folderPath = null;

   /**
    * The button that launches field customization editor.
    * Never <code>null</code> after initialization.
    */
   private JButton m_customButton = null;

   /**
    * Holds a reference to the RemoteCataloger. Initilized in Ctor.,
    * never <code>null</code> after that.
    */
   private PSRemoteCataloger m_remoteCataloger = null;

   /**
    * This can be used to pass the properties to the PSSearchFieldEditor
    * Initialized to empty <code>Properties</code> object. Any properties that
    * are to be added are done so in the ctor.
    */
   private Properties m_feprops = new Properties();

   /**
    * Flag indicating the search is newly created
    */
   private boolean m_newSearch = false;

   /**
    * Flag indicating the search is advanced search.
    */
   private boolean m_advSearch = false;

   /**
    * The button that launches advanced search fields.
    * Never <code>null</code> after initialization.
    */
   private JButton m_advButton = null;

   /**
    * Text to display for the button to expand this dialog into advanced mode,
    * never <code>null</code>, empty, or modified after construction.
    */
   private String m_advBtnText = null;

   /**
    * Text to display for the button to collapse this dialog into simple mode,
    * never <code>null</code>, empty, or modified after construction.
    */
   private String m_simpBtnText = null;

   /**
    * The mnemonic key used if the advanced button is shown.
    */
   private char m_advBtnMnemonic = 0;

   /**
    * The mnemonic key used if the simple button is shown.
    */
   private char m_simpBtnMnemonic = 0;

   /**
    * Panel that acts only as a container for the {@link #m_fieldEditorCtrl} so that
    * it can be added and removed dynamically as the search mode changes from
    * simple to advanced and back.  Never <code>null</code> after construction.
    */
   private JPanel m_searchPanelPanel = null;

   /**
    * The control for toggling the 'include subfolders' property. Never 
    * <code>null</code>.
    */
   private JCheckBox m_includeSubfoldersCtrl = new JCheckBox();

   /**
    * The ok button for the dialog, never <code>null</code> after construction.
    */
   private JButton m_okButton = null;

   /**
    * Search configuration, initialized by ctor, never <code>null</code> after
    * that.
    */
   private PSSearchConfig m_searchConfig;
   
   /**
    * A reference to the applet never <code>null</code>
    */
   private PSContentExplorerApplet m_applet = null;

   /**
    * It is a panel that handles all the components that need to be displayed
    * when search is in simple format. 
    * Never <code>null</code> after construction.
    */
   PSSearchSimplePanel m_searchSimplePanel = null;

   /**
    * This panel contains the search controls other than the field list in the
    * advanced section of the dialog. Never <code>null</code> after 
    * construction.
    */
   PSSearchAdvancedPanel m_advancedPropsPanel = null; 

   /**
    * Flag indicating that the full text engine is available.
    * Initialized in {@link #initDialog()}
    */
   private boolean m_isEngineAvailable;

   /**
    * The list of characters which are considered "special" as part of the
    * lucene query syntax.  Synonym expansion may not be used if any of these
    * characters are included in a full text search query. 
    */
   private static final List<String> ms_specialChars = new ArrayList<String>();

   static
   {
      ms_specialChars.add("+");
      ms_specialChars.add("-");
      ms_specialChars.add("&&");
      ms_specialChars.add("||");
      ms_specialChars.add("!");
      ms_specialChars.add("(");
      ms_specialChars.add(")");
      ms_specialChars.add("{");
      ms_specialChars.add("}");
      ms_specialChars.add("[");
      ms_specialChars.add("]");
      ms_specialChars.add("^");
      ms_specialChars.add("\"");
      ms_specialChars.add("~");
      ms_specialChars.add("*");
      ms_specialChars.add("?");
      ms_specialChars.add(":");
      ms_specialChars.add("\\");
   }
}
