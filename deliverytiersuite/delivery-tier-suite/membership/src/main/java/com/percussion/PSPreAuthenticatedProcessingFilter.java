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

package com.percussion;

import org.apache.catalina.users.MemoryRole;
import org.apache.catalina.users.MemoryUser;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PSPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter  {


    public PSPreAuthenticatedProcessingFilter() {
        setAuthenticationDetailsSource(new PSAuthenticationDetailsSource());
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return "ANONYMOUS";
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    public static class PSAuthenticationDetailsSource implements
            AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedAuthenticationToken> {
        @Override
        public PreAuthenticatedAuthenticationToken buildDetails(HttpServletRequest request) {
            // create container for pre-auth data
            Principal principal = request.getUserPrincipal();
            if(principal == null || !principal.getClass().isAssignableFrom( MemoryUser.class)) {
                String userName = request.getHeader("tomcat-user");
                String password = request.getHeader("tomcat-password");
                if(userName != null && userName.equalsIgnoreCase("ps_manager")){
                    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_deliverymanager"));
                    return new PreAuthenticatedAuthenticationToken(userName,password,grantedAuthorities);
                }else{
                    return new PreAuthenticatedAuthenticationToken("ANONYMOUS","N/A");
                }
            }else{
                MemoryUser memoryUser = (MemoryUser) principal;
                List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                Iterator roles = memoryUser.getRoles();
                while (roles.hasNext()){
                    MemoryRole role = (MemoryRole) roles.next();
                    String roleName = "ROLE_" + role.getName();
                    grantedAuthorities.add(new SimpleGrantedAuthority(roleName));
                }
                return new PreAuthenticatedAuthenticationToken(memoryUser.getName(),memoryUser.getPassword(),grantedAuthorities);
            }
        }
    }

}
