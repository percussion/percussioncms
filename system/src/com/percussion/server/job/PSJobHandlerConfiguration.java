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

package com.percussion.server.job;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides the required initParams for each class derived from PSJobRunner, as
 * well as the classname used to instantiate the job runner. The initParams are 
 * simply name-value pairs that one or more jobs may require at runtime.
 */
public class PSJobHandlerConfiguration
{

   /**
    * Construct this class from its XML representation.  
    * 
    * @param config The configuration document.  Format is:
    * <pre><code>
    *  &lt;!ELEMENT PSXJobHandlerConfiguration (InitParams, Categories)&gt;
    *  $lt;!--
    *    The init params for the handler.  These are added to any init params
    *    returned for a particular job type.
    *  -->
    *  &lt;!ELEMENT InitParams (InitParam*)&gt;
    *  $lt;!--
    *    name - The name of the param
    *    value - The value of the param
    *  -->
    *  &lt;!ELEMENT InitParam EMPTY&gt;
    *  &lt;!ATTLIST InitParam
    *     name CDATA #REQUIRED
    *     value CDATA #REQUIRED&gt;
    *  $lt;!--
    *    A list of categories, each containing init params and job types.  
    *    Used to group job runners and to provide a set of common init params 
    *    for all job runners in the category.  In the future may be used to
    *    implement handler locking at the category level.
    *  -->
    *  &lt;!ELEMENT Categories (Category*)&gt;
    *  $lt;!--
    *    Provides category-level init params.  These are added to any init 
    *    params returned for a particular job type within the category.
    *    
    *    name - the name of the category
    *  -->
    *  &lt;!ELEMENT Category (InitParams, Jobs)&gt;
    *  &lt;!ATTLIST Category
    *     name CDATA #REQUIRED&gt;
    *  $lt;!--
    *    A list of job types that belong to this category.
    *  -->
    *  &lt;!ELEMENT Jobs (Job*)&gt;
    *  $lt;!--
    *    A job type, defining the jobs init params and runner class.
    *    
    *    jobType - the name of the job type.
    *    className - the name of the class used to instantiate this type's
    *    job runner.
    *  -->
    *  &lt;!ELEMENT Job (InitParams)&gt;
    *  &lt;!ATTLIST Job
    *     jobType CDATA #REQUIRED
    *     className CDATA #REQUIRED
    * 
    * </code></pre>
    * 
    * @throws IllegalArgumentException if <code>config</code> is 
    * <code>null</code>.
    * @throws PSUnknownDocTypeException if the doc is not of the correct type
    * @throws PSUnknownNodeTypeException if a node is not of the correct type.
    */
   public PSJobHandlerConfiguration(Document config) 
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException  
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      Element root = config.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      //make sure we got the correct root node tag
      if (false == XML_NODE_NAME.equals(root.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(root);
      walker.setCurrent(root);

      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // get handler init params
      Element params = walker.getNextElement("InitParams", firstFlag);
      if (params == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, "InitParams");


      Properties handlerProps = loadInitParams(params, null);
      m_initParams.put(getContext(null, null), handlerProps);

      // load each category and the jobs etc.
      walker.setCurrent(root);
      Element categories = walker.getNextElement("Categories", firstFlag);
      if (categories == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, "Categories");

      Element cat = walker.getNextElement("Category", firstFlag);

      while(cat != null)
      {
         String catName = cat.getAttribute("name");
         if (catName == null || catName.trim().length() == 0)
         {
            Object[] args = {cat.getTagName(), "name", catName};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         String context = getContext(catName, null);

         // get the category init params combined with hander init params
         Properties catProps = loadInitParams(cat, handlerProps);
         // store them
         m_initParams.put(context, catProps);

         // load the jobs for this category
         Element jobs = walker.getNextElement("Jobs", firstFlag);
         if (jobs == null)
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, "Jobs");

         Element job = walker.getNextElement("Job", firstFlag);
         while (job != null)
         {
            // get the job info
            String jobType = job.getAttribute("jobType");
            if (jobType == null || jobType.trim().length() == 0)
            {
               Object[] args = {job.getTagName(), "jobType", jobType};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            
            String className = job.getAttribute("className");
            if (className == null || className.trim().length() == 0)
            {
               Object[] args = {job.getTagName(), "className", className};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            
            String jobCtx = getContext(catName, jobType);
            m_jobs.put(jobCtx, className);

            // get the job's initparams combined with category init params
            Properties jobProps = loadInitParams(job, catProps);
            // store them
            m_initParams.put(jobCtx, jobProps);

            // look for the next job
            job = walker.getNextElement("Job", nextFlag);
         }

         // look for the next category
         walker.setCurrent(cat);
         cat = walker.getNextElement("Category", nextFlag);
      }


   }

   /**
    * Returns the name of the class to instantiate for this job type.
    * 
    * @param cat The category of this job.  May not be <code>null</code> or 
    * empty, and must be an existing category.
    * @param jobType A string which identifies a single job type within a 
    * category.  May not be <code>null</code> or empty, and must be am
    * existing job type within the specified category.
    * 
    * @return The class name.  Never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJobException if The job definition cannot be located.
    */
   public String getJobClassName(String category, String jobType) 
      throws PSJobException
   {
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException(
            "category may not be null or empty");

      if (jobType == null || jobType.trim().length() == 0)
         throw new IllegalArgumentException(
            "jobType may not be null or empty");

      String className = (String)m_jobs.get(getContext(category, jobType));

      if (className == null)
      {
         Object[] args = {category, jobType};
         throw new PSJobException(IPSJobErrors.JOB_DEFINITION_NOT_FOUND, args);
      }

      return className;
   }

   /**
    * Returns the InitParams for the JobHandler.  These are params that are
    * available to all jobs as well.
    * 
    * @return The params, never <code>null</code>.
    */
   public Properties getHandlerInitParams()
   {
      Properties props = (Properties)m_initParams.get(getContext(null, null));

      return props;
   }

   /**
    * Returns the combined InitParams for the jobtype, category, and
    * the JobHandler.
    * 
    * @param category The category of this job.  May not be <code>null</code> or 
    * empty, and must be an existing category.
    * @param jobType A string which identifies a single job type within a 
    * category.  May not be <code>null</code> or empty, and must be am
    * existing job type within the specified category.
    * 
    * @return The params as a Properties object, never <code>null</code>, may
    * be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJobException if The job definition cannot be located.
    */
   public Properties getJobInitParams(String category, String jobType)
      throws PSJobException
   {
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException(
            "category may not be null or empty");
         
      if (jobType == null || jobType.trim().length() == 0)
         throw new IllegalArgumentException("jobType may not be null or empty");

      Properties props = (Properties)m_initParams.get(
         getContext(category, jobType));

      if (props == null)
      {
         Object[] args = {category, jobType};
         throw new PSJobException(IPSJobErrors.JOB_DEFINITION_NOT_FOUND, args);
      }

      return props;
   }

   
   /**
    * Retrieves any InitParams from within the supplied node and returns them
    * as a <code>Properties</code> object.
    * 
    * @param el The node to search within, may not be <code>null</code>.
    * @param defaults A set of properties to use as defaults.  May be
    * <code>null</code>.
    * 
    * @return The Properties object.  Never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if <code>el</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if a node is not of the correct type.
    */
   private Properties loadInitParams(Element el, Properties defaults)
      throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("el may not be null.");

      Properties props;
      if (defaults != null)
         props = new Properties(defaults);
      else
         props = new Properties();
         
      PSXmlTreeWalker walker = new PSXmlTreeWalker(el);
      walker.setCurrent(el);

      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element param = walker.getNextElement("InitParam", firstFlag);
      while (param != null)
      {
         String attName = param.getAttribute("name");
         if (attName == null || attName.trim().length() == 0)
         {
            Object[] args = {"InitParam", "name", attName};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         String attVal = param.getAttribute("value");
         if (attVal == null || attVal.trim().length() == 0)
         {
            Object[] args = {"InitParam", "value", attVal};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         props.setProperty(attName, attVal);
         param = walker.getNextElement("InitParam", nextFlag);
      }

      return props;
   }

   /**
    * Constructs the appropriate context for the given cat and jobtype.  Both
    * may be <code>null</code> to retrieve just the handler context, or jobType
    * may be <code>null</code> to retrieve a category context.  If jobType is 
    * not <code>null</code>, then category may not be <code>null</code>.
    * 
    * @param cat The category.  May be <code>null</code> if jobType is also
    * <code>null</code>.
    * @param jobType The job type.  May be <code>null</code>.
    * 
    * @return The context.
    * 
    * @throws IllegalArgumentException if <code>cat</code> is <code>null</code> 
    * and <code>jobType</code> is not <code>null</code>.
    */
   private String getContext(String cat, String jobType)
   {
      if ((cat == null) && (jobType != null))
         throw new IllegalArgumentException(
            "cat may not be null if jobType is not also null");

      String ctx = null;

      if (cat == null)
         ctx = "handler";
      else if (jobType == null)
         ctx = "handler/" + cat;
      else
         ctx = "handler/" + cat + "/" + jobType;

      return ctx;
   }

   /**
    * Name of the root XML node in this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXJobHandlerConfiguration";
   
   
   /**
    * Map of job class names.  Each entry is a string that contains the
    * class name for each job.  Keys are specified by a context of
    * "handler/<category>/<jobType>".  Never <code>null</code>, intialized
    * during ctor, never modified after that.
    */
   private HashMap m_jobs = new HashMap();

   /**
    * Map of initParams.  Each key is a context as a <code>String</code> and the 
    * value is a <code>Properties</code> object containing the params for the 
    * specified key.  Keys are specified by a context of
    * "handler/<category>/<job>".  Any portion of the context starting from the
    * left may be used as a key to store initParams for the handler as well as
    * the category and the jobs in that category.  Never <code>null</code>, 
    * initialized during ctor, never modified after that.
    */
   private HashMap m_initParams = new HashMap();
   
}
