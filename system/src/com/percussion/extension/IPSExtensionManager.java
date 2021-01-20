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

import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * The extension manager is the sole object through which extensions and
 * extension handlers are installed, updated, removed, and inspected.
 * Only one singleton extension manager will be active at a
 * time (although this may not be the case in the future).
 * <P>
 * All communications between extension handlers and other server components
 * (including other extension handlers) shall be done through the extension
 * manager instance and nowhere else.
 * <P>
 * Implementations must be thread-safe, and all methods must execute
 * atomically. Errors in any extensions or extension handlers should not
 * cause the implementation to assume an inconsistent state.
 * <P>
 * Implementations of all methods should be failfast -- they should either
 * immediately and completely succeed or they should immediately raise an
 * exception.
 * <P>
 * Implementations should also be fault tolerant, with emphasis on isolating
 * extension-specific and handler-specific errors. A failure in a single
 * extension or extension handler should not affect other extensions or
 * extension handlers.
 * <P>
 * Implementors are also encouraged to delay resource consumption until
 * absolutely necessary, using lazy-loading of extension handlers.
 */
public interface IPSExtensionManager
{
   /*
    * DESIGN CONSIDERATIONS:
    *
    * 1) Why do many of these methods throw PSExtensionHandlerException?
    *
    *    ANSWER: Implementators are encouraged to delay handler
    *    loading until absolutely necessary. This means that, as a side
    *    effect of preparing an extension, the extension handler may have
    *    to be initialized for the first time.
    *
    * 2) Why can't the caller get their hands on extension handler objects?
    *
    *   ANSWER: If extension handler objects were globally accessible, then
    *   their implementations would have to be thread safe. This would make
    *   it more difficult for customers to write their own handlers. If we
    *   ensure that all extension handlers are managed only by the extension
    *   manager, then the extension manager can manage synchronization.
    *
    * 3) Why is there no installExtensionHandler(), removeExtensionHandler(),
    *    or updateExtensionHandler() ?
    *
    *   ANSWER: The new model treats extension handlers as merely
    *   another type of extension. They are handled by the built-in
    *   PSExtensionHandlerHandler extension handler (no kidding!).
    */

   /**
    * Initializes this extension manager.
    *
    * @param codeRoot The file under which all extensions and extension
    * handlers will be loaded, stored, and managed. Must not be
    * <CODE>null</CODE>.
    *
    * @param initProps Initialization properties. The only required
    * property is named "configFile" and its value is the filename of the
    * configuration file for this manager (understood to be relative
    * to <CODE>codeRoot</CODE>).
    *
    * @param isServer <code>true</code> if it is called from {@link PSServer};
    * otherwise it may called from a unit test (for example). 
    * 
    * @throws PSExtensionException If an error occurred that will prevent
    * the extension manager from working at all.
    */
   public void init(File codeRoot, Properties initProps, boolean isServer)
      throws PSExtensionException;

   /**
    * Convenience method that calls
    * {@link #init(File, Properties, boolean) init(File, Properties, false)}
    * 
    * @param codeRoot The file under which all extensions and extension handlers
    * will be loaded, stored, and managed. Must not be <CODE>null</CODE>.
    * 
    * @param initProps Initialization properties. The only required property is
    * named "configFile" and its value is the filename of the configuration file
    * for this manager (understood to be relative to <CODE>codeRoot</CODE>).
    * 
    * @throws PSExtensionException If an error occurred that will prevent the
    * extension manager from working at all.
    */
   public void init(File codeRoot, Properties initProps)
      throws PSExtensionException;

   /**
    * Shuts down the extension manager. Implementations should do their
    * best to shut down as cleanly as possible even under exceptional error
    * conditions. <B>However, implementations should prefer losing new data
    * over corrupting old data.</B>
    *
    * @throws PSExtensionException If an error occurred that may have
    * interfered with a clean shutdown.
    */
   public void shutdown()
      throws PSExtensionException;

   /**
    * Gets the names of all installed extension handlers, whether
    * they have been activated or not.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> PSExtensionRef objects.
    */
   public Iterator getExtensionHandlerNames();

