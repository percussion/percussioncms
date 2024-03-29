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
