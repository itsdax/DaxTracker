package scripts.dax.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.tribot.api.General;
import org.tribot.util.Util;
import scripts.dax.tracker.data.JwtContainer;
import scripts.dax.tracker.data.UserCredentials;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static scripts.dax.tracker.DaxLogger.*;

public class DaxTracker {

    private static final long SECOND_IN_MILLIS = 1000;
    private static final long MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS;

    // IMPORTANT: Anything faster will start to drop data and produce inaccurate results
    private static final long FIXED_UPLOAD_PERIOD = 5 * MINUTE_IN_MILLIS;

    private final DaxTrackerClient daxTrackerClient;
    private final Map<String, Long> data;
    private final Lock lock;
    private final Timer timer;
    private final Gson gson;

    private JwtContainer jwtContainer;
    private UserCredentials userCredentials;

    public DaxTracker(String scriptId, String secret) {
        this.daxTrackerClient = new DaxTrackerClient(scriptId, secret);
        this.data = new HashMap<>();
        this.lock = new ReentrantLock();
        this.timer = new Timer();
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            init();
        } catch (IOException e) {
            error("Unable to obtain DaxTracker account. Please try again later or try deleting: %s", getSettingsFilePath());
            e.printStackTrace();
            return;
        }
        run();
    }

    public void trackData(String name, long value) {
        this.lock.lock();
        try {
            data.compute(name, (s, initial) -> initial != null ? initial + value : value);
        } finally {
            this.lock.unlock();
        }
    }

    private void run() {
        info("Started DaxTracker with update frequency of %s", FIXED_UPLOAD_PERIOD);
        timer.scheduleAtFixedRate(task(), 0, FIXED_UPLOAD_PERIOD);
    }

    public void stop() {
        info("Stopping DaxTracker... Uploading remaining data before exit");
        timer.cancel();
        timer.purge();

        // run one last time
        task().run();
    }

    private TimerTask task() {
        return new TimerTask() {
            @Override
            public void run() {
                lock.lock();
                info("Uploading data to servers...");
                try {
                    if (data.isEmpty()) return;
                    if (track(data)) data.clear();
                } finally {
                    lock.unlock();
                }
            }
        };
    }

    private boolean track(Map<String, Long> map) {
        try {
            this.daxTrackerClient.trackData(jwtContainer, this.userCredentials.getId(), map);
            info("Successfully uploaded data to DaxTracker servers");
        } catch (Exception e) {
            warn("Unable to track data... trying again next iteration. Error(%s)", e.getMessage());
            return false;
        }
        return true;
    }

    private void init() throws IOException {
        this.userCredentials = getExistingAccount();

        if (this.userCredentials == null) {
            warn("No existing DaxTracker account. Creating one...");
            this.userCredentials = this.daxTrackerClient.createUser(General.getTRiBotUsername());
            DaxLogger.info(userCredentials.getSecretKey());
            String contents = gson.toJson(this.userCredentials);
            DaxLogger.info("Created User: %s", contents);
            Files.writeString(Path.of(getSettingsFilePath()), contents);
        }

        info("Loaded DaxTracker account from %s", getSettingsFilePath());
        this.jwtContainer = daxTrackerClient.login(userCredentials.getId(), userCredentials.getSecretKey());
    }

    private UserCredentials getExistingAccount() {
        try {
            return new Gson().fromJson(getSettingsFileContents(), UserCredentials.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSettingsFileContents() throws IOException {
        String contents = Files.readString(Path.of(getSettingsFilePath()));
        DaxLogger.info("Loaded contents for user credentials: %s", contents);
        return contents;
    }

    private String getSettingsFilePath() {
        return String.format("%s%s%s", Util.getWorkingDirectory(), File.separator, "daxTrackerConfig.json");
    }

}
