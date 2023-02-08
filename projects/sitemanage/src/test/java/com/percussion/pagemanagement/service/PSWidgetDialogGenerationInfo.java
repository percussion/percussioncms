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
