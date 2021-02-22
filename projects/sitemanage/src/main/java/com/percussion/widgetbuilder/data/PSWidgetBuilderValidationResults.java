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
package com.percussion.widgetbuilder.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author JaySeletz
 *
 */
@XmlRootElement(name="WidgetBuilderValidationResults")
@JsonRootName("WidgetBuilderValidationResults")
public class PSWidgetBuilderValidationResults
{
    private List<PSWidgetBuilderValidationResult> results;
    private long definitionId;

    public List<PSWidgetBuilderValidationResult> getResults()
    {
        return results;
    }

    public void setResults(List<PSWidgetBuilderValidationResult> results)
    {
        this.results = results;
    }

    /**
     * Set the id of the validated definition
     * 
     * @param definitionId
     */
    public void setDefinitionId(long definitionId)
    {
        this.definitionId = definitionId;
    }

    public long getDefinitionId()
    {
        return definitionId;
    }
    
}
