from modules.data_acess.driver import Connector
import threading, time, datetime


__pool = {
    
}

__locked = False
__cachingTime = 60 * 30

def __summaryOfSharedInfo(cartid):
    query = '''
        select sc.cart_id, sc.cart_holder, sc.numbers_of_members, sc.total_price, sc.created_at, users.full_name as holdername, users.avatar as holderavatar, car_tname, total_items
        from (select * from shared_cart where cart_id  = %s) sc
        join users on (
            user_id = sc.cart_holder
        )
    '''
    
    cursor = Connector.establishConnection().cursor()
    # generalInfoRow = cursor.execute(query, (cartid, )).fetchone()
    cursor.execute(query, (cartid, ))
    generalInfoRow = cursor.fetchone()
    
    query = '''
        select cart_id, member_id, product_id, size_id, quantity from shared_cart_details where cart_id = %s
    '''
    
    # itemsInfoRows = cursor.execute(query, (cartid, )).fetchall()
    cursor.execute(query, (cartid, ))
    itemsInfoRows = cursor.fetchall()
    
    return {
        "id": generalInfoRow[0],
        "cartholder": {
            "id": generalInfoRow[1],
            "name": generalInfoRow[5],
            "avatar": generalInfoRow[6]
        },
        "numbersOfMembers": generalInfoRow[2],
        "totalprice": generalInfoRow[3],
        "createdAt": datetime.datetime.strftime(generalInfoRow[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfoRow[4] else None,
        "cartname": generalInfoRow[7],
        "totalitems": generalInfoRow[8],
        "items": {
            f"{row[2]}": {
                "sizeid": row[3],
                "quantity": row[4],
            } for row in itemsInfoRows
        }
    }
    
def AddToSharedCart(cartid, user_id, productid, sizeid, quantity):
    pass

def GetCart(cartid):
    global __pool
    if cartid not in __pool:
        __pool[cartid] = __summaryOfSharedInfo(cartid)
    __pool[cartid]['timeout'] = datetime.datetime.now() + __cachingTime
    return __pool[cartid]

def __bg(timeout = 60 * 60):
    removed = [key for key, val in __pool.items() if val['timeout'] < datetime.datetime.now().timestamp()]
    for key in removed:
        del __pool[key]
    threading.Timer(timeout, __bg).start()

__bg()