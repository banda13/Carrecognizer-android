package com.ai.deep.andy.carrecognizer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import android.Manifest.permission.READ_CONTACTS
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.provider.Settings
import android.service.autofill.RegexValidator
import android.util.Log
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.model.User
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.services.users.LoginService
import com.ai.deep.andy.carrecognizer.services.users.RegistrationService
import com.ai.deep.andy.carrecognizer.utils.ErrorUtils
import com.ai.deep.andy.carrecognizer.utils.Logger
import com.android.volley.TimeoutError
import com.orm.SugarRecord

import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {

    private var loginTask: UserLoginTask? = null
    private var signInTask: UserSignInTask? = null
    private var currentUser: User? = null

    private var lastErrorMessage: String = "Unknown error occurred with authentication, please try again later"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegistration()
                return@OnEditorActionListener true
            }
            false
        })
        val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if(settings.getBoolean("keep_user_logged_in", true)){
            loadCachedUser()
        }
        else{
            Log.i(Logger.LOGTAG, "Auto login disabled")
        }

        if(!settings.getBoolean("enable_anonimus_usage", true)){
            continue_without_login.visibility = View.GONE
        }
        else{
            Log.i(Logger.LOGTAG, "Anonimus login disabled")
        }


        email_sign_in_button.setOnClickListener { attemptRegistration() }

        continue_without_login.setOnClickListener { temporaryAttemptLogin() }
    }

    private fun loadCachedUser(){
        if(loginTask != null){
            return
        }
        //there will be maximum 1 User
        val users = SugarRecord.findAll(User::class.java)
        if(users.hasNext()){
            currentUser = users.next()
        }
        if(currentUser != null){
            if(!currentUser!!.temporary_user){
                //auto login if user already signed up!
                showProgress(true)
                val email : String = currentUser!!.email.toString()
                val password : String = currentUser!!.password.toString()
                loginTask = UserLoginTask(email, password , this)
                loginTask!!.execute(null as Void?)
            }
        }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptRegistration() {
        if (signInTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null
        firstname.error = null
        lastname.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        val firstNameStr = firstname.text.toString()
        val lastNameStr = lastname.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if(TextUtils.isEmpty(firstNameStr)){
            firstname.error = getString(R.string.error_field_required)
            focusView = firstname
            cancel = true
        }

        if(TextUtils.isEmpty(lastNameStr)){
            lastname.error = getString(R.string.error_field_required)
            focusView = lastname
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            if(currentUser == null) {
                saveCurrentUser(emailStr, passwordStr, firstNameStr, lastNameStr, false)
                signInTask = UserSignInTask(emailStr, passwordStr, firstNameStr, lastNameStr, this)
                signInTask!!.execute(null as Void?)
            }
            else{
                saveCurrentUser(emailStr, passwordStr, firstNameStr, lastNameStr, false)
                signInTask = UserSignInTask(emailStr, passwordStr, firstNameStr, lastNameStr, this)
                signInTask!!.execute(null as Void?)
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun temporaryAttemptLogin(){
        if(loginTask != null|| signInTask != null){
            return
        }
        val temporaryPw = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)
        val androidId = Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)
        val temporaryEmail = "$androidId@android.com"

        showProgress(true)
        if(currentUser == null){
            saveCurrentUser(temporaryEmail, temporaryPw, "Anonymous", "User", true)
            signInTask = UserSignInTask(temporaryEmail, temporaryPw, "Anonymous", "User", this)
            signInTask!!.execute(null as Void?)
        }
        else{
            loginTask = UserLoginTask(currentUser?.email!!, currentUser?.password!!, this)
            loginTask!!.execute(null as Void?)
        }
    }

    private fun saveCurrentUser(email: String, pw: String?, firstName: String, lastName: String, temporal: Boolean){

        SugarRecord.deleteAll(User::class.java)
        currentUser = User()
        currentUser!!.email = email
        currentUser!!.password = pw
        currentUser!!.first_name = firstName
        currentUser!!.last_name = lastName
        currentUser!!.temporary_user = temporal
        currentUser!!.registration_date = System.currentTimeMillis()
        currentUser!!.save()

        val users = SugarRecord.findAll(User::class.java)
        if(users.hasNext()){
            currentUser = users.next()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mEmail: String,
                                                   private val mPassword: String,
                                                   private val context: Activity) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            var done: Boolean = false
            var success : Boolean = false

            LoginService(applicationContext, object : VolleyOnEventListener<JSONObject>{
                override fun onSuccess(obj: JSONObject) {
                    success = true
                    done = true
                }

                override fun onFailure(e: Exception) {
                    lastErrorMessage = if(e is TimeoutError){
                        "Server is not available, check your internet connection"
                    } else{
                        "Unable to login: " + e.message
                    }
                    ErrorUtils.logError(lastErrorMessage, e, ErrorUtils.ErrorCode.USER_LOGIN_FAILED)
                    success = false
                    done = true
                }
            }).login(mEmail, mPassword)
            while(!done){
                //its of to block in background
            }
            return success
        }

        override fun onPostExecute(success: Boolean?) {
            loginTask = null
            showProgress(false)

            if (success!!) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                finish()
            } else {
                Toast.makeText(context, lastErrorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCancelled() {
            loginTask = null
            showProgress(false)
        }
    }

    inner class UserSignInTask internal constructor(private val mEmail: String,
                                                   private val mPassword: String,
                                                   private val mFirstName: String,
                                                   private val mLastName: String,
                                                   private val context: Activity) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            var done: Boolean = false
            var success : Boolean = false

            RegistrationService(applicationContext, object : VolleyOnEventListener<JSONObject>{
                override fun onSuccess(obj: JSONObject) {
                    LoginService(applicationContext, object : VolleyOnEventListener<JSONObject>{
                        override fun onSuccess(obj: JSONObject) {
                            success = true
                            done = true
                        }

                        override fun onFailure(e: Exception) {
                            lastErrorMessage = if(e is TimeoutError){
                                "Server is not available, check your internet connection"
                            } else{
                                "Unable to login: " + e.message
                            }
                            ErrorUtils.logError(lastErrorMessage, e, ErrorUtils.ErrorCode.USER_LOGIN_FAILED)
                            success = false
                            done = true
                        }
                    }).login(mEmail, mPassword)
                }

                override fun onFailure(e: Exception) {
                    LoginService(applicationContext, object : VolleyOnEventListener<JSONObject>{
                        override fun onSuccess(obj: JSONObject) {
                            success = true
                            done = true
                        }

                        override fun onFailure(e: Exception) {
                            success = false
                            done = true
                        }
                    }).login(mEmail, mPassword)
                }
            }).registration(mEmail, mPassword, mFirstName, mLastName)

            while(!done){
                //its of to block in background
            }
            return success
        }

        override fun onPostExecute(success: Boolean?) {
            signInTask = null
            showProgress(false)

            if (success!!) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                finish()
            } else {
                Toast.makeText(context, lastErrorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCancelled() {
            signInTask = null
            showProgress(false)
        }
    }

    companion object {

        private const val REQUEST_READ_CONTACTS = 0

    }
}
