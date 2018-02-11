package com.axzae.homeassistant.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.axzae.homeassistant.BuildConfig;
import com.axzae.homeassistant.R;
import com.axzae.homeassistant.util.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Changelog {

    public static Changelog getLatest() {
        Changelog result = items.get(BuildConfig.VERSION_CODE);
        if (result == null) {
            throw new RuntimeException("VERSION CODE: " + BuildConfig.VERSION_CODE + " NOT FOUND");
        }
        return result;
    }

    public static ArrayList<Changelog> getItems() {
        ArrayList<Changelog> results = new ArrayList<>(items.values());
        Collections.sort(results, new Comparator<Changelog>() {
            @Override
            public int compare(Changelog lhs, Changelog rhs) {
                return rhs.versionCode - lhs.versionCode; //descending order
            }
        });

        return results;
    }

    private static final HashMap<Integer, Changelog> items;

    static {
        items = new HashMap<>();

        Changelog change32d = new Changelog(56, "Release v3.2d", "09 Dec 2017");
        change32d.logs.add("Commands will now send via websocket if possible. Else it will fallback to REST API approach.");
        change32d.logs.add("yR sensor can now show weather icon correctly.");
        change32d.logs.add("Revamped how data are being synced in the whole application (it was a mess and it still is).");
        change32d.logs.add("Resolved widget sync issue (I hope? Let me know in the comment)");
        change32d.logs.add("Sorry for not replying emails. I'm quite busy on my personal life atm and also have hectic weeks ahead. Unless there are some very urgent and small changes, there will be no more updates for 2017. Happy Holidays, guys!");
        items.put(change32d.versionCode, change32d);

        Changelog change32c = new Changelog(55, "Release v3.2c", "03 Dec 2017");
        change32c.logs.add("Websocket support for widget. (Hope this can keep toggling state in sync)");
        change32c.logs.add("Fixed crash reported by Christian Löffler.");
        items.put(change32c.versionCode, change32c);

        Changelog change32b = new Changelog(54, "Release v3.2b", "03 Dec 2017");
        change32b.logs.add("Feature: Icon in Widget! Also, I tried my best to make the widget smaller.");
        change32b.logs.add("Fixed toggle bug in widget caused by code refactoring. Thanks Aleksandr Ivanov!");
        items.put(change32b.versionCode, change32b);

        Changelog change32 = new Changelog(53, "Release v3.2", "02 Dec 2017");
        change32.logs.add("Added support for Sun, Fan, Cover, and Vacuum. ");
        change32.logs.add("Shuffled the arrangement a little for the sensor tile. Let me know if you prefer the older look.");
        change32.logs.add("Minor fix for climate graph.");
        items.put(change32.versionCode, change32);

        Changelog change31b = new Changelog(52, "Release v3.1b", "28 Nov 2017");
        change31b.logs.add("Prevent crash in Map when you do not have zones or device trackers configured.");
        change31b.logs.add("Added intent flags to fix widget crashing in Mashmallow and lower.");
        change31b.logs.add("More compact row in group card.");
        change31b.logs.add("Other minor bug fixes.");
        items.put(change31b.versionCode, change31b);

        Changelog change31 = new Changelog(51, "Release v3.1", "26 Nov 2017");
        change31.logs.add("New Feature: Map. Support plotting of zones and live location update for device trackers with longitude and latitude values. (Let me know if you think something is missing)");
        items.put(change31.versionCode, change31);

        Changelog change30b = new Changelog(50, "Release v3.0b", "25 Nov 2017");
        change30b.logs.add("Fixed a bug where entity is not clickable in widget or missing in group card if it's not already exists in one of the dashboards outside.");
        items.put(change30b.versionCode, change30b);

        Changelog change30 = new Changelog(49, "Release v3.0", "25 Nov 2017");
        change30.logs.add("Updated group card to support interaction with all the controls");
        change30.logs.add("Allows 1x1 widget. (If it can't fit, you get the idea, it just doesn't work on 1x1)");
        items.put(change30.versionCode, change30);

        Changelog change29c = new Changelog(47, "Release v2.9c", "21 Nov 2017");
        change29c.logs.add("Fixed widget on Oreo. Thanks Nick Latocha for helping out on the testing!");
        change29c.logs.add("Fixed widget stuck after first toggle.");
        items.put(change29c.versionCode, change29c);

        Changelog change29 = new Changelog(45, "Release v2.9", "19 Nov 2017");
        change29.logs.add("Support Scene and Alarm ControlPanel components.");
        change29.logs.add("Changed async task to use ThreadPool Executor.");
        change29.logs.add("Minor UI fixes.");
        items.put(change29.versionCode, change29);

        Changelog change28g = new Changelog(44, "Release v2.8g", "18 Nov 2017");
        change28g.logs.add("Support device class for binary sensor. (let me know if any values doesn't display correctly)");
        change28g.logs.add("Fix: Toggling mode in widget was broken since v2.6.");
        change28g.logs.add("Updated widget to support all the remaining dialogs.");
        change28g.logs.add("Inverted card color for sensor, device tracker, and sun. (Experimenting with UI. will revert if majority of the feedbacks are negative.)");
        items.put(change28g.versionCode, change28g);

        Changelog change28f = new Changelog(43, "Release v2.8f", "17 Nov 2017");
        change28f.logs.add("Tons of data loading optimizations. Animations should be smoother now.");
        change28f.logs.add("Switched tile to card. (Not sure if it's better, let me know if you prefer the old look)");
        change28f.logs.add("Fixed a bug where the same entity can only exists in only one group at a time.");
        change28f.logs.add("Pre sorting of entities during bootstrapping. (sensor, followed by device tracker, remaining kind of sensors, lastly by domain, alphabetically)");
        items.put(change28f.versionCode, change28f);

        Changelog change28d = new Changelog(41, "Release v2.8d", "15 Nov 2017");
        change28d.logs.add("Fix websocket and camera failing issue caused by baseUrl ending with slash.");
        change28d.logs.add("Added icon and state for device tracker and sun.");
        change28d.logs.add("Pattern checking for input_text.");
        items.put(change28d.versionCode, change28d);

        Changelog change28c = new Changelog(40, "Release v2.8c", "13 Nov 2017");
        change28c.logs.add("Edit Dashboard to follow the Front Dashboard look.");
        change28c.logs.add("Fix: Edit Dashboard sorting will now work correctly. This is the mother of all fixes! sorry for taking long :P");
        change28c.logs.add("Added debugging code to troubleshoot camera connection error happening to some users.");
        items.put(change28c.versionCode, change28c);

        Changelog change28b = new Changelog(39, "Release v2.8b", "12 Nov 2017");
        change28b.logs.add("A better input_text dialog.");
        change28b.logs.add("Fix: Tiles in search result don't get updated. (quite an annoying behavior! surprisingly no one raised this to me)");
        change28b.logs.add("Fix: input_datetime crashes when there's no values initially.");
        change28b.logs.add("Separate okHttp instance for each Websocket, RestAPI, and GlideApp. (hopefully this can reduces Protocol Exception error)");
        change28b.logs.add("Fix: No graph on certain sensors reported by Alexey Ivanov (caused by sensor without milisecond in timestamp)");
        change28b.logs.add("Fix: Crash reported by Paul Gallagher");
        change28b.logs.add("Turned off cubic curve on graph.");
        items.put(change28b.versionCode, change28b);

        Changelog change28 = new Changelog(38, "Release v2.8", "12 Nov 2017");
        change28.logs.add("Feature: Support input_text and input_datetime components.");
        change28.logs.add("Support input_number. (was supporting input_slider previously and did not aware of HA 0.55 changed the component name to input_number)");
        change28.logs.add("Support default_view group. (will override Home tab if found)");
        change28.logs.add("Fix: Animation behavior of the toolbar and bottom navigation.");
        change28.logs.add("Fix for Oreo Widget crashing? (changed to use JobIntentService. will continue monitoring)");
        change28.logs.add("Added a button to start websocket in the drawer.");
        change28.logs.add("Slightly tweaked Logbook interface to give it a more 'material' look.");
        items.put(change28.versionCode, change28);

        Changelog change27 = new Changelog(37, "Release v2.7", "11 Nov 2017");
        change27.logs.add("Feature: Card for Group Component (Beta). Can't perform action on row yet. Useful at the moment for checking sensors states.");
        change27.logs.add("Retractable toolbar and bottom navigation, allowing more spaces for tiles.");
        items.put(change27.versionCode, change27);

        Changelog change26e = new Changelog(36, "Release v2.6e", "09 Nov 2017");
        change26e.logs.add("Conform to mdi icon in customizes.yaml file. (You can now have custom icon for your switches, automations, scripts, lights, etc.)");
        change26e.logs.add("Fix: Dashboard got launched sometimes when widget is clicked.");
        change26e.logs.add("Fix: App crash when you sign out with two or more servers configured.");
        items.put(change26e.versionCode, change26e);

        Changelog change26c = new Changelog(34, "Release v2.6c", "08 Nov 2017");
        change26c.logs.add("2.6c: Fixed NumberFormatException for Temperature attribute.");
        change26c.logs.add("2.6b: Added workaround to support HA 0.57 (Invalid HA Server)");
        change26c.logs.add("2.6b: Fixed wrongly pointed server in climate card.");
        //change26c.logs.add("Thank you for your patience! Banner ads testing is complete. It is replaced by Interstitial Ads that will run from now until 14th Nov.");
        items.put(change26c.versionCode, change26c);

        Changelog change26 = new Changelog(32, "Release v2.6", "06 Nov 2017");
        change26.logs.add("Feature: Quick Connection Switching. (Useful for users who switch between intranet and internet)");
        change26.logs.add("Feature: Support Climate Component.");
        change26.logs.add("Fix: Camera now works with HTTPS self-signed cert. (Previously showing connection error)");
        change26.logs.add("Added dialog for Switch component.");
        change26.logs.add("Slight modification to the dialog of Automation component.");
        change26.logs.add("Added Japanese localization.");
        items.put(change26.versionCode, change26);


        Changelog change25 = new Changelog(31, "Release v2.5", "31 Oct 2017");
        change25.logs.add("Feature: Logbook.");
        change25.logs.add("Fixed a bug where light dialog crashes on certain conditions.");
        change25.logs.add("Fixed a bug where media player controls missing/beneath the picture.");
        change25.logs.add("Added some lines in hope to fix widget crashing.");
        change25.logs.add("Added some debugging lines to collect data on why edit dashboard crashes sometimes.");
        change25.logs.add("Localization for Dutch. Thanks Dag! :)");
        change25.logs.add("Added Bootstrap file attachment option in bug reporting.");
        items.put(change25.versionCode, change25);


        Changelog change24 = new Changelog(30, "Release v2.4", "27 Oct 2017");
        change24.logs.add("Feature: Support MediaPlayer and Camera Components.");
        change24.logs.add("Added Color Temperature and Brightness sliders to Light Component. (Use Long Click to trigger advance/dialog mode)");
        change24.logs.add("All actions will now trigger instantly (removed set button in dialog)");
        change24.logs.add("Removed bottom click/long click behavior for tile. Tile will now only have two actions - Single Click and Long Click.");
        items.put(change24.versionCode, change24);


        Changelog change23b = new Changelog(29, "Release v2.3b", "26 Oct 2017");
        change23b.logs.add("Added Navigation Drawer.");
        change23b.logs.add("Added FAQs Section (using markdown).");
        change23b.logs.add("Fixed black notification icon bug in KitKat.");
        change23b.logs.add("Moved stuffs around to make it more intuitive.");
        change23b.logs.add("Fixed Bootstrap crash reported by Oleksii Serdiuk.");
        items.put(change23b.versionCode, change23b);

//        Changelog change23 = new Changelog(28, "Release v2.3", "26 Oct 2017");
//        change23.logs.add(".");
//        items.put(change23.versionCode, change23);

        Changelog change22c = new Changelog(27, "Release v2.2c", "24 Oct 2017");
        change22c.logs.add("Tons of bug fixes based on crashlytics report. I hope it works on kitkat now.");
        items.put(change22c.versionCode, change22c);

        Changelog change22b = new Changelog(26, "Release v2.2b", "23 Oct 2017");
        change22b.logs.add("Bug Fix: HTTPS for WebSocket.");
        items.put(change22b.versionCode, change22b);


        Changelog change22 = new Changelog(25, "Release v2.2", "23 Oct 2017");
        change22.logs.add("Feature: WebSocket. Polling mode is finally here!");
        items.put(change22.versionCode, change22);

        Changelog change21 = new Changelog(24, "Release v2.1", "22 Oct 2017");
        change21.logs.add("Feature: Widget! (Beta Version). I know it's still buggy, please just bear with me and report me the issues you found");
        change21.logs.add("Changed certain ON/OFF components to use icon.");
        change21.logs.add("Localization: Now support simplified and traditional chinese.");
        items.put(change21.versionCode, change21);


        Changelog change20d = new Changelog(23, "Release v2.0d", "20 Oct 2017");
        change20d.logs.add("Fixed 'Sorted by Order' bug.");
        change20d.logs.add("Fixed potential crash when opening Edit Dashboard.");
        items.put(change20d.versionCode, change20d);

        Changelog change20c = new Changelog(22, "Release v2.0c", "20 Oct 2017");
        change20c.logs.add("Fixed Crash reported by Vladislav Bykov.");
        items.put(change20c.versionCode, change20c);

        Changelog change20b = new Changelog(21, "Release v2.0b", "20 Oct 2017");
        change20b.logs.add("Localization: Updated for Russian.");
        items.put(change20b.versionCode, change20b);

        Changelog change20 = new Changelog(20, "Release v2.0", "20 Oct 2017");
        change20.logs.add("Feature: Support Material Design Icons");
        change20.logs.add("Feature: Multiple Dashboards");
        change20.logs.add("Using ReactiveX to coordinate UI updates.");
        items.put(change20.versionCode, change20);


        Changelog change19b = new Changelog(19, "Release v1.9b", "17 Oct 2017");
        change19b.logs.add("Fix attempt for crash in Oreo. (Starting in Oreo, Content notifications must now be backed by a valid ContentProvider)");
        change19b.logs.add("Localization: Updated for Russian.");
        items.put(change19b.versionCode, change19b);

        Changelog change19 = new Changelog(18, "Release v1.9", "16 Oct 2017");
        change19.logs.add("Feature: Optimized data loading approach. Butterly smooth scrolling.");
        change19.logs.add("Feature: Bottom Navigation.");
        change19.logs.add("Feature: Search in Dashboard.");
        change19.logs.add("Swapped Save and Add button in Edit Mode.");
        change19.logs.add("More Animations / Transitions");
        change19.logs.add("Added more bugs to fix later. :)");
        items.put(change19.versionCode, change19);

        Changelog change18h = new Changelog(17, "Release v1.8h", "15 Oct 2017");
        change18h.logs.add("Android Oreo round icon fix.");
        change18h.logs.add("Bug Fix: Bootstrap Crash reported by Robin Petterson.");
        change18h.logs.add("Added Bug Report link in Settings.");
        items.put(change18h.versionCode, change18h);

        Changelog change18g = new Changelog(16, "Release v1.8g", "14 Oct 2017");
        change18g.logs.add("Bug Fix: Crash for Light RGB color with decimal point value.");
        items.put(change18g.versionCode, change18g);

        Changelog change18f = new Changelog(15, "Release v1.8f", "14 Oct 2017");
        change18f.logs.add("Bug Fix: Crash for InputNumber step with decimal point value.");
        items.put(change18f.versionCode, change18f);

        Changelog change18e = new Changelog(14, "Release v1.8e", "13 Oct 2017");
        change18e.logs.add("Feature: Crash Reporting. (It seems to me a lot of users having troubles connecting to their HomeAssistant. I made this to speed up the debugging process)");
        items.put(change18e.versionCode, change18e);

        Changelog change18d = new Changelog(13, "Release v1.8d", "13 Oct 2017");
        change18d.logs.add("New App Icon.");
        change18d.logs.add("Added Russian translation.");
        items.put(change18d.versionCode, change18d);

        Changelog change18c = new Changelog(12, "Release v1.8c", "11 Oct 2017");
        change18c.logs.add("Support HTTPS self-signed cert.");
        items.put(change18c.versionCode, change18c);

        Changelog change18b = new Changelog(11, "Release v1.8b", "11 Oct 2017");
        change18b.logs.add("Bug Fix: Invalid character 'X' in SimpleDateFormat happening to devices with Android 6.0 and below.");
        items.put(change18b.versionCode, change18b);

        Changelog change18 = new Changelog(10, "Release v1.8", "11 Oct 2017");
        change18.logs.add("Feature: Line Graph for 'Sensor' Component. Sample screenshot available in Play Store.");
        change18.logs.add("Note: Looking for localization expert. Please contact me if you are willing to contribute. Credits will be given. Thanks!");
        items.put(change18.versionCode, change18);

        Changelog change17 = new Changelog(9, "Release v1.7", "09 Oct 2017");
        change17.logs.add("Added 'Confetti' in About.");
        change17.logs.add("Feature: Support toggle for 'Group' component.");
        change17.logs.add("Feature: Color Picker for Light. (Tap on the lower half of the tile)");
        change17.logs.add("Feature: Blur background for dialogs.");
        change17.logs.add("Feature: Support picture in 'Sensor' component.");
        change17.logs.add("Sensor to use a separate color scheme (default to green for now).");
        change17.logs.add("Added animation while tile is appearing.");
        change17.logs.add("Added checksum column in database. (Sign out and Sign in again in case of any crashes)");
        items.put(change17.versionCode, change17);

        Changelog change16c = new Changelog(8, "Release v1.6c", "08 Oct 2017");
        change16c.logs.add("Ditched Calligraphy in favor of Custom Font @ Android Suport Library 26. This will resolve the random crashing faced by a lot of the users lately.");
        change16c.logs.add("Now you can trigger automation by long pressing on the tile.");
        items.put(change16c.versionCode, change16c);

//        Changelog change16b = new Changelog(7, "Release v1.6b", "06 Oct 2017");
//        change16b.logs.add("Fixed crash when adding new control.");
//        items.put(7, change16b);

        Changelog change16 = new Changelog(6, "Release v1.6", "06 Oct 2017");
        change16.logs.add("Feature: Dashboard Sorting");
        change16.logs.add("EmptyView in dashboard.");
        change16.logs.add("Added Trello link in About.");
        change16.logs.add("Bugs and UI fixes.");
        items.put(change16.versionCode, change16);

        Changelog change15 = new Changelog(5, "Release v1.5", "04 Oct 2017");
        change15.logs.add("Feature: Dashboard Customization!");
        change15.logs.add("Feature: Changelog and What's New for features tracking.");
        change15.logs.add("Feature: Sound Effect on/off in Settings.");
        change15.logs.add("Tweak: Refresh Button on Toolbar.");
        change15.logs.add("Tweak: Fade animation to Dashboard on start.");
        change15.logs.add("Other minor bugs and UI fixes.");
        items.put(change15.versionCode, change15);

        Changelog change14 = new Changelog(4, "Release v1.4", "03 Oct 2017");
        change14.logs.add("Slightly rebranded. Changed color scheme to follow material guideline.");
        change14.logs.add("Changed to use BaseURL instead of IP during connection setup.");
        change14.logs.add("Use ContentProvider for more proper reflection of the data state.");
        change14.logs.add("Now support more toggles(Script, Automation, Boolean, MediaPlayer).");
        change14.logs.add("Support InputSlider and InputSelect.");
        change14.logs.add("Added Settings. Allow to customize column numbers.");
        change14.logs.add("Added WebUI shortcut menu.");
        change14.logs.add("Added Open Source Licenses page for proper attribution to the libraries used.");
        items.put(change14.versionCode, change14);

        Changelog change13 = new Changelog(3, "Release v1.3", "26 Aug 2017");
        change13.logs.add("Fixed Empty Tiles Bug.");
        change13.logs.add("Changed from Websocket to Rest API.");
        change13.logs.add("Added local database for bootstrapped data.");
        change13.logs.add("Use ViewPager for swiping between pages.");
        items.put(change13.versionCode, change13);

        Changelog change11 = new Changelog(1, "Release v1.1", "21 Aug 2017");
        change11.logs.add("Initial Release");
        items.put(change11.versionCode, change11);
    }

    public Changelog(int versionCode, String version, String date) {
        this.versionCode = versionCode;
        this.version = version;
        this.date = date;
    }

    @SerializedName("versionCode")
    public Integer versionCode;

    @SerializedName("version")
    public String version;

    @SerializedName("date")
    public String date;

    @SerializedName("logs")
    public ArrayList<String> logs = new ArrayList<>();

    @Override
    public String toString() {
        return (new Gson()).toJson(this);
    }

    public CharSequence getChangelog(Context context) {
        CharSequence result = "";
        for (int i = 0; i < logs.size(); i++) {
            Log.d("YouQi", logs.get(i));
            result = TextUtils.concat(result, CommonUtil.getSpanText(context, " - " + logs.get(i), R.color.md_blue_grey_500, null), i == (logs.size() - 1) ? "" : "\n");
        }
//        Spannable subString = new SpannableString(text);
//        subString.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null)), 0, subString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        response = TextUtils.concat(response, subString);

        return result;
    }
}