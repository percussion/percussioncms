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
package com.percussion.extension;

import java.io.File;
import java.io.IOException;

/**
 * A simple testing extension used from the command line. The extension
 * can create its own def, create its own manager, then install itself
 * to the PSTestingExtensionHandler.
 */
public class PSTestingExtension implements IPSExtension
{
   public PSTestingExtension()
   {
      m_isInited = false;
   }

   /**
    * @see IPSExtension#init
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      System.err.println(toString() + " init(" + codeRoot + ")");
      m_codeRoot = codeRoot;
      m_isInited = true;
   }

   /** Do something which should violate the testing security policy */
   public void deniedFileSecurity()
      throws SecurityException
   {
      System.err.println("Violating file security.");
      File f = new File("/temp/foobar");
      f.exists();
   }

   public void deniedSecurityManagerSecurity()
   {
      System.err.println("Violating security manager security");
      System.setSecurityManager(new SecurityManager());
   }

   /** Do something which should be allowed by the testing security policy */
   public void allowedFileSecurity()
      throws IOException
   {
      System.err.println("Reading files under the " + m_codeRoot + " directory.");
      File f = new File(m_codeRoot, "foobar");
      f.exists();

      // create a temporary file under the coderoot
      System.err.println("About to create a temporary file under " + m_codeRoot);
      File newFile = File.createTempFile("test", ".tmp", m_codeRoot);
      newFile.delete();
   }

   protected void finalize() throws Throwable
   {
      System.err.println("Finalizing PSTestingExtension instance " + toString());
      super.finalize();
   }

   static
   {
      System.err.println("Loading PSTestingExtension class");
   }

   private File m_codeRoot;
   private boolean m_isInited;
}
