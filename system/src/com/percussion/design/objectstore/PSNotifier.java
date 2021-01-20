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

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * The PSNotifier class defines users to be sent e-mail based upon various
 * errors which may occur during request processing by an application.
 *
 * @see PSApplication#getNotifier
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSNotifier extends PSComponent
{
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
   public PSNotifier(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Tells the notifier to use the Simple Mail Transfer Protocol (SMTP)
    * mail provider.
    */
   public static final int MP_TYPE_SMTP = 1;
   

   /**
    * Constructor for serialization, fromXml, etc.
    */
   public PSNotifier() {
      super();
      // get the recipient list
        m_recipients = new PSCollection(
         com.percussion.design.objectstore.PSRecipient.class);
   }
   
   /**
    * Construct a notifier object for sending notification through the
    * specified mail server. The default user name "E2" will be set as
    * the message originator (from).
    *
    * @param   provider      the PSNotifier.MP_TYPE_xxx provider type
    *
    * @param   server      the server to send mail through
    *
    * @exception   PSIllegalArgumentException   if provider or server is invalid
    *
    * @see         #setProviderType
    * @see         #setServer
    */
   public PSNotifier(int provider, java.lang.String server)
      throws PSIllegalArgumentException
   {
      this();
      setProviderType(provider);
      setServer(server);
   }
   
   /**
    * Gets the type of mail provider associated with this object.
    *
    * @return     the mail provider type (PSNotifier.MP_TYPE_xxx)
    */
   public int getProviderType()
   {
      return m_providerType;
   }
   
   /**
    * Sets the type of mail provider associated with this object.
    * The specified mail provider will be used to send any messages.
    *
    * @param provider   the mail provider to use. At this time, only
    *                   PSNotifier.MP_TYPE_SMTP is supported.
    * @exception   PSIllegalArgumentException   if provider is invalid
    */
   public void setProviderType(int provider)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateProviderType(provider);
      if (ex != null)
         throw ex;

      m_providerType = provider;
   }

   private static PSIllegalArgumentException validateProviderType(int provider)
   {
      if (provider != MP_TYPE_SMTP) {
         Object[] args = { new Integer(provider) };
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.NOTIFIER_PROVIDER_TYPE_INVALID, args);
      }

      return null;
   }
   
   /**
    * Gets the host name of the mail server through which messages
    * will be routed.
    *
    * @return     the name of the mail server
    */
   public java.lang.String getServer()
   {
      return m_server;
   }
   
   /**
    * Sets the host name of the mail server through which messages
    * will be routed. This will not be verified as a valid host until
    * run-time.
    * This is limited to 255 characters.
    *
    * @param      name      the name of the mail server
    *
    * @exception   PSIllegalArgumentException      if name is null, empty or
    *                                             exceeds the specified limit
    */
   public void setServer(java.lang.String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateServer(name);
      if (ex != null)
         throw ex;

      m_server = name;
   }
   
   private static PSIllegalArgumentException validateServer(String name)
   {
      if ((null == name) || (name.length() == 0))
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.NOTIFIER_SERVER_NULL);
      else if (name.length() > SERVER_NAME_MAX_LEN) {
         Object[] args = { new Integer(SERVER_NAME_MAX_LEN),
                           new Integer(name.length()) };
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.NOTIFIER_SERVER_TOO_BIG, args);
      }

      return null;
   }

   /**
    * Gets the e-mail address being used as the originator of the
    * mail message.
    *
    * @return     the e-mail address of the mail originator
    */
   public java.lang.String getFrom()
   {
      return m_from;
   }
   
   /**
    * Set the name of the mail originator (from).
    * Sets the e-mail address to use as the originator of the
    * mail message. If an address is not set (it is null or empty),
    * "E2" will be used.
    * This is limited to 255 characters.
    *
    * @param      from       the name of the mail originator
    *
    * @exception   PSIllegalArgumentException 
    *                        if from exceeds the specified limit
    */
   public void setFrom(java.lang.String from)
      throws PSIllegalArgumentException
   {
      if ((null == from) || (from.length() == 0))
         from = DEFAULT_FROM;

      PSIllegalArgumentException ex = validateFrom(from);
      if (ex != null)
         throw ex;

      m_from = from;
   }
   
   private static PSIllegalArgumentException validateFrom(String from)
   {
      if (from.length() > FROM_NAME_MAX_LEN) {
         Object[] args = { new Integer(FROM_NAME_MAX_LEN),
                           new Integer(from.length()) };
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.NOTIFIER_FROM_TOO_BIG, args);
      }

      return null;
   }

   /**
    * Get the recipients of any notifications sent by the application.
    *
    * @return   a collection containing the recipients (PSRecipient objects)
    */
   public com.percussion.util.PSCollection getRecipients()
   {
      return m_recipients;
   }
   
   /**
    * Overwrite the recipients associated with this notifier with the
    * specified collection. If you only want to modify certain recipients,
    * add a new recipient, etc. use getRecipients to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSNotifier object. Any subsequent changes made to the object
    * by the caller will also effect the notifier.
    *
    * @param recipients the new recipients
    *
    * @exception   PSIllegalArgumentException   if the collection contains
    *                                          objects of an incorrect type
    *
    * @see               #getRecipients
    * @see               PSRecipient
    */
   public void setRecipients(com.percussion.util.PSCollection recipients)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateRecipients(recipients);
      if (ex != null)
         throw ex;

      m_recipients = recipients;
   }
   
   
   private static PSIllegalArgumentException validateRecipients(
      PSCollection recipients)
   {
      if (recipients != null) {
         if (!com.percussion.design.objectstore.PSRecipient.class.isAssignableFrom(
            recipients.getMemberClassType())) {
            Object[] args = { "Recipient", "PSRecipient", recipients.getMemberClassName() };
            return new PSIllegalArgumentException(
               IPSObjectStoreErrors.COLL_BAD_CONTENT_TYPE, args);
         }
      }
      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param  notifier a valid PSNotifier. If null, a PSIllegalArgumentException is
    * thrown.
    *
    * @throws PSIllegalArgumentException if notifier is null
    */
   public void copyFrom( PSNotifier notifier )
         throws PSIllegalArgumentException
   {
      copyFrom((PSComponent) notifier );
      // assume object is valid
      m_server = notifier.getServer();
      m_from = notifier.getFrom();
      m_recipients = notifier.getRecipients();
      m_providerType = notifier.getProviderType();
   }

   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXNotifier XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXNotifier defines users to be sent e-mail based upon various
    *       errors which may occur during request processing by an
    *       application.
    *
    *       Object References:
    *
    *       PSXRecipient - the recipients of any notifications sent by the
    *       application.
    *    --&gt;
    *    &lt;!ELEMENT PSXNotifier   (providerType, server, from?, PSXRecipient*)&gt;
    *
    *    &lt;!--
    *       SMTP - use the Simple Mail Transfer Protocol (SMTP) mail
    *       provider.
    *    --&gt;
    *    &lt;!ENTITY % PSXMailProviderType   "(SMTP)"&gt;
    *
    *    &lt;!--
    *       the type of mail provider associated with this object. The
    *       specified mail provider will be used to send any messages.
    *    --&gt;
    *    &lt;!ELEMENT providerType   (%PSXMailProviderType)&gt;
    *
    *    &lt;!--
    *       the host name of the mail server through which messages will be
    *       routed.
    *    --&gt;
    *    &lt;!ELEMENT server         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the e-mail address of the mail originator (from).
    *    --&gt;
    *    &lt;!ELEMENT from          (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXNotifier XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      
      // set provider type
      if (m_providerType == MP_TYPE_SMTP)
         PSXmlDocumentBuilder.addElement(doc, root, "providerType", XML_FLAG_SMTP);
      else
         PSXmlDocumentBuilder.addElement(doc, root, "providerType", "");
      
      // set server name
      PSXmlDocumentBuilder.addElement(doc, root, "server", m_server);
      
      // set from name
      PSXmlDocumentBuilder.addElement(doc, root, "from", m_from);
      
      // and add all recipients
      if (m_recipients != null) {
         IPSComponent recipient;
         int size = m_recipients.size();
         for (int i = 0; i < size; i++) {
            recipient = (IPSComponent)m_recipients.get(i);
            root.appendChild(recipient.toXml(doc));
         }
      }
      
      return root;
   }
   
   /**
    * This method is called to populate a PSNotifier Java object
    * from a PSXNotifier XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXNotifier
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
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
      
         // better be our only supported provider type
         sTemp = tree.getElementData("providerType");
         if (sTemp == null) {
            Object[] args = { ms_NodeType, "providerType", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else if (sTemp.equalsIgnoreCase(XML_FLAG_SMTP))
            m_providerType = MP_TYPE_SMTP;
         else {
            Object[] args = { ms_NodeType, "providerType", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      
         try {      // which server should we send mail through?
            setServer(tree.getElementData("server"));
         } catch (PSIllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "server", e);
         }
      
         try {      // who's this from (not required)
            setFrom(tree.getElementData("from"));
         } catch (PSIllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "from", e);
         }
      
         PSRecipient recipient;
         String curNodeType = PSRecipient.ms_NodeType;
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         m_recipients.clear();
         if (tree.getNextElement(curNodeType, firstFlags) != null){
            do{
               recipient = new PSRecipient(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_recipients.add(recipient);
            } while (tree.getNextElement(curNodeType, nextFlags) != null);
         }

      } finally {
         resetParentList(parentComponents, parentSize);
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

      PSException ex = validateServer(m_server);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      ex = validateFrom(m_from);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());
      
      ex = validateRecipients(m_recipients);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      ex = validateProviderType(m_providerType);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      // do children
      cxt.pushParent(this);

      try
      {
         if (m_recipients == null)
            cxt.validationWarning(this,
            IPSObjectStoreErrors.NOTIFIER_RECIPIENTS_EMPTY, null);
         else
         {
            for (int i = 0; i < m_recipients.size(); i++)
            {
               Object o = m_recipients.get(i);
               PSRecipient recp = (PSRecipient)o;
               recp.validate(cxt);
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSNotifier)) return false;
      if (!super.equals(o)) return false;
      PSNotifier that = (PSNotifier) o;
      return m_providerType == that.m_providerType &&
              Objects.equals(m_server, that.m_server) &&
              Objects.equals(m_from, that.m_from) &&
              Objects.equals(m_recipients, that.m_recipients);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_server, m_from, m_recipients, m_providerType);
   }

   private static final String         DEFAULT_FROM   = "";
   private static final String         XML_FLAG_SMTP   = "SMTP";
   
   private             String                                 m_server = "";
   private             String                                 m_from = DEFAULT_FROM;
   private             com.percussion.util.PSCollection       m_recipients = null;
   private           int                                    m_providerType = MP_TYPE_SMTP;

   private static final int      SERVER_NAME_MAX_LEN   = 255;
   private static final int      FROM_NAME_MAX_LEN      = 255;

   /* public access on this so they may reference each other in fromXml,
    * including legacy classes */
   public static final String   ms_NodeType            = "PSXNotifier";
}

