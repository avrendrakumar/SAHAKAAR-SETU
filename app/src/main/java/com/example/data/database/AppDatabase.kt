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
        CooperativeEntity::class,
        WorkerProfileEntity::class,
        ServiceCategoryEntity::class,
        BookingEntity::class,
        WelfareTransactionEntity::class,
        RatingEntity::class,
        DisputeEntity::class,
        InstitutionalBookingEntity::class,
        DemandForecastEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cooperativeDao(): CooperativeDao
    abstract fun workerDao(): WorkerDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao
    abstract fun welfareDao(): WelfareDao
    abstract fun ratingDao(): RatingDao
    abstract fun disputeDao(): DisputeDao
    abstract fun institutionalDao(): InstitutionalDao
    abstract fun demandForecastDao(): DemandForecastDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahakaar_setu_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initial seed on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            if (db.cooperativeDao().getCount() == 0) {
                db.cooperativeDao().insertCooperatives(SeedData.cooperatives)
            }
            if (db.serviceDao().getCount() == 0) {
                db.serviceDao().insertServices(SeedData.serviceCategories)
            }
            if (db.workerDao().getCount() == 0) {
                val workers = SeedData.generateWorkers()
                db.workerDao().insertWorkers(workers)
                val bookings = SeedData.generateBookings(workers)
                db.bookingDao().insertBookings(bookings)
            }
            db.welfareDao().insertTransactions(SeedData.welfareTransactions)
            db.demandForecastDao().insertForecasts(SeedData.demandForecasts)
            for (d in SeedData.disputes) {
                db.disputeDao().insertDispute(d)
            }
            for (ib in SeedData.institutionalBookings) {
                db.institutionalDao().insertInstitutionalBooking(ib)
            }
            db.notificationDao().insertNotifications(SeedData.notifications)
        }
    }
}
