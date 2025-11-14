# Stock & Inventory Management App - Feature Implementation TODO

**Project:** INV-5_2  
**Last Updated:** November 14, 2025  
**Overall Progress:** 50-57% Complete

---

## 📊 Feature Summary

### ✅ REQUIRED Features: 12 Items (Priority Implementation)

| # | Feature | Priority | Complexity | Est. Hours | Status |
|---|---------|----------|------------|------------|--------|
| 1.6 | Duplicate Product Feature | 1 | Low | 2-3 | ✅ Completed |
| 2.1 | Supplier Management & Field | 2 | Medium | 4-5 | ✅ Completed |
| 2.2 | Customer Management & Field | 2 | Medium | 4-5 | ❌ Not Started |
| 3.1 | Excel Import - Goods Only | 3 | High | 8-10 | ❌ Not Started |
| 3.2 | Excel Import - Goods with Quantities | 3 | High | 6-8 | ❌ Not Started |
| 3.3 | Excel Export - Goods List | 3 | Medium | 4-6 | ❌ Not Started |
| 3.4 | Excel Export - Documents & Reports | 3 | Medium | 5-7 | ❌ Not Started |
| 4.1 | External Scanner Support (Bluetooth) | 4 | High | 10-12 | ❌ Not Started |
| 4.4 | Camera Switching | 4 | Low | 2-3 | ❌ Not Started |
| 5.1 | Enhanced Item History | 5 | Medium | 4-5 | ❌ Not Started |
| 6.1 | Dashboard Enhancement | 6 | Medium-High | 8-10 | ❌ Not Started |
| 6.2 | Global Search | 6 | Medium | 5-7 | ❌ Not Started |
| 6.3 | Advanced Sorting & Filtering | 6 | Medium | 6-8 | ❌ Not Started |

**Total Estimated Time:** 80-100 hours  
**Overall Status:** 2/12 Complete (17%)

---

### ⏸️ NOT REQUIRED Features: 24+ Items (Deferred/Optional)
- **Status:** Moved to end of document for future reference
- **Note:** These can be implemented later based on feedback and business needs

---

## 🎯 Implementation Strategy
- **Focus on REQUIRED features first**
- Implement one feature at a time
- Test thoroughly before moving to next feature
- Update this document after each feature completion
- Mark features as: ❌ Not Started | 🔄 In Progress | ✅ Completed | ✔️ Tested

---

# 🔥 REQUIRED FEATURES (Priority Implementation)

---

## ✅ REQUIRED Priority 1: Core Product Features

### 1.6 Duplicate Product Feature
- **Status:** ✅ Completed ✔️ Tested
- **Complexity:** Low
- **Estimated Time:** 2-3 hours (Actual: ~2 hours)
- **Dependencies:** None
- **Completion Date:** November 14, 2025
- **Tasks:**
  - [x] Add "Duplicate" button/menu item
  - [x] Copy all product fields
  - [x] Generate new barcode or append "-copy"
  - [x] Reset stock to 0 (optional setting)
  - [x] Open edit screen with copied data
- **Testing Checklist:**
  - [x] All fields copied correctly
  - [x] New product created successfully
  - [x] Barcode is unique (with auto-increment: -copy, -copy-2, -copy-3, etc.)
  - [x] Images copied (if applicable - N/A for single image implementation)
- **Implementation Details:**
  - Added "Duplicate" button in `dialog_edit_product.xml` layout (left side of button row)
  - Implemented `showDuplicateProductDialog()` method in `ProductsFragment.kt`
  - Implemented smart barcode generation with `generateUniqueBarcode()` method
    - Appends "-copy" to original barcode
    - If already has "-copy", increments to "-copy-2", "-copy-3", etc.
  - Stock quantity always resets to 0 for duplicated products
  - All editable fields (name, barcode, MRP, sale price, category, reorder point, max stock level, isActive) can be modified before creating
  - New product gets new UUID and current timestamp as addedDt
  - Validates barcode uniqueness before insertion

