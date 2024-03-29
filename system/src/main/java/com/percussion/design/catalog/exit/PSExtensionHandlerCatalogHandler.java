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

package com.percussion.design.catalog.exit;

import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionDefFactory;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements cataloging of extension handlers
 * installed on the server.
 * <p>
 * Extension handler catalog requests are sent to the server
 * using the PSXExtensionHandlerCatalog XML document. Its definition
 * is as follows:
 * <pre><code>
 *
 *    &lt;!ELEMENT PSXExtensionHandlerCatalog EMPTY&gt;
 *
 * <pre><code>
 *
 * The PSXExtensionHandlerCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * <pre><code>
 *
 *    &lt;!ELEMENT PSXExtensionHandlerCatalogResults   (IPSExtensionHandlerDef*)&gt;
 *
 * <pre><code>
 */
public class PSExtensionHandlerCatalogHandler implements IPSCatalogHandler
{
   /**
    * Constructs an instance of this handler. This is used primarily
    * by the cataloger.
    */
   public PSExtensionHandlerCatalogHandler()
   {
      super();
   }

   /**
    * Format the catalog request based upon the specified
    * request information. The request information for this
    * request type is:
    *
    * <table border="1">
    * <tr>
    *      <th>Key</th>
    *      <th>Value</th>
    *      <th>Required</th>
    *   </tr>
    * <tr>
    *      <td>RequestCategory</td>
    *      <td>exit</td>
    *      <td>yes</td>
    *   </tr>
    * <tr>
    *      <td>RequestType</td>
    *      <td>ExtensionHandler</td>
    *      <td>yes</td>
    *   </tr>
    * </table>
    *
    * @param   req         the request information
    *
    * @return               an XML document containing the appropriate
    *                        catalog request information
    */
   public org.w3c.dom.Document formatRequest(java.util.Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"exit".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid: exit or null");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"ExtensionHandler".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid: ExtensionHandler or null");
      }

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, "PSXExtensionHandlerCatalog");

      return reqDoc;
   }


   /**
    * Get an array containing the extension handlers installed
    * on the server. This is a convenience method which makes a
    * call to the cataloger's catalog method using the appropriate
    * properties for this request type.
    *
    * @param   cataloger      a cataloger containing a connection to
    *                           the Rhythmyx server we will catalog through
    *
    * @return                  an array containing the extension handlers
    *                           installed on the server.
    *
    * @exception   PSServerException
    *                           if the server is not responding.
    *
    * @exception   PSAuthenticationFailedException
    *                           if the credentials specified for the
    *                           server connection are invalid.
    *
    * @exception   PSAuthorizationException
    *                           if the user does not have designer or
    *                           administrator access to the server.
    *
    * @exception   IOException
    *                           if a communication error occurs while
    *                           processing the request
    */
   public static IPSExtensionDef[] getCatalog(PSCataloger cataloger)
      throws
         com.percussion.conn.PSServerException,
         com.percussion.security.PSAuthenticationFailedException,
         com.percussion.security.PSAuthorizationException,
         java.io.IOException
   {
      IPSExtensionDef[] ret = null;

      // create the properties
      java.util.Properties req = new java.util.Properties();

      req.put("RequestCategory", "exit");
      req.put("RequestType", "ExtensionHandler");

      // perform the catalog request
      Document doc = null;
      try{
         doc = cataloger.catalog(req);
      } catch (IllegalArgumentException e){
         throw new PSServerException(e);
      }

      /* store the extension handler definitions in a list.
       * The returned XML tree contains the standard root node
       * (which we can ignore) then each extension handler
       * (where each handler is a child of the root, but siblings to
       * each other). To walk the tree we can get the root node
       * create a walker for it. We can get the first child to get
       * the first extension handler then iterate siblings to get
       * all subsequent extension handlers.
       */
      Element root = doc.getDocumentElement();
      if (root != null)
      {
         List l = new ArrayList();
         PSXmlTreeWalker w = new PSXmlTreeWalker(doc);
         IPSExtensionDefFactory factory = new PSExtensionDefFactory();
         for (   Element e = w.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
                  | PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
               e != null;
               e = w.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS) )
         {
            try
            {
               l.add( factory.fromXml( e ));
            }
            catch (Exception exc)
            {
               throw new PSServerException(exc);
            }
         }

         // and convert the list to an array
         final int size = l.size();
         ret = new IPSExtensionDef[size];
         if (size > 0)
            l.toArray(ret);
      }
      else
      {   // create an empty one - no extension handlers!
         ret = new IPSExtensionDef[0];
      }

      return ret;
   }
}

