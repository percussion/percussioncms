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
import static org.junit.Assert.assertTrue;


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
