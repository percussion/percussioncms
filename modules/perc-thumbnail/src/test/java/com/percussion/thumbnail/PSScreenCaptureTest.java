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

package com.percussion.thumbnail;


import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class PSScreenCaptureTest {

    protected static final Logger log = LogManager.getLogger();

    public static final String RXDEPLOYDIR = "rxdeploydir";

    public static File temp;


    @Before
    public void before()
            throws IOException
    {
/*
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if(!(temp.delete()))
        {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        System.setProperty(RXDEPLOYDIR, temp.getAbsolutePath());
        log.info("Temp folder set to " + System.getProperty(RXDEPLOYDIR));
  */
    }

    @After
    public void after() {
    /*    String path = System.getProperty(RXDEPLOYDIR);
        log.info("Cleaup folder "+path);
        if (temp.exists())
            temp.delete();
*/
    }


    @Test
    public void generateEmptyThumb() throws IOException {
            File file = new File(System.getProperty(RXDEPLOYDIR),"emptythumb.jpg");
            log.info("Creating empty thumb to "+file.getAbsolutePath());
            PSScreenCapture.generateEmptyThumb(file.getAbsolutePath());
            assertTrue(file.exists());
            BufferedImage bimg = ImageIO.read(file);
            assertNotNull("File "+file.getAbsolutePath()+" is not an image",bimg);
    }

    @Test
    public void takeCapture() throws IOException {

        capture(1024,2048);
        capture(1024,512);
        capture(100,100);

    }

    public void capture(int height, int width) throws IOException {
        File file = new File(System.getProperty(RXDEPLOYDIR),"testimg_"+height+"_"+width+".jpg");
        log.info("Taking capture to "+file.getAbsolutePath());
        PSScreenCapture.takeCapture("https://www.percussion.com",file.getAbsolutePath(), width,height);
        assertTrue(file.exists());
        BufferedImage bimg = ImageIO.read(file);

        assertEquals(height,bimg.getHeight());
        assertEquals(width,bimg.getWidth());
    }

}
