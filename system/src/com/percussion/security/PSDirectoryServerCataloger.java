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

package com.percussion.security;

import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * A cataloger using a directory server as source.
 */
public class PSDirectoryServerCataloger extends PSDirectoryCataloger
{
   /** @see PSCataloger */
   public PSDirectoryServerCataloger(Properties properties)
   {
      super(properties);
   }
   
   /** @see PSCataloger */
   public PSDirectoryServerCataloger(Properties properties, 
      PSServerConfiguration config)
   {
      super(properties, config);
   }
   
   // see IPSDirectoryCataloger interface 
   public String getCatalogerType()
   {
      return "directorySet";
   }   

   // see IPSDirectoryCataloger interface 
   public String getCatalogerDisplayType()
   {
      return "Directory Provider";
   }      
   
   /** @see IPSDirectoryCataloger */
   public String getAttribute(PSSubject user, String attributeName)
   {
      if (user == null)
         throw new IllegalArgumentException("user cannot be null");
         
      if (attributeName == null)
         throw new IllegalArgumentException("attributeName cannot be null");

      attributeName = attributeName.trim();
      if (attributeName.length() == 0)
         throw new IllegalArgumentException("attributeName cannot be empty");

      String result = null;
      Iterator directories = getDirectories().values().iterator();
      while (result == null && directories.hasNext())
      {
         PSDirectoryDefinition directory = 
            (PSDirectoryDefinition) directories.next();
         result = getAttribute(directory, user.getName(), 
            getObjectAttributeName(), attributeName);
      }
      
      return result;
   }
   
   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(PSSubject user)
   {
      if (user == null)
         throw new IllegalArgumentException("user cannot be null");

      // do the search for all configured directories
      Iterator directories = getDirectories().values().iterator();
      while (directories.hasNext())
      {
         PSDirectoryDefinition directory = 
            (PSDirectoryDefinition) directories.next();
         Collection attributeNames = directory.getDirectory().getAttributes();
         
         getAttributes(user, attributeNames);
      }
      
      return user;
   }

   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(PSSubject user, Collection attributeNames)
   {
      if (user == null)
         throw new IllegalArgumentException("user cannot be null");

      // prepare returning attributes array
      String[] returningAttrs = null;
      if (attributeNames != null && !attributeNames.isEmpty())
         returningAttrs = (String[]) attributeNames.toArray(new String[0]);

      // initialize the search results map
      Map searchResults = new HashMap();
      
      // do the search for all configured directories
      Iterator directories = getDirectories().values().iterator();
      while (directories.hasNext())
      {
         PSDirectoryDefinition directory = 
            (PSDirectoryDefinition) directories.next();
         getAttributes(directory, user.getName(), getObjectAttributeName(), 
            returningAttrs, searchResults);
      }
      
      // set attributes in returned subject
      PSAttributeList attributes = user.getAttributes();
      Iterator keys = searchResults.keySet().iterator();
      while (keys.hasNext())
      {
         String key = (String) keys.next();
         attributes.setAttribute(key, (List) searchResults.get(key));
      }
      
      return user;
   }
   
   /** @see IPSDirectoryCataloger */
   public Collection findUsers(PSConditional[] criteria,
      Collection attributeNames)
   {
      Collection result = new ArrayList();
      
      if (criteria == null)
         criteria = new PSConditional[] {null};
      
      // walk criteria, and build list of conditionals, searching every 1000
      Map<String, List<String>> filter = new HashMap<String, List<String>>();
      for (int i = 0; i < criteria.length; i++)
      {
         Map<String, String> aFilter = createFilter(criteria[i]);
         for (Map.Entry<String, String> entry : aFilter.entrySet())
         {
            String val = PSJndiUtils.processFilter(entry.getValue());
            List valList = filter.get(entry.getKey());
            if (valList == null)
            {
               valList = new ArrayList<String>();
               filter.put(entry.getKey(), valList);
            }
            valList.add(val);
         }
         
         if ((i % 100 == 0) || i == criteria.length - 1)
         {
            Iterator directories = getDirectories().values().iterator();
            while (directories.hasNext())
            {
               PSDirectoryDefinition directory = 
                  (PSDirectoryDefinition) directories.next();
               result.addAll(getSubjects(directory, filter, attributeNames));
            }
            filter.clear();
         }
      }      
      
      return result;
   }
}
