/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.loginapp.di

import android.content.Context
import androidx.room.Room
import com.example.loginapp.data.repositories.auth.AuthRepository
import com.example.loginapp.data.repositories.auth.DefaultLoginRepository
import com.example.loginapp.data.repositories.task.DefaultTaskRepository
import com.example.loginapp.data.repositories.task.TaskRepository
import com.example.loginapp.data.source.local.TaskDao
import com.example.loginapp.data.source.local.ToDoDatabase
import com.example.loginapp.data.source.network.NetworkDataSource
import com.example.loginapp.data.source.network.TaskNetworkDataSource
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindTaskRepository(repository: DefaultTaskRepository): TaskRepository

    @Singleton
    @Binds
    abstract fun bindLoginRepository(repository: DefaultLoginRepository): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SignInClientModule {

    @Singleton
    @Provides
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient {
        return Identity.getSignInClient(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun bindNetworkDataSource(dataSource: TaskNetworkDataSource): NetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: ToDoDatabase): TaskDao = database.taskDao()
}
