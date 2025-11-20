package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create expense_categories table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                categoryName TEXT NOT NULL,
                categoryType TEXT NOT NULL,
                notes TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Create expenses table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expenses (
                expenseId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                expenseDate INTEGER NOT NULL,
                expenseCategory TEXT NOT NULL,
                expenseType TEXT NOT NULL,
                description TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Pre-populate expense categories - OPEX categories
        val opexCategories = listOf(
            Triple("Travel", "OPEX", "Travel, food, lodging"),
            Triple("Rent", "OPEX", "Office/warehouse rent"),
            Triple("Utilities", "OPEX", "Electricity, water, internet"),
            Triple("Office Supplies", "OPEX", "Stationary, consumables"),
            Triple("Fuel", "OPEX", "Vehicle fuel"),
            Triple("Courier & Logistics", "OPEX", "Shipping charges"),
            Triple("Sales & Marketing", "OPEX", "Ads, promotions"),
            Triple("Staff Welfare", "OPEX", "Snacks, gifts"),
            Triple("Professional Fees", "OPEX", "CA, lawyer, audit fees"),
            Triple("Subscription (Monthly/Annual)", "OPEX", "SaaS tools"),
            Triple("Minor Repairs & Maintenance", "OPEX", "Routine service"),
            Triple("Salaries / Wages", "OPEX", "Monthly salaries for staff"),
            Triple("Employee Benefits", "OPEX", "Allowances, PF, gratuity, perks"),
            Triple("Contractor / Freelancer Payments", "OPEX", "Short-term outsourced work"),
            Triple("Overtime Payments", "OPEX", "Additional work hours"),
            Triple("Bonus / Incentives", "OPEX", "Sales incentives, performance bonuses"),
            Triple("Employee Insurance (Group)", "OPEX", "Health, life insurance"),
            Triple("Training & Development", "OPEX", "Course fees, certifications"),
            Triple("Recruitment Expenses", "OPEX", "Hiring portal fees, consultancy"),
            Triple("Travel Allowance (TA)", "OPEX", "Employee travel expenses"),
            Triple("Daily Allowance (DA)", "OPEX", "Meals, stay, incidental expenses")
        )

        // Pre-populate expense categories - CAPEX categories
        val capexCategories = listOf(
            Triple("Furniture Purchase", "CAPEX", "Tables, chairs, desks"),
            Triple("Machinery Purchase", "CAPEX", "Major equipment"),
            Triple("IT Equipment Purchase", "CAPEX", "Laptops, desktops, servers"),
            Triple("Property/Building Improvements", "CAPEX", "Construction, major renovation"),
            Triple("Long-term Software License", "CAPEX", "One-time perpetual license")
        )

        // Pre-populate expense categories - MIXED categories
        val mixedCategories = listOf(
            Triple("IT / Hardware", "MIXED", "New laptop → CAPEX, Repair → OPEX"),
            Triple("Furniture & Fixtures", "MIXED", "New chair → CAPEX, Cleaning → OPEX"),
            Triple("Repairs & Maintenance", "MIXED", "Normal service → OPEX, Major overhaul → CAPEX"),
            Triple("Renovation", "MIXED", "Repainting → OPEX, Constructing partition → CAPEX"),
            Triple("Software & Tools", "MIXED", "SaaS monthly → OPEX, One-time license → CAPEX")
        )

        // Add "Others" option
        val othersCategory = Triple("Others", "MIXED", "Specify category and type manually")

        // Insert all categories
        (opexCategories + capexCategories + mixedCategories + othersCategory).forEach { (name, type, notes) ->
            database.execSQL(
                """
                INSERT INTO expense_categories (categoryName, categoryType, notes) 
                VALUES (?, ?, ?)
                """.trimIndent(),
                arrayOf(name, type, notes)
            )
        }
    }
}
