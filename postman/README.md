# Employee Management Postman Files

Import these two files into Postman:

- `EmployeeManagement.postman_collection.json`
- `EmployeeManagement.local.postman_environment.json`

Select the `Employee Management Local` environment before running requests.

Run order:

1. `Create Employee`
2. `Get All Employees`
3. `Get Employee By Id`
4. `Update Employee`
5. `Delete Employee`

The `Create Employee` request automatically saves the returned `id` into the `employeeId` environment variable.
