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

package com.percussion.widgetbuilder.service;


import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldsListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderSummaryData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResults;

import java.util.List;

/**
 * 
 * @author matthewernewein
 * 
 */
public interface IPSWidgetBuilderService
{
    /**
     * Checks to see if the service is enabled. Activation and deactivation of
     * the service can be done in Server.properties under the key
     * WidgetBuilderActive
     * 
     * @return a boolean <code>true</code> if service enabled <code>false</code>
     *         if service is not enabled
     */
    public boolean isWidgetBuilderEnabled();

    /**
     * Checks to see if the current definition has been deployed 
     * @param definitionId
     * @return true if the widget is deployed, false if the widget is not deployed
     */
    public boolean isWidgetDefinitionDeployed(long definitionId);

    /**
     * Deletes a widget builder definition
     * @param definitionId
     */
    public void deleteWidgetBuilderDefinition(long definitionId);
    
    /**
     * Gets a list of all the widget definition on this system
     * @return a list of the widgets definitions on the system
     */
    public List<PSWidgetBuilderDefinitionData> loadAll();
    
    /**
     * Loads a widget definition given an id
     * @param definitionId
     * @return a widget definition
     */
    public PSWidgetBuilderDefinitionData loadWidgetDefinition(long definitionId);
    
    
    /**
     * Saves a definition
     * @param definition
     * @return the validation results for the save.  If the validation failed, there will be errors in the results.  
     * If the validation is successful, the results will have the ID of the saved definition 
     */
    public PSWidgetBuilderValidationResults saveWidgetBuilderDefinition(PSWidgetBuilderDefinitionData definition);

    /**
     * Builds and deploys the widget.
     * @param definitionId
     */
    public void deployWidget(long definitionId);

    /**
     * Gets a list of summaries for all widget definitions on this system
     * 
     * @return The list, not <code>null</code>, may be empty.
     */
    List<PSWidgetBuilderSummaryData> loadAllSummaries();
    
    /**
     * Validate the supplied definition:
     * 
     * Widget name for new widget definition is unique
     * Required fields for widget definition and format
     * All field names including child fields must be unique with character restrictions
     * Version format
     * no field will break widget building or packaging or installation
     * 
     * Does not validate widget functionality eg: display html, resources (exists or not or breaks the page when included)
     * 
     * @param definition The definition to validate, not <code>null</code>.
     * 
     * @return The results of the validation, not <code>null</code>, may be empty.
     */
    public PSWidgetBuilderValidationResults validate(PSWidgetBuilderDefinitionData definition);
    
}
