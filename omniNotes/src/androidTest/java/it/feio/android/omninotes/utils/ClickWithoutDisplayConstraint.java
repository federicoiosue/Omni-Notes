package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.utils.VisibleViewMatcher.isVisible;
import static org.hamcrest.Matchers.allOf;

import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.PrecisionDescriber;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.Tapper;
import androidx.test.espresso.util.HumanReadables;
import java.util.Locale;
import java.util.Optional;
import org.hamcrest.Matcher;

/**
 * Custom click action similar to the GeneralClickAction provided by Espresso.
 * <p>
 * The only difference is that it does not force the target view to be displayed at least 90% on
 * screen (i.e., 90% of the view in sight of the user). In this custom class, the only constraint is
 * that the view needs to have "Visible" visibility and positive height and width. A typical example
 * is when a long form has a visible view at the bottom, but the UI needs to be scrolled to reach
 * it.
 */
public final class ClickWithoutDisplayConstraint implements ViewAction {

  private static final String TAG = "ClickWithoutDisplayConstraint";

  final CoordinatesProvider coordinatesProvider;
  final Tapper tapper;
  final PrecisionDescriber precisionDescriber;
  private final Optional<ViewAction> rollbackAction;
  private final int inputDevice;
  private final int buttonState;


  @Deprecated
  public ClickWithoutDisplayConstraint(
      Tapper tapper,
      CoordinatesProvider coordinatesProvider,
      PrecisionDescriber precisionDescriber) {
    this(tapper, coordinatesProvider, precisionDescriber, 0, 0, null);
  }

  public ClickWithoutDisplayConstraint(
      Tapper tapper,
      CoordinatesProvider coordinatesProvider,
      PrecisionDescriber precisionDescriber,
      int inputDevice,
      int buttonState) {
    this(tapper, coordinatesProvider, precisionDescriber, inputDevice, buttonState, null);
  }

  @Deprecated
  public ClickWithoutDisplayConstraint(
      Tapper tapper,
      CoordinatesProvider coordinatesProvider,
      PrecisionDescriber precisionDescriber,
      ViewAction rollbackAction) {
    this(tapper, coordinatesProvider, precisionDescriber, 0, 0, rollbackAction);
  }

  public ClickWithoutDisplayConstraint(
      Tapper tapper,
      CoordinatesProvider coordinatesProvider,
      PrecisionDescriber precisionDescriber,
      int inputDevice,
      int buttonState,
      ViewAction rollbackAction) {
    this.coordinatesProvider = coordinatesProvider;
    this.tapper = tapper;
    this.precisionDescriber = precisionDescriber;
    this.inputDevice = inputDevice;
    this.buttonState = buttonState;
    this.rollbackAction = Optional.ofNullable(rollbackAction);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Matcher<View> getConstraints() {
    Matcher<View> standardConstraint = isVisible();
    if (rollbackAction.isPresent()) {
      return allOf(standardConstraint, rollbackAction.get().getConstraints());
    } else {
      return standardConstraint;
    }
  }

  @Override
  public void perform(UiController uiController, View view) {
    float[] coordinates = coordinatesProvider.calculateCoordinates(view);
    float[] precision = precisionDescriber.describePrecision();

    Tapper.Status status = Tapper.Status.FAILURE;
    int loopCount = 0;
    // Native event injection is quite a tricky process. A tap is actually 2
    // seperate motion events which need to get injected into the system. Injection
    // makes an RPC call from our app under test to the Android system server, the
    // system server decides which window layer to deliver the event to, the system
    // server makes an RPC to that window layer, that window layer delivers the event
    // to the correct UI element, activity, or window object. Now we need to repeat
    // that 2x. for a simple down and up. Oh and the down event triggers timers to
    // detect whether or not the event is a long vs. short press. The timers are
    // removed the moment the up event is received (NOTE: the possibility of eventTime
    // being in the future is totally ignored by most motion event processors).
    //
    // Phew.
    //
    // The net result of this is sometimes we'll want to do a regular tap, and for
    // whatever reason the up event (last half) of the tap is delivered after long
    // press timeout (depending on system load) and the long press behaviour is
    // displayed (EG: show a context menu). There is no way to avoid or handle this more
    // gracefully. Also the longpress behavour is app/widget specific. So if you have
    // a seperate long press behaviour from your short press, you can pass in a
    // 'RollBack' ViewAction which when executed will undo the effects of long press.

    while (status != Tapper.Status.SUCCESS && loopCount < 3) {
      try {
        status = tapper.sendTap(uiController, coordinates, precision, inputDevice, buttonState);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
          Log.d(
              TAG,
              "perform: "
                  + String.format(
                  Locale.ROOT,
                  "%s - At Coordinates: %d, %d and precision: %d, %d",
                  this.getDescription(),
                  (int) coordinates[0],
                  (int) coordinates[1],
                  (int) precision[0],
                  (int) precision[1]));
        }
      } catch (RuntimeException re) {
        throw new PerformException.Builder()
            .withActionDescription(
                String.format(
                    Locale.ROOT,
                    "%s - At Coordinates: %d, %d and precision: %d, %d",
                    this.getDescription(),
                    (int) coordinates[0],
                    (int) coordinates[1],
                    (int) precision[0],
                    (int) precision[1]))
            .withViewDescription(HumanReadables.describe(view))
            .withCause(re)
            .build();
      }

      int duration = ViewConfiguration.getPressedStateDuration();
      // ensures that all work enqueued to process the tap has been run.
      if (duration > 0) {
        uiController.loopMainThreadForAtLeast(duration);
      }
      if (status == Tapper.Status.WARNING) {
        if (rollbackAction.isPresent()) {
          rollbackAction.get().perform(uiController, view);
        } else {
          break;
        }
      }
      loopCount++;
    }
    if (status == Tapper.Status.FAILURE) {
      throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(
              new RuntimeException(
                  String.format(
                      Locale.ROOT,
                      "Couldn't click at: %s,%s precision: %s, %s . Tapper: %s coordinate"
                          + " provider: %s precision describer: %s. Tried %s times. With Rollback?"
                          + " %s",
                      coordinates[0],
                      coordinates[1],
                      precision[0],
                      precision[1],
                      tapper,
                      coordinatesProvider,
                      precisionDescriber,
                      loopCount,
                      rollbackAction.isPresent())))
          .build();
    }

    if (tapper == Tap.SINGLE && view instanceof WebView) {
      // WebViews will not process click events until double tap
      // timeout. Not the best place for this - but good for now.
      uiController.loopMainThreadForAtLeast(ViewConfiguration.getDoubleTapTimeout());
    }
  }

  @Override
  public String getDescription() {
    return tapper.toString().toLowerCase() + " click";
  }
}