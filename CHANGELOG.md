# Historia zmian

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

## Zbudowanie mock-upu listy czatów, view modele i DI (2023-11-17)

Zacząłem tworzyć ekran z listą czatów (ChatsScreen). Na te potrzeby utworzyłem model `Chat`,
umieszczając go we współdzielonym między apką a serwerem module `shared`.

Aby od razu zadbać o jego serializację, dodałem we współdzielonym module `kotlinx.serialization`.
Do zapisu czasu wysłania ostatniej wiadomości w czacie użyłem typu z biblioteki `kotlin.datetime`,
która też znalazła się we wspólnych zależnościach.

Wracając do tworzenia layoutu, chcąc wyświetlić obrazek (awatar) czatu, użyłem biblioteki `Kamel`.
Ta wymaga bezpośredniego zdefiniowania odpowiedniego silnika HTTP dla biblioteki `Ktor` dla każdego
z targetów, co zrobiłem, a co przyda się przy tworzeniu części sieciowej.

Następnie zacząłem prace nad architekturą — chciałem dodać view model, by tam trzymać stan listy
chatów, jak i pobierać je z API. Użyłem więc view modeli od mokko oraz koin do DI.
Czy jest idealnie? Nie jest. Ale jako tako na razie działa.

Idąc za ciosem zacząłem kombinować z konfiguracją sieci. Przeniosłem mockowe dane z common do server
i zwracam je teraz z endpointu `/chats`. Apka przy użyciu Ktorfita z Ktorem pod spodem i odpowiednim
silnikiem na Androidzie i iOS pobiera listę czatów z adresu 0.0.0.0. Tak na Androidzie tak i na iOS
wymagane było połączenie https i musiałem to obejść, by móc developać apkę na localhoście.

## Kombinowanie z KSP do libki Koin, żeby nie pisać ręcznie kodu na żadnym targecie (2023-11-26)

Bardzo nie podobało mi się to, że poprzednio android używał ksp do generowania kodu głównego modułu
Koin, a w iOS musiałem ręcznie wpisywać, co jest factory, co jest single, etc
(Hilt przyzwyczaił do dobrego). Z tego powodu poszperałem i ustawiłem Koina dla wszystkich
interesujących nas targetów (czyli wszystkie iOSowe + Android). Brakującą implementację
rozszerzenia `org.koin.androidx.viewmodel.dsl.viewModel` dopisałem na pałę w iosMain sources, tak
by zamiast używać `koinViewModel` dostępnego tylko dla androida, użyć `rememberKoinInject()`
(jako w zasadzie jedynego mi znanego sposobu na to).

I to działa!

Z jednym minusem — Intellij ma problemy z wykryciem wygenerowanego kodu dla iosX64Main w iosMain
i przez `org.koin.ksp.generated.defaultModule` wyświetla się tak, jakby go brakowało, chociaż tak
naprawdę to jest i wszystko się normalnie kompiluje. Jako _obejście_ dodałem expect/actual
w rozbiciu na `iosMain` vs iosX64Main itd., by nie oglądać tego errora :)
To chyba ten issue: https://github.com/google/ksp/issues/963

Dodatkowo chwilowo jesteśmy uwiązani na wersji .13 ksp, ze względu na jakiś dziwny error przy
budowaniu iOSa (dodałem komentarz nad wersją).

### Część druga

Chciałem skonfigurować wcześniej kwestie wielu środowisk (o czym mam nadzieję za chwilę), ale
nie mogłem uruchomić XCode... Pobrałem wersję beta i lecimy dalej!

Patrząc na ten artykuł https://tooploox.com/kotlin-multiplatform-handling-different-environments
i używając tego pluginu https://github.com/yshrsmz/BuildKonfig ustawiłem dwa flavory: dev i prod,
gdzie pierwszy to lokalne środowisko, a prod to mój przyszły (nie skonfigurowany jeszcze) backend
na pich.ovh.
W artykule opisane są osobne konfiguracje .xcconfig dla dev i prod, ale nie wiem po co, kiedy
obie apki mogę przesetawiać przez gradle.properties.

Z paroma przeszkodami, ale udało mi się to zrobić tak, żeby mi w miarę odpowiadało. Największym
minusem jest potrzeba edycji śledzonego przez gita pliku gradle.properties, ale niech na razie tak
zostanie.

Ciekawostka: jeśli Xcode project configuration nie jest ustawione (bo akurat zmienialiśmy dostępne
konfiguracje przez XCode) to build się zawiesza na tej części z gradlem i nic się nie dzieje przez
wiele minut.

## Deploy API (:server) na VPSa (2023-12-07)

Żeby móc się gdziekolwiek pochwalić tym, co tu robię, potrzebna jest możliwość połączenia się z API
nawet bez komputera pod ręką. Dlatego wykorzystałem te rzeczy, które już znam i opakowałem moduł
:server w kontener Dockera, który następnie jest używany przez Docker Compose (to na przyszłość,
żeby móc łatwo bazę danych razem deployować), a to z kolei jest deployowane na VPS w Oracle Cloud
i wystawione pod moją domeną kommunicator.pich.ovh.

