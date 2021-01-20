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
package com.percussion.extension;

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The base class for handling compiled Java extensions.
 */
public class PSJavaExtensionHandler extends PSExtensionHandler implements IPSNotificationListener
{
   /**
    * Add this instance for any file changes in the server environment.
    * This should be called during server start up; but may not be in a unit
    * test environment.
    */
   void addListenerForFileChanges()
   {
      IPSNotificationService notifyService = PSNotificationServiceLocator
            .getNotificationService();
      notifyService.addListener(EventType.FILE, this);
   }
   
   /*
    * //see IPSNotificationListener.notifyEvent() method for details
    */
   public void notifyEvent(PSNotificationEvent event)
   {
      if (event == null || event.getType() != EventType.FILE
            || (!(event.getTarget() instanceof File)))
      {
         return;
      }
      File tgtFile = (File) event.getTarget();
      if (!tgtFile.getPath().endsWith(".jar"))
         return;
      
      synchronized (ms_extraJars)
      {
         try
         {
            ms_extraJars.add(tgtFile.toURL());
         }
         catch (Exception e)
         {
            ms_log.error("Failed to register jar file, "
                  + tgtFile.getAbsolutePath(), e);
         }
      }
   }
   
   /**
    * Initializes this extension handler. The extension manager will
    * call this method after construction and before calling any
    * other methods.
    * <p>
    * Note that the extension handler will have permission to read
    * and write any files or directiors under <CODE>codeRoot</CODE>
    * (recursively). The handler will not have permissions for
    * any other files or directories.
    *
    * @param def The extension definition containing initialization
    * params and other information. The required initialization
    * params are:
    * <UL>
    * <LI><B>configFile</B> - The configuration filename, relative to
    * <CODE>codeRoot</CODE>.
    * </UL>
    *
    * @param codeRoot The root directory where this extension handler
    * should install and look for any files relating to itself or
    * its extensions. The subdirectory structure under codeRoot is
    * left up to the handler implementation. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the codeRoot does not exist,
    * is not accessible, or specifies a protocol that this extension handler
    * cannot handle. Also thrown for any other initialization errors that
    * will prohibit this handler from doing its job correctly, such
    * as invalid or missing properties.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      super.init(def, codeRoot);
      m_loaders = new HashMap();
   }

   /**
    * Shuts down this extension handler. The extension manager will
    * call this method to indicate that this extension handler will
    * no longer be used (e.g., server shut down, etc.)
    *
    * @throws PSExtensionException If an error occurred which prohibited
    * this extension handler from cleanly shutting down.
    */
   public synchronized void shutdown()
      throws PSExtensionException
   {
      try
      {
         super.shutdown();
      }
      finally
      {
         m_loaders = null;
      }
   }

   /**
    * Gets the name of this extension handler.
    *
    * @return The name of this extension handler. Never <CODE>null</CODE>.
    */
   public String getName()
   {
      return "Java";
   }

