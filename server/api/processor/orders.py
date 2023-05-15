import random
from modules.email_service import gmail
import time, traceback, datetime
import threading

from modules.helpers import config
from modules.data_acess.driver import Connector

from .carts import GetSharedCartInfo

from .misc import StoresLocationJson, UUID, BranchInfo

connection = Connector.establishConnection()

__mailInstance = gmail.MailService()
# __mailInstance.login('hocviencanhsatnhandan290@gmail.com', config.Config.getValue('email-password'))

def ProcessSharedOrder(user_id, cartid, extraInfo):
    global __mailInstance
    
    preorder_required = False
    total_price = 0
    
    orderType = extraInfo.get('type', 0) # 0 --> delivery, 1 --> pickup
    
    customer_name, phone_number, location, email, branchid, date = extraInfo.get('customer_name' , None),\
        extraInfo.get('phone_number', None), extraInfo.get('location', None), \
        extraInfo.get('email', None), extraInfo.get('branchid', None), extraInfo.get('date', None)
        
    if date is not None:
        date = datetime.datetime.strptime(date, '%d-%m-%Y')
            
    if not phone_number or not email:
        return {"message": "User info should be provided"}
    
    if orderType == 0 and (not location):
        return {"message": "Location should be provided"}
    
    if orderType == 1 and (not branchid or not date):
        return {"message": "Branch id and Date should be provided for pickup order"}
    
    cursor = Connector.establishConnection().cursor()
    query = '''
    select member_id, product_id, size_id, quantity, p.title, p.price, p.image_url from
    (
        select member_id, product_id, size_id, quantity from shared_cart_details where cart_id = %s
    ) c join product p on (p.id = c.product_id)
    '''
    
    # rows = cursor.execute(query, (cartid, )).fetchall()
    cursor.execute(query, (cartid, ))
    rows = cursor.fetchall()
    ordersData = {}
    ids = set([])
    
    from modules.authentication.authenticator import nameMapping
    
    consumed = {}
    
    for row in rows:
        if row[0] not in ordersData:
            ordersData[row[0]] = {
                'username': nameMapping.get(row[0], 'Unknown'),
                'products': []
            }

        ordersData[row[0]]['products'].append({
            "productid": row[1],
            "size": row[2],
            "quantity": row[3],
            "price": row[5],
            "title": row[4],
            "image": row[6]
        })
        
        ids.add(row[1])
        total_price += row[5] * row[3]
        
        if row[0] not in consumed:
            consumed[row[1]] = {
                'S': 0,
                'M': 0,
                'L': 0,
                'XL': 0,
                'XXL': 0,
            }
        
        consumed[row[1]][row[2]] += row[3]

    if not len(ids):
        return {"message": "Cart is empty"}
    
    query = 'select product_id, size_id, quantity from inventory where product_id in ({})'.format(','.join([str(i) for i in ids]))
    # rows = cursor.execute(query).fetchall()
    cursor.execute(query)
    rows = cursor.fetchall()
    
    inventoriesData = {}
    
    for row in rows:
        if str(row[0]) not in inventoriesData:
            inventoriesData[str(row[0])] = {}
        inventoriesData[str(row[0])][row[1]] = row[2]
    
    updateTrendingQueryPattern = 'call UpdateTrending %s, %s'
    dropDownQuery = 'update inventory set quantity = (select max(a) from (values (quantity - %s), (0)) as tmptable(a)) where product_id = %s and size_id = %s'
    # Error
    for key, val in consumed.items():
        for size, quantity in val.items():
            if quantity != 0:
                cursor.execute(dropDownQuery, (quantity, key, size))
                cursor.execute(updateTrendingQueryPattern, (key, quantity))

    mailContentBase = '''
    <div class='main'>
        <div class="container">
            <p class="app__name">SEE STORE</p>
            <div class='divider'></div>
        </div>
    '''

    mailContentBase += gmail.mail_greeting(
        f'Cảm ơn quý khách hàng {customer_name} đã tin tưởng và sử dụng dịch vụ của chúng tôi! Đơn hàng chung của quý khách gồm có <b>{len(list(ordersData.keys()))} thành viên</b>, chi tiết như sau:'
    )
    
    if orderType == 1:
        branchInfo = BranchInfo(branchid)
        mailContentBase += gmail.html_mail_pickup_order(
            appointmentDate = date, 
            addressname = branchInfo['branch_name'], 
            address = branchInfo['address'],
        )
        
    mailContentBase += gmail.carts_info(
        items = ordersData
    )
    
    mailContentBase += gmail.hr0()
    delivered_on = datetime.datetime.now() + datetime.timedelta(days = 5)
    mailContentBase += gmail.ord_summary(
        price = 0 if orderType == 1 else total_price, 
        shipping_fee = 0 if orderType == 1 else 30000, 
        total = (total_price + 30000) if orderType == 0 else total_price, 
        extra_note = f'Đơn hàng của quý khách sẽ được xác nhận và giao hàng trước ngày <b>{delivered_on.strftime(r"%Y-%m-%d")}</b> đến địa chỉ <b>{location}</b> và liên lạc qua số điện thoại <b>{phone_number}</b>.' if orderType == 0\
            else 'Mọi sự thay đổi thông tin xin liên hệ lại với chúng tôi qua số điện thoại <b>1723 0098</b>'
    )
    
    mailContentBase += "</div>"
    
    mailContent = gmail.build_email_content(
        'truongvans1010@gmail.com', 
        [email], 
        subject = 'Đơn hàng của bạn đang trên đường vận chuyển!' if orderType == 0 else 'Lịch hẹn thử đồ!', 
        content = {'html': gmail.html_mail_2(content = mailContentBase)}
    )

    threading.Thread(target = __mailInstance.send_mail, args = (mailContent, ), daemon = True).start()
    
    query = 'update shared_cart set opening = 0 where cart_id = %s'
    cursor.execute(query, (cartid, ))
    
    if orderType == 0:
        query = 'insert into shared_orders(cart_id, customer_name, customer_phone, address, email) values(%s, %s, %s, %s, %s)'
        cursor.execute(query, (cartid, customer_name, phone_number, location, email))
        
    else:
        query = 'insert into shared_orders(cart_id, customer_name, customer_phone, email, order_type, branch_id, pickup_time) values(%s, %s, %s, %s, %s, %s, %s)'
        cursor.execute(query, (cartid, customer_name, phone_number, email, orderType, branchid, date))

    cursor.commit()
    return {
        "message": "Done!"
    }
    
