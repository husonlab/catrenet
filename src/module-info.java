module catlynet {
    requires transitive jloda2;
	requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires com.install4j.runtime;

    exports catlynet.algorithm;
    exports catlynet.main;
    exports catlynet.tab;
    exports catlynet.window;
    exports catlynet.model;
    exports catlynet.view;
    exports catlynet.action;
    exports catlynet.tools;

    opens catlynet.algorithm;

    opens catlynet.tab;
    opens catlynet.window;
    opens catlynet.format;
    opens catlynet.vformat;

    opens catlynet.dialog.exportlist;

    opens catlynet.resources.images;
    opens catlynet.resources.icons;
}