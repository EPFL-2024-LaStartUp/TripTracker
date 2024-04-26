package com.example.triptracker.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptracker.model.profile.UserProfile
import com.example.triptracker.model.repository.UserProfileRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the UserProfile class This class is responsible for handling the data operations
 * for the UserProfile class
 */
class UserProfileViewModel(
    private val userProfileRepository: UserProfileRepository = UserProfileRepository()
) : ViewModel() {

  private var _userProfileList = MutableLiveData<List<UserProfile>>()
  private val userProfileList: LiveData<List<UserProfile>> = _userProfileList

  private val _currentUserProfile = MutableLiveData<UserProfile?>()
  val currentUserProfile: LiveData<UserProfile?> = _currentUserProfile

  init {
    fetchAllUserProfiles()
  }

  private fun fetchAllUserProfiles() {
    viewModelScope.launch {
      userProfileRepository.getAllUserProfiles { profiles ->
        Log.d("ProfileViewModel", "Fetched all user profiles $profiles")
        _userProfileList.value = profiles // Correctly use postValue to update LiveData
      }
    }
  }

  fun getUserProfile(email: String): UserProfile? {
    viewModelScope.launch {
      userProfileRepository.getUserProfileByEmail(email) { userProfile ->
        _currentUserProfile.postValue(userProfile)
      }
    }
    return _currentUserProfile.value
  }

  /**
   * This function adds a new user profile to the database.
   *
   * @param userProfile : user profile to add
   */
  fun addNewUserProfileToDb(userProfile: UserProfile) {
    userProfileRepository.addNewUserProfile(userProfile)
  }

  /**
   * This function updates a user profile from the database.
   *
   * @param userProfile : user profile to update
   */
  fun updateUserProfileInDb(userProfile: UserProfile) {
    userProfileRepository.updateUserProfile(userProfile)
  }

  /**
   * This function adds new followers to the user profile in the database.
   *
   * @param userProfile : user profile to update
   * @param follower : follower to add
   */
  fun addFollowersInDb(userProfile: UserProfile, follower: UserProfile) {
    val updatedProfile = userProfile.copy(followers = userProfile.followers + follower)
    userProfileRepository.updateUserProfile(updatedProfile)

    val updatedFollowerProfile = follower.copy(following = follower.following + userProfile)
    userProfileRepository.updateUserProfile(updatedFollowerProfile)
  }

  /**
   * This function adds new following to the user profile in the database.
   *
   * @param userProfile : user profile to update
   * @param following : following to add
   */
  fun addFollowingInDb(userProfile: UserProfile, following: UserProfile) {
    val updatedProfile = userProfile.copy(following = userProfile.following + following)
    userProfileRepository.updateUserProfile(updatedProfile)

    val updatedFollowingProfile = following.copy(followers = following.followers + userProfile)
    userProfileRepository.updateUserProfile(updatedFollowingProfile)
  }

  /**
   * This function removes a follower from the user profile in the database.
   *
   * @param userProfile : user profile to update
   * @param follower : follower to remove
   */
  fun removeFollowerInDb(userProfile: UserProfile, follower: UserProfile) {
    val updatedProfile =
        userProfile.copy(followers = userProfile.followers.filter { it.mail != follower.mail })
    userProfileRepository.updateUserProfile(updatedProfile)

    val updatedFollowerProfile =
        follower.copy(following = follower.following.filter { it.mail != userProfile.mail })
    userProfileRepository.updateUserProfile(updatedFollowerProfile)
  }

  /**
   * This function removes a following from the user profile in the database.
   *
   * @param userProfile : user profile to update
   * @param following : following to remove
   */
  fun removeFollowingInDb(userProfile: UserProfile, following: UserProfile) {
    val updatedProfile =
        userProfile.copy(following = userProfile.following.filter { it.mail != following.mail })
    userProfileRepository.updateUserProfile(updatedProfile)

    val updatedFollowingProfile =
        following.copy(followers = following.followers.filter { it.mail != userProfile.mail })
    userProfileRepository.updateUserProfile(updatedFollowingProfile)
  }

  /**
   * This function removes the user profile matching the id from the database.
   *
   * @param mail : mail of the user profile to remove
   */
  fun removeUserProfileInDb(mail: String) {
    userProfileRepository.removeUserProfile(mail)
  }
}
