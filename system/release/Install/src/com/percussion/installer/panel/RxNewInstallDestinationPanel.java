/******************************************************************************
 *
 * [ RxNewInstallDestinationPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxNewInstallDestinationModel;
import com.zerog.ia.api.pub.CustomError;

import java.awt.Button;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * Panel for RxNewInstallDestinationModel.
 */
public class RxNewInstallDestinationPanel extends RxIAPanel
implements ActionListener
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      rxAdd(new Label("Directory:"));
      
      rxAdd(m_txtDestDir = new TextField());
      
      rxAdd("RightTopWidth=80", m_browse = new Button("Browse"));
      m_browse.addActionListener(this);
      
      RxNewInstallDestinationModel nidm = new RxNewInstallDestinationModel(
            this);
      setModel(nidm);
   }
   
   /**
    * See {@link ActionListener#actionPerformed(ActionEvent)} for details.
    */
   @SuppressWarnings("unused")
   public void actionPerformed(ActionEvent e)
   {
      Container container = getParent();
      while (container != null && !(container instanceof Frame))
         container = container.getParent();
      
      try
      {
         JFileChooser chooser = new JFileChooser();
         chooser.setDialogTitle("Select a directory");
         
         String currentDir = m_txtDestDir.getText();
         if (currentDir.trim().length() > 0)
            chooser.setCurrentDirectory(new File(currentDir));
         
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         int returnVal = chooser.showOpenDialog(container);
         if (returnVal == JFileChooser.APPROVE_OPTION)
            m_txtDestDir.setText(chooser.getSelectedFile().getAbsolutePath());
      }
      catch (Exception exception)
      {
         logEvent(CustomError.ERROR, "err", exception);
      }
   }
   
   @Override
   protected void entered()
   {
      m_txtDestDir.setText(getDM().getDestDir());
   }
   
   @Override
   protected void exiting()
   {
      getDM().setDestDir(m_txtDestDir.getText());      
   }
   
   @Override
   protected void entering()
   {
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxNewInstallDestinationModel getDM()
   {
      return (RxNewInstallDestinationModel) getModel();
   }
   
   /**
    * UI component for the installation directory.
    */
   private TextField m_txtDestDir;
   
   /**
    * UI component for the browse button.
    */
   private Button m_browse;   
}
