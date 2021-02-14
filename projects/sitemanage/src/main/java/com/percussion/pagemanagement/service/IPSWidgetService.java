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
package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoRequest;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoResult;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.share.service.IPSDataService;
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
    public List<PSWidgetSummary> findByType(String type);
    
    /**
     * Finds all the widgets of the supplied type.
     * @param type The type of the widgets, widget types are set to one of the PSWidgetTypeEnum.
     * @param filterDisabledWidgets if the value is equals case in sensitive "yes", then disabled widgets are filtered from returned list
     * @return List of widget summaries never <code>null</code> may be empty.
     */
    public List<PSWidgetSummary> findByType(String type, String filterDisabledWidgets);
    
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
