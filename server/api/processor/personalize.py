from modules.data_acess.driver import Connector
from .products import GetProductsByList_2

def GetWishList(user_id, page = None):
    query = 'select product_id from wishlist where user_id = %s'
    cursor = Connector.establishConnection().cursor()
    # rows = cursor.execute(query, (user_id,)).fetchall()
    cursor.execute(query, (user_id,))
    rows = cursor.fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList_2(ids, page)

def AddToUserWishList(user_id, productid):
    query = 'insert into wishlist (user_id, product_id) values (%s, %s)'
    cursor = Connector.establishConnection().cursor()
    
    try:
        cursor.execute(query, (user_id, productid))
        cursor.commit()
    except:
        return { "message": f"{productid} is already in user's wishlist!" }
    
    return { "message": "Done!" }

def RemoveFromUserWishList(user_id, productid):
    query = 'delete from wishlist where user_id = %s and product_id = %s'
    cursor = Connector.establishConnection().cursor()
    
    try:
        cursor.execute(query, (user_id, productid))
        cursor.commit()
    except:
        return { "message": f"{productid} has not added to user's wishlist!" }
    
    return { "message": "Removed!" }
    
def UpdateUserWishList(user_id, wishlist):
    query = 'delete from wishlist where user_id = %s'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (user_id, ))
    cursor.commit()
    query = 'insert into wishlist (user_id, product_id) values (%s, %s)'
    cursor.executemany(query, tuple((user_id, item) for item in wishlist))
    cursor.commit()
    
    return {
        'message': "All done!"
    }