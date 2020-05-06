package ie.wit.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class BackingTracksModel(
    var uid: String? = "",
    var instrumentType: String = "N/A",
    var Key: String= "N/A",
    var KeyType: String= "N/A",
    var titlebox: String = "",
    var composer: String = "",
    var TrackLink: String = "",
    var bpm1: Int = 0,
    var bpm2: Int=0,
    var bpm3: Int=0,
//    var bpm4: Int=0,
    var editKeyType: String = "",
    var message: String = "a message",
    var upvotes: Int = 0,
    var profilepic: String = "",
    var isfavourite: Boolean = false,
    var email: String? = "joe@bloggs.com",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var uploadpic: String = "")
    : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "titlebox" to titlebox,
            "composer" to composer,
            "instrumentType" to instrumentType,
            "TrackLink" to TrackLink,
            "Key" to Key,
            "KeyType" to KeyType,
            "editKeyType" to editKeyType,
            "bpm1" to bpm1,
            "bpm2" to bpm2,
            "bpm3" to bpm3,
        //    "bpm4" to bpm4,
            "message" to message,
            "upvotes" to upvotes,
            "profilepic" to profilepic,
            "isfavourite" to isfavourite,
            "latitude" to latitude,
            "longitude" to longitude,
            "email" to email,
            "uploadpic" to uploadpic
        )
    }
}