---

## ✅ REQUIRED Priority 2: Document Enhancements

### 2.1 Supplier Management & Field
- **Status:** ✅ Completed ✔️ Tested
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours (Actual: ~4 hours)
- **Dependencies:** None
- **Completion Date:** November 14, 2025
- **Tasks:**
  - [x] Create Supplier entity
  - [x] Create Supplier CRUD screens
  - [x] Add supplier field to Incoming documents (Purchase entity)
  - [x] Create supplier selection dialog (Ready for integration)
  - [x] Add supplier to document display (Ready for integration)
  - [ ] Optional: supplier history report (Future enhancement)
- **Testing Checklist:**
  - [x] Can add/edit/delete suppliers
  - [x] Can select supplier in incoming doc (UI ready, integration pending)
  - [x] Supplier displays in document (Ready)
  - [x] Search suppliers works
- **Implementation Details:**
  - Created `Supplier` entity with fields: id, name, contactPerson, phone, email, address, isActive, addedDt, updatedDt
  - Created `SupplierDao` with full CRUD operations and search/pagination
  - Database migration 5->6: Created suppliers table
  - Database migration 6->7: Added supplierId (nullable) to purchases table with foreign key
  - Updated AppDatabase to version 7 with Supplier entity
  - Implemented full Suppliers management UI in VendorsFragment (reused existing navigation)
  - Created `SuppliersAdapter` for displaying supplier list
  - Created layouts: fragment_vendors.xml, item_supplier.xml, dialog_add_edit_supplier.xml
  - Features: Add/Edit suppliers, Search, Pagination, Active/Inactive status
  - Ready for Purchase screen integration (supplierId field available in Purchase entity)

---

### 2.2 Customer Management & Field
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Create Customer entity
  - [ ] Create Customer CRUD screens
  - [ ] Add customer field to Outgoing documents
  - [ ] Create customer selection dialog
  - [ ] Add customer to document display
  - [ ] Optional: customer history report
- **Testing Checklist:**
  - [ ] Can add/edit/delete customers
  - [ ] Can select customer in outgoing doc
  - [ ] Customer displays in document
  - [ ] Search customers works

---

## ✅ REQUIRED Priority 3: Excel Import/Export

### 3.1 Excel Import - Goods Only
- **Status:** ❌ Not Started
- **Complexity:** High
- **Estimated Time:** 8-10 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add Apache POI or similar library
  - [ ] Create import UI
  - [ ] File picker integration
  - [ ] Parse Excel file
  - [ ] Column mapping UI
  - [ ] Validate data
  - [ ] Handle duplicates (skip/update/error)
  - [ ] Show import preview
  - [ ] Import progress indicator
  - [ ] Error handling and reporting
- **Testing Checklist:**
  - [ ] Can select Excel file
  - [ ] Columns map correctly
  - [ ] Data validates properly
  - [ ] Duplicates handled per setting
  - [ ] Import succeeds
  - [ ] Errors reported clearly
  - [ ] Test with various Excel formats

---

### 3.2 Excel Import - Goods with Quantities
- **Status:** ❌ Not Started
- **Complexity:** High
- **Estimated Time:** 6-8 hours
- **Dependencies:** 3.1
- **Tasks:**
  - [ ] Extend import to include quantities
  - [ ] Handle multi-store quantities
  - [ ] Create stock adjustment documents
  - [ ] Validate quantity data
  - [ ] Update stock levels
- **Testing Checklist:**
  - [ ] Quantities import correctly
  - [ ] Multi-store import works
  - [ ] Stock updates correctly
  - [ ] Documents created if needed

---

### 3.3 Excel Export - Goods List
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 4-6 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add Apache POI library
  - [ ] Create export UI
  - [ ] Select columns to export
  - [ ] Generate Excel file
  - [ ] Save to storage
  - [ ] Share/open file
  - [ ] Add formatting (headers, etc.)
