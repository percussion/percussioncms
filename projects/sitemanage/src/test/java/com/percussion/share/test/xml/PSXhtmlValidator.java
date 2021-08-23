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
package com.percussion.share.test.xml;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import com.percussion.share.test.PSMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Validates XHTML
 * Inspired by:
 * <a href="http://nutrun.com/weblog/xhtmlvalidator-validate-xhtml-in-java/">
 * XhtmlValidator – Validate XHTML in Java
 * </a>
 * <p>
 * <strong>DO NOT REUSE THIS OBJECT</strong>
 * Create a new object each time for {@link #isValid(InputStream)}.
 * 
 * @author adamgent
 * @see #isValid(InputStream)
 * @see PSMatchers#validXhtml()
 */
public class PSXhtmlValidator {

    private static final Logger log = LogManager.getLogger(PSXhtmlValidator.class);

    private DocumentBuilder parser;
    private PSXhtmlErrorHandler handler = new PSXhtmlErrorHandler();

    public PSXhtmlValidator() {
        initializeParser();
    }

    /**
     * 
     * @param in never <code>null</code>.
     * @return <code>true</code> if valid.
     */
    public boolean isValid(final InputStream in) {
        validate(in);
        return (handler.getErrors().size() == 0);
    }
    
    public Collection<SAXParseException> getErrors() {
        return unmodifiableCollection(handler.getErrors());
    }

    private void validate(final InputStream in) {
        notNull(in);
        try {
            parser.parse(in);
        } catch (SAXException e) {
            //Ignore - Let error handler handle it
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeParser() {
        try {
            DocumentBuilderFactory factory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                    new PSXmlSecurityOptions(
                            true,
                            true,
                            true,
                            false,
                            true,
                            false
                    )
            );
            factory.setValidating(true);
            parser = factory.newDocumentBuilder();
            parser.setEntityResolver(new PSXhtmlEntityResolver());
            parser.setErrorHandler(handler);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }
    
    
    public static class PSXhtmlErrorHandler implements ErrorHandler {
        private Collection<SAXParseException> errors = new ArrayList<SAXParseException>();

        public void warning(SAXParseException e) {
            errors.add(e);
        }

        public void error(SAXParseException e) {
            errors.add(e);
        }

        public void fatalError(SAXParseException e) {
            errors.add(e);
        }

        public Collection<SAXParseException> getErrors()
        {
            return errors;
        }
        
    }
    
    public static class PSXhtmlEntityResolver implements EntityResolver {

        private static final String DTD_ROOT = 
            "/" + PSXhtmlValidator.class.getPackage().getName().replaceAll("\\.", "/") + "/dtds";

        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            final String dtd = pathToDtd(systemId);
            final InputStream stream = PSXhtmlValidator.class.getResourceAsStream(dtd);
            return new InputSource(new InputStreamReader(stream));
        }

        private String pathToDtd(final String systemId) {
            return DTD_ROOT + "/" + dtdFilename(systemId);
        }

        private String dtdFilename(final String systemId) {
            String entity = systemId.substring("http://".length());
            entity = entity.substring("file:///".length());
            String[] pathElements = entity.split("/");
            return pathElements[pathElements.length - 1];
        }
    }
}
