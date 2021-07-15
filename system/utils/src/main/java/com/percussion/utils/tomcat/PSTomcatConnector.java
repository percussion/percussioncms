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

package com.percussion.utils.tomcat;

import com.percussion.util.FunctionalUtils;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.PSAbstractConnector;
import com.percussion.utils.container.XMLEnabled;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides an object representation and implements XML serialization for a
 * Tomcat Connector element. See http://jakarta.apache.org/tomcat/tomcat-4.1-
 * doc/config/coyote.html for details and the XML format. 
 */
public class PSTomcatConnector extends PSAbstractConnector implements XMLEnabled {

   /*/
    * Xml node name of the Connector element.
    */
   public static final String CONNECTOR_NODE_NAME = "Connector";



    public PSTomcatConnector(Path connectorFileContext, String scheme, int port , Map<String, String> props) {
        super(connectorFileContext);

        if (props!=null)
            this.properties.putAll(props);


        setScheme(scheme);
        setPort(port);

        this.properties.put(PORT_ATTR,Integer.toString(port));
        this.properties.put(SCHEME_ATTR,scheme);

        updateFields();
    }


    public PSTomcatConnector(Path connectorFileContext, IPSConnector copyFrom, Map<String,String> props) {
        super(connectorFileContext);
        if (props!=null)
            this.properties.putAll(props);
        copyFrom(copyFrom);
    }

