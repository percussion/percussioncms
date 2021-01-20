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

package com.percussion.server.job;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Handles all client requests for Job operations including Export,
 * Import, and Backup jobs.  Also handles requests for the current status of a
 * job.
 */
public class PSJobHandler implements IPSLoadableRequestHandler, IPSJobListener
{
   /**
    * Initializes the request methods and loads the job runner definitions.  
    * The <code>cfgFileIn</code> param should provide valid XML used to 
    * construct a {@link PSJobHandlerConfiguration} object.
    * 
    * See {@link IPSLoadableRequestHandler} class for more info on parameters 
    * and exceptions.
    * 
    * @throws PSServerException if <code>InputStream</code> is <code>null</code>
    * or contains invalid content.
    */
   public void init(Collection requestRoots, InputStream cfgFileIn) 
      throws PSServerException
   {
      if (requestRoots == null || requestRoots.size() == 0)
         throw new IllegalArgumentException(
            "must provide at least one request root" );

      // Must not be null in this instance. Not a runtime exception:
      if(cfgFileIn == null)
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_CONFIGURATION_FILE_IS_NULL,
            getName());

      PSConsole.printMsg(HANDLER, "Initializing Job Handler");
            
      // set members request roots:
      m_requestRoots = requestRoots;

      try
      {
         // load the configuration file:
         Document configDoc =
            PSXmlDocumentBuilder.createXmlDocument( cfgFileIn, false );

         m_config = new PSJobHandlerConfiguration(configDoc);
         m_factory = new PSJobRunnerFactory(m_config);
      }
      catch(SAXException e)
      {
         Object[] args = {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_CONFIGURATION_FILE_IS_INVALID, 
               args);
      }
      catch(IOException e)
      {
         Object[] args = {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_UNEXPECTED_EXCEPTION, args);
      }
      catch(PSUnknownDocTypeException e)
      {
         Object[] args = {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_UNEXPECTED_EXCEPTION, args);
      }
      catch(PSUnknownNodeTypeException e)
      {
         Object[] args = {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_UNEXPECTED_EXCEPTION, args);
      }

   }

   /**
    * Process the request using the input context information and data.
    * Determines which action to take and calls the appropriate request method.
    * All request methods return an XML Document, which is used to set the
    * headers and content of the response. Sends the response back to the
    * requestor.  An XML document is returned as the body of the response, 
    * either the <code>Document</code> returned from the request method, or the 
    * XML representation of a {@link PSJobException} if an error occurred.  If 
    * an error is returned, the status code of the response is set to 
    * <code>500</code>.
    * 
    * @param request The request object containing all context data associated 
    * with the request, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>request</code> is 
    * <code>null</code>.
    */
   public void processRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      Document respDoc = null;
      
      String reqType = request.getCgiVariable(
         IPSCgiVariables.CGI_PS_REQUEST_TYPE);
         
      String subReqType;
      try 
      {
         if (reqType == null || !reqType.startsWith("job-"))
         {
            throw new PSJobException(
               IPSJobErrors.INVALID_REQUEST_TYPE, reqType == null ? "" : 
                  reqType);
         }
         else
         {
            subReqType = reqType.substring("job-".length());
         }
       
         Document inDoc = request.getInputDocument();
         if (inDoc == null)
            throw new PSJobException(IPSJobErrors.NULL_INPUT_DOC);
      
         
         // all requests have security checked by default - must have admin
         // access
         PSServer.checkAccessLevel(request, 
            PSAclEntry.SACE_ADMINISTER_SERVER);
            
         if(subReqType.equals("runJob"))
            respDoc = runJob(inDoc, request);
         else if(subReqType.equals("cancelJob"))
            respDoc = cancelJob(inDoc, request);
         else if(subReqType.equals("getJobStatus"))
            respDoc = getJobStatus(inDoc, request);
         else
         {
            throw new PSJobException(
               IPSJobErrors.INVALID_REQUEST_TYPE, reqType);
         }
      }
      catch (Exception e) 
      {
         // Convert to xml response
         PSJobException je = null;
         if (e instanceof PSJobException)
            je = (PSJobException)e;
         else if (e instanceof PSException)
            je = new PSJobException((PSException)e);
         else
         {
            je = new PSJobException(IPSJobErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
         }
               
         respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element respEl = je.toXml(respDoc);
         PSXmlDocumentBuilder.replaceRoot(respDoc, respEl);
         request.getResponse().setStatus(500);
      }
      
      
      PSResponse resp = request.getResponse();
      if (respDoc != null)
         resp.setContent(respDoc);
   }

   /**
    * Runs the specified Job.  Creates the Job from the type using the
    * {@link PSJobRunnerFactory} to create the correct type of job runner.  Job 
    * is run in it's own thread, and a response is returned as soon 
    * as it is started.  To check on the status of a running Job, use 
    * <code>getStatus()</code>. Running a job will lock the entire 
    * <code>PSJobHandler</code>, so that only one job will be able to run at a 
    * time.
    * 
    * @param inDoc The document containing the job descriptor.  Format of this 
    * document is defined by the type of job being run, and the contents are 
    * solely interpreted by the class derived from {@link PSJobRunner}.  May
    * not be <code>null</code>.
    * @param req The request context, never <code>null</code>.  Must specify the 
    * category and job type using the <code>sys_jobCategory</code> and 
    * <code>sys_jobType</code> html parameters respectively.
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXJobRunResponse EMPTY>
    * &lt;!ATTLIST PSXJobRunResponse
    *    jobId CDATA #REQUIRED
    * > 
    * </code></pre>
    * 
    * Never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException if the user cannot be 
    * authenticated.
    * @throws PSAuthorizationException If the user is not authorized to
    * run the job.
    * @throws PSJobException If there are any other errors.
    */
   public Document runJob(Document inDoc, PSRequest req) 
      throws PSAuthenticationFailedException, PSAuthorizationException, 
         PSJobException
   {
      if (inDoc == null)
         throw new IllegalArgumentException("inDoc may not be null");
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      String category = req.getParameter("sys_jobCategory");
      if (category == null || category.trim().length() == 0)
      {
         Object[] args = {"sys_jobCategory", category == null ? "null" : 
            category};
         throw new PSJobException(IPSJobErrors.SERVER_REQUEST_PARAM_INVALID, 
            args);
      }
      
      String jobType = req.getParameter("sys_jobType");
      if (jobType == null || jobType.trim().length() == 0)
      {
         Object[] args = {"sys_jobType", jobType == null ? "null" : jobType};
         throw new PSJobException(IPSJobErrors.SERVER_REQUEST_PARAM_INVALID, 
            args);
      }
      
      // create the job
      Properties params = m_config.getJobInitParams(category, jobType);
      PSJobRunner runner = m_factory.getJobRunner(category, jobType);

      // see if we are locked
      lockJobHandler(runner);
      
      // init the job      
      int jobId = m_nextJobId++;
      runner.init(jobId, inDoc, req, params);
      
      // add self as listener
      runner.addJobListener(this);
      
      // start and store it
      runner.start();
      m_jobRunners.put(new Integer(jobId), runner);

      // prepare response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc, 
         "PSXJobRunResponse");
      root.setAttribute("jobId", String.valueOf(jobId));

      return respDoc;
   }

   /**
    * Checks the status of the specified job. Used by the server when
    * polled for the status of a currently running job.  The result contains
    * a value between <code>1-100</code> to indicate the % done and a message.
    * <code>100</code> indicates that the job has completed successfully.  If
    * the job has terminated abnormally, <code>-1</code> is returned and
    * errortext will be included in the response document.
    * 
    * @param doc the XML document containing the request data (the Job Id).  
    * May not be <code>null</code>.
    * 
    * Format is:
    * <pre><code>
    * &lt;!--
    * PSXJobGetStatus contains the job id.
    * >
    * &lt;!ELEMENT PSXJobGetStatus EMPTY>
    * &lt;!ATTLIST PSXJobGetStatus 
    *    id CDATA #REQUIRED
    * >
    * 
    * @param req the request context, may not be <code>null</code>.
    * 
    * @return the XML response document containing a PSXJobGetStatusResponse 
    * node that contains the status.  Format is:
    * 
    * &lt;!ELEMENT PSXJobGetStatusResponse EMPTY>
    * &lt;!ATTLIST PSXJobGetStatusResponse 
    *    status CDATA #REQUIRED
    *    message CDATA #REQUIRED
    * >
    * </code></pre>
    * 
    * @throws IllegalArgumentException if either param is invalid.
    * @throws PSJobException if there are any errors.
    */
   public Document getJobStatus(Document doc, PSRequest req) 
      throws PSJobException  
   {
      if (doc == null)
         throw new IllegalArgumentException("inDoc may not be null");
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Element reqRoot = doc.getDocumentElement();
      String id = reqRoot.getAttribute("id");
      Integer jobId;
      try 
      {
         jobId = new Integer(id);
      }
      catch (NumberFormatException e) 
      {
         Object[] msgArgs = {reqRoot.getTagName(), "id", id};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);
         
         Object[] args = {reqRoot.getTagName(), une.getLocalizedMessage()};
         throw new PSJobException(
            IPSJobErrors.SERVER_REQUEST_MALFORMED, args);
      }
      
      PSJobRunner job = (PSJobRunner)m_jobRunners.get(jobId);
      if (job == null)
         throw new PSJobException(IPSJobErrors.INVALID_JOB_ID, 
            jobId.toString());
            
      // prepare response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element respRoot = PSXmlDocumentBuilder.createRoot(respDoc, 
         "PSXJobGetStatusResponse");
      respRoot.setAttribute("status", String.valueOf(job.getStatus()));
      respRoot.setAttribute("message", job.getStatusMessage());

      return respDoc;
      
   }

   /**
    * Attempts to stop the currently running job.  Since job is running in its
    * own thread, it may complete on its own before noticing that it has been
    * requested to stop.  Returns one of 3 possible result codes:<br>
    * 1 - Job cancelled successfully<br>
    * 2 - Job completed before cancelled<br>
    * 3 - Job aborted<br>
    * 
    * @param doc the XML document containing the request data (the Job Id).  May 
    * not be <code>null</code>.  Format is <br><br>
    * 
    * &lt;!--<br>
    * PSXJobCancel contains the job id.<br>
    * --&gt;<br>
    * &lt;!ELEMENT PSXJobCancel EMPTY&gt;<br>
    * &lt;!ATTLIST PSXJobCancel id CDATA #REQUIRED&gt;<br>
    * 
    * @param req the request context, may not be <code>null</code>.
    * 
    * @return the XML response document containing a PSXJobCancelResponse node
    * that contains the result.  Format is:<br><br>
    * 
    * &lt;!ELEMENT PSXJobCancelResponse EMPTY&gt;<br>
    * &lt;!ATTLIST PSXJobCancelResponse resultCode CDATA #REQUIRED&gt;<br>
    * 
    * @throws IllegalArgumentException if either param is invalid.
    * @throws PSJobException if there are any errors.
    */
   public Document cancelJob(Document doc, PSRequest req) throws PSJobException
   {
      if (doc == null)
         throw new IllegalArgumentException("inDoc may not be null");
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Element reqRoot = doc.getDocumentElement();
      String id = reqRoot.getAttribute("id");
      Integer jobId;
      try 
      {
         jobId = new Integer(id);
      }
      catch (NumberFormatException e) 
      {
         Object[] msgArgs = {reqRoot.getTagName(), "id", id};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);
         
         Object[] args = {reqRoot.getTagName(), une.getLocalizedMessage()};
         throw new PSJobException(
            IPSJobErrors.SERVER_REQUEST_MALFORMED, args);
      }
      
      PSJobRunner job = (PSJobRunner)m_jobRunners.get(jobId);
      if (job == null)
         throw new PSJobException(IPSJobErrors.INVALID_JOB_ID, 
            jobId.toString());
            
      int resultCode = doCancelJob(job);
      
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element respRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXJobCancelResponse");
      respRoot.setAttribute("resultCode", Integer.toString(resultCode));
      
      return respDoc;
   }

   /**
    * Shutdown the request handler, first attempting to cancel any
    * running jobs.
    */
   public void shutdown()
   {
      PSConsole.printMsg(HANDLER, "Shutting down Job Handler");
      PSJobRunner curJob = unlockJobHandler();
      if (curJob != null)
         doCancelJob(curJob);
   }

   /**
    * Called by job when job completes.  Removes self as job listener on job, 
    * and frees this handler to process another job.
    * 
    * @param jobId Id of the job that has completed.
    */
   public void jobCompleted(long jobId)
   {
      // remove listener
      PSJobRunner job = (PSJobRunner)m_jobRunners.get(new Integer((int) jobId));
      if (job != null)
         job.removeJobListener(this);

      // unlock handler
      unlockJobHandler(job);
   }

   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }
   
   
   /**
    * Attempts to cancel a job.
    * 
    * @param job The the job to cancel, assumed not <code>null</code>.
    * 
    * @return An int code representing the result of the cancel attempt.  For
    * more information, see {@link #cancelJob(Document, PSRequest)}
    */
   private int doCancelJob(PSJobRunner job)
   {
      int resultCode = 3;
      
      // cancel the job
      job.cancelJob();

      // now wait till it's completed
      while(!job.isCompleted())
      {
         if (!job.isAlive())
         {
            // job thread has stopped is not completed or aborted?
            break;
         }

         try
         {
            // sleep before we check again
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
            if (job.isCompleted())
               break;
         }
      }

      if (job.isCompleted())
      {
         // we've stopped it, see if it is aborted, cancelled, or finished.
         if (job.getStatus() == -1)
            resultCode = 3;
         else if (job.getStatus() < 100)
            resultCode = 1;
         else
            resultCode = 2;
      }

      // remove self as listener
      job.removeJobListener(this);
      
      // unlock the handler      
      unlockJobHandler(job);
      
      return resultCode;
   }
   
   /**
    * Locks this handler with the provided job. Only one job may run at a time.
    * 
    * @param job The job that will be run, assumed not <code>null</code>.
    * 
    * @throws PSJobException if the handler is already locked.
    */
   private void lockJobHandler(PSJobRunner job) throws PSJobException
   {
      synchronized(m_jobMonitor)
      {
         if (m_currentJob != null && m_currentJob.isAlive())
            throw new PSJobException(IPSJobErrors.JOB_ALREADY_RUNNING);
         else
            m_currentJob = job;
      }
   }
   
   /**
    * Unlocks the handler using the supplied job.  Handler is only unlocked if
    * the supplied job is the current job locking the handler.
    * 
    * @param job The job, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the handler is unlocked, <code>false</code>
    * otherwise.
    */
   private boolean unlockJobHandler(PSJobRunner job)
   {
      boolean result = false;
      
      synchronized(m_jobMonitor)
      {
         if (m_currentJob == job)
         {
            m_currentJob = null;
            result = true;
         }
      }
      
      return result;
   }
   
   /**
    * Unlocks the handler.  
    * 
    * @return The job that was locking the handler, or <code>null</code> if the
    * handler is not locked.
    */
   private PSJobRunner unlockJobHandler()
   {
      PSJobRunner locker = null;
      
      synchronized(m_jobMonitor)
      {
         locker = m_currentJob;
         m_currentJob = null;
      }
      
      return locker;
   }
   
   
   /**
    * Name of this handler.
    */
   private static final String HANDLER = "JobHandler";

   /**
    * Storage for the request roots, initialized in <code>init()</code>, never
    * <code>null</code>, empty or modified after that. A list of 
    * <code>String</code> objects.
    */
   private Collection m_requestRoots = null;

   /**
    * Job handler config used by this handler, intialized by file provided by
    * <code>init()</code> method, never <code>null</code> or modified after 
    * that.
    */
   private PSJobHandlerConfiguration m_config;
   
   /**
    * The currently running job.  Since only one job may be run a time, this
    * also acts as a lock object.
    * Not <code>null</code> while a job is running.
    */
   private PSJobRunner m_currentJob = null;
   
   /**
    * Monitor object for synchronizing access to {@link #m_currentJob}
    */
   private Object m_jobMonitor = new Object();
   
   /**
    * Map of all jobs run by this handler.  Jobs are added when they are 
    * started. Once jobs are completed, they remain in this map indefinitely as 
    * status may be requested at any time.  Key is the job id as an 
    * <code>Integer</code>, value is an instance of a <code>PSJobRunner</code>.  
    * Never <code>null</code>.
    */
   private Map m_jobRunners = new HashMap();
   
   /**
    * The next job id to use, incremented each time a job is started by a call
    * to <code>runJob()</code>.
    */
   private int m_nextJobId = 1;
   
   /**
    * The factory used to run jobs.  Initialized during the <code>init()</code>
    * method, never <code>null</code> or modified after that.
    */
   private PSJobRunnerFactory m_factory;
}
