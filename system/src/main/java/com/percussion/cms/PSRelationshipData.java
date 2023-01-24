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
package com.percussion.cms;

import java.util.HashMap;
import java.util.Map;

/**
 * Relationship data structure for caching during processing of inline links. An 
 * object of this class is built purely from all outgoing relationships of the 
 * parent item. The relationships are of the category Active Assembly.
 * 
 * The fields in this should ideally be initialized once per parent content item 
 * processing
 * 
 * @see com.percussion.cms.PSSingleValueBuilder
 * 
 */    
public class PSRelationshipData
{
   /**
    * Map of all dependent items' contentids and the promotable version list for 
    * each contentid. The promotable version list is initilized to 
    * <code>null</code> and is built on ad hoc basis. The key is the Integer 
    * representing the contentid and the value is a list of Integer versions of 
    * contentids of the previous promotable versions of the item with contentid 
    * which is the key part of this object. Never <code>null</code>.  
    */
   public Map m_contentIdPathMap = new HashMap();

   /**
    * Map of all relationshipids (Integer) and the contentids of all outgoing 
    * relationships of active assembly category for the parent item. The key is 
    * the Integer verison of the relationshipid and the value is the Integer 
    * version o fthe dependent's contentid. Never <code>null</code>. 
    */
   public Map m_relIdContentIdMap = new HashMap();
}
