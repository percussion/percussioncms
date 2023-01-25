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

import java.util.EventListener;

/**
 * An extension listener can receive notifications of events that relate
 * to an extension.
 */
public interface IPSExtensionListener extends EventListener
{
   /**
    * Notification that the given extension has been updated in the given
    * manager.
    *
    * @param ref The extension name and handler name. Never <CODE>null</CODE>.
    * @param mgr The extension manager. Never <CODE>null</CODE>.
    */
   public void extensionUpdated(PSExtensionRef ref, IPSExtensionManager mgr);

   /**
    * Notification that the given extension has been removed from the
    * given manager.
    *
    * @param ref The extension name and handler name. Never <CODE>null</CODE>.
    * @param mgr The extension manager. Never <CODE>null</CODE>.
    */
   public void extensionRemoved(PSExtensionRef ref, IPSExtensionManager mgr);

   /**
    * Notification that the given extension has been disabled somehow,
    * without being removed. Depending on the implementation, this event
    * map imply that the extension will not function correctly.
    *
    * @param ref The extension name and handler name. Never <CODE>null</CODE>.
    * @param mgr The extension manager. Never <CODE>null</CODE>.
    */
   public void extensionShutdown(PSExtensionRef ref, IPSExtensionManager mgr);

   /**
    * Notification that an extension has been added to the extension manager.
    * This only needs to be implemented for global listeners, others can create
    * an empty method. Will only be called if registered as a global listener.
    * 
    * @param ref The extension name and handler name. Never <CODE>null</CODE>.
    * @param mgr The extension manager. Never <CODE>null</CODE>.
    */
   public void extensionAdded(PSExtensionRef ref, PSExtensionManager manager);
}
