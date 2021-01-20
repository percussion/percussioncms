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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package  com.percussion.filetracker;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.Keymap;

/**
 * This displays a dialog box to connect to Rx server and get the content item
 * list. User needs to choose the server alias and supply userid and passwords.
 * The first empty input field gets the focus and OK is the default button.
 * Clicking CANCEL buton puts the application in offline mode, if a snapshot
 * exists, Application is colsed otherwise.
 */

public class PSFUDConnectDlg extends JDialog
{
   //UI related variables are declared here

   /**
    * Main panel for the dialog box. This will have a gridbag layout housing
    * two panels, main panel and button panel.
    */
   JPanel m_DialogPanel = new JPanel();

   /**
    * Layout used by the dialog panel - m_DialogPanel
    */
   GridBagLayout m_GridBagLayoutDialog = new GridBagLayout();

   /**
    * This is the first in the dialog panel with grid layout (4x1) housing four
    * two panels for server alias, userid, password and version.
    */
   JPanel m_MainPanel = new JPanel();


   /**
    * Layout used for main panel - m_MainPanel
    */
   GridLayout m_GridLayoutMain = new GridLayout();

   /**
    * This panel consists in of a label for server alias and a combobox to
    * display a list of server aliases availabel in the configuration document.
    * Uses a default layout (gridbag layout).
    */
   JPanel m_PanelServerAlias = new JPanel();

   /**
    * Label for server alias
    */
   JLabel m_LabelServerAlias = new JLabel();

   /**
    * Combobox for server alias
    */
   JComboBox m_ServerAlias = null;

   /**
    * This panel consists in of a label for userid and a text field to input
    * userid. Uses a default layout (gridbag layout).
    */
   JPanel m_PanelUserid = new JPanel();

   /**
    * Label for userid
    */
   JLabel m_LabelUserid = new JLabel();

   /**
    * Text field for userid
    */
   JTextField m_Userid = new JTextField();

   /**
    * This panel consists in of a label for password and a password field to
    * input password. Uses a default layout (gridbag layout).
    */
   JPanel m_PanelPassword = new JPanel();

   /**
    * Label for password field
    */
   JLabel m_LabelPassord = new JLabel();

   /**
    * Password field
    */
   JPasswordField m_Password = new JPasswordField();

   /**
    * This panel consists in of a label for displaying version String.
    * Uses a default layout (gridbag layout).
    */
   JPanel m_PanelVersion = new JPanel();

   /**
    * Label for Vesion string
    */
   JLabel m_LabelVersion = new JLabel();

   /**
    * This is the second panel the dialog panel with grid layout (3x1) housing
    * OK button ,CANCEL button and label spacer.
    */
   JPanel m_PanelButtons = new JPanel();

   /**
    * Layout used by m_PanelButtons
    */
   GridLayout m_GridLayoutButtons = new GridLayout();

   /**
    * A spacer, must have empty text
    */
   JLabel m_LabelSpace1 = new JLabel();

   /**
    * OK button clicking which the content item list metadata is loaded from
    * the Rx server and the window is refreshed if load succeeds. Dialog box is
    * redisplayed if load fails.
    */
   JButton m_ButtonOK = new JButton();

   /**
    * CANCEL button clicking which will put the application in offline mode if
    * snapshot exists otherwise application closes.
    */
   JButton m_ButtonCancel = new JButton();

   /**
    * Constructor that takes the parent Frame object and the dialogbox title.
    * This dialog box is always a modal.
    */
   public PSFUDConnectDlg(JFrame frame, String title)
      throws PSFUDInvalidConfigFileException
   {
      super(frame, title, true);

      init();

      pack();
   }

