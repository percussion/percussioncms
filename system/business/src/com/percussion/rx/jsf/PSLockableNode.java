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
