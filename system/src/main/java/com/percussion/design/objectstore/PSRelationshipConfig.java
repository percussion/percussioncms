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
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class represents objects conforming to the
 * sys_RelationshipConfigLookup.dtd. The contents of RelationshipConfig
 * elements are expanded using the sys_RelationshipConfig.dtd.
 */
public class PSRelationshipConfig extends PSComponent
   implements IPSCatalogSummary, IPSCloneTuner
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    it may be <code>null</code>.
    * @param parentComponents the parent objects of this object, it may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationshipConfig(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Simply call {@link #PSRelationshipConfig(Element, IPSDocument, List)
    *  PSRelationshipConfig(Element, null, null)}
    */
   public PSRelationshipConfig(Element sourceNode) 
      throws PSUnknownNodeTypeException
   {
      this(sourceNode, null, null);
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * <p>
    * Note, the id will not be copied from the supplied config object.
    *
    * @param c a valid <code>PSRelationshipConfig</code>, not <code>null</code>.
    */
   public void copyFrom(PSRelationshipConfig c)
   {
      // don't call super.copyFrom(c) because it will call setId().
      if (c == null)
         throw new IllegalArgumentException("c cannot be null");
      
      m_description = c.m_description;
      m_effects = c.m_effects;
      setName(c.m_name);
      m_label = c.m_label;
      m_type = c.m_type;
      m_category = c.m_category;
      m_sysProps = c.m_sysProps;
      m_sysPropMap = c.m_sysPropMap;
      m_userProps = c.m_userProps;
      m_userPropMap = c.m_userPropMap;
      m_processChecks = c.m_processChecks;
      m_cloneOverrideFieldList = c.m_cloneOverrideFieldList;
      
      m_useOwnerRevision = c.m_useOwnerRevision;
      m_useDependentRevision = c.m_useDependentRevision;
   }

   /**
    * Constructs the relationship configuration with specified name and type.
    * Will have default system properties and uses name as label too.
    *
    * @param name the name of the config, may not be <code>null</code> or empty.
    *   It must not contain any white space characters, 
    *   see {@link #setName(String)} for detail.
    * @param type the type of the rel  config, RS_TYPE_SYSTEM or RS_TYPE_USER, 
    * may not be <code>null</code> or empty.
    */
   PSRelationshipConfig(String name, String type)
   {
      setName(name);
      setType(type);
      m_label = name;
      m_category = CATEGORY_ACTIVE_ASSEMBLY;

      for (PSProperty sysPropDefault : ms_systemDefaults)
         m_sysProps.add(sysPropDefault.clone());
      resetSysPropMap();
   }

   /**
    * Constructs a relationship config with specified name, type and category.
    * 
    * @param name the name of the config, may not be <code>null</code> or empty.
    *   It must not contain any white space characters, 
    *   see {@link #setName(String)} for detail.
    * @param type the type of the relationship config, must be one of the 
    *    {@link #RS_TYPE_SYSTEM} or {@link #RS_TYPE_USER}. 
    * @param category the category of the config, must be one of the 
    *    CATEGORY_XXX values.
    */
   public PSRelationshipConfig(String name, String type, String category)
   {
      this(name, type);
      setCategory(category);
   }
   
   /**
    * Implements IPSCatalogSummary#getGUID(). This can only be called after 
    * it contains an assigned id, the UUID of it is greater than <code>0</code>.
    * 
    * @see #setId(int)
    */
   public IPSGuid getGUID()
   {
      if (!isAssinedId())
         throw new IllegalStateException("The id must be an assigned value.");

      return new PSGuid(PSTypeEnum.RELATIONSHIP_CONFIGNAME, getId());
   }
   
   /**
    * Override {@link PSComponent#setId(int)}. It sets a valid id, but the id
    * cannot be reset to another one if the object already has a valid id.
    * The id of the relationships are stored in the id/name mapping table,
    * which can only obtained and set by system, not exposed to the public.
    * A new (or next available) id can be obtained from 
    * {@link PSGuidHelper#generateNextLong(PSTypeEnum)}
    * for a new relationship configuration.
    * 
    * @param id the assigned id, must be greater than <code>0</code>. 
    */
   @Override
   public void setId(int id)
   {
      if (isAssinedId())
         throw new IllegalStateException("The id cannot be reset.");
      if (id <= 0)
         throw new IllegalArgumentException("The id must be > 0.");
      
      super.setId(id);
   }
   
   /**
    * @return <code>true</code> if the object contains an assigned id, which
    *   is not its default (unknown) value.
    * @see #setId(int)
    */
   public boolean isAssinedId()
   {
      return getId() != UNKNOWN_ID;
   }
   
   /**
    * Sets the id to the default (unknown) value.
    */
   public void resetId()
   {
      super.setId(UNKNOWN_ID);
   }
   
   /**
    * This should only be used externally by classes used for testing purposes.
    * The type is intended to be immutable.
    * 
    * @param type the type of the rel  config, RS_TYPE_SYSTEM or RS_TYPE_USER, 
    * may not be <code>null</code> or empty.
    */
   public void setType(String type)
   {
      if (type == null
         || (!type.equals(RS_TYPE_SYSTEM)
            && !type.equals(RS_TYPE_USER)))
      {
         throw new IllegalArgumentException(
         "type must not be null and must be one of RS_TYPE_XXXX values.");
      }
      m_type = type;
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
      if (!(o instanceof PSRelationshipConfig))
         return false;

      PSRelationshipConfig t = (PSRelationshipConfig) o;

      boolean equal = true;
      if (!m_name.equals(t.m_name))
         equal = false;
      else if ((m_description == null) ^ (t.m_description == null))
         equal = false;
      else if ( m_description != null && !m_description.equals(
            t.m_description))
      {
         equal = false;
      }
      else if (!m_effects.equals(t.m_effects))
         equal = false;
      else if (!m_sysProps.equals(t.m_sysProps))
         equal = false;
      else if (!m_userProps.equals(t.m_userProps))
         equal = false;
      else if (
         (m_cloneOverrideFieldList == null
            && t.m_cloneOverrideFieldList != null)
            || (m_cloneOverrideFieldList != null
               && t.m_cloneOverrideFieldList == null))
         equal = false;
      else if (
         m_cloneOverrideFieldList != null
            && !m_cloneOverrideFieldList.equals(t.m_cloneOverrideFieldList))
         equal = false; 

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_name)
            .append(m_description)
            .toHashCode();
   }

   /**
    * Makes a deep copy of this object.
    *
    * @return a deep copy of this object, never <code>null</code>.
    */
   @Override
   public Object clone()
   {
      PSRelationshipConfig config = (PSRelationshipConfig) super.clone();

      config.setName(m_name);
      config.m_description = m_description;

      config.m_effects = new PSCollection(PSConditionalEffect.class);
      Iterator iter = m_effects.iterator();
      while (iter.hasNext())
         config.m_effects.add(((PSConditionalEffect)iter.next()).clone());

      config.m_sysProps = (PSPropertySet) m_sysProps.clone();
      resetSysPropMap();

      config.m_userProps = (PSPropertySet) m_userProps.clone();
      resetUserPropMap();

      config.m_processChecks = new PSCollection(PSProcessCheck.class);
      iter = m_processChecks.iterator();
      while (iter.hasNext())
         config.m_processChecks.add(((PSProcessCheck)iter.next()).clone());

      config.m_cloneOverrideFieldList =
         (PSCloneOverrideFieldList) m_cloneOverrideFieldList.clone();

      return config;
   }
   
   /**
    * Can the dependent of this relationship have mutliple owners. This is a 
    * system property by the virtue of the category of the relationship. For 
    * example, an item can be related to multiple items via active assembly 
    * category of relationships but cannot be related to multiple items 
    * (objects) via folder category of relationships.
    * @return <code>true</code> if the dependent of this relationship can 
    * have multiple owners, <code>false</code> otherise. 
    */
   public boolean canHaveMultipleOwers()
   {
      //Only active aasembly category relationship support dependents having 
      //multiple owners
      if(getCategory().equals(CATEGORY_ACTIVE_ASSEMBLY))
         return true;
      return false;
   }

   /**
    * Sets the name (unique) of the relationship.
    *
    * @param name name of relationship, may not be <code>null</code> or empty.
    *   The name may not contain any white space characters, such as ' ', 
    *   '\t' and '\n', see {@link Character#isWhitespace(char)} for detail.
    */
   public void setName(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");
      
      for (int i=0; i<name.length(); i++)
      {
         if (Character.isWhitespace(name.charAt(i)))
            throw new IllegalArgumentException("Relationship config name ("
                  + name + ") must not contain white space characters.");
      }

      m_name = name;
   }

   /**
    * Sets the label (display name) of the relationship.
    *
    * @param label label of relationship, may not be <code>null</code> or empty.
    */
   public void setLabel(String label)
   {
      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      m_label = label;
   }

   /**
    * Sets the category of the relationship. Used to group different
    * relationships.
    *
    * @param category category of relationship, must be one of the
    *    CATEGORY_XXX. 
    */
   public void setCategory(String category)
   {
      for (PSEntry entry : CATEGORY_ENUM)
      {
         if (entry.getValue().equalsIgnoreCase(category))
         {
            m_category = entry.getValue();
            return;
         }
      }
      
      throw new IllegalArgumentException("category must be one of the CATEGORY_XXX.");
   }

   /**
    * Sets the description of the relationship.
    *
    * @param desc description of relationship, may be <code>null</code> or empty
    */
   public void setDescription(String desc)
   {
      m_description = desc;
   }

   /**
    * Get the relationship name. The name is unique server-wide.
    *
    * @return the relationship name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the relationship label.
    *
    * @return the relationship label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * Get the type of relationship
    * @return one of the RS_TYPE_XXXX values. Never <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Get the relationship category.
    *
    * @return the relationship category, never <code>null</code> or empty.
    */
   public String getCategory()
   {
      return m_category;
   }

   /**
    * Get the relationship description.
    *
    * @return the relationship description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Get all relationship effects.
    *
    * @return a list of relationship effects 
    *    ({@link com.percussion.design.objectstore.PSConditionalEffect} 
    *    objects), never <code>null</code>, might be empty.
    */
   public Iterator getEffects()
   {
      return m_effects.iterator();
   }

   /**
    * Get all relationship process checks.
    *
    * @return a list of process checks (PSProcessCheck objects),
    *    never <code>null</code>, might be empty.
    */
   public Iterator getProcessChecks()
   {
      return m_processChecks.iterator();
   }

   /**
    * Get the process check for the supplied name. The name compairsion is
    * case sensitive.
    *
    * @param name the name of the process check, not <code>null</code> or
    *    empty.
    * @return the process check if found, <code>null</code> otherwise.
    */
   public PSProcessCheck getProcessCheck(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      Iterator checks = getProcessChecks();
      while (checks.hasNext())
      {
         PSProcessCheck check = (PSProcessCheck) checks.next();
         if (check.getName().equals(name))
            return check;
      }

      return null;
   }

   /**
    * Get all system properties.
    *
    * @return a new map with a copy of all system properties,
    *    never <code>null</code>, might be empty.
    */
   public Map<String, String> getSystemProperties()
   {
      return m_sysPropMap;
   }

   /**
    * Reset the system property mapper {@link #m_sysPropMap}. This should be
    * called right after {@link #m_sysProps} is updated. 
    */
   private void resetSysPropMap()
   {
      m_sysPropMap.clear();

      Iterator sysProps = m_sysProps.iterator();
      while(sysProps.hasNext())
      {
         PSProperty property = (PSProperty)sysProps.next();

         String strValue = null;
         Object value = property.getValue();
         if (property.getType() == PSProperty.TYPE_BOOLEAN)
         {
            strValue = PSProperty.XML_BOOL_NO;
            Boolean bool = (Boolean) value;
            if (bool != null && bool.booleanValue())
               strValue = PSProperty.XML_BOOL_YES;
         }
         else
         {
            if (value != null)
               strValue = value.toString();
         }

         m_sysPropMap.put(property.getName(), strValue);
      }
      m_useOwnerRevision = null;
      m_useDependentRevision = null;
   }

   /**
    * Gets the list of system properties.
    *
    * @return the iterator over zero or more <code>PSProperty</code> objects,
    * never <code>null</code> or empty.
    */
   public Iterator getSysProperties()
   {
      return m_sysProps.iterator();
   }

   /**
    * Gets the list of system properties with built-in effects properties
    * and allow cloning property filtered out. Filters out RS_ALLOWCLONING.
    *
    * @return the iterator over zero or more <code>PSProperty</code> objects,
    * never <code>null</code> or empty.
    */
   public Iterator getSysPropertiesFiltered()
   {
      final List<PSProperty> props = new ArrayList<>();
      Iterator it = m_sysProps.iterator();
      while(it.hasNext())
      {
         final PSProperty prop = (PSProperty)it.next();
         if(!prop.getName().equals(RS_ALLOWCLONING))
            props.add(prop);
      }

      return props.iterator();
   }

   /**
    * Removes the system property with the supplied name if present. There is
    * no change if the specified property is not present.
    * 
    * @param name the name of the system property, never <code>null</code>
    * 
    * @return <code>true</code> if removed the specified system property;
    *   otherwise, return <code>false</code>.
    */
   public boolean removeSysProperty(String name)
   {
      PSProperty prop = getSysProperty(name);
      if (prop != null)
      {
         m_sysProps.remove(prop);
         m_sysPropMap.remove(name);
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Get the specified system property
    * @param name the property name to lookup, not <code>null</code>.
    * @return the requested system property as PSProperty object or
    * <code>null</code> if not found.
    */
    public PSProperty getSysProperty(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("property name cannot be null");

      Iterator props = getSysProperties();
      PSProperty property = null;
      while(props.hasNext())
      {
         property = (PSProperty) props.next();
         if (property.getName().equals(name))
            return property;
      }

      return null;
   }

   /**
    * Get the specified system property.
    *
    * @param name the property name to lookup, not <code>null</code>.
    * @return the requested system property or empty string if not found.
    */
   public String getSystemProperty(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("property name cannot be null");

      String prop = getSystemProperties().get(name);
      if (prop != null)
         return prop;

      return "";
   }

   /**
    * Get all user properties as a map of name/value pairs. The key is the
    * property name as <code>String</code> and the value is the property
    * value as <code>String</code>.
    *
    * @return a new map with a copy of all user properties,
    *    never <code>null</code>, might be empty.
    */
   public Map<String, String> getUserProperties()
   {
      return m_userPropMap;
   }
   
   /**
    * Get a list of custom property names, which is part of user properties, but
    * is not part of the pre-defined user properties, which is returned by 
    * {@link #getPreDefinedUserPropertyNames()}.
    * 
    * @return a list of customer property names, never <code>null</code>, but
    *    may be empty. 
    */
   public Collection<String> getCustomPropertyNames()
   {
      return m_customPropertyNames;
   }
   
   /**
    * Get a list of pre-defined user property names. These are also the 
    * required user properties for a Relationship Configuration instance with 
    * {@link #CATEGORY_ACTIVE_ASSEMBLY} category.
    * 
    * @return a list of property names, never <code>null</code>, but may be
    *    empty.
    */
   public static Collection<String> getPreDefinedUserPropertyNames()
   {
      return ms_predefinedUserPropNames;
   }

   /**
    * Custom property names, never <code>null</code>, but may be empty.
    * @see #getCustomPropertyNames() for more info.
    */
   private HashSet<String> m_customPropertyNames = new HashSet<>();
   
   /**
    * Reset the user property mapper {@link #m_userPropMap}. This should be
    * called after {@link #m_userProps} is updated.
    */
   @SuppressWarnings("unchecked")
   private void resetUserPropMap()
   {
      m_userPropMap.clear();
      
      Iterator userProps = m_userProps.iterator();
      m_customPropertyNames.clear();
      while (userProps.hasNext())
      {
         PSProperty property = (PSProperty)userProps.next();
         Object value = property.getValue();
         m_userPropMap.put(property.getName(), value == null ? null : value
               .toString());
         
         if (! ms_predefinedUserPropNames.contains(property.getName()))
            m_customPropertyNames.add(property.getName());
      }
   }

   /**
    * Gets the list of user-defined properties as an iterator over
    * <code>PSProperty</code> objects.
    *
    * @return the iterator over zero or more <code>PSProperty</code> objects,
    *    never <code>null</code>
    */
   public Iterator getUserDefProperties()
   {
      return m_userProps.iterator();
   }

   /**
    * Sets the list of user defined properties.
    *
    * @param props the props to set, may not be <code>null</code>, can be empty.
    * All entries must be instances of <code>PSProperty</code>.
    */
   public void setUserDefProperties(Iterator props)
   {
      if(props == null)
         throw new IllegalArgumentException("props may not be null.");

      m_userProps.clear();
      while(props.hasNext())
         m_userProps.add(props.next());
      
      resetUserPropMap();
   }

   /**
    * Sets the list of effects to be run for this relationship.
    *
    * @param effects the list of effects, may not be <code>null</code>, can be
    * empty. All entries must be instances of <code>PSConditionalEffect</code>.
    */
   public void setEffects(Iterator effects)
   {
      if(effects == null)
         throw new IllegalArgumentException("effects may not be null.");

      m_effects.clear();
      while(effects.hasNext())
         m_effects.add(effects.next());
   }

   /**
    * Sets the list of process checks to be run for this relationship.
    *
    * @param checks the list of checks, may not be <code>null</code>,
    * can be empty. All entries must be instances of <code>PSProcessCheck</code>.
    */
   public void setProcessChecks(Iterator checks)
   {
      if(checks == null)
         throw new IllegalArgumentException("checks may not be null.");

      m_processChecks.clear();
      while(checks.hasNext())
         m_processChecks.add(checks.next());
   }

   /**
    * Set the object containing the list of fields and their dynamic values 
    * to set for a cloned object that was created based on this relationship.
    * @param fieldList the new fields, it may be <code>null</code>.
    */
   public void setCloneOverrideFieldList(PSCloneOverrideFieldList fieldList)
   {
      m_cloneOverrideFieldList = fieldList;
   }

   /**
    * Get the specified user property value as <code>String</code>.
    *
    * @param name the property name to lookup, may be <code>null</code> or
    *    empty.
    * @return the requested user property value as <code>String</code> or
    *    <code>null</code> if not found.
    */
   public String getUserProperty(String name)
   {
      Object prop = getUserProperties().get(name);
      if (prop != null)
         return prop.toString();

      return null;
   }

   /**
    * Get the complete specified user property.
    *
    * @param name the property name to lookup, not <code>null</code>, may be
    *    empty. Property names are case sensitive.
    * @return the requested user property as <code>PSProperty</code> object or
    *    <code>null</code> if not found.
    */
    public PSProperty getUsrProperty(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("property name cannot be null");

      Iterator props = getUserDefProperties();
      PSProperty property = null;
      while (props.hasNext())
      {
         property = (PSProperty) props.next();
         if (property.getName().equals(name))
            return property;
      }

      return null;
   }

   /**
    * Get the requested property. First we try to get it from the system
    * properties. If that returns <code>null</code> we get it from the user
    * properties.
    *
    * @param name the property name, not <code>null</code>, may be empty.
    *    Property names are case sensitive.
    * @return the requested property as <code>PSProperty</code> object or
    *    <code>null</code> if not found.
    */
   public PSProperty getProperty(String name)
   {
      PSProperty property = getSysProperty(name);
      if (property == null)
         property = getUsrProperty(name);

      return property;
   }

   /**
    * Finds whether this is a 'System' relationship config.
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>
    */
   public boolean isSystem()
   {
      return m_type.equals(RS_TYPE_SYSTEM);
   }

   /**
    * Finds whether this is a 'User' relationship config.
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>
    */
   public boolean isUser()
   {
      return m_type.equals(RS_TYPE_USER);
   }

   /**
    * Is the revision used for the owner locator?
    *
    * @return <code>true</code> if used, <code>false</code> otherwise.
    */
   public boolean useOwnerRevision()
   {
      if (m_useOwnerRevision == null)
      {
         m_useOwnerRevision = getSystemProperty(
               RS_USEOWNERREVISION).equalsIgnoreCase(PSProperty.XML_BOOL_YES);
      }
      return m_useOwnerRevision;
   }

   /**
    * A transient data to improve the performance since it may used VERY
    * frequently. Set by {@link #useOwnerRevision()}
    */
   private Boolean m_useOwnerRevision = null;

   /**
    * A transient data to improve the performance since it may used VERY
    * frequently. Set by {@link #useDependentRevision()}
    */
   private Boolean m_useDependentRevision = null;
   
   
   /**
    * Is the revision used for the dependents locator?
    *
    * @return <code>true</code> if used, <code>false</code> otherwise.
    */
   public boolean useDependentRevision()
   {
      if (m_useDependentRevision == null)
      {
         m_useDependentRevision = getSystemProperty(
               RS_USEDEPENDENTREVISION).equalsIgnoreCase(
               PSProperty.XML_BOOL_YES);
      }
      return m_useDependentRevision;
   }

   /**
    * Is cloning allowed for this relationship type?
    *
    * @return <code>true</code> if cloning is allowed, <code>false</code>
    *    otherwise.
    */
   public boolean isCloningAllowed()
   {
      return getSystemProperty(
         RS_ALLOWCLONING).equalsIgnoreCase(PSProperty.XML_BOOL_YES);
   }

   /**
    * Is the promotable system effect enabled?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPromotable()
   {
      if(getCategory().equals(
          PSRelationshipConfig.CATEGORY_PROMOTABLE))
      {
         return true;
      }
      return false;
   }
   
   /**
    * Test if this relationship instance is of category active assembly.
    * 
    * @return <code>true</code> if this is an active assembly relationship,
    *    <code>false</code> otherwise.
    */
   public boolean isActiveAssemblyRelationship()
   {
      String category = getCategory();
      return category.equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
   }
   
   
   /**
    * Is this relationship to be skipped when the item is promoted?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isSkipPromotion()
   {
      return getSystemProperty(
         RS_SKIPPROMOTION).equalsIgnoreCase(PSProperty.XML_BOOL_YES);
   }

   /**
    * Should the server id or the current request user be used to execute
    * effects?
    *
    * @return <code>true</code> to use the server id, <code>false</code>
    *    otherwise.
    */
   public boolean useServerId()
   {
      return getSystemProperty(
         RS_USESERVERID).equalsIgnoreCase(PSProperty.XML_BOOL_YES);
   }

   /**
    * Should we filter this relationship based on community?
    *
    * @return <code>true</code> if yes, <code>false</code> otherwise.
    */
   public boolean useCommunityFilter()
   {
      return getSystemProperty(
         RS_USECOMMUNITYFILTER).equalsIgnoreCase(PSProperty.XML_BOOL_YES);
   }

   /**
    * 
    * @return Get the object containing the list of fields and their dynamic 
    * values to set for a cloned object that was created based on this 
    * relationship. May be <code>null</code>.
    */
   public PSCloneOverrideFieldList getCloneOverrideFieldList()
   {
      return m_cloneOverrideFieldList;
   }

   /** @see IPSComponent */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
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
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         setIdFromXml(sourceNode);
         
         // REQUIRED: name attribute
         String name = tree.getElementData(XML_ATTR_NAME);
         try
         {
            setName(name);
         }
         catch (IllegalArgumentException e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               XML_ATTR_NAME,
               e.getLocalizedMessage()
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         m_label = tree.getElementData(XML_ATTR_LABEL);
         if (m_label == null || m_label.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               XML_ATTR_LABEL,
               "null or empty"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         m_category = tree.getElementData(XML_ATTR_CATEGORY);
         if (m_category == null || m_category.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               XML_ATTR_CATEGORY,
               "null or empty"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }        

         m_type =
            getEnumeratedAttribute(
               tree,
               XML_ATTR_TYPE,
               new String[] { RS_TYPE_SYSTEM, RS_TYPE_USER });

         // OPTIONAL: PSXCloneOverrideFieldList element
         node = tree.getNextElement(CLONEFIELD_OVERRIDE_LIST_ELEM, firstFlags);
         if (node != null)
            m_cloneOverrideFieldList =
               new PSCloneOverrideFieldList(node, null, null);

         // OPTIONAL: EffectSet element
         tree.setCurrent(sourceNode);
         node = tree.getNextElement(EFFECT_SET_ELEM, firstFlags);
         m_effects.clear();
         if (node != null)
         {
            node = tree.getNextElement(
               PSConditionalEffect.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_effects.add(new PSConditionalEffect(node, null, null));

               node = tree.getNextElement(
                  PSConditionalEffect.XML_NODE_NAME, nextFlags);
            }
         }

         // OPTIONAL: PSXPropertySet element
         tree.setCurrent(sourceNode);
         node = tree.getNextElement(PSPropertySet.XML_NODE_NAME, firstFlags);
         m_sysProps.clear();
         if (node != null)
            m_sysProps.fromXml(node, parentDoc, parentComponents);
         addSystemPropertyDefaults(m_sysProps);
         resetSysPropMap();

         // OPTIONAL: UserPropertySet element
         tree.setCurrent(sourceNode);
         node = tree.getNextElement(USER_PROPERTY_SET_ELEM, firstFlags);
         m_userProps.clear();
         if (node != null)
         {
            node = tree.getNextElement(PSProperty.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_userProps.add( new PSProperty(node) );
               node = tree.getNextElement(PSProperty.XML_NODE_NAME, nextFlags);
            }
         }
         resetUserPropMap();

         tree.setCurrent(sourceNode);
         node = tree.getNextElement(PROCESS_CHECKS_ELEM, firstFlags);
         m_processChecks.clear();
         if (node != null)
         {
            node = tree.getNextElement(
               PSProcessCheck.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_processChecks.add( new PSProcessCheck(node, null, null) );
               node = tree.getNextElement(
                  PSProcessCheck.XML_NODE_NAME, nextFlags);
            }
         }

         // OPTIONAL: Description element
         tree.setCurrent(sourceNode);
         node = tree.getNextElement(DESCRIPTION_ELEM, firstFlags);
         if (node != null)
            m_description = PSXmlTreeWalker.getElementData(node);
         else
            m_description = "";
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Sets the id from the XML representation of the object. Set the id
    * to {@link PSComponent#UNKNOWN_ID} if the XML does not contain a
    * valid id.
    * 
    * @param source the XML representation of the config object.
    */
   private void setIdFromXml(Element source)
   {
      String data = source.getAttribute(ID_ATTR);
      try 
      {
         super.setId(UNKNOWN_ID);
         if (data != null && data.trim().length() > 0)
         {
            int id = Integer.parseInt(data);
            if (id > 0)
               super.setId(id);
         }
      } 
      catch (NumberFormatException e) 
      {
         // ignore invalid data.
      }
   
   }
   
   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPE, m_type);
      root.setAttribute(XML_ATTR_LABEL, m_label);
      
      // set the optional id if it is valid
      if (getId() != UNKNOWN_ID)
         root.setAttribute(ID_ATTR, String.valueOf(getId()));
      
      if(m_category != null)
         root.setAttribute(XML_ATTR_CATEGORY, m_category);

      Element elem = null;
      
      //add clone override fields
      if(m_cloneOverrideFieldList != null)
         root.appendChild(m_cloneOverrideFieldList.toXml(doc));

      // add effects
      Iterator effects = getEffects();
      if (effects.hasNext())
      {
         elem = doc.createElement(EFFECT_SET_ELEM);
         root.appendChild(elem);
         while (effects.hasNext())
            elem.appendChild(((IPSComponent) effects.next()).toXml(doc));
      }

      // add system properties
      if (!m_sysProps.isEmpty())
         root.appendChild(m_sysProps.toXml(doc));

      // add user properties
      Iterator keys = m_userProps.iterator();
      if (keys.hasNext())
      {
         elem = doc.createElement(USER_PROPERTY_SET_ELEM);
         root.appendChild(elem);
         while (keys.hasNext())
            elem.appendChild(((PSProperty)keys.next()).toXml(doc) );
      }

      // add process checks
      keys = m_processChecks.iterator();
      if (keys.hasNext())
      {
         elem = doc.createElement(PROCESS_CHECKS_ELEM);
         root.appendChild(elem);
         while (keys.hasNext())
            elem.appendChild( ((PSProcessCheck)keys.next()).toXml(doc) );
      }

      if(m_description != null)
      {
         PSXmlDocumentBuilder.addElement(
            doc, root, DESCRIPTION_ELEM, m_description);
      }

      return root;
   }

   /**
    * Gets the string representation of this object (Label of the relationship).
    *
    * @return the name, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return m_label;
   }

   /**
    * Checks if the supplied list of properties contains all defined system
    * properties and adds all system properties that are not found in the
    * supplied list.
    *
    * @param properties the properties list to be updated with all non
    *    existing system properties, assumed not <code>null</code>, may be
    *    empty.
    */
   private void addSystemPropertyDefaults(PSPropertySet properties)
   {
      for (PSProperty systemDefault : ms_systemDefaults)
      {
         boolean found = false;
         for (int i=0; !found && i<properties.size(); i++)
         {
            PSProperty property = (PSProperty) properties.get(i);
            if (systemDefault.getName().equals(property.getName()))
               found = true;
         }

         if (!found)
            properties.add(systemDefault.clone());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(java.lang.Object,
    * long)
    */
   public Object tuneClone(long newId)
   {
      m_id = (int) newId;
      // assume this is only used from the workbench, where user creating
      // user defined relationship configuration, so the type can only be user
      if ( isSystem() )
      {
         setType(RS_TYPE_USER);
      }
      return this;
   }

   /**
    * Gets the resource bundle that contains the description for system
    * properties.
    *
    * @return the bundle, never <code>null</code>
    */
   private static ResourceBundle getBundle()
   {
      if(ms_resBundle == null)
      {
         ms_resBundle = ResourceBundle.getBundle(
            "com.percussion.server.PSStringResources");
      }

      return ms_resBundle;
   }
   
   /**
    * Get a list of user property defaults. These defaults are used if the 
    * current configuration does not know about a property.
    * 
    * @return a list with user property defaults, never <code>null</code> or
    *    be empty.
    */
   public static Iterator getUserPropertyDefaults()
   {
      return ms_userDefaults.iterator();
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationshipConfig";

   /**
    * The <code>true</code> value used in configuration properties.
    */
   public static String PROPERTY_TRUE = "yes";

   /**
    * The <code>false</code> value used in configuration properties.
    */
   public static String PROPERTY_FALSE = "no";

   /**
    * The relationship property name that specifies whether or not cloning
    * is allowed.
    */
   public static String RS_ALLOWCLONING = "rs_allowcloning";

   /**
    * The relationship property name that specifies whether or not the owner
    * revision is used.
    */
   public static String RS_USEOWNERREVISION = "rs_useownerrevision";

   /**
    * The relationship property name that specifies whether or not the dependent
    * revision is used.
    */
   public static String RS_USEDEPENDENTREVISION = "rs_usedependentrevision";

   /**
    * The relationship property name that specifies whether or not to user
    * the internal server id to run effects.
    */
   public static String RS_USESERVERID = "rs_useserverid";

   /**
    * The relationship property name that specifies whether or not to filter
    * based on community id.
    */
   public static String RS_USECOMMUNITYFILTER = "rs_usecommunityfilter";

   /**
    * The relationship property name that enables or disables the promote
    * built in effect.
    * @deprecated we no more have built-in effects
    */
   public static String RS_PROMOTE = "rs_promote";

   /**
    * This system property is used if <code>rs_promote</code> is enabled. It
    * defines the transition to take for the original item if the new version
    * becomes public. Defaults to 'Default', in which case the first
    * transition in alpha order with the 'Default' flag set will be used to
    * move the original item out of the current public state.
    * @deprecated we no more have built-in effects
    */
   public static String RS_NAMEDTRANSITION = "rs_namedtransition";

   /**
    * The relationship property name that enables or disables strong
    * dependencies.
    * @deprecated we no more have built-in effects
    */
   public static String RS_STRONGDEPENDENCY = "rs_strongdependency";

   /**
    * This system property is used if <code>rs_strongdependency</code> is
    * enabled. It specifies whether or not to transit strong dependents into
    * a public state.
    * @deprecated we no more have built-in effects
    */
   public static String RS_FORCETRANSITION = "rs_forcetransition";

   /**

    * This system property is used to determine if relationships of this type
    * should be required to be packaged and deployed with the owner.  Defaults
    * to 'no', so that relationships are not by default required to be deployed
    * with the owner.
    */
   public static String RS_ISLOCALDEPENDENCY = "rs_islocaldependency";
   
   /**
    * This system property is used to determine if an incoming relationship 
    * to the item to be promoted has to be moved to the promoted item.
    */
   public static String RS_SKIPPROMOTION = "rs_skippromotion";

   /**
    * The enumeration of RS_ system property names as Strings,
    * never <code>null</code>. 
    */
   public static final Object[] RS_PROPERTY_NAME_ENUM =
   {
      RS_ALLOWCLONING,
      RS_USEOWNERREVISION,
      RS_USEDEPENDENTREVISION,
      RS_USESERVERID,
      RS_ISLOCALDEPENDENCY,
      RS_USECOMMUNITYFILTER,
      RS_SKIPPROMOTION
   };

   // pre-defined user property names.
  
   /**
    * One of the (optional) pre-defined user properties, it records the slot id 
    * of the relationship. It is used by Active Assembly relationship. 
    */
   public static final String PDU_SLOTID = "sys_slotid";

   /**
    * One of the (optional) pre-defined user properties, it records the sort 
    * rand of the relationship. It is used by Active Assembly relationship. 
    */
   public static final String PDU_SORTRANK = "sys_sortrank";

   /**
    * One of the (optional) pre-defined user properties, it records the variant 
    * id of the relationship. It is used by Active Assembly relationship. 
    */
   public static final String PDU_VARIANTID = "sys_variantid";

   /**
    * One of the (optional) pre-defined user properties, it records the folder 
    * id of the relationship. It is used by cross site link. 
    */
   public static final String PDU_FOLDERID = "sys_folderid";
   
   /**
    * One of the (optional) pre-defined user properties, it records the site id
    * of the relationship. It is used by cross site link. 
    */
   public static final String PDU_SITEID = "sys_siteid";
   
   /**
    * The widget name, as an optional pre-defined uesr property.
    * This widget name may relate to {@link #PDU_SLOTID} as the widget instance id.
    */
   public static final String PDU_WIDGET_NAME = "sys_widgetname";
   
   /**
    * One of the pre-defined user properties, to marks a relationship as an 
    * inline link relationship. The value of this property is the name of the 
    * field that contains the inline link plus the child row id delimited with 
    * a colon if this field belongs to a child editor. If this property is 
    * missing, <code>null</code> or empty, the relationship is not treated as 
    * inline link relationship. This is a user property used in active assembly 
    * relationships.
    */
   public static final String PDU_INLINERELATIONSHIP = "rs_inlinerelationship";

   /**
    * @deprecated use {@link #PDU_INLINERELATIONSHIP} instead.
    */
   public static final String RS_INLINERELATIONSHIP = PDU_INLINERELATIONSHIP;

  
   /**
    * The keyword used to allow deep cloning in the "allowedTypes" system
    * property.
    */
   public static final String DEEP_CLONING = "deep";

   /**
    * The keyword used to allow shallow cloning in the "allowedTypes" system
    * property.
    */
   public static final String SHALLOW_CLONING = "shallow";

   /**
    * The specifier for the system relationship New Copy.
    */
   public static final String TYPE_NEW_COPY = "NewCopy";
   
   /**
    * The specifier for the system relationship Widget Assembly.
    */
   public static final String TYPE_WIDGET_ASSEMBLY = "Widget-Assembly";
   
   /**
    * The specifier for the system relationship Widget Content.
    */
   public static final String TYPE_WIDGET_CONTENT = "Widget-Content";
   
   /**
    * The specifier for the system relationship Local Content.
    * This relationship is used to associate local content items
    * with a widget and parent page or template.  Local content items
    * will only be associated to one page or template.
    */
   public static final String TYPE_LOCAL_CONTENT = "LocalContent";

   /**
    * The specifier for the system relationship Active Assembly.
    */
   public static final String TYPE_ACTIVE_ASSEMBLY = "ActiveAssembly";

   /**
    * The specifier for the system relationship Mandatory Active Assembly.
    */
   public static final String TYPE_ACTIVE_ASSEMBLY_MANDATORY =
      "ActiveAssembly-Mandatory";

   /**
    * The specifier for the system relationship Related Content (Active 
    * Assembly).
    * @deprecated use {@link #TYPE_ACTIVE_ASSEMBLY} instead.
    */
   public static final String TYPE_RELATED_CONTENT = TYPE_ACTIVE_ASSEMBLY;

   /**
    * The specifier for the system relationship Translation.
    */
   public static final String TYPE_TRANSLATION = "Translation";

   /**
    * The specifier for the system relationship Mandatory Translation.
    */
   public static final String TYPE_TRANSLATION_MANDATORY =
      "Translation-Mandatory";

   /**
    * The specifier for the system relationship Translation.
    * @deprecated use {@link #TYPE_TRANSLATION} instead
    */
   public static final String TRANSLATION = TYPE_TRANSLATION;

   /**
    * The specifier for the system relationship Promotable Version.
    */
   public static final String TYPE_PROMOTABLE_VERSION = "PromotableVersion";

   /**
    * The specifier for the system relationship Folder Content.
    */
   public static final String TYPE_FOLDER_CONTENT = "FolderContent";

   /**
    * The specifier for the system relationship recycled content.
    */
   public static final String TYPE_RECYCLED_CONTENT = "RecycledContent";

   /**
    * The id for the active assembly (system) relationship, which is stored
    * in {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table. 
    */
   public static final int ID_ACTIVE_ASSEMBLY = 1;
   
   /**
    * The id of the active assembly mandatory (system) relationship, which is 
    * stored in {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_ACTIVE_ASSEMBLY_MANDATORY = 2;
   
   /**
    * The id of the folder (system) relationship, which is stored in
    * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_FOLDER_CONTENT = 3;

   /**
    * The id of the new copy (system) relationship, which is stored in
    * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_NEW_COPY = 4;
   
   /**
    * The id of the promotable version (system) relationship, which is stored
    * in {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_PROMOTABLE_VERSION = 5;
   
   /**
    * The id of the translation (system) relationship, which is stored in
    * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_TRANSLATION = 6;
   
   /**
    * The id of the translation mandatory (system) relationship, which is stored
    * in {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.  
    */
   public static final int ID_TRANSLATION_MANDATORY = 7;

   /**
    * The id of the recycled (system) relationship, which is stored in
    * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table.
    */
   public static final int ID_RECYCLED_CONTENT = 8;

   /**
    * A list of system defined relationship configs. The id and name the enum
    * are in sync with the table data in 
    * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME}.
    */
   public enum SysConfigEnum
   {
      ACTIVE_ASSEMBLY           (ID_ACTIVE_ASSEMBLY, TYPE_ACTIVE_ASSEMBLY),
      ACTIVE_ASSEMBLY_MANDATORY (ID_ACTIVE_ASSEMBLY_MANDATORY, TYPE_ACTIVE_ASSEMBLY_MANDATORY),
      FOLDER_CONTENT            (ID_FOLDER_CONTENT, TYPE_FOLDER_CONTENT),
      NEW_COPY                  (ID_NEW_COPY, TYPE_NEW_COPY),
      PROMOTABLE_VERSION        (ID_PROMOTABLE_VERSION, TYPE_PROMOTABLE_VERSION),
      @SuppressWarnings("hiding") 
      TRANSLATION               (ID_TRANSLATION, TYPE_TRANSLATION),
      TRNASLATION_MANDATORY     (ID_TRANSLATION_MANDATORY, TYPE_TRANSLATION_MANDATORY),
      RECYCLED_CONTENT          (ID_RECYCLED_CONTENT, TYPE_RECYCLED_CONTENT);
      
      /**
       * The numeric value of the Enum
       */
      private int m_id;
      
      /**
       * The name of the enum.
       */
      private String m_sysName;
      
      /**
       * Construct a enum from a numeric value and name
       * 
       * @param id the numeric value of the enum
       * @param name the name of the enum
       */
      SysConfigEnum(int id, String name)
      {
         m_id = id;
         m_sysName = name;
      }
      
      /**
       * @return the numeric value of the enum
       */
      public int getId()
      {
         return m_id;
      }
      
      /**
       * @return the name of the enum
       */
      public String getName()
      {
         return m_sysName;
      }
   }
   
   /**
    * The specifier for the system relationship category Active Assembly.
    */
   public static final String CATEGORY_ACTIVE_ASSEMBLY = "rs_activeassembly";

   /**
    * The specifier for the system relationship category Copy.
    */
   public static final String CATEGORY_COPY = "rs_copy";

   /**
    * The specifier for the system relationship category Folder.
    */
   public static final String CATEGORY_FOLDER = "rs_folder";

   /**
    * The specifier for the system relationship category recycled.
    */
   public static final String CATEGORY_RECYCLED = "rs_recycled";

   /**
    * The specifier for the system relationship category Generic.
    */
   public static final String CATEGORY_GENERIC = "rs_generic";
   
   /**
    * The specifier for the system relationship category Widget.
    */
   public static final String CATEGORY_WIDGET = "rs_widget";
      
   /**
    * The specifier for the system relationship category Promotable.
    */
   public static final String CATEGORY_PROMOTABLE = "rs_promotable";

   /**
    * The specifier for the system relationship category Translation.
    */
   public static final String CATEGORY_TRANSLATION = "rs_translation";

   /**
    * An enumeration of all relationship categories build into the system. 
    * Categories are used to group relationship types with similar purposes 
    * together.
    */
   public static final PSEntry[] CATEGORY_ENUM =
   {
      new PSEntry(CATEGORY_ACTIVE_ASSEMBLY, "Active Assembly"),
      new PSEntry(CATEGORY_COPY, "New Copy"),
      new PSEntry(CATEGORY_FOLDER, "Folder"),
      new PSEntry(CATEGORY_GENERIC, "Generic"),
      new PSEntry(CATEGORY_WIDGET, "Widget"),
      new PSEntry(CATEGORY_PROMOTABLE, "Promotable Version"),
      new PSEntry(CATEGORY_TRANSLATION, "Translation"),
      new PSEntry(CATEGORY_RECYCLED, "Recycled")
   };

   /**
    * An array of <code>String</code> with all system defined relationship
    * categories.
    */
   public static final String[] SYSTEM_RELATIONSHIP_CATEGORIES =
   {
      CATEGORY_ACTIVE_ASSEMBLY
   };

   /*
    * Impl note on FILTER_TYPE_xxx: Ideally, the value for xxx_COMMUNITY should 
    * just be the 2 other community values OR'd together, but for backwards 
    * compatibility, I'm leaving it with its original value.
    * Also, this whole concept is backwards. Flags should be positive, not
    * negative, as that is much harder to understand and harder to combine
    * flag values.
    */

   /**
    * Constant to indicate that the relationship set should NOT be filtered by
    * community. This flag is equivalent to using both 
    * {@link #FILTER_TYPE_ITEM_COMMUNITY} and 
    * {@link #FILTER_TYPE_FOLDER_COMMUNITY}. It overrides either of those flags.
    */
   public static final int FILTER_TYPE_COMMUNITY = 0x1<<0;

   /**
    * Constant to indicate that the relationship set should NOT be filtered
    * based on the folder permissions
    */
   public static final int FILTER_TYPE_FOLDER_PERMISSIONS = 0x1<<1;

   /**
    * Constant to indicate that the relationship set should NOT be filtered
    * by the item's community. Other objects may still be filtered by 
    * community, depending on the other filter type flags.
    * This flag is only meaningful if {@link #FILTER_TYPE_COMMUNITY} is not
    * present.
    */
   public static final int FILTER_TYPE_ITEM_COMMUNITY = 0x1<<2;

   /**
    * Constant to indicate that the relationship set should NOT be filtered
    * by the folder's community. Other objects may still be filtered by 
    * community, depending on the other filter type flags.
    * This flag is only meaningful if {@link #FILTER_TYPE_COMMUNITY} is not
    * present.
    */
   public static final int FILTER_TYPE_FOLDER_COMMUNITY = 0x1<<3;

   /**
    * The constant to indicate NOT to apply any filters.
    */
   public static final int FILTER_TYPE_NONE = FILTER_TYPE_COMMUNITY
         | FILTER_TYPE_FOLDER_PERMISSIONS | FILTER_TYPE_ITEM_COMMUNITY
         | FILTER_TYPE_ITEM_COMMUNITY;
   
   /**
    * Property of an effect indicating that it can be executed when processing
    * relationships in which the current item is the owner of the relationship.
    */
   public static final String ACTIVATION_ENDPOINT_OWNER = "owner";

   /**
    * Property of an effect indicating that it can be executed when processing
    * relationships in which the current item is the dependent of the
    * relationship.
    */
   public static final String ACTIVATION_ENDPOINT_DEPENDENT = "dependent";

   /**
    * Property of an effect indicating that it can be executed when processing
    * relationships in which the current item is the either owner or dependent
    * of the relationship.
    */
   public static final String ACTIVATION_ENDPOINT_EITHER = "either";

   /**
    * List of possible activation endpoints for a conditional effect for
    * convenience. Initialized in a static initializer and holds all
    * ACTIVATION_ENDPOINT_XXXX values defined in this class.
    */
   private static List<String> ms_activationEndPointList = new ArrayList<>();

   /**
    * Initialize the list of all possible activation end points for an effect.
    */
   static
   {
      ms_activationEndPointList.add(ACTIVATION_ENDPOINT_OWNER);
      ms_activationEndPointList.add(ACTIVATION_ENDPOINT_DEPENDENT);
      ms_activationEndPointList.add(ACTIVATION_ENDPOINT_EITHER);
   }

   /**
    * Check if the supplied activation point is a valid one.
    * @param activationEndPoint activation end point to validate, may be
    * <code>null</code> or empty.
    * @return <code>false</code> if the parameter is <code>null</code> or not
    * one of the ACTIVATION_ENDPOINT_XXXX values.
    */
   static public boolean isActivationEndPointValid(String activationEndPoint)
   {
      if(activationEndPoint == null ||
         !ms_activationEndPointList.contains(activationEndPoint))
         return false;
      return true;
   }

   /**
    * Gets the list of activation endpoints.
    *
    * @return the iterator over zero or more activation end points as String 
    * objects, never <code>null</code> or empty.
    */
   public static Iterator<String> getActivationEndPoints()
   {
      return ms_activationEndPointList.iterator();
   }

   /**
    * The relationship name, server-wide unique.  Initialized in ctor, may be
    * modified through calls to <code>setName(String)</code>, never <code>null
    * </code> or empty.
    * Note, it is protected so that the same code (of this class) can be reused
    * in the upgrade plugin.
    */
   protected String m_name = null;

   /**
    * The relationship label.  Initialized in ctor, may be modified through
    * calls to <code>setLabel(String)</code> and never <code>null</code> or
    * empty.
    */
   private String m_label = null;

   /**
    * The relationship category.  Initialized in ctor, may be modified through
    * calls to <code>setCategory(String)</code>. May be <code>null</code> or
    * empty.
    */
   private String m_category = null;

   /**
    * The relationship description. Initialized in ctor, may be modified through
    * calls to <code>setDescription(String)</code>. May be <code>null</code> or
    * empty.
    */
   private String m_description = "";

   /**
    * A collection of effects. Initialized in ctor, nerver changed after that.
    * Never <code>null</code>, might be empty.
    */
   private PSCollection m_effects = new PSCollection(PSConditionalEffect.class);

   /**
    * The list of system <code>PSProperty</code>s. Never
    * <code>null</code> may be empty. The property defs may change.
    */
   private PSPropertySet m_sysProps = new PSPropertySet();

   /**
    * This is used to cache the info in {@link #m_sysProps}, never 
    * <code>null</code>, but may be empty.
    */
   private HashMap<String, String> m_sysPropMap = new HashMap<String, String>();

   /**
    * The list of user <code>PSProperty</code>s. Never <code>null</code>, may
    * be empty. The list may be changed and the entries in the list may modify.
    */
   private PSPropertySet m_userProps = new PSPropertySet();

   /**
    * This is used to cache the info in {@link #m_userProps}, never 
    * <code>null</code>, but may be empty.
    */
   private HashMap<String, String> m_userPropMap= new HashMap<>();
   

   /**
    * The list of process checks that need to be performed for this
    * relationship, Initialized in ctor, might be empty. The list may be changed
    * and the entries in the list may modify.
    */
   private PSCollection m_processChecks =
      new PSCollection(PSProcessCheck.class);
   
   /**
    * Object representing the list of fileds for which the values need to be 
    * overridden for the cloned object when cloned based on this relationship. 
    * May be <code>null</code>. 
    */
   private PSCloneOverrideFieldList m_cloneOverrideFieldList = null;

   /**
    * The resource bundle to use to get description for system properties,
    * <code>null</code> until first call to <code>getBundle()</code> and never
    * modified after that.
    */
   private static ResourceBundle ms_resBundle = null;

   /**
    * The defaults for all defined system properties. These might be
    * overwritten by specific relationship configurations.
    */
   private static final List<PSProperty> ms_systemDefaults =
         new ArrayList<>();
   static
   {
      ms_systemDefaults.add(new PSProperty(RS_ALLOWCLONING,
         PSProperty.TYPE_BOOLEAN, Boolean.TRUE, false,
         getBundle().getString("sys_prop_alClone_desc")));

      ms_systemDefaults.add(new PSProperty(RS_ISLOCALDEPENDENCY,
         PSProperty.TYPE_BOOLEAN, Boolean.FALSE, false,
         getBundle().getString("sys_prop_isLocalDependency")));
      
      ms_systemDefaults.add(new PSProperty(RS_SKIPPROMOTION,
         PSProperty.TYPE_BOOLEAN, Boolean.FALSE, false,
         getBundle().getString("sys_prop_skippromotion")));

      ms_systemDefaults.add(new PSProperty(RS_USEDEPENDENTREVISION,
         PSProperty.TYPE_BOOLEAN, Boolean.FALSE, true,
         getBundle().getString("sys_prop_depRev_desc")));

      ms_systemDefaults.add(new PSProperty(RS_USEOWNERREVISION,
         PSProperty.TYPE_BOOLEAN, Boolean.TRUE, true,
         getBundle().getString("sys_prop_ownRev_desc")));

      ms_systemDefaults.add(new PSProperty(RS_USESERVERID,
         PSProperty.TYPE_BOOLEAN, Boolean.TRUE, false,
         getBundle().getString("sys_prop_useSvrID_desc")));
   }

   /**
    * A set of pre-defined user property names in the system.
    */
   private static final Set<String> ms_predefinedUserPropNames = new HashSet<>();
   
   /**
    * The defaults for new defined user properties. These might be
    * overwritten by specific relationship configurations.
    */
   private static final List<PSProperty> ms_userDefaults =
         new ArrayList<>();
   static
   {
      ms_userDefaults.add(new PSProperty(RS_INLINERELATIONSHIP,
         PSProperty.TYPE_STRING, null, false, null));
      
      ms_predefinedUserPropNames.add(PDU_SLOTID);
      ms_predefinedUserPropNames.add(PDU_SORTRANK);
      ms_predefinedUserPropNames.add(PDU_VARIANTID);
      ms_predefinedUserPropNames.add(PDU_FOLDERID);
      ms_predefinedUserPropNames.add(PDU_SITEID);
      ms_predefinedUserPropNames.add(PDU_INLINERELATIONSHIP);
      ms_predefinedUserPropNames.add(PDU_WIDGET_NAME);
   }
   
   
   /**
    * Type name for system relationships. System relationships are the ones 
    * that are shipped as part of the product. 
    */
   static public final String RS_TYPE_SYSTEM = "system";

   /**
    * Type name for user relationships. User relationships are the ones 
    * created by the CMS implementer.
    */
   static public final String RS_TYPE_USER = "user";
   
   /**
    * The type of relationship, represents whether it is 'System' or 'User'
    * relationship, default is 'User'.
    */
   private String m_type = RS_TYPE_USER;

   /*
    * The following strings define all elements/attributes used to parse/create
    * the XML for this object. No Java documentation will be added to this.
    */
   private static final String CLONEFIELD_OVERRIDE_LIST_ELEM = 
      PSCloneOverrideFieldList.XML_NODE_NAME;
   private static final String DESCRIPTION_ELEM = "Explanation";
   private static final String EFFECT_SET_ELEM = "EffectSet";
   private static final String USER_PROPERTY_SET_ELEM = "UserPropertySet";
   private static final String PROCESS_CHECKS_ELEM = "ProcessChecks";
   
   public static final String XML_ATTR_NAME = "name";
   public static final String XML_ATTR_TYPE = "type";
   public static final String XML_ATTR_LABEL = "label";
   public static final String XML_ATTR_CATEGORY = "category";
   public static final String XML_ATTR_PROPERTY = "property";
   public static final String XML_ATTR_OWNER = "owner";
   public static final String XML_ATTR_DEPENDENT = "dependent";
   
   /**
    * Process check name for clone shallow option.
    */
   public static final String PROC_CHECK_CLONE_SHALLOW = "rs_cloneshallow";

   /**
    * Process check name for clone deep option.
    */
   public static final String PROC_CHECK_CLONE_DEEP = "rs_clonedeep";
}
