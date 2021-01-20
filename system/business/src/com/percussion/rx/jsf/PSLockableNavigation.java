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
package com.percussion.rx.jsf;

/**
 * This is used for a navigation which tree nodes can be locked when one of the
 * node is in edit mode. 
 *
 * @author Yu-Bing Chen
 */
public class PSLockableNavigation extends PSNavigation
{
   /* (non-Javadoc)
    * @see com.percussion.rx.jsf.PSNavigation#areCategoryNodesEnabled()
    */
   @Override
   public boolean areCategoryNodesEnabled()
   {
      return getCurrentItemGuid() == null;
   }

   /**
    * Determines if the navigation tree is locked, that is one of the node is
    * in edit mode. The navigation tree will be hidden while it is locked.
    * 
    * @return <code>true</code> if the navigation tree is locked; otherwise
    * return <code>false</code>.
    */
   public boolean getIsLocked()
   {
      return getCurrentNode() instanceof PSEditableNode;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected void focusOnStartingNode()
   {
      super.focusOnStartingNode();
    
      // reset to enable all tree nodes, in case it was in editor node.
      setCurrentItemGuid(null);
   }
}
