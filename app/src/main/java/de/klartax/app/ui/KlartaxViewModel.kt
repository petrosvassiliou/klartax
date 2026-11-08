package de.klartax.app.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.klartax.app.R
import de.klartax.app.core.math.Money
import de.klartax.app.core.security.GoBdHashChain
import de.klartax.app.data.local.KlartaxDao
import de.klartax.app.data.local.entities.AssetEntity
import de.klartax.app.data.local.entities.AuditLogEntity
import de.klartax.app.data.local.entities.BankTransactionEntity
import de.klartax.app.data.local.entities.CapitalAndCryptoEntity
import de.klartax.app.data.local.entities.ChildTaxEntity
import de.klartax.app.data.local.entities.CustomerEntity
import de.klartax.app.data.local.entities.DocumentTextTemplateEntity
import de.klartax.app.data.local.entities.EmploymentTaxEntity
import de.klartax.app.data.local.entities.EurBookingEntity
import de.klartax.app.data.local.entities.InvoiceEntity
import de.klartax.app.data.local.entities.InvoiceLineItem
import de.klartax.app.data.local.entities.KlartextEurCategory
import de.klartax.app.data.local.entities.PensionAndInsuranceEntity
import de.klartax.app.data.local.entities.RentalPropertyEntity
import de.klartax.app.data.local.entities.ServiceCatalogItemEntity
import de.klartax.app.data.local.entities.SpecialExpenseAndBurdenEntity
import de.klartax.app.data.local.entities.StationeryConfigEntity
import de.klartax.app.data.local.entities.TaxAssessmentNoticeEntity
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.data.local.entities.TimeTrackingEntryEntity
import de.klartax.app.data.local.entities.VehicleLogEntity
import de.klartax.app.data.seed.DatabaseSeeder
import de.klartax.app.data.sync.DriveSyncRepository
import de.klartax.app.data.sync.SyncUiState
import de.klartax.app.domain.calculator.ComprehensiveTaxSummary
import de.klartax.app.domain.calculator.KlartaxCalculatorEngine
import de.klartax.app.domain.einvoice.EInvoiceGeneratorEngine
import de.klartax.app.domain.einvoice.UstVaReport
import de.klartax.app.domain.elster.ElsterAndBescheidEngine
import de.klartax.app.domain.elster.SteuerSparTipp
import de.klartax.app.domain.scanner.OnDeviceReceiptAnalyzer
import de.klartax.app.domain.scanner.ReceiptScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class KlartaxDestination(@StringRes val labelResId: Int) {
    COCKPIT(R.string.nav_cockpit),
    CUSTOMERS(R.string.nav_customers),
    EUR_AND_RECEIPTS(R.string.nav_eur),
    EINVOICES(R.string.nav_invoices),
    WISO_INCOME_TAX(R.string.nav_wiso_tax),
    ELSTER_BESCHEID_SYNC(R.string.nav_elster_sync)
}

