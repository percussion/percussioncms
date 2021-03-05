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

package com.percussion.rx.delivery.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.IOUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.server.PSServer;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.security.deprecated.PSAesCBC;
import com.percussion.utils.types.PSPair;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Post edition task that will publish web_resources files to amazon s3 bucket.
 * Makes an MD5 check before publishing the resources. Any files that don't exist
 * under web_resources are deleted from the s3 bucket when published.   
 *
 */
public class PSAmazonS3EditionTask implements IPSEditionTask
{

   private static final String WEB_RESOURCES = "web_resources";

   private File webResFolder = null;

   private String webResFolderPath = "";

   private IPSPubServerDao pubServerDao;
    private String targetRegion = Regions.DEFAULT_REGION.getName();

   private static Logger m_log = Logger.getLogger(PSAmazonS3EditionTask.class.getName());

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
   {
      webResFolder = new File(PSServer.getRxDir().getAbsolutePath() + "/" + WEB_RESOURCES);
      webResFolderPath = webResFolder.getAbsolutePath();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSEditionTask#perform(com.percussion.services.publisher.IPSEdition, com.percussion.services.sitemgr.IPSSite, java.util.Date, java.util.Date, long, long, boolean, java.util.Map, com.percussion.rx.publisher.IPSEditionTaskStatusCallback)
    */
   public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobId, long duration,
         boolean success, Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception
   {

      IPSPubServer pubServer = getPubServerDao().findPubServer(edition.getPubServerId());
      String bucketName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, "");
      TransferManager tm = null;
      try
      {
         AmazonS3 s3Client = getAmazonS3Client(pubServer);
         tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
         //Get list of files to be deleted and to be uploaded
         PSPair<List<File>, List<String>> fileList = getFileList(s3Client, bucketName);
         
         // Delete files that don't exist
         for (String key : fileList.getSecond())
         {
            s3Client.deleteObject(bucketName, key);
         }
         
         // Upload modified files
         MultipleFileUpload mfUpload = tm.uploadFileList(bucketName, WEB_RESOURCES, webResFolder, fileList.getFirst());
         mfUpload.waitForCompletion();
      }
      catch (Exception e)
      {
         m_log.error(
               "Error occurred while copying the web_resources files to amazon s3 bucket for Site: " + site.getLabel(), e);
         throw e;
      }
      finally
      {
         if (tm != null)
            tm.shutdownNow();
      }

   }

