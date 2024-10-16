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
package com.percussion.share.test;

import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.apache.commons.lang.Validate.noNullElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Generic test utility class to clean up
 * entities after a test is finished and handling exceptions properly.
 * 
 * <p>
 * To use this utility override {@link #clean(Object)}.
 * In your test use {@link #add(Object...)} to add objects you want to delete during
 * tearDown.
 * 
 * Then in tearDown call {@link #clean()} to cleanup.
 * 
 * Take note that {@link #clean()} should only be called once consequently reuse of the cleaner 
 * instantiated objects is discouraged. Instead create a new cleaner object.
 *  
 * @author adamgent
 *
 * @param <ID> identifier for the object to delete.
 */
public abstract class PSTestDataCleaner<ID>
{
    private List<ID> dataIds = new ArrayList<ID>();
    private List<ID> failedIds = new ArrayList<ID>();
    
    private boolean failOnErrors = false;
    private boolean failed = false;
    private boolean removeDuplicates = true;
    private boolean finished = false;
    
    
    /**
     * Add an id(s) for the cleaner to clean.
     * @param ids never any <code>null</code> elements.
     */
    public void add(ID ... ids) {
        noNullElements(ids, "one of the ids passed to the cleaner was null");
        for (ID id : ids) {
            boolean shouldAdd = 
                ! ( isRemoveDuplicates() && getDataIds().contains(id));
            if ( shouldAdd )
                dataIds.add(id);
        }
    }
    
    /**
     * Remove an id(s) from the cleaner.
     * @param ids never any <code>null</code> elements.
     */
    public void remove(ID ...ids) {
        noNullElements(ids, "one of the ids passed to the cleaner was null");
        dataIds.removeAll(asList(ids));
    }
    
    /**
     * Cleans a single entity of with id.
     * @param id id of the entity, never <code>null</code>.
     * @throws Exception any exception can be thrown but will be caught by the cleaner.
     */
    protected abstract void clean(ID id) throws Exception;
    
    /**
     * Gets the stored data IDs.
     * 
     * @return the data IDs, may be re-ordered by the derived class, never
     * <code>null</code>.
     */
    protected List<ID> getDataIds()
    {
        return dataIds;
    }
    
    /**
     * Will clean all the entities registered with this cleaner.
     * This should only be called once and will most likely fail if called multiple times.
     * @return <code>true</code> if successful.
     */
    public boolean clean() {
        Collection<ID> ids = getDataIds();
        Set<ID> processed = new HashSet<ID>();
        if (ids.isEmpty()) {
            log.debug("Nothing to clean.");
            return true;
        }
        log.debug("Started Cleaning....");
        
        for (ID id : ids) {
            log.debug("Trying to clean id: " + id);
            if ( processed.contains(id) ) {
                if ( isRemoveDuplicates()) {
                    log.debug("Duplicate id: " + id);
                    continue;
                }
                log.warn("Duplicate id:" + id);
            }
            try {
                clean(id);
                log.debug("Successfully cleaned id: " + id);
            }
            catch(Exception e) {
                log.error("Failed to clean id: " + id + " because of: ", e);
                failedIds.add(id);
                setFailed(true);
            }
            
            processed.contains(id);
            
        }
        finished = true;
        log.debug("Finished Cleaning....");
        if ( isFailed() && isFailOnErrors()) {
            log.debug("Cleaning was set to fail and there were errors so we are failing now.");
            fail("Failed to clean ids: " + failedIds);
        }
        return ! isFailed();
    }
    
    /**
     * Runs a list of cleaners in order and then validates that the cleaners succeeded.
     * @see #validateCleaners(PSTestDataCleaner...)
     * @param cleaners no <code>null</code> elements.
     */
    public static final synchronized void runCleaners(PSTestDataCleaner<?> ...cleaners) {
        noNullElements(cleaners, "No null cleaners");
        for (PSTestDataCleaner<?> c : cleaners) {
            c.clean();
        }
        validateCleaners(cleaners);
    }
    
    /**
     * Validates a list of cleaners in order.
     * @param cleaners no <code>null</code> elements.
     */
    public static final void validateCleaners(PSTestDataCleaner<?> ...cleaners) {
        noNullElements(cleaners, "No null cleaners");
        List<PSTestDataCleaner<?>> failedCleaners = new ArrayList<PSTestDataCleaner<?>>();
        
        for (PSTestDataCleaner<?> c : cleaners) {
            if (c.isFailed()) failedCleaners.add(c);
        }
        if (! failedCleaners.isEmpty() )
            fail("Some cleaners failed: " + failedCleaners);
    }
    
    /**
     * If true will remove duplicate ids from the cleaning list when clean is
     * performed.
     * @return <code>true</code> will remove duplicate ids
     */
    public boolean isRemoveDuplicates()
    {
        return removeDuplicates;
    }

    public void setRemoveDuplicates(boolean removeDuplicates)
    {
        this.removeDuplicates = removeDuplicates;
    }
    
    

    /**
     * Test if the cleaner is finished.
     * @return <code>true</code> if the cleaner is done cleaning.
     */
    public boolean isFinished()
    {
        return finished;
    }

    public void setFinished(boolean finished)
    {
        this.finished = finished;
    }

    /**
     * Test if the cleaner has failed to clean.
     * @return <code>true</code> if the cleaner failed to clean all ids, <code>false</code> otherwise.
     */
    public boolean isFailed()
    {
        return failed;
    }

    public void setFailed(boolean failed)
    {
        this.failed = failed;
    }

    /**
     * If true will throw an assertion failure if there
     * are any errors during cleanup.
     * 
     * @return never <code>null</code>, default is false.
     */
    public boolean isFailOnErrors()
    {
        return failOnErrors;
    }

    public void setFailOnErrors(boolean fail)
    {
        this.failOnErrors = fail;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSTestDataCleaner{");
        sb.append("dataIds=").append(dataIds);
        sb.append(", failedIds=").append(failedIds);
        sb.append(", failOnErrors=").append(failOnErrors);
        sb.append(", failed=").append(failed);
        sb.append(", removeDuplicates=").append(removeDuplicates);
        sb.append(", finished=").append(finished);
        sb.append(", log=").append(log);
        sb.append('}');
        return sb.toString();
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Logger log = LogManager.getLogger(getClass());
    
}
