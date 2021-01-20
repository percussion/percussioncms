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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;


/**
 * The PSDataEncryptor class defines how data should be encrypted when
 * communicating with a client. Data can be sent in the clear or using a
 * Secure Socket Layer (SSL) connection. When using SSL, the required
 * key strength can also be defined. The key strength is the number of bits
 * in the key. The most common algorithms use 40-bit or 128-bit keys, where
 * 128-bit keys are significantly more secure than 40-bit keys.
 * <p>
 * The lowest level object the PSDataEncryptor object is associated with
 * overrides any settings of a higher level object. The hierarchy is as
 * follows:
 * <ul>
 * <li>Server</li>
 * <li>Application (overrides Server)</li>
 * <li>Data Set (overrides Application)</li>
 *
 * @see PSDataSet#getDataEncryptor
 * @see PSApplication#getDataEncryptor
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataEncryptor extends PSComponent
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
   public PSDataEncryptor(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Default constructor for fromXml, serialization, etc.
    */
   PSDataEncryptor()
   {
      this(DEFAULT_KEY_STRENGTH);
   }

   /**
    * Constructs a data encryptor object requiring SSL connections using the
    * specified key strength.
    *
    * @param   keyStrength    the minimum number of bits required in the key
    */
   public PSDataEncryptor(int keyStrength)
   {
      super();
      m_keyStrength = keyStrength;
      m_encryption = true;
   }

   /**
    * Constructs a data encryptor object. If require is true, SSL connections
    * are required using the default key strength of 40 bits. Otherwise,
    * SSL connections are not required.
    *
    * @param   require   <code>true</code> to require SSL connections,
    *                   <code>false</code> otherwise
    */
   public PSDataEncryptor(boolean require)
   {
      super();
      m_keyStrength = DEFAULT_KEY_STRENGTH;
      m_encryption = require;
   }

   /**
    * Is an SSL connection required to secure the data being transferred?
    *
    * @return      <code>true</code> if SSL is required,
    *             <code>false</code> otherwise
    */
   public boolean isSSLRequired()
   {
      return m_encryption;
   }

   /**
    * Enable or disable requiring SSL connections to secure the data
    * being transferred. If SSL is being required and a key strength
    * was not previously set, the default key strength of 40 bits
    * will be used.
    *
    * @param   enable   <code>true</code> to require SSL connections,
    *                   <code>false</code> otherwise
    */
   public void setSSLRequired(boolean enable)
   {
      m_encryption = enable;
   }

   /**
    * Get the minimum SSL key strength which must be used for data
    * encryption. Key strengths are specified by their size in bits.
    * The most common algorithms use 40-bit or 128-bit keys.
    *
    * @return      the minimum number of bits in the key
    */
   public int getKeyStrength()
   {
      return m_keyStrength;
   }

   /**
    * Set the minimum SSL key strength which must be used for data
    * encryption. Key strengths are specified by their size in bits.
    * The most common algorithms use 40-bit or 128-bit keys.
    *
    * @param   bits   the minimum number of bits required in the key
    */
   public void setKeyStrength(int bits)
   {
      m_keyStrength = bits;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param encryptor a valid PSDataEncryptor.
    *
    */
   public void copyFrom( PSDataEncryptor encryptor )
   {
      copyFrom((PSComponent) encryptor );
      setKeyStrength( encryptor.getKeyStrength());
      setSSLRequired( encryptor.isSSLRequired());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDataEncryptor)) return false;
      if (!super.equals(o)) return false;
      PSDataEncryptor that = (PSDataEncryptor) o;
      return m_keyStrength == that.m_keyStrength &&
              m_encryption == that.m_encryption;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_keyStrength, m_encryption);
   }
   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataEncryptor XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataEncryptor defines how data should be encrypted when
    *       communicating with a client. Data can be sent in the clear or
    *       using a Secure Socket Layer (SSL) connection. When using SSL,
    *       the required key strength can also be defined. The key strength
    *       is the number of bits in the key. The most common algorithms use
    *       40-bit or 128-bit keys, where 128-bit keys are significantly more
    *       secure than 40-bit keys.
    *
    *       The lowest level object the PSDataEncryptor object is associated
    *       with overrides any settings of a higher level object. The
    *       hierarchy is as follows:
    *
    *          - PSXServer
    *          - PSXApplication (overrides PSXServer)
    *          - PSXDataSet (overrides PSXApplication)
    *    --&gt;
    *    &lt;!ELEMENT PSXDataEncryptor (SSLRequired, keyStrength?)&gt;
    *
    *    &lt;!--
    *       is an SSL connection required to secure the data being transferred?
    *    --&gt;
    *    &lt;!ELEMENT SSLRequired      (%PSXIsEnabled)&gt;
    *
    *    &lt;!--
    *       the minimum SSL key strength which must be used for data
    *       encryption. Key strengths are specified by their size in bits.
    *       The most common algorithms use 40-bit or 128-bit keys.
    *    --&gt;
    *    &lt;!ELEMENT keyStrength      (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataEncryptor XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private              boolean     m_encryption = false;
      PSXmlDocumentBuilder.addElement(   doc, root, "SSLRequired",
         m_encryption ? "yes" : "no");

      //private              int         m_keyStrength = 40;
      PSXmlDocumentBuilder.addElement(   doc, root, "keyStrength",
         String.valueOf(m_keyStrength));

      return root;
   }

   /**
    * This method is called to populate a PSDataEncryptor Java object
    * from a PSXDataEncryptor XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataEncryptor
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

      // is SSL (encryption) required?
      sTemp = tree.getElementData("SSLRequired");
      m_encryption = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

      // what's the key strength?
      sTemp = tree.getElementData("keyStrength");
      if ((sTemp == null) && m_encryption) {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.DATAENC_KEY_STRENGTH_REQD);
      }
      else if (sTemp != null) {
         try {
            m_keyStrength = Integer.parseInt(sTemp);
         } catch (NumberFormatException e) {
            Object[] args = { ms_NodeType, "keyStrength", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValiditionException, but the
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

      if (m_encryption && m_keyStrength <= 0)
         cxt.validationError(this, 0, "Invalid key strength: " + m_keyStrength);
   }

   private static final int         DEFAULT_KEY_STRENGTH = 40;


   // NOTE: when adding members, be sure to update the copyFrom method,
   // the validate method, the equals method, and the to/fromXml method
   private              int         m_keyStrength;
   private              boolean     m_encryption;

   /* public access on this so they may reference each other in fromXml,
    * including legacy classes */
   public static final String   ms_NodeType            = "PSXDataEncryptor";
}

