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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.security;

import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.PSDeliveryInfoServiceLocator;
import com.percussion.delivery.service.impl.PSDeliveryInfoService;
import com.percussion.utils.tools.PSPatternMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class PSSecurityHeaderFilter implements Filter {

    private PSDeliveryInfoService psDeliveryInfoService;
    private List<PSDeliveryInfo> psDeliveryInfoServiceList;
    private String policy=null;
    static PSPatternMatcher ms_matcher = new PSPatternMatcher('?', '*', null);
    private PSSecurityUtility securityUtil = new PSSecurityUtility();


    /*
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Does nothing.

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain next) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        if(securityUtil.xFrameOptionsRequired())
        {
            if(!httpResp.containsHeader(PSSecurityUtility.HEADER_XFRAMEOPTIONS))
            {
                // add the header in the response
                httpResp.addHeader(PSSecurityUtility.HEADER_XFRAMEOPTIONS, securityUtil.getXFrameOptions());
            }
        }

        if(securityUtil.xXSSProtectionRequired())
        {
            if(!httpResp.containsHeader(PSSecurityUtility.HEADER_XSSPROTECTION))
            {
                // add the header in the response
                httpResp.addHeader(PSSecurityUtility.HEADER_XSSPROTECTION, securityUtil.getXXSSProtection());
            }
        }

        if(securityUtil.xContentTypeOptionsRequired())
        {
            if(!httpResp.containsHeader(PSSecurityUtility.HEADER_XCONTENTTYPEOPTIONS))
            {
                // add the header in the response
                httpResp.addHeader(PSSecurityUtility.HEADER_XCONTENTTYPEOPTIONS, securityUtil.getXContentTypeOptions());
            }
        }

        if(securityUtil.contentSecurityPolicyRequired())
        {
            if(!httpResp.containsHeader(PSSecurityUtility.HEADER_CONTENTSECURITY_POLICY))
            {
                // add the header in the response

                if(policy==null){
                    policy=securityUtil.getContentSecurityPolicy();
                    psDeliveryInfoService= (PSDeliveryInfoService) PSDeliveryInfoServiceLocator.getDeliveryInfoService();
                    psDeliveryInfoServiceList = psDeliveryInfoService.findAll();
                    policy= editContentSecurityPolicy(psDeliveryInfoServiceList,policy);
                }

                httpResp.addHeader(PSSecurityUtility.HEADER_CONTENTSECURITY_POLICY, policy);
            }
        }

        if(securityUtil.httpsRequired() && httpReq.isSecure())
        {
            if(securityUtil.strictTransportSecurityRequired()){

                if(!httpResp.containsHeader(PSSecurityUtility.HEADER_STRICTTRANSPORTSECURITY))
                {
                    // add the header in the response
                    httpResp.addHeader(PSSecurityUtility.HEADER_STRICTTRANSPORTSECURITY, PSSecurityUtility.HEADER_MAXAGE + "="+securityUtil.getStrictTransportSecurityMaxAge());
                }
            }
        }

        boolean statusRequest = ms_matcher.doesMatchPattern("/sessioncheck", httpReq.getServletPath());

        if(statusRequest){
            //Always turn off caching on session url
            httpResp.setHeader(PSSecurityUtility.HEADER_CACHE_CONTROL, PSSecurityUtility.CACHE_CONTROL_SESSION);
        } else {
            if(securityUtil.isCacheControlRequired())
                httpResp.setHeader(PSSecurityUtility.HEADER_CACHE_CONTROL, securityUtil.getCacheControl());
        }

        next.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {

    }

    public static String editContentSecurityPolicy( List<PSDeliveryInfo> psDeliveryInfoList,String contentSecurityString ) {


        StringBuffer serverString=new StringBuffer();

        for(PSDeliveryInfo psDeliveryInfo : psDeliveryInfoList)
        {
            serverString.append(psDeliveryInfo.getUrl()+"/");
            serverString.append(" ");
            serverString.append(psDeliveryInfo.getUrl()+"/*");
            serverString.append(" ");
        }
        if(contentSecurityString.contains("frame-src")) {

            contentSecurityString=contentSecurityString.replaceAll("frame-src", "frame-src "+" "+serverString.toString());

        }else {
            if(contentSecurityString.endsWith(";")) {
                contentSecurityString=contentSecurityString+" frame-src 'self' "+" "+serverString+";";
            }else {
                contentSecurityString=contentSecurityString+"; frame-src 'self' "+" "+serverString+";";

            }

        }

        return contentSecurityString;
    }
}