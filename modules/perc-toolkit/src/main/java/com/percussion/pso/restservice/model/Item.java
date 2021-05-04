/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.model;


import javax.xml.bind.annotation.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Collection of package objects.
 * @author sbolton
 *
 */
@XmlRootElement(name = "Item")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name="", propOrder={"folders","folderInfo","fields","children","relationships","depRelationships","errors"})
public class Item {
	
	private String forceCheckin;//never (default), always, user;
	private String title;
	private String contentType;
	private String communityName;
	private String state;
	private String workflow;
	private String locale;
	private String updateType;
	private String checkoutUserName;
	private Boolean checkInOnly;
	private String updatedDateField;
	private String keyField;
	private String contextRoot;
	private String transition;
	private String editTransition;
	private Relationships relationships;
	private Relationships depRelationships;
	private Integer revision;
	private Integer contentId;
	private String ETag;
	private String lastModified;
	private List<Field> fields;
	


	private List<Child> children = null;
	private List<Error> errors = null;

	private List<String> folders = null;
	private FolderInfo folderInfo;
	
	
	@XmlAttribute
	public String getUpdateType() {
		return updateType;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}
	
	@XmlAttribute
	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
	@XmlAttribute
	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}
	@XmlAttribute
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	@XmlAttribute
	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
	@XmlAttribute
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	@XmlAttribute
	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}
	@XmlAttribute
	public String getCheckoutUserName() {
		return checkoutUserName;
	}
	public void setCheckoutUserName(String checkoutUserName) {
		this.checkoutUserName = checkoutUserName;
	}

	@XmlAttribute
	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	@XmlAttribute
	public Integer getContentId() {
		return contentId;
	}

	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	@XmlAttribute
	public String getLocale() {
		return locale;
	}

	public Item()
	{
		super();
	}

	@XmlElement(name = "Field")
	@XmlElementWrapper(name="Fields")
	public List<Field> getFields()
	{
		return this.fields;
	}

	public void setFields(List<Field> fields) {
		this.fields=fields;
	}

	@XmlElement(name = "Child")
	@XmlElementWrapper(name="Children")
	public List<Child> getChildren()
	{
		return children;
	}
	public void setChildren(List<Child> children) {
		this.children = children;
	}


	public void setFolders(List<String> folders) {
		this.folders = folders;
	}
	@XmlElement(name = "Path")
	@XmlElementWrapper(name="Folders")
	public List<String> getFolders() {
		return folders;
	}

	public void setRelationships(Relationships relationships) {
		this.relationships = relationships;
	}

	@XmlElement(name="Relationships")
	public Relationships getRelationships() {
		return relationships;
	}

	public void setDepRelationships(Relationships relationships) {
		this.depRelationships = relationships;
	}
	@XmlElement(name="DepRelationships")
	public Relationships getDepRelationships() {
		return depRelationships;
	}

	@XmlElement(name = "Error")
	@XmlElementWrapper(name="Errors")
	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	public void addError(Error.ErrorCode error, String message) {
		if(errors == null) errors = new ArrayList<Error>();
		errors.add(new Error(error,message));
	}



	public void addError(Error.ErrorCode error,Exception e) {
		if(errors == null) errors = new ArrayList<Error>();
		String message = e.getMessage() ;
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		errors.add(new Error(error,contentId,message + ":"+sw.toString()));
	}
	
	public void addError(Error.ErrorCode error,String message, Exception e) {
		if(errors == null) errors = new ArrayList<Error>();
		String messageex = e.getMessage() +"\n";
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		errors.add(new Error(error,contentId,messageex + "\n"+ message +"\n"+sw.toString()));
		
	}

	public void addError(Error.ErrorCode error) {
		if(errors == null) errors = new ArrayList<Error>();
		errors.add(new Error(error,contentId,""));
	}

	public boolean hasError(Error.ErrorCode error) {
		if (errors != null) {
			for (Error errorTest : errors) {
				if (errorTest.getErrorCode() == error) {
					return true;
				}
			}
		}
		return false;
	}
	@XmlElement
	public void setFolderInfo(FolderInfo folderInfo) {
		this.folderInfo = folderInfo;
	}

	public FolderInfo getFolderInfo() {
		return folderInfo;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	@XmlAttribute
	public String getTitle() {
		return title;
	}

	public void setForceCheckin(String forceCheckin) {
		this.forceCheckin = forceCheckin;
	}
	@XmlAttribute
	public String getForceCheckin() {
		return forceCheckin;
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.HashMap#containsKey(Object)
	 */
	
	public boolean containsField(String key) {
		if(fields!=null){
			for(Field field: fields) {
				if (field.getName()!=null && field.getName().equals(key)) return true;
			}
		}
		return false;
	}

	/**
	 * @param newField
	 * @return
	 * @see java.util.HashMap#put(Object, Object)
	 */
	@XmlTransient
	public void setField(Field newField) {
		boolean fieldFound=false;
		for(Field field: fields) {
			if (field.getName().equals(newField.getName())) { 
				field = newField;
				fieldFound = true;
			}
			if (!fieldFound) {
				fields.add(field);
			}
		}
	
	}

	


	/**
	 * @param name
	 * @return
	 * @see java.util.HashMap#get(Object)
	 */
	
	public Field getField(String name) {
		if(fields!=null){
			for(Field field: fields) {
				if (field.getName()!=null && field.getName().equals(name)) return field;
			}
		}
		return null;
	}
	@XmlAttribute
	public void setCheckInOnly(Boolean checkInOnly) {
		this.checkInOnly = checkInOnly;
	}

	public Boolean getCheckInOnly() {
		return checkInOnly;
	}

	/**
	 * @param updatedDateField the updatedDateField to set
	 */
	@XmlAttribute
	public void setUpdatedDateField(String updatedDateField) {
		this.updatedDateField = updatedDateField;
	} 
	/**
	 * @return the updatedDateField
	 */
	
	public String getUpdatedDateField() {
		return updatedDateField;
	}

	@XmlAttribute
	public void setTransition(String transition) {
		this.transition = transition;
	}
	
	public String getTransition() {
		return transition;
	}

	@XmlAttribute
	public void setEditTransition(String editTransition) {
		this.editTransition = editTransition;
	}

	public String getEditTransition() {
		return editTransition;
	}

	
}
