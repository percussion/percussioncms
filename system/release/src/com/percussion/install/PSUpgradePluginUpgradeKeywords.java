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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.install;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;

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
