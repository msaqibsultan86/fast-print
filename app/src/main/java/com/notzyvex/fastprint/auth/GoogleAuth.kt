package com.notzyvex.fastprint.auth

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.notzyvex.fastprint.state.UserProfile

private const val TAG = "GoogleAuth"

sealed interface SignInResult {
    data class Success(val profile: UserProfile, val idToken: String) : SignInResult
    data object Cancelled : SignInResult
    data object NoAccounts : SignInResult
    data class Error(val message: String) : SignInResult
    /** GOOGLE_WEB_CLIENT_ID was never set — see SETUP.md. */
    data object NotConfigured : SignInResult
}

/**
 * Sign in with Google through Credential Manager.
 *
 * The server client ID is injected at build time from local.properties, so no client ID or
 * secret is committed. The returned profile is the real Google account's name/email/photo.
 */
class GoogleAuth(private val webClientId: String) {

    val isConfigured: Boolean get() = webClientId.isNotBlank()

    suspend fun signIn(activity: Activity): SignInResult {
        if (!isConfigured) return SignInResult.NotConfigured

        val option = GetGoogleIdOption.Builder()
            // false so a first-time user still sees their accounts rather than an empty sheet
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(activity)
                .getCredential(activity, request)
            parse(response.credential)
        } catch (e: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (e: NoCredentialException) {
            SignInResult.NoAccounts
        } catch (e: GetCredentialException) {
            Log.e(TAG, "credential request failed", e)
            SignInResult.Error(e.message ?: "Sign-in failed")
        }
    }

    private fun parse(credential: androidx.credentials.Credential): SignInResult {
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return SignInResult.Error("Unexpected credential type")
        }
        return try {
            val token = GoogleIdTokenCredential.createFrom(credential.data)
            SignInResult.Success(
                profile = UserProfile(
                    name = token.displayName ?: token.givenName,
                    email = token.id,
                    photoUrl = token.profilePictureUri?.toString(),
                ),
                idToken = token.idToken,
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "failed to parse Google ID token", e)
            SignInResult.Error("Could not read the Google account")
        }
    }
}
