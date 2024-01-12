# HOW TO PULL REQUEST

### ABOUT

This is the reference guide to correctly create a PR and how to structure the information. Think in a PR in a way communicate to the team what features you have implemented, how you did it and mainly, as an explanation of your work.

### STRUCTURE

1.	**Title:** It must be concise and descriptive. If the title has part or the whole the first commit description and you think is not too concise or accurate, feel free to change it in the PR editor.
2.	**Description:** It must explain the context of your PR, what problem is solving, how you did it and other important information to understand your code. At the end of the description, you can add the name of the ticket / tile / task to place the PR in a specific point.
3.	**Changes:** A list of your improvements, bug fixes, refactors, etc. If you think that some point in the list needs a further explanation, feel free to add more text or screenshots.
4.	**Additional notes:** Everything you think your team needs to know, any doubts about something specific in your PR, any additional checks…
5.	**Mandatory meme:** Everything is better if you laugh at it. After all, a PR is simply a “section” where you “talk” to your teammates, so it doesn’t have to be formal.

### EXAMPLE

1.	**Title:** Implement route drawing on map.
2.	**Description:** This PR introduces the route drawing on a map feature. Every time you press the START ROUTE button on the main screen, the app will record every location the user is going through until the user press the FINISH button. Then the system will draw a line connecting all the recorded points. Ticket #DrawLineInMap
3.	**Changes:**
  -		Updated method “methodToRecordPoints”
  -		Added method “methodToDrawLine”
  -		Added “Start” and “Finish” button to “main screen.”
  -		Refactored variable “point” to “location”
4.	**Additional notes:** Please, check if the position of the buttons is correct.
5.	**Mandatory meme:**
   
![trump-drawing-gif](https://github.com/Terciodecuplo/Wheely/assets/112321922/c87c9504-e414-49f2-b3f6-ed878700f74e)
