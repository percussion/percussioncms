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
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSDependent;
import com.percussion.cms.objectstore.PSDependentSet;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSRelationshipUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * This class encasulates all server side functionality for active assembly.
 * This only deals with relationships of category
 * <code>PSRelationshipConfig.CATEGORY_ATIVE_ASSEMBLY</code>.
 */
public class PSActiveAssemblerProcessor extends PSRelationshipProcessor
{
   /**
    * Create the backend processor for the supplied request.
    *
    * @param type the relationship type on which to operate, must be of category
    *    <code>PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY</code>. If
    *    <code>null</code> is supplied, the default
    *    <code>PSRelationshipConfig.TYPE_RELATED_CONTENT</code> is used.
    * @throws PSCmsException if no configuration was found for the requested
    *    type or if the requested type is not of category
    *    <code>PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY</code>.
    */
   public PSActiveAssemblerProcessor()
   {
      super();
   }

   /**
    * Create the backend processor for the supplied context.
    *
    * @param context the request context to use, if <code>null</code> is
    *    provided, the internal rhythmyx user will be used to perform the
    *    requests.
    * @param type the relationship type on which to operate, must be of category
    *    <code>PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY</code>. If
    *    <code>null</code> is supplied, the default
    *    <code>PSRelationshipConfig.TYPE_RELATED_CONTENT</code> is used.
    * @throws PSCmsException if no configuration was found for the requested
    *    type or if the requested type is not of category
    *    <code>PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY</code>.
    */
   public PSActiveAssemblerProcessor(IPSRequestContext context)
      throws PSCmsException
   {
      super();
   }

   /**
    * Inserts all supplied dependents for the provided owner into the
    * relationship table. This method assumes that the <code>sys_sortrank</code>
    * in all existing relationships is continious.
    *
    * @param owner the owner for the newly create relationships,
    *    not <code>null</code>.
    * @param dependents a list of dependents for which to create a
    *    relation to the owner, not <code>null</code>, may be empty.  All
    *    dependents must specify the same value for the sys_slotid
    *    property.
    * @param index the index where to insert the new relationships, must be > 0.
    *    If the index is bigger then the current size of relationships, the
    *    new dependents will be appendend.
    * @throws PSCmsException if all dependents do not specify the same slot, or
    * if anything goes wrong processing the request.
    */
   public void insert(PSLocator owner, PSDependentSet dependents, int index)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");

      if (dependents == null)
         throw new IllegalArgumentException("dependents cannot be null");

      if (index <= 0)
         throw new IllegalArgumentException("index must be > 0");

      // validate and retrieve the slot id of the dependents
      String slotId = checkPropertyValue(IPSHtmlParameters.SYS_SLOTID,
         owner.getId(), dependents);

      PSRelationshipConfig config = getConfigForSlot(m_request, slotId);

      // get existing relationships for this slot
      PSRelationshipSet resultSet = new PSRelationshipSet();
      PSRelationshipSet relationships = getDependents(
         config.getName(), owner, IPSHtmlParameters.SYS_SLOTID, slotId);

      int sortrank = index;
      if (relationships.size() < index)
         sortrank = relationships.size() + 1;

      // build the insert set starting the sortrank at the supplied index
      PSRelationshipSet inserts = new PSRelationshipSet();
      Iterator dependentKeys = dependents.iterator();
      while (dependentKeys.hasNext())
      {
         PSDependent dependent = (PSDependent) dependentKeys.next();
         PSRelationship relationship = new PSRelationship(-1, owner,
            dependent.getLocator(), config);

         updateProperties(relationship, dependent);

         // set the sortrank no matter what they supplied in the properties
         relationship.setProperty(IPSHtmlParameters.SYS_SORTRANK,
            Integer.toString(sortrank++));

         inserts.add(relationship);
      }

