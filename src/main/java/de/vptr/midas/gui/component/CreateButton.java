package de.vptr.midas.gui.component;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class CreateButton extends Button {
    private final static String DEFAULT_TOOLTIP = "Create";

    public CreateButton(final ComponentEventListener<ClickEvent<Button>> createAction, final String tooltipText) {
        super("", createAction);
        this.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        this.setIcon(LineAwesomeIcon.PLUS_SOLID.create());
        this.setTooltipText(tooltipText != null ? tooltipText : DEFAULT_TOOLTIP);
    }

    public CreateButton(final ComponentEventListener<ClickEvent<Button>> createAction) {
        this(createAction, null);
    }
}
