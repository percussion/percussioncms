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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A link to a navigation image. All Navigation images under a Navon w3il have
 * image links.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavImageLink extends PSNavLink
{

   private static final Logger log = LogManager.getLogger(PSNavImageLink.class);
   /**
    * Construct a image link from a relationship.
    * 
    * @param req the parent request contrxt
    * @param relation an Active Assembly relationship between the Navon and the
    *           desired NavImage item.
    * @throws PSNavException when any error occurs.
    */
   public PSNavImageLink(IPSRequestContext req, PSAaRelationship relation)
         throws PSNavException
   {
      super();

      PSContentTypeVariant variant = m_config.getImageInfoVariant();

      super.buildLinkFromRelationship(req, relation, variant, false);

      //      PSLocator loc = new PSLocator(this.contentId);
      //      PSComponentSummary summary = PSNavUtil.getItemSummary(req, loc);
      //      Document ddoc =
      //         PSNavUtil.getVariantDocument(
      //            req,
      //            variant,
      //            summary.getCurrentLocator());
      //      String select =
      //         PSNavUtil.getFieldValueFromXML(
      //            ddoc,
      //            config.getPropertyString(PSNavConfig.NAVIMAGE_SELECTOR_FIELD));
      String select = getSelector(this.m_contentId);

      if (select != null && select.trim().length() > 0)
      {
         this.m_imageSelector = select;
      }
      else
      {
         this.m_imageSelector = null;
      }
   }

   /**
    * Gets the image color selector.
    * 
    * @return
    */
   public String getImageSelector()
   {
      return m_imageSelector;
   }

   /**
    * returns an XML element for the Image Link. This XML documnet will be added
    * to the NavTree XML document.
    * 
    * @param parentElem the containing Navon element in the NavTree XML
    *           document.
    * @return
    */
   public Element toXML(Element parentElem)
   {
      Document doc = parentElem.getOwnerDocument();
      Element elem = doc.createElement(XML_ELEM_IMAGELINK);
      super.toXML(elem);
      if (this.m_imageSelector != null)
         elem.setAttribute(XML_ATTR_SELECTOR, m_imageSelector);
      parentElem.appendChild(elem);
      return elem;
   }

   /**
    * Gets the color selector value from the database.
    * 
    * @param id the contentid of the NavImage item.
    * @return the color selector value or <code>null</code> if one does not
    *         exist.
    * @throws PSNavException
    */
   private String getSelector(int id) throws PSNavException
   {
      Connection conn = PSNavSQLUtils.connect();
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String selector = null;
      try
      {
         stmt = PSPreparedStatement.getPreparedStatement(conn, SQL_LOAD);
         stmt.setInt(1, id);
         rs = stmt.executeQuery();
         boolean valid = rs.next();
         if (valid)
         {
            selector = rs.getString(1);
         }
      }
      catch (Exception ex)
      {
         log.error("SQL Error", ex);
         throw new PSNavException(ex);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs);
      }

      return selector;
   }

   /**
    * The color selector for this link.
    */
   private String m_imageSelector = null;

   /**
    * Configuration instance.
    */
   private PSNavConfig m_config = PSNavConfig.getInstance();

   /**
    * SQL Statement for loading the NavImage item data
    */
   private static final String SQL_LOAD = "select nav.SELECTOR  from "
         + "RXS_CT_NAVIGATIONIMAGETRIPLE nav, CONTENTSTATUS cs where "
         + "cs.CONTENTID = nav.CONTENTID and cs.CURRENTREVISION = "
          + "nav.REVISIONID and cs.CONTENTID = ?";

   /**
    * XML Element name for the image link
    */
   public static final String XML_ELEM_IMAGELINK = "image-link";

   /**
    * XML attribute name for the color selector
    */
   public static final String XML_ATTR_SELECTOR = "selector";

}
