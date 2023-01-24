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

import com.percussion.HTTPClient.PSBinaryFileData;
import com.percussion.design.objectstore.PSLocator;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;


/**
 * This interface simplifies making a request to a Rhythmyx application or
 * resource that returns an XML document. It allows the user to set all the
 * information to make a request to the server, it will construct the XML
 * document out of the response from the server. Also includes methods
 * that handle binary data and can return bytes instead of an xml document.
 */
public interface IPSRemoteRequesterEx extends IPSRemoteRequester
{

   /**
    * Makes an http/s request to the specified binary resource, providing
    * the key-value pairs in the params map as html parameters. Expects that a
    * byte array will be returned.
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
    * @return The byte array representing the returned data, may be empty if
    * no data was returned
    *
    * @throws IOException If any problems occur while communicating with the
    *    server.
    */
   public byte[] getBinary(String resource, Map params)
      throws IOException;

   /**
    * Makes an http/s request to the specified binary update resource,
    * providing the key-value pairs in the params map as html parameters.
    *
    * @param the BinaryFileData array data that represents the binary being sent.
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
    * @return the <code>PSLocator</code> for this content item. May be <code>
    * null</code> if the locator could not be retrieved.
    *
    * @throws IOException If any problems occur while communicating with the
    *    server.
    */
   public PSLocator updateBinary(
      PSBinaryFileData[] files,
      String resource,
      Map params)
      throws IOException, SAXException;




}