   /**
    * Initialize the dialog box with all default field values. The server alias
    * combobox is populated with the list obtained from the configuration file.
    * Current alias from the config document is set selected in the combobox.
    * The cursor is set to first non empty field. OK button is made default
    * button.
    *
    * @throws if the configuration document does have any server aliases listed.
    *
    */
   private void init()
      throws PSFUDInvalidConfigFileException
   {
      m_ServerAlias = new JComboBox(MainFrame.getConfig().getServerAliases());
      m_ButtonOK.setText("OK");
      m_ButtonOK.addActionListener(
         new PSFUDConnectDlg_m_ButtonOK_actionAdapter(this));
      m_ButtonCancel.setText("Cancel");
      m_GridLayoutButtons.setColumns(1);
      m_GridLayoutButtons.setVgap(2);
      m_GridLayoutButtons.setRows(3);
      m_PanelButtons.setLayout(m_GridLayoutButtons);
      m_ButtonCancel.addActionListener(
         new PSFUDConnectDlg_m_ButtonCancel_actionAdapter(this));

      addWindowListener(new WindowAdapter()
      {
         //This is just to set the focus to first non empty field
         //Can be generalized for more fields.
         public void windowOpened(WindowEvent e)
         {
            if(m_Userid.getText().length() < 1)
               m_Userid.requestFocus();
            else
               m_Password.requestFocus();
         }
         //Handle system close event - equivalent to Cancel.
         public void windowClosing(WindowEvent e)
         {
            windowClosing(e);
         }
      });

      m_DialogPanel.setLayout(m_GridBagLayoutDialog);
      m_DialogPanel.setBackground(SystemColor.text);
      m_DialogPanel.setBorder(BorderFactory.createEtchedBorder());
      m_DialogPanel.setMaximumSize(new Dimension(375, 125));
      m_DialogPanel.setMinimumSize(new Dimension(375, 125));
      m_DialogPanel.setPreferredSize(new Dimension(375, 125));
      m_LabelServerAlias.setPreferredSize(new Dimension(100, 17));
      m_LabelServerAlias.setHorizontalAlignment(SwingConstants.RIGHT);
      m_LabelServerAlias.setHorizontalTextPosition(SwingConstants.LEFT);
      m_LabelServerAlias.setText("Server alias: ");
      m_LabelPassord.setPreferredSize(new Dimension(100, 17));
      m_LabelPassord.setHorizontalAlignment(SwingConstants.RIGHT);
      m_LabelPassord.setText("Password:");
      m_LabelUserid.setPreferredSize(new Dimension(100, 17));
      m_LabelUserid.setHorizontalAlignment(SwingConstants.RIGHT);
      m_LabelUserid.setText("User ID:");
      m_Password.setPreferredSize(new Dimension(150, 21));
      m_MainPanel.setLayout(m_GridLayoutMain);
      m_GridLayoutMain.setColumns(2);
      m_GridLayoutMain.setRows(4);
      m_ServerAlias.setPreferredSize(new Dimension(150, 24));
      m_Userid.setPreferredSize(new Dimension(150, 21));
      m_LabelVersion.setMaximumSize(new Dimension(200, 17));
      m_LabelVersion.setMinimumSize(new Dimension(200, 17));
      m_LabelVersion.setPreferredSize(new Dimension(280, 17));
      m_LabelVersion.setHorizontalAlignment(SwingConstants.CENTER);
      m_DialogPanel.add(m_MainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 
            1.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 8), 0, 0));
      m_MainPanel.add(m_PanelServerAlias, null);
      m_PanelServerAlias.add(m_LabelServerAlias, null);
      m_PanelServerAlias.add(m_ServerAlias, null);
      m_MainPanel.add(m_PanelUserid, null);
      m_PanelUserid.add(m_LabelUserid, null);
      m_PanelUserid.add(m_Userid, null);
      m_MainPanel.add(m_PanelPassword, null);
      m_PanelPassword.add(m_LabelPassord, null);
      m_PanelPassword.add(m_Password, null);
      m_MainPanel.add(m_PanelVersion, null);
      m_PanelVersion.add(m_LabelVersion, null);
      m_DialogPanel.add(m_PanelButtons,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE,
            new Insets(12, 0, 12, 8), 0, 0));
      m_PanelButtons.add(m_LabelSpace1, null);
      m_PanelButtons.add(m_ButtonOK, null);
      m_PanelButtons.add(m_ButtonCancel, null);
      getContentPane().add(m_DialogPanel);

      /*
       * This is a workaround for the default button to work till JDK 1.3
       */
      KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

      Keymap map = m_Userid.getKeymap();
      map.removeKeyStrokeBinding(enter);

      map = m_Password.getKeymap();
      map.removeKeyStrokeBinding(enter);
      // workaround ends

      this.getRootPane().setDefaultButton(m_ButtonOK);


      m_LabelVersion.setForeground(Color.blue);
      m_LabelVersion.setText(MainFrame.getVersionString());
      m_ServerAlias.setSelectedItem(ms_FUDConfig.getServerAlias());
      m_Userid.setText(ms_FUDConfig.getUserid());
      m_Password.setText(ms_FUDConfig.getPassword());
  }

   /**
    * Get method that returns the selected server alias
    *
    * @return server alias as String
    *
    */
   public String getServerAlias()
   {
      return m_ServerAlias.getSelectedItem().toString();
   }

   /**
    * Get method that returns userid
    *
    * @return userid as String
    *
    */
   public String getUserid()
   {
      return m_Userid.getText();
   }

   /**
    * Get method that returns the password
    *
    * @return unencrypted password alias as String
    *
    */
   public String getPassword()
   {
      return new String(m_Password.getPassword());
   }

   /**
    * Acion handler for OK button. If user clicks OK or ENTER button with empty
    * value for userid, a message box is displayed waiting for the input.
    */
   void ButtonOK_actionPerformed(ActionEvent e)
   {
      String tmp = m_Userid.getText();
      if(null == tmp || tmp.length() < 1)
      {
         JOptionPane.showMessageDialog(this,
            MainFrame.getRes().getString("errorUseridEmpty"));
         m_Userid.requestFocus();
         return;
      }
      m_exit = OK;
      dispose();
   }

   /**
    * Acion handler for Cancel. Exit value is set to CANCEL and dialog box
    * is closed.
    */
   void ButtonCancel_actionPerformed(ActionEvent e)
   {
      m_exit = CANCEL;
      dispose();
   }

   /**
    * Handle the system close event. Result is same as CANCEL
    */
   void windowClosing(WindowEvent e)
   {
      m_exit = CANCEL;
      dispose();
   }

   /**
    * Refernce to PSFUDConfig object for convenience
    */
   static private PSFUDConfig ms_FUDConfig = MainFrame.getConfig();

   /**
    * Get the type of exit operation performed to close this dialog.
    *
    * @return the exit operation (OK, CANCEL or CLOSE)
    */
   public int getExit()
   {
      return m_exit;
   }

   /**
    * The storage for the exit performed.
    */
   private int m_exit = -1;
   /**
    * The exit indicator for OK.
    */
   public static final int OK = 1;
   /**
    * The exit indicator for Cancel.
    */
   public static final int CANCEL = 2;

}

