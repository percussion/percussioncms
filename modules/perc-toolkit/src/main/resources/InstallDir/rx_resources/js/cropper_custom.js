var jcrop_api_controls = {}

function on_change_update(c,target){
    var json_values = {
        "x" : c.x,
        "y" : c.y,
        "w"  : c.w,
        "h"  : c.h
    }
    $("input[name='"+ target+"_x']").val(c.x);
    $("input[name='"+ target+"_y']").val(c.y);
    $("input[name='"+ target+"_w']").val(c.w);
    $("input[name='"+ target+"_h']").val(c.h);
    resize_w = $("input[name='"+ target+"_resize_w']").val();
    resize_h = $("input[name='"+ target+"_resize_h']").val();
    if(resize_h.length != 0){
        json_values["resize_w"] = parseInt(resize_w);
    }
    if(resize_w.length != 0){
        json_values["resize_h"] = parseInt(resize_h);
    }
    $("input[name='"+ target+"']").val(JSON.stringify(json_values));
}

function bind_jcrop(target){
    json_values = null;
    try{
        json_values=JSON.parse($("input[name='"+ target+"']").val());
        $("input[name='"+ target+"_x']").val(json_values["x"]);
        $("input[name='"+ target+"_y']").val(json_values["y"]);
        $("input[name='"+ target+"_w']").val(json_values["w"]);
        $("input[name='"+ target+"_h']").val(json_values["h"]);
        if(json_values["resize_w"]){
            $("input[name='"+ target+"_resize_w']").val(json_values["resize_w"]);
        }
        if(json_values["resize_h"]){
            $("input[name='"+ target+"_resize_h']").val(json_values["resize_h"]);
        }
    }catch(e){
        //ignore
    }
    if(json_values!= null){
        $('#'+ target +' > img').Jcrop({
            onChange: function(c){
                on_change_update(c,target);
            },
            onSelect: function(c){
                on_change_update(c,target);
            },
            setSelect: [ json_values.x, json_values.y, json_values.x + json_values.w, json_values.y + json_values.h ]
        },function(){
          jcrop_api_controls[target] = this;
        });
    }else{
        $('#'+ target +' > img').Jcrop({
            onChange: function(c){
                on_change_update(c,target);
            },
            onSelect: function(c){
                on_change_update(c,target);
            }
        },function(){
          jcrop_api_controls[target] = this;
        });
    }
    
}

function init_edit_crop_control(obj,read_only){
    var object_name = obj.name.replace("_hash","_crop");
    var full = location.protocol+'//'+location.hostname+(location.port ? ':'+location.port: '');
    var image_path = full + "/Rhythmyx/assembly/aa?widget=hf&hash=" +obj.value;

    var target_items = $("input[name^='"+object_name+"']");
    var initialized = [];
    for( var j=0; j < target_items.length; j++){
        if($.inArray(target_items[j].name, initialized) == -1){
            
            var img = $('<img class="crop_image" />');
            img.attr('alt', image_path);
            img.attr('src', image_path);
            $('#'+target_items[j].name).html(img);
            bind_jcrop(target_items[j].name);
            initialized.push(target_items[j].name);
        }
        
    }

}


function init_crop_controls(){
    var images_bound = $("input[name$='_hash']");
    var initialized = [];
    for(var k=0; k<images_bound.length; k++){
        if($.inArray(images_bound[k].name, initialized) == -1){
            init_edit_crop_control(images_bound[k]);
            initialized.push(images_bound[k].name);
        }
    }
}

$(document).ready(function(){
    init_crop_controls();

    $('.image_cropper_inputs input').change(function(e){
        var target = $(this).parent().attr('data-param-name');

        var x = parseInt($("input[name='"+ target+"_x']").val());
        var y = parseInt($("input[name='"+ target+"_y']").val());
        var x2 = parseInt($("input[name='"+ target+"_w']").val()) + x;
        var y2 = parseInt($("input[name='"+ target+"_h']").val()) + y;

        jcrop_api_controls[target].setSelect([ x, y, x2, y2 ]);
    });
});