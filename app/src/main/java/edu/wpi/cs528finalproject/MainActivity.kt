package edu.wpi.cs528finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
//        val webView = findViewById<WebView>(R.id.webview)
//        webView.webViewClient = WebViewClient()
//        webView.loadUrl("https://www.cdc.gov/coronavirus/2019-ncov/index.html")
//        val webSettings = webView.settings
//        webSettings.javaScriptEnabled = true

        val signupbutton = findViewById<Button>(R.id.button2);

        signupbutton.setOnClickListener {
            RegisterUser()
        }
    }

    private fun RegisterUser() {
        val useremail = findViewById<EditText>(R.id.emailinput).text.toString();
        val userpassword = findViewById<EditText>(R.id.passwordinput).text.toString();

        if (useremail.isEmpty() || userpassword.isEmpty()) {
            Toast.makeText(this, "Email or Password is Empty !", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(useremail, userpassword).addOnCompleteListener {
            if(!it.isSuccessful) return@addOnCompleteListener

            Log.d("Main", "Successfully created with uid: ${it.result?.user?.uid}")
        }.addOnFailureListener {
            Toast.makeText(this, "Could not sign up : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

}