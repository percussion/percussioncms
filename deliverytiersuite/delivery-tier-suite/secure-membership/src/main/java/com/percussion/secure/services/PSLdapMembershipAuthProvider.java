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
package com.percussion.secure.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;


import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.util.StringUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;

/**
* Works to provide authentication for Active Directory users 
* using Spring Security
* 
* @author Shweta Patel
*
*/
public class PSLdapMembershipAuthProvider extends AbstractLdapAuthenticationProvider
{
   private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

   // Error codes
   private static final int USERNAME_NOT_FOUND = 0x525;
   private static final int INVALID_PASSWORD = 0x52e;
   private static final int NOT_PERMITTED = 0x530;
   private static final int PASSWORD_EXPIRED = 0x532;
   private static final int ACCOUNT_DISABLED = 0x533;
   private static final int ACCOUNT_EXPIRED = 0x701;
   private static final int PASSWORD_NEEDS_RESET = 0x773;
   private static final int ACCOUNT_LOCKED = 0x775;

   private final String domain;
   private final String rootDn;
   private final String url;
   private boolean convertSubErrorCodesToExceptions;
   private boolean useAuthenticationRequestCredentials = true;
   private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private String userSearchFilter;
    private String groupRoleAttribute;
    private String groupSearchFilter;
    private String groupSearchBase;


   // Only used to allow tests to substitute a mock LdapContext
   ContextFactory contextFactory = new ContextFactory();


   /**
    * @param domain the domain name (may be null or empty)
    * @param url an LDAP url (or multiple URLs)
    */
   public PSLdapMembershipAuthProvider(String domain, String url) {
       Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
       this.domain = StringUtils.hasText(domain) ? domain.toLowerCase() : null;
       //this.url = StringUtils.hasText(url) ? url : null;
       this.url = url;
       rootDn = this.domain == null ? null : rootDnFromDomain(this.domain);
   }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

   @Override
   protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
       String username = auth.getName();
       String password = (String)auth.getCredentials();
       DirContextOperations ctxOps = null;

       DirContext ctx = bindAsUser(username, password);

       try {
    	   ctxOps = searchForUser(ctx, username);

       } catch (NamingException e) {
           logger.error("Failed to locate directory entry for authenticated user: " + username + "- PSLdapMembershipAuthProvider.doAuthentication", e);
           logger.error("Failed to locate directory entry for authenticated user: " + username + "- PSLdapMembershipAuthProvider.doAuthentication", badCredentials());
       } finally {
           LdapUtils.closeContext(ctx);
       }
       
