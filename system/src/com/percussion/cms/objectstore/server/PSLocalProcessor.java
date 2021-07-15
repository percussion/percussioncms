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
import com.percussion.cms.objectstore.PSProcessingStatistics;
import com.percussion.cms.objectstore.PSProcessorCommon;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSSqlException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSServer;
import com.percussion.util.PSCharSets;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * This class contains the know how to implement the necessary processing
 * when operating in the same JVM as the server. It uses internal requests
 * to accomplish its work. All internal requests are performed on behalf of
 * the user specified in the request. Because it is request dependent, it
 * must be set on the proxy for each new request. To enforce this, this class
 * will verify that a particular context is only used for a single request.
 *
 *
 * @todo: add check to prevent accidental use across requests
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSLocalProcessor extends PSProcessorCommon
{
   /**
    * Creates a processor that can fulfill database operation requests locally
    * on the Rhythmyx server for the duration of a single request. Implementors
    * should not instantiate this class directly but should use the {@link
    * com.percussion.cms.objectstore.PSProcessorProxy PSProcessorProxy} class.
    * <p>See {@link PSServerProcessor#PSServerProcessor(Map) base class}
    * for further details.
    *
    * @param req Never <code>null</code>. All work is performed as the user
    *    authenticated in req.
    */
   public PSLocalProcessor(PSRequest req, Map procConfig)
   {
      super(procConfig);
      if (null == req)
         throw new IllegalArgumentException("Request must be supplied.");

      m_req = req;
   }


   /**
    * Creates a processor that can fulfill database operation requests locally
    * on the Rhythmyx server for the duration of a single request. Implementors
    * should not instantiate this class directly but should use the {@link
    * com.percussion.cms.objectstore.PSProcessorProxy PSProcessorProxy} class.
    * <p>See {@link PSServerProcessor#PSServerProcessor(Map) base class}
    * for further details.
    *
    * @param ctx Never <code>null</code>. All work is performed as the user
    *    authenticated in ctx.
    */
   public PSLocalProcessor(IPSRequestContext ctx, Map procConfig)
   {
      super(procConfig);
      if (null == ctx)
      {
         throw new IllegalArgumentException(
               "Request context must be supplied.");
      }

      m_ctx = ctx;
   }


   /**
    * See base class for details.
    * <li>For each entry in ids, create N html parameters whose name is the name
    *    of the entry key. The value of each instance should be the value of
    *    one of the entries in the associated collection.</li>
    * <li>Generate an internal request to the resource specified in
    *    loadResource.</li>
    */
   protected Document doLoad(String resourceName, Map ids)
      throws PSCmsException
   {
      if (resourceName==null || resourceName.trim().length() < 1)
         throw new UnsupportedOperationException(
            "Empty resource name is Not supported by this processor.");

      try
      {
         Map params = new HashMap();
         Iterator pairs = ids.keySet().iterator();
         while (pairs.hasNext())
         {
            String keyPartName = (String) pairs.next();
            String[] idSet = (String[]) ids.get(keyPartName);
            if ( idSet.length > 1 )
            {
               ArrayList l = new ArrayList();
               for (int i=0; i < idSet.length; i++)
                  l.add(idSet[i]);
               params.put(keyPartName, l);
            }
            else
               params.put( keyPartName, idSet[0]);
         }

         Document doc = null;
         PSInternalRequest ireq = getRequest(resourceName, params, null);
         IPSInternalResultHandler irh =
               (IPSInternalResultHandler) ireq.getInternalRequestHandler();
         /* Multipart components may use stylesheets to generate their xml,
            therefore, we need to make sure the stylesheet gets executed, but
            if they don't we don't want to use the default stylesheet. */
         if (resourceName.toLowerCase().endsWith("xml"))
            doc = ireq.getResultDoc();
         else
         {
            ByteArrayOutputStream os = ireq.getMergedResult();
            doc = PSXmlDocumentBuilder.createXmlDocument( new StringReader(
                  new String(os.toByteArray(), PSCharSets.rxJavaEnc())), false);
         }

         // check for empty root - this means no rows returned, so return a
         // null doc
         Document resultDoc = null;
         if (doc != null)
         {
            Element root = doc.getDocumentElement();
            if (root != null)
            {
               // try to get first child
               PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
               Element child = tree.getNextElement(
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
               if (child != null)
                  resultDoc = doc;
            }
         }

         return resultDoc;
      }
      catch (PSInternalRequestCallException irce )
      {
         throw new PSCmsException(irce.getErrorCode(),
               irce.getErrorArguments());
      }
      catch (UnsupportedEncodingException uee)
      {
         //this should never happen unless we change our default encoding
         throw new RuntimeException(uee.getLocalizedMessage());
      }
      catch (IOException ioe)
      {
         //this should never happen because we are dealing w/ byte arrays
         throw new RuntimeException(ioe.getLocalizedMessage());
      }
      catch (SAXException se)
      {
         String[] args =
         {
            resourceName,
            se.getLocalizedMessage(),
            se.getException().getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.XML_PARSING_ERROR, args);
      }
   }

   //see interface for description
   protected int doDelete(String resourceName, Map ids)
      throws PSCmsException
   {
      if (resourceName==null || resourceName.trim().length() < 1)
         throw new UnsupportedOperationException(
            "Empty resource name is Not supported by this processor.");

      try
      {
         Map params = new HashMap();
         Iterator pairs = ids.keySet().iterator();
         while (pairs.hasNext())
         {
            String keyPartName = (String) pairs.next();
            String[] idSet = (String[]) ids.get(keyPartName);
            if ( idSet.length > 1 )
            {
               ArrayList l = new ArrayList();
               for (int i=0; i < idSet.length; i++)
                  l.add(idSet[i]);
               params.put(keyPartName, l);
            }
            else
               params.put( keyPartName, idSet[0]);
         }
         PSInternalRequest ireq = getRequest(resourceName, params, null);
         PSRequest req = ireq.getRequest();
         req.setAllowsCloning(false);
         ireq.performUpdate();
         return req.getStatistics().getRowsDeleted();
      }
      catch (PSInternalRequestCallException irce )
      {
         throw new PSCmsException(irce.getErrorCode(),
               irce.getErrorArguments());
      }
      catch (PSAuthorizationException ae )
      {
         throw new PSCmsException(ae.getErrorCode(),
               ae.getErrorArguments());
      }
      catch (PSAuthenticationFailedException afe )
      {
         throw new PSCmsException(afe.getErrorCode(),
               afe.getErrorArguments());
      }
   }


   /**
    * Gets the request handler for the specified resource.
    *
    * @param resourceName  The name of the handler, in the format app/resource.
    *    Assumed not <code>null</code>.
    *
    * @param params Extra html params to use for the request, may be
    *    <code>null</code>.
    *
    * @param input If not <code>null</code>, set as the input document on the
    *    request.
    *
    * @return Never <code>null</code>.
    *
    * @throws PSCmsException If the handler can't be found.
    */
   private PSInternalRequest getRequest(String resourceName, Map params,
         Document input)
      throws PSCmsException
   {
      PSInternalRequest req;
      if (null != m_req)
      {
         req = PSServer.getInternalRequest(resourceName, m_req, params, true);
         if (req != null)
            req.getRequest().setInputDocument(input);
      }
      else
      {
         req = PSServer.getInternalRequest(resourceName, m_ctx, params,
               true, input);
      }
      if ( null == req )
      {
         String[] args =
         {
            resourceName
         };
         throw new PSCmsException( IPSCmsErrors.REQUIRED_RESOURCE_MISSING,
               args);
      }
      PSRequest r = req.getRequest();
      r.setAllowsCloning(false);
      return req;
   }


   //see base class
   protected int[] doAllocateIds(String lookup, int count)
      throws PSCmsException
   {
      try
      {
         return PSIdGenerator.getNextIdBlock(lookup, count);
      }
      catch (SQLException se)
      {
         throw new PSCmsException(IPSCmsErrors.SQL_EXCEPTION_WRAPPER,
               PSSqlException.getFormattedExceptionText(se));
      }
   }


   //see base class for description
   protected PSProcessingStatistics doSave(String resourceName, Document input)
      throws PSCmsException
   {
      try
      {
         PSInternalRequest ireq = getRequest(resourceName, null, input);
         PSRequest req = ireq.getRequest();
         req.setAllowsCloning(false);
         ireq.performUpdate();
         PSRequestStatistics stats = req.getStatistics();
         return new PSProcessingStatistics(
               stats.getRowsInserted(),
               stats.getRowsUpdated(),
               stats.getRowsDeleted(),
               stats.getRowsSkipped(),
               stats.getRowsFailed());
      }
      catch (PSInternalRequestCallException irce )
      {
         throw new PSCmsException(irce.getErrorCode(),
               irce.getErrorArguments());
      }
      catch (PSAuthorizationException ae )
      {
         throw new PSCmsException(ae.getErrorCode(),
               ae.getErrorArguments());
      }
      catch (PSAuthenticationFailedException afe )
      {
         throw new PSCmsException(afe.getErrorCode(),
               afe.getErrorArguments());
      }
   }


   /**
    * Either this member or m_ctx will be valid and the other <code>null
    * </code>. It is set in the ctor, then never changed after that.
    * Used when generating requests. All requests are performed on behalf
    * of the authenticated user associated with this request.
    */
   private PSRequest m_req = null;

   /**
    * See {@link #m_req} for details.
    */
   private IPSRequestContext m_ctx = null;
}
