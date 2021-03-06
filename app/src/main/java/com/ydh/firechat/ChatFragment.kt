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
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ydh.firechat.databinding.FragmentChatBinding
import com.ydh.firechat.model.Chat
import com.ydh.firechat.model.NotificationData
import com.ydh.firechat.model.PushNotification
import com.ydh.firechat.model.User
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    lateinit var firebaseUser: FirebaseUser
    lateinit var databaseReference: DocumentReference
    var chatList = ArrayList<Chat>()
    var topic = ""
    lateinit var sender: User
    private val db by lazy { Firebase.firestore }

    private lateinit var binding: FragmentChatBinding
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)



        binding.run {
            imgBack.setOnClickListener {
                requireActivity().onBackPressed()
            }

            arguments?.let {
                val args = ChatFragmentArgs.fromBundle(it)
                println(args.sender.toString())
                sender = args.sender
                databaseReference =
                        FirebaseFirestore.getInstance().collection("users").document(sender.userId)
//                binding.se = args.contact
            }
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser!!




        databaseReference.addSnapshotListener { value, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            val users = mutableListOf<User>()

            value?.let {

                it.toObject(User::class.java).let { user ->
                    tvUserName.text = user?.userName
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


        binding.apply {

            btnSendMessage.setOnClickListener {
                val message: String = etMessage.text.toString()

                if (message.isEmpty()) {
                    Toast.makeText(context, "message is empty", Toast.LENGTH_SHORT).show()
                    etMessage.setText("")
                } else {
                    sendMessage(firebaseUser!!.uid, sender.userId, message)
                    etMessage.setText("")
                    topic = "/${sender.userId}"
                    PushNotification(NotificationData(sender.userName!!, message),
                            topic).also {
                        sendNotification(it)
                    }

                }
            }
        }

        readMessage(firebaseUser.uid, sender.userId)

        return binding.root

    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {

        val msg = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("chat").add(msg).addOnSuccessListener {
            Toast.makeText(context, "sent", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { exc ->
            exc.printStackTrace()
        }

    }

    fun readMessage(senderId: String, receiverId: String) {


        db.collection("chat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        error.printStackTrace()
                        return@addSnapshotListener
                    }

                    value?.let {
                        chatList.clear()
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
            if (response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Log.e("TAG", e.toString())
        }
    }


}