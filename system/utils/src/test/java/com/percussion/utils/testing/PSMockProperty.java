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
package com.percussion.utils.testing;

import com.percussion.utils.jsr170.PSValueFactory;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

public class PSMockProperty implements Property
{
   private Value m_value = null;

   public void setValue(Value arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = arg0;
   }

   public void setValue(Value[] arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   public void setValue(String arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(String[] arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   public void setValue(InputStream arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(long arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(double arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(Calendar arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(boolean arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public void setValue(Node arg0) throws ValueFormatException,
         VersionException, LockException, ConstraintViolationException,
         RepositoryException
   {
      m_value = PSValueFactory.createValue((Object) arg0);
   }

   public Value getValue() throws ValueFormatException, RepositoryException
   {
      return m_value;
   }

   public Value[] getValues() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   public String getString() throws ValueFormatException, RepositoryException
   {
      return m_value.getString();
   }

   public InputStream getStream() throws ValueFormatException,
         RepositoryException
   {
      return m_value.getStream();
   }

   public long getLong() throws ValueFormatException, RepositoryException
   {
      return m_value.getLong();
   }

   public double getDouble() throws ValueFormatException, RepositoryException
   {
      return m_value.getDouble();
   }

   public Calendar getDate() throws ValueFormatException, RepositoryException
   {
      return m_value.getDate();
   }

   public boolean getBoolean() throws ValueFormatException, RepositoryException
   {
      return m_value.getBoolean();
   }

   public Node getNode() throws ValueFormatException, RepositoryException
   {
      throw new RepositoryException("Not supported: getNode()");
   }

   public long getLength() throws ValueFormatException, RepositoryException
   {
      return 0;
   }

   public long[] getLengths() throws ValueFormatException, RepositoryException
   {
      throw new ValueFormatException("This is a single valued property");
   }

   public PropertyDefinition getDefinition() throws RepositoryException
   {
      return null;
   }

   public int getType() throws RepositoryException
   {
      return 0;
   }

   public String getPath() throws RepositoryException
   {
      return null;
   }

   public String getName() throws RepositoryException
   {
      return null;
   }

   public Item getAncestor(int arg0) throws ItemNotFoundException,
         AccessDeniedException, RepositoryException
   {
      return null;
   }

   public Node getParent() throws ItemNotFoundException, AccessDeniedException,
         RepositoryException
   {
      return null;
   }

   public int getDepth() throws RepositoryException
   {
      return 0;
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
      arg0.visit((Property) this);
   }

   public void save() throws AccessDeniedException, ItemExistsException,
         ConstraintViolationException, InvalidItemStateException,
         ReferentialIntegrityException, VersionException, LockException,
         NoSuchNodeTypeException, RepositoryException
   {
      throw new RepositoryException("Not supported: save()");
   }

   public void refresh(boolean arg0) throws InvalidItemStateException,
         RepositoryException
   {
      throw new RepositoryException("Not supported: refresh()");
   }

   public void remove() throws VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new RepositoryException("Not supported: remove()");
   }

}
