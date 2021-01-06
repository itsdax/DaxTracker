# DaxTracker

Solution for tracking user stats and generating dynamic signature without the need to spin up your own infra.

## Usage

```java
package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
import scripts.dax.tracker.DaxTracker;

@ScriptManifest(name = "Test Script", authors = {"dax"}, category = "Tools")
public class TestScript extends Script implements Ending {

    private DaxTracker daxTracker;

    public TestScript() {
        daxTracker = new DaxTracker("API-KEY", "SECRET-KEY");
    }

    @Override
    public void run() {
        while (true) {
            // your script logic here
            if (stuff) doStuff();

            if (choppedDownTree()) {
                daxTracker.trackData("Log", 1);
                daxTracker.trackData("Woodcutting", 25);
            }

            // you can even track time ran. Use seconds instead of milliseconds since you might hit overflow
            daxTracker.trackData("runtime", 123456L);

            sleep(100);
        }
    }

    @Override
    public void onEnd() {
        // Always make sure to call stop to make sure all stats are uploaded to server before exit
        daxTracker.stop();
    }
}
```




