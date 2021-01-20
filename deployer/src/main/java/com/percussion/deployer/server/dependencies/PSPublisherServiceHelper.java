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
package com.percussion.deployer.server.dependencies;

import com.percussion.data.PSIdGenerator;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.map.HashedMap;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * A util class to interface msm with the Publisher service
 * @author vamsinukala
 *
 */
public class PSPublisherServiceHelper
{
   /**
    * getInstance() in a singleton
    */
   public PSPublisherServiceHelper()
   {
      resetContentLists();
   }

   /**
    * A convenience method to reset any loaded content lists so that we donot
    * have any stale data. MUST BE STATELESS
    */
   private void resetContentLists()
   {
      if ( m_namedContentList != null )
      {
         m_namedContentList.clear();
         m_namedContentList = null;
      }
      
      if ( m_guidContentList != null )
      {
         m_guidContentList.clear();
         m_guidContentList = null;
      }
      m_namedContentList =  new HashedMap();
      m_guidContentList  =  new HashedMap();
   }

   /**
    * @return the service handler
    */
   public IPSPublisherService getPublisherSvc()
   {
      return m_publisherSvc;
   }
      

   @SuppressWarnings("unchecked")
   private void getContentLists() throws PSDeployException
   {
      try
      {
         List<IPSContentList> cList = m_publisherSvc.findAllContentLists("");
         Iterator<IPSContentList> it = cList.iterator();
         resetContentLists();
         while(it.hasNext())
         {
            IPSContentList c = it.next();
            m_namedContentList.put(c.getName(), c);
            m_guidContentList.put(c.getGUID(), c);
         }
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e);
      }
   }
   
    
   /**
    * Given an extension name, get the extension ref
    * @param name cannot be <code>null</code> or empty
    * @param context the context for extension ref may or may not be 
    * <code>null</code>
    * @param interfacename the interface name that the ExtensionMgr needs, may
    * be <code>null</code>
    * @return the extension reference
    * @throws PSDeployException 
    * @throws PSExtensionException
    */
   @SuppressWarnings("unchecked")
   private static PSExtensionRef getExtensionRef(String name, String context,
         String interfacename) throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "Extension name may not be null or empty");
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      Iterator<PSExtensionRef> refs;
      boolean found = false;
      try
      {
         
         refs = emgr.getExtensionNames(null, context, interfacename, name);
      }
      catch (PSExtensionException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not locate extension:" + name);
      }
      PSExtensionRef ref = null;
      while (refs.hasNext() && !found)
      {
         ref = refs.next();
         if (ref.getExtensionName().equals(name))
         {
            found = true;
            break;
         }
      }
      return found == true ? ref : null;
   }

   /**
    * Util method to fetch the expander extension by name
    * @param name the extension name
    * @return the extension
    * @throws PSDeployException
    */
   public PSExtensionRef getExpanderExtensionRef(String name)
         throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         return null;
      
      return getExtensionRef(name, "global/percussion/system/",
               "com.percussion.services.publisher.IPSTemplateExpander");
   }
   
   /**
    * Given the extension name, return its reference
    * @param name the extension name may not be <code>null</code>
    * @return the extension
    * @throws PSDeployException 
    */
   public PSExtensionRef getGeneratorExtensionRef(String name)
         throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         return null;
      return getExtensionRef(name, "global/percussion/system/",
            "com.percussion.services.publisher.IPSContentListGenerator");
   }

   /**
    * Given the extension name, return its reference
    * @param name the extension name may not be <code>null</code>
    * @return the extension reference
    * @throws PSDeployException 
    */
   public static PSExtensionRef getItemFilterRuleExtensionRef(String name)
         throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         return null;
      return getExtensionRef(name, "global/percussion/itemfilter/",
            "com.percussion.services.filter.IPSItemFilterRule");
   }

   
   /**
    * Util method to return a named ContentList
    * @return a map of <contentlist_name, IPSContentList> 
    * @throws PSDeployException
    */
   public IterableMap getNamedContentListMap() throws PSDeployException
   {
      getContentLists();
      return m_namedContentList;
   }
   
   /**
   * Utility method to return a list of ContentList names, sorted by name
   * 
   * @param nameFilter a name filter, only content lists with names that include
   *            the given string will be returned. Equivalent to %filter% in
   *            SQL. never <code>null</code> but can be empty.
   * @return a list of content lists, might be empty, 
   *            but never <code>null</code>
    * @throws PSDeployException
    */
   public List<String> getAllContentListNames(String nameFilter) 
   throws PSDeployException
   {
      try
      {
         List<String> sList = m_publisherSvc.findAllContentListNames(nameFilter);
         return sList;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e);
      }
   }
   
   /**
    *  A helper method to generate an id so that Assembly/Publisher services
    *  can alter the index after deserialization of the particular type but 
    *  before saving the session.  we get PK Violations
    * @param type 
    * @param curId 
    * @return a long guid
    */
   public long getNextIdByType(PSTypeEnum type, String curId)
   {
      if ( type == null )
         throw new IllegalArgumentException("PSTypeEnum Key cannot be null");
      
      long id = -1;
      try
      {
         if (type.equals(PSTypeEnum.CONTENT_LIST) )
               id = PSIdGenerator.getNextId(PK_CONTENTLIST);
      }
      catch (SQLException e)
      {
         // All bets are off ;-)
         IPSGuid g = new PSGuid(type, curId);
         // add some sanity
         id =  g.getUUID()+ 10000;
      }
      return id;
   }
   
   
   /**
    * Util method to get content lists by GUIDs 
    * @return a Map<IPSGuid, IPSContentList>
    * @throws PSDeployException
    */
   public IterableMap getGuidContentListMap() throws PSDeployException
   {
      getContentLists();
      return m_guidContentList;
   }
   
   
   /**
    * Da assembly Service
    */
   private static IPSPublisherService m_publisherSvc = 
                           PSPublisherServiceLocator.getPublisherService();


   /**
    * A content list indexed by names
    */
   
   private IterableMap m_namedContentList = null;
   
   /**
    * A content list indexed by GUIDS, the guid is 
    */
   private IterableMap m_guidContentList = null;
   

   /**
    * the Primary key column for contentlist table, this might not be the best
    * way to do it, probably use annotation introspection
    */
   public static final String PK_CONTENTLIST = "CONTENTLISTID";

}
