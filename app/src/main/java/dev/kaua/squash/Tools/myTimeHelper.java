package dev.kaua.squash.Tools;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class myTimeHelper {
    private final int hours;
    private final int minutes;

    public myTimeHelper(int hours, int minutes) {
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) throw new IllegalArgumentException();
        this.hours = hours;
        this.minutes = minutes;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public static myTimeHelper parse(String input) {
        char[] cs = input.toCharArray();
        if (cs.length != 5) throw new IllegalArgumentException();
        for (int i = 0; i < 5; i++) {
            if (i == 2) continue;
            if (cs[i] < '0' || cs[i] > '9') throw new IllegalArgumentException();
        }
        if (cs[2] != ':') throw new IllegalArgumentException();

        int h = (cs[0] - '0') * 10 + cs[1] - '0';
        int m = (cs[3] - '0') * 10 + cs[4] - '0';
        return new myTimeHelper(h, m);
    }

    public static myTimeHelper now() {
        GregorianCalendar gc = new GregorianCalendar();
        return new myTimeHelper(gc.get(Calendar.HOUR_OF_DAY), gc.get(Calendar.MINUTE));
    }

    public myTimeHelper difference(myTimeHelper outro) {
        int difHoras = this.hours - outro.hours;
        int difMinutes = this.minutes - outro.minutes;
        while (difMinutes < 0) {
            difMinutes += 60;
            difHoras--;
        }
        while (difHoras < 0) {
            difHoras += 24;
        }
        return new myTimeHelper(difHoras, difMinutes);
    }

    @Override
    public @NotNull String toString() {
        return ((hours < 10) ? "0" : "") + hours + ":" + ((minutes < 10) ? "0" : "") + minutes;
    }
}