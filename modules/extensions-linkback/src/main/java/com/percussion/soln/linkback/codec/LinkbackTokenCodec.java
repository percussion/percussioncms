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
package com.percussion.soln.linkback.codec;

import java.util.Map;

/**
 * This interface provides an encoding mechanism for the parameters required by
 * Rhythmyx Linkback servlets. The parameters in the map of parameters are
 * defined in IPSHtmlParameters.
 * <ul>
 * <li>sys_contentid</li>
 * <li>sys_revision</li>
 * <li>sys_template</li>
 * <li>sys_siteid</li>
 * <li>sys_folderid</li>
 * </ul>
 * These parameters are encoded from a Map that may contain multiple parameters.
 * Each parameter value will be one of:
 * <ul>
 * <li>A String</li>
 * <li>A String Array</li>
 * <li>A List of Strings</li>
 * </ul>
 * In the case of an Array or List, only the first value will be used. When
 * decoding a token, the values of the parameters will alwasy be Strings.
 * 
 * @author DavidBenua
 * 
 */
public interface LinkbackTokenCodec {

    /**
     * Encode a link back token based on URL parameters.
     * 
     * @param params
     *            a map of parameters. The names of the parameters must match
     *            the constants defined in IPSHtmlParameters, and the values
     *            must be either <code>String</code> or <code>String[]</code>
     *            representations. Invalid or unknown parameters are ignored.
     * @return the encoded token.
     * @throws IllegalArgumentException
     *             if no valid parameters were specified
     */
    String encode(Map<String, Object> params) throws IllegalArgumentException;

    /**
     * Decodes a link back token into its component parameters.
     * 
     * @param token
     *            the encoded token
     * @return the parameter values.
     * @throws IllegalArgumentException
     *             if any unknown or invalid parameters are encountered.
     */
    Map<String, String> decode(String token) throws IllegalArgumentException;

}
