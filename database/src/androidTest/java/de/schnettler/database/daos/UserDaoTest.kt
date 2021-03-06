package de.schnettler.database.daos

import ch.tutteli.atrium.api.fluent.en_GB.notToBe
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import de.schnettler.database.collectValue
import de.schnettler.database.models.User
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class UserDaoTest : DatabaseTest() {

    @Test
    fun getUserReturnsUser() = runBlockingTest {
        // GIVEN - a db with Multiple Users
        val users = generateUsers(3)
        db.userDao().insertAll(users)

        // WHEN - User requested from db
        val user1 = db.userDao().getUser(users[0].name)
        val user2 = db.userDao().getUser(users[1].name)
        val user3 = db.userDao().getUser(users[2].name)

        // THEN - the right user is returned
        user1.collectValue { expect(it).toBe(users[0]) }
        user2.collectValue { expect(it).toBe(users[1]) }
        user3.collectValue { expect(it).toBe(users[2]) }
    }

    @Test
    fun getUserReturnsNull() = runBlockingTest {
        // GIVEN - Database with 3 Artists
        db.userDao().insertAll(generateUsers(3))

        // WHEN - Requested user not in db
        val user = db.userDao().getUser("TestUser")

        // THEN - Returns null
        user.collectValue { expect(it).toBe(null) }
    }

    @Test
    fun updateArtistCountUpdatesCount() = runBlockingTest {
        // GIVEN - Database with one user
        val oldUser = generateUsers(1).first()
        db.userDao().insert(oldUser)

        // WHEN - artistcount is Updated
        val updatedUser = oldUser.copy(artistCount = 10)
        val changedRows =
            db.userDao().updateArtistCount(oldUser.name, updatedUser.artistCount)
        val loadedUser = db.userDao().getUser(oldUser.name)

        // THEN - update reflected in data from db
        expect(changedRows).toBe(1)
        loadedUser.collectValue {
            expect(it).toBe(updatedUser)
            expect(it).notToBe(oldUser)
        }
    }
}

fun generateUsers(num: Int): List<User> {
    val result = mutableListOf<User>()
    for (i in 0..num) {
        result.add(
            User(
                name = "User$i",
                playcount = 10L * i,
                url = "User${i}Url",
                realname = "User${i}Real",
                age = 10L * i + 10,
                registerDate = 1000L * i,
                countryCode = "DE",
                imageUrl = "User${i}Image",
                artistCount = 0
            )
        )
    }
    return result
}