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

package com.percussion.activity.data;

import static org.apache.commons.lang.Validate.notEmpty;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotEmpty;


/**
 * This object holds the effectiveness details of the items under named site or site folder.
 */
@JsonRootName(value = "Effectiveness")
public class PSEffectiveness
{
    public PSEffectiveness()
    {        
    }
    
    /**
     * Constructs an effectiveness object.
     * 
     * @param name see {@link #getName()}.
     * @param effectiveness see {@link #getEffectiveness()}.
     */
    public PSEffectiveness(String name, Long effectiveness)
    {
        notEmpty(name);
        
        this.name = name;
        this.effectiveness = effectiveness;
    }

    /**
     * @return the the name of the site, section, or folder, never blank.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name must not be blank.
     */
    public void setName(String name)
    {
        notEmpty(name);
        
        this.name = name;
    }

    /**
     * @return the calculated effectiveness value for the item.
     */
    public Long getEffectiveness()
    {
        return effectiveness;
    }

    /**
     * @param effectiveness the effectiveness to set
     */
    public void setEffectiveness(Long effectiveness)
    {
        this.effectiveness = effectiveness;
    }

    @NotEmpty
    private String name;
    
    private Long effectiveness;
 
}
