/* ****************************************************************************
 *
* [ PSContentExplorerMenuBar.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.cx;

import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSMenuBar;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The class that represents top-level menu bar in the applet.
 */
public class PSContentExplorerMenuBar extends JMenuBar implements IPSSelectionListener
{
   /**
    * Constructs the global menu bar with supplied parameters.
    *
    * @param menuBar the menu bar that holds the top-level menu actions, may not
    * be <code>null</code>
    * @param menuSource the menu source on which the menu actions should act on,
    * may not be <code>null</code>
    * @param actManager the action manager, may not be <code>null</code>
    */
   public PSContentExplorerMenuBar(PSMenuBar menuBar, PSMenuSource menuSource, PSActionManager actManager)
   {
      
      if (menuBar == null)
         throw new IllegalArgumentException("menuBar may not be null.");

      if (menuSource == null)
         throw new IllegalArgumentException("menuSource may not be null.");

      if (actManager == null)
         throw new IllegalArgumentException("actManager may not be null.");
      this.setFocusTraversalKeysEnabled(true);
      m_menuBar = menuBar;
      m_menuSource = menuSource;
      m_actManager = actManager;
      m_menus = new ArrayList<PSContentExplorerMenu>();

      createMenus();

   }

   /**
    * Refresh the menu with the current menu source selected.  
    *
    */
   public void refreshMenus() 
   {
      for (PSContentExplorerMenu cxMenu : m_menus)
      {
         cxMenu.refreshChildMenus();
      }
   }
   
   /**
    * Create all menus for this menu bar
    */
   private void createMenus()
   {
      Iterator actions = m_menuBar.getActions();
      PSMenuAction action = null;
      
      while(actions.hasNext())
      {
         action = (PSMenuAction)actions.next();
         if(action != null)
         {
            PSContentExplorerMenu menu = new PSContentExplorerMenu(
               action, m_menuSource, m_actManager);
            m_menus.add(menu);
            m_menuActions.add(action);
            
            add(menu.getMenu());
         }
      }
   }

   /**
    * Gets the menu represented by this menu bar as pop-up menu.
    *
    * @return the pop-up menu, never <code>null</code> and will have menu
    * elements.
    */
   public JPopupMenu getPopupMenu()
   {
      JPopupMenu popup = new JPopupMenu();      
   
      for(PSMenuAction action : m_menuActions)
      {
         PSContentExplorerMenu cxMenu = new PSContentExplorerMenu(
               action, m_menuSource, m_actManager);
         
       //  JMenu menu = new JMenu(cxMenu.getMenu().getAction());
         JMenu menu = cxMenu.getMenu();
         menu.setFont(PSContentExplorerMenu.POPUP_MENU_FONT);
         menu.getPopupMenu().addPopupMenuListener(cxMenu);
         popup.add(menu);
      }

      return popup;
   }

   private PSMenuBar m_menuBar;
   private PSMenuSource m_menuSource;
   private PSActionManager m_actManager;
   private List<PSContentExplorerMenu> m_menus;
   

   /**
    * The list of <code>PSContentExplorerMenu</code>s that represent that exists
    * in this menu bar, initialized and filled in the constructor and never
    * <code>null</code> or modified after that.
    */
   private List<PSMenuAction> m_menuActions = new ArrayList<PSMenuAction>();
   
   /**
    * Notification event that the selection has changed in main view of applet.
    * Updates the default selection and context selection of the menu source if
    * the supplied selection is an instance of {@link PSNavigationalSelection},
    * otherwise only context selection.
    * 
    * @param selection the selection object that represents the current
    *           selection in the main view.
    */
   public void selectionChanged(PSSelection selection)
   {
      if (selection == null)
         throw new IllegalArgumentException("selection may not be null.");

      if (selection instanceof PSNavigationalSelection)
         m_menuSource.setSource(selection);

      m_menuSource.setContextSource(selection);

      refreshMenus();
      m_actManager.informListeners(PSActionEvent.REFRESH_OPTIONS);
   }
}