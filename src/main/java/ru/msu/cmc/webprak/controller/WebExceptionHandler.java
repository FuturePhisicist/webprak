package ru.msu.cmc.webprak.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(EntityNotFoundException exception, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
