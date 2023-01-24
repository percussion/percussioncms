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
