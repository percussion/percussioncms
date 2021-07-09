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
package com.percussion.cms.objectstore.server;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSCataloger;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentDefProcessorProxy;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSContentTypeSet;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.server.util.PSFieldFinderUtil;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.IPSWebServicesErrors;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class maintains a registry of all active content types. A content type
 * is a collection of the various design elements that compose a content item.
 * The goal is to collect all of the pieces that are distributed in various
 * places including the file system and multiple tables in the repository. These
 * elements include:
 * <ul>
 * <li>Content editor definition (stored in an Rx application)</li>
 * <li>Content type definition (stored in the CONTENTTYPES table)</li>
 * <li>Slots assigned to this content type, (which include variants assigned to
 * that slot)</li>
 * </ul>
 * <p>
 * This registry provides acccess only to content types that have a running
 * application, and thus can be used to create and access content.
 * <p>
 * This class provides access to 2 types of objects, what is known as a summary
 * and the complete definition for a content type. The summary provides a name
 * and description and is typically useful for generating UIs.
 * <p>
 * Each list can be filtered based on the community. When trying to get a
 * definition, the type they are trying to get must be visible to the community
 * to which the requestor is logged in or they will not see those.
 */
public class PSItemDefManager
{
   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger(PSItemDefManager.class);
   
   /**
    * This class implements the singleton pattern. The single instance of this
    * class is available using this method. A long lived object should call this
    * method and save the returned reference for the duration of the program to
    * prevent garbage collection of it.
    * <p>
    * The instance is created the first time this method is called.
    * 
    * @return The only instance of this class, never <code>null</code>.
    */
   public static PSItemDefManager getInstance()
   {
      return ms_instance;
   }

   /**
    * Searches every registered content type identified by <code>ctypeIds
    * </code>
    * for a field whose name matches <code>fieldName</code>. This is done by
    * calling {@link PSItemDefinition#getFieldByName(String)
    * PSItemDefinition.getFieldByName(fieldName)} on each registered content
    * editor.
    * 
    * @param ctypeIds An array of 0 or more content type identifiers within
    *           which to search. If <code>null</code> or empty, all registered
    *           types are searched. If a supplied id is not registered, it is
    *           skipped. No community filtering is done.
    * 
    * @param fieldName The name of fields to retrieve. A case-insensitive
    *           compare is performed. Never <code>null</code> or empty.
    * 
    * @return A collection of <code>PSField</code> objects, each of which has
    *         a name equal to <code>fieldName</code> (case insensitive). Never
    *         <code>null</code>, may be empty if no matches are found.
    */
   public Collection<PSField> getFieldsByName(long[] ctypeIds, String fieldName)
   {
      if (null == fieldName || fieldName.trim().length() == 0)
      {
         throw new IllegalArgumentException("fieldName cannot be null or empty");
      }

      if (ctypeIds == null || ctypeIds.length == 0)
         ctypeIds = getContentTypeIds(COMMUNITY_ANY);

      Collection<PSField> fieldDefs = new ArrayList<>();
      for (int i = 0; i < ctypeIds.length; i++)
      {
         long ctypeId = ctypeIds[i];
         if (!isRegisteredItemType(ctypeId))
            continue;

         try
         {
            PSItemDefinition def = getItemDef(ctypeId, COMMUNITY_ANY);
            PSField f = def.getFieldByName(fieldName);
            if (null != f)
               fieldDefs.add(f);
         }
         catch (PSInvalidContentTypeException e)
         { /* this will never happen as we just made sure it is a valid id */
         }
      }
      return fieldDefs;
   }
   
   /**
    * Returns the display label for a the specified field name of
    * a particualr content type.
    * @param ctypeId must be a valid content type id.
    * @param fieldName the field name whose label needs
    * to be returned. Cannot be <code>null</code> or empty.
    * @return the field's display label or <code>null</code>.
    */
   public String getFieldLabel(long ctypeId, String fieldName)
   {
      if (null == fieldName || fieldName.trim().length() == 0)
      {
         throw new IllegalArgumentException("fieldName cannot be null or empty");
      }
      if (!isRegisteredItemType(ctypeId))
         throw new IllegalArgumentException("Invalid content type id: " + ctypeId);
      
      String label = null;
      PSDisplayMapping dm = getDisplayMapping(ctypeId,fieldName);
      if(dm != null)
      {
        label = dm.getUISet().getLabel().getText();
      }
      return label;
   }


   /**
    * Returns the display mapping for a the specified field name of
    * a particular content type.
    * @param ctypeId must be a valid content type id.
    * @param fieldName the field name whose display mapping needs
    * to be returned. Cannot be <code>null</code> or empty.
    * @return the display mapping corresponding to the supplied fiel dname 
    * or <code>null</code>.
    */
   public PSDisplayMapping getDisplayMapping(long ctypeId, String fieldName) 
   {
        if (null == fieldName || fieldName.trim().length() == 0)
        {
            throw new IllegalArgumentException("fieldName cannot be null or empty");
        }
        if (!isRegisteredItemType(ctypeId))
        throw new IllegalArgumentException("Invalid content type id: " + ctypeId);
    
        PSDisplayMapping dmapping = null;
        try 
        {
            PSItemDefinition def = getItemDef(ctypeId, COMMUNITY_ANY);
            PSContentEditorPipe pipe =  
            (PSContentEditorPipe) def.getContentEditor().getPipe();
            if (null == pipe)
            {
                throw new RuntimeException("Missing pipe on content editor.");
            }
            PSContentEditorMapper mapper = pipe.getMapper();
            PSUIDefinition uidef = mapper.getUIDefinition();
            PSDisplayMapper parent = uidef.getDisplayMapper();
            Iterator<?> mappings = parent.iterator();

            while (mappings.hasNext() && dmapping == null) 
            {
                PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
                if (mapping.getFieldRef().equals(fieldName)) 
                {
                    dmapping = mapping;
                    break;
                }
                if (mapping.getDisplayMapper() != null) 
                {
                    Iterator<?> imappings = mapping.getDisplayMapper().iterator();
                    while (imappings.hasNext()) 
                    {
                        PSDisplayMapping imapping = (PSDisplayMapping) imappings
                            .next();
                        if (imapping.getFieldRef().equals(fieldName)) 
                        {
                            dmapping = mapping;
                            break;
                        }
                    }
                }
            }
        } 
        catch (PSInvalidContentTypeException e) 
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    return dmapping;
}

   /**
    * Gets the ids of all running content editors visible to the requestor.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @return An array of 0 or more numeric identifiers, one for each running
    *         content editor visible to the requestor. These can be used to
    *         obtain definitions or summaries of the associated content type.
    */
   public long[] getContentTypeIds(PSSecurityToken securityToken)
   {
      return getContentTypeIds(securityToken.getCommunityId());
   }

   /**
    * Gets the ids of all running content editors which are visible to the
    * supplied community via community id.
    * 
    * @param communityId community id to filter the content types by.
    *           {@link #COMMUNITY_ANY} to not to filter by community.
    * 
    * @return An array of 0 or more numeric identifiers, one for each running
    *         content editor visible to the community supplied. If no content
    *         types are found for the community with especified communityid , an
    *         empty array is returned.
    */
   public long[] getContentTypeIds(int communityId)
   {
      return getVisibleContentTypes(communityId, true);
   }

