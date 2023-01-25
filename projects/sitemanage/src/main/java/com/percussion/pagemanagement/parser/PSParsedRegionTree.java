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

import java.util.HashMap;
import java.util.Map;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.parser.IPSRegionParser.IPSRegionParserRegionFactory;

/**
 * An Abstract Syntax Tree of Regions parsed from an unexpanded HTML template 
 * (not processed by velocity yet).
 * <p>
 * Groups top-level {@link PSAbstractRegion} objects by id. Also keeps track of all
 * child region within the tree.
 * 
 * @param <REGION> Region type.
 * @param <CODE> Code type.
 */
public class PSParsedRegionTree<REGION extends PSAbstractRegion, CODE extends PSRegionCode>
{
    private REGION rootNode;

    private Map<String, REGION> regions = new HashMap<>();

    private static final String ROOT_NODE_ID = "percRoot";

    private IPSRegionParserRegionFactory<REGION, CODE> regionFactory;

    public PSParsedRegionTree(IPSRegionParserRegionFactory<REGION, CODE> regionFactory)
    {
        this.regionFactory = regionFactory;
        rootNode = this.regionFactory.createRootRegion();
        rootNode.setRegionId(ROOT_NODE_ID);
    }

    /**
     * A parsed region tree should have a root node
     * and it should be a region.
     * 
     * @return never <code>null</code>.
     */
    public REGION getRootNode()
    {
        return rootNode;
    }

    /**
     * The region id to region map.
     * @return never <code>null</code>.
     */
    public Map<String, REGION> getRegions()
    {
        return regions;
    }
}
