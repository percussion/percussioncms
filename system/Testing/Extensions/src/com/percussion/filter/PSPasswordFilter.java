/******************************************************************************
 *
 * [ PSPasswordFilter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.filter;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.IPSPasswordFilter;

import java.io.File;

/**
 * This password filter is used to test that the filter is called correct 
 * when used with directory services. 
 */
public class PSPasswordFilter implements IPSPasswordFilter
{
   /**
    * Just adds the string 'admin' to the supplied passord and returns that.
    */
   public String encrypt(String password)
   {
      if (password == null)
         throw new IllegalArgumentException("password cannot be null");
      
      System.out.println("called PSPasswordFilter.encrypt(String) for TESTING ONLY.");
      
      return password + "admin";
   }
   
   /**
    * Nothing to be initialized.
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
   }
}
