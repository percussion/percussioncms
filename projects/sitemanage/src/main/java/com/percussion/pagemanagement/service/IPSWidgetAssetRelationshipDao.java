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

package com.percussion.pagemanagement.service;

/**
 * The service used to update Page & Asset relationships.
 * 
 * @author YuBingChen
 */
public interface IPSWidgetAssetRelationshipDao
{
    /**
     * Updates the widget name (of the given template) for all relationships
     * where the owners are the pages that use the given template
     *  
     * @param templateId the ID of the template, not blank.
     * @param widgetName the new name of the widget, may be null or empty.
     * @param widgetId the ID of the widget, not blank.
     * 
     * @return number of relationships have been updated.
     */
    int updateWidgetNameForRelatedPages(String templateId, String widgetName, long widgetId);
}