def ProcessPersonalOrder(user_id, extraInfo):
    global __mailInstance

    preorder_required = False
    total_price = 0
    
    orderType = extraInfo.get('type', 0) # 0 --> delivery, 1 --> pickup
    
    customer_name, phone_number, location, email, branchid, date = extraInfo.get('customer_name' , None),\
        extraInfo.get('phone_number', None), extraInfo.get('location', None), \
        extraInfo.get('email', None), extraInfo.get('branchid', None), extraInfo.get('date', None)
        
    if date is not None:
        date = datetime.datetime.strptime(date, '%d-%m-%Y')
            
    if not phone_number or not email:
        return {"message": "User info should be provided"}
    
    if orderType == 0 and (not location):
        return {"message": "Location should be provided"}
    
    if orderType == 1 and (not branchid or not date):
        return {"message": "Branch id and Date should be provided for pickup order"}
    
    query = 'select cart_id from cart where cart_holder = %s and opening = 1'
    cursor = Connector.establishConnection().cursor()
    # cartid = cursor.execute(query, (user_id, )).fetchone()

    cursor.execute(query,(user_id, ))
    cartid = cursor.fetchone()    
    if not cartid:
        return {"message": "Cart is empty"}
    
    cartid = cartid[0]
    
    query = 'select cd.product_id, cd.quantity, cd.size_id, p.price, p.image_url, p.title from (select product_id, quantity, size_id from cart_detail where cart_id = %s) cd left join product p on (p.id = cd.product_id)'
    # rows = cursor.execute(query, (cartid, )).fetchall()
    cursor.execute(query,(cartid, ))
    rows = cursor.fetchall()  
    
    orderData = [{
        'productid': row[0],
        'quantity': row[1],
        'sizeid': row[2],
        'price': row[3],
        'imageurl': row[4],
        'title': row[5],
    } for row in rows]
    
    if len(orderData) == 0:
        return {'message': 'Cart is empty'}
    
    query = "select product_id, size_id, quantity from inventory where product_id in ({})".format(','.join([str(item['productid']) for item in orderData]))
    # rows = cursor.execute(query).fetchall()
    cursor.execute(query)
    rows = cursor.fetchall()  
    
    inventoriesData = {}
    
    for row in rows:
        if str(row[0]) not in inventoriesData:
            inventoriesData[str(row[0])] = {}
        inventoriesData[str(row[0])][row[1]] = row[2]
    
    updateTrendingQueryPattern = 'call UpdateTrending (%s, %s)'
    dropDownQuery = 'UPDATE inventory SET quantity = GREATEST(quantity - %s, 0) WHERE product_id = %s AND size_id = %s'
    
    for item in orderData:
        if inventoriesData[str(item['productid'])][item['sizeid']] < item['quantity']:
            preorder_required = True
        
        cursor.execute(dropDownQuery, (item['quantity'], item['productid'], item['sizeid']))
        total_price += item['quantity'] * item['price']

        print(item['productid'], item['quantity'])
        cursor.execute(updateTrendingQueryPattern, (item['productid'], item['quantity']))

    if orderType == 1:
        store = BranchInfo(branchid)
    
    mailContentBase = '''
        <div class='main'>
            <div class="container">
                <p class="app__name">SEE STORE</p>
                <div class='divider'></div>
            </div>
    '''

    mailContentBase += gmail.mail_greeting(
        f'Cảm ơn quý khách hàng <b>{customer_name}</b> đã tin tưởng và sử dụng dịch vụ của chúng tôi! Đơn hàng của quý khách gồm có <b>{len(orderData)} sản phẩm</b>, chi tiết như sau:' if orderType == 0 else \
        f'Cảm ơn quý khách hàng <b>{customer_name}</b> đã tin tưởng và sử dụng dịch vụ của chúng tôi! Sau đây là thông tin lịch hẹn thử đồ và thông tin chi tiết các sản phẩm quý khách đã chọn:'
    )
    
    if orderType == 1:
        mailContentBase += gmail.html_mail_pickup_order(
            appointmentDate = date,
            addressname = store['branch_name'],
            address = store['address']
        )
    
    mailContentBase += gmail.hr('Quý khách')
    mailContentBase += '''<div class="container">'''

    for item in orderData:
        mailContentBase += gmail.item_html_template(
            item['imageurl'],
            item['title'],
            item['price'],
            item['quantity'],
            item['sizeid']
        ) + '<br>\n'

    mailContentBase += '''</div>'''
    mailContentBase += gmail.hr0()
    delivered_on = datetime.datetime.now() + datetime.timedelta(days = 5)
    mailContentBase += gmail.ord_summary(
        price = 0 if orderType == 1 else total_price, 
        shipping_fee = 0 if orderType == 1 else 30000, 
        total = (total_price + 30000) if orderType == 0 else total_price, 
        extra_note = f'Đơn hàng của quý khách sẽ được xác nhận và giao hàng trước ngày <b>{delivered_on.strftime(r"%Y-%m-%d")}</b> đến địa chỉ <b>{location}</b> và liên lạc qua số điện thoại <b>{phone_number}</b>.' if orderType == 0\
            else 'Mọi sự thay đổi thông tin xin liên hệ với chúng tôi qua số điện thoại <b>1723 0098</b>. Chúc quý khách một ngày tốt lành!'
    )
    
    mailContent = gmail.build_email_content(
        'truongvansi.dev@gmail.com', 
        [email], 
        subject = 'Hurray! Đơn hàng của bạn đang trên đường vận chuyển!' if orderType == 0 else 'Hot, hot hot!! Lịch hẹn thử đồ!', 
        content = {'html': gmail.html_mail_2(content = mailContentBase)}
    )
    
    # threading.Thread(target = __mailInstance.send_mail, args = (mailContent, ), daemon = True).start()

    newCartId = UUID()
    
    cursor.execute('update cart set opening = 0 where cart_id = %s', (cartid, ))
    
    
    if orderType == 0:
        cursor.execute('insert into orders(cart_id, payment_status, deliver_status, order_type, customer_name, customer_phone, address, email) values (%s, %s, %s, %s, %s, %s, %s, %s)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email))
    else: 
        cursor.execute('insert into orders(cart_id, payment_status, deliver_status, order_type, customer_name, customer_phone, address, email, branch_id, pickup_time) values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email, branchid, date))
    cursor.execute('insert into cart(cart_id, cart_holder) values (%s, %s)', (newCartId, user_id))
    
    connection.commit()
    return {"message": "Done!"}




def GetSharedOrdersData(user_id):
    print(user_id)
    query = '''
        select s.cart_id, s.total_price, s.total_items, so.created_at, so.payment_status, so.deliver_status, order_type, branch_id, cart_name from (
            select scart.cart_id, scart.total_price, scart.total_items, scart.cart_name from (
                select * from shared_cart where opening = 0
            ) scart join (
                select * from shared_cart_member where member_id = %s
            ) smem on (smem.cart_id = scart.cart_id)
        ) s left join shared_orders so on (so.cart_id = s.cart_id)
        order by so.created_at desc
    '''
    
    cursor = Connector.establishConnection().cursor()
    # rows = cursor.execute(query, (user_id, )).fetchall()
    cursor.execute(query,(user_id, ))
    rows = cursor.fetchall()
    
    return {
        "orders": [
            {
                "orderid": row[0],
                "cartname": row[8],
                "totalprice": row[1],
                "totalitems": row[2], 
                "createdat": datetime.datetime.strftime(row[3] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[3] else None,
                "paymentstatus": row[4],
                "deliverstatus": row[5],
                "ordertype": 1 if row[6] else 0,
                "branchid": row[7]
            } for row in rows
        ]
    }


def GetPersonalOrdersData(user_id):
    query = '''select KK.cart_id, KK.total_price, count(cart_detail.product_id), KK.created_at, KK.payment_status, KK.deliver_status, order_type, branch_id
        from cart_detail right join (
            select K.cart_id, K.total_price, orders.created_at, payment_status, deliver_status, order_type, branch_id 
            from (select cart_id, total_price from cart c where cart_holder = %s and opening = 0) K 
            left join orders on (orders.cart_id = K.cart_id)
        ) KK on (KK.cart_id = cart_detail.cart_id)
        group by KK.cart_id, KK.total_price, KK.created_at, KK.payment_status, KK.deliver_status, order_type, branch_id
        order by KK.created_at desc
    '''
    
    cursor = Connector.establishConnection().cursor()
    # rows = cursor.execute(query, (user_id, )).fetchall()
    cursor.execute(query,(user_id, ))
    rows = cursor.fetchall()
    
    res = {
        "orders": [
            {
                "orderid": row[0],
                "totalprice": row[1],
                "totalitems": row[2], 
                "createdat": datetime.datetime.strftime(row[3] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[3] else None,
                "paymentstatus": row[4],
                "deliverstatus": row[5],
                "ordertype": 1 if row[6] else 0,
                "branchid": row[7]
            } for row in rows
        ] + GetSharedOrdersData(user_id)['orders']
    }
    
    res['orders'].sort(key = lambda x: datetime.datetime.strptime(x['createdat'], '%d-%m-%Y'), reverse = True)
    print(res)
    return res

# o.createdAt, o.PaymentStatus, o.DeliverStatus, o.OrderType, o.branchid, c.totalprice, o.CustomerName, o.CustomerPhone, o.Address, o.Email, o.PickupTime
def SharedOrderDetails(user_id, cartid):
    cursor = Connector.establishConnection().cursor()
    
    query = '''
        select 
            created_at, payment_status, deliver_status, order_type, branch_id, 
            total_price, customer_name, customer_phone, qddress, email, pickup_time, cart_name
        from (
            select scart.cart_id, scart.total_price, scart.total_items, scart.cart_name from (
                select * from shared_cart where opening = 0 and cart_id = %s
            ) scart join (
                select * from shared_cart_member where member_id = %s
            ) smem on (smem.cart_id = scart.cart_id)
        ) s left join shared_orders so on (so.cart_id = s.cart_id)
    '''
    
    # generalInfo = cursor.execute(query, (cartid, user_id, )).fetchone()
    cursor.execute(query, (cartid, user_id, ))
    generalInfo = cursor.fetchone()

    if not generalInfo:
        raise Exception("permission denied")
    
    query = '''
        select p.id, s.size_id, s.quantity, p.title, p.descriptions, p.price, p.sex_id, p.category_id, p.image_url from (
            select product_id, size_id, quantity from shared_cart_details where cart_id = %s
        ) s join product p on ( p.id = s.product_id )
    '''
    
    # rows = cursor.execute(query, (cartid, )).fetchall()
    cursor.cursor.execute(query, (cartid, ))
    rows = cursor.fetchall()

    
    res = {
        "totalprice": generalInfo[5],
        "createdat": datetime.datetime.strftime(generalInfo[0] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[0] else None,
        "paymentstatus": generalInfo[1],
        "deliverstatus": generalInfo[2],
        "ordertype": 1 if generalInfo[3] else 0,
        "branchid": generalInfo[4],
        "cartname": generalInfo[11],
        "details": [
            {
                "sizeid": row[1],
                "quantity": row[2],
                "product": {
                    'id': row[0],
                    'title': row[3],
                    'description': row[4],
                    'price': row[5],
                    'sex': row[6],
                    'category': row[7],
                    'images': [row[8]]
                }
            } for row in rows
        ]
    }
    
    orderType = 1 if generalInfo[3] else 0
    
    if not orderType:
        res['infor'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9],
            "location": generalInfo[8],
        },
    else:
        res['date'] = datetime.datetime.strftime(generalInfo[10] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[10] else None
        
        res['customer'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9]
        }
        
        res['branch'] = BranchInfo(generalInfo[4])
        
    return res
    

def PersonalOrderDetails(user_id, code):
    cursor = Connector.establishConnection().cursor()
    
    query = '''select o.created_at, o.payment_status, o.deliver_status, o.order_type, o.branch_id, c.total_price, o.customer_name, o.customer_phone, o.address, o.email, o.pickup_time from (
            select cart_id, cart_holder, total_price from cart 
            where cart_holder = %s and cart_id = %s and opening = 0
        ) c left join orders o on (o.cart_id = c.cart_id)
    '''

    # generalInfo = cursor.execute(query, (user_id, code, )).fetchone()
    cursor.execute(query, (user_id, code, ))
    generalInfo = cursor.fetchone()
    if not generalInfo:
        try:
            res = SharedOrderDetails(user_id, code) # search in shared order
            return res
        except: 
            raise Exception("permission denied")        
    
    query = '''
        select c.product_id, c.size_id, c.quantity, p.title, p.descriptions, p.price, p.sex_id, p.category_id, p.image_url from (
            select product_id, size_id, quantity from cart_detail where cart_id = %s
        ) c left join product p on (p.id = c.product_id)
    '''

    # rows = cursor.execute(query, (code, )).fetchall()
    cursor.execute(query, (code, ))
    rows = cursor.fetchall()
    orderType = 1 if generalInfo[3] else 0
    
    res = {
        "totalprice": generalInfo[5],
        "createdat": datetime.datetime.strftime(generalInfo[0] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[0] else None,
        "paymentstatus": generalInfo[1],
        "deliverstatus": generalInfo[2],
        "ordertype": 1 if generalInfo[3] else 0,
        "branchid": generalInfo[4],
        "details": [
            {
                "sizeid": row[1],
                "quantity": row[2],
                "product": {
                    'id': row[0],
                    'title': row[3],
                    'description': row[4],
                    'price': row[5],
                    'sex': row[6],
                    'category': row[7],
                    'images': [row[8]]
                }
            } for row in rows
        ]
    }
    
    if not orderType:
        res['infor'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9],
            "location": generalInfo[8],
        },
    else:
        res['date'] = datetime.datetime.strftime(generalInfo[10] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[10] else None
        
        res['customer'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9]
        }
        
        res['branch'] = BranchInfo(generalInfo[4])
        
    return res

def ProcessMarkAsDelivered(cartid):
    cursor = Connector.establishConnection().cursor()
    cursor.execute('update orders set deliver_status = 1, payment_status = 1 where cart_id = %s', (cartid, ))
    cursor.execute('update shared_orders set deliver_status = 1, payment_status = 1 where cart_id = %s', (cartid, ))
    # cursor.commit()
    return {"message": "Done!"}