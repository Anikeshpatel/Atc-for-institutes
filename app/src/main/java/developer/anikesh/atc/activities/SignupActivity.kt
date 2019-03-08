package developer.anikesh.atc.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import developer.anikesh.atc.R
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.ByteArrayOutputStream

class SignupActivity : AppCompatActivity() {

    private var photo: Bitmap? = null
    private lateinit var dialog: ProgressDialog
    private lateinit var mContext: Context

    private var isRegisterd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mContext = this

        dialog = ProgressDialog(this)
        dialog.setTitle("Please Wait")
        dialog.setCanceledOnTouchOutside(false)

        profilePicView.setOnClickListener {
            startActivityForResult(Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), 100)
        }

        registerBtn.setOnClickListener {
            if (!TextUtils.isEmpty(nameInput.text) && !TextUtils.isEmpty(branchCodeInput.text)) {
                if (isBranchCodeValid(branchCodeInput.text.toString())) {
                    if (photo != null) {

                        val storage = FirebaseStorage.getInstance().getReference("ProfilePic")
                            .child(FirebaseAuth.getInstance().uid!! + ".jpg")
                        val outputStream = ByteArrayOutputStream()
                        photo!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        dialog.show()
                        storage.putBytes(outputStream.toByteArray())
                            .addOnSuccessListener {
                                storage.downloadUrl.addOnSuccessListener {uri ->
                                    val database = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().uid!!)
                                    val user = mapOf(
                                        Pair("Name", nameInput.text.toString()),
                                        Pair("BranchCode", branchCodeInput.text.toString()),
                                        Pair("ProfilePic", uri.toString()),
                                        Pair("IsBlocked", false)
                                    )
                                    database.setValue(user).addOnSuccessListener {
                                        dialog.dismiss()
                                        Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
                                        isRegisterd = true
                                        startActivity(Intent(this, AttendanceActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        })
                                        finish()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Failed to register user", Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Enable to upload profile photo", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            }
                            .addOnProgressListener { taskSnapshot ->
                                val progress = (100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                                dialog.progress = progress.toInt()
                            }

                    }else {
                        Toast.makeText(this, "Please Choose Profile Pic", Toast.LENGTH_LONG).show()
                    }
                }else {
                    Toast.makeText(this, "Secret Branch Code is not valid", Toast.LENGTH_LONG).show()
                }
            }else {
                Toast.makeText(this, "Input fields are empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isBranchCodeValid(branchCode: String): Boolean {
        return when(branchCode) {
            "CS0863" -> true
            "ME0863" -> true
            "CE0863" -> true
            "EE0863" -> true
            "EC0863" -> true
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            data?.let {
                photo = BitmapFactory.decodeStream(contentResolver.openInputStream(data.data!!))
                val width = photo!!.width
                val height = photo!!.height
                var maxWidth = 150
                var maxHeight = 150
                if (width > maxWidth || height > maxHeight) {
                    val imageRatio = width.toFloat() / height.toFloat()
                    val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

                    if (ratioMax > imageRatio) {
                        maxWidth = ((maxHeight * imageRatio).toInt())

                    }else {
                        maxHeight = ((maxWidth / imageRatio).toInt())
                    }
                    photo = Bitmap.createScaledBitmap(photo!!, maxWidth, maxHeight, true)
                }
                Glide.with(this)
                    .load(photo)
                    .into(profilePicView)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isRegisterd) {
            FirebaseAuth.getInstance().signOut()
        }
    }
}
