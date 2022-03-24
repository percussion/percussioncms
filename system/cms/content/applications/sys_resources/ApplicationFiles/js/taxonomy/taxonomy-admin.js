(function( $ ){ // START JQERY WRAPPING

/* helper functions */
function tax_admin_elements_to_ids(theNodes){
    return theNodes.map(function() { return this.id; }).get();
}

function tax_admin_unprefix_id(theId){
    return theId.split("_")[theId.split("_").length - 1];
}

function tax_admin_tree_wrap_id(theId,theTreeDivId){
    return theTreeDivId + '_' + tax_admin_unprefix_id(theId);
}

function tax_admin_get_save_load_id(theTreeDivId){
    return theTreeDivId + "_ids";
}

function tax_admin_get_init_ids(theTreeDivId){
    var theVal = $('#' + tax_admin_get_save_load_id(theTreeDivId)).val();
    if ($.trim(theVal).length > 0){
        return theVal.split(",");
    } else {
        return [];
    }
}

/* tree selector functions */ 
function tax_admin_get_unchecked_nodes(theTreeDivId){
    return $('#' + theTreeDivId).find('.jstree-unchecked');
}
function tax_admin_get_all_nodes(theTreeDivId){
    // TODO make this better
    return $('#' + theTreeDivId).find('.jstree-undetermined,.jstree-checked,.jstree-unchecked');
}
function tax_admin_get_checked_nodes(theTreeDivId){
    return $('#' + theTreeDivId).find('.jstree-undetermined,.jstree-checked');
}

/* update functions */ 

function tax_admin_rebuild_tags(theTreeDivId){
    // update hidden save/load field
    $('#' + tax_admin_get_save_load_id(theTreeDivId)).val( tax_admin_get_checked_nodes(theTreeDivId).map(function() { return tax_admin_unprefix_id(this.id); }).get().join(','));
}

function tax_admin_reset(theTreeDivId){
    $('#' + theTreeDivId).jstree("uncheck_all");
    $('#' + theTreeDivId).jstree("close_all");
    tax_admin_rebuild_tags(theTreeDivId);
}


/* name safe tree functions */
function tax_admin_uncheck_tree_node(theTreeDivId,theId){
    $('#' + theTreeDivId).jstree("uncheck_node","#" + tax_admin_tree_wrap_id(theId,theTreeDivId));
}
function tax_admin_check_tree_node(theTreeDivId,theId){
    //alert("#" + tax_admin_tree_wrap_id(theId,theTreeDivId));
    $('#' + theTreeDivId).jstree("check_node","#" + tax_admin_tree_wrap_id(theId,theTreeDivId));
}
function tax_admin_open_tree_node(theTreeDivId,theId){
    $('#' + theTreeDivId).jstree("open_node","#" + tax_admin_tree_wrap_id(theId,theTreeDivId));
}




function tax_admin_tree_search_on_ajax_tree(theTreeDivId, search_term, open_checked){
    var the_tree = $('#' + theTreeDivId);
    //var a = the_tree.jstree('get_settings');
    //a.xml_data.ajax.async = false;
    //var a = the_tree.jstree('set_settings',a);
    
    //$.jstree._reference('#' + theTreeDivId)._get_settings().xml_data.ajax.async = false
    
    //$('#' + theTreeDivId).jstree("open_all");
    //$('#' + theTreeDivId).jstree("close_all");
    //if (open_checked == true){
    //    tax_admin_tree_open_checked(theTreeDivId);
    //}
    $('#' + theTreeDivId).jstree("search", search_term);
    //$('#' + theTreeDivId).jstree("open_all");
}

function tax_admin_tree_open_checked(theTreeDivId){
    var a = tax_admin_elements_to_ids(tax_admin_get_checked_nodes(theTreeDivId));
    for(i = 0; i < a.length; i++){
        tax_admin_open_tree_node(theTreeDivId, a[i]);
    }    
}

function tax_admin_init_tree(theTreeDivId){
    var a = tax_admin_get_init_ids(theTreeDivId);
    for(i = 0; i < a.length; i++){
        tax_admin_open_tree_node(theTreeDivId, a[i]);
        tax_admin_check_tree_node(theTreeDivId, a[i]);
    }    
}

function tax_admin_un_check_disabled_nodes(theTreeDivId){
    tax_admin_get_checked_nodes(theTreeDivId).each(function() { 
        if (this.title == 'Disabled Taxon'){
            tax_admin_uncheck_tree_node(theTreeDivId,this.id);
        }
    });
}

// TODO

// so we followed the direction in http://docs.jquery.com/Plugins/Authoring
// however right now we have to do $().taxonomy("method") instead of $.taxonomy("method")
// perhaps extend is really want we want
// http://stackoverflow.com/questions/1758370/calling-a-jquery-plugin-without-specifying-any-elements

var methods = {
  tax_admin_init_tree : function( theTreeDivId ) {tax_admin_init_tree(theTreeDivId);},
  tax_admin_rebuild_tags : function( theTreeDivId ) {tax_admin_rebuild_tags(theTreeDivId);},
  tax_admin_un_check_disabled_nodes : function( theTreeDivId ) {tax_admin_un_check_disabled_nodes(theTreeDivId);},
  tax_admin_unprefix_id : function( theId ) {return tax_admin_unprefix_id(theId);}, // TODO note the return here... aaron wasted some time cause that was missing!!!
  tax_admin_tree_search_on_ajax_tree : function( theTreeDivId , search_term, open_checked) {tax_admin_tree_search_on_ajax_tree(theTreeDivId, search_term, open_checked);}
};

$.fn.taxonomy = function( method ) {
  
  // Method calling logic
  if ( methods[method] ) {
    return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
  } else if ( typeof method === 'object' || ! method ) {
    return methods.init.apply( this, arguments );
  } else {
    $.error( 'Method ' +  method + ' does not exist on jQuery.taxonomy' );
  }    

};

})( jQuery ); // END JQUERY WRAPPING