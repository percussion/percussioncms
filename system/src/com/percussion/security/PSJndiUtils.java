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
package com.percussion.security;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSErrorManager;
import com.percussion.error.PSRuntimeException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;

/**
 * This class provides utility functions used for JNDI operations.
 */
@SuppressWarnings("unchecked")
public class PSJndiUtils
{
   /**
    * Do not instantiate this class, all functionality exposed is static.
    */
   private PSJndiUtils()
   {
   }
   

   /**
    * Get the requested property and remove it from the supplied properties
    * parameter.
    *
    * @param properties the properties from where to get and remove the
    *    requested property, not <code>null</code>, may be empty.
    * @param name the property name, not <code>null</code> or empty.
    * @param required <code>true</code> ir the property is required,
    *    <code>false</code> otherwise.
    * @return the requested property if found, <code>null</code> otherwise.
    * @throws IllegalArgumentException if the property is required but not
    *    found or empty.
    */
   public static String getProperty(Properties properties, String name,
      boolean required)
   {
      if (properties == null)
         throw new IllegalArgumentException("properties cannot be null");

      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      String property = (String) properties.remove(name);
      if (property == null && !required)
         return property;

      String errorMsg = "property '" + name + "' ";
      if ((property == null) && required)
         throw new IllegalArgumentException(errorMsg + "cannot be null");

      property = property.trim();
      if ((property.length() == 0) && required)
         throw new IllegalArgumentException(errorMsg + "cannot be empty");

      return property;
   }

   /**
    * Initializes the password filter instance, given the class name.
    *
    * @param extensionName the password filter extension name. Must
    *    not be <code>null</code> or empty.
    * @return the newly constructed password filter, never <code>null<code>.
    * @throws PSRuntimeException if instance of filter class cannot be found
    *    or constructed.
    */
   public static IPSPasswordFilter initPasswordFilter(String extensionName)
   {
      if ((extensionName == null))
         throw new IllegalArgumentException("extensionName cannot be null");
         
      extensionName = extensionName.trim();
      if (extensionName.length() == 0)
         throw new IllegalArgumentException("extensionName cannot be null");

      try 
      {
         PSExtensionRef ref = new PSExtensionRef(extensionName);
         IPSExtension ext =
               PSServer.getExtensionManager(null).prepareExtension(ref, null);
         if (!(ext instanceof IPSPasswordFilter))
            throw new IllegalArgumentException(
                  "Supplied extension is not a password filter.");
         
         return (IPSPasswordFilter) ext;
      }
      catch (PSNotFoundException e)
      {
         throw new PSRuntimeException(
            IPSSecurityErrors.DIR_PASSWORD_FILTER_INIT_ERROR,
               new Object[] { extensionName, e.toString() });
      }
      catch (PSExtensionException e)
      {
         throw new PSRuntimeException(
            IPSSecurityErrors.DIR_PASSWORD_FILTER_INIT_ERROR,
               new Object[] { extensionName, e.toString() });
      }
   }

   /**
    * Builds an AND filter for all entries in the supplied map.
    * 
    * @param values the filter values, where the key is the attribute name 
    *    as <code>String</code> and the value is the attribute value as 
    *    <code>String</code>, or a list of <code>String</code>snot 
    *    <code>null<code> or empty. <code>null<code> or empty attribute values 
    *    will be replace with a wildcard value '*'. <code>null</code> or 
    *    empty attribute keys are not allowed.
    * @return the filter string in the form of (&(key1=value1)...(keyN=valueN))
    *    for all map entries, never <code>null<code>.
    */
   public static String buildFilter(Map values)
   {
      if (values == null)
         throw new IllegalArgumentException("values cannot be null");
         
      if (values.isEmpty())
         throw new IllegalArgumentException("values cannot be empty");
         
      StringBuffer filter = new StringBuffer("(&");
      
      Iterator keys = values.keySet().iterator();
      while (keys.hasNext())
      {
         String key = (String) keys.next();
         if (key == null)
            throw new IllegalArgumentException("key cannot be null");
            
         key = key.trim();
         if (key.length() == 0)
            throw new IllegalArgumentException("key cannot be empty");
            
         Object obj = values.get(key);
         if (obj == null || obj instanceof String)
         {
            String value = (String) values.get(key);
            appendFilterCond(filter, key, value);
         }
         else
         {
            List valList = (List) obj;
            if (valList.isEmpty())
               appendFilterCond(filter, key, null);
            else
            {
               filter.append("(|");
               Iterator vals = valList.iterator();
               while (vals.hasNext())
               {
                  String value = (String) vals.next();
                  appendFilterCond(filter, key, value);
               }
               filter.append(")");            
            }
         }
      }
      filter.append(")");

      return filter.toString();
   }


