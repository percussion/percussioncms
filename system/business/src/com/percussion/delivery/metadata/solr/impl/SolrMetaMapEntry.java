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

package com.percussion.delivery.metadata.solr.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="entry")
public class SolrMetaMapEntry
{
   @XmlAttribute
   private String key;
   @XmlAttribute
   private String value;
   
   public SolrMetaMapEntry()
   {
      
   }
   public SolrMetaMapEntry(String key, String value)
   {
      this.key = key;
      this.value = value;
   }
   public String getKey()
   {
      return key;
   }
   public void setKey(String key)
   {
      this.key = key;
   }
   public String getValue()
   {
      return value;
   }
   public void setValue(String value)
   {
      this.value = value;
   }
   
   
}
