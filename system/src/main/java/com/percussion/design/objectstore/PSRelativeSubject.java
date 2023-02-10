/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.design.objectstore;

import com.percussion.error.PSDatabaseComponentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * The PSRelativeSubject class defines a subject (group or user) to be stored
 * in some context, including attributes specific to that context for
 * that subject.  This extension of <code>PSSubject</code> is responsible for
 * persisting any relationships it has in its context to the back end database,
 * but is not responsible for storing the actual subject data.
 * See {@link #toXml(Document) toXml} for a more complete definition of this
 * class.
 *
 * @see PSSubject
 *
 * @see PSGlobalSubject
 */
public class PSRelativeSubject extends PSSubject
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
   public PSRelativeSubject(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Set the database component id from the global subject that this
    * relative subject corresponds to.
    *
    * @param id The global subject's database component id.
    */
   void setDbId(int id)
   {
      m_databaseComponentId = id;
   }

   /**
    * Make a new global subject from this relative subject.
    *
    * @return A new subject with all of the same internal properties,
    *    but with no attributes.  If sub is <code>null</code> this
    *    call returns <code>null</code>.
    *
    * @throws IllegalArgumentException If the construction fails.
    */
   PSGlobalSubject makeGlobalSubject()
   {
      return new PSGlobalSubject(getName(), m_type, null);
   }

   /**
    * Construct an empty relative subject (for synchronization, etc.).
    *
    * @see PSSubject#PSSubject() for more information.
    */
   PSRelativeSubject()
   {
      super();
   }

   /**
    * Construct a complete relative subject.
    *
    * @see PSSubject#PSSubject(String, int, PSAttributeList) for more
    *    information.
    */
   public PSRelativeSubject(String name, int type, PSAttributeList atts)
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
    *
    * No Action Elements are created directly by this object.
    *
    * See {@link PSDatabaseComponent#toDatabaseXml} for information
    * about this methods parameters and exceptions.  Differences are noted
    * below.
    *
    * @param relationContext This is the relation context to use to persist
    * this object's attributes.  May not be <code>null</code>.
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null || relationContext == null)
         throw new IllegalArgumentException("one or more params is null");

      // now add our relation to our parent
      relationContext.addKey(getComponentType(), m_databaseComponentId);
      relationContext.m_componentState = m_componentState;
      relationContext.toDatabaseXml(doc, actionRoot, relationContext);

      // write out our attributes
      PSRelation myCtx = (PSRelation)relationContext.clone();
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


