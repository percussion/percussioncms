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
package com.percussion.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * This interface simplifies making a request to a Rhythmyx application or 
 * resource that returns an XML document. It allows the user to set all the 
 * information to make a request to the server, it will construct the XML 
 * document out of the response from the server.
 */
public interface IPSRemoteRequester
{
   /**
    * Makes an http/s request to the specified resource, providing the key-
    * value pairs in the params map as html parameters. Expects that an
    * xml document will be returned.
    *
    * @param resource Never <code>null</code> or empty. Must be a full path
    *    to the target resource without the root path, e.g. app/res.xml. (assume
    *    the full path including the root is, /Rhythmyx/app/res.xml)
    *
    * @param params A set of name/value pairs. Each key is a String, while
    *    each value is either a String or a List of Strings. If a list
    *    is supplied, then an htlm param with the name of the key will
    *    be created for each entry.
    *
    * @param useSSL If <code>true</code>, the request will be made using a
    *    secure channel, otherwise std http will be used.
    *
    * @return The document representing the returned data, or null if no
    *    data was returned.
    *
    * @throws IOException If any problems occur while communicating with the
    *    server.
    *
    * @throws SAXException If the returned data is not parsable as an xml
    *    document.
    */
   public Document getDocument(String resource, Map params)
      throws IOException, SAXException;

   /**
    * Equivalent to calling {@link #getDocument(String,Map,boolean)
    * getDocument}.
    */
   public Document sendUpdate(String resource, Map params)
      throws IOException, SAXException;

   /**
    * Just like {@link #getDocument(String,Map,boolean) getDocument}, except
    * it sends a document.
    * 
    * @param doc The to be send document, it may not be <code>null</code>.
    */
   public Document sendUpdate(String resource, Document doc)
      throws IOException, SAXException;

   /**
    * Shuts down all open connections if there is any
    */
   public void shutdown();

}
