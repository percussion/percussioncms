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

package com.percussion.category.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="CategoryNode")
public class PSTransformCategoryNode {

   private String id;
   private String label;
   private String selectable;
   private List<PSTransformCategoryNode> childNodes = new ArrayList<>();
   
   @XmlAttribute(name="id")
   public String getId()
   {
      return id;
   }
   
   public void setId(String id)
   {
      this.id = id;
   }
   
   @XmlAttribute(name="label")
   public String getLabel()
   {
      return label;
   }
   
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   @XmlAttribute(name="selectable")
   public String getSelectable()
   {
      return selectable;
   }
   
   public void setSelectable(String selectable)
   {
      this.selectable = selectable;
   }
   
   @XmlElement(name="Node")
   public List<PSTransformCategoryNode> getChildNodes()
   {
      return childNodes;
   }
   
   public void setChildNodes(List<PSTransformCategoryNode> childNodes)
   {
      this.childNodes = childNodes;
   }

   @Override
   public String toString()
   {
      return "PSUpgradePluginCategoryNode [id=" + id + ", label=" + label + ", selectable=" + selectable
            + ", childNodes=" + childNodes + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((childNodes == null) ? 0 : childNodes.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((label == null) ? 0 : label.hashCode());
      result = prime * result + ((selectable == null) ? 0 : selectable.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      PSTransformCategoryNode other = (PSTransformCategoryNode) obj;
      if (childNodes == null)
      {
         if (other.childNodes != null) {
            return false;
         }
      }
      else if (!childNodes.equals(other.childNodes)) {
         return false;
      }
      if (id == null)
      {
         if (other.id != null) {
            return false;
         }
      }
      else if (!id.equals(other.id)) {
         return false;
      }
      if (label == null)
      {
         if (other.label != null) {
            return false;
         }
      }
      else if (!label.equals(other.label)) {
         return false;
      }
      if (selectable == null)
      {
         if (other.selectable != null) {
            return false;
         }
      }
      else if (!selectable.equals(other.selectable)) {
         return false;
      }
      return true;
   }
}
