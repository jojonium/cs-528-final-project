package edu.wpi.cs528finalproject.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.wpi.cs528finalproject.R


class ReportFragment : Fragment() {

    private lateinit var reportViewModel: ReportViewModel
    private lateinit var database: DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        reportViewModel =
            ViewModelProvider(this).get(ReportViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_report, container, false)
//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        val reportButton = root.findViewById<Button>(R.id.button);
        database = Firebase.database.reference

        reportButton.setOnClickListener{
            reportFB()
        }

        return root
    }

    private fun reportFB(){
        val location = activity?.findViewById<EditText>(R.id.reportLocation)?.text.toString();
        val date = activity?.findViewById<EditText>(R.id.reportDate)?.text.toString();
        val noofpeople = activity?.findViewById<EditText>(R.id.reportNumPeople)?.text.toString();
        val currentFirebaseUserEmail = FirebaseAuth.getInstance().currentUser?.email;

        if (location.isEmpty() || date.isEmpty() || noofpeople.isEmpty()) {
            Toast.makeText(activity, "One of the above fields is empty !", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("report").child(location).child("useremail").setValue(currentFirebaseUserEmail)
        database.child("report").child(location).child("noofpeople").setValue(noofpeople)
        database.child("report").child(location).child("date").setValue(date)

    }


}