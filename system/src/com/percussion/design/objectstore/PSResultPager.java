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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;


/**
 * The PSResultPager class defines how result pages will be generated to
 * allow for partial result set processing. For instance, a request may
 * generate 100 rows of result data. It may be useful to limit this,
 * allow 10 rows per page, etc.
 *
 * @see          PSDataSet
 * @see          PSDataSet#getResultPager
 *
 * @author       Tas Giakouminakis
 * @version     1.0
 * @since    1.0
 */
public class PSResultPager extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                                                            object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                                                            object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                                                            if the XML element node is not of the
    *                                                            appropriate type
    */
   public PSResultPager(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a results paging object.
    */
   public PSResultPager()
   {
      super();
   }

   /**
    * Get the maximum number of rows which can be returned in a page.
    * <p>
    * Users may perform broad searches in an attempt to locate information.
    * This may result in large result sets which negatively impact server
    * performance. The impact of large searches can be minimized by defining
    * the number of rows which can be returned in a result page. The maximum
    * number of result pages can also be set. For instance, if a search
    * finds 1000 matches and the maximum rows per page is set to 10, the
    * first request will bring back the first 10 rows. The user can then ask
    * for the next page of results. If the maximum number of pages is set to
    * 5, the user can request the next 4 pages. This gives them at most 50
    * of the 1000 matches.
    *
    * @return       the maximum number of rows which can be returned in a page
    */
   public int getMaxRowsPerPage()
   {
      return m_maxRowsPerPage;
   }

   /**
    * Set the maximum number of rows which can be returned in a page.
    * <p>
    * Users may perform broad searches in an attempt to locate information.
    * This may result in large result sets which negatively impact server
    * performance. The impact of large searches can be minimized by defining
    * the number of rows which can be returned in a result page. The maximum
    * number of result pages can also be set. For instance, if a search
    * finds 1000 matches and the maximum rows per page is set to 10, the
    * first request will bring back the first 10 rows. The user can then ask
    * for the next page of results. If the maximum number of pages is set to
    * 5, the user can request the next 4 pages. This gives them at most 50
    * of the 1000 matches.
    *
    * @param max    the maximum number of rows which can be returned in a page
    * @see    #setMaxPages
    */
   public void setMaxRowsPerPage(int max)
   {
      m_maxRowsPerPage = max;
   }

   /**
    * Get the maximum number of pages which can be returned for multi-page
    * requests.
    * <p>
    * Users may perform broad searches in an attempt to locate information.
    * This may result in large result sets which negatively impact server
    * performance. The impact of large searches can be minimized by defining
    * the number of rows which can be returned in a result page. The maximum
    * number of result pages can also be set. For instance, if a search
    * finds 1000 matches and the maximum rows per page is set to 10, the
    * first request will bring back the first 10 rows. The user can then ask
    * for the next page of results. If the maximum number of pages is set to
    * 5, the user can request the next 4 pages. This gives them at most 50
    * of the 1000 matches.
    *
    * @return       the maximum number of rows which can be returned in a page
    */
   public int getMaxPages()
   {
      return m_maxPages;
   }

   /**
    * Set the maximum number of pages which can be returned for multi-page
    * requests.
    * <p>
    * Users may perform broad searches in an attempt to locate information.
    * This may result in large result sets which negatively impact server
    * performance. The impact of large searches can be minimized by defining
    * the number of rows which can be returned in a result page. The maximum
    * number of result pages can also be set. For instance, if a search
    * finds 1000 matches and the maximum rows per page is set to 10, the
    * first request will bring back the first 10 rows. The user can then ask
    * for the next page of results. If the maximum number of pages is set to
    * 5, the user can request the next 4 pages. This gives them at most 50
    * of the 1000 matches.
    *
    * @param max    the maximum number of pages which can be returned
    * @see    #setMaxRowsPerPage
    */
   public void setMaxPages(int max)
   {
      m_maxPages = max;
   }

   /**
    * Get the maximum number of page links allowed to be displayed
    * on a page.
    * <p>
    * Instead of returning potentially hundreds of page links
    * on a page, we limit the number of links to this value.
    * The links displayed are adjusted relative to the current
    * page. Example if the users current page is 9 and
    * the total pages is over 13 and the max is set to 10 then
    * links for pages 4 thru 13 would be displayed.
    *
    * @return this can be any integer (-1, etc)?
    */
   public int getMaxPageLinks()
   {
      return m_maxPageLinks;
   }


   /**
   * Set the maximum number of page links allowed to be displayed
   * on a page.
   * <p>
   * Instead of returning potentially hundreds of page links
   * on a page, we limit the number of links to this value.
   * The links displayed are adjusted relative to the current
   * page. Example if the users current page is 9 and
   * the total pages is over 13 and the max is set to 10 then
   * links for pages 4 thru 13 would be displayed.
   *
   * @param max  the max number of page links to be displayed
   */
   public void setMaxPageLinks(int max)
   {
      m_maxPageLinks = max;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param pager a valid PSResultPager. If null, a IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( PSResultPager pager )
   {
      copyFrom((PSComponent) pager );
      // assume page is in valid state
      m_maxRowsPerPage = pager.getMaxRowsPerPage();
      m_maxPages = pager.getMaxPages();
      m_maxPageLinks = pager.getMaxPageLinks();
   }



   /* **************   IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXResultPager XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *   &lt;!--
    *       PSXResultPager defines how result pages will be generated to
    *                    allow for partial result set processing. For instance, a request
    *                    may generate 100 rows of result data. It may be useful to limit
    *                    this, allow 10 rows per page, etc.
    *   --&gt;
    *   &lt;!ELEMENT PSXResultPager (maxRowsPerPage?, maxPages?)&gt;
    *
    *   &lt;!--
    *       the maximum number of rows which can be returned in a page.
    *
    *       Users may perform broad searches in an attempt to locate
    *       information. This may result in large result sets which
    *       negatively impact server performance. The impact of large
    *       searches can be minimized by defining the number of rows which
    *       can be returned in a result page. The maximum number of result
    *       pages can also be set. For instance, if a search finds 1000
    *       matches and the maximum rows per page is set to 10, the first
    *       request will bring back the first 10 rows. The user can then ask
    *       for the next page of results. If the maximum number of pages is
    *       set to 5, the user can request the next 4 pages. This gives them
    *       at most 50 of the 1000 matches.
    *   --&gt;
    *   &lt;!ELEMENT maxRowsPerPage  (#PCDATA)&gt;
    *
    *   &lt;!--
    *       the maximum number of pages which can be returned for multi-page
    *       requests. See maxRowsPerPage for more information on this.
    *   --&gt;
    *   &lt;!ELEMENT maxPages        (#PCDATA)&gt;
    *
    *   &lt;!--
    *       the maximum number of page links to be displayed in the pager links
    *       navigation.
    *   --&gt;
    *   &lt;!ELEMENT maxPageLinks       (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXResultPager XML element node
    */
   public Element toXml(Document doc)
   {
      //make sure that given settings make sence
      fixupPagerSettings();

      Element root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      PSXmlDocumentBuilder.addElement(   doc, root, "maxRowsPerPage",
         String.valueOf(m_maxRowsPerPage));

      PSXmlDocumentBuilder.addElement(   doc, root, "maxPages",
         String.valueOf(m_maxPages));

      PSXmlDocumentBuilder.addElement(   doc, root, "maxPageLinks",
         String.valueOf(m_maxPageLinks));

      return root;
   }

   /**
    * This method is called to populate a PSResultPager Java object
    * from a PSXResultPager XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception    PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXResults
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
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
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      sTemp = tree.getElementData("maxRowsPerPage");
      if (sTemp == null)
         m_maxRowsPerPage = DEFAULT_MAX_ROWS_PER_PAGE;
      else {
         try {
            m_maxRowsPerPage = Integer.parseInt(sTemp);
         } catch (NumberFormatException e) {
            Object[] args = { ms_NodeType, "maxRowsPerPage", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      sTemp = tree.getElementData("maxPages");
      if (sTemp == null)
         m_maxPages = DEFAULT_MAX_PAGES;
      else {
         try {
            m_maxPages = Integer.parseInt(sTemp);
         } catch (NumberFormatException e) {
            Object[] args = { ms_NodeType, "maxPages", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      sTemp = tree.getElementData("maxPageLinks");
      if (sTemp == null)
         m_maxPageLinks = DEFAULT_MAX_PAGE_LINKS;
      else {
         try {
            m_maxPageLinks = Integer.parseInt(sTemp);
         } catch (NumberFormatException e) {
            Object[] args = { ms_NodeType, "maxPageLinks", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      //make sure that given settings make sence
      fixupPagerSettings();
   }

   /**
    * makes sure that given pager settings actually make sence
    * and if they don't then makes necessary corrections.
    * ie: it doesn't make sence to have more page links than pages..
   */
   private void fixupPagerSettings()
   {
      if (m_maxPageLinks > 0 && m_maxPages > 0 && m_maxPages < m_maxPageLinks)
         m_maxPageLinks = m_maxPages;
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
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSResultPager)) return false;
      if (!super.equals(o)) return false;
      PSResultPager that = (PSResultPager) o;
      return m_maxRowsPerPage == that.m_maxRowsPerPage &&
              m_maxPages == that.m_maxPages &&
              m_maxPageLinks == that.m_maxPageLinks;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_maxRowsPerPage, m_maxPages, m_maxPageLinks);
   }

   private static final int   DEFAULT_MAX_ROWS_PER_PAGE   = -1;
   private static final int   DEFAULT_MAX_PAGES            = -1;
   private static final int   DEFAULT_MAX_PAGE_LINKS = 10;


   // NOTE: when adding members, be sure to update the copyFrom method
   private int m_maxRowsPerPage   = DEFAULT_MAX_ROWS_PER_PAGE;
   private int m_maxPages         = DEFAULT_MAX_PAGES;
   private int m_maxPageLinks    = DEFAULT_MAX_PAGE_LINKS;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXResultPager";
}

