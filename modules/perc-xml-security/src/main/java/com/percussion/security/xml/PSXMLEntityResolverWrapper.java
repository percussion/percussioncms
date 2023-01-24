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

package com.percussion.security.xml;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;

import static com.percussion.security.xml.PSSecureXMLUtils.getNoOpSource;

public class PSXMLEntityResolverWrapper implements XMLEntityResolver{
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
                    resourceIdentifier.getPublicId(),resourceIdentifier.getLiteralSystemId(), PSExceptionUtils.getMessageForLog(e));
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
