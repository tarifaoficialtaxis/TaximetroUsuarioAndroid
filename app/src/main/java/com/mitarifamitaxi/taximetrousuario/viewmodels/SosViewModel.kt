package com.mitarifamitaxi.taximetrousuario.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.models.Contact
import com.mitarifamitaxi.taximetrousuario.models.DialogType
import com.mitarifamitaxi.taximetrousuario.models.ItemImageButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class SosViewModel(context: Context, private val appViewModel: AppViewModel) : ViewModel() {

    private val appContext = context.applicationContext

    var dialogType by mutableStateOf(DialogType.SUCCESS)
    var showDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("")
    var dialogMessage by mutableStateOf("")
    var dialogShowCloseButton by mutableStateOf(true)
    var dialogPrimaryAction: String? by mutableStateOf(null)

    var showContactDialog by mutableStateOf(false)

    val contactObj = mutableStateOf(Contact())
    var itemSelected: ItemImageButton? = null

    init {
        Handler(Looper.getMainLooper()).postDelayed({
            showCustomDialog(
                DialogType.WARNING,
                appContext.getString(R.string.warning),
                appContext.getString(R.string.article_35_message),
                appContext.getString(R.string.confirm),
                showCloseButton = false
            )
        }, 700)

        getCityContacts(appViewModel.userData?.city)
    }

    private fun showCustomDialog(
        type: DialogType,
        title: String,
        message: String,
        primaryAction: String? = null,
        showCloseButton: Boolean = true
    ) {
        showDialog = true
        dialogType = type
        dialogTitle = title
        dialogMessage = message
        dialogPrimaryAction = primaryAction
        dialogShowCloseButton = showCloseButton
    }

    private fun getCityContacts(userCity: String?) {

        viewModelScope.launch {
            if (userCity != null) {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val ratesQuerySnapshot = withContext(Dispatchers.IO) {
                        firestore.collection("contacts")
                            .whereEqualTo("city", userCity)
                            .get()
                            .await()
                    }

                    if (!ratesQuerySnapshot.isEmpty) {
                        val contactsDoc = ratesQuerySnapshot.documents[0]
                        try {
                            contactObj.value =
                                contactsDoc.toObject(Contact::class.java) ?: Contact()
                        } catch (e: Exception) {
                            showCustomDialog(
                                DialogType.ERROR,
                                appContext.getString(R.string.something_went_wrong),
                                appContext.getString(R.string.general_error)
                            )
                        }
                    } else {
                        showCustomDialog(
                            DialogType.ERROR,
                            appContext.getString(R.string.something_went_wrong),
                            appContext.getString(R.string.error_no_contacts_found)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("SosViewModel", "Error fetching contacts: ${e.message}")
                    showCustomDialog(
                        DialogType.ERROR,
                        appContext.getString(R.string.something_went_wrong),
                        appContext.getString(R.string.general_error)
                    )
                }
            } else {
                showCustomDialog(
                    DialogType.ERROR,
                    appContext.getString(R.string.something_went_wrong),
                    appContext.getString(R.string.error_no_city_set)
                )
            }
        }


    }

    fun validateSendMessageAction(onIntentReady: (Intent) -> Unit) {

        when (itemSelected?.id) {
            "POLICE" -> {
                sendWhatsappMessage(
                    contactObj.value.policeNumber ?: "",
                    appContext.getString(R.string.police)
                ) { intent ->
                    onIntentReady(intent)
                }
            }

            "FIRE_FIGHTERS" -> {
                sendWhatsappMessage(
                    contactObj.value.firefightersNumber ?: "",
                    appContext.getString(R.string.fire_fighters)
                ) { intent ->
                    onIntentReady(intent)
                }

            }

            "AMBULANCE" -> {
                sendWhatsappMessage(
                    contactObj.value.ambulanceNumber ?: "",
                    appContext.getString(R.string.ambulance)
                ) { intent ->
                    onIntentReady(intent)
                }
            }

            "ANIMAL_CARE" -> {
                sendWhatsappMessage(
                    contactObj.value.animalCareNumber ?: "",
                    appContext.getString(R.string.animal_care),
                    appContext.getString(R.string.sos_animal_care)
                ) { intent ->
                    onIntentReady(intent)
                }
            }

            "SUPPORT" -> {

                if (appViewModel.userData?.supportNumber != null) {
                    sendWhatsappMessage(
                        appViewModel.userData?.supportNumber ?: "",
                        appContext.getString(R.string.support),
                    ) { intent ->
                        onIntentReady(intent)
                    }
                } else {
                    showCustomDialog(
                        DialogType.WARNING,
                        appContext.getString(R.string.support_number_not_found),
                        appContext.getString(R.string.set_up_support_number),
                        appContext.getString(R.string.add_number),
                    )
                }
            }

            "FAMILY" -> {
                if (appViewModel.userData?.familyNumber != null) {
                    sendWhatsappMessage(
                        appViewModel.userData?.familyNumber ?: "",
                        appContext.getString(R.string.family),
                    ) { intent ->
                        onIntentReady(intent)
                    }
                } else {
                    showCustomDialog(
                        DialogType.WARNING,
                        appContext.getString(R.string.family_number_not_found),
                        appContext.getString(R.string.set_up_family_number),
                        appContext.getString(R.string.add_number),
                    )
                }
            }

        }

    }

    fun validateCallAction(onIntentReady: (Intent) -> Unit) {

        when (itemSelected?.id) {
            "POLICE" -> {
                buildIntentCall(contactObj.value.policeNumber ?: "") { intent ->
                    onIntentReady(intent)
                }
            }

            "FIRE_FIGHTERS" -> {
                buildIntentCall(contactObj.value.firefightersNumber ?: "") { intent ->
                    onIntentReady(intent)
                }
            }

            "AMBULANCE" -> {
                buildIntentCall(contactObj.value.ambulanceNumber ?: "") { intent ->
                    onIntentReady(intent)
                }
            }

            "ANIMAL_CARE" -> {
                buildIntentCall(contactObj.value.animalCareNumber ?: "") { intent ->
                    onIntentReady(intent)
                }
            }

            "SUPPORT" -> {
                if (appViewModel.userData?.supportNumber != null) {
                    buildIntentCall(appViewModel.userData?.supportNumber ?: "") { intent ->
                        onIntentReady(intent)
                    }
                } else {
                    showCustomDialog(
                        DialogType.WARNING,
                        appContext.getString(R.string.support_number_not_found),
                        appContext.getString(R.string.set_up_support_number),
                        appContext.getString(R.string.add_number),
                    )
                }
            }

            "FAMILY" -> {
                if (appViewModel.userData?.familyNumber != null) {
                    buildIntentCall(appViewModel.userData?.familyNumber ?: "") { intent ->
                        onIntentReady(intent)
                    }
                } else {
                    showCustomDialog(
                        DialogType.WARNING,
                        appContext.getString(R.string.family_number_not_found),
                        appContext.getString(R.string.set_up_family_number),
                        appContext.getString(R.string.add_number),
                    )
                }
            }

        }

    }


    private fun sendWhatsappMessage(
        phoneNumber: String,
        sosType: String,
        event: String? = null,
        onIntentReady: (Intent) -> Unit
    ) {

        val userLocation = appViewModel.userData?.location
        val message = buildString {
            append("*SOS ${sosType.uppercase()}*\n")
            if (event != null) {
                append("*${event}:*\n")
            } else {
                append("*${appContext.getString(R.string.this_is_my_location)}:*\n")
            }
            append("https://maps.google.com/?q=${userLocation?.latitude},${userLocation?.longitude}\n")
        }

        val messageToSend = URLEncoder.encode(message, "UTF-8").replace("%0A", "%0D%0A")
        val whatsappURL =
            "whatsapp://send?text=$messageToSend&phone=${appViewModel.userData?.countryCodeWhatsapp}${phoneNumber}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(whatsappURL)
        }

        if (intent.resolveActivity(appContext.packageManager) != null) {
            onIntentReady(intent)
        } else {
            showCustomDialog(
                DialogType.ERROR,
                appContext.getString(R.string.something_went_wrong),
                appContext.getString(R.string.whatsapp_not_installed)
            )
        }

    }

    private fun buildIntentCall(phoneNumber: String, onIntentReady: (Intent) -> Unit) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber}")
        }
        onIntentReady(intent)
    }

}

class SosViewModelFactory(
    private val context: Context,
    private val appViewModel: AppViewModel
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SosViewModel::class.java)) {
            return SosViewModel(context, appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}