from modules.data_acess.driver import Connector
from .misc import GetCategoryName, GetSexName
from .constants import ITEM_PER_PAGE, allIdsList
import random

categoryMapping = None
priceMapping = None

if categoryMapping is None:
    query = 'select p.id, category_name, p.price from product p join category on p.category_id = category.category_id'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    categoryMapping = {row[0]: row[1] for row in rows}
    priceMapping = {row[0]: row[2] for row in rows}

def GetProducts(sex, category, page = None):
    if not sex and not category:
        return {}
    
    res = {}
    
    if sex and category:
        query = 'select id, title, descriptions, price, image_url from product where sex_id = %s and category_id = %ss'
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query)
        rows = cursor.fetchall()
        
        res = {'data': [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'images': [row[4]],
            'category_name': GetCategoryName(category),
            'category': category,
            'sex': sex
        } for row in rows]}
        
        if page is not None:
            lIdx = page * ITEM_PER_PAGE //6
            rIdx = (page + 1) * ITEM_PER_PAGE
            _lIdx, _rIdx = max(lIdx, 0), min(rIdx, len(res['data']))
            res['data'] = res['data'][_lIdx : _rIdx]
        
        return res        

    if sex:
        query = 'select id, title, descriptions, price, category_id, image_url from product where sex_id = %s'
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query,(sex, ))
        rows = cursor.fetchall()
        res = {}
        
        for row in rows:
            if row[4] not in res: res[row[4]] = []
            res[row[4]] += [{
                'id': row[0],
                'title': row[1],
                'description': row[2],
                'price': int(row[3]),
                'images': [row[5]],
                'sex': sex,
                'category_name': GetCategoryName(row[4]),
                'category': row[4]
            }]
        
        if page is not None:
            
            lIdx = page * ITEM_PER_PAGE // 6
            rIdx = (page + 1) * ITEM_PER_PAGE // 6
            
            res = {
                key: val[max(lIdx, 0): min(rIdx, len(val))] for key, val in res.items()
            }
        
        return res
    
    if category:
        query = 'select id, title, descriptions, price, sex_id, image_url, category_id from product where category_id = %s'
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query, (category, ))
        rows = cursor.fetchall()
        res = {}
        for row in rows:
            if row[4] not in res: res[row[4]] = []
            res[row[4]] += [{
                'id': row[0],
                'title': row[1],
                'description': row[2],
                'price': int(row[3]),
                'images': [row[5]],
                'category_name': GetCategoryName(category),
                'category': category,
                'sex': row[4]
            }]
        
        if page is not None:
            
            lIdx = page * ITEM_PER_PAGE
            rIdx = (page + 1) * ITEM_PER_PAGE
            
            res = {
                key: val[max(lIdx, 0): min(rIdx, len(val))] for key, val in res.items()
            }

        return res
    
def GetProductsByListRand(limit, ignoreid):
    global allIdsList
    if not allIdsList:
        query = "select id from product"
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query)
        rows = cursor.fetchall()
        allIdsList = [row[0] for row in rows]

    if type(ignoreid) != list: ignoreid = [ignoreid]
    randIds = [id for id in allIdsList if id not in ignoreid]
    randIds = random.sample(randIds, min(limit, len(randIds)))
    
    query = f"select id, title, descriptions, price, sex_id, category_id, image_url from product p where p.id in ({','.join([str(i) for i in randIds])})"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()

    return [
        {
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        } for row in rows
    ]
    
def GetProductsByList(ids: list, limit = 8, requiredSampler = True):
    if len(ids) > limit:
        ids = random.sample(ids, min(len(ids), limit))
  
    if len(ids) == 0:
        return {
            'data': GetProductsByListRand(limit, -1)
        }
    
    ids = [str(id) for id in ids]    
    
    idsStr = f'({", ".join(ids)})'
    query = f"select id, title, descriptions, price, sex_id, category_id, image_url from product p where p.id in {idsStr}"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    res = {"data": []}
    for row in rows:
        res["data"] += [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        }]
        
    if len(res["data"]) < limit and requiredSampler:
        res["data"] += GetProductsByListRand(limit - len(res["data"]), ids[0])
    return res  

def GetProductsByList_2(ids, page = None):
    if type(ids) != list or len(ids) == 0:
        return {
            'data': []
        }
    
    query = f"select id, title, descriptions, price, sex_id, category_id, image_url from product p where p.id in ({','.join([str(i) for i in ids])})"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    
    res = {
        'data': []
    }
    
    for row in rows:
        res['data'] += [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        }]
        
    if page is not None:
        lIdx, rIdx = (page) * ITEM_PER_PAGE, (page + 1) * ITEM_PER_PAGE
        res['data'] = res['data'][max(0, lIdx) : min(rIdx, len(res['data']))]

    return res

def GetProductDetails(id, user_id):
    queryInfo = f"select id, title, descriptions, price, sex_id, category_id from product p where p.id = %s"
    queryImage = f"select I.image_url from image I where I.product_id = %s"
    querySize = f"select size_id, quantity from inventory I where I.product_id = %s"
    
    cursor = Connector.establishConnection().cursor()
    
    isInWishList = False
    if user_id is not None:
        checkWishList = f"select count(*) from wishlist w where w.user_id =  %s and w.product_id =  %s"
        # result = cursor.execute(checkWishList, (user_id, id)).fetchone()
        cursor.execute(checkWishList, (user_id, id))
        result = cursor.fetchone()[0]
        if result > 0: isInWishList = True
    cursor.execute(queryInfo, (id,))
    prodInfo = cursor.fetchone()
    # prodInfo = cursor.execute(queryInfo, (id,)).fetchone()
    cursor.execute(queryImage, (id,))
    images = cursor.fetchall()

    # inventory = cursor.execute(querySize, (id, )).fetchall()
    cursor.execute(querySize, (id,))
    inventory = cursor.fetchall()

    
    return {
        'id': prodInfo[0],
        'title': prodInfo[1],
        'description': prodInfo[2],
        'price': int(prodInfo[3]),
        'sex': prodInfo[4],
        'category': prodInfo[5],
        'category_name': GetCategoryName(prodInfo[5]),
        'inventory': {
            item[0]: item[1] for item in inventory
        },
        'images': [item[0] for item in images],
        'isInWishList': isInWishList
    } if prodInfo else None