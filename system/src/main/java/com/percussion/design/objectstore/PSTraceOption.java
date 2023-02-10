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
 * This class encapsulates all info about a particular trace option
 */
public class PSTraceOption 
{
   
   /**
    * Returns the name of the trace option for display purposes
    *
    * @return the name of this option.  Never <code>null</code>.
    * @roseuid 39F5CEC003D8
    */
   public String getDisplayName() 
   {
      return m_displayName;
   }
   
   /**
    * Returns the description for this trace option for display purposes.
    * 
    * @return the description.  May be <code>null</code>.
    * @roseuid 39F5CF0A0000
    */
   public String getDescription() 
   {
      return m_description;
   }
   
   /**
    * The constructor for this class.
    * 
    * @param traceFlag the flag used to identify this option
    * @param displayName the name of this option for display purposes.  May not be 
    * <code>null</code>.
    * @param description a description of this option used for display purposes.  May 
    * be <code>null</code>.
    * @param name the internal name that identifies this option.  May not be 
    * <code>null</code>.
    * @roseuid 39F5CF370242
    */
   public PSTraceOption(int traceFlag, String displayName, String description, String name)
   {
      if ((displayName == null) || displayName.length() == 0)
         throw new IllegalArgumentException("PSTraceOption: displayName must not be null");

      if ((name == null) || name.length() == 0)
         throw new IllegalArgumentException("PSTraceOption: name must not be null");

      m_optionFlag = traceFlag;
      m_displayName = displayName;
      m_description = description;
      m_name = name;

   }
   
   /**
    * Returns this object formatted as a String. 
    * 
    * @return A String representation of this object.  Never <code>null</code>.
    * @roseuid 39F5D1E7029F
    */
   public String toString()
   {
      return "0x" + Integer.toHexString(m_optionFlag);
   }

   /**
    * Returns the flag used  when specifiying this option from the console.
    *
    * @return the flag used to specifiy this option
    * @roseuid 39F6F2C1034B
    */
   public int getFlag()
   {
      return m_optionFlag;
   }

   /**
    * Returns the internal name that identifies this option.
    *
    * @return The internal name.  Never <code>null</code>.
    * @roseuid 39F726E10399
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * the displayed name of this option
    */
   private String m_displayName = null;
   
   /**
    * The description to display for this option
    */
   private String m_description = null;
   
   /**
    * Flag used to identify this option
    */
   private int m_optionFlag = 0;
   
   /**
    * the internal name that identifies this option.
    */
   private String m_name = null;
   
}
