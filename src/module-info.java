module catlynet {
    requires transitive jloda;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires fx.platform.utils;
    requires com.install4j.runtime;

    exports catlynet.main;
    exports catlynet.window;

    opens catlynet.window;
    opens catlynet.resources.images;
    opens catlynet.resources.icons;
}