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

package com.percussion.delivery.caching.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PSCacheRegion", propOrder = {
    "provider",
    "webProperties"
})
public class PSCacheRegion
{
    @XmlElement(name = "Provider", required = true)
    protected PSCacheProvider provider;

    @XmlElementWrapper(name = "WebProperties")
    @XmlElement(name = "WebProperty")
    protected List<PSCacheWebProperty> webProperties;

    @XmlAttribute
    protected String name;

    public PSCacheProvider getProvider()
    {
        return provider;
    }

    public void setProvider(PSCacheProvider value)
    {
        this.provider = value;
    }

    public List<PSCacheWebProperty> getWebProperties()
    {
        return webProperties;
    }

    public void setWebProperties(List<PSCacheWebProperty> props)
    {
        this.webProperties = props;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        this.name = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || !(obj.getClass().getName().equals(this.getClass().getName())))
            return false;
        return name.equals(((PSCacheRegion)obj).name);
    }
    
    

}