       return ctxOps;
   }
   
   @Override
   protected Authentication createSuccessfulAuthentication(UsernamePasswordAuthenticationToken authentication,
           UserDetails user) {
       
       Object password = useAuthenticationRequestCredentials ? authentication.getCredentials() : user.getPassword();
       
       if(user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
           
           //int subErrorCode = parseSubErrorCode(new InsufficientAuthenticationException("User Not Authorized").getMessage());
           
           if (convertSubErrorCodesToExceptions) {
        	   logger.error("User Not Authorized - PSLdapMembershipAuthProvider.createSuccessfulAuthentication()");
               raiseExceptionForErrorCode(1328);
               //raiseExceptionForErrorCode(subErrorCode);
           }
       } 

       UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user, password,
               authoritiesMapper.mapAuthorities(user.getAuthorities()));
       result.setDetails(authentication.getDetails());

       return result;
   }
   

   /**
    * Creates the user authority list from the values of the {@code memberOf} attribute obtained from the user's
    * Active Directory entry.
    */
   @Override
   protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
       String[] groups = userData.getStringAttributes("memberOf");

       if (groups == null) {
           logger.debug("No values for 'memberOf' attribute.");

           return AuthorityUtils.NO_AUTHORITIES;
       }

       if (logger.isDebugEnabled()) {
           logger.debug("'memberOf' attribute values: " + Arrays.asList(groups));
       }

       ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(groups.length);

       for (String group : groups) {
           authorities.add(new SimpleGrantedAuthority(new DistinguishedName(group).removeLast().getValue()));
       }

       return authorities;
   }

   private DirContext bindAsUser(String username, String password) {
       // TODO. add DNS lookup based on domain
       final String bindUrl = url;
       DirContext dirContext = null;

       Hashtable<String,String> env = new Hashtable<String,String>();
       env.put(Context.SECURITY_AUTHENTICATION, "simple");
       String bindPrincipal = createBindPrincipal(username);
       env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
       env.put(Context.PROVIDER_URL, bindUrl);
       env.put(Context.SECURITY_CREDENTIALS, password);
       env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
       env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());

       try {
    	   dirContext = contextFactory.createContext(env);
       } catch (NamingException e) {
           if ((e instanceof AuthenticationException) || (e instanceof OperationNotSupportedException)) {
               handleBindException(bindPrincipal, e);
               logger.error("Bad Credentials - PSLdapMembershipAuthProvider.bindAsUser", badCredentials());
           } else {
        	   logger.error("Convert Ldap Exception - PSLdapMembershipAuthProvider.bindAsUser", LdapUtils.convertLdapException(e));
           }
       }
       
       return dirContext;
   }

   void handleBindException(String bindPrincipal, NamingException exception) {
       if (logger.isDebugEnabled()) {
           logger.debug("Authentication for " + bindPrincipal + " failed:" + exception);
       }

       int subErrorCode = parseSubErrorCode(exception.getMessage());

       if (subErrorCode > 0) {
           logger.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));

           if (convertSubErrorCodesToExceptions) {
               raiseExceptionForErrorCode(subErrorCode);
           }
       } else {
           logger.debug("Failed to locate AD-specific sub-error code in message");
       }
   }

   int parseSubErrorCode(String message) {
       Matcher m = SUB_ERROR_CODE.matcher(message);

       if (m.matches()) {
           return Integer.parseInt(m.group(1), 16);
       }

       return -1;
   }

   void raiseExceptionForErrorCode(int code) {
       switch (code) {
           case PASSWORD_EXPIRED:
               throw new CredentialsExpiredException(messages.getMessage("LdapAuthenticationProvider.credentialsExpired",
                       "User credentials have expired"));
           case ACCOUNT_DISABLED:
               throw new DisabledException(messages.getMessage("LdapAuthenticationProvider.disabled",
                       "User is disabled"));
           case ACCOUNT_EXPIRED:
               throw new AccountExpiredException(messages.getMessage("LdapAuthenticationProvider.expired",
                       "User account has expired"));
           case ACCOUNT_LOCKED:
               throw new LockedException(messages.getMessage("LdapAuthenticationProvider.locked",
                       "User account is locked"));
           case NOT_PERMITTED:
               throw new InsufficientAuthenticationException(messages.getMessage("LdapAuthenticationProvider.notPermitted",
                       "User not authorized"));
               
       }
   }

   String subCodeToLogMessage(int code) {
       switch (code) {
           case USERNAME_NOT_FOUND:
               return "User was not found in directory";
           case INVALID_PASSWORD:
               return "Supplied password was invalid";
           case NOT_PERMITTED:
               return "User not permitted to logon at this time";
           case PASSWORD_EXPIRED:
               return "Password has expired";
           case ACCOUNT_DISABLED:
               return "Account is disabled";
           case ACCOUNT_EXPIRED:
               return "Account expired";
           case PASSWORD_NEEDS_RESET:
               return "User must reset password";
           case ACCOUNT_LOCKED:
               return "Account locked";
       }

       return "Unknown (error code " + Integer.toHexString(code) +")";
   }

   private BadCredentialsException badCredentials() {
       return new BadCredentialsException(messages.getMessage(
                       "LdapAuthenticationProvider.badCredentials", "Bad credentials"));
   }

   private DirContextOperations searchForUser(DirContext ctx, String username) throws NamingException {
       SearchControls searchCtls = new SearchControls();
       searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

       String searchFilter = getUserSearchFilter();

       final String bindPrincipal = createBindPrincipal(username);

       String searchRoot = rootDn != null ? rootDn : searchRootFromPrincipal(bindPrincipal);

       return SpringSecurityLdapTemplate.searchForSingleEntryInternal(ctx, searchCtls, searchRoot, searchFilter,
               new Object[]{bindPrincipal});
   }

   private String searchRootFromPrincipal(String bindPrincipal) {
       int atChar = bindPrincipal.lastIndexOf('@');

       if (atChar < 0) {
           logger.debug("User principal '" + bindPrincipal + "' does not contain the domain, and no domain has been configured");
           logger.error("Bad Credentials - PSLdapMembershipAuthProvider.searchRootFromPrincipal", badCredentials());
       }

       return rootDnFromDomain(bindPrincipal.substring(atChar+ 1, bindPrincipal.length()));
   }

   private String rootDnFromDomain(String domain) {
       String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
       StringBuilder root = new StringBuilder();

       for (String token : tokens) {
           if (root.length() > 0) {
               root.append(',');
           }
           root.append("dc=").append(token);
       }

       return root.toString();
   }

   String createBindPrincipal(String username) {
       if (domain == null || username.toLowerCase().endsWith(domain)) {
           return username;
       }

       return username + "@" + domain;
   }

   /**
    * By default, a failed authentication (LDAP error 49) will result in a {@code BadCredentialsException}.
    * <p>
    * If this property is set to {@code true}, the exception message from a failed bind attempt will be parsed
    * for the AD-specific error code and a {@link CredentialsExpiredException}, {@link DisabledException},
    * {@link AccountExpiredException} or {@link LockedException} will be thrown for the corresponding codes. All
    * other codes will result in the default {@code BadCredentialsException}.
    *
    * @param convertSubErrorCodesToExceptions {@code true} to raise an exception based on the AD error code.
    */
   public void setConvertSubErrorCodesToExceptions(boolean convertSubErrorCodesToExceptions) {
       this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
   }

   static class ContextFactory {
       DirContext createContext(Hashtable<?,?> env) throws NamingException {
           return new InitialLdapContext(env, null);
       }
   }
}
