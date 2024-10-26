package com.example.damagedetectioncar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    // Hardcoded valid credentials
    private val validCredentials = mapOf(
        "mad" to "mad",
        "car" to "car",
        "damage" to "damage",
        "user" to "password"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        val loginButton: Button = findViewById(R.id.loginButton)
        val signupButton: Button = findViewById(R.id.signupButton)

        loginButton.setOnClickListener {
            loginUser()
        }

        signupButton.setOnClickListener {
            // Handle signup logic here
            Toast.makeText(this, "Sign Up functionality is not implemented yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (validCredentials[username] == password) {
            // If credentials are valid, navigate to the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Call finish() to prevent returning to login
        } else {
            // Show error message
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }
}
