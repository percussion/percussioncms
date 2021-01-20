/*[ ICustomCompare.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

import java.io.InputStream;

/**
 * This interface can be used by any class that wishes to overided the comparison
 * of the http body and the expected file when using the <code>&lt;Expect&gt;</code>
 * tag.  In addition to the <code>href</code> attribute in this element, you may
 * also supply a <code>compareclass</code> attribute, which specifies the classname
 * that implements this interface.  That class will be used when comparing the
 * returned http body with the supplied file specified in the href attribute instead
 * of the default comparison.  If the <code>compareclass</code> attribute is
 * supplied but the <code>href</code> attribute is missing, the <code>compareclass</code>
 * attribute will be ignored. <p>
 *
 * Example:  &lt;Expect href="http://www.qa.com/testdata/1.xml"
 *                      compareclass="com.percussion.autotest.empire.TraceCompare"&gt;
 *                ...
 *           &lt;/Expect&gt;
 *
 */
public interface ICustomCompare
{

   /**
    * Compare the given input streams, raising an error on the
    * first difference found.
    *
    * @param expectedResult input stream containing the data to match against.
    * @param expectedCharSet Character set to use when reading the expected
    *                         result input stream
    * @param actualResult input stream containing the actual data to compare to
    *                     the expected result.
    * @param actualCharSet Character set to use when reading the actual result
    *                      input stream
    *
    * @throws ScriptTestFailedException if the comparison fails
    */
   public void compare(InputStream expectedResult,
                        String expectedCharSet,
                        InputStream actualResult,
                        String actualCharSet)
                  throws Exception;

}
