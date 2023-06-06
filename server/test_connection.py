import json
import pyodbc

# Set up connection parameters
server = 'LAPTOP-8VIIT720\\POMAN'
database = 'test_python'
username = 'pomanjr'
password = 'pomanjr'
driver = '{ODBC Driver 17 for SQL Server}'

# Connect to the database
json_data = '''
{
    "title": "Product A",
    "description": "This is a description of Product A",
    "price": 9.99,
    "sex_id": 1,
    "category_id": 1,
    "image_url": "https://example.com/image.jpg"
}
'''

# Parse the JSON data into a dictionary
data = json.loads(json_data)

# Construct the SQL INSERT statement
insert_statement = f"INSERT INTO Product (Title, Descriptions, Price, sex_id, category_id, ImageUrl) VALUES ('{data['title']}', '{data['description']}', {data['price']}, {data['sex_id']}, {data['category_id']}, '{data['image_url']}')"

# Connect to the SQL Server database and execute the INSERT statement
conn = pyodbc.connect(f"DRIVER={{SQL Server}};SERVER={server};DATABASE={database};UID={username};PWD={password}")
cursor = conn.cursor()
cursor.execute(insert_statement)
conn.commit()
conn.close()
