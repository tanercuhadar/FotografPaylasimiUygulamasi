package com.tanercuhadar.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tanercuhadar.fotografpaylasma.databinding.FragmentKullaniciBinding


class KullaniciFragment : Fragment() {

    private var _binding: FragmentKullaniciBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButton.setOnClickListener { kayitOl(it) }
        binding.GirisButton.setOnClickListener { girisYap(it) }
        val guncelkullanici = auth.currentUser
        if (guncelkullanici!=null){
            val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun kayitOl(view: View) {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (email.isNotEmpty()&& password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
                if (task.isSuccessful){
                    //kullanıcı oluşturuldu
                    val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }

            }.addOnFailureListener { exepciton->
                Toast.makeText(requireContext(), exepciton.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }else
            Toast.makeText(requireContext(), "Lütfen Boş Bırakmayınız", Toast.LENGTH_SHORT).show()



    }

    fun girisYap(view: View) {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                }
            }.addOnFailureListener { exeption ->
                    Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show()
        }

    }else{
            Toast.makeText(requireContext(), "Lütfen Boş Bırakmayınız", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}