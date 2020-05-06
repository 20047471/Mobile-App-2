package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.adapters.TrackAdapter
import ie.wit.adapters.TrackListener
import ie.wit.main.BackingTrackApp
import ie.wit.models.BackingTracksModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_track_lists.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

open class YourTracksFragment : Fragment(), AnkoLogger,
    TrackListener {

    lateinit var app: BackingTrackApp
    lateinit var loader : AlertDialog
    lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as BackingTrackApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_track_lists, container, false)
        activity?.title = getString(R.string.action_your_tracks)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = root.recyclerView.adapter as TrackAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                deleteBackingTrack((viewHolder.itemView.tag as BackingTracksModel).uid)
                deleteUserDonation(app.auth.currentUser!!.uid,
                    (viewHolder.itemView.tag as BackingTracksModel).uid)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(root.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onBackingTrackClick(viewHolder.itemView.tag as BackingTracksModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(root.recyclerView)

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            YourTracksFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    open fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllBackingTracks(app.auth.currentUser!!.uid)
            }
        })
    }

    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
    }

    fun deleteUserDonation(userId: String, uid: String?) {
        app.database.child("user-tracks").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Tracks error : ${error.message}")
                    }
                })
    }

    fun deleteBackingTrack(uid: String?) {
        app.database.child("backingtracks").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Tracks error : ${error.message}")
                    }
                })
    }

    override fun onBackingTrackClick(backingTracks: BackingTracksModel) {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, UpdateTrackDetailsFragment.newInstance(backingTracks))
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        if(this::class == YourTracksFragment::class)
            getAllBackingTracks(app.auth.currentUser!!.uid)
    }

    fun getAllBackingTracks(userId: String?) {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading Tracks from Firebase")
        val tracksList = ArrayList<BackingTracksModel>()
        app.database.child("user-tracks").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Tracks error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        val backings = it.
                            getValue<BackingTracksModel>(BackingTracksModel::class.java)

                        tracksList.add(backings!!)
                        root.recyclerView.adapter =
                            TrackAdapter(tracksList, this@YourTracksFragment,false)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()

                        app.database.child("user-tracks").child(userId)
                            .removeEventListener(this)
                    }
                }
            })
    }
}
