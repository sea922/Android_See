import pymysql
import platform
import sys, os
from modules.helpers import config
import datetime

server = 'containers-us-west-117.railway.app'
database = 'railway'
port = 5930
username = 'root'
password = '2QzH6B5DAP3vW8eEqTnv'
driver = 'pymysql'

# server = 'localhost'
# database = 'company'
# port = ""
# username = 'root'
# password = ''
# driver = 'pymysql'

class Connector:
    __connection = None
    __lastUsed = None
            
    def establishConnection():        
        if not Connector.__connection or datetime.datetime.now() - Connector.__lastUsed > datetime.timedelta(minutes = 15):
            Connector.__connection = pymysql.connect(
                host=server,
                database=database,
                user=username,
                password=password,
                port=port,
                autocommit=True,
            )
            Connector.__lastUsed = datetime.datetime.now()
            print(config.Config.getValue("dbname"))
        return Connector.__connection
    
    __backgroudConnection = None
    __backgroundLastTimeEstablished = None
    def establishBackgroundConnection():
        if not Connector.__backgroudConnection or datetime.datetime.now() - Connector.__backgroundLastTimeEstablished > datetime.timedelta(minutes = 15):
            Connector.__connection = pymysql.connect(
                host=server,
                port=port,
                database=database,
                user=username,
                password=password,
                autocommit=True,
            )
            Connector.__backgroundLastTimeEstablished = datetime.datetime.now()
        return Connector.__backgroudConnection
