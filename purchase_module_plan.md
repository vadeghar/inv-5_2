# Purchase Module Implementation Plan (Revised)

## 1. Database and Entities

-   **Database**: Use Room persistence library for local storage.
-   **Entities**:
    -   `Product`: Define as a Room entity.
    -   `Purchase`: Define as a Room entity.
    -   `PurchaseItem`: Define as a Room entity.
    
        ### Entities (project source)

        Below are the concrete entity definitions (fields, types, primary & foreign keys and notes) as implemented in the project source. These are taken from the Room entity classes under `app/src/main/java/com/example/inv_5/data/entities`.

        #### Purchase

        - Table name: `purchases`
        - Kotlin class: `com.example.inv_5.data.entities.Purchase`
        - Primary key: `id: String`
        - Fields:
            - `id: String` — primary key
            - `vendor: String` — vendor name / supplier
            - `totalAmount: Double` — grand total for the purchase (taxable + tax)
            - `invoiceNo: String` — supplier invoice number
            - `invoiceDate: Date` — invoice date (stored as `java.util.Date`)
            - `addedDate: Date` — date the purchase record was added to the app
            - `totalQty: Int` — total quantity across all purchase items
            - `totalTaxable: Double` — summed taxable amount across items
            - `status: String` — purchase status (example values used in UI: quoted, received, cancelled)

        #### PurchaseItem

        - Table name: `purchase_items`
        - Kotlin class: `com.example.inv_5.data.entities.PurchaseItem`
        - Primary key: `id: String`
        - Foreign keys:
            - `purchaseId` -> `purchases(id)` with `onDelete = CASCADE`
        - Fields:
            - `id: String` — primary key
            - `purchaseId: String` — foreign key referencing `purchases(id)` (mutable `var` in code so it can be assigned after creating the parent `Purchase`)
            - `productId: String` — foreign key referencing `products(id)`
            - `hsn: String` — HSN / tax code for the product
            - `mrp: Double` — MRP of the product
            - `discountAmount: Double` — discount amount (currency)
            - `discountPercentage: Double` — discount percent (0-100)
            - `rate: Double` — effective per-unit rate after discount
            - `quantity: Int` — purchased quantity
            - `taxable: Double` — taxable value for this line
            - `tax: Double` — tax amount for this line
            - `total: Double` — total amount for this line (taxable + tax)

        Notes: Declared with `@Entity(..., foreignKeys = [...])` so Room enforces referential integrity. The code must ensure that the parent `Purchase` is inserted (or already exists) before inserting its `PurchaseItem` children. In the app we perform the parent+children inserts inside a Room transaction (`db.runInTransaction { ... }`) on IO to avoid FOREIGN KEY constraint failures.

        #### Product

        - Table name: `products`
        - Kotlin class: `com.example.inv_5.data.entities.Product`
        - Primary key: `id: String`
        - Fields:
            - `id: String` — primary key
            - `name: String` — product name (Will be added when new purchase is being added)
            - `mrp: Double` — product MRP (Will be added when new purchase is being added)
            - `salePrice: Double` — default sale price (Will be added when new purchase is being added)
            - `barCode: String` — barcode string (Will be added when new purchase is being added)
            - `category: String` — product category
            - `quantityOnHand` Int - (Will be added when new purchase is being added - Addition with quantity in purchase item for this product)
            - `reorderPoint` Int (1 is default)
            - `maximumStockLevel` Int (5 is default)
            - `isActive` Boolean (True is default)
            - `addedDt` Datetime (record insertion date time)
            - `updatedDt` DateTime (record updation date time)

        Notes: Simple product master used for selecting items while adding a purchase. when the new purchase item is adding, see if there is a product with barCode and MRP combination - If exists increase the `quantityOnHand` by adding the quantity from the new purchase item. If no product exist with barCode and MRP combination, enter a new Product with given mapping details from purchase item to product.
