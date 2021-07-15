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

package com.percussion.extensions.cms;


import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Get the properties of a specified relationship in the following format:
 * <pre>
 * &lt;!ELEMENT prop (#PCDATA)>
 * &lt;!ATTLIST  prop name CDATA #REQUIRED>
 * &lt;!ELEMENT relProps (prop* )>
 * &lt;!ATTLIST  relProps rid CDATA #REQUIRED>
 * </pre>
 * Expected parameter in the request context is "sys_relationshipid", which
 * is the id of the specified relationship.
 */
public class PSGetRelationshipProps extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * See the class description for the expected input and return document.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String sRid = request.getParameter(IPSHtmlParameters.SYS_RELATIONSHIPID);
      
      Element props = doc.createElement(EL_PROPS);
      sRid = (sRid == null) ? "" : sRid;
      props.setAttribute(ATTR_RID, sRid);

      int rid;

      try
      {
         rid = Integer.parseInt(sRid);
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setRelationshipId(rid);
         filter.setCommunityFiltering(false);
         PSRelationshipSet rels = relProxy.getRelationships(filter);

         if (rels.size() > 0)
            doc = getRelationshipProps(doc, (PSRelationship)rels.get(0));
         else
            doc = getRelationshipProps(doc, null);
      }
      catch (NumberFormatException ne)
      {
         doc = getRelationshipProps(doc, null);
      }
      catch (Exception ex)
      {
         doc = getRelationshipProps(doc, null);
      }

      return doc;
   }

   /**
    * Create XML representation of the supplied relationship properties. The
    * XML format is described in the class description.
    *
    * @param doc the document used to create element, assumed not
    *   <code>null</code>.
    * @param rel the relationship object, it may be <code>null</code>.
    *
    * @return the XML document according to the given relationship object.
    */
   private Document getRelationshipProps(Document doc, PSRelationship rel)
   {
      Element props = doc.createElement(EL_PROPS);

      if (rel != null)
      {
         props.setAttribute(ATTR_RID, String.valueOf(rel.getId()));
         Set<Entry<String, String>> entries = rel.getUserProperties().entrySet();
         for (Entry<String, String> entry : entries)
         {
            Element prop = PSXmlDocumentBuilder.addElement(doc, props, EL_PROP,
                  entry.getValue());
            prop.setAttribute(ATTR_NAME, entry.getKey());
         }
      }
      else // create an empty element
      {
         props.setAttribute(ATTR_RID, "");
         Element prop = PSXmlDocumentBuilder.addElement(doc, props, EL_PROP, "");
         prop.setAttribute(ATTR_NAME, "");
      }

      PSXmlDocumentBuilder.replaceRoot(doc, props);

      return doc;
   }

   private static final String EL_PROPS = "relProps";
   private static final String EL_PROP = "prop";
   private static final String ATTR_RID = "rid";
   private static final String ATTR_NAME = "name";

}
