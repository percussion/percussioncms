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
