package edu.wpi.cs528finalproject.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.wpi.cs528finalproject.LoginActivity
import edu.wpi.cs528finalproject.R
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlin.math.roundToInt

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    private var correctlyWearingMaskCounter = 0L
    private var numberOfTimesPromptedToWearMask = 0L

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        val signoutButton = root.findViewById<Button>(R.id.button)
        database = Firebase.database.reference

        signoutButton.setOnClickListener {
            SignOutUser()
        }

        // Calculate the percentage of times the user wears his or her mask based on the data in firebase
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser?.email?.split('@')?.get(0)
            ?: "No User"
        var percentage = 0.0
        val percentEventListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                correctlyWearingMaskCounter =
                    (dataSnapshot.child("correctlyWearingMaskCounter").value ?: 0L) as Long
                numberOfTimesPromptedToWearMask =
                    (dataSnapshot.child("numberOfTimesPromptedToWearMask").value
                        ?: 0L) as Long
                //update the view with the percent of data
                if (numberOfTimesPromptedToWearMask != 0L) {
                    percentage =
                        (correctlyWearingMaskCounter.toDouble() / numberOfTimesPromptedToWearMask.toDouble())
                    statsText.text = getString(R.string.statsSentence).format(percentage * 100)
                } else {
                    statsText.text = getString(R.string.noDataSentence)
                }
            }
        }
        val ref = database.child("maskWearing").child(currentFirebaseUser)
        ref.addListenerForSingleValueEvent(percentEventListener)

        // Calculate the users percentile compared to all other users in firebase
        val percentileEventListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.value
                if (data !is HashMap<*, *>) return
                // determine the percentile of the user
                // loop through all user percentages and determine how many this user is larger than
                // round up to make it a nicer number to display
                val percentageList = DoubleArray(data.size)
                var counter = 0
                for ((_, value) in data) {
                    if (value !is HashMap<*, *>) return
                    val numerator = (value["correctlyWearingMaskCounter"] as Long).toDouble()
                    val denominator =
                        (value["numberOfTimesPromptedToWearMask"] as Long).toDouble()
                    percentageList[counter] = numerator / denominator
                    counter++
                }
                counter = 0
                percentageList.sort()
                for (p in percentageList) {
                    if (percentage < p) {
                        break
                    }
                    counter++
                }
                val percentile = ((counter.toDouble() / data.size.toDouble()) * 100).roundToInt()
                when {
                    percentile >= 75 -> {
                        percentileCircle.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(),
                                R.color.green
                            )
                        )
                    }
                    percentile > 40 -> {
                        percentileCircle.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(),
                                R.color.yellow
                            )
                        )
                    }
                    else -> {
                        percentileCircle.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(),
                                R.color.red
                            )
                        )
                    }
                }
                percentileText.text = getString(R.string.percentile).format(percentile)
                percentileCircle.text = getString(R.string.percentileNum).format(percentile)
            }
        }
        val ref2 = database.child("maskWearing")
        ref2.addListenerForSingleValueEvent(percentileEventListener)

        return root
    }

    private fun SignOutUser() {
        val intent = Intent(activity, LoginActivity::class.java)
        FirebaseAuth.getInstance().signOut();
        startActivity(intent)
    }

}