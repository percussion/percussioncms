package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxUpdateUpgradeFlag;

public class RxCommunityInstallOptionModel extends RxIAModel
{

   public RxCommunityInstallOptionModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(true);
      
   }
   
   /**
    * See {@link RxIAModel#entered()} for detailed information.
    */
   @Override
   public boolean entered()
   {
      setInstallType(RxUpdateUpgradeFlag.checkNewInstall() ?
            RX_RECOMMENDED : RX_CUSTOM);
         
      return super.entered();
   }
   
   @Override
   public boolean queryExit()
   {
      
      return super.queryExit();
   }
   
   @Override
   public String getTitle()
   {
      return "Installation Type";
   }
   
   /**
    * Determine if the type of CM1 installation is custom install.
    *
    * @return <code>true</code> if the type of cm1 installation is custom
    * install, <code>false</code> otherwise
    */
   public static boolean checkCustomInstall()
   {
      return (ms_Type == RX_CUSTOM) ? true : false;
   }
   
   /**
    * Determine if the type of CM1 installation is recommended install.
    *
    * @return <code>true</code> if the type of cm1 installation is recommended install,
    * <code>false</code> otherwise
    */
   public static boolean checkRecommendedInstall()
   {
      return (ms_Type == RX_RECOMMENDED) ? true : false;
   }

   
   
   
   /**
    * Sets the type of CM1 installation (custom or recommended).
    *       
    * @param type the installation type, must be one of the following:
    * {@link #RX_RECOMMENDED}, {@link #RX_CUSTOM}
    */
   public void setInstallType(int type)
   {
      if (type != RX_RECOMMENDED && type != RX_CUSTOM)
      {
         throw new IllegalArgumentException("type must be recommended or custom");
      }
      
      ms_Type = type;
   }
   
   /**
    * Returns the type of CM1 installation.
    *
    * @return the type of CM1 installation, one of <code>RX_INSTALL_xxx</code>
    * values
    */
   public static int fetchInstallType()
   {
      return ms_Type;
   }
   
    
   /**
    * Constant for CM1 installation of type recommended install.
    */
   static public final int RX_RECOMMENDED = 0;

   /**
    * Constant for CM1 installation of type custom install.
    */
   static public final int RX_CUSTOM = 1;

   /**
    * type of installation. Valid values are one of <code><RX_xxx/code>
    * values, defaults to <code>RX_INSTALL_NEW</code>
    */
   private static int ms_Type = RX_CUSTOM;

}
