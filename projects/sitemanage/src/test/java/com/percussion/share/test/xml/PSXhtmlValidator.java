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
package com.percussion.share.test.xml;

import com.percussion.error.PSExceptionUtils;
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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
