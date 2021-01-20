/******************************************************************************
 *
 * [ RxRepositoryUpgradeChecksErrRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.install.PSUpgradePluginCheckDuplicateKeysInTables;
import com.percussion.installanywhere.RxIARule;


/**
 * This rule will evaluate to <code>true</code> if the
 * {@link PSUpgradePluginCheckDuplicateKeysInTables} upgrade plugin finished
 * with errors. 
 */
public class RxRepositoryUpgradeChecksErrRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      if (PSUpgradePluginCheckDuplicateKeysInTables.m_upgradeErrorFlag)
         return true;

      return false;
   }
}

