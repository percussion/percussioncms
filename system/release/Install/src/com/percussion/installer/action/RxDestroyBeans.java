/******************************************************************************
 *
 * [ RxDestroyBeans.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import org.quartz.SchedulerException;

import com.percussion.installanywhere.RxIAAction;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.schedule.impl.PSSchedulerBean;


/**
 * This action destroys beans before shutting down repository
 */
public class RxDestroyBeans extends RxIAAction
{
   @Override
   public void execute()
   {  
      PSBaseServiceLocator.destroy();
   }
}
