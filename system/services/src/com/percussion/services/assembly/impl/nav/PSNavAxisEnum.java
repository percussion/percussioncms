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
