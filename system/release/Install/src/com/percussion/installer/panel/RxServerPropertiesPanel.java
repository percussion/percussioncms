/******************************************************************************
 *
 * [ RxServerPropertiesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.util.PSOsTool;

import java.awt.Checkbox;
import java.awt.Label;
import java.awt.TextField;
import javax.swing.JTextArea;


/**
 * AWT Implementation of {@link RxServerPropertiesModel}.
 */
public class RxServerPropertiesPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxServerPropertiesModel spm = new RxServerPropertiesModel(this);
      setModel(spm);
      
      if (PSOsTool.isWindowsPlatform())
      {
         String strSvcNameLabel = RxInstallerProperties.getResources().
         getString(InstallUtil.RHYTHMYX_SVC_NAME);
         
         rxAdd(new Label(strSvcNameLabel));
         rxAdd(m_txtSvcName);
         
         rxAdd(2);
         
         String strSvcDescLabel = RxInstallerProperties.getResources().
         getString(InstallUtil.RHYTHMYX_SVC_DESC);
         
         rxAdd(new Label(strSvcDescLabel));
         rxAdd(m_txtSvcDesc);
         
         rxAdd(2);
      }
      
      rxAdd(new Label(RxInstallerProperties.getString(
      "RxPortPanelBean.BindPort.label")));
      rxAdd(m_txtBindPort = new TextField(getDM().getBindPort()));  
      
      JTextArea note = new JTextArea(RxInstallerProperties.getString(
            "additionalPortsNote"));
      note.setBackground(super.getBackground());
      note.setEditable(false);
      note.setLineWrap(true);
      note.setWrapStyleWord(true);
      rxAdd(note);
      
      m_chkConfigurePorts = new Checkbox(RxInstallerProperties.getString(
            "customizePorts"));            
      m_chkConfigurePorts.setBackground(super.getBackground());                  
      rxAdd(m_chkConfigurePorts);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      m_txtSvcName.setText(getDM().getRhythmyxSvcName());
      m_txtSvcDesc.setText(getDM().getRhythmyxSvcDesc());
      m_txtBindPort.setText(getDM().getBindPort());
      m_chkConfigurePorts.setState(getInstallValue(
            RxVariables.RX_CUSTOMIZE_PORTS).equalsIgnoreCase("true"));
   }
   
   @Override
   public void exiting()
   {
      getDM().setRhythmyxSvcName(m_txtSvcName.getText());
      getDM().setRhythmyxSvcDesc(m_txtSvcDesc.getText());
      getDM().setBindPort(m_txtBindPort.getText());
      
      if (m_chkConfigurePorts.getState())
         setInstallValue(RxVariables.RX_CUSTOMIZE_PORTS, "true");
      else
         setInstallValue(RxVariables.RX_CUSTOMIZE_PORTS, "false");
   }
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      
      if (propName.compareTo(InstallUtil.RHYTHMYX_SVC_NAME) == 0 )
         m_txtSvcName.setText(getDM().getRhythmyxSvcName());
      else if (propName.compareTo(InstallUtil.RHYTHMYX_SVC_DESC) == 0 )
         m_txtSvcDesc.setText(getDM().getRhythmyxSvcDesc());
      else if (propName.equalsIgnoreCase("BindPort"))
         m_txtBindPort.setText(getDM().getBindPort());
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link RxServerPropertiesModel}.
    * 
    * @return the data model for this panel.
    */
   private RxServerPropertiesModel getDM()
   {
      return (RxServerPropertiesModel) getModel();
   }
   
   /**
    * The service name text component.
    */
   private TextField m_txtSvcName = new TextField();
   
   /**
    * The service description text component.
    */
   private TextField m_txtSvcDesc = new TextField();
   
   /**
    * The server port text component.
    */
   private TextField m_txtBindPort;
   
   /**
    * The additional ports configuration checkbox component.
    */
   private Checkbox m_chkConfigurePorts;
}
