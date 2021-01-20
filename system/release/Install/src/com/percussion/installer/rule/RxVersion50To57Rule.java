/******************************************************************************
 *
 * [ RxVersion50To57Rule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;


/**
 * This rule will return <code>true</code> when <code>evaluate</code> is invoked
 * if the installed build on the system lies between 5.0.x and 5.7.x.
 */
public class RxVersion50To57Rule extends RxVersionBuildNumberRule
{
   /**************************************************************************
    * Bean property Accessors
    **************************************************************************/
   
   @Override
   protected int getMajorVersionFrom()
   {
      return 5;
   }
      
   @Override
   protected int getMinorVersionFrom()
   {
      return 0;
   }
  
   @Override
   protected int getBuildFrom()
   {
      return -1;
   }
  
   @Override
   protected int getMajorVersionTo()
   {
      return 5;
   }
   
   @Override
   protected int getMinorVersionTo()
   {
      return 7;
   }

   @Override
   protected int getBuildTo()
   {
      return -1;
   }
}

