
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

import com.percussion.util.FunctionalUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.percussion.util.FunctionalUtils.IS_POSITIVE_NUMBER_OR_NOT_NULL;

public class PSJettyConnectors extends PSAbstractConnectors {

    public static final String KEYSTORE_FILE_ATTR = "jetty.sslContext.keyStorePath";
    public static final String KEYSTORE_PASS_ATTR = "jetty.sslContext.keyStorePassword";
    public static final String TRUSTSTORE_FILE_ATTR = "jetty.sslContext.trustStorePath";
    public static final String TRUSTSTORE_PASS_ATTR = "jetty.sslContext.trustStorePassword";
    public static final String KEYMANAGER_PASS_ATTR = "jetty.sslContext.keyManagerPassword";

    public static final String INCLUDE_CIPHERS_PROP = "perc.ssl.includeCiphers";
    public static final String PROTOCOLS_PROP = "perc.ssl.protocols";
    public static final String JETTY_HTTP_PORT_KEY = "jetty.http.port";
    public static final String JETTY_HTTPS_PORT_KEY = "jetty.ssl.port";
    public static final String JETTY_HTTP_HOST = "jetty.http.host";
    public static final String JETTY_HTTPS_HOST = "jetty.ssl.host";
    public static final String JETTY_SHUTDOWN_PORT = "-DSTOP.PORT";


    private final Path rxRootDir;
    private final Path jettyRoot = Paths.get("jetty");
    private final Path jettyBase = jettyRoot.resolve("base");
    private final Path jettyDefaults = jettyRoot.resolve("defaults");
    private final Path jettyStartD = jettyBase.resolve("start.d");
    private final Path installationProperties = Paths.get("etc", "installation.properties");


    public PSJettyConnectors(Path rxDir) {
        super(rxDir.resolve("jetty/base"));
        rxRootDir = rxDir;
    }


    public PSJettyConnectors(Path rxDir,PSAbstractConnectors connectorInfo) {
        this(rxDir);
        mergeConnectors(connectorInfo.getConnectors());
    }