enum class WisoInterviewTopic(@StringRes val titleResId: Int) {
    PERSONAL_FAMILY(R.string.wiso_topic_personal),
    FREELANCE_EUR_BRIDGE(R.string.wiso_topic_eur_bridge),
    ANLAGE_N_COMMUTE_HOMEOFFICE(R.string.wiso_topic_anlage_n),
    ANLAGE_KIND(R.string.wiso_topic_children),
    SEC35A_HANDWERKER_NEBENKOSTEN(R.string.wiso_topic_sec35a),
    ANLAGE_KAP_CRYPTO(R.string.wiso_topic_kap_crypto),
    ANLAGE_V_RENTAL(R.string.wiso_topic_rental_v)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class KlartaxViewModel @Inject constructor(
    private val dao: KlartaxDao,
    private val seeder: DatabaseSeeder,
    private val calculatorEngine: KlartaxCalculatorEngine,
    private val eInvoiceEngine: EInvoiceGeneratorEngine,
    private val receiptAnalyzer: OnDeviceReceiptAnalyzer,
    private val elsterAndBescheidEngine: ElsterAndBescheidEngine,
    private val driveSyncRepository: DriveSyncRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _selectedDestination = MutableStateFlow(KlartaxDestination.COCKPIT)
    val selectedDestination: StateFlow<KlartaxDestination> = _selectedDestination.asStateFlow()

    private val _selectedWisoTopic = MutableStateFlow(WisoInterviewTopic.ANLAGE_N_COMMUTE_HOMEOFFICE)
    val selectedWisoTopic: StateFlow<WisoInterviewTopic> = _selectedWisoTopic.asStateFlow()

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _showNewEntryDialog = MutableStateFlow(false)
    val showNewEntryDialog: StateFlow<Boolean> = _showNewEntryDialog.asStateFlow()

    private val _selectedInvoiceCustomerId = MutableStateFlow<String?>(null)
    val selectedInvoiceCustomerId: StateFlow<String?> = _selectedInvoiceCustomerId.asStateFlow()

    private val _isBiometricUnlocked = MutableStateFlow(false)
    val isBiometricUnlocked: StateFlow<Boolean> = _isBiometricUnlocked.asStateFlow()

    private val _statusBannerDe = MutableStateFlow<String?>(null)
    val statusBannerDe: StateFlow<String?> = _statusBannerDe.asStateFlow()

    val syncState: StateFlow<SyncUiState> = driveSyncRepository.syncState

    init {
        viewModelScope.launch {
            seeder.seedIfEmpty()
        }
    }

    private val allProfilesFlow = dao.observeAllProfiles()
    private val activeProfileFlow = dao.observeActiveProfile().filterNotNull()

    val uiState: StateFlow<KlartaxAppUiState?> = activeProfileFlow
        .flatMapLatest { profile ->
            val eurBundleFlow = combine(
                dao.observeCustomers(profile.id),
                dao.observeEurBookings(profile.id),
                dao.observeInvoices(profile.id),
                dao.observeTimeTrackingEntries(profile.id),
                dao.observeAssets(profile.id)
            ) { customers, bookings, invoices, timeEntries, assets ->
                EurDataBundle(customers, bookings, invoices, timeEntries, assets)
            }

            val sevDeskCatalogAndStationeryFlow = combine(
                dao.observeServiceCatalogItems(profile.id),
                dao.observeDocumentTextTemplates(profile.id),
                dao.observeStationeryConfig(profile.id),
                allProfilesFlow
            ) { catalog, templates, stationery, profiles ->
                SevDeskMasterDataBundle(catalog, templates, stationery, profiles)
            }

            val wisoBundleFlow = combine(
                dao.observeEmploymentTax(profile.id),
                dao.observeSpecialExpensesAndBurdens(profile.id),
                dao.observeCapitalAndCrypto(profile.id),
                dao.observeRentalProperties(profile.id),
                dao.observeChildren(profile.id)
            ) { emp, special, cap, rental, children ->
                WisoDataBundle(emp, special, cap, rental, children)
            }

            val auxBundleFlow = combine(
                dao.observeVehicleLog(profile.id),
                dao.observePensionAndInsurance(profile.id),
                dao.observeTaxAssessmentNotices(profile.id),
                dao.observeAuditLogs(profile.id),
                dao.observeBankTransactions(profile.id)
            ) { vehicle, pension, notices, audits, bankTx ->
                AuxDataBundle(vehicle, pension, notices, audits, bankTx)
            }

            combine(
                eurBundleFlow,
                sevDeskCatalogAndStationeryFlow,
                wisoBundleFlow,
                auxBundleFlow,
                _globalSearchQuery
            ) { eur, master, wiso, aux, query ->
                val summary = calculatorEngine.calculateFullTaxSummary(
                    profile = profile,
                    eurBookings = eur.bookings,
                    assets = eur.assets,
                    vehicleLog = aux.vehicle,
                    employmentList = wiso.employment,
                    specialExpenses = wiso.specialExpenses,
                    capitalAndCrypto = wiso.capitalAndCrypto,
                    rentalProperties = wiso.rentalProperties,
                    children = wiso.children,
                    pensionAndInsurance = aux.pension
                )

                val ustVaReport = eInvoiceEngine.generateUstVaSummary(
                    profile = profile,
                    bookings = eur.bookings,
                    periodLabelDe = "Q4 / Gesamtjahr ${profile.taxYear}"
                )

                val datevCsvPreview = eInvoiceEngine.generateDatevBookingBatchCsv(
                    profile = profile,
                    bookings = eur.bookings
                )

                val tips = elsterAndBescheidEngine.generateProactiveTaxSavingTips(profile, summary)
                val elsterXmlPreview = elsterAndBescheidEngine.generateElsterDeclarationXml(profile, summary)
                val firstNotice = aux.notices.firstOrNull()
                val objectionLetter = firstNotice?.let {
                    elsterAndBescheidEngine.generateFormalObjectionLetterDe(profile, it)
                }.orEmpty()

                val verifiableAuditChain = aux.audits.map { a ->
                    GoBdHashChain.ChainVerifiableEntry(
                        id = a.id,
                        previousHash = a.previousHash,
                        entryHash = a.entryHash,
                        entityType = a.entityType,
                        entityId = a.entityId,
                        actionType = a.actionType,
                        payloadJson = a.payloadJson,
                        createdAtEpochMillis = a.createdAtEpochMillis
                    )
                }
                val chainVerification = GoBdHashChain.verifyChain(verifiableAuditChain)

                val filteredBookings = if (query.isBlank()) {
                    eur.bookings
                } else {
                    eur.bookings.filter {
                        it.descriptionDe.contains(query, ignoreCase = true) ||
                            it.counterpartyName.contains(query, ignoreCase = true) ||
                            it.documentNumber.contains(query, ignoreCase = true) ||
                            (it.attachmentFileName?.contains(query, ignoreCase = true) == true)
                    }
                }

                val filteredCustomers = if (query.isBlank()) {
                    eur.customers
                } else {
                    eur.customers.filter {
                        it.companyOrFullName.contains(query, ignoreCase = true) ||
                            it.customerNumber.contains(query, ignoreCase = true) ||
                            it.contactPersonName.contains(query, ignoreCase = true) ||
                            it.city.contains(query, ignoreCase = true)
                    }
                }

                KlartaxAppUiState(
                    activeProfile = profile,
                    allProfiles = master.allProfiles,
                    taxSummary = summary,
                    customers = filteredCustomers,
                    serviceCatalogItems = master.catalogItems,
                    documentTextTemplates = master.textTemplates,
                    stationeryConfig = master.stationeryConfig,
                    eurBookings = filteredBookings,
                    invoices = eur.invoices,
                    timeEntries = eur.timeEntries,
                    assets = eur.assets,
                    vehicleLog = aux.vehicle,
                    bankTransactions = aux.bankTransactions,
                    employmentRecords = wiso.employment,
                    specialExpenses = wiso.specialExpenses,
                    capitalAndCrypto = wiso.capitalAndCrypto,
                    rentalProperties = wiso.rentalProperties,
                    children = wiso.children,
                    pensionAndInsurance = aux.pension,
                    assessmentNotices = aux.notices,
                    auditLogs = aux.audits,
                    gobdVerification = chainVerification,
                    ustVaReport = ustVaReport,
                    datevCsvPreview = datevCsvPreview,
                    proactiveTips = tips,
                    elsterXmlPreview = elsterXmlPreview,
                    objectionLetterPreview = objectionLetter
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun markBiometricAuthenticated() {
        _isBiometricUnlocked.value = true
    }

    fun selectDestination(destination: KlartaxDestination) {
        _selectedDestination.value = destination
    }

    fun selectWisoTopic(topic: WisoInterviewTopic) {
        _selectedWisoTopic.value = topic
    }

    fun advanceToNextWisoTopic() {
        val topics = WisoInterviewTopic.entries
        val nextIndex = (topics.indexOf(_selectedWisoTopic.value) + 1) % topics.size
        _selectedWisoTopic.value = topics[nextIndex]
    }

    fun updateGlobalSearch(query: String) {
        _globalSearchQuery.value = query
    }

    fun openNewEntryDialog() {
        _showNewEntryDialog.value = true
    }

    fun closeNewEntryDialog() {
        _showNewEntryDialog.value = false
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            dao.switchActiveProfile(profileId)
            _statusBannerDe.value = "Steuerprofil umgeschaltet – Live-Tacho (§ 32a EStG) & Rücklagen-Check neu berechnet."
        }
    }

    // --- 1A. Kundenverwaltung (CRM & Stammdaten) ---

    fun saveCustomer(
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
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val hourlyRateCents = runCatching { Money.toCents(Money.of(defaultHourlyRateEuro)) }
                .getOrDefault(150_00L)
                .coerceAtLeast(0L)
            val id = existingId ?: "CUST-${UUID.randomUUID().toString().take(6)}"
            val cleanNumber = customerNumber.ifBlank { "KD-2026-00${currentState.customers.size + 1}" }

            val entity = CustomerEntity(
                id = id,
                profileId = profile.id,
                customerNumber = cleanNumber,
                companyOrFullName = companyOrFullName.ifBlank { "Neuer Geschäftskunde GmbH" },
                contactPersonName = contactPersonName,
                email = email,
                streetAndNumber = streetAndNumber,
                postalCode = postalCode,
                city = city,
                countryIso = countryIso.ifBlank { "DE" },
                vatId = vatId.ifBlank { null },
                buyerReferenceLeitwegId = leitwegId,
                defaultHourlyRateNetCents = hourlyRateCents,
                paymentTermDays = paymentTermDays.coerceIn(0, 90),
                defaultTaxMode = defaultTaxMode,
                notesDe = notesDe.ifBlank { null },
                updatedAtEpochMillis = now
            )
            dao.upsertCustomers(listOf(entity))
            _statusBannerDe.value = "Kunde ${entity.customerNumber} (${entity.companyOrFullName}) in den Stammdaten gespeichert."
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            dao.deleteCustomerById(customer.id)
            _statusBannerDe.value = "Kunde ${customer.customerNumber} (${customer.companyOrFullName}) gelöscht."
        }
    }

    fun startInvoiceForCustomer(customer: CustomerEntity) {
        _selectedInvoiceCustomerId.value = customer.id
        _selectedDestination.value = KlartaxDestination.EINVOICES
        _statusBannerDe.value = "Kunde ${customer.companyOrFullName} (${customer.customerNumber}) ins Angebots- & Rechnungs-Studio geladen."
    }

    fun selectCustomerInInvoiceStudio(customerId: String?) {
        _selectedInvoiceCustomerId.value = customerId
    }

    // --- 1B. Produkte & Dienstleistungen (Artikel- & Leistungskatalog) ---

    fun saveServiceCatalogItem(
        existingId: String?,
        articleNumber: String,
        itemType: String,
        titleDe: String,
        descriptionDe: String,
        unitCode: String,
        unitPriceNetEuro: String,
        vatRatePercent: Int,
        defaultQuantity: String
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val netCents = runCatching { Money.toCents(Money.of(unitPriceNetEuro)) }
                .getOrDefault(150_00L)
                .coerceAtLeast(0L)
            val id = existingId ?: "CAT-${UUID.randomUUID().toString().take(6)}"
            val cleanNumber = articleNumber.ifBlank {
                if (itemType == "PRODUCT") "PR-20${currentState.serviceCatalogItems.size + 1}"
                else "DL-10${currentState.serviceCatalogItems.size + 1}"
            }
            val cleanQty = runCatching {
                BigDecimal(defaultQuantity.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP).toPlainString()
            }.getOrDefault("1.00")

            val item = ServiceCatalogItemEntity(
                id = id,
                profileId = profile.id,
                articleNumber = cleanNumber,
                itemType = itemType,
                titleDe = titleDe.ifBlank { "Beratungs- & Entwicklungsleistung" },
                descriptionDe = descriptionDe,
                unitCode = unitCode,
                unitPriceNetCents = netCents,
                vatRatePercent = vatRatePercent,
                defaultQuantity = cleanQty,
                updatedAtEpochMillis = now
            )
            dao.upsertServiceCatalogItems(listOf(item))
            _statusBannerDe.value = "${if (itemType == "PRODUCT") "Produkt" else "Dienstleistung"} ${item.articleNumber} (${item.titleDe}) im Katalog gespeichert."
        }
    }

    fun deleteServiceCatalogItem(item: ServiceCatalogItemEntity) {
        viewModelScope.launch {
            dao.deleteServiceCatalogItemById(item.id)
            _statusBannerDe.value = "Katalog-Position ${item.articleNumber} gelöscht."
        }
    }

    // --- 1C. Standardtexte & Textbausteine (Angebote, Aufträge, Rechnungen) ---

    fun saveDocumentTextTemplate(
        existingId: String?,
        templateTitleDe: String,
        documentScope: String,
        placementType: String,
        bodyTextDe: String,
        isDefault: Boolean
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = existingId ?: "TPL-${UUID.randomUUID().toString().take(6)}"
            val tpl = DocumentTextTemplateEntity(
                id = id,
                profileId = profile.id,
                templateTitleDe = templateTitleDe.ifBlank { "Neuer Textbaustein" },
                documentScope = documentScope,
                placementType = placementType,
                bodyTextDe = bodyTextDe,
                isDefault = isDefault,
                updatedAtEpochMillis = now
            )
            dao.upsertDocumentTextTemplates(listOf(tpl))
            _statusBannerDe.value = "Textbaustein '${tpl.templateTitleDe}' gespeichert."
        }
    }

