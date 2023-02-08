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
