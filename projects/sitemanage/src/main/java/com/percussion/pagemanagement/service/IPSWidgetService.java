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

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoRequest;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoResult;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;

import java.util.List;

/**
 * Retrieves {@link PSWidgetDefinition} and validates {@link PSWidgetItem}
 * against their corresponding {@link PSWidgetDefinition}.
 * 
 * @author adamgent
 *
 */
public interface IPSWidgetService extends IPSDataService<PSWidgetDefinition, PSWidgetSummary, String>
{

    /**
     * Enum of widgets that must be disabled for SaaS Install
     */
    public static enum SAAS_WIDGETS { LOGIN("percLogin"),
                                       SECURE_LOGIN("percSecureLogin"),
                                       REGISTRATION("percRegistration");
        private String widget = null;
        
        private SAAS_WIDGETS(String widget) {
            this.widget = widget;
        }
        
        public String valueOf() {
            return this.widget;
        }
        
    }
    
    /**
     * Validates a widget item against its {@link PSWidgetDefinition}
     * 
     * @param widgetItem
     * @return never <code>null</code>.
     * @throws PSPropertiesValidationException 
     * @see PSWidgetItem#getDefinitionId()
     */
    public PSSpringValidationException validateWidgetItem(PSWidgetItem widgetItem) throws PSPropertiesValidationException;
    
    
    /**
     * @return The name of the base template of the widgets.
     */
    public String getBaseTemplate();

    /**
     * @param baseTemplate The name of the base template for the widgets, never
     *            <code>null</code>. Assigned through spring property.
     */
    public void setBaseTemplate(String baseTemplate);
    
    /**
     * Finds all the widgets of the supplied type.
     * @param type The type of the widgets, widget types are set to one of the PSWidgetTypeEnum.
     * @return List of widget summaries never <code>null</code> may be empty.
     */
    public List<PSWidgetSummary> findByType(String type) throws PSDataServiceException;
    
    /**
     * Finds all the widgets of the supplied type.
     * @param type The type of the widgets, widget types are set to one of the PSWidgetTypeEnum.
     * @param filterDisabledWidgets if the value is equals case in sensitive "yes", then disabled widgets are filtered from returned list
     * @return List of widget summaries never <code>null</code> may be empty.
     */
    public List<PSWidgetSummary> findByType(String type, String filterDisabledWidgets) throws PSDataServiceException;
    
    /**
     * Find the additional information about a widget from the package that installed it.
     * 
     * @param request A list of package names, not <code>null</code>, may be empty in which case an empty list is returned.
     * 
     * @return A list of results for each widget that package info was found. If package info for a widget is not found, not result
     * for that widget is included in the results.
     */
    public PSWidgetPackageInfoResult findWidgetPackageInfo(PSWidgetPackageInfoRequest request);
}
