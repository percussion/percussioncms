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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.percussion.content.IPSMimeContent;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.log.PSLogHandler;
import com.percussion.log.PSLogServerWarning;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.PSServer;
import com.percussion.util.IOTools;

/**
 * The base class for handling all extensions.
 */
public abstract class PSExtensionHandler implements IPSExtensionHandler
{
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
    * parameters and other config info. The required init params are:
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
      if (m_isInited)
      {
         shutdown();
      }

      if (codeRoot == null)
         throw new IllegalArgumentException("codeRoot cannot be null");

      if (!codeRoot.isDirectory())
      {
         Object[] args = new Object[] { getName(),
                 codeRoot.toString() + " must be a directory." };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
                 args);
      }

      if (!codeRoot.canRead())
      {
         Object[] args = new Object[] { getName(),
                 codeRoot.toString() + " must be readable." };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
                 args);
      }

      try
      {
         m_rootDir = codeRoot.getCanonicalFile();
      }
      catch (IOException e)
      {
         Object[] args = new Object[] { getName(),
                 e.toString() };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
                 args);
      }

      String cfgFileName = def.getInitParameter(
              IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME);
      if (cfgFileName == null || cfgFileName.trim().length() == 0)
      {
         Object[] args = new Object[] { getName(),
                 "configFile must be specified" };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_HANDLER_INIT_FAILED,
                 args);
      }

      m_configFile = new File(m_rootDir, cfgFileName);

      // load the config file
      initializeConfig(m_configFile);

      m_isInited = true;

      try
      {
         processPendingRemovals();
      }
      catch (IOException e)
      {
         // don't throw, because deleting a pending removal should not
         // affect the normal operation of this handler
         Object[] args = new Object[] {
                 def.getRef().toString(), // extension name
                 "", // resource name
                 e.toString() // reason
         };

         logMessage(IPSExtensionErrors.EXT_RESOURCE_DELETE_ERROR, args);
      }
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
      if (!m_isInited)
         return;

      try
      {
         storeConfig();
      }
      finally
      {
         m_isInited = false;
         m_config = null;
         m_liveExtensions = null;
      }
   }

   /**
    * Gets the names of all extensions installed with this
    * extension handler.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> PSExtensionRefs.
    */
   public Iterator getExtensionNames()
   {
      checkState();
      return m_config.getExtensionNames();
   }

   /**
    * Gets the names of all extensions installed within the given
    * context in this extension handler.
    *
    * @param context the context in which to search, not <code>null</code>.
    *
    * @return An Iterator over 0 or more non-<CODE>null</CODE>
    * PSExtensionRefs. Will return <CODE>null</CODE> if the
    * given context does not exist in this handler.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getExtensionNames(String context)
   {
      checkState();
      return m_config.getExtensionNames(context);
   }

   /**
    * Returns <CODE>true</CODE> if and only if the given extension ref refers
    * to an installed extension.
    *
    * @param ref The fully qualified extension name. Must not be
    * <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public boolean exists(PSExtensionRef ref)
   {
      checkState();

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionDef def = m_config.getExtensionDef(ref);
      return (def != null);
   }

   /**
    * Returns the extension definition for the extension with the
    * given name.
    *
    * @param ref The name of an installed extension. Must not
    * be <CODE>null</CODE>.
    *
    * @throws PSNotFoundException If the extension does not
    * exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSExtensionDef getExtensionDef(PSExtensionRef ref)
           throws PSNotFoundException
   {
      checkState();

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionDef def = m_config.getExtensionDef(ref);
      if (def == null)
      {
         throw new PSNotFoundException(IPSExtensionErrors.EXT_NOT_FOUND,
                 ref.toString());
      }

      try
      {
         /* Check to see if we need to add supplied resources (this is only done
          * the first time we load it and the resources are not part of the
          * def.  From now on they will be saved with the def.
          */
         Iterator resources = def.getSuppliedResources();
         if (resources == null)
         {
            // catalog them
            resources = getResources(def);

            // add them to the def
            def.setSuppliedResources(resources);

            // save the def
            storeConfig();
         }
      }
      catch (PSExtensionException e)
      {
         /* need to log this cause we can't throw it - this method is called
          * in too many places to change what it throws now.  Anyone counting
          * on this list who asks the def for it's supplied resources and
          * gets back null will know that it failed to upgrade the def.
          */
         Object[] args = {e.toString()};
         logMessage(0, args);
      }

      return def;
   }

   /**
    * Prepares and returns the installed extension.
    *
    * @param ref The name of an installed extension. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSNotFoundException If the extension does not
    * exist.
    *
    * @throws PSExtensionException If the extension is invalid.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSExtension prepare(PSExtensionRef ref)
           throws PSNotFoundException, PSExtensionException
   {
      checkState();
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionDef def = m_config.getExtensionDef(ref);
      if (def == null)
      {
         throw new PSNotFoundException(IPSExtensionErrors.EXT_NOT_FOUND,
                 ref.toString());
      }

      // see if an instance has already been loaded
      IPSExtension liveExt = getLiveExtension(ref);
      if (liveExt == null)
      {
         synchronized (this)
         {
            liveExt = getLiveExtension(ref);
            if (liveExt == null)
            {
               // ensure the directory exists before we prepare the extension
               File dir = getCodeBase(def);
               if (!dir.exists())
                  dir.mkdirs();

               // an instance hasn't been loaded yet
               liveExt = loadExtension(ref);
               putLiveExtension(ref, liveExt);
            }
         }
      }
      else
      {
         // found a loaded instance, let's see if it's reentrant
         String reentrant = def.getInitParameter(IPSExtensionDef.INIT_PARAM_REENTRANT);


         if (reentrant != null && reentrant.equals("yes"))
         {
            // this extension is re-entrant, so we can just return it
         }
         else
         {
            // this extension is not re-entrant, so we need to create a new
            // instance of it (we'll need to use pooling, I imagine)
            // TODO
            throw new RuntimeException("Only reentrant extensions supported");
         }
      }

      return liveExt;
   }

   /**
    * Installs the given extension.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    * Must be an instance of PSExtensionDef. After this method successfully
    * completes, the extension version init param will be one greater
    * than it was upon entry (or it will be set to "1" if it wasn't set
    * upon entry).
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

      if (!(def instanceof PSExtensionDef))
      {
         // TODO: i18n and code
         throw new PSExtensionException(0,
                 "def is not the right type for this handler");
      }

      PSExtensionRef ref = def.getRef();
      if (m_config.isExtensionDefined(ref))
      {
         throw new PSExtensionException(IPSExtensionErrors.EXT_ALREADY_EXISTS,
                 ref.toString());
      }

      // save all the extension resources and add extension to config
      try
      {
         // set the initial version, which will be 1 (one)
         int version = def.getVersion();

         ((PSExtensionDef)def).setInitParameter(
                 INIT_PARAM_VERSION, Integer.toString(version));

         // save the resources to disk
         saveResources(def, resources);

         // add the saved resource files to the def
         def.setSuppliedResources(getResources(def));

         //save it
         m_config.addExtensionDef(def);
         storeConfig();

         // Ask extensions manager to notify listeners
         PSExtensionManager emgr = (PSExtensionManager) PSServer
                 .getExtensionManager(null);
         if (emgr != null)
         {
            // Won't execute in standalone extensions installation
            emgr.notifyAdd(def.getRef());
         }
      }
      catch (IOException e)
      {
         Object[] args = new Object[] {
                 ref.toString(), e.toString()
         };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_INSTALL_UPDATE_ERROR, args);
      }
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

      if (!(def instanceof PSExtensionDef))
      {
         // TODO: i18n and code
         throw new PSExtensionException(0,
                 "def is not the right type for this handler");
      }

      PSExtensionRef ref = def.getRef();
      IPSExtensionDef oldDef = m_config.getExtensionDef(ref);
      if (oldDef == null)
      {
         throw new PSNotFoundException(IPSExtensionErrors.EXT_NOT_FOUND,
                 ref.toString());
      }

      // increment the version on the new def to old ver + 1
      final int oldVer = oldDef.getVersion();
      final int newVer = oldVer + 1;
      ((PSExtensionDef)def).setInitParameter(
              INIT_PARAM_VERSION, Integer.toString(newVer));

      // now deactivate the old version and install the new version
      remove(ref);
      install(def, resources);
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
      checkState();

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      if (!m_config.isExtensionDefined(ref))
      {
         throw new PSNotFoundException(IPSExtensionErrors.EXT_NOT_FOUND,
                 ref.toString());
      }

      // get the def and the code base of the extension
      IPSExtensionDef def = m_config.getExtensionDef(ref);
      File extDir = getCodeBase(def);

      // register the extension directory with the pending-removal
      // mechanism.
      // TODO: Optimization: if the extension is not yet loaded, then
      // all its files should be immediately removable (no registration
      // necessary).
      try
      {
         m_config.setPendingRemoval(extDir);
      }
      catch (IOException e)
      {
         Object[] args = new Object[] {
                 ref.toString(), extDir, e.toString()
         };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_RESOURCE_DELETE_ERROR,
                 args);
      }

      m_liveExtensions.remove(ref);

      // remove the definition from the config and re-store it
      m_config.removeExtensionDef(ref);
      storeConfig();
   }

   /**
    * Returns a list of files that the extension requires by cataloging the
    * files in the resource directory.
    * @param def The extension def.  May not be <code>null</code>.
    * @return An Iterator over <code>0</code> or more URL objects.  Path is
    * relative to the code root of the extension.  Never <code>null</code>.
    * @throws IllegalArgumentException if def is <code>null</code>.
    * @throws PSExtensionException if the files cannot be located.
    */
   @SuppressWarnings("unchecked")
   public Iterator getResources(IPSExtensionDef def) throws PSExtensionException
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null.");

      File codeRoot = getCodeBase(def);

      ArrayList resources = new ArrayList();

      try
      {
         // walk resource locations
         Iterator locations = def.getResourceLocations();

         while (locations.hasNext())
         {
            URL location = (URL)locations.next();
            // build path to file relative from code root
            File locFile = new File(codeRoot, location.getFile());
            if (!locFile.isDirectory())
               resources.add(location);
            else
            {
               Iterator locUrls = catalogResources(locFile,
                       codeRoot).iterator();
               while (locUrls.hasNext())
               {
                  resources.add(locUrls.next());
               }
            }
         }
      }
      catch(MalformedURLException e)
      {
         Object[] args = new Object[] {
                 def.getRef().toString(), e.toString()
         };

         throw new PSExtensionException(
                 IPSExtensionErrors.CATALOG_EXT_RESOURCE_ERROR, args);
      }

      return resources.iterator();
   }

   /**
    * Recursively catalogs the files in the specified resource directory and
    * returns them as an ArrayList of <code>0</code> or more URL objects.
    *
    * @param location The location to search.  Must be a directory on the local
    * file system.
    * @param codeRoot The code base of the def.
    * @return The ArrayList. Never <code>null</code>.
    * @throws IllegalArgumentException if location is <code>null</code> or is
    * not a directory, or if codeRoot is <code>null</code>.
    * @throws MalformedURLException if the result from the catalog is invalid.
    */
   @SuppressWarnings("unchecked")
   private ArrayList catalogResources(File location, File codeRoot)
           throws MalformedURLException
   {
      if (location == null)
         throw new IllegalArgumentException("location may not be null.");

      if (!location.isDirectory())
         throw new IllegalArgumentException(
                 "location must be a valid directory.");

      if (codeRoot == null)
         throw new IllegalArgumentException("codeRoot may not be null.");

      int rootEndPos = codeRoot.getPath().length() + 1;

      File[] files = location.listFiles();
      ArrayList retFiles = new ArrayList();

      for (int i = 0; i < files.length; i++)
      {
         // if it is a directory, recurisively catalog it and add it to the
         // list, otherwise it is a file so just add it
         if (files[i].isFile())
         {
            // need to strip off the code root - this is a fully qualified path
            String fullPath = files[i].getPath();
            String relPath = fullPath.substring(rootEndPos);
            retFiles.add(new URL("file", "", relPath));
         }
         else
         {
            Iterator iFiles = catalogResources(files[i], codeRoot).iterator();
            while (iFiles.hasNext())
            {
               // get these as URL's so just add it
               retFiles.add(iFiles.next());
            }
         }
      }

      return retFiles;
   }

   /**
    * Gets the live instance of the extension with the given
    * name.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @return The live instance, or <CODE>null</CODE> if no live
    * instance was found.
    *
    * @see #getLiveExtension
    */
   protected IPSExtension getLiveExtension(PSExtensionRef ref)
   {
      return (IPSExtension)m_liveExtensions.get(ref);
   }

   /**
    * Adds the live extension with the given name, overwriting any other
    * live extension with the same name.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    * @param ext The live instance. Must not be <CODE>null</CODE>.
    *
    * @see #putLiveExtension
    */
   @SuppressWarnings("unchecked")
   private void putLiveExtension(PSExtensionRef ref,
                                 IPSExtension ext)
   {
      m_liveExtensions.put(ref, ext);
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
   protected abstract IPSExtension loadExtension(PSExtensionRef ref)
           throws PSExtensionException, PSNotFoundException;

   /**
    * Creates an initialization Properties object for an extension,
    * given an extension definition containing init params. The returned
    * Properties object will be suitable for calling IPSExtension.init.
    *
    * @param def The extension def. Must not be <CODE>null</CODE>.
    *
    * @return An initialization Properties object. Never <CODE>null</CODE>.
    */
   protected Properties createInitProps(IPSExtensionDef def)
   {
      Properties props = new Properties();
      for (Iterator i = def.getInitParameterNames(); i.hasNext(); )
      {
         String name = i.next().toString();
         props.setProperty(name, def.getInitParameter(name));
      }

      return props;
   }

   /**
    * Returns the directory in which the given extension and all
    * of its resources would be stored.
    * <p>
    * Note that this method does not consider whether such an extension
    * actually exists or not. It just returns where the code would be
    * stored if the extension were installed.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    *
    * @return The directory in which the given extension and all
    * of its resources would be stored. Never <CODE>null</CODE>.
    */
   public File getCodeBase(IPSExtensionDef def)
   {
      PSExtensionRef ref = def.getRef();

      return new File(m_rootDir, ref.getContext() + ref.getExtensionName() +
              "/" + def.getVersion() + "/");
   }

   /**
    * Gets the value of the named initialization parameter of
    * the named extension.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    * @param propertyName The property name. Must not be <CODE>null</CODE>.
    *
    * @return The initialization parameter value of the extension,
    * or <CODE>null</CODE> if the extension or param does not exist.
    */
   protected String getExtensionParam(PSExtensionRef ref, String propertyName)
   {
      String val = null;
      IPSExtensionDef def = m_config.getExtensionDef(ref);
      if (def != null)
      {
         val = def.getInitParameter(propertyName);
      }

      return val;
   }

   /**
    * If this object has not been initialized (or has been
    * shutdown without being reinitialized), throws an
    * IllegalStateException.
    */
   protected void checkState()
   {
      if (!m_isInited)
      {
         throw new IllegalStateException(
                 "Extension handler has not been initialized.");
      }
   }

   /**
    * Save all of the given resources under the given directory.
    *
    * @param def The definition for the extension whose resources are
    * given. Must not be <CODE>null</CODE>.
    *
    * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
    * named IPSMimeContent objects specifying any resources that should be
    * saved along with the extension. The resources may or may not
    * correspond to the URLs returned from
    * <CODE>ext.getResourceLocations()</CODE>.
    *
    * @throws IOException If an IO error occurred while saving.
    *
    * @throws PSExtensionException If the code base could not be obtained
    * or if another error occurred with the resources.
    */
   private void saveResources(IPSExtensionDef def, Iterator resources)
           throws IOException, PSExtensionException
   {
      File dir = getCodeBase(def);

      // first create the directory
      dir.mkdirs();

      if (!dir.isDirectory())
      {
         Object[] args = new Object[] {
                 def.getRef().toString(), // extension name
                 dir.toString(), // resource name
                 "could not create extension code root" // reason
         };

         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_RESOURCE_STORE_ERROR,
                 args);
      }

      while (resources.hasNext())
      {
         IPSMimeContent resource = (IPSMimeContent)resources.next();
         String resName = resource.getName();
         if (resName == null || resName.length() == 0)
         {
            Object[] args = new Object[] {
                    def.getRef().toString(), // extension name
                    "", // resource name
                    "empty path" // reason
            };

            logMessage(IPSExtensionErrors.EXT_RESOURCE_STORE_ERROR, args);
            continue;
         }

         File f = new File(resName);
         if (f.isAbsolute())
         {
            Object[] args = new Object[] {
                    def.getRef().toString(),
                    resName,
                    "absolute path"
            };

            logMessage(IPSExtensionErrors.EXT_RESOURCE_STORE_ERROR, args);
            continue;
         }

         if (f.toString().indexOf("..") >= 0)
         {
            Object[] args = new Object[] {
                    def.getRef().toString(),
                    resName,
                    "contains .."
            };

            logMessage(IPSExtensionErrors.EXT_RESOURCE_STORE_ERROR, args);
            continue;
         }

         f = new File(dir, f.toString());
         f.getParentFile().mkdirs(); // create all necessary directories
         OutputStream out = null;
         InputStream in = resource.getContent();
         try
         {
            out = new BufferedOutputStream(new FileOutputStream(f));
            IOTools.copyStream(in, out, 8192);
         }
         finally
         {
            if (out != null)
               try { out.close(); } catch (IOException e) { /* ignore */ }

            if (in != null)
               try { in.close(); } catch (IOException e) { /* ignore */ }
         }
      }
   }

   /**
    * Processes all pending removals by deleting the files or directories
    * if possible. Removes all successfully deleted files or directories from
    * the pending-removal list. For each removal, once deleted, if the current
    * directory is empty, moves up one directory and deletes that directory.
    * Repeats until reaching a non-empty directory or the root dir for this
    * extension handler.
    *
    * @throws IOException If an I/O occurred during processing.
    * @throws PSExtensionException If an error occurred during processing.
    */
   private void processPendingRemovals() throws PSExtensionException,
           IOException
   {
      for (Iterator i = m_config.getPendingRemovals(); i.hasNext(); )
      {
         File remove = (File)i.next();

         // ensure that this file is under the handler root
         if (!isWithinRoot(remove))
         {
            // remove this and continue
            m_config.clearPendingRemoval(remove);
            continue;
         }

         processPendingRemoval(remove);
         removeExtensionDirectory(remove.getParentFile());

      }
      storeConfig();
   }

   /**
    * Processes the given pending removal by deleting the file or directory
    * if possible. If the file or directory was successfully deleted,
    * removes it from the pending-removal list.
    *
    * @param f The file or directory pending removal. Must not be
    * <CODE>null</CODE>.
    *
    * @throws IOException If an I/O error occurred during processing.
    */
   private void processPendingRemoval(File f)
           throws IOException
   {
      if (f.isDirectory())
      {
         // remove all files under this
         File[] files = f.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            processPendingRemoval(files[i]);
         }
      }

      if (f.delete())
      {
         m_config.clearPendingRemoval(f);
      }
      else
      {
         // maybe the file was already deleted
         if (!f.exists())
         {
            m_config.clearPendingRemoval(f);
         }
         else
         {
            // we'll try it on VM close then (but don't clear the pending
            // entry until later, when we're sure it's been deleted)
            f.deleteOnExit();
         }
      }
   }

   /**
    * If the current directory is empty, moves up one directory and deletes
    * that directory. Repeats until reaching a non-empty directory or
    * the root dir for this extension handler.
    *
    * @param f The directory for removal.  If null, nothing is removed.
    *
    * @throws IOException If an I/O error occurred during processing.
    */
   private void removeExtensionDirectory(File f)
           throws IOException
   {
      /* proceed only if we have an empty directory that is not the root
         directory for this extention */
      if ((f == null) || !f.isDirectory() || (f.listFiles().length > 0) ||
              f.getCanonicalFile().equals(m_rootDir))
      {
         return;
      }

      if (f.delete())
      {
         removeExtensionDirectory(f.getParentFile());
      }
   }

   /**
    * Returns <CODE>true</CODE> if and only if the given file is within the
    * handler's root directory.
    *
    * @param f The file to check. Must not be <CODE>null</CODE>.
    *
    * @return <CODE>true</CODE> iff f is within (or equal to) m_rootDir
    *
    * @throws IOException If the canonical file could not be determined.
    */
   private boolean isWithinRoot(File f)
           throws IOException
   {
      f = f.getCanonicalFile();
      while (f != null)
      {
         if (f.equals(m_rootDir))
            return true;

         f = f.getParentFile();
      }
      return false;
   }

   /**
    * Stores the configuration information into the configuration
    * file.
    *
    * @throws PSExtensionException If an error occurred.
    */
   private void storeConfig() throws PSExtensionException
   {
      try
      {
         // store the config in the proper file, don't store the JEXL methods
         m_config.store(m_configFile, true);

         // reinitialize from the just stored file to load all JEXL methods
         initializeConfig(m_configFile);
      }
      catch (IOException e)
      {
         // TODO: i18n and code
         // TODO: maybe a fail-safe that backs up the config somewhere eles
         throw new PSExtensionException(0, e.toString());
      }
   }

   /**
    * Initializes the configuration object from the given file. Subclasses
    * should override this if they want to instantiate a more derived type
    * of extension handler config.
    *
    * @param configFile The configuration file. Must not be
    * <CODE>null</CODE>. Must exist and be a readable file containing
    * valid configuration information.
    *
    * @throws PSExtensionException If the configuration object could not
    * be initialized.
    *
    * @see #m_config
    */
   protected void initializeConfig(File configFile)
           throws PSExtensionException
   {
      // make sure everything exists, so we can bootstrap if necessary
      try
      {
         configFile.getParentFile().mkdirs();
         if (!configFile.exists())
         {
            PSExtensionHandlerConfiguration.createShellConfigFile(
                    configFile, getName());
         }
      }
      catch (IOException e)
      {
         throw new PSExtensionException(
                 IPSExtensionErrors.EXT_MANAGER_INIT_FAILED, e.toString());
      }

      m_config = new PSExtensionHandlerConfiguration(configFile,
              new PSExtensionDefFactory());
   }

   /**
    * Logs the given message to the server logging mechanism (if available)
    * or stdout.
    *
    * @param errorCode The error code. An error code recognized by the
    * server error handling system or 0.
    *
    * @param args The error arguments corresponding to
    * <CODE>errorCode</CODE>. Must not be <CODE>null</CODE>.
    */
   protected void logMessage(int errorCode, Object[] args)
   {
      PSLogServerWarning warning = new PSLogServerWarning(errorCode, args,
              false, "ExtensionHandler");
      if (m_logHandler == null)
      {
         PSLogSubMessage[] messages = warning.getSubMessages();
         StringBuffer strMessage = new StringBuffer("");
         for (int i=0; i<messages.length; i++)
            strMessage.append(messages[i].getText());
         System.out.println(strMessage.toString());
      }
      else
         m_logHandler.write(warning);
   }

   /**
    * The version of the extension, as a positive integer. This is an internal
    * version # to allow us to do dynamic loading.
    */
   private static final String INIT_PARAM_VERSION =
           "com.percussion.extension.version";


   /**
    * The extension handler configuration, including all extension
    * definitions.
    */
   protected PSExtensionHandlerConfiguration m_config;

   /**
    * A map from extension names to live extension instances.
    * Unused extensions will not have an entry in this map,
    * but all instantiated extensions will have an entry.
    */
   private volatile Map m_liveExtensions = new ConcurrentHashMap(8, 0.9f, 1);

   /**
    * The config file.
    */
   private File m_configFile;

   /**
    * The directory under which all extensions are stored and
    * retrieved. Never <CODE>null</CODE>.
    * <p>
    * This is guaranteed to be canonical.
    * @see java.io.File#getCanonicalFile
    */
   private File m_rootDir;

   /**
    * Used to see if this handler has been initialized or not.
    */
   private boolean m_isInited;

   /**
    * Used to write messages to the server log.
    */
   private PSLogHandler m_logHandler = PSServer.getLogHandler();
}
