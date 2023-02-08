/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.util.PSPreparedStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
    * @param req the parent request context
    * @param relation an Active Assembly relationship between the Navon and the
    *           desired NavImage item.
    * @throws PSNavException when any error occurs.
    */
   public PSNavImageLink(IPSRequestContext req, PSAaRelationship relation)
         throws PSNavException
   {
      super();

      PSContentTypeTemplate variant = m_config.getNavImageInfoTemplate();

      super.buildLinkFromRelationship(req, relation, variant, false);

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
         log.error("SQL Error: {}", PSExceptionUtils.getMessageForLog(ex));
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
   private final PSNavConfig m_config = PSNavConfig.getInstance();

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
