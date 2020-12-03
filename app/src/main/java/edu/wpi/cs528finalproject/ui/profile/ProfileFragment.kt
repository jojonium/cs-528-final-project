package edu.wpi.cs528finalproject.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.wpi.cs528finalproject.MainActivity
import edu.wpi.cs528finalproject.R

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

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

        signoutButton.setOnClickListener{
            SignOutUser();
        }
        return root
    }


    private fun SignOutUser(){
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

}