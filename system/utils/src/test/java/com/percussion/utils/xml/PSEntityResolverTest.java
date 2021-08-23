/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.utils.xml;

import com.percussion.security.xml.PSCatalogResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PSEntityResolverTest {

    private static final Logger log = LogManager.getLogger(PSEntityResolverTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = TemporaryFolder.builder().build();
    private File testInstallRoot;

    @Before
    public void setup() throws IOException {
        testInstallRoot = temporaryFolder.newFolder("testInstallRoot");
        System.setProperty("rxdeploydir",testInstallRoot.getAbsolutePath());
    }

    @Test
    @Ignore("TODO: Update to use the test XML Catalog")
    public void testExternalEntityOutsideOfInstall() throws IOException {
        PSCatalogResolver resolver = new PSCatalogResolver();
        InputSource src = resolver.resolveEntity("-//W3C//ENTITIES_Latin_1_for_XHTML//EN","https://www.percussion.com/DTD/HTMLlat1x.ent");

        assertNotNull(src);

        log.info("Resolved {} to SYSTEM of: {}",src.getPublicId(),src.getSystemId());
        log.info("=============================");
        assertNotNull(src.getByteStream());
        log.info("{}",inputSourceToString(src));
        assertEquals("-//W3C//ENTITIES_Latin_1_for_XHTML//EN",src.getPublicId());



    }

    private String inputSourceToString(InputSource src) throws IOException {
        InputStream r=src.getByteStream();

        StringBuilder b=new StringBuilder();
        int c;
        while((c=r.read())>-1)
            b.appendCodePoint(c);

        return b.toString();
    }
}
