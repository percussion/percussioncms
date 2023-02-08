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
package com.percussion.sitemanage.importer;

import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author JaySeletz
 *
 */
public class PSSiteImportLogger implements IPSSiteImportLogger
{
    private PSLogObjectType objectType;
    
    private StringBuilder log;
    

    /**
     * Separator for parts of a log msg.
     */
    private static final String LOG_MSG_SEP = ": ";
    
    /**
     * List of messages, first part is the category, second part is the message
     */
    private List<PSPair<String, String>> errorLogMessages = null; 
    
    private CountDownLatch waitingThreadCount = null;
    
    /**
     * Construct a logger.
     * 
     * @param objectType The type of object being imported.
     */
    public PSSiteImportLogger(PSLogObjectType objectType)
    {
        Validate.notNull(objectType);
        this.objectType = objectType;
        log = new StringBuilder();
    }
    
    @Override
    public void appendLogMessage(PSLogEntryType type, String category, String message)
    {
        Validate.notNull(type);
        Validate.notEmpty(category);
        Validate.notEmpty(message);
        
        log.append(type);
        log.append(LOG_MSG_SEP);
        log.append(category);
        log.append(LOG_MSG_SEP);
        log.append(message);
        log.append("\n");
        
        if (type.equals(PSLogEntryType.ERROR) && errorLogMessages != null)
        {
            errorLogMessages.add(new PSPair<>(category, message));
        }
    }
    
    /**
     * Gets the current log buffer as a String.
     */
    public String getLog()
    {
        return log.toString();
    }
    
    @Override
    public PSLogObjectType getType()
    {
        return objectType;
    }

    @Override
    public void logErrors()
    {

        errorLogMessages = new ArrayList<>();
    }
    
    @Override
    public List<PSImportLogEntry> getErrors(PSLogObjectType type, String objectId, String description)
    {
        Validate.notNull(type);
        Validate.notEmpty(objectId);
        Validate.notEmpty(description);
        
        if (errorLogMessages == null)
            return null;
        
        List<PSImportLogEntry> result = new ArrayList<>();
        for (PSPair<String,String> message : errorLogMessages)
        {
            result.add(new PSImportLogEntry(objectId, type.name(), new Date(), description, message.getFirst(), message.getSecond()));
        }
        
        return result;
    }

    @Override
    public synchronized void setWaitCount(int count)
    {
        if (waitingThreadCount != null)
        {
            throw new IllegalStateException("wait count has already been set on this object");
        }
        
        waitingThreadCount = new CountDownLatch(count);
    }

    @Override
    public void removeFromWaitCount()
    {
        if (waitingThreadCount != null)
        {
            waitingThreadCount.countDown();
        }
    }

    @Override
    public void waitForThreads(long timeoutSeconds)
    {
        if (waitingThreadCount == null)
            return;
        
        try
        {
            waitingThreadCount.await(timeoutSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
    
    
}