Jakie były trudności? Ze względu na to, że moduł serwerowy, moduł współdzielony, jak i apka mobilna
są połączone w jeden wielki projekt, a VPS w Oracle jest na ARM (VM.Standard.A1.Flex, czyli
Arm processor from Ampere) i przez brak prebuildu kotlina na tę architekturę
(kotlin-native-prebuilt-linux-aarch64
https://youtrack.jetbrains.com/issue/KT-36871/Support-Aarch64-Linux-as-a-host-for-the-Kotlin-Native)
to musiałem hackować i wywalić na potrzeby zbudowania tego modułu, modułu composeApp oraz targetów
iOSowych z modułu shared.

## Aktualizacja zależności, przykładowy test, etc (2023-12-22)

Zaktualizowałem parę zależności i dodałem przykładowy test w commonTest. Żeby uniknąć jakichś
dziwnych errorów, że jednej wartości nie ma to dodałem dla BuildKonfigu domyślną wartość `baseUrl`.

## Ekran konwersacji — nawigacja między ekranami (2023-12-23)

https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html

Podjąłem decyzję — do nawigacji użyłem PreCompose. Bo ma swoją implementację view modeli
i integrację z Koinem. Nawet ma wbudowaną nawigację, która przypomina tę z Navigation Component
od Google'a. Ale chwilę później zrezygnowałem z tego wyboru. Chyba właśnie przez ten ostatni punkt.
Navigation Component dla Compose UI jest... siermiężny. Chciałem czegoś nowego.

Dlatego swój wzrok skierowałem ku bibliotece Voyager — posiadającej podobne funkcje (a nawet
więcej), ale zrealizowane trochę inaczej.

Dlaczego nie Decompose albo Appyx? Decompose wymaga pisania sporej ilości boilerplate'u, którego
wolałbym uniknąć. Do tego nie ma wbudowanej integracji czegoś, co by przypominało view modeli
ani integracji z Koinem. Appyx również.

## Ikonka apki, UUID, JWT, ekrany rejestracji i logowania (2023-12-25)

1. Zrobiłem (ukradłem) ikonkę https://uxwing.com/chat-icon/.
2. Zamiast nietypowanych stringów jako UUID machnąłem biblioteczkę od tego, która ma od razu
   wsparcie dla SQLDelight i Jetbrains Exposed.
3. Na podstawie https://codersee.com/secure-rest-api-with-ktor-jwt-access-tokens/ zrobiłem
   (skopiowałem) część serwerową rejestracji (na razie tylko in-memory), pobierania tokenu i
   szczegółów
   usera
4. Machnąłem na szybko ekrany welcome, rejestracji i logowania. Obsługa błędów jest słaba, dużo
   trzeba jeszcze zrobić w okolicach obsługi różnych kodów HTTP z API, ale da się zarejestrować
   i potem zalogować takim kontem.

## Firebase i Crashlytics (2023-12-26)

Jako kolejny krok pomyślałem, że zrobię sobie logowanie ruchu sieciowego. A przy okazji chciałem też
dodać jakąś bibliotekę do zarządzania logami (jakiegoś Timbera). Wybór stanął na Kemricie od
Touchlab. Tam zauważyłem, że jest dostępna integracja z Crashlytics. Super. Przy okazji skonfiguruję
Firebase'a — pomyślałem. O jakże byłem głupi...

Żeby kompleksowo do tego podejść, chciałem od razu użyć biblioteki do obsługi całego Firebase'owego
API. Oczywistym wyborem jest Firebase od gitlive. Tylko że tam trzeba osobno zainstalować
i skonfigurować Firebase na poszczególnych platformach. Z Androidem poszło gładko — jedna zależność,
jeden pliczek konfiguracyjny i wsio. Na iOS to trochę bardziej zagmatwane.

W świecie iOS istnieją teraz w zasadzie dwa package managery — Cocoapods i Swift Package Manager.
Z tym pierwszym KMM ma nawet działającą integrację poprzez gradle'a! Tylko że jej podpięcie znacznie
wydłuża synchronizację projektu. Gdyby to działało od razu, to nawet bym się nie zastanawiał.
Niestety nie działało. Choć `iosMain` widział klasy z paczki Firebase, to apka się nie kompilowała
(linker nie mógł znaleźć frameworku). Kombinowałem z flagami `linkOnly`, ale wtedy, choć apka się
budowała, to crashowała się zaraz przy starcie.

Dlatego zacząłem kombinować z SPMem. Objawy były w zasadzie podobne, nic nie dzialało.
Następnego dnia (2023-12-27) pomyślałem, że spróbuję jeszcze wygenerować inny projekt i porównać
`.xcodeproj`. Moją uwagę zwrócił brak jednego bloku z kopiowaniem frameworków, biblioteką
Crashlyics (czyli tą brakującą!) w sekcji z `Frameworks, Libraries and Embedded Content` oraz
innymi flagami (`${inherited}` czy coś takiego) kompilatora. Przywróciłem flagi i przez Xcode
dodałem brakujący framework. Zadziałało!!!

## Logging (2023-12-27)

Podpiąłem Kermita pod view modele i ktora, żeby w debugu wszystko się ładnie na logcata/to coś w iOS
logowało, a na produkcji by wszystko szło do crashlytics.

Później zacząłem kombinować z lokalną bazą danych SQDelight, żeby zapisywać w niej zalogowanego
usera razem z JWT, ale nie skończyłem, bo jakiś dziwny problem z wylogowywaniem był. Miałem
wrażenie, że Flow nie emituje listy userów po wylogowaniu, ale następnego dnia okazało się, że...

https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-ktor-sqldelight.html

## Baza danych na serwerze (2023-12-28)

... okazało się, że baza danych działała dobrze, tylko ja to napisałem tak, że to nie miało prawa
działać. Myślałem, że jak zmienię startowy ekran on runtime w App.kt, to się to ładnie
przekomponuje, ale to tak nie działa. Dodałem `replaceAll` na WelcomeScreen przy wylogowywaniu
i wszystko gra.

Dalej — usprawniłem ekran z listą czatów oraz z ekranem konwersacji.

Dalej — połączenie z bazą PostgreSQL w części serwerowej. Używamy Jetbrains Exposed. Mały problem
ze zmiennymi środowiskowymi. Fajny
plugin https://plugins.jetbrains.com/plugin/7861-envfile/versions/stable
ale nie dostępny niby dla Android Studio. Na szczęście można to ominąć, pobierając zip ręcznie.

Przewijając do godziny prawie 03:00 - połączenie działa. Udało mi się zarejestrować usera
i zalogować się na niego.
Do zrobienia na pewno będzie zrobienie jakiegoś upserta przy istniejącym
już uuid (problem dwóch generałów) oraz wyjątku przy już istniejącym username.
Będzie trzeba też ogarnąć hashowanie hasła przy tworzeniu kont i porównywanie takiego hasła później.
Celuję tutaj raczej w bcrypt ze Spring security (celuję w coś, co znam, a bcrypta znam z PHP).

Z "sukcesów" (a raczej rzeczy, które się jakoś ładnie złożyły) to użyłem integracji kotlinx.uuid
z SQLDelight i Jetbrains Exposed, dzięki czemu się to jakoś tam ładnie teraz prawie samo
serializuje.

Z ciekawostek to do migracji bazy na backendzie użyłem Flyway. Do zapisania płci użyłem enuma, ale
żeby go zapisać w bazie postgresowej, trzeba było ręcznie utworzyć tam wcześniej takiego enuma
(ja nawet nie wiedziałem, że postgres takie rzeczy ma). Oczywiście, można było to załatwić zwykłym
stringiem, ale to by nie było to samo :)

