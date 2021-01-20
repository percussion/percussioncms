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

package com.percussion.delivery.integrations.ems;

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
                return new PreAuthenticatedAuthenticationToken("ANONYMOUS","N/A");
            }else{
                MemoryUser memoryUser = (MemoryUser) principal;
                List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
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
