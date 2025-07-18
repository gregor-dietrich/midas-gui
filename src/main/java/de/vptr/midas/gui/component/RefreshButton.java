package de.vptr.midas.gui.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class RefreshButton extends Button {
    private final static String DEFAULT_TOOLTIP = "Refresh";

    public RefreshButton(final ComponentEventListener<ClickEvent<Button>> refreshAction, final String tooltipText) {
        super("", refreshAction);
        this.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.setIcon(new Icon(VaadinIcon.REFRESH));
        this.setTooltipText(tooltipText != null ? tooltipText : DEFAULT_TOOLTIP);
    }

    public RefreshButton(final ComponentEventListener<ClickEvent<Button>> refreshAction) {
        this(refreshAction, null);
    }
}
