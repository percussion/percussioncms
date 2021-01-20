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
