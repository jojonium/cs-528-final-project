package edu.wpi.cs528finalproject.ui.upload

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.wpi.cs528finalproject.MaskClassifier
import edu.wpi.cs528finalproject.R
import edu.wpi.cs528finalproject.Utils
import kotlinx.android.synthetic.main.fragment_upload.*


private const val REQUEST_CODE = 528
class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel
    private lateinit var classifier: MaskClassifier
    private lateinit var ctx: Context
    private lateinit var database: DatabaseReference

    private var correctlyWearingMaskCounter = 0L
    private var numberOfTimesPromptedToWearMask = 0L

    override fun onAttach(context: Context) {
        ctx = context
        super.onAttach(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        uploadViewModel =
            ViewModelProvider(this).get(UploadViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_upload, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        database = Firebase.database.reference
        classifier = MaskClassifier(Utils.assetFilePath(ctx, "mask_detection2.pt"))

        // Get the current Value
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser?.email?.split('@')?.get(0)
                ?: "No User";

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                correctlyWearingMaskCounter = (dataSnapshot.child("correctlyWearingMaskCounter")?.getValue() ?: 0L) as Long
                numberOfTimesPromptedToWearMask = (dataSnapshot.child("numberOfTimesPromptedToWearMask")?.getValue() ?: 0L) as Long
                Log.d(null, "hi")
            }
        }
        val ref = database.child("maskWearing").child(currentFirebaseUser)
        ref.addListenerForSingleValueEvent(valueEventListener)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(imageButtonTakePicture!=null){
            imageButtonTakePicture.setOnClickListener(){
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if(activity?.let { it1 -> takePictureIntent.resolveActivity(it1.packageManager) } !=null){
                    startActivityForResult(takePictureIntent, REQUEST_CODE)
                }else{
                    Toast.makeText(activity, "Unable to open camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val takenImage = data?.extras?.get("data") as Bitmap
            imagePreviewView.setImageBitmap(takenImage)
            imagePreviewView.setVisibility(View.VISIBLE)

            val mask = classifier.predict(takenImage)
            Log.d(null, "Mask prediction")
            Log.d(null, mask.toString())

            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser?.email?.split('@')?.get(0)
                    ?: "No User";

            if(mask==0){
                textView2.setText(getString(R.string.noMask))
                database.child("maskWearing").child(currentFirebaseUser).child("correctlyWearingMaskCounter").setValue(correctlyWearingMaskCounter)
                numberOfTimesPromptedToWearMask += 1
                database.child("maskWearing").child(currentFirebaseUser).child("numberOfTimesPromptedToWearMask").setValue(numberOfTimesPromptedToWearMask)

            }else{
                textView2.setText(getString(R.string.mask))
                correctlyWearingMaskCounter += 1
                database.child("maskWearing").child(currentFirebaseUser).child("correctlyWearingMaskCounter").setValue(correctlyWearingMaskCounter)
                numberOfTimesPromptedToWearMask += 1
                database.child("maskWearing").child(currentFirebaseUser).child("numberOfTimesPromptedToWearMask").setValue(numberOfTimesPromptedToWearMask)
            }
            textView2.setVisibility(View.VISIBLE)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}