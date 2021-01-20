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
