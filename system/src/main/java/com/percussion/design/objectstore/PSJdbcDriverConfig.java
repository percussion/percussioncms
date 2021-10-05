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

import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Object representation of a JDBC driver configuration.  Provides default 
 * values to supply to a JNDI Datasource configuration based on a selected
 * driver name.
 */
public class PSJdbcDriverConfig extends PSComponent
{
   /**
    * Constant of root element name to use for XML serialization.
    */
   public static final String XML_NODE_NAME = "PSXJdbcDriverConfig";
   
   /**
    * Construct this object from it's XML representation.  See 
    * {@link #toXml(Document)} for the expected format.
    * 
    * @param src The source element, may not be <code>null</code>.
    * @throws PSUnknownNodeTypeException 
    */
   public PSJdbcDriverConfig(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      fromXml(src, null, null);
   }

   /**
    * Construct this object from its properties.
    * 
    * @param driverName The name of the JDBC driver to configure, may not be
    * <code>null</code> or empty.
    * @param className The name of the class to use, may not be
    * <code>null</code> or empty.
    * @param containerTypeMapping The default JBoss container type mapping to
    * use for this driver. See {@link #getContainerTypeMapping()} for more info.
    */
   public PSJdbcDriverConfig(String driverName, String className,
      String containerTypeMapping)
   {
      if (StringUtils.isBlank(driverName))
         throw new IllegalArgumentException(
            "driverName may not be null or empty");
      
      if (StringUtils.isBlank(className))
         throw new IllegalArgumentException(
            "className may not be null or empty");
      
      if (StringUtils.isBlank(containerTypeMapping))
         throw new IllegalArgumentException(
            "containerTypeMapping may not be null or empty");
      
      m_driverName = driverName;
      m_className = className;
      m_containerTypeMapping = containerTypeMapping;
   }

   /**
    * Get the name of the driver this configuration represents.
    * 
    * @return The dirver name, never <code>null</code> or empty.
    */
   public String getDriverName()
   {
      return m_driverName;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * 
    * @param obj a valid PSComponent. Cannot be <code>null</code> and must be
    * an instance of <code>PSJdbcDriverConfig</code>
    */
   @Override
   public void copyFrom(PSComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
      
      if (!(obj instanceof PSJdbcDriverConfig))
         throw new IllegalArgumentException(
            "obj must be instance of PSJdbcDriverConfig");
      
      PSJdbcDriverConfig other = (PSJdbcDriverConfig) obj;
      m_className = other.m_className;
      m_driverName = other.m_driverName;
      m_containerTypeMapping = other.m_containerTypeMapping;
   }

   /**
    * see interface for description
    */
   @Override
   public Object clone()
   {
      return super.clone();
   }

   /**
    * Restores this object from it's XML configuration. 
    * 
    * @param sourceNode The source XML node, may not be <code>null</code>.  See
    * {@link #toXml(Document)} for the expected format.
    * @param parentDoc Ignored.
    * @param parentComponents Ignored.
    * 
    * @throws PSUnknownNodeTypeException If the supplied element is not in the
    * expected format.
    */
   public void fromXml(Element sourceNode,
         @SuppressWarnings("unused") IPSDocument parentDoc,
         @SuppressWarnings("unused") ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      
      PSXMLDomUtil.checkNode(sourceNode, XML_NODE_NAME);
      
      m_driverName = PSXMLDomUtil.checkAttribute(sourceNode, DRIVER_NAME_ATTR, 
         true);
      m_className = PSXMLDomUtil.checkAttribute(sourceNode, CLASS_NAME_ATTR, 
         true);
      m_containerTypeMapping = PSXMLDomUtil.checkAttribute(sourceNode, 
         TYPE_MAPPING_ATTR, true);      
   }
   

   /**
    * Sets the name of the driver this configuration represents.
    * 
    * @param driverName The name, may not be <code>null</code> or empty.
    */
   public void setDriverName(String driverName)
   {
      if (StringUtils.isBlank(driverName))
         throw new IllegalArgumentException(
            "driverName may not be null or empty");
      
      m_driverName = driverName;
   }

   /**
    * Get the name of the class to use for this driver.
    * 
    * @return The class name, never <code>null</code> or empty.
    */
   public String getClassName()
   {
      return m_className;
   }

   /**
    * Set the name of the class to use for this driver.
    * @param className The classname, may not be <code>null</code> or empty.
    */
   public void setClassName(String className)
   {
      if (StringUtils.isBlank(className))
         throw new IllegalArgumentException(
            "className may not be null or empty");
      
      m_className = className;
   }

   /**
    * Get the default container type mapping for this driver.  See {@link 
    * com.percussion.utils.jboss.PSJndiDatasource#getContainerTypeMapping() 
    * PSJndiDatasource.getContainerTypeMapping()} for more info.
    * 
    * @return The type mapping, never <code>null</code> or empty.
    */
   public String getContainerTypeMapping()
   {
      return m_containerTypeMapping;
   }

   /**
    * Set the default container type mapping for this driver.  See 
    * {@link #getContainerTypeMapping()} for details.
    *  
    * @param typeMapping The type mapping, never <code>null</code> or empty.
    */
   public void setContainerTypeMapping(String typeMapping)
   {
      if (StringUtils.isBlank(typeMapping))
         throw new IllegalArgumentException(
            "typeMapping may not be null or empty");
      
      m_containerTypeMapping = typeMapping;
   }

   /**
    * Serializes this class from its XML representation.
    * @param doc The source element, never <code>null</code>.  The expected 
    * format is:
    * 
    * <pre>
    * <code>
    * &lt;!ELEMENT PSXJdbcDriverConfig (EMPTY)>
    * &lt;!ATTRLIST PSXJdbcDriverConfig
    *    driverName CDATA #REQUIRED
    *    className CDATA #REQUIRED
    *    containerTypeMapping CDATA #REQUIRED 
    * >
    * </code>
    * </pre>
    * 
    * @return The element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(DRIVER_NAME_ATTR, m_driverName);
      root.setAttribute(CLASS_NAME_ATTR, m_className);
      root.setAttribute(TYPE_MAPPING_ATTR, m_containerTypeMapping);
      
      return root;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSJdbcDriverConfig))
      {
         return false;
      }
      if (obj == this)
      {
         return true;
      }
      final PSJdbcDriverConfig config = (PSJdbcDriverConfig) obj;
      return new EqualsBuilder()
            .append(m_id, config.m_id)
            .append(m_className, config.m_className)
            .append(m_driverName, config.m_driverName)
            .append(m_containerTypeMapping, config.m_containerTypeMapping)
            .isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_id)
            .append(m_className)
            .append(m_driverName)
            .append(m_containerTypeMapping)
            .toHashCode();
   }
   
   @Override
   public String toString()
   {
      return m_driverName;
   }   
   
   /**
    * The name of the class never <code>null</code> or empty.  
    * See {@link #getClassName()}.
    */
   private String m_className;
   
   /**
    * The container type mapping, never <code>null</code> or empty. See
    * {@link #getContainerTypeMapping()}.
    */
   private String m_containerTypeMapping;
   
   /**
    * The driver name, never <code>null</code> or empty.  See 
    * {@link #getDriverName()}.
    */
   private String m_driverName;

   // private XML constants
   private static final String DRIVER_NAME_ATTR = "driverName";
   private static final String CLASS_NAME_ATTR = "className";
   private static final String TYPE_MAPPING_ATTR = "containerTypeMapping";
}
