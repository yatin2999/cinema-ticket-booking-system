# Cinema Ticket Booking System

A Java desktop application demonstrating object-oriented programming through cinema ticket booking. It lists five popular movies with short descriptions and their shows, lets you pick standard or premium seats on a seat map, issues a ticket in the customer's name with a booking reference, and shows how full each show is and how much it has sold.

[Watch the demo](https://github.com/user-attachments/assets/8a8a4989-bdc4-4a44-8d10-4d3e5dc4ec06)

## Features

- Five movies (Titanic, Zootopia, Inside Out, Inception and Interstellar) shown in two auditoriums.
- A show list sorted by showtime or by movie title. The selected show's heading includes the movie's duration, with a short description below it.
- A seat map with standard seats at ₹250.00 and premium seats (the rear rows) at ₹350.00, and up to 10 seats per booking.
- Tickets in the customer's name, with references such as `BK-0001`, lookup by reference, and cancellation that frees the seats again.
- An overview of seats sold, occupancy and sales per show and in total.
- A plain-text catalogue, `data/catalogue.txt`, holding the auditoriums, movies and shows.

## Requirements

- Windows with JDK 25, with `java` and `javac` on the `PATH`.
- Windows PowerShell 5.1 or PowerShell 7.
- Internet access for the first build, to download JavaFX 25.0.4.

## Build and run

In PowerShell, from this folder:

```powershell
.\build.ps1
.\run.ps1
```

The first time it runs, `build.ps1` downloads the JavaFX libraries for Windows from Maven Central over HTTPS into `lib`, and then compiles `src` into `out\classes`. Both folders stay on your computer and are ignored by Git; you can delete them and build again at any time.

`run.ps1` starts the application with the bundled catalogue. To use another catalogue file, pass its path, in quotes if it contains spaces:

```powershell
.\run.ps1 "C:\My Catalogues\week end shows.txt"
```

A relative path is resolved against the current folder. If PowerShell refuses to run scripts, run them as `powershell -ExecutionPolicy Bypass -File .\build.ps1` (and the same for `run.ps1`), which affects only that command.

## Catalogue format

A catalogue is UTF-8 text with one record per line and fields separated by `|`. Spaces around fields, blank lines and lines starting with `#` are ignored.

```text
AUDITORIUM | AUD1 | Audi 1 | 8 | 12 | 2
MOVIE | MOV1 | Titanic | 195 | A romance unfolds aboard the ill-fated ocean liner.
SHOW | SHW2 | MOV1 | AUD1 | 2026-11-06 13:15
```

- `AUDITORIUM | id | name | rows | seats per row | premium rows`: up to 26 rows of up to 30 seats; the premium rows are counted from the back.
- `MOVIE | id | title | running time in minutes | description`: the short description is optional.
- `SHOW | id | movie id | auditorium id | start`: the movie and the auditorium must be defined on earlier lines.

The application reports basic format errors, such as a wrong number of fields, an invalid number or start time, an unknown record type, or a reference to an undefined movie or auditorium. The window names the line, and nothing is loaded.

## Demonstration

1. Run `.\run.ps1`. The Booking tab lists the five shows by showtime. Choose **By movie title** above the list to sort them alphabetically.
2. Choose _Titanic_. Its heading reads "Titanic · 195 min" above a short description, and rows G and H, marked ★, are premium.
3. Select seats D7 and G5: ₹250.00 and ₹350.00, a total of ₹600.00. Type a customer name, and choose **Confirm booking**. The Tickets tab opens with ticket BK-0001 in that name.
4. Open the Overview tab: Titanic now has 2 of 96 seats sold (2.1%) and ₹600.00 in sales.
5. On the Tickets tab, type `bk-0001`, choose **Find booking**, then **Cancel this booking** and confirm. The ticket shows CANCELLED, the seats are free again, and the overview returns to zero.

## Source layout

- `src`: the Java classes, in the default package. `CinemaApp` starts the application, `BookingService` holds the bookings and notifies the three tabs through `BookingListener`, and `ShowOrder` provides the two show orderings.
- `resources/cinema.css`: the stylesheet.
- `data/catalogue.txt`: the bundled catalogue.
- `docs/DESIGN.md`: the classes, UML diagrams and design patterns.
- `build.ps1` and `run.ps1`: the build and run scripts.

## License

Released under the MIT License; see [LICENSE](LICENSE).
