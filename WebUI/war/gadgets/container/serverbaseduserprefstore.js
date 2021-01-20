/**
 * Server-based user preference store.
 * @constructor
 */
gadgets.ServerBasedUserPrefStore = function() {
  gadgets.UserPrefStore.call(this);
};

/**
 * Inherit the base gadgets properties and functions
 */
gadgets.ServerBasedUserPrefStore.inherits(gadgets.UserPrefStore);

/**
 * The local preferences cache.
 */
gadgets.ServerBasedUserPrefStore.prototype.prefCache = {};

/**
 * The metakey used to load/save the prefs to the metadata service.
 * Initialized by the <code>init<code> function.
 */
gadgets.ServerBasedUserPrefStore.prototype.metakey = "";

/**
 * Initializes the preference store by retrieving the prefs from the
 * server and caching then locally. This function must have been called
 * for the rest of the functions to work correctly.
 */
gadgets.ServerBasedUserPrefStore.prototype.init = function(metakey, callback)
{
   var self = this;
   this.metakey = metakey;
   jQuery.PercMetadataService.find(metakey, function(status, result){
          if(status == jQuery.PercServiceUtils.STATUS_SUCCESS)
          {
             if(result == null)
                result = {};
             var obj = result.metadata;
             var temp = null;
             if(!isEmpty(obj))
             {
                var exec = "temp = " + obj.data + ";";
                eval(exec);
                self.prefCache = temp.userprefs;
             }
             callback(jQuery.PercServiceUtils.STATUS_SUCCESS);
          } 
          else
          {
             callback(jQuery.PercServiceUtils.STATUS_ERROR);
          }         
       });  
}

/**
 * Get user prefs for a specific gadget.
 * @param gadget {object} the gadget object, cannot be <code>null</code>.
 * @return user prefs object which is simply a hash of name/value pairs
 * @type object
 */ 
gadgets.ServerBasedUserPrefStore.prototype.getPrefs = function(gadget) {
  var userPrefs = {};
  if(isEmpty(this.prefCache))
     return userPrefs;
  var mid = "mid_" + gadget.id;
  var prefs = this.prefCache[mid];
  if(typeof(prefs) != 'undefined')
  {
     for(var name in prefs)
     {
        userPrefs[name] = prefs[name];
     }
  }
  return userPrefs;
};

/**
 * Saves user prefs for the specified gadget. 
 * @param gadget {object} the gadget object, cannot be <code>null</code>.
 */ 
gadgets.ServerBasedUserPrefStore.prototype.savePrefs = function(gadget) {
  
  if(this.prefCache == null)
  {
     this.prefCache = {};
  }
  var prefs = {};
  for(var name in gadget.getUserPrefs()) {
     var v = gadget.getUserPrefValue(name);
     if(typeof(v) != 'undefined' && v != null && v != "")
        prefs[name] = gadget.getUserPrefValue(name);
  }
  this.prefCache["mid_" + gadget.id] = prefs;
  this._persistUserPrefs();     
}; 

/**
 * Remove the user prefs entry for the specified gadget. 
 * @param gadget {object} the gadget object, cannot be <code>null</code>.
 */
gadgets.ServerBasedUserPrefStore.prototype.removePrefs = function(gadget){
   if(isEmpty(this.getPrefs(gadget)))
      return;
   var count = 0;
   for( c in this.prefCache){count++;}
   if(count > 1)
   {
      delete this.prefCache["mid_" + gadget.id];
      this._persistUserPrefs();
   }
   else
   {
      var _self = this;
      jQuery.PercMetadataService.deleteEntry(this.metakey,         
         function(status, msg){
            if(status == jQuery.PercServiceUtils.STATUS_ERROR)
            {
              //Maybe log here when we have a good way to log                   
            }
            _self.prefCache = {};             
         });
   }   
}

/**
 * Persists all user prefs in the cache to the metadata service.
 */ 
gadgets.ServerBasedUserPrefStore.prototype._persistUserPrefs = function(){
   var self = this;
   var up = {"userprefs": this.prefCache};
   jQuery.PercMetadataService.save(this.metakey, up, function(status, result){
          if(status == jQuery.PercServiceUtils.STATUS_SUCCESS)
          {
             
             if(typeof(callback) == 'function')
                callback(jQuery.PercServiceUtils.STATUS_SUCCESS);
          }
          else
          {
             var defaultMsg = 
                jQuery.PercServiceUtils.extractDefaultErrorMessage(result.request);
             if(typeof(callback) == 'function')
                callback(jQuery.PercServiceUtils.STATUS_ERROR, defaultMsg);
          }
       });  
}

/**
 * Helper method to determine if an object is empty.
 */
function isEmpty(ob){
   for(var i in ob){ return false;}
   return true;
} 

/**
 * Create the pref store instance and set it on the container
 */
gadgets.Container.prototype.userPrefStore =
    new gadgets.ServerBasedUserPrefStore();