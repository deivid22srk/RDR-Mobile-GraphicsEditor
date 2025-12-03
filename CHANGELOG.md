# Changelog - RDR Mobile Graphics Editor v2.0

## Version 2.0 - Dynamic Edition

### 🎯 Major Changes

#### 1. Dynamic XML Loading
- The app no longer has a hardcoded graphics.xml file
- Reads `/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml` at startup
- Automatically parses all fields and generates UI dynamically
- Future-proof: If the game updates the XML structure, the app adapts automatically

#### 2. Partial Line Modification
- Only modifies the specific lines that were changed by the user
- Preserves the original file structure and formatting
- More efficient and safer than replacing the entire file
- Reduces risk of corruption or unintended changes

#### 3. Efficient Root Access
- Uses **libsu** library instead of shell commands for better performance
- Requests root access on app startup
- More secure and reliable root operations
- Better error handling and logging

#### 4. Gofile Integration
- New Settings tab with upload functionality
- Compress the entire project (including hidden files)
- Upload to Gofile with progress tracking
- Share your modifications easily

### 📱 New Features

#### Dynamic Graphics Screen
- Auto-generates UI based on XML content
- Groups settings into logical categories:
  - Display
  - Graphics Quality
  - Motion Blur
  - Upscaling
  - HDR
  - Advanced
- Shows "Modified" indicator for changed fields
- Smart inference of min/max values for sliders

#### Settings Screen
- Project information
- Upload to Gofile with real-time progress
- Feature list with descriptions
- Clean Material Design 3 interface

### 🔧 Technical Improvements

#### New Classes
- `XmlField.kt` - Data models for different field types (Boolean, Int, Float)
- `XmlParser.kt` - Dynamic XML parser with smart type detection
- `GofileUploader.kt` - Gofile API integration
- `ProjectCompressor.kt` - ZIP compression with progress tracking
- `DynamicGraphicsScreen.kt` - Dynamic UI generation
- `SettingsScreen.kt` - Settings and export functionality

#### Updated Classes
- `RootManager.kt` - Added `parseGraphicsConfig()` and `writePartialGraphicsConfig()`
- `MainActivity.kt` - Request root on startup
- `MainScreen.kt` - Added Settings tab navigation
- `AndroidManifest.xml` - Added INTERNET permission

### 🎨 UI Improvements
- Better loading states with progress indicators
- Clear error messages when root access fails
- Modified fields are visually marked
- Grouped settings for better organization
- Real-time progress for compression and upload

### 🔒 Security
- No shell command injection vulnerabilities
- Secure root operations via libsu
- Proper error handling throughout
- No hardcoded credentials or sensitive data

### 📦 Dependencies
All dependencies remain the same:
- libsu 5.2.2 - For root access
- Jetpack Compose - For UI
- Material 3 - For design components

### 🚀 Usage

1. Install the app
2. Grant root access when prompted
3. The app will automatically read the graphics.xml
4. Modify any settings you want
5. Tap "Apply Changes" to save (only modified lines are updated)
6. Use Settings tab to upload your project to Gofile

### 📝 Notes

- Make sure RDR Mobile (com.netflix.NGP.Kamo) is installed
- Root access is required
- The app adapts to any future changes in graphics.xml structure
- Original file formatting is preserved
