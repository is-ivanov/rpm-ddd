package by.iivanov.rpm.shared.infrastructure.web;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * A controller advice that applies to all controllers in the application, designed to handle
 * trimming of string input parameters to help prevent issues caused by leading or trailing whitespace.
 *
 * <p>The class customizes the data binding process by registering a {@link StringTrimmerEditor},
 * which converts empty strings or strings consisting only of whitespace into null values.
 * This ensures that input processing throughout the application conforms to a consistent format.
 */
@ControllerAdvice
class StringTrimmerControllerAdvice {

    @InitBinder
    void initBinder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
