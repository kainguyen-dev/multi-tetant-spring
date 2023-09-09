# Multi tenant implementation in Spring

### 1. Testing Multi-tenant with Foreign key 

```shell
# 1. Build database
docker-compose -f docker/db_foreign_key.yaml up -d  

# 2. Start application 

# 3. Test with simple curl 
curl -H "X-TENANT-ID: 2"  -X GET localhost:8080/data_log | json_pp 

```

The response will not return data with tenant_id = 2

```json
[
   {
      "dataValue" : "982b9a2a-7d3a-43d8-ae95-3e6a7f46a7e6_tenant_id_2",
      "id" : 2,
      "tenantId" : 2
   },
   {
      "dataValue" : "fcfe5c62-529e-45b2-9de2-87716942d0a1_tenant_id_2",
      "id" : 5,
      "tenantId" : 2
   }
]
```


### 2. Testing Multi-tenant with Multi schema

```shell
# 1. Build database
docker-compose -f docker/db_multi_schema.yaml up --force-recreate -V -d 

# 2. Start application 

# 3. Test with simple curl 
curl --location 'localhost:8080/data_log' --header 'X-TENANT-ID: CAR_EV' | json_pp 

```
The response will not return data with tenant_id = CAR_EV

```json
[
  {
    "id": 1,
    "dataValue": "6959e88d-4f66-4f3e-946e-1c695e7abf03_CAR_EV"
  },
  {
    "id": 2,
    "dataValue": "44f7d081-5c8d-4a95-bc59-5a5e5c9a4421_CAR_EV"
  }
]
```