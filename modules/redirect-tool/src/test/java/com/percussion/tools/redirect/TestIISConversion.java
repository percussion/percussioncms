/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.tools.redirect;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.percussion.tools.redirect.TestRedirectList.createTempFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestIISConversion {

    @Before
    public void setup() throws IOException {
        createTempFolder();
    }

    @Test
    public void testIISConversion() throws IOException {
        PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(
                TestRedirectList.getRedirectSourceFilePath());

        PSIISRedirectConverter cvt = new PSIISRedirectConverter();

        assertEquals(PSIISRedirectConverter.FILENAME, cvt.getFilename());

        String outFolder = TestRedirectList.tempFolder.getRoot().getAbsolutePath();
        int count = cvt.convertRedirects(list,outFolder);
        assertTrue("Count must be greater than 0",count>0);

    }

    @Test
    public void testGetFileName(){
        PSIISRedirectConverter cvt = new PSIISRedirectConverter();

        assertEquals(PSIISRedirectConverter.FILENAME, cvt.getFilename());

    }
}
