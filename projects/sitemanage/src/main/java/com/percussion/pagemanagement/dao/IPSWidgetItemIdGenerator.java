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