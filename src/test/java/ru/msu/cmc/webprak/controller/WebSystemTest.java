package ru.msu.cmc.webprak.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.msu.cmc.webprak.BaseIntegrationTest;

import java.util.List;

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
    void departmentAndPositionPagesAreAvailable() throws Exception {
        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Головной офис")));

        mockMvc.perform(get("/positions/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend-разработчик")));
    }
}
