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
   },
   {
      "dataValue" : "6959e88d-4f66-4f3e-946e-1c695e7abf03_tenant_id_2",
      "id" : 8,
      "tenantId" : 2
   },
   {
      "dataValue" : "8e9cf4f6-66f1-4d31-8ad3-946d3c60ed62_tenant_id_2",
      "id" : 11,
      "tenantId" : 2
   }
]
```