package com.example.triptracker.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.triptracker.model.location.popupState
import com.example.triptracker.model.profile.MutableUserProfile
import com.example.triptracker.navigation.AllowLocationPermission
import com.example.triptracker.navigation.checkForLocationPermission
import com.example.triptracker.navigation.getCurrentLocation
import com.example.triptracker.view.Navigation
import com.example.triptracker.view.NavigationBar
import com.example.triptracker.view.home.DisplayItinerary
import com.example.triptracker.view.theme.Montserrat
import com.example.triptracker.view.theme.md_theme_grey
import com.example.triptracker.view.theme.md_theme_light_black
import com.example.triptracker.view.theme.md_theme_light_dark
import com.example.triptracker.view.theme.md_theme_light_onPrimary
import com.example.triptracker.view.theme.md_theme_light_outline
import com.example.triptracker.view.theme.md_theme_orange
import com.example.triptracker.viewmodel.MapViewModel
import com.example.triptracker.viewmodel.UserProfileViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
/**
 * Composable displaying the map overview with all the paths and markers of trips that are around
 * the user's location. Needs the context of the app in order to ask for location permission and
 * access location.
 *
 * @param mapViewModel: The view model of the map (optional)
 * @param context: The context of the app (needed for location permission and real time location)
 * @param navigation: The app navigation instance
 * @param checkLocationPermission: Boolean whether to check or not the location permission for tests
 *   purposes (optional)
 */
fun MapOverview(
    mapViewModel: MapViewModel = viewModel(),
    context: Context,
    navigation: Navigation,
    checkLocationPermission: Boolean = true, // Default value true, can be overridden during tests
    selectedId: String = "",
    userProfile: MutableUserProfile
) {
  var mapProperties by remember {
    mutableStateOf(
        MapProperties(
            mapType = MapType.NORMAL, isMyLocationEnabled = checkForLocationPermission(context)))
  }

  var uiSettings by remember {
    mutableStateOf(MapUiSettings(myLocationButtonEnabled = checkForLocationPermission(context)))
  }

  var loadMapScreen by remember {
    mutableStateOf(if (checkLocationPermission) checkForLocationPermission(context) else false)
  }

  // Check if the location permission is granted if not re-ask for it. If the result is still
  // negative then disable the location button and center the view on EPFL
  // else enable the location button and center the view on the user's location and show real time
  // location
  when (loadMapScreen) {
    true -> {
      Scaffold(
          bottomBar = { NavigationBar(navigation) }, modifier = Modifier.testTag("MapOverview")) {
              innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
              Map(mapViewModel, context, mapProperties, uiSettings, selectedId, userProfile)
            }
          }
    }
    false -> {
      AllowLocationPermission(
          onPermissionGranted = {
            mapProperties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true)
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            loadMapScreen = true
          },
          onPermissionDenied = {
            mapProperties = MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false)
            uiSettings = MapUiSettings(myLocationButtonEnabled = false)
            loadMapScreen = true
          })
    }
  }
}

/**
 * Composable displaying the map with the user's location and the city name on the top bar.
 *
 * @param mapViewModel: The view model of the map
 * @param context: The context of the app (needed for location permission and real time location)
 * @param mapProperties: The properties of the map (type, location enabled)
 * @param uiSettings: The settings of the map (location button enabled)
 * @param currentSelectedId: The id of the selected path
 */