      // move existing relationships after the last new insert
      for (int i=0; i< relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         String strSortrank =
            relationship.getProperty(IPSHtmlParameters.SYS_SORTRANK);
         if (Integer.parseInt(strSortrank) >= index)
         {
            relationship.setProperty(IPSHtmlParameters.SYS_SORTRANK,
               Integer.toString(sortrank++));
            resultSet.add(relationship);
         }
      }

      resultSet.addAll(inserts);
      getDbProcessor().modifyRelationships(resultSet);
   }

   /**
    * Makes an internal request to get the name of the relationship type allowed
    * for the slot. Returns the matching relationship object by relationship
    * type name.
    * @param request request context to make an internal request to the Rhythmyx
    * resource, must not be <code>null</code>.
    * @param slotid slotid to get the configuration for the allowed relationship
    * type. Must not be <code>null</code> ro empty.
    * @return relationship configuration based on the allowed relationship type
    * for the slot. May be <code>null</code> if matching relationship config is
    * not found (Which is a very rare case).
    * @throws PSNotFoundException if the resource for internal request is not
    * found.
    * @throws IllegalArgumentException if the request context supplied is
    * <code>null</code> and slotid supplied is <code>null</code> or empty.
    */
   static public PSRelationshipConfig getConfigForSlot(
      IPSRequestContext request, String slotid)
      throws PSCmsException
   {
      if(request == null)
         throw new IllegalArgumentException("request must not be null");
      if(slotid == null || slotid.length()<1)
         throw new IllegalArgumentException("sloyid must not be null or empty");

      try
      {
         String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT
            + "/" + SLOT_RELATIONSHIP_LOOKUP;

         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_SLOTID, slotid);

         IPSInternalRequest ir = request.getInternalRequest(resource, params,
            false);

         if (ir == null)
         {
            Object[] args =
            {
               resource,
               "No request handler found."
            };
            throw new PSCmsException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
         }
         Document doc = ir.getResultDoc();
         NodeList slots = doc.getElementsByTagName("Slot");
         String relName = "";
         if(slots.getLength() > 0)
         {
            Element slot = (Element)slots.item(0);
            relName = slot.getAttribute("aaRelType");
            if(relName.length()<1)
               relName = PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;
         }
         PSRelationshipConfig cfg =
            PSRelationshipCommandHandler.getRelationshipConfig(relName);
         return cfg;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * Deletes all relationships between the owner and the dependents.
    *
    * @param owner the owner for which to delete the relationships, not
    *    <code>null</code>.
    * @param dependents the dependents for which to delete the relationships,
    *    not <code>null</code> may be empty.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public void delete(PSLocator owner, PSDependentSet dependents)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");

      if (dependents == null)
         throw new IllegalArgumentException("dependents cannot be null");

      Map slotDependentMap = buildSlotDependentMap(dependents);
      Iterator slots = slotDependentMap.keySet().iterator();
      String slotid = null;
      List deps = null;
      while(slots.hasNext())
      {
         slotid = (String)slots.next();
         deps = (List)slotDependentMap.get(slotid);
         if(deps == null)
            continue;
         int relationshipIds[] = new int[dependents.size()];
         for (int i=0; i<deps.size(); i++)
            relationshipIds[i] = ((PSDependent)deps.get(i)).getRelationshipId();
         delete(getConfigForSlot(m_request, slotid).getName(), owner,
            relationshipIds);
      }
   }

   /**
    * Helper method that builds the map of slotid and the associated dependent
    * from the dependent set.
    * @param dependents dependent set to categorize by slot, assumed
    * not <code>null</code>
    * @return map of slotid (String) and dependents for this slot (List), never
    * <code>null</code> may be empty.
    */
   private Map buildSlotDependentMap(PSDependentSet dependents)
   {
      Map slotDependentMap = new HashMap();
      PSDependent dependent = null;
      for(int i=0; i<dependents.size();i++)
      {
         dependent = (PSDependent)dependents.get(i);
         PSPropertySet props = dependent.getProperties();
         String slotid = props.getProperty(
            IPSHtmlParameters.SYS_SLOTID).getValue().toString();
         if(slotid == null || slotid.length()<1)
            continue;
         List deps = (List)slotDependentMap.get(slotid);
         if(deps == null)
         {
            deps = new ArrayList();
            slotDependentMap.put(slotid, deps);
         }
         deps.add(dependent);
      }
      return slotDependentMap;
   }

   /**
    * Reorders the supplied dependents for the provided owner. This method
    * assumes that the <code>sys_sortrank</code> in all existing relationships
    * is continious starting with 1.
    *
    * @param owner the owner to reorder the relationships for, not
    *    <code>null</code>.
    * @param dependents the dependents to be reordered, not <code>null</code>,
    *    may be empty.  All dependents must specify the same value for the
    *    sys_slotid property.
    * @param index the index where to insert the new relationships, must be > 0.
    *    If the index is bigger then the current size of relationships, the
    *    dependents will be moved to the end.
    * @throws PSCmsException if all dependents do not specify the same slot, or
    * if anything goes wrong processing the request.
    */
   public void reorder(PSLocator owner, PSDependentSet dependents, int index)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");

      if (dependents == null)
         throw new IllegalArgumentException("dependents cannot be null");

      if (index <= 0)
         throw new IllegalArgumentException("index must be > 0");

      // validate and retrieve the slot id of the dependents
      String slotId = checkPropertyValue(IPSHtmlParameters.SYS_SLOTID,
         owner.getId(), dependents);

      // get existing relationships
      PSRelationshipSet relationships = getSortedRelationships(owner, slotId);

      // build the reorder relationship set
      PSRelationshipSet reorderSet = new PSRelationshipSet();
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         PSDependent dependent = contains(dependents.iterator(), relationship);
         if (dependent != null)
         {
            updateProperties(relationship, dependent);
            reorderSet.add(relationship);
         }
      }

      // build the result set
      boolean inserted = false;
      PSRelationshipSet resultSet = new PSRelationshipSet();
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);

         int sortrank = Integer.parseInt(relationship.getProperty(
            IPSHtmlParameters.SYS_SORTRANK));

         if (!inserted && sortrank == index)
         {
            resultSet.addAll(reorderSet);
            inserted = true;
         }
         PSDependent dependent = contains(dependents.iterator(), relationship);
         if (dependent == null)
            resultSet.add(relationship);
      }

      // need to append the list if its not been inserted to the results set yet
      if (!inserted)
         resultSet.addAll(reorderSet);

      // normalize the result set
      normalizeSortRank(resultSet);

      getDbProcessor().modifyRelationships(resultSet);
   }

   /**
    * Get the all current relationships for the specified slot id, sorted
    * ascending to their sort rank property with sort rank normalized starting
    * at 1 continiously incrementing.
    *
    * @param owner the owner for which to get the sorted relationships, assumed
    *    not <code>null</code>.
    * @param slotId The slot id to use to filter the returned relationships,
    * assumed not <code>null</code> or empty.
    *
    * @return a collection of relationships sorted ascending based on their
    *    sort rank property, never <code>null</code>, may be empty.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   private PSRelationshipSet getSortedRelationships(PSLocator owner,
      String slotId) throws PSCmsException
   {
      // get existing relationships
      PSRelationshipSet relationships = getDependents(
         getConfigForSlot(m_request, slotId).getName(), owner,
         IPSHtmlParameters.SYS_SLOTID, slotId);

      // create a map sorted based on the sort rank property
      Map relationshipMap = new TreeMap();
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         String sortRank = relationship.getProperty(
            IPSHtmlParameters.SYS_SORTRANK);

         Integer intRank = null;
         try
         {
            intRank = new Integer(sortRank);
         }
         catch (NumberFormatException nfe)
         {
            Object[] args =
            {
               "" + owner.getId(), "" + relationship.getDependent().getId(),
               IPSHtmlParameters.SYS_SORTRANK, sortRank
            };
            throw new PSCmsException(
               IPSCmsErrors.INVALID_RELATIONSHIP_PROP_VALUE, args);
         }

         PSRelationshipSet set =
            (PSRelationshipSet) relationshipMap.get(intRank);
         if (set == null)
         {
            set = new PSRelationshipSet();
            relationshipMap.put(intRank, set);
         }

         set.add(relationship);
      }

      PSRelationshipSet resultSet = new PSRelationshipSet();
      Iterator keys = relationshipMap.keySet().iterator();
      while (keys.hasNext())
      {
         Integer key = (Integer) keys.next();
         resultSet.addAll((PSRelationshipSet) relationshipMap.get(key));
      }
      normalizeSortRank(resultSet);

      return resultSet;
   }

   /**
    * Normalizes the sort rank property of the supplied relationships starting
    * at 1 continiously incremented.
    *
    * @param relationships a collection of relationships to normalize, assumed
    *    not <code>null</code>, may be empty.
    */
   private void normalizeSortRank(PSRelationshipSet relationships)
   {
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         relationship.setProperty(IPSHtmlParameters.SYS_SORTRANK,
            Integer.toString(i+1));
      }
   }

   /**
    * Updates all relationships between the owner and the dependents.
    *
    * @param owner the relationship owner for which to update the relationships,
    *    not <code>null</code>.
    * @param dependents the dependents which contain all the update
    *    informations, not <code>null</code>, may be empty.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public void update(PSLocator owner, PSDependentSet dependents)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");

      if (dependents == null)
         throw new IllegalArgumentException("dependents cannot be null");

      PSRelationshipSet updates = new PSRelationshipSet();

      Map slotDepMap = buildSlotDependentMap(dependents);
      Iterator slots = slotDepMap.keySet().iterator();
      PSRelationshipSet relationships = new PSRelationshipSet();
      while(slots.hasNext())
      {
         String slotid = (String)slots.next();
         relationships.addAll(getDependents(
            getConfigForSlot(m_request, slotid).getName(), owner));
      }

      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         PSDependent dependent = contains(dependents.iterator(), relationship);
         if (dependent != null)
         {
            updateProperties(relationship, dependent);
            updates.add(relationship);
         }
      }
      getDbProcessor().modifyRelationships(updates);
   }

   /**
    * Updates the properties in the supplied relationship with the properties
    * from the provided depenent. Properties with <code>null</code> values
    * will be skipped.
    *
    * @param relationship the relationship in which to update the properties,
    *    assumed not <code>null</code>.
    * @param dependent the dependent containing the properties to which the
    *    relationship is updated, assumed not <code>null</code>.
    */
   private void updateProperties(PSRelationship relationship,
      PSDependent dependent)
   {
      Iterator properties = dependent.getProperties().iterator();
      while (properties.hasNext())
      {
         PSProperty property = (PSProperty) properties.next();
         Object value = property.getValue();
         if (value != null)
            relationship.setProperty(property.getName(), value.toString());
      }
   }


   /**
    * Tests whether the supplied relationship is contained in the provided list
    * of <code>PSDependent</code> objects. Containment is tested using the
    * relationship id.
    *
    * @param list a list of <code>PSDependent</code> objects, assumed not
    *    <code>null</code>, may be empty.
    * @param relationship the relationship to test, assumed not <code>null</code>.
    * @return the matching <code>PSDependent</code> if the list contains the
    *    supplied locator, <code>null</code> otherwise.
    * @throws PSCmsException if the supplied list contains invalid locators.
    */
   private PSDependent contains(Iterator list, PSRelationship relationship)
      throws PSCmsException
   {
      while (list.hasNext())
      {
         PSDependent dependent = (PSDependent) list.next();
         int test = dependent.getRelationshipId();
         if (test == relationship.getId())
            return dependent;
      }
      return null;
   }

   /**
    * Gets the value of the specified property name from the dependent set,
    * validating that all dependents have the same non-<code>null</code>
    * non-empty value for the specified property.
    *
    * @param propertyName The property name, assumed not <code>null</code> or
    * empty.
    * @param owner The id of the owner, used for error messages.
    * @param dependents The dependent set, assumed not <code>null</code> or
    * empty.
    *
    * @return The property value, never <code>null</code> or empty.
    *
    * @throws PSCmsException if the specified property is not found, or if
    * all dependents in the <code>dependents</code> set do not have the same
    * value.
    */
   private String checkPropertyValue(String propertyName, int owner,
      PSDependentSet dependents) throws PSCmsException
   {
      String propVal = null;
      Iterator deps = dependents.iterator();
      while (deps.hasNext())
      {
         PSDependent dep = (PSDependent)deps.next();
         PSPropertySet props = dep.getProperties();
         PSProperty prop = props.getProperty(propertyName);
         if (prop == null || prop.getType() != PSProperty.TYPE_STRING ||
            prop.getValue() == null ||
            prop.getValue().toString().trim().length() == 0)
         {
            Object[] args = {String.valueOf(owner), String.valueOf(
               dep.getLocator().getId()), propertyName, "null"};
            throw new PSCmsException(
               IPSCmsErrors.INVALID_RELATIONSHIP_PROP_VALUE, args);
         }

         String tmpVal = prop.getValue().toString();
         if (propVal == null)
         {
            propVal = tmpVal;
         }
         else if (!propVal.equals(tmpVal))
         {
            Object[] args = {String.valueOf(owner), String.valueOf(
               dep.getLocator().getId()), propertyName, tmpVal};
            throw new PSCmsException(
               IPSCmsErrors.INVALID_RELATIONSHIP_PROP_VALUE, args);
         }
      }

      return propVal;
   }

   /**
    * Catalogs all dependents of the supplied owner that are related through
    * the provided relationship type, that contain the specified property
    * value.  Delegates dependency retrieval to
    * <code>super.getDependents(type, owner)</code>.  Only additional
    * information is specified here.
    *
    * @param propName The name of the property to filter on, assumed not
    * <code>null</code> or empty.
    * @param propVal The value of the property, assumed not <code>null</code> or
    * empty.
    *
    * @return a <code>PSRelationshipSet</code>, never <code>null</code>, may
    *    be empty.
    */
   private PSRelationshipSet getDependents(String type, PSLocator owner,
      String propName, String propVal) throws PSCmsException
   {
      PSRelationshipSet dependents = new PSRelationshipSet();

      PSRelationshipSet relationships = getDependents(type, owner);
      for (int i=0; i<relationships.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) relationships.get(i);
         if (propVal.equals(relationship.getProperty(propName)))
            dependents.add(relationship);
      }

      return dependents;
   }

   /**
    * Get the relationships for all related content attached to the supplied
    * item locator unfiltered by community.
    * 
    * @param locator the locator of the item for which to lookup the related 
    *    content relationships, not <code>null</code>.
    * @return the relationships of all related content unfiltered by community, 
    *    never <code>null</code>, may be empty.
    * @throws PSCmsException for any error making the lookup.
    */
   public PSRelationshipSet getRelatedContent(PSLocator locator) 
      throws PSCmsException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(locator);
      filter.setCommunityFiltering(false);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      filter.limitToOwnerRevision(true);
      return getRelationships(filter);
   }

   public static PSActiveAssemblerProcessor getInstance()
   {
      return aaInstance;
   }
   
   /**
    * Name of the Rhythmyx resource that produces an XML document with slotid
    * and allowed relationship type name.
    */
   static public final String SLOT_RELATIONSHIP_LOOKUP = "slotrelation";
   
   private static final PSActiveAssemblerProcessor aaInstance = new PSActiveAssemblerProcessor();


}
