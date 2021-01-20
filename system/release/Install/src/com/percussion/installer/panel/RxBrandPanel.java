/******************************************************************************
 *
 * [ RxBrandPanel.java ]
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
import com.percussion.installer.model.RxBrandModel;

import java.awt.Label;
import java.awt.TextField;


/**
 * AWT Implementation of {@link RxBrandModel}.
 */
public class RxBrandPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();

      RxBrandModel bm = new RxBrandModel(this);
      setModel(bm);
      
      //base class will layout components as a vertical stack,
      //so all we need here is to add them one after another!
      
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "license")));

      rxAdd(m_txtLicense = new TextField());

      rxAdd(5);
      
      rxAdd(new Label(
            RxInstallerProperties.getResources().getString("productcode")));
      
      rxAdd(m_txtBrandCode = new TextField());
      
      getDataModel().setIsBrander(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), RxBrandModel.IS_BRANDER_VAR)).
            equalsIgnoreCase("true"));
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
   }
     
   @Override
   public void exiting()
   {
      RxBrandModel data = getDataModel();
      
      data.setBrandCode(m_txtBrandCode.getText());
      data.setLicense(m_txtLicense.getText());
   }   
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      
      if (propName.equalsIgnoreCase("BrandCode"))
         m_txtBrandCode.setText(getDataModel().getBrandCode());
         
      if (propName.equalsIgnoreCase("License"))
         m_txtLicense.setText(getDataModel().getLicense());
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxBrandModel getDataModel()
   {
      return (RxBrandModel) getModel();
   }
   
   /*************************************************************************
   * UI Component Variables
   *************************************************************************/

   /**
   * The license input component.
   */
   private TextField m_txtLicense = null;

   /**
   * The product code input component.
   */
   protected TextField m_txtBrandCode = null;
}
