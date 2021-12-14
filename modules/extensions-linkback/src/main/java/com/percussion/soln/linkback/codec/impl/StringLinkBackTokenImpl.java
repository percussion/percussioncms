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

package com.percussion.soln.linkback.codec.impl;

import com.percussion.soln.linkback.codec.LinkbackTokenCodec;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
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

    private static final Logger log = LogManager.getLogger(StringLinkBackTokenImpl.class);

    private static byte bitMask;

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
        try {
            byte[] tokenBytes = Base64.decodeBase64(token.getBytes(ASCII));
            for (int i = 0; i < tokenBytes.length; i++) {
                tokenBytes[i] ^= bitMask;
            }
            codedToken = new String(tokenBytes, ASCII);
        } catch (UnsupportedEncodingException ex) { // Should never happen ASCII
            // is always supported.
            log.error("Unsupported Encoding " + ASCII, ex);
            throw new IllegalStateException("Encoding Error");
        }
        log.debug("Decoded token is {}", codedToken);
        String[] parts = codedToken.split(DELIM);
        for (int i = 0; i < parts.length; i++) {
            decodePart(oparm, parts[i]);
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
        try {
            byte[] tokenBytes = sb.toString().getBytes(ASCII);
            for (int i = 0; i < tokenBytes.length; i++) {
                tokenBytes[i] ^= bitMask;
            }
            token = new String(Base64.encodeBase64(tokenBytes), ASCII);
            return token;
        } catch (UnsupportedEncodingException ex) {
            // this should never happen, ascii is always supported.
            log.error("Unsupported Encoding {}, Error: {}", ASCII, ex.getMessage());
            log.debug(ex.getMessage(), ex);
            return null;
        }
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
            log.warn("Missing value in parameter map {}", pname);
        }

    }

    @SuppressWarnings("unchecked")
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

    private static final String ASCII = "ASCII";
}
