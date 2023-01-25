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
package com.percussion.services.security;

/**
 * This contains the authentication for a logged in user. 
 * @author dougrand
 */
public interface IPSAuthentication
{
   /**
    * @return the name of the logged in user
    */
   String getUserName();
   
   /**
    * Check to see if the user has the given role
    * @param roleName the name of a role, must never be <code>null</code>
    * @return <code>true</code> if the user is in the given role
    */
   boolean isUserInRole(String roleName);
}
