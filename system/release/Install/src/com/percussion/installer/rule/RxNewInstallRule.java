/******************************************************************************
 *
 * [ RxNewInstallRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.action.RxUpdateUpgradeFlag;


/**
 * This rule will evaluate to <code>true</code> if this is a new install.
 */
public class RxNewInstallRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      return RxUpdateUpgradeFlag.checkNewInstall();
   }
}
