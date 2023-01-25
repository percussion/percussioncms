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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.IPSItemDefChangeListener;
import com.percussion.error.PSException;

import java.util.ArrayList;
import java.util.List;

/**
 * The listener for the legacy content repository
 * 
 * @author dougrand 
 */
public class PSContentTypeChangeListener implements IPSItemDefChangeListener
{  
   /**
    * Holds a reference to the content repository to update on changes
    */
   private PSContentRepository m_instance = null;
   
   /**
    * Holds the list of outstanding changes
    */
   private List<PSContentTypeChange> m_waitingChanges =
      new ArrayList<>();
   
   /**
    * Ctor
    * @param rep repository this listener applies to, never <code>null</code>
    */
   public PSContentTypeChangeListener(PSContentRepository rep)
   {
      if (rep == null)
      {
         throw new IllegalArgumentException("rep may not be null");
      }
      m_instance = rep;
   }
   
   public void registered(PSItemDefinition def, boolean notify)
         throws PSException
   {
      m_waitingChanges.add(new PSContentTypeChange(def, true));
      handle(notify);
   }

   public void unregistered(PSItemDefinition def, boolean notify)
         throws PSException
   {
      m_waitingChanges.add(new PSContentTypeChange(def, false));
      handle(notify);
   }
   
   private void handle(boolean notify) throws PSException
   {
      if (notify)
      {
         try
         {
            m_instance.configure(m_waitingChanges);
         }
         catch (Exception e)
         {
            throw new PSException("Problem configuring legacy content store", e);
         }
         m_waitingChanges.clear();
      }
   }

}