    private Path getJettyConfigFile(Path path) {
        Objects.nonNull(path);

        Path baseItem = rxRootDir.resolve(jettyBase).resolve(path).normalize();
        if (Files.exists(baseItem)) {
            return baseItem;
        }
        Path defaultItem = rxRootDir.resolve(jettyDefaults).resolve(path).normalize();
        if (Files.exists(defaultItem)) {
            try {
                Files.copy(defaultItem, baseItem);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return baseItem;
    }


    public void load() {
        setConnectors(loadHttpConnectors());
    }

    List<IPSConnector> loadHttpConnectors()
    {
        Path propertyPath = getJettyConfigFile(installationProperties);

        final Map<String, String> properties = loadProperties(propertyPath);

        Function<String,Optional<String>> getProp = (p) -> Optional.ofNullable(properties.get(p));

        Function<String, Path> jettyBaseRelativePath = p -> rxRootDir.resolve(jettyBase).resolve(p).toAbsolutePath();

        Path sslkeyBase = rxRootDir.resolve(jettyBase).toAbsolutePath().normalize();
        final PSAbstractConnector http = PSAbstractConnector.getBuilder().setConnectorFileContext(sslkeyBase).build();
        final PSAbstractConnector https= PSAbstractConnector.getBuilder().setConnectorFileContext(sslkeyBase).setHttps().build();

        http.setScheme(PSAbstractConnector.SCHEME_HTTP);
        https.setScheme(PSAbstractConnector.SCHEME_HTTPS);

        getProp.apply(JETTY_HTTP_PORT_KEY)
                .map(NumberUtils::toInt)
                .filter(IS_POSITIVE_NUMBER_OR_NOT_NULL)
                .ifPresent(http::setPort);

        getProp.apply(JETTY_HTTP_HOST)
                .ifPresent(http::setHostAddress);

        getProp.apply(JETTY_HTTPS_PORT_KEY)
                .map(NumberUtils::toInt)
                .filter(IS_POSITIVE_NUMBER_OR_NOT_NULL)
                .ifPresent(https::setPort);


        getProp.apply(JETTY_HTTPS_HOST)
                .ifPresent(https::setHostAddress);

        getProp.apply(INCLUDE_CIPHERS_PROP)
                .map(FunctionalUtils::commaStringToStream)
                .map(e -> e.collect(Collectors.toSet()))
                .ifPresent(https::setCiphers);

        getProp.apply(KEYSTORE_FILE_ATTR)
                .map(jettyBaseRelativePath)
                .ifPresent(https::setKeystoreFile);

        getProp.apply(KEYSTORE_PASS_ATTR)
                .ifPresent(https::setKeystorePass);

        getProp.apply(TRUSTSTORE_FILE_ATTR)
                .map(jettyBaseRelativePath)
                .ifPresent(https::setTruststoreFile);

        getProp.apply(TRUSTSTORE_PASS_ATTR)
                .ifPresent(https::setTruststorePass);


        getProp.apply(PROTOCOLS_PROP)
                .map(FunctionalUtils::commaStringToStream)
                .map(e -> e.collect(Collectors.toSet()))
                .ifPresent(https::setSslProtocols);

        List<IPSConnector> connectors = new ArrayList<IPSConnector>();
        if (http.getPort()>0)
            connectors.add(http);

        if (https.getPort()>0)
            connectors.add(https);

        return connectors;
    }


    public void save() {
        List<IPSConnector> itemConnectorsSaved = this.getConnectors();
        load();

        mergeConnectors(itemConnectorsSaved);

        Path propertyPath = getJettyConfigFile(installationProperties);

        final Map<String, String> properties = loadProperties(propertyPath);

        getHttpsConnector().ifPresent(
                c-> {
                    Optional.ofNullable(c.getPort())
                            .filter(IS_POSITIVE_NUMBER_OR_NOT_NULL)
                            .map(Object::toString)
                            .ifPresent( v->properties.put(JETTY_HTTPS_PORT_KEY,v));

                    Optional.ofNullable(c.getHostAddress())
                            .ifPresent( v->properties.put(JETTY_HTTPS_HOST,v));

                    Optional.ofNullable(c.getCiphers())
                            .map(PSJettyConnectors::toCommaString)
                            .ifPresent( v->properties.put(INCLUDE_CIPHERS_PROP,v));


                    Optional.ofNullable(c.getKeystoreFile())
                            .filter(Objects::nonNull)
                            .map(getPathStringFunction())
                            .ifPresent( v->properties.put(KEYSTORE_FILE_ATTR,v.toString()));

                    Optional.ofNullable(c.getKeystorePass())
                            .ifPresent( v->properties.put(KEYSTORE_PASS_ATTR,v));

                    Optional.ofNullable(c.getTruststoreFile())
                            .filter(Objects::nonNull)
                            .map(getPathStringFunction())
                            .ifPresent( v->properties.put(TRUSTSTORE_FILE_ATTR,v.toString()));

                    Optional.ofNullable(c.getTruststorePass())
                            .ifPresent( v->properties.put(TRUSTSTORE_PASS_ATTR,v));

                    Optional.ofNullable(c.getSslProtocols())
                            .map(PSJettyConnectors::toCommaString)
                            .ifPresent( v->properties.put(PROTOCOLS_PROP,v));
                });

        getHttpConnector().ifPresent(
                c-> {
                    Optional.ofNullable(c.getPort())
                            .filter(IS_POSITIVE_NUMBER_OR_NOT_NULL)
                            .map(Object::toString)
                            .ifPresent(v->properties.put(JETTY_HTTP_PORT_KEY,v));


                    Optional.ofNullable(c.getHostAddress())
                            .ifPresent(v->properties.put(JETTY_HTTP_HOST,v));

                });


        saveProperties(properties,propertyPath);

    }

    private Function<Path, Path> getPathStringFunction() {
        Path keystoreBase = rxRootDir.resolve(jettyBase).normalize().toAbsolutePath();
        return p -> keystoreBase.relativize(keystoreBase.resolve(p).toAbsolutePath().normalize());
    }

    @Override
    public String toString() {
        return "PSJettyConnectors{" +
                "rxRootDir=" + rxRootDir +
                ", jettyRoot=" + jettyRoot +
                ", jettyBase=" + jettyBase +
                ", jettyDefaults=" + jettyDefaults +
                ", installationProperties=" + installationProperties +
                '}';
    }
}
