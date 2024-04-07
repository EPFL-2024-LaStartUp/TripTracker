package com.example.triptracker.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.triptracker.model.itinerary.Itinerary
import com.example.triptracker.model.itinerary.ItineraryList
import com.example.triptracker.model.repository.ItineraryRepository

/**
 * ViewModel for the Home Screen. Fetches all itineraries from the repository stores them in a
 * LiveData object
 */
class HomeViewModel : ViewModel() {
  private val repository = ItineraryRepository()

  private var itineraryInstance = ItineraryList(listOf())
  private var _itineraryList = MutableLiveData<List<Itinerary>>()
  val itineraryList: LiveData<List<Itinerary>> = _itineraryList

  private var _pinNamesMap =
      MutableLiveData<Map<String, List<String>>>() // Map of itinerary ID to list of pin names
  val pinNamesMap: LiveData<Map<String, List<String>>> = _pinNamesMap

  // Fetch all itineraries from the repository on initialization
  init {
    fetchItineraries()
  }

  /** Fetches all itineraries from the repository and stores them in the itineraryList LiveData */
  private fun fetchItineraries() {
    itineraryInstance.itineraryList =
        repository.getAllItineraries(callback = { itineraryList -> fetchPinNames(itineraryList) })
    _itineraryList.value = itineraryInstance.itineraryList
  }

  /**
   * Fetches the names of all pinned places for each itinerary in the list
   *
   * @param itineraries List of itineraries
   */
  private fun fetchPinNames(itineraries: List<Itinerary>) {
    val pinNamesMap = mutableMapOf<String, List<String>>()

    for (itinerary in itineraries) {
      Log.d("TypeOfPin", itinerary.pinnedPlaces.javaClass.simpleName)
      val pinNames = repository.getPinNames(itinerary)
      Log.d("pinNamesMap", "Fetched pin names for itinerary: $pinNames")
      pinNamesMap[itinerary.id] = pinNames
    }
    _pinNamesMap.postValue(pinNamesMap)
  }
}
