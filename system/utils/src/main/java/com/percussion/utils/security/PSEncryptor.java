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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.security;

import com.percussion.utils.io.PathUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Observable;

/**
 * Main encryption class to be used for Encryption within the code base.
 *
 * When initialized will generate an instance unique encryption key in
 * <InstallDir>/rxconfig/secure/.key if one is not present.
 *
 *
 */
public class PSEncryptor extends PSAbstractEncryptor {

    private static final Logger log = LogManager.getLogger(PSEncryptor.class);
    private static final String SECURE_DIR = "/rxconfig/secure/";
    private static final String SECURE_KEY_FILE = ".key";
    private static final String ROTATE_FLAG_FILE = "rotate";
    private IPSKey secretKey;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public static final String SECRETKEY_PROPNAME="secretKey";


    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    /**
     * Replaces the current key file with the specified bytes.
     * @param newKey a byte array containing the replacement key
     * @param notifyListeners when true listeners will be notified, when false they will not.
     */
    public void forceReplaceKeyFile(byte[] newKey, boolean notifyListeners){

        File installDir = PathUtils.getRxDir();

        generateNewKeyFile(this.secretKey,
                Paths.get(installDir.getAbsolutePath() + SECURE_DIR + SECURE_KEY_FILE),
                newKey,
                notifyListeners);

    }

    private byte[] generateNewKeyFile(IPSKey key,Path secureKeyFile, byte[] secureKey,boolean notifyListeners){

        try {
            //Notify any listeners that the key has changed.
            pcs.firePropertyChange(
                    SECRETKEY_PROPNAME,
                    this.secretKey,  //old value
                    secureKey //new value
                    );

            File f = secureKeyFile.toFile();
            f.getParentFile().mkdirs();
            FileOutputStream writer = new FileOutputStream(f);
            writer.write(secureKey);
            writer.close();
        } catch (IOException e) {
            log.error("Error writing instance secure key file: (" + secureKeyFile.toAbsolutePath().toString() + ")" + ") :" + e.getMessage());
            log.debug(e);
        }
        return secureKey;
    }
    /**
     * Loads the persisted encryption key or generates a new one of it does not exist.
     *
     * @return Returns an IPSKey instance with the secureKey assigned.  May return null if Key loading or generation fails.
     */
    private IPSKey loadKey(){
        IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM);
        File installDir = PathUtils.getRxDir();
        Path secureKeyFile = Paths.get(installDir.getAbsolutePath() + SECURE_DIR + SECURE_KEY_FILE);
        byte[] secureKey=null;
        Path rotateFlag = Paths.get(installDir.getAbsolutePath() + SECURE_DIR + ROTATE_FLAG_FILE);

        if(Files.exists(secureKeyFile) && ! Files.exists(rotateFlag)){
            //load key
            try {
                secureKey = Files.readAllBytes(secureKeyFile);
            } catch (IOException e) {
                log.error("Error reading instance secure key file (" + secureKeyFile.toAbsolutePath().toString() + "): " + e.getMessage());
                log.debug(e);
            }
        }else{
            //Either there was no key or the rotate flag was set
            secureKey = generateNewKeyFile(key,secureKeyFile,key.generateKey().getEncoded(),true);
            try {
                Files.deleteIfExists(rotateFlag);
            } catch (IOException e) {
               log.warn("Unable to remove the encryption key rotation flag file: " + rotateFlag.toAbsolutePath() + ".  They key will be rotated on restart unless this file is removed. Error was: " + e.getMessage());
            }
        }

