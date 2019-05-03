# Poker
Java Texas Hold 'em game

### Done:
* Card, deck, table abstractions
* Turn and TurnNotification event abstractions
* Methods to determine a player's best hand 
  * e.g. from a player's 2 hole cards + 5 community cards, what is the highest ranked hand he can play
>1. Royal Flush 
>2. Straight Flush
>3. Four of a kind   
> et cetera

* Can play a round of poker via command line

### To do:
* ~~Fully implement players as well as a table object to store the data necessary to depict the current state of the poker game for a player
  * ~~Table
    * pre-flop, flop, turn, river, or an interim?
    * list of players
    * pot size
    * community cards
    * last turn
  * ~~Player
    * in the round?
    * how many chips
    * dealer? small blind? big blind?
    * how many chips have they bet in the current round
* ~~Create the functionality to play a round of poker
  * ~~Post blinds
  * ~~Show cards
  * ~~Allow bets starting at the player after the big blind
    * ~~Add bet to pot, show players the actions of other players
    * ~~If a player folds, remove from table
  * ~~If only one player hasn't folded, end round and award pot~~
  * ~~When back at dealer, go to next round--repeat til last round of betting~~
  * ~~Showdown, award pot to winner~~
* Create a GUI 
  * perhaps an MVC web application to facilitate online multiplayer poker
