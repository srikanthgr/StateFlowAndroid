package com.demo.stateflowandroid.data.repository


import com.demo.stateflowandroid.data.service.ApiService
import com.demo.stateflowandroid.di.IoDispatcher
import com.demo.stateflowandroid.domain.Repo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReposForQueryRepository @Inject constructor(
    private val apiService: ApiService,
   @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getReposForQuery(query: String): Flow<List<Repo>> {
        return flow {
            emit(apiService.getReposForQuery(query))
        }
            .flowOn(ioDispatcher)
    }
}
