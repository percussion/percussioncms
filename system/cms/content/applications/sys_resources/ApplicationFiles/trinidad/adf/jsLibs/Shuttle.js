var _shuttle_no_name="You must supply the shuttle's name to create a proxy";
var _shuttle_no_form_name_provided="A form name must be provided";
var _shuttle_no_form_available="This shuttle is not in a form";
function TrShuttleProxy(
a0,
a1
)
{
if(a0==(void 0))
{
alert(_shuttle_no_name);
this.shuttleName="";
this.formName="";
return;
}
this.shuttleName=a0;
this.formName="";
if(a1==(void 0))
{
var a2=document.forms.length;
var a3=a0+":leading";
for(var a4=0;a4<a2;a4++)
{
if(document.forms[a4][a3]!=(void 0))
{
this.formName=document.forms[a4].name;
break;
}
}
if(this.formName=="")
{
alert(shuttle_no_form_available);
return;
}
}
else
{
this.formName=a1;
}
}
TrShuttleProxy.prototype.getItems=function(
a5
)
{
if(a5==(void 0))
{
a5=true;
}
var a6=TrShuttleProxy._getListName(this.shuttleName,a5);
var a7=document.forms[this.formName].elements[a6];
var a8=new Array();
for(var a9=0;a9<a7.length-1;a9++)
{
a8[a9]=a7.options[a9];
}
return a8;
};
TrShuttleProxy.prototype.getSelectedItems=function(
a10
)
{
if(a10==(void 0))
{
a10=true;
}
var a11=TrShuttleProxy._getListName(this.shuttleName,a10);
var a12=document.forms[this.formName].elements[a11];
var a13=new Array();
var a14=0;
for(var a15=0;a15<a12.length-1;a15++)
{
if(a12.options[a15].selected)
{
a13[a14]=a12.options[a15];
a14++;
}
}
return a13;
};
TrShuttleProxy.prototype.getItemCount=function(
a16
)
{
if(a16==(void 0))
{
a16=true;
}
var a17=TrShuttleProxy._getListName(this.shuttleName,a16);
return document.forms[this.formName].elements[a17].length-1;
};
TrShuttleProxy.prototype.getSelectedItemCount=function(
a18
)
{
if(a18==(void 0))
{
a18=true;
}
var a19=TrShuttleProxy._getListName(this.shuttleName,a18);
var a20=document.forms[this.formName].elements[a19];
var a21=0;
for(var a22=0;a22<a20.length-1;a22++)
{
if(a20.options[a22].selected)
{
a21++;
}
}
return a21;
};
TrShuttleProxy.prototype.addItem=function(
a23,
a24,
a25,
a26,
a27
)
{
if(a26==(void 0))
{
a26="";
}
if(a25==(void 0))
{
a25="";
}
if(a27==(void 0))
{
a27="";
}
if(a23==(void 0))
{
a23=true;
}
var a28=TrShuttleProxy._getListName(this.shuttleName,a23);
if(a24==(void 0))
{
a24=document.forms[this.formName].elements[a28].length-1;
}
if(a24<0)
{
a24=0;
}
if(a24>document.forms[this.formName].elements[a28].length-1)
{
a24=document.forms[this.formName].elements[a28].length-1;
}
var a29=document.forms[this.formName].elements[a28];
a29.options[a29.length]=
new Option(a29.options[a29.length-1].text,
a29.options[a29.length-1].value,
false,
false);
for(var a30=a29.length-1;a30>a24;a30--)
{
a29.options[a30].text=a29.options[a30-1].text;
a29.options[a30].value=a29.options[a30-1].value;
a29.options[a30].selected=a29.options[a30-1].selected;
}
a29.options[a24].text=a25;
a29.options[a24].value=a26;
a29.options[a24].selected=false;
var a31=TrShuttleProxy._getDescArray(a28);
TrShuttleProxy._addDescAtIndex(a31,a27,a24);
TrShuttleProxy._makeList(this.formName,a28);
};
TrShuttleProxy.prototype.deleteItemByValue=function(
a32,
a33
)
{
if(a33==(void 0))
{
return;
}
var a34=TrShuttleProxy._getListName(this.shuttleName,a32);
var a35=document.forms[this.formName].elements[a34];
for(var a36=0;a36<a35.length-1;a36++)
{
var a37=a35.options[a36].value;
if(a37==a33)
{
var a38=TrShuttleProxy._getDescArray(a34);
TrShuttleProxy._deleteDescAtIndex(a38,a36);
TrShuttleProxy._clearDescAreas(this.formName,a34);
a35.options[a36]=null;
TrShuttleProxy._makeList(this.formName,a34);
return;
}
}
};
TrShuttleProxy.prototype.deleteSelectedItems=function(
a39
)
{
if(a39==(void 0))
{
a39=true;
}
var a40=TrShuttleProxy._getListName(this.shuttleName,a39);
var a41=document.forms[this.formName].elements[a40];
var a42=TrShuttleProxy._getSelectedIndexes(this.formName,a40);
for(var a43=a42.length;a43>=0;a43--)
{
a41.options[a42[a43]]=null;
}
var a44=TrShuttleProxy._getDescArray(a40);
TrShuttleProxy._deleteDescAtIndexes(a44,a42);
TrShuttleProxy._clearDescAreas(this.formName,a40);
TrShuttleProxy._makeList(this.formName,a40);
};
TrShuttleProxy.prototype.move=function(
a45,
a46
)
{
if(a46==(void 0))
{
a46=false;
}
if(a45==(void 0))
{
a45=true;
}
var a47=TrShuttleProxy._getListName(this.shuttleName,a45);
var a48=TrShuttleProxy._getListName(this.shuttleName,!a45);
if(a46)
{
TrShuttleProxy._moveAllItems(a47,a48,this.formName);
}
else
{
TrShuttleProxy._moveItems(a47,a48,this.formName);
}
};
TrShuttleProxy.prototype.reorderList=function(
a49,
a50,
a51
)
{
if(a51==(void 0))
{
a51=true;
}
if(a50==(void 0))
{
a50=false;
}
if(a49==(void 0))
{
a49=false;
}
var a52=TrShuttleProxy._getListName(this.shuttleName,a51);
if(!a50)
{
TrShuttleProxy._orderList(a49,a52,this.formName);
}
else
{
TrShuttleProxy._orderTopBottomList(a49,a52,this.formName);
}
};
TrShuttleProxy.prototype.reset=function()
{
TrShuttleProxy._resetItems(this.shuttleName,this.formName);
};
TrShuttleProxy._remove=function(a53,a54,a55)
{
var a56=a53.length;
if(a55>a56)
return;
for(var a57=a54;a57<a56;a57++)
{
if(a57<a56-a55)
a53[a57]=a53[a57+a55];
else
a53[a57]=void 0;
}
a53.length=a56-a55;
}
TrShuttleProxy._displayDesc=function(
a58,
a59
)
{
if(a59==(void 0))
{
alert(_shuttle_no_form_name_provided);
return;
}
if(a59.length==0)
{
alert(shuttle_no_form_available);
return;
}
var a60=document.forms[a59].elements[a58+':desc'];
if(a60==void(0))
{
return;
}
var a61=TrShuttleProxy._getDescArray(a58);
if(a61==(void 0)||a61.length==0)
{
return;
}
var a62=TrShuttleProxy._getSelectedIndexes(a59,a58);
if(a62.length==0)
{
a60.value="";
TrShuttleProxy._setSelected(a58,a62);
return;
}
var a63=TrShuttleProxy._getSelectedDesc(a58,a61,a62);
a60.value=a63;
TrShuttleProxy._setSelected(a58,a62);
}
TrShuttleProxy._getDescArray=function
(
a64
)
{
var a65=window[a64.replace(':','_')+'_desc'];
return a65;
}
TrShuttleProxy._getSelectedDesc=function
(
a66,
a67,
a68
)
{
var a69=TrShuttleProxy._getSelectedArray(a66);
if(a68.length==1)
return a67[a68[0]];
if(a68.length-a69.length!=1)
return"";
for(var a70=0;a70<a68.length;a70++)
{
if(a70>=a69.length||a69[a70]!=a68[a70])
return a67[a68[a70]];
}
return"";
}
TrShuttleProxy._getSelectedArray=function
(
a71
)
{
var a72=window[a71.replace(':','_')+'_sel'];
return a72;
}
TrShuttleProxy._setSelected=function
(
a73,
a74
)
{
var a75=TrShuttleProxy._getSelectedArray(a73);
if(a75!=(void 0))
{
var a76=a75.length;
TrShuttleProxy._remove(a75,0,a76);
for(var a77=0;a77<a74.length;a77++)
{
a75[a77]=a74[a77];
}
}
}
TrShuttleProxy._addDescAtIndex=function
(
a78,
a79,
a80
)
{
if(a78!=(void 0))
{
var a81=a78.length;
for(var a82=a81-1;a82>=a80;a82--)
{
a78[a82+1]=a78[a82];
}
a78[a80]=a79;
a78.length=a81+1;
}
}
TrShuttleProxy._deleteDescAtIndex=function
(
a83,
a84
)
{
if(a83!=(void 0))
TrShuttleProxy._remove(a83,a84,1);
}
TrShuttleProxy._deleteDescAtIndexes=function
(
a85,
a86
)
{
if(a85!=(void 0))
{
for(var a87=a86.length-1;a87>=0;a87--)
{
TrShuttleProxy._remove(a85,a86[a87],1);
}
}
}
TrShuttleProxy._clearDescAreas=function(
a88,
a89,
a90
)
{
var a91=document.forms[a88].elements[a89+':desc'];
var a92=document.forms[a88].elements[a90+':desc'];
if(a91!=void(0))
{
a91.value="";
}
if(a92!=void(0))
{
a92.value="";
}
}
TrShuttleProxy._moveItems=function(
a93,
a94,
a95
)
{
if(a95==(void 0))
{
a95=TrShuttleProxy._findFormNameContaining(a93);
}
if(a95.length==0)
{
alert(shuttle_no_form_available);
return;
}
var a96=document.forms[a95].elements[a93];
var a97=document.forms[a95].elements[a94];
if(a96==(void 0)||a97==(void 0))
return;
var a98=TrShuttleProxy._getSelectedIndexes(a95,a93);
if(a98.length==0)
{
if(_shuttle_no_items_selected.length>0)
alert(_shuttle_no_items_selected);
return;
}
var a99=TrShuttleProxy._getDescArray(a93);
var a100=TrShuttleProxy._getDescArray(a94);
a97.selectedIndex=-1;
var a101=a97.length-1;
var a102=a97.options[a101].text;
for(var a103=0;a103<a98.length;a103++)
{
var a104=a96.options[a98[a103]].text;
var a105=a96.options[a98[a103]].value;
if(a103==0)
{
a97.options[a101].text=a104;
a97.options[a101].value=a105;
}
else
{
a97.options[a101]=new Option(a104,a105,false,false);
}
if(a100!=(void 0)&&a99!=(void 0))
a100[a101]=a99[a98[a103]];
a97.options[a101].selected=true;
a101++;
}
a97.options[a101]=new Option(a102,"",false,false);
a97.options[a101].selected=false;
for(var a103=a98.length-1;a103>=0;a103--)
{
if(a99!=(void 0))
TrShuttleProxy._remove(a99,a98[a103],1);
a96.options[a98[a103]]=null;
}
a96.selectedIndex=-1;
TrShuttleProxy._clearDescAreas(a95,a93);
TrShuttleProxy._displayDesc(a94,a95);
TrShuttleProxy._makeList(a95,a93);
TrShuttleProxy._makeList(a95,a94);
}
TrShuttleProxy._moveAllItems=function(
a106,
a107,
a108
)
{
if(a108==(void 0))
{
a108=TrShuttleProxy._findFormNameContaining(a106);
}
var a109=document.forms[a108].elements[a106];
var a110=document.forms[a108].elements[a107];
var a111=
a110.options[document.forms[a108].elements[a107].length-1].text
var a112=a110.length-1;
var a113=TrShuttleProxy._getDescArray(a106);
var a114=TrShuttleProxy._getDescArray(a107);
if(a109.length>1)
{
var a115=a109.length
for(var a116=0;a116<a115-1;a116++)
{
var a117=a109.options[0].text;
var a118=a109.options[0].value;
a109.options[0]=null;
if(a116==0)
{
a110.options[a112].text=a117;
a110.options[a112].value=a118;
}
else
{
a110.options[a112]=new Option(a117,a118,false,false);
}
if(a114!=(void 0)&&a113!=(void 0))
a114[a112]=a113[a116];
a112++;
}
a110.options[a112]=new Option(a111,"",false,false);
a110.options[a112].selected=false;
if(a113!=(void 0))
{
var a119=a113.length;
TrShuttleProxy._remove(a113,0,a119);
}
a109.selectedIndex=-1;
a110.selectedIndex=-1;
TrShuttleProxy._clearDescAreas(a108,a106,a107);
TrShuttleProxy._makeList(a108,a106);
TrShuttleProxy._makeList(a108,a107);
}
else if(_shuttle_no_items.length>0)
{
alert(_shuttle_no_items);
}
}
TrShuttleProxy._orderList=function(
a120,
a121,
a122
)
{
if(a122==(void 0))
{
a122=TrShuttleProxy._findFormNameContaining(a121);
}
var a123=document.forms[a122].elements[a121];
var a124=TrShuttleProxy._getSelectedIndexes(a122,a121);
if(a124.length==0)
{
if(_shuttle_no_items_selected.length>0)
alert(_shuttle_no_items_selected);
return;
}
var a125=TrShuttleProxy._getDescArray(a121);
var a126=a124.length-1;
while(a126>=0)
{
var a127=a124[a126];
var a128=a127;
var a129=a126;
while((a129>0)&&((a124[a129]-
a124[a129-1])==1))
{
a129--;
a128--;
}
if(a120==0)
{
if(a128!=0)
{
var a130=a123.options[a128-1].text;
var a131=a123.options[a128-1].value;
if(a125!=(void 0))
var a132=a125[a128-1];
for(var a133=a128;a133<=a127;a133++)
{
a123.options[a133-1].text=a123.options[a133].text;
a123.options[a133-1].value=a123.options[a133].value;
a123.options[a133-1].selected=true;
if(a125!=(void 0))
a125[a133-1]=a125[a133];
}
a123.options[a127].text=a130;
a123.options[a127].value=a131;
a123.options[a127].selected=false;
if(a125!=(void 0))
a125[a127]=a132;
}
}
else
{
if(a127!=a123.length-2)
{
var a130=a123.options[a127+1].text;
var a131=a123.options[a127+1].value;
if(a125!=(void 0))
var a132=a125[a127+1];
for(var a133=a127;a133>=a128;a133--)
{
a123.options[a133+1].text=a123.options[a133].text;
a123.options[a133+1].value=a123.options[a133].value;
a123.options[a133+1].selected=true;
if(a125!=(void 0))
a125[a133+1]=a125[a133];
}
a123.options[a128].text=a130;
a123.options[a128].value=a131;
a123.options[a128].selected=false;
if(a125!=(void 0))
a125[a128]=a132;
}
}
a126=a129-1;
}
TrShuttleProxy._displayDesc(a121,a122);
TrShuttleProxy._makeList(a122,a121);
}
TrShuttleProxy._orderTopBottomList=function(
a134,
a135,
a136
)
{
if(a136==(void 0))
{
a136=TrShuttleProxy._findFormNameContaining(a135);
}
var a137=document.forms[a136].elements[a135];
var a138=TrShuttleProxy._getSelectedIndexes(a136,a135);
if(a138.length==0)
{
if(_shuttle_no_items_selected.length>0)
alert(_shuttle_no_items_selected);
return;
}
var a139=TrShuttleProxy._getDescArray(a135);
var a140=new Array();
var a141=new Array();
var a142=new Array();
var a143=new Array();
var a144=0;
if(a134==0)
{
var a145=0;
var a144=0;
for(var a146=0;
a146<a138[a138.length-1];
a146++)
{
if(a146!=a138[a145])
{
a142[a144]=a137.options[a146].text;
a143[a144]=a137.options[a146].value;
if(a139!=(void 0))
a140[a144]=a139[a146];
a144++
}
else
{
if(a139!=(void 0))
a141[a145]=a139[a146];
a145++;
}
}
if(a139!=(void 0))
a141[a145]=a139[a146];
for(var a147=0;a147<a138.length;a147++)
{
a137.options[a147].text=a137.options[a138[a147]].text;
a137.options[a147].value=a137.options[a138[a147]].value;
a137.options[a147].selected=true;
if(a139!=(void 0))
a139[a147]=a141[a147];
}
for(var a148=0;a148<a142.length;a148++)
{
a137.options[a147].text=a142[a148];
a137.options[a147].value=a143[a148];
a137.options[a147].selected=false;
if(a139!=(void 0))
a139[a147]=a140[a148];
a147++
}
}
else
{
var a145=1;
var a144=0;
if(a139!=(void 0))
a141[0]=a139[a138[0]];
for(var a149=a138[0]+1;
a149<=a137.length-2;
a149++)
{
if((a145==a138.length)||
(a149!=a138[a145]))
{
a142[a144]=a137.options[a149].text;
a143[a144]=a137.options[a149].value;
if(a139!=(void 0))
a140[a144]=a139[a149];
a144++;
}
else
{
if(a139!=(void 0))
a141[a145]=a139[a149];
a145++;
}
}
var a148=a137.length-2;
for(var a147=a138.length-1;a147>=0;a147--)
{
a137.options[a148].text=a137.options[a138[a147]].text;
a137.options[a148].value=a137.options[a138[a147]].value;
a137.options[a148].selected=true;
if(a139!=(void 0))
a139[a148]=a141[a147];
a148--;
}
for(var a147=a142.length-1;a147>=0;a147--)
{
a137.options[a148].text=a142[a147];
a137.options[a148].value=a143[a147];
a137.options[a148].selected=false;
if(a139!=(void 0))
a139[a148]=a140[a147];
a148--
}
}
TrShuttleProxy._displayDesc(a135,a136);
TrShuttleProxy._makeList(a136,a135);
}
TrShuttleProxy._getSelectedIndexes=function(
a150,
a151
)
{
var a152=document.forms[a150].elements[a151];
var a153=new Array();
var a154=0;
for(var a155=0;a155<a152.length-1;a155++)
{
if(a152.options[a155].selected)
{
a153[a154]=a155;
a154++;
}
}
return a153;
}
TrShuttleProxy._findFormNameContaining=function(
a156
)
{
var a157=document.forms.length;
for(var a158=0;a158<a157;a158++)
{
if(document.forms[a158][a156]!=(void 0))
{
return document.forms[a158].name;
}
}
return"";
}
TrShuttleProxy._makeList=function(
a159,
a160
)
{
var a161=document.forms[a159].elements[a160];
if(a161==null)
return;
var a162="";
for(var a163=0;a163<a161.length-1;a163++)
{
if(a161.options[a163].value.length>0)
{
a162=a162+
TrShuttleProxy._trimString(a161.options[a163].value)
+';';
}
else
{
a162=a162+
TrShuttleProxy._trimString(a161.options[a163].text)
+';';
}
}
document.forms[a159].elements[a160+':items'].value=a162;
}
TrShuttleProxy._trimString=function(
a164
)
{
var a165=a164.length-1;
if(a164.charAt(a165)!=' ')
{
return a164;
}
while((a164.charAt(a165)==' ')&&(a165>0))
{
a165=a165-1;
}
a164=a164.substring(0,a165+1);
return a164;
}
TrShuttleProxy._getListName=function(
a166,
a167
)
{
var a168=(a167)?a166+":leading":
a166+":trailing";
return a168;
}
TrShuttleProxy._resetItems=function(
a169,
a170)
{
if(a170==(void 0))
{
a170=TrShuttleProxy._findFormNameContaining(from);
}
if(a170.length==0)
{
alert(shuttle_no_form_available);
return;
}
leadingListName=TrShuttleProxy._getListName(a169,true);
trailingListName=TrShuttleProxy._getListName(a169,false);
var a171=document.forms[a170].elements[leadingListName];
var a172=document.forms[a170].elements[trailingListName];
var a173=TrShuttleProxy._getOriginalLists(a169,a170);
var a174=a173.leading;
var a175=a173.trailing;
var a176=TrShuttleProxy._getDescArray(leadingListName);
var a177=TrShuttleProxy._getDescArray(trailingListName);
TrShuttleProxy._resetToOriginalList(a174,a176,a171);
TrShuttleProxy._resetToOriginalList(a175,a177,a172);
TrShuttleProxy._makeList(a170,leadingListName);
TrShuttleProxy._makeList(a170,trailingListName);
return false;
}
TrShuttleProxy._getOriginalLists=function
(
a178,
a179
)
{
var a180=window['_'+a179+'_'+a178+'_orig'];
return a180;
}
TrShuttleProxy._resetToOriginalList=function
(
a181,
a182,
a183
)
{
if(a181==(void 0)||a183==(void 0))
return;
a183.selectedIndex=a181.selectedIndex;
var a184=0;
for(;a184<a181.options.length;a184++)
{
var a185=a181.options[a184].text;
var a186=a181.options[a184].value;
var a187=a181.options[a184].defaultSelected;
var a188=a181.options[a184].selected;
{
a183.options[a184]=new Option(a185,a186,
a187,a188);
a183.options[a184].defaultSelected=a187;
a183.options[a184].selected=a188;
}
if(a182!=(void 0))
a182[a184]=a181.descriptions[a184];
}
var a189=a183.options.length-1;
while(a189>=a184)
{
if(a182!=(void 0))
a182[a189]=null;
a183.options[a189]=null;
a189--;
}
}
TrShuttleProxy._copyLists=function(a190,a191)
{
if(a191==(void 0))
{
a191=TrShuttleProxy._findFormNameContaining(from);
}
if(a191.length==0)
{
alert(shuttle_no_form_available);
return;
}
var a192=new Object();
a192.leading=TrShuttleProxy._copyList(TrShuttleProxy._getListName(a190,true),a191);
a192.trailing=TrShuttleProxy._copyList(TrShuttleProxy._getListName(a190,false),a191);
return a192;
}
TrShuttleProxy._copyList=function(a193,a194)
{
if(a194==(void 0)||a193==(void 0))
return;
var a195=document.forms[a194].elements[a193];
if(a195==null)
return;
var a196=TrShuttleProxy._getDescArray(a193);
var a197=new Object();
a197.selectedIndex=a195.selectedIndex;
a197.options=new Array();
a197.descriptions=new Array();
for(var a198=0;a198<a195.options.length;a198++)
{
a197.options[a198]=new Option(a195.options[a198].text,
a195.options[a198].value,
a195.options[a198].defaultSelected,
a195.options[a198].selected);
a197.options[a198].defaultSelected=a195.options[a198].defaultSelected;
a197.options[a198].selected=a195.options[a198].selected;
if(a196!=null)
a197.descriptions[a198]=a196[a198];
}
return a197;
}
