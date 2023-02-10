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


@XmlRootElement(name="Tree")
public class PSTransformCategory {

   private String label;
   private List<PSTransformCategoryNode> topNodes = new ArrayList<>();
   
   @XmlAttribute(name="label")
   public String getLabel()
   {
      return label;
   }
   
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   @XmlElement(name="Node")
   public List<PSTransformCategoryNode> getTopNodes()
   {
      return topNodes;
   }
   
   public void setTopNodes(List<PSTransformCategoryNode> topNodes)
   {
      this.topNodes = topNodes;
   }

   @Override
   public String toString()
   {
      return "PSUpgradePluginCategory [label=" + label + ", topNodes=" + topNodes + "]";
   }
}
