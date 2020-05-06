package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.models.BackingTracksModel
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_tracks.view.*
import kotlinx.android.synthetic.main.card_tracks.view.imagefavourite
import kotlinx.android.synthetic.main.card_tracks.view.titlebox
import kotlinx.android.synthetic.main.card_tracks.view.KeyType as KeyType

interface TrackListener {
    fun onBackingTrackClick(backingTracks: BackingTracksModel)
}

class TrackAdapter constructor(var backingTracks: ArrayList<BackingTracksModel>,
                               private val listener: TrackListener, reportall : Boolean)
    : RecyclerView.Adapter<TrackAdapter.MainHolder>() {

    val reportAll = reportall

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_tracks,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val donation = backingTracks[holder.adapterPosition]
        holder.bind(donation,listener,reportAll)
    }

    override fun getItemCount(): Int = backingTracks.size

    fun removeAt(position: Int) {
        backingTracks.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(backingTracks: BackingTracksModel, listener: TrackListener, reportAll: Boolean) {
            itemView.tag = backingTracks
            itemView.titlebox.text = backingTracks.titlebox
            itemView.composer.text = backingTracks.composer
            itemView.TrackLink.text = backingTracks.TrackLink
            itemView.bpm1.text = backingTracks.bpm1.toString()
            itemView.bpm2.text = backingTracks.bpm2.toString()
            itemView.bpm3.text = backingTracks.bpm3.toString()
       //     itemView.amount4.text = donation.bpm4.toString()
            itemView.instrumentType.text = backingTracks.instrumentType
            itemView.Key.text = backingTracks.Key
            itemView.KeyType.text = backingTracks.KeyType



            if(backingTracks.isfavourite) itemView.imagefavourite.setImageResource(android.R.drawable.star_big_on)

            if(!reportAll)
                itemView.setOnClickListener { listener.onBackingTrackClick(backingTracks) }

            if(!backingTracks.profilepic.isEmpty()) {
                Picasso.get().load(backingTracks.profilepic.toUri())
                    //.resize(180, 180)
                    .transform(CropCircleTransformation())
                    .into(itemView.imageIcon)
            }
            else
                itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_homer_round)
        }
    }
}