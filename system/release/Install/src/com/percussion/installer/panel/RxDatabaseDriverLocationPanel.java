/******************************************************************************
 *
 * [ RxDatabaseDriverLocationPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxDatabaseDriverLocationModel;
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
public class RxDatabaseDriverLocationPanel extends RxIAPanel
implements ActionListener
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      rxAdd(new Label("Please enter driver location:"));
      
      rxAdd(m_txtDriverLocation = new TextField());
      
      rxAdd("RightTopWidth=80", m_browse = new Button("Browse"));
      m_browse.addActionListener(this);
      
      RxDatabaseDriverLocationModel ddlm = new RxDatabaseDriverLocationModel(this);
      
      setModel(ddlm);
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
         chooser.setDialogTitle("Select database driver");
         
         String currentDir = m_txtDriverLocation.getText();
         if (currentDir.trim().length() > 0)
            chooser.setCurrentDirectory(new File(currentDir));
         
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         int returnVal = chooser.showOpenDialog(container);
         if (returnVal == JFileChooser.APPROVE_OPTION)
            m_txtDriverLocation.setText(chooser.getSelectedFile().getAbsolutePath());
      }
      catch (Exception exception)
      {
         logEvent(CustomError.ERROR, "err", exception);
      }
   }
   
   @Override
   protected void entered()
   {
      String driverLocation = getDM().getDriverLocation();
      m_txtDriverLocation.setText(driverLocation);
   }
   
   @Override
   protected void exiting()
   {
      getDM().setDriverLocation(m_txtDriverLocation.getText());   
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
   private RxDatabaseDriverLocationModel getDM()
   {
      return (RxDatabaseDriverLocationModel) getModel();
   }
   
   /**
    * UI component for the installation directory.
    */
   private TextField m_txtDriverLocation;
   
   /**
    * UI component for the browse button.
    */
   private Button m_browse;   
}
