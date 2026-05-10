import { useEffect, useMemo, useState } from "react";
import {
  BriefcaseBusiness,
  CalendarDays,
  CheckCircle2,
  CircleDollarSign,
  Edit3,
  Loader2,
  Plus,
  RefreshCcw,
  Search,
  Trash2,
  Users,
  X
} from "lucide-react";

const emptyForm = {
  firstName: "",
  lastName: "",
  email: "",
  department: "",
  jobTitle: "",
  salary: "",
  hireDate: new Date().toISOString().slice(0, 10),
  status: "ACTIVE"
};

const statusLabels = {
  ACTIVE: "Active",
  INACTIVE: "Inactive",
  ON_LEAVE: "On leave"
};

function App() {
  const [employees, setEmployees] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [departmentFilter, setDepartmentFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [validationErrors, setValidationErrors] = useState({});

  const departments = useMemo(() => {
    return [...new Set(employees.map((employee) => employee.department).filter(Boolean))].sort();
  }, [employees]);

  const stats = useMemo(() => {
    const activeCount = employees.filter((employee) => employee.status === "ACTIVE").length;
    const payroll = employees.reduce((total, employee) => total + Number(employee.salary || 0), 0);
    const departmentsCount = new Set(employees.map((employee) => employee.department)).size;

    return {
      total: employees.length,
      activeCount,
      departmentsCount,
      payroll
    };
  }, [employees]);

  useEffect(() => {
    loadEmployees();
  }, []);

  async function request(path, options = {}) {
    const response = await fetch(path, {
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        ...options.headers
      },
      ...options
    });

    if (response.status === 204) {
      return null;
    }

    const data = await response.json();

    if (!response.ok) {
      const apiError = new Error(data.message || "Something went wrong");
      apiError.validationErrors = data.validationErrors || {};
      throw apiError;
    }

    return data;
  }

  async function loadEmployees(nextDepartment = departmentFilter) {
    setLoading(true);
    setError("");

    try {
      const query = nextDepartment ? `?department=${encodeURIComponent(nextDepartment)}` : "";
      const data = await request(`/api/employees${query}`);
      setEmployees(data);
    } catch (apiError) {
      setError(apiError.message);
    } finally {
      setLoading(false);
    }
  }

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setValidationErrors((current) => ({ ...current, [name]: undefined }));
  }

  function editEmployee(employee) {
    setEditingId(employee.id);
    setForm({
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      department: employee.department,
      jobTitle: employee.jobTitle,
      salary: employee.salary,
      hireDate: employee.hireDate,
      status: employee.status
    });
    setMessage("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
    setValidationErrors({});
  }

  async function submitEmployee(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");
    setValidationErrors({});

    const payload = {
      ...form,
      salary: Number(form.salary)
    };

    try {
      const path = editingId ? `/api/employees/${editingId}` : "/api/employees";
      const method = editingId ? "PUT" : "POST";
      await request(path, {
        method,
        body: JSON.stringify(payload)
      });

      setMessage(editingId ? "Employee updated." : "Employee created.");
      resetForm();
      await loadEmployees();
    } catch (apiError) {
      setError(apiError.message);
      setValidationErrors(apiError.validationErrors || {});
    } finally {
      setSaving(false);
    }
  }

  async function deleteEmployee(employee) {
    const confirmed = window.confirm(`Delete ${employee.firstName} ${employee.lastName}?`);
    if (!confirmed) {
      return;
    }

    setError("");
    setMessage("");

    try {
      await request(`/api/employees/${employee.id}`, { method: "DELETE" });
      setMessage("Employee deleted.");
      await loadEmployees();
      if (editingId === employee.id) {
        resetForm();
      }
    } catch (apiError) {
      setError(apiError.message);
    }
  }

  function applyDepartmentFilter(event) {
    event.preventDefault();
    loadEmployees(departmentFilter);
  }

  function clearDepartmentFilter() {
    setDepartmentFilter("");
    loadEmployees("");
  }

  return (
    <main className="app-shell">
      <section className="topbar">
        <div>
          <p className="eyebrow">Employee Management</p>
          <h1>Team directory</h1>
        </div>
        <button className="ghost-button" type="button" onClick={() => loadEmployees()} disabled={loading}>
          <RefreshCcw size={18} aria-hidden="true" />
          Refresh
        </button>
      </section>

      <section className="stats-grid" aria-label="Employee summary">
        <Metric icon={Users} label="Employees" value={stats.total} />
        <Metric icon={CheckCircle2} label="Active" value={stats.activeCount} />
        <Metric icon={BriefcaseBusiness} label="Departments" value={stats.departmentsCount} />
        <Metric icon={CircleDollarSign} label="Payroll" value={formatCurrency(stats.payroll)} />
      </section>

      {(message || error) && (
        <div className={error ? "notice error" : "notice success"} role="status">
          {error || message}
        </div>
      )}

      <section className="workspace">
        <form className="employee-form" onSubmit={submitEmployee}>
          <div className="section-heading">
            <div>
              <p className="eyebrow">{editingId ? "Edit employee" : "New employee"}</p>
              <h2>{editingId ? "Update profile" : "Add profile"}</h2>
            </div>
            {editingId && (
              <button className="icon-button" type="button" onClick={resetForm} aria-label="Cancel editing">
                <X size={18} aria-hidden="true" />
              </button>
            )}
          </div>

          <div className="form-grid">
            <Field label="First name" name="firstName" value={form.firstName} onChange={updateField} error={validationErrors.firstName} />
            <Field label="Last name" name="lastName" value={form.lastName} onChange={updateField} error={validationErrors.lastName} />
            <Field label="Email" name="email" type="email" value={form.email} onChange={updateField} error={validationErrors.email} />
            <Field label="Department" name="department" value={form.department} onChange={updateField} error={validationErrors.department} />
            <Field label="Job title" name="jobTitle" value={form.jobTitle} onChange={updateField} error={validationErrors.jobTitle} />
            <Field label="Salary" name="salary" type="number" min="0" step="0.01" value={form.salary} onChange={updateField} error={validationErrors.salary} />
            <Field label="Hire date" name="hireDate" type="date" value={form.hireDate} onChange={updateField} error={validationErrors.hireDate} />
            <label className="field">
              <span>Status</span>
              <select name="status" value={form.status} onChange={updateField}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="ON_LEAVE">On leave</option>
              </select>
              {validationErrors.status && <small>{validationErrors.status}</small>}
            </label>
          </div>

          <button className="primary-button" type="submit" disabled={saving}>
            {saving ? <Loader2 className="spin" size={18} aria-hidden="true" /> : <Plus size={18} aria-hidden="true" />}
            {editingId ? "Save changes" : "Create employee"}
          </button>
        </form>

        <section className="directory">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Directory</p>
              <h2>Employees</h2>
            </div>
          </div>

          <form className="filter-bar" onSubmit={applyDepartmentFilter}>
            <div className="search-field">
              <Search size={18} aria-hidden="true" />
              <input
                list="departments"
                value={departmentFilter}
                onChange={(event) => setDepartmentFilter(event.target.value)}
                placeholder="Filter by department"
                aria-label="Filter by department"
              />
              <datalist id="departments">
                {departments.map((department) => (
                  <option key={department} value={department} />
                ))}
              </datalist>
            </div>
            <button className="ghost-button" type="submit">Apply</button>
            <button className="icon-button" type="button" onClick={clearDepartmentFilter} aria-label="Clear department filter">
              <X size={18} aria-hidden="true" />
            </button>
          </form>

          <div className="table-wrap">
            {loading ? (
              <div className="empty-state">
                <Loader2 className="spin" size={26} aria-hidden="true" />
                Loading employees...
              </div>
            ) : employees.length === 0 ? (
              <div className="empty-state">No employees found.</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Department</th>
                    <th>Role</th>
                    <th>Salary</th>
                    <th>Hire date</th>
                    <th>Status</th>
                    <th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {employees.map((employee) => (
                    <tr key={employee.id}>
                      <td>
                        <strong>{employee.firstName} {employee.lastName}</strong>
                        <span>{employee.email}</span>
                      </td>
                      <td>{employee.department}</td>
                      <td>{employee.jobTitle}</td>
                      <td>{formatCurrency(employee.salary)}</td>
                      <td>
                        <CalendarDays size={16} aria-hidden="true" />
                        {formatDate(employee.hireDate)}
                      </td>
                      <td>
                        <span className={`status ${employee.status.toLowerCase().replace("_", "-")}`}>
                          {statusLabels[employee.status] || employee.status}
                        </span>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button className="icon-button" type="button" onClick={() => editEmployee(employee)} aria-label={`Edit ${employee.firstName}`}>
                            <Edit3 size={17} aria-hidden="true" />
                          </button>
                          <button className="icon-button danger" type="button" onClick={() => deleteEmployee(employee)} aria-label={`Delete ${employee.firstName}`}>
                            <Trash2 size={17} aria-hidden="true" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </section>
    </main>
  );
}

function Metric({ icon: Icon, label, value }) {
  return (
    <article className="metric">
      <Icon size={20} aria-hidden="true" />
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function Field({ label, error, ...props }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input {...props} />
      {error && <small>{error}</small>}
    </label>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(value));
}

export default App;
