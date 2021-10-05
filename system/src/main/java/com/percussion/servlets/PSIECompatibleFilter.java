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

package com.percussion.servlets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;


public class PSIECompatibleFilter implements Filter
{
   public void init(FilterConfig arg0) throws ServletException {  
   } 
   
   public void destroy() {  
   } 
   
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {  

       //Add the Edge header
       HttpServletResponse wrappedResponse = (HttpServletResponse) response;
       WrapperResponse responseWrapper = new WrapperResponse(wrappedResponse);
       responseWrapper.addHeader("X-UA-Compatible", "IE=Edge");


      String userAgentStr = ((HttpServletRequest)request).getHeader("user-agent");

      // Check to see if we have to do with a IE request. If so, we return a wrapped request.  
      if (userAgentStr != null && (userAgentStr.contains("MSIE 1") || userAgentStr.contains("Trident"))) {  
        ServletRequest wrappedRequest = new WrapperRequest((HttpServletRequest)request);  
        chain.doFilter(wrappedRequest, responseWrapper);
      } else {  
        chain.doFilter(request, responseWrapper);
      }  
    }
   
   private class WrapperRequest extends HttpServletRequestWrapper {
      
      public WrapperRequest(HttpServletRequest request) {  
        super(request);  
      }  
    
      public String getHeader(String name) {  
         
        // IE 10: replace 'MSIE 10.x' into 'MSIE 9.x' for ADF 11.1.1.6 and below  
        HttpServletRequest request = (HttpServletRequest)getRequest();  
        if ("user-agent".equalsIgnoreCase(name) && request.getHeader("user-agent").contains("MSIE 10")) {  
          return request.getHeader("user-agent").replaceAll("MSIE [^;]*;", "MSIE 9.0;");  
        }
        
        // IE 11: replace the whole agent-string into an MSIE 9.0 string for ADF 11.1.1.6 and below  
        // or MSIE 10.0 for ADF 11.1.1.7 or higher  
        if ("user-agent".equalsIgnoreCase(name) && request.getHeader("user-agent").contains("Trident")) {  
          //Choose your preferred version  
            return "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.2; Trident/6.0)";
        }
        return request.getHeader(name);  
      }  
    }

    private class WrapperResponse extends HttpServletResponseWrapper {
        public WrapperResponse(HttpServletResponse response) {
            super(response);
        }
    }
}
