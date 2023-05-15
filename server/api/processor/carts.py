from modules.data_acess.driver import Connector
from .misc import UUID
import random, threading, datetime, traceback, time
from .products import categoryMapping, priceMapping
import pymysql
import traceback

MOVE_DATA_SYNCING_TO_BACKGROUND = False

connection = Connector.establishConnection()
    
def ProcessFetchCartsList(user_id):
    query = "select cart_id, total_price from cart where cart_holder = %s and opening = 1"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (user_id,)).fetchone()
    
    if not row:
        cartid = UUID()
        query = "insert into cart (cart_id, cart_holder, opening) values (%s, %s, 1)"
        cursor.execute(query, (cartid, user_id))
        cursor.commit()
        
        return {
            'total-price': 0,
            'details': []
        }
    
    total_price = row[1]
    cartid = row[0]
    
    query = "select cd.product_id, cd.size_id, cd.quantity, p.title, p.descriptions, p.price, p.sex_id, p.category_id, p.image_url from (select product_id, size_id, quantity from cart_detail c where c.cart_id = %s) cd left join product p on cd.product_id = p.id"
    rows = cursor.execute(query, (cartid,)).fetchall()
    
    return {
        'total-price': total_price,
        'details': [{
            'size': row[1],
            'quantity': row[2],
            'product': {
                'id': row[0],
                'title': row[3],
                'description': row[4],
                'price': row[5],
                'sex': row[6],
                'category': row[7],
                'images': [row[8]]
            }
        } for row in rows]
    }
    
__backgroundQueue = [] # queries
__backgroundQueueTMP = []
__backgroundProcessing = False
__defaultQueries = [
    'delete from shared_cart_details where quantity <= 0',
    'delete from cart_detail where quantity <= 0',
    'call UpdateTotalItems_SharedCart',
    'call CounterHacker'
]
    
def __background(timeout = 0.2):
    global __backgroundProcessing, __defaultQueries, __backgroundQueueTMP, __backgroundQueue

    while True:
        try:
            if len(__backgroundQueue) != 0:
                __backgroundProcessing = True
                conn = pymysql.connect(host="containers-us-west-117.railway.app", user="root", password="2QzH6B5DAP3vW8eEqTnv", database="railway", port=5930)
                cursor = conn.cursor()
                # conn = Connector.establishBackgroundConnection()
                # cursor = conn.cursor()
                print("Test:: ", conn)

                for query in __backgroundQueue:
                    try:
                        cursor.execute(query[0], query[1])
                        conn.commit()
                    except Exception as e:
                        print(e)
                        traceback.print_exc()
                        conn.rollback()
                
                for query in __defaultQueries:
                    try:
                        cursor.execute(query)
                        conn.commit()
                    except Exception as e:
                        print(f'[ERROR] Background processing failed - {query}')
                        print(e)
                        traceback.print_exc()
                        cursor.rollback()

                __backgroundQueue = __backgroundQueueTMP
                __backgroundQueueTMP.clear()

                __backgroundProcessing = False
        except:
            print('[ERROR] Background processing failed')
            traceback.print_exc()
            __backgroundProcessing = False
            __backgroundQueue += __backgroundQueueTMP
            __backgroundQueueTMP.clear()
        
        time.sleep(timeout)
        
threading.Thread(target = __background, daemon = True).start()

def __updateTotalPriceBG(cartid):
    # pass the cursor as params due to threading issue
    query = '''
        update cart set total_price = (
            select sum(cd.quantity * p.price) as s from ( 
                select product_id, quantity from cart_detail where cart_id = %s
            ) cd left join product p on cd.product_id = p.id
        ) where cart_id = %s
    '''
    
    if MOVE_DATA_SYNCING_TO_BACKGROUND:
        if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, cartid)])
        else: __backgroundQueueTMP.append([query, (cartid, cartid)])
    else:
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query, (cartid, cartid))
        # cursor.commit()
    