        key.setSecret(secureKey);
        return key;
    }


    private static volatile PSEncryptor instance;

    /**
     * Private CTOR
     */
    private PSEncryptor(){
        init();
    }

    /**
     * Initialize the encryptor
     */
    private void init() {

        //Check if there is an existing key - if not create it
        this.secretKey = loadKey();

    }


    public static PSEncryptor getInstance(){
        synchronized (PSEncryptor.class) {
            if (instance == null) {
                instance = new PSEncryptor();
            }
            return instance;
        }
    }


    /**
     * Encrypts the provided string using the supplied secret key.  Deprecated, see encrypt(String str)
     *
     * @param str The string to encrypt, may not be <code>null</code>, may be
     *            empty.
     * @param password The secret key to encrypt the string, may not be
     *            <code>null</code> or empty.
     * @return The encrypted string, never <code>null</code>, may be empty.
     */
    @Override
    public String encrypt(String str, String password) throws PSEncryptionException{
        //TODO: implement me
        throw new PSEncryptionException();
    }

    /**
     * The string to encrypt.
     * @param str
     * @return
     */
    public String encrypt(String str) throws PSEncryptionException {

             return Base64.getEncoder().encodeToString(secretKey.getEncryptor().encrypt(str));

    }

    /**
     * Decrypts the provided string using the supplied secret key.
     *
     * @param str The string to decrypt, may not be <code>null</code>, may be
     *            empty.
     * @param key The secret key that was used to encrypt the string, may not
     *            be <code>null</code> or empty.
     * @return The decrypted string, never <code>null</code>, may be empty.
     */
    @Override
    @Deprecated
    public String decrypt(String str, String key) throws PSEncryptionException  {
        //TODO: Implement me
        throw new PSEncryptionException();
    }

    /**
     * Decrypts the specified bytearray.
     * @param encrypted base64 encoded encrypted string
     * @return decrypted string or null if the string fails to decrypt
     */
    public String decrypt(String encrypted) throws PSEncryptionException{

        if(encrypted== null)
            throw new IllegalArgumentException();

        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
        return secretKey.getDecryptor().decrypt(encryptedBytes);
    }

    /**
     * Converts a <code>BigInteger</code> to a byte array whose size is a
     * multiple of {@link #BYTE_ARRAY_MULTIPLE}.  For positive values or a value
     * of zero, the byte array is padded with leading 0 byte values if necessary.
     * For negative values, the byte array is padded with leading -1 byte values
     * if necessary.
     *
     * @param bigInt The <code>BigInteger</code> to convert, may not be
     *               <code>null</code>.
     * @return A valid byte array form of a <code>BigInteger</code> with a size
     * which is a multiple of {@link #BYTE_ARRAY_MULTIPLE}.
     */
    @Override
    public byte[] toByteArray(BigInteger bigInt) throws PSEncryptionException {
        return new byte[0];
    }

    /**
     * Given a UID and password, returns an encrypted password
     * @param uid The user id
     * @param pw
     * @return
     * @throws PSEncryptionException,IllegalArgumentException
     */
    @Override
    public String encryptCredentials(String uid, String pw) throws PSEncryptionException,IllegalArgumentException{
        if(uid == null)
            throw new IllegalArgumentException("uid is required");

        if(pw == null)
            throw new IllegalArgumentException("pw is required");

        return new String(Base64.getEncoder().encode(
                secretKey.getEncryptor().encryptWithPassword(uid + ":" + pw,pw)),StandardCharsets.UTF_8);

    }

    /**
     * Decrypts the specified credentials using the supplied password.
     *
     * @param encrypted The encrypted credentials
     * @param pw        The password to use for decryption.
     * @return Returns a decrypted string in the form of uid:password
     * @throws PSEncryptionException,IllegalArgumentException
     */
    @Override
    public String decryptCredentials(String encrypted, String pw) throws PSEncryptionException,IllegalArgumentException {
        if(encrypted == null)
            throw new IllegalArgumentException("encrypted is required");

        if(pw == null)
            throw new IllegalArgumentException("pw is required");

        return secretKey.getDecryptor().decryptWithPassword(encrypted,pw);
    }

    /**
     * Utility method for getting the UID portion of a credential string in the uid:password format
     * @param credentials a non null plain text string in uid:password format
     * @return the uid
     */
    public static String getUID(String credentials) throws IllegalArgumentException{
        if(credentials == null || !credentials.contains(":"))
            throw new IllegalArgumentException("Invalid credential string");

        return credentials.substring(0,credentials.indexOf(":"));
    }

    /**
     * Utility method for getting the password portion of a credential string in the uid:password format
     * @param credentials a non null plain text string in uid:password format
     * @return the password
     */
    public static String getPassword(String credentials) throws IllegalArgumentException{
        if(credentials == null || !credentials.contains(":"))
            throw new IllegalArgumentException("Invalid credential string");

        return credentials.substring(credentials.indexOf(":")+1);
    }

    /**
     * Decrypts legacy encryption keys.
     * @param encrypted
     * @return
     * @throws PSEncryptionException
     */
    public String decryptLegacyKey(String encrypted) throws PSEncryptionException{

            //Read the secure key
            byte[] data = getLegacyKeyFile();
            IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM);
            key.setSecret(Base64.getDecoder().decode(data));

            return key.getDecryptor().decrypt(Base64.getDecoder().decode(encrypted));

    }

    private byte[] getLegacyKeyFile() throws PSEncryptionException {
        byte[] ret = null;

        //Try to get the key from resource - fail over to file system if not found
        try {
            ret = IOUtils.toByteArray(this.getClass().getResourceAsStream(
                    "/com/percussion/security/encryption/.legacy-key"));
        } catch (IOException | FileSystemNotFoundException e) {
           log.debug("Unable to load .legacy-key from classpath.",e);
        }

        try {
            if (ret == null) {
                ret = Files.readAllBytes(Paths.get(PathUtils.getRxDir() + SECURE_DIR + ".legacy-key"));
            }
        } catch (IOException e) {
            throw new PSEncryptionException("Unable to load .legacy-key from rxconfig/secure.",e);
        }
        return ret;
    }


}
