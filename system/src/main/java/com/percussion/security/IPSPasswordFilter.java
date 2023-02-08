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
package com.percussion.security;

import com.percussion.extension.IPSExtension;

/**
 * IPSPasswordFilter is an interface to password encryption. Often when
 * passwords are stored (in a file, database, or wherever), they are first
 * encrypted. When authentication occurs, the password that is entered
 * by the user is encrypted by the same process and the encrypted versions
 * are compared byte-for-byte.
 *
 * IMPORTANT: Implementing classes are required to provide a meaningful
 * no-arguments constructor that will produce a working filter.
 */
public interface IPSPasswordFilter extends IPSExtension, IPSPasswordFilterUpgrade
{
   /**
    * This method is called by the Percussion CMS security provider before
    * authenticating a user. The password submitted in the request is
    * run through this filter, then checked against the stored password
    * character-for-character.
    *
    * @param password The clear-text password to be encrypted. Never
    * <CODE>null</CODE>.
    *
    * @return A string containing the encrypted password. Never
    * <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public String encrypt(String password);


   String getAlgorithm();
}
