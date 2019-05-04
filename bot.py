import collector
from discord.ext import commands
from secrets import TOKEN


Narco = commands.Bot(command_prefix=">")
valid_games = {
	"poker": 0,
}


def verify_user(ctx, user):
	try:
		user = user[2:][:-1]
		try:
			user = int(user)
			user_name = ctx.guild.get_member(user)
			if user_name is None:
				return None
			else:
				return user_name

		except ValueError:
			return None

	except IndexError:
		return None


@Narco.command()
async def money(ctx, user=None):
	user_id = ctx.message.author.id
	if user:
		user_obj = verify_user(ctx, user)
		if user_obj:
			data = collector.get_currency(user_obj.id)
			await ctx.send(f"{user_obj.name} has {data}<:brampelbuck:546794299954561039>")
		else:
			await ctx.send(f"That's not a valid user <@{user_id}>")
	else:
		data = collector.get_currency(user_id)
		await ctx.send(f"{ctx.message.author.name} has {data}<:brampelbuck:546794299954561039>")


@Narco.command()
async def rank(ctx, game, user=None):
	user_id = ctx.message.id
	try: valid_games[game.lower()]
	except KeyError:
		await ctx.send(f"{game} is not a valid game.")
		return
	if user:
		user_obj = verify_user(ctx, user)
		if user_obj:
			data = collector.get_ranking(user_obj.id, game)
			await ctx.send(f"{user_obj.name} is {data['pos']} out of {data['total']} in {game}")
		else:
			await ctx.send(f"That's not a valid user <@{user_id}>")
	else:
		data = collector.get_ranking(user_id, game)
		await ctx.send(f"{ctx.message.author.name} is {data['pos']} out of {data['total']} in {game}")


@rank.error
async def rank_error(ctx, error):
	if ctx.command.qualified_name in "rank":
		await ctx.send(f"<@{ctx.message.author.id}> Missing Game Name!")


@Narco.command()
async def join(ctx):
	# TODO ADD PASSWORD SUPPORT
	user_id = ctx.message.author.id
	if collector.check_user(user_id): await ctx.send("You've already joined!")
	else:
		collector.add_user(user_id)
		await ctx.send(f"<@{user_id}>You've been added!")

Narco.run(TOKEN)