def __updateSharedCartBG(cartid):
    query = '''
        update shared_cart set total_price = (
            select COALESCE(sum(quantity * price), 0) from (
                    select *
                    from shared_cart_details 
                    where cart_id = %s
                ) sc join product p on (p.id = sc.product_id)
        ), numbers_of_members = (
            select count(*) from shared_cart_member where cart_id = %s
        ) where cart_id = %s
    '''

    if MOVE_DATA_SYNCING_TO_BACKGROUND:
        if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, cartid, cartid)])
        else: __backgroundQueueTMP.append([query, (cartid, cartid, cartid)])
    else:
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query, (cartid, cartid, cartid))
        # cursor.commit()
    
def __pushLogsBG(cartid, note):
    query = 'insert into shared_cart_history(cart_id, note) values (%s, %s)'

    if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, note)])
    else: __backgroundQueue.append([query, (cartid, note)])
    
def AddToCart(user_id, product_id, sizeid, quantity, cartids = ['*']):

    if quantity == 0:
        return {"message": "Quantity cannot be zero"}

    errors = []
    
    cursor = Connector.establishConnection().cursor()
    
    from modules.authentication.authenticator import nameMapping
    username = nameMapping[user_id]

    for row in cartids:
        if row == '*': continue
        query = "select count(*) as tmp from shared_cart_member where cart_id = %s and member_id = %s"
        # current = cursor.execute(query, (row, user_id, )).fetchone()
        cursor.execute(query, (row, user_id, ))
        current = cursor.fetchone()
        
        if not current:
            errors += 'Error occur while adding new item to the cart ' + row
            continue
                
        query = "select * from shared_cart_details where cart_id = %s and member_id = %s and product_id = %s and size_id = %s"
        # current = cursor.execute(query, (row, user_id, product_id, sizeid)).fetchone()
        cursor.execute(query, (row, user_id, product_id, sizeid))
        current = cursor.fetchone()
        
        if not current:
            query = 'insert into shared_cart_details (cart_id, member_id, product_id, size_id, quantity) values (%s, %s, %s, %s, %s)'
            cursor.execute(query, (row, user_id, product_id, sizeid, quantity))
        else:
            query = '''
                    UPDATE shared_cart_details 
                        SET quantity = (
                            SELECT MAX(a) 
                            FROM (
                                SELECT quantity + %s AS a
                                    UNION ALL
                                SELECT 0
                             ) AS tmptable
                        ) 
                    WHERE cart_id = %s AND member_id = %s 
                    AND product_id = %s AND size_id = %s
            '''

            # print(quantity, row, user_id, product_id, sizeid)
            cursor.execute(
                query,
                (quantity, row, user_id, product_id, sizeid)
            )

        __updateSharedCartBG(row)

        if quantity != -1000: __pushLogsBG(row, f'{username} đã {"thêm" if quantity > 0 else "bỏ"} {quantity if quantity > 0 else -quantity} sản phẩm có mã {product_id} ({categoryMapping[product_id]}) size {sizeid} {"vào" if quantity > 0 else "ra khỏi"} giỏ hàng')
        else: __pushLogsBG(row, f'{username} đã ném sản phẩm có mã {product_id} ({categoryMapping[product_id]}) size {sizeid} ra khỏi giỏ hàng')
    
    if '*' in cartids:
        query = "select cart_id from cart where cart_holder = %s and opening = 1"
       
        # row = cursor.execute(query, (user_id,)).fetchone()
        cursor.execute(query, (user_id,))
        row = cursor.fetchone()
        
        if not row:
            cartid = UUID()
            query = "insert into cart (cart_id, cart_holder, opening) values (%s, %s, 1)"
            cursor.execute(query, (cartid, user_id))
            cursor.commit()
        else: 
            cartid = row[0]
        
        query = "select quantity from cart_detail where cart_id = %s and product_id = %s and size_id = %s"
        # row = cursor.execute(query, (cartid, product_id, sizeid)).fetchone()
        cursor.execute(query, (cartid, product_id, sizeid))
        row = cursor.fetchone()
        
        if not row:
            query = "insert into cart_detail (cart_id, product_id, size_id, quantity) values (%s, %s, %s, %s)"
            cursor.execute(query, (cartid, product_id, sizeid, quantity))
        else:
            if row[0] + quantity <= 0: query = "delete from cart_detail where cart_id = %s and product_id =  %s and size_id =  %s"
            else: query = "update cart_detail set quantity =  %s where cart_id =  %s and product_id =  %s and size_id =  %s"
            cursor.execute(query, (
                row[0] + quantity, cartid, product_id, sizeid
            ) if row[0] + quantity > 0 else (cartid, product_id, sizeid))
        
        connection.commit()
        
        __updateTotalPriceBG(cartid)
    
    res = {"message": "done!"}
    if len(errors) != 0:
        res['errors'] = errors
        
    return res

