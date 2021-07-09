/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.filetracker;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

/**
 * The main frame window for the application. Provides menu, toolbar buttons,
 * and status bar. The frame maintains a list of actions that are currently in
 * the menu or toolbar.
 * <p>
 * This class also provides a mechanism to enable or disable the actions that
 * can be performed depending on selected nodes in the tree table and their
 * current states.
 * <p>
 */
public class MainFrame extends JFrame
{
   //all UI related variables/constants here

   /**
    * Scroll pane is the container for the tree table to provide scrolling.
    */
   private JScrollPane m_ScrollPane = new JScrollPane();

   private JMenuBar m_MenuBar = new JMenuBar();
   private PSToolBar m_ToolBar = new PSToolBar();
   private static JLabel ms_StatusBar = new JLabel();

   /**
    * A map of all actions that shared by the menu bar and tool bar.
    */
   private HashMap m_actions = new HashMap(30);

   /**
    * A reusable wait cursor.
    */
   private static final Cursor WAITCURSOR = Cursor.getPredefinedCursor(
                                                         Cursor.WAIT_CURSOR);

   /**
    * This is the main and only component (besides menu, tool and status bars)
    * that fills the main window. Whenever the tree table model is refreshed
    * with a new content list from the server, this object will be recreated
    * discarding the old one.
    *
    * Never <code>null</code>.
    */
   private PSFUDJTreeTable m_TreeTable = null;

   /**
    * Initializes the main window and prompts for user info to connect to
    * Rhythmyx server to get the content item list metadata. If the query
    * fails for some reason user can press CANCEL button to work offline if
    */
   public MainFrame()
   {
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      init();
   }

