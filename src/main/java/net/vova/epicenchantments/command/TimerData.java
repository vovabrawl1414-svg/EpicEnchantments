package net.vova.epicenchantments.command;

public class TimerData {
    private long targetTime = 0;
    private long startTime = 0;
    private long pausedTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private TimerMode mode = TimerMode.STOPPED;

    // 👇 НОВОЕ: для предстартового отсчёта
    private boolean isCountdownActive = false;  // идёт ли предстартовый отсчёт
    private long countdownStartTime = 0;        // когда начался предстартовый отсчёт
    private boolean hasPlayedFinishSound = false;

    public enum TimerMode {
        STOPPED,
        COUNTDOWN,
        STOPWATCH
    }

    public void setCountdown(long milliseconds) {
        this.targetTime = milliseconds;
        this.mode = TimerMode.COUNTDOWN;
        this.isRunning = false;
        this.isPaused = false;
        this.isCountdownActive = false;
        this.hasPlayedFinishSound = false;
    }

    public void start() {
        if (mode == TimerMode.COUNTDOWN) {
            startTime = System.currentTimeMillis();
            isRunning = true;
            isPaused = false;
            this.hasPlayedFinishSound = false;
        } else if (mode == TimerMode.STOPPED) {
            // 👇 Запускаем ПРЕДСТАРТОВЫЙ отсчёт для секундомера
            mode = TimerMode.STOPWATCH;
            isCountdownActive = true;           // включаем режим предстарта
            countdownStartTime = System.currentTimeMillis(); // запоминаем время начала
            isRunning = false;                   // сам секундомер ещё не запущен
            isPaused = false;
        }
    }

    // 👇 НОВЫЙ МЕТОД: проверяет, идёт ли предстартовый отсчёт
    public boolean isCountdownActive() {
        return mode == TimerMode.STOPWATCH && isCountdownActive;
    }

    // 👇 НОВЫЙ МЕТОД: сколько секунд прошло с начала предстарта (0-4)
    public int getCountdownSecond() {
        if (!isCountdownActive) return -1;
        long elapsed = System.currentTimeMillis() - countdownStartTime;
        return (int)(elapsed / 1000);
    }

    // 👇 НОВЫЙ МЕТОД: проверить, нужно ли играть звук для текущей секунды предстарта
    public boolean shouldPlayCountdownSound(int second) {
        return isCountdownActive && second >= 0 && second <= 4;
    }

    // 👇 НОВЫЙ МЕТОД: запустить секундомер после предстарта
    public void startStopwatchAfterCountdown() {
        if (isCountdownActive) {
            isCountdownActive = false;
            isRunning = true;                    // теперь реально запускаем секундомер
            startTime = System.currentTimeMillis(); // с этого момента идёт отсчёт
        }
    }

    public void pause() {
        if (isRunning && !isPaused) {
            isPaused = true;
            pausedTime = System.currentTimeMillis();
        }
    }

    public void resume() {
        if (isRunning && isPaused) {
            long pauseDuration = System.currentTimeMillis() - pausedTime;
            startTime += pauseDuration;
            isPaused = false;
        }
    }

    public void stop() {
        isRunning = false;
        isPaused = false;
        isCountdownActive = false;
        mode = TimerMode.STOPPED;
        startTime = 0;
        targetTime = 0;
        countdownStartTime = 0;
        hasPlayedFinishSound = false;
    }

    public long getCurrentTime() {
        // Если идёт предстарт - показываем 00:00
        if (isCountdownActive) return 0;

        if (!isRunning) return 0;

        long now = System.currentTimeMillis();

        if (isPaused) {
            if (mode == TimerMode.COUNTDOWN) {
                return Math.max(0, targetTime - (pausedTime - startTime));
            } else {
                return pausedTime - startTime;
            }
        } else {
            if (mode == TimerMode.COUNTDOWN) {
                return Math.max(0, targetTime - (now - startTime));
            } else {
                return now - startTime;
            }
        }
    }

    public boolean isFinished() {
        return mode == TimerMode.COUNTDOWN && isRunning && !isPaused && getCurrentTime() <= 0;
    }

    public boolean shouldPlayFinishSound() {
        if (isFinished() && !hasPlayedFinishSound) {
            hasPlayedFinishSound = true;
            return true;
        }
        return false;
    }

    public TimerMode getMode() { return mode; }
    public boolean isRunning() { return isRunning; }
    public boolean isPaused() { return isPaused; }
}