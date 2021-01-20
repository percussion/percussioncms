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

