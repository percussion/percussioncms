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
package com.percussion.pagemanagement.dao;

import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSWidgetItem;

/**
 * Generates ids for widget items.
 * The ids only have to be unique with in the page.
 * @author adamgent
 *
 */
public interface IPSWidgetItemIdGenerator
{

    /**
     * 
     * Generates an id that should be unique over the widget associations.
     * 
     * @param widgets never <code>null</code>.
     * @param item never <code>null</code>.
     * @return generated id.
     */
    public abstract Long generateId(PSRegionWidgetAssociations widgets, PSWidgetItem item);

    /**
     * Generates and sets ids for widgets that do not have an id.
     * Or in other words where {@link PSWidgetItem#getId()} is <code>null</code>
     * an new id will be generated and set on the widget.
     * <p>
     * <strong>Note that this method will mutate the widgets</strong>
     * @param widgets never <code>null</code>.
     */
    public void generateIds(PSRegionWidgetAssociations widgets);
    
    /**
     * Sets ids to null for widgets that have an id.
     * @param widgets never <code>null</code>.
     */
    public void deleteIds (PSRegionWidgetAssociations widgets);

}
