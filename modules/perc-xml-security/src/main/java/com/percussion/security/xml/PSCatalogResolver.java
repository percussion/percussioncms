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
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import static com.percussion.security.xml.PSSecureXMLUtils.getNoOpSource;

public class PSCatalogResolver extends CatalogResolver{

    private static final Logger log = LogManager.getLogger(PSCatalogResolver.class);
    private IPSInternalRequestURIResolver internalRequestURIResolver = null;

    public IPSInternalRequestURIResolver getInternalRequestURIResolver() {
        return internalRequestURIResolver;
    }

    public void setInternalRequestURIResolver(IPSInternalRequestURIResolver internalRequestURIResolver) {
        this.internalRequestURIResolver = internalRequestURIResolver;
    }

    /**
     * Constructor
     */
    public PSCatalogResolver() {
        super();
    }

    /**
     * Constructor
     *
     * @param privateCatalog
     */
    public PSCatalogResolver(boolean privateCatalog) {
        super(privateCatalog);
    }

    /**
     * Constructor
     *
     * @param manager
     */
    public PSCatalogResolver(CatalogManager manager) {
        super(manager);
    }



    /**
     * Return the underlying catalog
     */
    @Override
    public Catalog getCatalog() {
        return super.getCatalog();
    }

    /**
     * Implements the guts of the <code>resolveEntity</code> method
     * for the SAX interface.
     *
     * <p>Presented with an optional public identifier and a system
     * identifier, this function attempts to locate a mapping in the
     * catalogs.</p>
     *
     * <p>If such a mapping is found, it is returned.  If no mapping is
     * found, null is returned.</p>
     *
     * @param publicId The public identifier for the entity in question.
     *                 This may be null.
     * @param systemId The system identifier for the entity in question.
     *                 XML requires a system identifier on all external entities, so this
     *                 value is always specified.
     * @return The resolved identifier (a URI reference).
     */
    @Override
    public String getResolvedEntity(String publicId, String systemId) {
        //Only want this if debug is explicitly enabled.
        if (log.isDebugEnabled()) {
            super.getCatalog().getCatalogManager().debug.setDebug(99);
        }
        return super.getResolvedEntity(publicId, systemId);
    }

    /**
     * Implements the <code>resolveEntity</code> method
     * for the SAX interface.
     *
     * <p>Presented with an optional public identifier and a system
     * identifier, this function attempts to locate a mapping in the
     * catalogs.</p>
     *
     * <p>If such a mapping is found, the resolver attempts to open
     * the mapped value as an InputSource and return it. Exceptions are
     * ignored and null is returned if the mapped value cannot be opened
     * as an input source.</p>
     *
     * <p>If no mapping is found (or an error occurs attempting to open
     * the mapped value as an input source), null is returned and the system
     * will use the specified system identifier as if no entityResolver
     * was specified.</p>
     *
     * @param publicId The public identifier for the entity in question.
     *                 This may be null.
     * @param systemId The system identifier for the entity in question.
     *                 XML requires a system identifier on all external entities, so this
     *                 value is always specified.
     * @return An InputSource for the mapped identifier, or null.
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {

        //Only want this if debug is explicitly enabled.
        if (log.isDebugEnabled()) {
            super.getCatalog().getCatalogManager().debug.setDebug(99);
        }

        InputSource is=null;
        try {
            is = super.resolveEntity(publicId, systemId);

        }catch(Exception e){
            return getNoOpSource();
        }

        if (is == null) {
            return getNoOpSource();
        }else{
            return is;
        }

    }

    /**
     * JAXP URIResolver API
     *
     * @param href
     * @param base
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException {

        if(href==null || "".equalsIgnoreCase(href)){
            return null;
        }
        //Only want this if debug is explicitly enabled.
        if (log.isDebugEnabled()) {
            super.getCatalog().getCatalogManager().debug.setDebug(99);
        }

        Source s = null;

        //Process any internal requests to the XML application server first
        if(internalRequestURIResolver!= null){
            s = internalRequestURIResolver.resolve(href,base);
            // If we got a result, return it.
            if(s != null){
                return s;
            }
        }
        try {
            s = super.resolve(href, null);
        }catch(Exception e){
            log.warn("Error resolving external resource from local XML Catalog.  href: {} base: {} Error:{}",
                    href,base, PSExceptionUtils.getMessageForLog(e));
            throw new TransformerException(e);
        }
        if(s == null){
            log.warn("Error resolving external resource from local XML Catalog.  href: {} base: {}",
                    href,base);
            throw new TransformerException("Resource was not resolved in the local XML catalog. Un-trusted external references are not allowed. href:" + href + " base:" + base);
        }
        return s;
    }
}
