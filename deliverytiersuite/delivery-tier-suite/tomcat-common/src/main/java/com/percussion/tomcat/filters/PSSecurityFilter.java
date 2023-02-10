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

package com.percussion.tomcat.filters;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class PSSecurityFilter extends GenericFilterBean {

    private static final Logger log = LogManager.getLogger(PSSecurityFilter.class);

    private static String PERC_SECURITY_PROPS_ROOT = "/conf/perc/perc-security.properties";
    private String CONTENT_SECURITY_POLICY_NAME= "contentSecurityPolicy";
    private String CONTENT_SECURITY_POLICY_VALUE= "default-src 'self'";
    private static String CATALINA_BASE = "catalina.base";

    @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


         if ( response instanceof HttpServletResponse) {
             HttpServletResponse httpResp = (HttpServletResponse) response;

             httpResp.addHeader(CONTENT_SECURITY_POLICY_NAME, CONTENT_SECURITY_POLICY_VALUE);
             chain.doFilter(request, response);
         }else{
             chain.doFilter(request,response);
         }

        }


    @Override
    protected void initFilterBean() throws ServletException {

            Properties props = new Properties();
            //Find in local Webapp,
            String tomcatBase = System.getProperty(CATALINA_BASE);
            if (tomcatBase != null) {
                try (
                        InputStream in = new FileInputStream(
                                tomcatBase + PERC_SECURITY_PROPS_ROOT)) {
                    props.load(in);
                } catch (IOException e) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                }
            }

            String val = props.getProperty(CONTENT_SECURITY_POLICY_NAME);
            if (val != null && val.trim() != "") {
                CONTENT_SECURITY_POLICY_VALUE = val;
            }

    }

    private Properties readPropertiesFile(String fileName) throws IOException {
        Properties prop = null;
        try(FileInputStream fis = new FileInputStream(fileName) ) {
            prop = new Properties();
            prop.load(fis);
        } catch(IOException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }

        return prop;
    }

}
