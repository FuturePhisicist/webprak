package ru.msu.cmc.webprak.controller.form;

import ru.msu.cmc.webprak.model.Position;

public class PositionForm {
    private String name;
    private String responsibilities;

    public static PositionForm from(Position position) {
        PositionForm form = new PositionForm();
        form.setName(position.getName());
        form.setResponsibilities(position.getResponsibilities());
        return form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }
}