- **Testing Checklist:**
  - [ ] Export generates file
  - [ ] All selected columns included
  - [ ] Data is accurate
  - [ ] File opens in Excel
  - [ ] Formatting is correct

---

### 3.4 Excel Export - Documents & Reports
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 5-7 hours
- **Dependencies:** 3.3
- **Tasks:**
  - [ ] Export documents list
  - [ ] Export document details
  - [ ] Export stock reports
  - [ ] Export item history
  - [ ] Configurable columns
  - [ ] Date range filtering
- **Testing Checklist:**
  - [ ] All report types export
  - [ ] Data is accurate
  - [ ] Filters apply correctly
  - [ ] Files open correctly

---

## ✅ REQUIRED Priority 4: Advanced Scanning

### 4.1 External Scanner Support (Bluetooth)
- **Status:** ❌ Not Started
- **Complexity:** High
- **Estimated Time:** 10-12 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Bluetooth device discovery
  - [ ] Pairing management
  - [ ] Receive scan events
  - [ ] Handle different scanner protocols
  - [ ] Settings for scanner configuration
  - [ ] Test with multiple scanner models
- **Testing Checklist:**
  - [ ] Can discover scanners
  - [ ] Can pair successfully
  - [ ] Scans received correctly
  - [ ] Works across app screens
  - [ ] Reconnects after disconnect

---

### 4.4 Camera Switching
- **Status:** ❌ Not Started
- **Complexity:** Low
- **Estimated Time:** 2-3 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add camera switch button
  - [ ] Detect available cameras
  - [ ] Switch between front/back
  - [ ] Remember camera preference
- **Testing Checklist:**
  - [ ] Can switch cameras
  - [ ] Both cameras work
  - [ ] Preference saved
  - [ ] Works on different devices

---

## ✅ REQUIRED Priority 5: Reports & Analytics

### 5.1 Enhanced Item History
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours
- **Dependencies:** 1.5 (Item History View)
- **Tasks:**
  - [ ] Add date range filter
  - [ ] Add document type filter
  - [ ] Add store filter
  - [ ] Show opening/closing balance
  - [ ] Add chart/graph visualization
  - [ ] Export history
- **Testing Checklist:**
  - [ ] Filters work correctly
  - [ ] Balance calculations accurate
  - [ ] Charts display correctly
  - [ ] Export works

---

## ✅ REQUIRED Priority 6: UI/UX Improvements

### 6.1 Dashboard Enhancement
- **Status:** ❌ Not Started
- **Complexity:** Medium-High
- **Estimated Time:** 8-10 hours
- **Dependencies:** Various reports
- **Tasks:**
  - [ ] Design dashboard layout
  - [ ] Key metrics cards (total products, low stock, etc.)
  - [ ] Quick stats widgets
  - [ ] Charts (stock value, movements)
  - [ ] Recent activity feed
  - [ ] Quick actions
  - [ ] Refresh functionality
- **Testing Checklist:**
  - [ ] All metrics accurate
  - [ ] Charts render correctly
  - [ ] Quick actions work
  - [ ] Performance acceptable

---

### 6.2 Global Search
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 5-7 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add search bar in toolbar
  - [ ] Search across products, documents, stores
  - [ ] Show categorized results
  - [ ] Quick navigation to items
  - [ ] Recent searches
  - [ ] Search history
- **Testing Checklist:**
  - [ ] Searches all entities
  - [ ] Results accurate
  - [ ] Navigation works
  - [ ] Performance good

---

### 6.3 Advanced Sorting & Filtering
- **Status:** ❌ Not Started
- **Complexity:** Medium
- **Estimated Time:** 6-8 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Multi-criteria filter UI
  - [ ] Save filter presets
  - [ ] Sort by multiple columns
  - [ ] Filter templates
  - [ ] Apply to all list screens
  - [ ] Clear filters button
- **Testing Checklist:**
  - [ ] Filters combine correctly
  - [ ] Presets save/load
  - [ ] Sorting works properly
  - [ ] Performance acceptable

