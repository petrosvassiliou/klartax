package de.klartax.app.ui

import android.content.ClipDescription
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.klartax.app.R
import de.klartax.app.core.math.Money
import de.klartax.app.data.local.entities.CustomerEntity
import de.klartax.app.data.local.entities.DocumentTextTemplateEntity
import de.klartax.app.data.local.entities.EurBookingEntity
import de.klartax.app.data.local.entities.InvoiceEntity
import de.klartax.app.data.local.entities.InvoiceLineItem
import de.klartax.app.data.local.entities.KlartextEurCategory
import de.klartax.app.data.local.entities.ServiceCatalogItemEntity
import de.klartax.app.ui.theme.KlartaxSuccessGreen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Phase 3: Material 3 Expressive & Adaptive Layout (`NavigationSuiteScaffold` + `ListDetailPaneScaffold`)
 * in den Markenfarben **Dunkelrot (`#7A1624`), Weiß (`#FFFFFF`) & Königsblau (`#1E3A8A`)**
 * inkl. komplettem SevDesk-Funktionsumfang:
 * - Kundenverwaltung (CRM & BT-10 Leitweg-ID)
 * - Produkte & angebotene Dienstleistungen (Artikelkatalog)
 * - Standardtexte & Textbausteine (Kopf- & Fußtexte für Angebote, Aufträge & Rechnungen)
 * - DIN-5008 Briefpapier & Firmenlogo-Designer
 * - Durchgängig verknüpfte Belegkette: Angebot (AN) → Auftrag / Auftragsbestätigung (AB) → E-Rechnung (RE · ZUGFeRD 2.3 & XRechnung 3.0)
 * - Ausgabenerfassung inkl. PDF- & Bilderanhängen
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun KlartaxAdaptiveApp(
    viewModel: KlartaxViewModel,
    onRequestBiometricUnlock: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val selectedWisoTopic by viewModel.selectedWisoTopic.collectAsStateWithLifecycle()
    val searchQuery by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
    val showNewEntryDialog by viewModel.showNewEntryDialog.collectAsStateWithLifecycle()
    val selectedInvoiceCustomerId by viewModel.selectedInvoiceCustomerId.collectAsStateWithLifecycle()
    val isBiometricUnlocked by viewModel.isBiometricUnlocked.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerDe.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val searchFocusRequester = remember { FocusRequester() }
    var isDraggingReceiptOverApp by remember { mutableStateOf(false) }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDraggingReceiptOverApp = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDraggingReceiptOverApp = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDraggingReceiptOverApp = false
                val clipData = event.toAndroidDragEvent().clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val droppedText = clipData.getItemAt(0).text?.toString().orEmpty()
                    if (droppedText.isNotBlank()) {
                        viewModel.processDroppedOrScannedReceiptContent(
                            rawTextOrXml = droppedText,
                            attachedFileName = "DragDrop_Beleg_${LocalDate.now()}.pdf",
                            attachedMimeType = "application/pdf"
                        )
                        return true
                    }
                }
                return false
            }
        }
    }

    val currentState = uiState
    if (currentState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Klartax SQLCipher-Tresor & § 32a EStG Engine werden initialisiert…",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    NavigationSuiteScaffold(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                    when (keyEvent.key) {
                        Key.N -> {
                            viewModel.openNewEntryDialog()
                            true
                        }
                        Key.F -> {
                            runCatching { searchFocusRequester.requestFocus() }
                            true
                        }
                        Key.S -> {
                            viewModel.triggerZeroKnowledgeDriveSync()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                        event.mimeTypes().any { it.contains("xml") || it.contains("pdf") || it.contains("image") }
                },
                target = dragAndDropTarget
            ),
        navigationSuiteItems = {
            KlartaxDestination.entries.forEach { destination ->
                item(
                    selected = selectedDestination == destination,
                    onClick = { viewModel.selectDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = when (destination) {
                                KlartaxDestination.COCKPIT -> Icons.Default.Speed
                                KlartaxDestination.CUSTOMERS -> Icons.Default.Badge
                                KlartaxDestination.EUR_AND_RECEIPTS -> Icons.Default.ReceiptLong
                                KlartaxDestination.EINVOICES -> Icons.Default.QrCode2
                                KlartaxDestination.WISO_INCOME_TAX -> Icons.Default.Calculate
                                KlartaxDestination.ELSTER_BESCHEID_SYNC -> Icons.Default.Security
                            },
                            contentDescription = stringResource(destination.labelResId)
                        )
                    },
                    label = { Text(stringResource(destination.labelResId)) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                ExpressiveKlartaxTopBar(
                    state = currentState,
                    isBiometricUnlocked = isBiometricUnlocked,
                    onSwitchProfile = viewModel::switchProfile,
                    onSyncClick = viewModel::triggerZeroKnowledgeDriveSync,
                    onUnlockClick = onRequestBiometricUnlock
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openNewEntryDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                    text = { Text("Ausgabe / PDF-Beleg (Strg+N)", fontWeight = FontWeight.Bold) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedVisibility(visible = isDraggingReceiptOverApp) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                            Text(
                                text = stringResource(R.string.drag_drop_Zone_hint),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = statusBanner != null) {
                    statusBanner?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                TextButton(onClick = viewModel::dismissBanner) {
                                    Text("Ausblenden")
                                }
                            }
                        }
                    }
                }

                when (selectedDestination) {
                    KlartaxDestination.COCKPIT -> ExpressiveCockpitScreen(
                        state = currentState,
                        onOpenCustomers = { viewModel.selectDestination(KlartaxDestination.CUSTOMERS) },
                        onOpenWisoTopic = { topic ->
                            viewModel.selectWisoTopic(topic)
                            viewModel.selectDestination(KlartaxDestination.WISO_INCOME_TAX)
                        },
                        onOpenEur = { viewModel.selectDestination(KlartaxDestination.EUR_AND_RECEIPTS) },
                        onOpenInvoices = { viewModel.selectDestination(KlartaxDestination.EINVOICES) },
                        onSimulateReceiptDrop = { sampleText, recurring ->
                            viewModel.processDroppedOrScannedReceiptContent(
                                rawTextOrXml = sampleText,
                                forceRecurringTenDayCheck = recurring,
                                attachedFileName = if (recurring) "Telekom_Glasfaser_2026_12.pdf" else "Scan_Beleg_2026.pdf",
                                attachedMimeType = "application/pdf",
                                attachedSizeBytes = 158_400L
                            )
                        }
                    )
                    KlartaxDestination.CUSTOMERS -> ExpressiveCustomersAndCatalogScreen(
                        state = currentState,
                        searchQuery = searchQuery,
                        searchFocusRequester = searchFocusRequester,
                        onSearchQueryChange = viewModel::updateGlobalSearch,
                        onSaveCustomer = viewModel::saveCustomer,
                        onDeleteCustomer = viewModel::deleteCustomer,
                        onStartInvoiceForCustomer = viewModel::startInvoiceForCustomer,
                        onSaveCatalogItem = viewModel::saveServiceCatalogItem,
                        onDeleteCatalogItem = viewModel::deleteServiceCatalogItem,
                        onSaveTextTemplate = viewModel::saveDocumentTextTemplate,
                        onDeleteTextTemplate = viewModel::deleteDocumentTextTemplate,
                        onSaveStationeryConfig = viewModel::saveStationeryConfig
                    )
                    KlartaxDestination.EUR_AND_RECEIPTS -> ExpressiveEurListDetailScreen(
                        state = currentState,
                        searchQuery = searchQuery,
                        searchFocusRequester = searchFocusRequester,
                        onSearchQueryChange = viewModel::updateGlobalSearch,
                        onOpenExpenseDialog = viewModel::openNewEntryDialog,
                        onAttachFileToBooking = viewModel::attachFileToBooking,
                        onTriggerStornoCorrection = viewModel::triggerGoBdStornoAndCorrection
                    )
                    KlartaxDestination.EINVOICES -> ExpressiveEInvoiceStudioScreen(
                        state = currentState,
                        selectedCustomerId = selectedInvoiceCustomerId,
                        onSelectCustomerId = viewModel::selectCustomerInInvoiceStudio,
                        onIssueFullInvoice = viewModel::issueFullInvoiceWithLineItems,
                        onConvertQuoteToOrder = viewModel::convertQuoteToOrderConfirmation,
                        onConvertToEInvoice = viewModel::convertQuoteOrOrderToEInvoice,
                        onMarkInvoicePaid = viewModel::markInvoiceAsPaidAndBookToEur,
                        onCreateStornoInvoice = viewModel::createStornoForInvoice,
                        onAdvanceDunningLevel = viewModel::advanceInvoiceDunningLevel,
                        onAddTimeTrackingEntry = viewModel::addTimeTrackingEntry
                    )
                    KlartaxDestination.WISO_INCOME_TAX -> ExpressiveWiso3PaneScreen(
                        state = currentState,
                        selectedTopic = selectedWisoTopic,
                        onSelectTopic = viewModel::selectWisoTopic,
                        onNextTopic = viewModel::advanceToNextWisoTopic,
                        onToggleSplitting = viewModel::toggleSplittingAssessment,
                        onUpdateHomeOfficeDays = viewModel::updateHomeOfficeDays,
                        onUpdateCommuteKm = viewModel::updateCommuteDistanceKm,
                        onUpdateCommuteDays = viewModel::updateCommuteWorkdays,
                        onUpdateWorkEquipmentEuro = viewModel::updateWorkEquipmentEuro,
                        onUpdateChildcareCents = viewModel::updateChildcareExpensesCents,
                        onUpdateSec35aLaborCents = viewModel::updateSec35aEligibleLaborCents,
                        onApplyNebenkostenSec35a = viewModel::applyNebenkostenSec35aAssistant
                    )
                    KlartaxDestination.ELSTER_BESCHEID_SYNC -> ExpressiveElsterBescheidSyncScreen(
                        state = currentState,
                        syncState = syncState,
                        onTriggerSync = viewModel::triggerZeroKnowledgeDriveSync
                    )
                }
            }
        }
    }

    if (showNewEntryDialog) {
        ExpenseCaptureDialog(
            defaultYear = currentState.activeProfile.taxYear,
            onDismiss = viewModel::closeNewEntryDialog,
            onSubmitExpense = viewModel::submitDetailedExpenseWithAttachment
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpressiveKlartaxTopBar(
    state: KlartaxAppUiState,
    isBiometricUnlocked: Boolean,
    onSwitchProfile: (String) -> Unit,
    onSyncClick: () -> Unit,
    onUnlockClick: () -> Unit
) {
    val isRefund = state.taxSummary.isRefund
    val refundFormatted = Money.formatEuro(state.taxSummary.expectedRefundOrBackPayment.abs())

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "KLARTAX",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
                Column {
                    Text(
                        text = state.activeProfile.profileTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Steuerjahr ${state.activeProfile.taxYear} · ${state.customers.size} Kunden · ${state.serviceCatalogItems.size} Leistungen · GoBD-Tresor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            val otherProfile = state.allProfiles.firstOrNull { it.id != state.activeProfile.id }
            if (otherProfile != null) {
                AssistChip(
                    onClick = { onSwitchProfile(otherProfile.id) },
                    label = { Text("Profil wechseln") },
                    leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(onClick = onSyncClick) {
                Icon(Icons.Default.CloudSync, contentDescription = "Drive Sync")
            }

            IconButton(onClick = onUnlockClick) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometrie",
                    tint = if (isBiometricUnlocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
            }

            Surface(
                color = if (isRefund) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = CircleShape,
                modifier = Modifier.padding(end = 12.dp, start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isRefund) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isRefund) "LIVE-ERSTATTUNG" else "NACHZAHLUNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRefund) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "${if (isRefund) "+ " else "- "}$refundFormatted",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isRefund) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

/**
 * 1. Expressive Cockpit Screen mit halbrundem Live-Tacho (`Canvas`), Steuerrücklagen-Split-Bar & Schnellzugriff.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveCockpitScreen(
    state: KlartaxAppUiState,
    onOpenCustomers: () -> Unit,
    onOpenWisoTopic: (WisoInterviewTopic) -> Unit,
    onOpenEur: () -> Unit,
    onOpenInvoices: () -> Unit,
    onSimulateReceiptDrop: (String, Boolean) -> Unit
) {
    val summary = state.taxSummary
    val isRefund = summary.isRefund
    val refundFormatted = Money.formatEuro(summary.expectedRefundOrBackPayment.abs())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE-ERSTATTUNGS-TACHO (§ 32a EStG)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (state.activeProfile.isMarriedJointAssessment) "Splittingtarif aktiv" else "Grundtarif",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(155.dp)
                        ) {
                            LiveErstattungsTachoGauge(
                                progress = 0.78f,
                                primaryColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${if (isRefund) "+ " else "- "}$refundFormatted",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Voraussichtliche Erstattung vom Finanzamt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ExpressiveMetricPill("Zu verst. Einkommen", Money.formatEuro(summary.taxableIncomeZvE))
                            ExpressiveMetricPill("Durchschnittssteuer", "${summary.effectiveTaxRatePercent} %")
                            ExpressiveMetricPill("Splitting-Vorteil", "+ ${Money.formatEuro(summary.splittingAdvantageEuro)}")
                        }
                    }
                }

                if (state.activeProfile.hasFreelanceActivity) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "STEUERRÜCKLAGEN-WARNER",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Bank: ${Money.formatEuroFromCents(state.activeProfile.businessBankBalanceCents)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val totalBank = Money.fromCents(state.activeProfile.businessBankBalanceCents).coerceAtLeast(BigDecimal.ONE)
                            val freeRatio = summary.freeLiquidityAfterReserves
                                .divide(totalBank, 4, RoundingMode.HALF_UP)
                                .toFloat()
                                .coerceIn(0.15f, 0.85f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .clip(CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(freeRatio)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f - freeRatio)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Frei verfügbar (Netto-Liquidität)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = Money.formatEuro(summary.freeLiquidityAfterReserves),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Auf Rücklagenkonto parken",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = Money.formatEuro(summary.recommendedTotalTaxReserve),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "EÜR-Gewinn (§ 4 Abs. 3): ${Money.formatEuro(summary.eurResult.freelanceProfit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "USt-Zahllast (Kz 83): ${Money.formatEuro(summary.eurResult.vatPayableOrRefund)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Direkter Schnellzugriff auf die SevDesk-Module
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenCustomers() },
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Kunden, Katalog & Briefpapier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${state.customers.size} Kunden · ${state.serviceCatalogItems.size} Produkte/Leistungen · ${state.documentTextTemplates.size} Textbausteine · DIN-5008 Logo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenEur() },
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text("Ausgaben & PDF/Foto-Belege", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        val withAttachmentCount = state.eurBookings.count { !it.attachmentFileName.isNullOrBlank() }
                        Text(
                            text = "${state.eurBookings.size} EÜR-Buchungen · $withAttachmentCount mit verknüpftem PDF-/Foto-Originalbeleg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenInvoices() },
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Angebote → Aufträge → Rechnungen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        val quotesCount = state.invoices.count { it.documentType == "QUOTE" }
                        val ordersCount = state.invoices.count { it.documentType == "ORDER_CONFIRMATION" }
                        val invCount = state.invoices.count { it.documentType == "INVOICE" }
                        Text(
                            text = "$quotesCount Angebote (AN) · $ordersCount Aufträge (AB) · $invCount E-Rechnungen (RE · ZUGFeRD 2.3 / XRechnung)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "KI-Belegscanner & PDF/Foto-Direktimport (100 % On-Device)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Trennt automatisch EÜR-Betriebsausgaben von privaten § 35a Handwerkerbelegen & prüft die 10-Tages-Regel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalActionPill(
                            label = "+ § 35a Handwerker",
                            onClick = {
                                onSimulateReceiptDrop(
                                    "Meisterbetrieb Elektro Süd GmbH\nRechnungsdatum: 07.11.2026\nWartung Wärmepumpe Arbeitskosten Lohnanteil 450,00 EUR\nGesamtbetrag 650,00 €",
                                    false
                                )
                            }
                        )
                        FilledTonalActionPill(
                            label = "+ 10-Tages-Regel (29.12.)",
                            onClick = {
                                onSimulateReceiptDrop(
                                    "Deutsche Telekom AG\nRechnungsdatum: 29.12.2026\nGlasfaser Business Server Cloud Hosting\nGesamtbetrag 119,00 €",
                                    true
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "PROAKTIVE STEUERSPAR-HEBEL (KLICK ZUM DIREKT-BEARBEITEN)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = 3
            ) {
                state.proactiveTips.forEach { tip ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val targetTopic = when {
                                    tip.id.contains("HOMEOFFICE") || tip.id.contains("EQUIPMENT") ->
                                        WisoInterviewTopic.ANLAGE_N_COMMUTE_HOMEOFFICE
                                    tip.id.contains("35A") ->
                                        WisoInterviewTopic.SEC35A_HANDWERKER_NEBENKOSTEN
                                    else ->
                                        WisoInterviewTopic.FREELANCE_EUR_BRIDGE
                                }
                                onOpenWisoTopic(targetTopic)
                            },
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "+ ${Money.formatEuro(tip.potentialSavingEuro)} Sparpotenzial",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = tip.titleDe,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tip.descriptionDe,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(76.dp)) }
    }
}

@Composable
private fun LiveErstattungsTachoGauge(
    progress: Float,
    primaryColor: Color,
    trackColor: Color
) {
    Canvas(
        modifier = Modifier
            .width(260.dp)
            .height(140.dp)
    ) {
        val strokeWidth = 22.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, (size.height - strokeWidth / 2) * 1.85f)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        drawArc(
            color = trackColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            brush = Brush.horizontalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.7f), primaryColor)
            ),
            startAngle = 180f,
            sweepAngle = 180f * progress.coerceIn(0.05f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ExpressiveMetricPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilledTonalActionPill(label: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 2. SevDesk Stammdaten-Zentrale (`ExpressiveCustomersAndCatalogScreen`):
 * - Tab 0: Kundenverwaltung (CRM, Leitweg-ID BT-10, USt-IdNr., Standard-Stundensatz)
 * - Tab 1: Produkte & angebotene Dienstleistungen (Artikelkatalog für 1-Klick-Auswahl in Angeboten/Rechnungen)
 * - Tab 2: Standardtexte & Textbausteine (Kopf- & Fußtexte für Angebote, Aufträge, Rechnungen)
 * - Tab 3: Briefpapier & Firmenlogo Designer (DIN 5008 mit Logo-Upload & Live-Vorschau)
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveCustomersAndCatalogScreen(
    state: KlartaxAppUiState,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onSaveCustomer: (
        existingId: String?,
        customerNumber: String,
        companyOrFullName: String,
        contactPersonName: String,
        email: String,
        streetAndNumber: String,
        postalCode: String,
        city: String,
        countryIso: String,
        vatId: String,
        leitwegId: String,
        paymentTermDays: Int,
        defaultHourlyRateEuro: String,
        defaultTaxMode: String,
        notesDe: String
    ) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onStartInvoiceForCustomer: (CustomerEntity) -> Unit,
    onSaveCatalogItem: (
        existingId: String?,
        articleNumber: String,
        itemType: String,
        titleDe: String,
        descriptionDe: String,
        unitCode: String,
        unitPriceNetEuro: String,
        vatRatePercent: Int,
        defaultQuantity: String
    ) -> Unit,
    onDeleteCatalogItem: (ServiceCatalogItemEntity) -> Unit,
    onSaveTextTemplate: (
        existingId: String?,
        templateTitleDe: String,
        documentScope: String,
        placementType: String,
        bodyTextDe: String,
        isDefault: Boolean
    ) -> Unit,
    onDeleteTextTemplate: (DocumentTextTemplateEntity) -> Unit,
    onSaveStationeryConfig: (
        companyDisplayName: String,
        companySubtitleOrClaim: String,
        logoMonogramText: String,
        logoFileName: String?,
        logoImageUri: String?,
        primaryAccentHex: String,
        secondaryAccentHex: String,
        senderWindowLineDe: String,
        footerCol1AddressDe: String,
        footerCol2BankDe: String,
        footerCol3TaxDe: String,
        showDin5008FoldMarks: Boolean,
        showEpcGiroCodeQr: Boolean
    ) -> Unit
) {
    var activeSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-Tab-Umschalter für alle 4 SevDesk-Stammdatenbereiche
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = { Text("1. Kundenverwaltung (${state.customers.size})") }
            )
            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = { Text("2. Produkte & Dienstleistungen (${state.serviceCatalogItems.size})") }
            )
            FilterChip(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                label = { Text("3. Standardtexte & Textbausteine (${state.documentTextTemplates.size})") }
            )
            FilterChip(
                selected = activeSubTab == 3,
                onClick = { activeSubTab = 3 },
                label = { Text("4. Briefpapier & Firmenlogo (DIN 5008)") }
            )
        }

        when (activeSubTab) {
            0 -> CustomersCrmSubScreen(
                state = state,
                searchQuery = searchQuery,
                searchFocusRequester = searchFocusRequester,
                onSearchQueryChange = onSearchQueryChange,
                onSaveCustomer = onSaveCustomer,
                onDeleteCustomer = onDeleteCustomer,
                onStartInvoiceForCustomer = onStartInvoiceForCustomer
            )
            1 -> ServiceCatalogSubScreen(
                state = state,
                onSaveCatalogItem = onSaveCatalogItem,
                onDeleteCatalogItem = onDeleteCatalogItem
            )
            2 -> DocumentTextTemplatesSubScreen(
                state = state,
                onSaveTextTemplate = onSaveTextTemplate,
                onDeleteTextTemplate = onDeleteTextTemplate
            )
            3 -> StationeryLogoDesignerSubScreen(
                state = state,
                onSaveStationeryConfig = onSaveStationeryConfig
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CustomersCrmSubScreen(
    state: KlartaxAppUiState,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onSaveCustomer: (
        existingId: String?,
        customerNumber: String,
        companyOrFullName: String,
        contactPersonName: String,
        email: String,
        streetAndNumber: String,
        postalCode: String,
        city: String,
        countryIso: String,
        vatId: String,
        leitwegId: String,
        paymentTermDays: Int,
        defaultHourlyRateEuro: String,
        defaultTaxMode: String,
        notesDe: String
    ) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onStartInvoiceForCustomer: (CustomerEntity) -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    var selectedCustomerId by remember { mutableStateOf(state.customers.firstOrNull()?.id) }
    var isCreatingNewCustomer by remember { mutableStateOf(false) }

    val selectedCustomer = if (isCreatingNewCustomer) {
        null
    } else {
        state.customers.find { it.id == selectedCustomerId } ?: state.customers.firstOrNull()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kunden-Stammdaten (CRM)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Button(
                            onClick = {
                                isCreatingNewCustomer = true
                                selectedCustomerId = null
                            }
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Neuer Kunde")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Kunde, KD-Nr., Leitweg-ID oder Ort suchen…") },
                        singleLine = true
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.customers, key = { it.id }) { customer ->
                            val isSelected = !isCreatingNewCustomer && selectedCustomer?.id == customer.id
                            val customerInvoices = state.invoices.filter {
                                (it.customerId == customer.id || it.customerName.equals(customer.companyOrFullName, ignoreCase = true)) &&
                                    it.documentType == "INVOICE"
                            }
                            val billedGrossCents = customerInvoices.sumOf { it.grossTotalCents }

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isCreatingNewCustomer = false
                                        selectedCustomerId = customer.id
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, customer.id)
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                Text(
                                                    customer.customerNumber,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                            if (customer.buyerReferenceLeitwegId.isNotBlank()) {
                                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                                    Text(
                                                        "BT-10: ${customer.buyerReferenceLeitwegId}",
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${Money.formatEuroFromCents(customer.defaultHourlyRateNetCents)} / Std.",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = customer.companyOrFullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${customer.contactPersonName} · ${customer.postalCode} ${customer.city} (${customer.countryIso})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Umsatz: ${Money.formatEuroFromCents(billedGrossCents)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                var custNumber by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.customerNumber ?: "KD-2026-00${state.customers.size + 1}")
                }
                var companyName by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.companyOrFullName ?: "")
                }
                var contactPerson by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.contactPersonName ?: "")
                }
                var email by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.email ?: "")
                }
                var street by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.streetAndNumber ?: "")
                }
                var postalCode by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.postalCode ?: "")
                }
                var city by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.city ?: "")
                }
                var countryIso by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.countryIso ?: "DE")
                }
                var vatId by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.vatId ?: "")
                }
                var leitwegId by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.buyerReferenceLeitwegId ?: "")
                }
                var paymentTermDays by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableIntStateOf(selectedCustomer?.paymentTermDays ?: 14)
                }
                var hourlyRateEuro by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(
                        Money.fromCents(selectedCustomer?.defaultHourlyRateNetCents ?: 150_00L).toPlainString()
                    )
                }
                var defaultTaxMode by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.defaultTaxMode ?: "STANDARD_19")
                }
                var notesDe by remember(selectedCustomer, isCreatingNewCustomer) {
                    mutableStateOf(selectedCustomer?.notesDe ?: "")
                }

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCreatingNewCustomer) "Neuen Kunden anlegen" else "Kunde bearbeiten",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black
                            )
                            if (selectedCustomer != null) {
                                Button(
                                    onClick = { onStartInvoiceForCustomer(selectedCustomer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Angebot / Auftrag / Rechnung erstellen →", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = custNumber,
                                onValueChange = { custNumber = it },
                                label = { Text("Kundennummer") },
                                modifier = Modifier.weight(0.35f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("Firma / Behörde / Vollständiger Name") },
                                modifier = Modifier.weight(0.65f),
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = contactPerson,
                                onValueChange = { contactPerson = it },
                                label = { Text("Ansprechpartner(in)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-Mail für Belegversand") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = street,
                                onValueChange = { street = it },
                                label = { Text("Straße & Hausnummer") },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("PLZ") },
                                modifier = Modifier.weight(0.2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ort") },
                                modifier = Modifier.weight(0.3f),
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = countryIso,
                                onValueChange = { countryIso = it },
                                label = { Text("Land (ISO)") },
                                modifier = Modifier.weight(0.25f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = vatId,
                                onValueChange = { vatId = it },
                                label = { Text("USt-IdNr. (BT-48)") },
                                modifier = Modifier.weight(0.35f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = leitwegId,
                                onValueChange = { leitwegId = it },
                                label = { Text("Leitweg-ID (BT-10)") },
                                modifier = Modifier.weight(0.4f),
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = hourlyRateEuro,
                                onValueChange = { hourlyRateEuro = it },
                                label = { Text("Standard-Stundensatz (€ Netto)") },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = paymentTermDays.toString(),
                                onValueChange = { paymentTermDays = it.toIntOrNull() ?: 14 },
                                label = { Text("Zahlungsziel (Tage)") },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                        }

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = defaultTaxMode == "STANDARD_19",
                                onClick = { defaultTaxMode = "STANDARD_19" },
                                label = { Text("19 % Regelbesteuerung") }
                            )
                            FilterChip(
                                selected = defaultTaxMode == "REDUCED_7",
                                onClick = { defaultTaxMode = "REDUCED_7" },
                                label = { Text("7 % Ermäßigt") }
                            )
                            FilterChip(
                                selected = defaultTaxMode == "KLEINUNTERNEHMER_SEC19",
                                onClick = { defaultTaxMode = "KLEINUNTERNEHMER_SEC19" },
                                label = { Text("§ 19 UStG Kleinunternehmer") }
                            )
                            FilterChip(
                                selected = defaultTaxMode == "REVERSE_CHARGE_SEC13B",
                                onClick = { defaultTaxMode = "REVERSE_CHARGE_SEC13B" },
                                label = { Text("§ 13b Reverse Charge") }
                            )
                        }

                        OutlinedTextField(
                            value = notesDe,
                            onValueChange = { notesDe = it },
                            label = { Text("Interne Vertrags- & Abrechnungsnotizen") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (selectedCustomer != null) {
                                OutlinedButton(onClick = { onDeleteCustomer(selectedCustomer) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kunde löschen")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    onSaveCustomer(
                                        selectedCustomer?.id,
                                        custNumber,
                                        companyName,
                                        contactPerson,
                                        email,
                                        street,
                                        postalCode,
                                        city,
                                        countryIso,
                                        vatId,
                                        leitwegId,
                                        paymentTermDays,
                                        hourlyRateEuro,
                                        defaultTaxMode,
                                        notesDe
                                    )
                                    isCreatingNewCustomer = false
                                }
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kunden-Stammdaten speichern", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * Tab 1: Produkte & angebotene Dienstleistungen (SevDesk Artikel- & Leistungskatalog).
 */
@Composable
private fun ServiceCatalogSubScreen(
    state: KlartaxAppUiState,
    onSaveCatalogItem: (
        existingId: String?,
        articleNumber: String,
        itemType: String,
        titleDe: String,
        descriptionDe: String,
        unitCode: String,
        unitPriceNetEuro: String,
        vatRatePercent: Int,
        defaultQuantity: String
    ) -> Unit,
    onDeleteCatalogItem: (ServiceCatalogItemEntity) -> Unit
) {
    var selectedItem by remember { mutableStateOf(state.serviceCatalogItems.firstOrNull()) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val activeItem = if (isCreatingNew) null else (selectedItem ?: state.serviceCatalogItems.firstOrNull())

    var articleNumber by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.articleNumber ?: "DL-10${state.serviceCatalogItems.size + 1}")
    }
    var itemType by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.itemType ?: "SERVICE")
    }
    var titleDe by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.titleDe ?: "")
    }
    var descriptionDe by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.descriptionDe ?: "")
    }
    var unitCode by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.unitCode ?: "HUR")
    }
    var unitPriceEuro by remember(activeItem, isCreatingNew) {
        mutableStateOf(Money.fromCents(activeItem?.unitPriceNetCents ?: 150_00L).toPlainString())
    }
    var vatRatePercent by remember(activeItem, isCreatingNew) {
        mutableIntStateOf(activeItem?.vatRatePercent ?: 19)
    }
    var defaultQuantity by remember(activeItem, isCreatingNew) {
        mutableStateOf(activeItem?.defaultQuantity ?: "1.00")
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produkte & Dienstleistungen (${state.serviceCatalogItems.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Button(onClick = { isCreatingNew = true; selectedItem = null }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Neue Leistung / Produkt")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.serviceCatalogItems, key = { it.id }) { item ->
                    val isSelected = !isCreatingNew && activeItem?.id == item.id
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isCreatingNew = false
                                selectedItem = item
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(item.articleNumber, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(
                                            if (item.itemType == "PRODUCT") "Produkt / Lizenz" else "Dienstleistung",
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${Money.formatEuroFromCents(item.unitPriceNetCents)} / ${item.unitCode}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(item.titleDe, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${item.descriptionDe} · Standardmenge: ${item.defaultQuantity} · ${item.vatRatePercent} % USt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isCreatingNew) "Neue Leistung / Produkt anlegen" else "Katalog-Position bearbeiten",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Hinterlegte Leistungen & Produkte stehen im Angebots-, Auftrags- und Rechnungs-Studio per 1-Klick zur Auswahl.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = itemType == "SERVICE",
                        onClick = { itemType = "SERVICE" },
                        label = { Text("Dienstleistung / Honorar") }
                    )
                    FilterChip(
                        selected = itemType == "PRODUCT",
                        onClick = { itemType = "PRODUCT" },
                        label = { Text("Produkt / Lizenz / Paket") }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = articleNumber,
                        onValueChange = { articleNumber = it },
                        label = { Text("Artikel-/Leistungs-Nr.") },
                        modifier = Modifier.weight(0.35f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = titleDe,
                        onValueChange = { titleDe = it },
                        label = { Text("Bezeichnung der Leistung / des Produkts") },
                        modifier = Modifier.weight(0.65f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = descriptionDe,
                    onValueChange = { descriptionDe = it },
                    label = { Text("Leistungsbeschreibung (erscheint auf Angebot, Auftrag & Rechnung)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = unitPriceEuro,
                        onValueChange = { unitPriceEuro = it },
                        label = { Text("Einzelpreis Netto (€)") },
                        modifier = Modifier.weight(0.4f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultQuantity,
                        onValueChange = { defaultQuantity = it },
                        label = { Text("Standard-Menge") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitCode,
                        onValueChange = { unitCode = it },
                        label = { Text("Einheit (HUR/DAY/C62)") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HUR" to "Stunde (HUR)", "DAY" to "Tagessatz (DAY)", "C62" to "Pauschal/Stück (C62)").forEach { (code, label) ->
                        FilterChip(
                            selected = unitCode == code,
                            onClick = { unitCode = code },
                            label = { Text(label) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(19, 7, 0).forEach { rate ->
                        FilterChip(
                            selected = vatRatePercent == rate,
                            onClick = { vatRatePercent = rate },
                            label = { Text("$rate % USt") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (activeItem != null) {
                        OutlinedButton(onClick = { onDeleteCatalogItem(activeItem) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Löschen")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            onSaveCatalogItem(
                                activeItem?.id,
                                articleNumber,
                                itemType,
                                titleDe,
                                descriptionDe,
                                unitCode,
                                unitPriceEuro,
                                vatRatePercent,
                                defaultQuantity
                            )
                            isCreatingNew = false
                        }
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Im Katalog speichern", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Standardtexte & Textbausteine für Angebote, Auftragsbestätigungen und Rechnungen.
 */
@Composable
private fun DocumentTextTemplatesSubScreen(
    state: KlartaxAppUiState,
    onSaveTextTemplate: (
        existingId: String?,
        templateTitleDe: String,
        documentScope: String,
        placementType: String,
        bodyTextDe: String,
        isDefault: Boolean
    ) -> Unit,
    onDeleteTextTemplate: (DocumentTextTemplateEntity) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf(state.documentTextTemplates.firstOrNull()) }
    var isCreatingNew by remember { mutableStateOf(false) }
    val activeTpl = if (isCreatingNew) null else (selectedTemplate ?: state.documentTextTemplates.firstOrNull())

    var titleDe by remember(activeTpl, isCreatingNew) {
        mutableStateOf(activeTpl?.templateTitleDe ?: "")
    }
    var documentScope by remember(activeTpl, isCreatingNew) {
        mutableStateOf(activeTpl?.documentScope ?: "INVOICE")
    }
    var placementType by remember(activeTpl, isCreatingNew) {
        mutableStateOf(activeTpl?.placementType ?: "HEADER_INTRO")
    }
    var bodyTextDe by remember(activeTpl, isCreatingNew) {
        mutableStateOf(activeTpl?.bodyTextDe ?: "")
    }
    var isDefault by remember(activeTpl, isCreatingNew) {
        mutableStateOf(activeTpl?.isDefault ?: true)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Standardtexte & Textbausteine (${state.documentTextTemplates.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Button(onClick = { isCreatingNew = true; selectedTemplate = null }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Neuer Textbaustein")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.documentTextTemplates, key = { it.id }) { tpl ->
                    val isSelected = !isCreatingNew && activeTpl?.id == tpl.id
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isCreatingNew = false
                                selectedTemplate = tpl
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(tpl.documentScope, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(
                                        if (tpl.placementType == "HEADER_INTRO") "Kopftext / Einleitung" else "Schlusstext / Fußtext",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            Text(tpl.templateTitleDe, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = tpl.bodyTextDe,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isCreatingNew) "Neuen Textbaustein anlegen" else "Textbaustein bearbeiten",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                HorizontalDivider()

                OutlinedTextField(
                    value = titleDe,
                    onValueChange = { titleDe = it },
                    label = { Text("Name des Textbausteins (z. B. Rechnung: 2 % Skonto)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Dokumenttyp-Zuordnung:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "QUOTE" to "Angebot (AN)",
                        "ORDER_CONFIRMATION" to "Auftrag (AB)",
                        "INVOICE" to "Rechnung (RE)"
                    ).forEach { (scopeCode, label) ->
                        FilterChip(
                            selected = documentScope == scopeCode,
                            onClick = { documentScope = scopeCode },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Position auf dem Briefpapier:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = placementType == "HEADER_INTRO",
                        onClick = { placementType = "HEADER_INTRO" },
                        label = { Text("Kopftext / Einleitungstext") }
                    )
                    FilterChip(
                        selected = placementType == "FOOTER_CLOSING",
                        onClick = { placementType = "FOOTER_CLOSING" },
                        label = { Text("Schlusstext / Zahlungsbedingungen") }
                    )
                }

                OutlinedTextField(
                    value = bodyTextDe,
                    onValueChange = { bodyTextDe = it },
                    label = { Text("Volltext des Textbausteins") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (activeTpl != null) {
                        OutlinedButton(onClick = { onDeleteTextTemplate(activeTpl) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Löschen")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            onSaveTextTemplate(
                                activeTpl?.id,
                                titleDe,
                                documentScope,
                                placementType,
                                bodyTextDe,
                                isDefault
                            )
                            isCreatingNew = false
                        }
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Textbaustein speichern", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Briefpapier & Firmenlogo Konfigurator (SevDesk Briefpapier-Designer nach DIN 5008).
 */
@Composable
private fun StationeryLogoDesignerSubScreen(
    state: KlartaxAppUiState,
    onSaveStationeryConfig: (
        companyDisplayName: String,
        companySubtitleOrClaim: String,
        logoMonogramText: String,
        logoFileName: String?,
        logoImageUri: String?,
        primaryAccentHex: String,
        secondaryAccentHex: String,
        senderWindowLineDe: String,
        footerCol1AddressDe: String,
        footerCol2BankDe: String,
        footerCol3TaxDe: String,
        showDin5008FoldMarks: Boolean,
        showEpcGiroCodeQr: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val cfg = state.stationeryConfig

    var companyDisplayName by remember(cfg) {
        mutableStateOf(cfg?.companyDisplayName ?: "Lukas Weber · Studio für FinTech-Architektur & Adaptive UI")
    }
    var companyClaim by remember(cfg) {
        mutableStateOf(cfg?.companySubtitleOrClaim ?: "Principal Software Engineering · EN 16931 & GoBD Advisory · München")
    }
    var logoMonogram by remember(cfg) {
        mutableStateOf(cfg?.logoMonogramText ?: "LW")
    }
    var logoFileName by remember(cfg) {
        mutableStateOf(cfg?.logoFileName ?: "Weber_Studio_Markenlogo_2026.svg")
    }
    var logoImageUri by remember(cfg) {
        mutableStateOf(cfg?.logoImageUri ?: "file:///klartax_vault/branding/Weber_Studio_Markenlogo_2026.svg")
    }
    var primaryHex by remember(cfg) {
        mutableStateOf(cfg?.primaryAccentHex ?: "#7A1624")
    }
    var secondaryHex by remember(cfg) {
        mutableStateOf(cfg?.secondaryAccentHex ?: "#1E3A8A")
    }
    var senderWindowLine by remember(cfg) {
        mutableStateOf(cfg?.senderWindowLineDe ?: "Lukas Weber · Leopoldstraße 88 · 80802 München")
    }
    var footerCol1 by remember(cfg) {
        mutableStateOf(cfg?.footerCol1AddressDe ?: "Lukas Weber\nLeopoldstraße 88\n80802 München")
    }
    var footerCol2 by remember(cfg) {
        mutableStateOf(cfg?.footerCol2BankDe ?: "Commerzbank München\nIBAN: DE89 7008 0000 0512 3456 00\nBIC: DRESDEFF700")
    }
    var footerCol3 by remember(cfg) {
        mutableStateOf(cfg?.footerCol3TaxDe ?: "Finanzamt München\nSteuernummer: 143/815/08155\nUSt-IdNr.: DE314159265")
    }
    var showFoldMarks by remember(cfg) {
        mutableStateOf(cfg?.showDin5008FoldMarks ?: true)
    }
    var showGiroCode by remember(cfg) {
        mutableStateOf(cfg?.showEpcGiroCodeQr ?: true)
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var resolvedName = "Firmenlogo_${System.currentTimeMillis()}.png"
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) resolvedName = cursor.getString(idx) ?: resolvedName
                    }
                }
            }
            logoFileName = resolvedName
            logoImageUri = uri.toString()
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Briefpapier & Firmenlogo anpassen (DIN 5008)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            logoPickerLauncher.launch(arrayOf("image/png", "image/jpeg", "image/svg+xml"))
                        }
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Firmenlogo hochladen (PNG/SVG/JPG)")
                    }
                    OutlinedTextField(
                        value = logoMonogram,
                        onValueChange = { logoMonogram = it.take(4) },
                        label = { Text("Logo-Kürzel") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                }

                if (!logoFileName.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aktives Firmenlogo: $logoFileName ($logoMonogram)",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = companyDisplayName,
                    onValueChange = { companyDisplayName = it },
                    label = { Text("Firmenname / Kanzlei- / Studio-Bezeichnung im Briefkopf") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = companyClaim,
                    onValueChange = { companyClaim = it },
                    label = { Text("Untertitel / Claim im Briefkopf") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = senderWindowLine,
                    onValueChange = { senderWindowLine = it },
                    label = { Text("DIN 5008 Rücksendeangabe im Adressfenster") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = footerCol1,
                        onValueChange = { footerCol1 = it },
                        label = { Text("Fußzeile Spalte 1 (Anschrift)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = footerCol2,
                        onValueChange = { footerCol2 = it },
                        label = { Text("Fußzeile Spalte 2 (Bank/IBAN)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = footerCol3,
                        onValueChange = { footerCol3 = it },
                        label = { Text("Fußzeile Spalte 3 (Steuer-Nr.)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DIN 5008 Falz- & Lochmarken sowie EPC-GiroCode anzeigen")
                    Switch(
                        checked = showFoldMarks,
                        onCheckedChange = { showFoldMarks = it; showGiroCode = it }
                    )
                }

                Button(
                    onClick = {
                        onSaveStationeryConfig(
                            companyDisplayName,
                            companyClaim,
                            logoMonogram,
                            logoFileName,
                            logoImageUri,
                            primaryHex,
                            secondaryHex,
                            senderWindowLine,
                            footerCol1,
                            footerCol2,
                            footerCol3,
                            showFoldMarks,
                            showGiroCode
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Briefpapier & Firmenlogo speichern", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Rechte Spalte: Live DIN-A4 Briefpapier-Vorschau
        Card(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = companyDisplayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF7A1624),
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = companyClaim,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                color = Color(0xFF7A1624),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = logoMonogram,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "LOGO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFD9DC)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF7A1624), thickness = 2.dp)

                        Text(
                            text = senderWindowLine,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF524345)
                        )
                        Text(
                            text = "Musterkunde GmbH\nMaximilianstraße 42\n80539 München",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1517),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (showFoldMarks) {
                            Text(
                                text = "— DIN 5008 Falzmarke (105 mm / 210 mm) aktiv —",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(color = Color(0xFFDADCE0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(footerCol1, modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color(0xFF524345))
                            Text(footerCol2, modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color(0xFF524345))
                            Text(footerCol3, modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color(0xFF524345))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Ausgabenerfassung & EÜR (`ExpressiveEurListDetailScreen`) inkl. PDF- & Bilderanhänge.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ExpressiveEurListDetailScreen(
    state: KlartaxAppUiState,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onOpenExpenseDialog: () -> Unit,
    onAttachFileToBooking: (EurBookingEntity, String, String, String, Long) -> Unit,
    onTriggerStornoCorrection: (EurBookingEntity, Long, String) -> Unit
) {
    val context = LocalContext.current
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    var selectedBookingId by remember { mutableStateOf(state.eurBookings.firstOrNull()?.id) }
    val selectedBooking = state.eurBookings.find { it.id == selectedBookingId } ?: state.eurBookings.firstOrNull()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && selectedBooking != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"
            var resolvedName = "Beleg_${selectedBooking.documentNumber}.pdf"
            var resolvedSize = 184_320L
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) resolvedName = cursor.getString(nameIdx) ?: resolvedName
                        if (sizeIdx >= 0) resolvedSize = cursor.getLong(sizeIdx)
                    }
                }
            }
            onAttachFileToBooking(selectedBooking, resolvedName, mimeType, uri.toString(), resolvedSize)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ausgaben, EÜR & Belege",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Gewinn: ${Money.formatEuro(state.taxSummary.eurResult.freelanceProfit)} · USt-Zahllast: ${Money.formatEuro(state.ustVaReport.kz83RemainingVatPayable)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = onOpenExpenseDialog) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Ausgabe & Beleg")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Beleg, Lieferant, PDF-Dateiname oder Betrag suchen…") },
                        singleLine = true
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.eurBookings, key = { it.id }) { booking ->
                            val isSelected = selectedBooking?.id == booking.id
                            val hasAttachment = !booking.attachmentFileName.isNullOrBlank()
                            val isImage = booking.attachmentMimeType?.startsWith("image") == true

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBookingId = booking.id
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, booking.id)
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Badge(
                                                containerColor = if (booking.isLocked) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                }
                                            ) {
                                                Text(
                                                    text = if (booking.isLocked) "GoBD Fest" else "Entwurf",
                                                    modifier = Modifier.padding(horizontal = 4.dp),
                                                    color = if (booking.isLocked) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSecondaryContainer
                                                    }
                                                )
                                            }
                                            if (hasAttachment) {
                                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                                    Text(
                                                        text = if (isImage) "FOTO-BELEG" else "PDF-BELEG",
                                                        modifier = Modifier.padding(horizontal = 4.dp),
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                            if (booking.appliedTenDayRule) {
                                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                                    Text(
                                                        text = "10-Tages-Regel",
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = booking.descriptionDe,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${booking.counterpartyName} · Kz ${booking.category.anlageEurKz}" +
                                                (booking.attachmentFileName?.let { " · 📎 $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${if (booking.category.isIncome) "+ " else "- "}${Money.formatEuroFromCents(booking.grossAmountCents)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (booking.category.isIncome) {
                                                KlartaxSuccessGreen
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = "Netto ${Money.formatEuroFromCents(booking.netAmountCents)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                selectedBooking?.let { booking ->
                    var correctionEuroInput by remember(booking.id) {
                        mutableStateOf(Money.fromCents(booking.netAmountCents).toPlainString())
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = booking.documentNumber,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = booking.descriptionDe,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                                Text(
                                    text = Money.formatEuroFromCents(booking.grossAmountCents),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (booking.category.isIncome) KlartaxSuccessGreen else MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ExpressiveStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Netto-Betrag",
                                    value = Money.formatEuroFromCents(booking.netAmountCents)
                                )
                                ExpressiveStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = "USt (${booking.vatRatePercent} %)",
                                    value = Money.formatEuroFromCents(booking.vatAmountCents)
                                )
                                ExpressiveStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Abzugsfähig (EÜR)",
                                    value = Money.formatEuroFromCents(booking.deductibleNetCents)
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val isImg = booking.attachmentMimeType?.startsWith("image") == true
                                            Icon(
                                                imageVector = if (isImg) Icons.Default.Image else Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = stringResource(R.string.expense_attachment_preview_title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (!booking.isLocked) {
                                            OutlinedButton(
                                                onClick = {
                                                    filePickerLauncher.launch(
                                                        arrayOf("application/pdf", "image/jpeg", "image/png", "application/xml")
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PDF / Foto wählen")
                                            }
                                        }
                                    }

                                    if (!booking.attachmentFileName.isNullOrBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = booking.attachmentFileName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                                        val kb = booking.attachmentSizeBytes / 1024L
                                                        Text(
                                                            text = "${booking.attachmentMimeType ?: "application/pdf"} · $kb KB",
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                HorizontalDivider()
                                                Text(
                                                    text = "AUSSTELLER: ${booking.counterpartyName}\n" +
                                                        "BELEGDATUM: ${booking.invoiceOrDueDateIso} | ZAHLUNG: ${booking.paymentDateIso}\n" +
                                                        "POSITION: ${booking.descriptionDe}\n" +
                                                        "NETTO: ${Money.formatEuroFromCents(booking.netAmountCents)} + " +
                                                        "UST (${booking.vatRatePercent}%): ${Money.formatEuroFromCents(booking.vatAmountCents)} = " +
                                                        "BRUTTO: ${Money.formatEuroFromCents(booking.grossAmountCents)}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Noch kein PDF- oder Foto-Beleg angehängt.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Amtliche EÜR- & SKR-Kontierung",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${booking.category.displayNameDe} · ${booking.category.anlageEurLineLabel} (Kz ${booking.category.anlageEurKz})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "SKR03: ${booking.category.skr03Account} · SKR04: ${booking.category.skr04Account} · Zahlungsdatum: ${booking.paymentDateIso}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (booking.isLocked) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text(
                                                text = "GoBD-Festschreibung aktiv (SHA-256 verkettet)",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = correctionEuroInput,
                                                onValueChange = { correctionEuroInput = it },
                                                label = { Text("Korrigierter Netto-Betrag (€)") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    val newCents = runCatching {
                                                        Money.toCents(Money.of(correctionEuroInput))
                                                    }.getOrDefault(booking.netAmountCents)
                                                    onTriggerStornoCorrection(
                                                        booking,
                                                        newCents,
                                                        "GoBD-Korrekturbuchung durch Steuerpflichtigen"
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Storno & Korrektur")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ExpressiveStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * 4. Durchgängiges SevDesk Angebots-, Auftrags- & E-Rechnungs-Studio (`ExpressiveEInvoiceStudioScreen`):
 * - Erstellen von Angeboten (`AN`), Auftragsbestätigungen (`AB`) und E-Rechnungen (`RE`)
 * - 1-Klick-Auswahl von Produkten & Dienstleistungen aus dem Katalog (`ServiceCatalogItemEntity`)
 * - 1-Klick-Auswahl von Standardtexten / Textbausteinen (`DocumentTextTemplateEntity`)
 * - 1-Klick-Umwandlung in der Belegkette: `Angebot (AN) → Auftrag (AB) → E-Rechnung (RE)`
 * - Live DIN-A4 Briefpapier-Vorschau inkl. Firmenlogo, DIN-5008-Adressfenster, Falzmarken, GiroCode & 3-spaltiger Fußzeile
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressiveEInvoiceStudioScreen(
    state: KlartaxAppUiState,
    selectedCustomerId: String?,
    onSelectCustomerId: (String?) -> Unit,
    onIssueFullInvoice: (
        customerId: String?,
        customerName: String,
        customerStreet: String,
        customerPostalCode: String,
        customerCity: String,
        customerCountryIso: String,
        customerVatId: String?,
        leitwegId: String,
        documentType: String,
        eInvoiceStandard: String,
        taxMode: String,
        paymentTermDays: Int,
        lineItems: List<InvoiceLineItem>,
        bookDirectlyAsPaidInEur: Boolean,
        linkedQuoteId: String?,
        linkedQuoteNumber: String?,
        linkedOrderId: String?,
        linkedOrderNumber: String?,
        subjectTitleDe: String,
        headerIntroTextDe: String,
        footerClosingTextDe: String
    ) -> Unit,
    onConvertQuoteToOrder: (InvoiceEntity) -> Unit,
    onConvertToEInvoice: (InvoiceEntity) -> Unit,
    onMarkInvoicePaid: (InvoiceEntity) -> Unit,
    onCreateStornoInvoice: (InvoiceEntity) -> Unit
) {
    val activeCustomer = state.customers.find { it.id == selectedCustomerId } ?: state.customers.firstOrNull()
    val stationery = state.stationeryConfig

    var documentType by remember { mutableStateOf("INVOICE") }
    var eInvoiceStandard by remember { mutableStateOf("ZUGFERD_2_3") }
    var taxMode by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.defaultTaxMode ?: "STANDARD_19")
    }

    var customerName by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.companyOrFullName ?: "Süddeutsche FinTech Solutions AG")
    }
    var customerStreet by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.streetAndNumber ?: "Maximilianstraße 42")
    }
    var customerPostalCode by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.postalCode ?: "80539")
    }
    var customerCity by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.city ?: "München")
    }
    var customerCountryIso by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.countryIso ?: "DE")
    }
    var customerVatId by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.vatId ?: "DE129485712")
    }
    var leitwegId by remember(activeCustomer) {
        mutableStateOf(activeCustomer?.buyerReferenceLeitwegId ?: "991-12345-67")
    }
    var paymentTermDays by remember(activeCustomer) {
        mutableIntStateOf(activeCustomer?.paymentTermDays ?: 14)
    }

    // Verknüpfung zu Angebot / Auftrag & Textbausteine
    var linkedQuoteNumber by remember { mutableStateOf<String?>("AN-2026-002") }
    var linkedOrderNumber by remember { mutableStateOf<String?>(null) }

    val defaultIntroForType = remember(documentType, state.documentTextTemplates) {
        state.documentTextTemplates.firstOrNull {
            it.documentScope == documentType && it.placementType == "HEADER_INTRO"
        }?.bodyTextDe
            ?: "Sehr geehrte Damen und Herren, vielen Dank für die Zusammenarbeit. Wir berechnen Ihnen folgende Positionen:"
    }
    val defaultFooterForType = remember(documentType, state.documentTextTemplates) {
        state.documentTextTemplates.firstOrNull {
            it.documentScope == documentType && it.placementType == "FOOTER_CLOSING"
        }?.bodyTextDe
            ?: "Zahlbar innerhalb des vereinbarten Zahlungsziels ohne Abzug."
    }

    var subjectTitleDe by remember(documentType) {
        mutableStateOf(
            when (documentType) {
                "QUOTE" -> "Angebot: Q4 Architektur- & E-Rechnungs-Integration"
                "ORDER_CONFIRMATION" -> "Auftragsbestätigung: Q4 Architektur- & E-Rechnungs-Integration"
                else -> "Honorarrechnung: Q4 Architektur- & E-Rechnungs-Integration"
            }
        )
    }
    var headerIntroTextDe by remember(defaultIntroForType) { mutableStateOf(defaultIntroForType) }
    var footerClosingTextDe by remember(defaultFooterForType) { mutableStateOf(defaultFooterForType) }

    var bookDirectlyAsPaid by remember { mutableStateOf(false) }
    var showRawXmlTab by remember { mutableStateOf(false) }

    val lineItems = remember(activeCustomer) {
        val defaultRateCents = activeCustomer?.defaultHourlyRateNetCents ?: 150_00L
        mutableStateListOf(
            InvoiceLineItem(
                positionNumber = 1,
                title = "Principal FinTech-Architektur & Kotlin Multiplatform Engineering",
                description = "Leistungszeitraum gemäß Stundennachweis",
                quantityHoursOrUnits = "40.00",
                unitCode = "HUR",
                unitPriceNetCents = defaultRateCents,
                vatRatePercent = 19,
                lineNetTotalCents = defaultRateCents * 40L,
                articleNumber = "DL-101"
            )
        )
    }

    var newPosTitle by remember { mutableStateOf("") }
    var newPosQty by remember { mutableStateOf("8.00") }
    var newPosUnitPriceEuro by remember(activeCustomer) {
        mutableStateOf(Money.fromCents(activeCustomer?.defaultHourlyRateNetCents ?: 150_00L).toPlainString())
    }

    val vatRateInt = when (taxMode) {
        "REDUCED_7" -> 7
        "KLEINUNTERNEHMER_SEC19", "REVERSE_CHARGE_SEC13B" -> 0
        else -> 19
    }

    val liveNetCents = lineItems.sumOf { it.lineNetTotalCents }
    val liveNetBd = Money.fromCents(liveNetCents)
    val liveVatBd = Money.percentage(liveNetBd, Money.of(vatRateInt.toString()))
    val liveGrossBd = liveNetBd.add(liveVatBd)

    var previewedSavedInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Linke Spalte: SevDesk Angebots-, Auftrags- & Rechnungs-Konfigurator
        Card(
            modifier = Modifier
                .weight(1.12f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Angebote (AN) · Aufträge (AB) · Rechnungen (RE)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Verknüpfte Belegkette + Produkte/Leistungen + Textbausteine + DIN-5008 Briefpapier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onAddTimeTrackingEntry(
                                    "Sprint & Architektur-Beratung",
                                    customerName,
                                    "Technische Konzeption & Code-Review",
                                    120,
                                    "150,00"
                                )
                            }
                        ) {
                            Text("⏱ +2 Std. erfassen")
                        }
                        OutlinedButton(
                            onClick = {
                                val unbilled = state.timeEntries.filter { !it.isBilled }
                                if (unbilled.isNotEmpty()) {
                                    unbilled.forEach { entry ->
                                        val hrs = BigDecimal(entry.durationMinutes)
                                            .divide(BigDecimal("60"), 2, RoundingMode.HALF_UP)
                                        val netLine = hrs.multiply(Money.fromCents(entry.hourlyRateNetCents))
                                            .setScale(2, RoundingMode.HALF_UP)
                                        lineItems.add(
                                            InvoiceLineItem(
                                                positionNumber = lineItems.size + 1,
                                                title = "${entry.projectName}: ${entry.taskDescription}",
                                                description = "Zeiterfassung vom ${entry.dateIso}",
                                                quantityHoursOrUnits = hrs.toPlainString(),
                                                unitCode = "HUR",
                                                unitPriceNetCents = entry.hourlyRateNetCents,
                                                vatRatePercent = vatRateInt,
                                                lineNetTotalCents = Money.toCents(netLine),
                                                articleNumber = "ZEIT"
                                            )
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("+ Zeiterfassung (${state.timeEntries.count { !it.isBilled }})")
                        }
                    }
                }

                // 1. Dokumenttyp (Angebot -> Auftrag -> Rechnung)
                Text("1. Belegart in der SevDesk-Kette wählen:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = documentType == "QUOTE",
                        onClick = { documentType = "QUOTE"; previewedSavedInvoice = null },
                        label = { Text("Angebot (AN)") }
                    )
                    FilterChip(
                        selected = documentType == "ORDER_CONFIRMATION",
                        onClick = { documentType = "ORDER_CONFIRMATION"; previewedSavedInvoice = null },
                        label = { Text("Auftragsbestätigung / Auftrag (AB)") }
                    )
                    FilterChip(
                        selected = documentType == "INVOICE",
                        onClick = { documentType = "INVOICE"; previewedSavedInvoice = null },
                        label = { Text("E-Rechnung (RE · EN 16931)") }
                    )
                }

                // 2. Kunde aus Kundenverwaltung wählen
                Text("2. Kunde aus Stammdaten wählen:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.customers.forEach { cust ->
                        FilterChip(
                            selected = activeCustomer?.id == cust.id,
                            onClick = {
                                onSelectCustomerId(cust.id)
                                previewedSavedInvoice = null
                            },
                            label = { Text("${cust.customerNumber}: ${cust.companyOrFullName}") }
                        )
                    }
                }

                // Verknüpfung zu Angebot / Auftrag + Leitweg-ID
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = linkedQuoteNumber.orEmpty(),
                        onValueChange = { linkedQuoteNumber = it.ifBlank { null } },
                        label = { Text("Verknüpftes Angebot (AN)") },
                        modifier = Modifier.weight(0.33f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = linkedOrderNumber.orEmpty(),
                        onValueChange = { linkedOrderNumber = it.ifBlank { null } },
                        label = { Text("Verknüpfter Auftrag (AB)") },
                        modifier = Modifier.weight(0.33f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = leitwegId,
                        onValueChange = { leitwegId = it },
                        label = { Text("Leitweg-ID (BT-10)") },
                        modifier = Modifier.weight(0.34f),
                        singleLine = true
                    )
                }

                // 3. Produkte & angebotene Dienstleistungen mit 1 Klick aus dem Katalog einfügen
                Text(
                    text = "3. Produkte & Dienstleistungen aus Katalog hinzufügen (1-Klick):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.serviceCatalogItems.forEach { catItem ->
                        AssistChip(
                            onClick = {
                                previewedSavedInvoice = null
                                val qtyBd = runCatching { BigDecimal(catItem.defaultQuantity) }.getOrDefault(BigDecimal.ONE)
                                val netLineBd = qtyBd.multiply(Money.fromCents(catItem.unitPriceNetCents))
                                    .setScale(2, RoundingMode.HALF_UP)
                                lineItems.add(
                                    InvoiceLineItem(
                                        positionNumber = lineItems.size + 1,
                                        title = catItem.titleDe,
                                        description = catItem.descriptionDe,
                                        quantityHoursOrUnits = catItem.defaultQuantity,
                                        unitCode = catItem.unitCode,
                                        unitPriceNetCents = catItem.unitPriceNetCents,
                                        vatRatePercent = catItem.vatRatePercent,
                                        lineNetTotalCents = Money.toCents(netLineBd),
                                        articleNumber = catItem.articleNumber
                                    )
                                )
                            },
                            label = {
                                Text("+ [${catItem.articleNumber}] ${catItem.titleDe.take(28)}… (${Money.formatEuroFromCents(catItem.unitPriceNetCents)})")
                            }
                        )
                    }
                }

                // Aktuelle Positionen
                lineItems.forEachIndexed { index, item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pos. ${index + 1} ${item.articleNumber?.let { "[$it]" }.orEmpty()}: ${item.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.quantityHoursOrUnits} ${item.unitCode} × ${Money.formatEuroFromCents(item.unitPriceNetCents)} Netto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Money.formatEuroFromCents(item.lineNetTotalCents),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black
                                )
                                if (lineItems.size > 1) {
                                    IconButton(onClick = { lineItems.removeAt(index) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Entfernen")
                                    }
                                }
                            }
                        }
                    }
                }

                // Freie Zusatzposition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPosTitle,
                        onValueChange = { newPosTitle = it },
                        label = { Text("Individuelle Position") },
                        modifier = Modifier.weight(0.45f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPosQty,
                        onValueChange = { newPosQty = it },
                        label = { Text("Menge") },
                        modifier = Modifier.weight(0.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPosUnitPriceEuro,
                        onValueChange = { newPosUnitPriceEuro = it },
                        label = { Text("Netto €") },
                        modifier = Modifier.weight(0.25f),
                        singleLine = true
                    )
                    OutlinedButton(
                        onClick = {
                            val qtyBd = runCatching { BigDecimal(newPosQty.replace(",", ".")) }.getOrDefault(BigDecimal.ONE)
                            val unitBd = runCatching { Money.of(newPosUnitPriceEuro) }.getOrDefault(Money.of("150.00"))
                            val lineNetBd = qtyBd.multiply(unitBd).setScale(2, RoundingMode.HALF_UP)
                            lineItems.add(
                                InvoiceLineItem(
                                    positionNumber = lineItems.size + 1,
                                    title = newPosTitle.ifBlank { "Zusatzleistung" },
                                    description = "Individuelle Position",
                                    quantityHoursOrUnits = qtyBd.toPlainString(),
                                    unitCode = "HUR",
                                    unitPriceNetCents = Money.toCents(unitBd),
                                    vatRatePercent = vatRateInt,
                                    lineNetTotalCents = Money.toCents(lineNetBd),
                                    articleNumber = "INDIV"
                                )
                            )
                            newPosTitle = ""
                        }
                    ) {
                        Text("+ Pos.")
                    }
                }

                // 4. Standardtexte / Textbausteine auswählen
                Text(
                    text = "4. Standardtexte & Textbausteine (Kopftext & Zahlungsbedingungen):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.documentTextTemplates.forEach { tpl ->
                        AssistChip(
                            onClick = {
                                previewedSavedInvoice = null
                                if (tpl.placementType == "HEADER_INTRO") {
                                    headerIntroTextDe = tpl.bodyTextDe
                                } else {
                                    footerClosingTextDe = tpl.bodyTextDe
                                }
                            },
                            label = { Text(tpl.templateTitleDe) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subjectTitleDe,
                    onValueChange = { subjectTitleDe = it },
                    label = { Text("Betreffzeile auf dem Briefpapier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = headerIntroTextDe,
                    onValueChange = { headerIntroTextDe = it },
                    label = { Text("Kopftext / Einleitungstext") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = footerClosingTextDe,
                    onValueChange = { footerClosingTextDe = it },
                    label = { Text("Schlusstext / Zahlungsbedingungen / Skonto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        previewedSavedInvoice = null
                        onIssueFullInvoice(
                            activeCustomer?.id,
                            customerName,
                            customerStreet,
                            customerPostalCode,
                            customerCity,
                            customerCountryIso,
                            customerVatId.ifBlank { null },
                            leitwegId,
                            documentType,
                            eInvoiceStandard,
                            taxMode,
                            paymentTermDays,
                            lineItems.toList(),
                            bookDirectlyAsPaid,
                            null,
                            linkedQuoteNumber,
                            null,
                            linkedOrderNumber,
                            subjectTitleDe,
                            headerIntroTextDe,
                            footerClosingTextDe
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (documentType) {
                            "QUOTE" -> "Angebot (AN) erstellen & speichern (${Money.formatEuro(liveGrossBd)})"
                            "ORDER_CONFIRMATION" -> "Auftragsbestätigung (AB) erstellen (${Money.formatEuro(liveGrossBd)})"
                            else -> "E-Rechnung (RE) GoBD-konform festschreiben (${Money.formatEuro(liveGrossBd)})"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                // 5. Verknüpfte Belegkette (Angebote -> Aufträge -> Rechnungen) mit 1-Klick-Umwandlung
                Text(
                    text = "5. Verknüpfte Angebote, Aufträge & Rechnungen (${state.invoices.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                state.invoices.forEach { doc ->
                    val chainText = listOfNotNull(
                        doc.linkedQuoteNumber?.let { "🔗 Angebot $it" },
                        doc.linkedOrderNumber?.let { "🔗 Auftrag $it" }
                    ).joinToString(" · ")

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previewedSavedInvoice = doc },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Badge(
                                        containerColor = when (doc.documentType) {
                                            "QUOTE" -> MaterialTheme.colorScheme.secondary
                                            "ORDER_CONFIRMATION" -> Color(0xFF0F766E)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ) {
                                        Text(
                                            text = doc.documentType,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                    Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(doc.status, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                    Text(
                                        text = "${doc.invoiceNumber} · ${doc.customerName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = Money.formatEuroFromCents(doc.grossTotalCents),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (chainText.isNotBlank()) {
                                Text(
                                    text = chainText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (doc.documentType == "QUOTE") {
                                    OutlinedButton(onClick = { onConvertQuoteToOrder(doc) }) {
                                        Text("→ In Auftrag (AB) umwandeln")
                                    }
                                    Button(onClick = { onConvertToEInvoice(doc) }) {
                                        Text("→ In E-Rechnung (RE) umwandeln")
                                    }
                                } else if (doc.documentType == "ORDER_CONFIRMATION") {
                                    Button(onClick = { onConvertToEInvoice(doc) }) {
                                        Text("→ Auftrag in E-Rechnung (RE) umwandeln")
                                    }
                                } else if (doc.documentType == "INVOICE") {
                                    if (doc.status != "PAID") {
                                        OutlinedButton(onClick = { onMarkInvoicePaid(doc) }) {
                                            Text("Bezahlt buchen (EÜR)")
                                        }
                                    }
                                    if (doc.status != "STORNO_CANCELLED") {
                                        TextButton(onClick = { onCreateStornoInvoice(doc) }) {
                                            Text("GoBD-Storno (381)")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rechte Spalte: Live DIN-A4 Briefpapier mit Firmenlogo, Textbausteinen, Positionen, GiroCode & 3-Spalten-Fußzeile
        val displayedDoc = previewedSavedInvoice
        val displayedLineItems = remember(displayedDoc, lineItems.toList()) {
            if (displayedDoc != null) {
                runCatching {
                    Json { ignoreUnknownKeys = true }.decodeFromString<List<InvoiceLineItem>>(displayedDoc.lineItemsJson)
                }.getOrDefault(lineItems.toList())
            } else {
                lineItems.toList()
            }
        }

        Card(
            modifier = Modifier
                .weight(0.98f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (displayedDoc != null) "Briefpapier-Vorschau: ${displayedDoc.invoiceNumber}" else "Live Briefpapier-Vorschau",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = !showRawXmlTab,
                            onClick = { showRawXmlTab = false },
                            label = { Text("DIN-A4 Briefpapier & Logo") }
                        )
                        FilterChip(
                            selected = showRawXmlTab,
                            onClick = { showRawXmlTab = true },
                            label = { Text("EN 16931 CII XML") }
                        )
                    }
                }

                if (!showRawXmlTab) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(22.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Briefkopf mit Firmenlogo aus StationeryConfigEntity
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stationery?.companyDisplayName
                                                ?: "${state.activeProfile.taxpayerFirstName} ${state.activeProfile.taxpayerLastName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF7A1624),
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = stationery?.companySubtitleOrClaim
                                                ?: "FinTech-Architektur & Steuer-Beratung",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFF7A1624),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = stationery?.logoMonogramText ?: "LW",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = displayedDoc?.eInvoiceStandard ?: eInvoiceStandard,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFFD9DC)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF7A1624), thickness = 1.5.dp)

                                // DIN 5008 Adressfenster + Beleg-Metadaten (inkl. Angebots-/Auftrags-Referenz)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stationery?.senderWindowLineDe
                                                ?: "Lukas Weber · Leopoldstraße 88 · 80802 München",
                                            fontSize = 9.sp,
                                            color = Color(0xFF524345)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${displayedDoc?.customerName ?: customerName}\n" +
                                                "${displayedDoc?.customerStreet ?: customerStreet}\n" +
                                                "${displayedDoc?.customerPostalCode ?: customerPostalCode} ${displayedDoc?.customerCity ?: customerCity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF1A1517),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Beleg-Nr.: ${displayedDoc?.invoiceNumber ?: "NEU"}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF7A1624),
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "Leitweg-ID: ${displayedDoc?.buyerReferenceLeitwegId ?: leitwegId}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.Bold
                                        )
                                        val refQuote = displayedDoc?.linkedQuoteNumber ?: linkedQuoteNumber
                                        val refOrder = displayedDoc?.linkedOrderNumber ?: linkedOrderNumber
                                        if (!refQuote.isNullOrBlank()) {
                                            Text("Ref. Angebot: $refQuote", fontSize = 10.sp, color = Color(0xFF524345))
                                        }
                                        if (!refOrder.isNullOrBlank()) {
                                            Text("Ref. Auftrag: $refOrder", fontSize = 10.sp, color = Color(0xFF524345))
                                        }
                                    }
                                }

                                // Betreff & Kopftext (Textbaustein)
                                Text(
                                    text = displayedDoc?.subjectTitleDe?.ifBlank { subjectTitleDe } ?: subjectTitleDe,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF1A1517),
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = displayedDoc?.headerIntroTextDe?.ifBlank { headerIntroTextDe } ?: headerIntroTextDe,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF333333)
                                )

                                // Positionstabelle
                                displayedLineItems.forEach { pos ->
                                    Surface(
                                        color = Color(0xFFF6F8FC),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Pos. ${pos.positionNumber} ${pos.articleNumber?.let { "[$it]" }.orEmpty()}: ${pos.title}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1A1517),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "${pos.quantityHoursOrUnits} ${pos.unitCode} × ${Money.formatEuroFromCents(pos.unitPriceNetCents)} Netto",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF524345)
                                                )
                                            }
                                            Text(
                                                text = Money.formatEuroFromCents(pos.lineNetTotalCents),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A1517),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                val previewNet = displayedDoc?.let { Money.fromCents(it.netTotalCents) } ?: liveNetBd
                                val previewVat = displayedDoc?.let { Money.fromCents(it.vatTotalCents) } ?: liveVatBd
                                val previewGross = displayedDoc?.let { Money.fromCents(it.grossTotalCents) } ?: liveGrossBd

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (stationery?.showEpcGiroCodeQr != false) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFFDCE4FF),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.QrCode2,
                                                    contentDescription = "EPC GiroCode",
                                                    tint = Color(0xFF1E3A8A),
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .padding(6.dp)
                                                )
                                            }
                                            Text(
                                                text = "EPC-GiroCode\nSEPA-Überweisung",
                                                fontSize = 10.sp,
                                                color = Color(0xFF1E3A8A),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Netto: ${Money.formatEuro(previewNet)}", fontSize = 11.sp, color = Color(0xFF524345))
                                        Text("USt ($vatRateInt %): ${Money.formatEuro(previewVat)}", fontSize = 11.sp, color = Color(0xFF524345))
                                        Text(
                                            text = "Gesamt: ${Money.formatEuro(previewGross)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF7A1624),
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // Schlusstext / Zahlungsbedingungen (Textbaustein)
                                Text(
                                    text = displayedDoc?.footerClosingTextDe?.ifBlank { footerClosingTextDe } ?: footerClosingTextDe,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF333333)
                                )
                            }

                            // 3-spaltige Briefpapier-Fußzeile
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                HorizontalDivider(color = Color(0xFFDADCE0))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = stationery?.footerCol1AddressDe ?: "Lukas Weber · München",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 9.sp,
                                        color = Color(0xFF524345)
                                    )
                                    Text(
                                        text = stationery?.footerCol2BankDe ?: "IBAN: DE89 7008 0000 0512 3456 00",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 9.sp,
                                        color = Color(0xFF524345)
                                    )
                                    Text(
                                        text = stationery?.footerCol3TaxDe ?: "USt-IdNr.: DE314159265",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 9.sp,
                                        color = Color(0xFF524345)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val xmlPreview = displayedDoc?.embeddedXmlPayload
                        ?: state.invoices.firstOrNull { it.embeddedXmlPayload.isNotBlank() }?.embeddedXmlPayload.orEmpty()
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF131112),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = xmlPreview,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFDCE4FF)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. WISO Einkommensteuer 3-Pane Studio (`ExpressiveWiso3PaneScreen`)
 */
@Composable
private fun ExpressiveWiso3PaneScreen(
    state: KlartaxAppUiState,
    selectedTopic: WisoInterviewTopic,
    onSelectTopic: (WisoInterviewTopic) -> Unit,
    onNextTopic: () -> Unit,
    onToggleSplitting: (Boolean) -> Unit,
    onUpdateHomeOfficeDays: (Int) -> Unit,
    onUpdateCommuteKm: (Int) -> Unit,
    onUpdateCommuteDays: (Int) -> Unit,
    onUpdateWorkEquipmentEuro: (String) -> Unit,
    onUpdateChildcareCents: (Long) -> Unit,
    onUpdateSec35aLaborCents: (de.klartax.app.data.local.entities.SpecialExpenseAndBurdenEntity, Long) -> Unit,
    onApplyNebenkostenSec35a: (
        caretakerEuro: String,
        gardenEuro: String,
        cleaningEuro: String,
        chimneyEuro: String,
        winterServiceEuro: String
    ) -> Unit
) {
    val summary = state.taxSummary
    val emp = state.employmentRecords.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(0.85f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "WISO STEUER-THEMENBAUM",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                WisoInterviewTopic.entries.forEachIndexed { index, topic ->
                    val isSelected = selectedTopic == topic
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTopic(topic) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = stringResource(topic.titleResId),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1.45f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(selectedTopic.titleResId),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(onClick = onNextTopic) {
                        Text("Nächstes Thema →")
                    }
                }

                HorizontalDivider()

                when (selectedTopic) {
                    WisoInterviewTopic.PERSONAL_FAMILY -> {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Zusammenveranlagung (Ehegattensplitting § 32a Abs. 5 EStG)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Live-Steuerersparnis durch Splittingtarif: + ${Money.formatEuro(summary.splittingAdvantageEuro)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = state.activeProfile.isMarriedJointAssessment,
                                    onCheckedChange = onToggleSplitting
                                )
                            }
                        }
                    }

                    WisoInterviewTopic.FREELANCE_EUR_BRIDGE -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "1-Klick-Brücke: Anlage EÜR → Anlage S (Zeile 4)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ExpressiveStatBox(
                                        modifier = Modifier.weight(1f),
                                        label = "Betriebseinnahmen",
                                        value = Money.formatEuro(summary.eurResult.netOperatingIncome)
                                    )
                                    ExpressiveStatBox(
                                        modifier = Modifier.weight(1f),
                                        label = "Ausgaben + AfA",
                                        value = Money.formatEuro(
                                            summary.eurResult.deductibleOperatingExpenses.add(summary.eurResult.totalAfaDeduction)
                                        )
                                    )
                                    ExpressiveStatBox(
                                        modifier = Modifier.weight(1f),
                                        label = "EÜR-Gewinn (§ 4 Abs. 3)",
                                        value = Money.formatEuro(summary.eurResult.freelanceProfit)
                                    )
                                }
                            }
                        }
                    }

                    WisoInterviewTopic.ANLAGE_N_COMMUTE_HOMEOFFICE -> {
                        if (emp != null) {
                            var equipmentInput by remember(emp.workEquipmentCostCents) {
                                mutableStateOf(Money.fromCents(emp.workEquipmentCostCents).toPlainString())
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Homeoffice-Pauschale (§ 4 Abs. 5 Nr. 6c EStG)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "6,00 € pro Tag bis max. 210 Tage (1.260,00 €)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${emp.homeOfficeDays} Tage = ${Money.formatEuro(BigDecimal(emp.homeOfficeDays * 6))}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Slider(
                                        value = emp.homeOfficeDays.toFloat(),
                                        onValueChange = { newVal ->
                                            onUpdateHomeOfficeDays(newVal.toInt() - emp.homeOfficeDays)
                                        },
                                        valueRange = 0f..210f
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { onUpdateHomeOfficeDays(-10) }) { Text("- 10 Tage") }
                                        OutlinedButton(onClick = { onUpdateHomeOfficeDays(+10) }) { Text("+ 10 Tage") }
                                        Button(onClick = { onUpdateHomeOfficeDays(210 - emp.homeOfficeDays) }) {
                                            Text("Maximum (210 Tage)")
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Entfernungspauschale (0,30 € / 0,38 € ab 21. km)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${emp.commuteDistanceOneWayKm} km · ${emp.commuteWorkdaysPerYear} Tage",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Slider(
                                        value = emp.commuteDistanceOneWayKm.toFloat(),
                                        onValueChange = { onUpdateCommuteKm(it.toInt()) },
                                        valueRange = 1f..100f
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = equipmentInput,
                                    onValueChange = { equipmentInput = it },
                                    label = { Text("Arbeitsmittel & Fachliteratur (€)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(onClick = { onUpdateWorkEquipmentEuro(equipmentInput) }) {
                                    Text("Übernehmen")
                                }
                            }
                        }
                    }

                    WisoInterviewTopic.ANLAGE_KIND -> {
                        val firstChild = state.children.firstOrNull()
                        if (firstChild != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "${firstChild.childFirstName} · Kinderbetreuungskosten (80 % bis 4.800 €)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Aktuell erfasst: ${Money.formatEuroFromCents(firstChild.childcareExpenseTotalCents)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black
                                    )
                                    Slider(
                                        value = (firstChild.childcareExpenseTotalCents / 100f),
                                        onValueChange = { euroFloat ->
                                            onUpdateChildcareCents(euroFloat.toLong() * 100L)
                                        },
                                        valueRange = 0f..6000f
                                    )
                                }
                            }
                        }
                    }

                    WisoInterviewTopic.SEC35A_HANDWERKER_NEBENKOSTEN -> {
                        state.specialExpenses
                            .filter { it.recordCategory.contains("35A") }
                            .forEach { rec ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rec.titleDe, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(
                                                "20 % Direktabzug: ${Money.formatEuroFromCents(rec.eligibleLaborAndMachineCents * 20 / 100)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { onUpdateSec35aLaborCents(rec, rec.eligibleLaborAndMachineCents + 250_00L) }
                                            ) {
                                                Text("+ 250 € Lohnanteil")
                                            }
                                            if (rec.eligibleLaborAndMachineCents >= 250_00L) {
                                                OutlinedButton(
                                                    onClick = { onUpdateSec35aLaborCents(rec, rec.eligibleLaborAndMachineCents - 250_00L) }
                                                ) {
                                                    Text("- 250 € Lohnanteil")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        // Interaktiver Nebenkostenabrechnungs-Assistent (§ 35a EStG)
                        var nkCaretaker by remember { mutableStateOf("420,00") }
                        var nkGarden by remember { mutableStateOf("280,00") }
                        var nkCleaning by remember { mutableStateOf("310,00") }
                        var nkChimney by remember { mutableStateOf("95,00") }
                        var nkWinter by remember { mutableStateOf("185,00") }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Nebenkostenabrechnungs-Assistent (§ 35a EStG · 20 % Direktabzug)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = nkCaretaker,
                                        onValueChange = { nkCaretaker = it },
                                        label = { Text("Hausmeister (€)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = nkGarden,
                                        onValueChange = { nkGarden = it },
                                        label = { Text("Gartenpflege (€)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = nkCleaning,
                                        onValueChange = { nkCleaning = it },
                                        label = { Text("Reinigung (€)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = nkChimney,
                                        onValueChange = { nkChimney = it },
                                        label = { Text("Schornsteinfeger (€)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = nkWinter,
                                        onValueChange = { nkWinter = it },
                                        label = { Text("Winterdienst (€)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            onApplyNebenkostenSec35a(
                                                nkCaretaker,
                                                nkGarden,
                                                nkCleaning,
                                                nkChimney,
                                                nkWinter
                                            )
                                        }
                                    ) {
                                        Text("20 % Steuerabzug übernehmen")
                                    }
                                }
                            }
                        }
                    }

                    WisoInterviewTopic.ANLAGE_KAP_CRYPTO -> {
                        state.capitalAndCrypto.forEach { cap ->
                            ExpressiveStatBox(
                                modifier = Modifier.fillMaxWidth(),
                                label = cap.institutionOrWalletName,
                                value = "Gewinn: ${Money.formatEuroFromCents(cap.capitalGainsGrossCents + cap.cryptoProfitCents)}"
                            )
                        }
                    }

                    WisoInterviewTopic.ANLAGE_V_RENTAL -> {
                        state.rentalProperties.forEach { prop ->
                            ExpressiveStatBox(
                                modifier = Modifier.fillMaxWidth(),
                                label = prop.propertyAddressDe,
                                value = "Einkünfte V+V: ${Money.formatEuro(summary.rentalIncomeOrLossV)}"
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "LIVE-STEUERBERECHNUNG (§ 32a EStG)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${if (summary.isRefund) "+ " else "- "}${Money.formatEuro(summary.expectedRefundOrBackPayment.abs())}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    SummaryLine("EÜR-Gewinn (Anlage S)", Money.formatEuro(summary.eurResult.freelanceProfit))
                    SummaryLine("Einkünfte Anlage N", Money.formatEuro(summary.taxableEmploymentIncomeN))
                    SummaryLine("Gesamtbetrag d. Einkünfte", Money.formatEuro(summary.totalIncomeGdE))
                    SummaryLine("Sonderausgaben & Vorsorge", "- ${Money.formatEuro(summary.specialExpensesDeduction.add(summary.pensionDeductionSec10))}")
                    SummaryLine("Zu verst. Einkommen (zvE)", Money.formatEuro(summary.taxableIncomeZvE))
                    SummaryLine("Tarifliche ESt (§ 32a)", Money.formatEuro(summary.tariffIncomeTax))
                    SummaryLine("§ 35a Direktabzug (20 %)", "- ${Money.formatEuro(summary.sec35aTaxCreditTotal)}")
                    SummaryLine("Festgesetzte ESt + KiSt", Money.formatEuro(summary.totalTaxLiability))
                    SummaryLine("Gezahlte Lohnsteuer & VZ", Money.formatEuro(summary.totalPrepaidTaxes))
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Grenzsteuersatz: ${summary.marginalTaxRatePercent} % · Durchschnittssteuer: ${summary.effectiveTaxRatePercent} %",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * 6. ELSTER, Bescheid-Prüfer & Zero-Knowledge Sync (`ExpressiveElsterBescheidSyncScreen`)
 */
@Composable
private fun ExpressiveElsterBescheidSyncScreen(
    state: KlartaxAppUiState,
    syncState: de.klartax.app.data.sync.SyncUiState,
    onTriggerSync: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val notice = state.assessmentNotices.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    label = { Text("KI-Steuerbescheid-Prüfer & Einspruch") }
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    label = { Text("ELSTER-XML & DATEV-CSV") }
                )
                FilterChip(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    label = { Text("Google Drive Sync & GoBD-Kette (${state.gobdVerification.verifiedEntriesCount}/5)") }
                )
            }

            Button(onClick = onTriggerSync) {
                Icon(Icons.Default.CloudDone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zero-Knowledge Sync (Strg+S)")
            }
        }

        when (activeTab) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("SOLL-IST-ABWEICHUNGSANALYSE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            if (notice != null) {
                                ExpressiveStatBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "Klartax Berechnung (SOLL)",
                                    value = Money.formatEuroFromCents(notice.expectedRefundCents)
                                )
                                ExpressiveStatBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = "Finanzamt Bescheid (IST)",
                                    value = Money.formatEuroFromCents(notice.actualAssessedRefundCents)
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Abweichung: ${Money.formatEuroFromCents(notice.discrepancyCents)} zu Ihrem Nachteil",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = notice.deviationSummaryDe,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(22.dp)
                        ) {
                            Text(
                                text = "1-Klick-Einspruchsschreiben (§ 355 AO + § 361 AO)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = state.objectionLetterPreview,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ELSTER ERiC Transfer-XML", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFF131112),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = state.elsterXmlPreview,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState()),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFDCE4FF)
                                )
                            }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("DATEV-Buchungsstapel CSV (${state.activeProfile.skrChartOfAccounts})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFF131112),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = state.datevCsvPreview,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState()),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD9DC)
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sync-Status: ${syncState.statusMessageDe} · GoBD-Kette: ${if (state.gobdVerification.isValid) "100 % Integrität verifiziert" else "Prüfung"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.auditLogs, key = { it.id }) { log ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("#${log.sequenceNumber} · ${log.summaryDe}", fontWeight = FontWeight.Bold)
                                            Text("SHA-256: ${log.entryHash}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                        }
                                        Text(log.actionType, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vollständiger Ausgabenerfassungs-Dialog inkl. PDF- & Bilderanhang-Auswahl (`ActivityResultContracts.OpenDocument`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseCaptureDialog(
    defaultYear: Int,
    onDismiss: () -> Unit,
    onSubmitExpense: (
        counterpartyName: String,
        descriptionDe: String,
        category: KlartextEurCategory,
        amountEuroInput: String,
        isGrossInput: Boolean,
        vatRatePercent: Int,
        invoiceDateIso: String,
        paymentDateIso: String,
        isRecurringYearEnd: Boolean,
        hospitalityGuests: String,
        hospitalityOccasion: String,
        attachmentFileName: String?,
        attachmentMimeType: String?,
        attachmentUri: String?,
        attachmentSizeBytes: Long?
    ) -> Unit
) {
    val context = LocalContext.current
    var counterparty by remember { mutableStateOf("Hetzner Online GmbH") }
    var description by remember { mutableStateOf("Dedicated Root-Server & Cloud Backup") }
    var selectedCategory by remember { mutableStateOf(KlartextEurCategory.EXPENSE_IT_CLOUD_SOFTWARE) }
    var amountEuro by remember { mutableStateOf("238,00") }
    var isGrossInput by remember { mutableStateOf(true) }
    var vatRatePercent by remember { mutableIntStateOf(19) }
    var invoiceDateIso by remember { mutableStateOf("$defaultYear-11-07") }
    var paymentDateIso by remember { mutableStateOf("$defaultYear-11-07") }
    var isRecurringYearEnd by remember { mutableStateOf(false) }
    var hospitalityGuests by remember { mutableStateOf("Dr. Thomas Lindner (Süddeutsche FinTech AG)") }
    var hospitalityOccasion by remember { mutableStateOf("Architektur-Abnahme & Projektplanung") }

    var attachmentFileName by remember { mutableStateOf<String?>("Hetzner_Cloud_RG2026_11.pdf") }
    var attachmentMimeType by remember { mutableStateOf<String?>("application/pdf") }
    var attachmentUri by remember { mutableStateOf<String?>("file:///klartax_vault/Hetzner_Cloud_RG2026_11.pdf") }
    var attachmentSizeBytes by remember { mutableLongStateOf(164_800L) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "application/pdf"
            var name = "Beleg_${System.currentTimeMillis()}.${if (mime.startsWith("image")) "jpg" else "pdf"}"
            var size = 215_040L
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                        if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                    }
                }
            }
            attachmentFileName = name
            attachmentMimeType = mime
            attachmentUri = uri.toString()
            attachmentSizeBytes = size
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.expense_dialog_title), fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("1. PDF- oder Foto-Beleg anhängen:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            pickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                        }
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF / Foto wählen")
                    }
                    OutlinedButton(
                        onClick = {
                            attachmentFileName = "Bewirtung_Quittung_2026.jpg"
                            attachmentMimeType = "image/jpeg"
                            attachmentUri = "file:///klartax_vault/Bewirtung_Quittung_2026.jpg"
                            attachmentSizeBytes = 412_600L
                            selectedCategory = KlartextEurCategory.EXPENSE_HOSPITALITY_70_30
                            counterparty = "Brenner Operngrill München"
                            description = "Geschäftsessen Projektabschluss"
                            amountEuro = "185,00"
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Demo Foto-Quittung")
                    }
                }

                if (!attachmentFileName.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📎 $attachmentFileName (${attachmentSizeBytes / 1024} KB)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { attachmentFileName = null }) {
                                Text("Entfernen")
                            }
                        }
                    }
                }

                Text("2. Buchungskategorie (Anlage EÜR):", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        KlartextEurCategory.EXPENSE_IT_CLOUD_SOFTWARE,
                        KlartextEurCategory.EXPENSE_HOSPITALITY_70_30,
                        KlartextEurCategory.EXPENSE_VEHICLE_MOBILITY,
                        KlartextEurCategory.EXPENSE_SUBCONTRACTORS,
                        KlartextEurCategory.EXPENSE_AFA_DEPRECIATION,
                        KlartextEurCategory.INCOME_19_PCT
                    ).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayNameDe) }
                        )
                    }
                }

                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = { Text("Lieferant / Aussteller / Kunde") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Buchungstext / Leistungsbeschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = amountEuro,
                        onValueChange = { amountEuro = it },
                        label = { Text(if (isGrossInput) "Brutto-Betrag (€)" else "Netto-Betrag (€)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    FilterChip(
                        selected = isGrossInput,
                        onClick = { isGrossInput = !isGrossInput },
                        label = { Text(if (isGrossInput) "Brutto" else "Netto") }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(19, 7, 0).forEach { rate ->
                        FilterChip(
                            selected = vatRatePercent == rate,
                            onClick = { vatRatePercent = rate },
                            label = { Text("$rate % USt") }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = invoiceDateIso,
                        onValueChange = { invoiceDateIso = it },
                        label = { Text("Rechnungsdatum (JJJJ-MM-TT)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentDateIso,
                        onValueChange = { paymentDateIso = it },
                        label = { Text("Zahlungsdatum") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (selectedCategory == KlartextEurCategory.EXPENSE_HOSPITALITY_70_30) {
                    OutlinedTextField(
                        value = hospitalityGuests,
                        onValueChange = { hospitalityGuests = it },
                        label = { Text("Bewirtete Personen (Pflichtfeld § 4 Abs. 5 Nr. 2 EStG)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hospitalityOccasion,
                        onValueChange = { hospitalityOccasion = it },
                        label = { Text("Konkreter Bewirtungsanlass") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wiederkehrende Ausgabe um Jahreswechsel (10-Tages-Regel)", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = isRecurringYearEnd,
                        onCheckedChange = {
                            isRecurringYearEnd = it
                            if (it) {
                                invoiceDateIso = "$defaultYear-12-29"
                                paymentDateIso = "${defaultYear + 1}-01-04"
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitExpense(
                        counterparty,
                        description,
                        selectedCategory,
                        amountEuro,
                        isGrossInput,
                        vatRatePercent,
                        invoiceDateIso,
                        paymentDateIso,
                        isRecurringYearEnd,
                        hospitalityGuests,
                        hospitalityOccasion,
                        attachmentFileName,
                        attachmentMimeType,
                        attachmentUri,
                        attachmentSizeBytes
                    )
                }
            ) {
                Text("Ausgabe & Beleg speichern", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
