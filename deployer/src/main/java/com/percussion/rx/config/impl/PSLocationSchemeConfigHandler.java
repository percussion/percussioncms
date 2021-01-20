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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The handler for configuring Location Scheme objects.
 *
 * @author YuBingChen
 */
public class PSLocationSchemeConfigHandler extends PSObjectConfigHandler
{
   public PSLocationSchemeConfigHandler()
   {
      m_extraProperties.put(CONTEXTS, null);
      m_extraProperties.put(CONTENT_TYPE, null);
      m_extraProperties.put(TEMPLATE, null);
   }

   @Override
   public boolean equals(Object otherObj)
   {
      if ((!(otherObj instanceof PSLocationSchemeConfigHandler) || (!super
            .equals(otherObj))))
      {
         return false;
      }
      
      PSLocationSchemeConfigHandler other = (PSLocationSchemeConfigHandler) otherObj;
      return new EqualsBuilder().append(m_extraProperties, other.m_extraProperties)
            .isEquals();
   }
      
   @Override
   public int hashCode()
   {
      return super.hashCode()
            + new HashCodeBuilder().append(m_extraProperties).toHashCode();
   }
   
   @Override
   public List<PSConfigValidation> validate(IPSConfigHandler other)
   {
      List<PSPair<String, ObjectState>> commonNames = getCommonObjectNames(other);

      PSConfigValidation vError;
      List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
      for (PSPair<String, ObjectState> pair : commonNames)
      {
         String msg = " Context / Location Scheme pair \"" + pair.getFirst()
               + "\" is already configured.";
         vError = new PSConfigValidation(pair.getFirst(), null, true, msg);
         vError.setObjectType(getType());

         result.add(vError);
      }
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getType()
    */
   @Override
   public PSTypeEnum getType()
   {
      return PSTypeEnum.LOCATION_SCHEME;
   }

   /**
    * Sets the type enum of the design object wired by spring.
    */
   @Override
   public void setType(PSTypeEnum type)
   {
      if (!type.equals(PSTypeEnum.LOCATION_SCHEME))
         throw new IllegalArgumentException("type must be LOCATION_SCHEME.");
   }
   
   @Override
   public boolean process(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets)
   {
      if (m_isUnProcess)
         throw new IllegalStateException(
               "Cannot call process() after called unprocess().");
      
      if (obj == null)
         return false;
      
      return super.process(obj, state, aSets);
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public boolean unprocess(Object obj, List assocSets)
   {
      m_isUnProcess = true;
      super.unprocess(obj, assocSets);
      return true;
   }
   
   /**
    * Gets a list of Context names.
    * 
    * @return a list of Context names as a {@link List List&lt;String>} type or 
    * it may be a string that contains a ${place-holder}. It may be 
    * <code>null</code> if the {@link #setNames(Object)} has never been called
    * or wired by Spring framework.
    */
   public Object getContexts()
   {
      return m_extraProperties.get(CONTEXTS);
   }
   
   /**
    * Sets the Context names. 
    *  
    * @param names the names of the Contexts. It is either a 
    * {@link List List&lt;String>} type or a string that contains a 
    * ${place-holder}. It may not be <code>null</code> or empty.
    */
   public void setContexts(Object names)
   {
      if (names == null)
         throw new IllegalStateException("Context names may not be null.");
      
      m_extraProperties.put(CONTEXTS, names);
   }

   /**
    * Returns the Template name. This is an optional property for configuring
    * existing Location Scheme. However, it is a required property for 
    * configuring a new Location Scheme.
    * 
    * @return the Template name. It may be <code>null</code> if the Template
    * name is not defined.
    */
   public String getTemplate()
   {
      return (String) m_extraProperties.get(TEMPLATE);
   }

   /**
    * Sets the Template name.
    * @param name the new Template name. It may not be <code>null</code> or 
    * empty.
    */
   public void setTemplate(String name)
   {
      if (StringUtils.isBlank(name))
         throw new PSConfigException("Template name may not be null or empty.");
      
      m_extraProperties.put(TEMPLATE, name);
   }
   
   /**
    * Returns the Content Type name. This is an optional property for
    * configuring existing Location Scheme. However, it is a required property
    * for configuring a new Location Scheme.
    * 
    * @return the Content Type name. It may be <code>null</code> if the
    * Template name is not defined.
    */
   public String getContentType()
   {
      return (String) m_extraProperties.get(CONTENT_TYPE);
   }
   
   /**
    * Sets the Content Type name.
    * @param name the new Content Type name. It may not be <code>null</code> or 
    * empty.
    */
   public void setContentType(String name)
   {
      if (StringUtils.isBlank(name))
         throw new PSConfigException(
               "Content Type name may not be null or empty.");
      
      m_extraProperties.put(CONTENT_TYPE, name);
   }
   
   /*
    * //see base class method for details
    */
   @Override
   public List<PSPair<Object, ObjectState>> getDesignObjects(
         Map<String, Object> cachedObjs)
   {
      if (StringUtils.isBlank(getName()))
         throw new PSConfigException(
               "Location Scheme name may not be null or empty.");

      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      List<PSPair<Object, ObjectState>> result = new ArrayList<PSPair<Object, ObjectState>>();
      
      for (PSPair<String, ObjectState> sname : getObjectNames())
      {
         Object scheme = getLocationScheme(sname.getFirst(), model, cachedObjs);
         result.add(new PSPair<Object, ObjectState>(scheme, sname.getSecond()));
      }
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.impl.PSObjectConfigHandler#getDefaultDesignObject(java.util.Map)
    */
   @Override
   public Object getDefaultDesignObject(Map<String, Object> cachedObjs)
   {
      Collection<String> names;
      Object contextsObj = getContexts();
      // if the contexts object has replacement value, we get all the location
      // schemes as we can't decide what the user going to add in the default
      // config.
      if ((contextsObj instanceof String)
            && ((String) contextsObj).contains("${"))
      {
         contextsObj = "*";
      }
      names = getNamesFromCtxProperty(contextsObj);
      if (names == null || names.isEmpty())
         return null;
      String locSchemeName = getName();
      // If the specified location scheme does not exist, we assume it is new
      // and load the first object from available and return.
      if (!names.contains(locSchemeName))
         locSchemeName = names.iterator().next();
      Object scheme = getLocationScheme(locSchemeName, PSConfigUtils
            .getSchemeModel(), cachedObjs);
      return scheme;
   }

   /*
    * //see base class method for details
    */
   @Override
   public IPSGuid saveResult(IPSDesignModel model, Object obj, ObjectState state,
         List<IPSAssociationSet> assocList)
   {
      if (m_isUnProcess || state.equals(ObjectState.PREVIOUS))
      {
         IPSLocationScheme scheme = (IPSLocationScheme) obj;
         model.delete(scheme.getGUID());
      }
      else
      {
         model.save(obj, assocList);
      }
      return ((IPSLocationScheme) obj).getGUID();
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected Collection<String> getCurNames()
   {
      if (StringUtils.isBlank(getName()))
         throw new PSConfigException(
               "Location Scheme name may not be null or empty.");

      return getNamesFromCtxProperty(getContexts());
   }
   
   /*
    * //see base class method for details
    */
   @SuppressWarnings("unchecked")
   @Override
   protected Collection<String> getPrevNames()
   {
      Map<String, Object> prevProps = getPrevExtraProperties();
      if (prevProps == null || prevProps.isEmpty())
         return Collections.emptyList();

      return getNamesFromCtxProperty(prevProps.get(CONTEXTS));
   }
   
   /**
    * Gets the Location Scheme names from the specified contexts property,
    * which may be a string with wild-card, such as "*s*"; or a list of 
    * strings.
    * 
    * @param ctxValue the value of "contexts" property. It may be 
    * <code>null</code> or empty. 
    * 
    * @return a list of Location Scheme names, never <code>null</code>, but may 
    * be empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<String> getNamesFromCtxProperty(Object ctxValue)
   {
      if (ctxValue == null)
         return Collections.emptyList();
      if ((ctxValue instanceof Collection) && ((Collection)ctxValue).isEmpty())
         return Collections.emptyList();
         
      Collection<String> ctxNames = PSConfigUtils.getObjectNames(
            ctxValue, PSConfigUtils.getContextModel(), CONTEXTS);

      List<String> result = new ArrayList<String>();      
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel(); 
      for (String ctxName : ctxNames)
      {
         String name = getSchemeName(ctxName, model);
         result.add(name);
      }
      return result;
   }


   /**
    * This method does the actual work for {@link #getDesignObjects(Map)}.
    * 
    * @param ctxName the context name, assumed not <code>null</code> or empty.
    * @param model the Location Scheme model, never <code>null</code>.
    * 
    * @return the cached, loaded or created Location Scheme, never 
    * <code>null</code>.
    */
   private String getSchemeName(String ctxName, PSLocationSchemeModel model)
   {
      // get Location Scheme from cache
      String schemeName = getUniqueSchemeName(ctxName);

      // get Location Scheme from repository
      IPSDesignModel ctxModel = PSConfigUtils.getContextModel();
      IPSGuid ctxId = ctxModel.nameToGuid(ctxName);
      model.setContextId(ctxId);
      validateUniqueSchemeName(getName(), model, ctxName);
      return schemeName;
   }


   /**
    * This method does the actual work for {@link #getDesignObjects(Map)}.
    * 
    * @param uniqueSchemeName the unique location scheme name, assumed not
    * <code>null</code> or empty.
    * @param model the Location Scheme model, never <code>null</code>.
    * @param cachedObjs the cached Location Scheme objects, never
    * <code>null</code>, may be empty.
    * 
    * @return the cached, loaded or created Location Scheme, never
    * <code>null</code>.
    */
   private Object getLocationScheme(String uniqueSchemeName,
         PSLocationSchemeModel model, Map<String, Object> cachedObjs)
   {
      String ctxName = getContextFromUniqueName(uniqueSchemeName);
      
      // get Location Scheme from cache
      String schemeName = getUniqueSchemeName(ctxName);
      if (cachedObjs.get(schemeName) != null)
      {
         Object scheme = cachedObjs.get(schemeName);
         validateSameContentTypeAndTemplate(scheme, ctxName);
         return scheme;
      }
      
      // get Location Scheme from repository
      IPSDesignModel ctxModel = PSConfigUtils.getContextModel();
      IPSGuid ctxId = ctxModel.nameToGuid(ctxName);
      model.setContextId(ctxId);
      validateUniqueSchemeName(getName(), model, ctxName);
      IPSGuid id = model.nameToGuid(getName());
      IPSLocationScheme scheme;
      if (id != null)
      {
         scheme = (IPSLocationScheme) model.loadModifiable(id);
         validateSameContentTypeAndTemplate(scheme, ctxName);
      }
      else
      {
         scheme = createLocationScheme(model, ctxName, cachedObjs);
      }
      
      cachedObjs.put(uniqueSchemeName, scheme);

      return scheme;
   }

   /**
    * Validates the specified Location Scheme name, make sure it is unique
    * within the specified Context.
    * 
    * @param schemeName the Location Scheme name in question, assumed not
    * <code>null</code> or empty.
    * @param model the Location Scheme model, assumed not <code>null</code>.
    * @param ctxName the Context name, which is already set to the above
    * model, assumed not <code>null</code>.
    */
   private void validateUniqueSchemeName(String schemeName,
         PSLocationSchemeModel model, String ctxName)
   {
      boolean found = false;
      for (String n : model.findAllNames())
      {
         if (!schemeName.equalsIgnoreCase(n))
            continue;
         
         if (!found)
         {
            found = true;
            continue;
         }
         
         throw new PSConfigException("Failed to configure Location Scheme \""
               + schemeName
               + "\" because the name is not unique in Context \"" + ctxName
               + "\"."); 
      }
   }

   /**
    * Validates the Content Type and Template specified in the given Location
    * Scheme are the same as the {@link #getContentType()} and
    * {@link #getTemplate()}. Skip the checking of any of the properties are
    * not defined.
    * 
    * @param schemeObj the Location Scheme in question, assumed not
    * <code>null</code>.
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    */
   private void validateSameContentTypeAndTemplate(Object schemeObj,
         String ctxName)
   {
      IPSLocationScheme scheme = (IPSLocationScheme) schemeObj;
      if (StringUtils.isNotBlank(getContentType()))
      {
         IPSGuid ctId = PSConfigUtils.getContentTypeModel().nameToGuid(
               getContentType());
         if (scheme.getContentTypeId().longValue() != ctId.longValue())
         {
            throw new PSConfigException(
                  "Failed to configure Location Scheme \""
                        + getName()
                        + "\". It's Content Type \""
                        + getContentType()
                        + "\" is different with another same named Location Scheme under Context \""
                        + ctxName + "\".");
         }
      }
      if (StringUtils.isNotBlank(getTemplate()))
      {
         IPSGuid tpId = PSConfigUtils.getTemplateModel().nameToGuid(
               getTemplate());
         if (scheme.getTemplateId().longValue() != tpId.longValue())
         {
            throw new PSConfigException(
                  "Failed to configure Location Scheme \""
                        + getName()
                        + "\". It's Template \""
                        + getTemplate()
                        + "\" is different with another same named Location Scheme under Context \""
                        + ctxName + "\".");
         }
      }
   }
   
   /**
    * Creates a Location Scheme.
    * 
    * @param model the model used to create the Location Scheme, assumed not
    * <code>null</code>.
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @param the cached, loaded or created Location Scheme, never 
    * <code>null</code>.
    * 
    * @return the created Location Scheme, never <code>null</code>.
    */
   private IPSLocationScheme createLocationScheme(PSLocationSchemeModel model,
         String ctxName, Map<String, Object> cachedObjs)
   {
      validateNoneBlankContentTypeTemplate();
      PSPair<IPSGuid, IPSGuid> pair = getContentTypeTemplateAssoc();
      validateUniqueContentTypeTemplateAssoc(pair.getFirst(),
            pair.getSecond(), model, ctxName, cachedObjs);
      
      IPSLocationScheme scheme = model.createScheme(getName());
      scheme.setContentTypeId(pair.getFirst().longValue());
      scheme.setTemplateId(pair.getSecond().longValue());
      return scheme;
   }

   /**
    * Validates the specified Content Type and Template association, make sure
    * it is different with all Location Schemes under the same Context.
    * It is validated against the Location Schemes in cached and repository.
    * 
    * @param ctId the Content Type ID, assumed not <code>null</code>.
    * @param tpId the Template ID, assumed not <code>null</code>.
    * @param model the Location Scheme model, assumed not <code>null</code>.
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @param the cached, loaded or created Location Scheme, never 
    * <code>null</code>.
    */
   private void validateUniqueContentTypeTemplateAssoc(IPSGuid ctId,
         IPSGuid tpId, PSLocationSchemeModel model, String ctxName,
         Map<String, Object> cachedObjs)
   {
      // validate uniqueness against repository
      for (IPSLocationScheme scheme : model.findAllSchemes())
      {
         validateDiffContentTypeTemplate(ctId, tpId, ctxName, scheme, true);
      }

      // validate uniqueness against the cached Location Schemes
      for (String name : cachedObjs.keySet())
      {
         IPSLocationScheme scheme = (IPSLocationScheme) cachedObjs.get(name);
         if (isContextScheme(ctxName, name))
         {
            validateDiffContentTypeTemplate(ctId, tpId, ctxName, scheme, false);
         }
      }
      
   }

   /**
    * Validates the specified Content Type and Template association is not
    * contained in the given Location Scheme.
    * 
    * @param ctId the Content Type ID, assumed not <code>null</code>.
    * @param tpId the Template ID, assumed not <code>null</code>.
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @param scheme the Location Scheme in question. assumed not
    * <code>null</code>.
    * @param isExist <code>true</code> if the Location Scheme exists in the
    * repository.
    */
   private void validateDiffContentTypeTemplate(IPSGuid ctId, IPSGuid tpId,
         String ctxName, IPSLocationScheme scheme, boolean isExist)
   {
      if (scheme.getTemplateId().longValue() != tpId.longValue()
            || scheme.getContentTypeId().longValue() != ctId.longValue())
         return;
      
      if (isExist)
      {
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the existing Location Scheme \""
               + scheme.getName()
               + "\" has the same association of ContentType/Template (\""
               + getContentType() + "\"/\"" + getTemplate()
               + "\") under Context \"" + ctxName + "\".");
      }

      throw new PSConfigException("Failed to create Location Scheme \""
            + getName() + "\" since another Location Scheme \""
            + scheme.getName()
            + "\" has the same association of ContentType/Template (\""
            + getContentType() + "\"/\"" + getTemplate()
            + "\") under Context \"" + ctxName + "\".");
      
   }
   
   /**
    * validates the Content Type and Template association.
    * 
    * @return the validate Content Type and Template IDs, never
    * <code>null</code>.
    */
   private PSPair<IPSGuid, IPSGuid> getContentTypeTemplateAssoc()
   {
      String template = getTemplate();
      String ctName = getContentType();

      IPSGuid tpGuid = PSConfigUtils.getTemplateModel().nameToGuid(template);
      IPSGuid ctGuid = PSConfigUtils.getContentTypeModel().nameToGuid(ctName);
      if (tpGuid == null)
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the Template \"" + template
               + "\" does not exist.");
      if (ctGuid == null)
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the Content Type \"" + ctName
               + "\" does not exist.");
      
      if (!validateContentTypeTemplateAssoc(ctGuid, tpGuid))
      {
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the Content Type \"" + ctName
               + "\" does not associate with Template \"" + template + "\".");
      }
      
      return new PSPair<IPSGuid, IPSGuid>(ctGuid, tpGuid);
   }

