package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxCommunityInstallOptionModel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;

public class RxCommunityInstallOptionPanel extends RxIAPanel
{
   
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      String lblReco =
         RxInstallerProperties.getResources().getString("recommendedRxInstall");
      String lblCus =
         RxInstallerProperties.getResources().getString("customRxInstall");
      
      m_checkGroup = new CheckboxGroup();
      
      rxAdd(m_checkRecommended = new Checkbox(lblReco, m_checkGroup, true));
      
      rxAdd(m_checkCustom = new Checkbox(lblCus, m_checkGroup, false));
      
      RxCommunityInstallOptionModel iom = new RxCommunityInstallOptionModel(this);
      setModel(iom);
            
   }

   @Override
   protected void entering()
   {
      m_checkGroup.setSelectedCheckbox(getDataModel().checkRecommendedInstall() ? m_checkRecommended : m_checkCustom);
      
   }

   @Override
   protected void entered()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void exiting()
   {
      RxCommunityInstallOptionModel data = getDataModel();
            
      if (m_checkRecommended.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxCommunityInstallOptionModel.RX_RECOMMENDED);
      else if (m_checkCustom.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxCommunityInstallOptionModel.RX_CUSTOM);      
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxCommunityInstallOptionModel getDataModel()
   {
      return (RxCommunityInstallOptionModel) getModel();
   }
   
   
   
   /*************************************************************************
    * UI Component Variables
    *************************************************************************/
   
   /**
    * Checkbox group for displaying the Rhythmyx installation options,
    * initialized in the <code>initialize()</code> method, never
    * <code>null</code> or modified after that.
    */
   
   private CheckboxGroup m_checkGroup = null;
   
   /**
    * Checkbox for the new installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkRecommended = null;
   
   /**
    * Checkbox for the upgrade installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkCustom = null;

}
