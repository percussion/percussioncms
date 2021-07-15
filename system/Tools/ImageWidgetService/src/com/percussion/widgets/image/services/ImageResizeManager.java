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

package com.percussion.widgets.image.services;

import com.percussion.widgets.image.data.ImageData;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.InputStream;

public abstract interface ImageResizeManager
{
  public static final int ROTATE_LEFT = -1;
  public static final int ROTATE_RIGHT = 1;

  public abstract ImageData generateImage(InputStream paramInputStream, Rectangle paramRectangle, Dimension paramDimension, int paramInt)
    throws Exception;

  public abstract ImageData generateImage(InputStream paramInputStream)
    throws Exception;

  public abstract ImageData generateImage(InputStream paramInputStream, Rectangle paramRectangle, Dimension paramDimension)
    throws Exception;
}
