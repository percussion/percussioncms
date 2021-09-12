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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Main encryption class to be used for Encryption within the code base.
 *
 * When initialized will generate an instance unique encryption key in
 * <InstallDir>/rxconfig/secure/.key if one is not present.
 *
 *
 */
public class PSEncryptor extends PSAbstractEncryptor {

    private String secureDir;
    private static final Logger log = LogManager.getLogger(PSEncryptor.class);
    public static final String SECURE_DIR = File.separator + "rxconfig"+File.separator+ "secure" +File.separator;
    private static final String SECURE_KEY_FILE = ".key";
    private static final String OLD_SECURE_KEY_FILE = ".keyold";
    public static final String ROTATE_FLAG_FILE = "rotate";
    private IPSKey secretKey;
    private IPSKey oldSecretKey;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public static final String SECRETKEY_PROPNAME="secretKey";

    private static Map<String, List<String>> propertyFilesWithPassword = new HashMap();
    private static Map<String, List<String>> xmlFilesWithPassword = new HashMap();


    private static File getRxDir(String path){
        return new File(".");
    }
    /**
     * Initializes a new instance with the specified algorithm and keyLocation
     *
     * @param algorithm
     * @param keyLocation
     */
    public PSEncryptor(String algorithm, String keyLocation) {
        init(algorithm,keyLocation);
    }


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
        //If No change in key then no need to do anything.
        if(secretKey != null){
            byte[] oldKey = secretKey.getSecret();
           if(Arrays.equals(newKey,oldKey)) {
               return;
           }
        }
        File installDir = getRxDir(null);
        generateNewKeyFile(this.secretKey,
                Paths.get(this.secureDir.concat(SECURE_KEY_FILE)),
                newKey,
                notifyListeners);

    }

    private byte[] generateNewKeyFile(IPSKey key,Path secureKeyFile, byte[] secureKey,boolean notifyListeners){

        try {
            log.info("Generating New Key");
            File f = secureKeyFile.toFile();
            oldSecretKey = secretKey;
            //Backup Existing key File if Exists
            if(f.exists()) {
                log.info("Backing up old Key");
                File copied = new File(f.getPath().concat("old"));
                //Delete existing .oldkey Encryption Key file
                if(Files.exists(copied.toPath())){
                    Files.delete(copied.toPath());
                }
                //Create new .oldKey Encryption key file
                FileUtils.copyFile(f, copied);
            }
            f.getParentFile().mkdirs();
            try(FileOutputStream writer = new FileOutputStream(f)) {
                writer.write(secureKey);
            }
            key.setSecret(secureKey);

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
    private IPSKey loadKey(String algorithm, String keyLocation){

        if(algorithm == null)
            algorithm = PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM;

        if(keyLocation == null) {
            keyLocation = getRxDir(null).getAbsolutePath() + SECURE_DIR;
        }

        IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(algorithm);
        File installDir = getRxDir(null);
        Path secureKeyFile = Paths.get(keyLocation + SECURE_KEY_FILE);
        byte[] secureKey=null;
        Path rotateFlag = Paths.get(keyLocation + ROTATE_FLAG_FILE);

        if(Files.exists(secureKeyFile) && ! Files.exists(rotateFlag)){
            //load key
            try {
                secureKey = Files.readAllBytes(secureKeyFile);
            } catch (IOException e) {
                log.error("Error reading instance secure key file ({}): {}",secureKeyFile.toAbsolutePath().toString(), e.getMessage());
                log.debug(e);
            }
            Path oldSecureKeyFile = Paths.get(keyLocation + OLD_SECURE_KEY_FILE);
            if(Files.exists(oldSecureKeyFile)){
                byte[] oldSecureKeyByte=null;
                try {
                    oldSecureKeyByte = Files.readAllBytes(oldSecureKeyFile);
                    if(oldSecretKey == null) {
                        oldSecretKey = PSEncryptionKeyFactory.getKeyGenerator(algorithm);
                    }
                    oldSecretKey.setSecret(oldSecureKeyByte);
                } catch (IOException e) {
                    log.error("Error reading instance Oldsecure key file ({}): {}",oldSecureKeyFile.toAbsolutePath().toString(), e.getMessage());
                    log.debug(e);
                }

            }
        }else{
            //Either there was no key or the rotate flag was set
            secureKey = generateNewKeyFile(key,secureKeyFile,key.generateKey().getEncoded(),true);
            try {
                Files.deleteIfExists(rotateFlag);
            } catch (IOException e) {
                log.warn("Unable to remove the encryption key rotation flag file: {} .  They key will be rotated on restart unless this file is removed. Error was: {} ",rotateFlag.toAbsolutePath(), e.getMessage());
            }
        }
        key.setSecret(secureKey);
        this.secretKey = key;
        notifyKeyRotation();
        return key;
    }

    public static boolean checkSecureKeyPresent(String secureDir){
        if(secureDir == null) {
            String rxPath = getRxDir(null).getAbsolutePath();
            if (rxPath.endsWith("\\.")) {
                rxPath = rxPath.substring(0, rxPath.length() - 2);
            }
            secureDir = rxPath;
        }
        String keyLocation = secureDir +PSEncryptor.SECURE_KEY_FILE;
        File secureFile = new File(keyLocation);
        return secureFile.exists();
    }

    private void notifyKeyRotation(){
        //Notify any listeners that the key has changed.

        pcs.firePropertyChange(
                SECRETKEY_PROPNAME,
                oldSecretKey,  //old value
                secretKey //new value
        );

        rotatePropertyFileKeys();
        rotateXMLFileKeys();
    }

    /**
     * If using this API, then Pwd will not be reencrypted on rotation of key
     * Either add listener to rotation or use encryptProperty(...) API
     * @param secureDir
     * @param value
     * @return
     * @throws PSEncryptionException
     */
    public static String encryptString(String secureDir, String value) throws PSEncryptionException {
        PSEncryptor encryptor = PSEncryptor.getInstance("AES",secureDir);
        String decryptStr = encryptor.encrypt(value);
        return decryptStr;
    }

    public static String decryptString(String secureDir,String value) throws PSEncryptionException {
        PSEncryptor encryptor = PSEncryptor.getInstance("AES", secureDir);
        try {
            return encryptor.decrypt(value);
        }catch (PSEncryptionException ee){
            return encryptor.decryptWithOld(value);
        }
    }

    public static String encryptProperty( String secureDir, String propertyFilePath, String keyName, String value) throws PSEncryptionException {

        PSEncryptor encryptor = PSEncryptor.getInstance("AES",secureDir);
        String encryptStr = encryptor.encrypt(value);
        storeInVault(propertyFilePath,keyName,encryptStr);

        return encryptStr;
    }

    public static String decryptWithOldKey(String secureDir,String value) throws PSEncryptionException {
        PSEncryptor encryptor = PSEncryptor.getInstance("AES",secureDir);
        return encryptor.decryptWithOld(value);
    }

    public static String decryptProperty(String secureDir,String propertyFilePath, String keyName, String value) throws PSEncryptionException {
        PSEncryptor encryptor = PSEncryptor.getInstance("AES",secureDir);
        String decryptStr = encryptor.decrypt(value);
        storeInVault(propertyFilePath,keyName,value);
        return decryptStr;
    }

    private static void storeInVault(String propertyFilePath, String keyName, String value){
        List props = (List) propertyFilesWithPassword.get(propertyFilePath);
        if(props == null){
            props = new ArrayList();
        }
        //Keep encrypted String in Map such that it can be replaced at the time of rotation
        if(keyName == null){
            props = (List) xmlFilesWithPassword.get(propertyFilePath);
            if(props == null){
                props = new ArrayList();
            }
            //keeping EncryptedString
            props.add(value);
            log.debug("Adding Pwd to Vault: {} : {}", propertyFilePath,keyName);
            xmlFilesWithPassword.put(propertyFilePath,props);
        }else {
            props.add(keyName);
            log.debug("Adding Pwd to Vault: {} : {}", propertyFilePath,keyName);
            propertyFilesWithPassword.put(propertyFilePath,props);
        }

    }

    /**
     * Use this method when creating PasswordVault
     */
    private static void rotatePropertyFileKeys(){
        for(Object propertyFile : propertyFilesWithPassword.keySet() ){
            List encPwds = (List) propertyFilesWithPassword.get(propertyFile);
            File propFile = new File(propertyFile.toString());
            try (FileInputStream in = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(in);
                for (Object propKey:encPwds) {
                    String pwd = props.getProperty((String) propKey);
                    if(pwd != null){
                        String decryptPwd = instance.decrypt(pwd);
                        String encPwd = instance.encrypt(decryptPwd);
                        props.setProperty((String) propKey,encPwd);
                    }
                }
                props.store(new FileOutputStream(propFile), null);
            } catch (IOException | PSEncryptionException e) {
                log.error("Rotation of decrypted property Values Failed. ERROR: {} ", e.getMessage());
            }

        }
    }

    private static void rotateXMLFileKeys() {
        for (Object propertyFile : xmlFilesWithPassword.keySet()) {
            try {
                List<String> encPwds = (List) xmlFilesWithPassword.get(propertyFile);

                File absFile = new File(propertyFile.toString());

                try (FileReader fr = new FileReader(absFile)) {   //reads the file
                    BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
                    StringBuffer sb = new StringBuffer();    //constructs a string buffer with no characters
                    String line;
                    while ((line = br.readLine()) != null) {
                        for (String oldPw : encPwds) {
                            String dePwd = instance.decrypt((String) oldPw);
                            String newPwd = instance.encrypt(dePwd);
                            line.replaceAll(oldPw,newPwd);
                        }
                        sb.append(line);      //appends line to string buffer
                        sb.append("\n");     //line feed
                    }
                    fr.close();    //closes the stream and release the resources
                    log.info("Passwords rotated in File : {}" + absFile.getAbsolutePath() );
                } catch (IOException e) {
                   log.error("Rotation Of Password Failed in XML File Updates");
                }

            } catch (PSEncryptionException pe) {
                log.error("Reencryption Failed on Rotation Of Password in XML File Updates");
            }

        }
    }

    private static PSEncryptor instance;

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
        this.secretKey = loadKey(null,null);

    }

    private void init(String algorithm, String keystoreLocation){
        this.secretKey = loadKey(algorithm,keystoreLocation);

        //Make sure it has a trailing /
        keystoreLocation = keystoreLocation.trim();
        if(!keystoreLocation.endsWith(File.separator))
            keystoreLocation.concat(File.separator);

        this.secureDir = keystoreLocation;
    }


    /**
     * Gets an instance of the Encryptor for decrypting and encrypting
     * data.  A new key will be generated in the specified location if
     * an existing key is not found.
     *
     * @param algorithm  Currently on AES is supported
     * @param keyLocation The location to store the encryption key. Example: InstallDir/rxconfig/secure or user.home.dir/.perc-secure
     * @return A valid Encryptor instance
     */
    public static PSEncryptor getInstance(String algorithm, String keyLocation){
        if(algorithm==null || keyLocation==null)
            throw new IllegalArgumentException("Algorithm and KeyLocation are required.");

        synchronized (PSEncryptor.class) {
            if (instance == null) {
                instance = new PSEncryptor(algorithm,keyLocation);
            }

            return instance;
        }
    }

    public static void rotateKey(String algorithm, String keyLocation){
        Path rotateFlag = Paths.get(keyLocation + ROTATE_FLAG_FILE);
        try {
            Files.createFile(rotateFlag);
            synchronized (PSEncryptor.class) {
                if(instance == null) {
                    instance = new PSEncryptor(algorithm, keyLocation);
                }else{
                    instance.loadKey(algorithm,keyLocation);
                }
            }
        } catch (IOException e) {
            log.warn("Unable to rotate Key ERROR: {} .",e.getMessage());
            log.debug("Unable to rotate Key ERROR: {} .",e.getMessage(),e);
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
    public String encrypt(String str, String password) throws PSEncryptionException {
        //TODO: implement me
        throw new PSEncryptionException();
    }

    /**
     * The string to encrypt, returns a Base64 encoded version of the String.  NOTE: Base64 may not
     * be safe encoding for all data formats.
     *
     * Use the hex method to hex encode for formats like JSON.
     *
     * @param str The string to encrypt
     * @return base64 encoded encrypted string.
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
     * @param legacyDecryptor The legacy decryptor to use for decryption if decryption fails. Used to handle encryption upgrades.
     * @return The decrypted string, never <code>null</code>, may be empty.
     */
    @Override
    @Deprecated
    public String decrypt(String str, String key, PSAbstractEncryptor legacyDecryptor) throws PSEncryptionException  {
        //TODO: Implement me
        throw new PSEncryptionException();
    }

    /**
     * Decrypts the specified bytearray.
     * @param encrypted base64 encoded encrypted string
     * @return decrypted string or null if the string fails to decrypt
     */
    public String decrypt(String encrypted) throws PSEncryptionException{

            if (encrypted == null)
                throw new IllegalArgumentException();

            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
            return secretKey.getDecryptor().decrypt(encryptedBytes);
    }

    public String decryptWithOld(String encrypted) throws PSEncryptionException{

        if(encrypted== null)
            throw new IllegalArgumentException();
        if(oldSecretKey == null){
            throw new PSEncryptionException();
        }

        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
        return oldSecretKey.getDecryptor().decrypt(encryptedBytes);
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
            try(InputStream is = this.getClass().getResourceAsStream(
                    "/com/percussion/security/encryption/.legacy-key")) {
                ret = IOUtils.toByteArray(is);
            }
        } catch (IOException | FileSystemNotFoundException e) {
           log.debug("Unable to load .legacy-key from classpath.",e);
        }

        try {
            if (ret == null) {
                ret = Files.readAllBytes(Paths.get(getRxDir(null) + SECURE_DIR + ".legacy-key"));
            }
        } catch (IOException e) {
            throw new PSEncryptionException("Unable to load .legacy-key from rxconfig/secure.",e);
        }
        return ret;
    }


}
