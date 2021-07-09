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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.data;

import java.util.Iterator;

/**
 * Interface to allow classes to be informed of changes to a table through an
 * update resource of a Rhythmyx Application.
 */
public interface IPSTableChangeListener
{
   /**
    * Gets the column names to include in any table change events this listener
    * is interested in receiving.
    *
    * @param tableName The name of the table for which the event is being
    * created.  May not be <code>null</code> or empty.
    * @param actionType the action for which the event is being created, one of
    * the <code>PSTableChangeEvent.ACTION_xxx</code> types.
    *
    * @return The Iterator over <code>0</code> or more columns, or
    * <code>null</code> if this listener should not receive an event for this
    * table change.  Only columns that will be modified using
    * <code>String</code> or numeric values should be specified.  Other types of
    * data will cause the column to be omitted from the event when it is
    * created.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public Iterator getColumns(String tableName, int actionType);

   /**
    * Called to notify listeners when a table has been changed by an update
    * resource in a Rhythmyx application.
    *
    * @param e The change event object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the event object is <code>null</code>.
    */
   public void tableChanged(PSTableChangeEvent e);

}
