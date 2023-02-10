/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.cx;

import org.apache.log4j.Logger;



public class PSAjaxSwingWrapperLocator
{
   private static volatile IPSAjaxSwingWrapper wrapperClass;

   private static final Object lock = new Object();

   static Logger log = Logger.getLogger(PSAjaxSwingWrapperLocator.class);
   
   public static IPSAjaxSwingWrapper getInstance()
   {
      if (wrapperClass == null)
      {
         synchronized (lock)
         {
            if (wrapperClass == null && isAjaxSwingApplet())
            {
               try
               {
                  Class<?> c = Class.forName("com.percussion.ajaxswing.PSAjaxSwingWrapper");
                  wrapperClass = (IPSAjaxSwingWrapper) c.newInstance();
                  log.info("Running Applet in AjaxSwing context");
               }
               catch (ClassNotFoundException e)
               {
                  log.error("Running with AjaxSwing but com.percussion.ajaxswing.PSAjaxSwingWrapper not compiled with rxcx.",e);
                 
               }
               catch (InstantiationException e)
               {
                  log.error("Running with AjaxSwing but Cannot instantiate com.percussion.ajaxswing.PSAjaxSwingWrapper",e);

               }
               catch (IllegalAccessException e)
               {
                  log.error("Running with AjaxSwing but IllegalAccess creating instance com.percussion.ajaxswing.PSAjaxSwingWrapper",e);
               
               }
               if (wrapperClass == null)
               {
                  wrapperClass = new PSDefaultAjaxSwingWrapper();
                  log.info("Running Applet in Browser context");
               }
            }
            else {
               if (wrapperClass == null)
               {
                  wrapperClass = new PSDefaultAjaxSwingWrapper();
                  log.info("Running Applet in Browser context");
               }
            }

         }
      }
      return wrapperClass;
   }

   private static boolean isAjaxSwingApplet()
   {
      boolean exist = true;
      
      try
      {
    	  //if the class has a PSContentExplorerFrame then it is launched as an application
    	  Class<?> c = Class.forName("com.percussion.cx.PSContentExplorerFrame");
    	  if(c!= null){
    		  exist = false;
    	  }
    	 
    	  
         Class.forName("com.creamtec.ajaxswing.AjaxSwingManager");
      }
      catch (ClassNotFoundException e)
      {
         exist = false;
      }
      return exist;
   }
}
