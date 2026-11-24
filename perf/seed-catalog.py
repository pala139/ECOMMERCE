"""Generate deterministic SQL data for a fresh, disposable benchmark database."""

print("START TRANSACTION;")
for number in range(1, 101):
    print(f"INSERT INTO categories (id,name,description,created_at) VALUES ({number},'Category {number:03d}','Synthetic benchmark category',NOW());")
for number in range(1, 1001):
    category = (number - 1) % 100 + 1
    print(f"INSERT INTO products (id,name,description,price,stock_quantity,category_id,created_at) VALUES ({number},'Product {number:04d}','Synthetic benchmark product',19.99,100,{category},NOW());")
print("COMMIT;")
