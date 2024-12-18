package com.tanercuhadar.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tanercuhadar.fotografpaylasma.R
import com.tanercuhadar.fotografpaylasma.adapter.PostAdapter
import com.tanercuhadar.fotografpaylasma.databinding.FragmentFeedBinding
import com.tanercuhadar.fotografpaylasma.model.Post


class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    lateinit var popop: PopupMenu
    private lateinit var auth:FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList :ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? =null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth=Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingButtonTikalandi(it) }
        popop = PopupMenu(requireContext(), binding.floatingActionButton)
        val inflater = popop.menuInflater
        inflater.inflate(R.menu.my_popop_menu, popop.menu)
        popop.setOnMenuItemClickListener(this)
        fireStoreVerileriAl()
        adapter=PostAdapter(postList)
        binding.feedRecylerView.layoutManager=LinearLayoutManager(requireContext())
        binding.feedRecylerView.adapter=adapter


    }
    private fun fireStoreVerileriAl(){
        db.collection("posts").addSnapshotListener { value, error ->
            if (error!=null){
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT).show()
            }else{
                if (value!=null){
                    if (!value.isEmpty){
                        postList.clear()
                        val documents =value.documents
                        for (document in documents){
                            val comment= document.get("comment") as String //casting
                            val email = document.get("email") as String
                            val downloadUrl = document.get("downloadUrl") as String
                            val post = Post(email,comment, downloadUrl  )
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

    }




    fun floatingButtonTikalandi(view: View) {
        popop.show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        //Popop menü içindeli itemlere tıklanınca ne olur kontorlü yapacağımız yer.
        if (item?.itemId == R.id.yuklemeItem) {
            val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
            Navigation.findNavController(requireView()).navigate(action)
        } else if (item?.itemId == R.id.cikisItem) {
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        return true

    }


}


