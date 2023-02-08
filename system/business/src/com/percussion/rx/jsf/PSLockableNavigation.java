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
