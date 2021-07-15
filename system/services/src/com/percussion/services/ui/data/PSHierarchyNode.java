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
package com.percussion.services.ui.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * This object represents a single workbench hierarchy node.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSHierarchyNode")
@Table(name = "PSX_WB_HIERARCHY_NODE")
public class PSHierarchyNode implements Serializable, IPSCatalogSummary, 
   IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -5425086083061018906L;
   
   /**
    * Full guid value.
    */
   @Id
   @Column(name = "NODE_ID", nullable = false)
   private long id;

   @Version
   @Column(name = "VERSION")
   private Integer version;

   /**
    * Full guid value.
    */
   @Basic
   @Column(name = "PARENT_ID", nullable = true)
   private long parentId;

   @Basic
   @Column(name = "NAME", nullable = false, length = 250)
   private String name;

   @Basic
   @Column(name = "TYPE", nullable = false)
   private int type;
   
   @Transient
   private Map<String, String> properties = new HashMap<>();

   /**
    * This ctor is meant for serialization only. It should not be used by 
    * clients.
    */
   public PSHierarchyNode()
   {
   }
   
   /**
    * This enum identifies how the node is used.
    *
    * @author paulhoward
    */
   public enum NodeType
   {
      /**
       * This node is a folder, meaning it can contain other nodes.
       */
      FOLDER(1),
      
      /**
       * This node is used to indicate where some other object should be placed
       * in the hierarchy. It is not a container.
       */
      PLACEHOLDER(2);
      
      /**
       * Lookup enum value by ordinal. Ordinals should be unique. If they are
       * not unique, then the first enum value with a matching ordinal is
       * returned.
       * 
       * @param s ordinal value
       * @return an enumerated value or <code>null</code> if the ordinal does
       * not match
       */
      public static NodeType valueOf(int s)
      {
         NodeType types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].getOrdinal() == s)
               return types[i];
         }
         return null;
      }

      /**
       * We want to have our own assigned numbers, so we pass that in to the
       * ctor for each enum.
       * 
       * @param ordinal Any value is OK.
       */
      private NodeType(int ordinal)
      {
         m_value = ordinal;
      }
      
      /**
       * Returns the value supplied in the ctor.
       * @return See description.
       */
      public int getOrdinal()
      {
         return m_value;
      }
      
      /**
       * See ctor.
       */
      private final int m_value;
   }
   
   /**
    * This is not for client use. It should only be used by the betwixt
    * serialization mechanism. When betwixt supports enums, this could be
    * removed.
    * 
    * @return Equivalent to {@link #getType()}.ordinal().
    */
   @IPSXmlSerialization(suppress=true)
   public int getTypeInt()
   {
      return type;
   }
   
   /**
    * This is not for client use. It should only be used by the betwixt
    * serialization mechanism. When betwixt supports enums, this could be
    * removed.
    * 
    * @param nodeType Like {@link #setType(NodeType)}, but takes the ordinal of
    * the enum.
    */
   public void setTypeInt(int nodeType)
   {
      type = nodeType;
   }
   
   /**
    * Create a valid node.
    * 
    * @param nodeName See {@link #setName(String)}.
    * @param nodeGuid See {@link #setGUID(IPSGuid)}.
    * @param nodeType See {@link #setType(NodeType)}.
    */
   public PSHierarchyNode(String nodeName, IPSGuid nodeGuid, NodeType nodeType)
   {
      // depend on setters for validation
      setName(nodeName);
      setGUID(nodeGuid);
      setType(nodeType);
   }
   
   /**
    * Get the object version.
    * 
    * @return the object version, <code>null</code> if not initialized yet.
    */
   public Integer getVersion()
   {
      return version;
   }
   
   /**
    * Set the object version. The version can only be set once in the life 
    * cycle of this object. 
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null)
         throw new IllegalStateException(
            "version can only be initialized once");
      
      if (version < 0)
         throw new IllegalArgumentException("version must be >= 0");
      
      this.version = version;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set a new name for this hierarchy node. A node cannot have the same name
    * as a sibling, case-insensitive. This check is performed by the service,
    * not this class.
    * 
    * @param nodeName the new name, not <code>null</code> or empty.
    */
   public void setName(String nodeName)
   {
      if (StringUtils.isBlank(nodeName))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      this.name = nodeName;
   }
   
   /* (non-Javadoc)
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }
   
   /* (non-Javadoc)
    * @see IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSDesignGuid(id);
   }
   
   /**
    * The id of the parent of this hierarchy node, <code>null</code> if this 
    * hierarchy node does not have a parent.
    * 
    * @return the id of the parent hierarchy node, <code>null</code> if this 
    *    is a root node.
    */
   public IPSGuid getParentId()
   {
      return parentId == 0 ? null : new PSDesignGuid(parentId);
   }
   
   /**
    * Get the type of this hierarchy node.
    * 
    * @return the hierarchy node type.
    */
   public NodeType getType()
   {
      return NodeType.valueOf(type);
   }
   
   /**
    * Set a new hierarchy node type. This is meant for use w/ the default ctor.
    * If called otherwise, an exception is thrown.
    * 
    * @param nt The new hierarchy node type, not <code>null</code>.
    * @throws IllegalStateException If called more than once.
    */
   public void setType(NodeType nt)
   {
      if (type != 0)
         throw new IllegalStateException("Can only set the type once.");
      
      if (nt == null)
         throw new IllegalArgumentException("nt cannot be null");

      type = nt.getOrdinal();
   }
   
   /**
    * Set the id of the new parent hierarchy node.
    * 
    * @param parent the id of the new hierarchy parent, <code>null</code> to
    *    make this a root node.
    */
   public void setParentId(IPSGuid parent)
   {
      if (null == parent)
         parentId = 0;
      else
         parentId = new PSDesignGuid(parent).getValue();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (id != 0)
         throw new IllegalStateException("cannot change existing guid");

      id = new PSDesignGuid(newguid).getValue();
   }
   
   /**
    * Get all hierarchy node properties.
    * 
    * @return the hierarchy node properties, never <code>null</code>, may
    *    be empty.
    */
   public Map<String, String> getProperties()
   {
      return properties;
   }
   
   /**
    * Set new hierarchy node properties.
    * 
    * @param properties the new hierarchy node properties, may be
    *    <code>null</code> or empty. All supplied properties must have a valid
    *    id. All properties will be attached to this node.
    */
   public void setProperties(Map<String, String> properties)
   {
      if (properties == null)
         this.properties.clear();
      else
         this.properties = properties;
   }
   
   /**
    * Adds a new hierarchy node property or replaces an existing one. 
    * 
    * @param propertyName never <code>null</code> or empty.
    * @param value may be <code>null</code> or empty.
    */
   public void addProperty(String propertyName, String value)
   {
      if (StringUtils.isBlank(propertyName))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      properties.put(propertyName, value);
   }
   
   /**
    * Remove the property for the specified name.
    * 
    * @param propertyName the name or the property to be removed, may be 
    *    <code>null</code> or empty. Nothing is done if the referenced property
    *    does not exist.
    */
   public void removeProperty(String propertyName)
   {
      if (!StringUtils.isBlank(propertyName))
         properties.remove(propertyName);
   }
   
   /**
    * Get the property for the specified name.
    * 
    * @param propertyName the name of the property to get, may be <code>null</code> or
    *    empty.
    * @return the property for the specified name, <code>null</code> if not
    *    found.
    */
   public String getProperty(String propertyName)
   {
      if (StringUtils.isBlank(propertyName))
         return null;
      
      return properties.get(propertyName);
   }

   
   
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + (int) (parentId ^ (parentId >>> 32));
      result = prime * result + type;
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSHierarchyNode other = (PSHierarchyNode) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      if (parentId != other.parentId)
         return false;
      if (type != other.type)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

