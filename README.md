# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

## Główne założenia (wymagania?) funkcjonalne projektu

Założeniem projektu (nazwa kodowa "Kommunicator") jest stworzenie komunikatora w formie aplikacji
mobilnej na Androida i iOS przy wykorzystaniu technologii Kotlin Multiplatform. Aplikacja będzie
wykorzystywać REST API udostępniane przez cześć serwerową wykonaną we frameworku Ktor.
Dane wszystkich użytkowników z wiadomościami przechowywane będą w bazie danych PostgreSQL.
Sposobem na zabezpieczenie komunikacji między klientem a serwerem oprócz SSL będzie JWT.

Podstawowe funkcjonalności komunikatora:

- rejestracja i logowanie
- dodawanie kontaktów
- wysyłanie i odbieranie wiadomości tekstowych
- statusy (odczytana wiadomość, pisanie)
- historia wiadomości dostępna offline
- powiadomienia push

## Postawienie projektu (2023-06-06)

Potrzebne nam będą:

- Android Studio (w momencie tworzenia AS Flamingo)
- plugin do AS https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile
- macOS, bo bez niego nie zbudujemy apki na iOS

Tworzymy nowy pusty projekt aplikacji (do wyboru jest jeszcze biblioteka).

Nie zmieniamy nic oprócz nazwy apki i paczki (które ustawiamy kolejno na "Kommunicator"
i "io.github.mklkj.kommunicator).

Niestety nazwa aplikacji nie ma odzwierciedlenia na iOS i musimy wykonać dodatkowo to
https://stackoverflow.com/a/239006/6695449.

Dodatkowo z jakiegoś powodu na moim komputerze jest problem z rozpoznawaniem przez AS skryptów .kts.
Rozwiązaniem jest użycie innego JDK niż Embedded JDK (np. Corretto JDK).

## Materiały

- biblioteki KMM 1 - https://github.com/terrakok/kmm-awesome
- biblioteki KMM 2 - https://github.com/AAkira/Kotlin-Multiplatform-Libraries
- biblioteki KMP od IceRock - https://moko.icerock.dev/
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
- JWT do autoryzacji - https://ktor.io/docs/jwt.html
- moze jakaś integracja z ChatGPT? - https://github.com/yml-org/ychat