## Baza danych - deploy, usprawnienia (2023-12-29)

Do późnych godzin wieczornych (a właściwie do 4 w nocy lub nad ranem — co kto woli) próbowałem
ogarnąć bazę danych na VPSie przez docker compose. Teoretycznie wszystko powinno zadzialać od
strzała. Teoretycznie. Jednak ciągle wywalało błąd autoryzacji na usera, którego podałem w envach.
Zmieniłem je na domyślne, tj. `postgres` jako login i hasło. Wtedy znowu z brakiem bazy danych był
problem. Z tego, co kojarzyłem, to baza danych tworzy się tylko przy pierwszym uruchomieniu. Tylko
że wyrzucenie kontenera nie wystarczyło, trzeba było jeszcze wyczyścić volumen (co w sumie dobrze).

Po drodze testowałem też te konenery u siebie i trafiłem na dziwny błąd z nie wykrywaniem migracji
przez Flyway. Taka historia https://stackoverflow.com/a/77237118/6695449. Wystarczyło zrobić jak ten
gość i pykło.

Kontynuując kwestie z wczoraj związane z samym backendem — zrobiłem hashowanie hasła.
Użyłem bcrypta. Zastanawiałem się nad https://github.com/patrickfav/bcrypt, ale jako że to bardzo
delikatna sprawa, to ostatecznie użyłem implementacji ze spring security
(`org.springframework.security:spring-security-crypto`).

Dodałem lepszą (a w zasadzie jakąkolwiek) walidację przy tworzeniu konta i logowaniu. Teraz od razu
jest jasne, że dane konto już istnieje (a raczej, że username jest już użyty) albo że wpisało się
nieprawidłowe dane logowania.

Ciekawostka — żeby wysłać w Ktorze sam kod http to https://ktor.io/docs/responses.html#status.
Co do samego wyboru kodu http to https://stackoverflow.com/q/3825990/6695449.

Do końca dnia bawiłem się z naprawą crasha `java.io.NotSerializableException: kotlinx.uuid.UUID`.
Spowodowany jest on tym, że jako argument w `ConversationScreen` przekazuję `kotlinx.uuid.UUID`,
który to niestety nie jest serializowalny na androidzie (tj. nie implementuje
`java.lang.Serializable`). Więc żeby to zrobić ładnie, bez wycofywania się z wyboru tego typu
pomyślałem, że zmodyfikuję oryginalną bibliotekę. Najpierw sforkowałem oryginalą bibliotekę, która
jak się okazało... już dawno nie jest rozwijana i ja sam używam tutaj już forku *facepalm*.
Straciłem na to kilka godzin, bo przez to, że biblioteka 3 lata nie była ruszana, to nawet zbudować
się nie chciała. Kiedy już wziąłem tego forka i zrobiłem z niego swojego forka, dodałem według
instrukcji z docsów
Voyagera https://voyager.adriel.cafe/state-restoration#multiplatform-state-restoration
JavaSerializable. Do tego teścik z serializacją i deserializacją
według https://www.baeldung.com/java-serialization
i w ten sposób powstał taki PR https://github.com/hfhbd/kotlinx-uuid/pull/282. Sam crash raczej
nie jest mocno dotkliwy na tym etapie projektu, więc mogę poczekać, aż zostanie zmergowany do
*upstreamu*.

