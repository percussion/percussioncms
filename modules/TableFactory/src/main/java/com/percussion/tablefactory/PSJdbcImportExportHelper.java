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

package com.percussion.tablefactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSJdbcImportExportHelper
{
   public static String OPTION_DB_EXPORT = "-dbexport";
   public static String OPTION_DB_IMPPORT = "-dbimport";
   public static String OPTION_DB_PROPS = "-dbprops";
   public static String OPTION_STORAGE_PATH = "-storagepath";
   public static String OPTION_TABLES_TO_SKIP = "-tablestoskip";
   public static String DB_OPTION = "dboption";
   public static String DEF_DATA_FOLDER = "defData";
   public static String BINARY_DATA_FOLDER = "binaryData";
   public static String BINARY_DATA_BUCKET = "bucket";
   public static String BINARY_DATA_INITIAL_BUCKET = BINARY_DATA_BUCKET + "_0";
   public static Set<String> requiredParams = new HashSet<>(Arrays.asList(OPTION_DB_PROPS,OPTION_STORAGE_PATH));
   public static int MAX_FILES_IN_FILDER = 500;
   public static List<String> tablesToSkip = new ArrayList<>();
   
   /* 
    * Import on MySql is choking on import if the limitSizeForIndex value is set to true for DPL_ID_MAPPING mapping.
    * Added this map to check the table while cataloging and update the value.
    * The key is the table name, the value is colon (:) separated list of column names.
    * For example limitSizeForIndexMap.put("DPL_ID_MAPPING", "REPOSITORY_ID");
    * If there are multiple columns that needs to be set with true value for limit size.
    * limitSizeForIndexMap.put("DPL_ID_MAPPING", "REPOSITORY_ID:ID_MAP");
    */
   public static Map<String, String> limitSizeForIndexMap = new HashMap<>();
   static {
      limitSizeForIndexMap.put("DPL_ID_MAPPING", "REPOSITORY_ID");
   }
   public static Map<String, String> getOptions (String[] options) {
      if (options == null || options.length < 1) {
         throw new IllegalArgumentException("options must not be null or empty");
      }
      String optionType = StringUtils.defaultString(options[0]);
      if (!(OPTION_DB_EXPORT.equals(optionType) || OPTION_DB_IMPPORT.equals(optionType))) {
         throw new IllegalArgumentException("first option must be either " + OPTION_DB_EXPORT + " or " + OPTION_DB_IMPPORT);
      }
      Map<String, String> optionsMap = new HashMap<>();
      optionsMap.put(DB_OPTION, optionType);
      for (int i=1; i<options.length; i = i+2) {
         String key = options[i];
         String value = options[i+1];
         optionsMap.put(key, value);
      }
      if (!CollectionUtils.isSubCollection(requiredParams, optionsMap.keySet())) {
         out("Required parameters are missing");
         usage(optionType);
      }
      //validate props file
      File propsFile = new File(optionsMap.get(OPTION_DB_PROPS));
      if (!propsFile.exists()) {
         out(OPTION_DB_PROPS + " option value must be a valid file.");
         usage(optionType);
      }
      //validate storage path
      File storageFolder = new File(optionsMap.get(OPTION_STORAGE_PATH));
      if(!storageFolder.exists() || !storageFolder.isDirectory()) {
         out(OPTION_STORAGE_PATH + " option value must be a valid directory.");
         usage(optionType);
      }
      //Assign skip tables
      String tablesToSkipOption = optionsMap.get(OPTION_TABLES_TO_SKIP);
      if(StringUtils.isNotBlank(tablesToSkipOption)){
         tablesToSkip = Arrays.asList(tablesToSkipOption.split(","));
         for(int i =0;i<tablesToSkip.size()-1;i++){
            if(tablesToSkip.get(i)!= null)
            {
               tablesToSkip.set(i, tablesToSkip.get(i).trim());
            }
         }
      }
      return optionsMap;
   }
   
   /**
    * Recursively find a file by name.
    * 
    * @param file
    * @param search
    * @return
    */
   public static File findFile(File file, String search) {
      if (file.isDirectory()) {
          File[] arr = file.listFiles();
          for (File f : arr) {
              File found = findFile(f, search);
              if (found != null)
                  return found;
          }
      } else {
          if (file.getName().equals(search)) {
              return file;
          }
      }
      return null;
  }
   
   
   public static File getNextBucket(File currBucket) {
      String[] bucketParts = currBucket.getName().split("_");
      int nextBucketNumber = Integer.parseInt(bucketParts[1]) + 1;
      File newBucket = new File(currBucket.getParentFile(), bucketParts[0] + "_" + nextBucketNumber);
      newBucket.mkdirs();
      return newBucket;
   }

   /**
    * 
    * @param dboption assumed to be either -dbexport or -dbimport.
    */
   private static void usage(String dboption) {
      if (OPTION_DB_EXPORT.equals(dboption)) {
         out(
               "Usage: java com.percussion.tablefactory.tools.PSCatalogTableData " + OPTION_DB_PROPS + 
               " <properties_file_name> " + OPTION_STORAGE_PATH + " <storage_folder_location>" +         
               "[" + OPTION_TABLES_TO_SKIP + " <tables_to_skip_options>]");
      }
      else {
         out(
               "Usage: java com.percussion.tablefactory.PSJdbcTableFactory " + OPTION_DB_PROPS + 
               " <properties_file_name> " + OPTION_STORAGE_PATH + " <storage_folder_location>");
      }
      out("Where:");
      out("properties_file_name - path to the properties file defining the");
      out("    backend database server.  Required.");
      out("storage_folder_location - path to folder under which the data, def and binary files are stored");
      out("    Required.");
      if (OPTION_DB_EXPORT.equals(dboption)) {
         out("tables_to_skip_options - pipe (|) seperated list of tables to skip, <tablename>-data.sql files are created for these tables.");
          out("Example:");
         out("com.percussion.tablefactory.tools.PSCatalogTableData " +
         "-dbprops serverProps.properties -storagepath e:/Rhythmyx/dataexport -tablestoskip PSX_EDITION_TASK_PARAM|PSX_PUBSERVER_PROPERTIES");
      }
      else{
         out("Example:");
         out("com.percussion.tablefactory.PSJdbcTableFactory " +
         "-dbprops serverProps.properties -storagepath e:/Rhythmyx/dataexport");
      }
      System.exit(1);
   }

   private static void out(String s)
   {
      System.out.println( s );
   }   
}
