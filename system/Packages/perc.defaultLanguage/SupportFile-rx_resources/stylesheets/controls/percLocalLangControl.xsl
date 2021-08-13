<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
        <!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES_Latin_1_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLlat1x.ent">
        %HTMLlat1;
        <!ENTITY % HTMLsymbol PUBLIC "-//W3C//ENTITIES_Symbols_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLsymbolx.ent">
        %HTMLsymbol;
        <!ENTITY % HTMLspecial PUBLIC "-//W3C//ENTITIES_Special_for_XHTML//EN" "https://www.percussion.com/DTD/HTMLspecialx.ent">
        %HTMLspecial;
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/XSL/Transform/1.0" xmlns:psxctl="URN:percussion.com/control"
                xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n"
                extension-element-prefixes="psxi18n">
	<xsl:template match="/"/>
	<!--
     percLocalLanguage
 -->
	<psxctl:ControlMeta name="percLocalLangControl" dimension="single" choiceset="none">>
		<psxctl:Description>Provides UI for associating sites with Locales</psxctl:Description>
		<psxctl:ParamList>
         <psxctl:Param name="id" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="class" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
            <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="style" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
         </psxctl:Param>
         <psxctl:Param name="columncount" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the number of column(s) displayed.</psxctl:Description>
            <psxctl:DefaultValue>1</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="columnwidth" datatype="String" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the column in pixels or percentage.</psxctl:Description>
            <psxctl:DefaultValue>100%</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_width" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the width of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>400</psxctl:DefaultValue>
         </psxctl:Param>
         <psxctl:Param name="dlg_height" datatype="Number" paramtype="generic">
            <psxctl:Description>This parameter specifies the height of the dialog box that is opened during field editing in Active Assembly.</psxctl:Description>
            <psxctl:DefaultValue>200</psxctl:DefaultValue>
         </psxctl:Param>
        </psxctl:ParamList>
		<psxctl:AssociatedFileList>
            <psxctl:FileDescriptor name="all.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../../cm/jslib/profiles/3x/libraries/fontawesome/css/all.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor> 
            <psxctl:FileDescriptor name="percLocalLang.css" type="css" mimetype="text/css">
                <psxctl:FileLocation>../rx_resources/widgets/percLocalLang/css/percLocalLang.css</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="percLocalLang.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../rx_resources/widgets/percLocalLang/js/percLocalLang.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_constants.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_path_constants.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="perc_path_manager.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_path_manager.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>            
            <psxctl:FileDescriptor name="perc_utils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/plugins/perc_utils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercSiteService.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercSiteService.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercSiteService.js" type="script" mimetype="text/javascript">
                    <psxctl:FileLocation>../../cm/services/PercPathService.js</psxctl:FileLocation>
                    <psxctl:Timestamp/>
            </psxctl:FileDescriptor>
            <psxctl:FileDescriptor name="PercServiceUtils.js" type="script" mimetype="text/javascript">
                <psxctl:FileLocation>../../cm/services/PercServiceUtils.js</psxctl:FileLocation>
                <psxctl:Timestamp/>
            </psxctl:FileDescriptor>

		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='percLocalLangControl']" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-local-lang" id="{@paramName}">
    <h2>International SEO - Individual page language alternates selection</h2>
    <span id="perc-help-toggle" class="perc-toggle-help fa fa-question-circle" title="Click to toggle help"></span><label for="perc-help-toggle">Click to toggle help.</label>
