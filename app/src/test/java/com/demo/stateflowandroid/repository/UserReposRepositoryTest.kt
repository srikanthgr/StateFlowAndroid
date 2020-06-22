package com.demo.stateflowandroid.repository

import com.demo.stateflowandroid.data.repository.UserReposRepository
import com.demo.stateflowandroid.data.service.ApiService
import com.demo.stateflowandroid.domain.Repo
import com.demo.stateflowandroid.domain.RepoOwner
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"
private const val TEST_AVATAR_URL = "someAvatarUrl"

class UserReposRepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `should get user repos on success`() = testDispatcher.runBlockingTest {
        // GIVEN
        val repoOwner = RepoOwner(TEST_USERNAME, TEST_AVATAR_URL)
        val repo1 = Repo(name = "someRepo1", owner = repoOwner, stars = 10)
        val repo2 = Repo(name = "someRepo2", owner = repoOwner, stars = 55)
        val expectedRepoList = listOf(repo1, repo2)

        val apiService = mock<ApiService> {
            onBlocking { getUserRepos(TEST_USERNAME) } doReturn expectedRepoList
        }

        val repository = UserReposRepository(apiService, testDispatcher)

        // WHEN
        val flow = repository.getUserRepos(TEST_USERNAME)

        // THEN
        flow.collect { actualRepoList: List<Repo> ->
            actualRepoList.shouldBeEqualTo(expectedRepoList)
        }
    }

    @Test(expected = IOException::class)
    fun `should throw error for user repos if any exception is thrown`() =
        testDispatcher.runBlockingTest {
            // GIVEN
            val apiService = mock<ApiService> {
                onBlocking { getUserRepos(TEST_USERNAME) } doAnswer {
                    throw IOException()
                }
            }

            val repository = UserReposRepository(apiService, testDispatcher)

            // WHEN
            val flow = repository.getUserRepos(TEST_USERNAME)

            // THEN
            flow.collect { actualRepoList: List<Repo> ->
                actualRepoList.shouldBeEmpty()
            }
        }
}
