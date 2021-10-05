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

import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSQueryPipe class extends the PSPipe class providing support for
 * query enabled pipes.
 *
 * @see PSDataSet
 * @see PSDataSet#getPipe
 * @see PSPipe
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSQueryPipe extends PSPipe
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object
    * from
    *
    * @param parentDoc the Java object which is the parent of this object
    *
    * @param parentComponents the parent objects of this object
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    */
   public PSQueryPipe(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSQueryPipe() {
      super();
   }

   /**
    * Constructs a new query pipe with the specified name. The pipe is
    * initially empty -- that is, no mappings, exits, etc.
    *
    * @param name    the name of the pipe. This must be a unique
    * name within the data set. If it is non-unique,
    * an exception will be thrown when the application
    * is saved on the E2 server. This is limited to 50
    * characters.
    */
   public PSQueryPipe(java.lang.String name)
   {
      super();
      setName(name);
   }

   /**
    * Get the data selector associated with this pipe. The data selector
    * defines how data will be queried.
    *
    * @return     the data selector (may be null)
    */
   public PSDataSelector getDataSelector()
   {
      return m_dataSelector;
   }

   /**
    * Overwrite the data selector object with the specified data selector
    * object. If you only want to modify some settings, use getDataSelector
    * to get the existing object and modify the returned object directly.
    * <p>
    * The data selector defines how data will be queried.
    * <p>
    * The PSDataSelector object supplied to this method will be stored
    * with the PSPipe object. Any subsequent changes made to the object
    * by the caller will also effect the pipe.
    *
    * @param selector   the new data selector (may be null)
    *
    * @see               #getDataSelector
    * @see               PSDataSelector
    */
   public void setDataSelector(PSDataSelector selector)
   {
      m_dataSelector = selector;
   }

   /**
    * Gets the cache settings if any have been defined for this pipe.
    *
    * @return The settings, never <code>null</code>.
    */
   public PSResourceCacheSettings getCacheSettings()
   {
      return m_cacheSettings;
   }

   /**
    * Sets cache settings for this pipe.
    *
    * @param cacheSettings The cache settings, may not be <code>null</code>.
    */
   public void setCacheSettings(PSResourceCacheSettings cacheSettings)
   {
      if (cacheSettings == null)
         throw new IllegalArgumentException("cacheSettings may not be null");

      m_cacheSettings = cacheSettings;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param pipe a valid PSQueryPipe. 
    */
   public void copyFrom( PSQueryPipe pipe )
   {
      copyFrom((PSPipe) pipe );
      // assume pipe is in valid state
      m_dataSelector = pipe.getDataSelector();
      m_cacheSettings = pipe.m_cacheSettings;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXQueryPipe XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *   &lt;!--
    *      The PSXQueryPipe provides support for query enabled pipes. It
    *      defines how an XML document is being mapped to one or more
    *      back-end data stores. It also defines how the data will be
    *      selected from the back-end data store(s).
    *
    *      Object References:
    *
    *      PSXBackEndDataTank - the back-end data tank describing the
    *      back-end data stores being used to access data for this pipe.
    *
    *      PSXDataMapper - the data mapper associated with this pipe. The
    *      data mapper defines mappings between XML elements or attributes
    *      and back-end columns. JavaScript can also be used in lieu of a
    *      back-end column. This allows an XML element or attribute to be
    *      mapped to a dynamically computed value.
    *
    *      PSXDataSelector - the data selector associated with this pipe.
    *      The data selector defines how data will be queried.
    *
    *      PSXResourceCacheSettings - the cache settings associate with this
    *      pipe.  Defines if and when this pipe's resource is cached.
    *   --&gt;
    *   &lt;!ELEMENT PSXQueryPipe (name, description?,
    *      PSXBackEndDataTank?, PSXDataMapper?,
    *      PSXDataSelector?, ResultDataExits, PSXResourceCacheSettings)&gt;
    *
    *   &lt;!--
    *      the name of the pipe. This must be a unique name within the data
    *      set. This is limited to 50 characters.
    *   --&gt;
    *   &lt;!ELEMENT name             (#PCDATA)&gt;
    *
    *   &lt;!--
    *      the description of the pipe. This is imited to 255 characters.
    *   --&gt;
    *   &lt;!ELEMENT description      (#PCDATA)&gt;
    *
    *   &lt;!--
    *      result data exits are used to post-process the data before it is
    *      sent back to the requestor. Data is in XML format at this point.
    *      Setting cookies and filtering data are the most common uses of
    *      this type of exit. The exits are returned in the order in which
    *      they will be executed.
    *   --&gt;
    *   &lt;!ELEMENT ResultDataExits   (PSXExitCall*)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXQueryPipe XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private  String m_name = ""; defined in base class PSPipe
      PSXmlDocumentBuilder.addElement(
         doc, root, PSPipe.NAME_ELEM, m_name);

      //private  String m_description = ""; defined in base class PSPipe
      PSXmlDocumentBuilder.addElement(
         doc, root, PSPipe.DESCRIPTION_ELEM, m_description);

      //PSBackEndDataTank defined in PSPipe class
      if(m_backEndDataTank != null)
         root.appendChild(m_backEndDataTank.toXml(doc));

      //PSDataMapper object
      if(m_dataMapper != null)
         root.appendChild(m_dataMapper.toXml(doc));

      //private PSDataSelector m_dataSelector = null;
      if(m_dataSelector != null)
         root.appendChild(m_dataSelector.toXml(doc));

      //private com.percussion.util.PSCollection m_resultDataExits = null;
      if (m_inputDataExtensions != null)
      {
         Element parent = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, PSPipe.INPUT_DATA_EXITS_ELEM);
         parent.appendChild(m_inputDataExtensions.toXml(doc));
      }

      if (m_resultDataExtensions != null)
      {
         Element parent = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, PSPipe.RESULT_DATA_EXITS_ELEM);
         parent.appendChild(m_resultDataExtensions.toXml(doc));
      }

      root.appendChild(m_cacheSettings.toXml(doc));

      return root;
   }

   /**
    * This method is called to populate a PSQueryPipe Java object
    * from a PSXQueryPipe XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of type
    * PSXQueryPipe
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
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

         try
         {
            //private          String          m_name = "";
            setName(tree.getElementData("name"));
         }
         catch (IllegalArgumentException e)
         {
            throw new PSUnknownNodeTypeException(ms_NodeType, "name",
               new PSException (e.getLocalizedMessage()));
         }

         try
         {
            //private          String          m_description = "";
            setDescription(tree.getElementData("description"));
         }
         catch (IllegalArgumentException e)
         {
            throw new PSUnknownNodeTypeException(ms_NodeType, "description",
                  new PSException (e.getLocalizedMessage()));
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXQueryPipe>

         //PSBackEndDataTank object
         if (tree.getNextElement(PSBackEndDataTank.ms_NodeType, firstFlags) !=
            null)
         {
            m_backEndDataTank = new PSBackEndDataTank(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //PSXDataMapper object
         if (tree.getNextElement(PSDataMapper.ms_NodeType, firstFlags) != null)
         {
            m_dataMapper = new PSDataMapper(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //PSXDataSelector object
         if (tree.getNextElement(PSDataSelector.ms_NodeType, firstFlags) !=
            null)
         {
            m_dataSelector = new PSDataSelector(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //private PSCollection m_inputDataExits = null;   // PSExtensionCall
         if (tree.getNextElement("InputDataExits", firstFlags) != null)
         {
            // the input data exits can be empty
            if (tree.getNextElement(PSExtensionCallSet.ms_NodeType, firstFlags)
               != null)
            {
               // the next element had better be the exit set
               m_inputDataExtensions = new PSExtensionCallSet(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
            }
         }

         tree.setCurrent(cur);

         //private PSCollection m_resultDataExits = null;   // PSExtensionCall
         if (tree.getNextElement("ResultDataExits", firstFlags) != null)
         {
            // the result data exits can be empty
            if (tree.getNextElement(PSExtensionCallSet.ms_NodeType, firstFlags)
               != null)
            {
               // the next element had better be the exit set
               m_resultDataExtensions = new PSExtensionCallSet(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
            }
         }

         tree.setCurrent(cur);

         // load cache settings if supplied
         if (tree.getNextElement(PSResourceCacheSettings.XML_NODE_NAME,
            firstFlags) != null)
         {
            m_cacheSettings = new PSResourceCacheSettings(
               (Element)tree.getCurrent());
         }
         else // use defaults
            m_cacheSettings = new PSResourceCacheSettings();

      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
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
      super.validate(cxt);

      if (!cxt.startValidation(this, null))
         return;

      if (m_backEndDataTank != null)
      {
         int joinSize = (m_backEndDataTank.getJoins()  == null)
            ? 0 : m_backEndDataTank.getJoins().size();
         int tabSize  = (m_backEndDataTank.getTables() == null)
            ? 0 : m_backEndDataTank.getTables().size();

         // if there are N tables, there must be at *least* N-1 joins
         if (joinSize < (tabSize - 1))
         {
             // too few joins
             String dsName = "unknown";
             IPSComponent pnt = cxt.peekParent();
             if (pnt instanceof PSDataSet)
             {
                     dsName = ((PSDataSet)pnt).getName();
             }

             Object[] args
                     = new Object[] {        dsName, "" + tabSize, "" + joinSize,
                             "" + (tabSize - 1) };

             cxt.validationError(this,
                     IPSObjectStoreErrors.BE_TANK_JOINS_REQUIRED, args);
         }
      }

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_dataSelector == null)
         {
            cxt.validationError(
               this,
               IPSObjectStoreErrors.QPIPE_DATA_SELECTOR_NULL,
               null);
         }
         else
            m_dataSelector.validate(cxt);

         /* if we're doing a heterogeneous join, they must provide the
          * components (where clauses, etc.) so we can build the statements.
          * We do not support allowing them to pass in native statements,
          * as we do not have parsing capabilities at this time.
          */
         if ((m_backEndDataTank != null) && (m_dataSelector != null))
         {
            PSCollection tabs = m_backEndDataTank.getTables();
            int size = (tabs == null) ? 0 : tabs.size();
            if (m_dataSelector.isSelectByNativeStatement() && (size > 1))
            {
               PSBackEndTable firstTable = (PSBackEndTable)tabs.get(0);
               
               for (int i = 0; i < size; i++)
               {
                  PSBackEndTable secondTable = (PSBackEndTable)tabs.get(i);
                  if (!firstTable.isSameDatasource(secondTable))
                  {
                     cxt.validationError(
                        this,
                        IPSObjectStoreErrors.HETERO_NATIVE_SELECT_NOT_SUPPORTED,
                        null);
                  }
               }
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   public boolean equals(Object o)
   {
      boolean bEqual = true;
      /* It isn't strictly necessary to do it this way because the base class is
         abstract, but it doesn't hurt. */
      if ( !super.equals(o))
         bEqual = false;

      if ( bEqual && o instanceof PSQueryPipe )
      {
         PSQueryPipe other = (PSQueryPipe)o;

         if (!compare(m_dataSelector, other.m_dataSelector))
            bEqual = false;
         else if (!compare(m_cacheSettings, other.m_cacheSettings))
            bEqual = false;
      }
      return bEqual;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   // NOTE: when adding members, be sure to update the copyFrom method
   private   PSDataSelector         m_dataSelector = null;

   /**
    * Cache settings to determine if and when this resource is cached.
    * Never <code>null</code>, modified by calls to <code>fromXml()</code>
    * and <code>setCacheSettings</code>.
    */
   private PSResourceCacheSettings m_cacheSettings =
      new PSResourceCacheSettings();

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXQueryPipe";
}


