module catanets {
    requires transitive jloda;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires fx.platform.utils;

    exports catylnet.main;
    exports catylnet.window;

    opens catylnet.window;


}