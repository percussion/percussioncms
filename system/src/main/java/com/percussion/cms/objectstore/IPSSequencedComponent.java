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
