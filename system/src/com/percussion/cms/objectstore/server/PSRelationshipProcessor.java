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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This class encapsulates all relationship functionality provided on the
 * server side.
 */
public class PSRelationshipProcessor implements IPSRelationshipProcessor
{
   private static final Logger log = LogManager.getLogger(PSRelationshipProcessor.class);
   /**
    * Create the backend processor for the supplied request.
    *
    * @see {@link PSRelationshipProcessor(IPSRequestContext)}.
    */
   public PSRelationshipProcessor()
   {
      m_dbProcessor = PSRelationshipDbProcessor.getInstance();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#add(
    * java.lang.String, java.lang.String, java.util.List, com.percussion.
    * cms.objectstore.PSKey)
    */
   public void add(
      String componentType,
      String relationshipType,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      validateComponentType(componentType);
      add(relationshipType, children, (PSLocator) targetParent);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#add(java.
    * lang.String, java.util.List, com.percussion.design.objectstore.PSLocator)
    */
   public void add(
      String relationshipType,
      List children,
      PSLocator targetParent)
      throws PSCmsException
   {
      PSRelationshipConfig config = getConfig(relationshipType);

      PSRelationshipSet set = new PSRelationshipSet();
      Iterator dependentKeys = children.iterator();
      while (dependentKeys.hasNext())
      {
         PSLocator dependent = validateKey((PSKey) dependentKeys.next());
         set.add(
            new PSRelationship(
               -1,
               validateKey(targetParent),
               dependent,
               config));
      }
      m_dbProcessor.modifyRelationships(set);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#copy(java.lang.String, java.util.List, com.percussion.cms.objectstore.PSKey)
    */
   public void copy(String relationshipType, List children, PSKey parent)
      throws PSCmsException
   {
      throw new UnsupportedOperationException("Not supported by this processor.");
   }

   /**
    * Deletes all supplied relationships.
    *
    * @param relationships the relationships to be deleted, not
    *    <code>null</code>, may be empty.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public void delete(PSRelationshipSet relationships) throws PSCmsException
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");

      m_dbProcessor.deleteRelationships(relationships);
   }

   /**
    * Delete all relationships with the relationship ids that are in the array 
    * of the provided relationship ids which have the supplied type and owner.
    *
    * @param relationshipType the relationship type to delete, may be 
    * <code>null</code> but not empty, in which case the dependents of all 
    * relationship types will be deleted. An exception is thrown if the 
    * requested type was not found in the relationship configuration.
    * @param owner the owner from which to delete the dependents, must be 
    * a valid key to a persisted object, see {@link #validateKey(PSKey)} for 
    * whats a valid key.
    * @param rids an array of relationship ids to be deletded. If 
    * <code>null</code>, returns immediately.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public void delete(
      String relationshipType,
      PSKey owner,
      int[] rids)
      throws PSCmsException
   {
      if (rids == null)
         return;
         
      PSRelationshipSet deletes = new PSRelationshipSet();
      //Get all the relationships irrespective of community and permissions
      PSRelationshipSet relationships =
         getDependents(
            relationshipType,
            owner,
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY
               | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS);
   
      Arrays.sort(rids);
   
      Iterator it = relationships.iterator();
      while (it.hasNext())
      {
         PSRelationship relationship = (PSRelationship) it.next();
         int id = relationship.getId();
         if (Arrays.binarySearch(rids, id) >= 0)
            deletes.add(relationship);
      }
   
      m_dbProcessor.deleteRelationships(deletes);
   }

   /**
    * Deletes all relationships of the specified type between the supplied 
    * owner and dependents. Provide <code>null</code> for type and dependents 
    * to delete all relationships for the supplied owner.
    *
    * @param relationshipType the relationship type to delete, may be 
    * <code>null</code> but not empty, in which case the dependents of all 
    * relationship types will be deleted. An exception is thrown if the 
    * requested type was not found in the relationship configuration.
    * @param owner the owner of the relationships to delete , must be a 
    * valid key to a persisted object, see {@link #validateKey(PSKey)} for 
    * what is a valid key.
    * @param dependents a list of <code>PSKey</code> objects that are 
    * dependents of the relationships to be deleted, <code>null</code> to 
    * delete all relationships for the specified type. Dependents that do not 
    * have any relationship to the owner of the specified type will silently be 
    * skipped.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public void delete(String relationshipType, PSKey owner, List dependents)
      throws PSCmsException
   {
      PSRelationshipSet deletes = new PSRelationshipSet();
      //Get all the relationships irrespective of community and permissions
      PSRelationshipSet relationships =
         getDependents(
            relationshipType,
            owner,
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY
               | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS);

      Iterator it = relationships.iterator();
      while (it.hasNext())
      {
         PSRelationship relationship = (PSRelationship) it.next();
         if (dependents == null
            || containsDependent(dependents.iterator(), relationship))
         {
            deletes.add(relationship);
         }
      }

      m_dbProcessor.deleteRelationships(deletes);
   }

   /**
    * Convenience method that calls
    * {@link #getAncestors(String, PSKey, int)
    * getAncestors(String, PSKey, 0)}
    */
   public PSRelationshipSet getAncestors(String relationshipType, PSKey owner)
      throws PSCmsException
   {
      return getAncestors(relationshipType, owner, 0);
   }

   /**
    * Get all ancestorsts for the supplied object.
    *
    * @param relationshipType the relationship type used to get the ancestors,
    *  not <code>null</code> or empty.
    * @param object the object key for which to get the ancestors, must be a 
    * valid key to a persisted object, see {@link #validateKey(PSKey)} for 
    * whats a valid key.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships 
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx 
    * constants defined in <code>PSRelationshipConfig</code> to restrict 
    * filtering on both), should be set to <code>0</code> if filtering is to 
    * be performed.
    * @return a <code>PSRelationshipSet</code>, never <code>null</code>, may 
    * be empty.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public PSRelationshipSet getAncestors(
      String relationshipType,
      PSKey object,
      int doNotApplyFilters)
      throws PSCmsException
   {
      return m_dbProcessor.queryRelationships(
         getConfig(relationshipType),
         validateKey(object),
         true,
         doNotApplyFilters);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getChildren(
    * java.lang.String, com.percussion.cms.objectstore.PSKey)
    */
   public PSComponentSummary[] getChildren(
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      List locatorList = getDependentLocators(relationshipType, parent);

      return getComponentSummaries(locatorList);
   }

   /**
    * This API checks incase a relationship with
    *   Same ownerId , same dependentId, same RelationshipConfig, and same revisions
    *   already exists, tehn don't create a new Relationship.
    * @param rel
    * @return
    */
   public PSRelationship checkIfRelationshipAlreadyExists(PSRelationship rel){
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      String name = rel.getConfig().getName();
      if (name != null && name.trim().length() > 0)
         filter.setName(name);

      int ownerId = rel.getOwner().getId();
      if (ownerId != -1)
      {
         int rev = rel.getOwner().getRevision();
         filter.setOwner(new PSLocator(ownerId, rev));
         filter.limitToOwnerRevision(true);
      }

      int dependentId = rel.getDependent().getId();
      if (dependentId != -1) {
         filter.setDependent(new PSLocator(dependentId, rel.getDependent().getRevision()));
      }

      String slotId = rel.getProperty(IPSHtmlParameters.SYS_SLOTID);
      if (slotId != null && !slotId.trim().isEmpty())
         filter.setProperty(IPSHtmlParameters.SYS_SLOTID, slotId + "");
      try {
         PSRelationshipSet relSet = processor.getRelationships(filter);
         if(relSet == null){
            return null;
         }
         for (Object o : relSet) {
            PSRelationship rel1 = (PSRelationship) o;
            if (rel1.getConfig().equals(rel.getConfig())) {
               if (rel1.getDependent().getRevision() == rel.getDependent().getRevision()) {
                  if (rel1.getOwner().getRevision() == rel.getOwner().getRevision()) {
                     //Checking this slot id to resolve the page summary issue if same image is uploaded in page summary and image widget for the same page (CMS-7800).
                     if (rel.getUserProperty(PSRelationshipConfig.PDU_SLOTID) != null && rel1.getUserProperty(PSRelationshipConfig.PDU_SLOTID) != null) {
                        if (rel1.getUserProperty(PSRelationshipConfig.PDU_SLOTID).getValue().equalsIgnoreCase(rel.getUserProperty(PSRelationshipConfig.PDU_SLOTID).getValue())) {
                           return rel1;
                        }
                     } else {
                        return rel1;
                     }
                  }
               }
            }
         }
      } catch (PSCmsException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(e);
      }
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getChildren(
    * java.lang.String, java.lang.String, com.percussion.cms.objectstore.PSKey)
    */
   public PSComponentSummary[] getChildren(
      String type,
      String relationshipType,
      PSKey parent)
      throws PSCmsException
   {
      validateComponentType(type);
      List locatorList = getDependentLocators(relationshipType, parent);

      return getComponentSummaries(locatorList);
   }

   /**
    * Convenience method that calls
    * {@link #getDependentLocators(String, PSKey, int)
    * getDependentLocators(String, PSKey, 0)}
    */
   public List getDependentLocators(String type, PSKey owner)
      throws PSCmsException
   {
      return getDependentLocators(type, owner, 0);
   }

   /**
    * Catalogs all dependents of the supplied owner that are related through
    * the provided relationship type.
    *
    * @param relationshipType the relationship type to catalog, may be <code>null</code>
    *    but not empty, in which case the dependents of all relationship types
    *    will be returned. An exception is thrown if the requested type was
    *    not found in the relationship configuration.
    * @param owner the owner for which to catalog the dependents, must be
    *    a valid key to a persisted object, see {@link #validateKey(PSKey)} for
    *    whats a valid key.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a list of <code>PSLocator</code> objects addressing dependents of
    *    the supplied owner. Never <code>null</code>, may be empty.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public List getDependentLocators(
      String relationshipType,
      PSKey owner,
      int doNotApplyFilters)
      throws PSCmsException
   {
      PSRelationshipSet relationships =
         getDependents(relationshipType, owner, doNotApplyFilters);

      List result = new ArrayList();
      Iterator it = relationships.iterator();
      while (it.hasNext())
      {
         PSRelationship relationship = (PSRelationship) it.next();
         if (relationship.getConfig().getName().equalsIgnoreCase(relationshipType))
            result.add(relationship.getDependent());
      }

      return result;
   }

   /**
    * Convenience method that calls {@link #getDependents(String, PSKey, int)}.
    * passes last param - doNotApplyFilters = 0;
    */
   public PSRelationshipSet getDependents(String type, PSKey owner)
      throws PSCmsException
   {
      return getDependents(type, owner, 0);
   }

   /**
    * Catalogs all dependents of the supplied owner that are related through
    * the provided relationship type.
    *
    * @param type the relationship type to catalog, may be <code>null</code>
    *    but not empty, in which case the dependents of all relationship types
    *    will be returned. An exception is thrown if the requested type was
    *    not found in the relationship configuration.
    * @param owner the owner for which to catalog the dependents, must be
    *    a valid key to a persisted object, see {@link #validateKey(PSKey)} for
    *    whats a valid key.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a <code>PSRelationshipSet</code>, never <code>null</code>, may
    *    be empty.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public PSRelationshipSet getDependents(
      String type,
      PSKey owner,
      int doNotApplyFilters)
      throws PSCmsException
   {
      return m_dbProcessor.queryRelationships(
         getConfig(type),
         validateKey(owner),
         doNotApplyFilters);
   }

   /**
    * Convenience method that calls
    * {@link #getParents(String, PSKey, int)
    * getParents(String, PSKey, 0)}
    */
   public List getParents(String type, PSKey owner) throws PSCmsException
   {
      return getParents(type, owner, 0);
   }

   /**
    * Get all parents for the supplied object.
    *
    * @param type the relationship type used to get the parents, not
    *    <code>null</code> or empty.
    * @param object the object key for which to get the parents, must be
    *    a valid key to a persisted object, see {@link #validateKey(PSKey)} for
    *    whats a valid key.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a list of <code>PSLocator</code> objects for all parents found.
    *    Never <code>null</code>, may be empty.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public List getParents(String type, PSKey object, int doNotApplyFilters)
      throws PSCmsException
   {
      Iterator relationships =
         m_dbProcessor
            .queryRelationships(
               getConfig(type),
               validateKey(object),
               true,
               doNotApplyFilters)
            .iterator();

      List parents = new ArrayList();
      while (relationships.hasNext())
         parents.add(((PSRelationship) relationships.next()).getOwner());

      return parents;
   }

   /**
    * See {@link IPSRelationshipProcessor#getParents(String, String, PSKey)
    * interface}
    */
   public PSComponentSummary[] getParents(
      String type,
      String relationshipType,
      PSKey locator)
      throws PSCmsException
   {
      List locatorList = getParents(relationshipType, locator);

      return getComponentSummaries(locatorList);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(com.percussion.cms.objectstore.PSRelationshipFilter)
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter)
      throws PSCmsException
   {
      return m_dbProcessor.getRelationships(filter);
   }
   
   /**
    * Just like {@link #getRelationships(PSRelationshipFilter)}, except this
    * returns all relationships in a list.
    * 
    * @return a list of zero or more relationships with no particular order,
    *    never <code>null</code>, but may be empty.
    *    
    * @see IPSRelationshipProcessor#getRelationships(PSRelationshipFilter)
    * 
    * @throws PSCmsException if an error occurred while retrieving relationships.
    */
   public List<PSRelationship> getRelationshipList(PSRelationshipFilter filter)
      throws PSCmsException
   {
      return m_dbProcessor.getRelationshipList(filter);
   }   

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(java.lang.String, com.percussion.design.objectstore.
    * PSLocator, boolean)
    */
   public PSRelationshipSet getRelationships(
      String relationshipType,
      PSLocator locator,
      boolean owner)
      throws PSCmsException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(relationshipType);
      if (owner)
         filter.setOwner(locator);
      else
         filter.setDependent(locator);

      return getRelationships(filter);
   }

   /**
    * Convenience method that calls {@link #getSiblings(String, PSKey, int)}
    * passes doNotApplyFilters = 0.
    */
   public List getSiblings(String type, PSKey owner) throws PSCmsException
   {
      return getSiblings(type, owner, 0);
   }

   /**
    * Get all siblings for the supplied object.
    *
    * @param type the relationship type used to get the siblings, not
    *    <code>null</code> or empty.
    *
    * @param object the object key for which to get the siblings, must be
    *    a valid key to a persisted object, see {@link #validateKey(PSKey)} for
    *    whats a valid key.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a list of <code>List</code> objects for each parent found, each
    *    containing a <code>List</code> of <code>PSKey</code> objects for all
    *    siblings found in that particular parent. The returned list does not
    *    include the object key itself. Never <code>null</code>, may be empty.
    *
    * @throws PSCmsException if any errors occur processing the request.
    */
   public List getSiblings(String type, PSKey object, int doNotApplyFilters)
      throws PSCmsException
   {
      validateKey(object);
      PSLocator locator = (PSLocator) object;
      List siblings = new ArrayList();

      Iterator parents = getParents(type, object).iterator();
      while (parents.hasNext())
      {
         PSKey parent = (PSKey) parents.next();

         List parentSiblings = new ArrayList();
         Iterator relationships =
            m_dbProcessor
               .queryRelationships(
                  getConfig(type),
                  validateKey(parent),
                  doNotApplyFilters)
               .iterator();
         while (relationships.hasNext())
         {
            PSRelationship sibling = (PSRelationship) relationships.next();

            // don't add object itself
            if (!equalsDependent(locator, sibling))
               parentSiblings.add(sibling.getDependent());
         }

         siblings.addAll(parentSiblings);
      }

      return siblings;
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getSummaries(
    * com.percussion.cms.objectstore.PSRelationshipFilter, boolean)
    */
   public PSComponentSummaries getSummaries(
      PSRelationshipFilter filter,
      boolean owner)
      throws PSCmsException
   {
      PSRelationshipSet relationships = getRelationships(filter);
      if (relationships.isEmpty())
         return new PSComponentSummaries();

      Set itemSet = new HashSet();
      Iterator iter = relationships.iterator();
      while (iter.hasNext())
      {
         PSRelationship rel = (PSRelationship) iter.next();
         PSLocator loc = owner ? rel.getOwner() : rel.getDependent();
         itemSet.add(loc);
      }
      
      return PSServerFolderProcessor.getInstance().getComponentSummaries(itemSet.iterator(), null, false);

   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getSummaryByPath(java.lang.String, java.lang.String, java.lang.String)
    */
   public PSComponentSummary getSummaryByPath(
      String componentType,
      String path,
      String relationshipTypeName)
      throws PSCmsException
   {
      int itemId = getIdByPath(componentType, path, relationshipTypeName);
      if (itemId == -1)
         return null;

      Collection locators = new ArrayList(1);
      locators.add(new PSLocator(itemId));

      return getComponentSummaries(locators)[0];
   }
   
   /**
    * Just like {@link #getSummaryByPath(String, String, String)}, except it
    * returns the content id of the supplied path.
    *   
    * @return the content id. Returns <code>-1</code> if there is no such 
    *    relationship path exists in the system.
    */
   public int getIdByPath(
      String componentType,
      String path,
      String relationshipTypeName)
      throws PSCmsException
   {
      validateComponentType(componentType);

      return m_dbProcessor.getIdByPath(path, relationshipTypeName);
   }

   /**
    * Just like {@link #add(String, PSKey, List)}, but in addition to that
    * the relationships are inserted at the given index into the existing
    * order of relationships for the supplied type and owner. This requires
    * that the relationship configuration for the supplied type defines the
    * system property <code>sys_sortrank</code>. An exception is thrown if
    * <code>sys_sortrank</code> is not defined.
    *
    * @param type the relationship type to create, not <code>null</code> or
    *    empty. An exception is thrown if the requested type was not found
    *    in the relationship configuration.
    * @param owner the owner of the new relationships beeing created, must be
    *    a valid key to a persisted object, see {@link #validateKey(PSKey)} for
    *    whats a valid key.
    * @param dependents a list of <code>PSKey</code> of all dependents which
    *    need to be related to the supplied owner. All supplied keys must
    *    reference persisted objects and must be valid, see
    *    {@link #validateKey(PSKey)} for whats a valid key.
    * @param index the index where to insert the new relationships, must be > 0.
    *    If the index is bigger then the current size of relationships, the
    *    new dependents will be appendend.
    * @throws PSCmsException if any errors occur processing the request.
    */
   public void insert(String type, PSKey owner, List dependents, int index)
      throws PSCmsException
   {
      if (index <= 0)
         throw new IllegalArgumentException("index must be > 0");

      PSRelationshipConfig config = getConfig(type);
      if (config.getSystemProperty(IPSHtmlParameters.SYS_SORTRANK) == null)
         throw new PSCmsException(
            IPSCmsErrors.INVALID_INSERT_RELATIONSHIP_TYPE,
            type);

      // get existing relationships
      PSRelationshipSet resultSet = new PSRelationshipSet();
      PSRelationshipSet relationships = getDependents(type, owner);

      int sortrank = index;
      if (relationships.size() < index)
         sortrank = relationships.size() + 1;

      // build the insert set starting the sortrank at the supplied index
      PSRelationshipSet inserts = new PSRelationshipSet();
      Iterator dependentKeys = dependents.iterator();
      while (dependentKeys.hasNext())
      {
         PSLocator dependent = validateKey((PSKey) dependentKeys.next());
         PSRelationship relationship =
            new PSRelationship(-1, validateKey(owner), dependent, config);
         relationship.setProperty(
            IPSHtmlParameters.SYS_SORTRANK,
            Integer.toString(sortrank++));

         inserts.add(relationship);
      }

      // move existing relationships after the last new insert
      for (int i = 0; i < relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         String strSortrank =
            relationship.getProperty(IPSHtmlParameters.SYS_SORTRANK);
         if (Integer.parseInt(strSortrank) >= index)
         {
            relationship.setProperty(
               IPSHtmlParameters.SYS_SORTRANK,
               Integer.toString(sortrank++));
            resultSet.add(relationship);
         }
      }

      resultSet.addAll(inserts);
      m_dbProcessor.modifyRelationships(resultSet);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#move(
    * java.lang.String, com.percussion.design.objectstore.PSLocator, 
    * java.util.List, com.percussion.design.objectstore.PSLocator)
    */
   public void move(
      String relationshipTypeName,
      PSLocator sourceParent,
      List children,
      PSLocator targetParent)
      throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children must not be null");
      if (sourceParent == null)
         throw new IllegalArgumentException("sourceParent must not be null");
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent must not be null");

      if (children.isEmpty())
         return;

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(sourceParent);
      filter.setName(relationshipTypeName);
      
      // do not filter by community as the dependent items community is
      // irrelevant in a move
      filter.setCommunityFiltering(false);
      
      PSRelationshipSet relationships = getRelationships(filter);
      //filter out relationships that do not have children in the supplied list
      for (int i = relationships.size() - 1; i >= 0; i--)
      {
         PSRelationship rel = (PSRelationship) relationships.get(i);
         if (!listContains(children,
            rel.getDependent(),
            rel.getConfig().useDependentRevision()))
         {
            relationships.remove(i);
         }
      }


      for (Object relObj : relationships)
      {
          PSRelationship element = (PSRelationship) relObj;
          log.debug("Modifying relationships setting old Owner: {} : to {} ", element.getOwner(), targetParent);
         element.setOwner(targetParent);
      }
      log.debug("Attempting to save relationship changes");
      save(relationships);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#move(
    * java.lang.String, com.percussion.cms.objectstore.PSKey, java.util.List, 
    * com.percussion.cms.objectstore.PSKey)
    */
   public void move(
      String relationshipTypeName,
      PSKey sourceParent,
      List children,
      PSKey targetParent)
      throws PSCmsException
   {
      move(
         relationshipTypeName,
         (PSLocator) sourceParent,
         children,
         (PSLocator) targetParent);
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#save(
    * com.percussion.design.objectstore.PSRelationshipSet)
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException
   {
      m_dbProcessor.modifyRelationships(relationships);
   }



   /**
    * Get the database processor.
    *
    * @return the database processor, never <code>null</code>.
    */
   protected PSRelationshipDbProcessor getDbProcessor()
   {
      return m_dbProcessor;
   }

   /**
    * Tests whether the dependent of a relationship is contained in the
    * provided list of <code>PSKey</code> objects. Containment is tested using
    * the id if the dependent revision is ignored; otherwise using the
    * keys <code>equal</code> methods.
    *
    * @param possibleDeps a list of <code>PSLocator</code> objects, assumed not
    *    <code>null</code>, may be empty.
    * @param relationship the relationship to test, assumed not
    *    <code>null</code>.
    *
    * @return <code>true</code> if the list contains the supplied locator,
    *    <code>false</code> otherwise.
    * @throws PSCmsException if the supplied list contains invalid locators.
    */
   private boolean containsDependent(
      Iterator possibleDeps,
      PSRelationship relationship)
      throws PSCmsException
   {
      boolean found = false;

      while (possibleDeps.hasNext() && (!found))
      {
         PSLocator test = validateKey((PSKey) possibleDeps.next());
         if (equalsDependent(test, relationship))
            found = true;
      }

      return found;
   }

   /**
    * Determines whether a locator equals the dependent of a relationship.
    *
    * @param locator The to be compared locator, assume not <code>null</code>.
    *
    * @param relationship The to be compared relationship, assume not
    *    <code>null</code>.
    *
    * @return <code>true</code> if the locator equals the dependent of the
    *    relationship; otherwise return <code>false</code>.
    */
   private boolean equalsDependent(
      PSLocator locator,
      PSRelationship relationship)
   {
      PSRelationshipConfig config = relationship.getConfig();
      PSLocator dependent = relationship.getDependent();
      boolean equal = false;

      if (!config.useDependentRevision())
         equal = locator.getId() == dependent.getId();
      else
         equal = locator.equals(dependent);

      return equal;
   }

   /**
    * Uses Local PSComponentProcessorProxy to get ComponentSummaries
    * of a given collection of PSLocators.
    * @param locators collection of PSLocator objects,
    * never <code>null</code>, may be <code>empty</code>.
    * @return array of PSComponentSummary objects,
    * never <code>null</code>, may be <code>empty</code>.
    * 
    * @throws PSCmsException if encounters any problems while
    * fetching or parsing the XML document.
    */
   private PSComponentSummary[] getComponentSummaries(Collection locators)
      throws PSCmsException
   {
      if (locators.isEmpty())
         return new PSComponentSummary[0];

      PSComponentSummaries sums = PSServerFolderProcessor.getInstance().getComponentSummaries(locators.iterator(), null, true);
      
      return sums.toArray();
   }

   /**
    * Get the relationship config for the supplied type.
    *
    * @param relationshipType the relationship type for which to get the configuration,
    *    not <code>null</code> or empty.
    * @return the relationship configuration, may be <code>null</code>.
    * @throws PSCmsException if the supplied relationship type is not defined
    *    in the system.
    */
   public PSRelationshipConfig getConfig(String relationshipType)
      throws PSCmsException
   {
      if (relationshipType == null || relationshipType.trim().length() == 0)
         throw new IllegalArgumentException("type cannot be null or empty");

      PSRelationshipConfig config =
         PSRelationshipCommandHandler.getRelationshipConfig(relationshipType);

      return config;
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationshipOwnerPaths(java.lang.String, com.percussion.design.
    * objectstore.PSLocator, java.lang.String)
    */
   public String[] getRelationshipOwnerPaths(
      String componentType,
      PSLocator locator,
      String relationshipTypeName)
      throws PSCmsException
   {
      validateComponentType(componentType);
      if (locator == null)
         throw new IllegalArgumentException("locator must not be null");

      if (!relationshipTypeName
         .equals(PSRelationshipConfig.TYPE_FOLDER_CONTENT) && !relationshipTypeName.equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT))
      {
         throw new UnsupportedOperationException(
            "getRelationshipOwnerPaths() is not implemented for relationship type : "
               + relationshipTypeName);
      }
      try
      {
         return m_dbProcessor.getOwnerPaths(locator, relationshipTypeName);
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSCmsException(e);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * isDescendent(com.percussion.design.objectstore.PSLocator, com.percussion.
    * design.objectstore.PSLocator, java.lang.String)
    */
   public boolean isDescendent(
      String componentType,
      PSLocator parent,
      PSLocator child,
      String relationshipTypeName)
      throws PSCmsException
   {
      validateComponentType(componentType);
      if (parent == null)
         throw new IllegalArgumentException("parent must not be null");
      if (child == null)
         throw new IllegalArgumentException("child must not be null");
      if (relationshipTypeName == null)
         throw new IllegalArgumentException("relationshipTypeName must not be null");

      List ancestors =
         m_dbProcessor.getOwnerLocators(child, relationshipTypeName);
      Iterator iter = ancestors.iterator();
      while (iter.hasNext())
      {
         PSLocator element = (PSLocator) iter.next();
         if (element.getId() == parent.getId())
            return true;
      }
      
      return false;
   }
   
   // see interface for method details
   public PSKey[] getDescendentsLocators(
      String type,
      String relationshipTypeName,
      PSKey parent)
      throws PSCmsException
   {
      if (relationshipTypeName == null)
         throw new IllegalArgumentException("relationshipTypeName must not be null");
      
      List results = new ArrayList();
      getDescendentLocators(relationshipTypeName, parent, results);

      return (PSKey[])results.toArray(new PSKey[results.size()]);
   }
   
   /**
    * Helper method to recursively retrieve all descendent locators
    * 
    * @param relationshipTypeName the relationship type name, assume not 
    * <code>null</code> or empty.
    * @param parent A valid key that references the current owner of
    *    the relationships to all the supplied children. A valid key is one
    *    that references an existing object in the database. Never
    *    <code>null</code>.
    * @param results A List that will be used to store the results. It is
    *    a list over zero or more {@link PSLocator} objects. Assume it is
    *    not <code>null</code>, may be empty.
    * 
    * @throws PSCmsException if any error occurs
    */
   private void getDescendentLocators(
      String relationshipTypeName,
      PSKey parent,
      List results)
      throws PSCmsException
   {
      
      PSRelationshipSet dependents = getDependents(relationshipTypeName, parent);
      Iterator rels = dependents.iterator();
      PSRelationship rel;
      PSLocator depLocator;
      while (rels.hasNext())
      {
         rel = (PSRelationship) rels.next();
         depLocator = rel.getDependent();

         // First check to be sure that this item has
         // not yet been processed, we don't want to
         // accidently fall into an infinite recursion loop if
         // a child some how links back to itself
         if(results.contains(depLocator))
            continue;
         
         results.add(depLocator);
         
         getDescendentLocators(relationshipTypeName, depLocator, results);            
      }      
   }

   /**
    * Validates a component type. It must be <code>COMPONENT_TYPE</code>.
    * 
    * @param type
    *           The component type to be validated.
    *  
    */
   private void validateComponentType(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (!type
         .equalsIgnoreCase(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE))
      {
         throw new IllegalArgumentException(
            "type cannot be "
               + type
               + ". "
               + "It must be "
               + PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE);
      }

   }

   /**
    * Validates the supplied key. Must not be <code>null</code>, must be
    * persisted and must be of type <code>PSLocator</code>.
    * The supplied key cast to the proper type.
    *
    * @param key the key to validate, not <code>null</code> and of type
    *    <code>PSLocator</code>.
    * @return a <code>PSLocator</code>, never <code>null</code>.
    * @throws PSCmsException if the supplied key is not persisted or of
    *    expected type.
    */
   private PSLocator validateKey(PSKey key) throws PSCmsException
   {
      if (key == null)
         throw new IllegalArgumentException("key may not be null");

      if (!key.isPersisted())
         throw new PSCmsException(IPSCmsErrors.PERSISTED_KEY_EXPECTED);

      if (!(key instanceof PSLocator))
      {
         Object[] args = { PSLocator.class, key.getClass().getName()};
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_KEY_TYPE, args);
      }

      return (PSLocator) key;
   }

   /**
    * Helper method to check if a locator list contains the specified locator.
    * Comparison can be revision sensitive.
    * @param locatorList list of {@link com.percussion.design.objectstore.PSLocator locators}
    *                    to check in, assumed not <code>null</code> may be empty.
    * @param locator {@link com.percussion.design.objectstore.PSLocator locator} 
    * to check for, assumed not <code>null</code>.
    * @param revisionSensitive <code>true</code> is the check has to be 
    * revision sensitive, <code>false</code> otherwise. 
    * @return <code>true</code> if the list contains the specified locator, 
    * <code>false</code> otherwise.
    */
   private boolean listContains(
      List locatorList,
      PSLocator locator,
      boolean revisionSensitive)
   {
      if (revisionSensitive)
         return locatorList.contains(locator);

      Iterator iter = locatorList.iterator();
      while (iter.hasNext())
      {
         PSLocator element = (PSLocator) iter.next();
         if (element.getId() == locator.getId())
            return true;
      }
      return false;
   }

   public static PSRelationshipProcessor getInstance()
   {
       return instance;
   }


   /**
    * Request object used to process various operations. Initialized by the
    * constructor, never <code>null</code> after that.
    */
   protected IPSRequestContext m_request;

   /**
    * The relationship database processor, initialized in constructor, never
    * <code>null</code> or modified after that.
    */
   private PSRelationshipDbProcessor m_dbProcessor = null;
   
   
   private static final String TRANSACTIONAL_RELATIONSHIP_PROC= "useTransactionalRelationshipProcessing";
   private static final PSRelationshipProcessor instance = new PSRelationshipProcessor();


}
