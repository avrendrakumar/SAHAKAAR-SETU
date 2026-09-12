package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CooperativeEntity::class,
        WorkerProfileEntity::class,
        ServiceCategoryEntity::class,
        BookingEntity::class,
        PaymentEntity::class,
        InvoiceEntity::class,
        RatingEntity::class,
        DisputeEntity::class,
        WelfareWalletEntity::class,
        WelfareTransactionEntity::class,
        NotificationEntity::class,
        DemandForecastEntity::class,
        InstitutionalBookingEntity::class,
        AuditLogEntity::class,
        ProfessionEntity::class,
        LocationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cooperativeDao(): CooperativeDao
    abstract fun workerDao(): WorkerDao
    abstract fun serviceCategoryDao(): ServiceCategoryDao
    abstract fun bookingDao(): BookingDao
    abstract fun paymentDao(): PaymentDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun ratingDao(): RatingDao
    abstract fun disputeDao(): DisputeDao
    abstract fun welfareDao(): WelfareDao
    abstract fun notificationDao(): NotificationDao
    abstract fun demandForecastDao(): DemandForecastDao
    abstract fun institutionalBookingDao(): InstitutionalBookingDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun professionDao(): ProfessionDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahakaar_setu_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            database.userDao().insertUsers(DatabaseSeeder.getInitialUsers())
            database.cooperativeDao().insertCooperatives(DatabaseSeeder.getInitialCooperatives())
            database.serviceCategoryDao().insertCategories(DatabaseSeeder.getInitialCategories())
            database.workerDao().insertWorkers(DatabaseSeeder.getInitialWorkers())
            database.welfareDao().insertWallets(DatabaseSeeder.getInitialWelfareWallets())
            database.bookingDao().insertBookings(DatabaseSeeder.getInitialBookings())
            database.demandForecastDao().insertForecasts(DatabaseSeeder.getInitialDemandForecasts())
            database.institutionalBookingDao().insertInstitutionalBookings(DatabaseSeeder.getInitialInstitutionalBookings())
            database.professionDao().insertProfessions(DatabaseSeeder.getInitialProfessions())
            database.locationDao().insertLocations(DatabaseSeeder.getInitialLocations())
            DatabaseSeeder.getInitialNotifications().forEach {
                database.notificationDao().insertNotification(it)
            }
            DatabaseSeeder.getInitialInvoices().forEach {
                database.invoiceDao().insertInvoice(it)
            }
        }
    }
}
