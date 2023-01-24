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
package com.percussion.rxverify.data;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This class describes a single file. This is only a data class and is used as
 * a structure
 */
public class PSFileInfo implements Externalizable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct a new fileinfo object
    * 
    * @param file the file, must never be <code>null</code>
    * @param relpath the relative path, must never be <code>null</code> or
    *           empty
    * @throws NoSuchAlgorithmException if the message digest algorithm is
    *            missing
    * @throws IOException should never occur, but would indicate a problem
    *            opening or reading from the file
    * @throws DigestException should never occur, but would indicate a problem
    *            with the digester
    */
   public PSFileInfo(File file, String relpath)
         throws NoSuchAlgorithmException, DigestException, IOException {
      if (file == null)
      {
         throw new IllegalArgumentException("file must never be null");
      }
      if (relpath == null || relpath.trim().length() == 0)
      {
         throw new IllegalArgumentException("relpath may not be null or empty");
      }
      m_size = file.length();
      m_path = relpath;

      // Compute Digest
      if (file.canRead() && !excluded(relpath))
      {
         MessageDigest md = MessageDigest.getInstance("SHA-256");

         try(InputStream is = new FileInputStream(file))
         {
            byte[] buffer = new byte[8096];
            int count;
            while ((count = is.read(buffer)) >= 0)
            {
               md.update(buffer, 0, count);
            }
            m_digest = md.digest();
         }
      }
      else
      {
         m_digest = new byte[0];
      }
   }

   /**
    * Void ctor for object reader
    */
   public PSFileInfo() {
      // 
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuilder dstr = new StringBuilder(32);
      char hexDigits[] =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};
      for (byte b : m_digest) {
         byte h = (byte) ((b / 16) & 0xF);
         byte l = (byte) (b & 0xF);
         dstr.append(hexDigits[h]);
         dstr.append(hexDigits[l]);

      }
      return m_path + " size: " + m_size + " bytes, SHA-256: " + dstr;
   }

   /**
    * Return if the given path shouldn't be digested
    * 
    * @param path a path, assumed non- <code>null</code> or empty
    * @return <code>true</code> if the given path should not be digested
    */
   private boolean excluded(String path)
   {
      String checkpath = path.toLowerCase();
      return checkpath.endsWith(".exe") || checkpath.endsWith(".dll")
            || checkpath.endsWith(".bin") || checkpath.indexOf(".so") > -1;
   }

   /**
    * @return Returns the checksum.
    */
   public byte[] getDigest()
   {
      return m_digest;
   }

   /**
    * @return Returns the path.
    */
   public String getPath()
   {
      return m_path;
   }

   /**
    * @return Returns the size.
    */
   public long getSize()
   {
      return m_size;
   }

   /**
    * The relative path of the file
    */
   private String m_path;

   /**
    * The MD5 checksum for the file contents
    */
   private byte[] m_digest;

   /**
    * The size of the file
    */
   private long m_size;

   /*
    * (non-Javadoc)
    * 
    * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
    */
   public void readExternal(ObjectInput in) throws IOException,
         ClassNotFoundException
   {
      m_path = in.readUTF();
      m_digest = (byte[]) in.readObject();
      m_size = in.readLong();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(m_path);
      out.writeObject(m_digest);
      out.writeLong(m_size);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object b)
   {
      PSFileInfo binfo = (PSFileInfo) b;
      return m_path.equals(binfo.m_path)
            && Arrays.equals(m_digest, binfo.m_digest)
            && m_size == binfo.m_size;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return m_path.hashCode();
   }
}
