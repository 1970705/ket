package com.wordland.di

import android.content.Context
import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.data.dao.TrackingDao
import com.wordland.data.dao.WordDao
import com.wordland.data.database.WordDatabase
import com.wordland.data.repository.*
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.hint.HintManager
import com.wordland.domain.usecase.usecases.*
import com.wordland.ui.components.HapticFeedbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection Module for Wordland app
 * Provides database, DAOs, repositories, and managers
 * Updated Week 4: Added audio and haptic managers
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // ==================== DATABASE ====================

    @Provides
    @Singleton
    fun provideWordDatabase(
        @ApplicationContext context: Context,
    ): WordDatabase {
        return WordDatabase.getInstance(context)
    }

    // ==================== DAOs ====================

    @Provides
    @Singleton
    fun provideWordDao(database: WordDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideProgressDao(database: WordDatabase): ProgressDao {
        return database.progressDao()
    }

    @Provides
    @Singleton
    fun provideTrackingDao(database: WordDatabase): TrackingDao {
        return database.trackingDao()
    }

    @Provides
    @Singleton
    fun provideIslandMasteryDao(database: WordDatabase): IslandMasteryDao {
        return database.islandMasteryDao()
    }

    @Provides
    @Singleton
    fun provideWorldMapDao(database: WordDatabase): com.wordland.data.dao.WorldMapDao {
        return database.worldMapDao()
    }

    // ==================== REPOSITORIES ====================

    @Provides
    @Singleton
    fun provideWordRepository(wordDao: WordDao): WordRepository {
        return WordRepositoryImpl(wordDao)
    }

    @Provides
    @Singleton
    fun provideProgressRepository(progressDao: ProgressDao): ProgressRepository {
        return ProgressRepositoryImpl(progressDao)
    }

    @Provides
    @Singleton
    fun provideTrackingRepository(trackingDao: TrackingDao): TrackingRepository {
        return TrackingRepositoryImpl(trackingDao)
    }

    @Provides
    @Singleton
    fun provideIslandMasteryRepository(
        islandMasteryDao: IslandMasteryDao,
        progressDao: ProgressDao,
    ): IslandMasteryRepository {
        return IslandMasteryRepositoryImpl(islandMasteryDao, progressDao)
    }

    // ==================== ASSET MANAGERS ====================

    @Provides
    @Singleton
    fun provideAudioAssetManager(
        @ApplicationContext context: Context,
    ): com.wordland.data.assets.AudioAssetManager {
        return com.wordland.data.assets.AudioAssetManager(context)
    }

    @Provides
    @Singleton
    fun provideImageAssetManager(
        @ApplicationContext context: Context,
    ): com.wordland.data.assets.ImageAssetManager {
        return com.wordland.data.assets.ImageAssetManager(context)
    }

    @Provides
    @Singleton
    fun provideHapticFeedbackManager(
        @ApplicationContext context: Context,
    ): HapticFeedbackManager {
        return HapticFeedbackManager(context)
    }

    // ==================== DOMAIN MANAGERS ====================

    // ComboManager is an object (singleton), accessed directly without provider

    // ==================== USE CASES ====================

    // Home & Island Map
    @Provides
    @Singleton
    fun provideGetIslandsUseCase(islandRepo: IslandMasteryRepository): GetIslandsUseCase = GetIslandsUseCase(islandRepo)

    @Provides
    @Singleton
    fun provideGetUserStatsUseCase(
        progressRepo: ProgressRepository,
        islandRepo: IslandMasteryRepository,
        wordRepo: WordRepository,
    ) = GetUserStatsUseCase(progressRepo, islandRepo, wordRepo)

    @Provides
    @Singleton
    fun provideGetUserProgressUseCase(progressRepo: ProgressRepository) = GetUserProgressUseCase(progressRepo)

    // Level Select
    @Provides
    @Singleton
    fun provideGetLevelsUseCase(progressRepo: ProgressRepository) = GetLevelsUseCase(progressRepo)

    // Learning (Core)
    @Provides
    @Singleton
    fun provideLoadLevelWordsUseCase(wordRepo: WordRepository) = LoadLevelWordsUseCase(wordRepo)

    @Provides
    @Singleton
    fun provideSubmitAnswerUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository,
        trackingRepo: TrackingRepository,
    ) = SubmitAnswerUseCase(wordRepo, progressRepo, trackingRepo)

    @Provides
    @Singleton
    fun provideGetNextWordUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository,
    ) = GetNextWordUseCase(wordRepo, progressRepo)

    @Provides
    @Singleton
    fun provideUseHintUseCase(
        trackingRepo: TrackingRepository,
        wordRepo: WordRepository,
        hintGenerator: HintGenerator,
        hintManager: HintManager,
        behaviorAnalyzer: BehaviorAnalyzer,
    ) = UseHintUseCaseEnhanced(trackingRepo, wordRepo, hintGenerator, hintManager, behaviorAnalyzer)

    @Provides
    @Singleton
    fun provideUnlockNextLevelUseCase(progressRepo: ProgressRepository) = UnlockNextLevelUseCase(progressRepo)

    // World Map
    @Provides
    @Singleton
    fun provideWorldMapRepository(worldMapDao: com.wordland.data.dao.WorldMapDao): com.wordland.data.repository.WorldMapRepository {
        return com.wordland.data.repository.WorldMapRepositoryImpl(worldMapDao)
    }

    @Provides
    @Singleton
    fun provideExploreRegionUseCase(worldMapRepo: com.wordland.data.repository.WorldMapRepository) = ExploreRegionUseCase(worldMapRepo)

    // Review
    @Provides
    @Singleton
    fun provideGetReviewWordsUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository,
    ) = GetReviewWordsUseCase(wordRepo, progressRepo)
}
