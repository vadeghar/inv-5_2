# StockSmart - Test Cases Document

**Application:** StockSmart Inventory Management  
**Version:** 1.0  
**Date:** November 21, 2025  
**Platform:** Android  

---

## Table of Contents

1. [Store Details Management](#1-store-details-management)
2. [Product Management](#2-product-management)
3. [Purchase Management](#3-purchase-management)
4. [Sales Management](#4-sales-management)
5. [Stock Management](#5-stock-management)
6. [Supplier Management](#6-supplier-management)
7. [Customer Management](#7-customer-management)
8. [Expense Management](#8-expense-management)
9. [Activity Log](#9-activity-log)
10. [Reports & Export](#10-reports--export)
11. [Barcode Scanning](#11-barcode-scanning)
12. [Settings & Backup](#12-settings--backup)
13. [Dashboard & Home](#13-dashboard--home)
14. [Navigation & UI](#14-navigation--ui)

---

## 1. Store Details Management

### TC-SD-001: View Store Details
**Priority:** High  
**Precondition:** App installed and launched for first time  
**Steps:**
1. Open navigation drawer
2. Navigate to "Store Details" screen
**Expected Result:**
- Store details form displayed
- Fields: Store Name, Caption, Address, Phone, Owner Name
- Save button enabled

### TC-SD-002: Save Store Details - Valid Data
**Priority:** High  
**Steps:**
1. Navigate to Store Details screen
2. Enter valid data in all fields:
   - Store Name: "Test Store"
   - Caption: "Quality Products"
   - Address: "123 Main Street"
   - Phone: "9876543210"
   - Owner Name: "John Doe"
3. Click Save button
**Expected Result:**
- Success toast displayed: "Store details saved successfully"
- Data persists in database
- Details visible in navigation drawer header

### TC-SD-003: Update Existing Store Details
**Priority:** Medium  
**Precondition:** Store details already saved  
**Steps:**
1. Navigate to Store Details screen
2. Modify existing values
3. Click Save button
**Expected Result:**
- Updated values saved successfully
- Navigation drawer header reflects changes

### TC-SD-004: Save Store Details - Empty Fields
**Priority:** Medium  
**Steps:**
1. Navigate to Store Details screen
2. Leave fields empty
3. Click Save button
**Expected Result:**
- Validation errors displayed for required fields
- Data not saved until all required fields filled

---

## 2. Product Management

### TC-PM-001: Add New Product - Valid Data
**Priority:** Critical  
**Steps:**
1. Navigate to Products screen
2. Click "Add Product" FAB
3. Enter product details:
   - Name: "Test Product"
   - MRP: "100.00"
   - Sale Price: "90.00"
   - Barcode: "1234567890"
   - Category: "Electronics"
4. Click Save
**Expected Result:**
- Product created successfully
- Toast: "Product added successfully"
- Product visible in products list
- Activity log entry created
- Stock quantity initialized to 0

### TC-PM-002: Add Product - Duplicate Barcode
**Priority:** High  
**Precondition:** Product with barcode "1234567890" exists  
**Steps:**
1. Click Add Product
2. Enter same barcode "1234567890"
3. Fill other fields
4. Click Save
**Expected Result:**
- Error message displayed
- Product not saved
- User prompted to enter unique barcode

### TC-PM-003: Edit Existing Product
**Priority:** High  
**Precondition:** At least one product exists  
**Steps:**
1. Navigate to Products screen
2. Long-press on a product
3. Select "Edit" from dialog
4. Modify product name
5. Click Save
**Expected Result:**
- Product updated successfully
- Activity log entry created: "Product updated"
- Changes reflected in products list

### TC-PM-004: Duplicate Product
**Priority:** Medium  
**Precondition:** Product exists  
**Steps:**
1. Long-press on a product
2. Select "Duplicate"
3. System auto-generates new barcode
4. Modify name if needed
5. Click Save
**Expected Result:**
- New product created with unique ID and barcode
- Original product unchanged
- Both products visible in list

### TC-PM-005: Delete Product - No Transactions
**Priority:** High  
**Precondition:** Product has no purchase/sale history  
**Steps:**
1. Long-press on product
2. Select "Delete"
3. Confirm deletion
**Expected Result:**
- Product deleted from database
- Activity log entry created
- Product removed from list

### TC-PM-006: Delete Product - With Transactions
**Priority:** High  
**Precondition:** Product has purchase/sale history  
**Steps:**
1. Attempt to delete product
2. System checks for transactions
**Expected Result:**
- Warning dialog displayed
- User informed about transaction history
- Deletion prevented OR soft delete (mark inactive)

### TC-PM-007: View Product History
**Priority:** Medium  
**Precondition:** Product has transactions  
**Steps:**
1. Long-press on product
2. Select "History"
**Expected Result:**
- Item History screen opened
- All purchases and sales listed chronologically
- Running balance displayed
- Date filters functional

### TC-PM-008: Search Products by Name
**Priority:** Medium  
**Precondition:** Multiple products exist  
**Steps:**
1. Navigate to Products screen
2. Enter product name in search box
**Expected Result:**
- Filtered list displayed
- Only matching products shown
- Search case-insensitive

### TC-PM-009: Search Products by Barcode
**Priority:** Medium  
**Steps:**
1. Enter barcode in search box
**Expected Result:**
- Product with matching barcode displayed
- Exact match prioritized

### TC-PM-010: Product Pagination
**Priority:** Medium  
**Precondition:** More than 20 products exist  
**Steps:**
1. Scroll to bottom of products list
2. Click "Next" pagination button
**Expected Result:**
- Next 20 products loaded
- Page number updated
- Previous/Next buttons enabled/disabled appropriately

### TC-PM-011: Add Product - Invalid MRP (Negative)
**Priority:** Medium  
**Steps:**
1. Enter negative value for MRP: "-50"
2. Attempt to save
**Expected Result:**
- Validation error displayed
- Product not saved

### TC-PM-012: Add Product - Sale Price > MRP
**Priority:** Low  
**Steps:**
1. Enter MRP: "100"
2. Enter Sale Price: "150"
3. Save product
**Expected Result:**
- Warning displayed (optional)
- Product saved (business rule may allow this)

---

## 3. Purchase Management

### TC-PU-001: Create Purchase - Single Item
**Priority:** Critical  
**Steps:**
1. Click "Add Purchase" FAB
2. Select supplier
3. Enter invoice number and date
4. Scan/enter product barcode
5. Select product from matched list
6. Enter quantity: 10, rate: 50
7. Apply tax if needed
8. Click Save
**Expected Result:**
- Purchase created successfully
- Product stock incremented by 10
- Purchase visible in purchases list
- Activity log entry created
- Supplier associated

### TC-PU-002: Create Purchase - Multiple Items
**Priority:** Critical  
**Steps:**
1. Add Purchase
2. Add 3 different products with quantities
3. Enter tax details
4. Save purchase
**Expected Result:**
- All 3 products added to purchase
- Total calculated correctly
- Stock updated for all products
- Activity log shows total amount

### TC-PU-003: Create Purchase - No Supplier
**Priority:** Medium  
**Steps:**
1. Create purchase without selecting supplier
2. Add items and save
**Expected Result:**
- Purchase saved successfully
- Supplier field optional or shows "Walk-in"

### TC-PU-004: Edit Purchase - Modify Quantity
**Priority:** High  
**Precondition:** Purchase exists with Qty: 10  
**Steps:**
1. Open purchase for editing
2. Change quantity from 10 to 15
3. Save changes
**Expected Result:**
- Stock increased by additional 5 units
- Purchase updated in database
- Activity log entry created
- Total recalculated

### TC-PU-005: Delete Purchase
**Priority:** High  
**Precondition:** Purchase exists  
**Steps:**
1. Long-press purchase
2. Select Delete
3. Confirm deletion
**Expected Result:**
- Purchase removed from database
- Stock rolled back (decremented)
- Activity log entry created
- Warning if deleting old transaction

### TC-PU-006: Purchase with GST Calculation
**Priority:** High  
**Steps:**
1. Create purchase
2. Add item with rate: 1000
3. Apply CGST: 9%, SGST: 9%
4. Save
**Expected Result:**
- CGST: ₹90
- SGST: ₹90
- Total: ₹1180
- Tax breakdown visible

### TC-PU-007: Purchase with IGST
**Priority:** Medium  
**Steps:**
1. Create purchase
2. Apply IGST: 18% instead of CGST/SGST
**Expected Result:**
- IGST calculated correctly
- CGST/SGST disabled when IGST applied

### TC-PU-008: Barcode Scan During Purchase
**Priority:** High  
**Steps:**
1. Create purchase
2. Click barcode icon
3. Scan product barcode
**Expected Result:**
- Product auto-populated
- If multiple MRPs, selection dialog shown
- Quantity field focused for entry

### TC-PU-009: Purchase List Pagination
**Priority:** Medium  
**Precondition:** More than 20 purchases  
**Steps:**
1. Navigate to Purchases screen
2. Scroll to bottom
3. Load next page
**Expected Result:**
- 20 purchases loaded per page
- Pagination controls functional

### TC-PU-010: Search Purchases by Invoice Number
**Priority:** Medium  
**Steps:**
1. Enter invoice number in search
**Expected Result:**
- Matching purchases displayed
- Search filters purchases list

---

## 4. Sales Management

### TC-SA-001: Create Sale - Single Item
**Priority:** Critical  
**Steps:**
1. Click "Add Sale" button
2. Select customer
3. Enter customer details
4. Scan/enter product barcode
5. Select product
6. Enter quantity: 5
7. Rate auto-populated from sale price
8. Apply tax
9. Save sale
**Expected Result:**
- Sale created successfully
- Stock decremented by 5
- Activity log entry created
- Customer associated

### TC-SA-002: Create Sale - Insufficient Stock
**Priority:** Critical  
**Precondition:** Product stock = 3  
**Steps:**
1. Attempt to sell quantity: 5
**Expected Result:**
- Error message: "Insufficient stock"
- Sale not allowed
- Stock unchanged

### TC-SA-003: Create Sale - Walk-in Customer
**Priority:** High  
**Steps:**
1. Create sale without selecting customer
2. Enter customer name directly
3. Add items and save
**Expected Result:**
- Sale saved with customer name
- Customer not linked to database customer

### TC-SA-004: Edit Sale - Change Quantity
**Priority:** High  
**Precondition:** Sale exists with Qty: 5  
**Steps:**
1. Edit sale
2. Change quantity to 3
3. Save
**Expected Result:**
- Stock increased by 2 (rollback difference)
- Sale updated
- Activity log entry

### TC-SA-005: Delete Sale
**Priority:** High  
**Steps:**
1. Delete existing sale
2. Confirm deletion
**Expected Result:**
- Sale removed
- Stock restored (incremented)
- Activity log entry

### TC-SA-006: Sale with Discount
**Priority:** Medium  
**Steps:**
1. Create sale
2. Apply discount: 10%
3. Save
**Expected Result:**
- Discount calculated correctly
- Total reduced
- Discount visible on invoice

### TC-SA-007: Sale with Multiple Tax Rates
**Priority:** Medium  
**Steps:**
1. Add items with different tax rates
2. Calculate totals
**Expected Result:**
- Tax calculated per item
- Total tax summed correctly

### TC-SA-008: Generate Sale Invoice
**Priority:** Low  
**Precondition:** Sale exists  
**Steps:**
1. View sale details
2. Click "Generate Invoice"
**Expected Result:**
- Invoice PDF generated
- Store details included
- Customer details shown
- Items, quantities, rates, tax, total displayed

---

## 5. Stock Management

### TC-ST-001: View All Stock Levels
**Priority:** High  
**Steps:**
1. Navigate to Stock Management screen
**Expected Result:**
- All products listed with current stock
- Quantity on Hand displayed
- Pagination functional

### TC-ST-002: Identify Out-of-Stock Items
**Priority:** High  
**Precondition:** Some products have 0 stock  
**Steps:**
1. View stock list
**Expected Result:**
- Out-of-stock items highlighted (red/warning indicator)
- Quantity shows 0
- Clear visual distinction

### TC-ST-003: Search Stock by Product Name
**Priority:** Medium  
**Steps:**
1. Enter product name in search
**Expected Result:**
- Filtered stock list displayed
- Only matching products shown

### TC-ST-004: Stock Level Accuracy After Purchase
**Priority:** Critical  
**Precondition:** Product stock = 10  
**Steps:**
1. Create purchase with quantity: 5
2. View stock
**Expected Result:**
- Stock level = 15
- Real-time update

### TC-ST-005: Stock Level Accuracy After Sale
**Priority:** Critical  
**Precondition:** Product stock = 15  
**Steps:**
1. Create sale with quantity: 3
2. View stock
**Expected Result:**
- Stock level = 12
- Immediate update

### TC-ST-006: Reorder Point Alert
**Priority:** Low  
**Precondition:** Product reorder point = 5, stock = 4  
**Steps:**
1. View stock list
**Expected Result:**
- Product highlighted as below reorder point
- Alert indicator displayed

### TC-ST-007: Stock History Timeline
**Priority:** Medium  
**Steps:**
1. Select product from stock list
2. View history
**Expected Result:**
- All stock movements displayed
- Purchases show positive quantities
- Sales show negative quantities
- Running balance calculated

---

## 6. Supplier Management

### TC-SU-001: Add New Supplier
**Priority:** High  
**Steps:**
1. Navigate to Suppliers screen
2. Click "Add Supplier"
3. Enter details:
   - Name: "ABC Suppliers"
   - Contact Person: "John"
   - Phone: "9876543210"
   - Email: "abc@example.com"
   - Address: "123 Street"
4. Save
**Expected Result:**
- Supplier created
- Activity log entry
- Supplier visible in list

### TC-SU-002: Edit Supplier Details
**Priority:** Medium  
**Steps:**
1. Select supplier
2. Click Edit
3. Modify phone number
4. Save
**Expected Result:**
- Changes saved
- Activity log updated
- Updated info displayed

### TC-SU-003: Delete Supplier - No Purchases
**Priority:** Medium  
**Precondition:** Supplier has no associated purchases  
**Steps:**
1. Delete supplier
2. Confirm
**Expected Result:**
- Supplier deleted
- Activity log entry

### TC-SU-004: Delete Supplier - With Purchases
**Priority:** High  
**Precondition:** Supplier has purchases  
**Steps:**
1. Attempt to delete supplier
**Expected Result:**
- Warning displayed
- Deletion prevented or soft delete

### TC-SU-005: Search Suppliers
**Priority:** Medium  
**Steps:**
1. Enter supplier name in search
**Expected Result:**
- Filtered list displayed

### TC-SU-006: Supplier Pagination
**Priority:** Low  
**Precondition:** More than 20 suppliers  
**Steps:**
1. Navigate through pages
**Expected Result:**
- 20 suppliers per page loaded

---

## 7. Customer Management

### TC-CU-001: Add New Customer
**Priority:** High  
**Steps:**
1. Navigate to Customers screen
2. Click "Add Customer"
3. Enter details:
   - Name: "Jane Doe"
   - Phone: "9998887776"
   - Email: "jane@example.com"
   - Address: "456 Avenue"
4. Save
**Expected Result:**
- Customer created
- Activity log entry
- Customer in list

### TC-CU-002: Edit Customer Details
**Priority:** Medium  
**Steps:**
1. Select customer
2. Edit details
3. Save
**Expected Result:**
- Updates saved
- Activity log entry

### TC-CU-003: Delete Customer - No Sales
**Priority:** Medium  
**Steps:**
1. Delete customer with no sales
**Expected Result:**
- Customer deleted
- Activity log entry

### TC-CU-004: Delete Customer - With Sales
**Priority:** High  
**Precondition:** Customer has sales  
**Steps:**
1. Attempt deletion
**Expected Result:**
- Warning or prevention

### TC-CU-005: Search Customers
**Priority:** Medium  
**Steps:**
1. Search by name or phone
**Expected Result:**
- Filtered results

### TC-CU-006: Quick Customer Selection in Sale
**Priority:** High  
**Steps:**
1. Create new sale
2. Click customer dropdown
**Expected Result:**
- All active customers listed
- Selection updates customer details in sale

---

## 8. Expense Management

### TC-EX-001: Add OPEX Expense
**Priority:** High  
**Steps:**
1. Navigate to Expenses
2. Click "Add Expense"
3. Enter:
   - Expense ID: "EXP001"
   - Date: Today
   - Category: "Rent"
   - Amount: 10000
   - Expense Type: OPEX
   - Payment Method: "Cash"
4. Save
**Expected Result:**
- Expense created
- Activity log entry with amount
- Expense in list

### TC-EX-002: Add CAPEX Expense
**Priority:** High  
**Steps:**
1. Add expense with type: CAPEX
2. Enter higher amount
3. Save
**Expected Result:**
- CAPEX expense saved
- Distinguished from OPEX

### TC-EX-003: Edit Expense
**Priority:** Medium  
**Steps:**
1. Edit existing expense
2. Change amount
3. Save
**Expected Result:**
- Updated in database
- Activity log entry

### TC-EX-004: Delete Expense
**Priority:** Medium  
**Steps:**
1. Delete expense
2. Confirm
**Expected Result:**
- Expense removed
- Activity log entry

### TC-EX-005: Search Expenses by Category
**Priority:** Medium  
**Steps:**
1. Filter expenses by category
**Expected Result:**
- Only matching expenses shown

### TC-EX-006: GST on Expense
**Priority:** Medium  
**Steps:**
1. Add expense with GST applicable
2. Enter GST amount
**Expected Result:**
- GST tracked separately
- Total includes GST

---

## 9. Activity Log

### TC-AL-001: View All Activities
**Priority:** High  
**Steps:**
1. Navigate to Activity Log from sidebar
**Expected Result:**
- All activities listed chronologically (newest first)
- Shows: Description, Document Number, Additional Info, Timestamp, Amount
- Pagination functional

### TC-AL-002: Activity Logged - Product Added
**Priority:** High  
**Precondition:** Add new product  
**Steps:**
1. Create product "Test Product"
2. Navigate to Activity Log
**Expected Result:**
- Entry: "Product 'Test Product' added"
- Entity Type: PRODUCT
- Activity Type: ADD
- Document Number: Barcode
- Timestamp: Current time

### TC-AL-003: Activity Logged - Purchase Created
**Priority:** Critical  
**Steps:**
1. Create purchase with total ₹5000
2. View Activity Log
**Expected Result:**
- Entry: "Purchase 'INV001' created"
- Amount: ₹5000
- Additional Info: Supplier name
- Entity Type: PURCHASE

### TC-AL-004: Activity Logged - Sale Created
**Priority:** Critical  
**Steps:**
1. Create sale
2. Check Activity Log
**Expected Result:**
- Entry: "Sale 'SALE001' created"
- Customer name in Additional Info
- Amount displayed

### TC-AL-005: Activity Logged - Expense Added
**Priority:** High  
**Steps:**
1. Add expense ₹2000
2. View log
**Expected Result:**
- Entry: "Expense 'EXP001' added"
- Amount: ₹2000
- Category in Additional Info

### TC-AL-006: Activity Logged - Customer Added
**Priority:** Medium  
**Steps:**
1. Add customer
2. Check log
**Expected Result:**
- Entry: "Customer 'Jane Doe' added"
- Phone in Additional Info

### TC-AL-007: Activity Logged - Supplier Added
**Priority:** Medium  
**Steps:**
1. Add supplier
2. Check log
**Expected Result:**
- Entry: "Supplier 'ABC Corp' added"
- Phone in Additional Info

### TC-AL-008: Activity Logged - Delete Operations
**Priority:** High  
**Steps:**
1. Delete product/purchase/sale/expense
2. Check log
**Expected Result:**
- DELETE activity logged
- Entity details preserved

### TC-AL-009: Search Activities by Description
**Priority:** Medium  
**Steps:**
1. Enter search term in Activity Log search
2. Search for "Purchase"
**Expected Result:**
- Only purchase-related activities shown
- Real-time filtering

### TC-AL-010: Search Activities by Document Number
**Priority:** Medium  
**Steps:**
1. Search by invoice number
**Expected Result:**
- Matching activities displayed

### TC-AL-011: Search Activities by Entity Type
**Priority:** Medium  
**Steps:**
1. Search "SALE" or "CUSTOMER"
**Expected Result:**
- Filtered by entity type

### TC-AL-012: Color-Coded Activity Types
**Priority:** Low  
**Steps:**
1. View Activity Log
**Expected Result:**
- ADD badges: Green
- EDIT badges: Orange
- DELETE badges: Red
- Clear visual distinction

### TC-AL-013: Home Screen Recent Activities
**Priority:** High  
**Steps:**
1. Navigate to Home screen
2. Check Recent Activity section
**Expected Result:**
- Latest 5 activities displayed
- "View All" button present
- Clicking "View All" navigates to Activity Log

### TC-AL-014: Activity Log Empty State
**Priority:** Low  
**Precondition:** No activities logged yet  
**Steps:**
1. View Activity Log
**Expected Result:**
- "No activities found" message
- Empty state UI displayed

---

## 10. Reports & Export

### TC-RE-001: Export Stock Summary Report
**Priority:** High  
**Precondition:** Products with stock exist  
**Steps:**
1. Navigate to Reports
2. Click "Stock Summary"
3. Select date range (optional)
4. Click Export
**Expected Result:**
- Excel file created in Downloads
- Filename: StockSummary_YYYYMMDD_HHMMSS.xlsx
- Contains: Product Name, Barcode, Category, Quantity, MRP, Sale Price
- Toast: "Stock summary exported"

### TC-RE-002: Export Out-of-Stock Report
**Priority:** High  
**Precondition:** Some products have 0 stock  
**Steps:**
1. Click "Out of Stock Report"
2. Export
**Expected Result:**
- Only 0-stock products listed
- Excel format
- Professional formatting

### TC-RE-003: Inventory Valuation - FIFO Method
**Priority:** High  
**Steps:**
1. Select "Inventory Valuation"
2. Choose method: FIFO
3. Export
**Expected Result:**
- Excel with FIFO calculations
- Stock valued at first-in purchase prices
- Total valuation calculated

### TC-RE-004: Inventory Valuation - LIFO Method
**Priority:** High  
**Steps:**
1. Choose LIFO method
2. Export
**Expected Result:**
- Stock valued at last-in purchase prices
- Different total than FIFO

### TC-RE-005: Inventory Valuation - Weighted Average
**Priority:** High  
**Steps:**
1. Choose Weighted Average
2. Export
**Expected Result:**
- Average cost calculated
- Consistent valuation method

### TC-RE-006: COGS Report Export
**Priority:** Medium  
**Steps:**
1. Select date range
2. Export COGS report
**Expected Result:**
- Cost of Goods Sold calculated
- Date range respected
- Sales and purchase costs compared

### TC-RE-007: Inventory Aging Report
**Priority:** Medium  
**Steps:**
1. Export Aging Report
**Expected Result:**
- Products grouped by age
- Categories: 0-30 days, 31-60, 61-90, >90 days
- Stock value per age group

### TC-RE-008: Purchase Document Export
**Priority:** Medium  
**Steps:**
1. Select date range
2. Export Purchases
**Expected Result:**
- All purchases in range exported
- Details: Invoice, Date, Supplier, Items, Total
- Excel format

### TC-RE-009: Sale Document Export
**Priority:** Medium  
**Steps:**
1. Export Sales
2. Select date range
**Expected Result:**
- Sales data exported
- Customer, items, amounts included

### TC-RE-010: Import Purchases from Excel
**Priority:** Medium  
**Steps:**
1. Click "Import Purchases"
2. Select valid Excel file
3. Import
**Expected Result:**
- Purchases created in database
- Stock updated
- Success message with count

### TC-RE-011: Import Invalid Excel Format
**Priority:** Medium  
**Steps:**
1. Attempt to import incorrectly formatted file
**Expected Result:**
- Error message displayed
- No data imported
- User guided on correct format

### TC-RE-012: Storage Permission Denied
**Priority:** High  
**Precondition:** Storage permission not granted  
**Steps:**
1. Attempt export
**Expected Result:**
- Permission request dialog shown
- If denied, user informed to enable in settings

---

## 11. Barcode Scanning

### TC-BC-001: Scan Barcode - Camera
**Priority:** High  
**Steps:**
1. In Purchase/Sale screen, click barcode icon
2. Grant camera permission
3. Point camera at barcode
4. Barcode detected
**Expected Result:**
- Barcode auto-populated in field
- Product lookup performed
- Camera closed automatically

### TC-BC-002: Switch Camera (Front/Back)
**Priority:** Medium  
**Steps:**
1. Open scanner
2. Click switch camera button
**Expected Result:**
- Camera switches between front/back
- Scanning continues to work

### TC-BC-003: Barcode Not Found
**Priority:** Medium  
**Steps:**
1. Scan barcode not in database
**Expected Result:**
- Message: "Product not found"
- Option to add new product with this barcode

### TC-BC-004: Multiple Products Same Barcode
**Priority:** Medium  
**Precondition:** Multiple products with same barcode exist  
**Steps:**
1. Scan barcode
**Expected Result:**
- Selection dialog shown
- All matching products listed
- User selects correct product

### TC-BC-005: Bluetooth Scanner Pairing
**Priority:** High  
**Steps:**
1. Navigate to Settings > Bluetooth Scanner
2. Click "Pair Device"
3. Select scanner from list
**Expected Result:**
- Scanner paired
- Connection status: Connected
- Auto-connect enabled

### TC-BC-006: Bluetooth Scanner Input
**Priority:** High  
**Precondition:** Bluetooth scanner paired  
**Steps:**
1. In Purchase screen
2. Scan product with Bluetooth scanner
**Expected Result:**
- Barcode input received
- Product looked up automatically
- No manual typing needed

### TC-BC-007: Bluetooth Scanner Disconnect
**Priority:** Medium  
**Steps:**
1. Turn off Bluetooth scanner
2. Attempt scanning
**Expected Result:**
- Connection status: Disconnected
- User notified
- Option to reconnect

### TC-BC-008: Camera Permission Denied
**Priority:** High  
**Steps:**
1. Deny camera permission
2. Attempt to scan
**Expected Result:**
- Error message displayed
- User guided to settings to enable permission

---

## 12. Settings & Backup

### TC-SE-001: Database Backup
**Priority:** Critical  
**Steps:**
1. Navigate to Settings
2. Click "Backup Database"
3. Grant storage permission
**Expected Result:**
- Backup file created in Downloads
- Filename: stocksmart_backup_YYYYMMDD_HHMMSS.db
- Success toast displayed
- File size > 0

### TC-SE-002: Database Restore
**Priority:** Critical  
**Precondition:** Valid backup file exists  
**Steps:**
1. Click "Restore Database"
2. Select backup file
3. Confirm restore
**Expected Result:**
- Warning: "This will replace current data"
- On confirmation, database restored
- App data matches backup
- App restart prompted

### TC-SE-003: Restore Invalid File
**Priority:** High  
**Steps:**
1. Attempt to restore non-database file
**Expected Result:**
- Error: "Invalid backup file"
- Database unchanged

### TC-SE-004: Backup with No Storage Permission
**Priority:** High  
**Steps:**
1. Deny storage permission
2. Attempt backup
**Expected Result:**
- Permission request shown
- Backup fails if denied
- User guided to enable permission

### TC-SE-005: Multiple Backups Management
**Priority:** Low  
**Steps:**
1. Create multiple backups
2. List backups
**Expected Result:**
- All backups listed with timestamps
- Option to delete old backups (manual via file manager)

---

## 13. Dashboard & Home

### TC-DH-001: Dashboard Stats Display
**Priority:** High  
**Steps:**
1. Open app (Home screen)
**Expected Result:**
- Cards display:
  - Total Purchases
  - Total Sales
  - Total Expenses
  - Out of Stock count
  - Active Suppliers
  - Active Customers
- Stats accurate and real-time

### TC-DH-002: Quick Actions - Add Purchase
**Priority:** High  
**Steps:**
1. Click "Purchases" card on Home
**Expected Result:**
- Navigate to Add Purchase screen
- Ready to create new purchase

### TC-DH-003: Quick Actions - Add Sale
**Priority:** High  
**Steps:**
1. Click "Sales" card
**Expected Result:**
- Navigate to Add Sale screen

### TC-DH-004: Navigate to Stock Management
**Priority:** Medium  
**Steps:**
1. Click "Stock" card
**Expected Result:**
- Navigate to Stock Management screen

### TC-DH-005: Recent Activity Feed
**Priority:** High  
**Steps:**
1. View Home screen
2. Check Recent Activity section
**Expected Result:**
- Latest 5 activities shown
- Activity type badges visible
- Amounts displayed
- Timestamps shown

### TC-DH-006: Pull-to-Refresh Dashboard
**Priority:** Medium  
**Steps:**
1. Pull down on Home screen
**Expected Result:**
- Loading indicator shown
- Stats refreshed
- Recent activities reloaded

### TC-DH-007: Dashboard Loading State
**Priority:** Low  
**Steps:**
1. Launch app (slow device)
**Expected Result:**
- Progress indicators shown while loading
- Stats load gracefully
- No UI freezing

### TC-DH-008: Dashboard Error Handling
**Priority:** Medium  
**Precondition:** Database error simulated  
**Steps:**
1. Trigger database error
**Expected Result:**
- Error toast displayed
- App remains functional
- Retry option available

---

## 14. Navigation & UI

### TC-UI-001: Navigation Drawer - Open/Close
**Priority:** High  
**Steps:**
1. Tap hamburger icon / swipe from left edge
**Expected Result:**
- Navigation drawer opens
- Menu items visible
- Store details in header

### TC-UI-002: Navigate to All Modules
**Priority:** High  
**Steps:**
1. Open drawer
2. Click each menu item:
   - Home
   - Store Details
   - Purchases
   - Sales
   - Stock Management
   - Suppliers
   - Customers
   - Expenses
   - Activity Log
   - Reports
   - Settings
**Expected Result:**
- Each screen loads correctly
- No crashes
- Smooth transitions

### TC-UI-003: Back Button Navigation
**Priority:** Medium  
**Steps:**
1. Navigate deep into app (Home > Purchases > Add Purchase)
2. Press back button
**Expected Result:**
- Navigate back through screens
- Eventually return to Home
- Exit app on final back press

### TC-UI-004: Toolbar Title Updates
**Priority:** Low  
**Steps:**
1. Navigate to different screens
**Expected Result:**
- Toolbar title matches current screen

### TC-UI-005: Splash Screen
**Priority:** Low  
**Steps:**
1. Launch app
**Expected Result:**
- Splash screen shown for 2 seconds
- Branding displayed
- Transition to Home screen

### TC-UI-006: Empty State Handling
**Priority:** Medium  
**Steps:**
1. View screen with no data (e.g., Products with 0 products)
**Expected Result:**
- "No products found" message
- Empty state icon/illustration
- Clear call-to-action (Add Product button)

### TC-UI-007: Pagination UI
**Priority:** Medium  
**Steps:**
1. Navigate through paginated list
**Expected Result:**
- Page number displayed
- Previous/Next buttons functional
- Disabled state for first/last page

### TC-UI-008: Search UI Responsiveness
**Priority:** Medium  
**Steps:**
1. Type in search box rapidly
**Expected Result:**
- No lag
- Results filter in real-time
- Smooth scrolling

### TC-UI-009: Date Picker Functionality
**Priority:** Medium  
**Steps:**
1. Click date field in Purchase/Sale/Report
**Expected Result:**
- Calendar dialog opens
- Date selectable
- Selected date populated in field

### TC-UI-010: Toast Notifications
**Priority:** Low  
**Steps:**
1. Perform various actions
**Expected Result:**
- Appropriate toasts shown:
  - Success: Green/check icon
  - Error: Red/error icon
  - Info: Neutral
- Auto-dismiss after 2-3 seconds

---

## Test Summary

### Total Test Cases by Module

| Module | Test Cases | Priority Critical/High |
|--------|------------|------------------------|
| Store Details | 4 | 3 |
| Product Management | 12 | 7 |
| Purchase Management | 10 | 6 |
| Sales Management | 8 | 5 |
| Stock Management | 7 | 5 |
| Supplier Management | 6 | 3 |
| Customer Management | 6 | 3 |
| Expense Management | 6 | 3 |
| Activity Log | 14 | 7 |
| Reports & Export | 12 | 7 |
| Barcode Scanning | 8 | 5 |
| Settings & Backup | 5 | 4 |
| Dashboard & Home | 8 | 4 |
| Navigation & UI | 10 | 3 |
| **TOTAL** | **116** | **65** |

### Test Execution Priority

**Priority 1 (Critical):** 25 test cases - Core business logic, data integrity  
**Priority 2 (High):** 40 test cases - Key features, user workflows  
**Priority 3 (Medium):** 38 test cases - Secondary features, edge cases  
**Priority 4 (Low):** 13 test cases - UI polish, nice-to-have features  

### Testing Guidelines

1. **Smoke Testing:** Execute all Priority 1 (Critical) tests before release
2. **Regression Testing:** Run full test suite after major changes
3. **UAT (User Acceptance Testing):** Focus on real-world scenarios
4. **Performance Testing:** Test with large datasets (1000+ products)
5. **Device Testing:** Test on multiple Android versions (8.0+)

### Known Limitations

- Multi-user concurrent editing not supported
- Network/cloud features out of scope
- Barcode scanner compatibility varies by device
- Large datasets may impact performance on low-end devices

---

## 15. UI/UX Design Guidelines & Theme Specifications

### 15.1 Color Theme

#### Primary Color Palette (Light Mode)
| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Primary Navy Blue | `#0D47A1` | App Bar, Primary buttons, Status bar |
| Secondary Blue | `#1976D2` | Accent elements, Links |
| Accent Green | `#43A047` | Success actions, Positive indicators |
| Background White | `#FFFFFF` | Main background |
| Surface Light Gray | `#ECEFF1` | Card surfaces |
| Text Primary | `#212121` | Main text, Headings |
| Text Secondary | `#757575` | Hints, Labels |

#### Dark Mode Color Palette
| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Dark Primary Navy | `#0A357A` | Primary UI elements |
| Dark Background | `#0F1115` | Main background |
| Dark Surface | `#1A1C20` | Card backgrounds |
| Dark Text Primary | `#E6E6E6` | Main text |
| Dark Text Secondary | `#B3B3B3` | Secondary text |

#### Dashboard Card Colors (Light Mode)
| Card Type | Background | Text Color | Icon Color |
|-----------|------------|------------|------------|
| Purchases | `#E3F2FD` (Light Blue) | `#0D47A1` (Navy) | Navy Blue |
| Sales | `#E8F5E9` (Light Green) | `#2E7D32` (Dark Green) | Green |
| Out of Stock | `#FFEBEE` (Light Red) | `#C62828` (Red) | Red |
| Stock/Products | `#E8EAF6` (Light Indigo) | `#3F51B5` (Indigo) | Indigo |
| Reports | `#E0F2F1` (Light Teal) | `#00796B` (Teal) | Teal |
| Expenses | `#FFF9C4` (Light Yellow) | `#F57F17` (Amber) | Amber |
| Customers | `#F3E5F5` (Light Purple) | `#7B1FA2` (Purple) | Purple |
| Suppliers | `#F1F8E9` (Light Lime) | `#558B2F` (Olive) | Olive Green |
| Settings | `#ECEFF1` (Light Gray) | `#455A64` (Blue Gray) | Blue Gray |

#### Status Colors
| Status | Color | Hex Code |
|--------|-------|----------|
| In Stock | Green | `#4CAF50` |
| Low Stock | Orange | `#FF9800` |
| Out of Stock | Red | `#F44336` |
| Success | Green | `#4CAF50` |
| Warning | Orange | `#FFA726` |
| Error/Danger | Red | `#EF5350` |
| Info | Blue | `#29B6F6` |

#### Activity Log Entity Colors
| Entity Type | Color | Hex Code |
|-------------|-------|----------|
| Purchase | Green | `#4CAF50` |
| Sale | Red | `#D32F2F` |
| Expense | Orange | `#FF9800` |
| Product | Navy Blue | `#0D47A1` |
| Customer | Purple | `#9C27B0` |
| Supplier | Teal | `#009688` |
| Report | Light Green | `#8BC34A` |

### 15.2 Typography

#### Text Sizes
| Element | Size (sp) | Weight | Color |
|---------|-----------|--------|-------|
| App Bar Title | 20sp | Normal | White |
| Page Heading | 24sp | Bold | Text Primary |
| Section Heading | 18sp | Bold | Text Primary |
| Card Title | 16sp | Bold | Text Primary |
| Body Text | 14sp | Normal | Text Primary |
| Button Text | 14sp | Medium | White/Primary |
| Caption/Hint | 12sp | Normal | Text Secondary |
| Small Label | 10sp | Normal | Text Secondary |
| Input Text | 16sp | Normal | Text Primary |

#### Text Styles
```xml
<!-- Headings -->
Page Heading: 24sp, Bold, #212121
Section Heading: 18sp, Bold, #212121
Card Title: 16sp, Bold, #212121

<!-- Body Text -->
Primary Text: 14sp, Normal, #212121
Secondary Text: 12sp, Normal, #757575
Caption: 10sp, Normal, #757575

<!-- Input Fields -->
Input Label: 12sp, Normal, #757575
Input Text: 16sp, Normal, #212121
Hint Text: 14sp, Normal, #CCCCCC
Error Text: 12sp, Normal, #D32F2F
```

### 15.3 Spacing & Dimensions

#### Padding & Margins
| Element | Value | Usage |
|---------|-------|-------|
| Screen Padding | 16dp | Horizontal/Vertical screen margins |
| Card Margin | 8dp | Space between cards |
| Card Padding | 16dp | Internal card padding |
| Button Padding | 12dp (H), 8dp (V) | Button internal padding |
| Input Field Padding | 12dp | Horizontal padding |
| List Item Padding | 16dp | List item vertical/horizontal |
| Section Spacing | 24dp | Space between sections |
| Element Spacing | 8dp | Space between related elements |

#### Component Sizes
| Component | Width | Height | Notes |
|-----------|-------|--------|-------|
| FAB Button | 56dp | 56dp | Floating Action Button |
| Icon Button | 40dp | 40dp | Standard icon buttons |
| Small Icon | 24dp | 24dp | List item icons |
| Large Icon | 48dp | 48dp | Feature icons |
| App Bar | match_parent | 56dp | Toolbar height |
| Navigation Drawer | 280dp | match_parent | Drawer width |
| Nav Header | match_parent | 176dp | Drawer header |
| List Item | match_parent | 72dp (min) | Standard list item |
| Card Height | wrap_content | - | Dynamic based on content |

### 15.4 Text Fields (EditText)

#### Appearance
- **Background Color:** White (`#FFFFFF`)
- **Text Color:** Dark Charcoal (`#333333`)
- **Hint Color:** Medium Gray (`#CCCCCC`)
- **Border:** 1dp stroke, Light Gray (`#E0E0E0`)
- **Corner Radius:** 4dp
- **Padding:** 12dp horizontal
- **Height:** 48dp minimum (single line)
- **Text Size:** 16sp

#### States
- **Normal:** Light gray border
- **Focused:** Primary navy blue border (`#0D47A1`)
- **Error:** Red border (`#D32F2F`) with error text below
- **Disabled:** Gray background (`#F5F5F5`), gray text

#### Input Types
```xml
Single Line: maxLines="1", inputType="text"
Multi-line: minLines="3", maxLines="5"
Number: inputType="number"
Decimal: inputType="numberDecimal"
Phone: inputType="phone"
Email: inputType="textEmailAddress"
Date: Non-editable, click opens DatePicker
```

### 15.5 Buttons

#### Primary Button (Filled)
- **Background:** Accent Green (`#43A047`)
- **Text Color:** White (`#FFFFFF`)
- **Corner Radius:** 8dp
- **Elevation:** 2dp
- **Padding:** 12dp horizontal
- **Height:** 48dp
- **Text Size:** 14sp, Medium weight
- **Ripple Effect:** White with 30% opacity

#### Secondary Button (Outlined)
- **Background:** Transparent/White
- **Border:** 1dp Accent Green (`#43A047`)
- **Text Color:** Accent Green (`#43A047`)
- **Corner Radius:** 8dp
- **Padding:** 12dp horizontal
- **Height:** 48dp
- **Ripple Effect:** Green with 15% opacity

#### Text Button
- **Background:** Transparent
- **Text Color:** Primary Navy Blue (`#0D47A1`)
- **No Border**
- **Text Size:** 14sp
- **Ripple Effect:** Navy with 15% opacity

#### Button Sizes
| Size | Width | Height | Usage |
|------|-------|--------|-------|
| Small | wrap_content | 36dp | Inline actions |
| Medium | wrap_content | 48dp | Standard actions |
| Large | match_parent | 56dp | Primary CTAs |

#### Button States
- **Normal:** Full opacity, elevation 2dp
- **Pressed:** Ripple effect, elevation 4dp
- **Disabled:** 38% opacity, no elevation
- **Loading:** Progress indicator replacing text

### 15.6 Cards (MaterialCardView)

#### Standard Card
- **Background:** White (`#FFFFFF`)
- **Corner Radius:** 8dp - 12dp
- **Elevation:** 2dp - 4dp
- **Margin:** 8dp
- **Padding:** 16dp
- **Stroke:** None (or 1dp light gray for emphasis)

#### Dashboard Cards
- **Corner Radius:** 12dp
- **Elevation:** 4dp
- **Min Height:** 100dp
- **Colored Background:** Based on card type (see color table)
- **Icon Size:** 40dp x 40dp
- **Title Size:** 14sp, Bold
- **Value Size:** 24sp, Bold

#### List Item Cards
- **Corner Radius:** 8dp
- **Elevation:** 2dp
- **Margin:** 8dp vertical, 16dp horizontal
- **Clickable:** Ripple effect on touch

### 15.7 Dialogs & Popups

#### Dialog Specifications
- **Background:** White (`#FFFFFF`)
- **Corner Radius:** 12dp
- **Elevation:** 8dp
- **Max Width:** 420dp (default), 720dp (forms)
- **Padding:** 24dp
- **Title Size:** 20sp, Bold
- **Content Padding:** 16dp top/bottom

#### Dialog Buttons
- **Alignment:** Right-aligned (horizontal)
- **Spacing:** 8dp between buttons
- **Button Order:** Negative (Cancel) left, Positive (OK) right
- **Style:** Text buttons for dialogs

#### Alert Dialogs
```
Title: 20sp, Bold, Navy Blue
Message: 14sp, Normal, Text Primary
Buttons: Uppercase, 14sp, Medium
Icon (optional): 48dp x 48dp, Colored based on type
```

#### Bottom Sheets
- **Background:** White
- **Corner Radius:** 16dp (top corners only)
- **Handle:** 32dp x 4dp gray bar at top
- **Peek Height:** 200dp

### 15.8 Lists & RecyclerViews

#### List Item Layout
- **Height:** 72dp (two-line), 56dp (single-line)
- **Padding:** 16dp horizontal, 8dp vertical
- **Divider:** 1dp, Light Gray (`#E0E0E0`)
- **Icon:** 40dp x 40dp (left aligned)
- **Text Margin:** 16dp from icon

#### List Item States
- **Normal:** White background
- **Selected:** Light gray background (`#F5F5F5`)
- **Pressed:** Ripple effect
- **Swipe Actions:** Reveal actions (delete, edit)

#### Pagination
- **Position:** Bottom of list
- **Style:** Outlined buttons (Previous/Next)
- **Page Indicator:** "Page X of Y" text
- **Spacing:** 16dp from list bottom

### 15.9 Navigation

#### App Bar (Toolbar)
- **Height:** 56dp
- **Background:** Primary Navy Blue (`#0D47A1`)
- **Title Color:** White (`#FFFFFF`)
- **Title Size:** 20sp
- **Icon Color:** White
- **Elevation:** 4dp

#### Navigation Drawer
- **Width:** 280dp
- **Background:** White
- **Header Height:** 176dp
- **Header Background:** Primary Navy Blue gradient
- **Menu Item Height:** 48dp
- **Menu Item Padding:** 16dp horizontal
- **Selected Item:** Light blue background (`#E3F2FD`)
- **Divider:** 1dp gray between sections

#### Bottom Navigation (if used)
- **Height:** 56dp
- **Background:** White
- **Selected Color:** Primary Navy Blue
- **Unselected Color:** Gray (`#757575`)
- **Icon Size:** 24dp

### 15.10 Icons

#### Icon Sizes
| Context | Size | Color |
|---------|------|-------|
| App Bar Icons | 24dp | White |
| Menu Icons | 24dp | Text Secondary |
| List Item Icons | 40dp | Entity-specific color |
| Card Icons | 48dp | Card-specific color |
| FAB Icon | 24dp | White |
| Status Icons | 16dp | Status color |

#### Icon Tinting
- **Primary Actions:** Navy Blue (`#0D47A1`)
- **Success:** Green (`#4CAF50`)
- **Warning:** Orange (`#FF9800`)
- **Error:** Red (`#F44336`)
- **Disabled:** Gray (`#BDBDBD`)

### 15.11 Form Layouts

#### Form Field Spacing
```
Label to Input: 4dp
Input to Error: 4dp
Error to Next Label: 16dp
Field Groups: 24dp separation
Form Padding: 16dp
```

#### Form Elements
```xml
Label: 12sp, Text Secondary, above input
Input: 16sp, 48dp height, white background
Required Indicator: Red asterisk (*) after label
Helper Text: 12sp, Text Secondary, below input
Error Text: 12sp, Red, below input with error icon
```

#### Radio Buttons & Checkboxes
- **Size:** 20dp x 20dp
- **Label Size:** 14sp
- **Label Color:** Text Primary (`#212121`)
- **Selected Color:** Accent Green
- **Unselected Color:** Text Secondary
- **Spacing:** 8dp between radio/checkbox and label

### 15.12 Loading & Progress Indicators

#### Progress Bar (Determinate)
- **Height:** 4dp
- **Color:** Primary Navy Blue
- **Background:** Light Gray (`#E0E0E0`)

#### Circular Progress (Indeterminate)
- **Size:** 48dp (default), 24dp (small)
- **Color:** Primary Navy Blue
- **Stroke Width:** 4dp

#### Shimmer Loading
- **Base Color:** Light Gray (`#E0E0E0`)
- **Highlight Color:** White (`#FFFFFF`)
- **Duration:** 1000ms

### 15.13 Empty States

#### Empty State Layout
- **Icon Size:** 96dp x 96dp
- **Icon Color:** Light Gray (`#BDBDBD`)
- **Title Size:** 18sp, Text Primary
- **Message Size:** 14sp, Text Secondary
- **Button:** Primary filled button
- **Vertical Spacing:** 24dp between elements

### 15.14 Search Bars

#### Search View
- **Height:** 48dp
- **Background:** White with 1dp gray border
- **Corner Radius:** 24dp (rounded)
- **Icon Size:** 24dp (search/clear icons)
- **Text Size:** 16sp
- **Hint Color:** Text Secondary
- **Margin:** 16dp horizontal

### 15.15 Badges & Chips

#### Badge
- **Size:** 16dp x 16dp (small), 20dp x 20dp (medium)
- **Background:** Red (`#F44336`)
- **Text Color:** White
- **Text Size:** 10sp
- **Position:** Top-right of icon

#### Chip (Activity Type)
- **Height:** 24dp
- **Padding:** 8dp horizontal, 2dp vertical
- **Corner Radius:** 12dp
- **Text Size:** 10sp, Bold
- **Text Color:** White
- **Background Colors:**
  - ADD: Green (`#4CAF50`)
  - EDIT: Orange (`#FF9800`)
  - DELETE: Red (`#F44336`)
  - GENERATE: Blue (`#2196F3`)

### 15.16 Shadows & Elevation

#### Elevation Levels
| Level | Usage | dp |
|-------|-------|-----|
| 0dp | Flat surfaces | 0dp |
| 2dp | Cards (resting) | 2dp |
| 4dp | App Bar, Raised Cards | 4dp |
| 6dp | Snackbar | 6dp |
| 8dp | Dialogs, Modals | 8dp |
| 12dp | FAB (resting) | 12dp |
| 16dp | Navigation Drawer | 16dp |

### 15.17 Animations & Transitions

#### Duration Standards
| Animation Type | Duration |
|----------------|----------|
| Simple | 100ms |
| Standard | 300ms |
| Complex | 500ms |
| Screen Transition | 400ms |

#### Animation Types
- **Fade In/Out:** Alpha 0 to 1
- **Slide In:** From edge to position
- **Expand/Collapse:** Height animation
- **Ripple:** Touch feedback
- **Page Transition:** Slide left/right

### 15.18 Accessibility

#### Touch Targets
- **Minimum Size:** 48dp x 48dp
- **Recommended:** 56dp x 56dp for primary actions

#### Contrast Ratios
- **Normal Text:** 4.5:1 minimum
- **Large Text:** 3:1 minimum
- **Icons:** 3:1 minimum

#### Content Descriptions
- All ImageViews: contentDescription attribute
- Icon Buttons: Descriptive labels
- Decorative Images: contentDescription=""

---

**Document Version:** 1.0  
**Last Updated:** November 21, 2025  
**Prepared By:** StockSmart QA Team
