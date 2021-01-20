VERSION 5.00
Begin VB.UserControl LaunchWordCt 
   ClientHeight    =   615
   ClientLeft      =   0
   ClientTop       =   0
   ClientWidth     =   1470
   ScaleHeight     =   615
   ScaleWidth      =   1470
   Begin VB.CommandButton cmdLaunch 
      Caption         =   "&Launch Word"
      Height          =   615
      Left            =   0
      TabIndex        =   0
      Top             =   0
      Width           =   1455
   End
End
Attribute VB_Name = "LaunchWordCt"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = True
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = True
Option Explicit

Dim wordApp As Object
Dim theDoc As Object
Dim theURL As String
Dim templateDir As String
Dim templateURL As String
Dim theEncoding  As String
Dim theBodySourceName  As String
Dim theFirstTimeUse  As String
Dim theDebugMode  As String
Dim theBodyURL  As String
Dim theParamString  As String
Dim theInlineSlots  As String
Dim theDocURL As String
Dim errCount As Long
Dim inCharLong As Long
Dim ermsg As String
Dim newFile As Boolean
Dim dotFilename As String
'Default Property Values:
Const m_def_ContentEditorURL = ""
Const m_def_WordTemplateURL = ""
Const m_def_ContentBodyURL = ""
Const m_def_EncodingParam = ""
Const m_def_BodySourceName = ""
Const m_def_FirstTimeUse = ""
Const m_def_DebugMode = ""
Const m_def_ParamString = ""
Const m_def_InlineSlots = ""
Dim retval As Long
Const ermsgRxAdmin = " Please consult your Rhythmyx Administrator."
Const UPLOAD_BOUNDARY = "6G+fadfgstj34h"

'Property Variables:
Dim m_ContentEditorURL As String
Dim m_WordTemplateURL As String
Dim m_ContentBodyURL As String
Dim m_EncodingParam As String
Dim m_BodySourceName As String
Dim m_FirstTimeUse As String
Dim m_DebugMode As String
Dim m_ParamString As String
Dim m_InlineSlots As String
Dim m_Success As Boolean

Dim contentid As Long
Dim revisionid As Long

'Log Variables:
Dim m_logFile As String
Dim fsobj As New FileSystemObject
Dim textstrobj As TextStream
Dim strLog As String
Dim dashline As String

' mgb - for converting to utf-8
Private Declare Function WideCharToMultiByte Lib "kernel32" _
    (ByVal CodePage As Long, ByVal dwFlags As Long, _
     ByVal lpWideCharStr As Long, ByVal cchWideChar As Long, _
     ByVal lpMultiByteStr As Long, ByVal cchMultiByte As Long, _
     ByVal lpDefaultChar As Long, lpUsedDefaultChar As Long) As Long

' make top window
Private Declare Function FindWindowA Lib "user32.dll" (ByVal className As Any, ByVal title As Any) As Long
Private Declare Function SetWindowPos Lib "user32.dll" (ByVal hwnd As Long, ByVal hWndInsertAfter As Long, ByVal x As Long, ByVal y As Long, ByVal cx As Long, ByVal cy As Long, ByVal wFlags As Long) As Long

Const SWP_NOMOVE = 2
Const SWP_NOSIZE = 1
Const FLAGS = SWP_NOMOVE Or SWP_NOSIZE
Const HWND_TOP = 0
Const HWND_TOPMOST = -1
Const HWND_NOTOPMOST = -2


'Event Declarations:
Event ReadProperties(PropBag As PropertyBag) 'MappingInfo=UserControl,UserControl,-1,ReadProperties
Event InitProperties() 'MappingInfo=UserControl,UserControl,-1,InitProperties
Event Click() 'MappingInfo=cmdLaunch,cmdLaunch,-1,Click


 Private Declare Function GetEnvironmentVariable Lib _
     "kernel32" Alias "GetEnvironmentVariableA" _
     (ByVal lpName As String, ByVal lpBuffer _
     As String, ByVal nSize As Long) As Long
     
 Dim xmldoc As New MSXML2.DOMDocument40
 
Private Sub MakeTopWindow()

    On Error GoTo Ignore
    
    Dim capt As String
    capt = Application.ActiveWindow.Caption & " - Microsoft Word"
    
    Dim hwnd As Long

    hwnd = FindWindowA(0&, capt)

    Dim lResult As Long
    lResult = SetWindowPos(hwnd, HWND_TOP, 0, 0, 0, 0, FLAGS)
    
Ignore:
End Sub

Public Sub Fire()
On Error GoTo errorHandler
    m_Success = True
        
'Log file initialization
    strLog = ""
    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Begining of the Word Control..."
    strLog = strLog & vbCrLf & dashline
    ermsg = ""
    newFile = False
'Initialize the parameters coming from browser
    theDebugMode = DebugMode
    If UCase(theDebugMode) = "YES" Then
        m_logFile = InputBox("Enter the absolute path for log file", "Log File", "C:\Wordlog.txt")
        If m_logFile = "" Then
            MsgBox "Log file name is empty, log file will not be written."
        End If
    End If
    theURL = ContentEditorURL
    If theURL = "" Then
        strLog = strLog & vbCrLf & "ContentEditorURL is missing."
        MsgBox "ContentEditorURL is missing." & ermsgRxAdmin
        GoTo logwriter
    End If
    strLog = strLog & vbCrLf & "ContentEditorURL"
    strLog = strLog & vbCrLf & theURL
    strLog = strLog & vbCrLf & vbCrLf
    theBodyURL = ContentBodyURL
    If theBodyURL = "" Then
        strLog = strLog & vbCrLf & "ContentBodyURL is missing."
        MsgBox "ContentBodyURL is missing." & ermsgRxAdmin
        Exit Sub
    End If
    theDocURL = Replace(theBodyURL, "&amp;", "&")
    
    theBodySourceName = BodySourceName
    strLog = strLog & vbCrLf & "BodySourceName"
    strLog = strLog & vbCrLf & theBodySourceName
    strLog = strLog & vbCrLf & vbCrLf
  
    theFirstTimeUse = FirstTimeUse
    strLog = strLog & vbCrLf & "FirstTimeUse"
    strLog = strLog & vbCrLf & theFirstTimeUse
    strLog = strLog & vbCrLf & vbCrLf
    
    theEncoding = EncodingParam
    strLog = strLog & vbCrLf & "EncodingParam"
    strLog = strLog & vbCrLf & theEncoding
    strLog = strLog & vbCrLf & vbCrLf
    