def __summaryOfSharedInfo(cartid):
    query = '''select sc.cart_id, sc.cart_holder, sc.numbers_of_members, sc.total_price, sc.created_at, users.full_name as holder_name, users.avatar as holder_avatar, cart_name, total_items
        from (select * from shared_cart where cart_id  = %s) sc
        join users on (
            user_id = sc.cart_holder
        )
    '''
    
    # row = Connector.establishConnection().cursor().execute(query, (cartid, )).fetchone()
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (cartid, ))
    row = cursor.fetchone()

    return {
        "id": row[0],
        "cartholder": {
            "id": row[1],
            "name": row[5],
            "avatar": row[6]
        },
        "members": row[2],
        "totalprice": row[3],
        "createdAt": datetime.datetime.strftime(row[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[4] else None,
        "cartname": row[7],
        "totalitems": row[8]
    }

def ProcessMakeSharedCart(user_id, cartname):
    cartid = UUID()
    query = "insert into shared_cart (cart_id, cart_holder, opening, total_price, numbers_of_members, created_at, cart_name) values (%s, %s, 1, 0, 1, %s, %s)"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (cartid, user_id, datetime.datetime.now(), cartname))
    # cursor.commit()
    
    query = "insert into shared_cart_member (cart_id, member_id) values (%s, %s)"
    cursor.execute(query, (cartid, user_id))
    # cursor.commit()
    
    return {
        "id": cartid,
        "info": __summaryOfSharedInfo(cartid)
    }
    
def ProcessJoinSharedCart(cartid, user_id):
    cursor = Connector.establishConnection().cursor()
    query = "select cart_id from shared_cart where cart_id = %s and opening = 1"
    # row = cursor.execute(query, (cartid, )).fetchone()
    cursor.execute(query, (cartid, ))
    row = cursor.fetchone()

    if not row:
        return { "message": "shared cart not found" }
    
    query = "select * from shared_cart_member where member_id = %s and cart_id = %s"
    # row = cursor.execute(query, (user_id, cartid)).fetchone()
    cursor.execute(query, (user_id, cartid))
    cursor.fetchone()
    if row:
        return { 
            "message": "you are already in this shared cart",
            "info": __summaryOfSharedInfo(cartid)
        }
    
    try:
        query = "insert into shared_cart_member (cart_id, member_id) values (%s, %s)"
        cursor.execute(query, (cartid, user_id))
        # cursor.commit()
        threading.Thread(target=__updateSharedCartBG, args = (cartid, ), daemon = True).start()
    except: return {"message": "Something went wrong"}
    
    return { 
        "message": "joined!",
        "info": __summaryOfSharedInfo(cartid)
    }

def GetSharedCartInfo(memberid, code):
    query = '''
        select sc.cart_id, shared_cart.cart_holder, shared_cart.opening, shared_cart.total_price, shared_cart.numbers_of_members, shared_cart.created_at, shared_cart.cart_name
        from (select cart_id from shared_cart_member where member_id = %s and cart_id = %s) as sc 
        left join shared_cart on (sc.cart_id = shared_cart.cart_id)
    '''
    
    cursor = Connector.establishConnection().cursor()
    # row = cursor.execute(query, (memberid, code, )).fetchone()
    cursor.execute(query, (memberid, code, ))
    row = cursor.fetchone()
    
    if not row:
        raise Exception("Cart not found")
    
    return {
        "id": row[0],
        "cartholder": row[1],
        "opening": row[2],
        "totalprice": row[3],
        "members": row[4],
        "createdat": row[5],
        "cartname": row[6],
    }
    
def ProcessSavePersonalCart(user_id, data):
    query = 'delete from cart_detail where cart_id = (select cart_id from cart where cart_holder = %s and opening = 1)'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (user_id, ))
    # cursor.commit()
    connection.commit()
    query = "select cart_id, opening from cart where cart_holder = %s and opening = 1"
    # row = cursor.execute(query, (user_id, )).fetchone()
    cursor.execute(query, (user_id, ))
    row = cursor.fetchone()
    
    cartid = None
    if not row:
        newCartId = UUID()
        query = "insert into cart (cart_id, cart_holder) values (%s, %s)"
        cursor.execute(query, (newCartId, user_id))
        cartid = newCartId
    else: 
        cartid = row[0]

    query = 'insert into cart_detail (cart_id, product_id, size_id, quantity) values (%s, %s, %s, %s)'

    if len(data) != 0:
        cursor.executemany(query, tuple((cartid, item['product_id'], item['size'], item['quantity']) for item in data))
        cursor.commit()
        threading.Thread(target=__updateTotalPriceBG, args = (cartid, ), daemon = True).start()
    
    return { "message": "saved!" }