   /**
    * Gets the ids of all running content editors (both visible and not visible)
    * to the supplied community via community id.
    * 
    * @param communityId community id to filter the content types by.
    *           {@link #COMMUNITY_ANY} to not to filter by community.
    * 
    * @return An array of 0 or more numeric identifiers, one for each running
    *         content editor visible to the community supplied. If no content
    *         types are found for the community with especified communityid , an
    *         empty array is returned.
    */
   public long[] getAllContentTypeIds(int communityId)
   {
      return getVisibleContentTypes(communityId, false);
   }

   /**
    * Same as {@link #getContentTypeIds(PSSecurityToken)}, except it returns
    * the names rather than the numeric id. Content type names are unique across
    * the system (case insensitive).
    * 
    * @param securityToken See {@link #getContentTypeIds(PSSecurityToken)}.
    * 
    * @return An array of 0 or more non-empty String identifiers, one for each
    *         running content editor visible to the requestor. These can be used
    *         to obtain definitions of the associated content type.
    */
   public String[] getContentTypeNames(PSSecurityToken securityToken)
   {
      if (null == securityToken)
         throw new IllegalArgumentException("Missing security token");

      long[] ids = getContentTypeIds(securityToken);
      String[] names = new String[ids.length];
      for (int i = 0; i < ids.length; i++)
         names[i] = getItemDefSummary(ids[i]).getName();
      return names;
   }

   /**
    * Same as {@link #getContentTypeIds(int)}, except it returns the names
    * rather than the numeric id. Content type names are unique across the
    * system (case insensitive).
    * 
    * @param communityId community id to filter the content types by.
    *           {@link #COMMUNITY_ANY} to not to filter by community.
    * 
    * @return An array of 0 or more non-empty String identifiers, one for each
    *         running content editor visible to the supplied community. These
    *         can be used to obtain definitions of the associated content type.
    */
   public String[] getContentTypeNames(int communityId)
   {
      long[] ids = getContentTypeIds(communityId);
      String[] names = new String[ids.length];
      for (int i = 0; i < ids.length; i++)
         names[i] = getItemDefSummary(ids[i]).getName();
      return names;
   }

   /**
    * Content types can be uniquely identified by their name or numeric id. Maps
    * the supplied identifier to the name of the content type associated with
    * that id. No visibility restrictions are applied.
    * 
    * @param contentTypeId The id of the desired type. A valid id matches one of
    *           the ids of the running content type handlers.
    * 
    * @return If the supplied id is valid, a non-empty name is returned.
    *         Otherwise, an exception is thrown.
    * 
    * @throws PSInvalidContentTypeException If the supplied id is not valid.
    */
   public String contentTypeIdToName(long contentTypeId)
         throws PSInvalidContentTypeException
   {
      /*
       * we don't use the access methods for the member here because we don't
       * have a security token
       */
      List<?> defs = m_itemDefMap.get(new Long(contentTypeId));
      if (null == defs)
         throw new PSInvalidContentTypeException("" + contentTypeId);
      return ((PSItemDefSummary) defs.get(1)).getName();
   }
   
   /**
    * Returns a list of all content type id names that use a specified shared
    * field group.
    * @param sharedGroupName cannot be <code>null</code> or empty.
    * @return array of all content type names using the specified shared field group.
    * Never <code>null</code>, may be empty.
    * @throws PSInvalidContentTypeException
    */
   public String[] getContentTypesUsingSharedFieldGroup(String sharedGroupName) throws PSInvalidContentTypeException
   {
      if(sharedGroupName == null || sharedGroupName.length() == 0)
         throw new IllegalArgumentException("sharedGroupName cannot be null or empty.");
      Collection<String> results = new ArrayList<>();
      long[] cts = getAllContentTypeIds(COMMUNITY_ANY);
      for(long ct : cts)
      {
         PSItemDefinition def = getItemDef(ct, COMMUNITY_ANY);
         PSContentEditorPipe pipe = (PSContentEditorPipe)def.getContentEditor().getPipe();
         PSContentEditorMapper mapper = pipe.getMapper();
         Iterator<?> sharedIncludes = mapper.getSharedFieldIncludes();
         while(sharedIncludes.hasNext())
         {
            String groupName = (String)sharedIncludes.next();
            if(groupName.equals(sharedGroupName))
            {
               results.add(contentTypeIdToName(ct));
               break;               
            }
         }
      }
      return results.toArray(new String[]{});
   }

   /**
    * Maps the supplied identifier with a display label for the given content
    * type.
    * 
    * @param contentTypeId The id of the desired type. A valid id matches one of
    *           the ids of the running content type handlers.
    * 
    * @return If the supplied id is valid, a non-empty name is returned.
    *         Otherwise, an exception is thrown.
    * 
    * @throws PSInvalidContentTypeException If the supplied id is not valid.
    */
   public String contentTypeIdToLabel(long contentTypeId)
         throws PSInvalidContentTypeException
   {
      /*
       * we don't use the access methods for the member here because we don't
       * have a security token
       */
      List<?> defs = m_itemDefMap.get(new Long(contentTypeId));
      if (null == defs)
         throw new PSInvalidContentTypeException("" + contentTypeId);
      return ((PSItemDefSummary) defs.get(1)).getLabel();
   }

   /**
    * Just like {@link #contentTypeIdToName(long)}, except
    * the name rather than the id is supplied.
    * 
    * @param contentTypeName A non-empty identifier for a content type. A valid
    *           name matches of of the names of the running content type
    *           handlers. The comparison is performed case insensitive.
    * 
    * @return If the supplied name is valid, the identifier of the type using
    *         the supplied name, otherwise an exception is thrown.
    * 
    * @throws PSInvalidContentTypeException If the supplied name does not match
    *            any running content type.
    */
   public long contentTypeNameToId(String contentTypeName)
         throws PSInvalidContentTypeException
   {
      /*
       * we don't use the access methods for the member here because we don't
       * have a security token
       */
      Iterator<List<Object>> defs = m_itemDefMap.values().iterator();
      long id = -1;
      boolean found = false;
      while (defs.hasNext() && !found)
      {
         PSItemDefSummary summary = (PSItemDefSummary) ((List<?>) defs.next())
               .get(1);
         if (summary.getName().equalsIgnoreCase(contentTypeName))
         {
            id = summary.getTypeId();
            found = true;
         }
      }
      if (!found)
         throw new PSInvalidContentTypeException(contentTypeName);

      return id;
   }

   /**
    * A summary provides a couple of pieces of information about an item
    * definition. They are typically used to provide a list and description to a
    * user when creating a user interface. Normally, the {@link
    * #getSummaries(PSSecurityToken) getSummaries} method is used to get all
    * available summaries.
    * 
    * @param contentTypeId The id of the desired content type. A valid id
    *           matches one of the ids of the running content editors visible to
    *           the requestor.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @return If a valid id is supplied, the summary for the corresponding
    *         content type is returned, otherwise an exception is thrown.
    * 
    * @throws PSInvalidContentTypeException If the supplied id is not valid.
    * 
    * @see #getSummaries(PSSecurityToken)
    */
   public PSItemDefSummary getSummary(long contentTypeId,
         PSSecurityToken securityToken) throws PSInvalidContentTypeException
   {
      int communityId = securityToken.getCommunityId();
      PSItemDefSummary summary = getSummary(contentTypeId, communityId);
      if (summary == null)
      {
         throw new RuntimeException("Content type with id=" + contentTypeId
               + " is not visible to the community with id=" + communityId);
      }
      return summary;
   }

