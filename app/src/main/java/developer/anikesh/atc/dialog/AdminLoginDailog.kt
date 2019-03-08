package developer.anikesh.atc.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import developer.anikesh.atc.R
import developer.anikesh.atc.activities.LoginActivity
import developer.anikesh.atc.activities.MainActivity
import kotlinx.android.synthetic.main.admin_login_dialog.*

@SuppressLint("ValidFragment")
class AdminLoginDailog(var listener: Context): DialogFragment() {

    private lateinit var progressDialog: ProgressDialog

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        progressDialog = ProgressDialog(context)
        return inflater.inflate(R.layout.admin_login_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adminLoginBtn.setOnClickListener {
            progressDialog.show()
            if (!TextUtils.isEmpty(adminIdInput.text) && !TextUtils.isEmpty(adminPasswordInput.text)) {
                val databaseRef = FirebaseDatabase.getInstance().getReference("Admin")
                databaseRef.addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show()
                        progressDialog.dismiss()
                    }
                    override fun onDataChange(data: DataSnapshot) {
                        val adminId = (data.value as Map<*, *>)["AdminId"]
                        val adminPasswd = (data.value as Map<*, *>)["Passwd"]
                        if (adminIdInput.text.toString() == adminId && adminPasswordInput.text.toString() == adminPasswd) {
                            Toast.makeText(context, "Login Success", Toast.LENGTH_LONG).show()
                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                            sharedPreferences.edit().apply {
                                putBoolean("isAdmin", true)
                                if (commit()) {
                                    progressDialog.dismiss()
                                    startActivity(Intent(context, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    })
                                    (listener as LoginActivity).finish()
                                }
                            }
                        }else {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Wrong Credentials", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialog.dismiss()
        (listener as LoginActivity).onAdminDialogDismiss()
    }
}