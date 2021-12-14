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

package com.percussion.design.objectstore.server;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;

import java.util.EventListener;


/**
 * The IPSApplicationListener interface is implemented by classes
 * interested in trapping changes to an application's object
 * in the object store. This allows applications to immediately react to
 * the changes.
 * <P>
 * At this time, changes are not vetoable. The recipient is merely notified
 * of the change after the action has been processed and a response has
 * been sent to the originator.
 * <P>
 * We are also not supporting notification for only changed components.
 * For instance, if you're only interested in changes to the
 * a particular data set objects, you must implement applicationUpdated
 * and determine if the data set object you're interested in was changed
 * on your own.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSApplicationListener extends EventListener
{
   /**
    * Changes have been made to the application.
    * <P>
    * If the application has been modified, including a rename, the
    * applicationRenamed method will be called first, then the
    * applicationUpdated method. If a rename did not occur, 
    * applicationRenamed will not be called.
    *
    * @param   app         the application object
    */
   public void applicationUpdated(PSApplication app)
      throws PSSystemValidationException, PSServerException, PSNotFoundException;

   /**
    * A new application has been created.
    *
    * @param   app         the application object
    */
   public void applicationCreated(PSApplication app)
      throws PSSystemValidationException, PSServerException, PSNotFoundException;

   /**
    * The name of the application has been changed.
    * <P>
    * If additional changes have also been made to the application, the
    * applicationRenamed method will be called first, then the
    * applicationUpdated method.
    *
    * @param   app         the application object
    *
    * @param   oldName      the original name of the application
    *
    * @param   newName      the new name of the application
    */
   public void applicationRenamed(
      PSApplication app, String oldName, String newName);

   /**
    * The application has been removed from the object store.
    * It is guaranteed that no other information has changed.
    *
    * @param   app         the application object
    */
   public void applicationRemoved(PSApplication app);
}

