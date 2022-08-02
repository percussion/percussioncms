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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Validates the Redirect Entry List
 */
public class TestRedirectList {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    public static void createTempFolder() throws IOException {
        tempFolder.create();
    }
    @Before
    public void setup() throws IOException {
        createTempFolder();
    }

    public static String getRedirectSourceFilePath() throws IOException {
        InputStream in = TestRedirectList.class.getClassLoader()
                .getResourceAsStream("test-redirects.csv");

        assert in != null;

        File redirects = tempFolder.newFile("redirects.csv");
        FileOutputStream out = new FileOutputStream(redirects);

        IOUtils.copy(in,out);

        in.close();
        out.close();

        return redirects.getAbsolutePath();
    }

    @Test
    public void testRedirectList() throws IOException {

        PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(getRedirectSourceFilePath());

        assertNotNull(list);
        assertTrue(list.size()>0);
        for(PSPercussionRedirectEntry e : list){
            System.out.println(e.toString());
        }
    }
}
