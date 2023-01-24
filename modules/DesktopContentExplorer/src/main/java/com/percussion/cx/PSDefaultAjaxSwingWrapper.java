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

import com.percussion.util.PSHttpConnection;

import java.net.MalformedURLException;



public class PSDefaultAjaxSwingWrapper implements IPSAjaxSwingWrapper
{

   public void createAjaxSwingHandlers(PSContentExplorerApplet applet)
   {
      // DO Nothing
      
   }
   

   public void openWindow(PSHttpConnection conn, String url, String target, String style) throws MalformedURLException
   {
      // DO Nothing
      
   }
   

   public void refreshWindow(PSHttpConnection conn) throws MalformedURLException
   {
      // DO Nothing
      
   }
   
 
   public boolean isAjaxSwingEnabled()
   {
      return false;
   }


   
}
