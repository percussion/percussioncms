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

import com.percussion.design.objectstore.PSNotFoundException;

import java.io.File;
import java.util.Iterator;

/**
 * An extension handler is directly responsible for loading,
 * storing, and managing extensions of a particular type.
 * <p>
 * The implementation of an extension handler may actually be
 * responsible for executing the extensions, or it may instead
 * create IPSExtension objects which can execute on their own.
 * <p>
 * Implementations are not required to be thread-safe with respect
 * to installing, cataloging, removing, or otherwise managing
 * individual extensions. However, implementations must be aware
 * that multiple IPSExtension objects returned from prepare()
 * may be executed in different threads. That is, although multiple
 * threads will not call prepare() simultaneously, prepare() may
 * be called several times for a single extension, and the returned
 * IPSExtension object(s) may be used simultaneously from multiple
 * threads.
 */
public interface IPSExtensionHandler extends IPSExtension
{
   /** The extensions sub directory to the server, where all extensions will be stored. */
   public static final String EXTENSIONS_SUBDIR = "Extensions";

   /** The predefined handler context that all handler refs must use. */
   public static final String HANDLER_CONTEXT = "Handlers";

   /** The predefined handler name that all handler refs must use. */
   public static final String HANDLER_HANDLER = "ExtensionHandler";

   /** The default name for the handler configuration file. */
   public static final String DEFAULT_CONFIG_FILENAME = "Extensions.xml";

   /** The name of the key used to store the configuration filename */
   public static final String INIT_PARAM_CONFIG_FILENAME =
      "com.percussion.extension.configFile";

   /**
    * TODO: add this doc elsewhere
    *
    * Initializes this extension handler. The extension manager will
    * call this method after construction and before calling any
    * other methods.
    * <p>
    * Note that the extension handler will have permission to read
    * and write any files or directiors under <CODE>codeRoot</CODE>
    * (recursively). The handler will not have permissions for
    * any other files or directories.
    *
    * @param def The extension def, containing configuration info and
    * init params.
    *
    * @param codeRoot The root directory where this extension handler
    * should install and look for any files relating to itself or
    * its extensions. The subdirectory structure under codeRoot is
    * left up to the handler implementation.
    *
    * @throws PSExtensionException If the codeRoot does not exist,
    * is not accessible, or specifies a protocol that this extension handler
    * cannot handle. Also thrown for any other initialization errors that
    * will prohibit this handler from doing its job correctly, such
    * as invalid or missing properties.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */

   /**
    * Shuts down this extension handler. The extension manager will
    * call this method to indicate that this extension handler will
    * no longer be used (e.g., server shut down, etc.)
    *
    * @throws PSExtensionException If an error occurred which prohibited
    * this extension handler from cleanly shutting down.
    */
   public void shutdown()
      throws PSExtensionException;

   /**
    * Gets the name of this extension handler.
    *
    * @return The name of this extension handler. Never <CODE>null</CODE>.
    */
   public String getName();

   /**
    * Gets the names of all extensions installed with this
    * extension handler.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> PSExtensionRefs.
    */
   public Iterator getExtensionNames();

   /**
    * Gets the names of all extensions installed within the given
    * context in this extension handler.
    *
    * @param context the context in which to search. Must not be 
    *    <CODE>null</CODE>.
    *
    * @return An Iterator over 0 or more non-<CODE>null</CODE>
    * PSExtensionRefs. Will return <CODE>null</CODE> if the
    * given context does not exist in this handler.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getExtensionNames(String context);

   /**
    * Returns <CODE>true</CODE> if and only if the given extension ref refers
    * to an installed extension.
    *
    * @param ref The fully qualified extension name. Must not be
    * <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public boolean exists(PSExtensionRef ref);

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
      throws PSNotFoundException;

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
      throws PSNotFoundException, PSExtensionException;

   /**
    * Installs the given extension.
    *
    * @param def The extension definition. Must not be <CODE>null</CODE>.
    *
    * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
    * named IPSMimeContent objects specifying any resources that should be
    * saved along with the extension. The resources may or may not
    * correspond to the URLs returned from the def's
    * <CODE>getResourceLocations()</CODE> method.
    *
    * @throws PSExtensionException If the extension is invalid.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void install(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException;

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
   public void update(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException, PSNotFoundException;

   /**
    * Permanently disables and removes the installed extension. Any
    * Java classes that were defined and loaded as part of this
    * extension must be unloaded. Any native libraries that were
    * loaded as part of this extension must be unloaded.
    *
    * @param ref The name of an installed extension. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSNotFoundException If the extension does not
    * exist.
    *
    * @throws PSExtensionException If an error occurred while removing
    * the extension.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void remove(PSExtensionRef ref)
      throws PSNotFoundException, PSExtensionException;

   /**
    * Returns a list of files that the extension requires by cataloging the
    * files in the resource directory.
    * @param def The extension def.  May not be <code>null</code>.
    * @return An Iterator over <code>0</code> or more URL objects.  Path is
    * relative to the code root of the extension.  Never <code>null</code>.
    * @throws IllegalArgumentException if def is <code>null</code>.
    * @throws PSExtensionException if the files cannot be located.
    */
   public Iterator getResources(IPSExtensionDef def)
      throws PSExtensionException;

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
    *
    * @throws PSExtensionException If the directory could not be
    * computed or if the extension name was not well-formed.
    */
   public File getCodeBase(IPSExtensionDef def)
      throws PSExtensionException;

}
