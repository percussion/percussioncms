/******************************************************************************
 *
 * [ RxInstallOptionModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxUpdateUpgradeFlag;


/**
 * This is the data model of a panel that is displayed when installing Rhythmyx.
 * It provides the user to choose one of the following options:
 * 1> New install
 * 2> Upgrade existing install
 */
public class RxInstallOptionModel extends RxIAModel
{
   /**
    * Constructs the option panel.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxInstallOptionModel(IPSProxyLocator locator)
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
         case RX_INSTALL_NEW:
            RxUpdateUpgradeFlag.newInstall();
            break;

         case RX_INSTALL_UPGRADE:
            RxUpdateUpgradeFlag.upgradeInstall();
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
      setInstallType(RxUpdateUpgradeFlag.checkNewInstall() ?
         RX_INSTALL_NEW : RX_INSTALL_UPGRADE);
         
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
      return (ms_installType == RX_INSTALL_NEW) ? true : false;
   }

   /**
    * Determine if the type of Rx installation is upgrade install.
    *
    * @return <code>true</code> if the type of Rx installation is upgrade
    * install, <code>false</code> otherwise
    */
   public static boolean checkUpgradeInstall()
   {
      return ((ms_installType == RX_INSTALL_UPGRADE) ||
         (ms_installType == RX_INSTALL_UPGRADE_OTHER)) ? true : false;
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
      ms_installType = RX_INSTALL_NEW;
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
      ms_installType = other ? RX_INSTALL_UPGRADE_OTHER : RX_INSTALL_UPGRADE;
   }
   
   /**
    * Gets the type of Rx installation.  Will be one of three values:
    * {@link #RX_INSTALL_NEW}, {@link #RX_INSTALL_UPGRADE}, or
    * {@link #RX_INSTALL_UPGRADE_OTHER}.
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
    * {@link #RX_INSTALL_NEW}, {@link #RX_INSTALL_UPGRADE}, or
    * {@link #RX_INSTALL_UPGRADE_OTHER}.
    */
   public void setInstallType(int type)
   {
      if (type != RX_INSTALL_NEW && type != RX_INSTALL_UPGRADE &&
            type != RX_INSTALL_UPGRADE_OTHER)
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
   * Constant for Rhythmyx installation of type new install.
   */
  static public final int RX_INSTALL_NEW = 0;

  /**
   * Constant for Rhythmyx installation of type upgrade install.
   */
  static public final int RX_INSTALL_UPGRADE = 1;

  /**
   * Constant for Rhythmyx installation of type upgrade install where the
   * user selects "Other..." in the combox box displaying the existing
   * Rhythmyx installations
   */
  static public final int RX_INSTALL_UPGRADE_OTHER = 2;

  /**
   * type of installation. Valid values are one of <code><RX_INSTALL_xxx/code>
   * values, defaults to <code>RX_INSTALL_NEW</code>
   */
  private static int ms_installType = RX_INSTALL_NEW;

  /*************************************************************************
  * Member Variables
  *************************************************************************/


}
