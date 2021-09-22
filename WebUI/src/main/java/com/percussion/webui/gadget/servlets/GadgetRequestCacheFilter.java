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

package com.percussion.webui.gadget.servlets;

import com.percussion.error.PSExceptionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class validates the urls passed in for Gadgets Meta calls to make sure urls passed in are valid.
 */
public class GadgetRequestCacheFilter implements Filter {

    private static final Logger log = LogManager.getLogger(GadgetRequestCacheFilter.class.getName());

        private FilterConfig filterConfig = null;

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            //Makes a Wrapper for Request so that it can be used multiple times.
            request = new GadgetRequestWrapper((HttpServletRequest) request);

            //cm/gadgets/ifr passes in param "url" for gadget that needs to be validated to be not malformed.
            String gadgetUrl = request.getParameter("url");
            if(gadgetUrl != null && !gadgetUrl.isEmpty()) {
                try {
                    if (!PSGadgetUtils.isValidGadgetPathInUrl((HttpServletRequest) request, new URI(gadgetUrl))) {
                        ((HttpServletResponse) response).sendError(404);
                        return;
                    }
                }catch (URISyntaxException e) {
                    ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    return ;
                }
            }

            //cm/gadget/metadata is a POST request that passes in JSON object for gadget metadata that
            //includes url. Thus sanitizing that passed in url.
            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            JSONObject reqBody = null;
            try(InputStreamReader is = new InputStreamReader(request.getInputStream(),
                    encoding)) {
                String body = IOUtils.toString(is);
                if(body != null && !body.isEmpty()) {
                    reqBody = new JSONObject(new String(body.getBytes(), encoding));
                    if (!validURLs((HttpServletRequest) request, (HttpServletResponse) response, reqBody)) {
                        ((HttpServletResponse) response).sendError(404);
                        return;
                    }
                }
            }

            chain.doFilter(request, response);
        }

    private boolean validURLs(HttpServletRequest request, HttpServletResponse response, JSONObject reqBody)  {
        try {
            JSONArray gadgets = (JSONArray) reqBody.get("gadgets");
            if (gadgets != null) {
                for (int i = 0; i < gadgets.length(); i++) {
                    JSONObject gadget = gadgets.getJSONObject(i);
                    String url = (String) gadget.get("url");
                    if (!PSGadgetUtils.isValidGadgetPathInUrl((HttpServletRequest) request, new URI(url))) {
                        return false;
                    }
                }
            }
            return true;
        }catch (URISyntaxException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error(PSExceptionUtils.getMessageForLog(e));
            return false;
        }

    }

        public void init(FilterConfig filterConfiguration) throws ServletException {
            this.filterConfig = filterConfiguration;
        }

        public void destroy() {
            this.filterConfig = null;
        }
    }
