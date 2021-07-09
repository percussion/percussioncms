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
package com.percussion.cms.handlers;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSActiveAssemblerHandlerRequest;
import com.percussion.cms.objectstore.server.PSActiveAssemblerProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.error.PSStandaloneException;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Redirects all requests to the <code>PSActiveAssemblyProcessor</code>.
 */
public class PSActiveAssemblyRequestHandler implements IPSLoadableRequestHandler
{
   /*
    * Initializes the request methods, no configuration file is used.
    * See {@link IPSLoadableRequestHandler} class for more info.
    *
    * @throws PSServerException is never thrown.
    */
   public void init(Collection requestRoots, InputStream config)
      throws PSServerException
   {
      if (requestRoots == null || requestRoots.size() == 0)
         throw new IllegalArgumentException(
            "must provide at least one request root" );


      PSConsole.printMsg(HANDLER, "Initializing Active Assembly Handler");

      m_requestRoots = requestRoots;
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }

   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   /**
    *
    * @param request the request object containing all context data associated
    *    with the request, may not be <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      Document respDoc = null;
      PSResponse resp = request.getResponse();
      try
      {
         String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);

         PSActiveAssemblerHandlerRequest requestDoc = null;
         Document doc = request.getInputDocument();
         if (doc != null)
         {
            requestDoc = new PSActiveAssemblerHandlerRequest(
               doc.getDocumentElement());
         }
         else
         {
            String inputDoc = request.getParameter(INPUT_DOC);
            if (inputDoc != null)
            {
               ByteArrayInputStream is =
                  new ByteArrayInputStream(inputDoc.getBytes());
               doc = PSXmlDocumentBuilder.createXmlDocument(is, false);

               requestDoc = new PSActiveAssemblerHandlerRequest(
                  doc.getDocumentElement());
            }
         }

         if (command != null)
         {
            if(requestDoc != null)
            {
               PSActiveAssemblerProcessor processor = PSActiveAssemblerProcessor.getInstance();
               if (command.equals(INSERT))
               {
                  processor.insert(requestDoc.getOwner(), requestDoc.getDependents(),
                     requestDoc.getIndex());
               }
               else if (command.equals(UPDATE))
               {
                  processor.update(requestDoc.getOwner(), requestDoc.getDependents());
               }
               else if (command.equals(REORDER))
               {
                  processor.reorder(requestDoc.getOwner(), requestDoc.getDependents(),
                     requestDoc.getIndex());
               }
               else if (command.equals(DELETE))
               {
                  processor.delete(requestDoc.getOwner(), requestDoc.getDependents());
               }
               else
               {
                  // unknown command
                  throw new PSCmsException(
                     IPSCmsErrors.UNKNOWN_AA_COMMAND, command);
               }
            }
            else
            {
               if (command.equals(AA_REL_LOOKUP))
               {
                  processAaRelationshipLookup(request);
               }
               else
               {
                  // unknown command
                  throw new PSCmsException(
                     IPSCmsErrors.UNKNOWN_AA_COMMAND, command);
               }
            }
         }
         else
         {
            // missing command or input document parameter
            String requiredParams = IPSHtmlParameters.SYS_COMMAND + ", " +
               INPUT_DOC;
            throw new PSCmsException(
               IPSCmsErrors.MISSING_AA_PARAMETER, requiredParams);
         }
      }
      catch (Throwable e)
      {
         // create error response
         PSRequestException exception = null;
         if (e instanceof PSException)
            exception = new PSRequestException((PSException) e);
         else
         {
            exception = new PSRequestException(IPSCmsErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }

         respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element respEl = exception.toXml(respDoc);
         PSXmlDocumentBuilder.replaceRoot(respDoc, respEl);
         resp.setStatus(500);
      }

      if (respDoc != null)
         resp.setContent(respDoc);
   }

   /**
    * Processes the Active Assembly relationship lookup request. The response
    * will be an XMl document with sys_Lookup dtd. Each entry is the
    * relationship name and label for active assembly relationship.
    * @param request request object assumed not <code>null</code>.
    */
   private void processAaRelationshipLookup(PSRequest request)
   {
      PSResponse resp = request.getResponse();
      Document resDoc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         Element root = PSXmlDocumentBuilder.createRoot(resDoc, "sys_Lookup");
         Iterator iter = PSRelationshipCommandHandler.getRelationshipConfigs(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         PSRelationshipConfig config = null;
         while(iter.hasNext())
         {
            config = (PSRelationshipConfig)iter.next();
            Element entry = PSXmlDocumentBuilder.addElement(resDoc, root, "PSXEntry", null);
            PSXmlDocumentBuilder.addElement(resDoc, entry, "PSXDisplayText", config.getLabel());
            PSXmlDocumentBuilder.addElement(resDoc, entry, "Value", config.getName());
         }
         resp.setContent(resDoc);
      }
      catch(Throwable t)
      {
         resDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(resDoc, "Error");
         Element msg = PSXmlDocumentBuilder.addElement(
            resDoc, root, "Message", t.toString());
         PSXmlDocumentBuilder.replaceRoot(resDoc, msg);
         resp.setContent(resDoc);
         resp.setStatus(500);
      }
   }

   /**
    * Shutdown the request handler.
    */
   public void shutdown()
   {
      PSConsole.printMsg(HANDLER, "Shutting down Active Assembly Handler");
   }

   /**
    * A local exception used to format the error message.
    */
   private class PSRequestException extends PSStandaloneException
   {
      // see base class for description
      public PSRequestException(int msgCode, Object singleArg)
      {
         super(msgCode, singleArg);
      }

      // see base class for description
      public PSRequestException(int msgCode, Object[] arrayArgs)
      {
         super(msgCode, arrayArgs);
      }

      // see base class for description
      public PSRequestException(int msgCode)
      {
         super(msgCode);
      }

      // see base class for description
      public PSRequestException(PSException ex)
      {
         super(ex);
      }

      // see base class for description
      protected String getResourceBundleBaseName()
      {
         return "com.percussion.error.PSErrorStringBundle";
      }

      // see base class for description
      protected String getXmlNodeName()
      {
         return "PSXRequestException";
      }
   }



   /**
    * Name of this handler.
    */
   public static final String HANDLER = "AAHandler";

   /**
    * The command parameter expected for inserts.
    */
   public static final String INSERT = "insert";

   /**
    * The command parameter expected for updates.
    */
   public static final String UPDATE = "update";

   /**
    * The command parameter expected for reorders.
    */
   public static final String REORDER = "reorder";

   /**
    * The command parameter expected for deletes.
    */
   public static final String DELETE = "delete";

   /**
    * The HTML parameter that provides the input document. The document must
    * confor to the PSXActiveAssemblerHandlerRequest.dtd.
    */
   public static final String INPUT_DOC = "inputdoc";

   /**
    * The command parameter for active assembly relationship type lookups.
    */
   public static final String AA_REL_LOOKUP = "aarellookup";

   /**
    * Storage for the request roots, initialized in <code>init()</code>, never
    * <code>null</code>, empty or modified after that. A list of
    * <code>String</code> objects.
    */
   private Collection m_requestRoots = null;

}