   /**
    * Creates amazon s3 client from the credentials.
    * @param pubServer assumed not <code>null</code>
    * @return AmazonS3 client.
    * @throws Exception
    */
   private AmazonS3 getAmazonS3Client(IPSPubServer pubServer) throws Exception
   {
       AmazonS3 s3 = null;

       if(PSAmazonS3DeliveryHandler.isEC2Instance()){
           m_log.debug("EC2 Instance Running");
           s3 = AmazonS3ClientBuilder.standard()
                   .withCredentials(new InstanceProfileCredentialsProvider(false))
                   .withRegion(Regions.getCurrentRegion().getName())
                   .build();
       }else {
           m_log.debug("Using Access/Security Key");
           PSAesCBC aes = new PSAesCBC();
           String accessKey = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY, "");
           String secretKey = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, "");
           String selectedRegionName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_EC2_REGION, "");

           try {
               accessKey = aes.decrypt(accessKey, IPSPubServerDao.encryptionKey);
               secretKey = aes.decrypt(secretKey, IPSPubServerDao.encryptionKey);
           } catch (Exception e) {
               m_log.error(e);
               throw new PSDeliveryException(IPSDeliveryErrors.COULD_NOT_DECRYPT_CREDENTIALS, e, getExceptionMessage(e));
           }
           if(selectedRegionName == null || selectedRegionName.trim().equals("")){

               //Default to EC2 regions
               try {
                   if (Regions.getCurrentRegion() != null){
                       selectedRegionName = Regions.getCurrentRegion().getName();
                   }
               }catch(Exception e){
                   //Do nothing
               }
               //Fallback to publisher-beans.xml
               if(selectedRegionName == null || selectedRegionName.trim().equals("") ){
                   selectedRegionName = getConfiguredAWSRegion().getName();
               }
           }

           BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
           s3 =  AmazonS3ClientBuilder.standard().withRegion(selectedRegionName).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
       }
       return s3;

   }
    public String getTargetRegion()
    {
        return targetRegion;
    }

    public void setTargetRegion(String targetRegion)
    {
        this.targetRegion = targetRegion;
    }

    private Region getConfiguredAWSRegion()
    {
        return Region.getRegion(Regions.fromName(targetRegion));
    }

    private String getExceptionMessage(Exception e){
        return (StringUtils.isBlank(e
                .getLocalizedMessage()) ? e.getClass().getName() : e
                .getLocalizedMessage());
    }

   /**
    * Returns the list of files to be deleted and to be uploaded. Gets the list of files from file system
    * and compares their md5hash with the list of files from web_resources folder from amazon s3 bucket.
    * @param s3Client assumed not <code>null</code>.
    * @param bucketName name of the Amazon S3 bucket assumed not null.
    * @return PSPair the first object is a list files that needs to be uploaded
    * and the second object is a list of keys for the corresponding objects that needs to be
    * deleted.
    * @throws FileNotFoundException
    * @throws IOException
    */
   private PSPair<List<File>, List<String>> getFileList(AmazonS3 s3Client, String bucketName)
         throws FileNotFoundException, IOException
   {
      List<File> modifiedFiles = new ArrayList<>();
      List<String> delKeys = new ArrayList<>();
      Map<String, PSPair<String, File>> localFilesMap = getLocalWebResFiles();
      Map<String, String> s3FilesMap = getAmazonS3FilesMap(s3Client, bucketName);
      // Prepare files to upload
      for (String key : localFilesMap.keySet())
      {
         boolean addFile = true;
         if (s3FilesMap.keySet().contains(key))
         {
            addFile = !(localFilesMap.get(key).getFirst().equals(s3FilesMap.get(key)));
         }
         if (addFile)
         {
            modifiedFiles.add(localFilesMap.get(key).getSecond());
         }
      }

      // Prepare deletes
      delKeys.addAll(s3FilesMap.keySet());
      delKeys.removeAll(localFilesMap.keySet());

      return new PSPair<>(modifiedFiles, delKeys);
   }

   /**
    * Gets the list of local files from web_resources folder.
    * @return Map of key and a PSPair object with first object as md5hash of the file and second object 
    * to be the file itself.
    * @throws FileNotFoundException
    * @throws IOException
    */
   private Map<String, PSPair<String, File>> getLocalWebResFiles() throws FileNotFoundException, IOException
   {
      Map<String, PSPair<String, File>> localFilesMap = new HashMap<>();
      generateLocalFileMap(webResFolder, localFilesMap);
      return localFilesMap;
   }

   private void generateLocalFileMap(File dir, Map<String, PSPair<String, File>> localFilesMap)
         throws FileNotFoundException, IOException
   {
      for (File file : dir.listFiles())
      {
         if (file.isFile() && !isIgnorableFile(file))
         {
            String key = generateKey(file);
            try(InputStream is = new FileInputStream(file)){
               localFilesMap.put(key,
                     new PSPair<>(DigestUtils.md5Hex(IOUtils.toByteArray(is)), file));
            }
         }
         else if (file.isDirectory())
         {
            generateLocalFileMap(file, localFilesMap);
         }

      }
   }

   /**
    * Generates the key based on the file path relative to web_resources folder.
    * Converts the backward slashes to forward slashes if exists, as the amazon key gets generated with 
    * forward slashes.
    * @param file assumed not <code>null</code>
    * @return enerated key never <code>null</code>.
    */
   private String generateKey(File file)
   {
      String key = file.getAbsolutePath().replace(webResFolderPath, "");
      key = key.replace("\\", "/");
      return WEB_RESOURCES + key;
   }

   /**
    * Applies known rules to avoid uploading files to amazon s3 from web_resources folder
    * @param file assumed not <code>null</code>.
    * @return <code>true</code> if the files is ignorable and <code>false</code> if not.
    */
   private boolean isIgnorableFile(File file)
   {
      if (file.getName().equals("Thumbs.db"))
         return true;
      if (file.getName().startsWith("."))
         return true;
      return false;
   }

   /**
    * Helper method that returns amazon s3 file keys along with md5hash.
    * @param client
    * @param bucketName
    * @return
    */
   private Map<String, String> getAmazonS3FilesMap(AmazonS3 client, String bucketName)
   {
      ObjectListing listing;
      Map<String, String> filesMap = new TreeMap<>();

      ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(WEB_RESOURCES);

      do
      {
         listing = client.listObjects(listObjectsRequest);
         for (S3ObjectSummary summary : listing.getObjectSummaries())
         {
            filesMap.put(summary.getKey(), summary.getETag());
         }
         listObjectsRequest.setMarker(listing.getNextMarker());
      }
      while (listing.isTruncated());

      return filesMap;
   }

   public TaskType getType()
   {
      return TaskType.PREEDITION;
   }

   /**
    * Gets the pub-server service, lazy load.
    * 
    * @return pub-server service, never <code>null</code>.
    */
   private IPSPubServerDao getPubServerDao()
   {
      if (pubServerDao == null)
         pubServerDao = PSSiteManagerLocator.getPubServerDao();

      return pubServerDao;
   }

}
