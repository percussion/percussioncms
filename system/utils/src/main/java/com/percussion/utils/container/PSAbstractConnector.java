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

package com.percussion.utils.container;

import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.tomcat.PSTomcatConnector;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class PSAbstractConnector implements IPSConnector, XMLEnabled {

    /**
     * Scheme constant for the "http" protocol.
     */
    public static final String SCHEME_HTTP = "http";
    /**
     * Scheme constant for the "https" protocol.
     */
    public static final String SCHEME_HTTPS = "https";
    private static Pattern EXTRACT_PROPNAME = Pattern.compile("\\$\\{([^}]*)}");

    private Path connectorFileContext = Paths.get(".");

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    protected Map<String,String> properties = new HashMap<>();

    protected String scheme = SCHEME_HTTP;
    protected String getHostAddress;
    protected Path keystoreFile;
    protected String keystorePass;
    protected Path truststoreFile;
    protected String truststorePass;
    protected int port;
    protected Set<String> ciphers;
    protected Set<String> sslProtocols;


    protected String protocol=PROTOCOL_HTTP;

    public PSAbstractConnector(Path connectorFileContext)
    {
        this.connectorFileContext=connectorFileContext;
    }

    public  Optional<String> processPropertyReference(String value) {

        if (value==null)
            return Optional.empty();

        Matcher matcher = EXTRACT_PROPNAME.matcher(value);

        StringBuffer buffer = new StringBuffer();
        //if (!matcher.matches())
        //    return Optional.of(value);

        while (matcher.find()) {
            //if cannot find the property name within ${ } then just use the original text
            String replacement = properties.getOrDefault(matcher.group(1), matcher.group(0));

            if (replacement != null) {
                // matcher.appendReplacement(buffer, replacement);
                // see comment
                matcher.appendReplacement(buffer, "");
                buffer.append(replacement);
            }
        }
        matcher.appendTail(buffer);
        return Optional.of(buffer.toString());
    }


    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getHostAddress() {
        return getHostAddress;
    }

    @Override
    public void setHostAddress(String getHostAddress) {
        this.getHostAddress = getHostAddress;
    }

    @Override
    public Path getKeystoreFile() {
        return keystoreFile;
    }

    @Override
    public void setKeystoreFile(Path keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    @Override
    public String getKeystorePass() {
        return keystorePass;
    }

    @Override
    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    @Override
    public Path getTruststoreFile() {
        return truststoreFile;
    }

    @Override
    public void setTruststoreFile(Path truststoreFile) {
        this.truststoreFile = truststoreFile;
    }

    @Override
    public String getTruststorePass() {
        return truststorePass;
    }

    @Override
    public void setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;

    }

    @Override
    public Set<String> getCiphers() {
        return ciphers;
    }

    @Override
    public void setCiphers(Set<String> ciphers) {
        this.ciphers = ciphers;
    }

    @Override
    public Set<String> getSslProtocols() {
        return sslProtocols;
    }

    @Override
    public void setSslProtocols(Set<String> protocols) {
        this.sslProtocols = protocols;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String toCommaString(Collection<String> string) {
        return string.stream().collect(Collectors.joining(","));
    }


    @Override
    public void copyFrom(IPSConnector c)
    {
        Map<String, String> fromProperties = c.getProperties();
        this.getProperties().putAll(fromProperties);
        updateFields();
    }


    protected abstract void updateFields();

    protected abstract void updateProperties();

    @Override
    public String toString() {
        return "PSAbstractConnector{" +
                "protocol=" + protocol +
                ", scheme='" + scheme + '\'' +
                ", getHostAddress='" + getHostAddress + '\'' +
                ", keystoreFile=" + keystoreFile +
                ", keystorePass='" + PSStringUtils.hidePass(keystorePass) + '\'' +
                ", truststoreFile=" + truststoreFile +
                ", truststorePass='" + PSStringUtils.hidePass(truststorePass)  + '\'' +
                ", port=" + port +
                ", ciphers=" + ciphers +
                ", sslProtocols=" + sslProtocols +
                '}';
    }



    public Path getConnectorFileContext() {
        return connectorFileContext;
    }

    protected Function<String, Path> getAbsolutePath() {
        return p -> getConnectorFileContext().resolve(p).normalize().toAbsolutePath();
    }

    protected Function<Path, String> getRelativePathString() {
            return p -> getConnectorFileContext().normalize().toAbsolutePath().relativize(getConnectorFileContext().resolve(p).normalize().toAbsolutePath()).toString();
    }


    public static class Builder
    {
        protected String scheme=SCHEME_HTTP;
        protected Path connectorFileContext;
        protected Element source;
        protected IPSConnector copyFrom;
        protected Map<String, String> props;
        protected int port;

        public Builder()
        {

        }

        public Builder(Builder connectorBuilder) {
            this.connectorFileContext=connectorBuilder.connectorFileContext;
            this.source=connectorBuilder.source;
            this.copyFrom=connectorBuilder.copyFrom;
            this.props=connectorBuilder.props;
            this.port=connectorBuilder.port;
        }


        public Builder setConnectorFileContext(Path connectorFileContext) {
            this.connectorFileContext = connectorFileContext;
            return this;
        }

        public Builder setSource(Element source) {
            this.source = source;
            return this;
        }

        public Builder setCopyFrom(IPSConnector copyFrom) {
            this.copyFrom = copyFrom;

            return copyFrom.getScheme().equals(SCHEME_HTTPS) ? setHttps():this;
        }

        public Builder setProps(Map<String, String> props) {
            this.props = props;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public HttpsBuilder setHttps()
        {
            scheme=SCHEME_HTTPS;
            return (this instanceof HttpsBuilder) ? (HttpsBuilder)this :new HttpsBuilder(this);
        }

        public PSAbstractConnector build()
        {

            PSAbstractConnector connector = null;

            if(connectorFileContext == null) {
                connectorFileContext = Paths.get(".");
            }

            if (this.source!=null)
            {
                connector = new PSTomcatConnector(connectorFileContext,this.source,this.props);
            }else if (copyFrom!=null)
                connector = new PSTomcatConnector(connectorFileContext,copyFrom,props);
            else
                connector = new PSTomcatConnector(connectorFileContext, scheme,port, props);

            return connector;
        }

    }

    public static class HttpsBuilder<T extends PSAbstractConnector> extends Builder
    {

        protected Path truststoreFile;
        protected String truststorePass;
        protected Set<String> ciphers;
        protected Set<String> sslProtocols;


        protected Path keystoreFile;
        protected String keystorePass;

        public HttpsBuilder(){
            super();
        }

        public HttpsBuilder(Builder connectorBuilder) {
            super(connectorBuilder);
            setHttps();
           this.connectorFileContext = connectorBuilder.connectorFileContext;
        }

        public HttpsBuilder setKeystoreFile(Path keystoreFile) {
            this.keystoreFile = keystoreFile;
            return this;
        }

        public HttpsBuilder setKeystorePass(String keystorePass) {
            this.keystorePass = keystorePass;
            return this;
        }

        public HttpsBuilder setTruststoreFile(Path truststoreFile) {
            this.truststoreFile = truststoreFile;
            return this;
        }

        public HttpsBuilder setTruststorePass(String truststorePass) {
            this.truststorePass = truststorePass;
            return this;

        }

        public HttpsBuilder setCiphers(Set<String> ciphers) {
            this.ciphers = ciphers;
            return this;
        }

        public HttpsBuilder setSslProtocols(Set<String> sslProtocols) {
            this.sslProtocols = sslProtocols;
            return this;
        }

        @Override
        public HttpsBuilder setHttps()
        {
            this.scheme=PSAbstractConnector.SCHEME_HTTPS;
            return this;
        }

        public PSAbstractConnector build()
        {

            PSAbstractConnector connector = super.build();
            if (truststoreFile!=null)
                connector.setTruststoreFile(truststoreFile);
            if (truststorePass!=null)
                connector.setTruststorePass(truststorePass);
            if (keystoreFile!=null)
                connector.setKeystoreFile(keystoreFile);
            if (keystorePass!=null)
                connector.setKeystorePass(keystorePass);
            if (ciphers!=null)
                connector.setCiphers(ciphers);
            if (sslProtocols!=null)
                connector.setSslProtocols(sslProtocols);

            connector.setScheme(PSAbstractConnector.SCHEME_HTTPS);

            return connector;
        }

    }

    public static PSAbstractConnector.Builder getBuilder()
    {
        return new PSAbstractConnector.Builder();
    }
}
