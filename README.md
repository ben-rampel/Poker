# Texas Hold'em

## Information
A Java application simulates a Texas Hold'em game and emits the current game state as JSON via a WebSocket connection to the web client.
The AngularJS-based client renders the poker table and all the information the player needs and sends turn data to the server over WebSockets to advance the game.

## How to run
* Get dependencies with Maven & Bower
* Start server with the "spring-boot:run" Maven goal
* Open client and enter name of a player in the game.
