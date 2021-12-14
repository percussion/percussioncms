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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The PSSecurityConfiguration class defines an entry in the user-security-conf.xml
 * file under the rxapp.war/WEB-INF/config/user/security directory.
 * This entry defines which paths allow anonymous access or basic authorization,
 * as well as if secure login is required.
 */
public class PSSecurityConfiguration implements IPSDocument
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from
    *
    * @exception   PSUnknownDocTypeException if the XML document is not
    *                                      of type PSSecurityConfiguration
    *
    * @exception   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    */
   public PSSecurityConfiguration(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceDoc);
   }

   /**
    * Empty constructor for creating from serialization, fromXml() etc
    *
    */
   PSSecurityConfiguration()
   {
      super();
      m_isForceSecureLogin = XML_FLAG_TYPE_NO;
      m_paths = new ArrayList();
   }

   /**
    * Creates a security configuration entry with the specified secure login
    * status and path elements. 
    *
    * @param   isForceSecureLogin  the secure login status of this entry
    *
    * @param   paths  the path elements included in this security configuration
    */
   public PSSecurityConfiguration(String isForceSecureLogin, ArrayList paths)
   {
      super();
      m_isForceSecureLogin = isForceSecureLogin;
      m_paths = paths;
   }

   /**
    * Get the secure login status associated with this entry.
    *
    * @return   the secure login status associated with this entry
    */
   public String getIsForceSecureLogin()
   {
      return m_isForceSecureLogin;
   }

   /**
    * Set the secure login status associated with this entry.
    *
    * @param   isForceSecureLogin the secure login status to associate
    *                  with this entry. 
    */
   public void setIsForceSecureLogin(String isForceSecureLogin)
   {
      m_isForceSecureLogin = isForceSecureLogin;
   }

   // **************   IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXSecurityConfiguration XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *  &lt;!--
    *     The PSSecurityConfiguration class defines an entry in the user-security-conf.xml
    *     file under the rxapp.war/WEB-INF/config/user/security directory.
    *     This entry defines which paths allow anonymous access or basic authorization,
    *     as well as if secure login is required.
    *  --&gt;
    *  &lt;!ELEMENT PSSecurityConfiguration               (path*)&gt;
    *  &lt;!ATTLIST forceSecureLogin (yes | no) "no"&gt;
    *     
    *  &lt;!--
    *     The PSPath class defines a path with authentication type:
    *
    *     form - this entry represents form based authentication.
    *
    *     basic - this entry represents basic authenitication.
    *
    *     anonymous - this entry represents anonymous authentication.
    *  --&gt;
    *  &lt;!ELEMENT path                   (#PCDATA)&gt;
    *  &lt;!ATTLIST authType (form | basic | anonymous) "form"&gt;
    *
    * @return   the newly created securityConfiguration XML element node
    */
   public Document toXml()
   {
      //create PSSecurityConfiguration element
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(ms_NodeType);
      
      //add secure login status attribute
      root.setAttribute("forceSecureLogin", getIsForceSecureLogin());
      
      Element node = null;
      PSPath pathElem = null;
      int i;
      
      for (i=0; i < m_paths.size(); i++)
      {
         //Set the value and authentication type
         pathElem = (PSPath) m_paths.get(i);
         node = PSXmlDocumentBuilder.addElement(doc, root, "path", pathElem.getPath());
         node.setAttribute("authType", pathElem.getAuthType());
         root.appendChild(node);
      }

      doc.appendChild(root);
      return doc;
   }

   /**
    * This method is called to populate a PSSecurityConfiguration Java object
    * from a PSSecurityConfiguration XML document. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownDocTypeException if the XML document is not
    *                                      of type PSSecurityConfiguration
    */
   public void fromXml(Document sourceDoc)
                       throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if (sourceDoc == null){
         throw new PSUnknownDocTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      
      //make sure we got the security configuration type node
      if (false == ms_NodeType.equals(root.getNodeName())){
         Object[] args = { ms_NodeType, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);

      //get the security configuration forceSecureLogin element from attribute
      String sTemp = tree.getElementData("forceSecureLogin");
      if ((sTemp == null) || (sTemp.length() == 0)){
         Object[] args = { ms_NodeType, "forceSecureLogin", "empty" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else if (sTemp.equals(XML_FLAG_TYPE_YES))
         setIsForceSecureLogin(XML_FLAG_TYPE_YES);
      else if (sTemp.equals(XML_FLAG_TYPE_NO))
         setIsForceSecureLogin(XML_FLAG_TYPE_NO);
      else{
         Object[] args = { ms_NodeType, "forceSecureLogin", sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      //Read path elements of the security configuration
      m_paths = new ArrayList();
      
      String authType = "";
      String path = "";
      Element pathElem = null;
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      
      pathElem = tree.getNextElement(PSPath.XML_NODE_NAME, firstFlags); 
      while (pathElem != null)
      {
         authType = tree.getElementData(PSPath.XML_ATTR_NAME, false);
         path = tree.getElementData();
          
         m_paths.add(new PSPath(authType, path));
         pathElem = tree.getNextElement(PSPath.XML_NODE_NAME, nextFlags);
      }
   }

   /**
    * This method adds a PSPath to the path container
    *
    * @param authType the authentication type of the path to add
    * @param path the path to add
    */
   public void addPath(String authType, String path)
   {
      m_paths.add(new PSPath(authType, path));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSecurityConfiguration)) return false;
      PSSecurityConfiguration that = (PSSecurityConfiguration) o;
      return Objects.equals(m_isForceSecureLogin, that.m_isForceSecureLogin) &&
              Objects.equals(m_paths, that.m_paths);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_isForceSecureLogin, m_paths);
   }

   private String m_isForceSecureLogin = "";
   private ArrayList m_paths = null;
   
   private static final String XML_FLAG_TYPE_YES      = "yes";
   private static final String XML_FLAG_TYPE_NO       = "no";
   
   /**
    * The form authentication type constant
    */
   public static final String FORM_AUTH_TYPE = "form";
   
   /**
    * The basic authentication type constant
    */
   public static final String BASIC_AUTH_TYPE = "basic";
   
   /**
    * The anonymous authentication type constant
    */
   public static final String ANONYMOUS_AUTH_TYPE = "anonymous";
   
   // package access on this so they may reference each other in fromXml
   static final String   ms_NodeType            = "securityConfiguration";
   
   /**
    * This class represents a path element in the PSSecurityConfiguration object.
    * 
    * @author peterfrontiero
    *
    */
   private class PSPath
   {
      /**
       * Creates a path entry with the specified authentication type and path. 
       *
       * @param   authType  the authentication type to be used with this entry
       *
       * @param   path      the path to authenticate
       */
      public PSPath(String authType, String path)
      {
         m_authType = authType;
         m_path = path;
      }
      
      /**
       * AuthType getter method
       */
      public String getAuthType()
      {
         return m_authType;
      }
      
      /**
       * Path getter method
       */
      public String getPath()
      {
         return m_path;
      }
      
      /**
       * The authentication type
       */
      private String m_authType = "";
      
      /**
       * The path
       */
      private String m_path = "";
      
      /**
       * The path xml node name
       */
      public static final String XML_NODE_NAME = "path";
      
      /**
       * The authType xml attribute name
       */
      public static final String XML_ATTR_NAME = "authType";
   }
}

