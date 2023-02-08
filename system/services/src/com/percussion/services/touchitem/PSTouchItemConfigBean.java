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
package com.percussion.services.touchitem;

import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates a single touch item configuration which consists
 * of source and target content types, level (folder) value, and
 * flag to indicate if AA parents should be touched.
 * 
 * @author peterfrontiero
 */
public class PSTouchItemConfigBean
{
   /**
    * @return the source content type names
    */
   public Set<String> getSourceTypes()
   {
      return sourceTypes;
   }

   /**
    * @param sourceTypes source content type names
    */
   public void setSourceTypes(Set<String> sourceTypes)
   {
      this.sourceTypes = sourceTypes;
   }

   /**
    * @return the target content types
    */
   public Set<String> getTargetTypes()
   {
      return targetTypes;
   }

   /**
    * @param targetTypes the target content types
    */
   public void setTargetTypes(Set<String> targetTypes)
   {
      this.targetTypes = targetTypes;
   }

   /**
    * @return the level which indicates the folder
    * relative to the current item's folder in which
    * target items will be touched.
    */
   public int getLevel()
   {
      return level;
   }

   /**
    * @param level the level to set
    */
   public void setLevel(int level)
   {
      this.level = level;
   }

   /**
    * @return <code>true</code> to touch direct AA parents
    * of the items, <code>false</code> otherwise.
    */
   public boolean isTouchAAParents()
   {
      return touchAAParents;
   }

   /**
    * @param touchAAParents <code>true</code> to touch direct
    * AA parents of the items, <code>false</code> otherwise.
    */
   public void setTouchAAParents(boolean touchAAParents)
   {
      this.touchAAParents = touchAAParents;
   }
   
   /**
    * See {@link #getSourceTypes()}.
    */
   private Set<String> sourceTypes = new HashSet<>();
   
   /**
    * See {@link #getTargetTypes()}.
    */
   private Set<String> targetTypes = new HashSet<>();
   
   /**
    * See {@link #getLevel()}.
    */
   private int level = 0;
   
   /**
    * See {@link #isTouchAAParents()}.
    */
   private boolean touchAAParents = false;
   
}
