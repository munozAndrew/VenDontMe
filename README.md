Project Description
Split It is a collaborative expense management Android application that allows groups of friends to easily split bills and track shared expenses. Users can create groups, 
add friends, capture receipt photos using their phone's camera, manually input itemized expenses, and assign items to specific group members. The app automatically calculates 
who owes what and maintains a complete history of all shared expenses within each group. Built with modern Android development practices using Kotlin, Jetpack Compose, and Supabase as the backend.

Team Members

Anthony Mendoza
Andrew Munoz


Figma Design
https://www.figma.com/design/JDTRcOcOvzOwNiUNPKUPHO/app-reciept-scanner?node-id=0-1&t=HWsk0ugABpqDx72n-1 
Alternatively, you can include exported PNG screenshots of your key screens here:
Key Screens:

Sign In / Sign Up
Home Screen (Groups List)
Group Detail (Members & Receipts)
Camera Receipt Capture
Add Receipt Items
Receipt Detail (View & Edit)
Friends Management


Features & Technologies
Android & Jetpack Compose Features
Core Android Components:

Single Activity Architecture with proper lifecycle management
Jetpack Compose for fully declarative UI
Navigation Component with type-safe routing using @Serializable data classes
ViewModel for state management and configuration change handling
StateFlow for reactive state updates
Coroutines for asynchronous operations

UI Components:

Material 3 Design system (Material You)
Scaffold with TopAppBar, FAB, and BottomNavigation
LazyColumn for efficient scrollable lists
AsyncImage (Coil) for remote image loading
Card, TextField, Button, and other Material components
Custom Composables for reusable UI elements

Camera & Media:

Camera Integration using ActivityResultContracts.TakePicture()
FileProvider for secure photo capture and storage
MediaStore for saving images to gallery
Image Preview with approve/retake functionality

State Management:

ViewModel with mutableStateOf for reactive UI updates
State Hoisting pattern for component reusability
Remember and rememberSaveable for configuration changes
LaunchedEffect for side effects and auto-refresh

Navigation:

Type-Safe Navigation using sealed interface and Kotlin serialization
NavHost and NavController for screen routing
Deep linking support with URI parameters
Back stack management with proper popBackStack usage

Data Persistence:

Supabase PostgreSQL database for all app data
Supabase Storage for receipt image hosting
Row Level Security (RLS) for data access control
Real-time sync across devices and group members

Permissions:

Camera Permission handling using Accompanist Permissions library
Storage Permissions (Android 12 and below)
Runtime Permission Requests with proper user feedback

Third-Party Libraries
Supabase Kotlin SDK (io.github.jan-tennert.supabase)

Authentication with email/password
PostgreSQL database (Postgrest)
Storage for receipt images
Real-time subscriptions (future enhancement)

Why we chose it: Supabase provides a complete backend-as-a-service with authentication, database, and file storage out of the box, allowing us to focus on the Android app rather than building a custom backend.
Coil (io.coil-kt:coil-compose)

Async image loading from Supabase Storage
Automatic caching and memory management
Compose-native integration

Why we chose it: Coil is the recommended image loading library for Jetpack Compose and has excellent performance.
Accompanist Permissions (com.google.accompanist:accompanist-permissions)

Declarative permission handling in Compose
Clean API for requesting camera permissions

Why we chose it: Provides a Compose-first approach to handling runtime permissions.
Kotlinx Serialization (org.jetbrains.kotlinx:kotlinx-serialization-json)

Type-safe data serialization
Navigation route parameters
API communication with Supabase


Database Schema
Tables

profiles - User profile information
groups - Group details (name, description, creator)
group_members - Group membership with roles (admin/member)
friends - Friend relationships with status (pending/accepted)
receipts - Receipt metadata (total, description, image URL)
receipt_items - Individual items on receipts
receipt_item_claims - Item assignments to users (for splitting)

Row Level Security (RLS)
All tables have RLS policies to ensure users can only access their own data and data from groups they're members of.


Key Workflows
1. User Authentication

Sign up with email, username, and display name
Email verification (optional)
Sign in with existing credentials
Profile creation in database

2. Friend Management

Search users by username
Send friend requests
Accept/reject incoming requests
View friends list

3. Group Creation & Management

Create groups with name and description
Creator automatically becomes admin
Add friends as members
View group members and receipts

4. Receipt Workflow

Click "New Split" in group
Camera opens to capture receipt photo
Image preview with approve/retake
Manually enter items (name, price, quantity)
Add optional description
Real-time total calculation
Save to database and storage
Receipt appears in group history

5. Receipt Management

Click receipt to view details
Edit mode: modify items, prices, description
Assign items to group members
Split items among multiple people
View split summary (who owes what)
Automatic even splitting


Configuration Change Handling
The app properly handles configuration changes (screen rotation) through:

ViewModels that survive configuration changes
State hoisting to maintain UI state
savedInstanceState for critical navigation state
rememberSaveable for transient UI state
All network operations in ViewModel scope (not lifecycle-dependent)

Dependencies & Requirements
Minimum Android Version

minSdk: 24 (Android 7.0 Nougat)
targetSdk: 34 (Android 14)
compileSdk: 34

Required Device Features

Camera - For receipt capture functionality
Internet Connection - Required for all features (Supabase backend)
Storage Access - For saving receipt photos to gallery

Build Configuration
Key dependencies in app/build.gradle.kts

Compose
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")
implementation("androidx.navigation:navigation-compose:2.7.5")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

Supabase
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:auth-kt:2.0.0")

Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

Permissions
implementation("com.google.accompanist:accompanist-permissions:0.34.0")


we also use SUPABASE API keys to connect to the backend
These are accessed via BuildConfig.SUPABASE_URL and BuildConfig.SUPABASE_ANON_KEY at runtime.

Known Limitations & Future Enhancements
Current Limitations

No offline mode (requires internet connection)
Receipt item assignment doesn't persist across app restarts (future: store in receipt_item_claims table)
Dark mode follows system settings only (no in-app toggle)
No receipt search/filter functionality

Planned Enhancements

Real-time updates using Supabase subscriptions
Push notifications for friend requests and group invites
Receipt OCR (automatic text extraction from photos)
Export receipts as PDF
Payment settlement tracking (mark debts as paid)
Group settings and permissions
User profile customization
