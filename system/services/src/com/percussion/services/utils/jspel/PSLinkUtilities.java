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
package com.percussion.services.utils.jspel;

import com.percussion.services.assembly.jexl.PSLinkUtils;

import java.net.MalformedURLException;

import org.apache.commons.lang.StringUtils;

/**
 * A class to provide link utilities to JSPs or XSL. Both JSPs and XSL allow
 * binding a function to a static method in Java.
 * 
 * @author dougrand
 */
public class PSLinkUtilities
{
   private static PSLinkUtils ms_link = new PSLinkUtils();
   
   /**
    * Get an absolute link from a relative link. 
    * @param relPath the relative path, never <code>null</code> or empty
    * @return the absolute path
    * @throws MalformedURLException
    */
   public static String getAbsLink(String relPath) throws MalformedURLException
   {
      if (StringUtils.isBlank(relPath))
      {
         throw new IllegalArgumentException("relPath may not be null or empty");
      }
      return ms_link.getAbsUrl(relPath, true);
   }
   
}
