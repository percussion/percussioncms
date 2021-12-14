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
package com.percussion.cms.objectstore;


/**
 * Any db component that supports ordering must implement this interface.
 * When used with the IPSDbComponentList, the list will manage the sequence
 * ids within the group of components. The sequence ids of the components in
 * the list do not need to be sequential. If multiple members have the same
 * id, their order is indeterminent.
 * <p>Components implementing this interface should ignore the sequence id
 * in the implementations of hashCode and equals.
 *
 * @author Paul Howard
 * @version 1.0
 */
public interface IPSSequencedComponent
{
   /**
    * Index of this item w/ respect to all other items in the same list.
    * Zero based.
    *
    * @return The current position of this entry within the list of related
    *    components. -1 if the position is not set. If -1 is returned, this
    *    entry is sequenced at the end of the list with all other unassigned
    *    entries in an undefined order.
    */
   public int getPosition();


   /**
    * Sets the new position of this component within a set of related
    * components. If the new value is the same as the current value, no change
    * in state should occur. If the new value is different, it should behave
    * the same as it would for any other attibute value change (i.e., the
    * state should not be unmodified after the method returns).
    *
    * @param pos A value >= 0.
    */
   public void setPosition(int pos);
}
