# HammamiWellnessCentre
Un'applicazione mobile nativa per Android, sviluppata in **Kotlin**, il cui design è stato interamente guidato da un processo di **Human-Centered Design (HCD)**. Utilizzando **Material Design 3**, l'app consente agli utenti di esplorare servizi, prenotare trattamenti, gestire il proprio profilo e interagire con il centro benessere attraverso un'interfaccia intuitiva e accessibile.

---

### 📄 **Documentazione Completa**
*   [**Leggi la Relazione di Progetto Completa](relazione hammami.pdf)**
*   [**Visualizza le Slide di Presentazione](slide presentazione hammami.pdf)**

---

*(Inserisci qui uno screenshot chiave dell'app, come la homepage o la schermata di un servizio)*


## 🎯 Obiettivo del Progetto

Creare una soluzione mobile completa che migliori l'engagement dei clienti e ottimizzi i processi di prenotazione per il centro benessere "Hammami". Il progetto è stato sviluppato seguendo un rigoroso processo di **User-Centered Design (UCD)**, dalla ricerca iniziale fino all'implementazione tecnica.

## ✨ Feature Principali

*   **Flusso di Prenotazione Completo:** Dalla selezione del servizio alla scelta dell'operatore (sviluppo futuro), fino al pagamento e alla conferma.
*   **Autenticazione e Profilo Utente:** Gestione sicura di registrazione, login e area personale per la modifica dei dati e la visualizzazione dello storico.
*   **Sistemi di Loyalty:** Implementazione di "punti karma", coupon e gift card digitali per incentivare la fedeltà dei clienti.
*   **Interfaccia Amministratore:** Vista dedicata per lo staff per la gestione dell'agenda e degli appuntamenti.

## 🏛️ Architettura e Stack Tecnologico

L'applicazione è costruita su un'architettura moderna, robusta e scalabile, seguendo le best practice dello sviluppo Android.

*   **Linguaggio:** **Kotlin** (100% nativo)
*   **Architettura:** **MVVM (Model-View-ViewModel)** per una chiara separazione delle responsabilità (UI, logica di business, dati).
*   **UI Toolkit:** **Android Jetpack** con `Fragments` e `Navigation Component` per una navigazione a Single-Activity robusta e gestita.
*   **Backend (BaaS):** **Google Firebase**
    *   **Firestore:** Database NoSQL per la gestione di utenti, servizi, prenotazioni e recensioni.
    *   **Firebase Authentication:** Per la gestione sicura di registrazione, login e sessioni.
    *   **Firebase Storage:** Per l'hosting e la distribuzione di immagini.
*   **Dependency Injection:** **Hilt** per gestire le dipendenze in modo efficiente e promuovere la modularità.
*   **Gestione degli Errori:** Strategia di gestione degli errori strutturata su più livelli (Domain, Data, Presentation) per garantire la robustezza dell'app.

## 🎨 Processo di UX/UI Design

Il design dell'interfaccia è stato un pilastro del progetto, guidato da un processo di ricerca e progettazione approfondito:

1.  **User Research & Analisi dei Competitor:** Conduzione di questionari e analisi di mercato per definire i requisiti funzionali.
2.  **Creazione di Personas:** Sviluppo di profili utente realistici per guidare le decisioni di design.
3.  **Prototipazione su Figma:** Realizzazione di mockup interattivi e wireframe per validare i flussi utente prima dello sviluppo.
4.  **Aderenza a Design System:** Implementazione basata sui principi di **Material Design 3**.
