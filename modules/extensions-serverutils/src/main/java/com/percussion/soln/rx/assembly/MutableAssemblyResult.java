package com.percussion.soln.rx.assembly;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.io.IOUtils;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.util.PSPurgableTempFile;


public class MutableAssemblyResult extends DelegateToAssemblyItemAssemblyResult {
    
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    private Status status = Status.SUCCESS;
    private byte[] resultData;
    private String mimeType = "text/html";
    private PSPurgableTempFile resultFile;
    private File tempDir;
    private boolean fileReleased;
    private boolean paginated = false;

    public MutableAssemblyResult(IPSAssemblyItem assemblyItem, byte[] resultData, String mimeType) {
        super();
        this.setAssemblyItem(assemblyItem);
        setResultData(resultData);
        setMimeType(mimeType);
    }

    public long getResultLength() {
        return getResultData().length;
    }

    public InputStream getResultStream() {
        return new ByteArrayInputStream(getResultData());
    }
    

    public String toResultString() throws IllegalStateException,
            UnsupportedEncodingException {
        if (getMimeType().startsWith("text/")) {
            throw new IllegalStateException(
                    "The result must have a mimetype of text/something");
        }
        return new String(getResultData(), StandardCharsets.UTF_8);
    }



    public byte[] getResultData() {
        return resultData;
    }


    public void setResultData(byte[] resultData) {
        this.resultData = resultData;
    }


    /**
     * Metadata to be delivered to the metadata service for item.  This
     * is extracted from the "$perc.metadata" binding
     *
     * @return null if there is no metadata defined
     */
    @Override
    public Map<String, Object> getMetaData() {
        return null;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public void clearResults() {
        setResultData(null);
        getAssemblyItem().setResultData(null);
        if (resultFile != null && ! fileReleased) {
           resultFile.release();
           resultFile = null;
           fileReleased = true;
        }
        
    }

    public boolean isPaginated() {
        return paginated;
    }

    public PSPurgableTempFile getResultFile() throws IOException {
        if (resultFile == null) {
            resultFile = new PSPurgableTempFile("result", ".tmp", getTempDir());
            try (OutputStream os = new FileOutputStream(resultFile)){
                IOUtils.write(getResultData(), os);
            }
        }

        fileReleased = true;
        return resultFile;
    }

    public boolean isSuccess() {
        return Status.SUCCESS == getStatus();
    }

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Gets the owner ID of the assembled item.
     *
     * @return the ID. It may be <code>null</code> if unknown.
     */
    @Override
    public IPSGuid getOwnerId() {
        return null;
    }

    /**
     * Sets the owner ID of the assembled item.
     *
     * @param ownerId the owner ID. It may be <code>null</code> if unknown.
     */
    @Override
    public void setOwnerId(IPSGuid ownerId) {

    }

    /**
     * Set the publishing server id to use with the delivery item.
     *
     * @param pubserverid the ID of the publishing server.
     *                    It may be <code>null</code> if the publish-server is unknown.
     */
    @Override
    public void setPubServerId(Long pubserverid) {

    }

    /**
     * Get the publishing server id that is used for this item.
     *
     * @return publishing server id. It is <code>null</code> if the publish-server is unknown.
     */
    @Override
    public Long getPubServerId() {
        return null;
    }

    public void setStatus(Status status) {
        getAssemblyItem().setStatus(status);
        this.status = status;
    }

    public void setPaginated(boolean paginated) {
        this.paginated = paginated;
    }


}
