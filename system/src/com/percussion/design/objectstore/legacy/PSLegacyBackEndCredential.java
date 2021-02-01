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


package com.percussion.design.objectstore.legacy;


import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.IPSValidationContext;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;


/**
 * The PSBackEndCredential class defines access credentials for a
 * back-end data store. The credentials are then used by the server
 * whenever accessing the particular back-end.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
@Deprecated
public class PSLegacyBackEndCredential extends PSComponent
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSLegacyBackEndCredential(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSLegacyBackEndCredential()
   {
      super();
   }

   /**
    * Constructs a back-end credential object with the specified name.
    *
    * @param alias   the new alias for this object. This must be a
    *                 unique name on the E2 server. If it is non-unique,
    *                 an exception will be thrown when the application or
    *                 server containing this entry is saved.
    *
    * @see       #setAlias
    */
   public PSLegacyBackEndCredential(java.lang.String alias)
   {
      super();
      setAlias(alias);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSLegacyBackEndCredential)) return false;
      if (!super.equals(o)) return false;
      PSLegacyBackEndCredential that = (PSLegacyBackEndCredential) o;
      return Objects.equals(m_alias, that.m_alias) &&
              Objects.equals(m_comment, that.m_comment) &&
              Objects.equals(m_driver, that.m_driver) &&
              Objects.equals(m_server, that.m_server) &&
              Objects.equals(m_userId, that.m_userId) &&
              Objects.equals(m_password, that.m_password) &&
              Objects.equals(m_condCollection, that.m_condCollection);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_alias, m_comment, m_driver, m_server, m_userId, m_password, m_condCollection);
   }

   /**
    * Get the alias used to reference this object.
    *
    * @return      the alias of the back-end table
    */
   public java.lang.String getAlias()
   {
      return m_alias;
   }

   /**
    * Set the alias used to reference this object.
    * Aliases are limited to 128 characters.
    *
    * @param alias   the new alias for this object. This must be a
    *                 unique name on the E2 server. If it is non-unique,
    *                 an exception will be thrown when the application or
    *                 server containing this entry is saved.
    */
   public void setAlias(java.lang.String alias)
   {
      IllegalArgumentException ex = validateAlias(alias);

      if (ex != null)
         throw ex;

      m_alias = alias;
   }

   private static IllegalArgumentException validateAlias(String alias)
   {
      if (null == alias || alias.length() == 0)
         return new IllegalArgumentException("back-end credit alias is null");

      if (alias.length() > MAX_ALIAS_NAME_LEN)
      {
            return new IllegalArgumentException("back-end credit alias is too big" +
               MAX_ALIAS_NAME_LEN + " " + alias.length());
      }

      return null;
   }

   /**
    * Get the comment associated with this back-end credential object.
    *
    * @return       the associated comment
    */
   public java.lang.String getComment()
   {
      return m_comment;
   }

   /**
    * Set the comment associated with this back-end credential object.
    * Comments are limited to 255 characters.
    *
    * @param comment  a descriptive blurb to associate with this object
    */
   public void setComment(String comment)
   {
      if (comment == null)
         comment = "";

      IllegalArgumentException ex = validateComment(comment);
      if (ex != null)
         throw ex;

      m_comment = comment;
   }

   public static IllegalArgumentException validateComment(String comment)
   {
      if (comment != null && comment.length() > MAX_COMMENT_LEN) {
            return new IllegalArgumentException("back-end credit comment is too big" +
               MAX_COMMENT_LEN + " " + comment.length());
      }

      return null;
   }
   /**
    * Get the back-end driver for which the credentials should be used.
    *
    * @return       the back-end driver for which the credentials should
    *              be used
    */
   public java.lang.String getDriver()
   {
      return m_driver;
   }

   /**
    * Set the driver used to access the back-end table.
    * Drivers are limited to 128 characters.
    * <p>
    * The valid list of drivers can be located by using the PSCataloger
    * to perform a catalog of category "data" and type "Driver".
    *
    * @param driver   the back-end driver for which the credentials should
    *                 be used.
    */
   public void setDriver(String driver)
   {
      IllegalArgumentException ex = validateDriver(driver);
      if (ex != null)
         throw ex;

      m_driver = driver;
   }

   private static IllegalArgumentException validateDriver(String driver)
   {
      if (null == driver || driver.length() == 0)
         return new IllegalArgumentException("back-end driver is null");
      else if (driver.length() > MAX_DRIVER_NAME_LEN) {
            return new IllegalArgumentException("back-end driver is too big" +
               MAX_DRIVER_NAME_LEN + " " + driver.length());
      }

      return null;
   }

   /**
    * Get the back-end server for which the credentials should be used.
    *
    * @return       the back-end server for which the credentials should
    *              be used
    */
   public java.lang.String getServer()
   {
      return m_server;
   }

   /**
    * Set the back-end server for which the credentials should be used.
    *
    * @param server   the back-end server for which the credentials should be
    *             used. This is limited to ??? characters.
    */
   public void setServer(java.lang.String server)
   {
      if (server == null)
         server = "";

      IllegalArgumentException ex = validateServer(server);
      if (ex != null)
         throw ex;

      m_server = server;
   }

   private static IllegalArgumentException validateServer(String server)
   {
      if (server != null && server.length() > MAX_SERVER_NAME_LEN) {
         return new IllegalArgumentException("back-end server is too big" +
            MAX_SERVER_NAME_LEN + " " + server.length());

      }

      return null;
   }

   /**
    * Get the user id to login to the back-end with.
    *
    * @return       the user id to login to the back-end with
    */
   public java.lang.String getUserId()
   {
      return m_userId;
   }

   /**
    * Set the user id to login to the back-end with.
    *
    * @param uid      the user id to login to the back-end with.
    *             This is limited to ??? characters.
    */
   public void setUserId(java.lang.String uid)
   {
      if (uid == null)
         uid = "";

      IllegalArgumentException ex = validateUserId(uid);
      if (ex != null)
         throw ex;

      m_userId  = uid;
   }

   private static IllegalArgumentException validateUserId(String uid)
   {
      if (uid != null && uid.length() > MAX_UID_LEN)
      {
         return new IllegalArgumentException("back-end UID is too big" +
            MAX_UID_LEN + " " + uid.length());
      }

      return null;
   }

   /**
    * Get the password to login to the back-end with.
    *
    * @return       the password to login to the back-end with
    */
   public java.lang.String getPassword()
   {
      return m_password;
   }

   /**
    * Set the password to login to the back-end with.
    *
    * @param pw the password to login to the back-end with.
    *            This is limited to MAX_PASSWORD_LEN characters.
    */
   public void setPassword(java.lang.String pw)
   {
      if (pw == null)
         pw = "";

      IllegalArgumentException ex = validatePassword(pw);
      if (ex != null)
         throw ex;

      m_password = makeLasagna(pw);
   }

   /**
    * Get the collection of PSConditional objects.
    */
   public PSCollection getConditionals()
   {
      return m_condCollection;
   }

   /**
    * Set the conditions which must be met for these credentials to be used.
    * @param   conds  a collection of PSConditional objects
    */
   public void setConditionals(PSCollection conds)
   {
      if (conds == null){
         m_condCollection = null;
         return;
      }

      IllegalArgumentException ex = validateCondCollection(conds);
      if (ex != null)
         throw ex;

      m_condCollection = conds;
   }

   private static IllegalArgumentException validateCondCollection(PSCollection conds)
   {
      if (conds != null) {
         if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
            conds.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Backend Credential Conditionals: " +
               conds.getMemberClassName());
         }
      }

      return null;
   }

   private String makeLasagna(String pwd)
   {
      try {
         return PSEncryptor.getInstance("AES",
                 PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).encrypt(pwd);
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   private static IllegalArgumentException validatePassword(String pw)
   {
      if (pw != null && pw.length() > MAX_PASSWORD_LEN) {
         return new IllegalArgumentException("back-end password is too big: " +
            MAX_PASSWORD_LEN + " " + pw.length());
      }

      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param bc a valid PSBackEndCredential.
    */
   public void copyFrom( PSLegacyBackEndCredential bc )
   {
      super.copyFrom((PSComponent) bc );
      // this is effectively a deep copy since Strings are immutable
      m_alias = bc.m_alias;
      m_comment = bc.m_comment;
      m_driver = bc.m_driver;
      m_server = bc.m_server;
      m_userId = bc.m_userId;
      m_password = bc.m_password;
      m_condCollection = bc.m_condCollection;
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXBackEndCredential XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *      PSBackEndCredential defines access credentials for a  back-end
    *      data store. The credentials are then used by the server whenever
    *      accessing the particular back-end.
    *    --&gt;
    *    &lt;!ELEMENT PSXBackEndCredential   (alias, comment?, driver,
    *                                server?, userId?, password?)&gt;
    *
    *    &lt;!--
    *      the alias used to reference this object.
    *    --&gt;
    *    &lt;!ELEMENT alias            (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the comment associated with this back-end credential object.
    *      Comments are limited to 255 characters.
    *    --&gt;
    *    &lt;!ELEMENT comment             (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the back-end driver for which the credentials should be used.
    *    --&gt;
    *    &lt;!ELEMENT driver               (%PSXBackEndProviderType)&gt;
    *
    *    &lt;!--
    *      the back-end server for which the credentials should be used.
    *    --&gt;
    *    &lt;!ELEMENT server               (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the user id to login to the back-end with.
    *    --&gt;
    *    &lt;!ELEMENT userId               (#PCDATA)&gt;
    *
    *    &lt;!--
    *      the password to login to the back-end with.
    *    --&gt;
    *    &lt;!ELEMENT password            (#PCDATA)&gt;
    *
    *    &lt;!--
    *      has the password been stored encrypted?
    *    --&gt;
    *    &lt;!ATTLIST password
    *         encrypted (yes | no)            #IMPLIED&gt;
    * </code></pre>
    *
    * @return      the newly created PSXBackEndCredential XML element node
    */
   public Element toXml(Document   doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //create Alias name object
      PSXmlDocumentBuilder.addElement(   doc, root, "alias", m_alias);

      //create comment object
      PSXmlDocumentBuilder.addElement(   doc, root, "comment", m_comment);

      //create driver object
      PSXmlDocumentBuilder.addElement(   doc, root, "driver", m_driver);

      //create server object
      PSXmlDocumentBuilder.addElement(   doc, root, "server", m_server);

      //create user ID object
      PSXmlDocumentBuilder.addElement(   doc, root, "userId", m_userId);

      //create password object
      Element pwNode = PSXmlDocumentBuilder.addElement(
         doc, root, "password", m_password);
      pwNode.setAttribute("encrypted", "yes");

      //create conditional(s) object
      if (m_condCollection != null) {
         IPSComponent comp;
         Element node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Conditionals");
         int size = m_condCollection.size();
         for (int i = 0; i < size; i++) {
            comp = (IPSComponent)m_condCollection.get(i);
            node.appendChild(comp.toXml(doc));
         }
      }

      return root;
   }

   /**
    * This method is called to populate a PSBackEndCredential Java object
    * from a PSXBackEndCredential XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                               of type PSXBackEndCredential
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      //read Alias name
      try {
         setAlias(tree.getElementData("alias"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "alias",
               new PSException (e.getLocalizedMessage()));
      }

      //read comment from value from node
      try {
         setComment(tree.getElementData("comment"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "comment",
               new PSException (e.getLocalizedMessage()));
      }

      //Read driver name from XML node
      try {
         setDriver(tree.getElementData("driver"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "driver",
               new PSException (e.getLocalizedMessage()));
      }

      //read server name
      try {
         setServer(tree.getElementData("server"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "server",
               new PSException (e.getLocalizedMessage()));
      }

      //read user ID element
      try {
         setUserId(tree.getElementData("userId"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "userId",
               new PSException (e.getLocalizedMessage()));
      }

      // read password element - don't call setPassword as it will
      // try to encrypt the already encrypted password
      Element pwNode = tree.getNextElement("password",
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      if (pwNode == null)
         m_password = "";
      else
      {
         // by default, assume it's encrypted
         m_password = tree.getElementData(".", false);

         // now do the real check and update it if required
         sTemp = tree.getElementData("encrypted", false);
         if ((sTemp == null) || !sTemp.equalsIgnoreCase("yes"))
         {
            try {
               setPassword(m_password);
            } catch (IllegalArgumentException e) {
               throw new PSUnknownNodeTypeException(ms_NodeType, "password",
               new PSException (e.getLocalizedMessage()));
            }
         }
      } // end of reading password element

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // get all the conditionals now
      if (m_condCollection != null)
         m_condCollection.clear();

      if (tree.getNextElement("Conditionals", PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS) != null)
      {
         PSConditional cond;
         String curNodeType = PSConditional.ms_NodeType;
         for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
               curNode != null;
               curNode = tree.getNextElement(curNodeType, nextFlags))
         {
            cond = new PSConditional((Element)tree.getCurrent(), parentDoc, parentComponents);
            m_condCollection.add(cond);
         }
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateAlias(m_alias);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateComment(m_comment);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateUserId(m_userId);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateServer(m_server);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validatePassword(m_password);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateCondCollection(m_condCollection);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer(100);
      buf.append(m_alias + ": " + m_driver + ":" + m_server + ":" + m_userId);
      if (m_comment != null && m_comment.length() > 0)
      {
         buf.append("{" + m_comment + "}");
      }
      return buf.toString();
   }

   /*
    * NOTE: When adding new members, update the copyFrom method!
    */
   private      String              m_alias = "";
   private      String              m_comment = "";
   private      String              m_driver = "";
   private      String              m_server = "";
   private      String              m_userId = "";
   private      String              m_password = "";
   private      PSCollection        m_condCollection = null;

   private static final int         MAX_ALIAS_NAME_LEN   = 128;
   private static final int         MAX_COMMENT_LEN      = 255;
   private static final int         MAX_DRIVER_NAME_LEN   = 128;
   private static final int         MAX_SERVER_NAME_LEN   = 128;
   private static final int         MAX_UID_LEN            = 128;
   private static final int         MAX_PASSWORD_LEN      = 128;

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXBackEndCredential";
}
