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
package com.percussion.data;

import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;


/**
 * Implements URIResolver to resolve style sheet URIs.
 **/
public class PSUriResolver implements URIResolver
{
   /**
    * Implementation for resolving style sheet URI. Returns source object of
    * style sheet specified in passed in reference if it has protocol, otherwise
    * creates a source of file by resolving the reference from 'Rhythmyx' server
    * root. The passed in <code>base</code> is ignored, because the URI can be
    * resolved directly from <code>href</code>.
    *
    * @throws TransformerException if it can not resolve URI or error happens
    * while resolving.
    *
    * @see javax.xml.transform.URIResolver#resolve
    * resolve(String href, String base)
    */
   public Source resolve(String href, String base)
      throws TransformerException
   {
      //Deal with the internal requests here itself to avoid external requests
      //by XSLT processor
      int port = PSServer.getListenerPort();
      String baseUrl = "http://127.0.0.1";
      if(port != 80)
         baseUrl += ":" + port;
      
      if (href.toLowerCase().startsWith(baseUrl))
      {
         try
         {
            boolean domResult = false;
            
            PSInternalRequest iReq = null;
            PSRequest req = (PSRequest) PSRequestInfo
                  .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
            if(req == null)
               throw new IllegalStateException("req must not be null");
            iReq =  PSServer.getInternalRequest(href,
               req, null, false);
            //Internal request might be null for example, in the case of a 
            //request to loadable handler.
            if (iReq == null)
            {
               // In such a case deal the href normally
               PSUserSession sess = req.getUserSession();
               href = href + (href.contains("?") ? "&pssessionid=" : "?pssessionid=")
                  + sess.getId();
               return new StreamSource(href);
            }
            String pageExt = iReq.getRequest().getRequestPageExtension();
            IPSInternalRequestHandler handler = iReq
            .getInternalRequestHandler();
            if (pageExt.toLowerCase().equals(".xml")
                  || pageExt.toLowerCase().equals(".txt"))
            {
               //make sure these extension are not mapped to other mime types
               //explicitly
               
               if (handler instanceof PSQueryHandler)
               {
                  PSQueryHandler qh = (PSQueryHandler) handler;
                  PSDataSet dset = qh.getDataSet();
                  if (dset != null)
                  {
                     PSRequestor rqr = dset.getRequestor();
                     if (rqr.getMimeType(pageExt) == null)
                        domResult = true;
                  }
               }
            }

            if (domResult)
            {
               Document doc = iReq.getResultDoc();
               return new DOMSource(doc, href);
            }
            //Else return merged result
            else
            {
               ByteArrayInputStream is;
               ByteArrayOutputStream bos = null;
               
               try
               {   
                  if (handler instanceof PSQueryHandler)
                  {
                     bos = iReq.getMergedResult();
                     is = new ByteArrayInputStream(bos.toByteArray());
                  }
                  else
                  {
                     PSRequest areq = iReq.getRequest();
                     areq.setAllowsCloning(false);
                     iReq.makeRequest();
                     PSRequestStatistics stats = areq.getStatistics();
                     Document doc = PSXmlDocumentBuilder.createXmlDocument();
                     stats.toXml(doc);
                     String output = PSXmlDocumentBuilder.toString(doc);
                     is = new ByteArrayInputStream(output.getBytes());
                  }
                  return new StreamSource(is, href);
               }
               catch (PSAuthorizationException e)
               {
                  throw new TransformerException(e);
               }
               catch (PSAuthenticationFailedException e)
               {
                  throw new TransformerException(e);
               }
               finally
               {
                  if (bos != null)
                  {
                     try
                     {
                        bos.close();
                        bos = null;
                     }
                     catch (IOException e)
                     {
                     }
                  }
               }
            }
         }
         catch (PSInternalRequestCallException e)
         {
            throw new TransformerException(e);
         }
//         catch (MalformedURLException e)
//         {
//            throw new TransformerException(e);
//         }
//         catch (IOException e)
//         {
//            throw new TransformerException(e);
//         }
      }

      if (href.startsWith(HTTP_PROTOCOL) || href.startsWith(FILE_PROTOCOL))
      {
         try
         {
            href = PSServer.getResolvedURL(new URL(href)).toExternalForm();
         }
         catch (IOException e)
         {
            throw new TransformerException(e);
         }
         return new StreamSource(href);
      }
      
      if (href.startsWith(RELATIVE_URI))
      {
         String postFix = href.substring(RELATIVE_URI.length());
         
         if (postFix.indexOf(RELATIVE_PATH) != -1)
            throw new TransformerException(
               "Can not resolve the provided URI, wrong relative path: " + href);
         postFix = PSServer.getRxFile(postFix);

         return new StreamSource(FILE_PROTOCOL + postFix);
      }
      // When href is empty then return base as StreamSource if base is not
      // empty.
      if (href.length() < 1 && base != null && base.trim().length() > 1)
      {
         return new StreamSource(base);
      }
      String serverRoot = PSServer.getRequestRoot();
      String postFix = href.substring(serverRoot.length());
      if (PSServer.isCaseSensitiveURL())
      {
         if (href.startsWith(serverRoot))
            return new StreamSource(FILE_PROTOCOL + postFix);
      }
      else
      {
         String hrefRoot = href.substring(0, serverRoot.length());
         if (hrefRoot.equalsIgnoreCase(serverRoot))
            return new StreamSource(FILE_PROTOCOL + postFix);
      }

      throw new TransformerException(
         "Can not resolve the provided URI: " + href);
   }

   /**
    * Constant to indicate the path is relative.
    */
   public static final String RELATIVE_PATH = "..";

   /**
    * Constant to indicate the URI is relative.
    */
   public static final String RELATIVE_URI = RELATIVE_PATH + "/";


   /**
    * Constant to indicate http protocol. If a reference URL string starts
    * with this, it is a HTTP url stream.
    */
   public static final String HTTP_PROTOCOL  = "http:";

   /**
    * Constant to indicate file protocol. If a reference URL string starts
    * with this, it is a file stream.
    */
   public static final String FILE_PROTOCOL = "file:";
   
   /**
    * Constant to indicate that a file in in the rhythmyx root
    * directory
    */
    public static final String RXROOT_URI = FILE_PROTOCOL + "///Rhythmyx/";
}
