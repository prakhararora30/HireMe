package com.example.hireme

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.UserRole
import com.example.hireme.ui.auth.LoginScreen
import com.example.hireme.ui.auth.RegisterScreen
import com.example.hireme.ui.home.HomeScreen
import com.example.hireme.ui.home.HomeCategoryItem
import com.example.hireme.ui.landing.LandingScreen
import com.example.hireme.ui.payment.PaymentScreen
import com.example.hireme.ui.rating.RatingScreen
import com.example.hireme.ui.request.CreateRequestScreen
import com.example.hireme.ui.request.RequestConfirmationScreen
import com.example.hireme.ui.subservice.SubServiceScreen
import com.example.hireme.ui.worker.WorkerHomeScreen

object Routes {
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CUSTOMER_HOME = "customer_home"
    const val WORKER_HOME = "worker_home"
    const val SUB_SERVICES = "sub_services/{categoryId}/{categoryName}"
    const val CREATE_REQUEST = "create_request/{subServiceId}/{categoryId}"
    const val REQUEST_CONFIRMATION = "request_confirmation/{requestId}"
    const val RATING = "rating/{requestId}"
    const val PAYMENT = "payment/{requestId}"

    fun subServices(categoryId: String, categoryName: String) =
        "sub_services/$categoryId/$categoryName"

    fun createRequest(subServiceId: String, categoryId: String) =
        "create_request/$subServiceId/$categoryId"

    fun requestConfirmation(requestId: String) =
        "request_confirmation/$requestId"

    fun rating(requestId: String) =
        "rating/$requestId"

    fun payment(requestId: String) =
        "payment/$requestId"
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LANDING
    ) {
        // ── 1. Landing Screen ────────────────────────────────────────
        composable(Routes.LANDING) {
            LandingScreen(
                onGetStartedClick = { navController.navigate(Routes.LOGIN) },
                onSignInClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        // ── 2. Login Screen ──────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { role ->
                    val destination = when (role) {
                        UserRole.CUSTOMER -> Routes.CUSTOMER_HOME
                        UserRole.WORKER -> Routes.WORKER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── 3. Register Screen ───────────────────────────────────────
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { _, _, role ->
                    val destination = when (role) {
                        UserRole.CUSTOMER -> Routes.CUSTOMER_HOME
                        UserRole.WORKER -> Routes.WORKER_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── 4. Customer Home (Categories) ────────────────────────────
        composable(Routes.CUSTOMER_HOME) {
            HomeScreen(
                onServiceClick = { category ->
                    navController.navigate(Routes.subServices(category.id, category.title))
                },
                onSubServiceClick = { subService ->
                    navController.navigate(Routes.createRequest(subService.id, subService.categoryId))
                },
                onRequestClick = { request ->
                    when (request.status) {
                        com.example.hireme.data.RequestStatus.COMPLETED ->
                            navController.navigate(Routes.rating(request.id))
                        com.example.hireme.data.RequestStatus.RATED ->
                            navController.navigate(Routes.payment(request.id))
                        else -> { /* No action for other statuses */ }
                    }
                },
                onLogout = {
                    RepositoryProvider.repository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── 5. Worker Home ───────────────────────────────────────────
        composable(Routes.WORKER_HOME) {
            HomeScreen(
                onServiceClick = { category ->
                    navController.navigate(Routes.subServices(category.id, category.title))
                },
                onSubServiceClick = { subService ->
                    navController.navigate(Routes.createRequest(subService.id, subService.categoryId))
                },
                onRequestClick = { request ->
                    when (request.status) {
                        com.example.hireme.data.RequestStatus.COMPLETED ->
                            navController.navigate(Routes.rating(request.id))
                        com.example.hireme.data.RequestStatus.RATED ->
                            navController.navigate(Routes.payment(request.id))
                        else -> { /* No action for other statuses */ }
                    }
                },
                onLogout = {
                    RepositoryProvider.repository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── 6. Sub-Services List ─────────────────────────────────────
        composable(
            route = Routes.SUB_SERVICES,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            SubServiceScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onSubServiceClick = { subService ->
                    navController.navigate(Routes.createRequest(subService.id, categoryId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── 7. Create Request ────────────────────────────────────────
        composable(
            route = Routes.CREATE_REQUEST,
            arguments = listOf(
                navArgument("subServiceId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subServiceId = backStackEntry.arguments?.getString("subServiceId") ?: ""
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CreateRequestScreen(
                subServiceId = subServiceId,
                categoryId = categoryId,
                onRequestCreated = { requestId ->
                    navController.navigate(Routes.requestConfirmation(requestId)) {
                        popUpTo(Routes.CUSTOMER_HOME)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── 8. Request Confirmation ──────────────────────────────────
        composable(
            route = Routes.REQUEST_CONFIRMATION,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            RequestConfirmationScreen(
                requestId = requestId,
                onBackToHome = {
                    navController.navigate(Routes.CUSTOMER_HOME) {
                        popUpTo(Routes.CUSTOMER_HOME) { inclusive = true }
                    }
                }
            )
        }

        // ── 9. Rating Screen ─────────────────────────────────────────
        composable(
            route = Routes.RATING,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            RatingScreen(
                requestId = requestId,
                onRatingSubmitted = {
                    navController.navigate(Routes.payment(requestId)) {
                        popUpTo(Routes.CUSTOMER_HOME)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── 10. Payment Screen ───────────────────────────────────────
        composable(
            route = Routes.PAYMENT,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            PaymentScreen(
                requestId = requestId,
                onPaymentComplete = {
                    navController.navigate(Routes.CUSTOMER_HOME) {
                        popUpTo(Routes.CUSTOMER_HOME) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
