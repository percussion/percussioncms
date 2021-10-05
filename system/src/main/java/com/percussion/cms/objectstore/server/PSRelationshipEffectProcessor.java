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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSAttemptResult;
import com.percussion.relationship.PSExecutionContext;
import com.percussion.relationship.PSTestResult;
import com.percussion.relationship.effect.PSEffectTestResultPair;
import com.percussion.relationship.effect.PSRelationshipEffectTestResult;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Core relationship effect processing is performed by this class. This class
 * is typically instantiated by any command handler or processor that needs 
 * processing relationship effects, for example, {@link com.percussion.cms.objectstore.server.PSRelationshipDbProcessor},
 * {@link com.percussion.cms.handlers.PSWorkflowCommandHandler}. This class is not thread safe and hence
 * each thread must instantiate a new object of this class.
 */
public class PSRelationshipEffectProcessor
{
   /**
    * Convenience ctor that calls
    * {@link #PSRelationshipEffectProcessor(PSRelationshipSet, PSExecutionData, int) 
    * this(null, data, executionContext)}.
    */
   public PSRelationshipEffectProcessor(
      PSExecutionData data,
      int executionContext)
   {
      this(null, data, executionContext);
   }

   /**
    * If the relationship set is supplied (non-<code>null</code>), its
    * contents will be used, otherwise, the processor assumes that a locator
    * for the item is available in the request associated with the execution
    * data and assumes the relationships to be processed are all relationships
    * that point to or from the item specified by the locator.
    * 
    * @param relationshipSet relationship set for processing the effects, may be
    * <code>null</code>.
    * @param data execution data, must not be <code>null</code>
    * @param executionContext must be one of the valid
    * {@link IPSExecutionContext} RS_XXXX values.
    * @throws IllegalArgumentException if any of the parameters is not a valid
    * one.
    */
   public PSRelationshipEffectProcessor(
      PSRelationshipSet relationshipSet,
      PSExecutionData data,
      int executionContext)
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");

