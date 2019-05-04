import requests
BASE_URL = ""


def check_user(user_id):
	pass


def add_user(user_id):
	pass


def get_currency(user_id):
	"""NEEDS TO BE IMPLEMENTED"""
	if not check_user(user_id): add_user(user_id)
	return 1 


def update_currency(user_id):
	if not check_user(user_id): add_user(user_id)
	pass


def create_new_user(user_id):
	if not check_user(user_id): add_user(user_id)
	pass


def get_ranking(user_id, game_id):
	if not check_user(user_id): add_user(user_id)
	return {"pos": 1, "total": 30}





