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

package com.percussion.delivery.caching.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PSCacheConfig", propOrder = {
    "cacheRegion"
})
@XmlRootElement(name = "CacheConfig")
public class PSCacheConfig
{
    @XmlElement(name = "CacheRegion", required = true)
    protected Collection<PSCacheRegion> cacheRegion;
    
    @XmlAttribute
    protected int maxQueueWorkers = 5;

    public PSCacheRegion getRegion(String name)
    {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty.");
        if (cacheRegion == null)
            return null;
        for (PSCacheRegion reg : cacheRegion)
        {
            if (reg.getName().equals(name))
                return reg;
        }
        return null;
    }

    public Collection<PSCacheRegion> getCacheRegion()
    {
        if (cacheRegion == null)
        {
            cacheRegion = new ArrayList<>();
        }
        return this.cacheRegion;
    }
    
    public int getMaxQueueWorkers()
    {
        return maxQueueWorkers;
    }

}
