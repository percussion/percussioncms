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

/**
  * A small data object class to hold some summary information for
  * a template.
  */	 
(function($){
	/**
	 * Constructor to create a new template summary object.
	 * @param sourceId source id of template from which a template will be created
	 */			
	 $.Perc_Template_Summary = function(sourceId, templateId, templateName){
		templateName = templateName + "";
      this.sourceId = sourceId;
		this.templateId = templateId;
		this.baseTemplateName = templateName;
        this.templateName = templateName;
        this.templateNameLowerCase = templateName.toLowerCase();
		this.siteIds = new Array();
		this.imageUrl = null;
		this.persisted = false;
		this.deleteFlag = false;
		this.isBase = false;
        this.contentMigrationVersion = 0;
		
		/**
		 * @returns the template base name
		 */
		this.getBaseTemplateName = function() {return this.baseTemplateName; }
		
		/**
		 * @returns the template source id (String).
		 */			 
		this.getSourceId = function(){return this.sourceId;}
		
		/**
		 * @returns the template id (String).
		 */			
		this.getTemplateId = function(){return this.templateId;}
		
		/**
		 * @returns the template name (String).
		 */			 
		this.getTemplateName = function(){return this.templateName;}
		
		/**
         * @returns the lowercase template name (String).
         */          
        this.getTemplateNameLowerCase = function(){return this.templateNameLowerCase;}
        
        /**
		 * @returns the array of assigned sites (Array).
		 */			 
		this.getAssignedSites = function(){
			return this.siteIds;
		}
		
		/**
		 * @returns the image relative url for this template (String).
		 */
		this.getImageUrl = function(){
			return this.imageUrl;
		}
		
		this.isMarkedForDelete = function(){
         return this.deleteFlag;
      }
      
      this.isBaseTemplate = function(){
         return this.isBase;
      }
		 
		 /**
		* @returns is persisted status (boolean) 
		*/				
		this.isPersisted = function(){return this.persisted;}			 
		
		/**
		 * @returns the version of the template (String).
		 */
		this.getContentMigrationVersion = function(){
			return this.contentMigrationVersion;
		}

		/**
		 * @param id (String) the source id, may be <code>null</code> or
		 * undefined.
		 */					
		this.setSourceId = function(id){this.sourceId = id;}
		
		/**
		 * @param id (String) the template id, may be <code>null</code> or
		 * undefined.
		 */ 
		this.setTemplateId = function(id){this.templateId = id;}
		
		/**
		 * @param name (String) the template name, should not be <code>null</code> or
		 * undefined.
		 */ 
		this.setTemplateName = function(name){				
			this.templateName = name;
			this.templateNameLowerCase = name.toLowerCase();
		}
        
		/**
		 * @param cmv sets the version of the template (String).
		 */
		this.setContentMigrationVersion = function(cmv){
			 this.contentMigrationVersion = cmv;
		}
		
		/**
		 * @param siteIds (String) the list of site id this template is assigned to
		 * , may be <code>null</code> or
		 * undefined.
		 */		
		this.setAssignedSites = function(siteIds){
         if(siteIds == null || typeof siteIds === 'undefined')
            siteIds = new Array();
         this.siteIds = siteIds;
      }
		
		this.assignToSite = function(siteId){
         if(!this.containsSite(siteId))
            this.siteIds.push(siteId);
      }
		
		this.unassignFromSite = function(siteId){
			for(s in this.siteIds)
				if(siteId == this.siteIds[s]) {
					this.siteIds.splice(s,1);
				}
		}
		
		this.containsSite = function(siteId){
         for(s in this.siteIds)
         {
            if(siteId == this.siteIds[s])
               return true;
         }
         return false;
      }
		
		/**
		 * @param url (String) the template url, may be <code>null</code> or
		 * undefined.
		 */
		this.setImageUrl = function(url){this.imageUrl = url;}
		
		/**
		 * @param isPersisted (boolean).
		 */			 
		this.setPersisted = function(isPersisted){this.persisted = isPersisted;}
		
		/**
		 * Set the delete flag. <code>true</code> idicates that this template
		 * is marked for deletion.
		 */              		
		this.setDeleteFlag = function(del){this.deleteFlag(del);}
		
		this.setBaseTemplate = function(isBase){this.isBase = isBase;}

		/**
		 * Clone this object
		 */			 
		this.clone = function(){
			var clone = $.extend(true, {}, this);
			clone.setPersisted(false);          
			return clone;      
		}
		
		this.isOrphan = function(){
   //      	alert([this.getTemplateId(), this.isBase, this.siteIds.length,  (!this.isBase &&  this.siteIds.length == 0)]);
	//		if(this.isBase
         return (!this.isBase &&  this.siteIds.length == 0); 
      }
		
	}

})(jQuery);
