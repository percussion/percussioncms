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

package com.percussion.services.widgetbuilder;

import com.percussion.share.dao.IPSGenericDao;

import java.util.List;



public interface IPSWidgetBuilderDefinitionDao
{
    /**
     * Saves the widget builder definition object.
     * @param definition must not be <code>null</code>
     * @return the widget builder definition
     */
    PSWidgetBuilderDefinition save(PSWidgetBuilderDefinition definition) throws IPSGenericDao.SaveException;
    
    /**
     * Finds widget builder definition by the definition id
     * @param definitionId raw contentid of the item
     * @return a widget builder definition if exists otherwise <code>null</code>.
     */
    PSWidgetBuilderDefinition find(long definitionId);
    
    /**
     * Deletes the widget builder definition entry for the supplied id.
     * @param definitionId must not be <code>null</code>
     */
    void delete(long definitionId);
    
    /**
     * Gets a list of all the WidgetBuilderDefinitions
     */
    List<PSWidgetBuilderDefinition> getAll();
}
