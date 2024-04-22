package com.example.triptracker.screens.userProfile

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

/** This class represents the UserProfileFollowing Screen and the elements it contains. */
class UserProfileFollowingScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<UserProfileFollowingScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("FollowingScreen") }) {

  // Structural elements of the UI
  val followingTitle: KNode = child { hasTestTag("FollowingTitle") }
  val goBackButton: KNode = child { hasTestTag("GoBackButton") }
  val removeButton: KNode = child { hasTestTag("RemoveButton") }
  val followingList: KNode = child { hasTestTag("FollowingList") }
  val followingProfile: KNode = child { hasTestTag("Friend") }
}
