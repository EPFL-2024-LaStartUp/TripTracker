package com.example.triptracker.model.repository

import android.util.Log
import com.example.triptracker.model.itinerary.Itinerary
import com.example.triptracker.model.location.Location
import com.example.triptracker.model.location.Pin
import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

/**
 * Repository for the Itinerary class This class is responsible for handling the data operations for
 * the Itinerary class It interacts with the Firebase Firestore to save, update, delete and retrieve
 * the itinerary data$
 */
open class ItineraryRepository {

  // Initialise the Firebase Firestore
  val db = Firebase.firestore

  // Reference to the collection of itineraries
  val itineraryDb = db.collection("itineraries")

  // List of itineraries
  private var _itineraryList = mutableListOf<Itinerary>()

  // A tag for logging
  private val TAG = "FirebaseConnection - ItineraryRepository"

  // Get a unique ID for the itinerary
  fun getUID(): String {
    return db.collection("itineraryId").document().id // check this
  }

  // Get all itineraries from the database
  open fun getAllItineraries(callback: (List<Itinerary>) -> Unit): List<Itinerary> {
    db.collection("itineraries")
        .get()
        .addOnSuccessListener { result ->
          itineraryList(result)
          callback(_itineraryList)
        }
        .addOnFailureListener { e -> Log.e(TAG, "Error getting all itineraries", e) }
    return _itineraryList
  }

  open fun getPinNames(itinerary: Itinerary): List<String> {
    val pinNames = mutableListOf<String>()
    for (pin in itinerary.pinnedPlaces) {
      if (pin is Pin) {
        pinNames.add(pin.name)
      } else {
        Log.e("Error", "Expected item to be Pin, but got ${pin.javaClass.simpleName}")
      }
    }
    Log.d("PinNames", pinNames.toString())
    return pinNames
  }

  // Convert the query snapshot to a list of itineraries
  private fun itineraryList(taskSnapshot: QuerySnapshot) {
    for (document in taskSnapshot) {
      val locationData = document.data["location"] as Map<*, *>
      val location =
          Location(
              locationData["latitude"] as Double,
              locationData["longitude"] as Double,
              locationData["name"] as String)
      val pinnedPlacesData = document.data["pinnedPlaces"] as? List<Map<String, Any>> ?: emptyList()
      val pinnedPlaces: List<Pin> =
          pinnedPlacesData.map { pinData ->
            // Assuming Pin class has a constructor that takes these fields:
            Pin(
                pinData["latitude"] as? Double ?: 0.0,
                pinData["longitude"] as? Double ?: 0.0,
                pinData["name"] as? String ?: "",
                pinData["description"] as? String ?: "",
                pinData["image-url"] as? String ?: "")
          }

      val itinerary =
          Itinerary(
              id = document.id,
              title = document.getString("title") ?: "",
              username = document.getString("username") ?: "",
              location = location,
              flameCount = document.getLong("flameCount") ?: 0L,
              startDateAndTime = document.getString("startDateAndTime") ?: "",
              endDateAndTime = document.getString("endDateAndTime") ?: "",
              pinnedPlaces = pinnedPlaces,
              description = document.getString("description") ?: "")

      _itineraryList.add(itinerary)
    }
  }

  // Update an itinerary in the database
  fun updateItinerary(itinerary: Itinerary) {
    db.collection("itineraries")
        .document(itinerary.id)
        .set(itinerary)
        .addOnSuccessListener { Log.d(TAG, "Itinerary updated successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error updating itinerary", e) }
  }

  // Add a new itinerary to the database
  fun addNewItinerary(itinerary: Itinerary) {
    db.collection("itineraries")
        .document(itinerary.id)
        .set(itinerary)
        .addOnSuccessListener { Log.d(TAG, "Itinerary added successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error adding new itinerary", e) }
  }

  // Remove an itinerary from the database
  fun removeItinerary(id: String) {
    db.collection("itineraries")
        .document(id)
        .delete()
        .addOnSuccessListener { Log.d(TAG, "Itinerary removed successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error removing itinerary", e) }
  }
}
