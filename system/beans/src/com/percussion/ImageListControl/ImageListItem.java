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

package com.percussion.ImageListControl;

import javax.swing.ImageIcon;

/** The property class of ImageListControl.  This class contains the ImageIcon
  * object and a String to be used in the ImageListControlRenderer.
*/

public class ImageListItem
{

  public ImageListItem()
  {
    m_icon = null;
    itemText = null;
    data = null;
  }

  public ImageListItem(ImageIcon icon, String text)
  {
    this.m_icon = icon;
    itemText = text;
    data = null;
  }

  public ImageListItem(ImageIcon icon, String text, Object data)
  {
    this.m_icon = icon;
    itemText = text;
    this.data = data;
  }

//
// PUBLIC METHODS
//

  public String toString()
  {
    return itemText;
  }

/** @returns String The text name of the ImageListItem object.
*/
  public String getText()
  {
    return itemText;
  }

/** @param text The text name of the ImageListItem object.
*/
  public void setText(String text)
  {
    itemText = text;
  }

/** @returns ImageIcon The image of the ImageListItem object.
*/
  public ImageIcon getImage()
  {
    return m_icon;
  }

/** @param icon The image of the ImageListItem object.
*/
  public void setImage(ImageIcon icon)
  {
    this.m_icon = icon;
  }

/** @return Object The data object associated with the list display element.
*/
  public Object getData()
  {
    return data;
  }

/** @param data The data object associated with the list display element.
*/
  public void setData(Object data)
  {
    this.data = data;
  }

  private String itemText;
  private ImageIcon m_icon;
  private Object data;
}

