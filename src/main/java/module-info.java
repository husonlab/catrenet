module bioraf {
	requires com.install4j.runtime;

	requires transitive jloda2;
	requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.web;
	requires commons.math3;

	exports bioraf.algorithm;
	exports bioraf.main;
	exports bioraf.tab;
	exports bioraf.window;
	exports bioraf.model;
	exports bioraf.view;
	exports bioraf.action;
	exports bioraf.tools;
	exports bioraf.settings;

	opens bioraf.algorithm;
	opens bioraf.tab;
	opens bioraf.window;
	opens bioraf.settings;
	opens bioraf.dialog.exportlist;
	opens bioraf.dialog.targets;


	opens bioraf.resources.images;
	opens bioraf.resources.icons;

	exports bioraf;
	exports bioraf.dialog;
}