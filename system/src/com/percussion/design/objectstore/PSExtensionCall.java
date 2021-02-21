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

import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSExtensionCall class defines the framework for calling an
 * extension handler. The user needs to set the extension definition correctly.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSExtensionCall extends PSComponent
   implements IPSBackEndMapping, IPSDocumentMapping, IPSDependentObject
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from
    *
    * @param   parentDoc      the Java object which is the parent of this
    *                             object
    *
    * @param   parentComponents  the parent objects of this object
    *
    * @throws   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    */
   public PSExtensionCall(Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents
      )
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSExtensionCall()
   {

   }

   /**
    * Construct a Java extension call definition.
    * <p>
    * The specified extension will be called with the specified parameters.
    * The parameter string may refer to literals or variables.
    * Variables must be in the correct context. For example, when
    * using the extension as a back-end column mapping, only back-end columns
    * are supported as variables.
    * <P>
    * Note: It is the caller's responsibility to make sure that the param
    * bindings are appropriate for the extension. Before constructing the
    * PSExtensionCall, the caller should obtain the IPSExtensionDef of
    * the called extension and use the runtime parameter defs therein to
    * ensure the param bindings will be accepted by the extension at
    * invocation time.
    *
    * @param ext The name of the extension to be called. Must not be
    * <CODE>null</CODE>.
    *
    * @param params The array of parameter values that will be bound to
    * the extension params at runtime.
    *
    * @see   #setParamValues
    * @see   #setExtensionRef(PSExtensionRef)
    */
   public PSExtensionCall(PSExtensionRef ext, PSExtensionParamValue[] params)
   {
      setExtensionRef(ext);
      setParamValues(params); //defined in PSExtensionCall abstract class
   }


   /**
    * Shallow-copy constructor.
    *
    * @param call object to shallow-copy from; may not be <code>null</code>.
    */
   protected PSExtensionCall(PSExtensionCall source)
   {
      if (null == source)
         throw new IllegalArgumentException("source may not be null");

      try
      {
         setExtensionRef( source.getExtensionRef() );
         setParamValues( source.getParamValues() );
      } catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      setId( source.getId() );
   }


   /**
    * Converts this object into a String suitable for displaying in a table
    * cell.
    * @return the string representation of the extension reference used by
    * this extension call in the format: <i>name(param, param)</i>.
    */
   public String toString()
   {
      StringBuffer function = new StringBuffer();
      PSExtensionParamValue[] params = getParamValues();
      function.append( getExtensionRef().getExtensionName() );
      function.append( "(" );
      for (int i=0; i<params.length; i++)
      {
         if (params[i] == null)
            function.append( "" );
         else
            function.append( params[i].getValue().getValueDisplayText() );
         if (i < (params.length - 1))
            function.append( ", " );
      }
      function.append( ")" );
      return function.toString();
   }

   /**
    * Gets the name of the extension which will be called.
    *
    * @return the extension which will be called; never <code>null</code>.
    */
   public PSExtensionRef getExtensionRef()
   {
      return m_ext;
   }

   /**
    * Sets the extension to be called.
    *
    * @param ext The name of the extension to be called, not <code>null</code>
    */
   public void setExtensionRef(PSExtensionRef ext)
   {
      IllegalArgumentException ex = validateExtension(ext);
      if (ex != null)
         throw ex;

      m_ext = ext;
   }

   /**
    * Validates the provided extension reference.
    *
    * @param ext the extension reference to be validated, might be
    *    <code>null</code>.
    * @return a IllegalArgumentException if the validation failed,
    *    <code>null</code> otherwise.
    */
   private static IllegalArgumentException validateExtension(PSExtensionRef ext)
   {
      if (null == ext)
      {
         return new IllegalArgumentException("udf call exit null");
      }

      return null;
   }

   /**
    * Get the parameter values associated with the extension call.
    * <p>
    * A PSExtensionParamValue object is in the array for each parameter
    * defined in the IPSExtensionDef associated with this object.
    *
    * @return an array of PSExtensionParamValue objects, never <code>null
    * </code>.
    */
   public PSExtensionParamValue[] getParamValues()
   {
      PSExtensionParamValue[] retList =
         new PSExtensionParamValue[m_params.size()];
      m_params.toArray( retList );
      return retList;
   }

   /**
    * Set the parameters associated with the exit call.
    * The object uses the input array. No further use of the array should
    * be made as it will affect this object as well.
    *
    * @param   params      an array of PSExtensionParamValue objects
    */
   public void setParamValues(PSExtensionParamValue[] params)
   {
      setParamValues(PSIteratorUtils.iterator(params));
   }

   /**
    * Set the parameters associated with the exit call.
    *
    * @param params An Iterator over 0 or more PSExtensionParamValue
    * objects. Can be <CODE>null</CODE>.
    */
   public void setParamValues(Iterator params)
   {
      m_params = new LinkedList();

      // build column names which need to be mapped
      ArrayList cols = new ArrayList();

      if (params != null)
      {
         // get the back-end column names
         while (params.hasNext())
         {
            PSExtensionParamValue val = (PSExtensionParamValue)(params.next());
            if (val != null && val.isBackEndColumn())
            {
               cols.add(val.getValue().getValueText());
            }
            m_params.add(val);
         }
      }

      m_columns = new String[cols.size()];
      cols.toArray(m_columns);
   }

   /**
    * Get the list of handler this exit should be applied to.
    *
    * @return a list of handler names this should be applied to, never
    *    <code>null</code>, might be empty. If the returned list is empty,
    *    this should be applied to all handlers, otherwise only to the listed
    *    ones.
    */
   public Iterator getApplyTo()
   {
      if (m_applyTo == null)
         return PSIteratorUtils.emptyIterator();

      return m_applyTo.iterator();
   }

   /**
    * Set a new applyTo list.
    *
    * @param applyTo a list of handler names this exit should be applied to.
    *    Set it to <code>null</code> or empty to apply it to all handlers.
    */
   public void setApplyTo(List applyTo)
   {
      m_applyTo = applyTo;
   }

   // ************ IPSBackEndMapping Interface Implementation ************

   /**
    * Get the columns which must be selected from the back-end(s) in
    * order to use this mapping. The column name syntax is
    * <code>back-end-table-alias.column-name</code>.
    *
    * @return   the columns which must be selected from the back-end(s)
    *           in order to use this mapping
    */
   public String[] getColumnsForSelect()
   {
      return m_columns;
   }


   /**
    * @return the extension name of our extension reference, never <code>null
    * </code>, always well-formed.
    * @see PSExtensionRef#getExtensionName
    */
   public String getName()
   {
      return getExtensionRef().getExtensionName();
   }


   /**
    * @return this extension's parameter values wrapped in a list, never
    * <code>null</code>.
    * @see #getParamValues()
    */
   public Collection getParameters()
   {
      return Arrays.asList( getParamValues() );
   }


   /**
    * @return the constant {@link #VALUE_TYPE}
    */
   public String getType()
   {
      return VALUE_TYPE;
   }


   // see interface for description
   public Object clone()
   {
      PSExtensionCall copy = (PSExtensionCall) super.clone();
      // PSExtensionRef is immutable
      copy.m_params = new ArrayList( m_params.size() );
      for (Iterator iter = m_params.iterator(); iter.hasNext();)
      {
         PSExtensionParamValue value = (PSExtensionParamValue) iter.next();
         copy.m_params.add( value.clone() );
      }
      return copy;
   }


   // *********** IPSReplacementValue Interface Implementation ***********

   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText()
   {
      return getValueText();
   }

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText()
   {
      return (m_ext == null) ? "" : m_ext.toString();
   }

   // **************  IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXExtensionCall XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *  &lt;!--
    *     PSXExtensionCall is used to define a call to a Java exit.
    *        It is most commonly used for translation and validation.
    *
    *        PSXExtensionParamValue - the parameter values to use when making
    *        the call to this exit
    *  --&gt;
    *  &lt;!ELEMENT PSXExtensionCall    (name, PSXExtensionParamValue*)&gt;
    *
    *  &lt;!--
    *     the name of exit which will be called. This name must match a
    *     an exit associated with this application.
    *  --&gt;
    *  &lt;!ELEMENT name          (#PCDATA)&gt;
    * </code></pre>
    *
    * @return   the newly created PSXExtensionCall XML element node
    */
   public Element toXml(Document doc)
   {
      Element  root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // get the name of the exit
      if (m_ext != null)
         PSXmlDocumentBuilder.addElement(doc, root, "name", m_ext.toString());

      if (m_params != null)
      {
         Element node;
         for (Iterator i = m_params.iterator(); i.hasNext(); )
         {
            PSExtensionParamValue val = (PSExtensionParamValue)(i.next());
            if (val != null )
            {
               node = val.toXml(doc);
               root.appendChild(node);
            }
         }
      }

      return root;
   }

   /**
    * This method is called to populate a PSExtensionCall Java object
    * from a PSXExtensionCall XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                      of type PSXExtensionCall
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

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
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      // Find the exit and check its name for validity
      String exitName = tree.getElementData("name");

      if (exitName == null || (!(PSExtensionRef.isValidFullName(exitName))))
      {
         Object[] args = {ms_NodeType, "'name'",
            ((exitName == null) ? "null" : exitName)};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_ext = new PSExtensionRef(exitName);
      Collection params = new LinkedList();

      final String curNodeType = PSExtensionParamValue.ms_NodeType;

      final int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      final int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for ( Element curNode = tree.getNextElement(curNodeType, firstFlags);
         curNode != null;
         curNode = tree.getNextElement(curNodeType, nextFlags))
      {
         params.add(new PSExtensionParamValue(
            (Element)tree.getCurrent(), parentDoc, parentComponents));
      }

      try
      {
         setParamValues(params.iterator());
      }
      catch (IllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(0, e.getLocalizedMessage());
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validate this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw exceptions. Instead, it
    * should register any errors with the validation context to
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      if (m_columns == null)
         cxt.validationError(this, IPSObjectStoreErrors.EXT_CALL_PARAM_VALUE_NULL, null);

      if (m_params != null)
      {
         cxt.pushParent(this);
         try
         {
            for (Iterator i = m_params.iterator(); i.hasNext(); )
            {
               PSExtensionParamValue val = (PSExtensionParamValue)(i.next());
               if (null != val)
                  val.validate(cxt);
            }
         }
         finally
         {
            cxt.popParent();
         }
      }
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSExtensionCall))
         return false;

      PSExtensionCall other = (PSExtensionCall)o;

      if (!compare(m_ext, other.m_ext))
         return false;

      if (!compare(m_params, other.m_params))
         return false;

      return true;
   }

   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE     = "ExtensionCall";

   /**
    * The name of the extension to be called. Never <CODE>null</CODE>
    * after successful init.
    */
   protected PSExtensionRef m_ext;

   /**
    * A Collection of zero or more non-<CODE>null</CODE>
    * PSExtensionParamValue objects. Never <CODE>null</CODE>.
    */
   protected Collection m_params;

   /**
    * An array of zero or more columns which need to be mapped in order for the
    * param values to be bound successfully. There is NOT a 1:1 correspondence
    * between columns and params. Never <CODE>null</CODE> after a successful
    * init.
    */
   protected String[] m_columns;

   /**
    * A list of command handler names this exit should be applied to, might
    * be <code>null</code> or empty. If the list is empty or
    * <code>null</code>, this exit will be applied to all handlers, otherwise
    * it will only be applied to the handlers in this list.
    */
   private List m_applyTo = null;

   /** Name of the root element of this object's XML representation */
   public static final String  ms_NodeType    = "PSXExtensionCall";

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;

      if (m_applyTo != null)
      {
         sum += m_applyTo.hashCode();
      }

      if (m_columns != null)
      {
         sum += m_columns.hashCode();
      }

      if (m_ext != null)
      {
         sum += m_ext.hashCode();
      }

      if (m_params != null)
      {
         sum += m_params.hashCode();
      }

      return sum;
   }

}

