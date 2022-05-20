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
package com.percussion.assetmanagement.service.impl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Servlet responsible for converting supplied text to an image and serve it as
 * an image.
 * 
 */
public class PSTextToImageServletTest //extends HttpServlet
{
    private static final long serialVersionUID = 1L;

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
//    {
//        String imageText = request.getParameter("imageText");
//        if (StringUtils.isBlank(imageText))
//        {
//            imageText = DEFAULT_IMAGE_TEXT;
//        }
//        else
//        {
//            imageText = imageText.trim();
//        }
//        BufferedImage img = drawIcon(imageText);
//        try {
//            ImageIO.write(img, "png", response.getOutputStream());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//    }

    private static BufferedImage drawIcon(String imageText){
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Verdana", Font.BOLD, 12);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(imageText );
        if(width == 0){
            width = imageText.length()*10;
        }
        int height = fm.getHeight() ;
        g2d.dispose();
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(255, 102, 0));
        g2d.drawString(imageText, 0, fm.getAscent() );

        g2d.dispose();
        return img;
    }

    public static void main(String[] args){
        File iconFile = new File("C:/test/abc.png");
       BufferedImage img =  drawIcon("< xyz2 >");
        try {
            ImageIO.write(img, "png", iconFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * The logger
     */
  //  private static final Logger ms_logger = LogManager.getLogger(PSTextToImageServlet.class);

    private static final String DEFAULT_IMAGE_TEXT = "GlobalVar";

}
