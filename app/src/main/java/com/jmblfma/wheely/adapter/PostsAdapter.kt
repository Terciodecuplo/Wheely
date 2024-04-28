package com.jmblfma.wheely.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Post

class PostsAdapter(val postList: ArrayList<Post>, var context: Context) :
    RecyclerView.Adapter<PostsAdapter.MyHolder>() {

    class MyHolder(item: View) : ViewHolder(item) {
        var userProfileImage: ImageView
        var userName: TextView
        var trackInfo: TextView
        var trackTitle: TextView
        var trackPreview: ImageView

        init {
            userProfileImage = item.findViewById(R.id.userPost_image)
            userName = item.findViewById(R.id.userName_text)
            trackInfo = item.findViewById(R.id.postInfo_text)
            trackTitle = item.findViewById(R.id.trackName_text)
            trackPreview = item.findViewById(R.id.trackPreview_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view: View
        view = LayoutInflater.from(context).inflate(R.layout.itempost_recycler, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        var post = postList[position]
        holder.userProfileImage.setImageResource(R.drawable.user_default_pic)
        holder.userName.text = post.postedBy.nickname
        holder.trackInfo.text = post.associatedTrack.creationTimestamp
        holder.trackTitle.text = post.associatedTrack.name
        holder.trackPreview.setImageResource(R.drawable.route_example)
    }


}