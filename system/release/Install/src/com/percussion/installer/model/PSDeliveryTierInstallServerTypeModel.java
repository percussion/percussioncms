package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.RxVariables;

/**
 * This is a model that maintains the default product server type, and
 * allows the end user to change it.
 */
public class PSDeliveryTierInstallServerTypeModel extends RxIAModel
{
   static public final int PRODUCTION = 0;
   static public final int STAGING = 1;
   private static int servertype = PRODUCTION; 
   
   public PSDeliveryTierInstallServerTypeModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(true);
   }
   
   public static int getServerType()
   {
      return servertype;
   }

   public static void setServerType(int pServertype)
   {
      PSDeliveryTierInstallServerTypeModel.servertype = pServertype;
   }

}
