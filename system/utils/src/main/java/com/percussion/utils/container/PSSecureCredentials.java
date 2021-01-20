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

package com.percussion.utils.container;

import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents an "application-policy" element in the 
 * {@link PSJBossUtils#LOGIN_CONFIG_FILE_NAME} file, used to save datasource 
 * credentials with an encrypted password.  Note that this is not implemented to
 * load save application policy elements in general, only those specified by 
 * this class.
 */
public class PSSecureCredentials
{
   /**
    * Constant for the "application-policy" element name.
    */
   public static final String APP_POLICY_NODE_NAME = "application-policy";

   /**
    * Determine if the supplied node specifies the supplied security domain
    * 
    * @param node The node to check, may not be <code>null</code> and must be
    * an {@link #APP_POLICY_NODE_NAME} element.
    * @param secDomain The name to match, may not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if it is a match, <code>false</code> if not.
    */
   public static boolean isMatch(Element node, String secDomain)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      if (!APP_POLICY_NODE_NAME.equals(node.getNodeName()))
         throw new IllegalArgumentException("Invalid node");
      
      if (StringUtils.isBlank(secDomain))
         throw new IllegalArgumentException(
            "secDomain may not be null or empty");

      return secDomain.equals(getName(node));
   }
   
   /**
    * Determine if the supplied element represents a {@link PSSecureCredentials}
    * 
    * @param node The element to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it is, <code>false</code> if not.
    */
   public static boolean isSecureCredentials(Element node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      return getName(node).startsWith(SEC_DOMAIN_PREFIX);
   }

   /**
    * Get the domain name of the secure credential specified by the supplied
    * element.
    * 
    * @param node The node to check, may not be <code>null</code>.
    * 
    * @return The name, never <code>null</code>, may be empty. 
    */
   public static String getName(Element node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      return node.getAttribute(NAME_ATTR);
   }
   
   /**
    * Construct this object from it's properties.
    * 
    * @param datasourceName The name of the JNDI Datasource for which this
    * object will specify credentials, never <code>null</code> or empty.
    * @param userId The user name, may not be <code>null</code> or empty.
    * @param password The password, already encrypted, may be <code>null</code>
    * or empty.  The password must be encrypted in a form that will be handled
    * by the {@link #LOGIN_MODULE_CLASSNAME}, which uses the 
    * {@link PSLegacyEncrypter} to decrypt passwords.
    */
   public PSSecureCredentials(String datasourceName, String userId,
      String password)
   {
      if (StringUtils.isBlank(datasourceName))
         throw new IllegalArgumentException(
            "datasourceName may not be null or empty");
      
      if (StringUtils.isBlank(userId))
         throw new IllegalArgumentException("userId may not be null or empty");
      
      m_datasourceName = datasourceName;
      m_userId = userId;
      m_password = (StringUtils.isBlank(password) ? "" : password);
      m_securityDomain = calculateSecurityDomain(datasourceName);
   }
   
   /**
    * Construct this object from its XML representation as defined by the
    * "application-policy" element in the JBoss security_config.dtd DTD.
    *   
    * @param source The source element, may not be <code>null</code>.
    * @throws PSInvalidXmlException If the supplied element is invalid.
    */
   public PSSecureCredentials(Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      if (!source.getNodeName().equals(APP_POLICY_NODE_NAME))
         throw new IllegalArgumentException(
            "invalid source application policy element");
      
      String policyName = getName(source);
      if (StringUtils.isBlank(policyName))
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, 
            new Object[] {APP_POLICY_NODE_NAME, NAME_ATTR, policyName});
      m_securityDomain = policyName;
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      
      Element authEl = tree.getNextElement(AUTHENTICATION, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (authEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            AUTHENTICATION);

      Element loginModEl = tree.getNextElement(LOGIN_MODULE, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (loginModEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            LOGIN_MODULE);
      
      Element moduleOptionEl = tree.getNextElement(MODULE_OPTION,  
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (moduleOptionEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            MODULE_OPTION);
      boolean foundPassword = false;
      while (moduleOptionEl != null)
      {
         String nameAttr = getName(moduleOptionEl);
         if (USERNAME_ATTR_VAL.equals(nameAttr))
            m_userId = PSXmlUtils.getElementData(moduleOptionEl, 
               MODULE_OPTION, true);
         else if (PASSWORD_ATTR_VAL.equals(nameAttr))
         {
            foundPassword = true;
            m_password = PSXmlUtils.getElementData(moduleOptionEl, 
               MODULE_OPTION, false);
         }
         else if (MANAGED_CONN_FACTORY_ATTR_VAL.equals(nameAttr))
         {
            String factoryName = PSXmlUtils.getElementData(moduleOptionEl, 
               MODULE_OPTION, true);
            // format is "jboss.jca:service=LocalTxCM,name=jdbc/rxdefault", get
            // name of datasource following the "="
            m_datasourceName = StringUtils.substringAfterLast(factoryName, "=");
         }
         moduleOptionEl = tree.getNextElement(MODULE_OPTION,  
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      // make sure we got user name, dsname, and at least empty pwd.
      if (m_userId == null)
      {
         throw new PSInvalidXmlException(
            IPSXmlErrors.XML_ELEMENT_ATTR_INVALID_VAL, 
            new Object[] {MODULE_OPTION, NAME_ATTR, USERNAME_ATTR_VAL, "null"});
      }
      else if (!foundPassword)
      {
         throw new PSInvalidXmlException(
            IPSXmlErrors.XML_ELEMENT_ATTR_INVALID_VAL, 
            new Object[] {MODULE_OPTION, NAME_ATTR, PASSWORD_ATTR_VAL, ""});
      }
      else if (m_securityDomain == null)
      {
         throw new PSInvalidXmlException(
            IPSXmlErrors.XML_ELEMENT_ATTR_INVALID_VAL, 
            new Object[] {MODULE_OPTION, NAME_ATTR, 
               MANAGED_CONN_FACTORY_ATTR_VAL, "null"});
      }      
   }

   /**
    * Serialize this object to its XML representation.  See 
    * {@link #PSSecureCredentials(Element)} for more information.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    * 
    * @return The root XML element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(APP_POLICY_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_securityDomain);
      
      Element authEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         AUTHENTICATION);
      
      Element loginModuleEl = PSXmlDocumentBuilder.addEmptyElement(doc, authEl, 
         LOGIN_MODULE);
      loginModuleEl.setAttribute(LOGIN_MODULE_CODE_ATTR, LOGIN_MODULE_CLASSNAME);
      loginModuleEl.setAttribute(LOGIN_MODULE_FLAG_ATTR, "required");
      
      Element moduleEl;
      moduleEl = PSXmlDocumentBuilder.addElement(doc, loginModuleEl, 
         MODULE_OPTION, m_userId);
      moduleEl.setAttribute(NAME_ATTR, USERNAME_ATTR_VAL);
      
      moduleEl = PSXmlDocumentBuilder.addElement(doc, loginModuleEl, 
         MODULE_OPTION, m_password);
      moduleEl.setAttribute(NAME_ATTR, PASSWORD_ATTR_VAL);
      
      // format is "jboss.jca:service=LocalTxCM,name=jdbc/rxdefault", specify
      // name of datasource following the "="      
      moduleEl = PSXmlDocumentBuilder.addElement(doc, loginModuleEl, 
         MODULE_OPTION, "jboss.jca:service=LocalTxCM,name=" + m_datasourceName);
      moduleEl.setAttribute(NAME_ATTR, MANAGED_CONN_FACTORY_ATTR_VAL);
      
      return root;
   }

   /**
    * Get the user name specified by these credentials.
    * 
    * @return The user name, never <code>null</code> or empty.
    */
   public String getUserId()
   {
      return m_userId;
   }

   /**
    * Get the password specified by these credentials.
    * 
    * @return The encrypted password, may be empty, never <code>null</code>.
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * Get the name of the JNDI Datasource for which this object specifies
    * credentials.
    * 
    * @return The name, never <code>null</code> or empty
    */
   public String getDatasourceName()
   {
      return m_datasourceName;
   }
   
   /**
    * Get the name to use as the security domain name when refering to these
    * credentials in the datasource configuration.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getSecurityDomainName()
   {
      return m_securityDomain;
   }


   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
   
   /**
    * Constructs a security domain name based on the supplied datasource name.
    * 
    * @param datasourceName The datasource name, assumed not <code>null</code> 
    * or empty.
    * 
    * @return The security domain name, never <code>null</code> or empty.
    */
   private String calculateSecurityDomain(String datasourceName)
   {
      return SEC_DOMAIN_PREFIX + datasourceName.replace('/', '_');
   }
   
   /**
    * The datasource name, initialized during ctor, never <code>null</code>, 
    * empty, or modified after that.
    */
   private String m_datasourceName;
   
   /**
    * The username, initialized during ctor, never <code>null</code>, 
    * empty, or modified after that.
    */
   private String m_userId;
   
   /**
    * The password, initialized during ctor, may be <code>null</code>, never 
    * empty or modified after that.
    */
   private String m_password;
   
   /**
    * The security domain, initialized during ctor , never <code>null</code>, 
    * empty, or modified after that.
    */
   private String m_securityDomain;

   /**
    * Constant for the security domain prefix.
    */
   private static final String SEC_DOMAIN_PREFIX = "rx.datasource.";
   
   /**
    * Constant for the login module class anem.
    */
   private static final String LOGIN_MODULE_CLASSNAME = 
      "com.percussion.services.security.loginmods.PSSecureIdentityLoginModule";
   
   // private XML constants.
   private static final String NAME_ATTR = "name";
   private static final String AUTHENTICATION = "authentication";
   private static final String LOGIN_MODULE = "login-module";
   private static final String LOGIN_MODULE_CODE_ATTR = "code";
   private static final String LOGIN_MODULE_FLAG_ATTR = "flag";
   private static final String MODULE_OPTION = "module-option";
   private static final String USERNAME_ATTR_VAL = "username";
   private static final String PASSWORD_ATTR_VAL = "password";
   private static final String MANAGED_CONN_FACTORY_ATTR_VAL = 
      "managedConnectionFactoryName";
}
