/******************************************************************************
 *
 * [ UTStandardCommandPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The UTStandardCommandPanel provides the standard command panel containing
 * the buttons OK, Cancel and Help.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTStandardCommandPanel extends JPanel implements ActionListener
{
   /**
    * Create the standard command panel containing an OK, Cancel and Help button.
    *
    * @param   dialog   the dialog containing this instance
    * @param helpURL the help page URL
    * @param axis    the layout axis
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTStandardCommandPanel(PSDialog dialog, String helpURL, int axis)
   {
      this( dialog, axis, true );
   }


   public UTStandardCommandPanel(PSDialog dialog, int axis, boolean showHelpButton )
   {
      m_dialog = dialog;

      try
      {
         m_res = ResourceHelper.getResources();
      }
      catch(MissingResourceException ex)
      {
         System.out.println(ex);
         throw ex;
      }

      if (axis == SwingConstants.VERTICAL)
         initVerticalPanel( showHelpButton );
      else
         initHorizontalPanel( showHelpButton );
   }

   public UTStandardCommandPanel(PSDialog dialog, int axis,
      boolean showHelpButton,  boolean showApplyButton)
   {
      m_showApplyButton = showApplyButton;
      m_dialog = dialog;
      try
      {
         m_res = ResourceHelper.getResources();
      }
      catch(MissingResourceException ex)
      {
         System.out.println(ex);
         throw ex;
      }

      if (axis == SwingConstants.VERTICAL)
         initVerticalPanel( showHelpButton );
      else
         initHorizontalPanel( showHelpButton );
   }

   /**
    * Initialize this as vertical command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is created
    * and the local m_helpButton member is initialized with the new button.
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initButtons( boolean showHelpButton )
   {
      // create ok button and handle all its actions
      m_okButton = new UTFixedButton(
         PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + "@OK"));
      m_okButton.setDefaultCapable(true);
      m_okButton.setActionCommand(m_res.getString("ok"));
      m_okButton.addActionListener(this);
      m_okButton.setMnemonic(PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(
                  getClass().getName() + ".mnemonic.OK@O").charAt(0));
      //create apply button and handle all its actions
      if (m_showApplyButton)
      {
         m_applyButton = new UTFixedButton(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@Apply"));
         m_applyButton.setDefaultCapable(true);
         m_applyButton.setActionCommand(m_res.getString("apply"));
         m_applyButton.addActionListener(this);
         m_applyButton.setMnemonic(PSI18NTranslationKeyValues.getInstance()
               .getMnemonic(getClass().getName() + "@Apply"));
      }

      // create cancel button and handle all its actions
      m_cancelButton = new UTFixedButton(
         PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + "@Cancel"));
      m_cancelButton.setActionCommand(m_res.getString("cancel"));
      m_cancelButton.addActionListener(this);
      m_cancelButton.setMnemonic(PSI18NTranslationKeyValues.getInstance().
      getTranslationValue(getClass().getName() + ".mnemonic.Cancel@C").charAt(0));
      // creating behavior that gives defaultButton focus back to the OK button
      // when focus leaves the CANCEL button.
      m_cancelButton.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            getRootPane().setDefaultButton(m_okButton);
         }
      });

      if ( showHelpButton )
      {
         // create help button and handle all its actions
         m_helpButton = new UTFixedButton(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + "@Help"));
         m_helpButton.setActionCommand(m_res.getString("help"));
         m_helpButton.addActionListener(this);
         m_helpButton.setMnemonic(PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + ".mnemonic.Help@H").charAt(0));
         // creating behavior that gives defaultButton focus back to the OK button
         // when focus leaves the HELP button.
         m_helpButton.addFocusListener(new FocusAdapter()
         {
            public void focusLost(FocusEvent e)
            {
               getRootPane().setDefaultButton(m_okButton);
            }
         });
      }
   }

   /**
    * Initialize this as vertical command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is added to
    * the panel in the standard location.
    */
   private void initVerticalPanel( boolean showHelpButton )
   {
      showHelpButton = (showHelpButton && !inEclipseRxWorkbench());
      initButtons( showHelpButton );

      Box box = new Box(BoxLayout.Y_AXIS);
      box.add(Box.createVerticalGlue());
      box.add(m_okButton);
      box.add(Box.createVerticalStrut(5));
      if (m_showApplyButton)
      {
         box.add(m_applyButton);
         box.add(Box.createVerticalStrut(5));
      }
      box.add(m_cancelButton);
      if ( showHelpButton )
      {
         box.add(Box.createVerticalStrut(5));
         box.add(m_helpButton);
      }

      this.setLayout(new BorderLayout());
      this.add(box, BorderLayout.NORTH);
   }
   
   /**
    * Determine if we are in an eclipse Rx Workbench environment to determine
    * if the help button should show.
    * @return <code>true</code> if in the Rx workbench
    */
   private boolean inEclipseRxWorkbench()
   {
      String classname = "com.percussion.workbench.ui.help.PSHelpManager";
      String methodname = "displayHelpFromLegacy";
      try
      {
         Class clazz = Class.forName(classname);
         Method method = 
            clazz.getMethod(methodname, new Class[]{String.class});
         return true;
      }
      catch (Throwable t)
      {
         // Throwable, not Exception to catch possible ClassNotFoundError too
         return false;         
      }     
   }

   /**
    * Initialize this as horizontal command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is added to
    * the panel in the standard location.
    */
   private void initHorizontalPanel( boolean showHelpButton )
   {
      showHelpButton = (showHelpButton && !inEclipseRxWorkbench());
      initButtons( showHelpButton );

      Box box = new Box(BoxLayout.X_AXIS);
      box.add(Box.createHorizontalGlue());
      box.add(m_okButton);
      box.add(Box.createHorizontalStrut(5));
      if (m_showApplyButton)
      {
         box.add(m_applyButton);
         box.add(Box.createHorizontalStrut(5));
      }
      box.add(m_cancelButton);
      if ( showHelpButton )
      {
         box.add(Box.createHorizontalStrut(5));
         box.add(m_helpButton);
      }

      this.setLayout(new FlowLayout(FlowLayout.LEFT));
      this.add(box);
   }


   /**
    * Create the dialogs command panel (OK, Cancel and Help).
    *
    * @return  JPanel, a grid panel containing the dialogs command panel
    */
   //////////////////////////////////////////////////////////////////////////////
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(m_res.getString("ok")))
      {
         onOk();
      }
      if (e.getActionCommand().equals(m_res.getString("cancel")))
      {
         onCancel();
      }
      if (e.getActionCommand().equals(m_res.getString("help")))
      {
         onHelp();
      }
      if (e.getActionCommand().equals(m_res.getString("apply")))
      {
         onApply();
      }
   }

   /**
    * Get the OK button.
    *
    * @return     JButton     the OK button
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getOkButton()
   {
      return m_okButton;
   }

   /**
    * Get the Cancel button.
    *
    * @return     JButton     the Cancel button
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getCancelButton()
   {
      return m_cancelButton;
   }

   /**
    * Get the Help button.
    *
    * @return     JButton     the Help button, which may be null
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getHelpButton()
   {
      return m_helpButton;
   }

   /**
    * The default action closes and disposes the dialog. Override this if special
    * functionality is nessecary.
    */
   public void onCancel()
   {
      m_dialog.onCancel();
   }

   /**
    * The default implementation for 'OK' in the dialog.
    */
   public void onOk()
   {
      m_dialog.onOk();
   }

   /**
    * The default implementation for 'Apply' in the dialog.
    */
   public void onApply()
   {
      m_dialog.onApply();
   }

   /**
    * The default action opens the help URL. Override this only if no help
    * functionality is nessecary.
    *
    */
   public void onHelp()
   {
      m_dialog.onHelp();
   }

   //////////////////////////////////////////////////////////////////////////////
   /*
    * class resources
    */
   private static ResourceBundle m_res = null;
   /**
    * the OK button
    */
   JButton m_okButton = null;
   /**
    * the Cancel button
    */
   private JButton m_cancelButton = null;
   /**
    * the help button. May be null if panel is created with the help button
    * flag set to <code>false</code>.
    */
   private JButton m_helpButton = null;
   /**
    * the dialog which contains this
    */
   private PSDialog m_dialog = null;

   /**
    * Initialized in the ctor, specifies if the <code>m_applyButton</code> is to
    * be shown in the panel or not.
    */
   private boolean m_showApplyButton;

   /**
    * the apply button, if <code>m_showApplyButton</code> is <code>true</code>
    * it is initialized in {@link#initButtons(boolean)}, never <code>null</code>
    * or modified after that.
    */
   private JButton m_applyButton;
}
