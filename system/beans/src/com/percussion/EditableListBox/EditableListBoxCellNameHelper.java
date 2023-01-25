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

package com.percussion.EditableListBox;

/** This interface is needed to help CellEditors to identify objects for embedding
  * the name (String) to part of the Cell Object.  Then it sets name String from
  * the cell of the list to the actual object referencing the cell list name.
  * This is implemented by the object stored in the TableModel.
*/

public interface EditableListBoxCellNameHelper
{

/** Sets the String displayed on the cell of the list(JTable) to the Object
  * referencing that name.
*/

   public void setName(String s);

/** Retrieves the name String from the Cell reference Object */

   public String getName();
}

