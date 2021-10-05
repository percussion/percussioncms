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
