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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
