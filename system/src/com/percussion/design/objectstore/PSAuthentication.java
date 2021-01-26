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
package com.percussion.design.objectstore;

import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSCryptographer;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A component that holds one authentication definition used to connect with
 * directory servers. See the toXml(Document) method for the DTD
 * description.
 */
public class PSAuthentication extends PSComponent
{
   private static final Logger logger = LogManager.getLogger(PSAuthentication.class);

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSAuthentication(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs this object with supplied parameters.
    *
    * @param name the name of this authentication, not <code>null</code> or
    *    empty.
    * @param scheme the authentication scheme, not <code>null</code>,
    *    one of <code>SCHEME_ENUM</code>.
    * @param user the user name used to authenticate, not <code>null</code> or
    *    empty.
    * @param userAttr the attribute name that holds the user to be
    *    authenticated, may be <code>null</code> or empty.
    * @param pw the user password to authenticate with, may be <code>null</code>
    *    or empty. If <code>null</code> is supplied, an empty
    *    <code>String</code> is used to authenticate.
    * @param filterExtension the fully qualified extension name to filter the
    *    password, may be <code>null</code> but not empty.
    */
   public PSAuthentication(String name, String scheme, String user,
      String userAttr, String pw, String filterExtension)
   {
      setName(name);
      setScheme(scheme);
      setUser(user);
      setUserAttr(userAttr);
      setPassword(pw);
      setFilterExtension(filterExtension);
   }

   /**
    * Get the name of this authentication object.
    * 
    * @return the name of this authentication object, never <code>null</code> or
    *    empty. This name is used to reference this definition from other
    *    contexts.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new name for this authentication.
    *
    * @param name the new name to set, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      m_name = name;
   }
   
   /**
    * Should the base DN be appended to the user name for authentication?
    * 
    * @return <code>true</code> if the base DN must be appended to the user
    *    name for authentication, <code>false</code> otherwise.
    */
   public boolean shouldAppendBaseDn()
   {
      return m_appendBaseDn;
   }
   
   /**
    * Set whether or not the base DN must be appended to the user name for 
    * authentication.
    * 
    * @param value <code>true</code> if the base DN must be appended to the 
    *    user name for authentication, <code>false</code> otherwise.
    */
   public void setAppendBaseDn(boolean value)
   {
      m_appendBaseDn = value;
   }

   /**
    * Get the scheme used for this authentication.
    * 
    * @return the scheme to be used for this authentication, never
    *    <code>null</code> or empty, one of <code>SCHEME_ENUM</code>.
    */
   public String getScheme()
   {
      return m_scheme;
   }

   /**
    * Set the new authentication scheme. The scheme is not case sensitive and
    * will be saved in lower case always.
    *
    * @param scheme the authentication scheme to set, not <code>null</code> or
    *    empty, must be one of <code>SCHEME_ENUM</code>.
    */
   public void setScheme(String scheme)
   {
      if (scheme == null)
         throw new IllegalArgumentException("scheme cannot be null");

      scheme = scheme.trim().toLowerCase();
      if (scheme.length() == 0)
         throw new IllegalArgumentException("scheme cannot be empty");

      boolean valid = false;
      for (int i=0; i<SCHEME_ENUM.length; i++)
      {
         if (SCHEME_ENUM[i].equals(scheme))
         {
            valid = true;
            break;
         }
      }
      if (!valid)
         throw new IllegalArgumentException("scheme must be one of SCHEME_ENUM");

      m_scheme = scheme;
   }

   /**
    * Get the user name to authenticate with.
    * 
    * @return the user name to authenticate with, never <code>null</code> but
    *    may be empty.
    */
   public String getUser()
   {
      return m_user;
   }
   
   /**
    * Get the user name to authenticate with. The base DN will be appended if 
    * this object defines so.
    * 
    * @param providerUrl the provider url to extract the base DN from, not
    *    <code>null</code> or empty.
    * @return the user name with the base DN appended if this object defines
    *    so, never <code>null</code> but may empty.
    */
   public String getUser(String providerUrl)
   {
      String user = getUser();
      if (shouldAppendBaseDn())
      {
         try
         {
            String baseDn = getBaseDn(providerUrl);
            if (baseDn.length() > 0)
               user += "," + baseDn;
         }
         catch (MalformedURLException e)
         {
            // this should never happen
         }
      }
      
      return user;
   }

   /**
    * Set a new user.
    *
    * @param user the new user, may be <code>null</code> but not empty. A 
    *    <code>null</code> parameter is stored as empty <code>String</code>.
    */
   public void setUser(String user)
   {
      if (user == null)
        user = "";

      m_user = user.trim();
   }

   /**
    * Get the attribute name for the user to authentication with.
    * 
    * @return the attribute name for the user to authenticate with, may be
    *    <code>null</code>, never empty.
    */
   public String getUserAttr()
   {
      return m_userAttr;
   }

   /**
    * Set a new user attribute name.
    *
    * @param userAttr the new attribute name which holds the user to
    *    authenticate with, may be <code>null</code> or empty. Empty 
    *    attribute names are stored as <code>null<code>.
    */
   public void setUserAttr(String userAttr)
   {
      if (userAttr != null)
         userAttr = userAttr.trim();
      if (userAttr != null && userAttr.length() == 0)
         userAttr = null;

      m_userAttr = userAttr;
   }
   
   /**
    * Get the principal <code>String</code> which is 
    * <<code>m_userAttr</code>>=<<code>m_user</code>> if a user attribute is 
    * defined, only <<code>m_user</code>> otherwise.
    * 
    * @return the principal <code>String</code> as used to authenticate, 
    *    never <code>null<code> or empty.
    */
   public String getPrincipal()
   {
      String attrName = getUserAttr();
      if (attrName != null)
         return attrName + "=" + getUser();
         
      return getUser();
   }
   
   /**
    * Get the principal for the supplied provider url. This is the user
    * attribute name appended with the user and appended with the base DN if
    * so specified in this object.
    * 
    * @param providerUrl the provider url from which to get the base DN which 
    *    will be appended to the principal if shouldAppendBaseDn()
    *    returns <code>true</code>.
    * @return the full principal string in the form 
    *    <code>userAttr=user, base DN</code>, never <code>null</code> or empty.
    */
   public String getPrincipal(String providerUrl)
   {
      String principal = getPrincipal();
      if (shouldAppendBaseDn())
      {
         try
         {
            String baseDn = getBaseDn(providerUrl);
            if (baseDn.length() > 0)
               principal += "," + baseDn;
         }
         catch (MalformedURLException e)
         {
            // this should never happen
         }
      }
      
      return principal;
   }
   
   /**
    * Get the base DN from the supplied provider url.
    * 
    * @param providerUrl the provider url from which to get the base url, not
    *    <code>null</code> or empty.
    * @return the base DN found in the supplied provider url, never 
    *    <code>null</code>, may be empty.
    * @throws MalformedURLException for any error extracting te base DN.
    */
   private String getBaseDn(String providerUrl) throws MalformedURLException
   {
      if (providerUrl == null)
         throw new IllegalArgumentException("providrurl cannot be null");
      
      providerUrl = providerUrl.trim();
      if (providerUrl.length() == 0)
         throw new IllegalArgumentException("providrurl cannot be empty");
      
      URL url = new URL(providerUrl);
      
      String baseDn = url.getFile();
      String delimiter = "/";
      int pos = baseDn.indexOf(delimiter);
      if (pos != -1)
         baseDn = baseDn.substring(pos + delimiter.length());
         
      return baseDn;
   }

   /**
    * Get the password to authenticate with.
    * 
    * @return the password to authenticate with, never <code>null</code>, may
    *    be empty.
    */
   public String getPassword()
   {
      return m_pw;
   }

   /**
    * Set a new password to authenticate with.
    *
    * @param pw the new password to authenticate with, may be <code>null</code>
    *    or empty. A <code>null</code> parameter is stored as empty
    *    <code>String</code>.
    */
   public void setPassword(String pw)
   {
      if (pw == null)
         pw = "";

      m_pw = pw.trim();
   }
   
   /**
    * Get the credential string used to authenticate with.
    * 
    * @return the credential string as used to authenticate, never 
    *    <code>null<code>, may be empty.
    */
   public String getCredentials()
   {
      return getPassword();
   }

   /**
    * @return the fully qualified filter extension name to filter the password
    *    with, never <code>null</code> may be empty.
    */
   public String getFilterExtension()
   {
      return m_filterExtension;
   }

   /**
    * Set a new password filter extension. Set it to <code>null</code> or empty
    * if no password filter needs to be applied.
    *
    * @param filterExtension the new password filter extension name, may be
    *    <code>null</code> or empty.
    */
   public void setFilterExtension(String filterExtension)
   {
      if (filterExtension == null)
         filterExtension = "";

      m_filterExtension = filterExtension.trim();
   }

   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      setName(getRequiredElement(tree, XML_ATTR_NAME, false));
      setScheme(getEnumeratedAttribute(tree, XML_ATTR_SCHEME, SCHEME_ENUM, 
         true));

      Node current = tree.getCurrent();

      Element credentials = tree.getNextElement(XML_ELEM_CREDENTIALS);
      if (credentials == null)
      {
         Object[] args = { XML_ELEM_CREDENTIALS, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      String data = tree.getElementData(XML_ELEM_USER, false);
      setUser(data);
      
      Element user = tree.getNextElement(XML_ELEM_USER);
      if (user == null)
      {
         Object[] args = { XML_ELEM_USER, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      String appendBaseDn = tree.getElementData(XML_ATTR_APPENDBASEDN, false);
      if (appendBaseDn != null)
         setAppendBaseDn(appendBaseDn.equalsIgnoreCase(XML_ATTRVALUE_YES));
      setUserAttr(tree.getElementData(XML_ATTR_ATTRIBUTE_NAME, false));
      
      tree.setCurrent(credentials);

      Element password = tree.getNextElement(XML_ELEM_PASSWORD);
      if (password == null)
      {
         Object[] args = { XML_ELEM_PASSWORD, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      String encryptedValue = tree.getElementData(XML_ATTR_ENCRYPTED, false);
      boolean encrypted = (encryptedValue != null && 
         encryptedValue.trim().equalsIgnoreCase(XML_ATTRVALUE_YES));
         
      data = tree.getElementData(XML_ELEM_PASSWORD, false);
      if (encrypted)
      {
         String userStr = getUser();
         String key = userStr.trim().length() == 0 ? PSLegacyEncrypter.INVALID_DRIVER() : userStr;

         try{
            PSEncryptor.getInstance().decrypt(data);
         } catch (PSEncryptionException e) {
            data = PSCryptographer.decrypt(PSLegacyEncrypter.INVALID_CRED(), key, data);
         }

      }
      setPassword(data);

      tree.setCurrent(current);

      Element filterExtension =
         tree.getNextElement(XML_ELEM_FILTER_EXTENSION_NAME);
      if (filterExtension != null)
         setFilterExtension(PSXmlTreeWalker.getElementData(filterExtension));
   }

   /**
    * Creates an XML output for the following DTD:
    * &lt;!ELEMENT PSXAuthentication (Credentials, FilterExtensionName?)&gt;
    * &lt;!ATTLIST PSXAuthentication
    *    name CDATA #REQUIRED
    * &lt;
    * &lt;!ELEMENT Credentials (User, Password)&gt;
    * &lt;!ELEMENT User (#PCDATA)&gt;
    * &lt;!ATTLIST User
    *    attributeName CDATA #REQUIRED
    * &lt;
    * &lt;!ELEMENT Password (#PCDATA)&gt;
    * &lt;!ATTLIST Password
    *    attributeName CDATA #REQUIRED
    *    encrypted (yes | no) "yes"
    * &lt;
    * &lt;!ELEMENT FilterExtensionName (#PCDATA)&gt;
    * &lt;!ATTLIST PSAAuthentication
    *    name CDATA #REQUIRED
    * &lt;
    *  
    * @see IPSComponent 
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, getName());
      root.setAttribute(XML_ATTR_SCHEME, getScheme());

      Element credentials = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         XML_ELEM_CREDENTIALS);

      String userStr = getUser();
      Element user = PSXmlDocumentBuilder.addElement(doc, credentials,
         XML_ELEM_USER, userStr);
      user.setAttribute(XML_ATTR_APPENDBASEDN, shouldAppendBaseDn() ? 
         XML_ATTRVALUE_YES : XML_ATTRVALUE_NO);
      if (getUserAttr() != null)
         user.setAttribute(XML_ATTR_ATTRIBUTE_NAME, getUserAttr());

      String pw = null;
      try {
         pw = PSEncryptor.getInstance().encrypt(getPassword());
      } catch (PSEncryptionException e) {
         logger.error("Error encrypting password: " + e.getMessage(),e);
         pw = "";
      }

      Element password = PSXmlDocumentBuilder.addElement(doc, credentials,
         XML_ELEM_PASSWORD, pw);
      password.setAttribute(XML_ATTR_ENCRYPTED, XML_ATTRVALUE_YES);

      if (getFilterExtension().length() != 0)
         PSXmlDocumentBuilder.addElement(doc, root,
            XML_ELEM_FILTER_EXTENSION_NAME, getFilterExtension());

      return root;
   }

   /** @see PSComponent */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom(c);

      if (!(c instanceof PSAuthentication))
         throw new IllegalArgumentException("c must be a PSAuthentication object");

      PSAuthentication o = (PSAuthentication) c;

      setName(o.getName());
      setScheme(o.getScheme());
      setUser(o.getUser());
      setAppendBaseDn(o.shouldAppendBaseDn());
      setUserAttr(o.getUserAttr());
      setPassword(o.getPassword());
      setFilterExtension(o.getFilterExtension());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAuthentication)) return false;
      PSAuthentication that = (PSAuthentication) o;
      return m_appendBaseDn == that.m_appendBaseDn &&
              Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_scheme, that.m_scheme) &&
              Objects.equals(m_user, that.m_user) &&
              Objects.equals(m_userAttr, that.m_userAttr) &&
              Objects.equals(m_filterExtension, that.m_filterExtension);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_scheme, m_user, m_appendBaseDn, m_userAttr, m_filterExtension);
   }

   /**
    * Test if the supplied <code>String</code> objects are equal.
    * 
    * @param a the first string to be tested, assumed not <code>null</code>.
    * @param b the second string to be tested, assumed not <code>null</code>.
    * @return <code>true</code> if both strings are equal, <code>false</code>
    *    otherwise.
    */
   private boolean equals(String a, String b)
   {
      if (a == null && b == null)
         return true;
         
      if (a != null && b != null)
         return a.equals(b);
         
      return false;
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXAuthentication";

   /**
    * The scheme element value if no authentication scheme is used.
    */
   public static final String SCHEME_NONE = "none";

   /**
    * The scheme element value for the simple authentication scheme.
    */
   public static final String SCHEME_SIMPLE = "simple";

   /**
    * The scheme element value for the cram-md authentication scheme.
    */
   public static final String SCHEME_CRAMMD = "cram-md";

   /**
    * An array with all supported authentication schemes.
    */
   public static final String[] SCHEME_ENUM =
   {
      SCHEME_NONE,
      SCHEME_SIMPLE,
      SCHEME_CRAMMD
   };

   /**
    * Holds the name of this authentication. This name is used to reference
    * this definition from other contexts. Set during construction, never
    * <code>null</code> or empty after that.
    */
   private String m_name = null;

   /**
    * Holds the authentication scheme. Set during construction, never
    * <code>null</code> or empty after that.
    */
   private String m_scheme = null;

   /**
    * Holds the user to authenticate with. Set during construction, never
    * <code>null</code> may be empty after that.
    */
   private String m_user = null;
   
   /**
    * This flag is used to specify whether or not the base DN must be appended
    * to the user name for authentication. Defaults to <code>false</code>, set
    * through setAppendBaseDn(boolean).
    */
   private boolean m_appendBaseDn = false;

   /**
    * Holds the attribute name which holds the user name to authenticate with.
    * Set during construction, may be <code>null</code> but not empty after 
    * that.
    */
   private String m_userAttr = null;

   /**
    * Holds the user password to authenticate with. Set during construction,
    * never <code>null</code> after that, may be empty.
    */
   private String m_pw = null;

   /**
    * Holds the fully qualified name for the filter extension used to filter
    * the password. Set during construction, never <code>null</code> after
    * that but may be empty. Is set to empty if no password filter needs to be
    * applied.
    */
   private String m_filterExtension = "";

   // XML element and attribute constants.
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_SCHEME = "scheme";
   private static final String XML_ATTR_APPENDBASEDN = "appendBaseDn";
   private static final String XML_ATTR_ATTRIBUTE_NAME = "attributeName";
   private static final String XML_ATTR_ENCRYPTED = "encrypted";
   private static final String XML_ELEM_CREDENTIALS = "Credentials";
   private static final String XML_ELEM_FILTER_EXTENSION_NAME =
      "FilterExtensionName";
   private static final String XML_ELEM_USER = "User";
   private static final String XML_ELEM_PASSWORD = "Password";
   private static final String XML_ATTRVALUE_YES = "yes";
   private static final String XML_ATTRVALUE_NO = "no";
}
