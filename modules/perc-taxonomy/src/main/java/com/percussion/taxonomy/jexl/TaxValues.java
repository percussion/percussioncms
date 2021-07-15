/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.taxonomy.jexl;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class TaxValues extends ArrayList<String>
{
   /**
    * An object that can handle both single and multiple taxonomy attribute
    * values It can be treated like a regular List for multiple values or
    * toString will output a comma separated list for multiple values.
    */
   private static final long serialVersionUID = 1L;

   public TaxValues()
   {
      super();
   }

   public TaxValues(String string)
   {
      super();
      this.add(string);
   }

   public boolean isMultiValued()
   {
      return this.size() > 1;
   }

   public String join(String delim)
   {
      return StringUtils.join(this, delim);
   }

   @Override
   public String toString()
   {
      return this.join(",");
   }

}
