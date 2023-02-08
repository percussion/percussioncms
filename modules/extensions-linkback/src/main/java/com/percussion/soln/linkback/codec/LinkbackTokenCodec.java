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
