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
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.util.PSSortTool;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The extension manager is the sole object through which extensions and
 * extension handlers are installed, updated, removed, and inspected.
 * Only one singleton extension manager will be active at a
 * time (although this may not be the case in the future).
 * <P>
 * All communications between extension handlers and other server components
 * (including other extension handlers) shall be done through the extension
 * manager instance and nowhere else.
 */
public class PSExtensionManager
   implements IPSExtensionManager
{
   private static final Logger log = LogManager.getLogger(PSExtensionManager.class);
   
   /***
    * Key to store listeners that respond to any change in the extensions 
    * manager.
    */
   private static final String GLOBAL_LISTENER = "GLOBAL_LISTENER";
   
   /**
    * Constructs a new extension manager that does nothing. Call init
    * before using.
    */
   public PSExtensionManager()
   {
      m_isInited = false;
      m_listeners = new HashMap<>();
   }

   /*
    * see IPSExtensionManager interface method for details
    */
   public synchronized void init(File codeRoot, Properties initProps)
   throws PSExtensionException
   {
      init(codeRoot, initProps, false);
   }

   /*
    * see IPSExtensionManager interface method for details
    */
   public synchronized void init(File codeRoot, Properties initProps,
         boolean isServer) throws PSExtensionException
   {
      if (codeRoot == null)
         throw new IllegalArgumentException("codeRoot cannot be null");

      if (initProps == null)
         throw new IllegalArgumentException("initProps cannot be null");

      if (null == initProps.getProperty(
         IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME))
      {
         throw new IllegalArgumentException("configFile must be specified");
      }

      if (m_isInited)
         return;

      log.info("Initializing extension manager.");

      // initialize the handler-handler - it has a "fake" handler itself
      PSExtensionRef hhRef = new PSExtensionRef(
         "PSExtensionManager",
         IPSExtensionHandler.HANDLER_CONTEXT,
         IPSExtensionHandler.HANDLER_HANDLER);

      Properties hhProps = new Properties();
      hhProps.setProperty(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);

      PSExtensionDef hhDef = new PSExtensionDef(
         hhRef,
         PSIteratorUtils.iterator(IPSExtensionHandler.class.getName()),
         null,
         hhProps,
         null);

      m_extHandlerHandler = new PSExtensionHandlerHandler();
      m_extHandlerHandler.init(hhDef, codeRoot);
      if (isServer)
      {
         m_extHandlerHandler.addListenerForFileChanges();
      }

      m_isInited = true;

      log.info("Initialization successful.");
   }

   /**
    * Shuts down the extension manager. Implementations should do their
    * best to shut down as cleanly as possible even under exceptional error
    * conditions. <B>However, implementations should prefer losing new data
    * over corrupting old data.</B>
    *
    * @throws PSExtensionException If an error occurred that may have
    * interfered with a clean shutdown.
    */
   public synchronized void shutdown()
      throws PSExtensionException
   {
      if (!m_isInited)
         return;

      log.info("Shutting down extension manager.");

      try
      {
         m_extHandlerHandler.shutdown();
      }
      finally
      {
         m_extHandlerHandler = null;
         m_listeners = null;
         m_isInited = false;
      }

      log.info("Shutdown successful.");
   }

   /**
    * Gets the names of all installed extension handlers, whether
    * they have been activated or not.
    *
    * @return A non-<CODE>null</CODE> Iterator over 0 or more
    * non-<CODE>null</CODE> PSExtensionRef objects.
    */
   public synchronized Iterator getExtensionHandlerNames()
   {
      checkState();
      return m_extHandlerHandler.getExtensionNames();
   }

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
    * non-<CODE>null</CODE> PSExtensionRefs.  They are sorted by extension name,
    * ascending, case insensitive.
    *
    * @throws PSExtensionException If the extension handler
    * encountered an error that prohibited it from starting.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized Iterator getExtensionNames(
      String handlerNamePattern,
      String context,
      String interfacePattern,
      String extensionNamePattern
      )
      throws PSExtensionException
   {
      checkState();

      // build the appropriate pattern matchers
      PSPatternMatcher hPatMat = null;
      if (handlerNamePattern != null)
      {
         hPatMat = PSPatternMatcher.SQLPatternMatcher(handlerNamePattern);
      }

      PSPatternMatcher iPatMat = null;
      if (interfacePattern != null)
      {
         iPatMat = PSPatternMatcher.SQLPatternMatcher(interfacePattern);
      }

      PSPatternMatcher ePatMat = null;
      if (extensionNamePattern != null)
      {
         ePatMat = PSPatternMatcher.SQLPatternMatcher(extensionNamePattern);
      }

      Collection<PSExtensionRef> matches = 
         new LinkedList<>(); // store matches in here
      // for all extension handlers
      for (Iterator i = getExtensionHandlerNames(); i.hasNext(); )
      {
         PSExtensionRef handlerRef = (PSExtensionRef)(i.next());
         String hName = handlerRef.getExtensionName();
         if ( (hPatMat != null) && (!hPatMat.doesMatchPattern(hName)) )
         {
            continue;
         }

         try
         {
            // prepare the handler so that we can catalog from it
            IPSExtensionHandler extHandler =
               (IPSExtensionHandler)(m_extHandlerHandler.prepare(handlerRef));

            // for all extensions in this handler (optionally within context)
            Iterator j = null;
            if (context != null)
               j = extHandler.getExtensionNames(context);
            else
               j = extHandler.getExtensionNames();

            while (j != null && j.hasNext())
            {
               PSExtensionRef ref = (PSExtensionRef)j.next();
               String eName = ref.getExtensionName();
               if ( (ePatMat != null) && (!ePatMat.doesMatchPattern(eName)) )
               {
                  continue;
               }

               // for all interfaces implemented by this extension
               IPSExtensionDef def = extHandler.getExtensionDef(ref);
               for (Iterator k = def.getInterfaces(); k.hasNext(); )
               {
                  String iName = (String)k.next();
                  if ( (iPatMat != null) && (!iPatMat.doesMatchPattern(iName)))
                  {
                     continue;
                  }

                  matches.add(def.getRef());
                  break; // one interface matches, that's all we need
               } // end forall interfaces
            } // end forall extensions
         }
         catch (PSNotFoundException e)
         {
            // this should never happen because we are only querying
            // extensions and handlers that we know exist
            // TODO: log abnormal situation
         }

      } // end forall extension handlers

      // sort the list before returning it
      PSExtensionRef[] sortList = new PSExtensionRef[matches.size()];
      matches.toArray(sortList);
      PSSortTool.MergeSort(sortList, PSExtensionRef.getComparator());
      
      matches.clear();
      for (int i = 0; i < sortList.length; i++)
         matches.add(sortList[i]);

      return matches.iterator();
   }


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
      throws PSNotFoundException, PSExtensionException
   {
      if (ref == null)
         throw new IllegalArgumentException("ref may not be null.");

      IPSExtensionDef def = getExtensionDef(ref);

      // get the supplied resource list.  If null, we need to catalog the list
      Iterator resources = def.getSuppliedResources();
      if (resources == null)
      {
         // get the handler
         IPSExtensionHandler handler;
         String handlerName = ref.getHandlerName();
         if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
         {
            handler = m_extHandlerHandler;
         }
         else
         {
            PSExtensionRef handlerRef = getHandlerRef(handlerName);
            handler = (IPSExtensionHandler)(m_extHandlerHandler.prepare(
               handlerRef));
         }

         // catalog the files
         resources = handler.getResources(def);
      }
      
      return resources;
   }

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
      throws PSExtensionException
   {
      checkState();

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionHandler handler;
      String handlerName = ref.getHandlerName();
      
      try
      {
         if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
         {
            handler = m_extHandlerHandler;
         }
         else
         {
            PSExtensionRef handlerRef = getHandlerRef(handlerName);
            handler = (IPSExtensionHandler)(m_extHandlerHandler.prepare(
               handlerRef));
         }
         
         return handler.exists(ref);
      }
      catch (PSNotFoundException e)
      {
         // ignore
      }

      return false;
   }

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
      throws PSExtensionException, PSNotFoundException
   {
      checkState();

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      IPSExtensionHandler handler;
      String handlerName = ref.getHandlerName();
      if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
      {
         handler = m_extHandlerHandler;
      }
      else
      {
         PSExtensionRef handlerRef = getHandlerRef(handlerName);
         handler = (IPSExtensionHandler)(m_extHandlerHandler.prepare(
            handlerRef));
      }

      return handler.getExtensionDef(ref);
   }

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
   public synchronized void startExtensionHandler(String handlerName)
      throws PSExtensionException, PSNotFoundException
   {
      checkState();
      // initialize the handle if not already initialized
      PSExtensionRef ref = getHandlerRef(handlerName);
      m_extHandlerHandler.prepare(ref);
   }

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
   public synchronized void stopExtensionHandler(String handlerName)
      throws PSExtensionException, PSNotFoundException
   {
      checkState();
      PSExtensionRef ref = getHandlerRef(handlerName);
      IPSExtensionHandler extHandler =
         (IPSExtensionHandler)(m_extHandlerHandler.prepare(ref));

      extHandler.shutdown();
   }

   /**
    * Installs the given extension with the appropriate handler. If
    * an extension with the same name is already installed in the
    * handler, then {@link #updateExtension updateExtension} must be
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
   public synchronized void installExtension(IPSExtensionDef def,
      Iterator resources
      )
      throws PSExtensionException,
         PSNotFoundException,
         PSNonUniqueException
   {
      checkState();
      installExtension(def, resources, null);
   }

   /**
    * Installs the given extension with the appropriate handler and
    * optional listener.
    * <p>
    * If an extension with the same name is already installed in the
    * handler, then {@link #updateExtension updateExtension} must be
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
   public synchronized void installExtension(IPSExtensionDef def,
      Iterator resources, IPSExtensionListener listener
      )
      throws PSExtensionException,
         PSNotFoundException,
         PSNonUniqueException
   {
      checkState();
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (resources == null)
         throw new IllegalArgumentException("resources cannot be null");

      String handlerName = def.getRef().getHandlerName();
      IPSExtensionHandler extHandler = null;

      if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
      {
         extHandler = m_extHandlerHandler;
      }
      else
      {
         if (listener != null)
         {
            registerListener(def.getRef(), listener);
         }

         // prepare the handler so we can install the extension to it
         PSExtensionRef handlerRef = getHandlerRef(handlerName);
         extHandler = 
            (IPSExtensionHandler)(m_extHandlerHandler.prepare(handlerRef));
      }

      extHandler.install(def, resources);
   }

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
   public synchronized void removeExtension(PSExtensionRef ref)
      throws PSNotFoundException, PSExtensionException
   {
      checkState();
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      // prepare the handler so we can remove the extension from it
      String handlerName = ref.getHandlerName();
      IPSExtensionHandler extHandler = null;

      if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
      {
         extHandler = m_extHandlerHandler;
      }
      else
      {
         PSExtensionRef handlerRef = getHandlerRef(handlerName);
         extHandler =
            (IPSExtensionHandler)(m_extHandlerHandler.prepare(handlerRef));
      }

      extHandler.remove(ref);
      notifyRemove(ref);
   }

   /**
    * Synchronously removes and re-installs the given extension. Currently
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
   public synchronized void updateExtension(IPSExtensionDef def,
      Iterator resources
      )
      throws PSExtensionException,
         PSNotFoundException
   {
      checkState();
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (resources == null)
         throw new IllegalArgumentException("resources cannot be null");


      // prepare extension handler so we can update the extension into it
      String handlerName = def.getRef().getHandlerName();
      IPSExtensionHandler extHandler = null;

      if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
      {
         extHandler = m_extHandlerHandler;
      }
      else
      {
         PSExtensionRef handlerRef = getHandlerRef(handlerName);
         extHandler =
            (IPSExtensionHandler)(m_extHandlerHandler.prepare(handlerRef));
      }

      extHandler.update(def, resources);
      notifyUpdate(def.getRef());
   }

   /**
    * Prepares and returns the extension with the given name. The caller should
    * cast the returned object to the appropriate type for direct invocation.
    * Note: This method may return the same extension object when called with
    * the same arguments.
    *
    * @param ref The name of the extension to be prepared. Must not be
    * <CODE>null</CODE>.
    *
    * @param listener An optional extension listener
    *
    * @return An extension runner for the given extension, which may be invoked at
    * any time. Never <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the extension handler
    * could not be started. The defined extension will not be installed.
    *
    * @throws PSNotFoundException If the appropriate extension handler
    * or extension does not exist. The defined extension will not be installed.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized IPSExtension prepareExtension(PSExtensionRef ref,
      IPSExtensionListener listener)
      throws PSNotFoundException,
         PSExtensionException
   {
      checkState();

      // prepare the handler so it can prepare the extension
      String handlerName = ref.getHandlerName();
      PSExtensionRef handlerRef = getHandlerRef(handlerName);
      IPSExtensionHandler extHandler =
         (IPSExtensionHandler)(m_extHandlerHandler.prepare(handlerRef));

      return extHandler.prepare(ref);
   }

   /**
    * Unegisters the given listener for events concerning the extension
    * referred to be extRef. If the listener is not registered, nothing
    * will happen. If the extension does not exist, nothing will
    * happen.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @param listener The listener. Must not be <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized void unregisterListener(PSExtensionRef ref,
      IPSExtensionListener listener)
   {
      checkState();
      Object key = ref != null ? ref : GLOBAL_LISTENER;
      
      Collection<IPSExtensionListener> listeners = m_listeners.get(key);
      if (listeners != null)
      {
         listeners.remove(listener);
      }
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
    *
    * @throws IllegalArgumentException if def is <code>null</code>.
    * @throws PSExtensionException If the directory could not be
    * computed or if the extension name was not well-formed.
    * @throws PSNotFoundException If the named extension or
    * its handler cannot be found.
    */
   public File getCodeBase(IPSExtensionDef def)
      throws PSNotFoundException, PSExtensionException
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      // get the ref
      PSExtensionRef ref = def.getRef();

      // get the handler
      IPSExtensionHandler handler;
      String handlerName = ref.getHandlerName();
      if (handlerName.equals(IPSExtensionHandler.HANDLER_HANDLER))
      {
         handler = m_extHandlerHandler;
      }
      else
      {
         PSExtensionRef handlerRef = getHandlerRef(handlerName);
         handler = (IPSExtensionHandler)(m_extHandlerHandler.prepare(
            handlerRef));
      }

      return handler.getCodeBase(def);
   }


   /**
    * Notifies all listeners registered for the given extension
    * that the extension has been removed.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    */
   private synchronized void notifyRemove(PSExtensionRef ref)
   {
      Collection<IPSExtensionListener> listeners = m_listeners.get(ref);
      if (listeners != null)
      {
         for (IPSExtensionListener listener : listeners)
         {
            try
            {
               listener.extensionRemoved(ref, this);
            }
            catch (Throwable t)
            {
               log.warn("Listener threw exception on remove ", t);
            }
         }
      }
      listeners = m_listeners.get(GLOBAL_LISTENER);
      if (listeners != null)
      {
         for (IPSExtensionListener listener : listeners)
         {
            try
            {
               listener.extensionRemoved(ref, this);
            }
            catch (Throwable t)
            {
               log.warn("Listener threw exception on remove ", t);
            }
         }
      }      
   }

   /**
    * Notifies all listeners registered for the given extension
    * that the extension has been updated.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    */
   private synchronized void notifyUpdate(PSExtensionRef ref)
   {
      Collection<IPSExtensionListener> listeners = m_listeners.get(ref);
      if (listeners != null)
      {
         for (IPSExtensionListener listener : listeners)
         {
            try
            {
               listener.extensionUpdated(ref, this);
            }
            catch (Throwable t)
            {
               log.warn("Listener threw exception on update ", t);
            }
         }
      }
      listeners = m_listeners.get(GLOBAL_LISTENER);
      if (listeners != null)
      {
         for (IPSExtensionListener listener : listeners)
         {
            try
            {
               listener.extensionUpdated(ref, this);
            }
            catch (Throwable t)
            {
               log.warn("Listener threw exception on update ", t);
            }
         }
      }      
   }

   /**
    * Notifies all global listeners about additions
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    */
   public synchronized void notifyAdd(PSExtensionRef ref)
   {
      Collection<IPSExtensionListener> listeners = m_listeners.get(GLOBAL_LISTENER);
      if (listeners != null)
      {
         for (IPSExtensionListener listener : listeners)
         {
            try
            {
               listener.extensionAdded(ref, this);
            }
            catch (Throwable t)
            {
               log.warn("Listener threw exception on update ", t);
            }
         }
      }      
   }
   
   /**
    * Registers the given listener for events concerning the extension
    * referred to be extRef.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @param listener The listener. Must not be <CODE>null</CODE>.
    */
   public synchronized void registerListener(PSExtensionRef ref,
      IPSExtensionListener listener)
   {
      Object key = ref != null ? ref : GLOBAL_LISTENER;
      
      Collection<IPSExtensionListener> listeners = m_listeners.get(key);
      if (listeners == null)
      {
         listeners = new LinkedList<>();
         m_listeners.put(key, listeners);
      }

      listeners.add(listener);
   }

   /**
    * If this object has not been initialized (or has been
    * shutdown without being reinitialized), throws an
    * IllegalStateException.
    */
   private void checkState()
   {
      if (!m_isInited)
      {
         throw new IllegalStateException(
            "Extension manager has not been initialized.");
      }
   }

   private PSExtensionRef getHandlerRef(String handlerName)
   {
      return PSExtensionRef.handlerRef(handlerName);
   }

   /**
    * This class is used to convert an Iterator over PSExtensionRefs
    * into an Iterator over the extension names from those refs.
    */
   static class PSExtensionRefNameIterator implements Iterator
   {
      public PSExtensionRefNameIterator(Iterator refs)
      {
         m_refs = refs;
      }

      public boolean hasNext()
      {
         return m_refs.hasNext();
      }

      public Object next()
      {
         PSExtensionRef ref = (PSExtensionRef)m_refs.next();
         return ref.getExtensionName();
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }

      private Iterator m_refs;
   }

   /** <CODE>true</CODE> if and only if this manager has been initialized */
   private boolean m_isInited;

   /** The handler for all extension handlers. */
   private PSExtensionHandlerHandler m_extHandlerHandler;

   /**
    * The map from extension ref to a Collection of listeners for that
    * extension.
    */
   private Map<Object,Collection<IPSExtensionListener>> m_listeners;
}
