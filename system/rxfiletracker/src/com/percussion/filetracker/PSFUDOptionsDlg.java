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



import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;




/**
 * This class is to display the application options dialog box. This is a 
 * simple and straightforward class. The options are initialized from the 
 * configuration file and written back when user click OK button.
 */

public class PSFUDOptionsDlg extends JDialog
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
   GridBagLayout m_GridBagLayoutDialogPanel = new GridBagLayout();

   /**
    * This is the first in the dialog panel with grid layout (4x1) housing four
    * two panels for server alias, userid, password and version.
    */
   JPanel m_MainPanel = new JPanel();

   /**
    * Layout used for main panel - m_MainPanel
    */
   GridLayout m_GridLayoutMainPanel = new GridLayout();

   /**
    * This panel houses a checkbox to purge local copy option.
    * Uses a default layout (gridbag layout).
    */
   JPanel m_PanelPurgeLocalCopy = new JPanel();

   /**
    * Purge local copy checkbox, defaults to the option value from 
    * configuration document.
    */
   JCheckBox m_PurgeLocal = new JCheckBox();

   /**
    * This panel houses a checkbox to prompt before purging option.
    * Uses a default layout (gridbag layout).
    */
   JPanel m_PanelPromptPurge = new JPanel();

   /**
    * Prompt before purging checkbox, defaults to the option value from
    * configuration document.
    */
   JCheckBox m_PromptBeforePurge = new JCheckBox();

   /**
    * This panel houses a checkbox to prompt before overwrite local file option.
    * Uses a default layout (gridbag layout).
    */
   JPanel m_PanelPropmptOverwrite = new JPanel();

   /**
    * Prompt before overwrite local file  checkbox, defaults to the option
    * value from configuration document.
    */
   JCheckBox m_PromptBeforeOverwrite = new JCheckBox();

   /**
    * A spacer in the main panel
    */
   JPanel m_PanelSpace1 = new JPanel();

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
    * OK button clicking which the options are written to configuration 
    * document.
    */
   JButton m_ButtonOK = new JButton();
   /**
    * CANCEL button closes the dialog box doing nothing.
    */
   JButton m_ButtonCancel = new JButton();

   /**
    * Constructor. Makes sure always modal. Initializes dialog box with default
    * values for the fields.
    */
   public PSFUDOptionsDlg(JFrame frame, String title)
   {
      super(frame, title, true);
      init();
      pack();
   }

   /**
    * Initialization of the dialog box with all components and default values.
    */
   private void init()
   {
      m_MainPanel.setLayout(m_GridLayoutMainPanel);
      m_ButtonOK.setText("OK");
      m_ButtonOK.addActionListener(
            new PSFUDOptionsDlg_m_ButtonOK_actionAdapter(this));
      m_ButtonCancel.setText("Cancel");
      m_GridLayoutButtons.setColumns(1);
      m_GridLayoutButtons.setVgap(2);
      m_GridLayoutButtons.setRows(4);
      m_PanelButtons.setLayout(m_GridLayoutButtons);
      m_ButtonCancel.addActionListener(
            new PSFUDOptionsDlg_m_ButtonCancel_actionAdapter(this));

      addWindowListener(new WindowAdapter()
      {
         //Handle system close event - equivalent to Cancel.
         public void windowClosing(WindowEvent e)
         {
            closeWindow(e);
         }
      });

      m_DialogPanel.setLayout(m_GridBagLayoutDialogPanel);
      m_GridLayoutMainPanel.setRows(5);
      m_PurgeLocal.setPreferredSize(new Dimension(350, 25));
      m_PurgeLocal.setText(
         MainFrame.getRes().getString("dlgtext_PurgeLocalCopyAfterUpload"));
      m_DialogPanel.setBorder(BorderFactory.createEtchedBorder());
      m_DialogPanel.setMinimumSize(new Dimension(450, 142));
      m_DialogPanel.setPreferredSize(new Dimension(450, 142));
      m_PromptBeforePurge.setPreferredSize(new Dimension(350, 25));
      m_PromptBeforePurge.setText(
         MainFrame.getRes().getString("dlgtext_PromptBeforePurgingLocal"));
      m_PromptBeforeOverwrite.setPreferredSize(new Dimension(350, 25));
      m_PromptBeforeOverwrite.setText(
         MainFrame.getRes().getString("dlgtext_PromptBeforeOverwritingLocal"));
      m_DialogPanel.add(m_MainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0,
            1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 8), 0, 0));

      m_MainPanel.add(m_PanelSpace1, null);
      m_MainPanel.add(m_PanelPurgeLocalCopy, null);
      m_PanelPurgeLocalCopy.add(m_PurgeLocal, null);
      m_MainPanel.add(m_PanelPromptPurge, null);
      m_PanelPromptPurge.add(m_PromptBeforePurge, null);
      m_MainPanel.add(m_PanelPropmptOverwrite, null);
      m_PanelPropmptOverwrite.add(m_PromptBeforeOverwrite, null);
      m_DialogPanel.add(m_PanelButtons,
            new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.NORTH, GridBagConstraints.NONE,
            new Insets(12, 0, 12, 8), 0, 0));

      m_PanelButtons.add(m_LabelSpace1, null);
      m_PanelButtons.add(m_ButtonOK, null);
      m_PanelButtons.add(m_ButtonCancel, null);
      getContentPane().add(m_DialogPanel);

      m_PurgeLocal.setSelected(MainFrame.getConfig().getIsPurgeLocalCopy());
      m_PromptBeforePurge.setSelected(MainFrame.getConfig().
            getIsPromptBeforePurge());
      m_PromptBeforeOverwrite.setSelected(MainFrame.getConfig().
            getIsPromptBeforeOverwrite());
   }


   /**
    * Acion handler for OK button. New option values are written to the
    * configuartion document.
    */
   void ButtonOK_actionPerformed(ActionEvent e)
   {
      MainFrame.getConfig().setIsPurgeLocalCopy(m_PurgeLocal.isSelected());
      MainFrame.getConfig().setIsPromptBeforePurge(
            m_PromptBeforePurge.isSelected());
      MainFrame.getConfig().setIsPromptBeforeOverwrite(
            m_PromptBeforeOverwrite.isSelected());
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
   void closeWindow(WindowEvent e)
   {
      m_exit = CANCEL;
      dispose();
   }

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
class PSFUDOptionsDlg_m_ButtonOK_actionAdapter implements ActionListener
{
   PSFUDOptionsDlg dlg;

   /**
    * Constructor taking the caller class instance.
    */
   PSFUDOptionsDlg_m_ButtonOK_actionAdapter(PSFUDOptionsDlg dlg)
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
class PSFUDOptionsDlg_m_ButtonCancel_actionAdapter implements ActionListener
{
   PSFUDOptionsDlg dlg;

   /**
    * Constructor taking the caller class instance.
    */
   PSFUDOptionsDlg_m_ButtonCancel_actionAdapter(PSFUDOptionsDlg dlg)
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

