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
package com.percussion.pagemanagement.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represents a region in a template code.
 * 
 * @author adamgent
 * @see PSAbstractRegion
 */
@JsonRootName("Region" )
public class PSRegion extends PSAbstractRegion {


    private static final long serialVersionUID = 1L;

    @Override
    public void accept(IPSRegionNodeVisitor visitor)
    {
        visitor.visit(this);
        //We do not visit the subnodes like in the classic visitor pattern.
        //This keeps the order of the visitor flexibible.
    }    
    
    /**
     * Gets all regions, which includes the region of itself and all descendant regions.
     * The 1st element is the region itself, followed by its direct child region, and so forth.
     * @return the regions described above, never <code>null</code> or empty.
     */
    @JsonIgnoreProperties("allRegions")
    public List<PSRegion> getAllRegions()
    {
        List<PSRegion> results = new ArrayList<>();
        collectRegions(this, results);
        return results;
    }
    
    private void collectRegions(PSRegion parent, List<PSRegion> results)
    {   
        results.add(parent);
        collectRegions(parent.getChildren(), results);
    }
    
    private void collectRegions(List<PSRegionNode> nodes, List<PSRegion> results)
    {
        for (PSRegionNode node : nodes)
        {
            if (node instanceof PSRegion)
            {
                collectRegions((PSRegion) node, results);
            }
        }
    }


}