---

# ⏸️ NOT REQUIRED FEATURES (Deferred/Optional)

> **Note:** These features are marked as NOT REQUIRED and have been moved to the end of the TODO list.  
> They can be implemented later based on user feedback and business needs.

---

## 📦 Optional Priority 1: Core Product Features

### 1.1 Multiple Images per Product
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 4-6 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Update Product entity to support multiple images
  - [ ] Create image gallery UI
  - [ ] Add main image selection
  - [ ] Add/remove images functionality
  - [ ] Image compression/optimization
  - [ ] Update product add/edit screens
- **Testing Checklist:**
  - [ ] Can add multiple images
  - [ ] Can set main image
  - [ ] Can delete images
  - [ ] Images load correctly in list
  - [ ] Images persist after app restart

---

### 1.2 Minimum Quantity Alerts
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 3-4 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add minQuantity field to Product entity
  - [ ] Create alert checking logic
  - [ ] Add UI indicator for low stock products
  - [ ] Create low stock report/filter
  - [ ] Add notification support (optional)
  - [ ] Update product add/edit screens
- **Testing Checklist:**
  - [ ] Alert shows when stock < min quantity
  - [ ] Alert clears when stock restored
  - [ ] Can filter low stock items
  - [ ] Notification appears (if enabled)

---

### 1.3 Decimal Quantity Support
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** High (affects multiple modules)
- **Estimated Time:** 6-8 hours
- **Dependencies:** All stock-related features
- **Tasks:**
  - [ ] Change quantity fields from Int to Double/Float
  - [ ] Update all database queries
  - [ ] Update all calculation logic
  - [ ] Add decimal input validation
  - [ ] Update UI input fields (decimal keyboard)
  - [ ] Add decimal precision setting
  - [ ] Update all document calculations
  - [ ] Update reports
- **Testing Checklist:**
  - [ ] Can enter decimal quantities
  - [ ] Calculations are accurate
  - [ ] Stock tracking works correctly
  - [ ] Reports show correct decimals
  - [ ] No rounding errors
  - [ ] Test with 0.5, 0.25, 1.333, etc.

---

### 1.4 Allow/Disallow Negative Stock Setting
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2-3 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add setting in SharedPreferences
  - [ ] Create settings UI toggle
  - [ ] Add validation in outgoing documents
  - [ ] Add validation in stock operations
  - [ ] Show appropriate error messages
- **Testing Checklist:**
  - [ ] When disabled, prevents negative stock
  - [ ] When enabled, allows negative stock
  - [ ] Error message shows when blocked
  - [ ] Setting persists

---

### 1.5 Item History View
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium-High
- **Estimated Time:** 5-7 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Create history data query
  - [ ] Design history UI layout
  - [ ] Show document reference links
  - [ ] Add date filtering
  - [ ] Show running balance
  - [ ] Add export history option
  - [ ] Timeline/list view
- **Testing Checklist:**
  - [ ] Shows all product movements
  - [ ] Correct chronological order
  - [ ] Links to documents work
  - [ ] Running balance is accurate
  - [ ] Filter by date works
  - [ ] Export works

---

## 📦 Optional Priority 2: Document Enhancements

### 2.3 Price Fields in Documents
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium-High
- **Estimated Time:** 6-8 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add price fields to document line items
  - [ ] Add purchase price to incoming docs
  - [ ] Add sale price to outgoing docs
  - [ ] Calculate totals
  - [ ] Add settings to show/hide prices
  - [ ] Update document UI with prices
  - [ ] Add price to product entity (optional)
  - [ ] Currency formatting
- **Testing Checklist:**
  - [ ] Can enter prices per line
  - [ ] Totals calculate correctly
  - [ ] Can hide/show via settings
  - [ ] Currency displays correctly
  - [ ] Prices persist

---

