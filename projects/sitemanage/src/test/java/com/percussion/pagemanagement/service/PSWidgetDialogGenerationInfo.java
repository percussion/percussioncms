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

/**
 * @author miltonpividori
 *
 */
public class PSWidgetDialogGenerationInfo
{
    private PSWidgetDefinition widgetDefinition;
    
    private boolean layoutDialogGenerated = false;
    private boolean contentDialogGenerated = false;
    private boolean styleDialogGenerated = false;
    
    public PSWidgetDialogGenerationInfo(PSWidgetDefinition widgetDefinition)
    {
        this.widgetDefinition = widgetDefinition;
    }

    /**
     * @return the widgetDefinition
     */
    public PSWidgetDefinition getWidgetDefinition()
    {
        return widgetDefinition;
    }

    /**
     * @param widgetDefinition the widgetDefinition to set
     */
    public void setWidgetDefinition(PSWidgetDefinition widgetDefinition)
    {
        this.widgetDefinition = widgetDefinition;
    }

    /**
     * @return the layoutDialogGenerated
     */
    public boolean isLayoutDialogGenerated()
    {
        return layoutDialogGenerated;
    }

    /**
     * @param layoutDialogGenerated the layoutDialogGenerated to set
     */
    public void setLayoutDialogGenerated(boolean layoutDialogGenerated)
    {
        this.layoutDialogGenerated = layoutDialogGenerated;
    }

    /**
     * @return the contentDialogGenerated
     */
    public boolean isContentDialogGenerated()
    {
        return contentDialogGenerated;
    }

    /**
     * @param contentDialogGenerated the contentDialogGenerated to set
     */
    public void setContentDialogGenerated(boolean contentDialogGenerated)
    {
        this.contentDialogGenerated = contentDialogGenerated;
    }

    /**
     * @return the styleDialogGenerated
     */
    public boolean isStyleDialogGenerated()
    {
        return styleDialogGenerated;
    }

    /**
     * @param styleDialogGenerated the styleDialogGenerated to set
     */
    public void setStyleDialogGenerated(boolean styleDialogGenerated)
    {
        this.styleDialogGenerated = styleDialogGenerated;
    }
}
