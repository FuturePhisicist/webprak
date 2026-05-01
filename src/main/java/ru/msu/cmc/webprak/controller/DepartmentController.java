package ru.msu.cmc.webprak.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webprak.controller.form.DepartmentForm;
import ru.msu.cmc.webprak.controller.form.DepartmentPositionForm;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.service.DepartmentService;
import ru.msu.cmc.webprak.service.EmployeeService;
import ru.msu.cmc.webprak.service.PositionService;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final PositionService positionService;

    @GetMapping
    public String list(Model model) {
        List<DepartmentRow> rows = departmentService.findAll().stream()
                .map(this::toRow)
                .toList();
        model.addAttribute("rows", rows);
        return "departments/list";
    }

    @GetMapping("/graph")
    public String graph(Model model) {
        List<Department> departments = departmentService.findAll();
        Map<Long, Department> byId = departments.stream()
                .collect(Collectors.toMap(Department::getId, Function.identity()));
        List<DepartmentNode> nodes = departments.stream()
                .map(department -> toNode(department, byId))
                .toList();
        model.addAttribute("nodes", nodes);
        return "departments/graph";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addFormData(model, new DepartmentForm(), "Добавление подразделения", null, null);
        return "departments/form";
    }

    @PostMapping
    public String create(@ModelAttribute DepartmentForm form, Model model, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.create(form.getName(), form.getParentDepartmentId(), form.getManagerEmployeeId());
            redirectAttributes.addFlashAttribute("success", "Подразделение добавлено");
            return "redirect:/departments/" + department.getId();
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            addFormData(model, form, "Добавление подразделения", exception.getMessage(), null);
            return "departments/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Department department = requireDepartment(id);
        List<Department> childDepartments = departmentService.findChildDepartments(id);
        List<DepartmentPositionRow> departmentPositions = positionService.findDepartmentPositions(id).stream()
                .map(dp -> new DepartmentPositionRow(
                        dp.getPosition().getId(),
                        dp.getPosition().getName(),
                        dp.getSlotsTotal(),
                        employeeService.findActiveEmployeesByPosition(dp.getPosition().getId()).stream()
                                .filter(employee -> employeeService.findCurrentAssignmentWithDetails(employee.getId())
                                        .map(assignment -> assignment.getDepartment().getId().equals(id))
                                        .orElse(false))
                                .toList()))
                .toList();

        model.addAttribute("department", department);
        model.addAttribute("childDepartments", childDepartments);
        model.addAttribute("departmentPositions", departmentPositions);
        model.addAttribute("departmentPositionForm", new DepartmentPositionForm());
        model.addAttribute("positions", positionService.findAll());
        model.addAttribute("activeEmployees", employeeService.findActiveEmployeesByDepartment(id));
        return "departments/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Department department = requireDepartment(id);
        addFormData(model, DepartmentForm.from(department), "Редактирование подразделения", null, id);
        return "departments/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute DepartmentForm form,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            departmentService.update(id, form.getName(), form.getParentDepartmentId(), form.getManagerEmployeeId());
            redirectAttributes.addFlashAttribute("success", "Подразделение сохранено");
            return "redirect:/departments/" + id;
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            addFormData(model, form, "Редактирование подразделения", exception.getMessage(), id);
            return "departments/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Подразделение удалено");
            return "redirect:/departments";
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/departments/" + id;
        }
    }

    @PostMapping("/{id}/positions")
    public String addPosition(@PathVariable Long id,
                              @ModelAttribute DepartmentPositionForm form,
                              RedirectAttributes redirectAttributes) {
        try {
            positionService.addPositionToDepartment(id, form.getPositionId(), form.getSlotsTotal());
            redirectAttributes.addFlashAttribute("success", "Должность добавлена в подразделение");
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/departments/" + id;
    }

    @PostMapping("/{departmentId}/positions/{positionId}/delete")
    public String removePosition(@PathVariable Long departmentId,
                                 @PathVariable Long positionId,
                                 RedirectAttributes redirectAttributes) {
        try {
            positionService.removePositionFromDepartment(departmentId, positionId);
            redirectAttributes.addFlashAttribute("success", "Должность удалена из подразделения");
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/departments/" + departmentId;
    }

    private Department requireDepartment(Long id) {
        return departmentService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Подразделение не найдено: id=" + id));
    }

    private DepartmentRow toRow(Department department) {
        return new DepartmentRow(
                department.getId(),
                department.getName(),
                department.getParentDepartment() == null ? null : department.getParentDepartment().getId(),
                department.getParentDepartment() == null ? "Корневое" : department.getParentDepartment().getName(),
                department.getManager() == null ? null : department.getManager().getId(),
                department.getManager() == null ? "Не назначен" : department.getManager().getFullName(),
                departmentService.countChildDepartments(department.getId()),
                departmentService.countActiveEmployees(department.getId())
        );
    }

    private DepartmentNode toNode(Department department, Map<Long, Department> byId) {
        return new DepartmentNode(
                department.getId(),
                department.getName(),
                department.getParentDepartment() == null ? null : department.getParentDepartment().getId(),
                department.getManager() == null ? null : department.getManager().getId(),
                department.getManager() == null ? "Не назначен" : department.getManager().getFullName(),
                departmentService.countChildDepartments(department.getId()),
                departmentService.countActiveEmployees(department.getId()),
                depth(department, byId)
        );
    }

    private int depth(Department department, Map<Long, Department> byId) {
        int depth = 0;
        Department current = department;
        while (current.getParentDepartment() != null) {
            depth++;
            current = byId.get(current.getParentDepartment().getId());
            if (current == null) {
                break;
            }
        }
        return depth;
    }

    private void addFormData(Model model, DepartmentForm form, String title, String error, Long departmentId) {
        model.addAttribute("form", form);
        model.addAttribute("title", title);
        model.addAttribute("error", error);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("departments", departmentService.findAll().stream()
                .filter(department -> !department.getId().equals(departmentId))
                .toList());
        model.addAttribute("employees", employeeService.findAll());
    }

    public record DepartmentRow(Long id,
                                String name,
                                Long parentId,
                                String parentName,
                                Long managerId,
                                String managerName,
                                long childCount,
                                long activeEmployeeCount) {
    }

    public record DepartmentNode(Long id,
                                 String name,
                                 Long parentId,
                                 Long managerId,
                                 String managerName,
                                 long childCount,
                                 long activeEmployeeCount,
                                 int depth) {
    }

    public record DepartmentPositionRow(Long positionId,
                                        String positionName,
                                        int slotsTotal,
                                        List<Employee> activeEmployees) {
    }
}
