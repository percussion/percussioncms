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
