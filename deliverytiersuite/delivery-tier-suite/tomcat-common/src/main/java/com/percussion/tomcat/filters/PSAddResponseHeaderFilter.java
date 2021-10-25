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

package com.percussion.tomcat.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.CacheControl;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PSAddResponseHeaderFilter implements Filter {

    private static final Logger log = LogManager.getLogger(PSAddResponseHeaderFilter.class);

    private static String PERC_SECURITY_PROPS_ROOT = "/conf/perc/perc-security.properties";
    private static String CATALINA_BASE = "catalina.base";
    private Long cachingAgeTimeValue = null;
    private TimeUnit cachingAgeTimeUnit = null;
    private String CACHING_MAX_AGE_VALUE_PROPERTY_KEY="cacheControlMaxAgeValue";
    private String CACHING_MAX_AGE_UNIT_PROPERTY_KEY="cacheControlMaxAgeUnit";
    private static final String PERC_SECURITY_PROPERTIES = "/WEB-INF/perc-security.properties";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if ( response instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) response;

            //If global perc-security.properties does not have cache-control properties then look into service level perc-security.properties.
            //This will help service level configuration by removing the global value and configuring each service level values. If service level values do not exist set default value.
            if(cachingAgeTimeValue == null || cachingAgeTimeUnit == null){
                Properties contextProps = new Properties();
                ServletContext contextPath = request.getServletContext();
                InputStream in = contextPath.getResourceAsStream(PERC_SECURITY_PROPERTIES);
                contextProps.load(in);

                String contextCachingAgeTimeVal = contextProps.getProperty(CACHING_MAX_AGE_VALUE_PROPERTY_KEY);
                if (contextCachingAgeTimeVal != null && contextCachingAgeTimeVal.trim() != "") {
                    cachingAgeTimeValue = Long.parseLong(contextCachingAgeTimeVal);
                }else{
                    cachingAgeTimeValue = Long.valueOf(60);
                }

                String contextCachingAgeUnitVal = contextProps.getProperty(CACHING_MAX_AGE_UNIT_PROPERTY_KEY);
                if (contextCachingAgeUnitVal != null && contextCachingAgeUnitVal.trim() != "") {
                    cachingAgeTimeUnit = TimeUnit.valueOf(contextCachingAgeUnitVal);
                }else{
                    cachingAgeTimeUnit = TimeUnit.SECONDS;
                }

            }

            httpResp.setHeader("Cache-Control", CacheControl.maxAge(cachingAgeTimeValue, cachingAgeTimeUnit).getHeaderValue()+", "+CacheControl.noStore().getHeaderValue()+", "+CacheControl.noCache().getHeaderValue());
            httpResp.setHeader("Pragma", "no-cache");
            chain.doFilter(request, response);
        }else{
            chain.doFilter(request,response);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Properties props = new Properties();
        //Find in local Webapp,
        String tomcatBase = System.getProperty(CATALINA_BASE);
        if (tomcatBase != null) {
            try (
                    InputStream in = new FileInputStream(
                            tomcatBase + PERC_SECURITY_PROPS_ROOT)) {
                props.load(in);
            } catch (IOException e) {
                log.error(e.getMessage());
                log.debug(e);
            }
        }

        String cachingAgeTimeVal = props.getProperty(CACHING_MAX_AGE_VALUE_PROPERTY_KEY);
        if (cachingAgeTimeVal != null && cachingAgeTimeVal.trim() != "") {
            cachingAgeTimeValue = Long.parseLong(cachingAgeTimeVal);
        }

        String cachingAgeUnitVal = props.getProperty(CACHING_MAX_AGE_UNIT_PROPERTY_KEY);
        if (cachingAgeUnitVal != null && cachingAgeUnitVal.trim() != "") {
            cachingAgeTimeUnit = TimeUnit.valueOf(cachingAgeUnitVal);
        }
    }

    @Override
    public void destroy() {

    }
}
