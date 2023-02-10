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

package com.percussion.design.objectstore.server;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.error.PSNotFoundException;
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