   /**
    * Determines if the specified Content Type and Template is a valid 
    * association.
    * 
    * @param ctId the Content Type ID, assumed not <code>null</code>.
    * @param tpId the Template ID, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the association is valid.
    */
   private boolean validateContentTypeTemplateAssoc(IPSGuid ctId, IPSGuid tpId)
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      try
      {
         return mgr.findContentTypeTemplateAssociation(tpId, ctId) != null;
      }
      catch (RepositoryException e)
      {
         return false;
      }
   }
   
   /**
    * Validate the Content Type name and Template name are not <code>null</code>
    * or empty.
    */
   private void validateNoneBlankContentTypeTemplate()
   {
      String template = getTemplate();
      String ctName = getContentType();
      if (StringUtils.isBlank(template))
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the required property, \""
               + TEMPLATE + "\" is not defined.");
      if (StringUtils.isBlank(ctName))
         throw new PSConfigException("Failed to create Location Scheme \""
               + getName() + "\" since the required property, \""
               + CONTENT_TYPE + "\" is not defined.");      
   }
 
   /**
    * The separator used to create the unique names for the Location Schemes.
    */
   private static final String UNIQUE_SEP = " - ";
   
   /**
    * Returns the unique name from the specified Context name. 
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @return the unique name Location Scheme name, never <code>null</code> or
    * empty.
    */
   private String getUniqueSchemeName(String ctxName)
   {
      return ctxName + UNIQUE_SEP + getName();
   }
   
   /**
    * Gets the context name from a unique name, which is returned from
    * {@link #getUniqueSchemeName(String)}.
    * 
    * @param uniqueName the unique name, assumed not <code>null</code> or empty.
    * 
    * @return the context name, never <code>null</code> or empty.
    */
   private String getContextFromUniqueName(String uniqueName)
   {
      return uniqueName.substring(0, uniqueName.length() - UNIQUE_SEP.length()
            - getName().length());
   }
   
   /**
    * Determines of the specified Location Scheme name is belong to the
    * given Context.
    * 
    * @param ctxName the Context name, assumed not <code>null</code> or empty.
    * @param uniqueName the unique name of the Location Scheme that is 
    * created by {@link #getUniqueSchemeName(String)}.
    * 
    * @return <code>true</code> if the Location Scheme is under the given
    * Context.
    */
   private boolean isContextScheme(String ctxName, String uniqueName)
   {
      return uniqueName.startsWith(ctxName + UNIQUE_SEP);
   }
   
   /*
    * //see base class method for details
    */
   @Override
   public boolean isGetDesignObjects()
   {
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getExtraProperties()
   {
      return m_extraProperties;
   }

   /*
    * //see base class method for details
    */
   @Override
   public void setExtraProperties(Map<String, Object> props)
   {
      m_extraProperties.putAll(props);
   }

   /**
    * Holds the {@link #CONTEXTS} property, which is a list of Context names
    */
   private Map<String, Object> m_extraProperties = new HashMap<String, Object>();

   /**
    * Determines if the {@link #unprocess(Object, List)} was called.
    * Default to <code>false</code>.
    */
   private boolean m_isUnProcess = false;
   
   /**
    * The name of the extra/additional property. This is a required property
    * see {@link #getContexts()} for details.
    */
   public static final String CONTEXTS = "contexts";
   
   /**
    * The Template name property. This is optional property for existing
    * Location Scheme. However, it is a required property for configuring
    * created/new Location Scheme. 
    */
   public static final String TEMPLATE = "template";
   
   /**
    * The Content Type name property. This is optional property for existing
    * Location Scheme. However, it is a required property for configuring
    * created/new Location Scheme.
    */
   public static final String CONTENT_TYPE = "contentType";
}
