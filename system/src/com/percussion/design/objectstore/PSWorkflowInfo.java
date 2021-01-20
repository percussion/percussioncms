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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents which workflows a content editor's items are allowed to enter.
 * @see PSContentEditor
 */
public class PSWorkflowInfo extends PSComponent
{

   /**
    * Initializes a newly created <code>PSWorkflowInfo</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSWorkflowInfo( Element sourceNode )
         throws PSUnknownNodeTypeException
   {
      if ( null == sourceNode )
         throw new IllegalArgumentException( "sourceNode cannot be null" );
      fromXml( sourceNode, null, null );
   }


   /**
    * Initializes a newly created <code>PSWorkflowInfo</code> object of the
    * specified type and with the specified workflow ids.
    *
    * @param type either {@link #TYPE_EXCLUSIONARY} or {@link #TYPE_INCLUSIONARY}
    * @param values List of workflow ids that will be allowed or disallowed
    * (depending on the type).
    */
   public PSWorkflowInfo( String type, List values )
   {
      setType(type);
      setValues(values);
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
                        ArrayList parentComponents )
         throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );

      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      setType( getEnumeratedAttribute( tree, XML_TYPE,
         new String[] {TYPE_EXCLUSIONARY, TYPE_INCLUSIONARY} ) );

      setValuesAsCSV( getRequiredElement( tree, XML_VALUES ) );
   }


   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * <pre><code>
    * &lt;!ELEMENT PSXWorkflowInfo EMPTY>
    * &lt;!ATTLIST
    *    type  (inclusionary|exclusionary) #REQUIRED
    *    values CDATA   #REQUIRED
    * >
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
      root.setAttribute( XML_TYPE, getType() );
      root.setAttribute( XML_VALUES, getValuesAsCSV() );
      return root;
   }

   /**
    * Converts the values field from a List to a comma-delimited String.
    *
    * @return String containing each workflow id from the values field,
    * separated by commas.  Never <code>null</code>, but empty if values is
    * empty.
    */
   private String getValuesAsCSV()
   {
      StringBuffer values = new StringBuffer();
      for ( Iterator i = getValues(); i.hasNext(); )
      {
         Integer workflowId = (Integer) i.next();
         values.append(workflowId);
         if (i.hasNext())
            values.append(",");
      }
      return values.toString();
   }

   /**
    * Converts a comma-delimited String of workflow ids to a List and sets it
    * to the values field.
    *
    * @param valuesString comma-delimited integers, each representing a
    * workflow id.
    * @throws NumberFormatException if <code>valuesString</code> contains
    * a token that is not a parseable Integer.
    */
   private void setValuesAsCSV(String valuesString)
   {
      if (null == valuesString)
         throw new IllegalArgumentException("values cannot be null");

      ArrayList values = new ArrayList();
      StringTokenizer st = new StringTokenizer( valuesString, "," );
      while (st.hasMoreTokens())
      {
         values.add( new Integer( st.nextToken() ) );
      }
      setValues( values );
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param c a valid PSWorkflowInfo. Cannot be <code>null</code>.
    */
   public void copyFrom(PSComponent c)
   {
      super.copyFrom( c );
      if (! (c instanceof PSWorkflowInfo) )
         throw new IllegalArgumentException("invalid object for copy");

      PSWorkflowInfo info = (PSWorkflowInfo) c;
      setType( info.getType() );
      List values = new ArrayList();
      for ( Iterator i = info.getValues(); i.hasNext(); )
      {
         values.add( i.next() );
      }
      setValues( values );
   }

   /**
    * Creates a deep copy of this workflow info.
    *
    * @return a deep copy of this object, never <code>null</code>.
    */
   public PSWorkflowInfo deepCopyFrom()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      PSWorkflowInfo wfInfo = null;
      try
      {
         wfInfo = new PSWorkflowInfo(toXml(doc));
      }
      catch (PSUnknownNodeTypeException e)
      {
         /*
          * Since we create the new object from a valide one, this should
          * never happen.
          */
      }

      return wfInfo;
   }

   /**
    * Convenience method to determine whether this workflow is exclusionary or
    * inclusionary.
    *
    * @return <code>true</code> if this is of type exclusionary,
    *    <code>false</code> if it is inclusionary.
    */
   public boolean isExclusionary()
   {
      if (m_valueAccessor != null)
         return m_valueAccessor.isExclusionary();
      
      return getType().equalsIgnoreCase(TYPE_EXCLUSIONARY);
   }

   /**
    * @return an Iterator of the Integers from the values field (workflow ids)
    */
   public Iterator getValues()
   {
      return getValueList().iterator();
   }

   /**
    * Get the workflow ids as a list
    * 
    * @return a {@link List} of workflow ids as {@link Integer}s. Ownership of
    * the list remains with this class. Therefore, any changes made to the list
    * will affect this class.
    */
   public List getWorkflowIds()
   {
      return getValueList();
   }

   /**
    * Get the list of values to use.  
    * 
    * @return The list, never <code>null</code>.
    */
   private List<Integer> getValueList()
   {
      if (m_valueAccessor != null)
         return m_valueAccessor.getValues();
      
      return m_values;
   }
   
   /**
    * Sets the workflow ids that are permitted or disallowed (based on the type)
    * for content items created by the content editor which owns this workflow
    * info.
    *
    * @param values a (possibly empty) List of Integers that represent valid
    * workflow ids or a List of PSWorkflows.  PSWorkflow objects will be
    * converted to Integers based on their database id.
    * <p>
    * The specified List is copied before assignment, so changes to the List
    * will not affect this Object.
    */
   public void setValues( List values )
   {
      if (null == values)
         throw new IllegalArgumentException("values cannot be null");

      m_values = new ArrayList();
      for (Iterator i = values.iterator(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof PSWorkflow)
         {
            // convert it to an Integer
            i.remove();
            m_values.add( new Integer( ((PSWorkflow)o).getDbId() ) );
         }
         else if (!(o instanceof Integer))
            i.remove();
      }
      m_values.addAll(values);
   }


   /**
    * Gets the type of this workflow info.
    *
    * @return either {@link #TYPE_EXCLUSIONARY} or {@link #TYPE_INCLUSIONARY}
    */
   public String getType()
   {
      if (m_valueAccessor != null)
         return m_valueAccessor.isExclusionary() ? TYPE_EXCLUSIONARY : TYPE_INCLUSIONARY;
      
      return m_type;
   }


   /**
    * Sets the type of this workflow info.
    *
    * @param type either {@link #TYPE_EXCLUSIONARY} or {@link #TYPE_INCLUSIONARY},
    * never <code>null</code>.
    */
   public void setType( String type )
   {
      if (null == type)
         throw new IllegalArgumentException("type cannot be null");

      if (! (type.equals(TYPE_EXCLUSIONARY) || type.equals(TYPE_INCLUSIONARY)))
         throw new IllegalArgumentException("type is not a legal value");

      m_type = type;
   }
   
   /**
    * Set a value accessor to use to obtain workflow IDs dynamically instead of
    * using data stored in this object.
    * 
    * @param valueAccessor The accessor to use, may not be <code>null</code>.
    */
   public void setValueAccessor(IPSWorkflowInfoValueAccessor valueAccessor)
   {
      Validate.notNull(valueAccessor);
      m_valueAccessor = valueAccessor;
   }


   /**
    * Name of root XML element
    */
   public static final String XML_NODE_NAME = "PSXWorkflowInfo";

   // names of child XML elements
   private static final String XML_TYPE = "type";
   private static final String XML_VALUES = "values";

   /**
    * Indicates a workflow info describes a list of permitted workflows
    */
   public static final String TYPE_INCLUSIONARY = "inclusionary";

   /**
    * Indicates a workflow info describes a list of forbidden workflows
    */
   public static final String TYPE_EXCLUSIONARY = "exclusionary";

   /**
    * The type of workflow info controls whether the workflows
    * listed as values of this workflow info should be considered an inclusive
    * list (content produced by this content editor must enter one of these
    * workflows) or any exclusive list (content can enter any workflow except
    * these)
    * <p>
    * Set in the constructor and always one of {@link #TYPE_EXCLUSIONARY} or
    * {@link #TYPE_INCLUSIONARY}
    */
   private String m_type;

   /**
    * Contains the workflow ids (as Integers) that will be allowed or
    * disallowed (based on the setting of type).
    * <p>
    * Set in the constructor and never <code>null</code>.
    */
   private List<Integer> m_values;
   
   /**
    * Used to obtain workflow IDs dynamically instead of using data stored in
    * this object, may be <code>null</code> if one has not been set, in which
    * case stored values are used.
    */
   private IPSWorkflowInfoValueAccessor m_valueAccessor;

   // DEBUG
   public static void main(String[] args)
   {
      ArrayList workflows = new ArrayList();
      workflows.add(new Integer(5));
      PSWorkflowInfo me = new PSWorkflowInfo(TYPE_EXCLUSIONARY, workflows);

      workflows.add(new Integer(1));
      workflows.add(new Integer(7));
      PSWorkflowInfo meToo = new PSWorkflowInfo(TYPE_INCLUSIONARY, workflows);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement( "DEBUG" );
      doc.appendChild(root);
      root.appendChild(me.toXml(doc));
      root.appendChild(meToo.toXml(doc));
      System.out.println(PSXmlDocumentBuilder.toString(doc));
   }
}
