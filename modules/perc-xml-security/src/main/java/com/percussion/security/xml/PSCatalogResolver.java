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
        super.getCatalog().getCatalogManager().debug.setDebug(99);
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
        super.getCatalog().getCatalogManager().debug.setDebug(99);
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
        super.getCatalog().getCatalogManager().debug.setDebug(99);
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
                    href,base, e.getMessage());
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
