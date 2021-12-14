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

import java.util.List;
import java.util.Objects;

/**
 * Represents the metadata for a single workflow
 */
public class PSWorkflow extends PSComponent
{
   /**
    * Initializes a newly created <code>PSWorkflow</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSWorkflow( Element sourceNode )
         throws PSUnknownNodeTypeException
   {
      if ( null == sourceNode )
         throw new IllegalArgumentException( "sourceNode cannot be null" );
      fromXml( sourceNode, null, null );
   }


   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * <pre><code>
    * &lt;!ELEMENT getWorkflows (PSXWorkflow*) >
    *
    * &lt;!ELEMENT PSXWorkflow (name, description?, administrator?, initial_state?)>
    * &lt;!ATTLIST PSXWorkflow
    *    id CDATA #REQUIRED
    * >
    * &lt;!ELEMENT name (#PCDATA)>
    * &lt;!ELEMENT description (#PCDATA)>
    * &lt;!ELEMENT administrator (#PCDATA)>
    * &lt;!ELEMENT initial_state (#PCDATA)>
    * </code></pre>
    *
    * @param doc The XML document being constructed, needed to create new
    *    elements.  Cannot be <code>null</code>.
    * @return    the newly created XML element node
    */
   public Element toXml( Document doc )
   {
      if ( null == doc )
         throw new IllegalArgumentException( "Must provide a valid Document" );
      Element root = doc.createElement( XML_NODE_NAME );

      root.setAttribute( XML_DB_ID, String.valueOf( getDbId() ) );
      PSXmlDocumentBuilder.addElement( doc, root, XML_NAME, getName() );
      if ( getDescription() != null )
         PSXmlDocumentBuilder.addElement( doc, root, XML_DESCRIPTION,
               getDescription() );
      if ( getAdministrator() != null )
         PSXmlDocumentBuilder.addElement( doc, root, XML_ADMINISTRATOR,
               getAdministrator() );
      if ( getInitialState() != NOT_ASSIGNED )
         PSXmlDocumentBuilder.addElement( doc, root, XML_INITIAL_STATE,
               String.valueOf( getInitialState() ) );

      return root;
   }


   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml( Element sourceNode, IPSDocument parentDoc,
                        List parentComponents )
         throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );

      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      // get the required elements
      setDbId( Integer.parseInt( getRequiredElement( tree, XML_DB_ID ) ) );
      setName( getRequiredElement( tree, XML_NAME ) );

      // get the optional elements
      setAdministrator( tree.getElementData( XML_ADMINISTRATOR ) );
      setDescription( tree.getElementData( XML_DESCRIPTION ) );
      setInitialState( Integer.parseInt(
            tree.getElementData( XML_INITIAL_STATE ) ) );
   }


   /**
    * Tests if the specified instance and this instance are equal by comparing
    * name, administrator, description, initial state, and database id.
    *
    * @param test instance to compare to; assumed not <code>null</code>
    * @return <code>true</code> if the specified instance and this instance
    * are equal, <code>false</code> otherwise.
    */
   private boolean equals(PSWorkflow test)
   {
      return ( getDbId() == test.getDbId() ) &&
             ( getName().equals( test.getName() ) ) &&
             ( PSComponent.compare( getAdministrator(),
                   test.getAdministrator() ) ) &&
             ( PSComponent.compare( getDescription() ,
                   test.getDescription() ) ) &&
             ( getInitialState() == test.getInitialState() );
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSWorkflow)) return false;
      if (!super.equals(o)) return false;
      PSWorkflow that = (PSWorkflow) o;
      return m_initialState == that.m_initialState &&
              m_dbId == that.m_dbId &&
              Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_administrator, that.m_administrator) &&
              Objects.equals(m_description, that.m_description);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_administrator, m_description, m_initialState, m_dbId);
   }

   /**
    * Peforms a shallow copy from the specified PSWorkflow instance to this
    * instance.
    *
    * @param c instance from which to copy (not <code>null</code>)
    */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom( c );
      if (!(c instanceof PSWorkflow)) throw
               new IllegalArgumentException("invalid object for copy");

      PSWorkflow workflow = (PSWorkflow) c;
      setDbId( workflow.getDbId() );
      setAdministrator( workflow.getAdministrator() );
      setDescription( workflow.getDescription() );
      setInitialState( workflow.getInitialState() );
      setName( workflow.getName() );
   }


   /**
    * @return a string representation of the object, its name
    * @see #getName
    */
   public String toString()
   {
      return getName();
   }


   /**
    * @return the name assigned to this workflow.  Never <code>null</code> or
    *         empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Sets the name of this workflow.  This name should be unique across
    * all workflows.
    *
    * @param name a unique string to assign
    */
   public void setName( String name )
   {
      if ( null == name || name.trim().length() == 0 )
         throw new IllegalArgumentException( "name cannot be null or empty" );
      m_name = name;
   }


   /**
    * @return the name of the user or role that administers (has full access)
    * this workflow.  May be <code>null</code> or empty.
    */
   public String getAdministrator()
   {
      return m_administrator;
   }


   /**
    * Sets the name of the administrator of this workflow.
    *
    * @param administrator name of user or role that should have administrative
    * rights for this workflow; may be <code>null</code> or empty.
    */
   public void setAdministrator( String administrator )
   {
      m_administrator = administrator;
   }


   /**
    * @return the description assigned to this workflow. May be
    * <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_description;
   }


   /**
    * Sets the description of this content type.
    *
    * @param description may be <code>null</code> or empty
    */
   public void setDescription( String description )
   {
      m_description = description;
   }


   /**
    * @return the database identifier (primary key) for this workfow, or
    * {@link #NOT_ASSIGNED}.
    */
   public int getDbId()
   {
      return m_dbId;
   }


   /**
    * Sets the database identifier (primary key) for this workflow
    *
    * @param dbId the database identifier for the workflow
    */
   public void setDbId(int dbId)
   {
      m_dbId = dbId;
   }


   /**
    * @return the identifier for the state that new content enters this
    *         workflow, or {@link #NOT_ASSIGNED}.
    */
   public int getInitialState()
   {
      return m_initialState;
   }


   /**
    * Sets the state that new content will enter this workflow.
    *
    * @param initialState the database identifier of the state
    */
   public void setInitialState( int initialState )
   {
      m_initialState = initialState;
   }


   /**
    * Name of root XML element
    */
   public static final String XML_NODE_NAME = "PSXWorkflow";

   // names of the XML elements that are this object's children
   private static final String XML_DB_ID = "id";
   private static final String XML_NAME = "name";
   private static final String XML_DESCRIPTION = "description";
   private static final String XML_ADMINISTRATOR = "administrator";
   private static final String XML_INITIAL_STATE = "initial_state";

   /**
    * Indicates that a value has not been assigned.
    */
   public static final int NOT_ASSIGNED = -1;

   private String m_name;
   private String m_administrator = null;
   private String m_description = null;
   private int m_initialState = NOT_ASSIGNED;
   private int m_dbId = NOT_ASSIGNED;
}
