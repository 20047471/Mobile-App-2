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
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class UpdateTrackDetailsFragment : Fragment(), AnkoLogger {

    lateinit var app: BackingTrackApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editBackingTracks: BackingTracksModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as BackingTrackApp

        arguments?.let {
            editBackingTracks = it.getParcelable("editdonation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit_track_details)
        loader = createLoader(activity!!)

        root.editTrackTitle.setText(editBackingTracks!!.titlebox)
        root.editInstrument.setText(editBackingTracks!!.instrumentType)
        root.editYotubeLink.setText(editBackingTracks!!.composer)
        root.editComposer.setText(editBackingTracks!!.TrackLink)
     //   root.editUpvotes.setText(editDonation!!.upvotes.toString())
      //  root.editKeyType.setText(editDonation!!.KeyType)

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Track on Server...")
            updateDonationData()
            updateDonation(editBackingTracks!!.uid, editBackingTracks!!)
            updateUserDonation(app.auth.currentUser!!.uid,
                               editBackingTracks!!.uid, editBackingTracks!!)
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(backingTracks: BackingTracksModel) =
            UpdateTrackDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editdonation",backingTracks)
                }
            }
    }

    fun updateDonationData() {
        editBackingTracks!!.titlebox = root.editTrackTitle.text.toString()
        editBackingTracks!!.composer = root.editYotubeLink.text.toString()
        editBackingTracks!!.TrackLink = root.editComposer.text.toString()
    //    editDonation!!.upvotes = root.editUpvotes.text.toString().toInt()
      //  editDonation!!.KeyType = if (root.editKeyType.checkedRadioButtonId == R.id.editSharp) "Sharp"
       // else if (root.editKeyType.checkedRadioButtonId == R.id.editFlat) "Flat" else "Natural"

    }

    fun updateUserDonation(userId: String, uid: String?, backingTracks: BackingTracksModel) {
        app.database.child("user-tracks").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(backingTracks)
                        activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.homeFrame, YourTracksFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Tracks error : ${error.message}")
                    }
                })
    }

    fun updateDonation(uid: String?, backingTracks: BackingTracksModel) {
        app.database.child("backingtracks").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(backingTracks)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase tracks error : ${error.message}")
                    }
                })
    }
}
