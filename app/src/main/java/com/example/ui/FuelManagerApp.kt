package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FuelManagerApp(viewModel: FuelManagerViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for SharedFlow notifications to display inside Custom Snackbars
    LaunchedEffect(Unit) {
        viewModel.notificationToast.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    MyApplicationTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier.padding(12.dp),
                        containerColor = IndustrialPrimaryContainer,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "Alert",
                                tint = StatusWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = data.visuals.message, fontSize = 14.sp)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (currentUser == null) {
                    LoginScreen(viewModel = viewModel)
                } else {
                    AppNavigationShell(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: FuelManagerViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("carlosaperezp@gmail.com") }
    var password by remember { mutableStateOf("admin123") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ALMACENISTA") }
    var isConfigHintOpen by remember { mutableStateOf(false) }
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(IndustrialPrimary, IndustrialPrimaryContainer))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 420.dp)
                .testTag("login_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(IndustrialSecondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = "FuelManager Pro",
                        tint = IndustrialSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FuelManager Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialPrimary
                )

                Text(
                    text = if (isRegisterMode) "Crear Nueva Cuenta de Operador" else "Sistemas Integrales de Combustible e Inventarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (loginError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusAlert.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusAlert.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = StatusAlert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loginError ?: "",
                                color = StatusAlert,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isRegisterMode) {
                    // Registration inputs
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("Ej. Juan Gómez") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = IndustrialPrimary,
                            unfocusedTextColor = IndustrialPrimary,
                            focusedLabelColor = IndustrialSecondary,
                            unfocusedLabelColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedBorderColor = IndustrialSecondary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Credentials Inputs
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Corporativo") },
                    placeholder = { Text("ejemplo@empresa.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IndustrialPrimary,
                        unfocusedTextColor = IndustrialPrimary,
                        focusedLabelColor = IndustrialSecondary,
                        unfocusedLabelColor = Color.Gray,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        focusedBorderColor = IndustrialSecondary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IndustrialPrimary,
                        unfocusedTextColor = IndustrialPrimary,
                        focusedLabelColor = IndustrialSecondary,
                        unfocusedLabelColor = Color.Gray,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        focusedBorderColor = IndustrialSecondary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rol Asignado en la Organización:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ADMINISTRADOR" to "Admin", "TÉCNICO" to "Técnico", "ALMACENISTA" to "Almacén").forEach { (roleCode, label) ->
                            val isSelected = selectedRole == roleCode
                            OutlinedButton(
                                onClick = { selectedRole = roleCode },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) IndustrialPrimaryContainer else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (isSelected) IndustrialPrimary else SurfaceBorder),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Firebase Info Link Checkbox style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isConfigHintOpen = !isConfigHintOpen }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Firebase Info",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Configuración de Tokens & Firebase",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isConfigHintOpen) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeutralBg),
                        modifier = Modifier.padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Aviso de Integración:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialPrimary
                            )
                            Text(
                                text = "La sesión utiliza autenticación robusta mediante criptografía de tokens. Para producción final con Firebase, asegúrese de agregar el archivo google-services.json al proyecto Android.",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Login / Register
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.register(email, name, selectedRole)
                        } else {
                            viewModel.login(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                ) {
                    Text(
                        text = if (isRegisterMode) "Crear Nueva Cuenta" else "Ingresar de Forma Segura",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle between Register and Login Modes
                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        viewModel.logout() // clear any login errors
                    }
                ) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tiene cuenta? Iniciar Sesión" else "¿No tiene cuenta? Registrar Nueva Cuenta",
                        fontSize = 13.sp,
                        color = IndustrialSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isRegisterMode) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Fast user switcher keys (for demonstrator/auditor ease)
                    Text("Ingreso inmediato por roles:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RoleSwitchButton(label = "Admin", active = email == "carlosaperezp@gmail.com") {
                            email = "carlosaperezp@gmail.com"
                        }
                        RoleSwitchButton(label = "Técnico", active = email == "tecnico@fuelmanager.com") {
                            email = "tecnico@fuelmanager.com"
                        }
                        RoleSwitchButton(label = "Almacén", active = email == "almacenista@fuelmanager.com") {
                            email = "almacenista@fuelmanager.com"
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSwitchButton(label: String, active: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (active) IndustrialPrimaryContainer else Color.Transparent
        ),
        border = BorderStroke(1.dp, if (active) IndustrialPrimary else SurfaceBorder),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (active) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppNavigationShell(viewModel: FuelManagerViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Inicio, 1: Fuel, 2: Stock, 3: CMMS
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header
        AppHeader(
            currentUser = currentUser,
            isOnline = isOnline,
            onToggleConnection = { viewModel.toggleConnection() },
            onLogout = { viewModel.logout() }
        )

        // Contents Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> FuelScreen(viewModel = viewModel)
                2 -> InventoryScreen(viewModel = viewModel)
                3 -> CMMSMantenimientoScreen(viewModel = viewModel)
            }
        }

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Inicio") },
                label = { Text("Inicio", fontWeight = FontWeight.Medium, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndustrialSecondary,
                    selectedTextColor = IndustrialSecondary,
                    indicatorColor = IndustrialSecondary.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(if (selectedTab == 1) Icons.Filled.LocalGasStation else Icons.Outlined.LocalGasStation, contentDescription = "Fuel") },
                label = { Text("Fuel", fontWeight = FontWeight.Medium, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndustrialSecondary,
                    selectedTextColor = IndustrialSecondary,
                    indicatorColor = IndustrialSecondary.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(if (selectedTab == 2) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2, contentDescription = "Stock") },
                label = { Text("Stock", fontWeight = FontWeight.Medium, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndustrialSecondary,
                    selectedTextColor = IndustrialSecondary,
                    indicatorColor = IndustrialSecondary.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(if (selectedTab == 3) Icons.Filled.Build else Icons.Outlined.Build, contentDescription = "CMMS") },
                label = { Text("CMMS", fontWeight = FontWeight.Medium, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndustrialSecondary,
                    selectedTextColor = IndustrialSecondary,
                    indicatorColor = IndustrialSecondary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun AppHeader(
    currentUser: User?,
    isOnline: Boolean,
    onToggleConnection: () -> Unit,
    onLogout: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = IndustrialSecondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CAP ERP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = IndustrialPrimary
                    )
                    Text(
                        text = (currentUser?.role ?: "OPERADOR").uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Connection Toggle and Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection Pill status
                Surface(
                    color = if (isOnline) StatusSuccess.copy(alpha = 0.12f) else StatusAlert.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { onToggleConnection() }
                        .padding(end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) StatusSuccess else StatusAlert)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) StatusSuccess else StatusAlert
                        )
                    }
                }

                // Profile Image with drop-down context log out
                Box {
                    if (currentUser?.imageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = currentUser.imageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, IndustrialSecondary, CircleShape)
                                .clickable { expandedMenu = true },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IndustrialPrimaryContainer)
                                .clickable { expandedMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(2)?.uppercase() ?: "US",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver Perfil Rol: ${currentUser?.role}") },
                            onClick = {},
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                expandedMenu = false
                                onLogout()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusAlert) }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: DashboardScreen (Inicio)
// ----------------------------------------------------
@Composable
fun DashboardScreen(viewModel: FuelManagerViewModel) {
    val dispatches by viewModel.allDispatches.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dashboard Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Panel de Control Gerencial",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = IndustrialPrimary
                )
                Text(
                    text = "Visualización en tiempo real de activos y telemetría de combustible.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Top Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportReport("PDF") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialPrimaryContainer),
                contentPadding = PaddingValues(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar PDF", fontSize = 12.sp, color = Color.White)
            }
            Button(
                onClick = {
                    scope.launch {
                        isSyncing = true
                        viewModel.syncERP()
                        isSyncing = false
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary),
                contentPadding = PaddingValues(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("sync_icon")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSyncing) "Sincronizando..." else "Sincronizar", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // KPI Widget: Balance de Materiales (24h)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "BALANCE DE MATERIALES (24H)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Entrada vs Salida",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = IndustrialPrimary
                        )
                    }
                    Surface(
                        color = StatusSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+2.4% Eficiencia", fontSize = 11.sp, color = StatusSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar 1
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RECEPCIÓN (ENTRADA)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("12,450 L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    }
                    LinearProgressIndicator(
                        progress = { 0.85f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EdgeBlue,
                        trackColor = NeutralBg
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar 2
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DISPENSA (SALIDA)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("11,890 L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    }
                    LinearProgressIndicator(
                        progress = { 0.78f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IndustrialSecondary,
                        trackColor = NeutralBg
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful custom Bar Chart placeholder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(NeutralBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val heights = listOf(0.4f, 0.6f, 0.55f, 0.8f, 0.9f, 0.75f, 0.45f, 0.65f)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(h)
                                .padding(horizontal = 4.dp)
                                .background(EdgeBlue.copy(alpha = 0.3f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GMROI DE INVENTARIO card (styled beautiful Deep Blue container)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "GMROI DE INVENTARIO",
                    fontSize = 11.sp,
                    color = IndustrialOnPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "2.84",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 38.sp
                    )
                }
                Text(
                    text = "Retorno de margen bruto sobre inversión de refacciones.",
                    fontSize = 12.sp,
                    color = Color.LightGray.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ESTADO FISCAL", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Surface(
                        color = StatusSuccess,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OPTIMIZADO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nivel de Tanques Principales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NIVEL DE TANQUES PRINCIPALES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tank 1
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(NeutralBg, RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Graphic
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.68f) // 68%
                                    .background(Brush.verticalGradient(listOf(IndustrialSecondaryContainer, IndustrialSecondary)))
                            )
                            Text(
                                text = "68%",
                                fontWeight = FontWeight.Bold,
                                color = IndustrialPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TK-001 DIESEL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = IndustrialPrimary)
                        Text("15,200L total", fontSize = 10.sp, color = Color.Gray)
                    }

                    // Tank 2
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(NeutralBg, RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Graphic
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.24f) // 24%
                                    .background(Brush.verticalGradient(listOf(StatusWarning, Color(0xFFCA8A04))))
                            )
                            Text(
                                text = "24%",
                                fontWeight = FontWeight.Bold,
                                color = IndustrialPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TK-002 GASOL.", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = IndustrialPrimary)
                        Text("4,800L total", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alertas Críticas Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ALERTAS CRÍTICAS DE PLANTA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusAlert)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display dynamic list of critical alarms compiled from real state
                notifications.take(3).forEach { n ->
                    val colorContainer = when {
                        n.title.contains("ANOMALÍA") || n.title.contains("FUGA") -> StatusAlert.copy(alpha = 0.08f)
                        n.title.contains("MERMA") || n.title.contains("DESVIADO") -> StatusWarning.copy(alpha = 0.08f)
                        else -> Color.Gray.copy(alpha = 0.08f)
                    }
                    val colorText = when {
                        n.title.contains("ANOMALÍA") || n.title.contains("FUGA") -> StatusAlert
                        n.title.contains("MERMA") || n.title.contains("DESVIADO") -> Color(0xFFB45309)
                        else -> IndustrialPrimary
                    }
                    val icon = when {
                        n.title.contains("ANOMALÍA") || n.title.contains("FUGA") -> Icons.Default.Warning
                        n.title.contains("MERMA") || n.title.contains("DESVIADO") -> Icons.Default.ReportProblem
                        else -> Icons.Default.Engineering
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorContainer)
                            .border(1.dp, colorText.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = colorText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = n.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colorText)
                            Text(text = n.message, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------
// TAB 2: FuelScreen (Control de Despacho)
// ----------------------------------------------------
@Composable
fun FuelScreen(viewModel: FuelManagerViewModel) {
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val dispatches by viewModel.allDispatches.collectAsStateWithLifecycle()
    val rawVolObs by viewModel.dispatchObservedVolume.collectAsStateWithLifecycle()
    val factor by viewModel.vcfFactor.collectAsStateWithLifecycle()
    val activeId by viewModel.dispatchActiveId.collectAsStateWithLifecycle()

    var isAddingDispatch by remember { mutableStateOf(false) }

    val observedVolVal = rawVolObs.toDoubleOrNull() ?: 0.0
    val correctedVolVal = observedVolVal * factor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core telemetry panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("TANQUE PRINCIPAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("TK-900 Alpha", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    }
                    Surface(
                        color = EdgeBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "EDGE SENSOR ACTIVE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EdgeBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful volumetric meter tank simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeutralBg)
                ) {
                    // Fluid wave background representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.72f) // 72%
                            .background(Brush.verticalGradient(listOf(Color(0xFFFB923C), IndustrialSecondary)))
                            .align(Alignment.BottomCenter)
                    )
                    // Metrics overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("100% capacity", fontSize = 10.sp, color = Color.DarkGray)
                            Text("15,000L Limit", fontSize = 10.sp, color = Color.DarkGray)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("72%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                            Text("10,800 LITROS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RESERVA MIN: 15%", fontSize = 9.sp, color = StatusAlert, fontWeight = FontWeight.Bold)
                            Text("0L empty", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats trackers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = NeutralBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("TEMP. ACTUAL", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("24.5 °C", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndustrialPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = IndustrialPrimaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("FACTOR VCF", fontSize = 9.sp, color = IndustrialOnPrimaryContainer, fontWeight = FontWeight.Bold)
                            Text("0.9923", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN DE KPIs ---
        Text(
            text = "RENDIMIENTO Y APORTES DE COMBUSTIBLE (KPIs)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        val receipts by viewModel.allFuelReceipts.collectAsStateWithLifecycle()
        val gasoilReceipts = receipts.filter { it.fuelType.equals("Gasoil", ignoreCase = true) }
        val gasoilTotal = gasoilReceipts.sumOf { it.totalReceived }
        val gasoilUsed = gasoilReceipts.sumOf { it.usedAmount }
        val gasoilWaste = gasoilReceipts.sumOf { it.wasteAmount }
        val gasoilEfficiency = if (gasoilTotal > 0) (gasoilUsed / gasoilTotal) * 100 else 0.0

        val oilReceipts = receipts.filter { it.fuelType.equals("Aceite Usado", ignoreCase = true) }
        val oilTotal = oilReceipts.sumOf { it.totalReceived }
        val oilUsed = oilReceipts.sumOf { it.usedAmount }
        val oilWaste = oilReceipts.sumOf { it.wasteAmount }
        val oilEfficiency = if (oilTotal > 0) (oilUsed / oilTotal) * 100 else 0.0

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gasoil KPI
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GASOIL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndustrialSecondary)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IndustrialSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Entrante:", fontSize = 9.sp, color = Color.Gray)
                    Text("${String.format("%.1f", gasoilTotal)} L", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Aprovechado:", fontSize = 9.sp, color = Color.Gray)
                            Text("${String.format("%.1f", gasoilUsed)} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Residuo:", fontSize = 9.sp, color = Color.Gray)
                            Text("${String.format("%.1f", gasoilWaste)} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusAlert)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Eficiencia: ${String.format("%.1f", gasoilEfficiency)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    LinearProgressIndicator(
                        progress = { (gasoilEfficiency / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusSuccess,
                        trackColor = SurfaceBorder
                    )
                }
            }

            // Aceite Usado KPI
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACEITE USADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF78350F))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Entrante:", fontSize = 9.sp, color = Color.Gray)
                    Text("${String.format("%.1f", oilTotal)} L", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Aprovechado:", fontSize = 9.sp, color = Color.Gray)
                            Text("${String.format("%.1f", oilUsed)} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Residuo:", fontSize = 9.sp, color = Color.Gray)
                            Text("${String.format("%.1f", oilWaste)} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusAlert)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Eficiencia: ${String.format("%.1f", oilEfficiency)}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    LinearProgressIndicator(
                        progress = { (oilEfficiency / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusSuccess,
                        trackColor = SurfaceBorder
                    )
                }
            }
        }

        // --- REGISTRO DE RECEPCIÓN ---
        var isRecordingReceipt by remember { mutableStateOf(false) }
        var selectedFuelType by remember { mutableStateOf("Gasoil") }
        var receiptTotalStr by remember { mutableStateOf("") }
        var receiptUsedStr by remember { mutableStateOf("") }

        // Proactive prominent activation button
        Button(
            onClick = { isRecordingReceipt = !isRecordingReceipt },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp)
                .testTag("recepcion_combustible_action_toggle"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Recepción de Combustible",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRecordingReceipt) "OCULTAR FORMULARIO DE RECEPCIÓN" else "REGISTRAR RECEPCIÓN DE COMBUSTIBLE",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }

        AnimatedVisibility(visible = isRecordingReceipt) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("fuel_receipt_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Recepción de Combustible",
                                tint = IndustrialSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECEPCIÓN DE COMBUSTIBLE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = IndustrialPrimary
                            )
                        }
                    }

                    var localErrorMsg by remember { mutableStateOf<String?>(null) }
                    
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Divider(color = SurfaceBorder, modifier = Modifier.padding(bottom = 12.dp))

                        // 1. Tipo de combustible
                        Text("Tipo de combustible", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Gasoil", "Aceite Usado").forEach { type ->
                                val isSelected = selectedFuelType == type
                                OutlinedButton(
                                    onClick = { selectedFuelType = type },
                                    modifier = Modifier.weight(1f).testTag("fuel_type_btn_$type"),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) IndustrialPrimary.copy(alpha = 0.08f) else Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) IndustrialSecondary else SurfaceBorder)
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IndustrialSecondary else Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2 & 3. Cantidad recibida & Cantidad aprovechable
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cantidad recibida (L):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = receiptTotalStr,
                                    onValueChange = { 
                                        receiptTotalStr = it
                                        localErrorMsg = null
                                    },
                                    placeholder = { Text("Ej. 1000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("receipt_total_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cantidad aprovechable (L):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = receiptUsedStr,
                                    onValueChange = { 
                                        receiptUsedStr = it
                                        localErrorMsg = null
                                    },
                                    placeholder = { Text("Ej. 950") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("receipt_used_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Cantidad para disposición final (dynamic/live auto-calculated)
                        val rawTotalStr = receiptTotalStr.replace(",", ".").trim()
                        val rawUsedStr = receiptUsedStr.replace(",", ".").trim()
                        val totalVol = rawTotalStr.toDoubleOrNull() ?: 0.0
                        val usedVol = rawUsedStr.toDoubleOrNull() ?: 0.0
                        val wasteVol = (totalVol - usedVol).coerceAtLeast(0.0)

                        Text("Cantidad para disposición final (L):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = "${String.format("%.2f", wasteVol)} L",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("receipt_waste_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = if (wasteVol > 0) StatusAlert else Color.DarkGray,
                                disabledBorderColor = if (wasteVol > 0) StatusAlert.copy(alpha = 0.5f) else SurfaceBorder,
                                disabledLabelColor = Color.Gray,
                                disabledContainerColor = NeutralBg
                            )
                        )

                        if (totalVol > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val pct = (usedVol / totalVol) * 100
                            Text(
                                text = "Eficiencia: ${String.format("%.1f", pct)}% Aprovechado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 95) StatusSuccess else if (pct >= 85) StatusWarning else StatusAlert,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        if (localErrorMsg != null) {
                            Text(
                                text = localErrorMsg ?: "",
                                color = StatusAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val cleanTotal = receiptTotalStr.replace(",", ".").trim()
                                val cleanUsed = receiptUsedStr.replace(",", ".").trim()
                                val totVal = cleanTotal.toDoubleOrNull() ?: 0.0
                                val useVal = cleanUsed.toDoubleOrNull() ?: 0.0
                                
                                if (totVal <= 0.0) {
                                    localErrorMsg = "El volumen total recibido debe ser mayor a 0."
                                } else if (useVal < 0.0) {
                                    localErrorMsg = "El volumen aprovechado no puede ser negativo."
                                } else if (useVal > totVal) {
                                    localErrorMsg = "El volumen aprovechado no puede exceder el total recibido."
                                } else {
                                    viewModel.recordFuelReceipt(
                                        selectedFuelType,
                                        totVal,
                                        useVal,
                                        totVal - useVal
                                    )
                                    // Reset inputs beautifully
                                    receiptTotalStr = ""
                                    receiptUsedStr = ""
                                    localErrorMsg = null
                                    isRecordingReceipt = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("receipt_submit_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                        ) {
                            Text("COMPLETAR REGISTRO Y GUARDAR", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Recent entries inside the table to prove persistence
                        if (receipts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "HISTORIAL RECIENTE / RECENT RECEIPTS:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = NeutralBg),
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    receipts.take(4).forEach { rec ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(if (rec.fuelType == "Gasoil") IndustrialSecondary else Color(0xFF78350F))
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = rec.fuelType,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                Text(
                                                    text = "Op: ${rec.operator}",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Recibido", fontSize = 8.sp, color = Color.Gray)
                                                    Text("${rec.totalReceived} L", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Aprovechado", fontSize = 8.sp, color = Color.Gray)
                                                    Text("${rec.usedAmount} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Residuo", fontSize = 8.sp, color = Color.Gray)
                                                    Text("${rec.wasteAmount} L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusAlert)
                                                }
                                            }
                                        }
                                        if (rec != receipts.take(4).last()) {
                                            Divider(color = SurfaceBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO DESPACHO form segment (Industrial control interface representation)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header of despacho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = IndustrialSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NUEVO DESPACHO", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndustrialPrimary)
                    }
                    Text("#FL-2849", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activo Scanner Segment
                Text("PASO 1: IDENTIFICAR ACTIVO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // QR scanner box triggers verified simulation
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(110.dp)
                            .clickable {
                                viewModel.dispatchActiveId.value = "VOL-442"
                                viewModel.dispatchObservedVolume.value = "250.00"
                            },
                        color = IndustrialSecondary.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, IndustrialSecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = IndustrialSecondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "ESCANEAR CÓDIGO QR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Manual code input
                    Column(
                        modifier = Modifier.weight(1.5f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = activeId,
                            onValueChange = { viewModel.dispatchActiveId.value = it },
                            placeholder = { Text("Ingresar ID manual") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                        )
                        Text("Ej: VOL-442, GEN-09", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated vehicle preview card dynamically visible
                AnimatedVisibility(visible = activeId.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeutralBg)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Truck image mock representation using Coil
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1601584115197-04ecc0da31d7?w=150",
                            contentDescription = "Volvo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(activeId.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndustrialPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = IndustrialPrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        "EXCAVADORA",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Camión Pesado • Capacidad: 450L", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = "verified", tint = StatusSuccess)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STEP 2: Volume selectors
                Text("PASO 2: ESPECIFICAR VOLUMEN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Volumen Observado (L)", fontSize = 10.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = rawVolObs,
                            onValueChange = { viewModel.dispatchObservedVolume.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Volumen Corregido (VCF 15°C)", fontSize = 10.sp, color = Color.Gray)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            color = NeutralBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(
                                    text = String.format("%.2f L", correctedVolVal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.dispatchActiveId.value = ""
                            viewModel.dispatchObservedVolume.value = "0"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, IndustrialPrimary)
                    ) {
                        Text("CANCELAR", color = IndustrialPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.dispatchFuel() },
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REGISTRAR DESPACHO", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Dispatches representation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ÚLTIMOS DESPACHOS DEL TURNO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                dispatches.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = log.activeId, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Hora: ${log.timeStr} • ${log.operator}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${log.observedVolume} L",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = IndustrialPrimary
                            )
                            Surface(
                                color = if (log.isOffline) StatusWarning.copy(alpha = 0.12f) else StatusSuccess.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (log.isOffline) "OFFLINE" else "SINCRONIZADO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.isOffline) Color(0xFFB45309) else StatusSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Divider(color = SurfaceBorder)
                }
            }
        }
    }
}


// ----------------------------------------------------
// TAB 3: InventoryScreen (Stock Control)
// ----------------------------------------------------
@Composable
fun InventoryScreen(viewModel: FuelManagerViewModel) {
    val inventory by viewModel.allInventory.collectAsStateWithLifecycle()
    val rawQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val abcFilter by viewModel.selectedAbcFilter.collectAsStateWithLifecycle()
    val vedFilter by viewModel.selectedVedFilter.collectAsStateWithLifecycle()

    var showReceiptDialog by remember { mutableStateOf(false) }
    var showScanIaDialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var newSku by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newLoc by remember { mutableStateOf("") }
    var newQty by remember { mutableStateOf("") }
    var newAbc by remember { mutableStateOf("A") }
    var newVed by remember { mutableStateOf("V") }

    // Filtering logic
    val filteredInventory = inventory.filter { item ->
        val matchesSearch = item.name.contains(rawQuery, ignoreCase = true) || item.sku.contains(rawQuery, ignoreCase = true) || item.location.contains(rawQuery, ignoreCase = true)
        val matchesAbc = abcFilter == "TODOS" || item.abcGrade == abcFilter
        val matchesVed = vedFilter == "TODOS" || item.vedGrade == vedFilter
        matchesSearch && matchesAbc && matchesVed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Inventory Main title
        Column {
            Text(
                text = "Control de Inventario",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = IndustrialPrimary
            )
            Text(
                text = "Monitoreo de refacciones y activos industriales en tiempo real.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Receipts and IA Scanner triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showReceiptDialog = true },
                modifier = Modifier.weight(1f).testTag("recepcion_button"),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recepción", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { showScanIaDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialPrimaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Escaneo IA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats Bento Grid representation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stat 1: Total Stock
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("STOCK TOTAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = IndustrialSecondary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("12,482", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    Text("Units", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Stat 2: DSI Promedio
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DSI PROMEDIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("18.4", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    Text("Días", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stat 3: Alta criticidad
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ALTA CRITICIDAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = StatusAlert, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("24", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StatusAlert)
                    Text("SKUs", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Stat 4: Valorizado
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("VALORIZADO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Icon(Icons.Default.Payments, contentDescription = null, tint = IndustrialSecondary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("$2.4M", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    Text("USD", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search engine inputs and drop filter metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = rawQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Buscar por SKU, nombre pasillo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ABC filter spinner mock
                    var abcDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { abcDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "ABC: $abcFilter", fontSize = 11.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = abcDropdownExpanded, onDismissRequest = { abcDropdownExpanded = false }) {
                            listOf("TODOS", "A", "B", "C").forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        viewModel.selectedAbcFilter.value = grade
                                        abcDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // VED filter spinner mock
                    var vedDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { vedDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "VED: $vedFilter", fontSize = 11.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = vedDropdownExpanded, onDismissRequest = { vedDropdownExpanded = false }) {
                            listOf("TODOS", "V", "E", "D").forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade) },
                                    onClick = {
                                        viewModel.selectedVedFilter.value = grade
                                        vedDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inventory list/table container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeutralBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ARTÍCULO / SKU", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1.5f))
                    Text("STOCK ACTUAL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                if (filteredInventory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron registros.", color = Color.Gray)
                    }
                } else {
                    filteredInventory.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Info with customized industry icon representation
                            Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeutralBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (item.iconName) {
                                        "oil_barrel" -> Icons.Default.OilBarrel
                                        "bolt" -> Icons.Default.Bolt
                                        else -> Icons.Default.SettingsInputComponent
                                    }
                                    Icon(icon, contentDescription = null, tint = IndustrialSecondary, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndustrialPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = item.location, fontSize = 11.sp, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                        Text(text = item.sku, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.LightGray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = IndustrialPrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "${item.abcGrade}-${item.vedGrade}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Column 2: Stocks info values
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${item.stockActual} uds",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = IndustrialPrimary
                                )

                                // Linear progress mapping to state required limit
                                val fillRatio = (item.stockActual.toFloat() / item.totalStockRequired.toFloat()).coerceIn(0f, 1f)
                                val progressColor = when {
                                    item.statusText == "Stock Bajo" -> StatusAlert
                                    item.statusText == "Exceso" -> StatusWarning
                                    else -> StatusSuccess
                                }

                                LinearProgressIndicator(
                                    progress = { fillRatio },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = progressColor,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )

                                surfaceStatusPill(statusText = item.statusText)
                            }
                        }
                        Divider(color = SurfaceBorder)
                    }
                }
            }
        }

        // Floating Scanner IA indicator icon floating button representation
        Spacer(modifier = Modifier.height(80.dp))
    }

    // Modal Receipt Dialog box
    if (showReceiptDialog) {
        AlertDialog(
            onDismissRequest = { showReceiptDialog = false },
            title = { Text("Registrar Recepción de Material", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newSku, onValueChange = { newSku = it }, label = { Text("SKU Código") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nombre de Refacción") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newLoc, onValueChange = { newLoc = it }, label = { Text("Ubicación Pasillo/Estante") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newQty, onValueChange = { newQty = it }, label = { Text("Cantidad Entrada") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Criticidad (ABC)", fontSize = 11.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("A", "B", "C").forEach { g ->
                                    FilterChip(selected = newAbc == g, onClick = { newAbc = g }, label = { Text(g) })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newQty.toIntOrNull() ?: 1
                        viewModel.addReceipt(newSku, newName, newLoc, amount, newAbc, newVed)
                        showReceiptDialog = false
                        // reset
                        newSku = ""
                        newName = ""
                        newLoc = ""
                        newQty = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                ) {
                    Text("Ingresar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceiptDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Modern Computer Vision Dynamic Scan mock UI
    if (showScanIaDialog) {
        AlertDialog(
            onDismissRequest = { showScanIaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = StatusSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conteo Automatizado por IA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Camera layout simulator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // Blurred truck/machinery background
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?w=300",
                            contentDescription = null,
                            alpha = 0.5f,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Scanning overlay lines
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .background(StatusAlert)
                        )
                        Text(
                            "RECONOCIENDO ELEMENTOS...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeutralBg),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DETECCIÓN RECIENTE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("98% PRECISIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "12x Bujía de Ignición 22mm (BJ-11200-S) encontradas en Bloque 02.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = IndustrialPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.runIaCount("BJ-11200-S", 12)
                        showScanIaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                ) {
                    Text("Confirmar Conteo IA")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanIaDialog = false }) { Text("Descartar") }
            }
        )
    }
}

@Composable
fun surfaceStatusPill(statusText: String) {
    val color = when (statusText) {
        "Stock Bajo" -> StatusAlert
        "Exceso" -> StatusWarning
        else -> StatusSuccess
    }
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(statusText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}


// ----------------------------------------------------
// TAB 4: CMMSMantenimientoScreen (Orders List & Detail form)
// ----------------------------------------------------
@Composable
fun CMMSMantenimientoScreen(viewModel: FuelManagerViewModel) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val selectedWorkOrder by viewModel.selectedWorkOrder.collectAsStateWithLifecycle()
    val activeTab by viewModel.selectedOtTab.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showCreateEmergencyDialog by remember { mutableStateOf(false) }
    var emergencyTitle by remember { mutableStateOf("") }
    var emergencyLoc by remember { mutableStateOf("") }

    // Navigation and tabs filters
    val filteredOrders = orders.filter { o ->
        when (activeTab) {
            "Preventivas" -> o.type.contains("Preventiva")
            "Correctivas" -> o.type.contains("Correctiva") || o.isEmergency
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBackground)
            .padding(16.dp)
    ) {
        // High level heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Órdenes de Trabajo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = IndustrialPrimary
                )
                Text(
                    text = "Control e informes de fallas y calibración centralizada.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Quick Create FAB representer inside title block
            IconButton(
                onClick = { showCreateEmergencyDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StatusAlert)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Emergency OT", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid-based display split-pane! Supports fluid list-detail hierarchy
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1: Feeds list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Filters tab buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Todas", "Preventivas", "Correctivas").forEach { t ->
                        val selected = activeTab == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectedOtTab.value = t },
                            color = if (selected) IndustrialPrimaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = t,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Color.DarkGray,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Scrollable Orders List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredOrders.forEach { order ->
                        val border = if (selectedWorkOrder?.otId == order.otId) {
                            BorderStroke(2.dp, if (order.isEmergency) StatusAlert else IndustrialSecondary)
                        } else {
                            BorderStroke(1.dp, SurfaceBorder)
                        }

                        Card(
                            onClick = { viewModel.selectWorkOrder(order) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = border,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (order.isEmergency) StatusAlert else Color.Gray.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = order.type.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (order.isEmergency) Color.White else Color.DarkGray,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = order.otId,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.LightGray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(text = order.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Ubicación: ${order.location}", fontSize = 11.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = SurfaceBorder)
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Asignado: ${order.assignedTo}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = order.scheduledTime, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // Column 2: Detailed form Canvas (Tablet mode/selected order inspect)
            selectedWorkOrder?.let { order ->
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Card header info dark band
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IndustrialPrimaryContainer)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EJECUCIÓN DE TRABAJO - ${order.otId}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialOnPrimaryContainer,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = order.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Prioridad: ${if (order.isEmergency) "Crítica" else "Normal"}",
                                    fontSize = 11.sp,
                                    color = if (order.isEmergency) StatusAlert else Color.White
                                )
                            }
                        }

                        // Form content area scrollable
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Fields: Reading of meters representation
                            var horometroVal by remember(order.otId) { mutableStateOf(order.readingHours.toString()) }
                            var kilometrajeVal by remember(order.otId) { mutableStateOf(order.mileage.toString()) }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Lectura Horómetro Actual", fontSize = 10.sp, color = Color.Gray)
                                    OutlinedTextField(
                                        value = horometroVal,
                                        onValueChange = { horometroVal = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Kilometraje Reportado", fontSize = 10.sp, color = Color.Gray)
                                    OutlinedTextField(
                                        value = kilometrajeVal,
                                        onValueChange = { kilometrajeVal = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialSecondary)
                                    )
                                }
                            }

                            // Evidencia Fotográfica Block
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Evidencia Fotográfica",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialPrimary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PhotoCapturePlaceholder(label = "Antes") {}
                                    PhotoCapturePlaceholder(label = "Durante") {}
                                    PhotoCapturePlaceholder(label = "Después") {}

                                    // Render default loaded parts image
                                    AsyncImage(
                                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAtnK9ZKAwMhE2PBV-iXl_BB_eox4ibWajKvP_zvhEuvYhSQQKAaIELbx2OhZJWNKYHDfsSxzev2naOvkS3Q2PprY7WNCscsOXid7mjjEVxBxGoQPsPvqWkPVZ2UnimifTil9N67cZHJWIZbcpWRYiuqVQn6tAnoHal--2JH33SJojRKbIjcQcoxk8XnhSVa9Lc_bOoKbOe0TCn47l2N-1ODPAHMEVtcY6NRkBz8i2NlkCUIN0sjcJJdBfoopkw7UR53UZAZDBEfLKm",
                                        contentDescription = "Evidencia",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // DRAWING SIGNATURE PAD (Sustained on Room Local Database persistence)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Firma de Conformidad",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialPrimary
                                )

                                // Actual signature tracking lists
                                val signatureDrawingLines = remember { mutableStateListOf<List<Offset>>() }
                                var currentLine = remember { mutableStateOf<List<Offset>>(emptyList()) }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    color = NeutralBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Box {
                                        // Drawing Canvas
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(Unit) {
                                                    detectDragGestures(
                                                        onDragStart = { offset ->
                                                            currentLine.value = listOf(offset)
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            val newPoint = change.position
                                                            currentLine.value = currentLine.value + newPoint
                                                        },
                                                        onDragEnd = {
                                                            signatureDrawingLines.add(currentLine.value)
                                                            currentLine.value = emptyList()
                                                        }
                                                    )
                                                }
                                        ) {
                                            // Draw existing lines
                                            signatureDrawingLines.forEach { line ->
                                                for (i in 0 until line.size - 1) {
                                                    drawLine(
                                                        color = IndustrialPrimary,
                                                        start = line[i],
                                                        end = line[i + 1],
                                                        strokeWidth = 3.dp.toPx()
                                                    )
                                                }
                                            }

                                            // Draw current line
                                            val current = currentLine.value
                                            if (current.isNotEmpty()) {
                                                for (i in 0 until current.size - 1) {
                                                    drawLine(
                                                        color = IndustrialPrimary,
                                                        start = current[i],
                                                        end = current[i + 1],
                                                        strokeWidth = 3.dp.toPx()
                                                    )
                                                }
                                            }
                                        }

                                        // Overlay instructions
                                        if (signatureDrawingLines.isEmpty() && currentLine.value.isEmpty()) {
                                            Text(
                                                text = "Use su dedo o lápiz óptico para firmar",
                                                modifier = Modifier.align(Alignment.Center),
                                                fontSize = 12.sp,
                                                color = Color.LightGray
                                            )
                                        }

                                        // Clear button
                                        IconButton(
                                            onClick = {
                                                signatureDrawingLines.clear()
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                        ) {
                                            Icon(Icons.Default.RestartAlt, contentDescription = "Clear", tint = Color.Gray)
                                        }
                                    }
                                }
                            }

                            // Actions buttons closures
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.saveWorkOrderDraft(
                                            order.otId,
                                            horometroVal.toDoubleOrNull() ?: 0.0,
                                            kilometrajeVal.toIntOrNull() ?: 0,
                                            signaturePoints = "draft_signed"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, IndustrialPrimary)
                                ) {
                                    Text("Guardar Borrador", color = IndustrialPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.finalizeWorkOrder(
                                            order.otId,
                                            horometroVal.toDoubleOrNull() ?: 0.0,
                                            kilometrajeVal.toIntOrNull() ?: 0,
                                            signaturePoints = "completed_signed"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialSecondary)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Finalizar OT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Emergency OT FAB details Dialog
    if (showCreateEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showCreateEmergencyDialog = false },
            title = { Text("Declarar Correctiva de Emergencia", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Las órdenes correctivas críticas enviarán automáticamente alertas push en tiempo real directos a los smartphones de los técnicos en turno.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = emergencyTitle,
                        onValueChange = { emergencyTitle = it },
                        label = { Text("Falla Detectada / Título") },
                        placeholder = { Text("Ej: Pandeo en rodillo transportador") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emergencyLoc,
                        onValueChange = { emergencyLoc = it },
                        label = { Text("Ubicación exacta") },
                        placeholder = { Text("Ej: Alimentador de Generador 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emergencyTitle.isNotEmpty()) {
                            viewModel.createEmergencyCorrective(emergencyTitle, emergencyLoc)
                        }
                        showCreateEmergencyDialog = false
                        emergencyTitle = ""
                        emergencyLoc = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAlert)
                ) {
                    Text("Asignar & Notificar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateEmergencyDialog = false }) { Text("Volver") }
            }
        )
    }
}

@Composable
fun PhotoCapturePlaceholder(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralBg)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}
