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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation for the PSXLocation DTD in BasicObjects.dtd. This object is
 * used to indicate where custom actions are to be displayed.
 */
public class PSLocation extends PSComponent
{
   /**
    * Creates a new custom action location.
    *
    * @param page the page type, one of
    *    PAGE_SUMMARY_VIEW|PAGE_ROW_EDIT|PAGE_CHILD_ROW_EDIT
    * @param type the location type, one of
    *    TYPE_FORM|TYPE_ROW|TYPE_FIELD|TYPE_WF_ACTION|TYPE_WF_TRANSITION
    */
   public PSLocation(int page, int type)
   {
      setPage(page);
      setType(type);
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
   public PSLocation(Element sourceNode, IPSDocument parentDoc,
                     List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   /**
    * Checks this object to determine if there is a match between the supplied
    * properties and this object.
    *
    * @param pageType One of the PAGE_xxx types.
    *
    * @param pageLocation One of the TYPE_xxx constants.
    *
    * @param fieldRef The name of the field/fieldset to which the actions
    *    should apply. This parameter is only needed for certain values of
    *    page type and pageLocation. See {@link #getFieldRefs() getFieldRefs}
    *    for more details.
    *
    * @return <code>true</code> if there is a match, <code>false</code>
    *    otherwise.
    */
   public boolean hasCustomActions( int pageType, int pageLocation,
         String fieldRef )
   {
      boolean fieldMatch = false;
      Iterator fieldRefs = getFieldRefs();

      // if there are no fields specified, then all fields match
      if ( !fieldRefs.hasNext())
         fieldMatch = true;

      while ( fieldRefs.hasNext() && !fieldMatch )
      {
         String fieldName = (String) fieldRefs.next();
         if ( fieldName.equalsIgnoreCase( fieldRef ))
            fieldMatch = true;
      }
      return ( pageType == m_page && pageLocation == m_type && fieldMatch );
   }


   /**
    * Needed for serialisation
    */
   protected PSLocation()
   {
   }

   /**
    * Get the location page.
    *
    * @return the location page.
    */
   public int getPage()
   {
      return m_page;
   }

   /**
    * Set a new location page.
    *
    * @param page the new location page.
    */
   public void setPage(int page)
   {
      if (page != PAGE_SUMMARY_VIEW &&
          page != PAGE_ROW_EDIT &&
          page != PAGE_CHILD_ROW_EDIT)
         throw new IllegalArgumentException("Unknown page");

      m_page = page;
   }

   /**
    * Get the location type.
    *
    * @return the location type.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set a new location type.
    *
    * @param type the new location type.
    */
   public void setType(int type)
   {
      if (type != TYPE_FORM && type != TYPE_ROW &&
          type != TYPE_FIELD && type != TYPE_WF_ACTION &&
          type != TYPE_WF_TRANSITION)
         throw new IllegalArgumentException("Unknown type");

      m_type = type;
   }


   /**
    * Gets the preferred position of this action relative to other actions.
    *
    * @return The preferred position, 1 based. A value &lt; 1 means the
    *    designer didn't specify a position.
    */
   public int getSequence()
   {
      return m_sequence;
   }


   /**
    * Sets the position of this action relative to other actions located in
    * the same location.
    *
    * @param position The relative position, 1 based. A value of 1 means
    *    place this action at the top of the list of actions in the output
    *    document.
    */
   public void setSequence( int position )
   {
      if ( position < 1 )
         m_sequence = 0;
      else
         m_sequence = position;
   }


   /**
    * When positioning an action relative to children objects, this list
    * specifies which field sets the action should be set in. If the page is
    * <code>PAGE_SUMMARY_VIEW</code> or <code>PAGE_ROW_EDIT</code> and type
    * is <code>TYPE_FORM</code>, then this should contain 1 or more elements,
    * otherwise the entries should be ignored.
    * <p>This class does not enforce this rule except when loading from xml
    * to make it easier to manipulate the object programatically.
    *
    * @return A valid iterator over 0 or more String objects. Each string
    *    should reference an existing fieldset (although this class makes
    *    no guarantee that this is <code>true</code>).
    */
   public Iterator getFieldRefs()
   {
      return m_fieldRefs.iterator();
   }


   /**
    * See {@link #getFieldRefs() getFieldRefs} for a description.
    *
    * @param refs An iterator with 0 or more Strings, never <code>null</code>.
    *    Each entry is added to the local list by doing a <code>toString
    *    </code> on it.
    */
   public void setFieldRefs( Iterator refs )
   {
      if ( null == refs )
         throw new IllegalArgumentException( "refs can't be null" );

      m_fieldRefs.clear();
      while ( refs.hasNext())
         m_fieldRefs.add( refs.next().toString());
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSField, not <code>null</code>.
    */
   public void copyFrom(PSLocation c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_page = c.m_page;
      m_type = c.m_type;
      m_sequence = c.m_sequence;
      m_fieldRefs = c.m_fieldRefs;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSLocation))
         return false;

      PSLocation t = (PSLocation) o;

      boolean equal = true;
      if (m_page != t.m_page)
         equal = false;
      else if (m_type != t.m_type)
         equal = false;
      else if ( m_sequence != t.m_sequence )
         equal = false;
      else if ( !compare( m_fieldRefs, t.m_fieldRefs ))
         equal = false;

      return equal;
   }
   
   /**
    * Generates hash code for this object. 
    */
   @Override
   public int hashCode()
   {
      return m_page +
            m_type +
            m_sequence +
            (m_fieldRefs == null ? 0 : m_fieldRefs.hashCode());
   }

   // see IPSComponent
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
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

         // OPTIONAL: get the page attribute
         data = tree.getElementData(PAGE_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<PAGE_ENUM.length; i++)
            {
               if (PAGE_ENUM[i].equalsIgnoreCase(data))
               {
                  m_page = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PAGE_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

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

         // OPTIONAL: get the sequence attribute
         data = tree.getElementData(SEQUENCE_ATTR);
         if (data != null)
         {
            boolean found = false;
            try
            {
               m_sequence = Integer.parseInt( data );
            }
            catch ( NumberFormatException e )
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  SEQUENCE_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // Load field refs if there are any
         if ( PAGE_SUMMARY_VIEW == m_page || ( PAGE_ROW_EDIT == m_page &&
               TYPE_FORM == m_type ))
         {
            node = tree.getNextElement( FIELDREF_ELEM, firstFlags );
            if ( null == node )
            {
               Object[] args =
               {
                  FIELDREF_ELEM,
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_NULL, args);
            }

            do
            {
               String fieldRef = tree.getElementData( node );
               if ( fieldRef.trim().length() == 0 )
               {
                  Object[] args =
                  {
                     XML_NODE_NAME,
                     FIELDREF_ELEM,
                     ""
                  };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
               m_fieldRefs.add( fieldRef );
               node = tree.getNextElement( FIELDREF_ELEM, nextFlags );
            }
            while ( null != node );
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(PAGE_ATTR, PAGE_ENUM[m_page]);
      root.setAttribute(TYPE_ATTR, TYPE_ENUM[m_type]);
      if ( m_sequence > 0 )
         root.setAttribute(SEQUENCE_ATTR, ""+m_sequence);

      // add the field refs, if there are any
      Iterator refs = m_fieldRefs.iterator();
      while ( refs.hasNext())
      {
         String name = (String) refs.next();
         Element fieldRefElem = doc.createElement( FIELDREF_ELEM );
         fieldRefElem.appendChild( doc.createTextNode( name ));
         root.appendChild( fieldRefElem );
      }

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (( PAGE_SUMMARY_VIEW == m_page || ( PAGE_ROW_EDIT == m_page &&
               TYPE_FORM == m_type )) && m_fieldRefs.size() == 0 )
         {
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_LOCATION, null);
         }
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXLocation";

   /** Form type specifier */
   public static final int PAGE_SUMMARY_VIEW = 0;

   /** Row type specifier */
   public static final int PAGE_ROW_EDIT = 1;

   /** Field type specifier */
   public static final int PAGE_CHILD_ROW_EDIT = 2;

   /**
    * An array of XML attribute values for the page.
    * They are specified at the index of the specifier.
    */
   private static final String[] PAGE_ENUM =
   {
      "summaryView", "rowEdit", "childRowEdit"
   };

   /** Form type specifier */
   public static final int TYPE_FORM = 0;

   /** Row type specifier */
   public static final int TYPE_ROW = 1;

   /** Field type specifier */
   public static final int TYPE_FIELD = 2;

   /** Workflow action type specifier */
   public static final int TYPE_WF_ACTION = 3;

   /** Workflow transition type specifier */
   public static final int TYPE_WF_TRANSITION = 4;

   /**
    * An array of XML attribute values for the type.
    * They are specified at the index of the specifier.
    */
   private static final String[] TYPE_ENUM =
   {
      "form", "row", "field", "wfAction", "wfTransition"
   };

   /**
    * Which type of editor should the action appear upon. Always one of the
    * PAGE_xxx types. Defaults to <code>PAGE_SUMMARY_VIEW</code>.
    */
   private int m_page = PAGE_SUMMARY_VIEW;

   /**
    * Where on the page is the action to be located. Always one of the
    * TYPE_xxx values. Defaults to <code>TYPE_FORM</code>.
    */
   private int m_type = TYPE_FORM;

   /**
    * If <code>m_page</code> is <code>PAGE_SUMMARY_VIEW</code> or it's <code>
    * PAGE_ROW_EDIT</code> and the type is TYPE_FIELD, then this list contains
    * the names of the field sets to which the action applies, otherwise it is
    * empty. Each entry is a String. Never <code>null</code>.
    */
   private List m_fieldRefs = new ArrayList();

   /**
    * What's the position of this button relative to the existing buttons.
    * A value of 1 indicates this button should be first in the list of actions
    * in the output document. A value &lt; 1 means don't care. In that case,
    * they should be added to the end of the list. The default value is 0.
    */
   private int m_sequence = 0;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String PAGE_ATTR = "page";
   private static final String TYPE_ATTR = "type";
   private static final String SEQUENCE_ATTR = "sequence";
   private static final String FIELDREF_ELEM = "FieldRef";
}

