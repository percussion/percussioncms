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
package com.percussion.testing;

import java.io.IOException;
import java.util.Properties;


/**
 * If a JUnit test requires a Rhythmyx server to run and it is invoked as
 * a remote client (of the Rhythmyx server), it should implement this
 * interface in addition to the TestCase class. 
 */
public interface IPSClientBasedJunitTest extends IPSUnitTestConfigHelper
{
    /**
     * Gets the information that is needed to connect to a Rhythmyx Server.
     *
     * @param type type of a connection requested, can be one of CONN_TYPE_XX.
     * @return The connection properties. Never <code>null</code>.
     * The required properties are:
     * <table border="1">
     * <tr>
     * <th>Key</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td>hostName</td>
     * <td>The name of the Rx server machine, required.</td>
     * </tr>
     * <tr>
     * <td>port</td>
     * <td>The port the Rx server is listening on. If not provided,
     * 9992 is used for non-ssl and 9443 for ssl.</td>
     * </tr>
     * <tr>
     * <td>loginId</td>
     * <td>The user name to use when connecting. If empty all connections
     * will be made anonymously.</td>
     * </tr>
     * <tr>
     * <td>loginPw</td>
     * <td>The password to use when connecting. If not provided, "" is
     * used. Must be unencrypted.</td>
     * </tr>
     * <tr>
     * <td>useSSL</td>
     * <td>A flag to indicate whether the connection should be encrypted.
     * If 'true', then uses an SSL socket for communication. Any other
     * value, or absence of the property and the connection will be
     * made without SSL. If <code>true</code>, the supplied port
     * must accept ssl connection requests.</td>
     * </tr>
     * <tr>
     * <td>serverRoot</td>
     * <td>The server's request root. If not supplied, Rhythmyx is used.
     * </td>
     * </tr>
     * </table>
     * @throws IOException
     */
    static Properties getConnectionProps(int type) throws IOException {
        throw new IllegalArgumentException("class needs to override getConnectionProps");
    }
}
