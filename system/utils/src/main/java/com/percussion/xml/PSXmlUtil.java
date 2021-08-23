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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.xml;

import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class PSXmlUtil {

    /**
     * Returns the provided document as XML formatted, UTF8 encoded string with
     * indentations.
     *
     * @param node the {@link Node} to be returned as string, may be
     *             <code>null</code>.
     * @return the provided document as string, the error message in case of
     * IOExceptions, an empty String if the provided document is
     * <code>null</code>.
     */
    // Replaces old PSXmlDomUtil.toString(Node node)
    public static String toString(Node node) {

        if (node == null)
            return "";

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try {
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(node);
            StringWriter writer = new StringWriter();

            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Cannot convert XML Node to String", e);
        }

    }
}
