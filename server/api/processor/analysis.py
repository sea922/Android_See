from modules.data_acess.driver import Connector
from .products import GetProductsByList
import random

def TrendingItems(top_k = 5):
    query = 'SELECT id, count FROM trending ORDER BY count DESC LIMIT {}'.format(top_k)
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList(ids, top_k)

def HighlightItems(top_k = 5):
    query = f'select {top_k} product_id from inventory group by product_id order by sum(quantity) desc'
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList(ids, top_k)

def RelatedItems(id, top_k = 5):
    query = 'select category_id from product where id = %s'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (id,))
    rows = cursor.fetchall()
    # rows = cursor.execute(query, (id,)).fetchall()
    
    if len(rows) == 0:
        return []

    catid = rows[0][0]
    
    query = 'select id from product where category_id = %s and id != %s limit 10'
    cursor.execute(query, (catid, id))
    rows = cursor.fetchall()
    # rows = cursor.execute(query, (catid, id)).fetchall()
    randList = [row[0] for row in rows]
    return GetProductsByList(random.sample(randList, min(len(randList), top_k)), top_k, False)