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
package com.percussion.delivery.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.percussion.utils.date.PSConcurrentDateFormat;

import java.io.IOException;

/**
 * Custom date serializer to put the serialized date into a non numeric
 * format. Uses the date format of yyyy-MM-dd'T'HH:mm:ssZ
 * @author erikserating
 *
 */
public class PSCustomDateSerializer extends JsonSerializer<Object>
{

   private final PSConcurrentDateFormat formatter = new PSConcurrentDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
   
   /* (non-Javadoc)
    * @see org.codehaus.jackson.map.JsonSerializer#serialize(
    *    java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
    */
   @Override
   public void serialize(Object value, JsonGenerator gen,
            @SuppressWarnings("unused") SerializerProvider provider) throws
           IOException
   {
      String formattedDate = formatter.format(value);

      gen.writeString(formattedDate);
      
   }

}
