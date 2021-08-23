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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.delivery.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.percussion.error.PSExceptionUtils;
import com.percussion.legacy.security.deprecated.PSAesCBC;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.server.PSServer;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.types.PSPair;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.client.ClientBuilder.newClient;

/**
 * This handler delivers content to the amazon s3.
 */
public class PSAmazonS3DeliveryHandler extends PSBaseDeliveryHandler
{
    private static final String CREDS_WRONG_MSG = "Either bucket {} doesn't exist or the credentials to access the bucket are wrong. Error: {}";
    private String targetRegion = Regions.DEFAULT_REGION.getName();
    private static Boolean isEC2Instance = null;

    public String getTargetRegion()
    {
        return targetRegion;
    }

    public void setTargetRegion(String targetRegion)
    {
        this.targetRegion = targetRegion;
    }

    /**
     * Logger.
     */
    private static final Logger log = LogManager.getLogger(PSAmazonS3DeliveryHandler.class);

    private ConcurrentHashMap<Long,TransferManager> jobTransferManagers;

    @Override
    public void init(long jobid, IPSSite site, IPSPubServer pubServer) throws PSDeliveryException{
        //Call the base class
        super.init(jobid, site, pubServer);
        if (jobTransferManagers == null) {
            jobTransferManagers = new ConcurrentHashMap<>();
        }
        if(!jobTransferManagers.containsKey(jobid)) { // if key does not exist
            AmazonS3 s3Client = getAmazonS3Client(pubServer);

            TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
            jobTransferManagers.put(jobid, tm);
        }
    }

    private Region getConfiguredAWSRegion()
    {
        return Region.getRegion(Regions.fromName(targetRegion));
    }

    @Override
    protected void releaseForDelivery(long jobId){
        super.releaseForDelivery(jobId);
        TransferManager t = jobTransferManagers.get(jobId);
        if(t != null) {
            t.shutdownNow(true);
            jobTransferManagers.remove(jobId);
        }
    }

