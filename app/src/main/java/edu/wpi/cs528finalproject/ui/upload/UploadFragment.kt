package edu.wpi.cs528finalproject.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.wpi.cs528finalproject.R

class UploadFragment : Fragment() {

    private lateinit var uploadViewModel: UploadViewModel

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
        return root
    }
}