   /**
    * Gets the names of all extensions which meet the specified
    * criteria.
    *
    * @param handlerNamePattern A case-sensitive SQL-like pattern for
    * the extension handler name. An extension will be returned only if its
    * handler's name matches this pattern. If <CODE>null</CODE>, then this
    * criteria is dropped from the search.
    *
    * @param context The context in which to search inside each handler. This
    * is not a pattern -- it is a literal context whose canonicalized version
    * will be used to further narrow down the list of extensions. If
    * <CODE>null</CODE>, this criteria will be dropped from the search
    * (will return extensions from all contexts within each handler).
    *
    * @param interfacePattern A case-sensitive SQL-like pattern for the
    * interfaces implemented by the extension. Only extensions which
    * implement an interface whose name matches this pattern will
    * be returned. If <CODE>null</CODE>, then this criteria is dropped
    * from the search.
    *
    * @param extensionNamePattern A case-sensitive SQL-like pattern
    * for the name of the extension. Only extensions whose name
    * matches this pattern will be returned. If <CODE>null</CODE>, then
    * this criteria will be dropped from the search.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> PSExtensionRefs.
    *
    * @throws PSExtensionException If the extension handler
    * encountered an error that prohibited it from starting.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getExtensionNames(
      String handlerNamePattern,
      String context,
      String interfacePattern,
      String extensionNamePattern
      )
      throws PSExtensionException;

   /**
    * Returns <CODE>true</CODE> if and only if the given extension ref refers
    * to an installed extension.
    *
    * @param ref The fully qualified extension name. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the appropriate handler
    * could not be initialized.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public boolean exists(PSExtensionRef ref)
      throws PSExtensionException;

   /**
    * Gets the extension definition for the extension with the
    * given fully qualified name.
    *
    * @param ref The fully qualified extension name. Must not
    * be <CODE>null</CODE>.
    *
    * @throws PSNotFoundException If the named extension or
    * its handler cannot be found.
    *
    * @throws PSExtensionException If the appropriate handler
    * could not be initialized.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSExtensionDef getExtensionDef(PSExtensionRef ref)
      throws PSExtensionException, PSNotFoundException;

   /**
    * Starts the extension handler with the given name if it is not
    * already started. This method is idempotent - starting a handler
    * that is already started will have no effect.
    *
    * @param handlerName The name of an installed extension handler. Must
    * not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension handler
    * encountered an error that prohibited it from starting.
    *
    * @throws PSNotFoundException If <CODE>name</CODE> does not refer
    * to an installed extension handler.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void startExtensionHandler(String handlerName)
      throws PSExtensionException, PSNotFoundException;

   /**
    * Stops the extension handler with the given name if it is not
    * already stopped. This method is idempotent - stopping a handler
    * that is already stopped will have no effect.
    *
    * @param handlerName The name of an installed extension handler. Must
    * not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension handler
    * encountered an error that prohibited it from shutting down
    * cleanly.
    *
    * @throws PSNotFoundException If <CODE>name</CODE> does not refer
    * to an installed extension handler.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void stopExtensionHandler(String handlerName)
      throws PSExtensionException, PSNotFoundException;

   /**
    * Installs the given extension with the appropriate handler. If
    * an extension with the same name is already installed in the
    * handler, then @link { #updateExtension updateExtension } must be
    * used instead.
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
    * @throws PSExtensionException If the extension definition fails
    * the handler's validation rules or if the extension could not be
    * loaded (some implementations may defer loading until prepareExtension
    * is called), or if the extension handler could not be started. In any
    * case, the defined extension will not be installed.
    *
    * @throws PSNotFoundException If the appropriate extension handler
    * does not exist. The defined extension will not be installed.
    *
    * @throws PSNonUniqueException If the extension already exists. Use
    * updateExtension instead. The defined extension will not be installed.
    */
   public void installExtension(IPSExtensionDef def, Iterator resources)
      throws PSExtensionException,
         PSNotFoundException,
         PSNonUniqueException;

