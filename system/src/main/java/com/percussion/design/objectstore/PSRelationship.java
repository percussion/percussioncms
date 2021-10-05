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
package com.percussion.design.objectstore;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Implements the PSXRelationship element defined in PSXRelationshipSet.dtd.
 */
public class PSRelationship extends PSComponent
{
   /**
    * Generated serial number.
    */
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new relationship object for the supplied parameters.
    *
    * @param id the relationship id. This is used as update key to the
    *    {@link IPSConstants#PSX_RELATIONSHIPS} table.
    * @param owner the owner locator, not <code>null</code>.
    * @param dependent the dependent locator, not <code>null</code>.
    * @param config the relationship configuration, not <code>null</code>.
    */
   public PSRelationship(int id, PSLocator owner, PSLocator dependent,
      PSRelationshipConfig config)
   {
      if (owner == null || dependent == null || config == null)
        throw new IllegalArgumentException("paramters cannot be null");

      setId(id);
      m_owner = owner;
      m_dependent = dependent;
      m_config = config;

      initUserProperties();
   }

   /**
    * Constructs an identical object from the specified object.
    * 
    * @param source the specified source object, may not be <code>null</code>.
    */
   protected PSRelationship(PSRelationship source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");
      
      m_id = source.m_id;
      m_isPersisted = source.m_isPersisted;
      m_owner = source.m_owner;
      m_dependent = source.m_dependent;
      m_config = source.m_config;
      m_dependentCommunityId = source.m_dependentCommunityId;
      m_dependentObjectType = source.m_dependentObjectType;
      m_userProperties = source.m_userProperties;
   }
   
   /**
    * Creates a new relationship object for the supplied parameters that allows
    * derived classes to defer assignment of relationship configuration.
    * Dervived classes must call {@link #setConfig} immediately after
    * construction.
    * 
    * @param id the relationship id. This is used as update key to the
    *           {@link IPSConstants#PSX_RELATIONSHIPS} table.
    * @param owner the owner (parent) of the relationship, not <code>null</code>.
    * @param dependent the dependent (child) of the relationship, not
    *           <code>null</code>.
    */
   protected PSRelationship(int id, PSLocator owner, PSLocator dependent)
   {
      if (owner == null || dependent == null)
         throw new IllegalArgumentException(
               "neither owner nor dependent may be null");

      setId(id);
      m_owner = owner;
      m_dependent = dependent;
   }
   
   /**
    * Sets the configuration used for this relationship and initializes the user
    * properties. This method only may be used after calling the constructor
    * that allows deferred config assignment. This method may not be used to
    * change an already assigned config.
    * 
    * @param config the relationship configuration, not <code>null</code>.
    * @throws IllegalStateException if this relationship has already been
    *            assigned a config.
    */
   public void setConfig(PSRelationshipConfig config)
   {
      if (config == null)
         throw new IllegalArgumentException(
               "relationship config may not be null");
      m_config = config;
      initUserProperties();
   }
   
   /**
    * Initialize the user properties according to the supplied relationship
    * configuration object and current relationshiip id.
    * Note, both {@link #m_id} and {@link #m_config} have to be set.
    */
   @SuppressWarnings("unchecked")
   private void initUserProperties()
   {
      if (m_config.getUserProperties().isEmpty())
      {
         m_userProperties = Collections.EMPTY_LIST;
      }
      else
      {
         Set<Map.Entry<String, String>> entries = m_config.getUserProperties()
               .entrySet();
         m_userProperties = new ArrayList<>(entries
               .size());
         for (Map.Entry<String, String> entry : entries)
         {
            PSRelationshipPropertyData prop = new PSRelationshipPropertyData(
                  entry.getKey(), entry.getValue());
            m_userProperties.add(prop);
         }
      }
   }
   
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationship(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      this(sourceNode, parentDoc, parentComponents, null);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    *    
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationship(Element sourceNode) throws PSUnknownNodeTypeException
      {
         this(sourceNode, null, null, null);
      }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents the parent objects of this object, may be
    *    <code>null</code>.
    * @param config the relationship configuration, might be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationship(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents, PSRelationshipConfig config)
         throws PSUnknownNodeTypeException
   {
      m_config = config;
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Creates a new relationship object from the supplied source.
    * @param id relationship id that overrides source id.
    * @param source object to make a copy from,
    * never <code>null</code>.
    */
   public PSRelationship(int id, PSRelationship source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      copyFrom(source);
      
      setId(id);
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid <code>PSRelationship</code>, not <code>null</code>.
    */
   public void copyFrom(PSRelationship c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_config = c.getConfig();
      m_dependent = c.getDependent();
      m_dependentCommunityId = c.m_dependentCommunityId;
      m_dependentObjectType = c.m_dependentObjectType;
      m_isPersisted = c.m_isPersisted;
      m_owner = c.getOwner();
      m_userProperties = c.m_userProperties;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to, may be <code>null</code>.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSRelationship))
         return false;

      PSRelationship t = (PSRelationship) o;

      return new EqualsBuilder()
         .append(m_id, t.m_id)
         .append(m_config, t.m_config)
         .append(m_isPersisted, t.m_isPersisted)
         .append(m_owner, t.m_owner)
         .append(m_dependent, t.m_dependent)
         .append(m_dependentCommunityId, t.m_dependentCommunityId)
         .append(m_dependentObjectType, t.m_dependentObjectType)
         .append(m_userProperties, t.m_userProperties)
         .isEquals();
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_config) 
            .append(m_dependent)
            .append(m_dependentCommunityId) 
            .append(m_dependentObjectType)
            .append(m_owner)
            .append(m_userProperties)
            .append(m_id)
            .append(m_isPersisted)
            .toHashCode(); 
   }

   /**
    * Makes a deep copy of this object.
    * Note, the relationship config object is not cloned since this object
    * does not own the config object. 
    *
    * @return a deep copy of this object, never <code>null</code>.
    */
   @Override
   public Object clone()
   {
      PSRelationship relationship = (PSRelationship) super.clone();
      relationship.m_config = m_config;
      relationship.m_isPersisted = m_isPersisted;
      relationship.m_dependent = (PSLocator) m_dependent.clone();
      relationship.m_dependentCommunityId = m_dependentCommunityId;
      relationship.m_dependentObjectType = m_dependentObjectType;
      relationship.m_owner = (PSLocator) m_owner.clone();
      relationship.m_userProperties = new ArrayList<>(
            m_userProperties);

      return relationship;
   }

   /**
    * @return the GUID of this relationship object, never <code>null</code>.
    */
   public IPSGuid getGuid()
   {
      return new PSGuid(PSTypeEnum.RELATIONSHIP, getId());
   }
   
   /**
    * Sets a new GUID to this object. This can only be called when this object
    * does not have a valid GUID. 
    * 
    * @param newguid the new GUID, never <code>null</code>, must be a valid
    *   GUID, where its type is {@link PSTypeEnum#RELATIONSHIP} and its value
    *   is greater than zero.
    */
   public void setGUID(IPSGuid newguid)
   {
      if (newguid == null)
      {
         throw new IllegalArgumentException("newguid may not be null");
      }
      if (PSTypeEnum.valueOf(newguid.getType()) != PSTypeEnum.RELATIONSHIP)
      {
         throw new IllegalArgumentException(
               "The type of newguid must be PSTypeEnum.RELATIONSHIP");
      }
      if (newguid.longValue() < 0L)
      {
         throw new IllegalArgumentException(
               "The value of newguid must be > 0");
      }
      if (getId() > 0)
      {
         throw new IllegalStateException("Cannot change existing guid");
      }
      setId((int)newguid.longValue());
   }
   
   /**
    * Get the relationship owner's locator.
    *
    * @return the owner's locator, never <code>null</code>.
    */
   public PSLocator getOwner()
   {
      return m_owner;
   }

   /**
    * Get the relationship dependent's locator.
    *
    * @return the dependent's locator, never <code>null</code>.
    */
   public PSLocator getDependent()
   {
      return m_dependent;
   }

   /**
    * Get the relationship dependent's community id.
    *
    * @return the dependent's community id (-1 all communities).
    */
   public int getDependentCommunityId()
   {
      return m_dependentCommunityId;
   }

   /**
    * Set the relationship dependent's community id.
    */
   public void setDependentCommunityId(int dependentCommunityId)
   {
      m_dependentCommunityId = dependentCommunityId;
   }


   /**
    * Get the relationship dependent's object type.
    *
    * @return the dependent's object type.
    */
   public int getDependentObjectType()
   {
      return m_dependentObjectType;
   }

   /**
    * Set the relationship owner's locator.
    *
    * @param owner The owner's locator, never <code>null</code>.
    */
   public void setOwner(PSLocator owner)
   {
      if (owner == null)
         throw new IllegalArgumentException("owner may not be null");

      m_owner = owner;
   }

   /**
    * Set the relationship dependent's locator.
    *
    * @param dependent The dependent's locator, never <code>null</code>.
    */
   public void setDependent(PSLocator dependent)
   {
      if (dependent == null)
         throw new IllegalArgumentException("dependent may not be null");

      m_dependent = dependent;
   }

   /**
    * Set the relationship dependent's object type.
    */
   public void setDependentObjectType(int dependentObjectType)
   {
      m_dependentObjectType = dependentObjectType;
   }

   /**
    * Get the requested property. First tries to get the requested property
    * from the system property of the relationship configuration, if that fails 
    * it tries the user properties.
    *
    * @param name the property name to get the property for, not
    *    <code>null</code> or empty.
    *    
    * @return the requested property, might be <code>null</code> if no
    *    property for the supplied name exists.
    */
   public String getProperty(String name)
   {
      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null");

      String value = getConfig().getSystemProperty(name);
      if (value == null || value.trim().length() == 0)
      {
         PSRelationshipPropertyData prop = findUserProperty(name);
         if (prop != null)
            value = prop.getValue();
      }

      return value;
   }
   
   /**
    * Find a user property with the supplied name.
    * 
    * @param name the name of the searched user property, assumed not 
    *   <code>null</code>.
    *   
    * @return the user property, it may be <code>null</code> if cannot find.
    */
   private PSRelationshipPropertyData findUserProperty(String name)
   {
      for (PSRelationshipPropertyData prop : m_userProperties)
      {
         if (prop.getName().equalsIgnoreCase(name))
            return prop;
      }
      return null;
   }

   /**
    * Set the supplied value for the provided user property name.
    *
    * @param name the property name to set the property for, not
    *    <code>null</code> or empty. It must be one of the defined user 
    *    property name and cannot be one of the system property name.
    * @param value the new value to set, may be <code>null</code> or empty.
    */
   public void setProperty(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
        throw new IllegalArgumentException("name cannot be null");

      if (m_config.getSysProperty(name) != null)
         throw new IllegalArgumentException(
               "Cannot set the value for system property '" + name + "'");
         
      PSRelationshipPropertyData prop = findUserProperty(name);
      if (prop == null)
         throw new IllegalArgumentException("Unknown user property name: '"
               + name + "'");
      
      prop.setValue(value);
   }

   /**
    * Is the revision used for the owner locator?
    *
    * @return <code>true</code> if used, <code>false</code> otherwise.
    * 
    * @deprecated use PSRelationshipConfig#useOwnerRevision() instead.
    */
   public boolean useOwnerRevision()
   {
      return getConfig().useOwnerRevision();
   }

   /**
    * Is the revision used for the dependents locator?
    *
    * @return <code>true</code> if used, <code>false</code> otherwise.
    * 
    * @deprecated use PSRelationshipConfig#useDependentRevision() instead.
    */
   public boolean useDependentRevision()
   {
      return getConfig().useDependentRevision();
   }

   /**
    * Is cloning allowed for this relationship type?
    *
    * @return <code>true</code> if cloning is allowed, <code>false</code>
    *    otherwise.
    * 
    * @deprecated use PSRelationshipConfig#isCloningAllowed() instead.
    */
   public boolean isCloningAllowed()
   {
      return getConfig().isCloningAllowed();
   }

   /**
    * Is the promotable system effect enabled?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    * 
    * @deprecated use PSRelationshipConfig#isPromotable() instead.
    */
   public boolean isPromotable()
   {
      return getConfig().isPromotable();
   }

   /**
    * Is this relationship to be skipped when the item is promoted?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    * 
    * @deprecated use PSRelationshipConfig#isSkipPromotion() instead.
    */
   public boolean isSkipPromotion()
   {
      return getConfig().isSkipPromotion();
   }

   /**
    * Is this an inline relationship?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isInlineRelationship()
   {
      PSRelationshipPropertyData prop = findUserProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP);
      if (prop != null)
      {
         String value = prop.getValue();
         return value != null && value.trim().length() > 0;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Test if this relationship instance is of category active assembly.
    * 
    * @return <code>true</code> if this is an active assembly relationship,
    *    <code>false</code> otherwise.
    *    
    * @deprecated use PSRelationshipConfig#isActiveAssemblyRelationship() instead.
    */
   public boolean isActiveAssemblyRelationship()
   {
      return getConfig().isActiveAssemblyRelationship();
   }

   /**
    * Should the server id or the current request user be used to execute
    * effects?
    *
    * @return <code>true</code> to use the server id, <code>false</code>
    *    otherwise.
    * 
    * @deprecated use PSRelationshipConfig#useServerId() instead.
    */
   public boolean useServerId()
   {
      return getConfig().useServerId();
   }

   /**
    * Get both system and user relationship properties. System properties are
    * always all returned. Only non-<code>null</code>, non-empty user properties
    * will be returned.
    *
    * @return the relationship instance properties, never
    *    <code>null</code>, might be empty. The returned map values are never
    *    <code>null</code> or empty.
    */
   public Map<String, String> getProperties()
   {
      if (m_userProperties.isEmpty())
      {
         return getConfig().getSystemProperties();
      }
      else
      {
         Map<String, String> props = new HashMap<>();
         props.putAll(getConfig().getSystemProperties());
         props.putAll(getUserProperties());
         return props;
      }
   }
   
   /**
    * Get a map with all system and user properties defined for this 
    * relationship. All defined properties will be returned no matter what 
    * their values are.
    * 
    * @return a map with all system and user properties, never 
    *    <code>null</code>, may be empty. The returned map values may be
    *    <code>null</code> or empty.
    */
   public Map<String, String> getAllProperties()
   {
      Map<String, String> props = new HashMap<>();
      props.putAll(getConfig().getSystemProperties());
      if (!m_userProperties.isEmpty())
      {
         for (PSRelationshipPropertyData prop : m_userProperties)
            props.put(prop.getName(), prop.getValue());
      }
      
      return props;
   }
   
   /**
    * Gets the folder id of the relationship used for Cross Site Linking.
    * @return maybe null.
    */
   public Integer getLegacyFolderId() {
      String folderid = this.getProperty(PSRelationshipConfig.PDU_FOLDERID);
      if (isNotBlank(folderid)) {
         return Integer.parseInt(folderid);
      }
      return null;
   }
   
   /**
    * Gets the folder id of the relationship used for Cross Site Linking.
    * @return maybe null.
    */
   public void setLegacyFolderId(Integer folderId) {
      if (folderId == null)
         this.setProperty(PSRelationshipConfig.PDU_FOLDERID, null);
      else
         this.setProperty(PSRelationshipConfig.PDU_FOLDERID, "" + folderId);
   }   
   
   /**
    * Gets the site id of the relationship used for Cross Site Linking.
    * @return maybe null.
    */
   public Integer getLegacySiteId() {
      String siteId = this.getProperty(PSRelationshipConfig.PDU_SITEID);
      if (isNotBlank(siteId)) {
         return Integer.parseInt(siteId);
      }
      return null;
   }
   
   /**
    * Gets the folder id of the relationship used for Cross Site Linking.
    * @return maybe null.
    */
   public void setLegacySiteId(Integer siteId) {
      if (siteId == null)
         this.setProperty(PSRelationshipConfig.PDU_SITEID, null);
      else
         this.setProperty(PSRelationshipConfig.PDU_SITEID, "" + siteId);
   }   
   

   /**
    * Get all non-null and non-empty user properties of this relationship.
    * 
    * @return the user properties, never <code>null</code>, but may be empty.
    */
   public Map<String, String> getUserProperties()
   {
      Map<String, String> userProps = new HashMap<>();
      String value;
      for (PSRelationshipPropertyData prop : m_userProperties)
      {
         value = prop.getValue();
         if (value != null && value.trim().length() > 0)
            userProps.put(prop.getName(), value);
      }
      
      return userProps;
   }

   /**
    * Gets a user property by name.
    * 
    * @param propertyName the name of the user property. It may be 
    *   <code>null</code> or empty.
    * 
    * @return the user property. It may be <code>null</code> if cannot find
    *   such user property.
    */
   public PSRelationshipPropertyData getUserProperty(String propertyName)
   {
      for (PSRelationshipPropertyData prop : m_userProperties)
      {
         if (prop.getName().equalsIgnoreCase(propertyName))
            return prop;
      }
      
      return null;
   }

   /**
    * Set a supplied user property. This property will be added if cannot find
    * a user property with such name.
    * 
    * @param srcProperty the source user property. Never <code>null</code>.
    */
   public void setUserProperty(PSRelationshipPropertyData srcProperty)
   {
      if (srcProperty == null)
         throw new IllegalArgumentException("srcProperty may not be null.");
         
      PSRelationshipPropertyData tgtProperty = getUserProperty(srcProperty.getName());
      if (tgtProperty == null)
      {
         m_userProperties.add(srcProperty);
      }
      else
      {
         tgtProperty.setPersisted(srcProperty.isPersisted());
         tgtProperty.setValue(srcProperty.getValue());
      }
   }
   
   /**
    * Gets all user properties. This method cannot be overridden by derived
    * classes.
    * 
    * @return a list of user properties, never <code>null</code>, but may be
    *   empty.
    */
   public final List<PSRelationshipPropertyData> getAllUserProperties()
   {
      return m_userProperties;
   }
   
   /**
    * Get the relationships configuration.
    *
    * @return the relationships configuration, never <code>null</code>.
    */
   public PSRelationshipConfig getConfig()
   {
      return m_config;
   }

   /**
    * Returns the description for this relationship
    *
    * @return the relationship description, may be <code>null</code> or empty
    */
   public String getDescription()
   {
      return getConfig().getDescription();
   }

   /**
    * Sets the description for this relationship
    * @param desc the description, may be <code>null</code> or empty
    */
   public void setDescription(String desc)
   {
      getConfig().setDescription(desc);
   }

   /**
    * Method to reset the relationship id so that when saved will create a new 
    * relationship instead of modifying the existing one. This will be useful 
    * for example, when you have an existing relationship and want to create a 
    * new one based on this with some properties/attributes of the relationship 
    * changed.
    *
    */
   public void resetId()
   {
      m_id = -1;
      m_isPersisted = false;
   }
   
   /** @see IPSComponent */
   public void fromXml(Element sourceNode, 
      @SuppressWarnings("unused") IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: id attribute
         data = tree.getElementData(ATTR_ID);
         if (data == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ATTR_ID,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         try
         {
            setId(Integer.parseInt(data));
         }
         catch (NumberFormatException e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ATTR_ID,
               data
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // REQUIRED: config attribute
         data = tree.getElementData(ATTR_CONFIG);
         if (data == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ATTR_CONFIG,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         if (m_config == null)
         {
            PSRelationshipConfigSet configs =
               PSRelationshipCommandHandler.getConfigurationSet();
            m_config = configs.getConfig(data);
         }
         if (m_config == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ATTR_CONFIG,
               data
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         // OPTIONAL: isPersisted attribute
         data = tree.getElementData(ATTR_ISPERSISTED);
         if (data != null && data.equals(XML_TRUE))
            m_isPersisted = true;
         else
            m_isPersisted = false;

         // REQUIRED: owner element
         node = tree.getNextElement(ELEM_OWNER, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ELEM_OWNER,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         node = tree.getNextElement(PSLocator.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSLocator.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         m_owner = new PSLocator(node);

         // REQUIRED: dependent element
         tree.setCurrent(sourceNode);
         node = tree.getNextElement(ELEM_DEPENDENT, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ELEM_DEPENDENT,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         try
         {
            setDependentCommunityId(
               PSXMLDomUtil.checkAttributeInt(node, ATTR_COMMUNITYID, false));
         }
         catch (NumberFormatException ex) { /* ignore already set */ }
         try
         {
            setDependentObjectType(
               PSXMLDomUtil.checkAttributeInt(node, ATTR_OBJECTTYPE, false));
         }
         catch (NumberFormatException ex) { /* ignore already set */ }

         node = tree.getNextElement(PSLocator.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSLocator.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         m_dependent = new PSLocator(node);

         // OPTIONAL: PSXPropertySet element
         tree.setCurrent(sourceNode);
         initUserProperties();
         node = tree.getNextElement(PROPERTY_LIST, firstFlags);
         if (node != null)
         {
            node = PSXMLDomUtil.getFirstElementChild(node);
            PSRelationshipPropertyData prop, currProp;
            while (node != null)
            {
               currProp = new PSRelationshipPropertyData(node);
               prop = findUserProperty(currProp.getName());
               if (prop != null)
               {
                  prop.setPersisted(currProp.isPersisted());
                  prop.setValue(currProp.getValue());
               }
               else
               {
                  log.warn("Unknown property name: ' {} ' in fromXml()", currProp.getName());
               }
               
               node = PSXMLDomUtil.getNextElementSibling(node);
            }
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ATTR_ID, Integer.toString(m_id));
      root.setAttribute(ATTR_CONFIG, m_config.getName());
      root.setAttribute(ATTR_ISPERSISTED, m_isPersisted? XML_TRUE : XML_FALSE);

      Element elem = null;

      // add owner locator
      elem = PSXmlDocumentBuilder.addEmptyElement(doc, root, ELEM_OWNER);
      elem.appendChild(m_owner.toXml(doc));

      // add dependent locator
      elem = PSXmlDocumentBuilder.addEmptyElement(doc, root, ELEM_DEPENDENT);
      elem.setAttribute(ATTR_COMMUNITYID, ""+getDependentCommunityId());
      elem.setAttribute(ATTR_OBJECTTYPE, ""+getDependentObjectType());
      elem.appendChild(m_dependent.toXml(doc));

      // add relationship properties
      if (! m_userProperties.isEmpty())
      {
         elem = doc.createElement(PROPERTY_LIST);
         root.appendChild(elem);
      }
      for (PSRelationshipPropertyData prop : m_userProperties)
      {
         elem.appendChild(prop.toXml(doc));
      }

      return root;
   }

   /**
    * Determines if this object is persisted in the repository.
    *
    * @return <code>true</code> if this object does exist in the repository;
    *    otherwise return <code>false</code>. Default to <code>false</code>.
    */
   public boolean isPersisted()
   {
      return m_id != -1 && m_isPersisted;
   }

   /**
    * Set the persistent status. It is typically used in conjunction with 
    * {@link #setId(int)} when creating a new object.
    * 
    * @param isPersisted the to be set persistent status. <code>true</code> if
    *    the object is already persisted in the repository; otherwise 
    *    <code>false</code>. Default to <code>false</code>.
    */
   public void setPersisted(boolean isPersisted)
   {
      m_isPersisted = isPersisted;
   }

   /**
    * Indicate if this object has been persisted in the repository.
    * <code>true</code> if it is persisted; otherwise <code>false</code>.
    * Default to <code>false</code>.
    */
   @Transient
   private boolean m_isPersisted = false;

   /**
    * Returns this as string.
    *
    * @return the XML representation as string, never <code>null</code>.
    */
   @Override
   public String toString()
   {
      return PSXmlDocumentBuilder.toString(
         toXml(PSXmlDocumentBuilder.createXmlDocument()));
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationship";

   /**
    * The owner locator, initialized in ctor, never <code>null</code> after
    * that, may be modified by calls to <code>setOwner()</code>.
    */
   private PSLocator m_owner = null;

   /**
    * The dependent locator, initialized in ctor, never <code>null</code> after
    * that, may be modified by calls to <code>setDependent()</code>.
    */
   private PSLocator m_dependent = null;

   /**
    * The dependent's community id, set by default to -1 (indicating all
    * communities), set in the ctor, may be reset by setDependentCommunityId()
    */
   private int m_dependentCommunityId = -1;

   /**
    * The dependent's object type, set by default to 1 (indicating an ITEM), set
    * in the ctor, may be reset by setDependentObjectType()
    */
   private int m_dependentObjectType = PSCmsObject.TYPE_ITEM;

   /**
    * The relationship configuration, initialized in constructor, never changed
    * after that.
    */
   private PSRelationshipConfig m_config = null;
   
   /**
    * The user properties. Defaults to EMPTY list, never <code>null</code>.
    * Note, we must to use {@link List} for the user properties; otherwise
    * the {@link #equals(Object)} method may not work correctly. 
    */
   @SuppressWarnings("unchecked")
   private List<PSRelationshipPropertyData> m_userProperties = 
      Collections.EMPTY_LIST;
   
   /**
    * The logger for this class.
    */
   private static final Logger log = LogManager.getLogger(PSRelationship.class);

   /*
    * The following strings define all elements/attributes used to parse/create
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String ATTR_CONFIG = "config";
   private static final String ATTR_ID = "id";
   private static final String ATTR_COMMUNITYID = "communityid";
   private static final String ATTR_OBJECTTYPE = "objecttype";
   private final static String ATTR_ISPERSISTED = "persisted";
   private static final String ELEM_OWNER = "Owner";
   private static final String ELEM_DEPENDENT = "Dependent";

   private static final String PROPERTY_LIST = "PropertyList";
   
   /**
    * True boolean value in XML
    */
   public static final String XML_TRUE = "true";

   /**
    * False boolean value in XML
    */
   public static final String XML_FALSE = "false";
   
}
