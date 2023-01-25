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
