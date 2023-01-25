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

/***
 * Provides an interface for Password Filters to use when upgrading
 * encryption algorithms used in Password hashing.
 *
 */
public interface IPSPasswordFilterUpgrade {

    /***
     * Will encrypt the password using the hashing / encryption
     * routine used in the previous version of the software.
     *
     * This is to allow Security Providers to re-encrypt passwords
     * on login after a security update.
     *
     * @param password
     * @return
     */
    String legacyEncrypt(String password);

    String getLegacyAlgorithm();
}
