package com.percussion.pso.demandpreview.service;

/**
 * Configuration bean for use in Spring configuration of 
 * the preview site, edition, template and context.
 * This class is used as an inner bean in the Spring configuration. 
 * The members are all names of objects, rather than object classes
 * themselves.  
 *
 * @author davidbenua
 *
 */
public class SiteEditionConfig {
	
	private String siteName;
	private String editionName;
	private String contextURLRootVar;
	private int assemblyContext; 
	
	/**
	 * Default Constructor.
	 */
	public SiteEditionConfig()
	{
		
	}
	
	/**
	 * Gets the site name
	 * @return the site name.
	 */
	public String getSiteName()
	{
		return siteName;				
	}
	
    /**
     * Sets the site name.
     * @param siteName the site name to set. 
     */
	public void setSiteName(String siteName)
	{
		this.siteName = siteName;
	}
	
	/**
	 * Gets the edition name
	 * @return the edition name.
	 */
	public String getEditionName()
	{
		return editionName;
		
	}
	
	/**
	 * Sets the edition name. 
	 * @param editionName the edition name to set. 
	 */
	public void setEditionName(String editionName)
	{
		this.editionName = editionName;
	}

 
   /**
    * Gets the assembly context. 
    * @return the assemblyContext
    */
   public int getAssemblyContext()
   {
      return assemblyContext;
   }

   /**
    * Sets the assembly context. 
    * @param assemblyContext the assemblyContext to set
    */
   public void setAssemblyContext(int assemblyContext)
   {
      this.assemblyContext = assemblyContext;
   }

/**
 * Gets the context variable that holds the URL root for
 * the assembly context.
 * 
 * @return The context variable name
 */
public String getContextURLRootVar() {
	return contextURLRootVar;
}

/**
 * Sets the context variable that holds the URL root for
 * the assembly context.
 * 
 * @param contextURLRootVar The name of the context variable holding the URL root
 */
public void setContextURLRootVar(String contextURLRootVar) {
	this.contextURLRootVar = contextURLRootVar;
}

}