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

package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.error.PSException;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.IPSWebServicesErrors;
import com.percussion.server.webservices.PSWebServicesBaseHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Loadable handler for getting shared, system and local content editor fields
 * from the server.
 */
public class PSCatalogServerObjectHandler implements IPSLoadableRequestHandler
{
   //see the interface.
   @SuppressWarnings("unused") 
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      //do nothing.
   }

   // see {@link com.percussion.server.IPSRequestHandler}
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      Document responseDoc = null;
      PSResponse response = request.getResponse();
      String catalogType = request.getRequestPage(false);

      try
      {
         validateAuthentication(request);

         if (catalogType.equalsIgnoreCase("ContentEditorFields"))
         {
            PSLocalCataloger cataloger = new  PSLocalCataloger(request);

            //by default, hidden and result only fields not included
            int controlFlags = 0;

            //presence of param activates behavior
            if (request.getParameter(IPSHtmlParameters.SYS_INCLUDEHIDDENFIELDS)
                  != null)
            {
               controlFlags =
                     controlFlags | PSLocalCataloger.FLAG_INCLUDE_HIDDEN;
            }
            if (request.getParameter(
                  IPSHtmlParameters.SYS_INCLUDERESULTONLYFIELDS) != null)
            {
               controlFlags =
                     controlFlags | PSLocalCataloger.FLAG_INCLUDE_RESULTONLY;
            }

            if (request.getParameter(
                  IPSHtmlParameters.SYS_RESTRICTFIELDSTOUSERCOMMUNITY) != null)
            {
               controlFlags = controlFlags | 
                  PSLocalCataloger.FLAG_RESTRICT_TOUSERCOMMUNITY;
            }

            if (request.getParameter(
                  IPSHtmlParameters.SYS_USERSEARCH) != null)
            {
               controlFlags =
                     controlFlags | PSLocalCataloger.FLAG_USER_SEARCH;
            }

            if (request.getParameter(
                  IPSHtmlParameters.SYS_CTYPESHIDEFROMMENU) != null)
            {
               controlFlags = controlFlags | 
                  PSLocalCataloger.FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU;
            }
            
            if (request.getParameter(
               IPSHtmlParameters.SYS_EXCLUDE_CHOICES) != null)
            {
               controlFlags =
                  controlFlags | PSLocalCataloger.FLAG_EXCLUDE_CHOICES;
            }            

            Set<String> fieldNames = new HashSet<>();
            Object obj = request.getParameters().get(
               IPSHtmlParameters.SYS_CE_FIELD_NAME);
            if (obj instanceof List)
            {
               List names = (List)obj;
               for (Object name : names)
               {
                  if (name != null)
                     fieldNames.add(name.toString());
               }
            }
            else if (obj != null)
            {
               fieldNames.add(obj.toString());
            }
            
            Element ceFieldElem = cataloger.getCEFieldXml(controlFlags, 
               fieldNames);
            responseDoc = ceFieldElem.getOwnerDocument();
         }
         else if (catalogType.equalsIgnoreCase("Relationship"))
         {
            responseDoc = processRelationship(request);
         }
         else if (catalogType.equalsIgnoreCase("RelationshipInfoSet"))
         {
            PSLocalCataloger cataloger = new  PSLocalCataloger(request);
            PSRelationshipInfoSet infoSet = cataloger.getRelationshipInfoSet();
            responseDoc = PSXmlDocumentBuilder.createXmlDocument();
            Element setEl = infoSet.toXml(responseDoc);
            PSXmlDocumentBuilder.replaceRoot(responseDoc, setEl);
         }
         else if (catalogType.equalsIgnoreCase("SearchConfig"))
         {
            PSLocalCataloger cataloger = new  PSLocalCataloger(request);
            PSSearchConfig config = cataloger.getSearchConfig();
            responseDoc = PSXmlDocumentBuilder.createXmlDocument();
            Element setEl = config.toXml(responseDoc);
            PSXmlDocumentBuilder.replaceRoot(responseDoc, setEl);
         }

         response.setContent(responseDoc);
      }
      catch (PSException e)
      {
         int code = e.getErrorCode();

         // check to see if this exception is really an authentication error
         if (code == IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION ||
             code == IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION ||
             code == IPSSecurityErrors.SESS_NOT_AUTHORIZED)
         {
            response.setResponseHeader(PSResponse.RHDR_WWW_AUTH,
                                       "Basic realm=\"\"");
            response.setStatus(IPSHttpErrors.HTTP_UNAUTHORIZED);
         }
         else
         {
            responseDoc = PSWebServicesBaseHandler.createResultResponseDoc(
               "failure", code, e.toString());
            response.setIsErrorResponse(true);
         }
      }
      catch(Exception e)
      {
            responseDoc = PSWebServicesBaseHandler.createResultResponseDoc(
               "failure", 9999, e.toString());
            response.setIsErrorResponse(true);
      }
   }


   /**
    * Validates the authentication for the given request.
    *
    * @param request The to be validated request object. Assume not
    *    <code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   private void validateAuthentication(PSRequest request)
      throws PSException
   {
      // make sure we have a valid session
      // this should throw an exception if not logged in
      // TODO: we should find a better way to do this
      String path = "sys_psxWebServices/login";

      PSInternalRequest iReq = PSServer.getInternalRequest(path,
                                                           request,
                                                           null,
                                                           true);
      if (iReq == null)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_INTERNAL_REQUEST_FAILED,
               path);
      }

      iReq.performUpdate();
   }

   /**
    * Process the relationship request of get-children, get-parents or
    * get-summaries.
    *
    * @param request The object which contains the parameters for this request.
    *    See <code>getComponentSummariesDoc</code> and <code>
    *    getSumaries</code> for a list of parameters.
    *    Assume not <code>null</code>.
    *
    * @return The result of the request, never <code>null</code>.
    *
    * @throws UnsupportedOperationException if the method in the request is
    *    not "getChildren", "getParents" or "getSummaries".
    *
    * @throws PSCmsException if an error occurs.
    */
   private Document processRelationship(PSRequest request)
      throws PSCmsException
   {
      String method = request.getParameter("method");
      Document respDoc = null;

      if (method.equalsIgnoreCase("getChildren") ||
          method.equalsIgnoreCase("getParents") )
      {
         respDoc = getComponentSummariesDoc(request, method);
      }
      else if (method.equalsIgnoreCase("getSummaries"))
      {
         respDoc = getSummaries(request);
      }
      else if (method.equalsIgnoreCase("getRelationships"))
      {
         respDoc = getRelationships(request);
      }
      else // unknown/unsupported method
      {
         throw new UnsupportedOperationException(
               "method \"" + method + "\" is not supported by this handler.");
      }

      return respDoc;
   }

   /**
    * This method can be used to get the relationships filtered by the supplied
    * filter.
    * 
    * @param request The request object, it must contains the following
    * parameters:
    * <TABLE BORDER="1">
    * <TR><TH>Parameter name</TH><TH>Description</TH></TR>
    * <TR><TD>relationshipFilter</TD><TD>The PSRelationshipFilter as xml</TD></TR>
    * </TABLE>
    * @return The document consisting of to xml of PSRelationshipSet object.
    * @throws PSCmsException if an error occurs.
    */
   private Document getRelationships(PSRequest request) throws PSCmsException
   {
      String rFilter = request.getParameter("relationshipFilter");
      PSRelationshipFilter filter = new PSRelationshipFilter();
      Document inputDoc;
      try
      {
         inputDoc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(rFilter), false);
         filter.fromXml(inputDoc.getDocumentElement());
      }
      catch (IOException e)
      {
         //Should not get here
         PSConsole.printMsg(this.getClass().getName(), e);
         Object[] args = new Object[]{HANDLER, e.getLocalizedMessage()};
         throw new PSCmsException(
            IPSCmsErrors.UNEXPECTED_CATALOG_ERROR,
            args);
      }
      catch (SAXException e)
      {
         // Should not get here
         PSConsole.printMsg(this.getClass().getName(), e);
         Object[] args = new Object[]{HANDLER, e.getLocalizedMessage()};
         throw new PSCmsException(
            IPSCmsErrors.UNEXPECTED_CATALOG_ERROR,
            args);
      }


      PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();

      PSRelationshipSet relSet = proxy.getRelationships(filter);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element relEl = relSet.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, relEl);

      return doc;

   }

   /**
    * Get the component summaries of children or parents for a locator, which
    * is specified in the <code>request</code> object as parameters.
    *
    * @param request The request object, it contains the following parameters:
    *   <TABLE BORDER="1">
    *   <TR><TH>Parameter name</TH><TH>Description</TH></TR>
    *   <TR><TD>id</TD><TD>The id of the locator</TD></TR>
    *   <TR><TD>revision</TD><TD>The revision of the locator</TD></TR>
    *   <TR><TD>relationshipType</TD><TD>The relationship type</TD></TR>
    *   </TABLE>
    *
    * @param method The method of the processing. If it is "getChildren", then
    *    get children of the locator; otherwise, get the parents of the locator.
    *    Assume it is not <code>null</code> or empty.
    *
    * @return The cataloged component summaries, never <code>null</code>, but
    *    may by empty.
    *
    * @throws PSCmsException if an error occurs.
    */
   private Document getComponentSummariesDoc(PSRequest request, String method)
      throws PSCmsException
   {
      String relationshipType = request.getParameter("relationshipType");
      String id = request.getParameter("id");
      String revision = request.getParameter("revision");
      PSLocator locator = new PSLocator(id, revision);

      PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();

      PSComponentSummary[] summaryArray;
      if (method.equalsIgnoreCase("getChildren"))
      {
         summaryArray = proxy.getChildren(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
            relationshipType, locator);
      }
      else // get the parents
      {
         summaryArray = proxy.getParents(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
            relationshipType, locator);
      }

      PSComponentSummaries summaries = new PSComponentSummaries(summaryArray);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element summariesEl = summaries.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, summariesEl);

      return doc;
   }

   /**
    * This method can be used to get the summaries for the owner or dependent
    * objects in the relationships filtered by the supplied filter.
    *
    * @param request The request object, it must contains the following parameters:
    *   <TABLE BORDER="1">
    *   <TR><TH>Parameter name</TH><TH>Description</TH></TR>
    *   <TR><TD>owner</TD><TD>specify <code>true</code> to get the summaries
    *    of owners of the relationships or specify <code>false</code> to
    *    get summaries of the dependents. </TD></TR>
    *   <TR><TD>relationshipFilter</TD><TD>The PSRelationshipFilter as xml</TD></TR>
    *   </TABLE>
    * @return The cataloged component summaries, never <code>null</code>, but
    *    may by empty.
    * @throws PSCmsException if an error occurs.
    */
   private Document getSummaries(PSRequest request)
    throws PSCmsException
   {
      boolean owner =
         Boolean.valueOf(request.getParameter("owner", "false")).booleanValue();
      String rFilter = request.getParameter("relationshipFilter");
      PSRelationshipFilter filter = new PSRelationshipFilter();
      Document inputDoc;
      try
      {
         inputDoc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(rFilter), false);
         filter.fromXml(inputDoc.getDocumentElement());
      }
      catch (IOException e)
      {
         //Should not get here
         PSConsole.printMsg(this.getClass().getName(), e);
         Object[] args = new Object[]{HANDLER, e.getLocalizedMessage()};
         throw new PSCmsException(
            IPSCmsErrors.UNEXPECTED_CATALOG_ERROR,
            args);
      }
      catch (SAXException e)
      {
         // Should not get here
         PSConsole.printMsg(this.getClass().getName(), e);
         Object[] args = new Object[]{HANDLER, e.getLocalizedMessage()};
         throw new PSCmsException(
            IPSCmsErrors.UNEXPECTED_CATALOG_ERROR,
            args);
      }


      PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();

      PSComponentSummaries summaries = proxy.getSummaries(filter, owner);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element summariesEl = summaries.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, summariesEl);

      return doc;

   }

   // see {@link com.percussion.server.IPSRequestHandler}
   public void shutdown()
   {
   }

   // see {@link com.percussion.server.IPSRootedHandler}
   public String getName()
   {
      return HANDLER;
   }

   // see {@link com.percussion.server.IPSRootedHandler}
   public Iterator getRequestRoots()
   {
      return null;
   }

   /**
    * Name of the custom application for web services.
    */
   public static final String WEB_SERVICES_APP = "sys_psxWebServices";

   /**
    * Name of the handler.
    */
   private static final String HANDLER = "sys_ceFieldsCataloger";
}