'    If LCase(theEncoding) = "utf-8" Then
'        inCharLong = 65001
'    Else
'        inCharLong = 1252
'    End If
    
    theParamString = ParamString
    'call the buildpost function to insert the document.
    If BuildPost <> 1 Then
        m_Success = False
        MsgBox "Error occured while inserting the document. Make sure that you entered all the required fields."
        Exit Sub
    End If
    
    templateURL = WordTemplateURL
    If templateURL = "" Then
        strLog = strLog & vbCrLf & "WordTemplateURL is missing."
        MsgBox "WordTemplateURL is missing." & ermsgRxAdmin
        GoTo logwriter
    End If
    strLog = strLog & vbCrLf & "WordTemplateURL"
    strLog = strLog & vbCrLf & templateURL
    strLog = strLog & vbCrLf & vbCrLf
    

    'Call LoadXMLDocument function to download and load the document from server to xmlDoc.
    retval = LoadXMLDocument()
    If retval = 0 Then
        If ermsg = "" Then ermsg = "An error occurred."
        If Err.Description <> "" Then ermsg = ermsg & "Error Description: " & Err.Description
        ermsg = ermsg & ermsgRxAdmin
        MsgBox ermsg
        GoTo logwriter
    End If
    'Call PARSEXML function to parse the document xmlDoc and open Word.
    retval = PARSEXML()
    If retval = 0 Then
        If ermsg = "" Then ermsg = "An error occurred."
        If Err.Description <> "" Then ermsg = ermsg & "Error Description: " & Err.Description
        ermsg = ermsg & ermsgRxAdmin
        MsgBox ermsg
        GoTo logwriter
    End If
    
    theDoc.Activate
    
    'bring word window on top
    MakeTopWindow
            
    GoTo logwriter
    Exit Sub
logwriter:
    If m_logFile <> "" Then
        On Error Resume Next
        Err.Clear
        Set textstrobj = fsobj.OpenTextFile(m_logFile, ForWriting, True)
        If Err.Number <> 0 Then
            MsgBox Err.Description
        Else
            textstrobj.WriteLine Now
            textstrobj.Write strLog
            textstrobj.Close
        End If
    End If
    If IsObject(textstrobj) Then Set textstrobj = Nothing
    If IsObject(fsobj) Then Set fsobj = Nothing
    If IsObject(xmldoc) Then Set xmldoc = Nothing
    Exit Sub
errorHandler:
    If ermsg = "" Then ermsg = "An error occurred."
    If Err.Description <> "" Then ermsg = ermsg & "Error Description: " & Err.Description
    ermsg = ermsg & ermsgRxAdmin
    MsgBox ermsg
End Sub
Private Function DownloadAndSaveWordTemplateFile() As Integer
'This function is to download the word template and save it to winnt or windows directory.
'Return value 1 for success and 0 for failure
On Error GoTo errorHandler
strLog = strLog & vbCrLf & vbCrLf
strLog = strLog & vbCrLf & "Inside DownloadAndSaveWordTemplateFile function"
strLog = strLog & vbCrLf & dashline
ermsg = "Unable to download and save word template."
Dim objXMLHTTP As XMLHTTP40
Set objXMLHTTP = New MSXML2.XMLHTTP40
Dim dotFileArray, dotFileArray1
objXMLHTTP.Open "GET", templateURL, False
objXMLHTTP.send
dotFileArray = Split(templateURL, "/")
dotFileArray1 = Split(dotFileArray(UBound(dotFileArray, 1)), "?")

dotFilename = templateDir & "\" & dotFileArray1(0)

Dim b() As Byte
b() = objXMLHTTP.responseBody
Open dotFilename For Binary Access Write As #1
Put #1, , b
Close #1
If IsObject(objXMLHTTP) Then Set objXMLHTTP = Nothing
DownloadAndSaveWordTemplateFile = 1 'Success
ermsg = ""
Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in DownloadAndSaveWordTemplateFile function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    If IsObject(objXMLHTTP) Then Set objXMLHTTP = Nothing
    DownloadAndSaveWordTemplateFile = 0
End Function

Private Function LoadXMLDocument() As Integer
'This function is to download the document from the database and load it into xmlDoc object.
On Error GoTo errorHandler
strLog = strLog & vbCrLf & vbCrLf
strLog = strLog & vbCrLf & "Inside LoadXMLDocument"
strLog = strLog & vbCrLf & dashline

ermsg = "Unable to download the document. "
Dim objXMLHTTP As XMLHTTP40

Set objXMLHTTP = New MSXML2.XMLHTTP40
strLog = strLog & vbCrLf & "Passed creating objXMLHTTP"

objXMLHTTP.Open "GET", theURL, False
strLog = strLog & vbCrLf & "Passed objXMLHTTP.open"

objXMLHTTP.send
strLog = strLog & vbCrLf & "Passed objXMLHTTP.send"
strLog = strLog & vbCrLf & "objXMLHTTP.Status = " & objXMLHTTP.Status

If objXMLHTTP.Status >= 400 Then
    ermsg = ermsg + "HTTP Error Code: " & objXMLHTTP.Status
    LoadXMLDocument = 0
    Exit Function
End If

xmldoc.async = False
If xmldoc.loadXML(objXMLHTTP.responseXML.xml) <> True Then
    strLog = strLog & vbCrLf & "xmlDoc.loadXML is false."
    textstrobj.Write objXMLHTTP.responseXML.xml
    ermsg = ermsg + "LoadXML failed."
    LoadXMLDocument = 0
    Exit Function
