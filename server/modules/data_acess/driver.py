import datetime
import pyodbc
import platform
import sys, os


from modules.helpers import config

server = 'LAPTOP-8VIIT720\\POMAN'
database = 'store01'
username = 'pomanjr'
password = 'pomanjr'
driver = '{ODBC Driver 17 for SQL Server}'
class Connector:
    __connection = None
    __lastUsed = None
            
    def establishConnection():        
        if not Connector.__connection or datetime.datetime.now() - Connector.__lastUsed > datetime.timedelta(minutes = 15):
            Connector.__connection = pyodbc.connect(
                f'DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password}',
    timeout=5
            )
            Connector.__lastUsed = datetime.datetime.now()
            print(config.Config.getValue("dbname"))
        return Connector.__connection
    
    __backgroudConnection = None
    __backgroundLastTimeEstablished = None
    def establishBackgroundConnection():
        if not Connector.__backgroudConnection or datetime.datetime.now() - Connector.__backgroundLastTimeEstablished > datetime.timedelta(minutes = 15):
            Connector.__connection = pyodbc.connect(
                f'DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password}',
    timeout=5
            )
            Connector.__backgroundLastTimeEstablished = datetime.datetime.now()
        return Connector.__backgroudConnection