package ru.msu.cmc.webprak.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.msu.cmc.webprak.BaseIntegrationTest;
import ru.msu.cmc.webprak.model.Department;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebSystemTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DepartmentController departmentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void homePageShowsMainUseCases() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Все служащие")))
                .andExpect(content().string(containsString("Граф подразделений")))
                .andExpect(content().string(containsString("Добавить должность")));
    }

    @Test
    void employeesSearchReturnsMatchingEmployee() throws Exception {
        mockMvc.perform(get("/employees").param("q", "Иванов"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(content().string(containsString("Иванов Иван Иванович")));
    }

    @Test
    void employeesListSupportsFiltersAndSortModes() throws Exception {
        mockMvc.perform(get("/employees").param("departmentId", "5").param("sort", "hireDate"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(content().string(containsString("Ким Мария Сергеевна")));

        mockMvc.perform(get("/employees").param("positionId", "3").param("sort", "department"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(content().string(containsString("Сидорова Анна Олеговна")));

        mockMvc.perform(get("/employees").param("sort", "position"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"));
    }

    @Test
    void employeesSearchShowsEmptyResultMessage() throws Exception {
        mockMvc.perform(get("/employees").param("q", "НетТакогоСотрудника"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Служащие не найдены")));
    }

    @Test
    void missingEmployeeReturnsNotFoundMessage() throws Exception {
        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Сотрудник не найден: id=999")));
    }

    @Test
    void employeePagesAreAvailable() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(content().string(containsString("Добавление служащего")));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/detail"))
                .andExpect(content().string(containsString("Иванов Иван Иванович")));

        mockMvc.perform(get("/employees/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(content().string(containsString("Редактирование служащего")));

        mockMvc.perform(get("/employees/1/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/history"))
                .andExpect(content().string(containsString("Старший HR")));

        mockMvc.perform(get("/employees/1/assign"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/assign"))
                .andExpect(content().string(containsString("Текущее назначение будет закрыто")));
    }

    @Test
    void employeeCanBeCreated() throws Exception {
        mockMvc.perform(post("/employees")
                        .param("lastName", "Орлова")
                        .param("firstName", "Ольга")
                        .param("middleName", "Павловна")
                        .param("homeAddress", "Moscow, Lenina 1")
                        .param("education", "MASTER")
                        .param("hireDate", "2025-01-15")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/6"));
    }

    @Test
    void employeeCreateValidationErrorReturnsFormMessage() throws Exception {
        mockMvc.perform(post("/employees")
                        .param("lastName", "")
                        .param("firstName", "Ольга")
                        .param("homeAddress", "Moscow, Lenina 1")
                        .param("education", "MASTER")
                        .param("hireDate", "2025-01-15")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attribute("error", "Фамилия обязательна"));
    }

    @Test
    void employeeCanBeUpdatedAndValidationErrorIsShown() throws Exception {
        mockMvc.perform(post("/employees/1")
                        .param("lastName", "Иванов")
                        .param("firstName", "Иван")
                        .param("middleName", "Петрович")
                        .param("homeAddress", "New address")
                        .param("education", "MASTER")
                        .param("hireDate", "2018-04-16")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/1"))
                .andExpect(flash().attribute("success", "Служащий сохранен"));

        mockMvc.perform(post("/employees/1")
                        .param("lastName", "")
                        .param("firstName", "Иван")
                        .param("homeAddress", "New address")
                        .param("education", "MASTER")
                        .param("hireDate", "2018-04-16")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attribute("error", "Фамилия обязательна"));
    }

    @Test
    void employeeWithoutActiveAssignmentCanBeDeleted() throws Exception {
        mockMvc.perform(post("/employees")
                        .param("lastName", "Орлова")
                        .param("firstName", "Ольга")
                        .param("homeAddress", "Moscow, Lenina 1")
                        .param("education", "MASTER")
                        .param("hireDate", "2025-01-15")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/6"));

        mockMvc.perform(post("/employees/6/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("success", "Служащий удален"));
    }

    @Test
    void activeEmployeeDeleteShowsBusinessError() throws Exception {
        mockMvc.perform(post("/employees/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/1"))
                .andExpect(flash().attribute("error", "Нельзя удалить сотрудника с активным назначением"));
    }

    @Test
    void assignmentWithoutFreeSlotsReturnsBusinessError() throws Exception {
        mockMvc.perform(post("/employees/5/assign")
                        .param("departmentId", "1")
                        .param("positionId", "1")
                        .param("startDate", "2025-02-01")
                        .param("note", "Проверка ошибки"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/assign"))
                .andExpect(model().attribute("error", "Нет свободных ставок для departmentId=1, positionId=1"));
    }

    @Test
    void employeeCanBeAssignedAndTransferred() throws Exception {
        mockMvc.perform(post("/employees/5/assign")
                        .param("departmentId", "4")
                        .param("positionId", "5")
                        .param("startDate", "2025-02-01")
                        .param("note", "Повторный прием"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/5/history"))
                .andExpect(flash().attribute("success", "Назначение создано"));

        mockMvc.perform(post("/employees/4/assign")
                        .param("departmentId", "4")
                        .param("positionId", "5")
                        .param("startDate", "2025-03-01")
                        .param("note", "Перевод"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/4/history"))
                .andExpect(flash().attribute("success", "Служащий переведен на новую должность"));
    }

    @Test
    void positionCanBeAddedToDepartment() throws Exception {
        mockMvc.perform(post("/departments/5/positions")
                        .param("positionId", "5")
                        .param("slotsTotal", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/5"))
                .andExpect(flash().attribute("success", "Должность добавлена в подразделение"));
    }

    @Test
    void duplicateDepartmentPositionReturnsBusinessError() throws Exception {
        mockMvc.perform(post("/departments/5/positions")
                        .param("positionId", "3")
                        .param("slotsTotal", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/5"))
                .andExpect(flash().attribute("error", "Должность уже предусмотрена в подразделении"));
    }

    @Test
    void departmentPagesAndMutationsAreAvailable() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(content().string(containsString("Головной офис")));

        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(content().string(containsString("Добавление подразделения")));

        mockMvc.perform(post("/departments")
                        .param("name", "Архив")
                        .param("parentDepartmentId", "1")
                        .param("managerEmployeeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/6"))
                .andExpect(flash().attribute("success", "Подразделение добавлено"));

        mockMvc.perform(get("/departments/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(content().string(containsString("Редактирование подразделения")));

        mockMvc.perform(post("/departments/6")
                        .param("name", "Архив документов")
                        .param("parentDepartmentId", "1")
                        .param("managerEmployeeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/6"))
                .andExpect(flash().attribute("success", "Подразделение сохранено"));

        mockMvc.perform(post("/departments/6/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attribute("success", "Подразделение удалено"));

        mockMvc.perform(post("/departments")
                        .param("name", "Без руководителя")
                        .param("parentDepartmentId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/7"));

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Без руководителя")))
                .andExpect(content().string(containsString("Не назначен")));
    }

    @Test
    void departmentMutationErrorsAreShown() throws Exception {
        mockMvc.perform(get("/departments/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Подразделение не найдено: id=999")));

        mockMvc.perform(post("/departments").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attribute("error", "Название подразделения обязательно"));

        mockMvc.perform(post("/departments/1")
                        .param("name", "Головной офис")
                        .param("parentDepartmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attribute("error", "Подразделение не может быть головным для самого себя"));

        mockMvc.perform(post("/departments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/1"))
                .andExpect(flash().attribute("error", "Нельзя удалить подразделение с внутренними подразделениями"));
    }

    @Test
    void departmentPositionCanBeRemovedAndErrorsAreShown() throws Exception {
        mockMvc.perform(post("/departments/5/positions/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/5"))
                .andExpect(flash().attribute("success", "Должность удалена из подразделения"));

        mockMvc.perform(post("/departments/5/positions/4/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments/5"))
                .andExpect(flash().attribute("error", "Нельзя удалить должность из подразделения: есть активные назначения"));
    }

    @Test
    void positionPagesAndMutationsAreAvailable() throws Exception {
        mockMvc.perform(get("/positions"))
                .andExpect(status().isOk())
                .andExpect(view().name("positions/list"))
                .andExpect(content().string(containsString("Backend-разработчик")));

        mockMvc.perform(get("/positions/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("positions/form"))
                .andExpect(content().string(containsString("Добавление должности")));

        String longResponsibilities = "x".repeat(130);
        mockMvc.perform(post("/positions")
                        .param("name", "Инженер-программист")
                        .param("responsibilities", longResponsibilities))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/positions/6"))
                .andExpect(flash().attribute("success", "Должность добавлена"));

        mockMvc.perform(get("/positions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Инженер-программист")))
                .andExpect(content().string(containsString("...")));

        mockMvc.perform(get("/positions/6/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("positions/form"))
                .andExpect(content().string(containsString("Редактирование должности")));

        mockMvc.perform(post("/positions/6")
                        .param("name", "Ведущий инженер")
                        .param("responsibilities", "Разработка"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/positions/6"))
                .andExpect(flash().attribute("success", "Должность сохранена"));

        mockMvc.perform(post("/positions/6/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/positions"))
                .andExpect(flash().attribute("success", "Должность удалена"));
    }

    @Test
    void positionMutationErrorsAreShown() throws Exception {
        mockMvc.perform(get("/positions/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Должность не найдена: id=999")));

        mockMvc.perform(post("/positions")
                        .param("name", "")
                        .param("responsibilities", "Разработка"))
                .andExpect(status().isOk())
                .andExpect(view().name("positions/form"))
                .andExpect(model().attribute("error", "Название должности обязательно"));

        mockMvc.perform(post("/positions/999")
                        .param("name", "Инженер")
                        .param("responsibilities", "Разработка"))
                .andExpect(status().isOk())
                .andExpect(view().name("positions/form"))
                .andExpect(model().attribute("error", "Должность не найдена: id=999"));

        mockMvc.perform(post("/positions/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/positions/3"))
                .andExpect(flash().attribute("error", "Нельзя удалить должность с активными назначениями"));
    }

    @Test
    void departmentGraphUsesTreePreorder() throws Exception {
        MvcResult result = mockMvc.perform(get("/departments/graph"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/graph"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<DepartmentController.DepartmentNode> nodes =
                (List<DepartmentController.DepartmentNode>) result.getModelAndView().getModel().get("nodes");

        assertEquals("Головной офис", nodes.get(0).name());
        assertEquals(0, nodes.get(0).depth());
        assertEquals("IT департамент", nodes.get(1).name());
        assertEquals(1, nodes.get(1).depth());
        assertEquals("Разработка", nodes.get(2).name());
        assertEquals(2, nodes.get(2).depth());
        assertEquals("HR департамент", nodes.get(3).name());
        assertEquals(1, nodes.get(3).depth());
        assertEquals("Финансовый отдел", nodes.get(4).name());
        assertEquals(1, nodes.get(4).depth());
    }

    @Test
    void departmentGraphBuilderSkipsAlreadyVisitedNodes() {
        Department root = new Department();
        root.setId(901L);
        root.setName("Root without manager");

        Department child = new Department();
        child.setId(902L);
        child.setName("Child without manager");
        child.setParentDepartment(root);

        List<DepartmentController.DepartmentNode> nodes = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Map<Long, List<Department>> childrenByParentId = Map.of(901L, List.of(child));

        ReflectionTestUtils.invokeMethod(
                departmentController,
                "appendGraphNode",
                root,
                0,
                childrenByParentId,
                nodes,
                visited
        );
        ReflectionTestUtils.invokeMethod(
                departmentController,
                "appendGraphNode",
                root,
                0,
                childrenByParentId,
                nodes,
                visited
        );

        assertEquals(2, nodes.size());
        assertEquals("Не назначен", nodes.get(0).managerName());
        assertEquals(0, nodes.get(0).depth());
        assertEquals("Child without manager", nodes.get(1).name());
        assertEquals(1, nodes.get(1).depth());
    }

    @Test
    void departmentAndPositionPagesAreAvailable() throws Exception {
        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Головной офис")));

        mockMvc.perform(get("/positions/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend-разработчик")));
    }
}
