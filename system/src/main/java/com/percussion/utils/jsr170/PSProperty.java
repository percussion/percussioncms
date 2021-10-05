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
package com.percussion.utils.jsr170;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.beans.IPSPropertyLoader;
import com.percussion.utils.beans.PSPropertyWrapper;
import com.percussion.utils.io.PSReaderInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Represents a single property for a node. The actual data is held in loaded
 * instances where each instance holds multiple values. The mappings between the
 * fields and the instances is part of the
 * {@link com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration}.
 */
@SuppressWarnings("unused")
public class PSProperty extends PSPropertyWrapper 
   implements IPSProperty, IPSJcrCacheItem
{
   private static final Logger log = LogManager.getLogger(PSProperty.class);

   private static final String SET_NOT_SUPPORTED="Set is not supported";
   /**
    * The name of this property, never <code>null</code> after construction
    */
   protected String m_name;

   /**
    * The value of the property, extracted at initialization, could be
    * <code>null</code>
    */
   protected Object m_value;
   
   /**
    * Cached string value for Clobs.
    */
   protected String m_strValue = null;

   /**
    * The name of the bean property, normally the same as {@link #m_name}. May
    * be <code>null</code>.
    */
   protected String m_beanPropertyName = null;

   /**
    * The parent (containing) node for this property, never <code>null</code>
    * after construction
    */
   protected Node m_parent;

   /**
    * This tracks how deep this item is in the overall tree. Set in the ctor.
    */
   protected int m_depth;

   /**
    * If this is not <code>null</code>, the listed interceptors are run,
    * first to last, when accessing the property for the first time, i.e. during
    * initialization.
    */
   protected List<IPSPropertyInterceptor> m_interceptors = null;

   /**
    * Ctor
    * 
    * @param pname the name of this property, never <code>null</code> or
    *           empty, and must correspond to the underlying property used in
    *           the mapping object
    * @param parent the containing node of this property, never
    *           <code>null</code>
    * @param loader the underlying mapping object instance, never
    *           <code>null</code>, checked in superclass
    * @throws RepositoryException
    */
   public PSProperty(String pname, Node parent, IPSPropertyLoader loader)
         throws RepositoryException {
      super(loader);

      if (StringUtils.isBlank(pname))
      {
         throw new IllegalArgumentException("pname may not be null or empty");
      }
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      }
      m_name = pname;
      m_parent = parent;
      m_depth = parent.getDepth() + 1;
   }

   /**
    * Ctor
    * 
    * @param pname the name of this property, never <code>null</code> or
    *           empty, and must correspond to the underlying property used in
    *           the mapping object
    * @param parent the containing node of this property, never
    *           <code>null</code>
    * @param instance the underlying mapping object instance, never
    *           <code>null</code>
    * @param beanPropertyName optional, may be <code>null</code>, if present,
    *           this is used instead of the pname in accessing the underlying
    *           bean property
    * @throws RepositoryException
    */
   public PSProperty(String pname, Node parent, Object instance,
         String beanPropertyName) throws RepositoryException {
      super(instance);

      if (StringUtils.isBlank(pname))
      {
         throw new IllegalArgumentException("pname may not be null or empty");
      }
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      }
      m_name = pname;
      m_beanPropertyName = beanPropertyName;
      m_parent = parent;
      m_depth = parent.getDepth() + 1;
   }

   @Override
   public synchronized void init()
   {
      if (m_initialized)
         return;

      super.init(); // Sets initialized

      if (m_beanPropertyName != null)
         m_value = getPropertyValue(m_beanPropertyName);
      else
      {
         int colon = m_name.indexOf(':');
         String propname;
         if (colon >= 0)
         {
            propname = m_name.substring(colon + 1);
         }
         else
         {
            propname = m_name;
         }
         m_value = getPropertyValue(propname);
      }

      if (m_interceptors != null)
      {
         try
         {
            for (IPSPropertyInterceptor intercept : m_interceptors)
            {
               m_value = intercept.translate(getString());
            }
         }
         catch (Exception e)
         {

            log.warn(
                  "Setting property to null as the interceptor threw an error: {}",
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            m_value = null;
         }
      }
   }

   /**
    * Create explicit property, this method is intended primarily for testing.
    * If used otherwise, it would be for artificial properties that are, for
    * example, calculated and have no direct existence in the repository.
    * 
    * @param pname the name of the property, never <code>null</code> or empty
    * @param parent the parent node, never <code>null</code>
    * @param value the value, may be <code>null</code>.
    * @throws RepositoryException
    */
   public PSProperty(String pname, Node parent, Object value)
         throws RepositoryException {
         super(parent);

      if (StringUtils.isBlank(pname))
      {
         throw new IllegalArgumentException("pname may not be null or empty");
      }
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      }

      m_depth = parent.getDepth() + 1;
      m_parent = parent;
      m_value = value;
      m_name = pname;
      m_initialized = true;

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(javax.jcr.Value)
    */
   public void setValue(Value arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(javax.jcr.Value[])
    */
   public void setValue(Value[] arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(java.lang.String)
    */
   public void setValue(String arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(java.lang.String[])
    */
   public void setValue(String[] arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(java.io.InputStream)
    */
   public void setValue(InputStream arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(long)
    */
   public void setValue(long arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(double)
    */
   public void setValue(double arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(java.util.Calendar)
    */
   public void setValue(Calendar arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(boolean)
    */
   public void setValue(boolean arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#setValue(javax.jcr.Node)
    */
   public void setValue(Node arg0) throws RepositoryException
   {
      throw new RepositoryException(SET_NOT_SUPPORTED);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getValue()
    */
   public Value getValue() throws RepositoryException
   {
      init();
      return PSValueFactory.createValue(m_value);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getValues()
    */
   @JsonIgnore
   public Value[] getValues() throws RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getString()
    */
   public String getString() throws RepositoryException
   {
      init();

      if (m_value == null)
         return "";
      else if (m_value instanceof Clob)
      {
         if (m_strValue == null)
         {
            try
            {
               Clob clob = (Clob) m_value;
               try(StringWriter w = new StringWriter((int) clob.length())) {
                  try (Reader r = clob.getCharacterStream()) {
                     IOUtils.copy(r, w);
                     m_strValue = w.toString();
                  }
               }
            }
            catch (Exception e)
            {
               throw new RepositoryException(
                     "Could not extract string for property " + m_name, e);
            }
         }
         return m_strValue;
      }
      else if (m_value instanceof Blob)
      {
         if (m_strValue == null)
         {
            try
            {
               Blob blob = (Blob) m_value;
               try(StringWriter w = new StringWriter((int) blob.length() * 2)) {
                  try (Reader r = new InputStreamReader(blob.getBinaryStream(), StandardCharsets.UTF_8)) {
                     IOUtils.copy(r, w);
                     m_strValue = w.toString();
                  }
               }
            }
            catch (Exception e)
            {
               throw new RepositoryException(
                     "Could not extract string for property " + m_name, e);
            }
         }
         return m_strValue;
      }
      else
         return m_value.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getStream()
    */
   public InputStream getStream() throws
         RepositoryException
   {
      init();

      try
      {
         if (m_value instanceof byte[]) {
            try (ByteArrayInputStream bs = new ByteArrayInputStream((byte[]) m_value)) {
               return bs;
            }
         }else if (m_value instanceof Clob)
         {
            try(InputStream ism = new PSReaderInputStream(((Clob) m_value)
                     .getCharacterStream())){
               return ism;
            }
         }
         else if (m_value instanceof Blob) {
            try (InputStream ism = ((Blob) m_value).getBinaryStream()) {
               return ism;
            }
         }else {
            try(InputStream ism = PSValueConverter.convertToStream(getString())){
               return ism;
            }
         }
      }
      catch (SQLException| IOException e)
      {
         throw new RepositoryException("Couldn't retrieve LOB value", e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getLong()
    */
   public long getLong() throws RepositoryException
   {
      init();

      if (m_value == null || m_value=="")
         return 0;
      else if (m_value instanceof Long)
         return (Long) m_value;
      else
      {
         try
         {
            return Long.parseLong(m_value.toString());
         }
         catch (NumberFormatException e)
         {
            throw new ValueFormatException(e);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getDouble()
    */
   public double getDouble() throws RepositoryException
   {
      init();

      if (m_value == null || m_value=="")
         return 0;
      else if (m_value instanceof Double)
         return (Double) m_value;
      else
      {
         try
         {
            return Double.parseDouble(m_value.toString());
         }
         catch (NumberFormatException e)
         {
            throw new ValueFormatException(e);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getDate()
    */
   public Calendar getDate() throws RepositoryException
   {
      init();

      if (m_value == null || m_value=="")
         return null;
      else if (m_value instanceof Calendar)
         return (Calendar) m_value;
      else if (m_value instanceof Timestamp)
      {
         Calendar cal = new GregorianCalendar();
         cal.setTimeInMillis(((Timestamp) m_value).getTime());
         return cal;
      }
      else if (m_value instanceof Date)
      {
         Calendar cal = new GregorianCalendar();
         cal.setTimeInMillis(((Date) m_value).getTime());
         return cal;
      }
      else
      {
         return PSValueConverter.convertToCalendar(m_value.toString());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getBoolean()
    */
   public boolean getBoolean() throws RepositoryException
   {
      init();

      if (m_value == null || m_value == "")
         return false;
      else if (m_value instanceof Boolean)
         return (Boolean) m_value;
      else
      {
         return Boolean.parseBoolean(m_value.toString());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getNode()
    */
   public Node getNode() throws RepositoryException
   {
      init();

      if (m_value instanceof Node)
         return (Node) m_value;
      else
         return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getLength()
    */
   public long getLength() throws RepositoryException
   {
      init();

      if (m_value == null)
         return 0;
      else if (m_value instanceof byte[])
      {
         return ((byte[]) m_value).length;
      }
      else if (m_value instanceof Clob)
      {
         try
         {
            return ((Clob) m_value).length();
         }
         catch (SQLException e)
         {
            throw new RepositoryException("Couldn't determine length", e);
         }
      }
      else if (m_value instanceof Blob)
      {
         try
         {
            return ((Blob) m_value).length();
         }
         catch (SQLException e)
         {
            throw new RepositoryException("Couldn't determine length", e);
         }         
      }
      else
      {
         String str = getString();
         return str.length();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getLengths()
    */
    @JsonIgnore
   public long[] getLengths() throws RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getDefinition()
    */
   public PropertyDefinition getDefinition() throws RepositoryException
   {
      if (m_parent == null || m_parent.getDefinition() == null
            || m_parent.getDefinition().getDeclaringNodeType() == null)
      {
         throw new IllegalStateException(
               "Missing parent, parent definition or nodetype information");
      }
      NodeType nodetype = m_parent.getDefinition().getDeclaringNodeType();
      if (nodetype == null)
      {
         throw new IllegalStateException("Missing nodetype information");
      }
      PropertyDefinition[] defs = nodetype.getPropertyDefinitions();
      for (PropertyDefinition def : defs)
      {
         if (m_name.equals(def.getName()))
         {
            return def;
         }
      }
      throw new PathNotFoundException("Can't find definition for property: "
            + m_name);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getType()
    */
   public int getType() throws RepositoryException
   {
      init();

      // Get the value and extract the type... the future will
      // have more here
      Value v = PSValueFactory.createValue(m_value);
      return v != null ? v.getType() : PropertyType.UNDEFINED;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getPath()
    */
   public String getPath() throws RepositoryException
   {
      return getParent().getPath() +
              "/" +
              getName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getName()
    */
   public String getName() throws RepositoryException
   {
      return m_name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getAncestor(int)
    */
   public Item getAncestor(int index) throws RepositoryException
   {
      if (m_depth < index)
      {
         throw new ItemNotFoundException(
               "Can't have an ancestor below this node's depth");
      }
      if (m_depth == index)
      {
         return this;
      }
      else
      {
         return m_parent.getAncestor(index);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getParent()
    */
   public Node getParent() throws RepositoryException
   {
      return m_parent;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getDepth()
    */
   public int getDepth()
   {
      return m_depth;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#getSession()
    */
   public Session getSession() throws RepositoryException
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#isNode()
    */
   public boolean isNode()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#isNew()
    */
   public boolean isNew()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#isModified()
    */
   public boolean isModified()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#isSame(javax.jcr.Item)
    */
   public boolean isSame(Item arg0)
   {
      return equals(arg0);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#accept(javax.jcr.ItemVisitor)
    */
   public void accept(ItemVisitor visitor) throws RepositoryException
   {
      visitor.visit(this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#save()
    */
   public void save() throws RepositoryException
   {
      throw new RepositoryException("Save not implemented");
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#refresh(boolean)
    */
   public void refresh(boolean arg0) throws
         RepositoryException
   {
      throw new RepositoryException("Refresh not implemented");
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Item#remove()
    */
   public void remove() throws
         RepositoryException
   {
      throw new RepositoryException("Remove not implemented");
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return new ToStringBuilder(this).append("name", m_name).append(
            "value", m_value).append("initialized", m_initialized)
            .toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSProperty)) return false;
      
      PSProperty b = (PSProperty) obj;
      return new EqualsBuilder().append(m_depth, b.m_depth).append(m_name,
            b.m_name).append(m_value, b.m_value).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_depth).append(m_name).append(
            m_value).toHashCode();
   }

   /**
    * Add a property interceptor
    * 
    * @param interceptor the interceptor, never <code>null</code>
    */
   public void addInterceptor(IPSPropertyInterceptor interceptor)
   {
      if (interceptor == null)
      {
         throw new IllegalArgumentException("interceptor may not be null");
      }
      if (m_interceptors == null)
      {
         m_interceptors = new ArrayList<>();
      }
      m_interceptors.add(interceptor);
   }

   public long getSizeInBytes() throws RepositoryException {
      try
      {
         if (m_value == null)
            return 0;
         else if (m_value instanceof byte[])
            return ((byte[]) m_value).length;
         else if (m_value instanceof String)
            return ((String) m_value).length() * 4;
         else if (m_value instanceof Clob)
            return ((Clob) m_value).length() * 4;
         else if (m_value instanceof Blob)
            return ((Blob) m_value).length();
         else 
            return 0;
      }
      catch (SQLException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
         throw new RepositoryException(e);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.utils.jsr170.IPSProperty#isNull()
    */
   public boolean isNull()
   {
      init();
      return m_value == null;
   }
}
