# Klartax – Buchhaltung (SevDesk EÜR) & Steuer (WISO) im Klartext

**Klartax** (`de.klartax.app`) ist eine native Android- & ChromeOS-/Googlebook-Finanzsuite in **100 % Kotlin & Jetpack Compose (Material 3 Expressive & Adaptive UI)**, die den kompletten Funktionsumfang von **SevDesk** (EÜR nach § 4 Abs. 3 EStG, Kundenverwaltung, Artikelkatalog, DIN-5008-Briefpapier mit Firmenlogo, verknüpfte Angebote/Aufträge/E-Rechnungen nach EN 16931) und **WISO Steuer** (komplette Einkommensteuererklärung nach § 32a EStG inkl. ELSTER-XML & Bescheid-Prüfer) vereint.

---

## Highlights & Funktionsumfang

### 1. Modul A – SevDesk EÜR, CRM, Briefpapier & E-Rechnung (EN 16931)
- **Kundenverwaltung (CRM):** Verwaltung von Kundenstammdaten inkl. Kundennummer, Ansprechpartner, Anschrift, USt-IdNr. (`BT-48`), Behörden-Leitweg-ID (`BT-10`), Stundensatz & Zahlungsziel mit 1-Klick-Belegerstellung.
- **Produkte & Dienstleistungen (Artikelkatalog):** Hinterlegen wiederkehrender Leistungen (`SERVICE`) und Produkte/Lizenzen (`PRODUCT`) mit Artikelnummer (`DL-101`, `PR-201`), EN-16931-Einheitencode (`HUR`, `DAY`, `C62`) und Netto-Preis zur direkten Auswahl bei der Angebot- und Rechnungsstellung.
- **Standardtexte & Textbausteine:** Wiederverwendbare Einleitungs- (`HEADER_INTRO`) und Schlusstexte (`FOOTER_CLOSING`) für Angebote, Auftragsbestätigungen und Rechnungen.
- **DIN-5008-Briefpapier mit Firmenlogo:** Individueller Briefpapier-Designer mit Firmenlogo-Upload (`PNG`/`JPG`/`SVG`) oder Monogramm, Falzmarken (`105 mm / 210 mm`), EPC-GiroCode-QR und 3-spaltiger Fußzeile.
- **Durchgängige Belegkette (`AN` → `AB` → `RE`):** 1-Klick-Umwandlung von **Angebot (`AN`)** → **Auftragsbestätigung / Auftrag (`AB`)** → **E-Rechnung (`RE`)** inkl. automatischer Referenzierung (`<ram:BuyerOrderReferencedDocument>`).
- **E-Rechnung Engine (EN 16931):** Generator & Parser für **ZUGFeRD 2.3** (hybrides PDF/A-3 mit eingebetteter `factur-x.xml` via `pdfbox-android`) und **XRechnung 3.0 (CII XML)**.
- **Ausgabenerfassung inkl. PDF- & Bilderanhänge:** Beleg-Upload (`application/pdf`, `image/jpeg`, `image/png`) per Dateiauswahl oder Drag & Drop, On-Device-OCR-Belegextraktion, **10-Tages-Regel (§ 11 EStG)**, **Bewirtungs-Assistent (70 % / 30 % bei 100 % Vorsteuer)**, **PKW-Günstigerprüfung** (1-%-Regelung vs. Fahrtenbuch vs. Kilometerpauschale) und **AfA-/GWG-/Digital-AfA-Rechner**.

### 2. Modul B – WISO Einkommensteuer-Suite (§ 32a EStG)
- **Live-Steuerrechner & Steuerrücklagen-Warner:** Echtzeit-Berechnung von zu versteuerndem Einkommen (`zvE`), Einkommensteuer nach § 32a EStG, Einzel- vs. Zusammenveranlagung (Splitting-Vorteil), Solidaritätszuschlag, Kirchensteuer (8 % BY/BW vs. 9 %) sowie unterjähriger Steuerrücklagen-Empfehlung.
- **Geführtes Klartext-Interview & Formulare:** Anlage S (Freiberufler-EÜR-Brücke), Anlage N (Pendlerpauschale 0,30 €/0,38 € ab 21. km vs. ÖPNV, Homeoffice-Tagespauschale), Anlage Kind, Anlage KAP & Krypto (§ 23 EStG), Anlage V, § 35a EStG (Handwerker & Haushaltsnahe Dienstleistungen) und § 33 EStG (zumutbare Belastung in 3 Stufen).
- **ELSTER-XML & KI-Bescheid-Prüfer:** Erzeugt das ELSTER-Übermittlungsprotokoll, erkennt Abweichungen im Steuerbescheid und generiert ein fertiges Einspruchsschreiben nach **§ 355 AO**.

### 3. Sicherheit, GoBD & Serverless Cloud-Sync
- **Präzise Finanzmathematik:** Ausschließliche Verwendung von `java.math.BigDecimal` (`RoundingMode.HALF_UP`) und `Long` (Euro-Cent) – keinerlei `Float`/`Double` in Finanzberechnungen.
- **Lokale SQLCipher-Datenbank & GoBD-Hash-Kette:** Ende-zu-Ende-Verschlüsselung mit SQLCipher (`klartax_encrypted.db`), `BiometricPrompt`-Schutz und unveränderlicher SHA-256-Verkettung aller festgeschriebenen Buchungen (`isLocked = true`).
- **Zero-Knowledge Sync (`appDataFolder` & WebDAV):** Mit **Google Tink (`AES-256-GCM`)** verschlüsselte Delta-Pakete im persönlichen Google Drive `appDataFolder` sowie lokaler `.klartax`-Vollbackup-Export/Import.
