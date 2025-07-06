package com.example.syncwell.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.syncwell.ui.auth.LoginScreen
import com.example.syncwell.ui.auth.RegisterScreen
import com.example.syncwell.ui.auth.ForgotPasswordScreen
import com.example.syncwell.ui.profile.ProfileScreen
import com.example.syncwell.ui.medicines.MedicineListScreen
import com.example.syncwell.ui.medicines.AddEditMedicineScreen
import com.example.syncwell.ui.tasks.TaskListScreen
import com.example.syncwell.ui.tasks.AddEditTaskScreen
import com.example.syncwell.ui.viewmodel.UserViewModel
import com.example.syncwell.ui.wellness.WellnessScreen
import com.example.syncwell.ui.dashboard.DashboardScreen
import androidx.hilt.navigation.compose.hiltViewModel

// All the routes in the app
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object MedicineList : Screen("medicine_list")
    object AddEditMedicine : Screen("add_edit_medicine/{medicineId}") {
        fun createRoute(medicineId: String? = null) = "add_edit_medicine/${medicineId ?: "new"}"
    }
    object TaskList : Screen("task_list")
    object AddEditTask : Screen("add_edit_task/{taskId}") {
        fun createRoute(taskId: String? = null) = "add_edit_task/${taskId ?: "new"}"
    }
    object Wellness : Screen("wellness")
    object Profile : Screen("profile")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel,
    startDestination: String = Screen.Login.route
) {
    val authState by userViewModel.authState.collectAsState()

    // Monitor auth state and redirect accordingly
    LaunchedEffect(authState) {
        when (authState) {
            is UserViewModel.AuthState.SignedIn -> {
                // Navigate to dashboard if the user is signed in and we're on an auth screen
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Screen.Login.route || 
                    currentRoute == Screen.Register.route || 
                    currentRoute == Screen.ForgotPassword.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is UserViewModel.AuthState.SignedOut -> {
                // Navigate to login if the user is signed out and we're not on an auth screen
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != Screen.Login.route && 
                    currentRoute != Screen.Register.route && 
                    currentRoute != Screen.ForgotPassword.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> { /* No action for loading or error states */ }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // Authentication flows
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                userViewModel = userViewModel
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }},
                userViewModel = userViewModel
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                userViewModel = userViewModel
            )
        }
        
        // Main app screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToMedicines = { navController.navigate(Screen.MedicineList.route) },
                onNavigateToTasks = { navController.navigate(Screen.TaskList.route) },
                onNavigateToWellness = { navController.navigate(Screen.Wellness.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                userViewModel = userViewModel,
                taskViewModel = hiltViewModel(),
                medicineViewModel = hiltViewModel(),
                wellnessViewModel = hiltViewModel()
            )
        }
        
        composable(Screen.MedicineList.route) {
            MedicineListScreen(
                onNavigateToAddMedicine = { navController.navigate(Screen.AddEditMedicine.createRoute()) },
                onNavigateToEditMedicine = { medicineId -> 
                    navController.navigate(Screen.AddEditMedicine.createRoute(medicineId)) 
                },
                onNavigateBack = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }}
            )
        }
        
        composable(
            route = Screen.AddEditMedicine.route,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId")
            AddEditMedicineScreen(
                medicineId = if (medicineId == "new") null else medicineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onNavigateToAddTask = { navController.navigate(Screen.AddEditTask.createRoute()) },
                onNavigateToEditTask = { taskId -> 
                    navController.navigate(Screen.AddEditTask.createRoute(taskId)) 
                },
                onNavigateBack = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }}
            )
        }
        
        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddEditTaskScreen(
                taskId = if (taskId == "new") null else taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Wellness.route) {
            WellnessScreen(
                onNavigateBack = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }}
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    userViewModel.signOut()
                    // Navigation handled by auth state observer
                },
                onNavigateBack = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }},
                userViewModel = userViewModel
            )
        }
    }
} 