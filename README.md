## Motion detection AI doorbell assistant/intercom

The application is designed to be used on old Android devices.
It can be used as an entrance check camera, phone and a doorbell.
After motion is detected, if the app is run as a home launcher or run as an assistant,
it sends an event to the local area network that on client devices with
the same app shows video from the sender in a small right bottom square.
It can be dismissed by click.

The application also includes a VoIP phone that can be used for remote
communication with an assistant device or between all clients.

Simply, it allows local area network calls.

### Achieved so far:

- motion detection
- VoIP phone
- stateful media player
- auto LAN NSD discovery
- some cute design
- phone library that can be customized on the fly
- permission component compose for Android
- voice AI transcription / voice detection part
- koog preimplementation
- auto-discovery AI ollama server

### To do:

- Whisper integration or an autodetection of the AI server with Whisper
- STT / TTS completion

### Screens:

<!--suppress ALL -->
<table>
  <tr>
    <td><img src="sources/Screenshot_20260115_115820.png" alt="Screen 6" width="200"></td>
    <td><img src="sources/Screenshot_20260115_115813.png" alt="Screen 7" width="200"></td>
  </tr>
  <tr>
    <td><img src="sources/Screenshot_20260115_120036.png" alt="Screen 1" width="200"></td>
    <td><img src="sources/Screenshot_20260115_120044.png" alt="Screen 2" width="200"></td>
    <td><img src="sources/Screenshot_20260115_120028.png" alt="Screen 3" width="200"></td>
    <td><img src="sources/Screenshot_20260115_120011.png" alt="Screen 4" width="200"></td>
    <td><img src="sources/Screenshot_20260115_115830.png" alt="Screen 5" width="200"></td>
  </tr>
</table>
