from modules.data_acess.driver import Connector
from modules.helpers.config import Config
import platform

try:
    import pyjwt
except:
    import jwt as pyjwt

import datetime

import threading

nameMapping = None

def __backgroundUpdateThread(timeout=10):
    query = 'select user_id, full_name from users'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    global nameMapping
    nameMapping = {row[0]: row[1] for row in rows}
    threading.Timer(timeout, __backgroundUpdateThread).start()

__backgroundUpdateThread()

class Verifier:
    def decode(token):
        return pyjwt.decode(token, 'app-secret-key', algorithms=['HS256'])

    def safeDecode(token):
        try: return Verifier.decode(token)
        except: pass
        return {}

    def verify(**kwargs):
        try: 
            pyjwt.decode(kwargs['token'], 'app-secret-key', algorithms=['HS256'])
        except: return False
        return True

    def generateToken(**kwargs):
        return pyjwt.encode(kwargs['payload'], 'app-secret-key', algorithm='HS256')        

class Authenticator:
    def Login(**kwargs):
        user_id = kwargs.get("userid", "Unknown")
        full_name = kwargs.get("fullname", "Unknown")
        email = kwargs.get("email", "Unknown")
        phone = kwargs.get("phone", "Unknown")
        token = kwargs.get("token", "Unknown")
        avatar = kwargs.get("avatar", "")

        if not Authenticator.verify(user_id, token):
            return {
                "error": "invalid token"
            }
        
        cursor = Connector.establishConnection().cursor()
        query = "select * from users where user_id = %s"
        cursor.execute(query, user_id)
        row = cursor.fetchone()
        if not row:
            query = "insert into users (user_id, full_name, email, phone, address, avatar) values (%s, %s, %s, %s, %s, %s)"
            cursor.execute(query, (user_id, full_name, email, phone, "", avatar))
            # cursor.commit()

        return {
            "message": "Chào mừng đến với See!",
            "access_token": Verifier.generateToken (
                payload = {
                    "user_id": user_id,
                    "email": email,
                    "phone": phone
                }
            )
        }

    def verify(user_id, token):
        return True