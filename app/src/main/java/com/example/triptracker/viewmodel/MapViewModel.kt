package com.example.triptracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.triptracker.model.geocoder.NominatimApi
import com.example.triptracker.model.itinerary.Itinerary
import com.example.triptracker.model.itinerary.ItineraryList
import com.example.triptracker.model.location.Location
import com.example.triptracker.model.location.Pin
import com.example.triptracker.model.repository.ItineraryRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch

/**
 * ViewModel for the MapOverview composable. It contains the city name state and the geocoder to
 * reverse decode the location.
 *
 * @param geocoder: Geocoder with Nominatim API that allows to reverse decode the location
 *   (optional)
 * @param pathList: Mutable list of that contains all the itineraries (optional)
 * @param repository: Repository containing the itineraries for Firebase interactions (optional)
 * @param filteredPathList: Mutable map of Itinerary linked to a list of coordinates for filtering
 *   purposes (optional)
 */
class MapViewModel(
    val geocoder: NominatimApi = NominatimApi(),
    val pathList: MutableLiveData<ItineraryList> = MutableLiveData<ItineraryList>(),
    private val repository: ItineraryRepository = ItineraryRepository(),
    val filteredPathList: MutableLiveData<Map<Itinerary, List<LatLng>>> =
        MutableLiveData<Map<Itinerary, List<LatLng>>>()
) : ViewModel() {

  val DUMMY_SELECTED_POLYLINE =
      SelectedPolyline(
          Itinerary("", "", "", Location(0.0, 0.0, ""), 0, "", "", emptyList(), "", emptyList()),
          LatLng(0.0, 0.0))

  // state for the city name displayed at the top of the screen
  val cityNameState = mutableStateOf("")

  /** Data class describing a selected Polyline */
  data class SelectedPolyline(val itinerary: Itinerary, val startLocation: LatLng)

  // Hold the selected polyline state
  var selectedPolylineState = mutableStateOf<SelectedPolyline?>(null)

  var selectedPin = mutableStateOf<Pin?>(null)

  var displayPopUp = mutableStateOf(false)

  var displayPicturesPopUp = mutableStateOf(false)

  init {
    viewModelScope.launch { getAllItineraries() }
  }

  /**
   * Reverse decodes the location to get the city name. On success update the cityNameState at the
   * top of the screen
   *
   * @param lat : latitude of the location
   * @param lon : longitude of the location
   */
  fun reverseDecode(lat: Float, lon: Float, _geocoder: NominatimApi = geocoder) {
    _geocoder.getCity(lat, lon) { cityName -> cityNameState.value = cityName }
  }

  /** Get all itineraries from the database and update the pathList */
  private fun getAllItineraries() {
    pathList.postValue(ItineraryList(repository.getAllItineraries()))
  }

  /**
   * Get all paths from the pathList in the format of the title of the itinerary and the route
   *
   * @return a map of the title of the itinerary and the route
   */
  fun getAllPaths(_pathList: MutableLiveData<ItineraryList> = pathList): Map<String, List<LatLng>> {
    return _pathList.value?.getAllItineraries()?.map { it.title to it.route }?.toMap() ?: emptyMap()
  }

  /**
   * Get the filtered paths based on the visible region of the map
   *
   * @param latLngBounds : the visible region of the map
   */
  fun getFilteredPaths(
      latLngBounds: LatLngBounds?,
      limit: Int = 10,
      _pathList: MutableLiveData<ItineraryList> = pathList
  ) {
    if (latLngBounds == null) {
      filteredPathList.value = emptyMap()
    } else {
      filteredPathList.postValue(
          _pathList.value
              ?.getFilteredItineraries(latLngBounds, limit)
              ?.map { it to it.route }
              ?.toMap() ?: emptyMap())
    }
  }

  fun getPathById(id: String, _pathList: MutableLiveData<ItineraryList> = pathList): Itinerary? {
    return _pathList.value?.getAllItineraries()?.find { it.id == id }
  }

  fun getPathByLatLng(
      latLng: LatLng,
      _pathList: MutableLiveData<ItineraryList> = pathList
  ): SelectedPolyline {
    //        filteredPathList.postValue(
    //            _pathList.value?.getAllItineraries()?.map { itinerary ->
    //                itinerary to itinerary.route
    //            }?.toMap() ?: emptyMap()
    //        )
    //        filteredPathList.value?.filter { (itinerary, route) ->
    //            val location = LatLng(itinerary.location.latitude, itinerary.location.longitude)
    //            Log.d("FOUNDRESULT", location.toString() + " " + latLng.toString())
    //            location == latLng
    //        }?.forEach { (itinerary, route) ->
    //            Log.d("FOUNDRESULT", "getPathByLatLng: $itinerary")
    //            selectedPolylineState.value = SelectedPolyline(itinerary, latLng)
    //            return SelectedPolyline(itinerary, latLng)
    //        }
    return DUMMY_SELECTED_POLYLINE
  }
}
