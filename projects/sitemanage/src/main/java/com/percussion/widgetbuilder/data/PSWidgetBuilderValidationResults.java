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
