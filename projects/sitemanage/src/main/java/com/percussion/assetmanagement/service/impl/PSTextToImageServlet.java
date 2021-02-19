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
package com.percussion.assetmanagement.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Servlet responsible for converting supplied text to an image and serve it as
 * an image.
 * 
 */
public class PSTextToImageServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String imageText = request.getParameter("imageText");
        if (StringUtils.isBlank(imageText))
        {
            imageText = DEFAULT_IMAGE_TEXT;
        }
        else
        {
            imageText = imageText.trim();
        }

        // Find the height and width of the image
        Font font = new Font("Verdana", Font.PLAIN, 11);

        FontMetrics metrics = new FontMetrics(font)
        {
        };
        Rectangle2D bounds = metrics.getStringBounds(imageText, null);
        int widthInPixels = (int) bounds.getWidth() + 2;
        int heightInPixels = (int) bounds.getHeight();

        // create buffered image
        BufferedImage buffer = new BufferedImage(widthInPixels, heightInPixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = buffer.createGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 102, 0));
        graphics.drawString(imageText, 1, 10);

        // Write the image to response
        response.setContentType("image/png");
        OutputStream os = response.getOutputStream();
        ImageIO.setUseCache(false);
        ImageIO.write(buffer, "png", os);
        os.close();
    }

    /**
     * The logger
     */
    private static Logger ms_logger = Logger.getLogger(PSTextToImageServlet.class);

    private static final String DEFAULT_IMAGE_TEXT = "";

}
