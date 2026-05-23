package com.example.tastetoshare.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.tastetoshare.screens.LoginRouteScreen
import com.example.tastetoshare.screens.SignUpScreen
import com.example.tastetoshare.viewmodels.LoginRegisterViewModel

fun NavGraphBuilder.authGraph(navActions: AppNavigationActions, navCtrl: NavController) {

    navigation<AuthGraph>(startDestination = WelcomeRoute) {
//        composable <WelcomeRoute> {
//            WelcomeDestination(
//                ,
//            )
//        }

//        composable<LoginRoute> {
//            val viewModel: LoginRegisterViewModel = viewModel(
//                factory = LoginRegisterViewModel.Factory
//            )
//            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//            LaunchedEffect(uiState.isLoginSuccessful) {
//                if (uiState.isLoginSuccessful) {
//                    navActions.navigateToHomeFromAuth()
//                }
//            }
//
//            LoginRouteScreen(
//                uiState = uiState,
//                onEmailChange = { viewModel.onEmailChanged(it) },
//                onPasswordChange = { viewModel.onPasswordChanged(it) },
//                onLoginClick = { viewModel.loginUser() },
//                onNavigateBack = { navActions.navigateBack() },
//                onForgetPassword = { /* TODO */ }
//            )
//        }

        composable<RegisterRoute> { backStackEntry ->
            val viewModel: LoginRegisterViewModel = viewModel(
                factory = LoginRegisterViewModel.Factory
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isSignUpSuccessful) {
                if (uiState.isSignUpSuccessful) {
                    navActions.navigateToHomeFromAuth()
                }
            }

            val returnedUri by backStackEntry.savedStateHandle.getStateFlow<String?>("returned_uri", null).collectAsStateWithLifecycle()
            LaunchedEffect(returnedUri) {
                val uri = returnedUri
                if (uri != null) {
                    viewModel.handlePhotoUri(uri)
                    backStackEntry.savedStateHandle["returned_uri"] = null
                }
            }

            SignUpScreen(
                uiState = uiState,
                onNavigateBack = { navActions.navigateBack() },
                onNameChange = { viewModel.onNameChange(it) },
                onUsernameChange = { viewModel.onUsernameChanged(it) },
                onEmailChange = { viewModel.onEmailChanged(it) },
                onPasswordChange = { viewModel.onPasswordChanged(it) },
                onBiographyChange = { viewModel.onBiographyChanged(it) },
                onRoleChange = { viewModel.onRoleChanged(it) },
                onPhotoSelectedFromGallery = { uri -> viewModel.onPhotoSelectedFromGallery(uri) },
                onCuisineSearchChange = { viewModel.setFieldSearchCuisineTypeTags(it) },
                onDietarySearchChange = { viewModel.setFieldSearchDietaryRestrictionTags(it) },
                onOtherSearchChange = { viewModel.setFieldSearchOtherTags(it) },
                onCuisineTagClick = { viewModel.toggleSelectedCuisineTag(it) },
                onDietaryTagClick = { viewModel.toggleSelectedDietaryTag(it) },
                onOtherTagClick = { viewModel.toggleSelectedOtherTag(it) },
                onCreateAccount = { viewModel.validateProfile() },
                onNavigateToPictureDialog = {isFrontCamera -> navActions.navigateToPictureDialog(isFrontCamera)}
            )
        }
    }
}