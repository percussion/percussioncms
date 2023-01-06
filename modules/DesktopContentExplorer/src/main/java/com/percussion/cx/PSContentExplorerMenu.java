/******************************************************************************
 *
 * [ PSContentExplorerMenu.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.utils.collections.PSIteratorUtils;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The class that should be used to construct menus, sub-menus and pop-up menus
 * represented by {@link com.percussion.cx.objectstore#PSAction}.
 * This class implements the Callable interface so the menu can be built on a 
 * separate thread when selection changes.
 */ 
public class PSContentExplorerMenu implements PopupMenuListener, Callable<PSContentExplorerMenu>
{
   static Logger log = Logger.getLogger(PSContentExplorerMenu.class);
   /**
    * Constructs the menu from suppled action. The action represents top-level
    * menu and its children represents either menuitem or sub-menu based on
    * their type.  This constructor builds the menu on construction.
    *
    * @param action the action represented by this menu, may not be <code>null
    * </code> and its type must be either one of <code>PSAction.TYPE_MENU</code>
    * or <code>PSAction.TYPE_CONTEXTMENU</code> values.
    * @param menuSource the source on which this menu should act on, may not be
    * <code>null</code>
    * @param actionManager the action manager to use to execute any action or to
    * get sub-menu, may not be <code>null</code>
    */
   public PSContentExplorerMenu(PSMenuAction action,
      PSMenuSource menuSource, PSActionManager actionManager)
   {
      this(action, menuSource, actionManager, true);
   }
   /**
    * Constructs the menu from suppled action. The action represents top-level
    * menu and its children represents either menuitem or sub-menu based on
    * their type. The menu may be build on construction or delayed with the build
    * option.
    *
    * @param action the action represented by this menu, may not be <code>null
    * </code> and its type must be either one of <code>PSAction.TYPE_MENU</code>
    * or <code>PSAction.TYPE_CONTEXTMENU</code> values.
    * @param menuSource the source on which this menu should act on, may not be
    * <code>null</code>
    * @param actionManager the action manager to use to execute any action or to
    * get sub-menu, may not be <code>null</code>
    * @param build whether we build the menu on construction
    */
   public PSContentExplorerMenu(PSMenuAction action,
      PSMenuSource menuSource, PSActionManager actionManager, boolean build)
   {
      if(action == null)
         throw new IllegalArgumentException("action may not be null.");

      if(!action.isMenu())
         throw new IllegalArgumentException("action must represent a menu");

      if(menuSource == null)
         throw new IllegalArgumentException("menuSource may not be null.");

      if(actionManager == null)
         throw new IllegalArgumentException("actionManager may not be null.");

      if (actionManager.getApplet() == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = actionManager.getApplet();
      
      m_menuSource = menuSource;
      m_actManager = actionManager;
      m_action = action;
      if (build) 
         buildMenu();
   }
   
   /**
    * Remove and rebuild all child menu entries
    */
   public void refreshChildMenus()
   {
      m_menu.removeAll();
      buildChildMenus();
   }
   
   /**
    * Build child menu entries for this menu
    */
   private void buildChildMenus()
   {
      JMenu currentMenu = m_menu;
      int count = 0;
      int max = getEstimatedMaxItems(currentMenu);
      String more = m_applet.getResourceString(
         getClass().getName() + "@MORE");
      
      Iterator children = m_action.getChildren();
      while(children.hasNext())
      {
         count++;
         if(count == max)
         {
            JMenu moreMenu = new JMenu(more);
            moreMenu.setFocusTraversalKeysEnabled(true);
            moreMenu.setFocusable(true);
            currentMenu.add(moreMenu);
            currentMenu = moreMenu;
            count = 1;
         }
         PSMenuAction childAction = (PSMenuAction)children.next();
         Action childMenuAction = buildAction(childAction);
         JComponent menu;
         if(childAction.isMenu())
         {
            menu = new JMenu(childMenuAction);
            if (m_menuSource.getSource()!=null)
               handleMenuAction(childAction, (JMenu)menu);
            ((JMenu)menu).getPopupMenu().addPopupMenuListener(this);
         }
         else
         {
            String menuChecked = childAction.getProperty(
               IPSConstants.PROPERTY_MENU_ITEM_CHECKED);
            if(menuChecked != null && menuChecked.equalsIgnoreCase(
               IPSConstants.PROPERTY_TRUE))
            {
               menu = new JCheckBoxMenuItem(childMenuAction);
               ((JCheckBoxMenuItem)menu).setState(true);
            }
            else
               menu = new JMenuItem(childMenuAction);
         }    
         menu.setFocusTraversalKeysEnabled(true);
         menu.setFocusable(true);
         menu.setFont(m_actManager.getApplet().getOptionsManager()
               .getDisplayOptions().getMenuFont());
         menu.setForeground(m_actManager.getApplet().getOptionsManager()
               .getDisplayOptions().getContextMenuForeGroundColor());         
         menu.setOpaque(true);
         currentMenu.add(menu);

      }
   }

   /**
    * Builds <code>JMenu</code> from this menu's action. Adds the menu elements
    * to this menu for the children of this action.
    */
   private void buildMenu()
   {
      
      Action menuAction = buildAction(m_action);
      m_menu = new JMenu(menuAction);
      m_menu.setFocusTraversalKeysEnabled(true);
      m_menu.setFocusable(true);
      m_menu.getPopupMenu().addPopupMenuListener(this);
      m_menu.setOpaque(true);
      
      buildChildMenus();
   }

   /**
    * Builds the <code>Action</code> for the supplied <code>PSAction</code>
    * element. Set the accelerator, mnemonic keys, tooltip and icon properties
    * on the <code>Action</code> object if available.
    *
    * @param action the action, assumed not <code>null</code>.
    *
    * @return the swing action, never <code>null</code>
    *
    * @throws RuntimeException if the icon url is specified and is unable to
    * load the icon
    */
   private Action buildAction(PSMenuAction action)
   {
      String menuLabel = action.getLabel();
      KeyStroke accKey = null;
      char mnem = 0;
      String tooltip = null;
      Icon icon = null;

      if(action.getProperties() != null)
      {
         PSProperties props = action.getProperties();
         String accelKey = props.getProperty(PSMenuAction.PROP_ACCEL_KEY);

         if(accelKey != null)
            accKey = KeyStroke.getKeyStroke(accelKey);

         String mnemChar = props.getProperty(PSMenuAction.PROP_MNEM_KEY);
         if(mnemChar != null && mnemChar.trim().length() > 0)
            mnem = mnemChar.charAt(0);

         tooltip = props.getProperty(PSMenuAction.PROP_SHORT_DESC);

         String url = props.getProperty(PSMenuAction.PROP_SMALL_ICON);
         try
         {
            if(url != null && url.trim().length() > 0)
               icon = m_actManager.getIcon(url);
         }
         catch(Exception ex)
         {
            throw new RuntimeException(
               "Error getting icon specified by menu url " +
               ex.getLocalizedMessage());
         }
      }

      AbstractAction menuAction = new AbstractAction(menuLabel, icon)
      {
         public void actionPerformed(ActionEvent e)
         {
            handleAction(e.getSource());
         }
      };

      if(mnem != 0)
         menuAction.putValue(Action.MNEMONIC_KEY, new Integer((int)mnem));

      if(accKey != null)
         menuAction.putValue(Action.ACCELERATOR_KEY, accKey);

      if(tooltip != null)
         menuAction.putValue(Action.SHORT_DESCRIPTION, tooltip);

      menuAction.putValue(ACTION_OBJECT, action);

      return menuAction;
   }

   /**
    * Delegated the menu-item action to the action manager.
    *
    * @param action the action to execute, assumed not <code>null</code>
    */
   private void handleMenuItemAction(PSMenuAction action)
   {
      m_actManager.executeAction(action, m_menuSource.getSource());
   }

   /**
    * Handles the menu action using action manager. Gets the sub-menu actions
    * from the action manager and then builds the sub-menu for it. If the menu
    * selected is a context-menu, the selection source will be the context
    * source of this menu, otherwise the selection source is the source of this
    * menu. After loading the sub-menu, checks and updates the context-submenu
    * state if it has any as specified in <code>
    * updateContextMenuState(MenuElement[])</code>.
    *
    * @param action the action to execute, assumed not <code>null</code>
    * @param source the source component of the action, assumed not <code>null
    * </code>
    */
   private void handleMenuAction(PSMenuAction action, JMenu source)
   {
      PSContentExplorerMenu subMenu = null;
      updateContextMenuState(source);
      if(action.isContextMenu())
      {
         PSSelection ctxSelection = m_menuSource.getContextSource();
         if(ctxSelection == null)
            throw new IllegalStateException(
               "No context selection avaialable to execute context-menu action");

         PSMenuAction ctxAction = m_actManager.getContextMenu(ctxSelection);
         if(ctxAction != null)
         {
            PSMenuSource menuSource = new PSMenuSource(ctxSelection);
            subMenu = new PSContentExplorerMenu(
               ctxAction, menuSource, m_actManager);
         }
      }
      else
      {
         PSMenuAction childAction = m_actManager.getContextMenu(
            action, m_menuSource.getSource());
         if(childAction != null)
         {
            subMenu = new PSContentExplorerMenu(
               childAction, m_menuSource, m_actManager);
         }
      }
      source.removeAll();
      if(subMenu != null && subMenu.getMenuElements().length > 0)
      {
         MenuElement[] menuElements = subMenu.getMenuElements();
         for (int i = 0; i < menuElements.length; i++)
            source.add(menuElements[i].getComponent());
      }
   }

   /**
    * Finds the context menu from the supplied list of menu elements and update
    * its state according to the current context selection. If there is no
    * context selection, makes the context menu invisible, otherwise makes it
    * visible and the label of menu represents the selection. If the context
    * selection represents multiple nodes, then it displays label as 'Group'.
    *
    * @param menuElements the list of menu elements, assumed not <code>null
    * </code>, if empty nothing is done.
    * Limitation: Currently, we are relying on the first character to be the 
    * mnemonic. Swing doesn't let you recycle among common mnemonics. This needs
    * to be addressed in the next rev by a customizable scheme.
    */
   private void updateContextMenuState(JMenu subMenu)
   {
      Action action = subMenu.getAction();
      if (action == null)
         return;
      PSMenuAction childAction = (PSMenuAction) action.getValue(ACTION_OBJECT);
      if (childAction.isContextMenu())
      {
         PSSelection contextSel = m_menuSource.getContextSource();
         if (contextSel == null)
         {
            subMenu.setVisible(false);
         }
         else
         {
            subMenu.setVisible(true);
            subMenu.setSelected(false);
            Iterator selNodes = contextSel.getNodeList();
            List selection = PSIteratorUtils.cloneList(selNodes);
            String label;
            if (selection.size() == 1)
               label = ((PSNode) selection.get(0)).getLabel();
            else
               label = m_applet.getResourceString(getClass().getName() + "@Group");

            subMenu.getAction().putValue(Action.NAME, label);
            char mnem = PSContentExplorerApplet.getResourceMnemonic(getClass(), "Group", 'G');
            subMenu.getAction().putValue(Action.MNEMONIC_KEY, new Integer(mnem));

         }
      }
   }

   /**
    * Handles the action specified by the source object.
    *
    * @param source the source of action, assumes it as an instance of <code>
    * JMenu</code> or <code>JMenuItem</code>
    */
   private void handleAction(Object source)
   {
      if(source instanceof JMenuItem)
      {         
         JMenuItem menuItem = (JMenuItem)source;
         PSMenuAction action =
            (PSMenuAction)menuItem.getAction().getValue(ACTION_OBJECT);
         handleMenuItemAction(action);
      }
   }
   
   /**
    * Allow for building of the menu in the background on a separate thread
    * @return This instance with the menu fully built
    */
   public PSContentExplorerMenu call() throws Exception
   {
      buildMenu();
      return this;
   }
   
   //nothing to implement
   public void popupMenuWillBecomeVisible(PopupMenuEvent e)
   {
   }

   //nothing to implement
   public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
   {
   }

   //nothing to implement
   public void popupMenuCanceled(PopupMenuEvent e)
   {

   }

   /**
    * Gets the menu component that represents this menu.
    *
    * @return menu, never <code>null</code>.
    */
   public JMenu getMenu()
   {
      return m_menu;
   }

   /**
    * Gets the popup-menu representation of this menu.
    *
    * @return the popup menu, never <code>null</code>
    */
   public JPopupMenu getPopupMenu()
   {
      return m_menu.getPopupMenu();
   }

   /**
    * Gets the menu/menu items that are part of this menu.
    *
    * @return the list of menu elements, may be empty, never <code>null</code>,
    * each element of the list can be either an instanceof <code>JMenuItem
    * </code> or <code>JMenu</code>.
    */
   public MenuElement[] getMenuElements()
   {
      return getPopupMenu().getSubElements();
   }

   /**
    * Estimates the maximum number of items that can show up on a menu without
    * "falling off" the screen. This is determined by calculating the line
    * height for each item based on the current font.
    * 
    * @param menu The menu to check for, assumed not <code>null</code>. 
    * 
    * @return the estimated maximum number of items to show per menu
    */
   private int getEstimatedMaxItems(Component menu)
   {
      FontMetrics metrics = menu.getFontMetrics(menu.getFont());
      int lineHeight = metrics.getHeight()
         + metrics.getDescent()
         + metrics.getLeading()
         + metrics.getAscent();

      return (getAvailableScreenHeight() / lineHeight);
   }

   /**
    * Returns the available screen height, this is the screen height
    * minus the taskbar (or best estimation if ran under Java 1.3).
    *
    * @return the available screen height
    */
   private int getAvailableScreenHeight()
   {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      int screenHeight = toolkit.getScreenSize().height;

      Insets insets = null;
      Frame frame = m_actManager.getApplet().getParentFrame();
      if(frame != null)
      {
         GraphicsConfiguration gc = frame.getGraphicsConfiguration();
         insets = toolkit.getScreenInsets(gc);
      }
      
      if(insets != null)
      {
         screenHeight -= (insets.top + insets.bottom);
      }
      else
      {
         screenHeight -= TASKBAR_HEIGHT;
      }

      return screenHeight < 0 ? 0 : screenHeight ;
   }


    /**
     * There seems to be an issue in the 1.4.1_xx plugin where by they are not 
     * correctly honoring signed applets.
     * There is a  work around to get the applet to work by simply adding a 
     * permission in the plugins' security policy file.
     * The file path is : {java_plugin_home}\lib\security\java.policy
     * So on most windows systems it would be found at:   
     * c:\Program Files\Java\j2re1.4.1_xx\lib\security\java.policy
     * Just open that file and append the following entry to the end of 
     * the file:
     * grant {
     *       permission java.lang.RuntimePermission "accessClassInPackage.sun.awt.windows";
     * };
     */
    private static void printAccessControlExceptionInfo() 
    {
        String jvmHome = System.getProperty("java.home");
        log.error("A security exception was thrown while " +
                "attempting to find your \nmonitor's width and height.\n" +
                "There are two options: \n" +
                "  1.Upgrade your plugin. \n" +
                "  2.Grant permission in the plugins' security policy file.\n\n" +
                "Path to your plugin's security policy file is : \n" + jvmHome +
                "\\lib\\security\\java.policy. \n\n" +
                "Add the following at the end of the file:\n" +
                "grant { \n" +
                "    permission java.lang.RuntimePermission " +
                "\"accessClassInPackage.sun.awt.windows\"; \n" +
                "};" );
    }


/**
    * Estimated height of the users task bar
    */
   public static final int TASKBAR_HEIGHT = 150;

   /**
    * The source of this menu on which this menu should act on, initialized in
    * the ctor and never <code>null</code> or modified after that.
    */
   private PSMenuSource m_menuSource;

   /**
    * The action manager to use to execute any action, initialized in the ctor
    * and never <code>null</code> or modified after that.
    */
   private PSActionManager m_actManager;
   
   /**
    * The action to use to generate the menu initialized in the ctor
    * and never <code>null</code> or modified after that.
    */
   private PSMenuAction m_action;

   /**
    * The visual/swing menu that represents this object, initialized in ctor
    * through a call to <code>buildMenu(PSAction)</code> and never <code>null
    * </code> or modified after that.
    */
   private JMenu m_menu;

   /**
    * The constant to use to store the action (<code>PSAction</code>) in menu
    * item action (<code>javax.swing.Action</code>).
    */
   private static final String ACTION_OBJECT = "Action Object";
   
   /**
    * Font to use for the popup menu text
    */
   public static final Font POPUP_MENU_FONT = 
      new Font("Arial", Font.PLAIN, 11);
   
   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;

}


