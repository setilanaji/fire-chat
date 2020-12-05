package com.ydh.firechat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ydh.firechat.databinding.FragmentLoginBinding
import com.ydh.firechat.databinding.FragmentSignUpBinding
import kotlinx.android.synthetic.main.fragment_sign_up.*


class SignUpFragment : Fragment() {

    lateinit var binding: FragmentSignUpBinding
    private val auth by lazy { Firebase.auth }

    private val db by lazy { Firebase.firestore }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        setView()
        return binding.root
    }

    private fun setView(){
        binding.run {
            btRegRegister.setOnClickListener {
                if (etRegisterUserEmail.text.toString().isNotEmpty() &&
                    etRegisterUserPassword.text.toString().isNotEmpty()
                ) {
                    showLoading(true)

                    auth.createUserWithEmailAndPassword(
                        etRegisterUserEmail.text.toString(),
                        etRegisterUserPassword.text.toString()
                    ).addOnSuccessListener {
                        showLoading(false)

                        it.user?.sendEmailVerification()

                        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

                        val userData = hashMapOf(
                                "userId" to firebase.uid,
                                "userName" to etRegisterUserName.text.toString(),
                                "profileImage" to ""
                        )

                        db.collection("users").document(firebase.uid).set(userData).addOnSuccessListener {
                                    showLoading(false)
                            requireActivity().onBackPressed()

//                                    findNavController().navigate(R.id.action_masukFragment_to_daftarFragment)
                                }.addOnFailureListener { exc ->
                                    exc.printStackTrace()

                                    showLoading(false)
                                }



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

    private fun showLoading(isLoading: Boolean) {
        binding.run {
            pbRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
            cvRegister.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

}