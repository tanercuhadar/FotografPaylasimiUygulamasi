
package com.tanercuhadar.fotografpaylasma.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.tanercuhadar.fotografpaylasma.databinding.FragmentYuklemeBinding
import java.util.UUID

class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage:FirebaseStorage
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLaunchers()  // Launchers'ı burada başlatıyoruz.
        auth=Firebase.auth
        storage=Firebase.storage
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.YukleButton.setOnClickListener { yukleTiklandi(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }
    }

    fun yukleTiklandi(view: View) {
        val uuid = UUID.randomUUID()
        val gorselId ="${uuid}.jpg"
        val reference=storage.reference
        val gorselReferansi= reference.child("images").child(gorselId)
        if (secilenGorsel!==null){
            gorselReferansi.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask->
                //url alma işlemi yapılacak
                gorselReferansi.downloadUrl.addOnCompleteListener {uri->
                    if (auth.currentUser!==null){
                        val downloadUrl =uri.toString()
                        val hasMap = hashMapOf<String,Any>()
                        hasMap.put("downloadUrl", downloadUrl)
                        hasMap.put("email",auth.currentUser?.email.toString())
                        hasMap.put("comment",binding.commentText.text.toString())
                        hasMap.put("date",Timestamp.now())
                        db.collection("posts").add(hasMap).addOnSuccessListener { documentReference->
                            //Veri database yüklenmiş oluyor
                            val action = YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)
                        }.addOnFailureListener { exeption->
                            Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                }.addOnSuccessListener {  }
            }.addOnFailureListener {exeption->
                Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun gorselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Read media images
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    // İzin mantığı kullanıcıya gösterilmeli
                    Snackbar.make(view, "Galeriye gitmek için izin ver", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver", View.OnClickListener {
                            // İzin istememiz lazım
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    // İzin istemeliyiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // İzin var, galeriye git
                val intenToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            }
        } else {
            // Read external storage
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // İzin mantığı kullanıcıya gösterilmeli
                    Snackbar.make(view, "Galeriye gitmek için izin ver", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver", View.OnClickListener {
                            // İzin istemeliyiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    // İzin istemeliyiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                // İzin var, galeriye git
                val intenToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            }
        }
    }

    private fun registerLaunchers() {
        // ActivityResultLauncher başlatma işlemi
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilenGorsel!!
                            )
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorsel
                            )
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // PermissionLauncher başlatma işlemi
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // İzin verildi
                val intenToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            } else {
                // Kullanıcı izni vermedi
                Toast.makeText(requireContext(), "İzine ihtiyacımız var", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

