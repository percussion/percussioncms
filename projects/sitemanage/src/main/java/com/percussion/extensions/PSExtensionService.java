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

package com.percussion.extensions;

import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.*;
import com.percussion.server.PSServer;
import com.percussion.share.dao.impl.PSExtensionManagerFactory;

import java.io.File;
import java.util.Iterator;

public class PSExtensionService implements IPSExtensionService{

    private PSExtensionManager manager = null;

    public PSExtensionService(){
        manager = (PSExtensionManager) PSServer
                .getExtensionManager(null);
    }

    /**
     * Gets the names of all installed extension handlers, whether
     * they have been activated or not.
     *
     * @return A non-<CODE>null</CODE> Iterator over 0 or more
     * non-<CODE>null</CODE> PSExtensionRef objects.
     */
    @Override
    public Iterator<PSExtensionRef> getExtensionHandlerNames() {
        return manager.getExtensionHandlerNames();
    }

    /**
     * Gets the names of all extensions which meet the specified
     * criteria.
     *
     * @param handlerNamePattern   A case-sensitive SQL-like pattern for
     *                             the extension handler name. An extension will be returned only if its
     *                             handler's name matches this pattern. If <CODE>null</CODE>, then this
     *                             criteria is dropped from the search.
     * @param context              The context in which to search inside each handler. This
     *                             is not a pattern -- it is a literal context whose canonicalized version
     *                             will be used to further narrow down the list of extensions. If
     *                             <CODE>null</CODE>, this criteria will be dropped from the search
     *                             (will return extensions from all contexts within each handler).
     * @param interfacePattern     A case-sensitive SQL-like pattern for the
     *                             interfaces implemented by the extension. Only extensions which
     *                             implement an interface whose name matches this pattern will
     *                             be returned. If <CODE>null</CODE>, then this criteria is dropped
     *                             from the search.
     * @param extensionNamePattern A case-sensitive SQL-like pattern
     *                             for the name of the extension. Only extensions whose name
     *                             matches this pattern will be returned. If <CODE>null</CODE>, then
     *                             this criteria will be dropped from the search.
     * @return A non-<CODE>null</CODE> Iterator over 0 or more
     * non-<CODE>null</CODE> PSExtensionRefs.  They are sorted by extension name,
     * ascending, case insensitive.
     * @throws PSExtensionException     If the extension handler
     *                                  encountered an error that prohibited it from starting.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public Iterator<PSExtensionRef> getExtensionNames(String handlerNamePattern, String context, String interfacePattern, String extensionNamePattern) throws PSExtensionException {
        return manager.getExtensionNames(handlerNamePattern,context,interfacePattern,extensionNamePattern);
    }

    /**
     * Returns a list of URL objects for each resource file the extension
     * requires.
     *
     * @param ref The fully qualified extension name. Must not
     *            be <CODE>null</CODE>.
     * @return a list of URL objects.  May be <code>null</code> if no
     * files exist under the extension directory.
     * @throws PSNotFoundException      If the named extension or
     *                                  its handler cannot be found.
     * @throws IllegalArgumentException If ref is <code>null</code>.
     * @throws PSExtensionException     If there is an error attempting to locate
     *                                  the files.
     */
    @Override
    public Iterator getExtensionFiles(PSExtensionRef ref) throws PSNotFoundException, PSExtensionException {
        return manager.getExtensionFiles(ref);
    }

