package com.example.cheerschecklist

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

class DatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        schemaDirectoryPath = Path.of("schemas"),
        databasePath = Files.createTempFile("cheerschecklist-migration-test", ".db"),
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    @Test
    fun migrate1To3_preservesExistingRowAndAddsNullableColumns() {
        val v1 = helper.createDatabase(1)
        v1.execSQL(
            "INSERT INTO TastedDrink (id, name, category, dateTasted, rating, notes) " +
                "VALUES (1, 'Aberlour 12', 'WHISKY', '2026-09-18', 5, '')",
        )
        v1.close()

        val migrated = helper.runMigrationsAndValidate(3, listOf(MIGRATION_1_2, MIGRATION_2_3))
        migrated.prepare("SELECT name, brand, color, oiliness, scent, flavor FROM TastedDrink WHERE id = 1").use { stmt ->
            assertTrue(stmt.step())
            assertEquals("Aberlour 12", stmt.getText(0))
            assertTrue(stmt.isNull(1))
            assertTrue(stmt.isNull(2))
            assertTrue(stmt.isNull(3))
            assertTrue(stmt.isNull(4))
            assertTrue(stmt.isNull(5))
        }
        migrated.close()
    }

    @Test
    fun migrate2To3_preservesBrandAndAddsNullableColumns() {
        val v2 = helper.createDatabase(2)
        v2.execSQL(
            "INSERT INTO TastedDrink (id, name, brand, category, dateTasted, rating, notes) " +
                "VALUES (1, 'Chardonnay', 'Concha y Toro', 'WINE', '2026-03-15', 4, '')",
        )
        v2.close()

        val migrated = helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3))
        migrated.prepare("SELECT brand, color, oiliness, scent, flavor FROM TastedDrink WHERE id = 1").use { stmt ->
            assertTrue(stmt.step())
            assertEquals("Concha y Toro", stmt.getText(0))
            assertTrue(stmt.isNull(1))
            assertTrue(stmt.isNull(2))
            assertTrue(stmt.isNull(3))
            assertTrue(stmt.isNull(4))
        }
        migrated.close()
    }
}
