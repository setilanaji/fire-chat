package com.ydh.firechat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ydh.firechat.databinding.FragmentChatBinding
import com.ydh.firechat.model.Chat
import com.ydh.firechat.model.NotificationData
import com.ydh.firechat.model.PushNotification
import com.ydh.firechat.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    lateinit var firebaseUser: FirebaseUser
    lateinit var databaseReference: DocumentReference
    var chatList = ArrayList<Chat>()
    var topic =""
    private val db by lazy { Firebase.firestore }

    private lateinit var binding: FragmentChatBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)



        binding.run {
            imgBack.setOnClickListener {
                activity?.onBackPressed()
            }

            arguments?.let {
                val args = ChatFragmentArgs.fromBundle(it)
                println(args.sender.toString())
                binding.se = args.contact
            }
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        databaseReference =
        FirebaseFirestore.getInstance().collection("users").document(userId)


        databaseReference.addSnapshotListener { value, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            val users = mutableListOf<User>()

            value?.let {

                    it.toObject(User::class.java).let { user ->

                        if (user?.profileImage == "") {
                            binding.imgProfile.setImageResource(R.drawable.profile_image)
                        } else {
                            Glide.with(this).load(user?.profileImage).into(binding.imgProfile)
                        }

                    }

            }

//            adapter.list = users

//            println(users.map { it.email })
        }


        binding.run {

            btnSendMessage.setOnClickListener {
                var message: String = etMessage.text.toString()

                if (message.isEmpty()) {
                    Toast.makeText(context, "message is empty", Toast.LENGTH_SHORT).show()
                    etMessage.setText("")
                } else {
                    sendMessage(firebaseUser!!.uid, userId, message)
                    etMessage.setText("")
                    topic = "/$userId"
                    PushNotification(NotificationData(userName!!, message),
                            topic).also {
                        sendNotification(it)
                    }

                }
            }
        }

        readMessage(firebaseUser.uid, userId)

        return inflater.inflate(R.layout.fragment_chat, container, false)

    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {

        val msg = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message
        )

        db.collection("chat").add(msg).addOnSuccessListener {
          Toast.makeText(context, "sent", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { exc ->
            exc.printStackTrace()
        }

    }

    fun readMessage(senderId: String, receiverId: String) {


        db.collection("chat").addSnapshotListener { value, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            value?.let {
                for (doc in it) {
                    doc.toObject(Chat::class.java).let { model ->
                        if (model.senderId == senderId && model.receiverId == receiverId
                                || model.senderId == receiverId && model.receiverId == senderId) {
                            println(model)
                            chatList.add(model)
                        }
                    }
                }
            }

            val chatAdapter = ChatAdapter(requireContext(), chatList)
            binding.chatRecyclerView.adapter = chatAdapter

        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }





}