<div class="perc-help">
    This widget is used to augment the beharior of the Defaul Language widget which scans your sites to automatically index and add alteranate language page link references to your page. 
    This widget will allow you to manually selecte alternate language pages that may not be automatically detected.
    <br />  
    <br />  
    Edit this widget at the page level to select specific alternate language versions of this page.
    Add a reference to the table below for each Page, Language, and Region combination. Language is always required.
    <br />  
    <br />  
    When the Region or Country is selected, Search Engines will only return results from the international site to users speaking that language in that region. 
    Otherwise UK English content might be returned before US English content on US Search Engines and visa versa. 
    <br />
    <br />
    Tips:
    <ul>
        <li>Always choose language and country when you want search engines to return search results only in that region.</li>
        <li>Site Names should match the <a href="https://support.google.com/webmasters/answer/44231?hl=en" target="_blank" rel="noopener noreferrer">Preferred Domain Name</a> for the site.</li>
        <li>Click here for more information: <a href="https://support.google.com/webmasters/answer/189077?hl=en" target="_blank" rel="noopener noreferrer">rel=alternate href-lang=x</a></li>
    </ul>
</div>
<div id="perc-local-lang-editor" class="perc-local-lang-editor">
    <span class="perc-table-add fa fa-plus-square"></span>
<table class="perc-table">
    <tbody>
        <tr><th>Protocol*</th><th>Page Selector*</th><th>Language*</th><th>Region/Country</th><th>Default</th><th></th><th></th></tr>
    <!-- This is the template table row -->
    <tr class="hide perc-local-lang-row" role="presentation">
        <td>
            <select id="perc-lang-protocol" class="perc-lang-protocol">
                <option value="http" title="http">http</option>
                <option value="https" title="https">https</option>
            </select>
        </td>
        <td><div id="perc-page-select" class="perc-page-select">
            <input type="text" name="pageSelections"  id="perc-content-page-selections-0" class="perc-content-edit-page-selections" data-page-id="false" title="{Value}" value="{Value}" readonly="readonly" style="background-color:#E6E6E9;overflow: hidden;text-overflow: ellipsis;">
                <xsl:if test="@accessKey!=''">
                    <xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
                </xsl:if>
                <xsl:call-template name="parametersToAttributes">
                    <xsl:with-param name="controlClassName" select="'sys_PagePath'"/>
                    <xsl:with-param name="controlNode" select="."/>
                </xsl:call-template>
            </input>
            <input type="button" for="perc-content-page-selections-0" class="perc-page-field-select-button" value="Browse"/>
        </div></td>
        <td><select id="perc-lang-list" class="perc-lang-list">
            <option value="ab" title="аҧсуа бызшәа, аҧсшәа">Abkhaz</option>
            <option value="aa" title="Afaraf">Afar</option>
            <option value="af" title="Afrikaans">Afrikaans</option>
            <option value="ak" title="Akan">Akan</option> 	
            <option value="sq" title="Shqip">Albanian</option>
            <option value="am" title="አማርኛ">Amharic</option>
            <option value="ar" title="العربية">Arabic</option>
            <option value="an" title="aragonés">Aragonese</option>
            <option value="hy" title="Հայերեն">Armenian</option>
            <option value="as" title="অসমীয়া">Assamese</option>                                                 	
            <option value="av" title="авар мацӀ, магӀарул мацӀ">Avaric</option>
            <option value="ae" title="avesta">Avestan</option>
            <option value="ay" title="aymar aru">Aymara</option>
            <option value="az" title="azərbaycan dili">Azerbaijani</option>
            <option value="bm" title="bamanankan">Bambara</option>
            <option value="ba" title="башҡорт теле">Bashkir</option>
            <option value="eu" title="euskara, euskera">Basque</option>
            <option value="be" title="беларуская мова">Belarusian</option>
            <option value="bn" title="বাংলা">Bengali/Bangla</option>                                    
            <option value="bh" title="भोजपुरी">Bihari</option>
            <option value="bi" title="Bislama">Bislama</option>
            <option value="bs" title="bosanski jezik">Bosnian</option>
            <option value="br" title="brezhoneg">Breton</option>
            <option value="bg" title="български език">Bulgarian</option>
            <option value="my" title="ဗမာစာ">Burmese</option>
            <option value="ca" title="català">Catalan</option>
            <option value="ch" title="Chamoru">Chamorro</option>
            <option value="ce" title="нохчийн мотт">Chechen</option>
            <option value="ny" title="chiCheŵa, chinyanja">Chichewa, Chewa, Nyanja</option>
            <option value="zh" title="中文 (Zhōngwén), 汉语, 漢語">Chinese</option>
            <option value="cv" title="чӑваш чӗлхи">Chuvash</option>
            <option value="kw" title="Kernewek">Cornish</option>
            <option value="co" title="corsu, lingua corsa">Corsican</option>
            <option value="cr" title="ᓀᐦᐃᔭᐍᐏᐣ">Cree</option>
            <option value="hr" title="hrvatski jezik">Croatian</option>
            <option value="cs" title="čeština, český jazyk">Czech</option>
            <option value="da" title="dansk">Danish</option>
            <option value="dv" title="ދިވެހި">Divehi, Dhivehi, Maldivian</option>
            <option value="nl" title="Nederlands, Vlaams">Dutch</option>
            <option value="dz" title="རྫོང་ཁ">Dzongkha</option>
            <option value="en" title="English">English</option>
            <option value="eo" title="Esperanto">Esperanto</option>
            <option value="et" title="eesti, eesti keel">Estonian</option>
            <option value="ee" title="Eʋegbe">Ewe</option>
            <option value="fo" title="føroyskt">Faroese</option>
            <option value="fj" title="vosa Vakaviti">Fijian</option>
            <option value="fi" title="suomi, suomen kieli">Finnish</option>
            <option value="fr" title="français, langue française">French</option>
            <option value="ff" title="Fulfulde, Pulaar, Pular">Fula, Fulah, Pulaar, Pular</option>
            <option value="gl" title="galego">Galician</option>
            <option value="ka" title="ქართული">Georgian</option>
            <option value="de" title="Deutsch">German</option>
            <option value="el" title="ελληνικά">Greek (modern)</option>
            <option value="gn" title="Avañe'ẽ">Guaraní</option>
            <option value="gu" title="ગુજરાતી">Gujarati</option>
            <option value="ht" title="Kreyòl ayisyen">Haitian, Haitian Creole</option>
            <option value="ha" title="هَوُسَ">Hausa</option>
            <option value="he" title="עברית">Hebrew (modern)</option>
            <option value="hz" title="Otjiherero">Herero</option>
            <option value="hi" title="हिन्दी, हिंदी">Hindi</option>
            <option value="ho" title="Hiri Motu">Hiri Motu</option>
            <option value="hu" title="magyar">Hungarian</option>
            <option value="ia" title="Interlingua">Interlingua</option>
            <option value="id" title="Bahasa Indonesia">Indonesian</option>
            <option value="ie" title="Interlingue">Interlingue</option>
            <option value="ga" title="Gaeilge">Irish</option>
            <option value="ig" title="Asụsụ Igbo">Igbo</option>
            <option value="ik" title="Iñupiaq, Iñupiatun">Inupiaq</option>
            <option value="io" title="Ido">Ido</option>
            <option value="is" title="Íslenska">Icelandic</option>
            <option value="it" title="italiano">Italian</option>
            <option value="iu" title="ᐃᓄᒃᑎᑐᑦ">Inuktitut</option>
            <option value="ja" title="日本語 (にほんご)">Japanese</option>
            <option value="jv" title="basa Jawa">Javanese</option>
            <option value="kl" title="kalaallisut, kalaallit oqaasii">Kalaallisut, Greenlandic</option>
            <option value="kn" title="ಕನ್ನಡ">Kannada</option>
            <option value="kr" title="Kanuri">Kanuri</option>
            <option value="ks" title="कश्मीरी, كشميري‎">Kashmiri</option>
            <option value="kk" title="қазақ тілі">Kazakh</option>
            <option value="km" title="ខ្មែរ, ខេមរភាសា, ភាសាខ្មែរ">Khmer</option>
            <option value="ki" title="Gĩkũyũ">Kikuyu, Gikuyu</option>
            <option value="rw" title="Ikinyarwanda">Kinyarwanda</option>
            <option value="ky" title="Кыргызча, Кыргыз тили">Kyrgyz</option>
            <option value="kv" title="коми кыв">Komi</option>
            <option value="kg" title="Kikongo">Kongo</option>
            <option value="ko" title="한국어, 조선어">Korean</option>
            <option value="ku" title="Kurdî, كوردی‎">Kurdish</option>
            <option value="kj" title="Kuanyama">Kwanyama, Kuanyama</option>
            <option value="la" title="latine, lingua latina">Latin</option>
            <option value="lb" title="Lëtzebuergesch">Luxembourgish, Letzeburgesch</option>
            <option value="lg" title="Luganda">Ganda</option>
            <option value="li" title="Limburgs">Limburgish, Limburgan, Limburger</option>
            <option value="ln" title="Lingála">Lingala</option>
            <option value="lo" title="ພາສາລາວ">Lao</option>
            <option value="lt" title="lietuvių kalba">Lithuanian</option>
            <option value="lu" title="Tshiluba">Luba-Katanga</option>
            <option value="lv" title="latviešu valoda">Latvian</option>
            <option value="gv" title="Gaelg, Gailck">Manx</option>
            <option value="mk" title="македонски јазик">Macedonian</option>
            <option value="mg" title="fiteny malagasy">Malagasy</option>
            <option value="ms" title="bahasa Melayu, بهاس ملايو‎">Malay</option>
            <option value="ml" title="മലയാളം">Malayalam</option>
            <option value="mt" title="Malti">Maltese</option>
            <option value="mi" title="te reo Māori">Māori</option>
            <option value="mr" title="मराठी">Marathi (Marāṭhī)</option>
            <option value="mh" title="Kajin M̧ajeļ">Marshallese</option>
            <option value="mn" title="Монгол хэл">Mongolian</option>
            <option value="na" title="Dorerin Naoero">Nauruan</option>
            <option value="nv" title="Diné bizaad">Navajo, Navaho</option>
            <option value="nd" title="isiNdebele">Northern Ndebele</option>
            <option value="ne" title="नेपाली">Nepali</option>
            <option value="ng" title="Owambo">Ndonga</option>
            <option value="nb" title="Norsk bokmål">Norwegian Bokmål</option>
            <option value="nn" title="Norsk nynorsk">Norwegian Nynorsk</option>
            <option value="no" title="Norsk">Norwegian</option>
            <option value="ii" title="ꆈꌠ꒿ Nuosuhxop">Nuosu</option>
            <option value="nr" title="isiNdebele">Southern Ndebele</option>
            <option value="oc" title="occitan, lenga d'òc">Occitan</option>
            <option value="oj" title="ᐊᓂᔑᓈᐯᒧᐎᓐ">Ojibwe, Ojibwa</option>
            <option value="cu" title="ѩзыкъ словѣньскъ">Old Church Slavonic, Church Slavonic, Old Bulgarian</option>
            <option value="om" title="Afaan Oromoo">Oromo</option>
            <option value="or" title="ଓଡ଼ିଆ">Oriya</option>
            <option value="os" title="ирон æвзаг">Ossetian, Ossetic</option>
            <option value="pa" title="ਪੰਜਾਬੀ, پنجابی‎">Panjabi, Punjabi</option>
            <option value="pi" title="पाऴि">Pāli</option>
            <option value="fa" title="فارسی">Persian (Farsi)</option>
            <option value="pl" title="język polski, polszczyzna">Polish</option>
            <option value="ps" title="پښتو">Pashto, Pushto</option>
            <option value="pt" title="português">Portuguese</option>
            <option value="qu" title="Runa Simi, Kichwa">Quechua</option>
            <option value="rm" title="rumantsch grischun">Romansh</option>
            <option value="rn" title="Ikirundi">Kirundi</option>
            <option value="ro" title="limba română">Romanian</option>
            <option value="ru" title="Русский">Russian</option>
            <option value="sa" title="संस्कृतम्">Sanskrit (Saṁskṛta)</option>
            <option value="sc" title="sardu">Sardinian</option>
            <option value="sd" title="सिन्धी, سنڌي، سندھی">Sindhi</option>
            <option value="se" title="Davvisámegiella">Northern Sami</option>
            <option value="sm" title="gagana fa'a Samoa">Samoan</option>
            <option value="sg" title="yângâ tî sängö">Sango</option>
            <option value="sr" title="српски језик">Serbian</option>
            <option value="gd" title="Gàidhlig">Scottish Gaelic, Gaelic</option>
            <option value="sn" title="chiShona">Shona</option>
            <option value="si" title="සිංහල">Sinhala, Sinhalese</option>
            <option value="sk" title="slovenčina, slovenský jazyk">Slovak</option>
            <option value="sl" title="slovenski jezik, slovenščina">Slovene</option>
            <option value="so" title="Soomaaliga, af Soomaali">Somali</option>
            <option value="st" title="Sesotho">Southern Sotho</option>
            <option value="es" title="español">Spanish</option>
            <option value="su" title="Basa Sunda">Sundanese</option>
            <option value="sw" title="Kiswahili">Swahili</option>
            <option value="ss" title="SiSwati">Swati</option>
            <option value="sv" title="svenska">Swedish</option>
            <option value="ta" title="தமிழ்">Tamil</option>
            <option value="te" title="తెలుగు">Telugu</option>
            <option value="tg" title="тоҷикӣ, toçikī, تاجیکی‎">Tajik</option>
            <option value="th" title="ไทย">Thai</option>
            <option value="ti" title="ትግርኛ">Tigrinya</option>
            <option value="bo" title="བོད་ཡིག">Tibetan Standard, Tibetan, Central</option>
            <option value="tk" title="Türkmen, Түркмен">Turkmen</option>
            <option value="tl" title="Wikang Tagalog">Tagalog</option>
            <option value="tn" title="Setswana">Tswana</option>
            <option value="to" title="faka Tonga">Tonga (Tonga Islands)</option>
            <option value="tr" title="Türkçe">Turkish</option>
            <option value="ts" title="Xitsonga">Tsonga</option>
            <option value="tt" title="татар теле, tatar tele">Tatar</option>
            <option value="tw" title="Twi">Twi</option>
            <option value="ty" title="Reo Tahiti">Tahitian</option>
            <option value="ug" title="ئۇيغۇرچە‎, Uyghurche">Uyghur</option>
            <option value="uk" title="Українська">Ukrainian</option>
            <option value="ur" title="اردو">Urdu</option>
            <option value="uz" title="Oʻzbek, Ўзбек, أۇزبېك‎">Uzbek</option>
            <option value="ve" title="Tshivenḓa">Venda</option>
            <option value="vi" title="Tiếng Việt">Vietnamese</option>
            <option value="vo" title="Volapük">Volapük</option>
            <option value="wa" title="walon">Walloon</option>
            <option value="cy" title="Cymraeg">Welsh</option>
            <option value="wo" title="Wollof">Wolof</option>
            <option value="fy" title="Frysk">Western Frisian</option>
            <option value="xh" title="isiXhosa">Xhosa</option>
            <option value="yi" title="ייִדיש">Yiddish</option>
            <option value="yo" title="Yorùbá">Yoruba</option>
            <option value="za" title="Saɯ cueŋƅ, Saw cuengh">Zhuang, Chuang</option>
            <option value="zu" title="isiZulu">Zulu</option>
            </select></td>
        <td><select id="perc-country-list" class="perc-country-list">
            <option value="">None</option>
            <option value="AF">Afghanistan</option>
            <option value="AX">&Aring;land Islands</option>
            <option value="AL">Albania</option>
            <option value="DZ">Algeria</option>
            <option value="AS">American Samoa</option>
            <option value="AD">Andorra</option>
            <option value="AO">Angola</option>
            <option value="AI">Anguilla</option>
            <option value="AQ">Antarctica</option>
            <option value="AG">Antigua and Barbuda</option>
            <option value="AR">Argentina</option>
            <option value="AM">Armenia</option>
            <option value="AW">Aruba</option>
            <option value="AU">Australia</option>
            <option value="AT">Austria</option>
            <option value="AZ">Azerbaijan</option>
            <option value="BS">Bahamas</option>
            <option value="BH">Bahrain</option>
            <option value="BD">Bangladesh</option>
            <option value="BB">Barbados</option>
            <option value="BY">Belarus</option>
            <option value="BE">Belgium</option>
            <option value="BZ">Belize</option>
            <option value="BJ">Benin</option>
            <option value="BM">Bermuda</option>
            <option value="BT">Bhutan</option>
            <option value="BO">Bolivia, Plurinational State of</option>
            <option value="BA">Bosnia and Herzegovina</option>
            <option value="BW">Botswana</option>
            <option value="BV">Bouvet Island</option>
            <option value="BR">Brazil</option>
            <option value="IO">British Indian Ocean Territory</option>
            <option value="BN">Brunei Darussalam</option>
            <option value="BG">Bulgaria</option>
            <option value="BF">Burkina Faso</option>
            <option value="BI">Burundi</option>
            <option value="KH">Cambodia</option>
            <option value="CM">Cameroon</option>
            <option value="CA">Canada</option>
            <option value="CV">Cape Verde</option>
            <option value="KY">Cayman Islands</option>
            <option value="CF">Central African Republic</option>
            <option value="TD">Chad</option>
            <option value="CL">Chile</option>
            <option value="CN">China</option>
            <option value="CX">Christmas Island</option>
            <option value="CC">Cocos (Keeling) Islands</option>
            <option value="CO">Colombia</option>
            <option value="KM">Comoros</option>
            <option value="CG">Congo</option>
            <option value="CD">Congo, the Democratic Republic of the</option>
            <option value="CK">Cook Islands</option>
            <option value="CR">Costa Rica</option>
            <option value="CI">C&ocirc;te d'Ivoire</option>
            <option value="HR">Croatia</option>
            <option value="CU">Cuba</option>
            <option value="CY">Cyprus</option>
            <option value="CZ">Czech Republic</option>
            <option value="DK">Denmark</option>
            <option value="DJ">Djibouti</option>
            <option value="DM">Dominica</option>
            <option value="DO">Dominican Republic</option>
            <option value="EC">Ecuador</option>
            <option value="EG">Egypt</option>
            <option value="SV">El Salvador</option>
            <option value="GQ">Equatorial Guinea</option>
            <option value="ER">Eritrea</option>
            <option value="EE">Estonia</option>
            <option value="ET">Ethiopia</option>
            <option value="FK">Falkland Islands (Malvinas)</option>
            <option value="FO">Faroe Islands</option>
            <option value="FJ">Fiji</option>
            <option value="FI">Finland</option>
            <option value="FR">France</option>
            <option value="GF">French Guiana</option>
            <option value="PF">French Polynesia</option>
            <option value="TF">French Southern Territories</option>
            <option value="GA">Gabon</option>
            <option value="GM">Gambia</option>
            <option value="GE">Georgia</option>
            <option value="DE">Germany</option>
            <option value="GH">Ghana</option>
            <option value="GI">Gibraltar</option>
            <option value="GR">Greece</option>
            <option value="GL">Greenland</option>
            <option value="GD">Grenada</option>
            <option value="GP">Guadeloupe</option>
            <option value="GU">Guam</option>
            <option value="GT">Guatemala</option>
            <option value="GG">Guernsey</option>
            <option value="GN">Guinea</option>
            <option value="GW">Guinea-Bissau</option>
            <option value="GY">Guyana</option>
            <option value="HT">Haiti</option>
            <option value="HM">Heard Island and McDonald Islands</option>
            <option value="VA">Holy See (Vatican City State)</option>
            <option value="HN">Honduras</option>
            <option value="HK">Hong Kong</option>
            <option value="HU">Hungary</option>
            <option value="IS">Iceland</option>
            <option value="IN">India</option>
            <option value="ID">Indonesia</option>
            <option value="IR">Iran, Islamic Republic of</option>
            <option value="IQ">Iraq</option>
            <option value="IE">Ireland</option>
            <option value="IM">Isle of Man</option>
            <option value="IL">Israel</option>
            <option value="IT">Italy</option>
            <option value="JM">Jamaica</option>
            <option value="JP">Japan</option>
            <option value="JE">Jersey</option>
            <option value="JO">Jordan</option>
            <option value="KZ">Kazakhstan</option>
            <option value="KE">Kenya</option>
            <option value="KI">Kiribati</option>
            <option value="KP">Korea, Democratic People's Republic of</option>
            <option value="KR">Korea, Republic of</option>
            <option value="KW">Kuwait</option>
            <option value="KG">Kyrgyzstan</option>
            <option value="LA">Lao People's Democratic Republic</option>
            <option value="LV">Latvia</option>
            <option value="LB">Lebanon</option>
            <option value="LS">Lesotho</option>
            <option value="LR">Liberia</option>
            <option value="LY">Libyan Arab Jamahiriya</option>
            <option value="LI">Liechtenstein</option>
            <option value="LT">Lithuania</option>
            <option value="LU">Luxembourg</option>
            <option value="MO">Macao</option>
            <option value="MK">Macedonia, the former Yugoslav Republic of</option>
            <option value="MG">Madagascar</option>
            <option value="MW">Malawi</option>
            <option value="MY">Malaysia</option>
            <option value="MV">Maldives</option>
            <option value="ML">Mali</option>
            <option value="MT">Malta</option>
            <option value="MH">Marshall Islands</option>
            <option value="MQ">Martinique</option>
            <option value="MR">Mauritania</option>
            <option value="MU">Mauritius</option>
            <option value="YT">Mayotte</option>
            <option value="MX">Mexico</option>
            <option value="FM">Micronesia, Federated States of</option>
            <option value="MD">Moldova, Republic of</option>
            <option value="MC">Monaco</option>
            <option value="MN">Mongolia</option>
            <option value="ME">Montenegro</option>
            <option value="MS">Montserrat</option>
            <option value="MA">Morocco</option>
            <option value="MZ">Mozambique</option>
            <option value="MM">Myanmar</option>
            <option value="NA">Namibia</option>
            <option value="NR">Nauru</option>
            <option value="NP">Nepal</option>
            <option value="NL">Netherlands</option>
            <option value="AN">Netherlands Antilles</option>
            <option value="NC">New Caledonia</option>
            <option value="NZ">New Zealand</option>
            <option value="NI">Nicaragua</option>
            <option value="NE">Niger</option>
            <option value="NG">Nigeria</option>
            <option value="NU">Niue</option>
            <option value="NF">Norfolk Island</option>
            <option value="MP">Northern Mariana Islands</option>
            <option value="NO">Norway</option>
            <option value="OM">Oman</option>
            <option value="PK">Pakistan</option>
            <option value="PW">Palau</option>
            <option value="PS">Palestinian Territory, Occupied</option>
            <option value="PA">Panama</option>
            <option value="PG">Papua New Guinea</option>
            <option value="PY">Paraguay</option>
            <option value="PE">Peru</option>
            <option value="PH">Philippines</option>
            <option value="PN">Pitcairn</option>
            <option value="PL">Poland</option>
            <option value="PT">Portugal</option>
            <option value="PR">Puerto Rico</option>
            <option value="QA">Qatar</option>
            <option value="RE">R&eacute;union</option>
            <option value="RO">Romania</option>
            <option value="RU">Russian Federation</option>
            <option value="RW">Rwanda</option>
            <option value="BL">Saint Barth&eacute;lemy</option>
            <option value="SH">Saint Helena, Ascension and Tristan da Cunha</option>
            <option value="KN">Saint Kitts and Nevis</option>
            <option value="LC">Saint Lucia</option>
            <option value="MF">Saint Martin (French part)</option>
            <option value="PM">Saint Pierre and Miquelon</option>
            <option value="VC">Saint Vincent and the Grenadines</option>
            <option value="WS">Samoa</option>
            <option value="SM">San Marino</option>
            <option value="ST">Sao Tome and Principe</option>
            <option value="SA">Saudi Arabia</option>
            <option value="SN">Senegal</option>
            <option value="RS">Serbia</option>
            <option value="SC">Seychelles</option>
            <option value="SL">Sierra Leone</option>
            <option value="SG">Singapore</option>
            <option value="SK">Slovakia</option>
            <option value="SI">Slovenia</option>
            <option value="SB">Solomon Islands</option>
            <option value="SO">Somalia</option>
            <option value="ZA">South Africa</option>
            <option value="GS">South Georgia and the South Sandwich Islands</option>
            <option value="ES">Spain</option>
            <option value="LK">Sri Lanka</option>
            <option value="SD">Sudan</option>
            <option value="SR">Suriname</option>
            <option value="SJ">Svalbard and Jan Mayen</option>
            <option value="SZ">Swaziland</option>
            <option value="SE">Sweden</option>
            <option value="CH">Switzerland</option>
            <option value="SY">Syrian Arab Republic</option>
            <option value="TW">Taiwan, Province of China</option>
            <option value="TJ">Tajikistan</option>
            <option value="TZ">Tanzania, United Republic of</option>
            <option value="TH">Thailand</option>
            <option value="TL">Timor-Leste</option>
            <option value="TG">Togo</option>
            <option value="TK">Tokelau</option>
            <option value="TO">Tonga</option>
            <option value="TT">Trinidad and Tobago</option>
            <option value="TN">Tunisia</option>
            <option value="TR">Turkey</option>
            <option value="TM">Turkmenistan</option>
            <option value="TC">Turks and Caicos Islands</option>
            <option value="TV">Tuvalu</option>
            <option value="UG">Uganda</option>
            <option value="UA">Ukraine</option>
            <option value="AE">United Arab Emirates</option>
            <option value="GB">United Kingdom</option>
            <option value="US">United States</option>
            <option value="UM">United States Minor Outlying Islands</option>
            <option value="UY">Uruguay</option>
            <option value="UZ">Uzbekistan</option>
            <option value="VU">Vanuatu</option>
            <option value="VE">Venezuela, Bolivarian Republic of</option>
            <option value="VN">Viet Nam</option>
            <option value="VG">Virgin Islands, British</option>
            <option value="VI">Virgin Islands, U.S.</option>
            <option value="WF">Wallis and Futuna</option>
            <option value="EH">Western Sahara</option>
            <option value="YE">Yemen</option>
            <option value="ZM">Zambia</option>
            <option value="ZW">Zimbabwe</option>
        </select></td>
        <td><input type="radio" name="default-lang" /></td>
        <td>
          <span class="perc-table-remove fa fa-minus"></span>
        </td>
        <td>
        </td>
    </tr>
</tbody>
 </table>
</div>
</div>
	</xsl:template>
	<xsl:template match="Control[@name='percLocalLangControl' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" id="perc-content-edit-{@paramName}" value="{Value}"/>
		<div class="perc-local-lang-readonly" id="{@paramName}">
			<div style="display:none;">TODO: Finish ME!</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
