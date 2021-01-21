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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;

import com.percussion.utils.io.PathUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;


import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.util.PSProperties;

public class PSSecurityUtility {
    
    
    public static final String REQUIRE_HTTPS="requireHTTPS";
    public static final String REQUIRE_HTTPS_DEFAULT = "false";
    public static final String REQUIRE_CONTENT_SECURITY_POLICY="requireContentSecurityPolicy";
    public static final String REQUIRE_CONTENT_SECURITY_POLICY_DEFAULT = "false";
    public static final String CONTENT_SECURITY_POLICY = "contentSecurityPolicy";
    public static final String CONTENT_SECURITY_POLICY_DEFAULT="default-src 'self' *.percussion.com *.percussion.marketing *.percussion.services 'unsafe-inline' 'unsafe-eval'; script-src 'self' *.siteimprove.net  'unsafe-inline' 'unsafe-eval'";
    public static final String REQUIRE_XFRAME_OPTIONS = "requireXFrameOptions";
    public static final String REQUIRE_XFRAME_OPTIONS_DEFAULT = "true";
    public static final String XFRAME_OPTIONS="xFrameOptions";
    public static final String XFRAME_OPTIONS_DEFAULT = "SAMEORIGIN";
    public static final String REQUIRE_XSS_PROTECTION = "requireXXSSProtection";
    public static final String REQUIRE_XSS_PROTECTION_DEFAULT = "true";
    public static final String XSS_PROTECTION = "xXSSProtection";
    public static final String XSS_PROTECTION_DEFAULT = "1; mode=block";
    public static final String REQUIRE_XCONTENTTYPE_OPTIONS = "requireXContentTypeOptions";
    public static final String REQUIRE_XCONTENTTYPE_OPTIONS_DEFAULT = "true";
    public static final String XCONTENTTYPE_OPTIONS = "xContentTypeOptions";
    public static final String XCONTENTTYPE_OPTIONS_DEFAULT = "nosniff";
    public static final String REQUIRE_STRICT_TRANSPORT_SECURITY = "requireStrictTransportSecurity";
    public static final String REQUIRE_STRICT_TRANSPORT_SECURITY_DEFAULT = "false";
    public static final String STRICT_TRANSPORT_SECURITY_MAXAGE = "stricttransportsecuritymaxage";
    public static final String STRICT_TRANSPORT_SECURITY_MAXAGE_DEFAULT = "300";
    public static final String REQUIRE_CACHE_CONTROL = "requireCacheControl";
    public static final String REQUIRE_CACHE_CONTROL_DEFAULT = "true";
    public static final String CACHE_CONTROL = "cacheControl";
    public static final String CACHE_CONTROL_DEFAULT = "no-cache='Set-Cookie, Set-Cookie2',must-revalidate";
    
    
    public static final String HEADER_XFRAMEOPTIONS = "X-Frame-Options";
    public static final String HEADER_XSSPROTECTION = "X-XSS-Protection";
    public static final String HEADER_XCONTENTTYPEOPTIONS = "X-Content-Type-Options";
    public static final String HEADER_CONTENTSECURITY_POLICY = "Content-Security-Policy";
    public static final String HEADER_STRICTTRANSPORTSECURITY = "Strict-Transport-Security";
    public static final String HEADER_MAXAGE = "max-age";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String CACHE_CONTROL_SESSION = "no-cache,must-revalidate";
    
    
    private Boolean isHTTPSRequired = null;
    private Boolean isStrictTransportSecurityRequired = null;
    private Boolean isContentSecurityPolicyRequired   = null;;
    private Boolean isxFrameOptionsRequired = null;
    private Boolean isXXSSProtectionRequired = null;
    private Boolean isXContentTypeOptionsRequired = null;
    private Boolean cacheControlRequired = null; 
    private String contentSecurityPolicy = null; 
    private Integer strictTransportSecurityMaxAge = null; 
    private String xFrameOptions = null; 
    private String xXSSProtection= null; 
    private String xContentTypeOptions = null; 
    private String cacheControl = null; 
    
    private static final Log log = LogFactory.getLog(PSSecurityUtility.class);
    
    public PSSecurityUtility(){}
    
    /**
    * If requireHTTPS server property exists and its value is set to true or yes. The value is locally stored in a variable,
    * instead of reading from a property each time.
    * @return <code>true</code> if it is a set to true.
    */
        public boolean httpsRequired()
       {
          if(isHTTPSRequired != null){
             return isHTTPSRequired.booleanValue();
          }
          boolean result = false;
          Properties serverProps = getServerProperties();
          if(serverProps != null){
             String httpsProperty = serverProps.getProperty(REQUIRE_HTTPS, "false");
             if(StringUtils.isNotBlank(httpsProperty) && (httpsProperty.equalsIgnoreCase("true") || httpsProperty.equalsIgnoreCase("yes"))){
                isHTTPSRequired = Boolean.TRUE;
                result = true;
             }
             else{
                isHTTPSRequired = Boolean.FALSE;
             }
          }
          return result;
       }
       