End If
strLog = strLog & vbCrLf & "Passed xmlDoc.loadXML"

If xmldoc.documentElement.selectSingleNode("/ContentEditor/ItemContent/DisplayField/Control[@paramName='" & theBodySourceName & "_filename']/Value") Is Nothing Then
    strLog = strLog & vbCrLf & theBodySourceName & "_filename value is missing and treating the document as new."
    newFile = True
End If

If IsObject(objXMLHTTP) Then Set objXMLHTTP = Nothing
LoadXMLDocument = 1
ermsg = ""
Exit Function

errorHandler:
    strLog = strLog & vbCrLf & "Error occured in LoadXMLDocument function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    If IsObject(objXMLHTTP) Then Set objXMLHTTP = Nothing
    LoadXMLDocument = 0
End Function

Private Function PARSEXML() As Integer
On Error GoTo errorHandler
    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Inside PARSEXML function"
    strLog = strLog & vbCrLf & dashline
    ermsg = "Unable to parse the document. "
    Dim DisplayFields As IXMLDOMNodeList
    Dim DisplayFieldsUserStatus As IXMLDOMNodeList
    Dim DisplayFieldsActionLinkList As IXMLDOMNodeList
    Dim DisplayFieldsAll As IXMLDOMNodeList

    Dim i As Long
    'Check weather UserStatus exists or not
    Set DisplayFieldsUserStatus = xmldoc.selectNodes("/ContentEditor/UserStatus")
    If DisplayFieldsUserStatus Is Nothing Then
       strLog = strLog & vbCrLf & "UserStatus field is missing."
       ermsg = ermsg & "UserStatus is missing."
       PARSEXML = 0
       Exit Function
    End If
    'Check weather ActionLinkList Update exists or not
    Set DisplayFieldsActionLinkList = xmldoc.selectNodes("/ContentEditor/ActionLinkList/ActionLink")
    If DisplayFieldsActionLinkList Is Nothing Then
       strLog = strLog & vbCrLf & "ActionLinkList is missing."
       ermsg = ermsg & "ActionLinkList is missing."
       PARSEXML = 0
       Exit Function
    End If
    'Check weather DisplayField exists or not
    Set DisplayFieldsAll = xmldoc.selectNodes("/ContentEditor/ItemContent/DisplayField")
    If DisplayFieldsAll.length = 0 Then
         strLog = strLog & vbCrLf & "DisplayField is missing."
         ermsg = ermsg & "DisplayField is missing."
         PARSEXML = 0
         Exit Function
    End If

      
    retval = openDoc()
    If retval = 0 Then 'Failed to open the word with document in it
        strLog = strLog & vbCrLf & "Failed in openDoc fnction, called from PARSEXML function."
        PARSEXML = 0
        Exit Function
    End If
    'Clear all Custom Document Properties.
    For i = 1 To theDoc.CustomDocumentProperties.Count
        theDoc.CustomDocumentProperties.Item(1).Delete
    Next
    
 
    Dim ParValue$
    
    'make URL without parameters
    ParValue$ = Left(theURL, InStr(1, theURL, "?") - 1)
    
    Call addACustomProperty("RxContentEditorURL", ParValue$)

    For i = 0 To (DisplayFieldsUserStatus.length - 1)
        retval = getUserSessionID(DisplayFieldsUserStatus.nextNode)
        If retval = 0 Then
            strLog = strLog & vbCrLf & "Failed in getUserSessionID fnction, called from PARSEXML function."
            PARSEXML = 0
            Exit Function
        End If
    Next
    'Add inline slot ids to custom properties
    Call addInlineSlotsToCustomProps
    For i = 0 To DisplayFieldsActionLinkList.Item(0).childNodes.length - 2
            retval = getActionLinkProps(DisplayFieldsActionLinkList.Item(0).childNodes.Item(i + 1))
            If retval = 0 Then
                strLog = strLog & vbCrLf & "Failed in getActionLinkProps fnction, called from PARSEXML function."
                PARSEXML = 0
                Exit Function
            End If
    Next
    
    ermsg = "Error occurred"
    
    For i = 0 To (DisplayFieldsAll.length - 1)
         Call AddCustomProperties(DisplayFieldsAll.nextNode())
    Next
    
    Dim found As Boolean
    found = False
    For i = 1 To theDoc.CustomDocumentProperties.Count
       If theDoc.CustomDocumentProperties.Item(i).Name = "RxField:" & theBodySourceName & "_filename" Then
          strLog = strLog & vbCrLf & theBodySourceName & "_filename custom property is missing, treating it as a new file."
          found = True
          Exit For
       End If
    Next
    
    If Not found Then
        Call theDoc.CustomDocumentProperties.Add("RxField:" & theBodySourceName & "_filename", False, 4, "NewFile.doc")
    End If
    
    wordApp.ActiveWindow.Caption = theDoc.CustomDocumentProperties("RxField:" & theBodySourceName & "_filename")
   
    ermsg = ""
    PARSEXML = 1

    Exit Function

errorHandler:
    strLog = strLog & vbCrLf & "Error occured in PARSEXML function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    PARSEXML = 0
End Function
Private Sub AddCustomProperties(DisplayEntry As IXMLDOMElement)

On Error GoTo errorHandler
strLog = strLog & vbCrLf & vbCrLf
strLog = strLog & vbCrLf & "Inside AddCustomProperties function"
strLog = strLog & vbCrLf & dashline

Dim ParName$, ParValue$
Dim ControlEntry As IXMLDOMElement
Dim ValueElem As IXMLDOMElement
Dim DisplayChoiceEntryList As IXMLDOMNodeList
Dim DispEntry As IXMLDOMElement
Dim dce As Long
If Not DisplayEntry.getElementsByTagName("Control") Is Nothing Then
    Set ControlEntry = DisplayEntry.getElementsByTagName("Control").Item(0)
Else
    Exit Sub
