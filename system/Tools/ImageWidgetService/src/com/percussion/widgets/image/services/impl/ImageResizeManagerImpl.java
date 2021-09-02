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

package com.percussion.widgets.image.services.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.widgets.image.data.ImageData;
import com.percussion.widgets.image.services.ImageResizeManager;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class ImageResizeManagerImpl implements ImageResizeManager {
    private static final Logger log = LogManager.getLogger(ImageResizeManagerImpl.class);

    private String imageFormat = "";
    private String extension = "";
    private String contentType = "";
    private float compression = 1F;
    private int stepFactor = 2;
    private int maxInterpolationSize = 1000000;

    public ImageData generateImage(InputStream input) throws IllegalArgumentException {

        return generateImage(input, null, null, 0);
    }

    public ImageData generateImage(InputStream input, Rectangle cropBox,
                                   Dimension size) throws IllegalArgumentException {
        return generateImage(input, cropBox, size, 0);
    }

    public ImageData generateImage(InputStream input, Rectangle cropBox,
                                   Dimension size, int rotate) throws IllegalArgumentException {
        ImageData result = new ImageData();

        result.setExt(this.getExtension());
        result.setMimeType(this.getContentType());

        try {
            BufferedImage inImage = ImageIO.read(input);

            Dimension outputSize = getOutSize(cropBox, size, inImage);
            outputSize.height = (outputSize.height <= 0) ? 1 : outputSize.height;
            outputSize.width = (outputSize.width <= 0) ? 1 : outputSize.width;

            Rectangle sourceBox = getSourceBox(cropBox, inImage);

            BufferedImage outImage = scaleImage(inImage, sourceBox, outputSize);
            outImage = rotateImage(outImage, rotate);

            result = postProcessImage(result, outImage);

        } catch (IOException e) {
            log.error("Unable to read image data. Error: {}", e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return result;


    }

    private ImageData postProcessImage(ImageData result, BufferedImage outImage)
            throws IllegalArgumentException  {

        try(ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outStream)) {

            ImageWriter iw = getImageWriter(result.getExt());
            ImageWriteParam iwp = iw.getDefaultWriteParam();
            IIOMetadata metadata = null;

            if (result.getMimeType().equals("image/jpg")) {
                log.debug("Compressing a JPEG");

                iwp.setCompressionMode(2);
                iwp.setCompressionQuality(this.compression);
            }
            if ((result.getMimeType().equals("image/gif"))
                    && (outImage.getColorModel().hasAlpha())) {
                log.debug("Setting GIF transparency flag");
                metadata = getTransparentMetadata(outImage, iw, iwp);
                log.debug("Transparent metadata is {}", metadata);
            }



            iw.setOutput(mcios);
            iw.write(null, new IIOImage(outImage, new ArrayList(), metadata), iwp);
            mcios.flush();
            outStream.flush();
            result.setSize(outStream.size());
            log.debug("Output size is {}", result.getSize());
            result.setBinary(outStream.toByteArray());
            result.setWidth(outImage.getWidth());
            result.setHeight(outImage.getHeight());
        }
        catch (IOException e) {
            log.error("Error was encountered while trying to create output stream. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
        }
        return result;
    }

    private ImageWriter getImageWriter(String format) throws IllegalArgumentException {

        if(format == null || StringUtils.isEmpty(format))
            throw new IllegalArgumentException("Image format is required.");

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(format);

        if (!iter.hasNext()) {
            //Scan for plugins
            ImageIO.scanForPlugins();
            //try again
            iter = ImageIO.getImageWritersByFormatName(format);
            if(!iter.hasNext())
                throw new IllegalArgumentException("No writer found for the image format: " + format);
        }

        ImageWriter iw = iter.next();
        log.debug("Image writer is {}" , iw.getClass().getCanonicalName());
        return iw;
    }

    private Rectangle getSourceBox(Rectangle cropBox, BufferedImage inImage) {
        Rectangle sourceBox;
        if (cropBox != null) {
            sourceBox = new Rectangle(cropBox);
        } else {
            sourceBox = new Rectangle(0, 0, inImage.getWidth(),
                    inImage.getHeight());
        }
        return sourceBox;
    }

    private Dimension getOutSize(Rectangle cropBox, Dimension size,
                                 BufferedImage inImage) {
        Dimension outsize;
        if ((size != null) && (cropBox != null)) {
            outsize = computeSizeFromAspectRatio(cropBox, size);
        } else {
            if (size != null) {
                outsize = new Dimension(size);
            } else {
                if (cropBox != null) {
                    if (cropBox.width <= 0) {
                        cropBox.width = 1;
                    }
                    if (cropBox.height <= 0) {
                        cropBox.height = 1;
                    }
                    outsize = new Dimension(cropBox.width, cropBox.height);
                } else {
                    outsize = new Dimension(inImage.getWidth(), inImage.getHeight());
                }
            }
        }
        return outsize;
    }

    protected BufferedImage scaleImage(BufferedImage inImage,
                                       Rectangle sourceBox, Dimension outSize) {
        log.debug("Scaling image");

        BufferedImage croppedImage = inImage.getSubimage(sourceBox.x,
                sourceBox.y, sourceBox.width, sourceBox.height);

        while (((croppedImage.getHeight() > outSize.height * this.stepFactor) || (croppedImage
                .getWidth() > outSize.width * this.stepFactor))
                && ((outSize.height > 1) && (outSize.width > 1))) {
            croppedImage = halfImage(croppedImage);
        }

        BufferedImage outImage = createBufferedImage(outSize.width,
                outSize.height, inImage);
        Graphics2D g2d = getGraphics(outImage);

        g2d.drawImage(croppedImage, 0, 0, outSize.width, outSize.height, 0, 0,
                croppedImage.getWidth(), croppedImage.getHeight(), null);

        g2d.dispose();
        return outImage;
    }

    protected BufferedImage halfImage(BufferedImage inImage) {
        long timer = System.currentTimeMillis();
        int height = inImage.getHeight() / this.stepFactor;
        int width = inImage.getWidth() / this.stepFactor;
        log.debug("Scaling to image height {} width {}", height, width);
        BufferedImage halfImage = createBufferedImage(width, height, inImage);
        Graphics2D half = getGraphics(halfImage);

        if (height * width < this.maxInterpolationSize) {
            log.debug("using bilinear interpolation");
            half.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

        half.drawImage(inImage, 0, 0, width, height, 0, 0, inImage.getWidth(),
                inImage.getHeight(), null);
        if (log.isDebugEnabled()) {
            long timestop = System.currentTimeMillis();
            long elapsed = timestop - timer;
            log.debug("Time elapsed is {}" , elapsed);
        }
        half.dispose();
        return halfImage;
    }

    protected BufferedImage rotateImage(BufferedImage inImage, int rotate) {
        long timer = System.currentTimeMillis();

        if (rotate == 0) {
            return inImage;
        }
        if ((rotate != 1) && (rotate != -1)) {
            throw new IllegalArgumentException("rotate must be 1 or -1");
        }
        log.debug("Original image size is {}, {}" , inImage.getWidth() ,
                 inImage.getHeight());
        double i0 = inImage.getWidth() / 2.0D;
        double j0 = inImage.getHeight() / 2.0D;
        log.debug("Anchor point is {}, {}" , i0 , j0);
        AffineTransform trans = AffineTransform.getQuadrantRotateInstance(rotate,
                i0, j0);
        double jdiff;

        if (rotate < 0) {
            jdiff = j0 - i0;
        } else {
            jdiff = i0 - j0;
        }
        log.debug("translation distance is {}" , jdiff);
        trans.translate(jdiff, jdiff);
        int width = inImage.getHeight();
        int height = inImage.getWidth();
        log.debug("new width: {} new height: {}" , width ,  height);
        BufferedImage outImage = createBufferedImage(width, height, inImage);
        Graphics2D graph = getGraphics(outImage);

        graph.drawRenderedImage(inImage, trans);
        if (log.isDebugEnabled()) {
            long timestop = System.currentTimeMillis();
            long elapsed = timestop - timer;
            log.debug("Rotation Time elapsed is {}" , elapsed);
        }
        graph.dispose();
        return outImage;
    }

    public Dimension computeSizeFromAspectRatio(Rectangle box, Dimension size) {
        Validate.isTrue((size.height > 0) || (size.width > 0));
        int height = 0;
        int width = 0;

        if (size.height == 0) {
            log.debug("computing width from aspect ratio");
            width = size.width;
            if (box.height > 0) {
                double scale = size.getWidth() / box.getWidth();
                log.debug("scale is {}" ,scale);
                height = (int)Math.round(box.getHeight() * scale);
            } else {
                height = 1;
            }
        } else if (size.width == 0) {
            height = size.height;
            if (box.height > 0) {
                double scale = size.getHeight() / box.getHeight();
                width = (int)Math.round(box.getWidth() * scale);
            } else {
                width = 1;
            }
        }
        if ((height > 1) || (width > 1)) {
            size = new Dimension(width, height);
        }
        return size;
    }

    protected boolean isTransparent(BufferedImage image) {
        return image.getTransparency() != 1;
    }

    protected BufferedImage filterColors(BufferedImage image) {
        if ((image.getColorModel() instanceof IndexColorModel)) {
            IndexColorModel icm = (IndexColorModel) image.getColorModel();
            int transparentPixel = icm.getTransparentPixel();
            log.debug("Transparent Pixel is {}" , transparentPixel);
            int argb = icm.getRGB(transparentPixel);
            log.debug("Transparent RGB is {}" , Integer.toHexString(argb));
            int tr = icm.getRed(transparentPixel);
            int tg = icm.getGreen(transparentPixel);
            int tb = icm.getBlue(transparentPixel);
            int ta = icm.getAlpha(transparentPixel);
            log.debug("Transparent RGB as colors {} {} {}" , tr , tg , tb);
            log.debug("Transparent Alpha is {}" ,ta);
            int count = 0;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    WritableRaster raster = image.getRaster();
                    int[] data = new int[1];
                    int pix = raster.getPixel(i, j, data)[0];
                    if (pix == transparentPixel)
                        continue;
                    int irgb = icm.getRGB(pix);
                    int ir = icm.getRed(pix);
                    int ig = icm.getGreen(pix);
                    int ib = icm.getBlue(pix);
                    int ia = icm.getAlpha(pix);
                    if ((ia < 100) && (count++ < 100))
                        log.debug("data pixel {} {} {} {} {}", i , j , pix ,
                                Integer.toHexString(irgb) , ia);
                    if (ia >= 10)
                        continue;
                    data[0] = transparentPixel;
                    raster.setPixel(i, j, data);
                }
            }

        }

        return image;
    }

    protected BufferedImage createBufferedImage(int width, int height,
                                                BufferedImage baseImage) {
        boolean transparency = isTransparent(baseImage);
        log.debug("image transparency is {}" , transparency);
        int imageType = baseImage.getType();
        log.debug("image type is {}" , imageType);
        GraphicsConfiguration gc = baseImage.createGraphics()
                .getDeviceConfiguration();
        BufferedImage outImage = gc.createCompatibleImage(width, height, 2);
        if (imageType == 0) {
            outImage = new BufferedImage(width, height, 6);
        } else if ((imageType == 13) || (imageType == 12)) {
            IndexColorModel cm = (IndexColorModel) baseImage.getColorModel();
            outImage = new BufferedImage(width, height, imageType, cm);
        } else {
            outImage = new BufferedImage(width, height, imageType);
        }
        return outImage;
    }

    protected Graphics2D getGraphics(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);

        ColorModel cm = image.getColorModel();
        if (((cm instanceof IndexColorModel)) && (cm.hasAlpha())) {
            log.debug("clearing transparent image");
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.setComposite(AlphaComposite.Src);
        }
        return g2d;
    }

    protected IIOMetadata getTransparentMetadata(BufferedImage image,
                                                 ImageWriter writer, ImageWriteParam writeParam)
            throws IIOInvalidTreeException {
        IndexColorModel cm = (IndexColorModel) image.getColorModel();
        int transparentColor = cm.getTransparentPixel();
        log.debug("transparent color is {}" , transparentColor);
        ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(image);
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageTypeSpecifier,
                writeParam);
        String metaFormatName = metadata.getNativeMetadataFormatName();
        log.debug("meta format name is {}" , metaFormatName);
        IIOMetadataNode root = (IIOMetadataNode) metadata
                .getAsTree(metaFormatName);
        IIOMetadataNode gce = getChildMetadataNode(root,
                "GraphicControlExtension");
        gce.setAttribute("transparentColorFlag", "true");
        gce.setAttribute("transparentColorIndex",
                String.valueOf(transparentColor));

        logXml(root);

        metadata.mergeTree(metaFormatName, root);
        return metadata;
    }

    protected void logXml(Element element) {
        if (!log.isDebugEnabled())
            return;
        try {

            TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setAttribute("indent-number", 2);
            Transformer idTransform = transFactory.newTransformer();
            idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
            idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
            // Apache default indentation is 0
            idTransform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source input = new DOMSource(element);

            StringWriter out = new StringWriter();
            StreamResult output = new StreamResult(out);

            idTransform.transform(input, output);
            log.debug(out);
        } catch (TransformerException e) {
            log.error("Transformer Exception: {}" , PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
        }
    }

    protected IIOMetadataNode getChildMetadataNode(IIOMetadataNode root,
                                                   String nodeName) {
        int nNodes = root.getLength();
        for (int i = 0; i < nNodes; i++) {
            IIOMetadataNode node = (IIOMetadataNode) root.item(i);
            if (!node.getNodeName().equalsIgnoreCase(nodeName))
                continue;
            log.debug("found node {}" , nodeName);
            return node;
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        root.appendChild(node);
        log.debug("created new node {}" , nodeName);
        return node;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public float getCompression() {
        return this.compression;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public int getStepFactor() {
        return this.stepFactor;
    }

    public void setStepFactor(int stepFactor) {
        this.stepFactor = stepFactor;
    }

    public int getMaxInterpolationSize() {
        return this.maxInterpolationSize;
    }

    public void setMaxInterpolationSize(int maxInterpolationSize) {
        this.maxInterpolationSize = maxInterpolationSize;
    }

}
