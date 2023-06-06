# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

## Główne założenia (wymagania?) funkcjonalne projektu

Założeniem projektu jest stworzenie komunikatora w formie aplikacji mobilnej na Androida i iOS
(nazwa kodowa "Kommunicator") wraz z backendem w języku Kotlin przy wykorzystaniu technologii
Kotlin Multiplatform. Jednym z celów jest współdzielenie między platformami jak największej ilości
kodu, a co najmniej wydzielenie i współdzielenie warstwy z logiką biznesową.

Podstawowe funkcjonalności komunikatora:

- rejestracja i logowanie
- dodawanie kontaktów
- wysyłanie i odbieranie wiadomości tekstowych
- statusy (odczytana wiadomość, pisanie)
- powiadomienia push

Dodatkowe opcjonalne funkcjonalności:

- grupy
- historia wiadomości dostępna offline
- udostępnianie plików, obrazów

## Postawienie projektu (2023-06-06)

Potrzebne nam będą:

- Android Studio (w momencie tworzenia AS Flamingo)
- plugin do AS https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile

Tworzymy nowy pusty projekt aplikacji (do wyboru jest jeszcze biblioteka).

Nie zmieniamy nic oprócz nazwy apki i paczki (które ustawiamy kolejno na "Kommunicator"
i "io.github.mklkj.kommunicator).

Niestety nazwa aplikacji nie ma odzwierciedlenia na iOS i musimy wykonać dodatkowo to
https://stackoverflow.com/a/239006/6695449.

## Materiały

- biblioteki KMM - https://github.com/terrakok/kmm-awesome
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
