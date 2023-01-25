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
package com.percussion.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Makes sure the WSDLs are provided by the Rhythmyx web services. 
 *
 * @author Andriy Palamarchuk
 */
public class PSWsdlProvidedTest extends PSTestBase
{
   public void testWebservicesWsdls() throws IOException
   {
      final String[] services =
            {"assembly", "content", "security", "system", "ui"};
      for (final String service : services)
      {
         checkWebserviceProvidesWSDL(service);

      }
   }

   /**
    * Throws an exception if the webservice does not provide its WSDL.
    * @param service the service name to test (without the "SOAP" suffix).
    * E.g. "assembly". Assumed not <code>null</code>. 
    * @throws IOException when reading service result fails.
    */
   private void checkWebserviceProvidesWSDL(final String service)
         throws IOException
   {
      final String validWsdlString =
            "=\"urn:www.percussion.com/6.0.0/" + service + "\"";
      final String endpoint = getEndpoint(service + "SOAP");
      final URL url = new URL(endpoint + "?wsdl");
      final BufferedReader in =
            new BufferedReader(new InputStreamReader(url.openStream()));
      try
      {
         String s;
         while ((s = in.readLine()) != null) 
         {
            if (s.contains(validWsdlString))
            {
               return;
            }
         }
      }
      finally
      {
         in.close();
      }

      fail("Could not find string '" + validWsdlString
            + "' in data returned from " + url);
   }
}
