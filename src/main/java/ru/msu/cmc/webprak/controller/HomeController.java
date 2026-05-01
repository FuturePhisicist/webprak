package ru.msu.cmc.webprak.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.msu.cmc.webprak.service.DepartmentService;
import ru.msu.cmc.webprak.service.EmployeeService;
import ru.msu.cmc.webprak.service.PositionService;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final PositionService positionService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("employeeCount", employeeService.findAll().size());
        model.addAttribute("departmentCount", departmentService.findAll().size());
        model.addAttribute("positionCount", positionService.findAll().size());
        return "index";
    }
}
