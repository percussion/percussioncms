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

package com.percussion.design.objectstore;


/**
 * The PSLiteral class is used to define a replacement value is a 
 * static literal value. Literals of specific types are meant to
 * extend this class.
 *
 * @see     IPSReplacementValue
 *
 * @author       Tas Giakouminakis
 * @version  1.0
 * @since       1.0
 */
public abstract class PSLiteral
   extends PSComponent implements IPSBackEndMapping, IPSDocumentMapping
{
   /**
    * The value type associated with this instances of this class.
    */
   public static final String      VALUE_TYPE      = "Literal";
   
   /**
    * Default constructor for internal use
    */
   protected PSLiteral()
   {
      super();
   }

   //  IPSBackEndMapping Interface Implementation
   public String[] getColumnsForSelect()
   {
      return null;
   }
   
   /**
    * Returns the string representation of this literal.
    * 
    * @return the display text; never <code>null</code> or empty.
    * @see #getValueDisplayText
    */
   public String toString()
   {
     return getValueDisplayText();
   }  
}
