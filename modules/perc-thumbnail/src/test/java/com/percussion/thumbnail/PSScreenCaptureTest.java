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
