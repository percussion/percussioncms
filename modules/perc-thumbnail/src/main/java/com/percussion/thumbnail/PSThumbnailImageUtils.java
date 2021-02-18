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

package com.percussion.thumbnail;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PSThumbnailImageUtils {
    private static final Log log = LogFactory.getLog(PSThumbnailImageUtils.class);

    public static void resizeThumbnail(String thumbnailFilePath) {

        File inImageFile = new File(thumbnailFilePath);

        int i = 0;
        while ((!inImageFile.exists() || !inImageFile.canWrite()) && i < 120) {
            Thread.sleep(500);
            i++;
        }

        if (inImageFile.exists() && !inImageFile.canWrite()) {
            File outImageFile = new File(thumbnailFilePath);

            try (FileInputStream inputStream = new FileInputStream(inImageFile)) {
                try (FileOutputStream output = new FileOutputStream(outImageFile)) {

                    BufferedImage sourceImage = ImageIO.read(inputStream);
                    Image thumbnail = sourceImage.getScaledInstance(290, 207, Image.SCALE_SMOOTH);
                    BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
                            thumbnail.getHeight(null),
                            BufferedImage.TYPE_INT_RGB);
                    bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);


                    ImageWriter imageWriter = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();

                    float quality = 1.0f;
                    JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
                    jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
                    jpegParams.setCompressionQuality(quality);

                    imageWriter.setOutput(output);

                    IIOImage outimage = new IIOImage(bufferedThumbnail, null, null);
                    imageWriter.write(null, outimage, jpegParams);
                    imageWriter.dispose();
                }
            } catch (Exception e) {
                //FB: DMI_INVOKING_TOSTRING_ON_ARRAY NC 1-16-16
                log.debug("Failed to resize thumbnail at: " + thumbnailFilePath, e);
            }
        }

    }
}