## Poszerzenie sprawdzania JWT na endpointy chatu (2023-12-30)

W końcu to trzeba było zrobić — czaty i wiadomości muszą być zabezpieczone. Nawet nie wiecie, jak
się ucieszyłem, gdy zobaczyłem, że jest gotowy plugin pełniący funkcję interceptora dodającego JWT
do nagłówka przy każdym requeście. Zachwyt nie trwał jednak zbyt długo, bo się okazało, że ten token
się tam zapisuje na wieki... aż nie trafi się request z 401. Ale znalazłem obejście, więc jakoś
to przeżyjemy
https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death.

A tu ładne screeny jak cały mechanizm z refresh tokenami mógłby wyglądać
https://medium.com/@lahirujay/token-refresh-implementation-with-ktor-in-kotlin-multiplatform-mobile-f4d77b33b355

## Refresh tokenu (2023-12-31)

Zrobiłem refresh tokenu. Na backendzie mam tabelkę `usertokens` z wszystkimi refresh tokenami i przy
logowaniu tworzę userowi jednego (losowy stiring) i odsyłam razem z walidnym access tokenem (JWT).
Po tym, jak access token przestanie być ważny, to user ma puknąć pod `/api/auth/refresh`, gdzie
dostaje nowy refresh token (poprzedni jest usuwany) i nowy access token. Na razie ustawiłem ważność
access tokenu na minutę. Zastanawiam się, jak powszedni będzie problem z odebraniem nowego access
tokenu przez klienta. Bo jeśli backend go usunie, a user nie dostanie nowego, to efektywnie zostanie
wywalony z apki.

## Obsługa wygasłych refresh tokenów, ekran konta (2024-01-07)

Tydzień mi uciekł...

Zrobiłem obsługę HTTP 401 w apce. Jak refresh token wygaśnie (po miesiącu) to apka raz, że w ogóle
skończy ładować dane (a wieszała się przez mój błąd — blokowała się po otrzymaniu 401 po refreshu)
to teraz wyloguje aktualnie zalogowanego usera i dodatkowo przeniesie go na WelcomeScreen, gdzie
będzie mógł się zalogować ponownie.

Dodałem też sobie ekran konta (z nazwą usera i przyciskiem do wylogowywania), bo czemu nie. Przyda
się.

## Dodawanie kontaktów (2024-01-08)

Voyager, voyager, voyager... niby masz te swoje nawigowanie między ekranami z integracją z tabami,
ale jak chce zrobić i to, i to, to działa to słabo!

Zrobiłem bottom nava z zakładką z kontaktami (coś jak w Messengerze). Przy okazji wynikły problemy
(z nawigacją) i trochę się zeszło.

## Dodawanie kontaktów ciąg dalszy (2024-01-10)

Skończyłem (na razie) kwestię z nawigacją. Dorobiłem ekran dodawania kontaktu oraz backend.
Wywaliłem nagłówek content type json z poszczególnych serwisów i dodałem go jako taki w default
request. Backend teraz umie dodawać pojedynczy kontakt. Jak spróbujesz dodać siebie, to zwróci 409.
Jak to nieistniejący kontakt, to 204, a jak istniejący to 500 - do zmiany. Idzie 500, bo primary key
by się powtarzał i to na poziomie bazy się teraz wywala. Pasuje dodać sprawdzenie, żeby tego
uniknąć. No i pasuje dodać ludzkie komunikaty po stronie apki.

## Cache'owanie kontaktów, tworzenie czatów (2024-01-14)

Zacząłem od kompletnych podstaw i dorobiłem brakującą walidację refresh tokenu. Teraz jak straci
on ważność, to apka wyloguje usera.

Potem poprawki przy logowaniu i rejestracji — trim na danych wejściowych, dodanie brakujących pól,
walidacji do nich.

Zamiast placeholderów avatarów dodałem requesty do gravatara — dzięki temu pokazuje się mój avatar.

Dalej zrobiłem zapisywanie kontaktów do bazy danych, która jest tutaj też jedynym źródłem prawdy.
Przy wejściu na ekran kontaktów oczywiście je odświeżam, ale wszystko przechodzi przez bazę danych.
Tutaj też wyszedł problem z trzymaniem stanu view modeli — po przejściu na ekran dodawania kontaktu
poprzedni ekran z ich listą nie zaktualizuje się mimo emisji z bazy danych. Wygląda na to, że job
zostaje scancelowany. Trzeba to dokładnie jeszcze sprawdzić i znaleźć lepsze rozwiązanie niż
przeładowywanie danych za każdym razem.

No i wisienka na torcie dzisiejszych zmagań — chaty. Dodałem stosowne tableki w bazie danych.
Baza, jak i apka jest w zasadzie przygotowana wstępnie na chaty grupowe, ale chyba nie będę ich
implementował, jeszcze zobaczymy. Na razie działa tworzenie chatu podając id usera. Jest trochę
nadrutowane, bo lista chatów jak i pojedynczy czat nie są zbyt optymalnie wyciągane z bazy danych.
Teraz czym więcej czatów po wejściu do apki tym więcej będzie requestów do bazy, co nie jest dobre.
Trzeba będzie znaleźć opcję na ogarnięcie tego odpowiednimi podzapytaniami itp.

## Ładowanie/przeładowywanie danych chatów i kontaktów (2024-01-25)