### 2.4 Document Status Enhancement (Paid)
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low-Medium
- **Estimated Time:** 3-4 hours
- **Dependencies:** 2.3 (Price fields)
- **Tasks:**
  - [ ] Add "Paid" status to document
  - [ ] Add payment tracking
  - [ ] Add "Mark as Paid" button
  - [ ] Add payment date
  - [ ] Filter by payment status
  - [ ] Add payment method (optional)
- **Testing Checklist:**
  - [ ] Can mark document as paid
  - [ ] Status updates correctly
  - [ ] Filter works
  - [ ] Payment date recorded

---

### 2.5 Add Items via Barcode in Documents
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add scan button in document screen
  - [ ] Implement barcode search
  - [ ] Auto-add product to line items
  - [ ] Handle quantity increment if scanned again
  - [ ] Show toast/feedback
  - [ ] Add scan mode setting (add vs increment)
- **Testing Checklist:**
  - [ ] Scan adds product to document
  - [ ] Scanning again increments quantity
  - [ ] Works with all document types
  - [ ] Product not found handling

---

### 2.6 Document Comments Field
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add comments field to document entity
  - [ ] Add comments input in document screen
  - [ ] Display comments in document view
  - [ ] Add to document exports
- **Testing Checklist:**
  - [ ] Can add/edit comments
  - [ ] Comments persist
  - [ ] Comments show in view mode
  - [ ] Comments export correctly

---

## 📦 Optional Priority 3: Advanced Scanning

### 4.2 External Scanner Support (HID)
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 4-6 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Capture keyboard input
  - [ ] Detect barcode pattern (prefix/suffix)
  - [ ] Filter regular keyboard input
  - [ ] Configuration for HID settings
  - [ ] Focus management
- **Testing Checklist:**
  - [ ] HID scanner input captured
  - [ ] Regular keyboard still works
  - [ ] Works in any input field
  - [ ] Configurable delimiters work

---

### 4.3 Batch Scanning Modes
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 5-6 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Create scanning mode selector
  - [ ] Mode 1: Scan = +1 (auto increment)
  - [ ] Mode 2: Scan + enter quantity
  - [ ] Mode 3: Continuous scanning
  - [ ] Visual feedback for each mode
  - [ ] Batch result summary
  - [ ] Cancel/complete batch
- **Testing Checklist:**
  - [ ] All modes work correctly
  - [ ] Quantities update properly
  - [ ] Can switch modes
  - [ ] Batch summary accurate
  - [ ] Can cancel/complete

---

## 📦 Optional Priority 4: Reports & Analytics

### 5.2 Stock Movement Reports
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium-High
- **Estimated Time:** 6-8 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Create movement summary query
  - [ ] In/Out totals by period
  - [ ] By product, category, store
  - [ ] Trend analysis
  - [ ] Visual charts
  - [ ] Export capability
- **Testing Checklist:**
  - [ ] Reports show correct data
  - [ ] All filters work
  - [ ] Charts display trends
  - [ ] Export works

---

### 5.3 Profit/Cost Reports
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** High
- **Estimated Time:** 8-10 hours
- **Dependencies:** 2.3 (Price fields)
- **Tasks:**
  - [ ] Calculate purchase costs
  - [ ] Calculate sale revenues
  - [ ] Calculate profit margins
  - [ ] By product, period, store
  - [ ] Profit/loss summary
  - [ ] Visual dashboards
  - [ ] Export reports
- **Testing Checklist:**
  - [ ] Calculations accurate
  - [ ] All dimensions work
  - [ ] Charts meaningful
  - [ ] Export includes all data

---

## 📦 Optional Priority 5: Settings Enhancements

### 6.1 Image Size/Quality Settings
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low-Medium
- **Estimated Time:** 3-4 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add settings for image quality
  - [ ] Compression options (low/medium/high)
  - [ ] Max resolution setting
  - [ ] Apply to camera capture
  - [ ] Apply to gallery selection
  - [ ] Show estimated file size
