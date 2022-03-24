(function( $ ){ // START JQUERY WRAPPING
/* helper functions */

function tax_join_padded(db_values){
    return ' ' + db_values.join(' , ') + ' ';
}

function tax_html_escape(s){
    return $('<div/>').text(s).html();
}

function tax_current_tax_id(theTreeDivId){
    return $('#' + theTreeDivId + '_tax_id').val();
}

function tax_elements_to_ids(theNodes){
    return theNodes.map(function() { return this.id; }).get();
}

function tax_unprefix_id(theId){
    return theId.split("_")[theId.split("_").length - 1];
}

function tax_tree_wrap_id(theId){
    return 'treenode_' + tax_unprefix_id(theId);
}

function tax_auto_wrap_id(theId){
    return 'auto_' + tax_unprefix_id(theId);
}

function tax_get_save_load_id(theTreeDivId){
    // e.g. 'tax_fun_stuff_tree' => 'funs_stuff'
    var a = theTreeDivId.split('_');
    return a.slice(1,a.length -1).join('_');
}

function tax_get_init_ids(theTreeDivId){
    var theVal = tax_get_init_ids_as_string(theTreeDivId);
    if ($.trim(theVal).length > 0){
        return theVal.split(",");
    } else {
        return [];
    }
}

function tax_get_init_ids_as_string(theTreeDivId){
    // remove spaces because format is now  '1 , 2 , 7'
    return $.trim($('#' + tax_get_save_load_id(theTreeDivId)).val()).replace(/ /g, '');
}


/* tree selector functions */ 
function tax_get_unchecked_nodes(theTreeDivId){
    return $('#' + theTreeDivId).find('.jstree-unchecked');
}
// function tax_get_all_nodes(theTreeDivId){
//     // TODO make this better
//     return $('#' + theTreeDivId).find('.jstree-undetermined,.jstree-checked,.jstree-unchecked');
// }
function tax_get_checked_nodes(theTreeDivId){
    return $('#' + theTreeDivId).find('.jstree-undetermined,.jstree-checked');
}

function tax_get_li_a_text(the_li){
    return $($(the_li).children('a')[0]).text();
}


/* update functions */ 

function un_check_disabled_nodes(theTreeDivId){
    tax_get_checked_nodes(theTreeDivId).each(function() { 
        if (this.title == 'Disabled Taxon'){
            tax_uncheck_tree_node(theTreeDivId,this.id);
        }
    });
}

function tax_add_tag(theTreeDivId,id, name, skip_do_checkbox){
    var save_load_obj = $('#' + tax_get_save_load_id(theTreeDivId));
    var db_id = tax_unprefix_id(id);
    var db_values = tax_get_init_ids(theTreeDivId);
    var visual_tag_id = theTreeDivId + '_visual_tag_' + db_id;
    var not_in_array = $.inArray(db_id,db_values)<0;
    var not_in_tags = $('#' + visual_tag_id).length == 0;
    
    if (not_in_array || not_in_tags){
        
        if (not_in_array){
            db_values = $.merge(db_values,[db_id]);
            save_load_obj.val(tax_join_padded(db_values));
        }
        var del_link = '<a href="#" title="remove ' + name + ' tag" onclick="$().taxonomy(' + "'tax_remove_tag','" + theTreeDivId + "'" + ',' + "'" + db_id + "'" + ',false);return false;"><b>X</b></a>';
        var s = ' <span class="taxtag" id="' + visual_tag_id + '">' + name + ' ' + del_link + ' ' + '</span>';
        $('#' + theTreeDivId + '_tags').append(s);

        if (not_in_array){
            if (!skip_do_checkbox){
                tax_check_tree_node(theTreeDivId,db_id);
            }
        }
    
        
    
    }
    
    if (not_in_array && not_in_tags){
        $.get("/Rhythmyx/taxonomy/xmlprovider.htm", { 'ids' : db_id, 'action' : 'setInUse'} );
    }
    
}

function tax_remove_tag(theTreeDivId,id,skip_do_checkbox){
    var save_load_obj = $('#' + tax_get_save_load_id(theTreeDivId));
    var db_id = tax_unprefix_id(id);
    var db_values = tax_get_init_ids(theTreeDivId);

    if ($.inArray(db_id,db_values)>=0){
        db_values = jQuery.grep(db_values, function(value) {
            return value != db_id;
        });
        
        var visual_tag_id = theTreeDivId + '_visual_tag_' + db_id;
        
        $('#' + visual_tag_id).remove();
        
        save_load_obj.val(tax_join_padded(db_values));

        if (!skip_do_checkbox){
            tax_uncheck_tree_node(theTreeDivId,db_id);
        }
        
    }

}

function tax_rebuild_tags(theTreeDivId){
    tax_get_checked_nodes(theTreeDivId).each(function() { 
        tax_add_tag(theTreeDivId,this.id,tax_html_escape(tax_get_li_a_text(this)),true);
    });
    tax_get_unchecked_nodes(theTreeDivId).each(function() { 
        tax_remove_tag(theTreeDivId,this.id,true);
    });
}


/* name safe tree functions */
function tax_uncheck_tree_node(theTreeDivId,theId){
    $('#' + theTreeDivId).jstree("uncheck_node","#" + tax_tree_wrap_id(theId));
}
function tax_check_tree_node(theTreeDivId,theId){
    $('#' + theTreeDivId).jstree("check_node","#" + tax_tree_wrap_id(theId));
}


function tax_tree_search(theTreeDivId){
    $("#" + theTreeDivId).jstree("search", $('#' + theTreeDivId +  '_search').val());
}

function tax_init_tree(theTreeDivId){
    var a = tax_get_init_ids(theTreeDivId);
    for(i = 0; i < a.length; i++){
        tax_check_tree_node(theTreeDivId, a[i]);
    }
}

/* autocomplete functions */
function tax_wrap_title(t){
    var ret = jQuery.trim(t);
    ret = ret.replace(/\|/g, '&#13;&#10;');
    ret = ret.replace(/ /g, '&nbsp;');
    return ret;
}


function tax_autocomplete_rebuild(theTreeDivId){
    var the_url = '/Rhythmyx/taxonomy/xmlprovider.htm?action=getAutocompleteSearch&langID=1&taxID=' + tax_current_tax_id(theTreeDivId) + '&prefix=treenode_&exclude_ids=' + tax_get_init_ids_as_string(theTreeDivId);
    $( "#" + theTreeDivId + "_dropdown" ).autocomplete( "option", "source", the_url);
}

function tax_init_autocomplete(theTreeDivId){
    var theObj = $( "#" + theTreeDivId + "_dropdown" );
    theObj.autocomplete({
        source: '/Rhythmyx/taxonomy/xmlprovider.htm?action=getAutocompleteSearch&langID=1&taxID=' + tax_current_tax_id(theTreeDivId) + '&prefix=treenode_&exclude_ids=' + tax_get_init_ids_as_string(theTreeDivId),
        minLength: 3,
        select: function(event, ui) {
            tax_add_tag(theTreeDivId, ui.item.id, ui.item.value, false);
            tax_autocomplete_rebuild(theTreeDivId);
        }        
    });
}

// create accordion
function tax_create_accordion(theTreeDivId, tax_id){
    $("#" + theTreeDivId + "_accordion").bind( "accordionchangestart", function(event, ui) {
        if (ui.newHeader.text().indexOf('Browsing') > -1){
            $("#" + theTreeDivId).jstree('destroy').empty();
            tax_create_tree(theTreeDivId, tax_id);
        }
    }).accordion({ header: "h3" });
}

// create tree
function tax_create_tree(theTreeDivId, tax_id) {
$("#" + theTreeDivId).data("only_expand_children", false);
$("#" + theTreeDivId)
.bind("loaded.jstree", function(event, data) {
    $("#" + theTreeDivId).data("only_expand_children", true);
    $('#' + theTreeDivId + '_spinner').hide();
    tax_init_autocomplete(this.id);
    tax_init_tree(this.id);
    tax_rebuild_tags(theTreeDivId);
    // this is so that search works for trees that only have one top level parent
    if ($('#' + theTreeDivId + ' li').length == 1){
        $('#' + theTreeDivId).jstree('open_node','#' + $('#' + theTreeDivId + ' li').first().attr('id'))    
    }
})
.bind("check_node.jstree", function(event, data) {
    un_check_disabled_nodes(this.id);
    tax_autocomplete_rebuild(this.id);
    tax_rebuild_tags(this.id);
})
.bind("uncheck_node.jstree", function(event, data) {
    tax_autocomplete_rebuild(this.id);
    tax_rebuild_tags(this.id);
})
.bind("after_open.jstree", function(event, data) {
    tax_init_tree(this.id);
})
.bind("search.jstree", function (e, data) {
    if (data.rslt.nodes.length <= 0){
        alert('not found');
    }
})    
.jstree({
    "xml_data" : {
        "ajax" : {
            "cache" : false,
            "async" : true, 
            "url" : "/Rhythmyx/taxonomy/xmlprovider.htm?action=getXMLJSTreeLazy&exclude_disabled=true&prefix=treenode_&langID=1&taxID=" + tax_id + "&already_picked_node_ids=" + tax_get_init_ids_as_string(theTreeDivId),
            "data" : function (n) { 
                return {
                    "only_expand_children" : $("#" + theTreeDivId).data("only_expand_children"), 
                    "nodeID" : n.attr ? tax_unprefix_id(n.attr("id")) : '' 
                }; 
            }
        }
    },
    "core" : {"html_titles"    : true},
    "checkbox" : { "two_state" : true },
    "plugins" : [ "themes", "xml_data", "checkbox" , "search"],
    "search" : {
        "case_insensitive" : true,
        "search_method" : "jstree_text_contains",
        "ajax" : {
            "cache" : false,
            "url" : '/Rhythmyx/taxonomy/xmlprovider.htm?action=getJSTreeSearch&prefix=treenode_&exclude_disabled=true&langID=1&taxID=' + tax_id,
            "data" : function (str) {
                return { 
                    "q" : str 
                }; 
            }
        }
    }        
});
}

// TODO

// so we followed the direction in http://docs.jquery.com/Plugins/Authoring
// however right now we have to do $().taxonomy("method") instead of $.taxonomy("method")
// perhaps extend is really want we want
// http://stackoverflow.com/questions/1758370/calling-a-jquery-plugin-without-specifying-any-elements

var methods = {
  tax_create_accordion : function(theTreeDivId, tax_id) {tax_create_accordion(theTreeDivId, tax_id);},
  tax_create_tree : function(theTreeDivId, tax_id) {tax_create_tree(theTreeDivId, tax_id);},
  tax_tree_search : function( theTreeDivId ) {tax_tree_search(theTreeDivId);},
  tax_remove_tag : function(theTreeDivId,id,skip_do_checkbox) {tax_remove_tag(theTreeDivId,id,skip_do_checkbox);}
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
