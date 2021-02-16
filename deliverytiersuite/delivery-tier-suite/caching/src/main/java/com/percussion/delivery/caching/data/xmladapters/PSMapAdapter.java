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

package com.percussion.delivery.caching.data.xmladapters;

import com.percussion.delivery.caching.data.PSCacheProviderProperties;
import com.percussion.delivery.caching.data.PSCacheProviderProperty;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Custom XmlAdapter which takes a PSCacheProviderProperties object (it has
 * a list of PSCacheProviderProperty objects) and maps it into a Map<String,String>
 * object.
 * 
 * @author miltonpividori
 *
 */
public class PSMapAdapter extends XmlAdapter<PSCacheProviderProperties, Map<String, String>>
{

    @Override
    public Map<String, String> unmarshal(PSCacheProviderProperties v) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<>();
        
        for (PSCacheProviderProperty providerProp : v.getProperty())
            hashMap.put(providerProp.getName(), providerProp.getValue());
        
        return hashMap;
    }

    @Override
    public PSCacheProviderProperties marshal(Map<String, String> v) throws Exception
    {
        PSCacheProviderProperties providerProperties = new PSCacheProviderProperties();
        
        for (String propName : v.keySet())
        {
            PSCacheProviderProperty prop = new PSCacheProviderProperty();
            prop.setName(propName);
            prop.setValue(v.get(propName));
            
            providerProperties.getProperty().add(prop);
        }
        
        return providerProperties;
    }

}
