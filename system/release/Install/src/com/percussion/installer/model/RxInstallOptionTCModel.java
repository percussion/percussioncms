/******************************************************************************
 *
 * [ RxInstallOptionTCModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;


/**
 * This is the data model of a panel that is displayed when installing Rhythmyx.
 * It provides the user to choose one of the following options:
 * 1> Typical
 * 2> Custom
 */
public class RxInstallOptionTCModel extends RxIAModel
{
   /**
    * Constructs the option panel.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxInstallOptionTCModel(IPSProxyLocator locator)
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
      return super.queryExit();
   }

   /**
    * See {@link RxIAModel#entered()} for detailed information.
    */
   @Override
   public boolean entered()
   {
      return super.entered();
   }

 /*************************************************************************
  * Properties Accessors and Mutators
  *************************************************************************/

 /*************************************************************************
  * Public functions
  *************************************************************************/

   /**
    * Determine if the type of Rx installation is typical install.
    *
    * @return <code>true</code> if the type of Rx installation is typical
    * install, <code>false</code> otherwise
    */
   public static boolean checkTypicalInstall()
   {
      return (ms_installType == RX_INSTALL_TYPICAL) ? true : false;
   }

   /**
    * Determine if the type of Rx installation is custom install.
    *
    * @return <code>true</code> if the type of Rx installation is custom
    * install, <code>false</code> otherwise
    */
   public static boolean checkCustomInstall()
   {
      return (ms_installType == RX_INSTALL_CUSTOM) ? true : false;
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
    * Sets the type of Rx installation (typical, custom).
    *       
    * @param type the installation type, must be one of the following:
    * {@link #RX_INSTALL_TYPICAL}, {@link #RX_INSTALL_CUSTOM}.
    */
   public void setInstallType(int type)
   {
      if (type != RX_INSTALL_TYPICAL && type != RX_INSTALL_CUSTOM)
      {
         throw new IllegalArgumentException("type must be typical or custom");
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
   * Constant for Rhythmyx installation of type typical install.
   */
  static public final int RX_INSTALL_TYPICAL = 0;

  /**
   * Constant for Rhythmyx installation of type custom install.
   */
  static public final int RX_INSTALL_CUSTOM = 1;

  /**
   * type of installation. Valid values are one of <code><RX_INSTALL_xxx/code>
   * values, defaults to <code>RX_INSTALL_TYPICAL</code>
   */
  private static int ms_installType = RX_INSTALL_TYPICAL;

  /*************************************************************************
  * Member Variables
  *************************************************************************/


}
