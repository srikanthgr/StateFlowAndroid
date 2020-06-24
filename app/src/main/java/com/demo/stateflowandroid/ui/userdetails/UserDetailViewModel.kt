package com.demo.stateflowandroid.ui.userdetails

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.stateflowandroid.data.repository.UserRepository
import com.demo.stateflowandroid.domain.UserDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class UserDetailViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    //A better approach will be maintain a state
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: MutableStateFlow<UserDetails?> = _userDetails

    private val _isError = MutableStateFlow(false)
    val error : StateFlow<Boolean> = _isError

    fun lookupUser(login: String) {
        viewModelScope.launch {
            userRepository.getUserDetails(login)
                .collect { result ->
                    when {
                        result.isSuccess -> {
                            _userDetails.value = result.getOrNull().also { data ->
                                Timber.i("success getting user details: $data")
                            }
                        }
                        result.isFailure -> {
                            Timber.e(result.exceptionOrNull(), "error getting user details")
                            _isError.value = true
                        }
                    }
                }
        }
    }
}