End If
Set ValueElem = ControlEntry.selectSingleNode("Value")
If ValueElem Is Nothing Then
    Set DisplayChoiceEntryList = ControlEntry.selectNodes("DisplayChoices/DisplayEntry")
    If DisplayChoiceEntryList Is Nothing Then
        Exit Sub
    End If
    ParName$ = "RxField_MV:" + ControlEntry.getAttribute("paramName")
    If DisplayChoiceEntryList.length = 0 Then
        Exit Sub
    End If
    For dce = 0 To (DisplayChoiceEntryList.length - 1)
        Set DispEntry = DisplayChoiceEntryList.Item(dce)
        If DispEntry.getAttribute("selected") = "yes" Then
            ParValue$ = ParValue$ & DispEntry.selectSingleNode("Value").Text & ";"
        End If
    Next dce
    If ParValue$ <> "" Then
        ParValue$ = Left(ParValue$, Len(ParValue$) - 1)
    End If
Else
    ParName$ = "RxField:" + ControlEntry.getAttribute("paramName")
    ParValue$ = ValueElem.Text
End If

If ParValue$ <> "" And ParName$ <> "" Then
    Call addACustomProperty(ParName$, ParValue$)
End If

Exit Sub

errorHandler:
    strLog = strLog & vbCrLf & "Error occured in AddCustomProperties function but continuing...."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    If errCount = 0 Then
        Call MsgBox(Err.Description & vbCrLf & "Please refresh your page", , "Need Refresh")
        errCount = errCount + 1
    End If
End Sub

Private Function getUserSessionID(DisplayEntry As IXMLDOMElement) As Integer
    On Error GoTo errorHandler
    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Inside getUserSessionID function"
    strLog = strLog & vbCrLf & dashline
    ermsg = "Failed to getUserSessionID."
    Dim ParName$, ParValue$
    ParName$ = "sessionId"
    ParValue$ = DisplayEntry.getAttribute(ParName$)
    Call addACustomProperty("RxField:pssessionid", ParValue$)
    getUserSessionID = 1
    ermsg = ""
    strLog = strLog & vbCrLf & "getUserSessionID succeeded"
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in getUserSessionID function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    getUserSessionID = 0
End Function

Private Function getActionLinkProps(DisplayEntry As IXMLDOMElement) As Integer
    On Error GoTo errorHandler
    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Inside getActionLinkProps function"
    strLog = strLog & vbCrLf & dashline
    ermsg = "Failed to get Action Link Props."
    Dim ParName$, ParValue$
    ParName$ = "RxField:" & DisplayEntry.getAttribute("name")
    ParValue$ = DisplayEntry.Text
    Call addACustomProperty(ParName$, ParValue$)
    ermsg = ""
    getActionLinkProps = 1
    strLog = strLog & vbCrLf & "getActionLinkProps succeeded"
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in getActionLinkProps function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    getActionLinkProps = 0
End Function

Private Sub addACustomProperty(propname, propvalue)
    On Error GoTo errorHandler

    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Inside addACustomProperty function"
    strLog = strLog & vbCrLf & dashline
    Dim found As Boolean
    found = False
    Dim i As Long
    For i = 1 To theDoc.CustomDocumentProperties.Count
       If propname = theDoc.CustomDocumentProperties.Item(i).Name Then
          found = True
          theDoc.CustomDocumentProperties.Item(i).Value = propvalue
          Exit For
       End If
    Next
    
    
    If Not found Then
        Call theDoc.CustomDocumentProperties.Add(propname, False, 4, propvalue)
    End If
    strLog = strLog & vbCrLf & "Added a custom property. propname = " & propname & "  and  propvalue = " & propvalue
    Exit Sub
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in addACustomProperty function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
End Sub

Private Function openDoc() As Integer
On Error GoTo errorHandler
    On Error Resume Next
    strLog = strLog & vbCrLf & vbCrLf
    strLog = strLog & vbCrLf & "Inside openDoc function"
    strLog = strLog & vbCrLf & dashline
    ermsg = "Failed to open Word Document."
    
    Set wordApp = GetObject(, "Word.Application")
    
    If Err Then Set wordApp = CreateObject("Word.Application")
    If wordApp Is Nothing Then
        strLog = strLog & vbCrLf & "Could not create Word.Application object."
        ermsg = ermsg & "Could not create Word.Application object."
        openDoc = 0
        Exit Function
    End If
    On Error GoTo errorHandler
    templateDir = wordApp.Options.DefaultFilePath(2)
    'Call DownloadAndSaveWordTemplateFile to save Rhythmyx.dot to windows directory.
    strLog = strLog & vbCrLf & "Calling DownloadAndSaveWordTemplateFile to save Rhythmyx.dot to windows directory."
    retval = DownloadAndSaveWordTemplateFile()
    'retval 0 means failed to download and save the template. 1 means success.
    If retval = 0 Then
        strLog = strLog & vbCrLf & "Failed in DownloadAndSaveWordTemplateFile."
        openDoc = 0
        Exit Function
    End If
    wordApp.Visible = True
    If newFile Then
        strLog = strLog & vbCrLf & "Opening the word with a new file and adding the template when newFile is true."
        Set theDoc = wordApp.Documents.Add(Template:=dotFilename, NewTemplate:=False, DocumentType:=0)
        strLog = strLog & vbCrLf & "Opening the word succeeded when newFile is true."
    Else
        'If user clears the word document and then tries to launch word.
        'The clear makes the BODYCONTENT column as null.
        'In such case wordApp.Documents.Open fails
        'It is not a correct assumption though if wordApp.Documents.Open fails it has been assumed that the word is cleared.
        'and we open a new document
       On Error Resume Next
            Err.Clear
            strLog = strLog & vbCrLf & "Opening the word document and adding the template."
            'Always set the encoding to utf8 (65001 is msoEncodingUTF8)
            Set theDoc = wordApp.Documents.Open(theDocURL, , False, False, , , , , , , 65001)
            If Err.Number <> 0 Then
                strLog = strLog & vbCrLf & "Opening the word document failed, trying to open the word with new file."
                Set theDoc = wordApp.Documents.Add(Template:=dotFilename, NewTemplate:=False, DocumentType:=0)
                Err.Clear
            End If
        On Error GoTo errorHandler
    End If
    
    strLog = strLog & vbCrLf & "Attaching the word template."
    theDoc.AttachedTemplate = dotFilename
    strLog = strLog & vbCrLf & "Word template attachement is completed."
    
    theDoc.Activate
    If Not newFile Then
        CorrectInlineLinks
    End If
    ermsg = ""
    openDoc = 1
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in openDoc function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    openDoc = 0
End Function

