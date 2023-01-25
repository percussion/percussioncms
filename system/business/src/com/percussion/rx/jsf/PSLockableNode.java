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
package com.percussion.rx.jsf;

import com.percussion.utils.guid.IPSGuid;

/**
 * A node can be locked while one of the node in the tree is in edit mode. 
 *
 * @author Yu-Bing Chen
 */
public class PSLockableNode extends PSNodeBase
{
   /**
    * Constructor allowing to specify title, outcome and label.
    *
    * @param title never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    * @param label the value returned by {@link #getLabel()}.
    *    Can be <code>null</code> or blank.
    *    
    * @see PSNodeBase#PSNodeBase(String, String, String) 
    */
   public PSLockableNode(String title, String outcome, String label) 
   {
      super(title, outcome, label);
      m_guid = null;
   }

   /**
    * Constructs an instance with a title and ID. 
    * 
    * @param title never <code>null</code> or empty
    * @param guid the GUID of the design object, never <code>null</code>
    */
   public PSLockableNode(String title, IPSGuid guid) 
   {
      super(title, null);
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      m_guid = guid;
   }

   @Override
   public boolean getEnabled()
   {
      final PSNavigation nav = getModel().getNavigator();
      return nav.getCurrentItemGuid() == null
            || nav.getCurrentItemGuid().equals(m_guid);
   }

   @Override
   public Object getKey()
   {
      if (m_guid == null)
         return super.getKey();
      else
         return m_guid.toString();
   }

   /**
    * @return the GUID for this node, may be <code>null</code> if not defined.
    */
   public IPSGuid getGUID()
   {
      return m_guid;
   }

   /**
    * The identifier for the object being edited, it may be <code>null</code>
    * if not defined, but never modified after constructor.
    */
   private final IPSGuid m_guid;
}
