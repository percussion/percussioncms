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