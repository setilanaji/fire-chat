package com.ydh.firechat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ydh.firechat.databinding.FragmentLoginBinding
import com.ydh.firechat.model.User


class LoginFragment : Fragment() {


    private lateinit var binding: FragmentLoginBinding
    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setView()
        return binding.root
    }

    private fun setView(){
        binding.apply {
            btLogToRegister.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_signUpFragment) }

            tvResetPassword.setOnClickListener {
                if (etLoginUserEmail.text.toString().isNotEmpty()){
                    auth.sendPasswordResetEmail(etLoginUserEmail.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showMessage("reset password link has been sent to your email")
                            } else {
                                showMessage("something wrong")
                            }
                        }
                }else{
                    showMessage("please fill your email")
                }

            }


            btLogLogin.setOnClickListener {
                if (etLoginUserEmail.text.toString().isNotEmpty() &&
                    etLoginUserPassword.text.toString().isNotEmpty()
                ) {
                    showLoading(true)

                    auth.signInWithEmailAndPassword(
                        etLoginUserEmail.text.toString(),
                        etLoginUserPassword.text.toString()
                    ).addOnSuccessListener {
                        if (it.user?.isEmailVerified == true) {

                            val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!


//                            db.collection("users").document(it.user!!.uid)
//                                .set((firebase.uid, firebase.email!!, firebase.photoUrl.toString()))
//                                .addOnSuccessListener {
//                                    showLoading(false)
//
////                                    findNavController().navigate(R.id.action_masukFragment_to_daftarFragment)
//                                }.addOnFailureListener { exc ->
//                                    exc.printStackTrace()
//
//                                    showLoading(false)
//                                }

                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            showMessage("Email belum diverifikasi")
                        }

                        showLoading(false)
                    }.addOnFailureListener {
                        it.printStackTrace()

                        showMessage(it.message ?: "Oops something went wrong")
                        showLoading(false)
                    }
                } else {
                    showMessage("Email dan password tidak boleh kosong")
                }
            }
        }
    }
    private fun showMessage(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null && auth.currentUser?.isEmailVerified == true) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.run {
            pbSignIn.visibility = if (isLoading) View.VISIBLE else View.GONE
            cvSignIn.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

}