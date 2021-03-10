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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class allows one to specify filtering criteria when querying for
 * relationships by the implementer of the interface {@link 
 * com.percussion.cms.objectstore.IPSRelationshipProcessor}. The aim is to be
 * able to specify the filtering conditions easily. The following rules apply
 * when using the filter conditions.
 * <p>
 * <ol>
 * <li> All specified conditions will be ANDed except for relationship name,
 * type, and category. These 3 properties are OR'd together before they are
 * AND'ed with the other properties. For example, if category and a property
 * name-value is set, the results include the relationships that match the
 * category and the property value. If the <em>translation<em> category and the 
 * <em>Active Assembly Mandatory<em> translation name are both specified, any
 * relationship that is either AA mandatory or has a category of translation 
 * will be returned.</li>
 * <li> The order of using filter parameters (if specified) will be as follows:
 * </li>
 * <ol>
 * <li>relationship id</li>
 * <li>relationship name</li>
 * <li>relationship owner</li>
 * <li>relationship dependent</li>
 * <li>item contenttypeid</li>
 * <li>item objecttypeid</li>
 * <li>relationship category</li>
 * <li>relationship type</li>
 * <li>relationship properties</li>
 * </ol>
 * </li>
 * </ol>
 * <p>
 * 
 * @author RammohanVangapalli
 */
public class PSRelationshipFilter
{

   /**
    * Default constructor
    */
   public PSRelationshipFilter()
   {
   }
   
   /**
    * Constructs an instance of its XML representaion.
    * 
    * @param src the XML representation of the object, never <code>null</code>.
    */
   public PSRelationshipFilter(Element src)
   {
      fromXml(src);
   }

   /**
    * Constructs an instance from a specified filter. The new instance is a 
    * shallow copy of the specified one.
    * 
    * @param other the to be shallow copied filter; it may not be 
    *    <code>null</code>.
    */
   public PSRelationshipFilter(PSRelationshipFilter other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null.");
      
      m_category = other.m_category;
      m_dependentContentTypeIds = other.m_dependentContentTypeIds;
      m_dependentobjecttype = other.m_dependentobjecttype;
      m_dependents = other.m_dependents;
      m_filterByCommunity = other.m_filterByCommunity;
      m_limitToEditOrCurrentOwnerRevision = other.m_limitToEditOrCurrentOwnerRevision;
      m_limitToTipOwnerRevision = other.m_limitToTipOwnerRevision;
      m_limitToOwnerRevision = other.m_limitToOwnerRevision;
      m_limitToPublicOwnerRevision = other.m_limitToPublicOwnerRevision;
      m_names = other.m_names;
      m_owner = other.m_owner;
      m_ownercontenttypeid = other.m_ownercontenttypeid;
      m_ownerobjecttype = other.m_ownerobjecttype;
      m_rid = other.m_rid;
      m_type = other.m_type;
   }
   
   /**
    * Accessor for the relationship category filter.
    *
    * @return the name of the relationship category by which to filter,
    *    <code>null</code> or empty if not filtered by relationship category.
    * @see #setCategory(String)
    */
   public String getCategory()
   {
      return m_category;
   }

   /**
    * Setter for the relationship category filter.
    * <p>
    * Note: if the category is {@link #FILTER_CATEGORY_FOLDER}, then the
    * name will be set to {@link #FILTER_NAME_FOLDER_CONTENT}, since there
    * is only one relationship type in folder category.
    * <p>
    * The category is OR'd with the name(s) and type(s) before AND'ing with the
    * other properties.
    *
    * @param category the relationship category to be filtered by,
    *    <code>null</code> or empty to turn the relationship category filter
    *    off. Must be one of the <code>FILTER_CATEGORY_XXX</code> values
    *    defined in this class if not <code>null</code> or empty.
    */
   public void setCategory(String category)
   {
      if (category != null)
      {
         category = category.trim();
         if (category.length() > 0)
         {
            boolean found = false;
            for (int i=0; !found && i<FILTERS_BY_CATEGORY.length; i++)
               found = FILTERS_BY_CATEGORY[i].equals(category);

            if (!found)
               throw new IllegalArgumentException(
                  "category must be one of FILTER_CATEGORY_XXX");
         }
      }

      m_category = category;
      if (FILTER_CATEGORY_FOLDER.equals(m_category))
         setName(FILTER_NAME_FOLDER_CONTENT);
   }

   /**
    * Accessor for the relationship type filter.
    *
    * @return the name of the relationship type by which to filter,
    *    <code>null</code> or empty if not filtered by relationship type.
    * @see #setType(String)
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Setter for the relationship type filter.
    * <p>
    * The type is OR'd with the name(s) and category(ies) before AND'ing with
    * the other properties.
    * 
    * @param type the relationship type to be filtered by, <code>null</code>
    * or empty to turn the relationship type filter off. Must be one of the
    * <code>FILTER_TYPE_XXX</code> values defined in this class if not
    * <code>null</code> or empty.
    */
   public void setType(String type)
   {
      if (type != null)
      {
         type = type.trim();
         if (type.length() > 0)
         {
            boolean found = false;
            for (int i=0; !found && i<FILTERS_BY_TYPE.length; i++)
               found = FILTERS_BY_TYPE[i].equals(type);

            if (!found)
               throw new IllegalArgumentException(
                  "type must be one of FILTER_TYPE_XXX");
         }
      }

      m_type = type;
   }

   /**
    * Accessor for the relationship name filter. 
    * Note, this is used in conjunction with {@link #setName(String)}.
    *
    * @return one relationship name by which to filter, <code>null</code> 
    *   if not filtered by relationship name.
    *   
    * @see #setName(String)
    */
   public String getName()
   {
      if (m_names.isEmpty())
         return null;
      else
         return m_names.iterator().next();
   }

   /**
    * Get a list of relationship names that will be filtered by.
    * 
    * @return the relationship names, may be empty, never <code>null</code>.
    */
   public Collection<String> getNames()
   {
      return m_names;
   }
   
