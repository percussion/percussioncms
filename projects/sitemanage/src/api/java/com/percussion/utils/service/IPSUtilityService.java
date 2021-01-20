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
    
    /**
     * Server property constant for SaaS flag.
     */
    public static final String SAAS_FLAG_SERVER_PROP = "doSAAS";
    
    public enum LogTypeEnum{
        info,debug,error
    }
    public enum LogCategoryEnum{
        General,
        PageOptimizer,
        SocialPromotion
    }
}
