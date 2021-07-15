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
