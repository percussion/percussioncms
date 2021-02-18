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

package com.percussion.pathmanagement.data.xmladapters;

import com.percussion.pathmanagement.data.PSPathItemDisplayProperties;
import com.percussion.pathmanagement.data.PSPathItemDisplayProperty;
import com.percussion.pathmanagement.data.xmladapters.PSMapAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Custom XmlAdapter which takes a PSPathItem object (it has
 * a map of String objects) and maps it into a PSPathItemDisplayProperties
 * object.
 * 
 * @author federicoromanelli
 *
 */
public class PSMapAdapter extends XmlAdapter<PSPathItemDisplayProperties, Map<String, String>>
{

    @Override
    public Map<String, String> unmarshal(PSPathItemDisplayProperties v) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<>();
        
        for (PSPathItemDisplayProperty displayProp : v.getDisplayProperty())
            hashMap.put(displayProp.getName(), displayProp.getValue());
        
        return hashMap;
    }

    @Override
    public PSPathItemDisplayProperties marshal(Map<String, String> v) throws Exception
    {
        PSPathItemDisplayProperties displayProperties = new PSPathItemDisplayProperties();
        try
        {
        
            for (String propName : v.keySet())
            {
                PSPathItemDisplayProperty prop = new PSPathItemDisplayProperty();
                prop.setName(propName);
                prop.setValue(v.get(propName));
                
                displayProperties.getDisplayProperty().add(prop);
            }
        }
        catch (Exception e){}
        
        return displayProperties;
    }

}