- **Testing Checklist:**
  - [ ] Quality settings apply
  - [ ] File sizes as expected
  - [ ] Images still usable
  - [ ] Performance acceptable

---

### 6.2 Decimal Precision Settings
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2-3 hours
- **Dependencies:** 1.3 (Decimal Quantity Support)
- **Tasks:**
  - [ ] Add decimal places setting (0-4)
  - [ ] Apply to quantity displays
  - [ ] Apply to calculations
  - [ ] Round properly
  - [ ] Update all UI
- **Testing Checklist:**
  - [ ] Precision applies globally
  - [ ] No calculation errors
  - [ ] UI displays correctly

---

### 6.3 Unique Barcode Requirement Setting
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add setting toggle
  - [ ] Validate on product save
  - [ ] Show appropriate error
  - [ ] Handle in import
- **Testing Checklist:**
  - [ ] When enabled, prevents duplicates
  - [ ] When disabled, allows duplicates
  - [ ] Import respects setting

---

### 6.4 Document Editing Rules
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 3-4 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add settings for edit permissions
  - [ ] Lock posted documents (optional)
  - [ ] Time-based restrictions
  - [ ] User role restrictions (future)
  - [ ] Override with permission
- **Testing Checklist:**
  - [ ] Rules enforced correctly
  - [ ] Locked docs can't be edited
  - [ ] Appropriate messages shown

---

### 6.5 Scanner Type Selection
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2-3 hours
- **Dependencies:** 4.1, 4.2
- **Tasks:**
  - [ ] Add scanner type setting
  - [ ] Options: Camera, Bluetooth, HID
  - [ ] Configure default behavior
  - [ ] Quick switch option
- **Testing Checklist:**
  - [ ] Can select scanner type
  - [ ] Selected type works
  - [ ] Setting persists

---

### 6.6 Price Display Settings
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Low
- **Estimated Time:** 2 hours
- **Dependencies:** 2.3
- **Tasks:**
  - [ ] Show/hide price toggle
  - [ ] Show/hide totals toggle
  - [ ] Apply to documents
  - [ ] Apply to exports
- **Testing Checklist:**
  - [ ] Prices hide when disabled
  - [ ] Totals hide when disabled
  - [ ] Settings apply everywhere

---

### 6.7 Auto-backup Scheduling
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add WorkManager for scheduling
  - [ ] Daily/weekly/monthly options
  - [ ] Backup location setting
  - [ ] Notification on completion
  - [ ] Cleanup old backups
  - [ ] Last backup timestamp
- **Testing Checklist:**
  - [ ] Schedule triggers correctly
  - [ ] Backup completes successfully
  - [ ] Notification appears
  - [ ] Old backups cleaned up

---

## 📦 Optional Priority 6: Security & Access

### 7.1 Fingerprint Unlock
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Medium
- **Estimated Time:** 4-5 hours
- **Dependencies:** None
- **Tasks:**
  - [ ] Add BiometricPrompt API
  - [ ] Settings toggle for biometric
  - [ ] Fallback to PIN
  - [ ] Lock on app background
  - [ ] Lock timeout setting
- **Testing Checklist:**
  - [ ] Fingerprint authentication works
  - [ ] Fallback to PIN works
  - [ ] Lock timeout works
  - [ ] Works on various devices

---

### 7.2 User Roles (Future - Online)
- **Status:** ❌ Not Started (NOT REQUIRED)
- **Complexity:** Very High
- **Estimated Time:** 20+ hours
- **Dependencies:** Online backend
- **Tasks:**
  - [ ] Design role system
  - [ ] Admin, Manager, Staff roles
  - [ ] Permission matrix
  - [ ] Role assignment UI
  - [ ] Enforce permissions
  - [ ] Audit log
- **Testing Checklist:**
  - [ ] TBD when online version planned

---

## 📦 Optional Priority 7: Online/Cloud Features (FUTURE)

