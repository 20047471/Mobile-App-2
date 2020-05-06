package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.adapters.TrackAdapter
import ie.wit.adapters.TrackListener
import ie.wit.models.BackingTracksModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_track_lists.view.*
import org.jetbrains.anko.info

class AllTracksFragment : YourTracksFragment(),
    TrackListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_track_lists, container, false)
        activity?.title = getString(R.string.menu_all_tracks)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        setSwipeRefresh()

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AllTracksFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllUsersBackingTracks()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getAllUsersBackingTracks()
    }

    fun getAllUsersBackingTracks() {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading All Users Backing Tracks from Firebase")
        val donationsList = ArrayList<BackingTracksModel>()
        app.database.child("backingtracks")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Tracks error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        val donation  = it.
                            getValue<BackingTracksModel>(BackingTracksModel::class.java)

                        donationsList.add(donation!!)
                        root.recyclerView.adapter =
                            TrackAdapter(donationsList, this@AllTracksFragment,true)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()

                        app.database.child("backingtracks").removeEventListener(this)
                    }
                }
            })
    }
}