   /**
    * Limit the retrieved relationships to the supplied relationship names.
    * <p>
    * The name(s) are OR'd with the category(ies) and type(s) before AND'ing
    * with the other properties.
    * 
    * @param names the relationship names to be filtered by. Filter by name is
    * off if it is empty or <code>null</code>. All white space characters
    * will be stripped from the names since the space character is not allowed
    * in the relationship name
    */
   public void setNames(Collection<String> names)
   {
      m_names.clear();
      
      if (names == null || names.isEmpty())
      {
         return;
      }
      else
      {
         for (String name : names)
            m_names.add(stripSpaceChars(name));
      }
   }
   
   /**
    * Setter for the relationship name filter. This is used when filtered by
    * only one relationship name.
    *
    * @param name the relationship name to be filtered by, <code>null</code>
    *    or empty to turn the relationship name filter off. Any name is
    *    allowed because the user can create his (or her) own relationships and 
    *    may want to filter by the relationship name. However, all white space
    *    characters will be stripped from the name since the space character is
    *    not allowed in the relationship name.
    */
   public void setName(String name)
   {
      m_names.clear();
      if (name != null && name.trim().length() > 0)
         m_names.add(stripSpaceChars(name));
   }
   
   /**
    * Strips all white space characters from the supplied relationship name.
    * 
    * @param name the relationship name, assumed not <code>null</code>
    * 
    * @return the relationship name without white space characters.
    */
   private String stripSpaceChars(String name)
   {
      StringBuffer buffer = new StringBuffer();
      for (int i=0; i<name.length(); i++)
      {
         if (!Character.isWhitespace(name.charAt(i)))
            buffer.append(name.charAt(i));
      }

      return buffer.toString();
   }

   /**
    * Accessor for the relationship property filter.
    *
    * @return all relationship properties to be filtered by, never
    *    <code>null</code>, may be empty if not filtered for any relationship
    *    property.
    * @see #setProperty(String, String)
    */
   public Map<String, String> getProperties()
   {
      return m_properties;
   }

   /**
    * Set a relationship property value by which to filter. Properties that do
    * exist will be overridden, otherwise the new property is added.
    *
    * @param name the property name, must not be <code>null</code> or empty.
    * @param value the property value to be filtered by. The filter for the
    *    property is turned off if the value is <code>null</code> or empty.
    */
   public void setProperty(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");

      m_properties.put(name, (value == null) ? value : value.trim());
   }

   /**
    * Accessor for the relationship dependent locator filter.
    *
    * @return may be <code>null</code> if not filtered by the relationshp
    *    dependent. It is the first element of the dependent list if there are
    *    more than one.
    *    
    * @see #setDependent(PSLocator)
    */
   public PSLocator getDependent()
   {
      if (m_dependents == null || m_dependents.isEmpty())
         return null;
      else
         return m_dependents.get(0);
   }
   
   /**
    * Accessor for getting a list of dependent locators of the filter.
    *
    * @return a list of dependent locators. It may be <code>null</code> if not 
    *    filtered by the relationshp dependent.
    *    
    * @see #setDependents(Collection)
    */
   public List<PSLocator> getDependents()
   {
      return m_dependents;
   }

   /**
    * Set the relationship dependent locator to filter by.
    *
    * @param locator the relationship dependent locator to filter by,
    *    <code>null</code> to turn this filter off.
    */
   public void setDependent(PSLocator locator)
   {
      if (locator == null)
      {
         setDependents(null);
      }
      else
      {
         List<PSLocator> dep = new ArrayList<>();
         dep.add(locator);
         setDependents(dep);
      }
   }

   /**
    * Set the relationship to filter by more than one dependents.
    *
    * @param locators the relationship dependent locators to filter by,
    *    <code>null</code> or empty to turn this filter off.
    */
   public void setDependents(Collection<PSLocator> locators)
   {
      if (locators == null || locators.isEmpty())
      {
         m_dependents = null;
      }
      else
      {
         resetDependents();
         m_dependents.addAll(locators);
      }
   }
   
   /**
    * Set the relationship dependent id (disregard dependent revision) to 
    * filter by.
    *
    * @param dependentId the relationship dependent id to filter by,
    *    <code>-1</code> to turn this filter off.
    */
   public void setDependentId(int dependentId)
   {
      if (dependentId == -1)
      {
         setDependentIds(null);
      }
      else
      {
         List<Integer> dep = new ArrayList<>();
         dep.add(dependentId);
         setDependentIds(dep);
      }
   }

   /**
    * Set the dependent ids (disregard dependent revision) for the filter. 
    *
    * @param dependentIds the relationship dependent ids to filter by,
    *    <code>null</code> or empty to turn this filter off.
    */
   public void setDependentIds(Collection<Integer> dependentIds)
   {
      if (dependentIds == null || dependentIds.isEmpty())
      {
         setDependents(null);
      }
      else
      {
         List<PSLocator> locs = new ArrayList<>();
         for (Integer id : dependentIds)
            locs.add(new PSLocator(id, -1));
         
         setDependents(locs);
      }
   }
   
   /**
    * Accessor for the relationship owner locator filter.
    *
    * @return may be <code>null</code> if not filtered by the relationship
    *    owner.
    * @see #setOwner(PSLocator)
    */
   public PSLocator getOwner()
   {
      return m_owner;
   }

   /**
    * Set the relationship owner locator to filter by.
    *
    * @param locator the relationship owner locator to filter by,
    *    <code>null</code> to turn this filter off.
    */
   public void setOwner(PSLocator locator)
   {
      m_owner = locator;
   }

   /**
    * Set the relationship owner id (disregard owner revision) to filter by.
    *
    * @param ownerId the relationship owner id to filter by
    *    <code>-1</code> to turn this filter off.
    */
   public void setOwnerId(int ownerId)
   {
      if (ownerId == -1)
         m_owner = null;
      else
         m_owner = new PSLocator(ownerId, -1);
   }
   
   /**
    * Accessor for the relationship id to be filtered by.
    *
    * @return the relationship id to filter by, -1 if this filter is turned off.
    * @see #setRelationshipId(int)
    */
   public int getRelationshipId()
   {
      return m_rid;
   }

   /**
    * Setter for the relationship id to be filtered by. 
    *
    * @param rid the relationship id to filter by, -1 to turn this filter off.
    *    Values lower as -1 will be set to -1.
    */
   public void setRelationshipId(int rid)
   {
      m_rid = (rid >= 0) ? rid : -1;
   }