-   **DAOs (Data Access Objects)**:
    -   `ProductDao`: For database operations related to `Product`.
    -   `PurchaseDao`: For database operations related to `Purchase`.
    -   `PurchaseItemDao`: For database operations related to `PurchaseItem`.
-   **Database Class**: Create a Room database class to hold the entities.

## 2. "Add Purchase" Screen UI

-   **Layout**: Create a new layout file for the "Add Purchase" screen.
-   **Fields**:    
    -   `Vendor`: `EditText` (Free Text)
    -   `Invoice No`: `EditText` (Free Text)
    -   `Invoice Date`: `TextView` with a click listener to show a `DatePickerDialog`.
    -   `Status`: `RadioGroup` with two `RadioButton`s for "Active" and "Inactive".
-   **Purchase Items**:
    -   `RecyclerView` to display the list of `PurchaseItem`s. Each item will show `name`, `barCode`, `qty`, and `MRP` in table format with headings in first row with serial number.
    -   `FloatingActionButton` (+) to add a new `PurchaseItem`.
-    **Fields**:
    -   `Total Taxable`: Sum of `Total` from all `PurchaseItem`s.
    -   `Total Tax` = Sum of `Tax` from all `PurchaseItem`s.
    -   `Total Qty` = Sum of `Quantity` from all `PurchaseItem`s.
    -   `Total Amount` = Sum of `Total` from all `PurchaseItem`s.
-   **Buttons**:
    -   `Save`: `Button` to save the purchase and its items to the database. (Navigation to be handled later).
    -   `Cancel`: `Button` to navigate back to the Purchase list screen.

## 3. "Add Purchase Item" Dialog

-   **Layout**: Create a new layout file for the "Add Purchase Item" dialog.
-   **Fields**:
    -   `Barcode`: `EditText` (Free Text)
    -   `HSN`: `EditText` (Free Text)
    -   `MRP`: `EditText` (NumberDecimal)
    -   `Discount Amount`: `EditText` (NumberDecimal)
    -   `Discount %`: `EditText` (NumberDecimal)
    -   `Rate`: `EditText` (NumberDecimal)
    -   `Quantity`: `EditText` (Number)
    -   `Sale Price`: `EditText` (NumberDecimal)
    -   `Taxable`: `EditText` (NumberDecimal, non-editable)
    -   `Tax %`: `EditText` (NumberDecimal)
    -   `Tax`: `EditText` (NumberDecimal, non-editable)
    -   `Total`: `EditText` (NumberDecimal, non-editable)
    
-   **Buttons**:
    -   `Add`: `Button` to add the item to the list in the "Add Purchase" screen.

## 4. Logic and Calculations

-   **Discount Calculation**:
    -   When `Discount Amount` is entered, calculate and update `Discount %`.
    -   When `Discount %` is entered, calculate and update `Discount Amount`.
    -   When `Rate` is entered, calculate and update both `Discount Amount` and `Discount %` based on `MRP`.
-   **Taxable, Tax, and Total Calculation**:
    -   `Taxable` = `Rate` \* `Quantity`
    -   `Tax` = `Taxable` \* (`Tax %` / 100)
    -   `Total` = `Taxable`
-   **Purchase Summary Calculation**:
    -   `totalAmount` = Sum of `Total` from all `PurchaseItem`s.
    -   `totalQty` = Sum of `Quantity` from all `PurchaseItem`s.
    -   `totalTaxable` = Sum of `Taxable` from all `PurchaseItem`s.
    -   `totalTax` = Sum of `Tax` from all `PurchaseItem`s.

## 5. Navigation

-   **From `MainActivity`**:
    -   Add a "Purchase" item to the navigation drawer.
    -   Clicking on "Purchase" will navigate to the `PurchasesFragment` (which will be implemented later).
-   **From `PurchasesFragment`**:
    -   Add a `FloatingActionButton` (+) to the `PurchasesFragment`.
    -   Clicking on the FAB will navigate to the `AddPurchaseActivity`.

