/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.percussion.tomcat.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class PSSecurityFilter extends GenericFilterBean {

    private static String PERC_SECURITY_PROPS_ROOT = "/conf/perc/perc-security.properties";
    private String CONTENT_SECURITY_POLICY_NAME= "ContentSecurityPolicy";
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
        try {
            Properties props = new Properties();
            //Find in local Webapp,
            InputStream in = null;
            String tomcatBase = System.getProperty(CATALINA_BASE);
            if (tomcatBase != null) {
                in = new FileInputStream(tomcatBase + PERC_SECURITY_PROPS_ROOT);
            }

            if (in != null) {
                props.load(in);
                String val = props.getProperty(CONTENT_SECURITY_POLICY_NAME);
                if (val != null && val.trim() != "") {
                    CONTENT_SECURITY_POLICY_VALUE = val;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties readPropertiesFile(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            fis.close();
        }
        return prop;
    }

        public void destroy() {}

}