   /**
    * Accessor for the owner contenttype id to be filtered by.
    *
    * @return the owner contenttype id to filter by, -1 if this filter is
    *    turned off.
    * @see #setOwnerContentTypeId(long)
    */
   public long getOwnerContentTypeId()
   {
      return m_ownercontenttypeid;
   }

   /**
    * Setter for the owner contenttype id to be filtered by.
    * <p>
    * Note, this must not be used in conjunction with 
    * {@link #setDependentContentTypeId(long)}.
    *
    * @param id the owner contenttype id to filter by, -1 to turn this filter
    *    off. Values lower as -1 will be set to -1.
    */
   public void setOwnerContentTypeId(long id)
   {
      if (getDependentContentTypeId() != -1)
         throw new IllegalStateException("Cannot set owner and dependent content type id at the same time.");

      m_ownercontenttypeid = (id >= 0) ? id : -1;
   }

   /**
    * Accessor for the dependent contenttype id to be filtered by.
    *
    * @return the dependent contenttype id to filter by, -1 if this filter is
    *    turned off. It is the first content type id if there are more than
    *    one content type ids.
    *    
    * @see #setDependentContentTypeId(long)
    */
   public long getDependentContentTypeId()
   {
      if (m_dependentContentTypeIds == null)
         return -1;
      else
         return m_dependentContentTypeIds.get(0).longValue();
   }

   /**
    * Gets the dependent contenttype ids of the filter.
    *
    * @return the dependent contenttype ids. It may be <code>null</code> if 
    *    this filter is turned off.
    *    
    * @see #setDependentContentTypeIds(Collection)
    */
   public List<Long> getDependentContentTypeIds()
   {
      return m_dependentContentTypeIds;
   }
   
   /**
    * Setter for the dependent contenttype id to be filtered by.
    * <p>
    * Note, this must not be used in conjunction with neither
    * {@link #setOwnerContentTypeId(long)} nor
    * {@link #limitToEditOrCurrentOwnerRevision(boolean) limitToEditOrCurrentOwnerRevision(true)}.
    * 
    * @param id the dependent contenttype id to filter by, -1 to turn this
    *           filter off. Values lower than -1 is the same as -1.
    */
   public void setDependentContentTypeId(long id)
   {
      if (id <= -1)
      {
         setDependentContentTypeIds(null);
      }
      else
      {
         List<Long> ids = new ArrayList<>();
         ids.add(id);
         setDependentContentTypeIds(ids);
      }
   }

   /**
    * Setter for the dependent contenttype ids to be filtered by.
    * <p>
    * Note, this must not be used in conjunction with neither
    * {@link #setOwnerContentTypeId(long)} nor
    * {@link #limitToEditOrCurrentOwnerRevision(boolean) limitToEditOrCurrentOwnerRevision(true)}.
    * 
    * @param ids the dependent contenttype ids to filter by, <code>null</code> 
    *       to turn this filter off.
    */
   public void setDependentContentTypeIds(Collection<Long> ids)
   {
      if (getOwnerContentTypeId() != -1)
         throw new IllegalStateException("Cannot set owner and dependent content type id at the same time.");
      if (getLimitToEditOrCurrentOwnerRevision()
            || getLimitToPublicOwnerRevision() || getLimitToTipOwnerRevision())
         throw new IllegalStateException("Cannot set dependent content type id in conjunction with limitToTipOwnerRevision(true) or limitToEditOrCurrentOwnerRevision(true) or getLimitToPublicOwnerRevision(true).");

      if (ids == null)
      {
         m_dependentContentTypeIds = null;
      }
      else
      {
         resetDependentContentTypeIds();
         m_dependentContentTypeIds.addAll(ids);
      }
   }

   
   /**
    * Accessor for the owner objecttype to be filtered by.
    *
    * @return the owner objecttype to filter by, -1 if this filter is
    *    turned off.
    * @see #setOwnerObjectType(int)
    */
   public int getOwnerObjectType()
   {
      return m_ownerobjecttype;
   }

   /**
    * Setter for the owner objecttype to be filtered by.
    * <p>
    * Note, this must not be used in conjunction with 
    * {@link #setDependentObjectType(int)}.
    *
    * @param type the owner objecttype to filter by, -1 to turn this filter
    *    off. Values lower as -1 will be set to -1.
    */
   public void setOwnerObjectType(int type)
   {
      if (getDependentObjectType() != -1)
         throw new IllegalStateException("Cannot set owner and dependent object types at the same time");
      
      m_ownerobjecttype = (type >= 0) ? type : -1;
   }

   /**
    * Accessor for the dependent objecttype to be filtered by.
    *
    * @return the dependent objecttype to filter by, -1 if this filter is
    *    turned off.
    * @see #setDependentObjectType(int)
    */
   public int getDependentObjectType()
   {
      return m_dependentobjecttype;
   }

   /**
    * Setter for the dependent objecttype to be filtered by.
    * <p>
    * Note, this must not be used in conjunction with neither 
    * {@link #setOwnerObjectType(int)} nor
    * {@link #limitToEditOrCurrentOwnerRevision(boolean) limitToEditOrCurrentOwnerRevision(true)}.
    *
    * @param type the dependent objecttype to filter by, -1 to turn this filter
    *    off. Values lower as -1 will be set to -1.
    */
   public void setDependentObjectType(int type)
   {
      if (getOwnerObjectType() != -1)
         throw new IllegalStateException("Cannot set owner and dependent object types at the same time");
      if (getLimitToEditOrCurrentOwnerRevision()
            || getLimitToPublicOwnerRevision()
            || getLimitToTipOwnerRevision())
         throw new IllegalStateException(
               "Cannot set dependent object type in conjunction with limitToTipOwnerRevision(true) limitToEditOrCurrentOwnerRevision(true) or getLimitToPublicOwnerRevision(true).");

      m_dependentobjecttype = (type >= 0) ? type : -1;
   }

