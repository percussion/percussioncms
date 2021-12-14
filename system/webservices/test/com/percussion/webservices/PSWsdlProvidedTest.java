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
