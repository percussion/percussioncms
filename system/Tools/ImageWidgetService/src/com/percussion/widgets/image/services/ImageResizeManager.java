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

package com.percussion.widgets.image.services;

import com.percussion.widgets.image.data.ImageData;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.InputStream;

public interface ImageResizeManager {

    int ROTATE_LEFT = -1;
    int ROTATE_RIGHT = 1;

    String getFileName();
    void setFileName(String imageFileName);

    String getImageFormat();
    void setImageFormat(String imageFormat);

    String getExtension();
    void setExtension(String extension);

    String getContentType();
    void setContentType(String contentType);

    ImageData generateImage(InputStream paramInputStream, Rectangle paramRectangle, Dimension paramDimension, int paramInt)
            throws Exception;

    ImageData generateImage(InputStream paramInputStream)
            throws Exception;

    ImageData generateImage(InputStream paramInputStream, Rectangle paramRectangle, Dimension paramDimension)
            throws Exception;
}
