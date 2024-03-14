{"gadgets.container": ["default"],
    "gadgets.parent" : null,
    "gadgets.lockedDomainRequired" : false,
    "gadgets.lockedDomainSuffix" : "-a.example.com:8080",
    "gadgets.parentOrigins" : ["*"],
    "gadgets.iframeBaseUri" : "//cm/gadgets/ifr",
    "gadgets.jsUriTemplate": "http://%host%/cm/gadgets/js/%js%",
    "gadgets.uri.iframe.lockedDomainSuffix" :  "-a.example.com:8080",
    "gadgets.uri.iframe.unlockedDomain" : "localhost:9992",
    "gadgets.uri.iframe.basePath" : "/cm/gadgets/ifr",
    "gadgets.uri.js.host" : "http://localhost:9992/",
    "gadgets.uri.js.path" : "/cm/gadgets/js",
    "gadgets.uri.oauth.callbackTemplate" : "//%host%/cm/gadgets/oauthcallback",
    "gadgets.securityTokenType" : "insecure",
    "gadgets.osDataUri" : "http://%host%/cm/rpc",
    "defaultShindigTestHost": "http://${SERVER_HOST}:${SERVER_PORT}",
    "defaultShindigProxyConcatAuthority": "${SERVER_HOST}:${SERVER_PORT}",
    "gadgets.uri.concat.host" : "${Cur['defaultShindigProxyConcatAuthority']}",
    "gadgets.uri.concat.path" : "/cm/gadgets/concat",
    "gadgets.uri.concat.js.splitToken" : "false",
    "gadgets.uri.proxy.host" : "${Cur['defaultShindigProxyConcatAuthority']}",
    "gadgets.uri.proxy.path" : "/cm/gadgets/proxy",
    "gadgets.features" : {
    "core.io" : {
        "proxyUrl" : "//%host%/cm/gadgets/proxy?container=default&refresh=%refresh%&url=%url%%rewriteMime%",
            "jsonProxyUrl" : "//%host%/cm/gadgets/makeRequest"
    },
    "views" : {
        "profile" : {
            "isOnlyVisible" : false,
                "urlTemplate" : "http://localhost/cm/gadgets/profile?{var}",
                "aliases": ["DASHBOARD", "default"]
        },
        "canvas" : {
            "isOnlyVisible" : true,
                "urlTemplate" : "http://localhost/cm/gadgets/canvas?{var}",
                "aliases" : ["FULL_PAGE"]
        }
    },
    "tabs": {
        "css" : [
            ".tablib_table {",
            "width: 100%;",
            "border-collapse: separate;",
            "border-spacing: 0px;",
            "empty-cells: show;",
            "font-size: 11px;",
            "text-align: center;",
            "}",
            ".tablib_emptyTab {",
            "border-bottom: 1px solid #676767;",
            "padding: 0px 1px;",
            "}",
            ".tablib_spacerTab {",
            "border-bottom: 1px solid #676767;",
            "padding: 0px 1px;",
            "width: 1px;",
            "}",
            ".tablib_selected {",
            "padding: 2px;",
            "background-color: #ffffff;",
            "border: 1px solid #676767;",
            "border-bottom-width: 0px;",
            "color: #3366cc;",
            "font-weight: bold;",
            "width: 80px;",
            "cursor: default;",
            "}",
            ".tablib_unselected {",
            "padding: 2px;",
            "background-color: #dddddd;",
            "border: 1px solid #aaaaaa;",
            "border-bottom-color: #676767;",
            "color: #000000;",
            "width: 80px;",
            "cursor: pointer;",
            "}",
            ".tablib_navContainer {",
            "width: 10px;",
            "vertical-align: middle;",
            "}",
            ".tablib_navContainer a:link, ",
            ".tablib_navContainer a:visited, ",
            ".tablib_navContainer a:hover {",
            "color: #3366aa;",
            "text-decoration: none;",
            "}"
        ]
    },
    "minimessage": {
        "css": [
            ".mmlib_table {",
            "width: 100%;",
            "font: bold 9px arial,sans-serif;",
            "background-color: #fff4c2;",
            "border-collapse: separate;",
            "border-spacing: 0px;",
            "padding: 1px 0px;",
            "}",
            ".mmlib_xlink {",
            "font: normal 1.1em arial,sans-serif;",
            "font-weight: bold;",
            "color: #0000cc;",
            "cursor: pointer;",
            "}"
        ]
    },
    "rpc" : {
        "parentRelayUrl" : "/cm/gadgets/files/container/rpc_relay.html",
            "useLegacyProtocol" : false
    },
    "skins" : {
        "properties" : {
            "BG_COLOR": "",
                "BG_IMAGE": "",
                "BG_POSITION": "",
                "BG_REPEAT": "",
                "FONT_COLOR": "",
                "ANCHOR_COLOR": ""
        }
    },
    "opensocial" : {
        "path" : "http://%host%/cm/rpc",
            "invalidatePath" : "http://%host%/cm/rpc",
            "domain" : "shindig",
            "enableCaja" : false,
            "supportedFields" : {
            "person" : ["id", {"name" : ["familyName", "givenName", "unstructured"]}, "thumbnailUrl", "profileUrl"],
                "activity" : ["id", "title"]

        }
    },
    "osapi.services" : {
        "gadgets.rpc" : ["container.listMethods"]
    },
    "osapi" : {
        "endPoints" : [ "http://%host%/cm/rpc" ]
    },
    "osml": {
        "library": "config/OSML_library.xml"
    }
}}
