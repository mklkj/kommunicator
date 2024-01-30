# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

Założeniem projektu (nazwa kodowa "Kommunicator") jest stworzenie komunikatora w formie aplikacji
mobilnej na Androida i iOS przy wykorzystaniu technologii Kotlin Multiplatform. Aplikacja będzie
wykorzystywać REST API udostępniane przez cześć serwerową wykonaną we frameworku Ktor.
Dane wszystkich użytkowników z wiadomościami przechowywane będą w bazie danych PostgreSQL.
Sposobem na zabezpieczenie komunikacji między klientem a serwerem oprócz SSL będzie JWT.

Podstawowe funkcjonalności komunikatora:

- [x] rejestracja i logowanie
- [x] dodawanie kontaktów
- [x] wysyłanie i odbieranie wiadomości tekstowych
- [ ] statusy (odczytana wiadomość, pisanie)
- [ ] historia wiadomości dostępna offline
- [ ] powiadomienia push 
