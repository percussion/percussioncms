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
package com.percussion.services.utils.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Converts to/from enums.
 * The idea is taken from
 * http://brondsema.net/blog/index.php/2005/09/30/jsf_and_java_5_0_enums.
 *
 * @author Andriy Palamarchuk
 */
public class PSEnumTypeConverter implements Converter
{

   // see base
   @SuppressWarnings("unchecked")
   public Object getAsObject(FacesContext context, UIComponent component,
         String value)
   {
      final Class enumType =
            component.getValueBinding("value").getType(context);
      return Enum.valueOf(enumType, value);
   }

   // see base
   public String getAsString(FacesContext context, UIComponent component,
         Object object)
   {
      if (object == null)
      {
         return null;
      }
      assert object instanceof Enum;
      return object.toString();
   }
}
