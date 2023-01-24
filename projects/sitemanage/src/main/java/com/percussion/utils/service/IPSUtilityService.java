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

package com.percussion.utils.service;

public interface IPSUtilityService
{

    /**
     * Encrypts the provided string using the secret key if supplied, otherwise
     * use the default key
     * 
     * @param val The string needs to be encrypted and and can not be empty or
     * <code>null</code>
     * @param key key to be used to encrypt the string can be <code>null</code>
     * or empty
     * @return The encrypted string, never <code>null</code>, may be empty.
     */
    public String encryptString(String val, String key);

    /**
     * Decrypts the provided string using the secret key if supplied, otherwise
     * use the default key
     * 
     * @param val The string needs to be decrypted and can not be empty or
     * <code>null</code>
     * @param key To be used to decrypt the string and can be <code>null</code>
     * or empty
     * @return The decrypted string, never <code>null</code>, may be empty.
     */
    public String decryptString(String val, String key);
    
    /**
     * This is a generic log method written for logging messages from client.
     * @param type if blank or not one of the LogTypeEnum enum value then treated as 
     * LogTypeEnum.info
     * @param category if blank or not one of the LogCategoryEnum enum value then treated as
     * LogCategoryEnum.General
     * @param message if blank no message is logged.
     */
    public void log(String type, String category, String message);
    
    /**
     * If a property called doSAAS exists in server.properties file and it's value
     * is set to either true or yes, then the method returns <code>true</code>
     * otherwise <code>false</code>.
     * @return <code>true</code> if it is a SaaS environment.
     */
    public boolean isSaaSEnvironment();

    
    public enum LogTypeEnum{
        info,debug,error
    }
    public enum LogCategoryEnum{
        General,
        PageOptimizer,
        SocialPromotion
    }
}
