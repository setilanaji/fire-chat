package com.ydh.firechat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.ydh.firechat.databinding.FragmentUserListBinding
import com.ydh.firechat.model.User
import com.ydh.firechat.service.FirebaseService
import kotlinx.android.synthetic.main.fragment_user_list.*

class UserListFragment : Fragment(), UserAdapter.UserListener {

    private lateinit var binding: FragmentUserListBinding
    private val adapter by lazy { UserAdapter(requireActivity(), this) }

    var userList = ArrayList<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserListBinding.inflate(inflater, container, false)

        binding.run {
//            rvUser.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

            rvUser.adapter = adapter
//            imgProfile.setOnClickListener {
//                val intent = Intent(
//                    this@UsersActivity,
//                    ProfileActivity::class.java
//                )
//                startActivity(intent)
//            }
        }
        FirebaseService.sharedPref =
            requireContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }


        getUsersList()


        return binding.root
    }

    private fun getUsersList() {
        userList.clear()
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/$userid")


        val databaseReference: CollectionReference =
            FirebaseFirestore.getInstance().collection("users")


        databaseReference.addSnapshotListener { value, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            value?.let {
                for (doc in it) {
                    doc.toObject(User::class.java).let { model ->
                        if (model.userId != userid) {
                            println(model)
                            userList.add(model)
                        }
                    }
                }
            }
            adapter.list = userList

//            val userAdapter = UserAdapter(requireContext(), userList)
//            binding.rvUser.adapter = adapter

        }



//        (object : Snap {
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                userList.clear()
//                val currentUser = snapshot.getValue(User::class.java)
//                if (currentUser!!.profileImage == ""){
//                    imgProfile.setImageResource(R.drawable.profile_image)
//                }else{
//                    Glide.with(this@UsersActivity).load(currentUser.profileImage).into(imgProfile)
//                }
//
//                for (dataSnapShot: DataSnapshot in snapshot.children) {
//                    val user = dataSnapShot.getValue(User::class.java)
//
//                    if (!user!!.userId.equals(firebase.uid)) {
//
//                        userList.add(user)
//                    }
//                }
//
//                val userAdapter = UserAdapter(this@UsersActivity, userList)
//
//                userRecyclerView.adapter = userAdapter
//            }
//
//        })
    }

    override fun onClick(model: User) {
        val action = UserListFragmentDirections.actionUserListFragmentToChatFragment(
                model
        )
        findNavController().navigate(action)    }


}