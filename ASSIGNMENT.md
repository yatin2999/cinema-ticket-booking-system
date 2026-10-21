# Object-Oriented Programming Project

## Cinema Ticket Booking System

Develop a Java desktop application for booking tickets at a small cinema showing a handful of popular movies. The cinema's auditoriums, movies and show times are read from a text file when the application starts. Customers choose a show, pick their seats, book them and manage their bookings, and the cinema's occupancy and sales can be reviewed at any time.

### Ticket Booking

- Browse the shows, sorted by showtime or by movie title, with each movie's duration and short description, the start time and the auditorium.
- Choose standard or premium seats on a seat map, each class with its own fixed price, enter the customer's name, and confirm the booking to receive a ticket with that name and a booking reference.
- Find any booking by its reference, and cancel a confirmed booking so that its seats become available again.

### Cinema Overview

- Load the auditoriums, movies and shows from a simple text catalogue, with basic input-error handling.
- Show which seats of each show are available and which are already booked.
- Display seats sold, occupancy and sales for every show and for the whole cinema, kept up to date after each booking or cancellation.

### Expected Concepts

The project should demonstrate sound object-oriented design: classes with clear responsibilities, encapsulation, inheritance where it fits naturally, interfaces and polymorphism, records and enums for simple values, collections, exception handling and reading data from a file. The design should use the Observer pattern to keep the screens in step with the bookings and the Strategy pattern for interchangeable ways of ordering the shows. The result should be a clear and polished Java desktop application.

### Testing and Submission

The application should be demonstrated by running it and walking through booking, lookup, cancellation and the overview. The submission should include the Java source code, the sample catalogue, instructions for building and running the application, and short design documentation with UML diagrams.
