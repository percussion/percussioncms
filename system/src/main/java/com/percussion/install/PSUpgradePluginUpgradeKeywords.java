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
package com.percussion.install;

import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Converts keyword names to the form required by Rhythmyx 6.0 - unique,
 * no whitespaces.
 *
 * @author Andriy Palamarchuk
 */
public class PSUpgradePluginUpgradeKeywords extends PSSpringUpgradePluginBase
{

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Fixing Keyword Names");
      
      final List keywords =
            getContentService().findKeywordsByLabel(null, null);
      final List keywordsToFixNames = findKeywordsToFixNames(keywords);
      final Set keywordNames = getKeywordNames(keywords);
      
      for (Iterator i = keywordsToFixNames.iterator(); i.hasNext();)
      {
         final PSKeyword keyword = (PSKeyword) i.next();
         keyword.setName(PSNameSpacesUtil.removeWhitespacesFromName(
               keyword.getName(), keywordNames));
         keywordNames.add(keyword.getName());
         getContentService().saveKeyword(keyword);
      }
      
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   /**
    * Selects keywords from the list which need their names to be fixed.
    */
   private List findKeywordsToFixNames(final List keywords)
   {
      final List keywordsToFixNames = new ArrayList();
      for (Iterator i = keywords.iterator(); i.hasNext();)
      {
         final PSKeyword keyword = (PSKeyword) i.next();
         final String name = keyword.getName();
         if (!name.equals(StringUtils.deleteWhitespace(name)))
         {
            keywordsToFixNames.add(keyword);
         }
      }
      return keywordsToFixNames;
   }

   /**
    * Set of keyword names extracted from the provided keywords.
    */
   private Set getKeywordNames(final List keywords)
   {
      final Set names = new HashSet();
      for (Iterator i = keywords.iterator(); i.hasNext();)
      {
         final PSKeyword keyword = (PSKeyword) i.next();
         names.add(keyword.getName());
      }
      return names;
   }

   /**
    * Convenience method to access content service.
    */
   private IPSContentService getContentService()
   {
      return PSContentServiceLocator.getContentService();
   }

   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   /**
    * Used for logging, initialized in {@link #process(IPSUpgradeModule, Element)}
    */
   private IPSUpgradeModule m_config;
}