/**
 * Action Listener for OK button. Handles actionPerformed which just calles the
 * Dialog class's method ButtonOK_actionPerformed()
 */
class PSFUDConnectDlg_m_ButtonOK_actionAdapter implements ActionListener
{
   PSFUDConnectDlg dlg;

   /**
    * Constructor taking the caller class instance.
    */
   PSFUDConnectDlg_m_ButtonOK_actionAdapter(PSFUDConnectDlg dlg)
   {
      this.dlg = dlg;
   }

   /**
    * Handle the actionPerformed event. Just calles the caller's
    * ButtonOK_actionPerformed() method.
    */
   public void actionPerformed(ActionEvent e)
   {
      dlg.ButtonOK_actionPerformed(e);
   }
}

/**
 * Action Listener for CANCEL button. Handles actionPerformed which just calles
 * the Dialog class's method ButtonCancel_actionPerformed()
 */
class PSFUDConnectDlg_m_ButtonCancel_actionAdapter implements ActionListener
{
   PSFUDConnectDlg dlg;

   /**
    * Constructor taking the caller class instance.
    */
   PSFUDConnectDlg_m_ButtonCancel_actionAdapter(PSFUDConnectDlg dlg)
   {
      this.dlg = dlg;
   }

   /**
    * Handle the actionPerformed event. Just calles the caller's
    * ButtonCancel_actionPerformed() method.
    */
   public void actionPerformed(ActionEvent e)
   {
      dlg.ButtonCancel_actionPerformed(e);
   }
}