   /**
    * Enable or disable community filtering for the relationships/components.
    *
    * @param enableCommunityFiltering <code>true</code> to enable and
    *    <code>false</code> to disable. Community filtering is enabled by
    *    default.
    */
   public void setCommunityFiltering(boolean enableCommunityFiltering)
   {
      m_filterByCommunity = enableCommunityFiltering;
   }

   /**
    * Does this filter by community?
    *
    * @return <code>true</code> if community filtering is enabled,
    *    <code>false</code> otherwise.
    */
   public boolean isCommunityFilteringEnabled()
   {
      return m_filterByCommunity;
   }
   
   /**
    * Does this filter only specify properties? If so, the processor can
    * optimize and do that search only up front instead of querying the entire
    * relationships table and then filtering the results
    * 
    * @return <code>true</code> if there are no non-default values other than
    *         the properties.
    */
   public boolean isPurePropertiesFilter()
   {
      return getProperties().size() > 0 && getDependent() == null
            && getNames().isEmpty() && !getLimitToOwnerRevision()
            && !getLimitToEditOrCurrentOwnerRevision()
            && !getLimitToTipOwnerRevision()
            && (getCategory() == null || getCategory().trim().length() == 0)
            && getRelationshipId() == -1
            && (getType() == null || getType().trim().length() == 0)
            && getDependentContentTypeId() == -1
            && getDependentObjectType() == -1 && getOwner() == null
            && getOwnerContentTypeId() == -1 && getOwnerObjectType() == -1;
   }   

   /**
    * Setter for using owner revision to be filtered by. This setting can only
    * take effect when the owner locator is also provided (via 
    * {@link #setOwner(PSLocator)}).
    * 
    * @param limitToOwnerRev <code>true</code> if is filtered by the owner revision. 
    */
   public void limitToOwnerRevision(boolean limitToOwnerRev)
   {
      m_limitToOwnerRevision = limitToOwnerRev;
   }
   
   /**
    * Setter for using the public revision of the owner to be filtered by. This
    * is typically used in conjunction with {@link #setDependent(PSLocator)}.
    *  
    * @param limitToPublicOwnerRev <code>true</code> if is filtered by the
    *   public revision of the owner.
    */
   public void limitToPublicOwnerRevision(boolean limitToPublicOwnerRev)
   {
      m_limitToPublicOwnerRevision = limitToPublicOwnerRev;
   }
   
   /**
    * Indicates whether it is filtered by the public revision of the owner. This
    * is typically used in conjunction with {@link #setDependent(PSLocator)}.
    * 
    * @return <code>true</code> if is filtered by the public revision of the
    *   owner; otherwise return <code>false</code>. Default to 
    *   <code>false</code>.
    */
   public boolean getLimitToPublicOwnerRevision()
   {
      return m_limitToPublicOwnerRevision;
   }
   
   /**
    * Gets the owner revision filtered by.
    * 
    * @return <code>true</code> if is filtered by the owner revision. Defaults
    *    to <code>false</code>.
    * 
    * @see #limitToOwnerRevision(boolean)
    */
   public boolean getLimitToOwnerRevision()
   {
      return m_limitToOwnerRevision;
   }
   
   /**
    * The relationships are limited to the Edit Revision (if it exists) or
    * the Current Revision (if Edit Revision does not exist) of the owner. 
    * This is typically used in conjunction with {@link #setDependent(PSLocator)}.
    * <p>
    * Note, this must not be used in conjunction with 
    * {@link #setDependentContentTypeId(long)} or 
    * {@link #setDependentObjectType(int)}
    *
    * @param limitToEditCurrOwnerRev <code>true</code> if the owner revision of the  
    *    relationships are limited by Edit or Current revision as described 
    *    above; otherwise the retrieved relationships may contain multiple 
    *    owner revisions for a particular (dependent) item.
    */
   public void limitToEditOrCurrentOwnerRevision(boolean limitToEditCurrOwnerRev)
   {
      if (limitToEditCurrOwnerRev && m_dependentContentTypeIds != null)
         throw new IllegalStateException("Cannot limit owner edit or current revision in conjunction with setDependentContentTypeId(int)");
      if (limitToEditCurrOwnerRev && m_dependentobjecttype != -1)
         throw new IllegalStateException("Cannot use owner edit or current revision in conjunction with setDependentObjectType(int)");
      
      m_limitToEditOrCurrentOwnerRevision = limitToEditCurrOwnerRev;
   }
   
   /**
    * The relationships are limited to the Tip Revision of the owner. 
    * This will pick up relationship changes on currently checked out items
    * but can be used to treat a relationship as if it were revisionless.
    * This is typically used in conjunction with {@link #setDependent(PSLocator)}.
    * <p>
    * Note, this must not be used in conjunction with 
    * {@link #setDependentContentTypeId(long)} or 
    * {@link #setDependentObjectType(int)}
    *
    * @param limitToTipOwnerRevision <code>true</code> if the owner revision of the
    *    relationships are limited by the Tip revision as described 
    *    above; otherwise the retrieved relationships may contain multiple 
    *    owner revisions for a particular (dependent) item.
    */
   public void limitToTipOwnerRevision(boolean limitToTipOwnerRevision)
   {
      if (limitToTipOwnerRevision && m_dependentContentTypeIds != null)
         throw new IllegalStateException("Cannot limit owner edit or current revision in conjunction with setDependentContentTypeId(int)");
      if (limitToTipOwnerRevision && m_dependentobjecttype != -1)
         throw new IllegalStateException("Cannot use owner edit or current revision in conjunction with setDependentObjectType(int)");
      
      m_limitToTipOwnerRevision = limitToTipOwnerRevision;
   }
   
   
   /**
    * Gets the filtered by Edit or Current revision of the owner of the 
    * retrieved relationships.
    * 
    * @return <code>true</code> if the owner revision of the relationships are
    *    filtered by (or limited to) the Edit Revision (if it exists) or
    *    the Current Revision (if Edit Revision does not exist) of the owner; 
    *    otherwise the retrieved relationships may contain multiple owner 
    *    revisions for a particular (dependent) item. 
    *    Defaults to <code>false</code>. 
    * 
    * @see #limitToEditOrCurrentOwnerRevision(boolean)
    */
   public boolean getLimitToEditOrCurrentOwnerRevision()
   {
      return m_limitToEditOrCurrentOwnerRevision;
   }
   
