/*[ IPSAutoTestJavaExtension.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire;

import org.w3c.dom.Element;

public interface IPSAutoTestJavaExtension
{

   /* Run any extended action item from the class that 
      implements this class, the class will be loaded and called
      when an item is defined in the test script as
      <blah className="thisClassName" ...></blah>
      The argument, actionElement, will contain any class-specific
      information needed to execute the specified action 
      NOTE:  currently only the attributes of actionElement itself
      are expanded for script-based macros, anything that uses macros
      must currently be implemented as an attribute of the main
      ExtendedJavaSupportCall element 
   */
   public void runExtensionAction(Element actionElement)
      throws Exception;

}
