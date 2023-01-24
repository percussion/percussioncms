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
