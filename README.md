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

## Aktualizacja bibliotek (2023-11-14)

Trochę mnie tu nie było... aktualizujemy zależności i upewniamy się, że wszystko działa. Oprócz
podbicia wersji kotlina i AGP podbiłem też wersję javy tak jako target, tak i source compatibility.

W międzyczasie zainstalowałem plugin https://touchlab.co/xcodekotlin.

W międzyczasie pojawiła się możliwość pisania UI aplikacji na iOS w Compose UI oraz generator
projektu (https://kmp.jetbrains.com/). Przerabiam ten projekt według tego, co wypluje ten generator.
Ten wizard wygenerował też moduł z backendem (<3) więc chętnie tego użyję.

## Zbudowanie mock-upu listy czatów (2023-11-17)

Zacząłem tworzyć ekran z listą czatów (ChatsScreen). Na te potrzeby utworzyłem model `Chat`,
umieszczając go we współdzielonym między apką a serwerem module `shared`.

Aby od razu zadbać  o jego serializację, dodałem we współdzielonym module `kotlinx.serialization`.
Do zapisu czasu wysłania ostatniej wiadomości w czacie użyłem typu z biblioteki `kotlin.datetime`,
która też znalazła się we wspólnych zależnościach.

Wracając do tworzenia layoutu, chcąc wyświetlić obrazek (awatar) czatu, użyłem biblioteki `Kamel`.
Ta wymaga bezpośredniego zdefiniowania odpowiedniego silnika HTTP dla biblioteki `Ktor` dla każdego
z targetów, co zrobiłem, a co przyda się przy tworzeniu części sieciowej.

## Materiały

- biblioteki KMM 1 - https://github.com/terrakok/kmm-awesome
- biblioteki KMM 2 - https://github.com/AAkira/Kotlin-Multiplatform-Libraries
- biblioteki KMP od IceRock - https://moko.icerock.dev/
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
- JWT do autoryzacji - https://ktor.io/docs/jwt.html
- moze jakaś integracja z ChatGPT? - https://github.com/yml-org/ychat
- przykładowa aplikacja KM w Compose UI i Swift UI - https://github.com/getspherelabs/cosmo-kmp


https://github.com/tunjid/Tiler
https://github.com/getspherelabs/cosmo-kmp
https://atscaleconference.com/messaging-at-scale/
https://www.donnfelker.com/why-kotlin-multiplatform-wont-succeed/
https://github.com/theapache64/rebugger
