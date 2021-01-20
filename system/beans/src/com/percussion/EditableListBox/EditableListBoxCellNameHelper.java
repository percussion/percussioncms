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

