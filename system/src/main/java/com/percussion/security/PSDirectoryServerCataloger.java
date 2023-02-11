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

package com.percussion.security;

import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

   private static final Logger log = LogManager.getLogger(PSDirectoryServerCataloger.class);
   
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
      String dirName = "";
      while (result == null && directories.hasNext())
      {
         try {
            PSDirectoryDefinition directory =
                    (PSDirectoryDefinition) directories.next();
            dirName = directory.getDirectory().getName();
            result = getAttribute(directory, user.getName(),
                    getObjectAttributeName(), attributeName);
          }catch (Exception e) {
            log.error("Error finding users for ldap Directory:{} : Error: {}", dirName, PSExceptionUtils.getMessageForLog(e));
         }
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
      String dirName = "";
      while (directories.hasNext()) {
         try{
            PSDirectoryDefinition directory =
                    (PSDirectoryDefinition) directories.next();
            dirName = directory.getDirectory().getName();
            Collection attributeNames = directory.getDirectory().getAttributes();

            getAttributes(user, attributeNames);
         }catch (Exception e) {
            log.error("Error finding users for ldap Directory:{} : Error: {}", dirName, PSExceptionUtils.getMessageForLog(e));
         }
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
      String dirName = "";
      while (directories.hasNext())
      {
         try {
            PSDirectoryDefinition directory =
                    (PSDirectoryDefinition) directories.next();
            dirName = directory.getDirectory().getName();
            getAttributes(directory, user.getName(), getObjectAttributeName(),
                    returningAttrs, searchResults);
         }catch(Exception e){
            log.debug("Auth Failed for directory {}. Error : {} ",dirName,PSExceptionUtils.getDebugMessageForLog(e));
         }
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
         Map<String, List<String>> filter = new HashMap<>();
         for (int i = 0; i < criteria.length; i++) {
            Map<String, String> aFilter = createFilter(criteria[i]);
            for (Map.Entry<String, String> entry : aFilter.entrySet()) {
               String val = PSJndiUtils.processFilter(entry.getValue());
               List valList = filter.get(entry.getKey());
               if (valList == null) {
                  valList = new ArrayList<String>();
                  filter.put(entry.getKey(), valList);
               }
               valList.add(val);
            }
            int errorLoadingDir=0;
            int dirSize = 0;
            Exception ex = null;
            if ((i % 100 == 0) || i == criteria.length - 1) {

               Iterator directories = getDirectories().values().iterator();
               String dirName = "";
               while (directories.hasNext()) {
                  try{
                     dirSize++;
                     PSDirectoryDefinition directory =
                             (PSDirectoryDefinition) directories.next();
                     dirName = directory.getDirectory().getName();
                     Collection subs = getSubjects(directory, filter, attributeNames);
                     result.addAll(subs);
                  }catch (Exception e){
                     log.error("Error finding users for ldap Directory:{} : Error: {}" ,dirName, PSExceptionUtils.getMessageForLog(e));
                     log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                     errorLoadingDir++;
                     ex = e;
                  }
               }
               filter.clear();
            }
            //throw an exception if all ldap directories fail to load, incase only a few of those fails to load,
            //then just log the error.
            if(dirSize != 0 && dirSize == errorLoadingDir){
               throw new PSSecurityException(IPSSecurityErrors.UNKNOWN_NAMING_ERROR,
                       new String[]{PSExceptionUtils.getDebugMessageForLog(ex)}, ex);
            }
         }
      return result;
   }
}