Private Sub UserControl_ReadProperties(PropBag As PropertyBag)
    
    RaiseEvent ReadProperties(PropBag)
    m_ContentEditorURL = PropBag.ReadProperty("ContentEditorURL", m_def_ContentEditorURL)
    m_WordTemplateURL = PropBag.ReadProperty("WordTemplateURL", m_def_WordTemplateURL)
    m_ContentBodyURL = PropBag.ReadProperty("ContentBodyURL", m_def_ContentBodyURL)
    m_EncodingParam = PropBag.ReadProperty("EncodingParam", m_def_EncodingParam)
    m_BodySourceName = PropBag.ReadProperty("BodySourceName", m_def_BodySourceName)
    m_FirstTimeUse = PropBag.ReadProperty("FirstTimeUse", m_def_FirstTimeUse)
    m_DebugMode = PropBag.ReadProperty("DebugMode", m_def_DebugMode)
    m_ParamString = PropBag.ReadProperty("ParamString", m_def_ParamString)
    m_InlineSlots = PropBag.ReadProperty("InlineSlots", m_def_InlineSlots)
    m_Success = PropBag.ReadProperty("Success", True)
        
    Exit Sub

errorHandler:
    If errCount = 0 Then
        Call MsgBox(Err.Description & vbCrLf & "Please refresh your page", , "Need Refresh")
        errCount = errCount + 1
    End If

    
End Sub

Private Sub UserControl_InitProperties()
    On Error GoTo errorHandler
    RaiseEvent InitProperties
    m_ContentEditorURL = m_def_ContentEditorURL
    m_WordTemplateURL = m_def_WordTemplateURL
    m_ContentBodyURL = m_def_ContentBodyURL
    m_EncodingParam = m_def_EncodingParam
    m_BodySourceName = m_def_BodySourceName
    m_FirstTimeUse = m_def_FirstTimeUse
    m_DebugMode = m_def_DebugMode
    m_ParamString = m_def_ParamString
    m_InlineSlots = m_def_InlineSlots
    m_Success = True
    
    Exit Sub

errorHandler:
    If errCount = 0 Then
        Call MsgBox(Err.Description & vbCrLf & "Please refresh your page", , "Need Refresh")
        errCount = errCount + 1
    End If

End Sub

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=13,0,0,0

Public Property Get ContentEditorURL() As String
    
    ContentEditorURL = m_ContentEditorURL
End Property

Public Property Let ContentEditorURL(ByVal New_ContentEditorURL As String)
    
    m_ContentEditorURL = New_ContentEditorURL
    
    PropertyChanged "ContentEditorURL"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=14,0,0,0

Public Property Get WordTemplateURL() As String
    
    WordTemplateURL = m_WordTemplateURL
End Property

Public Property Let WordTemplateURL(ByVal New_WordTemplateURL As String)
    
    m_WordTemplateURL = New_WordTemplateURL
    
    PropertyChanged "WordTemplateURL"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=15,0,0,0

Public Property Get ContentBodyURL() As String
    
    ContentBodyURL = m_ContentBodyURL
End Property

Public Property Let ContentBodyURL(ByVal New_ContentBodyURL As String)
    
    m_ContentBodyURL = New_ContentBodyURL
    
    PropertyChanged "ContentBodyURL"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=16,0,0,0

Public Property Get EncodingParam() As String
    
    EncodingParam = m_EncodingParam
End Property

Public Property Let EncodingParam(ByVal New_EncodingParam As String)
    
    m_EncodingParam = New_EncodingParam
    
    PropertyChanged "EncodingParam"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=17,0,0,0

Public Property Get BodySourceName() As String
    
    BodySourceName = m_BodySourceName
End Property

Public Property Let BodySourceName(ByVal New_BodySourceName As String)
    
    m_BodySourceName = New_BodySourceName
    
    PropertyChanged "BodySourceName"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=18,0,0,0
Public Property Get FirstTimeUse() As String
    
    FirstTimeUse = m_FirstTimeUse
End Property

Public Property Let FirstTimeUse(ByVal New_FirstTimeUse As String)
    
    m_FirstTimeUse = New_FirstTimeUse
    
    PropertyChanged "FirstTimeUse"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=19,0,0,0
Public Property Get DebugMode() As String
    
    DebugMode = m_DebugMode
End Property

Public Property Let DebugMode(ByVal New_DebugMode As String)
    
    m_DebugMode = New_DebugMode
    
    PropertyChanged "DebugMode"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=20,0,0,0
Public Property Get ParamString() As String
    
    ParamString = m_ParamString
End Property

Public Property Let ParamString(ByVal New_ParamString As String)
    
    m_ParamString = New_ParamString
    
    PropertyChanged "ParamString"
End Property

'WARNING! DO NOT REMOVE OR MODIFY THE FOLLOWING COMMENTED LINES!
'MemberInfo=21,0,0,0
Public Property Get InlineSlots() As String
    
    InlineSlots = m_InlineSlots
End Property

Public Property Let InlineSlots(ByVal New_InlineSlots As String)
    
    m_InlineSlots = New_InlineSlots
    
    PropertyChanged "InlineSlots"
End Property
Public Property Get Success() As Boolean
    Success = m_Success
End Property