   /**
    * Installs the given extension.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    *
    * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
    * named IPSMimeContent objects specifying any resources that should be
    * saved along with the extension. The resources may or may not
    * correspond to the URLs returned from
    * <CODE>ext.getResourceLocations()</CODE>.
    *
    * @throws PSExtensionException If the extension is invalid.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized void install(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException
   {
      checkState();

      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (resources == null)
         throw new IllegalArgumentException("resources cannot be null");

      String className = def.getInitParameter("className");
      if (!isValidClassName(className))
      {
         Object[] args = new Object[]  {
            def.getRef().getHandlerName(),
            def.getRef().getExtensionName(),
            "\"" + className + "\" is not a valid class name."
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }

      super.install(def, resources);
   }

   /**
    * Atomically removes and re-installs the given extension. Currently
    * this method is not guaranteed to be transactional, that is, the remove
    * may succeed but the install may fail.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    *
    * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
    * named IPSMimeContent objects specifying any resources that should be
    * saved along with the extension. The resources may or may not
    * correspond to the URLs returned from the def's
    * <CODE>getResourceLocations()</CODE> method. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSNotFoundException If the appropriate extension
    * does not exist. The defined extension will not be installed.
    *
    * @throws PSExtensionException If the extension definition fails
    * the handler's validation rules or if the extension could not be
    * loaded (some implementations may defer loading until prepareExtension
    * is called).
    *
    * @see #remove
    * @see #install
    */
   public synchronized void update(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException, PSNotFoundException
   {
      checkState();

      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (resources == null)
         throw new IllegalArgumentException("resources cannot be null");

      String className = def.getInitParameter("className");
      if (!isValidClassName(className))
      {
         Object[] args = new Object[]  {
            def.getRef().getHandlerName(),
            def.getRef().getExtensionName(),
            "\"" + className + "\" is not a valid class name."
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }

      super.update(def, resources);
   }

   /**
    * Permanently disables and removes the installed extension. Any
    * Java classes that were defined and loaded as part of this
    * extension must be unloaded. Any native libraries that were
    * loaded as part of this extension must be unloaded.
    *
    * @param ref The name of an installed extension. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSExtensionException If an error occurred while removing
    * extension.
    *
    * @throws PSNotFoundException If the extension does not
    * exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized void remove(PSExtensionRef ref)
      throws PSNotFoundException, PSExtensionException
   {
      super.remove(ref);

      // drop any references to the extension and its classloader
      m_loaders.remove(ref);
   }

   /**
    * Returns <CODE>true</CODE> if and only if the given String is
    * a valid Java class name of the form:
    * <PRE>
    *  &lt;class_name&gt := [ &lt;package_name&gt; &lt;identifier&gt;
    *  &lt;package_name&gt := [ &lt;package_name&gt; "." ] &lt;identifier&gt;
    * </PRE>
    *
    * @param className The class name to test for validity. If
    * <CODE>null</CODE>, this method will return <CODE>false</CODE>.
    *
    * @return <CODE>true</CODE> iff <CODE>className</CODE> is valid.
    */
   private static boolean isValidClassName(String className)
   {
      if (className == null)
         return false;

      // construct a tokenizer on . that returns individual classnames
      StringTokenizer toker = new StringTokenizer(className, ".", true);
      while (toker.hasMoreTokens())
      {
         String identifier = toker.nextToken();
         if (identifier.equals("."))
            continue;

         if (!PSExtensionRef.isValidExtensionName(identifier))
            return false;
      }

      return true;
   }

   /**
    * Loads the extension with the given name and returns it. It is
    * the caller's responsibility to add the returned extension to the
    * live extension map, if he so chooses.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @return The extension. Never <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension could not be loaded.
    *
    * @throws PSNotFoundException If no such extension exists.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   protected synchronized IPSExtension loadExtension(PSExtensionRef ref)
      throws PSExtensionException, PSNotFoundException
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionDef def = m_config.getExtensionDef(ref);
      if (def == null)
      {
         throw new PSNotFoundException(
            IPSExtensionErrors.EXT_NOT_FOUND, ref.toString());
      }

      String className = def.getInitParameter("className");
      if (!isValidClassName(className))
      {
         Object[] args = new Object[]  {
            def.getRef().getHandlerName(),
            def.getRef().getExtensionName(),
            "\"" + className + "\" is not a valid class name."
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }

      PSExtensionClassLoader loader =
         (PSExtensionClassLoader)m_loaders.get(ref);

      if (loader == null)
      {
         // create a class loader for this extension and add it to our map
         loader = createClassLoader(def);
         m_loaders.put(ref, loader);
      }

      try
      {
         // load and initialize the extension instance
         Class extClass = loader.loadClass(className);

         IPSExtension ext = (IPSExtension)extClass.newInstance();
         File baseDir = getCodeBase(def);
         ext.init(def, baseDir);
         return ext;
      }
      catch (ExceptionInInitializerError e)
      {
         Object[] args = new Object[]  {
            ref.getHandlerName(),
            ref.getExtensionName(),
            e.getException().toString()
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }
      catch (Throwable t)
      {
         Object[] args = new Object[]  {
            ref.getHandlerName(),
            ref.getExtensionName(),
            t.toString()
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }
   }

   /**
    * Creates a class loader that will find all and only files which are
    * (or are within) resource locations specified in the definition.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the class loader could not be
    * created.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   private PSExtensionClassLoader createClassLoader(IPSExtensionDef def)
      throws PSExtensionException
   {
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      try
      {
         File baseDir = getCodeBase(def);
         PSExtensionClassLoader loader = new PSExtensionClassLoader(baseDir);

         // create a URL classloader that looks in the right places
         for (Iterator i = def.getResourceLocations(); i.hasNext(); )
         {
            URL u = (URL)(i.next());
         
            // in this version, only local extension resources are supported
            if (!u.getProtocol().equals("file"))
            {
               Object[] args = new Object[]  {
                  def.getRef().getHandlerName(),
                  def.getRef().getExtensionName(),
                  u.toString() + " does not use a supported protocol"
               };

               throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
                  args);
            }

            File f = new File(u.getFile());
            if (f.isAbsolute()) // only relative paths are allowed
            {
               Object[] args = new Object[]  {
                  def.getRef().getHandlerName(),
                  def.getRef().getExtensionName(),
                  u.toString() + " does not specify a relative path"
               };

               throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
                  args);
            }

            if (f.toString().indexOf("..") != -1) // no escape from coderoot
            {
               Object[] args = new Object[]  {
                  def.getRef().getHandlerName(),
                  def.getRef().getExtensionName(),
                  u.toString() + " does not specify a relative path"
               };

               throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
                  args);
            }

            // make resource path relative to the extension coderoot
            f = new File(baseDir, f.toString());
            loader.addURL(f.toURL());
         }
         
         synchronized (ms_extraJars)
         {
            for (URL url : ms_extraJars)
            {
               loader.addURL(url);
            }
         }

         return loader;
      }
      catch (IOException e)
      {
         Object[] args = new Object[]  {
            def.getRef().getHandlerName(),
            def.getRef().getExtensionName(),
            e.toString()
         };

         throw new PSExtensionException(IPSExtensionErrors.EXT_INIT_FAILED,
            args);
      }
   }

   /**
    * A map from extension names to the class loaders used
    * to load them.
    */
   private Map m_loaders;
   
   /**
    * A list of jar files imported into the system after server start up.
    * Defaults to be an empty list. This list is populated by the packaging
    * tool or MSM.
    */
   private static Set<URL> ms_extraJars = new HashSet<URL>();
   
   /**
    * Logger for the assembler.
    */
   public static Log ms_log = LogFactory.getLog("PSJavaExtensionHandler");   
}
