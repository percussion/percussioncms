/******************************************************************************
 *
 * [ PSMakeCERequest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.extensions.testing;

import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSEditorDocumentBuilder;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSPreviewCommandHandler;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.error.PSException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSStopwatch;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Rhythmyx extension used to test the content editor apps through internal
 * requests. This extension should be set on the dummy resource because it
 * ignores the result document supplied to the result document processor and
 * makes internal requests to the content editor resource specified in the input
 * xml document of the request.
 */
public class PSMakeCERequest extends PSDefaultExtension
      implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Makes an internal request to the content editor resource with the
    * parameters specified in input xml document of the request. Supports the
    * cms commands 'edit', 'preview', 'modify' and 'relate' only. It does not 
    * work for the requests to insert/query a child item of a content editor 
    * that has multiple complex children. The request's parameters are 
    * modified by this exit and are no longer valid once the exit has been 
    * run. Expects the input document to the request conform to the following 
    * dtd.
    * <br>
    * &lt;!ELEMENT ContentEditorItem(PSXParam+)&gt;
    * &lt;!-- The format of resource is 'appName/ceDataSetName' --&gt;
    * &lt;!ATTLIST ContentEditorItem
    *    Resource CDATA #REQUIRED&gt;
    * &lt;!ELEMENT PSXParam(Value+)&gt;
    * &lt;!ATTLIST PSXParam
    *    Name CDATA #REQUIRED&gt;
    * &lt;!ELEMENT Value(#PCDATA&gt;
    * <br>
    * The returned result document will have a child element
    * <code>ItemContent</code> under the root for the content editor item
    * inserted/updated/queried if the request is successful, otherwise it will
    * have only the root element <code>XML_ROOT_NODE</code>.
    * <br>
    * The supplied request may specify the parameter 
    * <code>tst_maximumTime</code> with a string that will be interpreted as 
    * <code>double</code> containing the maximum request time allowed. If the
    * supplied maximal request time is exceeded, a 
    * <code>PSExtensionProcessingException</code> will be thrown.
    *
    * @param params  the parameters for this extension, may be <code>null</code>
    *    or empty.  The params are ignored.
    * @param request the request context object, may not be <code>null</code>.
    * @param resultDoc the result XML document, may be <code>null</code>.
    *
    * @return the result document with root element <code>XML_ROOT_NODE</code>,
    *    never <code>null</code>.
    *
    * @throws PSExtensionProcessingException if the request does not have an
    *    input document or the document does not conform to the above dtd or 
    *    does not have the parameters required to make a request to ce 
    *    resource or has a command which is not supported. This exception is 
    *    also thrown if the maximal allowed request time is exceeded.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      Document doc = request.getInputDocument();
      String msg = null;
      if( doc == null || doc.getDocumentElement() == null ||
         !doc.getDocumentElement().getTagName().equals(XML_CE_ITEM_ROOT_NODE) )
      {
         msg = "Request must have an input document with root element " +
            XML_CE_ITEM_ROOT_NODE;
      }

      String ceResource = null;
      if(doc != null)
      {
         Element root = doc.getDocumentElement();
         if(root != null)
            ceResource = root.getAttribute(XML_CE_RESOURCE_ATTR);
         if(ceResource == null || ceResource.trim().length() == 0)
         {
            msg = "The input document must specify the content editor " +
               "resource to make request." ;
         }
      }

      if(msg != null)
      {
         IllegalArgumentException ex = new IllegalArgumentException(msg);
         throw new PSExtensionProcessingException( getClass().getName(), ex);
      }

      removeRequestParameters(request);

      //Get all the parameters specified in the input document.
      NodeList paramList = doc.getElementsByTagName(XML_PARAM_NODE);
      if(paramList != null)
      {
         for(int i=0; i<paramList.getLength(); i++)
         {
            Node param = paramList.item(i);
            NamedNodeMap attrs = param.getAttributes();

            if(attrs != null)
            {
               String paramName = null;
               Node paramNameAttr = attrs.getNamedItem(XML_PARAM_NAME_ATTR);
               if(paramNameAttr != null)
                  paramName = paramNameAttr.getNodeValue();

               if(paramName != null && paramName.trim().length() != 0)
               {
                  NodeList valueElements = param.getChildNodes();
                  if(valueElements != null)
                  {
                     List valueList = new ArrayList();
                     for(int j=0; j<valueElements.getLength(); j++)
                     {
                        Node value = valueElements.item(j);
                        if(value.getNodeName().equals(XML_PARAM_VALUE_NODE))
                           valueList.add(PSXmlTreeWalker.getElementData(value));
                     }
                     if(!valueList.isEmpty())
                     {
                        if(valueList.size() == 1)
                           request.setParameter(paramName, valueList.get(0));
                        else
                           request.setParameter(paramName, valueList);
                     }
                  }
               }
            }
         }
      }
      // clear input document
      request.setInputDocument(null);

      //create result document
      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(retDoc, XML_ROOT_NODE);

      //have parameters set on the request
      if(request.getParametersIterator().hasNext())
      {
         String command = request.getParameter(PSContentEditorHandler.COMMAND_PARAM_NAME);

         if(command == null ||
            !(command.equals(PSEditCommandHandler.COMMAND_NAME) ||
            command.equals(PSModifyCommandHandler.COMMAND_NAME) ||
            command.equals(PSPreviewCommandHandler.COMMAND_NAME) ||
            command.equals(PSRelationshipCommandHandler.COMMAND_NAME)) )
         {
            throw new PSExtensionProcessingException( getClass().getName(),
               new IllegalArgumentException(
               "document does not have supported commands"));
         }
         
         PSStopwatch stopWatch = new PSStopwatch();
         stopWatch.start();
         Document resDoc = makeInternalRequest(request, ceResource, command);
         stopWatch.stop();
         
         String maximumTime = request.getParameter("tst_maximumTime");
         if (maximumTime != null)
         {
            double max = Double.parseDouble(maximumTime);
            if (stopWatch.elapsed() > max)
               throw new PSExtensionProcessingException(0,
                  "maxumim request time exceeded");
         }
         
         if (resDoc != null)
         {
            PSXmlTreeWalker walker = new PSXmlTreeWalker(resDoc);
            Element el = walker.getNextElement(
               PSEditorDocumentBuilder.ITEM_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

            if (el != null)
            {
               /* Remove the child key attribute and action link list node as
                * they will have child row id, content id and revision id
                * details which will be different for each test and auto test
                * script results comparison fails.
                */
               String childKeyAttr =
                  el.getAttribute(PSEditorDocumentBuilder.CHILDKEY_ATTRIB);
               if( childKeyAttr != null && childKeyAttr.length() != 0)
                  el.removeAttribute(PSEditorDocumentBuilder.CHILDKEY_ATTRIB);

               NodeList linkNodes = el.getElementsByTagName(
                  PSEditorDocumentBuilder.ACTIONLINKS_NAME);
               if(linkNodes != null)
               {
                  for(int i=0; i<linkNodes.getLength(); i++)
                  {
                     Node node = linkNodes.item(i);
                     Node parent = node.getParentNode();
                     parent.removeChild(node);
                  }
               }

               PSXmlDocumentBuilder.copyTree(retDoc, root, el, true);
            }
         }
      }
      else
      {
         throw new PSExtensionProcessingException(
            getClass().getName(), new IllegalArgumentException(
            "Does not have parameters for making a CE Request") );
      }
      return retDoc;
   }

   /**
    * Makes an internal request to the specified resource and returns the result
    * document. If the <code>command</code> is <code>modify</code>, then
    * after making the request, it queries the resource to get the result
    * document. If the request is to insert data into complex child and
    * <code>sys_contentid</code> and <code>sys_revision</code> are not
    * provided, then parent item and child items are inserted. It does not work
    * for the requests to insert/query a child item of a content editor that has
    * multiple complex children.
    *
    * @param request the request context, assumed not to be <code>null</code>
    * @param resource the content editor resource, assumed not to be
    * <code>null</code> and expects in the form of 'appName/ceDataSetName'.
    * @param command the cms command, assumed not to be <code>null</code> and
    * one of the following commands <code>modify</code>, <code>edit</code> and
    * <code>preview</code>.
    *
    * @return the result document, may be <code>null</code> if it could not
    * fulfill the request.
    * @throws PSExtensionProcessingException if any exception happened while
    * making the internal request.
    */
   private Document makeInternalRequest(IPSRequestContext request,
      String resource, String command)
      throws PSExtensionProcessingException
   {
      IPSInternalRequest intRequest = request.getInternalRequest(resource);

      /* DEV NOTE: IPSServerErrors is an obfuscated class, but since we are
         referencing a static final variable, the compiler stores the value
         directly into the generated code, rather than a reference.  Therefore,
         the exit will continue to work in a obfuscated environment. */
      if (intRequest == null)
         throw new PSExtensionProcessingException(
            IPSServerErrors.REQUEST_HANDLER_NOT_FOUND,
            new Object[]{ "-internal request-", resource } );

      Document doc = null;
      try 
      {
         if (command.equals(PSModifyCommandHandler.COMMAND_NAME))
         {
            /* Get the parameters specified in the request and insert parent if
             * the request is to insert a child item without providing content
             * id and revision id to get the ids, otherwise perform the request
             * as specified.
             */
            String childID = request.getParameter(
               PSContentEditorHandler.CHILD_ID_PARAM_NAME);
            String action = request.getParameter(
               PSApplicationBuilder.REQUEST_TYPE_HTML_PARAMNAME);
            String contentID = request.getParameter(
               PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
            String revisionID = request.getParameter(
               PSContentEditorHandler.REVISION_ID_PARAM_NAME);

            if(childID != null && !childID.trim().equals("0") &&
               action.equals(PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT) &&
               (contentID == null || revisionID == null) )
            {
               //it is a child with no content id and revision id
               doc = insertParentAndChild(request, resource);
            }
            else
            {
               //perform the request specified
               intRequest.performUpdate();

               // get the parameters set by the internal request for making a
               // query request to send the result document.
               IPSRequestContext internalContext =
                  intRequest.getRequestContext();
               contentID = internalContext.getParameter(
                  PSContentEditorHandler.CONTENT_ID_PARAM_NAME );
               revisionID = internalContext.getParameter(
                  PSContentEditorHandler.REVISION_ID_PARAM_NAME );
               childID = internalContext.getParameter(
                  PSContentEditorHandler.CHILD_ID_PARAM_NAME, "0" );
               String childRowID = internalContext.getParameter(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
               String pageIdStr = request.getParameter(
                  PSContentEditorHandler.PAGE_ID_PARAM_NAME);

               if (contentID != null && revisionID != null)
               {
                  removeRequestParameters(request);

                  request.setParameter(
                     PSContentEditorHandler.CONTENT_ID_PARAM_NAME, contentID);
                  request.setParameter(
                     PSContentEditorHandler.REVISION_ID_PARAM_NAME, revisionID);
                  request.setParameter(
                     PSContentEditorHandler.CHILD_ID_PARAM_NAME, childID);
                  if (childRowID != null)
                  {
                     request.setParameter(
                        PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
                        childRowID);
                  }
                  if (childID != null && !childID.trim().equals("0"))
                  {
                     /*
                      * For the modify handler odd page id's are supplied. The
                      * edit handler needs the even page id for that 
                      * (incremented by one).
                      */
                     if (pageIdStr == null || pageIdStr.trim().length() == 0)
                        throw new PSExtensionProcessingException(
                           getClass().getName(), new IllegalArgumentException(
                           "invalid pageId supplied (null or empty)"));
                     int pageId = Integer.parseInt(pageIdStr);
                     if (pageId % 2 == 0)
                        throw new PSExtensionProcessingException(
                           getClass().getName(), new IllegalArgumentException(
                           "invalid pageId supplied (must be an odd integer)"));
                        
                     request.setParameter(
                        PSContentEditorHandler.PAGE_ID_PARAM_NAME, 
                        Integer.toString(pageId++));
                  }
                  request.setParameter(
                     PSContentEditorHandler.COMMAND_PARAM_NAME,
                     PSEditCommandHandler.COMMAND_NAME);

                  doc = makeInternalRequest(request, resource,
                     PSEditCommandHandler.COMMAND_NAME);
               }
            }
         }
         else if (command.equals(PSRelationshipCommandHandler.COMMAND_NAME))
         {
            intRequest.makeRequest();
         }
         else
            doc = intRequest.getResultDoc();
      }
      catch(PSException e) {
         throw new PSExtensionProcessingException(getClass().getName(), e);
      }
      finally {
         intRequest.cleanUp();
      }

      return doc;
   }

   /**
    * Inserts the parent item before inserting the child item. This should be
    * called if there is a request to insert child item data without providing
    * <code>sys_contentid</code> and <code>sys_revision</code>.
    *
    * @param request the request context, assumed not to be <code>null</code>
    * @param resource the content editor resource, assumed not to be
    * <code>null</code> and expects in the form of 'appName/ceDataSetName'.
    *
    * @return the result document, may be <code>null</code> if
    * <code>sys_contentid</code> and <code>sys_revision</code> cannot be
    * retrieved after making a request to insert parent item.
    * @throws PSExtensionProcessingException if any exception happened while
    * making the internal request.
    */
   private Document insertParentAndChild(IPSRequestContext request,
      String resource)
      throws PSExtensionProcessingException
   {
      //store child parameters first
      Map childReqParams = removeRequestParameters(request);

      //make request to parent first
      request.setParameter(PSApplicationBuilder.REQUEST_TYPE_HTML_PARAMNAME,
         PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT);
      request.setParameter(PSContentEditorHandler.COMMAND_PARAM_NAME,
         PSModifyCommandHandler.COMMAND_NAME);
      
      //we always need a sys_title defined, make sure it is set
      request.setParameter("sys_title", childReqParams.get("sys_title"));

      IPSInternalRequest intRequest = request.getInternalRequest(resource);
      try {
         intRequest.performUpdate();

         // get the parameters set by the internal request for making a
         // query request to send the result document.
         IPSRequestContext internalContext = intRequest.getRequestContext();
         String contentID = internalContext.getParameter(
            PSContentEditorHandler.CONTENT_ID_PARAM_NAME);
         String revisionID = internalContext.getParameter(
            PSContentEditorHandler.REVISION_ID_PARAM_NAME);

         //make child request after inserting parent item successfully.
         if(contentID != null && revisionID != null)
         {
            Iterator params = childReqParams.entrySet().iterator();
            while(params.hasNext())
            {
               Map.Entry entry = (Map.Entry)params.next();
               internalContext.setParameter((String)entry.getKey(), entry.getValue());
            }

            return makeInternalRequest(internalContext, resource,
               PSModifyCommandHandler.COMMAND_NAME);
         }
      }
      catch(PSException e) {
         throw new PSExtensionProcessingException(getClass().getName(), e);
      }
      return null;
   }

   /**
    * Removes the parameters of the request.
    *
    * @param request the request context, assumed not to be <code>null</code>
    *
    * @return the map of removed parameters, never <code>null</code>,
    * may be empty.
    */
   private Map removeRequestParameters(IPSRequestContext request)
   {
      Map oldParams = new HashMap();
      Iterator parameters = request.getParametersIterator();
      while(parameters.hasNext())
      {
         Map.Entry entry = (Map.Entry)parameters.next();
         oldParams.put(entry.getKey(), entry.getValue());
      }

      parameters = oldParams.keySet().iterator();
      while(parameters.hasNext())
      {
         request.removeParameter( (String)parameters.next() );
      }

      return oldParams;
   }

   /**
    * The root element name for the request input document.
    */
   private static final String XML_CE_ITEM_ROOT_NODE = "ContentEditorItem";

   /**
    * The name of attribute for the root element to represent the content editor
    * resource.
    */
   private static final String XML_CE_RESOURCE_ATTR = "Resource";

   /**
    * The name of request parameter node.
    */
   private static final String XML_PARAM_NODE = "PSXParam";

   /**
    * The name of 'Name' attribute to represent the parameter name.
    */
   private static final String XML_PARAM_NAME_ATTR = "Name";

   /**
    * The name of parameter value node.
    */
   private static final String XML_PARAM_VALUE_NODE = "Value";

   /**
    * The root element name of the post-exit result document.
    */
   private static final String XML_ROOT_NODE = "PSXContentEditorRequestResults";
}
