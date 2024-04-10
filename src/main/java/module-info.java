module catrenet {
	requires com.install4j.runtime;

	requires transitive jloda_core;
	requires transitive jloda_fx;
	requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.web;
	requires commons.math3;

	exports catrenet.io;
	exports catrenet.algorithm;
	exports catrenet.main;
	exports catrenet.tab;
	exports catrenet.window;
	exports catrenet.model;
	exports catrenet.view;
	exports catrenet.action;
	exports catrenet.tools;
	exports catrenet.settings;
	exports catrenet.settings.displaylabels;

	opens catrenet.algorithm;
	opens catrenet.tab;
	opens catrenet.window;
	opens catrenet.settings;
	opens catrenet.settings.displaylabels;

	opens catrenet.dialog.exportlist;
	opens catrenet.dialog.targets;

	opens catrenet.resources.images;
	opens catrenet.resources.icons;

	exports catrenet.dialog;
}