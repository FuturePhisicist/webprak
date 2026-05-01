package ru.msu.cmc.webprak.controller.form;

public class DepartmentPositionForm {
    private Long positionId;
    private Integer slotsTotal = 1;

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Integer getSlotsTotal() {
        return slotsTotal;
    }

    public void setSlotsTotal(Integer slotsTotal) {
        this.slotsTotal = slotsTotal;
    }
}
