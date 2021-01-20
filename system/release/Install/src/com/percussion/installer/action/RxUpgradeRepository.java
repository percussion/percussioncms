/******************************************************************************
 *
 * [ RxUpgradeRepository.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;


/**
 * This action upgrades the repository file in case of upgrade.
 */
public class RxUpgradeRepository extends RxIAAction
{
   @Override
   public void execute()
   {
      InstallUtil.upgradeRepository(getInstallValue(RxVariables.INSTALL_DIR));
   }
}