   /**
    * Appends a condition to the supplied filter buffer for the specified
    * key and value.
    * @param filter The buffer, assumed not <code>null</code>.
    * @param attrName The name of the condition's attribute, assumed not 
    * <code>null</code> or empty.
    * @param value The condition value, may be <code>null</code> or empty.
    */
   private static void appendFilterCond(StringBuffer filter, String attrName, 
      String value)
   {
      if (value == null || value.trim().length() == 0)
         value = WILDCARD_SEARCH;
      
      filter.append("(");
      filter.append(attrName);
      filter.append("=");
      filter.append(escapeDnComponent(value));
      filter.append(")");
   }

   /**
    * Convenience method that calls 
    * {@link #createContext(PSDirectoryDefinition, String) 
    * createContext(directoryDef, null)}
    */
   public static DirContext createContext(PSDirectoryDefinition directoryDef)
      throws NamingException
   {
      return createContext(directoryDef, null);
   }
   
   /**
    * Creates a new context for the supplied directory using the specified url.
    * 
    * @param directoryDef the directory definition for which to create the 
    *    context, not <code>null<code>.
    * @param url The url to use, may be <code>null</code> to use the one defined
    *    in the supplied directory def. 
    * @return a new directory context, never <code>null<code>. The caller is
    *    responsible to close it.
    * @throws NamingException if anything goes wrong creating the new context.
    */
   public static DirContext createContext(PSDirectoryDefinition directoryDef, 
      String url) throws NamingException
   {
      if (directoryDef == null)
         throw new IllegalArgumentException("directoryDef cannot be null");
      
      PSAuthentication auth = directoryDef.getAuthentication();
      PSDirectory dir = directoryDef.getDirectory();
      
      if (StringUtils.isBlank(url))
         url = dir.getProviderUrl();
      
      Hashtable env = new Hashtable();


      // need to set the context factory (provided by user)
      env.put(Context.INITIAL_CONTEXT_FACTORY, dir.getFactory());
      
      String encurl = PSJndiUtils.getEncodedProviderUrl(url);
      env.put(Context.PROVIDER_URL, encurl);
      // Add connectionPool needs to go after setup of security_protocol
      // to set correct socket factory
      addConnectionPooling(env);



      // authenticate with the specified uid/pw
      env.put(Context.SECURITY_AUTHENTICATION, auth.getScheme());
      env.put(Context.SECURITY_PRINCIPAL, auth.getPrincipal(
         dir.getProviderUrl()));
      env.put(Context.SECURITY_CREDENTIALS, auth.getCredentials());

      // Provide debug output
      if (dir.isDebug())
         env.put("com.sun.jndi.ldap.trace.ber", System.out);

      return new InitialDirContext(env);
   }   
   
