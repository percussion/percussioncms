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
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;


/**
 * This class is used to specify the relationship a class has to its parent.
 * This relationship identifies a row in a relationship table in the database.
 */
public class PSRelation extends PSDatabaseComponent
   implements Cloneable
{
   /**
    * Construct a new, empty relation object.
    */
   public PSRelation()
   {
   }

   /**
    * Construct a new PSRelation with a name and a
    * value.  This is not a real relation until a second
    * key and value are present.
    *
    * @param keyName The name used to identify the first
    * piece of relation context, can't be <code>null</code> or
    * empty.
    *
    * @param keyValue The value used to identify the specific
    * id of the first piece of relation context, can't be <code>null</code>.
    *
    * @throws IllegalArgumentException if keyName is <code>null</code>
    * or empty.
    */
   public PSRelation(String keyName, String keyValue)
   {
      if (keyName == null || keyName.trim().length() == 0)
         throw new IllegalArgumentException("keyName may not be null or empty");

      if (keyValue == null || keyValue.trim().length() == 0)
         throw new IllegalArgumentException("keyValue may not be null or empty");

      if (keyValue == null)
         keyValue = "";

      addKey(keyName, keyValue);
   }

   /**
    * Convenience constructor that takes an integer for the keyValue instead
    * of a String.  See {@link #PSRelation(String, String) this} for more info.
    */
   public PSRelation(String keyName, int keyValue)
   {
      this(keyName, String.valueOf(keyValue));
   }
   
   /**
    * Private constructor, used for {@link #clone() clone} operation.
    */
   private PSRelation(PSCollection keyNameCollection,
      PSCollection keyValueCollection)
   {
      m_keyNames.addAll(keyNameCollection);
      m_keyValues.addAll(keyValueCollection);
   }

   /**
    * Make a new deep copy of this relation.
    */
   public Object clone()
   {
      PSRelation copy = new PSRelation(new PSCollection(m_keyNames.iterator()),
         new PSCollection(m_keyValues.iterator()));
         
      copy.m_componentState = m_componentState;
      copy.m_databaseComponentId = m_databaseComponentId;
      copy.m_id = m_id;

      return copy;
   }

   /**
    * Add another piece of relation context information.  Components
    * making relations should add their role component type name as
    * their key for relations.
    *
    * @param keyName The name used to identify the first
    * piece of relation context, can't be <code>null</code> or
    * empty.
    *
    * @param keyValue The value used to identify the specific 
    * id of the first piece of relation context, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if keyName is <code>null</code>
    * or empty.
    */
   public void addKey(String keyName, String keyValue)
   {
      if ((keyName == null) || (keyName.length() == 0))
         throw new IllegalArgumentException("Key name must be supplied.");

      if ((keyValue == null) || (keyValue.length() == 0))
         throw new IllegalArgumentException("Key value must be supplied.");
      
      m_keyNames.add(keyName);
      m_keyValues.add(keyValue);
   }

   /**
    * Convenience method to add a key using an integer for the keyValue.
    * Converts the keyValue to a String, and calls {@link #addKey(String,
    * String)}.
    */
   public void addKey(String keyName, int keyValue)
   {
      addKey(keyName, String.valueOf(keyValue));
   }


   /**
    * Gets the value of the specified key as an integer.
    *
    * @param keyName The name of the key.  May not be <code>null</code> or
    * empty.
    *
    * @return The value as an integer.
    *
    * @throws IllegalArgumentException if keyName is <code>null</code>, empty,
    * or if the specified keyName is not found in this relation.
    *
    * @throws NumberFormatException if the specified key's value cannot be
    * converted to an integer.
    */
   public int getIntValue(String keyName)
   {
      return Integer.parseInt(getValue(keyName));
   }

   /**
    * Gets the value of the specified key as an String.
    *
    * @param keyName The name of the key.  May not be <code>null</code> or
    * empty.
    *
    * @return The value as a String, if <code>null</code>, the key was
    * not found.
    */
   public String getValue(String keyName)
   {
      if (keyName == null || keyName.trim().length() == 0)
         throw new IllegalArgumentException("keyName may not be null or empty");

      String keyValue = null;

      for (int i=0; i < m_keyNames.size(); i++)
      {
         if (((String)m_keyNames.get(i)).equals(keyName))
         {
            keyValue = (String)m_keyValues.get(i);
            break;
         }
      }

      return keyValue;
   }

   /**
    * Get the relation type of this object.
    *
    * @return The relation type, a name containing all key name information,
    * which can be used to create an appropriate update action element for
    * serialization with the back-end.
    */
   public String getRelationType()
   {
      String relationType = "PSX";
      Iterator i = m_keyNames.iterator();
      while (i.hasNext())
      {
         relationType += i.next().toString();
      }
      relationType += "Relation";

      return relationType;
   }

   /**
    * Create an action element to insert or delete a relation in the back
    * end, if indicated.  The element root is a concatenation of each key's
    * name.  Each key is added as a child of the element root, with their value
    * as the element value.  For example, if there are two keys, "Key1" and
    * "Key2", (using a root element called 'root') is:
    *
    * <pre><code>
    * &lt;!ELEMENT root (Action*)&gt;
    * &lt;!ELEMENT Action (PSXKey1Key2Relation)
    * &lt;!ATTLIST Action
    * type (INSERT | UPDATE | DELETE) #REQUIRED
    * &gt;
    * </code></pre>
    *
    *
    * @see #toXml(Document) for more info on the xml structure
    * @see PSDatabaseComponent#toDatabaseXml(Document, Element, PSRelation) 
    * for more information.
    */
   public void toDatabaseXml(Document doc, Element actionRoot,
      PSRelation relation)  throws PSDatabaseComponentException
   {
      Element actionEl = getActionElement(doc, actionRoot);

      if (actionEl != null)
      {
         actionEl.appendChild(toXml(doc));
      }
   }

   /**
    * Does this relation match the relation context provided?
    *
    * @param relationContext The relation context to match against.
    * May not be <code>null</code>.
    *
    * @return <code>true</code> if it does, or <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if relationContext is <code>null</code>
    */
   public boolean isMatch(PSRelation relationContext)
   {
      if (relationContext == null)
         throw new IllegalArgumentException("A relation context must be provided.");

      if (m_keyNames.size() == (relationContext.m_keyNames.size() + 1) )
      {
         for (int i = 0; i < relationContext.m_keyNames.size(); i++)
         {
            if (!m_keyNames.get(i).equals(relationContext.m_keyNames.get(i)))
               return false;
            if (!m_keyValues.get(i).equals(relationContext.m_keyValues.get(i)))
               return false;
         }
      } else
      {
         return false;
      }

      return true;
   }
   
   /**
    * This method is called to create an  XML element
    * node containing the data described in this object.
    * <p>
    * The element root is a concatenation of each key's
    * name.  Each key is added as a child of the element root, with their value
    * as the element value.  For example, if there are two keys, "Key1" and
    * "Key2", (using a
    * root element called 'root') is:
    * <pre><code>
    * &lt;!ELEMENT PSXKey1Key2Relation (Key1, Key2)&gt;
    * &lt;!ELEMENT Key1 (#PCDATA)&gt;
    * &lt;!ELEMENT Key2 (#PCDATA)&gt;
    * </code></pre>
    *
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(getRelationType());

      // add our db state
      addComponentState(root);

      for (int i = 0; i < m_keyNames.size(); i++)
      {
         PSXmlDocumentBuilder.addElement(doc, root,
            m_keyNames.get(i).toString(), m_keyValues.get(i).toString());
      }

      return root;
   }

   /**
    * This method is called to populate a PSRelation Java object from
    * its XML element node.  A key will be created from each element immediately
    * below the supplied sourceNode, with the element name as the key name and
    * the element's value as it's value.  See {@link #toXml(Document) toXml} for
    * more information on the Xml structure.
    *
    * @throws  PSUnknownNodeTypeException if the XML element node does not
    *          contain at least two elements each with (#PCDATA) as their value.
    */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, "[PSX???Relation]");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element key = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      if (key == null)
      {
         Object[] args = {sourceNode.getTagName(), "[a key element]", "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      while (key != null)
      {
         addKey(key.getTagName(), tree.getElementData(key));
         key = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }
   
   /**
    * Override {@link PSDatabaseComponent#getActionElement(Document, Element)}
    * since relations are not updated unless an insert or delete is being
    * performed, updates to objects do not change the relations between
    * them.
    */
   protected Element getActionElement(Document doc, Element actionRoot)
   {
      Element actionElement = null;
      if (m_componentState != DATABASE_COMPONENT_UPDATED)
      {
         actionElement = super.getActionElement(doc, actionRoot);
      }
      return actionElement;
   }

   /**
    * See {@link PSDatabaseComponent#getDatabaseAppQueryDatasetName} for more
    * info.
    *
    * @throws IllegalStateException if this relation contains no keys.
    */
   public String getDatabaseAppQueryDatasetName()
   {
      if (m_keyNames.size() == 0)
         throw new IllegalStateException("This relation contains no keys");

      String resourceName = "";
      for (int i = 0; i < m_keyNames.size(); i++)
         resourceName += m_keyNames.get(i);
      resourceName += "Relation";
      
      return resourceName;
   }
   
   /**
    *
    */
   public String getDatabaseAppQueryDatasetName(String componentName)
   {
      if (m_keyNames.size() == 0)
         throw new IllegalStateException("This relation contains no keys");

      StringBuilder datasetName = new StringBuilder();
      for (int i = 0; i < m_keyNames.size(); i++)
         datasetName.append( m_keyNames.get(i));

      datasetName.append( componentName );
      datasetName.append( "Relation" );

      return buildAppQueryDatasetName( datasetName.toString());
   }

   /**
    * The collection of key names for this relation.
    * Initialized to an empty String collection, never empty or
    * <code>null</code> after construction.
    */
   PSCollection m_keyNames = new PSCollection(String.class);

   
   /**
    * The collection of key values for this relation.
    * Initialized to an empty String collection, never empty or
    * <code>null</code> after construction.
    */
   PSCollection m_keyValues = new PSCollection(String.class);
}
