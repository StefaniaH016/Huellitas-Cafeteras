import json
import os

base_dir = os.path.dirname(__file__)
src_path = os.path.join(base_dir, "postman_collection.json")
dst_path = os.path.join(base_dir, "postman_flat_collection.json")

with open(src_path, "r", encoding="utf-8") as f:
    nested = json.load(f)

flat_items = []
for folder in nested.get("item", []):
    folder_name = folder.get("name", "")
    prefix = folder_name.split(" - ")[0] if " - " in folder_name else folder_name
    for req in folder.get("item", []):
        new_req = dict(req)
        new_req["name"] = f"[{prefix}] {req.get('name', '')}"
        flat_items.append(new_req)

flat_col = {
    "info": {
        "name": "Huellitas Cafeteras API - Suite Completa (Ejecución Directa)",
        "description": "Colección completa con todos los tests integrados en cada solicitud (sin carpetas anidadas) para ejecución directa en Postman Desktop y Collection Runner sin pérdida de scripts.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "variable": nested.get("variable", []),
    "item": flat_items
}

with open(dst_path, "w", encoding="utf-8") as f:
    json.dump(flat_col, f, indent=2, ensure_ascii=False)

print(f"Created flat collection with {len(flat_items)} requests at {dst_path}")
