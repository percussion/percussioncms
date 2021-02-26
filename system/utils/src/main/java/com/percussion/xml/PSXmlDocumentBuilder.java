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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.xml;

import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.utils.xml.PSEntityResolver;
import com.percussion.utils.xml.PSProcessServerPageTags;
import com.percussion.utils.xml.PSSaxParseException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 * The PSXmlDocumentBuilder class is used to simplify the creation of an XML
 * document.
 *
 * @author Tas Giakouminakis
 * @version 1.0
 * @since 1.0
 */
public class PSXmlDocumentBuilder {

    /**
     * The logger for this class.
     */
    private static Logger ms_log = Logger.getLogger(PSXmlDocumentBuilder.class);

    /**
     * New line's in XML are always <code>&lt;CR&gt;&lt;LF&gt;</code>, even on
     * platforms where &lt;LF&gt; is normally used.
     */
    private static final String NEWLINE = "\r\n";

    /**
     * List of cached validating document builders, modified by calls to
     * {@link #getDocumentBuilder(boolean)} and
     * {@link #returnDocumentBuilder(DocumentBuilder)}, never <code>null</code>,
     * may be empty.
     */
    private static List<DocumentBuilder> ms_validatingBuilders = new ArrayList<DocumentBuilder>();

    /**
     * List of cached non-validating document builders, modified by calls to
     * {@link #getDocumentBuilder(boolean)} and
     * {@link #returnDocumentBuilder(DocumentBuilder)}, never <code>null</code>,
     * may be empty.
     */
    private static List<DocumentBuilder> ms_nonValidatingBuilders = new ArrayList<DocumentBuilder>();

    /**
     * List of cached document builder factories, modified by calls to
     * {@link #getDocumentBuilderFactory(boolean)} and
     * {@link #returnDocumentBuilderFactory(DocumentBuilderFactory)}, never
     * <code>null</code>, may be empty.
     */
    private static List<DocumentBuilderFactory> ms_factories = new ArrayList<DocumentBuilderFactory>();

    /**
     * Flag for allowing <code>null</code> nodes when obtaining its string
     * representation. If this flag is included and the node to convert to string
     * is <code>null</code>, then an empty string is returned. If this flag is
     * not specified and node is <code>null</code>, then
     * <code>IllegalArgumentException</code> is thrown.
     */
    public static final int FLAG_ALLOW_NULL = 1;

    /**
     * Flag for controlling the indentation. If this flag is included, the Xml
     * Document will not have indentations.
     */
    public static final int FLAG_NO_INDENT = 2;

    /**
     * Flag for controlling the Xml declaration. If this flag is included, the
     * Xml Document will have Xml declaration.
     */
    public static final int FLAG_OMIT_XML_DECL = 4;

    /**
     * Flag for controlling the DocType declaration. If this flag is included,
     * the Xml Document will have DocType declaration.
     */
    public static final int FLAG_OMIT_DOC_TYPE = 8;

    /**
     * Create a new, empty XML document.
     *
     * @return the new XML document
     */
    public static Document createXmlDocument() {
        DocumentBuilder bldr = getDocumentBuilder(false);
        Document doc = bldr.newDocument();
        returnDocumentBuilder(bldr);

        return doc;
    }

    /**
     * Create a new, empty XML document with a specified DTD reference, or no
     * reference if both the dtd and publicid are <code>null</code>.
     *
     * @param name A qualified name for the document type, also used for the root
     *           element name, must never be <code>null</code>.
     * @param dtd A url that references a dtd for this document's system id, may
     *           be <code>null</code>.
     * @param publicid A string, may be <code>null</code> that specifies this
     *           document's public id.
     *
     * @return the new XML document, never <code>null</code>
     */
    public static Document createXmlDocument(String name, URL dtd,
        String publicid) {
        if (name == null) {
            throw new IllegalArgumentException("name must never be null");
        }

        DocumentBuilder bldr = getDocumentBuilder(false);
        DOMImplementation impl = bldr.getDOMImplementation();
        String dtdpath = (dtd == null) ? null : dtd.toString();
        DocumentType type = impl.createDocumentType(name, publicid, dtdpath);
        Document doc = impl.createDocument(null, name, type);
        returnDocumentBuilder(bldr);

        return doc;
    }

