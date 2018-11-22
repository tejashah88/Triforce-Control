# Triforce-Control
An Android app that bridges any ADT-1 dev game controller to a computer to allow playing GameCube/Wii Games (mainly SSBM).

## How can I use it?
Download the APK on your phone from [here](https://github.com/tejashah88/Triforce-Control/releases). Make sure that your phone can install apps from unknown sources. The app should support from Lollipop (5.0) and above, but I can't make any promises. Ensure that bluetooth is on and that your game controller is paired to your phone. Launch the server and once it says "Application started", open the app and click the "Refresh" button. Once the controller and server connections are verified, you are ready to play! Have fun! :)

### What is it?
As the description implies, it's the Android app portion on the Triforce Control project, which is a system to connect the ADT-1 dev game controller (or really any Android TV based game controller) to a computer via an Android a phone. You can check out the server project here: https://github.com/tejashah88/Triforce-Listener

### Why did you make it?
The primary purpose was that I wanted to play GameCube/Wii Games with the ADT-1 dev kit's game controller, and everything but the joysticks were working. Since the dev kit was "no longer available" about 3 months after I got it, and most solutions applied more for the newer NVIDIA shield TV controller or similar variants, I decided to create a psuedo-driver for the controller and my computer using an Android phone as a bridge.

### How does it work?
The project consists of 2 main parts: the Android app and the Server. The Android app is responsible for retrieving inputs from the game controller and relaying them to the server over web sockets, and the server is responsible for translating said inputs from the controller into keyboard/mouse inputs in order to emulate a GameCube controller.