Set WshShell = WScript.CreateObject("WScript.Shell") 
Set sh = CreateObject("WScript.Shell")
Set shortcut = sh.CreateShortcut(WshShell.ExpandEnvironmentStrings("%PROGRAMDATA%\Microsoft\Windows\Start Menu\Programs\firmadorlibre.lnk"))
shortcut.TargetPath = "java.exe"
shortcut.Arguments = WshShell.ExpandEnvironmentStrings("-jar %AppData%\firmadorlibre\firmador.jar")
shortcut.IconLocation = WshShell.ExpandEnvironmentStrings("%AppData%\firmadorlibre\icon.ico")
shortcut.Save

' Definir el nombre del protocolo
protocolNameflsign = "flsign"
protocolNameflauth = "flauth"
' Definir la ruta a la aplicación que se debe abrir
appPath = "java.exe -jar %AppData%\firmadorlibre\firmador.jar"
iconPath = "%AppData%\firmadorlibre\icon.ico"

protocolName = protocolNameflsign
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\", "URL:flsign"
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\URL Protocol", ""
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\shell\open\command\", """" & appPath & """ ""%1"""
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\DefaultIcon\", """" & iconPath & """,0"

protocolName = protocolNameflauth
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\", "URL:flauth"
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\URL Protocol", ""
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\shell\open\command\", """" & appPath & """ ""%1"""
sh.RegWrite "HKEY_CLASSES_ROOT\" & protocolName & "\DefaultIcon\", """" & iconPath & """,0"
