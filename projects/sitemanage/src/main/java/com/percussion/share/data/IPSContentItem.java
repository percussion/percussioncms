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
package com.percussion.share.data;

import java.util.Map;
import java.util.Map.Entry;


/**
 * A Generic Low level representation of an item in the system backed
 * by a Rhythmyx content item.
 * <p>
 * 
 * @author adamgent
 *
 */
public interface IPSContentItem extends IPSItemSummary
{
    /**
     * A {@link Map} of all the {@link String} fields.
     * The {@link Entry#getKey()} of the {@link Map} is the name of the field.
     * The {@link Entry#getValue()} of the {@link Map} is value of the field.
     * @return never <code>null</code>.
     */
    public Map<String, Object> getFields();
    
    /**
     * 
     * @param fields never <code>null</code>.
     */
    public void setFields(Map<String, Object> fields);

}
