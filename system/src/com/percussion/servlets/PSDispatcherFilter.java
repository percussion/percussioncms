/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.servlets;

import com.percussion.security.SecureStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PSDispatcherFilter implements Filter {

    private static final String RHYTHMYX = "/Rhythmyx";

    private static final Pattern pattern = Pattern.compile("^.+\\/\\/[^\\/]+\\/Sites\\/([^\\/]*)", Pattern.MULTILINE);

    private static final String[] bannedPaths = new String[]{
            "/WEB-INF/",
            "/lib/",
            "/bin/",
            "/backups/",
            "/jetty/",
            "/logs/",
            "/temp/",
            "/util/",
            "/user/",
            "/patch/",
            "/css/"
    };

    private static final String[] resourcePaths = new String[] {
            "/Sites/",
            "/Assets/",
            "/rx_resources",
            "/sys_resources",
            "/cm/",
            "/web_resources",
            "/tmx/",
            "/services/",
            "/sessioncheck",
            "/webservices/",
            "/designwebservices/",
            "/content/",
            "/rest",
            "/v8",
            "/assembler/",
            "/contentlist",
            "/sitelist",
            "/login",
            "/logout",
            "/rxwebdav",
            "/ui/actionpage/panel",
            "/user/apps",
            "/publisher/",
            "/linkback/",
            "/servlet/",
            "/assembly/aa",
            "/contentui/aa",
            "/adf/",
            "/uploadAssetFile",
            "/textToImage/",
            "/Designer",
            "/Rhythmyx/",
            "/Rhythmyx/services"

    };

    /**
     * Logger to use, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSDispatcherFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        String strippedPath = path.startsWith(RHYTHMYX) ? StringUtils.substringAfter(path,RHYTHMYX) : path;
        String newPath =
                SecureStringUtils.cleanWildPath(resourcePaths,path,request.getRemoteAddr());


        if(newPath == null ||( Stream.of(bannedPaths).anyMatch(strippedPath::startsWith) && !(Stream.of(resourcePaths).anyMatch(strippedPath::startsWith)))){
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (Stream.of(resourcePaths).anyMatch(strippedPath::startsWith))
        {
            newPath = strippedPath;
        } else if (!path.startsWith(RHYTHMYX))
            newPath = addSitePrefixFromReferer((HttpServletRequest) request, path);

        if (newPath.equals(path))
            chain.doFilter(request, response); // Goes to container's own default servlet.
        else
            request.getRequestDispatcher(newPath).forward(req, resp);

    }

    /**
     * This is not a good way of doing this.  Trying to make preview paths handle like they are
     * on the original site we have to handle every unknown request and look at the referer.
     * There will be problems if there are clashes with internal resources as well being difficult
     * to see how this is working as it is hidden in this function and it is not normal to change the
     * response based upon the referer.
     *
     * @param request the servlet request
     * @param path  The path with initial /Rhythmyx removed if exists
     * @return The path with /Sites/{sitename}/ prefix unless in set of resource paths
     */
    private String addSitePrefixFromReferer(HttpServletRequest request, String path) {

            String referrer = request.getHeader("referer");

            if (referrer != null) {
                String site =null;
                try {

                    List<NameValuePair> params = URLEncodedUtils.parse(new URI(referrer), StandardCharsets.UTF_8);
                    NameValuePair siteParam = null;
                    /*NameValuePair siteParam = params.stream()
                            .filter(p -> p.getName().equals("site"))
                            .findFirst()
                            .orElse(null);
*/
                    if (siteParam != null)
                    {
                        site = siteParam.getValue();
                    }
                    else
                    {
                        Matcher m = pattern.matcher(referrer);

                        if (m.find()) {
                            site = m.group(1);
                            log.debug("Found site referer = {}" , site);

                        }


                    }
                } catch (URISyntaxException e) {
                    log.debug("Invalid referrer url");
                }
                if (site != null)
                    path = "/Sites/" + site + path;
            }

        return path;
    }


    @Override
    public void destroy() {

    }
}
