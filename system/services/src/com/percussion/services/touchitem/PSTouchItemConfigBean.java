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
