package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.PSDeliveryTierInstallOptionModel;
import com.percussion.installer.model.PSDeliveryTierInstallServerTypeModel;
import com.percussion.installer.model.PSDeliveryTierNewInstallDestinationModel;
import com.percussion.installer.model.PSDeliveryTierUpgradeDestinationModel;
import com.percussion.installer.model.RxUpgradeDestinationModel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;

/**
 * AWT Implementation of {@link RxUpgradeDestinationModel}.
 */
public class PSDeliveryTierInstallServerTypePanel extends RxIAPanel
{
   /**
    * Checkbox group for displaying the server type options
    */
   private CheckboxGroup m_checkGroup = null;
   
   /**
    * Checkbox for the server type "Production"
    */
   private Checkbox m_checkProd = null;
   
   /**
    * Checkbox for the Server type "Staging"
    */
   private Checkbox m_checkStag = null;
   
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();

      m_checkGroup = new CheckboxGroup();
      
      rxAdd(m_checkProd = new Checkbox(
            RxInstallerProperties.getResources().getString("newProductionInstall"), 
            m_checkGroup, 
            true));
      
      rxAdd(m_checkStag = new Checkbox(
            RxInstallerProperties.getResources().getString("newStagingInstall"),
            m_checkGroup, 
            false));    
      
      PSDeliveryTierInstallServerTypeModel stm = new PSDeliveryTierInstallServerTypeModel(this);
      setModel(stm);
   }
   
   @Override
   protected void entering()
   {
      //by default select production checkbox
      m_checkGroup.setSelectedCheckbox(m_checkProd);
   }

   @Override
   protected void entered()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void exiting()
   {
      // TODO Auto-generated method stub
      PSDeliveryTierInstallServerTypeModel data = getDM();

       if (m_checkStag.equals(m_checkGroup.getSelectedCheckbox())){
           data.setServerType(PSDeliveryTierInstallServerTypeModel.STAGING);
           setInstallValue(RxVariables.DTS_SERVER_TYPE, "false");
       }
       else {
           data.setServerType(PSDeliveryTierInstallServerTypeModel.PRODUCTION);
           setInstallValue(RxVariables.DTS_SERVER_TYPE, "true");
       }
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private PSDeliveryTierInstallServerTypeModel getDM()
   {
      return (PSDeliveryTierInstallServerTypeModel) getModel();
   }

   
   
}
