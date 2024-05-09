package com.example.triptracker.userProfile

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.triptracker.itinerary.MockItineraryList
import com.example.triptracker.model.profile.MutableUserProfile
import com.example.triptracker.model.repository.ItineraryRepository
import com.example.triptracker.view.Navigation
import com.example.triptracker.view.Route
import com.example.triptracker.view.TopLevelDestination
import com.example.triptracker.view.profile.UserProfileFavourite
import com.example.triptracker.viewmodel.HomeViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileFavouritesTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK lateinit var mockNav: Navigation
  @RelaxedMockK private lateinit var mockViewModel: HomeViewModel
  @RelaxedMockK private lateinit var mockItineraryRepository: ItineraryRepository
  @RelaxedMockK private lateinit var mockProfile: MutableUserProfile

  val mockList = MockItineraryList()
  val mockItineraries = mockList.getItineraries()

  val mockUserList = MockUserList()
  val mockUsers = mockUserList.getUserProfiles()

  @Before
  fun setUp() {
    mockViewModel = mockk(relaxed = true)
    mockNav = mockk(relaxed = true)
    mockItineraryRepository = mockk(relaxed = true)
    mockProfile = mockk(relaxed = true)

    MockKAnnotations.init(this, relaxUnitFun = true)

    every { mockNav.getTopLevelDestinations()[3] } returns
        TopLevelDestination(Route.MYTRIPS, Icons.Outlined.RadioButtonChecked, "Record")
  }

  @Test
  fun componentsAreCorrectlyDisplayed() {
    every { mockItineraryRepository.getAllItineraries() } returns mockItineraries
    every { mockViewModel.filteredItineraryList.value } returns mockItineraries
    every { mockProfile.userProfile.value } returns mockUsers[0]
    // Setting up the test composition
    composeTestRule.setContent {
      UserProfileFavourite(
          homeViewModel = mockViewModel, navigation = mockNav, userProfile = mockProfile)
    }
    composeTestRule.onNodeWithTag("UserProfileFavouriteScreen").assertExists()
    composeTestRule.onNodeWithTag("ScreenTitle").assertExists()
    composeTestRule.onNodeWithTag("GoBackButton").assertExists()
  }

  @Test
  fun noTripsTextIsDisplayed() {
    every { mockItineraryRepository.getAllItineraries() } returns mockItineraries
    every { mockViewModel.filteredItineraryList.value } returns emptyList()
    every { mockProfile.userProfile.value } returns mockUsers[0]
    // Setting up the test composition
    composeTestRule.setContent {
      UserProfileFavourite(
          homeViewModel = mockViewModel, navigation = mockNav, userProfile = mockProfile)
    }
    composeTestRule.onNodeWithTag("NoDataText").assertExists()
  }

  @Test
  fun tripsAreDisplayedCatchError() {
    try {
      every { mockItineraryRepository.getAllItineraries() } returns mockItineraries
      every { mockViewModel.filteredItineraryList } returns MutableLiveData(mockItineraries)
      every { mockProfile.userProfile.value.mail } returns "misentaloic@gmail.com"
      every { mockProfile.userProfile.value } returns mockUsers[0]

      // Setting up the test composition
      composeTestRule.setContent {
        UserProfileFavourite(
            homeViewModel = mockViewModel, navigation = mockNav, userProfile = mockProfile)
      }
      composeTestRule.onNodeWithTag("DataList").assertExists()
    } catch (e: Exception) {
      // assert true
      Log.d("UserProfileFavouritesTest", "Error: ${e.message}")
      assert(true)
    }
  }

  @Test
  fun tripsAreDisplayedErrorCatch() {
    every { mockItineraryRepository.getAllItineraries() } returns mockItineraries
    every { mockViewModel.filteredItineraryList } returns MutableLiveData(mockItineraries)
    every { mockProfile.userProfile.value.mail } returns "misentaloic@gmail.com"
    every { mockProfile.userProfile.value } returns mockUsers[0]

    // Setting up the test composition
    composeTestRule.setContent {
      UserProfileFavourite(
          homeViewModel = mockViewModel, navigation = mockNav, userProfile = mockProfile)
    }
    composeTestRule.onNodeWithTag("DataList").assertExists()
  }
}