   /**
    * Component initialization. initializes all components, viz. actions,
    * toolbar, menubar statusbar in the main window and prompts the user for
    * connection information.
    * <p>
    * Menu/toolbar model.
    * A map contains all Actions that will be used in the menu and/or toolbar,
    * except those that need JMenuCheckBox.
    * The key to each entry in the map will be the internal name of the action,
    * the value will be the Action object.
    * The menu is built using the desired actions in the desired order, as is
    * the toolbar.
    * To add new menuitems/toolbar buttons, add the action, then add the action
    * to the menu/toolbar as needed.
    */
   private void init()
   {
      URL url = getClass().getResource("images/fudmgr.gif");
      if (url != null)
         setIconImage(new ImageIcon(url).getImage());
      getContentPane().setLayout(new BorderLayout());
      setSize(new Dimension(640, 480));
      setTitle(APPTITLE);
      m_MouseHandler = new MouseHandler();
      ms_StatusBar.setBorder(BorderFactory.createLoweredBevelBorder());
      addMouseListener(m_MouseHandler);

      // create the file menu actions
      KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_fileConnect"));
      ImageIcon image = null;
      if (url != null)
         image = new ImageIcon(url);
      PSAction action = new PSAction("Connect", 'N',
                        ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            fileConnect();
         }
      };

      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("fileConnectDesc"));
      m_actions.put(CONNECT, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_fileExit"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Exit", 'x', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            fileExit();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("fileExitDesc"));
      m_actions.put(EXIT, action);

      JMenuItem menuItem;
      // Build the file menu
      PSMenu file = new PSMenu("File", 'F');
      menuItem = file.add((Action) m_actions.get(CONNECT));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = file.add((Action) m_actions.get(EXIT));
      menuItem.addMouseListener(m_MouseHandler);
      m_MenuBar.add(file);

      // create the View menu actions
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_viewRefresh"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Refresh", 'f', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            viewRefresh();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("viewRefreshDesc"));
      m_actions.put(REFRESH, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_viewOptions"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Options", 'O', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            viewOptions();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("viewOptionsDesc"));
      m_actions.put(OPTIONS, action);

      // Build the view menu
      PSMenu view = new PSMenu("View", 'V');
      menuItem = view.add((Action) m_actions.get(REFRESH));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = view.add((Action) m_actions.get(OPTIONS));
      menuItem.addMouseListener(m_MouseHandler);
      m_MenuBar.add(view);

      // create the Action menu actions
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_actionDownload"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Download", 'd', ks, image)
      {
         public void actionPerformed(ActionEvent e)
         {
            performAction(DOWNLOAD);
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("actionDownloadDesc"));
      m_actions.put(DOWNLOAD, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_U,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_actionUpload"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Upload", 'U', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            performAction(UPLOAD);
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("actionUploadDesc"));
      m_actions.put(UPLOAD, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_L,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_actionLaunch"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Launch", 'l', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            performAction(LAUNCH);
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("actionLaunchDesc"));
      m_actions.put(LAUNCH, action);

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_actionPurgeLocal"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("Purge local", 'g',
                     ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            performAction(PURGELOCAL);
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("actionPurgeLocalDesc"));
      m_actions.put(PURGELOCAL, action);

      // Build the Action menu
      PSMenu actions = new PSMenu("Action", 'A');
      menuItem = actions.add((Action) m_actions.get(DOWNLOAD));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = actions.add((Action) m_actions.get(UPLOAD));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = actions.add((Action) m_actions.get(LAUNCH));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = actions.add((Action) m_actions.get(PURGELOCAL));
      menuItem.addMouseListener(m_MouseHandler);
      m_MenuBar.add(actions);

      actions.addMenuListener(new PSMenuAdapter()
      {
         public void menuSelected(MenuEvent event)
         {
            updateMenu();
         }
      });

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                                            Event.CTRL_MASK, true);
      url = getClass().getResource(getRes().getString("gif_helpAbout"));
      image = null;
      if (url != null)
         image = new ImageIcon(url);
      action = new PSAction("About", 'b', ks,   image)
      {
         public void actionPerformed(ActionEvent e)
         {
            helpAbout();
         }
      };
      action.putValue(Action.SHORT_DESCRIPTION,
                      getRes().getString("helpAboutDesc"));
      m_actions.put(ABOUT, action);

      // Build the Help menu
      PSMenu help = new PSMenu("Help", 'h');
      menuItem = help.add((Action) m_actions.get(ABOUT));
      menuItem.addMouseListener(m_MouseHandler);
      m_MenuBar.add(help);

      setJMenuBar(m_MenuBar);

      // create the context menu
      m_contextMenu = new JPopupMenu();
      menuItem = m_contextMenu.add((Action) m_actions.get(DOWNLOAD));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = m_contextMenu.add((Action) m_actions.get(UPLOAD));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = m_contextMenu.add((Action) m_actions.get(PURGELOCAL));
      menuItem.addMouseListener(m_MouseHandler);
      menuItem = m_contextMenu.add((Action) m_actions.get(LAUNCH));
      menuItem.addMouseListener(m_MouseHandler);
      m_contextMenu.addSeparator();
      menuItem = m_contextMenu.add((Action) m_actions.get(OPTIONS));
      menuItem.addMouseListener(m_MouseHandler);
      m_contextMenu.addSeparator();
      menuItem = m_contextMenu.add((Action) m_actions.get(REFRESH));
      menuItem.addMouseListener(m_MouseHandler);

      /* Build toolbar */
      m_ToolBar.add((PSAction) m_actions.get(CONNECT));
      m_ToolBar.addSeparator();
      m_ToolBar.add((PSAction) m_actions.get(REFRESH));
      m_ToolBar.add((PSAction) m_actions.get(OPTIONS));
      m_ToolBar.addSeparator();
      m_ToolBar.add((PSAction) m_actions.get(DOWNLOAD));
      m_ToolBar.add((PSAction) m_actions.get(UPLOAD));
      m_ToolBar.add((PSAction) m_actions.get(LAUNCH));
      m_ToolBar.add((PSAction) m_actions.get(PURGELOCAL));
      m_ToolBar.addSeparator();
      m_ToolBar.add((PSAction) m_actions.get(ABOUT));

      m_ToolBar.addMouseListener(m_MouseHandler);

      getContentPane().add(m_ScrollPane, BorderLayout.CENTER);
      getContentPane().add(ms_StatusBar, BorderLayout.SOUTH);
      getContentPane().add(m_ToolBar, BorderLayout.NORTH);

      m_fudApplication = new PSFUDApplication();
      fileConnect();
   }

   /**
    * Initialization routine for tree alone. This will be called whenever the
    * model is refreshed with a new content item list from the server.
    */
   private void treeInit()
      throws PSFUDNullElementException
   {
      if(null != m_TreeTable)
      {
         m_TreeTable.removeMouseListener(m_MouseHandler);
         m_TreeTable = null;
      }

      m_TreeTable = new PSFUDJTreeTable(
                     new PSFUDNodeModel(
                     new PSFUDAppNode(m_fudApplication)
                     ));

      m_ScrollPane.getViewport().add(m_TreeTable, null);
      m_ScrollPane.getViewport().setBackground(Color.white );

      m_TreeTable.addMouseListener(m_MouseHandler);

      //show expanded on init
      viewExpandAll();

      setStatus(null);
   }


   /**
    * The method is called during application initialization and whenever user
    * performs action connect from the menubar or tool bar. Displays connect
    * dialog box for the connection information. Continues to display till the
    * connection succeeds or user clicks CANCEL buttn. Pressing cancel button
    * puts the application in offline mode if local copy (snapshot file) exists
    * or exits the application.
    */
   private void fileConnect()
   {
      PSFUDConnectDlg dlg = null;
      try
      {
         dlg = new PSFUDConnectDlg(this, APPTITLE + ": " +
            getRes().getString("ConnectString"));
      }
      catch(PSFUDInvalidConfigFileException e)
      {
         JOptionPane.showMessageDialog(this, e.getMessage());
         System.exit(1);
      }

      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation((frmSize.width - dlgSize.width) / 2 +
            loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);

      boolean bRepeat = false;
      int nReturn = -1;
      do
      {
         bRepeat = false;
         dlg.setModal(true);
         dlg.setVisible(true);
         nReturn = dlg.getExit();
         if(PSFUDConnectDlg.OK == nReturn)
         {
            setCursor(getWaitCursor());

            //If server alias or userid changes we need to load the new
            //application and discard the old one
            String serveralias = dlg.getServerAlias();
            String userid = dlg.getUserid();

            try
            {
               getConfig().setServerAlias(dlg.getServerAlias());
               getConfig().setUserid(dlg.getUserid());
               getConfig().setPassword(dlg.getPassword());

               m_fudApplication = null; //discard the old one
               m_fudApplication = new PSFUDApplication(); //load the new one
               m_fudApplication.refresh(m_fudApplication.loadRemote());

               treeInit();
            }
            catch(PSFUDNullElementException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
            catch(PSFUDEmptyServerAliasException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
            catch(PSFUDInvalidConfigFileException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
            catch(PSFUDAuthenticationFailureException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, getRes().
                        getString("errorAuthentication"));
               bRepeat = true;
            }
            catch(PSFUDServerException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
            catch(PSFUDNullDocumentsException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
            catch(PSFUDMergeDocumentsException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               bRepeat = true;
            }
         }
         else
         {
            try
            {
               m_fudApplication.refresh(null);
               treeInit();
            }
            catch(PSFUDMergeDocumentsException e)
            {
               //can ignore safely since there is no remote document to merge!
            }
            catch(PSFUDNullDocumentsException e)
            {
               System.exit(1);
            }
            catch(PSFUDNullElementException e)
            {
               JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
               System.exit(1);
            }
         }
      } while(bRepeat);

      setStatus(null);
      setCursor(null);
   }

   /**
    * The method is called when user performs action file|exit or during system
    * close. The configuration document and a snapshot of the current content
    * item list document are saved just before closing the application. If any
    * of these save actions fail for any reason user will be given a choice to
    * quit or resume.
    */
   private void fileExit()
   {
      try
      {
         getConfig().save();
         m_fudApplication.save();
      }
      catch(IOException e)
      {
         String msg = "";
         int option = JOptionPane.showConfirmDialog(MainFrame.this,
               MessageFormat.format(getRes().getString("errorSaveExit"),
               new String[]{e.getMessage()}), ERROR, JOptionPane.YES_NO_OPTION);

         if(option == JOptionPane.NO_OPTION)
            return;
      }
      System.exit(0);
   }

   /**
    * The method is called when user performs action view|refresh. The
    * application will be refreshed using the existing connection information.
    * The new content item list document from the server shall be merged with
    * the one in  memory according merging rules in the functional spec. If
    * refresh fails, user shall be prompted with appropriate error message.
    */
   private void viewRefresh()
   {
      setCursor(getWaitCursor());
      try
      {
         if(m_fudApplication.isOffline())
         {
            m_fudApplication.refresh(null);
         }
         else
         {
            m_fudApplication = null; //discard the old one
            m_fudApplication = new PSFUDApplication(); //load the new one
            m_fudApplication.refresh(m_fudApplication.loadRemote());
         }
         treeInit();
      }
      catch(PSFUDAuthenticationFailureException e)
      {
         JOptionPane.showMessageDialog(MainFrame.this, getRes().
                        getString("authenticationErrorReconnect"));
      }
      catch(PSFUDServerException e)
      {
         JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
      }
      catch(PSFUDNullDocumentsException e)
      {
         JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
      }
      catch(PSFUDNullElementException e)
      {
         JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
      }
      catch(PSFUDMergeDocumentsException e)
      {
         JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
      }
      setCursor(null);
   }

   /**
    * Helper function to display the tree table expanded. This function is not
    * really required now but may be required when we give a menu item to
    * exapnd all.
    */
   public void viewExpandAll()
   {
      m_TreeTable.expandAll(true);
   }

   /**
    * The method is called when user performs action view|options. The
    * application will be refreshed using the existing connection information.
    * The new content item list document from the server shall be merged with
    * the one in  memory according merging rules in the functional spec. If
    * refresh fails, user shall be prompted with appropriate error message.
    */
   public void viewOptions()
   {
      PSFUDOptionsDlg dlg = new PSFUDOptionsDlg(this, "Options");

      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation((frmSize.width - dlgSize.width) / 2 +
            loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);

      dlg.show();
   }

   /**
    * This method wraps all four action possible, viz. LAUNCH, DOWNLOAD, UPLOAD
    * and PURGELOCAL and shall be called by action listener with appropriate
    * action string. If any action changes the snapshot dicument the document
    * shall be written to disk.
    */
   private void performAction(String action)
   {
      setCursor(getWaitCursor());

      ArrayList fileNodes = m_TreeTable.getSelectedFileList();
      PSFUDFileNode fileNode = null;
      boolean bSaveSnapshot = false;
      for(int i=0; i<fileNodes.size(); i++)
      {
         try
         {
             fileNode = (PSFUDFileNode)fileNodes.get(i);
             if(action.equals(LAUNCH))
             {
             /*
             launch does not change the snapshot doc do not change
             bActionPerformed
             */
             //if local copy does not exist and remote exists, download first
               if(!fileNode.isLocalCopy() &&
                   fileNode.STATUS_CODE_ABSENT != fileNode.getStatusCode())
               {
                  performAction(DOWNLOAD);
               }
               //launch now.
               fileNode.launch();
             }
             else if(action.equals(PURGELOCAL))
             {
               if(fileNode.isLocalCopy() &&
                  getConfig().getIsPromptBeforePurge())
               {
                  int option = JOptionPane.showOptionDialog(MainFrame.this,
                     MessageFormat.format(getRes().getString("warningPurge"),
                     new String[] {fileNode.getFileName()}), WARNING,
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                     null, null, null);

                  if(JOptionPane.NO_OPTION == option)
                  {
                     System.out.println("File '" + fileNode.getLocalPath() +
                        "' is not being purged...");
                     continue;
                  }
               }

               if(false == fileNode.purgeLocal())
                  throw new Exception(getRes().getString("errorPurge"));
               //purge local changes snapshot doc, set the flag to save
               bSaveSnapshot = true;
            }
             else if(action.equals(DOWNLOAD))
             {
               if((fileNode.isLocalCopy() &&
                  getConfig().getIsPromptBeforeOverwrite()) ||
                  IPSFUDNode.STATUS_CODE_LOCALNEW == fileNode.getStatusCode())
               {
                  int option = JOptionPane.showOptionDialog(MainFrame.this,
                     MessageFormat.format(getRes().getString("warningOverwrite")
                     ,new String[]{fileNode.getFileName()}), WARNING,
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                     null, null, null);

                  if(JOptionPane.NO_OPTION == option)
                  {
                     //Debug purpose
                     System.out.println("File '" + fileNode.getLocalPath() +
                        "' is skipped for downloading...");
                     continue;
                  }

               }
               setStatus(MessageFormat.
                  format(getRes().getString("statusDownloadingFile"),
                         new String[]{fileNode.getFileName()}));

               fileNode.download();
               //download changes snapshot doc, set the flag to save
               bSaveSnapshot = true;
             }
             else if(action.equals(UPLOAD))
             {
               setStatus(MessageFormat.
                  format(getRes().getString("statusUploadingFile"),
                         new String[]{fileNode.getFileName()}));
               fileNode.upload();
               // purge local copy if option is set so
               if(getConfig().getIsPurgeLocalCopy())
                  fileNode.purgeLocal();
               //upload changes snapshot doc, set the flag to save
               bSaveSnapshot = true;
             }
         }
         catch(Exception e)
         {
            JOptionPane.showMessageDialog(MainFrame.this,
                     MessageFormat.format(
                        getRes().getString("errorAction"),
                        new String[] {fileNode.getFileName(),
                                       action, e.getMessage()}));
         }
      }

      //save the snapshot if required
      try
      {
         if(bSaveSnapshot)
            m_fudApplication.save();
      }
      catch(Exception e)
      {
               JOptionPane.showMessageDialog(MainFrame.this,
                     fileNode.getFileName() + " : " + e.getMessage());
      }
      m_TreeTable.repaint();
      setCursor(null);
      setStatus(null);
   }

   /**
    * The method is called when user performs action help|about. Displays
    * the standard about dialog box.
     */
   private void helpAbout()
   {
      AboutDialog dlg = new AboutDialog(MainFrame.this, "About " + APPTITLE);
      dlg.setModal(true);
      dlg.show();
   }

   /**
    * Overridden so we can exit on System Close
    */
   protected void processWindowEvent(WindowEvent e)
   {
      super.processWindowEvent(e);
      if(e.getID() == WindowEvent.WINDOW_CLOSING)
      {
         fileExit();
      }
   }

   /**
    * The menu/actions are enabled or disabled depending on the what type of
    * nodes user selected in the tree table and what state the nodes are in.
    * This is called whenever a menu item is selected or right mouse button
    * clicked or selection is changed using mouse.
    */
   public void updateMenu()
   {
      ArrayList fileNodes = m_TreeTable.getSelectedFileList();
      PSFUDFileNode fileNode = null;

      boolean bDownloadable = false;
      boolean bUploadable = false;
      boolean bLaunchable = false;
      boolean bPurgable = false;

      for(int i=0; i<fileNodes.size(); i++)
      {
         fileNode = (PSFUDFileNode)fileNodes.get(i);

         if(!bPurgable && fileNode.isLocalCopy())
         {
            bPurgable = true;
         }

         if(!bLaunchable && (fileNode.isRemoteExists() ||
             fileNode.isLocalCopy()))
         {
            bLaunchable = true;
         }

         if(!bDownloadable && fileNode.isRemoteExists())
         {
            bDownloadable = true;
         }

         if(!bUploadable && fileNode.isRemoteExists() &&
             fileNode.isLocalCopy())
         {
            bUploadable = true;
         }
      }

      ((Action) m_actions.get(DOWNLOAD)).setEnabled(bDownloadable);
      ((Action) m_actions.get(UPLOAD)).setEnabled(bUploadable);
      ((Action) m_actions.get(LAUNCH)).setEnabled(bLaunchable);
      ((Action) m_actions.get(PURGELOCAL)).setEnabled(bPurgable);
   }

   /**
    * Get the program resources. Loads if not yet loaded. Application quits
    * (with a an error message) if fails to load resource bundle.
    *
    * @return resource bundle as ResourceBundle never <code>null</code>.
    *
    */
   public static ResourceBundle getRes()
   {
      if(null != ms_res)
         return ms_res;

      try
      {
         ms_res = ResourceBundle.getBundle("com.percussion.filetracker.MainFrame" +
                  "Resources", Locale.getDefault());
      }
      catch(MissingResourceException mre)
      {
         //do nothing. Next check will take care of this.
      }
      if(ms_res == null)
      {
         JOptionPane.showMessageDialog(null,
            "Resource file could not be loaded. Make sure you have right" +
            " build or contact Percussion Technical Support.", "Fatal Error",
            JOptionPane.ERROR_MESSAGE);

            System.exit(1);
      }
      return ms_res;
   }

   /**
    * Get the configuration object. Tries to create on if not created yet.
    * Application quits (with an error message) if fails to load the 
    * configuration file.
    *
    * @return the instance of PSFUDConfig never <code>null</code>
    *
    */
   public static PSFUDConfig getConfig()
   {
      if(null != ms_FUDConfig)
         return ms_FUDConfig;

      try
      {
         ms_FUDConfig = new PSFUDConfig();
      }
      catch(Exception e)
      {
         JOptionPane.showMessageDialog(null, MessageFormat.format(
            getRes().getString("errorConfigFile"),
            new String[]{PSFUDConfig.FUDCONFIGFILE, e.getMessage()}),
            "Fatal Error", JOptionPane.ERROR_MESSAGE);

         System.exit(1);
      }
      return ms_FUDConfig;
   }
   /**
    * Sets the supplied text in the status bar. If <code>null</code> is
    * specified, idle message ('Ready'") is displayed.
    *
    * @param msg as String can be <code>null</code>
    *
    */
   public static void setStatus(String msg)
   {
      if(null == msg)
         ms_StatusBar.setText(STATUS_IDLE);
      else
         ms_StatusBar.setText(msg);
   }

   /**
    * Get mothod for wait cursor
    *
    * @return wait cursor as Cursor never <code>null</code>
    *
    */
   public static Cursor getWaitCursor()
   {
      return WAITCURSOR;
   }

   /**
    * This adapter is constructed to handle the situations when user moves 
    * mouse over the menu items the status bar displays the description of 
    * actions, and, to handle three situations for tree table, namely:    
    * <p>    
    * User clicks the left mouse button -> menu is updated,    
    * User clicks the right mouse button -> context menu is shown,    
    * User double clicks the left mouse button -> file is launced if possible, 
    * and when mouse enters the tree table the status bar is set to have 
    * STATUS_IDLE.    
    */   
   private class MouseHandler extends MouseAdapter   
   {      
      /**       
       * Constructor for the adapter.       
       */      
      public MouseHandler()
      {      
      }      
      
      /**       
       * Called by frame work when user clicks right mouse button. If the 
       * source is tree table, actions are updated and context menu is 
       * displayed.       
       *       
       * @param event MouseEvent       
       *       
       */      
      public void mouseReleased(MouseEvent event)      
      {
         if (m_contextMenu != null && event.isPopupTrigger())
         {
            updateMenu();
            m_contextMenu.show(event.getComponent(), event.getX(), 
               event.getY());
         }
         setStatus(null);
      }

      /**       
       * Called by frame work when user clicks or double clicks left mouse.       
       * Updates the actions. If doubleclicked in tree table, tries lauch       
       * the selected file, if applicable.       
       *       
       * @param event MouseEvent       
       *       
       */      
      public void mouseClicked(MouseEvent event)
      {
         updateMenu();
         if(2 == event.getClickCount())
         {
            performAction(LAUNCH);
         }
      }

      /**       
       * If mouse enters the menu items, the status bar is updated to display       
       * the description of each item, otherwise idle message.        
       */      
      public void mouseEntered(MouseEvent evt)      
      {         
         setStatus(null);         
         if (evt.getSource() instanceof AbstractButton)         
         {            
               AbstractButton button = (AbstractButton)evt.getSource();            
               String desc = button.getToolTipText();            
               if (desc != null)               
                  setStatus(desc);         
         }      
      }
   }
      
   /**
    * Returns the version string displayable in several dialog boxes
    *
    * @return  version string as String never <code>null</code>. Empty  string 
    *          if fails to load version resources.
    */
   public static String getVersionString()
   {
      ResourceBundle res = ResourceBundle.getBundle(
                     "com.percussion.filetracker.Version", Locale.getDefault());
      if(null == res)
         return "";

      String strVersion = res.getString("versionString");

      String tmp = res.getString("majorVersion") + "." +
                       res.getString("minorVersion");
      if(null != tmp && tmp.length() > 0)
         strVersion += " " + tmp;

      tmp = res.getString("buildNumber") + " (" + res.getString("buildId") + 
         ") ";

      if(null != tmp && tmp.length() > 0)
         strVersion += " " + tmp;

      return strVersion;
   }

   /**
    * The application node object that wraps the root element in the content
    * list document this handles loading remote, merging with snapshot and
    * saving besides being part of the tree table model for displaying in the
    * tree table.
    */
   private PSFUDApplication m_fudApplication = null;

   /**
    * The program resources.
    */
   private static ResourceBundle ms_res = null;

   /**
    * Reference to PSFUDConfig object that wraps Configuration XML document.
    */
   static public PSFUDConfig ms_FUDConfig = null;

   /**
    * The title of the application
    */
   static public final String APPTITLE = "Rhythmyx File Tracker";

   /**
    * Idle status text
    */
   static private final String STATUS_IDLE = "Ready";

   /**
    * Title for Warning message boxes.
    */
   static private final String WARNING = "Warning";

   /**
    * Title for Error message boxes.
    */
   static private final String ERROR = "Error";

   /**
    * Action names. The names must be unique within the set of all actions. The
    * names should not be tied to any particular main menu item.
    */

   private static final String CONNECT = "connect";
   private static final String EXIT = "exit";
   private static final String REFRESH = "refresh";
   private static final String OPTIONS = "options";
   private static final String DOWNLOAD = "download";
   private static final String UPLOAD = "upload";
   private static final String LAUNCH = "launch";
   private static final String PURGELOCAL = "purgelocal";
   private static final String ABOUT = "about";

   /**
    * The context menu valid for the selected tab.
    */
   private JPopupMenu m_contextMenu = null;

   /**
    * This adapter handles Mouse over messages on toolbar buttons and
    * menu items.    
    */   
   private MouseHandler m_MouseHandler;
}


