package com.demo.stateflowandroid.ui.repos

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.demo.stateflowandroid.R
import com.demo.stateflowandroid.databinding.RepoItemBinding
import com.demo.stateflowandroid.domain.Repo
import com.demo.stateflowandroid.domain.RepoOwner
import com.squareup.picasso.Picasso

private const val AVATAR_WIDTH = 250

class RepoAdapter(private val clickListener: (RepoOwner, ImageView) -> Unit) :
    ListAdapter<Repo, RepoAdapter.RepoViewHolder>(RepoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RepoViewHolder {
        val binding = RepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepoViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RepoViewHolder(
        private val binding: RepoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val repoNameView = binding.repoName
        private val starsView = binding.stars
        private val repoOwnerView = binding.repoOwner
        private val avatarImageView = binding.avatarImageView

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind(item: Repo) {
            repoNameView.text = item.name
            starsView.text = itemView.resources.getQuantityString(
                R.plurals.repo_stars, 0, item.stars
            )

            avatarImageView.apply {
                Picasso.get()
                    .load(item.owner.avatarUrl)
                    .resize(AVATAR_WIDTH, AVATAR_WIDTH)
                    .centerCrop()
                    .into(this)
                transitionName = item.owner.login
                setOnClickListener {
                    clickListener.invoke(item.owner, this)
                }
            }

            repoOwnerView.text = item.owner.login
            repoOwnerView.setOnClickListener {
                clickListener.invoke(item.owner, avatarImageView)
            }
        }
    }
}

class RepoDiffCallback : DiffUtil.ItemCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.name == newItem.name
    }
}


