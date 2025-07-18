package de.vptr.midas.gui.component;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class DeleteButton extends Button {
    private final static String DEFAULT_TOOLTIP = "Delete";

    public DeleteButton(final ComponentEventListener<ClickEvent<Button>> deleteAction, final String tooltipText) {
        super("", deleteAction);
        this.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        this.setIcon(LineAwesomeIcon.TRASH_ALT_SOLID.create());
        this.setTooltipText(tooltipText != null ? tooltipText : DEFAULT_TOOLTIP);
    }

    public DeleteButton(final ComponentEventListener<ClickEvent<Button>> deleteAction) {
        this(deleteAction, null);
    }
}
