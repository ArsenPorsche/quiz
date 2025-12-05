Quiz

Krótki opis

Quiz to prosta aplikacja do przeprowadzania quizów, z backendem w Javie. Projekt służy do przechowywania pytań oraz prowadzenia testów użytkowników.

Co można robić

Główne możliwości projektu:
- Logować się i Rejestrować się.
- Dodawać, edytować i usuwać pytania.
- Dodawać, edytować i usuwać kategoirie.
- Tworzyć i przeprowadzać testy/quizy dla użytkowników.
- Przeglądać wyniki testów.
- Importować pytania w formatach CSV.

Autorzy i wkład

- Arsen Popovych 99808
  - Model danych
  - Część powiązana z kategoriami i pytaniami
  - Część powiązana z autoryzacją
  - Testy

- Palina Prakapenia 99809
  - Model danych
  - Część powiązana z całym przeprowadzeniem quizów i otrzymaniem wyników
  - Testy

Uwagi:

  - Projekt korzysta z bazy danych MySQL.
  - W dołączonym dumpie bazy znajdują się przykładowe dane, m.in.:
 - Ponad 50 pytań testowych.
 - Użytkownicy testowi:
  admin / 123 (admin)
  polina / 123 (player)
  arsen / 123 (player)

  - Rejestracja użytkowników jest testowa i została przygotowana do obsługi za pomocą Postmana.
  - Podczas rejestracji istnieje możliwość wyboru roli użytkownika (np. PLAYER / ADMIN), co pozwala na przetestowanie uprawnień w systemie.
