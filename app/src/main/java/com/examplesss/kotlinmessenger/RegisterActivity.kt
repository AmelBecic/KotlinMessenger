package com.examplesss.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.log

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        register_button_register.setOnClickListener {
            performregister()

        }

        already_have_account_text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        select_photo_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }
    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo is selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver , selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            select_photo_button.alpha = 0f 
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //select_photo_button.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun performregister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d("RegisterActivity" , "Email is:" +email)
        Log.d("RegisterActivity" , "Password is:" +password)

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please enter email/pw" , Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                //else return succesfull

                Log.d("RegisterActivity", "Succesfully created user ")

                uploadImageToFirebase()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity" , "Error message:" + it.message)
                Toast.makeText(this,"Failed to create a user:"+ it.message , Toast.LENGTH_SHORT).show()
            }
    }

        private fun uploadImageToFirebase() {
            if (selectedPhotoUri == null) return


            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity" , "Succesfully uploaded image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("Register Activity" , "Image URL: $it")

                        saveUserToFirebase(it.toString())
                    }
                }
        }

        private fun saveUserToFirebase(profileImageURL: String) {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            val user = User(uid , name_edittext_register.text.toString() , profileImageURL )

            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity" , "Finally we saved user to database")
                }
        }

}

class User(val uid:String , val username: String , val profileImageURL: String)