   /**
    * Gets the filtered by Edit or Current revision of the owner of the 
    * retrieved relationships.
    * 
    * @return <code>true</code> if the owner revision of the relationships are
    *    filtered by (or limited to) the Edit Revision (if it exists) or
    *    the Current Revision (if Edit Revision does not exist) of the owner; 
    *    otherwise the retrieved relationships may contain multiple owner 
    *    revisions for a particular (dependent) item. 
    *    Defaults to <code>false</code>. 
    * 
    * @see #limitToEditOrCurrentOwnerRevision(boolean)
    */
   public boolean getLimitToTipOwnerRevision()
   {
      return m_limitToTipOwnerRevision;
   }
   
   
   
   /**
    * @see {@link #getLimitToCrossSiteLinks().
    */
   private boolean m_limitToCrossSiteLinks = false;
   
   /**
    * Determines whether to filter on cross site links, where
    * the relationships with none null value of site and/or folder IDs.
    * 
    * @return <code>true</code> if the returned relationships
    * contains none <code>null</code> values of site and/or folder IDs.
    */
   public boolean getLimitToCrossSiteLinks()
   {
      return m_limitToCrossSiteLinks;
   }
   
   /**
    * Sets to filter on cross site links.
    * 
    * @param isLimitToCrossSite it is <code>true</code> if does filter
    * on cross site links, where relationships have none null site 
    * and/or folder IDs.
    */
   public void setLimitToCrossSiteLinks(boolean isLimitToCrossSite)
   {
      m_limitToCrossSiteLinks = isLimitToCrossSite;
   }
   
