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
package com.percussion.pagemanagement.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

/**
 * @author JaySeletz
 *
 */
@XmlRootElement(name = "WidgetPackageInfoRequest")
public class PSWidgetPackageInfoRequest
{
    List<String> widgetNames = new ArrayList<>();

    /**
     * Get the list of widget names.
     * 
     * @return The list, never <code>null</code>, may be empty.
     */
    public List<String> getWidgetNames()
    {
        return widgetNames;
    }

    public void setWidgetNames(List<String> widgetNames)
    {
        Validate.notNull(widgetNames);
        this.widgetNames = widgetNames;
    }
    
}
