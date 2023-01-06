/******************************************************************************
 *
 * [ PSWizardPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

import com.percussion.cx.PSContentExplorerApplet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * An abstract base class that can be used to derive all wizard pages from. It
 * creates the correct layout and provides the controls to display the user
 * instuctions.
 */
public abstract class PSWizardPanel extends JPanel implements IPSWizardPanel
{
   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSWizardPanel(PSContentExplorerApplet applet)
   {
      m_applet = applet;
   }

   public PSWizardPanel()
   {
   }

   /**
    * Creates the panel for the supplied main panel. The panel constructed shows
    * an instruction panel on top and the supplied main panel on the botton.
    * 
    * @param mainPanel the main panel to be displayed at the bottom, may be
    *           <code>null</code> for the start and finish pages in which case a
    *           picture panel is shown to the left.
    */
   protected void initPanel(JPanel mainPanel)
   {
      m_instructions.setTabSize(2);
      m_mainPanel = mainPanel;

      setLayout(new BorderLayout());
      if (m_mainPanel == null)
      {
         add(createLeftPanel(), BorderLayout.WEST);
         add(createInstructionsPanel(), BorderLayout.CENTER);
      }
      else
      {
         add(createInstructionsPanel(), BorderLayout.NORTH);
         m_mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         add(m_mainPanel, BorderLayout.CENTER);
      }
   }

   /**
    * Create a new picture panel. This panel is shown on start and finish pages.
    * 
    * @return the new created panel, never <code>null</code>.
    */
   private JPanel createLeftPanel()
   {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(150, 0));
      panel.setBackground(Color.blue);

      return panel;
   }

   /**
    * Creates the instructions panel.
    * 
    * @return the new panel, never <code>null</code>.
    */
   private JPanel createInstructionsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBackground(Color.white);
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      m_instructions.setEditable(false);
      m_instructions.setLineWrap(true);
      m_instructions.setWrapStyleWord(true);
      m_instructions.setBackground(Color.white);

      JScrollPane scroll = new JScrollPane(m_instructions);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setPreferredSize(new Dimension(0, 80));
      scroll.setBorder(BorderFactory.createEmptyBorder());
      panel.add(scroll);

      return panel;
   }

   /**
    * By default we do not validate, must be overridden to implemment
    * validation.
    * 
    * @see IPSWizardPanel#validatePanel() for further documentation.
    */
   public void validatePanel() throws PSWizardValidationError
   {
   }

   /**
    * By default we do not skip any wizard page, must be overridden to change
    * this behavior.
    * 
    * @see IPSWizardPanel#skipNext() for further documentation.
    */
   public boolean skipNext()
   {
      return false;
   }

   /**
    * No default summary is available, must be overridden to return a page
    * specific summary.
    * 
    * @see IPSWizardPanel#getSummary() for further documentation.
    */
   public String getSummary()
   {
      return "";
   }

   /*
    * (non-Javadoc)
    * 
    * @see Object IPSWizardPanel#getData() for documentation.
    */
   public Object getData()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSWizardPanel#setData() for documentation.
    */
   public void setData(Object data)
   {
      m_data = data;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSWizardPanel#getInstruction() for documentation.
    */
   public String getInstruction()
   {
      return m_instructions.getText();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSWizardPanel#setInstruction() for documentation.
    */
   public void setInstruction(String instruction)
   {
      if (instruction == null)
         throw new IllegalArgumentException("instruction cannot be null");

      instruction = instruction.trim();
      if (instruction.length() == 0)
         throw new IllegalArgumentException("instruction cannot be empty");

      m_instructions.setText(instruction);
   }

   /**
    * The panel data with which it will be initialized, set during construction,
    * may be <code>null</code> after that.
    */
   protected Object m_data = null;

   /**
    * The instructions shown to the user on every wizard page, never
    * <code>null</code>, may be empty.
    */
   protected JTextArea m_instructions = new JTextArea();

   /**
    * The main wizard page panel, initialized in {@link #createPanel(JPanel)},
    * may be <code>null</code> for start and finish wizard pages.
    */
   protected JPanel m_mainPanel = null;

   /**
    * A reference back to the applet that initiated this action manager.
    */
   protected PSContentExplorerApplet m_applet;
}