   /**
    * Add the connection pooling configuration if its enabled.
    * 
    * @param env the environment to which to add the connection pooling 
    *    configuration properties, not <code>null</code>, may be empty.
    */
   public static void addConnectionPooling(Hashtable env)
   {
      if (env == null)
         throw new IllegalArgumentException("env cannot be null");
      
      PSServerConfiguration config = PSServer.getServerConfiguration();
     
   
         Properties jndiConnectionPooling = 
            config.getJndiConnectionPoolingConfig();
         
         Enumeration keys = jndiConnectionPooling.keys();
         while (keys.hasMoreElements())
         {
            String key = (String) keys.nextElement();
            String value = jndiConnectionPooling.getProperty(key);
            env.put(key, value);
         }

         String url = StringUtils.defaultString((String)env.get(Context.PROVIDER_URL));

         if (url.startsWith("ldaps:"))
         {
             env.put(Context.SECURITY_PROTOCOL, "ssl");
             env.put(Context.PROVIDER_URL,url.replace("ldaps:", "ldap:"));
         } else if (url.isEmpty())
         {
            throw new IllegalArgumentException("PROVIDER_URL not set in ldap environment");
         }

         String securityProtocol = StringUtils.defaultString((String)env.get(Context.SECURITY_PROTOCOL));
         if (securityProtocol!=null && securityProtocol.equals("ssl"))
         {
             String sslFactory =  StringUtils.defaultString((String)env.get(PSServerConfiguration.SECURE_SOCKET_FACTORY));
             if (!sslFactory.isEmpty())
             {
                 // Set ssl socket factory
                 env.put("java.naming.ldap.factory.socket",sslFactory);
             }
         }

   }

   /**
    * Create new search controls for the supplied parameters.
    * 
    * @param directory the directory for which to create the search controls
    *    for, not <code>null<code>.
    * @param returnAttributes an array with all attribute names that must be 
    *    returned, <code>null<code> to return all attributes, empty to return
    *    no attributes.
    * @return a search control object, never <code>null<code>.
    */
   public static SearchControls createSearchControls(PSDirectory directory,
      String[] returnAttributes)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");
      
      SearchControls searchControls = new SearchControls();

      int scope = directory.isShallowCatalogOption() ? 
         SearchControls.ONELEVEL_SCOPE : SearchControls.SUBTREE_SCOPE;
      searchControls.setSearchScope(scope);
      searchControls.setReturningAttributes(returnAttributes);

