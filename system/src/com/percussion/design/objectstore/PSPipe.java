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

package com.percussion.design.objectstore;

import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * The PSPipe abstract class is used to define how an XML document is being
 * mapped to one or more back-end data stores. Pipes requiring query
 * operations are handled through the PSQueryPipe class. Pipes requiring
 * insert, update and delete operations are handled through the PSUpdatePipe
 * class.
 *
 * @see   PSDataSet
 * @see   PSDataSet#getPipe
 * @see   PSQueryPipe
 * @see   PSUpdatePipe
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public abstract class PSPipe extends PSComponent {

/**
 * Get the name of the pipe.
 *
 * @return   the name of the pipe
 */
   public java.lang.String getName()
   {
      return m_name;
   }

   /**
    * Set the name of the pipe.
    * This is limited to 50 characters.
    *
    * @param   name  the new name of the pipe. This must be a unique
    *              name within the data set. If it is non-unique,
    *              an exception will be thrown when the application
    *              is saved on the E2 server.
    */
   public void setName(java.lang.String name)
   {
      IllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
   }

   private static IllegalArgumentException validateName(String name)
   {
      if ((null == name) || (name.length() == 0))
         return new IllegalArgumentException("pipe name is empty");
      else if (name.length() > MAX_PIPE_NAME_LEN)
      {
         return new IllegalArgumentException("pipe name is too big" +
            MAX_PIPE_NAME_LEN + " " + name.length());
      }

      return null;
   }

   /**
    * Get the description of the pipe.
    *
    * @return   the description of the pipe
    */
   public java.lang.String getDescription()
   {
      return m_description;
   }

   /**
    * Set the description of the pipe.
    * This is limited to 255 characters.
    *
    * @param   description the new description of the pipe.
    */
   public void setDescription(java.lang.String description)
   {
      IllegalArgumentException ex = validateDescription(description);
      if (ex != null)
         throw ex;
      m_description = description;
   }

   private static IllegalArgumentException validateDescription(
      String description)
   {
      if ((description != null)  && (description.length() > MAX_DESC_LEN))
      {
         return new IllegalArgumentException("pipe desc is too big" +
            MAX_DESC_LEN + " " + description.length());
      }

      return null;
   }

   /**
    * Get the back-end data tank describing the back-end data stores
    * being used to access data for this pipe.
    *
    * @return   the back-end data tank
    */
   public PSBackEndDataTank getBackEndDataTank()
   {
      return m_backEndDataTank;
   }

   /**
    * Overwrite the back-end data tank object with the specified back-end
    * data tank object. If you only want to modify some settings, use
    * getBackEndDataTank to get the existing object and modify the returned
    * object directly.
    * <p>
    * The back-end data tank describes the back-end being used to access
    * data for this pipe.
    * <p>
    * The PSBackEndDataTank object supplied to this method will be stored
    * with the PSPipe object. Any subsequent changes made to the object by
    * the caller will also effect the pipe.
    *
    * @param   dt    the new back-end data tank object
    *
    * @see   #getBackEndDataTank
    * @see   PSBackEndDataTank
    */
   public void setBackEndDataTank(PSBackEndDataTank dt)
   {
      m_backEndDataTank = dt;
   }

   private static IllegalArgumentException validateBackEndDataTank(
      PSBackEndDataTank dt)
   {
      if (dt == null)
         return new IllegalArgumentException("No backend data tank found");

      return null;
   }

   /**
    * Get the data mapper associated with this pipe. The data mapper defines
    * mappings between XML elements or attributes and back-end columns.
    * JavaScript can also be used in lieu of a back-end column. This allows
    * an XML element or attribute to be mapped to a dynamically computed
    * value.
    *
    * @return   the data mapper (may be null)
    */
   public PSDataMapper getDataMapper()
   {
      return m_dataMapper;
   }

   /**
    * Overwrite the data mappings object with the specified data mappings
    * object. If you only want to modify some data mappings, use
    * getDataMapper to get the existing object and modify the returned
    * object directly.
    * <p>
    * The data mapper defines mappings between XML elements or attributes
    * and back-end columns. JavaScript can also be used in lieu of a
    * back-end column. This allows an XML element or attribute to be mapped
    * to a computed value.
    * <p>
    * The PSDataMapper object supplied to this method will be stored with
    * the PSPipe object. Any subsequent changes made to the object by the
    * caller will also effect the pipe.
    *
    * @param   mapper   the new data mapper (may be null to clear mappings)
    *
    * @see   #getDataMapper
    * @see   PSDataMapper
    */
   public void setDataMapper(PSDataMapper mapper)
   {
      IllegalArgumentException ex = validateDataMapper(mapper);
      if (ex != null)
         throw ex;

      m_dataMapper = mapper;
   }

   private static IllegalArgumentException validateDataMapper
      (PSDataMapper mapper)
   {
      if (mapper != null)
      {
         if (!com.percussion.design.objectstore.PSDataMapping.class.isAssignableFrom(
            mapper.getMemberClassType()))
         {
            return new IllegalArgumentException("cool bad content type");
         }
      }

      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param   pipe a valid PSPipe. 
    */
   public void copyFrom( PSPipe pipe )
   {
      copyFrom((PSComponent) pipe );
      // assume pipe is in valid state
      m_name = pipe.getName();
      m_description = pipe.getDescription();
      m_backEndDataTank = pipe.getBackEndDataTank();
      m_dataMapper = pipe.getDataMapper();
      m_resultDataExtensions = pipe.getResultDataExtensions();
      m_inputDataExtensions = pipe.getInputDataExtensions();
   }


   @Override
   public boolean equals( Object o )
   {
      if (!( o instanceof PSPipe ))
         return false;
      PSPipe pipe = (PSPipe) o;
      boolean bEqual = true;
      if ( !compare( m_name, pipe.m_name ))
         bEqual = false;
      else if ( !compare( m_description, pipe.m_description ))
         bEqual = false;
      else if ( !compare( m_backEndDataTank, pipe.m_backEndDataTank ))
         bEqual = false;
      else if ( !compare( m_dataMapper, pipe.m_dataMapper ))
         bEqual = false;
      else if ( !compare(m_resultDataExtensions, pipe.m_resultDataExtensions))
         bEqual = false;
      else if ( !compare(m_inputDataExtensions, pipe.m_inputDataExtensions))
         bEqual = false;

      return bEqual;
   }

   /**
    * Generates code of the object.
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_name)
            .append(m_description).toHashCode();
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   @Override
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateName(m_name);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDescription(m_description);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateBackEndDataTank(m_backEndDataTank);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDataMapper(m_dataMapper);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_backEndDataTank != null)
            m_backEndDataTank.validate(cxt);

         if (m_dataMapper != null)
            m_dataMapper.validate(cxt);

         if (m_inputDataExtensions != null)
            m_inputDataExtensions.validate(cxt);

         if (m_resultDataExtensions != null)
            m_resultDataExtensions.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   /**
    * Get the input data extensions. Input data extensions are used to pre-process
    * the data sent by the requestor. Validation can be done, as well as
    * modification of that data. The extensions are returned in the order in
    * which they will be executed.
    *
    * @return   the extension calls used to massage the input data
    *                    (may be null)
    *
    * @see   PSExtensionCall
    */
   public PSExtensionCallSet getInputDataExtensions()
   {
      return m_inputDataExtensions;
   }


   /**
    * Overwrite the input data extensions with the specified set.
    * If you only want to modify certain extensions, add a new extension, etc. use
    * getInputDataExtensions to get the existing set and modify the
    * returned set directly.
    * <p>
    * Input data extensions are used to pre-process the data sent by the
    * requestor. Validation can be done, as well as modification of that
    * data. The extensions will be executed in the same order as they are stored
    * in the set.
    * <p>
    * The PSExtensionCallSet object supplied to this method will be stored with
    * the PSUpdatePipe object. Any subsequent changes made to the object by
    * the caller will also effect the update pipe.
    *
    * @param   extensions    the new input data extensions (may be null)
    *
    * @see   #setInputDataExtensions
    * @see   PSExtensionCall
    */
   public void setInputDataExtensions(PSExtensionCallSet extensions)
   {
      m_inputDataExtensions = extensions;
   }


   /**
    * Get the result data extensions. Result data extensions are used to post-process
    * the data before it is sent back to the requestor. Data is in XML
    * format at this point. Setting cookies and filtering data are the most
    * common uses of this type of extension. The extensions are returned in the
    * order in which they will be executed.
    *
    * @return   the extension calls used to massage the result data
    *              (may be null)
    *
    * @see   PSExtensionCallSet
    */
   public PSExtensionCallSet getResultDataExtensions()
   {
      return m_resultDataExtensions;
   }

   /**
    * Overwrite the result data extensions with the specified set.
    * If you only want to modify certain extensions, add a new extension, etc. use
    * getResultDataExtensions to get the existing set and modify the
    * returned set directly.
    * <p>
    * Result data extensions are used to post-process the data before it is
    * sent back to the requestor. Data is in XML format at this point.
    * Setting cookies and filtering data are the most common uses of this
    * type of extension. The extensions will be executed in the same order as they
    * are stored in the collection.
    * <p>
    * The PSExtensionCallSet object supplied to this method will be stored with
    * the PSQueryPipe object. Any subsequent changes made to the object
    * by the caller will also effect the query pipe.
    *
    * @param   extensions the new result data extensions (may be null)
    *
    * @see   #getResultDataExtensions
    * @see   PSExtensionCallSet
    */
   public void setResultDataExtensions(PSExtensionCallSet extensions)
   {
      m_resultDataExtensions = extensions;
   }



   /**
    * Creates a deep copy of this <tt>PSPipe</tt> instance
    * @return a clone of this instance
    */
   @Override
   public Object clone()
   {
      PSPipe copy = (PSPipe)super.clone();
      if (m_backEndDataTank !=null)
         copy.m_backEndDataTank =
            (PSBackEndDataTank)m_backEndDataTank.clone();
      if (m_dataMapper !=null)
         copy.m_dataMapper = (PSDataMapper)m_dataMapper.clone();
      if (m_resultDataExtensions !=null)
         copy.m_resultDataExtensions =
            (PSExtensionCallSet)m_resultDataExtensions.clone();
      if (m_inputDataExtensions !=null)
         copy.m_inputDataExtensions =
            (PSExtensionCallSet)m_inputDataExtensions.clone();
      return copy;
   }

   // NOTE: when adding members, be sure to update the copyFrom method
   protected String               m_name = "";
   protected String               m_description = "";
   protected PSBackEndDataTank   m_backEndDataTank = null;
   protected PSDataMapper         m_dataMapper = null;
   protected PSExtensionCallSet   m_resultDataExtensions   = null;
   protected PSExtensionCallSet   m_inputDataExtensions = null;

   private static final int      MAX_PIPE_NAME_LEN      = 50;
   private static final int      MAX_DESC_LEN         = 255;

   // package access on this so they may reference each other in fromXml
   static final String   ms_NodeType            = "PSXPipe";

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   protected static final String NAME_ELEM = "name";
   protected static final String DESCRIPTION_ELEM = "description";
   protected static final String INPUT_DATA_EXITS_ELEM = "InputDataExits";
   protected static final String RESULT_DATA_EXITS_ELEM = "ResultDataExits";
}

