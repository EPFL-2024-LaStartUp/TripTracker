package com.example.triptracker.view.profile.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triptracker.R
import com.example.triptracker.view.theme.md_theme_grey
import com.example.triptracker.viewmodel.UserProfileViewModel

/** This composable function displays the friend search bar */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSearchBar(viewModel: UserProfileViewModel, onSearchActivated: (Boolean) -> Unit) {
  var searchText by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current
  // If the search bar is active (in focus or contains text), we'll consider it active.
  var isActive by remember { mutableStateOf(false) }

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .wrapContentHeight()
              .padding(horizontal = 30.dp)
              .background(
                  color = Color.LightGray.copy(alpha = 0.3f),
                  shape = MaterialTheme.shapes.small.copy(CornerSize(50))),
      contentAlignment = Alignment.Center
  ) {
        TextField(
            value = searchText,
            onValueChange = { newText ->
              searchText = newText
              isActive = newText.isNotEmpty()
              viewModel.setSearchQuery(newText)
              onSearchActivated(isActive)
            },
            placeholder = {
              Text(
                  "Find Friends",
                  modifier = Modifier
                      .testTag("SearchBarText"),
                  textAlign = TextAlign.Start,
                  fontFamily = FontFamily(Font(R.font.montserrat_bold)),
                  fontSize = (LocalConfiguration.current.screenHeightDp * 0.025f).sp,
                  fontWeight = FontWeight.Medium,
                  letterSpacing = 0.15.sp,
                  color = md_theme_grey,
              )},
            leadingIcon = {
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  modifier = Modifier.testTag("SearchIcon"),
                  tint = if (isActive) Color.DarkGray else Color.Gray)
            },
            trailingIcon = {
              if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = {
                      searchText = ""
                      viewModel.setSearchQuery("")
                    },
                    modifier = Modifier.testTag("ClearButton")) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Clear",
                          tint = Color.Gray)
                    }
              }
            },
            singleLine = true,
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                      focusManager.clearFocus()
                      onSearchActivated(false)
                    }),
            modifier = Modifier
                .fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp * 0.09f).dp)
                .padding(horizontal = 12.dp)
                .testTag("SearchBar"))
      }
}
