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

package com.percussion.tablefactory;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class PSJdbcImportExportHelperTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String rxdeploydir;

    @Before
    public void setup() throws IOException {

        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }

    @Test
    public void testGetOptions() throws IOException {

        File props = temporaryFolder.newFile("db.properties");
        props.deleteOnExit();
        FileOutputStream out = new FileOutputStream(props);

        IOUtils.copy(this.getClass().getResourceAsStream("/com/percussion/tablefactory/db.properties"),out);

        String args[] = {"-dbexport","-dbprops",props.getAbsolutePath(), "-storagepath",temporaryFolder.getRoot().getAbsolutePath(),"-tablestoskip","PSX_PUBLICATION_DOC,PSX_PUBLICATIONSTATUS,PSX_PUBLICATION_SITE_ITEM,CONTENTSTATUSHISTORY_BAK,PSX_CONTENTCHANGEEVENT_BAK,PSX_SEARCHINDEXQUEUE"};
        Map<String,String> options = PSJdbcImportExportHelper.getOptions(args);

        assertNotNull(options);

        assertEquals("-dbexport",options.get("dboption"));
        assertEquals(props.getAbsolutePath(),options.get("-dbprops"));
        assertEquals(temporaryFolder.getRoot().getAbsolutePath(),options.get("-storagepath"));
        assertEquals("PSX_PUBLICATION_DOC,PSX_PUBLICATIONSTATUS,PSX_PUBLICATION_SITE_ITEM,CONTENTSTATUSHISTORY_BAK,PSX_CONTENTCHANGEEVENT_BAK,PSX_SEARCHINDEXQUEUE",options.get("-tablestoskip"));

}
}