'Write property values to storage
Private Sub UserControl_WriteProperties(PropBag As PropertyBag)
    On Error GoTo errorHandler
    Call PropBag.WriteProperty("ContentEditorURL", m_ContentEditorURL, m_def_ContentEditorURL)
    Call PropBag.WriteProperty("WordTemplateURL", m_WordTemplateURL, m_def_WordTemplateURL)
    Call PropBag.WriteProperty("ContentBodyURL", m_ContentBodyURL, m_def_ContentBodyURL)
    Call PropBag.WriteProperty("EncodingParam", m_EncodingParam, m_def_EncodingParam)
    Call PropBag.WriteProperty("BodySourceName", m_BodySourceName, m_def_BodySourceName)
    Call PropBag.WriteProperty("FirstTimeUse", m_FirstTimeUse, m_def_FirstTimeUse)
    Call PropBag.WriteProperty("DebugMode", m_DebugMode, m_def_DebugMode)
    Call PropBag.WriteProperty("ParamString", m_ParamString, m_def_ParamString)
    Call PropBag.WriteProperty("InlineSlots", m_InlineSlots, m_def_InlineSlots)
    Call PropBag.WriteProperty("Success", m_Success, True)
       
    
    Exit Sub

errorHandler:
    If errCount = 0 Then
        Call MsgBox(Err.Description & vbCrLf & "Please refresh your page", , "Need Refresh")
        errCount = errCount + 1
    End If

    
End Sub


Public Function BuildPost() As Integer
    On Error GoTo errorHandler
    Dim temp As String
    temp = vbCrLf
    Dim communityid As Long
    Dim folderid As Long
    Dim filename As String
    Dim thename As String
    Dim thevalue As String
    Dim dbactiontype As Boolean
    Dim i As Long
    Dim params
    params = Split(theParamString, "#|#|")
    For i = 0 To UBound(params)
        thename = Split(params(i), "|_|_")(0)
        thevalue = Split(params(i), "|_|_")(1)
        If thename = "DBActionType" Then
            dbactiontype = True
        End If
        If thename = "sys_communityid" And thevalue <> "" Then
            communityid = thevalue
        ElseIf thename = "sys_contentid" And thevalue <> "" Then
            contentid = thevalue
        ElseIf thename = "sys_revision" And thevalue <> "" Then
            revisionid = thevalue
        ElseIf thename = "sys_folderid" And thevalue <> "" Then
            folderid = thevalue
        ElseIf thename = theBodySourceName & "_filename" And thevalue <> "" Then
            filename = thevalue
        End If
        If thename <> "" And thename <> theBodySourceName & "_clear" Then
            temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
            temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & thename & Chr(34) & vbCrLf & vbCrLf
            temp = temp & ToUTF8(thevalue) & vbCrLf
        End If
    Next i
    Dim temps As String
    If theFirstTimeUse <> "yes" And contentid = 0 Then
        On Error Resume Next
        Err.Clear
        temps = Split(Split(theURL, "sys_contentid=")(1), "&")(0)
        contentid = CInt(temps)
        If Err.Number <> 0 Then
            BuildPost = 0
            Exit Function
        End If
        On Error GoTo errorHandler
    End If
    If theFirstTimeUse <> "yes" And revisionid = 0 Then
        On Error Resume Next
        Err.Clear
        temps = Split(Split(theURL, "sys_revision=")(1), "&")(0)
        revisionid = CInt(temps)
        If Err.Number <> 0 Then
            BuildPost = 0
            Exit Function
        End If
        On Error GoTo errorHandler
    End If
    If dbactiontype <> True Then
            temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
            temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & "DBActionType" & Chr(34) & vbCrLf & vbCrLf
            If contentid <> 0 Then
                temp = temp & "UPDATE" & vbCrLf
            Else
                temp = temp & "INSERT" & vbCrLf
            End If
    End If
    If theFirstTimeUse <> "yes" Then
        If InStr(1, temp, "sys_contentid") < 1 Then
                temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
                temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & "sys_contentid" & Chr(34) & vbCrLf & vbCrLf
                temp = temp & contentid & vbCrLf
        End If
        If InStr(1, temp, "sys_revision") < 1 Then
                temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
                temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & "sys_revision" & Chr(34) & vbCrLf & vbCrLf
                temp = temp & revisionid & vbCrLf
        End If
    End If
    If InStr(1, temp, "sys_command") < 1 Then
            temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
            temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & "sys_command" & Chr(34) & vbCrLf & vbCrLf
            temp = temp & "modify" & vbCrLf
    End If
    'Add sys_WordOCX parameter to denote that we are saving from word ocx.
    temp = temp & "--" & UPLOAD_BOUNDARY & vbCrLf
    temp = temp & "Content-Disposition: form-data; name=" & Chr(34) & "sys_WordOCX" & Chr(34) & vbCrLf & vbCrLf
    temp = temp & "yes" & vbCrLf
    
    temp = temp & "--" & UPLOAD_BOUNDARY & "--" & vbCrLf
    Dim http As New XMLHTTP40
    Dim url As String
    url = Split(theURL, ".xml?")(0) & ".xml"
    Dim sessid As String
    sessid = getParamFromLink(theURL, "pssessionid")
    'Add the pssessionid to the url
    url = url & "?pssessionid=" & sessid
    If contentid = 0 And folderid <> 0 Then
        url = url & "&psredirect=" & url & "%3Fsys_folderid%3D" & folderid
    End If
    Call http.Open("POST", url, False)
    Call http.setRequestHeader("Content-Type", "multipart/form-data; charset=utf-8; boundary=" & UPLOAD_BOUNDARY)
    Call http.setRequestHeader("Content-Length", Len(temp))
    If communityid <> 0 Then
        'We need to set the cookie twice due to the following bug.
        'BUG: XMLHTTP Fails to Send Cookies from a Client (Q290899)
        'http://support.microsoft.com/default.aspx?scid=kb;EN-US;q290899
        Call http.setRequestHeader("Cookie", "sys_community=" & communityid)
        Call http.setRequestHeader("Cookie", "sys_community=" & communityid)
    End If
