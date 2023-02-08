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

package com.percussion.xml;

import com.percussion.security.xml.PSCatalogResolver;
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
            PSCatalogResolver cr = new PSCatalogResolver();

            //TODO: This may need moved to perc-system to handle resolving internal XML server requests.
            transformer.setURIResolver(cr);

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