@Composable
fun Map(
    mapViewModel: MapViewModel,
    context: Context,
    mapProperties: MapProperties,
    uiSettings: MapUiSettings,
    currentSelectedId: String,
    userProfile: MutableUserProfile
) {
  // Used to display the gradient with the top bar and the changing city location
  val ui by remember { mutableStateOf(uiSettings) }
  val properties by remember { mutableStateOf(mapProperties) }
  var deviceLocation by remember { mutableStateOf(DEFAULT_LOCATION) }
  val coroutineScope = rememberCoroutineScope()

  // var mapPopupState by remember { mutableStateOf(mapViewModel.popUpState) }
  val pathList by mapViewModel.pathList.observeAsState()

  val cameraPositionState = rememberCameraPositionState {
    if (currentSelectedId == "") {
      getCurrentLocation(
          context = context,
          onLocationFetched = {
            deviceLocation = it
            position = CameraPosition.fromLatLngZoom(deviceLocation, 17f)
          })
    }
  }
  var visibleRegion: VisibleRegion?

  // val displayPopUp by remember { mutableStateOf(mapViewModel.displayPopUp) }
  // var displayPicturesPopUp by remember { mutableStateOf(mapViewModel.displayPicturePopUp) }

  var selectedPolyline by remember { mapViewModel.selectedPolylineState }

  var showCancelDialog by remember { mutableStateOf(false) }

  // When the camera is moving, the city name is updated in the top bar with geo decoding
  LaunchedEffect(cameraPositionState.isMoving) {
    mapViewModel.reverseDecode(
        cameraPositionState.position.target.latitude.toFloat(),
        cameraPositionState.position.target.longitude.toFloat())
    // Get the visible region of the map
    visibleRegion = cameraPositionState.projection?.visibleRegion
    // Get the filtered paths based on the visible region of the map asynchronously
    mapViewModel.getFilteredPaths(visibleRegion?.latLngBounds)
  }

  // Fetch the device location when the composable is launched
  LaunchedEffect(Unit) {
    getCurrentLocation(
        context = context,
        onLocationFetched = {
          if (currentSelectedId == "") {
            deviceLocation = it
            cameraPositionState.position = CameraPosition.fromLatLngZoom(deviceLocation, 17f)
          }
        })
  }

  // Get the path list from the view model and trigger the launch effect when the path list is
  // updated
  LaunchedEffect(pathList) {
    if (currentSelectedId != "") {
      // fetch the selected path
      Log.d("MapOverviewPRINT", pathList?.size().toString())
      if (pathList != null) {
        val selection = mapViewModel.getPathById(pathList!!, currentSelectedId)
        if (selection != null) {

          // center the camera on the selected path
          cameraPositionState.position =
              CameraPosition.fromLatLngZoom(
                  LatLng(selection.location.latitude, selection.location.longitude), 17f)

          // set the selected polyline
          mapViewModel.selectedPolylineState.value =
              MapViewModel.SelectedPolyline(selection, selection.route[0])
          selectedPolyline = mapViewModel.selectedPolylineState.value

          // display the path popup
          mapViewModel.displayPopUp.value = true
        }
      }
    }
  }

  // Displays the map
  Box() {
    Box(modifier = Modifier.fillMaxSize()) {
      GoogleMap(
          modifier =
              Modifier.matchParentSize()
                  .background(
                      brush =
                          Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
                  .testTag("Map"),
          cameraPositionState = cameraPositionState,
          properties = properties,
          uiSettings = ui,
          onMapClick = {
            if (!mapViewModel.asStartItinerary.value) {
              selectedPolyline = mapViewModel.DUMMY_SELECTED_POLYLINE
              mapViewModel.displayPopUp.value = false
              mapViewModel.displayPicturePopUp.value = false
            } else {
              if (mapViewModel.displayPicturePopUp.value) {
                mapViewModel.displayPicturePopUp.value = false
                mapViewModel.displayPopUp.value = true
                mapViewModel.popUpState.value = popupState.PATHOVERLAY
              }
            }
          },
          onMapLoaded = {
            // decode the city name and update the top bar
            mapViewModel.reverseDecode(
                cameraPositionState.position.target.latitude.toFloat(),
                cameraPositionState.position.target.longitude.toFloat())
            visibleRegion = cameraPositionState.projection?.visibleRegion
            mapViewModel.getFilteredPaths(visibleRegion?.latLngBounds)
          }) {
            // Display the path of the trips on the map only when they enter the screen
            mapViewModel.filteredPathList.value?.forEach { (location, latLngList) ->
              // Check if the polyline is selected
              val isSelected = selectedPolyline?.itinerary?.id == location.id
              // Display the pat polyline
              Polyline(
                  points = latLngList,
                  clickable = true,
                  color = md_theme_orange,
                  width = if (isSelected) 25f else 15f,
                  onClick = {
                    if (!mapViewModel.asStartItinerary.value) {
                      selectedPolyline = MapViewModel.SelectedPolyline(location, latLngList[0])
                      mapViewModel.popUpState.value = popupState.DISPLAYITINERARY
                      mapViewModel.displayPopUp.value = true
                    }
                  })

              // Display the start marker of the polyline and a thicker path when selected
              if (isSelected) {
                if (selectedPolyline!!.itinerary.route.isNotEmpty()) {
                  val startMarkerState =
                      rememberMarkerState(position = selectedPolyline!!.itinerary.route[0])
                  MarkerComposable(state = startMarkerState) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = "Start Location",
                        tint = md_theme_light_black)
                  }
                }

                selectedPolyline!!.itinerary.pinnedPlaces.forEach { pin ->
                  val markerState =
                      rememberMarkerState(position = LatLng(pin.latitude, pin.longitude))
                  MarkerComposable(
                      state = markerState,
                      onClick = {
                        // Display the pin information
                        mapViewModel.selectedPin.value = pin

                        mapViewModel.displayPicturePopUp.value = true

                        mapViewModel.displayPopUp.value = false

                        true
                      }) {
                        Icon(
                            imageVector = Icons.Outlined.PinDrop,
                            contentDescription = "Add Picture",
                            tint = md_theme_light_black)
                      }
                }
              }
            }
          }
    }
    if (showCancelDialog) {
      AlertDialog(
          shape = RoundedCornerShape(15.dp),
          modifier = Modifier.align(Alignment.Center),
          onDismissRequest = { showCancelDialog = false },
          title = {
            Text(
                text = "Cancel Itinerary",
                fontSize = 20.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                color = md_theme_light_onPrimary)
          },
          text = {
            Text(
                text = "Are you sure you want to cancel the itinerary?",
                fontSize = 16.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                color = md_theme_light_outline)
          },
          confirmButton = {
            Button(
                modifier = Modifier.testTag("YesCancelItineraryButton"),
                onClick = {
                  mapViewModel.asStartItinerary.value = false
                  mapViewModel.displayPopUp.value = true
                  mapViewModel.displayPicturePopUp.value = false
                  mapViewModel.popUpState.value = popupState.DISPLAYITINERARY
                  showCancelDialog = false
                },
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = md_theme_light_dark,
                        contentColor = md_theme_light_onPrimary),
                shape = RoundedCornerShape(35.dp),
            ) {
              Text(
                  text = "Yes",
                  fontSize = 14.sp,
                  fontFamily = Montserrat,
                  fontWeight = FontWeight.SemiBold,
                  color = md_theme_light_onPrimary)
            }
          },
          dismissButton = {
            Button(
                modifier = Modifier.testTag("NoCancelItineraryButton"),
                onClick = { showCancelDialog = false },
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = md_theme_light_black, contentColor = Color.White),
                shape = RoundedCornerShape(35.dp)) {
                  Text(
                      text = "No",
                      fontSize = 14.sp,
                      fontFamily = Montserrat,
                      fontWeight = FontWeight.SemiBold,
                      color = md_theme_light_onPrimary)
                }
          })
    }

    Box(modifier = Modifier.matchParentSize().background(gradient).align(Alignment.TopCenter)) {
      Row(
          modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween) {
            if (mapViewModel.asStartItinerary.value) {
              IconButton(
                  onClick = { showCancelDialog = true },
                  modifier = Modifier.testTag("CancelItineraryButton").size(50.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancel Itinerary",
                        tint = md_theme_light_dark)
                  }
            } else {
              Spacer(modifier = Modifier.width(50.dp))
            }
            Text(
                text = mapViewModel.cityNameState.value,
                modifier = Modifier.padding(30.dp),
                fontSize = 24.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                color = md_theme_light_dark)
            Spacer(modifier = Modifier.width(50.dp))
          }
    }
    Row(
        modifier = Modifier.align(Alignment.BottomStart),
        horizontalArrangement = Arrangement.Start) {
          Box(modifier = Modifier.padding(horizontal = 35.dp, vertical = 65.dp)) {
            if (ui.myLocationButtonEnabled &&
                properties.isMyLocationEnabled &&
                !mapViewModel.displayPopUp.value &&
                !mapViewModel.displayPicturePopUp.value) {
              DisplayCenterLocationButton(
                  coroutineScope = coroutineScope,
                  deviceLocation = deviceLocation,
                  cameraPositionState = cameraPositionState) {
                    getCurrentLocation(
                        context = context,
                        onLocationFetched = {
                          deviceLocation = it
                          cameraPositionState.position =
                              CameraPosition.fromLatLngZoom(deviceLocation, 17f)
                        })
                  }
            }
          }
        }
    if (mapViewModel.displayPopUp.value) {

      if (mapViewModel.selectedPolylineState.value != null) {
        // Display the itinerary of the selected polyline
        // (only when the polyline is selected)
        when (mapViewModel.popUpState.value) {
          popupState.DISPLAYITINERARY -> {
            Box(
                modifier =
                    Modifier.fillMaxHeight(0.3f).fillMaxWidth().align(Alignment.BottomCenter)) {
                  DisplayItinerary(
                      itinerary = mapViewModel.selectedPolylineState.value!!.itinerary,
                      onClick = { mapViewModel.popUpState.value = popupState.DISPLAYPIN },
                      test = false,
                  )
                }
          }
          popupState.DISPLAYPIN -> {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth().align(Alignment.BottomCenter)) {
              StartScreen(
                  itinerary = mapViewModel.selectedPolylineState.value!!.itinerary,
                  userProfileViewModel = UserProfileViewModel(),
                  userProfile = userProfile,
                  onClick = { mapViewModel.popUpState.value = popupState.PATHOVERLAY },
                  mapViewModel = mapViewModel)
            }
          }
          popupState.PATHOVERLAY -> {
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
              PathOverlaySheet(
                  itinerary = mapViewModel.selectedPolylineState.value!!.itinerary,
                  onClick = {
                    mapViewModel.popUpState.value = popupState.DISPLAYITINERARY
                    mapViewModel.displayPopUp.value = false
                    mapViewModel.displayPicturePopUp.value = true
                    mapViewModel.selectedPin.value = it
                  })
            }
          }
        }
      }
    }
    AnimatedVisibility(
        visible = mapViewModel.displayPicturePopUp.value,
        enter =
            fadeIn() + expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(100)),
        exit =
            fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(100)),
        modifier = Modifier.align(Alignment.BottomCenter)) {
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .fillMaxHeight(0.4f)
                      .align(Alignment.BottomCenter)
                      .background(
                          color = md_theme_light_black,
                          shape = RoundedCornerShape(topEnd = 35.dp, topStart = 35.dp))) {
                // Display the pictures of the selected pin
                // (only when the pin is selected)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Column(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(start = 30.dp, top = 10.dp)
                                  .verticalScroll(rememberScrollState()),
                          verticalArrangement = Arrangement.Center,
                          horizontalAlignment = Alignment.Start) {
                            Text(
                                text = mapViewModel.selectedPin.value?.description ?: "",
                                modifier = Modifier.padding(vertical = 10.dp),
                                fontSize = 20.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                color = md_theme_light_onPrimary)
                            Text(
                                text = mapViewModel.selectedPin.value?.name ?: "",
                                modifier = Modifier.padding(vertical = 10.dp),
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                color = md_theme_light_outline)
                          }
                    }

                val selectedPin = mapViewModel.selectedPin.value
                val scrollState = rememberScrollState()

                Row(
                    modifier =
                        Modifier.fillMaxSize()
                            .horizontalScroll(scrollState)
                            .align(Alignment.BottomStart)
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom) {
                      if (selectedPin?.image_url?.isEmpty() == true) {
                        Log.e("MapOverview", "No images available")
                        Text(
                            text = "No images available",
                            fontSize = 20.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Light,
                            color = md_theme_grey)
                      } else {
                        selectedPin?.image_url?.forEach { url ->
                          AsyncImage(
                              modifier =
                                  Modifier.clip(shape = RoundedCornerShape(20.dp))
                                      .height(200.dp)
                                      .padding(horizontal = 2.dp),
                              model = url,
                              contentDescription = "Image",
                          )
                        }
                      }
                    }
              }
        }
  }
}
