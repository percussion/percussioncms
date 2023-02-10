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
