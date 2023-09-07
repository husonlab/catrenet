/*
 * CatlyNet.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package catlynet.main;

import catlynet.window.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jloda.fx.util.*;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.UsageException;

import java.io.File;
import java.time.Duration;

/**
 * runs the CatlyNet program
 * Daniel Huson, 2020
 */
public class CatlyNet extends Application {
    private static ArgsOptions options;
    private static String[] inputFilesAtStartup;

    @Override
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(ProgramProperties::store));
        ProgramProperties.setUseGUI(true);

        ColorSchemeManager.BuiltInColorTables = new String[]{"Alhambra;6;0X4d66cc;0Xb3e6e6;0Xcc9933;0X669966;0X666666;0X994d00;" +
                                                             "Caspian8;8;0Xf64d1b;0X8633bc;0X41a744;0X747474;0X2746bc;0Xff9301;0Xc03150;0X2198bc;" +
                                                             "Fews8;8;0X5da6dc;0Xfba53a;0X60be68;0Xf27db0;0Xb39230;0Xb376b2;0Xdfd040;0Xf15954;" +
                                                             "Pairs12;12;0X267ab2;0Xa8cfe3;0X399f34;0Xb4df8e;0Xe11f27;0Xfa9b9b;0Xfe7f23;0Xfcbf75;0X6a4199;0Xcab3d6;0Xb05a2f;0Xffff9f;" +
                                                             "Pale12;12;0Xdbdada;0Xf27e75;0Xba7bbd;0Xceedc5;0Xfbf074;0Xf8cbe5;0Xf9b666;0Xfdffb6;0X86b0d2;0X95d6c8;0Xb3e46c;0Xbfb8da;" +
                                                             "Rainbow13;13;0Xed1582;0Xf73e43;0Xee8236;0Xe5ae3d;0Xe5da45;0Xa1e443;0X22da27;0X21d18e;0X21c8c7;0X1ba2fc;0X2346fb;0X811fd9;0X9f1cc5;" +
                                                             "Retro29;29;0Xf4d564;0X97141d;0Xe9af6b;0X82ae92;0X356c7c;0X5c8c83;0X3a2b27;0Xe28b90;0X242666;0Xc2a690;0Xb80614;0X35644f;0Xe3a380;0Xb9a253;0X72a283;0X73605b;0X94a0ad;0Xf7a09d;0Xe5c09e;0X4a4037;0Xcec07c;0X6c80bb;0X7fa0a4;0Xb9805b;0Xd5c03f;0Xdd802e;0X8b807f;0Xc42030;0Xc2603d;" +
                                                             "Sea9;9;0Xffffdb;0Xedfbb4;0Xc9ecb6;0X88cfbc;0X56b7c4;0X3c90bf;0X345aa7;0X2f2b93;0X121858;"};
    }

    /**
     * main
     *
     */
    public static void main(String[] args) throws CanceledException, UsageException {
        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();

        ResourceManagerFX.addResourceRoot(CatlyNet.class, "catlynet.resources");
        ProgramProperties.getProgramIconsFX().addAll(ResourceManagerFX.getIcons("CatlyNet-16.png", "CatlyNet-32.png", "CatlyNet-48.png", "CatlyNet-64.png", "CatlyNet-128.png", "CatlyNet-256.png", "CatlyNet-512.png"));

        ProgramProperties.setProgramName(Version.NAME);
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        ProgramProperties.setProgramLicence("""
                Copyright (C) 2023. his program comes with ABSOLUTELY NO WARRANTY.
                This is free software, licensed under the terms of the GNU General Public License, Version 3.
                Sources available at: https://github.com/husonlab/catlynet
                """);
        SplashScreen.setVersionString(ProgramProperties.getProgramVersion());
        SplashScreen.setImageResourceName("splash.png");

        try {
            parseArguments(args);
        } catch (Throwable th) {
            //catch any exceptions and the like that propagate up to the top level
            if (!th.getMessage().startsWith("Help")) {
                System.err.println("Fatal error:" + "\n" + th);
                Basic.caught(th);
            }
            System.exit(1);
        }

        launch(args);
    }

    protected static void parseArguments(String[] args) throws CanceledException, UsageException {
        options = new ArgsOptions(args, CatlyNet.class, Version.NAME + " - Auto catalytic networks");
        options.setAuthors("Daniel H. Huson and Mike A. Steel");
        options.setLicense(ProgramProperties.getProgramLicence());
        options.setVersion(ProgramProperties.getProgramVersion());

        options.comment("Input:");
        inputFilesAtStartup = options.getOption("-i", "input", "Input file(s)", new String[0]);

        options.comment(ArgsOptions.OTHER);

        final var propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", getDefaultPropertiesFile());
        final var showVersion = options.getOption("-V", "version", "Show version string", false);
        final var silentMode = options.getOption("-S", "silentMode", "Silent mode", false);
        ProgramExecutorService.setNumberOfCoresToUse(options.getOption("-t", "threads", "Maximum number of threads to use in a parallel algorithm (0=all available)", 0));
        ProgramProperties.setConfirmQuit(options.getOption("-q", "confirmQuit", "Confirm quit on exit", ProgramProperties.isConfirmQuit()));
        ProgramProperties.put("MaxNumberRecentFiles", 100);
        options.done();

        ProgramProperties.load(propertiesFile);

        if (silentMode) {
            Basic.stopCollectingStdErr();
            Basic.hideSystemErr();
            Basic.hideSystemOut();
        }

        if (showVersion) {
            System.err.println(ProgramProperties.getProgramVersion());
            System.err.println(jloda.util.Version.getVersion(CatlyNet.class, ProgramProperties.getProgramName()));
            System.err.println("Java version: " + System.getProperty("java.version"));
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SplashScreen.showSplash(Duration.ofSeconds(5));
        try {
            primaryStage.setTitle("Untitled - " + ProgramProperties.getProgramName());
            NotificationManager.setShowNotifications(false);

            final var mainWindow = new MainWindow();

            final var windowGeometry = new WindowGeometry(ProgramProperties.get("WindowGeometry", "50 50 800 800"));

            mainWindow.show(primaryStage, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
            MainWindowManager.getInstance().addMainWindow(mainWindow);

            for (var fileName : inputFilesAtStartup) {
                Platform.runLater(() -> FileOpenManager.getFileOpener().accept(fileName));
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            throw ex;
        }
    }

    @Override
    public void stop() {
        ProgramProperties.store();
        System.exit(0);
    }

    public static String getDefaultPropertiesFile() {
        if (ProgramProperties.isMacOS())
            return System.getProperty("user.home") + "/Library/Preferences/CatlyNet.def";
        else
            return System.getProperty("user.home") + File.separator + ".CatlyNet.def";
    }

    public static ArgsOptions getOptions() {
        return options;
    }
}
