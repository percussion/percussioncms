/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.security;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.Properties;

/**
 * Class can be used to encrypt and decrypt specified
 * properties in the specified properties file.
 * This class wraps the encrypted property
 * in a prefix "ENC=(" and a suffix ")".  Also includes
 * a decrypter which unwraps the prefix and suffix from the
 * string and calls PSEncrypter to decrypt a property.
 *
 * @see PSEncryptor
 * @author chriswright
 *
 */
public class PSEncryptProperties {

    private static final Logger log = LogManager.getLogger(
            PSEncryptProperties.class.getName());

    private static final String ERROR_PROPS_FILE = "Properties file cannot be null.";

    private static final String ERROR_PROPS = "Properties to encrypt cannot be null.";

    private static final String PREFIX = "ENC(";

    private static final String SUFFIX = ")";

    /**
     * Private default ctor.
     */
    private PSEncryptProperties() {}

    /**
     * Encrypts all properties if they have not already been encrypted.  The specified
     * file is overwritten with the encrypted values.  To check if the file has been
     * encrypted, we can look for string with prefix <code>ENC(hash)</code>.
     * @param propsFile the file to be encrypted.
     * @param propNames the collection of properties to be encrypted within the file.
     * @param secureDir the folder that contains the encryption key
     */
    public static void encryptFile(File propsFile, Collection<String> propNames, String secureDir) {
        if (propsFile == null) {
            throw new IllegalArgumentException(ERROR_PROPS_FILE);
        }
        if (propNames == null) {
            throw new IllegalArgumentException(ERROR_PROPS);
        }
        if (propNames.isEmpty()) {
            log.info("The properties to encrypt are empty; nothing to do.");
            return;
        }
        if (!propsFile.exists() || !propsFile.isFile()) {
            log.error("The properties file {}  does not exist." , propsFile.getAbsolutePath());
            return;
        }

        Properties props = new Properties();
        boolean isModified = false;
        File tempFile = null;
        try {
            tempFile = File.createTempFile(propsFile.getName(), ".temp", new File(propsFile.getParent()));
        }
        catch (IOException e) {
            log.error("Error creating temp file.", e);
            return;
        }

        // Writing to a temporary file because try/with resources can't read and write to same file.
        try (InputStream is = new FileInputStream(propsFile); OutputStream os = new FileOutputStream(tempFile)) {
            log.debug("Encrypting file at location: " + propsFile.getAbsolutePath());
            props.load(is);

            // loop through properties to encrypt
            for (String prop : propNames) {
                String propValue = props.getProperty(prop);
                if (!isEncrypted(propValue) && StringUtils.length(propValue) > 0) {
                    log.debug("Encrypting property: " + prop);
                    StringBuilder sb = new StringBuilder();
                    sb.append(PREFIX);
                    sb.append(PSEncryptor.encryptProperty(secureDir,propsFile.getAbsolutePath(),prop,propValue));
                    sb.append(SUFFIX);
                    props.setProperty(prop, sb.toString());
                    isModified = true;
                }
            }
            if (isModified) {
                log.debug("Properties were encrypted. Attempting to store and write the properties.");
                props.store(os, null);
                writeProperties(propsFile, tempFile);
            }
        } catch (IOException | PSEncryptionException e) {
            log.error("Error encrypting file: " + propsFile.getAbsolutePath(), e);
        } finally {
            try {
                java.nio.file.Files.delete(tempFile.toPath());
            } catch (IOException e) {
                log.error("Error deleting temp file: " + tempFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * This method unwraps and returns the decrypted value from within
     * the suffix and prefix of the original string.  The properties
     * file should be encrypted prior to reading the properties file
     * in which case the string should always be encrypted first.  In
     * the event that the properties file is read prior to encryption,
     * the string is assumed to be unencrypted and returned as is.
     * @param str the string to decode.
     * @param key the key used for original encryption.
     * @param secureDir the path to the secure folder
     * @param legacyEncryptor The encryptor to use to handle upgraded and changed encryption algorithms.
     * @return a String with the unencrypted property value.
     */
    public static String decryptProperty(String str, String key, String secureDir, PSAbstractEncryptor legacyEncryptor) {


        if (!StringUtils.startsWith(str, PREFIX)) {
            return str;
        }
        String strTemp = StringUtils.substringAfter(str, PREFIX);
        strTemp = StringUtils.substringBefore(strTemp, SUFFIX);

        return decrypt(strTemp, key,secureDir,legacyEncryptor);

    }

    /**
     * Attempts to decrypt a string using the latest PSEncryptor. If this fails will try with the legacy encryptor.
     * @param encrypted The encrypted string
     * @param key legacy encryption key, may be null
     * @param secureDir The location to where the secure key file is stored
     * @param legacyDecryptor A legacy decryptor to use in upgrade scenario
     * @return
     */
    private static String decrypt(String encrypted, String key,  String secureDir, PSAbstractEncryptor legacyDecryptor){
        String ret = "";
        try {
            ret = PSEncryptor.decryptString(secureDir,encrypted);
        }catch(PSEncryptionException | IllegalArgumentException e){
            try {
                ret = legacyDecryptor.decrypt(encrypted, key, legacyDecryptor);
            }catch(PSEncryptionException ex){
                log.error("Error decrypting password: {}", ex.getMessage());
            }
        }
        return  ret;

    }
    /**
     * Copies the properties in the temporary file to the main
     * properties file.
     *
     * @param propsFile the main properties file.
     * @param tempFile the temporary copy of the main properties file.
     */
    private static void writeProperties(File propsFile, File tempFile) {
        log.debug("Writing the properties file with encrypted properties: {}", propsFile.getAbsolutePath());
        try (FileInputStream is = new FileInputStream(tempFile); FileOutputStream os = new FileOutputStream(propsFile)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            log.error("Error writing to properties file: {}", propsFile.getAbsolutePath(), e);
        }
    }

    /**
     * Determines if the given property value is encrypted or not.
     *
     * @param str the property value to check
     * @return <code>true</code> if the property is encrypted.
     */
    private static boolean isEncrypted(String str) {
        return StringUtils.startsWith(str, PREFIX)
                && StringUtils.endsWith(str, SUFFIX);
    }
}
