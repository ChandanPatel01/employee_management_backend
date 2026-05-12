# Employee Management Postman Files

Import these two files into Postman:

- `EmployeeManagement.postman_collection.json`
- `EmployeeManagement.local.postman_environment.json`

Select the `Employee Management Local` environment before running requests.

Run order:

1. `Signup` or `Login`
2. `Create Employee`
3. `Get All Employees`
4. `Get Employee By Id`
5. `Update Employee`
6. `Delete Employee`

The auth requests automatically save the returned JWT into the `authToken` environment variable. The `Create Employee` request automatically saves the returned `id` into the `employeeId` environment variable.