   /**
    * Installs the given extension with the appropriate handler and
    * optional listener.
    * <p>
    * If an extension with the same name is already installed in the
    * handler, then @link { #updateExtension updateExtension } must be
    * used instead.
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
    * @param listener An optional extension listener. Can be
    * <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension definition fails
    * the handler's validation rules or if the extension could not be
    * loaded (some implementations may defer loading until prepareExtension
    * is called), or if the extension handler could not be started. In any
    * case, the defined extension will not be installed.
    *
    * @throws PSNotFoundException If the appropriate extension handler
    * does not exist. The defined extension will not be installed.
    *
    * @throws PSNonUniqueException If the extension already exists. Use
    * updateExtension instead. The defined extension will not be installed.
    */
   public void installExtension(IPSExtensionDef def,
      Iterator resources, IPSExtensionListener listener)
      throws PSExtensionException,
         PSNotFoundException,
         PSNonUniqueException;

   /**
    * Permanently disables and removes the extension with the given name.
    *
    * @param ext The name of the extension to be removed. Must not be
    * <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension handler
    * could not be started. The defined extension will not be installed.
    *
    * @throws PSNotFoundException If the appropriate extension handler
    * does not exist. The defined extension will not be installed.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void removeExtension(PSExtensionRef ext)
      throws PSNotFoundException, PSExtensionException;

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
    * @throws PSNotFoundException If the appropriate extension handler
    * does not exist. The defined extension will not be installed.
    *
    * @throws PSExtensionException If the extension definition fails
    * the handler's validation rules or if the extension could not be
    * loaded (some implementations may defer loading until prepareExtension
    * is called), or if the extension handler could not be started.
    *
    * @see #removeExtension
    * @see #installExtension
    */
   public void updateExtension(IPSExtensionDef def,
      Iterator resources
      )
      throws PSExtensionException,
         PSNotFoundException;

   /**
    * Prepares and returns the extension with the given name. The caller should
    * cast the returned object to the appropriate type for direct invocation.
    * Note: This method may return the same extension object when called with
    * the same arguments.
    *
    * @param ref The name of the extension to be prepared. Must not be
    * <CODE>null</CODE>.
    *
    * @param listener An optional extension listener. Can be <CODE>null</CODE>.
    *
    * @return An extension instance for the given extension, which may be invoked at
    * any time. Never <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension handler
    * could not be started. The defined extension will not be installed.
    *
    * @throws PSNotFoundException If the appropriate extension handler
    * does not exist. The defined extension will not be installed.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSExtension prepareExtension(PSExtensionRef ref,
      IPSExtensionListener listener
      )
      throws PSNotFoundException,
         PSExtensionException;
   
   /**
    * Registers the given listener for events concerning the extension
    * referred to be extRef.
    *
    * @param ref The extension ref. If <CODE>null</CODE> the listener
    * is registered to respond to any extension being registered or
    * unregistered.
    *
    * @param listener The listener. Must not be <CODE>null</CODE>.
    */
   public void registerListener(PSExtensionRef ref,
      IPSExtensionListener listener);
   
   /**
    * Unegisters the given listener for events concerning the extension
    * referred to be extRef. If the listener is not registered, nothing
    * will happen. If the extension does not exist, nothing will
    * happen.
    *
    * @param extRef The extension ref. May be <CODE>null</CODE> to unregister
    * a global listener.
    *
    * @param listener The listener. Must not be <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void unregisterListener(PSExtensionRef extRef,
      IPSExtensionListener listener);

   /**
    * Returns a list of URL objects for each resource file the extension
    * requires.
    *
    * @param ref The fully qualified extension name. Must not
    * be <CODE>null</CODE>.
    *
    * @return a list of URL objects.  May be <code>null</code> if no
    * files exist under the extension directory.
    *
    * @throws PSNotFoundException If the named extension or
    * its handler cannot be found.
    *
    * @throws IllegalArgumentException If ref is <code>null</code>.
    * @throws PSExtensionException If there is an error attempting to locate
    * the files.
    */
   public Iterator getExtensionFiles(PSExtensionRef ref)
      throws PSNotFoundException, PSExtensionException;

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
    * @throws IllegalArgumentException if def is <code>null</code>.
    * @throws PSExtensionException If the directory could not be
    * computed or if the extension name was not well-formed.
    * @throws PSNotFoundException If the named extension or
    * its handler cannot be found.
    */
   public File getCodeBase(IPSExtensionDef def)
      throws PSNotFoundException, PSExtensionException;

}
