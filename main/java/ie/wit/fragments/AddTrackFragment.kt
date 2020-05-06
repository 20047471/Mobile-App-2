package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.main.BackingTrackApp
import ie.wit.models.BackingTracksModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_add_track.*
import kotlinx.android.synthetic.main.fragment_add_track.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.lang.String.format
import java.util.HashMap


class AddTrackFragment : Fragment(), AnkoLogger {

    lateinit var app: BackingTrackApp
    var receiveTracks = 0
    lateinit var loader : AlertDialog
    lateinit var eventListener : ValueEventListener
    var favourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as BackingTrackApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_add_track, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_add_track)

        root.progressBar.max = 10000
        root.amountPicker.minValue = 0
        root.amountPicker.maxValue = 3

        root.amountPicker2.minValue = 0
        root.amountPicker2.maxValue = 9

        root.amountPicker3.minValue = 0
        root.amountPicker3.maxValue = 9

        root.amountPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected number to paymentAmount
            root.trackse.setText("$newVal")
        }

        setButtonListener(root)
        setFavouriteListener(root)
        return root;

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AddTrackFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener( layout: View) {
        layout.createButton.setOnClickListener {

            var titlebox = layout.titlebox.text.toString()
            var composer = layout.composer.text.toString()
            var TrackLink  = layout.TrackLink.text.toString()

            val bpm1 = if (layout.trackse.text.isNotEmpty())
                layout.trackse.text.toString().toInt() else layout.amountPicker.value

            val bpm2 = layout.amountPicker2.value
            val bpm3 = layout.amountPicker3.value

            val Key = if(layout.Key.checkedRadioButtonId == R.id.Akey) "A"
            else if(layout.Key.checkedRadioButtonId == R.id.Bkey) "B"
            else if(layout.Key.checkedRadioButtonId == R.id.Ckey) "C"
            else if(layout.Key.checkedRadioButtonId == R.id.Dkey) "D"
            else if(layout.Key.checkedRadioButtonId == R.id.Ekey) "E"
            else if(layout.Key.checkedRadioButtonId == R.id.Fkey) "F"
            else if(layout.Key.checkedRadioButtonId == R.id.Gkey) "G" else "n/a"

            val KeyType = if (layout.KeyType.checkedRadioButtonId == R.id.Sharp) "Sharp"
            else if (layout.KeyType.checkedRadioButtonId == R.id.Flat) "Flat" else "Natural"

            if(receiveTracks >= layout.progressBar.max)
                activity?.toast("BPM!")
            else {
                val instrumentType = if(layout.instrumentType.checkedRadioButtonId == R.id.Electric) "Electric" else "Acoustic"
                writeNewTrack(BackingTracksModel(instrumentType = instrumentType,
                    titlebox = titlebox,
                    composer = composer,
                    TrackLink = TrackLink,
                    Key = Key,
                    KeyType = KeyType,
                    bpm1 = bpm1,
                    bpm2 = bpm2,
                    bpm3 = bpm3,
                    profilepic = app.userImage.toString(),
                    isfavourite = favourite,
                    latitude = app.currentLocation.latitude,
                    longitude = app.currentLocation.longitude,
                    email = app.auth.currentUser?.email))
            }
        }
    }

    fun setFavouriteListener (layout: View) {
        layout.imagefavourite.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (!favourite) {
                    layout.imagefavourite.setImageResource(android.R.drawable.star_big_on)
                    favourite = true
                }
                else {
                    layout.imagefavourite.setImageResource(android.R.drawable.star_big_off)
                    favourite = false
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getTracks(app.auth.currentUser?.uid)
    }

    override fun onPause() {
        super.onPause()
        if(app.auth.uid != null)
            app.database.child("user-tracks")
                    .child(app.auth.currentUser!!.uid)
                    .removeEventListener(eventListener)
    }

    fun writeNewTrack(backingTracks: BackingTracksModel) {
        // Create new donation at /donations & /donations/$uid
        showLoader(loader, "Adding backingtracks to Firebase")
        info("Firebase DB Reference : $app.database")
        val uid = app.auth.currentUser!!.uid
        val key = app.database.child("backingtracks").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        backingTracks.uid = key
        val backingTrackValues = backingTracks.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/backingtracks/$key"] = backingTrackValues
        childUpdates["/user-tracks/$uid/$key"] = backingTrackValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }

    fun getTracks(userId: String?) {
        eventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                info("Firebase Backing Track error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                receiveTracks = 0
                val children = snapshot.children
                children.forEach {
                    val bt = it.getValue<BackingTracksModel>(BackingTracksModel::class.java)
                    receiveTracks += bt!!.bpm1
                }
                progressBar.progress = receiveTracks
                trackss.text = format("$ $receiveTracks")
            }
        }

        app.database.child("user-tracks").child(userId!!)
            .addValueEventListener(eventListener)
    }
}