    public PSTomcatConnector(Path connectorFileContext, Element source, Map<String,String> props) {
        super(connectorFileContext);
        if (props!=null)
            this.properties.putAll(props);
        try {
            fromXml(source);
        } catch (SAXException | IOException | PSInvalidXmlException e ) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public void setScheme(String scheme) {
        super.setScheme(scheme);
    }

    @Override
    public String getScheme() {
        return super.getScheme();
    }

    Optional<String> updateProp(String propname)
    {
        return processPropertyReference(properties.get(propname));
    }


    @Override
    protected void updateFields() {

        updateProp(SCHEME_ATTR)
                .ifPresent(this::setScheme);

        updateProp(PORT_ATTR)
                .map(NumberUtils::toInt)
                .ifPresent(this::setPort);

        updateProp(ADDRESS_ATTR)
                .ifPresent(this::setHostAddress);

        updateProp(CIPHERS_ATTR)
                .map(FunctionalUtils::commaStringToStream)
                .map(e -> e.collect(Collectors.toSet()))
                .ifPresent(this::setCiphers);

        updateProp(PROTOCOL_ATTR)
                .ifPresent(this::setProtocol);

        updateProp(SSL_PROTOCOLS)
                .map(FunctionalUtils::commaStringToStream)
                .map(e -> e.collect(Collectors.toSet()))
                .ifPresent(this::setSslProtocols);

        updateProp(KEYSTORE_FILE_ATTR)
                .map(getAbsolutePath())
                .ifPresent(this::setKeystoreFile);

        updateProp(KEYSTORE_PASS_ATTR)
                .ifPresent(this::setKeystorePass);

        updateProp(TRUSTSTORE_FILE_ATTR)
                .map(getAbsolutePath())
                .ifPresent(this::setTruststoreFile);

        updateProp(TRUSTSTORE_PASS_ATTR)
                .ifPresent(this::setTruststorePass);
    }


    /**
     * Serialize this connector to its XML representation.  See
     * {@link #PSTomcatConnector(Element)} for more information.
     *
     * @param doc The document to use, may not be <code>null</code>.
     * @return The resulting connector element, never <code>null</code>.
     */
    @Override
    public Document toXml() throws IOException, SAXException, PSInvalidXmlException {
        return toXml(propertyMap);
   }


    private Consumer<String> setProp(String key)
    {
        return s -> properties.put(key,s);
    }

    @Override
    protected void updateProperties() {

        Optional.ofNullable(this.getPort())
                .filter(FunctionalUtils.IS_POSITIVE_NUMBER_OR_NOT_NULL)
                .map(Object::toString)
                .ifPresent(setProp(PORT_ATTR));

        Optional.ofNullable(this.getHostAddress())
                .ifPresent(setProp(ADDRESS_ATTR));

        Optional.ofNullable(this.getScheme())
                .ifPresent(setProp(SCHEME_ATTR));

        Optional.ofNullable(this.getKeystoreFile())
                .map(getRelativePathString())
                .ifPresent(setProp(KEYSTORE_FILE_ATTR));

        Optional.ofNullable(this.getKeystorePass())
                .ifPresent(setProp(KEYSTORE_PASS_ATTR));

        Optional.ofNullable(this.getTruststoreFile())
                .map(getRelativePathString())
                .ifPresent(setProp(TRUSTSTORE_FILE_ATTR));

        Optional.ofNullable(this.getTruststorePass())
                .ifPresent(setProp(TRUSTSTORE_PASS_ATTR));

        Optional.ofNullable(this.getSslProtocols())
                .map(this::toCommaString)
                .ifPresent(setProp(PROTOCOL_ATTR));

        Optional.ofNullable(this.getCiphers())
                .map(this::toCommaString)
                .ifPresent(setProp(CIPHERS_ATTR));
    }




    /**
    * Gets all attributes from the supplied element as a set of properties.
    *
    * @param source The source element, assumed not <code>null</code>.
    *
    * @param properties
     * @return The properties, never <code>null</code>, may be empty.  Each
    * property has the attribute name as the key and the attribute value as the
    * value.
    */
   private Map<String,String> loadAttributeMap(Element source)
   {
        Map<String,String> attributeMap = new LinkedHashMap<>();
        if (source == null)
            throw new IllegalArgumentException("source may not be null");

        NamedNodeMap attrMap = source.getAttributes();

        for (int i = 0; i < attrMap.getLength(); i++)
        {
            Attr attr = (Attr)attrMap.item(i);

            attributeMap.put(attr.getName(), attr.getValue());
        }
        return attributeMap;

    }




    /**
     * Constant for the port attribute name.
     */
    public static final String PORT_ATTR = "port";
    public static final String PROTOCOL_ATTR = "protocol";
    public static final String CONNECTION_TIMEOUT_ATTR = "protocol";
    public static final String REDIRECT_PORT_ATTR = "redirectPort";
    public static final String URI_ENCODING_ATTR = "URIEncoding";
    public static final String X_POWERED_BY_ATTR = "xpoweredBy";
    public static final String COMPRESSION_ATTR = "compression";
    public static final String COMPRESSABLE_MIME_TYPE_ATTR = "compressableMimeType";
    public static final String COMPRESSION_MIN_SIZE_ATTR = "compressionMinSize";
    public static final String SSL_ENABLED_ATTR = "SSLEnabled";
    public static final String MAX_THREADS_ATTR = "maxThreads";
    public static final String SECURE_ATTR = "secure";
    public static final String CLIENT_AUTH_ATTR = "clientAuth";
   protected static final String KEYSTORE_FILE_ATTR = "keystoreFile";
   protected static final String KEYSTORE_PASS_ATTR = "keystorePass";
    protected static final String TRUSTSTORE_FILE_ATTR = "truststoreFile";
    protected static final String TRUSTSTORE_PASS_ATTR = "truststorePass";
   protected static final String CIPHERS_ATTR = "ciphers";
   protected static final String ADDRESS_ATTR = "address";
   protected static final String SCHEME_ATTR = "scheme";
   protected static final String SSL_PROTOCOLS = "sslProtocols";
    private static final String OLD_SSP_PROTOCOL_ATTR = "sslProtocol";
   protected static final String X_POWERED_BY = "xpoweredBy";
   protected static final String SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";

    public static final Map<String,String> PROP_MAP = new HashMap<>();


    static final HashSet<String> DELETE_ATTRIBUTES = new HashSet() {{
        add(X_POWERED_BY);
        add(OLD_SSP_PROTOCOL_ATTR);
    }};
    static final Map<String , String> DEFAULT_HTTP_ATTRIBUTES = new HashMap<String , String>() {{
        put(PORT_ATTR,  "9980");
        put(SCHEME_ATTR,SCHEME_HTTP);
        put(PROTOCOL_ATTR, "HTTP/1.1");
        put(CONNECTION_TIMEOUT_ATTR,"20000");
        put(REDIRECT_PORT_ATTR,"8443");
        put(URI_ENCODING_ATTR,"UTF-8");
        put(X_POWERED_BY_ATTR,"false");
        put(ADDRESS_ATTR,"0.0.0.0");
        put(COMPRESSION_ATTR,"false");
        put(COMPRESSABLE_MIME_TYPE_ATTR,"text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");
        put(COMPRESSION_MIN_SIZE_ATTR,"256");
    }};

    static final Map<String , String> DEFAULT_HTTPS_ATTRIBUTES = new HashMap<String , String>() {{
       putAll(DEFAULT_HTTP_ATTRIBUTES);
       put(SCHEME_ATTR,SCHEME_HTTPS);
       put(SECURE_ATTR,"true");
       put(KEYSTORE_FILE_ATTR,"conf/.keystore");
       put(CLIENT_AUTH_ATTR,"false");
       put(OLD_SSP_PROTOCOL_ATTR,"TLS");
       put(SSL_ENABLED_PROTOCOLS,"TLSv1.2");
       put(CIPHERS_ATTR,"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA");
    }};


    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    public static final HashMap<String,String> propertyMap = new HashMap<>();
    static {
        propertyMap.put(ADDRESS_ATTR,"jboss.bind.address");
    }

    /**
     * If the supplied element is a connector element that specifies the HTTP
     * protocol.
     *
     * @param connEl The element to check, may not be <code>null</code>.
     *
     * @return <code>true</code> if it is, <code>false</code> if no.
     */
    public static boolean isHttpConnector(Element connEl)
    {
        if (connEl == null)
            throw new IllegalArgumentException("connEl may not be null");

        boolean isHttpConn = false;

        if (connEl.getNodeName().equals(CONNECTOR_NODE_NAME))
        {
            String protocol = connEl.getAttribute(PROTOCOL_ATTR);
            isHttpConn = (StringUtils.isBlank(protocol) ||
                    protocol.toLowerCase().startsWith(SCHEME_HTTP));
        }

        return isHttpConn;
    }

    @Override
    public Map<String,String> getProperties()
    {
        updateProperties();
        return super.getProperties();
    }


    @Override
    public Document toXml(Map<String,String> properties) throws IOException, SAXException, PSInvalidXmlException
    {
        // create a new one
        Document doc = PSXmlDocumentBuilder.createXmlDocument();
        if (doc == null)
            throw new IllegalArgumentException("doc may not be null");

        updateProperties();

        Element root = doc.createElement(CONNECTOR_NODE_NAME);

        // set all attributes
        for (Map.Entry<String, String> entry : properties.entrySet())
        {

            Optional<String> mapRefValue = Optional.ofNullable(properties.get(entry.getKey()));
            //String value = mapRefValue.map(v -> "${" + v + "}").orElse(entry.getValue());
            String value = mapRefValue.orElse(entry.getValue());
            root.setAttribute(entry.getKey(), value);
        }

        DELETE_ATTRIBUTES.stream().forEach(root::removeAttribute);
        doc.appendChild(root);
        return doc;
    }

    @Override
    public void fromXml(Element el) throws SAXException, IOException, PSInvalidXmlException {

        fromXml(el,null);
    }

    @Override
    public void fromXml(Element source,Map<String,String> properties) throws IOException, SAXException, PSInvalidXmlException {

        if (source == null)
            throw new IllegalArgumentException("source may not be null");

        if (!source.getNodeName().equals(CONNECTOR_NODE_NAME))
            throw new IllegalArgumentException(
                    "source must be a Connector element");

        this.properties = loadAttributeMap(source);

        if (properties!=null) {
            this.properties.putAll(properties);
        }

        updateFields();
    }


}
