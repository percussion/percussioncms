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

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.objectstore.PSRelationshipInfo;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.PSNameLabel;
import com.percussion.guitools.PSPathLabel;
import org.apache.commons.lang.StringUtils;

import javax.accessibility.AccessibleContext;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The top-level action bar for the applet.
 */
public class PSActionBar extends JPanel implements IPSSelectionListener,
   ItemListener, IPSSearchListener
{
   /**
    * Constructs the action bar with supplied view.
    *
    * @param applet the applet that provides action manager to handle actions
    * and the parameters, may not be <code>null</code>
    * @param menu The additional top-level menu it has to show, if <code>null
    * </code>, this is not added to this menu bar, otherwise it is added.
    * @param view The view of the applet this action bar currently representing,
    * must be one of the <code>PSUiMode.TYPE_VIEW_xxx</code> values.
    * @param relationships the set of available relationships for dependency
    * tree view, may not be <code>null</code> or empty if the supplied view is
    * <code>PSUiMode.TYPE_VIEW_DT</code>.
    * @param listener the listener to add, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSActionBar(PSContentExplorerApplet applet, JMenuBar menu, String view,
                      PSRelationshipInfoSet relationships, IPSViewChangeListener listener)
   {
      if(applet == null)
         throw new IllegalArgumentException("applet may not be null.");

      if(!PSUiMode.isValidView(view))
         throw new IllegalArgumentException("view is not valid");

      if(view.equals(PSUiMode.TYPE_VIEW_DT) &&
         (relationships == null || !relationships.getComponents().hasNext()))
      {
         throw new IllegalArgumentException(
            "relationships may not be null or empty for " +
            PSUiMode.TYPE_VIEW_DT + " view");
      }
      
      m_applet = applet;
      m_view = view;
      m_pathLabelField.setFocusable(true);
      m_resultsLabel.setFocusable(true);
      m_resultsLabel.setEditable(false);
      m_resultsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      m_resultsLabel.setMinimumSize(new Dimension(0, 0));
      m_resultsLabel.setBorder(null);
      if (listener!=null)
         addViewChangeListener(listener);

      init(menu, relationships);      
   }

   /**
    * Initializes the action bar for different views.
    * <p>
    * Adds the menus of the supplied menu bar to this menu bar and a tool bar to
    * display content path in <code>PSUiMode.TYPE_VIEW_CX</code> and <code>
    * PSUiMode.TYPE_VIEW_IA</code> views.
    * <p>
    * In case of <code>PSUiMode.TYPE_VIEW_DT</code> view a drop-down list with
    * available relationships is added in addition to menu. Content Item is
    * shown as label in this action bar. Adds the selection listener for
    * drop-down list to inform its view change listeners.
    *
    * @param menu The additional top-level menu it has to show, if <code>null
    * </code>, this is not added to this menu bar, otherwise it is added.
    * @param relationshipSet the set of available relationships for dependency
    * tree view, assumed not <code>null</code> or empty if the current view is
    * <code>PSUiMode.TYPE_VIEW_DT</code>.
    */
   @SuppressWarnings("unchecked")
   private void init(JMenuBar menu, PSRelationshipInfoSet relationshipSet)
   {
      
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
            
      if(m_view.equals(PSUiMode.TYPE_VIEW_DT))
      {
         m_contentItem = new PSLocator(
            m_applet.getParameter(PSContentExplorerConstants.PARAM_CONTENTID),
            m_applet.getParameter(PSContentExplorerConstants.PARAM_REVISIONID));

         JPanel rsPanel = new JPanel();
         rsPanel.setLayout(new BoxLayout(rsPanel, BoxLayout.X_AXIS));
         JLabel rsLabel = new JLabel(
               m_applet.getResourceString(
               getClass().getName() + "@Relationship:"),
            SwingConstants.LEFT);
         rsLabel.setFocusable(true);    
         char mnemonic = PSContentExplorerApplet.getResourceMnemonic(
            getClass(), "Relationship:", 'R');
         
         rsPanel.add(rsLabel);
         rsPanel.add(makeFillerOpaque(Box.createHorizontalStrut(5)));
         
         m_rsCombo = new JComboBox();
         rsLabel.setLabelFor(m_rsCombo); // Associate label with combobox
         if (mnemonic != 0) {
            rsLabel.setDisplayedMnemonic(mnemonic);
         } 
         focusBorder.addToComponent(m_rsCombo, false);
         m_rsCombo.setRenderer(new DefaultListCellRenderer()
         {
            @Override
            public Component getListCellRendererComponent(
               JList list,
               Object value,
               int index,
               boolean isSelected,
               boolean cellHasFocus
               )
            {
               super.getListCellRendererComponent(list, value, index,
                  isSelected, cellHasFocus);
               if(value instanceof PSRelationshipInfo)
               {
                  PSRelationshipInfo relInfo = (PSRelationshipInfo)value;
                  setText(m_applet.getResourceString(
                     "psx." + PSConfigurationFactory.RELATIONSHIPS_CFG + "." +
                     relInfo.getName() + "@" + relInfo.getLabel()));
               }
               return this;
            }
         }
         );
         m_rsCombo.setPreferredSize(new Dimension(180,20));
         m_rsCombo.setMinimumSize(new Dimension(180,20));
         m_rsCombo.setMaximumSize(new Dimension(180,20));
         m_rsCombo.addItemListener(this);
            
              
         Iterator<PSRelationshipInfo> relationships = 
            relationshipSet.getComponents();
         List<PSRelationshipInfo> orderedRels = 
            new ArrayList<PSRelationshipInfo>();
         if(!relationships.hasNext())
            throw new IllegalStateException("No relationships found");
         while (relationships.hasNext())
         {
            orderedRels.add(relationships.next());
         }
         Collections.sort(orderedRels, new Comparator<PSRelationshipInfo>()
         {
            /**
             * Order by label, ascending.
             */
            public int compare(PSRelationshipInfo o1, PSRelationshipInfo o2)
            {
               String l1 = o1.getLabel();
               String l2 = o2.getLabel();
               return l1.compareToIgnoreCase(l2);
            }
         });

         String relName = m_applet.getParameter(
               PSContentExplorerConstants.PARAM_RELATIONSHIP_NAME);
         if (relName == null)
            relName = StringUtils.EMPTY;
         relationships = orderedRels.iterator();
         while(relationships.hasNext())
         {
            PSRelationshipInfo relInfo = relationships.next();
                        
            m_rsCombo.addItem(relInfo);
         
            if (relInfo.getName().equalsIgnoreCase(relName))
            {
               m_rsCombo.setSelectedItem(relInfo);
            }
         }
                  
         rsPanel.add(m_rsCombo);

         add(rsPanel);

         if(menu != null)
         {
            menu.setAlignmentY(CENTER_ALIGNMENT);
            menu.setBorderPainted(false);
            add(menu);
         }

         JPanel labelPanel = new JPanel();
         labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
         labelPanel.setAlignmentY(CENTER_ALIGNMENT);

         JLabel label = new JLabel(
               m_applet.getResourceString(
               getClass().getName() + "@Content Item:"));
         label.setIcon(PSImageIconLoader.loadIcon(
            CONTENT_PATH_SEPARATOR_ICON, false, m_applet));
         label.setFocusable(true);
         labelPanel.add(label);
         labelPanel.add(makeFillerOpaque(Box.createHorizontalStrut(5)));
         
         PSNameLabel nameLabel = new PSNameLabel();
         nameLabel.setText(
               m_applet.getParameter(PSContentExplorerConstants.PARAM_ITEM_TITLE));
         
         label.setLabelFor(nameLabel);
         
         labelPanel.add(nameLabel);
         labelPanel.add(makeFillerOpaque(Box.createHorizontalGlue()));
         add(makeFillerOpaque(Box.createHorizontalStrut(15)));
         add(labelPanel);
         
      }
      else
      {
         if(menu != null)
         {
            menu.setAlignmentY(CENTER_ALIGNMENT);
            menu.setBorderPainted(false);            
            add(menu);
            
         }

         JPanel labelPanel = new JPanel();
         labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
         labelPanel.setAlignmentY(CENTER_ALIGNMENT);
         labelPanel.setVisible(true);
         labelPanel.setFocusTraversalKeysEnabled(true);
       
         
         JLabel label = new JLabel(
               m_applet.getResourceString(
            getClass().getName() + "@Content Path:") + "  ");
         label.setIcon(PSImageIconLoader.loadIcon(
            CONTENT_PATH_SEPARATOR_ICON, false, m_applet));
  
         labelPanel.add(label);

         //Make the path text field as not edit able We need the text field 
         // rather the label, so that we can copy from that field
       
         m_pathLabelField.setOpaque(false);
        
         
         labelPanel.add(m_pathLabelField);
         label.setLabelFor(m_pathLabelField);
         
         
         // add refresh button
         Icon refreshIcon = PSImageIconLoader.loadIcon(REFRESH_ICON, false, m_applet);          
         m_refreshButton = new JButton(refreshIcon);
         Dimension buttonSize = new Dimension(refreshIcon.getIconWidth() + 5, 
         refreshIcon.getIconHeight() + 5);
         m_refreshButton.setPreferredSize(buttonSize);
         m_refreshButton.setMaximumSize(buttonSize);
         m_refreshButton.setMinimumSize(buttonSize);
         m_refreshButton.setVisible(true);
         m_refreshButton.setBorder(focusBorder);
         m_refreshButton.getAccessibleContext().setAccessibleName(
            "Click to refresh selected node in the navigation pane");
         m_refreshButton.getAccessibleContext().setAccessibleDescription(
               "Click to refresh selected node in the navigation pane description");
         final PSMenuAction refreshAction = new PSMenuAction(
            IPSConstants.ACTION_REFRESH, "refresh");
         String tooltip = m_applet.getResourceString(getClass(),
            "@Refresh View");
         m_refreshButton.setToolTipText(tooltip);
         m_refreshButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(
                  @SuppressWarnings("unused") ActionEvent e)
            {
               if (m_navSelection != null)
                  m_applet.getActionManager().executeAction(refreshAction, 
                     m_navSelection);
            }
         });
         
         labelPanel.add(makeFillerOpaque(Box.createHorizontalGlue()));
         
         // add search results label
         m_resultsLabel.setVisible(false);
         labelPanel.add(m_resultsLabel);

         // add refresh button
         labelPanel.add(m_refreshButton);
         labelPanel.add(makeFillerOpaque(Box.createHorizontalStrut(10)));
         
         add(makeFillerOpaque(Box.createHorizontalStrut(15)));         
         add(labelPanel);
      }
   }
   
   /**
    * Helper method to make a Box Filler opaque which is needed
    * for Windows Vista to render background color through the filler.
    * @param filler the filler object may be <code>null</code> or not
    * a Filler component in which case it will just be passed back
    * untouched.
    * @return filler object, may be <code>null</code>.
    */
   private Component makeFillerOpaque(final Component filler)
   {
      if(filler != null && filler instanceof Box.Filler)
      {
         ((Box.Filler)filler).setOpaque(true);
      }
      return filler;
   }

   /**
    * Adds the view change listener to its list of listeners that are interested
    * in change of view.
    *
    * @param listener the listener to add, may not be <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public void addViewChangeListener(IPSViewChangeListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener may not be null.");

      m_viewChangeListeners.add(listener);
   }

   /**
    * Notification event that the current selection has changed in main view.
    * Updates the content path that is shown in this panel if there is
    * navigational selection change.
    *
    * @param selection The object that encapsulates the selection details, may
    * not be <code>null</code>
    */
   public void selectionChanged(PSSelection selection)
   {
      if (selection == null)
         throw new IllegalArgumentException("selection may not be null.");

      if (selection instanceof PSNavigationalSelection)
      {
         PSNavigationalSelection navSel = (PSNavigationalSelection) selection;
         m_pathLabelField.setText(navSel.getSelectionPath());
         m_navSelection = selection;
      }

      PSNode node = null;
      
      if (selection.getNodeListSize() == 1)
      {
         node = (PSNode) selection.getNodeList().next();
      }

      if (node != null && node.getSearchResultCount() >= 0)
      {
         m_resultsLabel.setVisible(true);
   
         String resultlabel =
               m_applet.getResourceString(
               getClass().getName() + "@ResultCount");
   
         MessageFormat fmt = new MessageFormat(resultlabel);
         Object args[] =
            new Object[] { new Integer(node.getSearchResultCount()),
               node.isTruncated() ? new Integer(1) : new Integer(0)};
         String label = fmt.format(args).toString();
         m_resultsLabel.setText(label);
       
      }
   }

   /**
    * Worker method for selection change event in 'Relationships' combo-box.
    * Gets the selected relationship and requests the server to get the
    * dependendencies of the content item for this relationship. Then informs
    * the listeners that are interested in the view change according to user
    * action.
    *
    * @param e the change event, assumed not <code>null</code> as this method is
    * called by Swing model.
    */
   public void itemStateChanged(ItemEvent e)
   {
      if(e.getStateChange() == ItemEvent.SELECTED)
      {
         String relationship = ((PSRelationshipInfo)e.getItem()).getName();

         PSProperties props = new PSProperties();
         props.setProperty(IPSConstants.PROPERTY_RELATIONSHIP, relationship);
         props.setProperty(
            IPSConstants.PROPERTY_CONTENTID,
            String.valueOf(m_contentItem.getId()));
         props.setProperty(
            IPSConstants.PROPERTY_REVISION,
            String.valueOf(m_contentItem.getRevision()));

         Iterator viewChangeListeners = m_viewChangeListeners.iterator();
         while(viewChangeListeners.hasNext())
         {
            ((IPSViewChangeListener)viewChangeListeners.next()).
               viewDataChanged(props);
         }
         
         // Update accessibility information on combobox
         AccessibleContext ctx = m_rsCombo.getAccessibleContext();
         ctx.setAccessibleName(relationship);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.cx.IPSSearchListener#searchReset()
    */
   public void searchReset()
   {
      m_resultsLabel.setVisible(false);
      m_resultsLabel.setText("");   
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.cx.IPSSearchListener#searchInitiated(com.percussion.cx.objectstore.PSNode)
    */
   public void searchInitiated(@SuppressWarnings("unused") PSNode node)
   {      
      String label = m_applet.getResourceString(
         getClass().getName() + "@SearchInProgress");
      Cursor waitcur = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);            
      m_applet.setCursor(waitcur);
      m_resultsLabel.setVisible(true);
      m_resultsLabel.setText(label);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cx.IPSSearchListener#searchCompleted(com.percussion.cx.objectstore.PSNode)
    */
   public void searchCompleted(PSNode node)
   {    
      Cursor normal = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      m_applet.setCursor(normal);  
      
      m_resultsLabel.setVisible(true);
   
      String resultlabel =
            m_applet.getResourceString(
            getClass().getName() + "@ResultCount");
   
      MessageFormat fmt = new MessageFormat(resultlabel);
      Object args[] =
         new Object[] { 
            new Integer(node.getSearchResultCount()),
            node.isTruncated() ? new Integer(1) : new Integer(0)
            };
      String label = fmt.format(args).toString();
      m_resultsLabel.setText(label);              
   }
   
   /**
    * The object that represents the content item whose dependencies are shown
    * in the view panel for the selected relationship in this action bar in
    * case of <code>PSUiMode.TYPE_VIEW_DT</code> view. Initialized in <code>
    * init(JMenuBar)</code> and never <code>null</code> or modified after that.
    */
   private PSLocator m_contentItem;

   /**
    * The container applet to get the applet context and its parameters,
    * initialized in the ctor and never <code>null</code> or modified after that.
    */
   PSContentExplorerApplet m_applet;

   /**
    * The current view of UI, initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_view;

   /**
    * The text field that displays the navigational selection in 'CX' view,
    * initialized to an empty text field and the text may be modified as
    * selection changes. Never <code>null</code> after initialization.
    */
   private JTextField m_pathLabelField = new PSPathLabel();

   /**
    * The label indicates the current state of a search. When a search is 
    * not underway, this component is hidden. When a search has been started
    * it states results pending. When a search has been completed it states
    * the number of results found.
    */
   private JTextField     m_resultsLabel = new JTextField();
   
   /**
    * The combo-box that displays available relationships for a content item
    * to choose the relationship to view its dependencies, initialized in the
    * <code>init(JMenuBar)</code> in case of <code>PSUiMode.TYPE_VIEW_DT</code>
    * and never <code>null</code> or modified after that.
    */
   private JComboBox m_rsCombo;

   /**
    * The list of listeners that are interested in their view changes when any
    * action is executed in this action bar. Listeners gets added through calls
    * to <code>addViewChangeListener(IPSViewChangeListener)</code>, never <code>
    * null</code>, may be empty.
    */
   private List m_viewChangeListeners = new ArrayList();

   /**
    * Name of the image file to separate the content path and the menu bar. 
    * Image is loaded on initialization and stored as part of UIManager.
    */
   public static final String CONTENT_PATH_SEPARATOR_ICON =
      "ContentPathSeparator";

   /**
    * Name of the image file used for the refresh button. Image
    * is loaded on initialization and stored as part of UIManager.
    */
   public static final String REFRESH_ICON = "Refresh";
      
   /**
    * Button to refresh view, initialized during construction, never 
    * <code>null</code> or modified after that.
    */
   private JButton m_refreshButton;
   
   /**
    * Current selection in the nav tree, intially <code>null</code>, modified by 
    * calls to {@link #selectionChanged(PSSelection)}. 
    */
   PSSelection m_navSelection = null;


}
