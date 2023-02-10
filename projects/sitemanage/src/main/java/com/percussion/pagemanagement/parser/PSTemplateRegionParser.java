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
