package de.vptr.midas.gui.component;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.button.Button;

import de.vptr.midas.gui.service.ThemeService;

public class ThemeToggleButton extends Button {

    private final ThemeService themeService;

    public ThemeToggleButton(final ThemeService themeService) {
        this.themeService = themeService;
        this.addClickListener(e -> this.toggleTheme());
        this.updateButton();
    }

    private void toggleTheme() {
        final var nextTheme = this.themeService.getNextTheme();
        this.themeService.setTheme(nextTheme);
        this.updateButton();
    }

    private void updateButton() {
        final var currentTheme = this.themeService.getCurrentTheme();
        switch (currentTheme) {
            case LIGHT:
                this.setIcon(LineAwesomeIcon.SUN_SOLID.create());
                this.setTooltipText("Switch to Dark Theme");
                break;
            case DARK:
                this.setIcon(LineAwesomeIcon.MOON_SOLID.create());
                this.setTooltipText("Switch to System Theme");
                break;
            case SYSTEM:
                this.setIcon(LineAwesomeIcon.DESKTOP_SOLID.create());
                this.setTooltipText("Switch to Light Theme");
                break;
        }
    }
}
