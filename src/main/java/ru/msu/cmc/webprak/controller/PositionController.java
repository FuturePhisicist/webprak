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
import ru.msu.cmc.webprak.controller.form.PositionForm;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Position;
import ru.msu.cmc.webprak.service.AssignmentService;
import ru.msu.cmc.webprak.service.PositionService;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

@Controller
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;
    private final AssignmentService assignmentService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rows", positionService.findAll().stream()
                .map(position -> new PositionRow(
                        position.getId(),
                        position.getName(),
                        truncate(position.getResponsibilities()),
                        positionService.countDepartmentsUsingPosition(position.getId()),
                        positionService.countActiveAssignments(position.getId())))
                .toList());
        return "positions/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addFormData(model, new PositionForm(), "Добавление должности", null, null);
        return "positions/form";
    }

    @PostMapping
    public String create(@ModelAttribute PositionForm form, Model model, RedirectAttributes redirectAttributes) {
        try {
            Position position = positionService.create(form.getName(), form.getResponsibilities());
            redirectAttributes.addFlashAttribute("success", "Должность добавлена");
            return "redirect:/positions/" + position.getId();
        } catch (BusinessLogicException exception) {
            addFormData(model, form, "Добавление должности", exception.getMessage(), null);
            return "positions/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Position position = requirePosition(id);
        model.addAttribute("position", position);
        model.addAttribute("departmentPositions", positionService.findDepartmentPositionsByPosition(id).stream()
                .map(this::toSlotRow)
                .toList());
        model.addAttribute("activeAssignments", positionService.countActiveAssignments(id));
        return "positions/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Position position = requirePosition(id);
        addFormData(model, PositionForm.from(position), "Редактирование должности", null, id);
        return "positions/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute PositionForm form,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            positionService.update(id, form.getName(), form.getResponsibilities());
            redirectAttributes.addFlashAttribute("success", "Должность сохранена");
            return "redirect:/positions/" + id;
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            addFormData(model, form, "Редактирование должности", exception.getMessage(), id);
            return "positions/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            positionService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Должность удалена");
            return "redirect:/positions";
        } catch (BusinessLogicException | EntityNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/positions/" + id;
        }
    }

    private Position requirePosition(Long id) {
        return positionService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Должность не найдена: id=" + id));
    }

    private void addFormData(Model model, PositionForm form, String title, String error, Long positionId) {
        model.addAttribute("form", form);
        model.addAttribute("title", title);
        model.addAttribute("error", error);
        model.addAttribute("positionId", positionId);
    }

    private String truncate(String value) {
        return value.length() <= 100 ? value : value.substring(0, 97) + "...";
    }

    private PositionSlotRow toSlotRow(DepartmentPosition departmentPosition) {
        long occupied = assignmentService.countOccupiedSlots(
                departmentPosition.getDepartment().getId(),
                departmentPosition.getPosition().getId());
        long free = departmentPosition.getSlotsTotal() - occupied;
        return new PositionSlotRow(
                departmentPosition.getDepartment().getId(),
                departmentPosition.getDepartment().getName(),
                departmentPosition.getSlotsTotal(),
                occupied,
                Math.max(free, 0));
    }

    public record PositionRow(Long id,
                              String name,
                              String responsibilities,
                              long departmentCount,
                              long activeAssignmentCount) {
    }

    public record PositionSlotRow(Long departmentId,
                                  String departmentName,
                                  int slotsTotal,
                                  long occupiedSlots,
                                  long freeSlots) {
    }
}
