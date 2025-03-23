# 📱 Absolute Cinema (Android App)
Last update: 23/03/2025
## Disclaimer
To make the app work a server is required, so that's why I do not provide the apk. All the attached files are in Italian.
## Introduction
Absolute Cinema is an Android app I developed with a colleague as a university project for an exam in 2024. This app utilizes a client-server architecture, with the Android app acting as the client and a Flask server providing the backend functionality. 
We did this app for a fictitious cinema chain and we managed to finish our work in less than 2 months.
## App's features
The software has a login/sign in system that is called during the app startup. If the user is already logged, the app skips the login screen. Of course we implemented some mechanisms in order to check the correctness and the validity
of user's input. Once in the home screen, the user can see the scheduled movies to be shown in the cinemas chain's and the upcoming ones. The user can see the details of each movie and then choose if buying one or more tickets for a specific show, 
though he first needs to register a payment method or to pay a subscription (which provides a booklet of discounted tickets). From the home screen it's also possible to navigate to other menus, such as the one dedicated to subscription plans or the
profile screen, where the user can modify some of his personal data (e-mail, password, payment method, phone number). That said, users can also log out or delete their accounts. 
## Testing
The app runs without issues on a variety of devices and we patched all the bugs found. We also made a server using Flask to test the app within a subnetwork.
## Strenghts
- **Simplicity:** the app contains only useful features so that it's simple to use for a huge variety of people
- **User-friendly UI:** a minimal and modern UI is provided to please the user's eyes and be intuitive
- **UI animations:** navigating between different screens feels very fluid because of simple transition animations
- **UI scalability and responsiveness:** the UI is made almost entirely of ConstraintLayout in order to improve performance and adapt it to different devices
## Languages used
- Kotlin
- XML
- SQL
- Python
## Tools used
- Android Studio
- MySQL Workbench
- Git
## My role in the project
- Worked on the design of the app's structure, features and aesthetic
- Developed most of the Kotlin code and the entire UI
- Implemented UI animations
- Helped the design of the database's tables
- Tested the app on a real Android device