'Convert the text data into bytes before sending.
'This step is needed to preserve special characters
    Dim aPostData() As Byte
    aPostData = StrConv(temp, vbFromUnicode)
    http.send aPostData
    
    If http.Status <> 200 Then
        BuildPost = 0
        Exit Function
    Else
        xmldoc.loadXML (http.responseXML.xml)
        
        'also check if the server returned HTTP 200, but the XML data has an error node in it
        Dim xmlNode As IXMLDOMNode
        
        Dim docType As String
        docType = xmldoc.documentElement.Attributes.getNamedItem("docType").Text
        
        If docType = "sys_error" Then
            BuildPost = 0
            Exit Function
        End If
        
        If theFirstTimeUse = "yes" Then
            
            Dim newcid As String
            Dim newrid As String
            Dim s1 As Variant
            newcid = xmldoc.documentElement.selectSingleNode("Workflow/BasicInfo/HiddenFormParams/Param[@name='sys_contentid']").Text
            newrid = 1
            theURL = reformatLink(theURL, newcid, newrid)
            theBodyURL = reformatLink(theBodyURL, newcid, newrid)
        End If
    End If
    BuildPost = 1
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in BuildPost function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    BuildPost = 0
    If IsObject(http) Then Set http = Nothing
End Function

Private Function addInlineSlotsToCustomProps()
    On Error GoTo errorHandler
    Dim ilslots, ilslot, i
    If m_InlineSlots <> "" Then
        ilslots = Split(m_InlineSlots, "##")
        For i = 0 To UBound(ilslots)
            ilslot = Split(ilslots(i), "#")
            If UBound(ilslot) >= 1 Then
                If ilslot(0) <> "" And ilslot(1) <> "" Then
                    addACustomProperty ("RxField:" & ilslot(0)), ilslot(1)
                End If
            End If
        Next i
    End If
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in addInlineSlotsToCustomProps function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
End Function

' mgb function to return a string converted to UTF8
Public Function ToUTF8(ByVal st As String) As String
    Const CP_UTF8 = 65001

    Dim stBuffer As String
    Dim cwch As Long
    Dim pwz As Long
    Dim pwzBuffer As Long
    
    ' get the length
    pwz = StrPtr(st)
    cwch = WideCharToMultiByte(CP_UTF8, 0, pwz, -1, 0&, 0&, ByVal 0&, ByVal 0&)
    
    ' clear the buffer
    stBuffer = String$(cwch + 1, vbNullChar)
    pwzBuffer = StrPtr(stBuffer)
    
    ' convert to UTF8
    cwch = WideCharToMultiByte(CP_UTF8, 0, pwz, -1, pwzBuffer, Len(stBuffer), ByVal 0&, ByVal 0&)
    
    ' convert to VB string and return
    ToUTF8 = Left$(StrConv(stBuffer, vbUnicode), cwch - 1)
End Function
Public Function CorrectInlineLinks() As Integer
'Function to correct the contentids and revisions inside the inline links
    On Error GoTo errorHandler
    'Loop through all the links and prepare the semicolon list of contentids
    Dim inlinecontentids As String
    inlinecontentids = ";"
    Dim hLink As Word.Hyperlink
    Dim linkadd, cidvalue As String
    Dim ii As Integer
    For Each hLink In theDoc.Hyperlinks
        linkadd = hLink.Address
        linkadd = Replace(linkadd, "&amp;", "&")
        cidvalue = getContentIdFromLink(linkadd)
        'Add only if cidvalue is not empty and we have not added it already
        If Len(cidvalue) > 0 And InStr(inlinecontentids, ";" & cidvalue & ";") = 0 Then
            inlinecontentids = inlinecontentids & cidvalue & ";"
        End If
    Next hLink
    Dim iShape As InlineShape
    For Each iShape In theDoc.InlineShapes
        linkadd = iShape.LinkFormat.SourceFullName
        linkadd = Replace(linkadd, "&amp;", "&")
        cidvalue = getContentIdFromLink(linkadd)
        'Add only if cidvalue is not empty and we have not added it already
        If Len(cidvalue) > 0 And InStr(inlinecontentids, ";" & cidvalue & ";") = 0 Then
            inlinecontentids = inlinecontentids & cidvalue & ";"
        End If
    Next iShape
    'If the inlinecontentids is not empty, there may be bad content ids or
    'revisions or both. Get the corrected ids and revisons from Rhythmyx.
    If inlinecontentids = ";" Then
        'No inline links return here.
        CorrectInlineLinks = 1
        Exit Function
    End If
    'get rid of the leading and trailing ;
    inlinecontentids = Right(inlinecontentids, Len(inlinecontentids) - 1)
    inlinecontentids = Left(inlinecontentids, Len(inlinecontentids) - 1)
    'Make a request to the server to get the corrected ids and revisions
    'Build the url string
    Dim updatecidsurl As String
    updatecidsurl = Split(theURL, "Rhythmyx")(0) & "Rhythmyx/sys_clientSupport/updateinlinecontent.xml?"
    updatecidsurl = updatecidsurl & "sys_contentid=" & contentid
    updatecidsurl = updatecidsurl & "&sys_revision=" & revisionid
    updatecidsurl = updatecidsurl & "&inlinecontentids=" & inlinecontentids
    'Make a call using xml http
    Dim cidHttp As New MSXML2.XMLHTTP40
    cidHttp.Open "GET", updatecidsurl, False
    cidHttp.send
    'Check the status
    If cidHttp.Status <> 200 Then
        'Oops there is an error log it and return 0 to indicate update failed.
        CorrectInlineLinks = 0
        strLog = strLog & vbCrLf & "Http Error occured in CorrectInlineLinks function."
        strLog = strLog & vbCrLf & "Http Error Number = " & cidHttp.Status & " Http Error Description = " & cidHttp.responseText
        strLog = strLog & vbCrLf & vbCrLf
        CorrectInlineLinks = 0
        If IsObject(cidHttp) Then Set cidHttp = Nothing
        Exit Function
    End If
    'Now update the links with new contentids and new revisions
    Dim elem As IXMLDOMElement
    Dim doc As New DOMDocument40
    doc.loadXML (cidHttp.responseXML.xml)
    Dim xpathStr, newcontentid, newrevision, addpart1, addpart2, thename, thevalue, newlink As String
    
    'Loop through all the links again
    For Each hLink In theDoc.Hyperlinks
        linkadd = hLink.Address
        linkadd = Replace(linkadd, "&amp;", "&")
        cidvalue = getContentIdFromLink(linkadd)
        'Continue with the next link if the contentid value is empty
        If Len(cidvalue) = 0 Then GoTo linkLoop
        'Find the correctedid and revision
        xpathStr = "Child[@oldContentId=" & cidvalue & "]"
        Set elem = doc.documentElement.selectSingleNode(xpathStr)
        'We could not find a corresponding elem in the returned document continue
        If elem Is Nothing Then GoTo linkLoop
        newcontentid = elem.getAttribute("newContentId")
        newrevision = elem.getAttribute("newRevision")
        'For some reason we could not get the new content id continue with the next link
        If Len(newcontentid) = 0 Or Len(newrevision) = 0 Then GoTo linkLoop
        'We have the corrected ids and revisions replace them in the link
        newlink = reformatLink(linkadd, newcontentid, newrevision)
        If Len(newlink) = 0 Then GoTo linkLoop
        hLink.Address = newlink
