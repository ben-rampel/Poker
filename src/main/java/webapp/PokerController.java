package webapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poker.*;

import java.util.*;

import static poker.utils.handTypes;

@Controller
public class PokerController {
    private Table table;
    private Turn currentTurn;
    private TurnNotification currentTurnNotification;

    @GetMapping("/game")
    public String game(@RequestParam(name="player") String player, Model model) {
        LinkedHashMap<Player,Boolean> players = new LinkedHashMap<>(table.getPlayersInRound());
        while(players.size() < 6){
            players.put(new Player("Empty seat",0), false);
        }
        model.addAttribute("players", players);
        model.addAttribute("potSize", table.getPotSize());
        if(table.getPlayerFromName(player).getHole()[0] != null) {
            model.addAttribute("playerHand", table.getPlayerFromName(player).getHoleAsString());
        } else {
            model.addAttribute("playerHand", "No cards");
        }
        model.addAttribute("commonCard", table.getCommonCards());

        /* TODO: Check if the currentTurnNotification is for the given player. if it is, add the TurnNotification to the model */

        return "poker";
    }

    @GetMapping("/startGame")
    public String startGame(){
        table = new Table();
        List<Player> playerList = new LinkedList<>();
        playerList.add(new Player("John", 250));
        playerList.add(new Player("Bob", 250));
        playerList.add(new Player("Carl", 250));
        playerList.add(new Player("Joe", 250));
        addPlayers(playerList);
        startRound();

        return "";
    }

    @PutMapping("/sendTurn")
    public String sendTurn(@RequestParam(name="turn") String turn, Model model){
        //parse turn into a new turn object and put in currentTurn
        return "";
    }
    public void addPlayers(List<Player> players){
        for(Player p : players){
            table.addPlayer(p);
        }
    }

