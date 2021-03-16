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

(function($){

   $.Perc_SectionServiceClient =  {
      getRootSection: getRootSection,
      getChildren: getChildren,
      getSection: getSection,
      deleteSection: deleteSection,
      deleteSectionLink:deleteSectionLink,
      convertSectionToFolder:convertSectionToFolder,
      edit: edit,
      updateSectionLink: updateSectionLink,
      updateExternalLink:updateExternalLink,
      create: create,
      convertFolder:convertFolder,
      createSectionLink:createSectionLink,
      getTree: getTree,
      move: move,
      replaceLandingPage: replaceLandingPage,
      clearCache: clearCache,
      PERC_SECTION_TYPE:{SECTION:"section",EXTERNAL_LINK:"externallink",SECTION_LINK:"sectionlink"}
   };
   
   var cache = {
      getChildren: {}
   };
   
     /**
      * Create a new section. This is a JSON only call and returns json in 
      * the callback.      
      * @param sectionObj the create section object. Cannot be <code>null</code>.
      * @param callback the callback function to be called when the request completes.
      */               
     function create(sectionObj, callback){
        var url = sectionObj.CreateSiteSection?$.perc_paths.SECTION_CREATE:$.perc_paths.SECTION_CREATE_EXTERNAL_LINK;
        $.PercServiceUtils.makeJsonRequest(
           url,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           sectionObj);        
     }    

     /**
      * Create a new section. This is a JSON only call and returns json in 
      * the callback.      
      * @param sectionObj the create section object. Cannot be <code>null</code>.
      * @param callback the callback function to be called when the request completes.
      */               
     function convertFolder(sectionObj, callback){
        var url = $.perc_paths.SECTION_SECTION_FROM_FOLDER;
        $.PercServiceUtils.makeJsonRequest(
           url,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           sectionObj);        
     }    

     /**
      * Creates a section link between the supplied targetSectionId and supplied parentSectionId      
      * @param targetSectionId(String) the guid of the targetSection. Cannot be <code>null</code>.
      * @param parentSectionId(String) the guid of the parentSection. Cannot be <code>null</code>.
      * @param callback the callback function to be called when the request completes. The first parameter
      * is $.PercServiceUtils.STATUS_XXX and the second parameter is errorMessage in case of failure or PSSiteSection 
      * object incase of success. @see IPSSiteSectionService#createSectionLink for more details. 
      */               
     function createSectionLink(targetSectionId, parentSectionId, callback){
        $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_CREATE_SECTION_LINK + "/" + targetSectionId + "/" + parentSectionId,
           $.PercServiceUtils.TYPE_GET,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           });        
     }  
     
     /**
      * Update Section Info. This is a JSON only call and returns json in 
      * the callback.      
      * @param sectionObj the edit section object. Cannot be <code>null</code>.
      * @param callback the callback function to be called when the request completes.
      */               
     function edit(sectionObj, callback){
        $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_UPDATE,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           sectionObj);        
     }
     
     /**
      * Update the section link
      */
     function updateSectionLink(updateSecObject, callback) {
         $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SECTION_LINK_UPDATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result){
               if(status === $.PercServiceUtils.STATUS_SUCCESS)
               {
                   callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
               }
               else
               {
                   var defaultMsg = 
                      $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                   callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
               }
            },
            updateSecObject);        

     }
     
     /**
      * 
      */
     function updateExternalLink(sectionId, extLinkObj, callback)
     {
         $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SECTION_EXTERNAL_LINK_UPDATE + "/" + sectionId,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result){
               if(status === $.PercServiceUtils.STATUS_SUCCESS)
               {
                   callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
               }
               else
               {
                   var defaultMsg = 
                      $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                   callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
               }
            },
            extLinkObj);        

     }
     /**
      * Get the root section for the site specified as a passed option for
      * this widget.
      * @param site the sitename, assumed not <code>null</code> or empty.        
      * @param callback function to be called when section is retrieved, the
      * section object will be the sole argument passsed to the callback.      
      */                                       
     function getRootSection(site, callback){

       $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_GET_ROOT + "/" + site,
           $.PercServiceUtils.TYPE_GET,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {                  
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg =
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  var defaultErrorCode  =
                      $.PercServiceUtils.extractGlobalErrorCode(result.request);

                  if(defaultErrorCode === $.perc_errors.NAVIGATION_SERVICE_FOLDER_ID_NOT_FOUND_FOR_PATH || 
                      defaultErrorCode === $.perc_errors.NAVIGATION_SERVICE_CANNOT_FIND_NAVTREE_FOR_SITE){
                      // this is a bad site record.
                      console.warn("Bad site record for site was deleted");
                  }else {
                      callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                  }
              }
           }
         );
         
     } 
           
     /**
      * Get the child sections for the section specified.
      * This is only used to fill the cache and is not called directly.
      * @param  section {Object} the SiteSection object of the parent whose children
      * should be retrieved.                   
      * @param callback {function} function to be called when section is retrieved, the
      * parent section object will be the sole argument passsed to the callback.
      * @param force {boolean} flag indicating that we should force a request to server
      * instead of using cached data.            
      */  
     function getChildren(section, callback, force){
        if(!force && typeof(cache.getChildren[section.SiteSection.id]) != 'undefined')
       {
          callback($.PercServiceUtils.STATUS_SUCCESS, section, cache.getChildren[section.SiteSection.id]);
       }
       else
       {
        $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_GET_CHILDREN,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  cache.getChildren[section.SiteSection.id] = result.data;
                  callback($.PercServiceUtils.STATUS_SUCCESS, section, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           section
         );                
         }     
     }
     
     /** 
      * Get the data for an individual section. Used primarily for UpdateSection.
      * @param  sectionid {String} the section id, cannot be <code>null</code> or empty.   
      * @param callback {function} The Callback to fire when ready. Follows same callback conventions as
      *        the other callbacks in this API.
      */
     function getSection(sectionid, callback) {
        sectionid = sectionid.split("_")[0];
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SECTION_LOAD + "/" + sectionid,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
                {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                    }
                    else
                    {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                    }
                },
            false         // Data to post. Not required for GET
            );
     }
     
     /** 
      * Get the section tree for a site. 
      * @param  siteid {String} the site id, cannot be <code>null</code> or empty.     
      * @param callback {function} The Callback to fire when ready. Follows same callback conventions as
      * the other callbacks in this API.
      */
     function getTree(siteid, callback) {
        $.PercBlockUI();
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SECTION_GET_TREE + "/" + siteid,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
                {
                    $.unblockUI();
                    if (status === $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                    }
                    else
                    {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                        callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                    }
                },
            false,         // Data to post. Not required for GET
            function(status)
            {
                $.unblockUI();
                var result = {data : I18N.message("perc.ui.section.service.client@Timed Out")};
                callback($.PercServiceUtils.STATUS_ERROR, result);
            },
            120000
         );
     }
     
     /**
      * Move a section.
      * @param moveSiteSectionObj {Object} a request object corresponding to the following structure:
      * <pre>
      * obj.MoveSiteSection
      * obj.MoveSiteSection.targetId
      * obj.MoveSiteSection.sourceId
      * obj.MoveSiteSection.targetIndex                        
      * </pre>            
      * @param callback {function} The Callback to fire when ready. Follows same callback conventions as
      * the other callbacks in this API.
      */                       
     function move(moveSiteSectionObj, callback){
       $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_MOVE,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, moveSiteSectionObj, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           moveSiteSectionObj
         );       
     }
     
     /**
      * Make server request to replace a section landing page with the specified page.
      * @param pageid {string} the page id of what will become the new landing page.
      * Cannot be <code>null</code> or empty.
      * @param sectionid {string} the section id for the target section.
      * Cannot be <code>null</code> or empty. 
      * @param callback the callback function to be called when the request completes.
      */               
     function replaceLandingPage(pageid, sectionid, callback){
        var obj = {ReplaceLandingPage: {
           newLandingPageId: pageid,
           sectionId: sectionid
        }};
        
        $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_REPLACE_LANDING_PAGE,
           $.PercServiceUtils.TYPE_POST,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           },
           obj);        
     }

     /**
      * Delete a section.
      * @param sectionid {String} the section id, cannot be <code>null</code> or empty.
      * @param callback {function} The Callback to fire when ready. Follows same callback conventions as
      * the other callbacks in this API.      
      */                 
     function deleteSection(sectionid, callback){
       $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_DELETE + "/" + sectionid,
           $.PercServiceUtils.TYPE_DELETE,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           }
         );                    
     }
     
     /**
      * Deletes a section link between the supplied targetSectionId and supplied parentSectionId      
      * @param targetSectionId(String) the guid of the targetSection. Cannot be <code>null</code>.
      * @param parentSectionId(String) the guid of the parentSection. Cannot be <code>null</code>.
      * @param callback the callback function to be called when the request completes. The first parameter
      * is $.PercServiceUtils.STATUS_XXX and the second parameter is errorMessage in case of failure or PSNoContent 
      * object in case of success. @see IPSSiteSectionService#deleteSectionLink for more details. 
      */               
     function deleteSectionLink(targetSectionId, parentSectionId, callback){
        $.PercServiceUtils.makeJsonRequest(
           $.perc_paths.SECTION_DELETE_SECTION_LINK + "/" + targetSectionId + "/" + parentSectionId,
           $.PercServiceUtils.TYPE_GET,
           false,
           function(status, result){
              if(status === $.PercServiceUtils.STATUS_SUCCESS)
              {
                  callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
              }
              else
              {
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              }
           });        
     } 
     
     /**
      * Makes a call to server to convert section to folder.
      * @param {Object} sectionId must be a valid sectionId otherwise server will throw an error.
      * @param {Object} callback gets called after server converts section to folder, in case of error
      * the first argument to the callback is false and second argument is error message, in case of success
      * there is only the first argument and that is true.
      * 
      */
     function convertSectionToFolder(sectionId, callback){
        $.PercServiceUtils.makeRequest(
           $.perc_paths.SECTION_CONVERT_TO_FOLDER + sectionId,
           $.PercServiceUtils.TYPE_DELETE,
           false,
           function(status, result){
              if(status !== $.PercServiceUtils.STATUS_SUCCESS){
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                  callback(false, defaultMsg);
              }
              else{
                  callback(true);
              }
           });        
     } 
     
     /**
      * Clear cache entry.
      * @param category
      * @param key            
      */           
     function clearCache(category, key){
        if(!category && !key)
        {
          //clear all
          for(var n in cache)
          {
             cache[n] = {};
          }          
        }
        else if(category && !key){
           //clear whole category
           cache[category] = {};
        }
        else if(category && key){
           //clear specific category/key entry
           var cc = cache[category];
           delete cc[key];
        }
     }
     
    


})(jQuery);