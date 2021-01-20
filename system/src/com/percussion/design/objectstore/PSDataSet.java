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

import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Objects;


/**
 * The PSDataSet class is used to define how data is being accessed by an
 * application. Each data set maps a single XML document type to one or
 * more back-end data stores. Query, insert, update and delete operations
 * may be performed against the back-end data store. Multiple data sets
 * may be defined in an application, providing support for several XML
 * document types within an application.
 *
 * @see PSApplication
 * @see PSApplication#getDataSets
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataSet extends PSComponent
{
   /**
   * Construct a Java object from its XML representation. See the
   * {@link #toXml(Document) toXml} method for a description of the XML object.
   *
   * @param   sourceNode   the XML element node to construct this
   *   object from
   *
   * @param   parentDoc the Java object which is the parent of this
   * object
   *
   * @param   parentComponents the parent objects of this object
   *
   * @exception PSUnknownNodeTypeException
   * if the XML element node is not of the appropriate type
   */
   public PSDataSet(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
   throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
   * Constructor for serialization, fromXml, etc.
   */
   PSDataSet()
   {
      super();
   }

   /**
   * Constructs an empty data set with the specified name.
   *
   * @param name    the new name of the data set. This must be a unique
   *                name within the application. If it is non-unique,
   *                an exception will be thrown when the application
   *                is saved on the E2 server.
   *
   * @see         #setName
   */
   public PSDataSet(String name)
   {
      setName(name);
   }

   /**
   * Get the name of the data set.
   *
   * @return     the name of the data set
   */
   public String getName()
   {
      return m_name;
   }

   /**
   * Set the name of the data set.
   * This is limited to 50 characters.
   *
   * @param name    the new name of the data set. This must be a unique
   *                name within the application. If it is non-unique,
   *                an exception will be thrown when the application
   *                is saved on the E2 server.
   */
   public void setName(String name)
   {
      IllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
   }

   private static IllegalArgumentException validateName(String name)
   {
      if ((null == name) || (name.length() == 0))
         return new IllegalArgumentException("dataset name is null");
      else if (name.length() > MAX_DATASET_NAME_LEN) {
         return new IllegalArgumentException("dataset name is too big" +
            MAX_DATASET_NAME_LEN + " " + name.length());
      }

      return null;
   }

   /**
   * Get the description of the data set.
   *
   * @return the description of the data set
   */
   public String getDescription()
   {
      return m_description;
   }

  /**
   * Set the description of the data set.
   * This is limited to 255 characters.
   *
   * @param description   the new description of the data set.
   */
   public void setDescription(String description)
   {
      IllegalArgumentException ex = validateDescription(description);
      if (ex != null)
         throw ex;

      m_description = description;
   }

   /**
    * This method used to restrict the length of description to
    * MAX_DATASET_DESC_LEN (255) characters. This restriction has been
    * removed so that the description can be of any length.
    * @param description the description of the Data Set.
    * May be <code>null</code> or empty.
    * @return Always returns <code>null</code>
    */
   private static IllegalArgumentException validateDescription(
      @SuppressWarnings("unused") String description)
   {
      return null;
   }

   /**
   * Is transaction support enabled at the row level?
   *
   * @return     <code>true</code> if this is the transaction mode,
   *            <code>false</code> otherwise
   */
   public boolean isTransactionForRow()
   {
      return (DS_TRANSACTION_ROW == m_transactionType);
   }

   /**
    * Enable transaction support at the row level.
    * <p>
    * When performing data modifications (inserts, updates or deletes) it
    * may be desirable to apply changes in a transaction. If a set of changes
    * is being submitted (eg, two inserts, one update a failure of any one of
    * these modifications may require that all the
    * other modifications are also ignored. When this is the case, the
    * transaction should be treated for all rows. Use the
    * setTransactionForAllRows method when this type of transaction support
    * is desired.
    * <p>
    * When applying changes to multiple back-end data stores, it may be
    * desirable to maintain consistency only at the row level. This causes
    * the data to be applied to each back-end for a particular row of data.
    * If the change fails for any back-end, the changes are removed from all
    * the back-ends. If multiple rows are being processed, this will only
    * effect the row that failed, not other rows. Use this method when this
    * type of transaction support is desired.
    * <p>
    * It may also be desirable to allow all succesful changes to be applied
    * without regard for failures. When applying multiple changes which are
    * completely unrelated, this may be desirable. For instance, if updating
    * a price list and one of the prices fails, it may still be desirable to
    * keep the successful price changes. Use the setTransactionDisabled
    * method when this type of transaction support is desired.
    *
    * @see      #setTransactionForAllRows
    * @see      #setTransactionDisabled
    */
   public void setTransactionForRow()
   {
      m_transactionType = DS_TRANSACTION_ROW;
   }

   /**
    * Is transaction support enabled across all rows? This treats the entire
    * data set as a single transaction.
    *
    * @return     <code>true</code> if this is the transaction mode,
    *            <code>false</code> otherwise
    */
   public boolean isTransactionForAllRows()
   {
      return (DS_TRANSACTION_ALL_ROWS == m_transactionType);
   }

   /**
    * Enable transaction support across all rows.
    * <p>
    * When performing data modifications (inserts, updates or deletes) it
    * may be desirable to apply changes in a transaction. If a set of changes
    * is being submitted (eg, two inserts, one update and one delete) the
    * failure of any one of these modifications may require that all the
    * other modifications are also ignored. When this is the case, the
    * transaction should be treated for all rows. Use this method
    * when this type of transaction support is desired.
    * <p>
    * When applying changes to multiple back-end data stores, it may be
    * desirable to maintain consistency only at the row level. This causes
    * the data to be applied to each back-end for a particular row of data.
    * If the change fails for any back-end, the changes are removed from all
    * the back-ends. If multiple rows are being processed, this will only
    * effect the row that failed, not other rows. Use the
    * setTransacctionForRow method when this type of transaction support is
    * desired.
    * <p>
    * It may also be desirable to allow all succesful changes to be applied
    * without regard for failures. When applying multiple changes which are
    * completely unrelated, this may be desirable. For instance, if updating
    * a price list and one of the prices fails, it may still be desirable to
    * keep the successful price changes. Use the setTransactionDisabled
    * method when this type of transaction support is desired.
    *
    * @see      #setTransactionForRow
    * @see      #setTransactionDisabled
    */
   public void setTransactionForAllRows()
   {
      m_transactionType = DS_TRANSACTION_ALL_ROWS;
   }

   /**
   * Is transaction support disabled?
   *
   * @return     <code>true</code> if this is the transaction mode,
   *            <code>false</code> otherwise
   */
   public boolean isTransactionDisabled()
   {
      return (DS_TRANSACTION_DISABLED == m_transactionType);
   }

   /**
    * Disable transaction support.
    * <p>
    * When performing data modifications (inserts, updates or deletes) it
    * may be desirable to apply changes in a transaction. If a set of changes
    * is being submitted (eg, two inserts, one update and one delete) the
    * failure of any one of these modifications may require that all the
    * other modifications are also ignored. When this is the case, the
    * transaction should be treated for all rows. Use the
    * setTransactionForAllRows method when this type of transaction support
    * is desired.
    * <p>
    * When applying changes to multiple back-end data stores, it may be
    * desirable to maintain consistency only at the row level. This causes
    * the data to be applied to each back-end for a particular row of data.
    * If the change fails for any back-end, the changes are removed from all
    * the back-ends. If multiple rows are being processed, this will only
    * effect the row that failed, not other rows. Use the
    * setTransacctionForRow method when this type of transaction support is
    * desired.
    * <p>
    * It may also be desirable to allow all succesful changes to be applied
    * without regard for failures. When applying multiple changes which are
    * completely unrelated, this may be desirable. For instance, if updating
    * a price list and one of the prices fails, it may still be desirable to
    * keep the successful price changes. Use this method when this type of
    * transaction support is desired.
    *
    * @see      #setTransactionForRow
    * @see      #setTransactionForAllRows
    */
   public void setTransactionDisabled()
   {
      m_transactionType = DS_TRANSACTION_DISABLED;
   }

   /**
    * Get the data set's data encryption settings. Through this object,
    * E2 can force users to make requests through SSL. It can even be used to
    * enforce the key strength is appropriate for the given data set. This
    * allows the data set's data to be sent over secure channels. Incoming
    * requests from users, however, can still be sent in the clear. For this
    * reason, care must be taken when designing web pages so that forms
    * containing sensitive data, including user ids and passwords, are
    * submitted using HTTPS, not HTTP.
    *
    * @return     the data set's data encrytion settings (may be null)
    */
   public PSDataEncryptor getDataEncryptor()
   {
      return m_dataEncryptor;
   }

   /**
    * Overwrite the data set's data encryption object with the specified
    * data encryption object. If you only want to modify some data
    * encryption settings, use getDataEncryptor to get the existing object
    * and modify the returned object directly.
    * <p>
    * The PSDataEncryptor object supplied to this method will be stored with
    * the PSDataSet object. Any subsequent changes made to the object by
    * the caller will also effect the data set.
    *
    * @param encryptor   the new data encryptor for the data set
    * @see               #getDataEncryptor
    * @see               PSDataEncryptor
    */
   public void setDataEncryptor(PSDataEncryptor encryptor)
   {
      m_dataEncryptor = encryptor;
   }

   /**
    * Get the pipe defining the data associated with this data set. Pipes
    * are used to define the back-end data stores being used for data
    * access. They also define the mapping between the XML document and the
    * back-end data stores.
    *
    * @return      the PSPipe object (may be null)
    *
    * @see         PSQueryPipe
    * @see         PSUpdatePipe
    */
   public PSPipe getPipe()
   {
      return m_pipe;
   }

   /**
    * Set the pipe defining the data associated with this data set. Pipes
    * are used to define the back-end data stores being used for data
    * access. They also define the mapping between the XML document and the
    * back-end data stores.
    *
    * @param pipe the new pipe to use for this data set
    *
    * @see         PSQueryPipe
    * @see         PSUpdatePipe
    */
   public void setPipe(PSPipe pipe)
   {
      m_pipe = pipe;
   }

   /**
    * Get the page data tank describing the XML document being used for this
    * data set.
    *
    * @return     the page data tank (may be null)
    */
   public PSPageDataTank getPageDataTank()
   {
      return m_pageDataTank;
   }

   /**
    * Overwrite the page data tank object with the specified page data tank
    * object. If you only want to modify some settings, use getPageDataTank
    * to get the existing object and modify the returned object directly.
    * <p>
    * The page data tank describes the XML document being used for this data
    * set.
    * <p>
    * The PSPageDataTank object supplied to this method will be stored with
    * the PSDataSet object. Any subsequent changes made to the object by the
    * caller will also effect the data set.
    *
    * @param dt The new page data tank object. If <code>null</code>, the
    *    existing page is removed.
    *
    * @see            #getPageDataTank
    * @see            PSPageDataTank
    */
   public void setPageDataTank(PSPageDataTank dt)
   {
      m_pageDataTank = dt;
   }

   /**
    * Get the request definition for this data set. This includes the URL
    * and input parameter settings. If the request criteria is met, the
    * request will be processed using this data set.
    *
    * @return      the request definition (may be null)
    *
    * @see         PSRequestor
    */
   public PSRequestor getRequestor()
   {
      return m_requestor;
   }

   /**
    * Overwrite the requestor object with the specified requestor object
    * If you only want to modify some settings, use getRequestor to get
    * the existing object and modify the returned object directly.
    * <p>
    * The PSRequestor object supplied to this method will be stored with
    * the PSDataSet object. Any subsequent changes made to the object by the
    * caller will also effect the data set.
    *
    * @param req      the new requestor object
    *
    * @see            #getRequestor
    * @see            PSRequestor
    */
   public void setRequestor(PSRequestor req)
   {
      if (req == null)
         throw new IllegalArgumentException("dataset requestor is null");

      m_requestor = req;
   }

   /**
    * Is the output of this data set a result page or a link to another
    * data set?
    *
    * @return      <code>true</code> if a result page is the output;
    *             <code>false</code> if a request link is the output
    */
   public boolean isOutputResultPages()
   {
      return (m_results instanceof
         com.percussion.design.objectstore.PSResultPageSet);
   }

   /**
    * Get the result pages being generated by this data set.
    * One data set may produce HTML or XML results. It may also associate
    * a different stylesheet with the output, depending upon user defined
    * conditions.
    *
    * @return      the result pages or <code>null</code> if result pages
    *             are not being used
    *
    * @see         #isOutputResultPages
    */
   public PSResultPageSet getOutputResultPages()
   {
      if (isOutputResultPages())
         return (PSResultPageSet)m_results;

   return null;
   }

   /**
    * Set the output of this data set to use the specified result page(s).
    * If you only want to modify some settings, use getOutputResultPages
    * to get the existing object and modify the returned object directly.
    * <p>
    * The PSResultPageSet object supplied to this method will be stored with
    * the PSDataSet object. Any subsequent changes made to the object by the
    * caller will also effect the data set.
    *
    * @param output the new result pages object
    */
   public void setOutputResultPages(PSResultPageSet output)
   {
      m_results = output;
   }

   /**
    * Get the request link which will be used to get the output for this
    * data set. This allows data sets which do not produce output
    * (namely, update data sets) to execute other data sets to generate
    * output for them.
    *
    * @return      the request link or <code>null</code> if request links
    *             are not being used
    *
    * @see         #isOutputResultPages
    */
   public PSRequestLink getOutputRequestLink()
   {
      if (!isOutputResultPages())
         return (PSRequestLink)m_results;

      return null;
   }

   /**
    * Set the output of this data set to use the specified request link.
    * If you only want to modify some settings, use getOutputRequestLink
    * to get the existing object and modify the returned object directly.
    * <p>
    * The PSRequestLink object supplied to this method will be stored with
    * the PSDataSet object. Any subsequent changes made to the object by the
    * caller will also effect the data set.
    *
    * @param output the new request links object
    */
   public void setOutputRequestLink(PSRequestLink output)
   {
      m_results = output;
   }

   /**
    * Get the result pager which defines how data will be grouped for
    * return to the user. When a query may generate large result sets,
    * a pager object can be used to return the data in chunks.
    *
    * @return            the result pager or <code>null</code> if
    *                     paging is disabled
    */
   public PSResultPager getResultPager()
   {
      return m_resultPager;
   }

   /**
    * Set the result pager which defines how data will be grouped for
    * return to the user. When a query may generate large result sets,
    * a pager object can be used to return the data in chunks.
    *
    * @param   pager      the new result pager to use
    *                     or <code>null</code> to disable paging
    */
   public void setResultPager(PSResultPager pager)
   {
      m_resultPager = pager;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param ds a valid PSDataSet. 
    */
   public void copyFrom( PSDataSet ds )
   {
      copyFrom((PSComponent) ds );
      // assume object is valid
      m_name = ds.getName();
      m_description = ds.getDescription();
      if ( ds.isTransactionDisabled())
         setTransactionDisabled();
      else if ( ds.isTransactionForAllRows())
         setTransactionForAllRows();
      else if ( ds.isTransactionForRow())
         setTransactionForRow();
      m_dataEncryptor = ds.getDataEncryptor();
      m_pipe = ds.getPipe();
      m_pageDataTank = ds.getPageDataTank();
      m_requestor = ds.getRequestor();
      m_resultPager = ds.getResultPager();
      m_results = ds.getOutputRequestLink();
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataSet XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataSet is used to define how data is being accessed by an
    *       application. Each data set maps a single XML document type to
    *       one or more back-end data stores. Query, insert, update and
    *       delete operations may be performed against the back-end data
    *       store. Multiple data sets may be defined in an application,
    *       providing support for several XML  document types within an
    *       application.
    *
    *       Object References:
    *
    *       PSXDataEncryptor - the data set's data encryption settings.
    *       Through this object, E2 can force users to make requests through
    *       SSL. It can even be used to enforce the key strength is
    *       appropriate for the given data set. This allows the data set's
    *       data to be sent over secure channels. Incoming requests from
    *       users, however, can still be sent in the clear. For this reason,
    *       care must be taken when designing web pages so that forms
    *       containing sensitive data, including user ids and passwords, are
    *       submitted using HTTPS, not HTTP.
    *
    *       PSXQueryPipe, PSXUpdatePipe - the pipes defining the data
    *       associated with this data set. Pipes are used to define the
    *       back-end data stores being used for data access. They also define
    *       the mapping between the XML document and the back-end data stores.
    *       Multiple pipes are commonly used in one of two scenarios. When
    *       both querying and updating are required, one PSXQueryPipe and one
    *       PSXUpdatePipe can be defined. When a single XML document will be
    *       used to update multiple back-end tables, the PSXUpdatePipe falls
    *       short. It only allows a single update table to be defined. The
    *       solution to this problem is to define multiple PSXUpdatePipe
    *       objects in the data set.
    *
    *       PSXPageDataTank - the page data tank describing the XML document
    *       being used for this data set.
    *
    *       PSXRequestor - the request definition for this data set. This
    *       includes the URL and input parameter settings. If the request
    *       criteria is met, the request will be processed using this data
    *       set.
    *
    *       PSXResultPageSet - the definition of the results being generated
    *       by this data set. One data set may produce HTML or XML results.
    *       It may also associate a different stylesheet with the output,
    *       depending upon user defined conditions.
    *    --&gt;
    *    &lt;!ELEMENT PSXDataSet       (name, description?,
    *                                   transactionType?, PSXDataEncryptor?,
    *                                   (PSXUpdatePipe | PSXQueryPipe),
    *                                   PSXPageDataTank?, PSXRequestor?,
    *                                   (PSXResultPageSet | PSXRequestLink)?,
    *                                    PSXResultPager?)&gt;
    *
    *    &lt;!--
    *       the name of the data set. This must be a unique name within the
    *       application. This is limited to 50 characters.
    *    --&gt;
    *    &lt;!ELEMENT name             (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the new description of the data set. This is limited to 255
    *       characters.
    *    --&gt;
    *    &lt;!ELEMENT description      (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the type of transaction integrity to support:
    *
    *       forRow - when applying changes to multiple back-end data stores,
    *       it may be desirable to maintain consistency only at the row
    *       level. This causes the data to be applied to each back-end for a
    *       particular row of data. If the change fails for any back-end,
    *       the changes are removed from all the back-ends. If multiple rows
    *       are being processed, this will only effect the row that failed,
    *       not other rows. Use this method when this type of transaction
    *       support is desired.
    *
    *       forAllRows - when performing data modifications (inserts, updates
    *       or deletes) it may be desirable to apply changes in a
    *       transaction. If a set of changes is being submitted (eg, two
    *       inserts, one update and one delete) the failure of any one of
    *       these modifications may require that all the other modifications
    *       are also ignored. When this is the case, the transaction should
    *       be treated for all rows.
    *
    *       none - it may also be desirable to allow all succesful changes to
    *       be applied without regard for failures. When applying multiple
    *       changes which are completely unrelated, this may be desirable. For
    *       instance, if updating a price list and one of the prices fails, it
    *       may still be desirable to keep the successful price changes. Use
    *       the setTranscationDisabled method when this type of transaction
    *       support is desired.
    *    --&gt;
    *    &lt;!ENTITY % PSXDataSetTransactionType "(forRow, forAllRows, none)"&gt;
    *
    *    &lt;!--
    *       the type of transaction support (see %PSXDataSetTransactionType)
    *    --&gt;
    *    &lt;!ELEMENT transactionType   (%PSXDataSetTransactionType)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataSet XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private          String          m_name = "";
      PSXmlDocumentBuilder.addElement(doc, root, "name", m_name);

      //private          String          m_description = "";
      PSXmlDocumentBuilder.addElement(doc, root, "description", m_description);

      //private          int             m_transactionType = DS_TRANSACTION_DISABLED;
      if (m_transactionType == DS_TRANSACTION_ROW)
         PSXmlDocumentBuilder.addElement(   doc, root, "transactionType",
            XML_FLAG_XACT_ROW);
      else if (m_transactionType == DS_TRANSACTION_ALL_ROWS)
         PSXmlDocumentBuilder.addElement(   doc, root, "transactionType",
            XML_FLAG_XACT_ALL_ROWS);
      else
         PSXmlDocumentBuilder.addElement(   doc, root, "transactionType",
            XML_FLAG_XACT_NONE);

      //private          PSDataEncryptor m_dataEncryptor = null;
      if (m_dataEncryptor != null)
         root.appendChild(m_dataEncryptor.toXml(doc));

      //private          PSPipe    m_pipe = null;
      if (m_pipe != null)
         root.appendChild(m_pipe.toXml(doc));

      //private          PSPageDataTank  m_pageDataTank = null;
      if (m_pageDataTank != null)
         root.appendChild(m_pageDataTank.toXml(doc));

      //private          PSRequestor     m_requestor = null;
      if (m_requestor != null)
         root.appendChild(m_requestor.toXml(doc));

      //private          IPSResults      m_results;
      if (m_results != null)
         root.appendChild(((IPSComponent)m_results).toXml(doc));

      //private         PSResultPager      m_resultPager;
      if (m_resultPager != null)
         root.appendChild(m_resultPager.toXml(doc));

      return root;
   }

   /**
    * This method is called to populate a PSDataSet Java object
    * from a PSXDataSet XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataSet
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

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         try {      //private          String          m_name = "";
            setName(tree.getElementData("name"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "name",
               new PSException (e.getLocalizedMessage()));
         }

         try {      //private          String          m_description = "";
            setDescription(tree.getElementData("description"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "description",
               new PSException (e.getLocalizedMessage()));
         }

         //private       int    m_transactionType = DS_TRANSACTION_DISABLED;
         sTemp = tree.getElementData("transactionType");
         if (sTemp != null) {
            if (sTemp.equalsIgnoreCase(XML_FLAG_XACT_NONE))
               m_transactionType = DS_TRANSACTION_DISABLED;
            else if (sTemp.equalsIgnoreCase(XML_FLAG_XACT_ROW))
               m_transactionType = DS_TRANSACTION_ROW;
            else if (sTemp.equalsIgnoreCase(XML_FLAG_XACT_ALL_ROWS))
               m_transactionType = DS_TRANSACTION_ALL_ROWS;
               else {
                  Object[] args = { ms_NodeType, "transactionType", sTemp };
                     throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         else
            m_transactionType = DS_TRANSACTION_DISABLED;

         m_dataEncryptor = null;
         m_pipe = null;
         m_requestor = null;
         m_pageDataTank = null;
         m_resultPager = null;
         m_results = null;

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         for (   Element curNode = tree.getNextElement(firstFlags);
            curNode != null;
            curNode = tree.getNextElement(nextFlags))
         {
            String elementName = curNode.getTagName();
            if (PSDataEncryptor.ms_NodeType.equals(elementName)) {
               if(null == m_dataEncryptor)
                  m_dataEncryptor = new PSDataEncryptor(true);   //default
               m_dataEncryptor.fromXml(curNode, parentDoc, parentComponents);
            }
            else if (PSQueryPipe.ms_NodeType.equals(elementName)) {
               m_pipe = new PSQueryPipe(curNode, parentDoc, parentComponents);
            }
            else if (PSUpdatePipe.ms_NodeType.equals(elementName)) {
               m_pipe = new PSUpdatePipe(curNode, parentDoc, parentComponents);
            }
            else if (PSContentEditorPipe.XML_NODE_NAME.equals(elementName)) {
               m_pipe = new PSContentEditorPipe(
                  curNode, parentDoc, parentComponents);
            }
            else if (PSPageDataTank.ms_NodeType.equals(elementName)) {
               m_pageDataTank = new PSPageDataTank(
                  curNode, parentDoc, parentComponents);
            }
            else if (PSRequestor.ms_NodeType.equals(elementName)) {
               m_requestor = new PSRequestor(
                  curNode, parentDoc, parentComponents);
            }
            else if (PSResultPageSet.ms_NodeType.equals(elementName)) {
               m_results = new PSResultPageSet(
                  curNode, parentDoc, parentComponents);
            }
            else if (PSRequestLink.ms_NodeType.equals(elementName)) {
               m_results = new PSRequestLink(
                  curNode, parentDoc, parentComponents);
            }
            else if (PSResultPager.ms_NodeType.equals(elementName)) {
               m_resultPager = new PSResultPager(
                  curNode, parentDoc, parentComponents);
            }
         }

      } finally {
         resetParentList(parentComponents, parentSize);
      }

   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   @SuppressWarnings("unchecked")
   @Override
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateName(m_name);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDescription(m_description);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      //page tank is only required if this requestor is not a binary resource
      if (m_pageDataTank == null && (m_requestor != null &&
         !m_requestor.isDirectDataStream()))
         cxt.validationError(this, 0, "dataset page tank full");

      /* Go through the result pages and verify no xml field can be used
      for more than one type of request link */

      if (m_results != null)
      {
         if (m_results instanceof PSResultPageSet)
         {
            PSCollection   resultPages =
               ((PSResultPageSet)m_results).getResultPages();
            HashMap fieldLinks = new HashMap();
            int size = 0;

            if (resultPages != null)
            size = resultPages.size();

            for (int i = 0; i < size; i++)
            {
               PSResultPage resultPage = (PSResultPage) resultPages.get(i);
               PSCollection requestLinks = resultPage.getRequestLinks();

               for (int j = 0; j < requestLinks.size(); j++)
               {
                  PSRequestLink requestLink =
                     (PSRequestLink) requestLinks.get(j);
                  PSRequestLink requestLinkCheck =
                     (PSRequestLink) fieldLinks.get(requestLink.getXmlField());
                  if ((requestLinkCheck != null) &&
                     (!requestLinkCheck.equals(requestLink)))
                     cxt.validationError(this,
                        IPSObjectStoreErrors.DATASET_XMLFIELD_MULTI_LINK_ERROR,
                  requestLink.getXmlField());
                  fieldLinks.put(requestLink.getXmlField(), requestLink);
               }
            }
         } else if (m_results instanceof PSRequestLink)
         {
            /* Only one link, which is ok */
         }
      }

      // if (m_results == null
      //    &&   (m_requestor != null && m_requestor.getOutputMimeType() == null
      //       && m_requestor.isHtmlOutputEnabled()))
      //    cxt.validationError(this, IPSObjectStoreErrors.DATASET_RESULT_PAGES_NULL,
      //    null);

      // do children
      cxt.pushParent(this);

      try
      {
         if (m_dataEncryptor != null)
            m_dataEncryptor.validate(cxt);

         if (m_pipe != null)
            m_pipe.validate(cxt);

         if (m_pageDataTank != null)
            m_pageDataTank.validate(cxt);

         if (m_requestor != null)
            m_requestor.validate(cxt);

         if (m_resultPager != null)
            m_resultPager.validate(cxt);

         if (m_results instanceof IPSComponent)
         {
            IPSComponent cpnt = (IPSComponent)m_results;
            cpnt.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSDataSet)) return false;
        if (!super.equals(o)) return false;
        PSDataSet psDataSet = (PSDataSet) o;
        return m_transactionType == psDataSet.m_transactionType &&
                Objects.equals(m_name, psDataSet.m_name) &&
                Objects.equals(m_description, psDataSet.m_description) &&
                Objects.equals(m_dataEncryptor, psDataSet.m_dataEncryptor) &&
                Objects.equals(m_pipe, psDataSet.m_pipe) &&
                Objects.equals(m_pageDataTank, psDataSet.m_pageDataTank) &&
                Objects.equals(m_requestor, psDataSet.m_requestor) &&
                Objects.equals(m_resultPager, psDataSet.m_resultPager) &&
                Objects.equals(m_results, psDataSet.m_results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), m_name, m_description, m_transactionType, m_dataEncryptor, m_pipe, m_pageDataTank, m_requestor, m_resultPager, m_results);
    }

    /**
    * Creates a deep copy of this PSDataSet.
    * @return Return clone of this instance
    */
   @Override
   public Object clone()
   {
      PSDataSet copy = (PSDataSet)super.clone();
      if (m_pipe != null)
         copy.m_pipe = (PSPipe)m_pipe.clone();
      if (m_dataEncryptor != null)
         copy.m_dataEncryptor = (PSDataEncryptor)m_dataEncryptor.clone();
      if (m_pageDataTank != null)
         copy.m_pageDataTank = (PSPageDataTank)m_pageDataTank.clone();
      if (m_requestor != null)
         copy.m_requestor = (PSRequestor)m_requestor.clone();
      if (m_resultPager != null)
         copy.m_resultPager = (PSResultPager)m_resultPager.clone();
      if (m_results != null)
         copy.m_results = (IPSResults)m_results.clone();
      return copy;
   }

   // NOTE: when adding members, be sure to update the copyFrom method
   private String          m_name = "";
   private String          m_description = "";
   private int             m_transactionType = DS_TRANSACTION_DISABLED;
   private PSDataEncryptor m_dataEncryptor = null;
   private PSPipe            m_pipe = null;
   private PSPageDataTank  m_pageDataTank = null;
   private PSRequestor     m_requestor = null;
   private PSResultPager   m_resultPager = null;
   private IPSResults      m_results;

   //transactions disabled
   private static final int DS_TRANSACTION_DISABLED   = 0;
   //transactions per row
   private static final int DS_TRANSACTION_ROW      = 1;
   //transactions per multiple rows
   private static final int DS_TRANSACTION_ALL_ROWS   = 2;

   private static final String   XML_FLAG_XACT_NONE      = "none";
   private static final String   XML_FLAG_XACT_ROW         = "forRow";
   private static final String   XML_FLAG_XACT_ALL_ROWS   = "forAllRows";

   private static final int   MAX_DATASET_NAME_LEN      = 50;

   /** package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType   = "PSXDataSet";
}
