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
package com.percussion.utils.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Process command line arguments into a useful form. Arguments that exist but
 * have no value return the string "true", other arguments return their value.
 * Arguments that have no value will return <code>true</code> for the 
 * method {@link #isFlag(String)}
 */
public class PSParseArguments
{
   /**
    * Ctor
    * @param args command line arguments, must not be <code>null</code>
    */
   public PSParseArguments(String args[])
   {
      if (args == null)
      {
         throw new IllegalArgumentException("args must never be null");
      }
      
      for(int i = 0; i < args.length; i++)
      {
         int n = i + 1;
         String arg = args[i];
         String nextarg = null;
         if (n < args.length)
         {
            nextarg = args[n];
         }
         
         if (arg.charAt(0) == '-')
         {
            String argname = arg.length() > 1 ? arg.substring(1) : null;
            if (argname == null)
            {
               throw new IllegalArgumentException("Invalid argument found: " + 
                     arg);
            }
            
            if (nextarg == null || nextarg.charAt(0) == '-')
            {
               m_flagArguments.add(argname);
               m_args.put(argname, "true");
            }
            else
            {
               m_args.put(argname, nextarg);
               i++; // Increment past next argument
            }
         }
         else
         {
            m_rest.add(arg);
         }
      }
      
   }
   
   /**
    * Returns the arguments that were not associated with named flags
    * @return the unassociated args, never <code>null</code> but may be 
    * empty.
    */
   public List getRest()
   {
      return m_rest;
   }   
   
   /**
    * Get the value of the named arg
    * @param name the argument, must never be <code>null</code> or empty
    * @return the value, may be <code>null</code>
    */
   public String getArgument(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name must never be null");
      }
      
      return (String) m_args.get(name);
   }
   
   /**
    * Return if this named argument is a flag
    * @param name the argument, must never be <code>null</code> or empty
    * @return <code>true</code> if the given argument is a flag
    */
   public boolean isFlag(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name must never be null");
      }
      
      return m_flagArguments.contains(name);
   }
   
   /**
    * Holds correspondence between individual argument and its value. 
    * Initialized in the ctor.
    */
   private Map<String,String> m_args = new HashMap<String,String>();
   
   /**
    * Holds the set of arguments that are flag arguments.
    * Initialized in the ctor.
    */
   private Set<String> m_flagArguments = new HashSet<String>();
   
   /**
    * Holds arguments that are not associated with a -argument. Initialized
    * in the ctor.
    */
   private List<String> m_rest = new ArrayList<String>();
}