    /**
     * Returns <CODE>true</CODE> if and only if the given extension ref refers
     * to an installed extension.
     *
     * @param ref The fully qualified extension name. Must not be
     *            <CODE>null</CODE>.
     * @throws PSExtensionException     If the appropriate handler
     *                                  could not be initialized.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public boolean exists(PSExtensionRef ref) throws PSExtensionException {
        return manager.exists(ref);
    }

    /**
     * Gets the extension definition for the extension with the
     * given fully qualified name.
     *
     * @param ref The fully qualified extension name. Must not
     *            be <CODE>null</CODE>.
     * @throws PSNotFoundException      If the named extension or
     *                                  its handler cannot be found.
     * @throws PSExtensionException     If the appropriate handler
     *                                  could not be initialized.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public IPSExtensionDef getExtensionDef(PSExtensionRef ref) throws PSExtensionException, PSNotFoundException {
        return manager.getExtensionDef(ref);
    }

    /**
     * Starts the extension handler with the given name if it is not
     * already started. This method is idempotent - starting a handler
     * that is already started will have no effect.
     *
     * @param handlerName The name of an installed extension handler. Must
     *                    not be <CODE>null</CODE>.
     * @throws PSExtensionException     If the extension handler
     *                                  encountered an error that prohibited it from starting.
     * @throws PSNotFoundException      If <CODE>name</CODE> does not refer
     *                                  to an installed extension handler.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void startExtensionHandler(String handlerName) throws PSExtensionException, PSNotFoundException {
        manager.startExtensionHandler(handlerName);
    }

    /**
     * Stops the extension handler with the given name if it is not
     * already stopped. This method is idempotent - stopping a handler
     * that is already stopped will have no effect.
     *
     * @param handlerName The name of an installed extension handler. Must
     *                    not be <CODE>null</CODE>.
     * @throws PSExtensionException     If the extension handler
     *                                  encountered an error that prohibited it from shutting down
     *                                  cleanly.
     * @throws PSNotFoundException      If <CODE>name</CODE> does not refer
     *                                  to an installed extension handler.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void stopExtensionHandler(String handlerName) throws PSExtensionException, PSNotFoundException {
        manager.stopExtensionHandler(handlerName);
    }

    /**
     * Installs the given extension with the appropriate handler. If
     * an extension with the same name is already installed in the
     * handler, then {@link #updateExtension updateExtension} must be
     * used instead.
     *
     * @param def       The extension definition. Must not be <CODE>null</CODE>.
     * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
     *                  named IPSMimeContent objects specifying any resources that should be
     *                  saved along with the extension. The resources may or may not
     *                  correspond to the URLs returned from the def's
     *                  <CODE>getResourceLocations()</CODE> method. Must not be
     *                  <CODE>null</CODE>.
     * @throws PSExtensionException If the extension definition fails
     *                              the handler's validation rules or if the extension could not be
     *                              loaded (some implementations may defer loading until prepareExtension
     *                              is called), or if the extension handler could not be started. In any
     *                              case, the defined extension will not be installed.
     * @throws PSNotFoundException  If the appropriate extension handler
     *                              does not exist. The defined extension will not be installed.
     * @throws PSNonUniqueException If the extension already exists. Use
     *                              updateExtension instead. The defined extension will not be installed.
     */
    @Override
    public void installExtension(IPSExtensionDef def, Iterator resources) throws PSExtensionException, PSNotFoundException, PSNonUniqueException {
        manager.installExtension(def,resources);
    }

    /**
     * Installs the given extension with the appropriate handler and
     * optional listener.
     * <p>
     * If an extension with the same name is already installed in the
     * handler, then {@link #updateExtension updateExtension} must be
     * used instead.
     *
     * @param def       The extension definition. Must not be <CODE>null</CODE>.
     * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
     *                  named IPSMimeContent objects specifying any resources that should be
     *                  saved along with the extension. The resources may or may not
     *                  correspond to the URLs returned from the def's
     *                  <CODE>getResourceLocations()</CODE> method. Must not be
     *                  <CODE>null</CODE>.
     * @param listener  An optional extension listener. Can be
     *                  <CODE>null</CODE>.
     * @throws PSExtensionException If the extension definition fails
     *                              the handler's validation rules or if the extension could not be
     *                              loaded (some implementations may defer loading until prepareExtension
     *                              is called), or if the extension handler could not be started. In any
     *                              case, the defined extension will not be installed.
     * @throws PSNotFoundException  If the appropriate extension handler
     *                              does not exist. The defined extension will not be installed.
     * @throws PSNonUniqueException If the extension already exists. Use
     *                              updateExtension instead. The defined extension will not be installed.
     */
    @Override
    public void installExtension(IPSExtensionDef def, Iterator resources, IPSExtensionListener listener) throws PSExtensionException, PSNotFoundException, PSNonUniqueException {
        manager.installExtension(def,resources,listener);
    }

