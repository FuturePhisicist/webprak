package ru.msu.cmc.webprak.controller.form;

import ru.msu.cmc.webprak.model.EducationLevel;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;

import java.time.LocalDate;

public class EmployeeForm {
    private String lastName;
    private String firstName;
    private String middleName;
    private String homeAddress;
    private EducationLevel education;
    private LocalDate hireDate;
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    public static EmployeeForm from(Employee employee) {
        EmployeeForm form = new EmployeeForm();
        form.setLastName(employee.getLastName());
        form.setFirstName(employee.getFirstName());
        form.setMiddleName(employee.getMiddleName());
        form.setHomeAddress(employee.getHomeAddress());
        form.setEducation(employee.getEducation());
        form.setHireDate(employee.getHireDate());
        form.setStatus(employee.getStatus());
        return form;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public EducationLevel getEducation() {
        return education;
    }

    public void setEducation(EducationLevel education) {
        this.education = education;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
}
