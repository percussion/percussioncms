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

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSGlobalSubject class defines a subject (group or user) to be stored
 * in the global context.  This extension of <code>PSSubject</code> is
 * responsible for persisting the subject data to the back end database.
 * See {@link #toXml(Document) toXml} for a more complete definition
 * of this class.
 *
 * @see PSSubject
 */
public class PSGlobalSubject extends PSSubject
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from
    *
    * @param   parentDoc       the Java object which is the parent of this
    *                             object
    *
    * @param   parentComponents  the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    */
   public PSGlobalSubject(Element sourceNode,
      IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Make a new relative subject from this global subject.
    *
    * @return A new subject with all of the same internal properties,
    *    but with no attributes.
    */
   public PSRelativeSubject makeRelativeSubject()
   {
      return new PSRelativeSubject(getName(), m_type, null);
   }

   /**
    * Construct an empty subject (for synchronization, etc.) .
    *
    * @see PSSubject#PSSubject() for
    * more information.
    */
   public PSGlobalSubject()
   {
      super();
   }

   /**
    * Construct a complete relative subject.
    *
    * @see PSSubject#PSSubject(String, int, PSAttributeList) for more
    *    information.
    */
   public PSGlobalSubject(String name, int type, PSAttributeList atts)
   {
      super(name, type, atts);
   }

   /**
    * This method is called to create one or more Action XML elements
    * containing the data described in this object that is used to update
    * the database. The Elements are appended to the root of the passed in doc.
    * This method then calls the <code>toDatabaseXml</code> method on any of
    * this object's children.
    * <p>
    * The structure of the XML element(s) appended to the document (using a
    * root element called 'root') is:
    * <pre><code>
    *  &lt;!ELEMENT root (Action*)&gt;
    *  &lt;!ELEMENT Action (PSXSubject)&gt;
    *  &lt;!ATTLIST Action
    *     type (INSERT | UPDATE | DELETE) #REQUIRED
    *  &gt;
    *  &lt;!ELEMENT PSXSubject (name, securityProviderType?,
    *                                           securityProviderInstance?)&gt;
    *  &lt;!ATTLIST PSXSubject
    *     id CDATA #REQUIRED
    *     DbComponentId CDATA #REQUIRED
    *     type    %PSXSubjectType   #REQUIRED
    *  &gt;
    *  &lt;!ENTITY % PSXSubjectType "(user, group)"&gt;
    *  &lt;!--
    *     the name of the user or group associated with this subject.
    *  --&gt;
    *  &lt;!ELEMENT name                   (#PCDATA)&gt;
    *
    *  &lt;!--
    *     the type of security provider associated with this subject.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderType   (#PCDATA)&gt;
    *
    *  &lt;!--
    *     the security provider associated with this subject.
    *     Since there may be several instances of a given security
    *     provider (eg, multiple LDAP servers), specifying the instance to
    *     uniquely identify a subject may be necessary.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderInstance  (#PCDATA)&gt;
    * </code></pre>
    *
    * see {@link PSDatabaseComponent#toDatabaseXml} for information
    * about this methods parameters and exceptions.
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext)  throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null || relationContext == null)
         throw new IllegalArgumentException("one or more params is  null");

      // As a global subject, we'll ignore any relationContext passed.

      // if we are new, generate a new id
      if (isInsert())
         createDBComponentId();

      // Add action element to root
      Element actionElement = getActionElement(doc, actionRoot);
      if (actionElement != null)
      {
         // just toXml ourselves to this root - any extra data will be ignored
         actionElement.appendChild(toXml(doc, false));
      }

      // now add our attributes
      PSRelation myCtx = (PSRelation)relationContext.clone();
      myCtx.addKey(getComponentType(), m_databaseComponentId);

      m_attributes.toDatabaseXml(doc, actionRoot, myCtx);
   }

   /**
    * We must override this method because we are actually saved as the
    * type of our base class in the database.
    *
    * @return Always 'Subject'.
    */
   public String getComponentType()
   {
      return "Subject";
   }
}


