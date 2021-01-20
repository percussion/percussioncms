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

import javax.swing.ImageIcon;

/** Used for testing ICellImageHelper interface.
*/

public class TestImageObject 
    implements ICellImageHelper, EditableListBoxCellNameHelper
{
  public TestImageObject()
  {
    m_text = null;
    m_image = null;
  }

  public TestImageObject(String s)
  {
    m_text = s;
    m_image = null;
  }

  public TestImageObject(ImageIcon icon)
  {
    m_text = "";
    m_image = icon;
  }

  public TestImageObject(String s, ImageIcon icon)
  {
    m_text = s;
    m_image = icon;
  }

  public ImageIcon getImage()
  {
    return m_image;
  }

  public void setImage(ImageIcon icon)
  {
    m_image = icon;
  }

  public void setName(String s)
  {
    m_text = s;
  }

  public String getName()
  {
    return m_text;
  }

  public String toString()
  {
    return m_text;
  }

  private String m_text;
  private ImageIcon m_image;
} 