   /**
    * Same as {@link #getSummary(long,PSSecurityToken)}, except it restricts the
    * content type by the supplied community id instead of that from
    * {@link PSSecurityToken}.
    * 
    * @param contentTypeId The id of the desired content type. A valid id
    *           matches one of the ids of the running content editors visible to
    *           the requestor.
    * 
    * @param communityId community id to filter the content types by.
    *           {@link #COMMUNITY_ANY} to not to filter by community.
    * 
    * @return This method returns summary for the content type. Will be
    *         <code>null</code> if the content type requested is not visible
    *         to the community supplied.
    * 
    * @throws PSInvalidContentTypeException If the supplied id is not valid. For
    *            a content type id to be valid, the content editor for that type
    *            must be running on ther server.
    */
   public PSItemDefSummary getSummary(long contentTypeId, int communityId)
         throws PSInvalidContentTypeException
   {
      return getItemDefSummary(contentTypeId);
   }

   /**
    * Same as {@link #getSummary(long,PSSecurityToken)}, except based on the
    * name.
    * 
    * @param contentTypeName The name of the desired content type. A valid name
    *           matches one of the names of the running editors. The comparison
    *           is done case insensitive.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @return If a valid name is supplied, the summary for the corresponding
    *         content type is returned, otherwise, <code>null</code> is
    *         returned.
    * 
    * @throws PSInvalidContentTypeException If the supplied name is not valid.
    */
   public PSItemDefSummary getSummary(String contentTypeName,
         PSSecurityToken securityToken) throws PSInvalidContentTypeException
   {
      return getSummary(contentTypeNameToId(contentTypeName), securityToken);
   }

   /**
    * Same as {@link #getSummary(long, int)}, except based on the name.
    * 
    * @param contentTypeName The name of the desired content type. A valid name
    *           matches one of the names of the running editors. The comparison
    *           is done case insensitive.
    * 
    * @param communityId community id to filter the content types by.
    *           {@link #COMMUNITY_ANY} to not to restrict by community.
    * 
    * @return If a valid name is supplied, the summary for the corresponding
    *         content type is returned, otherwise, <code>null</code> is
    *         returned.
    * 
    * @throws PSInvalidContentTypeException If the supplied name is not valid.
    */
   public PSItemDefSummary getSummary(String contentTypeName, int communityId)
         throws PSInvalidContentTypeException
   {
      return getSummary(contentTypeNameToId(contentTypeName), communityId);
   }

   /**
    * Get a collection of summaries for all content types visible to the
    * requestor's community. See {@link #getSummary(long,PSSecurityToken)
    * getSummary} for details.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           filtering is controlled. Never <code>null</code>.
    * 
    * @return A summary for every content type visible to the requestor. Each
    *         element in the returned collection is a PSItemDefSummary. The
    *         requestor takes ownership of the returned collection - changes to
    *         it do not affect this class.
    */
   public Collection<PSItemDefSummary> getSummaries(PSSecurityToken securityToken)
   {
      if (null == securityToken)
         throw new IllegalArgumentException("Missing security token");

      Collection<PSItemDefSummary> summaries = new ArrayList<>();
      long[] ids = getContentTypeIds(securityToken);
      for (int i = 0; i < ids.length; i++)
      {
         summaries.add(getItemDefSummary(ids[i]));
      }
      return summaries;
   }

   /**
    * Same as {@link #getSummaries(PSSecurityToken)} except that the content
    * types are filtered by supplied community id instead of requestor's
    * commuity.
    * 
    * @param communityId community id to filter the content types by. -1 to not
    *           to restrict by community.
    * 
    * @return A summary for every content type visible to the supplied
    *         community. Each element in the returned collection is a
    *         PSItemDefSummary. The requestor takes ownership of the returned
    *         collection - changes to it do not affect this class.
    */
   public Collection<PSItemDefSummary> getSummaries(int communityId)
   {
      Collection<PSItemDefSummary> summaries = new ArrayList<>();
      long[] ids = getContentTypeIds(communityId);
      for (int i = 0; i < ids.length; i++)
      {
         summaries.add(getItemDefSummary(ids[i]));
      }
      return summaries;
   }

   /**
    * Get the item defintion for the specified content type id. An item
    * definition is the object representation of a content type. A content type
    * is a collection of design data that defines how to create and modify a
    * particular type of content item.
    * 
    * @param contentTypeId The id of the desired content type. A valid id
    *           matches one of the ids of the running content editors visible to
    *           the request.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @return A valid def if an editor matching the supplied type id is running
    *         and the requestor has the required access rights. If requestor
    *         cannot see the content type, a runtime exception is thrown. Never
    *         <code>null</code>. The returned item should be treated as
    *         read-only as it is the original object.
    * 
    * @throws PSInvalidContentTypeException If the supplied id is not valid.
    */
   public PSItemDefinition getItemDef(long contentTypeId,
         PSSecurityToken securityToken) throws PSInvalidContentTypeException
   {
      if (null == securityToken)
         throw new IllegalArgumentException("Missing security token");

      int communityId = securityToken.getCommunityId();
      PSItemDefinition itemDef = getItemDef(contentTypeId, communityId);
      if (itemDef == null)
      {
         throw new PSInvalidContentTypeException("Content type with id=" + contentTypeId
               + " is not visible to the community with id=" + communityId);
      }
      return itemDef;
   }

   /**
    * Same as {@link #getItemDef(long, PSSecurityToken)} but filtered by the
    * supplied community instead of requestor's community.
    * 
    * @param contentTypeId The id of the desired content type. A valid id
    *           matches one of the ids of the running content editors visible to
    *           the request.
    * 
    * @param communityId community id to filter the content types by. -1 to not
    *           to restrict by community.
    * 
    * @return A valid def if an editor matching the supplied type id is running
    *         and the def is visible to the supplied community. May be
    *         <code>null</code>. The returned item should be treated as
    *         read-only as it is the original object.
    * 
    * @throws PSInvalidContentTypeException If the supplied content type id is
    *            not valid.
    */
   public PSItemDefinition getItemDef(long contentTypeId, int communityId)
         throws PSInvalidContentTypeException
   {
      //TODO: FIXME Add community support back.
      // No longer filter by community in CM1
      // if (!isVisibleToCommunity(contentTypeId, communityId))
      //   return null;

      List defs = m_itemDefMap.get(new Long(contentTypeId));
      if (defs == null)
         throw new PSInvalidContentTypeException(String.valueOf(contentTypeId));
      return (PSItemDefinition) defs.get(0);
   }

   /**
    * Same as {@link #getItemDef(long,PSSecurityToken)}, except based on the
    * name of the content type.
    * 
    * @param contentTypeName The name of the desired content type. A valid name
    *           matches one of the names of the running editors. The comparison
    *           is done case insensitive. Never <code>null</code>.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If the supplied name is not valid.
    */
   public PSItemDefinition getItemDef(String contentTypeName,
         PSSecurityToken securityToken) throws PSInvalidContentTypeException
   {
      return getItemDef(contentTypeNameToId(contentTypeName), securityToken);
   }

   /**
    * Convenience method that calls {@link #getItemDef(long,int)
    * getItemDef(contentTypeNameToId(contentTypeName), communityId)}.
    */
   public PSItemDefinition getItemDef(String contentTypeName, int communityId)
         throws PSInvalidContentTypeException
   {
      return getItemDef(contentTypeNameToId(contentTypeName), communityId);
   }

