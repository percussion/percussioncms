/******************************************************************************
 *
 * [ PSDeliveryTierInstallOptionModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.PSDeliveryTierUpgradeFlag;

/**
 * @author nate
 *
 */
public class PSDeliveryTierInstallOptionModel extends RxIAModel
{
   /**
    * Constructs the option panel.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public PSDeliveryTierInstallOptionModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   /**
    * See {@link RxIAModel#queryExit()} for detailed information.
    */
   @Override
   public boolean queryExit()
   {
      switch (ms_installType)
      {
         case INSTALL_NEW:
            PSDeliveryTierUpgradeFlag.newInstall();
            break;

         case INSTALL_UPGRADE:
            PSDeliveryTierUpgradeFlag.upgradeInstall();
            break;
      }
      return super.queryExit();
   }

   /**
    * See {@link RxIAModel#entered()} for detailed information.
    */
   @Override
   public boolean entered()
   {
      setInstallType(PSDeliveryTierUpgradeFlag.checkNewInstall() ?
         INSTALL_NEW : INSTALL_UPGRADE);
         
      return super.entered();
   }

 /*************************************************************************
  * Properties Accessors and Mutators
  *************************************************************************/

 /*************************************************************************
  * Public functions
  *************************************************************************/

   /**
    * Determine if the type of Rx installation is new install.
    *
    * @return <code>true</code> if the type of Rx installation is new install,
    * <code>false</code> otherwise
    */
   public static boolean checkNewInstall()
   {
      return (ms_installType == INSTALL_NEW) ? true : false;
   }

   /**
    * Determine if the type of Rx installation is upgrade install.
    *
    * @return <code>true</code> if the type of Rx installation is upgrade
    * install, <code>false</code> otherwise
    */
   public static boolean checkUpgradeInstall()
   {
      return ((ms_installType == INSTALL_UPGRADE) ||
         (ms_installType == INSTALL_UPGRADE_OTHER)) ? true : false;
   }

   /**
    * Returns the type of Rx installation.
    *
    * @return the type of Rx installation, one of <code>RX_INSTALL_xxx</code>
    * values
    */
   public static int fetchInstallType()
   {
      return ms_installType;
   }

   /**
    * Sets the type of Rx installation to new install.
    */
   public static void newInstall()
   {
      ms_installType = INSTALL_NEW;
   }

   /**
    * Sets the type of Rx installation to upgrade install.
    *
    * @param other <code>true</code> if the user selected "Other..." in the
    * combox box displaying the existing Rhythmyx installations,
    * <code>false</code> otherwise.
    */
   public static void upgradeInstall(boolean other)
   {
      ms_installType = other ? INSTALL_UPGRADE_OTHER : INSTALL_UPGRADE;
   }
   
   /**
    * Gets the type of Rx installation.  Will be one of three values:
    * {@link #INSTALL_NEW}, {@link #INSTALL_UPGRADE}, or
    * {@link #INSTALL_UPGRADE_OTHER}.
    * 
    * @return the installation type (new, upgrade, upgrade other).
    */
   protected int getInstallType()
   {
      return ms_installType;
   }
   
   /**
    * Sets the type of Rx installation (new, upgrade, upgrade other).
    *       
    * @param type the installation type, must be one of the following:
    * {@link #INSTALL_NEW}, {@link #INSTALL_UPGRADE}, or
    * {@link #INSTALL_UPGRADE_OTHER}.
    */
   public void setInstallType(int type)
   {
      if (type != INSTALL_NEW && type != INSTALL_UPGRADE &&
            type != INSTALL_UPGRADE_OTHER)
      {
         throw new IllegalArgumentException("type must be new, upgrade, or " +
               "upgrade other");
      }
      
      ms_installType = type;
   }

   @Override
   public String getTitle()
   {
      return "Installation Type";
   }
   
   @Override
   protected boolean validateModel()
   {
      return true;
   }   

 /*************************************************************************
  * Private functions
  *************************************************************************/

 /*************************************************************************
  * Properties
  *************************************************************************/

  /*************************************************************************
  * Static Variables
  *************************************************************************/

  /**
   * Constant for installation of type new install.
   */
  static public final int INSTALL_NEW = 0;

  /**
   * Constant for installation of type upgrade install.
   */
  static public final int INSTALL_UPGRADE = 1;

  /**
   * Constant for installation of type upgrade install where the
   * user selects "Other..." in the combox box displaying the existing
   * installations
   */
  static public final int INSTALL_UPGRADE_OTHER = 2;

  /**
   * type of installation. Valid values are one of <code><RX_INSTALL_xxx/code>
   * values, defaults to <code>RX_INSTALL_NEW</code>
   */
  private static int ms_installType = INSTALL_NEW;


}
