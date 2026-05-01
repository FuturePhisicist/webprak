package ru.msu.cmc.webprak.controller.form;

import ru.msu.cmc.webprak.model.Department;

public class DepartmentForm {
    private String name;
    private Long parentDepartmentId;
    private Long managerEmployeeId;

    public static DepartmentForm from(Department department) {
        DepartmentForm form = new DepartmentForm();
        form.setName(department.getName());
        form.setParentDepartmentId(department.getParentDepartment() == null ? null : department.getParentDepartment().getId());
        form.setManagerEmployeeId(department.getManager() == null ? null : department.getManager().getId());
        return form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(Long parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public Long getManagerEmployeeId() {
        return managerEmployeeId;
    }

    public void setManagerEmployeeId(Long managerEmployeeId) {
        this.managerEmployeeId = managerEmployeeId;
    }
}
