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

package com.percussion.services.touchitem.impl;

import com.percussion.design.objectstore.PSRelationship;

import java.io.Serializable;

public class PSTouchItemData implements Serializable
{
   
   /**
    * safe to serialize.
    */
   private static final long serialVersionUID = -4130486603042453198L;
   private int action;
   private PSRelationship relationship;
   
   public int getAction()
   {
      return action;
   }
   public void setAction(int action)
   {
      this.action = action;
   }
   public PSRelationship getRelationship()
   {
      return relationship;
   }
   public void setRelationship(PSRelationship relationship)
   {
      this.relationship = relationship;
   }
   @Override
   public String toString()
   {
      return "PSTouchItemData [action=" + action + ", relationship="
            + relationship + "]";
   }
   
   

}