    /**
     * Permanently disables and removes the extension with the given name.
     *
     * @param ref@throws PSExtensionException If the extension handler
     *                   could not be started. The defined extension will not be installed.
     * @throws PSNotFoundException      If the appropriate extension handler
     *                                  does not exist. The defined extension will not be installed.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void removeExtension(PSExtensionRef ref) throws PSNotFoundException, PSExtensionException {
        manager.removeExtension(ref);
    }

    /**
     * Synchronously removes and re-installs the given extension. Currently
     * this method is not guaranteed to be transactional, that is, the remove
     * may succeed but the install may fail.
     *
     * @param def       The extension definition. Must not be <CODE>null</CODE>.
     * @param resources An Iterator over 0 or more non-<CODE>null</CODE>
     *                  named IPSMimeContent objects specifying any resources that should be
     *                  saved along with the extension. The resources may or may not
     *                  correspond to the URLs returned from the def's
     *                  <CODE>getResourceLocations()</CODE> method. Must not be
     *                  <CODE>null</CODE>.
     * @throws PSNotFoundException  If the appropriate extension handler
     *                              does not exist. The defined extension will not be installed.
     * @throws PSExtensionException If the extension definition fails
     *                              the handler's validation rules or if the extension could not be
     *                              loaded (some implementations may defer loading until prepareExtension
     *                              is called), or if the extension handler could not be started.
     * @see #removeExtension
     * @see #installExtension
     */
    @Override
    public void updateExtension(IPSExtensionDef def, Iterator resources) throws PSExtensionException, PSNotFoundException {
        manager.updateExtension(def,resources);
    }

    /**
     * Prepares and returns the extension with the given name. The caller should
     * cast the returned object to the appropriate type for direct invocation.
     * Note: This method may return the same extension object when called with
     * the same arguments.
     *
     * @param ref      The name of the extension to be prepared. Must not be
     *                 <CODE>null</CODE>.
     * @param listener An optional extension listener
     * @return An extension runner for the given extension, which may be invoked at
     * any time. Never <CODE>null</CODE>.
     * @throws PSExtensionException     If the extension handler
     *                                  could not be started. The defined extension will not be installed.
     * @throws PSNotFoundException      If the appropriate extension handler
     *                                  or extension does not exist. The defined extension will not be installed.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public IPSExtension prepareExtension(PSExtensionRef ref, IPSExtensionListener listener) throws PSNotFoundException, PSExtensionException {
        return manager.prepareExtension(ref,listener);
    }

    /**
     * Unegisters the given listener for events concerning the extension
     * referred to be extRef. If the listener is not registered, nothing
     * will happen. If the extension does not exist, nothing will
     * happen.
     *
     * @param ref      The extension ref. Must not be <CODE>null</CODE>.
     * @param listener The listener. Must not be <CODE>null</CODE>.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void unregisterListener(PSExtensionRef ref, IPSExtensionListener listener) {
        manager.unregisterListener(ref,listener);
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
     * @return The directory in which the given extension and all
     * of its resources would be stored. Never <CODE>null</CODE>.
     * @throws IllegalArgumentException if def is <code>null</code>.
     * @throws PSExtensionException     If the directory could not be
     *                                  computed or if the extension name was not well-formed.
     * @throws PSNotFoundException      If the named extension or
     *                                  its handler cannot be found.
     */
    @Override
    public File getCodeBase(IPSExtensionDef def) throws PSNotFoundException, PSExtensionException {
        return manager.getCodeBase(def);
    }

    /**
     * Notifies all global listeners about additions
     *
     * @param ref The extension ref. Must not be <CODE>null</CODE>.
     */
    @Override
    public void notifyAdd(PSExtensionRef ref) {
        manager.notifyAdd(ref);
    }

    /**
     * Registers the given listener for events concerning the extension
     * referred to be extRef.
     *
     * @param ref      The extension ref. Must not be <CODE>null</CODE>.
     * @param listener The listener. Must not be <CODE>null</CODE>.
     */
    @Override
    public void registerListener(PSExtensionRef ref, IPSExtensionListener listener) {
        manager.registerListener(ref, listener);
    }
}
