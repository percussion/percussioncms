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
package com.percussion.pagemanagement.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionNode;
import com.percussion.pagemanagement.data.PSRegionTree;

public class PSTemplateRegionParser extends PSRegionParserAdapter<PSRegion, PSRegionCode>
{

    PSRegionTree regionTree;
    Map<String, PSRegion> regions;
    
    
    public PSTemplateRegionParser(Map<String, PSRegion> regions)
    {
        super();
        this.regions = regions;
    }

    public PSRegion createRegion(String regionId)
    {
        PSRegion region = regions.get(regionId);
        if(region != null) {
            region.setChildren(new ArrayList<>());
        }
        else {
            region = new PSRegion();
        }
        region.setRegionId(regionId);
        return region;
    }

    public PSRegionCode createRegionCode()
    {
        return new PSRegionCode();
    }

    public PSRegion createRootRegion()
    {
        return new PSRegion();
    }
    
    public static PSParsedRegionTree<PSRegion, PSRegionCode> parse(Map<String, PSRegion> regions, String html) {
        regions = regions == null ? new HashMap<>() : regions;
        PSTemplateRegionParser parser = new PSTemplateRegionParser(regions);
        PSParsedRegionTree<PSRegion, PSRegionCode> pt = parser.parse(html);
        return pt;
    }

}
