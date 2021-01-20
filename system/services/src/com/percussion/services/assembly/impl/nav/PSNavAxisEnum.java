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
package com.percussion.services.assembly.impl.nav;


/**
 * The axis values for managed navigation. The self navon is always the navon
 * associated with the current content item being assembled.
 * 
 * @author dougrand
 */
public enum PSNavAxisEnum {
   /**
    * Not part of the axis, i.e. this navon isn't a sibling, parent, self
    * or child
    */
   NONE, 
   /**
    * This navon is an ancestor of the self, i.e. a grandparent or higher
    * parent to the self 
    */
   ANCESTOR, 
   /**
    * This navon is the direct parent of the self
    */
   PARENT, 
   /**
    * This navon is the child, grandchild or lower of the self
    */
   DESCENDANT, 
   /**
    * This is the self navon
    */
   SELF, 
   /**
    * This is a sibling navon, i.e. it shared its parent with the self navon
    */
   SIBLING;
}
