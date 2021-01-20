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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSCloneCommandHandler;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.design.objectstore.PSContentType;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.error.PSException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class supplies cloning functionality for the different object types
 * supported by the system.
 */
public class PSCloneFactory
{
   /**
    * Private constructor to force the user to use the static methods.
    */
   private PSCloneFactory()
   {
   }
   
   /**
    * Creates a clone for the supplied source and returns its locator. The
    * user has the possiblity to override certain parameters through the
    * <code>PSCloneCommandHandler.SYS_CLONE_OVERRIDE_FIELDSET</code> request
    * parameter.
    * 
    * @param request the request used to create the clone, not
    *    <code>null</code>.
    * @param source the source object to be cloned, not <code>null</code>.
    * @param childRowMappings returns a map of child rows that need to be 
    *    followed up, may be <code>null</code> or empty. See 
    *    {@link #CHILD_ROW_MAPPINGS_PRIVATE_OBJECT} for additional information. 
    * @return the locator of the newly created clone, never <code>null</code>.
    * @throws PSCmsException for any error creating the clone.
    */
   public static PSLocator createClone(PSRequest request, PSLocator source,
      Map childRowMappings) throws PSCmsException
   {
      try
      {
         PSLocator clone = null;
         
         IPSItemEntry item = null;
         PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
         if (cache != null)
            item = cache.getItem(source.getId());
         
         if (item != null)
         {
            if (item.isFolder())
               clone = createFolderClone(request, source);
            else
               clone = createItemClone(request, source, 
                  PSCms.getNewRequestResource(new PSRequestContext(request), 
                     source), childRowMappings);
         }
         else
         {
            PSContentType contentType = PSCms.getContentType(
               new PSRequestContext(request), source);
            if (contentType == null)
               throw new PSCmsException(
                  IPSCmsErrors.CONTENTTYPE_DEFINITION_NOT_FOUND, 
                  new Object[] { "" + source.getId() });
            
            int objectType = contentType.getObjectType();
            if (objectType == PSCmsObject.TYPE_FOLDER)
               clone = createFolderClone(request, source);
            else
               clone = createItemClone(request, source, 
                  contentType.getNewURL(), childRowMappings);
         }
            
         return clone;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }
   
   /**
    * Create a new clone of the supplied source folder.
    * 
    * @param request the request used to make the clone, assumed not
    *    <code>null</code>.
    * @param source the locator of the source folder to be cloned, assumed
    *    not <code>null</code> and of type folder.
    * @return the locator of the newly created folder, never <code>null</code>.
    * @throws PSCmsException for any error creating the new clone.
    */
   private static PSLocator createFolderClone(PSRequest request, 
      PSLocator source) throws PSCmsException
   {
      PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
      
      PSKey[] locators = new PSKey[] {new PSLocator(source.getId(), 1)};
      PSFolder[] src = processor.openFolder(locators);
      
      PSFolder tgt = (PSFolder) src[0].clone();
      
      Map overrides = (Map) request.getParameterObject(
         PSCloneCommandHandler.SYS_CLONE_OVERRIDE_FIELDSET, 
         new HashMap());
      Iterator keys = overrides.keySet().iterator();
      while (keys.hasNext())
      {
         String name = (String) keys.next();
         String value = (String) overrides.get(name);
         
         if (value != null)
         {
            if (name.equals("sys_title"))
               tgt.setName(value);
            
            if (value.trim().length() > 0)
            {
               if (name.equals(IPSHtmlParameters.SYS_COMMUNITYID))
               {
                  int communityId = Integer.parseInt(value);
                  tgt.setCommunityId(communityId);
               }
               else if (name.equals(IPSHtmlParameters.SYS_LANG))
                  tgt.setLocale(value);
            }
         }
      }
      
      tgt = processor.save(tgt);
      return (PSLocator) tgt.getLocator();
   }
   
   /**
    * Create a new clone of the supplied source item.
    * 
    * @param request the request used to create the clone, assumed not 
    *    <code>null</code>.
    * @param source the item to be cloned, assumedd not <code>null</code> and
    *    of type item.
    * @param cloneResource the resource url which will be used to create the
    *    new clone, assumed not <code>null</code> or empty.
    * @param childRowMappings returns a map of child rows that need to be 
    *    followed up, may be <code>null</code> or empty. See 
    *    {@link #CHILD_ROW_MAPPINGS_PRIVATE_OBJECT} for additional information. 
    * @return the locator of the newly created item, never <code>null</code>.
    * @throws PSCmsException for any error creating the new clone.
    */
   private static PSLocator createItemClone(PSRequest request, PSLocator source, 
      String cloneResource, Map childRowMappings) throws PSCmsException
   {
      // avoid eclipse warning
      if (childRowMappings == null);
      
      try
      {
         HashMap cloneParams = new HashMap();
         cloneParams.put(IPSHtmlParameters.SYS_COMMAND,
            PSCloneCommandHandler.COMMAND_NAME);
         cloneParams.put(IPSHtmlParameters.SYS_CONTENTID, 
            Integer.toString(source.getId()));
         cloneParams.put(IPSHtmlParameters.SYS_REVISION, 
            Integer.toString(source.getRevision()));
         cloneParams.put(PSCloneCommandHandler.SYS_WFACTION,
            PSCloneCommandHandler.WF_ACTION_CHECKIN);
   
         // get and call the clone command handler for the supplied resource
         PSInternalRequest ir = PSServer.getInternalRequest(cloneResource,
            request, cloneParams, true);
         if (ir == null)
         {
            Object[] args = { cloneResource, "No request handler found." };
            throw new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }
         ir.performUpdate();
         
         Object test = ir.getRequest().getPrivateObject(
            CHILD_ROW_MAPPINGS_PRIVATE_OBJECT);
         if (test != null)
            childRowMappings = (Map) test;
   
         // create the locater for the cloned object
         return new PSLocator(ir.getRequest().getParameter(
            IPSHtmlParameters.SYS_CONTENTID), ir.getRequest().getParameter(
            IPSHtmlParameters.SYS_REVISION));
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }
   
   /**
    * The key used to store the map of inline relationship id's copied during 
    * this request as a request private object. The map key is the old inline 
    * relationship id and the map value is the new inline relationship id, both
    * as <code>String</code>. The string values have the format of 
    * <code>fieldName:childRowId</code>. Initialized during the first all to 
    * {!createCopy(int, int, int, int, PSExecutionData)}, may be 
    * <code>null</code> before.
    */
   public static final String CHILD_ROW_MAPPINGS_PRIVATE_OBJECT = 
      "childRowMappings"; 
}
