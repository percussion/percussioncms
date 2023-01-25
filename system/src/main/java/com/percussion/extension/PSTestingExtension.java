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
