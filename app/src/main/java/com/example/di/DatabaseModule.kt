package com.example.di

import android.content.Context
import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.local.TamTamDatabase
import com.example.data.local.UserDao

object DatabaseModule {
    @Volatile
    private var databaseInstance: TamTamDatabase? = null

    fun provideDatabase(context: Context): TamTamDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: TamTamDatabase.getDatabase(context).also { databaseInstance = it }
        }
    }

    fun provideUserDao(database: TamTamDatabase): UserDao {
        return database.userDao()
    }

    fun provideConversationDao(database: TamTamDatabase): ConversationDao {
        return database.conversationDao()
    }

    fun provideMessageDao(database: TamTamDatabase): MessageDao {
        return database.messageDao()
    }
}
