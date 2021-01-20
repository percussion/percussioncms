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

package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the data necessary to construct a
 * <code>PSDeploymentServerConnection</code>.
 */
public class PSDeploymentServerConnectionInfo implements IPSDeployComponent
{
   /**
    * Constructs the object using the specified parameters.
    * 
    * @param server The name of the server to connect to, may not be
    *           <code>null</code> or empty.
    * @param port The port on the server. Must be greater than 0.
    * @param userid The user id to connect using, may not be <code>null</code>
    *           or empty.
    * @param password The password, may be <code>null</code> or empty. If
    *           <code>null</code>, and empty <code>String</code> is stored.
    * @param isPwdEncrypted If <code>true</code>, the password will be
    *           treated as encrypted. Otherwise, it is assumed to be clear text
    *           and will be encryted for storage or serialization to the server.
    */
   public PSDeploymentServerConnectionInfo(String server, int port,
         String userid, String password, boolean encrypted)
   {
      // validate arguments
      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");

      if (port <= 0)
         throw new IllegalArgumentException("port must be greater than zero");

      if (userid == null || userid.trim().length() == 0)
         throw new IllegalArgumentException("userid may not be null or empty");

      m_server = server;
      m_port = port;
      m_userid = userid;
      m_password = password;
      m_isPwdEncrypted = encrypted;
   }

   /**
    * Constructs the object from its XML representation.
    * 
    * @param source the element that represents the object, not
    *           <code>null</code>.
    * @throws PSUnknownNodeTypeException propagated from <code>fromXml</code>
    *            if the XML element node does not represent a type supported by
    *            the class.
    */
   public PSDeploymentServerConnectionInfo(Element source)
         throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   // see base class for javadoc
   public boolean equals(Object obj)
   {
      boolean result = false;
      if (obj instanceof PSDeploymentServerConnectionInfo)
      {
         PSDeploymentServerConnectionInfo info = (PSDeploymentServerConnectionInfo) obj;
         result = m_server.equals(info.m_server) && m_port == info.m_port
               && m_userid.equals(info.m_userid) && m_password.equals(info.m_password)
               && m_isPwdEncrypted == info.m_isPwdEncrypted;
      }
      return result;
   }

   // see base class for javadoc
   public int hashCode()
   {
      StringBuffer fieldMashup = new StringBuffer();
      fieldMashup.append(m_server).append("|");
      fieldMashup.append(m_port).append("|");
      fieldMashup.append(m_userid).append("|");
      fieldMashup.append(m_password).append("|");
      fieldMashup.append(m_isPwdEncrypted).append("|");
      
      return fieldMashup.toString().hashCode();
   }

   // see interface for javadoc
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_SERVER, m_server);
      root.setAttribute(XML_ATTR_PORT, String.valueOf(m_port));
      root.setAttribute(XML_ATTR_USERID, m_userid);
      root.setAttribute(XML_ATTR_PASSWORD, m_password);
      root.setAttribute(XML_ATTR_IS_PWD_ENCRYPTED, String
            .valueOf(m_isPwdEncrypted));
      return root;
   }

   // see interface for javadoc
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_server = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_SERVER);
      m_port = Integer.parseInt(PSDeployComponentUtils.getRequiredAttribute(
            sourceNode, XML_ATTR_PORT));
      m_userid = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_USERID);
      m_password = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_PASSWORD);
      m_isPwdEncrypted = Boolean.getBoolean(PSDeployComponentUtils
            .getRequiredAttribute(sourceNode, XML_ATTR_IS_PWD_ENCRYPTED));
   }

   // see interface for javadoc
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj parameter must not be null");

      if (!(obj instanceof PSDeploymentServerConnectionInfo))
         throw new IllegalArgumentException(
               "obj wrong type, expecting PSServerConnectionInfo");

      PSDeploymentServerConnectionInfo info = (PSDeploymentServerConnectionInfo) obj;

      m_server = info.m_server;
      m_port = info.m_port;
      m_userid = info.m_userid;
      m_password = info.m_password;
      m_isPwdEncrypted = info.m_isPwdEncrypted;

   }

   public boolean isPwdEncrypted()
   {
      return m_isPwdEncrypted;
   }

   public String getPassword()
   {
      return m_password;
   }

   public int getPort()
   {
      return m_port;
   }

   public String getServer()
   {
      return m_server;
   }

   public String getUserid()
   {
      return m_userid;
   }

   /**
    * Returns a string representation of the object.
    * 
    * @return concatenation of the userid, server, and port fields, never
    *         <code>null</code> or empty.
    */
   public String toString()
   {
      return m_userid + "@" + m_server + ":" + m_port;
   }

   /**
    * Root node name of the object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXServerConnectionInfo";

   /**
    * The name of the server to connect to, set during ctor, never
    * <code>null</code>, empty or modified after that.
    */
   private String m_server;

   /**
    * The port on the server to connect to, set during ctor, never modified
    * after that.
    */
   private int m_port;

   /**
    * The user's id. Set during ctor, never <code>null</code> or empty or
    * modified after that.
    */
   private String m_userid;

   /**
    * The user's password. Set during ctor, may be empty, never
    * <code>null</code> or modified after that.
    */
   private String m_password;

   /**
    * If <code>true</code>, the password will be treated as encrypted.
    * Otherwise, it is assumed to be clear text and will be encryted for storage
    * or serialization to the server.
    */
   private boolean m_isPwdEncrypted;

   /**
    * Name of the attribute containing the isPwdEncrypted field in the object's
    * XML representation.
    */
   private static final String XML_ATTR_IS_PWD_ENCRYPTED = "isPwdEncrypted";

   /**
    * Name of the attribute containing the password field in the object's XML
    * representation.
    */
   private static final String XML_ATTR_PASSWORD = "password";

   /**
    * Name of the attribute containing the userid field in the object's XML
    * representation.
    */
   private static final String XML_ATTR_USERID = "userid";

   /**
    * Name of the attribute containing the port field in the object's XML
    * representation.
    */
   private static final String XML_ATTR_PORT = "port";

   /**
    * Name of the attribute containing the server field in the object's XML
    * representation.
    */
   private static final String XML_ATTR_SERVER = "server";

}
