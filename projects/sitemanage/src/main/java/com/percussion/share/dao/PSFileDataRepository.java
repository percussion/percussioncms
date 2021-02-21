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
package com.percussion.share.dao;

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.text.MessageFormat.format;


/**
 * 
 * Loads files from a directory into data objects.
 * <p>
 * The files are read-only and are not modified.
 * <p>
 * Once the class is instantiated the expected use is:
 * <p>
 * For the first time:
 * <pre>
 * repo.init();
 * repo.getData();
 * </pre>
 * After initialized use poll: 
 * <pre>
 * repo.poll();
 * repo.getData();
 * </pre>
 * 
 * @author adamgent
 *
 * @param <T> the data type that the files are read into.
 * @see #poll()
 * @see #getData()
 */
public abstract class PSFileDataRepository<T>
{

    private String repositoryDirectory;

    private File root;

    private String fileExt = "xml";
    
    private AtomicReference<Data<T>> data = new AtomicReference<>();
    
    private boolean initialized = false;
    
    private static class Data<T> {
        protected Set<PSFileDataRepository.PSFileEntry> files;
        protected T data;
        public Data(T data, Set<PSFileDataRepository.PSFileEntry> files)
        {
            super();
            this.data = data;
            this.files = files;
        }
    }
    
    

    /**
     * 
     * Represents a single file in the repository.
     * 
     * @author adamgent
     *
     */
    public static class PSFileEntry {
        private String id;
        private String fileName;
        private Long lastModifiedDate;
        
        public PSFileEntry(String id, String fileName, Long lastModifiedDate)
        {
            super();
            this.id = id;
            this.fileName = fileName;
            this.lastModifiedDate = lastModifiedDate;
        }
    
        public String getId()
        {
            return id;
        }
    
        public String getFileName()
        {
            return fileName;
        }
    
        public Long getLastModifiedDate()
        {
            return lastModifiedDate;
        }
        
        public InputStream getInputStream() throws IOException {
            File w = new File(getFileName());
            return new FileInputStream(w);
    
        }
    
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
            return result;
        }
    
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PSFileEntry other = (PSFileEntry) obj;
            if (fileName == null)
            {
                if (other.fileName != null)
                    return false;
            }
            else if (!fileName.equals(other.fileName))
                return false;
            if (id == null)
            {
                if (other.id != null)
                    return false;
            }
            else if (!id.equals(other.id))
                return false;
            if (lastModifiedDate == null)
            {
                if (other.lastModifiedDate != null)
                    return false;
            }
            else if (!lastModifiedDate.equals(other.lastModifiedDate))
                return false;
            return true;
        }
    }



    /**
     * Called to initialize the directory that represents the file repository
     * by polling the files for the first time.
     */
    public void init() throws PSDataServiceException {
        if (initialized == true) return;
        
        try
        {
            poll();
        }
        catch (IOException | PSValidationException | PSXmlFileDataRepository.PSXmlFileDataRepositoryException e)
        {
            throw new PSDataServiceException(e);
        }
        
        initialized = true;

    }

    /**
     * Retrieve the currently loaded repository data.
     * @return never <code>null</code>.
     */
    public T getData() throws PSDataServiceException {
        init();
        return data.get().data;
    }
    
    private File getRoot() throws IOException {
        if (root != null) return root;
        root = new File(getRepositoryDirectory());
        if (!root.exists()) {
            log.error("Repository directory: {} does not exist.",getRepositoryDirectory());
            log.info("Creating directory: {}",  root);
            FileUtils.forceMkdir(root);
        }
        
        return root;
    }
    
    
    /**
     * Reloads the files if any changes have been made to them.
     * <p>
     * Poll should be called from quartz or some other scheduler.
     * @throws IOException 
     */
    public synchronized void poll() throws IOException, PSValidationException, PSXmlFileDataRepository.PSXmlFileDataRepositoryException {
        if (log.isTraceEnabled()) {
            log.trace(format("Polling folder: {0} for file ext: {1}", getRoot(), getFileExt()));
        }
        
        Collection<File> files = getFiles();
        
        Set<PSFileDataRepository.PSFileEntry> fileEntries = new HashSet<>();
        
        for( File file : files) {
            PSFileDataRepository.PSFileEntry fileEntry = new PSFileDataRepository.PSFileEntry(toId(file.getName()), file.getAbsolutePath(), file.lastModified());
            fileEntries.add(fileEntry);
        }
        
        Set<PSFileDataRepository.PSFileEntry> oldEntries;
        if (data.get() != null)
            oldEntries = data.get().files;
        else
            oldEntries = new HashSet<>();
        
        if ( ! oldEntries.equals(fileEntries)  || 
                (fileEntries.isEmpty())) {
            if (initialized) {
                log.debug("Files have changed under: {} reloading",getRoot() );
            }
            else {
                log.debug("Loading files from: {}" ,getRoot());
            }
            T object = update(fileEntries);
            data.set(new Data<> (object, fileEntries));
        }
        else {
            log.trace("Files have not changed under: {}",  getRoot());
        }

    }
    
    /**
     * The collection of all files to read from.
     * <p>
     * This method is safe to override if it is
     * {@link #getRepositoryDirectory()} and {@link #getFileExt()}
     * may not be applicable.
     * 
     * @return never <code>null</code>.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected Collection<File> getFiles() throws IOException {
        return FileUtils.listFiles(getRoot(), new String[] { getFileExt() } , false);
    }
    
    /**
     * Reloads data from the set of given files.
     * <p>
     * This method is safe to override. <em>For thread safety the returned object
     * should be a newly created object and not mutation 
     * of the current {@link #getData()}.</em>
     * @param files never <code>null</code>.
     * @return recommended that it not be <code>null</code>.
     * 
     * @throws IOException
     */
    protected abstract T update(Set<PSFileDataRepository.PSFileEntry> files) throws IOException, PSValidationException, PSXmlFileDataRepository.PSXmlFileDataRepositoryException;

    /**
     * Turns the filename into an id.
     * <p>
     * This method is safe to override.
     * @param fileName never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected String toId(String fileName)
    {
        return FilenameUtils.getBaseName(fileName);
    }

    /**
     * The directory to load files from.
     * 
     * @return never <code>null</code>.
     */
    protected String getRepositoryDirectory()
    {
        return repositoryDirectory;
    }

    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        this.repositoryDirectory = widgetsRepositoryDirectory;
    }

    
    protected String getFileExt()
    {
        return fileExt;
    }

    public void setFileExt(String fileExt)
    {
        this.fileExt = fileExt;
    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Logger log = LogManager.getLogger(getClass());

}