    /**
     * Remove the single item specified by location. This method can be
     * overridden in a subclass.
     *
     * @param item The item to be removed
     * @param jobId The current jobId
     * @param location the location, never <code>null</code> or empty.
     * @return the result of the removal operation
     */
    @Override
    protected IPSDeliveryResult doRemoval(Item item, long jobId, String location)
    {
        JobData job = m_jobData.get(jobId);
        IPSPubServer pubServer = job.m_pubServer;
        PSDeliveryException de = null;
        String destPath = location.substring(1);
        String bucketName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, "");
        try
        {
            AmazonS3 s3Client = getAmazonS3Client(pubServer);
            s3Client.deleteObject(bucketName, destPath);
        }
        catch(PSDeliveryException e){
            de = e;
        }
        catch(Exception e){
            de = new PSDeliveryException(
                    IPSDeliveryErrors.COULD_NOT_DELETE_FROM_AMAZON, e, location, bucketName, (StringUtils.isBlank(e
                    .getLocalizedMessage()) ? e.getClass().getName() : e
                    .getLocalizedMessage()));

        }
        if (de!=null)
        {
            return getItemResult(Outcome.FAILED, item, jobId, de
                    .getLocalizedMessage());
        }
        return getItemResult(Outcome.DELIVERED, item, jobId, null);
    }

    @Override
    protected IPSDeliveryResult doDelivery(Item item, long jobId,
                                           String location)
            throws PSDeliveryException
    {
        if (StringUtils.isBlank(location))
        {
            throw new IllegalArgumentException(
                    "location may not be null or empty");
        }
        JobData job = m_jobData.get(jobId);
        IPSPubServer pubServer = job.m_pubServer;
        PSDeliveryException de = null;
        String key = location.substring(1);
        String bucketName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, "");
        try
        {
            AmazonS3 s3Client = getAmazonS3Client(pubServer);
            TransferManager tm;
            if(jobTransferManagers.containsKey(jobId)){  // check for the jobId
                tm = jobTransferManagers.get(jobId);
            }else{ // if does not exist
                tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
                jobTransferManagers.put(jobId, tm);
            }
            if (item.getFile() != null) {
                String checksum = "";
                try (InputStream is = new FileInputStream(item.getFile())) {

                    //reading server.properties to check if checksum check needs to be done or not, default is false
                    if (PSServer.getServerProps().getProperty("optimizePublishWithChecksum", "false").equalsIgnoreCase("true")) {
                        checksum = calculateChecksum(is);
                        boolean checksumValueChanged = true;
                        log.debug("local CheckSum value -> {}" , checksum);
                        try {
                            GetObjectMetadataRequest mreq = new GetObjectMetadataRequest(bucketName, key);
                            ObjectMetadata retrieved_metadata = s3Client.getObjectMetadata(mreq);
                            if (retrieved_metadata != null) {
                                String s3CheckSum = retrieved_metadata.getUserMetaDataOf("Perc-Content-Checksum");
                                log.debug("S3 Checksum  property -> {}" , s3CheckSum);
                                if (checksum != null && checksum.equalsIgnoreCase(s3CheckSum)) {
                                    checksumValueChanged = false;
                                }
                            }

                        } catch (AmazonS3Exception e) {
                            if(e.getStatusCode() == 404){
                                log.debug("The object {} was not found so this is a new item.",key);
                            }else{
                                log.error(PSExceptionUtils.getMessageForLog(e));
                            }
                            // In any error state we can't confirm that the object hasn't changed
                            // so always flag it as a change so publish is attempted.
                            checksumValueChanged=true;
                        }
                        if (checksumValueChanged) {
                            copyToAmazonDirect(tm, bucketName, key, item.getFile(), item.getMimeType(), item.getLength(), checksum);
                        }
                    } else {
                        copyToAmazonDirect(tm, bucketName, key, item.getFile(), item.getMimeType(), item.getLength(), checksum);
                    }
                }
            }else{
                try(InputStream is = item.getResultStream()) {
                    copyToAmazon(tm, bucketName, key, is, item.getMimeType(), item.getLength());
                }
            }


        }
        catch(PSDeliveryException e){
            de = e;
        }
        catch(Exception e){
            de = new PSDeliveryException(
                    IPSDeliveryErrors.COULD_NOT_COPY_TO_AMAMZON, e, location, bucketName, (StringUtils.isBlank(e
                    .getLocalizedMessage()) ? e.getClass().getName() : e
                    .getLocalizedMessage()));

        }

        if (de != null)
        {
            return getItemResult(Outcome.FAILED, item, jobId, de
                    .getLocalizedMessage());
        }

        return new PSDeliveryResult(Outcome.DELIVERED, null, item.getId(),
                jobId, item.getReferenceId(), location.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * calculate the checksum of provided InputStream
     *
     * @param originalInputStream the result data stream, should not be null. The input stream should
     * be closed by the caller.
     *
     * @return return the checksum value
     */

    public String calculateChecksum(InputStream originalInputStream)
    {
        String result="";
        try {
            byte[] byteArray = IOUtils.toByteArray(originalInputStream);

            result = DigestUtils.sha256Hex(byteArray);
        }catch(Exception e){
            log.error("Exception occurred while calculateChecksum -- > {}", e.getMessage());
            log.debug(e);
        }
        return result;
    }

    private void copyToAmazon(TransferManager tm, String bucketName, String key, InputStream is, String mimeType, long contentLength) throws AmazonClientException, InterruptedException
    {
        try{
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(mimeType);
            metadata.setContentLength(contentLength);
            metadata.setCacheControl("max-age=20");
            Upload myUpload = tm.upload(new PutObjectRequest(bucketName, key, is, metadata));
            myUpload.waitForCompletion();
        }
        finally
        {
            if(is != null){
                try{
                    is.close();
                } catch (IOException e) {
                    log.debug(e);
                }
            }
        }

    }

    private void copyToAmazonDirect(TransferManager tm, String bucketName, String key, File file, String mimeType, long contentLength,String checksum) throws IOException, InterruptedException {

        ObjectMetadata metadata = new ObjectMetadata();
        try(InputStream fileInputStream = new FileInputStream(file)){
            metadata.setContentType(mimeType);
            metadata.setContentLength(contentLength);
            metadata.setCacheControl("max-age=20");
            metadata.addUserMetadata("Perc-Content-Checksum", checksum);
            Upload myUpload = tm.upload(new PutObjectRequest(bucketName, key, fileInputStream, metadata));
            myUpload.waitForCompletion();
        }

    }

    public static boolean isEC2Instance(){
        if(isEC2Instance != null){
            return isEC2Instance;
        }
        try {
            Client client = newClient();

            WebTarget resource = client.target("http://169.254.169.254/latest/meta-data/");

            Invocation.Builder request = resource.request();
            request.accept(MediaType.APPLICATION_JSON);

            Response response = request.get();

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                isEC2Instance = Boolean.TRUE;
                return true;
            } else {
                isEC2Instance = Boolean.FALSE;
            }
        }catch(Exception e){
            //means not an EC2 Server
            isEC2Instance = Boolean.FALSE;
        }
        return isEC2Instance;
    }

    private AmazonS3 getAmazonS3Client(IPSPubServer pubServer) throws PSDeliveryException{
        AmazonS3 s3 = null;

        String selectedRegionName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_EC2_REGION, "");
        if(selectedRegionName == null || selectedRegionName.trim().equals("")){

            //Default to EC2 regions
            try {
                if (Regions.getCurrentRegion() != null){
                    selectedRegionName = Regions.getCurrentRegion().getName();
                }
            }catch(Exception e){
                log.debug(e);
            }
            //Fallback to publisher-beans.xml
            if(selectedRegionName == null || selectedRegionName.trim().equals("") ){
                selectedRegionName = getConfiguredAWSRegion().getName();
            }
        }

        if(isEC2Instance()){
            log.debug("EC2 Instance Running");
            s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .withRegion(selectedRegionName)
                    .build();
        }else {

            String accessKey = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY, "");
            String secretKey = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, "");


            try {

                accessKey = decrypt(accessKey);
                secretKey = decrypt(secretKey);
            } catch (Exception e) {
                log.error(PSExceptionUtils.getMessageForLog(e));
                throw new PSDeliveryException(IPSDeliveryErrors.COULD_NOT_DECRYPT_CREDENTIALS, e, getExceptionMessage(e));
            }


            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            s3 =  AmazonS3ClientBuilder.standard().withRegion(selectedRegionName).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
        }
        return s3;

    }

    /**
     * Decrypt the string.  Will attempt to decrypt using legacy algorithms to handle upgrade scenario.
     * @param dstr base64 encoded encrypted string
     * @return clear text version of the string.
     */
    private String decrypt(String dstr) {

        try {

            return PSEncryptor.decryptString(dstr);
        } catch (PSEncryptionException e) {
            log.warn("Decryption failed: {}. Attempting to decrypt with legacy algorithm",e.getMessage());
            try {
                PSAesCBC aes = new PSAesCBC();
                return aes.decrypt(dstr, IPSPubServerDao.encryptionKey);
            } catch (PSEncryptionException psEncryptionException) {
                log.error("Unable to decrypt string. Error: {}",
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(e);
                return dstr;
            }
        }
    }

    private String getExceptionMessage(Exception e){
        return (StringUtils.isBlank(e
                .getLocalizedMessage()) ? e.getClass().getName() : e
                .getLocalizedMessage());
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#checkConnection(com.percussion.services.pubserver.IPSPubServer, com.percussion.services.sitemgr.IPSSite)
     */
    @Override
    public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
    {
        boolean result = true;
        String bucketName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, "");
        try
        {
            AmazonS3 s3Client = getAmazonS3Client(pubServer);
            s3Client.getS3AccountOwner();
            result = s3Client.doesBucketExistV2(bucketName);
        }
        catch (Exception e)
        {
            log.error(CREDS_WRONG_MSG, bucketName,
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            result = false;
        }
        return result;
    }

    public PSPair<Boolean, String> publishTestImage(IPSPubServer pubServer, IPSSite site, String token)
    {
        if(!checkConnection(pubServer, site)){
            return new PSPair<>(Boolean.FALSE, CREDS_WRONG_MSG);
        }
        PSPair<Boolean,String> result = new PSPair<>(Boolean.TRUE, "Successfully published, accessed and deleted image to amazon s3");

        String key = "Assets/uploads/" + generateTestImageKey(token);
        String bucketName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, "");
        //Create Image Asset
        TransferManager tm =null;

        try( InputStream in = new FileInputStream(PSServer.getRxDir().getAbsolutePath() + PERC_TEST_IMG_DIR + PERC_TEST_IMG)) {
            AmazonS3 s3Client = getAmazonS3Client(pubServer);
            tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
            copyToAmazon(tm, bucketName, key, in, "image/jpeg", in.available());
            s3Client = getAmazonS3Client(pubServer);
            s3Client.getObject(bucketName, key);
            s3Client.deleteObject(bucketName, key);

        }
        catch (Exception e)
        {
            log.error("Error copying image to amazon s3 bucket. {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            result = new PSPair<>(Boolean.FALSE, e.getLocalizedMessage());
        }
        finally
        {
            if(tm != null)
                tm.shutdownNow();
        }
        return result;
    }

    public static String generateTestImageKey(String token)
    {
        return FilenameUtils.getBaseName(PSAmazonS3DeliveryHandler.PERC_TEST_IMG) + "-" + token
                + "." + FilenameUtils.getExtension(PSAmazonS3DeliveryHandler.PERC_TEST_IMG);
    }

    public static final String PERC_TEST_IMG = "percussion_test_image_donotuse.jpg";

    public static final String PERC_TEST_IMG_DIR = "/sys_resources/images/";


}
