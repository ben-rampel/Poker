# Texas Hold'em

![alt text](https://github.com/brampel/Poker/blob/dev/pokerscreencap.PNG "Logo Title Text 1")

## Information
A Java application simulates a Texas Hold'em game and emits the current game state as JSON via a WebSocket connection to the web client.
The AngularJS-based client renders the poker table and all the information the player needs and sends turn data to the server over WebSockets to advance the game.

## How to run
* Install dependencies with Bower and Maven
* Start server with the "spring-boot:run" Maven goal
* Go to localhost:8080 in browser
