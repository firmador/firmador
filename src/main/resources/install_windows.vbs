Set WshShell = WScript.CreateObject("WScript.Shell") 
Set sh = CreateObject("WScript.Shell")
Set shortcut = sh.CreateShortcut(WshShell.ExpandEnvironmentStrings("%PROGRAMDATA%\Microsoft\Windows\Start Menu\Programs\firmadorlibre.lnk"))
shortcut.TargetPath = "java.exe"
shortcut.Arguments = WshShell.ExpandEnvironmentStrings("-jar %AppData%\firmadorlibre\firmador.jar")
shortcut.IconLocation = WshShell.ExpandEnvironmentStrings("%AppData%\firmadorlibre\icon.ico")
shortcut.Save