def ProcessGetMyCart(user_id):
    query = "select cart_id, total_price  from cart where cart_holder = %s and opening = 1"
    cursor = Connector.establishConnection().cursor()
    # row = cursor.execute(query, (user_id,)).fetchone()
    cursor.execute(query, (user_id,))
    row = cursor.fetchone()
    
    if not row:
        cartid = UUID()
        query = "insert into cart (cart_id, cart_holder, opening) values (%s, %s, 1)"
        cursor.execute(query, (cartid, user_id))
        # cursor.commit()
        
        return {
            'total-price': 0,
            'details': []
        }
    
    total_price = row[1]
    cartid = row[0]
    
    query = "select cd.product_id, cd.size_id, cd.quantity, p.title, p.descriptions, p.price, p.sex_id, p.category_id, p.image_url from (select product_id, size_id, quantity from cart_detail c where c.cart_id = %s) cd left join product p on cd.product_id = p.id"
    # rows = cursor.execute(query, (cartid,)).fetchall()
    cursor.execute(query, (cartid,))
    rows = cursor.fetchall()
    
    return {
        'total-price': total_price,
        'details': [{
            'size': row[1],
            'quantity': row[2],
            'product': {
                'id': row[0],
                'title': row[3],
                'description': row[4],
                'price': row[5],
                'sex': row[6],
                'category': row[7],
                'images': [row[8]]
            }
        } for row in rows]
    }
    
