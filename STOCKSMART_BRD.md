# StockSmart - Business Requirements Document (BRD)

**Document Version:** 1.0  
**Date:** November 21, 2025  
**Application Name:** StockSmart  
**Platform:** Android Mobile Application  
**Package:** com.example.inv_5  

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Business Objectives](#business-objectives)
3. [Stakeholders](#stakeholders)
4. [Scope](#scope)
5. [Functional Requirements](#functional-requirements)
6. [Non-Functional Requirements](#non-functional-requirements)
7. [Technical Stack](#technical-stack)
8. [Success Criteria](#success-criteria)
9. [Assumptions and Constraints](#assumptions-and-constraints)

---

## Executive Summary

**StockSmart** is a comprehensive Android-based inventory management application designed for small to medium-sized retail businesses. The application provides local, offline-first data storage with complete control over business operations including product management, purchases, sales, stock tracking, supplier/customer management, and advanced reporting capabilities.

### Tagline
**"Secure. Local. Smart."**

### Key Value Proposition
- **100% Offline Operation** - No internet dependency, all data stored locally
- **Complete Privacy** - Business data never leaves the device
- **Professional Features** - Enterprise-grade inventory management capabilities
- **Hardware Integration** - Support for Bluetooth barcode scanners and camera scanning
- **Advanced Reporting** - Excel export with multiple valuation methods (FIFO, LIFO, Weighted Average)
- **Activity Tracking** - Complete audit trail of all business operations

---

## Business Objectives

### Primary Objectives

1. **Streamline Inventory Operations**
   - Reduce manual data entry errors
   - Speed up purchase and sale transaction processing
   - Maintain accurate real-time stock levels

2. **Improve Business Intelligence**
   - Provide actionable insights through dashboard analytics
   - Enable data-driven decision making with comprehensive reports
   - Track business performance metrics (revenue, profit margins, stock turnover)

3. **Enhance Operational Efficiency**
   - Integrate barcode scanning for faster product lookup
   - Automate stock updates during transactions
   - Simplify supplier and customer relationship management

4. **Ensure Data Security and Privacy**
   - Store all data locally on device
   - Provide backup and restore capabilities
   - Maintain complete data ownership

### Secondary Objectives

1. Enable multi-device data portability through Excel import/export
2. Provide comprehensive audit trails for compliance
3. Support multiple inventory valuation methods for financial reporting
4. Minimize training requirements with intuitive UI/UX

---

## Stakeholders

### Primary Stakeholders

1. **Business Owners**
   - Small retail shop owners
   - Inventory managers
   - Store managers

2. **Store Operators**
   - Sales staff
   - Purchase managers
   - Inventory clerks

3. **Accountants/Bookkeepers**
   - Financial reporting users
   - Tax compliance personnel

### Secondary Stakeholders

1. **IT Support Personnel** (for backup/restore operations)
2. **Auditors** (for activity log review)
3. **Suppliers and Customers** (indirect - data management)

---

## Scope

### In Scope

#### Core Features (Must Have)

1. **Product Management**
   - Add, edit, delete, duplicate products
   - Barcode management
   - Category organization
   - Stock level tracking (quantity on hand, reorder point, maximum stock level)
   - Product history view

2. **Purchase Management**
   - Create and edit purchase orders
   - Multi-item purchase documents
   - Supplier association
   - Invoice tracking
   - Automatic stock updates

3. **Sales Management**
   - Create and edit sales invoices
   - Multi-item sales documents
   - Customer association
   - GST/Tax calculations
   - Automatic stock deductions

4. **Stock Management**
   - Real-time stock levels
   - Pagination for large datasets
   - Search and filter capabilities
   - Stock alerts (out-of-stock indicators)

5. **Supplier Management**
   - Add, edit suppliers
   - Contact information tracking
   - Purchase association

6. **Customer Management**
   - Add, edit customers
   - Contact information tracking
   - Sales association

7. **Reports & Export**
   - Stock summary reports (with date range filtering)
   - Out-of-stock reports
   - Inventory valuation reports (FIFO, LIFO, Weighted Average)
   - Cost of Goods Sold (COGS) reports
   - Inventory aging reports
   - Purchase/Sale document exports
   - Excel export functionality

8. **Expense Management**
   - Record business expenses
   - Categorize expenses (CAPEX, OPEX, MIXED)
   - Payment tracking
   - GST management

9. **Activity Log**
   - Complete transaction audit trail
   - Search and filter capabilities
   - Entity-based tracking (Products, Purchases, Sales, Suppliers, Customers, Expenses)

10. **Settings & Configuration**
    - Store details management
    - Database backup and restore
    - Bluetooth scanner configuration

#### Advanced Features (Nice to Have - Implemented)

1. **Barcode Scanning**
   - Inline camera barcode scanning
   - Front/back camera switching
   - Auto-product lookup

2. **Bluetooth Scanner Support**
   - External Bluetooth scanner pairing
   - Auto-connect functionality
   - Real-time barcode input

3. **Dashboard Analytics**
   - Total purchases/sales metrics
   - Out-of-stock counts
   - Recent activity feed
   - Quick action shortcuts

4. **Item History**
   - Complete transaction timeline
   - Date and type filters
   - Running balance calculations
   - Excel export

### Out of Scope

1. Multi-user/multi-device synchronization
2. Cloud storage or backup
3. Online ordering/e-commerce integration
4. Email/SMS notifications
5. Payment gateway integration
6. Barcode label printing
7. Multi-currency support
8. Multi-language localization

---

## Functional Requirements

### FR-1: Store Details Management
- **Priority:** High
- **Description:** Configure business information displayed on reports
- **Acceptance Criteria:**
  - Store name, caption, address, phone, owner name fields
  - One-time configuration (edit allowed)
  - Data persists across app sessions
  - Displayed on navigation drawer header

### FR-2: Product Management
- **Priority:** Critical
- **Description:** Manage product catalog
- **Acceptance Criteria:**
  - Create products with: ID, Name, MRP, Sale Price, Barcode, Category
  - Edit product details
  - Duplicate products with unique barcode generation
  - View product history (purchases, sales, balance)
  - Pagination (20 items per page)
  - Search by name or barcode
  - Active/inactive status management

### FR-3: Purchase Management
- **Priority:** Critical
- **Description:** Record inventory purchases
- **Acceptance Criteria:**
  - Create multi-item purchase documents
  - Associate with suppliers
  - Invoice number and date tracking
  - GST/Tax calculations (CGST, SGST, IGST)
  - Automatic product stock increment
  - Edit/delete purchases (with stock rollback)
  - View purchase list with pagination and search

### FR-4: Sales Management
- **Priority:** Critical
- **Description:** Record inventory sales
- **Acceptance Criteria:**
  - Create multi-item sales documents
  - Associate with customers
  - Customer details (name, address, phone)
  - GST/Tax calculations
  - Automatic product stock decrement
  - Edit/delete sales (with stock rollback)
  - View sales list with pagination and search

### FR-5: Stock Management
- **Priority:** Critical
- **Description:** Monitor inventory levels
- **Acceptance Criteria:**
  - Display all products with current stock
  - Pagination for performance
  - Search functionality
  - Out-of-stock highlighting
  - Reorder point alerts (future enhancement)

### FR-6: Supplier Management
- **Priority:** High
- **Description:** Manage supplier information
- **Acceptance Criteria:**
  - Create suppliers with contact details
  - Edit supplier information
  - Active/inactive status
  - Associate with purchases
  - Search functionality

### FR-7: Customer Management
- **Priority:** High
- **Description:** Manage customer information
- **Acceptance Criteria:**
  - Create customers with contact details
  - Edit customer information
  - Active/inactive status
  - Associate with sales
  - Quick customer selection in sales

### FR-8: Reporting & Export
- **Priority:** High
- **Description:** Generate business reports
- **Acceptance Criteria:**
  - Export to Excel format (.xlsx)
  - Date range filtering where applicable
  - Professional formatting (headers, totals, styling)
  - Multiple report types:
    1. Stock Summary
    2. Out-of-Stock
    3. Inventory Valuation (3 methods)
    4. COGS (Cost of Goods Sold)
    5. Inventory Aging
    6. Purchase/Sale Documents
  - Files saved to Downloads folder
  - Success/failure notifications

### FR-9: Expense Management
- **Priority:** Medium
- **Description:** Track business expenses
- **Acceptance Criteria:**
  - Record expenses with date, category, amount
  - Expense type classification (CAPEX/OPEX/MIXED)
  - GST/Tax tracking
  - Payment status tracking
  - Edit/delete expenses
  - Search functionality

### FR-10: Activity Log
- **Priority:** Medium
- **Description:** Audit trail of all operations
- **Acceptance Criteria:**
  - Log all create/update/delete operations
  - Entity type identification
  - Timestamp tracking
  - Search and filter
  - View transaction details

### FR-11: Barcode Scanning
- **Priority:** High
- **Description:** Hardware barcode scanning support
- **Acceptance Criteria:**
  - **Camera Scanner:**
    - Inline barcode scanning in purchase/sale dialogs
    - ML Kit barcode detection
    - Front/back camera switching
    - Preference persistence
  - **Bluetooth Scanner:**
    - Device pairing and connection
    - Auto-connect on app start
    - Real-time barcode input
    - Auto-product lookup
    - Settings configuration page

### FR-12: Database Management
- **Priority:** High
- **Description:** Backup and restore capabilities
- **Acceptance Criteria:**
  - Backup database to Downloads folder
  - Timestamped backup files
  - Restore from selected backup file
  - App restart after restore
  - Permission handling (Android 11+)

### FR-13: Dashboard
- **Priority:** Medium
- **Description:** Business metrics overview
- **Acceptance Criteria:**
  - Summary cards (Total Purchases, Sales, Out-of-Stock, Week Purchases)
  - Recent activity feed
  - Quick action cards (navigation shortcuts)
  - Pull-to-refresh functionality

---

## Non-Functional Requirements

### NFR-1: Performance
- App launch time < 3 seconds
- Transaction save time < 1 second
- List pagination (20 items) load time < 500ms
- Barcode scan detection time < 2 seconds
- Excel export (1000 records) < 10 seconds

### NFR-2: Reliability
- 99.9% uptime (excluding device issues)
- Zero data loss on app crash
- Successful database backup/restore 100%
- Transaction integrity maintained

### NFR-3: Usability
- Intuitive UI following Material Design 3 guidelines
- Minimal training required (< 30 minutes for basic operations)
- Clear error messages
- Confirmation dialogs for destructive actions
- Toast notifications for all operations

### NFR-4: Security
- Local SQLite database encryption (future enhancement)
- No cloud data transmission
- Permission-based access to camera, Bluetooth, storage
- Backup files user-controlled

### NFR-5: Compatibility
- Minimum Android API 26 (Android 8.0 Oreo)
- Target Android API 36
- Support for Android 8.0 to Android 15+
- Screen sizes: 5" to 10" (phones and small tablets)

### NFR-6: Maintainability
- Kotlin codebase with MVVM architecture
- Room Database with migrations
- Modular code structure
- Comprehensive error logging

### NFR-7: Scalability
- Support up to 10,000 products
- Support up to 50,000 transactions (purchases + sales)
- Pagination prevents memory issues
- Database size limit: 500MB

---

## Technical Stack

### Development Environment
- **Language:** Kotlin 2.0.21
- **IDE:** Android Studio
- **Build System:** Gradle 8.9
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36
- **Compile SDK:** 36

### Core Libraries
- **AndroidX Core KTX:** 1.15.0
- **AppCompat:** 1.7.0
- **Material Design:** 1.12.0
- **Navigation Components:** 2.8.4

### Database
- **Room Database:** 2.6.1
  - Room Runtime
  - Room KTX
  - Room Compiler (KAPT)

### Camera & ML
- **CameraX:** 1.2.3
  - Camera Core
  - Camera2
  - Camera Lifecycle
  - Camera View
- **ML Kit Barcode Scanning:** 17.0.2

### Excel Support
- **Apache POI:** 5.2.3
- **Apache POI OOXML:** 5.2.3

### Charts (Future)
- **MPAndroidChart:** v3.1.0 (library added, implementation pending)

### Other
- **SwipeRefreshLayout:** 1.1.0
- **GridLayout:** 1.0.0
- **MultiDex:** 2.0.1

### Permissions Required
- `CAMERA` - Barcode scanning
- `BLUETOOTH` / `BLUETOOTH_ADMIN` (API ≤30)
- `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` (API 31+)
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (API ≤32)
- `MANAGE_EXTERNAL_STORAGE` (Android 11+)

---

## Success Criteria

### Business Success Metrics

1. **Adoption:**
   - 100 active users within 3 months
   - 80% user retention rate
   - Average session duration > 10 minutes

2. **Operational Efficiency:**
   - 50% reduction in transaction recording time vs manual methods
   - 90% reduction in stock tracking errors
   - 100% accurate inventory counts

3. **User Satisfaction:**
   - 4.5+ star rating on Play Store
   - < 5% crash rate
   - < 10% uninstall rate

### Technical Success Metrics

1. **Performance:**
   - App size < 50MB
   - Memory usage < 200MB during operation
   - Battery consumption < 5% per hour of active use

2. **Quality:**
   - Zero critical bugs in production
   - < 5 minor bugs per release
   - 100% test coverage for critical paths

3. **Reliability:**
   - 99.9% successful database operations
   - Zero data corruption incidents
   - 100% backup/restore success rate

---

## Assumptions and Constraints

### Assumptions

1. Users have basic Android device literacy
2. Devices have sufficient storage (minimum 500MB free)
3. Camera available for barcode scanning (for devices without Bluetooth scanner)
4. Users have access to file management apps for backup files
5. Excel-compatible software available for viewing reports (Microsoft Excel, Google Sheets, LibreOffice)

### Constraints

1. **Technical Constraints:**
   - Single device, single user only
   - No cloud synchronization
   - Limited to Android platform
   - Requires Android 8.0 or higher

2. **Business Constraints:**
   - No ongoing server/hosting costs
   - One-time installation only
   - User responsible for data backups
   - No technical support infrastructure

3. **Resource Constraints:**
   - Solo developer project
   - Limited testing devices
   - No dedicated QA team

4. **Regulatory Constraints:**
   - GST calculations based on Indian tax structure
   - Date formats follow Indian standards (dd/MM/yyyy)
   - Currency display in INR (₹)

---

## Glossary

| Term | Definition |
|------|------------|
| **CAPEX** | Capital Expenditure - Long-term investments |
| **OPEX** | Operating Expenditure - Day-to-day operational costs |
| **COGS** | Cost of Goods Sold - Direct costs of producing goods sold |
| **FIFO** | First In First Out - Inventory valuation method |
| **LIFO** | Last In First Out - Inventory valuation method |
| **SKU** | Stock Keeping Unit - Unique product identifier |
| **MRP** | Maximum Retail Price |
| **GST** | Goods and Services Tax |
| **CGST** | Central Goods and Services Tax |
| **SGST** | State Goods and Services Tax |
| **IGST** | Integrated Goods and Services Tax |
| **SPP** | Serial Port Profile (Bluetooth) |
| **HSN** | Harmonized System of Nomenclature (product code) |

---

## Appendices

### Appendix A: Entity Relationship Overview

**Core Entities:**
1. **Product** - Product catalog
2. **Purchase** - Purchase documents
3. **PurchaseItem** - Line items in purchases
4. **Sale** - Sales documents
5. **SaleItem** - Line items in sales
6. **Supplier** - Vendor information
7. **Customer** - Customer information
8. **Expense** - Business expenses
9. **ActivityLog** - Audit trail
10. **StoreDetails** - Business configuration

**Relationships:**
- Purchase → Supplier (many-to-one)
- PurchaseItem → Purchase (many-to-one)
- PurchaseItem → Product (many-to-one)
- Sale → Customer (many-to-one)
- SaleItem → Sale (many-to-one)
- SaleItem → Product (many-to-one)

### Appendix B: Future Enhancements (Out of Scope for v1.0)

1. **Multi-user support** with role-based access
2. **Cloud backup** integration (Google Drive, Dropbox)
3. **Barcode label printing** via Bluetooth printers
4. **Offline-online sync** for multi-device scenarios
5. **Advanced analytics** with charts and graphs
6. **Low stock alerts** with push notifications
7. **Batch/lot tracking** for expiry management
8. **Serial number tracking** for high-value items
9. **Discount management** system
10. **Payment tracking** and reconciliation

---

**End of Business Requirements Document**

---

**Document Approval:**

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Business Analyst | ___________ | ___________ | ___________ |
| Product Owner | ___________ | ___________ | ___________ |
| Technical Lead | ___________ | ___________ | ___________ |
| QA Lead | ___________ | ___________ | ___________ |

---

**Revision History:**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Nov 21, 2025 | System Analyst | Initial document creation |

---
