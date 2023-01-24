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
package com.percussion.delivery.utils.security;

import com.percussion.security.PSEncryptor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author erikserating
 *
 */
@SuppressFBWarnings({"HARD_CODE_PASSWORD", "HARD_CODE_PASSWORD", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
public class PSSecurePropertyTest
{

   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   private static final String key1 = "ENC(srTe237dn+xXEMYOZhZEQM/1jRTskeQf)";
   private static final String key2 = "ENC(9VQE/lfcQK+lsruyKnt3V+QBtxVEyzgn)";
   
   private static final String strongKey1 = "ENC2(yMuC70r1jYE6V+wBKf7ioDZz+UVLjy1GOMxNQQFMTPA=)";
   private static final String strongKey2 = "ENC2(QrzEYF/ymtHPXJRqZx8wH3mIHtDkEGbE9mOcyY/4qQ4=)";
   private static final String oldKey = "ENC(srTe237dn+xXEMYOZhZEQM/1jRTskeQf)";
   
   private static final String pass = "testPassword";
   private static final String pass2 = "anotherTestPassword";
   private static final String salt = "testSaltKey";
   
   private static final String PROP_PASS = "password";
   
   private static final String PROP_PASS2 = "anotherPassword";
   
   private static final String DEFAULT_ENCRYPTION = "ENC";
   private static final String STRONG_ENCRYPTION = "ENC2";
   
   private static final String PROP_TEST1 = "test1";
   private static final String PROP_TEST1_VAL = "foo";
   private static final String PROP_TEST2 = "test2";
   private static final String PROP_TEST2_VAL = "bar";
   
   private static final String ORIG_PROPFILE1 = "propfile1.properties";
   private static final String PROPFILE1 = "copy_propfile1.properties";
   
   private static final Properties  props1 = new Properties();
   static
   {
      props1.put(PROP_PASS, pass);
      props1.put(PROP_PASS2, pass2);
      props1.put(PROP_TEST1, PROP_TEST1_VAL);
      props1.put(PROP_TEST2, PROP_TEST2_VAL);
      props1.put("first.regex.match", "blahblah1");
      props1.put("second.regex.match", "blahblah2");
      props1.put("third.regex.match", "blahblah3");
      props1.put("first.regex.mismatch", "blahblah4");
   }
      
   @Test
   public void testGetClouded() throws Exception
   {
      String encStr = PSEncryptor.encryptString(tempFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),pass);
      String enc1 = PSSecureProperty.getClouded(encStr);
      String enc2 = PSSecureProperty.getClouded(encStr);
      String dec1 = PSSecureProperty.getValue(enc1, null);
      String dec2 = PSSecureProperty.getValue(enc2, salt);
      assertEquals(pass, dec1);
      assertEquals(pass, dec2);
   }
   
   @Test
   public void testGetValue() throws Exception
   {
      
      String dec1 = PSSecureProperty.getValue(key1, null);
      String dec2 = PSSecureProperty.getValue(key2, salt);
      assertEquals(pass, dec1);
      assertEquals(pass, dec2);
   }
   
   @Test
   public void testIsEncodedValue() throws Exception
   {
      
      boolean defaultEncoding = PSSecureProperty.isValueClouded(key1);
      boolean strongEncoding = PSSecureProperty.isValueClouded(strongKey1);
      boolean notEncodedValue = PSSecureProperty.isValueClouded("srTe237dn+xXEMYOZhZEQM/1jRTskeQf");
      assertTrue(defaultEncoding);
      assertTrue(strongEncoding);
      assertFalse(notEncodedValue);
   }

   
   @Test
   public void testsecureProperties() throws Exception
   {
      Properties results = null;
      writeProps(ORIG_PROPFILE1, props1);
      copyProps(ORIG_PROPFILE1, PROPFILE1);
      writeProps(PROPFILE1, props1);

      File propfile1 = new File(tempFolder.getRoot().getAbsolutePath() + File.separator+  PROPFILE1);

      long lastmod = propfile1.lastModified();
      
      Collection<String> names = new ArrayList<String>();
      names.add(PROP_PASS);
      PSSecureProperty.secureProperties(propfile1, names, null, DEFAULT_ENCRYPTION);
      results = loadProps(PROPFILE1);
      //assertTrue(lastmod < propfile1.lastModified());      
      assertFalse(pass.equals(results.get(PROP_PASS)));
      assertEquals(pass2, results.get(PROP_PASS2));
      
      //Try again should see no modification time change
      lastmod = propfile1.lastModified();
      PSSecureProperty.secureProperties(propfile1, names, null, DEFAULT_ENCRYPTION);
      //assertEquals(lastmod, propfile1.lastModified());
      
      names.clear();
      names.add(".*match$");
      PSSecureProperty.secureProperties(propfile1, names, null, DEFAULT_ENCRYPTION);
      deleteFile(PROPFILE1);
      
   }
   
   private void writeProps(String filename, Properties props) throws Exception
   {
      File file = new File(tempFolder.getRoot().getAbsolutePath() + File.separator+  filename);

      try (OutputStream os = new FileOutputStream(file)) {
         props.store(os, "");
      }

   }
   
   private Properties loadProps(String filename) throws Exception {
      File file = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + filename);

      try (InputStream is = new FileInputStream(file)) {
         Properties props = new Properties();
         props.load(is);
         return props;
      }

   }
   
   private void copyProps(String source, String target) throws Exception
   {
      Properties props = loadProps(source);
      writeProps(target, props);
   }
   
   private void deleteFile(String filename) throws Exception
   {
      File file = new File(tempFolder.getRoot().getAbsolutePath() + File.separator+  filename);
      if(file.exists())
         file.delete();
   }
}
