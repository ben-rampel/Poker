# Implementing the Poker Game into a Spring MVC web app:

### How to run right now
the PokerController class has a startGame method to initialize a poker table object 
with the players hardcoded in the method. to run and access the app you must:

     * spring-boot:run in maven
     * localhost:8080/startGame
     * localhost:8080/game?player=John (or a name of a player in the game)
         
currently the view template is very basic and nonfunctional. it only gets the potSize dynamically from the model.

### To do

    * Fill players in the view with actual player names and chip amounts 
        * number of player seats can be fixed at 6 for now, with empty seats marked as such
    * Render turn options in the player's view when the currentTurnNotification.getPlayer equals the player for whom the view is displayed
    * Create form in the turn options that POSTs a Turn object to /sendTurn to complete the player's turn and advance the game
    * Dynamically render table and player attributes in the view
        * player cards and common cards
        * players in the round
        * etc
    * move the dealer button around in the view based on the table.dealerIndex
    * note who has folded, whose turn it is in view
    * get the view to update every second? 
    
    * a form to create a player and join the game
