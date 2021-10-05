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
package com.percussion.search.lucene;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSFieldRetriever;
import com.percussion.search.PSSearchAdmin;
import com.percussion.search.PSSearchException;

import java.io.IOException;
import java.util.Set;

public class PSSearchAdminReadOnly extends PSSearchAdmin
{

   /**
    * The only ctor.
    * @param realOne The admin that contains the real configuration. Never
    * <code>null</code>.
    */
   public PSSearchAdminReadOnly(PSSearchAdmin realOne)
   {
      if (null == realOne)
      {
         throw new IllegalArgumentException("search admin cannot be null");
      }
      m_admin = realOne;
   }

   //see base class
   public synchronized PSFieldRetriever getFieldRetriever(PSKey key)
      throws PSInvalidContentTypeException
   {
      return m_admin.getFieldRetriever(key);
   }

   /**
    * Always throws IllegalStateException.
    */
   public void save() throws IOException
   {
      throw new IllegalStateException(
            "The admin is not locked and cannot be modified.");
   }

   /**
    * Always throws IllegalStateException.
    */   
   public void verify(Set<Long> knownContentTypes) throws PSSearchException
   {
      // This implementation should never be called as it 
      // is not active. Complain!
      throw new IllegalStateException(
            "The read-only admin object does not support verify.");      
   }

   /**
    * Always throws IllegalStateException.
    */
   public PSKey[] optimizeIndexes(PSKey[] contentTypes) throws PSSearchException
   {
      throw new IllegalStateException(
            "The read-only admin object does not support index modification.");      
   }

   /**
    * Always throws IllegalStateException.
    */
   public PSKey[] rebuildIndexes(PSKey[] contentTypes) throws PSSearchException
   {
      throw new IllegalStateException(
            "The read-only admin object does not support index modification.");      
   }

   /**
    * Always throws IllegalStateException.
    */
   protected void doDelete(PSKey key)
   {
      throw new IllegalStateException(
            "The admin is not locked and cannot be modified.");
   }

   /**
    * Always throws IllegalStateException.
    */
   protected boolean update(PSItemDefinition def, boolean notify)
   {
      throw new IllegalStateException(
            "The admin is not locked and cannot be modified.");
   }

   @Override
   public void clearIndexLocks()
   {
      throw new IllegalStateException(
            "The read-only admin object does not support clear index locks " +
            "operation.");
   }

   /**
    * The admin to which all supported (read-only) methods are delegated.
    * Set in ctor, then never <code>null</code> or modified.
    */
   private PSSearchAdmin m_admin;

}
