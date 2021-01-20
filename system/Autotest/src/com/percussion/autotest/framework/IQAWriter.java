/*[ IQAWriter.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

/**
 * This interface is used to write out test results collected while running
 * QA scripts.
 */
public interface IQAWriter
{
   /**
    * Writes the test results to the target specified by the implementing 
    * class.
    *
    * @param results the test results to be written, not <code>null</code>.
    * @throws IllegalArgumentException if the provided results are
    *    <code>null</code>.
    * @throws Exception if any errors occur.
    */
   public void write(QATestResults results) throws Exception;
}
