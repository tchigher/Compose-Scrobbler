package de.schnettler.database.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import ch.tutteli.atrium.api.fluent.en_GB.notToBe
import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import de.schnettler.database.AppDatabase
import de.schnettler.database.collectValue
import de.schnettler.database.models.Artist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class BaseDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()


    @Test
    fun forceInsertOverwritesData() = runBlockingTest {
        // GIVEN - Artist in Database
        val artist = Artist(name = "TestArtist", url = "ArtistUrl")
        database.artistDao().insert(artist)

        // WHEN - Artist data changes and is updated in db
        val newArtist = artist.copy(url = "NewUrl", plays = 10)
        database.artistDao().forceInsert(newArtist)
        val loadedArtist = database.artistDao().getArtist(newArtist.id)

        // THEN - newArtist overwrites artist in database
        loadedArtist.collectValue {
            expect(it).notToBeNull().toBe(newArtist)
            expect(it).notToBe(artist)
        }
    }

    @Test
    fun insertDoesNotOverwriteData() = runBlockingTest {
        // GIVEN - Artist in Database
        val artist = Artist(name = "TestArtist", url = "ArtistUrl")
        database.artistDao().insert(artist)

        // WHEN - Artist data changes and is updated in db
        val newArtist = artist.copy(url = "NewUrl", plays = 10)
        database.artistDao().insert(newArtist)
        val loadedArtist = database.artistDao().getArtist(newArtist.id)

        // THEN - artist is not overwritten by newArtist
        loadedArtist.collectValue {
            expect(it).notToBe(newArtist)
            expect(it).toBe(artist)
        }
    }

    @Test
    fun deleteRemovesData() = runBlockingTest {
        // GIVEN - Artist in Database
        val artist = Artist(name = "TestArtist", url = "ArtistUrl")
        database.artistDao().insert(artist)

        // WHEN - Deleted Artist
        database.artistDao().delete(artist)
        val loadedArtist = database.artistDao().getArtist(artist.id)

        // THEN - Artist was removed from db
        loadedArtist.collectValue {
            expect(it).toBe(null)
        }
    }
}