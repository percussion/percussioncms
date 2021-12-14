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
package com.percussion.services.publisher;

import com.percussion.utils.guid.IPSGuid;

import java.util.Map;

/**
 * The Edition Task Definition. It defines a pre or post task that will be
 * executed when publishing an Edition.
 */
public interface IPSEditionTaskDef
{
   /**
    * Get the task id, the task id is the primary key identifying a particular
    * task.
    * 
    * @return the task id, never <code>null</code> for a persisted task.
    */
   IPSGuid getTaskId();

   /**
    * Set the task id
    * 
    * @param taskId the task id, never <code>null</code>.
    */
   void setTaskId(IPSGuid taskId);

   /**
    * Get the parent edition id.
    * 
    * @return the edition id, never <code>null</code> for a valid task.
    */
   IPSGuid getEditionId();

   /**
    * Set the parent edition id.
    * 
    * @param editionId the associated edition, never <code>null</code>.
    */
   void setEditionId(IPSGuid editionId);

   /**
    * Get the sequence number.
    * 
    * @return the sequence of this task, a negative sequence indicates the task
    *         should be executed before the edition, with the smaller value
    *         going first. A positive sequence indicates the task should be
    *         executed after the edition, a smaller value going first.
    */
   int getSequence();

   /**
    * Set the sequence number.
    * 
    * @param sequence the sequence, never <code>null</code>.
    */
   void setSequence(int sequence);

   /**
    * Get the extension name to be run. The extension must reference an
    * {@link IPSEditionTaskDef}.
    * 
    * @return the name of the extension to be run, may be <code>null</code>.
    */
   String getExtensionName();

   /**
    * Set the extension name.
    * 
    * @param extensionName the extension to be run.
    */
   void setExtensionName(String extensionName);

   /**
    * Get the continue on failure flag.
    * 
    * @return if this flag is <code>true</code> then this task can fail and
    *         the remaining tasks will be run. <code>false</code> indicates
    *         that if this task fails, then all future tasks should be
    *         cancelled. If a pre-edition task fails and this value is
    *         <code>false</code> then the edition will not be run.
    */
   boolean getContinueOnFailure();

   /**
    * Set the continue on failure flag.
    * 
    * @param continueOnFailure
    */
   void setContinueOnFailure(boolean continueOnFailure);

   /**
    * Get the parameters for this task.
    * 
    * @return the parameters, never <code>null</code> but could be empty.
    */
   Map<String, String> getParams();

   /**
    * If the passed parameter exists then this method updates the value,
    * otherwise this method will add the parameter.
    * 
    * @param parameterName the parameter name, never <code>null</code> or
    *            empty
    * @param value the parameter value, if <code>null</code> or empty the
    *            parameter will be removed.
    */
   void setParam(String parameterName, String value);

   /**
    * Remove the given parameter
    * @param name the parameter to remove, never <code>null</code> or empty.
    */
   void removeParam(String name);
   
   /**
    * @return get the hibernate version, <code>null</code> for unsaved
    * objects.
    */
   Integer getVersion();

}
