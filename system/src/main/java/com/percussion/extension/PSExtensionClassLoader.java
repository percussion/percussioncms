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
import java.io.FilePermission;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;

/**
 * Extension class loaders are used to load all extension handlers
 * and all Java-based extensions.
 * <p>
 * The extension class loader is also responsible for setting permissions
 * on extension handlers and Java-based extensions. The permissions may
 * be defined elsewhere, but they will be applied by this class.
 */
public class PSExtensionClassLoader extends URLClassLoader
{
   /**
    * Constructs an extension class loader that will find its Java classes
    * in specified directory or JAR file. Class loading will be delegated
    * to the default parent class loader.
    *
    * @param root The directory under which all resources will be
    * found. The directory must exist at construction time.
    *
    * @throws IOException If <CODE>root</CODE> does not exist or is not
    * readable.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   PSExtensionClassLoader(File root)
      throws IOException
   {
      // Pass the default class loader being used by the application
      this(root, PSExtensionClassLoader.class.getClassLoader() );
   }

   /**
    * Constructs an extension class loader that will find its Java classes
    * in specified directory or JAR file. Class loading will be delegated
    * to the given parent class loader.
    *
    * @param root The directory or JAR file in which Java classes will be
    * found. If <CODE>root</CODE> is a directory, it must end in a '/',
    * otherwise it will be assumed to refer to a JAR file. Must not be
    * <CODE>null</CODE>.
    * 
    * @param parent The parent class loader for delegation.
    *
    * @throws IOException If <CODE>root</CODE> does not exist or is not
    * readable.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   PSExtensionClassLoader(File root, ClassLoader parent)
     throws IOException
   {
     super(new URL[] { root.toURL() }, parent);

      if (!root.isDirectory())
         throw new IllegalArgumentException("root must be a directory");

     initCodeSource(root);
   }

   /**
    * @see URLClassLoader#addURL
    */
   public void addURL(URL url)
   {
      if (!url.getProtocol().equals("file"))
      {
         throw new IllegalArgumentException(
            "only file URLs are currently supported");
      }

      super.addURL(url);
   }

   /**
    * Returns the permissions for the given codesource. This method first
    * calls URLClassLoader.getPermissions(), which in turn calls
    * SecureClassLoader.getPermissions(). This method then adds some
    * more liberal permissions to the permission collection, such as
    * read and write access to all files under the extension's own
    * directory.
    *
    * @param codeSource The code source whose permissions we are to
    * return. Can be <CODE>null</CODE>.
    */
   protected PermissionCollection getPermissions(CodeSource codeSource)
   {
      PermissionCollection perms = super.getPermissions(codeSource);
      
      // if the codesource is within the extension directory, then
      // we grant some additional permissions
      if (m_codeSource.implies(codeSource))
      {
         perms.add(new FilePermission(
            m_rootDir.toString() + File.separator + "-", "read,write,delete"));
      }

      return perms;
   }

   /**
    * Private construction method to canonicalize the root directory member
    * variable and create the code source object member variable.
    *
    * @param file The file. Must not be <CODE>null</CODE>.
    */
   private void initCodeSource(File file)
      throws MalformedURLException
   {
      // m_rootDir must never end in a file sep char
      if (file.toString().endsWith(File.separator))
      {
         String fileStr = file.toString();
         fileStr = fileStr.substring(0, fileStr.length() - 1); // strip slash
         m_rootDir = new File(fileStr);
      }
      else
      {
         m_rootDir = file;
      }

      m_codeSource = new CodeSource(
         new File(m_rootDir.toString() + "/-").toURL(), (java.security.cert.Certificate[]) null);
   }

   /**
    * The code source corresponding to the extension root dir.
    * Never <CODE>null</CODE>
    */
   private CodeSource m_codeSource;

   /**
    * The canonical extension root dir. Never ends in File separator char.
    * Never <CODE>null</CODE>.
    */
   private File m_rootDir;
}
