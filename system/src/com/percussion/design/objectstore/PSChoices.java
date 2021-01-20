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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implements the PSXChoices DTD in BasicObjects.dtd.
 */
public class PSChoices extends PSComponent
{
   /**
    * Create new choices of type global.
    *
    * @param global the identifier of the global choices,must be greater
    *    than 0.
    */
   public PSChoices(int global)
   {
      setGlobal(global);
   }

   /**
    * Create new choices of type local.
    *
    * @param local a collection of PSEntry objects, never <code>null</code>,
    *    might be empty.
    */
   public PSChoices(PSCollection local)
   {
      setLocal(local);
   }

   /**
    * Create new choices of type lookup.
    *
    * @param lookup the lookup request, never <code>null</code>.
    * @param type the lookup type to create, can only be TYPE_LOOKUP or
    *    TYPE_INTERNAL_LOOKUP.
    */
   public PSChoices(PSUrlRequest lookup, int type)
   {
      setLookup(lookup, type);
   }
   
   /**
    * Create new choices of type table info.
    *
    * @param tableinfo object of PSChoiceTableInfo, never <code>null</code>.
    */
   public PSChoices(PSChoiceTableInfo tableinfo)
   {
      setTableInfo(tableinfo);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSChoices(Element sourceNode, IPSDocument parentDoc,
                    ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   public Object clone()
   {
      PSChoices copy = (PSChoices) super.clone();

      copy.m_defaultSelected = new PSCollection( PSDefaultSelected.class );
      for (int i = 0; i < m_defaultSelected.size(); i++)
      {
         PSDefaultSelected defaultSelected =
            (PSDefaultSelected) m_defaultSelected.elementAt( i );
         copy.m_defaultSelected.add( i, defaultSelected.clone() );
      }

      copy.m_local = new PSCollection( PSEntry.class );
      for (int i = 0; i < m_local.size(); i++)
      {
         PSEntry entry = (PSEntry) m_local.elementAt( i );
         copy.m_local.add( i, entry.clone() );
      }

      if (m_lookup != null)
         copy.m_lookup = (PSUrlRequest) m_lookup.clone();
      if (m_nullEntry != null)
         copy.m_nullEntry = (PSNullEntry) m_nullEntry.clone();

      if (m_choiceFilter != null)
         copy.m_choiceFilter = (PSChoiceFilter)m_choiceFilter.clone();


      return copy;
   }


   /**
    * Get the type of this choice.
    *
    * @return the type of this choice (gobal | local | lookup | tableinfo).
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Get the sort order.
    *
    * @return the sort order (ascending | descending | user).
    */
   public int getSortOrder()
   {
      return m_sortOrder;
   }

   /**
    * Set a sort order.
    *
    * @param sortOrder the new sort order (ascending | descending | user).
    */
   public void setSortOrder(int sortOrder)
   {
      if (sortOrder != SORT_ORDER_ASCENDING &&
          sortOrder != SORT_ORDER_DESCENDING &&
          sortOrder != SORT_ORDER_USER)
         throw new IllegalArgumentException("unkown sort order");

      m_sortOrder = sortOrder;
   }

   /**
    * Get the global choices, only valid if type is 'global'.
    *
    * @return the global choice table ID. Returns -1 if not valid.
    */
   public int getGlobal()
   {
      return m_global;
   }

   /**
    * Set a global choice reference. This also resets the type to 'global'.
    *
    * @param global the new global choice table ID.
    */
   public void setGlobal(int global)
   {
      if (global < 0)
         throw new IllegalArgumentException("invalid global choice ID");

      m_global = global;
      m_type = TYPE_GLOBAL;
   }

   /**
    * Get the list of local choices, only used if type is 'local'.
    *
    * @return the local choices, never <code>null</code>, might
    *    be empty.
    */
   public Iterator getLocal()
   {
      return m_local.iterator();
   }

   /**
    * Set the local choices. This also resets the type to 'local'.
    *
    * @param local a collection of PSEntry objects, not <code>null</code>
    *    might be empty.
    */
   public void setLocal(PSCollection local)
   {
      if (local == null)
         throw new IllegalArgumentException("the collection cannot be null");

      if (!local.getMemberClassName().equals(m_local.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSEntry collection expected");

      m_local.clear();
      m_local.addAll(local);
      m_type = TYPE_LOCAL;
   }

   /**
    * Get the lookup request, only used if type is 'lookup'.
    *
    * @return the lookup request, might be <code>null</code>.
    */
   public PSUrlRequest getLookup()
   {
      return m_lookup;
   }

   /**
    * Set a new lookup request. This alse resets the type to 'lookup'.
    *
    * @param lookup the new lookup request, might be <code>null</code>.
    * @param type the lookup type to create, can only be TYPE_LOOKUP or
    *    TYPE_INTERNAL_LOOKUP.
    */
   public void setLookup(PSUrlRequest lookup, int type)
   {
      if (lookup == null)
         throw new IllegalArgumentException("lookup cannot be null");
      if (type != TYPE_LOOKUP &&
          type != TYPE_INTERNAL_LOOKUP)
         throw new IllegalArgumentException(
            "type must be TYPE_LOOKUP or TYPE_INTERNAL_LOOKUP");

      m_lookup = lookup;
      m_type = type;
   }

   /**
    * Get the PSChoiceTableInfo object, only valid if type is 'tableinfo'.
    *
    * @return the PSChoiceTableInfo choice object. might be <code>null</code>.
    */
   public PSChoiceTableInfo getTableInfo()
   {
      return m_tableInfo;
   }

   /**
    * Set table info object. This also resets the type to 'tableinfo'.
    *
    * @param tableinfo the new PSChoiceTableInfo object.
    */
   public void setTableInfo(PSChoiceTableInfo tableinfo)
   {
      if (tableinfo == null)
         throw new IllegalArgumentException("tableinfo cannot be null");

      m_tableInfo = tableinfo;
      m_type = TYPE_TABLE_INFO;
   }

   /**
    * Get the null entry.
    *
    * @return the null entry, might be <code>null</code>.
    */
   public PSNullEntry getNullEntry()
   {
      return m_nullEntry;
   }

   /**
    * Set a new null entry.
    *
    * @param nullEntry the new null entry, might be <code>null</code>.
    */
   public void setNullEntry(PSNullEntry nullEntry)
   {
      m_nullEntry = nullEntry;
   }

   /**
    * Get a list of default selected entries.
    *
    * @return the list of default selected entries
    *    (PSDefaultSelected objects), never <code>null</code>, might be empty.
    */
   public Iterator getDefaultSelected()
   {
      return m_defaultSelected.iterator();
   }


   /**
    * @return an OPTIONAL ChoiceFilter, may be <code>null</code>.
    */
   public PSChoiceFilter getChoiceFilter()
   {
      return m_choiceFilter;
   }

   /**
    * Set new choiceFilter.
    *
    * @param choiceFilter may be <code>null</code>.
    */
   public void setChoiceFilter(PSChoiceFilter choiceFilter)
   {
      m_choiceFilter = choiceFilter;
   }

   /**
    * Set new default selected entries.
    *
    * @param defaultSelected a collection of PSDefaultSelected objects, might
    *    be <code>null</code> or empty.
    * @throws IllegalArgumentExcption if the provided collection is of wrong
    *    objects types.
    */
   public void setDefaultSelected(PSCollection defaultSelected)
   {
      if (defaultSelected != null &&
          !defaultSelected.getMemberClassName().equals(
          m_defaultSelected.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSDefaultSelected collection expected");

      m_defaultSelected.clear();
      if (defaultSelected != null)
         m_defaultSelected.addAll(defaultSelected);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSChoices, not <code>null</code>.
    */
   public void copyFrom(PSChoices c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      setDefaultSelected(c.m_defaultSelected);
      m_global = c.getGlobal();
      m_local = c.m_local;
      m_lookup = c.getLookup();
      m_tableInfo = c.getTableInfo();
      setNullEntry(c.getNullEntry());
      setSortOrder(c.getSortOrder());
      m_type = c.getType();
      m_choiceFilter = c.getChoiceFilter();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSChoices)) return false;
      if (!super.equals(o)) return false;
      PSChoices psChoices = (PSChoices) o;
      return m_type == psChoices.m_type &&
              m_sortOrder == psChoices.m_sortOrder &&
              m_global == psChoices.m_global &&
              Objects.equals(m_local, psChoices.m_local) &&
              Objects.equals(m_tableInfo, psChoices.m_tableInfo) &&
              Objects.equals(m_lookup, psChoices.m_lookup) &&
              Objects.equals(m_nullEntry, psChoices.m_nullEntry) &&
              Objects.equals(m_defaultSelected, psChoices.m_defaultSelected) &&
              Objects.equals(m_choiceFilter, psChoices.m_choiceFilter);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_type, m_sortOrder, m_global, m_local, m_tableInfo, m_lookup, m_nullEntry, m_defaultSelected, m_choiceFilter);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
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

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the type attribute
         data = tree.getElementData(TYPE_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<TYPE_ENUM.length; i++)
            {
               if (TYPE_ENUM[i].equalsIgnoreCase(data))
               {
                  m_type = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  TYPE_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // OPTIONAL: get the sort order attribute
         data = tree.getElementData(SORT_ORDER_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<SORT_ORDER_ENUM.length; i++)
            {
               if (SORT_ORDER_ENUM[i].equalsIgnoreCase(data))
               {
                  m_sortOrder = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  SORT_ORDER_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // REQUIRED: get the choice list depending on type
         if (m_type == TYPE_GLOBAL)
         {
            String key = null;
            node = tree.getNextElement(CHOICE_LIST_KEY_ELEM, firstFlags);
            if (node != null)
               key = tree.getElementData(node);

            try
            {
               m_global = Integer.parseInt(key);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  CHOICE_LIST_KEY_ELEM,
                  key == null ? "null" : key
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }
         else if (m_type == TYPE_LOCAL)
         {
            node = tree.getNextElement(PSEntry.XML_NODE_NAME, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSEntry.XML_NODE_NAME,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            Node current = tree.getCurrent();
            while(node != null)
            {
               m_local.add(new PSEntry(node, parentDoc, parentComponents));
               node = tree.getNextElement(PSEntry.XML_NODE_NAME, nextFlags);
            }
            tree.setCurrent(current);
         }
         else if (m_type == TYPE_LOOKUP || m_type == TYPE_INTERNAL_LOOKUP)
         {
            node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSUrlRequest.XML_NODE_NAME,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_lookup = new PSUrlRequest(node, parentDoc, parentComponents);
         }
         else if (m_type == TYPE_TABLE_INFO)
         {
            node = tree.getNextElement(PSChoiceTableInfo.XML_NODE_NAME, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSChoiceTableInfo.XML_NODE_NAME,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_tableInfo = new PSChoiceTableInfo(node, parentDoc, parentComponents);
         }

         // OPTIONAL: get the nullEntry
         node = tree.getNextElement(PSNullEntry.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_nullEntry = new PSNullEntry(node, parentDoc, parentComponents);

         // OPTIONAL: get the defaultSelected
         node = tree.getNextElement(DEFAULT_SELECTED_ELEM, nextFlags);
         if (node != null)
         {
            node = tree.getNextElement(
               PSDefaultSelected.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               m_defaultSelected.add(
                  new PSDefaultSelected(node, parentDoc, parentComponents));
               node = tree.getNextElement(
                  PSDefaultSelected.XML_NODE_NAME, nextFlags);
            }
         }

         //get OPTIONAL PSXChoiceFilter
         node = tree.getNextElement(PSChoiceFilter.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_choiceFilter =
               new PSChoiceFilter(node, parentDoc, parentComponents);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(TYPE_ATTR, TYPE_ENUM[m_type]);
      root.setAttribute(SORT_ORDER_ATTR, SORT_ORDER_ENUM[m_sortOrder]);

      // REQUIRED: add the choice list depending on the type
      if (m_type == TYPE_GLOBAL)
      {
         PSXmlDocumentBuilder.addElement(
            doc, root, CHOICE_LIST_KEY_ELEM, Integer.toString(m_global));
      }
      else if (m_type == TYPE_LOCAL)
      {
         Iterator it = getLocal();
         while (it.hasNext())
            root.appendChild(((IPSComponent) it.next()).toXml(doc));
      }
      else if(m_type == TYPE_TABLE_INFO)
      {
         root.appendChild(m_tableInfo.toXml(doc));
      }
      else // must be a lookup
      {
         root.appendChild(m_lookup.toXml(doc));
      }

      // create the null entry
      if (m_nullEntry != null)
         root.appendChild(m_nullEntry.toXml(doc));

      // create the default selected entries
      if (!m_defaultSelected.isEmpty())
      {
         Element elem = doc.createElement(DEFAULT_SELECTED_ELEM);
         Iterator it = getDefaultSelected();
         while (it.hasNext())
            elem.appendChild(((IPSComponent) it.next()).toXml(doc));
         root.appendChild(elem);
      }

      //create OPTIONAL choice filter
      if (m_choiceFilter != null)
         root.appendChild(m_choiceFilter.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_type != TYPE_GLOBAL &&
          m_type != TYPE_LOCAL &&
          m_type != TYPE_LOOKUP &&
          m_type != TYPE_INTERNAL_LOOKUP &&
          m_type != TYPE_TABLE_INFO)
      {
         Object[] args = { TYPE_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_CHOICE_TYPE, args);
      }

      if (m_sortOrder != SORT_ORDER_ASCENDING &&
          m_sortOrder != SORT_ORDER_DESCENDING &&
          m_sortOrder != SORT_ORDER_USER)
      {
         Object[] args = { SORT_ORDER_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_SORT_ORDER, args);
      }

      // do children
      context.pushParent(this);
      try
      {
         if (m_type == TYPE_GLOBAL)
         {
            if (m_global < 0)
            {
               Object[] args = { Integer.toString(m_global) };
               context.validationError(this,
                  IPSObjectStoreErrors.INVALID_GLOBAL_TABLE_ID, args);
            }
         }
         else if (m_type == TYPE_LOCAL)
         {
            if (m_local == null || m_local.isEmpty())
            {
               context.validationError(this,
                  IPSObjectStoreErrors.LOCAL_CHOICES_NULL_OR_EMPTY, null);
            }
            else
            {
               Iterator it = getLocal();
               while (it.hasNext())
                  ((PSEntry) it.next()).validate(context);
            }
         }
         else if(m_type == TYPE_TABLE_INFO)
         {
            if(m_tableInfo == null)
            {
               context.validationError(this,
                     IPSObjectStoreErrors.LOOKUP_TABLE_INFO_NULL, null);
            }
         }
         else
         {
            if (m_lookup == null)
            {
               context.validationError(this,
                  IPSObjectStoreErrors.LOOKUP_CHOICES_NULL, null);
            }
            else
               m_lookup.validate(context);
         }

         if (m_nullEntry != null)
            m_nullEntry.validate(context);

         Iterator it = getDefaultSelected();
         while (it.hasNext())
            ((PSEntry) it.next()).validate(context);

         //OPTIONAL choice filter
         if (m_choiceFilter!=null)
            m_choiceFilter.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXChoices";

   /**
    * Global type specifier. This type means the choices are stored in a
    * lookup table. They are obtained by the server when the document is
    * built.
    */
   public static final int TYPE_GLOBAL = 0;

   /**
    * Local type specifier. This type means the choices are entered directly
    * and stored in the application.
    */
   public static final int TYPE_LOCAL = 1;

   /**
    * Lookup type specifier. This type means an href is supplied and the
    * control/stylesheet is responsible for obtaining the entries.
    */
   public static final int TYPE_LOOKUP = 2;

   /**
    * Internal lookup type specifier. This means an href is supplied. The
    * entries are obtained through an internal request to the specified href.
    * The application providing the entries must conform to the
    * sys_Lookup.dtd.
    */
   public static final int TYPE_INTERNAL_LOOKUP = 3;

   /**
    * Choice table info specifier. This means table and column info is
    * specified. The entries are obtained by executing the SQL statement
    * using the table info.
    */
   public static final int TYPE_TABLE_INFO = 4;

   /**
    * An array of XML attribute values for the type. They are
    * specified at the index of the specifier.
    */
   private static final String[] TYPE_ENUM =
   {
      "global", "local", "lookup", "internalLookup", "tableinfo"
   };

   /** Ascending sort order specifier */
   public static final int SORT_ORDER_ASCENDING = 0;

   /** Descending sort order specifier */
   public static final int SORT_ORDER_DESCENDING = 1;

   /** User sort order specifier */
   public static final int SORT_ORDER_USER = 2;

   /**
    * An array of XML attribute values for the sort order. They are
    * specified at the index of the specifier.
    */
   private static final String[] SORT_ORDER_ENUM =
   {
      "ascending", "descending", "user"
   };

   /**
    * The type attribute specifies how and when the choices are specified/
    * created. See the type specifiers for additional information.
    */
   private int m_type = TYPE_GLOBAL;

   /** The attribute to specify the sort order. */
   private int m_sortOrder = SORT_ORDER_ASCENDING;

   /**
    * This member is only used if the type is global. It specifies the table
    * identifier to the table the choices are stored in.
    */
   private int m_global = -1;

   /**
    * This member is only valid if the type is local. A collection of
    * PSEntry objects specifying the choices. Never <code>null</code>, may
    * be empty.
    */
   private PSCollection m_local = new PSCollection( PSEntry.class );

   /**
    * This member is only valid if the type is table info. Might be
    * <code>null</code>.
    */
   private PSChoiceTableInfo m_tableInfo = null;

   /**
    * This member is only valid if the type is lookup. It specifies the
    * PSUrlRequest that contains the lookup. Might be <code>null</code>.
    */
   private PSUrlRequest m_lookup = null;

   /**
    * Specifies the null entry and when and how to add is to this choices.
    * This is optional and might therefor be <code>null</code>.
    */
   private PSNullEntry m_nullEntry = null;

   /**
    * A collection of default selected entries (PSDefaultSelected objects).
    * This is optional, never <code>null</code>, might be empty.
    */
   private PSCollection m_defaultSelected =
      new PSCollection( PSDefaultSelected.class );

   /**
    * OPTIONAL Choice filter, which is used to filter dynamic display choices
    * list that replace that returned by the content editor cataloger.
    * Since this element is OPTIONAL it may be <code>null</code>.
    */
   private PSChoiceFilter m_choiceFilter = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String CHOICE_LIST_KEY_ELEM = "Key";
   private static final String DEFAULT_SELECTED_ELEM = "DefaultSelected";
   private static final String TYPE_ATTR = "type";
   private static final String SORT_ORDER_ATTR = "sortOrder";
}

