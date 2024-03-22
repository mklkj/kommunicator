# Kommunicator — aplikacja multiplatformowa w Kotlin Multiplatform

[![Bitrise](https://img.shields.io/bitrise/e9f1ec00-2da0-40c7-9fdd-36b2c6a0ea46/master?token=UPKOp09hQ_iw6OdY0OeWWg&style=flat-square)](https://app.bitrise.io/app/e9f1ec00-2da0-40c7-9fdd-36b2c6a0ea46)
[![Download latest apk](https://img.shields.io/badge/apk-download_latest-blue?style=flat-square)](https://manager.wulkanowy.net.pl/v1/download/app/e9f1ec00-2da0-40c7-9fdd-36b2c6a0ea46/branch/master)

Założeniem projektu (nazwa kodowa "Kommunicator") jest stworzenie komunikatora w formie aplikacji
mobilnej na Androida i iOS przy wykorzystaniu technologii Kotlin Multiplatform. Aplikacja będzie
wykorzystywać REST API udostępniane przez cześć serwerową wykonaną we frameworku Ktor.
Dane wszystkich użytkowników z wiadomościami przechowywane będą w bazie danych PostgreSQL.
Sposobem na zabezpieczenie komunikacji między klientem a serwerem oprócz SSL będzie JWT.

Podstawowe funkcjonalności komunikatora:

- [x] rejestracja i logowanie
- [x] dodawanie kontaktów
- [x] wysyłanie i odbieranie wiadomości tekstowych
- [x] statusy (odczytana wiadomość, pisanie)
- [x] historia wiadomości dostępna offline
- [x] powiadomienia push 

## Uruchomienie projektu

### Serwer

```shell
$ cp .env.example .env # skopiowanie domyślnych ustawień
$ docker-compose up    # utworzenie obrazów z serwerem i bazą danych oraz ich uruchomienie
```

### Aplikacja

> [!IMPORTANT]
> Do zbudowania aplikacji wymagane jest co najmniej JDK 17.

> [!NOTE]
> Aplikacja domyślnie buduje się z adresem API ustawionym na http://192.168.227.5:8080/. Należy go
> dostosować do adresu hosta z postawionym w poprzednim kroku serwerem w pliku
> ./composeApp/build.gradle w bloku `targetConfigs`.

## Android

```shell
$ ./gradlew assembleDebug # zbudowanie apk
```

> [!TIP]
> Plik .apk będzie znajdował się w `./composeApp/build/outputs/apk/debug/`

## iOS

Do zbudowania aplikacji z działającymi powiadomieniami potrzebny jest Provisioning Profile,
a więc i konto Apple ID z Apple Developer Program membership.

W pliku Config.xcconfig ustawia się TEAM_ID używany przy budowaniu aplikacji. Można zostawić pusty
(jak w Config.xcconfig.example), ale wtedy nie będą działać powiadomienia.

```shell
$ cp iosApp/Configuration/Config.xcconfig.example iosApp/Configuration/Config.xcconfig
```
