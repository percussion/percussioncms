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

import javax.swing.ImageIcon;

/** ICellImageHelper is to be implemented by the object wrapper class used
  * as the EditableListBox cell elements.  The interface allows the 
  * EditableListBoxCellRenderer (or any editor) to interact and display images 
  * as ImageIcon objects. 
*/

public interface ICellImageHelper
{

/** @returns ImageIcon retrieves the image stored in this particular cell.
*/
  public ImageIcon getImage();

/** @param image In case the program needs to change images, this method does 
  * it.
*/
  public void setImage(ImageIcon image);  
} 
