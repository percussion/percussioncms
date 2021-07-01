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
package com.percussion.rxverify.data;

import com.percussion.tablefactory.PSJdbcTableSchema;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

/**
 * @author dougrand
 * 
 * This class represents the information about an installation. It can be
 * serialized to an external stream using the methods from
 * {@link java.io.Externalizable}
 */
public class PSInstallation implements Externalizable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Contains files sorted by category. Each entry in the map has the category
    * as the key and the value is a {@link List}of {@link PSFileInfo}s that each
    * describe a file in the application tree.
    */
   private Map<String,List<PSFileInfo>> m_files = new HashMap<String,List<PSFileInfo>>();

   /**
    * A Set of {@link PSJdbcTableSchema} objects
    */
   private Set<PSTableInfo> m_tables = new HashSet<PSTableInfo>();

   /**
    * Contains extension names sorted by category. Each entry in the map has the
    * category as the key and the value is a {@link List}of {@link String}s
    * that each describe an extension.
    */
   private Map<String,List<String>> m_extensions = new HashMap<String,List<String>>();

   /**
    * Record the dtm when this data is created
    */
   private long m_dtm = System.currentTimeMillis();

   /**
    * Get the list of files associated with a given category.
    * 
    * @param category a category, must never be <code>null</code> or empty
    * @return a {@link List}of {@link PSFileInfo}associated with the category,
    *         may be <code>null</code> but never empty.
    */
   public List getFiles(String category)
   {
      if (category == null || category.trim().length() == 0)
      {
         throw new IllegalArgumentException("category may not be null or empty");
      }
      return (List) m_files.get(category);
   }

   /**
    * Get the list of exit categories.
    * 
    * @return the {@link Iterator}for the key values of extensions, this allows
    *         the caller to iterate over a list of category strings.
    */
   public Iterator getExtensionCategories()
   {
      return m_extensions.keySet().iterator();
   }

   /**
    * Get the file categories
    * 
    * @return an {@link Iterator}that walks over the categories. The categories
    *         are all {@link String}s.
    */
   public Iterator getFileCategories()
   {
      return m_files.keySet().iterator();
   }

   /**
    * Add a file to the list of files associated with a given category.
    * 
    * @param category a category, must never be <code>null</code> or empty
    * @param info a pathname, must never be <code>null</code>
    */
   public void addFile(String category, PSFileInfo info)
   {
      if (category == null || category.trim().length() == 0)
      {
         throw new IllegalArgumentException("category may not be null or empty");
      }
      if (info == null)
      {
         throw new IllegalArgumentException("info may not be null or empty");
      }
      List<PSFileInfo> files = (List<PSFileInfo>) m_files.get(category);
      if (files == null)
      {
         files = new ArrayList<PSFileInfo>();
         m_files.put(category, files);
      }
      files.add(info);
   }

   /**
    * Get the set of tables
    * 
    * @return a set of tables, never <code>null</code> but could be empty. The
    *         returned set is unmodifiable.
    */
   public Set getTables()
   {
      return Collections.unmodifiableSet(m_tables);
   }

   /**
    * Add a table to the set of known tables
    * 
    * @param tablename the table information, must never be <code>null</code>
    */
   public void addTable(PSTableInfo table)
   {
      if (table == null)
      {
         throw new IllegalArgumentException(
               "table may not be null");
      }
      m_tables.add(table);
   }

   /**
    * Gets the dtm when this information was created originally
    * 
    * @return a {@link Date}representing the date time created of this
    *         information, never <code>null</code>
    */
   public Date getDTM()
   {
      return new Date(m_dtm);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
    */
   @SuppressWarnings("unchecked")
   public void readExternal(ObjectInput in) throws IOException,
         ClassNotFoundException
   {
      // Read dtm
      m_dtm = in.readLong();
      // Read table set
      int count = in.readInt();
      for (int i = 0; i < count; i++)
      {
         PSTableInfo table = (PSTableInfo) in.readObject();
         m_tables.add(table);
      }
      // Read file map
      m_files = (Map) in.readObject();
      // Read the extension table
      m_extensions = (Map) in.readObject();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      // Save write dtm
      out.writeLong(m_dtm);
      // Save table set
      out.writeInt(m_tables.size());
      for (Iterator iter = m_tables.iterator(); iter.hasNext();)
      {
         PSTableInfo table = (PSTableInfo) iter.next();
         out.writeObject(table);
      }
      // Save file table
      out.writeObject(m_files);
      // Save the extensions table
      out.writeObject(m_extensions);
   }

   /**
    * Output the contents of the installation into the log
    * 
    * @param logger a {@link Logger}, must never be <code>null</code>
    */
   public void list(Logger logger)
   {
      if (logger == null)
      {
         throw new IllegalArgumentException("logger must never be null");
      }
      logger.info("Created: " + getDTM());
      logger.info("Categoried files:");
      Iterator iter = getFileCategories();
      while (iter.hasNext())
      {
         String category = (String) iter.next();
         List files = getFiles(category);
         logger.info("   " + category);
         Iterator fiter = files.iterator();
         while (fiter.hasNext())
         {
            PSFileInfo file = (PSFileInfo) fiter.next();
            logger.info("      " + file.toString());
         }
      }
      logger.info("Database tables:");
      iter = getTables().iterator();
      while (iter.hasNext())
      {
         PSTableInfo table = (PSTableInfo) iter.next();
         logger.info("   " + table);
      }
      logger.info("Extensions:");
      iter = getExtensionCategories();
      while (iter.hasNext())
      {
         String category = (String) iter.next();
         List exits = getExtensions(category);
         logger.info("   " + category);
         Iterator eiter = exits.iterator();
         while (eiter.hasNext())
         {
            String relpath = (String) eiter.next();
            logger.info("      " + relpath);
         }
      }
   }

   /**
    * Return the extensions for the given category.
    * 
    * @param category the category, assumed not <code>null</code> or empty
    * @return the list of extensions, expressed as strings. May be empty.
    */
   public List getExtensions(String category)
   {
      if (category == null || category.trim().length() == 0)
      {
         throw new IllegalArgumentException("category may not be null or empty");
      }
      return (List) m_extensions.get(category);
   }

   /**
    * Add the given extension to the known extensions for the given category.
    * 
    * @param category The category, must never be <code>null</code> or empty
    * @param exit The exit name, must never be <code>null</code> or empty
    */
   public void addExtension(String category, String exit)
   {
      if (category == null || category.trim().length() == 0)
      {
         throw new IllegalArgumentException("category may not be null or empty");
      }
      if (exit == null || exit.trim().length() == 0)
      {
         throw new IllegalArgumentException("exit may not be null or empty");
      }
      List<String> exits = (List<String>) m_extensions.get(category);
      if (exits == null)
      {
         exits = new ArrayList<String>();
         m_extensions.put(category, exits);
      }
      exits.add(exit);
   }

}
