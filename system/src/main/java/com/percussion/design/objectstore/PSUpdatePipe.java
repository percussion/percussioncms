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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * The PSUpdatePipe class extends the PSPipe class providing support for
 * insert, update and/or delete enabled pipes.
 *
 * @see PSDataSet
 * @see PSDataSet#getPipe
 * @see PSPipe
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUpdatePipe extends PSPipe
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSUpdatePipe(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSUpdatePipe() {
      super();
   }

   /**
    * Constructs a new update pipe with the specified name. The pipe is
    * initially empty -- that is, no mappings, exits, etc.
    *
    * @param name    the name of the pipe. This must be a unique
    *                name within the data set. If it is non-unique,
    *                an exception will be thrown when the application
    *                is saved on the E2 server. This is limited to 50
    *                characters.
    */
   public PSUpdatePipe(java.lang.String name)
   {
      super();
      setName(name);
      m_dataSynchronizer = new PSDataSynchronizer();   /* default sync opts */
   }

   /**
    * Get the data synchronizer associated with this pipe. The data
    * synchronizer defines how data modifications will be applied.
    *
    * @return     the data synchronizer
    */
   public PSDataSynchronizer getDataSynchronizer()
   {
      return m_dataSynchronizer;
   }

   /**
    * Overwrite the data synchronizer object with the specified data
    * synchronizer object. If you only want to modify some settings, use
    * getDataSynchronizer to get the existing object and modify the returned
    * object directly.
    * <p>
    * The data synchronizer defines how data modifications will be applied.
    * <p>
    * The PSDataSynchronizer object supplied to this method will be stored
    * with the PSPipe object. Any subsequent changes made to the object by
    * the caller will also effect the pipe.
    *
    * @param synchronizer   the new data synchronizer
    *
    * @see                  #getDataSynchronizer
    * @see                  PSDataSynchronizer
    */
   public void setDataSynchronizer(PSDataSynchronizer synchronizer)
   {
      m_dataSynchronizer = synchronizer;
   }

   private static IllegalArgumentException validateDataSynchronizer(
      PSDataSynchronizer synchronizer)
   {
      if (synchronizer == null)
         return new IllegalArgumentException("updatepipe data synch is null");

      return null;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param pipe a valid PSUpdatePipe. If null, a IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( PSUpdatePipe pipe )
   {
      copyFrom((PSPipe) pipe );
      // assume pipe is in valid state
      m_dataSynchronizer = pipe.getDataSynchronizer();
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXUpdatePipe XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       The PSXUpdatePipe provides support for insert, update and/or
    *       delete enabled pipes. It defines how an XML document is being
    *       mapped to a back-end data store. It also defines how the data
    *       will be modified in the back-end data store.
    *
    *       Object References:
    *
    *       PSXBackEndDataTank - the back-end data tank describing the
    *       back-end data stores being used to access data for this pipe.
    *
    *       PSXDataMapper - the data mapper associated with this pipe. The
    *       data mapper defines mappings between XML elements or attributes
    *       and back-end columns. JavaScript can also be used in lieu of a
    *       back-end column. This allows an XML element or attribute to be
    *       mapped to a dynamically computed value.
    *
    *       PSXDataSynchronizer - the data synchronizer associated with this
    *       pipe. The data synchronizer defines how data modifications will
    *       be applied.
    *    --&gt;
    *    &lt;!ELEMENT PSXUpdatePipe     (name, description?,
    *                                   PSXBackEndDataTank?, PSXDataMapper?,
    *                                   PSXDataSynchronizer?, InputDataExits)&gt;
    *
    *    &lt;!--
    *       the name of the pipe. This must be a unique name within the data
    *       set. This is limited to 50 characters.
    *    --&gt;
    *    &lt;!ELEMENT name             (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the description of the pipe. This is imited to 255 characters.
    *    --&gt;
    *    &lt;!ELEMENT description      (#PCDATA)&gt;
    *
    *    &lt;!--
    *       input data exits are used to pre-process the data sent by the
    *       requestor. Validation can be done, as well as modification of
    *       that data. The exits are returned in the order in which they
    *       will be executed.
    *    --&gt;
    *    &lt;!ELEMENT InputDataExits   (PSXExitCall*)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXUpdatePipe XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      PSXmlDocumentBuilder.addElement(doc, root, PSPipe.NAME_ELEM, m_name);

      PSXmlDocumentBuilder.addElement(
         doc, root, PSPipe.DESCRIPTION_ELEM, m_description);

      if(m_backEndDataTank != null)
         root.appendChild(m_backEndDataTank.toXml(doc));

      if(m_dataMapper != null)
         root.appendChild(m_dataMapper.toXml(doc));

      if(m_dataSynchronizer != null)
         root.appendChild(m_dataSynchronizer.toXml(doc));

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

      return root;
   }

   /**
    * This method is called to populate a PSUpdatePipe Java object
    * from a PSXUpdatePipe XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXUpdatePipe
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
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
         Node saveCur;

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         try {      // set the pipe name
            setName(tree.getElementData("name"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "name",
                         new PSException (e.getLocalizedMessage()));
         }

         try {      // set the pipe description
            setDescription(tree.getElementData("description"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "description",
                         new PSException (e.getLocalizedMessage()));
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXUpdatePipe>

         //PSBackEndDataTank object
         if (tree.getNextElement(PSBackEndDataTank.ms_NodeType, firstFlags) != null) {
            m_backEndDataTank = new PSBackEndDataTank(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //PSXDataMapper object
         if (tree.getNextElement(PSDataMapper.ms_NodeType, firstFlags) != null) {
            m_dataMapper = new PSDataMapper(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //private         PSDataSynchronizer   m_dataSynchronizer = null;
         if (tree.getNextElement(PSDataSynchronizer.ms_NodeType, firstFlags) != null) {
            m_dataSynchronizer = new PSDataSynchronizer(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
         }

         tree.setCurrent(cur);

         //private          PSCollection    m_inputDataExtensions = null;   // PSExtensionCall
         if (tree.getNextElement("InputDataExits", firstFlags) != null) {
            // the input data exits can be empty
            if (tree.getNextElement(PSExtensionCallSet.ms_NodeType, firstFlags) != null) {
               // the next element had better be the exit set
               m_inputDataExtensions = new PSExtensionCallSet(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
            }
         }

         tree.setCurrent(cur);

         //private          PSCollection    m_resultDataExtensions = null;   // PSExtensionCall
         if (tree.getNextElement("ResultDataExits", firstFlags) != null) {
            // the result data exits can be empty
            if (tree.getNextElement(PSExtensionCallSet.ms_NodeType, firstFlags) != null) {
               // the next element had better be the exit set
               m_resultDataExtensions = new PSExtensionCallSet(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
            }
         }
      } finally {
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

      // must have a valid data synchronizer
      IllegalArgumentException ex = validateDataSynchronizer(m_dataSynchronizer);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      if (m_dataSynchronizer != null)
      {
         // at least of these three permissions must be on
         if (!   (m_dataSynchronizer.isDeletingAllowed()
               || m_dataSynchronizer.isUpdatingAllowed()
               || m_dataSynchronizer.isInsertingAllowed() ))
         {
            cxt.validationError(this,
               IPSObjectStoreErrors.UPDATEPIPE_NO_SYNC_TYPES, null);
         }
      }

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_dataSynchronizer != null)
            m_dataSynchronizer.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   public boolean equals(Object o)
   {
      boolean bEqual = true;

      if ( !super.equals(o))
         bEqual = false;

      if ( bEqual && o instanceof PSUpdatePipe )
      {
         PSUpdatePipe other = (PSUpdatePipe)o;

         if (!compare(m_dataSynchronizer, other.m_dataSynchronizer))
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
   private         PSDataSynchronizer   m_dataSynchronizer = null;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXUpdatePipe";
}

