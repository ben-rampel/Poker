<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style type="text/css">
	.table {
		height:300px;
		width: 700px;
		background: green;
		border-radius: 60px;
		margin-left: 300px;
		margin-top:120px;
	}
	.player {
		height:100px;
		width:100px;
		border-radius: 100px;
		background: grey;
		position: absolute;
		text-align: center;
	}
	.playerLabel {
		margin-top: 30px;
		margin-bottom: 0px;
	}
	.playerChipsLabel {
		margin-top:0px;
	}
	.commonCards {
		position: absolute;
		margin-top:130px;
		margin-left:315px;
	}
	.potAmount {
		position: absolute;
		margin-top:160px;
		margin-left:315px;
	}
	.dashboard {
		left:0;
		top:0;
		position: fixed;
		width:170px;
		height:100%;
		background: gray;
		padding-left:30px
	}
	#dealerbutton {
		background: blue;
		width:20px;
		height: 20px;
		border-radius: 20px;
		top: -100px;
		position:relative;
	}
</style>
<body>
	<div class="game">
	<div class="dashboard">
	<p>Your Cards:</p>
		<span class="card">6s</span>
		<span class="card">7s</span>
	<div style="padding-top:40px;">
		<button>Fold</button>
		<button>Check</button>
		<button>Bet <input type="text" style="width:40px;"> chips</button>
	</div>
	</div>
	<div class="table">
		<div class="player" style="margin-left: -50px;margin-top:100px">
			<p class="playerLabel">You</p>
			<p class="playerChipsLabel">248 chips</p>
		</div>
		<div class="player" style="margin-top: 250px; margin-left:167px">
			<p class="playerLabel">Player 2</p>
			<p class="playerChipsLabel">249 chips</p>
			<div id="dealerButton"></div>
		</div>
		<div class="player" style="margin-top: 250px; margin-left:434px">
			<p class="playerLabel">Player 3</p>
			<p class="playerChipsLabel">248 chips</p>
		</div>
		<div class="player" style="margin-top: -50px; margin-left:167px">
			<p class="playerLabel">Player 4</p>
			<p class="playerChipsLabel">248 chips</p>
		</div>
		<div class="player" style="margin-top: -50; margin-left: 434px">
			<p class="playerLabel">Player 5</p>
			<p class="playerChipsLabel">248 chips</p>
		</div>
		<div class="player" style="margin-left: 650px;margin-top: 100px">
			<p class="playerLabel">Player 6</p>
			<p class="playerChipsLabel">248 chips</p>
		</div>
		<div class ="commonCards">
			<span class="card">
				As
			</span>
			<span class="card">
				Ah
			</span>
			<span class="card">
				6d
			</span>						
		</div>

		<div class ="potAmount">
			<span>${potSize}</span>
		</div>
	</div>
	</div>
</body>
</html>