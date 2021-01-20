/******************************************************************************
 *
 * [ RxRhythmyxServiceNotInstalledRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

/**
 * This rule will evaluate to <code>true</code> if the Rhythmyx service 
 * specified in installation.properties is not installed.
 */
public class RxRhythmyxServiceNotInstalledRule extends
RxRhythmyxServiceInstallRule
{
   @Override
   public boolean evaluate()
   {
      return !super.evaluate();
   }
}