   /**
    * Same as {@link #getItemDef(long,PSSecurityToken)}, except based on an
    * item.
    * 
    * @param itemId the item locator. May not be <code>null</code>.
    * 
    * @param securityToken A security identifier that contains the requestor's
    *           security info. See the class description for details on how
    *           access is controlled. Never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If the supplied item is not valid.
    */
   public PSItemDefinition getItemDef(PSLocator itemId,
         PSSecurityToken securityToken) throws PSInvalidContentTypeException
   {
      return getItemDef(getItemContentType(itemId), securityToken);
   }

   /**
    * Same as {@link #getItemDef(PSLocator, PSSecurityToken)}, except that the
    * filtering is based on the supplied community id instead of requestor's
    * community.
    * 
    * @param itemId the item locator. May not be <code>null</code>.
    * 
    * @param communityId community id to filter the content types by. -1 to not
    *           to restrict by community.
    * 
    * @throws PSInvalidContentTypeException If the supplied item is not valid.
    */
   public PSItemDefinition getItemDef(PSLocator itemId, int communityId)
         throws PSInvalidContentTypeException
   {
      return getItemDef(getItemContentType(itemId), communityId);
   }

   /**
    * Makes an internal request to determine the content type id of the
    * specified content item.
    * 
    * @param itemId the item locator. May not be <code>null</code>.
    * 
    * @return the content type id, -1 if no matching content type is found.
    */
   public long getItemContentType(PSLocator itemId)
   {
      notNull(itemId, "itemId");
      /*
       * First we try the cache - Adam Gent.
       */
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null) {
         Integer contentId = itemId.getId();
         IPSItemEntry entry = cache.getItem(contentId);
         if (entry != null)
            return entry.getContentTypeId();
      }

      /*
       * If the cache is not up or it could not be found we will
       * have to do it old school - Adam Gent.
       */
      long contentTypeId = -1;
      Map<String, Object> params = new HashMap<>(1);
      params.put(IPSHtmlParameters.SYS_CONTENTID, itemId.getId() + "");
      Element result = load("lookupContentTypeId.xml", params);
      if (result != null)
      {
         // make the internal request and extract the type id from the result
         PSXmlTreeWalker tree = new PSXmlTreeWalker(result);
         Element el = tree.getNextElement("PSXContentType");
         try
         {
            contentTypeId = Long.parseLong(el.getAttribute("typeId"));
         }
         catch (NumberFormatException e)
         {
            // didn't find one
         }
      }

