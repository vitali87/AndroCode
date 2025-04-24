package com.example.androcode.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Qualifier for the IO dispatcher to be used for IO-bound operations.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

/**
 * Qualifier for the Default dispatcher to be used for CPU-intensive tasks.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

/**
 * Qualifier for the Main dispatcher to be used for UI operations.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

/**
 * Module that provides various coroutine dispatchers for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    /**
     * Provides the IO dispatcher for IO-bound operations.
     * 
     * @return The IO-optimized [CoroutineDispatcher]
     */
    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    /**
     * Provides the Default dispatcher for CPU-intensive tasks.
     * 
     * @return The default [CoroutineDispatcher]
     */
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    /**
     * Provides the Main dispatcher for UI operations.
     * 
     * @return The main thread [CoroutineDispatcher]
     */
    @MainDispatcher
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
