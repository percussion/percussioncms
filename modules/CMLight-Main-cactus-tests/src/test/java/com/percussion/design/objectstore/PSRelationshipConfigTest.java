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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PSRelationshipConfigTest {

    /**
     * Get the relationship configuration set used for testing purposes.
     *
     * @return the relationship configuration set, never <code>null</code> or
     *    empty.
     * @throws Exception for any error reading the relationship configuration
     *    set.
     */
    public static PSRelationshipConfigSet getConfigs() throws Exception
    {
        final String PATH = "../../testing/relationshipConfigurations.xml";
        Element configXml = loadXmlResource(PATH, PSRelationshipConfigTest.class);
        return new PSRelationshipConfigSet(configXml, null, null);
    }

    /**
     * Loads an XML document from a path that is relative to the suppliedt class.
     *
     * @param path the relative path to the specified class, it may not be
     *    <code>null</code> or empty.
     * @param cz the class that the above path is relative to, never
     *    <code>null</code>.
     *
     * @return the root element of the document, never <code>null</code>.
     */
    public static Element loadXmlResource(String path, Class cz) throws Exception
    {
        if (path == null || path.trim().length() == 0)
            throw new IllegalArgumentException("path may not be null or empty.");
        if (cz == null)
            throw new IllegalArgumentException("cz may not be null.");

        InputStream in = cz.getResourceAsStream(path);
        if (in == null)
        {
            throw new FileNotFoundException(
                    "Resource \"" + path + "\" was not found from " + cz);
        }
        Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
        in.close();
        return doc.getDocumentElement();
    }


}
