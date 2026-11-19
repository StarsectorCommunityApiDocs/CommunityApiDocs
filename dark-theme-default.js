// Show the dark theme to visitors who haven't picked a theme with the theme button.
// Javadoc's own default is light. Added to every page with javadoc's --add-script option,
// which loads it before the page applies the saved theme.
try {
    if (!localStorage.getItem("theme")) {
        localStorage.setItem("theme", "theme-dark");
    }
} catch (e) {
    // localStorage can be blocked, for example by privacy settings. Javadoc's light theme is used then.
}
