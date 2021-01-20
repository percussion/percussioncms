/******************************************************************************
 *
 * [ PSDeliveryTierNewInstallDestinationPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSSystem;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.PSDeliveryTierUpgradeFlag;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxStandAloneFlag;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.model.PSDeliveryTierInstallOptionModel;
import com.percussion.installer.model.PSDeliveryTierNewInstallDestinationModel;
import com.percussion.util.PSOsTool;
import com.percussion.utils.xml.PSEntityResolver;
import com.zerog.ia.api.pub.CustomError;

import java.awt.Button;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JFileChooser;

/**
 * @author nate
 *
 */
public class PSDeliveryTierNewInstallDestinationPanel extends RxIAPanel
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
      
      PSDeliveryTierNewInstallDestinationModel nidm = new PSDeliveryTierNewInstallDestinationModel(
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
   private PSDeliveryTierNewInstallDestinationModel getDM()
   {
      return (PSDeliveryTierNewInstallDestinationModel) getModel();
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
