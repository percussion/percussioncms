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

package com.percussion.security.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;

import java.io.IOException;

import static com.percussion.security.xml.PSSecureXMLUtils.getNoOpSource;

public class PSXMLEntityResolverWrapper implements XMLEntityResolver {

    private CatalogResolver resolver = new CatalogResolver();
    private static final Logger log = LogManager.getLogger(PSXMLEntityResolverWrapper.class);



    private XMLInputSource getXmlInput(InputSource is){
        XMLInputSource source = new XMLInputSource(is.getPublicId(),is.getSystemId(),null);
        source.setByteStream(is.getByteStream());
        source.setCharacterStream(is.getCharacterStream());
        source.setEncoding(is.getEncoding());
        return source;
    }

    /**
     * Resolves an external parsed entity. If the entity cannot be
     * resolved, this method should return null.
     *
     * @param resourceIdentifier location of the XML resource to resolve
     * @throws XNIException Thrown on general error.
     * @throws IOException  Thrown if resolved entity stream cannot be
     *                      opened or some other i/o error occurs.
     * @see XMLResourceIdentifier
     */
    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException {
        InputSource is = resolver.resolveEntity(resourceIdentifier.getPublicId(),resourceIdentifier.getLiteralSystemId());
        try{
            if(is == null){
                log.warn("Unable to resolve external resource from local XML Catalog.  PUBLIC: {} SYSTEM_ID: {}",
                        resourceIdentifier.getPublicId(),resourceIdentifier.getLiteralSystemId());
                return getXmlInput(getNoOpSource());
            }
        }catch(Exception e){
            log.warn("Error resolving external resource from local XML Catalog.  PUBLIC: {} SYSTEM_ID: {} Error:{}",
                    resourceIdentifier.getPublicId(),resourceIdentifier.getLiteralSystemId(), e.getMessage());
            return getXmlInput(getNoOpSource());
        }

        //We were able to resolve from the local Catalog so this resource is ok to return.
        XMLInputSource source = new XMLInputSource(is.getPublicId(),is.getSystemId(),null);
        source.setByteStream(is.getByteStream());
        source.setCharacterStream(is.getCharacterStream());
        source.setEncoding(is.getEncoding());
        return source;
    }
}
