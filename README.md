# Implementing the Poker Game into a Spring MVC web app:

### How to run right now
the PokerController class has a startGame method to initialize a poker table object 
with the players hardcoded in the method. to run and access the app you must:

     * spring-boot:run in maven
     * localhost:8080/startGame
     * localhost:8080/game?player=John (or a name of a player in the game) to see the game
         

### To do

    * move the dealer button around in the view based on the table.dealerIndex
    * display the winner in the view and restart the game when a winner is determined 
    * note who has folded, whose turn it is in view
    * get the view data to update every second without refreshing whole page
    * functionality to create a player and join the game
    
