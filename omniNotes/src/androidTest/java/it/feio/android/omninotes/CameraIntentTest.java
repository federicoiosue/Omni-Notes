///*
// * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package it.feio.android.omninotes;
//
//import android.app.Activity;
//import android.app.Instrumentation;
//import android.content.Intent;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.support.test.espresso.intent.rule.IntentsTestRule;
//import android.support.test.runner.AndroidJUnit4;
//import android.test.suitebuilder.annotation.LargeTest;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.intent.Intents.intending;
//import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
//import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static it.feio.android.omninotes.BaseEspressoTest.childAtPosition;
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.is;
//
//
//@RunWith(AndroidJUnit4.class)
//@LargeTest
//public class CameraIntentTest {
//
//        @Rule
//        public IntentsTestRule<MainActivity> mIntentsRule = new IntentsTestRule<>(
//				MainActivity.class);
//
//        @Before
//        public void stubCameraIntent() {
//            Instrumentation.ActivityResult result = createImageCaptureStub();
//
//            // Stub the Intent.
//            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
//        }
//
//        @Test
//        public void testTakePhoto() {
//			onView(allOf(withId(R.id.fab_expand_menu_button),
//					childAtPosition(
//							allOf(withId(R.id.fab),
//									childAtPosition(
//											withClassName(is("android.widget.FrameLayout")),
//											2)),
//							3),
//					isDisplayed())).perform(click());
//
//			onView(allOf(withId(R.id.fab_camera),
//					childAtPosition(
//							allOf(withId(R.id.fab),
//									childAtPosition(
//											withClassName(is("android.widget.FrameLayout")),
//											2)),
//							0),
//					isDisplayed())).perform(click());
//
//		}
//
//        private Instrumentation.ActivityResult createImageCaptureStub() {
//            // Put the drawable in a bundle.
//            Bundle bundle = new Bundle();
//            bundle.putParcelable(MainActivity.KEY_IMAGE_DATA, BitmapFactory.decodeResource(
//                    mIntentsRule.getActivity().getResources(), R.drawable.ic_launcher));
//
//            // Create the Intent that will include the bundle.
//            Intent resultData = new Intent();
//            resultData.putExtras(bundle);
//
//            // Create the ActivityResult with the Intent.
//            return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
//        }
//    }
