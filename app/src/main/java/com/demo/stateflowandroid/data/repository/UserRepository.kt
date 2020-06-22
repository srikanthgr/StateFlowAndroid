package com.demo.stateflowandroid.data.repository


import com.demo.stateflowandroid.data.service.ApiService
import com.demo.stateflowandroid.domain.UserDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

const val DELAY_ONE_SECOND = 1_000L

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val ioDispatcher: CoroutineDispatcher
) {

    fun getUserDetails(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(ioDispatcher)
    }

    fun getUserDetailsRetryIfFailed(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }.retry(retries = 2) { e ->
            (e is IOException).also {
                if (it) delay(DELAY_ONE_SECOND)
            }
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(ioDispatcher)
    }
}
