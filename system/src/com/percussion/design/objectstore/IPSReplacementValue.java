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

package com.percussion.design.objectstore;


/**
 * The IPSReplacementValue interface must be implemented by any class which
 * can be used as a replacement value for conditionals or exit parameters
 * at run-time.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSReplacementValue extends Cloneable {
   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType();

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText();

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText();
     
   /**
    * Creates a new instance of this object, deep copying all member variables.
    * If an implementing class has mutable member variables, it must override 
    * this method and clone() each of those variables.  This method will create
    * a shallow copy if it is not overridden.
    * 
    * @return a deep-copy clone of this instance, never <code>null</code>.
    */
   public Object clone();
}

