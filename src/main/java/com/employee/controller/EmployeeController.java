package com.employee.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.employee.model.Employee;

@Controller
public class EmployeeController {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    /*
     * This method establishes a connection to the database and returns the connection object.
     */
    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /*
     * This method returns the home page with a list of all employees.
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Employee> employees = getAllEmployees();
        model.addAttribute("employees", employees);
        return "index";
    }

    /*
     * This method returns the form to add a new employee.
     */
    @GetMapping("/add")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "add-employee";
    }

    /*
     * This method adds a new employee to the database.
     */
    @PostMapping("/add")
    public String addEmployee(@ModelAttribute Employee employee, Model model) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO employees (id, name, email, salary, department) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, employee.getId());
            statement.setString(2, employee.getName());
            statement.setString(3, employee.getEmail());
            statement.setDouble(4, employee.getSalary());
            statement.setString(5, employee.getDepartment());
            statement.executeUpdate();

            model.addAttribute("success", "Successfully added employee " + employee.getName());
            // Explicitly create new object to clear the form
            Employee newEmployee = new Employee();
            model.addAttribute("employee", newEmployee);
            return "add-employee";
        } catch (SQLException e) {
            model.addAttribute("error", "Error adding employee " + e.getMessage());
            e.printStackTrace();
            return "add-employee";
        }
    }

    /*
     * This method returns the list of employees whose name contains the search string.
     */
    @GetMapping("/search")
    public String searchEmployees(@RequestParam String name, Model model) {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM employees WHERE name LIKE ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + name + "%");
            statement.executeQuery();
            ResultSet resultSet = statement.getResultSet();
            // Iterate over the result set and create a list of employees
            while (resultSet.next()) {
                Employee employee = new Employee();
                employee.setId(resultSet.getLong("id"));
                employee.setName(resultSet.getString("name"));
                employee.setEmail(resultSet.getString("email"));
                employee.setSalary(resultSet.getDouble("salary"));
                employee.setDepartment(resultSet.getString("department"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        model.addAttribute("employees", employees);
        return "index";
    }

    /*
     * This method returns the form to edit an employee with the given ID.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Employee employee = getEmployeeById(id);
            model.addAttribute("employee", employee);
            return "edit-employee";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }

    /*
     * This method updates an employee with the given ID in the database.
     */
    @PostMapping("/edit/{id}")
    public String editEmployee(@PathVariable long id, @ModelAttribute Employee employee, Model model) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE employees SET name = ?, email = ?, salary = ?, department = ? WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, employee.getName());
            statement.setString(2, employee.getEmail());
            statement.setDouble(3, employee.getSalary());
            statement.setString(4, employee.getDepartment());
            statement.setLong(5, id);
            statement.executeUpdate();

            model.addAttribute("success", "Successfully updated employee " + employee.getName());
            return "edit-employee";
        } catch (SQLException e) {
            model.addAttribute("error", "Error updating employee " + e.getMessage());
            e.printStackTrace();
            return "edit-employee";
        }
    }

    /*
     * This method returns the form to delete an employee with the given ID.
     */
    @GetMapping("/delete/{id}")
    public String showDeleteForm(@PathVariable Long id, Model model) {
        try {
            Employee employee = getEmployeeById(id);
            model.addAttribute("employee", employee);
            return "delete-employee";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }

    /*
     * This method deletes an employee with the given ID from the database.
     */
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE from employees WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, id);
            statement.executeUpdate();
            redirectAttributes.addFlashAttribute("success", "Employee with ID " + id + " is successfully deleted!");

            return "redirect:/";
        } catch (SQLException e) {
            model.addAttribute("error", "Error updating employee " + e.getMessage());
            e.printStackTrace();
            return "delete-employee";
        }
    }

    /*
     * This method returns a list of all employees from the database.
     */
    private List<Employee> getAllEmployees() {
        // Implement this method to return a list of all employees from the database
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM employees";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Employee employee = new Employee();
                employee.setId(resultSet.getLong("id"));
                employee.setName(resultSet.getString("name"));
                employee.setEmail(resultSet.getString("email"));
                employee.setSalary(resultSet.getDouble("salary"));
                employee.setDepartment(resultSet.getString("department"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /*
     * This method returns an employee with the given ID from the database.
     */
    private Employee getEmployeeById(Long id) throws Exception {
        Employee employee = new Employee();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM employees WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                employee.setId(resultSet.getLong("id"));
                employee.setName(resultSet.getString("name"));
                employee.setEmail(resultSet.getString("email"));
                employee.setSalary(resultSet.getDouble("salary"));
                employee.setDepartment(resultSet.getString("department"));
            } else {
                throw new Exception("Employee not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employee;
    }
}
