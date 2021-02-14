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
package com.percussion.ui.service;

import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.data.PSSimpleDisplayFormat;

import java.util.List;


/**
 * It has the responsibility to retrieve needed data for {@link PSPathItem}
 * objects to properly display it on the List View. For instance, it fills
 * display properties for {@link PSPathItem} objects.
 * <p>
 * Each {@link IPSPathService} should have an {@link IPSListViewHelper}
 * implementation associated.
 * 
 * @author miltonpividori
 * 
 */
public interface IPSListViewHelper
{
    public static final String CONTENT_CREATEDBY_NAME = "sys_contentcreatedby";
    public static final String CONTENT_CREATEDDATE_NAME = "sys_contentcreateddate";
    public static final String POSTDATE_NAME = "sys_postdate";
    public static final String CONTENT_LAST_MODIFIED_DATE_NAME = "sys_contentlastmodifieddate";
    public static final String CONTENT_LAST_MODIFIER_NAME = "sys_contentlastmodifier";
    public static final String STATE_NAME = "sys_statename";
    public static final String WORKFLOW_NAME = "sys_workflow";
    public static final String TITLE_NAME = "sys_title";
    public static final String CONTENTTYPE_NAME = "sys_contenttypename";
    public static final String SIZE = "sys_size";
    
    
    /**
     * Fills the display properties of the {@link PSPathItem} objects given in the
     * {@link PSDisplayPropertiesCriteria} parameter. If the display properties are already set
     * for the first {@link PSPathItem} object in the list, then no action is performed,
     * because that means that the display properties were already set.
     * 
     * @param criteria It has the necessary information to fill the {@link PSPathItem} objects
     * with the display properties. It cannot be <code>null</code>, nor it's {@link PSPathItem}
     * object list field. If the {@link PSSimpleDisplayFormat} format is <code>null</code>, then
     * no action is performed.
     */
    void fillDisplayProperties(PSDisplayPropertiesCriteria criteria);
    
    /**
     * Set optional processors to post process the display properties.
     * 
     * @param processor The processor, ma be <code>null</code> to clear the procesor.
     */
    void setPostProcessors(List<IPSListViewProcessor> processors);
}
