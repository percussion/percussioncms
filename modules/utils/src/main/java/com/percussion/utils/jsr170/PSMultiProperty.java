/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.utils.jsr170;


import com.percussion.utils.beans.PSPropertyAccessException;
import com.percussion.utils.beans.PSPropertyWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.jcr.AccessDeniedException;
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
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Represents JSR-170 properties that can contain a set of values
 * 
 * @author dougrand
 */
public class PSMultiProperty extends PSPropertyWrapper 
   implements IPSJcrCacheItem, IPSProperty
{
   /**
    * The name of this property, never <code>null</code> after construction
    */
   public String m_name;

   /**
    * The values of the property, extracted at construction, could be
    * <code>null</code>
    */
   public Value m_value[];

   /**
    * The parent (containing) node for this property, never <code>null</code>
    * after construction
    */
   public Node m_parent;

   /**
    * This tracks how deep this item is in the overall tree. Set in the ctor.
    */
   public int m_depth;
   
   /**
    * Ctor for multi valued property
    * @param property property name, never <code>null</code> or empty
    * @param parent parent node, never <code>null</code>
    * @param rep the object to extract the value from,
    * never <code>null</code> but could conceivably be empty. The property
    * must reference a collection type bean property in the object
    * @throws PSPropertyAccessException
    * @throws RepositoryException 
    */
   @SuppressWarnings("unchecked")
   public PSMultiProperty(String property, Node parent, Object rep)
         throws PSPropertyAccessException, RepositoryException {
      super(rep);
      if (StringUtils.isBlank(property))
      {
         throw new IllegalArgumentException("pname may not be null or empty");
      }
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      } 
      m_depth = parent.getDepth() + 1;
      m_parent = parent;
      m_name = property;

      int colon = m_name.indexOf(':');
      String propname;
      if (colon >= 0)
      {
         propname = m_name.substring(colon+1);
      }
      else
      {
         propname = m_name;
      }
      Collection<Object> values = (Collection<Object>) super.getPropertyValue(propname);
      //Getting values.size() is a costly db call use a list so we only query for the
      // actual values.  values.iterator() is called in the for loop, this calls the db
      List<Value> valuesList = new ArrayList<>();

      for(Object value : values)
      {
         valuesList.add(PSValueFactory.createValue(value));
      }
      m_value = valuesList.toArray(new Value[0]);
   }

   public void setValue(Value arg0) throws VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(Value[] arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(String arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(String[] arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(InputStream arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(long arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(double arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(Calendar arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(boolean arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public void setValue(Node arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new RepositoryException("Set is not supported");
   }

   public Value getValue() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public Value[] getValues() throws ValueFormatException, RepositoryException
   {
      return m_value;
   }

   public String getString() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public InputStream getStream() throws ValueFormatException,
         RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public long getLong() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public double getDouble() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public Calendar getDate() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public boolean getBoolean() throws RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public Node getNode() throws RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public long getLength() throws RepositoryException
   {
      throw new ValueFormatException("This is a multi valued property");
   }

   public long[] getLengths() throws RepositoryException
   {
      long[] rval = new long[m_value.length];
      for(int i = 0; i < m_value.length; i++)
      {
         if (m_value[i] == null)
            rval[i] = 0;
         else if (m_value[i].getType() == PropertyType.BINARY)
         {
            rval[i] = -1;
         }
         else
         {
            rval[i] = m_value[i].getString().length();   
         }
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jcr.Property#getDefinition()
    */
   public PropertyDefinition getDefinition() throws RepositoryException
   {
      NodeType nodetype = validateParent(m_parent);

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

   public int getType() throws RepositoryException
   {
      if (m_value == null || m_value.length == 0)
      {
         return PropertyType.UNDEFINED;
      }
      else
      {
         return m_value[0].getType();
      }
   }

   public String getPath() throws RepositoryException
   {
      StringBuilder b = new StringBuilder();
      b.append(getParent().getPath());
      b.append("/");
      b.append(getName());

      return b.toString();
   }

   public String getName() throws RepositoryException
   {
      return m_name;
   }

   public Item getAncestor(int index) throws ItemNotFoundException,
         AccessDeniedException, RepositoryException
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

   public Node getParent() throws ItemNotFoundException, AccessDeniedException,
         RepositoryException
   {
      return m_parent;
   }

   public int getDepth() throws RepositoryException
   {
      return m_depth;
   }

   public Session getSession() throws RepositoryException
   {
      return null;
   }

   public boolean isNode()
   {
      return false;
   }

   public boolean isNew()
   {
      return false;
   }

   public boolean isModified()
   {
      return false;
   }

   public boolean isSame(Item arg0) throws RepositoryException
   {
      return equals(arg0);
   }

   public void accept(ItemVisitor arg0) throws RepositoryException
   {
      arg0.visit(this);
   }

   public void save() throws RepositoryException
   {
      throw new RepositoryException("Save is not supported");
   }

   public void refresh(boolean arg0) throws RepositoryException
   {
      throw new RepositoryException("Refresh is not supported");
   }

   public void remove() throws RepositoryException
   {
      throw new RepositoryException("Remove is not supported");
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return new ToStringBuilder(this).append("name", m_name).append("value",
            m_value).toString();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSMultiProperty)) return false;
      
      PSMultiProperty b = (PSMultiProperty) obj;
      return new EqualsBuilder().append(m_depth, b.m_depth).append(m_name,
            b.m_name).append(m_value, b.m_value).isEquals()
            && (m_parent == b.m_parent);
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

   public long getSizeInBytes() throws RepositoryException {
      if (m_value == null) return 0;
      
      long size = 0;
      for(Value v : m_value)
      {
         if (v != null)
         {
            if (v instanceof IPSJcrCacheItem)
            {
               size += ((IPSJcrCacheItem) v).getSizeInBytes();
            }
         }
      }
      return size;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.utils.jsr170.IPSProperty#isNull()
    */
   public boolean isNull()
   {
      return m_value == null;
   }

}
