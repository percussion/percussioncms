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

package com.percussion.services.general;

/**
 * The IPSRhythmyxInfo interface is provided as a convenient mechanism to access
 * the server information like installation root directory, server http port and
 * https port etc. For each property a Key needs to be added here and that key
 * need to be set in the <code>{@link com.percussion.server.PSServer}</code>
 * init method. initialized in the server init method.
 * 
 */
public interface IPSRhythmyxInfo
{
   /**
    * Rhythmyx server properties.
    */
   public enum Key {
      ROOT_DIRECTORY, LISTENER_PORT, LISTENER_SSL_PORT, VERSION, UNIT_TESTING;
   }

   /**
    * Get the value of a Rhythmyx Information property.
    * 
    * @param key the Key of the property to retrieve, may not be
    *           <code>null</code> or empty.
    * 
    * @return the property's value, or <code>null</code> if it is not found.
    */
   public Object getProperty(Key key);
}