Naprawiłem ten dziwny efekt z przeładowywaniem chatów i kontaktów przy ponownym wejściu na dany
ekran. Jak? Wystarczyło ze-scopować view modele na navigatora (coś jak navGraphViewModels
w androidzie).

To z kolei zmusiło mnie w końcu do zaimplementowania pull to refresha na tych ekranach, żeby dało
się z nich korzystać, zanim zajmę się websocketami (a przyda się to i tak, jako fallback).

Niestety w (stabilnej wersji) material3 ciągle pull to refresha. Ale ktoś przeportował. Skopiowałem
(do ui.widgets.PullRefresh). Działa. Przerobiłem siłą rzeczy te ekrany tak, żeby ten pull refresh
działał. Dodałem też pokazywanie snackbara z errorem i przyciskiem ponów. Wydaje się, że strasznie
dużo boilerplate'u wyszło. I to prawda. Nie mam jeszcze pomysłu co z tym zrobić, póki co niech
zostanie.

## Pobieranie listy czatów z ostatnią wiadomością i listą participantów (2024-01-27/28)

To było dopiero wyzwanie! Tl;dr: udało się.

A o co chodziło? Cóż, do tej pory lista czatów była pobierana w bardzo nieefektywny sposób, bo
najpierw była pobierana sama lista czatów, w których participantem jest aktualny user, a potem
DO KAŻDEGO OSOBNO była wyciągana ostatnia wiadomość z tego czatu ORAZ lista participantów.
Udało się zastąpić to wszystko pojedynczym zapytaniem sql.

Problemy, które po kolei wystąpiły:

1. jak napisać takie zapytanie? Początkowo wzorowałem się na
   tym https://stackoverflow.com/a/63353088/6695449, ale potem dla uproszczenia użyłem do pobierania
   ostatniej wiadomości lateral joina https://stackoverflow.com/a/63340078/6695449
2. jak w exposed wykonać takie wielkie query? Raczej nie uda mi się go zapisać przy użyciu
   ichniejszego DSL. Okazuje się, że się da https://stackoverflow.com/a/63451601/6695449
3. jak zmapować wynik tego zapytania? To było trochę ciężkie, niby
   to https://stackoverflow.com/a/66517209/6695449
   oraz https://github.com/JetBrains/Exposed/issues/118 pomogło, ale musiałem jeszcze kombinować

## Unikanie tworzenia nowych czatów dla tych samych uczestników (2024-01-30)

