package com.example.triptracker.model.repository

import android.util.Log
import com.example.triptracker.model.itinerary.Itinerary
import com.example.triptracker.model.location.Location
import com.example.triptracker.model.location.Pin
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
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

  /**
   * Get all itineraries from the database
   *
   * @return List of itineraries
   */
  open fun getAllItineraries(): List<Itinerary> {
    db.collection("itineraries")
        .get()
        .addOnSuccessListener { result -> itineraryList(result) }
        .addOnFailureListener { e -> Log.e(TAG, "Error getting all itineraries", e) }
    return _itineraryList
  }

  /**
   * Get the names of all pinned places for an itinerary and return them as a list of strings
   *
   * @param itinerary Itinerary object to get the pinned place names from
   * @return List of pinned place names
   */
  open fun getPinNames(itinerary: Itinerary): List<String> {
    val pinNames = mutableListOf<String>()
    for (pin in itinerary.pinnedPlaces) {
      pinNames.add(pin.name)
    }
    Log.d("PinNames", pinNames.toString())
    return pinNames
  }

  // Convert the query snapshot to a list of itineraries
  /**
   * Convert the query snapshot to a list of itineraries and add them to the _itineraryList
   *
   * @param taskSnapshot QuerySnapshot to convert to a list of itineraries
   */
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
                pinData["image-url"] as? List<String> ?: emptyList())
          }
      val routeData = document.data["route"] as? List<Map<String, Any>> ?: emptyList()
      val route: List<LatLng> = convertMapToLatLng(routeData)

      val itinerary =
          Itinerary(
              id = document.id,
              title = document.getString("title") ?: "",
              userMail = document.getString("userMail") ?: "",
              location = location,
              flameCount = document.getLong("flameCount") ?: 0L,
              saves = document.getLong("saves") ?: 0L,
              clicks = document.getLong("clicks") ?: 0L,
              numStarts = document.getLong("numStarts") ?: 0L,
              startDateAndTime = document.getString("startDateAndTime") ?: "",
              endDateAndTime = document.getString("endDateAndTime") ?: "",
              pinnedPlaces = pinnedPlaces,
              description = document.getString("description") ?: "",
              route = route)

      _itineraryList.add(itinerary)
    }
  }

  // Update an itinerary in the database
  /**
   * Update an itinerary in the database
   *
   * @param itinerary Itinerary object to update Serialize the Itinerary object to a map and update
   *   it in the database
   */
  fun updateItinerary(itinerary: Itinerary) {
    val itineraryData =
        hashMapOf<String, Any>(
            "id" to itinerary.id,
            "title" to itinerary.title,
            "userMail" to itinerary.userMail,
            "location" to
                hashMapOf(
                    "latitude" to itinerary.location.latitude,
                    "longitude" to itinerary.location.longitude,
                    "name" to itinerary.location.name),
            "flameCount" to itinerary.flameCount,
            "saves" to itinerary.saves, // Added saves field to Itinerary class
            "clicks" to itinerary.clicks, // Added clicks field to Itinerary class
            "numStarts" to itinerary.numStarts, // Added numStarts field to Itinerary class
            "startDateAndTime" to itinerary.startDateAndTime,
            "endDateAndTime" to itinerary.endDateAndTime,
            "pinnedPlaces" to
                itinerary.pinnedPlaces.map { pin ->
                  hashMapOf(
                      "latitude" to pin.latitude,
                      "longitude" to pin.longitude,
                      "name" to pin.name,
                      "description" to pin.description,
                      "image-url" to pin.image_url)
                },
            "description" to itinerary.description,
            "route" to convertLatLngToMap(itinerary.route))
    db.collection("itineraries")
        .document(itinerary.id)
        .set(itineraryData)
        .addOnSuccessListener { Log.d(TAG, "Itinerary updated successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error updating itinerary", e) }
  }

  /**
   * Add a new itinerary to the database
   *
   * @param itinerary Itinerary object to add Serialize the Itinerary object to a map and add it to
   *   the database
   */
  suspend fun addNewItinerary(itinerary: Itinerary) {
    val itineraryData =
        hashMapOf<String, Any>(
            "id" to itinerary.id,
            "title" to itinerary.title,
            "userMail" to itinerary.userMail,
            "location" to
                hashMapOf(
                    "latitude" to itinerary.location.latitude,
                    "longitude" to itinerary.location.longitude,
                    "name" to itinerary.location.name),
            "flameCount" to itinerary.flameCount,
            "saves" to itinerary.saves, // Added saves field to Itinerary class
            "clicks" to itinerary.clicks, // Added clicks field to Itinerary class,
            "numStarts" to itinerary.numStarts, // Added numStarts field to Itinerary class
            "startDateAndTime" to itinerary.startDateAndTime,
            "endDateAndTime" to itinerary.endDateAndTime,
            "pinnedPlaces" to
                itinerary.pinnedPlaces.map { pin ->
                  hashMapOf(
                      "latitude" to pin.latitude,
                      "longitude" to pin.longitude,
                      "name" to pin.name,
                      "description" to pin.description,
                      "image-url" to pin.image_url)
                },
            "description" to itinerary.description,
            "route" to
                convertLatLngToMap(
                    itinerary.route) // Assuming convertLatLngToMap is adapted for your LatLng
            // implementation
            )

    db.collection("itineraries")
        .document(itinerary.id)
        .set(itineraryData)
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

  /**
   * Increment a field in the itinerary document
   *
   * @param itineraryId Id of the itinerary document
   * @param fieldName Name of the field to increment
   * @return Unit
   */
  fun incrementField(itineraryId: String, fieldName: String) {
    val itineraryRef = itineraryDb.document(itineraryId)
    db.runTransaction { transaction ->
          val snapshot = transaction.get(itineraryRef)
          val currentCount = snapshot.getLong(fieldName) ?: 0
          transaction.update(itineraryRef, fieldName, currentCount + 1)
        }
        .addOnSuccessListener { Log.d(TAG, "Field incremented successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error incrementing field", e) }
  }

  /**
   * Get an itinerary by ID
   *
   * @param itineraryId Id of the itinerary
   * @param callback Callback function to call with the itinerary object
   */
  fun getItineraryById(itineraryId: String, callback: (Itinerary?) -> Unit) {
    itineraryDb
        .document(itineraryId)
        .get()
        .addOnSuccessListener { document ->
          if (document.exists()) {
            val locationData = document.data?.get("location") as Map<*, *>
            val location =
                Location(
                    locationData["latitude"] as Double,
                    locationData["longitude"] as Double,
                    locationData["name"] as String)
            val pinnedPlacesData =
                document.data!!["pinnedPlaces"] as? List<Map<String, Any>> ?: emptyList()
            val pinnedPlaces: List<Pin> =
                pinnedPlacesData.map { pinData ->
                  // Assuming Pin class has a constructor that takes these fields:
                  Pin(
                      pinData["latitude"] as? Double ?: 0.0,
                      pinData["longitude"] as? Double ?: 0.0,
                      pinData["name"] as? String ?: "",
                      pinData["description"] as? String ?: "",
                      pinData["image-url"] as? List<String> ?: emptyList())
                }
            val routeData = document.data!!["route"] as? List<Map<String, Any>> ?: emptyList()
            val route: List<LatLng> = convertMapToLatLng(routeData)

            val itinerary =
                Itinerary(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    userMail = document.getString("userMail") ?: "",
                    location = location,
                    flameCount = document.getLong("flameCount") ?: 0L,
                    saves = document.getLong("saves") ?: 0L,
                    clicks = document.getLong("clicks") ?: 0L,
                    numStarts = document.getLong("numStarts") ?: 0L,
                    startDateAndTime = document.getString("startDateAndTime") ?: "",
                    endDateAndTime = document.getString("endDateAndTime") ?: "",
                    pinnedPlaces = pinnedPlaces,
                    description = document.getString("description") ?: "",
                    route = route)
            callback(itinerary)
          } else {
            callback(null)
          }
        }
        .addOnFailureListener { e ->
          Log.e(TAG, "Error fetching itinerary by ID", e)
          callback(null)
        }
  }

  /**
   * Uses a Firestore transaction to safely update a field in the itinerary document.
   *
   * @param itineraryId The ID of the itinerary to update.
   * @param fieldName The name of the field to update.
   * @param newValue The new value to set for the field.
   */
  fun updateField(itineraryId: String, fieldName: String, newValue: Any) {
    val itineraryRef = db.collection("itineraries").document(itineraryId)

    db.runTransaction { transaction ->
          val snapshot = transaction.get(itineraryRef)
          if (snapshot.exists()) {
            transaction.update(itineraryRef, fieldName, newValue)
          } else {
            throw FirebaseFirestoreException(
                "Itinerary not found", FirebaseFirestoreException.Code.ABORTED)
          }
        }
        .addOnSuccessListener {
          Log.d(TAG, "Transaction successfully committed for field: $fieldName")
        }
        .addOnFailureListener { e -> Log.e(TAG, "Transaction failed for field: $fieldName", e) }
  }

  /**
   * Convert a list of LatLng objects to a list of maps with latitude and longitude keys
   *
   * @param latlngList List of LatLng objects
   * @return List of maps with latitude and longitude keys More Firestore friendly format
   */
  private fun convertLatLngToMap(latlngList: List<LatLng>): List<Map<String, Double>> =
      latlngList.map { latLng ->
        mapOf("latitude" to latLng.latitude, "longitude" to latLng.longitude)
      }

  /**
   * Convert a list of maps with latitude and longitude keys to a list of LatLng objects Used to
   * deserialize the route data from Firestore
   *
   * @param routeData List of maps with latitude and longitude keys
   * @return List of LatLng objects
   */
  private fun convertMapToLatLng(routeData: List<Map<String, Any>>): List<LatLng> {
    var route: List<LatLng> = emptyList()
    routeData.map { data ->
      route = route.plus(LatLng(data["latitude"] as Double, data["longitude"] as Double))
    }
    return route
  }
}
