package ru.msu.cmc.webprak.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webprak.controller.form.AssignmentForm;
import ru.msu.cmc.webprak.controller.form.EmployeeForm;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.EducationLevel;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.service.AssignmentService;
import ru.msu.cmc.webprak.service.DepartmentService;
import ru.msu.cmc.webprak.service.EmployeeService;
import ru.msu.cmc.webprak.service.PositionService;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final AssignmentService assignmentService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long departmentId,
                       @RequestParam(required = false) Long positionId,
                       @RequestParam(defaultValue = "name") String sort,
                       Model model) {
        List<Employee> employees = resolveEmployees(q, departmentId, positionId);
        List<EmployeeRow> rows = employees.stream()
                .map(this::toRow)
                .sorted(comparator(sort))
                .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("q", q);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("positionId", positionId);
        model.addAttribute("sort", sort);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("positions", positionService.findAll());
        return "employees/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addEmployeeFormData(model, new EmployeeForm(), "Добавление служащего", null);
        return "employees/form";
    }

    @PostMapping
    public String create(@ModelAttribute EmployeeForm form, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.create(
                    form.getLastName(), form.getFirstName(), form.getMiddleName(), form.getHomeAddress(),
                    form.getEducation(), form.getHireDate(), form.getStatus());
            redirectAttributes.addFlashAttribute("success", "Служащий добавлен");
            return "redirect:/employees/" + employee.getId();
        } catch (BusinessLogicException exception) {
            addEmployeeFormData(model, form, "Добавление служащего", exception.getMessage());
            return "employees/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Employee employee = requireEmployee(id);
        model.addAttribute("employee", employee);
        model.addAttribute("currentAssignment", employeeService.findCurrentAssignmentWithDetails(id).orElse(null));
        model.addAttribute("workPeriod", formatWorkPeriod(employee.getHireDate()));
        return "employees/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = requireEmployee(id);
        addEmployeeFormData(model, EmployeeForm.from(employee), "Редактирование служащего", null);
        model.addAttribute("employeeId", id);
        return "employees/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute EmployeeForm form,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            employeeService.update(
                    id, form.getLastName(), form.getFirstName(), form.getMiddleName(), form.getHomeAddress(),
                    form.getEducation(), form.getHireDate(), form.getStatus());
            redirectAttributes.addFlashAttribute("success", "Служащий сохранен");
            return "redirect:/employees/" + id;
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            addEmployeeFormData(model, form, "Редактирование служащего", exception.getMessage());
            model.addAttribute("employeeId", id);
            return "employees/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Служащий удален");
            return "redirect:/employees";
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/employees/" + id;
        }
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        Employee employee = requireEmployee(id);
        model.addAttribute("employee", employee);
        model.addAttribute("assignments", employeeService.getAssignmentHistoryWithDetails(id));
        return "employees/history";
    }

    @GetMapping("/{id}/assign")
    public String assignForm(@PathVariable Long id, Model model) {
        Employee employee = requireEmployee(id);
        AssignmentForm form = new AssignmentForm();
        form.setStartDate(LocalDate.now());
        addAssignmentFormData(model, employee, form, null);
        return "employees/assign";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id,
                         @ModelAttribute AssignmentForm form,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Employee employee = requireEmployee(id);
        try {
            if (employeeService.findCurrentAssignment(id).isPresent()) {
                assignmentService.transferEmployee(id, form.getDepartmentId(), form.getPositionId(), form.getStartDate(), form.getNote());
                redirectAttributes.addFlashAttribute("success", "Служащий переведен на новую должность");
            } else {
                assignmentService.assignEmployee(id, form.getDepartmentId(), form.getPositionId(), form.getStartDate(), form.getNote());
                redirectAttributes.addFlashAttribute("success", "Назначение создано");
            }
            return "redirect:/employees/" + id + "/history";
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            addAssignmentFormData(model, employee, form, exception.getMessage());
            return "employees/assign";
        }
    }

    private List<Employee> resolveEmployees(String q, Long departmentId, Long positionId) {
        if (departmentId != null) {
            return employeeService.findActiveEmployeesByDepartment(departmentId);
        }
        if (positionId != null) {
            return employeeService.findActiveEmployeesByPosition(positionId);
        }
        return employeeService.searchByFullName(q);
    }

    private EmployeeRow toRow(Employee employee) {
        Optional<Assignment> assignment = employeeService.findCurrentAssignmentWithDetails(employee.getId());
        return new EmployeeRow(
                employee.getId(),
                employee.getFullName(),
                employee.getHireDate(),
                formatWorkPeriod(employee.getHireDate()),
                employee.getStatus(),
                assignment.map(a -> a.getDepartment().getId()).orElse(null),
                assignment.map(a -> a.getDepartment().getName()).orElse("Нет назначения"),
                assignment.map(a -> a.getPosition().getId()).orElse(null),
                assignment.map(a -> a.getPosition().getName()).orElse("Нет назначения")
        );
    }

    private Comparator<EmployeeRow> comparator(String sort) {
        return switch (sort) {
            case "hireDate" -> Comparator.comparing(EmployeeRow::hireDate);
            case "department" -> Comparator.comparing(EmployeeRow::departmentName);
            case "position" -> Comparator.comparing(EmployeeRow::positionName);
            default -> Comparator.comparing(EmployeeRow::fullName);
        };
    }

    private Employee requireEmployee(Long id) {
        return employeeService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: id=" + id));
    }

    private void addEmployeeFormData(Model model, EmployeeForm form, String title, String error) {
        model.addAttribute("form", form);
        model.addAttribute("title", title);
        model.addAttribute("error", error);
        model.addAttribute("educationLevels", EducationLevel.values());
        model.addAttribute("statuses", EmployeeStatus.values());
    }

    private void addAssignmentFormData(Model model, Employee employee, AssignmentForm form, String error) {
        model.addAttribute("employee", employee);
        model.addAttribute("form", form);
        model.addAttribute("error", error);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("positions", positionService.findAll());
        model.addAttribute("currentAssignment", employeeService.findCurrentAssignmentWithDetails(employee.getId()).orElse(null));
    }

    private String formatWorkPeriod(LocalDate hireDate) {
        Period period = Period.between(hireDate, LocalDate.now());
        return "%d г. %d мес.".formatted(period.getYears(), period.getMonths());
    }

    public record EmployeeRow(Long id,
                              String fullName,
                              LocalDate hireDate,
                              String workPeriod,
                              EmployeeStatus status,
                              Long departmentId,
                              String departmentName,
                              Long positionId,
                              String positionName) {
    }
}
