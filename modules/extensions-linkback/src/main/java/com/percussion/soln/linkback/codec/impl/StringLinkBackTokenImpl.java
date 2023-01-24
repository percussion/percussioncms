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

package com.percussion.soln.linkback.codec.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.soln.linkback.codec.LinkbackTokenCodec;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default codec implementation used by the GenericLinkbackController.
 * 
 * @author DavidBenua
 * 
 */
public class StringLinkBackTokenImpl implements LinkbackTokenCodec {

    private static final Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);

    private final byte bitMask;

    /**
     * Default Constructor.
     */
    public StringLinkBackTokenImpl() {
        bitMask = (byte) 0x55;

    }

    /**
     * @see com.percussion.soln.linkback.codec.LinkbackTokenCodec#decode(java.lang.String)
     */
    public Map<String, String> decode(String token) throws IllegalArgumentException {
        Map<String, String> oparm = new HashMap<>();
        String codedToken;
        byte[] tokenBytes = Base64.decodeBase64(token.getBytes(StandardCharsets.US_ASCII));
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i] ^= bitMask;
        }
        codedToken = new String(tokenBytes, StandardCharsets.US_ASCII);
        log.debug("Decoded token is {}", codedToken);
        String[] parts = codedToken.split(DELIM);
        for (String part : parts) {
            decodePart(oparm, part);
        }
        return oparm;
    }

    private void decodePart(Map<String, String> pmap, String part) {
        if (StringUtils.isBlank(part)) { // do nothing
            log.debug("Part is blank");
            return;
        }
        char first = part.charAt(0);
        String rest = (part.length() > 1) ? part.substring(1) : "";
        log.debug("Matching part {} - {}", first, rest);
        switch (first) {
        case CONTENTID:
            pmap.put(IPSHtmlParameters.SYS_CONTENTID, rest);
            break;
        case REVISION:
            pmap.put(IPSHtmlParameters.SYS_REVISION, rest);
            break;
        case TEMPLATE:
            pmap.put(IPSHtmlParameters.SYS_TEMPLATE, rest);
            break;
        case SITE:
            pmap.put(IPSHtmlParameters.SYS_SITEID, rest);
            break;
        case FOLDER:
            pmap.put(IPSHtmlParameters.SYS_FOLDERID, rest);
            break;
        default:
            log.warn("Unrecognized part {} - {}", first, rest);
        }
    }

    /**
     * @see com.percussion.soln.linkback.codec.LinkbackTokenCodec#encode(java.util.Map)
     */
    public String encode(Map<String, Object> params) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        appendPart(params, sb, IPSHtmlParameters.SYS_CONTENTID, CONTENTID);
        appendPart(params, sb, IPSHtmlParameters.SYS_REVISION, REVISION);
        String templateParamName = IPSHtmlParameters.SYS_TEMPLATE;
        /*
         * Sometimes the old sys_variantid parameter is used instead of
         * sys_template. We must handle that case.
         */
        if (params.containsKey(IPSHtmlParameters.SYS_VARIANTID))
            templateParamName = IPSHtmlParameters.SYS_VARIANTID;
        appendPart(params, sb, templateParamName, TEMPLATE);
        appendPart(params, sb, IPSHtmlParameters.SYS_SITEID, SITE);
        appendPart(params, sb, IPSHtmlParameters.SYS_FOLDERID, FOLDER);
        log.debug("Encoded Raw value is {}", sb);
        String token;
        byte[] tokenBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i] ^= bitMask;
        }
        token = new String(Base64.encodeBase64(tokenBytes), StandardCharsets.US_ASCII);
        return token;
    }

    private static void appendPart(Map<String, Object> params, StringBuilder sb, String pname, char marker) {
        String value = simplifyValue(params.get(pname));
        if (StringUtils.isNotBlank(value)) {
            if (sb.length() > 0) {
                sb.append(DELIM);
            }
            sb.append(marker);
            sb.append(value);
        } else {
            log.debug("Missing value in parameter map {}", pname);
        }

    }

    public static String simplifyValue(Object value) {
        if (value == null) {
            log.debug("null value");
            return null;
        }
        String sval;
        if (value instanceof String[]) {
            String[] x = (String[]) value;
            if (x.length == 0) {
                log.trace("Empty String array");
                return "";
            }
            sval = x[0];
            log.trace("Converted String[] to {} {}", sval, value);
        } else if (value instanceof List) {
            List x = (List) value;
            if (x.isEmpty()) {
                log.debug("Empty List");
                return "";
            }
            sval = x.get(0).toString();
            log.trace("Converted List to {} {}", sval, value);
        } else {
            sval = value.toString();
            log.trace("Converted Object to {}", sval);
        }
        return sval;
    }

    public static final char CONTENTID = 'C';

    public static final char REVISION = 'R';

    public static final char TEMPLATE = 'T';

    public static final char SITE = 'S';

    public static final char FOLDER = 'F';

    public static final String DELIM = "-";

}
