/*[ IExecutionContext.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;


/**
 * This class provides an interface to the environment in which a class
 * implementing IResponseExit is executing. It can be expanded as needed. The
 * implementing classes must have a no-argument ctor so the class can be
 * initialized by the script interpreter.
 */
public interface IExecutionContext
{
   /**
    * Retrieves the value of any macro currently defined in the execution
    * environment. If the named macro is not defined, <code>null</code> is
    * returned. If the macro is defined with no value, an empty string is
    * returned.
    *
    * @param name The name of the macro, never <code>null</code> or empty.
    *
    * @return The value of the macro with the supplied name. If it isn't
    *    defined, <code>null</code> is returned. Note that <code>null</code>
    *    may be returned even if the macro is defined.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public String getMacro( String name );

   /**
    * Sets the value of a macro in the execution environment. If the named
    * macro is already defined, it is overwritten.
    *
    * @param name The name of the macro, never <code>null</code> or empty.
    *
    * @param value The value for the macro, may be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public void setMacro( String name, String value );
}
