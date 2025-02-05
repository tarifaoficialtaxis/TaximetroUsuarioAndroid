package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomButton
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomCheckBox
import com.mitarifamitaxi.taximetrousuario.components.ui.CustomTextField
import com.mitarifamitaxi.taximetrousuario.helpers.MontserratFamily
import com.mitarifamitaxi.taximetrousuario.models.User
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModel
import com.mitarifamitaxi.taximetrousuario.viewmodels.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(this)
    }

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    // One Tap client
    private lateinit var oneTapClient: SignInClient

    // Request code for starting One Tap UI
    private val REQ_ONE_TAP = 2

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Initialize the One Tap Client
        oneTapClient = Identity.getSignInClient(this)
        setContent {
            MainView(
                onRegisterClicked = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            )
        }
    }

    private fun signInWithGoogle() {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID (from strings.xml)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Show all accounts on the device (not only previously signed in)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Whether to show the "auto-select" UI.
            // If true, Google might automatically sign in with the last used account.
            .setAutoSelectEnabled(false)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    // Launch the sign-in flow
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQ_ONE_TAP,
                        null, 0, 0, 0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener { e ->
                // One Tap Sign-In failed
                Log.e(TAG, "One Tap failed: ${e.localizedMessage}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            Log.d(TAG, "Got ID token.")
                            firebaseAuthWithGoogle(idToken)
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in succeeded
                    val user = auth.currentUser
                    Log.d(TAG, "Firebase Sign-In success. User: ${user?.displayName}")

                    val localUser = user?.displayName?.let {
                        user.email?.let { it1 ->
                            User(
                                user.uid,
                                it,
                                it1
                            )
                        }
                    }
                    val userJson = Gson().toJson(localUser)

                    val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("USER_OBJECT", userJson)
                        apply()
                    }

                    startActivity(Intent(this, HomeActivity::class.java))

                } else {
                    // Sign-in failed
                    Log.e(TAG, "Firebase Sign-In failed: ${task.exception}")
                }
            }
    }


    @Composable
    private fun MainView(
        onRegisterClicked: () -> Unit
    ) {


        Column {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.white))
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(colorResource(id = R.color.main))
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.city_background),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 40.dp)
                            )

                            Image(
                                painter = painterResource(id = R.drawable.logo2),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(134.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-24).dp),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.white),
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 29.dp, bottom = 10.dp, start = 29.dp, end = 29.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.welcome),
                                fontFamily = MontserratFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = colorResource(id = R.color.main),
                                modifier = Modifier
                                    .padding(bottom = 25.dp),
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(17.dp),
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                            ) {
                                CustomTextField(
                                    value = viewModel.userName,
                                    onValueChange = { viewModel.userName = it },
                                    placeholder = stringResource(id = R.string.user_name),
                                    leadingIcon = Icons.Rounded.Person,
                                    keyboardType = KeyboardType.Email
                                )

                                CustomTextField(
                                    value = viewModel.password,
                                    onValueChange = { viewModel.password = it },
                                    placeholder = stringResource(id = R.string.password),
                                    isSecure = true,
                                    leadingIcon = Icons.Rounded.Lock,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,

                                ) {

                                CustomCheckBox(
                                    text = stringResource(id = R.string.remember_me),
                                    onValueChange = { viewModel.rememberMe = it }
                                )

                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(id = R.color.transparent),
                                    ),
                                    modifier = Modifier
                                        .width(180.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.recover_password),
                                        fontFamily = MontserratFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.main),
                                        textAlign = TextAlign.End,
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(vertical = 29.dp)
                            ) {

                                CustomButton(
                                    text = stringResource(id = R.string.login).uppercase(),
                                    onClick = { viewModel.login() }
                                )
                            }


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(color = colorResource(id = R.color.gray2))
                                )

                                Text(
                                    text = stringResource(id = R.string.connect_with),
                                    fontFamily = MontserratFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.gray1),
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(color = colorResource(id = R.color.gray2))
                                )
                            }

                            Spacer(modifier = Modifier.weight(1.0f))


                            Button(
                                onClick = {
                                    signInWithGoogle()
                                },
                                modifier = Modifier
                                    .width(133.dp)
                                    .height(45.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.button_google),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.weight(1.0f))

                            Button(
                                onClick = { onRegisterClicked() },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.transparent),
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        10.dp,
                                        Alignment.CenterHorizontally
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.no_account),
                                        fontFamily = MontserratFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.gray1),
                                    )

                                    Text(
                                        text = stringResource(id = R.string.register_here),
                                        fontFamily = MontserratFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.main),
                                    )
                                }
                            }


                        }
                    }
                }


            }
        }
    }
}