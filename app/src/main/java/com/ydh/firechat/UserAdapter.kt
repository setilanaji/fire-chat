package com.ydh.firechat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ydh.firechat.databinding.ItemContactBinding
import com.ydh.firechat.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val context: Context,
                  private val listener: UserListener) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    var list = mutableListOf<User>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    interface UserListener{
        fun onClick(model: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding: ItemContactBinding = DataBindingUtil.inflate(inflater,
                R.layout.item_contact,parent,false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]

//        println(user)
        holder.itemBinding.user = user

        Glide.with(context).load(user.profileImage).placeholder(R.drawable.profile_image).into(holder.itemBinding.ivItemContact)

//        holder.layoutUser.setOnClickListener {
//            val intent = Intent(context,ChatActivity::class.java)
//            intent.putExtra("userId",user.userId)
//            intent.putExtra("userName",user.userName)
//            context.startActivity(intent)
//        }
    }

    class ViewHolder( val itemBinding: ItemContactBinding, listener: UserListener) :RecyclerView.ViewHolder(itemBinding.root) {

        private var binding : ItemContactBinding? = null

        init {
            this.binding = itemBinding
            itemBinding.ivItemContact.setOnClickListener {
                listener.onClick(itemBinding.user)
            }
        }

//        val txtUserName:TextView = view.findViewById(R.id.tv_item_contact_name)
//        val txtTemp:TextView = view.findViewById(R.id.temp)
//        val imgUser:CircleImageView = view.findViewById(R.id.iv_item_contact)
//        val layoutUser:LinearLayout = view.findViewById(R.id.layoutUser)
    }
}