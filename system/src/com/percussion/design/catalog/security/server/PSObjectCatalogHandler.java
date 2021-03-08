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

package com.percussion.design.catalog.security.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.design.catalog.PSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.IPSSecurityProviderMetaData;
import com.percussion.server.PSRequest;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.Subject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSObjectCatalogHandler class implements cataloging of
 * objects. This request type is used to locate the objects defined in the
 * specified cataloger.
 * <p>
 * Object catalog requests are sent to the server using the
 * PSXSecurityObjectCatalog XML document. Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityObjectCatalog (catalogerName, catalogerType, 
 *       filter?, objectType*)&gt;
 *
 *     &lt;!--
 *        the name by which the cataloger should be referenced. 
 *      --&gt;
 *     &lt;!ELEMENT catalogerName (#PCDATA)&gt;
 *     
 *     &lt;!--
 *        the type of the cataloger, which when combined with the name forms a
 *        unique reference 
 *      --&gt;
 *     &lt;!ELEMENT catalogerType (#PCDATA)&gt;
 *
 *    &lt;--
 *       a filter to use for locating matches. The filter condition must
 *       use the SQL LIKE pattern matching syntax. Use _ to match a
 *       single character and % to match a string of length 0 or more.
 *       Multiple conditions can be included by delimiting them with a semi-
 *       colon. The delimiter can be escaped with itself. For example, to
 *       obtain all names that begin with a or b, set this element to the
 *       following value: a%;b%. An empty filter will match nothing.
 *     --&gt;
 *    &lt;!ELEMENT filter          (#PCDATA)&gt;
 *
 *    &lt;--
 *       the type of object to locate. By specifying multiple objectType
 *       elements, multiple object types can be searched for.
 *     --&gt;
 *    &lt;!ELEMENT objectType       (#PCDATA)&gt;
 * </pre>
 *
 * The PSXSecurityObjectCatalogResults XML document is sent as the response.
 * Its definition is as follows:
 * <pre>
 *    &lt;!ELEMENT PSXSecurityObjectCatalogResults (catalogerName, 
 *       catalogerType, Object*)&gt;
 *
 *     &lt;!--
 *        the name by which the cataloger should be referenced. 
 *      --&gt;
 *     &lt;!ELEMENT catalogerName (#PCDATA)&gt;
 *     
 *     &lt;!--
 *        the type of the cataloger, which when combined with the name forms a
 *        unique reference 
 *      --&gt;
 *     &lt;!ELEMENT catalogerType (#PCDATA)&gt;
 *
 *    &lt;!ELEMENT Object           (name)&gt;
 *
 *    &lt;--
 *       type - the type of security object this represents.
 *     --&gt;
 *    &lt;!ATTLIST Object
 *       type        CDATA          #REQUIRED
 *    &gt;
 *
 *    &lt;-- the name of the object.
 *     --&gt;
 *    &lt;!ELEMENT name             (#PCDATA)&gt;
 * </pre>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSObjectCatalogHandler
      extends PSCatalogRequestHandler
      implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSObjectCatalogHandler()
   {
      super();
   }


   /* ********  IPSCatalogRequestHandler Interface Implementation ******** */

   /**
    * Get the request type(s) (XML document types) supported by this
    * handler.
    *
    * @return     the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { ms_RequestDTD };
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process the catalog request. This uses the XML document sent as the
    * input data. The results are written to the specified output
    * stream using the appropriate XML document format.
    *
    * @param   request     the request object containing all context
    *                      data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      Document doc = request.getInputDocument();
      Element root = null;
      if ((doc == null) || ((root = doc.getDocumentElement()) == null))
      {
         Object[] args = { ms_RequestCategory, ms_RequestType, ms_RequestDTD };
         createErrorResponse( request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      /* verify this is the appropriate request type */
      if (!ms_RequestDTD.equals(root.getTagName()))
      {
         Object[] args = { ms_RequestDTD, root.getTagName() };
         createErrorResponse( request, new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      String catalogerName = tree.getElementData("catalogerName");
      String catalogerType = tree.getElementData("catalogerType");
      String filterPattern = tree.getElementData("filter");

      /* parse the objectType string */
      boolean findUsers = false;
      boolean findGroups = false;
      List<String> typeList = new ArrayList<>();
      while (tree.getNextElement("objectType", true, false) != null)
      {
         String type = tree.getElementData((Element)tree.getCurrent());
         typeList.add(type);
         if (IPSSecurityProviderMetaData.OBJECT_TYPE_USER.equals(type))
            findUsers = true;
         else if (IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP.equals(type))
            findGroups = true;
      }

      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc, ms_RequestDTD + "Results");

      if (catalogerName != null)
      {
         PSXmlDocumentBuilder.addElement(retDoc, root, "catalogerName",
               catalogerName);
      }
      
      if (catalogerType != null)
      {
         PSXmlDocumentBuilder.addElement(retDoc, root, "catalogerType",
               catalogerName);
      }

      if (filterPattern != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "filter", filterPattern);

      if (typeList != null)
      {
         for (String objectType : typeList)
         {
            PSXmlDocumentBuilder.addElement(retDoc, root, "objectType",
                  objectType);
         }
      }


      try
      {
         List<String> names = null;
         if (filterPattern != null)
            names = Arrays.asList(filterPattern.split(FILTER_PATTERN_DELIM));

         if (findUsers)
            addUsers(retDoc, root, names, catalogerName, catalogerType);
         
         if (findGroups)
            addGroups(retDoc, root, names, catalogerName, catalogerType);

         /* and send the result to the caller */
         sendXmlData(request, retDoc);
      }
      catch (Exception e)
      {
         createErrorResponse(request, e);
      }
   }

   /**
    * Adds an element for each user found using the supplied pattern list
    * 
    * @param retDoc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.
    * @param names The list of patterns to use, may be <code>null</code> or 
    * empty.
    * @param catalogerName The name of the cataloger, assumed not
    * <code>null</code> or empty.
    * @param catalogerType The type of the cataloger, assumed not
    * <code>null</code> or empty.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   private void addUsers(Document retDoc, Element root, List<String> names,
      String catalogerName, String catalogerType)
      throws PSSecurityCatalogException
   {
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      for (Subject subject : roleMgr.findUsers(names, catalogerName, 
         catalogerType))
      {
         Element node =
            PSXmlDocumentBuilder.addEmptyElement( retDoc, root, "Object");

         node.setAttribute("type", 
            IPSSecurityProviderMetaData.OBJECT_TYPE_USER);

         PSXmlDocumentBuilder.addElement(retDoc, node, "name", 
            PSJaasUtils.subjectToPrincipal(subject).getName());
      }
   }
   
   /**
    * Adds an element for each group found using the supplied pattern list
    * 
    * @param retDoc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.
    * @param names The list of patterns to use, may be <code>null</code> or 
    * empty.
    * @param catalogerName The name of the cataloger, assumed not
    * <code>null</code> or empty.
    * @param catalogerType The type of the cataloger, assumed not
    * <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   private void addGroups(Document retDoc, Element root, List<String> names, 
      String catalogerName, String catalogerType) throws Exception
   {
      if (names == null || names.isEmpty())
         addGroups(retDoc, root, (String)null, catalogerName, catalogerType);
      else
      {
         for (String name : names)
         {
            addGroups(retDoc, root, name, catalogerName, catalogerType);
         }
      }
   }
   

   /**
    * Adds an element for each group found using the supplied pattern
    * 
    * @param retDoc The doc to use, assumed not <code>null</code>.
    * @param root The root element to append to, assumed not <code>null</code>.
    * @param pattern The pattern to use, may be <code>null</code> or empty.
    * @param catalogerName The name of the cataloger, assumed not
    * <code>null</code> or empty.
    * @param catalogerType The type of the cataloger, assumed not
    * <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   private void addGroups(Document retDoc, Element root, String pattern, 
      String catalogerName, String catalogerType) throws Exception
   {
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      for (Principal principal : 
         roleMgr.findGroups(pattern, catalogerName, catalogerType))
      {
         Element node =
            PSXmlDocumentBuilder.addEmptyElement( retDoc, root, "Object");

         node.setAttribute("type", 
            IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP);

         PSXmlDocumentBuilder.addElement(retDoc, node, "name", 
            principal.getName());
      }
   }


   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }


   /**
    * This character is used to separate multiple search patterns supplied in
    * the filter element.
    */
   private static final String FILTER_PATTERN_DELIM = ";";

   private static final String ms_RequestCategory = "security";

   private static final String ms_RequestType = "Object";

   private static final String ms_RequestDTD = "PSXSecurityObjectCatalog";
}