    public void startRound(){
        for(Map.Entry<Player,Boolean> entry: table.getPlayersInRound().entrySet()){
            entry.setValue(true);
        }
        table.setDealerIndex(table.getDealerIndex() + 1);
        Player[] players = table.getPlayersInRound().keySet().toArray(new Player[0]);
        //Tell player left of dealer he may fold or post the small blind
        Turn lastTurn = new Turn(null,null,0);

        while(players.length > 0) {
            players[(table.getDealerIndex() + 1) % players.length].setRequiredBet(table.getSmallBlind());
            lastTurn = players[(table.getDealerIndex() + 1) % players.length].playTurn(
                    new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL),
                            0, table.getSmallBlind()),this);
            if(lastTurn.getAction() == Turn.PlayerAction.FOLD){
                table.getPlayersInRound().put(lastTurn.getPlayer(),false);
                List<Player> nonFoldedPlayers = new ArrayList<>();
                for(Player p : table.getPlayersInRound().keySet()){
                    if(table.getPlayersInRound().get(p)){
                        nonFoldedPlayers.add(p);
                    }
                }
                players = nonFoldedPlayers.toArray(new Player[0]);
                table.setCurrentPlayerIndex(table.getCurrentPlayerIndex()+1);
            } else {
                break;
            }
        }
        if(players.length == 0) {
            return;
        }
        receivePlayerTurn(lastTurn);
        players[(table.getDealerIndex() + 2)%players.length].setRequiredBet(table.getBigBlind());
        lastTurn = players[(table.getDealerIndex() + 2)%players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL),
                        0, table.getBigBlind()),this);

        for(Player p : players){
            p.drawHole(table.getDeck());
            System.out.println(p.getName() +  ", your hand is: " + p.getHoleAsString() + ". You have " + p.getChips() + " chips.");
        }

        receivePlayerTurn(lastTurn);




        System.out.println("POT AMOUNT: " + table.getPotSize());

        table.setCurrentPlayerIndex(table.getDealerIndex()+3);
        lastTurn = players[table.getCurrentPlayerIndex()%players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                        table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1)%players.length]), 0),this);
        receivePlayerTurn(lastTurn);

        System.out.println("Winner: " + table.getWinner().getName() + " with a " + handTypes[table.getWinner().bestHand(table.getCommonCards().toArray(new Card[0]))-1]);
        table.getWinner().receiveWinnings(table.getPotSize());

        for(Player p : players){
            p.drawHole(table.getDeck());
            System.out.println(p.getName() +  ", your hand was: " + p.getHoleAsString() + ". You now have " + p.getChips() + " chips.");
        }

    }

    private void receivePlayerTurn(Turn t){
        //note: fold needs to remove you from lists and set you to not playing
        //need to check if all players but one have folded
        //if so, end the game
        System.out.println("ROUND: " + table.getRound());
        table.setPotSize(table.getPotSize() + t.getBetAmount());
        if(t.getBetAmount() > 0) {
            table.getPlayerBets().put(t.getPlayer(), t.getBetAmount());
        }
        if(t.getAction() == Turn.PlayerAction.FOLD){
            table.getPlayerBets().remove(t.getPlayer());
            table.getPlayersInRound().put(t.getPlayer(), false);
        }
        System.out.println("TABLE RECEIVED ACTION OF\n\t " + t.toString());
        List<Player> nonFoldedPlayers = new ArrayList<>();
        for(Player p : table.getPlayersInRound().keySet()){
            if(table.getPlayersInRound().get(p)){
                nonFoldedPlayers.add(p);
            }
        }
        Player[] players = nonFoldedPlayers.toArray(new Player[0]);
        //Player[] players = playersInRound.keySet().toArray(new Player[0]);
        table.setCurrentPlayerIndex(table.getCurrentPlayerIndex()+1);

        //go to next round once the dealer plays his turn
        if(table.getCurrentPlayerIndex() == table.getDealerIndex()+players.length+1){
            switch(table.getRound()){
                case PREFLOP: table.setRound(Table.ROUND.FLOP);
                    for(int i = 0; i < 3; i++) {table.getCommonCards().add(table.getDeck().next());}
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                case FLOP: table.setRound(Table.ROUND.TURN);
                    table.getCommonCards().add(table.getDeck().next());
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                case TURN: table.setRound(Table.ROUND.RIVER);
                    table.getCommonCards().add(table.getDeck().next());
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                //showdown time -- implement end of game logic
                case RIVER: table.setRound(Table.ROUND.INTERIM);
                    int bestHand = 10;
                    for(int i = 0; i < players.length; i++){
                        if (players[i].bestHand(table.getCommonCards().toArray(new Card[0])) < bestHand) {
                            bestHand = players[i].bestHand(table.getCommonCards().toArray(new Card[0]));
                            table.setWinner(players[i]);
                        }
                    }
                    break;
            }
            if(!(table.getRound() == Table.ROUND.INTERIM)) {
                table.getPlayerBets().clear();
                table.setDealerIndex(table.getDealerIndex()+1);
                table.setCurrentPlayerIndex(table.getDealerIndex() + 1);
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                0, 0),this));
            }
        } else {

            //In the preflop, if we have passed the blinds and players have received their cards
            //and we are still in the same stage (haven't passed dealer), start passing turns around until we get back to the dealer
            if (table.getRound() == Table.ROUND.PREFLOP && players[table.getCurrentPlayerIndex() % players.length].getHole()[0] != null && table.getCurrentPlayerIndex() > table.getDealerIndex() + 3 && table.getCurrentPlayerIndex() <= table.getDealerIndex() + players.length) {
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                                table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0),this));

            }
            if (!(table.getRound() == Table.ROUND.PREFLOP || table.getRound() == Table.ROUND.INTERIM)) {
                if (!table.getPlayerBets().containsKey(players[(table.getCurrentPlayerIndex() - 1) % players.length]) || table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]) == 0) {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                    0, 0),this));
                } else {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                                    table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0),this));
                }
            }
        }


    }

    public Turn handleTurnNotification(TurnNotification t) {
        if(currentTurn != null){
            Turn temp = currentTurn;
            currentTurn = null;
            return temp;
        } else {
            currentTurnNotification = t;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return handleTurnNotification(t);
        }
    }

}