      m_execData = data;
      m_executionContext = new PSExecutionContext(executionContext, data, 
            m_relationshipsProcessed);
      m_relSetToProcess = relationshipSet;
      m_userRequest = data.getRequest();
      m_serverRequest = m_userRequest.getServerRequest();
   }

   /**
    * This method does perform the actual processing of the effects. The
    * caller must call this method after instantiation. Effect processing
    * involves the following steps:
    * <p>
    * <ol>
    * <li>Depending on the version of the constructor of this class execute
    * all test() methods of all effects. Testing of effects continues even
    * if a test returns error while processing a relationship to process all
    * the linked relationships depending on the direction of navigation.</li>
    * <li>Check all test results to see if there is one or more errors
    * returned by the test() methods of the effects. If there is one, throw
    * an exception with returned error</li>
    * <li>Run attempt() methods on all required relationships</li>
    * <li>Run all recover methods for the effects for which the attempt()
    * methods have been processed till then, in case of an error processing the
    * attempt() methods</li>
    * </ol>
    * @throws PSNotFoundException
    * @throws PSExtensionException
    * @throws PSIllegalArgumentException
    * @throws PSDataExtractionException
    * @throws PSExtensionProcessingException
    * @throws PSParameterMismatchException
    * @throws PSCmsException
    * <p>
    * @todo If there is an error running the test() methods of the effects
    * then we need to generate a detailed error report consisting of all
    * errors with details of the relationship, owner, dependent and effect
    * being processed  and present somehow to teh end user requested the
    * operation.
    */
   public void process()
      throws
         PSNotFoundException,
         PSExtensionException,
         PSIllegalArgumentException,
         PSDataExtractionException,
         PSExtensionProcessingException,
         PSParameterMismatchException,
         PSCmsException
   {
      //Content Item to process is known
      if (m_relSetToProcess == null)
      {
         PSRequest request = m_execData.getRequest();
         String contentid =
            request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
         PSLocator locator = new PSLocator(contentid, revision);

         runTests(locator);
      }
      //If relationship set to process was supplied process only those
      else
      {
         for (Object mRelSetToProcess : m_relSetToProcess) {
            //We always use the owner as the activation end point (I think
            //this is correct?)
            runTests((PSRelationship) mRelSetToProcess, true);
         }
      }
      PSException ex = isAllTestsSuccess();
      if (ex != null)
      {
         /**
          * @todo generate detailed error report.
          */
         throw new PSExtensionProcessingException(
            ex.getLanguageString(),
            ex.getErrorCode(),
            ex.getErrorArguments());
      }

      /**
       * Run attempt method on all the relationships processed in test() methods
       */
      PSAttemptResult result = runAttempts();
      if (result != null)
      {
         /*
          * An attempt() method is failed, run recover methods and throw
          * exception.
          */
         runRecovers(result);

         throw new PSExtensionProcessingException(
            result.getException().getLanguageString(),
            result.getException().getErrorCode(),
            result.getException().getErrorArguments());
      }
   }

   /**
    * Method to get the list of relationship that this item is associated with
    * (item is either owner or dependent of the relationship).
    * <p>
    * Note, the use owner revision property of the relationship configuration
    * will be considered when retrieving the relationships. If the property is 
    * <code>true</code>, then the retreived relationships will only contain one 
    * owner revision per owner and dependent pair. 
    * 
    * @param locator the locator of the item for which the relationships are 
    *    being queried, assumed not <code>null</code>.
    * 
    * @return an iterator of all relationships (pointing onwards and outwards 
    *    of the item specified by the locator), never <code>null</code>, 
    *    may be empty.
    * 
    * @exception PSCmsException if an error occurs.
    */
   private Iterator getAllRelationships(PSLocator locator) throws PSCmsException
   {
      Collection<PSRelationshipConfig> configs = getReleventRelationships();
      if (configs.isEmpty())
         return Collections.emptyIterator();
      
      Set<PSRelationship> relationships = new HashSet<>();

      Collection<String> allConfigNames = new HashSet<>();
      for (PSRelationshipConfig config : configs)
         allConfigNames.add(config.getName());
      
      PSRelationshipFilter filter = new PSRelationshipFilter();

      /*
       * Get all relationships with this items contentid and current revision 
       * as the owner.
       */
      filter.setNames(allConfigNames);
      filter.setOwner(locator);
      filter.limitToOwnerRevision(true);
      PSRelationshipProcessor relProc = PSRelationshipProcessor.getInstance();
      PSRelationshipSet set = relProc.getRelationships(filter);
      relationships.addAll(set);
      
      /*
       * Get all relationships with this items contentid as the owner, 
       * ignoring the revision.
       */
      filter.limitToOwnerRevision(false);
      Collection<String> ignoreOwnerRevConfigNames = 
         getConfigsWhichIgnoreOwnerRevision(configs);
      if (!ignoreOwnerRevConfigNames.isEmpty())
      {
         filter.setNames(ignoreOwnerRevConfigNames);
         set = relProc.getRelationships(filter);
         relationships.addAll(set);
      }
      
      /*
       * Get all relationships with this items (as the dependent) and 
       * its owner's Edit or Current Revision. 
       */
      filter = new PSRelationshipFilter();
      filter.setNames(allConfigNames);
      filter.setDependent(locator);
      filter.limitToEditOrCurrentOwnerRevision(true);
      set = relProc.getRelationships(filter);
      relationships.addAll(set);

      /**
       * Get all relationships with this items (as the dependent) and 
       * its owner's Public Revision. 
       */
      filter = new PSRelationshipFilter();
      filter.setNames(allConfigNames);
      filter.setDependent(locator);
      filter.limitToPublicOwnerRevision(true);
      set = relProc.getRelationships(filter);
      relationships.addAll(set);
      
      /*
       * Get all relationships with this items contentid as the dependent, 
       * ignoring the owner revision.
       */
      if (!ignoreOwnerRevConfigNames.isEmpty())
      {
         filter = new PSRelationshipFilter();
         filter.setNames(ignoreOwnerRevConfigNames);
         filter.setDependent(locator);
         
         set = relProc.getRelationships(filter);
         relationships.addAll(set);
      }
      
      return relationships.iterator();
   }
   
   /**
    * Get a list of relationships, where the effects of the relationships
    * are interested in the current Execution Context.
    * 
    * @return a list of relationships. It may be empty, never 
    *   <code>null</code>.
    */
   private Collection<PSRelationshipConfig> getReleventRelationships()
   {
      int exeCtx = m_executionContext.getContextType();
      HashSet<PSRelationshipConfig> retConfigs = new HashSet<>();
      Iterator configs = PSRelationshipCommandHandler
            .getConfigurationSet().iterator();
      PSRelationshipConfig config;
      Iterator cdEffects;
      PSConditionalEffect cdEffect;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         cdEffects = config.getEffects();
         while (cdEffects.hasNext())
         {
            cdEffect = (PSConditionalEffect) cdEffects.next();
            if (cdEffect.hasExecutionContext(exeCtx))
               retConfigs.add(config);
         }
      }
      
      return retConfigs;
   }
   
   /**
    * Get a list of relationship configuration that either ignore the owner
    * or the dependent revision.
    * 
    * @param configs the relationship configurations that will be selected from.
    *    Assumed not <code>null</code>.
    * @return the requested relationship configuration which ignore the owner 
    *    or ependent revision, never <code>null</code>, may be empty.
    */
   private Collection<String> getConfigsWhichIgnoreOwnerRevision( 
      Collection<PSRelationshipConfig> configs)
   {
      Collection<String> results = new ArrayList<>();
      
      for (PSRelationshipConfig config : configs)
      {
         if (!config.useOwnerRevision()) 
         {
            results.add(config.getName());
         }
      }
      
      return results;
   }
   
   /**
    * Checl whether to process this effector not based who (owner or
    * dependent) of the relationship is being processed and its activation
    * endpoint.
    * @param isOwner <code>true</code> if the item being processed is the
    * owner of the current relationship and <code>false</code> if is dependent.
    * @param activationEndPoint must be a valid activation end point from the
    * relationship configuration, i.e. one of teh {link PSRelationshipConfig}
    * ACTIVATION_ENDPOINT_XXXX strings. Assumed not <code>null</code>.
    * @return
    */
   private boolean isProcessThisEffect(
      boolean isOwner,
      String activationEndPoint)
   {
      boolean result = false;
      if (activationEndPoint
         .equals(PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER))
      {
         result = true;
      }
      else if (
         activationEndPoint.equals(
            PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER)
            && isOwner)
      {
         result = true;
      }
      else if (
         activationEndPoint.equals(
            PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT)
            && !isOwner)
      {
         result = true;
      }
      return result;
   }

   /**
    * Run all test() methods of all relationships for a given item locator.
    * Method first gets all relationshiops associated with this item and
    * processes each relationship by calling {@link #runTests(PSRelationship, boolean)}
    * method.
    * @param firstEnd the locator of the item for the relationships are to be
    * processed, assumed not <code>null</code>.
    * @throws PSNotFoundException
    * @throws PSExtensionException
    * @throws PSIllegalArgumentException
    * @throws PSDataExtractionException
    * @throws PSExtensionProcessingException
    * @throws PSParameterMismatchException
    * @throws PSCmsException
    */
   private void runTests(PSLocator firstEnd)
      throws
         PSNotFoundException,
         PSExtensionException,
         PSIllegalArgumentException,
         PSDataExtractionException,
         PSExtensionProcessingException,
         PSParameterMismatchException,
         PSCmsException
   {
      Iterator relationships = getAllRelationships(firstEnd);
      while (relationships.hasNext())
      {
         PSRelationship currentRel = (PSRelationship) relationships.next();
         runTests(currentRel, isOwner(currentRel, firstEnd));
      }
   }

   /**
    * Runs all test() methods of all effects on the given relationships.
    * The followings is the scheme followed while processing all relationships
    * and the efefcts attached to them.
    * <p>
    * We keep track of all relationships processed in the local map (member
    * variable {@link #m_relationshipsProcessed}) to facilitate duplicate
    * processing of relationships. Each relationship is unique and we never need
    * to process a relationship more than once during the entire recursive
    * processing.
    * <p>
    * Get all effects attached to the relationship and process each of them to
    * run the test() method. We run all the effects without worrying about the
    * result of the run.
    * <p>
    * Add the relationship to the processed map.
    * <p>
    * We parse through all effect test results to see if any of those says to
    * recurse dependents. If so we call the {@link #runTests(PSLocator)} method
    * for the  owner or dependent end of the relationship depending the
    * activation end point set for the the relationship via configuration.
    * <p>
    * As can be seen from the above algorithm we process each relationship tree
    * before we go to the next relationship for a particular item. At the end of
    * the process we will end up with process web or graph. If test method
    * returns error for one or more efects in the web we will be able to produce
    * a detailed error report pointing out the actual relationships and the
    * locators that caused the test() to fail.
    * <p>
    * @param currentRel the relationship object to for which this effects are
    * to be processed, assumed not <code>null</code>.
    * @param isOwner flag indicating if owner end or dependent end of the
    * relationship needs to be processed. <code>true</code> if the owner has to
    * be processed and <code>false</code> if the dependent end has to processed.
    * @throws PSNotFoundException
    * @throws PSExtensionException
    * @throws PSIllegalArgumentException
    * @throws PSDataExtractionException
    * @throws PSExtensionProcessingException
    * @throws PSParameterMismatchException
    * @throws PSCmsException
    */
   private void runTests(PSRelationship currentRel, boolean isOwner)
      throws
         PSNotFoundException,
         PSExtensionException,
         PSIllegalArgumentException,
         PSDataExtractionException,
         PSExtensionProcessingException,
         PSParameterMismatchException,
         PSCmsException
   {
      IPSExtensionManager manager = PSServer.getExtensionManager(null);
      PSExtensionRunner runner = null;
      PSTestResult result = null;
      if (m_relationshipsProcessed
         .containsKey(currentRel.getId()))
      {
         /** @todo: Relationship already processed. What to do??? */
         return;
      }
      try
      {
         if (currentRel.getConfig().useServerId())
            m_execData.setRequest(m_serverRequest);
         else
            m_execData.setRequest(m_userRequest);
            
         m_execData.setCurrentRelationship(currentRel);

         PSRelationship sourceRel = null;
         if (m_executionContext.isPreUpdate())
         {
              log.debug("pre_update setting source relationship to pre modified");
              sourceRel = getSourceRel(currentRel);
              if(sourceRel != null)
                  log.debug("source owner is {} dest owner is {} ", sourceRel.getOwner().getId(), currentRel.getOwner().getId());
         }
         m_execData.setSourceRelationship(sourceRel);

         Iterator effects = currentRel.getConfig().getEffects();
         PSRelationshipEffectTestResult relEffectResults =
            new PSRelationshipEffectTestResult(currentRel);
         
         while (effects.hasNext())
         {
            PSConditionalEffect effect = (PSConditionalEffect) effects.next();
            String activationEndPoint = effect.getActivationEndPoint();
            boolean processThisEffect =
               isProcessThisEffect(isOwner, activationEndPoint);
            if (!processThisEffect)
               continue;
            PSRuleListEvaluator evaluator =
               new PSRuleListEvaluator(effect.getConditions());
            processThisEffect = evaluator.isMatch(m_execData);
            if (!processThisEffect)
               continue;
            m_executionContext.setActivationEndPoint(isOwner);
            PSExtensionCall call = effect.getEffect();
            IPSExtension extension =
               manager.prepareExtension(call.getExtensionRef(), null);
            if (extension instanceof IPSEffect)
            {
               runner = PSExtensionRunner.createRunner(call, extension);
               result =
                  (PSTestResult) runner.testEffect(
                     m_execData,
                     m_executionContext);
   
               result.setActivationEndPoint(isOwner);
   
               relEffectResults.add(effect, result);
            }
         }
         //Add to the map of processed relationships
         m_relationshipsProcessed.put(
                 relEffectResults.getRelationship().getId(),
            relEffectResults);
   
         /**
          * @todo: do we test for success or failure here and stop recursing
          * through dependents in case of failure or walk entire web and then
          * report?
          */
   
         Iterator iter = relEffectResults.getResults();
         while (iter.hasNext())
         {
            PSEffectTestResultPair effectResult =
               (PSEffectTestResultPair) iter.next();
            if (!effectResult.getResult().getRecurseDependents())
               continue;
            PSRelationship rel = relEffectResults.getRelationship();
            PSLocator next = null;
            if (isOwner)
               next = rel.getDependent();
            else
               next = rel.getOwner();
            runTests(next);
            break;
         }
      }
      finally
      {
         m_execData.setRequest(m_userRequest);
      }
   }

   /**
    * This method executes attempt() methods of all effects of all relationships
    * (that are from {@link #m_relationshipsProcessed})that have been processed
    * for test() methods. The algorithm is as described below:
    * For each of the processed relationship in the processed relationships,
    * <ol>
    * <li>get the effect and test result pair</li>
    * <li>ignore if the result has a warning</li>
    * <li>process the attempt() method of the effect otherwise</li>
    * <li>check if the attempt() result is success and throw exception in case
    * of error</li>
    * </ol>
    * @return <code>null</code> if attempt processing is successful, otherwise
    * return the failed result.
    */
   private PSAttemptResult runAttempts()
      throws
         PSNotFoundException,
         PSExtensionException,
         PSIllegalArgumentException,
         PSDataExtractionException,
         PSExtensionProcessingException,
         PSParameterMismatchException,
         PSCmsException
   {
      IPSExtensionManager manager = PSServer.getExtensionManager(null);
      PSExtensionRunner runner = null;
      PSAttemptResult attemptResult = new PSAttemptResult();
      Iterator keys = m_relationshipsProcessed.keySet().iterator();
      PSRelationship relationship = null;
      PSRelationshipEffectTestResult result = null;

      try
      {
         while (keys.hasNext())
         {
            Integer key = (Integer) keys.next();
            result =
               (PSRelationshipEffectTestResult) m_relationshipsProcessed.get(key);

            relationship = result.getRelationship();

            if (relationship.getConfig().useServerId())
               m_execData.setRequest(m_serverRequest);
            else
               m_execData.setRequest(m_userRequest);

            m_execData.setCurrentRelationship(relationship);
            PSRelationship sourceRel = null;
            if (m_executionContext.isPreUpdate()) {
                log.debug("pre_update setting source relationship to pre modified");
                sourceRel = getSourceRel(relationship);
                if(sourceRel != null)
                  log.debug("source owner is {} dest owner is {} ", sourceRel.getOwner().getId(), relationship.getOwner().getId());
            }
             m_execData.setSourceRelationship(sourceRel);

            
            Iterator iter = result.getResults();
            PSEffectTestResultPair pair = null;
            PSConditionalEffect effect = null;
            while (iter.hasNext())
            {
               pair = (PSEffectTestResultPair) iter.next();
               effect = pair.getEffect();
               if (pair.getResult().hasWarning())
                  continue;

               m_executionContext.setActivationEndPoint(
                  pair.getResult().isActivationEndPointOwner());

               PSExtensionCall call = effect.getEffect();
               IPSExtension extension =
                  manager.prepareExtension(call.getExtensionRef(), null);
               if (extension instanceof IPSEffect)
               {
                  runner = PSExtensionRunner.createRunner(call, extension);
                  attemptResult =
                     (PSAttemptResult) runner.attemptEffect(
                        m_execData,
                        m_executionContext);
                  if (!attemptResult.isSuccess())
                  {
                     return attemptResult;
                  }
               }
            }
         }
      }
      finally
      {
         m_execData.setRequest(m_userRequest);
      }
      return null;
   }

   /**
    * Supposed to run all recover method on all the effects that processed from
    * the processed relationships till we find the effect result pair matching
    * the failed attempt result's effect. This method is provided only for
    * framework completeness sake and doe not do anything now. It needs to be
    * implemented after server supports transaction.
    *
    */
   private void runRecovers(PSAttemptResult attemptResult)
   {
      // avoid eclipse warning
      if (attemptResult == null);
      
      //nothing for now. This could be a task for future when we server
      //supports transactions.
   }

   /**
    * Helper method that checks if test() method for all effects of the
    * relationships processed yielded an overall success.
    * @return PSException that was set by the failed effect nethod of a
    * relationship. Will be <code>null</code> if there was no error processing
    * the test() mthod of the effects.
    */
   private PSException isAllTestsSuccess()
   {
      Iterator keys = m_relationshipsProcessed.keySet().iterator();
      PSRelationshipEffectTestResult result = null;
      PSEffectTestResultPair effectResultPair = null;
      while (keys.hasNext())
      {
         Integer key = (Integer) keys.next();
         result =
            (PSRelationshipEffectTestResult) m_relationshipsProcessed.get(key);
         Iterator iter = result.getResults();
         while (iter.hasNext())
         {
            effectResultPair = (PSEffectTestResultPair) iter.next();
            if (!effectResultPair.getResult().isSuccess())
            {
               return effectResultPair.getResult().getException();
            }
         }
      }
      return null;
   }

   /**
    * Helper method to test if the supplied locator is on the owner end of the
    * supplied relationship.
    * @param relation relationship to test, assumed not <code>null</code>.
    * @param locator locator to if it is on th eowner side of the relationship,
    * assumed not <code>null</code>.
    * @return <code>true</code> if it is the owner, <code>false</code>
    * othwerwise (if it is on the dependent side of the relationship).
    */
   private boolean isOwner(PSRelationship relation, PSLocator locator)
   {
      if (relation == null)
         throw new IllegalArgumentException("relation must not be null");

      int id = locator.getId();
      int ownerId = relation.getOwner().getId();
      return id == ownerId;
   }

   /**
     * Helper method to get the current persisted version of the current
     * relationship
     * 
     * @param currentRel relationship to test, assumed not <code>null</code>.
     * @return PSRelationship retrived based upon relationship id or
     *         <code>null</code> if relationship id not set.
     */

    private PSRelationship getSourceRel(PSRelationship currentRel)
    {
        int relId = currentRel.getId();
        PSRelationship sourceRel = null;
        if (relId > 0)
        {
            IPSRelationshipService svc = PSRelationshipServiceLocator.getRelationshipService();
            try
            {
                sourceRel = svc.loadRelationship(currentRel.getId());
            }
            catch (PSException e)
            {
                log.error("Cannot get relationship with id= {} ", currentRel.getId());
                log.error("Error : {} ",e.getMessage());
                log.debug(e.getMessage(),e);
            }
            m_execData.setSourceRelationship(sourceRel);
        }
        return sourceRel;
    }
   
   /**
    * Map of all relationships processed by the engine. The key is the
    * relationship object. and the value is the {@link PSRelationshipEffectTestResult}
    * object that contains the test result data.
    */
   private Map m_relationshipsProcessed = new HashMap();

   /**
    * Reference to the execution data, set in the constructor,
    * never <code>null</code> after that.
    */
   private PSExecutionData m_execData = null;

   /**
    * Reference to the execution context, initialized in the constructor,
    * never <code>null</code> after that.
    */
   private PSExecutionContext m_executionContext = null;

   /**
    * Relationship set to process. Set by one of the constructors. If the object
    * is constructed not using that constructor, this is <code>null</code>.
    * never <code>null</code> after that.
    */
   private PSRelationshipSet m_relSetToProcess = null;

   /**
    * The request object that can be used to make requests on behalf of server. 
    * Built off of user request associated with the execution data. Initialized 
    * in the constructor, never <code>null</code> after that. 
    */
   private PSRequest m_serverRequest = null;

   /**
    * Reference to the request object associated with the execution data. 
    * Initialized  in the constructor, never <code>null</code> after that.  
    */
   private PSRequest m_userRequest = null;
   
   /**
    * The logger for this class.
    */
   private static final Logger log = LogManager.getLogger(PSRelationshipEffectProcessor.class);

}
