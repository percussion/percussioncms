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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.beanutils.Converter;
import org.junit.experimental.categories.Category;

import java.util.List;

/**
 * The converter base test provides generic test functionality and should
 * be extended by all converter tests.
 */
@Category(IntegrationTest.class)
public abstract class PSConverterTestBase extends ServletTestCase
{
   /**
    * This method converts the supplied server side source to a client object
    * and back for the specified server and client types.
    * 
    * @param serverType the type of the server side object, not 
    *    <code>null</code>.
    * @param clientType the type of the client side object, not 
    *    <code>null</code>.
    * @param source the source object to make a round trip convertion with,
    *    not <code>null</code>, must be of type <code>serverType</code>.
    * @return the round trip converted server side object, never 
    *    <code>null</code>, always of type <code>serverType</code>.
    * @throws PSTransformationException for any transformation error.
    */
   protected Object roundTripConversion(Class serverType, Class clientType, 
      Object source) throws PSTransformationException
   {
      if (serverType == null)
         throw new IllegalArgumentException("serverType cannot be null");
      
      if (clientType == null)
         throw new IllegalArgumentException("clientType cannot be null");
      
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (!source.getClass().getName().equals(serverType.getName()))
         throw new IllegalArgumentException(
            "source must be of type serverType");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert server to client object
      Converter converter = factory.getConverter(serverType);
      Object clientObject = converter.convert(clientType, source);
      
      // convert client to server object
      converter = factory.getConverter(clientType);
      return converter.convert(serverType, clientObject);
   }
   
   /**
    * Converts a (server side) list to its corresponding (client side) array,
    * then convert the array back to a list.
    * 
    * @param cz a array class, may not be <code>null</code>.
    * @param srcList a list of server side object, may not be <code>null</code>.
    * 
    * @return the round trip list, never <code>null</code>, but may be empty.
    * 
    * @throws Exception if an error occurs.
    */
   protected List roundTripListConversion(Class cz, List srcList) throws Exception
   {
      if (! cz.isArray())
         throw new IllegalArgumentException("cz must be an instance of array.");
      if (srcList == null)
         throw new IllegalArgumentException("srcList must not be null.");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      // convert from list to array
      Converter converter = factory.getConverter(cz);
      Object[] array = (Object[]) converter.convert(cz, srcList);
      
      // convert from array to list
      converter = factory.getConverter(List.class);
      List target = (List) converter.convert(List.class, array);
      
      return target;
   }
   
   /**
    * Get the next test id for the supplied type.
    * 
    * @param type the type for which to create the test id, not 
    *    <code>null</code>.
    * @return the next test id, never <code>null</code>.
    */
   protected static IPSGuid getNextId(PSTypeEnum type)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.createGuid(type);
   }
}

