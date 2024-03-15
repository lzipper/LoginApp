package com.example.loginapp.login

import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.loginapp.R
import com.example.loginapp.TodoNavigationActions
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    @StringRes topBarTitle: Int,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: LoginViewModel = hiltViewModel(),
    state: UIStateTest = UIStateTest(),
    navActions: TodoNavigationActions
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let {error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    viewModel.signInWithIntent(intent = result.data ?: return@launch)
                    //viewModel.onGoogleLoginResult()
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        val uiState by viewModel.authUiState.collectAsState()

        when (uiState) {
            is AuthUiState.Base -> {
                ColumnWithFiveElements(
                    onFacebookLoginClicked = { viewModel.selectFacebookLogin() },
                    onGoogleLoginClicked = { viewModel.selectGoogleLogin() },
                    onPhoneNumberLoginClicked = { viewModel.selectPhoneLogin() }
                )
            }
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Telefon -> {
                LoginPhoneScreen(
                    onLoginClicked = { phoneNumber -> viewModel.signInWithPhoneNumber(phoneNumber) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is AuthUiState.Facebook -> {

            }
            is AuthUiState.Google -> {
                LaunchedEffect(key1 = viewModel.pendingIntent.collectAsState()) {
                    lifecycleScope.launch {
                        val loginIntentSender = viewModel.pendingIntent.value
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                intentSender = loginIntentSender ?: return@launch
                            ).build()
                        )
                    }
                }
            }
            is AuthUiState.Send -> {
                PinVerificationScreen(
                    onVerifyClicked = { pincode -> viewModel.verifyVerificationCode(pincode) }
                )
            }
            is AuthUiState.Success -> {
                navActions.navigateToMain()
            }
            is AuthUiState.Error -> {
                Text(text = (uiState as AuthUiState.Error).message ?: "ERROR")
            }
        }

    }
}

@Preview
@Composable
private fun ColumnPrev() {
    ColumnWithFiveElements({},{},{})
}

@Composable
fun ColumnWithFiveElements(
    onGoogleLoginClicked: () -> Unit,
    onFacebookLoginClicked: () -> Unit,
    onPhoneNumberLoginClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Dating App Logo")
            Text(text = "App Motto")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginButtons(
                onGoogleLoginClicked,
                onFacebookLoginClicked,
                onPhoneNumberLoginClicked
            )
        }
    }
}

@Composable
fun LoginButtons(
    onGoogleLoginClicked: () -> Unit,
    onFacebookLoginClicked: () -> Unit,
    onPhoneNumberLoginClicked: () -> Unit,
) {
    Button(
        onClick = {
            onGoogleLoginClicked()
        },
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_done),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login with Google")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Facebook Login Button
    Button(
        onClick = { onFacebookLoginClicked() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Facebook Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login with Facebook")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Phone Number Login Button
    Button(
        onClick = { onPhoneNumberLoginClicked() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Phone Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login with Phone Number")
        }
    }
}

@Composable
fun LoginPhoneScreen(
    onLoginClicked: (String) -> Unit,
    modifier: Modifier
) {
    var countryCode by remember { mutableStateOf("+49") }
    var phoneNumber by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize(),
            //.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val countryCodes = listOf("+49", "+1", "+2")

        CountryCodeDropdown(
            countryCodes = countryCodes,
            onDismiss = { expanded = false },
            onCountryCodeSelected = { code ->
                countryCode = code
                //onCountryCodeSelected(code)
                expanded = false
            },
            expanded = expanded,
            icon = Icons.Filled.Clear,
            text = countryCode
        )


        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Telefonnummer") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Text(text = countryCode, Modifier.clickable { expanded = true })
                //IconButton(
                //    onClick = { expanded = true }
                //) {
                //    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                //}
            },
        )

        Button(
            onClick = { onLoginClicked("$countryCode$phoneNumber") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Anmelden")
        }
    }
}

@Composable
fun CountryCodeDropdown(
    countryCodes: List<String>,
    onCountryCodeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    expanded: Boolean,
    text: String,
    icon: ImageVector,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        countryCodes.forEach { code ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(imageVector = icon, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = code)
                    }
                },
                onClick = { onCountryCodeSelected(code) }
            )
        }
    }
}

@Composable
fun PinVerificationScreen(
    onVerifyClicked: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bitte geben Sie Ihre PIN ein:",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            interactionSource = interactionSource,
        )

        Button(
            onClick = { onVerifyClicked(pin) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Verifizieren")
        }
    }
}
