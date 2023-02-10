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
package com.percussion.data;

import com.percussion.server.PSServer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URL;


/**
 * Implements URIResolver to resolve style sheet URIs.
 * @deprecated Use PSCategoryResolver
 **/
@Deprecated
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