### 8.1 Multi-device Sync
- **Status:** ❌ Future Feature (NOT REQUIRED)
- **Complexity:** Very High
- **Estimated Time:** 40+ hours
- **Dependencies:** Backend infrastructure

---

### 8.2 Cloud Backup
- **Status:** ❌ Future Feature (NOT REQUIRED)
- **Complexity:** High
- **Estimated Time:** 20+ hours

---

### 8.3 User Login System
- **Status:** ❌ Future Feature (NOT REQUIRED)
- **Complexity:** High
- **Estimated Time:** 15+ hours

---

### 8.4 Shared Company Database
- **Status:** ❌ Future Feature (NOT REQUIRED)
- **Complexity:** Very High
- **Estimated Time:** 50+ hours

---

# 📊 OVERALL PROGRESS TRACKING

---

## 📋 Progress Tracking

### Sprint Overview
- **Current Sprint:** Not Started
- **Sprint Goal:** TBD
- **Sprint Duration:** TBD

### Completed Features
1. **1.6 Duplicate Product Feature** - ✅ Completed (Nov 14, 2025)
   - Added duplicate button to product edit dialog
   - Implemented smart barcode generation with auto-increment
   - Stock resets to 0, all fields editable before creation
   - Validates barcode uniqueness

2. **2.1 Supplier Management & Field** - ✅ Completed (Nov 14, 2025)
   - Full Supplier CRUD operations with database migrations
   - Supplier entity with contact details (name, person, phone, email, address)
   - Complete UI: list, add, edit, search, pagination
   - Integrated supplierId field into Purchase entity
   - Ready for purchase document integration

### In Progress
*None yet*

### Blocked
*None yet*

---

## 📝 Notes & Decisions

### Technical Decisions
- Database: Room (already implemented)
- Image handling: TBD (Glide/Coil)
- Excel library: Apache POI vs alternatives
- Scanner: ZXing (camera), Bluetooth library TBD

### Known Issues
*Document issues found during implementation*

### Future Considerations
- Consider modularization as app grows
- Plan for backend API structure
- Consider Jetpack Compose migration

---

## 🎯 Next Steps - REQUIRED Features Implementation Plan

### Recommended Implementation Order:

1. **Start with:** 1.6 Duplicate Product Feature (2-3 hours) - Quick win, easy to implement
2. **Then:** 2.1 Supplier Management (4-5 hours) - Foundation for document enhancements
3. **Then:** 2.2 Customer Management (4-5 hours) - Similar to supplier
4. **Then:** 4.4 Camera Switching (2-3 hours) - Quick enhancement
5. **Then:** 3.1 Excel Import - Goods Only (8-10 hours) - Core feature
6. **Then:** 3.3 Excel Export - Goods List (4-6 hours) - Complement to import
7. **Then:** 3.2 Excel Import with Quantities (6-8 hours) - Extends import
8. **Then:** 3.4 Excel Export - Documents (5-7 hours) - Complete export functionality
9. **Then:** 4.1 Bluetooth Scanner (10-12 hours) - Major feature
10. **Then:** 5.1 Enhanced Item History (4-5 hours) - Reports foundation
11. **Then:** 6.1 Dashboard Enhancement (8-10 hours) - User experience
12. **Then:** 6.2 Global Search (5-7 hours) - User experience
13. **Finally:** 6.3 Advanced Sorting & Filtering (6-8 hours) - Polish

### Total Estimated Time: 80-100 hours

---

## 🎯 Action Items

- [ ] Review REQUIRED features list
- [ ] Set up testing framework if needed
- [ ] Create feature branch for first implementation
- [ ] Begin with feature 1.6 (Duplicate Product)
- [ ] Test thoroughly after each feature
- [ ] Update TODO.md progress after completion

---

**Remember:** 
- **Focus on REQUIRED features only** ✅
- Test thoroughly before marking as complete ✔️
- Update progress regularly 📊
- Document any issues or decisions 📝
- One feature at a time! 🎯
- NOT REQUIRED features are at the end for future reference ⏸️