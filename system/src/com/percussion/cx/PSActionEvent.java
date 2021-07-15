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
package com.percussion.cx;

import java.util.Iterator;

/**
 * The class that describes the action to take upon execution of an action.
 */
public class PSActionEvent
{
   /**
    * Constructs the event with the hint that needs to be refreshed.
    *
    * @param refreshHint the hint, may not be <code>null</code> or empty.
    */
   public PSActionEvent(String refreshHint)
   {
      if(refreshHint == null || refreshHint.trim().length() == 0)
         throw new IllegalArgumentException(
            "refreshHint may not be null or empty.");

      m_refreshHint = refreshHint;
   }

   /**
    * Gets the refresh hint that describes the action to take by the reciever
    * of this event object.
    *
    * @return the hint, never <code>null</code> or empty, may be one of the
    * REFRESH_xxx values.
    */
   public String getRefreshHint()
   {
      return m_refreshHint;
   }
   
   /**
    * Sets the nodes to refresh, these should be existing nodes in the tree.
    * Should be called if the refresh hint is <code>REFRESH_NODES</code>. The
    * first one in the list is the default selection for the listener.
    * 
    * @param nodes the nodes to refresh, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if nodes is not valid.
    */
   public void setRefreshNodes(Iterator nodes)
   {
      if(nodes == null || !nodes.hasNext())
         throw new IllegalArgumentException("nodes may not be null or empty.");
         
      m_nodes = nodes;      
   }   
   
   /**
    * Gets the list of nodes to refresh in UI, should be called by the listener.
    * The first one in the list should be selected in the tree.
    * 
    * @return the nodes, may be <code>null</code> if the refresh hint is not
    * <code>REFRESH_NODES</code>.
    */
   public Iterator getRefreshNodes()
   {
      return m_nodes;
   }

   /**
    * Set if a full vs partial refresh is required.  
    * 
    * @param isFull <code>true</code> if a full refresh is to be performed, 
    * <code>false</code> if a refresh of only dirty nodes is to be performed.
    */
   public void setIsFullRefresh(boolean isFull)
   {
      m_fullRefresh = isFull;
   }
   
   /**
    * Determine if a full vs partial refresh is required.  See 
    * {@link #setIsFullRefresh(boolean)} for more info.
    * 
    * @return <code>true</code> if a full refresh is to be performed, 
    * <code>false</code> if not.
    */
   public boolean isFullRefresh()
   {
      return m_fullRefresh;
   }

   /**
    * The hint that describes the action that needs to be performed by the
    * reciever, initialized in the ctor and never <code>null</code>, empty or
    * modified after that.
    */
   private String m_refreshHint;
   
   /**
    * The list of nodes to refresh in UI, <code>null</code> until call to <code>
    * setRefreshNodes(Iterator)</code>.
    */
   private Iterator m_nodes;
   /**
    * Determines if a full vs partial refresh is required.  Is <code>true</code>
    * if a full refresh is to be performed, <code>false</code> if a refresh of
    * only dirty nodes is to be performed.
    */
   private boolean m_fullRefresh = false;

   /**
    * The constant that describes the root of the navigational tree need to be
    * refreshed.
    */
   public static final String REFRESH_NAV_ROOT = "Root";

   /**
    * The constant that describes the selected node of the navigational tree
    * need to be refreshed.
    */
   public static final String REFRESH_NAV_SELECTED = "Selected";

   /**
    * The constant that describes the parent of the selected node of the
    * navigational tree need to be refreshed.
    */
   public static final String REFRESH_NAV_SEL_PARENT = "Parent";

   /**
    * The constant that describes list of the nodes of the navigational tree
    * need to be refreshed and first one in the list need to be selected.
    */
   public static final String REFRESH_NODES = "Nodes";

   /**
    * The constant that describes the display options of the applet need to be
    * refreshed.
    */
   public static final String REFRESH_OPTIONS = "Options";
   
   /**
    * The constant that describes that the any nodes matching the supplied nodes 
    * should be marked as dirty throughout the tree.
    */
   public static final String DIRTY_NODES = "DirtyNodes";
}
