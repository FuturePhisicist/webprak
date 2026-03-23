package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.cmc.webprak.BaseIntegrationTest;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentServiceTest extends BaseIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Test
    void findRootDepartments_shouldReturnOnlyRootDepartments() {
        List<Department> departments = departmentService.findRootDepartments();

        assertEquals(1, departments.size());
        assertEquals("Головной офис", departments.get(0).getName());
        assertNull(departments.get(0).getParentDepartment());
    }

    @Test
    void findChildDepartments_shouldReturnDirectChildren() {
        List<Department> departments = departmentService.findChildDepartments(1L);

        assertEquals(3, departments.size());
        assertTrue(departments.stream().anyMatch(d -> d.getName().equals("IT департамент")));
        assertTrue(departments.stream().anyMatch(d -> d.getName().equals("HR департамент")));
        assertTrue(departments.stream().anyMatch(d -> d.getName().equals("Финансовый отдел")));
    }

    @Test
    void countChildDepartments_shouldReturnCorrectCount() {
        long count = departmentService.countChildDepartments(1L);

        assertEquals(3, count);
    }

    @Test
    void countActiveEmployees_shouldReturnCorrectCountForDepartment() {
        long count = departmentService.countActiveEmployees(5L);

        assertEquals(1, count);
    }

    @Test
    void findManager_shouldReturnDepartmentManager() {
        Optional<Employee> managerOpt = departmentService.findManager(2L);

        assertTrue(managerOpt.isPresent());
        assertEquals(3L, managerOpt.get().getId());
        assertEquals("Сидорова", managerOpt.get().getLastName());
    }

    @Test
    void findManager_shouldReturnEmptyForNonExistingDepartment() {
        Optional<Employee> managerOpt = departmentService.findManager(999L);

        assertTrue(managerOpt.isEmpty());
    }

    @Test
    void findById_shouldReturnDepartmentWhenExists() {
        Optional<Department> departmentOpt = departmentService.findById(3L);

        assertTrue(departmentOpt.isPresent());
        assertEquals("HR департамент", departmentOpt.get().getName());
    }

    @Test
    void findById_shouldReturnEmptyWhenDepartmentDoesNotExist() {
        Optional<Department> departmentOpt = departmentService.findById(999L);

        assertTrue(departmentOpt.isEmpty());
    }
}

