/******************************************************************************
 *
 * [ PSWizardCommandPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class provides a command panel as used in wizard dialogs. It will
 * handle the wizard buttons 'Next', 'Back', 'Finish' and 'Cancel'.
 */
public class PSWizardCommandPanel extends JPanel implements ActionListener
{
   /* (non-Javadoc)
    * @see ActionListener#actionPerformed(ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(COMMAND_BACK))
         m_dialog.onBack();
      else if (e.getActionCommand().equals(COMMAND_NEXT))
         m_dialog.onNext();
      else if (e.getActionCommand().equals(COMMAND_CANCEL))
         m_dialog.onCancel();
      else if (e.getActionCommand().equals(COMMAND_FINISH))
         m_dialog.onFinish();
   }

   /**
    * Constructs a new wizard panel.
    * 
    * @param dialog the wizard dialog instance for which to create the command
    *    panel, not <code>null</code>, must be an instance of 
    *    <code>IPSWizardDialog</code>.
    */
   public PSWizardCommandPanel(IPSWizardDialog dialog)
   {
      if (!(dialog instanceof IPSWizardDialog))
         throw new IllegalArgumentException(
            "dialog cannot be null and must be an instanceof IPSWizardDialog");
      
      m_dialog = dialog;
      initPanel();
   }
   
   /**
    * Updates all panel controls according to the supplied page type. Depending
    * on the panel type, buttons are enabled/disabled and/or renamed.
    * 
    * @param type the page type for which to update the panel controls, must 
    *    be one of <code>IPSWizardDialog.TYPE_XXX</code>.
    */
   public void updateControls(int type)
   {
      validateType(type);
      
      switch (type)
      {
         case IPSWizardDialog.TYPE_FIRST:
            m_back.setEnabled(false);
            m_next.setText(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + "@Next >"));
            m_next.setMnemonic(PSI18NTranslationKeyValues.getInstance().
               getMnemonic(getClass().getName() + "@Next >"));
            m_next.setActionCommand(COMMAND_NEXT);
            break;
            
         case IPSWizardDialog.TYPE_MID:
            m_back.setEnabled(true);
            m_next.setText(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + "@Next >"));
            m_next.setMnemonic(PSI18NTranslationKeyValues.getInstance().
               getMnemonic(getClass().getName() + "@Next >"));
            m_next.setActionCommand(COMMAND_NEXT);
            break;
            
         case IPSWizardDialog.TYPE_LAST:
            m_back.setEnabled(true);
            m_next.setText(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + "@Finish"));
            m_next.setMnemonic(PSI18NTranslationKeyValues.getInstance().
               getMnemonic(getClass().getName() + "@Finish"));
            m_next.setActionCommand(COMMAND_FINISH);
            break;
      }
      
      invalidate();
   }
   
   /**
    * Validates the supplied panel type. Throws an 
    * <code>IllegalArgumentException</code> if the supplied type is invalid.
    * 
    * @param type the panel type to be validated.
    */
   private void validateType(int type)
   {
      for (int i=0; i<IPSWizardDialog.TYPES.length; i++)
      {
         if (IPSWizardDialog.TYPES[i] == type)
            return;
      }
      
      throw new IllegalArgumentException(
         "type must be one of the IPSWizardDialog.TYPE_XXX values");
   }
   
   /**
    * Initializes the panel UI.
    */
   private void initPanel()
   {
      m_back = new UTFixedButton(PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + "@< Back"));
      m_back.setMnemonic(PSI18NTranslationKeyValues.getInstance().
         getMnemonic(getClass().getName() + "@< Back"));
      m_back.setActionCommand(COMMAND_BACK);
      m_back.addActionListener(this);
      m_back.setDefaultCapable(true);
      m_back.setEnabled(false);
      
      m_next = new UTFixedButton(PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + "@Next >"));
      m_next.setMnemonic(PSI18NTranslationKeyValues.getInstance().
         getMnemonic(getClass().getName() + "@Next >"));
      m_next.setActionCommand(COMMAND_NEXT);
      m_next.addActionListener(this);
      m_next.setDefaultCapable(true);
      
      m_cancel = new UTFixedButton(PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + "@Cancel"));
      m_cancel.setMnemonic(PSI18NTranslationKeyValues.getInstance().
         getMnemonic(getClass().getName() + "@Cancel"));
      m_cancel.setActionCommand(COMMAND_CANCEL);
      m_cancel.addActionListener(this);
      
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      
      add(createDividerPanel());
      add(createCommandPanel());
   }
   
   /**
    * Creates the command panel with the three buttons 'next', 'back' and
    * 'cancel'.
    * 
    * @return the new panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      
      Box box = new Box(BoxLayout.X_AXIS);
      
      box.add(Box.createHorizontalGlue());
      box.add(m_back);
      box.add(Box.createHorizontalStrut(2));
      box.add(m_next);
      box.add(Box.createHorizontalStrut(10));
      box.add(m_cancel);

      panel.add(box);
      return panel;
   }
   
   /**
    * Creates the divider panel which is just a horizontal gray line to divide
    * the command panel optically from the page panel.
    * 
    * @return the new panel, never <code>null</code>.
    */
   private JPanel createDividerPanel()
   {
      JPanel panel = new JPanel(new FlowLayout());
      panel.setPreferredSize(new Dimension(0, 1));
      panel.setBackground(Color.gray);
      
      return panel;
   }
   
   /**
    * A reference to the wizard dialog which contains this panel. Initialized 
    * during construction, never <code>null</code> or changed after that. 
    */
   private IPSWizardDialog m_dialog = null;
   
   /**
    * The command panel 'Back' button, initialized in {@link #initPanel()}, 
    * never <code>null</code> after that.
    */
   private JButton m_back = null;
   
   /**
    * The command panel 'Next' button, initialized in {@link #initPanel()}, 
    * never <code>null</code> after that. This button changes its name to
    * 'Finish' if we reached the last wizard page.
    */
   private JButton m_next = null;
   
   /**
    * The command panel 'Cancel' button, initialized in {@link #initPanel()}, 
    * never <code>null</code> after that.
    */
   private JButton m_cancel = null;
   
   /**
    * The 'Back' button action command string. 
    */
   private static String COMMAND_BACK = "Back";
   
   /**
    * The 'Next' button action command string. 
    */
   private static String COMMAND_NEXT = "Next";
   
   /**
    * The 'Cancel' button action command string. 
    */
   private static String COMMAND_CANCEL = "Cancel";
   
   /**
    * The 'Finish' button action command string. 
    */
   private static String COMMAND_FINISH = "OK";
}
