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
package com.percussion.workflow;

/**
 * An interface that defines methods for notifications context which
 * provides methods for accessing the subject and content of notifications
 * associated with a particular workflow and transition.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
@Deprecated
public interface IPSNotificationsContext
{
/**
 * Gets the subject field for the current entry in the context.
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   none
 *
 * @return  subject 
 */
   public String getSubject();
   
/**
 * Gets Body field for the current entry in the context.
 * @author   Ram
 *
 * @version 1.0
 *
 * @param   none
 *
 * @return  Body 
 */
   public String getBody();
}
