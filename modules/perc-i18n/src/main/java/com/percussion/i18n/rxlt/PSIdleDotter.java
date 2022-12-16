/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.i18n.rxlt;

/**
 * This class displays idle dots to console at a regular interval. Any UI class
 * that has one or more operations consuming lot of time can extend this class
 * to display idle dots during that opertion.
 */

public class PSIdleDotter extends Thread
{
   /**
    * Implementation of the Thread's run method. Writes dots at a regular time
    * interval as long as the process is not ended and m_displayDot flag is
    * <code>true</code>.
    */
   public void run()
   {
      while(!m_processEnded)
      {
         if(!m_displayDot)
            continue;
         try
         {
            System.out.print(".");
            Thread.sleep(200L);
         }
         catch(Exception e){}
      }
   }

   /**
    * Method to set that flag to indicate dot session is ended.
    */
   public void endDotSession()
   {
      m_processEnded = true;
   }

   /**
    * Method to set that flag to display dots.
    * @param showOrStop falg to tell if the dots are to be displayed or not to.
    */
   public void showDots(boolean showOrStop)
   {
      if (!PSCommandLineProcessor.isLogEnabled())
         return;
      
      if (!PSCommandLineProcessor.areDotsEnabled())
         return;
      
      if(!m_started)
      {
         start();
         m_started = true;
      }
      m_displayDot = showOrStop;
      if(!showOrStop)
         //Just disply empty line after dot session
         System.out.println();

   }

   /**
    * Override this method to make sure the process ends at least during garbage
    * collection.
    */
   public void finlaize()
   {
      m_processEnded = true;
   }

   /**
    * Toggle switch to display or not to display idle dots.
    */
   private boolean m_displayDot = false;
   /**
    * switch to indicate that the process ended. Once this is true thread quits
    * the run() method.
    */
   private boolean m_processEnded = false;
   /**
    * Switch indicating if this thread is started or not.
    */
   private boolean m_started = false;

}