         public boolean isCacheControlRequired()
           {
               if(cacheControlRequired != null) {
                   return cacheControlRequired.booleanValue();
               }

               Properties serverProps = getServerProperties();
              if(serverProps != null){
                 String value = serverProps.getProperty(REQUIRE_CACHE_CONTROL, REQUIRE_CACHE_CONTROL_DEFAULT);
                 if(StringUtils.isNotBlank(value) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"))){
                     cacheControlRequired = Boolean.TRUE;

                  }
                  else{
                      cacheControlRequired = Boolean.FALSE;
                  }
              }
              return cacheControlRequired;
           }
        
    public String getCacheControl(){
         if(cacheControl != null) {
               return cacheControl;
           }
    
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String value = serverProps.getProperty(CACHE_CONTROL, CACHE_CONTROL_DEFAULT);
             cacheControl = value;
          }
          return cacheControl;
    }
       public boolean contentSecurityPolicyRequired()
       {
           if(isContentSecurityPolicyRequired != null) {
               return isContentSecurityPolicyRequired.booleanValue();
           }
           boolean result = false;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String cspProperty = serverProps.getProperty(REQUIRE_CONTENT_SECURITY_POLICY, REQUIRE_CONTENT_SECURITY_POLICY_DEFAULT);
             if(StringUtils.isNotBlank(cspProperty) && (cspProperty.equalsIgnoreCase("true") || cspProperty.equalsIgnoreCase("yes"))){
                 isContentSecurityPolicyRequired = Boolean.TRUE;
                 result = true;
              }
              else{
                  isContentSecurityPolicyRequired = Boolean.FALSE;
              }
          }
          return result;
       }
       
        
       public String getContentSecurityPolicy()
       {
           if(contentSecurityPolicy != null) {
               return contentSecurityPolicy;
           }
    
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String csp = serverProps.getProperty(CONTENT_SECURITY_POLICY, CONTENT_SECURITY_POLICY_DEFAULT);
             contentSecurityPolicy = csp;
          }
          return contentSecurityPolicy;
       }
       
       public boolean xFrameOptionsRequired()
       {
           if(isxFrameOptionsRequired != null) {
               return isxFrameOptionsRequired.booleanValue();
           }
           boolean result = false;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String frameProperty = serverProps.getProperty(REQUIRE_XFRAME_OPTIONS, REQUIRE_XFRAME_OPTIONS_DEFAULT);
             if(StringUtils.isNotBlank(frameProperty) && (frameProperty.equalsIgnoreCase("true") || frameProperty.equalsIgnoreCase("yes"))){
                 isxFrameOptionsRequired = Boolean.TRUE;
                 result = true;
              }
              else{
                  isxFrameOptionsRequired = Boolean.FALSE;
              }
          }
          return result;
       }
       public String getXFrameOptions()
       {
           if(xFrameOptions != null) {
               return xFrameOptions;
           }
    
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String frameOptions = serverProps.getProperty(XFRAME_OPTIONS, XFRAME_OPTIONS_DEFAULT);
             xFrameOptions = frameOptions;
          }
          return xFrameOptions;
       }
       
       public boolean xXSSProtectionRequired()
       {
           if(isXXSSProtectionRequired != null) {
               return isXXSSProtectionRequired.booleanValue();
           }
           boolean result = false;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String xssProperty = serverProps.getProperty(REQUIRE_XSS_PROTECTION, REQUIRE_XSS_PROTECTION_DEFAULT);
             if(StringUtils.isNotBlank(xssProperty) && (xssProperty.equalsIgnoreCase("true") || xssProperty.equalsIgnoreCase("yes"))){
                 isXXSSProtectionRequired = Boolean.TRUE;
                 result = true;
              }
              else{
                  isXXSSProtectionRequired = Boolean.FALSE;
              }
          }
          return result;
       }
       public String getXXSSProtection()
       {
           if(xXSSProtection != null) {
               return xXSSProtection;
           }
    
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String xssProtection = serverProps.getProperty(XSS_PROTECTION, XSS_PROTECTION_DEFAULT);
             xXSSProtection = xssProtection;
          }
          return xXSSProtection;
       }
       
       public boolean xContentTypeOptionsRequired()
       {
           if(isXContentTypeOptionsRequired != null) {
               return isXContentTypeOptionsRequired.booleanValue();
           }
           boolean result = false;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String ctProperty = serverProps.getProperty(REQUIRE_XCONTENTTYPE_OPTIONS, REQUIRE_XCONTENTTYPE_OPTIONS_DEFAULT);
             if(StringUtils.isNotBlank(ctProperty) && (ctProperty.equalsIgnoreCase("true") || ctProperty.equalsIgnoreCase("yes"))){
                 isXContentTypeOptionsRequired = Boolean.TRUE;
                 result = true;
              }
              else{
                  isXContentTypeOptionsRequired = Boolean.FALSE;
              }
          }
          return result;
       }
       
       public String getXContentTypeOptions()
       {
           if(xContentTypeOptions != null) {
               return xContentTypeOptions;
           }
    
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String ctOptions = serverProps.getProperty(XCONTENTTYPE_OPTIONS, XCONTENTTYPE_OPTIONS_DEFAULT);
             xContentTypeOptions = ctOptions;
          }
          return xContentTypeOptions;
       }
       
       public boolean strictTransportSecurityRequired()
       {
           if(isStrictTransportSecurityRequired != null) {
               return isStrictTransportSecurityRequired.booleanValue();
           }
           boolean result = false;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String stsProperty = serverProps.getProperty(REQUIRE_STRICT_TRANSPORT_SECURITY,REQUIRE_STRICT_TRANSPORT_SECURITY_DEFAULT);
             if(StringUtils.isNotBlank(stsProperty) && (stsProperty.equalsIgnoreCase("true") || stsProperty.equalsIgnoreCase("yes"))){
                 isStrictTransportSecurityRequired = Boolean.TRUE;
                 result = true;
              }
              else{
                  isStrictTransportSecurityRequired = Boolean.FALSE;
              }
          }
          return result;
       }
       
       public int getStrictTransportSecurityMaxAge()
       {
           if(strictTransportSecurityMaxAge != null) {
               return strictTransportSecurityMaxAge.intValue();
           }
           int result=0;
           Properties serverProps = getServerProperties();
          if(serverProps != null){
             String stsMaxAge = serverProps.getProperty(STRICT_TRANSPORT_SECURITY_MAXAGE, STRICT_TRANSPORT_SECURITY_MAXAGE);
             if(NumberUtils.isNumber(stsMaxAge)){
                 result = NumberUtils.toInt(stsMaxAge);
                 if(result > 0){
                     strictTransportSecurityMaxAge = result;
                 } else {
                     result = 0;
                 }
             }
          }
          return result;
       }

    /**
     * Will return an instance of secure random.  Will attempt to return a StrongSecureRandom first
     * but will return a standard SecureRandom if Strong is unavailable.  May return null if
     * secure random cannot be initialized.
     *
     * @return
     */
    public static SecureRandom getSecureRandom(){
        SecureRandom ret = new SecureRandom();
        return ret;
    }
       
       private PSProperties getServerProperties() {
           File propFile = PSProperties.getConfig(ENTRY_NAME, PROPS_SERVER,
                   getRxConfigDir(SERVER_DIR));

             try {
                ms_serverProps = new PSProperties(propFile.getPath());
            } catch (FileNotFoundException e) {
                log.error("File not found in PSProperties", e);
            } catch (IOException e) {
                log.error("IO Exception in PSProperties", e);
            }
             
             return ms_serverProps;
       }
       
       public static String getRxConfigDir(String path) {
       
           File item = new File(PathUtils.getRxDir(), path);
              if (item.exists() == false)
              {
                 throw new IllegalArgumentException("file does not exist: " + item.getAbsolutePath());
              }
              return item.getAbsolutePath();
       }
       
       public static final String PROPS_SERVER = "server.properties";
       
       /**
        * Constant for the name of the entry that reperesents workflow's name/value
        * pair.
        */
       public static final String ENTRY_NAME = "server_config_base_dir";
       
       
       /**
        * Constant for the directory containing all other configuration directories.
        */
       public static final String BASE_CONFIG_DIR = "rxconfig";

       /**
        * Constant for the directory containing server configs.
        * Assumed to be relative to the Rx directory. No trailing slash.
        */
       public static final String SERVER_DIR = BASE_CONFIG_DIR + "/Server";
       
       private static PSProperties ms_serverProps = new PSProperties();

    /**
     * Utility to remove parameters from header.
     * @param str
     * @return
     */
    public static String removeSpecialCharactersFromHeader(String str) {
           return str.replaceAll("[^a-zA-Z ]", "");
    }

    /**
     * Utility to sanitize a string for use in a file system path under a specified path.
     *
     * @param str
     * @return The sanitized string
     */
    public static String sanitizeStringForFileUnderPath(String containingPath, String str){
        return str;
    }

    /**
     * Utility to sanitize a string for use in a file system path
     *
     * @param str
     * @return The sanitized string
     */
    public static String sanitizeStringForFileSystem(String str){

        return str;
    }

    /**
     * Utility to sanitize a string for use in a SQL statement
     * @param str User provided string
     * @return The sanitized string
     */
    public static String sanitizeStringForSQLStatement(String str){
        return str;
    }

    /**
     * Sanitizes a user provided string for use in HTML
     * @param str a user provided string
     * @return The sanitized string
     */
    public static String sanitizeStringForHTML(String str){
        return str;
    }

}