    fun deleteDocumentTextTemplate(template: DocumentTextTemplateEntity) {
        viewModelScope.launch {
            dao.deleteDocumentTextTemplateById(template.id)
            _statusBannerDe.value = "Textbaustein '${template.templateTitleDe}' gelöscht."
        }
    }

    // --- 1D. Briefpapier & Firmenlogo Konfigurator (DIN 5008) ---

    fun saveStationeryConfig(
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
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val existing = currentState.stationeryConfig
            val config = StationeryConfigEntity(
                id = existing?.id ?: "STAT-${profile.id}",
                profileId = profile.id,
                companyDisplayName = companyDisplayName.ifBlank { "${profile.taxpayerFirstName} ${profile.taxpayerLastName}" },
                companySubtitleOrClaim = companySubtitleOrClaim,
                logoMonogramText = logoMonogramText.ifBlank { "LW" }.take(4),
                logoFileName = logoFileName,
                logoImageUri = logoImageUri,
                primaryAccentHex = primaryAccentHex,
                secondaryAccentHex = secondaryAccentHex,
                senderWindowLineDe = senderWindowLineDe,
                footerCol1AddressDe = footerCol1AddressDe,
                footerCol2BankDe = footerCol2BankDe,
                footerCol3TaxDe = footerCol3TaxDe,
                showDin5008FoldMarks = showDin5008FoldMarks,
                showEpcGiroCodeQr = showEpcGiroCodeQr,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            dao.upsertStationeryConfig(config)
            val logoNote = if (!logoFileName.isNullOrBlank()) " inkl. Firmenlogo ($logoFileName)" else ""
            _statusBannerDe.value = "DIN-5008-Briefpapier$logoNote gespeichert – alle Angebote, Aufträge & Rechnungen aktualisiert."
        }
    }

    // --- 2. Ausgabenerfassung inkl. PDF- & Bilderanhänge ---

    fun submitDetailedExpenseWithAttachment(
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
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val inputAmount = runCatching { Money.of(amountEuroInput) }.getOrDefault(Money.of("119.00"))
            val vatRateBd = Money.of(vatRatePercent.toString())

            val (netBd, vatBd, grossBd) = if (vatRatePercent <= 0) {
                Triple(inputAmount, Money.ZERO, inputAmount)
            } else if (isGrossInput) {
                val divisor = BigDecimal.ONE.add(vatRateBd.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP))
                val net = inputAmount.divide(divisor, 2, RoundingMode.HALF_UP)
                val vat = inputAmount.subtract(net)
                Triple(net, vat, inputAmount)
            } else {
                val vat = Money.percentage(inputAmount, vatRateBd)
                val gross = inputAmount.add(vat)
                Triple(inputAmount, vat, gross)
            }

            val netCents = Money.toCents(netBd)
            val vatCents = Money.toCents(vatBd)
            val grossCents = Money.toCents(grossBd)

            val isHospitality = category == KlartextEurCategory.EXPENSE_HOSPITALITY_70_30
            val deductibleCents = if (isHospitality) {
                Money.toCents(Money.percentage(netBd, Money.of("70.00")))
            } else {
                netCents
            }

            val safeDueIso = runCatching { LocalDate.parse(invoiceDateIso); invoiceDateIso }
                .getOrDefault("${profile.taxYear}-11-07")
            val safePayIso = runCatching { LocalDate.parse(paymentDateIso); paymentDateIso }
                .getOrDefault(safeDueIso)

            val tenDayEval = calculatorEngine.evaluateTenDayRuleSec11(
                dueOrInvoiceDate = LocalDate.parse(safeDueIso),
                paymentDate = LocalDate.parse(safePayIso),
                isRecurringPayment = isRecurringYearEnd
            )

            val docNum = "${if (category.isIncome) "EIN" else "AUS"}-${profile.taxYear}-${currentState.eurBookings.size + 101}"
            val booking = EurBookingEntity(
                id = "EUR-EXP-${UUID.randomUUID().toString().take(6)}",
                profileId = profile.id,
                documentNumber = docNum,
                descriptionDe = descriptionDe.ifBlank { category.displayNameDe },
                counterpartyName = counterpartyName.ifBlank { "Lieferant / Dienstleister" },
                counterpartyIban = null,
                category = category,
                invoiceOrDueDateIso = safeDueIso,
                paymentDateIso = safePayIso,
                isRecurringPayment = isRecurringYearEnd,
                appliedTenDayRule = tenDayEval.appliedTenDayRule,
                effectiveTaxYear = tenDayEval.effectiveTaxYear,
                netAmountCents = netCents,
                vatRatePercent = vatRatePercent,
                vatAmountCents = vatCents,
                grossAmountCents = grossCents,
                deductibleNetCents = deductibleCents,
                nonDeductibleNetCents = netCents - deductibleCents,
                hospitalityHost = if (isHospitality) "${profile.taxpayerFirstName} ${profile.taxpayerLastName}" else null,
                hospitalityGuests = if (isHospitality) hospitalityGuests.ifBlank { "Geschäftspartner lt. Beleg" } else null,
                hospitalityOccasion = if (isHospitality) hospitalityOccasion.ifBlank { "Projektbesprechung" } else null,
                taxFreeAllowanceType = null,
                attachmentFileName = attachmentFileName,
                attachmentMimeType = attachmentMimeType,
                attachmentUri = attachmentUri,
                attachmentSizeBytes = attachmentSizeBytes ?: 0L,
                isLocked = false,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = GoBdHashChain.GENESIS_HASH,
                updatedAtEpochMillis = now
            )

