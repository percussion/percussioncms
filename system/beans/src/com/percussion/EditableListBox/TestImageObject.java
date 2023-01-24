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
