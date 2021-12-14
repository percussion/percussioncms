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

package com.percussion.EditableListBox;

// import java.util.Vector;

/** This interface allows user specified objects to be created on default in the
  * element cells of the EditableListBox. EditableListBox owns an instance of
  * this interface, so the users will have to create their own implementation
  * of this interface in EditableListBox in order to work.
  * <P>
  * Use an anonymous inner class implementation on 
  * EditableListBox.setDataExchange(IDataExchange).
  * <P>
  * Future development will have ways to allow single/multiple data returns to
  * the EditableListBox.  For now, only single data return is possible in the
  * implementation.  If you need a work-around, see RoleMemberPropertyDialog.
  *
  * @see EditableListBox
*/
public interface IDataExchange
{
/** Creates a new instance of a user specified object for the EditableListBox
  * to store and use on default.  For example, clicking on the 
  * &quot;insert&quot; button, a new cell is created.  If the IDataExchange is
  * implemented, then the new cell will automatically create a new object 
  * specified in this method.
*/
  public Object createNewInstance();

  
/** Allows multiple objects to be returned to the EditableListBox.
*/  
  /*
  public Vector getNewData();
  */
} 
