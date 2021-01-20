/*[ QAFileWriter.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
/**
 * This class will write the test results to the test servers file system.
 */
public class QAFileWriter implements IQAWriter
{
   /**
    * Creates a new QA writer which writes the test results to the provided
    * file.
    *
    * @param file the target file to which this will write the QA results, 
    *    not <code>null</code>, must be a valid file.
    * @throws IllegalArgumentException if the provided file is invalid.
    */
   public QAFileWriter(File file)
   {
      if (file == null || !file.isFile())
         throw new IllegalArgumentException("we need a valid file");
      
      m_file = file;
   }

   /**
    * Implements IQAWriter to write the QA results to the specified file.
    *
    * See {@link IQAWriter#write(QATestResults)}
    */
   public void write(QATestResults results)
   {
      if (results == null)
         throw new IllegalArgumentException("results cannot be null");
      
      OutputStreamWriter out = null;
      try
      {
         System.out.println("Recording results to " + 
            m_file.getCanonicalPath());

         out = new OutputStreamWriter(new FileOutputStream(m_file));
         results.write(out);
      }
      catch (Throwable t)
      {
         System.out.println("**** Error recording results: " + t.toString());
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.flush();
               out.close();
            }
            catch (IOException e)
            {
               // we tried
            }
         }
      }
   }
   
   /**
    * The target file to write the results to. Initialized during 
    * construction, always a valid file after that.
    */
   private File m_file = null;
}
