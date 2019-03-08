package developer.anikesh.atc.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import developer.anikesh.atc.R
import developer.anikesh.atc.dialog.AdminLoginDailog
import developer.anikesh.atc.requestMediaPermission
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit



class LoginActivity : AppCompatActivity() {

    private lateinit var mContext: Activity
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var startingYPosition = 0
    private var curYPosition = 0

    private var isAdminLoginDialogShowing = false

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dialog = ProgressDialog(this)

        requestMediaPermission()

        mContext = this

        auth = FirebaseAuth.getInstance()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if(FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, AttendanceActivity::class.java))
            finish()
        }
        if (sharedPreferences.getBoolean("isAdmin", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        loginPhoneInput.append("+91")

        verifyBtn.setOnClickListener {
            dialog.show()
            if (verifyBtn.text == "Send Otp") {
                sendOtp()
            }else {
                val otpCode = otpView.text.toString()
                if (otpCode.length == 6) {
                    signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(storedVerificationId!!, otpCode))
                }
                verifyBtn.isEnabled = false
            }
        }

        loginRootLayout.setOnTouchListener { v, event ->
            handleGesture(event)
            true
        }
    }

    private fun sendOtp() {
        verifyBtn.isEnabled = false
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            loginPhoneInput.text.toString(),
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                    signInWithPhoneAuthCredential(p0!!)
                    Toast.makeText(mContext, "Verification Success", Toast.LENGTH_LONG).show()
                }
                override fun onVerificationFailed(p0: FirebaseException?) {
                    Toast.makeText(mContext, "Verification failed! Please check your Number", Toast.LENGTH_LONG).show()
                    verifyBtn.isEnabled = true
                    dialog.dismiss()
                }

                override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                    storedVerificationId = verificationId
                    resendToken = token
                    verifyBtn.isEnabled = true
                    verifyBtn.text = "Verify"
                    dialog.dismiss()
                    Toast.makeText(mContext, "Code send", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(mContext) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in User's information
                    dialog.dismiss()
                    val database = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().uid!!)
                    database.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                println("Already registered")
                                if ((snapshot.value as HashMap<*, *>)["IsBlocked"] as Boolean) {
                                    Toast.makeText(mContext, "You are Blocked by Admin", Toast.LENGTH_LONG).show()
                                }else {
                                    sharedPreferences.edit().apply {
                                        remove("LastDate")
                                        apply()
                                    }
                                    startActivity(Intent(mContext, AttendanceActivity::class.java))
                                    finish()
                                }
                            }else {
                                println("Not registered")
                                startActivity(Intent(mContext, SignupActivity::class.java))
                            }
                        }
                    })

                } else {
                    dialog.dismiss()
                    // Sign in failed, display a message and update the UI
                    Toast.makeText(mContext, "failed to sign in", Toast.LENGTH_LONG).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                verifyBtn.isEnabled = true
                verifyBtn.text = "Resend Otp"
            }
    }

    private fun handleGesture(event: MotionEvent) {
        val pointerCount = event.pointerCount
        if (pointerCount == 2) {
            val action = event.action
            if (action == MotionEvent.ACTION_MOVE) {
                if (startingYPosition == 0) {
                    startingYPosition = event.y.toInt()
                }
                curYPosition = event.y.toInt()

                if (curYPosition - startingYPosition > 300 ) {
                    if (!isAdminLoginDialogShowing) {
                        AdminLoginDailog(this).show(supportFragmentManager, "AdminLogin")
                        isAdminLoginDialogShowing = true
                    }
                }
            }
        }
        if (event.action == MotionEvent.ACTION_UP) {
            startingYPosition = 0
        }
    }

    fun onAdminDialogDismiss() {
        isAdminLoginDialogShowing = false
    }
}
