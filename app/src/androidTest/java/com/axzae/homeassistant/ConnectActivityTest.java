package com.axzae.homeassistant;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

//@RunWith(AndroidJUnit4.class)
public class ConnectActivityTest {
    //https://github.com/square/okhttp/tree/master/mockwebserver
    //https://github.com/chiuki/mockwebserver-demo
    //https://riggaroo.co.za/retrofit-2-mocking-http-responses/

    @Rule
    public ActivityTestRule<ConnectActivity> activityRule = new ActivityTestRule<>(ConnectActivity.class, true, false);

//    @Rule
//    public OkHttpIdlingResourceRule okHttpIdlingResourceRule = new OkHttpIdlingResourceRule();

    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule();

    @Before
    public void setBaseUrl() {
        //TestAppController app = (TestAppController) InstrumentationRegistry.getTargetContext().getApplicationContext();
        //app.setBaseUrl(mockWebServerRule.server.url("/").toString());

        Log.d("YouQi", "mock: " + mockWebServerRule.server.url("/").toString());
    }

    @Test
    public void loginTest() throws Exception {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(3598632);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        mockWebServerRule.server.setDispatcher(new MockDispatcher());
        activityRule.launchActivity(null);

        onView(allOf(withId(R.id.text_ipaddress), isDisplayed())).perform(
                click(),
                replaceText("http://127.0.0.1"),
                //closeSoftKeyboard(),
                pressImeActionButton()
        );

        onView(allOf(withId(R.id.text_password), isDisplayed())).perform(
                click(),
                replaceText("1234"),
                //closeSoftKeyboard(),
                pressImeActionButton()
        );

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