    /**
     * Creates a <code>DocumentBuilderFactory</code> object and sets the
     * validation feature off.
     *
     * @return <code>DocumentBuilderFactory</code> object. Never
     *         <code>null</code>
     */
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        return getDocumentBuilderFactory(false);
    }

    /**
     * Creates a <code>DocumentBuilderFactory</code> object and sets the
     * validation feature off or on based on the validating parameter.
     *
     * @param validating if <code>true</code> sets the validation feature on
     *    else sets it to off.
     *
     * @return <code>DocumentBuilderFactory</code> object. Never
     *         <code>null</code>
     */
    public static DocumentBuilderFactory getDocumentBuilderFactory(
        boolean validating) {
        DocumentBuilderFactory dbf = null;

        dbf = (DocumentBuilderFactory) popFromCache(ms_factories);

        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
            }
        } catch (FactoryConfigurationError err) {
            dbf = null;
        }

        if (dbf == null) {
            try {
                dbf = (DocumentBuilderFactory) Class.forName(
                        "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl")
                                                    .newInstance();
            } catch (Exception e) {
                dbf = null;
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }

        dbf.setNamespaceAware(true);
        dbf.setValidating(validating);

        return dbf;
    }

    /**
     * Returns the supplied factory to the cache so it is available for use.
     *
     * @param dbf The factory, assumed not <code>null</code>.
     */
    private static void returnDocumentBuilderFactory(DocumentBuilderFactory dbf) {
        synchronized (ms_factories) {
            ms_factories.add(dbf);
        }
    }

    /**
     * Removes the last object from the supplied list while synchronizing on that
     * list, and returns that object.
     *
     * @param cache The list to get the object from, assumed not
     *           <code>null</code>, may be empty.
     *
     * @return The object, or <code>null</code> if the list was emtpy.
     */
    private static Object popFromCache(List cache) {
        Object object = null;

        synchronized (cache) {
            int size = cache.size();

            if (size > 0) {
                object = cache.remove(size - 1);
            }
        }

        return object;
    }

    /**
     * Returns a <code>DocumentBuilder</code> which is used for parsing XML
     * documents.
     *
     * @param validating if <code>true</code> sets the validation feature on
     *           else sets it off.
     * @return a <code>DocumentBuilder</code> which is used for parsing XML
     *         documents. Never <code>null</code>.
     */
    public static DocumentBuilder getDocumentBuilder(boolean validating) {
        try {
            DocumentBuilder db = null;

            if (validating) {
                db = (DocumentBuilder) popFromCache(ms_validatingBuilders);
            } else {
                db = (DocumentBuilder) popFromCache(ms_nonValidatingBuilders);
            }

            if (db == null) {
                DocumentBuilderFactory dbf = getDocumentBuilderFactory(validating);
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
                db = dbf.newDocumentBuilder();
                db.setEntityResolver(PSEntityResolver.getInstance());
                returnDocumentBuilderFactory(dbf);
            }

            return db;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the supplied builder to the cache so it is available for use. Sets
     * the builder's error handler to <code>null</code>.
     *
     * @param builder The builder to return, assumed not <code>null</code>.
     */
    private static void returnDocumentBuilder(DocumentBuilder builder) {
        builder.setErrorHandler(null);

        if (builder.isValidating()) {
            synchronized (ms_validatingBuilders) {
                ms_validatingBuilders.add(builder);
            }
        } else {
            synchronized (ms_nonValidatingBuilders) {
                ms_nonValidatingBuilders.add(builder);
            }
        }
    }

    /**
     * Create an XML document by parsing the specified input stream. Delegates to
     * {@link #createXmlDocument(InputSource, boolean)}.
     *
     * @param in the byte input stream to read from, not <code>null</code>
     * @param validate <code>true</code> to validate the document
     * @return the parsed document, never <code>null</code> but may be empty.
     *
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public static Document createXmlDocument(InputStream in, boolean validate)
        throws IOException, SAXException {
        if (null == in) {
            throw new IllegalArgumentException("Input stream may not be null");
        }

        InputSource src = new InputSource(in);
        src.setEncoding(IPSUtilsConstants.RX_STANDARD_ENC);

        return createXmlDocument(src, validate);
    }

    /**
     * Create an XML document by parsing the specified reader. Delegates to
     * {@link #createXmlDocument(InputSource, boolean)}.
     *
     * @param in the character reader to read from, not <code>null</code>
     * @param validate <code>true</code> to validate the document
     * @return the parsed document, never <code>null</code> but may be empty.
     *
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public static Document createXmlDocument(Reader in, boolean validate)
        throws IOException, SAXException {
        if (null == in) {
            throw new IllegalArgumentException("Reader may not be null");
        }

        InputSource src = new InputSource(in);

        return createXmlDocument(src, validate);
    }

    /**
     * Create an XML document by parsing the specified input source. Delegates to
     * {@link #createXmlDocument(InputSource, boolean, PSSaxErrorHandler)}.
     *
     * @param in the source to read from, not <code>null</code>
     * @param validate <code>true</code> to validate the document
     * @return the parsed document, never <code>null</code> but may be empty.
     *
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public static Document createXmlDocument(InputSource in, boolean validate)
        throws IOException, SAXException {
        if (null == in) {
            throw new IllegalArgumentException("InputSource may not be null");
        }

        PSSaxErrorHandler errHandler = new PSSaxErrorHandler();
        errHandler.throwOnFatalErrors(false);

        return createXmlDocument(in, validate, errHandler);
    }

    /**
     * Create an XML document constructed from the specified input source, and
     * print any resulting errors or warnings to the log.
     *
     * @param in the input source to use
     *
     * @param validate <code>true</code> to validate the document
     *
     * @param errorLog where errors are written.
     *
     * @throws IOException if an I/O error occurs
     *
     * @throws SAXException if a parsing error occurs
     */
    public static Document createXmlDocument(InputSource in, boolean validate,
        PrintWriter errorLog) throws IOException, SAXException {
        PSSaxErrorHandler errHandler = new PSSaxErrorHandler(errorLog);
        errHandler.throwOnFatalErrors(false);

        return createXmlDocument(in, validate, errHandler);
    }

    /**
     * Create an XML document constructed from the specified input source, with
     * custom error handling.
     *
     * @param in the input source to use. May not be <code>null</code>.
     *
     * @param validate <code>true</code> to validate the document
     *
     * @param errHandler The SAX error handler for this document. This form of
     *           the method should be used when the error handler need
     *           non-standard behavior, or must throw exceptions immediately on
     *           error. Note that the PSXSaxErrorHandler is obfuscated, and
     *           cannot be used by Extensions. May not be <code>null</code>.
     *
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     * @throws IllegalArgumentException if in or errHandler is <code>null</code>
     */
    public static Document createXmlDocument(InputSource in, boolean validate,
        PSSaxErrorHandler errHandler) throws IOException, SAXException {
        if (in == null) {
            throw new IllegalArgumentException("in may not be null");
        }

        if (errHandler == null) {
            throw new IllegalArgumentException("errHandler may not be null");
        }

        DocumentBuilder db = getDocumentBuilder(validate);
        db.setErrorHandler(errHandler);

        Document doc = db.parse(in);
        returnDocumentBuilder(db);

        /*
         * if we have fatal errors, build execption and throw them, otherwise if
         * there are non-fatal errors, build exception and throw those.
         */
        List<SAXParseException> errors = null;

        if (errHandler.numFatalErrors() > 0) {
            errors = errHandler.getFatalErrorList();
        } else if (errHandler.numErrors() > 0) {
            errors = errHandler.getErrorList();
        }

        if (errors != null) {
            PSSaxParseException ex = new PSSaxParseException(errors);
            throw ex;
        }

        return doc;
    }

    /**
     * Create an XML document for the supplied source <code>String</code>. If
     * valid properties are supplied, the source will be tidied. If the the XML
     * header and the document entities element are not present, they will be
     * added.
     *
     * @param source the source to create the document from, may be
     *           <code>null</code> or empty, in which case an empty document is
     *           returned.
     * @param serverRoot the server root for which to add the default entities
     *           including server, port and root (e.g. localhost:9992/Rhythmyx),
     *           not <code>null</code> or empty.
     * @param properties the tidy properties, if <code>null</code> the source
     *           will not be tidied. Otherwise the source will be tidied with the
     *           supplied tidy properties, may be empty.
     * @param serverPageTags the configuration used to process the server page
     *           tags, may be <code>null</code> in which case server page tags
     *           are not processed. This is only used if tidy is enabled.
     * @param encoding the encoding to use for the tidy process, UTF8 is used if
     *           <code>null</code> or an empty <code>String</code> is
     *           provided. The format is expecetd in Java format.
     * @param validate <code>true</code> to use a validating poarser,
     *           <code>false</code> otherwise.
     * @return the XML document created from the supplied source, never
     *         <code>null</code>.
     * @throws IOException for any I/O error.
     * @throws UnsupportedEncodingException if the requested encoding is invalid.
     */
    public static Document createXmlDocument(String source, String serverRoot,
        Properties properties, PSProcessServerPageTags serverPageTags,
        String encoding, boolean validate)
        throws IOException, SAXException, UnsupportedEncodingException {
        if ((source == null) || (source.trim().length() == 0)) {
            return createXmlDocument();
        }

        if ((serverRoot == null) || (serverRoot.trim().length() == 0)) {
            throw new IllegalArgumentException(
                "serverRoot cannot be null or empty");
        }

        String tidiedSource = null;

        if (properties != null) {
            tidiedSource = tidy(source, properties, serverPageTags, encoding,
                    serverRoot);

            if (serverPageTags != null) {
                tidiedSource = serverPageTags.postProcess(tidiedSource);
            }
        } else {
            tidiedSource = source;
        }

        return createXmlDocument(new InputSource(new StringReader(tidiedSource)),
            validate);
    }

    /**
     * Add the entity references required by the parser. Since we are always
     * running on the server, the current directory is the server root, and the
     * DTD directory resides immediately below it.
     *
     * @param serverRoot the server root including protocol, server, port and
     *           root (e.g. http://localhost:9992/Rhythmyx). This will be used as
     *           the suffix on the entity file paths which are of the form
     *           '/DTD/basename.ent'. So the end result will be a filename of the
     *           form 'root/DTD/basename.ent'. If null or empty is supplied, "."
     *           is used.
     * @return all default entities for the supplied server root, never
     *         <code>null</code> or empty.
     */
    public static String getDefaultEntities(String serverRoot) {
        if ((serverRoot == null) || (serverRoot.trim().length() == 0)) {
            serverRoot = ".";
        }

        return "\t<!ENTITY % HTMLlat1 SYSTEM \"" + serverRoot +
        "/DTD/HTMLlat1x.ent\">" + NEWLINE + "\t\t%HTMLlat1;" + NEWLINE +
        "\t<!ENTITY % HTMLsymbol SYSTEM \"" + serverRoot +
        "/DTD/HTMLsymbolx.ent\">" + NEWLINE + "\t\t%HTMLsymbol;" + NEWLINE +
        "\t<!ENTITY % HTMLspecial SYSTEM \"" + serverRoot +
        "/DTD/HTMLspecialx.ent\">" + NEWLINE + "\t\t%HTMLspecial;";
    }

    /**
     * Tidy the source, based on the settings in the supplied tidy properties.
     * Tidy cleans up not well formed HTML to make it parsable to the XML parser.
     * If the supplied source has an XML header, we assume its well formed XML
     * and skip the tidy process.
     *
     * @param source the source HTML as <code>String</code> that needs to be
     *           tidied, not <code>null</code> or empty.
     * @param properties the properties that configure tidy, not
     *           <code>null</code>, may be empty, in which case the default
     *           settings are used.
     * @param serverPageTags the configuration used to process the server page
     *           tags, may be <code>null</code> in which case server page tags
     *           are not processed. Server page tags define elements such as ASP
     *           or JSP tags that are not handled correc with tidy. All elements
     *           specified as server page tags will be replaced with a valid HTML
     *           comment before tidy is processed and then placed back with the
     *           original element after the tidy process.
     * @param encoding the input encoding to use for the tidy process, UTF8 is
     *           used if <code>null</code> or an empty <code>String</code> is
     *           provided. The Java format is expected. The output string is
     *           always UTF8 encoded.
     * @param serverRoot the server root for which to add the default entities
     *           including server, port and root (e.g. localhost:9992/Rhythmyx),
     *           not <code>null</code> or empty.
     * @return the tidied <code>String</code> for the supplied source, tidied
     *         with the settings supplied in properties, never <code>null</code>.
     * @throws UnsupportedEncodingException if the requested encoding is invalid.
     * @throws IOException for any I/O error.
     *
     * @deprecated this does not work, use com.percussion.xml.PSXmlDomUtils#tidyInput(com.percussion.xmldom.PSXmlDomContext, String) instead.
     */
    public static String tidy(String source, Properties properties,
        PSProcessServerPageTags serverPageTags, String encoding,
        String serverRoot) throws UnsupportedEncodingException, IOException {
        if ((source == null) || (source.trim().length() == 0)) {
            throw new IllegalArgumentException("source cannot be null or empty");
        }

        if (properties == null) {
            throw new IllegalArgumentException("properties cannot be null");
        }

        if ((serverRoot == null) || (serverRoot.trim().length() == 0)) {
            throw new IllegalArgumentException(
                "serverRoot cannot be null or empty");
        }

        /**
         * If the supplied source claims it's XML, we do not tidy and return what
         * we received.
         */
        int piStart = source.indexOf("<?xml");

        if (piStart == 0) {
            return source;
        }

        Tidy tidy = new Tidy();
        tidy.setConfigurationFromProps(properties);

        StringWriter tidyErrors = new StringWriter();
        tidy.setErrout(new PrintWriter(tidyErrors));

        if ((encoding == null) || (encoding.trim().length() == 0)) {
            encoding = "UTF8";
        }

        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            String preProcessed = source;

            if (serverPageTags != null) {
                preProcessed = serverPageTags.preProcess(preProcessed);
            }

            is = new ByteArrayInputStream(preProcessed.getBytes(encoding));
            tidy.parseDOM(is, os);

            if (tidy.getParseErrors() > 0) {
                throw new RuntimeException(tidyErrors.toString());
            }

            /**
             * Return the tidied source with the XML header and the default
             * entitied added.
             */
            return os.toString("UTF8") +
            "<?xml version='1.0' encoding=\"UTF-8\"?>" + NEWLINE +
            "<!DOCTYPE html [" + getDefaultEntities(serverRoot) + "]>" +
            NEWLINE + NEWLINE;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) { /* ignore */
                }
            }

            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (Exception e) { /* ignore */
                }
            }
        }
    }

    /**
     * Create an error response document using the PSXError format. The
     * definition for this is:
     *
     * <PRE><CODE> &lt;ELEMENT PSXError (message, exceptionClass)&gt; &lt;ELEMENT
     * message (#PCDATA)&gt; &lt;ELEMENT exceptionClass (#PCDATA)&gt;
     * &lt;/PSXError&gt; </CODE></PRE>
     *
     * @param t the exception to report
     * @param loc the locale to use when formatting the error text
     */
    public static Document createErrorDocument(Throwable t, Locale loc) {
        if (loc != null) {
            ;
        }

        /* and report it to the user */
        Document respDoc = createXmlDocument();
        Element root = createRoot(respDoc, "PSXError");

        // we used to check for instanceof PSException so we could throw the
        // error message localized for the user, but we needed to break the
        // dependency on the error package.
        addElement(respDoc, root, "message", t.getLocalizedMessage());
        addElement(respDoc, root, "exceptionClass", t.getClass().getName());

        return respDoc;
    }

    /**
     * Calls {@link #createRoot(Document, String, String)  createRoot(doc, null,
     * rootName)}
     */
    public static Element createRoot(Document doc, String rootName) {
        return createRoot(doc, null, rootName);
    }

    /**
     * Calls {@link #createRoot(Document, String, String, String)
     * createRoot(doc, namespace, null, rootName)}
     */
    public static Element createRoot(Document doc, String namespace,
        String rootName) {
        return createRoot(doc, namespace, null, rootName);
    }

    /**
     * Create the root node for an XML document. There can be only one root node
     * for an XML document.
     * <p>
     * NOTE: 06/07/2004 If the namespace is required, then it is a good idea to
     * have an non-empty alias; otherwise some of the XML serializer may not be
     * able to handle the default namespace, such as the transformer of saxon.
     * This method always adds an namespace equivalent attribute as a workaround
     * for some of the faulty XML serializer, such as the transformer from xalan.
     *
     * @param doc the XML document to add the element to, may not be
     *           <code>null</code>.
     * @param namespace The namespace to use, may be <code>null</code> or empty
     *           to specify no namespace.
     * @param alias The alias to use for the namespace. This is appended onto the
     *           element name when creating the element e.g.
     *           <code>alias:rootName</code>. Ignored if <code>null</code>
     *           or empty.
     * @param rootName the tag name for the root node, may not be
     *           <code>null</code> or empty.
     *
     * @return the root node, never <code>null</code>.
     */
    public static Element createRoot(Document doc, String namespace,
        String alias, String rootName) {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if ((rootName == null) || (rootName.trim().length() == 0)) {
            throw new IllegalArgumentException(
                "rootName may not be null or empty");
        }

        Element root = doc.createElementNS(namespace,
                aliasTagName(alias, rootName));

        // namespace equivalent attribute to make sure it works for all XML
        // serializers. This is a workaround for the transformer of xalan.
        if ((alias != null) && (alias.trim().length() > 0) &&
                (namespace != null) && (namespace.trim().length() > 0)) {
            root.setAttribute("xmlns:" + alias, namespace);
        }

        doc.appendChild(root);

        return root;
    }

    /**
     * Prepends the supplied namespace alias to the supplied tagname if not
     * <code>null</code> or empty.
     *
     * @param alias The namespace alias, may be <code>null</code> or empty.
     * @param tagName The tag name to which the alias is prepended, assumed not
     *           <code>null</code> or empty.
     *
     * @return <code>alias:tagName</code> if the alias is not <code>null</code>
     *         or empty, else just the supplied <code>tagName</code>.
     */
    private static String aliasTagName(String alias, String tagName) {
        String elName = ((alias != null) && (alias.trim().length() > 0))
            ? (alias + ":" + tagName) : tagName;

        return elName;
    }

    /**
     * Replace the root node in this tree with another node.
     *
     * @param doc the XML document to add the element to. May not be
     *           <code>null</code>.
     * @param newRoot the new root node. May not be <code>null</code>.
     * @return the original root node. May be <code>null</code>.
     * @throws IllegalArgumentException if doc or newRoot is <code>null</code>
     */
    public static Element replaceRoot(Document doc, Element newRoot)
        throws DOMException {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (newRoot == null) {
            throw new IllegalArgumentException("newRoot may not be null");
        }

        Element oldRoot = doc.getDocumentElement();

        if (oldRoot != null) {
            doc.removeChild(oldRoot);
        }

        if (newRoot.getOwnerDocument() == doc) {
            doc.appendChild(newRoot);
        } else {
            Node importNode = doc.importNode(newRoot, true);
            doc.appendChild(importNode);
        }

        return oldRoot;
    }

    /**
     * Replace the root node in this tree with another node and make the root
     * node and all its descendants a child of the new root node.
     *
     * @param doc the XML document to add the element to. May not be
     *           <code>null</code>.
     * @param newRoot the new root node. May not be <code>null</code>.
     * @return the original root node. May be <code>null</code>.
     * @throws IllegalArgumentException if doc or newRoot is <code>null</code>
     */
    public static Element swapRoot(Document doc, Element newRoot) {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (newRoot == null) {
            throw new IllegalArgumentException("newRoot may not be null");
        }

        Element oldRoot = null;

        if (newRoot.getOwnerDocument() != doc) {
            Node importNode = doc.importNode(newRoot, true);
            newRoot = (Element) importNode;
        }

        oldRoot = replaceRoot(doc, newRoot);
        newRoot.appendChild(oldRoot);

        return oldRoot;
    }

    /**
     * Convenience method that calls
     * {@link #addElement(Document, Element, String, String, String)
     * addElement(doc, parent, null, name, value)}
     */
    public static Element addElement(Document doc, Element parent, String name,
        String value) {
        return addElement(doc, parent, null, name, value);
    }

    /**
     * Convenience method that calls
     * {@link #addElement(Document, Element, String, String, String, String)
     * addElement(doc, parent, namespace, null, name, value)}
     */
    public static Element addElement(Document doc, Element parent,
        String namespace, String name, String value) {
        return addElement(doc, parent, namespace, null, name, value);
    }

    /**
     * Add an element with the specified value as a child of the specified node.
     *
     * @param doc the XML document to add the element to May not be
     *           <code>null</code>
     * @param parent the node which will be used as the parent of the May not be
     *           <code>null</code> new element
     * @param namespace The namespace to use, may be <code>null</code> or empty
     *           to specify no namespace.
     * @param alias The alias to use for the namespace. This is appended onto the
     *           element name when creating the element e.g.
     *           <code>alias:name</code>. Ignored if <code>null</code> or
     *           empty.
     * @param name the new element's tag name. May not be <code>null</code> or
     *           empty.
     * @param value the new element's value. May be <code>null</code> or empty
     *
     * @return Never <code>null</code>.
     *
     * @throws IllegalArgumentException if doc or parent or name is
     *            <code>null</code> or name is empty
     */
    public static Element addElement(Document doc, Element parent,
        String namespace, String alias, String name, String value) {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (parent == null) {
            throw new IllegalArgumentException("parent may not be null");
        }

        if ((name == null) || (name.trim().length() < 1)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        Element node = parent.getOwnerDocument()
                             .createElementNS(namespace,
                aliasTagName(alias, name));
        parent.appendChild(node);

        if (value == null) {
            value = "";
        } else {
            value = normalize(value);
        }

        Text textNode = node.getOwnerDocument().createTextNode(value);
        node.appendChild(textNode);

        return node;
    }

    /**
     * Convenience method that calls
     * {@link #addEmptyElement(Document, Element, String, String)
     * addEmptyElement(doc, parent, null, name)}
     */
    public static Element addEmptyElement(Document doc, Element parent,
        String name) {
        return addEmptyElement(doc, parent, null, name);
    }

    /**
     * Convenience method that calls
     * {@link #addEmptyElement(Document, Element, String, String, String)
     * addEmptyElement(doc, parent, namespace, null, name)}
     */
    public static Element addEmptyElement(Document doc, Element parent,
        String namespace, String name) {
        return addEmptyElement(doc, parent, namespace, null, name);
    }

    /**
     * Add an empty element as a child of the specified node.
     *
     * @param doc the XML document to add the element to. May not be
     *           <code>null</code>.
     * @param parent the node which will be used as the parent of the new
     *           element. May not be <code>null</code>.
     * @param namespace The namespace to use, may be <code>null</code> or empty
     *           to specify no namespace.
     * @param alias The alias to use for the namespace. This is appended onto the
     *           element name when creating the element e.g.
     *           <code>alias:name</code>. Ignored if <code>null</code> or
     *           empty.
     * @param name the new element's tag name. May not be <code>null</code> or
     *           empty.
     *
     * @return The new element, never <code>null</code>.
     *
     * @throws IllegalArgumentException if doc or parent or name is
     *            <code>null</code> or name is empty
     */
    public static Element addEmptyElement(Document doc, Element parent,
        String namespace, String alias, String name) {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (parent == null) {
            throw new IllegalArgumentException("parent may not be null");
        }

        if ((name == null) || (name.trim().length() < 1)) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        Element node = parent.getOwnerDocument()
                             .createElementNS(namespace,
                aliasTagName(alias, name));
        parent.appendChild(node);

        return node;
    }

    /**
     * Copy a tree, rooted at the specified node as a child of the specified tree
     * and node.
     *
     * @param doc the XML document to add the node(s) to May not be
     *           <code>null</code>
     * @param parent the node which will be used as the parent of the new
     *           node(s). May not be <code>null</code>
     * @param tree the node (along with its children) which will be copied into
     *           the specified doc, if <code>null</code> then this method
     *           returns immediately
     * @throws IllegalArgumentException if doc or parent or tree is
     *            <code>null</code>
     */
    public static void copyTree(Document doc, Node parent, Node tree) {
        copyTree(doc, parent, tree, true);
    }

    /**
     * Copy a tree, rooted at the specified node as a child of the specified tree
     * and node.
     *
     * @param doc the XML document to add the node(s) to. May not be
     *           <code>null</code>
     * @param parent the node which will be used as the parent of the new node
     *           May not be <code>null</code>
     * @param tree the node (along with its children) which will be copied into
     *           the specified doc, if <code>null</code> then this method
     *           returns <code>null</code> immediately without modification
     * @param bClone if the node should be cloned before adding to the document
     *           if <code>true</code> the node is cloned, otherwise not cloned.
     * @return the reference to the node added to the tree, returns
     *         <code>null</code> if the tree to be added is <code>null</code>
     * @throws IllegalArgumentException if doc or parent or tree is
     *            <code>null</code>
     */
    public static Node copyTree(Document doc, Node parent, Node tree,
        boolean bClone) {
        if (tree == null) {
            return null;
        }

        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (parent == null) {
            throw new IllegalArgumentException("parent may not be null");
        }

        if (tree.getOwnerDocument() == doc) {
            if (bClone) {
                return parent.appendChild(tree.cloneNode(true));
            } else {
                return parent.appendChild(tree);
            }
        } else {
            Node importNode = doc.importNode(tree, true);

            return parent.appendChild(importNode);
        }
    }

    /**
     * Write the XML document to the specified output stream.
     *
     * @param doc the XML document to be written May not be <code>null</code>
     * @param out the output stream to write to May not be <code>null</code>
     * @throws IllegalArgumentException if doc or out is <code>null</code>
     */
    public static void write(Document doc, OutputStream out)
        throws java.io.IOException {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        if (out == null) {
            throw new IllegalArgumentException("out may not be null");
        }

        Writer w = new OutputStreamWriter(out, IPSUtilsConstants.RX_JAVA_ENC);
        PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
        walker.write(w, true, false, false, IPSUtilsConstants.RX_STANDARD_ENC);
    }

    /**
     * Write the XML document to the specified output stream. If the document
     * does not have document type, it writes the document type with specified
     * dtd.
     *
     * @param doc the XML document to be written, may not be <code>null</code>
     * @param out the output stream to write to, may not be <code>null</code>.
     *           Caller is responsible to close the stream.
     * @param dtd the url of dtd, may not be <code>null</code>. If the
     *           document does not have 'DOCTYPE' element, this dtd is used for
     *           adding this element.
     *
     * @throws IllegalArgumentException if any param is invalid.
     * @throws IOException if any io error occurs while writting the document.
     *
     * @deprecated Call
     * {@link #createXmlDocument(String, URL, String)} to create a document with
     *        a specific DTD instead. Then call the regular write methods, which
     *        will include the DOCTYPE reference.
     */
    public static void write(Document doc, OutputStream out, URL dtd)
        throws IOException {
        if (dtd == null) {
            throw new IllegalArgumentException("dtd can not be null");
        }

        write(doc, out, dtd.toExternalForm());
    }

    /**
     * Write the XML document to the specified output stream. If the document
     * does not have document type, it writes the document type with specified
     * dtd. Uses rhythmyx standard encoding.
     *
     * @param doc the XML document to be written, may not be <code>null</code>.
     * @param out the output stream to write to, may not be <code>null</code>.
     *           Caller is responsible to close the stream.
     * @param dtdPath the path of dtd file, may not be <code>null</code> or
     *           empty. If the document does not have 'DOCTYPE' element, this
     *           path is used for adding this element.
     *
     * @throws IllegalArgumentException if any param is invalid.
     * @throws IOException if any io error occurs while writting the document.
     *
     * @deprecated Call
     * {@link #createXmlDocument(String, URL, String)} to create a document with
     *        a specific DTD instead. Then call the regular write methods, which
     *        will include the DOCTYPE reference.
     */
    public static void write(Document doc, OutputStream out, String dtdPath)
        throws IOException {
        if (doc == null) {
            throw new IllegalArgumentException("doc can not be null");
        }

        if (out == null) {
            throw new IllegalArgumentException("out can not be null");
        }

        if ((dtdPath == null) || (dtdPath.trim().length() == 0)) {
            throw new IllegalArgumentException(
                "dtdPath can not be null or empty");
        }

        Writer w = new OutputStreamWriter(out, IPSUtilsConstants.RX_JAVA_ENC);
        PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);

        if (doc.getDocumentElement() != null) {
            writeXmlHeader(doc, w, IPSUtilsConstants.RX_STANDARD_ENC);

            if (doc.getDoctype() == null) {
                w.write(("<!DOCTYPE " + doc.getDocumentElement().getTagName() +
                    " SYSTEM \"" + dtdPath + "\">" +
                    System.getProperty("line.separator")));
            }
        }

        walker.write(w, true, true, true, IPSUtilsConstants.RX_STANDARD_ENC);
    }

    /**
     * Writes out the xml header with the XML version and the character encoding
     * reported by the Java VM.
     *
     * @param doc the XML document to be written Assumed not <code>null</code>.
     * @param w the output writer to use Assumed not <code>null</code>.
     * @param encoding the character encoding used by the writer. May be
     *           <code>null</code> or empty. If <code>null</code> or empty,
     *           will not write an encoding parameter
     */
    private static void writeXmlHeader(Document doc, Writer w, String encoding)
        throws IOException {
        String standalone = doc.getXmlStandalone() ? "yes" : "no";
        String xmlVer = doc.getXmlVersion();

        if ((xmlVer == null) || (xmlVer.trim().length() < 1)) {
            xmlVer = "1.0";
        }

        w.write("<?xml version='" + xmlVer + "'");

        if (encoding != null) {
            w.write(" encoding='");
            w.write(encoding);
            w.write("'");
        }

        if (standalone != null) {
            w.write(" standalone='");
            w.write(standalone);
            w.write("'");
        }

        w.write("?>\r\n");
    }

    /**
     * Write the XML document to the specified output writer.
     *
     * @param doc the XML document to be written May not be <code>null</code>.
     * @param out the output writer to use May not be <code>null</code>.
     * @throws IllegalArgumentException if doc or out is <code>null</code>
     */
    public static void write(Document doc, Writer out)
        throws java.io.IOException {
        write(doc, out, null);
    }

    /**
     * Write the XML document to the specified output writer.
     *
     * @param doc the XML document to be written May not be <code>null</code>.
     * @param out the output writer to use May not be <code>null</code>.
     * @param encoding the character encoding used by the writer. May be
     *           <code>null</code> or empty. If <code>null</code> or empty,
     *           no XML header will be written.
     * @throws IllegalArgumentException if doc or out is <code>null</code>
     */
    public static void write(Document doc, Writer out, String encoding)
        throws IOException {
        PSXmlTreeWalker w = new PSXmlTreeWalker(doc);

        w.write(out, true, false, false, encoding);
    }

    /**
     * Write the XML document to the specified output stream.
     *
     * @param node the XML document to be written
     *
     * @param out the output stream to write to
     */
    public static void write(Element node, OutputStream out)
        throws java.io.IOException {
        write(node, new OutputStreamWriter(out));
    }

    /**
     * Write the XML document to the specified output writer.
     *
     * @param node the XML document to be written
     *
     * @param out the output writer to use
     */
    public static void write(Element node, Writer out)
        throws java.io.IOException {
        PSXmlTreeWalker w = new PSXmlTreeWalker(node);
        w.write(out);
    }

    /**
     * Returns a string representation of the specified node. The returned string
     * is based on the node type:
     * <p>
     * Attribute - value of attribute CDATASection - content of the CDATA Section
     * Comment - content of the comment Document - the document tree Element -
     * the Element tree ProcessingInstruction - entire content excluding the
     * target Text - content of the text node
     * <p>
     * The following node types are currently not supported:
     * <p>
     * DocumentFragment DocumentType Entity EntityReference Notation
     * <p>
     *
     * @param node the XML node whose string representation is to be obtained,
     *           may be <code>null</code> if <code>FLAG_ALLOW_NULL</code> is
     *           included in the specified flag. If this flag is not specified
     *           and node is <code>null</code>, then
     *           <code>IllegalArgumentException</code> is thrown.
     *
     * @param flags optional flags for controlling the string representation of
     *           the node, one or more <code>FLAG_XXX</code> values OR'ed
     *           together.
     *
     * @return string representation of the specified node, never
     *         <code>null</code>, may be empty if <code>node</code> is
     *         <code>null</code> and <code>FLAG_ALLOW_NULL</code> is
     *         specified.
     */
    public static String toString(Node node, int flags) {
        boolean allowNull = ((flags & FLAG_ALLOW_NULL) == FLAG_ALLOW_NULL);

        if (node == null) {
            if (allowNull) {
                return "";
            } else {
                throw new IllegalArgumentException("node may not null");
            }
        }

        boolean indent = !((flags & FLAG_NO_INDENT) == FLAG_NO_INDENT);
        boolean omitXMLDeclaration = ((flags & FLAG_OMIT_XML_DECL) == FLAG_OMIT_XML_DECL);
        boolean omitDocumentType = ((flags & FLAG_OMIT_DOC_TYPE) == FLAG_OMIT_DOC_TYPE);

        PSXmlTreeWalker tree = null;

        if (node instanceof Document) {
            tree = new PSXmlTreeWalker((Document) node);
        } else if (node instanceof Element) {
            tree = new PSXmlTreeWalker((Element) node);
        }

        String retVal = null;

        if (tree != null) {
            StringWriter sw = null;

            try {
                sw = new StringWriter();
                tree.write(sw, indent, omitXMLDeclaration, omitDocumentType);
                retVal = sw.toString();
            } catch (IOException ioe) {
                ms_log.error("Error converting node to string",ioe);
                retVal = ioe.getLocalizedMessage();
            } finally {
                if (sw != null) {
                    try {
                        sw.close();
                    } catch (IOException ex) {
                    }
                }
            }
        } else {
            retVal = node.getNodeValue();
        }

        return retVal;
    }

    /**
     * Get a string representation of the document.
     *
     * @param doc the document
     *
     * @return the string representation
     */
    public static String toString(Document doc) {
        // convert the XML data to a string
        // TG - do not use a default size as the MS VM doesn't support it!
        java.io.StringWriter xmlDumpWriter = new java.io.StringWriter();

        try {
            PSXmlDocumentBuilder.write(doc, xmlDumpWriter);
        } catch (java.io.IOException e) {
            ms_log.error("Error converting DOM Document to string",e);
        }

        return xmlDumpWriter.toString();
    }

    /**
     * Get a string representation of the element and its children.
     *
     * @param node the element node
     *
     * @return the string representation
     */
    public static String toString(Element node) {
        // convert the XML data to a string
        // TG - do not use a default size as the MS VM doesn't support it!
        java.io.StringWriter xmlDumpWriter = new java.io.StringWriter();

        try {
            PSXmlDocumentBuilder.write(node, xmlDumpWriter);
        } catch (java.io.IOException e) {
            ms_log.error("Error converting DOM Document to string",e);
        }

        return xmlDumpWriter.toString();
    }

    /**
     * Normalizes the given string by converting crlf or cr to lf (\r\n to \n and
     * \r to \n)
     *
     * @param inStr The string to normalize, may be <code>null</code>.
     * @return The normalized string, or <code>null</code> if that is passed
     *         in.
     */
    public static String normalize(String inStr) {
        if (inStr == null) {
            return inStr;
        }

        StringBuffer inBuf = new StringBuffer(inStr);
        StringBuffer normBuf = new StringBuffer();

        // walk the input looking for our match
        for (int i = 0; i < inBuf.length(); i++) {
            char curChar = inBuf.charAt(i);

            if (curChar == '\r') {
                // first peek ahead and see if followed by a newline
                if (((i + 1) < inBuf.length()) &&
                        (inBuf.charAt(i + 1) == '\n')) {
                    // skip it
                    continue;
                } else {
                    // replace it
                    normBuf.append('\n');
                }

                continue;
            }

            // Only include valid XML characters.
            if ((curChar == 0x9) || (curChar == 0xA) || (curChar == 0xD) ||
                    ((curChar >= 0x20) && (curChar <= 0xD7FF)) ||
                    ((curChar >= 0xE000) && (curChar <= 0xFFFD)) ||
                    ((curChar >= 0x10000) && (curChar <= 0x10FFFF))) {
                normBuf.append(curChar);
            }
        }

        return normBuf.toString();
    }

    /**
     * Replace all of the TEXT nodes underneath a given element with a single new
     * text node. If the node has markup, it may not be preserved Comments, CDATA
     * sections, and other children of the node are not changed.
     *
     * @param parentDoc the document that the element belongs to. It may not be
     *           <code>null</code>.
     *
     * @param elementNode the element to be replaced. It may not be
     *           <code>null</code>.
     *
     * @param newValue the string which contains the new value. It may be
     *           <code>null</code>, then it has no effect or the value will
     *           not be changed.
     */
    public static Element replaceText(Document parentDoc, Element elementNode,
        String newValue) {
        if (parentDoc == null) {
            throw new IllegalArgumentException("parentDoc may not be null");
        }

        if (elementNode == null) {
            throw new IllegalArgumentException("elementNode may not be null");
        }

        int i;

        if (elementNode.hasChildNodes()) {
            elementNode.normalize();

            NodeList children = elementNode.getChildNodes();

            if ((children.getLength() == 1) &&
                    (children.item(0).getNodeType() == Node.TEXT_NODE)) {
                // only 1 child, and it's a TEXT. Just replace the value
                children.item(0).setNodeValue(newValue);

                return elementNode;
            }

            i = 0;

            while (i <= children.getLength()) {
                Node tempNode = children.item(i);

                if (tempNode.getNodeType() == Node.TEXT_NODE) {
                    // found a TEXT node, must delete it.
                    elementNode.removeChild(tempNode);
                } else {
                    // skip over any non TEXT nodes
                    i++;
                }
            }
        }

        Node newText = parentDoc.createTextNode(newValue);
        elementNode.appendChild(newText);

        return elementNode;
    }

    /**
     * @deprecated Use #removeElement(Element) instead
     */
    public static void removeElement(Document parentDoc, Element elementNode) {
        if (parentDoc != null) {
            ;
        }

        removeElement(elementNode);
    }

    /**
     * Remove an element from the document. This will also remove all children of
     * the element.
     *
     * @param elementNode the element to remove. It may not be <code>null</code>.
     *
     */
    public static void removeElement(Element elementNode) {
        if (elementNode == null) {
            throw new IllegalArgumentException("elementNode may not be null");
        }

        Node immediateParent = elementNode.getParentNode();

        if (immediateParent != null) {
            immediateParent.removeChild(elementNode);
        }
    }

    /**
     * Print the structure of the tree, recursively. Note this routine does not
     * print the contents, only the structure. This is mostly useful for
     * debugging document processors, but may have other uses as well. Normally,
     * the printing will be done on a subset of a document, starting with a
     * specified Node. This Node and any children will be printed.
     *
     *
     * @param currentNode The node to start the print with. It may not be
     *           <code>null</code>.
     *
     * @param stream The printstream that will recieve the output. It may not be
     *           <code>null</code>.
     *
     * @param parentIndent The string representing the input header. It may be
     *           <code>null</code> or empty, then it will be default to 3
     *           spaces.
     *
     * @throws java.io.IOException
     */
    public static void printXmlTree(Node currentNode, PrintStream stream,
        String parentIndent) throws java.io.IOException {
        if (currentNode == null) {
            throw new IllegalArgumentException("currentNode may not be null");
        }

        if (stream == null) {
            throw new IllegalArgumentException("stream may not be null");
        }

        String myIndent = parentIndent;

        if ((parentIndent == null) || (parentIndent.trim().length() == 0)) {
            myIndent = "   "; // default to 3 spaces
        }

        stream.print(myIndent);

        switch (currentNode.getNodeType()) {
        case Node.DOCUMENT_NODE:
            stream.print("Document " + currentNode.getNodeValue() +
                currentNode.toString());

            break;

        case Node.ATTRIBUTE_NODE:
            stream.print("Attribute " + currentNode.getNodeName());

            break;

        case Node.CDATA_SECTION_NODE:
            stream.print("CDATA " + currentNode.toString());

            break;

        case Node.COMMENT_NODE:
            stream.print("Comment " + currentNode.getNodeValue());

            break;

        case Node.DOCUMENT_FRAGMENT_NODE:
            stream.print("Document Fragment " + currentNode.toString());

            break;

        case Node.DOCUMENT_TYPE_NODE:
            stream.print("Document Type " + currentNode.toString());

            break;

        case Node.ELEMENT_NODE:
            stream.print("Element " + currentNode.getNodeName());

            break;

        case Node.ENTITY_NODE:
            stream.print("Entity " + currentNode.getNodeName());

            break;

        case Node.ENTITY_REFERENCE_NODE:
            stream.print("Entity Reference " + currentNode.getNodeName());

            break;

        case Node.NOTATION_NODE:
            stream.print("Notation " + currentNode.getNodeName());

            break;

        case Node.PROCESSING_INSTRUCTION_NODE:
            stream.print("Processing Instruction " +
                currentNode.getNodeValue());

            break;

        case Node.TEXT_NODE:
            stream.print("Text " + currentNode.getNodeValue().length() +
                " Bytes ");

            break;

        default:
            stream.print("Invalid Node Type" +
                String.valueOf(currentNode.getNodeType()));

            break;
        }

        stream.println();

        if (currentNode.hasChildNodes()) {
            NodeList children = currentNode.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                printXmlTree(children.item(i), stream, myIndent);
            }
        }
    }

    /**
     * Print the structure of the tree, recursively. Note this routine does not
     * print the contents, only the structure. This is mostly useful for
     * debugging document processors, but may have other uses as well. Normally,
     * the printing will be done on a subset of a document, starting with a
     * specified Node. This Node and any children will be printed.
     *
     * @param currentNode The node to start the print with. It may not be
     *           <code>null</code>.
     *
     * @param stream The printstream that will recieve the output. It may not be
     *           <code>null</code>.
     *
     * @throws java.io.IOException
     */
    public static void printXmlTree(Node currentNode, PrintStream stream)
        throws java.io.IOException {
        printXmlTree(currentNode, stream, "");
    }

    /**
     * This routine prints the structure of an entire document (including the
     * document root) to the specified stream. The contents of the elements are
     * not printed, only the structure.
     *
     * @param doc The node to start the print with. It may not be
     *           <code>null</code>.
     *
     * @param stream The printstream that will recieve the output. It may not be
     *           <code>null</code>.
     *
     * @throws java.io.IOException
     *
     */
    public static void printXmlTree(Document doc, PrintStream stream)
        throws java.io.IOException {
        printXmlTree(doc, stream, "");
    }

    /**
     * Main method, for debugging and showing sample code. Usage is : -i
     * (INPUT_XML_FILE_PATH) -o (OUTPUT_XML_FILE_PATH)
     *
     * @param argv required, format is: -i (INPUT_XML_FILE_PATH) -o
     *           (OUTPUT_XML_FILE_PATH)
     */
    public static void main(String[] argv) {
        if (argv.length != 4) {
            System.out.println("Invalid number of arguments.");
            printUsage();
            System.exit(1);
        }

        if (!argv[0].equalsIgnoreCase("-i")) {
            System.out.println("Invalid first argument.");
            printUsage();
            System.exit(1);
        }

        String inputFilePath = argv[1];
        File inputFile = new File(inputFilePath);

        if (!inputFile.isFile()) {
            System.out.println("Invalid input file path: " + inputFilePath);
            System.exit(1);
        }

        if (!argv[2].equalsIgnoreCase("-o")) {
            System.out.println("Invalid third argument.");
            printUsage();
            System.exit(1);
        }

        String outputFilePath = argv[3];
        File outputFile = new File(outputFilePath);
        File parentFile = outputFile.getParentFile();

        if (parentFile != null) {
            if (!parentFile.isDirectory()) {
                parentFile.mkdirs();
            }
        }

        try {
            InputSource in = new InputSource(new FileInputStream(inputFile));
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            int flags = FLAG_NO_INDENT;

            // int flags = 0;
            String str = PSXmlDocumentBuilder.toString(doc, flags);
            System.out.println(str);

            FileWriter fos = new FileWriter(outputFile);
            fos.write(str);
            fos.close();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Prints the usage of this class.
     */
    public static void printUsage() {
        System.out.println("Usage is : ");
        System.out.println("-i [INPUT_XML_FILE_PATH] -o [OUTPUT_XML_FILE_PATH]");
    }
}