      return contentTypeId;
   }

   /**
    * Retrieves the cached cms object from a content type id.
    * 
    * @param contentTypeId The content type id of the type of interest. If the
    * content type does not exist, <code>null</code> is returned.
    * 
    * @return If there isn't a cached instance, <code>null</code> is returned.
    */
   public PSCmsObject getCmsObject(long contentTypeId)
   {
      List<?> defs = m_itemDefMap.get(new Long(contentTypeId));
      if (defs == null)
         return null;
      else
         return (PSCmsObject) defs.get(3);
   }

   /**
    * During a content editor handler's initialization, it must call this method
    * to register its definition. During its shutdown sequence, it must call
    * {@link #unRegisterDef(PSContentEditor) unRegisterDef}. If the same def is
    * registered more than once, the successive registrations replace the
    * earlier ones.
    * 
    * @param editorDef The complete definition for the content editor, never
    *           <code>null</code>.
    * 
    * @param appName The name of the application that contains the supplied def.
    *           Never <code>null</code> or empty.
    * 
    * @return The content type id of the def that was registered.
    * 
    * @throws PSInvalidContentTypeException If a matching definition in the
    *            repository cannot be found. In this case, the handler should
    *            not start.
    * 
    * @throws RuntimeException If a listener throws an exception. The handler
    *            should not start in this case either.
    */
   public long registerDef(String appName, PSContentEditor editorDef)
         throws PSInvalidContentTypeException
   {
      if (null == editorDef)
         throw new IllegalArgumentException("Missing editor definition");
      if (null == appName || appName.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "Application name cannot be null or empty.");
      }

      long typeId = editorDef.getContentType();
      Long key = new Long(typeId);

      PSSecurityToken internalToken = PSRequest.getContextForRequest()
            .getSecurityToken();

      PSContentType typeDef = null;
       IPSNodeDefinition nodeDef = null;
      String typeName = ""; 
      String namenows = "";

       IPSContentMgr cml = PSContentMgrLocator.getContentMgr();
       try {
           nodeDef = cml.loadNodeDefinitions(Collections.singletonList(new PSGuid(PSTypeEnum.NODEDEF, typeId))).stream().findFirst().orElse(null);
       } catch (RepositoryException e) {
           log.error("Unable to locate content type with id {}, Error : {} ", typeId,e.getMessage());
           log.debug(e.getMessage(), e);
           throw new PSInvalidContentTypeException(Long.toString(typeId));
       }


       //NC FindBugs NPE
      if(nodeDef!= null){
          typeName = StringUtils.substringAfter(nodeDef.getName(),"rx:");
          typeDef = new PSContentType(
                  (int) nodeDef.getGUID().longValue(), typeName, nodeDef.getLabel(),
                  nodeDef.getDescription(), nodeDef.getQueryRequest(), nodeDef.getHideFromMenu(), nodeDef.getObjectType());

         namenows = StringUtils.deleteWhitespace(typeName);
      }else{
         log.error("Unable to locate content type with id {}" + typeId);
         throw new PSInvalidContentTypeException(Long.toString(typeId));
      }
      if (namenows.length() != typeName.length())
      {
         log.error("Invalid content type name {} contains whitespace", typeName);
      }
      
      String editorUrl = typeDef.getQueryRequest();
      if (!appName.equals(PSContentType.getAppName(editorUrl)))
      {
         log.warn("The content editor application name \"{} \" does not match the request root specified in the editor url"
            + " registration \"{}\" for content type name: {}", appName, editorUrl, typeName);
      }
      
      if (!PSContentType.createRequestUrl(typeName).equalsIgnoreCase(
         editorUrl))
      {
         log.warn("The content editor application \" {} \" does not follow proper naming conventions based on the "
            + "content type name: {}", appName, typeName);
      }
      
      m_currentRegisteredId = typeId;


      PSItemDefSummary summary = new PSItemDefSummary(typeName,
            typeDef.getLabel(), typeDef.getTypeId(), typeDef.getQueryRequest(),
            typeDef.getDescription());

      PSCmsObject cmsObject = PSServer.getCmsObjectRequired(typeDef
            .getObjectType());

      List<Object> defs = new ArrayList<>();

      PSItemDefinition def = new PSItemDefinition(appName, typeDef, editorDef);
      defs.add(def);
      defs.add(summary);
      defs.add(editorDef);
      defs.add(cmsObject);
      m_itemDefMap.put(key, defs);

      callItemDefListeners(def);
      m_currentRegisteredId = -1;

      return typeDef.getTypeId();
   }

   /**
    * Registers an item definition for the supplied paremeters.
    * 
    * @param def the item definition to register, not <code>null</code>, must
    *           contain a valid description and content editor object.
    * @param cmsObject the cms object to register, not <code>null</code>.
    */
   public void registerDef(PSItemDefinition def, PSCmsObject cmsObject)
   {
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (cmsObject == null)
         throw new IllegalArgumentException("cmsObject cannot be null");

      List<Object> defs = new ArrayList<>();
      defs.add(def);
      defs.add(def);
      defs.add(def.getContentEditor());
      defs.add(cmsObject);

      m_itemDefMap.put(new Long(def.getContentEditor().getContentType()), defs);
   }

   /**
    * Call the item def listeners on the given definition, or store the
    * definition for later use based on the {@link #m_deferredNotifications}
    * boolean.
    * 
    * @param def the definition, assumed non-<code>null</code>
    */
   private void callItemDefListeners(PSItemDefinition def)
   {
      if (!m_deferringNotifications)
      {
         callItemDefListenersInternal(def, true);
      }
      else
      {
         m_deferredNotifications.add(def);
      }
   }

   /**
    * Call the item def listeners on the given definition
    * 
    * @param def the definition, assumed non-<code>null</code>
    * @param notify the value is passed to the listener's
    *           <code>registered</code> method
    */
   private void callItemDefListenersInternal(PSItemDefinition def,
         boolean notify)
   {
      synchronized (m_itemDefListeners)
      {
         for (Iterator<IPSItemDefChangeListener> iter = m_itemDefListeners.iterator(); iter.hasNext();)
         {
            IPSItemDefChangeListener listener = (IPSItemDefChangeListener) iter
                  .next();
            try
            {
               listener.registered(def, notify);
            }
            catch (PSException e)
            {
               throw new PSRuntimeException(e.getErrorCode(), e
                     .getErrorArguments());
            }
         }
      }
   }

   /**
    * Gets the content editor url based on a content type passed as an id.
    * 
    * @param contentTypeId a number representing the content type
    * @return the url where the content type editor is located as
    *         <code>String</code>, never <code>null</code> or empty.
    * @throws PSException if the content type does not exist.
    */
   public String getTypeEditorUrl(long contentTypeId) throws PSException
   {
      PSItemDefSummary summary = getSummary(contentTypeId,
            PSItemDefManager.COMMUNITY_ANY);

      if (summary == null)
      {
         // content type does not exist
         throw new PSException(
               IPSWebServicesErrors.WEB_SERVICE_CONTENT_TYPE_NOT_FOUND, ""
                     + contentTypeId);
      }

      return summary.getEditorUrl();
   }

   /**
    * Convenience method that calls 
    * {@link #getAssemblerUrl(IPSRequestContext, int) 
    * getAssemblerUrl(new PSRequestContext(request, variantId)}.
    */
   public String getAssemblerUrl(PSRequest request, int variantId)
         throws PSInternalRequestCallException
   {
      return getAssemblerUrl(new PSRequestContext(request), variantId);
   }

   /**
    * Get the assembler url for the supplied variant id.
    * 
    * @param request the request to do the url lookup, not <code>null</code>.
    * @param variantId the variant id for which to lookup the assembler url.
    * @return the assembler url in a form like
    *         <code>../casArticle/casArticle.html</code>, may be
    *         <code>null</code> if no variant was found ffor the supplied id.
    * @throws PSInternalRequestCallException for any errors making the lookup.
    */
   public String getAssemblerUrl(IPSRequestContext request, int variantId)
         throws PSInternalRequestCallException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      Map<String, String> params = new HashMap<>();
      params.put(IPSHtmlParameters.SYS_VARIANTID, Integer.toString(variantId));

      IPSInternalRequest ir = request.getInternalRequest(
            "sys_casSupport/AssemblyUrl.xml", params, false);
      Document doc = ir.getResultDoc();
      Element root = doc.getDocumentElement();

      return root == null ? null : root.getAttribute("current");
   }

   /**
    * Reads the content type information from the repository and creates a
    * representative object. A resource called "contentTypes" is expected to
    * exist in the default app that optionally accepts a parameter called
    * sys_contenttype to filter the results.
    * 
    * See {@link PSContentType} for details on the PSXContentType element.
    * 
    * @param token The security token to use, assumed not <code>null</code>.
    * @param contentTypeId The id of the desired content type. Assumed to be a
    *           valid id.
    * 
    * @return The summary for the supplied id, if valid. Otherwise,
    *         <code>null</code> is returned.
    */
   private PSContentType getContentTypeDef(PSSecurityToken token,
         long contentTypeId)
   {
      if (contentTypeId == 0)
         throw new IllegalArgumentException("contentTypeId may not be = 0");

      int ctIds[] =
      {(int) contentTypeId};
      PSKey[] keys = PSContentType.createKeys(ctIds);

      Element[] elems;
      PSContentType contentType = null;

      try
      {
         elems = getProxy(token).load(
               PSDbComponent.getComponentType(PSContentTypeSet.class), keys);

         if (elems.length != 1)
            return null;

         contentType = new PSContentType(elems[0]);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
      catch (PSCmsException e1)
      {
         throw new RuntimeException(e1.getLocalizedMessage());
      }

      return contentType;
   }

   /**
    * Creates an internal request to the supplied resource, using the default
    * app. The root element from the document returned from this request is
    * returned. If the request is successful, but no document was returned
    * (empty results), <code>null</code> is returned.
    * 
    * @param resource The name of the resource which will build the result doc.
    *           Assumed not <code>null</code> or empty.
    * 
    * @param params An optional set of 'html' parameters that will be supplied
    *           with the request. May be <code>null</code> or empty.
    * 
    * @return The root element from the result doc, or <code>null if the
    *    result was empty.
    */
   private Element load(String resource, Map<String, Object> params)
   {
      try
      {
         String path = "sys_psxCms" + "/" + resource;
         PSRequest req = PSRequest.getContextForRequest();
         PSInternalRequest ir = PSServer.getInternalRequest(path, req, params,
               true);
         if (null == ir)
         {
            throw new RuntimeException("System application '" + path
                  + "' missing or not running.");
         }
         Document result = ir.getResultDoc();
         ir.cleanUp();
         return result.getDocumentElement();
      }
      catch (PSInternalRequestCallException e)
      {
         // this could happen if the app is not running
         throw new PSRuntimeException(e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * Retrieves the cached reference and returns it.
    * 
    * @param contentTypeId The id of the type of interest. If the associated
    *           editor is not running, <code>null</code> is returned.
    * 
    * @return If there isn't a cached instance, <code>null</code> is returned.
    */
   private PSItemDefSummary getItemDefSummary(long contentTypeId)
   {
      return (PSItemDefSummary) ((List<?>) (m_itemDefMap.get(new Long(
            contentTypeId)))).get(1);
   }

   /**
    * Retrieves the cached reference and returns it.
    * 
    * @param contentTypeId The id of the type of interest. If the associated
    * editor is not running, <code>null</code> is returned.
    * 
    * @return If there isn't a cached instance, <code>null</code> is returned.
    */
   public PSContentEditor getContentEditorDef(long contentTypeId)
   {
      List<?> defs = m_itemDefMap.get(contentTypeId);
      if (defs == null)
         return null;
      else
         return (PSContentEditor) defs.get(2);
   }

   /**
    * This method performs the following checks in order.
    * <ol>
    * <li>Makes sure the supplied content type (via content type id) is part of
    * the available content types.</li>
    * <li>Checks to see if the object type is content item. Returns
    * <code>true</code> if it is not an item assuming the other object types
    * need no filtering by community.</li>
    * </li>
    * Check succeeds if the supplied community is {@link #COMMUNITY_ANY}, which
    * means no filtering is required</li>
    * </li>
    * Finally it makes sure the community id supplied is configurd to be visible
    * to the supplied community by making an internal request to a Rhythmyx
    * resource</li>
    * 
    * @param contentTypeId The id of the desired content type. A valid id
    *           matches one of the ids of the running content editors.
    * 
    * @param communityId community id to filter the content types by. -1 to not
    *           to filter by community.
    * 
    * @return <code>true</code> if the content type asked is available and
    *         visible to the supplied community based on the checks described in
    *         the method description.
    * 
    * @throws PSInvalidContentTypeException If the content type id supplied is
    *            not listed in the avaialble content types.
    */
   public boolean isVisibleToCommunity(long contentTypeId, int communityId)
         throws PSInvalidContentTypeException
   {
      //TODO: FIXME We need community support added back to CM1.
      //We do not use community in CM1
      return true;
   }

   /**
    * Performs a check that determines whether a content editor whose object
    * type is {@link PSCmsObject#TYPE_ITEM} and whose identifier matches the
    * supplied id is currently registered.
    * 
    * @param contentTypeId Any value is allowed.
    * 
    * @return <code>true</code> if the supplied id identifies a registered
    *         content type, <code>false</code> otherwise.
    */
   private boolean isRegisteredItemType(long contentTypeId)
   {
      Iterator<Long> keys = m_itemDefMap.keySet().iterator();

      while (keys.hasNext())
      {
         Long key = (Long) keys.next();
         List<?> list = m_itemDefMap.get(key);
         if (list != null)
         {
            // skip Folder, which should not be visible from UI or MENU
            PSCmsObject cmsObj = (PSCmsObject) list.get(3);
            if (cmsObj != null && cmsObj.getTypeId() != PSCmsObject.TYPE_ITEM)
               continue;
         }

         long id = key.longValue();
         if (contentTypeId == id)
            return true;
      }
      return false;
   }

   /**
    * Looks for all valid content types that are visible to the requestor and
    * returns their ids as Integer so they can be used to directly access the
    * related info. Depending on the given set of controlFlags that are defined
    * in the {@link IPSCataloger} it may widen or narrow the visibility. Also
    * skips the Folder contenttype.
    * 
    * @param communityId The community id to filter the content types by.  Use 
    *           -1 to not filter by community.
    * @param isUIVisible <code>true</code> if get the UI visible content types
    *           only, which does not include the content types that are HIDE
    *           FROM MENU or UI, such as folder content type.
    * 
    * @return A valid array with 0 or more entries. Each entry is visible to the
    *         requestor.
    */
   private long[] getVisibleContentTypes(int communityId, boolean isUIVisible)
   {
      Collection<Long> visibleIds = new ArrayList<>();
      
      for ( Entry<Long,List<Object>> entry : m_itemDefMap.entrySet())
      {
         Long key = entry.getKey();
         int id = key.intValue();

         if (isUIVisible)
         {
            List<?> list = entry.getValue();
            if (list != null)
            {
               // skip Folder, which should not be visible from UI or MENU
               PSCmsObject cmsObj = (PSCmsObject) list.get(3);
               if (cmsObj != null
                     && cmsObj.getTypeId() == PSCmsObject.TYPE_FOLDER)
                  continue;
            }
         }

         // Skip any content type that's in the process of registering
         if (id == m_currentRegisteredId)
            continue;

         try
         {
            if (isVisibleToCommunity(id, communityId))
            {
               visibleIds.add(key);
            }
         }
         catch (PSInvalidContentTypeException e)
         { /* ignore, this should not happen */
         }
      }

      long[] results = new long[visibleIds.size()];
      Iterator<Long> ids = visibleIds.iterator();
      for (int i = 0; ids.hasNext(); i++)
      {
         Long id = (Long) ids.next();
         results[i] = id.intValue();
      }

      return results;
   }

   /**
    * Finds fields mapped to any content type content types based upon the
    * existence of a specific property name and value in the control properties.
    * This does not test against the dynamic generated value but the display
    * value of the configuration as seen in the properties dialog of workbench
    * 
    * @param propertyName
    * @param propertyValue
    * @return A List of field names keyed of the content type name, never <code>null</code>.
    *         
    * @throws PSInvalidContentTypeException
    */
   public Map<String, List<String>> getFieldsWithControlProp(String propertyName, String propertyValue)
         throws PSInvalidContentTypeException
   {
      Map<String, List<String>> retMap = new HashMap<>();
      for (long typeId : getAllContentTypeIds(COMMUNITY_ANY))
      {
         String typeName = getItemDefSummary(typeId).getName();
         PSItemDefinition def = getItemDef(typeId, COMMUNITY_ANY);
         List<String> fieldList = PSFieldFinderUtil.getFields(def, propertyName, propertyValue);
         if (!fieldList.isEmpty())
            retMap.put(typeName, fieldList);
      }
      return retMap;
   }
   
   /**
    * This method is used to remove a content type definition from the registry.
    * It should be called whenever the handler for this content type shuts down.
    * 
    * @param editorDef Must be the same def that was passed in to the <code>
    *    registerDef<code> method. Never <code>null</code>.
    * @throws Exception if the called listener throws an exception.
    */
   public void unRegisterDef(PSContentEditor editorDef) throws Exception
   {
      long typeId = editorDef.getContentType();
      if (editorDef == getContentEditorDef(typeId))
      {
         PSItemDefinition def = getItemDef(typeId, COMMUNITY_ANY);
         m_itemDefMap.remove(typeId);
         synchronized (m_itemDefListeners)
         {
            for (Iterator<IPSItemDefChangeListener> iter = m_itemDefListeners.iterator(); iter.hasNext();)
            {
               IPSItemDefChangeListener listener = (IPSItemDefChangeListener) iter
                     .next();
               listener.unregistered(def, true);
            }
         }
      }
   }

   /**
    * Add the specified listener to the list of listeners to call on
    * registration and unregistration of content editors.
    * 
    * @param listener the listener to add, must never be <code>null</code>
    */
   public void addListener(IPSItemDefChangeListener listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener must never be null");
      }
      synchronized (m_itemDefListeners)
      {
         m_itemDefListeners.add(listener);
      }
   }

   /**
    * Removes the specified listener from the list of listeners to call on
    * registration and unregistration of content editors.
    * 
    * @param listener the listener to remove, must never be <code>null</code>
    */
   public void removeListener(IPSItemDefChangeListener listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener must never be null");
      }
      synchronized (m_itemDefListeners)
      {
         m_itemDefListeners.remove(listener);
      }
   }

   /**
    * Unregister all listeners currently listenering for changes to item def
    * registration.
    */
   public void clearListeners()
   {
      synchronized (m_itemDefListeners)
      {
         m_itemDefListeners.clear();
      }
   }

   /**
    * Set the deferring notifications flag. If this flag is set, then calls that
    * ordinarily will make the search system restart will be deferred.
    */
   public synchronized void deferUpdateNotifications()
   {
      m_deferringNotifications = true;
   }

   /**
    * Causes any deferred work to be passed to the item def listeners. Right now
    * this is only item registrations.
    */
   public synchronized void commitUpdateNotifications()
   {
      if (m_deferringNotifications && m_deferredNotifications.size() > 0)
      {
         for (Iterator<PSItemDefinition> iter = m_deferredNotifications.iterator(); iter
               .hasNext();)
         {
            PSItemDefinition def = (PSItemDefinition) iter.next();
            // Calls with a true notification flag for the last definition
            callItemDefListenersInternal(def, !iter.hasNext());
         }
      }

      m_deferredNotifications = new ArrayList<>();
      m_deferringNotifications = false;
   }

   /**
    * The ctor is private to implement the singleton pattern. A long lived
    * object should get a reference to this object and keep it for the duration
    * of the program so that the class is not garbage collected.
    */
   private PSItemDefManager() {
   }

   /**
    * Returns Server side Component def processor proxy.
    * 
    * @param token The security token to use, may not be <code>null</code>.
    * 
    * @return The proxy, never <code>null</code>.
    */
   private PSComponentDefProcessorProxy getProxy(PSSecurityToken token)
   {
      if (token == null)
         throw new IllegalArgumentException("token may not be null");

      try
      {
         return new PSComponentDefProcessorProxy(
               PSProcessorProxy.PROCTYPE_SERVERLOCAL, new PSRequest(token));
      }
      catch (PSCmsException e)
      {
         throw new IllegalStateException(e.getMessage());
      }
   }

   /**
    * Gets the content type icon mode and value for the given content type id.
    * 
    * @param ctypeId, id of the contenttype, throws
    *           <code>PSInvalidContentTypeException</code>, if it is not a
    *           valid contenttype id.
    * @return a map of icon source and value. See
    *         {@link PSContentEditor#getIconSource()} for details of source and
    *         See {@link PSContentEditor#getIconValue()} for the details of
    *         value.
    * @throws PSInvalidContentTypeException See {@link #getItemDef(long, int)}.
    */
   public Map<String, String> getContentTypeIcon(
         int ctypeId) throws PSInvalidContentTypeException
   {
      PSItemDefinition ceDef =  getItemDef(ctypeId, COMMUNITY_ANY);
      Map<String, String> ctypeIcon = new HashMap<>();
      PSContentEditor ce = ceDef.getContentEditor();
      ctypeIcon.put(ce.getIconSource(), ce.getIconValue());
      return ctypeIcon;
   }
   
   /**
    * Gets the contenttype icon paths for the supplied list of locators. The
    * path may be empty or <code>null</code> if the content type of the item
    * is not specified with an icon or failed to determine the icon. If the
    * item's contenttype iconSource is
    * <code>PSContentEditor.ICON_SOURCE_NONE</code> then the path for that
    * item is set to null. If it is
    * <code>PSContentEditor.ICON_SOURCE_SPECIFIED</code> then the iconValue is
    * prefixed with "../rx_resources/images/ContentTypeIcons/" and set as item's
    * icon path. If it is <code>PSContentEditor.ICON_SOURCE_FROMFILEEXT</code>
    * then the icon path is determined by FileIcons.properties.
    * 
    * @param itemLoc List of item locators must not be <code>null</code>.
    * @return Map of locators and icon paths. Never <code>null</code> may be
    *         empty.
    */
   public Map<PSLocator, String> getContentTypeIconPaths(List<PSLocator> itemLoc)
   {
      if (itemLoc == null)
         throw new IllegalArgumentException("itemLoc must not be null");
      Map<PSLocator, String> iconMap = new HashMap<>();
      Map<Long, PSItemDefinition> typeDefs = new HashMap<>();
      Map<PSLocator, String> fileItems = new HashMap<>();
      for (PSLocator locator : itemLoc)
      {
         Long ctypeid = getItemContentType(locator);
         if(ctypeid == -1)
         {
            log.warn("Failed to get the content type for locator {} . Setting the icon path to null", locator.toString());
            iconMap.put(locator, null);
            continue;
         }
         PSItemDefinition typeDef = typeDefs.get(ctypeid);
         if (typeDef == null)
         {
            try
            {
               typeDef = getItemDef(ctypeid, COMMUNITY_ANY);
               typeDefs.put(ctypeid, typeDef);
            }
            catch (PSInvalidContentTypeException e)
            {
               log.warn("Failed to get the item def for contenttype id {} . Setting the icon path to null for locator {}", ctypeid, locator.toString());
               log.debug(e.getMessage(), e);
               iconMap.put(locator, null);
               continue;
            }
         }
         PSContentEditor editor = typeDef.getContentEditor();
         String iconMode = editor.getIconSource();
         if (iconMode.equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
         {
            iconMap.put(locator, "../" + RX_ICON_FOLDER + editor.getIconValue());
         }
         else if (iconMode.equals(PSContentEditor.ICON_SOURCE_FROMFILEEXT))
         {
            fileItems.put(locator, editor.getIconValue());
         }
         else
         {
            iconMap.put(locator, null);
         }
      }
      if (!fileItems.isEmpty())
      {
         //Remove until more efficient code can be written
         //iconMap.putAll(getFileIconPaths(fileItems));
      }
      return iconMap;
   }
   
   
   /**
    * Helper method to get the icon paths for the supplied map of locator and
    * extension field name. If an image file exists for the extension in
    * FileIcons.properties file, it will be set as icon path. If not checks for
    * a property with contenttype name and if exists sets that as icon path.
    * 
    * @param fileItems map of locators with fieldnames assumed not
    *           <code>null</code>
    * @return Map of locators and icon paths. The icon path set to
    *         <code>null</code>, if failed to determine.
    */
   private Map<PSLocator, String> getFileIconPaths(
         Map<PSLocator, String> fileItems)
   {
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      Map<PSLocator, String> icPaths = new HashMap<>();
      Properties rxProps = getRxFileIconProperties();
      Properties sysProps = getSysFileIconProperties();
      
      // don't load binary & child fields
      PSContentMgrConfig config = new PSContentMgrConfig();
      config.addOption(PSContentMgrOption.LOAD_MINIMAL);
      config.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
      
      for (PSLocator loc : fileItems.keySet())
      {
         List<Node> nodes;
         try
         {
            nodes = cmgr.findItemsByGUID(Collections.singletonList(guidMgr
                  .makeGuid(loc)), config);
            Node node = nodes.get(0);
            if (node == null)
            {
               icPaths.put(loc, null);
               log.warn("Failed to load item for locator {} Setting the icon path to null.", loc.toString());
               continue;
            }
            PSContentNode n = (PSContentNode) node;
            String fn = fileItems.get(loc);
            String ip = null;
            Property pr = n.getProperty(fn);
            if (pr != null)
            {
               ip = StringUtils.defaultString(pr.getString());
               ip = ip.trim();
               // remove leading dots if any
               while (ip.startsWith("."))
               {
                  ip = ip.substring(1);
               }
            }
            String iconFn="";
            //NC - FB - NPE Check
            if(ip != null){
               iconFn = rxProps.getProperty(ip);
            iconFn = getFullIconPath(iconFn,false);
            //Get it from system properties if it is blank
            if (StringUtils.isBlank(iconFn))
            {
               iconFn = sysProps.getProperty(ip);
               iconFn = getFullIconPath(iconFn,true);
            }            
            }
            //If it is still blank Check whether an icon exists with 
            //contenttype name as property
            if(StringUtils.isBlank(iconFn))
            {
               Long ctypeid = getItemContentType(loc);
               try
               {
                  PSItemDefinition typeDef = getItemDef(ctypeid, COMMUNITY_ANY);
                  String name = typeDef.getName();
                  iconFn = rxProps.getProperty(name);
                  iconFn = getFullIconPath(iconFn,false);
               }
               catch (PSInvalidContentTypeException e)
               {
                  //This should not happen as we just loaded the item def.
                  //ignore if happens
               }
            }
            icPaths.put(loc, iconFn);
         }
         catch (RepositoryException e)
         {
            // If we can't load the item better to set the icon path to null
            // rather than throwing an exception here.
            log.warn("Failed to load item for locator {}  Setting the icon path to null. Error : {}",loc.toString(), e);
            log.debug(e.getMessage(),e);
            icPaths.put(loc, null);
         }
      }
      return icPaths;
   }
   
   /**
    * Helper method to return the full path of the icon file.
    * 
    * @param iconFn if blank returns blank otherwise adds the full path.
    * @param isSys if <code>true</code> builds the system path otherwise
    *           builds the rx path.
    * @return Full path of the iconfile, may be <code>null</code> or empty.
    */
   public String getFullIconPath(String iconFn, boolean isSys)
   {
      if (StringUtils.isBlank(iconFn))
      {
         return iconFn;
      }
      String fullPath = "";
      if (isSys)
      {
         fullPath = "../" + SYS_ICON_FOLDER + FILE_ICONS_FOLDER + "/" + iconFn;
      }
      else
      {
         fullPath = "../" + RX_ICON_FOLDER + FILE_ICONS_FOLDER + "/" + iconFn;
      }
      return fullPath;
   }

   /**
    * Gets the rx properties consisting of file extension and corresponding icon
    * file name. The properties file is loaded if it is modifed after last load.
    * 
    * @return Properties of file extensions and icon paths, never
    *         <code>null</code> may be empty.
    */
   public synchronized Properties getRxFileIconProperties()
   {
      Properties rxProps = new Properties();
      boolean loadProps = false;
      File file = new File(PSServer.getRxDir() + "/" + RX_ICON_FOLDER
            + FILE_ICONS_FOLDER + "/" + FILE_ICONS_PROPERTIES);
      if(!file.exists())
         return rxProps;
      if (ms_rxFileIconProperties == null)
      {
         loadProps = true;
      }
      else
      {
         Iterator<Long> iter = ms_rxFileIconProperties.keySet().iterator();
         Long dt = iter.next();
         if (file.lastModified() > dt || ms_rxFileIconProperties.get(dt) == null)
            loadProps = true;
         else
            rxProps = ms_rxFileIconProperties.get(dt);
      }
      if (loadProps)
      {
         FileInputStream rxPropsIn;
         try
         {
            rxPropsIn = new FileInputStream(file);
            rxProps.load(rxPropsIn);
         }
         catch (FileNotFoundException e)
         {
            log.warn("Error getting the rx resources file icon details.", e);
            log.debug(e.getMessage(),e);
         }
         catch (IOException e)
         {
            log.warn("Error getting the rx resources file icon details.", e);
             log.debug(e.getMessage(),e);
         }
         ms_rxFileIconProperties = new HashMap<>();
         ms_rxFileIconProperties.put(new Long(file.lastModified()), rxProps);
      }
      return rxProps;
   }
   
   /**
    * Gets the sys properties consisting of file extension and corresponding icon
    * file name. The properties file is loaded only once.
    * 
    * @return Properties of file extensions and icon paths, never
    *         <code>null</code> may be empty.
    */
   public synchronized Properties getSysFileIconProperties()
   {
      //Load the system props if it is null
      if(ms_sysFileIconProperties == null)
      {
         ms_sysFileIconProperties = new Properties();
         File sfile = new File(PSServer.getRxFile(SYS_ICON_FOLDER
               + FILE_ICONS_FOLDER + "/" + FILE_ICONS_PROPERTIES));
         try
         {
            ms_sysFileIconProperties.load(new FileInputStream(sfile));
         }
         catch (FileNotFoundException e)
         {
            log.warn("Error getting the sys resources file icon details.", e);
             log.debug(e.getMessage(),e);
         }
         catch (IOException e)
         {
            log.warn("Error getting the sys resources file icon details.", e);
             log.debug(e.getMessage(),e);
         }
      }
      return ms_sysFileIconProperties;
   }
   
   /**
    * Content type icon properties, consisting of file extensions and icon path
    * for each extension. Initialized in {@see #getFileIconProperties()}.
    */
   private static Map<Long,Properties> ms_rxFileIconProperties = null;
   
   /**
    * Constant for the relative path of system content type icons folder.
    */
   public static final String RX_ICON_FOLDER = 
      "rx_resources/images/ContentTypeIcons/";

   /**
    * Content type icon properties, consisting of file extensions and icon path
    * for each extension. Initialized in {@see #getFileIconProperties()}.
    */
   private static Properties ms_sysFileIconProperties = null;
   
   /**
    * Constant for the relative path of system content type icons folder.
    */
   private static final String SYS_ICON_FOLDER = 
      "sys_resources/images/ContentTypeIcons/";

   /**
    * Constant for file icons folder name.
    */
   private static final String FILE_ICONS_FOLDER = "FileIcons";
  
   /**
    * Constant for file icons properties file name.
    */
   private static final String FILE_ICONS_PROPERTIES = "FileIcons.properties";
   
   /**
    * The only instance of this class. Constructed during class init, never
    * <code>null</code> after that.
    */
   private static PSItemDefManager ms_instance = new PSItemDefManager();

   /**
    * Used to maintain the registry of running content editor defs. The key is a
    * Long whose value is the content type id. The value is a List that contains
    * 4 entries. The first is a PSItemDefinition, the 2nd is a
    * PSItemDefSummary,the 3rd is the original PSContentEditor and the 4th is
    * the PSCmsObject. Should be accessed for reading by using the getItemDef,
    * getItemDefSummary, getContentEditorDef and getCmsObject methods.
    * <p>
    * Only modified by the registerDef and unRegisterDef methods. A hash table
    * is used rather than synchronizing all of the methods that access it.
    * Constructed in class init, then never <code>null</code>.
    */
   private Map<Long, List<Object>> m_itemDefMap = new ConcurrentHashMap<>();

   /**
    * Used to store a list of {@link IPSItemDefChangeListener} objects. The list
    * is initialized during construction, and never <code>null</code>, but
    * may be empty thereafter. The list is modified in the methods
    * {@link #addListener} and {@link #removeListener}.
    */
   private Collection<IPSItemDefChangeListener> m_itemDefListeners = new ArrayList<>();

   /**
    * Used to store the current type id being registered to avoid returning
    * content types that are not yet "available" to external users. Set and
    * reset in {@link #registerDef(String, PSContentEditor)} and checked in
    * {@link #getVisibleContentTypes(int, boolean)}
    */
   private long m_currentRegisteredId = -1;

   /**
    * Constant indicating all or any community. This can be used to specify not
    * to filter content type defiitions by community.
    */
   static public final int COMMUNITY_ANY = -1;

   /**
    * This is set to true in {@link #deferUpdateNotifications()} and checked in
    * {@link #commitUpdateNotifications()}. This serves the purpose of keeping
    * the item def manager from telling the search subsystem to notify on
    * update. This is done to prevent the search server from restarting after
    * every change registered by MSM.
    */
   private boolean m_deferringNotifications = false;

   /**
    * Tracks whether an update event has occurred that would have required
    * notifications. Reset in {@link #commitUpdateNotifications()}. Contains
    * items of class {@link PSItemDefinition}.
    */
   private Collection<PSItemDefinition> m_deferredNotifications = new ArrayList<>();
}
