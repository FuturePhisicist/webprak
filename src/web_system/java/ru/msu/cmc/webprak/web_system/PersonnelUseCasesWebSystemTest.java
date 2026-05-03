package ru.msu.cmc.webprak.web_system;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonnelUseCasesWebSystemTest extends BaseWebSystemTest {
    private static final String SESSION_ID_SUFFIX = "(;jsessionid=[A-Z0-9]+)?";

    @Test
    void userCanNavigateSearchAndOpenEmployeeHistory() {
        page.navigate(url("/"));

        assertThat(page.locator("body")).containsText("Система информации о персонале");

        page.getByText("Все служащие").click();
        assertThat(page).hasURL(Pattern.compile(".*/employees$"));

        page.locator("#q").fill("Иванов");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Иванов Иван Иванович");
        assertThat(page.locator("body")).not().containsText("Сидорова Анна Олеговна");

        page.getByText("Иванов Иван Иванович").click();
        assertThat(page.locator("body")).containsText("Текущее назначение");

        page.getByText("История служащего").click();
        assertThat(page.locator("body")).containsText("Старший HR");
        assertThat(page.locator("body")).containsText("Приём в HR департамент");
    }

    @Test
    void userSeesValidationErrorAndThenCreatesEmployee() {
        page.navigate(url("/"));
        page.getByText("Добавить служащего").click();

        page.locator("#lastName").fill("Яковлев");
        page.locator("#firstName").fill("Яков");
        page.locator("#middleName").fill("Яковлевич");
        page.locator("#hireDate").fill("2026-01-20");
        page.locator("#education").selectOption("MASTER");
        page.locator("#status").selectOption("ACTIVE");
        page.locator("#homeAddress").fill(" ");
        page.locator("button[type=submit]").click();

        assertThat(page.locator("body")).containsText("Домашний адрес обязателен");

        page.navigate(url("/employees/new"));
        page.locator("#lastName").fill("Яковлев");
        page.locator("#firstName").fill("Яков");
        page.locator("#middleName").fill("Яковлевич");
        page.locator("#hireDate").fill("2026-01-20");
        page.locator("#education").selectOption("MASTER");
        page.locator("#status").selectOption("ACTIVE");
        page.locator("#homeAddress").fill("Moscow, Test street 1");
        page.locator("button[type=submit]").click();

        assertThat(page).hasURL(Pattern.compile(".*/employees/6" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Служащий добавлен");
        assertThat(page.locator("body")).containsText("Яковлев Яков Яковлевич");
        assertThat(page.locator("body")).containsText("Текущее назначение: нет");
    }

    @Test
    void userCanCreatePositionAddItToDepartmentAndAssignEmployee() {
        page.navigate(url("/"));
        page.getByText("Добавить должность").click();

        page.locator("#name").fill("Инженер-программист");
        page.locator("#responsibilities").fill("Разработка прикладного ПО");
        page.locator("button[type=submit]").click();

        assertThat(page).hasURL(Pattern.compile(".*/positions/6" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Должность добавлена");

        page.navigate(url("/departments/5"));
        page.locator("#positionId").selectOption("6");
        page.locator("#slotsTotal").fill("1");
        page.locator("form.toolbar button[type=submit]").click();

        assertThat(page.locator("body")).containsText("Должность добавлена в подразделение");
        assertThat(page.locator("body")).containsText("Инженер-программист");

        page.navigate(url("/employees/5/assign"));
        page.locator("#departmentId").selectOption("5");
        page.locator("#positionId").selectOption("6");
        page.locator("#startDate").fill("2026-02-01");
        page.locator("#note").fill("Назначение через системный тест");
        page.locator("button[type=submit]").click();

        assertThat(page).hasURL(Pattern.compile(".*/employees/5/history" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Назначение создано");
        assertThat(page.locator("body")).containsText("Инженер-программист");
        assertThat(page.locator("body")).containsText("Назначение через системный тест");
    }

    @Test
    void userGetsBusinessErrorsForDuplicateDepartmentPositionAndBusySlot() {
        page.navigate(url("/departments/5"));
        page.locator("#positionId").selectOption("3");
        page.locator("#slotsTotal").fill("1");
        page.locator("form.toolbar button[type=submit]").click();

        assertThat(page.locator("body")).containsText("Должность уже предусмотрена в подразделении");

        page.navigate(url("/employees/5/assign"));
        page.locator("#departmentId").selectOption("1");
        page.locator("#positionId").selectOption("1");
        page.locator("#startDate").fill("2026-02-01");
        page.locator("button[type=submit]").click();

        assertThat(page.locator("body")).containsText("Нет свободных ставок для departmentId=1, positionId=1");
        assertThat(page).hasURL(Pattern.compile(".*/employees/5/assign" + SESSION_ID_SUFFIX + "$"));
    }

    @Test
    void userCanInspectDepartmentGraphAndOpenNestedDepartment() {
        page.navigate(url("/departments/graph"));

        String text = page.locator(".graph").innerText();
        int headOffice = text.indexOf("Головной офис");
        int it = text.indexOf("IT департамент");
        int development = text.indexOf("Разработка");
        int hr = text.indexOf("HR департамент");

        assertTrue(headOffice >= 0);
        assertTrue(it > headOffice);
        assertTrue(development > it);
        assertTrue(hr > development);

        page.getByText("Разработка").click();
        assertThat(page).hasURL(Pattern.compile(".*/departments/5" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Должности в подразделении");
        assertThat(page.locator("body")).containsText("QA-инженер");
    }

    @Test
    void userCanEditAndDeletePositionThroughButtons() {
        page.navigate(url("/positions/new"));
        page.locator("#name").fill("Временная должность");
        page.locator("#responsibilities").fill("Временные обязанности");
        page.locator("button[type=submit]").click();
        assertThat(page).hasURL(Pattern.compile(".*/positions/6" + SESSION_ID_SUFFIX + "$"));

        page.getByText("Редактировать").click();
        assertThat(page.locator("body")).containsText("Редактирование должности");

        page.locator("#name").fill("Временная должность 2");
        page.locator("#responsibilities").fill("Обновленные обязанности");
        page.locator("button[type=submit]").click();

        assertThat(page.locator("body")).containsText("Должность сохранена");
        assertThat(page.locator("body")).containsText("Временная должность 2");

        page.locator("form[action*='/positions/6'][action*='delete'] button").click();
        assertThat(page).hasURL(Pattern.compile(".*/positions" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Должность удалена");
        assertThat(page.locator("body")).not().containsText("Временная должность 2");
    }

    @Test
    void employeeUseCasesHaveSeparateSuccessAndErrorResults() {
        page.navigate(url("/employees/new"));
        fillEmployeeForm(" ", "Иван", "Иванович", "Moscow", "2026-01-10");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Фамилия обязательна");

        page.navigate(url("/employees/new"));
        fillEmployeeForm("Иванов", " ", "Иванович", "Moscow", "2026-01-10");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Имя обязательно");

        page.navigate(url("/employees/new"));
        fillEmployeeForm("Иванов", "Иван", "Иванович", " ", "2026-01-10");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Домашний адрес обязателен");

        page.navigate(url("/employees/new"));
        fillEmployeeForm("Петров", "Петр", "Петрович", "Moscow", "2026-01-10");
        page.locator("button[type=submit]").click();
        assertThat(page).hasURL(Pattern.compile(".*/employees/6" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Служащий добавлен");

        page.getByText("Редактировать").click();
        fillEmployeeForm("Петров", "Петр", "Сергеевич", "Moscow updated", "2026-01-10");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Служащий сохранен");
        assertThat(page.locator("body")).containsText("Петров Петр Сергеевич");

        page.locator("form[action*='/employees/6'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Служащий удален");
        assertThat(page.locator("body")).not().containsText("Петров Петр Сергеевич");

        page.navigate(url("/employees/1"));
        page.locator("form[action*='/employees/1'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Нельзя удалить сотрудника с активным назначением");
    }

    @Test
    void departmentUseCasesHaveSeparateSuccessAndErrorResults() {
        page.navigate(url("/departments/new"));
        page.locator("#name").fill(" ");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Название подразделения обязательно");

        page.navigate(url("/departments/new"));
        fillDepartmentForm("Архив", "1", "1");
        page.locator("button[type=submit]").click();
        assertThat(page).hasURL(Pattern.compile(".*/departments/6" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Подразделение добавлено");

        page.getByText("Редактировать").click();
        fillDepartmentForm("Архив документов", "1", "2");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Подразделение сохранено");
        assertThat(page.locator("body")).containsText("Архив документов");

        page.locator("form[action*='/departments/6'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Подразделение удалено");
        assertThat(page.locator("body")).not().containsText("Архив документов");

        page.navigate(url("/departments/1"));
        page.locator("form[action*='/departments/1/delete'] button").click();
        assertThat(page.locator("body")).containsText("Нельзя удалить подразделение с внутренними подразделениями");

        page.navigate(url("/departments/5"));
        page.locator("form[action*='/departments/5/delete'] button").click();
        assertThat(page.locator("body")).containsText("Нельзя удалить подразделение с активными сотрудниками");
    }

    @Test
    void positionUseCasesHaveSeparateSuccessAndErrorResults() {
        page.navigate(url("/positions/new"));
        page.locator("#name").fill(" ");
        page.locator("#responsibilities").fill("Описание");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Название должности обязательно");

        page.navigate(url("/positions/new"));
        page.locator("#name").fill("Системный аналитик");
        page.locator("#responsibilities").fill(" ");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Описание обязанностей обязательно");

        page.navigate(url("/positions/new"));
        fillPositionForm("Системный аналитик", "Анализ требований");
        page.locator("button[type=submit]").click();
        assertThat(page).hasURL(Pattern.compile(".*/positions/6" + SESSION_ID_SUFFIX + "$"));
        assertThat(page.locator("body")).containsText("Должность добавлена");

        page.getByText("Редактировать").click();
        fillPositionForm("Старший системный аналитик", "Анализ и постановка задач");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Должность сохранена");
        assertThat(page.locator("body")).containsText("Старший системный аналитик");

        page.locator("form[action*='/positions/6'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Должность удалена");
        assertThat(page.locator("body")).not().containsText("Старший системный аналитик");

        page.navigate(url("/positions/3"));
        page.locator("form[action*='/positions/3'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Нельзя удалить должность с активными назначениями");
    }

    @Test
    void departmentPositionUseCasesHaveSeparateSuccessAndErrorResults() {
        page.navigate(url("/departments/5"));
        page.locator("#positionId").selectOption("5");
        page.locator("#slotsTotal").fill("1");
        page.locator("form.toolbar button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Должность добавлена в подразделение");
        assertThat(page.locator("body")).containsText("Финансовый аналитик");

        page.locator("form[action*='/departments/5/positions/5'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Должность удалена из подразделения");

        page.locator("#positionId").selectOption("3");
        page.locator("#slotsTotal").fill("1");
        page.locator("form.toolbar button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Должность уже предусмотрена в подразделении");

        page.locator("form[action*='/departments/5/positions/4'][action*='delete'] button").click();
        assertThat(page.locator("body")).containsText("Нельзя удалить должность из подразделения: есть активные назначения");
    }

    @Test
    void assignmentUseCasesHaveSeparateSuccessAndErrorResults() {
        page.navigate(url("/employees/5/assign"));
        fillAssignmentForm("4", "5", "2026-02-01", "Повторный прием");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Назначение создано");
        assertThat(page.locator("body")).containsText("Повторный прием");

        page.navigate(url("/employees/4/assign"));
        fillAssignmentForm("4", "5", "2026-03-01", "Перевод в финансы");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Служащий переведен на новую должность");
        assertThat(page.locator("body")).containsText("Перевод в финансы");

        page.navigate(url("/employees/5/assign"));
        fillAssignmentForm("1", "1", "2026-03-01", "Занятая ставка");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Нет свободных ставок для departmentId=1, positionId=1");

        page.navigate(url("/employees/5/assign"));
        fillAssignmentForm("3", "5", "2026-03-01", "Нет должности в подразделении");
        page.locator("button[type=submit]").click();
        assertThat(page.locator("body")).containsText("Для подразделения id=3 не предусмотрена должность id=5");
    }

    @Test
    void notFoundPagesShowSeparateMessages() {
        page.navigate(url("/employees/999"));
        assertThat(page.locator("body")).containsText("Сотрудник не найден: id=999");

        page.navigate(url("/departments/999"));
        assertThat(page.locator("body")).containsText("Подразделение не найдено: id=999");

        page.navigate(url("/positions/999"));
        assertThat(page.locator("body")).containsText("Должность не найдена: id=999");
    }

    private void fillEmployeeForm(String lastName,
                                  String firstName,
                                  String middleName,
                                  String homeAddress,
                                  String hireDate) {
        page.locator("#lastName").fill(lastName);
        page.locator("#firstName").fill(firstName);
        page.locator("#middleName").fill(middleName);
        page.locator("#hireDate").fill(hireDate);
        page.locator("#education").selectOption("MASTER");
        page.locator("#status").selectOption("ACTIVE");
        page.locator("#homeAddress").fill(homeAddress);
    }

    private void fillDepartmentForm(String name, String parentDepartmentId, String managerEmployeeId) {
        page.locator("#name").fill(name);
        page.locator("#parentDepartmentId").selectOption(parentDepartmentId);
        page.locator("#managerEmployeeId").selectOption(managerEmployeeId);
    }

    private void fillPositionForm(String name, String responsibilities) {
        page.locator("#name").fill(name);
        page.locator("#responsibilities").fill(responsibilities);
    }

    private void fillAssignmentForm(String departmentId, String positionId, String startDate, String note) {
        page.locator("#departmentId").selectOption(departmentId);
        page.locator("#positionId").selectOption(positionId);
        page.locator("#startDate").fill(startDate);
        page.locator("#note").fill(note);
    }
}
