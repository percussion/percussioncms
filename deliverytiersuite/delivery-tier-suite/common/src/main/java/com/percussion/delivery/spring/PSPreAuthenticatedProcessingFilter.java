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

package com.percussion.delivery.spring;

import org.apache.catalina.realm.GenericPrincipal;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
            GenericPrincipal principal = (GenericPrincipal)request.getUserPrincipal();
            if(principal == null ) {
                return new PreAuthenticatedAuthenticationToken("ANONYMOUS","N/A");
            }else{
                List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                String[] roles = principal.getRoles();
                for (String role: roles){
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
                String password = principal.getPassword();
                if(password == null)
                    password = "NO_PASSWORD";
                return new PreAuthenticatedAuthenticationToken(principal.getName(),password,grantedAuthorities);
            }
        }
    }

    }
