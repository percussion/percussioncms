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

import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a content editor control dependency.  Dependencies
 * are content editor objects (like section links or exits) that must be part
 * of a content editor for a particular control to operate correctly.
 */
public class PSDependency extends PSComponent
{
   /**
    * Initializes a newly created <code>PSDependency</code> object, from
    * an XML representation.  See {@link #toXml} for the format.
    *
    * @param sourceNode   the XML element node to construct this object from,
    * must not be <code>null</code>.
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   public PSDependency(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException( "sourceNode may not be null" );

      fromXml( sourceNode, null, null );
   }
   

   // see interface for description
   public Object clone()
   {
      PSDependency copy = (PSDependency) super.clone();
      copy.m_dependent = (IPSDependentObject) m_dependent.clone();
      return copy;
   }


   /**
    * Provides a string representation of this object, which is delegated to 
    * the <code>toString()</code> method of the dependent object.
    * 
    * @return a string representation of the object; never <code>null</code>.
    */ 
   public String toString()
   {
      return m_dependent.toString();
   }


   /**
    * Gets the string constant for the type of the dependent object.
    * Used to determine if the dependent object an extension call, section 
    * link, or some other type.  Delegated to the <code>getType()</code> 
    * method of the dependent object.
    * 
    * @return the type; never <code>null</code> or empty.
    * @see IPSDependentObject#getType
    */ 
   private String getType()
   {
      return m_dependent.getType();
   }
   
   
   /**
    *
    * The format is described in sys_LibraryControlDef.dtd, and is reproduced
    * here:
    * <pre><code>
    * &lt;!ELEMENT psxctl:Dependency (psxctl:Default)>
    * &lt;!ATTLIST psxctl:Dependency
    *    status (readyToGo|setupRequired|setupOptional) "readyToGo"
    *    occurrence  (single|multiple) "single"
    * >
    * &lt;!ELEMENT psxctl:Default (PSXUrlRequest | PSXExtensionCall)>
    * </code></pre>
    */ 
   public Element toXml(Document doc)
   {
      Element root = doc.createElement( XML_NODE_NAME );
      root.setAttribute( XATTR_STATUS, getStatusAsString() );
      root.setAttribute( XATTR_OCCURRENCE, getOccurrenceAsString() );
      Element child = doc.createElement( XELEM_DEFAULT );
      child.appendChild( m_dependent.toXml( doc ) );   
      root.appendChild( child );
      return root;
   }

   
   // see interface for method description; see toXml for XML format
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );
      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );
      
      // @status
      String status = getRequiredElement( tree, XATTR_STATUS );
      if (status.equals( "readyToGo" ))
         m_status = READY_STATUS;
      else if (status.equals( "setupRequired" ))
         m_status = SETUP_REQUIRED_STATUS;
      else if (status.equals( "setupOptional" ))
         m_status = SETUP_OPTIONAL_STATUS;
      else
      {
         Object[] args = { XML_NODE_NAME, XATTR_STATUS, status };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );        
      }
      
      // @occurrence
      String occurrence = getRequiredElement( tree, XATTR_OCCURRENCE );
      if (occurrence.equals("single"))
         m_occurrence = SINGLE_OCCURRENCE;
      else if (occurrence.equals("multiple"))
         m_occurrence = MULTIPLE_OCCURRENCE;
      else
      {
         Object[] args = { XML_NODE_NAME, XATTR_OCCURRENCE, occurrence };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args );        
      }
      
      // default
      Element defaultChildElement = 
            tree.getNextElement( PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN );
      if (defaultChildElement != null)
      {
         // make sure this node has the right name
         if (! defaultChildElement.getNodeName().equals( XELEM_DEFAULT ))
         {
            Object[] args = { XML_NODE_NAME, XELEM_DEFAULT, 
                              defaultChildElement };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );                            
         }
         
         Element child = 
               tree.getNextElement( PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN );
         
         /* As IPSDependentObject is derived from IPSReplacementValue, we can
          * use the PSReplacementValueFactory to create the object.
          */
         IPSReplacementValue dependent = PSReplacementValueFactory.
               getReplacementValueFromXml( parentDoc, parentComponents, child,
                     XML_NODE_NAME, XELEM_DEFAULT );
         
         if (dependent instanceof IPSDependentObject)
            setDependent( (IPSDependentObject) dependent );
         else
         {
            Object[] args = { XML_NODE_NAME, XELEM_DEFAULT, dependent };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );            
         }
      }
      else
      {
         Object[] args = { XML_NODE_NAME, XELEM_DEFAULT, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );                
      }
   }


   /**
    * Gets the name of the dependent object.
    * 
    * @return the name; never <code>null</code> or empty.
    */ 
   public String getName()
   {
      return m_dependent.getName();
   }

   /**
    * Gets the dependent object.
    * 
    * @return the dependent object, never <code>null</code>.
    */
   public IPSDependentObject getDependent()
   {
      return m_dependent;
   }


   /**
    * Sets the dependent object.  The dependent object's id is assigned the
    * same value as this dependency's id so it is possible to reconstruct
    * the relationship when the application is loaded.
    * 
    * @param dependent object to assign, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if dependent is <code>null</code>
    */ 
   public void setDependent(IPSDependentObject dependent)
   {
      if (null == dependent)
         throw new IllegalArgumentException("dependent may not be null");
      
      m_dependent = dependent;
      m_dependent.setId( m_id );
   }


   /**
    * Sets the UI (E2Designer) id for this component and sets the dependent 
    * object to have the same id.
    * @param id the id to assign the component and the dependent object
    */ 
   public void setId(int id)
   {
      super.setId( id );
      m_dependent.setId( id );
   }


   /**
    * Converts the status code into a string representation.
    * @return the string representation, never <code>null</code> or empty.
    */ 
   private String getStatusAsString()
   {
      switch (m_status)
      {
         case READY_STATUS:
            return "readyToGo";
            
         case SETUP_REQUIRED_STATUS:
            return "setupRequired";

         case SETUP_OPTIONAL_STATUS:
            return "setupOptional";
      }     
      return "oops"; // this cannot happen
   }


   /**
    * Compares this dependency to the provided dependency, to see if they
    * refer to the same dependent object.  The dependent objects are considered
    * the same if they have same type and name.  For example, this method can
    * answer the question "Do both dependencies require the sys_FileInfo exit?"
    * Parameters of the dependent objects are not compared (neither number of
    * parameters nor values of parameters).
    * 
    * @param testObj object to compare dependent objects with; may not be
    * <code>null</code>.
    * @return <code>true</code> if the dependent objects are the same;
    * <code>false</code> otherwise.
    * @throws IllegalArgumentException if testObj is <code>null</code>.
    */ 
   public boolean hasSameDependent(PSDependency testObj)
   {
      if (null == testObj)
         throw new IllegalArgumentException("testObj may not be null");
      // should the number of parameters be compared?
      return ( getType().equals( testObj.getType() )) &&
             ( getName().equals( testObj.getName() ));
   }
   

   /**
    * @return code that describes how to handle multiple occurrences of this 
    * dependency within a single content editor.
    * @see #SINGLE_OCCURRENCE
    * @see #MULTIPLE_OCCURRENCE
    */ 
   public int getOccurrence()
   {
      return m_occurrence;
   }


   /**
    * @return code that indicates if this dependency can (or must) be modified 
    * by the user.
    * @see #READY_STATUS
    * @see #SETUP_OPTIONAL_STATUS
    * @see #SETUP_REQUIRED_STATUS
    */ 
   public int getStatus()
   {
      return m_status;
   }


   /**
    * Converts the occurrence code into a string representation.
    * @return the string representation, never <code>null</code> or empty.
    */ 
   private String getOccurrenceAsString()
   {
      switch (m_occurrence)
      {
         case SINGLE_OCCURRENCE:
            return "single";
            
         case MULTIPLE_OCCURRENCE:
            return "multiple";
      }      
      return "oops"; // this cannot happen
   }
   
   /** 
    * Describes the behavior when multiple occurrences of this dependency occur
    * within a single content editor.  Must be either <code>SINGLE_OCCURRENCE
    * </code> or <code>MULTIPLE_OCCURRENCE</code>.
    */
   private int m_occurrence;
   
   /** 
    * Indicates a single dependent object should shared by all controls within
    * a single content editor.
    */
   public static final int SINGLE_OCCURRENCE = 0;
   
   /** 
    * Indicates each control should have its own instance of the dependent 
    * object.
    */ 
   public static final int MULTIPLE_OCCURRENCE = 1;
   
   /**
    * Defines if this dependency can (or must) be modified by the user.  Must
    * be either <code>READY_STATUS</code>, <code>SETUP_REQUIRED_STATUS</code>, 
    * or <code>SETUP_OPTIONAL_STATUS</code>.
    */ 
   private int m_status;
   
   /** Indicates this dependency has no parameters to configure. */
   public static final int READY_STATUS = 0;
   
   /** Indicates this dependency has parameters that must be configured. */
   public static final int SETUP_REQUIRED_STATUS = 1;
   
   /** Indicates this dependency has parameters that may be configured. */
   public static final int SETUP_OPTIONAL_STATUS = 2;

   /**
    * Defines the component that must be included in the content editor.
    * Never <code>null</code>.
    */ 
   private IPSDependentObject m_dependent;
   
   /** Name of parent element in XML representation */
   public static final String XML_NODE_NAME = "psxctl:Dependency";
   
   /** Name of the status attribute in XML representation */
   private static final String XATTR_STATUS = "status";
   /** Name of the occurrence attribute in XML representation */
   private static final String XATTR_OCCURRENCE = "occurrence";
   /** Name of the default element in XML representation */
   private static final String XELEM_DEFAULT = "psxctl:Default";
   
   
}