   /**
    * Restores the filter values from XML. See {@link #toXml(Document)}
    * for DTD details.
    * @param elem the Element node that represents the
    * <code>PSRelationshipFilter</code> object, cannot be <code>null</code>.
    */
   public void fromXml(Element elem)
   {
      if(elem == null)
         throw new IllegalArgumentException("Element cannot be null.");
      String value = null;

      value = elem.getAttribute(XML_ATTR_RID);
      if(value != null && value.trim().length() > 0)
         m_rid = Integer.parseInt(value);
      else
         m_rid = -1;

      value = elem.getAttribute(XML_ATTR_TYPE);
      if(value != null && value.trim().length() > 0)
         m_type = value;
      else
         m_type = null;

      value = elem.getAttribute(XML_ATTR_CATEGORY);
      if(value != null && value.trim().length() > 0)
         m_category = value;
      else
         m_category = null;

      value = elem.getAttribute(XML_ATTR_COMMUNITY_FILTERING);
      if(value != null && value.trim().length() > 0)
         m_filterByCommunity = Boolean.valueOf(value).booleanValue();
      else
         m_filterByCommunity = true;

      try
      {
         NodeList nl = null;
         Element locator = null;
         Element owner = null;
         nl = elem.getElementsByTagName(XML_ELEM_OWNER);
         if((owner = (Element)nl.item(0)) != null)
         {
            value = owner.getAttribute(XML_ATTR_CONTENT_TYPE_ID);
            if(value != null && value.trim().length() > 0)
               m_ownercontenttypeid = Integer.parseInt(value);
            else
               m_ownercontenttypeid = -1;
            value = owner.getAttribute(XML_ATTR_OBJECT_TYPE);
            if(value != null && value.trim().length() > 0)
               m_ownerobjecttype = Integer.parseInt(value);
            else
               m_ownerobjecttype = -1;
            nl = owner.getElementsByTagName(PSLocator.XML_NODE_NAME);
            locator = (Element)nl.item(0);
            if(locator != null)
               m_owner = new PSLocator(locator);
         }

         // get the dependents if any
         nl = elem.getElementsByTagName(XML_ELEM_DEPENDENTS);
         Element dependentsEl = (Element)nl.item(0);
         if (dependentsEl != null)
         {
            value = dependentsEl.getAttribute(XML_ATTR_OBJECT_TYPE);
            if(value != null && value.trim().length() > 0)
               m_dependentobjecttype = Integer.parseInt(value);
            else
               m_dependentobjecttype = -1;
            
            // get the dependent locators if any
            NodeList dp = dependentsEl.getElementsByTagName(XML_ELEM_DEPENDENT);
            int length = dp.getLength();
            if(length > 0)
            {
               resetDependents();
               for (int i = 0; i < length; i++)
               {
                  Element dependent = (Element) dp.item(i);
                  nl = dependent.getElementsByTagName(PSLocator.XML_NODE_NAME);
                  locator = (Element) nl.item(0);
                  if (locator != null)
                     m_dependents.add(new PSLocator(locator));
               }
            }
            else
            {
               m_dependents = null;
            }
            
            // get the dependent content type ids if any
            NodeList idList = dependentsEl
                  .getElementsByTagName(XML_ELEM_CONTENT_TYPE_ID);
            length = idList.getLength();
            if(length > 0)
            {
               resetDependentContentTypeIds();
               String sID;
               for (int i = 0; i < length; i++)
               {
                  sID = PSXMLDomUtil.getElementData(idList.item(i));
                  m_dependentContentTypeIds.add(new Long(sID));
               }
            }
            else
            {
               m_dependentContentTypeIds = null;
            }
            
         }
         else
         {
            m_dependents = null;
            m_dependentContentTypeIds = null;
         }
         
         // get the properties if any
         nl = elem.getElementsByTagName(XML_ELEM_PROPERTIES);
         Element properties = null;
         if((properties = (Element)nl.item(0)) != null)
         {
            Element property = null;
            nl = properties.getElementsByTagName(XML_ELEM_PROPERTY);
            int i = 0;
            m_properties.clear();
            while((property = (Element)nl.item(i++)) != null)
            {
               m_properties.put(
                  property.getAttribute(XML_ATTR_PROP_NAME),
                  property.getAttribute(XML_ATTR_PROP_VALUE));
            }
         }
         
         // OPTIONAL relationship names
         nl = elem.getElementsByTagName(XML_ELEM_REL_NAME_SET);
         Element namesEl;
         if((namesEl = (Element)nl.item(0)) != null)
         {
            Element nameEl = null;
            nl = namesEl.getElementsByTagName(XML_ELEM_REL_NAME);
            int i = 0;
            m_names.clear();
            String name;
            while((nameEl = (Element)nl.item(i++)) != null)
            {
               name = nameEl.getAttribute(XML_ATTR_NAME);
               if (name != null && name.trim().length() > 0)
               m_names.add(name);
            }
         }
         

      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Reset the dependents list. This method must be called before modifying
    * the dependents list.
    */
   private void resetDependents()
   {
      if (m_dependents == null)
         m_dependents = new ArrayList<>();
      else
         m_dependents.clear();
   }

   /**
    * Reset the dependent content type id list. This method must be called 
    * before modifying the dependent content type id list.
    */
   private void resetDependentContentTypeIds()
   {
      if (m_dependentContentTypeIds == null)
         m_dependentContentTypeIds = new ArrayList<>();
      else
         m_dependentContentTypeIds.clear();
   }

   /**
    * Serializes this object into XML form as specified in the following
    * DTD:
    * <code>
    * <pre>
    * &lt;!ELEMENT PSXRelationshipFilter (Owner?, Dependents?, Properties?, TypeSet?)&gt;
    * &lt;!ATTLIST PSXRelationshipFilter
    * &nbsp;&nbsp;&nbsp; name CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; type CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; category CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; rid CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; communityFiltering CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT Owner (PSXKey?)&gt;
    * &lt;!ATTLIST Owner
    * &nbsp;&nbsp;&nbsp; contenttypeid CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; objecttypeid CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT Dependents (Dependent*, ContentTypeId*)&gt;
    * &lt;!ELEMENT Dependent (PSXKey?)&gt;
    * &lt;!ATTLIST Dependent
    * &nbsp;&nbsp;&nbsp; contenttypeid CDATA #IMPLIED
    * &nbsp;&nbsp;&nbsp; objecttypeid CDATA #IMPLIED
    * &gt;
    * &lt;!ELEMENT ContentTypeId (#PCDATA)&gt;
    * &lt;!ELEMENT Properties (Property*)&gt;
    * &lt;!ELEMENT Property EMPTY&gt;
    * &lt;!ATTLIST Property
    * &nbsp;&nbsp;&nbsp; name CDATA #REQUIRED
    * &nbsp;&nbsp;&nbsp; value CDATA #REQUIRED
    * &gt;
    * &lt;!ELEMENT RelationshipNameSet (RelationshipName*)&gt;
    * &lt;!ELEMENT RelationshipName EMPTY&gt;
    * &lt;!ATTLIST RelationshipName
    * &nbsp;&nbsp;&nbsp; name CDATA #REQUIRED
    * &gt;
    * </pre>
    * </code>
    *
    * @param doc the XML document to be appended to, cannot
    * be <code>null</code>
    *
    * @return the XML element that represents a <code>
    * PSRelationshipFilter</code>. Never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("Document cannot be null.");
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_RID, Integer.toString(m_rid));
      if(m_type != null)
         root.setAttribute(XML_ATTR_TYPE, m_type);
      if(m_category != null)
         root.setAttribute(XML_ATTR_CATEGORY, m_category);
      root.setAttribute(XML_ATTR_COMMUNITY_FILTERING,
         Boolean.valueOf(m_filterByCommunity).toString());

      Element owner = doc.createElement(XML_ELEM_OWNER);
      owner.setAttribute(XML_ATTR_CONTENT_TYPE_ID,
         Long.toString(m_ownercontenttypeid));
      owner.setAttribute(XML_ATTR_OBJECT_TYPE,
         Integer.toString(m_ownerobjecttype));
      if(m_owner != null)
         owner.appendChild(m_owner.toXml(doc));
      root.appendChild(owner);

      if (m_dependents != null || m_dependentContentTypeIds != null)
      {
         Element dependentsEl = doc.createElement(XML_ELEM_DEPENDENTS);
         dependentsEl.setAttribute(XML_ATTR_OBJECT_TYPE,
               Integer.toString(m_dependentobjecttype));
         
         if (m_dependents != null)
         {
            for (PSLocator dep : m_dependents)
            {
               Element depEl = doc.createElement(XML_ELEM_DEPENDENT);
               depEl.appendChild(dep.toXml(doc));
               dependentsEl.appendChild(depEl);
            }
         }
         if (m_dependentContentTypeIds != null)
         {
            for (Long typeId : m_dependentContentTypeIds)
            {
               PSXmlDocumentBuilder.addElement(doc, dependentsEl,
                     XML_ELEM_CONTENT_TYPE_ID, typeId.toString());
            }
         }
         
         root.appendChild(dependentsEl);
      }

      if(!m_properties.isEmpty())
      {
         Element property = null;
         Element properties = doc.createElement(XML_ELEM_PROPERTIES);
         Iterator keys = m_properties.keySet().iterator();
         while(keys.hasNext())
         {
            String key = (String)keys.next();
            String val = m_properties.get(key);
            property = doc.createElement(XML_ELEM_PROPERTY);
            property.setAttribute(XML_ATTR_PROP_NAME, key);
            property.setAttribute(XML_ATTR_PROP_VALUE, val);
            properties.appendChild(property);
         }
         root.appendChild(properties);
      }

      if(! m_names.isEmpty())
      {
         
         Element nameSetElem = doc.createElement(XML_ELEM_REL_NAME_SET);
         Element nameElem;
         for (String name : m_names)
         {
            nameElem = doc.createElement(XML_ELEM_REL_NAME);
            nameElem.setAttribute(XML_ATTR_NAME, name);
            nameSetElem.appendChild(nameElem);
         }
         root.appendChild(nameSetElem);
      }

      return root;

   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSRelationshipFilter))
         return false;

      PSRelationshipFilter other = (PSRelationshipFilter) obj;

      return new EqualsBuilder()
         .append(m_category, other.m_category)
         .append(m_dependentContentTypeIds, other.m_dependentContentTypeIds)
         .append(m_dependentobjecttype, other.m_dependentobjecttype)
         .append(m_dependents, other.m_dependents)
         .append(m_filterByCommunity, other.m_filterByCommunity)
         .append(m_limitToEditOrCurrentOwnerRevision, other.m_limitToEditOrCurrentOwnerRevision)
         .append(m_limitToOwnerRevision, other.m_limitToOwnerRevision)
         .append(m_limitToPublicOwnerRevision, other.m_limitToPublicOwnerRevision)
         .append(m_limitToCrossSiteLinks, other.m_limitToCrossSiteLinks)
         .append(m_names, other.m_names)
         .append(m_owner, other.m_owner)
         .append(m_ownercontenttypeid, other.m_ownercontenttypeid)
         .append(m_ownerobjecttype, other.m_ownerobjecttype)
         .append(m_rid, other.m_rid)
         .append(m_type, other.m_type)
         .isEquals();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder(13, 3).appendSuper(super.hashCode())
            .append(m_category)
            .append(m_dependentContentTypeIds)
            .append(m_dependentobjecttype)
            .append(m_dependents)
            .append(m_filterByCommunity)
            .append(m_limitToEditOrCurrentOwnerRevision)
            .append(m_limitToOwnerRevision)
            .append(m_limitToPublicOwnerRevision)
            .append(m_limitToCrossSiteLinks)
            .append(m_names)
            .append(m_owner)
            .append(m_ownercontenttypeid)
            .append(m_ownerobjecttype)
            .append(m_rid)
            .append(m_type)
            .toHashCode();
   }

   /**
    * A list of relationship names to be filtered by. It may be empty, but
    * never <code>null</code>. Filter by names is off by default.
    */
   private Set<String> m_names = new HashSet<>();
      
   /**
    * The relationship type filter, turned off by default. Use
    * {@link #setType(String)} to set a filter.
    */
   private String m_type = null;

   /**
    * The relationship category filter, turned off by default. Use
    * {@link #setCategory(String)} to set a filter.
    */
   private String m_category = null;

   /**
    * The relationship properties filter, turned off by default. Use
    * {@link #setProperty(String, String)} to set a filter.
    */
   private Map<String, String> m_properties = new HashMap<>();

   /**
    * The relationship owner filter, turned off by default. Use
    * {@link #setOwner(PSLocator)} to set a filter.
    */
   private PSLocator m_owner = null;

   /**
    * The relationship dependent filter, turned off by default. Use
    * {@link #setDependent(PSLocator)} to set a filter.
    */
   private List<PSLocator> m_dependents = null;

   /**
    * The relationship id filter, turned off by default. Use
    * {@link #setRelationshipId(int)} to set a filter.
    */
   private int m_rid = -1;

   /**
    * The relationship community filter, turned on by default. Use
    * {@link #setCommunityFiltering(boolean)} to set enable or disable the
    * filter.
    */
   private boolean m_filterByCommunity = true;

   /**
    * The owner contenttype id filter, turned off by default.
    */
   private long m_ownercontenttypeid = -1;

   /**
    * The dependent contenttype id(s) filter, turned off by default. Use
    * {@link #setDependentContentTypeIds(Collection)} to set a filter.
    */
   private List<Long> m_dependentContentTypeIds = null;

   /**
    * The owner objecttype filter, turned off by default. Use
    * {@link #setOwnerObjectType(int)} to set a filter.
    */
   private int m_ownerobjecttype = -1;

   /**
    * The dependent objecttype filter, turned off by default. Use
    * {@link #setDependentObjectType(int)} to set a filter.
    */
   private int m_dependentobjecttype = -1;

   /**
    * Indicates whether the retrieved relationships are limited to the Edit 
    * Revision (if it exists) or the Current Revision (if the Edit Revision not 
    * exists) of the owner. Defaults to <code>false</code>.
    */
   private boolean m_limitToEditOrCurrentOwnerRevision = false;
   
   /**
    * Indicates whether the retrieved relationships are limited to the Tip
    * Revision 
    *  Defaults to <code>false</code>.
    */
   private boolean m_limitToTipOwnerRevision = false;
   
   /**
    * Indicates whether it is filtered by the public revision of the owner.
    * <code>true</code> if it is. Default to <code>false</code>.
    */
   private boolean m_limitToPublicOwnerRevision = false;
   
   /**
    * The owner revision filter, turned off by default.
    * The relationships will be filtered by the owner revision if it is 
    * <code>true</code>. The owner revision is provided via {@link #m_owner}.
    */
   private boolean m_limitToOwnerRevision = false;
      
   /**
    * Constant to filter by category 'Active Assembly'.
    */
   static public final String FILTER_CATEGORY_ACTIVE_ASSEMBLY =
      PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY;

   /**
    * Constant to filter by category 'Folder Content'.
    */
   static public final String FILTER_CATEGORY_FOLDER =
      PSRelationshipConfig.CATEGORY_FOLDER;

   /**
    * Constant to filter by category 'Promotable Version'.
    */
   static public final String FILTER_CATEGORY_PROMOTABLE =
      PSRelationshipConfig.CATEGORY_PROMOTABLE;

   /**
    * Constant to filter by category 'Translation'.
    */
   static public final String FILTER_CATEGORY_TRANSLATION =
      PSRelationshipConfig.CATEGORY_TRANSLATION;

   /**
    * Constant to filter by category 'New Copy'.
    */
   static public final String FILTER_CATEGORY_COPY =
      PSRelationshipConfig.CATEGORY_COPY;

   /**
    * Constant to filter by category 'Generic'.
    */
   static public final String FILTER_CATEGORY_GENERIC =
      PSRelationshipConfig.CATEGORY_GENERIC;

   /**
    * Constant to filter by category 'Widget'.
    */
   static public final String FILTER_CATEGORY_WIDGET =
      PSRelationshipConfig.CATEGORY_WIDGET;

    /**
     * Constant to filter by category 'Recycled'.
     */
    static public final String FILTER_CATEGORY_RECYCLED =
            PSRelationshipConfig.CATEGORY_RECYCLED;

   /**
    * An array of all known relationship category filters.
    */
   public static final String[] FILTERS_BY_CATEGORY =
   {
      FILTER_CATEGORY_ACTIVE_ASSEMBLY,
      FILTER_CATEGORY_FOLDER,
      FILTER_CATEGORY_GENERIC,
      FILTER_CATEGORY_WIDGET,
      FILTER_CATEGORY_PROMOTABLE,
      FILTER_CATEGORY_TRANSLATION,
      FILTER_CATEGORY_COPY,
      FILTER_CATEGORY_RECYCLED
   };

   /**
    * Constant to filter by relationship type 'Active Assembly'.
    */
   static public final String FILTER_NAME_ACTIVE_ASSEMBLY =
      PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

   /**
    * Constant to filter by relationship type 'Active Assembly - Mandatory'.
    */
   static public final String FILTER_NAME_ACTIVE_ASSEMBLY_MANDATORY =
      PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY;

   /**
    * Constant to filter by relationship type 'New Copy'.
    */
   static public final String FILTER_NAME_NEW_COPY =
      PSRelationshipConfig.TYPE_NEW_COPY;

   /**
    * Constant to filter by relationship type 'Promotable Version'.
    */
   static public final String FILTER_NAME_PROMOTABLE_VERSION =
      PSRelationshipConfig.TYPE_PROMOTABLE_VERSION;

   /**
    * Constant to filter by relationship type 'Folder Content'.
    */
   static public final String FILTER_NAME_FOLDER_CONTENT =
      PSRelationshipConfig.TYPE_FOLDER_CONTENT;

   /**
    * Constant to filter by relationship type 'Translation'.
    */
   static public final String FILTER_NAME_TRANSLATION =
      PSRelationshipConfig.TYPE_TRANSLATION;

   /**
    * Constant to filter by relationship type 'Translation - Mandatory'.
    */
   static public final String FILTER_NAME_TRANSLATION_MANDATORY =
      PSRelationshipConfig.TYPE_TRANSLATION_MANDATORY;

   /**
    * Constant to filter by relationship type 'Widget-Assembly'.
    */
   static public final String FILTER_NAME_WIDGET_ASSEMBLY =
      PSRelationshipConfig.TYPE_WIDGET_ASSEMBLY;
   
   /**
    * Constant to filter by relationship type 'Widget-Content'.
    */
   static public final String FILTER_NAME_WIDGET_CONTENT =
      PSRelationshipConfig.TYPE_WIDGET_CONTENT;
   
   /**
    * Constant to filter by relationship type 'LocalContent'.
    */
   static public final String FILTER_NAME_LOCAL_CONTENT =
      PSRelationshipConfig.TYPE_LOCAL_CONTENT;
   
   /**
    * An array of all known system relationship name filters.
    */
   public static final String[] FILTERS_BY_NAME =
   {
      FILTER_NAME_ACTIVE_ASSEMBLY,
      FILTER_NAME_ACTIVE_ASSEMBLY_MANDATORY,
      FILTER_NAME_NEW_COPY,
      FILTER_NAME_WIDGET_ASSEMBLY,
      FILTER_NAME_WIDGET_CONTENT,
      FILTER_NAME_LOCAL_CONTENT,
      FILTER_NAME_PROMOTABLE_VERSION,
      FILTER_NAME_FOLDER_CONTENT,
      FILTER_NAME_TRANSLATION,
      FILTER_NAME_TRANSLATION_MANDATORY
   };

   /**
    * Constant to filter out system relationships.
    */
   static public final String FILTER_TYPE_SYSTEM =
      "system"; //PSRelationshipConfig.TYPE_ENUM[0];

   /**
    * Constant to filter out user relationships.
    */
   static public final String FILTER_TYPE_USER =
      "user"; //PSRelationshipConfig.TYPE_ENUM[1];

   /**
    * An array of all known system relationship type filters.
    */
   public static final String[] FILTERS_BY_TYPE =
   {
      FILTER_TYPE_SYSTEM,
      FILTER_TYPE_USER
   };

   
   
   @Override
   public String toString()
   {
      return "PSRelationshipFilter [m_limitToCrossSiteLinks=" + m_limitToCrossSiteLinks + ", m_names=" + m_names
            + ", m_type=" + m_type + ", m_category=" + m_category + ", m_properties=" + m_properties + ", m_owner="
            + m_owner + ", m_dependents=" + m_dependents + ", m_rid=" + m_rid + ", m_filterByCommunity="
            + m_filterByCommunity + ", m_ownercontenttypeid=" + m_ownercontenttypeid + ", m_dependentContentTypeIds="
            + m_dependentContentTypeIds + ", m_ownerobjecttype=" + m_ownerobjecttype + ", m_dependentobjecttype="
            + m_dependentobjecttype + ", m_limitToEditOrCurrentOwnerRevision=" + m_limitToEditOrCurrentOwnerRevision
            + ", m_limitToTipOwnerRevision=" + m_limitToTipOwnerRevision + ", m_limitToPublicOwnerRevision="
            + m_limitToPublicOwnerRevision + ", m_limitToOwnerRevision=" + m_limitToOwnerRevision + "]";
   }



   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationshipFilter";

   /** XML Attributes used to serialize this object **/
   protected static final String XML_ATTR_TYPE = "type";
   protected static final String XML_ATTR_CATEGORY = "category";
   protected static final String XML_ATTR_NAME = "name";
   protected static final String XML_ATTR_PROP_NAME = "name";
   protected static final String XML_ATTR_PROP_VALUE = "value";
   protected static final String XML_ATTR_RID = "rid";
   protected static final String XML_ATTR_COMMUNITY_FILTERING =
      "communityFiltering";
   protected static final String XML_ATTR_CONTENT_TYPE_ID = "contenttypeid";
   protected static final String XML_ATTR_OBJECT_TYPE = "objecttype";

   /** XML Elements used to serialize this object **/
   protected static final String XML_ELEM_PROPERTIES = "Properties";
   protected static final String XML_ELEM_PROPERTY = "Property";
   protected static final String XML_ELEM_OWNER = "Owner";
   protected static final String XML_ELEM_DEPENDENTS = "Dependents";
   protected static final String XML_ELEM_DEPENDENT = "Dependent";
   protected static final String XML_ELEM_CONTENT_TYPE_ID = "ContentTypeId";
   protected static final String XML_ELEM_REL_NAME_SET = "RelationshipNameSet";
   protected static final String XML_ELEM_REL_NAME = "RelationshipName";

}
