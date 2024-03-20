package com.example.triptracker.authentication

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleAuthenticator : Authenticator {

  override var signInLauncher: ActivityResultLauncher<Intent>? = null

  override fun createSignInIntent(applicationContext: Context): Intent {
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestProfile()
            .build()
    val googleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
    return googleSignInClient.signInIntent
  }

  override fun signIn(signInIntent: Intent) {
    signInLauncher?.launch(signInIntent)
  }

  override fun signOut() {
    TODO("Not yet implemented")
  }

  override fun delete() {
    TODO("Not yet implemented")
  }

  override fun isSignedIn(): Boolean {
    TODO("Not yet implemented")
  }
}
