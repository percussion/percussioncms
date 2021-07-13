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
package com.percussion.util;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class PSBaseHttpUtils
{

    /**
     * Reads the HTTP status line, which consists a series of (possibly
     * encoded) characters terminated by a \r\n.
     * <P>
     * It turns out that some browsers (like Netscape 4.6) do not escape
     * non-ASCII characters (like &uuml;) properly. For example, Netscape sends
     * &uuml; as byte value 252, which is the ISO-8859-1 (Latin) code for
     * that character, whereas IE sends a URL encoded (using %) hex
     * representation of the bytes.
     * <P>
     * The behavior of IE is bizarre when it comes to things like forms. If the
     * form's page is in ISO-8859-1, but you have "Always send URLs as UTF-8"
     * checked in the options, IE will send the first part of the URL (the
     * directory, the page name, etc.) as ASCII-encoded UTF-8, but will
     * send the second part as ASCII-encoded ISO-8859-1 (or whatever the
     * form is in). We currently cannot deal with this situation, so the
     * page author should either encode the page all in UTF-8 or put an
     * accept-charset attribute on the FORM tag.
     * <P>
     * This method reads the line from the input stream and tries to guess
     * the encoding of the bytes.
     */
    static String readStatusLine(PushbackInputStream in)
            throws IOException
    {
        try(java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream(100)) {

            // first read the raw bytes up to the line terminator
            // (\r and \n are the same in all ASCII-compatible character sets)
            int c;
            for (c = in.read(); c != -1; c = in.read()) {
                if (c == '\n') {
                    break;
                } else if (c == '\r') {
                    // if this is '\r', does '\n' follow (which should be skipped)
                    c = in.read();
                    if (c != '\n') {
                        in.unread(c);
                    }
                    break;
                } else {
                    bout.write(c);
                }
            }

            // now try to guess the encoding of these bytes (Latin or UTF8) ?
            byte[] bytes = bout.toByteArray();
            String ret;
            final int enc = PSCharSets.guessEncoding(bytes);
            if ((PSCharSets.CS_UTF8 & enc) != 0) {
                ret = new String(bytes, StandardCharsets.UTF_8);
            } else if ((PSCharSets.CS_ISO8859_1 & enc) != 0) {
                ret = new String(bytes, "ISO8859_1");
            } else {
                ret = new String(bytes, StandardCharsets.UTF_8);
            }

            return ret;
        }
    }

    /**
     * Parse the HTTP Content-Type header.
     * <P>
     * The content-type header can have many parts. The syntax, which
     * is summarized here, is defined fully in the HTTP 1.1 spec
     * (RFC 2068), especially in section 3.7.
     * <P>
     * It is important to note that the HTTP 1.1 spec allows unlimited
     * whitespace between tokens. From section 2.1:
     * <P>
     * <BLOCKQUOTE>
     * The grammar described by this specification is word-based. Except
     * where noted otherwise, linear whitespace (LWS) can be included
     * between any two adjacent words (token or quoted-string), and
     * between adjacent tokens and delimiters (tspecials), without
     * changing the interpretation of a field. At least one delimiter
     * (tspecials) must exist between any two tokens, since they would
     * otherwise be interpreted as a single token.
     * </BLOCKQUOTE>
     * <PRE>
     * CTL            = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
     * CHAR            = <any US-ASCII character (octets 0 - 127)>
     * token          = 1*<any CHAR except CTLs or tspecials>
     *
     * TEXT            = <any OCTET except CTLs, but including LWS>
     * quoted-string   = ( <"> *(qdtext) <"> )
     * qdtext         = <any TEXT except <">>
     *
     * media-type      = type "/" subtype *( ";" parameter )
     * type            = token
     * subtype         = token
     *
     * parameter      = attribute "=" value
     * attribute      = token
     * value          = token | quoted-string
     * </PRE>
     * The media-type is usually something like this:
     *    <CODE>application/x-www-form-urlencoded</CODE>
     * conforming to MIME type syntax.
     * <P>
     * After the media-type, there may be zero or more parameters,
     * each of which starts with a semicolon. That would look something
     * like this:
     *    <CODE>application/x-www-form-urlencoded ; charset=US-ASCII; foo="bar"</CODE>
     * <P>
     * Each parameter is of the form attribute=value, where value may
     * be either a token or a quoted string. A quoted string is a string
     * that starts and ends with a double quote ("). Since a token cannot
     * start with a double quote (or any other of a list of special
     * characters), our code makes the valid assumption that any value
     * starting with a double quote must also end in a double quote.
     * <P>
     * One of these parameters can be the charset parameter, which
     * specifies the character encoding of the form data. If this
     * parameter is left out, the spec says we must assume its value is
     * ISO-8859-1.
     *
     *
     * @param    contentType The value of the Content-Type HTTP header. If
     * <code>null</code> or empty, <code>null</code> is returned.
     *
     * @param    params A map in which we store the Content-Type parameters,
     * keyed by their lowercased names. The map can be null, in which case
     * no parameter values will be parsed or stored, except for the MIME type
     * which is the return value of this function.
     *
     * @return  String The media (MIME) type which makes up the first part
     * of the Content-Type header value. The media type will be all lowercase.
     * If <code>null</code> is supplied for the content type, <code>null</code>
     * is returned.
     */
    @SuppressWarnings("unchecked")
    public static String parseContentType(String contentType, Map params)
    {
        if (StringUtils.isBlank(contentType))
            return null;

        // strip off the media type which occurs before the optional
        // params string
        String mediaType;

        final String cType = contentType.trim();
        final int cTypeLen = cType.length();

        int semiPos = cType.indexOf(';');
        if(semiPos < 0)
            semiPos = cType.indexOf(',');
        if (semiPos < 0)
            semiPos = cTypeLen;

        mediaType = cType.substring(0, semiPos).trim().toLowerCase();

        if (params != null && semiPos != cTypeLen)
        {
            String remainder = cType.substring(semiPos+1);
            parseHttpParamsString(remainder, params);
        }

        return mediaType;
    }

    /**
     * Parse an HTTP params string which consists of 0 or more
     * attribute=value pairs separated by semicolons. Unlimited whitespace
     * is allowed between tokens. Values can also be quoted, which means
     * that special characters (such as = and ;) should be ignored
     * between the quote delimiters.
     *
     * The params will be stored in the map as LCASE(name) -> value.
     */
    @SuppressWarnings("unchecked")
    protected static int parseHttpParamsString(String paramStr, Map params)
    {
        final String str = paramStr.trim();
        final int strLen = str.length();

        int semiPos = 0;

        while (semiPos >= 0 && semiPos < strLen)
        {
            if (semiPos == 0)
                semiPos = -1; // special case for first param

            int nextSemiPos = str.indexOf(';', semiPos + 1);
            if (nextSemiPos < 0)
                nextSemiPos = strLen;

            String param = str.substring(semiPos + 1, nextSemiPos).trim();

            // must have at least one char in the attribute, an equals sign,
            // and at least one char in the value, making the shortest
            // possible param ("a=b") length 3.
            if (param.length() < 3)
            {
                throw new IllegalArgumentException("HTTP param length too short : "+param + " for string" + str);
            }

            int eqPos = param.indexOf('=');
            if (eqPos < 2 || eqPos == (param.length() - 1))
            {
                throw new IllegalArgumentException("HTTP param invalid equals position for param : "+param + " for string" + str);
            }

            String attribute = param.substring(0, eqPos).trim();
            String value = param.substring(eqPos + 1).trim();

            // ignore delimiters within quoted strings
            char start = value.charAt(0);
            char end = value.charAt(value.length() - 1);
            while (start == '"' && end != '"')
            {
                int quotePos = str.indexOf('"', nextSemiPos + 1);
                if (quotePos < 0)
                {
                    throw new IllegalArgumentException("HTTP cannot find next quote in : "+str + " from position " +nextSemiPos + 1);
                }
                nextSemiPos = str.indexOf(';', quotePos + 1);
                param = str.substring(semiPos + 1, nextSemiPos).trim();
                value = param.substring(eqPos + 1).trim();
                end = value.charAt(value.length() - 1);
            }

            params.put(attribute.toLowerCase(), value);
            // advance to the next parameter
            semiPos = nextSemiPos;
        }

        return strLen;
    }

    /**
     * Build HTTP Content-Type header by concatenating mimeType and encoding.
     *
     * @param   mimeType   mime type to add to header. if null or empty, an
     *                     empty header will be generated.
     * @param   encoding   character set encoding to add to header. if null or
     *                     empty, charset will not be included in header.
     * @return  The HTTP Content-Type header String; may be empty, but will
     *          not be <code>null</code>
     */
    public static String constructContentTypeHeader(String mimeType,
                                                    String encoding)
    {
        String contentHeader;
        if (null == mimeType || 0 == mimeType.length())
            contentHeader = "";
        else
        {
            if (null == encoding || 0 == encoding.length())
                contentHeader = mimeType;
            else
                contentHeader = mimeType + "; charset=" + encoding;
        }
        return contentHeader;
    }

    /**
     * Looks for the first occurrence of '?' and returns everything up to, but
     * not including that char.
     *
     * @param url Anything is allowed.
     *
     * @return All chars up to, but not including the param string delimiter. If
     *         there isn't one, the supplied string is returned.
     */
    public static String parseHttpPath(String url)
    {
        if (StringUtils.isBlank(url))
            return url;
        int pos = url.indexOf('?');
        if (pos < 0)
            return url;
        return url.substring(0, pos);
    }

    /**
     * Gets the path component of a given URL. It is the part between the port
     * and the first occurrence of '?'.
     *
     * @param url well formed URL string.
     *
     * @return the path component. It may be <code>null</code> or empty.
     *
     * @throws MalformedURLException if the URL is not well formed.
     *
     * @see URL#getPath()
     */
    public static String getPath(String url) throws MalformedURLException
    {
        URL origUrl = new URL(url);
        return origUrl.getPath();
    }

    /**
     * Convenience method that calls
     * {@link #parseQueryParamsString(String, boolean, boolean)
     * parseQueryParamString(query, <code>false</code>, <code>true</code>)}.
     */
    public static Map<String, Object> parseQueryParamsString(String query)
    {
        return parseQueryParamsString(query, false, true);
    }


    /**
     * Parses the query part of a url, extracting the name/value pairs and
     * returning them in a map. The values for multi-valued params are stored in
     * a <code>List</code> that maintains the order of the values in the
     * supplied string. Missing values are returned as empty strings. Each name
     * and value is URL decoded.
     * <p>
     * Note
     * <p>
     * This is a simple implementation and does not conform to the full http URL
     * specification.
     *
     * @param query A string of the form [path?]p1=v1&p2=v2... where [path?] is
     *        optional (searches for the first occurrence of ? and only uses rest
     *        of supplied string), px is the parameter name and vx is the value
     *        (which may be missing.) Parameter names may appear more than once.
     *        May be <code>null</code> or empty.
     * @param lowerCaseNames If <code>true</code>, the names are lower-cased
     *        before being added to the returned map.
     * @param urlDecode If <code>true</code>, the name and value will be
     *        decoded, otherwise they will be added to the map w/o decoding.
     * @return Never <code>null</code>.
     * @throws RuntimeException If the query param string is malformed.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseQueryParamsString(String query,
                                                             boolean lowerCaseNames, boolean urlDecode)
    {
        Map<String, Object> results = new HashMap<>();
        if (StringUtils.isBlank(query))
            return results;
        try
        {
            query = query.trim();
            int queryOffset = query.indexOf("?");
            if (queryOffset >= 0)
                query = query.substring(queryOffset+1);
            StringTokenizer toker1 = new StringTokenizer(query, "&");
            while (toker1.hasMoreTokens())
            {
                String pair = toker1.nextToken().trim();
                StringTokenizer toker2 = new StringTokenizer(pair, "=");
                String name;
                String value = StringUtils.EMPTY;
                if (toker2.hasMoreTokens())
                {
                    name = toker2.nextToken().trim();
                }
                else
                {
                    throw new RuntimeException(
                            "Invalid query string: missing param name");
                }
                if (toker2.hasMoreTokens())
                    value = toker2.nextToken().trim();
                if (toker2.hasMoreTokens())
                {
                    throw new RuntimeException(
                            "Invalid query string: invalid name/value pair: " + pair);
                }

                if (StringUtils.isBlank(name))
                {
                    throw new RuntimeException("Invalid query string: missing name"
                            + pair);
                }
                if (urlDecode)
                    name = URLDecoder.decode(name, "UTF8");
                if (lowerCaseNames)
                    name = name.toLowerCase();
                if (StringUtils.isBlank(value))
                    value = StringUtils.EMPTY;
                else if (urlDecode)
                    value = URLDecoder.decode(value, "UTF8");
                if (results.containsKey(name))
                {
                    Object current = results.get(name);
                    List<String> values;
                    if (!(current instanceof List))
                    {
                        values = new ArrayList<>();
                        results.put(name, values);
                        values.add((String) current);
                    }
                    else
                        values  = (List<String>) current;
                    values.add(value);
                }
                else
                    results.put(name, value);
            }
            return results;
        }
        catch (UnsupportedEncodingException e)
        {
            //UTF8 is always supported on java
            throw new RuntimeException(e);
        }
    }

    /**
     * If the supplied URL contains a query param with the supplied name
     * (case-insensitive,) the name/value pair is removed from the url,
     * otherwise, the URL is unmodified.
     *
     * @param url May be <code>null</code> or empty. A URL of the form
     *        <code>[path[?p1=[v1][&p2=[v2]]]]</code>.
     * @param paramName Anything allowed. Nothing is done to the name before
     *        comparing to the query params in the URL.
     * @return The supplied URL, without the param whose name matched the
     *         supplied one.
     */
    public static String removeQueryParam(String url, String paramName)
    {
        Map<String, Object> params = PSBaseHttpUtils.parseQueryParamsString(url,
                true, true);
        if (!params.containsKey(paramName.toLowerCase()))
            return url;

        params = PSBaseHttpUtils.parseQueryParamsString(url, false, false);
        for (String key : params.keySet())
        {
            if (key.equalsIgnoreCase(paramName))
            {
                params.remove(key);
                break;
            }
        }

        String result = PSBaseHttpUtils.addQueryParams(PSBaseHttpUtils.parseHttpPath(url),
                params, false);
        if (result.endsWith("?"))
            result = result.substring(0, result.length()-1);
        return result;
    }

    /**
     * Appends the supplied name/value pairings onto the end of the supplied path
     * to form a valid http scheme URL (assuming the supplied path is valid, the
     * path is not checked for its conformance to the scheme.)
     * <p>
     * Note: this is not a fully compliant implementation, just what I needed for
     * now.
     *
     * @param path Anything is allowed. This content will become the prefix in
     *        the returned string. It may already contain a query string.
     *        Trailing separators are handled correctly.
     *
     * @param params If provided, the values must either be <code>String</code>
     *        or <code>Collection</code> objects. <code>null</code> or empty
     *        keys are skipped. No URL encoding is done to the names or values.
     *
     * @param urlEncode If <code>true</code>, the name and value will be
     *        encoded before appending, otherwise they will be added to the map
     *        as is.
     * @return The generated string. Never <code>null</code>, may be empty.
     */
    @SuppressWarnings("unchecked")
    public static String addQueryParams(String path, Map<String, Object> params,
                                        boolean urlEncode)
    {
        StringBuilder result = new StringBuilder();
        if (path != null)
            result.append(path);
        if (params == null)
            return result.toString();

        if (result.length() > 0)
        {
            if (result.indexOf("?") < 0)
                result.append('?');
            else
            {
                char last = result.charAt(result.length()-1);
                if (last != '&' && last != '?')
                    result.append('&');
            }
        }
        for (String key : params.keySet())
        {
            Object o = params.get(key);
            if (o instanceof Collection)
            {
                Collection<String> values = (Collection<String>) o;
                for (String value : values)
                    appendKeyValuePair(result, key, value, urlEncode);
            }
            else
                appendKeyValuePair(result, key, o.toString(), urlEncode);
        }
        //strip off trailing '?' or '&'
        return result.substring(0, result.length()-1);
    }

    /**
     * Appends <code>param=value&</code> onto the supplied buffer if param is
     * not blank.
     *
     * @param result The target of the appending. Assumed not <code>null</code>.
     * Assumed that there is a trailing '&' or '?' character.
     * @param param Anything allowed. If blank, nothing is appended.
     * @param value Anything is allowed.
     */
    private static void appendKeyValuePair(StringBuilder result, String param,
                                           String value, boolean urlEncode)
    {
        try
        {
            if (StringUtils.isBlank(param))
                return;
            if (urlEncode)
                param = URLEncoder.encode(param, "UTF-8");
            result.append(param);
            result.append('=');
            if (value != null)
            {
                if (urlEncode)
                    value = URLEncoder.encode(value, "UTF-8");
                result.append(value);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            //should never happen w/ UTF8
            throw new RuntimeException(e);
        }
        result.append('&');
    }

    /**
     * Constant to use to indicate the operating system of the requestor is
     * Macintosh based.
     */
    static final String OS_MACINTOSH = "Macintosh";

    /**
     * Constant to use to indicate the operating system of the requestor is
     * Windows based.
     */
    static final String OS_WINDOWS = "Windows";

    /**
     * Constant to use to indicate the operating system of the requestor is
     * Unix based.
     */
    static final String OS_UNIX = "Unix";

    /**
     * Constant to use to indicate the operating system of the requestor is
     * unknown.
     */
    static final String OS_OTHER = "Other";

    /**
     * Constant for the path separator to use when the requestor's OS is
     * Mac based.
     */
    static final String SEP_MACINTOSH = ":";

    /**
     * Constant for the path separator to use when the requestor's OS is
     * Windows based.
     */
    static final String SEP_WINDOWS = "\\";

    /**
     * Constant for the path separator to use when the requestor's OS is
     * Unix based.
     */
    static final String SEP_UNIX = "/";

    /**
     * Constant for the path separator to use when the requestor's OS is
     * unknown.
     */
    static final String SEP_OTHER = "/";

}
