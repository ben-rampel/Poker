'use strict';
var hostname = "localhost";
var app = angular.module('poker', ['ngStomp']);
app.controller('controller', function ($scope, $stomp, $log, $http) {
    let $ctrl = this;
    $ctrl.raiseBet = 0;

    $ctrl.getCardUrl = function (rank, suit) {
        var rankMap = new Map();
        rankMap.set("two", 2);
        rankMap.set("three", 3);
        rankMap.set("four", 4);
        rankMap.set("five", 5);
        rankMap.set("six", 6);
        rankMap.set("seven", 7);
        rankMap.set("eight", 8);
        rankMap.set("nine", 9);
        rankMap.set("ten", 10);
        rankMap.set("jack", "J");
        rankMap.set("queen", "Q");
        rankMap.set("king", "K");
        rankMap.set("ace", "A");

        var suitMap = new Map();
        suitMap.set("hearts", "h");
        suitMap.set("clubs", "c");
        suitMap.set("spades", "s");
        suitMap.set("diamonds", "d");

        return "resources/cards/" + rankMap.get(rank) + suitMap.get(suit) + ".png";
    };

    $scope.player = "No one";
    $scope.currentGameData = {};

    let refreshBet = function () {
        if ($scope.currentGameData.turnNotification != null) {
            if ($scope.currentGameData.turnNotification.requiredBet > 0) {
                $ctrl.bet = $scope.currentGameData.turnNotification.requiredBet;
            } else {
                $ctrl.bet = $scope.currentGameData.turnNotification.minimumBet;
            }
        }
        //$ctrl.raiseBet = $ctrl.bet;
    };

    refreshBet();
    $scope.submitTurn = function (action, bet) {
        if (action === "CHECK") {
            //send check message
            $stomp.send('/app/sendTurn', {
                message: {
                    "action": "CHECK",
                    "betAmount": 0,
                    "player": $scope.player
                }
            });
            console.log("Player checked");
        } else if (action === "CALL") {
            //send call message
            $stomp.send('/app/sendTurn', {
                message: {
                    "action": "CALL",
                    "betAmount": $ctrl.bet,
                    "player": $scope.player
                }
            });
            console.log("Player called " + $ctrl.bet);
        } else if (action === "RAISE") {
            //validate bet amount against player's chips
            let currentPlayer = $scope.currentGameData.players.find(obj => obj.name == $scope.player);

            let raiseValue = $("#raiseValue").val();
            console.log(currentPlayer.chips + " " + $scope.currentGameData.turnNotification.minimumBet + " " + raiseValue);

            //if (raiseValue <= currentPlayer.chips) {
            if(true) {
                if ($scope.currentGameData.turnNotification.minimumBet === 0 || raiseValue > $scope.currentGameData.turnNotification.minimumBet) {
                    //send raise message
                    $stomp.send('/app/sendTurn', {
                        message: {
                            "action": "RAISE",
                            "betAmount": Number($ctrl.raiseBet),
                            "player": $scope.player
                        }
                    });
                    console.log("Player raised " + $ctrl.raiseBet);
                }
            }

        } else {
            //send fold message
            $stomp.send('/app/sendTurn', {
                message: {
                    "action": "FOLD",
                    "betAmount": 0,
                    "player": $scope.player
                }
            });
            console.log("Player folded");
        }
    };


    $stomp.setDebug(function (args) {
        $log.debug(args)
    });

    $scope.displayWinner = false;
    $scope.connectFunc = function () {

        console.log($scope.player);
        $http.post('http://' + hostname + ':8080/login', $scope.player)
            .then(
                function(data){
                    //successful login
                    console.log(data);
                    $stomp
                        .connect('http://' + hostname + ':8080/socket')
                        // frame = CONNECTED headers
                        .then(function (frame) {
                            var url = '/poker/' + $scope.player;
                            var subscription = $stomp.subscribe(url, function (payload, headers, res) {
                                $scope.currentGameData = payload;
                                refreshBet();
                                if ($scope.currentGameData.winner != null && $scope.displayWinner === false) {
                                    $scope.displayWinner = true;
                                    //show winner
                                    $('#winnerText').text($scope.currentGameData.winnerInfo);
                                    $('#winnerModal').modal('show');
                                } else {
                                    $scope.displayWinner = false;
                                }
                                $scope.$apply();
                                console.log(payload);
                            })
                            var errorSubscription = $stomp.subscribe(url + "/error", function (payload, headers, res) {
                                $('#errorText').text(payload);
                                $('#errorModal').modal('show');
                            });
                        })
                },
                function(data){
                    //bad login
                    $('#usernameSelect').modal('show');
                    $('#usernameWarning').show();
                    return;
                });
    };

    $('#usernameSelect').modal('show')

});

function loadCSS() {
    let head = document.getElementsByTagName("HEAD")[0];
    let cssURL = URL.createObjectURL(document.getElementById("css-file").files[0]);
    let link = document.createElement("link");
    link.rel = "stylesheet";
    link.type = "text/css";
    link.href = cssURL;
    head.appendChild(link);
}