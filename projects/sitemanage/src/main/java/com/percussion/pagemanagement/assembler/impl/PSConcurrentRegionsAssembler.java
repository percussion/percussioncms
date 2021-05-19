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
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.pagemanagement.assembler.IPSRegionAssembler;
import com.percussion.pagemanagement.assembler.IPSRegionsAssembler;
import com.percussion.pagemanagement.assembler.PSMergedRegion;
import com.percussion.pagemanagement.assembler.PSPageAssemblyContext;
import com.percussion.pagemanagement.assembler.PSRegionResult;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSWebserviceUtils;
import org.apache.commons.collections.list.AbstractListDecorator;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.percussion.util.IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Concurrently assemblies a collection of regions.
 * @author adamgent
 * @see PSSerialRegionsAssembler
 */
public class PSConcurrentRegionsAssembler implements IPSRegionsAssembler
{
    
    /**
     * @see #isWaitTillFinished()
     */
    private boolean waitTillFinished = false;


    public PSConcurrentRegionsAssembler()
    {
        super();
    }



    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void assembleRegions(
            IPSRegionAssembler regionAssembler, 
            IPSAssemblyItem assemblyItem, 
            PSPageAssemblyContext context,
            Collection<PSMergedRegion> mergedRegions)
    {
        StopWatch sw = new StopWatch(getClass().getSimpleName()+"#assembleRegions");
        List<RegionResultsCallable> calls = new ArrayList<>();
        List<PSMergedRegion> mrList = new ArrayList<>(mergedRegions);
        sw.start("cloneRequest");
        for(PSMergedRegion mr : mergedRegions) {
            notNull(mr, "Merged Region");
            RegionResultsCallable c = new RegionResultsCallable(PSRequestInfo.getRequestInfoMap(), regionAssembler, assemblyItem, context, mr);
            calls.add(c);
        }
        
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try
        {
            
            int i = 0;
            List<Future<List<PSRegionResult>>> results;
            if (isWaitTillFinished()) {
                results = executorService.invokeAll(calls);
            }
            else {
                results = new ArrayList<>();
                for(RegionResultsCallable c : calls) {
                    Future<List<PSRegionResult>> f = executorService.submit(c);
                    results.add(f);
                }
            }
            for(Future<List<PSRegionResult>> f : results) {
                PSMergedRegion mr = mrList.get(i);
                List<PSRegionResult> regions = new FutureList<>(f);
                context.getRegions().put(mr.getRegionId(), regions);
                ++i;
            }
            sw.stop();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        finally {
            executorService.shutdown();
            log.debug(sw.prettyPrint());
        }
    }
    
    /**
     * Callable that will delegate to the region assembler.
     * @author adamgent
     *
     */
    public static class RegionResultsCallable implements Callable<List<PSRegionResult>> {

        private IPSRegionAssembler regionAssembler;
        private IPSAssemblyItem assemblyItem;
        private PSPageAssemblyContext pageAssemblyContext;
        private PSMergedRegion mergedRegion;
        private Map<String, Object> requestInfoMap;        


        public RegionResultsCallable(Map<String,Object> requestInfoMap, IPSRegionAssembler regionAssembler,
                IPSAssemblyItem assemblyItem, PSPageAssemblyContext pageAssemblyContext, PSMergedRegion mergedRegion)
        {
            super();
            this.requestInfoMap = requestInfoMap;
            this.regionAssembler = regionAssembler;
            this.assemblyItem = assemblyItem;
            this.pageAssemblyContext = pageAssemblyContext;
            this.mergedRegion = mergedRegion;
        }


        @Override
        public List<PSRegionResult> call() throws Exception
        {
            PSThreadRequestUtils.initUserThreadRequest(requestInfoMap);
            PSRequest request = PSThreadRequestUtils.getPSRequest();
            
            setPreviewUrlGenerator(request);

            if ( isNotBlank(assemblyItem.getUserName()) ) {
                PSWebserviceUtils.setUserName(assemblyItem.getUserName());
            }
                
            
            List<PSRegionResult> regions = 
                regionAssembler.assembleRegion(assemblyItem, pageAssemblyContext, mergedRegion);
            pageAssemblyContext.getRegions().put(mergedRegion.getRegionId(), regions);
            return regions;
        }

        /**
         * Set the "custom" preview URL generator on the request, as a parameter to signal location generator, PSGeneratePubLocation, 
         * to use PSGeneratePreviewLink to generate "friendly URL" in the content of a previewed page.
         * 
         * @param req the request in which to set the parameter on, assumed not <code>null</code>.
         */
        private void setPreviewUrlGenerator(PSRequest req)
        {
            IPSAssemblyService service = PSAssemblyServiceLocator.getAssemblyService();
            try
            {
                // As we don't have a good way to use "friendly URL" to preview the thumbnail, 
                // so we don't want to generate "friendly URL" if the template is used to render a thumbnail image.  
                IPSAssemblyTemplate template = service.findTemplateByName("perc.imageThumbBinary");
                if (template != null)
                {
                    int imageThumbTemplateId = template.getGUID().getUUID();
                    String[] values = new String[] { "global/percussion/contentassembler/perc_casGeneratePreviewLink",
                            String.valueOf(imageThumbTemplateId) };
                    req.setPrivateObject(SYS_OVERWRITE_PREVIEW_URL_GEN,  values);
                }
            }
            catch (PSAssemblyException e)
            {
                log.error("Failed to find \"perc.imageThumbBinary\" template", e);
            }
        }
        
    }
    
    /**
     * Wraps a {@link Future} that holds a {@link List} back into {@link List}.
     * The list will not block computation of its results till its
     * accessed. Accessed meaning any of the list or collection methods being called
     * will ask the given future its value.
     * <p>
     * Uses commons list decorator but is not ideal since it 
     * does not implement generics and 
     * <a href="https://issues.apache.org/jira/browse/COLLECTIONS-352">
     * has some other problems.
     * </a>
     * 
     * @author adamgent
     *
     * @param <T> the type list holds.
     */
    public static class FutureList<T> extends AbstractListDecorator {

        private Future<List<T>> futureList;

        public FutureList(Future<List<T>> futureList)
        {
            super();
            this.futureList = futureList;
        }

        @Override
        protected Collection<T> getCollection()
        {
            try
            {
                return futureList.get();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString()
        {
            return getList().toString();
        }


        @Override
        public Iterator<T> iterator()
        {
            return getCollection().iterator();
        }

    }
    
    
    /**
     * If <code>true</code> the regions will be concurrently assembled independently from
     * each other but the call to assemble the regions 
     * will be blocked till all the regions have assembled.
     * <p>
     * If <code>false</code> the regions will be assembled concurrently and the call
     * to assemble will be returned immediatly. The region results will be
     * wrapped so that when they accessed computation will block till the results are completed.
     *  
     * @return never <code>null</code> default is <code>false</code>
     * @see FutureList
     */
    public boolean isWaitTillFinished()
    {
        return waitTillFinished;
    }


    public void setWaitTillFinished(boolean waitTillComplete)
    {
        this.waitTillFinished = waitTillComplete;
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSConcurrentRegionsAssembler.class);

}