      return searchControls;
   }

   /**
    * Attempts to determine the base context from the provided URL.
    * 
    * @param providerUrl the url to the directory provider, may be
    *           <code>null<code> or empty.
    * @return the base context if there is one specified by the provided url,
    *    or an empty string if there is not one specified, or <code>null</code>
    *    if the provided url is <code>null</code> or malformed.
    */
   public static String getBaseContext(String providerUrl)
   {
      String baseCtx = null;

      if (providerUrl != null)
      {
         // we'll only do this if we can parse the url properly
         try
         {
            URL url = getURL(providerUrl);
            baseCtx = url.getFile();
            if (baseCtx.startsWith("/"))
            {
               if (baseCtx.length() > 1)
                  baseCtx = baseCtx.substring(1);
               else
                  baseCtx = "";
            }
         }
         catch (MalformedURLException e)
         {
            // we return null to indicate it's malformed
         }
      }

      return baseCtx;
   }

   /**
    * Builds a fully qualified username using the principle attribute and the
    * base context. Thus if the uid provided is "JoeBloe", the principle
    * attribute is "cn", and the base context is
    * "ou=People,o=Percussion,c=us", then the result will be:
    * "cn=JoeBloe,ou=People,o=Percussion,c=us".
    *
    * @param uid the value of the userid attribute in the directory we are
    *    building a username for, my not be <code>null</code> or empty.
    * @param principleAttribute the priciple attribute name, may be 
    *    <code>null<code> but not empty.
    * @param baseContext the base context to build the full name for, may be
    *    <code>null<code> but not empty.
    * @return the fully qualified username, never <code>null</code> or empty.
    *    The returned name is escaped.
    */
   public static String getFullUserName(String uid, String principleAttribute,
      String baseContext)
   {
      if (uid == null)
         throw new IllegalArgumentException("uid cannot be null");
         
      uid = uid.trim();
      if (uid.length() == 0)
         throw new IllegalArgumentException("uid cannot be empty");
         
      if (principleAttribute != null)
      {
         principleAttribute = principleAttribute.trim();
         if (principleAttribute.length() == 0)
            throw new IllegalArgumentException(
               "principleAttribute cannot be empty");
      }
      
      if (baseContext != null)
      {
         baseContext = baseContext.trim();
         if (baseContext.length() == 0)
            throw new IllegalArgumentException("baseContext cannot be empty");
      }

      String prefix = principleAttribute;
      if (prefix != null)
         prefix += "=";
      else
         prefix = "";

      String suffix = baseContext;
      if (suffix != null)
         suffix = "," + suffix;
      else
         suffix = "";

      return prefix + escapeDnComponent(uid) + suffix;
   }

   /**
    * Convenience version of {@link #getCompoundName(String, String)} that calls
    * <code>getCompoundName(dn, null)</code>.
    */
   public static CompoundName getCompoundName(String dn)
      throws InvalidNameException
   {
      return getCompoundName(dn, null);
   }

   /**
    * Creates a compound name from the supplied distinguished name and optional
    * context.
    *
    * @param dn the distinguished name to create the compound name from, not
    *    <code>null<code> or empty.
    * @param context if dn is relative to a search context, the distinguished
    *    name of the entry the dn is relative to. This value is appended onto 
    *    the dn. May be <code>null</code> if dn is not a relative name.
    * @return the compound name, never <code>null</code>.
    * @throws InvalidNameException if dn is malformed.
    */
   public static CompoundName getCompoundName(String dn, String context)
      throws InvalidNameException
   {
      CompoundName cn = new CompoundName(dn, ms_namingProps);
      if (context != null)
      {
         CompoundName ctxName = new CompoundName(context, ms_namingProps);
         cn.addAll(ctxName);
      }

      return cn;
   }
   
   /**
    * Get the full password for the supplied parameters.
    * 
    * @param pw the password, not <code>null<code>, may be empty.
    * @param credentialAttribute the credential attribute name, may be
    *    <code>null<code> but not empty.
    * @return the full password which is <code>credentialAttribute=pw</code> if
    *    a valid credential attribute was supplied, <code>pw</code> otherwise.
    */
   public static String getFullPassword(String pw, String credentialAttribute)
   {
      if (pw == null)
         throw new IllegalArgumentException("pw cannot be null");
         
      if (credentialAttribute != null)
      {
         credentialAttribute = credentialAttribute.trim();
         if (credentialAttribute.length() == 0)
            throw new IllegalArgumentException(
               "credentialAttribute cannot be empty");
      }

      String prefix = credentialAttribute;
      if (prefix != null)
         prefix += "=";
      else
         prefix = "";
         
      return prefix + pw.trim();
   }
   
   /**
    * Escapes reserved characters in the provided name so that it is a valid dn
    * component.  Escapes the list of chars in {@link #DN_ESCAPE_CHARS} by
    * preceding them with a backslash.  Will skip escaping them if they are
    * already escaped.
    *
    * @param name The dn component that must be escaped, never <code>null</code>
    * or empty.
    *
    * @return The escaped string, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public static String escapeDnComponent(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");


      StringBuffer newBuf = new StringBuffer(name.length());
      for (int i=0; i < name.length(); i++)
      {
         char test = name.charAt(i);
         if (isDnEscapeChar(test))
         {
            // see if a backslash and next char is escaped
            if (test == DN_ESCAPE_CHAR && i < name.length()-1 &&
               isDnEscapeChar(name.charAt(i + 1)))
            {
               // add both and jump ahead
               newBuf.append(test);
               newBuf.append(name.charAt(++i));
            }
            else
            {
               // escape current char
               newBuf.append(DN_ESCAPE_CHAR);
               newBuf.append(test);
            }
         }
         else
            newBuf.append(test);
      }

      return newBuf.toString();
   }

   /**
    * Unescapes reserved characters in the provided name.  Reverses the process
    * done by {@link #escapeDnComponent(String)}.
    *
    * @param name The dn component to unescape, never <code>null</code>
    * or empty.
    *
    * @return The unescaped string, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public static String unEscapeDnComponent(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      StringBuffer newBuf = new StringBuffer(name.length());
      for (int i=0; i < name.length(); i++)
      {
         // see if this is a backslash escaping the next char
         if (name.charAt(i) == DN_ESCAPE_CHAR && (i < name.length()-1 &&
            isDnEscapeChar(name.charAt(i + 1))))
         {
            // skip ahead to the next char
            i++;
         }
         newBuf.append(name.charAt(i));
      }

      return newBuf.toString();
   }
   
   /**
    * Translates a SQL like filter string to a directory service like filter
    * string. SQL '%' filters and '_' are replaced with the directory service
    * '*' filter.
    * 
    * @param filter the SQL filter to be translated, may be <code>null</code>
    *    or empty.
    * @return the translated filter of <code>null</code> if <code>null</code>
    *    was supplied.
    */
   public static String translateSQLFilter(String filter)
   {
      if (filter == null)
         return filter;
         
      filter = filter.replace('%', '*');
      
      return filter.replace('_', '*');
   }
   
   /**
    * Encodes any spaces and other special chars according to RFC2255, may not
    * be <code>null</code> or empty.
    * 
    * @param providerURL The url to encode, may not be <code>null</code> or 
    * empty.
    * 
    * @return The escaped url, never <code>null</code> or empty.
    * 
    * @throws NamingException if the url cannot be parsed or encoded.
    */
   public static String getEncodedProviderUrl(String providerURL) 
      throws NamingException
   {
      if (providerURL == null || providerURL.trim().length() == 0)
         throw new IllegalArgumentException(
            "providerURL may not be null or empty");
      
      try
      {
         // attempt to encode any special chars in the url
         URL url = getURL(providerURL);
         URI uri = new URI(getProtocol(providerURL), null, url.getHost(), 
            url.getPort(), url.getFile(), null, null);
         return uri.toASCIIString();
      }
      catch (Exception e)
      {
         throw new NamingException(PSErrorManager.createMessage(
            IPSSecurityErrors.PARSE_JNDI_PROVIDER_URL_ERROR, 
            e.getLocalizedMessage()));
      }
   }
   
   /**
    * Get the protocol from the supplied url string.
    * 
    * @param url the url string from which we want the protocol, not
    *    <code>null</code> or empty.
    * @return the protocol of the supplied url string, never <code>null</code>
    *    may be empty.
    */
   public static String getProtocol(String url)
   {
      if (url == null)
         throw new IllegalArgumentException("url cannot be null");
      
      url = url.trim();
      if (url.length() == 0)
         throw new IllegalArgumentException("url cannot be empty");
      
      String protocol = "";
      int pos = url.indexOf("://");
      if (pos != -1)
         protocol = url.substring(0, pos);
      
      return protocol;
   }
   
   /**
    * Get the url for the supplied url string. The protocol will be converted to 
    * use the <code>http</code> 
    * protocol to avoid using protocols not supported by the <code>URL</code> 
    * class.
    * 
    * @param url the url string for which to create an <code>URL</code> object,
    *    not <code>null</code> or empty.
    * @return the <code>URL</code> object created for the supplied url string,
    *    never <code>null</code>.
    * @throws MalformedURLException for any malformed url string provided.
    */
   public static URL getURL(String url) throws MalformedURLException
   {
      if (url == null)
         throw new IllegalArgumentException("url cannot be null");
      
      url = url.trim();
      if (url.length() == 0)
         throw new IllegalArgumentException("url cannot be empty");
      
      String protocol = getProtocol(url);
      if (!StringUtils.isBlank(protocol))
         url = "http" + url.substring(protocol.length());
      
      return new URL(url); 
   }

   /**
    * Determines if the specified char is one of the chars in
    * {@link #DN_ESCAPE_CHARS}.
    *
    * @param test The char to test.
    *
    * @return <code>true</code> if it is in the list, <code>false</code>
    * otherwise.
    */
   private static boolean isDnEscapeChar(char test)
   {
      boolean isEscChar = false;

      for (int i=0; i < DN_ESCAPE_CHARS.length && !isEscChar; i++)
      {
         if (test == DN_ESCAPE_CHARS[i])
            isEscChar = true;
      }

      return isEscChar;
   }

   /**
    * Converts the array of String into a filter, applying the principle
    * attribute and converting wildcards.
    *
    * @param filterPattern An array of patterns, not <code>null</code>.
    *    If any pattern is <code>null</code> or empty, it is ignored.
    *
    * @param attr The principle attribute to use when constructing the filter.
    *    For example, if "cn" is supplied, each filter is used to constuct
    *    "cn=<filter>".  All patterns are combined using the <code>OR</code>
    *    condtional.
    *
    * @param baseFilter A base filter to append onto. The filter resulting from
    *    the supplied <code>filterPattern</code> is appended onto this using the
    *    <code>AND</code> conditional.  If <code>null</code> or empty, it is
    *    ignored.
    *
    * @return The filter, never <code>null</code>, may be empty.
    *
    * @throws PSSecurityException if the filter contains invalid wildcards.
    */
   public static String getFilterString(String[] filterPattern, String attr,
      String baseFilter) throws PSSecurityException
   {
      if (filterPattern == null)
         throw new IllegalArgumentException("filterPattern cannot be null");
         
      StringBuffer buf = new StringBuffer();
      if (filterPattern != null)
      {
         buf.append("(|");
         for (int i=0; i < filterPattern.length; i++)
         {
            String filter = filterPattern[i];
   
            // only support '%', convert to '*'
            filter = processFilter(filter);
            buf.append(" (");
            buf.append(attr);
            buf.append("=");
            buf.append(filter);
            buf.append(")");
         }
         buf.append(")");
      }
   
      if (baseFilter != null && baseFilter.trim().length() != 0)
      {
         buf.insert(0, "(& ");
         buf.append(" ");
         buf.append(baseFilter);
         buf.append(")");
      }
   
      return buf.toString();
   }


   /**
    * Prepares the filter by converting any supported wildcards
    *
    * @param filter The filter to process, may not be <code>null</code> or
    * empty.
    *
    * @return The converted filter, never <code>null</code> or empty.
    *
    * @throws PSSecurityException if the filter contains invalid wildcards.
    */
   public static String processFilter(String filter) throws PSSecurityException
   {
      if (StringUtils.isBlank(filter))
         throw new IllegalArgumentException("filter may not be null or empty");
      
      // only support '%', convert to '*'
      filter = filter.replace(
         IPSSecurityProviderMetaData.FILTER_MATCH_MANY, '*');
   
      return filter;
   }

   /**
    * The property key used to specify the alias attribute used for the
    * object attribute. This property is required dor all directory catalogers
    * and is used for authentication.
    */
   public static final String OBJECT_ATTRIBUTE_ALIAS = "objectAttributeAlias";
   
   /**
    * The attribute value used for wildcard searches.
    */
   public static final String WILDCARD_SEARCH = "*";
   
   /**
    * The standard ldap protocol.
    */
   public static final String PROTOCOL_LDAP = "ldap";
   
   /**
    * The secure ldap protocol.
    */
   public static final String PROTOCOL_LDAPS = "ldaps";

   /**
    * Character used to escape reserved characters in a DN component.
    */
   private static final char DN_ESCAPE_CHAR = '\\';

   /**
    * Array of characters that must be escaped in a DN according to the LDAPv3
    * specification.
    */
   private static final char[] DN_ESCAPE_CHARS = {',', '+', '"', '\\', '<', '>',
      ';'};

   /**
    * Set of properties used to create {@link CompoundName} objects to use to
    * create, parse and compare distinguished names.
    */
   private static final Properties ms_namingProps = new Properties();
   static
   {
      ms_namingProps.setProperty("jndi.syntax.direction", "left_to_right");
      ms_namingProps.setProperty("jndi.syntax.separator", ",");
      ms_namingProps.setProperty("jndi.syntax.ignorecase", "true");
      ms_namingProps.setProperty("jndi.syntax.escape", "\\");
      ms_namingProps.setProperty("jndi.syntax.trimblanks", "true");
   }
}