W tej chwili kliknięcie w kontakt tworzy nowy czat. Za każdym razem. Chciałem to obejść tak, żeby
przed utworzeniem nowego czatu sprawdzić, czy dla danej listy participantów już istnieje jakiś czat
i zwracać od razu jego ID. Problem nie jest prosty.
Znalazłem https://stackoverflow.com/a/69180271/6695449, ale tutaj dochodzi jeszcze kwestia tego,
że używam Exposed, a identyfikatorami u mnie są UUID. Dodatkowo ja tu potrzebuję dynamicznie
podstawiać identyfikatory. Znalazłem sposób na obsługę bindowania typu array przy bindowaniu
w tym wątku https://github.com/JetBrains/Exposed/issues/150. Kolejnym problemem okazało się
porównywanie tych tablic — żeby dostać true, obie muszą mieć dodatkowo tę samą kolejność. Szukałem
uniwersalnego sposobu na to (https://stackoverflow.com/q/12870105/6695449) ale ostatecznie zostałem
przy zwykłym sortowaniem w `array_agg` + sortowaniem listy participantów przed bindowaniem.

## Zapisywanie czatów i wiadomości na użytek offline (2024-01-31)

Mocno łączy się to z problemem wyciągania czatów z listą participantów i ostatnią wiadomością.
Tyle że tym razem trzeba to zrobić po stronie klienta. Nie mogłem użyć tego samego query i go
ewentualnie przerobić, bo sqlite nie obsługuje lateral joina. Zrobiłem to więcej inaczej,
przerabiając to https://stackoverflow.com/a/21460015/6695449.
Oprócz tego standardowo — tworzenie tabeli na czaty, uczestników czatu i wiadomości w sqlite, potem
zapisywanie wszystkiego, co dostałem z API i obserwowanie tych tabel na liście czatów. Dzięki temu
ostatniemu chcę łatwo obsłużyć wyświetlanie ostatniej wysłanej wiadomości i automatyczne odświeżanie
listy czatów, gdy pojawi się jakaś nowa wiadomość. W takiej architekturze baza danych będzie jedynym
źródłem prawdy dla UI.

Idąc za ciosem, dodałem jeszcze zapisywanie w lokalnej bazie załadowanych wiadomości po wejściu do
chatu. Teraz zawsze pobiera się tam te 15 (jakoś tak) wiadomości i one zastępują te, istniejące
na urządzeniu. Aplikacja jednak ładuje wiadomości tylko bezpośrednio z bazy (obserwuje zmiany na
tabeli) i dzięki temu niezależnie od tego, czy request do API się akurat uda, czy nie, to zawsze
wyświetlą się wiadomości zapisane w lokalnej bazie danych

## Poprawki wizualne, powiadomienia push (2024-02-02)

Bardzo denerwował mnie snackbar i FAB rysowany zbyt wysoko, tak jakby dolny inset został dwa razy
dodany. Po zmarnowaniu ponad godziny znalazłem rozwiązanie:
https://stackoverflow.com/a/77361483/6695449.

Trafiłem też na buga w wiadomościach — przez dodanie key do widoków teraz lazy column rozpoznaje,
które itemy są nowe, a które nie i lista się sama przestała przesuwać. Dodałem na te potrzeby więc
taki kod jak tu https://stackoverflow.com/a/77231790/6695449, który animuje scroll to najnowszej
wiadomości.

Nadchodzi kolejny ważny w historii tego projektu moment — powiadomienia push. Idę trochę na
łatwiznę, bo zamiast ręcznie implementować wszystko na obu platformach, wykorzystam bibliotekę
https://github.com/mirzemehdi/KMPNotifier. Na razie wydaje się wystarczająca, jeśli nie będzie,
to wtedy faktycznie trzeba będzie zrobić wszystko ręcznie.

Jak to ma działać — przede wszystkim apki mają dostawać z Firebase'a tokeny. Te tokeny mają wysyłać
(jak user się zaloguje albo token się zmieni) urządzenia poszczególnych użytkowników. Tokeny będą
przechowywane w bazie danych w osobnej tabeli. W momencie wysłania wiadomości przez jednego
użytkonika serwer będzie wyciągać z bazy wszystkie tokeny wszystkich (oprócz wysyłającego) userów
biorących udział w czacie i wyśle im wiadomość z informacją, że wysyłający wysłał wiadomość.

Idąc po kolei, to najpierw trzeba było określić strukturę tabeli. Token może mieć różną długość
https://stackoverflow.com/a/39964597/6695449, więc dałem 200. Doszły jeszcze kolumny z id urządzenia
(dzięki czemu przy zmianie tokenu nie będą zostawały stare). Dałem też kolumnę na nazwę urządzenia,
żeby się dało tokeny łatwo zidentyfikować.

Potem integracja z Firebase Admin SDK. Robimy według
instrukcji https://firebase.google.com/docs/admin/setup/. Do zmiennej środowiskowej
GOOGLE_APPLICATION_CREDENTIALS wsadzamy ścieżkę do pliku .json z credentialami (muszę dodać do gh
actions z deployem). Następnie implementujemy wysyłanie wiadomości do wielu userów według wzoru:
https://firebase.google.com/docs/cloud-messaging/send-message#send-messages-to-multiple-devices.

W części klienckiej trzeba podpiąć biblioteki, pododawać w kilku miejscach wysyłanie tokenu, tak jak
https://proandroiddev.com/how-to-implement-push-notification-in-kotlin-multiplatform-5006ff20f76c.
Jak zwykle problemy sprawiał iOS. W ogóle okazało się, że Firebase był źle zainicjowany (a właściwie
wcale nie był) na iOS. Tak jakby przez to, że inicjalizacja była zrobiona w kotlinie, to nie
działało. Robiąc tak, jak w artykule (tj. bezpośrednio w AppDelegate.swift) wszystko nagle zaczęło
działać. Przed wszystko mam na myśli generowanie tokena. Biblioteka Notifier zgłaszała błąd, że
nie została zainicjowana (ją też próbowałem inicjować z kodu w kotlinie).

Po zrobieniu tego wszystkiego otrzymałem pierwsze powiadomienie na Androidzie :tada: Jednak
powiadomienia na iOS dalej nie działają i nie wiem dlaczego. Możliwe, że trzeba skonfigurować APNs.

## Websockets (2024-02-06/07)

Bardzo fajna rzecz, ale jak coś nie działa to ciężko się połapać. Ale udało się, połapałem się.

Ktor ma wersję websocketów dla clienta i servera (w zasadzie to ten sam `common code`).
Na początek wzorujemy się tym https://github.com/ktorio/ktor-websockets-chat-sample/tree/final, bo
do tego linkuje dokumentacja. Jest tam też przykład operowania wieloma sesjami (który jest też
opisany tutaj: https://ktor.io/docs/websocket.html#handle-multiple-session), którym się mocno
zainspirowałem.

Mocno się zatrzymałem na etapie odbierania wiadomości, bo przez niedokładne przeczytanie
dokumentacji i zaimplementowanie tego "na oko" miałem sytuację, gdzie wysyłane wiadomości były
odbierane po drugiej stronie dopiero jako co drugie, albo nawet w pewnym momencie — co czwarte.
Jak się okazało to przez to, że dokumentacja (czy bardziej przykłady) mówi o dwóch sposobach na
odbieranie danych: jeden to pętla `for` na kanale (zmiennej) `incoming` i w środku odczytywanie
z frame, a druga to brak żadnej pętli albo pętla `while(true)` i użycie `receiveDeserialized()`
(https://ktor.io/docs/websocket-client-serialization.html#receive_data). Przechodząc do rzeczy:
pierwsze od razu "odbiera" dane i w `frame` jest już gotowa do obróbki wiadomość; w drugim pętla
sobie działa tak jak zawsze, a w środku metoda `receiveDeserialized()` odbiera i jednocześnie
obrabia (deserializuje) dane. Jeśli się to połączy, to jakby kod będzie próbował dwa razy częściej
odbierać wiadomości niż potrzeba. Uświadomiłem to sobie dopiero jak trafiłem na ten ticket:
https://youtrack.jetbrains.com/issue/KTOR-4452. Ostatecznie zrobiłem swoją metodę pomocniczą,
działającą podobnie do `receiveDeserialized()` z pominięciem wywołania `receive()` w środku.

## Aktualizacja bibliotek, więcej websocketów (2024-02-08/10)

Na początek aktualizacja bibliotek, żeby mieć najnowsze compose multiplatform. Jeszcze wszystko nie
ma stabilnej wersji, ale to nic. Wziąłem wersję dev, żeby była jak najbliższa kolejnemu wydaniu:
https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.6.0-dev1409. Do tego wersja
beta material3, bo coś zepsuli i z compose BOM nie idzie odpowiednia
https://github.com/JetBrains/compose-multiplatform/issues/4157 i nawet działa.

Idąc dalej — do realizacji ostatniego punktu wymagań potrzebuję jeszcze statusów o pisaniu
i aktywności. Żeby przesyłać statusy o tym, że ktoś coś pisze najlepiej wykorzystać websockety.
Nie chcemy jednak tworzyć miliona osobnych połączeń, a maksymalnie jedno per czat (tak jest teraz).
Po tym jednym połączeniu chcemy przesyłać wiadomości. Żeby sobie _ułatwić_ pracę, zastosujemy
polimiroficzną serializację z kotlinx.serialization
https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes.
I tutaj dałem się złapać na jedną niby oczywistą rzecz, ale w natłoku całej reszty kodu jej nie
zauważyłem. Ciągle nie chciał mi się dodawać oryginalny typ do zserializowanych jsonów, przez co
server nie mógł rozpoznać typu ramki. Okazało się, że chodzi o brak jasno zadeklarowanego typu
przy wywołaniu serializera, tj. np. `Json.encodeToString()` zserializuje obiekt tak, jak go widzi.
Żeby wziąć pod uwagę rodzica i dodać pole z `class discriminatorem` trzeba powiedzieć, o jaki typ
nadrzędny nam chodzi, więc powinniśmy zapisać `Json.encodeToString<Typ>()` i tego mi właśnie
na początku zabrakło.

Idziemy dalej i implementujemy statusy o pisaniu wiadomości. Na razie zrobiłem to tak, że user
piszący wiadomość wysyła pusty obiekt websocketem do serwera, który rozgłasza info o tym, że ten
user coś pisze w obiekcie z jego participant id wszystkim zainteresowanym. Następnie ci wszyscy
zainteresowani trzymają listę wszystkich osób, które coś pisały i okresowo usuwają z niej te wpisy,
które są starsze niż 1 sekunda. Dzięki temu w UI pokazujemy tych, którzy faktycznie coś w danym
momencie piszą i ukrywamy, kiedy pisać przestają.

## Websockety ciąg dalszy (2024-02-11)

Niestety wczorajszy deploy się nie powiódł. Apka się wywala, połączenie z websocketami się nie
ustanawia. Dramat.

Pierwszy problem był lekko ukryty i pokazywał się w momencie, kiedy sesji websocketowej nie było.
Powiązane z poprzednimi problemami, czyli z deserializacją — zły obiekt sobie na backendzie
próbowałem deserializować i się wywalało przez nieznane pole `type`. Zamiana na ten bardziej ogólny
wystarczyła.

Drugie, powód braku łączenia z websocketami. W zasadzie to były tu dwa problemy. Najpierw pokazał
się ten spowodowany użyciem złego protokołu (`ws`), kiedy API było za SSLem. Apka dostawała HTTP 304
(pewnie cloudflare próbował przekierować na https). Trzeba było zmienić na `wss` i...
i mamy kolejny problem. Teraz apka dostaje HTTP 404 :) Trochę zabawy w detektywa i mój wzrok padł
na kwestię nagłówków. API jest za nginxowym proxy, więc tam się pewnie header `Upgrade` nie
przesyła do API. Dodanie dwóch linijek do mojej konfiguracji nginxa
https://www.serverlab.ca/tutorials/linux/web-servers-linux/how-to-configure-nginx-for-websockets/
i załatwione.

Dalej — indykator pisania wiadomości. Jest trochę... słaby. Zróbmy go trochę lepszym.
Na początek ulepszenia w debounce/throtlingu/itp/itd. Znalazłem taką fajną metodę, która wysyła
tylko jeden request na 3 sekundy. Na razie wystarczy, chociaż nie jest idealna.

## Statusy przeczytania wiadomości (2024-02-12/13)

Ostatnia brakująca funkcja — statusy odczytania wiadomości. Bałem się, że będzie z nią najgorzej,
a tymczasem udało mi się ją całkiem dopracować i poza jednym mankamentem działa super.

A jak przebiegała historia?

Na początku wyobrażałem to sobie w ten sposób, że stworzę tabelkę, w której będzie trzymana historia
odczytania każdej pojedynczej wiadomości dla każdego uczestnika czatu. No i plan byłby fajny, ale
w realizacji to jest już gorzej. Już abstrahując od ilości danych, którą to by wygenerowało (w
zasadzie rekordów by było dodatkowo liczba wiadomości, razy liczba participantów), to określenie
na czacie, którą wiadomość oznaczyć jako przeczytaną nie jest trywialne. W zasadzie to odczytanie
nawet nie konkretnej, a konkretnych wiadomości, bo przecież ich jest więcej.

Po zasięgnięciu opinii internetu (stackoverflow) trafiłem na odpowiedź kogoś, kto radził
przechowywać jedynie ostatni czas odczytania wiadomości przez daną osobę. Po małym riserczu
organoleptycznym jak robią to inni, np. messenger, wyszło mi tyle, że tam prawdopodobnie jest to
zrealizowane właśnie w ten sposób.

Sama implementacja jest już dość prostolinijna. Tworzymy kolumnę przy chat participants na datę
odczytania, tak po stronie backendu, jak i mobilek. User odbierając wiadomość, wysyła też przez
websocket event o tym, że daną wiadomość odczytał. Później serwer rozsyła tę informację do
wszystkich podpiętych klientów, którzy zapisują ją u siebie w tabeli z uczestnikami czatu.
Apka na podstawie tego zapisanego timestampa określa to, która wiadomość na czacie została odczytana
przez danego użytkownika jako ostatnia i wyświetla w tym miejscu małe kółeczko z awatarkiem. Po
kliknięciu w wiadomosć wyświetla się imię tego użytkownika.
Żeby zachować "spójność" systemu, wiadomości wysyłane przez użytkownika również są później rozsyłane
przez serwer, żeby apka miała poprawną datę utworzenia wiadomości. Dzięki temu u użytkownika, który
wiadomość wysłał, można również bezpiecznie wyświetlić informacje o tym, że ją odczytał (choć to
trochę bez sensu, to przyda się do oceny działania apki).

## Poprawki powiadomień (2024-02-15)

Dodałem pytanie o uprawnienie do wysyłania powiadomień na Androidzie (taki box na ekranie z czatami)
oraz dorobiłem wysyłanie push tokena przy wejściu na ekran z czatami (tak dla pewności).

Ciągle mam problem z iOSem. Podobno powiadomienia powinny działać na symulatorze od Xcode 14,
ale u mnie się nie chcą pokazać.
https://developer.apple.com/documentation/xcode-release-notes/xcode-14-release-notes#Simulator

Według rady z https://stackoverflow.com/a/71133328/6695449 dodałem `gcm.message_id` i w końcu coś
zaczęło się dziać. Wygrzebałem errory z odpowiedzi przy wysyłaniu powiadomień i tam z obaczyłem
`Unexpected HTTP response with status: 401`. Szybkie guglando i mamy to
https://stackoverflow.com/q/55824244/6695449. W skrócie — trzeba skonfigurować APSN.

Tylko żeby to zrobić, trzeba być w _Apple Developer Program membership_ xD
https://forums.developer.apple.com/forums/thread/77135

Dla pewności - sprawdzić jeszcze to
https://www.kodeco.com/11395893-push-notifications-tutorial-getting-started

## Ciąg dalszy powiadomień na iOS

Udało się 🎉! Udało mi się załatwić provisioning profile, dzięki czemu wysyłanie powiadomień na
iOS działa.

Jak to tak mniej więcej trzeba było zrobić?

1. Wejść na https://appstoreconnect.apple.com/apps
2. Dodać nową apkę (wybrać nazwę, domyślny język w App Store)
3. Przy rejestrowaniu trzeba wybrać bundle ID, które najpierw trzeba zarejestrować
   tutaj https://developer.apple.com/account/resources/identifiers/bundleId/add/bundle
4. Tam wybieramy w Capabilities pozycję "Push Notifications"
5. Przechodzimy dalej i wrzucamy swój certyfikat z naszego Maca, a w następnym kroku pobieramy i
   instalujemy wygenerowany certyfikat
6. Kolejny krok to utworzenie Provisioning Profile
7. Wybieramy tam "iOS App Development", potem utworzone wcześniej app ID i zaznaczamy wygenerowany
   certyfikat
8. Potem zaznaczamy urządzenia, na których chcemy testować apkę
9. I na końcu możemy pobrać plik z wygenerowanym Provisioning profilem.

Nie jestem pewny, czy publikowanie TEAM_ID w tym repo będzie bezpieczne, więc ukryłem je
w `Config.xcconfig`, który to plik nie będzie śledzony przez gita, w zasadzie podobnie jak tutaj
https://stackoverflow.com/a/75226629/6695449.

## Materiały

- biblioteki KMM 1 - https://github.com/terrakok/kmm-awesome
- biblioteki KMM 2 - https://github.com/AAkira/Kotlin-Multiplatform-Libraries
- biblioteki KMP od IceRock - https://moko.icerock.dev/
- tutorial tworzenia konsolowej aplikacji czatu - https://ktor.io/docs/creating-web-socket-chat.html
- JWT do autoryzacji - https://ktor.io/docs/jwt.html
- moze jakaś integracja z ChatGPT? - https://github.com/yml-org/ychat
- przykładowa aplikacja KM w Compose UI i Swift UI - https://github.com/getspherelabs/cosmo-kmp
- przykładowy schemat bazy danych - https://github.com/yoosuf/Messenger
- aplikacja chatu z Gemini - https://github.com/chouaibMo/ChatGemini/tree/main

https://github.com/tunjid/Tiler
https://github.com/getspherelabs/cosmo-kmp
https://atscaleconference.com/messaging-at-scale/
https://www.donnfelker.com/why-kotlin-multiplatform-wont-succeed/
https://github.com/theapache64/rebugger
https://stackoverflow.com/questions/28907831/how-to-use-jti-claim-in-a-jwt
https://github.com/JetBrains/Exposed/wiki/Transactions
https://github.com/yveskalume/gemini-chat