## 6. Implementation Todo List

A new todo list will be created to reflect this revised plan.

## Implementation Tasks (concrete)

Below are prioritized, actionable implementation tasks to align the codebase with the updated plan (inventory fields on `Product`, product upsert when saving purchases, and UI mapping). Each item includes the target files and a short verification step.

1) Add inventory/metadata fields to `Product` and migration
     - Files changed/created:
         - `app/src/main/java/com/example/inv_5/data/entities/Product.kt` (added fields: `quantityOnHand`, `reorderPoint`, `maximumStockLevel`, `isActive`, `addedDt`, `updatedDt`)
         - `app/src/main/java/com/example/inv_5/data/database/AppDatabase.kt` (bumped Room `version` 1 -> 2)
         - `app/src/main/java/com/example/inv_5/data/database/ProductMigrations.kt` (new migration 1->2 that ALTERs `products` table to add columns with defaults)
         - `app/src/main/java/com/example/inv_5/data/database/DatabaseProvider.kt` (registered the migration with Room builder)
     - Verification:
         - Run `./gradlew assembleDebug` — build must succeed
         - On app start, Room should apply migration when DB version increments (or throw a clear migration error if DB is incompatible)

2) Extend `ProductDao` for lookups and updates
     - Files changed:
         - `app/src/main/java/com/example/inv_5/data/daos/ProductDao.kt` (added `getByBarcodeAndMrp`, `getById`, `getByMrpAndName`, `updateProduct`)
     - Verification:
         - Unit test / runtime check: call `getById` and `getByMrpAndName` to confirm queries return expected rows

3) Update Purchase save logic to upsert product inventory inside the same transaction
     - Files changed:
         - `app/src/main/java/com/example/inv_5/ui/purchases/AddPurchaseViewModel.kt` (modified `savePurchase()` to, for every `PurchaseItem`, attempt to find/update product or insert a new product; all inside `db.runInTransaction`)
     - Behavior:
         - If a matching product exists, `quantityOnHand` is increased by the purchased quantity and `updatedDt` set
         - If no matching product exists, a new `Product` row is inserted with `quantityOnHand = item.quantity`, `addedDt`/`updatedDt` set
     - Verification:
         - Manual test cases:
             - Add a purchase with a new product — verify `products` table has a new row and `quantityOnHand` equals the purchase quantity
             - Add a purchase referencing an existing product — verify `quantityOnHand` increments
             - Save purchase fails with FK constraint — verify transaction semantics prevent partial writes

4) UI and adapter wiring
     - Files to update:
         - `dialog_add_purchase_item.xml` / `AddPurchaseActivity.kt` / `PurchaseItemsAdapter.kt` — ensure `Sale Price` is collected and mapped to `Product.salePrice` when inserting products, and that `PurchaseItem` contains the minimal data needed to map to product (productId/barcode/mrp)
     - Verification:
         - Add item flow continues to work; new product insertion and quantity updates occur as expected

5) Optional / future work
     - Persist `salePrice` on `PurchaseItem` (historical price per line)
     - Improve product-matching heuristics (barcode + mrp + name) and present UI to resolve ambiguous matches
     - Add integration tests for DB migrations and the save transaction

Estimated order of execution and priority: tasks 1→3→2→4 (migration and transactional save are highest priority to avoid FK and inventory inconsistency issues).

Small note on assumptions made
- `PurchaseItem` currently stores `hsn` but not an explicit barcode string; in the implementation above we used `hsn` as a proxy for a barcode/name when attempting product matches. If your app collects a distinct barcode, prefer storing and matching on that field (and update `PurchaseItem` accordingly). I can change the matching logic to use a `barCode` field if you want — say so and I'll update the entity, DAO, migration and dialog mapping.

If you want me to implement the next task (I can start with the migration + product field additions and the ViewModel save logic changes I drafted), tell me which task to start and I'll mark it in the todo list and proceed with code edits, builds and quick verification steps.
