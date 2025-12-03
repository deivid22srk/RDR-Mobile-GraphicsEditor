# Changelog - RDR Mobile Graphics Editor v2.1

## Version 2.1 - File Explorer Edition

### 🎯 Major New Features

#### 1. Built-in File Explorer
- **NEW**: Browse system files starting from `/data/user/0/`
- Navigate through folders with back button and path display
- Visual file listing with permissions and sizes
- XML files are highlighted with special badges
- Tap any XML file to open it in the editor

#### 2. Universal XML Editor
- No longer limited to just graphics.xml
- Edit ANY XML file from anywhere on the system
- Dynamic path display shows currently loaded file
- All editing features work on any XML structure

#### 3. Enhanced Navigation
- 4 tabs: Files, Editor, Language, Settings
- Files tab: Browse and select XML files
- Editor tab: Edit selected XML (disabled until file selected)
- Language & Settings tabs remain the same
- Close button in top bar to exit editor and return to file browser

### 📱 New Features

#### File Explorer Screen
- Starts at `/data/user/0/` by default
- Shows folders and files with icons
- Displays file permissions and sizes
- XML files marked with "XML" badge
- Directories navigable with tap
- Back button to go to parent folder
- Refresh button to reload current directory
- Empty state and error handling
- Root access verification

#### Dynamic Editor Improvements
- Accepts any XML file path
- Shows current file path at top
- All modifications save to the selected file
- Preserves file location and permissions

### 🔧 Technical Improvements

#### New Files
- `FileItem.kt` - Data model for files and folders
- `FileExplorerScreen.kt` - Complete file browser UI

#### Updated Files
- `RootManager.kt`:
  - `readXmlFile(path)` - Read any XML file
  - `parseXmlFile(path)` - Parse any XML file
  - `writeXmlFile(path, content)` - Write to any XML file
  - `writePartialXmlFile(path, parsedXml)` - Partial update any XML
  - `listDirectory(path)` - List files in directory
  - `directoryExists(path)` - Check directory existence

- `DynamicGraphicsScreen.kt`:
  - Now accepts `xmlFilePath` parameter
  - Shows file path in header
  - Saves to correct file location

- `MainScreen.kt`:
  - Complete redesign with 4 tabs
  - State management for selected file
  - Dynamic tab enabling/disabling

### 🎨 UI Improvements
- File icons based on type (folder, XML, generic file)
- Color coding: folders (primary), XML files (tertiary)
- Monospace font for file paths and permissions
- Info card explaining how to select files
- Breadcrumb-style path display
- Material Design 3 throughout

### 🔒 Security & Permissions
- Root access required for file browsing
- Graceful error handling when access denied
- File permissions preserved on write
- Directory creation if needed

### 🚀 Usage

1. Open app → Files tab opens by default
2. Browse from `/data/user/0/` or any subfolder
3. Navigate folders by tapping
4. Tap any XML file to open in editor
5. Editor tab becomes enabled
6. Modify XML fields as needed
7. Apply Changes saves to the selected file
8. Close button returns to file browser

### 📝 Example Use Cases

**Edit Game Graphics:**
1. Browse to `/data/user/0/com.netflix.NGP.Kamo/files/`
2. Tap `graphics.xml`
3. Edit settings
4. Apply Changes

**Edit Any App Config:**
1. Browse to `/data/user/0/com.example.app/`
2. Find any XML file
3. Edit it dynamically
4. Save changes

**Explore System Files:**
1. Browse `/data/user/0/`
2. See all installed app data
3. Find XML configurations
4. Edit them universally

### 🎉 Benefits

- **Flexibility**: Edit ANY XML file, not just graphics.xml
- **Discovery**: Browse to find XML files you didn't know existed
- **Safety**: Still only modifies the lines you change
- **Convenience**: No need to know exact file paths
- **Future-proof**: Works with any app's XML files

### 📦 File Structure
```
app/src/main/java/com/rdrgraphics/editor/
├── MainActivity.kt
├── MainScreen.kt (REDESIGNED)
├── data/
│   ├── FileItem.kt (NEW)
│   ├── GraphicsConfig.kt
│   ├── LanguageConfig.kt
│   ├── XmlField.kt
│   └── XmlParser.kt
├── ui/screens/
│   ├── DynamicGraphicsScreen.kt (UPDATED)
│   ├── FileExplorerScreen.kt (NEW)
│   ├── GraphicsScreen.kt
│   ├── LanguageScreen.kt
│   └── SettingsScreen.kt
└── utils/
    ├── GofileUploader.kt
    ├── ProjectCompressor.kt
    ├── RootManager.kt (UPDATED)
    └── RootManagerEnhanced.kt
```

### ✅ What's Included

From v2.0:
- ✅ Dynamic XML parsing
- ✅ Partial line modification
- ✅ Efficient root access (libsu)
- ✅ Gofile integration

New in v2.1:
- ✅ File explorer starting at /data/user/0/
- ✅ Browse folders and files
- ✅ Select any XML file
- ✅ Universal XML editor
- ✅ Path-aware file operations

### 🔗 Compatibility

- Android 8.0+ (API 26+)
- Root access required
- Works with any app's XML files
- Tested on RDR Mobile but universal