def ProcessGetMySharedCart(user_id):
    query = "select cart_id, total_price, numbers_of_members, created_at, cart_name, total_items from shared_cart where cart_holder = %s and opening = 1"
    cursor = Connector.establishConnection().cursor()
    # rows = cursor.execute(query, (user_id,)).fetchall()
    cursor.execute(query, (user_id,))
    rows = cursor.fetchall()
    return {
        "shared-carts": [
            {
                "id": row[0],
                "totalprice": row[1],
                "members": row[2],
                "createdat": datetime.datetime.strftime(row[3] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[3] else None,
                "cartname": row[4],
                "totalitems": row[5] if row[5] is not None else 0
            } for row in rows
        ]
    }

def ProcessGetGetMyJoinedCart(user_id):
    query = ''' select cart_id, cart_holder, total_price, numbers_of_members, created_at, full_name, u.avatar, cc.cart_name, cc.total_items  from (
        select sc.cart_id, c.cart_holder, c.opening, c.total_price, c.numbers_of_members, c.created_at, cart_name, total_items
        from (select cart_id from shared_cart_member where member_id = %s) as sc 
        join (select * from shared_cart where opening = 1) c on (sc.cart_id = c.cart_id and c.cart_holder != %s)
    ) cc left join users u on (user_id = cc.cart_holder)
    '''
    
    cursor = Connector.establishConnection().cursor()
    # rows = cursor.execute(query, (user_id, user_id, )).fetchall()
    cursor.execute(query, (user_id, user_id, ))
    rows = cursor.fetchall()

    if not __backgroundProcessing:
        __backgroundQueue.append(['select * from size', ( )])
    else: __backgroundQueueTMP.append(['select * from size', ( )])
    
    return {
        "joined-carts": [
            {
                "id": row[0],
                "cartholder": row[1],
                "totalprice": row[2],
                "members": row[3],
                "createdat": datetime.datetime.strftime(row[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[4] else None,
                "cartholder": row[5],
                "cartholderavatar": row[6],
                "cartname": row[7],
                "totalitems": row[8]
            } for row in rows
        ]
    }
    
def PersonalSharedListInfo(user_id):
    query = '''select sc.cart_id, cart_holder, member_id, cart_name from shared_cart sc join (select * from shared_cart_member where member_id = %s) scm on ( 
        sc.cart_id = scm.cart_id
    ) where opening = 1'''
    
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (user_id, ))
    rows = cursor.fetchall()
    # rows = cursor.execute(query, (user_id, )).fetchall()
    print(rows)
    
    return {
        "shared": [
            {
                "cartid": row[0],
                "cartname": row[3],
            }
            for row in rows if row[1] == row[2]  
        ],
        "joined": [
            {
                "cartid": row[0],
                "cartname": row[3],
            }
            for row in rows if row[2] != row[1]
        ]
    }
    
def ProcessGetSharedCartInfo(cartid, user_id):
    query = "select * from shared_cart_member where cart_id = %s and member_id = %s"
    cursor = Connector.establishConnection().cursor()
    # row = cursor.execute(query, (cartid, user_id, )).fetchone()
    cursor.execute(query, (cartid, user_id, ))
    row = cursor.fetchone()

    if not row:
        raise Exception("Cart not found")
    
    # query = '''
    #                 UPDATE shared_cart_details 
    #                     SET quantity = (
    #                         SELECT MAX(a) 
    #                         FROM (
    #                             SELECT quantity + %s AS a
    #                                 UNION ALL
    #                             SELECT 0
    #                          ) AS tmptable
    #                     ) 
    #                 WHERE cart_id = %s AND member_id = %s 
    #                 AND product_id = %s AND size_id = %s
    #         '''
    
    query = '''
        select cart_id, product_id, size_id, COALESCE(sum(quantity), 0), title, descriptions, price, sex_id, category_id, image_url
        from (select * from shared_cart_details where cart_id = %s) scm join product p 
        on ( p.id = scm.product_id )
        group by cart_id, product_id, size_id, title, descriptions, price, sex_id, category_id, image_url
    '''
    # pymysql.err.OperationalError: (3819, "Check constraint 'shared_cart_details_chk_1' is violated.")
    
    # rows = cursor.execute(query, (cartid, )).fetchall()
    cursor.execute(query, (cartid, ))
    rows = cursor.fetchall()
    
    res = { }
    
    res["items"] = [
        {
            "sizeid": row[2],
            "quantity": row[3],
            "product": {
                "id": row[1],
                "title": row[4],
                "description": row[5],
                "price": row[6],
                "sex": row[7],
                "category": row[8],
                "images": [row[9]]
            }
        } for row in rows
    ]
    
    res["info"] = __summaryOfSharedInfo(cartid)
    
    res['info']['totalprice'] = sum(row[3] * priceMapping[row[1]] for row in rows)
    res['info']['totalitems'] = sum(row[3] for row in rows)
    
    # res["logs"] = [
    #     line[0] for line in cursor.execute('select note from shared_cart_history where cart_id = %s', (cartid, )).fetchall()
    # ]
    cursor.execute('SELECT note FROM shared_cart_history WHERE cart_id = %s', (cartid,))
    rows = cursor.fetchall()
    logs = [row[0] for row in rows]
    res['logs'] = logs

    
    return res