# E-Ink UI

`eink-ui` is the app's vendor-neutral Jetpack Compose design system for Android e-ink displays.

## Principles

- Every state is understandable in black and white. An optional accent reinforces focus and selection but never carries meaning alone.
- Components draw no ripple, elevation, shadow, or authored transition animation.
- Direct interaction remains intact, including scrolling, text entry, focus, and IME behavior.
- Controls use opaque colors, persistent outlines, and at least 48dp interaction targets.
- Public state is controlled by callers. Convenience state is limited to presentational disclosure.

## Theme

```kotlin
EinkTheme(accent = Color(0xFF546E7A)) {
    EinkButton(onClick = ::save, emphasis = EinkButtonEmphasis.Strong) {
        Text("Save")
    }
}
```

`EinkColors`, `EinkTypography`, `EinkSpacing`, `EinkBorders`, `EinkShapes`, and
`EinkLayoutTokens` can be replaced independently. The default theme is light-only.

## Components

- Actions: `EinkButton`, `EinkIconButton`, `EinkFloatingActionButton`, `EinkLink`
- Selection: `EinkCheckboxRow`, `EinkSwitchRow`, `EinkRadioGroup`, `EinkOptionGroup`
- Forms: `EinkTextField`, `EinkStepper`, `EinkColorChooser`
- Structure: `EinkSurface`, `EinkCard`, `EinkExpandableSection`
- Templates: `EinkSettingsScreen`, `EinkPickerDialog`, `EinkAdaptivePaneLayout`

Adaptive templates default to `EinkLayoutMode.Auto` and can be forced to
`SinglePane` or `TwoPane`. Callers own the active pane and all navigation state.