            dao.saveEurBookingGoBdSafe(booking)
            _showNewEntryDialog.value = false
            val attachmentInfo = if (!attachmentFileName.isNullOrBlank()) " inkl. Beleg-Anhang ($attachmentFileName)" else ""
            _statusBannerDe.value = "Buchung $docNum (${Money.formatEuro(grossBd)})$attachmentInfo erfasst – Live-Tacho aktualisiert."
        }
    }

    fun attachFileToBooking(
        booking: EurBookingEntity,
        fileName: String,
        mimeType: String,
        uriString: String,
        sizeBytes: Long
    ) {
        if (booking.isLocked) {
            _statusBannerDe.value = "Festgeschriebene GoBD-Buchung kann nicht überschrieben werden."
            return
        }
        viewModelScope.launch {
            val updated = booking.copy(
                attachmentFileName = fileName,
                attachmentMimeType = mimeType,
                attachmentUri = uriString,
                attachmentSizeBytes = sizeBytes,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            dao.saveEurBookingGoBdSafe(updated)
            _statusBannerDe.value = "Beleg-Anhang '$fileName' (${sizeBytes / 1024} KB) mit Buchung ${booking.documentNumber} verknüpft."
        }
    }

    // --- 3. Durchgängiger SevDesk-Belegworkflow: Angebot (AN) -> Auftrag (AB) -> E-Rechnung (RE) ---

    fun issueFullInvoiceWithLineItems(
        customerId: String?,
        customerName: String,
        customerStreet: String,
        customerPostalCode: String,
        customerCity: String,
        customerCountryIso: String,
        customerVatId: String?,
        leitwegId: String,
        documentType: String, // "QUOTE", "ORDER_CONFIRMATION", "INVOICE"
        eInvoiceStandard: String,
        taxMode: String,
        paymentTermDays: Int,
        lineItems: List<InvoiceLineItem>,
        bookDirectlyAsPaidInEur: Boolean,
        linkedQuoteId: String? = null,
        linkedQuoteNumber: String? = null,
        linkedOrderId: String? = null,
        linkedOrderNumber: String? = null,
        subjectTitleDe: String = "",
        headerIntroTextDe: String = "",
        footerClosingTextDe: String = ""
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        if (lineItems.isEmpty()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val vatRateInt = when (taxMode) {
                "REDUCED_7" -> 7
                "KLEINUNTERNEHMER_SEC19", "REVERSE_CHARGE_SEC13B" -> 0
                else -> 19
            }

            val normalizedItems = lineItems.mapIndexed { idx, item ->
                val qty = runCatching { BigDecimal(item.quantityHoursOrUnits.trim().replace(",", ".")) }
                    .getOrDefault(BigDecimal.ONE)
                    .setScale(2, RoundingMode.HALF_UP)
                val unitNetBd = Money.fromCents(item.unitPriceNetCents)
                val lineNetBd = qty.multiply(unitNetBd).setScale(2, RoundingMode.HALF_UP)
                item.copy(
                    positionNumber = idx + 1,
                    quantityHoursOrUnits = qty.toPlainString(),
                    vatRatePercent = vatRateInt,
                    lineNetTotalCents = Money.toCents(lineNetBd)
                )
            }

            val netTotalCents = normalizedItems.sumOf { it.lineNetTotalCents }
            val netTotalBd = Money.fromCents(netTotalCents)
            val vatTotalBd = Money.percentage(netTotalBd, Money.of(vatRateInt.toString()))
            val grossTotalBd = netTotalBd.add(vatTotalBd)

            val prefix = when (documentType) {
                "QUOTE" -> "AN"
                "ORDER_CONFIRMATION" -> "AB"
                "STORNO" -> "ST"
                else -> "RE"
            }
            val typeCount = currentState.invoices.count { it.documentType == documentType } + 1
            val nextNumber = "$prefix-${profile.taxYear}-00$typeCount"
            val todayIso = LocalDate.now().toString()
            val dueIso = LocalDate.now().plusDays(paymentTermDays.toLong().coerceAtLeast(1L)).toString()

            val lastAudit = dao.getLatestAuditLog(profile.id)
            val prevHash = lastAudit?.entryHash ?: GoBdHashChain.GENESIS_HASH
            val invId = "${prefix}-${UUID.randomUUID().toString().take(8)}"
            val payload = "DOC:$nextNumber|NET:$netTotalCents|VAT:${Money.toCents(vatTotalBd)}"
            val isInvoice = documentType == "INVOICE"
            val recordHash = if (!isInvoice) {
                GoBdHashChain.GENESIS_HASH
            } else {
                GoBdHashChain.computeEntryHash(
                    previousHash = prevHash,
                    entityType = "INVOICE",
                    entityId = invId,
                    actionType = "ISSUE_INVOICE",
                    canonicalPayload = payload,
                    timestampEpochMillis = now
                )
            }

            val autoSubject = subjectTitleDe.ifBlank {
                when (documentType) {
                    "QUOTE" -> "Angebot $nextNumber"
                    "ORDER_CONFIRMATION" -> "Auftragsbestätigung $nextNumber${linkedQuoteNumber?.let { " (zu $it)" }.orEmpty()}"
                    else -> "Rechnung $nextNumber${linkedOrderNumber?.let { " (zu $it)" }.orEmpty()}"
                }
            }

            val draftInvoice = InvoiceEntity(
                id = invId,
                profileId = profile.id,
                customerId = customerId,
                invoiceNumber = nextNumber,
                documentType = documentType,
                eInvoiceStandard = eInvoiceStandard,
                taxMode = taxMode,
                customerName = customerName.ifBlank { "Süddeutsche FinTech Solutions AG" },
                customerStreet = customerStreet.ifBlank { "Maximilianstraße 42" },
                customerPostalCode = customerPostalCode.ifBlank { "80539" },
                customerCity = customerCity.ifBlank { "München" },
                customerCountryIso = customerCountryIso.ifBlank { "DE" },
                customerVatId = customerVatId,
                buyerReferenceLeitwegId = leitwegId.ifBlank { "991-12345-67" },
                issueDateIso = todayIso,
                servicePeriodStartIso = LocalDate.now().minusDays(14).toString(),
                servicePeriodEndIso = todayIso,
                dueDateIso = dueIso,
                paidDateIso = if (bookDirectlyAsPaidInEur && isInvoice) todayIso else null,
                netTotalCents = netTotalCents,
                vatTotalCents = Money.toCents(vatTotalBd),
                grossTotalCents = Money.toCents(grossTotalBd),
                status = when (documentType) {
                    "QUOTE" -> "QUOTE_OPEN"
                    "ORDER_CONFIRMATION" -> "ORDER_CONFIRMED"
                    else -> if (bookDirectlyAsPaidInEur) "PAID" else "SENT_LOCKED"
                },
                dunningLevel = 0,
                sellerIban = "DE89700800000512345600",
                sellerBic = "DRESDEFF700",
                lineItemsJson = json.encodeToString(normalizedItems),
                embeddedXmlPayload = "",
                isLocked = isInvoice,
                stornoOfInvoiceId = null,
                linkedQuoteId = linkedQuoteId,
                linkedQuoteNumber = linkedQuoteNumber,
                linkedOrderId = linkedOrderId,
                linkedOrderNumber = linkedOrderNumber,
                subjectTitleDe = autoSubject,
                headerIntroTextDe = headerIntroTextDe,
                footerClosingTextDe = footerClosingTextDe,
                gobdRecordHash = recordHash,
                updatedAtEpochMillis = now
            )

            val xml = eInvoiceEngine.generateEn16931CiiXml(profile, draftInvoice, normalizedItems)
            val pdfBytes = runCatching {
                eInvoiceEngine.generateZugferdPdfWithEmbeddedXmlBytes(profile, draftInvoice, normalizedItems)
            }.getOrNull()

            dao.saveInvoiceGoBdSafe(draftInvoice.copy(embeddedXmlPayload = xml))

            if (isInvoice) {
                dao.insertAuditLog(
                    AuditLogEntity(
                        id = "AUD-$invId",
                        profileId = profile.id,
                        sequenceNumber = (lastAudit?.sequenceNumber ?: 0L) + 1L,
                        previousHash = prevHash,
                        entryHash = recordHash,
                        entityType = "INVOICE",
                        entityId = invId,
                        actionType = "ISSUE_INVOICE",
                        summaryDe = "Festschreibung E-Rechnung $nextNumber (${Money.formatEuro(grossTotalBd)})",
                        payloadJson = payload,
                        createdAtEpochMillis = now
                    )
                )
            }

            if (bookDirectlyAsPaidInEur && isInvoice) {
                val incomeCategory = when (taxMode) {
                    "REDUCED_7" -> KlartextEurCategory.INCOME_7_PCT
                    "KLEINUNTERNEHMER_SEC19" -> KlartextEurCategory.INCOME_KLEINUNTERNEHMER_SEC19
                    "REVERSE_CHARGE_SEC13B" -> KlartextEurCategory.INCOME_REVERSE_CHARGE_EU
                    else -> KlartextEurCategory.INCOME_19_PCT
                }
                val eurIncome = EurBookingEntity(
                    id = "EUR-INV-${UUID.randomUUID().toString().take(6)}",
                    profileId = profile.id,
                    documentNumber = nextNumber,
                    descriptionDe = "Zahlungseingang $nextNumber – ${draftInvoice.customerName}",
                    counterpartyName = draftInvoice.customerName,
                    counterpartyIban = null,
                    category = incomeCategory,
                    invoiceOrDueDateIso = todayIso,
                    paymentDateIso = todayIso,
                    isRecurringPayment = false,
                    appliedTenDayRule = false,
                    effectiveTaxYear = profile.taxYear,
                    netAmountCents = netTotalCents,
                    vatRatePercent = vatRateInt,
                    vatAmountCents = Money.toCents(vatTotalBd),
                    grossAmountCents = Money.toCents(grossTotalBd),
                    deductibleNetCents = netTotalCents,
                    nonDeductibleNetCents = 0L,
                    hospitalityHost = null,
                    hospitalityGuests = null,
                    hospitalityOccasion = null,
                    taxFreeAllowanceType = null,
                    attachmentFileName = "${nextNumber}_ZUGFeRD_2_3.pdf",
                    attachmentMimeType = "application/pdf",
                    attachmentUri = "file:///klartax_vault/${nextNumber}_ZUGFeRD_2_3.pdf",
                    attachmentSizeBytes = pdfBytes?.size?.toLong() ?: 48_640L,
                    isLocked = true,
                    isStornoRecord = false,
                    stornoOfBookingId = null,
                    gobdRecordHash = recordHash,
                    updatedAtEpochMillis = now
                )
                dao.saveEurBookingGoBdSafe(eurIncome)
            }

            val pdfInfo = if (pdfBytes != null) " (${pdfBytes.size / 1024} KB PDF/A-3 + factur-x.xml)" else ""
            _statusBannerDe.value = when (documentType) {
                "QUOTE" -> "Angebot $nextNumber (${Money.formatEuro(grossTotalBd)}) erstellt – bereit zur Umwandlung in Auftrag (AB) oder Rechnung."
                "ORDER_CONFIRMATION" -> "Auftragsbestätigung $nextNumber (${Money.formatEuro(grossTotalBd)}) verknüpft und gespeichert."
                else -> "E-Rechnung $nextNumber ($eInvoiceStandard · ${Money.formatEuro(grossTotalBd)})$pdfInfo GoBD-konform festgeschrieben."
            }
        }
    }

    /**
     * 1-Klick-Umwandlung im SevDesk-Workflow:
     * Wandelt ein Angebot (`AN-2026-xxx`) in eine verknüpfte Auftragsbestätigung / Auftrag (`AB-2026-xxx`) um.
     */
    fun convertQuoteToOrderConfirmation(quote: InvoiceEntity) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val orderCount = currentState.invoices.count { it.documentType == "ORDER_CONFIRMATION" } + 1
            val orderNumber = "AB-${profile.taxYear}-00$orderCount"
            val orderId = "ORDER-${UUID.randomUUID().toString().take(6)}"

            val orderIntro = currentState.documentTextTemplates
                .firstOrNull { it.documentScope == "ORDER_CONFIRMATION" && it.placementType == "HEADER_INTRO" }
                ?.bodyTextDe
                ?: "Sehr geehrte Damen und Herren, hiermit bestätigen wir verbindlich Ihren Auftrag gemäß Angebot ${quote.invoiceNumber}:"
            val orderFooter = currentState.documentTextTemplates
                .firstOrNull { it.documentScope == "ORDER_CONFIRMATION" && it.placementType == "FOOTER_CLOSING" }
                ?.bodyTextDe
                ?: quote.footerClosingTextDe

            val orderDoc = quote.copy(
                id = orderId,
                invoiceNumber = orderNumber,
                documentType = "ORDER_CONFIRMATION",
                issueDateIso = LocalDate.now().toString(),
                status = "ORDER_CONFIRMED",
                isLocked = false,
                linkedQuoteId = quote.id,
                linkedQuoteNumber = quote.invoiceNumber,
                subjectTitleDe = "Auftragsbestätigung $orderNumber (zu Angebot ${quote.invoiceNumber})",
                headerIntroTextDe = orderIntro,
                footerClosingTextDe = orderFooter,
                updatedAtEpochMillis = now
            )

            val updatedQuote = quote.copy(
                status = "QUOTE_ACCEPTED",
                linkedOrderId = orderId,
                linkedOrderNumber = orderNumber,
                updatedAtEpochMillis = now
            )

            dao.internalUpsertInvoice(updatedQuote)
            dao.internalUpsertInvoice(orderDoc)
            _statusBannerDe.value = "Angebot ${quote.invoiceNumber} als angenommen markiert und verknüpften Auftrag $orderNumber erzeugt."
        }
    }

    /**
     * 1-Klick-Umwandlung im SevDesk-Workflow:
     * Wandelt einen bestätigten Auftrag (`AB-2026-xxx`) oder ein Angebot (`AN-2026-xxx`) direkt in eine
     * GoBD-festgeschriebene EN 16931 E-Rechnung (`RE-2026-xxx`) inkl. Verknüpfung um.
     */
    fun convertQuoteOrOrderToEInvoice(sourceDoc: InvoiceEntity) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val invCount = currentState.invoices.count { it.documentType == "INVOICE" } + 1
            val invNumber = "RE-${profile.taxYear}-00$invCount"
            val invId = "INV-${UUID.randomUUID().toString().take(8)}"

            val lastAudit = dao.getLatestAuditLog(profile.id)
            val prevHash = lastAudit?.entryHash ?: GoBdHashChain.GENESIS_HASH
            val payload = "DOC:$invNumber|NET:${sourceDoc.netTotalCents}|VAT:${sourceDoc.vatTotalCents}|FROM:${sourceDoc.invoiceNumber}"
            val recordHash = GoBdHashChain.computeEntryHash(
                previousHash = prevHash,
                entityType = "INVOICE",
                entityId = invId,
                actionType = "ISSUE_INVOICE",
                canonicalPayload = payload,
                timestampEpochMillis = now
            )

            val items = runCatching {
                json.decodeFromString<List<InvoiceLineItem>>(sourceDoc.lineItemsJson)
            }.getOrDefault(emptyList())

            val invIntro = currentState.documentTextTemplates
                .firstOrNull { it.documentScope == "INVOICE" && it.placementType == "HEADER_INTRO" }
                ?.bodyTextDe
                ?: sourceDoc.headerIntroTextDe
            val invFooter = currentState.documentTextTemplates
                .firstOrNull { it.documentScope == "INVOICE" && it.placementType == "FOOTER_CLOSING" }
                ?.bodyTextDe
                ?: sourceDoc.footerClosingTextDe

            val linkedQuoteNum = if (sourceDoc.documentType == "QUOTE") sourceDoc.invoiceNumber else sourceDoc.linkedQuoteNumber
            val linkedQuoteIdVal = if (sourceDoc.documentType == "QUOTE") sourceDoc.id else sourceDoc.linkedQuoteId
            val linkedOrderNum = if (sourceDoc.documentType == "ORDER_CONFIRMATION") sourceDoc.invoiceNumber else sourceDoc.linkedOrderNumber
            val linkedOrderIdVal = if (sourceDoc.documentType == "ORDER_CONFIRMATION") sourceDoc.id else sourceDoc.linkedOrderId

            val draftInv = sourceDoc.copy(
                id = invId,
                invoiceNumber = invNumber,
                documentType = "INVOICE",
                issueDateIso = LocalDate.now().toString(),
                dueDateIso = LocalDate.now().plusDays(14).toString(),
                status = "SENT_LOCKED",
                isLocked = true,
                linkedQuoteId = linkedQuoteIdVal,
                linkedQuoteNumber = linkedQuoteNum,
                linkedOrderId = linkedOrderIdVal,
                linkedOrderNumber = linkedOrderNum,
                subjectTitleDe = "Rechnung $invNumber (zu ${sourceDoc.invoiceNumber})",
                headerIntroTextDe = invIntro,
                footerClosingTextDe = invFooter,
                gobdRecordHash = recordHash,
                updatedAtEpochMillis = now
            )

            val xml = eInvoiceEngine.generateEn16931CiiXml(profile, draftInv, items)
            dao.saveInvoiceGoBdSafe(draftInv.copy(embeddedXmlPayload = xml))

            // Quell-Auftrag / Angebot als abgerechnet markieren
            val updatedSource = sourceDoc.copy(
                status = if (sourceDoc.documentType == "QUOTE") "QUOTE_ACCEPTED" else "ORDER_BILLED",
                updatedAtEpochMillis = now
            )
            dao.internalUpsertInvoice(updatedSource)

            dao.insertAuditLog(
                AuditLogEntity(
                    id = "AUD-$invId",
                    profileId = profile.id,
                    sequenceNumber = (lastAudit?.sequenceNumber ?: 0L) + 1L,
                    previousHash = prevHash,
                    entryHash = recordHash,
                    entityType = "INVOICE",
                    entityId = invId,
                    actionType = "ISSUE_INVOICE",
                    summaryDe = "Umwandlung ${sourceDoc.invoiceNumber} -> E-Rechnung $invNumber (${Money.formatEuroFromCents(draftInv.grossTotalCents)})",
                    payloadJson = payload,
                    createdAtEpochMillis = now
                )
            )

            _statusBannerDe.value = "${sourceDoc.invoiceNumber} erfolgreich in GoBD-festgeschriebene E-Rechnung $invNumber umgewandelt."
        }
    }

    fun markInvoiceAsPaidAndBookToEur(invoice: InvoiceEntity) {
        if (invoice.status == "PAID") return
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val todayIso = LocalDate.now().toString()
            val vatRateInt = when (invoice.taxMode) {
                "REDUCED_7" -> 7
                "KLEINUNTERNEHMER_SEC19", "REVERSE_CHARGE_SEC13B" -> 0
                else -> 19
            }
            val incomeCategory = when (invoice.taxMode) {
                "REDUCED_7" -> KlartextEurCategory.INCOME_7_PCT
                "KLEINUNTERNEHMER_SEC19" -> KlartextEurCategory.INCOME_KLEINUNTERNEHMER_SEC19
                "REVERSE_CHARGE_SEC13B" -> KlartextEurCategory.INCOME_REVERSE_CHARGE_EU
                else -> KlartextEurCategory.INCOME_19_PCT
            }

            val eurIncome = EurBookingEntity(
                id = "EUR-PAID-${UUID.randomUUID().toString().take(6)}",
                profileId = profile.id,
                documentNumber = invoice.invoiceNumber,
                descriptionDe = "Zahlungseingang ${invoice.invoiceNumber} (${invoice.customerName})",
                counterpartyName = invoice.customerName,
                counterpartyIban = null,
                category = incomeCategory,
                invoiceOrDueDateIso = invoice.issueDateIso,
                paymentDateIso = todayIso,
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = profile.taxYear,
                netAmountCents = invoice.netTotalCents,
                vatRatePercent = vatRateInt,
                vatAmountCents = invoice.vatTotalCents,
                grossAmountCents = invoice.grossTotalCents,
                deductibleNetCents = invoice.netTotalCents,
                nonDeductibleNetCents = 0L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = null,
                attachmentFileName = "${invoice.invoiceNumber}_ZUGFeRD_2_3.pdf",
                attachmentMimeType = "application/pdf",
                attachmentUri = "file:///klartax_vault/${invoice.invoiceNumber}_ZUGFeRD_2_3.pdf",
                attachmentSizeBytes = 52_400L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = invoice.gobdRecordHash,
                updatedAtEpochMillis = now
            )
            dao.saveEurBookingGoBdSafe(eurIncome)
            dao.internalUpsertInvoice(invoice.copy(status = "PAID", paidDateIso = todayIso, updatedAtEpochMillis = now))
            _statusBannerDe.value = "Zahlungseingang für ${invoice.invoiceNumber} (${Money.formatEuroFromCents(invoice.grossTotalCents)}) nach § 11 EStG in EÜR verbucht."
        }
    }

    fun createStornoForInvoice(original: InvoiceEntity) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val lastAudit = dao.getLatestAuditLog(profile.id)
            val prevHash = lastAudit?.entryHash ?: GoBdHashChain.GENESIS_HASH
            val stornoId = "INV-ST-${UUID.randomUUID().toString().take(6)}"
            val stornoNumber = "${original.invoiceNumber}-STORNO"
            val payload = "STORNO_OF:${original.invoiceNumber}|NET:${-original.netTotalCents}"
            val stornoHash = GoBdHashChain.computeEntryHash(
                previousHash = prevHash,
                entityType = "INVOICE",
                entityId = stornoId,
                actionType = "STORNO_INVOICE",
                canonicalPayload = payload,
                timestampEpochMillis = now
            )

            val stornoInvoice = original.copy(
                id = stornoId,
                invoiceNumber = stornoNumber,
                documentType = "STORNO",
                netTotalCents = -original.netTotalCents,
                vatTotalCents = -original.vatTotalCents,
                grossTotalCents = -original.grossTotalCents,
                status = "STORNO_CANCELLED",
                isLocked = true,
                stornoOfInvoiceId = original.id,
                subjectTitleDe = "Stornorechnung $stornoNumber zu ${original.invoiceNumber}",
                gobdRecordHash = stornoHash,
                updatedAtEpochMillis = now
            )
            dao.saveInvoiceGoBdSafe(stornoInvoice)
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "AUD-$stornoId",
                    profileId = profile.id,
                    sequenceNumber = (lastAudit?.sequenceNumber ?: 0L) + 1L,
                    previousHash = prevHash,
                    entryHash = stornoHash,
                    entityType = "INVOICE",
                    entityId = stornoId,
                    actionType = "STORNO_INVOICE",
                    summaryDe = "GoBD-Stornorechnung $stornoNumber zu ${original.invoiceNumber}",
                    payloadJson = payload,
                    createdAtEpochMillis = now
                )
            )
            _statusBannerDe.value = "GoBD-konforme Stornorechnung $stornoNumber (TypeCode 381) mit SHA-256-Verkettung erzeugt."
        }
    }

    // --- Interaktive WISO-Interview Eingabemethoden (Echtzeit-Neuberechnung) ---

    fun toggleSplittingAssessment(isMarriedJoint: Boolean) {
        val current = uiState.value?.activeProfile ?: return
        viewModelScope.launch {
            dao.upsertProfiles(
                listOf(
                    current.copy(
                        isMarriedJointAssessment = isMarriedJoint,
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateHomeOfficeDays(deltaDays: Int) {
        val emp = uiState.value?.employmentRecords?.firstOrNull() ?: return
        val newDays = (emp.homeOfficeDays + deltaDays).coerceIn(0, 210)
        viewModelScope.launch {
            dao.upsertEmploymentTax(
                listOf(
                    emp.copy(
                        homeOfficeDays = newDays,
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateCommuteDistanceKm(newDistanceKm: Int) {
        val emp = uiState.value?.employmentRecords?.firstOrNull() ?: return
        viewModelScope.launch {
            dao.upsertEmploymentTax(
                listOf(
                    emp.copy(
                        commuteDistanceOneWayKm = newDistanceKm.coerceIn(1, 120),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateCommuteWorkdays(newDays: Int) {
        val emp = uiState.value?.employmentRecords?.firstOrNull() ?: return
        viewModelScope.launch {
            dao.upsertEmploymentTax(
                listOf(
                    emp.copy(
                        commuteWorkdaysPerYear = newDays.coerceIn(1, 250),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateWorkEquipmentEuro(euroInput: String) {
        val emp = uiState.value?.employmentRecords?.firstOrNull() ?: return
        val cents = runCatching { Money.toCents(Money.of(euroInput)) }.getOrNull() ?: return
        viewModelScope.launch {
            dao.upsertEmploymentTax(
                listOf(
                    emp.copy(
                        workEquipmentCostCents = cents.coerceAtLeast(0L),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateChildcareExpensesCents(newCents: Long) {
        val child = uiState.value?.children?.firstOrNull() ?: return
        viewModelScope.launch {
            dao.upsertChildren(
                listOf(
                    child.copy(
                        childcareExpenseTotalCents = newCents.coerceAtLeast(0L),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun updateSec35aEligibleLaborCents(record: SpecialExpenseAndBurdenEntity, newLaborCents: Long) {
        viewModelScope.launch {
            dao.upsertSpecialExpensesAndBurdens(
                listOf(
                    record.copy(
                        eligibleLaborAndMachineCents = newLaborCents.coerceAtLeast(0L),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun triggerGoBdStornoAndCorrection(booking: EurBookingEntity, newNetCents: Long, reasonDe: String) {
        viewModelScope.launch {
            val net = Money.fromCents(newNetCents)
            val vat = Money.percentage(net, Money.of(booking.vatRatePercent.toString()))
            val gross = net.add(vat)
            val corrected = booking.copy(
                id = "EUR-CORR-${UUID.randomUUID().toString().take(6)}",
                documentNumber = "${booking.documentNumber}-KORR",
                netAmountCents = newNetCents,
                vatAmountCents = Money.toCents(vat),
                grossAmountCents = Money.toCents(gross),
                deductibleNetCents = if (booking.category == KlartextEurCategory.EXPENSE_HOSPITALITY_70_30) {
                    Money.toCents(Money.percentage(net, Money.of("70.00")))
                } else {
                    newNetCents
                }
            )
            dao.createGoBdStornoAndCorrectionBooking(
                originalBookingId = booking.id,
                correctedBooking = corrected,
                reasonDe = reasonDe
            )
            _statusBannerDe.value = "GoBD-Storno & Korrekturbuchung (${corrected.documentNumber}) mit SHA-256-Hash-Verkettung angelegt."
        }
    }

    fun processDroppedOrScannedReceiptContent(
        rawTextOrXml: String,
        forceRecurringTenDayCheck: Boolean = false,
        attachedFileName: String? = null,
        attachedMimeType: String? = null,
        attachedUri: String? = null,
        attachedSizeBytes: Long? = null
    ) {
        val currentState = uiState.value ?: return
        val profile = currentState.activeProfile
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (rawTextOrXml.trimStart().startsWith("<")) {
                val parsed = eInvoiceEngine.parseIncomingEInvoiceXml(rawTextOrXml)
                val netCents = Money.toCents(parsed.netAmount)
                val vatCents = Money.toCents(parsed.vatAmount)
                val grossCents = Money.toCents(parsed.grossAmount)
                val newBooking = EurBookingEntity(
                    id = "EUR-XML-${UUID.randomUUID().toString().take(6)}",
                    profileId = profile.id,
                    documentNumber = parsed.invoiceNumber,
                    descriptionDe = "E-Rechnung Import: ${parsed.sellerName}",
                    counterpartyName = parsed.sellerName,
                    counterpartyIban = parsed.sellerIban,
                    category = KlartextEurCategory.EXPENSE_IT_CLOUD_SOFTWARE,
                    invoiceOrDueDateIso = parsed.issueDateIso,
                    paymentDateIso = parsed.issueDateIso,
                    isRecurringPayment = false,
                    appliedTenDayRule = false,
                    effectiveTaxYear = profile.taxYear,
                    netAmountCents = netCents,
                    vatRatePercent = 19,
                    vatAmountCents = vatCents,
                    grossAmountCents = grossCents,
                    deductibleNetCents = netCents,
                    nonDeductibleNetCents = 0L,
                    hospitalityHost = null,
                    hospitalityGuests = null,
                    hospitalityOccasion = null,
                    taxFreeAllowanceType = null,
                    attachmentFileName = attachedFileName ?: "${parsed.invoiceNumber}.xml",
                    attachmentMimeType = attachedMimeType ?: "application/xml",
                    attachmentUri = attachedUri ?: "file:///klartax_vault/${parsed.invoiceNumber}.xml",
                    attachmentSizeBytes = attachedSizeBytes ?: rawTextOrXml.length.toLong(),
                    isLocked = false,
                    isStornoRecord = false,
                    stornoOfBookingId = null,
                    gobdRecordHash = GoBdHashChain.GENESIS_HASH,
                    updatedAtEpochMillis = now
                )
                dao.saveEurBookingGoBdSafe(newBooking)
                _statusBannerDe.value = "EN 16931 E-Rechnung (${parsed.invoiceNumber} · ${Money.formatEuro(parsed.grossAmount)}) automatisch geparst und in EÜR übernommen."
            } else {
                val analysis = receiptAnalyzer.analyzeReceiptText(rawTextOrXml)
                if (analysis.classificationScope == ReceiptScope.FREELANCE_EUR_EXPENSE && profile.hasFreelanceActivity) {
                    val netCents = Money.toCents(analysis.netAmount)
                    val vatCents = Money.toCents(analysis.vatAmount)
                    val grossCents = Money.toCents(analysis.grossAmount)
                    val isHospitality = analysis.suggestedEurCategory == KlartextEurCategory.EXPENSE_HOSPITALITY_70_30
                    val deductibleCents = if (isHospitality) {
                        Money.toCents(Money.percentage(analysis.netAmount, Money.of("70.00")))
                    } else {
                        netCents
                    }
                    val dueIso = if (forceRecurringTenDayCheck) "${profile.taxYear}-12-28" else analysis.invoiceDateIso
                    val payIso = if (forceRecurringTenDayCheck) "${profile.taxYear + 1}-01-05" else analysis.invoiceDateIso
                    val tenDayEval = calculatorEngine.evaluateTenDayRuleSec11(
                        dueOrInvoiceDate = LocalDate.parse(dueIso),
                        paymentDate = LocalDate.parse(payIso),
                        isRecurringPayment = forceRecurringTenDayCheck
                    )
                    val booking = EurBookingEntity(
                        id = "EUR-SCAN-${UUID.randomUUID().toString().take(6)}",
                        profileId = profile.id,
                        documentNumber = "BELEG-${currentState.eurBookings.size + 101}",
                        descriptionDe = analysis.issuerName,
                        counterpartyName = analysis.issuerName,
                        counterpartyIban = analysis.iban,
                        category = analysis.suggestedEurCategory ?: KlartextEurCategory.EXPENSE_IT_CLOUD_SOFTWARE,
                        invoiceOrDueDateIso = dueIso,
                        paymentDateIso = payIso,
                        isRecurringPayment = forceRecurringTenDayCheck,
                        appliedTenDayRule = tenDayEval.appliedTenDayRule,
                        effectiveTaxYear = tenDayEval.effectiveTaxYear,
                        netAmountCents = netCents,
                        vatRatePercent = 19,
                        vatAmountCents = vatCents,
                        grossAmountCents = grossCents,
                        deductibleNetCents = deductibleCents,
                        nonDeductibleNetCents = netCents - deductibleCents,
                        hospitalityHost = if (isHospitality) "${profile.taxpayerFirstName} ${profile.taxpayerLastName}" else null,
                        hospitalityGuests = if (isHospitality) "Geschäftspartner lt. Beleg" else null,
                        hospitalityOccasion = if (isHospitality) "Projektbesprechung" else null,
                        taxFreeAllowanceType = null,
                        attachmentFileName = attachedFileName ?: "Scan_${analysis.invoiceDateIso}.pdf",
                        attachmentMimeType = attachedMimeType ?: "application/pdf",
                        attachmentUri = attachedUri ?: "file:///klartax_vault/Scan_${analysis.invoiceDateIso}.pdf",
                        attachmentSizeBytes = attachedSizeBytes ?: 142_800L,
                        isLocked = false,
                        isStornoRecord = false,
                        stornoOfBookingId = null,
                        gobdRecordHash = GoBdHashChain.GENESIS_HASH,
                        updatedAtEpochMillis = now
                    )
                    dao.saveEurBookingGoBdSafe(booking)
                } else {
                    val newSpecial = SpecialExpenseAndBurdenEntity(
                        id = "SPEC-SCAN-${UUID.randomUUID().toString().take(6)}",
                        profileId = profile.id,
                        recordCategory = analysis.suggestedPrivateCategory ?: "CRAFTSMAN_HANDWERKER_SEC35A",
                        titleDe = "Beleg: ${analysis.issuerName}",
                        providerOrRecipientName = analysis.issuerName,
                        invoiceDateIso = analysis.invoiceDateIso,
                        paymentMethodBankTransfer = true,
                        totalInvoiceGrossCents = Money.toCents(analysis.grossAmount),
                        eligibleLaborAndMachineCents = Money.toCents(analysis.eligibleLaborSec35a),
                        disabilityDegreeGdB = null,
                        updatedAtEpochMillis = now
                    )
                    dao.upsertSpecialExpensesAndBurdens(listOf(newSpecial))
                }
                _statusBannerDe.value = analysis.explanationDe
            }
        }
    }

    fun triggerZeroKnowledgeDriveSync() {
        val activeProfileId = uiState.value?.activeProfile?.id ?: return
        viewModelScope.launch {
            driveSyncRepository.performGoogleDriveAppDataDeltaSync(
                driveService = null,
                activeProfileId = activeProfileId
            ).onSuccess { report ->
                _statusBannerDe.value = "AES-256-GCM Delta-Paket erzeugt: ${report.deltaFileName} (${report.encryptedSizeBytes} Bytes)."
            }
        }
    }

    fun dismissBanner() {
        _statusBannerDe.value = null
    }
}

private data class EurDataBundle(
    val customers: List<CustomerEntity>,
    val bookings: List<EurBookingEntity>,
    val invoices: List<InvoiceEntity>,
    val timeEntries: List<TimeTrackingEntryEntity>,
    val assets: List<AssetEntity>
)

private data class SevDeskMasterDataBundle(
    val catalogItems: List<ServiceCatalogItemEntity>,
    val textTemplates: List<DocumentTextTemplateEntity>,
    val stationeryConfig: StationeryConfigEntity?,
    val allProfiles: List<TaxProfileEntity>
)

private data class WisoDataBundle(
    val employment: List<EmploymentTaxEntity>,
    val specialExpenses: List<SpecialExpenseAndBurdenEntity>,
    val capitalAndCrypto: List<CapitalAndCryptoEntity>,
    val rentalProperties: List<RentalPropertyEntity>,
    val children: List<ChildTaxEntity>
)

private data class AuxDataBundle(
    val vehicle: VehicleLogEntity?,
    val pension: PensionAndInsuranceEntity?,
    val notices: List<TaxAssessmentNoticeEntity>,
    val audits: List<AuditLogEntity>,
    val bankTransactions: List<BankTransactionEntity>
)

data class KlartaxAppUiState(
    val activeProfile: TaxProfileEntity,
    val allProfiles: List<TaxProfileEntity>,
    val taxSummary: ComprehensiveTaxSummary,
    val customers: List<CustomerEntity>,
    val serviceCatalogItems: List<ServiceCatalogItemEntity>,
    val documentTextTemplates: List<DocumentTextTemplateEntity>,
    val stationeryConfig: StationeryConfigEntity?,
    val eurBookings: List<EurBookingEntity>,
    val invoices: List<InvoiceEntity>,
    val timeEntries: List<TimeTrackingEntryEntity>,
    val assets: List<AssetEntity>,
    val vehicleLog: VehicleLogEntity?,
    val bankTransactions: List<BankTransactionEntity>,
    val employmentRecords: List<EmploymentTaxEntity>,
    val specialExpenses: List<SpecialExpenseAndBurdenEntity>,
    val capitalAndCrypto: List<CapitalAndCryptoEntity>,
    val rentalProperties: List<RentalPropertyEntity>,
    val children: List<ChildTaxEntity>,
    val pensionAndInsurance: PensionAndInsuranceEntity?,
    val assessmentNotices: List<TaxAssessmentNoticeEntity>,
    val auditLogs: List<AuditLogEntity>,
    val gobdVerification: GoBdHashChain.ChainVerificationResult,
    val ustVaReport: UstVaReport,
    val datevCsvPreview: String,
    val proactiveTips: List<SteuerSparTipp>,
    val elsterXmlPreview: String,
    val objectionLetterPreview: String
)