linkLoop:
    Next hLink
    'Loop through all the images
    For Each iShape In Application.ActiveDocument.InlineShapes
        linkadd = iShape.LinkFormat.SourceFullName
        linkadd = Replace(linkadd, "&amp;", "&")
        cidvalue = getContentIdFromLink(linkadd)
        'Continue with the next link if the contentid value is empty
        If Len(cidvalue) = 0 Then GoTo imgLoop
        'Find the correctedid and revision
        xpathStr = "Child[@oldContentId=" & cidvalue & "]"
        Set elem = doc.documentElement.selectSingleNode(xpathStr)
        'We could not find a corresponding elem in the returned document continue
        If elem Is Nothing Then GoTo imgLoop
        newcontentid = elem.getAttribute("newContentId")
        newrevision = elem.getAttribute("newRevision")
        'For some reason we could not get the new content id continue with the next link
        If Len(newcontentid) = 0 Or Len(newrevision) = 0 Then GoTo imgLoop
        'We have the corrected ids and revisions replace them in the link
        newlink = reformatLink(linkadd, newcontentid, newrevision)
        If Len(newlink) = 0 Then GoTo imgLoop
        iShape.LinkFormat.SourceFullName = newlink
imgLoop:
    Next iShape
    
    'We are done with the links return
    CorrectInlineLinks = 1
    Exit Function
errorHandler:
    strLog = strLog & vbCrLf & "Error occured in CorrectInlineLinks function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    CorrectInlineLinks = 0
    If IsObject(cidHttp) Then Set cidHttp = Nothing
End Function

Function getContentIdFromLink(linkadd) As String
    getContentIdFromLink = getParamFromLink(linkadd, "sys_contentid")
End Function
Function getParamFromLink(linkadd, paramName) As String
'Utility function to return the value of the supplied param name from the supplied link
'If the link is empty or if param does not exist then returns empty string
On Error GoTo errorHandler
    Dim linkaddar, thename, thevalue As String
    Dim ii As Integer
    
    If Len(linkadd) = 0 Or InStr(linkadd, paramName) = 0 Then
        getParamFromLink = ""
        Exit Function
    End If
    'Replace the &amp; with & in the address
    linkaddar = Replace(linkadd, "&amp;", "&")
    linkaddar = Split(Split(linkadd, "?")(1), "&")
    Dim nvpair
    For ii = 0 To UBound(linkaddar)
        nvpair = Split(linkaddar(ii), "=")
        'This may happen only on the last param of the link it may have a param with no value
        If UBound(nvpair) < 1 Then Exit For
        thename = nvpair(0)
        thevalue = nvpair(1)
        If thename = paramName Then
            getParamFromLink = thevalue
            Exit Function
        End If
    Next ii
    getParamFromLink = ""
Exit Function
errorHandler:
'Log the error
    strLog = strLog & vbCrLf & "Error occured in CorrectInlineLinks function."
    strLog = strLog & vbCrLf & "Error Number = " & Err.Number & " Error Description = " & Err.Description
    strLog = strLog & vbCrLf & vbCrLf
    getParamFromLink = ""
End Function


Function reformatLink(linkadd, newcontentid, newrevision) As String
On Error GoTo linkError
    Dim addpart1, addpart2, thename, thevalue As String
    Dim linkaddar, nvpair
    Dim ii As Integer
    
    'We have the corrected ids and revisions replace them in the link
    addpart1 = Split(linkadd, "?")(0) & "?"
    addpart2 = ""
    linkaddar = Split(Split(linkadd, "?")(1), "&")
    For ii = 0 To UBound(linkaddar)
        nvpair = Split(linkaddar(ii), "=")
        'This may happen only on the last param of the link it may have a param with no value
        If UBound(nvpair) < 1 Then Exit For
        thename = nvpair(0)
        thevalue = nvpair(1)
        If thename = "sys_dependentid" Or thename = "sys_contentid" Then
            addpart2 = addpart2 & thename & "=" & newcontentid & "&"
        ElseIf thename = "sys_revision" Then
            addpart2 = addpart2 & thename & "=" & newrevision & "&"
        Else
            addpart2 = addpart2 & thename & "=" & thevalue & "&"
        End If
    Next ii
    'Remove if there are any trailing & in the link
    If addpart2 <> "" Then addpart2 = Left(addpart2, Len(addpart2) - 1)
    'Set the full link
    reformatLink = addpart1 & addpart2
    Exit Function
linkError:
    reformatLink = ""
End Function






