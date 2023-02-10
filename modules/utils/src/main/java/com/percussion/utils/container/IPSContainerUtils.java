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

/**
 *
 */
package com.percussion.utils.container;

import com.percussion.utils.container.config.ContainerConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides an interface contract for Application Container utilities
 * that should be provided for each container. 
 *
 * @author natechadwick
 *
 */
public interface IPSContainerUtils extends ContainerConfig {

    PSAbstractConnectors getConnectorInfo();

    /**
     * Load all JNDI datasources from the specified file.  Any settings
     * configured that are not supported by the {@link PSJndiDatasource} class
     * are loaded and preserved when the datasource is saved
     * (see {@link #saveRxDatasources(File, File, List, String)}.
     *
     * @param dsFile The file from which the datasources should be loaded,
     * identified by {@link #DATASOURCE_FILE_NAME}. May not be <code>null</code>
     * and must be a file conforming to the JBoss "jboss-ds_1_5.dtd" DTD.
     *
     * @param loginCfgFile The file from which encrypted credentials are
     * loaded, identified by the {@link #LOGIN_CONFIG_FILE_NAME}.  May not be
     * <code>null</code> and must conform to the JBoss "security_config.dtd" DTD.
     *
     * @param secretKey The key to use when decrypting passwords, may not be
     * <code>null</code> or empty.
     *
     * @return A list of datasources, never <code>null</code>, may be empty.
     *
     * @throws IOException If any errors occur reading from the files.
     * @throws SAXException If either document is malformed.
     * @throws PSInvalidXmlException If either document does not conform to the
     * expected format.
     * @throws PSMissingApplicationPolicyException If a datasource specfies a
     * security domain name that cannot be located in the supplied
     * <code>logingCfgFile</code>
     * @throws org.xml.sax.SAXException
     */
    List<IPSJndiDatasource> getDatasources();

    /**
     * Saves the supplied JNDI datasource configurations to the supplied files,
     * replacing any existing configurations. For existing datasources loaded by
     * {@link #saveRxDatasources(File, File, List, String)}, any settings that
     * were configured but not supported by the {@link PSJndiDatasource} class
     * are saved intact. See that method for more detailed parameter information.
     * Note that {@link PSJndiDatasource#setSecurityDomain(String)} will be
     * called on all supplied datasources.
     *
     * @param dsFile The file to which the datasources should be saved, may not
     * be <code>null</code>.
     * @param loginCfgFile The file to which encrypted credentials are saved, may
     * not be <code>null</code>.
     * @param datasources The list of datasources to saved, may not be
     * <code>null</code>, may be empty.
     * @param secretKey The key to use when decrypting passwords, may not be
     * <code>null</code> or empty.
     * @throws IOException If there is an error saving to the files.
     * @throws SAXException If there is an error reading an existing file.
     * @throws org.xml.sax.SAXException
     */
    void setDatasources(
            List<IPSJndiDatasource> datasources);


    IPSDatasourceResolver getDatasourceResolver();

    void setDatasourceResolver(IPSDatasourceResolver resolver);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isLoaded();

    void setLoaded(boolean loaded);

}
